/**
 * 
 */
package nc.sgcb.labs.user.web;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
public record UserRolesRequest(@NotNull List<@NotEmpty String> roleLabels) {
}
