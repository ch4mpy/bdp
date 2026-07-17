Test generation guidelines — REST controller unit tests
=====================================================

Summary
-------
  - Tests MUST be generated from controller source (mappings, method signatures, @PreAuthorize, DTO validation).
  - Generated tests must follow the style:
    - `@WebMvcTest(properties = {"logging.level.org.springframework=DEBUG"})`
    - `@Import({<mapperImpls...>, SpringDataWebConvertersTestConfiguration.class, SecurityConfig.class})`
    - `@AutoConfigureAddonsWebmvcResourceServerSecurity`
    - Use `@MockitoBean` for injected beans (repositories, services, external clients).
    - Autowire `MockMvc` and `tools.jackson.databind.ObjectMapper` (as `json`).


Principles (how the generator should behave)
--------------------------------------------
1) Source-driven extraction (no separate YAML describing success/failure).
   - Inspect controller class for `@RequestMapping` / `@GetMapping` / `@PostMapping` / etc.
   - Resolve path variables and request params from method signature and mapping annotations.
   - Determine request body DTO type from parameter annotated `@RequestBody` (commonly a Java record).
   - Determine expected success status:
     - If method returns `ResponseEntity` with explicit status in code: use that.
     - Else if method or response type has `@ResponseStatus`: use that.
     - Else use fallback configured in the `generator.properties` file
2) Security:
   - Read the exact `@PreAuthorize` expression from the controller method or class.
   - Create the set of security tests:
     - anonymous user (use `@WithAnonymousUser`) expecting unauthorized if expression denies anonymous or if the endpoint is not matched by the expressions in the `com.c4-soft.springaddons.oidc.resourceserver.permit-all` property (see `application.properties`, `application.yml`, and test properties overrides)
     - persona tokens (use existing `src/test/resources` JWT files and `@WithJwt("file.json")`) —
       generator must prefer existing JWT files; mapping of spring authority -> jwt filename is in `jwt-mapping.json`.
   - Important: your project uses Keycloak client roles inside `resource_access` (not realm roles). Tests must reuse the existing JWT files (which already contain roles where necessary).
3) Validation:
   - Inspect jakarta.validation annotations on DTO record components / fields:
     - `@NotNull`, `@NotBlank`, `@Size`, `@Pattern`, `@Min`, `@Max`, etc.
   - For each constraint, generate at least one failing test that submits an invalid payload and asserts a 4xx response.
   - Also generate a successful test using valid values taken from `Fixtures` where applicable (see Fixtures usage below).
4) Fixtures & test data:
   - Prefer values from module `Fixtures` classes. If a required constant/method is missing, generator should pause and prompt the developer to either:
     - add the required constant/method to `Fixtures`, or
     - supply a literal test value in a local helper file.
   - Example: use `Fixtures.CUSTOMER_SUBJECT` for `customerId` values, `Fixtures.createCustomersXpfAccount(...)` to derive expected IBANs.
5) Test naming convention
   - Tests are named using BDD (Behavior Driven Development) conventions (givenX_whenY_thenZ):
     - then "given" statement provides the security context and any other relevant context
     - the "when" statement describes the REST call
     - the "then" statement describes the expectations
6) Test structure & helper annotations:
   - Tests must use:
     - `@WebMvcTest(properties = {"logging.level.org.springframework=DEBUG"})`
     - `@Import({ <controller-specific mapper implementations>, SpringDataWebConvertersTestConfiguration.class, SecurityConfig.class })`
     - `@AutoConfigureAddonsWebmvcResourceServerSecurity`
   - Beans that the controller depends on (services, repositories, external clients) must be declared as `@MockitoBean`.
   - Use `MockMvc` and `ObjectMapper` autowired in tests, exactly as in `AccountControllerTest`.
   - Use `tools.jackson.databind.ObjectMapper` as in existing tests.
7) Generated assertions:
   - For success cases: parse response body with `ObjectMapper` into the controller's response DTO (or into `TypeReference` if a list).
   - Use `assertThat` and `assertTrue` assertions in the same style as existing tests.
   - When controller returns created resource, assert `Location` header presence if applicable.

Files & locations
-----------------
As minimal artifacts to help generation, create the files listed below. The generator assumes controllers live in `src/main/java` and tests are written to `src/test/java` mirroring packages.

- `test-generation/generator.properties`
- `test-generation/test-template.mustache`
- `test-generation/fixture-resolution.md`
- `test-generation/jwt-mapping.json`
- `test-generation/README.md` (this file)

Notes about DTO conversion and invalid payloads
-----------------------------------------------
1) Instantiate DTOs (preferred) — why and how
  - Generated tests MUST instantiate the request DTO Java objects (records or classes) and serialize them with the autowired
    ObjectMapper before sending to MockMvc. This produces compile-time safe tests that refactor correctly when DTOs change.
  - Example (AccountCreationRequest is a record):

      var dto = new AccountCreationRequest(
          Fixtures.createCustomersXpfAccount(100000L).getIban().toMachineReadableString(),
          Fixtures.CUSTOMER_SUBJECT,
          "XPF"
      );

      mockMvc.perform(post("https://localhost" + AccountController.BASE_PATH)
              .contentType(MediaType.APPLICATION_JSON)
              .content(json.writeValueAsString(dto)))
          .andExpect(status().isCreated());

  - Implementation rules for the generator when instantiating DTOs:
    - For each record component / field choose a value using this precedence:
      1. Module `Fixtures` constants or factory methods (preferred).
      2. Standard defaults based on the Java type (String -> "validString", long/int -> 1, enum -> first constant).
      3. Prompt developer when no reasonable default can be chosen (e.g. custom complex types without fixtures).
    - For fields with validation annotations (`@NotNull`, `@NotBlank`, `@Pattern`, `@Min`, etc.) the generator must create both a valid DTO instance and one or more invalid DTO instances exercising failing constraints.
    - For custom validation annotations (e.g. `@IbanString`) prefer values supplied by `Fixtures` (e.g. a factory creating an Account with a valid IBAN). If none exist, prompt the developer to provide a suitable fixture or a literal example value.

2) When generating JSON strings (fallback)
  - The generator may produce raw JSON strings only as a fallback if the DTO type cannot be resolved or instantiated safely (for example: DTO class not on source path, private constructors, or complex nested types without fixtures).
  - Fallback JSON should be limited to cases where creating the DTO in Java is impossible; otherwise prefer DTO instantiation and ObjectMapper serialization.

3) Advantages of instantiating DTOs in tests
  - Compile-time safety: the test will not silently break when DTO fields are renamed.
  - Easier reuse of `Fixtures` and factory methods.
  - Cleaner invalid-case generation: create an otherwise-valid DTO then tweak one field to an invalid value.


When to prompt the developer
----------------------------
  - A required Fixture constant or factory method is missing.
  - A `@PreAuthorize` expression uses custom helper beans/functions that static analysis cannot resolve (e.g. `@PreAuthorize("@customers.check(#dto.customerId)")`).
  - A MapStruct mapper implementation cannot be found but the controller uses a mapper type.
  - Any ambiguous success status that cannot be determined from code.
