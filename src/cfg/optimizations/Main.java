package cfg.optimizations;

import cfg.Cfg;
import cfg.Cfg.Program;

import java.util.HashMap;
import java.util.HashSet;

public class Main {
  public Program.T program;

  private HashMap<Cfg.Stm.T, HashSet<String>> stmLiveIn = null;
  private HashMap<Cfg.Stm.T, HashSet<String>> stmLiveOut = null;
  private HashMap<Cfg.Transfer.T, HashSet<String>> transferLiveIn = null;
  private HashMap<Cfg.Transfer.T, HashSet<String>> transferLiveOut = null;

  public void accept(Program.T cfg) {
    // liveness analysis
    LivenessVisitor liveness = new LivenessVisitor();
    control.CompilerPass livenessPass = new control.CompilerPass(
        "Liveness analysis", cfg, liveness);
    if (!control.Control.skipPass("cfg.liveness")) {
      livenessPass.doit();

      stmLiveIn = liveness.getStmLiveIn();
      stmLiveOut = liveness.getStmLiveOut();
      transferLiveIn = liveness.getTransferLiveIn();
      transferLiveOut = liveness.getTransferLiveOut();
    }

    // dead-code elimination
    DeadCode deadCode = new DeadCode();
    control.CompilerPass deadCodePass = new control.CompilerPass(
        "Dead-code elimination", cfg, deadCode);
    if (!control.Control.skipPass("cfg.deadCode")) {
      if (control.Control.skipPass("cfg.liveness")) {
        System.out.println("Warning: Dead code elimination was skipped because of insufficient liveness data.");
      } else {
        deadCode.setStmLiveOut(stmLiveOut);

        deadCodePass.doit();
        cfg = deadCode.program;
      }
    }

    // reaching definition
    ReachingDefinition reachingDef = new ReachingDefinition();
    control.CompilerPass reachingDefPass = new control.CompilerPass(
        "Reaching definition", cfg, reachingDef);
    if (!control.Control.skipPass("cfg.reaching")) {
      reachingDefPass.doit();
      // Export necessary data structures
      // Your code here:
    }

    // constant propagation
    ConstProp constProp = new ConstProp();
    control.CompilerPass constPropPass = new control.CompilerPass(
        "Constant propagation", cfg, constProp);
    if (!control.Control.skipPass("cfg.constProp")) {
      constPropPass.doit();
      cfg = constProp.program;
    }

    // copy propagation
    CopyProp copyProp = new CopyProp();
    control.CompilerPass copyPropPass = new control.CompilerPass(
        "Copy propagation", cfg, copyProp);
    if (!control.Control.skipPass("cfg.copyProp")) {
      copyPropPass.doit();
      cfg = copyProp.program;
    }

    // available expression
    AvailExp availExp = new AvailExp();
    control.CompilerPass availExpPass = new control.CompilerPass(
        "Available expression", cfg, availExp);
    if (!control.Control.skipPass("cfg.availExp")) {
      availExpPass.doit();
      // Export necessary data structures
      // Your code here:
    }

    // CSE
    Cse cse = new Cse();
    control.CompilerPass csePass = new control.CompilerPass(
        "Common subexpression elimination", cfg, cse);
    if (!control.Control.skipPass("cfg.cse")) {
      csePass.doit();
      cfg = cse.program;
    }

    program = cfg;
  }
}
