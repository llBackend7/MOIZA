# MOIZA

사용자들이 모임원들과 함께 약속 시간과 약속 장소를 빠르게 정할 수 있도록 도와주는 서비스



### 주요 기능

그룹장을 통해 만들어진 모임 생성 방을 통해 각 그룹원들은 가능한 시간과 원하는 장소를 설정한다.
현황 페이지에서 최대로 겹치는 시간대를 추천해준다.
최종적으로 모두가 원하는 시간과 장소를 결정할 수 있도록 한다.
결과는 모든 그룹원들에게 알림으로 전송된다.

[모임 내에서 사용자간 겹치는 시간대를 식별하는 계산 서버](https://github.com/llBackend7/MOIZA-TIME-CALCULATOR/tree/main)

## 🕰️ 개발 기간

- 23.05.22 ~ 23.07.13 (8주)

## 🧑‍🤝‍🧑 맴버구성

- 임영균 : 참여 페이지, 모임 참여 기능, 겹치는 시간 TOP10 계산 기능(시간 현황 페이지), 시간 현황 계산 프로세스 분리, 경고 메시지 UI 추가, 테스트 관리
- 문재원 : 장소 투표 페이지, 테스트 관리, 경고 메시지(toastWarning) 추가
- 이은혜 : UI 설계/구현, 카카오 로그인, 결과 페이지, 시간 현황 페이지의 참여/미참여 멤버 표시, 카톡 메세지로 모임원 초대, 카카오맵 길찾기, 구글 캘린더 연동
- 정상화 : 모임 참여 기능 구현, 마감된 모임 이메일 발송, 채팅 페이지, 테스트 관리, CI/CD 구축, NCP 서버 배포



## ⚙️개발 환경 및 기술 스택

- **Front-end**

	HTML/CSS/JS, Thymeleaf, DaisyUI, TailwindCSS, JQuery, Ajax

- **Back-end**

	`Java 17`

	`Spring Boot 3.1.0`

	Spring Data JPA, MySQL

	MongoDB, Redis

- **Infrastructure**

	Naver Cloud Platform, Docker, Nginx, Jenkins

- **Test**

	Junit5, Jacoco, ApacheJmeter


## 📌 주요 기능

[성능 개선 기록 Wiki 보기](https://github.com/llBackend7/MOIZA/wiki/%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0-%EA%B8%B0%EB%A1%9D)

[겹치는 시간 TOP10 계산 프로세스 분리 과정 Wiki 보기](https://github.com/iyk2h/MOIZA-TIME-CALCULATOR/wiki/%EA%B2%B9%EC%B9%98%EB%8A%94-%EC%8B%9C%EA%B0%84-TOP10-%EA%B3%84%EC%82%B0-%ED%94%84%EB%A1%9C%EC%84%B8%EC%8A%A4-%EB%B6%84%EB%A6%AC-%EA%B3%BC%EC%A0%95)


