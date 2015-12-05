package ast;

import ast.Ast.*;
import ast.Ast.Exp.*;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import util.Pos;

public class Fac {
  // /////////////////////////////////////////////////////
  // To represent the "Fac.java" program in memory manually
  // this is for demonstration purpose only, and
  // no one would want to do this in reality (boring and error-prone).
  /*
   * class Factorial { public static void main(String[] a) {
   * System.out.println(new Fac().ComputeFac(10)); } } class Fac { public int
   * ComputeFac(int num) { int num_aux; if (num < 1) num_aux = 1; else num_aux =
   * num * (this.ComputeFac(num-1)); return num_aux; } }
   */

  static Pos pos = new Pos(-1, -1);

  // // main class: "Factorial"
  static MainClass.T factorial = new MainClassSingle(
          "Factorial", "a", new Print(new Call(
          new NewObject("Fac", pos), "ComputeFac",
          new util.Flist<Exp.T>().list(new Num(10, pos)), pos)));

  // // class "Fac"
  static ast.Ast.Class.T fac = new ast.Ast.Class.ClassSingle("Fac", null,
          new util.Flist<Dec.T>().list(),
          new util.Flist<Method.T>().list(new Method.MethodSingle(
                  new Type.Int(), "ComputeFac", new util.Flist<Dec.T>()
                  .list(new Dec.DecSingle(new Type.Int(), "num", pos)),
                  new util.Flist<Dec.T>().list(new Dec.DecSingle(
                          new Type.Int(), "num_aux", pos)), new util.Flist<Stm.T>()
                  .list(new If(new Lt(new Id("num", pos),
                          new Num(1, pos), pos), new Assign("num_aux",
                          new Num(1, pos)), new Assign("num_aux",
                          new Times(new Id("num", pos), new Call(
                                  new This(pos), "ComputeFac",
                                  new util.Flist<Exp.T>().list(new Sub(
                                          new Id("num", pos), new Num(1, pos), pos)), pos), pos)))),
                  new Id("num_aux", pos))));

  // program
  public static Program.T prog = new ProgramSingle(factorial,
          new util.Flist<ast.Ast.Class.T>().list(fac));
}
