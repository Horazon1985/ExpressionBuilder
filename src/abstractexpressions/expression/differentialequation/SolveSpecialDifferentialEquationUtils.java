package abstractexpressions.expression.differentialequation;

import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import static abstractexpressions.expression.differentialequation.SolveGeneralDifferentialEquationUtils.getFreeIntegrationConstantVariable;
import static abstractexpressions.expression.differentialequation.SolveGeneralDifferentialEquationUtils.getOrderOfDifferentialEquation;
import static abstractexpressions.expression.differentialequation.SolveGeneralDifferentialEquationUtils.simplifyTypesSolutionOfDifferentialEquation;
import abstractexpressions.expression.equation.SolveGeneralEquationUtils;
import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.basic.SimplifyUtilities;
import exceptions.DifferentialEquationNotAlgebraicallyIntegrableException;
import exceptions.EvaluationException;
import exceptions.MathToolException;
import notations.NotationLoader;

/**
 * Klasse, die Lösungsmethoden für spezielle Taypen von Differentialgleichungen
 * beinhaltet.
 */
public abstract class SolveSpecialDifferentialEquationUtils extends SolveGeneralDifferentialEquationUtils {

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

    //Typ: y^'' = g(y).
    /**
     * Liefert Lösungen von Differentialgleichungen vom Typ y'' = g(y). Die
     * eingegebene DGL hat die Form f = 0.
     */
    public static ExpressionCollection solveDifferentialEquationWithSecondDerivativeAndVarOrd(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        ExpressionCollection solutions = new ExpressionCollection();

        int ord = getOrderOfDifferentialEquation(f, varOrd);

        if (ord != 2) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        ExpressionCollection solutionsForHighestDerivative;
        try {
            solutionsForHighestDerivative = SolveGeneralEquationUtils.solveEquation(f, ZERO, varOrd + "''");
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        if (solutionsForHighestDerivative.isEmpty()) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // Zunächst: Typprüfung.
        boolean isEquationOfProperType;
        for (Expression solutionForHighestDerivative : solutionsForHighestDerivative) {
            // Zunächst: Typprüfung.
            isEquationOfProperType = doesExpressionContainOnlyDerivativesOfGivenOrder(solutionForHighestDerivative, varOrd, 2, 0)
                    && !solutionForHighestDerivative.contains(varAbsc);
            if (isEquationOfProperType) {
                solutions = SimplifyUtilities.union(solutions,
                        solveDifferentialEquationWithOnlySecondDerivative(solutionForHighestDerivative, varAbsc, varOrd));
            }
        }

        solutions.removeMultipleEquivalentTerms();
        return solutions;

    }

    //Typ: f(y'', y', y) = 0.
    /**
     * Liefert Lösungen von Differentialgleichungen vom Typ f(y'', y', y) = 0.
     */
    public static ExpressionCollection solveDifferentialEquationOfOrderTwoWithoutVarAbsc(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        int ord = getOrderOfDifferentialEquation(f, varOrd);

        if (f.contains(varAbsc) || ord != 2) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        /*
        Algorithmus: die DGL f(y'', y', y) = 0 wird mittels Substitution v = y'
        äquivalent zur DGL f(v*v', v', y) = 0. Für jede Lösung v(y) muss sodann
        die DGL y' = v(y) gelöst werden.
         */
        String varOrdSubst = NotationLoader.SUBSTITUTION_VAR;
        Expression substitutedDiffEq = f;
        substitutedDiffEq = substitutedDiffEq.replaceVariable(varOrd + "'", Variable.create(varOrdSubst));
        substitutedDiffEq = substitutedDiffEq.replaceVariable(varOrd + "''", Variable.create(varOrdSubst).mult(Variable.create(varOrdSubst + "'")));

        try {
            ExpressionCollection solutionsOfSubstitutedDiffEq = solveZeroDifferentialEquation(substitutedDiffEq, varOrd, varOrdSubst);
            ExpressionCollection solutions = new ExpressionCollection();
            // Jetzt: Für jede Lösung v die DGL y' = v(y) lösen und in die Menge der Lösungen aufnehmen.
            ExpressionCollection solutionsForVarOrd;
            for (Expression solutionOfSubstitutedDiffEq : solutionsOfSubstitutedDiffEq) {
                solutionsForVarOrd = solveDifferentialEquation(Variable.create(varOrd + "'"), solutionOfSubstitutedDiffEq, varAbsc, varOrd);
                solutions.addAll(solutionsForVarOrd);
            }
            return solutions;
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

    }

    private static ExpressionCollection solveDifferentialEquationWithOnlySecondDerivative(Expression rightSide, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        try {
            ExpressionCollection solutions = new ExpressionCollection();

            /*
             Lösungsalgorithmus: Sei y'' = f(y).
             Dann: 1. y' = +-(2*g(y) + C_1)^(1/2), g = int(f(t), t).
             2. h_(C_1)(y^(n)) = +-(x + C_2), h(t) = int(1/(2 * g(t) + C_1)^(1/2), t).
             3. y = h^(-1)_(C_1)(+-(x + C_2)).
             */
            // Schritt 1: Bilden von g.
            Expression g = new Operator(TypeOperator.integral, new Object[]{rightSide, varOrd}).simplify();
            // Schritt 2: Bilden von h_(C_1).
            Expression h = new Operator(TypeOperator.integral, new Object[]{ONE.div(TWO.mult(g).add(getFreeIntegrationConstantVariable())).pow(1, 2), varOrd}).simplify();
            try {
                solutions = SolveGeneralEquationUtils.solveEquation(h, Variable.create(varAbsc).add(getFreeIntegrationConstantVariable()), varOrd).simplify(simplifyTypesSolutionOfDifferentialEquation);
            } catch (EvaluationException e) {
                // Nichts tun, Lösung ignorieren.
            }
            try {
                solutions = SimplifyUtilities.union(solutions,
                        SolveGeneralEquationUtils.solveEquation(h, MINUS_ONE.mult(Variable.create(varAbsc).add(getFreeIntegrationConstantVariable())), varOrd).simplify(simplifyTypesSolutionOfDifferentialEquation));
            } catch (EvaluationException e) {
                // Nichts tun, Lösung ignorieren.
            }

            /* 
             Sonderfall: Wenn die Gleichung h_(C_1)(y) = +-(x + C_2)
             nicht explizit nach y aufgelöst werden kann, so werden die Ausdrücke
             h_(C_1)(y) -+(x + C_2) in die Lösungen mitaufgenommen und hinterher
             als implizite Lösungen interpretiert.<br>
             BEISPIEL: y'' = y^2 besitzt die implizite Lösung
             int(1/(2*y^3/3 + C_1)^(1/2),y) +- (x + C_2) = 0.
             */
            if (solutions.isEmpty() && solutions != SolveGeneralEquationUtils.NO_SOLUTIONS) {
                solutions.add(h.add(Variable.create(varAbsc).add(getFreeIntegrationConstantVariable())));
                solutions.add(h.sub(Variable.create(varAbsc).add(getFreeIntegrationConstantVariable())));
                return solutions;
            }

            // Schritt 3: y = int(h^(-1)_(C_1)(+-(x + C_2)), x, n) + C_3 + C_4 * x + ... + C_(n + 2) * x^(n - 1) bilden.
            for (Expression solution : solutions) {
                try {
                    solutions.add(solution.simplify(simplifyTypesSolutionOfDifferentialEquation));
                } catch (EvaluationException e) {
                    // Nichts tun, Lösung ignorieren.
                }
            }

            return solutions;
        } catch (EvaluationException ex) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
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

        solutions = SolveGeneralEquationUtils.solveEquation(implicitEquation, ZERO, varOrd);

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
     * Gibt die Koeffizienten a, b, c einer Bernoulli-Differentialgleichung f =
     * 0 zurück, wenn diese die Gestalt a(x)*y' + b(x)*y + c(x)*y^n = 0, a != 0,
     * 1 besitzt.<br>
     * VORAUSSETZUNG: f ist bereits weitestgehend vereinfacht.
     *
     * @throws NotBernoulliDifferentialEquationException
     */
    private static Expression[] getCoefficientsAndExponentForBernoulliDifferentialEquation(Expression f, String varAbsc, String varOrd) throws NotBernoulliDifferentialEquationException {

        Expression[] bernoulliData = new Expression[4];

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

                    // Im Zähler suchen.
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
                    // Im Nenner suchen.
                    if (!powerOfVarOrdFound) {
                        for (int i = 0; i < factorsDenominator.getBound(); i++) {
                            if (factorsDenominator.get(i).isPower()
                                    && ((BinaryOperation) factorsDenominator.get(i)).getLeft().equals(Variable.create(varOrd))
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varAbsc)
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varOrd)
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varOrd + "'")) {
                                powerOfVarOrdFound = true;
                                exponent = MINUS_ONE.mult(((BinaryOperation) factorsDenominator.get(i)).getRight()).simplify();
                                factorsDenominator.remove(i);
                            }
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
                    if (b.contains(varOrd)) {
                        throw new NotBernoulliDifferentialEquationException();
                    }
                    ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(summands.get(0));
                    ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(0));

                    boolean powerOfVarOrdFound = false;

                    // Im Zähler suchen.
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
                    // Im Nenner suchen.
                    if (!powerOfVarOrdFound) {
                        for (int i = 0; i < factorsDenominator.getBound(); i++) {
                            if (factorsDenominator.get(i).isPower()
                                    && ((BinaryOperation) factorsDenominator.get(i)).getLeft().equals(Variable.create(varOrd))
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varAbsc)
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varOrd)
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varOrd + "'")) {
                                powerOfVarOrdFound = true;
                                exponent = MINUS_ONE.mult(((BinaryOperation) factorsDenominator.get(i)).getRight()).simplify();
                                factorsDenominator.remove(i);
                            }
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

                } catch (EvaluationException e) {
                    throw new NotBernoulliDifferentialEquationException();
                }

            } else {

                ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(summands.get(1));
                ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(1));

                boolean powerOfVarOrdFound = false;

                try {
                    // Im Zähler suchen.
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
                    // Im Nenner suchen.
                    if (!powerOfVarOrdFound) {
                        for (int i = 0; i < factorsDenominator.getBound(); i++) {
                            if (factorsDenominator.get(i).isPower()
                                    && ((BinaryOperation) factorsDenominator.get(i)).getLeft().equals(Variable.create(varOrd))
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varAbsc)
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varOrd)
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varOrd + "'")) {
                                powerOfVarOrdFound = true;
                                exponent = MINUS_ONE.mult(((BinaryOperation) factorsDenominator.get(i)).getRight()).simplify();
                                factorsDenominator.remove(i);
                            }
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
                } catch (EvaluationException e) {
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
                    b = MINUS_ONE.mult(right.div(Variable.create(varOrd))).simplify();
                } catch (EvaluationException e) {
                    throw new NotBernoulliDifferentialEquationException();
                }
                if (b.contains(varOrd)) {
                    throw new NotBernoulliDifferentialEquationException();
                }

                ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(left);
                ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(left);

                boolean powerOfVarOrdFound = false;

                try {
                    // Im Zähler suchen.
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
                    // Im Nenner suchen.
                    if (!powerOfVarOrdFound) {
                        for (int i = 0; i < factorsDenominator.getBound(); i++) {
                            if (factorsDenominator.get(i).isPower()
                                    && ((BinaryOperation) factorsDenominator.get(i)).getLeft().equals(Variable.create(varOrd))
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varAbsc)
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varOrd)
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varOrd + "'")) {
                                powerOfVarOrdFound = true;
                                exponent = MINUS_ONE.mult(((BinaryOperation) factorsDenominator.get(i)).getRight()).simplify();
                                factorsDenominator.remove(i);
                            }
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
                } catch (EvaluationException e) {
                    throw new NotBernoulliDifferentialEquationException();
                }

            } else {

                ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(right);
                ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(right);

                boolean powerOfVarOrdFound = false;

                try {
                    // Im Zähler suchen.
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
                    // Im Nenner suchen.
                    if (!powerOfVarOrdFound) {
                        for (int i = 0; i < factorsDenominator.getBound(); i++) {
                            if (factorsDenominator.get(i).isPower()
                                    && ((BinaryOperation) factorsDenominator.get(i)).getLeft().equals(Variable.create(varOrd))
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varAbsc)
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varOrd)
                                    && !((BinaryOperation) factorsDenominator.get(i)).getRight().contains(varOrd + "'")) {
                                powerOfVarOrdFound = true;
                                exponent = MINUS_ONE.mult(((BinaryOperation) factorsDenominator.get(i)).getRight()).simplify();
                                factorsDenominator.remove(i);
                            }
                        }
                    }

                    if (powerOfVarOrdFound) {
                        c = MINUS_ONE.mult(SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator)).simplify();
                        if (c.contains(varOrd)) {
                            throw new NotBernoulliDifferentialEquationException();
                        }
                    } else {
                        throw new NotBernoulliDifferentialEquationException();
                    }
                } catch (EvaluationException e) {
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
                    c.mult(Variable.create(varOrd).pow(exponent)))).simplify();
            if (!difference.equals(ZERO)) {
                throw new NotBernoulliDifferentialEquationException();
            }
        } catch (EvaluationException e) {
            throw new NotBernoulliDifferentialEquationException();
        }

        bernoulliData[0] = a;
        bernoulliData[1] = b;
        bernoulliData[2] = c;
        bernoulliData[3] = exponent;
        return bernoulliData;

    }

    /**
     * Gibt Lösungen für die Differentialgleichung f = 0 zurück, wenn diese eine
     * Bernoullische DGL ist. Ansonsten wird eine
     * DifferentialEquationNotAlgebraicallyIntegrableException geworfen.
     *
     * @throws DifferentialEquationNotAlgebraicallyIntegrableException
     */
    public static ExpressionCollection solveBernoulliDifferentialEquation(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException, EvaluationException {

        ExpressionCollection solutions = new ExpressionCollection();

        Expression[] bernoulliData;
        try {
            bernoulliData = getCoefficientsAndExponentForBernoulliDifferentialEquation(f, varAbsc, varOrd);
        } catch (NotBernoulliDifferentialEquationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Expression[] coefficients = new Expression[]{bernoulliData[1].mult(bernoulliData[3].sub(1)).div(bernoulliData[0]).simplify(),
            bernoulliData[2].mult(bernoulliData[3].sub(1)).div(bernoulliData[0]).simplify()};
        String varOrdInSubstitutedDiffEq = notations.NotationLoader.SUBSTITUTION_VAR;
        /* 
         Ursprüngliche Differentialgleichung: a(x)*y' + b(x)*y + c(x)*y^n = 0.
         Substitutierte Differentialgleichung: z' + (n - 1)*(b(x)/a(x))*z + (n - 1)*(c(x)/a(x)) = 0, z = y^(1 - n).
         */
        Expression substitutedDifferentialEquation = Variable.create(varOrdInSubstitutedDiffEq + "'").sub(coefficients[0].mult(Variable.create(varOrdInSubstitutedDiffEq))).sub(coefficients[1]).simplify(simplifyTypesDifferentialEquation);
        ExpressionCollection solutionsOfSubstitutedDiffEq = SolveGeneralDifferentialEquationUtils.solveDifferentialEquationLinearOfOrderOne(substitutedDifferentialEquation, varAbsc, varOrdInSubstitutedDiffEq);

        for (Expression solution : solutionsOfSubstitutedDiffEq) {
            if (!solution.contains(varOrdInSubstitutedDiffEq)) {
                try {
                    solutions.add(solution.pow(ONE.div(ONE.sub(bernoulliData[3]))).simplify());
                } catch (EvaluationException e) {
                    // Nichts tun.
                }
            }
        }
        // TO DO.

        return solutions;

    }

}
