package commonsos.view.admin;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class AdminView {
  private Long id;
  private String adminname;
  private Long communityId;
  private Long roleId;
  private String rolename;
  private String emailAddress;
  private String telNo;
  private String department;
  private String photoUrl;
  private Instant loggedinAt;
  private Instant createdAt;
}