package main.servlet;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import main.model.Task;
import main.model.User;
import main.util.HibernateUtil;

public class TaskServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(TaskServlet.class.getName());
    private TemplateEngine templateEngine;
    private Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        try {
            LogManager.getLogManager().readConfiguration(
                getClass().getClassLoader().getResourceAsStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        
        logger.info("TaskServlet инициализирован");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("\n=== Начало обработки GET запроса ===");
        System.out.println("URI: " + request.getRequestURI());
        System.out.println("PathInfo: " + request.getPathInfo());
        System.out.println("Accept: " + request.getHeader("Accept"));
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Method: " + request.getMethod());
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            System.out.println("Пользователь не авторизован");
            response.sendRedirect("/");
            return;
        }
        
        System.out.println("Пользователь: " + user.getUsername());

        try {
            String pathInfo = request.getPathInfo();
            String accept = request.getHeader("Accept");
            boolean isJsonRequest = accept != null && accept.contains("application/json");
            
            System.out.println("PathInfo: " + pathInfo);
            System.out.println("Is JSON request: " + isJsonRequest);

            // Если это запрос на получение задач по дате
            if (pathInfo != null && pathInfo.startsWith("/date/")) {
                String dateStr = pathInfo.substring(6);
                System.out.println("Запрос на получение задач на дату: " + dateStr);
                
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                
                try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
                    LocalDateTime startOfDay = LocalDateTime.parse(dateStr + "T00:00:00");
                    LocalDateTime endOfDay = LocalDateTime.parse(dateStr + "T23:59:59");
                    
                    System.out.println("Поиск задач в диапазоне:");
                    System.out.println("Начало дня: " + startOfDay);
                    System.out.println("Конец дня: " + endOfDay);
                    
                    Query<Task> query = hibernateSession.createQuery(
                        "SELECT t FROM Task t WHERE t.user = :user AND t.startTime BETWEEN :startOfDay AND :endOfDay ORDER BY t.startTime", 
                        Task.class);
                    query.setParameter("user", user);
                    query.setParameter("startOfDay", startOfDay);
                    query.setParameter("endOfDay", endOfDay);
                    
                    List<Task> tasks = query.list();
                    System.out.println("Найдено задач на дату: " + tasks.size());
                    
                    if (tasks.size() > 0) {
                        System.out.println("Детали найденных задач:");
                        for (Task task : tasks) {
                            System.out.println("Задача ID: " + task.getId());
                            System.out.println("Название: " + task.getTitle());
                            System.out.println("Время начала: " + task.getStartTime());
                            System.out.println("Время окончания: " + task.getEndTime());
                            System.out.println("---");
                        }
                    }
                    
                    List<Map<String, Object>> tasksData = tasks.stream()
                        .map(task -> {
                            Map<String, Object> taskData = new HashMap<>();
                            taskData.put("id", task.getId());
                            taskData.put("title", task.getTitle());
                            taskData.put("description", task.getDescription());
                            taskData.put("startTime", task.getStartTime());
                            taskData.put("endTime", task.getEndTime());
                            taskData.put("priority", task.getPriority());
                            taskData.put("completed", task.isCompleted());
                            return taskData;
                        })
                        .collect(Collectors.toList());
                    
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                    String jsonResponse = mapper.writeValueAsString(tasksData);
                    System.out.println("Отправляем JSON ответ: " + jsonResponse);
                    response.getWriter().write(jsonResponse);
                }
                return;
            }

            // Если это запрос на получение конкретной задачи
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                String taskId = pathInfo.substring(1);
                System.out.println("Запрос на получение задачи с ID: " + taskId);
                
                // Устанавливаем заголовки для JSON ответа
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
                
                try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
                    Query<Task> query = hibernateSession.createQuery(
                        "SELECT t FROM Task t LEFT JOIN FETCH t.user WHERE t.id = :id AND t.user = :user", Task.class);
                    query.setParameter("id", Long.parseLong(taskId));
                    query.setParameter("user", user);
                    Task task = query.uniqueResult();
                    
                    if (task != null) {
                        System.out.println("Задача найдена: " + task.getTitle());
                        
                        Map<String, Object> responseData = new HashMap<>();
                        responseData.put("success", true);
                        
                        // Создаем упрощенное представление задачи
                        Map<String, Object> taskData = new HashMap<>();
                        taskData.put("id", task.getId());
                        taskData.put("title", task.getTitle());
                        taskData.put("description", task.getDescription());
                        taskData.put("startTime", task.getStartTime());
                        taskData.put("endTime", task.getEndTime());
                        taskData.put("priority", task.getPriority());
                        taskData.put("completed", task.isCompleted());
                        taskData.put("userId", task.getUser().getId());
                        taskData.put("username", task.getUser().getUsername());
                        
                        responseData.put("task", taskData);
                        
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.registerModule(new JavaTimeModule());
                        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                        String jsonResponse = mapper.writeValueAsString(responseData);
                        System.out.println("Отправляем JSON ответ: " + jsonResponse);
                        response.getWriter().write(jsonResponse);
                    } else {
                        System.out.println("Задача не найдена");
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "Задача не найдена");
                        ObjectMapper mapper = new ObjectMapper();
                        String jsonResponse = mapper.writeValueAsString(errorResponse);
                        System.out.println("Отправляем JSON ответ об ошибке: " + jsonResponse);
                        response.getWriter().write(jsonResponse);
                    }
                }
                return;
            }
            
            // Если это запрос на получение списка задач в формате JSON
            if (isJsonRequest) {
                // Устанавливаем заголовки для JSON ответа
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");

                System.out.println("Запрос на получение списка задач в формате JSON");
                try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
                    Query<Task> query = hibernateSession.createQuery(
                        "SELECT t FROM Task t LEFT JOIN FETCH t.user WHERE t.user = :user ORDER BY t.startTime", Task.class);
                    query.setParameter("user", user);
                    List<Task> tasks = query.list();
                    
                    System.out.println("Найдено задач: " + tasks.size());
                    
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("success", true);
                    
                    // Создаем упрощенное представление списка задач
                    List<Map<String, Object>> tasksData = tasks.stream()
                        .map(task -> {
                            Map<String, Object> taskData = new HashMap<>();
                            taskData.put("id", task.getId());
                            taskData.put("title", task.getTitle());
                            taskData.put("description", task.getDescription());
                            taskData.put("startTime", task.getStartTime());
                            taskData.put("endTime", task.getEndTime());
                            taskData.put("priority", task.getPriority());
                            taskData.put("completed", task.isCompleted());
                            taskData.put("userId", task.getUser().getId());
                            taskData.put("username", task.getUser().getUsername());
                            return taskData;
                        })
                        .collect(Collectors.toList());
                    
                    responseData.put("tasks", tasksData);
                    
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                    String jsonResponse = mapper.writeValueAsString(responseData);
                    System.out.println("Отправляем JSON ответ: " + jsonResponse);
                    response.getWriter().write(jsonResponse);
                }
                return;
            }
            
            // Если это запрос на отображение HTML страницы
            System.out.println("Запрос на отображение HTML страницы");
            ServletContext servletContext = request.getServletContext();
            JakartaServletWebApplication app = JakartaServletWebApplication.buildApplication(servletContext);
            WebContext ctx = new WebContext(app.buildExchange(request, response));
            ctx.setVariable("username", user.getUsername());
            
            try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
                Query<Task> query = hibernateSession.createQuery(
                    "SELECT t FROM Task t LEFT JOIN FETCH t.user WHERE t.user = :user ORDER BY t.startTime", Task.class);
                query.setParameter("user", user);
                List<Task> tasks = query.list();
                System.out.println("Найдено задач для HTML: " + tasks.size());
                ctx.setVariable("tasks", tasks);
            }

            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pages/tasks", ctx, response.getWriter());
            
        } catch (Exception e) {
            System.out.println("Ошибка при обработке запроса: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Внутренняя ошибка сервера: " + e.getMessage());
            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(errorResponse);
            System.out.println("Отправляем JSON ответ об ошибке: " + jsonResponse);
            response.getWriter().write(jsonResponse);
        }
        
        System.out.println("=== Конец обработки GET запроса ===\n");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("\n=== Начало обработки POST запроса ===");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            System.out.println("Пользователь не авторизован");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        System.out.println("Пользователь: " + user.getUsername());

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.matches("/\\d+/toggle")) {
            String taskId = pathInfo.substring(1, pathInfo.length() - 7);
            toggleTaskStatus(response, taskId, user);
            return;
        }

        // Проверяем, является ли это запросом на удаление
        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            String taskId = request.getParameter("id");
            if (taskId != null && !taskId.isEmpty()) {
                try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
                    Transaction transaction = hibernateSession.beginTransaction();
                    
                    Task task = hibernateSession.createQuery(
                        "SELECT t FROM Task t WHERE t.id = :id AND t.user = :user", Task.class)
                        .setParameter("id", Long.parseLong(taskId))
                        .setParameter("user", user)
                        .uniqueResult();
                        
                    if (task == null) {
                        System.out.println("Задача не найдена или не принадлежит пользователю");
                        sendJsonResponse(response, false, "Задача не найдена");
                        return;
                    }
                    
                    hibernateSession.remove(task);
                    transaction.commit();
                    
                    System.out.println("Задача успешно удалена");
                    sendJsonResponse(response, true, "Задача успешно удалена");
                } catch (Exception e) {
                    System.out.println("Ошибка при удалении задачи: " + e.getMessage());
                    e.printStackTrace();
                    sendJsonResponse(response, false, "Ошибка при удалении задачи: " + e.getMessage());
                }
                return;
            }
        }

        String contentType = request.getContentType();
        System.out.println("Content-Type: " + contentType);
        
        String title, description, startTimeStr, endTimeStr, priority, completedStr;
        String taskId = null;

        if (contentType != null && contentType.contains("application/json")) {
            String jsonBody = request.getReader().lines().collect(Collectors.joining());
            System.out.println("Получен JSON: " + jsonBody);
            
            JsonObject jsonData = gson.fromJson(jsonBody, JsonObject.class);
            taskId = jsonData.has("id") ? jsonData.get("id").getAsString() : null;
            title = jsonData.has("title") ? jsonData.get("title").getAsString() : null;
            description = jsonData.has("description") ? jsonData.get("description").getAsString() : null;
            startTimeStr = jsonData.has("startTime") ? jsonData.get("startTime").getAsString() : null;
            endTimeStr = jsonData.has("endTime") ? jsonData.get("endTime").getAsString() : null;
            priority = jsonData.has("priority") ? jsonData.get("priority").getAsString() : null;
            completedStr = jsonData.has("completed") ? jsonData.get("completed").getAsString() : null;
            
            System.out.println("Извлеченные данные из JSON:");
            System.out.println("ID: " + taskId);
            System.out.println("Title: " + title);
            System.out.println("StartTime: " + startTimeStr);
            System.out.println("EndTime: " + endTimeStr);
        } else {
            taskId = request.getParameter("id");
            title = request.getParameter("title");
            description = request.getParameter("description");
            startTimeStr = request.getParameter("startTime");
            endTimeStr = request.getParameter("endTime");
            priority = request.getParameter("priority");
            completedStr = request.getParameter("completed");
        }

        if (title == null || title.trim().isEmpty() || startTimeStr == null || startTimeStr.trim().isEmpty()) {
            System.out.println("Ошибка: отсутствуют обязательные поля");
            sendJsonResponse(response, false, "Название и время начала обязательны");
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
            LocalDateTime endTime = endTimeStr != null && !endTimeStr.isEmpty() ? 
                LocalDateTime.parse(endTimeStr, formatter) : null;
            boolean completed = completedStr != null && completedStr.equals("true");

            try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
                Transaction transaction = hibernateSession.beginTransaction();
                
                Task task;
                if (taskId != null && !taskId.isEmpty()) {
                    System.out.println("Обновление существующей задачи с ID: " + taskId);
                    Long id = Long.parseLong(taskId);
                    task = hibernateSession.createQuery(
                        "SELECT t FROM Task t WHERE t.id = :id AND t.user = :user", Task.class)
                        .setParameter("id", id)
                        .setParameter("user", user)
                        .uniqueResult();
                        
                    if (task == null) {
                        System.out.println("Задача не найдена или не принадлежит пользователю");
                        sendJsonResponse(response, false, "Задача не найдена");
                        return;
                    }
                } else {
                    System.out.println("Создание новой задачи");
                    task = new Task();
                    task.setUser(user);
                }
                
                task.setTitle(title);
                task.setDescription(description);
                task.setStartTime(startTime);
                task.setEndTime(endTime);
                task.setPriority(priority);
                task.setCompleted(completed);
                
                hibernateSession.merge(task);
                transaction.commit();
                
                System.out.println("Задача успешно сохранена");
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Задача успешно сохранена");
                responseData.put("taskId", task.getId());
                
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(new ObjectMapper().writeValueAsString(responseData));
            }
        } catch (Exception e) {
            System.out.println("Ошибка при сохранении задачи: " + e.getMessage());
            e.printStackTrace();
            sendJsonResponse(response, false, "Ошибка при сохранении задачи: " + e.getMessage());
        }
        
        System.out.println("=== Конец обработки POST запроса ===\n");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("\n=== Начало обработки DELETE запроса ===");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            System.out.println("Пользователь не авторизован");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        System.out.println("Пользователь: " + user.getUsername());

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            System.out.println("Неверный формат ID задачи");
            sendJsonResponse(response, false, "Неверный ID задачи");
            return;
        }

        String taskId = pathInfo.substring(1);
        System.out.println("Удаление задачи с ID: " + taskId);
        
        try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = hibernateSession.beginTransaction();
            
            Task task = hibernateSession.createQuery(
                "SELECT t FROM Task t WHERE t.id = :id AND t.user = :user", Task.class)
                .setParameter("id", Long.parseLong(taskId))
                .setParameter("user", user)
                .uniqueResult();
                
            if (task == null) {
                System.out.println("Задача не найдена или не принадлежит пользователю");
                sendJsonResponse(response, false, "Задача не найдена");
                return;
            }
            
            hibernateSession.remove(task);
            transaction.commit();
            
            System.out.println("Задача успешно удалена");
            sendJsonResponse(response, true, "Задача успешно удалена");
        } catch (Exception e) {
            System.out.println("Ошибка при удалении задачи: " + e.getMessage());
            e.printStackTrace();
            sendJsonResponse(response, false, "Ошибка при удалении задачи: " + e.getMessage());
        }
        
        System.out.println("=== Конец обработки DELETE запроса ===\n");
    }

    private void toggleTaskStatus(HttpServletResponse response, String taskId, User user) throws IOException {
        try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = hibernateSession.beginTransaction();
            
            Task task = hibernateSession.get(Task.class, Long.parseLong(taskId));
            if (task == null || !task.getUser().equals(user)) {
                sendJsonResponse(response, false, "Задача не найдена");
                return;
            }
            
            task.setCompleted(!task.isCompleted());
            hibernateSession.persist(task);
            transaction.commit();
            
            sendJsonResponse(response, true, "Статус задачи успешно изменен");
        } catch (Exception e) {
            sendJsonResponse(response, false, "Ошибка при изменении статуса задачи: " + e.getMessage());
        }
    }

    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", message);
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        response.getWriter().write(mapper.writeValueAsString(responseData));
    }
} 