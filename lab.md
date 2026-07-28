# TP : Validation des entrées

Les tests unitaires des services ne passent pas.
- identifier pourquoi et à quel moment les appels lèvent une exception.
- proposer une solution pour que le code du controller ne soit pas exécuté en cas d'IBAN mal formé.
- implémenter la solution dans `sgcb-starter-service-common` et mettre à jour :
  * `AccountCreationRequest`
  * `MoneyTransferFilterRequest`
  * `MoneyTransferRequest`
  * `CardPaymentCreationRequest`
  * `CardRequest`
  * `BeneficiaryRequest`