package commonsos.exception;

public class CommunityNotificationNotFoundException extends BadRequestException {
  private static final long serialVersionUID = 1L;
  
  public CommunityNotificationNotFoundException() {
    super("community notification not found");
  }
}
