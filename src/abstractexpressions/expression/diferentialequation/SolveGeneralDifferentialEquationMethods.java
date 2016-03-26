package abstractexpressions.expression.diferentialequation;

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
import abstractexpressions.expression.utilities.SimplifyPolynomialMethods;
import abstractexpressions.expression.utilities.SimplifyUtilities;
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
     * Hauptprozedur zum algebraischen Lösen von Differntialgleichungen f(x, y,
     * y', ..., y^(n)) = g(x, y, y', ..., y^(n)).
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveDifferentialEquation(Expression f, Expression g, String varAbsc, String varOrd) throws EvaluationException {
        resetIndexForIntegrationConstantVariable();
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

        f = f.simplify(simplifyTypesDifferentialEquation);
        g = g.simplify(simplifyTypesDifferentialEquation);

        // Zunächst werden einige Äquivalenzumformungen vorgenommen.
        // TO DO.
        return solveZeroDifferentialEquation(f.sub(g), varAbsc, varOrd);

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
        int ord = getOrderOfDifferentialEquation(f, varAbsc, varOrd);

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

        int ord = getOrderOfDifferentialEquation(differentialEquation, varAbsc, varOrd);
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

    protected static ExpressionCollection solveDifferentialEquationOfOrderOne(Expression f, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutions = new ExpressionCollection();

        // Typ: trennbare Veränderliche.
        try {
            solutions = solveDifferentialEquationWithSeparableVariables(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException ex) {
        }

        // Typ: y' + a(x)*y + b(x) = 0.
        try {
            solutions = solveDifferentialEquationLinearOfOrderOne(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException ex) {
        }

        // Typ: a(x, y) + b(x, y)*y' = 0.
        try {
            solutions = SolveSpecialDifferentialEquationMethods.solveExactDifferentialEquation(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException ex) {
        }

        // Typ: m*a(x, y) + m*b(x, y)*y' = 0 ist exakt für einen integrierenden Faktor m.
        try {
            solutions = SolveSpecialDifferentialEquationMethods.solveExactDifferentialEquationWithIngeratingFactor(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException ex) {
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
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException ex) {
        }

        try {
            // Typ: Homogene lineare DGL mit konstanten Koeffizienten.
            solutions = solveDifferentialEquationHomogeneousAndLinearWithConstantCoefficients(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException ex) {
        }

        try {
            // Typ: y^(n+2) = f(y^(n)).
            solutions = solveDifferentialEquationWithOnlyTwoDifferentDerivatives(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException ex) {
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
            solutions.addAll(constantZeros);
            if (constantZeros.isEmpty() && constantZeros != SolveGeneralEquationMethods.NO_SOLUTIONS) {
                // Implizit gegebene (konstante) Lösungen hinzufügen.
                solutions.add(factorWithVarOrd);
            }
        } catch (EvaluationException e) {
        }

        Expression integralOfFactorWithVarAbsc = new Operator(TypeOperator.integral, new Object[]{factorWithVarAbsc, varAbsc}).add(
                getFreeIntegrationConstantVariable());
        Expression integralOfReciprocalOfFactorWithVarOrd = new Operator(TypeOperator.integral, new Object[]{ONE.div(factorWithVarOrd), varOrd});

        try {
            ExpressionCollection solutionOfDiffEq = SolveGeneralEquationMethods.solveEquation(integralOfFactorWithVarAbsc, integralOfReciprocalOfFactorWithVarOrd, varOrd);
            solutions.addAll(solutionOfDiffEq);
            if (solutionOfDiffEq.isEmpty() && solutionOfDiffEq != SolveGeneralEquationMethods.NO_SOLUTIONS) {
                // Implizit gegebene Lösungen hinzufügen.
                Expression implicitSolution = integralOfReciprocalOfFactorWithVarOrd.sub(integralOfFactorWithVarAbsc).simplify();
                solutions.add(implicitSolution);
            }
        } catch (EvaluationException e) {
        }

        return solutions;

    }

    /**
     * Liefert die Lösung der Differentialgleichung a(x)*y' + b(x)*y + c(x) = 0.
     */
    private static ExpressionCollection solveDifferentialEquationLinearOfOrderOne(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

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
        if (SimplifyPolynomialMethods.getDegreeOfMultiPolynomial(fSubstituted, NotationLoader.SUBSTITUTION_VAR + "_0", NotationLoader.SUBSTITUTION_VAR + "_1").compareTo(BigInteger.ONE) != 0) {
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
            solution = getFreeIntegrationConstantVariable().sub(new Operator(TypeOperator.integral, new Object[]{c.mult(expOfIntegralOfB), varAbsc})).div(expOfIntegralOfB).simplify();
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

        int ord = getOrderOfDifferentialEquation(f, varAbsc, varOrd);

        /*
         Es sollen nur lineare Differentialgleichungen höherer Ordnung betrachtet werden.
         */
        if (ord < 2) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // 1. Typprüfung
        if (!isDifferentialEquationLinear(f, varAbsc, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        Expression b = getRestCoefficientInLinearDifferentialEquation(f, varAbsc, varOrd);
        ArrayList<Expression> coefficients = getLinearCoefficientsInLinearDifferentialEquation(f, varAbsc, varOrd);

        Expression fHomogeneous;
        try {
            fHomogeneous = f.sub(b).simplify();
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // 2. Homogene lineare Gleichung lösen.
        ExpressionCollection solutionsOfHomogeneousDiffEq;
        try {
            solutionsOfHomogeneousDiffEq = solveDifferentialEquationOfHigherOrder(fHomogeneous, varAbsc, varOrd);
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        if (solutionsOfHomogeneousDiffEq.getBound() != ord) {
            /* 
             In diesem Fall wurde keine Basis, sondern höchstens eine linear 
             unabhängige Teilmenge des Lösungsraumes ermittelt.
             */
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        ExpressionCollection constants = new ExpressionCollection();
        
        
        
        ExpressionCollection solutions = new ExpressionCollection();

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

        int n = getOrderOfDifferentialEquation(f, varAbsc, varOrd);
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
        int n = getOrderOfDifferentialEquation(f, varAbsc, varOrd);
        String varOrdWithPrimes = varOrd;
        HashSet<String> vars = new HashSet<>();
        for (int i = 0; i <= n; i++) {
            f = f.replaceVariable(varOrdWithPrimes, Variable.create(NotationLoader.SUBSTITUTION_VAR + "_" + i));
            vars.add(NotationLoader.SUBSTITUTION_VAR + "_" + i);
            varOrdWithPrimes += "'";
        }

        BigInteger deg = SimplifyPolynomialMethods.getDegreeOfMultiPolynomial(f, vars);
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
     * Liefert, ob die DGL f(x, y, y', ..., y^(n)) = 0 mit x = varAbsc, y =
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
        int n = getOrderOfDifferentialEquation(f, varAbsc, varOrd);
        String varOrdWithPrimes = varOrd;
        HashSet<String> vars = new HashSet<>();
        for (int i = 0; i <= n; i++) {
            f = f.replaceVariable(varOrdWithPrimes, Variable.create(NotationLoader.SUBSTITUTION_VAR + "_" + i));
            vars.add(NotationLoader.SUBSTITUTION_VAR + "_" + i);
            varOrdWithPrimes += "'";
        }

        BigInteger deg = SimplifyPolynomialMethods.getDegreeOfMultiPolynomial(f, vars);
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

    private static Expression getRestCoefficientInLinearDifferentialEquation(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        int ord = getOrderOfDifferentialEquation(f, varAbsc, varOrd);

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

    private static ArrayList<Expression> getLinearCoefficientsInLinearDifferentialEquation(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        int ord = getOrderOfDifferentialEquation(f, varAbsc, varOrd);

        String varOrdWithPrimes = varOrd;

        try {
            f = f.sub(getRestCoefficientInLinearDifferentialEquation(f, varAbsc, varOrd)).simplify();
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        for (int i = 0; i <= ord; i++) {
            f = f.replaceVariable(varOrdWithPrimes, Variable.create(NotationLoader.SUBSTITUTION_VAR + "_" + i));
            varOrdWithPrimes += "'";
        }

        ArrayList<Expression> coefficients = new ArrayList<>();

        Expression fCopy;
        for (int i = 0; i <= ord; i++) {
            fCopy = f;
            for (int j = 0; j <= ord; j++) {
                if (i == j) {
                    fCopy = fCopy.replaceVariable(NotationLoader.SUBSTITUTION_VAR + "_" + i, ONE);
                } else {
                    fCopy = fCopy.replaceVariable(NotationLoader.SUBSTITUTION_VAR + "_" + i, ZERO);
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
     * Liefert Lösungen einer homogenen linearen DGL beliebiger Ordnung.
     */
    private static ExpressionCollection solveDifferentialEquationHomogeneousAndLinearWithConstantCoefficients(Expression f, String varAbsc, String varOrd) throws EvaluationException, DifferentialEquationNotAlgebraicallyIntegrableException {

        ExpressionCollection solutionBase = new ExpressionCollection();

        if (!isDifferentialEquationHomogeneousAndLinearWithConstantCoefficients(f, varAbsc, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        // 1. Ermittlung der Koeffizienten.
        int n = getOrderOfDifferentialEquation(f, varAbsc, varOrd);
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
            solutionBase.addAll(getSolutionForParticularIrredicibleFactor(factor, varAbsc, NotationLoader.SUBSTITUTION_VAR));
        }

        // Aus den Basisvektoren nun die allgemeine Lösung basteln.
        Expression solution = ZERO;
        for (int i = 0; i < solutionBase.getBound(); i++) {
            solution = solution.add(getFreeIntegrationConstantVariable().mult(solutionBase.get(i)));
        }

        return new ExpressionCollection(solution.simplify());

    }

    private static ExpressionCollection getSolutionForParticularIrredicibleFactor(Expression factor, String varAbsc, String varInCharPolynomial) {

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
            // Fall: Linearfaktor.
            try {
                zeros = SolveGeneralEquationMethods.solveEquation(base, ZERO, varInCharPolynomial);
                if (!zeros.isEmpty()) {
                    for (int i = 0; i < exponent; i++) {
                        solutionBase.add(Variable.create(varAbsc).pow(i).mult(zeros.get(0).mult(Variable.create(varAbsc)).exp()));
                    }
                }
            } catch (EvaluationException ex) {
            }
        } else if (deg.equals(BigInteger.valueOf(2))) {
            // Fall: Irreduzibler quadratischer Faktor.
            try {
                ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(base, varInCharPolynomial);
                if (coefficients.getBound() < 3) {
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
            } catch (EvaluationException ex) {
            }
        }

        return solutionBase;

    }

    /**
     * Hilfsmethode. Liefert, ob in f nur bestimmte Ableitungen von y = varOrd
     * auftauchen. Diese Ordnungen sind in orders übergeben.
     */
    private static boolean doesExpressionContainOnlyDerivativesOfGivenOrder(Expression f, String varOrd, int orderOfDiffEq, int... orders) {

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

        int ord = getOrderOfDifferentialEquation(f, varAbsc, varOrd);

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
            return new ExpressionCollection(solution.simplify());
        } catch (EvaluationException e) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

    }

    //Typ: y^(n + 2) = f(y^(n)).
    /**
     * Liefert Lösungen von Differentialgleichungen vom Typ y^(n + 2) =
     * g(y^(n)). Die eingegebene DGL hat die Form f = 0;
     */
    private static ExpressionCollection solveDifferentialEquationWithOnlyTwoDifferentDerivatives(Expression f, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        ExpressionCollection solutions = new ExpressionCollection();

        int ord = getOrderOfDifferentialEquation(f, varAbsc, varOrd);

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
            isEquationOfProperType = doesExpressionContainOnlyDerivativesOfGivenOrder(solutionForHighestDerivative, varOrd, ord, ord - 2) && !solutionForHighestDerivative.contains(varAbsc);
            if (isEquationOfProperType) {
                solutions = SimplifyUtilities.union(solutions,
                        solveDifferentialEquationWithOnlyTwoDifferentDerivatives(solutionForHighestDerivative, ord, varAbsc, varOrd));
            }
        }

        solutions.removeMultipleTerms();
        return solutions;

    }

    private static ExpressionCollection solveDifferentialEquationWithOnlyTwoDifferentDerivatives(Expression rightSide, int ord, String varAbsc, String varOrd) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        try {
            ExpressionCollection solutions = new ExpressionCollection();

            /*
            Lösungsalgorithmus: Sei y^(n + 2) = f(y^(n)). Hier ist ord = n + 2.
            Dann: 1. y^(n + 1) = +-(2*g(y^(n)) + C_1)^(1/2), g = int(f(t), t).
            2. h_(C_1)(y^(n)) = +-(x + C_2), h(t) = int(1/(2 * g(t) + C_1)^(1/2), t).
            3. y = int(h^(-1)_(C_1)(+-(x + C_2)), x, n) + C_3 + C_4 * x + ... + C_(n + 2) * x^(n - 1).
             */
            // Schritt 1: Bilden von g.
            String varOrdWithPrimes = getVarWithPrimes(varOrd, ord - 2);
            Expression g = new Operator(TypeOperator.integral, new Object[]{rightSide, varOrdWithPrimes}).simplify();
            // Schritt 2: Bilden von h_(C_1).
            Expression h = new Operator(TypeOperator.integral, new Object[]{ONE.div(TWO.mult(g).add(getFreeIntegrationConstantVariable())).pow(1, 2), varOrdWithPrimes}).simplify();
            ExpressionCollection solutionsForIntermediateDerivative = SolveGeneralEquationMethods.solveEquation(h, Variable.create(varAbsc).add(getFreeIntegrationConstantVariable()), varOrdWithPrimes);
            solutionsForIntermediateDerivative = SimplifyUtilities.union(solutionsForIntermediateDerivative, SolveGeneralEquationMethods.solveEquation(h, MINUS_ONE.mult(Variable.create(varAbsc).add(getFreeIntegrationConstantVariable())), varOrdWithPrimes));

            /* 
            Sonderfall: Wenn n = 0 ist und die Gleichung h_(C_1)(y) = +-(x + C_2)
            nicht explizit nach y aufgelöst werden kann, so werden die Ausdrücke
            h_(C_1)(y) -+(x + C_2) in die Lösungen mitaufgenommen und hinterher
            als implizite Lösungen interpretiert.<br>
            BEISPIEL: y'' = y^2 besitzt die implizite Lösung
            int(1/(2*y^3/3 + C_1)^(1/2),y) +- (x + C_2) = 0.
             */
            if (ord == 2 && solutionsForIntermediateDerivative.isEmpty() && solutionsForIntermediateDerivative != SolveGeneralEquationMethods.NO_SOLUTIONS) {
                solutions.add(h.add(Variable.create(varAbsc).add(getFreeIntegrationConstantVariable())));
                solutions.add(h.sub(Variable.create(varAbsc).add(getFreeIntegrationConstantVariable())));
                return solutions;
            }

            // Schritt 3: y = int(h^(-1)_(C_1)(+-(x + C_2)), x, n) + C_3 + C_4 * x + ... + C_(n + 2) * x^(n - 1) bilden.
            Expression particularSolution;
            for (Expression solutionForIntermediateDerivative : solutionsForIntermediateDerivative) {
                particularSolution = solutionForIntermediateDerivative;
                for (int i = 0; i < ord - 2; i++) {
                    particularSolution = new Operator(TypeOperator.integral, new Object[]{particularSolution, varAbsc});
                }
                for (int i = 0; i < ord - 2; i++) {
                    particularSolution = particularSolution.add(getFreeIntegrationConstantVariable().mult(Variable.create(varAbsc).pow(i)));
                }
                solutions.add(particularSolution);
            }

            return solutions;
        } catch (EvaluationException ex) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

    }

}
