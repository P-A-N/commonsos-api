package commonsos.interceptor;


import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import commonsos.annotation.Synchronized;
import commonsos.repository.EntityManagerService;
import commonsos.repository.Repository;

public class RepositoryInterceptor extends AbstractModule implements MethodInterceptor {
  @Inject EntityManagerService entityManagerService;

  @Override
  protected void configure() {
    requestInjection(this);
    bindInterceptor(Matchers.subclassesOf(Repository.class), Matchers.annotatedWith(Synchronized.class), this);
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    Synchronized sync = invocation.getMethod().getAnnotation(Synchronized.class);
    if (sync != null) {
      synchronized(sync.value()) {
        return invocation.proceed();
      }
    } else {
      return invocation.proceed();
    }
  }
}