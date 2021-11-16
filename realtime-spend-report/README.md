# Realtime Spend Report Demo

If you want to follow along, you will require a computer with:

- Java 8 or 11
- Maven
- Docker



## Starting the Playground

We assume that you have [Docker](https://docs.docker.com/) (1.12+) and [docker-compose](https://docs.docker.com/compose/) (2.1+) installed on your machine.

The required configuration files are available in the [spark-streaming-playgrounds](https://github.com/listart/spark-streaming-playgrounds) repository. First checkout the code and build the docker image:

```sh
git clone https://github.com/listart/spark-streaming-playgrounds.git
cd spark-streaming-playgrounds/realtime-spend-report
docker-compose build
```

Then start the playground:

```sh
docker-compose up -d
```



## Entering the Playground

### Kafka Topics

You can look at the records that are written to the Kafka Topics by running

```sh
docker-compose exec kafka kafka-console-consumer.sh \
--bootstrap-server localhost:9092 \
--topic transactions
```

> input topic (1000 records/s)



### Mysql

Explore the results from inside MySQL.

```sh
$ docker-compose exec mysql mysql -Dsql-demo -usql-demo -pdemo-sql

mysql> select count(*) from spend_report;
+----------+
| count(*) |
+----------+
|      110 |
+----------+
```



### Grafana

Finally, go to Grafana http://localhost:3000 to see the fully visualized result!

