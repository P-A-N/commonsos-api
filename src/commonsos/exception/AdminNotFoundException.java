package commonsos.exception;

public class AdminNotFoundException extends BadRequestException {
  private static final long serialVersionUID = 1L;
  
  public AdminNotFoundException() {
    super("admin not found");
  }
}
