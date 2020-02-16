package commonsos.util;

import commonsos.exception.ServerErrorException;

public class ThreadUtil {
  
  private ThreadUtil() {}

  public static void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new ServerErrorException(e);
    }
  }
}
