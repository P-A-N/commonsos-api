package commonsos.repository.entity;

public enum SortType {
  ASC, DESC;
  
  public static SortType of(String value, SortType def) {
    for (SortType sort : values()) {
      if (sort.name().equals(value)) return sort;
    }
    return def;
  }
}
