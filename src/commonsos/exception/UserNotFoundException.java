package commonsos.exception;

public class UserNotFoundException extends BadRequestException {
  private static final long serialVersionUID = 1L;
  
  public UserNotFoundException() {
    super("user not found");
  }
}
