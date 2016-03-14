package abstractexpressions.expression.diferentialequation;

import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.equation.SolveGeneralEquationMethods;
import abstractexpressions.expression.utilities.ExpressionCollection;
import exceptions.DifferentialEquationNotAlgebraicallyIntegrableException;
import exceptions.EvaluationException;

/**
 * Klasse, die Lösungsmethoden für spezielle Taypen von Differentialgleichungen
 * beinhaltet.
 */
public abstract class SolveSpecialDifferentialEquationMethods extends SolveGeneralDifferentialEquationMethods {

    /**
     * Gibt zurück, ob die Differentialgleichung f = 0 die Gestalt a(x, y) +
     * b(x, y)*y' = 0 besitzt.
     */
    private static boolean isDifferentialEquationExact(Expression f, String varAbsc, String varOrd) {

        // Prüfung auf die formale Gestalt.
        Expression a = f;
        a = a.replaceVariable(varOrd + "'", ZERO);
        try {
            a = a.simplify();
        } catch (EvaluationException ex) {
            return false;
        }

        Expression b = (f.sub(a)).div(Variable.create(varOrd + "'"));
        try {
            b = b.simplify();
        } catch (EvaluationException ex) {
            return false;
        }

        if (b.contains(varOrd + "'")) {
            return false;
        }

        try {
            Expression difference = a.diff(varOrd).sub(b.diff(varAbsc)).simplify();
            return difference.equals(ZERO);
        } catch (EvaluationException ex) {
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
    private static Expression[] getCoefficientsForExactDifferentialEquation(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        if (!isDifferentialEquationExact(f, varAbsc, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Expression a = f;
        a = a.replaceVariable(varOrd + "'", ZERO);
        try {
            a = a.simplify();
        } catch (EvaluationException ex) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Expression b = (f.sub(a)).div(Variable.create(varOrd + "'"));
        try {
            b = b.simplify();
        } catch (EvaluationException ex) {
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

        ExpressionCollection solutions = new ExpressionCollection();

        if (!isDifferentialEquationExact(f, varAbsc, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Expression[] coefficients = getCoefficientsForExactDifferentialEquation(f, varAbsc, varOrd);

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

}
