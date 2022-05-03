package test.alipsa.simplerest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import se.alipsa.simplerest.MediaType;
import test.alipsa.simplerest.model.Company;

import java.io.IOException;

public class SimpleServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("In SimpleServlet.doGet");
    resp.setStatus(200);
    resp.setContentType(MediaType.APPLICATION_JSON.getValue());
    var writer = resp.getWriter();
    ObjectMapper mapper = new ObjectMapper();
    Company company = new Company("ABC", 123);
    writer.print(mapper.writeValueAsString(company));
    writer.close();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("In SimpleServlet.doPost");
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
    System.out.println("In SimpleServlet.doPost");
    resp.setStatus(200);
    resp.setContentType(MediaType.APPLICATION_JSON.getValue());
    ObjectMapper mapper = new ObjectMapper();
    var company = mapper.readValue(req.getInputStream(), Company.class);
    var writer = resp.getWriter();
    writer.print(mapper.writeValueAsString(company));
    writer.close();
  }
}
