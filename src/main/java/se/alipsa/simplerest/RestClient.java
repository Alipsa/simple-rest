package se.alipsa.simplerest;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

public class RestClient {

  private final ObjectMapper mapper;

  public RestClient() {
    mapper = new ObjectMapper();
  }

  public RestClient(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public boolean urlExistsAndIsImage(String urlString) {
    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      String contentType = conn.getContentType();
      return conn.getResponseCode() == 200 && contentType != null && contentType.startsWith("image");
    } catch (IOException e) {
      return false;
    }
  }

  public static String getContentAsBase64(String urlString) throws RestException {
    return Base64.getEncoder().encodeToString(getContentAsBytes(urlString));
  }

  public static byte[] getContentAsBytes(String urlString) throws RestException {
    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");

      if (conn.getResponseCode() != 200) {
        throw new RestException("Misslyckades att anropa: HTTP felkod = " + conn.getResponseCode());
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      InputStream is = url.openStream ();
      byte[] byteChunk = new byte[4096];
      int n;

      while ( (n = is.read(byteChunk)) > 0 ) {
        baos.write(byteChunk, 0, n);
      }
      is.close();
      return baos.toByteArray();
    }
    catch (IOException e) {
      throw new RestException("Failed to get content as bytes from " + urlString, e);
    }
  }

  public Response get(String urlString, String... acceptType) throws RestException {
    return get(urlString, null, acceptType);
  }

  public Response get(String urlString, Map<String, String> headers, String... acceptType) throws RestException {
    String accept = acceptType.length > 0 ? acceptType[0] : MediaType.APPLICATION_JSON.getValue();
    StringBuilder writer = new StringBuilder();
    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", accept);
      if (headers != null) {
        headers.forEach(conn::setRequestProperty);
      }
      int responseCode = conn.getResponseCode();
      var responseHeaders = conn.getHeaderFields();
      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        writer.append(line).append('\n');
      }
      conn.disconnect();
      return new Response(writer.toString(), responseCode, responseHeaders);
    } catch (IOException e) {
      throw new RestException("Failed to call " + urlString, e);
    }
  }

  public Response post(String urlString, Object payload) throws RestException {

    StringBuilder writer = new StringBuilder();
    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON.getValue());

      String input;
      if (payload instanceof CharSequence) {
        input = String.valueOf(payload);
      } else {
        input = mapper.writeValueAsString(payload);
      }
      OutputStream os = conn.getOutputStream();
      os.write(input.getBytes());
      os.flush();

      int responseCode = conn.getResponseCode();
      var headers = conn.getHeaderFields();

      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

      String line;
      while ((line = br.readLine()) != null) {
        writer.append(line).append('\n');
      }

      conn.disconnect();
      return new Response(writer.toString(), responseCode, headers);

    } catch (IOException e) {
      throw new RestException("Failed to call " + urlString, e);
    }
  }
}
