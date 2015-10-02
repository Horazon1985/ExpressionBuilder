package expressionsimplifymethods;

import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import exceptions.EvaluationException;
import expressionbuilder.Expression;
import java.math.BigInteger;
import translator.Translator;

public abstract class SimplifyUtilities {

    /**
     * Liefert, ob terms und termsToCompare äquivalente Einträge besitzen.
     */
    public static boolean equivalent(ExpressionCollection terms, ExpressionCollection termsToCompare) {

        if (terms.getBound() != termsToCompare.getBound()) {
            return false;
        }

        for (int i = 0; i < terms.getBound(); i++) {
            if ((terms.get(i) == null && termsToCompare.get(i) != null)
                    || (terms.get(i) != null && termsToCompare.get(i) == null)) {
                return false;
            }
            if (terms.get(i) != null && !terms.get(i).equivalent(termsToCompare.get(i))) {
                return false;
            }
        }

        return true;

    }

    /**
     * Gibt eine ExpressionCollection zurück, in der alle Elemente von terms
     * enthalten sind, wo aber keine äquivalenten Elemente doppelt auftreten.
     */
    public static ExpressionCollection removeMultipleEntries(ExpressionCollection terms) {

        ExpressionCollection result = new ExpressionCollection();
        boolean entryIsAlreadyContained;

        for (int i = 0; i < terms.getBound(); i++) {
            if (terms.get(i) == null) {
                continue;
            }
            entryIsAlreadyContained = false;
            for (int j = 0; j < result.getBound(); j++) {
                if (result.get(j).equivalent(terms.get(i))) {
                    entryIsAlreadyContained = true;
                    break;
                }
            }
            if (!entryIsAlreadyContained) {
                result.add(terms.get(i));
            }
        }

        return result;

    }

    /**
     * Liefert den Durchschnitt von termsLeft und termsRight (mit Vielfachheiten
     * gezählt!).
     */
    public static ExpressionCollection intersection(ExpressionCollection termsLeft, ExpressionCollection termsRight) {

        ExpressionCollection termsLeftCopy = ExpressionCollection.copy(termsLeft);
        ExpressionCollection termsRightCopy = ExpressionCollection.copy(termsRight);
        ExpressionCollection result = new ExpressionCollection();

        for (int i = 0; i < termsLeft.getBound(); i++) {
            for (int j = 0; j < termsRight.getBound(); j++) {
                if (termsLeftCopy.get(i) != null && termsRightCopy.get(j) != null && termsLeftCopy.get(i).equivalent(termsRightCopy.get(j))) {
                    result.add(termsLeftCopy.get(i));
                    termsLeftCopy.remove(i);
                    termsRightCopy.remove(j);
                }
            }
        }

        return result;

    }

    /**
     * Liefert die Differenz termsLeft \ termsRight (mit Vielfachheiten
     * gezählt!).
     */
    public static ExpressionCollection difference(ExpressionCollection termsLeft, ExpressionCollection termsRight) {

        ExpressionCollection result = new ExpressionCollection();
        /*
         termsLeft und termsRight werden in manchen Prozeduren noch
         nachträglich gebraucht und sollten nicht verändert werden ->
         termsLeft und termsRight kopieren.
         */
        ExpressionCollection termsLeftCopy = ExpressionCollection.copy(termsLeft);
        ExpressionCollection termsRightCopy = ExpressionCollection.copy(termsRight);

        Expression termLeft, termRight;
        boolean equivalentTermFound;

        for (int i = 0; i < termsLeft.getBound(); i++) {

            if (termsLeftCopy.get(i) == null) {
                continue;
            }

            termLeft = termsLeftCopy.get(i);
            equivalentTermFound = false;
            for (int j = 0; j < termsRight.getBound(); j++) {

                if (termsRightCopy.get(j) == null) {
                    continue;
                }

                termRight = termsRightCopy.get(j);
                if (termLeft.equivalent(termRight)) {
                    equivalentTermFound = true;
                    termsLeftCopy.remove(i);
                    termsRightCopy.remove(j);
                    break;
                }

            }
            if (!equivalentTermFound) {
                result.add(termLeft);
            }

        }

        return result;

    }

    /**
     * Vereinigt zwei ExpressionCollection, welche via 0, 1, 2, ... indiziert
     * sind und Expressions enthalten. Elemente in termsRight, welche in
     * termsLeft bereits vorkommen, werden NICHT mitaufgenommen.
     */
    public static ExpressionCollection union(ExpressionCollection termsLeft, ExpressionCollection termsRight) {

        /*
         termsLeft und termsRight werden in manchen Prozeduren noch
         nachträglich gebraucht und sollten nicht verändert werden ->
         termsLeft und termsRight kopieren.
         */
        ExpressionCollection termsLeftCopy = ExpressionCollection.copy(termsLeft);
        boolean termIsContainedInTermsLeft;

        for (int i = 0; i < termsRight.getBound(); i++) {
            termIsContainedInTermsLeft = false;
            for (int j = 0; j < termsLeft.getBound(); j++) {
                if (termsLeftCopy.get(j).equivalent(termsRight.get(i))) {
                    termIsContainedInTermsLeft = true;
                    break;
                }
            }
            if (!termIsContainedInTermsLeft) {
                termsLeftCopy.add(termsRight.get(i));
            }
        }

        return termsLeftCopy;

    }

    /**
     * Die Elemente von factors stellen Faktoren eines Ausdrucks dar. Es wird
     * die ExpressionCollection folgenden Faktoren zurückgegeben: alle Faktoren
     * in factors mit einer gemeinsamen Basis und ganzzahlige Exponenten werden
     * zu einem Faktor mit deselben Basis und dem maximalen Exponenten
     * zusammengefasst. Beispiel: factors = {a, 2, 4, 2, a^3, b, a^2, a^x}. Dann
     * wird {a^3, 2^2, 4, b, a^x} zurückgegeben.
     */
    public static ExpressionCollection collectFactorsByPowers(ExpressionCollection factors) {

        ExpressionCollection result = new ExpressionCollection();

        Expression base;
        BigInteger exponent;
        Expression resultBase;
        BigInteger resultExponent;

        while (!factors.isEmpty()) {

            for (int i = 0; i < factors.getBound(); i++) {

                if (factors.get(i) == null) {
                    continue;
                }
                if (factors.get(i).isPower()
                        && ((BinaryOperation) factors.get(i)).getRight().isIntegerConstant()
                        && ((BinaryOperation) factors.get(i)).getRight().isNonNegative()) {
                    resultBase = ((BinaryOperation) factors.get(i)).getLeft();
                    resultExponent = ((Constant) ((BinaryOperation) factors.get(i)).getRight()).getValue().toBigInteger();
                    factors.remove(i);
                } else {
                    resultBase = factors.get(i);
                    resultExponent = BigInteger.ONE;
                    factors.remove(i);
                }

                for (int j = i + 1; j < factors.getBound(); j++) {

                    if (factors.get(j) == null) {
                        continue;
                    }
                    if (factors.get(j).isPower()
                            && ((BinaryOperation) factors.get(j)).getRight().isIntegerConstant()
                            && ((BinaryOperation) factors.get(j)).getRight().isNonNegative()) {
                        base = ((BinaryOperation) factors.get(j)).getLeft();
                        exponent = ((Constant) ((BinaryOperation) factors.get(j)).getRight()).getValue().toBigInteger();
                    } else {
                        base = factors.get(j);
                        exponent = BigInteger.ONE;
                    }

                    if (base.equivalent(resultBase)) {
                        resultExponent = resultExponent.add(exponent);
                        factors.remove(j);
                    }

                }

                if (resultExponent.compareTo(BigInteger.ONE) == 0) {
                    result.add(resultBase);
                } else {
                    result.add(resultBase.pow(resultExponent));
                }

            }

        }

        return result;

    }

    /**
     * Fügt der ExpressionCollection summands alle Summanden von expr hinzu,
     * falls man expr als Summe auffasst. Die Keys sind 0, 1, 2, ..., size - 1.
     */
    private static void addSummands(Expression expr, ExpressionCollection summands) {
        if (expr.isSum()) {
            addSummands(((BinaryOperation) expr).getLeft(), summands);
            addSummands(((BinaryOperation) expr).getRight(), summands);
        } else if (!expr.equals(Expression.ZERO) || summands.isEmpty()) {
            /*
             Überflüssige Nullen sollen nicht mitaufgenommen werden, jedoch
             mindestens eine Null, falls summands sonst drohen würde, leer zu
             sein. Im approximativen Fall wird alles mitaufgenommen, da die
             Konstante Expression.ZERO precise == true besitzt.
             */
            summands.add(expr);
        }
    }

    /**
     * Gibt eine ExpressionCollection mit allen Summanden von expr hinzu, falls
     * man expr als Summe auffasst. Die Keys sind 0, 1, 2, ..., size - 1.
     */
    public static ExpressionCollection getSummands(Expression expr) {
        ExpressionCollection summands = new ExpressionCollection();
        addSummands(expr, summands);
        return summands;
    }

    /**
     * Gibt eine ExpressionCollection mit Summanden von expr zurück, welche die
     * Variablen var nicht enthalten. Die Keys sind 0, 1, 2, ..., size - 1.
     */
    public static ExpressionCollection getConstantSummands(Expression expr, String var) {

        ExpressionCollection summands = getSummands(expr);
        ExpressionCollection constantSummands = new ExpressionCollection();
        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) != null && !summands.get(i).contains(var)) {
                constantSummands.add(summands.get(i));
            }
        }

        if (constantSummands.isEmpty()) {
            constantSummands.put(0, Expression.ZERO);
        }

        return constantSummands;

    }

    /**
     * Gibt eine ExpressionCollection mit Summanden von expr zurück, welche die
     * Variablen var enthalten. Die Keys sind 0, 1, 2, ..., size - 1.
     */
    public static ExpressionCollection getNonConstantSummands(Expression expr, String var) {

        ExpressionCollection summands = getSummands(expr);
        ExpressionCollection constantSummands = new ExpressionCollection();
        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) != null && summands.get(i).contains(var)) {
                constantSummands.add(summands.get(i));
            }
        }

        if (constantSummands.isEmpty()) {
            constantSummands.put(0, Expression.ZERO);
        }

        return constantSummands;

    }

    /**
     * Liefert Summanden im Minuenden eines Ausdrucks. VORAUSSETZUNG: Der
     * Ausdruck muss in folgender Form sein, falls in diesem Differenzen
     * auftauchen: (...) - (...). Verboten sind also Ausdrücke wie a-(b-c) etc.
     * Ist expr keine Differenz, so wird expr als Minuend angesehen. BEISPIEL:
     * (a+b^2+exp(x)) - (u+v) liefert die Summanden {a, b^2, exp(x)} (als
     * ExpressionCollection, nummeriert via 0, 1, 2).
     */
    public static ExpressionCollection getSummandsLeftInExpression(Expression expr) {
        if (expr.isDifference()) {
            return getSummands(((BinaryOperation) expr).getLeft());
        }
        return getSummands(expr);
    }

    /**
     * Liefert Summanden im Subtrahenden eines Ausdrucks. VORAUSSETZUNG: Der
     * Ausdruck muss in folgender Form sein, falls in diesem Differenzen
     * auftauchen: (...) - (...). Verboten sind also Ausdrücke wie a-(b-c) etc.
     * Ist expr keine Differenz, so wird expr als Minuend angesehen. WICHTIG:
     * Das Ergebnis kann auch leer sein!
     */
    public static ExpressionCollection getSummandsRightInExpression(Expression expr) {
        if (expr.isDifference()) {
            return getSummands(((BinaryOperation) expr).getRight());
        }
        return new ExpressionCollection();
    }

    /**
     * Liefert Summanden im Minuenden eines Ausdrucks, welche die Variable var
     * nicht enthalten. VORAUSSETZUNG: Der Ausdruck muss in folgender Form sein,
     * falls in diesem Differenzen auftauchen: (...) - (...). Ist expr keine
     * Differenz, so wird expr als Minuend angesehen. Verboten sind also
     * Ausdrücke wie a-(b-c) etc. WICHTIG: Das Ergebnis kann auch eine leere
     * ExpressionCollection sein! BEISPIEL: expr = (a+x^3+b^2+exp(x)) - (u+v)
     * und var = "x" liefert die Summanden {a, b^2} (als ExpressionCollection,
     * nummeriert via 0, 1).
     */
    public static ExpressionCollection getConstantSummandsLeftInExpression(Expression expr, String var) {

        ExpressionCollection summands = getSummandsLeftInExpression(expr);
        ExpressionCollection constantSummands = new ExpressionCollection();

        for (int i = 0; i < summands.getBound(); i++) {
            if (!summands.get(i).contains(var)) {
                constantSummands.add(summands.get(i));
            }
        }

        return constantSummands;

    }

    /**
     * Liefert Summanden im Minuenden eines Ausdrucks, welche die Variable var
     * enthalten. VORAUSSETZUNG: Der Ausdruck muss in folgender Form sein, falls
     * in diesem Differenzen auftauchen: (...) - (...). Ist expr keine
     * Differenz, so wird expr als Minuend angesehen. Verboten sind also
     * Ausdrücke wie a-(b-c) etc. WICHTIG: Das Ergebnis kann auch eine leere
     * ExpressionCollection sein! BEISPIEL: expr = (a+x^3+b^2+exp(x)) - (u+v)
     * und var = "x" liefert die Summanden {x^3, exp(x)} (als
     * ExpressionCollection, nummeriert via 0, 1).
     */
    public static ExpressionCollection getNonConstantSummandsLeftInExpression(Expression expr, String var) {

        ExpressionCollection summands = getSummandsLeftInExpression(expr);
        ExpressionCollection nonConstantSummands = new ExpressionCollection();

        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i).contains(var)) {
                nonConstantSummands.add(summands.get(i));
            }
        }

        return nonConstantSummands;

    }

    /**
     * Liefert Summanden im Subtrahenden eines Ausdrucks, welche die Variable
     * var nicht enthalten. VORAUSSETZUNG: Der Ausdruck muss in folgender Form
     * sein, falls in diesem Differenzen auftauchen: (...) - (...). Ist expr
     * keine Differenz, so wird expr als Minuend angesehen. Verboten sind also
     * Ausdrücke wie a-(b-c) etc. WICHTIG: Das Ergebnis kann auch eine leere
     * ExpressionCollection sein! BEISPIEL: expr = (a+x^3+b^2+exp(x)) -
     * (u+v+x^5) und var = "x" liefert die Summanden {u, v} (als
     * ExpressionCollection, nummeriert via 0, 1).
     */
    public static ExpressionCollection getConstantSummandsRightInExpression(Expression expr, String var) {

        ExpressionCollection summands = getSummandsRightInExpression(expr);
        ExpressionCollection constantSummands = new ExpressionCollection();

        for (int i = 0; i < summands.getBound(); i++) {
            if (!summands.get(i).contains(var)) {
                constantSummands.add(summands.get(i));
            }
        }

        return constantSummands;

    }

    /**
     * Liefert Summanden im Subtrahenden eines Ausdrucks, welche die Variable
     * var enthalten. VORAUSSETZUNG: Der Ausdruck muss in folgender Form sein,
     * falls in diesem Differenzen auftauchen: (...) - (...). Verboten sind also
     * Ausdrücke wie a-(b-c) etc. Ist expr keine Differenz, so wird expr als
     * Minuend angesehen. WICHTIG: Das Ergebnis kann auch eine leere
     * ExpressionCollection sein! BEISPIEL: expr = (a+x^3+b^2+exp(x)) -
     * (u+v+x^5) und var = "x" liefert die Faktoren {x^5} (als
     * ExpressionCollection, nummeriert via 0).
     */
    public static ExpressionCollection getNonConstantSummandsRightInExpression(Expression expr, String var) {

        ExpressionCollection summands = getSummandsRightInExpression(expr);
        ExpressionCollection nonConstantSummands = new ExpressionCollection();

        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i).contains(var)) {
                nonConstantSummands.add(summands.get(i));
            }
        }

        return nonConstantSummands;

    }

    /**
     * Fügt der ExpressionCollection factors alle Faktoren von expr hinzu, falls
     * man expr als Produkt auffasst. Die Keys sind 0, 1, 2, ..., size - 1.
     */
    private static void addFactors(Expression expr, ExpressionCollection factors) {
        if (expr.isProduct()) {
            addFactors(((BinaryOperation) expr).getLeft(), factors);
            addFactors(((BinaryOperation) expr).getRight(), factors);
        } else if (!expr.equals(Expression.ONE) || factors.isEmpty()) {
            /*
             Überflüssige Einsen sollen nicht mitaufgenommen werden, jedoch
             mindestens eine Eins, falls factors sonst drohen würde, leer zu
             sein. Im approximativen Fall wird alles mitaufgenommen, da die
             Konstante Expression.ONE precise == true besitzt.
             */
            factors.add(expr);
        }
    }

    /**
     * Gibt eine ExpressionCollection mit allen Faktoren von expr hinzu, falls
     * man expr als Produkt auffasst. Die Keys sind 0, 1, 2, ..., size - 1.
     */
    public static ExpressionCollection getFactors(Expression expr) {
        ExpressionCollection factors = new ExpressionCollection();
        addFactors(expr, factors);
        return factors;
    }

    /**
     * Gibt eine ExpressionCollection mit Faktoren von expr zurück, welche die
     * Variablen var nicht enthalten. Die Keys sind 0, 1, 2, ..., size - 1.
     */
    public static ExpressionCollection getConstantFactors(Expression expr, String var) {

        ExpressionCollection factors = getFactors(expr);
        ExpressionCollection constantFactors = new ExpressionCollection();
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null && !factors.get(i).contains(var)) {
                constantFactors.add(factors.get(i));
            }
        }

        if (constantFactors.isEmpty()) {
            constantFactors.put(0, Expression.ONE);
        }

        return constantFactors;

    }

    /**
     * Gibt eine ExpressionCollection mit Faktoren von expr zurück, welche die
     * Variablen var enthalten. Die Keys sind 0, 1, 2, ..., size - 1.
     */
    public static ExpressionCollection getNonConstantFactors(Expression expr, String var) {

        ExpressionCollection factors = getFactors(expr);
        ExpressionCollection constantFactors = new ExpressionCollection();
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null && factors.get(i).contains(var)) {
                constantFactors.add(factors.get(i));
            }
        }

        if (constantFactors.isEmpty()) {
            constantFactors.put(0, Expression.ONE);
        }

        return constantFactors;

    }

    /**
     * Liefert Faktoren im Zähler eines Ausdrucks. VORAUSSETZUNG: Der Ausdruck
     * muss in folgender Form sein, falls in diesem Quotienten auftauchen: (...)
     * / (...). Verboten sind also Ausdrücke wie a/(b/c) etc. Ist expr kein
     * Quotient, so wird expr als Zähler angesehen. BEISPIEL:
     * (a*b^2*exp(x))/(u*v) liefert die Faktoren {a, b^2, exp(x)} (als
     * ExpressionCollection, nummeriert via 0, 1, 2).
     */
    public static ExpressionCollection getFactorsOfEnumeratorInExpression(Expression expr) {

        if (expr.isQuotient()) {
            return getFactors(((BinaryOperation) expr).getLeft());
        }
        return getFactors(expr);

    }

    /**
     * Liefert Faktoren im Nenner eines Ausdrucks. VORAUSSETZUNG: Der Ausdruck
     * muss in folgender Form sein, falls in diesem Quotienten auftauchen: (...)
     * / (...). Verboten sind also Ausdrücke wie a/(b/c) etc. Ist expr kein
     * Quotient, so wird expr als Zähler angesehen. WICHTIG: Das Ergebnis kann
     * auch leer sein!
     */
    public static ExpressionCollection getFactorsOfDenominatorInExpression(Expression expr) {

        if (expr.isQuotient()) {
            return getFactors(((BinaryOperation) expr).getRight());
        }
        return new ExpressionCollection();

    }

    /**
     * Liefert Faktoren im Zähler eines Ausdrucks, welche die Variable var nicht
     * enthalten. VORAUSSETZUNG: Der Ausdruck muss in folgender Form sein, falls
     * in diesem Quotienten auftauchen: (...) / (...). Ist expr kein Quotient,
     * so wird expr als Zähler angesehen. Verboten sind also Ausdrücke wie
     * a/(b/c) etc. WICHTIG: Das Ergebnis kann auch leer sein! BEISPIEL: expr =
     * (a*x^3*b^2*exp(x))/(u*v) und var = "x" liefert die Faktoren {x^3, exp(x)}
     * (als ExpressionCollection, nummeriert via 0, 1).
     */
    public static ExpressionCollection getConstantFactorsOfEnumeratorInExpression(Expression expr, String var) {

        ExpressionCollection factors = getFactorsOfEnumeratorInExpression(expr);
        ExpressionCollection constantFactors = new ExpressionCollection();

        for (int i = 0; i < factors.getBound(); i++) {
            if (!factors.get(i).contains(var)) {
                constantFactors.add(factors.get(i));
            }
        }

        return constantFactors;

    }

    /**
     * Liefert Faktoren im Zähler eines Ausdrucks, welche die Variable var
     * enthalten. VORAUSSETZUNG: Der Ausdruck muss in folgender Form sein, falls
     * in diesem Quotienten auftauchen: (...) / (...). Ist expr kein Quotient,
     * so wird expr als Zähler angesehen. Verboten sind also Ausdrücke wie
     * a/(b/c) etc. WICHTIG: Das Ergebnis kann auch leer sein! BEISPIEL: expr =
     * (a*x^3*b^2*exp(x))/(u*v) und var = "x" liefert die Faktoren {x^3, exp(x)}
     * (als ExpressionCollection, nummeriert via 0, 1).
     */
    public static ExpressionCollection getNonConstantFactorsOfEnumeratorInExpression(Expression expr, String var) {

        ExpressionCollection factors = getFactorsOfEnumeratorInExpression(expr);
        ExpressionCollection nonConstantFactors = new ExpressionCollection();

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i).contains(var)) {
                nonConstantFactors.add(factors.get(i));
            }
        }

        return nonConstantFactors;

    }

    /**
     * Liefert Faktoren im Nenner eines Ausdrucks, welche die Variable var nicht
     * enthalten. VORAUSSETZUNG: Der Ausdruck muss in folgender Form sein, falls
     * in diesem Quotienten auftauchen: (...) / (...). Verboten sind also
     * Ausdrücke wie a/(b/c) etc. Ist expr kein Quotient, so wird expr als
     * Zähler angesehen. WICHTIG: Das Ergebnis kann auch leer sein! BEISPIEL:
     * expr = (a*x^3*b^2*exp(x))/(sin(x)*u*v*x) und var = "x" liefert die
     * Faktoren {sin(x), x} (als ExpressionCollection, nummeriert via 0, 1).
     */
    public static ExpressionCollection getConstantFactorsOfDenominatorInExpression(Expression expr, String var) {

        ExpressionCollection factors = getFactorsOfDenominatorInExpression(expr);
        ExpressionCollection constantFactors = new ExpressionCollection();

        for (int i = 0; i < factors.getBound(); i++) {
            if (!factors.get(i).contains(var)) {
                constantFactors.add(factors.get(i));
            }
        }

        return constantFactors;

    }

    /**
     * Liefert Faktoren im Nenner eines Ausdrucks, welche die Variable var
     * enthalten. VORAUSSETZUNG: Der Ausdruck muss in folgender Form sein, falls
     * in diesem Quotienten auftauchen: (...) / (...). Verboten sind also
     * Ausdrücke wie a/(b/c) etc. Ist expr kein Quotient, so wird expr als
     * Zähler angesehen. WICHTIG: Das Ergebnis kann auch leer sein! BEISPIEL:
     * expr = (a*x^3*b^2*exp(x))/(sin(x)*u*v*x) und var = "x" liefert die
     * Faktoren {sin(x), x} (als ExpressionCollection, nummeriert via 0, 1).
     */
    public static ExpressionCollection getNonConstantFactorsOfDenominatorInExpression(Expression expr, String var) {

        ExpressionCollection factors = getFactorsOfDenominatorInExpression(expr);
        ExpressionCollection nonConstantFactors = new ExpressionCollection();

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i).contains(var)) {
                nonConstantFactors.add(factors.get(i));
            }
        }

        return nonConstantFactors;

    }

    /**
     * Bildet die Summe aus allen Termen von summands.
     */
    public static Expression produceSum(ExpressionCollection summands) {

        if (summands.isEmpty()) {
            summands.put(0, Expression.ZERO);
        }

        Expression result = Expression.ZERO;
        for (int i = summands.getBound() - 1; i >= 0; i--) {
            if (summands.get(i) != null && !summands.get(i).equals(Expression.ZERO)) {
                if (result.equals(Expression.ZERO)) {
                    result = summands.get(i);
                } else {
                    result = summands.get(i).add(result);
                }
            }
        }

        return result;

    }

    /**
     * Bildet die Differenz aus den Summen von allen Summanden in summandsLeft
     * und summandsRight.
     */
    public static Expression produceDifference(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) {

        if (summandsLeft.isEmpty() && summandsRight.isEmpty()) {
            return Expression.ZERO;
        } else if (!summandsLeft.isEmpty() && summandsRight.isEmpty()) {
            return produceSum(summandsLeft);
        } else if (summandsLeft.isEmpty() && !summandsRight.isEmpty()) {
            return Expression.MINUS_ONE.mult(produceSum(summandsRight));
        }
        return produceSum(summandsLeft).sub(produceSum(summandsRight));

    }

    /**
     * Bildet das Produkt aus allen Termen von factors.
     */
    public static Expression produceProduct(ExpressionCollection factors) {

        if (factors.isEmpty()) {
            factors.put(0, Expression.ONE);
        }

        Expression result = Expression.ONE;
        for (int i = factors.getBound() - 1; i >= 0; i--) {
            if (factors.get(i) != null && !factors.get(i).equals(Expression.ONE)) {
                if (result.equals(Expression.ONE)) {
                    result = factors.get(i);
                } else {
                    result = factors.get(i).mult(result);
                }
            }
        }

        return result;

    }

    /**
     * Bildet den Bruch aus den Produkten von allen Faktoren in
     * factorsEnumerator und factorsDenominator.
     */
    public static Expression produceQuotient(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator) {

        if (factorsEnumerator.isEmpty() && factorsDenominator.isEmpty()) {
            return Expression.ONE;
        } else if (!factorsEnumerator.isEmpty() && factorsDenominator.isEmpty()) {
            return produceProduct(factorsEnumerator);
        } else if (factorsEnumerator.isEmpty() && !factorsDenominator.isEmpty()) {
            return Expression.ONE.div(produceProduct(factorsDenominator));
        }
        return produceProduct(factorsEnumerator).div(produceProduct(factorsDenominator));

    }

    /**
     * Hilfsprozeduren für das Zusammenfassen von Konstanten.
     */
    /**
     * Sammelt bei Addition konstante Ausdrücke in summands so weit wie möglich
     * nach vorne. Die Einträge in summands sind via 0, 1, 2, ..., size - 1
     * indiziert.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection collectConstantsAndConstantExpressionsInSum(ExpressionCollection summands)
            throws EvaluationException {

        ExpressionCollection result = new ExpressionCollection();
        summands = collectConstantsInSum(summands);
        int l = summands.getBound();

        // Falls es rationale Konstanten in summands gibt, so sind diese jetzt im 0-ten Eintrag versammelt.
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
     * Hilfsmethode. Sammelt bei Addition Konstanten in summands zu einer
     * einzigen Konstante im 0-ten Eintrag. Die Einträge in h sind via 0, 1, 2,
     * ..., size - 1 indiziert. Wird NUR für
     * collectConstantsAndConstantExpressionsInSum() benötigt.
     *
     * @throws EvaluationException
     */
    private static ExpressionCollection collectConstantsInSum(ExpressionCollection summands)
            throws EvaluationException {

        ExpressionCollection result = new ExpressionCollection();

        // Im Folgenden werden Konstanten immer im 0-ten Eintrag gesammelt.
        boolean summandsContainApproximates = false;
        for (int i = 0; i < summands.getBound(); i++) {
            summandsContainApproximates = summandsContainApproximates || summands.get(i).containsApproximates();
        }

        if (!summandsContainApproximates) {

            Expression constantSummand = Expression.ZERO;
            for (int i = 0; i < summands.getBound(); i++) {
                if (summands.get(i) == null) {
                    continue;
                }
                if (summands.get(i).isIntegerConstantOrRationalConstant()) {
                    constantSummand = Constant.addTwoRationals(constantSummand, summands.get(i));
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
     * Sammelt bei Multiplikation konstante Ausdrücke in h zu einem einzigen
     * konstanten Ausdruck. Die Einträge in h sind via 0, 1, 2, ..., size - 1
     * indiziert.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection collectConstantsAndConstantExpressionsInProduct(ExpressionCollection factors)
            throws EvaluationException {

        ExpressionCollection result = new ExpressionCollection();
        factors = collectConstantsInProduct(factors);

        // Falls es rationale Konstanten in factors gibt, so sind diese jetzt im 0-ten Eintrag versammelt.
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
     * Sammelt bei Multiplikation Konstanten in factors zu einer einzigen
     * Konstante im 0-ten Eintrag. Die Einträge in factors sind via 0, 1, 2,
     * ..., size - 1 indiziert. Wird NUR für
     * collectConstantsAndConstantExpressionsInProduct() benötigt.
     *
     * @throws EvaluationException
     */
    private static ExpressionCollection collectConstantsInProduct(ExpressionCollection factors)
            throws EvaluationException {

        ExpressionCollection result = new ExpressionCollection();

        // Im Folgenden werden Konstanten immer im 0-ten Eintrag gesammelt.
        boolean factorsContainApproximatives = false;
        for (int i = 0; i < factors.getBound(); i++) {
            factorsContainApproximatives = factorsContainApproximatives || factors.get(i).containsApproximates();
        }

        if (!factorsContainApproximatives) {

            Expression constantFactor = Expression.ONE;
            for (int i = 0; i < factors.getBound(); i++) {
                if (factors.get(i).isIntegerConstantOrRationalConstant()) {
                    constantFactor = Constant.multiplyTwoRationals(constantFactor, factors.get(i));
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

        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }

            if (factors.get(i).isPower()) {
                base = ((BinaryOperation) factors.get(i)).getLeft();
                exponent = ((BinaryOperation) factors.get(i)).getRight();
            } else {
                base = (Expression) factors.get(i);
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
                    baseToCompare = (Expression) factors.get(j);
                    exponentToCompare = Expression.ONE;
                }

                /*
                 Konstante Potenzen von konstanten Zahlen NICHT auswerten!
                 Dies geschieht separat in anderen Methoden.
                 */
                if (!(base instanceof Constant && exponent instanceof Constant)
                        && !(baseToCompare instanceof Constant && exponentToCompare instanceof Constant)) {
                    /*
                     Dann sind Potenzen von ganzen Zahlen nicht
                     hineinverwickelt.
                     */
                    if (base.equivalent(baseToCompare)) {
                        exponent = exponent.add(exponentToCompare);
                        factors.put(i, base.pow(exponent));
                        factors.remove(j);
                    }
                }

                //Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                if (Thread.interrupted()) {
                    throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyMethods_COMPUTATION_ABORTED"));
                }

            }

        }

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

    public static void orderQuotient(Expression expr, ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator) {

        if (expr.isNotProduct() && expr.isNotQuotient()) {
            factorsEnumerator.add(expr);
            return;
        }

        if (expr.isProduct()) {
            orderQuotient(((BinaryOperation) expr).getLeft(), factorsEnumerator, factorsDenominator);
            orderQuotient(((BinaryOperation) expr).getRight(), factorsEnumerator, factorsDenominator);
        } else {
            orderQuotient(((BinaryOperation) expr).getLeft(), factorsEnumerator, factorsDenominator);
            orderQuotient(((BinaryOperation) expr).getRight(), factorsDenominator, factorsEnumerator);
        }

    }

}
