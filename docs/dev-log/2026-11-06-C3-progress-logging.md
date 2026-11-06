# 2026-11-06 [C-3] 기록 저장 흐름 및 되돌리기

- 작업자: Aetherx86
- 관련 이슈: #68

## 작업 내용

1. `ProgressRecordFlow` 도메인 흐름 구현
   - `saveRecord`: "남은 몫 모두 완료" 및 "일부만 기록"을 `ProgressRepository`에 저장
   - 목표량 도달 시 `GoalTransitionRule.reduce`를 통해 상태를 `COMPLETED`(`ACHIEVED`)로 전환하고 `GoalRepository.completeGoal` 호출
   - 스낵바 안내 문구("오늘 N단위를 모두 기록했습니다") 생성 및 결과 반환
   - `undoRecord`: 방금 저장한 단일 기록 삭제 및 목표가 완료 상태로 전환되었던 경우 `ACTIVE` 상태로 복구(`reopenGoal`)

2. 단위 테스트 작성
   - `ProgressRecordFlowTest`: 일반 기록 저장, 목표 달성 자동 완료, 0 이하 값 검증, 단일 기록 되돌리기 및 목표 재오픈 검증
