package commonsos.command.admin;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UpdateUserCommunitiesByAdminCommand {
  private Long id;
  private List<Long> communityList;
}