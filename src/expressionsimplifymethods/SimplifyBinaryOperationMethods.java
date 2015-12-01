package expressionsimplifymethods;

import computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import enumerations.TypeExpansion;
import exceptions.EvaluationException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.MINUS_ONE;
import static expressionbuilder.Expression.ONE;
import static expressionbuilder.Expression.ZERO;
import expressionbuilder.Function;
import expressionbuilder.TypeBinary;
import expressionbuilder.TypeFunction;
import flowcontroller.FlowController;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import translator.Translator;

/**
 * Viele kleine Einzelmethoden zur Vereinfachung von Ausdrücken der Klasse
 * BinaryOperation (für simplifyTrivial).
 */
public abstract class SimplifyBinaryOperationMethods {

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
                summandsLeft.put(i, summandsLeft.get(i).simplifyTrivial());
            }
            if (!(summandsLeft.get(i) instanceof Constant)) {
                allSummandsSimplifiedAreConstant = false;
            }
        }

        for (int i = 0; i < summandsRight.getBound(); i++) {
            if (summandsRight.get(i) != null) {
                summandsRight.put(i, summandsRight.get(i).simplifyTrivial());
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
            Expression left = ((BinaryOperation) expr).getLeft().simplifyTrivial();
            Expression right = ((BinaryOperation) expr).getRight().simplifyTrivial();
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
                factors.put(i, factors.get(i).simplifyTrivial());
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
            Expression left = ((BinaryOperation) expr).getLeft().simplifyTrivial();
            Expression right = ((BinaryOperation) expr).getRight().simplifyTrivial();
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
            Expression left = ((BinaryOperation) expr).getLeft().simplifyTrivial();
            Expression right = ((BinaryOperation) expr).getRight().simplifyTrivial();
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
                a = ((Constant) summandsLeft.get(0)).getValue().toBigInteger();
                b = BigInteger.ONE;
            } else {
                a = ((Constant) ((BinaryOperation) summandsLeft.get(0)).getLeft()).getValue().toBigInteger();
                b = ((Constant) ((BinaryOperation) summandsLeft.get(0)).getRight()).getValue().toBigInteger();
            }
            if (summandsRight.get(0).isIntegerConstant()) {
                c = ((Constant) summandsRight.get(0)).getValue().toBigInteger();
                d = BigInteger.ONE;
            } else {
                c = ((Constant) ((BinaryOperation) summandsRight.get(0)).getLeft()).getValue().toBigInteger();
                d = ((Constant) ((BinaryOperation) summandsRight.get(0)).getRight()).getValue().toBigInteger();
            }

            BigInteger enumerator = a.multiply(d).subtract(c.multiply(b));
            BigInteger denominator = b.multiply(d);
            BigInteger gcd = enumerator.gcd(denominator);
            enumerator = enumerator.divide(gcd);
            denominator = denominator.divide(gcd);

            summandsLeft.put(0, new Constant(enumerator).div(denominator));
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
                throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyBinaryOperationMethods_DIVISION_BY_ZERO"));
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
            double leftValue = expr.getLeft().simplifyTrivial().evaluate();
            if (leftValue < 0 && expr.getType().equals(TypeBinary.POW) && expr.getRight().isConstant()) {
                Expression exponent = expr.getRight().turnToPrecise();
                if (exponent.isRationalConstant() && expr.getRight().isRationalConstant()
                        && SimplifyAlgebraicExpressionMethods.isAdmissibleExponent(exponent)) {

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
     *
     * @throws EvaluationException
     */
    public static Expression rationalConstantToQuotient(BinaryOperation expr) throws EvaluationException {
        if (expr.isRationalConstant() && !expr.containsApproximates()) {
            return Constant.constantToQuotient(((Constant) expr.getLeft()).getValue(), ((Constant) expr.getRight()).getValue());
        }
        return expr;
    }

    /**
     * Falls expr einen Bruch mit negativen Nenner darstellt, so wird das
     * Minuszeichen in den Zähler verschoben. Ansonsten wird expr zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Expression eliminateNegativeDenominator(BinaryOperation expr) throws EvaluationException {

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
                    throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyBinaryOperationMethods_NEGATIVE_POWERS_OF_ZERO_NOT_DEFINED"));
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

            BigInteger enumerator = ((Constant) ((BinaryOperation) expr.getLeft()).getLeft()).getValue().toBigInteger();
            BigInteger denominator = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();
            BigInteger exponent = ((Constant) expr.getRight()).getValue().toBigInteger();

            if (expr.isPower()) {

                // Negative Potenzen von 0 sind nicht definiert.
                if (enumerator.equals(BigInteger.ZERO) && exponent.compareTo(BigInteger.ZERO) < 0) {
                    throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyBinaryOperationMethods_NEGATIVE_POWERS_OF_ZERO_NOT_DEFINED"));
                }
                /*
                 Potenzen von ganzen Zahlen sollen nur vereinfacht werden,
                 wenn die Basis >= 0 und der Exponent <= einer bestimmten
                 Schranke ist. Die Schranke für den Exponenten bewegt sich im
                 int-Bereich.
                 */
                if (exponent.compareTo(BigInteger.ZERO) >= 0
                        && exponent.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_POWER_OF_RATIONALS)) <= 0) {
                    return new Constant(enumerator.pow(exponent.intValue())).div(denominator.pow(exponent.intValue()));
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
                    throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyBinaryOperationMethods_ROOTS_OF_EVEN_ORDER_OF_NEGATIVE_NUMBERS_NOT_DEFINED"));
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
                throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyBinaryOperationMethods_ROOTS_OF_EVEN_ORDER_OF_NEGATIVE_NUMBERS_NOT_DEFINED"));
            }

        }

        return expr;

    }

    /**
     * Zieht, falls möglich, ein negatives Vorzeichen aus Wurzeln ungerader
     * Ordnung. Ansonsten wird expr zurückgegeben.<br>
     * BEISPIEL: bei expr = ((-7)*a)^(3/5) wird -(7*a)^(3/5)zurückgegeben, bei
     * expr = ((-7)*a)^(4/5) wird (7*a)^(4/5) zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Expression takeMinusSignOutOfOddRoots(BinaryOperation expr) throws EvaluationException {

        if (expr.isPower() && expr.isConstant() && !expr.containsApproximates()
                && !expr.getLeft().hasPositiveSign() && expr.getRight().isRationalConstant()) {

            /*
             In den folgenden Schritten ist die Anwendung von
             simplifyTrivial() wichtig, damit Koeffizienten sofort vollständig
             ausgerechnet werden und damit Potenzen später nicht wieder
             auseinandergezogen werden können (beispielsweise mit
             simplifyPowers() o. Ä.).
             */
            if (((BinaryOperation) expr.getRight()).getLeft().isEvenIntegerConstant()
                    && ((BinaryOperation) expr.getRight()).getRight().isOddIntegerConstant()) {
                return expr.getLeft().negate().pow(expr.getRight());
            }
            if (((BinaryOperation) expr.getRight()).getLeft().isOddIntegerConstant()
                    && ((BinaryOperation) expr.getRight()).getRight().isOddIntegerConstant()) {
                return MINUS_ONE.mult(expr.getLeft().negate().pow(expr.getRight()));
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

            BigInteger exponentEnumerator = ((Constant) ((BinaryOperation) expr.getRight()).getLeft()).getValue().toBigInteger();
            BigInteger exponentDenominator = ((Constant) ((BinaryOperation) expr.getRight()).getRight()).getValue().toBigInteger();

            BigInteger integerPartOfExponent = exponentEnumerator.divide(exponentDenominator);
            if (integerPartOfExponent.compareTo(BigInteger.ZERO) < 0) {
                integerPartOfExponent = integerPartOfExponent.subtract(BigInteger.ONE);
            }
            exponentEnumerator = exponentEnumerator.subtract(exponentDenominator.multiply(integerPartOfExponent));
            if (integerPartOfExponent.compareTo(BigInteger.ZERO) != 0 && integerPartOfExponent.abs().compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_POWER_OF_RATIONALS)) <= 0) {
                return expr.getLeft().pow(integerPartOfExponent).simplifyTrivial().mult(expr.getLeft().pow(exponentEnumerator, exponentDenominator));
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
             Wichtig: wird diese Methode von simplifyTrivial() aufgerufen, so
             werden keine Wurzeln gerader Ordnung aus negativen Zahlen
             gezogen, denn diese werden vorher in der Hauptmethode
             simplifyTrivial() aussortiert (EvaluationException wird
             geworfen). Bemerkung: Es werden nur Wurzeln bis zur Ordnung <=
             einer bestimmten Schranke exakt gezogen (analog zum Potenzieren).
             */
            ExpressionCollection factorsInExponentDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(expr.getRight());

            BigInteger rootDegree = BigInteger.ONE;
            if (factorsInExponentDenominator.get(0).isIntegerConstant()) {
                /*
                 factorsInExponentDenominator.get(0) sollte positiv sein, da 
                 negative Nenner in der Hauptprozedur simplifyTrivial() 
                 vorher positiv gemacht wurden. Die folgende Prüfung dient daher
                 nur zur Sicherheit.
                 */
                if (factorsInExponentDenominator.get(0).isNonPositive()) {
                    return expr;
                }
                rootDegree = ((Constant) factorsInExponentDenominator.get(0)).getValue().toBigInteger();
            }

            // 1. Fall: Basis ist eine ganze Zahl.
            if (expr.getLeft().isIntegerConstant()) {

                BigInteger base = ((Constant) expr.getLeft()).getValue().toBigInteger();

                if (rootDegree.compareTo(BigInteger.ONE) > 0 && rootDegree.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ARITHMETIC_MAX_ROOTDEGREE_OF_RATIONALS)) <= 0) {

                    HashMap<Integer, BigInteger> divisorsOfRootDegree = ArithmeticMethods.getDivisors(rootDegree);
                    int root;
                    for (int i = 0; i < divisorsOfRootDegree.size(); i++) {
                        root = divisorsOfRootDegree.get(i).intValue();
                        if (root == 1 || ((root / 2) * 2 == root && base.compareTo(BigInteger.ZERO) < 0)) {
                            // Es darf nicht versucht werden, Wurzeln gerader Ordnung aus negativen Zahlen zu ziehen.
                            continue;
                        }
                        BigInteger resultBase = ArithmeticMethods.sqrt(base, root);
                        if (resultBase.pow(root).compareTo(base) == 0) {

                            factorsInExponentDenominator.put(0, new Constant(rootDegree.divide(divisorsOfRootDegree.get(i))));
                            return new Constant(resultBase).pow(((BinaryOperation) expr.getRight()).getLeft().div(SimplifyUtilities.produceProduct(factorsInExponentDenominator)));

                        }
                    }

                }

            }
            if (expr.getLeft().isQuotient()) {
                // 2. Fall: Basis ist ein Quotient.
                if (((BinaryOperation) expr.getLeft()).getLeft().isIntegerConstant()) {

                    BigInteger baseEnumerator = ((Constant) ((BinaryOperation) expr.getLeft()).getLeft()).getValue().toBigInteger();

                    if (rootDegree.compareTo(BigInteger.ONE) > 0 && rootDegree.compareTo(BigInteger.valueOf(ComputationBounds.getBound("Bound_ROOTDEGREE_OF_RATIONALS"))) <= 0) {

                        HashMap<Integer, BigInteger> divisorsOfN = ArithmeticMethods.getDivisors(rootDegree);
                        int root;
                        for (int i = 0; i < divisorsOfN.size(); i++) {
                            root = divisorsOfN.get(i).intValue();
                            if (root == 1 || ((root / 2) * 2 == root && baseEnumerator.compareTo(BigInteger.ZERO) < 0)) {
                                /*
                                 Es darf nicht versucht werden, Wurzeln
                                 gerader Ordnung aus negativen Zahlen zu
                                 ziehen.
                                 */
                                continue;
                            }
                            BigInteger resultBaseEnumerator = ArithmeticMethods.sqrt(baseEnumerator, root);
                            if (resultBaseEnumerator.pow(root).compareTo(baseEnumerator) == 0) {

                                factorsInExponentDenominator.put(0, new Constant(rootDegree.divide(divisorsOfN.get(i))));
                                return new Constant(resultBaseEnumerator).pow(((BinaryOperation) expr.getRight()).getLeft().div(SimplifyUtilities.produceProduct(factorsInExponentDenominator))).div(
                                        (((BinaryOperation) expr.getLeft()).getRight()).pow(expr.getRight()));

                            }
                        }

                    }

                }
                if (((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {

                    BigInteger baseDenominator = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

                    if (rootDegree.compareTo(BigInteger.ONE) > 0 && rootDegree.compareTo(BigInteger.valueOf(ComputationBounds.getBound("Bound_ROOTDEGREE_OF_RATIONALS"))) <= 0) {

                        HashMap<Integer, BigInteger> divisorsOfN = ArithmeticMethods.getDivisors(rootDegree);
                        int root;
                        for (int i = 0; i < divisorsOfN.size(); i++) {
                            root = divisorsOfN.get(i).intValue();
                            if (root == 1 || ((root / 2) * 2 == root && baseDenominator.compareTo(BigInteger.ZERO) < 0)) {
                                /*
                                 Es darf nicht versucht werden, Wurzeln
                                 gerader Ordnung aus negativen Zahlen zu
                                 ziehen.
                                 */
                                continue;
                            }
                            BigInteger resultBaseDenominator = ArithmeticMethods.sqrt(baseDenominator, root);
                            if (resultBaseDenominator.pow(root).compareTo(baseDenominator) == 0) {

                                factorsInExponentDenominator.put(0, new Constant(rootDegree.divide(divisorsOfN.get(i))));
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
                BigDecimal constantEnumerator = ((Constant) ((BinaryOperation) expr.getLeft()).getLeft()).getValue();
                /*
                 Potenzen von Brüchen sollen nur vereinfacht werden, wenn
                 Exponenten <= einer bestimmten Schranke sind. Diese Schranke
                 bewegt sich im int-Bereich.
                 */
                if (((Constant) expr.getRight()).getValue().compareTo(BigDecimal.ZERO) >= 0
                        && ((Constant) expr.getRight()).getValue().compareTo(BigDecimal.valueOf(ComputationBounds.getBound("Bound_POWER_OF_RATIONALS"))) <= 0) {
                    constantEnumerator = constantEnumerator.pow(((Constant) expr.getRight()).getValue().intValue());
                    return new Constant(constantEnumerator).div(((BinaryOperation) expr.getLeft()).getRight().pow(expr.getRight()));
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
                        && ((Constant) expr.getRight()).getValue().compareTo(BigDecimal.valueOf(ComputationBounds.getBound("Bound_POWER_OF_RATIONALS"))) <= 0) {
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
            throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyBinaryOperationMethods_DIVISION_BY_ZERO"));
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
                throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyBinaryOperationMethods_NEGATIVE_POWERS_OF_ZERO_NOT_DEFINED"));
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
     * vereinfacht: (1) x + c*y = x - (-c)*y, falls c < 0 (2) x - c*y = x +
     * (-c)*y, falls c < 0.
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
    public static void pullMinusSignFromProductOrQuotientsWithCompleteNegativeSums(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator) {

        ExpressionCollection summandsLeft, summandsRight;
        boolean allSignsAreNegative;
        /* signAtTheEnd = true bedeutet, dass am Ende der Methode der Ausdruck ein 
         positives Vorzeichen besitzt, andernfalls ein negatives.
         */
        boolean signAtTheEnd = true;

        // Zähler durchsuchen.
        if (factorsEnumerator.getSize() > 1) {
            // Umformung nur bei echten Produkten vornehmen!
            for (int i = 0; i < factorsEnumerator.getBound(); i++) {

                if (factorsEnumerator.get(i) == null) {
                    continue;
                }

                summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(factorsEnumerator.get(i));
                summandsRight = SimplifyUtilities.getSummandsRightInExpression(factorsEnumerator.get(i));
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
                    factorsEnumerator.put(i, SimplifyUtilities.produceDifference(summandsLeft, summandsRight));
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
            factorsEnumerator.add(MINUS_ONE);
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
                return ((BinaryOperation) expr.getLeft()).getLeft().pow(((BinaryOperation) expr.getLeft()).getRight().mult(expr.getRight()).simplifyTrivial());
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
                BigInteger a = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getRight()).getLeft()).getValue().toBigInteger();
                BigInteger b = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getRight()).getRight()).getValue().toBigInteger();
                BigInteger c, d;
                if (expr.getRight().isIntegerConstant()) {
                    c = ((Constant) expr.getRight()).getValue().toBigInteger();
                    d = BigInteger.ONE;
                } else {
                    c = ((Constant) ((BinaryOperation) expr.getRight()).getLeft()).getValue().toBigInteger();
                    d = ((Constant) ((BinaryOperation) expr.getRight()).getRight()).getValue().toBigInteger();
                }

                BigInteger exponentEnumerator = a.multiply(c);
                BigInteger exponentDenominator = b.multiply(d);
                BigInteger gcdOfEnumeratorAndDenominator = exponentEnumerator.gcd(exponentDenominator);
                exponentDenominator = exponentDenominator.divide(gcdOfEnumeratorAndDenominator);
                if (b.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO) && !exponentDenominator.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
                    return expr;
                }

            }
            // Ansonsten einfach nur Exponenten ausmultiplizieren.
            return ((BinaryOperation) expr.getLeft()).getLeft().pow(((BinaryOperation) expr.getLeft()).getRight().mult(expr.getRight()).simplifyTrivial());
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
     * @throws EvaluationException.
     */
    public static void reduceLeadingCoefficientsInQuotientInApprox(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator) throws EvaluationException {

        // Prüfen, ob Approximationen vorliegen.
        boolean approximatesFound = false;
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i) == null) {
                continue;
            }
            if (factorsEnumerator.get(i).containsApproximates()) {
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

        if (((factorsEnumerator.get(0) == null) || !factorsEnumerator.get(0).isConstant())
                && factorsDenominator.get(0) != null && factorsDenominator.get(0).isConstant()) {

            double coefficientDenominator = factorsDenominator.get(0).evaluate();

            if (factorsEnumerator.get(0) == null) {
                factorsEnumerator.put(0, new Constant(1 / coefficientDenominator));
            } else {
                factorsEnumerator.put(0, new Constant(1 / coefficientDenominator).mult(factorsEnumerator.get(0)));
            }
            factorsDenominator.remove(0);

        } else if (factorsEnumerator.get(0) != null && factorsEnumerator.get(0).isConstant()
                && factorsDenominator.get(0) != null && factorsDenominator.get(0).isConstant()) {

            double coefficientEnumerator = factorsEnumerator.get(0).evaluate();
            double coefficientDenominator = factorsDenominator.get(0).evaluate();
            factorsEnumerator.put(0, new Constant(coefficientEnumerator / coefficientDenominator));
            factorsDenominator.remove(0);

        }

    }

    /**
     * Methode zum Kürzen von Leitkoeffizienten in Quotienten (für
     * reduceLeadingCoefficients), falls der Ausdruck NICHT approximiert,
     * sondern exakt berechnet werden soll.
     */
    public static void reduceLeadingCoefficientsInQuotient(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator) {

        // Prüfen, ob Approximationen vorliegen.
        boolean approximatesFound = false;
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i) == null) {
                continue;
            }
            if (factorsEnumerator.get(i).containsApproximates()) {
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
        if (factorsEnumerator.get(0) != null && factorsEnumerator.get(0) instanceof Constant
                && factorsDenominator.get(0) != null && factorsDenominator.get(0) instanceof Constant) {

            BigDecimal enumerator = ((Constant) factorsEnumerator.get(0)).getValue();
            BigDecimal denominator = ((Constant) factorsDenominator.get(0)).getValue();
            BigInteger[] reducedFraction = Constant.reduceFraction(enumerator, denominator);

            factorsEnumerator.put(0, new Constant(reducedFraction[0]));
            factorsDenominator.put(0, new Constant(reducedFraction[1]));

        } else if ((factorsEnumerator.get(0) == null || !(factorsEnumerator.get(0) instanceof Constant))
                && factorsDenominator.get(0) != null && factorsDenominator.get(0) instanceof Constant) {

            // v1 / (c * v2) = -v1 / (-c * v2), falls c < 0.
            BigDecimal coefficientDenominator = ((Constant) factorsDenominator.get(0)).getValue();
            if (coefficientDenominator.compareTo(BigDecimal.ZERO) < 0) {

                if (factorsEnumerator.get(0) == null) {
                    factorsEnumerator.put(0, MINUS_ONE);
                } else {
                    factorsEnumerator.put(0, MINUS_ONE.mult(factorsEnumerator.get(0)));
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
                BigDecimal enumerator = c_1.subtract(c_2.multiply(c_3));
                if (enumerator.multiply(c_3).compareTo(BigDecimal.ZERO) > 0) {
                    summandsLeft.put(0, new Constant(enumerator).div(c_2));
                    summandsRight.remove(0);
                } else if (enumerator.multiply(c_3).compareTo(BigDecimal.ZERO) < 0) {
                    summandsLeft.remove(0);
                    summandsRight.put(0, new Constant(enumerator.negate()).div(c_2));
                } else {
                    summandsLeft.remove(0);
                    summandsRight.remove(0);
                }

            } else if (summandsLeft.get(0) instanceof Constant && !(summandsRight.get(0) instanceof Constant)) {

                BigDecimal c_1 = ((Constant) summandsLeft.get(0)).getValue();
                BigDecimal c_2 = ((Constant) ((BinaryOperation) summandsRight.get(0)).getLeft()).getValue();
                BigDecimal c_3 = ((Constant) ((BinaryOperation) summandsRight.get(0)).getRight()).getValue();
                BigDecimal enumerator = c_1.multiply(c_3).subtract(c_2);
                if (enumerator.multiply(c_3).compareTo(BigDecimal.ZERO) > 0) {
                    summandsLeft.put(0, new Constant(enumerator).div(c_3));
                    summandsRight.remove(0);
                } else if (enumerator.multiply(c_3).compareTo(BigDecimal.ZERO) < 0) {
                    summandsLeft.remove(0);
                    summandsRight.put(0, new Constant(enumerator.negate()).div(c_3));
                } else {
                    summandsLeft.remove(0);
                    summandsRight.remove(0);
                }

            } else {

                BigDecimal c_1 = ((Constant) ((BinaryOperation) summandsLeft.get(0)).getLeft()).getValue();
                BigDecimal c_2 = ((Constant) ((BinaryOperation) summandsLeft.get(0)).getRight()).getValue();
                BigDecimal c_3 = ((Constant) ((BinaryOperation) summandsRight.get(0)).getLeft()).getValue();
                BigDecimal c_4 = ((Constant) ((BinaryOperation) summandsRight.get(0)).getRight()).getValue();
                BigDecimal enumerator = c_1.multiply(c_4).subtract(c_2.multiply(c_3));
                BigDecimal denominator = c_2.multiply(c_4);
                if (enumerator.multiply(denominator).compareTo(BigDecimal.ZERO) > 0) {
                    summandsLeft.put(0, new Constant(enumerator).div(denominator));
                    summandsRight.remove(0);
                } else if (enumerator.multiply(denominator).compareTo(BigDecimal.ZERO) < 0) {
                    summandsLeft.remove(0);
                    summandsRight.put(0, new Constant(enumerator.negate()).div(denominator));
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
    public static void reduceGCDInQuotient(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator) {

        Expression enumerator, denominator;
        Expression[] reducedEnumeratorAndDenominator;

        for (int i = 0; i < factorsEnumerator.getBound(); i++) {

            if (factorsEnumerator.get(i) == null) {
                continue;
            }

            enumerator = factorsEnumerator.get(i);
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
                if (enumerator.isNotPower()) {

                    reducedEnumeratorAndDenominator = reduceGCDInEnumeratorAndDenominator(enumerator, denominator);
                    if (!reducedEnumeratorAndDenominator[0].equivalent(enumerator)) {
                        // Dann KONNTE etwas gekürzt werden!
                        if (!reducedEnumeratorAndDenominator[1].equals(Expression.ONE)) {
                            factorsEnumerator.put(i, reducedEnumeratorAndDenominator[0]);
                            factorsDenominator.put(j, reducedEnumeratorAndDenominator[1]);
                        } else {
                            factorsEnumerator.put(i, reducedEnumeratorAndDenominator[0]);
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
                if (enumerator.isPower() && denominator.isPower()
                        && ((BinaryOperation) enumerator).getRight().equivalent(((BinaryOperation) denominator).getRight())) {
                    if ((((BinaryOperation) enumerator).getLeft().isSum() || ((BinaryOperation) enumerator).getLeft().isDifference())
                            && (((BinaryOperation) denominator).getLeft().isSum() || ((BinaryOperation) denominator).getLeft().isDifference())) {

                        reducedEnumeratorAndDenominator = reduceGCDInEnumeratorAndDenominator(((BinaryOperation) enumerator).getLeft(), ((BinaryOperation) denominator).getLeft());
                        if (!reducedEnumeratorAndDenominator[0].equivalent(((BinaryOperation) enumerator).getLeft())) {
                            /*
                             Es konnte mindestens ein Faktor im Zähler gegen
                             einen Faktor im Nenner gekürzt werden.
                             */
                            if (!reducedEnumeratorAndDenominator[1].equals(Expression.ONE)) {
                                factorsEnumerator.put(i, reducedEnumeratorAndDenominator[0].pow(((BinaryOperation) enumerator).getRight()));
                                factorsDenominator.put(j, reducedEnumeratorAndDenominator[1].pow(((BinaryOperation) enumerator).getRight()));
                            } else {
                                factorsEnumerator.put(i, reducedEnumeratorAndDenominator[0].pow(((BinaryOperation) enumerator).getRight()));
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
     * Hilfsmethode für reduceGCDInQuotient(). Kürzt ganzzahlige Faktoren aus
     * Brüchen, z. B. wird (25*x + 10*y)/(80*u - 35*v) zu (5*x + 2*y)/(16*u -
     * 7*v) vereinfacht. WICHTIG: Zähler oder Nenner dürfen keine Potenzen sein
     * (dies wird aber in reduceGCDInFactorsInEnumeratorAndDenominator()
     * ausgeschlossen).
     */
    private static Expression[] reduceGCDInEnumeratorAndDenominator(Expression enumerator, Expression denominator) {

        ExpressionCollection summandsLeftInEnumerator = SimplifyUtilities.getSummandsLeftInExpression(enumerator);
        ExpressionCollection summandsRightInEnumerator = SimplifyUtilities.getSummandsRightInExpression(enumerator);
        ExpressionCollection summandsLeftInDenominator = SimplifyUtilities.getSummandsLeftInExpression(denominator);
        ExpressionCollection summandsRightInDenominator = SimplifyUtilities.getSummandsRightInExpression(denominator);

        ExpressionCollection enumerators = new ExpressionCollection();
        BigInteger gcdOfAllCoefficients = BigInteger.ZERO;
        boolean containsConstant;

        // Zähler absuchen (Minuend)
        for (int i = 0; i < summandsLeftInEnumerator.getBound(); i++) {

            containsConstant = false;
            enumerators.clear();
            enumerators = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsLeftInEnumerator.get(i));
            for (int j = 0; j < enumerators.getBound(); j++) {

                if (enumerators.get(j).isIntegerConstant()) {
                    containsConstant = true;
                    if (gcdOfAllCoefficients.equals(BigInteger.ZERO)) {
                        gcdOfAllCoefficients = ((Constant) enumerators.get(j)).getValue().toBigInteger();
                        break;
                    } else {
                        gcdOfAllCoefficients = gcdOfAllCoefficients.gcd(((Constant) enumerators.get(j)).getValue().toBigInteger());
                        break;
                    }
                }
                if (gcdOfAllCoefficients.equals(BigInteger.ONE)) {
                    // Es kann dann nichts gekürzt werden.
                    Expression[] result = new Expression[2];
                    result[0] = enumerator;
                    result[1] = denominator;
                    return result;
                }

            }
            if (!containsConstant) {
                Expression[] result = new Expression[2];
                result[0] = enumerator;
                result[1] = denominator;
                return result;
            }

        }
        // Zähler absuchen (Subtrahend)
        for (int i = 0; i < summandsRightInEnumerator.getBound(); i++) {

            containsConstant = false;
            enumerators.clear();
            enumerators = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsRightInEnumerator.get(i));
            for (int j = 0; j < enumerators.getBound(); j++) {

                if (enumerators.get(j).isIntegerConstant()) {
                    containsConstant = true;
                    if (gcdOfAllCoefficients.equals(BigInteger.ZERO)) {
                        gcdOfAllCoefficients = ((Constant) enumerators.get(j)).getValue().toBigInteger();
                        break;
                    } else {
                        gcdOfAllCoefficients = gcdOfAllCoefficients.gcd(((Constant) enumerators.get(j)).getValue().toBigInteger());
                        break;
                    }
                }
                if (gcdOfAllCoefficients.equals(BigInteger.ONE)) {
                    // Es kann dann nichts gekürzt werden.
                    Expression[] result = new Expression[2];
                    result[0] = enumerator;
                    result[1] = denominator;
                    return result;
                }

            }
            if (!containsConstant) {
                Expression[] result = new Expression[2];
                result[0] = enumerator;
                result[1] = denominator;
                return result;
            }

        }

        // Nenner absuchen (Minuend)
        for (int i = 0; i < summandsLeftInDenominator.getBound(); i++) {

            containsConstant = false;
            enumerators.clear();
            enumerators = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsLeftInDenominator.get(i));
            for (int j = 0; j < enumerators.getBound(); j++) {

                if (enumerators.get(j).isIntegerConstant()) {
                    containsConstant = true;
                    if (gcdOfAllCoefficients.equals(BigInteger.ZERO)) {
                        gcdOfAllCoefficients = ((Constant) enumerators.get(j)).getValue().toBigInteger();
                        break;
                    } else {
                        gcdOfAllCoefficients = gcdOfAllCoefficients.gcd(((Constant) enumerators.get(j)).getValue().toBigInteger());
                        break;
                    }
                }
                if (gcdOfAllCoefficients.equals(BigInteger.ONE)) {
                    // Es kann dann nichts gekürzt werden.
                    Expression[] result = new Expression[2];
                    result[0] = enumerator;
                    result[1] = denominator;
                    return result;
                }

            }
            if (!containsConstant) {
                Expression[] result = new Expression[2];
                result[0] = enumerator;
                result[1] = denominator;
                return result;
            }

        }
        // Nenner absuchen (Subtrahend)
        for (int i = 0; i < summandsRightInDenominator.getBound(); i++) {

            containsConstant = false;
            enumerators.clear();
            enumerators = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsRightInDenominator.get(i));
            for (int j = 0; j < enumerators.getBound(); j++) {

                if (enumerators.get(j).isIntegerConstant()) {
                    containsConstant = true;
                    if (gcdOfAllCoefficients.equals(BigInteger.ZERO)) {
                        gcdOfAllCoefficients = ((Constant) enumerators.get(j)).getValue().toBigInteger();
                        break;
                    } else {
                        gcdOfAllCoefficients = gcdOfAllCoefficients.gcd(((Constant) enumerators.get(j)).getValue().toBigInteger());
                        break;
                    }
                }
                if (gcdOfAllCoefficients.equals(BigInteger.ONE)) {
                    // Es kann dann nichts gekürzt werden.
                    Expression[] result = new Expression[2];
                    result[0] = enumerator;
                    result[1] = denominator;
                    return result;
                }

            }
            if (!containsConstant) {
                Expression[] result = new Expression[2];
                result[0] = enumerator;
                result[1] = denominator;
                return result;
            }

        }

        if (gcdOfAllCoefficients.equals(BigInteger.ONE)) {
            // Es kann dann nichts gekürzt werden.
            Expression[] result = new Expression[2];
            result[0] = enumerator;
            result[1] = denominator;
            return result;
        }

        /*
         Hier angelangt kann a nicht 1 sein! Jetzt: Alle Summanden im Zähler 
         und Nenner durch a kürzen. Die einzelnen Summanden müssen noch 
         vereinfacht werden.
         */
        ExpressionCollection denominators = new ExpressionCollection();
        for (int i = 0; i < summandsLeftInEnumerator.getBound(); i++) {

            enumerators.clear();
            enumerators = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsLeftInEnumerator.get(i));
            denominators = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeftInEnumerator.get(i));
            for (int j = 0; j < enumerators.getBound(); j++) {

                /*
                 Falls Konstanten als Faktoren auftauchen, dann (NUR EINEN)
                 Faktor durch a teilen und neuen Summanden als Produkt der so
                 entstandenen Faktoren bilden.
                 */
                if (enumerators.get(j).isIntegerConstant()) {
                    enumerators.put(j, new Constant(((Constant) enumerators.get(j)).getValue().toBigInteger().divide(gcdOfAllCoefficients)));
                    summandsLeftInEnumerator.put(i, SimplifyUtilities.produceQuotient(enumerators, denominators));
                    break;
                }

            }

        }
        for (int i = 0; i < summandsRightInEnumerator.getBound(); i++) {

            enumerators.clear();
            denominators.clear();
            enumerators = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsRightInEnumerator.get(i));
            denominators = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRightInEnumerator.get(i));
            for (int j = 0; j < enumerators.getBound(); j++) {

                /*
                 Falls Konstanten als Faktoren auftauchen, dann (NUR EINEN)
                 Faktor durch a teilen und neuen Summanden als Produkt der so
                 entstandenen Faktoren bilden.
                 */
                if (enumerators.get(j).isIntegerConstant()) {
                    enumerators.put(j, new Constant(((Constant) enumerators.get(j)).getValue().toBigInteger().divide(gcdOfAllCoefficients)));
                    summandsRightInEnumerator.put(i, SimplifyUtilities.produceQuotient(enumerators, denominators));
                    break;
                }

            }

        }
        for (int i = 0; i < summandsLeftInDenominator.getBound(); i++) {

            enumerators.clear();
            denominators.clear();
            enumerators = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsLeftInDenominator.get(i));
            denominators = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeftInDenominator.get(i));
            for (int j = 0; j < enumerators.getBound(); j++) {

                /*
                 Falls Konstanten als Faktoren auftauchen, dann (NUR EINEN)
                 Faktor durch a teilen und neuen Summanden als Produkt der so
                 entstandenen Faktoren bilden.
                 */
                if (enumerators.get(j).isIntegerConstant()) {
                    enumerators.put(j, new Constant(((Constant) enumerators.get(j)).getValue().toBigInteger().divide(gcdOfAllCoefficients)));
                    summandsLeftInDenominator.put(i, SimplifyUtilities.produceQuotient(enumerators, denominators));
                    break;
                }

            }

        }
        for (int i = 0; i < summandsRightInDenominator.getBound(); i++) {

            enumerators.clear();
            denominators.clear();
            enumerators = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsRightInDenominator.get(i));
            denominators = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRightInDenominator.get(i));
            for (int j = 0; j < enumerators.getBound(); j++) {

                /*
                 Falls Konstanten als Faktoren auftauchen, dann (NUR EINEN)
                 Faktor durch a teilen und neuen Summanden als Produkt der so
                 entstandenen Faktoren bilden.
                 */
                if (enumerators.get(j).isIntegerConstant()) {
                    enumerators.put(j, new Constant(((Constant) enumerators.get(j)).getValue().toBigInteger().divide(gcdOfAllCoefficients)));
                    summandsRightInDenominator.put(i, SimplifyUtilities.produceQuotient(enumerators, denominators));
                    break;
                }

            }

        }

        Expression[] reducedEnumeratorAndDenominator = new Expression[2];
        reducedEnumeratorAndDenominator[0] = SimplifyUtilities.produceDifference(summandsLeftInEnumerator, summandsRightInEnumerator);
        reducedEnumeratorAndDenominator[1] = SimplifyUtilities.produceDifference(summandsLeftInDenominator, summandsRightInDenominator);
        return reducedEnumeratorAndDenominator;

    }

    /**
     * Macht aus Summen/Differenzen von Brüchen einen einzigen. Beispiel: a/b +
     * c/5 = (5*a + b*c)/(5*b) oder x/a+y/a^2 = (x*a + y)/a^2.
     *
     * @throws EvaluationException
     */
    public static Expression bringFractionToCommonDenominator(BinaryOperation expr) throws EvaluationException {

        if (expr.isNotSum() && expr.isNotDifference()) {
            return expr;
        }

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);
        ExpressionCollection commonDenominators;
        ExpressionCollection additionalDenominators;
        Expression baseOfFactorInCommonDenominators, baseOfFactorInCurrentDenominators;
        BigInteger exponentOfFactorInCommonDenominators, exponentOfFactorInCurrentDenominators;

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
                        exponentOfFactorInCommonDenominators = ((Constant) ((BinaryOperation) commonDenominators.get(k)).getRight()).getValue().toBigInteger();
                    } else {
                        baseOfFactorInCommonDenominators = commonDenominators.get(k);
                        exponentOfFactorInCommonDenominators = BigInteger.ONE;
                    }
                    if (additionalDenominators.get(j).isPower()
                            && ((BinaryOperation) additionalDenominators.get(j)).getRight().isIntegerConstant()
                            && ((BinaryOperation) additionalDenominators.get(j)).getRight().isNonNegative()) {
                        baseOfFactorInCurrentDenominators = ((BinaryOperation) additionalDenominators.get(j)).getLeft();
                        exponentOfFactorInCurrentDenominators = ((Constant) ((BinaryOperation) additionalDenominators.get(j)).getRight()).getValue().toBigInteger();
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
                        exponentOfFactorInCommonDenominators = ((Constant) ((BinaryOperation) commonDenominators.get(k)).getRight()).getValue().toBigInteger();
                    } else {
                        baseOfFactorInCommonDenominators = commonDenominators.get(k);
                        exponentOfFactorInCommonDenominators = BigInteger.ONE;
                    }
                    if (additionalDenominators.get(j).isPower()
                            && ((BinaryOperation) additionalDenominators.get(j)).getRight().isIntegerConstant()
                            && ((BinaryOperation) additionalDenominators.get(j)).getRight().isNonNegative()) {
                        baseOfFactorInCurrentDenominators = ((BinaryOperation) additionalDenominators.get(j)).getLeft();
                        exponentOfFactorInCurrentDenominators = ((Constant) ((BinaryOperation) additionalDenominators.get(j)).getRight()).getValue().toBigInteger();
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
        int l_commonDenominators = commonDenominators.getBound();
        int l_currentDenominators;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            commonDenominatorsCopy = ExpressionCollection.copy(commonDenominators);
            complementFactorsForEachSummand.clear();
            additionalDenominators = SimplifyUtilities.collectFactorsByPowers(SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(i)));
            l_currentDenominators = additionalDenominators.getBound();
            for (int j = 0; j < l_commonDenominators; j++) {

                if (commonDenominatorsCopy.get(j) == null) {
                    continue;
                }
                if (commonDenominatorsCopy.get(j).isPower()
                        && ((BinaryOperation) commonDenominatorsCopy.get(j)).getRight().isIntegerConstant()
                        && ((BinaryOperation) commonDenominatorsCopy.get(j)).getRight().isNonNegative()) {
                    baseOfFactorInCommonDenominators = ((BinaryOperation) commonDenominatorsCopy.get(j)).getLeft();
                    exponentOfFactorInCommonDenominators = ((Constant) ((BinaryOperation) commonDenominatorsCopy.get(j)).getRight()).getValue().toBigInteger();
                } else {
                    baseOfFactorInCommonDenominators = commonDenominatorsCopy.get(j);
                    exponentOfFactorInCommonDenominators = BigInteger.ONE;
                }

                factorOccursInCurrentDenominators = false;

                for (int k = 0; k < l_currentDenominators; k++) {
                    if (additionalDenominators.get(k) == null) {
                        continue;
                    }

                    if (additionalDenominators.get(k).isPower()
                            && ((BinaryOperation) additionalDenominators.get(k)).getRight().isIntegerConstant()
                            && ((BinaryOperation) additionalDenominators.get(k)).getRight().isNonNegative()) {
                        baseOfFactorInCurrentDenominators = ((BinaryOperation) additionalDenominators.get(k)).getLeft();
                        exponentOfFactorInCurrentDenominators = ((Constant) ((BinaryOperation) additionalDenominators.get(k)).getRight()).getValue().toBigInteger();
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
                        SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsLeft.get(i)))).simplify());
            }

        }

        for (int i = 0; i < summandsRight.getBound(); i++) {

            commonDenominatorsCopy = ExpressionCollection.copy(commonDenominators);
            complementFactorsForEachSummand.clear();
            additionalDenominators = SimplifyUtilities.collectFactorsByPowers(SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(i)));
            l_currentDenominators = additionalDenominators.getBound();
            for (int j = 0; j < l_commonDenominators; j++) {

                if (commonDenominatorsCopy.get(j) == null) {
                    continue;
                }
                if (commonDenominatorsCopy.get(j).isPower()
                        && ((BinaryOperation) commonDenominatorsCopy.get(j)).getRight().isIntegerConstant()
                        && ((BinaryOperation) commonDenominatorsCopy.get(j)).getRight().isNonNegative()) {
                    baseOfFactorInCommonDenominators = ((BinaryOperation) commonDenominatorsCopy.get(j)).getLeft();
                    exponentOfFactorInCommonDenominators = ((Constant) ((BinaryOperation) commonDenominatorsCopy.get(j)).getRight()).getValue().toBigInteger();
                } else {
                    baseOfFactorInCommonDenominators = commonDenominatorsCopy.get(j);
                    exponentOfFactorInCommonDenominators = BigInteger.ONE;
                }

                factorOccursInCurrentDenominators = false;

                for (int k = 0; k < l_currentDenominators; k++) {
                    if (additionalDenominators.get(k) == null) {
                        continue;
                    }

                    if (additionalDenominators.get(k).isPower()
                            && ((BinaryOperation) additionalDenominators.get(k)).getRight().isIntegerConstant()
                            && ((BinaryOperation) additionalDenominators.get(k)).getRight().isNonNegative()) {
                        baseOfFactorInCurrentDenominators = ((BinaryOperation) additionalDenominators.get(k)).getLeft();
                        exponentOfFactorInCurrentDenominators = ((Constant) ((BinaryOperation) additionalDenominators.get(k)).getRight()).getValue().toBigInteger();
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
                        SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsRight.get(i)))).simplify());
            }

        }

        return SimplifyUtilities.produceDifference(summandsLeft, summandsRight).div(SimplifyUtilities.produceProduct(commonDenominators));

    }

    /**
     * Prüft, ob Faktoren im Zähler und im Nenner zu einer Konstante gekürzt
     * werden können.
     *
     * @throws EvaluationException
     */
    public static void reduceFactorsInEnumeratorAndFactorInDenominatorToConstant(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator) throws EvaluationException {

        Expression factorEnumerator, factorDenominator;
        Expression[] reducedFactor;

        for (int i = 0; i < factorsEnumerator.getBound(); i++) {

            if (factorsEnumerator.get(i) == null) {
                continue;
            }

            factorEnumerator = factorsEnumerator.get(i);
            for (int j = 0; j < factorsDenominator.getBound(); j++) {

                if (factorsDenominator.get(j) == null) {
                    continue;
                }

                factorDenominator = factorsDenominator.get(j);
                if ((factorEnumerator.isSum() || factorEnumerator.isDifference())
                        && (factorDenominator.isSum() || factorDenominator.isDifference())) {

                    reducedFactor = reduceEnumeratorAndDenominatorToConstant(factorEnumerator, factorDenominator);
                    if (!reducedFactor[0].equivalent(factorEnumerator)) {
                        // Es konnte mindestens ein Faktor im Zähler gegen einen Faktor im Nenner gekürzt werden.
                        if (!reducedFactor[1].equals(ONE)) {
                            factorsEnumerator.put(i, reducedFactor[0]);
                            factorsDenominator.put(j, reducedFactor[1]);
                        } else {
                            factorsEnumerator.put(i, reducedFactor[0]);
                            factorsDenominator.remove(j);
                        }
                    }

                }
                /*
                 Sonderfall: Zähler und Nenner sind von der Form (a*expr)^k,
                 (b*expr)^k, mit rationalen a, b. Dann zu (a/b)^k kürzen.
                 */
                if (factorEnumerator.isPower() && factorDenominator.isPower()
                        && ((BinaryOperation) factorEnumerator).getRight().equivalent(((BinaryOperation) factorDenominator).getRight())) {
                    if ((((BinaryOperation) factorEnumerator).getLeft().isSum() || ((BinaryOperation) factorEnumerator).getLeft().isDifference())
                            && (((BinaryOperation) factorDenominator).getLeft().isSum() || ((BinaryOperation) factorDenominator).getLeft().isDifference())) {

                        reducedFactor = reduceEnumeratorAndDenominatorToConstant(((BinaryOperation) factorEnumerator).getLeft(), ((BinaryOperation) factorDenominator).getLeft());
                        if (!reducedFactor[0].equivalent(((BinaryOperation) factorEnumerator).getLeft())) {
                            // Es konnte mindestens ein Faktor im Zähler gegen einen Faktor im Nenner gekürzt werden.
                            factorsEnumerator.put(i, reducedFactor[0].pow(((BinaryOperation) factorEnumerator).getRight()));
                            factorsDenominator.put(j, reducedFactor[1].pow(((BinaryOperation) factorEnumerator).getRight()));
                        }

                    }
                }

            }

        }

    }

    /**
     * Hilfsmethode für
     * reduceFactorsInEnumeratorAndFactorInDenominatorToConstant(). Falls der
     * Zähler ein konstantes Vielfaches vom Nenner ist, so wird der gekürzte
     * Quotient zurückgegeben. Ansonsten wird ein Array bestehend aus zwei
     * Elementen, enumerator und denominator, wieder zurückgegeben. Z.B. (3*a +
     * 7*b)/(12*a + 28*b) = 1/4. Zähler und Nenner werden hier in Form von
     * HashMaps mit Summanden (Minuenden und Subtrahenden) als Einträgen
     * angegeben.
     *
     * @throws EvaluationException
     */
    private static Expression[] reduceEnumeratorAndDenominatorToConstant(Expression enumerator,
            Expression denominator) throws EvaluationException {

        ExpressionCollection summandsLeftInEnumerator = SimplifyUtilities.getSummandsLeftInExpression(enumerator);
        ExpressionCollection summandsRightInEnumerator = SimplifyUtilities.getSummandsRightInExpression(enumerator);
        ExpressionCollection summandsLeftInDenominator = SimplifyUtilities.getSummandsLeftInExpression(denominator);
        ExpressionCollection summandsRightInDenominator = SimplifyUtilities.getSummandsRightInExpression(denominator);

        if ((summandsLeftInEnumerator.getBound() != summandsLeftInDenominator.getBound()
                || summandsRightInEnumerator.getBound() != summandsRightInDenominator.getBound())
                && (summandsLeftInEnumerator.getBound() != summandsRightInDenominator.getBound()
                || summandsRightInEnumerator.getBound() != summandsLeftInDenominator.getBound())) {
            // Dann gibt es keine Chance, dass Zähler und Nenner zu einer Konstante gekürzt werden können.
            Expression[] result = new Expression[2];
            result[0] = enumerator;
            result[1] = denominator;
            return result;
        }

        Expression coefficientInEnumeratorForTesting, coefficientInDenominatorForTesting;
        Expression summandInEnumeratorForTesting, summandInDenominatorForTesting;
        ExpressionCollection summandsLeftInEnumeratorMultiples;
        ExpressionCollection summandsRightInEnumeratorMultiples;
        ExpressionCollection summandsLeftInDenominatorMultiples;
        ExpressionCollection summandsRightInDenominatorMultiples;

        // In summandsLeftInEnumerator nach passendem Testsummanden suchen.
        for (Expression summandEnumerator : summandsLeftInEnumerator) {

            if (summandEnumerator.isConstant()) {
                continue;
            }

            summandInEnumeratorForTesting = SimplifyUtilities.produceQuotient(
                    SimplifyUtilities.getNonConstantFactorsOfEnumeratorInExpression(summandEnumerator),
                    SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(summandEnumerator));

            for (Expression summandDenominator : summandsLeftInDenominator) {
                if (summandDenominator.isConstant()) {
                    continue;
                }

                summandInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                        SimplifyUtilities.getNonConstantFactorsOfEnumeratorInExpression(summandDenominator),
                        SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(summandDenominator));

                // Passende Testsummanden im Zähler und im Nenner gefunden.
                if (summandInEnumeratorForTesting.equivalent(summandInDenominatorForTesting)) {

                    coefficientInEnumeratorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfEnumeratorInExpression(summandEnumerator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandEnumerator));
                    coefficientInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfEnumeratorInExpression(summandDenominator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandDenominator));

                    summandsLeftInEnumeratorMultiples = ExpressionCollection.copy(summandsLeftInEnumerator);
                    summandsLeftInEnumeratorMultiples.multExpression(coefficientInDenominatorForTesting);
                    summandsLeftInEnumeratorMultiples = summandsLeftInEnumeratorMultiples.simplify();

                    summandsRightInEnumeratorMultiples = ExpressionCollection.copy(summandsRightInEnumerator);
                    summandsRightInEnumeratorMultiples.multExpression(coefficientInDenominatorForTesting);
                    summandsRightInEnumeratorMultiples = summandsRightInEnumeratorMultiples.simplify();

                    summandsLeftInDenominatorMultiples = ExpressionCollection.copy(summandsLeftInDenominator);
                    summandsLeftInDenominatorMultiples.multExpression(coefficientInEnumeratorForTesting);
                    summandsLeftInDenominatorMultiples = summandsLeftInDenominatorMultiples.simplify();

                    summandsRightInDenominatorMultiples = ExpressionCollection.copy(summandsRightInDenominator);
                    summandsRightInDenominatorMultiples.multExpression(coefficientInEnumeratorForTesting);
                    summandsRightInDenominatorMultiples = summandsRightInDenominatorMultiples.simplify();

                    if (summandsLeftInEnumeratorMultiples.equivalentInTerms(summandsLeftInDenominatorMultiples)
                            && summandsRightInEnumeratorMultiples.equivalentInTerms(summandsRightInDenominatorMultiples)) {
                        // Dann ist der gekürzte Bruch = coefficientInEnumeratorForTesting/coefficientInDenominatorForTesting.
                        Expression[] result = new Expression[2];
                        result[0] = coefficientInEnumeratorForTesting;
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
                        SimplifyUtilities.getNonConstantFactorsOfEnumeratorInExpression(summandDenominator),
                        SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(summandDenominator));

                // Passende Testsummanden im Zähler und im Nenner gefunden.
                if (summandInEnumeratorForTesting.equivalent(summandInDenominatorForTesting)) {

                    coefficientInEnumeratorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfEnumeratorInExpression(summandEnumerator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandEnumerator));
                    coefficientInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfEnumeratorInExpression(summandDenominator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandDenominator));

                    summandsLeftInEnumeratorMultiples = ExpressionCollection.copy(summandsLeftInEnumerator);
                    summandsLeftInEnumeratorMultiples.multExpression(coefficientInDenominatorForTesting);
                    summandsLeftInEnumeratorMultiples = summandsLeftInEnumeratorMultiples.simplify();

                    summandsRightInEnumeratorMultiples = ExpressionCollection.copy(summandsRightInEnumerator);
                    summandsRightInEnumeratorMultiples.multExpression(coefficientInDenominatorForTesting);
                    summandsRightInEnumeratorMultiples = summandsRightInEnumeratorMultiples.simplify();

                    summandsLeftInDenominatorMultiples = ExpressionCollection.copy(summandsLeftInDenominator);
                    summandsLeftInDenominatorMultiples.multExpression(coefficientInEnumeratorForTesting);
                    summandsLeftInDenominatorMultiples = summandsLeftInDenominatorMultiples.simplify();

                    summandsRightInDenominatorMultiples = ExpressionCollection.copy(summandsRightInDenominator);
                    summandsRightInDenominatorMultiples.multExpression(coefficientInEnumeratorForTesting);
                    summandsRightInDenominatorMultiples = summandsRightInDenominatorMultiples.simplify();

                    if (summandsLeftInEnumeratorMultiples.equivalentInTerms(summandsRightInDenominatorMultiples)
                            && summandsRightInEnumeratorMultiples.equivalentInTerms(summandsLeftInDenominatorMultiples)) {
                        // Dann ist der gekürzte Bruch = coefficientInEnumeratorForTesting/coefficientInDenominatorForTesting.
                        Expression[] result = new Expression[2];
                        result[0] = MINUS_ONE.mult(coefficientInEnumeratorForTesting);
                        result[1] = coefficientInDenominatorForTesting;
                        return result;
                    }

                }

            }

        }

        // In summandsRightInEnumerator nach passendem Testsummanden suchen.
        for (Expression summandEnumerator : summandsRightInEnumerator) {

            if (summandEnumerator.isConstant()) {
                continue;
            }

            summandInEnumeratorForTesting = SimplifyUtilities.produceQuotient(
                    SimplifyUtilities.getNonConstantFactorsOfEnumeratorInExpression(summandEnumerator),
                    SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(summandEnumerator));

            for (Expression summandDenominator : summandsLeftInDenominator) {
                if (summandDenominator.isConstant()) {
                    continue;
                }

                summandInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                        SimplifyUtilities.getNonConstantFactorsOfEnumeratorInExpression(summandDenominator),
                        SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(summandDenominator));

                // Passende Testsummanden im Zähler und im Nenner gefunden.
                if (summandInEnumeratorForTesting.equivalent(summandInDenominatorForTesting)) {

                    coefficientInEnumeratorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfEnumeratorInExpression(summandEnumerator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandEnumerator));
                    coefficientInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfEnumeratorInExpression(summandDenominator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandDenominator));

                    summandsLeftInEnumeratorMultiples = ExpressionCollection.copy(summandsLeftInEnumerator);
                    summandsLeftInEnumeratorMultiples.multExpression(coefficientInDenominatorForTesting);
                    summandsLeftInEnumeratorMultiples = summandsLeftInEnumeratorMultiples.simplify();

                    summandsRightInEnumeratorMultiples = ExpressionCollection.copy(summandsRightInEnumerator);
                    summandsRightInEnumeratorMultiples.multExpression(coefficientInDenominatorForTesting);
                    summandsRightInEnumeratorMultiples = summandsRightInEnumeratorMultiples.simplify();

                    summandsLeftInDenominatorMultiples = ExpressionCollection.copy(summandsLeftInDenominator);
                    summandsLeftInDenominatorMultiples.multExpression(coefficientInEnumeratorForTesting);
                    summandsLeftInDenominatorMultiples = summandsLeftInDenominatorMultiples.simplify();

                    summandsRightInDenominatorMultiples = ExpressionCollection.copy(summandsRightInDenominator);
                    summandsRightInDenominatorMultiples.multExpression(coefficientInEnumeratorForTesting);
                    summandsRightInDenominatorMultiples = summandsRightInDenominatorMultiples.simplify();

                    if (summandsRightInEnumeratorMultiples.equivalentInTerms(summandsLeftInDenominatorMultiples)
                            && summandsLeftInEnumeratorMultiples.equivalentInTerms(summandsRightInDenominatorMultiples)) {
                        // Dann ist der gekürzte Bruch = coefficientInEnumeratorForTesting/coefficientInDenominatorForTesting.
                        Expression[] result = new Expression[2];
                        result[0] = MINUS_ONE.mult(coefficientInEnumeratorForTesting);
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
                        SimplifyUtilities.getNonConstantFactorsOfEnumeratorInExpression(summandDenominator),
                        SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(summandDenominator));

                // Passende Testsummanden im Zähler und im Nenner gefunden.
                if (summandInEnumeratorForTesting.equivalent(summandInDenominatorForTesting)) {

                    coefficientInEnumeratorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfEnumeratorInExpression(summandEnumerator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandEnumerator));
                    coefficientInDenominatorForTesting = SimplifyUtilities.produceQuotient(
                            SimplifyUtilities.getConstantFactorsOfEnumeratorInExpression(summandDenominator),
                            SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(summandDenominator));

                    summandsLeftInEnumeratorMultiples = ExpressionCollection.copy(summandsLeftInEnumerator);
                    summandsLeftInEnumeratorMultiples.multExpression(coefficientInDenominatorForTesting);
                    summandsLeftInEnumeratorMultiples = summandsLeftInEnumeratorMultiples.simplify();

                    summandsRightInEnumeratorMultiples = ExpressionCollection.copy(summandsRightInEnumerator);
                    summandsRightInEnumeratorMultiples.multExpression(coefficientInDenominatorForTesting);
                    summandsRightInEnumeratorMultiples = summandsRightInEnumeratorMultiples.simplify();

                    summandsLeftInDenominatorMultiples = ExpressionCollection.copy(summandsLeftInDenominator);
                    summandsLeftInDenominatorMultiples.multExpression(coefficientInEnumeratorForTesting);
                    summandsLeftInDenominatorMultiples = summandsLeftInDenominatorMultiples.simplify();

                    summandsRightInDenominatorMultiples = ExpressionCollection.copy(summandsRightInDenominator);
                    summandsRightInDenominatorMultiples.multExpression(coefficientInEnumeratorForTesting);
                    summandsRightInDenominatorMultiples = summandsRightInDenominatorMultiples.simplify();

                    if (summandsLeftInEnumeratorMultiples.equivalentInTerms(summandsLeftInDenominatorMultiples)
                            && summandsRightInEnumeratorMultiples.equivalentInTerms(summandsRightInDenominatorMultiples)) {
                        // Dann ist der gekürzte Bruch = coefficientInEnumeratorForTesting/coefficientInDenominatorForTesting.
                        Expression[] result = new Expression[2];
                        result[0] = coefficientInEnumeratorForTesting;
                        result[1] = coefficientInDenominatorForTesting;
                        return result;
                    }

                }

            }

        }

        Expression[] result = new Expression[2];
        result[0] = enumerator;
        result[1] = denominator;
        return result;

    }

    /**
     * Vereinfacht ganzzahlige Potenzen von abs(). Genauer: abs(x)^(2*n) =
     * x^(2*n) und abs(x)^(2*n + 1) = x^(2*n)*abs(x).
     */
    public static Expression simplifyPowersOfAbs(BinaryOperation expr) {

        if (!expr.getLeft().isFunction(TypeFunction.abs) || expr.isNotPower() || !(expr.getRight().isIntegerConstant())) {
            return expr;
        }

        BigInteger exponent = ((Constant) expr.getRight()).getValue().toBigInteger();
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
                || !isPowerOfTen(((Constant) expr.getLeft()).getValue().toBigInteger())) {
            return expr;
        }

        ExpressionCollection summands = SimplifyUtilities.getSummands(expr.getRight());
        ExpressionCollection resultFactorsOutsideOfPowerOfTen = new ExpressionCollection();

        int exponent = SimplifyExpLog.getExponentIfDivisibleByPowerOfTen(((Constant) expr.getLeft()).getValue().toBigInteger());

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
                || !isPowerOfTen(((Constant) expr.getLeft()).getValue().toBigInteger())
                || expr.getRight().isNotDifference()) {
            return expr;
        }

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getRight());
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getRight());
        ExpressionCollection resultFactorsInEnumeratorOutsideOfPowerOfTen = new ExpressionCollection();
        ExpressionCollection resultFactorsInDenominatorOutsideOfPowerOfTen = new ExpressionCollection();

        int exponent = SimplifyExpLog.getExponentIfDivisibleByPowerOfTen(((Constant) expr.getLeft()).getValue().toBigInteger());

        for (int i = 0; i < summandsLeft.getBound(); i++) {
            if (summandsLeft.get(i) instanceof Function && ((Function) summandsLeft.get(i)).getType().equals(TypeFunction.lg)) {
                resultFactorsInEnumeratorOutsideOfPowerOfTen.add(((Function) summandsLeft.get(i)).getLeft().pow(exponent));
                summandsLeft.remove(i);
            } else if (summandsLeft.get(i).isProduct()
                    && !((BinaryOperation) summandsLeft.get(i)).getLeft().isEvenIntegerConstant()
                    && ((BinaryOperation) summandsLeft.get(i)).getRight() instanceof Function
                    && ((Function) ((BinaryOperation) summandsLeft.get(i)).getRight()).getType().equals(TypeFunction.lg)) {
                resultFactorsInEnumeratorOutsideOfPowerOfTen.add(((Function) ((BinaryOperation) summandsLeft.get(i)).getRight()).getLeft().pow(new Constant(exponent).mult(((BinaryOperation) summandsLeft.get(i)).getLeft())));
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
        if (resultFactorsInEnumeratorOutsideOfPowerOfTen.isEmpty() && resultFactorsInDenominatorOutsideOfPowerOfTen.isEmpty()) {
            return expr;
        } else if (!resultFactorsInEnumeratorOutsideOfPowerOfTen.isEmpty() && resultFactorsInDenominatorOutsideOfPowerOfTen.isEmpty()) {
            return SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfPowerOfTen).mult(expr.getLeft().pow(
                    SimplifyUtilities.produceSum(summandsLeft).sub(((BinaryOperation) expr.getRight()).getRight())));
        } else if (resultFactorsInEnumeratorOutsideOfPowerOfTen.isEmpty() && !resultFactorsInDenominatorOutsideOfPowerOfTen.isEmpty()) {
            return expr.getLeft().pow(((BinaryOperation) expr.getRight()).getLeft().sub(SimplifyUtilities.produceSum(summandsRight))).div(
                    SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfPowerOfTen));
        }
        return SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfPowerOfTen).mult(expr.getLeft().pow(SimplifyUtilities.produceDifference(summandsLeft, summandsRight))).div(
                SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfPowerOfTen));

    }

    /**
     * Methode für das Faktorisieren in Summen. Es wird alles, bis auf
     * Konstanten, faktorisiert.
     */
    public static void simplifyFactorizeInSums(ExpressionCollection summands) throws EvaluationException {

        ExpressionCollection commonEnumerators, commonDenominators;
        ExpressionCollection leftSummandRestEnumerators, leftSummandRestDenominators,
                rightSummandRestEnumerators, rightSummandRestDenominators;
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

                leftRestFactors = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summands.get(i));
                rightRestFactors = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summands.get(j));
                commonEnumerators = SimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                leftSummandRestEnumerators = SimplifyUtilities.difference(leftRestFactors, commonEnumerators);
                rightSummandRestEnumerators = SimplifyUtilities.difference(rightRestFactors, commonEnumerators);

                leftRestFactors = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(i));
                rightRestFactors = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(j));
                commonDenominators = SimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                leftSummandRestDenominators = SimplifyUtilities.difference(leftRestFactors, commonDenominators);
                rightSummandRestDenominators = SimplifyUtilities.difference(rightRestFactors, commonDenominators);

                // Im Folgenden werden gemeinsame Faktoren, welche rationale Zahlen sind, NICHT faktorisiert!
                if (!commonEnumerators.isEmpty() && commonDenominators.isEmpty()) {

                    if (commonEnumerators.getBound() == 1 && commonEnumerators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceProduct(commonEnumerators).mult(
                            SimplifyUtilities.produceQuotient(leftSummandRestEnumerators, leftSummandRestDenominators).add(
                                    SimplifyUtilities.produceQuotient(rightSummandRestEnumerators, rightSummandRestDenominators)));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (commonEnumerators.isEmpty() && !commonDenominators.isEmpty()) {

                    if (commonDenominators.getBound() == 1 && commonDenominators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceQuotient(leftSummandRestEnumerators, leftSummandRestDenominators).add(
                            SimplifyUtilities.produceQuotient(rightSummandRestEnumerators, rightSummandRestDenominators)).div(SimplifyUtilities.produceProduct(commonDenominators));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!commonEnumerators.isEmpty() && !commonDenominators.isEmpty()) {

                    if (commonEnumerators.getBound() == 1 && commonEnumerators.get(0) instanceof Constant
                            && commonDenominators.getBound() == 1 && commonDenominators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceProduct(commonEnumerators).mult(
                            SimplifyUtilities.produceQuotient(leftSummandRestEnumerators, leftSummandRestDenominators).add(
                                    SimplifyUtilities.produceQuotient(rightSummandRestEnumerators, rightSummandRestDenominators))).div(SimplifyUtilities.produceProduct(commonDenominators));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                FlowController.interruptComputationIfNeeded();

            }

        }

    }

    /**
     * Methode für das Faktorisieren in Differenzen. Es wird alles, bis auf
     * Konstanten, faktorisiert.
     */
    public static void simplifyFactorizeInDifferences(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) throws EvaluationException {

        ExpressionCollection commonEnumerators, commonDenominators;
        ExpressionCollection leftSummandRestEnumerators, leftSummandRestDenominators,
                rightSummandRestEnumerators, rightSummandRestDenominators;
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

                leftRestFactors = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsLeft.get(i));
                rightRestFactors = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsRight.get(j));
                commonEnumerators = SimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                leftSummandRestEnumerators = SimplifyUtilities.difference(leftRestFactors, commonEnumerators);
                rightSummandRestEnumerators = SimplifyUtilities.difference(rightRestFactors, commonEnumerators);

                leftRestFactors = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(i));
                rightRestFactors = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(j));
                commonDenominators = SimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                leftSummandRestDenominators = SimplifyUtilities.difference(leftRestFactors, commonDenominators);
                rightSummandRestDenominators = SimplifyUtilities.difference(rightRestFactors, commonDenominators);

                // Im Folgenden werden gemeinsame Faktoren, welche rationale Zahlen sind, NICHT faktorisiert!
                if (!commonEnumerators.isEmpty() && commonDenominators.isEmpty()) {

                    if (commonEnumerators.getBound() == 1 && commonEnumerators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceProduct(commonEnumerators).mult(
                            SimplifyUtilities.produceQuotient(leftSummandRestEnumerators, leftSummandRestDenominators).sub(
                                    SimplifyUtilities.produceQuotient(rightSummandRestEnumerators, rightSummandRestDenominators)));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (commonEnumerators.isEmpty() && !commonDenominators.isEmpty()) {

                    if (commonDenominators.getBound() == 1 && commonDenominators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceQuotient(leftSummandRestEnumerators, leftSummandRestDenominators).sub(
                            SimplifyUtilities.produceQuotient(rightSummandRestEnumerators, rightSummandRestDenominators)).div(
                                    SimplifyUtilities.produceProduct(commonDenominators));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!commonEnumerators.isEmpty() && !commonDenominators.isEmpty()) {

                    if (commonEnumerators.getBound() == 1 && commonEnumerators.get(0) instanceof Constant
                            && commonDenominators.getBound() == 1 && commonDenominators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceProduct(commonEnumerators).mult(
                            SimplifyUtilities.produceQuotient(leftSummandRestEnumerators, leftSummandRestDenominators).sub(
                                    SimplifyUtilities.produceQuotient(rightSummandRestEnumerators, rightSummandRestDenominators))).div(
                                    SimplifyUtilities.produceProduct(commonDenominators));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                FlowController.interruptComputationIfNeeded();

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
            return ((BinaryOperation) f).getLeft().simplifyExpand(type).div(((BinaryOperation) f).getRight().simplifyExpand(type));
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
                summandsLeft.put(i, SimplifyUtilities.produceProduct(ExpressionCollection.copy(factors, 0, smallestIndexOfFactorWhichIsEitherSumOrDifference)).mult(summandsLeft.get(i)).mult(SimplifyUtilities.produceProduct(ExpressionCollection.copy(factors, smallestIndexOfFactorWhichIsEitherSumOrDifference + 1, factors.getBound() + 1))).orderSumsAndProducts());
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, SimplifyUtilities.produceProduct(ExpressionCollection.copy(factors, 0, smallestIndexOfFactorWhichIsEitherSumOrDifference)).mult(summandsRight.get(i)).mult(SimplifyUtilities.produceProduct(ExpressionCollection.copy(factors, smallestIndexOfFactorWhichIsEitherSumOrDifference + 1, factors.getBound() + 1))).orderSumsAndProducts());
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
                BigInteger exponent = ((Constant) expr.getRight()).getValue().toBigInteger();
                BigInteger numberOfSummandsInBase = BigInteger.valueOf(summands.getBound());
                if (numberOfSummandsInBase.subtract(BigInteger.ONE).add(exponent).compareTo(boundNumberOfSummands) > 0) {
                    return expr;
                }
                BigInteger numberOfSummandsInResult = ArithmeticMethods.factorial(numberOfSummandsInBase.subtract(BigInteger.ONE).add(exponent).intValue()).divide(ArithmeticMethods.factorial(numberOfSummandsInBase.intValue() - 1).multiply(ArithmeticMethods.factorial(exponent.intValue())));
                if (numberOfSummandsInResult.compareTo(boundNumberOfSummands) > 0) {
                    return expr;
                }
                return SimplifyAlgebraicExpressionMethods.binomialExpansion((BinaryOperation) expr.getLeft(), exponent.intValue());
            } else if (expr.getLeft().isDifference()) {
                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getLeft());
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getLeft());
                BigInteger exponent = ((Constant) expr.getRight()).getValue().toBigInteger();
                BigInteger numberOfSummandsInBase = BigInteger.valueOf(summandsLeft.getBound() + summandsRight.getBound());
                if (numberOfSummandsInBase.subtract(BigInteger.ONE).add(exponent).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                    return expr;
                }
                BigInteger numberOfSummandsInResult = ArithmeticMethods.factorial(numberOfSummandsInBase.subtract(BigInteger.ONE).add(exponent).intValue()).divide(ArithmeticMethods.factorial(numberOfSummandsInBase.intValue() - 1).multiply(ArithmeticMethods.factorial(exponent.intValue())));
                if (numberOfSummandsInResult.compareTo(boundNumberOfSummands) > 0) {
                    return expr;
                }
                return SimplifyAlgebraicExpressionMethods.binomialExpansion((BinaryOperation) expr.getLeft(), exponent.intValue());
            }

        }

        return expr;

    }

    /**
     * Methode für das Faktorisieren in Summen. Es wird nur dann faktorisiert,
     * wenn die Summanden, bis auf konstante Faktoren, übereinstimmen.
     */
    public static void simplifyFactorizeAllButRationalsInSums(ExpressionCollection summands) throws EvaluationException {

        ExpressionCollection rationalEnumeratorsLeft;
        ExpressionCollection nonRationalEnumeratorsLeft = new ExpressionCollection();
        ExpressionCollection rationalDenominatorsLeft;
        ExpressionCollection nonRationalDenominatorsLeft = new ExpressionCollection();
        ExpressionCollection rationalEnumeratorsRight;
        ExpressionCollection nonRationalEnumeratorsRight = new ExpressionCollection();
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

                rationalEnumeratorsLeft = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summands.get(i));
                rationalEnumeratorsRight = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summands.get(j));
                rationalDenominatorsLeft = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(i));
                rationalDenominatorsRight = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(j));
                nonRationalEnumeratorsLeft.clear();
                nonRationalEnumeratorsRight.clear();
                nonRationalDenominatorsLeft.clear();
                nonRationalDenominatorsRight.clear();

                for (int k = 0; k < rationalEnumeratorsLeft.getBound(); k++) {
                    if (!(rationalEnumeratorsLeft.get(k) instanceof Constant)) {
                        nonRationalEnumeratorsLeft.add(rationalEnumeratorsLeft.get(k));
                        rationalEnumeratorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < rationalEnumeratorsRight.getBound(); k++) {
                    if (!(rationalEnumeratorsRight.get(k) instanceof Constant)) {
                        nonRationalEnumeratorsRight.add(rationalEnumeratorsRight.get(k));
                        rationalEnumeratorsRight.remove(k);
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
                        rationalEnumeratorsLeft.remove(k);
                        rationalDenominatorsRight.remove(k);
                    }
                }

                // Falls die nichtrationalen Faktoren NICHT übereinstimmen, nächster Schleifendurchgang.
                if (!nonRationalEnumeratorsLeft.equivalentInTerms(nonRationalEnumeratorsRight)
                        || !nonRationalDenominatorsLeft.equivalentInTerms(nonRationalDenominatorsRight)) {
                    continue;
                }

                if (!nonRationalEnumeratorsLeft.isEmpty() && nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalEnumeratorsLeft, rationalDenominatorsLeft).add(SimplifyUtilities.produceQuotient(rationalEnumeratorsRight, rationalDenominatorsRight)).mult(
                            SimplifyUtilities.produceProduct(nonRationalEnumeratorsLeft));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (nonRationalEnumeratorsLeft.isEmpty() && !nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalEnumeratorsLeft, rationalDenominatorsLeft).add(SimplifyUtilities.produceQuotient(rationalEnumeratorsRight, rationalDenominatorsRight)).div(SimplifyUtilities.produceProduct(nonRationalDenominatorsLeft));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!nonRationalEnumeratorsLeft.isEmpty() && !nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalEnumeratorsLeft, rationalDenominatorsLeft).add(
                            SimplifyUtilities.produceQuotient(rationalEnumeratorsRight, rationalDenominatorsRight)).mult(
                                    SimplifyUtilities.produceProduct(nonRationalEnumeratorsLeft)).div(
                                    SimplifyUtilities.produceProduct(nonRationalDenominatorsLeft));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                FlowController.interruptComputationIfNeeded();

            }

        }

    }

    /**
     * Methode für das Faktorisieren in Differenzen. Es wird nur dann
     * faktorisiert, wenn die Summanden, bis auf konstante Faktoren,
     * übereinstimmen.
     */
    public static void simplifyFactorizeAllButRationalsInDifferences(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) throws EvaluationException {

        ExpressionCollection rationalEnumeratorsLeft;
        ExpressionCollection nonRationalEnumeratorsLeft = new ExpressionCollection();
        ExpressionCollection rationalDenominatorsLeft;
        ExpressionCollection nonRationalDenominatorsLeft = new ExpressionCollection();
        ExpressionCollection rationalEnumeratorsRight;
        ExpressionCollection nonRationalEnumeratorsRight = new ExpressionCollection();
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

                rationalEnumeratorsLeft = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsLeft.get(i));
                rationalEnumeratorsRight = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsRight.get(j));
                rationalDenominatorsLeft = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(i));
                rationalDenominatorsRight = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(j));

                nonRationalEnumeratorsLeft.clear();
                nonRationalEnumeratorsRight.clear();
                nonRationalDenominatorsLeft.clear();
                nonRationalDenominatorsRight.clear();
                for (int k = 0; k < rationalEnumeratorsLeft.getBound(); k++) {
                    if (!(rationalEnumeratorsLeft.get(k) instanceof Constant)) {
                        nonRationalEnumeratorsLeft.add(rationalEnumeratorsLeft.get(k));
                        rationalEnumeratorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < rationalEnumeratorsRight.getBound(); k++) {
                    if (!(rationalEnumeratorsRight.get(k) instanceof Constant)) {
                        nonRationalEnumeratorsRight.add(rationalEnumeratorsRight.get(k));
                        rationalEnumeratorsRight.remove(k);
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
                if (!nonRationalEnumeratorsLeft.equivalentInTerms(nonRationalEnumeratorsRight)
                        || !nonRationalDenominatorsLeft.equivalentInTerms(nonRationalDenominatorsRight)) {
                    continue;
                }

                if (!nonRationalEnumeratorsLeft.isEmpty() && nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalEnumeratorsLeft, rationalDenominatorsLeft).sub(SimplifyUtilities.produceQuotient(rationalEnumeratorsRight, rationalDenominatorsRight)).mult(
                            SimplifyUtilities.produceProduct(nonRationalEnumeratorsLeft));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (nonRationalEnumeratorsLeft.isEmpty() && !nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalEnumeratorsLeft, rationalDenominatorsLeft).sub(SimplifyUtilities.produceQuotient(rationalEnumeratorsRight, rationalDenominatorsRight)).div(SimplifyUtilities.produceProduct(nonRationalDenominatorsLeft));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!nonRationalEnumeratorsLeft.isEmpty() && !nonRationalDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(rationalEnumeratorsLeft, rationalDenominatorsLeft).sub(
                            SimplifyUtilities.produceQuotient(rationalEnumeratorsRight, rationalDenominatorsRight)).mult(
                                    SimplifyUtilities.produceProduct(nonRationalEnumeratorsLeft)).div(
                                    SimplifyUtilities.produceProduct(nonRationalDenominatorsLeft));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                FlowController.interruptComputationIfNeeded();

            }

        }

    }

    /**
     * Methode für das Kürzen von Faktoren im Quotienten.
     */
    public static void simplifyReduceFactorsInQuotients(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator) throws EvaluationException {

        Expression base;
        Expression exponent;
        Expression compareBase;
        Expression compareExponent;

        for (int i = 0; i < factorsEnumerator.getBound(); i++) {

            if (factorsEnumerator.get(i) == null) {
                continue;
            }

            if (factorsEnumerator.get(i).isPower()) {
                base = ((BinaryOperation) factorsEnumerator.get(i)).getLeft();
                exponent = ((BinaryOperation) factorsEnumerator.get(i)).getRight();
            } else {
                base = factorsEnumerator.get(i);
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

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                FlowController.interruptComputationIfNeeded();

            }

            factorsEnumerator.put(i, base.pow(exponent));

        }

    }

}
