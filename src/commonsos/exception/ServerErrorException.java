package commonsos.exception;

public class ServerErrorException extends CommonsOSException {
  private static final long serialVersionUID = 1L;
  
  public ServerErrorException() {
  }

  public ServerErrorException(String message) {
    super(message);
  }

  public ServerErrorException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServerErrorException(Throwable cause) {
    super(cause);
  }
}
