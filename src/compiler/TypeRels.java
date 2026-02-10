package compiler;

import compiler.AST.*;
import compiler.lib.*;

public class TypeRels {

    // valuta se il tipo "a" e' <= al tipo "b", dove "a" e "b" sono tipi di base: IntTypeNode o BoolTypeNode
    public static boolean isSubtype(TypeNode a, TypeNode b) {
        if (a instanceof EmptyTypeNode && b instanceof RefTypeNode) return true;
        if (a instanceof ArrowTypeNode aa && b instanceof ArrowTypeNode bb) {
            if (!isSubtype(bb.ret, aa.ret)) return false;
            if (aa.parlist.size() != bb.parlist.size()) return false;
            for (int i = 0; i < aa.parlist.size(); i++) {
                if (!isSubtype(aa.parlist.get(i), bb.parlist.get(i))) return false;
            }
        }
        return a.getClass().equals(b.getClass()) || ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode));
    }

}
