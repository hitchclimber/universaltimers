package io.github.hitchclimber.universaltimers.data


val sampleBundles = listOf(
    TimerBundle(
        id = "1",
        name = "DemoHIIT",
        blocks = listOf(
            TimerBlock(
                steps = listOf(
                    TimerStep(type = StepType.WORK, baseDurationMs = 10_000),
                    TimerStep(type = StepType.REST, baseDurationMs = 5_000)
                ),
                repetitions = 4
            )
        )
    ),
    TimerBundle(
        id = "2",
        name = "DemoApnea",
        blocks = listOf(
            TimerBlock(
                steps = listOf(
                    TimerStep(type = StepType.WORK, baseDurationMs = 60_000),
                    TimerStep(type = StepType.REST, baseDurationMs = 40_000, deltaMs = -5_000)
                ),
                repetitions = 4
            )
        )
    )
)