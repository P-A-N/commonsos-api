package commonsos.view.app;

import java.time.Instant;
import java.util.List;

import commonsos.view.CommonView;
import commonsos.view.UserTokenBalanceView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class PrivateUserView extends CommonView {
  private Long id;
  private String fullName;
  private String firstName;
  private String lastName;
  private String username;
  private String status;
  private String telNo;
  private List<CommunityUserView> communityList;
  private String description;
  private List<UserTokenBalanceView> balanceList;
  private String location;
  private String avatarUrl;
  private String emailAddress;
  private Instant loggedinAt;
}