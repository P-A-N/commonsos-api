package commonsos.annotation;

public enum IP {
  WORDPRESS_SERVER("WORDPRESS_SERVER_IP");
  
  private String configurationKey;
  
  private IP(String configurationKey) {
    this.configurationKey = configurationKey;
  }
  
  public String getConfigurationKey() {
    return configurationKey;
  }
}
