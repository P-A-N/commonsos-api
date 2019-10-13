package commonsos.repository.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "admins")
@Getter @Setter @Accessors(chain=true) @ToString @EqualsAndHashCode(callSuper=false, of= {"id"})
public class Admin extends AbstractEntity {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  private String emailAddress;
  private String adminname;
  private String passwordHash;
  @ManyToOne
  @JoinColumn(name = "community_id")
  private Community community;
  @ManyToOne
  @JoinColumn(name = "role_id")
  private Role role;
  private String telNo;
  private String department;
  private String photoUrl;
  private boolean deleted;
  private Instant loggedinAt;
}
