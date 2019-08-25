package commonsos.view.admin;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class UserForAdminView {
  private Long id;
  private String username;
  private String status;
  private String telNo;
  private List<CommunityUserForAdminView> communityList;
  private String avatarUrl;
  private String emailAddress;
  private Instant loggedinAt;
  private Instant createdAt;
}