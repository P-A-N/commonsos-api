package commonsos.service.httprequest;

import javax.inject.Inject;
import javax.inject.Singleton;

import commonsos.Configuration;
import commonsos.exception.ServerErrorException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Singleton
@Slf4j
public class WordpressRequestService extends AbstractHttpRequestService {
  
  @Inject Configuration conf;

  public void sendCreateWordPressAccount(Long communityId, String wpUsername, String wpEmailAddress, String wpDisplayname) {
    HttpUrl url = new HttpUrl.Builder()
        .scheme("http")
        .host(conf.wordpressServerIp())
        .addPathSegment("wp-json")
        .addPathSegment("wp")
        .addPathSegment("v2")
        .build();
    
    RequestBody body = new FormBody.Builder()
        .add("username", wpUsername)
        .add("email", wpEmailAddress)
        .add("name", wpUsername)
        .add("password", conf.wordpressAccountDefaultPassword())
        .add("roles", conf.wordpressAccountDefaultAuthority())
        .add("community_id", String.valueOf(communityId))
        .build();
    
    String basicAuthorization = Credentials.basic(
        conf.wordpressBasicAuthorizationUsername(),
        conf.wordpressBasicAuthorizationPassword());
    
    Request request = new Request.Builder()
        .url(url)
        .addHeader("Authorization", basicAuthorization)
        .post(body)
        .build();
    
    try (Response response = execute(request)) {
      if (response.code() != 200) throw new ServerErrorException(String.format("Failed to create WordPress account. [Status=%d, Message=%s]", response.code(), response.message()));
    }
  }
}
