package lexer;

import lexer.Token.Kind;
import util.Bug;

import java.io.InputStream;
import java.util.Stack;

import static control.Control.ConLexer.dump;

/**
 * This file only remains for debug issue
 */
public class JavaLexer {
  String fname; // the input file name to be compiled
  InputStream fstream; // input stream for the above file
  Stack<Token> buffer; // buffer for the rollback token

  int lineRow = 1;
  int lineCol = 0;

  public String getFname() {
    return fname;
  }

  public JavaLexer(String fname, InputStream fstream) {
    this.fname = fname;
    this.fstream = fstream;
    this.buffer = new Stack<>();
  }

  private int read() throws Exception {
    int c = this.fstream.read();
    if ('\n' == c) { ++lineRow; lineCol = 0; }
    else ++lineCol;
    return c;
  }

  private void mark() throws Exception {
    this.fstream.mark(0);
  }

  private void reset() throws Exception {
    this.fstream.reset();
    --lineCol;
  }

  private int skip(int c) throws Exception {
    while (true) {
      if (' ' == c || '\t' == c || '\n' == c) c = skipBlanks(c);
      else if ('/' == c) c = skipComments(c);
      else break;
    }
    return c;
  }

  private int skipBlanks(int c) throws Exception {
    while (' ' == c || '\t' == c || '\n' == c) {
      c = read();
    }
    return c;
  }

  private int skipComments(int c) throws Exception {
    c = read();
    if ('/' == c) {
      do {
        c = read();
      } while ('\n' != c);
      c = read();
      return c;
    } else if ('*' == c) {
      while (true) {
        do {
          c = read();
        } while ('*' == c);
        c = read();
        if ('/' == c) {
          c = read();
          return c;
        }
      }
    } else {
      new Bug();
      return -1;
    }
  }

  // When called, return the next token (refer to the code "Token.java")
  // from the input stream.
  // Return TOKEN_EOF when reaching the end of the input stream.
  private Token nextTokenInternal() throws Exception {
    int c = read();
    if (-1 == c)
      // The value for "lineRow" is now "null",
      // you should modify this to an appropriate
      // line number for the "EOF" token.
      return new Token(Kind.TOKEN_EOF, null, null);

    // skip all kinds of "blanks" and "comments"
    c = skip(c);

    if (-1 == c)
      return new Token(Kind.TOKEN_EOF, null, null);

    switch (c) {
      case '+': return new Token(Kind.TOKEN_ADD, lineRow, lineCol);
      case '-': return new Token(Kind.TOKEN_SUB, lineRow, lineCol);
      case '*': return new Token(Kind.TOKEN_TIMES, lineRow, lineCol);
      case '>':
        mark();
        c = read();
        if ('=' == c) return new Token(Kind.TOKEN_GE, lineRow, lineCol - 1);
        else { reset(); return new Token(Kind.TOKEN_GT, lineRow, lineCol); }
      case '<':
        mark();
        c = read();
        if ('=' == c) return new Token(Kind.TOKEN_LE, lineRow, lineCol - 1);
        else { reset(); return new Token(Kind.TOKEN_LT, lineRow, lineCol); }
      case '(': return new Token(Kind.TOKEN_LPAREN, lineRow, lineCol);
      case ')': return new Token(Kind.TOKEN_RPAREN, lineRow, lineCol);
      case '[': return new Token(Kind.TOKEN_LBRACK, lineRow, lineCol);
      case ']': return new Token(Kind.TOKEN_RBRACK, lineRow, lineCol);
      case '{': return new Token(Kind.TOKEN_LBRACE, lineRow, lineCol);
      case '}': return new Token(Kind.TOKEN_RBRACE, lineRow, lineCol);
      case ',': return new Token(Kind.TOKEN_COMMER, lineRow, lineCol);
      case '.': return new Token(Kind.TOKEN_DOT, lineRow, lineCol);
      case '!': return new Token(Kind.TOKEN_NOT, lineRow, lineCol);
      case ':': return new Token(Kind.TOKEN_COLON, lineRow, lineCol);
      case ';': return new Token(Kind.TOKEN_SEMI, lineRow, lineCol);
      case '=': return new Token(Kind.TOKEN_ASSIGN, lineRow, lineCol);
      case '&':
        c = read();
        if ('&' == c) return new Token(Kind.TOKEN_AND, lineRow, lineCol - 1);
        else {
          new Bug();
          return null;
        }
      default:
        if (Character.isDigit(c)) {
          StringBuffer buffer = new StringBuffer();
          do {
            buffer.append((char)c);
            mark();
            c = read();
          } while (Character.isDigit(c));
          reset();
          return new Token(Kind.TOKEN_NUM, lineRow, lineCol - buffer.length(), buffer.toString());
        } else if (Character.isAlphabetic(c)) {
          StringBuffer buffer = new StringBuffer();
          do {
            buffer.append((char)c);
            mark();
            c = read();
          } while (Character.isAlphabetic(c) || Character.isDigit(c) || '_' == c);
          reset();
          String unresolvedStr = buffer.toString();
          for(String keyword: Token.keywords.keySet()) {
            if (keyword.equals(unresolvedStr))
              return new Token(Token.keywords.get(keyword), lineRow, lineCol - unresolvedStr.length() + 1);
          }
          return new Token(Kind.TOKEN_ID, lineRow, lineCol - unresolvedStr.length() + 1, unresolvedStr);
        }
        return null;
    }
  }

 public Token lookAhead() {
   Token t = this.nextToken();
   buffer.push(t);
   return t;
 }

  public Token nextToken() {
    Token t = null;
    if (!buffer.isEmpty()) {
      t = this.buffer.pop();
    } else try {
      t = this.nextTokenInternal();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    if (dump)
      System.out.println(t.toString());
    return t;
  }
}
