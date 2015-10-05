package cfg.optimizations;

import cfg.Cfg.*;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.Class.ClassSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Operand.Int;
import cfg.Cfg.Operand.Var;
import cfg.Cfg.Program.ProgramSingle;
import cfg.Cfg.Stm.*;
import cfg.Cfg.Transfer.Goto;
import cfg.Cfg.Transfer.If;
import cfg.Cfg.Transfer.Return;
import cfg.Cfg.Vtable.VtableSingle;
import util.Bug;
import util.Label;

import java.util.*;
import java.util.stream.Collectors;

public class LivenessVisitor implements cfg.Visitor {
  // gen, kill for one statement
  private HashSet<String> oneStmGen;
  private HashSet<String> oneStmKill;

  // gen, kill for one transfer
  private HashSet<String> oneTransferGen;
  private HashSet<String> oneTransferKill;

  // gen, kill for statements
  private HashMap<Stm.T, HashSet<String>> stmGen;
  private HashMap<Stm.T, HashSet<String>> stmKill;

  // gen, kill for transfers
  private HashMap<Transfer.T, HashSet<String>> transferGen;
  private HashMap<Transfer.T, HashSet<String>> transferKill;

  // gen, kill for blocks
  private HashMap<Block.T, HashSet<String>> blockGen;
  private HashMap<Block.T, HashSet<String>> blockKill;

  // liveIn, liveOut for blocks
  private HashMap<Block.T, HashSet<String>> blockLiveIn;
  private HashMap<Block.T, HashSet<String>> blockLiveOut;

  // liveIn, liveOut for statements
  public HashMap<Stm.T, HashSet<String>> stmLiveIn;
  public HashMap<Stm.T, HashSet<String>> stmLiveOut;

  // liveIn, liveOut for transfer
  public HashMap<Transfer.T, HashSet<String>> transferLiveIn;
  public java.util.HashMap<Transfer.T, java.util.HashSet<String>> transferLiveOut;

  // As you will walk the tree for many times, so
  // it will be useful to recored which is which:
  enum Liveness_Kind_t {
    None, StmGenKill, BlockGenKill, BlockInOut, StmInOut,
  }

  private Liveness_Kind_t kind = Liveness_Kind_t.None;

  public LivenessVisitor() {
    this.oneStmGen = new HashSet<>();
    this.oneStmKill = new HashSet<>();

    this.oneTransferGen = new HashSet<>();
    this.oneTransferKill = new HashSet<>();

    this.stmGen = new HashMap<>();
    this.stmKill = new HashMap<>();

    this.transferGen = new HashMap<>();
    this.transferKill = new HashMap<>();

    this.blockGen = new HashMap<>();
    this.blockKill = new HashMap<>();

    this.blockLiveIn = new HashMap<>();
    this.blockLiveOut = new HashMap<>();

    this.stmLiveIn = new HashMap<>();
    this.stmLiveOut = new HashMap<>();

    this.transferLiveIn = new HashMap<>();
    this.transferLiveOut = new HashMap<>();

    this.kind = Liveness_Kind_t.None;
  }

  // /////////////////////////////////////////////////////
  // utilities

  private java.util.HashSet<String> getOneStmGenAndClear() {
    java.util.HashSet<String> temp = this.oneStmGen;
    this.oneStmGen = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneStmKillAndClear() {
    java.util.HashSet<String> temp = this.oneStmKill;
    this.oneStmKill = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneTransferGenAndClear() {
    java.util.HashSet<String> temp = this.oneTransferGen;
    this.oneTransferGen = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneTransferKillAndClear() {
    java.util.HashSet<String> temp = this.oneTransferKill;
    this.oneTransferKill = new java.util.HashSet<>();
    return temp;
  }

  public HashMap<T, HashSet<String>> getStmLiveIn() {
    return stmLiveIn;
  }

  public HashMap<T, HashSet<String>> getStmLiveOut() {
    return stmLiveOut;
  }

  public HashMap<Transfer.T, HashSet<String>> getTransferLiveIn() {
    return transferLiveIn;
  }

  public HashMap<Transfer.T, HashSet<String>> getTransferLiveOut() {
    return transferLiveOut;
  }

  // /////////////////////////////////////////////////////
  // operand
  @Override
  public void visit(Int operand) { /* null */ }

  @Override
  public void visit(Var operand) {
    this.oneStmGen.add(operand.id);
  }

  // statements
  @Override
  public void visit(Add s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
  }

  @Override
  public void visit(Stm.NewIntArray m) {
    this.oneStmKill.add(m.dst);
    m.length.accept(this);
  }

  @Override
  public void visit(Stm.And s) {
    this.oneStmKill.add(s.dst);
    s.left.accept(this);
    s.right.accept(this);
  }

  @Override
  public void visit(Stm.ArraySelect s) {
    this.oneStmKill.add(s.dst);
    s.array.accept(this);
    s.index.accept(this);
  }

  @Override
  public void visit(Stm.Length s) {
    this.oneStmKill.add(s.dst);
    s.array.accept(this);
  }

  @Override
  public void visit(InvokeVirtual s) {
    this.oneStmKill.add(s.dst);
    this.oneStmGen.add(s.obj);
    for (Operand.T arg : s.args) {
      arg.accept(this);
    }
  }

  @Override
  public void visit(Lt s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
  }

  @Override
  public void visit(Stm.Gt s) {
    this.oneStmKill.add(s.dst);
    s.left.accept(this);
    s.right.accept(this);
  }

  @Override
  public void visit(Move s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.src.accept(this);
  }

  @Override
  public void visit(NewObject s) {
    this.oneStmKill.add(s.dst);
  }

  @Override
  public void visit(Print s) {
    s.arg.accept(this);
  }

  @Override
  public void visit(Sub s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
  }

  @Override
  public void visit(Times s) {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
  }

  // transfer
  @Override
  public void visit(If s) {
    // Invariant: accept() of operand modifies "gen"
    if (s.operand instanceof Operand.Var)
      this.oneTransferGen.add(((Var)s.operand).id);
  }

  @Override
  public void visit(Goto s) { /* null */ }

  @Override
  public void visit(Return s) {
    // Invariant: accept() of operand modifies "gen"
    if (s.operand instanceof Operand.Var)
      this.oneTransferGen.add(((Var)s.operand).id);
  }

  // utility functions:
  private void calculateStmTransferGenKill(BlockSingle b) {
    if (0 == b.stms.size()) {
      this.oneStmGen = new HashSet<>();
      this.oneStmKill = new HashSet<>();
    }
    for (Stm.T s : b.stms) {
      this.oneStmGen = new HashSet<>();
      this.oneStmKill = new HashSet<>();
      s.accept(this);
      this.stmGen.put(s, this.oneStmGen);
      this.stmKill.put(s, this.oneStmKill);
      if (control.Control.isTracing("liveness.step1")) {
        System.out.print("\ngen, kill for statement: " + s);
        System.out.print("\ngen is:");
        for (String str : this.oneStmGen) {
          System.out.print(str + ", ");
        }
        System.out.print("\nkill is:");
        for (String str : this.oneStmKill) {
          System.out.print(str + ", ");
        }
      }
    }
    this.oneTransferGen = new HashSet<>();
    this.oneTransferKill = new HashSet<>();
    b.transfer.accept(this);
    this.transferGen.put(b.transfer, this.oneTransferGen);
    this.transferKill.put(b.transfer, this.oneTransferGen);
    if (control.Control.isTracing("liveness.step1")) {
      System.out.print("\ngen, kill for transfer: " + b);
      System.out.print("\ngen is:");
      for (String str : this.oneTransferGen) {
        System.out.print(str + ", ");
      }
      System.out.println("\nkill is:");
      for (String str : this.oneTransferKill) {
        System.out.print(str + ", ");
      }
    }
  }

  private void calculateBlockGenKill(BlockSingle b) {
    HashSet<String> blockKill = new HashSet<>(), blockGen = new HashSet<>();
    if (null != b.transfer) {
      blockGen.addAll(this.transferGen.get(b.transfer));
      blockKill.addAll(this.transferKill.get(b.transfer));
    }
    for (int i = b.stms.size()-1; i >= 0; --i) {
      this.stmKill.get(b.stms.get(i)).stream().forEach(e -> {
        if (blockGen.contains(e)) blockGen.remove(e);
        blockKill.add(e);
      });
      this.stmGen.get(b.stms.get(i)).stream().forEach(blockGen::add);
    }
    this.blockGen.put(b, blockGen);
    this.blockKill.put(b, blockKill);
    if (control.Control.isTracing("liveness.step2")) {
      System.out.print("gen, kill for block " + b.label + ":");
      System.out.print("\n\tgen is: ");
      blockGen.stream().forEach(e -> System.out.print(e + ", "));
      System.out.print("\n\tkill is: ");
      blockKill.stream().forEach(e -> System.out.print(e + ", "));
      System.out.println();
    }
  }

  private boolean calculateBlockInOut(BlockSingle b) {
    HashSet<String> blockIn = new HashSet<>(), blockOut = new HashSet<>();
    b.out.stream().forEach(s -> {
      HashSet<String> sin = this.blockLiveIn.get(s);
      if (null != sin) blockOut.addAll(sin);
    });
    blockIn.addAll(this.blockGen.get(b));
    blockIn.addAll(blockOut.stream().filter(e -> !this.blockKill.get(b).contains(e)).collect(Collectors.toSet()));

    HashSet<String> oldBlockOut = this.blockLiveOut.get(b);
    if (null != oldBlockOut && oldBlockOut.equals(blockOut)) return true;
    else {
      this.blockLiveIn.put(b, blockIn);
      this.blockLiveOut.put(b, blockOut);
      return false;
    }
  }

  private void calculateStmInOut(BlockSingle b) {
    HashSet<String> transferLiveIn = new HashSet<>(), transferLiveOut = this.blockLiveOut.get(b);
    transferLiveIn.addAll(this.transferGen.get(b.transfer));
    transferLiveIn.addAll(transferLiveOut.stream()
            .filter(e -> !this.transferKill.get(b.transfer).contains(e))
            .collect(Collectors.toSet()));
    this.transferLiveIn.put(b.transfer, transferLiveIn);
    this.transferLiveOut.put(b.transfer, transferLiveOut);

    if (control.Control.isTracing("liveness.step4")) {
      System.out.print("in, out for stm " + b.transfer + ":");
      System.out.print("\n\tin is: ");
      transferLiveIn.stream().forEach(e -> System.out.print(e + ", "));
      System.out.print("\n\tout is: ");
      transferLiveOut.stream().forEach(e -> System.out.print(e + ", "));
      System.out.println();
    }

    if (0 == b.stms.size()) return;

    // specialize for the last stm,
    // where transferLiveIn equals to lastStmOut
    Stm.T lastStm = b.stms.getLast();
    HashSet<String> lastStmIn = new HashSet<>();
    lastStmIn.addAll(this.stmGen.get(lastStm));
    lastStmIn.addAll(transferLiveIn.stream()
            .filter(e -> !this.stmKill.get(lastStm).contains(e))
            .collect(Collectors.toSet()));
    this.stmLiveIn.put(lastStm, lastStmIn);
    this.stmLiveOut.put(lastStm, transferLiveIn);

    for (int i = b.stms.size()-2; i >= 0; --i) {
      Stm.T stm = b.stms.get(i);
      HashSet<String> stmIn = new HashSet<>(), stmOut = this.stmLiveIn.get(b.stms.get(i+1));
      stmIn.addAll(this.stmGen.get(stm));
      stmIn.addAll(stmOut.stream().filter(e -> !this.stmKill.get(stm).contains(e))
              .collect(Collectors.toSet()));
      this.stmLiveIn.put(stm, stmIn);
      this.stmLiveOut.put(stm, stmOut);

      if (control.Control.isTracing("liveness.step4")) {
        System.out.print("in, out for stm " + stm + ":");
        System.out.print("\n\tin is: ");
        stmIn.stream().forEach(e -> System.out.print(e + ", "));
        System.out.print("\n\tout is: ");
        stmOut.stream().forEach(e -> System.out.print(e + ", "));
        System.out.println();
      }
    }
  }

  private List<BlockSingle> reverseTopoSort(List<Block.T> blocks) {
    List<BlockSingle> bss = blocks.stream().map(b -> (BlockSingle)b).collect(Collectors.toList());
    for (int i = 0; i < bss.size(); ++i) {
      Transfer.T transfer = bss.get(i).transfer;
      List<Label> ls = new LinkedList<>();
      if (transfer instanceof Goto) ls.add(((Goto)transfer).label);
      else if (transfer instanceof If) {
        Transfer.If iff = (If)transfer;
        ls.add(iff.truee);
        ls.add(iff.falsee);
      }
      for (Label label: ls) {
        for (BlockSingle bs: bss) if (label.equals(bs.label)) {
          bs.in.add(bss.get(i));
          bss.get(i).out.add(bs);
        }
      }
    }

    List<BlockSingle> ret = new LinkedList<>();

    HashSet<BlockSingle> visited = new HashSet<>();
    Stack<BlockSingle> stack = new Stack<>();
    for (BlockSingle b: bss) if (0 == b.in.size()) stack.push(b);
    while (!stack.empty()) {
      BlockSingle bs = stack.pop();
      visited.add(bs);
      if (0 == bs.out.size()) {
        ret.add(bs);
      } else {
        boolean flag = true;
        for (BlockSingle o: bs.out) {
          if (!visited.contains(o)) {
            flag = false;
            stack.push(bs);
            stack.push(o);
            break;
          }
        }
        if (flag) ret.add(bs);
      }
    }

    return ret;
  }

  // block
  @Override
  public void visit(BlockSingle b) {
    switch (this.kind) {
      case StmGenKill:
        calculateStmTransferGenKill(b);
        break;
      case BlockGenKill:
        calculateBlockGenKill(b);
        break;
      case StmInOut:
        calculateStmInOut(b);
        break;
      default:
        // this should never reach
        new Bug();
        break;
    }
  }

  // method
  @Override
  public void visit(MethodSingle m) {
    // Four steps:
    // Step 1: calculate the "gen" and "kill" sets for each
    // statement and transfer
    this.kind = Liveness_Kind_t.StmGenKill;
    m.blocks.stream().forEach(b -> b.accept(this));

    // Step 2: calculate the "gen" and "kill" sets for each block.
    this.kind = Liveness_Kind_t.BlockGenKill;
    m.blocks.stream().forEach(b -> b.accept(this));

    // Step 3: calculate the "liveIn" and "liveOut" sets for each block
    this.kind = Liveness_Kind_t.BlockInOut;
    List<BlockSingle> rts = reverseTopoSort(m.blocks);
    while (true) {
      boolean fixed = true;
      for (BlockSingle b: rts) fixed = calculateBlockInOut(b) && fixed;
      if (fixed) break;
    }
    if (control.Control.isTracing("liveness.step3")) {
      rts.stream().forEach(b -> {
        System.out.print("\nin, out for block " + b.label + ":");
        System.out.print("\n\tin: ");
        this.blockLiveIn.get(b).stream().forEach(e -> System.out.print(e + ", "));
        System.out.print("\n\tout: ");
        this.blockLiveOut.get(b).stream().forEach(e -> System.out.print(e + ", "));
        System.out.println();
      });
    }

    // Step 4: calculate the "liveIn" and "liveOut" sets for each
    // statement and transfer
    this.kind = Liveness_Kind_t.StmInOut;
    m.blocks.stream().forEach(b -> b.accept(this));
  }

  @Override
  public void visit(MainMethodSingle m) {
    // Four steps:
    // Step 1: calculate the "gen" and "kill" sets for each
    // statement and transfer
    this.kind = Liveness_Kind_t.StmGenKill;
    m.blocks.stream().forEach(b -> b.accept(this));

    // Step 2: calculate the "gen" and "kill" sets for each block.
    this.kind = Liveness_Kind_t.BlockGenKill;
    m.blocks.stream().forEach(b -> b.accept(this));

    // Step 3: calculate the "liveIn" and "liveOut" sets for each block
    this.kind = Liveness_Kind_t.BlockInOut;
    boolean fixed = false;
    List<BlockSingle> rts = reverseTopoSort(m.blocks);
    while (!fixed) {
      for (BlockSingle b: rts)
        fixed = calculateBlockInOut(b);
    }

    // Step 4: calculate the "liveIn" and "liveOut" sets for each
    // statement and transfer
    this.kind = Liveness_Kind_t.StmInOut;
    m.blocks.stream().forEach(b -> b.accept(this));
  }

  // vtables
  @Override
  public void visit(VtableSingle v) { /* null */ }

  // class
  @Override
  public void visit(ClassSingle c) { /* null */ }

  // program
  @Override
  public void visit(ProgramSingle p) {
    p.mainMethod.accept(this);
    for (Method.T mth : p.methods) {
      mth.accept(this);
    }
  }
}
