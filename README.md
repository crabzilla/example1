# Example1 - Micronaut / NATS

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
git clone https://github.com/rodolfodpk/example1
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
cd example1/apps/command-handler
gradle run
```

7. Run events-projector application

```bash
cd example1/apps/events-projector
gradle run
```

8. Run events-publisher application

```bash
cd example1/apps/events-publisher
gradle run
```

7. Finally, make a request:

```bash
wget -O- http://localhost:8080/hello
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
