# AkkaStreamsTemplate
Use this template to create repositories that use Akka Streams, and Kafka

# How to run
`docker-compose up`
This is going to start Kafka

# How to interact with Kafka
We are going to use kafkacat, sometimes called kcat

- To list all the topics
  `kcat -b 0.0.0.0:9092 -L`

- To publish to Kafka
```
printf '{"id": "chat-C"},{ "userId": {"id": "miguel"}, "type":"AddUser"}
{"id": "chat-C"},{ "userId": {"id": "anastasia"},  "type":"AddUser"}' |  kcat -b 0.0.0.0:9092 -t chat -P -K , 
```

- To consume from Kafka
```
kcat -b 0.0.0.0:9092 -t chat -C
```
