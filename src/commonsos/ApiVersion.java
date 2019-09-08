package commonsos;

public enum ApiVersion {
  APP_API_VERSION(2,0,0);
  
  private int major;
  private int minor;
  private int patch;
  
  private ApiVersion(int major, int minor, int patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
  }
  
  public int conpareMajorTo(int major) {
    return Integer.compare(this.major, major);
  }
  
  @Override
  public String toString() {
    return String.format("%d.%d.%d", major, minor, patch);
  }
}
