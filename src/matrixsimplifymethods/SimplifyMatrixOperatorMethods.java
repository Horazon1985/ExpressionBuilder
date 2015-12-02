package matrixsimplifymethods;

import expressionbuilder.Expression;
import matrixexpressionbuilder.MatrixBinaryOperation;
import matrixexpressionbuilder.MatrixExpression;
import matrixexpressionbuilder.MatrixOperator;
import matrixexpressionbuilder.TypeMatrixOperator;

public abstract class SimplifyMatrixOperatorMethods {

    /**
     * Falls im Summenoperator eine Summe oder eine Differenz von Matrizen
     * auftaucht, so wird dieser in eine entsprechende Summe oder Differenz von
     * Summenoperatoren aufgeteilt.
     */
    public static MatrixExpression splitSumOfSumsOrDifferences(MatrixBinaryOperation summand, String var, Expression lowerLimit, Expression upperLimit) {

        if (summand.isDifference()) {

            Object[] paramsLeft = new Object[4];
            paramsLeft[0] = summand.getLeft();
            paramsLeft[1] = var;
            paramsLeft[2] = lowerLimit;
            paramsLeft[3] = upperLimit;
            Object[] paramsRight = new Object[4];
            paramsRight[0] = summand.getRight();
            paramsRight[1] = var;
            paramsRight[2] = lowerLimit;
            paramsRight[3] = upperLimit;
            return new MatrixOperator(TypeMatrixOperator.sum, paramsLeft).sub(new MatrixOperator(TypeMatrixOperator.sum, paramsRight));

        } else {

            MatrixExpressionCollection summands = SimplifyMatrixUtilities.getSummands(summand);
            Object[][] params = new Object[summands.getBound()][4];
            for (int i = 0; i < summands.getBound(); i++) {
                for (int j = 0; j < 4; j++) {
                    params[i][0] = summands.get(i);
                    params[i][1] = var;
                    params[i][2] = lowerLimit;
                    params[i][3] = upperLimit;
                }
                summands.put(i, new MatrixOperator(TypeMatrixOperator.sum, params[i]));
            }

            return SimplifyMatrixUtilities.produceSum(summands);

        }

    }

}
