
parser grammar MiniCalcParser;

options { tokenVocab=MiniCalcLexer; }

miniCalcFile : lines=line+ ;

line      : statement (NEWLINE | EOF) ;

statement : inputDeclaration # inputDeclarationStatement
          | varDeclaration   # varDeclarationStatement
          | assignment       # assignmentStatement
          | print            # printStatement
          | expression       # expressionStatement ;

print : PRINT LPAREN expression RPAREN ;

inputDeclaration : INPUT type name=ID ;

varDeclaration : VAR assignment ;

assignment : ID ASSIGN expression ;

expression : INTLIT                                                        # intLiteral
           | DECLIT                                                        # decimalLiteral
           | ID                                                            # valueReference
           | LPAREN expression RPAREN                                      # parenExpression
           | MINUS expression                                              # minusExpression
           | expression operator=(DIVISION|ASTERISK) expression # binaryOperation1
           | expression operator=(PLUS|MINUS) expression        # binaryOperation2;
type : DECIMAL # decimal
     | INT     # integer
     | STRING  # string ;