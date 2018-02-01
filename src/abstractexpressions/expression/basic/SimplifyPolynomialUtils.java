package abstractexpressions.expression.basic;

import abstractexpressions.expression.computation.ArithmeticUtils;
import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import exceptions.MathToolException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.PI;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Operator;
import enums.TypeSimplify;
import abstractexpressions.expression.classes.Variable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import abstractexpressions.expression.equation.PolynomialAlgebraUtils;
import abstractexpressions.expression.equation.SolveGeneralSystemOfEquationsUtils;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import exceptions.NotAlgebraicallySolvableException;
import java.math.BigDecimal;
import java.util.List;
import lang.translator.Translator;
import notations.NotationLoader;

public abstract class SimplifyPolynomialUtils {
    
    private static final String SU_PolynomialAlgebraMethods_TOO_HIGH_DEGREE = "SU_PolynomialAlgebraMethods_TOO_HIGH_DEGREE";
    private static final String EB_BinaryOperation_DIVISION_BY_ZERO = "EB_BinaryOperation_DIVISION_BY_ZERO";

    private static final HashSet<TypeSimplify> simplifyTypesDecomposePolynomial = getSimplifyTypesDecomposePolynomial();
    private static final HashSet<TypeSimplify> simplifyTypesExpandPolynomial = getSimplifyTypesExpandPolynomial();
    private static final HashSet<TypeSimplify> simplifyTypesPolynomialDivisionEuklidRepresentation = getSimplifyTypesPolynomialDivisionEuklidRepresentation();

    private static HashSet<TypeSimplify> getSimplifyTypesDecomposePolynomial() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        return simplifyTypes;
    }

    private static HashSet<TypeSimplify> getSimplifyTypesExpandPolynomial() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_expand_powerful);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        return simplifyTypes;
    }

    private static HashSet<TypeSimplify> getSimplifyTypesPolynomialDivisionEuklidRepresentation() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypes.add(TypeSimplify.simplify_factorize);
        simplifyTypes.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypes.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
        return simplifyTypes;
    }

    /**
     * Fehlerklasse für den Fall, dass die Partialbruchzerlegung nicht ermittelt
     * werden konnte.
     */
    public static class PolynomialNotDecomposableException extends MathToolException {

        private static final String NO_EXPLICIT_DECOMPOSITION_MESSAGE = "Polynomial admits no explicit decomposition.";

        public PolynomialNotDecomposableException() {
            super(NO_EXPLICIT_DECOMPOSITION_MESSAGE);
        }

        public PolynomialNotDecomposableException(String s) {
            super(s);
        }

    }

    /**
     * Gibt zurück, ob expr ein Polynom in der Variablen var ist. Voraussetzung:
     * expr ist vereinfacht, d.h. Operatoren etc. kommen NICHT vor (außer evtl.
     * &#915;(x), was kein Polynom ist).
     */
    public static boolean isPolynomial(Expression expr, String var) {
        if (!expr.contains(var)) {
            return true;
        }
        if (expr.equals(Variable.create(var))) {
            return true;
        }
        if (expr.isSum() || expr.isDifference() || expr.isProduct()) {
            return isPolynomial(((BinaryOperation) expr).getLeft(), var) && isPolynomial(((BinaryOperation) expr).getRight(), var);
        }
        if (expr.isQuotient() && !((BinaryOperation) expr).getRight().contains(var)) {
            return isPolynomial(((BinaryOperation) expr).getLeft(), var);
        }
        if (expr.isPower() && ((BinaryOperation) expr).getRight().isIntegerConstant() && ((BinaryOperation) expr).getRight().isNonNegative()) {
            return isPolynomial(((BinaryOperation) expr).getLeft(), var);
        }
        return false;
    }

    /**
     * Gibt zurück, ob expr ein Polynom in der Variablen var ist und ob sein
     * Grad zusätzlich nicht zu hoch ist (aktuell &#8804; 100). Voraussetzung:
     * expr ist vereinfacht, d.h. Operatoren etc. kommen NICHT vor (außer evtl.
     * &#915;(x), was kein Polynom ist).
     */
    public static boolean isPolynomialAdmissibleForComputation(Expression expr, String var) {
        return isPolynomial(expr, var) && getDegreeOfPolynomial(expr, var).compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL)) <= 0;
    }

    /**
     * Gibt zurück, ob expr ein lineares Polynom in der Variablen var ist.
     */
    public static boolean isLinearPolynomial(Expression expr, String var) {
        return isPolynomial(expr, var) && getDegreeOfPolynomial(expr, var).compareTo(BigInteger.ONE) == 0;
    }

    /**
     * Gibt zurück, ob expr ein quadratisches Polynom in der Variablen var ist.
     */
    public static boolean isQuadraticPolynomial(Expression expr, String var) {
        return isPolynomial(expr, var) && getDegreeOfPolynomial(expr, var).compareTo(BigInteger.valueOf(2)) == 0;
    }

    /**
     * Liefert (eine OBERE SCHRANKE für) den Grad des Polynoms, welches von f
     * repräsentiert wird. Falls f kein Polynom ist in var ist, so wird -1 (als
     * BigInteger) zurückgegeben.
     */
    public static BigInteger getDegreeOfPolynomial(Expression f, String var) {
        if (!isPolynomial(f, var)) {
            return BigInteger.valueOf(-1);
        }
        return getPolynomialDegree(f, var);
    }

    private static BigInteger getPolynomialDegree(Expression f, String var) {
        if (!f.contains(var)) {
            return BigInteger.ZERO;
        }
        if (f instanceof Variable) {
            if (((Variable) f).getName().equals(var)) {
                return BigInteger.ONE;
            }
            return BigInteger.ZERO;
        }
        if (f instanceof BinaryOperation) {
            if (f.isSum() || f.isDifference()) {
                return getDegreeOfPolynomial(((BinaryOperation) f).getLeft(), var).max(getDegreeOfPolynomial(((BinaryOperation) f).getRight(), var));
            }
            if (f.isProduct()) {
                return getDegreeOfPolynomial(((BinaryOperation) f).getLeft(), var).add(getDegreeOfPolynomial(((BinaryOperation) f).getRight(), var));
            }
            if (f.isQuotient() && !((BinaryOperation) f).getRight().contains(var)) {
                return getDegreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
            }
            if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant() && ((BinaryOperation) f).getRight().isNonNegative()) {
                BigInteger exp = ((Constant) ((BinaryOperation) f).getRight()).getBigIntValue();
                return getDegreeOfPolynomial(((BinaryOperation) f).getLeft(), var).multiply(exp);
            }
        }
        if (f instanceof Operator) {
            if (f.isConstant()) {
                return BigInteger.ZERO;
            }
        }
        // Dann ist f kein Polynom.
        return BigInteger.valueOf(-1);
    }

    /**
     * Liefert (eine UNTERE SCHRANKE für) die Ordnung des Polynoms, welches von
     * f repräsentiert wird. Falls f kein Polynom ist in var ist, so wird -1
     * (als BigInteger) zurückgegeben.
     */
    public static BigInteger getOrderOfPolynomial(Expression f, String var) {
        if (!isPolynomial(f, var)) {
            return BigInteger.valueOf(-1);
        }
        return getPolynomialOrder(f, var);
    }

    public static BigInteger getPolynomialOrder(Expression f, String var) {
        if (!f.contains(var)) {
            return BigInteger.ZERO;
        }
        if (f instanceof Variable) {
            if (((Variable) f).getName().equals(var)) {
                return BigInteger.ONE;
            }
            return BigInteger.ZERO;
        }
        if (f instanceof BinaryOperation) {
            if (f.isSum() || f.isDifference()) {
                return getOrderOfPolynomial(((BinaryOperation) f).getLeft(), var).min(getOrderOfPolynomial(((BinaryOperation) f).getRight(), var));
            }
            if (f.isProduct()) {
                if (!((BinaryOperation) f).getLeft().contains(var) && !((BinaryOperation) f).getRight().contains(var)) {
                    return BigInteger.ZERO;
                }
                if (((BinaryOperation) f).getLeft().contains(var) && !((BinaryOperation) f).getRight().contains(var)) {
                    return getOrderOfPolynomial(((BinaryOperation) f).getLeft(), var);
                }
                if (!((BinaryOperation) f).getLeft().contains(var) && ((BinaryOperation) f).getRight().contains(var)) {
                    return getOrderOfPolynomial(((BinaryOperation) f).getRight(), var);
                }
                return getOrderOfPolynomial(((BinaryOperation) f).getLeft(), var).add(getOrderOfPolynomial(((BinaryOperation) f).getRight(), var));
            }
            if (f.isQuotient() && !((BinaryOperation) f).getRight().contains(var)) {
                return getOrderOfPolynomial(((BinaryOperation) f).getLeft(), var);
            }
            if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant() && ((BinaryOperation) f).getRight().isNonNegative()) {
                BigInteger exp = ((Constant) ((BinaryOperation) f).getRight()).getBigIntValue();
                return getOrderOfPolynomial(((BinaryOperation) f).getLeft(), var).multiply(exp);
            }
            // Dann ist f kein Polynom
            return BigInteger.valueOf(-1);
        }
        if (f instanceof Operator) {
            if (f.isConstant()) {
                return BigInteger.ZERO;
            }
        }
        // Dann ist f kein Polynom
        return BigInteger.valueOf(-1);
    }

    /**
     * Ermittelt die Koeffizienten, falls f ein Polynom in derivative Variablen
     * var ist. Ist f kein Polynom, so wird eine leere ExpressionCollection
     * zurückgegeben.
     * 
     * @throws EvaluationException
     */
    public static ExpressionCollection getPolynomialCoefficients(Expression f, String var) throws EvaluationException {

        ExpressionCollection coefficients = new ExpressionCollection();

        if (!SimplifyPolynomialUtils.isPolynomial(f, var)) {
            return coefficients;
        }

        BigInteger deg = SimplifyPolynomialUtils.getDegreeOfPolynomial(f, var);
        if (deg.compareTo(BigInteger.ZERO) < 0) {
            return coefficients;
        }
        if (deg.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL)) > 0) {
            throw new EvaluationException(Translator.translateOutputMessage(SU_PolynomialAlgebraMethods_TOO_HIGH_DEGREE));
        }

        f = f.simplify(simplifyTypesExpandPolynomial);

        /* 
         Jetzt ist f ausmultipliziert. Einige Monome können allerdings doppelt vorkommen!
         Jetzt muss man die Koeffizienten passend sammeln.
         */
        for (int i = 0; i <= deg.intValue(); i++) {
            coefficients.put(i, ZERO);
        }

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(f);
        ExpressionCollection factorsNumerator, factorsDenominator;
        Expression currentCoefficient;
        int exponent;

        // Eigentliche Koeffizientenberechnung.
        for (Expression summand : summandsLeft) {
            // Zunächst konstante Koeffizienten separat sammeln:
            if (!summand.contains(var)) {
                coefficients.put(0, coefficients.get(0).add(summand));
            }
            factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(summand);
            factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summand);
            for (int j = 0; j < factorsNumerator.getBound(); j++) {
                // Zunächst lineare Koeffizienten separat sammeln:
                if (factorsNumerator.get(j).equals(Variable.create(var))) {
                    factorsNumerator.remove(j);
                    currentCoefficient = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator);
                    coefficients.put(1, coefficients.get(1).add(currentCoefficient));
                } else if (factorsNumerator.get(j).isPositiveIntegerPower() && ((BinaryOperation) factorsNumerator.get(j)).getLeft().equals(Variable.create(var))) {
                    exponent = ((Constant) ((BinaryOperation) factorsNumerator.get(j)).getRight()).getValue().intValue();
                    factorsNumerator.remove(j);
                    currentCoefficient = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator);
                    coefficients.put(exponent, coefficients.get(exponent).add(currentCoefficient));
                }
            }
        }
        for (Expression summand : summandsRight) {
            // Zunächst konstante Koeffizienten separat sammeln:
            if (!summand.contains(var)) {
                coefficients.put(0, coefficients.get(0).sub(summand));
            }
            factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(summand);
            factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summand);
            for (int j = 0; j < factorsNumerator.getBound(); j++) {
                // Zunächst lineare Koeffizienten separat sammeln:
                if (factorsNumerator.get(j).equals(Variable.create(var))) {
                    factorsNumerator.remove(j);
                    currentCoefficient = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator);
                    coefficients.put(1, coefficients.get(1).sub(currentCoefficient));
                } else if (factorsNumerator.get(j).isPositiveIntegerPower() && ((BinaryOperation) factorsNumerator.get(j)).getLeft().equals(Variable.create(var))) {
                    exponent = ((Constant) ((BinaryOperation) factorsNumerator.get(j)).getRight()).getValue().intValue();
                    factorsNumerator.remove(j);
                    currentCoefficient = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator);
                    coefficients.put(exponent, coefficients.get(exponent).sub(currentCoefficient));
                }
            }
        }

        // Koeffizienten vereinfachen!
        coefficients = coefficients.simplify(simplifyTypesExpandPolynomial);

        // Führende Koeffizienten, die = 0 sind, entfernen.
        while (coefficients.getBound() > 0 && coefficients.getLast().equals(Expression.ZERO)) {
            coefficients.remove(coefficients.getBound() - 1);
        }

        return coefficients;

    }

    /**
     * Gibt das Polynom bzgl. der Veränderlichen var zurück, wenn coefficients
     * die Koeffizienten darstellen. null-Koeffizienten werden ignoriert.
     */
    public static Expression getPolynomialFromCoefficients(ExpressionCollection coefficients, String var) {
        Expression polynomial = Expression.ZERO;
        for (int i = 0; i < coefficients.getBound(); i++) {
            if (coefficients.get(i) != null) {
                polynomial = polynomial.add(coefficients.get(i).mult(Variable.create(var).pow(i)));
            }
        }
        return polynomial;
    }

    /**
     * Gibt das Polynom bzgl. der Veränderlichen var zurück, wenn coefficients
     * die Koeffizienten darstellen. null-Koeffizienten werden ignoriert.
     */
    public static Expression getPolynomialFromCoefficients(String var, Object... coefficients) {
        Expression polynomial = Expression.ZERO;
        for (int i = 0; i < coefficients.length; i++) {
            if (coefficients[i] == null) {
                continue;
            }
            if (coefficients[i] instanceof Expression) {
                if (((Expression) coefficients[i]).isQuotient() && ((BinaryOperation) coefficients[i]).getLeft().equals(ONE)) {
                    // Ist der Koeffizient von der Form 1/a, so wird var^i/a hinzuaddiert.
                    polynomial = polynomial.add(Variable.create(var).pow(i).div(((BinaryOperation) coefficients[i]).getRight()));
                } else {
                    polynomial = polynomial.add(((Expression) coefficients[i]).mult(Variable.create(var).pow(i)));
                }
            } else if (coefficients[i] instanceof Integer) {
                polynomial = polynomial.add(new Constant((Integer) coefficients[i]).mult(Variable.create(var).pow(i)));
            } else if (coefficients[i] instanceof BigInteger) {
                polynomial = polynomial.add(new Constant((BigInteger) coefficients[i]).mult(Variable.create(var).pow(i)));
            } else if (coefficients[i] instanceof BigDecimal) {
                polynomial = polynomial.add(new Constant((BigDecimal) coefficients[i]).mult(Variable.create(var).pow(i)));
            } else if (coefficients[i] instanceof String) {
                polynomial = polynomial.add(Variable.create((String) coefficients[i]).mult(Variable.create(var).pow(i)));
            }
        }
        return polynomial;
    }

    /**
     * Zerlegt ein Polynom in Linearteile, soweit es geht.<br>
     * BEISPIEL: Das Polynom 5*x+6*x^3+x^5-(2+6*x^2+4*x^4) wird zu
     * (x-1)^2*(x-2)*(x^2+1) faktorisiert.
     *
     * @throws EvaluationException
     */
    public static Expression decomposeRationalPolynomialInIrreducibleFactors(Expression f, String var) throws EvaluationException {

        if (!SimplifyPolynomialUtils.isPolynomial(f, var)) {
            return f;
        }
        if (f.isProduct()) {
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, decomposeRationalPolynomialInIrreducibleFactors(factors.get(i), var));
            }
            return SimplifyUtilities.produceProduct(factors).simplify(simplifyTypesDecomposePolynomial);
        }
        if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant() && ((BinaryOperation) f).getRight().isNonNegative()) {
            Expression baseDecomposed = decomposeRationalPolynomialInIrreducibleFactors(((BinaryOperation) f).getLeft(), var);
            ExpressionCollection factors = SimplifyUtilities.getFactors(baseDecomposed);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).pow(((BinaryOperation) f).getRight()));
            }
            return SimplifyUtilities.produceProduct(factors);
        }

        // Leitkoeffizienten faktorisieren, damit das Restpolynom normiert ist.
        ExpressionCollection a = SimplifyPolynomialUtils.getPolynomialCoefficients(f, var);
        Expression leadCoefficient = a.get(a.getBound() - 1);
        a.divideByExpression(leadCoefficient);
        a = a.simplify();

        try {
            return leadCoefficient.mult(decomposeRationalPolynomial(a, var));
        } catch (PolynomialNotDecomposableException e) {
            return f;
        }

    }

    /**
     * Zerlegt ein Polynom in Linearteile, soweit es geht.<br>
     * BEISPIEL: Das Polynom 5*x+6*x^3+x^5-(2+6*x^2+4*x^4) wird zu
     * (x-1)^2*(x-2)*(x^2+1) faktorisiert.
     *
     * @throws EvaluationException
     */
    public static Expression decomposePolynomialInIrreducibleFactors(Expression f, String var) throws EvaluationException {

        if (!SimplifyPolynomialUtils.isPolynomial(f, var)) {
            return f;
        }
        if (f.isProduct()) {
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, decomposePolynomialInIrreducibleFactors(factors.get(i), var));
            }
            return SimplifyUtilities.produceProduct(factors).simplify(simplifyTypesDecomposePolynomial);
        }
        if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant() && ((BinaryOperation) f).getRight().isNonNegative()) {
            Expression baseDecomposed = decomposePolynomialInIrreducibleFactors(((BinaryOperation) f).getLeft(), var);
            ExpressionCollection factors = SimplifyUtilities.getFactors(baseDecomposed);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).pow(((BinaryOperation) f).getRight()));
            }
            return SimplifyUtilities.produceProduct(factors);
        }

        // Leitkoeffizienten faktorisieren, damit das Restpolynom normiert ist.
        ExpressionCollection a = SimplifyPolynomialUtils.getPolynomialCoefficients(f, var);
        Expression leadCoefficient = a.get(a.getBound() - 1);
        a.divideByExpression(leadCoefficient);
        a = a.simplify();

        return leadCoefficient.mult(decomposePolynomialInIrreducibleFactors(a, var));

    }

    /**
     * Faktorisiert ein Polynom, welches durch seine Koeffizienten a gegeben
     * ist.
     *
     * @throws EvaluationException
     */
    private static Expression decomposePolynomialInIrreducibleFactors(ExpressionCollection a, String var) throws EvaluationException {

        if (a.getBound() < 2) {
            return getPolynomialFromCoefficients(a, var);
        }

        try {
            return decomposePeriodicPolynomial(a, var);
        } catch (PolynomialNotDecomposableException e) {
        }

        try {
            return decomposeAntiperiodicPolynomial(a, var);
        } catch (PolynomialNotDecomposableException e) {
        }

        if (isPolynomialCyclic(a)) {
            try {
                Expression b = MINUS_ONE.mult(a.get(0)).div(a.get(a.getBound() - 1)).simplify();
                return decomposeCyclicPolynomial(a.getBound() - 1, b, var);
            } catch (PolynomialNotDecomposableException e) {
            }
        }

        if (isPolynomialRational(a)) {
            try {
                return decomposeRationalPolynomial(a, var);
            } catch (PolynomialNotDecomposableException e) {
            }
            try {
                return decomposePolynomialIntoSquarefreeFactors(a, var);
            } catch (PolynomialNotDecomposableException e) {
            }
            try {
                return decomposeRationalPolynomialBySolvingPolynomialSystem(a, var);
            } catch (PolynomialNotDecomposableException e) {
            }
        }

        try {
            return decomposePolynomialInMonomial(a, var);
        } catch (PolynomialNotDecomposableException e) {
        }

        if (a.getBound() == 3) {
            try {
                return decomposeQuadraticPolynomial(a, var);
            } catch (PolynomialNotDecomposableException e) {
            }
        }
        if (a.getBound() == 4) {
            try {
                return decomposeCubicPolynomial(a, var);
            } catch (PolynomialNotDecomposableException e) {
            }
        }

        return getPolynomialFromCoefficients(a, var);

    }

    /**
     * Prüfung, ob ein Polynom zyklisch ist.
     */
    private static boolean isPolynomialCyclic(ExpressionCollection a) {
        for (int i = 1; i < a.getBound() - 1; i++) {
            if (!a.get(i).equals(ZERO)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Prüfung, ob ein Polynom rational ist.
     */
    public static boolean isPolynomialRational(ExpressionCollection a) {
        for (int i = 0; i < a.getBound(); i++) {
            if (!a.get(i).isIntegerConstantOrRationalConstant()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Faktorisiert quadratische Polynome.
     */
    private static Expression decomposeQuadraticPolynomial(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        Expression discriminant = a.get(1).pow(2).sub(Expression.FOUR.mult(a.get(0)).mult(a.get(2))).simplify();
        if (discriminant.isAlwaysNonNegative()) {
            Expression zeroOne = Expression.MINUS_ONE.mult(a.get(1)).add(discriminant.pow(1, 2)).div(Expression.TWO.mult(a.get(2))).simplify();
            Expression zeroTwo = Expression.MINUS_ONE.mult(a.get(1)).sub(discriminant.pow(1, 2)).div(Expression.TWO.mult(a.get(2))).simplify();
            if (zeroOne.equivalent(zeroTwo)) {
                return a.get(2).mult(Variable.create(var).sub(zeroOne).simplify().pow(2));
            } else {
                return a.get(2).mult(Variable.create(var).sub(zeroOne).simplify().mult(Variable.create(var).sub(zeroTwo).simplify()));
            }
        }

        throw new PolynomialNotDecomposableException();

    }

    /**
     * Faktorisiert kubische Polynome.
     */
    private static Expression decomposeCubicPolynomial(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        Expression A = a.get(2).div(a.get(3)).simplify();
        Expression B = a.get(1).div(a.get(3)).simplify();
        Expression C = a.get(0).div(a.get(3)).simplify();

        // Gelöst wird nun die Gleichung x^3 + Ax^2 + Bx + C = 0
        /*
         Substitution x = z - A/3 (später muss zurücksubstituiert werden): p =
         B - A^2/3, q = 2A^3/27 - AB/3 + C. Gelöst wird nun die Gleichung z^3
         + pz + q = 0
         */
        Expression p = B.sub(A.pow(2).div(3)).simplify();
        Expression q = Expression.TWO.mult(A.pow(3).div(27)).sub(A.mult(B).div(3)).add(C).simplify();

        // Diskriminante discriminant = (p/3)^3 + (q/2)^2 = p^3/27 + q^2/4.
        Expression discriminant = p.pow(3).div(27).add(q.pow(2).div(4)).simplify();

        if (discriminant.equals(ZERO) || discriminant.isAlwaysPositive() || discriminant.isAlwaysNegative()) {
            ExpressionCollection zeros = PolynomialAlgebraUtils.solveCubicEquation(a);
            if (discriminant.isAlwaysPositive()) {
                // z_0 = einzige Nullstelle. Restfaktor = x^2 + (z_0+A)*x + (z_0^2+A*z_0+B).
                Expression irreducibleQuadraticFactor = Variable.create(var).pow(2).add(
                        zeros.get(0).add(A).mult(Variable.create(var))).add(
                        zeros.get(0).pow(2).add(A.mult(zeros.get(0))).add(B)).simplify();
                return a.get(3).mult(Variable.create(var).sub(zeros.get(0)).simplify()).mult(irreducibleQuadraticFactor);
            }
            if (discriminant.equals(ZERO)) {
                return a.get(3).mult(Variable.create(var).sub(zeros.get(0)).pow(2).simplify()).mult(Variable.create(var).sub(zeros.get(1)).simplify());
            }
            if (discriminant.isAlwaysNegative()) {
                return a.get(3).mult(Variable.create(var).sub(zeros.get(0)).simplify()).mult(
                        Variable.create(var).sub(zeros.get(1)).simplify()).mult(
                        Variable.create(var).sub(zeros.get(2)).simplify());
            }
        }

        throw new PolynomialNotDecomposableException();

    }

    /**
     * Faktorisiert Polynome vom Typ x^n - a.
     */
    private static Expression decomposeCyclicPolynomial(int n, Expression a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        if (n <= 2 || n > ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL
                || !a.isAlwaysPositive() && !a.isAlwaysNegative()) {
            throw new PolynomialNotDecomposableException();
        }

        Expression decomposedPolynomial = Expression.ONE;
        Expression quadraticFactor;

        // Im Folgenden dient simplify() dazu, die Werte a^(1/n)*cos(2*k*pi/n) zu vereinfachen, falls möglich.
        if (a.isAlwaysPositive()) {

            if (n % 2 == 0) {
                decomposedPolynomial = Variable.create(var).sub(a.pow(1, n)).simplify().mult(Variable.create(var).add(a.pow(1, n)).simplify());
                for (int i = 1; i < n / 2; i++) {
                    quadraticFactor = Variable.create(var).pow(2).sub(
                            TWO.mult(a.pow(1, n)).mult(TWO.mult(i).mult(PI).div(n).cos()).mult(Variable.create(var))).add(
                            a.pow(2, n)).simplify();
                    decomposedPolynomial = decomposedPolynomial.mult(quadraticFactor);
                }
            } else {
                decomposedPolynomial = Variable.create(var).sub(a.pow(1, n)).simplify();
                for (int i = 0; i < n / 2; i++) {
                    quadraticFactor = Variable.create(var).pow(2).sub(
                            TWO.mult(a.pow(1, n)).mult(TWO.mult(i + 1).mult(PI).div(n).cos()).mult(Variable.create(var))).add(
                            a.pow(2, n)).simplify();
                    decomposedPolynomial = decomposedPolynomial.mult(quadraticFactor);
                }
            }

        } else {

            a = MINUS_ONE.mult(a).simplify();
            if (n % 2 == 0) {
                for (int i = 0; i < n / 2; i++) {
                    quadraticFactor = Variable.create(var).pow(2).sub(
                            TWO.mult(a.pow(1, n)).mult(new Constant(2 * i + 1).mult(PI).div(n).cos()).mult(Variable.create(var))).add(
                            a.pow(2, n)).simplify();
                    decomposedPolynomial = decomposedPolynomial.mult(quadraticFactor);
                }
            } else {
                decomposedPolynomial = Variable.create(var).add(a.pow(1, n)).simplify();
                for (int i = 0; i < n / 2; i++) {
                    quadraticFactor = Variable.create(var).pow(2).sub(
                            TWO.mult(a.pow(1, n)).mult(new Constant(2 * i + 1).mult(PI).div(n).cos()).mult(Variable.create(var))).add(
                            a.pow(2, n)).simplify();
                    decomposedPolynomial = decomposedPolynomial.mult(quadraticFactor);
                }
            }

        }

        return decomposedPolynomial;

    }

    /**
     * Faktorisiert Polynome mit periodischen Koeffizienten.
     */
    private static Expression decomposePeriodicPolynomial(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        int m = getPeriodOfCoefficients(a);

        if (m > ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL || m == a.getBound()) {
            throw new PolynomialNotDecomposableException();
        }

        if (m == 1) {

            //Sonderfall: Das Polynom hat die Form 1 + x + x^2 + ... + x^n. Zerlegung mittels Einheitswurzeln.
            if (a.getBound() == 2) {
                throw new PolynomialNotDecomposableException();
            }
            int n = a.getBound();
            Expression decomposedPolynomial = ONE, quadraticFactor;

            if (n % 2 == 1) {
                for (int i = 1; i <= n / 2; i++) {
                    quadraticFactor = Variable.create(var).pow(2).sub(
                            TWO.mult(TWO.mult(i).mult(PI).div(n).cos()).mult(Variable.create(var))).add(ONE).simplify();
                    decomposedPolynomial = decomposedPolynomial.mult(quadraticFactor);
                }
            } else {
                decomposedPolynomial = Variable.create(var).add(ONE).simplify();
                for (int i = 1; i < n / 2; i++) {
                    quadraticFactor = Variable.create(var).pow(2).sub(
                            TWO.mult(TWO.mult(i).mult(PI).div(n).cos()).mult(Variable.create(var))).add(ONE).simplify();
                    decomposedPolynomial = decomposedPolynomial.mult(quadraticFactor);
                }
            }

            return decomposedPolynomial;

        }

        // Ab hier ist m >= 2.
        ExpressionCollection coefficientsSecondFactor = new ExpressionCollection();

        for (int i = 0; i < a.getBound() - m + 1; i++) {
            if (i % m == 0) {
                coefficientsSecondFactor.put(i, ONE);
            } else {
                coefficientsSecondFactor.put(i, ZERO);
            }
        }

        Expression polynomialOfPrimitivePeriod = decomposePolynomialInIrreducibleFactors(a.copy(0, m), var);
        Expression secondFactor = decomposePolynomialInIrreducibleFactors(coefficientsSecondFactor, var);

        return polynomialOfPrimitivePeriod.mult(secondFactor).simplify(simplifyTypesDecomposePolynomial);

    }

    /**
     * Faktorisiert Polynome mit antiperiodischen Koeffizienten.
     */
    private static Expression decomposeAntiperiodicPolynomial(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        int m = getAntiperiodOfCoefficients(a);

        if (m > ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL || m == 1 || m == a.getBound()) {
            throw new PolynomialNotDecomposableException();
        }

        Expression polynomialOfPrimitivePeriod = SimplifyPolynomialUtils.getPolynomialFromCoefficients(a.copy(0, m), var);
        Expression secondFactor = ONE;

        for (int i = 1; i < a.getBound() / m; i++) {
            if (i % 2 == 1) {
                secondFactor = secondFactor.sub(Variable.create(var).pow(i * m));
            } else {
                secondFactor = secondFactor.add(Variable.create(var).pow(i * m));
            }
        }

        polynomialOfPrimitivePeriod = decomposePolynomialInIrreducibleFactors(polynomialOfPrimitivePeriod, var);
        secondFactor = decomposePolynomialInIrreducibleFactors(secondFactor, var);

        return polynomialOfPrimitivePeriod.mult(secondFactor).simplify(simplifyTypesDecomposePolynomial);

    }

    /**
     * Faktorisiert Polynome, deren Monome Exponenten mit einem nichttrivialen
     * ggT besitzen (beispielsweise Polynome wie x^9+5*x^6-2*x^3+7; der ggT der
     * Exponenten beträgt 3).
     */
    private static Expression decomposePolynomialInMonomial(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        int m = getGGTOfAllExponents(a);

        if (m > ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL || m <= 1) {
            throw new PolynomialNotDecomposableException();
        }

        ExpressionCollection coefficients = new ExpressionCollection();
        for (int i = 0; i < a.getBound(); i++) {
            if (i % m == 0) {
                coefficients.add(a.get(i));
            }
        }

        Expression polynomialToDecompose = SimplifyPolynomialUtils.getPolynomialFromCoefficients(coefficients, var);
        polynomialToDecompose = decomposePolynomialInIrreducibleFactors(polynomialToDecompose, var);

        if (polynomialToDecompose.isProduct()) {

            // In diesem Fall konnte das Polynom faktorisiert werden.
            Expression decomposedPolynomial = polynomialToDecompose.replaceVariable(var, Variable.create(var).pow(m)).simplify(simplifyTypesDecomposePolynomial);
            ExpressionCollection factors = SimplifyUtilities.getFactors(decomposedPolynomial);
            if (factors.getBound() > 1) {
                for (int i = 0; i < factors.getBound(); i++) {
                    factors.put(i, decomposePolynomialInIrreducibleFactors(factors.get(i), var));
                }
                return SimplifyUtilities.produceProduct(factors);
            }

        }

        throw new PolynomialNotDecomposableException();

    }

    /**
     * Faktorisiert rationale Polynome mittels Nullstellensuche.<br>
     * VORAUSSETZUNG: alle Elemente von a sind rational.
     */
    private static Expression decomposeRationalPolynomial(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection restCoefficients = PolynomialAlgebraUtils.findAllRationalZerosOfRationalPolynomial(a, zeros);
        if (zeros.isEmpty()) {
            throw new PolynomialNotDecomposableException();
        }

        /* 
         Das Polynom, welches durch den Divisionsrest durch alle Linearfaktoren gegeben ist, 
         muss ebenfalls noch faktorisiert werden.
         */
        Expression result = decomposePolynomialInIrreducibleFactors(restCoefficients, var);
        int l = zeros.getBound();
        Expression currentZero = zeros.get(0);
        int currentMultiplicity = 1;
        while (!zeros.isEmpty()) {
            for (int i = 0; i < l; i++) {
                if (zeros.get(i) != null) {
                    currentZero = zeros.get(i);
                    currentMultiplicity = 1;
                    zeros.remove(i);
                    break;
                }
            }
            for (int i = 0; i < l; i++) {
                if (zeros.get(i) != null && zeros.get(i).equals(currentZero)) {
                    currentMultiplicity++;
                    zeros.remove(i);
                }
            }
            result = result.mult((Variable.create(var).sub(currentZero).simplify()).pow(currentMultiplicity));
        }

        return a.get(a.getBound() - 1).mult(result).simplify(simplifyTypesDecomposePolynomial);

    }

    /**
     * Faktorisiert ein Polynom in quadratfreie Faktoren. Ist das Polynom
     * rational, so hat die Faktorisierung die folgende Gestalt: ist f das zu
     * zerlegende Polynom, so wird f zu f = g<sub>1</sub>^n<sub>1</sub>* ...
     * *g<sub>k</sub>^n<sub>k</sub> mit n<sub>1</sub>
     * &#60; ... &#60; n<sub>k</sub> und paarweise teilerfremden g_i
     * faktorisiert.
     *
     * @throws EvaluationException
     */
    public static Expression decomposePolynomialIntoSquarefreeFactors(Expression f, String var) throws EvaluationException {

        if (!isPolynomial(f, var)) {
            return f;
        }
        // Faktorisierung nur für Polynome, wenn degree <= gewisse Schranke ist.
        if (getDegreeOfPolynomial(f, var).compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL)) > 0) {
            return f;
        }

        try {
            // Polynom normieren und zerlegen.
            ExpressionCollection coefficients = getPolynomialCoefficients(f, var);
            Expression leadingCoefficient = coefficients.get(coefficients.getBound() - 1);
            coefficients.divideByExpression(leadingCoefficient);
            coefficients.simplify();
            return leadingCoefficient.mult(decomposePolynomialIntoSquarefreeFactors(coefficients, var));
        } catch (PolynomialNotDecomposableException e) {
            return f;
        }

    }

    /**
     * Faktorisiert ein Polynom in quadratfreie Faktoren.
     *
     * @throws EvaluationException
     */
    public static Expression decomposePolynomialIntoSquarefreeFactorsLight(Expression f, String var) throws EvaluationException {

        if (!isPolynomial(f, var)) {
            return f;
        }
        // Faktorisierung nur für Polynome, wenn degree <= gewisse Schranke ist.
        if (getDegreeOfPolynomial(f, var).compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL)) > 0) {
            return f;
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        Expression base, decomposition;
        BigInteger exponent;
        for (int i = 0; i < factors.getBound(); i++) {
            try {
                if (factors.get(i).isPositiveIntegerPower()) {
                    base = ((BinaryOperation) factors.get(i)).getLeft();
                    exponent = ((Constant) ((BinaryOperation) factors.get(i)).getRight()).getBigIntValue();
                } else {
                    base = factors.get(i);
                    exponent = BigInteger.ONE;
                }
                // Polynom normieren, zerlegen und dann in die Faktoren wieder einfügen.
                ExpressionCollection coefficients = getPolynomialCoefficients(base, var);
                Expression leadingCoefficient = coefficients.get(coefficients.getBound() - 1);
                coefficients.divideByExpression(leadingCoefficient);
                coefficients.simplify();
                decomposition = leadingCoefficient.mult(decomposePolynomialIntoSquarefreeFactors(coefficients, var));
                factors.put(i, decomposition.pow(exponent));
            } catch (PolynomialNotDecomposableException e) {
            }
        }

        // Jetzt passende Faktoren zusammenfassen und ausgeben.
        return SimplifyUtilities.produceProduct(factors).simplify(TypeSimplify.order_difference_and_division, TypeSimplify.order_sums_and_products,
                TypeSimplify.simplify_basic, TypeSimplify.simplify_collect_products);

    }

    /**
     * Faktorisiert ein Polynom f in quadratfreie Faktoren. Kann solch eine
     * Zerlegung nicht ermittelt werden, so wird eine
     * PolynomialNotDecomposableException geworfen.<br>
     */
    public static Expression decomposePolynomialIntoSquarefreeFactors(ExpressionCollection a, String var) throws EvaluationException, PolynomialNotDecomposableException {

        // Wenn a nicht mindestens einem linearen Polynom entspricht, so kann es nicht quadratfrei faktorisiert werden.
        if (a.getBound() < 2) {
            throw new PolynomialNotDecomposableException();
        }

        ExpressionCollection aCopy = new ExpressionCollection(a);
        // a normieren!
        Expression leadingCoefficient = aCopy.get(aCopy.getBound() - 1);
        aCopy.divideByExpression(leadingCoefficient);
        aCopy = aCopy.simplify();

        Expression decomposition = decomposeRationalPolynomialByComputingGGTWithDerivative(aCopy, var);

        // Falls decomposition kein Produkt ist, dann konnte das Polynom nicht faktorisiert werden.
        if (decomposition.isNotProduct() && !decomposition.isPositiveIntegerPower()) {
            throw new PolynomialNotDecomposableException();
        }

        ArrayList<ExpressionCollection> coefficientsOfFactors = new ArrayList<>();
        ExpressionCollection factors = SimplifyUtilities.getFactors(decomposition);
        ExpressionCollection polynomialCoefficients;
        for (Expression factor : factors) {
            if (factor.isPositiveIntegerPower()) {
                if (((Constant) ((BinaryOperation) factor).getRight()).getBigIntValue().compareTo(
                        BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL)) > 0) {
                    throw new PolynomialNotDecomposableException();
                }
                int exponent = ((Constant) ((BinaryOperation) factor).getRight()).getValue().intValue();
                polynomialCoefficients = getPolynomialCoefficients(((BinaryOperation) factor).getLeft(), var);
                for (int i = 0; i < exponent; i++) {
                    coefficientsOfFactors.add(polynomialCoefficients.copy());
                }
            } else {
                coefficientsOfFactors.add(getPolynomialCoefficients(factor, var));
            }
        }

        ExpressionCollection[] quotient;

        // Versuchen, den j-ten Faktor durch eine Potenz des i-ten Faktors zu dividieren.
        for (int i = 0; i < coefficientsOfFactors.size(); i++) {

            if (coefficientsOfFactors.get(i).getBound() < 2) {
                continue;
            }

            for (int j = 0; j < coefficientsOfFactors.size(); j++) {

                if (j == i || coefficientsOfFactors.get(j).getBound() < 2) {
                    continue;
                }

                if (coefficientsOfFactors.get(j).getBound() >= coefficientsOfFactors.get(i).getBound()) {
                    quotient = polynomialDivision(coefficientsOfFactors.get(j), coefficientsOfFactors.get(i));
                    while (quotient[1].isEmpty() && quotient[0].getBound() > 1) {
                        coefficientsOfFactors.remove(j);
                        coefficientsOfFactors.add(j, quotient[0]);
                        coefficientsOfFactors.add(coefficientsOfFactors.get(i));
                        quotient = polynomialDivision(quotient[0], coefficientsOfFactors.get(i));
                    }
                }

            }
        }

        Expression result = leadingCoefficient;
        for (ExpressionCollection factor : coefficientsOfFactors) {
            result = result.mult(getPolynomialFromCoefficients(factor, var));
        }

        return result.simplify(simplifyTypesDecomposePolynomial);

    }

    /**
     * Hilfsmethode für die Faktorisierung von rationalen Polynomen f mittels
     * Berechnung der gemeinsamen Faktoren von f und f'.<br>
     * VORAUSSETZUNG: Alle Elemente von a sind rational.
     */
    private static Expression decomposeRationalPolynomialByComputingGGTWithDerivative(ExpressionCollection a, String var) throws EvaluationException {

        ExpressionCollection coefficientsOfDerivative = getPolynomialCoefficientsOfDerivative(a);
        ExpressionCollection ggT = getGGTOfPolynomials(a, coefficientsOfDerivative);

        if (ggT.getBound() < 2) {
            return getPolynomialFromCoefficients(a, var);
        }

        // Dann gibt es einen nichttrivialen ggT.
        ExpressionCollection quotient = polynomialDivision(a, ggT)[0];
        return decomposeRationalPolynomialByComputingGGTWithDerivative(quotient, var).mult(
                decomposeRationalPolynomialByComputingGGTWithDerivative(ggT, var)).simplify(simplifyTypesDecomposePolynomial);

    }

    /**
     * Sind a die Koeffizienten eines Polynoms f, so wird eine
     * ExpressionCollection mit Koeffizienten von f' zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection getPolynomialCoefficientsOfDerivative(ExpressionCollection a) throws EvaluationException {
        ExpressionCollection coefficientsOfDerivative = new ExpressionCollection();
        for (int i = 0; i < a.getBound() - 1; i++) {
            coefficientsOfDerivative.put(i, new Constant(i + 1).mult(a.get(i + 1)).simplify());
        }
        return coefficientsOfDerivative;
    }

    private static Expression decomposeRationalPolynomialBySolvingPolynomialSystem(ExpressionCollection a, String var) throws PolynomialNotDecomposableException {

        if (a.getBound() - 1 > ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL_FOR_DECOMPOSITION) {
            throw new PolynomialNotDecomposableException();
        }

        ExpressionCollection coefficientsOfNormalizedPolynomial = new ExpressionCollection();
        try {
            for (int i = 0; i < a.getBound() - 1; i++) {
                coefficientsOfNormalizedPolynomial.add(a.get(i).div(a.get(a.getBound() - 1)).simplify());
            }
        } catch (EvaluationException e) {
            throw new PolynomialNotDecomposableException();
        }

        // i ist der Grad eines Faktors in der Zerlegung.
        Expression[] equations = new Expression[a.getBound() - 1];
        List<String> vars = new ArrayList<>();
        for (int i = 0; i < coefficientsOfNormalizedPolynomial.getBound(); i++) {
            vars.add(NotationLoader.SUBSTITUTION_VAR + "_" + i);
        }

        Expression[] coefficientVarsOfFirstFactor, coefficientVarsOfSecondFactor;

        List<Expression[]> solutions;
        for (int i = 2; i <= coefficientsOfNormalizedPolynomial.getBound() / 2; i++) {

            coefficientVarsOfFirstFactor = new Expression[i + 1];
            coefficientVarsOfSecondFactor = new Expression[coefficientsOfNormalizedPolynomial.getBound() - i + 1];

            for (int k = 0; k < coefficientVarsOfFirstFactor.length; k++) {
                if (k < coefficientVarsOfFirstFactor.length - 1) {
                    coefficientVarsOfFirstFactor[k] = Variable.create(NotationLoader.SUBSTITUTION_VAR + "_" + k);
                } else {
                    coefficientVarsOfFirstFactor[k] = ONE;
                }
            }
            for (int k = 0; k < coefficientVarsOfSecondFactor.length; k++) {
                if (k < coefficientVarsOfSecondFactor.length - 1) {
                    coefficientVarsOfSecondFactor[k] = Variable.create(NotationLoader.SUBSTITUTION_VAR + "_" + (i + k));
                } else {
                    coefficientVarsOfSecondFactor[k] = ONE;
                }
            }

            // Gleichungssystem bilden.
            for (int j = 0; j < coefficientsOfNormalizedPolynomial.getBound(); j++) {
                equations[j] = ZERO;
                for (int k = j; k >= 0; k--) {
                    if (k > i || j - k > coefficientsOfNormalizedPolynomial.getBound() - i || j - k < 0) {
                        continue;
                    }
                    equations[j] = equations[j].add(coefficientVarsOfFirstFactor[k].mult(coefficientVarsOfSecondFactor[j - k]));
                }
            }
            for (int j = 0; j < coefficientsOfNormalizedPolynomial.getBound(); j++) {
                equations[j] = equations[j].sub(coefficientsOfNormalizedPolynomial.get(j));
            }

            // Gleichungssystem lösen.
            try {
                solutions = SolveGeneralSystemOfEquationsUtils.solvePolynomialSystemOfEquations(equations, vars, SolveGeneralSystemOfEquationsUtils.SolutionType.RATIONAL);
            } catch (NotAlgebraicallySolvableException e) {
                // Nichts tun, weiter probieren!
                continue;
            }

            // Fall: Keine Lösung.
            if (solutions.isEmpty()) {
                throw new PolynomialNotDecomposableException();
            }

            ExpressionCollection coefficientsOfFirstFactor = new ExpressionCollection();
            ExpressionCollection coefficientsOfSecondFactor = new ExpressionCollection();

            // Aufgrund der Symmetrie der Faktoren gibt es immer mehr als eine Lösung. Welche man nun nimmt, ist völlig egal.
            for (int j = 0; j < i; j++) {
                coefficientsOfFirstFactor.add(solutions.get(0)[j]);
            }
            // Leitkoeffizient = 1.
            coefficientsOfFirstFactor.add(ONE);

            for (int j = i; j < coefficientsOfNormalizedPolynomial.getBound(); j++) {
                coefficientsOfSecondFactor.add(solutions.get(0)[j]);
            }
            // Leitkoeffizient = 1.
            coefficientsOfSecondFactor.add(ONE);

            return a.get(a.getBound() - 1).mult(getPolynomialFromCoefficients(coefficientsOfFirstFactor, var).mult(getPolynomialFromCoefficients(coefficientsOfSecondFactor, var)));

        }

        throw new PolynomialNotDecomposableException();

    }

    /**
     * Addition zweier Polynome.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection addPolynomials(ExpressionCollection coefficientsLeft, ExpressionCollection coefficientsRight) throws EvaluationException {

        ExpressionCollection coefficientsOfSum = new ExpressionCollection();
        for (int i = 0; i < Math.max(coefficientsLeft.getBound(), coefficientsRight.getBound()); i++) {
            if (coefficientsLeft.get(i) == null && coefficientsRight.get(i) == null) {
                coefficientsOfSum.put(i, ZERO);
            } else if (coefficientsLeft.get(i) == null && coefficientsRight.get(i) != null) {
                coefficientsOfSum.put(i, coefficientsRight.get(i));
            } else if (coefficientsLeft.get(i) != null && coefficientsRight.get(i) == null) {
                coefficientsOfSum.put(i, coefficientsLeft.get(i));
            } else {
                coefficientsOfSum.put(i, coefficientsLeft.get(i).add(coefficientsRight.get(i)));
            }
        }

        // Führende Nullen beseitigen.
        while (!coefficientsOfSum.isEmpty() && coefficientsOfSum.get(coefficientsOfSum.getBound() - 1).equals(ZERO)) {
            coefficientsOfSum.remove(coefficientsOfSum.getBound() - 1);
        }
        // Fehlende Koeffizienten durch Nullen ergänzen.
        for (int i = 0; i < coefficientsOfSum.getBound(); i++) {
            if (coefficientsOfSum.get(i) == null) {
                coefficientsOfSum.put(i, ZERO);
            }
        }

        return coefficientsOfSum.simplify(simplifyTypesPolynomialDivisionEuklidRepresentation);

    }

    /**
     * Subtraktion zweier Polynome.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection subtractPolynomials(ExpressionCollection coefficientsLeft, ExpressionCollection coefficientsRight) throws EvaluationException {

        ExpressionCollection coefficientsOfDifference = new ExpressionCollection();
        for (int i = 0; i < Math.max(coefficientsLeft.getBound(), coefficientsRight.getBound()); i++) {
            if (coefficientsLeft.get(i) == null && coefficientsRight.get(i) == null) {
                coefficientsOfDifference.put(i, ZERO);
            } else if (coefficientsLeft.get(i) == null && coefficientsRight.get(i) != null) {
                coefficientsOfDifference.put(i, MINUS_ONE.mult(coefficientsRight.get(i)));
            } else if (coefficientsLeft.get(i) != null && coefficientsRight.get(i) == null) {
                coefficientsOfDifference.put(i, coefficientsLeft.get(i));
            } else {
                coefficientsOfDifference.put(i, coefficientsLeft.get(i).sub(coefficientsRight.get(i)));
            }
        }
        coefficientsOfDifference = coefficientsOfDifference.simplify();

        // Führende Nullen beseitigen.
        while (!coefficientsOfDifference.isEmpty() && coefficientsOfDifference.get(coefficientsOfDifference.getBound() - 1).equals(ZERO)) {
            coefficientsOfDifference.remove(coefficientsOfDifference.getBound() - 1);
        }
        // Fehlende Koeffizienten durch Nullen ergänzen.
        for (int i = 0; i < coefficientsOfDifference.getBound(); i++) {
            if (coefficientsOfDifference.get(i) == null) {
                coefficientsOfDifference.put(i, ZERO);
            }
        }

        return coefficientsOfDifference.simplify(simplifyTypesPolynomialDivisionEuklidRepresentation);

    }

    /**
     * Multiplikation zweier Polynome.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection multiplyPolynomials(ExpressionCollection coefficientsLeft, ExpressionCollection coefficientsRight) throws EvaluationException {

        ExpressionCollection coefficientsOfProduct = new ExpressionCollection();
        for (int i = 0; i < coefficientsLeft.getBound(); i++) {
            if (coefficientsLeft.get(i) == null) {
                continue;
            }
            for (int j = 0; j < coefficientsRight.getBound(); j++) {
                if (coefficientsRight.get(j) == null) {
                    continue;
                }
                if (coefficientsOfProduct.get(i + j) == null) {
                    coefficientsOfProduct.put(i + j, coefficientsLeft.get(i).mult(coefficientsRight.get(j)));
                } else {
                    coefficientsOfProduct.put(i + j, coefficientsOfProduct.get(i + j).add(coefficientsLeft.get(i).mult(coefficientsRight.get(j))));
                }
            }
        }

        // Fehlende Koeffizienten durch Nullen ergänzen.
        for (int i = 0; i < coefficientsOfProduct.getBound(); i++) {
            if (coefficientsOfProduct.get(i) == null) {
                coefficientsOfProduct.put(i, ZERO);
            }
        }
        return coefficientsOfProduct.simplify(simplifyTypesPolynomialDivisionEuklidRepresentation);

    }

    /**
     * Polynomdivision des Polynoms coefficientsEnumerator[n]*x^n + ... +
     * coeffcicientsEnumerator.get(1)*x + coeffcicientsEnumerator.get(0) durch
     * coefficientsDenominator[m]*x^m + ... + coefficientsDenominator[0].
     * Zurückgegeben wird ein Array aus zwei ExpressionCollections, im 0-ten
     * Arrayeintrag stehen die Koeffizienten des Quotienten, im 1-ten
     * Arrayeintrag die Koeffizienten des Divisionsrests.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection[] polynomialDivision(ExpressionCollection coefficientsNumerator, ExpressionCollection coefficientsDenominator) throws EvaluationException {

        ExpressionCollection[] quotient = new ExpressionCollection[2];
        quotient[0] = new ExpressionCollection();
        quotient[1] = new ExpressionCollection();

        // Falls deg(Zähler) < deg(Nenner) -> fertig.
        if (coefficientsNumerator.getBound() < coefficientsDenominator.getBound()) {
            quotient[1] = new ExpressionCollection(coefficientsNumerator);
            return quotient;
        }

        /* 
         Division durch ein Nullpolynom. Sollte bei vernünftigen Anwendungen nicht passieren 
         (und muss im Vorfeld geprüft werden).
         */
        if (coefficientsDenominator.isEmpty()) {
            throw new EvaluationException(Translator.translateOutputMessage(EB_BinaryOperation_DIVISION_BY_ZERO));
        }

        int degreeDenominator = coefficientsDenominator.getBound() - 1;
        ExpressionCollection multipleOfDenominator = new ExpressionCollection();
        ExpressionCollection coeffcicientsEnumeratorCopy = coefficientsNumerator.copy();
        for (int i = coeffcicientsEnumeratorCopy.getBound() - 1; i >= degreeDenominator; i--) {
            quotient[0].put(i - degreeDenominator, coeffcicientsEnumeratorCopy.get(i).div(coefficientsDenominator.get(degreeDenominator)).simplify(simplifyTypesPolynomialDivisionEuklidRepresentation));
            for (int j = degreeDenominator; j >= 0; j--) {
                multipleOfDenominator.put(j, (coefficientsDenominator.get(j).mult(coeffcicientsEnumeratorCopy.get(i))).div(coefficientsDenominator.get(degreeDenominator)).simplify(simplifyTypesPolynomialDivisionEuklidRepresentation));
            }
            for (int j = degreeDenominator; j >= 0; j--) {
                coeffcicientsEnumeratorCopy.put(i + j - degreeDenominator, coeffcicientsEnumeratorCopy.get(i + j - degreeDenominator).sub(multipleOfDenominator.get(j)).simplify(simplifyTypesPolynomialDivisionEuklidRepresentation));
            }
        }
        int indexOfLeadingCoefficientInRest = degreeDenominator - 1;
        while (indexOfLeadingCoefficientInRest >= 0 && coeffcicientsEnumeratorCopy.get(indexOfLeadingCoefficientInRest).equals(Expression.ZERO)) {
            indexOfLeadingCoefficientInRest--;
        }
        for (int i = 0; i <= indexOfLeadingCoefficientInRest; i++) {
            quotient[1].put(i, coeffcicientsEnumeratorCopy.get(i));
        }
        return quotient;

    }

    /**
     * Gibt den ggT zweier Polynome f und g zurück. Ist f oder g kein Polynom in
     * var, oder hat f oder g einen zu hohen Grad, so wird 1 zurückgegeben.
     * Ebenso wird in schwer entscheidbaren Fällen 1 zurückgegeben.
     */
    public static Expression getGGTOfPolynomials(Expression f, Expression g, String var) {

        try {

            BigInteger degF = getDegreeOfPolynomial(f, var);
            BigInteger degG = getDegreeOfPolynomial(g, var);

            if (degF.compareTo(BigInteger.ZERO) < 0 || degG.compareTo(BigInteger.ZERO) < 0
                    || degF.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL)) > 0
                    || degG.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL)) > 0) {
                return ONE;
            }

            ExpressionCollection coefficientsF = SimplifyPolynomialUtils.getPolynomialCoefficients(f, var);
            ExpressionCollection coefficientsG = SimplifyPolynomialUtils.getPolynomialCoefficients(g, var);

            ExpressionCollection coefficientsGGT = getGGTOfPolynomials(coefficientsF, coefficientsG);
            return getPolynomialFromCoefficients(coefficientsGGT, var);

        } catch (EvaluationException e) {
            return ONE;
        }

    }

    /**
     * Gibt den ggT zweier Polynome zurück, die durch die Koeffizienten a und b
     * gegeben sind.
     */
    public static ExpressionCollection getGGTOfPolynomials(ExpressionCollection a, ExpressionCollection b) throws EvaluationException {

        // Spezialfall: alle Koeffizienten eines der beiden Polynome sind = 0.
        // Prüfung für a.
        boolean allCoefficientsAreZero = true;
        for (Expression coefficient : a) {
            if (!coefficient.equals(ZERO)) {
                allCoefficientsAreZero = false;
                break;
            }
        }
        if (allCoefficientsAreZero) {
            return b;
        }
        // Prüfung für b.
        allCoefficientsAreZero = true;
        for (Expression coefficient : b) {
            if (!coefficient.equals(ZERO)) {
                allCoefficientsAreZero = false;
                break;
            }
        }
        if (allCoefficientsAreZero) {
            return a;
        }

        if (a.getBound() < b.getBound()) {
            return getGGTOfPolynomials(b, a);
        }

        ExpressionCollection aCopy = a.copy();
        ExpressionCollection bCopy = b.copy();
        ExpressionCollection r = polynomialDivision(aCopy, bCopy)[1];

        while (!r.isEmpty()) {
            aCopy = bCopy.copy();
            bCopy = r.copy();
            r = polynomialDivision(aCopy, bCopy)[1];
        }

        // Anschließend normieren!
        if (bCopy.getBound() > 0 && !ZERO.equals(bCopy.get(bCopy.getBound() - 1))) {
            bCopy.divideByExpression(bCopy.get(bCopy.getBound() - 1));
            bCopy = bCopy.simplify();
        }

        return bCopy;

    }

    /**
     * Gibt die Eukliddarstellung zweier Polynome f und g zurück: ist d = gcd(f,
     * g), so wird ein Polynomarray {a, b} zurückgegeben mit der Eigenschaft,
     * dass a*f + b*g = d. Dabei soll d stets normiert.
     */
    public static Expression[] getEuclideanRepresentationOfGCDOfTwoPolynomials(Expression f, Expression g, String var) {

        try {

            BigInteger degF = getDegreeOfPolynomial(f, var);
            BigInteger degG = getDegreeOfPolynomial(g, var);

            if (degF.compareTo(BigInteger.ZERO) < 0 || degG.compareTo(BigInteger.ZERO) < 0
                    || degF.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL)) > 0
                    || degG.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL)) > 0) {
                return new Expression[0];
            }

            ExpressionCollection coefficientsF = SimplifyPolynomialUtils.getPolynomialCoefficients(f, var);
            ExpressionCollection coefficientsG = SimplifyPolynomialUtils.getPolynomialCoefficients(g, var);

            ExpressionCollection[] coefficientsOfEuclideanRepresentation = getEuclideanRepresentationOfGCDOfTwoPolynomials(coefficientsF, coefficientsG);
            if (coefficientsOfEuclideanRepresentation.length == 2) {
                return new Expression[]{getPolynomialFromCoefficients(coefficientsOfEuclideanRepresentation[0], var), getPolynomialFromCoefficients(coefficientsOfEuclideanRepresentation[1], var)};
            }
            return new Expression[0];

        } catch (EvaluationException e) {
            return new Expression[0];
        }

    }

    /**
     * Gibt die Eukliddarstellung zweier Polynome f und g, die durch ihre
     * Koeffizienten a und b gegeben sind, zurück: ist d = gcd(f, g), so wird
     * ein Polynomarray {a, b} zurückgegeben mit der Eigenschaft, dass a*f + b*g
     * = d. Dabei soll d stets normiert.
     */
    private static ExpressionCollection[] getEuclideanRepresentationOfGCDOfTwoPolynomials(ExpressionCollection a, ExpressionCollection b) {

        if (b.getBound() > a.getBound()) {
            ExpressionCollection[] euclideanRepresentationOfFormerStep = getEuclideanRepresentationOfGCDOfTwoPolynomials(b, a);
            if (euclideanRepresentationOfFormerStep.length == 2) {
                return new ExpressionCollection[]{euclideanRepresentationOfFormerStep[1], euclideanRepresentationOfFormerStep[0]};
            } else {
                return new ExpressionCollection[0];
            }
        }

        ExpressionCollection[] quotient;
        try {
            quotient = polynomialDivision(a, b);
        } catch (EvaluationException e) {
            return new ExpressionCollection[0];
        }

        ExpressionCollection coefficientsOfSecondFactor;
        if (quotient[1].isEmpty()) {

            // Polynomdivision hinterlässt keinen Rest.
            try {
                return new ExpressionCollection[]{new ExpressionCollection(ZERO), new ExpressionCollection(ONE.div(b.get(b.getBound() - 1)).simplify())};
            } catch (EvaluationException e) {
                return new ExpressionCollection[0];
            }

        } else {

            // Sonstiger Fall (Euklid-Darstellung wird rekursiv berechnet).
            ExpressionCollection[] euclideanRepresentationOfFormerStep = getEuclideanRepresentationOfGCDOfTwoPolynomials(b, quotient[1]);
            if (euclideanRepresentationOfFormerStep.length == 2) {
                try {
                    coefficientsOfSecondFactor = subtractPolynomials(euclideanRepresentationOfFormerStep[0],
                            multiplyPolynomials(euclideanRepresentationOfFormerStep[1], quotient[0]));
                    return new ExpressionCollection[]{euclideanRepresentationOfFormerStep[1], coefficientsOfSecondFactor};
                } catch (EvaluationException e) {
                    return new ExpressionCollection[0];
                }
            } else {
                return new ExpressionCollection[0];
            }

        }

    }

    /**
     * Gibt die Eukliddarstellung dreier Polynome f, g und h zurück: es wird ein
     * Expression-Array {a, b} zurückgegeben, so dass a*f + b*g = h gilt mit
     * deg(a), deg(b) &#8804; max(deg(f), deg(g)). Ist dies nicht möglich, so
     * wird ein leeres Expression-Arrayegeben.
     */
    public static Expression[] getOptimalEuclideanRepresentation(Expression f, Expression g, Expression h, String var) {

        try {

            if (!isPolynomialAdmissibleForComputation(f, var) || !isPolynomialAdmissibleForComputation(g, var) || !isPolynomialAdmissibleForComputation(h, var)) {
                return new Expression[0];
            }

            ExpressionCollection coefficientsF = SimplifyPolynomialUtils.getPolynomialCoefficients(f, var);
            ExpressionCollection coefficientsG = SimplifyPolynomialUtils.getPolynomialCoefficients(g, var);
            ExpressionCollection coefficientsH = SimplifyPolynomialUtils.getPolynomialCoefficients(h, var);

            ExpressionCollection gcdOfFAndG = getGGTOfPolynomials(coefficientsF, coefficientsG);
            ExpressionCollection[] quotient = SimplifyPolynomialUtils.polynomialDivision(coefficientsH, gcdOfFAndG);
            if (!quotient[1].isEmpty()) {
                return new Expression[0];
            }

            // Für a = coFactorF und b = coFactorG gilt a*f + b*g = 0.
            ExpressionCollection coFactorF = SimplifyPolynomialUtils.polynomialDivision(coefficientsG, gcdOfFAndG)[0];
            ExpressionCollection coFactorG = SimplifyPolynomialUtils.polynomialDivision(coefficientsF, gcdOfFAndG)[0];
            coFactorG.multiplyWithExpression(MINUS_ONE);
            coFactorG = coFactorG.simplify(simplifyTypesPolynomialDivisionEuklidRepresentation);

            ExpressionCollection[] coefficientsOfEuclideanRepresentation = getEuclideanRepresentationOfGCDOfTwoPolynomials(coefficientsF, coefficientsG);

            // Berechnung einer Euklid-Darstellung von h. Diese ist noch nicht optimal.
            coefficientsOfEuclideanRepresentation[0] = multiplyPolynomials(coefficientsOfEuclideanRepresentation[0], quotient[0]);
            coefficientsOfEuclideanRepresentation[1] = multiplyPolynomials(coefficientsOfEuclideanRepresentation[1], quotient[0]);

            if (coefficientsOfEuclideanRepresentation.length == 2) {

                // Gradkorrektur / Optimierung der Euklid-Darstellung.
                ExpressionCollection a = coefficientsOfEuclideanRepresentation[0];
                ExpressionCollection b = coefficientsOfEuclideanRepresentation[1];
                int degLeftSide = Math.max(a.getBound() + coefficientsF.getBound() - 2, b.getBound() + coefficientsG.getBound() - 2);
                int degRightSide = coefficientsH.getBound() - 1;

                ExpressionCollection multipleOfCoFactorF, multipleOfCoFactorG;
                ExpressionCollection factor = new ExpressionCollection();

                /* 
                Da der Grad auf der linken Seite bei jedem Schritt um mindestens 1 fällt,
                ist die maximale Anzahl an Reduktionen gleich degLeftSide - (degF + degCoFactorF) 
                = degLeftSide - (degG + degCcoFactorG).
                 */
                for (int i = 0; i < degLeftSide - degRightSide; i++) {
                    if (a.getBound() >= coFactorF.getBound()) {
                        factor.clear();
                        factor.put(a.getBound() - coFactorF.getBound(), a.getLast().div(coFactorF.getLast()).simplify(simplifyTypesPolynomialDivisionEuklidRepresentation));
                        multipleOfCoFactorF = multiplyPolynomials(coFactorF, factor);
                        a = subtractPolynomials(a, multipleOfCoFactorF);
                        multipleOfCoFactorG = multiplyPolynomials(coFactorG, factor);
                        b = subtractPolynomials(b, multipleOfCoFactorG);
                    } else if (b.getBound() >= coFactorG.getBound()) {
                        factor.clear();
                        factor.put(b.getBound() - coFactorG.getBound(), b.getLast().div(coFactorG.getLast()).simplify(simplifyTypesPolynomialDivisionEuklidRepresentation));
                        multipleOfCoFactorG = multiplyPolynomials(coFactorG, factor);
                        b = subtractPolynomials(b, multipleOfCoFactorG);
                        multipleOfCoFactorF = multiplyPolynomials(coFactorF, factor);
                        a = subtractPolynomials(a, multipleOfCoFactorF);
                    } else {
                        break;
                    }
                }

                return new Expression[]{getPolynomialFromCoefficients(a, var), getPolynomialFromCoefficients(b, var)};

            }

            return new Expression[0];

        } catch (EvaluationException e) {
            return new Expression[0];
        }

    }

    /**
     * Liefert die kleinste Periode, unter welcher die Koeffizienten
     * coefficients periodisch sind.<br>
     * BEISPIEL: Sind [1,3,-17,2,3,9,1,3,-17,2,3,9] die Koeffizienten von einem
     * Polynom f (in aufsteigender Reihenfolge), so wird 6 zurückgegeben.
     */
    public static int getPeriodOfCoefficients(ExpressionCollection coefficients) {

        if (coefficients.isEmpty()) {
            return 0;
        }

        int l = coefficients.getBound();
        List<BigInteger> cycleLengths = ArithmeticUtils.getDivisors(BigInteger.valueOf(l));
        ExpressionCollection periodForCompare;
        ExpressionCollection currentPeriod;

        boolean periodFound;
        for (BigInteger cycleLength : cycleLengths) {
            periodForCompare = coefficients.copy(0, cycleLength.intValue());
            periodFound = true;
            for (int j = 1; j < coefficients.getBound() / cycleLength.intValue(); j++) {
                currentPeriod = coefficients.copy(j * cycleLength.intValue(), (j + 1) * cycleLength.intValue());
                if (!SimplifyUtilities.equivalent(periodForCompare, currentPeriod)) {
                    periodFound = false;
                    break;
                }
            }
            if (periodFound) {
                return cycleLength.intValue();
            }
        }
        return coefficients.getBound();

    }

    /**
     * Liefert die kleinste Periode, unter welcher die Koeffizienten
     * coefficients antiperiodisch sind.<br>
     * BEISPIEL: Sind [1,3,-17,-1,-3,17,1,3,-17,-1,-3,17] die Koeffizienten von
     * einem Polynom f (in aufsteigender Reihenfolge), so wird 3 zurückgegeben.
     */
    public static int getAntiperiodOfCoefficients(ExpressionCollection coefficients) {

        if (coefficients.isEmpty()) {
            return 0;
        }

        int l = coefficients.getBound();
        List<BigInteger> cycleLengths = ArithmeticUtils.getDivisors(BigInteger.valueOf(l));
        ExpressionCollection periodForCompare;
        ExpressionCollection currentPeriod;
        Expression sum;

        boolean periodFound;
        for (BigInteger cycleLength : cycleLengths) {
            periodForCompare = coefficients.copy(0, cycleLength.intValue());
            periodFound = true;
            for (int j = 1; j < coefficients.getBound() / cycleLength.intValue(); j++) {
                currentPeriod = coefficients.copy(j * cycleLength.intValue(), (j + 1) * cycleLength.intValue());
                if (j % 2 == 1) {
                    for (int k = 0; k < periodForCompare.getBound(); k++) {
                        try {
                            sum = periodForCompare.get(k).add(currentPeriod.get(k)).simplify();
                            if (!sum.equals(ZERO)) {
                                periodFound = false;
                                break;
                            }
                        } catch (EvaluationException e) {
                            periodFound = false;
                            break;
                        }
                    }
                } else if (!SimplifyUtilities.equivalent(periodForCompare, currentPeriod)) {
                    periodFound = false;
                    break;
                }
                if (!periodFound) {
                    break;
                }
            }
            if (periodFound) {
                return cycleLength.intValue();
            }
        }
        return coefficients.getBound();

    }

    /**
     * Liefert den ggT aller Exponenten nichttrivialer Monome des Polynoms mit
     * Koeffizienten coefficients.<br>
     * BEISPIEL: Beim Polynom x^15 + 2*x^10 + 7*x^5 + 1 wird der Wert 5
     * zurückgegeben.
     */
    public static int getGGTOfAllExponents(ExpressionCollection coefficients) {
        int result = 0;
        for (int i = 0; i < coefficients.getBound(); i++) {
            if (i != 0 && !coefficients.get(i).equals(Expression.ZERO)) {
                if (result == 0) {
                    result = i;
                } else {
                    result = ArithmeticUtils.gcd(result, i);
                }
            }
        }
        return result;
    }

    /**
     * Gibt die Resultante res(f,g) zweier Polynome f und g, welche durch ihre
     * Koeffizienten gegeben sind, als MatrixExpression (genauer: als eine
     * Determinante) zurück.
     *
     * @throws EvaluationException
     */
    public static MatrixExpression getResultant(ExpressionCollection coefficientsOfF, ExpressionCollection coefficientsOfG) throws EvaluationException {

        int degF = coefficientsOfF.getBound() - 1;
        int degG = coefficientsOfG.getBound() - 1;

        Expression[][] resMatrixEntries = new Expression[degF + degG][degF + degG];

        for (int i = 0; i < degG; i++) {
            for (int j = 0; j < degF + degG; j++) {
                if (j < i || j > i + degF) {
                    resMatrixEntries[i][j] = ZERO;
                } else {
                    resMatrixEntries[i][j] = coefficientsOfF.get(degF + i - j);
                }
            }
        }
        for (int i = degG; i < degF + degG; i++) {
            for (int j = 0; j < degF + degG; j++) {
                if (i - j < 0 || i - j > degG) {
                    resMatrixEntries[i][j] = ZERO;
                } else {
                    resMatrixEntries[i][j] = coefficientsOfG.get(i - j);
                }
            }
        }

        MatrixExpression resultant = new Matrix(resMatrixEntries).det().simplify();
        return resultant;

    }

}
