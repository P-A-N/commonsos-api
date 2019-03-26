package commonsos.controller.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.exception.BadRequestException;
import commonsos.repository.entity.User;
import commonsos.service.TransactionService;
import commonsos.view.TransactionView;
import spark.Request;

@ExtendWith(MockitoExtension.class)
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
    assertThat(controller.handleAfterLogin(user, request, null)).isSameAs(transactions);
  }

  @Test
  public void handle_noCommunityId() {
    assertThrows(BadRequestException.class, () -> controller.handleAfterLogin(new User(), request, null));
  }
}