package commonsos;

public class ThreadValue {
  
  private ThreadValue() {}
  
  private static ThreadLocal<String> requestedBy = ThreadLocal.withInitial(() -> "ANONYMOUS");
  
  public static String getRequestedBy() {
    return requestedBy.get();
  }
  
  public static void setRequestedBy(String value) {
    requestedBy.set(value);
  }
}
