package commonsos.repository.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name = "temporary_admin_email_address")
@Getter @Setter @Accessors(chain=true) @ToString
public class TemporaryAdminEmailAddress extends AbstractEntity {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  private String accessIdHash;
  private Instant expirationTime;
  private boolean invalid;
  private Long adminId;
  private String emailAddress;
}
