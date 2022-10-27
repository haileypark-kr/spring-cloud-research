# Spring Cloud로 개발하는 MSA 애플리케이션

## 필요 서비스 (주요 라이브러리)

1. [Discovery Service](http://localhost:8761) (spring-cloud-starter-netflix-eureka-server)
   - 가용한 서비스 인스턴스 목록과 그 위치(host, port)가 동적으로 변하는 가상화 혹은 컨테이너화된 환경에서, 클라이언트가 서비스 인스턴스를 호출할 수 있도록 Service registry를 제공/관리하는 서비스
   - Service Registration: 서비스가 자기 자신의 정보를 Eureka에 등록하는 행동
   - Service Registry: 서비스가 스스로 등록한 정보들의 목록, 가용한 서비스들의 위치 정보로 갱신됨
   - Service Discovery: 서비스 클라이언트가 요청을 보내고자 하는 대상의 정보를 Service Registry를 통해 발견하는 과정

2. API Gateway Service (spring-cloud-starter-netflix-eureka-client, spring-cloud-starter-gateway)
   - Frontend로 부터 모든 요청을 받아 내부 마이크로서비스들에게 요청을 전달하므로 단일 종단점을 갖게한다.
   - CORS, 인증, 보안과 같은 공통 설정을 처리할 수 있다.
   - 클라이언트 요청을 적절한 서비스로 라우팅하거나 필터를 적용하여 헤더에 특정 정보를 추가할 수 있다.

3. [Configuration Service](http://localhost:8888/ecommerce/default) (spring-cloud-config-server, spring-cloud-starter-bus-amqp, spring-boot-starter-actuator)
   - 분산 시스템에서 환경설정을 외부로 분리하여 관리할 수 있는 기능을 제공한다.
   - 운영중에 서버 빌드 및 배포 없이 환경설정 변경 가능 (단, 일부 설정에 따라 애플리케이션 재기동 필요할 수 있음)
   - Spring cloud bus를 통해 설정 값 변경 시 브로드캐스트할 수 있다.
      - AMQP, Kafka, Redis 지원.

4. User Service (spring-cloud-starter-openfeign, spring-cloud-starter-circuitbreaker-resilience4j)
   - 사용자 관리 서비스
   - 로그인 후 JWT를 발행해준다.
   - 사용자 정보 상세 조회 시, order service에서 주문 정보를 가져와서 응답한다.
      - order service에서 주문 정보를 가져오는 방법: RestTemplate + @LoadBalanced 사용 / FeignClient 사용
   - order service에서 주문 정보 조회 요청 실패 시, circuit breaker가 작동하여 빈 리스트를 리턴한다.
   - 참고: User service, order service, catalog service는 모두 다른 DB를 바라본다.


5. Order Service (spring-kafka)
   - 주문 서비스
   - 주문 요청이 들어오면, (1) order service의 DB에 주문 정보를 추가하고, (2) catalog service에 주문 정보를 보내준다.
   - (1) order service의 DB에 주문 정보를 추가하는 방법: JPA 사용하여 직접 DB에 저장 / Kafka의 "tbl_orders" 토픽으로 주문 정보 전달 시 kafka mariadb sink connector를 통해 DB에 저장
   - (2) catalog service에 주문 정보를 보내는 방법: Kafka의 "example-catalog-service" 토픽으로 주문 정보 전달하여 catalog service가 토픽 consume.

6. Catalog Service (spring-kafka)
   - 재고 관리 서비스
   - Kafka의 "example-catalog-service" 토픽을 리스닝하고 있다가 데이터가 들어오면 consume하여 재고 테이블 업데이트.



## 각 서비스 별 설정 정보

1. Discovery Service
   - Service Discovery 용 서버를 띄우기 위한 설정
   ```java
   @SpringBootApplication
   @EnableEurekaServer // 이 어노테이션만 추가하면 됨
   public class DiscoveryServiceApplication {
   
       public static void main(String[] args) {
           SpringApplication.run(DiscoveryServiceApplication.class, args);
       }
   
   }
   ```

2. API Gateway Service
   - Service Registration: pom.xml에 eureka client 추가 시 자동 설정됨.
   ```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
   ```
   - gateway filter 및 routing 설정: configs > gateway.yml 에서 관리. Configuartino service에서 읽어오며, busrefresh로 설정 변경 가능하다.
   - JWT 검증 필터: AuthorizationHeaderFilter.java
   ```
   @Override	
   public GatewayFilter apply(Config config) { 
   
   return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);
			}

			String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
			String jwt = authorizationHeader.replace("Bearer ", "");

			if (!isJwtValid(jwt)) {
				return onError(exchange, "JWT Not Valid", HttpStatus.UNAUTHORIZED);

			}

			// post-filter 없음
			return chain.filter(exchange);
		};
	}
   ```
   - 로깅 필터 등 넣을 수 있음.

3. Configuration Service
   - config server를 띄우기 위한 설정
   ```java
   @SpringBootApplication
   @EnableConfigServer
   public class ConfigServerApplication {
   
       public static void main(String[] args) {
           SpringApplication.run(ConfigServerApplication.class, args);
       }
   }
   ```

   - `spring-cloud-starter-bootstrap` 라이브러리를 추가하여, application.yml 이전에 설정 정보를 넣을 수 있는 bootstrap.yml을 추가
      - configuration service를 참조하는 다른 마이크로서비스들도 동일한 라이브러리 추가하고, bootstrap.yml에 읽어올 설정 정보를 추가
      - user service의 bootstrap.yml 예시
     ```
     spring:
       cloud:
         config:
           uri: http://localhost:8888  # Configuration service 정보
           name: ecommerce,user-service  # 읽어을 설정파일 이름

     profiles:
       active: dev # 어느 profile의 설정 정보를 읽어올 것인지. prod / dev 등
     ```

   - Spring cloud bus를 통해 설정 변경 브로드캐스팅
      - configuration service의 설정 값 변경 후 아무 마이크로서비스의 [/busrefresh API](http://localhost:8000/actuator/busrefresh) 호출하면 설정 정보 변경됨.
   ```xml
        <!-- AMQP를 통해 bus 설정하기 위한 라이브러리 추가 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
        </dependency>
   
        <!-- spring actuator refresh 하여 변경된 설정값을 스프링부트에 적용하기 위한 라이브러리 추가 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
   ```

4. User service
   - 사용자 상세 조회 시, order service에서 사용자 id로 주문 내역을 조회함
      1. Rest Template 사용

         ```java
         String orderUrl = String.format(env.getProperty("order-service.url"), userId);
         ResponseEntity<List<ResponseOrder>> responseEntity = restTemplate.exchange(orderUrl, HttpMethod.GET, null,
         new ParameterizedTypeReference<List<ResponseOrder>>() { 
 
         });
         
         orders = responseEntity.getBody();
         ```
      2. Feign client + ErrorDecoder 사용
         - @EnableFeignClients 어노테이션으로 feign 사용 설정
          ```java
          @SpringBootApplication
          @EnableEurekaClient
          @EnableFeignClients
          public class UserServiceApplication {

              public static void main(String[] args) {
                  SpringApplication.run(UserServiceApplication.class, args);
              }
          }

          ```
         - @FeignClient 어노테이션 붙은 인터페이스 작성, name은 호출하고자 하는 마이크로서비스의 서비스 이름
          ```java
          @FeignClient(name = "order-service")
          public interface OrderServiceClient {

              @GetMapping("/order-service/{userId}/orders")
              List<ResponseOrder> getOrders(@PathVariable String userId);
          }
          ```
         - API 호출 필요한 경우 인터페이스의 메소드 호출만 하면 됨.
          ```java
             List<ResponseOrder> orders = orderServiceClient.getOrders(userId);
 
          ```
         - API 호출 시 에러 처리는 try-catch 문이 아닌, ErrorDecoder로 처리
          ```java
             @Component
             @RequiredArgsConstructor
             public class FeignErrorDecoder implements ErrorDecoder {
 
                 private final Environment env;
 
                 @Override
                 public Exception decode(String methodKey, Response response) {
 
                     switch (response.status()) {
                         case 400:
                             break;
                         case 404:
                             if (methodKey.contains("getOrders")) {
                                 return new ResponseStatusException(HttpStatus.valueOf(response.status()),
                                     env.getProperty("order-service.exception.order-is-empty"));
                             }
                             break;
                         default:
                             return new Exception(response.reason());
                     }
 
                     return null;
                 }
             }
         ```
      4. Feign client + Circuit breaker 사용
         - 에러 발생 시 전달할 fallback 값 설정 가능.
         ```java
         CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
         List<ResponseOrder> orders = circuitbreaker.run(() -> orderServiceClient.getOrders(userId),
                 throwable -> new ArrayList<>());
 
         ```


5. Order service
   - 주문이 들어오면 kafka로 저장
      - JPA 또는 Kafka sink connector를 통해 tbl_orders 테이블에 저장
      - 재고 수정을 위해 kafka를 통해 catalog service에 주문 정보를 전달
       ```java
       @PostMapping("/{userId}/orders")
       public ResponseEntity createOrder(@PathVariable("userId") String userId, @RequestBody RequestOrder requestOrder) {

           ModelMapper mapper = new ModelMapper();
           mapper.getConfiguration().setMatchingStrategy(STRICT);

           OrderDto orderDto = mapper.map(requestOrder, OrderDto.class);
           orderDto.setUserId(userId);

           /* JPA. 주문 저장 ==> JPA로 저장하지 않고 kafka sink connector 통해서 저장할 예정
           OrderDto createdOrderDto = orderService.createOrder(orderDto);
            */

           /* KAFKA로 ORDERS DB에 데이터 저장 */
           orderDto.setOrderId(UUID.randomUUID().toString());
           orderDto.setTotalPrice(requestOrder.getUnitPrice() * requestOrder.getQuantity());

           // KAFKA. 카프카
           kafkaProducer.send("example-catalog-service", orderDto); // catalog-service에 주문 데이터 보내기
           orderProducer.send("tbl_orders", orderDto); // DB tbl_orders 테이블에 주문 데이터 보내기

           ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);
           return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);

       }
       ```
      - kafka DB sink connector 사용 시, DB 테이블의 스키마를 같이 전달해야 함.
       ```json
       {
           "schema": {
               "type": "struct",
               "fields": [
                   {
                       "type": "string",
                       "optional": true,
                       "field": "order_id"
                   },
                   {
                       "type": "string",
                       "optional": true,
                       "field": "user_id"
                   },
                   {
                       "type": "string",
                       "optional": true,
                       "field": "product_id"
                   },
                   {
                       "type": "int32",
                       "optional": true,
                       "field": "quantity"
                   },
                   {
                       "type": "int32",
                       "optional": true,
                       "field": "total_price"
                   },
                   {
                       "type": "int32",
                       "optional": true,
                       "field": "unit_price"
                   }
               ],
               "optional": false,
               "name": "tbl_orders"
           },
           "payload": {
               "product_id": "CATALOG-0001",
               "quantity": 3,
               "unit_price": 1200,
               "total_price": 3600,
               "order_id": "f077352b-851c-4736-b3a6-ce5c94c6310b",
               "user_id": "00b73397-19e9-4622-82cc-4e623676c951"
           }
       }
       ```

6. Catalog service
   - order service가 kafka topic에 produce한 주문 정보를 consume
    ```java
	@KafkaListener(topics = "example-catalog-service")
	public void updateQuantity(String kafkaMessage) {
		log.info("kafka updateQuantity : {}", kafkaMessage);

		Map<Object, Object> map = new HashMap<>();

		ObjectMapper mapper = new ObjectMapper();

		try {
			map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		Catalog catalogEntity = repository.findByProductId((String)map.get("productId"));
		if (catalogEntity != null) {
			catalogEntity.setStock(catalogEntity.getStock() - (Integer)map.get("quantity"));
			repository.save(catalogEntity);
		}
	}
    ```
    
