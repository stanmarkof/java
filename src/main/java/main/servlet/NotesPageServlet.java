package main.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import main.model.Note;
import main.model.User;
import main.repository.NoteRepository;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.util.List;

@WebServlet("/notes")
public class NotesPageServlet extends HttpServlet {
    private TemplateEngine templateEngine;
    private NoteRepository noteRepository;

    @Override
    public void init() throws ServletException {
        System.out.println("=== Инициализация NotesPageServlet ===");
        try {
            // Инициализация Thymeleaf
            ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setPrefix("/templates/");
            templateResolver.setSuffix(".html");
            templateResolver.setTemplateMode(TemplateMode.HTML);
            templateResolver.setCharacterEncoding("UTF-8");
            templateResolver.setCacheable(false);

            templateEngine = new TemplateEngine();
            templateEngine.setTemplateResolver(templateResolver);

            noteRepository = new NoteRepository();
            System.out.println("NotesPageServlet успешно инициализирован");
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации NotesPageServlet: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Ошибка при инициализации NotesPageServlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("\n=== Обработка GET запроса на /notes ===");
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            System.out.println("Пользователь не авторизован, перенаправление на /login");
            response.sendRedirect("/login");
            return;
        }

        System.out.println("Пользователь авторизован: " + user.getUsername());

        try {
            // Получаем заметки пользователя
            List<Note> notes = noteRepository.findAllByUser(user);
            System.out.println("Получено заметок: " + notes.size());

            // Создаем контекст для Thymeleaf
            Context context = new Context();
            context.setVariable("username", user.getUsername());
            context.setVariable("notes", notes);
            System.out.println("Контекст Thymeleaf создан");

            // Проверяем наличие шаблона
            String templatePath = "/templates/pages/notes.html";
            if (getClass().getResource(templatePath) == null) {
                String error = "Шаблон не найден: " + templatePath;
                System.err.println(error);
                throw new ServletException(error);
            }
            System.out.println("Шаблон найден: " + templatePath);

            // Обрабатываем шаблон
            response.setContentType("text/html;charset=UTF-8");
            System.out.println("Начинаем обработку шаблона");
            templateEngine.process("pages/notes", context, response.getWriter());
            System.out.println("Шаблон успешно обработан");
        } catch (Exception e) {
            System.err.println("Ошибка при обработке запроса: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при загрузке страницы: " + e.getMessage());
        }
    }
} 