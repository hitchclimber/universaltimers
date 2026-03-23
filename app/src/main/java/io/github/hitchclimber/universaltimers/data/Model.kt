package io.github.hitchclimber.universaltimers.data

import kotlinx.serialization.Serializable

@Serializable
enum class StepType {
    WORK,
    REST
}

@Serializable
data class TimerStep (
    val label: String = "",
    val type: StepType,
    val baseDurationMs: Long,
    val deltaMs: Long = 0,
    val minMs: Long = 0,
)

@Serializable
data class TimerBlock(
    val steps: List<TimerStep>,
    val repetitions: Int
)

data class TimerBundle(
    val id: String,
    val name: String,
    val blocks: List<TimerBlock>
)
