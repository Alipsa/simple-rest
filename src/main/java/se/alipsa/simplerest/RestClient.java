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
      conn.connect();
      String contentType = conn.getContentType();
      boolean result = conn.getResponseCode() == 200 && contentType != null && contentType.startsWith("image");
      conn.disconnect();
      return result;
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
      conn.connect();
      if (conn.getResponseCode() != 200) {
        throw new RestException("GET call to " + urlString + " failed: HTTP error code = " + conn.getResponseCode());
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      InputStream is = url.openStream ();
      byte[] byteChunk = new byte[4096];
      int n;

      while ( (n = is.read(byteChunk)) > 0 ) {
        baos.write(byteChunk, 0, n);
      }
      is.close();
      conn.disconnect();
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
      conn.connect();
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
      throw new RestException("Failed to call GET on " + urlString, e);
    }
  }

  public Response post(String urlString, Object payload) throws RestException {
    return post(urlString, payload, null);
  }

  public Response post(String urlString, Object payload, Map<String, String> requestHeaders) throws RestException {
    return putPost(urlString, payload, requestHeaders, "POST");
  }


  public Response put(String urlString, Object payload) throws RestException {
    return put(urlString, payload, null);
  }

  public Response put(String urlString, Object payload, Map<String, String> requestHeaders) throws RestException {
    return putPost(urlString, payload, requestHeaders, "PUT");
  }

  private Response putPost(String urlString, Object payload, Map<String, String> requestHeaders, String method) throws RestException {
    StringBuilder writer = new StringBuilder();
    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(method);
      if (requestHeaders != null && !requestHeaders.containsKey("Content-Type")) {
        conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON.getValue());
      }
      if (requestHeaders != null) {
        requestHeaders.forEach(conn::setRequestProperty);
      }

      if (payload != null) {
        conn.setDoOutput(true);
        String input;
        if (payload instanceof CharSequence) {
          input = String.valueOf(payload);
        } else {
          input = mapper.writeValueAsString(payload);
        }
        conn.connect();
        OutputStream os = conn.getOutputStream();
        os.write(input.getBytes());
        os.flush();
        os.close();
      } else {
        conn.connect();
      }

      int responseCode = conn.getResponseCode();
      var headers = conn.getHeaderFields();

      if (conn.getContentLength() > 0) {
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
          writer.append(line).append('\n');
        }
      }
      conn.disconnect();
      return new Response(writer.toString(), responseCode, headers);

    } catch (IOException e) {
      throw new RestException("Failed to call " + method + " on " + urlString, e);
    }
  }

  public Response delete(String urlString) throws RestException {
    StringBuilder writer = new StringBuilder();
    try {
    URL url = new URL(urlString);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(false);
    conn.setRequestMethod("DELETE");
    conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON.getValue());
    conn.connect();
    int responseCode = conn.getResponseCode();
    var headers = conn.getHeaderFields();

    if (conn.getContentLength() > 0) {
      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        writer.append(line).append('\n');
      }
    }
    conn.disconnect();
    return new Response(writer.toString(), responseCode, headers);
    } catch (IOException e) {
      throw new RestException("Failed to call DELETE on " + urlString, e);
    }
  }

  public Response head(String urlString) throws RestException {
    return head(urlString, null);
  }

  public Response head(String urlString, Map<String, String> headers) throws RestException {
    return headersRequest(urlString, headers, "HEAD");
  }

  public Response options(String urlString) throws RestException {
    return options(urlString, null);
  }

  public Response options(String urlString, Map<String, String> headers) throws RestException {
    return headersRequest(urlString, headers, "OPTIONS");
  }

  public Response headersRequest(String urlString, Map<String, String> headers, String method) throws RestException {
    String accept = MediaType.APPLICATION_JSON.getValue();

    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(method);
      conn.setRequestProperty("Accept", accept);
      if (headers != null) {
        headers.forEach(conn::setRequestProperty);
      }
      conn.connect();
      int responseCode = conn.getResponseCode();
      var responseHeaders = conn.getHeaderFields();
      conn.disconnect();
      return new Response("", responseCode, responseHeaders);
    } catch (IOException e) {
      throw new RestException("Failed to call " + method + " on " + urlString, e);
    }
  }
}
