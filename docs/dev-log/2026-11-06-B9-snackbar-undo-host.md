# [B-9] 기록 스낵바와 되돌리기 호스트 구현

- 일자: 2026-11-06
- 작업자: Aetherx86
- 관련: #64

## 한 일

- `MainActivity` 최상위 `Scaffold`의 `snackbarHost` 슬롯에 연동되는 공용 스낵바 호스트 `MbSnackbarHost` 구현
- 하위 컴포저블 트리에서 스낵바 상태에 즉시 접근할 수 있도록 `LocalSnackbarHostState` CompositionLocal 구성
- 메시지와 함께 "되돌리기" 액션 버튼을 노출하고 클릭 시 취소 콜백을 실행하는 `showUndoSnackbar` 코루틴 확장 함수 제공
- `MbSnackbarHostTest` 단위 테스트 작성 및 통과 확인

## 알게 된 것 / 막힌 것

- 기록 후 화면을 차단하는 모달 다이얼로그 대신 하단 스낵바를 통해 비차단 방식으로 안내하고 즉시 되돌릴 수 있는 기회를 제공함으로써 사용자 경험의 유연성을 확보했습니다.
