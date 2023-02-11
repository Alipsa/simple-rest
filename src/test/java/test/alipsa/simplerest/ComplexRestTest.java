package test.alipsa.simplerest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.alipsa.simplerest.RestClient;
import se.alipsa.simplerest.RestException;
import test.alipsa.simplerest.servlets.ComplexServlet;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ComplexRestTest {

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
    context.addServlet(ComplexServlet.class,"/complex/*");
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
  public void getComplexReturn() throws RestException, JsonProcessingException {
    var response = restClient.get(serverUrl + "complex");
    assertEquals(200, response.getResponseCode(), "get /complex, response Code");
    Map<String, List<String>> map = response.getForType(new TypeReference<>() {});
    assertEquals(2, map.size());
    assertEquals("bar", map.get("two").get(1));
  }

}
