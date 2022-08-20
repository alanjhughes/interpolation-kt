import java.lang.IllegalArgumentException


class Interpolation {
    companion object {
        fun interpolate(
            x: Float, input: List<Float>, output: List<Float>, type: ExtrapolationConfig
        ): Float {
            checkInputsSize(input.size, output.size)

            val narrowedInput = parseNarrowedInput(x, input, output)

            return internalInterpolate(x, narrowedInput, type)
        }

        fun interpolate(
            x: Float, input: List<Float>, output: List<Float>, type: Extrapolation = Extrapolation.EXTEND
        ): Float {
            checkInputsSize(input.size, output.size)

            val extrapolationConfig = type.requiredConfigFromType()

            val narrowedInput = parseNarrowedInput(x, input, output)

            return internalInterpolate(x, narrowedInput, extrapolationConfig)
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
    x: Float, narrowedInput: InterpolatedInput, extrapolationConfig: RequiredExtrapolationConfig
): Float {
    val (leftEdgeInput, rightEdgeInput, leftEdgeOutput, rightEdgeOutput) = narrowedInput

    if (rightEdgeInput - leftEdgeInput == 0f) return leftEdgeOutput

    val progress = (x - leftEdgeInput) / (rightEdgeInput - leftEdgeInput)
    val value = leftEdgeOutput + progress * (rightEdgeOutput - leftEdgeOutput)
    val coefficient = if (rightEdgeOutput >= leftEdgeOutput) 1f else -1f

    return when {
        coefficient * value < coefficient * leftEdgeOutput -> getVal(
            extrapolationConfig.extrapolateLeft, coefficient, value, leftEdgeOutput, rightEdgeOutput, x
        )

        coefficient * value > coefficient * rightEdgeOutput -> getVal(
            extrapolationConfig.extrapolateRight, coefficient, value, leftEdgeOutput, rightEdgeOutput, x
        )

        else -> value
    }
}

private fun getVal(
    type: Extrapolation, coefficient: Float, value: Float, leftEdgeOutput: Float, rightEdgeOutput: Float, x: Float
) = when (type) {
    Extrapolation.IDENTITY -> x
    Extrapolation.EXTEND -> value
    Extrapolation.CLAMP -> {
        if (coefficient * value < coefficient * leftEdgeOutput) {
            leftEdgeOutput
        } else {
            rightEdgeOutput
        }
    }
}

private fun checkInputsSize(inputSize: Int, outputSize: Int) {
    if (inputSize < 2 || outputSize < 2) {
        throw IllegalArgumentException("Interpolation input and output should contain at least two values")
    }
}

enum class Extrapolation {
    IDENTITY, CLAMP, EXTEND
}

private fun Extrapolation.requiredConfigFromType() = when (this) {
    Extrapolation.EXTEND -> RequiredExtrapolationConfig()
    Extrapolation.CLAMP -> RequiredExtrapolationConfig(Extrapolation.CLAMP, Extrapolation.CLAMP)
    Extrapolation.IDENTITY -> RequiredExtrapolationConfig(Extrapolation.IDENTITY, Extrapolation.IDENTITY)
}

private data class InterpolatedInput(
    var x1: Float, var y1: Float, var x2: Float, var y2: Float
)

data class RequiredExtrapolationConfig(
    var extrapolateLeft: Extrapolation = Extrapolation.EXTEND,
    var extrapolateRight: Extrapolation = Extrapolation.EXTEND
)

typealias ExtrapolationConfig = RequiredExtrapolationConfig
