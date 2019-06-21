package commonsos.view;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class UserPrivateView {
  private Long id;
  private String fullName;
  private String firstName;
  private String lastName;
  private String username;
  private List<CommunityView> communityList;
  private String description;
  private List<BalanceView> balanceList;
  private String location;
  private String avatarUrl;
  private String emailAddress;
}