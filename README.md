# Spring Cloud로 개발하는 MSA 애플리케이션

## 필요 서비스 (주요 라이브러리)

1. Discovery Service (spring-cloud-starter-netflix-eureka-server)
- 가용한 서비스 인스턴스 목록과 그 위치(host, port)가 동적으로 변하는 가상화 혹은 컨테이너화된 환경에서, 클라이언트가 서비스 인스턴스를 호출할 수 있도록 Service registry를 제공/관리하는 서비스
- Service Registration: 서비스가 자기 자신의 정보를 Eureka에 등록하는 행동
- Service Registry: 서비스가 스스로 등록한 정보들의 목록, 가용한 서비스들의 위치 정보로 갱신됨
- Service Discovery: 서비스 클라이언트가 요청을 보내고자 하는 대상의 정보를 Service Registry를 통해 발견하는 과정

2. API Gateway Service (spring-cloud-starter-netflix-eureka-client, spring-cloud-starter-gateway)
- Frontend로 부터 모든 요청을 받아 내부 마이크로서비스들에게 요청을 전달하므로 단일 종단점을 갖게한다.
- CORS, 인증, 보안과 같은 공통 설정을 처리할 수 있다.
- 클라이언트 요청을 적절한 서비스로 라우팅하거나 필터를 적용하여 헤더에 특정 정보를 추가할 수 있다.

3. Configuration Service (spring-cloud-config-server, spring-cloud-starter-bus-amqp, spring-boot-starter-actuator)
- 분산 시스템에서 환경설정을 외부로 분리하여 관리할 수 있는 기능을 제공한다.
- 운영중에 서버 빌드 및 배포 없이 환경설정 변경 가능 (단, 일부 설정에 따라 애플리케이션 재기동 필요할 수 있음)

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
