/**
 * 
 */
package nc.sgcb.labs.commons.exception;

/**
 * 
 *
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
public class InternalServerErrorException extends Exception {

  private static final long serialVersionUID = -2730634763223830115L;

  public InternalServerErrorException(String message) {
    super(message);
  }
}
