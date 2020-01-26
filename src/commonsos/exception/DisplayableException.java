package commonsos.exception;

public class DisplayableException extends CommonsOSException {
  private static final long serialVersionUID = 1L;
  
  public static DisplayableException ADMIN_IS_NOT_COMMUNITY_ADMIN = new DisplayableException("error.adminIsNotCommunityAdmin");
  public static DisplayableException ADMIN_BELONGS_TO_OTHER_COMMUNITY = new DisplayableException("error.adminBelongsToOtherCommunity");
  public static DisplayableException NOT_ENOUGH_ETHER_TO_INITIATE_COMMUNITY = new DisplayableException("error.notEnoughEtherToInitiateCommunity");
  public static DisplayableException NOT_ENOUGH_COIN_TO_BURN_FROM_COMMUNITY = new DisplayableException("error.notEnoughCoinToBurnFromCommunity");
  public static DisplayableException INVALID_UPDATE_STATUS_PUPLIC_TO_PRIVATE = new DisplayableException("error.invalid_update_status_puplic_to_private");
  public static DisplayableException OUT_OF_ETHER = new DisplayableException("error.outOfEther");
  
  public static DisplayableException getRequiredException(String param) {
    return new DisplayableException(String.format("error.%sRequired", param));
  }
  
  public static DisplayableException getInvalidException(String param) {
    return new DisplayableException(String.format("error.invalid_%s", param));
  }
  
  public static DisplayableException getInvalidCharacterException(String param) {
    return new DisplayableException(String.format("error.invalid_character_in_%s", param));
  }
  
  public static DisplayableException getInvalidlengthException(String param) {
    return new DisplayableException(String.format("error.invalid_%s_length", param));
  }
  
  public static DisplayableException getNotSupportedException(String param) {
    return new DisplayableException(String.format("error.%s_not_supported.", param));
  }
  
  public static DisplayableException getTakenException(String param) {
    return new DisplayableException(String.format("error.%sTaken", param));
  }

  public DisplayableException(String message) {
    super(message);
  }
}
