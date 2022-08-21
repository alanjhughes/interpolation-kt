class Interpolation {
    companion object {
        fun interpolate(
            x: Float,
            input: List<Float>,
            output: List<Float>,
            config: ExtrapolationConfig
        ): Float {
            checkInputsSize(input.size, output.size)

            val narrowedInput = parseNarrowedInput(x, input, output)

            return internalInterpolate(x, narrowedInput, config)
        }

        fun interpolate(
            x: Float,
            input: List<Float>,
            output: List<Float>,
            type: Extrapolation = Extrapolation.EXTEND
        ): Float {
            checkInputsSize(input.size, output.size)

            val extrapolationConfig = type.configFromType()

            val narrowedInput = parseNarrowedInput(x, input, output)

            return internalInterpolate(x, narrowedInput, extrapolationConfig)
        }

        @JvmName("interpolateInt")
        fun interpolate(
            x: Float,
            input: List<Int>,
            output: List<Int>,
            type: Extrapolation = Extrapolation.EXTEND
        ): Float {
            checkInputsSize(input.size, output.size)

            val extrapolationConfig = type.configFromType()

            val narrowedInput = parseNarrowedInput(x, input.map { it.toFloat() }, output.map { it.toFloat() })

            return internalInterpolate(x, narrowedInput, extrapolationConfig)
        }

        @JvmName("interpolateInt")
        fun interpolate(
            x: Float,
            input: List<Int>,
            output: List<Int>,
            config: ExtrapolationConfig
        ): Float {
            checkInputsSize(input.size, output.size)

            val narrowedInput = parseNarrowedInput(x, input.map { it.toFloat() }, output.map { it.toFloat() })

            return internalInterpolate(x, narrowedInput, config)
        }
    }
}

private fun parseNarrowedInput(
    x: Float,
    input: List<Float>,
    output: List<Float>,
): InterpolatedInput {
    val size = input.size

    val interpolatedInput = InterpolatedInput(
        x1 = input[0], y1 = input[1], x2 = output[0], y2 = output[1]
    )

    if (size > 2) {
        if (x > input[size - 1]) {
            interpolatedInput.x1 = input[size - 2]
            interpolatedInput.y1 = input[size - 1]
            interpolatedInput.x2 = output[size - 2]
            interpolatedInput.y2 = output[size - 1]
        } else {
            for (i in 1 until size) {
                if (x <= input[i]) {
                    interpolatedInput.x1 = input[i - 1]
                    interpolatedInput.y1 = input[i]
                    interpolatedInput.x2 = output[i - 1]
                    interpolatedInput.y2 = output[i]
                    break
                }
            }
        }
    }

    return interpolatedInput
}

private fun internalInterpolate(
    input: Float,
    narrowedInput: InterpolatedInput,
    extrapolationConfig: ExtrapolationConfig
): Float {
    val (x1, y1, x2, y2) = narrowedInput

    if (y1 - x1 == 0f) return x2

    val progress = (input - x1) / (y1 - x1)
    val value = x2 + progress * (y2 - x2)
    val coefficient = if (y2 >= x2) 1 else -1

    return when {
        coefficient * value < coefficient * x2 -> getValue(
            extrapolationConfig.extrapolateLeft, coefficient, value, x2, y2, input
        )

        coefficient * value > coefficient * y2 -> getValue(
            extrapolationConfig.extrapolateRight, coefficient, value, x2, y2, input
        )

        else -> value
    }
}

private fun getValue(
    type: Extrapolation,
    coefficient: Int,
    value: Float,
    leftEdgeOutput: Float,
    rightEdgeOutput: Float,
    x: Float
) = when (type) {
    Extrapolation.IDENTITY -> x
    Extrapolation.EXTEND -> value
    Extrapolation.CLAMP -> if (coefficient * value < coefficient * leftEdgeOutput) leftEdgeOutput else rightEdgeOutput
}

private fun checkInputsSize(inputSize: Int, outputSize: Int) {
    if (inputSize < 2 || outputSize < 2) {
        throw IllegalArgumentException("Interpolation input and output should contain at least two values")
    }
}

enum class Extrapolation {
    IDENTITY, CLAMP, EXTEND
}

private fun Extrapolation.configFromType() = when (this) {
    Extrapolation.EXTEND -> ExtrapolationConfig()
    Extrapolation.CLAMP -> ExtrapolationConfig(Extrapolation.CLAMP, Extrapolation.CLAMP)
    Extrapolation.IDENTITY -> ExtrapolationConfig(Extrapolation.IDENTITY, Extrapolation.IDENTITY)
}

private data class InterpolatedInput(
    var x1: Float,
    var y1: Float,
    var x2: Float,
    var y2: Float
)

data class ExtrapolationConfig(
    var extrapolateLeft: Extrapolation = Extrapolation.EXTEND,
    var extrapolateRight: Extrapolation = Extrapolation.EXTEND
)
