package se.alipsa.simplerest;

import static se.alipsa.simplerest.CommonHeaders.CONTENT_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

/**
 * The RestClient is the core of the simple-rest api.
 * It implements all the verbs needed for REST communication.
 * Methods are overloaded, so you can easily pick the one suited for your need.
 */
public class RestClient {

  private final ObjectMapper mapper;

  public RestClient() {
    mapper = new ObjectMapper().registerModule(new JavaTimeModule());
  }

  public RestClient(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * Although not RESTful, streaming raw images is commonly encountered in REST applications in the wild.
   * This method checks if the url looks like it is serving an image
   * @param urlString the url to verify
   * @return true if the url exists and if the content type claims it to be an image otherwise false
   */
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

  /**
   * Encode the binary content that the url is pointing to into a Base64 string.
   *
   * @param urlString the resource
   * @return a base 64 encoded string
   * @throws RestException if something goes wrong
   */
  public static String getContentAsBase64(String urlString) throws RestException {
    return Base64.getEncoder().encodeToString(getContentAsBytes(urlString));
  }

  /**
   * Fetch the binary content that the url is pointing to into a byte[].
   *
   * @param urlString the resource
   * @return a byte[] of the content
   * @throws RestException if something goes wrong
   */
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

  /**
   * Executes a HTTP GET request
   * @param urlString the url for the target resource
   * @param acceptType optional parameter if you want to set something other than application/json (you should not have to)
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response get(String urlString, String... acceptType) throws RestException {
    return get(urlString, null, acceptType);
  }

  /**
   * Executes a HTTP GET request
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
   * Executes a HTTP GET request
   * @param urlString the url for the target resource
   * @param payload the content Java object to send to the server as json (json conversion is done for you)
   * @param headers a Map of the headers to add to the request
   * @param acceptType optional parameter if you want to set something other than application/json (you should not have to)
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response get(String urlString, Object payload, Map<String, String> headers, String... acceptType) throws RestException {
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
      var responseHeaders = conn.getHeaderFields();
      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        writer.append(line).append('\n');
      }
      conn.disconnect();
      return new Response(writer.toString(), responseCode, responseHeaders, mapper);
    } catch (IOException e) {
      throw new RestException("Failed to call GET on " + urlString, e);
    }
  }

  /**
   * Executes a HTTP POST request
   * @param urlString the url for the target resource
   * @param payload the content Java object to send to the server as json (json conversion is done for you)
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response post(String urlString, Object payload) throws RestException {
    return post(urlString, payload, null);
  }

  /**
   * Executes a HTTP POST request
   * @param urlString the url for the target resource
   * @param payload the content Java object to send to the server as json (json conversion is done for you)
   * @param requestHeaders  Map of the headers to add to the request
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response post(String urlString, Object payload, Map<String, String> requestHeaders) throws RestException {
    return putPost(urlString, payload, requestHeaders, "POST");
  }


  /**
   * Executes a HTTP PUT request
   * @param urlString the url for the target resource
   * @param payload the content Java object to send to the server as json (json conversion is done for you)
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response put(String urlString, Object payload) throws RestException {
    return put(urlString, payload, null);
  }

  /**
   * Executes a HTTP PUT request
   * @param urlString the url for the target resource
   * @param payload the content Java object to send to the server as json (json conversion is done for you)
   * @param requestHeaders  Map of the headers to add to the request
   * @return a response object with the header, body and response code
   * @throws RestException if something goes wrong
   */
  public Response put(String urlString, Object payload, Map<String, String> requestHeaders) throws RestException {
    return putPost(urlString, payload, requestHeaders, "PUT");
  }

  /**
   * Executes a HTTP DELETE request
   * @param urlString the url for the target resource
   * @return a Response containing headers and status code and, possibly, the body content
   * @throws RestException if something goes wrong
   */
  public Response delete(String urlString) throws RestException {
    return delete(urlString, null);
  }

  /**
   * Executes a HTTP DELETE request
   * @param urlString the url for the target resource
   * @param requestHeaders  Map of the headers to add to the request
   * @return a Response containing headers and status code and, possibly, the body content
   * @throws RestException if something goes wrong
   */
  public Response delete(String urlString, Map<String, String> requestHeaders) throws RestException {
    StringBuilder writer = new StringBuilder();
    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(false);
      conn.setRequestMethod("DELETE");
      if (requestHeaders != null && !requestHeaders.containsKey(CONTENT_TYPE)) {
        conn.setRequestProperty(CONTENT_TYPE, MediaType.APPLICATION_JSON.getValue());
      }
      if (requestHeaders != null) {
        requestHeaders.forEach(conn::setRequestProperty);
      }
      conn.connect();
      int responseCode = conn.getResponseCode();
      var headers = conn.getHeaderFields();

      InputStream is = null;
      try {
        is = conn.getInputStream();
      } catch (IOException e) {
        // no content
      }
      if (is != null) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
          writer.append(line).append('\n');
        }
        is.close();
      }
      conn.disconnect();
      return new Response(writer.toString(), responseCode, headers, mapper);
    } catch (IOException e) {
      throw new RestException("Failed to call DELETE on " + urlString, e);
    }
  }

  /**
   * Executes a HTTP HEAD request
   * @param urlString the url for the target resource
   * @return a Response containing headers and status code, no body content (there SHOULD not be any)
   * @throws RestException if something goes wrong
   */
  public Response head(String urlString) throws RestException {
    return head(urlString, null);
  }

  /**
   * Executes a HTTP HEAD request
   * @param urlString the url for the target resource
   * @param requestHeaders  Map of the headers to add to the request
   * @return a Response containing headers and status code, no body content (there SHOULD not be any)
   * @throws RestException if something goes wrong
   */
  public Response head(String urlString, Map<String, String> requestHeaders) throws RestException {
    return headersRequest(urlString, requestHeaders, "HEAD");
  }

  /**
   * Executes a HTTP OPTIONS request
   * @param urlString the url for the target resource
   * @return a Response containing headers and status code, no body content (there SHOULD not be any)
   * @throws RestException @throws RestException if something goes wrong
   */
  public Response options(String urlString) throws RestException {
    return options(urlString, null);
  }

  /**
   * Executes a HTTP OPTIONS request
   * @param urlString the url for the target resource
   * @param requestHeaders  Map of the headers to add to the request
   * @return a Response containing headers and status code, no body content (there SHOULD not be any)
   * @throws RestException @throws RestException if something goes wrong
   */
  public Response options(String urlString, Map<String, String> requestHeaders) throws RestException {
    return headersRequest(urlString, requestHeaders, "OPTIONS");
  }

  private Response headersRequest(String urlString, Map<String, String> requestHeaders, String method) throws RestException {
    String accept = MediaType.APPLICATION_JSON.getValue();

    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
    StringBuilder writer = new StringBuilder();
    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(method);
      if (requestHeaders != null && !requestHeaders.containsKey(CONTENT_TYPE)) {
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
        OutputStream os = conn.getOutputStream();
        os.write(input.getBytes());
        os.flush();
        os.close();
      } else {
        conn.connect();
      }

      int responseCode = conn.getResponseCode();
      var headers = conn.getHeaderFields();
      InputStream is = null;
      try {
        is = conn.getInputStream();
      } catch (IOException e) {
        // no content
      }
      if (is != null) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        while ((line = br.readLine()) != null) {
          writer.append(line).append('\n');
        }
        is.close();
      }
      conn.disconnect();
      return new Response(writer.toString(), responseCode, headers, mapper);

    } catch (IOException e) {
      throw new RestException("Failed to call " + method + " on " + urlString, e);
    }
  }
}
