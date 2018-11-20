package commonsos.controller.transaction;

import commonsos.di.GsonProvider;
import commonsos.repository.entity.User;
import commonsos.service.TransactionService;
import commonsos.service.command.TransactionCreateCommand;

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

    controller.handleAfterLogin(user, request, null);

    verify(service).create(user, new TransactionCreateCommand().setAmount(new BigDecimal("10.2")).setBeneficiaryId(22L).setDescription("description").setAdId(33L));
  }
}