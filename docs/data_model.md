# 모바일 집사 데이터 모델

> 앱이 저장하는 데이터와, 그 데이터로 계산하는 값의 **정본**입니다. 초안 문서와 내용이 다르면 이 문서가 우선합니다.
>
> 기준일: 2026-10-28 · 정리: hss900606
>
> 합친 문서: [DB 설계 초안](draft/db_draft.md) (PR [#32], [#51]), [기능 정의 초안](draft/hss900606_draft.md) 4장 · 3.3, 논의 이슈 [#35] · [#36] · [#38] · [#39] · [#40] · [#52], 개발 이슈 [#44] · [#45] · [#48]

## 문서 안내

| 항목 | 내용 |
| --- | --- |
| 읽는 대상 | 도메인·DB·화면 개발자 |
| 작성 범위 | 시즌 1(v1.0)에서 저장하는 모델, 목표 상태 규칙, 계산 규칙. 이후 시즌은 확장 지점만 적습니다 |
| 다른 문서와의 관계 | 화면과 표시 규칙은 [디자인](design.md), 기능 흐름과 완료 기준은 [요구사항 · 기능 명세서](requirement_analysis.md), 결정 이유는 각 GitHub 이슈에 있습니다 |
| 바꾸는 방법 | 이 문서를 고치는 PR을 올립니다. 아직 합의되지 않은 안은 이슈에서 먼저 논의합니다 |

상태 표기는 [디자인](design.md)과 같습니다.

- **확정**: 이슈나 PR에서 합의된 내용. 개발 기준으로 씁니다.
- **임시**: 방향은 정했지만 시안·값이 아직 없거나, 다른 이슈·PR에서 바뀔 수 있는 내용. 이 문서에서는 주로 개발 이슈의 완료 조건에 적혀 그 PR에서 정해질 내용입니다.
- **미정**: 논의가 필요한 내용. 해당 이슈 링크를 붙입니다.

---

## 1. 원칙

> **사용자가 입력한 목표와 행동 기록은 보존하고, 오늘의 할 일과 달성 화면은 그 기록에서 계산합니다.**

| 원칙 | 뜻 |
| --- | --- |
| 기록을 원본으로 삼는다 | `ProgressEntry`를 입력 한 건 단위로 보존합니다. 날짜별 합계만 저장하지 않습니다 |
| 부분 달성도 기록이다 | 하루 분량에 못 미쳐도 기록이 있으면 그날은 기록한 날입니다. 빈 날을 벌점 데이터로 만들지 않습니다 |
| 결과는 저장하고, 진행 단계는 계산한다 | 사용자가 정한 결과(끝냈는지, 왜 끝났는지)는 저장합니다. "마감 지남"처럼 날짜만으로 알 수 있는 것은 저장하지 않습니다 ([#39]) |
| 계산과 저장을 나눈다 | 오늘의 목표, 날짜별 상태, 목표 진행 단계는 Room에 의존하지 않는 순수 Kotlin 함수가 계산합니다 |
| 로컬 온리 | 계정·서버·동기화를 전제로 한 데이터는 두지 않습니다 |
| 미래 기능을 막지 않는다 | `type`, `direction`, `pacingPeriod`는 시즌 1에서 값이 고정되어도 모델에 남깁니다 |

---

## 2. 저장할 것과 계산할 것

| 데이터 | 처리 | 위치 | 상태 |
| --- | --- | --- | --- |
| `Goal` | 저장 | Room `goals` 테이블 | 확정 (모델 [#44]) · 임시 (Room [#48]) |
| `ProgressEntry` | 저장 | Room `progress_entries` 테이블 | 확정 (모델 [#44]) · 임시 (Room [#48]) |
| 마지막으로 펼친 카드 | 저장 | DataStore에 목표 id | 확정 ([#38]) |
| `DailyRecord` (날짜별 기록 상태) | 계산 | — | 임시 ([#45]) |
| 오늘의 목표 (`PeriodQuota`) | 계산 | — | 임시 ([#45]) |
| 목표 진행 단계 (진행 중 / 마감 당일 / 마감 지남) | 계산 | — | 확정 ([#39]) |
| `Butler`, `UserProfile`, `RoutineBlock` 등 | 시즌 1 제외 | 9장 참고 | — |

시즌 1의 영속 데이터는 **테이블 두 개(`goals`, `progress_entries`)** 입니다. `DailyRecord`와 오늘의 목표를 테이블로 만들면 기록을 고칠 때마다 함께 갱신해야 할 값이 늘어나므로 만들지 않습니다.

---

## 3. 모델

### 3.1 `Goal`

| 필드 | 타입 | 시즌 1 값·제약 | 상태 |
| --- | --- | --- | --- |
| `id` | `String` | PK. UUID 문자열 | 확정 ([#44]), 생성 위치 미정 ([#44]) |
| `title` | `String` | 공백 불가 | 확정 ([#44]) |
| `type` | `GoalType` | `CUMULATIVE` 고정 | 확정 ([#36]) |
| `direction` | `Direction` | `UP` 고정 | 확정 ([#36]) |
| `pacingPeriod` | `PacingPeriod` | `DAY` 고정 | 확정 ([#36]) |
| `unit` | `String` | 표시 단위 ("쪽", "분", "보") | 확정 ([#44]) |
| `targetValue` | `Double` | 목표량. `startValue`보다 커야 함 | 확정 ([#44]) |
| `startValue` | `Double` | 기본값 `0.0` | 확정 ([#44]) |
| `startDate` | `LocalDate` | 목표 시작 날짜 | 확정 ([#44]) |
| `dueDate` | `LocalDate?` | 모델은 nullable, **시즌 1 입력은 필수**. `startDate` 이후 | 확정 ([#44]) |
| `measurementCadence` | `Cadence?` | 도달형 전용. 시즌 1은 `null` | 확정 ([#44]) |
| `trackingMode` | `TrackingMode` | `MANUAL` 고정 | 확정 ([#44]) |
| `healthDataType` | `HealthDataType?` | 자동 연동 전용. 시즌 1은 `null` | 확정 ([#44]) |
| `priority` | `Int` | 목표 순서. 위에서 3개가 오늘 탭에 펼쳐짐 | 확정 ([#38], FUNC-04), 같은 값일 때 정렬 미정 ([#44]) |
| `restDays` | `Set<DayOfWeek>` | 시즌 1은 빈 집합 | 확정 ([#44]) |
| `status` | `GoalStatus` | `ACTIVE` / `COMPLETED` | 확정 ([#39]) |
| `endReason` | `EndReason?` | 끝난 이유. 4장 참고 | 확정 ([#39], [#44]) |
| `endedAt` | `LocalDate?` | **사용자가 끝낸 날**. 마감일이 아님. 4장 참고 | 확정 ([#39], [#44]) |
| `overdueNoticeShown` | `Boolean` | 마감 지난 목표의 첫 안내 팝업을 보여 주었는지. 기본 `false` | 확정 ([#44]) |

`status`와 끝난 결과 필드는 항상 함께 움직입니다. **확정 ([#44])**

| `status` | `endReason` | `endedAt` |
| --- | --- | --- |
| `ACTIVE` | `null` | `null` |
| `COMPLETED` | 값 있음 | 값 있음 |

### 3.2 `ProgressEntry` — 확정 ([#44])

| 필드 | 타입 | 시즌 1 값·제약 |
| --- | --- | --- |
| `id` | `String` | PK. UUID 문자열 |
| `goalId` | `String` | FK → `Goal.id` |
| `recordedAt` | `Instant` | 기록 시각 |
| `recordedOffset` | `ZoneOffset` | 기록할 때 기기 시간대의 UTC offset (예: `+09:00`). **확정 ([#52])** |
| `amount` | `Double` | 기록한 양. `0`보다 커야 함 |
| `source` | `EntrySource` | `MANUAL` 고정 |

- 기록의 날짜는 `recordedAt`을 **기록할 때의 offset**으로 바꾼 로컬 날짜입니다 (`recordedAt.atOffset(recordedOffset).toLocalDate()`). 나중에 기기 시간대가 바뀌어도 이미 칠해진 칸은 옮겨지지 않습니다. **확정 ([#52])**
- 사용자가 기록을 고칠 때 바꾸는 것은 **날짜**입니다. 기록할 때의 로컬 시각과 `recordedOffset`은 그대로 두고 날짜만 바꿔 `recordedAt`을 다시 계산합니다. **확정 ([#52])**

- `Goal` 1 : N `ProgressEntry`입니다. 같은 날 아침 5쪽, 저녁 10쪽을 기록하면 **두 건**으로 남깁니다.
- 마감이 지났지만 아직 `ACTIVE`인 목표에도 기록을 받습니다. **확정 ([#39])**
- `COMPLETED` 목표의 기록은 **읽기 전용**입니다. 추가·수정·삭제할 수 없습니다. **확정 ([#39])**
- 목표량이나 마감일을 고쳐도 이전 기록은 그대로 둡니다. **확정 (DB 초안 3.1)**

### 3.3 enum

```kotlin
enum class GoalType { CUMULATIVE, TARGET_VALUE, RECURRING, FREQUENCY, CHECKLIST }
enum class Direction { UP, DOWN }
enum class PacingPeriod { DAY, WEEK, MONTH }
enum class TrackingMode { MANUAL, AUTO }
enum class EntrySource { MANUAL, HEALTH_CONNECT, NOTIFICATION_ACTION }

enum class GoalStatus {
    ACTIVE,
    COMPLETED,

    @Deprecated("끝난 목표는 COMPLETED와 endReason으로 구분합니다 (#39). 새 코드에서 쓰지 않습니다.")
    ARCHIVED,
}

enum class EndReason {
    ACHIEVED, // 기록으로 목표량에 도달 (보관함 태그 "달성")
    DEADLINE, // 마감이 지난 뒤 "여기서 마무리" 또는 직접 끝내기 (보관함 태그 "잠시 덮어둠")
    MANUAL,   // 마감 전에 직접 끝냄 (보관함 태그 "조기 종료")
}
```

| enum | 시즌 1에 쓰는 값 | 상태 |
| --- | --- | --- |
| `GoalType`, `Direction`, `PacingPeriod`, `TrackingMode`, `EntrySource` | `CUMULATIVE`, `UP`, `DAY`, `MANUAL`, `MANUAL` | 확정 ([#36], [#44]) |
| `GoalStatus` | `ACTIVE`, `COMPLETED`. `ARCHIVED`는 DEPRECATED | 확정 ([#39]) |
| `EndReason` | 세 값 모두. 보관함에 보이는 태그 문구는 [디자인](design.md) G-04 | 확정 ([#39], [#44]) |

- 값의 목록은 [기능 정의 초안](draft/hss900606_draft.md) 4장을 따릅니다.
- `ARCHIVED`를 바로 지우지 않는 이유: enum은 이름 문자열로 저장하므로(7장), 값을 지우면 마이그레이션이 필요합니다. 새 코드에서는 쓰지 않습니다.

---

## 4. 목표 상태

### 4.1 진행 단계 (계산) — 확정 ([#39])

`ACTIVE` 목표에만 계산합니다. 저장하지 않으므로 자정에 상태를 바꾸는 작업(WorkManager)이 필요 없습니다.

| 단계 | 조건 | 오늘의 목표 |
| --- | --- | --- |
| 진행 중 | 오늘 < `dueDate` | 남은 양 ÷ 남은 일수 (5.1) |
| 마감 당일 | 오늘 = `dueDate` | 남은 일수를 1일로 세어, 남은 양 전부 |
| 마감 지남 | 오늘 > `dueDate` | 없음. 안내 팝업과 "이어서" / "여기서 마무리" |

```kotlin
enum class GoalPhase { IN_PROGRESS, DUE_TODAY, OVERDUE }

fun phaseOf(goal: Goal, today: LocalDate): GoalPhase {
    val due = goal.dueDate ?: return GoalPhase.IN_PROGRESS
    return when {
        today < due -> GoalPhase.IN_PROGRESS
        today == due -> GoalPhase.DUE_TODAY
        else -> GoalPhase.OVERDUE
    }
}
```

> 위 코드는 [#39] 코멘트의 예시입니다. 함수와 enum 이름은 [#45] PR에서 정합니다.

### 4.2 상태 전이표

[#39]에서 bobfriendai님이 작성한 표를 기준으로, 이후 결정을 반영했습니다. "오늘"과 "사용자가 고른 날"은 모두 기기 로컬 날짜입니다(5.3).

| # | 마감 | 사건 | 결과 `status` | `endReason` | `endedAt` | 그 밖의 변화 | 상태 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 전 | 기록 추가, 목표 미도달 | `ACTIVE` | — | — | 진행률 갱신 | 확정 |
| 2 | 전 | 기록 추가로 목표 도달 | `COMPLETED` | `ACHIEVED` | 오늘 | 완료 안내 | 확정 |
| 3 | 전 | 직접 끝내기 | `COMPLETED` | `MANUAL` | 오늘 | 보관함에 "조기 종료" 태그 | 확정 |
| 4 | 지남 | 앱을 열거나 홈으로 돌아와 처음 감지 | `ACTIVE` | — | — | 안내 팝업 1회, `overdueNoticeShown = true` | 확정 ([#44]) |
| 5 | 지남 | "이어서" | `ACTIVE` | — | — | 새 `dueDate` 저장(오늘 이후), 하루 분량 다시 계산 | 확정 |
| 6 | 지남 | "여기서 마무리" | `COMPLETED` | `DEADLINE` | **사용자가 고른 날** | 보관함으로 이동, "잠시 덮어둠" 태그 | 확정 |
| 7 | 지남 | 팝업에서 고르지 않고 닫기 | `ACTIVE` | — | — | 카드에 "이어서" / "여기서 마무리" 버튼 계속 표시 | 확정 |
| 8 | 지남 | 직접 끝내기 | `COMPLETED` | `DEADLINE` | **사용자가 고른 날** | 6과 같음 | 확정 |
| 9 | 지남 | 기록 추가로 목표 도달 | `COMPLETED` | `ACHIEVED` | 오늘 | 완료 안내 | 확정 |
| 10 | 지남 | 기록 추가, 목표 미도달 | `ACTIVE` | — | — | 하루 분량 대신 스테퍼로 한 만큼만 기록 | 확정 |
| 11 | 전 | 목표량을 지금까지의 기록보다 낮게 수정 | 확인 후 "예": `COMPLETED` / "아니오": `ACTIVE` | `ACHIEVED` / — | 오늘 / — | "목표가 즉시 완료됩니다. 변경하시겠습니까?" | 확정 |
| 12 | — | 완료한 목표의 기록 수정·삭제 | `COMPLETED` | 그대로 | 그대로 | 불가 (읽기 전용) | 확정 |
| 13 | — | 되돌리기 | `ACTIVE` | `null` | `null` | 기록을 다시 고칠 수 있음 | 결과는 확정, 위치·기간은 미정 (4.3) |

- 5행의 `overdueNoticeShown`을 `false`로 되돌리는 것은 [#45] 전이표에만 있습니다. **임시 ([#45])**
- 4~10행의 감지 시점은 4.4를 따릅니다.

[#39] 원문 표에서 고친 곳

| 행 | 원문 | 정본 | 근거 |
| --- | --- | --- | --- |
| 6 | 결과 상태 `COMPELTED` | `COMPLETED` | 오타 |
| 6, 8 | `endedAt` = 마감일 | `endedAt` = 사용자가 고른 날 | 마감일은 `dueDate`에 이미 있으므로 `endedAt`에는 실제로 끝낸 날을 남깁니다 ([#39] 코멘트 세부 규칙 2, [#45] 전이표도 같음) |
| 12 | 완료 후 기록을 고쳐 합계가 목표 아래로 내려가는 경우 | 완료한 목표의 기록은 읽기 전용 | "조기 달성" 별도 상태를 만들지 않고 읽기 전용 + 되돌리기로 대신합니다 ([#39] 코멘트 세부 규칙 3) |
| 13 | 없음 | 되돌리기 추가 | [#39] 추가 결정 3 |

### 4.3 되돌리기 — 미정 ([#39], [#45])

두 이슈의 내용이 다릅니다. 결과(`ACTIVE`로, `endReason`·`endedAt`은 `null`로)는 같고, 언제·어디서 되돌릴 수 있는지와 무엇을 함께 취소하는지가 다릅니다.

| 쟁점 | [#39] 추가 결정 3 | [#45] 전이표 |
| --- | --- | --- |
| 기간 | 기간 제한 없음 | 완료 직후에만 |
| 버튼 위치 | 보관함 상세 화면, 확인 팝업 한 번 | 완료 직후 스낵바 |
| 함께 취소하는 것 | 상태만 되돌림. 기록은 다시 고칠 수 있게 됨 | 직전 기록 또는 "여기서 마무리"를 취소 |

두 방식은 함께 둘 수도 있습니다(완료 직후 스낵바 + 보관함 상세 버튼). [#45] PR 전에 정합니다.

### 4.4 평가 시점

| 규칙 | 상태 |
| --- | --- |
| 날짜가 바뀐 뒤 앱을 열거나 홈으로 돌아올 때 진행 단계를 계산하고, 필요하면 안내 팝업을 띄웁니다. 자정에 도는 작업은 없습니다 | 확정 ([#39], [#40]) |
| 기록 추가·수정·삭제, 목표 수정 때도 평가합니다 | 임시 ([#45]) |
| `ACTIVE` 목표만 평가합니다. `COMPLETED`는 계산 결과로 덮어쓰지 않고, 되돌리기로만 `ACTIVE`가 됩니다 | 확정 ([#39]) |
| 평가 결과가 저장된 상태·플래그와 다를 때만 전이로 보고, 전이표의 칸마다 단위 테스트를 둡니다 | 임시 ([#45]) |

---

## 5. 계산 규칙

### 5.1 오늘의 목표

| 규칙 | 상태 |
| --- | --- |
| 시즌 1의 계산 방식은 누적형 합산 방식 하나입니다 | 확정 ([#36]) |
| 오늘의 목표 = `min(ceil((목표 − 누적) / 남은 일수), 남은 양)`. 나누어떨어지지 않으면 올림 | 임시 ([#45]) |
| 남은 일수는 오늘을 포함해 셉니다. 마감 당일은 1일이라 남은 양 전부가 오늘의 목표입니다 | 확정 ([#39]) |
| "누적"은 오늘 이전까지의 기록 합입니다. 오늘 기록해도 오늘의 목표는 하루 동안 바뀌지 않습니다 | 임시 ([#45]) |
| 이미 달성했으면 0. 목표를 넘겨 기록해도 음수가 되지 않습니다 | 확정 (DB 초안 3.4) |
| 마감이 지났으면 나누기를 하지 않고 "마감 지남" 결과를 돌려줍니다 | 확정 ([#39]) |
| 시즌 1의 목표량·기록량은 정수입니다. 저장 타입은 `Double` 그대로입니다 | 임시 ([#45]) |
| 쉬는 날(`restDays`) 제외는 시즌 1에 없습니다 | 확정 ([#44]) |
| 지난 날짜의 목표(그날의 목표)를 그날 값으로 고정할지, 지금 기준으로 다시 계산할지 | 미정 ([#45]) |

### 5.2 날짜별 기록 상태 (`DailyRecord`)

| `RecordState` | 조건 | 화면 |
| --- | --- | --- |
| `FULL` | 그날 기록 합계 ≥ 그날의 목표 | 한 날 |
| `PARTIAL` | 기록은 있지만 그날의 목표 미만 | 한 날 (`FULL`과 같은 모양) |
| `NONE` | 기록 없음 | 기록 없음 |

| 규칙 | 상태 |
| --- | --- |
| 화면에는 "한 날" / "기록 없음" 두 가지만 보입니다. 모양은 [디자인](design.md) 3.1 | 확정 ([#35]) |
| 오늘 칸은 기록 여부로만 구분합니다. 자정이 지나면 기록 없는 오늘은 `NONE`이 됩니다 | 확정 ([#35]) |
| "14일 중 N일 기록"에는 오늘을 기록했을 때만 포함합니다 | 확정 ([#35]) |
| `RecordState` 계산은 `direction`을 인자로 받아 방향별로 나눌 수 있게 하고, 시즌 1은 `UP`만 구현합니다 | 확정 ([#40]) |
| 줄이기(`DOWN`) 목표는 기록 없는 지난 날을 "지킨 날"로 셉니다 (시즌 2) | 확정 ([#40]) |

### 5.3 날짜 기준

| 규칙 | 상태 |
| --- | --- |
| "오늘"은 **지금** 기기 시간대의 로컬 날짜(00시 기준)입니다 | 확정 ([#40], [#52]) |
| 기록의 날짜는 **기록할 때의** 시간대(`recordedOffset`)로 셉니다. 지금 기기 시간대로 다시 계산하지 않습니다 (3.2) | 확정 ([#52]) |
| `startDate`, `dueDate`, `endedAt`은 `LocalDate`라 시간대와 관계없습니다 | 확정 ([#44]) |
| 계산 함수는 `Clock`을 받아 씁니다 | 임시 ([#45]) |

---

## 6. 화면 상태 저장

| 상태 | 저장 위치 | 상태 |
| --- | --- | --- |
| 지금 펼친 카드 | ViewModel | 확정 |
| 마지막으로 펼친 카드 | DataStore에 목표 id. 그 목표가 끝났거나 없으면 첫 번째 카드 | 확정 ([#38]) |
| 카드 순서 | `Goal.priority` | 확정 (FUNC-04) |
| 마감 지남 여부 | 저장하지 않음 (4.1) | 확정 ([#39]) |
| 마감 지남 첫 팝업을 보았는지 | `Goal.overdueNoticeShown` | 확정 ([#44]) |

---

## 7. 저장 표현 (Room)

| 규칙 | 상태 |
| --- | --- |
| Room Entity는 도메인 모델과 분리하고 mapper로 변환합니다 | 임시 ([#48]) |
| `LocalDate` ↔ `YYYY-MM-DD` 문자열, `Instant` ↔ epoch ms `Long`, `ZoneOffset` ↔ 초 단위 `Int`(`totalSeconds`), enum ↔ `name` 문자열 | 임시 ([#48]) |
| `Set<DayOfWeek>` 저장 형식(문자열 / 비트마스크) | 미정 ([#48]) |
| FK `progress_entries.goalId → goals.id`. `ON DELETE CASCADE`는 쓰지 않습니다 | 임시 ([#48]) |
| 인덱스 `goals(status, priority)`, `progress_entries(goalId, recordedAt)` | 임시 ([#48]) |
| `exportSchema = true`로 스키마 json을 저장소에 둡니다. 개발 중에는 스키마가 바뀌면 앱을 지우고 다시 설치해도 됩니다 | 임시 ([#48]) |
| 끝난 목표의 기록은 지우지 않습니다 | 확정 ([#39]) |
| 목표 영구 삭제를 허용할지, 허용하면 기록까지 지울지 | 미정 ([#48]) |

---

## 8. Repository 인터페이스

| 규칙 | 상태 |
| --- | --- |
| 인터페이스는 `domain/repository/`에 `GoalRepository`, `ProgressRepository` 두 개를 둡니다 | 확정 ([#44]) |
| UI와 ViewModel은 DAO를 직접 부르지 않고 Repository만 씁니다 | 확정 (DB 초안 5장) |
| 함수 목록은 [DB 초안](draft/db_draft.md) 5.3 예시를 기준으로, 마감일 변경·상태 변경·우선순위 저장 함수를 더합니다 | 미정 ([#44]) |
| DB 초안 5.3의 `archiveGoal(id)`: `ARCHIVED`를 쓰지 않기로 해서 이름과 역할(끝내기, 되돌리기)을 다시 정해야 합니다 | 미정 ([#44]) |

---

## 9. 시즌 2 이후 확장 지점

이번에 만들 것이 아니라, 지금의 경계가 나중에 어떻게 넓어지는지입니다. 자세한 안은 [DB 초안](draft/db_draft.md) 7장에 있습니다.

| 확장 | 바뀌는 곳 | 논의 |
| --- | --- | --- |
| 반복·빈도·줄이기 목표 | `type`, `direction`, `pacingPeriod` 값 확장. 진행 단계 계산을 유형별로 나눔 | [#36], [#39] (nlime3141592님 "하루 한 번 체크형" 조건표) |
| 줄이기 목표가 하루 중 상한을 넘겼을 때 | 오늘 칸 계산 | [#40] |
| 집사 설정, 생활 패턴 | `Butler`, `UserProfile` (DataStore 후보) | 시즌 2·3 |
| 일정·TO-DO | 요일별 반복 블록, 날짜별 예외 | [#37] |
| Health Connect | `ProgressEntry`에 외부 기록 id 추가 필요 | 시즌 미정 |

---

## 10. 미정 사항

| # | 질문 | 논의 |
| --- | --- | --- |
| 1 | Repository 함수 목록, `archiveGoal(id)`의 이름과 역할 | [#44] |
| 2 | `id`(UUID)를 어디서 만드는지, `priority`가 같을 때의 정렬 | [#44] |
| 3 | 되돌리기의 기간·버튼 위치·함께 취소하는 것 | [#39], [#45] |
| 4 | 지난 날짜의 목표(그날의 목표)를 그날 값으로 고정할지 | [#45] |
| 5 | 목표 영구 삭제 허용 여부와 기록 처리, FK 삭제 방식 | [#48] |
| 6 | `Set<DayOfWeek>` 저장 형식 | [#48] |

---

## 변경 이력

| 날짜 | 내용 |
| --- | --- |
| 2026-10-27 | 초안 작성. DB 설계 초안과 #35·#36·#38·#39·#40 결정, #44·#45·#48 완료 조건을 반영. #39 상태 전이표 6행 오타(`COMPELTED`)와 6·8행 `endedAt`(마감일 → 사용자가 고른 날)을 고침 |
| 2026-10-28 | `EndReason`별 보관함 태그 문구(달성 / 잠시 덮어둠 / 조기 종료)를 적음. "임시" 정의를 디자인과 맞추고, `unit` 예시 "걸음" → "보" |
| 2026-10-28 | [#52] 결정(C안) 반영: `ProgressEntry.recordedOffset` 추가, 기록 날짜는 기록할 때의 시간대로 계산, 기록 수정은 날짜만. 5.3 날짜 기준과 7장 저장 형식 갱신, 10장 미정 7번 삭제 |
| 2026-10-28 | 용어 "오늘 할 몫"을 화면 문구와 같은 "오늘의 목표"로, 날짜별로 말할 때는 "그날의 목표"로 바꿈. 코드 이름 `PeriodQuota`는 그대로 |

[#32]: https://github.com/myaring/mobile-butler/pull/32
[#51]: https://github.com/myaring/mobile-butler/pull/51
[#35]: https://github.com/myaring/mobile-butler/issues/35
[#36]: https://github.com/myaring/mobile-butler/issues/36
[#37]: https://github.com/myaring/mobile-butler/issues/37
[#38]: https://github.com/myaring/mobile-butler/issues/38
[#39]: https://github.com/myaring/mobile-butler/issues/39
[#40]: https://github.com/myaring/mobile-butler/issues/40
[#44]: https://github.com/myaring/mobile-butler/issues/44
[#45]: https://github.com/myaring/mobile-butler/issues/45
[#48]: https://github.com/myaring/mobile-butler/issues/48
[#52]: https://github.com/myaring/mobile-butler/issues/52
