- add mapstruct or model mapper, [Comparing](https://dzone.com/articles/comparing-modelmapper-and-mapstruct-in-java-the-po) 
- gralvm with docker native image ?
  - [Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html)
  - [Example](https://medium.hexadefence.com/keycloak-admin-rest-api-63a294814e1b) - [Keycloak Api](https://www.keycloak.org/docs-api/21.1.1/rest-api/index.html)
  - [Example 2](https://www.appsdeveloperblog.com/keycloak-rest-api-create-a-new-user/)

[Spring boot 3 doc:Developing Your First GraalVM Native Application](https://docs.spring.io/spring-boot/docs/3.0.0/reference/htmlsingle/#native-image.developing-your-first-application)

[Add Spring Configuration Cloud (SCC)](https://cloud.spring.io/spring-cloud-config/reference/html/)
- add Class Generation with [Protobuf](https://protobuf.dev/reference/java/java-generated/)
- Migrate [Keycloak docker image](https://www.keycloak.org/getting-started/getting-started-docker)

### Fault Tolerance & Observability
- Add WarnerMetrics / Micrometer metrics for circuit breaker state transitions
- Implement distributed tracing with Micrometer Tracing (Brave/OpenTelemetry)
- Add admin alerting for circuit breaker open/closed state changes
- Implement caching layer (Redis/Caffeine) for employee reads as additional fallback
- Add health check for circuit breaker state in `/actuator/health`
