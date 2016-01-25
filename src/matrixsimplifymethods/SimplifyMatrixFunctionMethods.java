package matrixsimplifymethods;

import exceptions.EvaluationException;
import matrixexpressionbuilder.MatrixExpression;
import matrixexpressionbuilder.MatrixFunction;

public class SimplifyMatrixFunctionMethods {
    
    public static MatrixExpression approxConstantMatrixExpression(MatrixFunction matExpr) throws EvaluationException {
        if (matExpr.getLeft().isConstant() && matExpr.getLeft().containsApproximates()) {
            return matExpr.evaluate();
        }
        return matExpr;
    }
    
}
