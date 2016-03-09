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
     * In f sind Variablen enthalten, unter anderem möglicherweise auch
     * "Parametervariablen" C_1, C_2, .... Diese Funktion liefert dasjenige C_i
     * mit dem kleinsten Index i, welches in f noch nicht vorkommt.
     */
    public static String getFreeIntegrationConstantVariable(ExpressionCollection exprs) {
        String var = NotationLoader.FREE_INTEGRATION_CONSTANT_VAR + "_";
        int j = 1;
        boolean someTermContainsTheCurrentFreeIntegrationConstant;
        do {
            someTermContainsTheCurrentFreeIntegrationConstant = false;
            for (Expression expr : exprs) {
                someTermContainsTheCurrentFreeIntegrationConstant = someTermContainsTheCurrentFreeIntegrationConstant || expr.contains(var + j);
            }
            if (someTermContainsTheCurrentFreeIntegrationConstant) {
                j++;
            }
        } while (someTermContainsTheCurrentFreeIntegrationConstant);
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

        // Zunächst: Zerlegen in einfachere DGLen, wenn möglich.
        // Fall: f hat Produktgestalt.
        if (f.isProduct()) {
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            return solveZeroDifferentialEquationIfProduct(factors, varAbsc, varOrd);
        }

        // Fall: f hat Quotientgestalt.
        if (f.isQuotient()) {
//            return solveZeroDifferentialEquationIfQuotient(((BinaryOperation) f).getLeft(), ((BinaryOperation) f).getRight(), varAbsc, varOrd);
        }

        // Fall: f hat Potenzgestalt.
        if (f.isPower()) {
//            return solveZeroDifferentialEquationIfPower(((BinaryOperation) f).getLeft(), ((BinaryOperation) f).getRight(), varAbsc, varOrd);
        }

        // Grundlegendes Kriterium: Ordnung der Differentialgleichung.
        int ord = getOrderOfDifferentialEquation(f, varAbsc, varOrd);

        if (ord == 1) {
            return solveDifferentialEquationOfDegOne(f, varAbsc, varOrd);
        }
        return solveDifferentialEquationOfHigherDeg(f, varAbsc, varOrd);

    }

    private static ExpressionCollection solveZeroDifferentialEquationIfProduct(ExpressionCollection factors, String varAbsc, String varOrd) throws EvaluationException {
        ExpressionCollection solutions = new ExpressionCollection();
        for (Expression factor : factors) {
            solutions = SimplifyUtilities.union(solutions, solveZeroDifferentialEquation(factor, varAbsc, varOrd));
        }
        solutions.removeMultipleTerms();
        return solutions;
    }

    private static ExpressionCollection solveZeroDifferentialEquationIfQuotient(Expression numerator, Expression denominator, String varAbsc, String varOrd) throws EvaluationException {
        ExpressionCollection solutionsLeft = solveZeroDifferentialEquation(numerator, varAbsc, varOrd);
        ExpressionCollection solutionsRight = solveZeroDifferentialEquation(denominator, varAbsc, varOrd);
        ExpressionCollection solutions = SimplifyUtilities.union(solutionsLeft, solutionsRight);
        solutions.removeMultipleTerms();
        return solutions;
    }

    private static ExpressionCollection solveZeroDifferentialEquationIfPower(Expression base, Expression exponent, String varAbsc, String varOrd) throws EvaluationException {
        ExpressionCollection solutionsLeft = solveZeroDifferentialEquation(base, varAbsc, varOrd);
        ExpressionCollection solutionsRight = solveZeroDifferentialEquation(exponent, varAbsc, varOrd);
        ExpressionCollection solutions = SimplifyUtilities.union(solutionsLeft, solutionsRight);
        solutions.removeMultipleTerms();
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

    protected static ExpressionCollection solveDifferentialEquationOfDegOne(Expression f, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutions;

        // Typ: trennbare Veränderliche.
        solutions = solveDifferentialEquationWithSeparableVariables(f, varAbsc, varOrd);
        if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
            return solutions;
        }

        // TO DO.
        return solutions;

    }

    protected static ExpressionCollection solveDifferentialEquationOfHigherDeg(Expression f, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutions;

        try {
            // Typ: y^(n) = f(x).
            solutions = solveDifferentialEquationWithOnlyHighestDerivatives(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException ex) {
        }

        // Typ: Homogene lineare DGL mit konstanten Koeffizienten.
        solutions = solveDifferentialEquationHomogeneousAndLinearWithConstantCoefficients(f, varAbsc, varOrd);
        if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
            return solutions;
        }

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
                solutions = SimplifyUtilities.union(solutions, getSolutionForDifferentialEquationWithSeparableVariables(separatedFactors[0], separatedFactors[1], varAbsc, varOrd, solutions));
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

    private static ExpressionCollection getSolutionForDifferentialEquationWithSeparableVariables(Expression factorWithVarAbsc, Expression factorWithVarOrd, String varAbsc, String varOrd, ExpressionCollection solutionsOfDiffEqAlreadyFound) {

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

        Expression integralOfFactorWithVarAbsc = new Operator(TypeOperator.integral, new Object[]{factorWithVarAbsc, varAbsc}).add(
                Variable.create(getFreeIntegrationConstantVariable(solutionsOfDiffEqAlreadyFound)));
        Expression integralOfReciprocalOfFactorWithVarOrd = new Operator(TypeOperator.integral, new Object[]{ONE.div(factorWithVarOrd), varOrd});

        try {
            ExpressionCollection solutionOfDiffEq = SolveGeneralEquationMethods.solveEquation(integralOfFactorWithVarAbsc, integralOfReciprocalOfFactorWithVarOrd, varOrd);
            solutions.addAll(solutionOfDiffEq);
        } catch (EvaluationException ex) {
        }

        return solutions;

    }

    /*
     Algorithmen zum Lösen spezieller Differentialgleichungstypen allgemeiner Ordnung.
     */
    //Typ: Homogene lineare DGLen mit konstanten Koeffizienten.
    /**
     * Liefert, ob die DGL f(x, y, y', ..., y^(n)) = 0 mit x = varAbsc, y =
     * varOrd eine lineare DGL mit konstanten Koeffizienten ist.
     */
    private static boolean isDifferentialEquationLinearWithConstantCoefficients(Expression f, String varAbsc, String varOrd) {

        int n = getOrderOfDifferentialEquation(f, varAbsc, varOrd);
        String varOrdWithPrimes = varOrd;
        Expression restCoefficient = f;
        for (int i = 0; i <= n; i++) {
            restCoefficient = restCoefficient.replaceVariable(varOrdWithPrimes, ZERO);
            varOrdWithPrimes += "'";
        }

        try {
            f = f.sub(restCoefficient).simplify();
            return isDifferentialEquationHomogeneousAndLinearWithConstantCoefficients(f, varAbsc, varOrd);
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
        if (!(deg.compareTo(BigInteger.ZERO) >= 0 && deg.compareTo(BigInteger.ONE) <= 0)) {
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
     * Liefert Lösungen einer homogenen linearen DGL beliebiger Ordnung.
     */
    private static ExpressionCollection solveDifferentialEquationHomogeneousAndLinearWithConstantCoefficients(Expression f, String varAbsc, String varOrd) throws EvaluationException {

        ExpressionCollection solutionBase = new ExpressionCollection();

        if (!isDifferentialEquationHomogeneousAndLinearWithConstantCoefficients(f, varAbsc, varOrd)) {
            return solutionBase;
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
            solutionBase.addAll(getSolutionForParticularIrredicibleFactor(factor, varAbsc, NotationLoader.SUBSTITUTION_VAR, solutionBase));
        }

        // Aus den Basisvektoren nun die allgemeine Lösung basteln.
        Expression solution = ZERO;
        for (int i = 0; i < solutionBase.getBound(); i++) {
            solution = solution.add(Variable.create(getFreeIntegrationConstantVariable(solution)).mult(solutionBase.get(i)));
        }

        return new ExpressionCollection(solution.simplify());

    }

    private static ExpressionCollection getSolutionForParticularIrredicibleFactor(Expression factor, String varAbsc, String varInCharPolynomial, ExpressionCollection solutionsAlreadyFound) {

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

        String varOrdWithPrimes = varOrd;
        for (int i = 0; i < ord; i++) {
            varOrdWithPrimes += "'";
        }

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
                        solveDifferentialEquationWithOnlyHighestDerivatives(solutionForHighestDerivative, ord, varAbsc, varOrd, solutions));
            }
        }

        solutions.removeMultipleTerms();
        return solutions;

    }

    private static ExpressionCollection solveDifferentialEquationWithOnlyHighestDerivatives(Expression f, int ord, String varAbsc, String varOrd, ExpressionCollection solutionsAlreadyFound) throws DifferentialEquationNotAlgebraicallyIntegrableException {

        Expression solution = f;

        // Rechte Seite f ord-mal integrieren.
        for (int i = 0; i < ord; i++) {
            solution = new Operator(TypeOperator.integral, new Object[]{solution, varAbsc});
        }

        for (int i = 0; i < ord; i++) {
            String intConstant = getFreeIntegrationConstantVariable(solution);
            solution = solution.add(Variable.create(intConstant).mult(Variable.create(varAbsc).pow(i)));
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

        String varOrdWithPrimes = varOrd;
        for (int i = 0; i < ord; i++) {
            varOrdWithPrimes += "'";
        }

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
            isEquationOfProperType = doesExpressionContainOnlyDerivativesOfGivenOrder(f, varOrd, ord, ord - 2) && !f.contains(varAbsc);
            if (isEquationOfProperType) {
                solutions = SimplifyUtilities.union(solutions,
                        solveDifferentialEquationWithOnlyTwoDifferentDerivatives(solutionForHighestDerivative, ord, varAbsc, varOrd, solutions));
            }
        }

        solutions.removeMultipleTerms();
        return solutions;

    }

    private static ExpressionCollection solveDifferentialEquationWithOnlyTwoDifferentDerivatives(Expression rightSide, int ord, String varAbsc, String varOrd, ExpressionCollection solutionsAlreadyFound) {

        ExpressionCollection solutions = new ExpressionCollection();

        /*
        Lösungsalgorithmus: Sei y^(n + 2) = f(y^(n)).
        Dann: 1. y^(n + 1) = +-(2*g(y^(n)) + C_1), g = int(f(t), t).
        2. h_(C_1)(y^(n)) = +-(x + C_2), h(t) = int(1/(2 * g(t)), t).
        3. y = h^(-1)_(C_1)(+-(x + C_2)) + C_3 + C_4 * x + ... + C_(n + 2) * x^(n - 1).
         */
        return solutions;

    }

}
