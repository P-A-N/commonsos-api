package commonsos.service.httprequest;

import java.io.IOException;

import javax.inject.Inject;

import commonsos.exception.ServerErrorException;
import commonsos.service.AbstractService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public abstract class AbstractHttpRequestService extends AbstractService {
  
  private OkHttpClient okHttpClient;
  
  @Inject
  private void init() {
    okHttpClient = new OkHttpClient.Builder()
        .build();
  }
  
  protected Response execute(Request request) {
    try {
      log.info(String.format("creating http request. [%s %s]", request.method(), request.url()));
      Response response = okHttpClient.newCall(request).execute();
      
      log.info(String.format("http request finish. [%s %s, HTTP Status=%d]", request.method(), request.url(), response.code()));
      return response;
    } catch (IOException e) {
      log.warn(String.format("http request failed. [%s %s]", request.method(), request.url()));
      throw new ServerErrorException(e);
    }
  }
}
