package commonsos.di;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import commonsos.Configuration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;

@Slf4j @Singleton
public class Web3jProvider implements Provider<Web3j>{

  @Inject Configuration configuration;

  private Web3j instance;

  @Inject void init() {
    HttpService httpService = new HttpService(configuration.ethererumUrl());
    String basicAuthorization = Credentials.basic(
        configuration.ethererumBasicAuthorizationUsername(),
        configuration.ethererumBasicAuthorizationPassword());
    httpService.addHeader("Authorization", basicAuthorization);
    this.instance = Web3j.build(httpService);
  }

  @Override public Web3j get() {
    log.info("Web3jProvider get " + instance);
    return instance;
  }
}
