package commonsos.view.app;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class PublicUserView {
  private Long id;
  private String fullName;
  private String username;
  private String status;
  private List<CommunityView> communityList;
  private String description;
  private String location;
  private String avatarUrl;
}