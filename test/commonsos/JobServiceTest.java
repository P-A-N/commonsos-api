package commonsos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import com.google.inject.Injector;

import commonsos.repository.entity.User;

@ExtendWith(MockitoExtension.class)
public class JobServiceTest {

  @InjectMocks JobService service;
  @Mock ThreadPoolExecutor executor;
  @Mock Injector injector;

  @Test
  public void submit() {
    User user = new User().setUsername("john");
    Runnable task = mock(Runnable.class);
    when(executor.submit(any(Runnable.class))).thenAnswer(invocation -> {
      ((Runnable) invocation.getArgument(0)).run();
      return null;
    });

    service.submit(user, task);

    assertThat(MDC.get("username")).isEqualTo("john");
    verify(task).run();
    verify(injector).injectMembers(task);
  }
}