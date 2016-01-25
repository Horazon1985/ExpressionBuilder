package abstractexpressions.expression.computation;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.matrixexpression.classes.Matrix;

public abstract class StatisticMethods {

    /**
     * Berechnet die Koeffizienten der Regressionsgeraden zu den Punkten points
     * in der Ebene.<br>
     * VORAUSSETZUNG: points sind alle 2x1-Matrizen und points.length >= 2.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection getRegressionLineCoefficients(Matrix[] points) throws EvaluationException {

        int n = points.length;

        /*
         Zugrundeliegende Formeln: Y = a(0) + a(1)*X mit
         a(0) = (sum(x_i^2)*sum(y_i)-sum(x_i)*sum(x_i*y_i))/(n*sum(x_i^2)-sum(x_i)^2),
         a(1) = (n*sum(x_i*y_i)-sum(x_i)*sum(y_i))/(n*sum(x_i^2)-sum(x_i)^2),
         n = points.length.
         */
        ExpressionCollection coefficients = new ExpressionCollection();

        Expression numerator, denominator = ZERO;
        Expression sumLeft = ZERO, sumRight = ZERO;

        // Konstanten Koeffizienten berechnen.
        for (Matrix point : points) {
            sumLeft = sumLeft.add(point.getEntry(0, 0).pow(2));
        }
        for (Matrix point : points) {
            sumRight = sumRight.add(point.getEntry(1, 0));
        }
        numerator = sumLeft.mult(sumRight);

        sumLeft = ZERO;
        sumRight = ZERO;

        for (Matrix point : points) {
            sumLeft = sumLeft.add(point.getEntry(0, 0));
        }
        for (Matrix point : points) {
            sumRight = sumRight.add(point.getEntry(0, 0).mult(point.getEntry(1, 0)));
        }
        numerator = numerator.sub(sumLeft.mult(sumRight));

        for (Matrix point : points) {
            denominator = denominator.add(point.getEntry(0, 0).pow(2));
        }
        denominator = new Constant(n).mult(denominator);

        sumLeft = ZERO;
        for (Matrix point : points) {
            sumLeft = sumLeft.add(point.getEntry(0, 0));
        }

        sumLeft = sumLeft.pow(2);
        denominator = denominator.sub(sumLeft).simplify();

        coefficients.add(numerator.div(denominator).simplify());

        // Linearen Koeffizienten berechnen.
        sumLeft = ZERO;
        sumRight = ZERO;

        for (Matrix point : points) {
            sumLeft = sumLeft.add(point.getEntry(0, 0).mult(point.getEntry(1, 0)));
        }
        numerator = new Constant(n).mult(sumLeft);

        sumLeft = ZERO;
        for (Matrix point : points) {
            sumLeft = sumLeft.add(point.getEntry(0, 0));
        }
        for (Matrix point : points) {
            sumRight = sumRight.add(point.getEntry(1, 0));
        }
        numerator = numerator.sub(sumLeft.mult(sumRight));

        // Der Nenner ist bei beiden Koeffizienten derselbe.
        coefficients.add(numerator.div(denominator).simplify());

        return coefficients;

    }

    /**
     * Berechnet das Minimum der i-ten Komponente von points, wenn 0 <= i <
     * points.length. Andernfalls wird 0 zurückgegeben.<br> VORAUSSETZUNG:
     * points sind konstant und allesamt (n x 1)-Matrizen.
     *
     * @throws EvaluationException
     */
    public static Expression getMinimum(Matrix[] points, int i) throws EvaluationException {

        if (i < 0 || i >= points.length) {
            return ZERO;
        }

        Expression min = null;
        for (Matrix point : points) {
            if (min == null) {
                min = point.getEntry(i, 0);
            } else if (point.getEntry(i, 0).evaluate() < min.evaluate()) {
                min = point.getEntry(i, 0);
            }
        }

        return min;

    }

    /**
     * Berechnet das Maximum der i-ten Komponente von points, wenn 0 <= i <
     * points.length. Andernfalls wird 0 zurückgegeben.<br> VORAUSSETZUNG:
     * points sind konstant und allesamt (n x 1)-Matrizen.
     *
     * @throws EvaluationException
     */
    public static Expression getMaximum(Matrix[] points, int i) throws EvaluationException {

        if (i < 0 || i >= points.length) {
            return ZERO;
        }

        Expression max = null;
        for (Matrix point : points) {
            if (max == null) {
                max = point.getEntry(i, 0);
            } else if (point.getEntry(i, 0).evaluate() > max.evaluate()) {
                max = point.getEntry(i, 0);
            }
        }

        return max;

    }

}
