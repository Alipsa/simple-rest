package test.alipsa.simplerest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.alipsa.simplerest.Response;
import se.alipsa.simplerest.RestClient;
import se.alipsa.simplerest.RestException;
import test.alipsa.simplerest.model.Company;
import test.alipsa.simplerest.servlets.SimpleServlet;

import static org.junit.jupiter.api.Assertions.*;
import static se.alipsa.simplerest.CommonHeaders.*;

import java.util.Map;

public class BasicAuthRestTest {

  private static Server server;
  private static String serverUrl;
  private static RestClient restClient;

  private static final String userName = "per";
  private static final String password = "secret";


  @BeforeAll
  public static void startJetty() throws Exception {
    //System.out.println("Starting BasicAuth jetty server");
    server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(0); // auto-bind to available port
    server.addConnector(connector);

    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.addServlet(SimpleServlet.class, "/basic/*");
    server.setHandler(context);
    context.setSecurityHandler(basicAuthConfig());
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
  public void basicGetTest() throws RestException, JsonProcessingException {
    assertThrows(RestException.class, () -> restClient.get(serverUrl + "basic"));
    var response = restClient.get(serverUrl + "basic",
        Map.of(AUTHORIZATION, basicAuth(userName, password))
    );
    assertEquals(200, response.getResponseCode(), "get /basic, response Code");
    Company company = response.getObject(Company.class);
    assertEquals("ABC", company.getName(), "Company name");
    assertEquals(123, company.getNumber(), "company number");
  }

  @Test
  public void basicPostTest() throws RestException, JsonProcessingException {
    Company company = new Company();
    company.setName("Creative Design");
    var response = restClient.post(
        serverUrl + "basic",
        company,
        Map.of(AUTHORIZATION, basicAuth(userName, password))
    );
    Company c = response.getObject(Company.class);
    assertEquals("Creative Design", c.getName(), "Company name");
    assertEquals(191919, c.getNumber(), "company number");
  }

  @Test
  public void basicPutTest() throws JsonProcessingException, RestException {
    Company company = new Company("Creative Design", 191919);
    var response = restClient.put(
        serverUrl + "basic",
        company,
        Map.of(AUTHORIZATION, basicAuth(userName, password))
    );
    Company c = response.getObject(Company.class);
    assertEquals("Creative Design", c.getName(), "Company name");
    assertEquals(191919, c.getNumber(), "company number");
  }

  @Test
  public void basicDeleteTest() throws RestException {
    var response = restClient.delete(
        serverUrl + "basic/company/123",
        Map.of(AUTHORIZATION, basicAuth(userName, password))
    );
    assertEquals(204, response.getResponseCode(), "delete /basic/company/123, response Code");
    response = restClient.delete(
        serverUrl + "basic/company/345",
        Map.of(AUTHORIZATION, basicAuth(userName, password))
    );
    assertEquals(404, response.getResponseCode(), "delete /basic/company/345, response Code");
  }

  @Test
  public void basicHeadTest() throws RestException {
    Response response = restClient.head(
        serverUrl + "basic",
        Map.of(AUTHORIZATION, basicAuth(userName, password))
    );
    assertEquals("27", response.getHeader(CONTENT_LENGTH), "Content-Length");
    assertEquals("application/json", response.getHeader(CONTENT_TYPE));
  }

  @Test
  public void basicOptionsTest() throws RestException {
    Response response = restClient.options(
        serverUrl + "basic",
        Map.of(AUTHORIZATION, basicAuth(userName, password))
    );
    //System.out.println(response.getHeaders());
    assertEquals("GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS", response.getHeader(ALLOW));
  }

  private static SecurityHandler basicAuthConfig() {

    HashLoginService loginService = new HashLoginService();
    UserStore userStore = new UserStore();
    userStore.addUser(userName, new Password(password), new String[] {"users"});
    loginService.setUserStore(userStore);

    Constraint constraint = new Constraint();
    constraint.setName(Constraint.__BASIC_AUTH);
    constraint.setRoles(new String[]{"users"});
    constraint.setAuthenticate(true);

    ConstraintMapping cm = new ConstraintMapping();
    cm.setConstraint(constraint);
    cm.setPathSpec("/*");

    ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
    csh.setAuthenticator(new BasicAuthenticator());
    csh.setRealmName("myrealm");
    csh.addConstraintMapping(cm);
    csh.setLoginService(loginService);

    return csh;

  }
}
