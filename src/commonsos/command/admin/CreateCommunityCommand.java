package commonsos.command.admin;

import java.math.BigDecimal;
import java.util.List;

import commonsos.command.UploadPhotoCommand;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class CreateCommunityCommand {
  private String communityName;
  private String tokenName;
  private String tokenSymbol;
  private String wordpressAccountId;
  private String wordpressAccountEmailAddress;
  private BigDecimal transactionFee;
  private String description;
  private List<Long> adminIdList;
  private UploadPhotoCommand uploadPhotoCommand;
  private UploadPhotoCommand uploadCoverPhotoCommand;
}
