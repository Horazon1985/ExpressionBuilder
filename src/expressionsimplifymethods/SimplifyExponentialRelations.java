package expressionsimplifymethods;

import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import expressionbuilder.Variable;

public class SimplifyExponentialRelations {

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
            if (f.isPower() && !((BinaryOperation) f).getLeft().contains(var)) {
                Expression base = ((BinaryOperation) f).getLeft();
                ExpressionCollection summandsLeftConstant = SimplifyUtilities.getConstantSummandsLeftInExpression(((BinaryOperation) f).getRight(), var);
                ExpressionCollection summandsLeftNonConstant = SimplifyUtilities.getNonConstantSummandsLeftInExpression(((BinaryOperation) f).getRight(), var);
                ExpressionCollection summandsRightConstant = SimplifyUtilities.getConstantSummandsRightInExpression(((BinaryOperation) f).getRight(), var);
                ExpressionCollection summandsRightNonConstant = SimplifyUtilities.getNonConstantSummandsRightInExpression(((BinaryOperation) f).getRight(), var);
                Expression exponentLeft = SimplifyUtilities.produceDifference(summandsLeftConstant, summandsRightConstant);
                if (exponentLeft.equals(Expression.ZERO)) {
                    return f;
                }
                return base.pow(SimplifyUtilities.produceDifference(summandsLeftConstant, summandsRightConstant)).mult(base.pow(SimplifyUtilities.produceDifference(summandsLeftNonConstant, summandsRightNonConstant)));
            }
            return new BinaryOperation(separateConstantPartsInRationalExponentialEquations(((BinaryOperation) f).getLeft(), var), separateConstantPartsInRationalExponentialEquations(((BinaryOperation) f).getRight(), var), ((BinaryOperation) f).getType());
        }
        if (f instanceof Function) {
            if (!((Function) f).getType().equals(TypeFunction.exp)) {
                return new Function(separateConstantPartsInRationalExponentialEquations(((Function) f).getLeft(), var), ((Function) f).getType());
            }
            Expression argumentOfExp = ((Function) f).getLeft();
            ExpressionCollection summandsLeftConstant = SimplifyUtilities.getConstantSummandsLeftInExpression(argumentOfExp, var);
            ExpressionCollection summandsLeftNonConstant = SimplifyUtilities.getNonConstantSummandsLeftInExpression(argumentOfExp, var);
            ExpressionCollection summandsRightConstant = SimplifyUtilities.getConstantSummandsRightInExpression(argumentOfExp, var);
            ExpressionCollection summandsRightNonConstant = SimplifyUtilities.getNonConstantSummandsRightInExpression(argumentOfExp, var);
            Expression exponentLeft = SimplifyUtilities.produceDifference(summandsLeftConstant, summandsRightConstant);
            if (exponentLeft.equals(Expression.ZERO)) {
                return f;
            }
            return SimplifyUtilities.produceDifference(summandsLeftConstant, summandsRightConstant).exp().mult(SimplifyUtilities.produceDifference(summandsLeftNonConstant, summandsRightNonConstant).exp());
        }
        return f;
    }

}
