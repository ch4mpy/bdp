package nc.sgcb.labs.user;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@ConfigurationProperties(prefix = "bdp-labs.keycloak-admin-api")
@Data
public class KeycloakAdminApiProperties {

  private final URI baseUri;

  private final String realmName;

}