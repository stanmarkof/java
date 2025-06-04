package main;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import main.servlet.CalendarServlet;
import main.servlet.IndexServlet;
import main.servlet.LoginServlet;
import main.servlet.NotesPageServlet;
import main.servlet.NotesServlet;
import main.servlet.NoteFolderServlet;
import main.servlet.RegisterServlet;
import main.servlet.TaskServlet;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        
        // Настройка контекста сервлетов
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        
        // Регистрация сервлетов
        context.addServlet(new ServletHolder(new IndexServlet()), "/");
        context.addServlet(new ServletHolder(new LoginServlet()), "/login");
        context.addServlet(new ServletHolder(new RegisterServlet()), "/register");
        context.addServlet(new ServletHolder(new TaskServlet()), "/tasks/*");
        context.addServlet(new ServletHolder(new CalendarServlet()), "/calendar");
        context.addServlet(new ServletHolder(new NotesPageServlet()), "/notes");
        
        // Регистрация API сервлетов
        context.addServlet(new ServletHolder(new NotesServlet()), "/api/notes/*");
        context.addServlet(new ServletHolder(new NoteFolderServlet()), "/api/folders/*");
        
        // Настройка обработки статических ресурсов
        ServletHolder staticHolder = new ServletHolder("static", DefaultServlet.class);
        staticHolder.setInitParameter("resourceBase", System.getProperty("user.dir") + "/src/main/resources/static");
        staticHolder.setInitParameter("dirAllowed", "false");
        staticHolder.setInitParameter("pathInfoOnly", "true");
        context.addServlet(staticHolder, "/static/*");
        
        // Настройка обработки CSS файлов
        ServletHolder cssHolder = new ServletHolder("css", DefaultServlet.class);
        cssHolder.setInitParameter("resourceBase", System.getProperty("user.dir") + "/src/main/resources/static/css");
        cssHolder.setInitParameter("dirAllowed", "false");
        cssHolder.setInitParameter("pathInfoOnly", "true");
        context.addServlet(cssHolder, "/css/*");
        
        // Настройка обработки JS файлов
        ServletHolder jsHolder = new ServletHolder("js", DefaultServlet.class);
        jsHolder.setInitParameter("resourceBase", System.getProperty("user.dir") + "/src/main/resources/static/js");
        jsHolder.setInitParameter("dirAllowed", "false");
        jsHolder.setInitParameter("pathInfoOnly", "true");
        context.addServlet(jsHolder, "/js/*");
        
        // Настройка обработки статических HTML файлов
        ServletHolder pagesHolder = new ServletHolder("pages", DefaultServlet.class);
        pagesHolder.setInitParameter("resourceBase", System.getProperty("user.dir") + "/src/main/resources/templates/pages");
        pagesHolder.setInitParameter("dirAllowed", "false");
        pagesHolder.setInitParameter("pathInfoOnly", "true");
        pagesHolder.setInitParameter("excludePatterns", "tasks.html");
        context.addServlet(pagesHolder, "/pages/*");
        
        server.setHandler(context);
        
        server.start();
        server.join();
    }
} 