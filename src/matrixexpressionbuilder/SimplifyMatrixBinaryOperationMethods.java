package matrixexpressionbuilder;

import exceptions.EvaluationException;
import java.awt.Dimension;
import matrixsimplifymethods.MatrixExpressionCollection;
import matrixsimplifymethods.SimplifyMatrixUtilities;
import translator.Translator;

public abstract class SimplifyMatrixBinaryOperationMethods {

    public static void removeIdInProduct(MatrixExpressionCollection factors) throws EvaluationException {
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).isId() && !factors.isEmpty()) {
                factors.remove(i);
            }
            if (Thread.interrupted()) {
                throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixBinaryOperation_COMPUTATION_ABORTED"));
            }
        }
    }

    public static void reduceZeroProductToZero(MatrixExpressionCollection factors) throws EvaluationException {
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).isZeroMatrix()) {
                Dimension dim = SimplifyMatrixUtilities.produceProduct(factors).getDimension();
                factors.clear();
                factors.add(MatrixExpression.getZeroMatrix(dim.height, dim.width));
            }
            if (Thread.interrupted()) {
                throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixBinaryOperation_COMPUTATION_ABORTED"));
            }
        }
    }

}
