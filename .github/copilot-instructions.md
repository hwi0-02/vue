🤖 GitHub Copilot 지침 (Instructions)
이 문서는 이 저장소의 코드 일관성을 유지하고, 개발 생산성을 극대화하며, 기술 스택에 맞는 최적화된 코드를 생성하기 위해 GitHub Copilot이 준수해야 할 핵심 지침을 정의합니다.
1. 프로젝트 개요 및 기술 스택
이 프로젝트는 예약 시스템을 갖춘 호텔 예약 웹 서비스입니다.
당신은 세계 최고의 소프트웨어 엔지니어로서, 아래 지침을 엄격히 준수하여 코드를 생성해야 합니다.
1.1. 기술 스택

주요 언어: Java (백엔드), JavaScript (프론트엔드)
프론트엔드: Vue 3 (Options API 및 Composition API 혼용), Vue Router, Axios
백엔드: Spring Boot 3 (Java), Spring Data JPA, Spring Security (JWT 및 OAuth2)
빌드 도구: Vite (프론트엔드)
데이터베이스: mariaDB
버전 관리: Git (GitHub)


2. 프론트엔드 (Vue.js / JavaScript) 지침
Copilot은 Vue 컴포넌트 및 JavaScript 파일을 작성할 때 다음 규칙을 따라야 합니다.
2.1. Vue 컴포넌트 구조 및 스타일

파일 형식: 모든 UI 컴포넌트는 단일 파일 컴포넌트(.vue)를 사용해야 합니다.
컴포넌트 이름: 컴포넌트 이름은 항상 **PascalCase**를 사용합니다. (예: HotelDetailView.vue, AppHeader.vue)
컴포넌트 분리: 재사용 가능한 로직은 별도의 composables(src/composables/)로 분리하여 코드 중복을 최소화합니다.
스크립트 스타일: <script> 블록에서는 표준 JavaScript(ES6+) 문법을 사용합니다.

변수 선언은 var를 사용하지 않고 **const 또는 let**을 사용합니다.
비동기 로직에는 콜백 대신 async/await 구문을 우선 사용합니다.
배열 조작 시 map, filter, reduce 등 함수형 메서드를 적극 활용합니다.


API 통신: 모든 HTTP 요청은 src/api/http.js에 정의된 axios 인스턴스를 사용해야 합니다. 직접적인 fetch API 호출은 금지합니다.

API 호출 함수는 src/api/ 디렉토리에 모듈별로 분리하여 관리합니다.
에러 처리는 일관된 패턴(try-catch 또는 interceptor)을 따릅니다.


성능 최적화:

무거운 연산은 computed를 사용하여 캐싱합니다.
리스트 렌더링 시 반드시 :key 속성을 고유한 값으로 지정합니다.
불필요한 리렌더링을 방지하기 위해 v-once, v-memo 디렉티브를 적절히 활용합니다.
큰 리스트는 가상 스크롤링(virtual scrolling) 구현을 고려합니다.


스타일링:

<style> 블록은 scoped 속성을 사용하여 컴포넌트 스타일이 다른 컴포넌트에 영향을 주지 않도록 합니다.
기존 CSS 파일(예: src/assets/css/)에서 스타일을 가져오는 패턴을 유지합니다.
반복되는 스타일은 CSS 변수 또는 유틸리티 클래스로 추상화합니다.



2.2. 명명 규칙 (JavaScript)

변수, 함수, 메서드: camelCase (예: getUserData, hotelList, handleFormSubmit)
전역 상수: UPPER_SNAKE_CASE (필요한 경우)
이벤트 핸들러: handle 또는 on 접두사 사용 (예: handleClick, onSubmit)
Boolean 변수: is, has, should 접두사 사용 (예: isLoading, hasError, shouldShowModal)

2.3. 상태 관리 및 데이터 흐름

Props 검증: 모든 props에는 타입 검증과 필요시 기본값을 정의합니다.
이벤트 네이밍: 커스텀 이벤트는 kebab-case를 사용합니다. (예: @update-data, @item-selected)
반응성 유지: 객체나 배열을 업데이트할 때는 불변성을 유지하는 방식으로 작성합니다.


3. 백엔드 (Spring Boot / Java) 지침
Copilot은 Java 및 Spring Boot 코드를 생성할 때 다음 원칙을 준수해야 합니다.
3.1. 아키텍처 및 모듈화

레이어 분리: 코드는 Controller, Service, Repository, Domain, DTO로 명확하게 계층을 분리하여 작성해야 합니다.

Controller: HTTP 요청 처리 및 DTO 변환만 담당하고 비즈니스 로직을 포함하지 않습니다. 가능한 한 얇게(thin) 유지합니다.
Service: 핵심 비즈니스 로직을 구현합니다. 복잡한 로직은 별도의 헬퍼 클래스나 전략 패턴으로 분리합니다.
Repository: 데이터베이스 상호작용(Spring Data JPA)만 담당합니다. 복잡한 쿼리는 @Query 또는 QueryDSL을 활용합니다.


패키지 구조: 기존 패키지 구조(com.example.backend.[module_name].[layer])를 엄격하게 따릅니다.
DTO 사용: API 요청 본문(Request Body) 및 응답 본문(Response Body)에는 반드시 전용 DTO 객체를 사용합니다. 도메인(Domain) 엔티티를 직접 노출하거나 요청 객체로 사용하지 않습니다.

DTO와 Entity 간 변환은 명시적인 매퍼 메서드 또는 MapStruct를 활용합니다.



3.2. Java 코딩 컨벤션

명명 규칙: 표준 Java 컨벤션을 따릅니다.

클래스/인터페이스: PascalCase (예: HotelService, HotelDto)
메서드/변수: camelCase (예: getHotelDetails, reservationStatus)
상수: UPPER_SNAKE_CASE (예: DEFAULT_PAGE_SIZE)


JPA 엔티티:

@Entity가 붙은 도메인 클래스에는 비즈니스 로직을 최소화하고, 반드시 Lombok의 @Data 또는 @Getter/@Setter를 사용하여 코드를 간결하게 유지합니다.
연관 관계는 명확하게 정의하고, fetch 전략(LAZY/EAGER)을 신중하게 선택합니다. 기본적으로 FetchType.LAZY를 사용합니다.
N+1 쿼리 문제를 방지하기 위해 @EntityGraph 또는 join fetch를 활용합니다.


예외 처리:

예외 처리는 com.example.backend.common.GlobalExceptionHandler.java의 패턴을 따라 처리하며, 명시적인 예외 클래스를 정의하고 사용합니다.
비즈니스 예외와 시스템 예외를 명확히 구분합니다.



3.3. 성능 최적화

데이터베이스 쿼리:

불필요한 조인을 피하고, 필요한 컬럼만 조회(DTO Projection)합니다.
대량 데이터 처리 시 페이징(Pageable)을 반드시 적용합니다.
반복적인 쿼리는 배치 처리 또는 벌크 연산을 고려합니다.


캐싱: 자주 조회되고 변경이 적은 데이터는 @Cacheable 어노테이션을 활용한 캐싱을 적용합니다.
트랜잭션:

@Transactional은 Service 계층에만 적용하며, 불필요하게 긴 트랜잭션을 피합니다.
읽기 전용 작업에는 @Transactional(readOnly = true)를 사용합니다.


비동기 처리: 시간이 오래 걸리는 작업(이메일 발송, 외부 API 호출 등)은 @Async를 활용하여 비동기로 처리합니다.

3.4. 코드 품질

불변성: 가능한 한 필드를 final로 선언하고, 불변 객체를 선호합니다.
Stream API: 컬렉션 처리 시 Java Stream API를 적극 활용하여 가독성과 성능을 향상시킵니다.
Optional: null 체크 대신 Optional을 사용하여 명시적으로 값의 존재 여부를 표현합니다.
메서드 크기: 한 메서드는 한 가지 작업만 수행하도록 작성하며, 20줄을 넘지 않도록 합니다. 복잡한 로직은 별도 메서드로 분리합니다.


4. 일반 원칙

코드 문서화:

복잡하거나 중요한 로직을 포함하는 함수 및 클래스에는 Javadoc 스타일 주석을 사용하여 코드의 목적, 매개변수, 반환 값을 명확히 설명합니다. (특히 Java Service 계층의 메서드)
주석은 "무엇을"보다 "왜"를 설명하는 데 집중합니다.


로깅:

디버깅 및 시스템 모니터링을 위해 중요한 작업(API 호출, 트랜잭션 시작/종료, 에러 발생)에 대해 적절한 수준의 로깅(SLF4J/Lombok @Slf4j) 코드를 추가해야 합니다.
로그 레벨을 적절히 구분합니다: DEBUG(개발), INFO(중요 이벤트), WARN(잠재적 문제), ERROR(실제 오류)


테스트 가능한 코드:

의존성 주입을 적극 활용하여 테스트 가능한 코드를 작성합니다.
핵심 비즈니스 로직에는 단위 테스트 작성을 고려한 구조로 설계합니다.


DRY 원칙: 중복 코드를 최소화하고, 공통 로직은 유틸리티 클래스나 헬퍼 함수로 추상화합니다.
SOLID 원칙: 특히 단일 책임 원칙(SRP)과 의존성 역전 원칙(DIP)을 준수하여 유지보수성을 향상시킵니다.
에러 메시지: 사용자에게 표시되는 에러 메시지는 명확하고 실행 가능한 정보를 제공합니다. 디버그 정보는 로그에만 기록합니다.


5. 성능 모니터링 및 프로파일링

측정 가능한 코드: 성능이 중요한 부분에는 실행 시간 측정을 위한 로깅이나 메트릭을 추가합니다.
병목 지점 식별: 반복문 내부의 DB 호출, 불필요한 객체 생성, 동기화 블록 등 잠재적 병목 지점을 사전에 식별하고 최적화합니다.
리소스 관리: 파일, 커넥션, 스트림 등의 리소스는 try-with-resources 구문을 사용하여 자동으로 해제되도록 합니다.