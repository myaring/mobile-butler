# 2026-11-06 [C-5] 목표 탭 목록 및 탭 행 구현

- 작업자: Aetherx86
- 관련 이슈: #71

## 작업 내용

1. `GoalListTab` 및 목표 탭 UI 구현
   - "진행 중 N/보관함" 상단 탭 행 구성 (`PrimaryTabRow` 및 활성 목표 개수 실시간 표시)
   - `GoalListItem` 목록 연동 (목표 제목, 종류 라벨, 목표치 요약, 진행 바)
   - 행 클릭 시 `GoalScreenViewModel.selectGoal` 호출 후 목표 상세 탭으로 전환
   - 우측 하단 `ExtendedFloatingActionButton` ("목표 추가", 노란색 `MbPrimary` 컨테이너) 배치 및 목표 생성 탭 전환 연동
   - 목표 미등록 시 빈 상태 안내 문구 제공

2. 단위 테스트 작성
   - `GoalScreenViewModelTest`: 초기 상태 및 활성 목표 집계, 목표 선택 후 상세 전환, 보관함 및 생성 탭 전환, 뒤로 가기 동작 검증
