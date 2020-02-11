package commonsos.service.httprequest;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gson.Gson;

import commonsos.Configuration;
import commonsos.exception.ServerErrorException;
import commonsos.service.httprequest.body.WordpressRequestBody;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Singleton
public class WordpressRequestService extends AbstractHttpRequestService {
  
  @Inject private Configuration conf;
  @Inject private Gson gson;
  private MediaType mediaType;
  private HttpUrl url;
  private String basicAuthorization;
  
  @Inject
  public void init() {
    mediaType = MediaType.parse("application/json; charset=utf-8");
    url = new HttpUrl.Builder()
        .scheme("http")
        .host(conf.wordpressServerIp())
        .port(Integer.parseInt(conf.wordpressServerApiPort()))
        .addPathSegments(conf.wordpressServerApiCreateUserPath())
        .build();
    basicAuthorization = Credentials.basic(
        conf.wordpressBasicAuthorizationUsername(),
        conf.wordpressBasicAuthorizationPassword());
  }
  

  public void sendCreateWordPressAccount(Long communityId, String wpUsername, String wpEmailAddress, String wpDisplayname) {
    /*RequestBody body = new FormBody.Builder()
        .add("username", wpUsername)
        .add("email", wpEmailAddress)
        .add("name", wpUsername)
        .add("password", conf.wordpressAccountDefaultPassword())
        .add("roles", conf.wordpressAccountDefaultAuthority())
        .add("community_id", String.valueOf(communityId))
        .build();*/
    
    WordpressRequestBody body = new WordpressRequestBody()
        .setUsername(wpUsername)
        .setEmail(wpEmailAddress)
        .setName(wpDisplayname)
        .setPassword(conf.wordpressAccountDefaultPassword())
        .setRoles(conf.wordpressAccountDefaultAuthority())
        .setCommunity_id(communityId);
    
    RequestBody requestBody = RequestBody.create(gson.toJson(body), mediaType);
    
    Request request = new Request.Builder()
        .url(url)
        .addHeader("Authorization", basicAuthorization)
        .post(requestBody)
        .build();
    
    try (Response response = execute(request)) {
      if (response.code() != 200) throw new ServerErrorException(String.format("Failed to create WordPress account. [Status=%d, Message=%s]", response.code(), response.message()));
    }
  }
}
