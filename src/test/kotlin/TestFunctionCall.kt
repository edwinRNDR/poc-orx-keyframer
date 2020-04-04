import org.amshove.kluent.shouldBeNear
import org.openrndr.extra.keyframer.evaluateExpression
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object TestFunctionCall : Spek({
    describe("a function call") {
        val expression = "sqrt(4.0)"
        val result = evaluateExpression(expression)
        result?.shouldBeNear(2.0, 10E-6)
    }

    describe("two function calls") {
        val expression = "sqrt(4.0) * sqrt(4.0)"
        val result = evaluateExpression(expression)
        result?.shouldBeNear(4.0, 10E-6)
    }

    describe("two argument function call") {
        val expression = "max(0.0, 4.0)"
        val result = evaluateExpression(expression)
        result?.shouldBeNear(4.0, 10E-6)
    }

    describe("two argument function call") {
        val expression = "min(8.0, 4.0)"
        val result = evaluateExpression(expression)
        result?.shouldBeNear(4.0, 10E-6)
    }

    describe("three argument function call") {
        val expression = "mix(8.0, 4.0, 0.5)"
        val result = evaluateExpression(expression)
        result?.shouldBeNear(6.0, 10E-6)
    }

    describe("five argument function call") {
        val expression = "map(0.0, 1.0, 0.0, 8.0, 0.5)"
        val result = evaluateExpression(expression)
        result?.shouldBeNear(4.0, 10E-6)
    }

    describe("two argument function call, where argument order matters") {
        val expression = "pow(2.0, 3.0)"
        val result = evaluateExpression(expression)
        result?.shouldBeNear(8.0, 10E-6)
    }

    describe("nested function call") {
        val expression = "sqrt(min(8.0, 4.0))"
        val result = evaluateExpression(expression)
        result?.shouldBeNear(2.0, 10E-6)
    }
})