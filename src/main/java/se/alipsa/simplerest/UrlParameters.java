package se.alipsa.simplerest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlParameters {

  public static String parameters(String... params) {
    if (params.length == 0) {
      return "";
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
