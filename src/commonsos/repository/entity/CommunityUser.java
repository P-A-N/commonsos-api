package commonsos.repository.entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity @Table(name = "community_users")
@Getter @Setter @Accessors(chain=true)
public class CommunityUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @OneToOne
  @JoinColumn(name = "community_id")
  private Community community;
  @Column(name = "user_id")
  private Long userId;
  private Instant walletLastViewTime;
  private Instant adLastViewTime;
  private Instant notificationLastViewTime;
}
