package commonsos.repository.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "message_thread_parties")
@Getter @Setter @Accessors(chain=true) @ToString @EqualsAndHashCode(callSuper=false, of= {"id"})
public class MessageThreadParty extends AbstractEntity {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  @Column(name = "message_thread_id") private Long messageThreadId;
  @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
  private String personalTitle;
  private String photoUrl;
  private Instant visitedAt;
}