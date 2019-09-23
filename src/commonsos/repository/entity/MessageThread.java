package commonsos.repository.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name="message_threads")
@Getter @Setter @Accessors(chain=true) @ToString
public class MessageThread extends AbstractEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
  private Long adId;
  private Long communityId;
  private String title;
  private Long createdUserId;
  @Column(name = "is_group") private boolean group;
  private boolean deleted;

  @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "message_thread_id", referencedColumnName = "id")
  private List<MessageThreadParty> parties = new ArrayList<>();
}
