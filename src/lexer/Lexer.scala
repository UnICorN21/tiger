package lexer

import java.io.InputStream

import control.Control.ConLexer
import lexer.Token.Kind
import util.BugString

import scala.util.{Failure, Success, Try}

/**
  * ScalaLexer is a substitute of the current `Lexer` class.
  * It'll be renamed to `Lexer` someday.
  * @param fname the input file name to be compiled
  * @param fstream input stream for the above file
  */
class Lexer(val fname: String, fstream: InputStream) {
  private var buffer = List[Token]()
  private var lineRow = 1
  private var lineCol = 0

  private def read: Int = {
    val ret = fstream.read()
    if ('\n' == ret) { lineRow += 1; lineCol = 0; }
    else lineCol += 1
    ret
  }

  private def mark() = this.fstream.mark(0)

  private def reset() = { this.fstream.reset(); lineCol -= 1 }

  private def skip(c: Int): Int = c match {
    case ' ' | '\t' | '\n' => skip(skipBlanks(c))
    case '/' => skip(skipComments(read))
    case _ => c
  }

  private def skipBlanks(c: Int): Int = c match {
    case ' ' | '\t' | '\n' => skipBlanks(read)
    case _ => c
  }

  private def skipComments(c: Int): Int = c match {
    case '/' =>
      while ('\n' != read) { /* null */ }
      read
    case '*' =>
      do {
        while ('*' != read) { /* null */ }
      } while ('/' != read)
      read
    case _ =>
      bug"found unrecognized token at ($lineRow, $lineCol)"
      -1;
  }

  // helper function to construct the return token
  private def Token(kind: Kind) = Success(new Token(kind, lineRow, lineCol))
  private def Token(kind: Kind, lexeme: String) = Success(new Token(kind, lineRow, lineCol, lexeme))

  private def nextTokenInternal(): Try[Token] = skip(read) match {
      case -1 => Token(Kind.TOKEN_EOF)
      case '+' => Token(Kind.TOKEN_ADD)
      case '-' => Token(Kind.TOKEN_SUB)
      case '*' => Token(Kind.TOKEN_TIMES)
      case '>' =>
        mark()
        read match {
          case '=' => Token(Kind.TOKEN_GE)
          case _ =>
            reset()
            Token(Kind.TOKEN_GT)
        }
      case '<' =>
        mark()
        read match {
          case '=' => Token(Kind.TOKEN_LE)
          case _ =>
            reset()
            Token(Kind.TOKEN_LT)
        }
      case '(' => Token(Kind.TOKEN_LPAREN)
      case ')' => Token(Kind.TOKEN_RPAREN)
      case '[' => Token(Kind.TOKEN_LBRACK)
      case ']' => Token(Kind.TOKEN_RBRACK)
      case '{' => Token(Kind.TOKEN_LBRACE)
      case '}' => Token(Kind.TOKEN_RBRACE)
      case ',' => Token(Kind.TOKEN_COMMER)
      case '.' => Token(Kind.TOKEN_DOT)
      case '!' =>
        mark()
        read match {
          case '=' => Token(Kind.TOKEN_UNEQ)
          case _ =>
            reset()
            Token(Kind.TOKEN_NOT)
        }
      case ':' => Token(Kind.TOKEN_COLON)
      case ';' => Token(Kind.TOKEN_SEMI)
      case '=' =>
        mark()
        read match {
          case '=' => Token(Kind.TOKEN_EQ)
          case _ =>
            reset()
            Token(Kind.TOKEN_ASSIGN)
        }
      case '&' => read match {
        case '&' => Token(Kind.TOKEN_AND)
        case '_' => Failure(bug"found unrecognized token at ($lineRow, $lineCol)")
      }
      case c if Character.isDigit(c) =>
        def build(all: String): String = {
          mark()
          read match {
            case c if Character.isDigit(c) => build(all + c.toChar)
            case _ =>
              reset()
              all
          }
        }
        Token(Kind.TOKEN_NUM, build(c.toChar.toString))
      case c if Character.isAlphabetic(c) =>
        def build(all: String): String = {
          mark()
          read match {
            case c if Character.isAlphabetic(c) || Character.isDigit(c) || '_' == c => build(all + c.toChar)
            case _ =>
              reset()
              all
          }
        }
        val unresolved = build(c.toChar.toString)
        if (lexer.Token.keywords.containsKey(unresolved))
          Token(lexer.Token.keywords.get(unresolved))
        else Token(Kind.TOKEN_ID, unresolved)
    }

  def lookAhead() = {
    val token = nextToken()
    buffer = token :: buffer
    token
  }

  def nextToken(): Token = {
    var ret: Token = null
    buffer match {
      case head :: tail =>
        ret = head
        buffer = tail
      case Nil =>
        nextTokenInternal() match {
          case Success(token) => ret = token
          case Failure(err) =>
            err.printStackTrace()
            System.exit(1)
        }
    }
    if (ConLexer.dump) println(ret.toString)
    ret
  }
}