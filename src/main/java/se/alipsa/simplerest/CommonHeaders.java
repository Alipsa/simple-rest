package se.alipsa.simplerest;

import java.util.Base64;
import java.util.Map;

/**
 * These are some headers commonly used and some simple methods to make handling headers simpler
 */
public class CommonHeaders {

  public static final String ALLOW = "Allow";
  public static final String ACCEPT = "Accept";
  public static final String AUTHORIZATION = "Authorization";
  public static final String CONTENT_LENGTH = "Content-Length";
  public static final String CONTENT_TYPE = "Content-Type";

  /**
   * Creates a value used for the authorization header including the base 64 encoded string containing the username and password
   * @param username the username for the resource requiring basic authentication
   * @param password the password for the resource requiring basic authentication
   * @return a value used for the authorization header including the base 64 encoded string containing the username and password
   */
  public static String basicAuth(String username, String password) {
    return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
  }

  /**
   * Creates an authorization header including the base 64 encoded string containing the username and password
   * @param username the username for the resource requiring basic authentication
   * @param password the password for the resource requiring basic authentication
   * @return a Map of the authorization header including the base 64 encoded string containing the username and password
   */
  public static Map<String, String> basicAuthHeader(String username, String password) {
    return Map.of(AUTHORIZATION, basicAuth(username, password));
  }

  /**
   * Create a value to use for the authorization header, .e.g when using jason web tokens
   * @param token the token to use
   * @return the full value (Bearer + token) to use for the authorization header value
   */
  public static String bearer(String token) {
    return "Bearer " + token;
  }

  /**
   * Create an authorization header .e.g when using jason web tokens
   * @param token the token to use
   * @return a Map of the Authorization key and the full value (Bearer + token)
   * to use for the authorization header
   */
  public static Map<String, String> bearerHeader(String token) {
    return Map.of(AUTHORIZATION, bearer(token));
  }
}
