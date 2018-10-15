package commonsos.repository.user;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import commonsos.repository.community.Community;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "users")
@Getter @Setter @Accessors(chain=true) @EqualsAndHashCode @ToString
public class User {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  private String username;
  private String passwordHash;
  private String firstName;
  private String lastName;
  private String description;
  private String location;
  private String avatarUrl;
  private String wallet;
  private String walletAddress;
  private String pushNotificationToken;
  private String emailAddress;
  private boolean deleted;

  @ManyToMany
  @JoinTable(
    name = "community_users",
    joinColumns = @JoinColumn(name="user_id"),
    inverseJoinColumns = @JoinColumn(name="community_id"))
  private List<Community> joinedCommunities;
}
