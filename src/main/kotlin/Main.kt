fun main() {
    val interpolation = Interpolation()
    for (i in 100 downTo -100) {
        val result = interpolation.interpolate(
            i.toFloat(),
            listOf(0f, -1f),
            listOf(-2f, -4f),
            ExtrapolationConfig(Extrapolation.CLAMP, Extrapolation.EXTEND)
        )
        println(result)
    }
}