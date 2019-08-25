package commonsos.repository.entity;

public enum WalletType {
  MAIN, FEE;
  
  public static WalletType of(String walletDivision) {
    if (walletDivision == null) return null;
    for (WalletType type : WalletType.values()) {
      if (type.name().equals(walletDivision.toUpperCase())) return type;
    }
    return null;
  }
}
