# 실시간 채팅 시스템 구현

## [프로젝트 소개]

본 프로젝트는 WebSocket 기반 실시간 채팅 시스템으로,
사용자가 매칭 버튼을 클릭하면 랜덤한 사용자와 1:1 매칭되어 채팅방이 생성되며,
이후에는 기존에 매칭 이력이 있는 사용자와 자유롭게 채팅할 수 있도록 구현한 개인 프로젝트입니다.

단일 서버 환경에 국한되지 않고,
로드 밸런서를 고려한 이중 서버 환경에서도 안정적으로 동작하도록 설계하여
실시간 통신, 인증 처리, 동시성 문제를 중심으로 구현했습니다.

## [기간 / 인원]

기간: 2025.10 ~ 2025.12

인원: 1명 (개인 프로젝트)

## [담당 역할]

Spring Boot, JPA, PostgreSQL 기반 REST API 설계 및 구현

로드 밸런서를 고려한 이중 서버 구조 설계

JWT 기반 인증 및 토큰 재발급 흐름 구현

1:1 랜덤 매칭 로직 및 동시성 제어 구현

WebSocket 기반 실시간 채팅 및 파일 전송 기능 구현

실시간 알림 수신 기능 구현

AWS 환경에 서비스 배포

## [개발 내용]
### - 인증 / 토큰 관리

JWT 기반 Access Token / Refresh Token 인증 구조 적용

Access Token 상태에 따라 HTTP 상태 코드 기준으로 인증 흐름 분리

401 Unauthorized: Access Token 만료 → Refresh Token을 통한 자동 재발급

403 Forbidden: 인증 실패 또는 비정상 상태 → 강제 로그아웃 처리

초기에는 JWT를 LocalStorage에 저장하는 방식으로 구현했으나,
XSS 공격에 취약하다고 판단하여 Refresh Token을 HttpOnly Cookie로 관리하는 구조로 개선

프론트엔드는 UI 수준의 로그인 상태만 관리하고,
실제 인증 및 권한 검증은 서버(Spring Security)에서 수행

### - 실시간 채팅

WebSocket + SockJS + STOMP를 이용한 실시간 메시지 송·수신 구현

로드 밸런서 환경에서 WebSocket 세션이 서버별로 분산되는 문제를 해결하기 위해
RabbitMQ Fanout Exchange를 사용하여 모든 서버에 채팅 이벤트를 브로드캐스트하도록 구성

각 서버는 전달받은 이벤트를 자신에게 연결된 WebSocket 세션에 전송

### - 랜덤 매칭

사용자가 매칭 요청 시 대기열에 진입하여 1:1 랜덤 매칭 처리

동시 매칭 요청 시 중복 매칭이 발생할 수 있는 문제를 인지하고,
락(Lock) 기반 동시성 제어를 적용하여 매칭 무결성 보장

매칭 성공 시 채팅방을 생성하고,
이후에는 매칭 이력이 있는 사용자와 재접속 가능하도록 설계

### - 파일 전송

채팅 중 이미지 및 파일 전송 기능 구현

파일 메타데이터와 메시지 정보를 분리하여 관리

사용되지 않는 파일이 누적되는 문제를 고려하여
스케줄러 기반 파일 자동 삭제 로직을 추가하여 스토리지 관리

### - 실시간 알림

채팅 이벤트 발생 시 실시간 알림 수신 기능 구현

실시간 통신(WebSocket)과 일반 API 요청(HTTP)의 역할을 분리하여 처리


## [트러블슈팅]

WebSocket(STOMP) 연결 시 HTTP 요청과 달리
Spring Security JWT Filter가 적용되지 않는 문제를 확인

STOMP ChannelInterceptor를 구현하여 CONNECT 시점에서 JWT 인증 수행

메시지 단위 인증은 성능 및 복잡도를 고려해 제외하고,
연결 이전 단계에서 토큰 만료를 처리하도록 프론트엔드와 흐름을 정리

로드 밸런서 환경에서 특정 서버에만 메시지가 전달될 수 있는 문제를 인지하고,
RabbitMQ Fanout Exchange를 적용하여
모든 서버에 이벤트가 전달되도록 개선

동시 매칭 요청 시 중복 매칭 가능성을 확인하고,
락 기반 동시성 제어를 적용하여 매칭 무결성 확보

채팅 파일 업로드 기능 도입 후
사용되지 않는 파일이 지속적으로 누적될 수 있는 운영 문제를 고려하여
스케줄러 기반 파일 자동 정리 로직 추가

설계 고민 및 향후 개선 방향

채팅 메시지는 즉시 DB에 저장하는 구조를 사용했으나,
트래픽 증가 시 DB 쓰기 부하가 발생할 수 있다는 점을 인지

실무에서는 메시지 유실 방지와 실시간성을 우선시해
즉시 저장 방식을 사용한다는 점을 학습했으며,
향후 트래픽 증가 시 메시지 큐를 활용한 비동기 처리 구조를 검토할 수 있다고 판단

## [사용 기술]

### Backend

![Java](https://img.shields.io/badge/Java_17-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socketdotio&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

### Frontend

![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Axios](https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white)
![SockJS](https://img.shields.io/badge/SockJS-000000?style=for-the-badge&logo=socketdotio&logoColor=white)

## [프로젝트 화면]

### header, footer, main

#### header
<img width="1901" height="62" alt="header" src="https://github.com/user-attachments/assets/71d4a4c4-52f9-4307-9914-6f36f20dcfc1" />

#### footer
<img width="1896" height="208" alt="footer" src="https://github.com/user-attachments/assets/f3929910-5f92-4f98-a757-58d0e7f6c2a7" />

#### main
<img width="903" height="441" alt="main1" src="https://github.com/user-attachments/assets/aa5d281e-beac-45d5-9f8c-c20fa99d6306" />

### 채팅

#### 채팅 목록
<img width="851" height="611" alt="chatingList1" src="https://github.com/user-attachments/assets/c3a148cc-2b07-4076-a0cc-57bc9e3f0ca1" />

#### 채팅 방
<img width="567" height="793" alt="chating1" src="https://github.com/user-attachments/assets/ca9d8519-bb8d-4fea-ba7c-94c7499d67d2" />

#### 매칭
<img width="575" height="489" alt="matching2" src="https://github.com/user-attachments/assets/204957cd-0d0d-4fba-9c04-d7d5d9be0dd1" />

#### 알림
<img width="323" height="291" alt="alram1" src="https://github.com/user-attachments/assets/12ac612d-a4ae-4a43-845a-21a7aac090f7" />

### 회원

#### 회원가입
<img width="404" height="754" alt="register1" src="https://github.com/user-attachments/assets/205d58f0-629a-4c19-b109-ce0133de22ce" />

#### 회원 정보 수정
<img width="882" height="626" alt="updateMemberInfo_u1" src="https://github.com/user-attachments/assets/4f6baec2-f836-4e61-8191-d44d53b9e578" />

<img width="667" height="552" alt="updateMemberInfou2" src="https://github.com/user-attachments/assets/614d361b-fa3f-41b1-94bf-3b2aae00ee69" />

#### 로그인
<img width="395" height="430" alt="login1" src="https://github.com/user-attachments/assets/c278a0ae-2bff-465a-8c61-051c34a19f93" />


### 회원 관리













