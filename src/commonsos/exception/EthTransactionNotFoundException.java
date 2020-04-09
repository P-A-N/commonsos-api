package commonsos.exception;

public class EthTransactionNotFoundException extends BadRequestException {
  private static final long serialVersionUID = 1L;
  
  public EthTransactionNotFoundException() {
    super("eth transaction not found");
  }
}
