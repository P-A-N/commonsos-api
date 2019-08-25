package commonsos.service.command;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain=true)
public class CreateCommunityCommand {
  private String communityName;
  private String tokenName;
  private String tokenSymbol;
  private BigDecimal transactionFee;
  private String description;
  private List<Long> adminIdList;
  private UploadPhotoCommand uploadPhotoCommand;
  private UploadPhotoCommand uploadCoverPhotoCommand;
}
