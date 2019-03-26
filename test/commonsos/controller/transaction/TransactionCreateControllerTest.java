package commonsos.controller.transaction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import commonsos.di.GsonProvider;
import commonsos.repository.entity.User;
import commonsos.service.TransactionService;
import commonsos.service.command.TransactionCreateCommand;
import spark.Request;

@ExtendWith(MockitoExtension.class)
public class TransactionCreateControllerTest {

  @InjectMocks  TransactionCreateController controller;
  @Mock TransactionService service;

  @BeforeEach
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