/**
 *
 */
package nc.sgcb.labs.commons.exception;

/**
 *
 *
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
public class ResourceNotFoundException extends Exception {

  private static final long serialVersionUID = 1401428042718424402L;

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
