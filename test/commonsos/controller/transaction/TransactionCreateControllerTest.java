package commonsos.controller.transaction;

import commonsos.GsonProvider;
import commonsos.repository.user.User;
import commonsos.service.transaction.TransactionCreateCommand;
import commonsos.service.transaction.TransactionService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionCreateControllerTest {

  @InjectMocks  TransactionCreateController controller;
  @Mock TransactionService service;

  @Before
  public void setUp() {
    controller.gson = new GsonProvider().get();
  }

  @Test
  public void handle() {
    User user = new User();
    Request request = mock(Request.class);
    when(request.body()).thenReturn("{\"amount\": 10.2, \"beneficiaryId\": \"22\", \"description\": \"description\", \"adId\": \"33\" }");

    controller.handle(user, request, null);

    verify(service).create(user, new TransactionCreateCommand().setAmount(new BigDecimal("10.2")).setBeneficiaryId(22L).setDescription("description").setAdId(33L));
  }
}