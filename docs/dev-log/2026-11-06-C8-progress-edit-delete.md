# 2026-11-06 [C-8] 기록 수정 및 삭제 구현

- 작업자: Aetherx86
- 관련 이슈: #74

## 작업 내용

1. `EditRecordSheet` 컴포넌트 구현
   - 활성(`ACTIVE`) 목표의 기록 이력에서 연필 버튼 클릭 시 호출
   - 정수 단위 스테퍼(-/+)를 통한 기록량 수정 및 "수정 완료" 버튼 제공
   - "기록 삭제" 버튼을 통한 단일 기록 삭제 제공
   - 바깥 터치 또는 아래 스와이프 시 닫힘 처리

2. `GoalScreenViewModel` 수정 및 삭제 비즈니스 로직 연동
   - `updateProgressEntry`: 기록량 변경 후 총합 재계산 및 전체 진행률 실시간 반영
   - 수정 결과 목표량에 도달하거나 초과할 경우 `GoalStatus.COMPLETED`(`EndReason.ACHIEVED`) 자동 상태 전이
   - `deleteProgressEntry`: 해당 기록을 목록에서 제거하고 총합 갱신
   - 완료(`COMPLETED`) 목표에서는 연필/삭제 버튼 숨김(읽기 전용) 처리

3. 단위 테스트 작성
   - `ProgressEditDeleteTest`: 수치 변경 반영, 목표치 달성 시 `ACHIEVED` 자동 완료 전이, 기록 삭제, 0 이하 값 예외 검증, UI 텍스트 이모지 부재 검증
