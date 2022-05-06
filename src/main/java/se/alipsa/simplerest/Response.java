package se.alipsa.simplerest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class Response {

  private String payload;
  private int responseCode;
  private Map<String, List<String>> headers;

  public Response() {}

  public Response(String payload, int responseCode, Map<String, List<String>> headers) {
    this.payload = payload;
    this.responseCode = responseCode;
    this.headers = headers;
  }

  public <T> T getObject(Class<T> returnClass, ObjectMapper... customMapper) throws JsonProcessingException {
    ObjectMapper mapper = customMapper.length > 0 ? customMapper[0] : new ObjectMapper();
    return mapper.readValue(getPayload(), returnClass);
  }

  public <T> List<T> getObjectList(Class<T> returnClass, ObjectMapper... customMapper) throws JsonProcessingException {
    ObjectMapper mapper = customMapper.length > 0 ? customMapper[0] : new ObjectMapper();
    return mapper.readValue(getPayload(), new TypeReference<>() {});
  }

  public String getPayload() {
    return payload;
  }

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
