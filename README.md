## Original task

The task is to build the basic REST API for banking.

### Details

Consider using following simplified data model:
```
Account:
- accountId - unique account identifier
- balance - account balance

Transaction:
- txId - unique transacion identifier
- amount - transferred amount
- from - source account id
- to - destination account id
```

Implement following banking APIs:
- create new account with predefined balance
- fetch account balance by `accountId`
- create transaction between two accounts, that impacts account balance

## Implementation

Implemented as a Spring Boot 3 application in Kotlin, Java 17 is used as target JVM version. 
Postgres is used for persistence.

### How to build

`./gradlew build`

### How to run

```
docker build -t egrmdev/banking-service:latest .
docker-compose up -d
```

### How to use

#### Creating an account

```
curl -X POST localhost:8080/accounts -H "Content-Type: application/json" -d '{"balanceInCents": 42}'
```

Example response:

```json
{"id":"7c0afbb5-47df-424a-97ca-68c8b34b6941","balanceInCents":42}
```

#### Querying an account (and its balance)

```
curl -X GET localhost:8080/accounts/${ACCOUNT_ID}
```

Example response:

```json
{"id":"7c0afbb5-47df-424a-97ca-68c8b34b6941","balanceInCents":42}
```

#### Creating a money transfer transaction between two accounts

```
curl -X POST localhost:8080/transactions -H "Content-Type: application/json" -d '{"amountInCents": 24, "fromAccountId": "7c0afbb5-47df-424a-97ca-68c8b34b6941", "toAccountId": "1926a925-23c7-435f-8f51-0e8570d5a7ef"}'
```

Example response:
```json
{"id":"0d625af9-e913-4e3f-a71e-d6a631d4e023","amountInCents":24,"fromAccountId":"7c0afbb5-47df-424a-97ca-68c8b34b6941","toAccountId":"1926a925-23c7-435f-8f51-0e8570d5a7ef"}
```

### Details

The implementation tries to follow domain-driven design, and port and adapters (aka the hexagonal)
architecture.

#### Design decisions

- `Long` representing cents is used to pass balance and transfer amounts and for persistence. 
An alternative would be `BigDecimal`, which would have to be rounded to ensure precision of 2 points, 
e.g, with `BigDecimal#setScale(2, RoundingMode.HALF_EVEN)`. More complex calculations resulting 
in numbers with fractions would still be carried out in runtime by using `BigDecimal`, which would 
then be converted to cents and persisted.
- Atomicity of account updates in case of concurrent transfers is handled by setting transaction 
isolation level to repeatable read. Alternatives would be optimistic and pessimistic locking.
- No dedicated endpoint for fetching account's balance because in the current simplified model
it wouldn't be RESTful.
- Spring's default error responses for cases like a non-existent endpoint path weren't customised.
