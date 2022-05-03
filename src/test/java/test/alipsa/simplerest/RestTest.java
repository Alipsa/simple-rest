package test.alipsa.simplerest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.alipsa.simplerest.RestClient;
import se.alipsa.simplerest.RestException;
import test.alipsa.simplerest.model.Company;

public class RestTest {

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
    //ServletHolder defaultServ = new ServletHolder("default", DefaultServlet.class);
    //defaultServ.setInitParameter("resourceBase",System.getProperty("user.dir"));
    //defaultServ.setInitParameter("dirAllowed","true");
    context.addServlet(SimpleServlet.class,"/simple/*");
    context.addServlet(BasicAuthServlet.class, "/basic/*");
    context.addServlet(JwtServlet.class, "/jwt/*");
    server.setHandler(context);

    // Start Server
    server.start();

    // Determine Base URI for Server
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
      e.printStackTrace();
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
  public void simpleDeleteTest() {

  }

  @Test
  public void simpleHeadTest() {

  }

  @Test
  public void simpleOptionsTest() {

  }
}
