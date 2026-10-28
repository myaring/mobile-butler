/**
 * 임시 type 정의입니다. 사실 시즌 1 범위 밖입니다.
 * data model 에 있길래 써봤습니다.
 * 애초에 시즌 미정입니다.
 * 언제든지 바뀔수 았습니다. 이거 따라쓰다 발생하는 문제에 대해선 전 몰라여.
 */

package dev.myaring.mobile_butler.domain.model

enum class HealthDataType {
    STEPS,
    DISTANCE,
    ACTIVE_CALORIES,
    HEART_RATE,
    WEIGHT,
}