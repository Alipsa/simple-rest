package test.alipsa.simplerest;

import org.junit.jupiter.api.Test;
import se.alipsa.simplerest.Response;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ResponseTest {

  @Test
  public void testNullPayloadEquals() {
    Response nullPayload = new Response(null, 200, Map.of());
    Response stringNullPayload = new Response("null", 200, Map.of());
    assertNotEquals(nullPayload, stringNullPayload);
  }

  @Test
  public void testHeaderLookupCaseInsensitive() {
    Response response = new Response("", 200, Map.of("content-type", List.of("application/json")));
    assertEquals("application/json", response.getHeader("Content-Type"));
  }

  @Test
  public void testHeaderLookupNullMap() {
    Response response = new Response();
    assertNull(response.getHeader("Content-Type"));
  }
}
