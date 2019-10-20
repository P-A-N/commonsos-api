package commonsos.command.admin;

import commonsos.command.UploadPhotoCommand;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class CreateAdminTemporaryCommand {
  private String adminname;
  private String password;
  private Long communityId;
  private Long roleId;
  private String emailAddress;
  private String telNo;
  private String department;
  private UploadPhotoCommand uploadPhotoCommand;
}
