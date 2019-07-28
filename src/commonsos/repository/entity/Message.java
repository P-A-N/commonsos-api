package commonsos.repository.entity;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity @Table(name="messages")
@Getter @Setter @Accessors(chain=true) @ToString
public class Message extends AbstractEntity {
  @Id @GeneratedValue(strategy = IDENTITY) private Long id;
  private Long createdBy;
  private Long threadId;
  private String text;
}
