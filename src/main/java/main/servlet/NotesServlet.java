package main.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import main.model.Note;
import main.model.User;
import main.repository.NoteRepository;

public class NotesServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    private NoteRepository noteRepository;

    @Override
    public void init() throws ServletException {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/jpln",
                    "root",
                    "marina17"
            );
            noteRepository = new NoteRepository(connection);
        } catch (Exception e) {
            throw new ServletException("Database connection error", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("/");
            return;
        }

        List<Note> userNotes = List.of();
        try {
            userNotes = noteRepository.getNotesByUserId(user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ServletContext servletContext = request.getServletContext();
        JakartaServletWebApplication app =
                JakartaServletWebApplication.buildApplication(servletContext);
        WebContext ctx = new WebContext(app.buildExchange(request, response));
        ctx.setVariable("username", user.getUsername());
        ctx.setVariable("notes", userNotes);

        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("pages/notes", ctx, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            // Удаление заметки
            String idParam = request.getParameter("id");
            if (idParam != null && !idParam.isBlank()) {
                try {
                    int id = Integer.parseInt(idParam);
                    noteRepository.deleteNoteById(id);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            response.sendRedirect("/notes");
            return;
        }

        // Добавление или обновление заметки
        String idParam = request.getParameter("id");
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String returnTo = request.getParameter("returnTo");

        System.out.println("Received note data:");
        System.out.println("ID: " + idParam);
        System.out.println("Title: " + title);
        System.out.println("Content: " + content);
        System.out.println("User ID: " + user.getId());

        if (title != null && content != null && !title.isBlank() && !content.isBlank()) {
            Note note = new Note();
            note.setTitle(title);
            note.setContent(content);
            note.setUserId(user.getId());

            try {
                if (idParam != null && !idParam.isBlank()) {
                    note.setId(Integer.parseInt(idParam));
                    noteRepository.updateNote(note);
                    System.out.println("Note updated successfully");
                } else {
                    noteRepository.addNote(note);
                    System.out.println("Note added successfully");
                }
            } catch (SQLException e) {
                System.err.println("Error saving note: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Note not saved: title or content is empty");
        }

        // Перенаправляем на нужную страницу
        if ("calendar".equals(returnTo)) {
            response.sendRedirect("/calendar");
        } else {
            response.sendRedirect("/notes");
        }
    }
}

