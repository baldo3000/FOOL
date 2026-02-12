package compiler;

import compiler.AST.*;
import compiler.lib.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TypeRels {

    public static Map<String, String> superType = new HashMap<>();

    // valuta se il tipo "a" e' <= al tipo "b", dove "a" e "b" sono tipi di base: IntTypeNode o BoolTypeNode
    public static boolean isSubtype(TypeNode a, TypeNode b) {
        if (a instanceof EmptyTypeNode && b instanceof RefTypeNode) return true;
        if (a instanceof ArrowTypeNode aa && b instanceof ArrowTypeNode bb) {
            if (!isSubtype(aa.ret, bb.ret)) return false;
            if (aa.parlist.size() != bb.parlist.size()) return false;
            for (int i = 0; i < aa.parlist.size(); i++) {
                if (!isSubtype(bb.parlist.get(i), aa.parlist.get(i))) return false;
            }
            return true;
        }
        if(a instanceof RefTypeNode aa && b instanceof RefTypeNode bb){
            String aaClass = aa.id;
            while(aaClass != null && !aaClass.equals(bb.id))
                aaClass = superType.get(aaClass);
            return aaClass != null;
        }
        return a.getClass().equals(b.getClass()) || ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode));
    }

    public static TypeNode lowestCommonAncestor(TypeNode a, TypeNode b) {
        if (a instanceof EmptyTypeNode) return b;
        if (b instanceof EmptyTypeNode) return a;
        if (a instanceof RefTypeNode aa && b instanceof RefTypeNode bb) {
            while (true) {
                if (isSubtype(bb, aa)) return aa;
                String superId = superType.get(aa.id);
                if (superId != null) aa = new RefTypeNode(superId);
                else break;
            }
        }
        if(isSubtype(a, new IntTypeNode()) && isSubtype(b, new IntTypeNode())){
            if(a instanceof IntTypeNode || b instanceof IntTypeNode) return new IntTypeNode();
            return new BoolTypeNode();
        }
        return null;
    }

}
