package commonsos.controller.app.transaction;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.controller.app.transaction.BalanceController;
import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.TokenTransactionService;
import commonsos.view.app.BalanceView;
import spark.Request;

@ExtendWith(MockitoExtension.class)
public class BalanceControllerTest {

  @Mock Request request;
  @Mock TokenTransactionService service;
  @InjectMocks BalanceController controller;

  @Test
  public void handle() {
    when(request.queryParams("communityId")).thenReturn("123");
    User user = new User();
    when(service.balance(user, 123L)).thenReturn(new BalanceView().setBalance(TEN).setCommunityId(123L));

    assertThat(controller.handleAfterLogin(user, request, null).getBalance()).isEqualTo(TEN);
    assertThat(controller.handleAfterLogin(user, request, null).getCommunityId()).isEqualTo(123L);
  }

  @Test
  public void handle_noCommunityId() {
    assertThrows(BadRequestException.class, () -> controller.handleAfterLogin(new User(), request, null));
  }
}