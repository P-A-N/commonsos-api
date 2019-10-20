package commonsos.command.app;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @ToString
public class UpdateUserCommand {
  private String firstName;
  private String lastName;
  private String description;
  private String location;
  private String telNo;
}