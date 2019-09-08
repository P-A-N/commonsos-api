package commonsos;

import java.math.BigDecimal;
import java.net.URL;

import javax.inject.Singleton;

import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;

@Singleton
public class Cache {

  public static String SYS_CONFIG_KEY_MAINTENANCE_MODE = "maintenanceMode";
  
  private CacheManager cacheManager;
  private org.ehcache.Cache<String, String> tokenSymbolCache;
  private org.ehcache.Cache<String, String> tokenNameCache;
  private org.ehcache.Cache<String, BigDecimal> totalSupplyCache;
  private org.ehcache.Cache<String, String> systemConfigCache;
  
  public Cache() {
    URL url = this.getClass().getResource("/ehcache.xml"); 
    Configuration xmlConfig = new XmlConfiguration(url); 
    cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
    cacheManager.init();

    tokenSymbolCache = cacheManager.getCache("tokenSymbol", String.class, String.class);
    tokenNameCache = cacheManager.getCache("tokenName", String.class, String.class);
    totalSupplyCache = cacheManager.getCache("totalSupply", String.class, BigDecimal.class);
    systemConfigCache = cacheManager.getCache("systemConfig", String.class, String.class);
  }
  
  public String getTokenName(String tokenAddress) {
    return tokenNameCache.get(tokenAddress);
  }
  
  public void setTokenName(String tokenAddress, String tokenName) {
    tokenNameCache.put(tokenAddress, tokenName);
  }
  
  public String getTokenSymbol(String tokenAddress) {
    return tokenSymbolCache.get(tokenAddress);
  }
  
  public void setTokenSymbol(String tokenAddress, String tokenSymbol) {
    tokenSymbolCache.put(tokenAddress, tokenSymbol);
  }
  
  public BigDecimal getTotalSupply(String tokenAddress) {
    return totalSupplyCache.get(tokenAddress);
  }
  
  public void setTotalSupply(String tokenAddress, BigDecimal totalSupply) {
    totalSupplyCache.put(tokenAddress, totalSupply);
  }
  
  public String getSystemConfig(String configKey) {
    return systemConfigCache.get(configKey);
  }
  
  public void setSystemConfig(String configKey, String value) {
    systemConfigCache.put(configKey, value);
  }
}
