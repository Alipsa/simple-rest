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
import test.alipsa.simplerest.servlets.BasicAuthServlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class BasicAuthRestTest {

  private static Server server;
  private static String serverUrl;
  private static RestClient restClient;



  @BeforeAll
  public static void startJetty() throws Exception {
    System.out.println("Starting jetty server");
    server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(0); // auto-bind to available port
    server.addConnector(connector);

    ServletContextHandler context = new ServletContextHandler();
    context.addServlet(BasicAuthServlet.class, "/basic/*");
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
  
  @Test
  public void simpleGetTest() throws RestException, JsonProcessingException {
    var response = restClient.get(serverUrl + "simple");
    assertEquals(200, response.getResponseCode(), "get /simple, response Code");
    Company company = response.getObject(Company.class);
    assertEquals("ABC", company.getName(), "Company name");
    assertEquals(123, company.getNumber(), "company number");
  }

  @Test
  public void simplePostTest() throws RestException, JsonProcessingException {
    Company company = new Company();
    company.setName("Creative Design");
    var response = restClient.post(serverUrl + "simple", company);
    Company c = response.getObject(Company.class);
    assertEquals("Creative Design", c.getName(), "Company name");
    assertEquals(191919, c.getNumber(), "company number");
  }

  @Test
  public void simplePutTest() throws JsonProcessingException, RestException {
    Company company = new Company("Creative Design", 191919);
    var response = restClient.put(serverUrl + "simple", company);
    Company c = response.getObject(Company.class);
    assertEquals("Creative Design", c.getName(), "Company name");
    assertEquals(191919, c.getNumber(), "company number");
  }

  @Test
  public void simpleDeleteTest() throws RestException {
    var response = restClient.delete(serverUrl + "simple/company/123");
    assertEquals(204, response.getResponseCode(), "delete /simple/company/123, response Code");
    response = restClient.delete(serverUrl + "simple/company/345");
    assertEquals(404, response.getResponseCode(), "delete /simple/company/345, response Code");
  }

  @Test
  public void simpleHeadTest() throws RestException {
    Response response = restClient.head(serverUrl + "simple");
    assertEquals("27", response.getHeader("Content-Length"), "Content-Length");
    assertEquals("application/json", response.getHeader("Content-Type"));
  }

  @Test
  public void simpleOptionsTest() throws RestException {
    Response response = restClient.options(serverUrl + "simple");
    System.out.println(response.getHeaders());
  }
}
