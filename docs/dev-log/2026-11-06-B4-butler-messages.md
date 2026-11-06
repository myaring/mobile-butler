# [B-4] ButlerLine과 집사 문구 규칙 구현

- 일자: 2026-11-06
- 작업자: Aetherx86
- 관련: #59

## 한 일

- `ButlerMessages` (`domain/pacing`): 목표 평가 상태(`GoalEvaluation`) 및 일일 기록 상태(`RecordState`)에 따른 맞춤형 집사 문구 선택 로직 구현
- 집사 말투 규칙 준수: 모든 문구에 정중한 '합니다' 체 적용 및 이모지 완전 배제
- `ButlerLine` (`ui/component`): 집사 아이콘과 한 줄 문구를 표시하는 Material 3 Surface Composable 구현
- 단위 테스트 작성: 상태별 문구 반환, 한국어 정중 어미 검증, 이모지 미포함 검증 (10개 테스트)

## 알게 된 것 / 막힌 것

- 목표가 없거나(null), 진행 중, 마감 임박, 마감 기한 초과, 목표 완수 등 상황별 분기를 명확히 구분하여 일관된 어조를 유지하도록 설계함.
- UI 터치 영역 및 Figma 2차 시안의 `ButlerLine` 스펙(아이콘 + 한 줄 대사)에 맞추어 `TextOverflow.Ellipsis` 및 토큰 테마를 적용함.
