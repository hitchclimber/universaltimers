package io.github.hitchclimber.universaltimers.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun TimerBundle.toEntity(): BundleEntity = BundleEntity(
    id = id,
    name = name,
    blocksJson = Json.encodeToString(blocks),
    countdownEnabled = countdownEnabled,
)

fun BundleEntity.toTimerBundle(): TimerBundle = TimerBundle(
    id = id,
    name = name,
    blocks = Json.decodeFromString(blocksJson),
    countdownEnabled = countdownEnabled,
)