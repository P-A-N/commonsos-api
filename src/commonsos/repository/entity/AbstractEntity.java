package commonsos.repository.entity;

import java.time.Instant;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@MappedSuperclass
@Getter @Setter @Accessors(chain=true) @ToString
public abstract class AbstractEntity {
  private Instant createdAt;
  private Instant updatedAt;

  @PrePersist
  private void prePersist() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
  }
  
  @PreUpdate
  private void preUpdated() {
    updatedAt = Instant.now();
  }
}
