package nc.sgcb.labs.commons.domain;

public enum Currency {
  EUR((short) 2),
  AUD((short) 2),
  BRL((short) 2),
  CAD((short) 2),
  CHF((short) 2),
  CNY((short) 2),
  CZK((short) 2),
  DKK((short) 2),
  GBP((short) 2),
  HKD((short) 2),
  HUF((short) 2),
  IDR((short) 2),
  ILS((short) 2),
  INR((short) 2),
  ISK((short) 0),
  JPY((short) 0),
  KRW((short) 0),
  MXN((short) 2),
  MYR((short) 2),
  NOK((short) 2),
  NZD((short) 2),
  PHP((short) 2),
  PLN((short) 2),
  RON((short) 2),
  SEK((short) 2),
  SGD((short) 2),
  THB((short) 2),
  TRY((short) 2),
  USD((short) 2),
  XPF((short) 0),
  ZAR((short) 2);

  public final short decimals;
  
  Currency(short decimals) {
    this.decimals = decimals;
  }
}