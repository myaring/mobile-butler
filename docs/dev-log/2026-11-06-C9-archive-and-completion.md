# 2026-11-06 [C-9] 완료 처리와 보관함 구현

- 작업자: Aetherx86
- 관련 이슈: #75

## 작업 내용

1. `GoalArchiveTab` 컴포넌트 구현
   - `COMPLETED` 상태 목표들의 목록을 표시
   - 종료 사유별 뱃지 표시 (`EndReason.MANUAL`: "조기 종료", `EndReason.ACHIEVED`: "목표 달성", `EndReason.DEADLINE`: "기한 종료")
   - 달성한 양만 직관적으로 표시하고 목표 대비 %는 상세 화면으로 한정 (실패/미완료 부정적 어휘 배제 원칙 준수)
   - 행 클릭 시 `GoalScreenViewModel.selectGoal` 호출로 상세 화면(읽기 전용) 이동

2. 직접 종료 및 되돌리기 로직 구현
   - `completeGoalDirectly`: 마감일 전 직접 종료 시 `MANUAL`, 마감일 후 종료 시 `DEADLINE`, 목표량 도달 시 `ACHIEVED` 사유 지정 및 오늘 일자로 `endedAt` 기록
   - `undoGoalCompletion`: 완료 직후 스낵바 "되돌리기" 액션 연동으로 `ACTIVE` 상태 및 날짜/사유 초기화
   - 보관함 이동 후에도 기존 기록 데이터는 보존

3. 단위 테스트 작성
   - `GoalArchiveTest`: 마감일 전후 종료 사유 구분, 목표 달성 사유 검증, 직후 되돌리기 복원 검증, 보관함 필터링 및 텍스트 이모지 부재 검증
