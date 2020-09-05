# STEW

## :woman_student: 언제 어디서나 함께

언택트 시대를 맞은 학습의 변화 가운데 **'언제 어디서나 함께 공부할 수 있게 하자(STudy EveryWhere)'** 라는 목표를 가지고 온라인 캠 스터디 플랫폼인 STEW를 제작하였습니다.

1. 원하는 스터디그룹을 생성 또는 가입합니다.
2. 스터디원들과 함께 캠을 확인하며 공부 or 회의를 진행합니다.
3. 제공되는 달력과 타이머를 이용하여 일정 관리 및 공부 시간 관리가 가능합니다.

코로나 속에서도 언제 어디서나 함께 공부할 수 있습니다.

## :bar_chart: 개발 환경 및 기술 스택

### 프로젝트 기간

* 20/7/13 - 20/8/21

### 개발 환경

* VScode

* STS 3.9.12

* Node.js 12.18.2

### 기술 스택

#### Backend

* Spring boot

* JWT

* Swagger

#### Frontend

* Vue 2.x

* Vuex

* Vue-Router

* JavaScript

#### DB

* Maria-db
* redis

#### Web Server

* AWS

* Nginx

* letsencrypt

#### 부가 기능

* WebRTC - RTCmulticonnection Open source

## :gem: Getting Started

### Local 환경에서 실행

:one: DB설정

1. Docker에서 MariaDB, Redis실행

2. MariaDB에서 기본 카테고리 설정

   ```mariadb
   insert into gp_cat (gp_cat_nm) values ("영어");
   insert into gp_cat (gp_cat_nm) values ("중국어");
   insert into gp_cat (gp_cat_nm) values ("일본어");
   insert into gp_cat (gp_cat_nm) values ("외국어");
   insert into gp_cat (gp_cat_nm) values ("수능");
   insert into gp_cat (gp_cat_nm) values ("면접");
   insert into gp_cat (gp_cat_nm) values ("취업");
   insert into gp_cat (gp_cat_nm) values ("공무원");
   insert into gp_cat (gp_cat_nm) values ("학생");
   insert into gp_cat (gp_cat_nm) values ("대학생");
   insert into gp_cat (gp_cat_nm) values ("알고리즘");
   insert into gp_cat (gp_cat_nm) values ("고시");
   insert into gp_cat (gp_cat_nm) values ("기사");
   ```

:two: 이미지 경로 설정

- 제공된 기본 이미지로 경로 설정

1. ./main/java/com/ssafy/study/controller/CommonController.java

   ```java
   // 21줄 경로
   private final String fileBaseUrl = "{Your path}\\img";
   ```

2. ./main/java/com/ssafy/study/controller/GroupController.java

   ```java
   // 63줄 경로
   private final String fileBaseUrl = "{Your path}\\img\\group";
   ```

3. ./main/java/com/ssafy/study/controller/UserController.java

   ```java
   // 61줄 경로
   private final String fileBaseUrl = "{Your path}\\img\\user";
   ```

:three:  Backend

```bash6o
$ cd Backend
$ mvn spring-boot:run
```

:four: Frontend

```bash
$ cd Frontend
$ npm install
$ npm run serve
```

## :desktop_computer: Deployment

제공해 준 amazon서버가 지원된다면 : [STEW](https://i3b103.p.ssafy.io/) 홈페이지(9/3일 만료)

## :briefcase: License

[RTCMultiConnection](https://github.com/muaz-khan/RTCMultiConnection) is released under [MIT licence](https://github.com/muaz-khan/RTCMultiConnection/blob/master/LICENSE.md) . Copyright (c) [Muaz Khan](https://muazkhan.com/).

see the LICENSE.md file for details

## :two_men_holding_hands: Contributor

#### TEAM 어묵간장조

[엄홍재](ehj0128@gmail.com)(팀장) : 프로젝트 총괄, Frontend - webRTC개발, 일정, 타이머, 반응형 제작, 품질관리 담당

김송희 : Fullstack - Backend 기능 관련 API 개발, Frontend - UX/UI 

민강규 : Frontend - 채팅, 그룹 기능 개발, UX/UI

정인균 : Backend - Authentication 총괄

조희진 : Frontend - Authentication 총괄
