package commonsos;

public class ThreadValue {
  private static ThreadLocal<Boolean> readOnly = ThreadLocal.withInitial(() -> Boolean.TRUE);
  
  public static void setReadOnly(boolean value) {
    readOnly.set(value);
  }
  
  public static boolean isReadOnly() {
    return readOnly.get();
  }
}
