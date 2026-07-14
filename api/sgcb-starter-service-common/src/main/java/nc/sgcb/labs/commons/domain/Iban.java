package nc.sgcb.labs.commons.domain;

import java.io.Serializable;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Iban implements Serializable {
  private static final long serialVersionUID = 2149749066306012403L;
  public static final String IBAN_PATTERN_STRING = "^([A-Z]{2})([\\d]{2})([\\w]{1,30})$";
  @SuppressWarnings("null")
  private static final Pattern IBAN_PATTERN = Pattern.compile(IBAN_PATTERN_STRING);

  private final String countryCode;
  private final String checkDigits;
  private final String bban;

  public Iban(Iban other) {
    this(other.countryCode, other.checkDigits, other.bban);
  }

  public Iban(String iban) {
    this(Iban.parse(iban));
  }

  @SuppressWarnings("null")
  public String toHumanReadableString() {
    return "%s%s %s".formatted(countryCode, checkDigits, bban);
  }

  public String toMachineReadableString() {
    return Iban.toMachineReadableString(toHumanReadableString());
  }

  @Override
  public String toString() {
    return toHumanReadableString();
  }

  @SuppressWarnings("null")
  private static String toMachineReadableString(String humanReadable) {
    return humanReadable.replaceAll("\\s", "").toUpperCase();
  }

  /**
   * @param ibanStr IBAN in "human" or "machine" readable format
   * @return
   * @throws NotAnIbanException if the ibanStr is null or doesn't look like an IBAN
   */
  @SuppressWarnings("null")
  public static Iban parse(String ibanStr) throws NotAnIbanException {
    final var machineReable = toMachineReadableString(ibanStr);
    final var matcher = IBAN_PATTERN.matcher(machineReable);
    if (!matcher.matches()) {
      throw new NotAnIbanException(ibanStr);
    }
    return new Iban(matcher.group(1), matcher.group(2), matcher.group(3));
  }

  public static class NotAnIbanException extends RuntimeException {
    private static final long serialVersionUID = -4142706446224605996L;

    public NotAnIbanException(String ibanStr) {
      super("%s doesn't look like a valid IBAN".formatted(ibanStr));
    }
  }
}
