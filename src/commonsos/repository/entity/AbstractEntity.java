package commonsos.repository.entity;

import java.time.Instant;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import commonsos.ThreadValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@MappedSuperclass
@Getter @Setter @Accessors(chain=true) @ToString
public abstract class AbstractEntity {
  private String createdBy;
  private String updatedBy;
  private Instant createdAt;
  private Instant updatedAt;

  @PrePersist
  private void prePersist() {
    createdBy = ThreadValue.getRequestedBy();
    createdAt = Instant.now();
  }
  
  @PreUpdate
  private void preUpdated() {
    updatedBy = ThreadValue.getRequestedBy();
    updatedAt = Instant.now();
  }
}
