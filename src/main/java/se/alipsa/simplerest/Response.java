package se.alipsa.simplerest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * The Response class is a core part of the simple-rest api.
 * It contains the "raw" json string returned from the server and also the response code and header fields.
 * The response can
 */
public class Response {

  private String payload;
  private int responseCode;
  private Map<String, List<String>> headers;

  private final ObjectMapper objectMapper;

  /**
   * Default constructor, will create a vanilla ObjectMapper for use in subsequent calls.
   */
  public Response() {
    objectMapper = new ObjectMapper();
  }

  /**
   *
   * @param payload the "raw" content of the json response
   * @param responseCode the HTTP status code, e.g. 200 for OK etc.
   * @param headers a map of the HTTP Header content
   * @param prefObjectMapper an optional parameter for using a preferred Object Mapper other than the default on
   */
  public Response(String payload, int responseCode, Map<String, List<String>> headers, ObjectMapper... prefObjectMapper) {
    this.payload = payload;
    this.responseCode = responseCode;
    this.headers = headers;
    objectMapper = prefObjectMapper.length > 0 ? prefObjectMapper[0] : new ObjectMapper();
  }

  /**
   * Converts the json payload into a Java object
   * @param returnClass the type of Java Object to return
   * @param <T> the type of Java Object to return
   * @param customMapper an optional ObjectMapper
   * @return the java Object corresponding to the payload and the returnClass
   * @throws JsonProcessingException if the conversion failed.
   */
  public <T> T getObject(Class<T> returnClass, ObjectMapper... customMapper) throws JsonProcessingException {
    ObjectMapper mapper = customMapper.length > 0 ? customMapper[0] : objectMapper;
    return mapper.readValue(getPayload(), returnClass);
  }

  /**
   * Converts the json payload into a List of Java objects
   * @param returnClass the type of Java Object to return
   * @param <T> the type of Java Object in the List to return
   * @param customMapper an optional ObjectMapper
   * @return a List of the type corresponding to the returnClass provided
   * @throws JsonProcessingException if the conversion failed.
   */
  public <T> List<T> getObjectList(Class<T> returnClass, ObjectMapper... customMapper) throws JsonProcessingException {
    ObjectMapper mapper = customMapper.length > 0 ? customMapper[0] : objectMapper;
    //return mapper.readValue(getPayload(), new TypeReference<List<T>>() {});
    return mapper.readValue(getPayload(), mapper.getTypeFactory().constructCollectionType(List.class, returnClass));
  }

  /**
   *
   * @return the "raw" content of the response
   */
  public String getPayload() {
    return payload;
  }

  /**
   * Change the raw content
   * @param payload the payload to replace
   */
  public void setPayload(String payload) {
    this.payload = payload;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, List<String>> headers) {
    this.headers = headers;
  }

  /**
   * Most of the time you do not deal with multiple headers of the same value being set
   * This is a convenience method to quickly get the header value.
   * @param headerName the name of the header
   * @return the value of the header with the name mathing the param
   */
  public String getHeader(String headerName) {
    List<String> values = headers.get(headerName);
    if (values == null || values.isEmpty()) {
      return null;
    }
    return values.get(0);
  }

  @Override
  public String toString() {
    return getResponseCode() + ", " + getPayload();
  }
}
