package expressionsimplifymethods;

import exceptions.EvaluationException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.Operator;
import expressionbuilder.TypeFunction;
import expressionbuilder.TypeOperator;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

public abstract class SimplifyExpLog {

    public static void collectExponentialFunctionsInProduct(ExpressionCollection factors) {

        Expression argumentInExp = Expression.ZERO;
        int indexOfFirstExpFunction = -1;

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null && factors.get(i).isFunction(TypeFunction.exp)) {

                if (argumentInExp.equals(Expression.ZERO)) {
                    argumentInExp = ((Function) factors.get(i)).getLeft();
                    indexOfFirstExpFunction = i;
                } else {
                    argumentInExp = argumentInExp.add(((Function) factors.get(i)).getLeft());
                }
                factors.remove(i);

            }
        }

        if (indexOfFirstExpFunction >= 0) {
            factors.put(indexOfFirstExpFunction, argumentInExp.exp());
        }

    }

    public static void collectExponentialFunctionsInQuotient(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator) {

        Expression argumentInExpLeft = Expression.ZERO;
        Expression argumentInExpRight = Expression.ZERO;
        int indexOfFirstExpFunctionInEnumerator = -1, indexOfFirstExpFunctionInDenominator = -1;

        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i) != null && factorsEnumerator.get(i).isFunction(TypeFunction.exp)) {

                if (argumentInExpLeft.equals(Expression.ZERO)) {
                    argumentInExpLeft = ((Function) factorsEnumerator.get(i)).getLeft();
                    indexOfFirstExpFunctionInEnumerator = i;
                } else {
                    argumentInExpLeft = argumentInExpLeft.add(((Function) factorsEnumerator.get(i)).getLeft());
                }
                factorsEnumerator.remove(i);

            }
        }

        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i) != null && factorsDenominator.get(i).isFunction(TypeFunction.exp)) {

                if (argumentInExpRight.equals(Expression.ZERO)) {
                    argumentInExpRight = ((Function) factorsDenominator.get(i)).getLeft();
                    indexOfFirstExpFunctionInDenominator = i;
                } else {
                    argumentInExpRight = argumentInExpRight.add(((Function) factorsDenominator.get(i)).getLeft());
                }
                factorsDenominator.remove(i);

            }
        }

        if (indexOfFirstExpFunctionInEnumerator >= 0 && indexOfFirstExpFunctionInDenominator < 0) {
            factorsEnumerator.put(indexOfFirstExpFunctionInEnumerator, argumentInExpLeft.exp());
        } else if (indexOfFirstExpFunctionInEnumerator < 0 && indexOfFirstExpFunctionInDenominator >= 0) {
            /*
             Die resultierende Exponentialfunktion soll IMMER im Zähler stehen
             (hat Vorteile für weitere Anwendungen).
             */
            factorsEnumerator.add(Expression.MINUS_ONE.mult(argumentInExpRight).exp());
        } else if (indexOfFirstExpFunctionInEnumerator >= 0 && indexOfFirstExpFunctionInDenominator >= 0) {
            factorsEnumerator.put(indexOfFirstExpFunctionInEnumerator, argumentInExpLeft.sub(argumentInExpRight).exp());
        }

    }

    /**
     * Sammelt in einem Produkt Potenzen von nichtnegativen rationalen Zahlen
     * auf.
     *
     * @throws EvaluationException
     */
    public static void collectPowersOfRationalsWithSameExponentInProduct(ExpressionCollection factors) throws EvaluationException {

        Expression resultBase;

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null && factors.get(i).isPower()
                    && ((BinaryOperation) factors.get(i)).getLeft().isIntegerConstantOrRationalConstant()
                    && !((BinaryOperation) factors.get(i)).getLeft().isIntegerConstantOrRationalConstantNegative()) {

                resultBase = ((BinaryOperation) factors.get(i)).getLeft();

                for (int j = i + 1; j < factors.getBound(); j++) {
                    if (factors.get(j) != null && factors.get(j).isPower()
                            && ((BinaryOperation) factors.get(j)).getLeft().isIntegerConstantOrRationalConstant()
                            && !((BinaryOperation) factors.get(j)).getLeft().isIntegerConstantOrRationalConstantNegative()
                            && ((BinaryOperation) factors.get(j)).getRight().equivalent(((BinaryOperation) factors.get(i)).getRight())) {

                        resultBase = resultBase.mult(((BinaryOperation) factors.get(j)).getLeft());
                        factors.remove(j);

                    }
                    resultBase = resultBase.simplify();
                }

                if (!resultBase.equals(Expression.ONE)) {
                    factors.put(i, resultBase.pow(((BinaryOperation) factors.get(i)).getRight()));
                } else {
                    factors.remove(i);
                }

            }

        }

    }

    /**
     * Sammelt in einem Quotienten Potenzen von nichtnegativen rationalen Zahlen
     * auf.
     *
     * throws EvaluationException
     */
    public static void collectPowersOfRationalsWithSameExponentInQuotient(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator) throws EvaluationException {

        Expression resultBase;

        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i) != null && factorsEnumerator.get(i).isPower()
                    && ((BinaryOperation) factorsEnumerator.get(i)).getLeft().isIntegerConstantOrRationalConstant()
                    && !((BinaryOperation) factorsEnumerator.get(i)).getLeft().isIntegerConstantOrRationalConstantNegative()) {

                resultBase = ((BinaryOperation) factorsEnumerator.get(i)).getLeft();

                for (int j = 0; j < factorsDenominator.getBound(); j++) {
                    if (factorsDenominator.get(j) != null && factorsDenominator.get(j).isPower()
                            && ((BinaryOperation) factorsDenominator.get(j)).getLeft().isIntegerConstantOrRationalConstant()
                            && !((BinaryOperation) factorsDenominator.get(j)).getLeft().isIntegerConstantOrRationalConstantNegative()
                            && ((BinaryOperation) factorsDenominator.get(j)).getRight().equivalent(((BinaryOperation) factorsEnumerator.get(i)).getRight())) {

                        resultBase = resultBase.div(((BinaryOperation) factorsDenominator.get(j)).getLeft());
                        factorsDenominator.remove(j);

                    }
                    resultBase = resultBase.simplify();
                }

                if (!resultBase.equals(Expression.ONE)) {
                    factorsEnumerator.put(i, resultBase.pow(((BinaryOperation) factorsEnumerator.get(i)).getRight()));
                } else {
                    factorsEnumerator.remove(i);
                }

            }

        }

    }

    /**
     * Hier wird geprüft, ob sich gewisse Exponentialfunktionen im Nenner
     * befinden. Falls ja, so werden diese in den Zähler gebracht (und an den
     * letzten Faktor dranmultipliziert oder, falls der letzte Faktor leer ist,
     * an diese Stelle hinzugefügt), falls der Exponent NICHT konstant ist.
     * WICHTIG: Falls der Exponent konstant ist, darf dies NICHT gemacht werden,
     * ansonsten gibt es Endlosschleifen, da in simplifyTrivial() beispielsweise
     * a^(-3) wieder in den Nenner gebracht wird!
     *
     * @throws EvaluationException
     */
    public static void bringNonConstantExponentialFunctionsToEnumerator(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator) throws EvaluationException {

        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i) != null && factorsDenominator.get(i).isPower()
                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().isConstant()) {
                factorsEnumerator.add(((BinaryOperation) factorsDenominator.get(i)).getLeft().pow(Expression.MINUS_ONE.mult(((BinaryOperation) factorsDenominator.get(i)).getRight()).simplify()));
                factorsDenominator.remove(i);
            }
        }

    }

    /**
     * Vereinfacht: ln(1/x) bzw. lg(1/x) zu -ln(x) bzw. -lg(x), falls expr eine
     * Logarithmusfunktion darstellt.
     */
    public static Expression reduceLogarithmOfReciprocal(Expression logArgument, TypeFunction logType) {
        if (logArgument.isQuotient() && ((BinaryOperation) logArgument).getLeft().equals(Expression.ONE)) {
            Expression.MINUS_ONE.mult(new Function(((BinaryOperation) logArgument).getRight(), logType));
        }
        return new Function(logArgument, logType);
    }

    /**
     * Zieht Logarithmen auseinander.
     */
    public static Expression expandLogarithms(Expression logArgument, TypeFunction logType) {

        if (logArgument.isPower()) {
            return ((BinaryOperation) logArgument).getRight().mult(new Function(((BinaryOperation) logArgument).getLeft(), logType));
        }

        if (logArgument.isProduct() || logArgument.isQuotient()) {

            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(logArgument);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(logArgument);

            ExpressionCollection resultSummandsLeft = new ExpressionCollection();
            ExpressionCollection resultSummandsRight = new ExpressionCollection();

            for (int i = 0; i < factorsEnumerator.getBound(); i++) {
                if (factorsEnumerator.get(i).isPower()) {
                    resultSummandsLeft.put(i, ((BinaryOperation) factorsEnumerator.get(i)).getRight().mult(
                            new Function(((BinaryOperation) factorsEnumerator.get(i)).getLeft(), logType)));
                } else {
                    resultSummandsLeft.put(i, new Function(factorsEnumerator.get(i), logType));
                }
            }
            for (int i = 0; i < factorsDenominator.getBound(); i++) {
                if (factorsDenominator.get(i).isPower()) {
                    resultSummandsRight.put(i, ((BinaryOperation) factorsDenominator.get(i)).getRight().mult(
                            new Function(((BinaryOperation) factorsDenominator.get(i)).getLeft(), logType)));
                } else {
                    resultSummandsRight.put(i, new Function(factorsDenominator.get(i), logType));
                }
            }

            return SimplifyUtilities.produceDifference(resultSummandsLeft, resultSummandsRight);

        }

        return new Function(logArgument, logType);

    }

    /**
     * Vereinfacht: ln(prod(f(k), k, m, n)) = sum(ln(f(k)), k, m, n).
     */
    public static Expression expandLogarithmOfProduct(Expression logArgument, TypeFunction logType) {
        if (!logArgument.isOperator(TypeOperator.prod)) {
            return new Function(logArgument, logType);
        }
        return new Operator(TypeOperator.sum, new Object[]{new Function(logArgument, logType),
            ((Operator) logArgument).getParams()[1], ((Operator) logArgument).getParams()[2],
            ((Operator) logArgument).getParams()[3]}, ((Operator) logArgument).getPrecise());
    }

    /**
     * Zieht Faktoren in die Logarithmusfunktion hinein, wenn Logarithmen in
     * einer Summe vorliegt und wenn mindestens zwei logarithmische Summanden
     * vorliegen.
     */
    public static void pullFactorsIntoLogarithms(ExpressionCollection summands, TypeFunction logType) {

        ExpressionCollection factorsEnumerator, factorsDenominator;

        /**
         * In jedem Summanden werden Ausdrücke der Form y*ln(x) zu ln(x^y)
         * gemacht, falls<br>
         * y eine ungerade Zahl ist oder ein Bruch mit ungeradem Zähler.<br>
         * x eine konstante positive Zahl oder ein stets positiver Ausdruck
         * ist.<br>(ALT) x eine konstante positive Zahl oder ein nichtkonstanter
         * Ausdruck ist.(NEU)
         */
        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }

            factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summands.get(i));
            factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(i));
            Expression logArgument;

            /*
             Ohne Einschränkung kann angenommen werden, dass ganze konstante
             Faktoren im Zähler und Nenner von summands.get(i) ganz vorne (im
             1. Faktor in Zähler bzw. Nenner) stehen.
             */
            for (int j = 0; j < factorsEnumerator.getBound(); j++) {
                if (factorsEnumerator.get(j).isFunction(logType)) {

                    logArgument = ((Function) factorsEnumerator.get(j)).getLeft();
//                    if (logArgument.isAlwaysPositive()) {
//                        factorsEnumerator.remove(j);
//                        summands.put(i, new Function(logArgument.pow(SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator)), logType));
                    if (logArgument.isPositive() || !logArgument.isConstant()) {
                        factorsEnumerator.remove(j);
                        summands.put(i, new Function(logArgument.pow(SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator)), logType));
                    } else {
                        Expression exponent = Expression.ONE;
                        if (factorsEnumerator.get(0) != null && factorsEnumerator.get(0).isOddConstant()) {
                            exponent = factorsEnumerator.get(0);
                            factorsEnumerator.remove(0);
                        }
                        if (factorsDenominator.get(0) != null && factorsDenominator.get(0).isIntegerConstant()) {
                            exponent = exponent.div(factorsDenominator.get(0));
                            factorsDenominator.remove(0);
                        }
                        if (!exponent.equals(Expression.ONE)) {
                            factorsEnumerator.put(j, new Function(logArgument.pow(exponent), logType));
                            summands.put(i, SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator));
                        }
                    }

                }
            }

        }

    }

    /**
     * Sammelt Logarithmen in einer Summe auf, wenn mindestens zwei
     * logarithmische Summanden vorliegen. Beispiel:
     * (x+ln(a)+y+2*ln(b))-(z+ln(c)) wird zu (x+y+ln(a*b^2/c))-z
     */
    public static void collectLogarithmsInSum(ExpressionCollection summands, TypeFunction logType) {

        Expression logArgument = Expression.ONE;
        /*
         indexOfFirstLogFunction wird die Stelle sein, wo ein Logarithmus des
         passenden Typs ZUM ERSTEN MAL auftaucht (dahin wird auch das Ergebnis
         positioniert). Dies ist Notwendig, sonst ergeben sich Endlosschleifen
         beim simplify(), wenn in einer Summe zwei verschiedene
         Logarithmentypen auftauchen (diese Funktion setzt dann abwechselnd
         beide als den ersten Summanden).
         */
        int indexOfFirstLogFunction = -1;

        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) != null && summands.get(i).isFunction(logType)) {

                if (logArgument.equals(Expression.ONE)) {
                    logArgument = ((Function) summands.get(i)).getLeft();
                    indexOfFirstLogFunction = i;
                } else {
                    logArgument = logArgument.mult(((Function) summands.get(i)).getLeft());
                }
                summands.remove(i);

            }
        }

        if (indexOfFirstLogFunction >= 0) {
            summands.put(indexOfFirstLogFunction, new Function(logArgument, logType));
        }

    }

    /**
     * Sammelt Logarithmen in einer Differenz auf, wenn mindestens zwei
     * logarithmische Summanden vorliegen. Beispiel:
     * (x+ln(a)+y+2*ln(b))-(z+ln(c)) wird zu (x+y+ln(a*b^2/c))-z
     */
    public static void collectLogarithmsInDifference(ExpressionCollection summandsLeft, ExpressionCollection summandsRight, TypeFunction logType) {

        Expression logArgumentLeft = Expression.ONE;
        Expression logArgumentRight = Expression.ONE;

        // Nun Logarithmen im Minuenden aufsammeln.
        int indexOfFirstLogFunctionInEnumerator = -1;
        int indexOfFirstLogFunctionInDenominator = -1;
        /*
         Die beiden indexOfFirstLogFunction bedeuten die Stelle im Zähler bzw.
         Nenner, wo ein Logarithmus des passenden Typs ZUM ERSTEN MAL
         auftaucht (dahin wird auch das Ergebnis positioniert).
         */
        for (int i = 0; i < summandsLeft.getBound(); i++) {
            if ((summandsLeft.get(i) instanceof Function) && ((Function) summandsLeft.get(i)).getType().equals(logType)) {

                if (logArgumentLeft.equals(Expression.ONE)) {
                    logArgumentLeft = ((Function) summandsLeft.get(i)).getLeft();
                    indexOfFirstLogFunctionInEnumerator = i;
                } else {
                    logArgumentLeft = logArgumentLeft.mult(((Function) summandsLeft.get(i)).getLeft());
                }
                summandsLeft.remove(i);

            }
        }
        // Nun Logarithmen im Subtrahenden aufsammeln.
        for (int i = 0; i < summandsRight.getBound(); i++) {
            if ((summandsRight.get(i) instanceof Function) && ((Function) summandsRight.get(i)).getType().equals(logType)) {

                if (logArgumentRight.equals(Expression.ONE)) {
                    logArgumentRight = ((Function) summandsRight.get(i)).getLeft();
                    indexOfFirstLogFunctionInDenominator = i;
                } else {
                    logArgumentRight = logArgumentRight.mult(((Function) summandsRight.get(i)).getLeft());
                }
                summandsRight.remove(i);

            }
        }

        if (indexOfFirstLogFunctionInEnumerator >= 0 && indexOfFirstLogFunctionInDenominator < 0) {
            summandsLeft.put(indexOfFirstLogFunctionInEnumerator, new Function(logArgumentLeft, logType));
        } else if (indexOfFirstLogFunctionInEnumerator < 0 && indexOfFirstLogFunctionInDenominator >= 0) {
            summandsRight.put(indexOfFirstLogFunctionInDenominator, new Function(logArgumentRight, logType));
        } else if (indexOfFirstLogFunctionInEnumerator >= 0 && indexOfFirstLogFunctionInDenominator >= 0) {
            summandsLeft.put(indexOfFirstLogFunctionInEnumerator, new Function(logArgumentLeft.div(logArgumentRight), logType));
        }

    }

    /**
     * Falls der Ausdruck ein Produkt ist, wird das Produkt einzelner Potenzen
     * zurückgegeben, wenn der Exponent ganzzahlig ist. Falls nicht, so werden
     * nur "erlaubte" Koeffizienten herausgezogen (d.h. es dürfen nur dann
     * negative Koeffizienten zusammen mit dem Exponenten herausgezogen werden,
     * wenn der Exponent ein Bruch mit ungeradem Nenner ist).
     */
    public static Expression splitPowersInProduct(Expression expr) {

        if (expr.isNotPower() || ((BinaryOperation) expr).getLeft().isNotProduct()) {
            return expr;
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(((BinaryOperation) expr).getLeft());
        ExpressionCollection resultFactorsOutsideOfBracket = new ExpressionCollection();

        Expression exponent = ((BinaryOperation) expr).getRight();

        if (SimplifyAlgebraicExpressionMethods.isAdmissibleExponent(exponent)) {
            // Falls Exponent entweder ganzzahlig ist oder ein Bruch mit ungeradem Nenner.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).pow(exponent));
            }
            return SimplifyUtilities.produceProduct(factors);
        }

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i).isNonNegative()) {
                resultFactorsOutsideOfBracket.add(factors.get(i).pow(exponent));
                factors.remove(i);
            }
        }

        // Ergebnis bilden.
        if (resultFactorsOutsideOfBracket.isEmpty()) {
            return expr;
        }

        if (factors.isEmpty()) {
            return SimplifyUtilities.produceProduct(resultFactorsOutsideOfBracket);
        }

        return SimplifyUtilities.produceProduct(resultFactorsOutsideOfBracket).mult(SimplifyUtilities.produceProduct(factors).pow(exponent));

    }

    /**
     * Falls der Ausdruck eine Potenz eines Quotienten ist, wird der Quotient
     * einzelner Potenzen zurückgegeben, wenn der Exponent ganzzahlig ist. Falls
     * nicht, so werden nur "erlaubte" Koeffizienten herausgezogen (d.h. es
     * dürfen nur dann negative Koeffizienten zusammen mit dem Exponenten
     * herausgezogen werden, wenn der Exponent ein Bruch mit ungeradem Nenner
     * ist).
     */
    public static Expression splitPowersInQuotient(Expression expr) {

        if (expr.isNotPower() || ((BinaryOperation) expr).getLeft().isNotQuotient()) {
            return expr;
        }

        // Falls die Basis rational ist, soll NICHT aufgeteilt werden.
        if (((BinaryOperation) expr).getLeft().isRationalConstant()) {
            return expr;
        }

        Expression exponent = ((BinaryOperation) expr).getRight();

        /*
         Aufteilung nur erlaubt, wenn Zähler und Nenner positiv sind, oder
         wenn der Exponent ganzzahlig oder rational mit ungeradem Nenner ist.
         */
        if (exponent.isIntegerConstant()) {
            return ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getLeft().pow(((BinaryOperation) expr).getRight()).div(
                    ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getRight().pow(((BinaryOperation) expr).getRight()));
        }

        if (((BinaryOperation) ((BinaryOperation) expr).getLeft()).getLeft().isNonNegative() && ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getRight().isNonNegative()
                || SimplifyAlgebraicExpressionMethods.isAdmissibleExponent(exponent)) {
            return ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getLeft().pow(((BinaryOperation) expr).getRight()).div(
                    ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getRight().pow(((BinaryOperation) expr).getRight()));
        }

        return expr;

    }

    /**
     * Vereinfacht Verhältnisse von Logarithmen, die rational sind (z.B.
     * ln(125)/ln(25) = 3/2 etc.).
     *
     * @throws EvaluationException
     */
    public static void simplifyQuotientsOfLogarithms(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator, TypeFunction logType) throws EvaluationException {

        /*
         Die Bedeutung der a, b, c, d wird im Verlauf der Funktion in den
         Kommentaren erläutert.
         */
        BigInteger a, b, c, d;
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {

            if (factorsEnumerator.get(i) != null && factorsEnumerator.get(i) instanceof Function && ((Function) factorsEnumerator.get(i)).getType().equals(logType)
                    && ((Function) factorsEnumerator.get(i)).getLeft().isIntegerConstant()) {

                // Fall: ln(a)/ln(b) mit ganzen a, b.
                for (int j = 0; j < factorsDenominator.getBound(); j++) {
                    if (factorsDenominator.get(j) != null && factorsDenominator.get(j) instanceof Function && ((Function) factorsDenominator.get(j)).getType().equals(logType)
                            && ((Function) factorsDenominator.get(j)).getLeft().isIntegerConstant()) {

                        a = ((Constant) ((Function) factorsEnumerator.get(i)).getLeft()).getValue().toBigInteger();
                        b = ((Constant) ((Function) factorsDenominator.get(j)).getLeft()).getValue().toBigInteger();
                        Object[] isRational = isQuotientOfLogarithmsRational(a, b);
                        if (isRational.length == 2) {
                            if (((BigInteger) isRational[0]).compareTo(BigInteger.ONE) == 0) {
                                factorsEnumerator.remove(i);
                            } else {
                                factorsEnumerator.put(i, new Constant((BigInteger) isRational[0]));
                            }
                            if (((BigInteger) isRational[1]).compareTo(BigInteger.ONE) == 0) {
                                factorsDenominator.remove(j);
                            } else {
                                factorsDenominator.put(j, new Constant((BigInteger) isRational[1]));
                            }
                            break;
                        }

                    }
                }

            } else if (factorsEnumerator.get(i) != null && factorsEnumerator.get(i) instanceof Function && ((Function) factorsEnumerator.get(i)).getType().equals(logType)
                    && ((Function) factorsEnumerator.get(i)).getLeft().isRationalConstant()) {

                // Fall: ln(a/b)/ln(c/d) mit ganzen a, b, c, d.
                for (int j = 0; j < factorsDenominator.getBound(); j++) {
                    if (factorsDenominator.get(j) != null && factorsDenominator.get(j) instanceof Function && ((Function) factorsDenominator.get(j)).getType().equals(logType)
                            && ((Function) factorsDenominator.get(j)).getLeft().isRationalConstant()) {

                        a = ((Constant) ((BinaryOperation) ((Function) factorsEnumerator.get(i)).getLeft()).getLeft()).getValue().toBigInteger();
                        b = ((Constant) ((BinaryOperation) ((Function) factorsEnumerator.get(i)).getLeft()).getRight()).getValue().toBigInteger();
                        c = ((Constant) ((BinaryOperation) ((Function) factorsDenominator.get(j)).getLeft()).getLeft()).getValue().toBigInteger();
                        d = ((Constant) ((BinaryOperation) ((Function) factorsDenominator.get(j)).getLeft()).getRight()).getValue().toBigInteger();
                        /*
                         Nun wird geprüft, ob ln(a)/ln(c) und ln(b)/ln(d)
                         rational sind UND ob die Verhältnisse gleich sind.
                         */
                        Object[] isLogADivLogCRational = isQuotientOfLogarithmsRational(a, c);
                        Object[] isLogBDivLogDRational = isQuotientOfLogarithmsRational(b, d);
                        /*
                         Nun müssen die Verhältnisse
                         isLogADivLogCRational[0]/isLogADivLogCRational[1] und
                         isLogBDivLogDRational[0]/isLogBDivLogDRational[1]
                         gleich sein.
                         */
                        if (isLogADivLogCRational.length == 2 && isLogBDivLogDRational.length == 2
                                && ((BigInteger) isLogADivLogCRational[0]).multiply((BigInteger) isLogBDivLogDRational[1]).compareTo(
                                        ((BigInteger) isLogADivLogCRational[1]).multiply((BigInteger) isLogBDivLogDRational[0])) == 0) {

                            if (((BigInteger) isLogADivLogCRational[0]).compareTo(BigInteger.ONE) == 0) {
                                factorsEnumerator.remove(i);
                            } else {
                                factorsEnumerator.put(i, new Constant((BigInteger) isLogADivLogCRational[0]));
                            }

                            if (((BigInteger) isLogADivLogCRational[1]).compareTo(BigInteger.ONE) == 0) {
                                factorsDenominator.remove(j);
                            } else {
                                factorsDenominator.put(j, new Constant((BigInteger) isLogADivLogCRational[1]));
                            }

                            break;

                        }

                    }
                }

            }

            // Dasselbe wie oben, nur mit nichttrivialen Potenzen
            if (factorsEnumerator.get(i) != null && factorsEnumerator.get(i).isPower()
                    && ((BinaryOperation) factorsEnumerator.get(i)).getLeft() instanceof Function
                    && ((Function) ((BinaryOperation) factorsEnumerator.get(i)).getLeft()).getType().equals(logType)
                    && ((Function) ((BinaryOperation) factorsEnumerator.get(i)).getLeft()).getLeft().isIntegerConstant()) {

                // Fall: ln(a)^m/ln(b)^n mit ganzen a, b.
                for (int j = 0; j < factorsDenominator.getBound(); j++) {
                    if (factorsDenominator.get(j) != null && factorsDenominator.get(j).isPower()
                            && ((BinaryOperation) factorsDenominator.get(j)).getLeft() instanceof Function
                            && ((Function) ((BinaryOperation) factorsDenominator.get(j)).getLeft()).getType().equals(logType)
                            && ((Function) ((BinaryOperation) factorsDenominator.get(j)).getLeft()).getLeft().isIntegerConstant()) {

                        Expression m = ((BinaryOperation) factorsEnumerator.get(i)).getRight();
                        Expression n = ((BinaryOperation) factorsDenominator.get(j)).getRight();

                        /*
                         Nun wird vereinfacht, falls die Exponenten rationale
                         Zahlen sind, oder gleich sind.
                         */
                        if (m.isIntegerConstantOrRationalConstant() && n.isIntegerConstantOrRationalConstant() && !m.equivalent(n)) {
                            Expression k = m.sub(n).simplify();
                            a = ((Constant) ((Function) ((BinaryOperation) factorsEnumerator.get(i)).getLeft()).getLeft()).getValue().toBigInteger();
                            b = ((Constant) ((Function) ((BinaryOperation) factorsDenominator.get(j)).getLeft()).getLeft()).getValue().toBigInteger();
                            Object[] isRational = isQuotientOfLogarithmsRational(a, b);
                            if (k.isNonNegative()) {
                                if (isRational.length == 2) {
                                    if (((BigInteger) isRational[0]).compareTo(BigInteger.ONE) == 0) {
                                        factorsEnumerator.put(i, new Function(new Constant(a), logType).pow(k));
                                    } else {
                                        factorsEnumerator.put(i, new Constant((BigInteger) isRational[0]).pow(n).mult(new Function(new Constant(a), logType).pow(k)));
                                    }
                                    if (((BigInteger) isRational[1]).compareTo(BigInteger.ONE) == 0) {
                                        factorsDenominator.remove(j);
                                    } else {
                                        factorsDenominator.put(j, new Constant((BigInteger) isRational[1]).pow(n));
                                    }
                                    break;
                                }
                            } else {
                                if (isRational.length == 2) {
                                    if (((BigInteger) isRational[0]).compareTo(BigInteger.ONE) == 0) {
                                        factorsEnumerator.remove(i);
                                    } else {
                                        factorsEnumerator.put(i, new Constant((BigInteger) isRational[0]).pow(m));
                                    }
                                    if (((BigInteger) isRational[1]).compareTo(BigInteger.ONE) == 0) {
                                        factorsDenominator.put(j, new Function(new Constant(a), logType).pow(n.sub(m).simplify()));
                                    } else {
                                        factorsDenominator.put(j, new Constant((BigInteger) isRational[1]).pow(m).mult(new Function(new Constant(b), logType).pow(n.sub(m).simplify())));
                                    }
                                    break;
                                }
                            }
                        } else if (m.equivalent(n)) {
                            // Fall: m = n.
                            a = ((Constant) ((Function) ((BinaryOperation) factorsEnumerator.get(i)).getLeft()).getLeft()).getValue().toBigInteger();
                            b = ((Constant) ((Function) ((BinaryOperation) factorsDenominator.get(j)).getLeft()).getLeft()).getValue().toBigInteger();
                            Object[] isRational = isQuotientOfLogarithmsRational(a, b);
                            if (isRational.length == 2) {
                                if (((BigInteger) isRational[0]).compareTo(BigInteger.ONE) == 0) {
                                    factorsEnumerator.remove(i);
                                } else {
                                    factorsEnumerator.put(i, new Constant((BigInteger) isRational[0]).pow(m));
                                }
                                if (((BigInteger) isRational[1]).compareTo(BigInteger.ONE) == 0) {
                                    factorsDenominator.remove(j);
                                } else {
                                    factorsDenominator.put(j, new Constant((BigInteger) isRational[1]).pow(m));
                                }
                                break;
                            }
                        }

                    }
                }

            } else if (factorsEnumerator.get(i) != null && factorsEnumerator.get(i).isPower()
                    && ((BinaryOperation) factorsEnumerator.get(i)).getLeft() instanceof Function
                    && ((Function) ((BinaryOperation) factorsEnumerator.get(i)).getLeft()).getType().equals(logType)
                    && ((Function) ((BinaryOperation) factorsEnumerator.get(i)).getLeft()).getLeft().isRationalConstant()) {

                // Fall: ln(a/b)^m/ln(c/d)^n mit ganzen a, b, c, d.
                for (int j = 0; j < factorsDenominator.getBound(); j++) {
                    if (factorsDenominator.get(j) != null && factorsDenominator.get(j).isPower()
                            && ((BinaryOperation) factorsDenominator.get(j)).getLeft() instanceof Function
                            && ((Function) ((BinaryOperation) factorsDenominator.get(j)).getLeft()).getType().equals(logType)
                            && ((Function) ((BinaryOperation) factorsDenominator.get(j)).getLeft()).getLeft().isRationalConstant()) {

                        a = ((Constant) ((BinaryOperation) ((Function) ((BinaryOperation) factorsEnumerator.get(i)).getLeft()).getLeft()).getLeft()).getValue().toBigInteger();
                        b = ((Constant) ((BinaryOperation) ((Function) ((BinaryOperation) factorsEnumerator.get(i)).getLeft()).getLeft()).getRight()).getValue().toBigInteger();
                        c = ((Constant) ((BinaryOperation) ((Function) ((BinaryOperation) factorsDenominator.get(j)).getLeft()).getLeft()).getLeft()).getValue().toBigInteger();
                        d = ((Constant) ((BinaryOperation) ((Function) ((BinaryOperation) factorsDenominator.get(j)).getLeft()).getLeft()).getRight()).getValue().toBigInteger();
                        Object[] isLogADivLogCRational = isQuotientOfLogarithmsRational(a, c);
                        Object[] isLogBDivLogDRational = isQuotientOfLogarithmsRational(b, d);
                        /*
                         Nun müssen die Verhältnisse
                         isLogADivLogCRational[0]/isLogADivLogCRational[1] und
                         isLogBDivLogDRational[0]/isLogBDivLogDRational[1]
                         gleich sein.
                         */
                        if (isLogADivLogCRational.length == 2 && isLogBDivLogDRational.length == 2
                                && ((BigInteger) isLogADivLogCRational[0]).multiply((BigInteger) isLogBDivLogDRational[1]).compareTo(
                                        ((BigInteger) isLogADivLogCRational[1]).multiply((BigInteger) isLogBDivLogDRational[0])) == 0) {

                            Expression m = ((BinaryOperation) factorsEnumerator.get(i)).getRight();
                            Expression n = ((BinaryOperation) factorsDenominator.get(j)).getRight();
                            /*
                             Nun wird vereinfacht, falls die Exponenten
                             rationale Zahlen sind, oder gleich sind.
                             */
                            if (m.isIntegerConstantOrRationalConstant() && n.isIntegerConstantOrRationalConstant() && !m.equivalent(n)) {
                                Expression k = m.sub(n).simplify();
                                if (k.isNonNegative()) {
                                    if (((BigInteger) isLogADivLogCRational[0]).compareTo(BigInteger.ONE) == 0) {
                                        factorsEnumerator.put(i, new Function(new Constant(a).div(b), logType).pow(k));
                                    } else {
                                        factorsEnumerator.put(i, new Constant((BigInteger) isLogADivLogCRational[0]).pow(n).mult(new Function(new Constant(a).div(b), logType).pow(k)));
                                    }
                                    if (((BigInteger) isLogADivLogCRational[1]).compareTo(BigInteger.ONE) == 0) {
                                        factorsDenominator.remove(j);
                                    } else {
                                        factorsDenominator.put(j, new Constant((BigInteger) isLogADivLogCRational[1]).pow(n));
                                    }
                                    break;
                                } else {
                                    if (((BigInteger) isLogADivLogCRational[0]).compareTo(BigInteger.ONE) == 0) {
                                        factorsEnumerator.remove(i);
                                    } else {
                                        factorsEnumerator.put(i, new Constant((BigInteger) isLogADivLogCRational[0]).pow(m));
                                    }
                                    if (((BigInteger) isLogADivLogCRational[1]).compareTo(BigInteger.ONE) == 0) {
                                        factorsDenominator.put(j, new Function(new Constant(c).div(d), logType).pow(n.sub(m).simplify()));
                                    } else {
                                        factorsDenominator.put(j, new Constant((BigInteger) isLogADivLogCRational[1]).pow(m).mult(new Function(new Constant(c).div(d), logType).pow(n.sub(m).simplify())));
                                    }
                                    break;
                                }
                            } else if (m.equivalent(n)) {
                                // Fall: m = n.
                                if (((BigInteger) isLogADivLogCRational[0]).compareTo(BigInteger.ONE) == 0) {
                                    factorsEnumerator.remove(i);
                                } else {
                                    factorsEnumerator.put(i, new Constant((BigInteger) isLogADivLogCRational[0]).pow(m));
                                }
                                if (((BigInteger) isLogADivLogCRational[1]).compareTo(BigInteger.ONE) == 0) {
                                    factorsDenominator.remove(j);
                                } else {
                                    factorsDenominator.put(j, new Constant((BigInteger) isLogADivLogCRational[1]).pow(m));
                                }
                                break;
                            }

                        }

                    }
                }

            }

        }

    }

    /**
     * Gibt false zurück, falls log(b)/log(a) = ln(b)/ln(a) NICHT rational ist,
     * ansonsten werden zwei BigInteger m, n mit m/n = log(b)/log(a)
     * zurückgegeben. Wird in simplifyQuotientsOfLogarithms() benötigt, um zu
     * entscheiden, ob sich zwei Logarithmen zu einem rationalen Bruch kürzen
     * lassen.
     */
    public static Object[] isQuotientOfLogarithmsRational(BigInteger a, BigInteger b) {

        if (a.compareTo(BigInteger.ONE) <= 0 || b.compareTo(BigInteger.ONE) <= 0) {
            Object[] result = new Object[1];
            result[0] = false;
            return result;
        }
        if (a.compareTo(b) < 0) {
            Object[] resultOfInterchangedArguments = isQuotientOfLogarithmsRational(b, a);
            if (resultOfInterchangedArguments.length == 1) {
                return resultOfInterchangedArguments;
            }
            Object[] result = new Object[2];
            result[0] = resultOfInterchangedArguments[1];
            result[1] = resultOfInterchangedArguments[0];
            return result;
        }

        HashMap<Integer, Integer> powers = new HashMap<>();
        // Temporärer Platzhalter zum Kopieren von Parametern.
        BigInteger argumentCopy;
        int k;

        // Funktionsweise: ähnlich wie der Euklid-Algorithmus.
        while (a.mod(b).compareTo(BigInteger.ZERO) == 0) {
            k = 0;
            while (a.mod(b).compareTo(BigInteger.ZERO) == 0) {
                a = a.divide(b);
                k++;
            }
            powers.put(powers.size(), k);
            argumentCopy = b;
            b = a;
            a = argumentCopy;
            if (b.compareTo(BigInteger.ONE) == 0) {
                break;
            }
        }

        if (b.compareTo(BigInteger.ONE) > 0) {
            // Dann ist der Quotient der Logarithmen nicht rational.
            Object[] result = new Object[1];
            result[0] = false;
            return result;
        }

        BigInteger m = BigInteger.valueOf(powers.get(powers.size() - 1)), n = BigInteger.ONE;
        for (int i = powers.size() - 2; i >= 0; i--) {
            argumentCopy = n;
            n = m;
            m = argumentCopy.add(BigInteger.valueOf(powers.get(i)).multiply(n));
        }

        Object[] result = new Object[2];
        result[0] = m;
        result[1] = n;
        return result;

    }

    /**
     * Vereinfacht Folgendes: exp(a + ln(b) + c + ...) = b * exp(a + c + ...)
     */
    public static Expression reduceExpOfSumsOfLn(Function expr) {
        if (!expr.getType().equals(TypeFunction.exp)) {
            return expr;
        }
        ExpressionCollection summands = SimplifyUtilities.getSummands(expr.getLeft());
        ExpressionCollection resultFactorsOutsideOfExp = new ExpressionCollection();
        ExpressionCollection resultSummandsInExp = new ExpressionCollection();
        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) instanceof Function && ((Function) summands.get(i)).getType().equals(TypeFunction.ln)) {
                resultFactorsOutsideOfExp.add(((Function) summands.get(i)).getLeft());
                summands.remove(i);
            }
        }
        if (resultFactorsOutsideOfExp.isEmpty()) {
            return expr;
        }
        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) != null) {
                resultSummandsInExp.add(summands.get(i));
            }
        }
        return SimplifyUtilities.produceProduct(resultFactorsOutsideOfExp).mult(SimplifyUtilities.produceSum(resultSummandsInExp).exp());
    }

    /**
     * Vereinfacht wie in reduceExpOfSumsOfLn, nur für Differenzen in exp.
     */
    public static Expression reduceExpOfDifferencesOfLn(Function expr) {
        if (!expr.getType().equals(TypeFunction.exp) || expr.getLeft().isNotDifference()) {
            return expr;
        }
        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getLeft());
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getLeft());
        ExpressionCollection resultFactorsInEnumeratorOutsideOfExp = new ExpressionCollection();
        ExpressionCollection resultFactorsInDenominatorOutsideOfExp = new ExpressionCollection();
        for (int i = 0; i < summandsLeft.getBound(); i++) {
            if (summandsLeft.get(i) instanceof Function && ((Function) summandsLeft.get(i)).getType().equals(TypeFunction.ln)) {
                resultFactorsInEnumeratorOutsideOfExp.add(((Function) summandsLeft.get(i)).getLeft());
                summandsLeft.remove(i);
            }
        }
        for (int i = 0; i < summandsRight.getBound(); i++) {
            if (summandsRight.get(i) instanceof Function && ((Function) summandsRight.get(i)).getType().equals(TypeFunction.ln)) {
                resultFactorsInDenominatorOutsideOfExp.add(((Function) summandsRight.get(i)).getLeft());
                summandsRight.remove(i);
            }
        }
        if (resultFactorsInEnumeratorOutsideOfExp.isEmpty() && resultFactorsInDenominatorOutsideOfExp.isEmpty()) {
            return expr;
        } else if (!resultFactorsInEnumeratorOutsideOfExp.isEmpty() && resultFactorsInDenominatorOutsideOfExp.isEmpty()) {
            return SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfExp).mult(
                    SimplifyUtilities.produceSum(summandsLeft).sub(((BinaryOperation) expr.getLeft()).getRight()).exp());
        } else if (resultFactorsInEnumeratorOutsideOfExp.isEmpty() && !resultFactorsInDenominatorOutsideOfExp.isEmpty()) {
            return ((BinaryOperation) expr.getLeft()).getLeft().sub(SimplifyUtilities.produceSum(summandsRight)).exp().div(
                    SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfExp));
        }
        return SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfExp).mult(
                SimplifyUtilities.produceSum(summandsLeft).sub(SimplifyUtilities.produceSum(summandsRight)).exp()).div(
                        SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfExp));
    }

    /**
     * Vereinfacht Folgendes: ln(a * exp(b) * c * ...) = b + ln(a * c * ...)
     */
    public static Expression reduceLnOfProductsOfExp(Function expr) {
        if (!expr.getType().equals(TypeFunction.ln)) {
            return expr;
        }
        ExpressionCollection factors = SimplifyUtilities.getFactors(expr.getLeft());
        ExpressionCollection resultSummandsOutsideOfLn = new ExpressionCollection();
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) instanceof Function && ((Function) factors.get(i)).getType().equals(TypeFunction.exp)) {
                resultSummandsOutsideOfLn.add(((Function) factors.get(i)).getLeft());
                factors.remove(i);
            }
        }
        if (resultSummandsOutsideOfLn.isEmpty()) {
            return expr;
        }
        return SimplifyUtilities.produceSum(resultSummandsOutsideOfLn).add(SimplifyUtilities.produceProduct(factors).ln());
    }

    /**
     * Vereinfacht wie in reduceLnOfProductsOfExp, nur für Quotienten in ln.
     */
    public static Expression reduceLnOfQuotientsOfExp(Function expr) {
        if (!expr.getType().equals(TypeFunction.ln) || expr.getLeft().isNotQuotient()) {
            return expr;
        }
        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(expr.getLeft());
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(expr.getLeft());
        ExpressionCollection resultSummandsLeftOutsideOfLn = new ExpressionCollection();
        ExpressionCollection resultSummandsRightOutsideOfLn = new ExpressionCollection();
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i) instanceof Function && ((Function) factorsEnumerator.get(i)).getType().equals(TypeFunction.exp)) {
                resultSummandsLeftOutsideOfLn.add(((Function) factorsEnumerator.get(i)).getLeft());
                factorsEnumerator.remove(i);
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i) instanceof Function && ((Function) factorsDenominator.get(i)).getType().equals(TypeFunction.exp)) {
                resultSummandsRightOutsideOfLn.add(((Function) factorsDenominator.get(i)).getLeft());
                factorsDenominator.remove(i);
            }
        }
        if (resultSummandsLeftOutsideOfLn.isEmpty() && resultSummandsRightOutsideOfLn.isEmpty()) {
            return expr;
        } else if (!resultSummandsLeftOutsideOfLn.isEmpty() && resultSummandsRightOutsideOfLn.isEmpty()) {
            return SimplifyUtilities.produceSum(resultSummandsLeftOutsideOfLn).add(
                    SimplifyUtilities.produceProduct(factorsEnumerator).div(((BinaryOperation) expr.getLeft()).getRight()).ln());
        } else if (resultSummandsLeftOutsideOfLn.isEmpty() && !resultSummandsRightOutsideOfLn.isEmpty()) {
            return ((BinaryOperation) expr.getLeft()).getLeft().div(SimplifyUtilities.produceProduct(factorsDenominator)).ln().sub(
                    SimplifyUtilities.produceSum(resultSummandsRightOutsideOfLn));
        }
        return SimplifyUtilities.produceSum(resultSummandsLeftOutsideOfLn).add(
                SimplifyUtilities.produceProduct(factorsEnumerator).div(SimplifyUtilities.produceProduct(factorsDenominator)).ln()).sub(
                        SimplifyUtilities.produceSum(resultSummandsRightOutsideOfLn));
    }

    /**
     * Liefert die Anzahl der Nullen am Ende der Zahl. Beispielsweise liefert es
     * bei 570000 dann 4.
     */
    public static int getExponentIfDivisibleByPowerOfTen(BigInteger value) {

        if (value.compareTo(BigInteger.ZERO) == 0 || value.compareTo(BigInteger.ONE) == 0) {
            /*
             Ist value == 0, dann auch 0 ausgeben (obwohl dies sinnlos ist und
             nie verwendet wird). Grund: Vermeidung von theoretischen
             Endlosschleifen weiter unten.
             */
            return 0;
        }
        int exponentOfTen = 0;
        while (value.mod(BigInteger.TEN).compareTo(BigInteger.ZERO) == 0) {
            value = value.divide(BigInteger.TEN);
            exponentOfTen++;
        }
        return exponentOfTen;

    }

    /**
     * Vereinfacht Folgendes: lg(a * 2700^b * c * ...) = b*(2 + lg(27)) + lg(a *
     * c * ...)
     */
    public static Expression reduceLgOfProductsOfPowersOf10(Function expr) {
        if (!expr.getType().equals(TypeFunction.lg)) {
            return expr;
        }
        ExpressionCollection factors = SimplifyUtilities.getFactors(expr.getLeft());
        ExpressionCollection resultSummandsOutsideOfLg = new ExpressionCollection();
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i).isPower() && ((BinaryOperation) factors.get(i)).getLeft().isIntegerConstant()) {
                int exponent = getExponentIfDivisibleByPowerOfTen(((Constant) ((BinaryOperation) factors.get(i)).getLeft()).getValue().toBigInteger());
                if (exponent > 0) {
                    BigDecimal resultBase = ((Constant) ((BinaryOperation) factors.get(i)).getLeft()).getValue().divide(BigDecimal.TEN.pow(exponent));
                    resultSummandsOutsideOfLg.add(((BinaryOperation) factors.get(i)).getRight().mult(new Constant(exponent).add(new Constant(resultBase).lg())));
                    factors.remove(i);
                }
            } else if (factors.get(i).isIntegerConstant()) {
                int exponent = getExponentIfDivisibleByPowerOfTen(((Constant) factors.get(i)).getValue().toBigInteger());
                if (exponent > 0) {
                    BigDecimal resultBase = ((Constant) factors.get(i)).getValue().divide(BigDecimal.TEN.pow(exponent));
                    resultSummandsOutsideOfLg.add(new Constant(exponent));
                    factors.put(i, new Constant(resultBase));
                }
            }
        }
        if (resultSummandsOutsideOfLg.isEmpty()) {
            return expr;
        }
        return SimplifyUtilities.produceSum(resultSummandsOutsideOfLg).add(SimplifyUtilities.produceProduct(factors).lg());
    }

    /**
     * Dasselbe wie reduceLgOfProductsOfPowersOf10(), nur für Quotienten
     */
    public static Expression reduceLgOfQuotientsOfPowersOf10(Function expr) {
        if (!expr.getType().equals(TypeFunction.lg) || expr.getLeft().isNotQuotient()) {
            return expr;
        }
        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(expr.getLeft());
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(expr.getLeft());
        ExpressionCollection resultSummandsLeftOutsideOfLg = new ExpressionCollection();
        ExpressionCollection resultSummandsRightOutsideOfLg = new ExpressionCollection();
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i).isPower() && ((BinaryOperation) factorsEnumerator.get(i)).getLeft().isIntegerConstant()) {
                int exponent = getExponentIfDivisibleByPowerOfTen(((Constant) ((BinaryOperation) factorsEnumerator.get(i)).getLeft()).getValue().toBigInteger());
                if (exponent > 0) {
                    BigDecimal resultBase = ((Constant) ((BinaryOperation) factorsEnumerator.get(i)).getLeft()).getValue().divide(BigDecimal.TEN.pow(exponent));
                    resultSummandsLeftOutsideOfLg.add(((BinaryOperation) factorsEnumerator.get(i)).getRight().mult(new Constant(exponent).add(new Function(new Constant(resultBase), TypeFunction.lg))));
                    factorsEnumerator.remove(i);
                }
            } else if (factorsEnumerator.get(i).isIntegerConstant()) {
                int exponent = getExponentIfDivisibleByPowerOfTen(((Constant) factorsEnumerator.get(i)).getValue().toBigInteger());
                if (exponent > 0) {
                    BigDecimal resultBase = ((Constant) factorsEnumerator.get(i)).getValue().divide(BigDecimal.TEN.pow(exponent));
                    resultSummandsLeftOutsideOfLg.add(new Constant(exponent));
                    factorsEnumerator.put(i, new Constant(resultBase));
                }
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i).isPower() && ((BinaryOperation) factorsDenominator.get(i)).getLeft().isIntegerConstant()) {
                int exponent = getExponentIfDivisibleByPowerOfTen(((Constant) ((BinaryOperation) factorsDenominator.get(i)).getLeft()).getValue().toBigInteger());
                if (exponent > 0) {
                    BigDecimal resultBase = ((Constant) ((BinaryOperation) factorsDenominator.get(i)).getLeft()).getValue().divide(BigDecimal.TEN.pow(exponent));
                    resultSummandsRightOutsideOfLg.add(((BinaryOperation) factorsDenominator.get(i)).getRight().mult(new Constant(exponent).add(new Function(new Constant(resultBase), TypeFunction.lg))));
                    factorsDenominator.remove(i);
                }
            } else if (factorsDenominator.get(i).isIntegerConstant()) {
                int exponent = getExponentIfDivisibleByPowerOfTen(((Constant) factorsDenominator.get(i)).getValue().toBigInteger());
                if (exponent > 0) {
                    BigDecimal resultBase = ((Constant) factorsDenominator.get(i)).getValue().divide(BigDecimal.TEN.pow(exponent));
                    resultSummandsRightOutsideOfLg.add(new Constant(exponent));
                    factorsDenominator.put(i, new Constant(resultBase));
                }
            }
        }
        if (resultSummandsLeftOutsideOfLg.isEmpty() && resultSummandsRightOutsideOfLg.isEmpty()) {
            return expr;
        } else if (!resultSummandsLeftOutsideOfLg.isEmpty() && resultSummandsRightOutsideOfLg.isEmpty()) {
            return SimplifyUtilities.produceSum(resultSummandsLeftOutsideOfLg).add(
                    SimplifyUtilities.produceProduct(factorsEnumerator).div(((BinaryOperation) expr.getLeft()).getRight()).lg());
        } else if (resultSummandsLeftOutsideOfLg.isEmpty() && !resultSummandsRightOutsideOfLg.isEmpty()) {
            return ((BinaryOperation) expr.getLeft()).getLeft().div(SimplifyUtilities.produceProduct(factorsDenominator)).lg().sub(
                    SimplifyUtilities.produceSum(resultSummandsRightOutsideOfLg));
        }
        return SimplifyUtilities.produceSum(resultSummandsLeftOutsideOfLg).add(
                SimplifyUtilities.produceProduct(factorsEnumerator).div(SimplifyUtilities.produceProduct(factorsDenominator)).lg()).sub(
                        SimplifyUtilities.produceProduct(resultSummandsRightOutsideOfLg));
    }

}
