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
    val baseDurationMs: UShort,
    val deltaMs: Short = 0,
    val minMs: UShort = 0u,
)

@Serializable
data class TimerBlock(
    val steps: List<TimerStep>,
    val repetitions: UShort
)

data class TimerBundle(
    val id: String,
    val name: String,
    val blocks: List<TimerBlock>
)
