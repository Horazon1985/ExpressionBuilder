package logicalsimplifymethods;

import logicalexpressionbuilder.LogicalBinaryOperation;
import logicalexpressionbuilder.LogicalConstant;
import logicalexpressionbuilder.LogicalExpression;
import static logicalexpressionbuilder.LogicalExpression.FALSE;
import static logicalexpressionbuilder.LogicalExpression.TRUE;
import logicalexpressionbuilder.LogicalUnaryOperation;
import logicalexpressionbuilder.TypeLogicalBinary;

public class SimplifyLogicalBinaryOperationMethods {

    public static LogicalExpression trivialOperationsWithFalseTrue(LogicalBinaryOperation logExpr) {

        // Triviale Umformungen
        // 1=a oder 0=a zu a=1 oder a=0 machen.
        if (logExpr.getType().equals(TypeLogicalBinary.EQUIVALENCE)
                && logExpr.getLeft().isConstant()) {
            return logExpr.getRight().equiv(logExpr.getLeft());
        }
        
        // a>a = 1
        if (logExpr.getType().equals(TypeLogicalBinary.IMPLICATION) && logExpr.getLeft().equivalent(logExpr.getRight())) {
            return TRUE;
        }

        // a=!a = 0 und !a=a = 0
        if (logExpr.getType().equals(TypeLogicalBinary.EQUIVALENCE)) {
            if (logExpr.getLeft().isNeg() && ((LogicalUnaryOperation) logExpr.getLeft()).getLeft().equals(logExpr.getRight())) {
                return FALSE;
            }
            if (logExpr.getRight().isNeg() && ((LogicalUnaryOperation) logExpr.getRight()).getLeft().equals(logExpr.getLeft())) {
                return FALSE;
            }
        }

        // !a|a = 1 und !a&a = 0
        if ((logExpr.getType().equals(TypeLogicalBinary.OR) || logExpr.getType().equals(TypeLogicalBinary.AND))
                && logExpr.getLeft().isNeg()
                && logExpr.getRight().equals(((LogicalUnaryOperation) logExpr.getLeft()).getLeft())) {
            return new LogicalConstant(logExpr.getType().equals(TypeLogicalBinary.OR));
        }

        // a|!a = 1 und a&!a = 0
        if ((logExpr.getType().equals(TypeLogicalBinary.OR) || logExpr.getType().equals(TypeLogicalBinary.AND))
                && logExpr.getRight().isNeg()
                && logExpr.getLeft().equals(((LogicalUnaryOperation) logExpr.getRight()).getLeft())) {
            return new LogicalConstant(logExpr.getType().equals(TypeLogicalBinary.OR));
        }

        // 0>a = 1
        if (logExpr.getType().equals(TypeLogicalBinary.IMPLICATION)
                && logExpr.getLeft().isConstant() && !logExpr.getLeft().evaluate()) {
            return TRUE;
        }

        // 1>a = a=1
        if (logExpr.getType().equals(TypeLogicalBinary.IMPLICATION)
                && logExpr.getLeft().isConstant() && logExpr.getLeft().evaluate()) {
            return logExpr.getRight().equiv(TRUE);
        }
        
        // a>1 = 1
        if (logExpr.getType().equals(TypeLogicalBinary.IMPLICATION)
                && logExpr.getRight().isConstant() && logExpr.getRight().evaluate()) {
            return TRUE;
        }

        // a>0 = a=0
        if (logExpr.getType().equals(TypeLogicalBinary.IMPLICATION)
                && logExpr.getRight().isConstant() && !logExpr.getRight().evaluate()) {
            return logExpr.getLeft().equiv(FALSE);
        }
        
        return logExpr;

    }

}
