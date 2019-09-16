package commonsos.interceptor;


import java.lang.reflect.Method;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;

import commonsos.Configuration;
import commonsos.ThreadValue;
import commonsos.annotation.ReadOnly;
import commonsos.annotation.RestrictAccess;
import commonsos.annotation.Synchronized;
import commonsos.exception.AuthenticationException;
import commonsos.repository.EntityManagerService;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Route;

@Slf4j
public class ControllerInterceptor extends AbstractModule implements MethodInterceptor {
  @Inject EntityManagerService entityManagerService;
  @Inject Configuration config;

  @Override
  protected void configure() {
    requestInjection(this);
    bindInterceptor(Matchers.subclassesOf(Route.class), new HandleMethodMatcher(), this);
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    try {
      // check access
      Class<?> handleClass = invocation.getThis().getClass().getSuperclass();
      RestrictAccess restrictAccess = handleClass.getAnnotation(RestrictAccess.class);
      if (restrictAccess != null) {
        checkAccess(invocation, restrictAccess);
      }
      
      // setup sync
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
  
  private void checkAccess(MethodInvocation invocation, RestrictAccess restrictAccess) {
    Object[] args = invocation.getArguments();
    Request request = null;
    for (Object arg : args) {
      if (arg instanceof Request) {
        request = (Request) arg;
        break;
      }
    }
    if (request == null) return;
    
    String AllowIps = config.environmentVariable(restrictAccess.allow().getConfigurationKey(), "");
    if (AllowIps == null) return;
    
    String accessIp = request.ip();
    for (String allowIp : AllowIps.split(",")) {
      if (allowIp.equals(accessIp)) return;
    }
    
    throw new AuthenticationException(String.format("Access from disallowed IP. IP=%s", accessIp));
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