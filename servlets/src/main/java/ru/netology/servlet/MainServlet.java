package ru.netology.servlet;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.netology.controller.PostController;
import ru.netology.repository.PostRepository;
import ru.netology.service.PostService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MainServlet extends HttpServlet {
     private PostController controller;
     private final String GET = "GET";
     private final String POST = "POST";
     private final String DELETE = "DELETE";


  @Override
  public void init() {
    final var context = new AnnotationConfigApplicationContext("ru.netology");
    controller = (PostController) context.getBean("postController");
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) {
    // если деплоились в root context, то достаточно этого
    try {
      final var path = req.getRequestURI();

      final var method = req.getMethod();
      // primitive routing
      if(path.equals("/api/posts")) {
        switch (method) {
          case GET -> {
            controller.all(resp);
            return;
          }
          case POST -> {
            controller.save(req.getReader(), resp);
            return;
          }
        }
      } else if (path.matches("/api/posts/\\d+")) {
        long l = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
        switch (method) {
          case GET -> {
            controller.getById(l, resp);
            return;
          }
          case DELETE -> {
            controller.removeById(l, resp);
            return;
          }
        }
      }

      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } catch (Exception e) {
      e.printStackTrace();
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}

