package linearalgebraalgorithms;

import exceptions.EvaluationException;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.MINUS_ONE;
import static expressionbuilder.Expression.ONE;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import java.awt.Dimension;
import matrixexpressionbuilder.Matrix;
import matrixexpressionbuilder.MatrixExpression;
import matrixexpressionbuilder.MatrixPower;
import matrixsimplifymethods.MatrixExpressionCollection;
import matrixsimplifymethods.SimplifyMatrixUtilities;
import solveequationmethods.SolveMethods;

public abstract class EigenvaluesEigenvectorsAlgorithms {

    /**
     * Gibt das charakteristische Polynom der Matrix matExpr bzgl. der Variablen
     * var zurück als det(var*E - matExtr). Auch wenn matExtr keine quadratische
     * Matrix darstellt, so wird der Matrixausdruck det(var*E - matExtr)
     * zurückgegeben. Ein entsprechender Fehler wird bei späterer Evaluierung
     * geworfen.
     */
    public static MatrixExpression getCharacteristicPolynomial(MatrixExpression matExpr, String var) throws EvaluationException {
        Dimension dim = matExpr.getDimension();
        return new Matrix(Variable.create(var)).mult(MatrixExpression.getId(dim.height)).sub(matExpr).det();
    }

    /**
     * Gibt die Eigenwerte des Matrizenausdruck matExtr zurück, wenn möglich.
     * Andernfalls wird eine leere ExpressionCollection zurückgegeben.
     */
    public static ExpressionCollection getEigenvalues(MatrixExpression matExpr) {

        try {

            // Fall: matExpr ist eine Matrixpotenz.
            if (matExpr.isPower()) {
                MatrixExpression base = ((MatrixPower) matExpr).getLeft();
                Expression exponent = ((MatrixPower) matExpr).getRight();
                ExpressionCollection eigenvalues = getEigenvalues(base);
                eigenvalues.powExpression(exponent);
                eigenvalues = eigenvalues.simplify();
                eigenvalues.removeMultipleTerms();
                return eigenvalues;
            }

            String var = getCharacteristicPolynomialVariable(matExpr);
            Object abstractCharPolynomial = getCharacteristicPolynomial(matExpr, var).simplify().convertOneTimesOneMatrixToExpression();
            if (!(abstractCharPolynomial instanceof Expression)) {
                return new ExpressionCollection();
            }
            Expression charPolynomial = (Expression) abstractCharPolynomial;
            SolveMethods.setSolveTries(100);
            return SolveMethods.solveZeroEquation(charPolynomial, var);

        } catch (EvaluationException e) {
            return new ExpressionCollection();
        }

    }

    /**
     * In matExpr sind Variablen enthalten, unter anderem möglicherweise auch
     * "Parametervariablen" X_1, X_2, .... Diese Funktion liefert dasjenige X_i
     * mit dem kleinsten Index i, welches in f noch nicht vorkommt.
     */
    public static String getCharacteristicPolynomialVariable(MatrixExpression matExpr) {
        String var = "X_";
        int j = 1;
        while (matExpr.contains(var + String.valueOf(j))) {
            j++;
        }
        return var + j;
    }

    /**
     * Gibt die Eigenvektoren der Matrix matExpr zum Eigenwert eigenvalue
     * zurück, falls möglich.
     */
    public static MatrixExpressionCollection getEigenvectorsForEigenvalue(MatrixExpression matExpr, Expression eigenvalue) {

        try {
            Dimension dim = matExpr.getDimension();
            /* 
             Sonderfall: matExpr ist eine Potenz A^k. Dann werden stattdessen die 
             Eigenvektoren von A zum Eigenwert eigenvalue^(1/k) (+/-eigenvalue^(1/k)
             für ganzes gerades k) gesucht und dann ausgegeben.
             */
            if (matExpr.isPower()) {
                MatrixExpression base = ((MatrixPower) matExpr).getLeft();
                Expression exponent = ((MatrixPower) matExpr).getRight();
                if (exponent.isEvenIntegerConstant()) {
                    return SimplifyMatrixUtilities.union(getEigenvectorsForEigenvalue(base, eigenvalue.pow(ONE.div(exponent))),
                            getEigenvectorsForEigenvalue(base, MINUS_ONE.mult(eigenvalue.pow(ONE.div(exponent)))));
                }
                return getEigenvectorsForEigenvalue(base, eigenvalue.pow(ONE.div(exponent)));
            }

            // A - k*E bilden, A = Matrix, k = Eigenwert von A.
            MatrixExpression matrixMinusMultipleOfE = matExpr.sub(new Matrix(eigenvalue).mult(MatrixExpression.getId(dim.height))).simplify();
            if (!(matrixMinusMultipleOfE instanceof Matrix)) {
                return new MatrixExpressionCollection();
            }
            return GaussAlgorithm.computeKernelOfMatrix((Matrix) matrixMinusMultipleOfE);
        } catch (EvaluationException e) {
            return new MatrixExpressionCollection();
        }

    }

    /**
     * Wenn true zurückgegeben wird, dann ist die Matrix konstant und (reell)
     * diagonalisierbar. Ansonsten ist es nicht bekannt.
     */
    public static boolean isMatrixDiagonalizable(Matrix m) {

        if (!m.isConstant()) {
            return false;
        }

        Dimension dim = m.getDimension();

        if (dim.height != dim.width) {
            return false;
        }

        ExpressionCollection eigenvalues = getEigenvalues(m);

        if (eigenvalues.getBound() == dim.height) {
            return true;
        }

        /*
         Prüfung, ob die Summe der Dimensionen der Eigenräume gleich der Zeilenanzahl 
         = Spaltenanzahl ist. 
         */
        int sumOfDimension = 0;
        for (int i = 0; i < eigenvalues.getBound(); i++) {
            sumOfDimension += getEigenvectorsForEigenvalue(m, eigenvalues.get(i)).getBound();
        }

        return sumOfDimension == dim.height;

    }

    /**
     * Gibt, falls die Matrix m diagonalisierbar ist, eine Matrix mit Spalten,
     * bestehend aus allen Eigenvektoren von m, zurück. Andernfalls wird false
     * zurückgegeben.
     */
    public static Object getEigenvectorBasisMatrix(Matrix m) {

        if (isMatrixDiagonalizable(m)) {

            ExpressionCollection eigenvalues = getEigenvalues(m);
            MatrixExpressionCollection allEigenvectors = new MatrixExpressionCollection();
            MatrixExpressionCollection eigenvectorsForEigenvalue;
            for (int i = 0; i < eigenvalues.getBound(); i++) {
                eigenvectorsForEigenvalue = getEigenvectorsForEigenvalue(m, eigenvalues.get(i));
                allEigenvectors.add(eigenvectorsForEigenvalue);
            }

            if (allEigenvectors.getBound() == m.getDimension().width) {
                Expression[][] eigenvectorMatrix = new Expression[m.getDimension().height][m.getDimension().width];
                for (int i = 0; i < m.getDimension().width; i++) {
                    for (int j = 0; j < m.getDimension().height; j++) {
                        eigenvectorMatrix[j][i] = ((Matrix) allEigenvectors.get(i)).getEntry(j, 0);
                    }
                }
                return new Matrix(eigenvectorMatrix);
            }

        }

        return false;

    }

}
