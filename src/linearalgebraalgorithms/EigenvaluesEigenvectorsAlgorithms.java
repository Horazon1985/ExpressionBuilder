package linearalgebraalgorithms;

import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import java.awt.Dimension;
import matrixexpressionbuilder.Matrix;
import matrixexpressionbuilder.MatrixExpression;
import matrixexpressionbuilder.MatrixFunction;
import matrixexpressionbuilder.MatrixPower;
import matrixexpressionbuilder.TypeMatrixFunction;
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
        return new MatrixFunction(new Matrix(Variable.create(var)).mult(MatrixExpression.getId(dim.height)).sub(matExpr), TypeMatrixFunction.det);
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
    public MatrixExpressionCollection getEigenvectorsForEigenvalue(MatrixExpression matExpr, Expression eigenvalue) {

        MatrixExpressionCollection eigenvectors = new MatrixExpressionCollection();

        return eigenvectors;

    }

    /**
     * Gibt die Matrix zurück, die durch Multiplikation mit dem kgV aller Nenner
     * aller Matrixeinträge entsteht.
     */
    public MatrixExpression getMultipleWithoutDenominator(Matrix matExpr) {

        Matrix result = new Matrix(Expression.ONE);

        return result;

    }

}
