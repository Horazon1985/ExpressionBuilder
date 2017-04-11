package abstractexpressions.logicalexpression.basic;

import abstractexpressions.logicalexpression.classes.LogicalBinaryOperation;
import abstractexpressions.logicalexpression.classes.LogicalExpression;

public abstract class LogicalSimplifyUtilities {

    /**
     * Gibt den Durchschnitt von logTermsLeft und logTermsRight zurück (mit
     * Vielfachheiten gezählt).
     */
    public static LogicalExpressionCollection intersection(LogicalExpressionCollection logTermsLeft,
            LogicalExpressionCollection logTermsRight) {

        LogicalExpressionCollection logTermsLeftCopy = logTermsLeft.copy();
        LogicalExpressionCollection logTermsRightCopy = logTermsRight.copy();
        LogicalExpressionCollection result = new LogicalExpressionCollection();

        for (int i = 0; i < logTermsLeft.getBound(); i++) {
            for (int j = 0; j < logTermsRight.getBound(); j++) {
                if (logTermsLeftCopy.get(i) != null && logTermsRightCopy.get(j) != null && logTermsLeftCopy.get(i).equivalent(logTermsRightCopy.get(j))) {
                    result.add(logTermsLeftCopy.get(i));
                    logTermsLeftCopy.remove(i);
                    logTermsRightCopy.remove(j);
                }
            }
        }

        return result;

    }

    /**
     * Gibt die Differenz logTermsLeft \ logTermsRight zurück (mit
     * Vielfachheiten gezählt).
     */
    public static LogicalExpressionCollection difference(LogicalExpressionCollection logTermsLeft,
            LogicalExpressionCollection logTermsRight) {

        LogicalExpressionCollection result = new LogicalExpressionCollection();
        /*
         logTermsLeft und logTermsRight werden in manchen Prozeduren noch
         nachträglich gebraucht und sollten nicht verändert werden ->
         logTermsLeft und logTermsRight kopieren.
         */
        LogicalExpressionCollection logTermsLeftCopy = logTermsLeft.copy();
        LogicalExpressionCollection logTermsRightCopy = logTermsRight.copy();

        LogicalExpression termLeft, termRight;
        boolean equivalentTermFound;
        for (int i = 0; i < logTermsLeft.getBound(); i++) {
            termLeft = logTermsLeftCopy.get(i);
            equivalentTermFound = false;
            for (int j = 0; j < logTermsRight.getBound(); j++) {
                if (logTermsRightCopy.get(j) != null) {

                    termRight = logTermsRightCopy.get(j);
                    if (termLeft.equivalent(termRight)) {
                        equivalentTermFound = true;
                        logTermsLeftCopy.remove(i);
                        logTermsRightCopy.remove(j);
                        break;
                    }

                }
            }
            if (!equivalentTermFound) {
                result.add(termLeft);
            }
        }

        return result;

    }

    /**
     * Fügt der LogicalExpressionCollection summands alle Summanden von logExpr
     * hinzu, falls man logExpr als Summe auffasst. Die Keys sind 0, 1, 2, ...,
     * size - 1.
     */
    private static void addLogicalSummands(LogicalExpression logExpr, LogicalExpressionCollection summands) {
        if (logExpr.isOr()) {
            addLogicalSummands(((LogicalBinaryOperation) logExpr).getLeft(), summands);
            addLogicalSummands(((LogicalBinaryOperation) logExpr).getRight(), summands);
        } else if (!logExpr.equals(LogicalExpression.FALSE) || summands.isEmpty()) {
            summands.add(logExpr);
        }
    }

    /**
     * Gibt eine LogicalExpressionCollection mit allen Summanden von logExpr
     * hinzu, falls man logExpr als Summe auffasst. Die Keys sind 0, 1, 2, ...,
     * size - 1.
     */
    public static LogicalExpressionCollection getLogicalSummands(LogicalExpression logExpr) {
        LogicalExpressionCollection summands = new LogicalExpressionCollection();
        addLogicalSummands(logExpr, summands);
        return summands;
    }

    /**
     * Fügt der LogicalExpressionCollection summands alle factors von logExpr
     * hinzu, falls man logExpr als Produkt auffasst. Die Keys sind 0, 1, 2,
     * ..., size - 1.
     */
    private static void addLogicalFactors(LogicalExpression logExpr, LogicalExpressionCollection factors) {
        if (logExpr.isAnd()) {
            addLogicalFactors(((LogicalBinaryOperation) logExpr).getLeft(), factors);
            addLogicalFactors(((LogicalBinaryOperation) logExpr).getRight(), factors);
        } else if (!logExpr.equals(LogicalExpression.TRUE) || factors.isEmpty()) {
            factors.add(logExpr);
        }
    }

    /**
     * Gibt eine LogicalExpressionCollection mit allen Faktoren von logExpr
     * hinzu, falls man logExpr als Produkt auffasst. Die Keys sind 0, 1, 2,
     * ..., size - 1.
     */
    public static LogicalExpressionCollection getLogicalFactors(LogicalExpression logExpr) {
        LogicalExpressionCollection factors = new LogicalExpressionCollection();
        addLogicalFactors(logExpr, factors);
        return factors;
    }

    /**
     * Fügt der LogicalExpressionCollection equivTerms alle Terme von logExpr
     * hinzu, falls man logExpr als eine Ketten von Äquivalenzen auffasst. Die
     * Keys sind 0, 1, 2, ..., size - 1.
     */
    private static void addLogicalEquivalentTerms(LogicalExpression logExpr, LogicalExpressionCollection equivTerms) {
        if (logExpr.isEquiv()) {
            addLogicalEquivalentTerms(((LogicalBinaryOperation) logExpr).getLeft(), equivTerms);
            addLogicalEquivalentTerms(((LogicalBinaryOperation) logExpr).getRight(), equivTerms);
        } else {
            equivTerms.add(logExpr);
        }
    }

    /**
     * Gibt eine LogicalExpressionCollection mit allen Termen von logExpr hinzu,
     * falls man logExpr als eine Kette von Äquivalenzen auffasst. Die Keys sind
     * 0, 1, 2, ..., size - 1.
     */
    public static LogicalExpressionCollection getLogicalEquivalentTerms(LogicalExpression logExpr) {
        LogicalExpressionCollection equivTerms = new LogicalExpressionCollection();
        addLogicalEquivalentTerms(logExpr, equivTerms);
        return equivTerms;
    }

    /**
     * Gibt die Summe aus allen Termen von summands zurück.
     */
    public static LogicalExpression produceSum(LogicalExpressionCollection summands) {

        if (summands.isEmpty()) {
            return LogicalExpression.FALSE;
        }

        LogicalExpression result = LogicalExpression.FALSE;
        for (int i = summands.getBound() - 1; i >= 0; i--) {
            if (summands.get(i) != null && !summands.get(i).equals(LogicalExpression.FALSE)) {
                result = summands.get(i).or(result);
            }
        }

        return result;

    }

    /**
     * Gibt das Produkt aus allen Termen von factors zurück.
     */
    public static LogicalExpression produceProduct(LogicalExpressionCollection factors) {

        if (factors.isEmpty()) {
            return LogicalExpression.TRUE;
        }

        LogicalExpression result = LogicalExpression.TRUE;
        for (int i = factors.getBound() - 1; i >= 0; i--) {
            if (factors.get(i) != null && !factors.get(i).equals(LogicalExpression.TRUE)) {
                result = factors.get(i).and(result);
            }
        }

        return result;

    }

    /**
     * Gibt die Äquivalenzkette aus allen Termen von equivTerms zurück.
     */
    public static LogicalExpression produceEquivalenceChain(LogicalExpressionCollection equivTerms) {

        if (equivTerms.isEmpty()) {
            return LogicalExpression.TRUE;
        }

        LogicalExpression result = LogicalExpression.TRUE;
        for (int i = equivTerms.getBound() - 1; i >= 0; i--) {
            if (equivTerms.get(i) != null) {
                result = equivTerms.get(i).equiv(result);
            }
        }

        return result;

    }

}
