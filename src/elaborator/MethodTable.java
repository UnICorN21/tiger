package elaborator;

import ast.Ast.Dec;
import ast.Ast.Type;
import util.VarInfo;

import java.util.LinkedList;
import java.util.List;

public class MethodTable {
  private java.util.Hashtable<String, VarInfo> table;

  public MethodTable() {
    this.table = new java.util.Hashtable<>();
  }

  // Duplication is not allowed
  public void put(LinkedList<Dec.T> formals,
      LinkedList<Dec.T> locals) {
    for (Dec.T dec : formals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.table.get(decc.id) != null) {
        System.out.println("duplicated parameter: " + decc.id);
        System.exit(1);
      }
      this.table.put(decc.id, new VarInfo(decc.id, decc.type, decc.pos));
    }

    for (Dec.T dec : locals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.table.get(decc.id) != null) {
        System.out.println("duplicated variable: " + decc.id);
        System.exit(1);
      }
      this.table.put(decc.id, new VarInfo(decc.id, decc.type, decc.pos));
    }

  }

  // return null for non-existing keys
  public Type.T get(String id) {
    VarInfo ret = this.table.get(id);
    if (null != ret) ret.used = true;
    return null == ret ? null : ret.type;
  }

  public List<VarInfo> getUnusedVars() {
    List<VarInfo> ret = new util.Flist<VarInfo>().list();
    this.table.forEach((id, info) -> {
      if (!info.used) ret.add(info);
    });
    return ret;
  }

  public void dump() {
    this.table.forEach((name, info) -> {
      System.out.println(String.format("variable(%s, %s)", name, info.type));
    });
  }

  public void clear() {
    this.table.clear();
  }

  @Override
  public String toString() {
    return this.table.toString();
  }
}
