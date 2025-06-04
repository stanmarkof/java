package main.servlet;

import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.Transaction;
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
import main.model.User;
import main.util.HibernateUtil;

public class RegisterServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user != null) {
            response.sendRedirect("/calendar");
            return;
        }

        ServletContext servletContext = request.getServletContext();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        WebContext context = new WebContext(application.buildExchange(request, response));
        
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("auth/register", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!password.equals(confirmPassword)) {
            ServletContext servletContext = request.getServletContext();
            JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
            WebContext context = new WebContext(application.buildExchange(request, response));
            context.setVariable("error", "Пароли не совпадают");
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("auth/register", context, response.getWriter());
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            
            session.persist(user);
            transaction.commit();
            
            response.sendRedirect("/");
        } catch (Exception e) {
            ServletContext servletContext = request.getServletContext();
            JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
            WebContext context = new WebContext(application.buildExchange(request, response));
            context.setVariable("error", "Ошибка при регистрации: " + e.getMessage());
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("auth/register", context, response.getWriter());
        }
    }
} 