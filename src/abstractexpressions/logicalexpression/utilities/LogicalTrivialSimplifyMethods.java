package abstractexpressions.logicalexpression.utilities;

import exceptions.EvaluationException;
import flowcontroller.FlowController;
import abstractexpressions.logicalexpression.classes.LogicalExpression;
import abstractexpressions.logicalexpression.classes.LogicalUnaryOperation;

public abstract class LogicalTrivialSimplifyMethods {

    /**
     * Kürzt in einer Summe (|) gleiche Summanden.
     */
    public static void cancelMultipleSummands(LogicalExpressionCollection summands) throws EvaluationException {

        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) == null) {
                continue;
            }
            for (int j = i + 1; j < summands.getBound(); j++) {
                if (summands.get(j) == null) {
                    continue;
                }
                if (summands.get(j).equivalent(summands.get(i))) {
                    summands.remove(j);
                }
            }
            // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
            FlowController.interruptComputationIfNeeded();
        }

    }

    /**
     * Eliminiert in einer Summe (|) FALSE.
     */
    public static void cancelFalseInSum(LogicalExpressionCollection summands) {

        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) == null) {
                continue;
            }
            if (summands.get(i).equals(LogicalExpression.FALSE)) {
                summands.remove(i);
            }
        }

    }

    /**
     * Falls in einer Summe (|) TRUE vorkommt -> alle Summanden löschen und nur
     * in den ersten Summanden TRUE eintragen.
     */
    public static void findTrueInSum(LogicalExpressionCollection summands) {
        for (LogicalExpression summand : summands) {
            if (summand.equals(LogicalExpression.TRUE)) {
                summands.clear();
                summands.add(LogicalExpression.TRUE);
                return;
            }
        }
    }

    /**
     * Kürzt in einem Produkt (&) gleiche Faktoren.
     */
    public static void cancelMultipleFactors(LogicalExpressionCollection factors) throws EvaluationException {

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) == null) {
                continue;
            }
            for (int j = i + 1; j < factors.getBound(); j++) {
                if (factors.get(j) == null) {
                    continue;
                }
                if (factors.get(j).equivalent(factors.get(i))) {
                    factors.remove(j);
                }
            }
            // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
            FlowController.interruptComputationIfNeeded();
        }

    }

    /**
     * Eliminiert in einem Produkt (&) TRUE.
     */
    public static void cancelTrueInProduct(LogicalExpressionCollection factors) {

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).equals(LogicalExpression.TRUE)) {
                factors.remove(i);
            }
        }

    }

    /**
     * Falls in einem Produkt (&) FALSE vorkommt -> alle Faktoren löschen und
     * nur in den ersten Faktor FALSE eintragen.
     */
    public static void findFalseInProduct(LogicalExpressionCollection factors) {
        for (LogicalExpression factor : factors) {
            if (factor.equals(LogicalExpression.FALSE)) {
                factors.clear();
                factors.add(LogicalExpression.FALSE);
                return;
            }
        }
    }

    /**
     * Kürzt in einer Äquivalenzkette (=) jeweils zwei äquivalente Terme zu
     * TRUE.
     */
    public static void cancelDoubleTermsInEquivalenceChain(LogicalExpressionCollection equivTerms) throws EvaluationException {

        for (int i = 0; i < equivTerms.getBound(); i++) {
            if (equivTerms.get(i) == null) {
                continue;
            }
            for (int j = i + 1; j < equivTerms.getBound(); j++) {
                if (equivTerms.get(j) == null) {
                    continue;
                }
                if (equivTerms.get(j).equivalent(equivTerms.get(i))) {
                    equivTerms.put(i, LogicalExpression.TRUE);
                    equivTerms.remove(j);
                    break;
                }
            }
            // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
            FlowController.interruptComputationIfNeeded();
        }

    }

    /**
     * Falls in einer Summe a und !a vorkommen -> summands löschen und nur in
     * den ersten Summanden TRUE reinschreiben.
     */
    public static void collectExpressionsAndNegatedExpressionsInSum(LogicalExpressionCollection summands) throws EvaluationException {

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }

            for (int j = 0; j < summands.getBound(); j++) {
                if (summands.get(j) == null) {
                    continue;
                }
                if (summands.get(j).isNotNeg()) {
                    continue;
                }
                if (summands.get(i).equivalent(((LogicalUnaryOperation) summands.get(j)).getLeft()) && i != j) {
                    summands.clear();
                    summands.put(0, LogicalExpression.TRUE);
                    return;
                }
            }
            
            // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
            FlowController.interruptComputationIfNeeded();

        }

    }

    /**
     * Falls in einem Produkt a und !a vorkommen -> factors löschen und nur in
     * den ersten Faktor FALSE reinschreiben.
     */
    public static void collectExpressionsAndNegatedExpressionsInProduct(LogicalExpressionCollection factors) throws EvaluationException {

        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }

            for (int j = 0; j < factors.getBound(); j++) {
                if (factors.get(j) == null) {
                    continue;
                }
                if (factors.get(j).isNotNeg()) {
                    continue;
                }
                if (factors.get(i).equivalent(((LogicalUnaryOperation) factors.get(j)).getLeft()) && i != j) {
                    factors.clear();
                    factors.put(0, LogicalExpression.FALSE);
                    return;
                }
            }

            // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
            FlowController.interruptComputationIfNeeded();
            
        }

    }

}
