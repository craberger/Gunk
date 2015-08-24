package DunceCap

import scala.collection.immutable.List
import scala.collection.immutable.ListMap
import scala.util.parsing.combinator.RegexParsers

object DCParser extends RegexParsers {
  def run(line:String,s:CodeStringBuilder) : Unit = {
    this.parseAll(this.statements, line) match {
      case DCParser.Success(ast, _) => {
        Environment.emitASTNodes(s)
      }
    }
  }

  //some basic expressions
  def identifierName:Parser[String] = """[_\p{L}][_\p{L}\p{Nd}]*""".r
  def selectionElement:Parser[String] = """"[^"]*"|\d+""".r
  def expression:Parser[String] = """[^\]]*""".r
  def aggOp:Parser[String] = """SUM""".r
  def selectionOp:Parser[String] = """=""".r
  def typename:Parser[String] = """long|string|float""".r
  def emptyStatement = "".r ^^ {case r => List()}
  def emptyStatementMap:Parser[Map[String,String]] = "".r ^^ {case r => Map[String,String]()}

  //for the lhs expression
  def lhsStatement = relationIdentifier | scalarIdentifier
  def relationIdentifier: Parser[QueryRelation] = identifierName ~ (("(" ~> attrList) <~ ")") ^^ {case id~attrs => new QueryRelation(id, attrs)}
  def scalarIdentifier: Parser[QueryRelation] = identifierName ~ (("(" ~> emptyStatement) <~ ")") ^^ {case a~l => new QueryRelation(a,l)}
  def attrList : Parser[List[String]] = notLastAttr | lastAttr
  def notLastAttr = identifierName ~ ("," ~> attrList) ^^ {case a~rest => a +: rest}
  def lastAttr = identifierName ^^ {case a => List(a)}

  //for the aggregate statement
  def aggregateStatement : Parser[Map[String,String]] = (recursiveAggregate <~ ";") | emptyStatementMap
  def recursiveAggregate : Parser[Map[String,String]] = multipleAggregateStatement | singleAggregateStatement
  def multipleAggregateStatement = (singleAggregateStatement <~ ",")  ~ recursiveAggregate ^^ {case t~rest => t ++: rest}
  def singleAggregateStatement = identifierName ~ (("(" ~> aggOp) <~ ")") ^^ {case identifierName~aggOp => Map( (identifierName -> aggOp) )}

  //for the join query
  def joinStatement:Parser[List[SelectionRelation]] = multipleJoinIdentifiers | singleJoinIdentifier 
  def multipleJoinIdentifiers = (singleJoinIdentifier <~ ",") ~ joinStatement ^^ {case t~rest => t ++: rest}
  def singleJoinIdentifier = identifierName ~ (("(" ~> joinAttrList) <~ ")") ^^ {case id~attrs => List( new SelectionRelation(id, attrs) )}
  def joinAttrList : Parser[List[(String,String,String)]] = notLastJoinAttr | lastJoinAttr
  def notLastJoinAttr = selectionStatement ~ ("," ~> joinAttrList) ^^ {case a~rest => a +: rest}
  def lastJoinAttr = selectionStatement ^^ {case a => List(a)} 
  def selectionStatement : Parser[(String,String,String)] =  selection | emptySelection
  def selection: Parser[(String,String,String)] = (identifierName ~ selectionOp ~ selectionElement) ^^ {case a~b~c => (a,b,c) }
  def emptySelection: Parser[(String,String,String)] = identifierName ^^ {case a => (a,"","")}

  //for the aggregate expression (just going to emit the raw text in C++ code for now)
  def aggregateExpressionStatement: Parser[Map[String,String]] = (";" ~> recursiveAggregateExpression) | emptyStatementMap
  def recursiveAggregateExpression : Parser[Map[String,String]] = multipleAggregateExpressionStatement | singleAggregateExpressionStatement
  def multipleAggregateExpressionStatement = (singleAggregateExpressionStatement <~ ",")  ~ recursiveAggregateExpression ^^ {case t~rest => t ++: rest}
  def singleAggregateExpressionStatement:Parser[Map[String,String]] = (identifierName ~ ("=" ~> aggregateExpression) ) ^^ {case a~b => Map( (a,b) )}
  def aggregateExpression:Parser[String] = ("[" ~> expression <~ "]")

  def queryStatement = (lhsStatement ~ (":=" ~> aggregateStatement) ~ joinStatement ~ (aggregateExpressionStatement <~ ".") ) ^^ {case a~b~c~d => Environment.addASTNode(new ASTQueryStatement(a,b,c,d))}
  def printStatement = lhsStatement ^^ {case l => Environment.addASTNode(new ASTPrintStatement(l))}
  def statement = queryStatement | printStatement
  def statements = rep(statement)
}
