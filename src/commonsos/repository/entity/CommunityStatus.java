package commonsos.repository.entity;

public enum CommunityStatus {
  PUBLIC, PRIVATE;
  
  public static CommunityStatus of(String value, CommunityStatus def) {
    for (CommunityStatus sort : values()) {
      if (sort.name().equals(value)) return sort;
    }
    return def;
  }
}
