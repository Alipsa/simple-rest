package test.alipsa.simplerest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.net.URI;

public class RestTest {

  private static Server server;
  private static URI serverUri;

  @BeforeAll
  public static void startJetty() throws Exception {
    // Create Server
    server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(0); // auto-bind to available port
    server.addConnector(connector);

    ServletContextHandler context = new ServletContextHandler();
    //ServletHolder defaultServ = new ServletHolder("default", DefaultServlet.class);
    //defaultServ.setInitParameter("resourceBase",System.getProperty("user.dir"));
    //defaultServ.setInitParameter("dirAllowed","true");
    context.addServlet(SimpleServlet.class,"/simple");
    context.addServlet(BasicAuthServlet.class, "/basic");
    context.addServlet(JwtServlet.class, "/jwt");
    server.setHandler(context);

    // Start Server
    server.start();

    // Determine Base URI for Server
    String host = connector.getHost();
    if (host == null) {
      host = "localhost";
    }
    int port = connector.getLocalPort();
    serverUri = new URI(String.format("http://%s:%d/",host,port));
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
}
