# 2026-11-06 [A-7] 가짜 저장소와 샘플 데이터 팩토리 구현

- 작업자: Aetherx86
- 관련 이슈: #50

## 작업 내용

1. 디버그 전용 가짜 저장소 구현 (`app/src/debug/java/dev/myaring/mobile_butler/data/fake/`)
   - `FakeGoalRepository`: `GoalRepository` 인터페이스의 메모리 기반 StateFlow 구현 (우선순위 정렬, 마감일 수정, 우선순위 갱신, 완료 및 재오픈, 알림 플래그 처리)
   - `FakeProgressRepository`: `ProgressRepository` 인터페이스의 메모리 기반 StateFlow 구현 (추가, 수정, 삭제, 날짜 범위 조회, 관찰)
   - `src/debug` 소스셋에 배치하여 릴리스 빌드에는 제외되고 디버그 환경에서만 동작하도록 격리

2. 샘플 데이터 팩토리 (`SampleDataFactory`) 구현
   - "먀의 투쟁" (734쪽, 마감 11/15, 과거 4건의 누적 기록)
   - "전공 서적 독서" (300쪽, 마감 11/20, 1건 기록)
   - "알고리즘 문제 풀이" (100문제, 마감 11/30)
   - "누적 10만보 걷기" (100000걸음, 마감 11/15, 직접 입력 MANUAL, 1건 기록)
   - 전 항목 누적형(CUMULATIVE) 목표로 구성

3. 단위 테스트 작성 및 릴리스 빌드 검증
   - `FakeRepositoriesTest`: 샘플 데이터 필수 요구사항 충족 검증, 우선순위 정렬, 완료 및 재오픈, 날짜 구간 조회, 이모지 부재 검증
   - `./gradlew assembleRelease` 통과로 릴리스 제외 및 R8 최적화 검증
