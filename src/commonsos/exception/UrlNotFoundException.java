package commonsos.exception;

public class UrlNotFoundException extends CommonsOSException {
  private static final long serialVersionUID = 1L;
  
  public UrlNotFoundException() {
  }

  public UrlNotFoundException(String message) {
    super(message);
  }

  public UrlNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
