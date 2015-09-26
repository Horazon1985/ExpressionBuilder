package matrixsimplifymethods;

import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import java.awt.Dimension;
import java.util.ArrayList;
import linearalgebraalgorithms.EigenvaluesEigenvectorsAlgorithms;
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
     * Falls matExpr eine Diagonalmatrix ist, so wird f(matExpr) explizit
     * berechnet. Ansonsten wird f(m) zurückgegeben.
     */
    public static MatrixExpression simplifyPowerSeriesFunctionOfDiagonalMatrix(MatrixExpression matExpr, TypeMatrixFunction type) {

        if (matExpr.isMatrixFunction(type)
                && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && ((Matrix) ((MatrixFunction) matExpr).getLeft()).isDiagonalMatrix()) {

            // Dann explizit ausrechnen.
            Expression[][] resultExtry = new Expression[((Matrix) ((MatrixFunction) matExpr).getLeft()).getRowNumber()][((Matrix) ((MatrixFunction) matExpr).getLeft()).getColumnNumber()];
            for (int i = 0; i < ((Matrix) ((MatrixFunction) matExpr).getLeft()).getRowNumber(); i++) {
                for (int j = 0; j < ((Matrix) ((MatrixFunction) matExpr).getLeft()).getColumnNumber(); j++) {
                    if (i != j) {
                        resultExtry[i][j] = Expression.ZERO;
                    } else {
                        resultExtry[i][j] = new Function(((Matrix) ((MatrixFunction) matExpr).getLeft()).getEntry(i, j), convertMatrixFunctionTypeToFunctionType(type));
                    }
                }
            }

            return new Matrix(resultExtry);
        }
        return matExpr;

    }

    private static TypeFunction convertMatrixFunctionTypeToFunctionType(TypeMatrixFunction type) {
        switch (type) {
            case cos:
                return TypeFunction.cos;
            case cosh:
                return TypeFunction.cosh;
            case exp:
                return TypeFunction.exp;
            case ln:
                return TypeFunction.ln;
            case sin:
                return TypeFunction.sin;
            default:
                return TypeFunction.sinh;
        }
    }

    /**
     * Falls matExpr eine diagonalisierbare Matrix ist, so wird f(matExpr)
     * explizit berechnet. Ansonsten wird f(m) zurückgegeben.
     */
    public static MatrixExpression simplifyPowerSeriesFunctionOfDiagonalizableMatrix(MatrixExpression matExpr, TypeMatrixFunction type) {

        if (matExpr.isMatrixFunction(type)
                && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) ((MatrixFunction) matExpr).getLeft())) {

            Object eigenvectorMatrix = EigenvaluesEigenvectorsAlgorithms.getEigenvectorBasisMatrix((Matrix) ((MatrixFunction) matExpr).getLeft());
            if (eigenvectorMatrix instanceof Matrix) {
                try {
                    Matrix m = (Matrix) ((MatrixFunction) matExpr).getLeft();
                    MatrixExpression matrixInDiagonalForm = ((Matrix) eigenvectorMatrix).pow(-1).mult(m).mult((Matrix) eigenvectorMatrix).simplify();
                    if (matrixInDiagonalForm instanceof Matrix && ((Matrix) matrixInDiagonalForm).isDiagonalMatrix()) {
                        // Das Folgende kann dann direkt explizit berechnet werden.
                        return ((Matrix) eigenvectorMatrix).mult(new MatrixFunction(((Matrix) matrixInDiagonalForm), type)).mult(((Matrix) eigenvectorMatrix).pow(-1));
                    }
                } catch (EvaluationException e) {
                    return matExpr;
                }
            }

        }

        return matExpr;

    }

    /**
     * Falls matExpr eine nilpotente Matrix ist, so wird f(matExpr) explizit
     * berechnet. Ansonsten wird f(m) zurückgegeben.
     */
    public static MatrixExpression simplifyPowerSeriesFunctionOfNilpotentMatrix(MatrixExpression matExpr, TypeMatrixFunction type) {

        if (matExpr.isMatrixFunction(type)
                && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && ((Matrix) ((MatrixFunction) matExpr).getLeft()).isNilpotentMatrix()) {

            try {

                Dimension dim = ((Matrix) ((MatrixFunction) matExpr).getLeft()).getDimension();
                Matrix m = (Matrix) ((MatrixFunction) matExpr).getLeft();
                MatrixExpression powerOfM = m;
                int maxExponent = 1;
                while (!powerOfM.equals(MatrixExpression.getZeroMatrix(dim.height, dim.width)) && maxExponent < dim.height) {
                    powerOfM = powerOfM.mult(m).simplify();
                    maxExponent++;
                }

                ArrayList<Expression> taylorCoefficients = getTaylorCoefficientsOfFunction(type, maxExponent);
                MatrixExpression result = MatrixExpression.getZeroMatrix(dim.height, dim.width);
                // Ergebnispolynom bilden.
                for (int i = 0; i < taylorCoefficients.size(); i++) {
                    if (taylorCoefficients.get(i).equals(Expression.ZERO)) {
                        continue;
                    }
                    if (result.equals(MatrixExpression.getZeroMatrix(dim.height, dim.width))) {
                        result = new Matrix(taylorCoefficients.get(i)).mult(m.pow(i));
                    } else {
                        result = result.add(new Matrix(taylorCoefficients.get(i)).mult(m.pow(i)));
                    }
                }
                return result;

            } catch (EvaluationException e) {
            }

        }

        return matExpr;

    }

    private static ArrayList<Expression> getTaylorCoefficientsOfFunction(TypeMatrixFunction type, int n) throws EvaluationException {

        ArrayList<Expression> taylorCoefficients = new ArrayList<>();
        Expression coefficient;

        if (type.equals(TypeMatrixFunction.cos)){
        
        
        
        } else if (type.equals(TypeMatrixFunction.cosh)){
        
        
        
        } else if (type.equals(TypeMatrixFunction.exp)){
            coefficient = Expression.ONE;
            for (int i = 0; i <= n; i++){
            
            
            }
        } else if (type.equals(TypeMatrixFunction.ln)){
        
        
        
        } else if (type.equals(TypeMatrixFunction.sin)){
        
        
        
        } else {
            // Hier ist type = TypeMatrixFuncktion.sinh.
        
        
        
        }
        
        return taylorCoefficients;

    }

    /**
     * Vereinfacht f(A^k * B * A^(-k)) = A^k * f(B) * A^(-k).
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyPowerSeriesFunctionOfConjugatedMatrix(MatrixExpression matExpr, TypeMatrixFunction type) throws EvaluationException {

        if (matExpr.isMatrixFunction(type) && ((MatrixFunction) matExpr).getLeft().isProduct()) {

            MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(((MatrixFunction) matExpr).getLeft());
            if (factors.getBound() == 3 && factors.get(0).isPower() && factors.get(2).isPower()
                    && ((MatrixPower) factors.get(0)).getLeft().equivalent(((MatrixPower) factors.get(2)).getLeft())) {

                Expression sumOfExponents = ((MatrixPower) factors.get(0)).getRight().add(((MatrixPower) factors.get(2)).getRight()).simplify();
                if (sumOfExponents.equals(Expression.ZERO)) {
                    return factors.get(0).mult(new MatrixFunction(factors.get(1), type).mult(factors.get(2)));
                }

            }

        }

        return matExpr;

    }

}
