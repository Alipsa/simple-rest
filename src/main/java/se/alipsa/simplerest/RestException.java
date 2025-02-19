package se.alipsa.simplerest;

/**
 * The exception thrown when there is a problem executing the call
 */
public class RestException extends Exception {

  /**
   * Create a rest exception.
   *
   * @param message the message indicating the issue
   */
  public RestException(String message) {
    super(message);
  }

  /**
   * Create a rest exception, a wrapper for all kinds of
   * exceptions from the underlying code.
   *
   * @param message the message indicating the issue
   * @param cause the cause of the exception
   */
  public RestException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a rest exception, a wrapper for all kinds of
   * exceptions from the underlying code.
   * Wraps another exception in a RestException
   *
   * @param cause the cause of the exception
   */
  public RestException(Throwable cause) {
    super(cause);
  }
}
