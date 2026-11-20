# [C-2] TodayViewModel 구현

- 일자: 2026-11-06
- 작업자: Aetherx86
- 관련: #67

## 한 일

- 오늘 화면의 UI 상태를 관리하는 `TodayScreenViewModel` 및 `TodayUiState` 구현
- 목표를 우선순위 오름차순으로 정렬하여 상위 3개(`topGoals`)와 나머지(`remainingGoals`)로 분리
- 펼쳐진 카드 상태(`expandedGoalId`)를 영속화하지 않고 ViewModel 메모리 상태로 관리 (기본값: 우선순위 1번)
- `domain/pacing`의 `PacingCalculator`, `ProgressStatistics`를 연동하여 카드별 오늘 몫, 진행률, D-day, 14일 상태 계산
- 날짜/클럭 변경 시 자동 재계산 지원 (`setClock`)
- `TodayScreenViewModelTest` 7개 단위 테스트 작성 및 통과 확인

## 알게 된 것 / 막힌 것

- 상위 3개와 나머지 목표를 명확히 분리하여 노출하고 카드 펼침 상태를 일시적 뷰 상태로 격리함으로써 도메인 모델의 순수성을 보존하고 깔끔한 상태 단방향 흐름(UDF)을 구성했습니다.
