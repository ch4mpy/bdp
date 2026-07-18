package nc.sgcb.labs.customer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
@Import(MockedOAuth2ClientTestConfiguration.class)
class CustomerServiceApplicationTests {

  @Test
  void contextLoads() {}

}
