package org.openrndr.extra.keyframer

import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.tree.ErrorNode
import org.antlr.v4.kotlinruntime.tree.ParseTreeWalker
import org.antlr.v4.kotlinruntime.tree.TerminalNode
import org.openrndr.extra.keyframer.antlr.MiniCalcLexer
import org.openrndr.extra.keyframer.antlr.MiniCalcParser
import org.openrndr.extra.keyframer.antlr.MiniCalcParserBaseListener
import java.lang.Math.pow
import java.lang.RuntimeException
import java.util.*
import kotlin.math.*

internal enum class IDType {
    VARIABLE,
    FUNCTION1,
    FUNCTION2
}

internal class ExpressionListener : MiniCalcParserBaseListener() {
    val doubleStack = Stack<Double>()
    val functionStack = Stack<(DoubleArray) -> Double>()
    val variables = mutableMapOf<String, Double>()

    val idTypeStack = Stack<IDType>()

    var lastExpressionResult: Double? = null

    override fun visitErrorNode(node: ErrorNode) {
        println("yo that's an error")
        println(node.text)
    }


    override fun exitExpressionStatement(ctx: MiniCalcParser.ExpressionStatementContext) {
        val result = doubleStack.pop()
        lastExpressionResult = result
    }

    override fun exitAssignment(ctx: MiniCalcParser.AssignmentContext) {
        val value = doubleStack.pop()
        variables[ctx.ID()?.text ?: error("buh")] = value
    }

    override fun exitMinusExpression(ctx: MiniCalcParser.MinusExpressionContext) {
        super.exitMinusExpression(ctx)
    }

    override fun exitBinaryOperation1(ctx: MiniCalcParser.BinaryOperation1Context) {
        val right = doubleStack.pop()
        val left = doubleStack.pop()


        val result = when (val operator = ctx.operator?.type) {
            MiniCalcParser.Tokens.PLUS.id -> left + right
            MiniCalcParser.Tokens.MINUS.id -> left - right
            MiniCalcParser.Tokens.ASTERISK.id -> left * right
            MiniCalcParser.Tokens.DIVISION.id -> left / right
            else -> error("operator not implemented")
        }
        doubleStack.push(result)
    }

    override fun exitBinaryOperation2(ctx: MiniCalcParser.BinaryOperation2Context) {
        val left = doubleStack.pop()
        val right = doubleStack.pop()
        val result = when (val operator = ctx.operator?.type) {
            MiniCalcParser.Tokens.PLUS.id -> left + right
            MiniCalcParser.Tokens.MINUS.id -> right - left
            MiniCalcParser.Tokens.ASTERISK.id -> left * right
            MiniCalcParser.Tokens.DIVISION.id -> left / right
            else -> error("operator not implemented")
        }
        doubleStack.push(result)
    }

    override fun enterValueReference(ctx: MiniCalcParser.ValueReferenceContext) {
        idTypeStack.push(IDType.VARIABLE)
    }

    override fun enterFunctionCall1Expression(ctx: MiniCalcParser.FunctionCall1ExpressionContext) {
        idTypeStack.push(IDType.FUNCTION1)
    }
    override fun exitFunctionCall1Expression(ctx: MiniCalcParser.FunctionCall1ExpressionContext) {
        val function = functionStack.pop()
        val argument = doubleStack.pop()

        val result = function.invoke(doubleArrayOf(argument))
        doubleStack.push(result)
    }

    override fun enterFunctionCall2Expression(ctx: MiniCalcParser.FunctionCall2ExpressionContext) {
        idTypeStack.push(IDType.FUNCTION2)
    }
    override fun exitFunctionCall2Expression(ctx: MiniCalcParser.FunctionCall2ExpressionContext) {
        val function = functionStack.pop()
        val argument1 = doubleStack.pop()
        val argument0 = doubleStack.pop()

        val result = function.invoke(doubleArrayOf(argument0, argument1))
        doubleStack.push(result)
    }

    override fun visitTerminal(node: TerminalNode) {
        val type = node.symbol?.type
        if (type == MiniCalcParser.Tokens.INTLIT.id) {
            doubleStack.push(node.text.toDouble())
        }
        if (type == MiniCalcParser.Tokens.DECLIT.id) {
            doubleStack.push(node.text.toDouble())
        }
        if (type == MiniCalcParser.Tokens.ID.id) {

            when (val idType = idTypeStack.pop()) {
                IDType.VARIABLE -> doubleStack.push(
                    variables[node.text] ?: error("unresolved variable: '${node.text}'")
                )
                IDType.FUNCTION1 -> {
                    val function: (DoubleArray) -> Double =
                        when (val candidate = node.text) {
                            "sqrt" -> { x -> sqrt(x[0]) }
                            "radians" -> { x -> Math.toRadians(x[0]) }
                            "degrees" -> { x -> Math.toDegrees(x[0]) }
                            "cos" -> { x -> cos(x[0]) }
                            "sin" -> { x -> sin(x[0]) }
                            "acos" -> { x -> acos(x[0]) }
                            "asin" -> { x -> asin(x[0]) }
                            "exp" -> { x -> exp(x[0]) }
                            "abs" -> { x -> abs(x[0]) }
                            else -> error("unresolved function: '${candidate}'")
                        }
                    functionStack.push(function)
                }
                IDType.FUNCTION2 -> {
                    val function: (DoubleArray) -> Double =
                        when (val candidate = node.text) {
                            "max" -> { x -> max(x[0], x[1]) }
                            "min" -> { x -> min(x[0], x[1]) }
                            "pow" -> { x -> x[0].pow(x[1]) }
                            else -> error("unresolved function: '${candidate}'")
                        }
                    functionStack.push(function)
                }
                else -> error("unsupported id-type $idType")
            }
        }
    }
}

fun evaluateExpression(input: String, variables: Map<String, Double> = emptyMap()): Double? {
    val lexer = MiniCalcLexer(CharStreams.fromString(input))
    val parser = MiniCalcParser(CommonTokenStream(lexer))

    val root = parser.miniCalcFile()
    val listener = ExpressionListener()
    listener.variables.putAll(variables)
    try {
        ParseTreeWalker.DEFAULT.walk(listener, root)
    } catch (e: Throwable) {
        throw RuntimeException(e)
    }
    return listener.lastExpressionResult
}

fun main() {
    println(evaluateExpression("30"))
    println(evaluateExpression("0.5"))
    println(evaluateExpression(" 3.25 + r * 0.5", variables = mapOf("r" to 0.0)))
    println(evaluateExpression(" 3.25 + r * 0.5", variables = mapOf("r" to 1.0)))
    println(evaluateExpression(" (r * 0.5) + 3.25", variables = mapOf("r" to 1.0)))
    println(evaluateExpression(" 3.25 + r * 0.5", variables = mapOf("r" to 2.0)))
    println(evaluateExpression("(r * 0.5) + 3.25", variables = mapOf("r" to 2.0)))
    println(evaluateExpression(" 3.25 + r * 0.5", variables = mapOf("r" to 3.0)))
    println(evaluateExpression(" 3.25 + r * 0.5", variables = mapOf("r" to 4.0)))
    println(evaluateExpression("30 + i", variables = mapOf("i" to 10.0)))

}