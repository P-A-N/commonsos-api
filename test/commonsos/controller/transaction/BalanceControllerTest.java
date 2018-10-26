package commonsos.controller.transaction;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.TransactionService;
import commonsos.view.BalanceView;
import spark.Request;

@RunWith(MockitoJUnitRunner.class)
public class BalanceControllerTest {

  @Mock Request request;
  @Mock TransactionService service;
  @InjectMocks BalanceController controller;

  @Test
  public void handle() {
    when(request.queryParams("communityId")).thenReturn("123");
    User user = new User();
    when(service.balance(user, 123L)).thenReturn(new BalanceView().setBalance(TEN).setCommunityId(123L));

    assertThat(controller.handle(user, request, null).getBalance()).isEqualTo(TEN);
    assertThat(controller.handle(user, request, null).getCommunityId()).isEqualTo(123L);
  }

  @Test(expected = BadRequestException.class)
  public void handle_noCommunityId() {
    controller.handle(new User(), request, null);
  }
}