package main.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import main.model.NoteFolder;
import main.model.User;
import main.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet(urlPatterns = {"/api/folders", "/api/folders/"})
public class NoteFolderServlet extends HttpServlet {
    private final Gson gson;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public NoteFolderServlet() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
            context.serialize(src.format(formatter)));
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
            LocalDateTime.parse(json.getAsString(), formatter));
        this.gson = gsonBuilder.create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("NoteFolderServlet: Processing GET request");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            System.out.println("NoteFolderServlet: User not found in session");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<NoteFolder> folders = session.createQuery("FROM NoteFolder WHERE user = :user", NoteFolder.class)
                    .setParameter("user", user)
                    .list();
            System.out.println("NoteFolderServlet: Found " + folders.size() + " folders");
            sendJsonResponse(response, folders);
        } catch (Exception e) {
            System.out.println("NoteFolderServlet: Error processing GET request: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("NoteFolderServlet: Processing POST request");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            System.out.println("NoteFolderServlet: User not found in session");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String requestBody = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            System.out.println("NoteFolderServlet: Request body: " + requestBody);
            
            if (requestBody == null || requestBody.trim().isEmpty()) {
                throw new IllegalArgumentException("Request body is empty");
            }

            NoteFolder folder;
            try {
                folder = gson.fromJson(requestBody, NoteFolder.class);
            } catch (JsonSyntaxException e) {
                System.out.println("NoteFolderServlet: Invalid JSON format: " + e.getMessage());
                throw new IllegalArgumentException("Invalid JSON format: " + e.getMessage());
            }

            if (folder.getName() == null || folder.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Folder name is required");
            }

            folder.setUser(user);
            folder.setCreatedAt(LocalDateTime.now());

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                session.save(folder);
                transaction.commit();
                System.out.println("NoteFolderServlet: Folder created successfully");
                sendJsonResponse(response, folder);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("NoteFolderServlet: Validation error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            System.out.println("NoteFolderServlet: Error processing POST request: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("NoteFolderServlet: Processing PUT request");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = request.getPathInfo();
        User user = (User) request.getSession().getAttribute("user");

        if (user == null || pathInfo == null) {
            System.out.println("NoteFolderServlet: User not found or invalid path");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String folderId = pathInfo.substring(1);
            System.out.println("NoteFolderServlet: Updating folder with ID: " + folderId);

            String requestBody = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            System.out.println("NoteFolderServlet: Request body: " + requestBody);
            
            NoteFolder updatedFolder = gson.fromJson(requestBody, NoteFolder.class);
            
            if (updatedFolder.getName() == null || updatedFolder.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Folder name is required");
            }

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                
                NoteFolder existingFolder = session.get(NoteFolder.class, Long.parseLong(folderId));
                
                if (existingFolder == null || !existingFolder.getUser().getId().equals(user.getId())) {
                    System.out.println("NoteFolderServlet: Folder not found or access denied");
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                // Обновляем поля папки
                existingFolder.setName(updatedFolder.getName());
                existingFolder.setDescription(updatedFolder.getDescription());

                session.update(existingFolder);
                transaction.commit();
                
                System.out.println("NoteFolderServlet: Folder updated successfully");
                sendJsonResponse(response, existingFolder);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("NoteFolderServlet: Validation error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            System.out.println("NoteFolderServlet: Error processing PUT request: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        User user = (User) request.getSession().getAttribute("user");

        if (user == null || pathInfo == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String folderId = pathInfo.substring(1);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            NoteFolder folder = session.get(NoteFolder.class, Long.parseLong(folderId));
            
            if (folder == null || !folder.getUser().getId().equals(user.getId())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            session.delete(folder);
            transaction.commit();
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        System.out.println("NoteFolderServlet: Sending response: " + jsonResponse);
        response.getWriter().write(jsonResponse);
    }
} 