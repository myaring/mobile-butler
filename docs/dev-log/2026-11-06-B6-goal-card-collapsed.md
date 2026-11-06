# [B-6] GoalCard 접힌 상태 컴포넌트 구현

- 일자: 2026-11-06
- 작업자: Aetherx86
- 관련: #61

## 한 일

- design T-01 및 T-01b 시안 기준 접힌 카드 `GoalCardCollapsed` 컴포넌트 구현
- 좌측 아이콘, 제목, 오늘의 목표 및 D-day 한 줄 요약 렌더링
- 카드 본체 탭 시 호출되는 `onCardClick` 콜백 연결 (펼침 전환)
- 우측 테두리 `기록` 버튼(`MbButtonStyle.Outlined`) 및 `onRecordClick` 콜백 연결 (시트 직접 열기)
- `GoalCardCollapsedTest` 단위 테스트 작성 및 통과 확인

## 알게 된 것 / 막힌 것

- 접힌 카드는 최소한의 공간에서 오늘 할 일과 마감까지 남은 일수를 한눈에 파악할 수 있도록 텍스트 요약에 집중하고, 우측에 즉시 기록할 수 있는 테두리 버튼을 배치하여 인터랙션 편의성을 높였습니다.
