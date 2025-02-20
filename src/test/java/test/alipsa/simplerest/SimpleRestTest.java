package test.alipsa.simplerest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static se.alipsa.simplerest.CommonHeaders.ALLOW;
import static se.alipsa.simplerest.CommonHeaders.CONTENT_LENGTH;
import static se.alipsa.simplerest.CommonHeaders.CONTENT_TYPE;

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
import test.alipsa.simplerest.servlets.SimpleServlet;

public class SimpleRestTest {

  private static Server server;
  private static String serverUrl;
  private static RestClient restClient;


  @BeforeAll
  public static void startJetty() throws Exception {
    //System.out.println("Starting jetty server");
    server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(0); // auto-bind to available port
    server.addConnector(connector);

    ServletContextHandler context = new ServletContextHandler();
    context.addServlet(SimpleServlet.class, "/simple/*");
    server.setHandler(context);

    server.start();

    String host = connector.getHost();
    if (host == null) {
      host = "localhost";
    }
    int port = connector.getLocalPort();
    serverUrl = String.format("http://%s:%d/", host, port);
    restClient = new RestClient();
  }

  @AfterAll
  public static void stopJetty() {
    try {
      server.stop();
    } catch (Exception e) {
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
  public void getComplexReturn() {

  }

  @Test
  public void simplePostTest() throws JsonProcessingException {
    try {
      Company company = new Company();
      company.setName("Creative Design");
      var response = restClient.post(serverUrl + "simple", company);
      Company c = response.getObject(Company.class);
      assertEquals("Creative Design", c.getName(), "Company name");
      assertEquals(191919, c.getNumber(), "company number");
    } catch (RestException e) {
      fail(e);
    }
  }

  @Test
  public void simplePutTest() throws JsonProcessingException {
    try {
      Company company = new Company("Creative Design", 191919);
      var response = restClient.put(serverUrl + "simple", company);
      Company c = response.getObject(Company.class);
      assertEquals("Creative Design", c.getName(), "Company name");
      assertEquals(191919, c.getNumber(), "company number");
    } catch (RestException e) {
      fail(e);
    }
  }

  @Test
  public void simpleDeleteTest() {
    try {
      var response = restClient.delete(serverUrl + "simple/company/123");
      assertEquals(204, response.getResponseCode(), "delete /simple/company/123, response Code");
      response = restClient.delete(serverUrl + "simple/company/345");
      assertEquals(404, response.getResponseCode(), "delete /simple/company/345, response Code");
    } catch (RestException e) {
      fail(e);
    }
  }

  @Test
  public void simpleHeadTest() {
    try {
      Response response = restClient.head(serverUrl + "simple");
      assertEquals("27", response.getHeader(CONTENT_LENGTH), "Content-Length");
      assertEquals("application/json", response.getHeader(CONTENT_TYPE));
    } catch (RestException e) {
      fail(e);
    }
  }

  @Test
  public void simpleOptionsTest() {
    try {
      Response response = restClient.options(serverUrl + "simple");
      //System.out.println(response.getHeaders());
      assertEquals("GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS", response.getHeader(ALLOW));
    } catch (RestException e) {
      fail(e);
    }
  }

  @Test
  public void testSslExternal() {
    try {
      // Should work both with all trusted and OS trusted CA
      var url = "https://www.sunet.se";
      var rcOs = new RestClient(false);
      var osResponse = rcOs.get(url);

      var rcAll = new RestClient(true);
      var allResponse = rcAll.get(url);
      assertEquals(allResponse, osResponse);
    } catch (RestException e) {
      fail(e);
    }
  }
}
