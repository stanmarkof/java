package main.servlet;

import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.query.Query;
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

public class LoginServlet extends HttpServlet {
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
        templateEngine.process("index", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String login = request.getParameter("login");
        String password = request.getParameter("password");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Проверяем, является ли введенное значение email'ом
            boolean isEmail = login.contains("@");
            
            String hql;
            if (isEmail) {
                hql = "FROM User WHERE email = :login AND password = :password";
            } else {
                hql = "FROM User WHERE username = :login AND password = :password";
            }
            
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("login", login);
            query.setParameter("password", password);
            
            User user = query.uniqueResult();
            
            if (user != null) {
                HttpSession httpSession = request.getSession();
                httpSession.setAttribute("user", user);
                response.sendRedirect("/calendar");
            } else {
                ServletContext servletContext = request.getServletContext();
                JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
                WebContext context = new WebContext(application.buildExchange(request, response));
                context.setVariable("error", "Неверный логин/email или пароль");
                
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("index", context, response.getWriter());
            }
        } catch (Exception e) {
            ServletContext servletContext = request.getServletContext();
            JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
            WebContext context = new WebContext(application.buildExchange(request, response));
            context.setVariable("error", "Ошибка при входе: " + e.getMessage());
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("index", context, response.getWriter());
        }
    }
} 