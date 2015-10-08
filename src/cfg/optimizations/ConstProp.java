package cfg.optimizations;

import cfg.Cfg;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Program;
import cfg.Cfg.Program.ProgramSingle;
import cfg.Cfg.Stm.*;
import cfg.Cfg.Transfer.Goto;
import cfg.Cfg.Transfer.If;
import cfg.Cfg.Transfer.Return;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

public class ConstProp implements cfg.Visitor {
  public Program.T program;

  private Cfg.MainMethod.T mainMethod;
  private Cfg.Method.T method;
  private Cfg.Block.T block;
  private Cfg.Stm.T stm;
  private Cfg.Transfer.T transfer;

  private HashMap<T, HashSet<T>> reachingDefStmIn;
  private HashMap<Cfg.Transfer.T, HashSet<T>> reachingDefTransferIn;
  
  public ConstProp() {
    this.program = null;
    this.mainMethod = null;
    this.method = null;
    this.block = null;
    this.stm = null;
    this.transfer = null;
  }

  public void setReachingDefStmIn(HashMap<T, HashSet<T>> reachingDefStmIn) {
    this.reachingDefStmIn = reachingDefStmIn;
  }

  public void setReachingDefTransferIn(HashMap<Cfg.Transfer.T, HashSet<T>> reachingDefTransferIn) {
    this.reachingDefTransferIn = reachingDefTransferIn;
  }

  private Cfg.Operand.T makePropagate(HashSet<Cfg.Stm.T> rdiset, Cfg.Operand.T target) {
    Cfg.Operand.T ret = target;
    if (target instanceof Cfg.Operand.Var) {
      Set<Cfg.Stm.T> defs = rdiset.stream().filter(s -> s.dst.equals(((Cfg.Operand.Var)target).id))
              .collect(Collectors.toSet());
      if (1 == defs.size()) for (Cfg.Stm.T s: defs) {
        if (s instanceof Move)
          ret = ((Move)s).src;
      }
    }
    return ret;
  }

  // statements
  @Override
  public void visit(Add s) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefStmIn.get(s);
    Cfg.Operand.T left = makePropagate(rdiset, s.left);
    Cfg.Operand.T right = makePropagate(rdiset, s.right);
    if (left.equals(s.left) && right.equals(s.right)) this.stm = s;
    else this.stm = new Add(s.dst, s.ty, left, right);
  }

  @Override
  public void visit(Cfg.Stm.NewIntArray m) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefStmIn.get(m);
    Cfg.Operand.T length = makePropagate(rdiset, m.length);
    if (length.equals(m.length)) this.stm = m;
    else this.stm = new NewIntArray(m.dst, length);
  }

  @Override
  public void visit(Cfg.Stm.And s) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefStmIn.get(s);
    Cfg.Operand.T left = makePropagate(rdiset, s.left);
    Cfg.Operand.T right = makePropagate(rdiset, s.right);
    if (left.equals(s.left) && right.equals(s.right)) this.stm = s;
    else this.stm = new And(s.dst, s.ty, left, right);
  }

  @Override
  public void visit(Cfg.Stm.ArraySelect s) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefStmIn.get(s);
    Cfg.Operand.T index = makePropagate(rdiset, s.index);
    if (index.equals(s.index)) this.stm = s;
    else this.stm = new ArraySelect(s.array, s.dst, index, s.ty);
  }

  @Override
  public void visit(InvokeVirtual s) {
    LinkedList<Cfg.Operand.T> args = new LinkedList<>();
    HashSet<Cfg.Stm.T> rdiset = reachingDefStmIn.get(s);
    s.args.stream().map(arg -> makePropagate(rdiset, arg)).forEach(args::add);
    this.stm = new InvokeVirtual(s.dst, s.obj, s.f, args);
  }

  @Override
  public void visit(Lt s) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefStmIn.get(s);
    Cfg.Operand.T left = makePropagate(rdiset, s.left);
    Cfg.Operand.T right = makePropagate(rdiset, s.right);
    if (left.equals(s.left) && right.equals(s.right)) this.stm = s;
    else this.stm = new Lt(s.dst, s.ty, left, right);
  }

  @Override
  public void visit(Cfg.Stm.Gt s) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefStmIn.get(s);
    Cfg.Operand.T left = makePropagate(rdiset, s.left);
    Cfg.Operand.T right = makePropagate(rdiset, s.right);
    if (left.equals(s.left) && right.equals(s.right)) this.stm = s;
    else this.stm = new Gt(s.dst, s.ty, left, right);
  }

  @Override
  public void visit(AssignArray s) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefStmIn.get(s);
    Cfg.Operand.T index = makePropagate(rdiset, s.index);
    if (index.equals(s.index)) this.stm = s;
    else this.stm = new AssignArray(s.dst, s.ty, index, s.exp);
  }

  @Override
  public void visit(Move s) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefStmIn.get(s);
    Cfg.Operand.T src = makePropagate(rdiset, s.src);
    if (src.equals(s.src)) this.stm = s;
    else this.stm = new Move(s.dst, s.ty, src);
  }

  @Override
  public void visit(Print s) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefStmIn.get(s);
    Cfg.Operand.T arg = makePropagate(rdiset, s.arg);
    if (arg.equals(s.arg)) this.stm = s;
    else this.stm = new Print(arg);
  }

  @Override
  public void visit(Sub s) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefStmIn.get(s);
    Cfg.Operand.T left = makePropagate(rdiset, s.left);
    Cfg.Operand.T right = makePropagate(rdiset, s.right);
    if (left.equals(s.left) && right.equals(s.right)) this.stm = s;
    else this.stm = new Sub(s.dst, s.ty, left, right);
  }

  @Override
  public void visit(NewObject m) {
    this.stm = m;
  }

  @Override
  public void visit(Length s) {
    this.stm = s;
  }

  @Override
  public void visit(Times s) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefStmIn.get(s);
    Cfg.Operand.T left = makePropagate(rdiset, s.left);
    Cfg.Operand.T right = makePropagate(rdiset, s.right);
    if (left.equals(s.left) && right.equals(s.right)) this.stm = s;
    else this.stm = new Times(s.dst, s.ty, left, right);
  }

  // transfer
  @Override
  public void visit(If s) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefTransferIn.get(s);
    Cfg.Operand.T cond = makePropagate(rdiset, s.operand);
    if (cond.equals(s.operand)) this.transfer = s;
    else this.transfer = new If(cond, s.truee, s.falsee);
  }

  @Override
  public void visit(Goto t) {
    this.transfer = t;
  }

  @Override
  public void visit(Return s) {
    HashSet<Cfg.Stm.T> rdiset = reachingDefTransferIn.get(s);
    Cfg.Operand.T ret = makePropagate(rdiset, s.operand);
    if (ret.equals(s.operand)) this.transfer = s;
    else this.transfer = new Return(ret);
  }

  // block
  @Override
  public void visit(BlockSingle b) {
    LinkedList<Cfg.Stm.T> stms = new LinkedList<>();
    b.stms.forEach(stm -> {
      stm.accept(this);
      stms.add(this.stm);
    });
    b.transfer.accept(this);
    this.block = new BlockSingle(b.label, stms, this.transfer);
  }

  // method
  @Override
  public void visit(MethodSingle m) {
    LinkedList<Cfg.Block.T> blocks = new LinkedList<>();
    m.blocks.forEach(b -> {
      b.accept(this);
      blocks.add(this.block);
    });
    this.method = new MethodSingle(m.retType, m.id, m.classId, m.formals, m.locals, blocks, m.entry, m.exit, m.retValue);
  }

  @Override
  public void visit(MainMethodSingle m) {
    LinkedList<Cfg.Block.T> blocks = new LinkedList<>();
    m.blocks.forEach(b -> {
      b.accept(this);
      blocks.add(this.block);
    });
    this.mainMethod = new MainMethodSingle(m.locals, blocks);
  }

  // program
  @Override
  public void visit(ProgramSingle p) {
    p.mainMethod.accept(this);
    LinkedList<Cfg.Method.T> methods = new LinkedList<>();
    for (Cfg.Method.T m: p.methods) {
      m.accept(this);
      methods.add(this.method);
    }

    this.program = new ProgramSingle(p.classes, p.vtables, methods, this.mainMethod);
  }
}
