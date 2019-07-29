package commonsos.repository.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.time.Instant;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "temporary_users")
@Getter @Setter @Accessors(chain=true) @ToString
public class TemporaryUser extends AbstractEntity {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  private String accessIdHash;
  private Instant expirationTime;
  private boolean invalid;
  private String description;
  private String firstName;
  private String lastName;
  private String location;
  private String passwordHash;
  private String username;
  private String telNo;
  private String emailAddress;
  private boolean waitUntilCompleted;

  @ManyToMany
  @JoinTable(
    name = "temporary_community_users",
    joinColumns = @JoinColumn(name="temporary_user_id"),
    inverseJoinColumns = @JoinColumn(name="community_id"))
  private List<Community> communityList;
}
