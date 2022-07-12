package se.alipsa.simplerest;

/**
 * A media type describes the type of content
 */
public enum MediaType {
  /** The media type for json payload */
  APPLICATION_JSON("application/json"),
  /** THe media type for xml payload */
  APPLICATION_XML("application/xml");

  private final String value;

  MediaType(String value) {
    this.value = value;
  }

  /**
   * @return The actual value to use
   */
  public String getValue() {
    return value;
  }
}
