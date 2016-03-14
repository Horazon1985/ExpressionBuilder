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
    private static boolean isDifferentialEquationLinearInFirstDerivative(Expression f, String varAbsc, String varOrd) {

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
        
        // Prüfung, ob f - (a + b*y') = 0 ist.
        Expression difference;
        try{
            difference = f.sub(a.add(b.mult(Variable.create(varOrd + "'")))).simplify();
            if (!difference.equals(ZERO)){
                return false;
            }
        } catch (EvaluationException ex) {
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
        
        Expression difference;
        // Prüfung, ob f - (a + b*y') = 0 ist.
        try{
            difference = f.sub(a.add(b.mult(Variable.create(varOrd + "'")))).simplify();
            if (!difference.equals(ZERO)){
                return false;
            }
        } catch (EvaluationException ex) {
            return false;
        }

        // Prüfung der Integrabilitätsbedingung.
        try {
            difference = a.diff(varOrd).sub(b.diff(varAbsc)).simplify();
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
    private static Expression[] getCoefficientsForLinearInFirstDerivativeDifferentialEquation(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        if (!isDifferentialEquationLinearInFirstDerivative(f, varAbsc, varOrd)) {
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

        if (!isDifferentialEquationExact(f, varAbsc, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Expression[] coefficients = getCoefficientsForLinearInFirstDerivativeDifferentialEquation(f, varAbsc, varOrd);

        // Spezialfälle für den integrierenden Faktor m.
        
        Expression h, m;
        
        // 1. (a_y - b_x)/b = h(x).
        try{
            h = coefficients[0].diff(varOrd).sub(coefficients[1].diff(varAbsc)).div(coefficients[1]).simplify();
            if (!h.contains(varOrd)){
                // Hier ist m = m(x) = int(exp(h(x)),x)
                m = new Operator(TypeOperator.integral, new Object[]{h.exp(), varAbsc}).simplify();
                return solveExactDifferentialEquation(m.mult(f).simplify(simplifyTypesDifferentialEquation), varAbsc, varOrd);
            }
        } catch (EvaluationException e){
        }
        
        // 2. (b_x - a_y)/a = h(y).
        try{
            h = coefficients[1].diff(varAbsc).sub(coefficients[0].diff(varOrd)).div(coefficients[0]).simplify();
            if (!h.contains(varAbsc)){
                // Hier ist m = m(x) = int(exp(h(x)),x)
                m = new Operator(TypeOperator.integral, new Object[]{h.exp(), varOrd}).simplify();
                return solveExactDifferentialEquation(m.mult(f).simplify(simplifyTypesDifferentialEquation), varAbsc, varOrd);
            }
        } catch (EvaluationException e){
        }

        throw new DifferentialEquationNotAlgebraicallyIntegrableException();

    }
    

}
