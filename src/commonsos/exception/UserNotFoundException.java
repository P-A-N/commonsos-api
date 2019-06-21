package commonsos.exception;

public class UserNotFoundException extends BadRequestException {
  public UserNotFoundException() {
    super("user not found");
  }
}
