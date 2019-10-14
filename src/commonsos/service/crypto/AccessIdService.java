package commonsos.service.crypto;

import java.util.function.Predicate;

import javax.inject.Singleton;

import org.apache.commons.lang3.RandomStringUtils;

import commonsos.exception.ServerErrorException;
import commonsos.service.AbstractService;

@Singleton
public class AccessIdService extends AbstractService {

  private static final int MAX_CHALLENGE_COUNT = 10;
  private static final int ID_LENGTH = 48;
  
  public String generateAccessId(Predicate<String> predicate) {
    for (int i = 0; i < MAX_CHALLENGE_COUNT; i++) {
      String accessId = RandomStringUtils.randomAlphanumeric(ID_LENGTH);
      if (predicate.test(accessId)) {
        return accessId;
      }
    }
    
    throw new ServerErrorException("generate accessId failed");
  }
}
