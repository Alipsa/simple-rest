package se.alipsa.simplerest;

import static se.alipsa.simplerest.CommonHeaders.ACCEPT;
import static se.alipsa.simplerest.CommonHeaders.CONTENT_TYPE;
import static se.alipsa.simplerest.RequestMethod.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import nl.altindag.ssl.SSLFactory;

/**
 * The RestClient is the core of the simple-rest api.
 * It implements all the verbs needed for REST communication.
 * Methods are overloaded, so you can easily pick the one suited for your need.
 */
public class RestClient {

  private final ObjectMapper mapper;
  SSLSocketFactory sslSocketFactory;

  private SSLSocketFactory getTrustAllSSLSocketFactory()
      throws NoSuchAlgorithmException, KeyManagementException {
    // Create a trust manager that does not validate certificate chains
    TrustManager[] trustAllCertManagers = new TrustManager[]{
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }
          public void checkClientTrusted(
              java.security.cert.X509Certificate[] certs, String authType) {
          }
          public void checkServerTrusted(
              java.security.cert.X509Certificate[] certs, String authType) {
          }
        }
    };
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCertManagers, new java.security.SecureRandom());
    return sc.getSocketFactory();
  }

  /**
   * Default ctor, creates an object mapper with the JavaTimeModule enabled
   *
   * @param trustAllCertManagers if true, any ssl connection can be made, if false
   *                             only trues keystore and os installed certs.
   *                             Default is false.
   * @throws RestException if the SSL Socket factory cannot be created
   */
  public RestClient(boolean... trustAllCertManagers) throws RestException {
    this(new ObjectMapper().registerModule(new JavaTimeModule()), trustAllCertManagers);

  }

  /** Creates a rest client with the object mapper specified
   *
   * @param mapper the ObjectMapper to use
   * @param trustAllCertManagers if true, any ssl connection can be made, if false
   *    *                        only trues keystore and os installed certs.
   *                             Default is false.
   * @throws RestException if the SSL Socket factory cannot be created
   */
  public RestClient(ObjectMapper mapper, boolean... trustAllCertManagers)
      throws RestException {
    this.mapper = mapper;
    try {
      if (trustAllCertManagers.length > 0 && trustAllCertManagers[0]) {
        sslSocketFactory = getTrustAllSSLSocketFactory();
      } else {
        sslSocketFactory = SSLFactory.builder().withDefaultTrustMaterial() // JDK trusted CA's
            .withSystemTrustMaterial()  // OS trusted CA's
            .build().getSslSocketFactory();
      }
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new RestException("Failed to set up SSL socket factory", e);
    }
  }

  /**
   * Although not RESTful, streaming raw images is commonly encountered in REST applications in the wild.
   * This method checks if the url looks like it is serving an image
   *
   * @param urlString the url to verify
   * @return true if the url exists and if the content type claims it to be an image otherwise false
   */
  public boolean urlExistsAndIsImage(String urlString) {
    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = openConnection(url);
      conn.setRequestMethod(GET);
      conn.connect();
      String contentType = conn.getContentType();
      boolean result = conn.getResponseCode() == 200 && contentType != null && contentType.startsWith("image");
      conn.disconnect();
      return result;
    } catch (IOException | RestException e) {
      return false;
    }
  }

  /**
   * Encode the binary content that the url is pointing to into a Base64 string.
   *
   * @param urlString the resource
   * @return a base 64 encoded string
   * @throws RestException if something goes wrong
   */
  public String getContentAsBase64(String urlString) throws RestException {
    return Base64.getEncoder().encodeToString(getContentAsBytes(urlString));
  }

  /**
   * Fetch the binary content that the url is pointing to into a byte[].
   *
   * @param urlString the resource
   * @return a byte[] of the content
   * @throws RestException if something goes wrong
   */
  public byte[] getContentAsBytes(String urlString) throws RestException {
    HttpURLConnection conn = null;
    try {
      URL url = new URL(urlString);
      conn = openConnection(url);
      conn.setRequestMethod(GET);
      conn.connect();
      int responseCode = conn.getResponseCode();
      if (responseCode != 200) {
        String errorBody = readErrorBody(conn);
        if (!errorBody.isEmpty()) {
          throw new RestException("GET call to " + urlString + " failed: HTTP error code = "
              + responseCode + ", body: " + errorBody.trim());
        }
        throw new RestException("GET call to " + urlString + " failed: HTTP error code = " + responseCode);
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try (InputStream is = conn.getInputStream()) {
        byte[] byteChunk = new byte[4096];
        int n;
        while ((n = is.read(byteChunk)) > 0) {
          baos.write(byteChunk, 0, n);
        }
      }
      return baos.toByteArray();
    }
    catch (IOException e) {
      throw new RestException("Failed to get content as bytes from " + urlString, e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  /**
   * Executes a HTTP GET request.
   *
   * @param urlString the url for the target resource
   * @param acceptType optional parameter if you want to set something other than application/json (you should not have to)
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response get(String urlString, String... acceptType) throws RestException {
    return get(urlString, null, acceptType);
  }

  /**
   * Executes a HTTP GET request.
   *
   * @param urlString the url for the target resource
   * @param headers a Map of the headers to add to the request
   * @param acceptType optional parameter if you want to set something other than application/json (you should not have to)
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response get(String urlString, Map<String, String> headers, String... acceptType) throws RestException {
    return get(urlString, null, headers, acceptType);
  }

  /**
   * Executes a HTTP GET request.
   *
   * @param urlString the url for the target resource
   * @param payload the content Java object to send to the server as json (json conversion is done for you).
   *                Note: it is generally a BAD idea to send a payload with a get request. The Http 1.1 spec says:
   *                "A payload within a GET request message has no defined semantics;
   *                sending a payload body on a GET request might cause some existing implementations to reject the request."
   * @param headers a Map of the headers to add to the request
   * @param acceptType optional parameter if you want to set something other than application/json (you should not have to)
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response get(String urlString, Object payload, Map<String, String> headers, String... acceptType) throws RestException {
    String accept = acceptType.length > 0 ? acceptType[0] : MediaType.APPLICATION_JSON.getValue();
    HttpURLConnection conn = null;
    try {
      URL url = new URL(urlString);
      conn = openConnection(url);
      conn.setRequestMethod(GET);
      conn.setRequestProperty(ACCEPT, accept);
      if (headers != null) {
        headers.forEach(conn::setRequestProperty);
      }
      if (payload != null) {
        if (headers == null || !headers.containsKey(CONTENT_TYPE)) {
          conn.setRequestProperty(CONTENT_TYPE, MediaType.APPLICATION_JSON.getValue());
        }
        conn.setDoOutput(true);
        String input;
        if (payload instanceof CharSequence) {
          input = String.valueOf(payload);
        } else {
          input = mapper.writeValueAsString(payload);
        }
        conn.connect();
        try (OutputStream os = conn.getOutputStream()) {
          os.write(input.getBytes(StandardCharsets.UTF_8));
          os.flush();
        }
      } else {
        conn.connect();
      }
      int responseCode = conn.getResponseCode();
      var responseHeaders = conn.getHeaderFields();
      if (responseCode >= 400) {
        String errorBody = readErrorBody(conn);
        if (!errorBody.isEmpty()) {
          throw new RestException("Failed to call GET on " + urlString + ": HTTP error code = "
              + responseCode + ", body: " + errorBody.trim());
        }
        throw new RestException("Failed to call GET on " + urlString + ": HTTP error code = " + responseCode);
      }
      String body = readAnyBody(conn);
      return new Response(body, responseCode, responseHeaders, mapper);
    } catch (IOException e) {
      throw new RestException("Failed to call GET on " + urlString, e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  /**
   * Executes a HTTP POST request.
   *
   * @param urlString the url for the target resource
   * @param payload the content Java object to send to the server as json (json conversion is done for you)
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response post(String urlString, Object payload) throws RestException {
    return post(urlString, payload, null);
  }

  /**
   * Executes a HTTP POST request.
   *
   * @param urlString the url for the target resource
   * @param payload the content Java object to send to the server as json (json conversion is done for you)
   * @param requestHeaders  Map of the headers to add to the request
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response post(String urlString, Object payload, Map<String, String> requestHeaders) throws RestException {
    return putPost(urlString, payload, requestHeaders, POST);
  }


  /**
   * Executes a HTTP PUT request.
   *
   * @param urlString the url for the target resource
   * @param payload the content Java object to send to the server as json (json conversion is done for you)
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response put(String urlString, Object payload) throws RestException {
    return put(urlString, payload, null);
  }

  /**
   * Executes a HTTP PUT request.
   *
   * @param urlString the url for the target resource
   * @param payload the content Java object to send to the server as json (json conversion is done for you)
   * @param requestHeaders  Map of the headers to add to the request
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response put(String urlString, Object payload, Map<String, String> requestHeaders) throws RestException {
    return putPost(urlString, payload, requestHeaders, PUT);
  }

  /**
   * Executes a HTTP DELETE request.
   *
   * @param urlString the url for the target resource
   * @return a Response containing headers and status code and, possibly, the body content
   * @throws RestException if something goes wrong
   */
  public Response delete(String urlString) throws RestException {
    return delete(urlString, null);
  }

  /**
   * Executes a HTTP DELETE request.
   *
   * @param urlString the url for the target resource
   * @param requestHeaders  Map of the headers to add to the request
   * @return a Response containing headers and status code and, possibly, the body content
   * @throws RestException if something goes wrong
   */
  public Response delete(String urlString, Map<String, String> requestHeaders) throws RestException {
    HttpURLConnection conn = null;
    try {
      URL url = new URL(urlString);
      conn = openConnection(url);
      conn.setDoOutput(false);
      conn.setRequestMethod(DELETE);
      if (requestHeaders == null || !requestHeaders.containsKey(CONTENT_TYPE)) {
        conn.setRequestProperty(CONTENT_TYPE, MediaType.APPLICATION_JSON.getValue());
      }
      if (requestHeaders != null) {
        requestHeaders.forEach(conn::setRequestProperty);
      }
      conn.connect();
      int responseCode = conn.getResponseCode();
      var headers = conn.getHeaderFields();

      try {
        String body = readAnyBody(conn);
        return new Response(body, responseCode, headers, mapper);
      } catch (IOException e) {
        return new Response("", responseCode, headers, mapper);
      }
    } catch (IOException e) {
      throw new RestException("Failed to call DELETE on " + urlString, e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  /**
   * Executes a HTTP HEAD request.
   *
   * @param urlString the url for the target resource
   * @return a Response containing headers and status code, no body content (there SHOULD not be any)
   * @throws RestException if something goes wrong
   */
  public Response head(String urlString) throws RestException {
    return head(urlString, null);
  }

  /**
   * Executes a HTTP HEAD request.
   *
   * @param urlString the url for the target resource
   * @param requestHeaders  Map of the headers to add to the request
   * @return a Response containing headers and status code, no body content (there SHOULD not be any)
   * @throws RestException if something goes wrong
   */
  public Response head(String urlString, Map<String, String> requestHeaders) throws RestException {
    return headersRequest(urlString, requestHeaders, HEAD);
  }

  /**
   * Executes a HTTP OPTIONS request.
   *
   * @param urlString the url for the target resource
   * @return a Response containing headers and status code, no body content (there SHOULD not be any)
   * @throws RestException @throws RestException if something goes wrong
   */
  public Response options(String urlString) throws RestException {
    return options(urlString, null);
  }

  /**
   * Executes a HTTP OPTIONS request.
   *
   * @param urlString the url for the target resource
   * @param requestHeaders  Map of the headers to add to the request
   * @return a Response containing headers and status code, no body content (there SHOULD not be any)
   * @throws RestException @throws RestException if something goes wrong
   */
  public Response options(String urlString, Map<String, String> requestHeaders) throws RestException {
    return headersRequest(urlString, requestHeaders, OPTIONS);
  }

  private Response headersRequest(String urlString, Map<String, String> requestHeaders, String method) throws RestException {
    String accept = MediaType.APPLICATION_JSON.getValue();

    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = openConnection(url);
      conn.setRequestMethod(method);
      conn.setRequestProperty("Accept", accept);
      if (requestHeaders != null) {
        requestHeaders.forEach(conn::setRequestProperty);
      }
      conn.connect();
      int responseCode = conn.getResponseCode();
      var responseHeaders = conn.getHeaderFields();
      conn.disconnect();
      return new Response("", responseCode, responseHeaders, mapper);
    } catch (IOException e) {
      throw new RestException("Failed to call " + method + " on " + urlString, e);
    }
  }

  private Response putPost(String urlString, Object payload, Map<String, String> requestHeaders, String method) throws RestException {
    HttpURLConnection conn = null;
    try {
      URL url = new URL(urlString);
      conn = openConnection(url);
      conn.setRequestMethod(method);
      if (requestHeaders == null || !requestHeaders.containsKey(CONTENT_TYPE)) {
        conn.setRequestProperty(CONTENT_TYPE, MediaType.APPLICATION_JSON.getValue());
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
        try (OutputStream os = conn.getOutputStream()) {
          os.write(input.getBytes(StandardCharsets.UTF_8));
          os.flush();
        }
      } else {
        conn.connect();
      }

      int responseCode = conn.getResponseCode();
      var headers = conn.getHeaderFields();
      String body = "";
      try {
        body = readAnyBody(conn);
      } catch (IOException e) {
        // no content
      }
      return new Response(body, responseCode, headers, mapper);

    } catch (IOException e) {
      throw new RestException("Failed to call " + method + " on " + urlString, e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  HttpURLConnection openConnection(URL url)
      throws RestException {
    try {
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      if (conn instanceof HttpsURLConnection) {
        ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
      }
      return conn;
    } catch (IOException e) {
      throw new RestException("Failed to open connection to " + url, e);
    }
  }

  private String readAnyBody(HttpURLConnection conn) throws IOException {
    try {
      return readBody(conn.getInputStream());
    } catch (IOException e) {
      InputStream errorStream = conn.getErrorStream();
      if (errorStream == null) {
        throw e;
      }
      return readBody(errorStream);
    }
  }

  private String readErrorBody(HttpURLConnection conn) throws IOException {
    InputStream errorStream = conn.getErrorStream();
    return readBody(errorStream);
  }

  private String readBody(InputStream inputStream) throws IOException {
    if (inputStream == null) {
      return "";
    }
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      StringBuilder writer = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        writer.append(line).append('\n');
      }
      return writer.toString();
    }
  }
}
