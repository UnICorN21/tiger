package parser;

import ast.Ast;
import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;
import util.Pos;

import java.util.LinkedList;

public class Parser {
  Lexer lexer;
  Token current;

  public Parser(String fname, java.io.InputStream fstream) {
    lexer = new Lexer(fname, fstream);
    current = lexer.nextToken();
  }

  // /////////////////////////////////////////////
  // utility methods to connect the lexer
  // and the parser.


  private void advance() {
    current = lexer.nextToken();
  }

  private String eatToken(Kind kind) {
    String ret = current.lexeme;
    if (kind == current.kind) {
      advance();
    }
    else {
      ret = null;
      System.out.println("Expects: " + kind.toString());
      System.out.println("But got: " + current.kind.toString() +
              " at (" + current.lineRow + ", " + current.lineCol + ") in " + lexer.fname());
      advance();
//      System.exit(1);
    }
    return ret;
  }

  private Pos getCurrentPos() {
    return new Pos(current.lineRow, current.lineCol);
  }

  private void error() {
    System.out.println("Syntax error: compilation aborting...\n");
    System.exit(1);
  }

  // ////////////////////////////////////////////////////////////
  // below are method for parsing.

  // A bunch of parsing methods to parse expressions. The messy
  // parts are to deal with precedence and associativity.

  // ExpList -> Exp ExpRest*
  // ->
  // ExpRest -> , Exp
  private LinkedList<Ast.Exp.T> parseExpList() {
    LinkedList<Ast.Exp.T> ret = new util.Flist<Ast.Exp.T>().list();
    if (current.kind == Kind.TOKEN_RPAREN)
      return ret;
    Ast.Exp.T first = parseExp();
    ret.add(first);
    while (current.kind == Kind.TOKEN_COMMER) {
      advance();
      Ast.Exp.T exp = parseExp();
      ret.add(exp);
    }
    return ret;
  }

  // AtomExp -> (exp)
  // -> INTEGER_LITERAL
  // -> true
  // -> false
  // -> this
  // -> classType
  // -> new int [exp]
  // -> new classType ()
  private Ast.Exp.T parseAtomExp() {
    Ast.Exp.T ret = null;
    Pos pos = getCurrentPos();
    switch (current.kind) {
    case TOKEN_LPAREN: {
      advance();
      ret = parseExp();
      eatToken(Kind.TOKEN_RPAREN);
      break;
    }
    case TOKEN_NUM: {
      String num = eatToken(Kind.TOKEN_NUM);
      ret = new Ast.Exp.Num(Integer.valueOf(num), pos);
      break;
    }
    case TOKEN_TRUE: {
      advance();
      ret = new Ast.Exp.True(pos);
      break;
    }
    case TOKEN_FALSE: {
      advance();
      ret = new Ast.Exp.False(pos);
      break;
    }
    case TOKEN_THIS: {
      advance();
      ret = new Ast.Exp.This(pos);
      break;
    }
    case TOKEN_ID: {
      String id = eatToken(Kind.TOKEN_ID);
      ret = new Ast.Exp.Id(id, pos);
      break;
    }
    case TOKEN_NEW: {
      advance();
      switch (current.kind) {
      case TOKEN_INT: {
        advance();
        eatToken(Kind.TOKEN_LBRACK);
        Ast.Exp.T exp = parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        ret = new Ast.Exp.NewIntArray(exp, pos);
        break;
      }
      case TOKEN_ID: {
        String id = eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LPAREN);
        eatToken(Kind.TOKEN_RPAREN);
        ret = new Ast.Exp.NewObject(id, pos);
        break;
      }
      default:
        error();
      }
      break;
    }
    default:
      error();
    }
    return ret;
  }

  // NotExp -> AtomExp
  // -> AtomExp .classType (expList)
  // -> AtomExp [exp]
  // -> AtomExp .length
  private Ast.Exp.T parseNotExp() {
    Ast.Exp.T ret = parseAtomExp();
    while (current.kind == Kind.TOKEN_DOT || current.kind == Kind.TOKEN_LBRACK) {
      Pos pos = getCurrentPos();
      if (current.kind == Kind.TOKEN_DOT) {
        advance();
        if (current.kind == Kind.TOKEN_LENGTH) {
          advance();
          ret = new Ast.Exp.Length(ret, pos);
          return ret;
        }
        String id = eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LPAREN);
        LinkedList<Ast.Exp.T> params = parseExpList();
        eatToken(Kind.TOKEN_RPAREN);
        ret = new Ast.Exp.Call(ret, id, params, pos);
      } else {
        advance();
        Ast.Exp.T exp = parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        ret = new Ast.Exp.ArraySelect(ret, exp, pos);
      }
    }
    return ret;
  }

  // TimesExp -> ! TimesExp
  // -> NotExp
  private Ast.Exp.T parseTimesExp() {
    Ast.Exp.T ret = null;
    int cnt = 0;
    Pos pos = getCurrentPos();
    while (current.kind == Kind.TOKEN_NOT) {
      advance();
      ++cnt;
    }
    Ast.Exp.T exp = parseNotExp();
    if (cnt-- > 0) ret = new Ast.Exp.Not(exp, pos);
    else ret = exp;
    while (cnt-- > 0) {
      ret = new Ast.Exp.Not(ret, pos);
    }
    return ret;
  }

  // AddSubExp -> TimesExp * TimesExp
  // -> TimesExp
  private Ast.Exp.T parseAddSubExp() {
    Ast.Exp.T ret = parseTimesExp();
    while (current.kind == Kind.TOKEN_TIMES) {
      Pos pos = getCurrentPos();
      advance();
      Ast.Exp.T exp = parseTimesExp();
      ret = new Ast.Exp.Times(ret, exp, pos);
    }
    return ret;
  }

  // LtExp -> AddSubExp + AddSubExp
  // -> AddSubExp - AddSubExp
  // -> AddSubExp
  private Ast.Exp.T parseLtExp() {
    Ast.Exp.T ret = parseAddSubExp();
    while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
      boolean select = true;
      if (current.kind == Kind.TOKEN_SUB) select = false;
      Pos pos = getCurrentPos();
      advance();
      Ast.Exp.T exp = parseAddSubExp();
      if (select) ret = new Ast.Exp.Add(ret, exp, pos);
      else ret = new Ast.Exp.Sub(ret, exp, pos);
    }
    return ret;
  }

  // AndExp -> LtExp < LtExp
  // -> LtExp
  private Ast.Exp.T parseAndExp() {
    Ast.Exp.T ret = parseLtExp();
    while (current.kind == Kind.TOKEN_LT || current.kind == Kind.TOKEN_LE
            || current.kind == Kind.TOKEN_GT || current.kind == Kind.TOKEN_GE) {
      Pos pos = getCurrentPos();
      Kind tokenKind = current.kind;
      advance();
      Ast.Exp.T exp = parseLtExp();
      switch (tokenKind) {
        case TOKEN_LT: ret = new Ast.Exp.Lt(ret, exp, pos); break;
        case TOKEN_GT: ret = new Ast.Exp.Gt(ret, exp, pos); break;
        default: break;
      }
    }
    return ret;
  }

  // Exp -> AndExp && AndExp
  // -> AndExp
  private Ast.Exp.T parseExp() {
    Ast.Exp.T ret = parseAndExp();
    while (current.kind == Kind.TOKEN_AND) {
      Pos pos = getCurrentPos();
      advance();
      Ast.Exp.T ele = parseAndExp();
      ret = new Ast.Exp.And(ret, ele, pos);
    }
    return ret;
  }

  // Statement -> { Statement* }
  // -> if ( Exp ) Statement else Statement
  // -> while ( Exp ) Statement
  // -> System.out.println ( Exp ) ;
  // -> classType = Exp ;
  // -> classType [ Exp ]= Exp ;
  private Ast.Stm.T parseStatement() {
    Ast.Stm.T stm = null;
    switch (current.kind) {
      case TOKEN_LBRACE: {
        advance();
        LinkedList<Ast.Stm.T> stms = parseStatements();
        eatToken(Kind.TOKEN_RBRACE);
        stm = new Ast.Stm.Block(stms);
        break;
      }
      case TOKEN_IF: {
        advance();
        eatToken(Kind.TOKEN_LPAREN);
        Ast.Exp.T cond = parseExp();
        eatToken(Kind.TOKEN_RPAREN);
        Ast.Stm.T iff = parseStatement();
        eatToken(Kind.TOKEN_ELSE);
        Ast.Stm.T elsee = parseStatement();
        stm = new Ast.Stm.If(cond, iff, elsee);
        break;
      }
      case TOKEN_WHILE: {
        advance();
        eatToken(Kind.TOKEN_LPAREN);
        Ast.Exp.T cond = parseExp();
        eatToken(Kind.TOKEN_RPAREN);
        Ast.Stm.T block = parseStatement();
        stm = new Ast.Stm.While(cond, block);
        break;
      }
      case TOKEN_SYSTEM: {
        advance();
        eatToken(Kind.TOKEN_DOT);
        eatToken(Kind.TOKEN_OUT);
        eatToken(Kind.TOKEN_DOT);
        eatToken(Kind.TOKEN_PRINTLN);
        eatToken(Kind.TOKEN_LPAREN);
        Ast.Exp.T printEntity = parseExp();
        eatToken(Kind.TOKEN_RPAREN);
        eatToken(Kind.TOKEN_SEMI);
        stm = new Ast.Stm.Print(printEntity);
        break;
      }
      case TOKEN_ID: {
        String id = eatToken(Kind.TOKEN_ID);
        Ast.Exp.T index = null;
        if (Kind.TOKEN_LBRACK == current.kind) {
          advance();
          index = parseExp();
          eatToken(Kind.TOKEN_RBRACK);
        }
        eatToken(Kind.TOKEN_ASSIGN);
        Ast.Exp.T exp = parseExp();
        eatToken(Kind.TOKEN_SEMI);
        if (null != index) stm = new Ast.Stm.AssignArray(id, index, exp);
        else stm = new Ast.Stm.Assign(id, exp);
        break;
      }
      default:
        error();
    }
    return stm;
  }

  // Statements -> Statement Statements
  // ->
  private LinkedList<Ast.Stm.T> parseStatements() {
    LinkedList<Ast.Stm.T> stms = new util.Flist<Ast.Stm.T>().list();
    while (current.kind == Kind.TOKEN_LBRACE || current.kind == Kind.TOKEN_IF
        || current.kind == Kind.TOKEN_WHILE
        || current.kind == Kind.TOKEN_SYSTEM || current.kind == Kind.TOKEN_ID) {
      Ast.Stm.T stm = parseStatement();
      stms.add(stm);
    }
    return stms;
  }

  // Type -> int []
  // -> boolean
  // -> int
  // -> classType
  private Ast.Type.T parseType() {
    Ast.Type.T ret = null;
    switch (current.kind) {
      case TOKEN_INT:
        boolean isArray = false;
        advance();
        if (Kind.TOKEN_LBRACK == current.kind) {
          isArray = true;
          advance();
          eatToken(Kind.TOKEN_RBRACK);
        }
        if (isArray) ret = new Ast.Type.IntArray();
        else ret = new Ast.Type.Int();
        break;
      case TOKEN_BOOLEAN:
        advance();
        ret = new Ast.Type.Boolean();
        break;
      case TOKEN_ID:
        String id = eatToken(Kind.TOKEN_ID);
        ret = new Ast.Type.ClassType(id);
        break;
      default:
        error();
    }
    return ret;
  }

  // VarDecl -> Type classType ;
  private Ast.Dec.T parseVarDecl() {
    // to parse the "Type" nonterminal in this method, instead of writing
    // a fresh one.
    Ast.Type.T type = parseType();
    Pos pos = getCurrentPos();
    String id = eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_SEMI);
    return new Ast.Dec.DecSingle(type, id, pos);
  }

  // VarDecls -> VarDecl VarDecls
  // ->
  private LinkedList<Ast.Dec.T> parseVarDecls() {
    LinkedList<Ast.Dec.T> ret = new util.Flist<Ast.Dec.T>().list();
    while (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
            || (current.kind == Kind.TOKEN_ID && lexer.lookAhead().kind == Kind.TOKEN_ID)) {
      Ast.Dec.T dec = parseVarDecl();
      ret.add(dec);
    }
    return ret;
  }

  // FormalList -> Type classType FormalRest*
  // ->
  // FormalRest -> , Type classType
  private LinkedList<Ast.Dec.T> parseFormalList() {
    LinkedList<Ast.Dec.T> ret = new util.Flist<Ast.Dec.T>().list();
    if (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
      Ast.Type.T type = parseType();
      Pos pos = getCurrentPos();
      String id = eatToken(Kind.TOKEN_ID);
      ret.add(new Ast.Dec.DecSingle(type, id, pos));
      while (current.kind == Kind.TOKEN_COMMER) {
        advance();
        type = parseType();
        pos = getCurrentPos();
        id = eatToken(Kind.TOKEN_ID);
        ret.add(new Ast.Dec.DecSingle(type, id, pos));
      }
    }
    return ret;
  }

  // Method -> public Type classType ( FormalList )
  // { VarDecl* Statement* return Exp ;}
  private Ast.Method.T parseMethod() {
    eatToken(Kind.TOKEN_PUBLIC);
    Ast.Type.T retType = parseType();
    String id = eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_LPAREN);
    LinkedList<Ast.Dec.T> formalList = parseFormalList();
    eatToken(Kind.TOKEN_RPAREN);
    eatToken(Kind.TOKEN_LBRACE);
    LinkedList<Ast.Dec.T> localDecs = parseVarDecls();
    LinkedList<Ast.Stm.T> localStms = parseStatements();
    eatToken(Kind.TOKEN_RETURN);
    Ast.Exp.T retexp = parseExp();
    eatToken(Kind.TOKEN_SEMI);
    eatToken(Kind.TOKEN_RBRACE);
    return new Ast.Method.MethodSingle(retType, id, formalList, localDecs, localStms, retexp);
  }

  // MethodDecls -> MethodDecl MethodDecls
  // ->
  private LinkedList<Ast.Method.T> parseMethodDecls() {
    LinkedList<Ast.Method.T> ret = new util.Flist<Ast.Method.T>().list();
    while (current.kind == Kind.TOKEN_PUBLIC) {
      Ast.Method.T method = parseMethod();
      ret.add(method);
    }
    return ret;
  }

  // ClassDecl -> class classType { VarDecl* MethodDecl* }
  // -> class classType extends classType { VarDecl* MethodDecl* }
  private Ast.Class.T parseClassDecl() {
    eatToken(Kind.TOKEN_CLASS);
    String id = eatToken(Kind.TOKEN_ID);
    String extendId = null;
    if (current.kind == Kind.TOKEN_EXTENDS) {
      eatToken(Kind.TOKEN_EXTENDS);
      extendId = eatToken(Kind.TOKEN_ID);
    }
    eatToken(Kind.TOKEN_LBRACE);
    LinkedList<Ast.Dec.T> localVars = parseVarDecls();
    LinkedList<Ast.Method.T> methods = parseMethodDecls();
    eatToken(Kind.TOKEN_RBRACE);
    return new Ast.Class.ClassSingle(id, extendId, localVars, methods);
  }

  // ClassDecls -> ClassDecl ClassDecls
  // ->
  private LinkedList<Ast.Class.T> parseClassDecls() {
    LinkedList<Ast.Class.T> ret = new util.Flist<Ast.Class.T>().list();
    while (current.kind == Kind.TOKEN_CLASS) {
      Ast.Class.T clazz = parseClassDecl();
      ret.add(clazz);
    }
    return ret;
  }

  // MainClass -> class classType {
  //   public static void main ( String [] classType ) {
  //     Statement
  //   }
  // }
  private Ast.MainClass.T parseMainClass() {
    eatToken(Kind.TOKEN_CLASS);
    String id = eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_LBRACE);
    eatToken(Kind.TOKEN_PUBLIC);
    eatToken(Kind.TOKEN_STATIC);
    eatToken(Kind.TOKEN_VOID);
    eatToken(Kind.TOKEN_MAIN);
    eatToken(Kind.TOKEN_LPAREN);
    eatToken(Kind.TOKEN_STRING);
    eatToken(Kind.TOKEN_LBRACK);
    eatToken(Kind.TOKEN_RBRACK);
    String args = eatToken(Kind.TOKEN_ID);
    eatToken(Kind.TOKEN_RPAREN);
    eatToken(Kind.TOKEN_LBRACE);
    Ast.Stm.T stm = parseStatement();
    eatToken(Kind.TOKEN_RBRACE);
    eatToken(Kind.TOKEN_RBRACE);
    return new Ast.MainClass.MainClassSingle(id, args, stm);
  }

  // Program -> MainClass ClassDecl*
  private Ast.Program.ProgramSingle parseProgram() {
    Ast.MainClass.T mainClazz = parseMainClass();
    LinkedList<Ast.Class.T> clazzes = parseClassDecls();
    eatToken(Kind.TOKEN_EOF);
    return new Ast.Program.ProgramSingle(mainClazz, clazzes);
  }

  public ast.Ast.Program.T parse() {
    return parseProgram();
  }
}
