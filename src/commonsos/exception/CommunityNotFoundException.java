package commonsos.exception;

public class CommunityNotFoundException extends BadRequestException {
  private static final long serialVersionUID = 1L;
  
  public CommunityNotFoundException() {
    super("community not found");
  }
}
