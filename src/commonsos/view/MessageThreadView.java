package commonsos.view;

import java.time.Instant;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @Setter @Accessors(chain=true) @EqualsAndHashCode @ToString
public class MessageThreadView {
  private Long id;
  private AdView ad;
  private Long communityId;
  private String title;
  private String personalTitle;
  private List<UserView> parties;
  private UserView creator;
  private UserView counterParty;
  private MessageView lastMessage;
  private boolean unread;
  private boolean group;
  private String photoUrl;
  private Instant createdAt;
}
