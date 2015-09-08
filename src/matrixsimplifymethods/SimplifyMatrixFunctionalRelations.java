package matrixsimplifymethods;

import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import java.awt.Dimension;
import matrixexpressionbuilder.Matrix;
import matrixexpressionbuilder.MatrixBinaryOperation;
import matrixexpressionbuilder.MatrixExpression;
import matrixexpressionbuilder.MatrixFunction;
import matrixexpressionbuilder.MatrixPower;
import matrixexpressionbuilder.TypeMatrixBinary;
import matrixexpressionbuilder.TypeMatrixFunction;

public class SimplifyMatrixFunctionalRelations {

    /**
     * Vereinfacht Doppelpotenzen, falls möglich. Ansonsten wird expr
     * zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyDoublePowers(MatrixExpression matExpr) throws EvaluationException {

        if (matExpr.isPower() && ((MatrixPower) matExpr).getLeft().isPower()) {
            // Dann einfach nur Exponenten ausmultiplizieren, falls möglich.
            return ((MatrixPower) ((MatrixPower) matExpr).getLeft()).getLeft().pow(((MatrixPower) ((MatrixPower) matExpr).getLeft()).getRight().mult(((MatrixPower) matExpr).getRight()).simplifyTrivial());
        }
        return matExpr;

    }

    /**
     * Falls matExpr die Determinante einer Dreiecksmatrix darstellt, so wird
     * diese explizit berechnet (als das Produkt der Diagonalelemente).
     * Ansonsten wird matExpr zurückgegeben.
     */
    public static MatrixExpression simplifyDetOfTriangularMatrix(MatrixExpression matExpr) {

        if (matExpr instanceof MatrixFunction && ((MatrixFunction) matExpr).getType().equals(TypeMatrixFunction.det)
                && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && ((Matrix) ((MatrixFunction) matExpr).getLeft()).isSquareMatrix()
                && (((Matrix) ((MatrixFunction) matExpr).getLeft()).isLowerTriangularMatrix()
                || ((Matrix) ((MatrixFunction) matExpr).getLeft()).isUpperTriangularMatrix())) {
            // Dann explizit ausrechnen.
            Expression det = Expression.ONE;
            for (int i = 0; i < ((Matrix) ((MatrixFunction) matExpr).getLeft()).getRowNumber(); i++) {
                if (det.equals(Expression.ONE)) {
                    det = ((Matrix) ((MatrixFunction) matExpr).getLeft()).getEntry(i, i);
                } else {
                    det = det.mult(((Matrix) ((MatrixFunction) matExpr).getLeft()).getEntry(i, i));
                }
            }

            return new Matrix(det);
        }
        return matExpr;

    }

    /**
     * Falls matExpr die Determinante einer Blockmatrix darstellt, so wird diese
     * explizit berechnet (als das Produkt der Determinanten der einzelnen
     * Blöcke). Ansonsten wird matExpr zurückgegeben.
     */
    public static MatrixExpression simplifyDetOfBlockMatrix(MatrixExpression matExpr) {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.det)
                && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && ((Matrix) ((MatrixFunction) matExpr).getLeft()).getLengthOfUpperSquareBlock() instanceof Integer) {
            // Dann muss die Matrix in der Determinante automatisch quadratisch sein.
            Matrix m = (Matrix) ((MatrixFunction) matExpr).getLeft();
            Dimension dim = m.getDimension();
            int lengthUpperBlock = (int) m.getLengthOfUpperSquareBlock();
            if (lengthUpperBlock == dim.height) {
                return matExpr;
            }

            Expression[][] entriesUpperBlock = new Expression[lengthUpperBlock][lengthUpperBlock];
            Expression[][] entriesLowerBlock = new Expression[dim.height - lengthUpperBlock][dim.width - lengthUpperBlock];

            // Oberen Block als Matrix bilden.
            for (int i = 0; i < lengthUpperBlock; i++) {
                for (int j = 0; j < lengthUpperBlock; j++) {
                    entriesUpperBlock[i][j] = m.getEntry(i, j);
                }
            }
            // Unteren Block als Matrix bilden.
            for (int i = lengthUpperBlock; i < dim.height; i++) {
                for (int j = lengthUpperBlock; j < dim.width; j++) {
                    entriesLowerBlock[i - lengthUpperBlock][j - lengthUpperBlock] = m.getEntry(i, j);
                }
            }

            return new Matrix(entriesUpperBlock).det().mult(new Matrix(entriesLowerBlock).det());

        }

        return matExpr;

    }

    /**
     * Vereinfacht det(A^n) = (det(A))^n.
     */
    public static MatrixExpression simplifyDetOfMatrixPower(MatrixExpression matExpr) {
        if (matExpr.isMatrixFunction(TypeMatrixFunction.det) && ((MatrixFunction) matExpr).getLeft().isPower()) {
            // Dann einfach den Exponenten aus der Determinante herausziehen.
            return ((MatrixPower) ((MatrixFunction) matExpr).getLeft()).getLeft().det().pow(((MatrixPower) ((MatrixFunction) matExpr).getLeft()).getRight());
        }
        return matExpr;
    }

    /**
     * Vereinfacht det(A_1 * ... * A_n) = det(A_1) * ... * det(A_n).
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyDetOfMatrixProducts(MatrixExpression matExpr) throws EvaluationException {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.det) && ((MatrixFunction) matExpr).getLeft().isProduct()) {

            // Es wird geprüft, ob das Argument ein gültiger Matrizenausdruck ist.
            ((MatrixFunction) matExpr).getLeft().getDimension();

            MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(((MatrixFunction) matExpr).getLeft());
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, new MatrixFunction(factors.get(i), TypeMatrixFunction.det));
            }
            return SimplifyMatrixUtilities.produceProduct(factors);

        }
        return matExpr;

    }

    /**
     * Vereinfacht tr(A_1 +- ... +- A_n) = tr(A_1) +- ... +- tr(A_n).
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyTrOfMatrixSums(MatrixExpression matExpr) throws EvaluationException {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.tr) && ((MatrixFunction) matExpr).getLeft().isSum()) {

            // Es wird geprüft, ob das Argument ein gültiger Matrizenausdruck ist.
            ((MatrixFunction) matExpr).getLeft().getDimension();

            if (((MatrixFunction) matExpr).getLeft().isSum()) {
                MatrixExpressionCollection summands = SimplifyMatrixUtilities.getSummands(((MatrixFunction) matExpr).getLeft());
                for (int i = 0; i < summands.getBound(); i++) {
                    summands.put(i, new MatrixFunction(summands.get(i), TypeMatrixFunction.tr));
                }
                return SimplifyMatrixUtilities.produceSum(summands);
            }
            if (((MatrixFunction) matExpr).getLeft().isDifference()) {
                return new MatrixBinaryOperation(((MatrixBinaryOperation) ((MatrixFunction) matExpr).getLeft()).getLeft().tr(),
                        ((MatrixBinaryOperation) ((MatrixFunction) matExpr).getLeft()).getRight().tr(), TypeMatrixBinary.MINUS);
            }

        }

        return matExpr;

    }

    /**
     * Vereinfacht tr(A^k * B * A^(-k)) = tr(B).
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyTrOfConjugatedMatrix(MatrixExpression matExpr) throws EvaluationException {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.tr) && ((MatrixFunction) matExpr).getLeft().isProduct()) {

            MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(((MatrixFunction) matExpr).getLeft());
            if (factors.getBound() == 3 && factors.get(0).isPower() && factors.get(2).isPower()
                    && ((MatrixPower) factors.get(0)).getLeft().equivalent(((MatrixPower) factors.get(2)).getLeft())) {

                Expression sumOfExponents = ((MatrixPower) factors.get(0)).getRight().add(((MatrixPower) factors.get(2)).getRight()).simplify();
                if (sumOfExponents.equals(Expression.ZERO)) {
                    return new MatrixFunction(factors.get(1), TypeMatrixFunction.tr);
                }

            }

        }

        return matExpr;

    }

    /**
     * Falls matExpr die Exponentialfunktion einer Diagonalmatrix darstellt, so
     * wird diese explizit berechnet. Ansonsten wird matExpr zurückgegeben.
     */
    public static MatrixExpression simplifyExpOfDiagonalMatrix(MatrixExpression matExpr) {

        if (matExpr instanceof MatrixFunction && ((MatrixFunction) matExpr).getType().equals(TypeMatrixFunction.exp)
                && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && ((Matrix) ((MatrixFunction) matExpr).getLeft()).isDiagonalMatrix()) {

            // Dann explizit ausrechnen.
            Expression[][] resultExtry = new Expression[((Matrix) ((MatrixFunction) matExpr).getLeft()).getRowNumber()][((Matrix) ((MatrixFunction) matExpr).getLeft()).getColumnNumber()];
            for (int i = 0; i < ((Matrix) ((MatrixFunction) matExpr).getLeft()).getRowNumber(); i++) {
                for (int j = 0; j < ((Matrix) ((MatrixFunction) matExpr).getLeft()).getColumnNumber(); j++) {
                    if (i != j) {
                        resultExtry[i][j] = Expression.ZERO;
                    } else {
                        resultExtry[i][j] = new Function(((Matrix) ((MatrixFunction) matExpr).getLeft()).getEntry(i, j), TypeFunction.exp);
                    }
                }
            }

            return new Matrix(resultExtry);
        }
        return matExpr;

    }

    /**
     * Vereinfacht det(exp(A)) = exp(tr(A)).
     */
    public static MatrixExpression simplifyDetOfExpOfMatrix(MatrixExpression matExpr) {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.det)
                && ((MatrixFunction) matExpr).getLeft().isMatrixFunction(TypeMatrixFunction.exp)) {
            // Achtung: Künstliches Casten zu MatrixExpression notwendig, da sonst die falsche Methode exp() angewendet wird. 
            return ((MatrixExpression) ((MatrixFunction) ((MatrixFunction) matExpr).getLeft()).getLeft().tr()).exp();
        }
        return matExpr;

    }

    /**
     * Vereinfacht exp(A^k * B * A^(-k)) = A^k * exp(B) * A^(-k).
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyExpOfConjugatedMatrix(MatrixExpression matExpr) throws EvaluationException {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.exp) && ((MatrixFunction) matExpr).getLeft().isProduct()) {

            MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(((MatrixFunction) matExpr).getLeft());
            if (factors.getBound() == 3 && factors.get(0).isPower() && factors.get(2).isPower()
                    && ((MatrixPower) factors.get(0)).getLeft().equivalent(((MatrixPower) factors.get(2)).getLeft())) {

                Expression sumOfExponents = ((MatrixPower) factors.get(0)).getRight().add(((MatrixPower) factors.get(2)).getRight()).simplify();
                if (sumOfExponents.equals(Expression.ZERO)) {
                    return factors.get(0).mult(new MatrixFunction(factors.get(1), TypeMatrixFunction.exp).mult(factors.get(2)));
                }

            }

        }

        return matExpr;

    }

    /**
     * Falls matExpr der Logarithmus einer Diagonalmatrix darstellt, so wird
     * dieser explizit berechnet. Ansonsten wird matExpr zurückgegeben.
     */
    public static MatrixExpression simplifyLnOfDiagonalMatrix(MatrixExpression matExpr) {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.ln)
                && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && ((Matrix) ((MatrixFunction) matExpr).getLeft()).isDiagonalMatrix()) {

            // Dann explizit ausrechnen.
            Expression[][] resultExtry = new Expression[((Matrix) ((MatrixFunction) matExpr).getLeft()).getRowNumber()][((Matrix) ((MatrixFunction) matExpr).getLeft()).getColumnNumber()];
            for (int i = 0; i < ((Matrix) ((MatrixFunction) matExpr).getLeft()).getRowNumber(); i++) {
                for (int j = 0; j < ((Matrix) ((MatrixFunction) matExpr).getLeft()).getColumnNumber(); j++) {
                    if (i != j) {
                        resultExtry[i][j] = Expression.ZERO;
                    } else {
                        resultExtry[i][j] = new Function(((Matrix) ((MatrixFunction) matExpr).getLeft()).getEntry(i, j), TypeFunction.ln);
                    }
                }
            }

            return new Matrix(resultExtry);
        }
        return matExpr;

    }

    /**
     * Vereinfacht ln(A^k * B * A^(-k)) = A^k * ln(B) * A^(-k).
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyLnOfConjugatedMatrix(MatrixExpression matExpr) throws EvaluationException {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.ln) && ((MatrixFunction) matExpr).getLeft().isProduct()) {

            MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(((MatrixFunction) matExpr).getLeft());
            if (factors.getBound() == 3 && factors.get(0).isPower() && factors.get(2).isPower()
                    && ((MatrixPower) factors.get(0)).getLeft().equivalent(((MatrixPower) factors.get(2)).getLeft())) {

                Expression sumOfExponents = ((MatrixPower) factors.get(0)).getRight().add(((MatrixPower) factors.get(2)).getRight()).simplify();
                if (sumOfExponents.equals(Expression.ZERO)) {
                    return factors.get(0).mult(new MatrixFunction(factors.get(1), TypeMatrixFunction.ln).mult(factors.get(2)));
                }

            }

        }

        return matExpr;

    }

}
