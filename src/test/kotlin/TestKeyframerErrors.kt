import org.amshove.kluent.`should throw`
import org.amshove.kluent.`with message`
import org.amshove.kluent.invoking
import org.openrndr.extra.keyframer.Keyframer
import org.openrndr.extra.keyframer.KeyframerFormat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File
import kotlin.IllegalStateException

object TestKeyframerErrors : Spek({
    class Animation : Keyframer() {
        val position by Vector2Channel(arrayOf("x", "y"))
    }

    describe("loading a faulty json") {
        val animation = Animation()
        val json = """
        """
        it("should throw an exception") {
            invoking { animation.loadFromJsonString(json) } `should throw` (IllegalStateException::class)
        }
    }

    describe("loading a non existing json") {
        val animation = Animation()
        it("should throw an exception") {
            invoking { animation.loadFromJson(File("this-does-not-exist")) } `should throw` (IllegalArgumentException::class)
        }
    }

    describe("loading a json with a faulty time expression (1) ") {
        val animation = Animation()
        it("should throw an exception") {
            invoking {
                animation.loadFromJson(
                    File("src/test/resources/error-reporting/time-01.json"),
                    format = KeyframerFormat.SIMPLE
                )
            } `should throw` IllegalStateException::class `with message` "Error loading from 'src/test/resources/error-reporting/time-01.json': error in keys[0].'time': parser error in expression: ')('; [line: 1, character: 0 , near: [@0,0:0=')',<21>,1:0] ]"
        }
    }

    describe("loading a json with a faulty time expression (2) ") {
        val animation = Animation()
        it("should throw an exception") {
            invoking {
                animation.loadFromJson(
                    File("src/test/resources/error-reporting/time-02.json"),
                    format = KeyframerFormat.SIMPLE
                )
            } `should throw` IllegalStateException::class `with message` "Error loading from 'src/test/resources/error-reporting/time-02.json': error in keys[0].'time': error in evaluation of 'doesNotExist': unresolved variable: 'doesNotExist'"
        }
    }

    describe("loading a json with a non-existing easing") {
        val animation = Animation()
        it("should throw an exception") {
            invoking {
                animation.loadFromJson(
                    File("src/test/resources/error-reporting/easing.json"),
                    format = KeyframerFormat.SIMPLE
                )
            } `should throw` IllegalStateException::class `with message` "Error loading from 'src/test/resources/error-reporting/easing.json': error in keys[0].'easing': unknown easing name 'garble'"
        }
    }

    describe("loading a json with a faulty value (1)") {
        val animation = Animation()

        it("should throw an exception") {
            invoking {
                animation.loadFromJson(
                    File("src/test/resources/error-reporting/value-01.json"),
                    format = KeyframerFormat.SIMPLE
                )
            } `should throw` IllegalStateException::class `with message` "Error loading from 'src/test/resources/error-reporting/value-01.json': error in keys[0].'x': error in evaluation of 'garble': unresolved variable: 'garble'"
        }
    }
})