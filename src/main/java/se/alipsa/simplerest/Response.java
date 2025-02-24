package se.alipsa.simplerest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    // does not work, content is returned as List<Map<String,String>>:
    // return mapper.readValue(getPayload(), new TypeReference<List<T>>() {});
    return mapper.readValue(getPayload(), mapper.getTypeFactory().constructCollectionType(List.class, returnClass));
  }

  /**
   * Converts the json payload into a List of Java objects dealing with non primitives such as Lists and Maps.
   * Example: <code>TypeReference ref = new TypeReference&lt;List&gt;BigDecimal&gt;&gt;() { };</code>
   *
   * @param type the type reference to used when mapping
   * @param customMapper optional ObjectMapper to use
   * @return an Object of the class defined in the TypeReference
   * @throws JsonProcessingException  if the conversion failed.
   */
  public <T> T getForType(TypeReference<T> type, ObjectMapper... customMapper) throws JsonProcessingException {
    ObjectMapper mapper = customMapper.length > 0 ? customMapper[0] : objectMapper;
    return mapper.reader().forType(type).readValue(getPayload());
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

  /**
   *
   * @return the http status code (e.g. 200 for OK, 404 for not found etc.)
   */
  public int getResponseCode() {
    return responseCode;
  }

  /**
   * @param responseCode set the response code for this response
   */
  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  /**
   * @return The headers in this response
   */
  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  /**
   * @param headers set the headers for this response
   */
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

  @Override
  public boolean equals(Object o) {
    if (! (o instanceof Response other)) {
      return false;
    }
    return getResponseCode() == other.getResponseCode() && String.valueOf(getPayload()).equals(other.getPayload());
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(getPayload());
    result = 31 * result + getResponseCode();
    return result;
  }
}
