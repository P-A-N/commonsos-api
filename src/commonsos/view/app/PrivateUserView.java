package commonsos.view.app;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class PrivateUserView {
  private Long id;
  private String fullName;
  private String firstName;
  private String lastName;
  private String username;
  private String status;
  private String telNo;
  private List<CommunityUserView> communityList;
  private String description;
  private List<BalanceView> balanceList;
  private String location;
  private String avatarUrl;
  private String emailAddress;
  private Instant loggedinAt;
}