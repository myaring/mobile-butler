package dev.myaring.mobile_butler.model

enum class ButlerPersona(
    val code: String,
    val displayName: String,
    val summary: String,
    val sampleGreeting: String
) {
    CYNICAL(
        code = "CYNICAL",
        displayName = "표독형",
        summary = "날카롭고 쌀쌀맞지만 확실하게 챙겨주는 집사",
        sampleGreeting = "또 오셨습니까? 빈둥거릴 시간 있으면 오늘 할 분량이나 확인하시죠."
    ),
    CALM(
        code = "CALM",
        displayName = "안정형",
        summary = "차분하고 든든하게 곁을 지키는 집사 (기본값)",
        sampleGreeting = "주인님, 어서 오십시오. 오늘도 무리하지 마시고 계획한 분량만큼 차근차근 걸어가시면 됩니다."
    ),
    ANXIOUS(
        code = "ANXIOUS",
        displayName = "불안형",
        summary = "마감과 일정을 조마조마하게 걱정하는 집사",
        sampleGreeting = "주, 주인님! 오셨군요! 오늘 분량 아직이신가요? 마감일이 벌써 다가오고 있어요..."
    );

    companion object {
        fun fromCode(code: String?): ButlerPersona {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: CALM
        }
    }
}
