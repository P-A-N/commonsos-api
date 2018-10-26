package commonsos.exception;

public class AdNotFoundException extends BadRequestException {
  public AdNotFoundException() {
    super("ad not found");
  }
}
