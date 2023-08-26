# saga_pattern

Implementation of the saga pattern for learning purposes. The saga pattern can be implemented either orchestration-based or a choreography-based. 
So far i did not implement the choreography-based version. My implementation is based on the descriptions in Microservices Patterns (Chris Richardson, ISBN 9781617294549). I do not use the Eventuate Tram Sagas Framework ([1]) but rather try to implement the described logic myself.

## TODO
### Orchestration-based
- Implement alternative version that uses RabbitMQ channels rather than RabbitMQ RPC calls and persist the saga states whenever a remote procedure call is made.
- Prevent infinite retries if a service is not available --> Look into circuit breaker pattern / exponential backoff
- Upgrade to postgres 15

[1]:https://github.com/eventuate-tram/eventuate-tram-sagas
