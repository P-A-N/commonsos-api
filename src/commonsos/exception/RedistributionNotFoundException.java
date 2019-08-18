package commonsos.exception;

public class RedistributionNotFoundException extends BadRequestException {
  private static final long serialVersionUID = 1L;
  
  public RedistributionNotFoundException() {
    super("redistribution not found");
  }
}
