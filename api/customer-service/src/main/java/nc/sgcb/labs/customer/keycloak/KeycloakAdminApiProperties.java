package nc.sgcb.labs.customer.keycloak;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@ConfigurationProperties(prefix = "keycloak-admin-api")
@Data
public class KeycloakAdminApiProperties {

  private final URI baseUri;

  private final String realmName;
}
