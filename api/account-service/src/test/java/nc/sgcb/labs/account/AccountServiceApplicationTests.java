package nc.sgcb.labs.account;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(properties = {"issuer=http://localhost:8089/auth/realms/labs"})
@ActiveProfiles("h2")
@EnableWireMock({@ConfigureWireMock(port = 8089)})
class AccountServiceApplicationTests {

  @Test
  void contextLoads() {}

}
