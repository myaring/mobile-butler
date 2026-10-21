# 모바일 집사 — 데이터베이스 · 데이터 구조 정의 초안

> 작성: yuhan3958 / 기준일: 2026-10-21
> [기능 정의 초안](hss900606_draft.md)의 4장 데이터 모델과 [요구사항 · 기능 명세서](../requirement_analysis.md)를 바탕으로, 데이터 저장 방식과 코드 경계를 구체화한 문서입니다.
> **확정안이 아니라 협업 논의를 위한 개인 초안**이며, 합의된 설계는 `docs/design.md` 등에 반영합니다.

> **범위 주의**: 이 문서는 시즌 1~4를 고려하되 **시즌 1(v1.0, 목표일 11/15)** 구현을 우선합니다. 시즌 1은 목표 만들기·오늘 화면·기록하기가 중심이며, 이후 시즌의 모델을 이번 구현에 포함한다는 뜻이 아닙니다.

> **문서 우선순위**: 실제 구현 범위·완료 기준은 [요구사항 · 기능 명세서](../requirement_analysis.md)와 팀 합의가 우선합니다. 기능 정의 초안은 장기 방향의 근거이고, **이 문서의 PK·컬럼·인덱스·Repository 구성은 제안**입니다.

---

## 1. 데이터 설계의 한 줄 원칙

> **사용자가 입력한 목표와 행동 기록은 보존하고, 오늘의 할 일과 달성 화면은 그 기록에서 계산합니다.**

이 앱의 핵심은 숫자를 저장하는 것 자체가 아니라, 어제의 작은 진척이 오늘 화면으로 다시 이어지는 경험입니다. 따라서 다음 규칙을 우선합니다.

| 원칙 | 설계상의 의미 |
| --- | --- |
| **기록을 원본으로 삼는다** | `ProgressEntry`를 개별 입력 단위로 보존합니다. 날짜별 합계만 저장하지 않습니다. |
| **부분 달성도 기록이다** | 목표 미달이라도 기록이 있으면 `DailyRecord`에 반영합니다. 빈 날은 벌점 데이터로 만들지 않습니다. |
| **계산과 저장을 분리한다** | 페이싱 계산은 Room 쿼리가 아니라 별도의 순수 Kotlin 계산 로직이 맡습니다. |
| **로컬 온리** | 계정·서버·실시간 동기화를 전제로 한 테이블은 만들지 않습니다. |
| **지금 필요한 것부터** | 시즌 1에는 `Goal`과 `ProgressEntry`를 영속 데이터의 중심으로 사용합니다. |
| **미래 기능을 막지 않는다** | `type`, `direction`, `pacingPeriod`는 시즌 1에서 값이 고정되어도 모델에 남깁니다. |

여기서 가장 중요한 구별은 **모델(model)이 있다고 모두 테이블(table)이 되어야 하는 것은 아니라는 점**입니다.

---

## 2. 저장소 선택과 시즌별 범위

### 2.1 저장할 것과 계산할 것

[기능 정의 초안](hss900606_draft.md)에 등장하는 데이터 모델을 용도에 따라 나누면 다음과 같습니다.

| 모델 | 역할 | 시즌 1 처리 | 이후 시즌 |
| --- | --- | --- | --- |
| `Goal` | 목표 설정과 현재 상태 | **Room 원본 테이블** | 목표 유형 확장 |
| `ProgressEntry` | 사용자가 입력한 분량 기록 | **Room 원본 테이블** | Health Connect·알림 입력 경로 확장 |
| `DailyRecord` | 하루치 기록 합계와 히트맵 상태 | **조회·계산 결과** | 성능 필요 시 캐시 검토 |
| `PeriodQuota` | 오늘/이번 주기의 권장 분량 | **계산 결과** | 주·월 단위 및 `DOWN` 처리 확대 |
| `Butler` | 집사 이름·말투·이미지 | 제외 | 시즌 2, DataStore 후보 |
| `UserProfile` | 생활 패턴·방해 금지 시간 | 제외 | 시즌 3, DataStore 후보 |
| `RoutineBlock` | 생활 패턴 타임라인 | 제외 | 시즌 3, Room 후보 |
| `NotificationSchedule` | 알림 예약 상태 | 제외 | 시즌 2, 저장 정책 검토 |
| `ChecklistItem` | 체크리스트 목표의 항목 | 제외 | 도입 시즌 미정, Room 후보 |

**제안**: v1.0에서는 `goals`와 `progress_entries` **두 테이블부터 시작합니다.** `DailyRecord`와 `PeriodQuota`를 별도 테이블로 만들면, 기록 수정 시 어떤 값을 함께 갱신해야 하는지 문제가 더 많아집니다.

### 2.2 저장 기술의 역할

| 기술 | 맡을 데이터 | 판단 |
| --- | --- | --- |
| **Room(SQLite)** | 목표 및 개별 진행 기록 | 기능 초안의 로컬 DB 후보. 이 문서는 Room을 사용한다고 가정해 구체화합니다. |
| **DataStore** | 향후 집사 설정·생활 패턴·앱 설정 | 시즌 1에서는 필수 아님. 도입 시 타입과 직렬화 정책 합의 |
| **앱 내부 파일 저장소** | 향후 집사 아바타 이미지 | 이미지는 DB에 직접 넣기보다 파일로, 경로는 설정에 저장 |
| **서버 DB** | 없음 | 로컬 온리 결정에 따라 현재 범위 밖 |

> Room을 쓴다고 해서 UI에서 DAO를 곧바로 호출하지는 않습니다. **데이터를 어떻게 보관하는지**와 **화면에서 데이터를 어떻게 쓰는지**를 나누는 것이 협업의 핵심입니다.

---

## 3. 시즌 1 핵심 데이터 모델

### 3.1 `Goal` — 목표 자체를 정의하는 데이터

`Goal`은 한 가지 목표의 기준값과 기간, 정렬·보관 상태를 보관합니다. 아래 필드는 기능 정의 초안의 이름을 최대한 유지했습니다.

| 필드 | Kotlin 타입 제안 | 내용 및 제약 제안 |
| --- | --- | --- |
| `id` | `String` | PK. UUID 문자열 사용 제안 |
| `title` | `String` | 목표명. 공백 문자열 불허 |
| `type` | `GoalType` | 시즌 1: `CUMULATIVE` |
| `direction` | `Direction` | 시즌 1: `UP` |
| `pacingPeriod` | `PacingPeriod` | 시즌 1: `DAY` |
| `unit` | `String` | `쪽`, `분`, `개` 등 표시 단위 |
| `targetValue` | `Double` | 달성할 목표량. 유한한 양수 |
| `startValue` | `Double` | 시작 기준값. 시즌 1 기본값 `0.0` |
| `startDate` | `LocalDate` | 목표 시작 날짜 |
| `dueDate` | `LocalDate?` | 장기 모델상 선택 가능, **시즌 1 입력은 필수** |
| `measurementCadence` | `Cadence?` | 도달형 전용. 시즌 1: `null` |
| `trackingMode` | `TrackingMode` | 시즌 1: `MANUAL` |
| `healthDataType` | `HealthDataType?` | 자동 연동 전용. 시즌 1: `null` |
| `priority` | `Int` | 목표 카드 노출 순서 |
| `restDays` | `Set<DayOfWeek>` | 분량 계산 시 제외할 요일. 시즌 1 적용 범위 협의 필요 |
| `status` | `GoalStatus` | `ACTIVE` / `COMPLETED` / `ARCHIVED` |

**시즌 1에서 사용 가능한 조합**

```kotlin
Goal(
    type = GoalType.CUMULATIVE,
    direction = Direction.UP,
    pacingPeriod = PacingPeriod.DAY,
    trackingMode = TrackingMode.MANUAL,
    // 기타 필드: 사용자 입력 및 기본값
)
```

목표 유형을 나중에 추가하더라도 `Goal`이라는 모델 자체를 갈아엎지 않도록 `type`·`direction`·`pacingPeriod`는 처음부터 구분해 둡니다. 다만 **미래 기능 필드를 모두 화면에 노출하는 것은 아닙니다.**

#### 필드 검사 규칙 (제안)

- 제목은 `isNotBlank()`여야 합니다.
- 시즌 1 누적형은 `targetValue > startValue`를 기본 조건으로 제안합니다.
- `dueDate`는 시즌 1 생성 화면에서 필수이며, `startDate`보다 빠를 수 없습니다.
- `targetValue`, `startValue`는 `NaN`·무한대를 허용하지 않습니다.
- 사용자가 목표량이나 마감일을 수정해도 이전 `ProgressEntry`는 그대로 남깁니다.
- `priority`가 같은 목표가 있을 때의 정렬 규칙은 고정해야 합니다.

> **논의 필요**: 원본 모델의 `restDays`는 장기 기능을 고려한 필드입니다. 시즌 1에서 쉬는 날을 실제로 지원할지, 일단 빈 집합으로 둘지 결정해야 합니다.

### 3.2 `ProgressEntry` — 실제로 한 만큼 남기는 기록

기록은 하루에 한 번만 입력한다고 가정하지 않습니다. 아침에 5쪽, 저녁에 10쪽을 읽었다면 **서로 다른 두 기록**을 유지합니다. 그래야 수정·삭제가 자연스럽습니다.

| 필드 | Kotlin 타입 제안 | 내용 및 제약 제안 |
| --- | --- | --- |
| `id` | `String` | PK. UUID 문자열 제안 |
| `goalId` | `String` | FK → `Goal.id` |
| `recordedAt` | `Instant` | 기록 시각 |
| `amount` | `Double` | 기록한 양. 시즌 1 수동 입력은 `> 0` |
| `source` | `EntrySource` | 시즌 1: `MANUAL` |

```kotlin
enum class EntrySource {
    MANUAL,
    HEALTH_CONNECT,
    NOTIFICATION_ACTION,
}
```

**`Goal 1 : N ProgressEntry`** 관계입니다. 기록 원본을 남겨두면 사용자가 값을 고쳐도 전체 누적량을 다시 계산할 수 있습니다.

**예시**: 독서 목표 380쪽에 대해 오늘 5쪽, 10쪽을 따로 입력했다면 기록 2개가 생성되고, 오늘 합계는 15쪽입니다.

#### 삭제 정책 (미확정)

- `ARCHIVED`는 목표를 숨기거나 비활성화할 뿐, **기록을 지우지 않습니다.**
- 목표의 영구 삭제를 허용한다면 연관 기록까지 지울지, 사용자에게 복구 경고를 보여줄지 합의해야 합니다.
- 따라서 외래키의 `ON DELETE CASCADE`도 아직 확정하지 않습니다.

### 3.3 `DailyRecord` — 일자별 표시를 위한 파생 데이터

홈의 **최근 2주 기록 스트립**과 목표 상세의 **달력 히트맵**에 쓰입니다. 기능 정의 초안의 `DailyRecord` 의미를 유지하지만 시즌 1에서는 테이블이 아니라 기록을 묶어 만드는 결과로 제안합니다.

```kotlin
DailyRecord(
    date: LocalDate,
    goalId: String,
    achievedAmount: Double,
    quota: Double,
    state: RecordState, // FULL / PARTIAL / NONE
)
```

| 상태 | 시즌 1 해석 제안 | 화면에서의 의미 |
| --- | --- | --- |
| `FULL` | 해당 날짜의 기록 합계가 할당량 이상 | 충분히 채워진 칸 |
| `PARTIAL` | 기록은 있지만 할당량 미만 | 부분적으로 채워진 칸 |
| `NONE` | 해당 날짜의 기록이 없음 | 옅은 회색 빈 칸 |

**부분 달성도 기록입니다.** 이 구별은 기록의 존재 여부와 목표 달성 여부를 혼동하지 않기 위해 필요합니다.

다만 **과거 날짜의 `quota`를 현재 기준으로 재계산할지**, 과거 당시의 할당량을 보존할지는 아직 확정하지 않았습니다. 첫 버전의 표시 정책과 함께 결정할 문제입니다.

### 3.4 `PeriodQuota` — 저장이 아니라 계산의 결과

```kotlin
PeriodQuota(
    goalId: String,
    periodStart: LocalDate,
    quota: Double,
    carriedOver: Double,
)
```

기능 정의 초안에서 정한 기본 공식은 다음과 같습니다.

```text
이번 주기 할당량 = ceil((목표치 - 현재 누적) / 남은 주기 수)
```

시즌 1에서는 `DAY`만 다루므로 **오늘의 권장 분량**을 의미합니다. 기록이 추가·수정·삭제되면 계산 결과도 달라질 수 있습니다.

- 목표가 이미 달성되었다면 권장 분량은 0으로 처리합니다.
- 남은 일수가 0 이하인 경우에는 나누기 계산 대신 마감 상태로 분기합니다.
- 마감일 포함 여부, 시작일·휴식일 제외 규칙은 별도 테스트로 확정해야 합니다.
- `carriedOver`의 표현 방식은 계산 엔진과 함께 결정합니다. 현재 테이블은 만들지 않습니다.

---

## 4. Room 관계·테이블 초안

### 4.1 관계도

```text
┌──────────────────────────────┐
│ goals                        │
│ ──────────────────────────── │
│ id (PK)                      │
│ title / targetValue / unit   │
│ startDate / dueDate          │
│ type / direction / period    │
│ priority / status / ...      │
└──────────────┬───────────────┘
               │ 1
               │
               │ N
┌──────────────▼───────────────┐
│ progress_entries             │
│ ──────────────────────────── │
│ id (PK)                      │
│ goalId (FK → goals.id)       │
│ recordedAt / amount / source │
└──────────────────────────────┘

DailyRecord / PeriodQuota = 위 데이터를 읽어 생성한 결과
```

### 4.2 테이블과 인덱스 제안

| 테이블 | 기본키 | 외래키 | 인덱스 후보 |
| --- | --- | --- | --- |
| `goals` | `id` | 없음 | `(status, priority)` |
| `progress_entries` | `id` | `goalId → goals.id` | `(goalId, recordedAt)` |

**조회 방향**은 주로 세 가지입니다.

1. 활성 목표를 우선순위 순서대로 나열합니다.
2. 특정 목표의 기록을 시간순으로 조회합니다.
3. 목표와 날짜 범위를 조건으로 기록을 집계합니다.

따라서 `(goalId, recordedAt)` 복합 인덱스는 시즌 1에도 유용합니다. 정확한 SQL·DAO 메서드와 영구 삭제 규칙은 구현 PR에서 확정합니다.

### 4.3 Kotlin 타입과 SQLite 저장 표현

SQLite에는 `LocalDate`, `Instant`, enum, `Set<DayOfWeek>`를 그대로 담을 수 없으므로 **변환 기준을 하나로 통일**해야 합니다.

| Kotlin 타입 | 저장 형식 제안 | 주의사항 |
| --- | --- | --- |
| `LocalDate` | `YYYY-MM-DD` 문자열 | 날짜 계산에서는 다시 `LocalDate`로 변환 |
| `Instant` | epoch milliseconds `Long` | UTC 기준 시각으로 보관 |
| `enum` | `name` 문자열 | 이름 변경 시 DB 마이그레이션 필요 |
| `Set<DayOfWeek>` | 순서를 고정한 문자열 또는 비트마스크 | 서로 다른 직렬화 방식이 섞이지 않아야 함 |
| `Double` | SQLite `REAL` | 향후 금전 목표는 정밀도 재검토 |

기능 정의 초안의 모델을 그대로 참고하되, 실제 `@Entity`는 이런 저장 표현을 고려해 별도로 작성합니다. **도메인 모델과 Room Entity를 같은 클래스로 만들지**도 논의 대상이지만, 협업 구조에서는 분리를 추천합니다.

---

## 5. 데이터 접근 클래스는 어디에 두는가

여러 사람이 각자 다른 화면을 개발할 때 가장 큰 위험은 **화면마다 직접 Room을 다루는 코드가 생기는 것**입니다. 그러면 DB 변경이 화면 전체에 퍼집니다.

### 5.1 권장 호출 흐름

```text
Compose UI
    ↓
ViewModel
    ↓
Repository 인터페이스
    ↓
Repository 구현체
    ↓
DAO
    ↓
Room Database

PacingCalculator ← Goal + ProgressEntry (Room에 의존하지 않음)
```

UI는 `GoalEntity`를 알 필요가 없습니다. DAO는 화면 상태나 페이싱 계산을 알 필요가 없습니다.

### 5.2 패키지 구조 제안

아래의 최상위 패키지명은 **자리표시자**입니다. 실제 앱 패키지에 맞춰 바꿉니다.

```text
dev.myaring.mobile_butler/
├── data/
│   ├── local/
│   │   ├── ButlerDatabase.kt
│   │   ├── dao/
│   │   │   ├── GoalDao.kt
│   │   │   └── ProgressEntryDao.kt
│   │   ├── entity/
│   │   │   ├── GoalEntity.kt
│   │   │   └── ProgressEntryEntity.kt
│   │   └── converter/
│   │       └── RoomConverters.kt
│   ├── mapper/
│   │   ├── GoalMapper.kt
│   │   └── ProgressEntryMapper.kt
│   └── repository/
│       ├── GoalRepositoryImpl.kt
│       └── ProgressRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Goal.kt
│   │   ├── ProgressEntry.kt
│   │   └── GoalEnums.kt
│   ├── repository/
│   │   ├── GoalRepository.kt
│   │   └── ProgressRepository.kt
│   └── pacing/
│       └── PacingCalculator.kt
│
├── ui/
│   ├── home/
│   ├── goal/
│   └── history/
│
└── di/                  # DI 도입 시
    └── DatabaseModule.kt
```

| 위치 | 맡는 일 | 협업 시 경계 |
| --- | --- | --- |
| `data/local/entity` | 실제 Room 테이블 정의 | 저장 필드·마이그레이션 담당 |
| `data/local/dao` | DB 조회·삽입·수정·삭제 | 쿼리 로직만 작성 |
| `data/local/ButlerDatabase` | Entity·DAO 등록, DB 인스턴스 | 버전·변환기 공통 관리 |
| `data/mapper` | Entity ↔ 도메인 모델 변환 | UI와 DB의 타입 독립 유지 |
| `data/repository` | DAO를 조합한 실제 데이터 접근 | 인터페이스 계약 구현 |
| `domain/repository` | 외부에 노출할 데이터 접근 계약 | UI 담당자가 먼저 참고할 API |
| `domain/pacing` | 오늘 할 분량·재분배 계산 | 순수 Kotlin 테스트 가능 |
| `ui` | 화면·화면 상태 관리 | DAO 직접 접근 금지 |

### 5.3 Repository 계약 예시

아래는 **협업 시 함수명을 합의하기 위한 예시**이지, 확정 API가 아닙니다.

```kotlin
interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>
    suspend fun getGoal(id: String): Goal?
    suspend fun saveGoal(goal: Goal)
    suspend fun archiveGoal(id: String)
}

interface ProgressRepository {
    fun observeEntries(goalId: String): Flow<List<ProgressEntry>>
    suspend fun addEntry(entry: ProgressEntry)
    suspend fun updateEntry(entry: ProgressEntry)
    suspend fun deleteEntry(id: String)
}
```

먼저 **Repository 인터페이스를 합의**하면 DB 담당자는 Room 구현을 진행하고, UI 담당자는 임시 구현체를 이용해 병렬 개발할 수 있습니다.

---

## 6. 핵심 기능별 데이터 흐름

### 6.1 목표 만들기 (`FUNC-01`)

```text
목표명·목표량·마감일 입력
    ↓
유효성 검사
    ↓
Goal 저장 (CUMULATIVE / UP / DAY)
    ↓
오늘 권장량 계산
    ↓
홈 목표 카드 표시
```

### 6.2 기록하기 (`FUNC-03`)와 자동 재계산 (`FUNC-02`)

```text
독서 +5쪽 입력
    ↓
ProgressEntry 1건 INSERT
    ↓
목표별 전체 기록 다시 조회
    ├─ 누적값·남은 목표량 계산
    ├─ 오늘 기록 합계 계산
    ├─ DailyRecord / 히트맵 상태 계산
    └─ PeriodQuota / 오늘 권장량 계산
    ↓
홈과 목표 상세 UI 갱신
```

예를 들어 오늘 23쪽을 권장받은 사용자가 5쪽만 읽어도 오늘 기록 스트립은 빈 칸이 아닙니다. 이후 18쪽을 더 입력하면 같은 날짜의 합산 결과가 23쪽이 되어 `FULL`로 바뀝니다.

### 6.3 기록 수정·삭제

원본 `ProgressEntry`를 수정하거나 삭제한 뒤 **해당 목표의 파생값을 다시 계산**합니다.

- 수정 후 총누적과 남은 목표량이 바뀌어야 합니다.
- 시간을 수정해 날짜가 달라지면 **이전 날짜와 새 날짜**의 히트맵 모두 갱신되어야 합니다.
- 삭제한 기록이 그날의 유일한 기록이었다면 해당 날짜는 `NONE`이 됩니다.

### 6.4 목표 우선순위 (`FUNC-04`)

`priority`를 정렬 기준으로 사용합니다. 앱을 종료했다 다시 실행해도 순서가 유지되어야 하므로 **화면 상태만 바꾸는 게 아니라 `Goal`의 저장값도 갱신**해야 합니다.

### 6.5 계산 시점

페이싱은 매번 DB에 숫자를 써두는 방식보다 **목표·기록·오늘 날짜를 인수로 받아 재계산**하는 편이 단순합니다.

> 자정이 지나거나 날짜가 바뀌었는데 DB 행이 수정되지 않았다는 이유로 화면의 ‘오늘 할 분량’이 어제 값으로 남으면 안 됩니다. 날짜 변경을 UI가 어떻게 감지할지도 별도 구현 논의가 필요합니다.

---

## 7. 시즌 2~4를 위한 확장 지점

이 절은 이번에 구현할 테이블 목록이 아니라, **지금의 경계가 나중에 어떻게 확장되는지** 정리한 것입니다.

| 시점 | 확장 요소 | 데이터 설계 방향 |
| --- | --- | --- |
| **시즌 2** | 집사 말투·이미지·이름 | `Butler` 설정, DataStore + 앱 내부 이미지 파일 |
| **시즌 2** | 알림 및 알림 액션 기록 | `NotificationSchedule` 상세화, `EntrySource.NOTIFICATION_ACTION` 사용 |
| **시즌 2 제안** | 반복·빈도·`DOWN` 목표 | `Goal.type`, `direction`, `pacingPeriod` 확장 |
| **시즌 3** | 시간표·생활 패턴 | `UserProfile`, `RoutineBlock`, 목표 연결 키 |
| **시즌 4** | 통계·실행 패턴 | 기록 기반 집계 로직. 성능 문제 시 캐시 검토 |
| **시즌 미정** | 체크리스트, Health Connect, 백업 | `ChecklistItem`, 자동 기록 출처, 내보내기 포맷 등 별도 설계 |

#### `Butler`와 `UserProfile`

기능 초안의 두 모델은 여러 행을 검색·조인하는 데이터보다 **사용자 설정**의 성격이 강합니다. 따라서 DataStore를 우선 후보로 두되, 구체적인 직렬화는 실제 도입 시 확정합니다.

#### `RoutineBlock`과 `ChecklistItem`

목표와 연결되는 항목의 집합이므로 Room 테이블이 적합한 후보입니다. 다만 시간표 편집·체크리스트 기능이 들어오는 시즌에 관계와 삭제 정책을 확정하는 것이 좋습니다.

#### Health Connect

앱 내부에서 생성한 기록과 외부에서 가져온 기록을 중복 집계하지 않도록 출처와 외부 기록 ID가 필요할 수 있습니다. **현재 `ProgressEntry` 초안에는 외부 기록 ID가 없습니다.** 연동 기능을 실제 도입할 때 중복 방지 키와 수정 동기화 정책을 추가 설계해야 합니다.

---

## 8. 데이터 무결성·테스트·마이그레이션

### 8.1 지켜야 할 불변 조건

1. `ProgressEntry.goalId`가 존재하지 않는 목표를 가리켜서는 안 됩니다.
2. 시즌 1에서 기록량은 양의 유한한 수여야 합니다.
3. 목표를 보관해도 해당 목표의 과거 기록을 잃어서는 안 됩니다.
4. 기록을 수정하거나 삭제하면 누적량·일별 표시·권장량이 같은 원본에서 다시 산출되어야 합니다.
5. UI의 별도 변수에 저장된 합계가 Room의 원본보다 우선해서는 안 됩니다.

### 8.2 최소 테스트 사례

| 상황 | 기대 결과 |
| --- | --- |
| 300쪽 목표, 30일, 기록 없음 | 기본 계산식에 따라 하루 10쪽 표시(30일을 전부 포함한 경우) |
| 같은 날짜에 +5쪽, +10쪽 입력 | 기록 2건, 해당 날짜 합계 15쪽 |
| +10쪽 기록을 +3쪽으로 수정 | 전체 합계와 남은 권장 분량 갱신 |
| 해당 날짜의 유일한 기록 삭제 | `DailyRecord.state = NONE` |
| 목표량·마감일 수정 | 과거 진행 기록 보존, 권장 분량 재계산 |
| 목표 우선순위 바꾼 뒤 앱 재실행 | 순서 유지 |
| 마감일 지남 | 0으로 나누지 않고 상태 처리 |
| 목표량 초과 기록 | 오늘 권장량이 음수가 되지 않음 |
| 날짜·enum 저장 후 다시 읽기 | 원본 값과 동일하게 복원 |
| 이전 DB 버전에서 업그레이드 | 사용자의 기존 목표·기록 유지 |

### 8.3 Room 변경 시 정책

- Room 스키마 버전과 변환 정책을 명시적으로 관리합니다.
- `exportSchema = true`로 스키마 이력을 버전 관리하는 안을 권장합니다.
- 실제 사용자 데이터가 생긴 뒤에는 **무분별한 destructive migration을 피합니다.**
- Entity 필드나 enum 이름을 바꾸는 PR은 마이그레이션 영향도 함께 검토합니다.

---

## 9. 팀에서 결정해야 할 항목

| 안건 | 현재 제안 | 결정이 필요한 이유 |
| --- | --- | --- |
| Room 채택 | v1.0 원본 테이블 2개 | 기술 후보를 실제 구현으로 확정해야 함 |
| 식별자 | UUID 문자열 | ID 생성 위치·백업 충돌 정책과 연결 |
| 목표 영구 삭제 | 기본은 보관 | FK 삭제 방식과 기록 보존이 연결됨 |
| `restDays` | 필드는 두고 v1.0 사용 여부 논의 | 할당량 계산과 UI 범위에 영향 |
| `DailyRecord` | 저장하지 않고 계산 | 과거 `quota` 고정 여부에 영향 |
| 기록 날짜 기준 | 기기 로컬 날짜를 우선 제안 | 시간대 변경 시 기록이 다른 날에 표시될 수 있음 |
| DB `Double` | 수량형 우선 사용 | 미래의 금전·정밀 측정 데이터에서 오차 가능 |
| `dueDate` | 모델 nullable / 시즌 1 필수 | 이후 무기한 목표 확장 대비 |
| Repository 구분 | 도메인 인터페이스 + 데이터 구현체 | 소규모 팀의 복잡도와 테스트 편의 사이 선택 |
| 요구사항과 시즌 차이 | 최신 팀 합의 확인 | 요구사항 명세서에는 집사 성격 기능도 있으나 기능 초안 시즌 1에서는 제외됨 |

**개인 제안**: 기능과 구현 범위는 이 문서가 단독으로 확정하지 않습니다. 일단 시즌 1의 `Goal`·`ProgressEntry` 인터페이스와 데이터 경계부터 합의하고, 나머지는 해당 기능을 실제로 개발하는 시즌에 구체화하는 편이 충돌을 줄입니다.

---

## 10. 시즌 1 개발 순서 제안

1. **도메인 모델 정의** — `Goal`, `ProgressEntry` 및 필요한 enum 작성
2. **Repository 인터페이스 합의** — 화면과 DB 담당자가 같은 계약을 사용하도록 함수명 정의
3. **페이싱 계산 구현** — `PacingCalculator`와 경계 조건 테스트
4. **Room 영속화 구현** — Entity, DAO, TypeConverter, Database 및 저장 테스트
5. **Repository 구현체 연결** — Entity ↔ 도메인 모델 변환과 `Flow` 연결
6. **기능 연결** — 목표 만들기, 기록·수정·삭제, 홈 카드·히트맵 표시
7. **스키마 관리** — 마이그레이션 및 회귀 테스트 기준 문서화

작업 단위와 담당자·상태는 `requirement_analysis.md` 및 GitHub 이슈에서 관리합니다. **이 문서는 설계의 근거와 선택지를 정리하는 문서**이지, 별도의 할 일 목록을 운영하려는 문서는 아닙니다.

---

## 11. 참고 문서 및 적용 범위

- [기능 정의 초안](hss900606_draft.md) — 목표 유형, 진행 기록, 페이싱 엔진, 장기 데이터 모델, 시즌 범위
- [요구사항 · 기능 명세서](../requirement_analysis.md) — `FUNC-01`~`FUNC-06` 및 최신 작업 단위
- [킥오프 미팅 메모](261018_kickoff_meeting.md) — 최초 논의 사항
- [문서 안내](../README.md) — 초안 배치와 협업 문서 작성 규칙

> 이 문서는 `docs/draft/db_draft.md`에 두는 **독립적인 DB 설계 초안**입니다. 다른 사람의 `hss900606_draft.md`를 수정하거나 대체하지 않습니다.
