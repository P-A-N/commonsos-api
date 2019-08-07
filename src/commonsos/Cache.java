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

  private CacheManager cacheManager;
  private org.ehcache.Cache<Long, String> tokenSymbolCache;
  private org.ehcache.Cache<Long, String> tokenNameCache;
  private org.ehcache.Cache<Long, BigDecimal> totalSupplyCache;
  
  public Cache() {
    URL url = this.getClass().getResource("/ehcache.xml"); 
    Configuration xmlConfig = new XmlConfiguration(url); 
    cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
    cacheManager.init();

    tokenSymbolCache = cacheManager.getCache("tokenSymbol", Long.class, String.class);
  }
  
  public String getTokenName(Long communityId) {
    return tokenNameCache.get(communityId);
  }
  
  public void setTokenName(Long communityId, String tokenName) {
    tokenNameCache.put(communityId, tokenName);
  }
  
  public String getTokenSymbol(Long communityId) {
    return tokenSymbolCache.get(communityId);
  }
  
  public void setTokenSymbol(Long communityId, String tokenSymbol) {
    tokenSymbolCache.put(communityId, tokenSymbol);
  }
  
  public BigDecimal getTotalSupply(Long communityId) {
    return totalSupplyCache.get(communityId);
  }
  
  public void setTotalSupply(Long communityId, BigDecimal totalSupply) {
    totalSupplyCache.put(communityId, totalSupply);
  }
}
