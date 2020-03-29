package org.operndr.extra.keyframer

import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.ParserRuleContext
import org.antlr.v4.kotlinruntime.tree.ErrorNode
import org.antlr.v4.kotlinruntime.tree.ParseTreeWalker
import org.antlr.v4.kotlinruntime.tree.TerminalNode
import org.openrndr.extra.keyframer.antlr.MiniCalcLexer
import org.openrndr.extra.keyframer.antlr.MiniCalcParser
import org.openrndr.extra.keyframer.antlr.MiniCalcParserBaseListener
import java.lang.RuntimeException
import java.util.*

internal class ExpressionListener : MiniCalcParserBaseListener() {
    val doubleStack = Stack<Double>()
    val variables = mutableMapOf<String, Double>()

    var lastExpressionResult: Double? = null

    override fun visitErrorNode(node: ErrorNode) {
        println("yo that's an error")
        println(node.text)
    }

//    override fun enterEveryRule(ctx: ParserRuleContext) {
//        println(ctx.text)
//    }

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

    override fun visitTerminal(node: TerminalNode) {
        val type = node.symbol?.type
        if (type == MiniCalcParser.Tokens.INTLIT.id) {
            doubleStack.push(node.text.toDouble())
        }
        if (type == MiniCalcParser.Tokens.DECLIT.id) {
            doubleStack.push(node.text.toDouble())
        }
        if (type == MiniCalcParser.Tokens.ID.id) {
            doubleStack.push(variables[node.text] ?: error("unresolved variable: '${node.text}'"))
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