import control.CommandLine;
import control.Control;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import static control.Control.ConAst.dumpAst;
import static control.Control.ConAst.testFac;

public class Tiger {
  static Tiger tiger;
  static CommandLine cmd;
  static InputStream fstream;
  public ast.Ast.Program.T theAst;

  // lex and parse
  public void lexAndParse(String fname) {
    Parser parser;

    try {
      fstream = new BufferedInputStream(new FileInputStream(fname));
      parser = new Parser(fname, fstream);

      theAst = parser.parse();

      fstream.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void compile(String fname) {
    // /////////////////////////////////////////////////////
    // to test the pretty printer on the "test/Fac.java" program
    if (testFac) {
      System.out.println("Testing the Tiger compiler on Fac.java starting:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      control.CompilerPass ppPass = new control.CompilerPass(
          "Pretty printing AST", ast.Fac.prog, pp);
      // ppPass.exec();

      // elaborate the given program, this step is necessary
      // for that it will annotate the AST with some
      // informations used by later phase.
      elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
      control.CompilerPass elabPass = new control.CompilerPass(
          "Elaborating the AST", ast.Fac.prog, elab);
      elabPass.exec();

      // optimize the AST
      ast.optimizations.Main optAstPasses = new ast.optimizations.Main();
      control.CompilerPass optAstPass = new control.CompilerPass(
          "Optimizing AST", optAstPasses, ast.Fac.prog);
      optAstPass.exec();
      ast.Fac.prog = optAstPasses.program;

      // code generation
      switch (control.Control.ConCodeGen.codegen) {
      case Bytecode:
        codegen.bytecode.TranslateVisitor trans = new codegen.bytecode.TranslateVisitor();
        control.CompilerPass genBytecodePass = new control.CompilerPass(
            "Bytecode generation", ast.Fac.prog, trans);
        genBytecodePass.exec();
        codegen.bytecode.Ast.Program.T bytecodeAst = trans.program;

        codegen.bytecode.PrettyPrintVisitor ppbc = new codegen.bytecode.PrettyPrintVisitor();
        control.CompilerPass ppBytecodePass = new control.CompilerPass(
            "Bytecode printing", bytecodeAst, ppbc);
        ppBytecodePass.exec();
        break;
      case C:
        // Compile this program to C.
        codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
        control.CompilerPass genCCodePass = new control.CompilerPass(
                "Translation to C code", ast.Fac.prog, transC);
        genCCodePass.exec();
        codegen.C.Ast.Program.T cAst = transC.program;

        if (control.Control.ConAst.dumpC) {
          codegen.C.PrettyPrintVisitor ppC = new codegen.C.PrettyPrintVisitor();
          control.CompilerPass ppCCodePass = new control.CompilerPass(
                  "C code printing", cAst, ppC);
          ppCCodePass.exec();
        }

        // translation to control-flow graph
        cfg.TranslateVisitor transCfg = new cfg.TranslateVisitor();
        control.CompilerPass genCfgCodePass = new control.CompilerPass(
                "Control-flow graph generation", cAst, transCfg);
        genCfgCodePass.exec();
        cfg.Cfg.Program.T cfgAst = transCfg.program;

        // visualize the control-flow graph, if necessary
        if (control.Control.visualize != Control.Visualize_Kind_t.None) {
          cfg.VisualVisitor toDot = new cfg.VisualVisitor();
          control.CompilerPass genDotPass = new control.CompilerPass(
                  "Draw control-flow graph", cfgAst, toDot);
          genDotPass.exec();
        }

        // optimizations on the control-flow graph
        cfg.optimizations.Main cfgOpts = new cfg.optimizations.Main();
        control.CompilerPass cfgOptPass = new control.CompilerPass(
                "Control-flow graph optimizations", cfgOpts, cfgAst);
        cfgOptPass.exec();
        cfgAst = cfgOpts.program;

        cfg.PrettyPrintVisitor ppCfg = new cfg.PrettyPrintVisitor(cfgOpts.livenessIn);
        control.CompilerPass ppCfgCodePass = new control.CompilerPass(
            "C code printing", cfgAst, ppCfg);
        ppCfgCodePass.exec();
        break;
      case Dalvik:
        codegen.dalvik.TranslateVisitor transDalvik = new codegen.dalvik.TranslateVisitor();
        control.CompilerPass genDalvikCodePass = new control.CompilerPass(
            "Dalvik code generation", ast.Fac.prog, transDalvik);
        genDalvikCodePass.exec();
        codegen.dalvik.Ast.Program.T dalvikAst = transDalvik.program;

        codegen.dalvik.PrettyPrintVisitor ppDalvik = new codegen.dalvik.PrettyPrintVisitor();
        control.CompilerPass ppDalvikCodePass = new control.CompilerPass(
            "Dalvik code printing", dalvikAst, ppDalvik);
        ppDalvikCodePass.exec();
        break;
      case X86:
        // similar
        break;
      default:
        break;
      }
      return;
    }

    if (fname == null) {
      cmd.usage();
      return;
    }
    Control.ConCodeGen.fileName = fname;

    // /////////////////////////////////////////////////////
    // it would be helpful to be able to test the lexer
    // independently.
    if (Control.ConLexer.test) {
      System.out.println("Testing the lexer. All tokens:");
      try {
        fstream = new BufferedInputStream(new FileInputStream(fname));
        Lexer lexer = new Lexer(fname, fstream);
        Token token = lexer.nextToken();

        while (token.kind != Token.Kind.TOKEN_EOF) {
          System.out.println(token.toString());
          token = lexer.nextToken();
        }
        fstream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.exit(1);
    }

    // /////////////////////////////////////////////////////////
    // normal compilation phases.

    control.CompilerPass lexAndParsePass = new control.CompilerPass(
        "Lex and parse", tiger, fname);
    lexAndParsePass.doitName("lexAndParse");

    // pretty printing the AST, if necessary
    if (dumpAst) {
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      control.CompilerPass ppAstPass = new control.CompilerPass(
          "Pretty printing the AST", theAst, pp);
      ppAstPass.exec();
    }

    // elaborate the AST, report all possible errors.
    elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
    control.CompilerPass elabAstPass = new control.CompilerPass(
        "Elaborating the AST", theAst, elab);
    elabAstPass.exec();

    // optimize the AST
    ast.optimizations.Main optAstPasses = new ast.optimizations.Main();
    control.CompilerPass optAstPass = new control.CompilerPass(
        "Optimizing the AST", optAstPasses, theAst);
    optAstPass.doitName("accept");
    theAst = optAstPasses.program;

    // code generation
    switch (control.Control.ConCodeGen.codegen) {
    case Bytecode:
      codegen.bytecode.TranslateVisitor trans = new codegen.bytecode.TranslateVisitor();
      control.CompilerPass genBytecodePass = new control.CompilerPass(
          "Bytecode generation", theAst, trans);
      genBytecodePass.exec();
      codegen.bytecode.Ast.Program.T bytecodeAst = trans.program;
      codegen.bytecode.PrettyPrintVisitor ppbc = new codegen.bytecode.PrettyPrintVisitor();
      control.CompilerPass ppBytecodePass = new control.CompilerPass(
          "Bytecode printing", bytecodeAst, ppbc);
      ppBytecodePass.exec();
      break;
    case C:
      codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
      control.CompilerPass genCCodePass = new control.CompilerPass(
          "C code generation", theAst, transC);
      genCCodePass.exec();
      codegen.C.Ast.Program.T cAst = transC.program;

      if (control.Control.ConAst.dumpC) {
        codegen.C.PrettyPrintVisitor ppC = new codegen.C.PrettyPrintVisitor();
        control.CompilerPass ppCCodePass = new control.CompilerPass(
                "C code printing", cAst, ppC);
        ppCCodePass.exec();
      }

      // translation to control-flow graph
      cfg.TranslateVisitor transCfg = new cfg.TranslateVisitor();
      control.CompilerPass genCfgCodePass = new control.CompilerPass(
              "Control-flow graph generation", cAst, transCfg);
      genCfgCodePass.exec();
      cfg.Cfg.Program.T cfgAst = transCfg.program;

      // visualize the control-flow graph, if necessary
      if (control.Control.visualize != Control.Visualize_Kind_t.None) {
        cfg.VisualVisitor toDot = new cfg.VisualVisitor();
        control.CompilerPass genDotPass = new control.CompilerPass(
                "Draw control-flow graph", cfgAst, toDot);
        genDotPass.exec();
      }

      // optimizations on the control-flow graph
      cfg.optimizations.Main cfgOpts = new cfg.optimizations.Main();
      control.CompilerPass cfgOptPass = new control.CompilerPass(
              "Control-flow graph optimizations", cfgOpts, cfgAst);
      cfgOptPass.exec();
      cfgAst = cfgOpts.program;

      cfg.PrettyPrintVisitor ppCfg = new cfg.PrettyPrintVisitor(cfgOpts.livenessIn);
      control.CompilerPass ppCfgCodePass = new control.CompilerPass(
              "C code printing", cfgAst, ppCfg);
      ppCfgCodePass.exec();
      break;
    case Dalvik:
      codegen.dalvik.TranslateVisitor transDalvik = new codegen.dalvik.TranslateVisitor();
      control.CompilerPass genDalvikCodePass = new control.CompilerPass(
          "Dalvik code generation", theAst, transDalvik);
      genDalvikCodePass.exec();
      codegen.dalvik.Ast.Program.T dalvikAst = transDalvik.program;

      codegen.dalvik.PrettyPrintVisitor ppDalvik = new codegen.dalvik.PrettyPrintVisitor();
      control.CompilerPass ppDalvikCodePass = new control.CompilerPass(
          "Dalvik code printing", dalvikAst, ppDalvik);
      ppDalvikCodePass.exec();
      break;
    case X86:
      // similar
      break;
    default:
      break;
    }
  }

  public void link(String str) {
    System.out.println("Start linking...");
    switch (control.Control.ConCodeGen.codegen) {
      case C:
        String outName = str.substring(str.lastIndexOf('/') + 1, str.lastIndexOf(".java"));
        try {
          Process proc = Runtime.getRuntime().exec("gcc " + str + ".c -I runtime -o " + outName);
          int exitVal = proc.waitFor();
          if (0 == exitVal) System.out.println("Link succeed.");
          else throw new RuntimeException("ExitVal is not zero.");
        } catch (Exception e) {
          System.out.println("Link failed.");
          e.printStackTrace();
        }
        break;
      case Bytecode:
        try {
          Process proc = Runtime.getRuntime().exec("java -jar jasmin.jar *.j");
          int exitVal = proc.waitFor();
          if (0 == exitVal) System.out.println("Link succeed.");
          else throw new RuntimeException("ExitVal is not zero.");
        } catch (Exception e) {
          System.out.println("Link failed.");
          e.printStackTrace();
        }
    }
  }

  public void compileAndLink(String fname) {
    // compile
    control.CompilerPass compilePass = new control.CompilerPass("Compile",
        tiger, fname);
    compilePass.doitName("compile");

    // linking
    control.CompilerPass linkPass = new control.CompilerPass("Linking", tiger,
        fname);
    linkPass.doitName("link");
  }

  public static void main(String[] args) {
    // ///////////////////////////////////////////////////////
    // handle command line arguments
    tiger = new Tiger();
    cmd = new CommandLine();
    String fname;
    fname = cmd.scan(args);

    control.CompilerPass tigerAll = new control.CompilerPass("Tiger", tiger,
        fname);
    tigerAll.doitName("compileAndLink");
  }
}
