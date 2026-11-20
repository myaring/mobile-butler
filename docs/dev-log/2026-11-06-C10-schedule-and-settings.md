# [C-10] 일정 탭 준비 중 팝업 및 설정 화면 구현

- 일자: 2026-11-06
- 작업자: Aetherx86
- 관련: #76

## 한 일

- `ScheduleEntry` (`ui/schedule`): 향후 일정 기능 확장을 위한 진입점 및 패키지 구조 마련
- 일정 탭 내비게이션 동작: 하단 일정 탭 탭 시 화면 전환 없이 "준비 중이에요" AlertDialog 표시
- `SettingScreen` (`ui/navigation`): 앱 버전(1.0.0) 정보를 표시하도록 화면 단순화 구성
- 단위 테스트 작성: ScheduleScreen null delegatee 검증, SettingScreenViewModel 검증

## 알게 된 것 / 막힌 것

- 시즌 1 스펙에 따라 일정 탭은 네비게이션 스택을 변경하지 않고 상위 네비게이션 바에서 `screenDrawerDelegateeOrNull == null` 분기를 통해 팝업을 띄우는 구조를 유지함.
