# 2026-11-06 [B-7] GoalListItem 컴포넌트 구현

- 작업자: Aetherx86
- 관련 이슈: #62

## 작업 내용

1. `GoalListItem` 및 `DragHandleIcon` 컴포넌트 구현
   - 순서 변경 손잡이 자리 (`DragHandleIcon`, Canvas 기반 3줄 라인 렌더링)
   - 제목 및 종류 라벨(누적/하루) 표시
   - 한 줄 요약(`summary`) 텍스트
   - `MbProgressBar` 연동 진행률 바 표시 및 접근성 시맨틱 제공
   - 오른쪽 상세 이동 화살표 아이콘 및 카드 클릭 콜백 처리

2. 단위 테스트 작성
   - `GoalListItemTest`: 시맨틱 텍스트 이모지 부재 검증, 진행률 클램프 및 퍼센트 포맷 검증
