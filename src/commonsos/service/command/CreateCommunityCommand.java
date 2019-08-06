package commonsos.service.command;

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
  private Double transactionFee;
  private String description;
  private List<Long> adminIdList;
  private UploadPhotoCommand uploadPhotoCommand;
  private UploadPhotoCommand uploadCoverPhotoCommand;
}
