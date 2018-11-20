package commonsos.interceptor;


import java.lang.reflect.Method;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;

import commonsos.ThreadValue;
import commonsos.annotation.ReadOnly;
import commonsos.annotation.Synchronized;
import commonsos.repository.EntityManagerService;
import lombok.extern.slf4j.Slf4j;
import spark.Route;

@Slf4j
public class TransactionInterceptor extends AbstractModule implements MethodInterceptor {
  @Inject EntityManagerService entityManagerService;

  @Override
  protected void configure() {
    requestInjection(this);
    bindInterceptor(Matchers.subclassesOf(Route.class), new HandleMethodMatcher(), this);
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    try {
      // setup sync
      Class<?> handleClass = invocation.getThis().getClass().getSuperclass();
      Synchronized sync = handleClass.getAnnotation(Synchronized.class);
      
      if (sync != null) {
        synchronized(sync.value()) {
          return proceedMethod(invocation);
        }
      } else {
        return proceedMethod(invocation);
      }
      
    } finally {
      close(entityManagerService);
    }
  }
  
  private Object proceedMethod(MethodInvocation invocation) throws Throwable {
    Class<?> handleClass = invocation.getThis().getClass().getSuperclass();

    // setup read-only
    ThreadValue.setReadOnly(handleClass.isAnnotationPresent(ReadOnly.class));
    return entityManagerService.runInTransaction(invocation::proceed);
  }

  private void close(EntityManagerService entityManagerService) {
    try {
      entityManagerService.close();
    }
    catch (Throwable e) {
      log.error("Failed to close entity manager:", e);
    }
  }

  class HandleMethodMatcher extends AbstractMatcher<Method> {
    @Override
    public boolean matches(Method method) {
      return "handle".equals(method.getName()) && !method.isSynthetic();
    }
  }
}