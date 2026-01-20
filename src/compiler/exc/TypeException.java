package compiler.exc;

import compiler.lib.*;

import java.io.Serial;

public class TypeException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public String text;

    public TypeException(String t, int line) {
        FOOLlib.typeErrors++;
        text = t + " at line " + line;
    }

}
