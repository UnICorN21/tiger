import ast.Ast.Program;
import control.CommandLine;
import control.Control;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;

import java.io.*;
import java.util.List;

import static control.Control.ConAst.dumpAst;
import static control.Control.ConAst.testFac;

public class Tiger {
  public static void main(String[] args) {
    InputStream fstream;
    Parser parser;

    // ///////////////////////////////////////////////////////
    // handle command line arguments
    CommandLine cmd = new CommandLine();
    String fname = cmd.scan(args);

    // /////////////////////////////////////////////////////
    // to test the pretty printer on the "test/Fac.java" program
    if (testFac) {
      System.out.println("Testing the Tiger compiler on Fac.java starting:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      ast.Fac.prog.accept(pp);

      // elaborate the given program, this step is necessary
      // for that it will annotate the AST with some
      // informations used by later phase.
      elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
      ast.Fac.prog.accept(elab);

      // Compile this program to C.
      System.out.println("code generation starting");
      // code generation
      switch (control.Control.ConCodeGen.codegen) {
      case Bytecode:
        System.out.println("bytecode codegen");
        codegen.bytecode.TranslateVisitor trans = new codegen.bytecode.TranslateVisitor();
        ast.Fac.prog.accept(trans);
        codegen.bytecode.Ast.Program.T bytecodeAst = trans.program;
        codegen.bytecode.PrettyPrintVisitor ppbc = new codegen.bytecode.PrettyPrintVisitor();
        bytecodeAst.accept(ppbc);
        break;
      case C:
        System.out.println("C codegen");
        codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
        ast.Fac.prog.accept(transC);
        codegen.C.Ast.Program.T cAst = transC.program;
        codegen.C.PrettyPrintVisitor ppc = new codegen.C.PrettyPrintVisitor();
        cAst.accept(ppc);
        break;
      case Dalvik:
        // similar
        break;
      case X86:
        // similar
        break;
      default:
        break;
      }
      System.out.println("Testing the Tiger compiler on Fac.java finished.");
      System.exit(1);
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
    Program.T theAst = null;

    // parsing the file, get an AST.
    try {
      fstream = new BufferedInputStream(new FileInputStream(fname));
      parser = new Parser(fname, fstream);

      theAst = parser.parse();

      fstream.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    // pretty printing the AST, if necessary
    if (dumpAst) {
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      theAst.accept(pp);
    }

    // elaborate the AST, report all possible errors.
    elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
    theAst.accept(elab);

    List<String> outputFilenames = null;

    // code generation
    switch (control.Control.ConCodeGen.codegen) {
    case Bytecode:
      codegen.bytecode.TranslateVisitor trans = new codegen.bytecode.TranslateVisitor();
      theAst.accept(trans);
      codegen.bytecode.Ast.Program.T bytecodeAst = trans.program;
      codegen.bytecode.PrettyPrintVisitor ppbc = new codegen.bytecode.PrettyPrintVisitor();
      bytecodeAst.accept(ppbc);
      outputFilenames = ppbc.getOutputFileNames();
      break;
    case C:
      codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
      theAst.accept(transC);
      codegen.C.Ast.Program.T cAst = transC.program;
      codegen.C.PrettyPrintVisitor ppc = new codegen.C.PrettyPrintVisitor();
      cAst.accept(ppc);
      outputFilenames = ppc.getOutputFileNames();
      break;
    case Dalvik:
      // similar
      break;
    case X86:
      // similar
      break;
    default:
      break;
    }

    System.out.println("Generated IR files are:");
    outputFilenames.stream().forEach(System.out::println);
    System.out.println();

    // Lab3, exercise 6: add some glue code to
    // call gcc to compile the generated C or x86
    // file, or call java to run the bytecode file,
    // or dalvik to run the dalvik bytecode.
    // Your code here:
    Runtime rt = Runtime.getRuntime();
    try {
      Process process = null;
      switch (Control.ConCodeGen.codegen) {
        case Bytecode:
          process = rt.exec("java -jar jasmin.jar " + outputFilenames.stream().reduce("", (all, name) -> all + " " + name));
          break;
        case C:
          process = rt.exec("gcc -w " + outputFilenames.get(0));
          break;
        case Dalvik:
          break;
        case X86:
          break;
        default:
          break;
      }
      int exitVal = process.waitFor();
      InputStreamReader reader = new InputStreamReader(process.getErrorStream());
      LineNumberReader inr = new LineNumberReader(reader);
      String procOutput;
      while ((procOutput = inr.readLine()) != null) {
        System.out.println(procOutput);
      }
      System.out.println(String.format("Compiled %s.", 0 == exitVal ? "succeed" : "failed"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
