# Kafka Microservices Project --- Order Processing System

An event-driven Order Processing System built with **Spring Boot
Microservices, Apache Kafka, and MySQL**.

The project demonstrates asynchronous communication between independent
microservices, Kafka producers and consumers, consumer groups,
partitions, message ordering using `orderId` as the Kafka key, consumer
scaling, failure/recovery, duplicate-event protection through
idempotency, retry handling, and Dead Letter Topic (DLT) processing.

------------------------------------------------------------------------

## 1. Project Overview

The system contains four independent Spring Boot microservices:

-   **Order Service**
-   **Payment Service**
-   **Delivery Service**
-   **Notification Service**

Kafka is used as the asynchronous communication layer between these
services.

### End-to-End Business Flow

``` text
Customer
   |
   | POST /orders
   v
Order Service :8080
   |
   | ORDER_CREATED
   v
Kafka: order-created
   |                         \
   v                          v
Payment Service          Notification Service
   |
   | PAYMENT_SUCCESS / PAYMENT_FAILED
   v
Kafka
   |
   | PAYMENT_SUCCESS
   v
Delivery Service
   |
   | DELIVERY_CREATED
   v
Kafka: delivery-created
   |
   v
Notification Service
```

If payment fails, Delivery Service is **not triggered**.

------------------------------------------------------------------------

## 2. Services

  ------------------------------------------------------------------------------------------------
  Service                                           Port Consumes             Produces
  -------------------------------- --------------------- -------------------- --------------------
  `project-order-service`                         `8080` ---                  `order-created`

  `project-payment-service`                       `8085` `order-created`      `payment-success`,
                                                                              `payment-failed`

  `project-delivery-service`                      `8082` `payment-success`    `delivery-created`

  `project-notification-service`                  `8083` `order-created`,     ---
                                                         `payment-success`,   
                                                         `payment-failed`,    
                                                         `delivery-created`   
  ------------------------------------------------------------------------------------------------

### Service responsibilities

**Order Service** - Accepts order requests. - Stores orders in MySQL. -
Creates an `ORDER_CREATED` Kafka event. - Uses `orderId` as the Kafka
message key.

**Payment Service** - Consumes `ORDER_CREATED`. - Checks for duplicate
source events before processing. - Simulates payment success/failure. -
Stores payment information in MySQL. - Publishes either
`PAYMENT_SUCCESS` or `PAYMENT_FAILED`.

**Delivery Service** - Consumes successful payment events. - Creates and
stores delivery information. - Generates a tracking number. - Publishes
`DELIVERY_CREATED`.

**Notification Service** - Consumes all relevant business events. -
Generates simulated SMS-style notification output. - Uses retry and DLT
handling for processing failures.

------------------------------------------------------------------------

## 3. Technology Stack

-   Java 21
-   Spring Boot
-   Spring Data JPA / Hibernate
-   Apache Kafka
-   ZooKeeper-based Kafka environment used in the course/demo setup
-   MySQL
-   Maven / Maven Wrapper
-   Postman
-   Docker Desktop
-   Eclipse IDE

------------------------------------------------------------------------

## 4. Project Structure

``` text
Kafka Project
|
├── project-order-service
│   └── src/main/java/com/flipkart/order
│       ├── controller
│       ├── entity
│       ├── repository
│       ├── request
│       ├── response
│       └── service
│           └── kafka
|
├── project-payment-service
│   └── src/main/java/com/flipkart/payment
│       ├── consume
│       ├── entity
│       ├── repository
│       ├── response
│       └── service
│           └── kafka
|
├── project-delivery-service
│   └── src/main/java/com/flipkart/delivery
│       ├── consume
│       ├── entity
│       ├── repository
│       ├── response
│       └── service
│           └── kafka
|
├── project-notification-service
│   └── src/main/java/com/flipkart/notification
│       ├── consume
│       ├── response
│       └── service
|
├── Postman
├── Demo
└── README.md
```

------------------------------------------------------------------------

## 5. Infrastructure

The demonstrated environment uses:

``` text
ZooKeeper
Kafka
MySQL
```

Kafka runs on:

``` text
localhost:9092
```

The Kafka and ZooKeeper services are run using Docker Desktop.

The MySQL database used by the project is:

``` text
ecom_kafka_project
```

------------------------------------------------------------------------

## 6. Kafka Topics

### Application Topics

  Topic                Purpose
  -------------------- -----------------------------------
  `order-created`      Carries `ORDER_CREATED` events
  `payment-success`    Carries successful payment events
  `payment-failed`     Carries failed payment events
  `delivery-created`   Carries `DELIVERY_CREATED` events

### DLT

The demonstrated DLT is:

``` text
payment-success.DLT
```

Retry/DLT infrastructure topics may also be created automatically by
Spring Kafka's `@RetryableTopic`.

------------------------------------------------------------------------

## 7. Consumer Groups

The project uses separate consumer groups so services can independently
consume the same business event.

``` text
payment-service-group
delivery-service-group
notification-service-group
```

### Payment Service

``` text
Topic: order-created
Group: payment-service-group
```

### Delivery Service

``` text
Topic: payment-success
Group: delivery-service-group
```

### Notification Service

``` text
Topics:
- order-created
- payment-success
- payment-failed
- delivery-created

Group:
notification-service-group
```

Because Payment and Notification use different consumer groups, both can
independently consume the same `ORDER_CREATED` event.

------------------------------------------------------------------------

## 8. Order Service

### Responsibilities

1.  Receives an order request.
2.  Saves the order using JPA/Hibernate.
3.  Creates a Kafka event.
4.  Sets:

``` text
eventType = ORDER_CREATED
```

5.  Generates an event ID in the form:

``` text
EVT-xxxxx
```

6.  Publishes the event to `order-created`.

### Kafka Message Key

The Order Service uses:

``` java
String key = String.valueOf(responseEntity.getOrderId());
```

Therefore:

``` text
orderId → Kafka message key
```

This is used for the partitioning and ordering demonstration.

### Endpoint

``` http
POST http://localhost:8080/orders
```

Example request:

``` json
{
  "customerId": 101,
  "customerName": "Rahul",
  "productId": 501,
  "productName": "Laptop",
  "quantity": 1,
  "amount": 75000,
  "deliveryAddress": "Bangalore"
}
```

------------------------------------------------------------------------

## 9. Payment Service

### Responsibilities

1.  Consumes `ORDER_CREATED`.
2.  Checks whether the source event has already been processed.
3.  Simulates payment processing.
4.  Stores the payment result.
5.  Publishes either success or failure.

### Payment Simulation

The current implementation uses:

``` java
boolean isSuccess = random.nextInt(100) < 80;
```

Meaning:

``` text
0–79  → payment success
80–99 → payment failure
```

So the demo simulates an approximately:

``` text
80% success
20% failure
```

payment outcome.

### Payment Events

Successful payment:

``` text
eventType = PAYMENT_SUCCESS
paymentStatus = SUCCESS
```

Failed payment:

``` text
eventType = PAYMENT_FAILED
paymentStatus = FAILED
reason = INSUFFICIENT_FUNDS
```

### Generated IDs

Payment event:

``` text
PAY-xxxxx
```

Payment transaction:

``` text
TXN-xxxxx
```

### Kafka Key

Payment messages are also published using:

``` text
orderId
```

as the Kafka key.

------------------------------------------------------------------------

## 10. Payment Consumer

`KafkaPaymentConsumer` consumes:

``` text
order-created
```

using:

``` text
payment-service-group
```

The consumer receives the Kafka message as:

``` text
ConsumerRecord<String, String>
```

`ObjectMapper` converts the JSON string into:

``` text
KafkaOrderResponse
```

The response is then passed to:

``` text
PaymentService.processPayment()
```

The consumer keeps Kafka-consumption logic separate from payment
business logic.

------------------------------------------------------------------------

## 11. Duplicate Message and Idempotency

Payment Service implements database-backed idempotency.

For every incoming `ORDER_CREATED` event, the original event ID is used
as the source identifier.

Before processing:

``` text
existsBySourceId(response.getEventId())
```

is checked.

### First delivery

``` text
EVT-xxxxx
   |
   v
Check database
   |
   v
Not found
   |
   v
Process payment
   |
   v
Save payment record
```

### Duplicate delivery

``` text
EVT-xxxxx
   |
   v
Check database
   |
   v
Already exists
   |
   v
Ignore duplicate
```

During testing, the same source event was replayed and the service
produced:

``` text
Duplicate event recievedEVT-68126
```

without creating another payment record.

------------------------------------------------------------------------

## 12. Delivery Service

Delivery Service consumes:

``` text
payment-success
```

using:

``` text
delivery-service-group
```

### Responsibilities

-   Creates a delivery event.
-   Generates a delivery event ID:

``` text
DEL-xxxxx
```

-   Generates a tracking number:

``` text
TRK-xxxxx
```

-   Determines a simulated delivery status.
-   Stores delivery information in MySQL.
-   Publishes `DELIVERY_CREATED`.

The delivery message uses `orderId` as the Kafka key.

### Important Business Rule

Delivery Service consumes only:

``` text
payment-success
```

Therefore:

``` text
PAYMENT_FAILED
      ↓
Delivery Service is not triggered
```

------------------------------------------------------------------------

## 13. Delivery Consumer

`KafkaDeliveryConsume` consumes:

``` text
payment-success
```

using:

``` text
delivery-service-group
```

The Kafka JSON string is converted using `ObjectMapper` into:

``` text
KafkaPaymentResponse
```

Then:

``` text
DeliveryService.createDelivery()
```

is called.

------------------------------------------------------------------------

## 14. Notification Service

Notification Service consumes four business events:

``` text
ORDER_CREATED
PAYMENT_SUCCESS
PAYMENT_FAILED
DELIVERY_CREATED
```

Each event has its own Kafka listener method.

The listeners use:

``` text
notification-service-group
```

### Notification Output

The service simulates SMS notifications by printing messages such as:

``` text
SMS SENT
Customer: 101
Order: 51
Your order #51 has been placed successfully.
```

and:

``` text
SMS SENT
Customer: 101
Order: 51
Your payment of Rs.75001 for order #51 was successful.
```

For failed payments:

``` text
SMS SENT
Customer: 101
Order: 50
Your payment for order #50 has failed. Reason: INSUFFICIENT_FUNDS
```

------------------------------------------------------------------------

## 15. Retry and DLT

The Notification Service uses:

``` java
@RetryableTopic(attempts = "3", dltTopicSuffix = ".DLT")
```

for each notification listener.

A common:

``` java
@DltHandler
```

handles messages that are not successfully processed after the
configured retry attempts.

### Demonstrated DLT Flow

During the DLT test, `PAYMENT_SUCCESS` processing was intentionally made
to fail.

The message went through retry processing and was finally sent to:

``` text
payment-success.DLT
```

The DLT handler printed:

``` text
Message moved to DLT: ...
```

The final DLT evidence is stored in the `Demo` folder.

> The intentional test exception was removed/commented out after the DLT
> demonstration so the normal notification flow remains active.

------------------------------------------------------------------------

## 16. Kafka Partitions and Ordering

The application topics use multiple partitions. The demonstrated
application topics were configured with three partitions.

Example:

``` text
order-created
Partition 0
Partition 1
Partition 2
```

The Kafka message key is:

``` text
orderId
```

Example:

``` text
Order ID
   ↓
Kafka key
   ↓
Partition selection
```

A console consumer can display both key and partition:

``` bash
kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-created \
  --property print.partition=true \
  --property print.key=true
```

The project demonstration verified different orders being distributed
across partitions.

Using the same key provides consistent partition routing for that key,
which supports ordering of messages for the same key within a partition.

------------------------------------------------------------------------

## 17. Consumer Scaling

Consumer scaling was demonstrated using multiple Payment Service
instances with the same:

``` text
payment-service-group
```

Example:

``` text
Payment Service Instance 1
          |
          |
Payment Service Instance 2
          |
          v
payment-service-group
          |
          v
order-created partitions
```

Kafka distributes available partitions among active consumers in the
same consumer group.

The scaling demonstration and screenshots are stored in the `Demo`
folder.

------------------------------------------------------------------------

## 18. Failure and Recovery

The Payment Service failure/recovery demonstration follows this
sequence:

``` text
1. Stop Payment Service
        ↓
2. Create an order
        ↓
3. Payment Service is unavailable
        ↓
4. Restart Payment Service
        ↓
5. Kafka event is consumed
        ↓
6. Payment is processed
```

The demonstration was recorded in:

``` text
PaymentServiceStop&StartsDemo.mp4
```

The video is stored in the `Demo` folder.

------------------------------------------------------------------------

## 19. Successful Payment Flow --- Verified

A complete successful flow was demonstrated with Order ID `51`.

``` text
ORDER_CREATED
      ↓
PAYMENT_SUCCESS
      ↓
DELIVERY_CREATED
      ↓
NOTIFICATION SENT
```

The final screenshot is:

``` text
Successful Payment Flow.png
```

------------------------------------------------------------------------

## 20. Payment Failure Flow --- Verified

A payment failure flow was demonstrated with Order ID `50`.

``` text
ORDER_CREATED
      ↓
PAYMENT_FAILED
      ↓
DELIVERY_NOT_TRIGGERED
      ↓
NOTIFICATION SENT
```

The final screenshot is:

``` text
Payment Failed Flow.png
```

This verifies that a failed payment does not trigger Delivery Service.

------------------------------------------------------------------------

## 21. Database

MySQL is used for persistence.

Database:

``` text
ecom_kafka_project
```

Main business data:

``` text
Orders
Payments
Deliveries
```

Spring Data JPA / Hibernate handles database persistence and query
generation.

The Payment table stores the original source event ID used for
duplicate-event detection.

------------------------------------------------------------------------

## 22. Running the Project

### Step 1 --- Start Infrastructure

Start:

``` text
ZooKeeper
Kafka
MySQL
```

Verify the Kafka and ZooKeeper Docker containers are running.

Kafka:

``` text
localhost:9092
```

### Step 2 --- Start Order Service

``` bash
cd project-order-service
mvnw.cmd spring-boot:run
```

Port:

``` text
8080
```

### Step 3 --- Start Payment Service

``` bash
cd project-payment-service
mvnw.cmd spring-boot:run
```

Port:

``` text
8085
```

### Step 4 --- Start Delivery Service

``` bash
cd project-delivery-service
mvnw.cmd spring-boot:run
```

Port:

``` text
8082
```

### Step 5 --- Start Notification Service

``` bash
cd project-notification-service
mvnw.cmd spring-boot:run
```

Port:

``` text
8083
```

------------------------------------------------------------------------

## 23. Place an Order

Using Postman:

``` http
POST http://localhost:8080/orders
Content-Type: application/json
```

Example:

``` json
{
  "customerId": 101,
  "customerName": "Rahul",
  "productId": 501,
  "productName": "Laptop",
  "quantity": 1,
  "amount": 75000,
  "deliveryAddress": "Bangalore"
}
```

Expected flow:

``` text
POST /orders
     ↓
Order Service
     ↓
ORDER_CREATED
     ↓
order-created
     ↓
Payment Service
     ↓
PAYMENT_SUCCESS / PAYMENT_FAILED
     |
     +---- PAYMENT_FAILED → Notification
     |
     +---- PAYMENT_SUCCESS
                    ↓
              Delivery Service
                    ↓
              DELIVERY_CREATED
                    ↓
              Notification Service
```

------------------------------------------------------------------------

## 24. Useful Kafka Commands

### Describe topics and partitions

``` bash
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --describe
```

### List topics

``` bash
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### Observe keys and partitions

``` bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-created \
  --property print.partition=true \
  --property print.key=true
```

### Check Payment Consumer Group

``` bash
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group payment-service-group
```

For scaling/member information:

``` bash
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group payment-service-group \
  --members
```

------------------------------------------------------------------------

## 25. Demo Evidence

All final screenshots and the failure/recovery video are kept together
in:

``` text
Demo/
```

Current evidence includes:

``` text
Successful Payment Flow.png
Payment Failed Flow.png
Kafka topics & partitions.png
Ordering & distribution.png
payment consumer group.png
Consumer Scaling 01.png
Consumer Scaling 02.png
scaling proof-01.png
scaling proof-02.png
duplicate event check (idempotency).png
DLT.png
PaymentServiceStop&StartsDemo.mp4
```

These demonstrate the major project requirements:

-   Successful end-to-end flow
-   Payment failure flow
-   Kafka topics and partitions
-   Message key / partition distribution
-   Consumer group
-   Consumer scaling
-   Duplicate message / idempotency
-   Retry and DLT
-   Payment Service failure and recovery

------------------------------------------------------------------------

## 26. Postman Collection

The project includes a Postman collection for creating orders.

Request:

``` text
POST /orders
```

The Postman collection is included with the project.

------------------------------------------------------------------------

## 27. Architecture Diagram

The project includes an architecture diagram showing:

``` text
Customer
   ↓
Order Service
   ↓
Kafka: order-created
   ├──────────────→ Notification Service
   ↓
Payment Service
   ├──→ payment-success ──→ Delivery Service
   │                            ↓
   │                       delivery-created
   │                            ↓
   │                     Notification Service
   │
   └──→ payment-failed ──→ Notification Service
```

------------------------------------------------------------------------

## 28. Final Testing Checklist

  Requirement                                       Status
  ------------------------------------------------- --------------
  Order Service → `ORDER_CREATED`                   ✅ Completed
  Payment Service consumes `order-created`          ✅ Completed
  Payment success/failure events                    ✅ Completed
  Delivery Service consumes successful payment      ✅ Completed
  Delivery event                                    ✅ Completed
  Notification Service consumes relevant events     ✅ Completed
  Multiple consumer groups                          ✅ Completed
  Multiple partitions                               ✅ Completed
  `orderId` as Kafka key                            ✅ Completed
  Partition distribution / ordering demonstration   ✅ Completed
  Consumer scaling                                  ✅ Completed
  Payment Service failure and recovery              ✅ Completed
  Duplicate message / idempotency                   ✅ Completed
  Retry handling                                    ✅ Completed
  `payment-success.DLT`                             ✅ Completed
  Code cleanup/refactoring                          ✅ Completed
  Successful flow evidence                          ✅ Completed
  Payment failure evidence                          ✅ Completed
  Final demo evidence                               ✅ Completed

------------------------------------------------------------------------

## 29. Final Architecture Summary

``` text
                         CUSTOMER
                            |
                            | POST /orders
                            v
                  +----------------------+
                  |    ORDER SERVICE     |
                  |       :8080          |
                  +----------+-----------+
                             |
                             | ORDER_CREATED
                             v
                    +----------------+
                    |      KAFKA     |
                    | order-created  |
                    +-------+--------+
                            |
                 +----------+----------+
                 |                     |
                 v                     v
       +-------------------+   +----------------------+
       |  PAYMENT SERVICE  |   | NOTIFICATION SERVICE |
       |       :8085       |   |        :8083         |
       +---------+---------+   +----------------------+
                 |
          +------+------+
          |             |
          v             v
 payment-success   payment-failed
          |
          v
 +----------------------+
 |   DELIVERY SERVICE   |
 |       :8082          |
 +----------+-----------+
            |
            | DELIVERY_CREATED
            v
    +----------------+
    |      KAFKA     |
    | delivery-created|
    +-------+--------+
            |
            v
 +----------------------+
 | NOTIFICATION SERVICE |
 +----------------------+
```

------------------------------------------------------------------------

## 30. Conclusion

This project demonstrates an event-driven Order Processing System using
Spring Boot Microservices, Apache Kafka, and MySQL.

The completed implementation demonstrates:

-   Asynchronous communication between independent microservices
-   Kafka producers and consumers
-   Separate consumer groups
-   Multiple Kafka partitions
-   `orderId` as the Kafka message key
-   Partition distribution and ordering behavior
-   Consumer scaling
-   Payment Service failure and recovery
-   Duplicate-event protection through database-backed idempotency
-   Retry processing
-   Dead Letter Topic handling
-   Successful and failed payment flows
-   Notification processing

The project source code, Postman collection, architecture diagram, final
screenshots, and demo video are organized as part of the final
submission.
