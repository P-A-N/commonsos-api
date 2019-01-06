package commonsos.exception;

public class NotificationNotFoundException extends BadRequestException {
  private static final long serialVersionUID = 1L;
  
  public NotificationNotFoundException() {
    super("notification not found");
  }
}
