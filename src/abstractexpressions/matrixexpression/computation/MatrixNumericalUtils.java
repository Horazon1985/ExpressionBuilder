package abstractexpressions.matrixexpression.computation;

import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import java.awt.Dimension;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.MatrixFunction;
import abstractexpressions.matrixexpression.classes.TypeMatrixFunction;

public abstract class MatrixNumericalUtils {

    private static Matrix add(Matrix mLeft, Matrix mRight) throws EvaluationException {
        double[][] resultEntry = new double[mLeft.getRowNumber()][mLeft.getRowNumber()];
        for (int i = 0; i < mLeft.getRowNumber(); i++) {
            for (int j = 0; j < mLeft.getRowNumber(); j++) {
                resultEntry[i][j] = mLeft.getEntry(i, j).evaluate() + mRight.getEntry(i, j).evaluate();
            }
        }
        return new Matrix(resultEntry);
    }

    private static Matrix sub(Matrix mLeft, Matrix mRight) throws EvaluationException {
        double[][] resultEntry = new double[mLeft.getRowNumber()][mLeft.getRowNumber()];
        for (int i = 0; i < mLeft.getRowNumber(); i++) {
            for (int j = 0; j < mLeft.getRowNumber(); j++) {
                resultEntry[i][j] = mLeft.getEntry(i, j).evaluate() - mRight.getEntry(i, j).evaluate();
            }
        }
        return new Matrix(resultEntry);
    }

    private static Matrix div(Matrix m, double n) throws EvaluationException {
        double[][] resultEntry = new double[m.getRowNumber()][m.getRowNumber()];
        for (int i = 0; i < m.getRowNumber(); i++) {
            for (int j = 0; j < m.getRowNumber(); j++) {
                resultEntry[i][j] = m.getEntry(i, j).evaluate() / n;
            }
        }
        return new Matrix(resultEntry);
    }

    /**
     * Berechnet mPower * m.<br>
     * WICHTIG: m muss quadratisch sein! Dies wird NICHT mehr geprüft.
     */
    private static Matrix getNextPowerOfMatrix(Matrix mPower, Matrix m) throws EvaluationException {
        double[][] resultEntry = new double[m.getRowNumber()][m.getRowNumber()];
        for (int i = 0; i < m.getRowNumber(); i++) {
            for (int j = 0; j < m.getRowNumber(); j++) {
                resultEntry[i][j] = 0;
                for (int k = 0; k < m.getRowNumber(); k++) {
                    resultEntry[i][j] = resultEntry[i][j] + mPower.getEntry(i, k).evaluate() * m.getEntry(k, j).evaluate();
                }
            }
        }
        return new Matrix(resultEntry);
    }

    private static Matrix getApproxOfCosOfMatrix(Matrix m, int n) throws EvaluationException {

        Dimension dim = m.getDimension();

        Matrix cosOfM = (Matrix) MatrixExpression.getZeroMatrix(dim.height, dim.height).turnToApproximate();
        Matrix powerOfM = (Matrix) MatrixExpression.getId(dim.height).turnToApproximate();
        double factorial = 1;

        for (int i = 0; i <= n; i++) {
            if (i % 4 == 0) {
                cosOfM = add(cosOfM, div(powerOfM, factorial));
            } else if (i % 4 == 2) {
                cosOfM = sub(cosOfM, div(powerOfM, factorial));
            }
            if (i < n) {
                powerOfM = getNextPowerOfMatrix(powerOfM, m);
                factorial *= i + 1;
            }
        }

        return cosOfM;

    }

    private static Matrix getApproxOfCoshOfMatrix(Matrix m, int n) throws EvaluationException {

        Dimension dim = m.getDimension();

        Matrix coshOfM = (Matrix) MatrixExpression.getZeroMatrix(dim.height, dim.height).turnToApproximate();
        Matrix powerOfM = (Matrix) MatrixExpression.getId(dim.height).turnToApproximate();
        double factorial = 1;

        for (int i = 0; i <= n; i++) {
            if (i % 4 == 0 || i % 4 == 2) {
                coshOfM = add(coshOfM, div(powerOfM, factorial));
            }
            if (i < n) {
                powerOfM = getNextPowerOfMatrix(powerOfM, m);
                factorial *= i + 1;
            }
        }

        return coshOfM;

    }

    private static Matrix getApproxOfExpOfMatrix(Matrix m, int n) throws EvaluationException {

        Dimension dim = m.getDimension();

        Matrix expOfM = (Matrix) MatrixExpression.getZeroMatrix(dim.height, dim.height).turnToApproximate();
        Matrix powerOfM = (Matrix) MatrixExpression.getId(dim.height).turnToApproximate();
        double factorial = 1;

        for (int i = 0; i <= n; i++) {
            expOfM = add(expOfM, div(powerOfM, factorial));
            if (i < n) {
                powerOfM = getNextPowerOfMatrix(powerOfM, m);
                factorial *= i + 1;
            }
        }

        return expOfM;

    }

    private static Matrix getApproxOfLnOfMatrix(Matrix m, int n) throws EvaluationException {

        Dimension dim = m.getDimension();

        Matrix lnOfM = (Matrix) MatrixExpression.getZeroMatrix(dim.height, dim.height).turnToApproximate();
        Matrix powerOfM = (Matrix) m.turnToApproximate();

        for (int i = 1; i <= n; i++) {
            if (i % 2 == 1) {
                lnOfM = add(lnOfM, div(powerOfM, i));
            } else {
                lnOfM = sub(lnOfM, div(powerOfM, i));
            }
            if (i < n) {
                powerOfM = getNextPowerOfMatrix(powerOfM, m);
            }
        }

        return lnOfM;

    }

    private static Matrix getApproxOfSinOfMatrix(Matrix m, int n) throws EvaluationException {

        Dimension dim = m.getDimension();

        Matrix sinOfM = (Matrix) MatrixExpression.getZeroMatrix(dim.height, dim.height).turnToApproximate();
        Matrix powerOfM = (Matrix) MatrixExpression.getId(dim.height).turnToApproximate();
        double factorial = 1;

        for (int i = 0; i <= n; i++) {
            if (i % 4 == 1) {
                sinOfM = add(sinOfM, div(powerOfM, factorial));
            } else if (i % 4 == 3) {
                sinOfM = sub(sinOfM, div(powerOfM, factorial));
            }
            if (i < n) {
                powerOfM = getNextPowerOfMatrix(powerOfM, m);
                factorial *= i + 1;
            }
        }

        return sinOfM;

    }

    private static Matrix getApproxOfSinhOfMatrix(Matrix m, int n) throws EvaluationException {

        Dimension dim = m.getDimension();

        Matrix sinhOfM = (Matrix) MatrixExpression.getZeroMatrix(dim.height, dim.height).turnToApproximate();
        Matrix powerOfM = (Matrix) MatrixExpression.getId(dim.height).turnToApproximate();
        double factorial = 1;

        for (int i = 0; i <= n; i++) {
            if (i % 4 == 1 || i % 4 == 3) {
                sinhOfM = add(sinhOfM, div(powerOfM, factorial));
            }
            if (i < n) {
                powerOfM = getNextPowerOfMatrix(powerOfM, m);
                factorial *= i + 1;
            }
        }

        return sinhOfM;

    }

    private static double estimateOperatorNormOfMatrix(Matrix m) throws EvaluationException {
        double result = 0;
        for (int i = 0; i < m.getRowNumber(); i++) {
            for (int j = 0; j < m.getRowNumber(); j++) {
                result += Math.pow(m.getEntry(i, j).evaluate(), 2);
            }
        }
        return Math.sqrt(result);
    }

    public static MatrixExpression getApproxOfMatrixFunction(Matrix argument, TypeMatrixFunction type) throws EvaluationException {

        double boundOpNorm = estimateOperatorNormOfMatrix(argument);
        if (!argument.isSquareMatrix() || boundOpNorm > ComputationBounds.BOUND_OPERATOR_MAX_NUMBER_OF_MEMBERS_IN_SUM_OR_PRODUCT) {
            switch (type) {
                case cos:
                    return argument.cos();
                case cosh:
                    return argument.cosh();
                case exp:
                    return argument.exp();
                case sin:
                    return argument.sin();
                case sinh:
                    return argument.sinh();
            }
        }
        // Sonderfall: Logarithmus (aufgrund von Konvergenzradius = 1)
        Matrix argumentMinusId = (Matrix) argument.sub(MatrixExpression.getId(argument.getRowNumber())).simplifyComputeMatrixOperations();
        double boundOpNormForLn = estimateOperatorNormOfMatrix(argumentMinusId);
        if ((!argument.isSquareMatrix() || boundOpNormForLn > 0.8) && type.equals(TypeMatrixFunction.ln)) {
            return argument.ln();
        }
        // Eigentliche numerische Berechnung von ln(m).
        int boundSummands;
        switch (type) {
            case cos:
                boundSummands = Math.max(10, 4 * ComputationBounds.BOUND_NUMERIC_MAX_OPERATOR_NORM_TO_COMPUTE_MATRIX_FUNCTION);
                return getApproxOfCosOfMatrix(argument, boundSummands);
            case cosh:
                boundSummands = Math.max(10, 4 * ComputationBounds.BOUND_NUMERIC_MAX_OPERATOR_NORM_TO_COMPUTE_MATRIX_FUNCTION);
                return getApproxOfCoshOfMatrix(argument, boundSummands);
            case exp:
                boundSummands = Math.max(10, 4 * ComputationBounds.BOUND_NUMERIC_MAX_OPERATOR_NORM_TO_COMPUTE_MATRIX_FUNCTION);
                return getApproxOfExpOfMatrix(argument, boundSummands);
            case ln:
                boundSummands = Math.max(10, 4 * ComputationBounds.BOUND_NUMERIC_MAX_OPERATOR_NORM_TO_COMPUTE_MATRIX_FUNCTION);
                return getApproxOfLnOfMatrix(argumentMinusId, boundSummands);
            case sin:
                boundSummands = Math.max(10, 4 * ComputationBounds.BOUND_NUMERIC_MAX_OPERATOR_NORM_TO_COMPUTE_MATRIX_FUNCTION);
                return getApproxOfSinOfMatrix(argument, boundSummands);
            case sinh:
                boundSummands = Math.max(10, 4 * ComputationBounds.BOUND_NUMERIC_MAX_OPERATOR_NORM_TO_COMPUTE_MATRIX_FUNCTION);
                return getApproxOfSinhOfMatrix(argument, boundSummands);
        }

        return new MatrixFunction(argument, type);

    }

}
