package commonsos.exception;

public class TokenTransactionNotFoundException extends BadRequestException {
  private static final long serialVersionUID = 1L;
  
  public TokenTransactionNotFoundException() {
    super("token transaction not found");
  }
}
