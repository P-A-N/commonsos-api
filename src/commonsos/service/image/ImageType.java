package commonsos.service.image;

public enum ImageType {
  JPEG,PNG;
  
  public static ImageType valueOf(String value, boolean errorIfNotFound) {
    if (errorIfNotFound) return valueOf(value);
    
    if (value == null) return null;
    
    for (ImageType type : values()) {
      if (type.name().equals(value.toUpperCase())) return type;
    }
    
    return null;
  }
}
