package parser

import java.io.InputStream

import lexer.Token.Kind
import lexer.{Token, Lexer}
import util.Pos

/**
  * Created by Huxley on 11/15/15.
  */
class ScalaParser(lexer: Lexer) {
  var current: Token = null

  def this(fname: String, fstream: InputStream) = {
    this(new Lexer(fname, fstream))
    current = lexer.nextToken()
  }

  //////////////////////////////////////////////////////
  // utility methods to connect the lexer and the parser
  private def advance() = current = lexer.nextToken()

  private def eatToken(kind: Kind) = {
    var ret: String = null
    if (kind == current.kind) {
      ret = current.lexeme
      advance()
    }
    else {
      println(s"Expects ${kind.toString}")
      println(s"Bug got: ${current.kind.toString} at (${current.lineRow}, ${current.lineCol}) in ${lexer.fname}")
      advance()
    }
    ret
  }

  private def currentPos = Pos(current.lineRow, current.lineCol)

  private def error() = {
    println("Syntax error: compilation aborting...\n")
    System.exit(1)
  }

  /////////////////////////////////////////////////////
  // below are methods for parsing.

  // A bunch of parsing methods to parse expressions. The messy
  // parts are to deal with precedence and associativity.

  // TODO
}
