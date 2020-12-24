import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.random.Random.Default.nextBoolean


const val NUM_FLIPS: Int = 10
const val WIN_MULTIPLIER: Double = 1.5
const val LOSE_MULTIPLIER: Double = 0.6
const val START_BALANCE: Double = 100.0
const val BIN_SIZE: Double = 50.0
const val NUM_GAMES: Int = 10000
const val MAX_X: Double = 6000.0

val NUM_BINS = ceil(MAX_X / BIN_SIZE).toInt()

lateinit var pg: Program


fun main() = application {
    configure {
        width = 1080
        height = 720
        windowResizable = true
    }
    program {
        pg = this

        val frequencies = IntArray(NUM_BINS) { 0 }
        var gameIndex = 0
        var extremeValues = 0

        //if calculating values beforehand
        if (NUM_GAMES != -1) {

            //calculate values
            repeat(NUM_GAMES) {
                val newBalance = randBalance()
                val indexForNewBalance = indexForBalance(newBalance)

                if (indexForNewBalance < NUM_BINS) {
                    frequencies[indexForNewBalance]++
                }
                else {
                    extremeValues++
                }
            }

            gameIndex = NUM_GAMES

            //print values
            for (freq in frequencies.filter { it != 0 }) {
                val i = frequencies.indexOf(freq)

                val minBalance = (i * BIN_SIZE).toInt()
                val maxBalance = ((i+1) * BIN_SIZE).toInt()

                println("$minBalance-$maxBalance: $freq")
            }
            println("£$MAX_X+: $extremeValues")
        }

        extend {
            drawer.clear(ColorRGBa.BLACK)

            drawer.fill = rgb("0096C7")

            //if calculating values in real time
            if (NUM_GAMES == -1) {
                gameIndex++

                val newBalance = randBalance()
                val indexForNewBalance = indexForBalance(newBalance)

                if (indexForNewBalance < NUM_BINS) {
                    frequencies[indexForNewBalance]++
                    println("#$gameIndex: £$newBalance")
                }
                else {
                    extremeValues++
                    println("Extreme value £$newBalance excluded from graph")
                }
            }

            for ((i, freq) in frequencies.withIndex()) {
                val x = (i * BIN_SIZE).toGraphX()
                val y = (freq.toDouble() / gameIndex).toGraphY()
                drawer.rectangle(x, y, BIN_SIZE.toGraphX(), height.toDouble())
            }

            drawer.fill = ColorRGBa.WHITE

            for (freq in frequencies.filter { it != 0 }) {
                val i = frequencies.indexOf(freq)

                val minBalance = (i * BIN_SIZE)
                val maxBalance = ((i+1) * BIN_SIZE)

                drawer.text("$minBalance-$maxBalance", (i * BIN_SIZE).toGraphX(), (freq.toDouble() / gameIndex).toGraphY())
            }
        }
    }
}

fun randBalance(): Double {
    val numWins = List(10) { nextBoolean() }.count { it }
    return START_BALANCE * WIN_MULTIPLIER.pow(numWins) * LOSE_MULTIPLIER.pow(NUM_FLIPS - numWins)
}

fun indexForBalance(balance: Double): Int {
    return (balance / BIN_SIZE).toInt()
}

fun Double.toGraphX() = this * (pg.width/MAX_X)
fun Double.toGraphY() = (1 - this) * pg.height