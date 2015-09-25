package linearalgebraalgorithms;

import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import java.awt.Dimension;
import matrixexpressionbuilder.Matrix;
import matrixexpressionbuilder.MatrixExpression;
import matrixexpressionbuilder.MatrixPower;
import matrixsimplifymethods.MatrixExpressionCollection;
import solveequationmethods.SolveMethods;

public class EigenvaluesEigenvectorsAlgorithms {

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
     * Gibt die Matrix zurück, die durch Multiplikation mit dem kgV aller Nenner
     * aller Matrixeinträge entsteht.
     */
    public MatrixExpression getMultipleWithoutDenominator(Matrix matExpr) {

        Matrix result = new Matrix(Expression.ONE);

        return result;

    }

    /**
     * Wenn true zurückgegeben wird, dann ist die Matrix (reell)
     * diagonalisierbar. Ansonsten ist es nicht bekannt.
     */
    public static boolean isMatrixDiagonalizable(Matrix m) {

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

}
