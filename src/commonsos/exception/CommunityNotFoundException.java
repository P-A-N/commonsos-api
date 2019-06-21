package commonsos.exception;

public class CommunityNotFoundException extends BadRequestException {
  public CommunityNotFoundException() {
    super("community not found");
  }
}
