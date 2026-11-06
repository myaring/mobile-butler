package dev.myaring.mobile_butler.engine

import dev.myaring.mobile_butler.model.ButlerEvent
import dev.myaring.mobile_butler.model.ButlerPersona

object ButlerDialogueEngine {

    private val dialogues: Map<ButlerPersona, Map<ButlerEvent, List<String>>> = mapOf(
        ButlerPersona.CYNICAL to mapOf(
            ButlerEvent.HOME_GREETING to listOf(
                "또 오셨습니까? 빈둥거릴 시간 있으면 오늘 할 분량이나 확인하시죠.",
                "제 시간 뺏지 마시고 오늘 할당량이나 제때 끝내세요.",
                "설마 오늘도 마감 직전에 벼락치기할 생각은 아니겠죠?"
            ),
            ButlerEvent.PROGRESS_LOGGED to listOf(
                "흥, 겨우 이만큼 하고 뿌듯해하시는 건 아니겠죠? 그래도 안 한 것보단 낫네요.",
                "기록은 정상 처리했습니다. 내일도 이 정도로 해주셔야 합니다.",
                "이 정도 속도면 계획대로는 가겠군요. 딴짓만 안 한다면 말입니다."
            ),
            ButlerEvent.GOAL_COMPLETED to listOf(
                "어라? 설마 정말 다 끝내셨습니까? 솔직히 중간에 포기할 줄 알았는데, 이번만큼은 인정하겠습니다.",
                "목표 달성이라니 별일이네요. 고생하셨습니다. 하지만 다음 목표도 곧 시작해야겠죠?"
            ),
            ButlerEvent.DEADLINE_APPROACHING to listOf(
                "마감이 코앞인데 여유 부릴 때입니까? 정신 바짝 차리세요.",
                "달력 좀 보십시오. 마감이 다가오는데 왜 그렇게 태평하십니까?"
            ),
            ButlerEvent.DEADLINE_D_DAY to listOf(
                "오늘이 마감일입니다. 이제 도망칠 곳도 없으니 끝까지 해내세요."
            ),
            ButlerEvent.DEADLINE_OVERDUE to listOf(
                "결국 기한을 넘기셨군요. 제가 그럴 줄 알았습니다. 지금이라도 서둘러 마무리하시죠."
            ),
            ButlerEvent.NO_GOALS to listOf(
                "목표조차 없다니, 인생을 너무 편하게 사시는 거 아닙니까? 당장 새 목표부터 등록하세요."
            )
        ),
        ButlerPersona.CALM to mapOf(
            ButlerEvent.HOME_GREETING to listOf(
                "주인님, 어서 오십시오. 오늘도 무리하지 마시고 계획한 분량만큼 차근차근 걸어가시면 됩니다.",
                "좋은 하루입니다. 오늘도 주인님의 페이스에 맞춰 든든히 함께하겠습니다.",
                "꾸준함이 가장 큰 힘입니다. 오늘의 목표를 확인해보실까요?"
            ),
            ButlerEvent.PROGRESS_LOGGED to listOf(
                "오늘 분량을 훌륭히 기록하셨습니다. 한 걸음 더 나아가셨군요.",
                "성실히 걸어가시는 모습이 보기 좋습니다. 편안한 마음으로 다음 일정을 준비하세요.",
                "수고 많으셨습니다. 차근차근 나아가는 모습이 참 든든합니다."
            ),
            ButlerEvent.GOAL_COMPLETED to listOf(
                "축하드립니다, 주인님. 마침내 목표를 완수하셨습니다. 진심으로 자랑스럽습니다.",
                "끈기 있게 끝까지 해내셨습니다. 잠시 성취의 기쁨을 누리셔도 좋습니다."
            ),
            ButlerEvent.DEADLINE_APPROACHING to listOf(
                "마감일이 얼마 남지 않았습니다. 서두르지 마시고 지금처럼만 침착하게 이어가세요.",
                "마감이 다가오고 있습니다. 남은 날 동안 하루 분량을 충실히 채워보겠습니다."
            ),
            ButlerEvent.DEADLINE_D_DAY to listOf(
                "오늘이 마감 당일입니다. 마지막까지 최선을 다하실 수 있도록 정성껏 보필하겠습니다."
            ),
            ButlerEvent.DEADLINE_OVERDUE to listOf(
                "기한이 조금 지났지만 포기하지 마세요. 늦더라도 끝까지 해내는 것이 진정한 성공입니다."
            ),
            ButlerEvent.NO_GOALS to listOf(
                "아직 등록된 목표가 없습니다. 이루고 싶은 소중한 목표를 하나 등록해보시는 건 어떨까요?"
            )
        ),
        ButlerPersona.ANXIOUS to mapOf(
            ButlerEvent.HOME_GREETING to listOf(
                "주, 주인님! 오셨군요! 오늘 분량 계획은 어떻게 되나요? 벌써 시간이 이렇게...",
                "혹시 오늘 할 일 깜빡하신 건 아니죠? 저, 제가 계속 신경 쓰여서 안절부절못했습니다.",
                "어서 오세요! 오늘도 잘 해낼 수 있을지 걱정 반 기대 반으로 기다렸어요!"
            ),
            ButlerEvent.PROGRESS_LOGGED to listOf(
                "휴, 다행이에요! 오늘 분량 해주셔서 가슴을 쓸어내렸습니다. 정말 잘하셨어요!",
                "기록 완료된 거죠? 밀리면 어쩌나 조마조마했는데 한숨 돌렸습니다!",
                "와, 진도가 나갔어요! 이대로만 계속 가주시면 정말 좋겠어요!"
            ),
            ButlerEvent.GOAL_COMPLETED to listOf(
                "해, 해내셨군요! 끝까지 못 갈까 봐 얼마나 조마조마했는지 모릅니다! 정말 대단하세요!",
                "목표 달성이라니 믿기지 않아요! 너무 감격스러워서 눈물이 날 것 같아요!"
            ),
            ButlerEvent.DEADLINE_APPROACHING to listOf(
                "주인님! 마감이 벌써 얼마 안 남았어요! 어쩌죠, 계획대로 다 끝낼 수 있을까요?!",
                "D-day가 다가옵니다! 제발 밀리지 말아주세요, 제가 다 떨려요!"
            ),
            ButlerEvent.DEADLINE_D_DAY to listOf(
                "오늘이 바로 마감날이에요! 남은 분량 오늘 꼭 끝내셔야 합니다! 으악, 힘내세요!"
            ),
            ButlerEvent.DEADLINE_OVERDUE to listOf(
                "어, 어떡해요... 기한이 지나버렸어요! 그래도 지금이라도 끝내야 해요, 제발요!"
            ),
            ButlerEvent.NO_GOALS to listOf(
                "목표가 하나도 없어요! 아무 계획도 없이 하루를 보내면 너무 불안한데... 하나 정해주실래요?"
            )
        )
    )

    fun getDialogue(
        persona: ButlerPersona,
        event: ButlerEvent,
        index: Int = 0
    ): String {
        val pool = dialogues[persona]?.get(event)
            ?: dialogues[ButlerPersona.CALM]?.get(event)
            ?: listOf("주인님의 하루를 충실히 보필하겠습니다.")

        val safeIndex = (index % pool.size).let { if (it < 0) it + pool.size else it }
        return pool[safeIndex]
    }

    fun getAllDialogues(persona: ButlerPersona, event: ButlerEvent): List<String> {
        return dialogues[persona]?.get(event)
            ?: dialogues[ButlerPersona.CALM]?.get(event)
            ?: listOf("주인님의 하루를 충실히 보필하겠습니다.")
    }
}
