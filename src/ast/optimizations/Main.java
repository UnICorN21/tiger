package ast.optimizations;

public class Main {
  public ast.Ast.Program.T program;
  
  public void accept(ast.Ast.Program.T ast) {
    DeadClass dceVisitor = new DeadClass();
    control.CompilerPass deadClassPass = new control.CompilerPass(
        "Dead class elimination", ast, dceVisitor);
    if (!control.Control.skipPass("ast.DeadClass")) {
      deadClassPass.exec();
      ast = dceVisitor.program;
    }

    AlgSimp algVisitor = new AlgSimp();
    control.CompilerPass algPass = new control.CompilerPass(
            "Algebraic simplification", ast, algVisitor);
    if (!control.Control.skipPass("ast.AlgSimp")) {
      algPass.exec();
      ast = algVisitor.program;
    }

    ConstFold cfVisitor = new ConstFold();
    control.CompilerPass constFoldPass = new control.CompilerPass(
            "Const folding", ast, cfVisitor);
    if (!control.Control.skipPass("ast.ConstFold")) {
      constFoldPass.exec();
      ast = cfVisitor.program;
    }

    DeadCode dcodeVisitor = new DeadCode();
    control.CompilerPass deadCodePass = new control.CompilerPass(
        "Dead code elimination", ast, dcodeVisitor);
    if (!control.Control.skipPass("ast.DeadCode")){
      deadCodePass.exec();
      ast = dcodeVisitor.program;
    }

    program = ast;
  }
}
