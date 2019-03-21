package commonsos.exception;

public class AuthenticationException extends CommonsOSException {
  private static final long serialVersionUID = 1L;

  public AuthenticationException() {
  }

  public AuthenticationException(String message) {
    super(message);
  }
}
