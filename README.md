# Example1 - Micronaut / NATS

## This is an example of an application based on

* Kotlin
* Micronaut
* Vertx (Core and Postgres non-blocking driver)
* Crabzilla (based on Vertx)
* Nats Streaming
* Reactive JOOQ (for read model)

###### Requirements

* Java 11
* Maven (tested with 3.5.0+)
* Docker compose (tested with 1.18.0)
* Kotlin plugin for your IDE

## Modules roles

### Libraries
* read-model: Classes generated by JOOQ against the read model database
* write-model: Classes and functions that express the domain / write model (using crabzilla-core)

### Applications
* commands-handler: to receive commands using REST or NATS queues and execute them
* events-publisher: to scan the event store periodically then publish the new events to a NATS topic
* events-publisher-ha: same service as above but with High Availability using clustered Vertx (powered by Hazelcast)
* events-projector: to project events into read model or to project them to integration events and publish them
* queries-handler: to perform non blocking queries against the read model

### Runtime architecture
* commands-handler and queries-handler apps can scale horizontally (many instances) with independence. To attend 80% reads and 20% writes scenarios, for example.
* events-publisher and events-projector apps should have only one active instance process. Vertical scalability. Single writer principle to achieve processing events in order. For resilience, these processes could work with more instances but only with an efficient active/standby mode. The example events-publisher-ha accomplish it (horizontal scaling and fail-safe resilience) using clustered Vertx.

## Steps

1. Clone crabzilla:

```bash
git clone https://github.com/crabzilla/crabzilla
cd crabzilla
```

2. Open another terminal and build it, skipping running tests:

```bash
mvn clean install -DskipTests=true
```

3. Now clone this demo

```bash
git clone https://github.com/crabzilla/example1
cd example1
```


4. Now let's start the Postgres database and NATS

```bash
cd example1
docker-compose up
```


5. Now build it

```bash
cd example1
gradle build
```

6. Run command-handler application

```bash
cd apps/command-handler
gradle run
```

7. Run events-publisher (or events-publisher-ha) application

```bash
cd apps/events-publisher
gradle run
```

8. Run events-projector application

```bash
cd apps/events-projector
gradle run
```

9. Run queries-handler application

```bash
cd apps/queries-handler
gradle run
```

10. Make a request to commands-handler:

```bash
wget -O- http://localhost:8080/hello
```

11. Finally, make a request to queries-handler:

```bash
wget -O- http://localhost:8081/customers
```

## Notes

1. NATS is running in memory, so the messages are not persistent.
2. Since we are using an AtomicInteger to generate ids, every time you starts the application you need to recreate the database:

```bash
cd demo
docker-compose down -v
docker-compose up
```

## Design

![GitHub Logo](/cqrs-arch-outbox.png)
