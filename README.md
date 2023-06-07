# MOIZA

### 주요 기능

그룹장을 통해 만들어진 모임 생성 방을 통해
각 그룹원들은 가능한 시간과 원하는 장소를 설정하고
최종적으로 모두가 원하는 시간과 장소를 결정할 수 있도록 한다.
결과는 모든 그룹원들에게 알림으로 전송된다.

### 추가 기능

- 카카오톡 로그인 API
- 카카오맵 API
- 채팅방
- 알림 기능
    - 이메일
    - 카톡(유료)
    - 디스코드(미정), 슬랙(미정)
- 구글 캘린더 API (미정)

### 개별 구현 현황

#### 이은혜
UI 구현
- [X] 로그인 페이지
- [X] 메인 페이지
- [X] 모임 생성 페이지
- [X] 모임 참가 페이지
- [X] 투표 및 현황 페이지
    - 투표 페이지
    - 현황 페이지
    - 채팅 페이지
- [ ] 결과 페이지

기능 구현
- [X] 카카오 로그인


- 메인 페이지
    - [X] 그룹 생성
    - [X] 카카오톡 프로필 정보 불러오기
    - [X] 로그아웃
    - [ ] 방 링크 연결: 모임 참여 페이지 구현 후 연결 예정


- 결과 페이지
    - [ ] 그룹 정보 불러오기
    - [ ] 카카오 맵
    - [ ] 공유하기 기능

#### 임영균
- 모임 참여 페이지
- [X] 방 입장
- [X] 시간 선택
- [X] 장소 선택


- 투표 페이지
    - [X] 멤버별로 선택된 시간 리스트 출력(멤버 많은 순)


#### 정상화

- [X] 방 생성 페이지
- [X] 알림 발송 : 마감시간 이후 방 참여자에게 메일 발송

#### 문재원

- 투표 페이지 진행중
- 현황 페이지