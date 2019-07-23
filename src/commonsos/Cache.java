package commonsos;

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
  
  public Cache() {
    URL url = this.getClass().getResource("/ehcache.xml"); 
    Configuration xmlConfig = new XmlConfiguration(url); 
    cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
    cacheManager.init();

    tokenSymbolCache = cacheManager.getCache("tokenSymbol", Long.class, String.class);
  }
  
  public String getTokenSymbol(Long communityId) {
    return tokenSymbolCache.get(communityId);
  }
  
  public void setTokenSymbol(Long communityId, String tokenSymbol) {
    tokenSymbolCache.put(communityId, tokenSymbol);
  }
}
