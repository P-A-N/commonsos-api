package commonsos.view.app;

import java.time.Instant;
import java.util.List;

import commonsos.view.CommonView;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true)
public class MessageThreadView extends CommonView {
  private Long id;
  private AdView ad;
  private Long communityId;
  private String title;
  private String personalTitle;
  private List<PublicUserView> parties;
  private PublicUserView creator;
  private PublicUserView counterParty;
  private MessageView lastMessage;
  private boolean unread;
  private boolean group;
  private String photoUrl;
  private Instant createdAt;
}
