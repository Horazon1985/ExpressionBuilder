package abstractexpressions.matrixexpression.utilities;

import exceptions.EvaluationException;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.MatrixFunction;
import abstractexpressions.matrixexpression.classes.TypeMatrixFunction;

public class SimplifyMatrixFunctionMethods {
    
    public static MatrixExpression approxConstantMatrixExpression(MatrixFunction matExpr) throws EvaluationException {
        if (matExpr.getLeft().isConstant() && matExpr.getLeft().containsApproximates()) {
            return matExpr.evaluate();
        }
        return matExpr;
    }
    
    /**
     * Vereinfacht bestimmte Kompositionen zweier Funktionen.
     */
    public static MatrixExpression simplifyCompositionOfTwoFunctions(TypeMatrixFunction type, MatrixExpression argumentF) {

        if (argumentF instanceof MatrixFunction) {

            MatrixFunction argumentFunction = (MatrixFunction) argumentF;

            if (type.equals(TypeMatrixFunction.ln) && argumentFunction.getType().equals(TypeMatrixFunction.exp)) {
                return argumentFunction.getLeft();
            }
            if (type.equals(TypeMatrixFunction.exp) && argumentFunction.getType().equals(TypeMatrixFunction.ln)) {
                return argumentFunction.getLeft();
            }
            if (type.equals(TypeMatrixFunction.det) && argumentFunction.getType().equals(TypeMatrixFunction.exp)) {
                return argumentFunction.getLeft().tr().exp();
            }

        }

        return new MatrixFunction(argumentF, type);

    }
    
    
}
