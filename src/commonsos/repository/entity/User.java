package commonsos.repository.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.time.Instant;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "users")
@Getter @Setter @Accessors(chain=true) @ToString @EqualsAndHashCode(callSuper=false, of= {"id"})
public class User extends AbstractEntity {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  private String username;
  private String passwordHash;
  private String firstName;
  private String lastName;
  private String description;
  private String location;
  private String avatarUrl;
  private String status;
  private String telNo;
  private String wallet;
  private String walletAddress;
  private String pushNotificationToken;
  private String emailAddress;
  private boolean deleted;
  private Instant loggedinAt;

  @OneToMany(cascade = {CascadeType.ALL})
  @OrderBy("community.id ASC")
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private List<CommunityUser> communityUserList;
}
