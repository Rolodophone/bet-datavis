import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.openrndr.PresentationMode
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.random.Random.Default.nextBoolean


const val NUM_FLIPS: Int = 1000
const val WIN_MULTIPLIER: Double = 1.5
const val LOSE_MULTIPLIER: Double = 0.6
const val START_BALANCE: Double = 1_000_000.0
const val BIN_SIZE: Double = 50.0
const val NUM_GAMES: Int = 10000
const val MAX_X: Double = 6100.0
const val STEP_BY_STEP = false

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
		window.presentationMode = if (STEP_BY_STEP) PresentationMode.MANUAL else PresentationMode.AUTOMATIC

		val frequencies = IntArray(NUM_BINS) { 0 }
		var gameIndex = 0
		var extremeValues = 0
		var cumulativeProfit = 0.0

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

				cumulativeProfit += newBalance - START_BALANCE
			}

			gameIndex = NUM_GAMES

			//print values
			for ((i, freq) in frequencies.withIndex()) {
				if (freq == 0) continue

				val minBalance = (i * BIN_SIZE).toInt()
				val maxBalance = ((i+1) * BIN_SIZE).toInt()

				println("$minBalance-$maxBalance: $freq")
			}
			println("£$MAX_X+: $extremeValues")
			println("Cumulative profit: £$cumulativeProfit")
			println("Mean profit: £${ cumulativeProfit / NUM_GAMES }")
		}

		extend {
			drawer.clear(ColorRGBa.BLACK)

			drawer.fill = rgb("0096C7")

			//calculate values in real time
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

				cumulativeProfit += newBalance - START_BALANCE
			}

			//draw bars
			for ((i, freq) in frequencies.withIndex()) {
				val x = (i * BIN_SIZE).toGraphX()
				val y = (freq.toDouble() / gameIndex).toGraphY()
				drawer.rectangle(x, y, BIN_SIZE.toGraphX(), height.toDouble())
			}

			//draw labels
			drawer.fill = ColorRGBa.WHITE

			for ((i, freq) in frequencies.withIndex()) {
				if (freq == 0) continue

				val minBalance = (i * BIN_SIZE)
				val maxBalance = ((i+1) * BIN_SIZE)

				drawer.text("$minBalance-$maxBalance", (i * BIN_SIZE).toGraphX(), (freq.toDouble() / gameIndex).toGraphY())
			}

			if (STEP_BY_STEP) {

				//print info
				for ((i, freq) in frequencies.withIndex()) {
					if (freq == 0) continue

					val minBalance = (i * BIN_SIZE).toInt()
					val maxBalance = ((i+1) * BIN_SIZE).toInt()

					println("$minBalance-$maxBalance: $freq")
				}
				println("£$MAX_X+: $extremeValues")
				println("Cumulative profit: £$cumulativeProfit")
				println("Mean profit: £${ cumulativeProfit / gameIndex }")

				//wait for user to request another draw
				GlobalScope.launch {
					readLine()
					window.requestDraw()
				}
			}
		}
	}
}

fun randBalance(): Double {
	val numWins = List(NUM_FLIPS) { nextBoolean() }.count { it }
	return START_BALANCE * WIN_MULTIPLIER.pow(numWins) * LOSE_MULTIPLIER.pow(NUM_FLIPS - numWins)
}

fun indexForBalance(balance: Double): Int {
	return (balance / BIN_SIZE).toInt()
}

fun Double.toGraphX() = this * (pg.width/MAX_X)
fun Double.toGraphY() = (1 - this) * pg.height