package se.alipsa.simplerest;

/**
 * The exception thrown when there is a problem executing the call
 */
public class RestException extends Exception {

  /** Default ctor */
  public RestException() {
    super();
  }

  /**
   * @param message the message indicating the issue
   */
  public RestException(String message) {
    super(message);
  }

  /**
   *
   * @param message the message indicating the issue
   * @param cause the cause of the exception
   */
  public RestException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Wraps another exception in a RestException
   * @param cause the cause of the exception
   */
  public RestException(Throwable cause) {
    super(cause);
  }
}
