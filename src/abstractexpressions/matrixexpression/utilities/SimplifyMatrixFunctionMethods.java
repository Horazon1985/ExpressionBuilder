package abstractexpressions.matrixexpression.utilities;

import exceptions.EvaluationException;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.MatrixFunction;

public class SimplifyMatrixFunctionMethods {
    
    public static MatrixExpression approxConstantMatrixExpression(MatrixFunction matExpr) throws EvaluationException {
        if (matExpr.getLeft().isConstant() && matExpr.getLeft().containsApproximates()) {
            return matExpr.evaluate();
        }
        return matExpr;
    }
    
}
