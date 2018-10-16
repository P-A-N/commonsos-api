package commonsos.controller.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import commonsos.exception.BadRequestException;
import commonsos.repository.user.User;
import commonsos.service.transaction.TransactionService;
import commonsos.service.transaction.TransactionView;
import spark.Request;

@RunWith(MockitoJUnitRunner.class)
public class TransactionListControllerTest {

  @Mock Request request;
  @Mock TransactionService service;
  @InjectMocks TransactionListController controller;

  @Test
  public void handle() {
    when(request.queryParams("communityId")).thenReturn("123");
    ArrayList<TransactionView> transactions = new ArrayList<>();
    User user = new User();
    when(service.transactions(user, 123L)).thenReturn(transactions);
    assertThat(controller.handle(user, request, null)).isSameAs(transactions);
  }

  @Test(expected = BadRequestException.class)
  public void handle_noCommunityId() {
    controller.handle(new User(), request, null);
  }
}