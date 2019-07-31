package commonsos.repository.entity;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "community_users")
@Getter @Setter @Accessors(chain=true) @ToString
public class CommunityUser extends AbstractEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @OneToOne
  @JoinColumn(name = "community_id")
  private Community community;
  private Long userId;
  private Instant walletLastViewTime = Instant.EPOCH;
  private Instant adLastViewTime = Instant.EPOCH;
  private Instant notificationLastViewTime = Instant.EPOCH;
}
