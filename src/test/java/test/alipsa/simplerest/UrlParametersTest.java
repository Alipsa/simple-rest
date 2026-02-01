package test.alipsa.simplerest;

import org.junit.jupiter.api.Test;
import se.alipsa.simplerest.UrlParameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UrlParametersTest {

  @Test
  public void testUrlParams() {
    assertEquals("?foo=123&bar=898", UrlParameters.parameters("foo", "123", "bar", "898"));
  }

  @Test
  public void testOddParamCount() {
    assertThrows(IllegalArgumentException.class, () -> UrlParameters.parameters("foo", "123", "bar"));
  }
}
