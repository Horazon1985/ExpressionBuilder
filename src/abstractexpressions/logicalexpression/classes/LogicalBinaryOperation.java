package abstractexpressions.logicalexpression.classes;

import exceptions.EvaluationException;
import java.util.HashSet;
import abstractexpressions.logicalexpression.utilities.LogicalExpressionCollection;
import abstractexpressions.logicalexpression.utilities.LogicalSimplifyUtilities;
import abstractexpressions.logicalexpression.utilities.LogicalTrivialSimplifyMethods;
import abstractexpressions.logicalexpression.utilities.SimplifyLogicalBinaryOperationMethods;
import lang.translator.Translator;

public class LogicalBinaryOperation extends LogicalExpression {

    private final LogicalExpression left, right;
    private final TypeLogicalBinary type;

    public LogicalBinaryOperation(LogicalExpression left, LogicalExpression right, TypeLogicalBinary type) {
        this.left = left;
        this.right = right;
        this.type = type;
    }

    public TypeLogicalBinary getType() {
        return this.type;
    }

    public LogicalExpression getLeft() {
        return this.left;
    }

    public LogicalExpression getRight() {
        return this.right;
    }

    @Override
    public LogicalExpression copy() {
        return new LogicalBinaryOperation(this.left, this.right, this.type);
    }

    @Override
    public boolean evaluate() {

        if (this.type.equals(TypeLogicalBinary.AND)) {
            return this.left.evaluate() && this.right.evaluate();
        }
        if (this.type.equals(TypeLogicalBinary.OR)) {
            return this.left.evaluate() || this.right.evaluate();
        }
        if (this.type.equals(TypeLogicalBinary.IMPLICATION)) {
            return !this.left.evaluate() || this.right.evaluate();
        } else {
            // Hier ist type == TypeLogicalBinary.EQUIVALENCE
            return (this.left.evaluate() && this.right.evaluate())
                    || (!this.left.evaluate() && !this.right.evaluate());
        }

    }

    @Override
    public void addContainedVars(HashSet vars) {
        this.left.addContainedVars(vars);
        this.right.addContainedVars(vars);
    }

    @Override
    public boolean contains(String var) {
        return this.left.contains(var) || this.right.contains(var);
    }

    @Override
    public boolean isConstant() {
        return this.left.isConstant() && this.right.isConstant();
    }

    @Override
    public boolean equals(LogicalExpression logExpr) {
        return logExpr instanceof LogicalBinaryOperation
                && this.getType().equals(((LogicalBinaryOperation) logExpr).getType())
                && this.getLeft().equals(((LogicalBinaryOperation) logExpr).getLeft())
                && this.getRight().equals(((LogicalBinaryOperation) logExpr).getRight());
    }

    @Override
    public boolean equivalent(LogicalExpression logExpr) {

        if (logExpr instanceof LogicalBinaryOperation && this.getType().equals(((LogicalBinaryOperation) logExpr).getType())) {
            if (this.type.equals(TypeLogicalBinary.OR)) {

                LogicalExpressionCollection summandsThis = LogicalSimplifyUtilities.getLogicalSummands(this);
                LogicalExpressionCollection summandsLogExpr = LogicalSimplifyUtilities.getLogicalSummands(logExpr);
                return (summandsThis.getBound() == summandsLogExpr.getBound())
                        && LogicalSimplifyUtilities.difference(summandsThis, summandsLogExpr).isEmpty();

            }
            if (this.type.equals(TypeLogicalBinary.AND)) {

                LogicalExpressionCollection factorsThis = LogicalSimplifyUtilities.getLogicalFactors(this);
                LogicalExpressionCollection factorsLogExpr = LogicalSimplifyUtilities.getLogicalFactors(logExpr);
                return (factorsThis.getBound() == factorsLogExpr.getBound())
                        && LogicalSimplifyUtilities.difference(factorsThis, factorsLogExpr).isEmpty();

            }
            if (this.type.equals(TypeLogicalBinary.EQUIVALENCE)) {

                LogicalExpressionCollection equivTermsThis = LogicalSimplifyUtilities.getLogicalEquivalentTerms(this);
                LogicalExpressionCollection equivTermsLogExpr = LogicalSimplifyUtilities.getLogicalEquivalentTerms(logExpr);
                return (equivTermsThis.getBound() == equivTermsLogExpr.getBound())
                        && LogicalSimplifyUtilities.difference(equivTermsThis, equivTermsLogExpr).isEmpty();

            }
            return this.getLeft().equivalent(((LogicalBinaryOperation) logExpr).getLeft())
                    && this.getRight().equivalent(((LogicalBinaryOperation) logExpr).getRight());
        }
        return false;

    }

    @Override
    public String writeLogicalExpression() {

        if (this.type.equals(TypeLogicalBinary.EQUIVALENCE)) {
            return this.left.writeLogicalExpression() + "=" + this.right.writeLogicalExpression();
        } else if (this.type.equals(TypeLogicalBinary.IMPLICATION)) {

            if (this.right.isEquiv()) {
                return this.left.writeLogicalExpression() + ">" + this.right.writeLogicalExpression();
            }
            return this.left.writeLogicalExpression() + ">" + this.right.writeLogicalExpression();

        } else if (this.type.equals(TypeLogicalBinary.OR)) {

            String leftAsText, rightAsText;

            if (this.left.isEquiv() || this.left.isImpl()) {
                leftAsText = "(" + this.left.writeLogicalExpression() + ")";
            } else {
                leftAsText = this.left.writeLogicalExpression();
            }

            if (this.right.isEquiv() || this.right.isImpl()) {
                rightAsText = "(" + this.right.writeLogicalExpression() + ")";
            } else {
                rightAsText = this.right.writeLogicalExpression();
            }

            return leftAsText + "|" + rightAsText;

        } else {

            String leftAsText, rightAsText;

            if (this.left instanceof LogicalBinaryOperation && this.left.isNotAnd()) {
                leftAsText = "(" + this.left.writeLogicalExpression() + ")";
            } else {
                leftAsText = this.left.writeLogicalExpression();
            }

            if (this.right instanceof LogicalBinaryOperation && this.right.isNotAnd()) {
                rightAsText = "(" + this.right.writeLogicalExpression() + ")";
            } else {
                rightAsText = this.right.writeLogicalExpression();
            }

            return leftAsText + "&" + rightAsText;

        }

    }

    @Override
    public LogicalExpression simplifyTrivial() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateOutputMessage("LEB_LogicalBinaryOperation_COMPUTATION_ABORTED"));
        }

        //Konstante Ausdrücke direkt auswerten.
        if (this.isConstant()) {
            return new LogicalConstant(this.evaluate());
        }

        LogicalExpression logExprSimplified;

        // Umformungen, die Summanden involvieren.
        if (this.getType().equals(TypeLogicalBinary.OR)) {

            LogicalExpressionCollection summands = LogicalSimplifyUtilities.getLogicalSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyTrivial());
            }

            LogicalTrivialSimplifyMethods.findTrueInSum(summands);
            LogicalTrivialSimplifyMethods.cancelFalseInSum(summands);
            LogicalTrivialSimplifyMethods.cancelMultipleSummands(summands);
            LogicalTrivialSimplifyMethods.collectExpressionsAndNegatedExpressionsInSum(summands);

            logExprSimplified = LogicalSimplifyUtilities.produceSum(summands);
            if (!this.equals(logExprSimplified)) {
                return logExprSimplified;
            }

        }

        // Umformungen, die Faktoren involvieren.
        if (this.getType().equals(TypeLogicalBinary.AND)) {

            LogicalExpressionCollection factors = LogicalSimplifyUtilities.getLogicalFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyTrivial());
            }

            LogicalTrivialSimplifyMethods.findFalseInProduct(factors);
            LogicalTrivialSimplifyMethods.cancelTrueInProduct(factors);
            LogicalTrivialSimplifyMethods.cancelMultipleFactors(factors);
            LogicalTrivialSimplifyMethods.collectExpressionsAndNegatedExpressionsInProduct(factors);

            logExprSimplified = LogicalSimplifyUtilities.produceProduct(factors);
            if (!this.equals(logExprSimplified)) {
                return logExprSimplified;
            }

        }

        // Umformungen, die Äquivalenzketten involvieren.
        if (this.getType().equals(TypeLogicalBinary.EQUIVALENCE)) {

            LogicalExpressionCollection equivTerms = LogicalSimplifyUtilities.getLogicalEquivalentTerms(this);
            for (int i = 0; i < equivTerms.getBound(); i++) {
                equivTerms.put(i, equivTerms.get(i).simplifyTrivial());
            }

            LogicalTrivialSimplifyMethods.cancelDoubleTermsInEquivalenceChain(equivTerms);

            logExprSimplified = LogicalSimplifyUtilities.produceEquivalenceChain(equivTerms);
            if (!this.equals(logExprSimplified)) {
                return logExprSimplified;
            }

        }

        //Linken und rechten Teil bei logischen Binäroperationen zunächst separat vereinfachen
        LogicalBinaryOperation logExpr = new LogicalBinaryOperation(this.getLeft().simplifyTrivial(), this.getRight().simplifyTrivial(), this.getType());

        // Triviale Umformungen mit 0 und 1.
        logExprSimplified = SimplifyLogicalBinaryOperationMethods.trivialOperationsWithFalseTrue(logExpr);
        if (!logExprSimplified.equals(logExpr)) {
            return logExprSimplified;
        }

        return logExpr;

    }

    @Override
    public LogicalExpression factorizeInSums() throws EvaluationException {

        if (this.getType().equals(TypeLogicalBinary.OR)) {

            LogicalExpressionCollection summands = LogicalSimplifyUtilities.getLogicalSummands(this);
            // In jedem Summanden einzeln faktorisieren
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).factorizeInSums());
            }

            LogicalExpressionCollection commonFactors;
            LogicalExpression leftSummandRest, rightSummandRest, factorizedSummand;
            LogicalExpressionCollection leftRestFactors = new LogicalExpressionCollection();
            LogicalExpressionCollection rightRestFactors = new LogicalExpressionCollection();

            for (int i = 0; i < summands.getBound(); i++) {

                if (summands.get(i) == null) {
                    continue;
                }

                for (int j = i + 1; j < summands.getBound(); j++) {

                    if (summands.get(j) == null) {
                        continue;
                    }

                    leftRestFactors.clear();
                    rightRestFactors.clear();
                    leftRestFactors = LogicalSimplifyUtilities.getLogicalFactors(summands.get(i));
                    rightRestFactors = LogicalSimplifyUtilities.getLogicalFactors(summands.get(j));
                    commonFactors = LogicalSimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                    leftSummandRest = LogicalSimplifyUtilities.produceProduct(LogicalSimplifyUtilities.difference(leftRestFactors, commonFactors));
                    rightSummandRest = LogicalSimplifyUtilities.produceProduct(LogicalSimplifyUtilities.difference(rightRestFactors, commonFactors));

                    if (!commonFactors.isEmpty()) {

                        factorizedSummand = LogicalSimplifyUtilities.produceProduct(commonFactors).and(leftSummandRest.or(rightSummandRest));
                        summands.put(i, factorizedSummand);
                        summands.remove(j);
                        // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                        break;

                    }

                    // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                    if (Thread.interrupted()) {
                        throw new EvaluationException(Translator.translateOutputMessage("LEB_LogicalBinaryOperation_COMPUTATION_ABORTED"));
                    }

                }

            }

            // Ergebnis bilden.
            return LogicalSimplifyUtilities.produceSum(summands);

        } else {

            /*
             Hier ist type == TypeLogicalBinary.AND oder type ==
             TypeLogicalBinary.IMPLICATION oder ==
             TypeLogicalBinary.EQUIVALENCE
             */
            if (this.getType().equals(TypeLogicalBinary.AND)) {

                LogicalExpressionCollection factors = LogicalSimplifyUtilities.getLogicalFactors(this);
                // In jedem Ausdruck einzeln faktorisieren.
                for (int i = 0; i < factors.getBound(); i++) {
                    factors.put(i, factors.get(i).factorizeInSums());
                }
                return LogicalSimplifyUtilities.produceProduct(factors);

            } else if (this.getType().equals(TypeLogicalBinary.EQUIVALENCE)) {

                LogicalExpressionCollection equivTerms = LogicalSimplifyUtilities.getLogicalEquivalentTerms(this);
                // In jedem Ausdruck einzeln faktorisieren.
                for (int i = 0; i < equivTerms.getBound(); i++) {
                    equivTerms.put(i, equivTerms.get(i).factorizeInSums());
                }
                return LogicalSimplifyUtilities.produceEquivalenceChain(equivTerms);

            }

            return new LogicalBinaryOperation(this.left.factorizeInSums(), this.right.factorizeInSums(), this.type);

        }

    }

    @Override
    public LogicalExpression factorizeInProducts() throws EvaluationException {

        if (this.getType().equals(TypeLogicalBinary.AND)) {

            LogicalExpressionCollection factors = LogicalSimplifyUtilities.getLogicalFactors(this);
            // In jedem Faktor einzeln faktorisieren
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).factorizeInProducts());
            }

            LogicalExpressionCollection commonSummands;
            LogicalExpression leftFactorRest, rightFactorRest, factorizedFactor;
            LogicalExpressionCollection leftRestSumands = new LogicalExpressionCollection();
            LogicalExpressionCollection rightRestSumands = new LogicalExpressionCollection();

            for (int i = 0; i < factors.getBound(); i++) {

                if (factors.get(i) == null) {
                    continue;
                }

                for (int j = i + 1; j < factors.getBound(); j++) {

                    if (factors.get(j) == null) {
                        continue;
                    }

                    leftRestSumands.clear();
                    rightRestSumands.clear();
                    leftRestSumands = LogicalSimplifyUtilities.getLogicalSummands(factors.get(i));
                    rightRestSumands = LogicalSimplifyUtilities.getLogicalSummands(factors.get(j));
                    commonSummands = LogicalSimplifyUtilities.intersection(leftRestSumands, rightRestSumands);

                    leftFactorRest = LogicalSimplifyUtilities.produceSum(LogicalSimplifyUtilities.difference(leftRestSumands, commonSummands));
                    rightFactorRest = LogicalSimplifyUtilities.produceSum(LogicalSimplifyUtilities.difference(rightRestSumands, commonSummands));

                    if (!commonSummands.isEmpty()) {

                        factorizedFactor = LogicalSimplifyUtilities.produceSum(commonSummands).or(leftFactorRest.and(rightFactorRest));
                        factors.put(i, factorizedFactor);
                        factors.remove(j);
                        // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Faktor!
                        break;

                    }

                    // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                    if (Thread.interrupted()) {
                        throw new EvaluationException(Translator.translateOutputMessage("LEB_LogicalBinaryOperation_COMPUTATION_ABORTED"));
                    }

                }

            }

            // Ergebnis bilden.
            return LogicalSimplifyUtilities.produceProduct(factors);

        } else {

            /*
             Hier ist type == TypeLogicalBinary.OR oder type ==
             TypeLogicalBinary.IMPLICATION oder ==
             TypeLogicalBinary.EQUIVALENCE
             */
            if (this.getType().equals(TypeLogicalBinary.OR)) {

                LogicalExpressionCollection summands = LogicalSimplifyUtilities.getLogicalSummands(this);
                // In jedem Ausdruck einzeln faktorisieren.
                for (int i = 0; i < summands.getBound(); i++) {
                    summands.put(i, summands.get(i).factorizeInProducts());
                }
                return LogicalSimplifyUtilities.produceSum(summands);

            } else if (this.getType().equals(TypeLogicalBinary.EQUIVALENCE)) {

                LogicalExpressionCollection equivTerms = LogicalSimplifyUtilities.getLogicalEquivalentTerms(this);
                // In jedem Ausdruck einzeln faktorisieren.
                for (int i = 0; i < equivTerms.getBound(); i++) {
                    equivTerms.put(i, equivTerms.get(i).factorizeInProducts());
                }
                return LogicalSimplifyUtilities.produceEquivalenceChain(equivTerms);

            }

            return new LogicalBinaryOperation(this.left.factorizeInProducts(), this.right.factorizeInProducts(), this.type);

        }

    }

}
