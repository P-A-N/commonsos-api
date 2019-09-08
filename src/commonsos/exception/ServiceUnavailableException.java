package commonsos.exception;

public class ServiceUnavailableException extends CommonsOSException {
  private static final long serialVersionUID = 1L;
  
  public ServiceUnavailableException() {
  }

  public ServiceUnavailableException(String message) {
    super(message);
  }

  public ServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
