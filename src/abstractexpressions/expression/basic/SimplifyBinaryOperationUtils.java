package abstractexpressions.expression.basic;

import abstractexpressions.expression.computation.ArithmeticUtils;
import computationbounds.ComputationBounds;
import enums.TypeExpansion;
import exceptions.EvaluationException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeBinary;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.substitution.SubstitutionUtilities;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lang.translator.Translator;

/**
 * Viele kleine Einzelmethoden zur Vereinfachung von Ausdrücken der Klasse
 * BinaryOperation (für simplifyBasic).
 */
public abstract class SimplifyBinaryOperationUtils {
    
    private static final String SU_SimplifyBinaryOperationMethods_DIVISION_BY_ZERO = "SU_SimplifyBinaryOperationMethods_DIVISION_BY_ZERO";
    private static final String SU_SimplifyBinaryOperationMethods_NEGATIVE_POWERS_OF_ZERO_NOT_DEFINED = "SU_SimplifyBinaryOperationMethods_NEGATIVE_POWERS_OF_ZERO_NOT_DEFINED";
    private static final String SU_SimplifyBinaryOperationMethods_ROOTS_OF_EVEN_ORDER_OF_NEGATIVE_NUMBERS_NOT_DEFINED = "SU_SimplifyBinaryOperationMethods_ROOTS_OF_EVEN_ORDER_OF_NEGATIVE_NUMBERS_NOT_DEFINED";
    
    /**
     * Approximiert eine konstante Summe.<br>
     * VORAUSSETZUNG: Der Gesamtausdruck muss konstant sein.
     *
     * @throws EvaluationException
     */
    public static void computeSumIfApprox(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) throws EvaluationException {

        double result = 0;
        boolean allSummandsSimplifiedAreConstant = true;

        for (int i = 0; i < summandsLeft.getBound(); i++) {
            if (summandsLeft.get(i) != null) {
                summandsLeft.put(i, summandsLeft.get(i).simplifyBasic());
            }
            if (!(summandsLeft.get(i) instanceof Constant)) {
                allSummandsSimplifiedAreConstant = false;
            }
        }

        for (int i = 0; i < summandsRight.getBound(); i++) {
            if (summandsRight.get(i) != null) {
                summandsRight.put(i, summandsRight.get(i).simplifyBasic());
            }
            if (!(summandsRight.get(i) instanceof Constant)) {
                allSummandsSimplifiedAreConstant = false;
            }
        }

        if (!allSummandsSimplifiedAreConstant) {
            return;
        }

        for (int i = 0; i < summandsLeft.getBound(); i++) {
            if (summandsLeft.get(i) != null) {
                result = result + summandsLeft.get(i).evaluate();
            }
        }
        for (int i = 0; i < summandsRight.getBound(); i++) {
            if (summandsRight.get(i) != null) {
                result = result - summandsRight.get(i).evaluate();
            }
        }

        summandsLeft.clear();
        summandsRight.clear();
        summandsLeft.put(0, new Constant(result));

    }

    /**
     * Approximiert eine konstante Differenz.<br>
     * VORAUSSETZUNG: Der Gesamtausdruck muss konstant sein.
     *
     * @throws EvaluationException
     */
    public static Expression computeDifferenceIfApprox(Expression expr) throws EvaluationException {
        if (expr.isDifference() && expr.isConstant() && expr.containsApproximates()) {
            Expression left = ((BinaryOperation) expr).getLeft().simplifyBasic();
            Expression right = ((BinaryOperation) expr).getRight().simplifyBasic();
            if (left instanceof Constant && right instanceof Constant) {
                return new Constant(((Constant) left).getApproxValue() - ((Constant) right).getApproxValue());
            }
        }
        return expr;
    }

    /**
     * Approximiert ein konstantes Produkt.<br>
     * VORAUSSETZUNG: Der Gesamtausdruck muss konstant sein.
     *
     * @throws EvaluationException
     */
    public static void computeProductIfApprox(ExpressionCollection factors) throws EvaluationException {

        double result = 1;
        boolean allFactorsSimplifiedAreConstant = true;

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null) {
                factors.put(i, factors.get(i).simplifyBasic());
            }
            if (!(factors.get(i) instanceof Constant)) {
                allFactorsSimplifiedAreConstant = false;
            }
        }

        if (!allFactorsSimplifiedAreConstant) {
            return;
        }

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null) {
                result = result * factors.get(i).evaluate();
            }
        }

        factors.clear();
        factors.put(0, new Constant(result));

    }

    /**
     * Approximiert einen konstanten Quotienten.<br>
     * VORAUSSETZUNG: Der Gesamtausdruck muss konstant sein.
     *
     * @throws EvaluationException
     */
    public static Expression computeQuotientIfApprox(Expression expr) throws EvaluationException {
        if (expr.isQuotient() && expr.isConstant() && expr.containsApproximates()) {
            Expression left = ((BinaryOperation) expr).getLeft().simplifyBasic();
            Expression right = ((BinaryOperation) expr).getRight().simplifyBasic();
            if (left instanceof Constant && right instanceof Constant) {
                return new Constant(((Constant) left).getApproxValue() / ((Constant) right).getApproxValue());
            }
        }
        return expr;
    }

    /**
     * Approximiert eine konstante Potenz.<br>
     * VORAUSSETZUNG: Der Gesamtausdruck muss konstant sein.
     *
     * @throws EvaluationException
     */
    public static Expression computePowerIfApprox(Expression expr) throws EvaluationException {
        if (expr.isPower() && expr.isConstant() && expr.containsApproximates()) {
            Expression left = ((BinaryOperation) expr).getLeft().simplifyBasic();
            Expression right = ((BinaryOperation) expr).getRight().simplifyBasic();
            if (left instanceof Constant && right instanceof Constant) {
                return new Constant(Math.pow(((Constant) left).getApproxValue(), ((Constant) right).getApproxValue()));
            }
        }
        return expr;
    }

    /**
     * Beseitigt Nullen in summands.
     */
    public static void removeZerosInSums(ExpressionCollection summands) {
        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) != null && summands.get(i).equals(ZERO)) {
                summands.remove(i);
            }
        }
    }

    /**
     * Falls in factors eine Null vorkommen, werden alle Elemente von factors
     * entfernt und die Expression ZERO hinzugefügt..
     */
    public static void reduceProductWithZeroToZero(ExpressionCollection factors) {
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null && factors.get(i).equals(ZERO)) {
                factors.clear();
                factors.add(ZERO);
                break;
            }
        }
    }

    /**
     * Beseitigt Einsen in factors.
     */
    public static void removeOnesInProducts(ExpressionCollection factors) {
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null && factors.get(i).equals(ONE)) {
                factors.remove(i);
            }
        }
    }

    /**
     * Falls in summandsLeft und summandsRight Brüche auftauchen, so werden
     * diese verrechnet. Ohne Beschränkung der Allgemeinheit tauchen diese im
     * jeweils ersten Summanden auf.
     */
    public static void subtractFractions(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) {

        if (summandsLeft.get(0) != null && summandsRight.get(0) != null
                && summandsLeft.get(0).isIntegerConstantOrRationalConstant()
                && summandsRight.get(0).isIntegerConstantOrRationalConstant()) {

            BigInteger a, b, c, d;
            if (summandsLeft.get(0).isIntegerConstant()) {
                a = ((Constant) summandsLeft.get(0)).getBigIntValue();
                b = BigInteger.ONE;
            } else {
                a = ((Constant) ((BinaryOperation) summandsLeft.get(0)).getLeft()).getBigIntValue();
                b = ((Constant) ((BinaryOperation) summandsLeft.get(0)).getRight()).getBigIntValue();
            }
            if (summandsRight.get(0).isIntegerConstant()) {
                c = ((Constant) summandsRight.get(0)).getBigIntValue();
                d = BigInteger.ONE;
            } else {
                c = ((Constant) ((BinaryOperation) summandsRight.get(0)).getLeft()).getBigIntValue();
                d = ((Constant) ((BinaryOperation) summandsRight.get(0)).getRight()).getBigIntValue();
            }

            BigInteger numerator = a.multiply(d).subtract(c.multiply(b));
            BigInteger denominator = b.multiply(d);
            BigInteger gcd = numerator.gcd(denominator);
            numerator = numerator.divide(gcd);
            denominator = denominator.divide(gcd);

            summandsLeft.put(0, new Constant(numerator).div(denominator));
            summandsRight.remove(0);

        }

    }

    /**
     * Falls expr einen Kehrwert darstellt und falls approximiert wird, so wird
     * der Kehrwert zurückgegeben (oder eine EvaluationException geworfen, falls
     * der Nenner Null ist). Ansonsten wird expr selbst wieder zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Expression computeReciprocalInApprox(BinaryOperation expr) throws EvaluationException {

        if (expr.getRight().isConstant() && (expr.getType().equals(TypeBinary.DIV)) && expr.containsApproximates()) {
            if (expr.getRight().evaluate() == 0) {
                throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyBinaryOperationMethods_DIVISION_BY_ZERO));
            }
            return new BinaryOperation(new Constant(1 / expr.getRight().evaluate()), expr.getLeft(), TypeBinary.TIMES);
        }
        return expr;

    }

    /**
     * Falls expr eine Potenz mit rationalem Exponenten, welcher einen ungeraden
     * Nenner besitzt, darstellt und approximiert werden soll, so wird expr
     * ausgewertet und der vereinfachte Ausdruck wird zurückgegeben. Ansonsten
     * wird expr zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Expression computeOddRootOfNegativeConstantsInApprox(BinaryOperation expr) throws EvaluationException {

        /*
         Hier wird das folgende Problem aus dem Weg geschafft: Wird (-2)^(1/3)
         approximiert, so wird der Exponent zu 0.33333333333 approximiert und
         dementsprechend kann das Ergebnis nicht berechnet werden. Daher: wenn
         es um ungerade Wurzeln geht: negatives Vorzeichen rausschaffen!
         */
        if (expr.isConstant() && expr.containsApproximates()) {
            double leftValue = expr.getLeft().simplifyBasic().evaluate();
            if (leftValue < 0 && expr.getType().equals(TypeBinary.POW) && expr.getRight().isConstant()) {
                Expression exponent = expr.getRight().turnToPrecise();
                if (exponent.isRationalConstant() && expr.getRight().isRationalConstant()
                        && SimplifyAlgebraicExpressionUtils.isAdmissibleExponent(exponent)) {

                    if (((BinaryOperation) expr.getRight()).getRight().isOddIntegerConstant()) {
                        return new Constant(-Math.pow(-leftValue, expr.getRight().evaluate()));
                    }
                    return new Constant(Math.pow(-leftValue, expr.getRight().evaluate()));

                }
            }
        }

        return expr;

    }

    /**
     * Falls expr eine rationale Zahl darstellt, wird der gekürzte Bruch,
     * welcher expr darstellt, zurückgegeben. Ansonsten wird expr zurückgegeben.
     */
    public static Expression rationalConstantToQuotient(BinaryOperation expr) {
        if (expr.isRationalConstant() && !expr.containsApproximates()) {
            return constantToQuotient(((Constant) expr.getLeft()).getValue(), ((Constant) expr.getRight()).getValue());
        }
        return expr;
    }

    /**
     * Macht auch numerator/denominator einen (gekürzten) Bruch (als Expression)
     */
    public static Expression constantToQuotient(BigDecimal numerator, BigDecimal denominator) {
        BigInteger[] reducedFraction = reduceFraction(numerator, denominator);
        if (reducedFraction[1].equals(BigInteger.ONE)) {
            return new Constant(reducedFraction[0]);
        }
        return new Constant(reducedFraction[0]).div(reducedFraction[1]);
    }

    /**
     * Ermittelt den Zähler und Nenner vom Bruch gekürzten Bruch
     * numerator/denominator.
     */
    private static BigInteger[] reduceFraction(BigDecimal numerator, BigDecimal denominator) {

        if (denominator.compareTo(BigDecimal.ZERO) < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }

        BigDecimal reducedNumerator = numerator;
        BigDecimal reducedDenominator = denominator;

        while (!(reducedNumerator.compareTo(reducedNumerator.setScale(0, BigDecimal.ROUND_HALF_UP)) == 0)
                || !(reducedDenominator.compareTo(reducedDenominator.setScale(0, BigDecimal.ROUND_HALF_UP)) == 0)) {
            reducedNumerator = reducedNumerator.multiply(BigDecimal.TEN);
            reducedDenominator = reducedDenominator.multiply(BigDecimal.TEN);
        }

        BigInteger[] result = new BigInteger[2];
        result[0] = reducedNumerator.toBigInteger().divide(reducedNumerator.toBigInteger().gcd(reducedDenominator.toBigInteger()));
        result[1] = reducedDenominator.toBigInteger().divide(reducedNumerator.toBigInteger().gcd(reducedDenominator.toBigInteger()));
        return result;

    }

    /**
     * Falls expr einen Bruch mit negativen Nenner darstellt, so wird das
     * Minuszeichen in den Zähler verschoben. Ansonsten wird expr zurückgegeben.
     */
    public static Expression eliminateNegativeDenominator(BinaryOperation expr) {

        if (expr.isQuotient() && expr.getRight() instanceof Constant
                && ((Constant) expr.getRight()).getValue().compareTo(BigDecimal.ZERO) < 0) {
            return MINUS_ONE.mult(expr.getLeft()).div(((Constant) expr.getRight()).getValue().negate());
        }
        return expr;

    }

    /**
     * Falls expr eine ganzzahlige Potenz einer ganzen Zahl darstellt, so wird
     * diese Potenz ausgerechnet und zurückgegeben, falls der Exponent eine
     * bestimmte Schranke nicht übersteigt. Ansonsten wird expr zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Expression computePowersOfIntegers(BinaryOperation expr) throws EvaluationException {

        if (expr.getLeft().isIntegerConstant() && expr.getRight().isIntegerConstant() && !expr.containsApproximates()) {

            Constant constantLeft = (Constant) expr.getLeft();
            Constant constantRight = (Constant) expr.getRight();

            if (expr.isPower()) {

                // Negative Potenzen von 0 sind nicht definiert.
                if (constantLeft.equals(ZERO) && constantRight.getValue().compareTo(BigDecimal.ZERO) < 0) {
                    throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyBinaryOperationMethods_NEGATIVE_POWERS_OF_ZERO_NOT_DEFINED));
                }
                /*
                 Potenzen von ganzen Zahlen sollen nur vereinfacht werden,
                 wenn die Basis >= 0 und der Exponent <= einer bestimmten
                 Schranke ist. Die Schranke für den Exponenten bewegt sich im
                 int-Bereich.
                 */
                if (constantRight.getValue().compareTo(BigDecimal.ZERO) >= 0
                        && constantRight.getValue().compareTo(BigDecimal.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_POWER_OF_RATIONALS)) <= 0) {
                    return new Constant(constantLeft.getValue().pow(constantRight.getValue().intValue()));
                }

            }

        }

        return expr;
    }

    /**
     * Falls expr eine ganzzahlige Potenz eines Bruches darstellt, so wird diese
     * Potenz ausgerechnet und zurückgegeben, falls der Exponent eine bestimmte
     * Schranke nicht übersteigt. Ansonsten wird expr zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Expression computePowersOfFractions(BinaryOperation expr) throws EvaluationException {

        if (expr.getLeft().isRationalConstant() && expr.getRight().isIntegerConstant() && !expr.containsApproximates()) {

            BigInteger numerator = ((Constant) ((BinaryOperation) expr.getLeft()).getLeft()).getBigIntValue();
            BigInteger denominator = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getBigIntValue();
            BigInteger exponent = ((Constant) expr.getRight()).getBigIntValue();

            if (expr.isPower()) {

                // Negative Potenzen von 0 sind nicht definiert.
                if (numerator.equals(BigInteger.ZERO) && exponent.compareTo(BigInteger.ZERO) < 0) {
                    throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyBinaryOperationMethods_NEGATIVE_POWERS_OF_ZERO_NOT_DEFINED));
                }
                /*
                 Potenzen von ganzen Zahlen sollen nur vereinfacht werden,
                 wenn die Basis >= 0 und der Exponent <= einer bestimmten
                 Schranke ist. Die Schranke für den Exponenten bewegt sich im
                 int-Bereich.
                 */
                if (exponent.compareTo(BigInteger.ZERO) >= 0
                        && exponent.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_POWER_OF_RATIONALS)) <= 0) {
                    return new Constant(numerator.pow(exponent.intValue())).div(denominator.pow(exponent.intValue()));
                }

            }

        }

        return expr;
    }

    /**
     * Falls der Ausdruck expr eine Wurzel gerader Ordnung aus einem negativen
     * Ausdruck darstellt, so wird eine entsprechende EvaluationException
     * geworfen. Ansonsten wird expr zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static BinaryOperation checkNegativityOfBaseInRootsOfEvenDegree(BinaryOperation expr) throws EvaluationException {

        if (expr.isPower() && expr.isConstant() && !expr.containsApproximates()
                && expr.getRight().isRationalConstant()) {

            if (expr.getLeft().isNonNegative() || expr.getLeft().isNonPositive()) {
                // Dann kann das Vorzeichen von expr.getLeft() eindeutig entschieden werden.
                if (expr.getLeft().isNonNegative()) {
                    return expr;
                } else if (expr.getLeft().isNonPositive() && ((BinaryOperation) expr.getRight()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) expr.getRight()).getRight().isEvenIntegerConstant()) {
                    throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyBinaryOperationMethods_ROOTS_OF_EVEN_ORDER_OF_NEGATIVE_NUMBERS_NOT_DEFINED));
                }
            }

            boolean valueLeftIsDefined = false;
            double valueLeft = 0;
            try {
                valueLeft = expr.getLeft().evaluate();
                valueLeftIsDefined = true;
            } catch (EvaluationException e) {
            }

            if (valueLeftIsDefined && valueLeft < 0 && ((BinaryOperation) expr.getRight()).getLeft().isOddIntegerConstant()
                    && ((BinaryOperation) expr.getRight()).getRight().isEvenIntegerConstant()) {
                throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyBinaryOperationMethods_ROOTS_OF_EVEN_ORDER_OF_NEGATIVE_NUMBERS_NOT_DEFINED));
            }

        }

        return expr;

    }

    /**
     * Zieht, falls möglich, ein negatives Vorzeichen aus Wurzeln ungerader
     * Ordnung. Ansonsten wird expr zurückgegeben.<br>
     * BEISPIEL: Bei expr = ((-7)*a)^(3/5) wird -(7*a)^(3/5) zurückgegeben, bei
     * expr = ((-7)*a)^(4/5) wird (7*a)^(4/5) zurückgegeben.
     */
    public static Expression takeMinusSignOutOfOddRoots(BinaryOperation expr) {

        if (expr.isPower() && !expr.containsApproximates()) {

            if (expr.getRight().isRationalConstant() && ((BinaryOperation) expr.getRight()).getRight().isOddIntegerConstant()) {

                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getLeft());
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getLeft());
                boolean allSummandsHaveNegativeCoefficient = true;

                for (Expression summand : summandsLeft) {
                    allSummandsHaveNegativeCoefficient = allSummandsHaveNegativeCoefficient && !summand.hasPositiveSign();
                }
                for (Expression summand : summandsRight) {
                    allSummandsHaveNegativeCoefficient = allSummandsHaveNegativeCoefficient && summand.hasPositiveSign();
                }

                if (allSummandsHaveNegativeCoefficient) {

                    Expression baseNegated;
                    for (int i = 0; i < summandsLeft.getBound(); i++) {
                        summandsLeft.put(i, summandsLeft.get(i).negate());
                    }

                    baseNegated = SimplifyUtilities.produceSum(summandsLeft);
                    baseNegated = baseNegated.add(SimplifyUtilities.produceSum(summandsRight));

                    if (((BinaryOperation) expr.getRight()).getLeft().isEvenIntegerConstant()) {
                        return baseNegated.pow(expr.getRight());
                    }
                    if (((BinaryOperation) expr.getRight()).getLeft().isOddIntegerConstant()) {
                        return MINUS_ONE.mult(baseNegated.pow(expr.getRight()));
                    }

                }

            } else if (expr.getRight().isIntegerConstant()) {

                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getLeft());
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getLeft());
                boolean allSummandsHaveNegativeCoefficient = true;

                for (Expression summand : summandsLeft) {
                    allSummandsHaveNegativeCoefficient = allSummandsHaveNegativeCoefficient && !summand.hasPositiveSign();
                }
                for (Expression summand : summandsRight) {
                    allSummandsHaveNegativeCoefficient = allSummandsHaveNegativeCoefficient && summand.hasPositiveSign();
                }

                if (allSummandsHaveNegativeCoefficient) {

                    Expression baseNegated;
                    for (int i = 0; i < summandsLeft.getBound(); i++) {
                        summandsLeft.put(i, summandsLeft.get(i).negate());
                    }

                    baseNegated = SimplifyUtilities.produceSum(summandsLeft);
                    baseNegated = baseNegated.add(SimplifyUtilities.produceSum(summandsRight));

                    if (expr.getRight().isEvenIntegerConstant()) {
                        return baseNegated.pow(expr.getRight());
                    }
                    if (expr.getRight().isOddIntegerConstant()) {
                        return MINUS_ONE.mult(baseNegated.pow(expr.getRight()));
                    }

                }

            }

        }

        return expr;

    }

    /**
     * Zieht, falls möglich, ein negatives Vorzeichen dem Nenner eines
     * Bruches.<br>
     * BEISPIEL: Bei expr = (a+b*c)/(-x-7*y) wird ((-1)*(a+b*c)/(x+7*y))
     * zurückgegeben.
     */
    public static Expression takeMinusSignOutOfDenominatorInFraction(BinaryOperation expr) {

        if (expr.isQuotient()) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getRight());
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getRight());
            boolean allSummandsHaveNegativeCoefficient = true;

            for (Expression summand : summandsLeft) {
                allSummandsHaveNegativeCoefficient = allSummandsHaveNegativeCoefficient && !summand.hasPositiveSign();
            }
            for (Expression summand : summandsRight) {
                allSummandsHaveNegativeCoefficient = allSummandsHaveNegativeCoefficient && summand.hasPositiveSign();
            }

            if (allSummandsHaveNegativeCoefficient) {

                Expression denominatorNegated;
                for (int i = 0; i < summandsLeft.getBound(); i++) {
                    summandsLeft.put(i, summandsLeft.get(i).negate());
                }

                denominatorNegated = SimplifyUtilities.produceSum(summandsLeft);
                denominatorNegated = denominatorNegated.add(SimplifyUtilities.produceSum(summandsRight));
                return MINUS_ONE.mult(expr.getLeft()).div(denominatorNegated);

            }

        }

        return expr;

    }

    /**
     * Falls expr eine rationale Potenz einer rationalen Zahl a darstellt, so
     * wird multiplikativ die höchste ganze Potenz der Basis a abgespalten.
     * Ansonsten wird expr zurückgegeben.<br>
     * BEISPIEL: bei expr = (7/4)^(22/5) wird (7/4)^4*(7/4)^(2/5) zurückgegeben.
     * (7/4)^4 wird zudem intern vereinfacht. Die Vereinfachung von Potenzen mit
     * ganzzahligem Exponenten geschieht nur solange, wie der Exponent bestimmte
     * Schranken nicht überschreitet.
     *
     * @throws EvaluationException
     */
    public static Expression separateIntegerPowersOfRationalConstants(BinaryOperation expr) throws EvaluationException {

        if (expr.isPower() && !expr.containsApproximates() && expr.getRight().isRationalConstant() && expr.getLeft().isIntegerConstantOrRationalConstant()) {

            BigInteger exponentNumerator = ((Constant) ((BinaryOperation) expr.getRight()).getLeft()).getBigIntValue();
            BigInteger exponentDenominator = ((Constant) ((BinaryOperation) expr.getRight()).getRight()).getBigIntValue();

            BigInteger integerPartOfExponent = exponentNumerator.divide(exponentDenominator);
            if (integerPartOfExponent.compareTo(BigInteger.ZERO) < 0) {
                integerPartOfExponent = integerPartOfExponent.subtract(BigInteger.ONE);
            }
            exponentNumerator = exponentNumerator.subtract(exponentDenominator.multiply(integerPartOfExponent));
            if (integerPartOfExponent.compareTo(BigInteger.ZERO) != 0 && integerPartOfExponent.abs().compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_POWER_OF_RATIONALS)) <= 0) {
                return expr.getLeft().pow(integerPartOfExponent).simplifyBasic().mult(expr.getLeft().pow(exponentNumerator, exponentDenominator));
            }

        }

        return expr;

    }

    /**
     * Falls expr eine Potenz mit rationaler Basis und rationalem Exponenten
     * darstellt, so wird versucht, gewisse Wurzeln/gebrochene Potenzen ZUM TEIL
     * ODER GANZ exakt angegeben werden können. Ansonsten wird expr
     * zurückgegeben.<br>
     * BEISPIEL: 4^(1/4) = 2^(1/2), 4^(x/4) = 2^(x/2), 9^(7/4) = 3^(7/2) etc.
     *
     * @throws EvaluationException
     */
    public static Expression tryTakePartialRootsPrecisely(BinaryOperation expr) throws EvaluationException {

        if (expr.isPower() && expr.getRight().isQuotient()) {

            /*
             Wichtig: wird diese Methode von simplifyBasic() aufgerufen, so
             werden keine Wurzeln gerader Ordnung aus negativen Zahlen
             gezogen, denn diese werden vorher in der Hauptmethode
             simplifyBasic() aussortiert (EvaluationException wird
             geworfen). Bemerkung: Es werden nur Wurzeln bis zur Ordnung <=
             einer bestimmten Schranke exakt gezogen (analog zum Potenzieren).
             */
            ExpressionCollection factorsInExponentDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(expr.getRight());

            BigInteger rootDegree = BigInteger.ONE;
            if (factorsInExponentDenominator.get(0).isIntegerConstant()) {
                /*
                 factorsInExponentDenominator.get(0) sollte positiv sein, da 
                 negative Nenner in der Hauptprozedur simplifyBasic() 
                 vorher positiv gemacht wurden. Die folgende Prüfung dient daher
                 nur zur Sicherheit.
                 */
                if (factorsInExponentDenominator.get(0).isNonPositive()) {
                    return expr;
                }
                rootDegree = ((Constant) factorsInExponentDenominator.get(0)).getBigIntValue();
            }

            // 1. Fall: Basis ist eine ganze Zahl.
            if (expr.getLeft().isIntegerConstant()) {

                BigInteger base = ((Constant) expr.getLeft()).getBigIntValue();

                if (rootDegree.compareTo(BigInteger.ONE) > 0 && rootDegree.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_ROOTDEGREE_OF_RATIONALS)) <= 0) {

                    List<BigInteger> divisorsOfRootDegree = ArithmeticUtils.getDivisors(rootDegree);
                    int root;
                    for (BigInteger divisorOfRootDegree : divisorsOfRootDegree) {
                        root = divisorOfRootDegree.intValue();
                        if (root == 1 || root % 2 == 0 && base.compareTo(BigInteger.ZERO) < 0) {
                            // Es darf nicht versucht werden, Wurzeln gerader Ordnung aus negativen Zahlen zu ziehen.
                            continue;
                        }
                        BigInteger resultBase = ArithmeticUtils.root(base, root);
                        if (resultBase.pow(root).compareTo(base) == 0) {
                            factorsInExponentDenominator.put(0, new Constant(rootDegree.divide(divisorOfRootDegree)));
                            return new Constant(resultBase).pow(((BinaryOperation) expr.getRight()).getLeft().div(SimplifyUtilities.produceProduct(factorsInExponentDenominator)));
                        }
                    }

                }

            }
            if (expr.getLeft().isQuotient()) {
                // 2. Fall: Basis ist ein Quotient.
                if (((BinaryOperation) expr.getLeft()).getLeft().isIntegerConstant()) {

                    BigInteger baseNumerator = ((Constant) ((BinaryOperation) expr.getLeft()).getLeft()).getBigIntValue();

                    if (rootDegree.compareTo(BigInteger.ONE) > 0 && rootDegree.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_ROOTDEGREE_OF_RATIONALS)) <= 0) {

                        List<BigInteger> divisorsOfN = ArithmeticUtils.getDivisors(rootDegree);
                        int root;
                        for (BigInteger divisorOfN : divisorsOfN) {
                            root = divisorOfN.intValue();
                            if (root == 1 || root % 2 == 0 && baseNumerator.compareTo(BigInteger.ZERO) < 0) {
                                /*
                                 Es darf nicht versucht werden, Wurzeln
                                 gerader Ordnung aus negativen Zahlen zu
                                 ziehen.
                                 */
                                continue;
                            }
                            BigInteger resultBaseNumerator = ArithmeticUtils.root(baseNumerator, root);
                            if (resultBaseNumerator.pow(root).compareTo(baseNumerator) == 0) {
                                factorsInExponentDenominator.put(0, new Constant(rootDegree.divide(divisorOfN)));
                                return new Constant(resultBaseNumerator).pow(((BinaryOperation) expr.getRight()).getLeft().div(SimplifyUtilities.produceProduct(factorsInExponentDenominator))).div(
                                        (((BinaryOperation) expr.getLeft()).getRight()).pow(expr.getRight()));
                            }
                        }

                    }

                }
                if (((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {

                    BigInteger baseDenominator = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getBigIntValue();

                    if (rootDegree.compareTo(BigInteger.ONE) > 0 && rootDegree.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_ROOTDEGREE_OF_RATIONALS)) <= 0) {

                        List<BigInteger> divisorsOfN = ArithmeticUtils.getDivisors(rootDegree);
                        int root;
                        for (BigInteger divisorOfN : divisorsOfN) {
                            root = divisorOfN.intValue();
                            if (root == 1 || root % 2 == 0 && baseDenominator.compareTo(BigInteger.ZERO) < 0) {
                                /*
                                 Es darf nicht versucht werden, Wurzeln
                                 gerader Ordnung aus negativen Zahlen zu
                                 ziehen.
                                 */
                                continue;
                            }
                            BigInteger resultBaseDenominator = ArithmeticUtils.root(baseDenominator, root);
                            if (resultBaseDenominator.pow(root).compareTo(baseDenominator) == 0) {
                                factorsInExponentDenominator.put(0, new Constant(rootDegree.divide(divisorOfN)));
                                return (((BinaryOperation) expr.getLeft()).getLeft()).pow(expr.getRight()).div(new Constant(resultBaseDenominator).pow(((BinaryOperation) expr.getRight()).getLeft().div(
                                        SimplifyUtilities.produceProduct(factorsInExponentDenominator))));
                            }
                        }

                    }

                }

            }
        }

        return expr;

    }

    /**
     * Falls expr eine ganzzahlige Potenz eines Quotienten darstellt, also expr
     * = (a/b)^n mit n ganz, und falls a oder b ganze Zahlen sind, so wird
     * a^n/b^n zurückgegeben. Ansonsten wird expr zurückgegeben.
     */
    public static Expression simplifyPowerOfQuotient(BinaryOperation expr) {

        if (expr.isPower() && expr.getLeft().isQuotient() && expr.getRight().isIntegerConstant()) {

            // Falls der Zähler a und der Exponent n ganze Zahlen sind: (a/b)^n = a^n/b^n
            if (((BinaryOperation) expr.getLeft()).getLeft().isIntegerConstant()) {
                BigDecimal constantNumerator = ((Constant) ((BinaryOperation) expr.getLeft()).getLeft()).getValue();
                /*
                 Potenzen von Brüchen sollen nur vereinfacht werden, wenn
                 Exponenten <= einer bestimmten Schranke sind. Diese Schranke
                 bewegt sich im int-Bereich.
                 */
                if (((Constant) expr.getRight()).getValue().compareTo(BigDecimal.ZERO) >= 0
                        && ((Constant) expr.getRight()).getValue().compareTo(BigDecimal.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_POWER_OF_RATIONALS)) <= 0) {
                    constantNumerator = constantNumerator.pow(((Constant) expr.getRight()).getValue().intValue());
                    return new Constant(constantNumerator).div(((BinaryOperation) expr.getLeft()).getRight().pow(expr.getRight()));
                }
            }
            // Falls der Nenner b und der Exponent n ganze Zahlen sind: (a/b)^n = a^n/b^n
            if (((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                BigDecimal constantDenominator = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue();
                /*
                 Potenzen von Brüchen sollen nur vereinfacht werden, wenn
                 Exponenten <= einer bestimmten Schranke sind.
                 */
                if (expr.getRight().isIntegerConstant()
                        && ((Constant) expr.getRight()).getValue().compareTo(BigDecimal.ZERO) >= 0
                        && ((Constant) expr.getRight()).getValue().compareTo(BigDecimal.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_POWER_OF_RATIONALS)) <= 0) {
                    constantDenominator = constantDenominator.pow(((Constant) expr.getRight()).getValue().intValue());
                    return ((BinaryOperation) expr.getLeft()).getLeft().pow(expr.getRight()).div(constantDenominator);
                }
            }

        }

        return expr;

    }

    /**
     * Diverse triviale Vereinfachungen, welche 1 oder 0 in Differenzen
     * involvieren, werden vorgenommen.
     *
     * @throws EvaluationException
     */
    public static Expression trivialOperationsInDifferenceWithZeroOne(BinaryOperation expr) throws EvaluationException {

        //a+0 = a und a-0 = a
        if (expr.isDifference() && expr.getRight().equals(ZERO)) {
            return expr.getLeft();
        }

        //a-a = 0
        if (expr.isDifference() && expr.getLeft().equals(expr.getRight())) {
            return ZERO;
        }

        //0-a = (-1)*a
        if (expr.isDifference() && expr.getLeft().equals(ZERO)) {
            return MINUS_ONE.mult(expr.getRight());
        }

        return expr;

    }

    /**
     * Diverse triviale Vereinfachungen, welche 1 oder 0 in Quotienten
     * involvieren, werden vorgenommen.
     *
     * @throws EvaluationException
     */
    public static Expression trivialOperationsInQuotientWithZeroOne(BinaryOperation expr) throws EvaluationException {

        // a/1 = a
        if (expr.isQuotient() && expr.getRight().equals(ONE)) {
            return expr.getLeft();
        }

        // 0/a = 0
        if (expr.isQuotient() && expr.getLeft().equals(ZERO) && !expr.getRight().equals(ZERO)) {
            return ZERO;
        }

        // a/0 = FEHLER!
        if (expr.isQuotient() && expr.getRight().equals(ZERO)) {
            throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyBinaryOperationMethods_DIVISION_BY_ZERO));
        }

        return expr;

    }

    /**
     * Diverse triviale Vereinfachungen, welche 1 oder 0 in Potenzen
     * involvieren, werden vorgenommen.
     *
     * @throws EvaluationException
     */
    public static Expression trivialOperationsInPowerWithZeroOne(BinaryOperation expr) throws EvaluationException {

        // a^0 = 1
        if (expr.isPower() && expr.getRight().equals(ZERO)) {
            return ONE;
        }

        // a^1 = a
        if (expr.isPower() && expr.getRight().equals(ONE)) {
            return expr.getLeft();
        }

        // 0^a = 0
        if (expr.isPower() && expr.getLeft().equals(ZERO)) {
            if (expr.getRight().isConstant() && expr.getRight().isNonPositive() && !expr.getRight().equals(Expression.ZERO)) {
                throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyBinaryOperationMethods_NEGATIVE_POWERS_OF_ZERO_NOT_DEFINED));
            }
            return ZERO;
        }

        // 1^a = 1
        if (expr.isPower() && expr.getLeft().equals(ONE)) {
            return ONE;
        }

        return expr;

    }

    /**
     * Falls expr eine Potenz eines Quotienten oder des Betrags eines Quotienten
     * darstellt, bei der der Exponent konstant und negativ ist, so wird dieser
     * in einen Kehrwert (mit entsprechendem positiven Exponenten) umgewandelt
     * und zurückgegeben. Ansonsten wird expr zurückgegeben. Beispiel: bei expr
     * = (a/b)^(-3/2) wird (b/a)^(3/2) zurückgegeben.
     */
    public static Expression negativePowersOfQuotientsToReciprocal(BinaryOperation expr) {

        if (expr.isPower() && (expr.getLeft().isQuotient() || expr.getLeft().isFunction(TypeFunction.abs)
                && ((Function) expr.getLeft()).getLeft().isQuotient())
                && expr.getRight().isNegative()) {

            if (expr.getLeft().isQuotient()) {
                return ((BinaryOperation) expr.getLeft()).getRight().div(((BinaryOperation) expr.getLeft()).getLeft()).pow(
                        expr.getRight().negate());
            }
            Expression argumentOfAbs = ((Function) expr.getLeft()).getLeft();
            return ((BinaryOperation) argumentOfAbs).getRight().div(((BinaryOperation) argumentOfAbs).getLeft()).abs().pow(
                    expr.getRight().negate());
        }
        return expr;

    }

    /**
     * Falls expr eine Potenz darstellt, wobei die Basis KEIN Quotient ist, bei
     * der der Exponent rational und negativ ist, so wird dieser in einen
     * Kehrwert (mit entsprechendem positiven Exponenten) umgewandelt und
     * zurückgegeben.<br>
     * BEISPIEL: bei expr = a^(-3/2) wird 1/a^(3/2) zurückgegeben.
     */
    public static Expression negativePowersOfExpressionsToReciprocal(BinaryOperation expr) {
        if (expr.isPower() && expr.getLeft().isNotQuotient() && expr.getRight().isIntegerConstantOrRationalConstantNegative()) {
            return ONE.div(expr.getLeft().pow(MINUS_ONE.mult(expr.getRight())));
        }
        return expr;
    }

    /**
     * Falls expr einen Ausdruck der Form (1/x)^y darstellt (mit beliebigem y),
     * so wird 1/x^y zurückgegeben. Ansonsten wird expr zurückgegeben.
     */
    public static Expression simplifyPowersOfReciprocals(BinaryOperation expr) {
        if (expr.isPower() && expr.getLeft().isQuotient() && ((BinaryOperation) expr.getLeft()).getLeft().equals(ONE)) {
            return ONE.div(((BinaryOperation) expr.getLeft()).getRight().pow(expr.getRight()));
        }
        return expr;
    }

    /**
     * In einer Summe oder Differenz, in der negative Koeffizienten auftreten,
     * so werden diese entsprechend vereinfacht. Genauer wird folgendes
     * vereinfacht: (1) x + c*y = x - (-c)*y, falls c &#60; 0 (2) x - c*y = x +
     * (-c)*y, falls c &#60; 0.
     */
    public static void simplifySumsAndDifferencesWithNegativeCoefficient(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) {

        /* 
         Vorab: wenn summandsLeft nur ein Element besitzt und dieses einen negativen 
         Koeffizienten besitzt, dann nichts zun.
         */
        boolean allSummandsLeftHaveNegativCoefficients = true;
        for (int i = 0; i < summandsLeft.getBound(); i++) {
            if (summandsLeft.get(i) == null) {
                continue;
            }
            if (summandsLeft.get(i) != null && summandsLeft.get(i).hasPositiveSign()) {
                allSummandsLeftHaveNegativCoefficients = false;
                break;
            }
        }

        Expression summand;
        if (allSummandsLeftHaveNegativCoefficients) {
            /*
             Dann werden alle Summanden im Minuenden, bis auf den ersten, in den 
             Subtrahenden mit dem entsprechenden Vorzeichen verschoben.
             */
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                if (summandsLeft.get(i) == null) {
                    continue;
                }
                for (int j = i + 1; j < summandsLeft.getBound(); j++) {
                    if (summandsLeft.get(j) == null) {
                        continue;
                    }
                    summand = summandsLeft.get(j);
                    summandsLeft.remove(j);
                    summandsRight.add(summand.negate());
                }
                break;
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                if (summandsRight.get(i) != null && !summandsRight.get(i).hasPositiveSign()) {
                    summand = summandsRight.get(i);
                    summandsRight.remove(i);
                    summandsLeft.add(summand.negate());
                }
            }
        } else {
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                if (summandsLeft.get(i) != null && !summandsLeft.get(i).hasPositiveSign()) {
                    summand = summandsLeft.get(i);
                    summandsLeft.remove(i);
                    summandsRight.add(summand.negate());
                }
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                if (summandsRight.get(i) != null && !summandsRight.get(i).hasPositiveSign()) {
                    summand = summandsRight.get(i);
                    summandsRight.remove(i);
                    summandsLeft.add(summand.negate());
                }
            }
        }

    }

    /**
     * In einer Summe oder Differenz, in der negative Koeffizienten auftreten,
     * so werden diese entsprechend vereinfacht. Genauer wird folgendes
     * vereinfacht: (1) a_1*...*a_i*...*a_n = (-1)*a_1*...*(-a_i)*...*a_n, falls
     * a_i eine Summe oder eine Differenz ist, in der die Summanden alle ein
     * negatives Vorzeichen besitzen.
     */
    public static void pullMinusSignFromProductOrQuotientsWithCompleteNegativeSums(ExpressionCollection factorsNumerator, ExpressionCollection factorsDenominator) {

        ExpressionCollection summandsLeft, summandsRight;
        boolean allSignsAreNegative;
        /* signAtTheEnd = true bedeutet, dass am Ende der Methode der Ausdruck ein 
         positives Vorzeichen besitzt, andernfalls ein negatives.
         */
        boolean signAtTheEnd = true;

        // Zähler durchsuchen.
        if (factorsNumerator.getSize() > 1) {
            // Umformung nur bei echten Produkten vornehmen!
            for (int i = 0; i < factorsNumerator.getBound(); i++) {

                if (factorsNumerator.get(i) == null) {
                    continue;
                }

                summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(factorsNumerator.get(i));
                summandsRight = SimplifyUtilities.getSummandsRightInExpression(factorsNumerator.get(i));
                allSignsAreNegative = true;

                // Nur bei echten Summen / Differenzen fortfahren.
                if (summandsLeft.getBound() + summandsRight.getBound() < 2) {
                    continue;
                }

                for (Expression summandLeft : summandsLeft) {
                    if (summandLeft.hasPositiveSign()) {
                        allSignsAreNegative = false;
                        break;
                    }
                }
                if (allSignsAreNegative) {
                    for (Expression summandRight : summandsRight) {
                        if (!summandRight.hasPositiveSign()) {
                            allSignsAreNegative = false;
                            break;
                        }
                    }
                }

                if (allSignsAreNegative) {
                    for (int j = 0; j < summandsLeft.getBound(); j++) {
                        summandsLeft.put(j, summandsLeft.get(j).negate());
                    }
                    for (int j = 0; j < summandsRight.getBound(); j++) {
                        summandsRight.put(j, summandsRight.get(j).negate());
                    }
                    factorsNumerator.put(i, SimplifyUtilities.produceDifference(summandsLeft, summandsRight));
                    signAtTheEnd = !signAtTheEnd;
                }

            }
        }

        // Nenner durchsuchen.
        if (factorsDenominator.getSize() > 1) {
            // Umformung nur bei echten Produkten vornehmen!
            for (int i = 0; i < factorsDenominator.getBound(); i++) {

                if (factorsDenominator.get(i) == null) {
                    continue;
                }

                summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(factorsDenominator.get(i));
                summandsRight = SimplifyUtilities.getSummandsRightInExpression(factorsDenominator.get(i));
                allSignsAreNegative = true;

                // Nur bei echten Summen / Differenzen fortfahren.
                if (summandsLeft.getBound() + summandsRight.getBound() < 2) {
                    continue;
                }

                for (Expression summandLeft : summandsLeft) {
                    if (summandLeft.hasPositiveSign()) {
                        allSignsAreNegative = false;
                        break;
                    }
                }
                if (allSignsAreNegative) {
                    for (Expression summandRight : summandsRight) {
                        if (!summandRight.hasPositiveSign()) {
                            allSignsAreNegative = false;
                            break;
                        }
                    }
                }

                if (allSignsAreNegative) {
                    for (int j = 0; j < summandsLeft.getBound(); j++) {
                        summandsLeft.put(j, summandsLeft.get(j).negate());
                    }
                    for (int j = 0; j < summandsRight.getBound(); j++) {
                        summandsRight.put(j, summandsRight.get(j).negate());
                    }
                    factorsDenominator.put(i, SimplifyUtilities.produceDifference(summandsLeft, summandsRight));
                    signAtTheEnd = !signAtTheEnd;
                }

            }
        }

        if (!signAtTheEnd) {
            factorsNumerator.add(MINUS_ONE);
        }

    }

    /**
     * Vereinfacht Doppelpotenzen, falls möglich. Ansonsten wird expr
     * zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Expression simplifyDoublePowers(BinaryOperation expr) throws EvaluationException {

        if (expr.isPower() && expr.getLeft().isPower()) {
            // Zunächst: Ist die Basis immer nichtnegativ, so werden die Exponenten einfach ausmultipliziert.
            if (((BinaryOperation) expr.getLeft()).getLeft().isAlwaysNonNegative()) {
                return ((BinaryOperation) expr.getLeft()).getLeft().pow(((BinaryOperation) expr.getLeft()).getRight().mult(expr.getRight()).simplifyBasic());
            }
            if (((BinaryOperation) expr.getLeft()).getRight().isEvenIntegerConstant() && expr.getRight().isRationalConstant()
                    && ((BinaryOperation) expr.getRight()).getRight().isEvenIntegerConstant()) {
                // In diesem Fall: x^(2*k)^(n/(2*m)) = abs(x)^(k*n/m)
                return ((BinaryOperation) expr.getLeft()).getLeft().abs().pow(((BinaryOperation) expr.getLeft()).getRight().mult(expr.getRight()));
            }
            if (((BinaryOperation) expr.getLeft()).getRight().isRationalConstant() && expr.getRight().isIntegerConstantOrRationalConstant()) {

                /* 
                 In diesem Fall: x^((2*m+1)/(2*n))^(p/q) bleibt expr, falls der 
                 ausmultiplizierte und gekürzte Exponent einen ungeraden Nenner besitzt.
                 */
                BigInteger a = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getRight()).getLeft()).getBigIntValue();
                BigInteger b = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getRight()).getRight()).getBigIntValue();
                BigInteger c, d;
                if (expr.getRight().isIntegerConstant()) {
                    c = ((Constant) expr.getRight()).getBigIntValue();
                    d = BigInteger.ONE;
                } else {
                    c = ((Constant) ((BinaryOperation) expr.getRight()).getLeft()).getBigIntValue();
                    d = ((Constant) ((BinaryOperation) expr.getRight()).getRight()).getBigIntValue();
                }

                BigInteger exponentNumerator = a.multiply(c);
                BigInteger exponentDenominator = b.multiply(d);
                BigInteger gcdOfNumeratorAndDenominator = exponentNumerator.gcd(exponentDenominator);
                exponentDenominator = exponentDenominator.divide(gcdOfNumeratorAndDenominator);
                if (b.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO) && !exponentDenominator.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
                    return expr;
                }

            }
            // Ansonsten einfach nur Exponenten ausmultiplizieren.
            return ((BinaryOperation) expr.getLeft()).getLeft().pow(((BinaryOperation) expr.getLeft()).getRight().mult(expr.getRight()).simplifyBasic());
        }

        return expr;

    }

    /**
     * Falls expr von der Form exp(x)^k ist, so wird exp(x*k) zurückgegeben.
     * Ansonsten wird expr zurückgegeben.
     */
    public static Expression simplifyPowersOfExpFunction(BinaryOperation expr) {
        if (expr.isPower() && expr.getLeft() instanceof Function
                && ((Function) expr.getLeft()).getType().equals(TypeFunction.exp)) {
            return new Function(((Function) expr.getLeft()).getLeft().mult(expr.getRight()), TypeFunction.exp);
        }
        return expr;
    }

    /**
     * Methode zum Kürzen von Leitkoeffizienten in Quotienten (für
     * reduceLeadingCoefficients), falls der Ausdruck approximiert werden soll.
     *
     * @throws EvaluationException
     */
    public static void reduceLeadingCoefficientsInQuotientInApprox(ExpressionCollection factorsNumerator, ExpressionCollection factorsDenominator) throws EvaluationException {

        // Prüfen, ob Approximationen vorliegen.
        boolean approximatesFound = false;
        for (int i = 0; i < factorsNumerator.getBound(); i++) {
            if (factorsNumerator.get(i) == null) {
                continue;
            }
            if (factorsNumerator.get(i).containsApproximates()) {
                approximatesFound = true;
                break;
            }
        }
        if (!approximatesFound) {
            for (int i = 0; i < factorsDenominator.getBound(); i++) {
                if (factorsDenominator.get(i) == null) {
                    continue;
                }
                if (factorsDenominator.get(i).containsApproximates()) {
                    approximatesFound = true;
                    break;
                }
            }
        }

        // Falls keine zu approximierenden Ausdrücke vorliegen -> beenden.
        if (!approximatesFound) {
            return;
        }

        if (((factorsNumerator.get(0) == null) || !factorsNumerator.get(0).isConstant())
                && factorsDenominator.get(0) != null && factorsDenominator.get(0).isConstant()) {

            double coefficientDenominator = factorsDenominator.get(0).evaluate();

            if (factorsNumerator.get(0) == null) {
                factorsNumerator.put(0, new Constant(1 / coefficientDenominator));
            } else {
                factorsNumerator.put(0, new Constant(1 / coefficientDenominator).mult(factorsNumerator.get(0)));
            }
            factorsDenominator.remove(0);

        } else if (factorsNumerator.get(0) != null && factorsNumerator.get(0).isConstant()
                && factorsDenominator.get(0) != null && factorsDenominator.get(0).isConstant()) {

            double coefficientNumerator = factorsNumerator.get(0).evaluate();
            double coefficientDenominator = factorsDenominator.get(0).evaluate();
            factorsNumerator.put(0, new Constant(coefficientNumerator / coefficientDenominator));
            factorsDenominator.remove(0);

        }

    }

    /**
     * Methode zum Kürzen von Leitkoeffizienten in Quotienten (für
     * reduceLeadingCoefficients), falls der Ausdruck NICHT approximiert,
     * sondern exakt berechnet werden soll.
     */
    public static void reduceLeadingCoefficientsInQuotient(ExpressionCollection factorsNumerator, ExpressionCollection factorsDenominator) {

        // Prüfen, ob Approximationen vorliegen.
        boolean approximatesFound = false;
        for (int i = 0; i < factorsNumerator.getBound(); i++) {
            if (factorsNumerator.get(i) == null) {
                continue;
            }
            if (factorsNumerator.get(i).containsApproximates()) {
                approximatesFound = true;
                break;
            }
        }
        if (!approximatesFound) {
            for (int i = 0; i < factorsDenominator.getBound(); i++) {
                if (factorsDenominator.get(i) == null) {
                    continue;
                }
                if (factorsDenominator.get(i).containsApproximates()) {
                    approximatesFound = true;
                    break;
                }
            }
        }

        // Falls zu approximierenden Ausdrücke vorliegen -> beenden.
        if (approximatesFound) {
            return;
        }

        // (c1 * v1) / (c2 * v2) = (c1/ggT(c1, c2) * v1) / (c2/ggT(c1, c2) * v2)
        if (factorsNumerator.get(0) != null && factorsNumerator.get(0) instanceof Constant
                && factorsDenominator.get(0) != null && factorsDenominator.get(0) instanceof Constant) {

            BigDecimal numerator = ((Constant) factorsNumerator.get(0)).getValue();
            BigDecimal denominator = ((Constant) factorsDenominator.get(0)).getValue();
            BigInteger[] reducedFraction = reduceFraction(numerator, denominator);

            factorsNumerator.put(0, new Constant(reducedFraction[0]));
            factorsDenominator.put(0, new Constant(reducedFraction[1]));

        } else if ((factorsNumerator.get(0) == null || !(factorsNumerator.get(0) instanceof Constant))
                && factorsDenominator.get(0) != null && factorsDenominator.get(0) instanceof Constant) {

            // v1 / (c * v2) = -v1 / (-c * v2), falls c < 0.
            BigDecimal coefficientDenominator = ((Constant) factorsDenominator.get(0)).getValue();
            if (coefficientDenominator.compareTo(BigDecimal.ZERO) < 0) {

                if (factorsNumerator.get(0) == null) {
                    factorsNumerator.put(0, MINUS_ONE);
                } else {
                    factorsNumerator.put(0, MINUS_ONE.mult(factorsNumerator.get(0)));
                }
                factorsDenominator.put(0, new Constant(BigDecimal.ONE.negate().multiply(((Constant) factorsDenominator.get(0)).getValue())));

            }

        }

    }

    /**
     * Methode zum Kürzen von Leitkoeffizienten in Differenzen (für
     * reduceLeadingCoefficients), falls der Ausdruck approximiert werden soll.
     *
     * @throws EvaluationException
     */
    public static void reduceLeadingCoefficientsInDifferenceInApprox(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) throws EvaluationException {

        // Prüfen, ob Approximationen vorliegen.
        boolean approximatesFound = false;
        for (int i = 0; i < summandsLeft.getBound(); i++) {
            if (summandsLeft.get(i) == null) {
                continue;
            }
            if (summandsLeft.get(i).containsApproximates()) {
                approximatesFound = true;
                break;
            }
        }
        if (!approximatesFound) {
            for (int i = 0; i < summandsRight.getBound(); i++) {
                if (summandsRight.get(i) == null) {
                    continue;
                }
                if (summandsRight.get(i).containsApproximates()) {
                    approximatesFound = true;
                    break;
                }
            }
        }

        // Falls keine zu approximierenden Ausdrücke vorliegen -> beenden.
        if (!approximatesFound) {
            return;
        }

        if (summandsLeft.get(0) != null && summandsLeft.get(0).isConstant()
                && summandsRight.get(0) != null && summandsRight.get(0).isConstant()) {

            double coefficientLeft = summandsLeft.get(0).evaluate();
            double coefficientRight = summandsRight.get(0).evaluate();
            if (coefficientLeft > coefficientRight) {
                summandsLeft.put(0, new Constant(coefficientLeft - coefficientRight));
                summandsRight.remove(0);
            } else if (coefficientLeft < coefficientRight) {
                summandsRight.put(0, new Constant(coefficientRight - coefficientLeft));
                summandsLeft.remove(0);
            } else {
                summandsLeft.remove(0);
                summandsRight.remove(0);
            }

        }

    }

    /**
     * Methoden zum Kürzen von Leitkoeffizienten in Differenzen (für
     * reduceLeadingCoefficients), falls der Ausdruck NICHT approximiert werden
     * soll.
     */
    public static void reduceLeadingCoefficientsInDifference(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) {

        // Prüfen, ob Approximationen vorliegen.
        boolean approximatesFound = false;
        for (int i = 0; i < summandsLeft.getBound(); i++) {
            if (summandsLeft.get(i) == null) {
                continue;
            }
            if (summandsLeft.get(i).containsApproximates()) {
                approximatesFound = true;
                break;
            }
        }
        if (!approximatesFound) {
            for (int i = 0; i < summandsRight.getBound(); i++) {
                if (summandsRight.get(i) == null) {
                    continue;
                }
                if (summandsRight.get(i).containsApproximates()) {
                    approximatesFound = true;
                    break;
                }
            }
        }

        // Falls zu approximierenden Ausdrücke vorliegen -> beenden.
        if (approximatesFound) {
            return;
        }

        // (c1 + v1) - (c2 + v2) = ((c1 - c2) + v1) - v2 bzw. = v1 - ((c2 - c1) + v2) für rationale c1, c2.
        if (summandsLeft.get(0) != null && summandsLeft.get(0).isIntegerConstantOrRationalConstant()
                && summandsRight.get(0) != null && summandsRight.get(0).isIntegerConstantOrRationalConstant()) {

            if (summandsLeft.get(0) instanceof Constant && summandsRight.get(0) instanceof Constant) {

                BigDecimal c_1 = ((Constant) summandsLeft.get(0)).getValue();
                BigDecimal c_2 = ((Constant) summandsRight.get(0)).getValue();
                if (c_1.compareTo(c_2) > 0) {
                    summandsLeft.put(0, new Constant(c_1.subtract(c_2)));
                    summandsRight.remove(0);
                } else if (c_1.compareTo(c_2) < 0) {
                    summandsLeft.remove(0);
                    summandsRight.put(0, new Constant(c_2.subtract(c_1)));
                } else {
                    summandsLeft.remove(0);
                    summandsRight.remove(0);
                }

            } else if (!(summandsLeft.get(0) instanceof Constant) && summandsRight.get(0) instanceof Constant) {

                BigDecimal c_1 = ((Constant) ((BinaryOperation) summandsLeft.get(0)).getLeft()).getValue();
                BigDecimal c_2 = ((Constant) ((BinaryOperation) summandsLeft.get(0)).getRight()).getValue();
                BigDecimal c_3 = ((Constant) summandsRight.get(0)).getValue();
                BigDecimal numerator = c_1.subtract(c_2.multiply(c_3));
                if (numerator.multiply(c_3).compareTo(BigDecimal.ZERO) > 0) {
                    summandsLeft.put(0, new Constant(numerator).div(c_2));
                    summandsRight.remove(0);
                } else if (numerator.multiply(c_3).compareTo(BigDecimal.ZERO) < 0) {
                    summandsLeft.remove(0);
                    summandsRight.put(0, new Constant(numerator.negate()).div(c_2));
                } else {
                    summandsLeft.remove(0);
                    summandsRight.remove(0);
                }

            } else if (summandsLeft.get(0) instanceof Constant && !(summandsRight.get(0) instanceof Constant)) {

                BigDecimal c_1 = ((Constant) summandsLeft.get(0)).getValue();
                BigDecimal c_2 = ((Constant) ((BinaryOperation) summandsRight.get(0)).getLeft()).getValue();
                BigDecimal c_3 = ((Constant) ((BinaryOperation) summandsRight.get(0)).getRight()).getValue();
                BigDecimal numerator = c_1.multiply(c_3).subtract(c_2);
                if (numerator.multiply(c_3).compareTo(BigDecimal.ZERO) > 0) {
                    summandsLeft.put(0, new Constant(numerator).div(c_3));
                    summandsRight.remove(0);
                } else if (numerator.multiply(c_3).compareTo(BigDecimal.ZERO) < 0) {
                    summandsLeft.remove(0);
                    summandsRight.put(0, new Constant(numerator.negate()).div(c_3));
                } else {
                    summandsLeft.remove(0);
                    summandsRight.remove(0);
                }

            } else {

                BigDecimal c_1 = ((Constant) ((BinaryOperation) summandsLeft.get(0)).getLeft()).getValue();
                BigDecimal c_2 = ((Constant) ((BinaryOperation) summandsLeft.get(0)).getRight()).getValue();
                BigDecimal c_3 = ((Constant) ((BinaryOperation) summandsRight.get(0)).getLeft()).getValue();
                BigDecimal c_4 = ((Constant) ((BinaryOperation) summandsRight.get(0)).getRight()).getValue();
                BigDecimal numerator = c_1.multiply(c_4).subtract(c_2.multiply(c_3));
                BigDecimal denominator = c_2.multiply(c_4);
                if (numerator.multiply(denominator).compareTo(BigDecimal.ZERO) > 0) {
                    summandsLeft.put(0, new Constant(numerator).div(denominator));
                    summandsRight.remove(0);
                } else if (numerator.multiply(denominator).compareTo(BigDecimal.ZERO) < 0) {
                    summandsLeft.remove(0);
                    summandsRight.put(0, new Constant(numerator.negate()).div(denominator));
                } else {
                    summandsLeft.remove(0);
                    summandsRight.remove(0);
                }

            }

        }

    }

    /**
     * Kürzt ganzzahlige Faktoren aus Brüchen, z. B. wird a*(25*x +
     * 10*y)*b/(80*u - 35*v) zu a*(5*x + 2*y)*b/(16*u - 7*v) vereinfacht.
     */
    public static void reduceGCDInQuotient(ExpressionCollection factorsNumerator, ExpressionCollection factorsDenominator) throws EvaluationException {

        Expression numerator, denominator;
        Expression[] reducedNumeratorAndDenominator;

        for (int i = 0; i < factorsNumerator.getBound(); i++) {

            if (factorsNumerator.get(i) == null) {
                continue;
            }

            numerator = factorsNumerator.get(i);
            for (int j = 0; j < factorsDenominator.getBound(); j++) {

                if (factorsDenominator.get(j) == null) {
                    continue;
                }

                denominator = factorsDenominator.get(j);

                /*
                 Fall: Zähler und Nenner sind von der Form a*u und b*v, mit
                 ganzen a, b. Dann zu ((a/ggT(a,b))*u)/((b/ggT(a,b))*v)
                 kürzen.
                 */
                if (numerator.isNotPower()) {

                    reducedNumeratorAndDenominator = reduceGCDInNumeratorAndDenominator(numerator, denominator);
                    if (!reducedNumeratorAndDenominator[0].equivalent(numerator)) {
                        // Dann KONNTE etwas gekürzt werden!
                        if (!reducedNumeratorAndDenominator[1].equals(Expression.ONE)) {
                            factorsNumerator.put(i, reducedNumeratorAndDenominator[0]);
                            factorsDenominator.put(j, reducedNumeratorAndDenominator[1]);
                        } else {
                            factorsNumerator.put(i, reducedNumeratorAndDenominator[0]);
                            factorsDenominator.remove(j);
                        }
                        break;
                    }

                }

                /*
                 Fall: Zähler und Nenner sind von der Form (a*u)^k und
                 (b*v)^k, mit ganzen a, b. Dann zu
                 (((a/ggT(a,b))*u)/((b/ggT(a,b))*v))^k kürzen.
                 */
                if (numerator.isPower() && denominator.isPower()
                        && ((BinaryOperation) numerator).getRight().equivalent(((BinaryOperation) denominator).getRight())) {
                    if ((((BinaryOperation) numerator).getLeft().isSum() || ((BinaryOperation) numerator).getLeft().isDifference())
                            && (((BinaryOperation) denominator).getLeft().isSum() || ((BinaryOperation) denominator).getLeft().isDifference())) {

                        reducedNumeratorAndDenominator = reduceGCDInNumeratorAndDenominator(((BinaryOperation) numerator).getLeft(), ((BinaryOperation) denominator).getLeft());
                        if (!reducedNumeratorAndDenominator[0].equivalent(((BinaryOperation) numerator).getLeft())) {
                            /*
                             Es konnte mindestens ein Faktor im Zähler gegen
                             einen Faktor im Nenner gekürzt werden.
                             */
                            if (!reducedNumeratorAndDenominator[1].equals(Expression.ONE)) {
                                factorsNumerator.put(i, reducedNumeratorAndDenominator[0].pow(((BinaryOperation) numerator).getRight()));
                                factorsDenominator.put(j, reducedNumeratorAndDenominator[1].pow(((BinaryOperation) numerator).getRight()));
                            } else {
                                factorsNumerator.put(i, reducedNumeratorAndDenominator[0].pow(((BinaryOperation) numerator).getRight()));
                                factorsDenominator.remove(j);
                            }
                            break;
                        }

                    }
                }

            }

        }

    }

    /**
     * Vereinfacht beispielsweise 6*(1/8+a/4)*b zu 3*(1/4+a/2)*b
     *
     * @throws EvaluationException
     */
    public static void pullGCDOfCoefficientsInProducts(ExpressionCollection factors) throws EvaluationException {

        /*
         Die Methode soll nur dann ausgeführt werden, wenn factors entweder aus mindestens
         zwei Faktoren besteht, oder aus einem Faktor und dieser eine Faktor ist eine Potenz.
         */
        boolean uniqueFactorIsAPower = false;
        if (factors.getSize() == 1) {
            for (Expression factor : factors) {
                if (factor.isPower()) {
                    uniqueFactorIsAPower = true;
                }
            }
        }
        if (factors.getSize() < 2 && !uniqueFactorIsAPower) {
            return;
        }

        ExpressionCollection summandsLeft, summandsRight;
        BigInteger gcdOfNumerators, gcdOfDenominators;
        Expression factor, exponent;

        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null || factors.get(i).isNotSum() && factors.get(i).isNotDifference()
                    && factors.get(i).isNotPower()) {
                continue;
            }

            if (factors.get(i).isPower()) {
                factor = ((BinaryOperation) factors.get(i)).getLeft();
                exponent = ((BinaryOperation) factors.get(i)).getRight();
            } else {
                factor = factors.get(i);
                exponent = ONE;
            }

            if (factor.isNotSum() && factor.isNotDifference()) {
                continue;
            }

            summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(factor);
            summandsRight = SimplifyUtilities.getSummandsRightInExpression(factor);

            gcdOfNumerators = BigInteger.ZERO;
            gcdOfDenominators = BigInteger.ZERO;

            // Prüfen, ob die Zähler einen gemeinsamen ggT > 1 besitzen.
            for (Expression summand : summandsLeft) {
                gcdOfNumerators = gcdOfNumerators.gcd(getCoefficientInNumerator(summand));
            }
            for (Expression summand : summandsRight) {
                gcdOfNumerators = gcdOfNumerators.gcd(getCoefficientInNumerator(summand));
            }
            // Prüfen, ob die Nenner einen gemeinsamen ggT > 1 besitzen.
            for (Expression summand : summandsLeft) {
                gcdOfDenominators = gcdOfDenominators.gcd(getCoefficientInDenominator(summand));
            }
            for (Expression summand : summandsRight) {
                gcdOfDenominators = gcdOfDenominators.gcd(getCoefficientInDenominator(summand));
            }

            if (gcdOfNumerators.compareTo(BigInteger.ONE) > 0) {

                ExpressionCollection factorsNumerator, factorsDenominator;
                for (int j = 0; j < summandsLeft.getBound(); j++) {
                    factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsLeft.get(j));
                    factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(j));
                    if (factorsNumerator.get(0).isIntegerConstant()) {
                        factorsNumerator.put(0, new Constant(((Constant) factorsNumerator.get(0)).getBigIntValue().divide(gcdOfNumerators)));
                        summandsLeft.put(j, SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator));
                    }
                }
                for (int j = 0; j < summandsRight.getBound(); j++) {
                    factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsRight.get(j));
                    factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(j));
                    if (factorsNumerator.get(0).isIntegerConstant()) {
                        factorsNumerator.put(0, new Constant(((Constant) factorsNumerator.get(0)).getBigIntValue().divide(gcdOfNumerators)));
                        summandsRight.put(j, SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator));
                    }
                }

                if (factors.get(i).isPower()) {
                    factors.put(i, SimplifyUtilities.produceDifference(summandsLeft, summandsRight).pow(exponent));
                    factors.add(new Constant(gcdOfNumerators).pow(exponent));
                } else {
                    factors.put(i, SimplifyUtilities.produceDifference(summandsLeft, summandsRight));
                    factors.add(new Constant(gcdOfNumerators));
                }

            }

            if (gcdOfDenominators.compareTo(BigInteger.ONE) > 0) {

                ExpressionCollection factorsNumerator, factorsDenominator;
                for (int j = 0; j < summandsLeft.getBound(); j++) {
                    factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsLeft.get(j));
                    factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(j));
                    if (factorsDenominator.get(0).isIntegerConstant()) {
                        factorsDenominator.put(0, new Constant(((Constant) factorsDenominator.get(0)).getBigIntValue().divide(gcdOfDenominators)));
                        summandsLeft.put(j, SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator));
                    }
                }
                for (int j = 0; j < summandsRight.getBound(); j++) {
                    factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsRight.get(j));
                    factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(j));
                    if (factorsDenominator.get(0).isIntegerConstant()) {
                        factorsDenominator.put(0, new Constant(((Constant) factorsDenominator.get(0)).getBigIntValue().divide(gcdOfDenominators)));
                        summandsRight.put(j, SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator));
                    }
                }

                if (factors.get(i).isPower()) {
                    factors.put(i, SimplifyUtilities.produceDifference(summandsLeft, summandsRight).pow(exponent));
                    factors.add(ONE.div(gcdOfDenominators).pow(exponent));
                } else {
                    factors.put(i, SimplifyUtilities.produceDifference(summandsLeft, summandsRight));
                    factors.add(ONE.div(gcdOfDenominators));
                }

            }

        }

    }

    private static BigInteger getCoefficientInNumerator(Expression expr) {
        if (expr.isQuotient()) {
            ExpressionCollection factors = SimplifyUtilities.getFactors(((BinaryOperation) expr).getLeft());
            if (factors.get(0).isIntegerConstant()) {
                return ((Constant) factors.get(0)).getBigIntValue();
            }
        }
        ExpressionCollection factors = SimplifyUtilities.getFactors(expr);
        if (factors.get(0).isIntegerConstant()) {
            return ((Constant) factors.get(0)).getBigIntValue();
        }
        return BigInteger.ONE;
    }

    private static BigInteger getCoefficientInDenominator(Expression expr) {
        if (expr.isQuotient()) {
            ExpressionCollection factors = SimplifyUtilities.getFactors(((BinaryOperation) expr).getRight());
            if (factors.get(0).isIntegerConstant()) {
                return ((Constant) factors.get(0)).getBigIntValue();
            }
        }
        return BigInteger.ONE;
    }

    /**
     * Hilfsmethode für reduceGCDInQuotient(). Kürzt ganzzahlige Faktoren aus
     * Brüchen, z. B. wird (25*x + 10*y)/(80*u - 35*v) zu (5*x + 2*y)/(16*u -
     * 7*v) vereinfacht. WICHTIG: Zähler oder Nenner dürfen keine Potenzen sein
     * (dies wird aber in reduceGCDInFactorsInNumeratorAndDenominator()
     * ausgeschlossen).
     */
    private static Expression[] reduceGCDInNumeratorAndDenominator(Expression numerator, Expression denominator) throws EvaluationException {

        ExpressionCollection summandsLeftInNumerator = SimplifyUtilities.getSummandsLeftInExpression(numerator);
        ExpressionCollection summandsRightInNumerator = SimplifyUtilities.getSummandsRightInExpression(numerator);
        ExpressionCollection summandsLeftInDenominator = SimplifyUtilities.getSummandsLeftInExpression(denominator);
        ExpressionCollection summandsRightInDenominator = SimplifyUtilities.getSummandsRightInExpression(denominator);

        ExpressionCollection numerators = new ExpressionCollection();
        BigInteger gcdOfAllCoefficients = BigInteger.ZERO;
        boolean containsConstant;

        // Zähler absuchen (Minuend)
        for (int i = 0; i < summandsLeftInNumerator.getBound(); i++) {

            containsConstant = false;
            numerators.clear();
            numerators = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsLeftInNumerator.get(i));
            for (int j = 0; j < numerators.getBound(); j++) {

                if (numerators.get(j).isIntegerConstant()) {
                    containsConstant = true;
                    if (gcdOfAllCoefficients.equals(BigInteger.ZERO)) {
                        gcdOfAllCoefficients = ((Constant) numerators.get(j)).getBigIntValue();
                        break;
                    } else {
                        gcdOfAllCoefficients = gcdOfAllCoefficients.gcd(((Constant) numerators.get(j)).getBigIntValue());
                        break;
                    }
                }
                if (gcdOfAllCoefficients.equals(BigInteger.ONE)) {
                    // Es kann dann nichts gekürzt werden.
                    Expression[] result = new Expression[2];
                    result[0] = numerator;
                    result[1] = denominator;
                    return result;
                }

            }
            if (!containsConstant) {
                Expression[] result = new Expression[2];
                result[0] = numerator;
                result[1] = denominator;
                return result;
            }

        }

        // Zähler absuchen (Subtrahend)
        for (int i = 0; i < summandsRightInNumerator.getBound(); i++) {

            containsConstant = false;
            numerators.clear();
            numerators = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsRightInNumerator.get(i));
            for (int j = 0; j < numerators.getBound(); j++) {

                if (numerators.get(j).isIntegerConstant()) {
                    containsConstant = true;
                    if (gcdOfAllCoefficients.equals(BigInteger.ZERO)) {
                        gcdOfAllCoefficients = ((Constant) numerators.get(j)).getBigIntValue();
                        break;
                    } else {
                        gcdOfAllCoefficients = gcdOfAllCoefficients.gcd(((Constant) numerators.get(j)).getBigIntValue());
                        break;
                    }
                }
                if (gcdOfAllCoefficients.equals(BigInteger.ONE)) {
                    // Es kann dann nichts gekürzt werden.
                    Expression[] result = new Expression[2];
                    result[0] = numerator;
                    result[1] = denominator;
                    return result;
                }

            }
            if (!containsConstant) {
                Expression[] result = new Expression[2];
                result[0] = numerator;
                result[1] = denominator;
                return result;
            }

        }

        // Nenner absuchen (Minuend)
        for (int i = 0; i < summandsLeftInDenominator.getBound(); i++) {

            containsConstant = false;
            numerators.clear();
            numerators = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsLeftInDenominator.get(i));
            for (int j = 0; j < numerators.getBound(); j++) {

                if (numerators.get(j).isIntegerConstant()) {
                    containsConstant = true;
                    if (gcdOfAllCoefficients.equals(BigInteger.ZERO)) {
                        gcdOfAllCoefficients = ((Constant) numerators.get(j)).getBigIntValue();
                        break;
                    } else {
                        gcdOfAllCoefficients = gcdOfAllCoefficients.gcd(((Constant) numerators.get(j)).getBigIntValue());
                        break;
                    }
                }
                if (gcdOfAllCoefficients.equals(BigInteger.ONE)) {
                    // Es kann dann nichts gekürzt werden.
                    Expression[] result = new Expression[2];
                    result[0] = numerator;
                    result[1] = denominator;
                    return result;
                }

            }
            if (!containsConstant) {
                Expression[] result = new Expression[2];
                result[0] = numerator;
                result[1] = denominator;
                return result;
            }

        }

        // Nenner absuchen (Subtrahend)
        for (int i = 0; i < summandsRightInDenominator.getBound(); i++) {

            containsConstant = false;
            numerators.clear();
            numerators = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsRightInDenominator.get(i));
            for (int j = 0; j < numerators.getBound(); j++) {

                if (numerators.get(j).isIntegerConstant()) {
                    containsConstant = true;
                    if (gcdOfAllCoefficients.equals(BigInteger.ZERO)) {
                        gcdOfAllCoefficients = ((Constant) numerators.get(j)).getBigIntValue();
                        break;
                    } else {
                        gcdOfAllCoefficients = gcdOfAllCoefficients.gcd(((Constant) numerators.get(j)).getBigIntValue());
                        break;
                    }
                }
                if (gcdOfAllCoefficients.equals(BigInteger.ONE)) {
                    // Es kann dann nichts gekürzt werden.
                    Expression[] result = new Expression[2];
                    result[0] = numerator;
                    result[1] = denominator;
                    return result;
                }

            }
            if (!containsConstant) {
                Expression[] result = new Expression[2];
                result[0] = numerator;
                result[1] = denominator;
                return result;
            }

        }

        if (gcdOfAllCoefficients.equals(BigInteger.ONE)) {
            // Es kann dann nichts gekürzt werden.
            Expression[] result = new Expression[2];
            result[0] = numerator;
            result[1] = denominator;
            return result;
        }

        /*
         Hier angelangt kann a nicht 1 sein! Jetzt: Alle Summanden im Zähler 
         und Nenner durch a kürzen. Die einzelnen Summanden müssen noch 
         vereinfacht werden.
         */
        ExpressionCollection denominators = new ExpressionCollection();
        for (int i = 0; i < summandsLeftInNumerator.getBound(); i++) {

            numerators.clear();
            numerators = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsLeftInNumerator.get(i));
            denominators = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeftInNumerator.get(i));
            for (int j = 0; j < numerators.getBound(); j++) {

                /*
                 Falls Konstanten als Faktoren auftauchen, dann (NUR EINEN)
                 Faktor durch a teilen und neuen Summanden als Produkt der so
                 entstandenen Faktoren bilden.
                 */
                if (numerators.get(j).isIntegerConstant()) {
                    numerators.put(j, new Constant(((Constant) numerators.get(j)).getBigIntValue().divide(gcdOfAllCoefficients)));
                    summandsLeftInNumerator.put(i, SimplifyUtilities.produceQuotient(numerators, denominators));
                    break;
                }

            }

        }
        for (int i = 0; i < summandsRightInNumerator.getBound(); i++) {

            numerators.clear();
            denominators.clear();
            numerators = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsRightInNumerator.get(i));
            denominators = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRightInNumerator.get(i));
            for (int j = 0; j < numerators.getBound(); j++) {

                /*
                 Falls Konstanten als Faktoren auftauchen, dann (NUR EINEN)
                 Faktor durch a teilen und neuen Summanden als Produkt der so
                 entstandenen Faktoren bilden.
                 */
                if (numerators.get(j).isIntegerConstant()) {
                    numerators.put(j, new Constant(((Constant) numerators.get(j)).getBigIntValue().divide(gcdOfAllCoefficients)));
                    summandsRightInNumerator.put(i, SimplifyUtilities.produceQuotient(numerators, denominators));
                    break;
                }

            }

        }
        for (int i = 0; i < summandsLeftInDenominator.getBound(); i++) {

            numerators.clear();
            denominators.clear();
            numerators = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsLeftInDenominator.get(i));
            denominators = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeftInDenominator.get(i));
            for (int j = 0; j < numerators.getBound(); j++) {

                /*
                 Falls Konstanten als Faktoren auftauchen, dann (NUR EINEN)
                 Faktor durch a teilen und neuen Summanden als Produkt der so
                 entstandenen Faktoren bilden.
                 */
                if (numerators.get(j).isIntegerConstant()) {
                    numerators.put(j, new Constant(((Constant) numerators.get(j)).getBigIntValue().divide(gcdOfAllCoefficients)));
                    summandsLeftInDenominator.put(i, SimplifyUtilities.produceQuotient(numerators, denominators));
                    break;
                }

            }

        }
        for (int i = 0; i < summandsRightInDenominator.getBound(); i++) {

            numerators.clear();
            denominators.clear();
            numerators = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsRightInDenominator.get(i));
            denominators = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRightInDenominator.get(i));
            for (int j = 0; j < numerators.getBound(); j++) {

                /*
                 Falls Konstanten als Faktoren auftauchen, dann (NUR EINEN)
                 Faktor durch a teilen und neuen Summanden als Produkt der so
                 entstandenen Faktoren bilden.
                 */
                if (numerators.get(j).isIntegerConstant()) {
                    numerators.put(j, new Constant(((Constant) numerators.get(j)).getBigIntValue().divide(gcdOfAllCoefficients)));
                    summandsRightInDenominator.put(i, SimplifyUtilities.produceQuotient(numerators, denominators));
                    break;
                }

            }

        }

        Expression[] reducedNumeratorAndDenominator = new Expression[2];
        reducedNumeratorAndDenominator[0] = SimplifyUtilities.produceDifference(summandsLeftInNumerator, summandsRightInNumerator);
        reducedNumeratorAndDenominator[1] = SimplifyUtilities.produceDifference(summandsLeftInDenominator, summandsRightInDenominator);
        return reducedNumeratorAndDenominator;

    }

    /**
     * Macht aus Summen/Differenzen von Brüchen einen einzigen. Beispiel: a/b +
     * c/5 = (5*a + b*c)/(5*b) oder x/a+y/a^2 = (x*a + y)/a^2.
     *
     * @throws EvaluationException
     */
    public static Expression bringExpressionToCommonDenominator(Expression expr) throws EvaluationException {

        if (expr.isSum() || expr.isDifference()) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, bringExpressionToCommonDenominator(summandsLeft.get(i)));
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, bringExpressionToCommonDenominator(summandsRight.get(i)));
            }
            expr = SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (expr.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(expr);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, bringExpressionToCommonDenominator(factors.get(i)));
            }
            expr = SimplifyUtilities.produceProduct(factors);

        } else if (expr.isQuotient()) {

            expr = bringExpressionToCommonDenominator(((BinaryOperation) expr).getLeft()).div(bringExpressionToCommonDenominator(((BinaryOperation) expr).getRight()));
            if (expr.isQuotient()) {
                return expr.orderDifferencesAndQuotients().orderSumsAndProducts();
            }
            return expr;

        } else if (expr.isPower()) {

            expr = bringExpressionToCommonDenominator(((BinaryOperation) expr).getLeft()).pow(bringExpressionToCommonDenominator(((BinaryOperation) expr).getRight()));
            // Dann wird (a*b* ....)^n zu a^n*b^n*... für zulässige Exponenten n vereinfacht.
            expr = expr.simplifyPullApartPowers();
            if (expr.isQuotient()) {
                return expr.orderDifferencesAndQuotients().orderSumsAndProducts();
            }
            return expr;

        }

        // Ab hier kommt das eigentliche "auf einen Nenner bringen". 
        if (expr.isNotSum() && expr.isNotDifference()) {
            return expr;
        }

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);
        ExpressionCollection commonDenominators;
        ExpressionCollection additionalDenominators;
        Expression baseOfFactorInCommonDenominators, baseOfFactorInCurrentDenominators;
        BigInteger exponentOfFactorInCommonDenominators, exponentOfFactorInCurrentDenominators;

        /* 
        Etwas Vorarbeit: Falls im Nenner rationale Polynome in genau einer Veränderlichen auftauchen, 
        so sollen diese faktorisiert werden, wenn möglich.
         */
        Expression denominator;
        ExpressionCollection factorsDenominator;

        // Im Minuenden Nenner faktorisieren.
        for (int i = 0; i < summandsLeft.getBound(); i++) {
            if (summandsLeft.get(i).isQuotient()) {
                factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(i));
                for (int j = 0; j < factorsDenominator.getBound(); j++) {
                    factorsDenominator.put(j, decomposeIfIsRationalPolynomialInOneVariable(factorsDenominator.get(j)));
                }
                denominator = SimplifyUtilities.produceProduct(factorsDenominator);
                summandsLeft.put(i, ((BinaryOperation) summandsLeft.get(i)).getLeft().div(denominator));
            }
        }
        // Im Subtrahenden Nenner faktorisieren.
        for (int i = 0; i < summandsRight.getBound(); i++) {
            if (summandsRight.get(i).isQuotient()) {
                factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(i));
                for (int j = 0; j < factorsDenominator.getBound(); j++) {
                    factorsDenominator.put(j, decomposeIfIsRationalPolynomialInOneVariable(factorsDenominator.get(j)));
                }
                denominator = SimplifyUtilities.produceProduct(factorsDenominator);
                summandsRight.put(i, ((BinaryOperation) summandsRight.get(i)).getLeft().div(denominator));
            }
        }

        // Hauptnenner bilden.
        boolean factorOccursInCommonDenominators;
        commonDenominators = SimplifyUtilities.collectFactorsByPowers(SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(0)));

        for (int i = 1; i < summandsLeft.getBound(); i++) {

            additionalDenominators = SimplifyUtilities.difference(SimplifyUtilities.collectFactorsByPowers(SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(i))), commonDenominators);

            for (int j = 0; j < additionalDenominators.getBound(); j++) {

                factorOccursInCommonDenominators = false;

                for (int k = 0; k < commonDenominators.getBound(); k++) {

                    if (commonDenominators.get(k).isPower()
                            && ((BinaryOperation) commonDenominators.get(k)).getRight().isIntegerConstant()
                            && ((BinaryOperation) commonDenominators.get(k)).getRight().isNonNegative()) {
                        baseOfFactorInCommonDenominators = ((BinaryOperation) commonDenominators.get(k)).getLeft();
                        exponentOfFactorInCommonDenominators = ((Constant) ((BinaryOperation) commonDenominators.get(k)).getRight()).getBigIntValue();
                    } else {
                        baseOfFactorInCommonDenominators = commonDenominators.get(k);
                        exponentOfFactorInCommonDenominators = BigInteger.ONE;
                    }
                    if (additionalDenominators.get(j).isPower()
                            && ((BinaryOperation) additionalDenominators.get(j)).getRight().isIntegerConstant()
                            && ((BinaryOperation) additionalDenominators.get(j)).getRight().isNonNegative()) {
                        baseOfFactorInCurrentDenominators = ((BinaryOperation) additionalDenominators.get(j)).getLeft();
                        exponentOfFactorInCurrentDenominators = ((Constant) ((BinaryOperation) additionalDenominators.get(j)).getRight()).getBigIntValue();
                    } else {
                        baseOfFactorInCurrentDenominators = additionalDenominators.get(j);
                        exponentOfFactorInCurrentDenominators = BigInteger.ONE;
                    }

                    /*
                     Jetzt: Entweder Exponenten eines bereits vorhandenen
                     Faktors (in commonDenominators) erhöhen oder den neuen
                     Faktor hinzufügen.
                     */
                    if (baseOfFactorInCommonDenominators.equivalent(baseOfFactorInCurrentDenominators)) {
                        if (exponentOfFactorInCommonDenominators.max(exponentOfFactorInCurrentDenominators).compareTo(BigInteger.ONE) == 0) {
                            commonDenominators.put(k, baseOfFactorInCommonDenominators);
                        } else {
                            commonDenominators.put(k, baseOfFactorInCommonDenominators.pow(exponentOfFactorInCommonDenominators.max(exponentOfFactorInCurrentDenominators)));
                        }
                        factorOccursInCommonDenominators = true;
                    }

                }
                if (!factorOccursInCommonDenominators) {
                    commonDenominators.add(additionalDenominators.get(j));
                }

            }

        }

        for (int i = 0; i < summandsRight.getBound(); i++) {

            additionalDenominators = SimplifyUtilities.difference(SimplifyUtilities.collectFactorsByPowers(SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(i))), commonDenominators);

            for (int j = 0; j < additionalDenominators.getBound(); j++) {

                factorOccursInCommonDenominators = false;

                for (int k = 0; k < commonDenominators.getBound(); k++) {

                    if (commonDenominators.get(k).isPower()
                            && ((BinaryOperation) commonDenominators.get(k)).getRight().isIntegerConstant()
                            && ((BinaryOperation) commonDenominators.get(k)).getRight().isNonNegative()) {
                        baseOfFactorInCommonDenominators = ((BinaryOperation) commonDenominators.get(k)).getLeft();
                        exponentOfFactorInCommonDenominators = ((Constant) ((BinaryOperation) commonDenominators.get(k)).getRight()).getBigIntValue();
                    } else {
                        baseOfFactorInCommonDenominators = commonDenominators.get(k);
                        exponentOfFactorInCommonDenominators = BigInteger.ONE;
                    }
                    if (additionalDenominators.get(j).isPower()
                            && ((BinaryOperation) additionalDenominators.get(j)).getRight().isIntegerConstant()
                            && ((BinaryOperation) additionalDenominators.get(j)).getRight().isNonNegative()) {
                        baseOfFactorInCurrentDenominators = ((BinaryOperation) additionalDenominators.get(j)).getLeft();
                        exponentOfFactorInCurrentDenominators = ((Constant) ((BinaryOperation) additionalDenominators.get(j)).getRight()).getBigIntValue();
                    } else {
                        baseOfFactorInCurrentDenominators = additionalDenominators.get(j);
                        exponentOfFactorInCurrentDenominators = BigInteger.ONE;
                    }

                    /*
                     Jetzt: Entweder Exponenten eines bereits vorhandenen
                     Faktors (in commonDenominators) erhöhen oder den neuen
                     Faktor hinzufügen.
                     */
                    if (baseOfFactorInCommonDenominators.equivalent(baseOfFactorInCurrentDenominators)) {
                        if (exponentOfFactorInCommonDenominators.max(exponentOfFactorInCurrentDenominators).compareTo(BigInteger.ONE) == 0) {
                            commonDenominators.put(k, baseOfFactorInCommonDenominators);
                        } else {
                            commonDenominators.put(k, baseOfFactorInCommonDenominators.pow(exponentOfFactorInCommonDenominators.max(exponentOfFactorInCurrentDenominators)));
                        }
                        factorOccursInCommonDenominators = true;
                    }

                }
                if (!factorOccursInCommonDenominators) {
                    commonDenominators.add(additionalDenominators.get(j));
                }

            }

        }

        if (commonDenominators.isEmpty()) {
            // Dann gab es in der Summe/Differenz keine Brüche!
            return expr;
        }

        ExpressionCollection complementFactorsForEachSummand = new ExpressionCollection();
        ExpressionCollection commonDenominatorsCopy;

        /*
         Jetzt: Alle Zähler mit fehlenden Faktoren multiplizieren und in
         summandsLeft bzw. summandsRight abspeichern.
         */
        boolean factorOccursInCurrentDenominators;
        int numberOfFactorsInCommonDenominator = commonDenominators.getBound();
        int numberOfFactorsInCurrentDenominator;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            commonDenominatorsCopy = commonDenominators.copy();
            complementFactorsForEachSummand.clear();
            additionalDenominators = SimplifyUtilities.collectFactorsByPowers(SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(i)));
            numberOfFactorsInCurrentDenominator = additionalDenominators.getBound();
            for (int j = 0; j < numberOfFactorsInCommonDenominator; j++) {

                if (commonDenominatorsCopy.get(j) == null) {
                    continue;
                }
                if (commonDenominatorsCopy.get(j).isPower()
                        && ((BinaryOperation) commonDenominatorsCopy.get(j)).getRight().isIntegerConstant()
                        && ((BinaryOperation) commonDenominatorsCopy.get(j)).getRight().isNonNegative()) {
                    baseOfFactorInCommonDenominators = ((BinaryOperation) commonDenominatorsCopy.get(j)).getLeft();
                    exponentOfFactorInCommonDenominators = ((Constant) ((BinaryOperation) commonDenominatorsCopy.get(j)).getRight()).getBigIntValue();
                } else {
                    baseOfFactorInCommonDenominators = commonDenominatorsCopy.get(j);
                    exponentOfFactorInCommonDenominators = BigInteger.ONE;
                }

                factorOccursInCurrentDenominators = false;

                for (int k = 0; k < numberOfFactorsInCurrentDenominator; k++) {
                    if (additionalDenominators.get(k) == null) {
                        continue;
                    }

                    if (additionalDenominators.get(k).isPower()
                            && ((BinaryOperation) additionalDenominators.get(k)).getRight().isIntegerConstant()
                            && ((BinaryOperation) additionalDenominators.get(k)).getRight().isNonNegative()) {
                        baseOfFactorInCurrentDenominators = ((BinaryOperation) additionalDenominators.get(k)).getLeft();
                        exponentOfFactorInCurrentDenominators = ((Constant) ((BinaryOperation) additionalDenominators.get(k)).getRight()).getBigIntValue();
                    } else {
                        baseOfFactorInCurrentDenominators = additionalDenominators.get(k);
                        exponentOfFactorInCurrentDenominators = BigInteger.ONE;
                    }

                    if (baseOfFactorInCommonDenominators.equivalent(baseOfFactorInCurrentDenominators)) {
                        complementFactorsForEachSummand.add(baseOfFactorInCurrentDenominators.pow(exponentOfFactorInCommonDenominators.subtract(exponentOfFactorInCurrentDenominators)));
                        commonDenominatorsCopy.remove(j);
                        additionalDenominators.remove(k);
                        factorOccursInCurrentDenominators = true;
                    }

                }

                if (!factorOccursInCurrentDenominators) {
                    complementFactorsForEachSummand.add(commonDenominators.get(j));
                }

            }

            if (!complementFactorsForEachSummand.isEmpty()) {
                summandsLeft.put(i, SimplifyUtilities.produceProduct(complementFactorsForEachSummand).mult(SimplifyUtilities.produceProduct(
                        SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsLeft.get(i)))).simplify());
            }

        }

        for (int i = 0; i < summandsRight.getBound(); i++) {

            commonDenominatorsCopy = commonDenominators.copy();
            complementFactorsForEachSummand.clear();
            additionalDenominators = SimplifyUtilities.collectFactorsByPowers(SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(i)));
            numberOfFactorsInCurrentDenominator = additionalDenominators.getBound();
            for (int j = 0; j < numberOfFactorsInCommonDenominator; j++) {

                if (commonDenominatorsCopy.get(j) == null) {
                    continue;
                }
                if (commonDenominatorsCopy.get(j).isPower()
                        && ((BinaryOperation) commonDenominatorsCopy.get(j)).getRight().isIntegerConstant()
                        && ((BinaryOperation) commonDenominatorsCopy.get(j)).getRight().isNonNegative()) {
                    baseOfFactorInCommonDenominators = ((BinaryOperation) commonDenominatorsCopy.get(j)).getLeft();
                    exponentOfFactorInCommonDenominators = ((Constant) ((BinaryOperation) commonDenominatorsCopy.get(j)).getRight()).getBigIntValue();
                } else {
                    baseOfFactorInCommonDenominators = commonDenominatorsCopy.get(j);
                    exponentOfFactorInCommonDenominators = BigInteger.ONE;
                }

                factorOccursInCurrentDenominators = false;

                for (int k = 0; k < numberOfFactorsInCurrentDenominator; k++) {
                    if (additionalDenominators.get(k) == null) {
                        continue;
                    }

                    if (additionalDenominators.get(k).isPower()
                            && ((BinaryOperation) additionalDenominators.get(k)).getRight().isIntegerConstant()
                            && ((BinaryOperation) additionalDenominators.get(k)).getRight().isNonNegative()) {
                        baseOfFactorInCurrentDenominators = ((BinaryOperation) additionalDenominators.get(k)).getLeft();
                        exponentOfFactorInCurrentDenominators = ((Constant) ((BinaryOperation) additionalDenominators.get(k)).getRight()).getBigIntValue();
                    } else {
                        baseOfFactorInCurrentDenominators = additionalDenominators.get(k);
                        exponentOfFactorInCurrentDenominators = BigInteger.ONE;
                    }

                    if (baseOfFactorInCommonDenominators.equivalent(baseOfFactorInCurrentDenominators)) {
                        complementFactorsForEachSummand.add(baseOfFactorInCurrentDenominators.pow(exponentOfFactorInCommonDenominators.subtract(exponentOfFactorInCurrentDenominators)));
                        commonDenominatorsCopy.remove(j);
                        additionalDenominators.remove(k);
                        factorOccursInCurrentDenominators = true;
                    }

                }

                if (!factorOccursInCurrentDenominators) {
                    complementFactorsForEachSummand.add(commonDenominators.get(j));
                }

            }

            if (!complementFactorsForEachSummand.isEmpty()) {
                summandsRight.put(i, SimplifyUtilities.produceProduct(complementFactorsForEachSummand).mult(SimplifyUtilities.produceProduct(
                        SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsRight.get(i)))).simplify());
            }

        }

        return SimplifyUtilities.produceDifference(summandsLeft, summandsRight).div(SimplifyUtilities.produceProduct(commonDenominators));

    }

    /**
     * Hilfsmethode. Es wird, wenn möglich, der faktorisierte Ausdruck
     * zurückgegeben, wenn der Ausdruck ein Polynom in genau einer
     * Veränderlichen ist.
     */
    private static Expression decomposeIfIsRationalPolynomialInOneVariable(Expression expr) {

        Set<String> vars = expr.getContainedIndeterminates();
        if (vars.size() != 1) {
            return expr;
        }

        String var = null;
        for (String v : vars) {
            var = v;
        }

        if (SimplifyPolynomialUtils.isPolynomial(expr, var)) {
            try {
                return SimplifyPolynomialUtils.decomposeRationalPolynomialInIrreducibleFactors(expr, var);
            } catch (EvaluationException e) {
            }
        }

        return expr;

    }

    /**
     * Prüft, ob Faktoren im Zähler und im Nenner zu einer Konstante gekürzt
     * werden können.
     *
     * @throws EvaluationException
     */
    public static void reduceFactorsInNumeratorAndFactorInDenominatorToConstant(ExpressionCollection factorsNumerator, ExpressionCollection factorsDenominator) throws EvaluationException {

        Expression factorNumerator, factorDenominator;
        Expression[] reducedFactor;

        for (int i = 0; i < factorsNumerator.getBound(); i++) {

            if (factorsNumerator.get(i) == null) {
                continue;
            }

            factorNumerator = factorsNumerator.get(i);
            for (int j = 0; j < factorsDenominator.getBound(); j++) {

                if (factorsDenominator.get(j) == null) {
                    continue;
                }

                factorDenominator = factorsDenominator.get(j);
                if ((factorNumerator.isSum() || factorNumerator.isDifference())
                        && (factorDenominator.isSum() || factorDenominator.isDifference())) {

                    reducedFactor = reduceNumeratorAndDenominatorToConstant(factorNumerator, factorDenominator);
                    if (reducedFactor.length > 0) {
                        // Es konnte mindestens ein Faktor im Zähler gegen einen Faktor im Nenner gekürzt werden.
                        if (!reducedFactor[1].equals(ONE)) {
                            factorsNumerator.put(i, reducedFactor[0]);
                            factorsDenominator.put(j, reducedFactor[1]);
                        } else {
                            factorsNumerator.put(i, reducedFactor[0]);
                            factorsDenominator.remove(j);
                        }
                    }

                }
                /*
                 Sonderfall: Zähler und Nenner sind von der Form (a*expr)^k,
                 (b*expr)^k, mit rationalen a, b. Dann zu (a/b)^k kürzen.
                 */
                if (factorNumerator.isPower() && factorDenominator.isPower()
                        && ((BinaryOperation) factorNumerator).getRight().equivalent(((BinaryOperation) factorDenominator).getRight())) {
                    if ((((BinaryOperation) factorNumerator).getLeft().isSum() || ((BinaryOperation) factorNumerator).getLeft().isDifference())
                            && (((BinaryOperation) factorDenominator).getLeft().isSum() || ((BinaryOperation) factorDenominator).getLeft().isDifference())) {

                        reducedFactor = reduceNumeratorAndDenominatorToConstant(((BinaryOperation) factorNumerator).getLeft(), ((BinaryOperation) factorDenominator).getLeft());
                        if (reducedFactor.length > 0) {
                            // Es konnte mindestens ein Faktor im Zähler gegen einen Faktor im Nenner gekürzt werden.
                            if (!reducedFactor[1].equals(ONE)) {
                                factorsNumerator.put(i, reducedFactor[0].pow(((BinaryOperation) factorNumerator).getRight()));
                                factorsDenominator.put(j, reducedFactor[1].pow(((BinaryOperation) factorNumerator).getRight()));
                            } else {
                                factorsNumerator.put(i, reducedFactor[0].pow(((BinaryOperation) factorNumerator).getRight()));
                                factorsDenominator.remove(j);
                            }
                        }

                    }
                }

            }

        }

    }

    /**
     * Hilfsmethode für
     * reduceFactorsInNumeratorAndFactorInDenominatorToConstant(). Falls der
     * Zähler ein konstantes Vielfaches vom Nenner ist, so wird der gekürzte
     * Quotient zurückgegeben. Ansonsten wird ein Array bestehend aus zwei
     * Elementen, numerator und denominator, wieder zurückgegeben. Z.B. (3*a +
     * 7*b)/(12*a + 28*b) = 1/4. Zähler und Nenner werden hier in Form von
     * HashMaps mit Summanden (Minuenden und Subtrahenden) als Einträgen
     * angegeben.
     *
     * @throws EvaluationException
     */
    private static Expression[] reduceNumeratorAndDenominatorToConstant(Expression numerator,
            Expression denominator) throws EvaluationException {

        ExpressionCollection summandsLeftInNumerator = SimplifyUtilities.getSummandsLeftInExpression(numerator);
        ExpressionCollection summandsRightInNumerator = SimplifyUtilities.getSummandsRightInExpression(numerator);
        ExpressionCollection summandsLeftInDenominator = SimplifyUtilities.getSummandsLeftInExpression(denominator);
        ExpressionCollection summandsRightInDenominator = SimplifyUtilities.getSummandsRightInExpression(denominator);

        if ((summandsLeftInNumerator.getBound() != summandsLeftInDenominator.getBound()
                || summandsRightInNumerator.getBound() != summandsRightInDenominator.getBound())
                && (summandsLeftInNumerator.getBound() != summandsRightInDenominator.getBound()
                || summandsRightInNumerator.getBound() != summandsLeftInDenominator.getBound())) {
            // Dann gibt es keine Chance, dass Zähler und Nenner zu einer Konstante gekürzt werden können.
            Expression[] result = new Expression[0];
            return result;
        }

        Expression coefficientInNumeratorForTesting, coefficientInDenominatorForTesting;
        Expression summandInNumeratorForTesting, summandInDenominatorForTesting;
        ExpressionCollection summandsLeftInNumeratorMultiples;
        ExpressionCollection summandsRightInNumeratorMultiples;
        ExpressionCollection summandsLeftInDenominatorMultiples;
        ExpressionCollection summandsRightInDenominatorMultiples;

        // In summandsLeftInNumerator nach passendem Testsummanden suchen.
        for (Expression summandNumerator : summandsLeftInNumerator) {

            if (summandNumerator.isConstant()) {
                continue;
            }

            summandInNumeratorForTesting = SimplifyUtilities.produceQuotient(
                    SimplifyUtilities.getNonConstantFactorsOfNumeratorInExpression(summandNumerator),
                    SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(summandNumerator));

            for (Expression summandDenominator : summandsLeftInDenominator) {
                if (summandDenominator.isConstant()) {
                    continue;
                }

                summandInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                        SimplifyUtilities.getNonConstantFactorsOfNumeratorInExpression(summandDenominator),
                        SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(summandDenominator));

                // Passende Testsummanden im Zähler und im Nenner gefunden.
                if (summandInNumeratorForTesting.equivalent(summandInDenominatorForTesting)) {

                    coefficientInNumeratorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfNumeratorInExpression(summandNumerator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandNumerator));
                    coefficientInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfNumeratorInExpression(summandDenominator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandDenominator));

                    summandsLeftInNumeratorMultiples = summandsLeftInNumerator.copy();
                    summandsLeftInNumeratorMultiples.multiplyWithExpression(coefficientInDenominatorForTesting);
                    summandsLeftInNumeratorMultiples = summandsLeftInNumeratorMultiples.simplify();

                    summandsRightInNumeratorMultiples = summandsRightInNumerator.copy();
                    summandsRightInNumeratorMultiples.multiplyWithExpression(coefficientInDenominatorForTesting);
                    summandsRightInNumeratorMultiples = summandsRightInNumeratorMultiples.simplify();

                    summandsLeftInDenominatorMultiples = summandsLeftInDenominator.copy();
                    summandsLeftInDenominatorMultiples.multiplyWithExpression(coefficientInNumeratorForTesting);
                    summandsLeftInDenominatorMultiples = summandsLeftInDenominatorMultiples.simplify();

                    summandsRightInDenominatorMultiples = summandsRightInDenominator.copy();
                    summandsRightInDenominatorMultiples.multiplyWithExpression(coefficientInNumeratorForTesting);
                    summandsRightInDenominatorMultiples = summandsRightInDenominatorMultiples.simplify();

                    if (summandsLeftInNumeratorMultiples.equivalentInTerms(summandsLeftInDenominatorMultiples)
                            && summandsRightInNumeratorMultiples.equivalentInTerms(summandsRightInDenominatorMultiples)) {
                        // Dann ist der gekürzte Bruch = coefficientInNumeratorForTesting/coefficientInDenominatorForTesting.
                        Expression[] result = new Expression[2];
                        result[0] = coefficientInNumeratorForTesting;
                        result[1] = coefficientInDenominatorForTesting;
                        return result;
                    }

                }

            }

            for (Expression summandDenominator : summandsRightInDenominator) {
                if (summandDenominator.isConstant()) {
                    continue;
                }

                summandInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                        SimplifyUtilities.getNonConstantFactorsOfNumeratorInExpression(summandDenominator),
                        SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(summandDenominator));

                // Passende Testsummanden im Zähler und im Nenner gefunden.
                if (summandInNumeratorForTesting.equivalent(summandInDenominatorForTesting)) {

                    coefficientInNumeratorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfNumeratorInExpression(summandNumerator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandNumerator));
                    coefficientInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfNumeratorInExpression(summandDenominator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandDenominator));

                    summandsLeftInNumeratorMultiples = summandsLeftInNumerator.copy();
                    summandsLeftInNumeratorMultiples.multiplyWithExpression(coefficientInDenominatorForTesting);
                    summandsLeftInNumeratorMultiples = summandsLeftInNumeratorMultiples.simplify();

                    summandsRightInNumeratorMultiples = summandsRightInNumerator.copy();
                    summandsRightInNumeratorMultiples.multiplyWithExpression(coefficientInDenominatorForTesting);
                    summandsRightInNumeratorMultiples = summandsRightInNumeratorMultiples.simplify();

                    summandsLeftInDenominatorMultiples = summandsLeftInDenominator.copy();
                    summandsLeftInDenominatorMultiples.multiplyWithExpression(coefficientInNumeratorForTesting);
                    summandsLeftInDenominatorMultiples = summandsLeftInDenominatorMultiples.simplify();

                    summandsRightInDenominatorMultiples = summandsRightInDenominator.copy();
                    summandsRightInDenominatorMultiples.multiplyWithExpression(coefficientInNumeratorForTesting);
                    summandsRightInDenominatorMultiples = summandsRightInDenominatorMultiples.simplify();

                    if (summandsLeftInNumeratorMultiples.equivalentInTerms(summandsRightInDenominatorMultiples)
                            && summandsRightInNumeratorMultiples.equivalentInTerms(summandsLeftInDenominatorMultiples)) {
                        // Dann ist der gekürzte Bruch = coefficientInNumeratorForTesting/coefficientInDenominatorForTesting.
                        Expression[] result = new Expression[2];
                        result[0] = MINUS_ONE.mult(coefficientInNumeratorForTesting);
                        result[1] = coefficientInDenominatorForTesting;
                        return result;
                    }

                }

            }

        }

        // In summandsRightInNumerator nach passendem Testsummanden suchen.
        for (Expression summandNumerator : summandsRightInNumerator) {

            if (summandNumerator.isConstant()) {
                continue;
            }

            summandInNumeratorForTesting = SimplifyUtilities.produceQuotient(
                    SimplifyUtilities.getNonConstantFactorsOfNumeratorInExpression(summandNumerator),
                    SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(summandNumerator));

            for (Expression summandDenominator : summandsLeftInDenominator) {
                if (summandDenominator.isConstant()) {
                    continue;
                }

                summandInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                        SimplifyUtilities.getNonConstantFactorsOfNumeratorInExpression(summandDenominator),
                        SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(summandDenominator));

                // Passende Testsummanden im Zähler und im Nenner gefunden.
                if (summandInNumeratorForTesting.equivalent(summandInDenominatorForTesting)) {

                    coefficientInNumeratorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfNumeratorInExpression(summandNumerator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandNumerator));
                    coefficientInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfNumeratorInExpression(summandDenominator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandDenominator));

                    summandsLeftInNumeratorMultiples = summandsLeftInNumerator.copy();
                    summandsLeftInNumeratorMultiples.multiplyWithExpression(coefficientInDenominatorForTesting);
                    summandsLeftInNumeratorMultiples = summandsLeftInNumeratorMultiples.simplify();

                    summandsRightInNumeratorMultiples = summandsRightInNumerator.copy();
                    summandsRightInNumeratorMultiples.multiplyWithExpression(coefficientInDenominatorForTesting);
                    summandsRightInNumeratorMultiples = summandsRightInNumeratorMultiples.simplify();

                    summandsLeftInDenominatorMultiples = summandsLeftInDenominator.copy();
                    summandsLeftInDenominatorMultiples.multiplyWithExpression(coefficientInNumeratorForTesting);
                    summandsLeftInDenominatorMultiples = summandsLeftInDenominatorMultiples.simplify();

                    summandsRightInDenominatorMultiples = summandsRightInDenominator.copy();
                    summandsRightInDenominatorMultiples.multiplyWithExpression(coefficientInNumeratorForTesting);
                    summandsRightInDenominatorMultiples = summandsRightInDenominatorMultiples.simplify();

                    if (summandsRightInNumeratorMultiples.equivalentInTerms(summandsLeftInDenominatorMultiples)
                            && summandsLeftInNumeratorMultiples.equivalentInTerms(summandsRightInDenominatorMultiples)) {
                        // Dann ist der gekürzte Bruch = coefficientInNumeratorForTesting/coefficientInDenominatorForTesting.
                        Expression[] result = new Expression[2];
                        result[0] = MINUS_ONE.mult(coefficientInNumeratorForTesting);
                        result[1] = coefficientInDenominatorForTesting;
                        return result;
                    }

                }

            }

            for (Expression summandDenominator : summandsRightInDenominator) {
                if (summandDenominator.isConstant()) {
                    continue;
                }

                summandInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                        SimplifyUtilities.getNonConstantFactorsOfNumeratorInExpression(summandDenominator),
                        SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(summandDenominator));

                // Passende Testsummanden im Zähler und im Nenner gefunden.
                if (summandInNumeratorForTesting.equivalent(summandInDenominatorForTesting)) {

                    coefficientInNumeratorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfNumeratorInExpression(summandNumerator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandNumerator));
                    coefficientInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfNumeratorInExpression(summandDenominator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandDenominator));

                    summandsLeftInNumeratorMultiples = summandsLeftInNumerator.copy();
                    summandsLeftInNumeratorMultiples.multiplyWithExpression(coefficientInDenominatorForTesting);
                    summandsLeftInNumeratorMultiples = summandsLeftInNumeratorMultiples.simplify();

                    summandsRightInNumeratorMultiples = summandsRightInNumerator.copy();
                    summandsRightInNumeratorMultiples.multiplyWithExpression(coefficientInDenominatorForTesting);
                    summandsRightInNumeratorMultiples = summandsRightInNumeratorMultiples.simplify();

                    summandsLeftInDenominatorMultiples = summandsLeftInDenominator.copy();
                    summandsLeftInDenominatorMultiples.multiplyWithExpression(coefficientInNumeratorForTesting);
                    summandsLeftInDenominatorMultiples = summandsLeftInDenominatorMultiples.simplify();

                    summandsRightInDenominatorMultiples = summandsRightInDenominator.copy();
                    summandsRightInDenominatorMultiples.multiplyWithExpression(coefficientInNumeratorForTesting);
                    summandsRightInDenominatorMultiples = summandsRightInDenominatorMultiples.simplify();

                    if (summandsLeftInNumeratorMultiples.equivalentInTerms(summandsLeftInDenominatorMultiples)
                            && summandsRightInNumeratorMultiples.equivalentInTerms(summandsRightInDenominatorMultiples)) {
                        // Dann ist der gekürzte Bruch = coefficientInNumeratorForTesting/coefficientInDenominatorForTesting.
                        Expression[] result = new Expression[2];
                        result[0] = coefficientInNumeratorForTesting;
                        result[1] = coefficientInDenominatorForTesting;
                        return result;
                    }

                }

            }

        }

        Expression[] result = new Expression[0];
        return result;

    }

    /**
     * Kürzt (ganzzahlige Potenzen von) Ausdrücken aus Brüchen, z. B. wird
     * (x^2*y + z*x^3)/(2*x - x^4) zu (x*y + z*x^2)/(2 - x^3) vereinfacht.
     */
    public static void reduceSameExpressionInAllSummandsInQuotient(ExpressionCollection factorsNumerator, ExpressionCollection factorsDenominator) throws EvaluationException {

        Expression factorNumerator, factorDenominator;
        Expression[] reducedFactor;

        for (int i = 0; i < factorsNumerator.getBound(); i++) {

            if (factorsNumerator.get(i) == null) {
                continue;
            }

            factorNumerator = factorsNumerator.get(i);
            for (int j = 0; j < factorsDenominator.getBound(); j++) {

                if (factorsDenominator.get(j) == null) {
                    continue;
                }

                factorDenominator = factorsDenominator.get(j);
                if (factorNumerator.isSum() || factorNumerator.isDifference()
                        || factorDenominator.isSum() || factorDenominator.isDifference()) {

                    reducedFactor = reduceSameExpressionInAllSummandsInQuotient(factorNumerator, factorDenominator);
                    if (reducedFactor.length > 0) {
                        // Es konnten Terme in mindestens einem Faktor im Zähler gegen Terme in einem Faktor im Nenner gekürzt werden.
                        if (!reducedFactor[1].equals(ONE)) {
                            factorsNumerator.put(i, reducedFactor[0]);
                            factorsDenominator.put(j, reducedFactor[1]);
                        } else {
                            factorsNumerator.put(i, reducedFactor[0]);
                            factorsDenominator.remove(j);
                        }
                        break;
                    }

                } else if (factorNumerator.isPower() && factorDenominator.isPower()
                        && ((BinaryOperation) factorNumerator).getRight().equivalent(((BinaryOperation) factorDenominator).getRight())) {

                    /*
                     Sonderfall: Zähler und Nenner sind von der Form a^k,
                     b^k. Dann zu (a/b)^k kürzen.
                     */
                    if (((BinaryOperation) factorNumerator).getLeft().isSum() || ((BinaryOperation) factorNumerator).getLeft().isDifference()
                            || ((BinaryOperation) factorDenominator).getLeft().isSum() || ((BinaryOperation) factorDenominator).getLeft().isDifference()) {

                        reducedFactor = reduceSameExpressionInAllSummandsInQuotient(((BinaryOperation) factorNumerator).getLeft(), ((BinaryOperation) factorDenominator).getLeft());
                        if (reducedFactor.length > 0) {
                            // Es konnten Terme in mindestens einem Faktor im Zähler gegen Terme in einem Faktor im Nenner gekürzt werden.
                            if (!reducedFactor[1].equals(ONE)) {
                                factorsNumerator.put(i, reducedFactor[0].pow(((BinaryOperation) factorNumerator).getRight()));
                                factorsDenominator.put(j, reducedFactor[1].pow(((BinaryOperation) factorNumerator).getRight()));
                            } else {
                                factorsNumerator.put(i, reducedFactor[0].pow(((BinaryOperation) factorNumerator).getRight()));
                                factorsDenominator.remove(j);
                            }
                            break;
                        }

                    }
                }

            }

        }

    }

    /**
     * Hilfsmethode für die öffentliche Methode
     * reduceSameExpressionInAllSummandsInQuotient(). Kürzt (ganzzahlige
     * Potenzen von) Ausdrücken aus Brüchen, z. B. wird (x^2*y + z*x^3)/(2*x -
     * x^4) zu (x*y + z*x^2)/(2 - x^3) vereinfacht.
     */
    private static Expression[] reduceSameExpressionInAllSummandsInQuotient(Expression numerator, Expression denominator) throws EvaluationException {

        ExpressionCollection summandsLeftInNumerator = SimplifyUtilities.getSummandsLeftInExpression(numerator);
        ExpressionCollection summandsRightInNumerator = SimplifyUtilities.getSummandsRightInExpression(numerator);
        ExpressionCollection summandsLeftInDenominator = SimplifyUtilities.getSummandsLeftInExpression(denominator);
        ExpressionCollection summandsRightInDenominator = SimplifyUtilities.getSummandsRightInExpression(denominator);

        ExpressionCollection factorsToBeCancelled = SimplifyUtilities.getFactors(summandsLeftInNumerator.get(0));
        Expression factor, factorToCompare;
        BigInteger exponent, exponentToCompare, commonExponent;

        ExpressionCollection factorsInSummand;
        boolean factorToCompareFound = false;
        for (int i = 0; i < factorsToBeCancelled.getBound(); i++) {

            if (factorsToBeCancelled.get(i).isPositiveIntegerPower()) {
                factor = ((BinaryOperation) factorsToBeCancelled.get(i)).getLeft();
                exponent = ((Constant) ((BinaryOperation) factorsToBeCancelled.get(i)).getRight()).getBigIntValue();
            } else {
                factor = factorsToBeCancelled.get(i);
                exponent = BigInteger.ONE;
            }
            commonExponent = null;

            for (int j = 0; j < summandsLeftInNumerator.getBound(); j++) {

                factorsInSummand = SimplifyUtilities.getFactors(summandsLeftInNumerator.get(j));
                factorToCompareFound = false;

                for (int k = 0; k < factorsInSummand.getBound(); k++) {

                    if (factorsInSummand.get(k).isPositiveIntegerPower()) {
                        factorToCompare = ((BinaryOperation) factorsInSummand.get(k)).getLeft();
                        exponentToCompare = ((Constant) ((BinaryOperation) factorsInSummand.get(k)).getRight()).getBigIntValue();
                    } else {
                        factorToCompare = factorsInSummand.get(k);
                        exponentToCompare = BigInteger.ONE;
                    }

                    if (factor.equivalent(factorToCompare)) {
                        factorToCompareFound = true;
                        if (commonExponent == null) {
                            commonExponent = exponent.min(exponentToCompare);
                            break;
                        } else {
                            commonExponent = commonExponent.min(exponentToCompare);
                            break;
                        }
                    }

                }

                if (!factorToCompareFound) {
                    break;
                }

            }

            if (!factorToCompareFound) {
                continue;
            }

            for (int j = 0; j < summandsRightInNumerator.getBound(); j++) {

                factorsInSummand = SimplifyUtilities.getFactors(summandsRightInNumerator.get(j));
                factorToCompareFound = false;

                for (int k = 0; k < factorsInSummand.getBound(); k++) {

                    if (factorsInSummand.get(k).isPositiveIntegerPower()) {
                        factorToCompare = ((BinaryOperation) factorsInSummand.get(k)).getLeft();
                        exponentToCompare = ((Constant) ((BinaryOperation) factorsInSummand.get(k)).getRight()).getBigIntValue();
                    } else {
                        factorToCompare = factorsInSummand.get(k);
                        exponentToCompare = BigInteger.ONE;
                    }

                    if (factor.equivalent(factorToCompare)) {
                        factorToCompareFound = true;
                        commonExponent = commonExponent.min(exponentToCompare);
                        break;
                    }

                }

                if (!factorToCompareFound) {
                    break;
                }

            }

            if (!factorToCompareFound) {
                continue;
            }

            for (int j = 0; j < summandsLeftInDenominator.getBound(); j++) {

                factorsInSummand = SimplifyUtilities.getFactors(summandsLeftInDenominator.get(j));
                factorToCompareFound = false;

                for (int k = 0; k < factorsInSummand.getBound(); k++) {

                    if (factorsInSummand.get(k).isPositiveIntegerPower()) {
                        factorToCompare = ((BinaryOperation) factorsInSummand.get(k)).getLeft();
                        exponentToCompare = ((Constant) ((BinaryOperation) factorsInSummand.get(k)).getRight()).getBigIntValue();
                    } else {
                        factorToCompare = factorsInSummand.get(k);
                        exponentToCompare = BigInteger.ONE;
                    }

                    if (factor.equivalent(factorToCompare)) {
                        factorToCompareFound = true;
                        commonExponent = commonExponent.min(exponentToCompare);
                        break;
                    }

                }

                if (!factorToCompareFound) {
                    break;
                }

            }

            if (!factorToCompareFound) {
                continue;
            }

            for (int j = 0; j < summandsRightInDenominator.getBound(); j++) {

                factorsInSummand = SimplifyUtilities.getFactors(summandsRightInDenominator.get(j));
                factorToCompareFound = false;

                for (int k = 0; k < factorsInSummand.getBound(); k++) {

                    if (factorsInSummand.get(k).isPositiveIntegerPower()) {
                        factorToCompare = ((BinaryOperation) factorsInSummand.get(k)).getLeft();
                        exponentToCompare = ((Constant) ((BinaryOperation) factorsInSummand.get(k)).getRight()).getBigIntValue();
                    } else {
                        factorToCompare = factorsInSummand.get(k);
                        exponentToCompare = BigInteger.ONE;
                    }

                    if (factor.equivalent(factorToCompare)) {
                        factorToCompareFound = true;
                        commonExponent = commonExponent.min(exponentToCompare);
                        break;
                    }

                }

                if (!factorToCompareFound) {
                    break;
                }

            }

            if (!factorToCompareFound) {
                continue;
            }

            // Ab hier wurde eine gemeinsame Potenz gefunden! Jetzt muss sie überall herausgekürzt werden.
            for (int j = 0; j < summandsLeftInNumerator.getBound(); j++) {
                factorsInSummand = SimplifyUtilities.getFactors(summandsLeftInNumerator.get(j));
                for (int k = 0; k < factorsInSummand.getBound(); k++) {
                    if (factorsInSummand.get(k).isPositiveIntegerPower()) {
                        factorToCompare = ((BinaryOperation) factorsInSummand.get(k)).getLeft();
                        exponentToCompare = ((Constant) ((BinaryOperation) factorsInSummand.get(k)).getRight()).getBigIntValue();
                    } else {
                        factorToCompare = factorsInSummand.get(k);
                        exponentToCompare = BigInteger.ONE;
                    }
                    if (factor.equivalent(factorToCompare)) {
                        factorsInSummand.put(k, factor.pow(exponentToCompare.subtract(commonExponent)));
                        break;
                    }
                }
                summandsLeftInNumerator.put(j, SimplifyUtilities.produceProduct(factorsInSummand));
            }

            for (int j = 0; j < summandsRightInNumerator.getBound(); j++) {
                factorsInSummand = SimplifyUtilities.getFactors(summandsRightInNumerator.get(j));
                for (int k = 0; k < factorsInSummand.getBound(); k++) {
                    if (factorsInSummand.get(k).isPositiveIntegerPower()) {
                        factorToCompare = ((BinaryOperation) factorsInSummand.get(k)).getLeft();
                        exponentToCompare = ((Constant) ((BinaryOperation) factorsInSummand.get(k)).getRight()).getBigIntValue();
                    } else {
                        factorToCompare = factorsInSummand.get(k);
                        exponentToCompare = BigInteger.ONE;
                    }
                    if (factor.equivalent(factorToCompare)) {
                        factorsInSummand.put(k, factor.pow(exponentToCompare.subtract(commonExponent)));
                        break;
                    }
                }
                summandsRightInNumerator.put(j, SimplifyUtilities.produceProduct(factorsInSummand));
            }

            for (int j = 0; j < summandsLeftInDenominator.getBound(); j++) {
                factorsInSummand = SimplifyUtilities.getFactors(summandsLeftInDenominator.get(j));
                for (int k = 0; k < factorsInSummand.getBound(); k++) {
                    if (factorsInSummand.get(k).isPositiveIntegerPower()) {
                        factorToCompare = ((BinaryOperation) factorsInSummand.get(k)).getLeft();
                        exponentToCompare = ((Constant) ((BinaryOperation) factorsInSummand.get(k)).getRight()).getBigIntValue();
                    } else {
                        factorToCompare = factorsInSummand.get(k);
                        exponentToCompare = BigInteger.ONE;
                    }
                    if (factor.equivalent(factorToCompare)) {
                        factorsInSummand.put(k, factor.pow(exponentToCompare.subtract(commonExponent)));
                        break;
                    }
                }
                summandsLeftInDenominator.put(j, SimplifyUtilities.produceProduct(factorsInSummand));
            }

            for (int j = 0; j < summandsRightInDenominator.getBound(); j++) {
                factorsInSummand = SimplifyUtilities.getFactors(summandsRightInDenominator.get(j));
                for (int k = 0; k < factorsInSummand.getBound(); k++) {
                    if (factorsInSummand.get(k).isPositiveIntegerPower()) {
                        factorToCompare = ((BinaryOperation) factorsInSummand.get(k)).getLeft();
                        exponentToCompare = ((Constant) ((BinaryOperation) factorsInSummand.get(k)).getRight()).getBigIntValue();
                    } else {
                        factorToCompare = factorsInSummand.get(k);
                        exponentToCompare = BigInteger.ONE;
                    }
                    if (factor.equivalent(factorToCompare)) {
                        factorsInSummand.put(k, factor.pow(exponentToCompare.subtract(commonExponent)));
                        break;
                    }
                }
                summandsRightInDenominator.put(j, SimplifyUtilities.produceProduct(factorsInSummand));
            }

            Expression[] reducedFraction = new Expression[2];
            reducedFraction[0] = SimplifyUtilities.produceDifference(summandsLeftInNumerator, summandsRightInNumerator);
            reducedFraction[1] = SimplifyUtilities.produceDifference(summandsLeftInDenominator, summandsRightInDenominator);
            return reducedFraction;

        }

        return new Expression[0];

    }

    /**
     * Kürzt (ganzzahlige Potenzen von) Ausdrücken aus Brüchen, z. B. wird
     * (ab^3+a^2)/(b^3+a) zu a vereinfacht.
     */
    public static void reduceGeneralFractionToNonFractionInQuotient(ExpressionCollection factorsNumerator, ExpressionCollection factorsDenominator) throws EvaluationException {

        Expression factorNumerator, factorDenominator;
        Expression baseNumerator, baseDenominator;
        BigInteger exponentNumerator, exponentDenominator;
        Expression[] reducedFactor;

        for (int i = 0; i < factorsNumerator.getBound(); i++) {

            if (factorsNumerator.get(i) == null) {
                continue;
            }
            if (containsDoubleFraction(factorsNumerator.get(i))) {
                // Sicherheitshalber, sonst kann es Endlosschleifen geben.
                continue;
            }

            factorNumerator = factorsNumerator.get(i);
            for (int j = 0; j < factorsDenominator.getBound(); j++) {

                if (factorsDenominator.get(j) == null) {
                    continue;
                }
                if (containsDoubleFraction(factorsDenominator.get(j))) {
                    // Sicherheitshalber, sonst kann es Endlosschleifen geben.
                    continue;
                }

                factorDenominator = factorsDenominator.get(j);
                if ((factorNumerator.isSum() || factorNumerator.isDifference())
                        && (factorDenominator.isSum() || factorDenominator.isDifference())) {

                    reducedFactor = reduceGeneralFractionToNonFractionInQuotient(factorNumerator, factorDenominator);
                    if (reducedFactor.length > 0) {
                        // Es konnten Terme in mindestens einem Faktor im Zähler gegen Terme in einem Faktor im Nenner gekürzt werden.
                        factorsNumerator.put(i, reducedFactor[0]);
                        factorsDenominator.put(j, reducedFactor[1]);
                        break;
                    }

                } else if (factorNumerator.isPower() && factorDenominator.isPower()
                        && ((BinaryOperation) factorNumerator).getRight().equivalent(((BinaryOperation) factorDenominator).getRight())) {

                    /*
                     Sonderfall: Zähler und Nenner sind von der Form a^k, b^k. 
                     Dann zu (a/b)^k kürzen.
                     */
                    if ((((BinaryOperation) factorNumerator).getLeft().isSum() || ((BinaryOperation) factorNumerator).getLeft().isDifference())
                            && (((BinaryOperation) factorDenominator).getLeft().isSum() || ((BinaryOperation) factorDenominator).getLeft().isDifference())) {

                        reducedFactor = reduceGeneralFractionToNonFractionInQuotient(((BinaryOperation) factorNumerator).getLeft(), ((BinaryOperation) factorDenominator).getLeft());
                        if (reducedFactor.length > 0) {
                            // Es konnten Terme in mindestens einem Faktor im Zähler gegen Terme in einem Faktor im Nenner gekürzt werden.
                            factorsNumerator.put(i, reducedFactor[0].pow(((BinaryOperation) factorNumerator).getRight()));
                            factorsDenominator.put(j, reducedFactor[1].pow(((BinaryOperation) factorNumerator).getRight()));
                            break;
                        }

                    }
                } else {

                    /*
                     Sonderfall: Zähler oder Nenner sind von der Form (P(x))^p,
                     und Q(x)^q. Dann wird in P(x) und Q(x) gekürzt und danach entsprechend vereinfacht.
                     */
                    if (factorNumerator.isPositiveIntegerPower()) {
                        baseNumerator = ((BinaryOperation) factorNumerator).getLeft();
                        exponentNumerator = ((Constant) ((BinaryOperation) factorNumerator).getRight()).getBigIntValue();
                    } else {
                        baseNumerator = factorNumerator;
                        exponentNumerator = BigInteger.ONE;
                    }
                    if (factorDenominator.isPositiveIntegerPower()) {
                        baseDenominator = ((BinaryOperation) factorDenominator).getLeft();
                        exponentDenominator = ((Constant) ((BinaryOperation) factorDenominator).getRight()).getBigIntValue();
                    } else {
                        baseDenominator = factorDenominator;
                        exponentDenominator = BigInteger.ONE;
                    }

                    reducedFactor = reduceGeneralFractionToNonFractionInQuotient(baseNumerator, baseDenominator);
                    if (reducedFactor.length > 0) {
                        // Es konnten Terme in mindestens einem Faktor im Zähler gegen Terme in einem Faktor im Nenner gekürzt werden.
                        if (exponentNumerator.compareTo(exponentDenominator) > 0) {
                            factorsNumerator.put(i, reducedFactor[0].pow(exponentDenominator).mult(baseNumerator.pow(exponentNumerator.subtract(exponentDenominator))));
                            factorsDenominator.put(j, reducedFactor[1].pow(exponentDenominator));
                        } else {
                            factorsNumerator.put(i, reducedFactor[0].pow(exponentNumerator));
                            factorsDenominator.put(j, reducedFactor[1].pow(exponentNumerator).mult(baseDenominator.pow(exponentDenominator.subtract(exponentNumerator))));
                        }
                        break;
                    }

                }

            }

        }

    }

    /**
     * Hilfsmethode. Gibt zurück, ob expr Doppelbrüche enthält, die man auf
     * einen Bruch bringen könnte (d.h. beispielsweise, dass Brüche in
     * Funktionsargumenten ignoriert werden).
     */
    private static boolean containsDoubleFraction(Expression expr) {
        return containsRepeatedFraction(expr, true);
    }

    /**
     * Hilfsmethode. Gibt zurück, ob expr Doppelbrüche enthält, die man auf
     * einen Bruch bringen könnte (d.h. beispielsweise, dass Brüche in
     * Funktionsargumenten ignoriert werden).
     */
    private static boolean containsRepeatedFraction(Expression expr, boolean nestedFractionAllowed) {

        if (expr instanceof BinaryOperation) {
            if (expr.isSum() || expr.isDifference() || expr.isProduct()) {
                return containsRepeatedFraction(((BinaryOperation) expr).getLeft(), nestedFractionAllowed) || containsRepeatedFraction(((BinaryOperation) expr).getRight(), nestedFractionAllowed);
            }
            if (expr.isQuotient()) {
                if (!nestedFractionAllowed) {
                    return true;
                }
                return containsRepeatedFraction(((BinaryOperation) expr).getLeft(), false) || containsRepeatedFraction(((BinaryOperation) expr).getRight(), false);
            }
            if (expr.isIntegerPower()) {
                return containsRepeatedFraction(((BinaryOperation) expr).getLeft(), nestedFractionAllowed);
            }
        }
        return false;

    }

    /**
     * Hilfsmethode für die öffentliche Methode
     * reduceSameExpressionInAllSummandsInQuotient(). Kürzt (ganzzahlige
     * Potenzen von) Ausdrücken aus Brüchen, z. B. wird (ab^3+a^2)/(b^3+a) zu a
     * vereinfacht.
     */
    private static Expression[] reduceGeneralFractionToNonFractionInQuotient(Expression numerator, Expression denominator) throws EvaluationException {

        Set<Expression> setOfSubstitutions = getSetOfSubstitutionsForFractionReduction(numerator.div(denominator));

        if (setOfSubstitutions.isEmpty()) {
            return new Expression[0];
        }

        Expression numeratorSubstituted, denominatorSubstituted;
        BigInteger degNumerator, degDenominator;
        String substVar = SubstitutionUtilities.getSubstitutionVariable(numerator.div(denominator));
        for (Expression substitution : setOfSubstitutions) {

            numeratorSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(numerator, substitution, Variable.create(substVar));
            denominatorSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(denominator, substitution, Variable.create(substVar));

            if (SimplifyPolynomialUtils.isPolynomialAdmissibleForComputation(numeratorSubstituted, substVar)
                    && SimplifyPolynomialUtils.isPolynomialAdmissibleForComputation(denominatorSubstituted, substVar)) {

                degNumerator = SimplifyPolynomialUtils.getDegreeOfPolynomial(numeratorSubstituted, substVar);
                degDenominator = SimplifyPolynomialUtils.getDegreeOfPolynomial(denominatorSubstituted, substVar);

                if (degNumerator.compareTo(BigInteger.ZERO) > 0 && degDenominator.compareTo(BigInteger.ZERO) > 0) {

                    // Polynomdivision versuchen. Wenn diese keinen Rest hinterlässt -> Rücksubstitution und Ausgabe.
                    ExpressionCollection coefficientsNumerator = SimplifyPolynomialUtils.getPolynomialCoefficients(numeratorSubstituted, substVar);
                    ExpressionCollection coefficientsDenominator = SimplifyPolynomialUtils.getPolynomialCoefficients(denominatorSubstituted, substVar);

                    if (coefficientsNumerator.getBound() < 2 || coefficientsDenominator.getBound() < 2) {
                        // Falls eines der Grade doch noch auf 0 sinkt (getDegreeOfPolynomial() ist nur eine obere Schranke für den Grad!) -> einfach weiter probieren.
                        continue;
                    }

                    ExpressionCollection[] quotient = SimplifyPolynomialUtils.polynomialDivision(coefficientsNumerator, coefficientsDenominator);
                    if (quotient[1].isEmpty()) {
                        return new Expression[]{SimplifyPolynomialUtils.getPolynomialFromCoefficients(quotient[0], substVar).replaceVariable(substVar, substitution), ONE};
                    }
                    // Für den Kehrwert ebenso versuchen!
                    quotient = SimplifyPolynomialUtils.polynomialDivision(coefficientsDenominator, coefficientsNumerator);
                    if (quotient[1].isEmpty()) {
                        return new Expression[]{ONE, SimplifyPolynomialUtils.getPolynomialFromCoefficients(quotient[0], substVar).replaceVariable(substVar, substitution)};
                    }

                }

            }

        }

        return new Expression[0];

    }

    private static Set<Expression> getSetOfSubstitutionsForFractionReduction(Expression expr) {

        // Nur im Falle eines Quotienten (= eigentlicher Anwendungsfall) weitermachen.
        if (expr.isNotQuotient()) {
            return new HashSet<>();
        }

        ExpressionCollection summandsLeftInNumerator = SimplifyUtilities.getSummandsLeftInExpression(((BinaryOperation) expr).getLeft());
        ExpressionCollection summandsRightInNumerator = SimplifyUtilities.getSummandsRightInExpression(((BinaryOperation) expr).getLeft());
        ExpressionCollection summandsLeftInDenominator = SimplifyUtilities.getSummandsLeftInExpression(((BinaryOperation) expr).getRight());
        ExpressionCollection summandsRightInDenominator = SimplifyUtilities.getSummandsRightInExpression(((BinaryOperation) expr).getRight());
        Set<Expression> setOfSubstitutions = new HashSet<>();

        addSubstitutions(summandsLeftInNumerator, setOfSubstitutions);
        addSubstitutions(summandsRightInNumerator, setOfSubstitutions);
        addSubstitutions(summandsLeftInDenominator, setOfSubstitutions);
        addSubstitutions(summandsRightInDenominator, setOfSubstitutions);
        return setOfSubstitutions;
    }

    private static void addSubstitutions(ExpressionCollection summands, Set<Expression> setOfSubstitutions) {

        boolean algebraicallyDependendExpFunctionFound;
        boolean equivalentExpressionFound;
        ExpressionCollection factorsOfSummand;
        Expression quotient, newSubstitution;
        Expression base, baseToCompare;
        BigInteger exponent, exponentToCompare;

        /*
         Im Folgenden dürfen algebraische Operationen nicht auftreten, da diese 
         im Laufe algebraischer Vereinfachungen wieder rückgängig gemacht werden können 
         (-> Endloszyklen).
         */
        for (Expression summand : summands) {

            if (summand.isConstant()) {
                continue;
            }

            if (!summand.containsAlgebraicOperation()) {
                factorsOfSummand = SimplifyUtilities.getFactors(summand);
                for (Expression factor : factorsOfSummand) {

                    if (factor.isConstant()) {
                        continue;
                    }

                    if (factor.isFunction(TypeFunction.exp)) {

                        algebraicallyDependendExpFunctionFound = false;
                        // Sonderfall: Der Faktor ist eine Exponentialfunktion.
                        for (Expression subst : setOfSubstitutions) {
                            if (subst.isFunction(TypeFunction.exp)) {
                                try {
                                    quotient = ((Function) factor).getLeft().div(((Function) subst).getLeft()).simplify();
                                    if (quotient.isIntegerConstantOrRationalConstant()) {
                                        algebraicallyDependendExpFunctionFound = true;
                                    }
                                    if (quotient.isRationalConstant()) {
                                        newSubstitution = ((Function) subst).getLeft().div(((BinaryOperation) quotient).getRight()).exp().simplify();
                                        setOfSubstitutions.remove(subst);
                                        setOfSubstitutions.add(newSubstitution);
                                        break;
                                    }
                                } catch (EvaluationException e) {
                                    // Nichts tun, weiter suchen.
                                }
                            }
                        }
                        if (!algebraicallyDependendExpFunctionFound) {
                            setOfSubstitutions.add(factor);
                        }

                    } else if (factor.isPower() && !((BinaryOperation) factor).getRight().isConstant()) {

                        algebraicallyDependendExpFunctionFound = false;
                        // Sonderfall: Der Faktor ist eine Exponentialfunktion.
                        for (Expression subst : setOfSubstitutions) {
                            if (subst.isFunction(TypeFunction.exp)) {
                                try {
                                    quotient = (((BinaryOperation) factor).getLeft()).ln().mult(((BinaryOperation) factor).getRight()).div(
                                            ((Function) subst).getLeft()).simplify();
                                    if (quotient.isIntegerConstantOrRationalConstant()) {
                                        algebraicallyDependendExpFunctionFound = true;
                                    }
                                    if (quotient.isRationalConstant()) {
                                        newSubstitution = ((Function) subst).getLeft().div(((BinaryOperation) quotient).getRight()).exp().simplify();
                                        setOfSubstitutions.remove(subst);
                                        setOfSubstitutions.add(newSubstitution);
                                        break;
                                    }
                                } catch (EvaluationException e) {
                                    // Nichts tun, weiter suchen.
                                }
                            }
                        }
                        if (!algebraicallyDependendExpFunctionFound) {
                            setOfSubstitutions.add((((BinaryOperation) factor).getLeft()).ln().mult(((BinaryOperation) factor).getRight()).exp());
                        }

                    } else if (factor.isPositiveIntegerPower() && !factor.containsAlgebraicOperation() && !(((BinaryOperation) factor).getLeft() instanceof BinaryOperation)) {

                        base = ((BinaryOperation) factor).getLeft();
                        exponent = ((Constant) ((BinaryOperation) factor).getRight()).getBigIntValue();

                        equivalentExpressionFound = false;
                        for (Expression subst : setOfSubstitutions) {
                            if (subst.isPositiveIntegerPower()) {
                                baseToCompare = ((BinaryOperation) subst).getLeft();
                                exponentToCompare = ((Constant) ((BinaryOperation) subst).getRight()).getBigIntValue();
                            } else {
                                baseToCompare = subst;
                                exponentToCompare = BigInteger.ONE;
                            }
                            if (base.equivalent(baseToCompare)) {
                                equivalentExpressionFound = true;
                                newSubstitution = base.pow(exponent.gcd(exponentToCompare));
                                setOfSubstitutions.remove(subst);
                                setOfSubstitutions.add(newSubstitution);
                                break;
                            }
                        }
                        if (!equivalentExpressionFound) {
                            setOfSubstitutions.add(factor);
                        }

                    } else if (!(factor instanceof BinaryOperation)) {

                        equivalentExpressionFound = false;
                        for (Expression subst : setOfSubstitutions) {
                            if (subst.equivalent(factor)) {
                                equivalentExpressionFound = true;
                                break;
                            }
                        }
                        if (!equivalentExpressionFound) {
                            setOfSubstitutions.add(factor);
                        }

                    }

                }
            }

        }

    }

    /**
     * Falls factorsNumerator und factorsDenominator Faktoren (oder Potenzen von
     * Faktoren, bei denen die Exponenten gleich sind) besitzen, welche
     * rationale Polynome (von nicht allzu hohem Grad) sind, dann werden beide
     * Faktoren durch ihren ggT dividiert.
     *
     * @throws EvaluationException
     */
    public static void reducePolynomialFactorsInNumeratorAndDenominatorByGCD(ExpressionCollection factorsNumerator, ExpressionCollection factorsDenominator) throws EvaluationException {

        Expression factorNumerator, factorDenominator;
        Expression baseNumerator, baseDenominator;
        BigInteger exponentNumerator, exponentDenominator;
        Expression[] reducedFactor;

        Set<String> varsNumerator = new HashSet<>();
        Set<String> varsDenominator = new HashSet<>();
        String var;

        for (int i = 0; i < factorsNumerator.getBound(); i++) {

            if (factorsNumerator.get(i) == null) {
                continue;
            }

            varsNumerator.clear();
            factorsNumerator.get(i).addContainedVars(varsNumerator);
            if (varsNumerator.size() != 1) {
                continue;
            }
            var = getUniqueVar(varsNumerator);

            factorNumerator = factorsNumerator.get(i);
            if (!SimplifyPolynomialUtils.isPolynomial(factorNumerator, var)) {
                continue;
            }

            for (int j = 0; j < factorsDenominator.getBound(); j++) {

                if (factorsDenominator.get(j) == null) {
                    continue;
                }

                varsDenominator.clear();
                factorsDenominator.get(j).addContainedVars(varsDenominator);
                if (varsDenominator.size() != 1 || !varsDenominator.contains(var)) {
                    continue;
                }

                factorDenominator = factorsDenominator.get(j);
                if (!SimplifyPolynomialUtils.isPolynomial(factorDenominator, var)) {
                    continue;
                }

                if ((factorNumerator.isSum() || factorNumerator.isDifference())
                        && (factorDenominator.isSum() || factorDenominator.isDifference())) {

                    reducedFactor = reducePolynomialsInNumeratorAndDenominatorByGCD(factorNumerator, factorDenominator, var);
                    if (!reducedFactor[0].equivalent(factorNumerator)) {
                        // Es konnte mindestens ein Faktor im Zähler gegen einen Faktor im Nenner gekürzt werden.
                        if (!reducedFactor[1].equals(ONE)) {
                            factorsNumerator.put(i, reducedFactor[0]);
                            factorsDenominator.put(j, reducedFactor[1]);
                        } else {
                            factorsNumerator.put(i, reducedFactor[0]);
                            factorsDenominator.remove(j);
                        }
                        break;
                    }

                } else if (factorNumerator.isPower() && factorDenominator.isPower()
                        && ((BinaryOperation) factorNumerator).getRight().equivalent(((BinaryOperation) factorDenominator).getRight())) {

                    /*
                     Sonderfall: Zähler und Nenner sind von der Form P(x)^k,
                     und Q(x)^k. Dann wird zu (P(x)/Q(x))^k gekürzt.
                     */
                    if ((((BinaryOperation) factorNumerator).getLeft().isSum() || ((BinaryOperation) factorNumerator).getLeft().isDifference())
                            && (((BinaryOperation) factorDenominator).getLeft().isSum() || ((BinaryOperation) factorDenominator).getLeft().isDifference())) {

                        reducedFactor = reducePolynomialsInNumeratorAndDenominatorByGCD(((BinaryOperation) factorNumerator).getLeft(), ((BinaryOperation) factorDenominator).getLeft(), var);
                        if (!reducedFactor[0].equivalent(((BinaryOperation) factorNumerator).getLeft())) {
                            // Es konnte mindestens ein Faktor im Zähler gegen einen Faktor im Nenner gekürzt werden.
                            factorsNumerator.put(i, reducedFactor[0].pow(((BinaryOperation) factorNumerator).getRight()));
                            factorsDenominator.put(j, reducedFactor[1].pow(((BinaryOperation) factorNumerator).getRight()));
                            break;
                        }

                    }
                } else {

                    /*
                     Sonderfall: Zähler oder Nenner sind von der Form (P(x))^p,
                     und Q(x)^q. Dann wird in P(x) und Q(x) gekürzt und danach entsprechend vereinfacht.
                     */
                    if (factorNumerator.isPositiveIntegerPower()) {
                        baseNumerator = ((BinaryOperation) factorNumerator).getLeft();
                        exponentNumerator = ((Constant) ((BinaryOperation) factorNumerator).getRight()).getBigIntValue();
                    } else {
                        baseNumerator = factorNumerator;
                        exponentNumerator = BigInteger.ONE;
                    }
                    if (factorDenominator.isPositiveIntegerPower()) {
                        baseDenominator = ((BinaryOperation) factorDenominator).getLeft();
                        exponentDenominator = ((Constant) ((BinaryOperation) factorDenominator).getRight()).getBigIntValue();
                    } else {
                        baseDenominator = factorDenominator;
                        exponentDenominator = BigInteger.ONE;
                    }

                    reducedFactor = reducePolynomialsInNumeratorAndDenominatorByGCD(baseNumerator, baseDenominator, var);
                    if (!reducedFactor[0].equivalent(baseNumerator)) {
                        // Es konnte mindestens ein Faktor im Zähler gegen einen Faktor im Nenner gekürzt werden.
                        if (exponentNumerator.compareTo(exponentDenominator) > 0) {
                            factorsNumerator.put(i, reducedFactor[0].pow(exponentDenominator).mult(baseNumerator.pow(exponentNumerator.subtract(exponentDenominator))));
                            factorsDenominator.put(j, reducedFactor[1].pow(exponentDenominator));
                        } else {
                            factorsNumerator.put(i, reducedFactor[0].pow(exponentNumerator));
                            factorsDenominator.put(j, reducedFactor[1].pow(exponentNumerator).mult(baseDenominator.pow(exponentDenominator.subtract(exponentNumerator))));
                        }
                        break;
                    }

                }

            }

        }

    }

    private static String getUniqueVar(Set<String> vars) {
        Iterator<String> iter = vars.iterator();
        return iter.next();
    }

    /**
     * Hilfsmethode für reducePolynomialFactorsInNumeratorAndDenominatorByGCD().
     * Falls der Zähler und der Nenner RATIONALE Polynome (von nicht allzu hohem
     * Grad) sind, dann werden beide durch ihren ggT dividiert.
     *
     * @throws EvaluationException
     */
    private static Expression[] reducePolynomialsInNumeratorAndDenominatorByGCD(Expression f, Expression g, String var) throws EvaluationException {

        Expression gcd = SimplifyPolynomialUtils.getGGTOfPolynomials(f, g, var);

        if (gcd.equals(ZERO) || gcd.equals(ONE)) {
            return new Expression[]{f, g};
        }

        ExpressionCollection coefficientsF = SimplifyPolynomialUtils.getPolynomialCoefficients(f, var);
        ExpressionCollection coefficientsG = SimplifyPolynomialUtils.getPolynomialCoefficients(g, var);
        ExpressionCollection coefficientsGCD = SimplifyPolynomialUtils.getPolynomialCoefficients(gcd, var);

        Expression fReduced = SimplifyPolynomialUtils.getPolynomialFromCoefficients(SimplifyPolynomialUtils.polynomialDivision(coefficientsF, coefficientsGCD)[0], var);
        Expression gReduced = SimplifyPolynomialUtils.getPolynomialFromCoefficients(SimplifyPolynomialUtils.polynomialDivision(coefficientsG, coefficientsGCD)[0], var);

        return new Expression[]{fReduced, gReduced};

    }

    /**
     * Vereinfacht ganzzahlige Potenzen von abs(). Genauer: abs(x)^(2*n) =
     * x^(2*n) und abs(x)^(2*n + 1) = x^(2*n)*abs(x).
     */
    public static Expression simplifyPowersOfAbs(BinaryOperation expr) {

        if (!expr.getLeft().isFunction(TypeFunction.abs) || expr.isNotPower() || !(expr.getRight().isIntegerConstant())) {
            return expr;
        }

        BigInteger exponent = ((Constant) expr.getRight()).getBigIntValue();
        if (exponent.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
            return ((Function) expr.getLeft()).getLeft().pow(expr.getRight());
        }
        return ((Function) expr.getLeft()).getLeft().pow(exponent.subtract(BigInteger.ONE)).mult(expr.getLeft());

    }

    // Methoden zur Vereinfachung von Kompositionen von Zehnerlogarithmen und Zehnerpotenzen.
    /**
     * Vereinfacht Folgendes: 10...0^(a + m*lg(b) + c + ...) = b^(m*n) *
     * 10...0^(a + c + ...)
     */
    public static Expression reducePowerOfTenAndSumsOfLog10(BinaryOperation expr) {

        if (expr.isNotPower() || !(expr.getLeft().isIntegerConstant())
                || expr.getLeft().equals(ZERO)
                || !isPowerOfTen(((Constant) expr.getLeft()).getBigIntValue())) {
            return expr;
        }

        ExpressionCollection summands = SimplifyUtilities.getSummands(expr.getRight());
        ExpressionCollection resultFactorsOutsideOfPowerOfTen = new ExpressionCollection();

        int exponent = SimplifyExpLogUtils.getExponentIfDivisibleByPowerOfTen(((Constant) expr.getLeft()).getBigIntValue());

        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) instanceof Function && ((Function) summands.get(i)).getType().equals(TypeFunction.lg)) {
                resultFactorsOutsideOfPowerOfTen.add(((Function) summands.get(i)).getLeft().pow(exponent));
                summands.remove(i);
            } else if (summands.get(i).isProduct()
                    && !((BinaryOperation) summands.get(i)).getLeft().isEvenIntegerConstant()
                    && ((BinaryOperation) summands.get(i)).getRight() instanceof Function
                    && ((Function) ((BinaryOperation) summands.get(i)).getRight()).getType().equals(TypeFunction.lg)) {
                resultFactorsOutsideOfPowerOfTen.add(((Function) ((BinaryOperation) summands.get(i)).getRight()).getLeft().pow(new Constant(exponent).mult(((BinaryOperation) summands.get(i)).getLeft())));
                summands.remove(i);
            }
        }

        if (resultFactorsOutsideOfPowerOfTen.isEmpty()) {
            return expr;
        }

        // Ergebnis bilden
        return SimplifyUtilities.produceProduct(resultFactorsOutsideOfPowerOfTen).mult(expr.getLeft().pow(SimplifyUtilities.produceSum(summands)));

    }

    /**
     * Hilfsmethode. Prüft, ob value eine Zehnerpotenz ist.
     */
    public static boolean isPowerOfTen(BigInteger value) {
        if (value.compareTo(BigInteger.ZERO) <= 0) {
            return false;
        }
        while (value.mod(BigInteger.TEN).compareTo(BigInteger.ZERO) == 0) {
            value = value.divide(BigInteger.TEN);
        }
        return value.compareTo(BigInteger.ONE) == 0;
    }

    /**
     * Vereinfacht Folgendes: falls expr von der Form 10...00^(a_1 + ... + a_m -
     * (b_1 + ... + b_n)) schreiben lässt und falls a_i oder b_j von der Form
     * c*lg(x) mit einer ungeraden Konstante c ist, dann wird 10...00^(a_i) bzw.
     * 10...00^(b_j) herausgezogen. Beispiel: 10000^(x + 3*lg(x) + z) wird zu
     * x^12 * 10000^(x + z).
     */
    public static Expression reducePowerOfTenAndDifferencesOfLog10(BinaryOperation expr) {

        if (expr.isNotPower() || !expr.getLeft().isIntegerConstant()
                || expr.getLeft().equals(ZERO)
                || !isPowerOfTen(((Constant) expr.getLeft()).getBigIntValue())
                || expr.getRight().isNotDifference()) {
            return expr;
        }

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getRight());
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getRight());
        ExpressionCollection resultFactorsInNumeratorOutsideOfPowerOfTen = new ExpressionCollection();
        ExpressionCollection resultFactorsInDenominatorOutsideOfPowerOfTen = new ExpressionCollection();

        int exponent = SimplifyExpLogUtils.getExponentIfDivisibleByPowerOfTen(((Constant) expr.getLeft()).getBigIntValue());

        for (int i = 0; i < summandsLeft.getBound(); i++) {
            if (summandsLeft.get(i) instanceof Function && ((Function) summandsLeft.get(i)).getType().equals(TypeFunction.lg)) {
                resultFactorsInNumeratorOutsideOfPowerOfTen.add(((Function) summandsLeft.get(i)).getLeft().pow(exponent));
                summandsLeft.remove(i);
            } else if (summandsLeft.get(i).isProduct()
                    && !((BinaryOperation) summandsLeft.get(i)).getLeft().isEvenIntegerConstant()
                    && ((BinaryOperation) summandsLeft.get(i)).getRight() instanceof Function
                    && ((Function) ((BinaryOperation) summandsLeft.get(i)).getRight()).getType().equals(TypeFunction.lg)) {
                resultFactorsInNumeratorOutsideOfPowerOfTen.add(((Function) ((BinaryOperation) summandsLeft.get(i)).getRight()).getLeft().pow(new Constant(exponent).mult(((BinaryOperation) summandsLeft.get(i)).getLeft())));
                summandsLeft.remove(i);
            }
        }
        for (int i = 0; i < summandsRight.getBound(); i++) {
            if (summandsRight.get(i) instanceof Function && ((Function) summandsRight.get(i)).getType().equals(TypeFunction.lg)) {
                resultFactorsInDenominatorOutsideOfPowerOfTen.add(((Function) summandsRight.get(i)).getLeft().pow(exponent));
                summandsRight.remove(i);
            } else if (summandsRight.get(i).isProduct()
                    && !((BinaryOperation) summandsRight.get(i)).getLeft().isEvenIntegerConstant()
                    && ((BinaryOperation) summandsRight.get(i)).getRight() instanceof Function
                    && ((Function) ((BinaryOperation) summandsRight.get(i)).getRight()).getType().equals(TypeFunction.lg)) {
                resultFactorsInDenominatorOutsideOfPowerOfTen.add(((Function) ((BinaryOperation) summandsRight.get(i)).getRight()).getLeft().pow(new Constant(exponent).mult(((BinaryOperation) summandsRight.get(i)).getLeft())));
                summandsRight.remove(i);
            }
        }

        // Ergebnis bilden
        if (resultFactorsInNumeratorOutsideOfPowerOfTen.isEmpty() && resultFactorsInDenominatorOutsideOfPowerOfTen.isEmpty()) {
            return expr;
        } else if (!resultFactorsInNumeratorOutsideOfPowerOfTen.isEmpty() && resultFactorsInDenominatorOutsideOfPowerOfTen.isEmpty()) {
            return SimplifyUtilities.produceProduct(resultFactorsInNumeratorOutsideOfPowerOfTen).mult(expr.getLeft().pow(
                    SimplifyUtilities.produceSum(summandsLeft).sub(((BinaryOperation) expr.getRight()).getRight())));
        } else if (resultFactorsInNumeratorOutsideOfPowerOfTen.isEmpty() && !resultFactorsInDenominatorOutsideOfPowerOfTen.isEmpty()) {
            return expr.getLeft().pow(((BinaryOperation) expr.getRight()).getLeft().sub(SimplifyUtilities.produceSum(summandsRight))).div(
                    SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfPowerOfTen));
        }
        return SimplifyUtilities.produceProduct(resultFactorsInNumeratorOutsideOfPowerOfTen).mult(expr.getLeft().pow(SimplifyUtilities.produceDifference(summandsLeft, summandsRight))).div(
                SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfPowerOfTen));

    }

    /**
     * Methode für das Faktorisieren in Summen. Es wird alles, bis auf
     * Konstanten, faktorisiert.
     */
    public static void simplifyFactorizeInSums(ExpressionCollection summands) {

        ExpressionCollection commonNumerators, commonDenominators;
        ExpressionCollection leftSummandRestNumerators, leftSummandRestDenominators,
                rightSummandRestNumerators, rightSummandRestDenominators;
        Expression factorizedSummand;
        ExpressionCollection leftRestFactors, rightRestFactors;

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }

                leftRestFactors = SimplifyUtilities.getFactorsOfNumeratorInExpression(summands.get(i));
                rightRestFactors = SimplifyUtilities.getFactorsOfNumeratorInExpression(summands.get(j));
                commonNumerators = SimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                leftSummandRestNumerators = SimplifyUtilities.difference(leftRestFactors, commonNumerators);
                rightSummandRestNumerators = SimplifyUtilities.difference(rightRestFactors, commonNumerators);

                leftRestFactors = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(i));
                rightRestFactors = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(j));
                commonDenominators = SimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                leftSummandRestDenominators = SimplifyUtilities.difference(leftRestFactors, commonDenominators);
                rightSummandRestDenominators = SimplifyUtilities.difference(rightRestFactors, commonDenominators);

                // Im Folgenden werden gemeinsame Faktoren, welche rationale Zahlen sind, NICHT faktorisiert!
                if (!commonNumerators.isEmpty() && commonDenominators.isEmpty()) {

                    if (commonNumerators.getBound() == 1 && commonNumerators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceProduct(commonNumerators).mult(
                            SimplifyUtilities.produceQuotient(leftSummandRestNumerators, leftSummandRestDenominators).add(
                            SimplifyUtilities.produceQuotient(rightSummandRestNumerators, rightSummandRestDenominators)));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (commonNumerators.isEmpty() && !commonDenominators.isEmpty()) {

                    if (commonDenominators.getBound() == 1 && commonDenominators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceQuotient(leftSummandRestNumerators, leftSummandRestDenominators).add(
                            SimplifyUtilities.produceQuotient(rightSummandRestNumerators, rightSummandRestDenominators)).div(SimplifyUtilities.produceProduct(commonDenominators));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!commonNumerators.isEmpty() && !commonDenominators.isEmpty()) {

                    if (commonNumerators.getBound() == 1 && commonNumerators.get(0) instanceof Constant
                            && commonDenominators.getBound() == 1 && commonDenominators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceProduct(commonNumerators).mult(
                            SimplifyUtilities.produceQuotient(leftSummandRestNumerators, leftSummandRestDenominators).add(
                            SimplifyUtilities.produceQuotient(rightSummandRestNumerators, rightSummandRestDenominators))).div(SimplifyUtilities.produceProduct(commonDenominators));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

            }

        }

    }

    /**
     * Methode für das Faktorisieren in Summen. Es wird alles, bis auf
     * Konstanten, faktorisiert.
     */
    public static void simplifyFactorizeAntiEquivalentExpressionsInSums(ExpressionCollection summands) {

        Expression summandLeft, summandRight;
        ExpressionCollection factorsNumeratorLeft, factorsNumeratorRight, factorsDenominatorLeft, factorsDenominatorRight;
        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }

            summandLeft = summands.get(i);

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }

                summandRight = summands.get(j);

                factorsNumeratorLeft = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandLeft);
                factorsNumeratorRight = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandRight);
                factorsDenominatorLeft = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandLeft);
                factorsDenominatorRight = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandRight);

                //Zähler absuchen.
                for (int p = 0; p < factorsNumeratorLeft.getBound(); p++) {

                    // Konstanten NICHT faktorisieren!
                    if (factorsNumeratorLeft.get(i) instanceof Constant) {
                        continue;
                    }

                    for (int q = 0; q < factorsNumeratorRight.getBound(); q++) {
                        if (factorsNumeratorLeft.get(p).antiEquivalent(factorsNumeratorRight.get(q))) {
                            Expression commonFactor = factorsNumeratorLeft.get(p);
                            factorsNumeratorLeft.remove(p);
                            factorsNumeratorRight.remove(q);
                            Expression factorizedSummand = commonFactor.mult(
                                    SimplifyUtilities.produceQuotient(factorsNumeratorLeft, factorsDenominatorLeft).sub(
                                    SimplifyUtilities.produceQuotient(factorsNumeratorRight, factorsDenominatorRight)));
                            summands.put(i, factorizedSummand);
                            summands.remove(j);
                            return;
                        }
                    }
                }
                //Nenner absuchen.
                for (int p = 0; p < factorsDenominatorLeft.getBound(); p++) {

                    // Konstanten NICHT faktorisieren!
                    if (factorsDenominatorLeft.get(i) instanceof Constant) {
                        continue;
                    }

                    for (int q = 0; q < factorsDenominatorRight.getBound(); q++) {
                        if (factorsDenominatorLeft.get(p).antiEquivalent(factorsDenominatorRight.get(q))) {
                            Expression commonFactor = factorsDenominatorLeft.get(p);
                            factorsDenominatorLeft.remove(p);
                            factorsDenominatorRight.remove(q);
                            Expression factorizedSummand
                                    = SimplifyUtilities.produceQuotient(factorsNumeratorLeft, factorsDenominatorLeft).sub(
                                    SimplifyUtilities.produceQuotient(factorsNumeratorRight, factorsDenominatorRight)).div(commonFactor);
                            summands.put(i, factorizedSummand);
                            summands.remove(j);
                            return;
                        }
                    }
                }

            }

        }

    }

    /**
     * Methode für das Faktorisieren in Differenzen. Es wird alles, bis auf
     * Konstanten, faktorisiert.
     */
    public static void simplifyFactorizeInDifferences(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) {

        ExpressionCollection commonNumerators, commonDenominators;
        ExpressionCollection leftSummandRestNumerators, leftSummandRestDenominators,
                rightSummandRestNumerators, rightSummandRestDenominators;
        Expression factorizedSummand;
        ExpressionCollection leftRestFactors, rightRestFactors;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }

                leftRestFactors = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsLeft.get(i));
                rightRestFactors = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsRight.get(j));
                commonNumerators = SimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                leftSummandRestNumerators = SimplifyUtilities.difference(leftRestFactors, commonNumerators);
                rightSummandRestNumerators = SimplifyUtilities.difference(rightRestFactors, commonNumerators);

                leftRestFactors = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(i));
                rightRestFactors = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(j));
                commonDenominators = SimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                leftSummandRestDenominators = SimplifyUtilities.difference(leftRestFactors, commonDenominators);
                rightSummandRestDenominators = SimplifyUtilities.difference(rightRestFactors, commonDenominators);

                // Im Folgenden werden gemeinsame Faktoren, welche rationale Zahlen sind, NICHT faktorisiert!
                if (!commonNumerators.isEmpty() && commonDenominators.isEmpty()) {

                    if (commonNumerators.getBound() == 1 && commonNumerators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceProduct(commonNumerators).mult(
                            SimplifyUtilities.produceQuotient(leftSummandRestNumerators, leftSummandRestDenominators).sub(
                            SimplifyUtilities.produceQuotient(rightSummandRestNumerators, rightSummandRestDenominators)));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (commonNumerators.isEmpty() && !commonDenominators.isEmpty()) {

                    if (commonDenominators.getBound() == 1 && commonDenominators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceQuotient(leftSummandRestNumerators, leftSummandRestDenominators).sub(
                            SimplifyUtilities.produceQuotient(rightSummandRestNumerators, rightSummandRestDenominators)).div(
                            SimplifyUtilities.produceProduct(commonDenominators));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!commonNumerators.isEmpty() && !commonDenominators.isEmpty()) {

                    if (commonNumerators.getBound() == 1 && commonNumerators.get(0) instanceof Constant
                            && commonDenominators.getBound() == 1 && commonDenominators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceProduct(commonNumerators).mult(
                            SimplifyUtilities.produceQuotient(leftSummandRestNumerators, leftSummandRestDenominators).sub(
                            SimplifyUtilities.produceQuotient(rightSummandRestNumerators, rightSummandRestDenominators))).div(
                            SimplifyUtilities.produceProduct(commonDenominators));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

            }

        }

    }

    /**
     * Methode für das Faktorisieren in Differenzen. Es wird alles, bis auf
     * Konstanten, faktorisiert.
     */
    public static void simplifyFactorizeAntiEquivalentExpressionsInDifferences(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) {

        Expression summandLeft, summandRight;
        ExpressionCollection factorsNumeratorLeft, factorsNumeratorRight, factorsDenominatorLeft, factorsDenominatorRight;
        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }

            summandLeft = summandsLeft.get(i);

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }

                summandRight = summandsRight.get(j);

                factorsNumeratorLeft = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandLeft);
                factorsNumeratorRight = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandRight);
                factorsDenominatorLeft = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandLeft);
                factorsDenominatorRight = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandRight);

                //Zähler absuchen.
                for (int p = 0; p < factorsNumeratorLeft.getBound(); p++) {

                    // Konstanten NICHT faktorisieren!
                    if (factorsNumeratorLeft.get(i) instanceof Constant) {
                        continue;
                    }

                    for (int q = 0; q < factorsNumeratorRight.getBound(); q++) {
                        if (factorsNumeratorLeft.get(p).antiEquivalent(factorsNumeratorRight.get(q))) {
                            Expression commonFactor = factorsNumeratorLeft.get(p);
                            factorsNumeratorLeft.remove(p);
                            factorsNumeratorRight.remove(q);
                            Expression factorizedSummand = commonFactor.mult(
                                    SimplifyUtilities.produceQuotient(factorsNumeratorLeft, factorsDenominatorLeft).add(
                                    SimplifyUtilities.produceQuotient(factorsNumeratorRight, factorsDenominatorRight)));
                            summandsLeft.put(i, factorizedSummand);
                            summandsRight.remove(j);
                            return;
                        }
                    }
                }
                //Nenner absuchen.
                for (int p = 0; p < factorsDenominatorLeft.getBound(); p++) {

                    // Konstanten NICHT faktorisieren!
                    if (factorsDenominatorLeft.get(i) instanceof Constant) {
                        continue;
                    }

                    for (int q = 0; q < factorsDenominatorRight.getBound(); q++) {
                        if (factorsDenominatorLeft.get(p).antiEquivalent(factorsDenominatorRight.get(q))) {
                            Expression commonFactor = factorsDenominatorLeft.get(p);
                            factorsDenominatorLeft.remove(p);
                            factorsDenominatorRight.remove(q);
                            Expression factorizedSummand
                                    = SimplifyUtilities.produceQuotient(factorsNumeratorLeft, factorsDenominatorLeft).add(
                                    SimplifyUtilities.produceQuotient(factorsNumeratorRight, factorsDenominatorRight)).div(commonFactor);
                            summandsLeft.put(i, factorizedSummand);
                            summandsRight.remove(j);
                            return;
                        }
                    }
                }

            }

        }

    }

    /**
     * Hilfsmethode für simplifyExpand() in BinaryOperation. Multipliziert EINE
     * Klammer vollständig aus (gilt sowohl für Multiplikationen als auch für
     * Potenzen).
     */
    public static Expression simplifySingleExpand(Expression f, TypeExpansion type) throws EvaluationException {

        BinaryOperation expr;

        if (f.isSum() || f.isDifference()) {
            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(f);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(f);
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, summandsLeft.get(i).simplifyExpand(type));
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, summandsRight.get(i).simplifyExpand(type));
            }
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);
        } else if (f.isQuotient()) {

            /* 
             Hier wird Folgendes gemacht: der Zähler wird ausmultipliziert, der Nenner ebenfalls.
             Dann wird die Summe / Differenz aller Summanden im Zähler, dividiert durch den Nenner, gebildet.
             Beispiel: expand((a+b)*(c+d)/(x*(y+z))) wird zu a*c/(x*y+x*z) + a*d/(x*y+x*z) + b*c/(x*y+x*z) + b*d/(x*y+x*z) 
             vereinfacht. 
             */
            Expression expandedNumerator = ((BinaryOperation) f).getLeft().simplifyExpand(type);
            Expression expandedDenominator = ((BinaryOperation) f).getRight().simplifyExpand(type);
            ExpressionCollection summandsLeftInNumerator = SimplifyUtilities.getSummandsLeftInExpression(expandedNumerator);
            ExpressionCollection summandsRightInNumerator = SimplifyUtilities.getSummandsRightInExpression(expandedNumerator);
            Expression expandedExprLeft = ZERO, expandedExprRight = ZERO;
            for (Expression summandLeft : summandsLeftInNumerator) {
                expandedExprLeft = expandedExprLeft.add(summandLeft.div(expandedDenominator));
            }
            for (Expression summandRight : summandsRightInNumerator) {
                expandedExprRight = expandedExprRight.add(summandRight.div(expandedDenominator));
            }

            return expandedExprLeft.sub(expandedExprRight);

        } else if (f.isPower()) {
            Expression powerExpanded = ((BinaryOperation) f).getLeft().simplifyExpand(type).pow(((BinaryOperation) f).getRight().simplifyExpand(type));
            if (!(powerExpanded instanceof BinaryOperation)) {
                return powerExpanded;
            }
            expr = (BinaryOperation) powerExpanded;
        } else {
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyExpand(type));
            }
            Expression productOfExpandedFactors = SimplifyUtilities.produceProduct(factors);
            if (!(productOfExpandedFactors instanceof BinaryOperation)) {
                return productOfExpandedFactors;
            }
            expr = (BinaryOperation) productOfExpandedFactors;
        }

        if (expr.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(expr);
            BigInteger numberOfResultSummands = BigInteger.ONE;
            ExpressionCollection summandsLeftOfFactor;
            ExpressionCollection summandsRightOfFactor;
            for (int i = 0; i < factors.getBound(); i++) {
                summandsLeftOfFactor = SimplifyUtilities.getSummandsLeftInExpression(factors.get(i));
                summandsRightOfFactor = SimplifyUtilities.getSummandsRightInExpression(factors.get(i));
                numberOfResultSummands = numberOfResultSummands.multiply(BigInteger.valueOf(summandsLeftOfFactor.getBound() + summandsRightOfFactor.getBound()));
            }
            BigInteger boundNumberOfSummands = BigInteger.ZERO;
            if (type.equals(TypeExpansion.POWERFUL)) {
                boundNumberOfSummands = BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_POWERFUL_EXPANSION);
            } else if (type.equals(TypeExpansion.MODERATE)) {
                boundNumberOfSummands = BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_MODERATE_EXPANSION);
            } else if (type.equals(TypeExpansion.SHORT)) {
                boundNumberOfSummands = BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_SHORT_EXPANSION);
            }
            if (numberOfResultSummands.compareTo(boundNumberOfSummands) > 0) {
                return expr;
            }
            int smallestIndexOfFactorWhichIsEitherSumOrDifference = 0;
            while (smallestIndexOfFactorWhichIsEitherSumOrDifference < factors.getBound() && factors.get(smallestIndexOfFactorWhichIsEitherSumOrDifference).isNotSum() && factors.get(smallestIndexOfFactorWhichIsEitherSumOrDifference).isNotDifference()) {
                smallestIndexOfFactorWhichIsEitherSumOrDifference++;
            }
            if (smallestIndexOfFactorWhichIsEitherSumOrDifference == factors.getBound()) {
                return expr;
            }
            Expression currentFactor = factors.get(smallestIndexOfFactorWhichIsEitherSumOrDifference);
            factors.remove(smallestIndexOfFactorWhichIsEitherSumOrDifference);
            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(currentFactor);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(currentFactor);
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, SimplifyUtilities.produceProduct(factors.copy(0, smallestIndexOfFactorWhichIsEitherSumOrDifference)).mult(summandsLeft.get(i)).mult(SimplifyUtilities.produceProduct(factors.copy(smallestIndexOfFactorWhichIsEitherSumOrDifference + 1, factors.getBound() + 1))).orderSumsAndProducts());
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, SimplifyUtilities.produceProduct(factors.copy(0, smallestIndexOfFactorWhichIsEitherSumOrDifference)).mult(summandsRight.get(i)).mult(SimplifyUtilities.produceProduct(factors.copy(smallestIndexOfFactorWhichIsEitherSumOrDifference + 1, factors.getBound() + 1))).orderSumsAndProducts());
            }

            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (expr.isQuotient()) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getLeft());
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getLeft());
            if (summandsLeft.getBound() + summandsRight.getBound() == 1) {
                return expr;
            }
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, summandsLeft.get(i).div(expr.getRight()));
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, summandsRight.get(i).div(expr.getRight()));
            }
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        }

        if (expr.isPower() && expr.getRight().isIntegerConstant() && expr.getRight().isNonNegative()) {

            BigInteger boundNumberOfSummands = BigInteger.ZERO;
            if (type.equals(TypeExpansion.POWERFUL)) {
                boundNumberOfSummands = BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_POWERFUL_EXPANSION);
            } else if (type.equals(TypeExpansion.MODERATE)) {
                boundNumberOfSummands = BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_MODERATE_EXPANSION);
            } else if (type.equals(TypeExpansion.SHORT)) {
                boundNumberOfSummands = BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_SHORT_EXPANSION);
            }

            if (expr.getLeft().isSum()) {
                ExpressionCollection summands = SimplifyUtilities.getSummands(expr.getLeft());
                BigInteger exponent = ((Constant) expr.getRight()).getBigIntValue();
                BigInteger numberOfSummandsInBase = BigInteger.valueOf(summands.getBound());
                if (numberOfSummandsInBase.subtract(BigInteger.ONE).add(exponent).compareTo(boundNumberOfSummands) > 0) {
                    return expr;
                }
                BigInteger numberOfSummandsInResult = ArithmeticUtils.factorial(numberOfSummandsInBase.subtract(BigInteger.ONE).add(exponent).intValue()).divide(ArithmeticUtils.factorial(numberOfSummandsInBase.intValue() - 1).multiply(ArithmeticUtils.factorial(exponent.intValue())));
                if (numberOfSummandsInResult.compareTo(boundNumberOfSummands) > 0) {
                    return expr;
                }
                return SimplifyAlgebraicExpressionUtils.binomialExpansion((BinaryOperation) expr.getLeft(), exponent.intValue());
            } else if (expr.getLeft().isDifference()) {
                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getLeft());
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getLeft());
                BigInteger exponent = ((Constant) expr.getRight()).getBigIntValue();
                BigInteger numberOfSummandsInBase = BigInteger.valueOf(summandsLeft.getBound() + summandsRight.getBound());
                // Abschätzungen, ob die Anzahl der resultierenden Summanden nicht über die vordefinierte Schranke hinauswächst.
                if (numberOfSummandsInBase.subtract(BigInteger.ONE).add(exponent).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                    return expr;
                }
                if (exponent.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_SHORT_EXPANSION)) > 0) {
                    return expr;
                }
                BigInteger numberOfSummandsInResult = ArithmeticUtils.factorial(numberOfSummandsInBase.subtract(BigInteger.ONE).add(exponent).intValue()).divide(ArithmeticUtils.factorial(numberOfSummandsInBase.intValue() - 1).multiply(ArithmeticUtils.factorial(exponent.intValue())));
                if (numberOfSummandsInResult.compareTo(boundNumberOfSummands) > 0) {
                    return expr;
                }
                return SimplifyAlgebraicExpressionUtils.binomialExpansion((BinaryOperation) expr.getLeft(), exponent.intValue());
            }

        }

        return expr;

    }

    /**
     * Methode für das Faktorisieren in Summen. Es wird nur dann faktorisiert,
     * wenn die Summanden, bis auf konstante Faktoren, übereinstimmen.
     */
    public static void simplifyFactorizeAllButRationalsInSums(ExpressionCollection summands) throws EvaluationException {

        ExpressionCollection rationalNumeratorsLeft;
        ExpressionCollection nonRationalNumeratorsLeft = new ExpressionCollection();
        ExpressionCollection rationalDenominatorsLeft;
        ExpressionCollection nonRationalDenominatorsLeft = new ExpressionCollection();
        ExpressionCollection rationalNumeratorsRight;
        ExpressionCollection nonRationalNumeratorsRight = new ExpressionCollection();
        ExpressionCollection rationalDenominatorsRight;
        ExpressionCollection nonRationalDenominatorsRight = new ExpressionCollection();

        Expression factorizedSummand;

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }

                rationalNumeratorsLeft = SimplifyUtilities.getFactorsOfNumeratorInExpression(summands.get(i));
                rationalNumeratorsRight = SimplifyUtilities.getFactorsOfNumeratorInExpression(summands.get(j));
                rationalDenominatorsLeft = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(i));
                rationalDenominatorsRight = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(j));
                nonRationalNumeratorsLeft.clear();
                nonRationalNumeratorsRight.clear();
                nonRationalDenominatorsLeft.clear();
                nonRationalDenominatorsRight.clear();

                for (int k = 0; k < rationalNumeratorsLeft.getBound(); k++) {
                    if (!(rationalNumeratorsLeft.get(k) instanceof Constant)) {
                        nonRationalNumeratorsLeft.add(rationalNumeratorsLeft.get(k));
                        rationalNumeratorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < rationalNumeratorsRight.getBound(); k++) {
                    if (!(rationalNumeratorsRight.get(k) instanceof Constant)) {
                        nonRationalNumeratorsRight.add(rationalNumeratorsRight.get(k));
                        rationalNumeratorsRight.remove(k);
                    }
                }
                for (int k = 0; k < rationalDenominatorsLeft.getBound(); k++) {
                    if (!(rationalDenominatorsLeft.get(k) instanceof Constant)) {
                        nonRationalDenominatorsLeft.add(rationalDenominatorsLeft.get(k));
                        rationalDenominatorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < rationalDenominatorsRight.getBound(); k++) {
                    if (!(rationalDenominatorsRight.get(k) instanceof Constant)) {
                        nonRationalDenominatorsRight.add(rationalDenominatorsRight.get(k));
                        rationalDenominatorsRight.remove(k);
                    }
                }

                // Falls die nichtrationalen Faktoren NICHT übereinstimmen, nächster Schleifendurchgang.
                if (!nonRationalNumeratorsLeft.equivalentInTerms(nonRationalNumeratorsRight)
                        || !nonRationalDenominatorsLeft.equivalentInTerms(nonRationalDenominatorsRight)) {
                    continue;
                }

                if (!nonRationalNumeratorsLeft.isEmpty() && nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalNumeratorsLeft, rationalDenominatorsLeft).add(SimplifyUtilities.produceQuotient(rationalNumeratorsRight, rationalDenominatorsRight)).mult(
                            SimplifyUtilities.produceProduct(nonRationalNumeratorsLeft));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (nonRationalNumeratorsLeft.isEmpty() && !nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalNumeratorsLeft, rationalDenominatorsLeft).add(SimplifyUtilities.produceQuotient(rationalNumeratorsRight, rationalDenominatorsRight)).div(SimplifyUtilities.produceProduct(nonRationalDenominatorsLeft));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!nonRationalNumeratorsLeft.isEmpty() && !nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalNumeratorsLeft, rationalDenominatorsLeft).add(
                            SimplifyUtilities.produceQuotient(rationalNumeratorsRight, rationalDenominatorsRight)).mult(
                            SimplifyUtilities.produceProduct(nonRationalNumeratorsLeft)).div(
                            SimplifyUtilities.produceProduct(nonRationalDenominatorsLeft));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

            }

        }

    }

    /**
     * Methode für das Faktorisieren in Summen. Es wird nur dann faktorisiert,
     * wenn die Summanden, bis auf konstante Faktoren und bis auf
     * Antiäquivalenz, übereinstimmen.
     */
    public static void simplifyFactorizeAllButRationalsForAntiEquivalentExpressionsInSums(ExpressionCollection summands) throws EvaluationException {

        ExpressionCollection rationalNumeratorsLeft;
        ExpressionCollection nonRationalNumeratorsLeft = new ExpressionCollection();
        ExpressionCollection rationalDenominatorsLeft;
        ExpressionCollection nonRationalDenominatorsLeft = new ExpressionCollection();
        ExpressionCollection rationalNumeratorsRight;
        ExpressionCollection nonRationalNumeratorsRight = new ExpressionCollection();
        ExpressionCollection rationalDenominatorsRight;
        ExpressionCollection nonRationalDenominatorsRight = new ExpressionCollection();

        Expression factorizedSummand;

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }

                rationalNumeratorsLeft = SimplifyUtilities.getFactorsOfNumeratorInExpression(summands.get(i));
                rationalNumeratorsRight = SimplifyUtilities.getFactorsOfNumeratorInExpression(summands.get(j));
                rationalDenominatorsLeft = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(i));
                rationalDenominatorsRight = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(j));
                nonRationalNumeratorsLeft.clear();
                nonRationalNumeratorsRight.clear();
                nonRationalDenominatorsLeft.clear();
                nonRationalDenominatorsRight.clear();

                for (int k = 0; k < rationalNumeratorsLeft.getBound(); k++) {
                    if (!(rationalNumeratorsLeft.get(k) instanceof Constant)) {
                        nonRationalNumeratorsLeft.add(rationalNumeratorsLeft.get(k));
                        rationalNumeratorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < rationalNumeratorsRight.getBound(); k++) {
                    if (!(rationalNumeratorsRight.get(k) instanceof Constant)) {
                        nonRationalNumeratorsRight.add(rationalNumeratorsRight.get(k));
                        rationalNumeratorsRight.remove(k);
                    }
                }
                for (int k = 0; k < rationalDenominatorsLeft.getBound(); k++) {
                    if (!(rationalDenominatorsLeft.get(k) instanceof Constant)) {
                        nonRationalDenominatorsLeft.add(rationalDenominatorsLeft.get(k));
                        rationalDenominatorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < rationalDenominatorsRight.getBound(); k++) {
                    if (!(rationalDenominatorsRight.get(k) instanceof Constant)) {
                        nonRationalDenominatorsRight.add(rationalDenominatorsRight.get(k));
                        rationalDenominatorsRight.remove(k);
                    }
                }

                // Falls die nichtrationalen Faktoren bis auf Antiäquivalenz NICHT übereinstimmen, nächster Schleifendurchgang.
                if (!SimplifyUtilities.produceQuotient(nonRationalNumeratorsLeft, nonRationalDenominatorsLeft).antiEquivalent(
                        SimplifyUtilities.produceQuotient(nonRationalNumeratorsRight, nonRationalDenominatorsRight))) {
                    continue;
                }

                if (!nonRationalNumeratorsLeft.isEmpty() && nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalNumeratorsLeft, rationalDenominatorsLeft).sub(SimplifyUtilities.produceQuotient(rationalNumeratorsRight, rationalDenominatorsRight)).mult(
                            SimplifyUtilities.produceProduct(nonRationalNumeratorsLeft));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (nonRationalNumeratorsLeft.isEmpty() && !nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalNumeratorsLeft, rationalDenominatorsLeft).sub(SimplifyUtilities.produceQuotient(rationalNumeratorsRight, rationalDenominatorsRight)).div(SimplifyUtilities.produceProduct(nonRationalDenominatorsLeft));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!nonRationalNumeratorsLeft.isEmpty() && !nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalNumeratorsLeft, rationalDenominatorsLeft).sub(
                            SimplifyUtilities.produceQuotient(rationalNumeratorsRight, rationalDenominatorsRight)).mult(
                            SimplifyUtilities.produceProduct(nonRationalNumeratorsLeft)).div(
                            SimplifyUtilities.produceProduct(nonRationalDenominatorsLeft));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

            }

        }

    }

    /**
     * Methode für das Faktorisieren in Differenzen. Es wird nur dann
     * faktorisiert, wenn die Summanden, bis auf konstante Faktoren,
     * übereinstimmen.
     */
    public static void simplifyFactorizeAllButRationalsInDifferences(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) throws EvaluationException {

        ExpressionCollection rationalNumeratorsLeft;
        ExpressionCollection nonRationalNumeratorsLeft = new ExpressionCollection();
        ExpressionCollection rationalDenominatorsLeft;
        ExpressionCollection nonRationalDenominatorsLeft = new ExpressionCollection();
        ExpressionCollection rationalNumeratorsRight;
        ExpressionCollection nonRationalNumeratorsRight = new ExpressionCollection();
        ExpressionCollection rationalDenominatorsRight;
        ExpressionCollection nonRationalDenominatorsRight = new ExpressionCollection();

        Expression factorizedSummand;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }

                rationalNumeratorsLeft = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsLeft.get(i));
                rationalNumeratorsRight = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsRight.get(j));
                rationalDenominatorsLeft = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(i));
                rationalDenominatorsRight = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(j));

                nonRationalNumeratorsLeft.clear();
                nonRationalNumeratorsRight.clear();
                nonRationalDenominatorsLeft.clear();
                nonRationalDenominatorsRight.clear();
                for (int k = 0; k < rationalNumeratorsLeft.getBound(); k++) {
                    if (!(rationalNumeratorsLeft.get(k) instanceof Constant)) {
                        nonRationalNumeratorsLeft.add(rationalNumeratorsLeft.get(k));
                        rationalNumeratorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < rationalNumeratorsRight.getBound(); k++) {
                    if (!(rationalNumeratorsRight.get(k) instanceof Constant)) {
                        nonRationalNumeratorsRight.add(rationalNumeratorsRight.get(k));
                        rationalNumeratorsRight.remove(k);
                    }
                }
                for (int k = 0; k < rationalDenominatorsLeft.getBound(); k++) {
                    if (!(rationalDenominatorsLeft.get(k) instanceof Constant)) {
                        nonRationalDenominatorsLeft.add(rationalDenominatorsLeft.get(k));
                        rationalDenominatorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < rationalDenominatorsRight.getBound(); k++) {
                    if (!(rationalDenominatorsRight.get(k) instanceof Constant)) {
                        nonRationalDenominatorsRight.add(rationalDenominatorsRight.get(k));
                        rationalDenominatorsRight.remove(k);
                    }
                }

                // Falls die nichtkonstanten Faktoren NICHT übereinstimmen, nächster Schleifendurchgang.
                if (!nonRationalNumeratorsLeft.equivalentInTerms(nonRationalNumeratorsRight)
                        || !nonRationalDenominatorsLeft.equivalentInTerms(nonRationalDenominatorsRight)) {
                    continue;
                }

                if (!nonRationalNumeratorsLeft.isEmpty() && nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalNumeratorsLeft, rationalDenominatorsLeft).sub(SimplifyUtilities.produceQuotient(rationalNumeratorsRight, rationalDenominatorsRight)).mult(
                            SimplifyUtilities.produceProduct(nonRationalNumeratorsLeft));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (nonRationalNumeratorsLeft.isEmpty() && !nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalNumeratorsLeft, rationalDenominatorsLeft).sub(SimplifyUtilities.produceQuotient(rationalNumeratorsRight, rationalDenominatorsRight)).div(SimplifyUtilities.produceProduct(nonRationalDenominatorsLeft));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!nonRationalNumeratorsLeft.isEmpty() && !nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalNumeratorsLeft, rationalDenominatorsLeft).sub(
                            SimplifyUtilities.produceQuotient(rationalNumeratorsRight, rationalDenominatorsRight)).mult(
                            SimplifyUtilities.produceProduct(nonRationalNumeratorsLeft)).div(
                            SimplifyUtilities.produceProduct(nonRationalDenominatorsLeft));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

            }

        }

    }

    /**
     * Methode für das Faktorisieren in Differenzen. Es wird nur dann
     * faktorisiert, wenn die Summanden, bis auf konstante Faktoren,
     * übereinstimmen.
     */
    public static void simplifyFactorizeAllButRationalsForAntiEquivalentExpressionsInDifferences(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) throws EvaluationException {

        ExpressionCollection rationalNumeratorsLeft;
        ExpressionCollection nonRationalNumeratorsLeft = new ExpressionCollection();
        ExpressionCollection rationalDenominatorsLeft;
        ExpressionCollection nonRationalDenominatorsLeft = new ExpressionCollection();
        ExpressionCollection rationalNumeratorsRight;
        ExpressionCollection nonRationalNumeratorsRight = new ExpressionCollection();
        ExpressionCollection rationalDenominatorsRight;
        ExpressionCollection nonRationalDenominatorsRight = new ExpressionCollection();

        Expression factorizedSummand;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }

                rationalNumeratorsLeft = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsLeft.get(i));
                rationalNumeratorsRight = SimplifyUtilities.getFactorsOfNumeratorInExpression(summandsRight.get(j));
                rationalDenominatorsLeft = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(i));
                rationalDenominatorsRight = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(j));

                nonRationalNumeratorsLeft.clear();
                nonRationalNumeratorsRight.clear();
                nonRationalDenominatorsLeft.clear();
                nonRationalDenominatorsRight.clear();
                for (int k = 0; k < rationalNumeratorsLeft.getBound(); k++) {
                    if (!(rationalNumeratorsLeft.get(k) instanceof Constant)) {
                        nonRationalNumeratorsLeft.add(rationalNumeratorsLeft.get(k));
                        rationalNumeratorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < rationalNumeratorsRight.getBound(); k++) {
                    if (!(rationalNumeratorsRight.get(k) instanceof Constant)) {
                        nonRationalNumeratorsRight.add(rationalNumeratorsRight.get(k));
                        rationalNumeratorsRight.remove(k);
                    }
                }
                for (int k = 0; k < rationalDenominatorsLeft.getBound(); k++) {
                    if (!(rationalDenominatorsLeft.get(k) instanceof Constant)) {
                        nonRationalDenominatorsLeft.add(rationalDenominatorsLeft.get(k));
                        rationalDenominatorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < rationalDenominatorsRight.getBound(); k++) {
                    if (!(rationalDenominatorsRight.get(k) instanceof Constant)) {
                        nonRationalDenominatorsRight.add(rationalDenominatorsRight.get(k));
                        rationalDenominatorsRight.remove(k);
                    }
                }

                // Falls die nichtrationalen Faktoren bis auf Antiäquivalenz NICHT übereinstimmen, nächster Schleifendurchgang.
                if (!SimplifyUtilities.produceQuotient(nonRationalNumeratorsLeft, nonRationalDenominatorsLeft).antiEquivalent(
                        SimplifyUtilities.produceQuotient(nonRationalNumeratorsRight, nonRationalDenominatorsRight))) {
                    continue;
                }

                if (!nonRationalNumeratorsLeft.isEmpty() && nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalNumeratorsLeft, rationalDenominatorsLeft).add(SimplifyUtilities.produceQuotient(rationalNumeratorsRight, rationalDenominatorsRight)).mult(
                            SimplifyUtilities.produceProduct(nonRationalNumeratorsLeft));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (nonRationalNumeratorsLeft.isEmpty() && !nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalNumeratorsLeft, rationalDenominatorsLeft).add(SimplifyUtilities.produceQuotient(rationalNumeratorsRight, rationalDenominatorsRight)).div(SimplifyUtilities.produceProduct(nonRationalDenominatorsLeft));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!nonRationalNumeratorsLeft.isEmpty() && !nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalNumeratorsLeft, rationalDenominatorsLeft).add(
                            SimplifyUtilities.produceQuotient(rationalNumeratorsRight, rationalDenominatorsRight)).mult(
                            SimplifyUtilities.produceProduct(nonRationalNumeratorsLeft)).div(
                            SimplifyUtilities.produceProduct(nonRationalDenominatorsLeft));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

            }

        }

    }

    /**
     * Methode für das Kürzen von Faktoren im Quotienten.
     */
    public static void simplifyReduceFactorsInQuotients(ExpressionCollection factorsNumerator, ExpressionCollection factorsDenominator) throws EvaluationException {

        Expression base;
        Expression exponent;
        Expression compareBase;
        Expression compareExponent;

        for (int i = 0; i < factorsNumerator.getBound(); i++) {

            if (factorsNumerator.get(i) == null) {
                continue;
            }

            if (factorsNumerator.get(i).isPower()) {
                base = ((BinaryOperation) factorsNumerator.get(i)).getLeft();
                exponent = ((BinaryOperation) factorsNumerator.get(i)).getRight();
            } else {
                base = factorsNumerator.get(i);
                exponent = ONE;
            }

            for (int j = 0; j < factorsDenominator.getBound(); j++) {

                if (factorsDenominator.get(j) == null) {
                    continue;
                }

                if (factorsDenominator.get(j).isPower()) {
                    compareBase = ((BinaryOperation) factorsDenominator.get(j)).getLeft();
                    compareExponent = ((BinaryOperation) factorsDenominator.get(j)).getRight();
                } else {
                    compareBase = factorsDenominator.get(j);
                    compareExponent = ONE;
                }

                if (base.equivalent(compareBase)) {
                    exponent = exponent.sub(compareExponent);
                    factorsDenominator.remove(j);
                    break;
                }

            }

            factorsNumerator.put(i, base.pow(exponent));

        }

    }

    /**
     *
     */
    public static void reduceFactorialsInQuotients(ExpressionCollection factorsNumerator, ExpressionCollection factorsDenominator) {

        Expression factorNumerator, factorDenominator;
        Expression[] reducedFactors;
        for (int i = 0; i < factorsNumerator.getBound(); i++) {

            if (factorsNumerator.get(i) == null) {
                continue;
            }
            if (!factorsNumerator.get(i).isOperator(TypeOperator.fac)) {
                continue;
            }
            factorNumerator = factorsNumerator.get(i);

            for (int j = 0; j < factorsDenominator.getBound(); j++) {

                if (factorsDenominator.get(j) == null) {
                    continue;
                }
                if (!factorsDenominator.get(j).isOperator(TypeOperator.fac)) {
                    continue;
                }
                factorDenominator = factorsDenominator.get(j);

                reducedFactors = reduceQuotientOfFactorials((Expression) ((Operator) factorNumerator).getParams()[0], (Expression) ((Operator) factorDenominator).getParams()[0]);
                if (reducedFactors.length == 2) {
                    if (reducedFactors[0].equals(ONE)) {
                        factorsNumerator.remove(i);
                        factorsDenominator.put(j, reducedFactors[1]);
                    } else if (reducedFactors[1].equals(ONE)) {
                        factorsNumerator.put(i, reducedFactors[0]);
                        factorsDenominator.remove(j);
                    }
                    break;
                }

            }

        }

    }

    private static Expression[] reduceQuotientOfFactorials(Expression factorialArgumentFactorNumerator, Expression factorialArgumentFactorDenominator) {

        Expression difference;
        Expression[] reducedFactors;
        try {

            difference = factorialArgumentFactorNumerator.sub(factorialArgumentFactorDenominator).simplify();
            if (difference.isIntegerConstant()) {

                Expression quotient = factorialArgumentFactorNumerator.div(factorialArgumentFactorDenominator);
                /*
                 Zunächst wird die Summationsvariable für die äußere Summe
                 ermittelt. Dies ist entweder k (falls k nicht schon vorher
                 auftaucht), oder k_0, k_1, k_2, ...
                 */
                String indexVarForProduct = "k";
                int index = 0;
                while (quotient.contains(indexVarForProduct + "_" + index)) {
                    index++;
                }
                if (index == 0) {
                    if (quotient.contains("k")) {
                        indexVarForProduct = indexVarForProduct + "_0";
                    } else {
                        indexVarForProduct = "k";
                    }
                } else {
                    indexVarForProduct = indexVarForProduct + "_" + index;
                }

                if (difference.isNonNegativeIntegerConstant()) {
                    reducedFactors = new Expression[2];
                    reducedFactors[0] = new Operator(TypeOperator.prod, new Object[]{factorialArgumentFactorDenominator.add(Variable.create(indexVarForProduct)),
                        indexVarForProduct, ONE, difference});
                    reducedFactors[1] = ONE;
                    return reducedFactors;
                } else if (difference.isNegativeIntegerConstant()) {
                    reducedFactors = new Expression[2];
                    reducedFactors[0] = ONE;
                    reducedFactors[1] = new Operator(TypeOperator.prod, new Object[]{factorialArgumentFactorNumerator.add(Variable.create(indexVarForProduct)),
                        indexVarForProduct, ONE, MINUS_ONE.mult(difference)});
                    return reducedFactors;
                }

            }

        } catch (EvaluationException e) {
        }
        return new Expression[0];

    }

    /**
     * Sammelt bei Multiplikation gemeinsame nichtkonstante Faktoren in factors
     * zu einem einzigen Faktor. Die Einträge in factors sind via 0, 1, 2, ...,
     * size - 1 indiziert.
     *
     * @throws EvaluationException
     */
    public static void collectFactorsInProduct(ExpressionCollection factors) throws EvaluationException {

        Expression base;
        Expression exponent;
        Expression baseToCompare;
        Expression exponentToCompare;

        BigInteger exponentAsBigInteger, exponentToCompareAsBigInteger;
        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).isPower()) {
                base = ((BinaryOperation) factors.get(i)).getLeft();
                exponent = ((BinaryOperation) factors.get(i)).getRight();
            } else {
                base = factors.get(i);
                exponent = Expression.ONE;
            }

            for (int j = i + 1; j < factors.getBound(); j++) {

                if (factors.get(j) == null) {
                    continue;
                }
                if (factors.get(j).isPower()) {
                    baseToCompare = ((BinaryOperation) factors.get(j)).getLeft();
                    exponentToCompare = ((BinaryOperation) factors.get(j)).getRight();
                } else {
                    baseToCompare = factors.get(j);
                    exponentToCompare = Expression.ONE;
                }
                if (base.equivalent(baseToCompare)) {
                    if (!base.isIntegerConstantOrRationalConstant()) {
                        exponent = exponent.add(exponentToCompare);
                        factors.put(i, base.pow(exponent));
                        factors.remove(j);
                    } else if (exponent.isIntegerConstant()) {
                        exponentAsBigInteger = ((Constant) exponent).getBigIntValue();
                        if (exponentAsBigInteger.abs().compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_POWER_OF_RATIONALS)) > 0) {
                            exponent = exponent.add(exponentToCompare);
                            factors.put(i, base.pow(exponent));
                            factors.remove(j);
                        }
                    } else if (exponentToCompare.isIntegerConstant()) {
                        exponentToCompareAsBigInteger = ((Constant) exponentToCompare).getBigIntValue();
                        if (exponentToCompareAsBigInteger.abs().compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_POWER_OF_RATIONALS)) > 0) {
                            exponent = exponent.add(exponentToCompare);
                            factors.put(i, base.pow(exponent));
                            factors.remove(j);
                        }
                    } else if (!exponent.isIntegerConstant() || !exponentToCompare.isIntegerConstant()) {
                        exponent = exponent.add(exponentToCompare);
                        factors.put(i, base.pow(exponent));
                        factors.remove(j);
                    } else if (!base.isIntegerConstantOrRationalConstant()) {
                        exponent = exponent.add(exponentToCompare);
                        factors.put(i, base.pow(exponent));
                        factors.remove(j);
                    }
                }

            }

        }

    }

    /**
     * Hilfsmethode. Sammelt bei Addition Konstanten in summands zu einer
     * einzigen Konstante im 0-ten Eintrag. Die Einträge in h sind via 0, 1, 2,
     * ..., size - 1 indiziert. Wird NUR für
     * collectConstantsAndConstantExpressionsInSum() benötigt.
     *
     * @throws EvaluationException
     */
    private static ExpressionCollection collectConstantsInSum(ExpressionCollection summands) throws EvaluationException {

        ExpressionCollection result = new ExpressionCollection();

        // Im Folgenden werden Konstanten immer im 0-ten Eintrag gesammelt.
        boolean summandsContainApproximates = false;
        for (Expression summand : summands) {
            summandsContainApproximates = summandsContainApproximates || summand.containsApproximates();
        }

        if (!summandsContainApproximates) {

            Expression constantSummand = Expression.ZERO;
            for (int i = 0; i < summands.getBound(); i++) {
                if (summands.get(i) == null) {
                    continue;
                }
                if (summands.get(i).isIntegerConstantOrRationalConstant()) {
                    constantSummand = addTwoRationals(constantSummand, summands.get(i));
                    summands.remove(i);
                }
            }
            if (!constantSummand.equals(Expression.ZERO)) {
                result.put(0, constantSummand);
            }
            for (int i = 0; i < summands.getBound(); i++) {
                if (summands.get(i) != null) {
                    result.add(summands.get(i));
                }
            }
            if (result.isEmpty()) {
                result.put(0, Expression.ZERO);
            }
            return result;

        } else {

            Constant constantSummand = Expression.ZERO;
            for (int i = 0; i < summands.getBound(); i++) {
                if (summands.get(i).isConstant()) {
                    constantSummand = new Constant(constantSummand.getApproxValue() + summands.get(i).evaluate());
                    summands.remove(i);
                }
            }
            if (constantSummand.getApproxValue() != 0) {
                result.put(0, constantSummand);
            }
            for (int i = 0; i < summands.getBound(); i++) {
                if (summands.get(i) != null) {
                    result.add(summands.get(i));
                }
            }
            if (result.isEmpty()) {
                result.put(0, new Constant((double) 0));
            }
            return result;

        }

    }

    /**
     * Addiert zwei rationale Zahlen (beide Argumente können entweder Konstanten
     * oder Quotienten von Konstanten sein). Falls eines der Argumente keine
     * (rationale) Konstante ist, dann liefert die Funktion einfach c_1 + c_2
     */
    public static Expression addTwoRationals(Expression exprLeft, Expression exprRight) {

        if (!exprLeft.isIntegerConstantOrRationalConstant() || !exprRight.isIntegerConstantOrRationalConstant()) {
            return exprLeft.add(exprRight);
        }

        BigInteger numeratorLeft, numeratorRight, denominatorLeft, denominatorRight;
        if (exprLeft.isIntegerConstant()) {
            numeratorLeft = ((Constant) exprLeft).getBigIntValue();
            denominatorLeft = BigInteger.ONE;
        } else {
            numeratorLeft = ((Constant) ((BinaryOperation) exprLeft).getLeft()).getBigIntValue();
            denominatorLeft = ((Constant) ((BinaryOperation) exprLeft).getRight()).getBigIntValue();
        }
        if (exprRight.isIntegerConstant()) {
            numeratorRight = ((Constant) exprRight).getBigIntValue();
            denominatorRight = BigInteger.ONE;
        } else {
            numeratorRight = ((Constant) ((BinaryOperation) exprRight).getLeft()).getBigIntValue();
            denominatorRight = ((Constant) ((BinaryOperation) exprRight).getRight()).getBigIntValue();
        }

        return new Constant(numeratorLeft.multiply(denominatorRight).add(numeratorRight.multiply(denominatorLeft))).div(
                denominatorLeft.multiply(denominatorRight));

    }

    /**
     * Sammelt bei Multiplikation Konstanten in factors zu einer einzigen
     * Konstante im 0-ten Eintrag. Die Einträge in factors sind via 0, 1, 2,
     * ..., size - 1 indiziert. Wird NUR für
     * collectConstantsAndConstantExpressionsInProduct() benötigt.
     *
     * @throws EvaluationException
     */
    private static ExpressionCollection collectConstantsInProduct(ExpressionCollection factors) throws EvaluationException {

        ExpressionCollection result = new ExpressionCollection();

        // Im Folgenden werden Konstanten immer im 0-ten Eintrag gesammelt.
        boolean factorsContainApproximatives = false;
        for (Expression factor : factors) {
            factorsContainApproximatives = factorsContainApproximatives || factor.containsApproximates();
        }

        if (!factorsContainApproximatives) {

            Expression constantFactor = Expression.ONE;
            for (int i = 0; i < factors.getBound(); i++) {
                if (factors.get(i).isIntegerConstantOrRationalConstant()) {
                    constantFactor = multiplyTwoRationals(constantFactor, factors.get(i));
                    factors.remove(i);
                }
            }
            if (constantFactor.equals(Expression.ZERO)) {
                result.put(0, constantFactor);
                return result;
            }
            if (!constantFactor.equals(Expression.ONE)) {
                result.put(0, constantFactor);
            }
            for (int i = 0; i < factors.getBound(); i++) {
                if (factors.get(i) != null) {
                    result.add(factors.get(i));
                }
            }
            if (result.isEmpty()) {
                result.put(0, Expression.ONE);
            }
            return result;

        } else {

            Constant constantFactor = Expression.ONE;
            for (int i = 0; i < factors.getBound(); i++) {
                if (factors.get(i).isConstant()) {
                    constantFactor = new Constant(constantFactor.getApproxValue() * factors.get(i).evaluate());
                    factors.remove(i);
                }
            }
            if (constantFactor.getApproxValue() == 0) {
                result.put(0, constantFactor);
                return result;
            }
            if (constantFactor.getApproxValue() != 1) {
                result.put(0, constantFactor);
            }
            for (int i = 0; i < factors.getBound(); i++) {
                if (factors.get(i) != null) {
                    result.add(factors.get(i));
                }
            }
            if (result.isEmpty()) {
                result.put(0, new Constant((double) 1));
            }
            return result;

        }

    }

    /**
     * Multipliziert zwei rationale Zahlen (beide Argumente können entweder
     * Konstanten oder Quotienten von Konstanten sein). Falls eines der
     * Argumente keine (rationale) Konstante ist, dann liefert die Funktion
     * einfach c_1 * c_2
     */
    private static Expression multiplyTwoRationals(Expression exprLeft, Expression exprRight) {

        if (!exprLeft.isIntegerConstantOrRationalConstant() || !exprRight.isIntegerConstantOrRationalConstant()) {
            return exprLeft.mult(exprRight);
        }

        BigInteger numeratorLeft, numeratorRight, denominatorLeft, denominatorRight;
        if (exprLeft.isIntegerConstant()) {
            numeratorLeft = ((Constant) exprLeft).getBigIntValue();
            denominatorLeft = BigInteger.ONE;
        } else {
            numeratorLeft = ((Constant) ((BinaryOperation) exprLeft).getLeft()).getBigIntValue();
            denominatorLeft = ((Constant) ((BinaryOperation) exprLeft).getRight()).getBigIntValue();
        }
        if (exprRight.isIntegerConstant()) {
            numeratorRight = ((Constant) exprRight).getBigIntValue();
            denominatorRight = BigInteger.ONE;
        } else {
            numeratorRight = ((Constant) ((BinaryOperation) exprRight).getLeft()).getBigIntValue();
            denominatorRight = ((Constant) ((BinaryOperation) exprRight).getRight()).getBigIntValue();
        }

        return new Constant(numeratorLeft.multiply(numeratorRight)).div(denominatorLeft.multiply(denominatorRight));

    }

    /*
     * Hilfsprozeduren für das Zusammenfassen von Konstanten.
     */
    /**
     * Sammelt bei Addition konstante Ausdrücke in summands so weit wie möglich
     * nach vorne. Die Einträge in summands sind via 0, 1, 2, ..., size - 1
     * indiziert.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection collectConstantsAndConstantExpressionsInSum(ExpressionCollection summands) throws EvaluationException {

        ExpressionCollection result = new ExpressionCollection();
        summands = collectConstantsInSum(summands);
        int l = summands.getBound();
        if (summands.get(0).isIntegerConstantOrRationalConstant()) {
            result.add(summands.get(0));
            summands.remove(0);
        }
        for (int i = 0; i < l; i++) {
            if (summands.get(i) == null) {
                continue;
            }
            if (summands.get(i).isConstant() && !(summands.get(i) instanceof Constant)) {
                result.add(summands.get(i));
                summands.remove(i);
            }
        }
        for (int i = 0; i < l; i++) {
            if (summands.get(i) == null) {
                continue;
            }
            result.add(summands.get(i));
        }
        return result;

    }

    /**
     * Sammelt bei Multiplikation konstante Ausdrücke in h zu einem einzigen
     * konstanten Ausdruck. Die Einträge in h sind via 0, 1, 2, ..., size - 1
     * indiziert.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection collectConstantsAndConstantExpressionsInProduct(ExpressionCollection factors) throws EvaluationException {

        ExpressionCollection result = new ExpressionCollection();
        factors = collectConstantsInProduct(factors);
        if (factors.get(0).isIntegerConstantOrRationalConstant()) {
            result.add(factors.get(0));
            factors.remove(0);
        }
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null) {
                if (factors.get(i).isConstant() && !(factors.get(i) instanceof Constant)) {
                    result.add(factors.get(i));
                    factors.remove(i);
                }
            }
        }
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null) {
                result.add(factors.get(i));
            }
        }
        return result;

    }

    /**
     * Hilfsprozeduren für das Sortieren von Summen/Differenzen und
     * Produkten/Quotienten
     */
    public static void orderDifference(Expression expr, ExpressionCollection summandsLeft, ExpressionCollection summandsRight) {
        if (expr.isNotSum() && expr.isNotDifference()) {
            summandsLeft.add(expr);
            return;
        }
        if (expr.isSum()) {
            orderDifference(((BinaryOperation) expr).getLeft(), summandsLeft, summandsRight);
            orderDifference(((BinaryOperation) expr).getRight(), summandsLeft, summandsRight);
        } else {
            orderDifference(((BinaryOperation) expr).getLeft(), summandsLeft, summandsRight);
            orderDifference(((BinaryOperation) expr).getRight(), summandsRight, summandsLeft);
        }
    }

    /**
     * Hilfsprozeduren für das Sortieren von Summen/Differenzen und
     * Produkten/Quotienten
     */
    public static void orderQuotient(Expression expr, ExpressionCollection factorsNumerator, ExpressionCollection factorsDenominator) {
        if (expr.isNotProduct() && expr.isNotQuotient()) {
            factorsNumerator.add(expr);
            return;
        }
        if (expr.isProduct()) {
            orderQuotient(((BinaryOperation) expr).getLeft(), factorsNumerator, factorsDenominator);
            orderQuotient(((BinaryOperation) expr).getRight(), factorsNumerator, factorsDenominator);
        } else {
            orderQuotient(((BinaryOperation) expr).getLeft(), factorsNumerator, factorsDenominator);
            orderQuotient(((BinaryOperation) expr).getRight(), factorsDenominator, factorsNumerator);
        }
    }

}
