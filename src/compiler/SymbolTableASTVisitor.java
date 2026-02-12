package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.DecNode;
import compiler.lib.Node;
import compiler.lib.TypeNode;

import java.util.*;


public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

    private final List<Map<String, STentry>> symTable = new ArrayList<>();
    private int nestingLevel = 0; // current nesting level
    private int decOffset = -2; // counter for offset of local declarations at current nesting level
    private int classOffset = -2;
    Map<String, Map<String, STentry>> classTable = new HashMap<>();
    int stErrors = 0;

    SymbolTableASTVisitor() {
    }

    SymbolTableASTVisitor(boolean debug) {
        super(debug);
    } // enables print for debugging

    private STentry stLookup(String id) {
        int j = nestingLevel;
        STentry entry = null;
        while (j >= 0 && entry == null)
            entry = symTable.get(j--).get(id);
        return entry;
    }

    @Override
    public Void visitNode(ProgLetInNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = new HashMap<>();
        symTable.add(hm);
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        symTable.removeFirst();
        return null;
    }

    @Override
    public Void visitNode(ProgNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());
        STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.retType), decOffset--);
        //inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        //creare una nuova hashmap per la symTable
        nestingLevel++;
        Map<String, STentry> hmn = new HashMap<>();
        symTable.add(hmn);
        int prevNLDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset = -2;

        int parOffset = 1;
        for (ParNode par : n.parlist)
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        //rimuovere la hashmap corrente poiche' esco dallo scope
        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
        return null;
    }

    @Override
    public Void visitNode(VarNode n) {
        if (print) printNode(n);
        visit(n.exp);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        STentry entry = new STentry(nestingLevel, n.getType(), decOffset--);
        //inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Var id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        return null;
    }

    @Override
    public Void visitNode(PrintNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(IfNode n) {
        if (print) printNode(n);
        visit(n.cond);
        visit(n.th);
        visit(n.el);
        return null;
    }

    @Override
    public Void visitNode(EqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(LessEqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(GreaterEqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(TimesNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(DivNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }


    @Override
    public Void visitNode(PlusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(MinusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(OrNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(AndNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(NotNode n) {
        if (print) printNode(n);
        visit(n.node);
        return null;
    }

    @Override
    public Void visitNode(CallNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(IdNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println("Var or Par id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        return null;
    }

    @Override
    public Void visitNode(ClassNode n) {
        if (print) printNode(n);
        ClassTypeNode type = (ClassTypeNode) n.getType();
        STentry entry = new STentry(nestingLevel, type, classOffset--);
        Set<String> declarations = new HashSet<>();
        if (nestingLevel != 0) {
            System.out.println("Class id " + n.id + " at line " + n.getLine() + " must be declared at nesting level 0");
            stErrors++;
        }
        Map<String, STentry> hm = symTable.get(nestingLevel);
        if (hm.put(n.id, entry) != null) {
            System.out.println("Class " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }

        Map<String, STentry> virtualTable = new HashMap<>();

        if (n.superId != null) {
            Map<String, STentry> parentVirtualTable = classTable.get(n.superId);
            virtualTable.putAll(parentVirtualTable);

            STentry parentEntry = symTable.getFirst().get(n.superId);
            if (!(parentEntry.type instanceof ClassTypeNode parentType)) {
                System.out.println("Parent class " + n.superId + " at line " + n.getLine() + " must be a class.");
                stErrors++;
            } else {
                type.allFields.addAll(parentType.allFields);
                type.allMethods.addAll(parentType.allMethods);
                n.superEntry = parentEntry;
            }
        }

        nestingLevel++;
        symTable.add(virtualTable);
        int fieldOffset = -type.allFields.size() - 1;
        int methodOffset = type.allMethods.size();

        for (FieldNode field : n.fields) {
            if(!declarations.add(field.id)){
                System.out.println("Field "+field.id+" at line "+field.getLine()+" already declared");
                stErrors++;
            }

            if (virtualTable.containsKey(field.id)) {
                STentry oldEntry = virtualTable.get(field.id);
                STentry freshEntry = new STentry(oldEntry.nl, field.getType(), oldEntry.offset);

                int index = type.allFields.indexOf(virtualTable.get(field.id).type); // indice del campo nella classe padre
                type.allFields.set(index, field.getType());
                field.offset = oldEntry.offset;

                virtualTable.put(field.id, freshEntry); // rimpiazziamo la st entry con il tipo aggiornato
            } else {
                virtualTable.put(field.id, new STentry(nestingLevel, field.getType(), fieldOffset));
                type.allFields.add(field.getType());
                field.offset = fieldOffset--;
            }
        }

        for (MethodNode method : n.methods) {
            if(!declarations.add(method.id)){
                System.out.println("Method "+method.id+" at line "+method.getLine()+" already declared");
                stErrors++;
            }
            ArrowTypeNode a = new ArrowTypeNode(method.parlist.stream().map(DecNode::getType).toList(), method.retType);

            if (virtualTable.containsKey(method.id)) {
                STentry oldEntry = virtualTable.get(method.id);
                STentry freshEntry = new STentry(oldEntry.nl, a, oldEntry.offset);

                int index = type.allMethods.indexOf((ArrowTypeNode) virtualTable.get(method.id).type); // indice del metodo nella classe padre
                type.allMethods.set(index, a);
                method.offset = oldEntry.offset;

                virtualTable.put(method.id, freshEntry); // rimpiazziamo la st entry con il tipo aggiornato
            } else {
                virtualTable.put(method.id, new STentry(nestingLevel, a, methodOffset));
                type.allMethods.add(a);
                method.offset = methodOffset++;
            }
            visit(method);
        }

        classTable.put(n.id, virtualTable);
        symTable.remove(nestingLevel--);
        decOffset--;

        return null;
    }

    @Override
    public Void visitNode(MethodNode n) {
        nestingLevel++;
        Map<String, STentry> hmn = new HashMap<>();
        symTable.add(hmn);

        int parOffset = 1;
        for (ParNode par : n.parlist) {
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
        }

        final int prevNLDecOffset = decOffset;
        decOffset = -2;
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);

        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset;
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.objId);
        if (entry == null) {
            System.out.println("Var with id " + n.objId + " at line " + n.getLine() + " is not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
            if (!(n.entry.type instanceof RefTypeNode type)) {
                System.out.println("Var with id " + n.objId + " at line " + n.getLine() + " must be an object");
                stErrors++;
            } else {
                Map<String, STentry> virtualTable = classTable.get(type.id);
                if (virtualTable == null) {
                    System.out.println("Object " + n.objId + "'s class " + type.id + " is not declared");
                    stErrors++;
                } else {
                    n.methodEntry = virtualTable.get(n.methodId);
                }
            }
        }
        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(NewNode n) {
        if (print) printNode(n);
        STentry entry = symTable.getFirst().get(n.id);
        Map<String, STentry> virtualTable = classTable.get(n.id);
        if (entry == null) {
            System.out.println("Class " + n.id + " at line " + n.getLine() + " is not declared");
            stErrors++;
        } else if (virtualTable == null) {
            System.out.println("Variable " + n.id + " at line " + n.getLine() + " must be a class");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) {
        if (print) printNode(n);
        return null;
    }

    @Override
    public Void visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }
}
