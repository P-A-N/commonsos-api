package commonsos.exception;

public class BadRequestException extends CommonsOSException {
  private static final long serialVersionUID = 1L;
  
  public BadRequestException() {
  }

  public BadRequestException(String message) {
    super(message);
  }
}
