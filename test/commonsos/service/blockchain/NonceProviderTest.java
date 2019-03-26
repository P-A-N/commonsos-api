package commonsos.service.blockchain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NonceProviderTest {

  @Mock Web3j web3j;
  @InjectMocks @Spy NonceProvider provider;

  @Test
  public void nonceFor_readsInitialValueFromNetwork() throws IOException {
    Request request = buildResponseWith("99");

    when(web3j.ethGetTransactionCount("address", DefaultBlockParameterName.LATEST)).thenReturn(request);

    assertThat(provider.nonceFor("address")).isEqualTo(99);
  }

  @Test
  public void nonceFor_areSequential() throws IOException {
    Request request = buildResponseWith("99");

    when(web3j.ethGetTransactionCount("address", DefaultBlockParameterName.LATEST)).thenReturn(request);

    assertThat(provider.nonceFor("address")).isEqualTo(99);
    assertThat(provider.nonceFor("address")).isEqualTo(100);
    assertThat(provider.nonceFor("address")).isEqualTo(101);
  }

  @Test
  public void nonceFor_addressesHaveSeparateNonces() throws IOException {
    Request request = buildResponseWith("99");

    doReturn(request).when(web3j).ethGetTransactionCount("address1", DefaultBlockParameterName.LATEST);
    doReturn(request).when(web3j).ethGetTransactionCount("address2", DefaultBlockParameterName.LATEST);

    assertThat(provider.nonceFor("address1")).isEqualTo(99);
    assertThat(provider.nonceFor("address1")).isEqualTo(100);

    assertThat(provider.nonceFor("address2")).isEqualTo(99);
    assertThat(provider.nonceFor("address2")).isEqualTo(100);

    assertThat(provider.nonceFor("address1")).isEqualTo(101);
  }

  private Request buildResponseWith(String transactionCount) throws IOException {
    Request request = mock(Request.class);
    EthGetTransactionCount response = mock(EthGetTransactionCount.class);
    when(response.getTransactionCount()).thenReturn(new BigInteger(transactionCount));
    when(request.send()).thenReturn(response);
    return request;
  }
}