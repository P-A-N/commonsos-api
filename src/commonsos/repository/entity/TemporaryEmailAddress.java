package commonsos.repository.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "temporary_email_address")
@Getter @Setter @Accessors(chain=true) @ToString @EqualsAndHashCode(callSuper=false, of= {"id"})
public class TemporaryEmailAddress extends AbstractEntity {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  private String accessIdHash;
  private Instant expirationTime;
  private boolean invalid;
  private Long userId;
  private String emailAddress;
}
