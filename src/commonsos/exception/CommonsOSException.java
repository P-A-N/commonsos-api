package commonsos.exception;

public class CommonsOSException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public CommonsOSException() {
  }

  public CommonsOSException(String message) {
    super(message);
  }

  public CommonsOSException(String message, Throwable cause) {
    super(message, cause);
  }

  public CommonsOSException(Throwable cause) {
    super(cause);
  }
}
