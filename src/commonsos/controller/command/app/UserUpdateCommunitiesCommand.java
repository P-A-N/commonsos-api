package commonsos.controller.command.app;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @EqualsAndHashCode @Accessors(chain=true) @ToString
public class UserUpdateCommunitiesCommand {
  private List<Long> communityList;
}