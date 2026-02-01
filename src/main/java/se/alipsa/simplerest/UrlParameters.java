package se.alipsa.simplerest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utility class to easily build url parameters.
 * <code>
 * UrlParameters.parameters("foo", "123", "bar", "898")
 * </code>
 * will result in the parameter string "?foo=123&amp;bar=898"
 *
 */
public class UrlParameters {

  /**
   * Build a parameter string from the parameters supplied.
   *
   * @param params the key value pairs to build a parameter string from
   * @return a parameter string suitable to append to an url. Each parameter is properly url encoded.
   */
  public static String parameters(String... params) {
    if (params.length == 0) {
      return "";
    }
    if (params.length % 2 != 0) {
      throw new IllegalArgumentException("Parameters must be supplied as key/value pairs");
    }
    String up = "?";
    int i = 0;
    for (String param : params) {
      i++;
      if (i % 2 != 0) {
        if (i == 1) {
          up = up + URLEncoder.encode(param, StandardCharsets.UTF_8);
        } else {
          up = up + "&" + URLEncoder.encode(param, StandardCharsets.UTF_8);
        }
      } else {
        up = up + "="+ URLEncoder.encode(param, StandardCharsets.UTF_8);
      }
    }
    return up;
  }
}
