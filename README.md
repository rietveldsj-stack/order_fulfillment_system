# Order Fulfillment System

An event-driven order processing pipeline built with Spring Boot and RabbitMQ. An order
moves through inventory reservation, payment and shipment as a chain of asynchronous
events rather than one long synchronous transaction — and when payment fails, the
inventory reservation is compensated back.

Built as a hands-on project to work through the saga pattern, message-driven service
decoupling and failure handling in a distributed workflow.

## Why the saga pattern

Reserving stock, charging a card and creating a shipment cannot sit inside a single
database transaction — each step is a separate concern, and in a real system each would
be a separate service with its own database. A saga replaces the rollback you no longer
have with an explicit **compensating action**: if payment fails after stock was already
reserved, the inventory service listens for that failure and puts the stock back.

## The flow

```
POST /api/order
      |
      v
  OrderService ──── OrderPlacedEvent ────► InventoryService
                                                 |
                       ┌─────────────────────────┴───────────────────────┐
                       v                                                 v
              InventoryReservedEvent                        InventoryOutOfStockEvent
                       |                                          (order: OUT_OF_STOCK)
                       v
                 PaymentService
                       |
        ┌──────────────┴───────────────┐
        v                              v
 PaymentCompletedEvent          PaymentFailedEvent
        |                              |
        v                              v
  ShipmentService              InventoryService
  (order: SHIPPED)             restores stock,
                               (order: CANCELLED)   ◄── compensating transaction
```

Every hop goes through a `DirectExchange` with its own queue and routing key. Each
service is a `@RabbitListener` that knows nothing about the service before or after it.

### Order status lifecycle

`ORDER_PLACED` → `RESERVED` → `PAYMENT_COMPLETED` → `SHIPPED`

Failure branches: `OUT_OF_STOCK` (no stock to reserve) and `PAYMENT_FAILED` → `CANCELLED`
(stock returned by the compensating handler).

## Reliability

- **Dead letter queue.** Every business queue is declared with `x-dead-letter-exchange`
  pointing at a dedicated dead letter exchange, so a message that cannot be processed
  ends up somewhere inspectable instead of vanishing.
- **Bounded retries.** The listener container uses a stateless retry interceptor with
  `maxRetries(3)` and a `RejectAndDontRequeueRecoverer` — a poison message is retried a
  few times, then rejected to the DLQ rather than requeued forever.
- **Transactional consumers.** The inventory handlers are `@Transactional`, so the stock
  update and the order status change commit or fail together.

## Features

- JWT authentication — registration and login, stateless sessions
- Role-based customers, with orders scoped to the authenticated principal
- Order creation with line items; each line snapshots `priceAtPurchase`, so an order
  total stays correct when the catalogue price changes later
- Stock reservation with an all-or-nothing availability check
- Simulated payment authorisation (80% success rate) to exercise both saga branches
- Shipment records created on successful payment
- Centralised error handling via `@RestControllerAdvice` with a consistent error response

## Tech stack

| | |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, AMQP) |
| Messaging | RabbitMQ (Spring AMQP, Jackson JSON message converter) |
| Database | MySQL |
| Auth | JJWT |
| Build | Maven |
| Other | Lombok |

## API

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | — | Register a customer |
| `POST` | `/api/auth/login` | — | Log in, returns a JWT |
| `POST` | `/api/order` | JWT | Place an order, kicks off the saga |
| `GET` | `/api/order` | JWT | List the authenticated customer's orders |
| `GET` | `/api/order/{orderId}` | JWT | Fetch a single order |

Everything except `/api/auth/**` requires a `Bearer` token.

## Running it

**Prerequisites:** Java 17, Maven, MySQL, and a RabbitMQ broker.

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Copy the example config and fill in your own values:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

```bash
./mvnw spring-boot:run
```

The RabbitMQ management UI is at `http://localhost:15672` (guest/guest) — useful for
watching messages move through the queues and for inspecting the dead letter queue.

## Status and next steps

This is a learning project, not production software. Known gaps I'd address next:

- Test coverage is a context-load smoke test only; the saga branches deserve integration
  tests with an embedded or Testcontainers broker
- Payment is simulated rather than integrated with a real provider
- Consumers are not idempotent — a redelivered message would double-apply its effect
- The services are packages inside one deployable, not separately deployed services
