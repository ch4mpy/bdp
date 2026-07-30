/**
 *
 */
package nc.sgcb.labs.customer;

import nc.sgcb.labs.commons.jpa.IbanStringAttributeConverter;
import nc.sgcb.labs.customer.domain.Customer;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Configuration
@EnableTransactionManagement
public class PersistenceConfiguration {
}
