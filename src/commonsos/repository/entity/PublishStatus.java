package commonsos.repository.entity;

public enum PublishStatus {
  PUBLIC, PRIVATE;
  
  public static PublishStatus of(String value, PublishStatus def) {
    for (PublishStatus sort : values()) {
      if (sort.name().equals(value)) return sort;
    }
    return def;
  }
}
