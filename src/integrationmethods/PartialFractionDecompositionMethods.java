package integrationmethods;

import exceptions.EvaluationException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.ZERO;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyPolynomialMethods;
import solveequationmethods.PolynomialRootsMethods;

public abstract class PartialFractionDecompositionMethods {

    public static boolean isRationalFunction(Expression f, String var) {
        return f.isQuotient()
                && SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)
                && SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getRight(), var);
    }

    private static Expression[] performPolynomialDivisionForFractionalDecomposition(Expression f, String var) throws EvaluationException {
        if (!isRationalFunction(f, var)) {
            // Nichts tun.
            return new Expression[]{ZERO, f};
        }

        ExpressionCollection coefficientsEnumerator = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
        ExpressionCollection coefficientsDenominator = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getRight(), var);

        if (coefficientsEnumerator.getBound() >= coefficientsDenominator.getBound()) {

            ExpressionCollection[] resultOfPolynomialDivision = PolynomialRootsMethods.polynomialDivision(coefficientsEnumerator, coefficientsDenominator);
            Expression polynomialPart = Expression.ZERO;
            for (int i = 0; i < resultOfPolynomialDivision[0].getBound(); i++) {
                if (polynomialPart.equals(Expression.ZERO)) {
                    polynomialPart = resultOfPolynomialDivision[0].get(i).mult(Variable.create(var).pow(i + 1)).div(i + 1);
                } else {
                    polynomialPart = polynomialPart.add(resultOfPolynomialDivision[0].get(i).mult(Variable.create(var).pow(i + 1)).div(i + 1));
                }
            }

            return new Expression[]{ polynomialPart, PolynomialRootsMethods.getPolynomialFromCoefficients(resultOfPolynomialDivision[1], var).div(
                    ((BinaryOperation) f).getRight()) };

        }

        return new Expression[]{ZERO, f};

    }

    private static Expression factorizeDenominatorOfRationalFunction(Expression f, String var) throws EvaluationException {
        if (!isRationalFunction(f, var)) {
            // Nichts tun.
            return f;
        }
        Expression denominatorDecomposed = PolynomialRootsMethods.decomposePolynomialInIrreducibleFactors(((BinaryOperation) f).getRight(), var);
        return ((BinaryOperation) f).getLeft().div(denominatorDecomposed);
    }








}
