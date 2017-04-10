package abstractexpressions.expression.differentialequation;

import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.TWO;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.equation.SolveGeneralEquationUtils;
import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.basic.SimplifyMultiPolynomialUtils;
import abstractexpressions.expression.basic.SimplifyPolynomialUtils;
import abstractexpressions.expression.basic.SimplifyUtilities;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.PI;
import static abstractexpressions.expression.classes.Expression.TEN;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.TypeFunction;
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

public abstract class SolveGeneralDifferentialEquationUtils {

    /**
     * Konstanten, die aussagen, ob bei einer Gleichung keine Lösungen gefunden
     * werden konnten oder ob die Gleichung definitiv alle reellen Zahlen als
     * Lösungen besitzt.
     */
    public static final ExpressionCollection ALL_FUNCTIONS = new ExpressionCollection();
    public static final ExpressionCollection NO_SOLUTIONS = new ExpressionCollection();

    private static int indexForNextIntegrationConstant = 1;

    protected static final HashSet<TypeSimplify> simplifyTypesDifferentialEquation = new HashSet<>();
    protected static final HashSet<TypeSimplify> simplifyTypesSolutionOfDifferentialEquation = new HashSet<>();

    static {
        simplifyTypesDifferentialEquation.add(TypeSimplify.order_difference_and_division);
        simplifyTypesDifferentialEquation.add(TypeSimplify.order_sums_and_products);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_basic);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_collect_products);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_factorize);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_functional_relations);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypesDifferentialEquation.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);

        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.order_difference_and_division);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.order_sums_and_products);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_basic);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_collect_products);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_factorize);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_functional_relations);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_expand_logarithms);
        simplifyTypesSolutionOfDifferentialEquation.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
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

    private static void resetIndexForIntegrationConstantVariable() {
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
        ExpressionCollection solutions = solveGeneralDifferentialEquation(f, g, varAbsc, varOrd);
        return reindexFreeIntegrationConstantsInSolutions(solutions);
    }

    /**
     * Gibt eine ExpressionCollection zurück, in der die Elemente denen von
     * solutions entsprechen, bis auf die Indizes der freien Konstanten. Die
     * neuen Indizes lassen keinen Index mehr aus.<br>
     * Beispiel: Ist solutions = [C_1 + C_4*x + C_9*x^2, c_4 + C_15*sin(x)], so
     * wird [C_1 + C_2*x + C_3*x^2, C_2 + C_4*sin(x)] zurückgegeben. Diese
     * Methode dient dazu, dass die endgültigen Lösungen eine "natürlichere"
     * Struktur bekommen.
     */
    private static ExpressionCollection reindexFreeIntegrationConstantsInSolutions(ExpressionCollection solutions) {

        ArrayList<Variable> freeIntegrationConstants = getListOfFreeIntegrationConstants(solutions);
        int oldIndex = 1;

        for (int i = 0; i < freeIntegrationConstants.size(); i++) {
            while (!solutions.contains(NotationLoader.FREE_INTEGRATION_CONSTANT_VAR + "_" + oldIndex)) {
                oldIndex++;
            }
            for (int j = 0; j < solutions.getBound(); j++) {
                solutions.put(j, solutions.get(j).replaceVariable(NotationLoader.FREE_INTEGRATION_CONSTANT_VAR + "_" + oldIndex,
                        Variable.create(NotationLoader.FREE_INTEGRATION_CONSTANT_VAR + "_" + (i + 1))));
            }
            oldIndex++;
        }

        return solutions;

    }

    /**
     * Kürzt gleiche Summanden auf beiden Seiten.
     */
    private static Expression[] cancelEqualSummandsInDifferentialEquation(Expression f, Expression g) {

        ExpressionCollection summandsLeftF = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRightF = SimplifyUtilities.getSummandsRightInExpression(f);
        ExpressionCollection summandsLeftG = SimplifyUtilities.getSummandsLeftInExpression(g);
        ExpressionCollection summandsRightG = SimplifyUtilities.getSummandsRightInExpression(g);

        // Gleiche Summanden in Minuenden beseitigen.
        Expression summandF, summandG;
        for (int i = 0; i < summandsLeftF.getBound(); i++) {
            summandF = summandsLeftF.get(i);
            for (int j = 0; j < summandsLeftG.getBound(); j++) {
                if (summandsLeftG.get(j) != null) {
                    summandG = summandsLeftG.get(j);
                    if (summandF.equivalent(summandG)) {
                        summandsLeftF.remove(i);
                        summandsLeftG.remove(j);
                        break;
                    }
                }
            }
        }
        // Gleiche Summanden in Subtrahenden beseitigen.
        for (int i = 0; i < summandsRightF.getBound(); i++) {
            summandF = summandsRightF.get(i);
            for (int j = 0; j < summandsRightG.getBound(); j++) {
                if (summandsRightG.get(j) != null) {
                    summandG = summandsRightG.get(j);
                    if (summandF.equivalent(summandG)) {
                        summandsRightF.remove(i);
                        summandsRightG.remove(j);
                        break;
                    }
                }
            }
        }

        Expression[] result = new Expression[2];
        result[0] = SimplifyUtilities.produceDifference(summandsLeftF, summandsRightF);
        result[1] = SimplifyUtilities.produceDifference(summandsLeftG, summandsRightG);
        return result;

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
        Expression[] F;
        F = cancelEqualSummandsInDifferentialEquation(f, g);
        f = F[0];
        g = F[1];

        // Falls die Gleichung eine Potenzgleichung darstellt.
        try {
            return solvePowerDifferentialEquation(f, g, varAbsc, varOrd);
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        // Falls die Gleichung eine Funktionsgleichung darstellt.
        try {
            return solveFunctionDifferentialEquation(f, g, varAbsc, varOrd);
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        // Falls f und g einen gemeinsamen Faktor h im Zähler besitzen.
//        try {
//            return solveEquationWithCommonFactors(f, g, var);
//        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
//        }
        Expression diffEq = f.sub(g);
        /* 
        Zunächst: DGL im Folgenden Sinne reduzieren: Hat die DGL die Form
        f(y^(n), ..., y^(k), x) = 0, so wird die DGL 
        f(z^(n - k), ..., z', z, x) = 0 nach z und anschließend die 
        DGL y^(k) = z(x) nach y gelöst.
         */
        Object[] reductionOfDifferentialEquation = reduceDifferentialEquation(diffEq, varOrd);

        // Reduzierte DGL lösen.
        ExpressionCollection solutionsOfReducedDifferentialEquation = solveZeroDifferentialEquation((Expression) reductionOfDifferentialEquation[1], varAbsc, varOrd);

        // Lösungen aus den Lösungen der reduzierten DGL extrahieren.
        return getSolutionsFromSolutionsOfReducedDifferentialEquation((int) reductionOfDifferentialEquation[0], varAbsc, varOrd, solutionsOfReducedDifferentialEquation);

    }

    private static boolean doesNotContainDifferentialEquationVars(Expression f, String varAbsc, String varOrd) {
        HashSet<String> vars = f.getContainedIndeterminates();
        for (String var : vars) {
            if (var.equals(varAbsc)) {
                return false;
            }
            boolean isDerivativeOfVarOrd = var.startsWith(varOrd);
            for (int i = varOrd.length(); i < var.length(); i++) {
                isDerivativeOfVarOrd = isDerivativeOfVarOrd && var.substring(i, i + 1).equals("'");
            }
            if (isDerivativeOfVarOrd) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAdmissibleExponent(Expression expr) {
        return expr.isOddIntegerConstant()
                || expr.isRationalConstant() && ((BinaryOperation) expr).getLeft().isOddIntegerConstant()
                && ((BinaryOperation) expr).getRight().isOddIntegerConstant();
    }

    /**
     * Prozedur zum Finden spezieller Lösungen von Gleichungen der Form f(x, y,
     * ..., y^(n))^p(x, y, ..., y^(n)) = g(x, y, ..., y^(n))^q(x, y, ...,
     * y^(n)).
     *
     * @throws EvaluationException
     */
    private static ExpressionCollection solvePowerDifferentialEquation(Expression f, Expression g, String varAbsc, String varOrd) throws EvaluationException, DifferentialEquationNotAlgebraicallyIntegrableException {

        if (f.isPower() && doesNotContainDifferentialEquationVars(((BinaryOperation) f).getRight(), varAbsc, varOrd)
                && doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {
            Expression expF = ((BinaryOperation) f).getRight();
            if (isAdmissibleExponent(expF)) {
                return solveGeneralDifferentialEquation(((BinaryOperation) f).getLeft(), g.pow(ONE.div(expF)), varAbsc, varOrd);
            }
        }

        if (g.isPower() && doesNotContainDifferentialEquationVars(((BinaryOperation) g).getRight(), varAbsc, varOrd)
                && doesNotContainDifferentialEquationVars(f, varAbsc, varOrd)) {
            return solvePowerDifferentialEquation(g, f, varAbsc, varOrd);
        }

        if (f.isPower() && g.isPower()
                && ((BinaryOperation) f).getRight().equivalent(((BinaryOperation) g).getRight())
                && doesNotContainDifferentialEquationVars(((BinaryOperation) f).getRight(), varAbsc, varOrd)
                && doesNotContainDifferentialEquationVars(((BinaryOperation) g).getRight(), varAbsc, varOrd)) {

            Expression exp = ((BinaryOperation) f).getRight();
            if (isAdmissibleExponent(exp)) {
                return solveGeneralDifferentialEquation(((BinaryOperation) f).getLeft(), ((BinaryOperation) g).getLeft(), varAbsc, varOrd);
            }
            if (!exp.isIntegerConstantOrRationalConstant()) {
                return solveGeneralDifferentialEquation(((BinaryOperation) f).getLeft(), ((BinaryOperation) g).getLeft(), varAbsc, varOrd);
            }
            ExpressionCollection solutionsOne = solveGeneralDifferentialEquation(((BinaryOperation) f).getLeft(), ((BinaryOperation) g).getLeft(), varAbsc, varOrd);
            ExpressionCollection solutionsTwo = solveGeneralDifferentialEquation(((BinaryOperation) f).getLeft(), MINUS_ONE.mult(((BinaryOperation) g).getLeft()), varAbsc, varOrd);
            return SimplifyUtilities.union(solutionsOne, solutionsTwo);

        }

        throw new DifferentialEquationNotAlgebraicallyIntegrableException();

    }

    /**
     * Prozedur zum Lösen von Differentialgleichungen der Form F(f(x, y, ...,
     * y^(n))) = F(g(x, y, ..., y^(n))) oder F(f(x, y, ..., y^(n))) = const,
     * varAbsc = x, varOrd = y.
     */
    private static ExpressionCollection solveFunctionDifferentialEquation(Expression f, Expression g, String varAbsc, String varOrd)
            throws EvaluationException, DifferentialEquationNotAlgebraicallyIntegrableException {

        if (g.isFunction() && !doesNotContainDifferentialEquationVars(((Function) g).getLeft(), varAbsc, varOrd)
                && doesNotContainDifferentialEquationVars(f, varAbsc, varOrd)) {
            return solveFunctionDifferentialEquation(g, f, varAbsc, varOrd);
        }

        if (doesNotContainDifferentialEquationVars(f, varAbsc, varOrd) || !(f instanceof Function)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        TypeFunction type = ((Function) f).getType();

        if (!g.isFunction(type) && !doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {
            throw new DifferentialEquationNotAlgebraicallyIntegrableException();
        }

        switch (type) {
            case abs:
                return solveDifferentialEquationAbs(((Function) f).getLeft(), g, varAbsc, varOrd);
            case sgn:
                return solveDifferentialEquationSgn(((Function) f).getLeft(), g, varAbsc, varOrd);
            case exp:
                return solveDifferentialEquationExp(((Function) f).getLeft(), g, varAbsc, varOrd);
            case lg:
                return solveDifferentialEquationLg(((Function) f).getLeft(), g, varAbsc, varOrd);
            case ln:
                return solveDifferentialEquationLn(((Function) f).getLeft(), g, varAbsc, varOrd);
            case sin:
                return solveDifferentialEquationSin(((Function) f).getLeft(), g, varAbsc, varOrd);
            case cos:
                return solveDifferentialEquationCos(((Function) f).getLeft(), g, varAbsc, varOrd);
            case tan:
                return solveDifferentialEquationTan(((Function) f).getLeft(), g, varAbsc, varOrd);
            case cot:
                return solveDifferentialEquationCot(((Function) f).getLeft(), g, varAbsc, varOrd);
            case sec:
                return solveDifferentialEquationSec(((Function) f).getLeft(), g, varAbsc, varOrd);
            case cosec:
                return solveDifferentialEquationCosec(((Function) f).getLeft(), g, varAbsc, varOrd);
            case cosh:
                return solveDifferentialEquationCosh(((Function) f).getLeft(), g, varAbsc, varOrd);
            case sech:
                return solveDifferentialEquationSech(((Function) f).getLeft(), g, varAbsc, varOrd);
            default:
                // Ansonsten ist f eine bijektive Funktion
                return solveDifferentialEquationWithBijectiveFunction(((Function) f).getLeft(), type, g, varAbsc, varOrd);
        }

    }

    /**
     * Methode zum Lösen einer Betragsdifferentialgleichung |argument| = g..
     */
    private static ExpressionCollection solveDifferentialEquationAbs(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();
        ExpressionCollection solutionsPositive, solutionsNegative;

        if (g.isFunction(TypeFunction.abs)) {
            try {
                solutionsPositive = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft(), varAbsc, varOrd);
                solutionsNegative = solveGeneralDifferentialEquation(argument, Expression.MINUS_ONE.mult(((Function) g).getLeft()), varAbsc, varOrd);
                solutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {
            if (g.isNonPositive() && !g.equals(ZERO)) {
                // Gleichung ist unlösbar. 
                return NO_SOLUTIONS;
            }
            try {
                solutionsPositive = solveGeneralDifferentialEquation(argument, g, varAbsc, varOrd);
                solutionsNegative = solveGeneralDifferentialEquation(argument, Expression.MINUS_ONE.mult(g), varAbsc, varOrd);
                if (!solutionsPositive.isEmpty() || !solutionsNegative.isEmpty()) {
                    solutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        }

        return solutions;

    }

    /**
     * Methode zum Lösen einer DGL der Form sgn(argument) = g.
     */
    private static ExpressionCollection solveDifferentialEquationSgn(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();

        if (g.isConstant() && !g.equals(ZERO)) {
            // DGL ist entweder unlösbar oder kann nicht explizit gelöst werden.
            return NO_SOLUTIONS;
        }
        // Man kann zumindest über den Spezialfall g = 0 etwas aussagen: sgn(f) = 0 <=> f = 0.
        if (g.equals(ZERO)) {
            try {
                solutions = solveGeneralDifferentialEquation(argument, ZERO, varAbsc, varOrd);
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        }

        return solutions;

    }

    /**
     * Methode zum Lösen einer DGL der Form exp(argument) = g.
     */
    private static ExpressionCollection solveDifferentialEquationExp(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();

        if (g.isFunction(TypeFunction.exp)) {
            try {
                solutions = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft(), varAbsc, varOrd);
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {
            if (g.isNonPositive()) {
                // Gleichung ist unlösbar. 
                return NO_SOLUTIONS;
            }
            try {
                solutions = solveGeneralDifferentialEquation(argument, g.ln(), varAbsc, varOrd);
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        }

        return solutions;

    }

    /**
     * Methode zum Lösen einer DGL der Form lg(argument) = g.
     */
    private static ExpressionCollection solveDifferentialEquationLg(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();

        if (g.isFunction(TypeFunction.lg)) {
            try {
                solutions = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft(), varAbsc, varOrd);
                if (solutions == NO_SOLUTIONS) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {
            if (argument.isAlwaysNonPositive()) {
                // Gleichung ist unlösbar. 
                return NO_SOLUTIONS;
            }
            try {
                solutions = solveGeneralDifferentialEquation(argument, TEN.pow(g), varAbsc, varOrd);
                if (solutions == NO_SOLUTIONS) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        }

        return solutions;

    }

    /**
     * Methode zum Lösen einer DGL der Form ln(argument) = g.
     */
    private static ExpressionCollection solveDifferentialEquationLn(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();

        if (g.isFunction(TypeFunction.ln)) {
            try {
                solutions = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft(), varAbsc, varOrd);
                if (solutions == NO_SOLUTIONS) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {
            if (argument.isAlwaysNonPositive()) {
                // Gleichung ist unlösbar. 
                return NO_SOLUTIONS;
            }
            try {
                solutions = solveGeneralDifferentialEquation(argument, g.exp(), varAbsc, varOrd);
                if (solutions == NO_SOLUTIONS) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        }

        return solutions;

    }

    /**
     * Methode zum Lösen einer DGL der Form cosh(argument) = g.
     */
    private static ExpressionCollection solveDifferentialEquationCosh(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();
        ExpressionCollection possibleSolutions;
        ExpressionCollection solutionsPositive;
        ExpressionCollection solutionsNegative;

        // Lösungsfamilien erzeugen!
        if (g.isFunction(TypeFunction.cosh)) {

            try {
                solutionsPositive = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft(), varAbsc, varOrd);
                solutionsNegative = solveGeneralDifferentialEquation(argument, MINUS_ONE.mult(((Function) g).getLeft()), varAbsc, varOrd);
                possibleSolutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {

            try {
                solutionsPositive = solveGeneralDifferentialEquation(argument, g.arcosh(), varAbsc, varOrd);
                solutionsNegative = solveGeneralDifferentialEquation(argument, MINUS_ONE.mult(g.arcosh()), varAbsc, varOrd);
                possibleSolutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return solutions;

    }

    /**
     * Methode zum Lösen einer DGL der Form sech(argument) = g.
     */
    private static ExpressionCollection solveDifferentialEquationSech(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();
        ExpressionCollection possibleSolutions;
        ExpressionCollection solutionsPositive;
        ExpressionCollection solutionsNegative;

        // Lösungsfamilien erzeugen!
        if (g.isFunction(TypeFunction.sech)) {

            try {
                solutionsPositive = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft(), varAbsc, varOrd);
                solutionsNegative = solveGeneralDifferentialEquation(argument, MINUS_ONE.mult(((Function) g).getLeft()), varAbsc, varOrd);
                possibleSolutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {

            try {
                solutionsPositive = solveGeneralDifferentialEquation(argument, g.arsech(), varAbsc, varOrd);
                solutionsNegative = solveGeneralDifferentialEquation(argument, MINUS_ONE.mult(g.arsech()), varAbsc, varOrd);
                possibleSolutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return solutions;

    }

    /**
     * Methode zum Lösen einer DGL der Form sin(argument) = g.
     */
    private static ExpressionCollection solveDifferentialEquationSin(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();
        ExpressionCollection possibleSolutions;
        ExpressionCollection solutionsPositive;
        ExpressionCollection solutionsNegative = new ExpressionCollection();
        String K = getParameterVariable(g);

        // Lösungsfamilien erzeugen!
        if (g.isFunction(TypeFunction.sin)) {

            try {
                solutionsPositive = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft().add(TWO.mult(PI.mult(Variable.create(K)))), varAbsc, varOrd);
                solutionsNegative = solveGeneralDifferentialEquation(argument, ONE.add(TWO.mult(Variable.create(K))).mult(PI).sub(((Function) g).getLeft()), varAbsc, varOrd);
                possibleSolutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {

            Function gComposedWithInverse = new Function(g, TypeFunction.arcsin);
            try {
                if (g.equals(ONE) || g.equals(Expression.MINUS_ONE)) {
                    solutionsPositive = solveGeneralDifferentialEquation(argument, gComposedWithInverse.add(TWO.mult(PI.mult(Variable.create(K)))), varAbsc, varOrd);
                } else if (g.equals(ZERO)) {
                    solutionsPositive = solveGeneralDifferentialEquation(argument, PI.mult(Variable.create(K)), varAbsc, varOrd);
                } else {
                    solutionsPositive = solveGeneralDifferentialEquation(argument, gComposedWithInverse.add((TWO).mult(PI.mult(Variable.create(K)))), varAbsc, varOrd);
                    solutionsNegative = solveGeneralDifferentialEquation(argument, ONE.add(TWO.mult(Variable.create(K))).mult(PI).sub(gComposedWithInverse), varAbsc, varOrd);
                }
                possibleSolutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return solutions;

    }

    /**
     * Methode zum Lösen einer DGL der Form cos(argument) = g.
     */
    private static ExpressionCollection solveDifferentialEquationCos(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();
        ExpressionCollection possibleSolutions;
        ExpressionCollection solutionsPositive;
        ExpressionCollection solutionsNegative = new ExpressionCollection();
        String K = getParameterVariable(g);

        // Lösungsfamilien erzeugen!
        if (g.isFunction(TypeFunction.cos)) {

            try {
                solutionsPositive = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft().add(TWO.mult(PI.mult(Variable.create(K)))), varAbsc, varOrd);
                solutionsNegative = solveGeneralDifferentialEquation(argument, TWO.mult(PI.mult(Variable.create(K))).sub(((Function) g).getLeft()), varAbsc, varOrd);
                possibleSolutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {

            Function gComposedWithInverse = new Function(g, TypeFunction.arccos);
            try {
                if (g.equals(ONE) || g.equals(Expression.MINUS_ONE)) {
                    solutionsPositive = solveGeneralDifferentialEquation(argument, gComposedWithInverse.add(TWO.mult(PI.mult(Variable.create(K)))), varAbsc, varOrd);
                } else if (g.equals(ZERO)) {
                    solutionsPositive = solveGeneralDifferentialEquation(argument, gComposedWithInverse.add(PI.mult(Variable.create(K))), varAbsc, varOrd);
                } else {
                    solutionsPositive = solveGeneralDifferentialEquation(argument, gComposedWithInverse.add(TWO.mult(PI.mult(Variable.create(K)))), varAbsc, varOrd);
                    solutionsNegative = solveGeneralDifferentialEquation(argument, TWO.mult(PI.mult(Variable.create(K))).sub(gComposedWithInverse), varAbsc, varOrd);
                }
                possibleSolutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return solutions;

    }

    /**
     * Methode zum Lösen einer DGL der Form tan(argument) = g.
     */
    private static ExpressionCollection solveDifferentialEquationTan(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();
        ExpressionCollection possibleSolutions;
        String K = getParameterVariable(g);

        // Lösungsfamilien erzeugen!
        if (g.isFunction(TypeFunction.tan)) {

            try {
                possibleSolutions = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft().add(PI.mult(Variable.create(K))), varAbsc, varOrd);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {

            Function gComposedWithInverse = new Function(g, TypeFunction.arctan);
            try {
                possibleSolutions = solveGeneralDifferentialEquation(argument, gComposedWithInverse.add(PI.mult(Variable.create(K))), varAbsc, varOrd);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return solutions;

    }

    /**
     * Methode zum Lösen einer DGL der Form cot(argument) = g.
     */
    private static ExpressionCollection solveDifferentialEquationCot(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();
        ExpressionCollection possibleSolutions;
        String K = getParameterVariable(g);

        // Lösungsfamilien erzeugen!
        if (g.isFunction(TypeFunction.cot)) {

            try {
                possibleSolutions = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft().add(PI.mult(Variable.create(K))), varAbsc, varOrd);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {

            Function gComposedWithInverse = new Function(g, TypeFunction.arccot);
            try {
                possibleSolutions = solveGeneralDifferentialEquation(argument, gComposedWithInverse.add(PI.mult(Variable.create(K))), varAbsc, varOrd);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return solutions;

    }

    /**
     * Methode zum Lösen einer Gleichung der Form sec(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveDifferentialEquationSec(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();
        ExpressionCollection possibleSolutions;
        ExpressionCollection solutionsPositive;
        ExpressionCollection solutionsNegative = new ExpressionCollection();
        String K = getParameterVariable(g);

        // Lösungsfamilien erzeugen!
        if (g.isFunction(TypeFunction.sec)) {

            try {
                solutionsPositive = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft().add(TWO.mult(PI.mult(Variable.create(K)))), varAbsc, varOrd);
                solutionsNegative = solveGeneralDifferentialEquation(argument, TWO.mult(PI.mult(Variable.create(K))).sub(((Function) g).getLeft()), varAbsc, varOrd);
                possibleSolutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {

            Function gComposedWithInverse = new Function(g, TypeFunction.arcsec);
            try {
                if (g.equals(ONE) || g.equals(MINUS_ONE)) {
                    solutionsPositive = solveGeneralDifferentialEquation(argument, gComposedWithInverse.add(TWO.mult(PI.mult(Variable.create(K)))), varAbsc, varOrd);
                } else {
                    solutionsPositive = solveGeneralDifferentialEquation(argument, gComposedWithInverse.add(TWO.mult(PI.mult(Variable.create(K)))), varAbsc, varOrd);
                    solutionsNegative = solveGeneralDifferentialEquation(argument, TWO.mult(PI.mult(Variable.create(K))).sub(gComposedWithInverse), varAbsc, varOrd);
                }
                possibleSolutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return solutions;

    }

    /**
     * Methode zum Lösen einer Gleichung der Form cosec(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveDifferentialEquationCosec(Expression argument, Expression g, String varAbsc, String varOrd) {

        ExpressionCollection solutions = new ExpressionCollection();
        ExpressionCollection possibleSolutions;
        ExpressionCollection solutionsPositive;
        ExpressionCollection solutionsNegative = new ExpressionCollection();
        String K = getParameterVariable(g);

        // Lösungsfamilien erzeugen!
        if (g.isFunction(TypeFunction.cosec)) {

            try {
                solutionsPositive = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft().add(TWO.mult(PI.mult(Variable.create(K)))), varAbsc, varOrd);
                solutionsNegative = solveGeneralDifferentialEquation(argument, ONE.add(TWO.mult(Variable.create(K))).mult(PI).sub(((Function) g).getLeft()), varAbsc, varOrd);
                possibleSolutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {

            Function gComposedWithInverse = new Function(g, TypeFunction.arccosec);
            try {
                if (g.equals(ONE) || g.equals(MINUS_ONE)) {
                    solutionsPositive = solveGeneralDifferentialEquation(argument, gComposedWithInverse.add(TWO.mult(PI.mult(Variable.create(K)))), varAbsc, varOrd);
                } else {
                    solutionsPositive = solveGeneralDifferentialEquation(argument, gComposedWithInverse.add(TWO.mult(PI.mult(Variable.create(K)))), varAbsc, varOrd);
                    solutionsNegative = solveGeneralDifferentialEquation(argument, ONE.add(TWO.mult(Variable.create(K))).mult(PI).sub(gComposedWithInverse), varAbsc, varOrd);
                }
                possibleSolutions = SimplifyUtilities.union(solutionsPositive, solutionsNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleSolutions.getBound(); i++) {
                    try {
                        possibleSolutions.put(i, possibleSolutions.get(i).simplify());
                        solutions.add(possibleSolutions.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (solutions.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return solutions;

    }

    /**
     * In f sind Variablen enthalten, unter anderem "Parametervariablen" K_1,
     * K_2, .... Diese Funktion liefert dasjenige K_i, welches in h noch nicht
     * vorkommt.
     */
    private static String getParameterVariable(Expression f) {
        String var = NotationLoader.FREE_INTEGER_PARAMETER_VAR + "_";
        int j = 1;
        while (f.contains(var + j)) {
            j++;
        }
        return var + j;
    }

    /**
     * Methode zum Lösen einer Differentialgleichung f(x, y, ..., y^(n)) = g(x,
     * y, ..., y^(n)) mit bijektivem f.
     */
    private static ExpressionCollection solveDifferentialEquationWithBijectiveFunction(Expression argument, TypeFunction type,
            Expression g, String varAbsc, String varOrd) {

        TypeFunction inverseType;

        switch (type) {
            case sinh:
                inverseType = TypeFunction.arsinh;
                break;
            case tanh:
                inverseType = TypeFunction.artanh;
                break;
            case coth:
                inverseType = TypeFunction.arcoth;
                break;
            case cosech:
                inverseType = TypeFunction.arcosech;
                break;
            case arcsin:
                inverseType = TypeFunction.sin;
                break;
            case arccos:
                inverseType = TypeFunction.cos;
                break;
            case arctan:
                inverseType = TypeFunction.tan;
                break;
            case arccot:
                inverseType = TypeFunction.cot;
                break;
            case arcsec:
                inverseType = TypeFunction.sec;
                break;
            case arccosec:
                inverseType = TypeFunction.cosec;
                break;
            case arsinh:
                inverseType = TypeFunction.sinh;
                break;
            case arcosh:
                inverseType = TypeFunction.cosh;
                break;
            case artanh:
                inverseType = TypeFunction.tanh;
                break;
            case arcoth:
                inverseType = TypeFunction.coth;
                break;
            case arsech:
                inverseType = TypeFunction.sech;
                break;
            default:
                // Hier ist type == arccosech.
                inverseType = TypeFunction.cosech;
                break;
        }

        ExpressionCollection solutions = new ExpressionCollection();

        if (g.isFunction(type)) {
            try {
                solutions = solveGeneralDifferentialEquation(argument, ((Function) g).getLeft(), varAbsc, varOrd);
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        } else if (doesNotContainDifferentialEquationVars(g, varAbsc, varOrd)) {
            try {
                solutions = solveGeneralDifferentialEquation(argument, new Function(g, inverseType), varAbsc, varOrd);
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        }

        return solutions;

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
            solutions = SolveSpecialDifferentialEquationUtils.solveExactDifferentialEquation(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        // Typ: m*a(x, y) + m*b(x, y)*y' = 0 ist exakt für einen integrierenden Faktor m.
        try {
            solutions = SolveSpecialDifferentialEquationUtils.solveExactDifferentialEquationWithIngeratingFactor(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        // Typ: Bernoulli-DGL a(x)*y' + b(x)*y + c(x)*y^n = 0, n != 0, 1.
        try {
            solutions = SolveSpecialDifferentialEquationUtils.solveBernoulliDifferentialEquation(f, varAbsc, varOrd);
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
            solutions = SolveSpecialDifferentialEquationUtils.solveDifferentialEquationWithSecondDerivativeAndVarOrd(f, varAbsc, varOrd);
            if (!solutions.isEmpty() && solutions != NO_SOLUTIONS) {
                return solutions;
            }
        } catch (DifferentialEquationNotAlgebraicallyIntegrableException e) {
        }

        try {
            // Typ: f(y'', y', y) = 0.
            solutions = SolveSpecialDifferentialEquationUtils.solveDifferentialEquationOfOrderTwoWithoutVarAbsc(f, varAbsc, varOrd);
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

        ExpressionCollection solutionsForDerivative = SolveGeneralEquationUtils.solveEquation(f, ZERO, varOrd + "'");

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
            ExpressionCollection constantZeros = SolveGeneralEquationUtils.solveEquation(factorWithVarOrd, ZERO, varOrd);
            solutions.addAll(constantZeros.simplify(simplifyTypesSolutionOfDifferentialEquation));
            if (constantZeros.isEmpty() && constantZeros != SolveGeneralEquationUtils.NO_SOLUTIONS) {
                // Implizit gegebene (konstante) Lösungen hinzufügen.
                solutions.add(factorWithVarOrd.simplify(simplifyTypesSolutionOfDifferentialEquation));
            }
        } catch (EvaluationException e) {
        }

        Expression integralOfFactorWithVarAbsc = new Operator(TypeOperator.integral, new Object[]{factorWithVarAbsc, varAbsc}).add(
                getFreeIntegrationConstantVariable());
        Expression integralOfReciprocalOfFactorWithVarOrd = new Operator(TypeOperator.integral, new Object[]{ONE.div(factorWithVarOrd), varOrd});

        try {
            ExpressionCollection solutionOfDiffEq = SolveGeneralEquationUtils.solveEquation(integralOfFactorWithVarAbsc, integralOfReciprocalOfFactorWithVarOrd, varOrd);
            solutions.addAll(solutionOfDiffEq.simplify(simplifyTypesSolutionOfDifferentialEquation));
            if (solutionOfDiffEq.isEmpty() && solutionOfDiffEq != SolveGeneralEquationUtils.NO_SOLUTIONS) {
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
        if (SimplifyMultiPolynomialUtils.getDegreeOfMultiPolynomial(fSubstituted, NotationLoader.SUBSTITUTION_VAR + "_0", NotationLoader.SUBSTITUTION_VAR + "_1").compareTo(BigInteger.ONE) != 0) {
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

        BigInteger deg = SimplifyMultiPolynomialUtils.getDegreeOfMultiPolynomial(f, vars);
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

        BigInteger deg = SimplifyMultiPolynomialUtils.getDegreeOfMultiPolynomial(f, vars);
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

        Expression charPolynomial = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficients, NotationLoader.SUBSTITUTION_VAR);

        // Charakteristisches Polynom versuchen, vollständig zu faktorisieren.
        charPolynomial = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(charPolynomial, NotationLoader.SUBSTITUTION_VAR);
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

        BigInteger deg = SimplifyPolynomialUtils.getDegreeOfPolynomial(base, varInCharPolynomial);
        ExpressionCollection zeros;

        if (deg.equals(BigInteger.ONE)) {
            // Fall: Potenz eines Linearfaktors.
            try {
                zeros = SolveGeneralEquationUtils.solveEquation(base, ZERO, varInCharPolynomial);
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
                ExpressionCollection coefficients = SimplifyPolynomialUtils.getPolynomialCoefficients(base, varInCharPolynomial);
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

        BigInteger deg = SimplifyPolynomialUtils.getDegreeOfPolynomial(base, varInCharPolynomial);
        ExpressionCollection zeros;

        if (deg.equals(BigInteger.ONE)) {
            // Fall: Potenz eines Linearfaktors.
            try {
                zeros = SolveGeneralEquationUtils.solveEquation(base, ZERO, varInCharPolynomial);
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
                ExpressionCollection coefficients = SimplifyPolynomialUtils.getPolynomialCoefficients(base, varInCharPolynomial);
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
        charPolynomial = SimplifyPolynomialUtils.decomposePolynomialInIrreducibleFactors(charPolynomial, NotationLoader.SUBSTITUTION_VAR);
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
            solutionsForHighestDerivative = SolveGeneralEquationUtils.solveEquation(f, ZERO, varOrdWithPrimes);
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
