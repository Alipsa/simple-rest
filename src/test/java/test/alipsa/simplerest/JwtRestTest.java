package test.alipsa.simplerest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.alipsa.simplerest.Response;
import se.alipsa.simplerest.RestClient;
import se.alipsa.simplerest.RestException;
import test.alipsa.simplerest.model.Company;
import test.alipsa.simplerest.servlets.JwtServlet;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static se.alipsa.simplerest.CommonHeaders.*;
import static se.alipsa.simplerest.CommonHeaders.basicAuth;

public class JwtRestTest {

  private static Server server;
  private static String serverUrl;
  private static RestClient restClient;
  private static String userName = "per";
  private static String password = "secret";

  private static String jwtToken = null;

  @BeforeAll
  public static void startJetty() throws Exception {
    System.out.println("Starting jwt jetty server");
    server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(0); // auto-bind to available port
    server.addConnector(connector);

    ServletContextHandler context = new ServletContextHandler();
    context.addServlet(JwtServlet.class, "/jwt/*");
    server.setHandler(context);
    server.start();

    String host = connector.getHost();
    if (host == null) {
      host = "localhost";
    }
    int port = connector.getLocalPort();
    serverUrl = String.format("http://%s:%d/",host,port);
    restClient = new RestClient();
  }

  @AfterAll
  public static void stopJetty() {
    try {
      server.stop();
    }
    catch (Exception e) {
      fail(e);
    }
  }

  String getToken() throws RestException {
    if (jwtToken == null) {
      jwtToken = login();
    }
    return jwtToken;
  }

  String login() throws RestException {
    var loginInfo = new HashMap<String, String>();
    loginInfo.put("username", userName);
    loginInfo.put("password", password);
    var restClient = new RestClient();
    Response response = restClient.post(serverUrl + "jwt/login", loginInfo);
    System.out.println("Login: response code: " + response.getResponseCode());
    System.out.println("Got headers: " + response.getHeaders());
    if (response.getResponseCode() == 200) {
      return "Bearer " + response.getPayload().trim();
    }
    return null;
  }

  @Test
  public void jwtGetTest() throws RestException, JsonProcessingException {
    var response = restClient.get(serverUrl + "jwt",
        Map.of(AUTHORIZATION, getToken())
    );
    assertEquals(200, response.getResponseCode(), "get /jwt, response Code");
    Company company = response.getObject(Company.class);
    assertEquals("ABC", company.getName(), "Company name");
    assertEquals(123, company.getNumber(), "company number");
  }

  @Test
  public void jwtPostTest() throws RestException, JsonProcessingException {
    Company company = new Company();
    company.setName("Creative Design");
    var response = restClient.post(
        serverUrl + "jwt",
        company,
        Map.of(AUTHORIZATION, getToken())
    );
    Company c = response.getObject(Company.class);
    assertEquals("Creative Design", c.getName(), "Company name");
    assertEquals(191919, c.getNumber(), "company number");
  }

  @Test
  public void jwtPutTest() throws JsonProcessingException, RestException {
    Company company = new Company("Creative Design", 191919);
    var response = restClient.put(
        serverUrl + "jwt",
        company,
        Map.of(AUTHORIZATION, getToken())
    );
    Company c = response.getObject(Company.class);
    assertEquals("Creative Design", c.getName(), "Company name");
    assertEquals(191919, c.getNumber(), "company number");
  }

  @Test
  public void jwtDeleteTest() throws RestException {
    var response = restClient.delete(
        serverUrl + "jwt/company/123",
        Map.of(AUTHORIZATION, getToken())
    );
    assertEquals(204, response.getResponseCode(), "delete /jwt/company/123, response Code");
    response = restClient.delete(
        serverUrl + "jwt/company/345",
        Map.of(AUTHORIZATION, getToken())
    );
    assertEquals(404, response.getResponseCode(), "delete /jwt/company/345, response Code");
  }

  @Test
  public void jwtHeadTest() throws RestException {
    Response response = restClient.head(
        serverUrl + "jwt",
        Map.of(AUTHORIZATION, getToken())
    );
    assertEquals("27", response.getHeader(CONTENT_LENGTH), "Content-Length");
    assertEquals("application/json", response.getHeader(CONTENT_TYPE));
  }

  @Test
  public void jwtOptionsTest() throws RestException {
    Response response = restClient.options(
        serverUrl + "jwt",
        Map.of(AUTHORIZATION, getToken())
    );
    System.out.println(response.getHeaders());
  }
}
