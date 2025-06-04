package main.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import main.model.Note;
import main.model.User;
import main.model.NoteFolder;
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
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/api/notes", "/api/notes/"})
public class NotesServlet extends HttpServlet {
    private final Gson gson;

    public NotesServlet() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        
        // Добавляем адаптер для LocalDateTime
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        });
        
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        });
        
        this.gson = gsonBuilder.create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("NotesServlet: Processing GET request");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            System.out.println("NotesServlet: User not found in session");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String folderId = request.getParameter("folderId");
        System.out.println("NotesServlet: Folder ID from request: " + folderId);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Note> notes;
            if (folderId != null && !folderId.isEmpty()) {
                // Загружаем заметки только из указанной папки
                notes = session.createQuery("FROM Note n LEFT JOIN FETCH n.folder WHERE n.user = :user AND n.folder.id = :folderId", Note.class)
                        .setParameter("user", user)
                        .setParameter("folderId", Long.parseLong(folderId))
                        .list();
                System.out.println("NotesServlet: Loading notes for folder " + folderId);
            } else {
                // Загружаем только заметки без папки
                notes = session.createQuery("FROM Note n LEFT JOIN FETCH n.folder WHERE n.user = :user AND n.folder IS NULL", Note.class)
                        .setParameter("user", user)
                        .list();
                System.out.println("NotesServlet: Loading notes without folder");
            }
            
            System.out.println("NotesServlet: Found " + notes.size() + " notes");
            
            // Создаем список DTO для сериализации
            List<Map<String, Object>> noteDtos = notes.stream()
                .map(note -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", note.getId());
                    dto.put("title", note.getTitle());
                    dto.put("content", note.getContent());
                    dto.put("createdAt", note.getCreatedAt());
                    dto.put("updatedAt", note.getUpdatedAt());
                    dto.put("folderId", note.getFolder() != null ? note.getFolder().getId() : null);
                    return dto;
                })
                .collect(Collectors.toList());

            sendJsonResponse(response, noteDtos);
        } catch (Exception e) {
            System.out.println("NotesServlet: Error processing GET request: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("NotesServlet: Processing POST request");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            System.out.println("NotesServlet: User not found in session");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Session session = null;
        Transaction transaction = null;

        try {
            String requestBody = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            System.out.println("NotesServlet: Request body: " + requestBody);
            
            Note note = gson.fromJson(requestBody, Note.class);
            System.out.println("NotesServlet: Parsed note: " + note);
            
            if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Title is required");
            }
            
            // Открываем сессию для работы с базой данных
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Загружаем пользователя в текущей сессии
            user = session.get(User.class, user.getId());
            note.setUser(user);

            // Обработка папки
            if (note.getFolderId() != null) {
                System.out.println("NotesServlet: Processing folder with ID: " + note.getFolderId());
                NoteFolder folder = session.get(NoteFolder.class, note.getFolderId());
                if (folder != null && folder.getUser().getId().equals(user.getId())) {
                    System.out.println("NotesServlet: Found valid folder: " + folder.getName());
                    note.setFolder(folder);
                } else {
                    System.out.println("NotesServlet: Invalid folder or folder not found");
                    note.setFolder(null);
                }
            }

            // Сохраняем заметку
            session.save(note);
            transaction.commit();
            System.out.println("NotesServlet: Note created successfully with ID: " + note.getId());

            // Создаем DTO для ответа
            Map<String, Object> noteDto = new HashMap<>();
            noteDto.put("id", note.getId());
            noteDto.put("title", note.getTitle());
            noteDto.put("content", note.getContent());
            noteDto.put("createdAt", note.getCreatedAt());
            noteDto.put("updatedAt", note.getUpdatedAt());
            noteDto.put("folderId", note.getFolder() != null ? note.getFolder().getId() : null);

            sendJsonResponse(response, noteDto);

        } catch (IllegalArgumentException e) {
            System.out.println("NotesServlet: Validation error: " + e.getMessage());
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            System.out.println("NotesServlet: Error processing POST request: " + e.getMessage());
            e.printStackTrace();
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request data: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("NotesServlet: Processing PUT request");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = request.getPathInfo();
        User user = (User) request.getSession().getAttribute("user");

        if (user == null || pathInfo == null) {
            System.out.println("NotesServlet: User not found or invalid path");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String noteId = pathInfo.substring(1);
            System.out.println("NotesServlet: Updating note with ID: " + noteId);

            String requestBody = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            System.out.println("NotesServlet: Request body: " + requestBody);
            
            Note note = gson.fromJson(requestBody, Note.class);
            
            if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Title is required");
            }

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                
                Note existingNote = session.get(Note.class, Long.parseLong(noteId));
                
                if (existingNote == null || !existingNote.getUser().getId().equals(user.getId())) {
                    System.out.println("NotesServlet: Note not found or access denied");
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                // Обновляем поля заметки
                existingNote.setTitle(note.getTitle());
                existingNote.setContent(note.getContent());
                existingNote.setUpdatedAt(LocalDateTime.now());

                // Обработка папки
                if (note.getFolderId() != null) {
                    System.out.println("NotesServlet: Processing folder with ID: " + note.getFolderId());
                    NoteFolder folder = session.get(NoteFolder.class, note.getFolderId());
                    if (folder != null && folder.getUser().getId().equals(user.getId())) {
                        System.out.println("NotesServlet: Found valid folder: " + folder.getName());
                        existingNote.setFolder(folder);
                    } else {
                        System.out.println("NotesServlet: Invalid folder or folder not found");
                        existingNote.setFolder(null);
                    }
                } else {
                    existingNote.setFolder(null);
                }

                session.update(existingNote);
                transaction.commit();
                
                System.out.println("NotesServlet: Note updated successfully");
                
                // Создаем DTO для ответа
                Map<String, Object> noteDto = new HashMap<>();
                noteDto.put("id", existingNote.getId());
                noteDto.put("title", existingNote.getTitle());
                noteDto.put("content", existingNote.getContent());
                noteDto.put("createdAt", existingNote.getCreatedAt());
                noteDto.put("updatedAt", existingNote.getUpdatedAt());
                noteDto.put("folderId", existingNote.getFolder() != null ? existingNote.getFolder().getId() : null);

                sendJsonResponse(response, noteDto);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("NotesServlet: Validation error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            System.out.println("NotesServlet: Error processing PUT request: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("NotesServlet: Processing DELETE request");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = request.getPathInfo();
        User user = (User) request.getSession().getAttribute("user");

        if (user == null || pathInfo == null) {
            System.out.println("NotesServlet: User not found or invalid path");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String noteId = pathInfo.substring(1);
            System.out.println("NotesServlet: Deleting note with ID: " + noteId);

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                
                Note note = session.get(Note.class, Long.parseLong(noteId));
                
                if (note == null || !note.getUser().getId().equals(user.getId())) {
                    System.out.println("NotesServlet: Note not found or access denied");
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                session.delete(note);
                transaction.commit();
                
                System.out.println("NotesServlet: Note deleted successfully");
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (Exception e) {
            System.out.println("NotesServlet: Error processing DELETE request: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        System.out.println("NotesServlet: Sending response: " + jsonResponse);
        response.getWriter().write(jsonResponse);
    }
}

