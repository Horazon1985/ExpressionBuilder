package abstractexpressions.logicalexpression.classes;

import exceptions.EvaluationException;
import java.util.HashSet;

public class LogicalUnaryOperation extends LogicalExpression {

    private final LogicalExpression left;
    private final TypeLogicalUnary type;

    public LogicalUnaryOperation(LogicalExpression left, TypeLogicalUnary type) {
        this.left = left;
        this.type = type;
    }

    public TypeLogicalUnary getType() {
        return this.type;
    }

    public LogicalExpression getLeft() {
        return this.left;
    }

    @Override
    public LogicalExpression copy() {
        return new LogicalUnaryOperation(this.left, this.type);
    }

    @Override
    public boolean evaluate() {
        return !this.left.evaluate();
    }

    @Override
    public void addContainedVars(HashSet vars) {
        this.left.addContainedVars(vars);
    }

    @Override
    public boolean contains(String var) {
        return this.left.contains(var);
    }

    @Override
    public boolean isConstant() {
        return this.left.isConstant();
    }

    @Override
    public boolean equals(LogicalExpression logExpr) {
        return logExpr instanceof LogicalUnaryOperation 
                && this.getType().equals(((LogicalUnaryOperation) logExpr).getType())
                && this.getLeft().equals(((LogicalUnaryOperation) logExpr).getLeft());
    }

    @Override
    public boolean equivalent(LogicalExpression logExpr) {
        return logExpr instanceof LogicalUnaryOperation 
                && this.getType().equals(((LogicalUnaryOperation) logExpr).getType())
                && this.getLeft().equivalent(((LogicalUnaryOperation) logExpr).getLeft());
    }

    @Override
    public String toString() {
        if (this.left instanceof LogicalBinaryOperation) {
            return "!(" + this.left.toString() + ")";
        }
        return "!" + this.left.toString();
    }

    @Override
    public LogicalExpression simplifyBasic() throws EvaluationException {

        //Linken und rechten Teil bei logischen Binäroperationen zunächst separat vereinfachen
        LogicalUnaryOperation logExpr = new LogicalUnaryOperation(this.getLeft().simplifyBasic(), this.getType());

        //Konstante Ausdrücke direkt auswerten.
        if (logExpr.isConstant()) {
            return new LogicalConstant(this.evaluate());
        }

        //Doppelte Negation eliminieren.
        if (logExpr.type.equals(TypeLogicalUnary.NEGATION)
                && this.left.isNeg()) {
            return ((LogicalUnaryOperation) this.left).getLeft();
        }

        return logExpr;

    }

    @Override
    public LogicalExpression factorizeInSums() throws EvaluationException {
        return new LogicalUnaryOperation(this.left.factorizeInSums(), this.type);
    }

    @Override
    public LogicalExpression factorizeInProducts() throws EvaluationException {
        return new LogicalUnaryOperation(this.left.factorizeInSums(), this.type);
    }

}
