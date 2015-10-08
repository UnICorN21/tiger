package cfg.optimizations;

import cfg.Cfg;
import cfg.Cfg.Program;

import java.util.HashMap;
import java.util.HashSet;

public class Main {
  public Program.T program;

  private HashMap<Cfg.Stm.T, HashSet<String>> livenessOut = null;
  private HashMap<Cfg.Stm.T, HashSet<Cfg.Stm.T>> reachingDefStmIn = null;
  private HashMap<Cfg.Transfer.T, HashSet<Cfg.Stm.T>> reachingDefTransferIn = null;

  public void accept(Program.T cfg) {
    // liveness analysis
    LivenessVisitor liveness = new LivenessVisitor();
    control.CompilerPass livenessPass = new control.CompilerPass(
        "Liveness analysis", cfg, liveness);
    if (!control.Control.skipPass("cfg.liveness")) {
      livenessPass.exec();

      livenessOut = liveness.getStmLiveOut();
    }

    // dead-code elimination
    DeadCode deadCode = new DeadCode();
    control.CompilerPass deadCodePass = new control.CompilerPass(
        "Dead-code elimination", cfg, deadCode);
    if (!control.Control.skipPass("cfg.deadCode")) {
      if (control.Control.skipPass("cfg.liveness")) {
        System.out.println("Warning: Dead code elimination was skipped because the lack of liveness analysis.");
      } else {
        deadCode.setLivenessOut(livenessOut);
        deadCodePass.exec();
        cfg = deadCode.program;
      }
    }

    // reaching definition
    ReachingDefinition reachingDef = new ReachingDefinition();
    control.CompilerPass reachingDefPass = new control.CompilerPass(
        "Reaching definition", cfg, reachingDef);
    if (!control.Control.skipPass("cfg.reaching")) {
      reachingDefPass.exec();

      reachingDefStmIn = reachingDef.getStmIn();
      reachingDefTransferIn = reachingDef.getTransferIn();
    }

    // constant propagation
    ConstProp constProp = new ConstProp();
    control.CompilerPass constPropPass = new control.CompilerPass(
        "Constant propagation", cfg, constProp);
    if (!control.Control.skipPass("cfg.constProp")) {
      if (control.Control.skipPass("cfg.reaching")) {
        System.out.println("Warning: Constant propagation was skipped because the lack of reaching definition analysis.");
      } else {
        constProp.setReachingDefStmIn(reachingDefStmIn);
        constProp.setReachingDefTransferIn(reachingDefTransferIn);
        constPropPass.exec();
        cfg = constProp.program;
      }
    }

    // copy propagation
    CopyProp copyProp = new CopyProp();
    control.CompilerPass copyPropPass = new control.CompilerPass(
        "Copy propagation", cfg, copyProp);
    if (!control.Control.skipPass("cfg.copyProp")) {
      copyPropPass.exec();
      cfg = copyProp.program;
    }

    // available expression
    AvailExp availExp = new AvailExp();
    control.CompilerPass availExpPass = new control.CompilerPass(
        "Available expression", cfg, availExp);
    if (!control.Control.skipPass("cfg.availExp")) {
      availExpPass.exec();
      // Export necessary data structures
      // Your code here:
    }

    // CSE
    Cse cse = new Cse();
    control.CompilerPass csePass = new control.CompilerPass(
        "Common subexpression elimination", cfg, cse);
    if (!control.Control.skipPass("cfg.cse")) {
      csePass.exec();
      cfg = cse.program;
    }

    program = cfg;
  }
}
