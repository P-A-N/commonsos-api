package commonsos.exception;

public class AdNotFoundException extends BadRequestException {
  private static final long serialVersionUID = 1L;

  public AdNotFoundException() {
    super("ad not found");
  }
}
