package logicalexpressionbuilder;

import java.util.HashSet;

public class LogicalConstant extends LogicalExpression {

    private final boolean value;

    public LogicalConstant(boolean value) {
        this.value = value;
    }

    /**
     * Die logische Konstante wird genau dann auf TRUE gesetzt, wenn value == 0.
     */
    public LogicalConstant(int value) {
        this.value = value == 0;
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    public LogicalExpression copy() {
        return new LogicalConstant(this.value);
    }

    @Override
    public boolean evaluate() {
        return this.value;
    }

    @Override
    public void addContainedVars(HashSet vars) {
    }

    @Override
    public boolean contains(String var) {
        return false;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public boolean equals(LogicalExpression logExpr) {
        if (logExpr instanceof LogicalConstant) {
            return this.getValue() == ((LogicalConstant) logExpr).getValue();
        }
        return false;
    }

    @Override
    public boolean equivalent(LogicalExpression logExpr) {
        if (logExpr instanceof LogicalConstant) {
            return this.getValue() == ((LogicalConstant) logExpr).getValue();
        }
        return false;
    }

    @Override
    public String writeLogicalExpression() {
        if (this.value == true) {
            return "1";
        }
        return "0";
    }

    @Override
    public LogicalExpression simplifyTrivial() {
        return this;
    }

    @Override
    public LogicalExpression factorizeInSums() {
        return this;
    }
    
    @Override
    public LogicalExpression factorizeInProducts() {
        return this;
    }
    
}
