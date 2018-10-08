package commonsos.domain.auth;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "users")
@Getter @Setter @Accessors(chain=true) @EqualsAndHashCode @ToString
public class User {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  private Long communityId;
  private boolean admin;
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
}
