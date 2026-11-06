# [C-1] 오늘 화면 조립 구현

- 일자: 2026-11-06
- 작업자: Aetherx86
- 관련: #66

## 한 일

- design T-01 및 T-01b 시안 기준 `TodayScreenContent` 및 `TodayScreenRouter` 구현
- 위에서 아래로 순서 배치:
  1. 상단 앱 바 연동
  2. 집사 한마디 `ButlerLine`
  3. 펼친 카드 1개 `GoalCardExpanded`
  4. 접힌 카드들 `GoalCardCollapsed` (처음에 3개까지 노출)
  5. 나머지 목표가 있을 경우 "목표 N개 더보기" 텍스트 버튼 및 펼침/접힘 토글
- 접힌 카드 탭 시 해당 카드로 펼침 전환 및 기존 펼친 카드 접힘 처리
- 펼친 카드의 제목 줄 탭 시 접힘 처리
- 펼친 카드 우측 "자세히" 버튼 상세 이동 콜백 연동
- 접힌 카드의 테두리 `기록` 버튼 탭 시 카드를 펼치며 `RecordSheet` 바텀시트 오픈 연동
- 가짜 상태 기반 `@Preview` (`TodayScreenRouterPreview`) 작성
- `TodayScreenAssemblyTest` 단위 테스트 작성 및 통과 확인

## 알게 된 것 / 막힌 것

- 상위 3개와 나머지 목표를 시각적으로 분리하고, 화면 내에서 동시에 여러 카드가 펼쳐지지 않도록 단일 펼침 원칙을 유지하여 모바일 화면에서의 인지 부하를 줄였습니다.
