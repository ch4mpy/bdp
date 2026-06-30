/**
 * 
 */
package com.c4soft.bdp.labs;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@ConfigurationProperties(prefix = "bdp-labs.keycloak-admin-api")
@Data
public class KeycloakAdminApiProperties {

  private final URI baseUri;

  private final String realmName;

}
