# saga_pattern

Implementation of the saga pattern for learning purposes. The saga pattern can be implemented either orchestration-based or a choreography-based. 
So far i did not implement the choreography-based version. My implementation is based on the descriptions in Microservices Patterns (Chris Richardson, ISBN 9781617294549). I do not use the Eventuate Tram Sagas Framework ([1]) but rather try to implement the described logic myself.

## TODO
### Orchestration-based
- Implement alternative version that uses RabbitMQ channels rather than RabbitMQ RPC calls and persist the saga states whenever a remote procedure call is made.
- Prevent infinite retries if a service is not available --> Look into circuit breaker pattern / exponential backoff
- Upgrade to postgres 15

##### Issues
- Services apart from the order service can not connect to rabbitmq yet, when run in docker. This is not the case if the applications are run directly on the host machine and postgres & rabbitmq using docker as specified in docker-compose.yml.

[1]:https://github.com/eventuate-tram/eventuate-tram-sagas
