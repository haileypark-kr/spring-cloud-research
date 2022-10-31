# Spring Cloud로 개발하는 MSA 애플리케이션

## 구성 서비스 설명 <small>(주요 라이브러리)</small>

1. Discovery Service (Eureka server)
    - 가용한 서비스 인스턴스 목록과 그 위치(host, port)가 동적으로 변하는 환경에서, 클라이언트가 서비스 인스턴스를 호출할 수 있도록 Service registry를 제공/관리하는 서비스
    - Service Registration: 서비스가 자기 자신의 정보를 Eureka에 등록하는 행동.
    - Service Registry: 서비스가 스스로 등록한 정보들의 목록. 가용한 서비스들의 위치 정보로 갱신됨
    - Service Discovery: 서비스 클라이언트가 요청을 보내고자 하는 대상의 정보를 Service Registry를 통해 발견하는 과정
    - 처리 순서
        1. Discovery service를 사용하려면 각 마이크로서비스가 Eureka server의 본인의 위치정보를 먼저 등록해야 한다. (service registration)
        2. 클라이언트가 요청을 보내면 Service Registry에서 대상 서비스 인스턴스의 위치를 찾아 반환해준다. (service discovery)


2. API Gateway Service (spring-cloud-starter-netflix-eureka-client, spring-cloud-starter-gateway)
    - 클라이언트로부터 모든 요청을 받아 내부 마이크로서비스들에게 요청을 전달 => 단일 진입점을 갖게 한다.
    - CORS, 인증, 보안과 같은 공통 설정을 처리할 수 있다.
    - 클라이언트 요청을 적절한 서비스로 라우팅하거나 요청/응답에 필터를 적용할 수 있다.
    - 기본적으로 netty + webflux 로 되어 있음.


3. Configuration Service (spring-cloud-config-server, spring-cloud-starter-bus-amqp, spring-boot-starter-actuator)
    - 분산 시스템에서 환경설정을 외부로 분리하여 관리할 수 있는 기능을 제공한다.
    - 운영중에 서버 빌드 및 배포 없이 환경설정 변경 가능
        - 단, 일부 설정에 따라 애플리케이션 재기동 필요할 수 있음
            - ex) bean 설정 변경 등
    - Spring cloud bus를 통해 설정 값 변경 시 브로드캐스트할 수 있다.
        - AMQP, Kafka, Redis 지원.
        - 데모에서는 RabbitMQ를 사용하여 configuration service <-> 각 마이크로서비스 간 설정 브로드캐스팅 진행.


4. User Service (spring-cloud-starter-openfeign, spring-cloud-starter-circuitbreaker-resilience4j)
    - 사용자 관리 서비스
    - 로그인 후 JWT를 발행해준다.
    - 사용자 정보 상세 조회 시, order service에서 주문 정보를 가져와서 응답한다.
        - order service에서 주문 정보를 가져오는 방법: RestTemplate + @LoadBalanced 사용 / FeignClient 사용
    - order service에서 주문 정보 조회 요청 실패 시, circuit breaker가 작동하여 빈 리스트를 리턴한다.


5. Order Service (spring-kafka)
    - 주문 서비스
    - 주문 요청이 들어오면, (1) order service의 DB에 주문 정보를 추가하고, (2) catalog service에 주문 정보를 보내준다.
    - (1) order service의 DB에 주문 정보를 추가하는 방법: JPA 사용하여 직접 DB에 저장 / Kafka의 "tbl_orders" 토픽으로 주문 정보 전달 시 kafka mariadb sink connector를 통해 DB에 저장
    - (2) catalog service에 주문 정보를 보내는 방법: Kafka의 "example-catalog-service" 토픽으로 주문 정보 전달하여 catalog service가 토픽 consume.

6. Catalog Service (spring-kafka)
    - 재고 관리 서비스
    - Kafka의 "example-catalog-service" 토픽을 리스닝하고 있다가 데이터가 들어오면 consume하여 재고 테이블 업데이트.

> 참고: User service(maria db 1), order service(maria db 2), catalog service(h2)는 모두 다른 DB 서버를 바라본다.


<br>

## 각 서비스 코드 설명

1. [Discovery Service](http://localhost:8761)
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

2. [Configuration Service](http://localhost:8888/ecommerce/default)
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
        - configuration service의 bootstrap.yml: configuration service에서 사용할 encryption key 정보만 기재
        ```
        encrypt:
          key: sfksndkfnsdkfnskdjnfkjnkndsfknsdkjfnkj1231928u3912u312
        ```
        - 각 마이크로서비스의 bootstrap.yml: configuration service 서버 정보와 사용할 설정 파일의 이름을 bootstrap.yml에 기재
        ```
        spring:
            cloud:
              config:
                uri: http://localhost:8888  # Configuration service 정보
                name: ecommerce,user-service  # 읽어을 설정파일 이름

        profiles:
            active: dev # 어느 profile의 설정 정보를 읽어올 것인지. prod / dev 등
        ```
    - user-service.yml 설정파일 예시
        - '{cipher}암호화된 값' 추가하면 configuration service가 자동 복호화 수행 (암호화 key는 configuration service의 bootstrap.yml에 기재)
        ```
        spring:
          datasource:
            driver-class-name: org.mariadb.jdbc.Driver
            url: jdbc:mariadb://localhost:3308/users?characterEncoding=UTF-8&allowMultiQueries=true
            username: WEBADMIN
            password: '{cipher}d9ab769b687df942a3ac315f69640befbcf0989d4e6756919e97ce25b9bb4105'
        ```

    - Spring cloud bus를 통해 설정 변경 브로드캐스팅
        - 설정값 공유를 위한 bus 디펜던시와 설정값 변경 적용을 위한 spring actuator 디펜던시 추가
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

        - configuration service의 설정 값 변경 후 아무 마이크로서비스의 [actuator busrefresh API](http://localhost:8000/actuator/busrefresh) 호출하면 설정 정보 변경됨.

3. API Gateway Service
    - Service Registration: pom.xml에 eureka client 추가 시 자동 설정됨. @EnableEurekaClient 어노테이션 불필요
   ```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
   ```
    - gateway의 설정에는 라우팅, 글로벌 필터, 라우팅 별 필터 등의 규칙을 적음. (gateway.yml)
    - 어떤 요청이 어느 서비스로 라우팅되어야 할 지 규칙이 필요한데, 데모에서는 gateway로 들어오는 path가 각 서비스의 이름으로 시작할 때 그 서비스로 라우팅을 시켜줌
        - ex) Gateway로 /user-service/login 요청 ==> User service의 /login으로 라우팅
        - ex) Gateway로 /order-service/** 요청 ==> Order service의 /** 으로 라우팅
    - JWT 검증 필터: AuthorizationHeaderFilter.java
   ```java
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
    - gateway 설정: Configuration service > gateway.yml 에서 관리. busrefresh로 설정 변경 가능하다.



4. User service
    - 사용자 상세 조회 시, order service에서 사용자 id로 주문 내역을 조회함
        1. Rest Template 사용
            - RestTemplate bean 생성할 때 @LoadBalanced 어노테이션을 붙여야 IP 주소가 아닌 microservice 이름으로 호출 가능
            ```java
            @Bean
            @LoadBalanced // IP 가 아니라 microservice 이름으로 호출하려면 이거 붙여줘야 함.
            public RestTemplate restTemplate() {
                return new RestTemplate();
            }

            ```

           ```java
           String orderUrl = String.format(env.getProperty("order-service.url"), userId); // http://order-service/%s/orders
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

                @GetMapping("/{userId}/orders")
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


5. Order service
    - 주문이 들어오면 (1)order service DB의 tbl_orders에 주문 내역을 저장하고, (2)주문된 만큼 catalog service DB의 tbl_catalogs에 재고 정보를 업데이트해야 함.
        1. 주문 내역 저장: JPA 또는 Kafka sink connector를 통해 tbl_orders 테이블에 저장
        2. Catalog service에 주문 내역 전달: kafka를 통해 catalog service에 주문 정보를 전달
        ```java
        @PostMapping("/{userId}/orders")
        public ResponseEntity createOrder(@PathVariable("userId") String userId, @RequestBody RequestOrder requestOrder) {

            ModelMapper mapper = new ModelMapper();
            mapper.getConfiguration().setMatchingStrategy(STRICT);

            OrderDto orderDto = mapper.map(requestOrder, OrderDto.class);
            orderDto.setUserId(userId);

            /* JPA. 주문 저장
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
        - Kafka Producer
        ```java
        @Service
        @Slf4j
        @RequiredArgsConstructor
        public class KafkaProducer {

            private final KafkaTemplate<String, String> kafkaTemplate;

            public OrderDto send(String topic, OrderDto orderDto) {
                ObjectMapper mapper = new ObjectMapper();
                String jsonInString = "";
                try {
                    jsonInString = mapper.writeValueAsString(orderDto);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                kafkaTemplate.send(topic, jsonInString);

                log.info("Kafka Producer sent data from order microservice: {}", orderDto);

                return orderDto;
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


<br>

## 장애 처리와 microservice 분산 추적

1. 서킷브레이커 사용을 통한 장애 처리
    - 서킷 브레이커
        - 다량의 오류를 감지하면 서킷을 열어 새 호출을 받지 않는다.
        - 서킷이 열려 있을 때: 이어지는 호출에서 오류가 발생하지 않게 로컬 캐시의 데이터를 반환하거나 즉각적인 오류 메시지를 반환하는 등 폴백 메서드를 호출할 수 있다.
        - ==> 의존하는 서비스가 응답하지 않아 다른 마이크로서비스가 응답하지 못하게 되는 문제를 방지할 수 있다.
        - 시간이 지나면 half-open 상태로 전환되어 새로운 호출을 허용하다가, 새로운 오류를 감지하면 다시 서킷을 열고, 오류가 사라졌으면 서킷을 닫는다.
    - Resilience4J 서킷브레이커 사용 전: feign client 사용 시에도, ErrorDecoder를 통해 에러를 던질 수 있었음. fallback 값을 전달하는 것은 별도로 구현 필요.
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
        ```JAVA
        List<ResponseOrder> orders = orderServiceClient.getOrders(userId);
        ```
    - Resilience4J 서킷브레이커 사용 시, 에러 발생 시 바로 fallback 값 전달 가능
        ```java
        @Configuration
        public class Resilience4JConfig {

            @Bean
            public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {

                CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                    .failureRateThreshold(4) // 서킷브레이커 오픈을 결정하는 failure rate threshold percentage
                    .waitDurationInOpenState(Duration.ofMillis(1000)) // 서킷브레이커 오픈 상태를 유지하는 지속 시간
                    .slidingWindowType(
                        CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // 카운트 기반: 마지막 N번의 호출 결과 집계, 시간 기반: 마지막 N초 동안의 호출 결과 집계
                    .slidingWindowSize(2)
                    .build();
                TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                    .timeoutDuration(Duration.ofSeconds(4)) // 호출 타임아웃 설정 가능
                    .build();

                return factory -> factory.configureDefault(
                    id -> new Resilience4JConfigBuilder(id)
                        .timeLimiterConfig(timeLimiterConfig)
                        .circuitBreakerConfig(circuitBreakerConfig)
                        .build()
                );
            }
        }

        ```
        ```java
        /* 3. Cirbuit breaker 사용. 오류 발생 시 빈 리스트 응답. */
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        List<ResponseOrder> orders = circuitbreaker.run(() -> orderServiceClient.getOrders(userId),
            throwable -> new ArrayList<>());
        ```



2. Zipkin과 Spring Cloud Sleuth 을 통한 microservice 분산 추적
    - zipkin: 트위터에서 사용하는 분산 환경의 데이터 수집, 추적 시스템 (docker로 별도 설치)
        - span: 하나의 요청에 사용되는 작업의 단위. 64 bit unique id
        - trace: 트리 구조로 이루어진 span 셋. 하나의 요청에 대한 같은 trace id 발급
        - ex) 사용자가 요청 하나 하면 trace id 하나 발급, 여러 마이크로서비스에 요청이 가면 각각 다 span id가 발생.
    - sleuth: springboot 어플리케이션과 zipkin을 연동시켜줘서, 로그 파일/스트리밍 데이터 파일을 zipkin에 전달해줌.
        - 요청 값에 따른 trace id, span id를 발급해주는 역할.
        - ==> zipkin이 trace id, span id를 보고 시각화해줌.
    - user service -> order service 호출 후 로그 확인
        - 로그 구조: [서비스 명, trace id, span id]
        - user service와 order service의 trace id는 같지만, span id가 다른 점 확인 가능.
        ```bash
        # user service
        2022-10-29 18:19:12.806  INFO [user-service,2df9c4f20523ec24,2df9c4f20523ec24] 58876 --- [io-19999-exec-6] c.e.userservice.service.UserServiceImpl  : === before call order microservice
        ```
        ```bash
        # order service
        2022-10-29 18:19:12.905  INFO [order-service,2df9c4f20523ec24,f444b174a0d53197] 59427 --- [o-auto-1-exec-4] c.e.o.controller.OrderController         : === before retreive order data
        ```
    - [zipkin](http://localhost:9411/zipkin) 접속하여 trace id로 검색
    - order service에서 강제로 장애 발생시킨 경우, zipkin에서 오류도 확인 가능.
