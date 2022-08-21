fun main() {
    for (i in -200 until 200) {
        val value = Interpolation.interpolate(
            i.toFloat(),
            listOf(0, 1),
            listOf(2, 4),
            ExtrapolationConfig(extrapolateLeft = Extrapolation.CLAMP, extrapolateRight = Extrapolation.EXTEND)
        )
        println(value)
    }
}
