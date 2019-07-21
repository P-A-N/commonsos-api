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
  private org.ehcache.Cache<Long, String> tokenNameCache;
  private org.ehcache.Cache<Long, String> tokenSymbolCache;
  private org.ehcache.Cache<Long, BigDecimal> tokenTotalSupplyCache;
  
  public Cache() {
    URL url = this.getClass().getResource("/ehcache.xml"); 
    Configuration xmlConfig = new XmlConfiguration(url); 
    cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
    cacheManager.init();

    tokenNameCache = cacheManager.getCache("tokenName", Long.class, String.class);
    tokenSymbolCache = cacheManager.getCache("tokenSymbol", Long.class, String.class);
    tokenTotalSupplyCache = cacheManager.getCache("tokenTotalSupply", Long.class, BigDecimal.class);
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
  
  public BigDecimal getTokenTotalSupply(Long communityId) {
    return tokenTotalSupplyCache.get(communityId);
  }
  
  public void setTokenTotalSupply(Long communityId, BigDecimal tokenTotalSupply) {
    tokenTotalSupplyCache.put(communityId, tokenTotalSupply);
  }
}
