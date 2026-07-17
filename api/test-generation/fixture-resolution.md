Fixture resolution
==================

The generator looks for any classes in the module whose simple name ends with "Fixtures". Typical candidates:
- nc.sgcb.labs.account.Fixtures
- nc.sgcb.labs.account.AccountFixtures

The generator tries to resolve:
- string constants (e.g., CUSTOMER_SUBJECT)
- factory methods (e.g., createCustomersXpfAccount(long balance))

If a required symbol referenced in a generated test cannot be found:
- The generator stops and returns a prompt indicating the missing fixture symbol (e.g., "Missing: nc.sgcb.labs.account.Fixtures#CUSTOMER_SUBJECT").
- Developer options:
  1) add the missing constant/method to on of the module fixture classes
  2) instruct the generator to inline a literal value for the test
