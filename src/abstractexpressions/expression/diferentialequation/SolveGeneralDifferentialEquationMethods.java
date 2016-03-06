package abstractexpressions.expression.diferentialequation;

import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.equation.SolveGeneralEquationMethods;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyUtilities;
import enums.TypeSimplify;
import exceptions.EvaluationException;
import java.util.HashSet;
import notations.NotationLoader;

public abstract class SolveGeneralDifferentialEquationMethods {

    /**
     * Konstanten, die aussagen, ob bei einer Gleichung keine Lösungen gefunden
     * werden konnten oder ob die Gleichung definitiv alle reellen Zahlen als
     * Lösungen besitzt.
     */
    public static final ExpressionCollection ALL_REALS = new ExpressionCollection();
    public static final ExpressionCollection NO_SOLUTIONS = new ExpressionCollection();

    private static final HashSet<TypeSimplify> simplifyTypesDifferentialEquation = getSimplifyTypesDifferentialEquation();

    private static HashSet<TypeSimplify> getSimplifyTypesDifferentialEquation() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_factorize);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        return simplifyTypes;
    }

    /**
     * In f sind Variablen enthalten, unter anderem möglicherweise auch
     * "Parametervariablen" C_1, C_2, .... Diese Funktion liefert dasjenige C_i
     * mit dem kleinsten Index i, welches in f noch nicht vorkommt.
     */
    public static String getFreeIntegrationConstantVariable(Expression f) {
        String var = NotationLoader.FREE_INTEGRATION_CONSTANT_VAR + "_";
        int j = 1;
        while (f.contains(var + String.valueOf(j))) {
            j++;
        }
        return var + j;
    }
    
    /**
     * Hauptprozedur zum algebraischen Lösen von Differntialgleichungen f(x, y,
     * y', ..., y^(n)) = g(x, y, y', ..., y^(n)).
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveDifferentialEquation(Expression f, Expression g, String varAbsc, String varOrd) throws EvaluationException {
        if (g.equals(ZERO)) {
            return solveZeroDifferentialEquation(f, varAbsc, varOrd);
        }
        return solveGeneralDifferentialEquation(f, g, varAbsc, varOrd);
    }

    /**
     * Interne Hauptprozedur zum algebraischen Lösen von Differntialgleichungen
     * der Form f(x, y, y', ..., y^(n)) = g(x, y, y', ..., y^(n)).
     *
     * @throws EvaluationException
     */
    protected static ExpressionCollection solveGeneralDifferentialEquation(Expression f, Expression g, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutions = new ExpressionCollection();

        // TO DO.
        return solutions;

    }

    /**
     * Interne Hauptprozedur zum algebraischen Lösen von Differntialgleichungen
     * der Form f(x, y, y', ..., y^(n)) = 0.
     *
     * @throws EvaluationException
     */
    protected static ExpressionCollection solveZeroDifferentialEquation(Expression f, String varAbsc, String varOrd) throws EvaluationException {

        // Grundlegendes Kriterium: Ordnung der Differentialgleichung.
        int ord = getOrderOfDifferentialEquation(f, varAbsc, varOrd);

        if (ord == 1) {
            return solveDifferentialEquationOfDegOne(f, varAbsc, varOrd);
        }
        return solveDifferentialEquationOfHigherDeg(f, varAbsc, varOrd);

    }

    /**
     * Berechnet die Ordnung der Differentialgleichung f(x, y, y', ..., y^(n)) =
     * 0 mit x = varAbsc, y = varOrd.
     */
    private static int getOrderOfDifferentialEquation(Expression f, String varAbsc, String varOrd) {
        int ord = 0;
        HashSet<String> vars = f.getContainedIndeterminates();
        for (String var : vars) {
            if (var.startsWith(varOrd) && var.contains("'")) {
                ord = Math.max(ord, var.length() - var.replaceAll("'", "").length());
            }
        }
        return ord;
    }

    protected static ExpressionCollection solveDifferentialEquationOfDegOne(Expression f, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutions = new ExpressionCollection();

        // Typ: trennbare Veränderliche.
        solutions = solveDifferentialEquationWithSeparableVariables(f, varAbsc, varOrd);
        if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
            return solutions;
        }

        // TO DO.
        return solutions;

    }

    protected static ExpressionCollection solveDifferentialEquationOfHigherDeg(Expression f, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutions = new ExpressionCollection();

        // TO DO.
        return solutions;

    }

    /*
     Algorithmen zum Lösen spezieller Differentialgleichungstypen der Ordnung 1.
     */
 /*
    Typ: trennbare Veränderliche.
     */
    private static ExpressionCollection solveDifferentialEquationWithSeparableVariables(Expression f, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutions = new ExpressionCollection();

        ExpressionCollection solutionsForDerivative = SolveGeneralEquationMethods.solveEquation(f, ZERO, varOrd + "'");

        for (Expression singleSolutionForDerivative : solutionsForDerivative) {
            if (isRightSideOfDifferentialEquationInSeparableForm(singleSolutionForDerivative, varAbsc, varOrd)) {
                Expression[] separatedFactors = getSeparationForDifferentialEquationInSeparableForm(singleSolutionForDerivative, varAbsc, varOrd);
                solutions = SimplifyUtilities.union(solutions, getSolutionForDifferentialEquationWithSeparableVariables(separatedFactors[0], separatedFactors[1], varAbsc, varOrd));
            }
        }

        solutions.removeMultipleTerms();
        return solutions;

    }

    private static boolean isRightSideOfDifferentialEquationInSeparableForm(Expression f, String varAbsc, String varOrd) {

        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        for (Expression factor : factors) {
            /*
            Wichtig: Da die DGL die Ordnung 1 besitzt und im Vorfeld bereits y'
            aufgelöst wurdde, kann die rechte Seite der DGL y' = f die Variablen
            y', y'', y''' nicht enthalten.
             */
            if (factor.contains(varAbsc) && factor.contains(varOrd)) {
                return false;
            }
        }
        return true;

    }

    private static Expression[] getSeparationForDifferentialEquationInSeparableForm(Expression f, String varAbsc, String varOrd) {

        Expression factorWithVarAbsc = ONE, factorWithVarOrd = ONE;
        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        for (Expression factor : factors) {
            if (factor.contains(varOrd)) {
                factorWithVarOrd = factorWithVarOrd.mult(factor);
            } else {
                // Konstante Faktoren kommen in den Faktor für x (ist an sich völlig egal, wohin).
                factorWithVarAbsc = factorWithVarAbsc.mult(factor);
            }
        }
        return new Expression[]{factorWithVarAbsc, factorWithVarOrd};

    }

    private static ExpressionCollection getSolutionForDifferentialEquationWithSeparableVariables(Expression factorWithVarAbsc, Expression factorWithVarOrd, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();

        /*
            Seien x = varAbsc, y = varOrd, factorWithVarAbsc = f(x), factorWithVarOrd = g(y).
         */
        try {
            // 1. Alle Nullstellen von g(y) zu den Lösungen hinzufügen.
            ExpressionCollection constantZeros = SolveGeneralEquationMethods.solveEquation(factorWithVarOrd, ZERO, varOrd);
            solutions.addAll(constantZeros);
        } catch (EvaluationException ex) {
        }

        
        
        
        return solutions;

    }

}
