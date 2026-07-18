package nc.sgcb.labs.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(MockedOAuth2ClientTestConfiguration.class)
class CustomerServiceApplicationTests {

  @Test
  void contextLoads() {}

}
