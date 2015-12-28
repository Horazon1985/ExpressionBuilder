package computation;

import exceptions.EvaluationException;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.ZERO;
import expressionsimplifymethods.ExpressionCollection;
import matrixexpressionbuilder.Matrix;

public abstract class StatisticMethods {

    public static ExpressionCollection getRegressionLineCoefficients(Matrix[] points) throws EvaluationException {

        int n = points.length;

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
        denominator = denominator.sub(sumLeft);

        coefficients.add(numerator.div(denominator).simplify());

        // Linearen Koeffizienten berechnen.
        numerator = ZERO;
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

}
