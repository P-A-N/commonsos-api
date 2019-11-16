package commonsos.exception;

public class EthBalanceHistoryNotFoundException extends BadRequestException {
  private static final long serialVersionUID = 1L;
  
  public EthBalanceHistoryNotFoundException() {
    super("eth balance history not found");
  }
}
