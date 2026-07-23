# REST hero

REST hero is a simplified online bank. It is a frontend for REST services created as part of a training to learn REST APIs with Spring.

The frontend serves different purposes depending on whether the user is a bank advisor (non-empty role list) or a customer (empty role list).

For advisors, the app purpose is to manage all customers, accounts, and cards.

For customers, the app purpose is to manage their beneficiaries, pay with their card(s), and search for money transfers to or from their account(s).


## 1. Domain model and business rules

A _customer_ manages his _beneficiaries_. The _customer_ identifier is also his user ID (OAuth2 subject). He may have several _accounts_.

A _card_ is emitted for a specific _account_, but several cards can be emitted for the same _account_.

A _payment_ is made with a _card_, from the _account_ to which this _card_ is attached (the _source_), to a _destination_ account that has an IBAN taken from the card's owner _beneficiaries_.

## 2. REST API

Because of the OAuth2 BFF pattern, all REST requests are sent through a single gateway that maintains a session for the browser and translates between session-based authorization (with protection against CSRF using an `HttpOnly=false` cookie) and Bearer-based authorization (access token in the `Authorization` header).

The API is documented with the following OpenAPI specs:

- `gateway.openapi.json`: exposes login and logout endpoints, using the OAuth2 BFF pattern. The OAuth2 client is on the server behind a reverse proxy. In production, this reverse proxy serves both the frontend assets and the BFF. The frontend requests are authorized with an http-only session cookie and a CSRF token that the frontend can read from a cookie and set as a header. The Gateway also exposes a `/me` endpoint to get the current user's info. A response with empty values means that the user is not authenticated.
- `customer-service.openapi.json`: exposes REST operations related to customers and their beneficiaries.
- `account-service.openapi.json`: exposes REST operations related to accounts and money transfers between accounts.
- `card-service.openapi.json`: exposes REST operations related to cards and payments.

Java 21 or higher is required in the dev environment, and `@openapitools/openapi-generator-cli` should be added as a dev dependency. Client libraries should be generated from the OpenAPI specs using the following "scripts" in the package.json:

```
"gateway-api:generate": "npx openapi-generator-cli generate -i ./gateway.openapi.json -g typescript-fetch --type-mappings AnyType=any --additional-properties=stringEnums=true,enumPropertyNaming=camelCase,supportsES6=true -o src/rest/gateway",
"user-api:generate": "npx openapi-generator-cli generate -i ./user-service.openapi.json -g typescript-fetch --type-mappings AnyType=any --additional-properties=stringEnums=true,enumPropertyNaming=camelCase,supportsES6=true -o src/rest/api",
"customer-api:generate": "npx openapi-generator-cli generate -i ./customer-service.openapi.json -g typescript-fetch --type-mappings AnyType=any --additional-properties=stringEnums=true,enumPropertyNaming=camelCase,supportsES6=true -o src/rest/api",
"account-api:generate": "npx openapi-generator-cli generate -i ./account-service.openapi.json -g typescript-fetch --type-mappings AnyType=any --additional-properties=stringEnums=true,enumPropertyNaming=camelCase,supportsES6=true -o src/rest/api",
"card-api:generate": "npx openapi-generator-cli generate -i ./card-service.openapi.json -g typescript-fetch --type-mappings AnyType=any --additional-properties=stringEnums=true,enumPropertyNaming=camelCase,supportsES6=true -o src/rest/api",
"api": "npm run gateway-api:generate && npm run user-api:generate && npm run customer-api:generate && npm run account-api:generate && npm run card-api:generate"
```

## 3. Application sources

The app source should be kept in the following public Github repository https://github.com/ch4mpy/labs-frontend

## 4. Login & Logout

To initiate login, the frontend calls the gateway's `startLoginWithBff` operation, reads the response `location` header, and triggers a new navigation with it (set the `window.location.href`).

To initiate logout, the frontend calls the BFF's `logout` operation, reads the response `location` header, and triggers a new navigation with it (set the `window.location.href`).

## 5. UX

### 5.1. pages

The app starts on the _"Home"_ page.

#### 5.1.1. Home page

The _"Home"_ page should include the following sections:
- details: user first and last names, and email
- accounts: list a customer's accounts
- beneficiaries: list of the customer's beneficiaries

If the user is not granted the `account.read_any` permission, the customer is always the current user.

If the user is granted the `account.read_any` permission, the _"Home"_ page header includes an auto-complete component to select a customer. The _"Home"_ page content should be refreshed each time a new customer is selected.

Each entry in the account list should allow navigation to the _"Account details"_.

If the user is granted the `customer.edit` role, a `+ Add` button should stand next to the customer selection. This button should open a customer creation dialog. After a successful customer creation, this customer should be selected on the _"Accounts"_ page.

If the user is granted the `account.create` role, a `+ Add` button should stand at the end of the account list. This button should open an account creation dialog.

If the user is the customer or if he is granted `customer.edit`, pencil and trash buttons should be displayed for each beneficiary. A `+ Add` button should also stand at the end of the beneficiaries list. The beneficiary deletion should be confirmed. The beneficiary edition dialog should be shared for creation and edition.

#### 5.1.2. Account details

The _"Account details"_ page should contain 3 sections:
- _"info"_: display IBAN, currency, and balance
- _"cards"_: list cards. Each entry should allow navigation to the _"Card details"_
- _"movements"_: display and filter `MoneyTransferResponse` resources

If the user is granted the `card.create_any` role, a `+ Add` button should stand at the end of the card list. This button should open a card creation dialog.

#### 5.1.3. Card details

The _"Card details"_ page should contain 2 sections:
- _"info"_: display IBAN, card number, ceilings, and `active` indicator. The ceilings should be editable if the user is granted `card.ceilings_edit`. The `active` indicator should be editable if the user is granted `card.status_edit`.
- _"payments"_: list and filter payments. If the user is the owner of the account to which the card is attached, a `+ Pay` button should stand after the payments page. This button should display a payment creation dialog. The `destinationIban` is to be selected among those of the customer's beneficiaries. The beneficiaries component should be shared between the _"Home"_ page and the payment creation dialog.

### 5.2. Internationalization and accessibility

The application should be available in English and French, French being the default.

The application should comply reasonably with the [WCAG 2 recommendations](https://www.w3.org/WAI/WCAG22/quickref/).