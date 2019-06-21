package commonsos.repository.entity;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity @Table(name = "community_notifications")
@Getter @Setter @Accessors(chain=true)
public class CommunityNotification {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  private Long communityId;
  private String wordpressId;
  private Instant updatedAt;
}
