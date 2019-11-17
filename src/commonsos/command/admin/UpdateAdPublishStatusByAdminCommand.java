package commonsos.command.admin;

import commonsos.repository.entity.PublishStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UpdateAdPublishStatusByAdminCommand {
  private Long id;
  private PublishStatus publishStatus;
}