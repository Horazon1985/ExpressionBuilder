package abstractexpressions.expression.differentialequation;

import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.basic.SimplifyPolynomialUtils;
import abstractexpressions.expression.basic.SimplifyUtilities;
import exceptions.EvaluationException;
import exceptions.LaplaceTransformationNotComputableException;
import java.math.BigInteger;

public abstract class LaplaceTransformationUtils {

    public static Expression getLaplaceTransformation(Expression f, String var, String transVar) throws LaplaceTransformationNotComputableException {

        // Zunächst Linearkombinationen behandfeln.
        try {
            return getLaplaceTransformationOfSumsAndDifferences(f, var, transVar);
        } catch (LaplaceTransformationNotComputableException e) {
        }
        try {
            return getLaplaceTransformationOfScalarMultiples(f, var, transVar);
        } catch (LaplaceTransformationNotComputableException e) {
        }

        // Nun spezielle Funktionstypen behandeln.
        try {
            return getLaplaceTransformationIfFunctionIsConstant(f, var, transVar);
        } catch (LaplaceTransformationNotComputableException e) {
        }
        try {
            return getLaplaceTransformationIfFunctionIsIntgerPower(f, var, transVar);
        } catch (LaplaceTransformationNotComputableException e) {
        }
        try {
            return getLaplaceTransformationIfFunctionIsExponentialInLinearFunction(f, var, transVar);
        } catch (LaplaceTransformationNotComputableException e) {
        }
        try {
            return getLaplaceTransformationIfFunctionIsSineInLinearFunction(f, var, transVar);
        } catch (LaplaceTransformationNotComputableException e) {
        }
        try {
            return getLaplaceTransformationIfFunctionIsCosineInLinearFunction(f, var, transVar);
        } catch (LaplaceTransformationNotComputableException e) {
        }

        throw new LaplaceTransformationNotComputableException();

    }

    /**
     * L(f<sub>1</sub> + ... + f<sub>n</sub>) = L(f<sub>1</sub>) + ... +
     * L(f<sub>n</sub>).
     *
     * @throws LaplaceTransformationNotComputableException
     */
    private static Expression getLaplaceTransformationOfSumsAndDifferences(Expression f, String var, String transVar) throws LaplaceTransformationNotComputableException {

        if (f.isNotSum() && f.isNotDifference()) {
            throw new LaplaceTransformationNotComputableException();
        }

        Expression fTransformedLeft = ZERO;
        Expression fTransformedRight = ZERO;

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(f);

        for (Expression summand : summandsLeft) {
            fTransformedLeft = fTransformedLeft.add(getLaplaceTransformation(summand, var, transVar));
        }

        for (Expression summand : summandsRight) {
            fTransformedRight = fTransformedRight.add(getLaplaceTransformation(summand, var, transVar));
        }

        return fTransformedLeft.sub(fTransformedRight);

    }

    /**
     * L(c * f) = c * L(f) für reelles c.
     *
     * @throws LaplaceTransformationNotComputableException
     */
    private static Expression getLaplaceTransformationOfScalarMultiples(Expression f, String var, String transVar) throws LaplaceTransformationNotComputableException {

        if (f.isNotProduct() && f.isNotQuotient()) {
            throw new LaplaceTransformationNotComputableException();
        }

        Expression constantFactorsNumerator = ONE;
        Expression constantFactorsDenominator = ONE;

        ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(f);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);

        for (int i = 0; i < factorsNumerator.getBound(); i++) {
            if (!factorsNumerator.get(i).contains(var)) {
                constantFactorsNumerator.mult(factorsNumerator.get(i));
                factorsNumerator.put(i, null);

            }
        }

        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (!factorsDenominator.get(i).contains(var)) {
                constantFactorsDenominator.mult(factorsDenominator.get(i));
                factorsDenominator.put(i, null);

            }
        }

        if (constantFactorsNumerator.equals(ONE) && constantFactorsDenominator.equals(ONE)) {
            throw new LaplaceTransformationNotComputableException();
        }

        return constantFactorsNumerator.div(constantFactorsDenominator).mult(
                getLaplaceTransformation(SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator), var, transVar));

    }

    /**
     * L(c) = c/p, p = transVar.
     *
     * @throws LaplaceTransformationNotComputableException
     */
    private static Expression getLaplaceTransformationIfFunctionIsConstant(Expression f, String var, String transVar) throws LaplaceTransformationNotComputableException {
        if (!f.contains(var)) {
            return f.div(Variable.create(transVar));
        }
        throw new LaplaceTransformationNotComputableException();
    }

    /**
     * L(t^n) = n!/p^(n+1), t = var, p = transVar.
     *
     * @throws LaplaceTransformationNotComputableException
     */
    private static Expression getLaplaceTransformationIfFunctionIsIntgerPower(Expression f, String var, String transVar) throws LaplaceTransformationNotComputableException {

        ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(f);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
        BigInteger n = null;
        for (int i = 0; i < factorsNumerator.getBound(); i++) {
            if (factorsNumerator.get(i).isIntegerPower() && ((BinaryOperation) factorsNumerator.get(i)).getLeft().equals(Variable.create(var))) {
                n = ((Constant) ((BinaryOperation) factorsNumerator.get(i)).getRight()).getBigIntValue();
                factorsNumerator.remove(i);
                break;
            }
        }

        if (n != null) {
            Expression rest = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator);
            if (!rest.contains(var)) {
                return new Operator(TypeOperator.fac, new Object[]{new Constant(n)}).div(Variable.create(transVar).pow(n.add(BigInteger.ONE)));
            }
        }

        throw new LaplaceTransformationNotComputableException();

    }

    /**
     * L(exp(a*t + b)) = exp(b)/(p - a), t = var, p = transVar.
     *
     * @throws LaplaceTransformationNotComputableException
     */
    private static Expression getLaplaceTransformationIfFunctionIsExponentialInLinearFunction(Expression f, String var, String transVar) throws LaplaceTransformationNotComputableException {

        if (!f.isFunction(TypeFunction.exp)) {
            throw new LaplaceTransformationNotComputableException();
        }

        Expression argument = ((Function) f).getLeft();
        if (SimplifyPolynomialUtils.isLinearPolynomial(argument, var)) {
            try {
                ExpressionCollection coefficients = SimplifyPolynomialUtils.getPolynomialCoefficients(argument, var);
                if (coefficients.getBound() == 2) {
                    if (coefficients.get(0).equals(ZERO)) {
                        return ONE.div(Variable.create(transVar).sub(coefficients.get(1)));
                    }
                    return coefficients.get(0).exp().div(Variable.create(transVar).sub(coefficients.get(1)));
                }
            } catch (EvaluationException e) {
            }
        }

        throw new LaplaceTransformationNotComputableException();

    }

    /**
     * L(sin(a*t)) = a/(p^2 + a^2), t = var, p = transVar.
     *
     * @throws LaplaceTransformationNotComputableException
     */
    private static Expression getLaplaceTransformationIfFunctionIsSineInLinearFunction(Expression f, String var, String transVar) throws LaplaceTransformationNotComputableException {

        if (!f.isFunction(TypeFunction.sin)) {
            throw new LaplaceTransformationNotComputableException();
        }

        Expression argument = ((Function) f).getLeft();
        if (SimplifyPolynomialUtils.isLinearPolynomial(argument, var)) {
            try {
                ExpressionCollection coefficients = SimplifyPolynomialUtils.getPolynomialCoefficients(argument, var);
                if (coefficients.getBound() == 2 && coefficients.get(0).equals(ZERO)) {
                    return coefficients.get(1).div(Variable.create(transVar).pow(2).add(coefficients.get(1).pow(2)));
                }
            } catch (EvaluationException e) {
            }
        }

        throw new LaplaceTransformationNotComputableException();

    }

    /**
     * L(cos(a*t)) = p/(p^2 + a^2), t = var, p = transVar.
     *
     * @throws LaplaceTransformationNotComputableException
     */
    private static Expression getLaplaceTransformationIfFunctionIsCosineInLinearFunction(Expression f, String var, String transVar) throws LaplaceTransformationNotComputableException {

        if (!f.isFunction(TypeFunction.cos)) {
            throw new LaplaceTransformationNotComputableException();
        }

        Expression argument = ((Function) f).getLeft();
        if (SimplifyPolynomialUtils.isLinearPolynomial(argument, var)) {
            try {
                ExpressionCollection coefficients = SimplifyPolynomialUtils.getPolynomialCoefficients(argument, var);
                if (coefficients.getBound() == 2 && coefficients.get(0).equals(ZERO)) {
                    return Variable.create(transVar).div(Variable.create(transVar).pow(2).add(coefficients.get(1).pow(2)));
                }
            } catch (EvaluationException e) {
            }
        }

        throw new LaplaceTransformationNotComputableException();

    }

}
