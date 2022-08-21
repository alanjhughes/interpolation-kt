fun main() {
    for (i in 0 until 200) {
        val result = Interpolation.interpolate(i.toFloat(), listOf(-1, 0, 90, 90 * 2), listOf(1, 1, 1, 0))
        println(result)
    }
}