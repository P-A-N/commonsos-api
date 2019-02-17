package commonsos.exception;

public class ForbiddenException extends CommonsOSException {
  private static final long serialVersionUID = 1L;
  
  public ForbiddenException() {
  }

  public ForbiddenException(String message) {
    super(message);
  }
}
