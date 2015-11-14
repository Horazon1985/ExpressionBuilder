package expressionsimplifymethods;

import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import expressionbuilder.Variable;

public abstract class SimplifyExponentialRelations {

    /**
     * Gibt einen Ausdruck zurück, indem in Exponentialfunktionen bzgl. var der
     * von var abhängige Teil von dem von var unabhängigen Teil getrennt
     * wurde.<br>
     * BEISPIEL: Für f = 3 + 2^(x+7) - x^2*exp(8 - sin(x)) wird 3 + 2^7*2^x -
     * x^2*exp(8)*exp(-sin(x)) zurückgegeben.
     */
    public static Expression separateConstantPartsInRationalExponentialEquations(Expression f, String var) {
        if (!f.contains(var) || f instanceof Constant || f instanceof Variable) {
            return f;
        }
        if (f instanceof BinaryOperation) {
            if (f.isPower() && !((BinaryOperation) f).getLeft().contains(var) && ((BinaryOperation) f).getRight().contains(var)) {
                Expression base = ((BinaryOperation) f).getLeft();
                ExpressionCollection summandsLeftConstant = SimplifyUtilities.getConstantSummandsLeftInExpression(((BinaryOperation) f).getRight(), var);
                ExpressionCollection summandsLeftNonConstant = SimplifyUtilities.getNonConstantSummandsLeftInExpression(((BinaryOperation) f).getRight(), var);
                ExpressionCollection summandsRightConstant = SimplifyUtilities.getConstantSummandsRightInExpression(((BinaryOperation) f).getRight(), var);
                ExpressionCollection summandsRightNonConstant = SimplifyUtilities.getNonConstantSummandsRightInExpression(((BinaryOperation) f).getRight(), var);
                return base.pow(SimplifyUtilities.produceDifference(summandsLeftConstant, summandsRightConstant)).mult(base.pow(SimplifyUtilities.produceDifference(summandsLeftNonConstant, summandsRightNonConstant)));
            }
            return new BinaryOperation(separateConstantPartsInRationalExponentialEquations(((BinaryOperation) f).getLeft(), var), separateConstantPartsInRationalExponentialEquations(((BinaryOperation) f).getRight(), var), ((BinaryOperation) f).getType());
        }
        if (f instanceof Function) {
            if (!((Function) f).getType().equals(TypeFunction.exp)) {
                return new Function(separateConstantPartsInRationalExponentialEquations(((Function) f).getLeft(), var), ((Function) f).getType());
            }
            Expression argumentOfExp = ((Function) f).getLeft();
            ExpressionCollection constantSummandsLeft = SimplifyUtilities.getConstantSummandsLeftInExpression(argumentOfExp, var);
            ExpressionCollection nonConstantSummandsLeft = SimplifyUtilities.getNonConstantSummandsLeftInExpression(argumentOfExp, var);
            ExpressionCollection constantSummandsRight = SimplifyUtilities.getConstantSummandsRightInExpression(argumentOfExp, var);
            ExpressionCollection nonConstantsummandsRight = SimplifyUtilities.getNonConstantSummandsRightInExpression(argumentOfExp, var);
            Expression constantSummand = SimplifyUtilities.produceDifference(constantSummandsLeft, constantSummandsRight);
            if (constantSummand.equals(Expression.ZERO)) {
                // Dann gibt es keinen konstanten Summanden im Argument.
                return f;
            }
            return SimplifyUtilities.produceDifference(constantSummandsLeft, constantSummandsRight).exp().mult(SimplifyUtilities.produceDifference(nonConstantSummandsLeft, nonConstantsummandsRight).exp());
        }
        return f;
    }

}
