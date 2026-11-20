# 2026-11-06 [B-10] CalendarHeatmap 달력 히트맵 컴포넌트 구현

- 작업자: Aetherx86
- 관련 이슈: #65

## 작업 내용

1. `CalendarHeatmap` 컴포넌트 구현
   - 일~토 요일 머리글 및 7열 달력 날짜 그리드 구현 (일요일 0번 열 정렬)
   - 제목에 "YYYY년 MM월 / N일 중 M일 기록" 경과 및 기록 완료 일수 동적 표시
   - 좌우 월 이동 버튼(`IconButton`, 이전 달/다음 달) 및 상태 기억 지원
   - 미래 날짜 칸 35% 투명도(`alpha = 0.35f`) 적용
   - 오늘 날짜 칸 흰 테두리(`MbOnBackground`, 1.5dp) 및 기록 여부별 채움 상태(MbRecordDone / MbSurfaceVariant) 분기 처리
   - 하단 범례("한 날", "기록 없음") 표시 (부정적 어휘 및 기호 배제 원칙 준수)

2. 단위 테스트 작성
   - `CalendarHeatmapTest`: 제목 포맷 검증, 일요일 시작 정렬 패딩 검증, 미래 날짜 투명도 검증, 텍스트 이모지 부재 검증
