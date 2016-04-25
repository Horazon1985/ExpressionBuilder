package abstractexpressions.expression.differentialequation;

import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.equation.SolveGeneralEquationMethods;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyMultiPolynomialMethods;
import abstractexpressions.expression.utilities.SimplifyPolynomialMethods;
import abstractexpressions.expression.utilities.SimplifyUtilities;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.MatrixFunction;
import abstractexpressions.matrixexpression.classes.TypeMatrixFunction;
import enums.TypeSimplify;
import exceptions.DifferentialEquationNotAlgebraicallyIntegrableException;
import exceptions.EvaluationException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import notations.NotationLoader;

public abstract class SolveGeneralDifferentialEquationMethods {

    /**
     * Konstanten, die aussagen, ob bei einer Gleichung keine Lösungen gefunden
     * werden konnten oder ob die Gleichung definitiv alle reellen Zahlen als
     * Lösungen besitzt.
     */
    public static final ExpressionCollection ALL_FUNCTIONS = new ExpressionCollection();
    public static final ExpressionCollection NO_SOLUTIONS = new ExpressionCollection();

    private static int indexForNextIntegrationConstant = 1;

    protected static final HashSet<TypeSimplify> simplifyTypesDifferentialEquation = getSimplifyTypesDifferentialEquation();
    protected static final HashSet<TypeSimplify> simplifyTypesSolutionOfDifferentialEquation = getSimplifyTypesSolutionOfDifferentialEquation();

    private static HashSet<TypeSimplify> getSimplifyTypesDifferentialEquation() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
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
        simplifyTypes.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        return simplifyTypes;
    }

    private static HashSet<TypeSimplify> getSimplifyTypesSolutionOfDifferentialEquation() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
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
        simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
        simplifyTypes.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        return simplifyTypes;
    }

    /**
     * In in diversen Lösungen Integrationskonstanten enthalten sind, wird hier
     * diejenige Integrationskonstante (als Variable) zurückgegeben, welche noch
     * nicht vorgekommen ist.
     */
    public static Expression getFreeIntegrationConstantVariable() {
        Expression integrationConstant = Variable.create(NotationLoader.FREE_INTEGRATION_CONSTANT_VAR + "_" + indexForNextIntegrationConstant);
        indexForNextIntegrationConstant++;
        return integrationConstant;
    }

    public static void resetIndexForIntegrationConstantVariable() {
        indexForNextIntegrationConstant = 1;
    }

    public static ArrayList<Variable> getListOfFreeIntegrationConstants(ExpressionCollection solutions) {

        HashSet<String> vars = new HashSet<>();

        // Alle Unbestimmten in den Lösungen bestimmen.
        for (Expression solution : solutions) {
            solution.addContainedIndeterminates(vars);
        }

        // Filtern.
        ArrayList<Variable> integrationConstants = new ArrayList<>();
        for (String var : vars) {
            if (var.startsWith(NotationLoader.FREE_INTEGRATION_CONSTANT_VAR)) {
                integrationConstants.add(Variable.create(var));
            }
        }

        return integrationConstants;

    }

    /**
     * Liefert (als String) die Variable var'...', wobei die Anzahl der
     * Apostrophs gleich k ist.
     */
    private static String getVarWithPrimes(String var, int k) {
        for (int i = 0; i < k; i++) {
            var += "'";
        }
        return var;
    }

    /**
     * Berechnet die Ordnung der Differentialgleichung f(x, y, y', ..., y^(n)) =
     * 0 mit x = varAbsc, y = varOrd.
     */
    public static int getOrderOfDifferentialEquation(Expression f, String varOrd) {
        int ord = 0;
        HashSet<String> vars = f.getContainedIndeterminates();
        for (String var : vars) {
            if (var.startsWith(varOrd) && var.contains("'") && var.replaceAll("'", "").equals(varOrd)) {
                ord = Math.max(ord, var.length() - var.replaceAll("'", "").length());
            }
        }
        return ord;
    }

    /**
     * Berechnet die Ordnung der Differentialgleichung f(x, y, y', ..., y^(n)) =
     * 0 mit x = varAbsc, y = varOrd.
     */
    public static int getSubOrderOfDifferentialEquation(Expression f, String varOrd) {
        int subOrd = getOrderOfDifferentialEquation(f, varOrd);
        HashSet<String> vars = f.getContainedIndeterminates();
        for (String var : vars) {
            if (var.startsWith(varOrd) && var.replaceAll("'", "").equals(varOrd)) {
                subOrd = Math.min(subOrd, var.length() - var.replaceAll("'", "").length());
            }
        }
        return subOrd;
    }

    private static Object[] reduceDifferentialEquation(Expression f, String varOrd) {

        int ord = getOrderOfDifferentialEquation(f, varOrd);
        int subOrd = getSubOrderOfDifferentialEquation(f, varOrd);
        Expression fReduced = f;

        for (int i = subOrd; i <= ord; i++) {
            fReduced = fReduced.replaceVariable(getVarWithPrimes(varOrd, i), Variable.create(getVarWithPrimes(varOrd, i - subOrd)));
        }

        Object[] reduction = new Object[2];
        reduction[0] = subOrd;
        reduction[1] = fReduced;

        return reduction;

    }

    private static ExpressionCollection getSolutionsFromSolutionsOfReducedDifferentialEquation(int subOrd, String varAbsc, String varOrd,
            ExpressionCollection solutionsOfReducedDifferentialEquation) {

        ExpressionCollection solutions = new ExpressionCollection();
        Expression integratedSolution;

        for (Expression solution : solutionsOfReducedDifferentialEquation) {
            // Implizite Lösungen können nicht weiter nach x = varAbsc integriert werden, nur explizite.
            if (isSolutionExplicit(solution, varOrd) || !isSolutionExplicit(solution, varOrd) && subOrd == 0) {
                integratedSolution = solution;
                for (int j = 0; j < subOrd; j++) {
                    integratedSolution = new Operator(TypeOperator.integral, new Object[]{solution, varAbsc});
                }
                try {
                    solutions.add(integratedSolution.add(getArbitraryPolynomial(varAbsc, subOrd - 1)).simplify(simplifyTypesSolutionOfDifferentialEquation));
                } catch (EvaluationException e) {
                    // Nichts tun, weiter iterieren.
                }
            }
        }

        return solutions;

    }

    private static Expression getArbitraryPolynomial(String varAbsc, int degree) {
        Expression polynomial = ZERO;
        for (int i = 0; i <= degree; i++) {
            polynomial = polynomial.add(getFreeIntegrationConstantVariable().mult(Variable.create(varAbsc).pow(i)));
        }
        return polynomial;
    }

    private static boolean isSolutionExplicit(Expression solution, String varOrd) {
        return !solution.contains(varOrd);
    }

    /**
     * Hauptprozedur zum algebraischen Lösen von Differntialgleichungen f(x, y,
     * y', ..., y^(n)) = g(x, y, y', ..., y^(n)).
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveDifferentialEquation(Expression f, Expression g, String varAbsc, String varOrd) throws EvaluationException {
        resetIndexForIntegrationConstantVariable();
        return solveGeneralDifferentialEquation(f, g, varAbsc, varOrd);
    }

    /**
     * Interne Hauptprozedur zum algebraischen Lösen von Differntialgleichungen
     * der Form f(x, y, y', ..., y^(n)) = g(x, y, y', ..., y^(n)).
     *
     * @throws EvaluationException
     */
    protected static ExpressionCollection solveGeneralDifferentialEquation(Expression f, Expression g, String varAbsc, String varOrd) throws EvaluationException {

        f = f.simplify(simplifyTypesDifferentialEquation);
        g = g.simplify(simplifyTypesDifferentialEquation);

        // Zunächst werden einige Äquivalenzumformungen vorgenommen.
        // TO DO.
        Expression diffEq = f.sub(g);
        // Zunächst: DGL im Folgenden Sinne reduzieren.
        Object[] reductionOfDifferentialEquation = reduceDifferentialEquation(diffEq, varOrd);

        // Reduzierte DGL lösen.
        ExpressionCollection solutionsOfReducedDifferentialEquation = solveZeroDifferentialEquation((Expression) reductionOfDifferentialEquation[1], varAbsc, varOrd);

        // Lösungen aus den Lösungen der reduzierten DGL extrahieren.
        return getSolutionsFromSolutionsOfReducedDifferentialEquation((int) reductionOfDifferentialEquation[0], varAbsc, varOrd, solutionsOfReducedDifferentialEquation);

    }

    /**
     * Interne Hauptprozedur zum algebraischen Lösen von Differntialgleichungen
     * der Form f(x, y, y', ..., y^(n)) = 0.
     *
     * @throws EvaluationException
     */
    protected static ExpressionCollection solveZeroDifferentialEquation(Expression f, String varAbsc, String varOrd) throws EvaluationException {

        f = f.simplify(simplifyTypesDifferentialEquation);

        // Triviale Fälle:
        // 1. f = 0.
        if (f.equals(ZERO)) {
            return ALL_FUNCTIONS;
        }

        // 1. f > 0 oder f < 0.
        if (f.isAlwaysPositive() || f.isAlwaysNegative()) {
            return NO_SOLUTIONS;
        }

        // Zunächst: Zerlegen in einfachere DGLen, wenn möglich.
        // Fall: f hat Produktgestalt.
        if (f.isProduct()) {
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            return solveZeroDifferentialEquationIfProduct(factors, varAbsc, varOrd);
        }

        // Fall: f hat Quotientgestalt.
        if (f.isQuotient()) {
            return solveZeroDifferentialEquationIfQuotient(((BinaryOperation) f).getLeft(), ((BinaryOperation) f).getRight(), varAbsc, varOrd);
        }

        // Fall: f hat Potenzgestalt.
        if (f.isPower()) {
            return solveZeroDifferentialEquationIfPower(((BinaryOperation) f).getLeft(), ((BinaryOperation) f).getRight(), varAbsc, varOrd);
        }

        // Grundlegendes Kriterium: Ordnung der Differentialgleichung.
        int ord = getOrderOfDifferentialEquation(f, varOrd);

        if (ord == 1) {
            return solveDifferentialEquationOfOrderOne(f, varAbsc, varOrd);
        }
        return solveDifferentialEquationOfHigherOrder(f, varAbsc, varOrd);

    }

    /**
     * Setzt die Lösung solution in die Differentialgleichung
     * differentialEquation ein und gibt dies in vereinfachter Form aus.
     *
     * @throws EvaluationException
     */
    private static Expression evaluateDifferentialEquation(Expression differentialEquation, Expression solution, String varAbsc, String varOrd) throws EvaluationException {

        Expression value = differentialEquation;
        Expression derivative = solution;

        int ord = getOrderOfDifferentialEquation(differentialEquation, varOrd);
        String varOrdWithPrimes = varOrd;
        for (int i = 0; i <= ord; i++) {
            value = value.replaceVariable(varOrdWithPrimes, derivative);
            derivative = derivative.diff(varAbsc).simplify();
            varOrdWithPrimes += "'";
        }

        return value.simplify();

    }

    private static ExpressionCollection solveZeroDifferentialEquationIfProduct(ExpressionCollection factors, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutions = new ExpressionCollection();
        ArrayList<ExpressionCollection> solutionsForParticularFactors = new ArrayList<>();
        for (Expression factor : factors) {
            solutionsForParticularFactors.add(solveZeroDifferentialEquation(factor, varAbsc, varOrd));
        }

        // Prüfung, ob irgendwo ALL_FUNCTIONS vorkommt.
        boolean allFunctionsOccurs = false;
        for (ExpressionCollection solutionsForParticularFactor : solutionsForParticularFactors) {
            allFunctionsOccurs = allFunctionsOccurs || solutionsForParticularFactor == ALL_FUNCTIONS;
        }
        if (allFunctionsOccurs) {
            return ALL_FUNCTIONS;
        }

        // Prüfung, ob überall NO_SOLUTIONS vorkommt.
        boolean allSolutionsAreNoSolution = true;
        for (ExpressionCollection solutionsForParticularFactor : solutionsForParticularFactors) {
            allSolutionsAreNoSolution = allSolutionsAreNoSolution && solutionsForParticularFactor == NO_SOLUTIONS;
        }
        if (allSolutionsAreNoSolution) {
            return NO_SOLUTIONS;
        }

        // Sonstiger Fall: Vereinigung aller Lösungen bilden.
        for (int i = 0; i < solutionsForParticularFactors.size(); i++) {
            solutions = SimplifyUtilities.union(solutions, solutionsForParticularFactors.get(i));
        }
        solutions.removeMultipleTerms();
        return solutions;

    }

    private static ExpressionCollection solveZeroDifferentialEquationIfQuotient(Expression numerator, Expression denominator, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutionsNumerator = solveZeroDifferentialEquation(numerator, varAbsc, varOrd);
        solutionsNumerator.removeMultipleTerms();
        ExpressionCollection solutions = new ExpressionCollection();

        // Nun diejenigen Lösungen aussortieren, welche im Nenner = 0 liefern.
        for (Expression solution : solutionsNumerator) {
            if (!evaluateDifferentialEquation(denominator, solution, varAbsc, varOrd).equals(ZERO)) {
                solutions.add(solution);
            }
        }

        return solutions;
    }

    private static ExpressionCollection solveZeroDifferentialEquationIfPower(Expression base, Expression exponent, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutionsNumerator = solveZeroDifferentialEquation(base, varAbsc, varOrd);
        solutionsNumerator.removeMultipleTerms();
        ExpressionCollection solutions = new ExpressionCollection();

        // Nun diejenigen Lösungen aufnehmen, welche im Exponenten einen Ausdruck liefern, welcher stets > 0 ist.
        for (Expression solution : solutionsNumerator) {
            if (evaluateDifferentialEquation(exponent, solution, varAbsc, varOrd).isAlwaysPositive()) {
                solutions.add(solution);
            }
        }

        return solutions;

    }

    protected static ExpressionCollection solveDifferentialEquationOfOrderOne(Expression f, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutions = new ExpressionCollection();

        // Typ: trennbare Veränderliche.
        try {
            solutions = solveDifferentialEquationWithSeparableVariables(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        // Typ: y' + a(x)*y + b(x) = 0.
        try {
            solutions = solveDifferentialEquationLinearOfOrderOne(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        // Typ: a(x, y) + b(x, y)*y' = 0.
        try {
            solutions = SolveSpecialDifferentialEquationMethods.solveExactDifferentialEquation(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        // Typ: m*a(x, y) + m*b(x, y)*y' = 0 ist exakt für einen integrierenden Faktor m.
        try {
            solutions = SolveSpecialDifferentialEquationMethods.solveExactDifferentialEquationWithIngeratingFactor(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        // Typ: Bernoulli-DGL a(x)*y' + b(x)*y + c(x)*y^n = 0, n != 0, 1.
        try {
            solutions = SolveSpecialDifferentialEquationMethods.solveBernoulliDifferentialEquation(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        return solutions;

    }

    protected static ExpressionCollection solveDifferentialEquationOfHigherOrder(Expression f, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutions = new ExpressionCollection();

        try {
            // Typ: y^(n) = f(x).
            solutions = solveDifferentialEquationWithOnlyHighestDerivatives(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        try {
            // Typ: Lineare DGL (darin wird in weitere Untertypen aufgeteilt).
            solutions = solveDifferentialEquationLinear(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        try {
            // Typ: y^(n+2) = f(y^(n)).
            solutions = SolveSpecialDifferentialEquationMethods.solveDifferentialEquationWithOnlyTwoDifferentDerivatives(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        return solutions;

    }

    // Algorithmen zum Lösen spezieller Differentialgleichungstypen der Ordnung 1.
    // Typ: trennbare Veränderliche.
    /**
     * Gibt die Lösung (entweder in expliziter oder in impliziter Form) einer
     * Differentialgleichung mit trennbaren Veränderlichen zurück, wenn möglich.
     *
     * @throws EvaluationException
     * @throws DifferentialEquationNotAlgebraicallyIntegrableException
     */
    private static ExpressionCollection solveDifferentialEquationWithSeparableVariables(Expression f, String varAbsc, String varOrd) throws EvaluationException, DifferentialEquationNotAlgebraicallyIntegrableException {

        ExpressionCollection solutions = new ExpressionCollection();

        ExpressionCollection solutionsForDerivative = SolveGeneralEquationMethods.solveEquation(f, ZERO, varOrd + "'");

        for (Expression singleSolutionForDerivative : solutionsForDerivative) {
            if (isRightSideOfDifferentialEquationInSeparableForm(singleSolutionForDerivative, varAbsc, varOrd)) {
                Expression[] separatedFactors = getSeparationForDifferentialEquationInSeparableForm(singleSolutionForDerivative, varAbsc, varOrd);
                solutions = SimplifyUtilities.union(solutions, getSolutionForDifferentialEquationWithSeparableVariables(separatedFactors[0], separatedFactors[1], varAbsc, varOrd));
            }
        }

        solutions.removeMultipleTerms();

        if (solutions.isEmpty()) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }
        return solutions;

    }

    /**
     * Gibt zurück, ob f(x, y) sich in der Form F(x) * G(y) schreiben lässt,
     * wobei x = varAbsc, y = varOrd ist.<br>
     * VORAUSSETZUNG: f enthält die Variablen y', y'', ... nicht.
     */
    private static boolean isRightSideOfDifferentialEquationInSeparableForm(Expression f, String varAbsc, String varOrd) {

        ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(f);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
        /*
         Wichtig: Da die DGL die Ordnung 1 besitzt und im Vorfeld bereits y'
         aufgelöst wurde, kann die rechte Seite der DGL y' = f die Variablen
         y', y'', y''' nicht enthalten.
         */
        for (Expression factor : factorsNumerator) {
            if (factor.contains(varAbsc) && factor.contains(varOrd)) {
                return false;
            }
        }
        for (Expression factor : factorsDenominator) {
            if (factor.contains(varAbsc) && factor.contains(varOrd)) {
                return false;
            }
        }
        return true;

    }

    private static Expression[] getSeparationForDifferentialEquationInSeparableForm(Expression f, String varAbsc, String varOrd) {

        Expression numeratorOfFactorWithVarAbsc = ONE, denominatorOfFactorWithVarAbsc = ONE,
                numeratorOfFactorWithVarOrd = ONE, denominatorOfFactorWithVarOrd = ONE;
        ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(f);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
        for (Expression factor : factorsNumerator) {
            if (factor.contains(varOrd)) {
                numeratorOfFactorWithVarOrd = numeratorOfFactorWithVarOrd.mult(factor);
            } else {
                // Konstante Faktoren werden im Faktor für x aufgenommen (ist an sich völlig egal, wohin).
                numeratorOfFactorWithVarAbsc = numeratorOfFactorWithVarAbsc.mult(factor);
            }
        }
        for (Expression factor : factorsDenominator) {
            if (factor.contains(varOrd)) {
                denominatorOfFactorWithVarOrd = denominatorOfFactorWithVarOrd.mult(factor);
            } else {
                // Konstante Faktoren werden im Faktor für x aufgenommen (ist an sich völlig egal, wohin).
                denominatorOfFactorWithVarAbsc = denominatorOfFactorWithVarAbsc.mult(factor);
            }
        }
        return new Expression[]{numeratorOfFactorWithVarAbsc.div(denominatorOfFactorWithVarAbsc),
            numeratorOfFactorWithVarOrd.div(denominatorOfFactorWithVarOrd)};

    }

    /**
     * Gibt die Lösung einer Differentialgleichung mit trennbaren Veränderlichen
     * wieder, wenn die Zerlegung y' = F(x) * G(y) bereits bekannt ist. Hier ist
     * F = factorWithVarAbsc, G = factorWithVarOrd.
     */
    private static ExpressionCollection getSolutionForDifferentialEquationWithSeparableVariables(Expression factorWithVarAbsc, Expression factorWithVarOrd, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();

        // Seien x = varAbsc, y = varOrd, factorWithVarAbsc = f(x), factorWithVarOrd = g(y).
        try {
            // 1. Alle Nullstellen von g(y) zu den Lösungen hinzufügen.
            ExpressionCollection constantZeros = SolveGeneralEquationMethods.solveEquation(factorWithVarOrd, ZERO, varOrd);
            solutions.addAll(constantZeros.simplify(simplifyTypesSolutionOfDifferentialEquation));
            if (constantZeros.isEmpty() && constantZeros != SolveGeneralEquationMethods.NO_SOLUTIONS) {
                // Implizit gegebene (konstante) Lösungen hinzufügen.
                solutions.add(factorWithVarOrd.simplify(simplifyTypesSolutionOfDifferentialEquation));
            }
        } catch (EvaluationException e) {
        }

        Expression integralOfFactorWithVarAbsc = new Operator(TypeOperator.integral, new Object[]{factorWithVarAbsc, varAbsc}).add(
                getFreeIntegrationConstantVariable());
        Expression integralOfReciprocalOfFactorWithVarOrd = new Operator(TypeOperator.integral, new Object[]{ONE.div(factorWithVarOrd), varOrd});

        try {
            ExpressionCollection solutionOfDiffEq = SolveGeneralEquationMethods.solveEquation(integralOfFactorWithVarAbsc, integralOfReciprocalOfFactorWithVarOrd, varOrd);
            solutions.addAll(solutionOfDiffEq.simplify(simplifyTypesSolutionOfDifferentialEquation));
            if (solutionOfDiffEq.isEmpty() && solutionOfDiffEq != SolveGeneralEquationMethods.NO_SOLUTIONS) {
                // Implizit gegebene Lösungen hinzufügen.
                Expression implicitSolution = integralOfReciprocalOfFactorWithVarOrd.sub(integralOfFactorWithVarAbsc).simplify(simplifyTypesSolutionOfDifferentialEquation);
                solutions.add(implicitSolution);
            }
        } catch (EvaluationException e) {
        }

        return solutions;

    }

    /**
     * Liefert die Lösung der Differentialgleichung a(x)*y' + b(x)*y + c(x) = 0.
     */
    protected static ExpressionCollection solveDifferentialEquationLinearOfOrderOne(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        ExpressionCollection solutions = new ExpressionCollection();

        // 1. Typprüfung
        Expression a, b, c = f;
        c = c.replaceVariable(varOrd, ZERO);
        c = c.replaceVariable(varOrd + "'", ZERO);
        Expression fMinusC;
        try {
            c = c.simplify();
            // Homogenisierung. Neues f ist a(x)*y' + b(x)*y.
            fMinusC = f.sub(c).simplify();
        } catch (EvaluationException ex) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Expression fSubstituted = fMinusC;
        fSubstituted = fSubstituted.replaceVariable(varOrd, Variable.create(NotationLoader.SUBSTITUTION_VAR + "_0"));
        fSubstituted = fSubstituted.replaceVariable(varOrd + "'", Variable.create(NotationLoader.SUBSTITUTION_VAR + "_1"));
        if (SimplifyMultiPolynomialMethods.getDegreeOfMultiPolynomial(fSubstituted, NotationLoader.SUBSTITUTION_VAR + "_0", NotationLoader.SUBSTITUTION_VAR + "_1").compareTo(BigInteger.ONE) != 0) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        b = fSubstituted.replaceVariable(NotationLoader.SUBSTITUTION_VAR + "_0", ONE);
        b = b.replaceVariable(NotationLoader.SUBSTITUTION_VAR + "_1", ZERO);
        try {
            b = b.simplify();
        } catch (EvaluationException ex) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        a = fSubstituted.replaceVariable(NotationLoader.SUBSTITUTION_VAR + "_0", ZERO);
        a = a.replaceVariable(NotationLoader.SUBSTITUTION_VAR + "_1", ONE);
        try {
            a = a.simplify();
        } catch (EvaluationException ex) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // Prüfung, ob f - (a*y' + b*y + c) = 0.
        try {
            Expression difference = f.sub(a.mult(Variable.create(varOrd + "'")).add(b.mult(Variable.create(varOrd))).add(c)).simplify();
            if (!difference.equals(ZERO)) {
                throw new DifferentialEquationNotAlgebraicallyIntegrableException();
            }
        } catch (EvaluationException ex) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // Division durch den Leitkoeffizienten a.
        try {
            // a wird nicht mehr benötigt (und müsste eigentlich auf 1 gesetzt werden).
            c = c.div(a).simplify();
            b = b.div(a).simplify();
        } catch (EvaluationException ex) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // Die DGL hat nun die Form y'' + b(x)*y + c(x) = 0; 
        Expression expOfIntegralOfB = new Operator(TypeOperator.integral, new Object[]{b, varAbsc});
        Expression solution;
        try {
            expOfIntegralOfB = expOfIntegralOfB.exp().simplify();
            solution = getFreeIntegrationConstantVariable().sub(
                    new Operator(TypeOperator.integral, new Object[]{c.mult(expOfIntegralOfB), varAbsc})).div(expOfIntegralOfB).simplify(simplifyTypesSolutionOfDifferentialEquation);
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        solutions.add(solution);
        return solutions;

    }

    /**
     * Liefert die Lösung der Differentialgleichung a_n*y^(n) + ... + a_0*y +
     * b(x) = 0 mittels Variation der Konstanten.
     */
    private static ExpressionCollection solveDifferentialEquationLinear(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        int ord = getOrderOfDifferentialEquation(f, varOrd);

        // Es sollen nur lineare Differentialgleichungen höherer Ordnung betrachtet werden.
        if (ord < 2) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // 1. Typprüfung
        if (!isDifferentialEquationLinear(f, varAbsc, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Expression b = getRestCoefficientInLinearDifferentialEquation(f, varAbsc, varOrd);

        Expression fHomogeneous;
        try {
            fHomogeneous = f.sub(b).simplify();
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // 2. Homogene lineare Gleichung lösen.
        ExpressionCollection solutionsOfHomogeneousDiffEq;
        try {
            solutionsOfHomogeneousDiffEq = getLinearlyIndependentSolutionsOfDifferentialEquationHomogeneousAndLinear(fHomogeneous, varAbsc, varOrd);
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // Wenn b == 0, dann einfach die (allgemeine) homogene Lösung ausgeben.
        if (b.equals(ZERO)) {
            Expression generalSolution = ZERO;
            for (Expression solution : solutionsOfHomogeneousDiffEq) {
                generalSolution = generalSolution.add(getFreeIntegrationConstantVariable().mult(solution));
            }
            try {
                return new ExpressionCollection(generalSolution.simplify(simplifyTypesSolutionOfDifferentialEquation));
            } catch (EvaluationException e) {
                throw new DifferentialEquationNotAlgebraicallyIntegrableException();
            }
        }

        // Ab hier ist b != 0.
        if (solutionsOfHomogeneousDiffEq.getBound() != ord) {
            /* 
             In diesem Fall wurde keine Basis, sondern höchstens eine linear 
             unabhängige Teilmenge des Lösungsraumes ermittelt.
             */
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // Konstanten C_1(x), ..., C_n(x).
        ExpressionCollection constants = new ExpressionCollection();

        // 3. Methode der Variation der Konstanten anwenden.
        Expression[][] wronskiEntries = new Expression[solutionsOfHomogeneousDiffEq.getBound()][solutionsOfHomogeneousDiffEq.getBound()];

        for (int i = 0; i < solutionsOfHomogeneousDiffEq.getBound(); i++) {
            for (int j = 0; j < solutionsOfHomogeneousDiffEq.getBound(); j++) {
                try {
                    wronskiEntries[i][j] = new Operator(TypeOperator.diff, new Object[]{solutionsOfHomogeneousDiffEq.get(j), varAbsc, i}).simplify();
                } catch (EvaluationException e) {
                    throw new DifferentialEquationNotAlgebraicallyIntegrableException();
                }
            }
        }

        // Wronskideterminante (= Nenner aller Konstanten) bilden.
        Expression wronskiDet = getWronskiDet(wronskiEntries, varAbsc);
        Expression numeratorOfConstant;
        for (int i = 0; i < solutionsOfHomogeneousDiffEq.getBound(); i++) {
            // Zähler der einzelnen Konstanten bilden.
            numeratorOfConstant = getNumeratorDet(wronskiEntries, varAbsc, i, b);
            // Jeweilige Konstante C_i'(x) bilden.
            constants.add(numeratorOfConstant.div(wronskiDet));
        }

        // Nun alle C_i'(x) integrieren.
        for (int i = 0; i < constants.getBound(); i++) {
            try {
                constants.put(i, new Operator(TypeOperator.integral, new Object[]{constants.get(i), varAbsc}).simplify());
            } catch (EvaluationException e) {
                throw new DifferentialEquationNotAlgebraicallyIntegrableException();
            }
        }

        // Nun y(x) = (C_1(x) + K_1)*y_1(x) + ... + (C_n(x) + K_n)*y_n(x) bilden und zurückgeben.
        Expression solution = ZERO;
        for (int i = 0; i < solutionsOfHomogeneousDiffEq.getBound(); i++) {
            solution = solution.add(constants.get(i).add(getFreeIntegrationConstantVariable()).mult(solutionsOfHomogeneousDiffEq.get(i)));
        }

        try {
            return new ExpressionCollection(solution.simplify(simplifyTypesSolutionOfDifferentialEquation));
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

    }

    private static Expression getWronskiDet(Expression[][] wronskiEntries, String varAbsc) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        MatrixExpression wronskiDet;
        try {
            wronskiDet = new MatrixFunction(new Matrix(wronskiEntries), TypeMatrixFunction.det).simplify();
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Object wronskiDetAsObject = wronskiDet.convertOneTimesOneMatrixToExpression();

        if (wronskiDetAsObject instanceof Expression) {
            return (Expression) wronskiDetAsObject;
        }

        throw new DifferentialEquationNotAlgebraicallyIntegrableException();

    }

    private static Expression getNumeratorDet(Expression[][] wronskiEntries, String varAbsc, int k, Expression restCoefficient) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        Expression[][] matrixEntries = new Expression[wronskiEntries.length][wronskiEntries.length];
        for (int i = 0; i < wronskiEntries.length; i++) {
            for (int j = 0; j < wronskiEntries.length; j++) {
                if (j == k) {
                    if (i < wronskiEntries.length - 1) {
                        matrixEntries[i][j] = ZERO;
                    } else {
                        matrixEntries[wronskiEntries.length - 1][j] = MINUS_ONE.mult(restCoefficient);
                    }
                } else {
                    matrixEntries[i][j] = wronskiEntries[i][j];
                }
            }
        }

        MatrixExpression numeratorDet;
        try {
            numeratorDet = new MatrixFunction(new Matrix(matrixEntries), TypeMatrixFunction.det).simplify();
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Object numeratorDetAsObject = numeratorDet.convertOneTimesOneMatrixToExpression();

        if (numeratorDetAsObject instanceof Expression) {
            return (Expression) numeratorDetAsObject;
        }

        throw new DifferentialEquationNotAlgebraicallyIntegrableException();

    }

    /**
     * Liefert ein linear unabhängiges Erzeugendensystem des Lösungsraumes der
     * Differentialgleichung a_n*y^(n) + ... + a_0*y = 0, wenn möglich.
     */
    private static ExpressionCollection getLinearlyIndependentSolutionsOfDifferentialEquationHomogeneousAndLinear(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException, EvaluationException {

        int ord = getOrderOfDifferentialEquation(f, varOrd);

        // Es sollen nur lineare Differentialgleichungen höherer Ordnung betrachtet werden.
        if (ord < 2) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        if (!isDifferentialEquationHomogeneousAndLinear(f, varAbsc, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // Fall: lineare und homogene DGL mit konstanten Koeffizienten.
        if (isDifferentialEquationHomogeneousAndLinearWithConstantCoefficients(f, varAbsc, varOrd)) {
            return getLinearlyIndependentSolutionsOfDifferentialEquationHomogeneousAndLinearWithConstantCoefficients(f, varAbsc, varOrd);
        }

        // Fall: a_n*x^n*y^(n) + ... + a_1*x*y' + a_0*y = 0.
        if (isDifferentialEquationHomogeneousAndLinearWithQuasiHomogeneousCoefficients(f, varAbsc, varOrd)) {
            return getLinearlyIndependentSolutionsOfDifferentialEquationHomogeneousAndLinearWithQuasiHomogeneousCoefficients(f, varAbsc, varOrd);
        }

        // Sonst: keine exakte Lösung gefunden.
        throw new DifferentialEquationNotAlgebraicallyIntegrableException();

    }

    /*
     Algorithmen zum Lösen spezieller Differentialgleichungstypen allgemeiner Ordnung.
     */
    //Typ: Homogene lineare DGLen mit konstanten Koeffizienten.
    /*
     Algorithmen zum Lösen spezieller Differentialgleichungstypen allgemeiner Ordnung.
     */
    //Typ: Homogene lineare DGLen mit konstanten Koeffizienten.
    /**
     * Liefert, ob die DGL f(x, y, y', ..., y^(n)) = 0 mit x = varAbsc, y =
     * varOrd eine lineare DGL ist.
     */
    private static boolean isDifferentialEquationLinear(Expression f, String varAbsc, String varOrd) {

        int n = getOrderOfDifferentialEquation(f, varOrd);
        String varOrdWithPrimes = varOrd;
        Expression restCoefficient = f;
        for (int i = 0; i <= n; i++) {
            restCoefficient = restCoefficient.replaceVariable(varOrdWithPrimes, ZERO);
            varOrdWithPrimes += "'";
        }

        try {
            f = f.sub(restCoefficient).simplify();
            return isDifferentialEquationHomogeneousAndLinear(f, varAbsc, varOrd);
        } catch (EvaluationException e) {
            return false;
        }

    }

    /**
     * Liefert, ob die DGL f(x, y, y', ..., y^(n)) = 0 mit x = varAbsc, y =
     * varOrd eine lineare homogene DGL ist.
     */
    private static boolean isDifferentialEquationHomogeneousAndLinear(Expression f, String varAbsc, String varOrd) {

        /*
         Idee: Die i-te Ableitung y^(i) wird durch die Variable X_i ersetzt. Danach wird 
         geprüft, ob f als Multipolynom in den Variablen X_0, ..., X_n Grad <= 1 besitzt.
         */
        int n = getOrderOfDifferentialEquation(f, varOrd);
        String varOrdWithPrimes = varOrd;
        ArrayList<String> vars = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            f = f.replaceVariable(varOrdWithPrimes, Variable.create(NotationLoader.SUBSTITUTION_VAR + "_" + i));
            vars.add(NotationLoader.SUBSTITUTION_VAR + "_" + i);
            varOrdWithPrimes += "'";
        }

        BigInteger deg = SimplifyMultiPolynomialMethods.getDegreeOfMultiPolynomial(f, vars);
        if (!(deg.compareTo(BigInteger.ONE) == 0)) {
            return false;
        }

        // Jetzt wird auf Homogenität überprüft.
        for (int i = 0; i <= n; i++) {
            f = f.replaceVariable(NotationLoader.SUBSTITUTION_VAR + "_" + i, ZERO);
        }
        try {
            f = f.simplify();
            return f.equals(ZERO);
        } catch (EvaluationException e) {
            return false;
        }

    }

    /**
     * Gibt zurück, ob die DGL f(x, y, y', ..., y^(n)) = 0 mit x = varAbsc, y =
     * varOrd eine lineare homogene DGL mit konstanten Koeffizienten ist.
     */
    private static boolean isDifferentialEquationHomogeneousAndLinearWithConstantCoefficients(Expression f, String varAbsc, String varOrd) {

        if (f.contains(varAbsc)) {
            return false;
        }

        /*
         Idee: Die i-te Ableitung y^(i) wird durch die Variable X_i ersetzt. Danach wird 
         geprüft, ob f als Multipolynom in den Variablen X_0, ..., X_n Grad <= 1 besitzt.
         */
        int n = getOrderOfDifferentialEquation(f, varOrd);
        String varOrdWithPrimes = varOrd;
        ArrayList<String> vars = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            f = f.replaceVariable(varOrdWithPrimes, Variable.create(NotationLoader.SUBSTITUTION_VAR + "_" + i));
            vars.add(NotationLoader.SUBSTITUTION_VAR + "_" + i);
            varOrdWithPrimes += "'";
        }

        BigInteger deg = SimplifyMultiPolynomialMethods.getDegreeOfMultiPolynomial(f, vars);
        if (!(deg.compareTo(BigInteger.ONE) == 0)) {
            return false;
        }

        // Jetzt wird auf Homogenität überprüft.
        for (int i = 0; i <= n; i++) {
            f = f.replaceVariable(NotationLoader.SUBSTITUTION_VAR + "_" + i, ZERO);
        }
        try {
            f = f.simplify();
            return f.equals(ZERO);
        } catch (EvaluationException e) {
            return false;
        }

    }

    /**
     * Gibt zurück, ob die DGL f(x, y, y', ..., y^(n)) = 0 mit x = varAbsc, y =
     * varOrd eine lineare DGL der Form a_n*x^n*y^(n) + ... + a_1*x*y' + a_0*y =
     * 0 ist.
     */
    private static boolean isDifferentialEquationHomogeneousAndLinearWithQuasiHomogeneousCoefficients(Expression f, String varAbsc, String varOrd) {

        // Zunächst generell auf Linearität prüfen.
        if (!isDifferentialEquationLinear(f, varAbsc, varOrd)) {
            return false;
        }

        int n = getOrderOfDifferentialEquation(f, varOrd);

        // Jetzt wird auf Homogenität überprüft.
        Expression fCopy = f;
        for (int i = 0; i <= n; i++) {
            fCopy = fCopy.replaceVariable(getVarWithPrimes(varOrd, i), ZERO);
        }
        try {
            fCopy = fCopy.simplify();
            if (!fCopy.equals(ZERO)) {
                return false;
            }
        } catch (EvaluationException e) {
            return false;
        }

        // Nun einzelnen Koeffizienten betrachten.
        ExpressionCollection coefficients = new ExpressionCollection();
        for (int i = 0; i <= n; i++) {
            fCopy = f;
            for (int j = 0; j <= n; j++) {
                if (i == j) {
                    fCopy = fCopy.replaceVariable(getVarWithPrimes(varOrd, j), ONE);
                } else {
                    fCopy = fCopy.replaceVariable(getVarWithPrimes(varOrd, j), ZERO);
                }
            }
            try {
                coefficients.add(fCopy.simplify());
            } catch (EvaluationException e) {
                return false;
            }
        }

        // Jetzt wird auf Quasihomogenität der Koeffizienten überprüft.
        Expression quotient;
        for (int i = 0; i <= n; i++) {
            try {
                quotient = coefficients.get(i).div(Variable.create(varAbsc).pow(i)).simplify();
                if (quotient.contains(varAbsc)) {
                    return false;
                }
            } catch (EvaluationException e) {
                return false;
            }
        }

        return true;

    }

    /**
     * Gibt den Koeffizienten b(x) zurück, wenn die DGL f = 0 die Form
     * a_n(x)*y^(n) + ... + a_0(x)*y + b(x) = 0 besitzt. Eine Prüfung auf
     * Linearität findet NICHT statt.
     *
     * @throws DifferentialEquationNotAlgebraicallyIntegrableException
     */
    private static Expression getRestCoefficientInLinearDifferentialEquation(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        int ord = getOrderOfDifferentialEquation(f, varOrd);

        String varOrdWithPrimes = varOrd;
        for (int i = 0; i <= ord; i++) {
            f = f.replaceVariable(varOrdWithPrimes, Variable.create(NotationLoader.SUBSTITUTION_VAR + "_" + i));
            varOrdWithPrimes += "'";
        }

        for (int i = 0; i <= ord; i++) {
            f = f.replaceVariable(NotationLoader.SUBSTITUTION_VAR + "_" + i, ZERO);
        }

        try {
            return f.simplify();
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

    }

    /**
     * Gibt die Koeffizienten a_0(x), ..., a_n(x) zurück, wenn die DGL f = 0 die
     * Form a_n(x)*y^(n) + ... + a_0(x)*y + b(x) = 0 besitzt. Eine Prüfung auf
     * Linearität findet NICHT statt.
     *
     * @throws DifferentialEquationNotAlgebraicallyIntegrableException
     */
    private static ArrayList<Expression> getLinearCoefficientsInLinearDifferentialEquation(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        int ord = getOrderOfDifferentialEquation(f, varOrd);

        try {
            f = f.sub(getRestCoefficientInLinearDifferentialEquation(f, varAbsc, varOrd)).simplify();
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        ArrayList<Expression> coefficients = new ArrayList<>();

        Expression fCopy;
        for (int i = 0; i <= ord; i++) {
            fCopy = f;
            for (int j = 0; j <= ord; j++) {
                if (i == j) {
                    fCopy = fCopy.replaceVariable(getVarWithPrimes(varOrd, j), ONE);
                } else {
                    fCopy = fCopy.replaceVariable(getVarWithPrimes(varOrd, j), ZERO);
                }
            }
            try {
                fCopy = fCopy.simplify();
            } catch (EvaluationException e) {
                throw new DifferentialEquationNotAlgebraicallyIntegrableException();
            }
            coefficients.add(fCopy);
        }

        return coefficients;

    }

    /**
     * Liefert Lösungen einer homogenen linearen DGL beliebiger Ordnung mit
     * konstanten Koeffizienten.
     */
    private static ExpressionCollection getLinearlyIndependentSolutionsOfDifferentialEquationHomogeneousAndLinearWithConstantCoefficients(Expression f, String varAbsc, String varOrd) throws EvaluationException, DifferentialEquationNotAlgebraicallyIntegrableException {

        ExpressionCollection linearIndependentSolutions = new ExpressionCollection();

        if (!isDifferentialEquationHomogeneousAndLinearWithConstantCoefficients(f, varAbsc, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // 1. Ermittlung der Koeffizienten.
        int n = getOrderOfDifferentialEquation(f, varOrd);
        ExpressionCollection coefficients = new ExpressionCollection();
        String varOrdWithPrimes = varOrd;
        HashSet<String> vars = f.getContainedIndeterminates();

        Expression coefficient;
        for (int i = 0; i <= n; i++) {
            coefficient = f;
            for (String var : vars) {
                if (var.equals(varOrdWithPrimes)) {
                    coefficient = coefficient.replaceVariable(var, ONE);
                } else {
                    coefficient = coefficient.replaceVariable(var, ZERO);
                }
            }
            coefficient = coefficient.simplify();
            coefficients.add(coefficient);
            varOrdWithPrimes = varOrdWithPrimes + "'";
        }

        Expression charPolynomial = SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficients, NotationLoader.SUBSTITUTION_VAR);

        // Charakteristisches Polynom versuchen, vollständig zu faktorisieren.
        charPolynomial = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(charPolynomial, NotationLoader.SUBSTITUTION_VAR);
        ExpressionCollection factors = SimplifyUtilities.getFactors(charPolynomial);

        for (Expression factor : factors) {
            linearIndependentSolutions.addAll(getSolutionForPowerOfIrredicibleFactorIfCoefficientsAreConstant(factor, varAbsc, NotationLoader.SUBSTITUTION_VAR));
        }

        return new ExpressionCollection(linearIndependentSolutions.simplify(simplifyTypesSolutionOfDifferentialEquation));

    }

    /**
     * Gibt für einen Faktor (konkret: (x - a)^k oder (x^2+a*x+b)^k mit
     * x^2+a*x+b irreduzibel) des charakteristischen Polynoms Basiselemente des
     * Lösungsraumes zurück, wenn die DGL linear und homogen ist mit konstanten
     * Koeffizienten.
     */
    private static ExpressionCollection getSolutionForPowerOfIrredicibleFactorIfCoefficientsAreConstant(Expression factor, String varAbsc, String varInCharPolynomial) {

        ExpressionCollection solutionBase = new ExpressionCollection();

        Expression base;
        int exponent;
        if (factor.isIntegerPower()) {
            base = ((BinaryOperation) factor).getLeft();
            if (((Constant) ((BinaryOperation) factor).getRight()).getValue().compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
                // Sollte eigentlich nie vorkommen.
                return solutionBase;
            }
            exponent = ((Constant) ((BinaryOperation) factor).getRight()).getValue().intValue();
        } else {
            base = factor;
            exponent = 1;
        }

        BigInteger deg = SimplifyPolynomialMethods.getDegreeOfPolynomial(base, varInCharPolynomial);
        ExpressionCollection zeros;

        if (deg.equals(BigInteger.ONE)) {
            // Fall: Potenz eines Linearfaktors.
            try {
                zeros = SolveGeneralEquationMethods.solveEquation(base, ZERO, varInCharPolynomial);
                if (!zeros.isEmpty()) {
                    for (int i = 0; i < exponent; i++) {
                        solutionBase.add(Variable.create(varAbsc).pow(i).mult(zeros.get(0).mult(Variable.create(varAbsc)).exp()));
                    }
                }
            } catch (EvaluationException e) {
            }
        } else if (deg.equals(BigInteger.valueOf(2))) {
            // Fall: Potenz eines irreduziblen quadratischen Faktors.
            try {
                ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(base, varInCharPolynomial);
                if (coefficients.getBound() != 3) {
                    // Sollte bei korrekter vorheriger Prüfung eigentlich nie eintreten.
                    return solutionBase;
                }
                Expression discriminant = coefficients.get(1).pow(2).sub(new Constant(4).mult(coefficients.get(0)).mult(coefficients.get(2))).simplify();
                if (discriminant.isAlwaysNegative()) {
                    Expression factorForExp = MINUS_ONE.mult(coefficients.get(1)).div(TWO.mult(coefficients.get(2))).simplify();
                    Expression factorForSinCos = MINUS_ONE.mult(discriminant).pow(1, 2).div(TWO.mult(coefficients.get(2))).simplify();
                    for (int i = 0; i < exponent; i++) {
                        solutionBase.add(Variable.create(varAbsc).pow(i).mult(factorForExp.mult(Variable.create(varAbsc)).exp()).mult(
                                factorForSinCos.mult(Variable.create(varAbsc)).sin()));
                        solutionBase.add(Variable.create(varAbsc).pow(i).mult(factorForExp.mult(Variable.create(varAbsc)).exp()).mult(
                                factorForSinCos.mult(Variable.create(varAbsc)).cos()));
                    }
                }
            } catch (EvaluationException e) {
            }
        }

        return solutionBase;

    }

    /**
     * Gibt für einen Faktor (konkret: (x - a)^k oder (x^2+a*x+b)^k mit
     * x^2+a*x+b irreduzibel) des charakteristischen Polynoms Basiselemente des
     * Lösungsraumes zurück, wenn die DGL linear und homogen ist mit
     * quasihomogenen Koeffizienten.
     */
    private static ExpressionCollection getSolutionForPowerOfIrredicibleFactorIfCoefficientsAreQuasiHomogeneous(Expression factor, String varAbsc, String varInCharPolynomial) {

        ExpressionCollection solutionBase = new ExpressionCollection();

        Expression base;
        int exponent;
        if (factor.isIntegerPower()) {
            base = ((BinaryOperation) factor).getLeft();
            if (((Constant) ((BinaryOperation) factor).getRight()).getValue().compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
                // Sollte eigentlich nie vorkommen.
                return solutionBase;
            }
            exponent = ((Constant) ((BinaryOperation) factor).getRight()).getValue().intValue();
        } else {
            base = factor;
            exponent = 1;
        }

        BigInteger deg = SimplifyPolynomialMethods.getDegreeOfPolynomial(base, varInCharPolynomial);
        ExpressionCollection zeros;

        if (deg.equals(BigInteger.ONE)) {
            // Fall: Potenz eines Linearfaktors.
            try {
                zeros = SolveGeneralEquationMethods.solveEquation(base, ZERO, varInCharPolynomial);
                if (!zeros.isEmpty()) {
                    for (int i = 0; i < exponent; i++) {
                        solutionBase.add(Variable.create(varAbsc).ln().pow(i).mult(Variable.create(varAbsc).pow(zeros.get(0))));
                    }
                }
            } catch (EvaluationException e) {
            }
        } else if (deg.equals(BigInteger.valueOf(2))) {
            // Fall: Potenz eines irreduziblen quadratischen Faktors.
            try {
                ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(base, varInCharPolynomial);
                if (coefficients.getBound() != 3) {
                    // Sollte bei korrekter vorheriger Prüfung eigentlich nie eintreten.
                    return solutionBase;
                }
                Expression discriminant = coefficients.get(1).pow(2).sub(new Constant(4).mult(coefficients.get(0)).mult(coefficients.get(2))).simplify();
                if (discriminant.isAlwaysNegative()) {
                    /* 
                     Wenn die Nullstellen des quadratischen Faktors a +- i*b lauten,
                     so sind die Lösungen wie folgt gegeben:
                     ln(x)^k*x^a*cos(b*ln(x)) und ln(x)^k*x^a*sin(b*ln(x)), 
                     k = 0, 1, 2, ..., exponent - 1.
                     */
                    Expression realPartOfZero = MINUS_ONE.mult(coefficients.get(1).div(TWO.mult(coefficients.get(2)))).simplify();
                    Expression imaginaryPartOfZero = MINUS_ONE.mult(discriminant).pow(1, 2).div(TWO.mult(coefficients.get(2))).simplify();
                    for (int i = 0; i < exponent; i++) {
                        solutionBase.add(Variable.create(varAbsc).ln().pow(i).mult(Variable.create(varAbsc).pow(realPartOfZero)).mult(
                                imaginaryPartOfZero.mult(Variable.create(varAbsc).ln()).cos()));
                        solutionBase.add(Variable.create(varAbsc).ln().pow(i).mult(Variable.create(varAbsc).pow(realPartOfZero)).mult(
                                imaginaryPartOfZero.mult(Variable.create(varAbsc).ln()).sin()));
                    }
                }
            } catch (EvaluationException e) {
            }
        }

        return solutionBase;

    }

    /**
     * Liefert Lösungen einer DGL vom folgenden Typ: a_n*x^n*y^(n) + ... +
     * a_1*x*y' + a_0*y = 0.
     */
    private static ExpressionCollection getLinearlyIndependentSolutionsOfDifferentialEquationHomogeneousAndLinearWithQuasiHomogeneousCoefficients(Expression f, String varAbsc, String varOrd) throws EvaluationException, DifferentialEquationNotAlgebraicallyIntegrableException {

        ExpressionCollection linearIndependentSolutions = new ExpressionCollection();

        if (!isDifferentialEquationHomogeneousAndLinearWithQuasiHomogeneousCoefficients(f, varAbsc, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // 1. Ermittlung der Koeffizienten.
        int n = getOrderOfDifferentialEquation(f, varOrd);
        ArrayList<Expression> coefficients = getLinearCoefficientsInLinearDifferentialEquation(f, varAbsc, varOrd);

        Expression charPolynomial = ZERO;
        Expression quotient, summand;
        for (int i = 0; i < coefficients.size(); i++) {
            try {
                quotient = coefficients.get(i).div(Variable.create(varAbsc).pow(i)).simplify();
                if (quotient.contains(varAbsc)) {
                    throw new DifferentialEquationNotAlgebraicallyIntegrableException();
                }
            } catch (EvaluationException e) {
                throw new DifferentialEquationNotAlgebraicallyIntegrableException();
            }
            summand = quotient;
            for (int j = 0; j < i; j++) {
                summand = summand.mult(Variable.create(NotationLoader.SUBSTITUTION_VAR).sub(j));
            }
            charPolynomial = charPolynomial.add(summand);
        }

        charPolynomial = charPolynomial.simplify();

        // Charakteristisches Polynom versuchen, vollständig zu faktorisieren.
        charPolynomial = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(charPolynomial, NotationLoader.SUBSTITUTION_VAR);
        ExpressionCollection factors = SimplifyUtilities.getFactors(charPolynomial);

        for (Expression factor : factors) {
            linearIndependentSolutions.addAll(getSolutionForPowerOfIrredicibleFactorIfCoefficientsAreQuasiHomogeneous(factor, varAbsc, NotationLoader.SUBSTITUTION_VAR));
        }

        return new ExpressionCollection(linearIndependentSolutions.simplify(simplifyTypesSolutionOfDifferentialEquation));

    }

    /**
     * Hilfsmethode. Liefert, ob in f nur bestimmte Ableitungen von y = varOrd
     * auftauchen. Diese Ordnungen sind in orders übergeben.
     */
    protected static boolean doesExpressionContainOnlyDerivativesOfGivenOrder(Expression f, String varOrd, int orderOfDiffEq, int... orders) {

        HashSet<String> forbiddenDerivatives = new HashSet<>();

        String varOrdWithPrimes = varOrd;
        boolean found;

        // Blacklist aller verbotenen Ableitungen erstellen.
        for (int i = 0; i <= orderOfDiffEq; i++) {
            found = false;
            for (int ord : orders) {
                if (ord == i) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                forbiddenDerivatives.add(varOrdWithPrimes);
            }
            varOrdWithPrimes += "'";
        }

        // Prüfen.
        for (String derivative : forbiddenDerivatives) {
            if (f.contains(derivative)) {
                return false;
            }
        }

        return true;

    }

    //Typ: y^(n) = f(x).
    /**
     * Liefert Lösungen von Differentialgleichungen vom Typ y^(n + 2) =
     * g(y^(n)). Die eingegebene DGL hat die Form f = 0;
     */
    private static ExpressionCollection solveDifferentialEquationWithOnlyHighestDerivatives(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        ExpressionCollection solutions = new ExpressionCollection();

        int ord = getOrderOfDifferentialEquation(f, varOrd);

        String varOrdWithPrimes = getVarWithPrimes(varOrd, ord);

        ExpressionCollection solutionsForHighestDerivative;
        try {
            solutionsForHighestDerivative = SolveGeneralEquationMethods.solveEquation(f, ZERO, varOrdWithPrimes);
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
            isEquationOfProperType = doesExpressionContainOnlyDerivativesOfGivenOrder(solutionForHighestDerivative, varOrd, ord);
            if (isEquationOfProperType) {
                solutions = SimplifyUtilities.union(solutions,
                        solveDifferentialEquationWithOnlyHighestDerivatives(solutionForHighestDerivative, ord, varAbsc));
            }
        }

        solutions.removeMultipleTerms();
        return solutions;

    }

    private static ExpressionCollection solveDifferentialEquationWithOnlyHighestDerivatives(Expression f, int ord, String varAbsc) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        Expression solution = f;

        // Rechte Seite f ord-mal integrieren.
        for (int i = 0; i < ord; i++) {
            solution = new Operator(TypeOperator.integral, new Object[]{solution, varAbsc});
        }

        for (int i = 0; i < ord; i++) {
            solution = solution.add(getFreeIntegrationConstantVariable().mult(Variable.create(varAbsc).pow(i)));
        }

        try {
            return new ExpressionCollection(solution.simplify(simplifyTypesSolutionOfDifferentialEquation));
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

    }

}
