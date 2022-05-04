package test.alipsa.simplerest.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import se.alipsa.simplerest.MediaType;
import test.alipsa.simplerest.model.Company;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SimpleServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("In SimpleServlet.doGet for " + req.getRequestURI());
    resp.setStatus(200);
    resp.setContentType(MediaType.APPLICATION_JSON.getValue());
    var writer = resp.getWriter();
    ObjectMapper mapper = new ObjectMapper();
    Company company = new Company("ABC", 123);
    String value = mapper.writeValueAsString(company);
    resp.setContentLength(value.getBytes(StandardCharsets.UTF_8).length);
    writer.print(value);
    writer.close();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("In SimpleServlet.doPost for " + req.getRequestURI());
    resp.setStatus(200);
    resp.setContentType(MediaType.APPLICATION_JSON.getValue());
    ObjectMapper mapper = new ObjectMapper();
    var company = mapper.readValue(req.getInputStream(), Company.class);
    company.setNumber(191919);
    var writer = resp.getWriter();
    writer.print(mapper.writeValueAsString(company));
    writer.close();
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("In SimpleServlet.doPut for " + req.getRequestURI());
    resp.setStatus(200);
    resp.setContentType(MediaType.APPLICATION_JSON.getValue());
    ObjectMapper mapper = new ObjectMapper();
    var company = mapper.readValue(req.getInputStream(), Company.class);
    var writer = resp.getWriter();
    writer.print(mapper.writeValueAsString(company));
    writer.close();
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("In SimpleServlet.doDelete for " + req.getRequestURI());
    if (req.getRequestURI().endsWith("/company/123")) {
      resp.setStatus(204);
    } else {
      resp.setStatus(404);
    }
    resp.setContentLength(0);
  }

  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("In SimpleServlet.doHead for " + req.getRequestURI());
    resp.setStatus(200);
    resp.setContentType(MediaType.APPLICATION_JSON.getValue());
    ObjectMapper mapper = new ObjectMapper();
    Company company = new Company("ABC", 123);
    String value = mapper.writeValueAsString(company);
    resp.setContentLength(value.getBytes(StandardCharsets.UTF_8).length);
  }

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("In SimpleServlet.doOptions for " + req.getRequestURI());
    resp.setContentType(MediaType.APPLICATION_JSON.getValue());
    super.doOptions(req, resp);
  }
}
