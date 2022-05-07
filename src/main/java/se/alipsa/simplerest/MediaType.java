package se.alipsa.simplerest;

public enum MediaType {
  APPLICATION_JSON("application/json"),
  APPLICATION_XML("application/xml");

  private final String value;

  MediaType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
