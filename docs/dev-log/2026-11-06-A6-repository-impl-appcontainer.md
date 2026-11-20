# 2026-11-06 [A-6] Repository 구현체 및 AppContainer 바인딩

## 작업 개요
- 이슈: #49 [A-6] Repository 구현체 및 AppContainer
- 선행 이슈: #48 [A-5] Room 영속화
- 구현 내용:
  1. Room 기반 GoalRepositoryImpl, ProgressRepositoryImpl 구현
  2. MobileButlerApplication 및 AppContainer에서 Clock 주입 및 Repository 제공
  3. 공통 ViewModelProvider.Factory 구현 및 TodayScreen ViewModel 연동 예제 작성

## 세부 변경 사항
1. GoalRepositoryImpl, ProgressRepositoryImpl
   - RoomGoalRepository 및 RoomProgressRepository를 상속하는 구현체 제공
   - GoalRepository 및 ProgressRepository 인터페이스 구현 충족 및 Room Flow 연결
2. AppContainer
   - Clock(기본값: Clock.systemDefaultZone()), ButlerDatabase, GoalRepository, ProgressRepository를 필드로 노출
   - viewModelFactory { } DSL을 이용해 TodayScreenViewModel 등에 저장소와 Clock 주입 지원
3. MobileButlerApplication
   - onCreate 시점에 Clock.systemDefaultZone()을 주입하여 AppContainer 초기화
4. TodayScreen
   - TodayScreenViewModel에 goalRepository, progressRepository, clock 주입 인자 지원
   - TodayScreenRouter에서 context.appContainer.viewModelFactory를 통해 인스턴스 획득
5. 단위 테스트
   - AppContainerTest 추가: GoalRepositoryImpl/ProgressRepositoryImpl Flow 동작, AppContainer 의존성 주입, ViewModelFactory 동작 검증
