package commonsos.exception;

public class MessageThreadNotFoundException extends BadRequestException {
  private static final long serialVersionUID = 1L;
  
  public MessageThreadNotFoundException() {
    super("message thread not found");
  }
}
