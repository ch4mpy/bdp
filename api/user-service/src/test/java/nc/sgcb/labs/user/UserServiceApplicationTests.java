package nc.sgcb.labs.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(properties = {"issuer=http://localhost:8089/auth/realms/labs"})
@EnableWireMock({@ConfigureWireMock(port = 8089)})
class CustomerServiceApplicationTests {

  @Test
  void contextLoads() {}

}
