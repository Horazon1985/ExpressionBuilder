package abstractexpressions.expression.diferentialequation;

import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.equation.SolveGeneralEquationMethods;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyUtilities;
import exceptions.DifferentialEquationNotAlgebraicallyIntegrableException;
import exceptions.EvaluationException;
import exceptions.MathToolException;

/**
 * Klasse, die Lösungsmethoden für spezielle Taypen von Differentialgleichungen
 * beinhaltet.
 */
public abstract class SolveSpecialDifferentialEquationMethods extends SolveGeneralDifferentialEquationMethods {

    /**
     * Private Fehlerklasse für den Fall, dass ein Ausdruck kein rationales
     * Vielfaches von Pi ist.
     */
    private static class NotBernoulliDifferentialEquationException extends MathToolException {

        private static final String NOT_BERNOULLI_DIFFERENTIAL_EQUATION_MESSAGE = "Differential equation is not a Bernoulli differential equation.";

        public NotBernoulliDifferentialEquationException() {
            super(NOT_BERNOULLI_DIFFERENTIAL_EQUATION_MESSAGE);
        }

    }

    /**
     * Gibt zurück, ob die Differentialgleichung f = 0 die Gestalt a(x, y) +
     * b(x, y)*y' = 0 besitzt.
     */
    private static boolean isDifferentialEquationLinearInFirstDerivative(Expression f, String varOrd) {

        // Prüfung auf die formale Gestalt.
        Expression a = f;
        a = a.replaceVariable(varOrd + "'", ZERO);
        try {
            a = a.simplify();
        } catch (EvaluationException e) {
            return false;
        }

        Expression b = (f.sub(a)).div(Variable.create(varOrd + "'"));
        try {
            b = b.simplify();
        } catch (EvaluationException e) {
            return false;
        }

        if (b.contains(varOrd + "'")) {
            return false;
        }

        // Prüfung, ob f - (a + b*y') = 0 ist.
        Expression difference;
        try {
            difference = f.sub(a.add(b.mult(Variable.create(varOrd + "'")))).simplify();
            if (!difference.equals(ZERO)) {
                return false;
            }
        } catch (EvaluationException e) {
            return false;
        }

        return true;

    }

    /**
     * Gibt zurück, ob die Differentialgleichung f = 0 die Gestalt a(x, y) +
     * b(x, y)*y' = 0 besitzt und exakt ist.
     */
    private static boolean isDifferentialEquationExact(Expression f, String varAbsc, String varOrd) {

        // Prüfung auf die formale Gestalt.
        Expression a = f;
        a = a.replaceVariable(varOrd + "'", ZERO);
        try {
            a = a.simplify();
        } catch (EvaluationException e) {
            return false;
        }

        Expression b = (f.sub(a)).div(Variable.create(varOrd + "'"));
        try {
            b = b.simplify();
        } catch (EvaluationException e) {
            return false;
        }

        if (b.contains(varOrd + "'")) {
            return false;
        }

        Expression difference;
        // Prüfung, ob f - (a + b*y') = 0 ist.
        try {
            difference = f.sub(a.add(b.mult(Variable.create(varOrd + "'")))).simplify();
            if (!difference.equals(ZERO)) {
                return false;
            }
        } catch (EvaluationException e) {
            return false;
        }

        // Prüfung der Integrabilitätsbedingung.
        try {
            difference = a.diff(varOrd).sub(b.diff(varAbsc)).simplify();
            return difference.equals(ZERO);
        } catch (EvaluationException e) {
            return false;
        }

    }

    /**
     * Gibt das Array {a, b} zurück, falls die Differentialgleichung f = 0 die
     * Gestalt a(x, y) + b(x, y)*y' = 0 besitzt. Ansonsten wird eine
     * DifferentialEquationNotAlgebraicallyIntegrableException geworfen.
     *
     * @throws DifferentialEquationNotAlgebraicallyIntegrableException
     */
    private static Expression[] getCoefficientsForLinearInFirstDerivativeDifferentialEquation(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        if (!isDifferentialEquationLinearInFirstDerivative(f, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Expression a = f;
        a = a.replaceVariable(varOrd + "'", ZERO);
        try {
            a = a.simplify();
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Expression b = (f.sub(a)).div(Variable.create(varOrd + "'"));
        try {
            b = b.simplify();
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        return new Expression[]{a, b};

    }

    /**
     * Gibt Lösungen für die Differentialgleichung f = 0 zurück, wenn diese
     * exakt ist. Ansonsten wird eine
     * DifferentialEquationNotAlgebraicallyIntegrableException geworfen.
     *
     * @throws DifferentialEquationNotAlgebraicallyIntegrableException
     */
    public static ExpressionCollection solveExactDifferentialEquation(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException, EvaluationException {

        ExpressionCollection solutions;

        if (!isDifferentialEquationExact(f, varAbsc, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Expression[] coefficients = getCoefficientsForLinearInFirstDerivativeDifferentialEquation(f, varAbsc, varOrd);

        Expression integrand = new Operator(TypeOperator.integral, new Object[]{coefficients[0], varAbsc}).simplify();
        Expression implicitEquation = integrand.add(new Operator(TypeOperator.integral, new Object[]{coefficients[1].sub(integrand.diff(varOrd)), varOrd})).add(
                getFreeIntegrationConstantVariable()).simplify();

        solutions = SolveGeneralEquationMethods.solveEquation(implicitEquation, ZERO, varOrd);

        if (solutions.isEmpty() && implicitEquation.contains(varOrd)) {
            // Dann nur die implizite Gleichung für die Lösungen ausgeben.
            solutions.add(implicitEquation);
        }
        return solutions;

    }

    /**
     * Gibt Lösungen für die Differentialgleichung f = 0 zurück, wenn diese
     * exakt ist. Ansonsten wird eine
     * DifferentialEquationNotAlgebraicallyIntegrableException geworfen.
     *
     * @throws DifferentialEquationNotAlgebraicallyIntegrableException
     */
    public static ExpressionCollection solveExactDifferentialEquationWithIngeratingFactor(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException, EvaluationException {

        if (!isDifferentialEquationLinearInFirstDerivative(f, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Expression[] coefficients = getCoefficientsForLinearInFirstDerivativeDifferentialEquation(f, varAbsc, varOrd);

        // Spezialfälle für den integrierenden Faktor m.
        Expression h, m;

        // 1. (a_y - b_x)/b = h(x).
        try {
            h = coefficients[0].diff(varOrd).sub(coefficients[1].diff(varAbsc)).div(coefficients[1]).simplify();
            if (!h.contains(varOrd)) {
                // Hier ist m = m(x) = int(exp(h(x)),x)
                m = new Operator(TypeOperator.integral, new Object[]{h, varAbsc}).exp().simplify();
                return solveExactDifferentialEquation(m.mult(f).simplify(simplifyTypesDifferentialEquation), varAbsc, varOrd);
            }
        } catch (EvaluationException e) {
        }

        // 2. (b_x - a_y)/a = h(y).
        try {
            h = coefficients[1].diff(varAbsc).sub(coefficients[0].diff(varOrd)).div(coefficients[0]).simplify();
            if (!h.contains(varAbsc)) {
                // Hier ist m = m(x) = int(exp(h(x)),x)
                m = new Operator(TypeOperator.integral, new Object[]{h, varOrd}).exp().simplify();
                return solveExactDifferentialEquation(m.mult(f).simplify(simplifyTypesDifferentialEquation), varAbsc, varOrd);
            }
        } catch (EvaluationException e) {
        }

        throw new DifferentialEquationNotAlgebraicallyIntegrableException();

    }

    /**
     * Gibt zurück, ob die Differentialgleichung f = 0 eine Benoulli-DGL ist,
     * d.h. also die Gestalt a(x)*y' + b(x)*y + c(x)*y^n = 0, a != 0, 1
     * besitzt.<br>
     * VORAUSSETZUNG: f ist bereits weitestgehend vereinfacht.
     */
    private static Expression[] isDifferentialEquationBernoulli(Expression f, String varAbsc, String varOrd) throws NotBernoulliDifferentialEquationException {

        Expression[] coefficients = new Expression[3];

        // Prüfung auf die formale Gestalt.
        Expression a = f;
        a = a.replaceVariable(varOrd, ZERO);
        try {
            a = a.div(Variable.create(varOrd + "'")).simplify();
        } catch (EvaluationException e) {
            throw new NotBernoulliDifferentialEquationException();
        }

        if (a.contains(varOrd) || a.contains(varOrd + "'")) {
            throw new NotBernoulliDifferentialEquationException();
        }

        Expression fCopy = f;
        try {
            fCopy = f.sub(a.mult(Variable.create(varOrd + "'"))).simplify();
        } catch (EvaluationException e) {
            throw new NotBernoulliDifferentialEquationException();
        }

        if (fCopy.contains(varOrd + "'")) {
            throw new NotBernoulliDifferentialEquationException();
        }

        Expression b;
        Expression c;
        Expression exponent = null;

        if (!fCopy.isSum() && !fCopy.isDifference()) {
            try {
                b = fCopy.div(Variable.create(varOrd)).simplify();
                if (!b.contains(varOrd) && !b.contains(varOrd + "'")) {
                    c = ZERO;
                } else {
                    ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(fCopy);
                    ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(fCopy);
                    boolean powerOfVarOrdFound = false;
                    for (int i = 0; i < factorsNumerator.getBound(); i++) {
                        if (factorsNumerator.get(i).isPower()
                                && ((BinaryOperation) factorsNumerator.get(i)).getLeft().equals(Variable.create(varOrd))
                                && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varAbsc)
                                && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varOrd)
                                && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varOrd + "'")) {
                            powerOfVarOrdFound = true;
                            exponent = ((BinaryOperation) factorsNumerator.get(i)).getRight();
                            factorsNumerator.remove(i);
                        }
                    }
                    if (powerOfVarOrdFound) {
                        c = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator);
                        b = ZERO;
                        if (c.contains(varOrd) || c.contains(varOrd + "'")) {
                            throw new NotBernoulliDifferentialEquationException();
                        }
                    } else {
                        throw new NotBernoulliDifferentialEquationException();
                    }
                }
            } catch (EvaluationException e) {
                throw new NotBernoulliDifferentialEquationException();
            }

        } else if (f.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(fCopy);
            if (summands.getBound() != 2) {
                throw new NotBernoulliDifferentialEquationException();
            }

            try {
                b = summands.get(0).div(Variable.create(varOrd)).simplify();
            } catch (EvaluationException e) {
                throw new NotBernoulliDifferentialEquationException();
            }

            if (b.contains(varOrd)) {
                try {
                    b = summands.get(1).div(Variable.create(varOrd)).simplify();
                } catch (EvaluationException e) {
                    throw new NotBernoulliDifferentialEquationException();
                }
                if (b.contains(varOrd)) {
                    throw new NotBernoulliDifferentialEquationException();
                }
                ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(summands.get(0));
                ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(0));
                boolean powerOfVarOrdFound = false;
                for (int i = 0; i < factorsNumerator.getBound(); i++) {
                    if (factorsNumerator.get(i).isPower()
                            && ((BinaryOperation) factorsNumerator.get(i)).getLeft().equals(Variable.create(varOrd))
                            && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varAbsc)
                            && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varOrd)
                            && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varOrd + "'")) {
                        powerOfVarOrdFound = true;
                        exponent = ((BinaryOperation) factorsNumerator.get(i)).getRight();
                        factorsNumerator.remove(i);
                    }
                }
                if (powerOfVarOrdFound) {
                    c = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator);
                    if (c.contains(varOrd)) {
                        throw new NotBernoulliDifferentialEquationException();
                    }
                } else {
                    throw new NotBernoulliDifferentialEquationException();
                }
            } else {
                ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(summands.get(1));
                ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(1));
                boolean powerOfVarOrdFound = false;
                for (int i = 0; i < factorsNumerator.getBound(); i++) {
                    if (factorsNumerator.get(i).isPower()
                            && ((BinaryOperation) factorsNumerator.get(i)).getLeft().equals(Variable.create(varOrd))
                            && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varAbsc)
                            && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varOrd)
                            && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varOrd + "'")) {
                        powerOfVarOrdFound = true;
                        exponent = ((BinaryOperation) factorsNumerator.get(i)).getRight();
                        factorsNumerator.remove(i);
                    }
                }
                if (powerOfVarOrdFound) {
                    c = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator);
                    if (c.contains(varOrd)) {
                        throw new NotBernoulliDifferentialEquationException();
                    }
                } else {
                    throw new NotBernoulliDifferentialEquationException();
                }
            }

        } else {

            Expression left = ((BinaryOperation) fCopy).getLeft();
            Expression right = ((BinaryOperation) fCopy).getRight();

            try {
                b = left.div(Variable.create(varOrd)).simplify();
            } catch (EvaluationException e) {
                throw new NotBernoulliDifferentialEquationException();
            }

            if (b.contains(varOrd)) {
                try {
                    b = right.div(Variable.create(varOrd)).simplify();
                } catch (EvaluationException e) {
                    throw new NotBernoulliDifferentialEquationException();
                }
                if (b.contains(varOrd)) {
                    throw new NotBernoulliDifferentialEquationException();
                }
                ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(left);
                ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(left);
                boolean powerOfVarOrdFound = false;
                for (int i = 0; i < factorsNumerator.getBound(); i++) {
                    if (factorsNumerator.get(i).isPower()
                            && ((BinaryOperation) factorsNumerator.get(i)).getLeft().equals(Variable.create(varOrd))
                            && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varAbsc)
                            && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varOrd)
                            && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varOrd + "'")) {
                        powerOfVarOrdFound = true;
                        exponent = ((BinaryOperation) factorsNumerator.get(i)).getRight();
                        factorsNumerator.remove(i);
                    }
                }
                if (powerOfVarOrdFound) {
                    c = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator);
                    if (c.contains(varOrd)) {
                        throw new NotBernoulliDifferentialEquationException();
                    }
                } else {
                    throw new NotBernoulliDifferentialEquationException();
                }
            } else {
                ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(right);
                ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(right);
                boolean powerOfVarOrdFound = false;
                for (int i = 0; i < factorsNumerator.getBound(); i++) {
                    if (factorsNumerator.get(i).isPower()
                            && ((BinaryOperation) factorsNumerator.get(i)).getLeft().equals(Variable.create(varOrd))
                            && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varAbsc)
                            && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varOrd)
                            && !((BinaryOperation) factorsNumerator.get(i)).getRight().contains(varOrd + "'")) {
                        powerOfVarOrdFound = true;
                        exponent = ((BinaryOperation) factorsNumerator.get(i)).getRight();
                        factorsNumerator.remove(i);
                    }
                }
                if (powerOfVarOrdFound) {
                    c = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator);
                    if (c.contains(varOrd)) {
                        throw new NotBernoulliDifferentialEquationException();
                    }
                } else {
                    throw new NotBernoulliDifferentialEquationException();
                }
            }

        }

        if (exponent == null) {
            // Sollte eigentlich nicht passieren!
            throw new NotBernoulliDifferentialEquationException();
        }

        // Prüfung, ob f - (a*y' + b*y + c*y^n) = 0 ist.
        Expression difference;
        try {
            difference = f.sub(a.mult(Variable.create(varOrd + "'")).add(b.mult(Variable.create(varOrd))).add(
                    c.mult(Variable.create(varOrd + "'").pow(exponent)))).simplify();
            if (!difference.equals(ZERO)) {
                throw new NotBernoulliDifferentialEquationException();
            }
        } catch (EvaluationException e) {
            throw new NotBernoulliDifferentialEquationException();
        }

        coefficients[0] = a;
        coefficients[1] = b;
        coefficients[2] = c;
        return coefficients;

    }

}
