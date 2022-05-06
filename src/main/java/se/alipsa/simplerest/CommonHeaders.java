package se.alipsa.simplerest;

import java.util.Base64;

public class CommonHeaders {

  public static final String CONTENT_TYPE = "Content-Type";
  public static final String CONTENT_LENGTH = "Content-Length";
  public static final String AUTHORIZATION = "Authorization";
  public static final String ALLOW = "Allow";

  public static String basicAuth(String username, String password) {
    return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
  }

  public static String bearer(String token) {
    return "Bearer " + token;
  }
}
