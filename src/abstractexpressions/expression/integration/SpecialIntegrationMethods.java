package abstractexpressions.expression.integration;

import abstractexpressions.expression.computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import exceptions.NotAlgebraicallyIntegrableException;
import exceptions.NotSubstitutableException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.TypeOperator;
import enums.TypeSimplify;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.equation.SolveGeneralEquationMethods;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyExponentialRelations;
import abstractexpressions.expression.utilities.SimplifyPolynomialMethods;
import abstractexpressions.expression.utilities.SimplifyRationalFunctionMethods;
import abstractexpressions.expression.utilities.SimplifyTrigonometricalRelations;
import abstractexpressions.expression.utilities.SimplifyUtilities;
import static abstractexpressions.expression.integration.GeneralIntegralMethods.indefiniteIntegration;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import abstractexpressions.expression.substitution.SubstitutionUtilities;
import exceptions.MathToolException;

/**
 * Klasse mit Methoden für die Integration spezieller Funktionstypen.
 */
public abstract class SpecialIntegrationMethods extends GeneralIntegralMethods {

    /**
     * Private Fehlerklasse für den Fall, dass ein Ausdruck keine rationale
     * Funktion in x und einer Quadratwurzel aus einer quadratischen Funktion in
     * x (x = var) ist.
     */
    private static class NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException extends MathToolException {

        private static final String NOT_RATIONAL_FUNCTION_IN_VAR_AND_SQRT_OF_QUADRATIC_FUNCTION_MESSAGE = "Expression is not a rational function in variable and the square root of a quadratic function.";

        public NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException() {
            super(NOT_RATIONAL_FUNCTION_IN_VAR_AND_SQRT_OF_QUADRATIC_FUNCTION_MESSAGE);
        }

    }

    /**
     * Private Fehlerklasse für den Fall, dass ein Ausdruck keine rationale
     * Funktion in x und einer weiteren algebraischen Funktion in x (x = var)
     * ist.
     */
    private static class NotRationalFunctionInVarAndAnotherAlgebraicFunctionException extends MathToolException {

        private static final String NOT_RATIONAL_FUNCTION_IN_VAR_AND_ANOTHER_ALGEBRAIC_FUNCTION_MESSAGE = "Expression is not a rational function in variable and another algebraic function.";

        public NotRationalFunctionInVarAndAnotherAlgebraicFunctionException() {
            super(NOT_RATIONAL_FUNCTION_IN_VAR_AND_ANOTHER_ALGEBRAIC_FUNCTION_MESSAGE);
        }

    }

    /**
     * Private Fehlerklasse für den Fall, dass ein Ausdruck keine rationale
     * Funktion in einer rationalen Potenz von x (x = var) ist.
     */
    private static class NotRationalFunctionInRationalPowerOfVarException extends MathToolException {

        private static final String NOT_RATIONAL_FUNCTION_IN_RATIONAL_POWER_OF_VAR_MESSAGE = "Expression is not a rational function in variable and another algebraic function.";

        public NotRationalFunctionInRationalPowerOfVarException() {
            super(NOT_RATIONAL_FUNCTION_IN_RATIONAL_POWER_OF_VAR_MESSAGE);
        }

    }

    private static final HashSet<TypeSimplify> simplifyTypesRationalTrigonometricalFunction = getSimplifyTypesRationalTrigonometricalFunction();
    private static final HashSet<TypeSimplify> simplifyTypesExpandProductOfComplexExponentialFunction = getSimplifyTypesExpandProductOfComplexExponentialFunction();
    private static final HashSet<TypeSimplify> simplifyTypesAlgebraicFunction = getSimplifyTypesAlgebraicFunction();

    private static HashSet<TypeSimplify> getSimplifyTypesRationalTrigonometricalFunction() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_expand_moderate);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_multiply_exponents);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypes.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypes.add(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions);
        return simplifyTypes;
    }

    private static HashSet<TypeSimplify> getSimplifyTypesExpandProductOfComplexExponentialFunction() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypes.add(TypeSimplify.simplify_expand_products_of_complex_exponential_functions);
        simplifyTypes.add(TypeSimplify.simplify_expand_moderate);
        return simplifyTypes;
    }

    private static HashSet<TypeSimplify> getSimplifyTypesAlgebraicFunction() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_multiply_exponents);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_factorize);
        simplifyTypes.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypes.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypes.add(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions);
        return simplifyTypes;
    }

    /**
     * Integration rationaler Funktionen.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integrateRationalFunction(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        // Falls f keine rationale Function ist -> abbrechen.
        if (!PartialFractionDecompositionMethods.isRationalFunctionInCanonicalForm(f, var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        /*
         Im Folgenden sind nur rationale Funktionen zugelassen: der Nenner
         enthält keine Parameter, der Zähler darf welche enthalten.
         */
        HashSet<String> varsInDenominator = new HashSet<>();
        ((BinaryOperation) f).getRight().addContainedVars(varsInDenominator);
        if (!varsInDenominator.contains(var) || varsInDenominator.size() > 1) {
            /*
             Dies trifft AUCH DANN zu, wenn der Nenner Parameter enthält, aber
             von var nicht abhängt. Dann ist der Nenner bzgl. var konstant und
             kann vor das Integral getragen werden, was NICHT hier, sondern in
             GeneralIntegralMethods.takeConstantsOutOfIntegral() geschieht.
             Daher -> beenden.
             */
            throw new NotAlgebraicallyIntegrableException();
        }

        BigInteger degNnumerator = SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
        BigInteger degDenominator = SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) f).getRight(), var);

        // Nur bei Graden <= gewisse Schranke fortfahren.
        if (degNnumerator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0
                || degDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression partialFractionDecompositionOfF = PartialFractionDecompositionMethods.getPartialFractionDecomposition(f, var);

        if (f.equals(partialFractionDecompositionOfF)) {
            // Dann konnte f nicht in Partialbrüche zerlegt werden.
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection summands = SimplifyUtilities.getSummands(partialFractionDecompositionOfF);

        for (int i = 0; i < summands.getBound(); i++) {
            summands.put(i, new Operator(TypeOperator.integral, new Object[]{summands.get(i), var}).simplifyBasic());
        }

        return SimplifyUtilities.produceSum(summands);

    }

    /**
     * Integriert (a*x + b)/(c*x^2 + d*x + e)^n mit ganzem n >= 2 und
     * ireduziblem Nenner.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integrateQuotientOfLinearPolynomialAndPowerOfQuadraticPolynomial(Operator expr)
            throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient() || ((BinaryOperation) f).getRight().isNotPower()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().isPositiveIntegerConstant()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        BigInteger degNumerator = SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
        BigInteger degDenominator = SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);

        if (degNumerator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0
                || degDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection coefficientsNumerator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
        ExpressionCollection coefficientsDenominator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);
        BigInteger exponent = ((Constant) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getBigIntValue();

        if (exponent.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotAlgebraicallyIntegrableException();
        }

        int n = ((Constant) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getValue().intValue();

        if (coefficientsNumerator.getBound() > 2 || coefficientsDenominator.getBound() != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // In den folgenden Kommentaren sei a = coefficientsNumerator, b = coefficientsDenominator.
        Expression denominator = SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsDenominator, var);

        // Falls in den Leitkoeffizienten des Nenners Parameter auftreten, die das Vorzeichen nicht eindeutig machen -> Fehler werfen.
        if (!coefficientsDenominator.get(2).isAlwaysPositive() && !coefficientsDenominator.get(2).isAlwaysNegative()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Falls bei a irgendwelche Koeffizienten fehlen -> mit Nullen auffüllen.
        if (coefficientsNumerator.getBound() < 2) {
            for (int i = coefficientsNumerator.getBound(); i < 2; i++) {
                coefficientsNumerator.put(i, Expression.ZERO);
            }
        }

        Expression a = coefficientsDenominator.get(2);
        Expression b = coefficientsDenominator.get(1);
        Expression c = coefficientsDenominator.get(0);
        Expression d = coefficientsNumerator.get(1);
        Expression e = coefficientsNumerator.get(0);

        /*
         Falls der Nenner reduzibel ist -> Falsche Methode (es muss auf
         Partialbruchzerlegung zurückgegriffen werden).
         */
        // discriminant = 4*a*c*-b^2.
        Expression discriminant = new Constant(4).mult(a).mult(c).sub(b.pow(2)).simplify();
        if (!discriminant.isAlwaysPositive()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        /*
         Im Folgenden sei: a = b.get(1), b = b.get(0), c = b.get(2), d =
         a.get(1), e = a.get(0), D = discriminant, x = var.
         */
        // firstSummand = ((2*a*e-b*d)*x + (e*b-2*c*d))/((n - 1)*D*(a*x^2 + b*x + c)^(n - 1))
        Expression firstSummand = TWO.mult(a).mult(e).sub(b.mult(d)).mult(Variable.create(var)).add(
                e.mult(b).sub(TWO.mult(c).mult(d))).div(
                        new Constant(n - 1).mult(discriminant).mult(denominator.pow(n - 1))).simplify();

        // factor = (2*n - 3)*(2*a*e - b*d)/((n - 1)*D)
        Expression factor = new Constant(2 * n - 3).mult(TWO.mult(a).mult(e).sub(b.mult(d))).div(new Constant(n - 1).mult(discriminant)).simplify();

        // Integral = firstSummand + factor*int(1/(a*x^2+b*x+c)^(n-1),x).
        Object[] params = new Object[2];
        params[0] = ONE.div(denominator.pow(n - 1));
        params[1] = var;
        Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);

        return firstSummand.add(factor.mult(integralOfLowerPower));

    }

    /**
     * Integriert (a*x + b)/(c*x^2 + d*x + e) mit ireduziblem Nenner.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integrateQuotientOfLinearPolynomialAndQuadraticPolynomial(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getRight(), var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        BigInteger degNumerator = SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
        BigInteger degDenominator = SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) f).getRight(), var);

        if (degNumerator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0
                || degDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection coefficientsNumerator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
        ExpressionCollection coefficientsDenominator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) f).getRight(), var);

        if (coefficientsNumerator.getBound() > 2 || coefficientsDenominator.getBound() != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // In den folgenden Kommentaren sei a = coefficientsNumerator, b = coefficientsDenominator.
        // Falls in den Leitkoeffizienten des Nenners Parameter auftreten, die das Vorzeichen nicht eindeutig machen -> Fehler werfen.
        if (!coefficientsDenominator.get(2).isAlwaysPositive() && !coefficientsDenominator.get(2).isAlwaysNegative()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Falls bei a irgendwelche Koeffizienten fehlen -> mit Nullen auffüllen.
        if (coefficientsNumerator.getBound() < 2) {
            for (int i = coefficientsNumerator.getBound(); i < 2; i++) {
                coefficientsNumerator.put(i, Expression.ZERO);
            }
        }

        /*
         Falls der Nenner reduzibel ist -> Falsche Methode (es muss auf
         Partialbruchzerlegung zurückgegriffen werden).
         */
        // discriminant = b^2 - 4*a*c =: D.
        Expression discriminant = coefficientsDenominator.get(1).pow(2).sub(new Constant(4).mult(coefficientsDenominator.get(0)).mult(coefficientsDenominator.get(2))).simplify();
        if (!discriminant.isAlwaysNegative() || discriminant.equals(Expression.ZERO)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        /*
         Hilfsgrößen: a = b.get(2), b = b.get(1), c = b.get(0), d = a.get(1), e = a.get(0).
         p = d/(2*a), q = e/a - d*b/(2*a^2), r = (-D/(4*a^2))^(1/2). 
         DANN: int(dx + e)/(ax^2 + bx + c) 
         = p*ln(ax^2 + bx + c) + (q/r)*arctan((2ax + b)/(sgn(a)*(-D)^(1/2))) mit D = b^2 - 4ac.
         */
        Expression a = coefficientsDenominator.get(2);
        Expression b = coefficientsDenominator.get(1);
        Expression c = coefficientsDenominator.get(0);
        Expression d = coefficientsNumerator.get(1);
        Expression e = coefficientsNumerator.get(0);

        Expression p = d.div(TWO.mult(a)).simplify();
        Expression q = e.div(a).sub(d.mult(b).div(TWO.mult(a.pow(2)))).simplify();
        Expression r = MINUS_ONE.mult(discriminant).pow(1, 2).div(TWO.mult(a.abs())).simplify();

        // Log-Summanden bilden.
        Expression logSummand = p.mult(((BinaryOperation) f).getRight().abs().ln());
        // Arctan-Summanden bilden.
        Expression arctanArgument = TWO.mult(a).mult(Variable.create(var)).add(b).div(a.sgn().mult(MINUS_ONE.mult(discriminant).pow(1, 2))).simplify();
        Expression arctanSummand = q.mult(arctanArgument.arctan()).div(r).simplify();

        return logSummand.add(arctanSummand);

    }

    // Weitere Typen.
    private static boolean isExponentialFunction(Expression f, String var) {
        return f.isFunction(TypeFunction.exp) && SimplifyPolynomialMethods.isPolynomial(((Function) f).getLeft(), var)
                && SimplifyPolynomialMethods.getDegreeOfPolynomial(((Function) f).getLeft(), var).compareTo(BigInteger.ONE) == 0;
    }

    private static boolean isTrigonometricalFunction(Expression f, String var) {
        return (f.isFunction(TypeFunction.cos) || f.isFunction(TypeFunction.sin)) && SimplifyPolynomialMethods.isPolynomial(((Function) f).getLeft(), var)
                && SimplifyPolynomialMethods.getDegreeOfPolynomial(((Function) f).getLeft(), var).compareTo(BigInteger.ONE) == 0;
    }

    private static boolean isSumOfProductsOfExponentialAndTrigonometricalFunctions(Expression f, String var) {

        ExpressionCollection summands = SimplifyUtilities.getSummandsLeftInExpression(f);
        summands.addAll(SimplifyUtilities.getSummandsRightInExpression(f));

        ExpressionCollection factorsNumerator, factorsDenominator;
        int numberOfExpFactors, numberOfTrigonometricalFactors;
        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) == null || !summands.get(i).contains(var)) {
                continue;
            }
            factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(summands.get(i));
            factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(i));
            numberOfExpFactors = 0;
            numberOfTrigonometricalFactors = 0;
            for (int j = 0; j < factorsNumerator.getBound(); j++) {
                if (factorsNumerator.get(j).contains(var) && !isExponentialFunction(factorsNumerator.get(j), var)
                        && !isTrigonometricalFunction(factorsNumerator.get(j), var)) {
                    return false;
                }
                if (isExponentialFunction(factorsNumerator.get(j), var)) {
                    numberOfExpFactors++;
                }
                if (isTrigonometricalFunction(factorsNumerator.get(j), var)) {
                    numberOfTrigonometricalFactors++;
                }
            }
            for (int j = 0; j < factorsDenominator.getBound(); j++) {
                if (factorsDenominator.get(j).contains(var)) {
                    return false;
                }
            }
            if (numberOfExpFactors > 1 || numberOfTrigonometricalFactors > 1) {
                return false;
            }
        }

        return true;

    }

    private static Expression expandProductsOfComplexExponentialFunctions(Expression f, String var) throws EvaluationException {
        return f.simplify(simplifyTypesExpandProductOfComplexExponentialFunction, var);
    }

    /**
     * Integriert Funktionen vom Typ P(x) * f(a*x+b)^n, P = Polynom, f = sin
     * oder f = cos, wobei der Polynomgrad unterhalb einer geeigneten Schranke
     * liegt.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integrateProductOfPolynomialAndPowerOfTrigonometricFunction(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotProduct()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(f);

        if (factors.getBound() != 2) {
            throw new NotAlgebraicallyIntegrableException();
        }

        if (SimplifyPolynomialMethods.isPolynomial(factors.get(1), var)) {

            // Beide Faktoren vertauschen und noch einmal versuchen.
            Expression factorLeft = factors.get(0);
            Expression factorRight = factors.get(1);
            factors.put(0, factorRight);
            factors.put(1, factorLeft);
            Operator exprFactorsInterchanged = new Operator(TypeOperator.integral,
                    new Object[]{SimplifyUtilities.produceProduct(factors), var}, expr.getPrecise());
            return integrateProductOfPolynomialAndPowerOfTrigonometricFunction(exprFactorsInterchanged);

        }

        if (SimplifyPolynomialMethods.isPolynomial(factors.get(0), var)
                && factors.get(1).isPower()
                && ((BinaryOperation) factors.get(1)).getRight().isIntegerConstant()
                && ((BinaryOperation) factors.get(1)).getRight().isPositive()
                && (((BinaryOperation) factors.get(1)).getLeft().isFunction(TypeFunction.cos)
                || ((BinaryOperation) factors.get(1)).getLeft().isFunction(TypeFunction.sin))
                && SimplifyPolynomialMethods.isPolynomial(((Function) ((BinaryOperation) factors.get(1)).getLeft()).getLeft(), var)) {

            BigInteger degPolynomial = SimplifyPolynomialMethods.getDegreeOfPolynomial(factors.get(0), var);
            BigInteger exponentOfTrigonometricFunction = ((Constant) ((BinaryOperation) factors.get(1)).getRight()).getBigIntValue();
            BigInteger degArgument = SimplifyPolynomialMethods.getDegreeOfPolynomial(((Function) ((BinaryOperation) factors.get(1)).getLeft()).getLeft(), var);

            if (degPolynomial.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0
                    || exponentOfTrigonometricFunction.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0
                    || degArgument.compareTo(BigInteger.ONE) > 0) {
                throw new NotAlgebraicallyIntegrableException();
            }

            f = expandProductsOfComplexExponentialFunctions(f, var);
            Operator exprExpanded = new Operator(TypeOperator.integral, new Object[]{f, var});
            // Jetzt gelingt eine Integration mittels partieller Integration.
            return indefiniteIntegration(exprExpanded, true);

        }

        throw new NotAlgebraicallyIntegrableException();

    }

    /**
     * Integriert Funktionen vom Typ Polynom in Exponentialfunktionen und
     * trigonometrischen Funktionen, falls der Polynomgrad unterhalb einer
     * geeigneten Schranke liegt.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integratePolynomialInComplexExponentialFunctions(Operator expr) throws NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (!BinaryOperation.isPolynomialInVariousExponentialAndTrigonometricalFunctions(f, var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        try {
            // Nun Potenzen von sin und cos durch Summen von sin und cos ersetzen.
            Expression fExpanded = expandProductsOfComplexExponentialFunctions(f, var);
            if (isSumOfProductsOfExponentialAndTrigonometricalFunctions(fExpanded, var)) {
                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(fExpanded);
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(fExpanded);
                for (int i = 0; i < summandsLeft.getBound(); i++) {
                    summandsLeft.put(i, integrateSummandOfPolynomialInExponentialAndTrigonometricalFunctions(summandsLeft.get(i), var));
                }
                for (int i = 0; i < summandsRight.getBound(); i++) {
                    summandsRight.put(i, integrateSummandOfPolynomialInExponentialAndTrigonometricalFunctions(summandsRight.get(i), var));
                }
                return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);
            }
        } catch (EvaluationException e) {
        }

        throw new NotAlgebraicallyIntegrableException();

    }

    private static Expression integrateSummandOfPolynomialInExponentialAndTrigonometricalFunctions(Expression f, String var) throws EvaluationException, NotAlgebraicallyIntegrableException {

        // Zuerst: Fall eines konstanten Summanden:
        if (!f.contains(var)) {
            return f.mult(Variable.create(var));
        }

        ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(f);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
        ExpressionCollection constantfactorsNumerator = new ExpressionCollection();
        ExpressionCollection constantfactorsDenominator = new ExpressionCollection();

        for (int i = 0; i < factorsNumerator.getBound(); i++) {
            if (!factorsNumerator.get(i).contains(var)) {
                constantfactorsNumerator.add(factorsNumerator.get(i));
                factorsNumerator.remove(i);
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (!factorsDenominator.get(i).contains(var)) {
                constantfactorsDenominator.add(factorsDenominator.get(i));
                factorsDenominator.remove(i);
            }
        }

        if (!factorsDenominator.isEmpty() || factorsNumerator.getSize() > 2) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Jetzt wird die Stammfunktion, abhängig von der Gestalt des Summanden, explizit bestimmt.
        ExpressionCollection factors = new ExpressionCollection(factorsNumerator);
        Expression constantNumerator = SimplifyUtilities.produceProduct(constantfactorsNumerator);
        Expression constantDenominator = SimplifyUtilities.produceProduct(constantfactorsDenominator);

        // Fall: Integration von exp(a*x+b).
        if (factors.getBound() == 1 && factors.get(0).isFunction(TypeFunction.exp)) {
            Expression integral = integrateExpOfLinearFunction(((Function) factors.get(0)).getLeft(), var);
            return constantNumerator.mult((Expression) integral).div(constantDenominator);
        }
        // Fall: Integration von cos(a*x+b).
        if (factors.getBound() == 1 && factors.get(0).isFunction(TypeFunction.cos)) {
            Expression integral = integrateCosOfLinearFunction(((Function) factors.get(0)).getLeft(), var);
            return constantNumerator.mult((Expression) integral).div(constantDenominator);
        }
        // Fall: Integration von sin(a*x+b).
        if (factors.getBound() == 1 && factors.get(0).isFunction(TypeFunction.sin)) {
            Expression integral = integrateSinOfLinearFunction(((Function) factors.get(0)).getLeft(), var);
            return constantNumerator.mult((Expression) integral).div(constantDenominator);
        }
        // Fall: Integration von exp(a*x+b)*cos(c*x+d).
        if (factors.getBound() == 2 && factors.get(0).isFunction(TypeFunction.exp) && factors.get(1).isFunction(TypeFunction.cos)) {
            Expression integral = integrateProductOfExpCos(((Function) factors.get(0)).getLeft(), ((Function) factors.get(1)).getLeft(), var);
            return constantNumerator.mult((Expression) integral).div(constantDenominator);
        }
        // Fall: Integration von cos(a*x+b)*exp(c*x+d).
        if (factors.getBound() == 2 && factors.get(0).isFunction(TypeFunction.cos) && factors.get(1).isFunction(TypeFunction.exp)) {
            Expression integral = integrateProductOfExpCos(((Function) factors.get(1)).getLeft(), ((Function) factors.get(0)).getLeft(), var);
            return constantNumerator.mult((Expression) integral).div(constantDenominator);
        }
        // Fall: Integration von exp(a*x+b)*sin(c*x+d).
        if (factors.getBound() == 2 && factors.get(0).isFunction(TypeFunction.exp) && factors.get(1).isFunction(TypeFunction.sin)) {
            Expression integral = integrateProductOfExpSin(((Function) factors.get(0)).getLeft(), ((Function) factors.get(1)).getLeft(), var);
            return constantNumerator.mult((Expression) integral).div(constantDenominator);
        }
        // Fall: Integration von sin(a*x+b)*exp(c*x+d).
        if (factors.getBound() == 2 && factors.get(0).isFunction(TypeFunction.sin) && factors.get(1).isFunction(TypeFunction.exp)) {
            Expression integral = integrateProductOfExpSin(((Function) factors.get(1)).getLeft(), ((Function) factors.get(0)).getLeft(), var);
            return constantNumerator.mult((Expression) integral).div(constantDenominator);
        }

        throw new NotAlgebraicallyIntegrableException();

    }

    /**
     * Explizite Integration von exp(a*x+b).<br>
     *
     * @throws EvaluationException
     */
    private static Expression integrateExpOfLinearFunction(Expression expArgument, String var) throws EvaluationException, NotAlgebraicallyIntegrableException {
        if (!SimplifyPolynomialMethods.isPolynomial(expArgument, var)
                || SimplifyPolynomialMethods.getDegreeOfPolynomial(expArgument, var).compareTo(BigInteger.ONE) != 0) {
            throw new NotAlgebraicallyIntegrableException();
        }
        ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(expArgument, var);
        return expArgument.exp().div(coefficients.get(1));
    }

    /**
     * Explizite Integration von cos(a*x+b).<br>
     *
     * @throws EvaluationException
     */
    private static Expression integrateCosOfLinearFunction(Expression expArgument, String var) throws EvaluationException, NotAlgebraicallyIntegrableException {
        if (!SimplifyPolynomialMethods.isPolynomial(expArgument, var)
                || SimplifyPolynomialMethods.getDegreeOfPolynomial(expArgument, var).compareTo(BigInteger.ONE) != 0) {
            throw new NotAlgebraicallyIntegrableException();
        }
        ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(expArgument, var);
        return expArgument.sin().div(coefficients.get(1));
    }

    /**
     * Explizite Integration von sin(a*x+b).<br>
     *
     * @throws EvaluationException
     */
    private static Expression integrateSinOfLinearFunction(Expression expArgument, String var) throws EvaluationException, NotAlgebraicallyIntegrableException {
        if (!SimplifyPolynomialMethods.isPolynomial(expArgument, var)
                || SimplifyPolynomialMethods.getDegreeOfPolynomial(expArgument, var).compareTo(BigInteger.ONE) != 0) {
            throw new NotAlgebraicallyIntegrableException();
        }
        ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(expArgument, var);
        return MINUS_ONE.mult(expArgument.cos()).div(coefficients.get(1));
    }

    /**
     * Integration von exp(a*x+b)*sin(c*x+d).
     *
     * @throws EvaluationException
     */
    public static Expression integrateProductOfExpSin(Expression expArgument, Expression sinArgument, String var) throws EvaluationException, NotAlgebraicallyIntegrableException {

        ExpressionCollection coefficientsInExp = SimplifyPolynomialMethods.getPolynomialCoefficients(expArgument, var);
        ExpressionCollection coefficientsInSin = SimplifyPolynomialMethods.getPolynomialCoefficients(sinArgument, var);
        if (coefficientsInExp.getBound() != 2 || coefficientsInSin.getBound() != 2) {
            throw new NotAlgebraicallyIntegrableException();
        }

        /*
         Es wird int(exp(a*x+b)*sin(c*x+d), x) =
         exp(a*x+b)*(a*sin(c*x+d)-c*cos(c*x+d))/(a^2+c^2) zurückgegeben.
         */
        return expArgument.exp().mult(coefficientsInExp.get(1).mult(sinArgument.sin()).sub(coefficientsInSin.get(1).mult(sinArgument.cos()))).div(
                coefficientsInExp.get(1).pow(2).add(coefficientsInSin.get(1).pow(2)));

    }

    /**
     * Integration von exp(a*x+b)*cos(c*x+d).
     *
     * @throws EvaluationException
     */
    public static Expression integrateProductOfExpCos(Expression expArgument, Expression cosArgument, String var) throws EvaluationException, NotAlgebraicallyIntegrableException {

        ExpressionCollection coefficientsInExp = SimplifyPolynomialMethods.getPolynomialCoefficients(expArgument, var);
        ExpressionCollection coefficientsInCos = SimplifyPolynomialMethods.getPolynomialCoefficients(cosArgument, var);
        if (coefficientsInExp.getBound() != 2 || coefficientsInCos.getBound() != 2) {
            throw new NotAlgebraicallyIntegrableException();
        }

        /*
         Es wird int(exp(a*x+b)*cos(c*x+d), x) =
         exp(a*x+b)*(a*cos(c*x+d)+c*sin(c*x+d))/(a^2+c^2) zurückgegeben.
         */
        return expArgument.exp().mult(coefficientsInExp.get(1).mult(cosArgument.cos()).add(coefficientsInCos.get(1).mult(cosArgument.sin()))).div(
                coefficientsInExp.get(1).pow(2).add(coefficientsInCos.get(1).pow(2)));

    }

    /**
     * Integration von (a*x^2 + b*x + c)^(1/2).<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallyIntegrableException
     */
    private static Expression integrateSqrtOfQuadraticFunction(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotPower() || !((BinaryOperation) f).getRight().equals(Expression.ONE.div(Expression.TWO))
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);

        if (coefficients.getBound() != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Falls in den Koeffizienten Parameter auftreten, die das Vorzeichen ändern können -> false zurückgeben.
        for (int i = 0; i < 2; i++) {
            if (!coefficients.get(i).isConstant() && !coefficients.get(i).isAlwaysNonNegative() && !(Expression.MINUS_ONE).mult(coefficients.get(i)).simplify().isAlwaysNonNegative()) {
                throw new NotAlgebraicallyIntegrableException();
            }
        }

        // Im Folgenden sei a = a.get(2), b = a.get(1), c = a.get(0), diskr = D = b^2 - 4*a*c.
        Expression diskriminant = coefficients.get(1).pow(2).sub(new Constant(4).mult(coefficients.get(2)).mult(coefficients.get(0))).simplify();

        if (diskriminant.equals(Expression.ZERO) && coefficients.get(2).isAlwaysNonNegative() && !coefficients.get(2).equals(Expression.ZERO)) {
            // Dann ist f = a^(1/2)*|x - x_1| mit x_1 = -b/(2*a).
            Expression zero = Expression.MINUS_ONE.mult(coefficients.get(1)).div(Expression.TWO.mult(coefficients.get(2))).simplify();
            // F = a^(1/2)*(x - x_1)*|x - x_1|/2.
            return coefficients.get(2).pow(1, 2).mult(Variable.create(var).sub(zero)).mult(Variable.create(var).sub(zero).abs()).div(2);
        }
        if (!diskriminant.equals(Expression.ZERO) && (diskriminant.isNonNegative() || diskriminant.isAlwaysNonNegative())) {
            // Hier ist D > 0.
            if ((coefficients.get(2).isNonNegative() || coefficients.get(2).isAlwaysNonNegative()) && !coefficients.get(2).equals(Expression.ZERO)) {

                // Fall a > 0, D > 0. Reduktion auf den Typ int((x^2 - 1)^(1/2), x).
                /*
                 F = (2*a*x + b)*(a*x^2 + b*x + c)^(1/2)/(4*a) -
                 D*arcosh((2*a*x + b)/D^(1/2))/(8*a^(3/2)).
                 */
                return Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).mult(
                        ((BinaryOperation) f).getLeft().pow(1, 2)).div(new Constant(4).mult(coefficients.get(2))).sub(
                                diskriminant.mult(Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).div(diskriminant.pow(1, 2)).arcosh()).div(
                                        new Constant(8).mult(coefficients.get(2).pow(3, 2))));

            }
            if ((coefficients.get(2).isNonPositive() || Expression.MINUS_ONE.mult(coefficients.get(2)).simplify().isAlwaysNonNegative()) && !coefficients.get(2).equals(Expression.ZERO)) {

                // Fall a < 0, D > 0. Reduktion auf den Typ int((1 - x^2)^(1/2), x).
                /*
                 F = (2*a*x + b)*(a*x^2 + b*x + c)^(1/2)/(4*a) +
                 D*arcsin((-2*a*x - b)/D^(1/2))/(8*(-a)^(3/2)).
                 */
                return Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).mult(
                        ((BinaryOperation) f).getLeft().pow(1, 2)).div(new Constant(4).mult(coefficients.get(2))).add(
                                diskriminant.mult((new Constant(-2)).mult(coefficients.get(2)).mult(Variable.create(var)).sub(coefficients.get(1)).div(diskriminant.pow(1, 2)).arcsin()).div(
                                        new Constant(8).mult((Expression.MINUS_ONE).mult(coefficients.get(2)).pow(3, 2))));

            }
        }
        if (!diskriminant.equals(Expression.ZERO) && (diskriminant.isNonPositive() || Expression.MINUS_ONE.mult(diskriminant).simplify().isAlwaysNonNegative())
                && (coefficients.get(2).isNonNegative() || coefficients.get(2).isAlwaysNonNegative()) && !coefficients.get(2).equals(Expression.ZERO)) {

            // Fall a > 0, D < 0. Reduktion auf den Typ int((x^2 + 1)^(1/2), x).
            /*
             F = (2*a*x + b)*(a*x^2 + b*x + c)^(1/2)/(4*a) - D*arsinh((2*a*x +
             b)/(-D)^(1/2))/(8*a^(3/2)).
             */
            return Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).mult(
                    ((BinaryOperation) f).getLeft().pow(1, 2)).div(new Constant(4).mult(coefficients.get(2))).sub(
                            diskriminant.mult(Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).div(Expression.MINUS_ONE.mult(diskriminant).pow(1, 2)).arsinh()).div(
                                    new Constant(8).mult(coefficients.get(2).pow(3, 2))));

        }

        // Übrige, nicht eindeutig entscheidbare Fälle.
        throw new NotAlgebraicallyIntegrableException();

    }

    /**
     * Integration von (a*x^2 + b*x + c)^((2*n+1)/2).<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws exceptions.NotAlgebraicallyIntegrableException
     */
    public static Expression integrateOddPowerOfSqrtOfQuadraticPolynomial(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotPower()
                || !((BinaryOperation) f).getRight().isRationalConstant()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft().isOddIntegerConstant()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft().isPositive()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().equals(TWO)
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);

        if (coefficients.getBound() != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Falls in den Koeffizienten Parameter auftreten, die das Vorzeichen ändern können -> false zurückgeben.
        for (int i = 0; i < 2; i++) {
            if (!coefficients.get(i).isConstant() && !coefficients.get(i).isAlwaysNonNegative() && !(Expression.MINUS_ONE).mult(coefficients.get(i)).simplify().isAlwaysNonNegative()) {
                throw new NotAlgebraicallyIntegrableException();
            }
        }

        // Im Folgenden sei a = a.get(2), b = a.get(1), c = a.get(0), discriminant = D = b^2 - 4*a*c.
        Expression a = coefficients.get(2);
        Expression b = coefficients.get(1);
        Expression c = coefficients.get(0);
        Expression discriminant = b.pow(2).sub(new Constant(4).mult(a).mult(c)).simplify();

        BigInteger exponentNumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft()).getBigIntValue();

        if (exponentNumerator.compareTo(BigInteger.valueOf(2 * ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotAlgebraicallyIntegrableException();
        }

        int n = (((Constant) ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft()).getValue().intValue() - 1) / 2;

        if (n == 0) {
            return integrateSqrtOfQuadraticFunction(expr);
        }

        // firstSummand = (2*a*x + b)*(a*x^2+b*x+c)^((2*n+1)/2)/((4*n+4)*a)
        Expression firstSummand = (TWO.mult(a).mult(Variable.create(var)).add(b)).mult(f).div(new Constant(4 * n + 4).mult(a));

        // factor = ((2*n + 1)*discriminant)/((8*n + 8)*a)
        Expression factor = new Constant(2 * n + 1).mult(discriminant).div(new Constant(8 * n + 8).mult(a));

        // Integral = firstSummand - factor*int((a*x^2+b*x+c)^((2*n-1)/2),x).
        Object[] params = new Object[2];
        params[0] = ((BinaryOperation) f).getLeft().pow(2 * n - 1, 2);
        params[1] = var;
        Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);

        return firstSummand.sub(factor.mult(integralOfLowerPower));

    }

    /**
     * Integration von (A*x + B)*(a*x^2 + b*x + c)^((2*n+1)/2). Das quadratische
     * Polynom muss nicht irreduzibel sein.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws exceptions.NotAlgebraicallyIntegrableException
     */
    public static Expression integrateProductOfLinearPolynomialAndOddPowerOfSqrtOfQuadraticPolynomial(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (!isProductOfPolynomialAndOddPowerOfSqrtOfQuadraticFunction(f, var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection coefficientsPolynomial, coefficientsQuadraticPolynomial;
        Expression factorLeft, factorRight;
        int n;
        if (f.isProduct()) {
            factorLeft = ((BinaryOperation) f).getLeft();
            factorRight = ((BinaryOperation) f).getRight();
            if (SimplifyPolynomialMethods.isPolynomial(factorLeft, var)) {
                coefficientsPolynomial = SimplifyPolynomialMethods.getPolynomialCoefficients(factorLeft, var);
                coefficientsQuadraticPolynomial = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) factorRight).getLeft(), var);
                n = (((Constant) ((BinaryOperation) ((BinaryOperation) factorRight).getRight()).getLeft()).getValue().intValue() - 1) / 2;
            } else {
                coefficientsPolynomial = SimplifyPolynomialMethods.getPolynomialCoefficients(factorRight, var);
                coefficientsQuadraticPolynomial = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) factorLeft).getLeft(), var);
                n = (((Constant) ((BinaryOperation) ((BinaryOperation) factorLeft).getRight()).getLeft()).getValue().intValue() - 1) / 2;
            }
        } else {
            coefficientsPolynomial = new ExpressionCollection(ONE);
            coefficientsQuadraticPolynomial = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
            n = (((Constant) ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft()).getValue().intValue() - 1) / 2;
        }

        if (coefficientsPolynomial.getBound() > 2 || coefficientsQuadraticPolynomial.getBound() != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // coefficientsPolynomial bis zum Grad 1 mit Nullen auffüllen.
        for (int i = 0; i < 2; i++) {
            if (coefficientsPolynomial.get(i) == null) {
                coefficientsPolynomial.put(i, ZERO);
            }
        }

        Expression a = coefficientsQuadraticPolynomial.get(2);
        Expression b = coefficientsQuadraticPolynomial.get(1);
        Expression d = coefficientsPolynomial.get(1);
        Expression e = coefficientsPolynomial.get(0);
        /* 
         Man ist int((d*x+e)*(a*x^2+b*x+c)^((2*n+1)/2), x) = d/((2*n+3)*a)*(a*x^2+b*x+c)^((2*n+3)/2)
         + (e-d*b/(2*a))*int((a*x^2+b*x+c)^((2*n+1)/2), x)
         */
        Expression firstSummand = d.mult(SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsQuadraticPolynomial, var).pow(2 * n + 3, 2)).div(new Constant(2 * n + 3).mult(a));
        Expression factor = e.sub(d.mult(b).div(TWO.mult(a)));
        Object[] params = new Object[2];
        params[0] = SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsQuadraticPolynomial, var).pow(2 * n + 1, 2);
        params[1] = var;
        Expression integralOfLowerPower = integrateOddPowerOfSqrtOfQuadraticPolynomial(new Operator(TypeOperator.integral, params));
        return firstSummand.add(factor.mult(integralOfLowerPower));

    }

    /**
     * Integration von P(x)*(a*x^2 + b*x + c)^((2*n+1)/2). Das quadratische
     * Polynom muss nicht irreduzibel sein.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws exceptions.NotAlgebraicallyIntegrableException
     */
    public static Expression integrateProductOfPolynomialAndOddPowerOfSqrtOfQuadraticPolynomial(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (!isProductOfPolynomialAndOddPowerOfSqrtOfQuadraticFunction(f, var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection coefficientsPolynomial, coefficientsQuadraticPolynomial;
        Expression factorLeft, factorRight;
        int n;
        if (f.isProduct()) {
            factorLeft = ((BinaryOperation) f).getLeft();
            factorRight = ((BinaryOperation) f).getRight();
            if (SimplifyPolynomialMethods.isPolynomial(factorLeft, var)) {
                coefficientsPolynomial = SimplifyPolynomialMethods.getPolynomialCoefficients(factorLeft, var);
                coefficientsQuadraticPolynomial = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) factorRight).getLeft(), var);
                n = (((Constant) ((BinaryOperation) ((BinaryOperation) factorRight).getRight()).getLeft()).getValue().intValue() - 1) / 2;
            } else {
                coefficientsPolynomial = SimplifyPolynomialMethods.getPolynomialCoefficients(factorRight, var);
                coefficientsQuadraticPolynomial = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) factorLeft).getLeft(), var);
                n = (((Constant) ((BinaryOperation) ((BinaryOperation) factorLeft).getRight()).getLeft()).getValue().intValue() - 1) / 2;
            }
        } else {
            coefficientsPolynomial = new ExpressionCollection(ONE);
            coefficientsQuadraticPolynomial = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
            n = (((Constant) ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft()).getValue().intValue() - 1) / 2;
        }

        if (coefficientsQuadraticPolynomial.getBound() != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }

        int maxExponent = (coefficientsPolynomial.getBound() - 1) / 2;

        if (maxExponent == 0) {
            // f ist von der Form (A*x+B)*(a*x^2+b*x+c)^((2*n+1)/2).
            return integrateProductOfLinearPolynomialAndOddPowerOfSqrtOfQuadraticPolynomial(expr);
        }

        /*
         Sei k = maxExponent. Zunächst: Koeffizienten von (a*x^2 + b*x + c)^k
         bilden.
         */
        ExpressionCollection coefficientsOfPowerOfQuadraticPolynomial = getCoefficientsOfPowerOfPolynomial(coefficientsQuadraticPolynomial, maxExponent);
        ExpressionCollection[] quotient = SimplifyPolynomialMethods.polynomialDivision(coefficientsPolynomial, coefficientsOfPowerOfQuadraticPolynomial);

        /* 
         Man hat nun eine Darstellung der Form P(x) = (A*x+B)*(a*x^2+b*x+c)^k + Q(x)
         mit deg(Q) < 2*k. Daher: P(x)*(a*x^2+b*x+c)^((2*n+1)/2) = (A*x+B)*(a*x^2+b*x+c)^((2*n+2*k+1)/2)
         + Q(x)*(a*x^2+b*x+c)^n.
         */
        Expression firstSummand = SimplifyPolynomialMethods.getPolynomialFromCoefficients(quotient[0], var).mult(
                SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsQuadraticPolynomial, var).pow(2 * n + 2 * maxExponent + 1, 2));
        Expression secondSummand = SimplifyPolynomialMethods.getPolynomialFromCoefficients(quotient[1], var).mult(
                SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsQuadraticPolynomial, var).pow(2 * n + 1, 2));
        Object[] paramsFirstSummand = new Object[2];
        paramsFirstSummand[0] = firstSummand;
        paramsFirstSummand[1] = var;
        Expression integralOfFirstSummand = indefiniteIntegration(new Operator(TypeOperator.integral, paramsFirstSummand), true);
        Object[] paramsSecondSummand = new Object[2];
        paramsSecondSummand[0] = secondSummand;
        paramsSecondSummand[1] = var;
        Expression integralOfSecondSummand = indefiniteIntegration(new Operator(TypeOperator.integral, paramsSecondSummand), true);
        return integralOfFirstSummand.add(integralOfSecondSummand);

    }

    private static boolean isProductOfPolynomialAndOddPowerOfSqrtOfQuadraticFunction(Expression f, String var) {

        // Sonderfall: f = (a*x^2+b*x+c)^((2*n+1)/2).
        if (f.isPower()
                && ((BinaryOperation) f).getRight().isRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft().isOddIntegerConstant()
                && ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft().isPositive()
                && ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().equals(TWO)
                && SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)
                && SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) f).getLeft(), var).compareTo(BigInteger.valueOf(2)) == 0) {

            BigInteger exponentNumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft()).getBigIntValue();
            return exponentNumerator.compareTo(BigInteger.valueOf(2 * ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) < 0;

        }

        // Allgemeinfall: f = P(x)*(a*x^2+b*x+c)^((2*n+1)/2) bzw. (a*x^2+b*x+c)^((2*n+1)/2)*P(x).
        if (f.isNotProduct()) {
            return false;
        }

        Expression leftFactor = ((BinaryOperation) f).getLeft();
        Expression rightFactor = ((BinaryOperation) f).getRight();

        if (SimplifyPolynomialMethods.isPolynomial(leftFactor, var)
                && rightFactor.isPower()
                && ((BinaryOperation) rightFactor).getRight().isRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) rightFactor).getRight()).getLeft().isOddIntegerConstant()
                && ((BinaryOperation) ((BinaryOperation) rightFactor).getRight()).getLeft().isPositive()
                && ((BinaryOperation) ((BinaryOperation) rightFactor).getRight()).getRight().equals(TWO)
                && SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) rightFactor).getLeft(), var)
                && SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) rightFactor).getLeft(), var).compareTo(BigInteger.valueOf(2)) == 0) {

            BigInteger exponentNumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) rightFactor).getRight()).getLeft()).getBigIntValue();
            return exponentNumerator.compareTo(BigInteger.valueOf(2 * ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) < 0;

        }

        if (SimplifyPolynomialMethods.isPolynomial(rightFactor, var)
                && leftFactor.isPower()
                && ((BinaryOperation) leftFactor).getRight().isRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) leftFactor).getRight()).getLeft().isOddIntegerConstant()
                && ((BinaryOperation) ((BinaryOperation) leftFactor).getRight()).getLeft().isPositive()
                && ((BinaryOperation) ((BinaryOperation) leftFactor).getRight()).getRight().equals(TWO)
                && SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) leftFactor).getLeft(), var)
                && SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) leftFactor).getLeft(), var).compareTo(BigInteger.valueOf(2)) == 0) {

            BigInteger exponentNumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) leftFactor).getRight()).getLeft()).getBigIntValue();
            return exponentNumerator.compareTo(BigInteger.valueOf(2 * ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) < 0;

        }

        return false;

    }

    /**
     * Hilfsmethode:
     *
     * @throws EvaluationException
     */
    private static ExpressionCollection getCoefficientsOfPowerOfPolynomial(ExpressionCollection a, int n) throws EvaluationException {

        ExpressionCollection result = new ExpressionCollection(ONE);
        ExpressionCollection copyOfResult;
        Expression currentExtry;

        for (int i = 0; i < n; i++) {
            copyOfResult = ExpressionCollection.copy(result);
            result.clear();

            // Nun das Cauchyprodukt bilden.
            for (int j = 0; j < copyOfResult.getBound() + a.getBound() - 1; j++) {
                currentExtry = ZERO;
                for (int k = 0; k <= j; k++) {
                    if (k < copyOfResult.getBound() && j - k < a.getBound()) {
                        currentExtry = currentExtry.add(copyOfResult.get(k).mult(a.get(j - k)));
                    }
                }
                result.add(currentExtry);
            }
            result = result.simplify();

        }

        return result;

    }

    /**
     * Integration von 1/(a*x^2 + b*x + c)^(1/2).<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral. Falls der Integrand
     * nicht vom angegebenen Typ ist, so wird false zurückgegeben.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallyIntegrableException
     */
    private static Expression integrateReciprocalOfSqrtOfQuadraticFunction(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient() || !((BinaryOperation) f).getLeft().equals(Expression.ONE)
                || ((BinaryOperation) f).getRight().isNotPower()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().equals(Expression.ONE.div(Expression.TWO))
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        BigInteger degDenominator = SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);

        if (degDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);

        if (coefficients.getBound() != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Im Folgenden sei a = a.get(2), b = a.get(1), c = a.get(0), diskr = D = b^2 - 4*a*c.
        Expression a = coefficients.get(2);
        Expression b = coefficients.get(1);
        Expression discriminant = coefficients.get(1).pow(2).sub(new Constant(4).mult(coefficients.get(2)).mult(coefficients.get(0))).simplify();

        // Falls Uneindeutigkeiten im Vorzeichen der Diskriminante oder im Leitkoeffizienten bestehen, dann Fehler werfen.
        if (!a.isAlwaysPositive() && !a.isAlwaysNegative()
                || !discriminant.isAlwaysPositive() && !discriminant.isAlwaysNegative()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        if (discriminant.equals(Expression.ZERO) && a.isAlwaysNonNegative() && !a.equals(Expression.ZERO)) {
            // Dann ist f = 1/(a^(1/2)*|x - x_1|) mit x_1 = -b/(2*a).
            Expression zero = Expression.MINUS_ONE.mult(b).div(Expression.TWO.mult(a)).simplify();
            // F = sgn(x - x_1)*ln(|x - x_1|)/a^(1/2).
            return Variable.create(var).sub(zero).sgn().mult(
                    Variable.create(var).sub(zero).abs().ln()).div(a.pow(1, 2));
        }
        if (discriminant.isAlwaysPositive()) {
            // Hier ist D > 0.
            if (a.isAlwaysPositive()) {

                // Fall a > 0, D > 0. Reduktion auf den Typ int(1/(x^2 - 1)^(1/2), x).
                // F = arcosh((2*a*x + b)/D^(1/2))/(a^(1/2)).
                return Expression.TWO.mult(a).mult(Variable.create(var)).add(b).div(discriminant.pow(1, 2)).arcosh().div(a.pow(1, 2));

            }
            if (a.isAlwaysNegative()) {

                // Fall a < 0, D > 0. Reduktion auf den Typ int(1/(1 - x^2)^(1/2), x).
                // F = arcsin((-2*a*x - b)/D^(1/2))/((-a)^(1/2)).
                return new Constant(-2).mult(a).mult(Variable.create(var)).sub(b).div(discriminant.pow(1, 2)).arcsin().div(Expression.MINUS_ONE.mult(a).pow(1, 2));

            }
        }
        if (a.isAlwaysPositive() && discriminant.isAlwaysNegative()) {

            // Fall a > 0, D < 0. Reduktion auf den Typ int(1/(x^2 + 1)^(1/2), x).
            // F = arsinh((2*a*x + b)/(-D)^(1/2))/(a^(1/2)).
            return Expression.TWO.mult(a).mult(Variable.create(var)).add(b).div(Expression.MINUS_ONE.mult(discriminant).pow(1, 2)).arsinh().div(a.pow(1, 2));

        }

        // Übrige, nicht eindeutig entscheidbare Fälle.
        throw new NotAlgebraicallyIntegrableException();

    }

    /**
     * Integration von 1/(a*x^2 + b*x + c)^((2*n + 1)/2).<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallyIntegrableException
     */
    private static Expression integrateReciprocalOfOddPowerOfSqrtOfQuadraticPolynomial(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient() || !((BinaryOperation) f).getLeft().equals(Expression.ONE)
                || ((BinaryOperation) f).getRight().isNotPower()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().isRationalConstant()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        int n;

        if (!((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getRight().equals(TWO)
                || !((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getLeft().isPositiveOddIntegerConstant()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        BigInteger exponentNumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getLeft()).getBigIntValue();

        if (exponentNumerator.compareTo(BigInteger.valueOf(2 * ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotAlgebraicallyIntegrableException();
        }

        n = (exponentNumerator.intValue() - 1) / 2;

        if (n == 0) {
            return integrateReciprocalOfSqrtOfQuadraticFunction(expr);
        }

        ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);

        if (coefficients.getBound() != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Im Folgenden sei a = a.get(2), b = a.get(1), c = a.get(0), diskr = D = 4*a*c - b^2, x = var.
        Expression a = coefficients.get(2);
        Expression b = coefficients.get(1);
        Expression c = coefficients.get(0);
        Expression discriminant = new Constant(4).mult(a).mult(c).sub(b.pow(2)).simplify();

        // Falls Uneindeutigkeiten im Vorzeichen der Diskriminante oder im Leitkoeffizienten bestehen, dann Fehler werfen.
        if (!a.isAlwaysPositive() && !a.isAlwaysNegative()
                || !discriminant.isAlwaysPositive() && !discriminant.isAlwaysNegative()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression radicand = ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft();

        // firstSummand = (4*a*x+b)/((2*n-1)*D*R^((2*n-1)/2)
        Expression firstSummand = new Constant(4).mult(a).mult(Variable.create(var)).add(b).div(
                new Constant(2 * n - 1).mult(discriminant).mult(radicand.pow(2 * n - 1, 2)));

        // factor = 8*a*(n - 1)/((2*n - 1)*D)
        Expression factor = new Constant(8 * n - 8).mult(a).div(new Constant(2 * n - 1).mult(discriminant));

        // Integral = firstSummand + factor*int(1/(a*x^2+b*x+c)^((2*n - 1)/2),x).
        Object[] params = new Object[2];
        params[0] = ONE.div(radicand.pow(2 * n - 1, 2));
        params[1] = var;
        Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);

        return firstSummand.add(factor.mult(integralOfLowerPower));

    }

    /**
     * Integration von (d*x + e)/(a*x^2 + b*x + c)^((2*n + 1)/2).<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integrateQuotientOfLinearPolynomialAndOddPowerOfSqrtOfQuadraticPolynomial(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)
                || ((BinaryOperation) f).getRight().isNotPower()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().isRationalConstant()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        BigInteger degNumerator = SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
        BigInteger degDenominator = SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);

        if (degNumerator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0
                || degDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection coefficientsNumerator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
        ExpressionCollection coefficientsDenominator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);

        if (coefficientsNumerator.getBound() > 2 || coefficientsDenominator.getBound() != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Fehlende Koeffizienten im Zählen mit Nullen auffüllen.
        for (int i = 0; i < 2; i++) {
            if (coefficientsNumerator.get(i) == null) {
                coefficientsNumerator.put(i, ZERO);
            }
        }

        int n;

        if (!((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getRight().equals(TWO)
                || !((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getLeft().isPositiveOddIntegerConstant()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        BigInteger exponentNumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getLeft()).getBigIntValue();

        if (exponentNumerator.compareTo(BigInteger.valueOf(2 * ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotAlgebraicallyIntegrableException();
        }

        n = (exponentNumerator.intValue() - 1) / 2;

        if (coefficientsDenominator.getBound() != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // In den folgenden Kommentaren sei a = coefficientsNumerator, b = coefficientsDenominator.
        // Im Folgenden sei a = a.get(2), b = a.get(1), c = a.get(0), d = b.get(1), e = b.get(0), x = var.
        Expression a = coefficientsDenominator.get(2);
        Expression b = coefficientsDenominator.get(1);
        Expression c = coefficientsDenominator.get(0);
        Expression d = coefficientsNumerator.get(1);
        Expression e = coefficientsNumerator.get(0);

        // discriminant = b^2 - 4ac.
        Expression discriminant = b.pow(2).sub(new Constant(4).mult(a).mult(c)).simplify();
        if (!coefficientsDenominator.get(2).isAlwaysPositive() && !coefficientsDenominator.get(2).isAlwaysNegative()
                || !discriminant.isAlwaysPositive() && !discriminant.isAlwaysNegative()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // radicand = a*x^2 + b*x + c
        Expression radicand = ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft();

        // firstSummand = d/((1-2*n)*a*R^((2*n-1)/2)
        Expression firstSummand = d.div(new Constant(1 - 2 * n).mult(a).mult(radicand.pow(2 * n - 1, 2)));

        // factor = e - (d*b)/(2*a)
        Expression factor = e.sub(d.mult(b).div(TWO.mult(a)));

        // Integral = firstSummand + factor*int(1/(a*x^2+b*x+c)^((2*n + 1)/2),x).
        Object[] params = new Object[2];
        params[0] = ONE.div(((BinaryOperation) f).getRight());
        params[1] = var;
        Expression integralOfLowerPower = integrateReciprocalOfOddPowerOfSqrtOfQuadraticPolynomial(new Operator(TypeOperator.integral, params));

        return firstSummand.add(factor.mult(integralOfLowerPower));

    }

    /**
     * Integration von P(x)/(a*x^2 + b*x + c)^((2*n + 1)/2), P(x) = Polynom.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integrateQuotientOfPolynomialAndOddPowerOfSqrtOfQuadraticPolynomial(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)
                || ((BinaryOperation) f).getRight().isNotPower()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().isRationalConstant()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        BigInteger degNumerator = SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
        BigInteger degDenominator = SimplifyPolynomialMethods.getDegreeOfPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);

        if (degNumerator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0
                || degDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection coefficientsNumerator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
        ExpressionCollection coefficientsDenominator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);

        if (coefficientsDenominator.getBound() != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }

        int n;

        if (!((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getRight().equals(TWO)
                || !((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getLeft().isPositiveOddIntegerConstant()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        BigInteger exponentNumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getLeft()).getBigIntValue();

        if (exponentNumerator.compareTo(BigInteger.valueOf(2 * ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotAlgebraicallyIntegrableException();
        }

        n = (exponentNumerator.intValue() - 1) / 2;

        if (coefficientsDenominator.getBound() != 3) {
            throw new NotAlgebraicallyIntegrableException();
        }

        int maxExponent = (coefficientsNumerator.getBound() - 1) / 2;

        if (maxExponent == 0) {
            // f ist von der Form (A*x+B)/(a*x^2+b*x+c)^((2*n+1)/2).
            return integrateQuotientOfLinearPolynomialAndOddPowerOfSqrtOfQuadraticPolynomial(expr);
        }

        // discriminant = b^2 - 4ac.
        Expression a = coefficientsDenominator.get(2);
        Expression b = coefficientsDenominator.get(1);
        Expression c = coefficientsDenominator.get(0);
        Expression discriminant = b.pow(2).sub(new Constant(4).mult(a).mult(c)).simplify();
        if (!coefficientsDenominator.get(2).isAlwaysPositive() && !coefficientsDenominator.get(2).isAlwaysNegative()
                || !discriminant.isAlwaysPositive() && !discriminant.isAlwaysNegative()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        /*
         Sei k = maxExponent. Zunächst: Koeffizienten von (a*x^2 + b*x + c)^k
         bilden.
         */
        ExpressionCollection coefficientsOfPowerOfQuadraticPolynomial = getCoefficientsOfPowerOfPolynomial(coefficientsDenominator, maxExponent);
        ExpressionCollection[] quotient = SimplifyPolynomialMethods.polynomialDivision(coefficientsNumerator, coefficientsOfPowerOfQuadraticPolynomial);

        /* 
         Man hat nun eine Darstellung der Form P(x) = (A*x+B)*(a*x^2+b*x+c)^k + Q(x)
         mit deg(Q) < 2*k. Daher: P(x)/(a*x^2+b*x+c)^((2*n+1)/2) = (A*x+B)/(a*x^2+b*x+c)^((2*n-2*k+1)/2)
         + Q(x)/(a*x^2+b*x+c)^((2*n+1)/2). Der erste Summand muss abhängig davon,
         ob 2*n-2*k+1 > 0 oder 2*n-2*k+1 < 0 ist, behandelt werden.
         */
        Expression firstSummand, secondSummand;
        Object[] paramsFirstSummand = new Object[2];
        if (2 * n + 1 > 2 * maxExponent) {
            firstSummand = SimplifyPolynomialMethods.getPolynomialFromCoefficients(quotient[0], var).div(
                    SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsDenominator, var).pow(2 * n - 2 * maxExponent + 1, 2));
        } else {
            firstSummand = SimplifyPolynomialMethods.getPolynomialFromCoefficients(quotient[0], var).mult(
                    SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsDenominator, var).pow(2 * maxExponent - 2 * n - 1, 2));
        }
        secondSummand = SimplifyPolynomialMethods.getPolynomialFromCoefficients(quotient[1], var).div(
                SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsDenominator, var).pow(2 * n + 1, 2));
        paramsFirstSummand[0] = firstSummand;
        paramsFirstSummand[1] = var;
        Expression integralOfFirstSummand = indefiniteIntegration(new Operator(TypeOperator.integral, paramsFirstSummand), true);
        Object[] paramsSecondSummand = new Object[2];
        paramsSecondSummand[0] = secondSummand;
        paramsSecondSummand[1] = var;
        Expression integralOfSecondSummand = indefiniteIntegration(new Operator(TypeOperator.integral, paramsSecondSummand), true);
        return integralOfFirstSummand.add(integralOfSecondSummand);

    }

    /**
     * Integriert Funktionen vom Typ R(exp(a*x)), R = rationale Funktion.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integrateRationalFunctionInExp(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        HashSet<Expression> argumentsInExp = new HashSet<>();

        // Konstante Summanden aus Argumenten in Exponentialfunktionen herausziehen.
        f = SimplifyExponentialRelations.separateConstantPartsInRationalExponentialEquations(f, var);

        if (!SimplifyRationalFunctionMethods.isRationalFunktionInExp(f, var, argumentsInExp) || argumentsInExp.isEmpty()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        BigInteger gcdOfNumerators = BigInteger.ONE;
        BigInteger lcmOfDenominators = BigInteger.ONE;

        Iterator<Expression> iter = argumentsInExp.iterator();
        Expression firstArgument = iter.next();

        Expression derivativeOfFirstArgument;
        try {
            derivativeOfFirstArgument = firstArgument.diff(var).simplify();
            if (derivativeOfFirstArgument.contains(var)) {
                throw new NotAlgebraicallyIntegrableException();
            }
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression currentQuotient;

        while (iter.hasNext()) {
            currentQuotient = iter.next().div(firstArgument).simplify();
            // Die folgende Abfrage müsste wegen Vorbedingung immer true sein. Trotzdem sicherheitshalber!
            if (currentQuotient.isIntegerConstantOrRationalConstant()) {

                if (currentQuotient.isIntegerConstant()) {
                    gcdOfNumerators = gcdOfNumerators.gcd(((Constant) currentQuotient).getBigIntValue());
                } else {
                    gcdOfNumerators = gcdOfNumerators.gcd(((Constant) ((BinaryOperation) currentQuotient).getLeft()).getBigIntValue());
                    lcmOfDenominators = ArithmeticMethods.lcm(lcmOfDenominators,
                            ((Constant) ((BinaryOperation) currentQuotient).getRight()).getBigIntValue());
                }

            }
        }

        // Das ist die eigentliche Substitution.
        Expression factorOfExpArgument = new Constant(gcdOfNumerators).mult(derivativeOfFirstArgument).div(lcmOfDenominators).simplify();
        Expression substitution = new Constant(gcdOfNumerators).mult(firstArgument).div(lcmOfDenominators).exp().simplify();

        try {
            Expression fSubstituted = SubstitutionUtilities.substitute(f, var, substitution);
            String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
            /*
             Das Folgende ist eine Sicherheitsabfrage: Die substituierte Gleichung sollte vom 
             folgenden Typ sein: Alle Argumente, die in trigonometrischen Funktionen vorkommen,
             müssen von der Form n*x sein, wobei n eine ganze Zahl und x eine Variable ist.
             */
            if (!SimplifyRationalFunctionMethods.doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable((Expression) fSubstituted, substVar)) {
                throw new NotAlgebraicallyIntegrableException();
            }

            Expression substitutedIntegrand = ((Expression) fSubstituted).div(factorOfExpArgument.mult(Variable.create(substVar)));

            Operator substitutedIntegral = new Operator(TypeOperator.integral, new Object[]{substitutedIntegrand, substVar});
            Expression resultFunction = indefiniteIntegration(substitutedIntegral, true);
            return resultFunction.replaceVariable(substVar, factorOfExpArgument.mult(Variable.create(var)).exp());
        } catch (NotSubstitutableException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

    }

    /**
     * Integriert Funktionen vom Typ R(sin(a*x), cos(a*x)), R = rationale
     * Funktion.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integrateRationalFunctionInTrigonometricFunctions(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        HashSet<Expression> argumentsInTrigonometricalFunctions = new HashSet<>();

        // Konstante Summanden aus Argumenten in Exponentialfunktionen herausziehen.
        f = SimplifyTrigonometricalRelations.separateConstantPartsInRationalTrigonometricalEquations(f, var);

        if (!SimplifyRationalFunctionMethods.isRationalFunktionInTrigonometricalFunctions(f, var, argumentsInTrigonometricalFunctions) || argumentsInTrigonometricalFunctions.isEmpty()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        BigInteger gcdOfNumerators = BigInteger.ONE;
        BigInteger lcmOfDenominators = BigInteger.ONE;

        Iterator<Expression> iter = argumentsInTrigonometricalFunctions.iterator();
        Expression firstArgument = iter.next();

        Expression derivativeOfFirstArgument;
        try {
            derivativeOfFirstArgument = firstArgument.diff(var).simplify();
            if (derivativeOfFirstArgument.contains(var)) {
                throw new NotAlgebraicallyIntegrableException();
            }
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression currentQuotient;

        while (iter.hasNext()) {
            currentQuotient = iter.next().div(firstArgument).simplify();
            // Die folgende Abfrage müsste wegen Vorbedingung immer true sein. Trotzdem sicherheitshalber!
            if (currentQuotient.isIntegerConstantOrRationalConstant()) {

                if (currentQuotient.isIntegerConstant()) {
                    gcdOfNumerators = gcdOfNumerators.gcd(((Constant) currentQuotient).getBigIntValue());
                } else {
                    gcdOfNumerators = gcdOfNumerators.gcd(((Constant) ((BinaryOperation) currentQuotient).getLeft()).getBigIntValue());
                    lcmOfDenominators = ArithmeticMethods.lcm(lcmOfDenominators,
                            ((Constant) ((BinaryOperation) currentQuotient).getRight()).getBigIntValue());
                }

            }
        }

        // Das ist die eigentliche Substitution.
        Expression factorOfTrigonometricalArgument = new Constant(gcdOfNumerators).mult(derivativeOfFirstArgument).div(lcmOfDenominators).simplify();
        Expression substitution = new Constant(gcdOfNumerators).mult(firstArgument).div(lcmOfDenominators).simplify();

        try {

            Expression fSubstituted = SubstitutionUtilities.substitute(f, var, substitution).simplify();
            String substVar = SubstitutionUtilities.getSubstitutionVariable(f);

            /*
             Das Folgende ist eine Sicherheitsabfrage: Die substituierte Gleichung sollte vom 
             folgenden Typ sein: Alle Argumente, die in trigonometrischen Funktionen vorkommen,
             müssen von der Form n*x sein, wobei n eine ganze Zahl und x eine Variable ist.
             */
            if (!SimplifyRationalFunctionMethods.doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable(fSubstituted, substVar)) {
                throw new NotAlgebraicallyIntegrableException();
            }

            /*
             Nun ist fSubstituted eine rationale Funtion in sin(n*X_1) und cos(m*X_1), X_1 = substVar.
             Jetzt muss fSubstituted als rationale Function in sin(X_1) und cos(X_1) dargestellt werden.
             WICHTIG: Beim Vereinfachen darf hier nicht simplifyFunctionalRelations() verwendet werden,
             da dann beispielsweise sin(x)*cos(x) wieder zu sin(2*x)/2 vereinfacht wird.
             */
            fSubstituted = SimplifyRationalFunctionMethods.expandRationalFunctionInTrigonometricalFunctions(fSubstituted, substVar).simplify(simplifyTypesRationalTrigonometricalFunction);
            /*
             Jetzt erfolgt die eigentliche Substitution: cos(x) = (1-t^2)/(1+t^2),
             sin(x) = 2t/(1+t^2), dx = 2/(1+t^2)*dt.
             */
            String substVarForIntegral = SubstitutionUtilities.getSubstitutionVariable(fSubstituted);
            Expression substForCos = ONE.sub(Variable.create(substVarForIntegral).pow(2)).div(ONE.add(Variable.create(substVarForIntegral).pow(2)));
            Expression substForSin = TWO.mult(Variable.create(substVarForIntegral)).div(ONE.add(Variable.create(substVarForIntegral).pow(2)));
            Expression substForDX = TWO.div(ONE.add(Variable.create(substVarForIntegral).pow(2)));

            Expression substitutedIntegrand = SubstitutionUtilities.substituteExpressionByAnotherExpression(fSubstituted,
                    Variable.create(substVar).cos(), substForCos);
            substitutedIntegrand = SubstitutionUtilities.substituteExpressionByAnotherExpression(substitutedIntegrand,
                    Variable.create(substVar).sin(), substForSin);

            if (!substitutedIntegrand.contains(substVarForIntegral)) {
                // Dann konnte weder sin, noch cos substitutiert werden. Dies sollte hier eigentlich nicht passieren.
                throw new NotAlgebraicallyIntegrableException();
            }

            substitutedIntegrand = substitutedIntegrand.mult(substForDX).div(factorOfTrigonometricalArgument);
            Operator substitutedIntegral = new Operator(TypeOperator.integral, new Object[]{substitutedIntegrand, substVarForIntegral});
            Expression resultFunction = indefiniteIntegration(substitutedIntegral, true);
            // Falls noch unbestimmte Integrale auftauchen, dann konnte nicht ordentlich integriert werden.
            if (resultFunction.containsIndefiniteIntegral()) {
                throw new NotAlgebraicallyIntegrableException();
            }
            // Rücksubstitution!
            return resultFunction.replaceVariable(substVarForIntegral,
                    factorOfTrigonometricalArgument.mult(Variable.create(var)).div(2).tan());

        } catch (NotSubstitutableException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

    }

    /**
     * Integriert Funktionen vom Typ P(x)*f(x)^n, P = Polynom, f = arcsin,
     * arccos, arsinh, arcosh, n positive ganze Zahl.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integrateProductOfPolynomialAndPowerOfArcusOrAreaFunction(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotProduct()) {
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression factorLeft = ((BinaryOperation) f).getLeft();
        Expression factorRight = ((BinaryOperation) f).getRight();

        if (!SimplifyPolynomialMethods.isPolynomial(factorLeft, var)) {
            // Faktoren vertauschen und erneut prüfen.
            Expression factor = factorLeft;
            factorLeft = factorRight;
            factorRight = factor;
        }

        if (!SimplifyPolynomialMethods.isPolynomial(factorLeft, var)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression base;
        BigInteger exponent;
        if (factorRight.isPositiveIntegerPower()) {
            base = ((BinaryOperation) factorRight).getLeft();
            exponent = ((Constant) ((BinaryOperation) factorRight).getRight()).getBigIntValue();
        } else {
            base = factorRight;
            exponent = BigInteger.ONE;
        }

        String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
        Expression fSubstituted, integralOfFSubstituted;

        if (base.equals(Variable.create(var).arcsin())) {
            // f = P(x)*arcsin(x)^n. Substitution: t = arcsin(x) <=> x = sin(t). Integriert werden muss g = P(sin(t))*t^n*cos(t)
            fSubstituted = factorLeft.replaceVariable(var, Variable.create(substVar).sin()).mult(Variable.create(substVar).cos()).mult(Variable.create(substVar).pow(exponent));
            try {
                integralOfFSubstituted = integrateIndefinite(new Operator(TypeOperator.integral, new Object[]{fSubstituted, substVar}));
                if (integralOfFSubstituted.containsIndefiniteIntegral()) {
                    throw new NotAlgebraicallyIntegrableException();
                }
                return integralOfFSubstituted.replaceVariable(substVar, Variable.create(var).arcsin());
            } catch (EvaluationException e) {
                throw new NotAlgebraicallyIntegrableException();
            }
        } else if (base.equals(Variable.create(var).arccos())) {
            // f = P(x)*arccos(x)^n. Substitution: t = arccos(x) <=> x = cos(t). Integriert werden muss g = -P(cos(t))*t^n*sin(t)
            fSubstituted = MINUS_ONE.mult(factorLeft.replaceVariable(var, Variable.create(substVar).cos()).mult(Variable.create(substVar).sin()).mult(Variable.create(substVar).pow(exponent)));
            try {
                integralOfFSubstituted = integrateIndefinite(new Operator(TypeOperator.integral, new Object[]{fSubstituted, substVar}));
                if (integralOfFSubstituted.containsIndefiniteIntegral()) {
                    throw new NotAlgebraicallyIntegrableException();
                }
                return integralOfFSubstituted.replaceVariable(substVar, Variable.create(var).arccos());
            } catch (EvaluationException e) {
                throw new NotAlgebraicallyIntegrableException();
            }
        } else if (base.equals(Variable.create(var).arsinh())) {
            // f = P(x)*arsinh(x)^n. Substitution: t = arsinh(x) <=> x = sinh(t). Integriert werden muss g = P(sinh(t))*t^n*cosh(t)
            fSubstituted = factorLeft.replaceVariable(var, Variable.create(substVar).sinh()).mult(Variable.create(substVar).cosh()).mult(Variable.create(substVar).pow(exponent));
            try {
                integralOfFSubstituted = integrateIndefinite(new Operator(TypeOperator.integral, new Object[]{fSubstituted, substVar}));
                if (integralOfFSubstituted.containsIndefiniteIntegral()) {
                    throw new NotAlgebraicallyIntegrableException();
                }
                return integralOfFSubstituted.replaceVariable(substVar, Variable.create(var).arsinh());
            } catch (EvaluationException e) {
                throw new NotAlgebraicallyIntegrableException();
            }
        } else if (base.equals(Variable.create(var).arcosh())) {
            // f = P(x)*arcosh(x)^n. Substitution: t = arcosh(x) <=> x = cosh(t). Integriert werden muss g = P(cosh(t))*t^n*sinh(t)
            fSubstituted = factorLeft.replaceVariable(var, Variable.create(substVar).cosh()).mult(Variable.create(substVar).sinh()).mult(Variable.create(substVar).pow(exponent));
            try {
                integralOfFSubstituted = integrateIndefinite(new Operator(TypeOperator.integral, new Object[]{fSubstituted, substVar}));
                if (integralOfFSubstituted.containsIndefiniteIntegral()) {
                    throw new NotAlgebraicallyIntegrableException();
                }
                return integralOfFSubstituted.replaceVariable(substVar, Variable.create(var).arcosh());
            } catch (EvaluationException e) {
                throw new NotAlgebraicallyIntegrableException();
            }
        }

        throw new NotAlgebraicallyIntegrableException();

    }

    /////////////////////////// Integration algebraischer Funktionen //////////////////////////////////////////
    /**
     * Ermittelt potenzielle Substitutionen für die Integration einer
     * algebraischen Funktion.
     */
    private static void addSuitableSubstitutionForIntegrationOfAlgebraicFunctions(Expression f, String var, ExpressionCollection setOfSubstitutions) {
        if (f.contains(var) && f instanceof BinaryOperation && f.isNotPower()) {
            addSuitableSubstitutionForIntegrationOfAlgebraicFunctions(((BinaryOperation) f).getLeft(), var, setOfSubstitutions);
            addSuitableSubstitutionForIntegrationOfAlgebraicFunctions(((BinaryOperation) f).getRight(), var, setOfSubstitutions);
        } else if (f.contains(var) && f.isPower() && ((BinaryOperation) f).getRight().isRationalConstant()) {
            setOfSubstitutions.add(f);
        }
    }

    // Typ 1: R(x, (ax^2 + bx + c)^(1/2)), R = rationale Funktion.
    /**
     * Integriert Funktionen vom Typ R(x, (ax^2 + bx + c)^(1/2)), R = rationale
     * Funktion.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallyIntegrableException
     */
    public static Expression integrateRationalFunctionInVarAndSqrtOfQuadraticFunction(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        Expression radicand;
        try {
            radicand = getRadicandIfFunctionIsRationalFunctionInVarAndSqrtOfQuadraticFunction(f, var);
        } catch (NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(radicand, var);
        if (coefficients.getBound() != 3) {
            // Sollte eigentlich nicht eintreten.
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression a = coefficients.get(2);
        Expression b = coefficients.get(1);
        Expression c = coefficients.get(0);
        Expression discriminant;
        try {
            discriminant = b.pow(2).sub(new Constant(4).mult(a).mult(c)).simplify();
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression p, q;
        String substVarForIntegral = SubstitutionUtilities.getSubstitutionVariable(f);
        if (discriminant.isAlwaysNegative()) {

            try {
                p = b.div(TWO.mult(a)).simplify();
                q = MINUS_ONE.mult(discriminant).pow(1, 2).div(TWO.mult(a)).simplify();
            } catch (EvaluationException e) {
                throw new NotAlgebraicallyIntegrableException();
            }

            // Nur der Fall a > 0 ist möglich (bei a < 0 wäre die Wurzel für kein x definiert, x = var).
            if (a.isAlwaysPositive()) {

                Expression fSubstituted = f;

                // ZUERST: (ax^2+bx+c)^(1/2) wird durch q*a^(1/2)*(exp(t)+exp(-t))/2 ersetzt.
                fSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(fSubstituted, radicand.pow(1, 2),
                        q.mult(a.pow(1, 2)).mult(Variable.create(substVarForIntegral).exp().add(MINUS_ONE.mult(Variable.create(substVarForIntegral)).exp())).div(2));
                // DANACH: x wird durch q*(exp(t)-exp(-t))/2 - p ersetzt, t = substVarForIntegral.
                fSubstituted = fSubstituted.replaceVariable(var, q.mult(Variable.create(substVarForIntegral).exp().sub(MINUS_ONE.mult(Variable.create(substVarForIntegral)).exp())).div(2).sub(p));

                if (fSubstituted.contains(var)) {
                    // Sollte eigentlich nicht passieren.
                    throw new NotAlgebraicallyIntegrableException();
                }

                fSubstituted = q.div(2).mult(fSubstituted.mult(Variable.create(substVarForIntegral).exp().add(MINUS_ONE.mult(Variable.create(substVarForIntegral)).exp())));

                try {
                    // Vor dem Integrieren den Integranden vereinfachen.
                    fSubstituted = fSubstituted.simplify();
                } catch (EvaluationException e) {
                    // Sollte bei einem gültigen Integranden nicht passieren.
                    throw new NotAlgebraicallyIntegrableException();
                }

                Operator substitutedIntegral = new Operator(TypeOperator.integral, new Object[]{fSubstituted, substVarForIntegral});
                Expression resultFunction = indefiniteIntegration(substitutedIntegral, true);

                try {
                    // Vor der Rücksubstitution vereinfachen.
                    resultFunction = resultFunction.simplify();
                } catch (EvaluationException e) {
                    // Sollte bei einer gültigen Stammfunktion nicht passieren.
                    throw new NotAlgebraicallyIntegrableException();
                }
                // Falls noch unbestimmte Integrale auftauchen, dann konnte nicht ordentlich integriert werden.
                if (resultFunction.containsIndefiniteIntegral()) {
                    throw new NotAlgebraicallyIntegrableException();
                }
                // Rücksubstitution: t = ln(u + (1 + u^2)^(1/2)) mit u = (x + p)/q.
                Expression u = Variable.create(var).add(p).div(q);
                return resultFunction.replaceVariable(substVarForIntegral, u.add(ONE.add(u.pow(2)).pow(1, 2)).ln());

            }

        } else if (discriminant.isAlwaysPositive()) {

            if (a.isAlwaysPositive()) {

                try {
                    p = b.div(TWO.mult(a)).simplify();
                    q = discriminant.pow(1, 2).div(TWO.mult(a)).simplify();
                } catch (EvaluationException e) {
                    throw new NotAlgebraicallyIntegrableException();
                }
                Expression fSubstituted = f;

                // ZUERST: (ax^2+bx+c)^(1/2) wird durch q*a^(1/2)*(exp(t)-exp(-t))/2 ersetzt.
                fSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(fSubstituted, radicand.pow(1, 2),
                        q.mult(a.pow(1, 2)).mult(Variable.create(substVarForIntegral).exp().sub(MINUS_ONE.mult(Variable.create(substVarForIntegral)).exp())).div(2));
                // DANACH: x wird durch q*(exp(t)+exp(-t))/2 - p ersetzt, t = substVarForIntegral.
                fSubstituted = fSubstituted.replaceVariable(var, q.mult(Variable.create(substVarForIntegral).exp().add(MINUS_ONE.mult(Variable.create(substVarForIntegral)).exp())).div(2).sub(p));

                if (fSubstituted.contains(var)) {
                    // Sollte eigentlich nicht passieren.
                    throw new NotAlgebraicallyIntegrableException();
                }

                fSubstituted = q.div(2).mult(fSubstituted.mult(Variable.create(substVarForIntegral).exp().sub(MINUS_ONE.mult(Variable.create(substVarForIntegral)).exp())));

                try {
                    // Vor dem Integrieren den Integranden vereinfachen.
                    fSubstituted = fSubstituted.simplify();
                } catch (EvaluationException e) {
                    // Sollte bei einem gültigen Integranden nicht passieren.
                    throw new NotAlgebraicallyIntegrableException();
                }

                Operator substitutedIntegral = new Operator(TypeOperator.integral, new Object[]{fSubstituted, substVarForIntegral});
                Expression resultFunction = indefiniteIntegration(substitutedIntegral, true);

                try {
                    // Vor der Rücksubstitution vereinfachen.
                    resultFunction = resultFunction.simplify();
                } catch (EvaluationException e) {
                    // Sollte bei einer gültigen Stammfunktion nicht passieren.
                    throw new NotAlgebraicallyIntegrableException();
                }
                // Falls noch unbestimmte Integrale auftauchen, dann konnte nicht ordentlich integriert werden.
                if (resultFunction.containsIndefiniteIntegral()) {
                    throw new NotAlgebraicallyIntegrableException();
                }
                // Rücksubstitution: t = ln(u + (u^2 - 1)^(1/2)) mit u = (x + p)/q.
                Expression u = Variable.create(var).add(p).div(q);
                return resultFunction.replaceVariable(substVarForIntegral, u.add(u.pow(2).sub(ONE).pow(1, 2)).ln());

            } else if (a.isAlwaysNegative()) {

                try {
                    p = b.div(TWO.mult(a)).simplify();
                    q = MINUS_ONE.mult(discriminant.pow(1, 2)).div(TWO.mult(a)).simplify();
                } catch (EvaluationException e) {
                    throw new NotAlgebraicallyIntegrableException();
                }
                Expression fSubstituted = f;

                // ZUERST: (ax^2+bx+c)^(1/2) wird durch q*(-a)^(1/2)*cos(t) ersetzt.
                fSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(fSubstituted, radicand.pow(1, 2),
                        q.mult(MINUS_ONE.mult(a).pow(1, 2)).mult(Variable.create(substVarForIntegral).cos()));
                // DANACH: x wird durch q*sin(t) - p ersetzt, t = substVarForIntegral.
                fSubstituted = fSubstituted.replaceVariable(var, q.mult(Variable.create(substVarForIntegral).sin().sub(p)));

                if (fSubstituted.contains(var)) {
                    // Sollte eigentlich nicht passieren.
                    throw new NotAlgebraicallyIntegrableException();
                }

                fSubstituted = q.mult(fSubstituted.mult(Variable.create(substVarForIntegral).cos()));

                try {
                    // Vor dem Integrieren den Integranden vereinfachen.
                    fSubstituted = fSubstituted.simplify();
                } catch (EvaluationException e) {
                    // Sollte bei einem gültigen Integranden nicht passieren.
                    throw new NotAlgebraicallyIntegrableException();
                }

                Operator substitutedIntegral = new Operator(TypeOperator.integral, new Object[]{fSubstituted, substVarForIntegral});
                Expression resultFunction = indefiniteIntegration(substitutedIntegral, true);

                try {
                    // Vor der Rücksubstitution vereinfachen.
                    resultFunction = resultFunction.simplify();
                } catch (EvaluationException e) {
                    // Sollte bei einer gültigen Stammfunktion nicht passieren.
                    throw new NotAlgebraicallyIntegrableException();
                }
                // Falls noch unbestimmte Integrale auftauchen, dann konnte nicht ordentlich integriert werden.
                if (resultFunction.containsIndefiniteIntegral()) {
                    throw new NotAlgebraicallyIntegrableException();
                }
                // Rücksubstitution: t = arcsin(u) mit u = (x + p)/q.
                Expression u = Variable.create(var).add(p).div(q);
                return resultFunction.replaceVariable(substVarForIntegral, u.arcsin().simplify());

            }

        }

        // Fall: discriminant = 0. Schwierig zu integrieren!
        throw new NotAlgebraicallyIntegrableException();

    }

    private static Expression getRadicandIfFunctionIsRationalFunctionInVarAndSqrtOfQuadraticFunction(Expression f, String var) throws NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException {

        ExpressionCollection setOfSubstitutions = new ExpressionCollection();
        addSuitableSubstitutionForIntegrationOfAlgebraicFunctions(f, var, setOfSubstitutions);

        Expression radicand = null;
        for (Expression subst : setOfSubstitutions) {
            if (!subst.isPower() || !((BinaryOperation) subst).getRight().isRationalConstant()
                    || !((BinaryOperation) ((BinaryOperation) subst).getRight()).getLeft().isOddIntegerConstant()
                    || !((BinaryOperation) ((BinaryOperation) subst).getRight()).getRight().equals(TWO)) {
                throw new NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException();
            }
            if (radicand == null) {
                radicand = ((BinaryOperation) subst).getLeft();
                if (!SimplifyPolynomialMethods.isPolynomial(radicand, var)
                        || SimplifyPolynomialMethods.getDegreeOfPolynomial(radicand, var).compareTo(BigInteger.valueOf(2)) != 0) {
                    throw new NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException();
                }
            } else if (!radicand.equivalent(((BinaryOperation) subst).getLeft())) {
                throw new NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException();
            }
        }

        if (radicand == null) {
            // Dann ist die Funktion eine rationale Funktion (andere Methoden sind dann dafür zuständig). 
            throw new NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException();
        }

        // Schließlich: PRüfung, ob f eine rationale Funktion in x und g(x) = radicand^(1/2) ist.
        if (!SimplifyRationalFunctionMethods.isRationalFunctionInFunctions(f, var, Variable.create(var), radicand.pow(1, 2))) {
            throw new NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException();
        }

        return radicand;

    }

    // Typ 2: f(x, g(x)) = 0, f = rationale Funktion, mit der Eigenschaft, dass y = g(x) eine Auflösung der Form x = h(y), h = rationale Funktion, besitzt (z.B. y = (1 - x)^(1/3)).
    /**
     * Hauptmethode zum Integrieren von algebraischen Gleichungen der Form f(x,
     * g(x)) = 0, f = rationale Funktion in zwei Veränderlichen und g mit der
     * Eigenschaft, dass die Auflösung x = h(y) eine rationale Funktion in y
     * ist. Ist f keine solche Gleichung, so wird eine
     * NotAlgebraicallyIntegrableException geworfen.
     *
     * @throws EvaluationException, NotAlgebraicallyIntegrableException
     */
    public static Expression integrateRationalFunctionInVarAndAnotherAlgebraicExpressionEquation(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        Expression algebraicTerm, radicand, exponent;
        try {
            algebraicTerm = getAlgebraicTermIfFunctionIsRationalFunctionInVarAndAnotherAlgebraicFunction(f, var);
            if (!algebraicTerm.isRationalPower()) {
                throw new NotAlgebraicallyIntegrableException();
            }
            radicand = ((BinaryOperation) algebraicTerm).getLeft();
            exponent = ((BinaryOperation) algebraicTerm).getRight();
        } catch (NotRationalFunctionInVarAndAnotherAlgebraicFunctionException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
        ExpressionCollection zerosOfAlgebraicTermMinusSubstVar;

        try {
            zerosOfAlgebraicTermMinusSubstVar = SolveGeneralEquationMethods.solveEquation(radicand, Variable.create(substVar).pow(ONE.div(exponent)), var);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Lösung muss eindeutig sein.        
        if (zerosOfAlgebraicTermMinusSubstVar.getBound() != 1) {
            throw new NotAlgebraicallyIntegrableException();
        }

        if (!SimplifyRationalFunctionMethods.isRationalFunction(zerosOfAlgebraicTermMinusSubstVar.get(0), substVar)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression inverseTransformation = zerosOfAlgebraicTermMinusSubstVar.get(0);
        Expression derivativeOfInverseTransformation;
        try {
            derivativeOfInverseTransformation = inverseTransformation.diff(substVar).simplify(simplifyTypesAlgebraicFunction);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression integrandSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(f, algebraicTerm, Variable.create(substVar));
        integrandSubstituted = integrandSubstituted.replaceVariable(var, inverseTransformation);
        integrandSubstituted = integrandSubstituted.mult(derivativeOfInverseTransformation);

        Expression resultFunction = indefiniteIntegration(new Operator(TypeOperator.integral, new Object[]{integrandSubstituted, substVar}), true);

        try {
            // Vor der Rücksubstitution vereinfachen.
            resultFunction = resultFunction.simplify();
        } catch (EvaluationException e) {
            // Sollte bei einer gültigen Stammfunktion nicht passieren.
            throw new NotAlgebraicallyIntegrableException();
        }
        // Falls noch unbestimmte Integrale auftauchen, dann konnte nicht ordentlich integriert werden.
        if (resultFunction.containsIndefiniteIntegral()) {
            throw new NotAlgebraicallyIntegrableException();
        }
        // Rücksubstitution: y = g(x).
        return resultFunction.replaceVariable(substVar, algebraicTerm);

    }

    private static Expression getAlgebraicTermIfFunctionIsRationalFunctionInVarAndAnotherAlgebraicFunction(Expression f, String var) throws NotRationalFunctionInVarAndAnotherAlgebraicFunctionException {

        ExpressionCollection setOfSubstitutions = new ExpressionCollection();
        addSuitableSubstitutionForIntegrationOfAlgebraicFunctions(f, var, setOfSubstitutions);

        Expression algebraicTerm = null;
        for (Expression subst : setOfSubstitutions) {
            if (!subst.isPower() || !((BinaryOperation) subst).getRight().isRationalConstant()) {
                throw new NotRationalFunctionInVarAndAnotherAlgebraicFunctionException();
            }
            if (algebraicTerm == null) {
                algebraicTerm = subst;
            } else if (!algebraicTerm.equivalent(subst)) {
                throw new NotRationalFunctionInVarAndAnotherAlgebraicFunctionException();
            }
        }

        if (algebraicTerm == null) {
            // Dann ist die Funktion eine rationale Funktion (andere Methoden sind dann dafür zuständig). 
            throw new NotRationalFunctionInVarAndAnotherAlgebraicFunctionException();
        }

        // Schließlich: Prüfung, ob f eine rationale Funktion in x und g(x) = radicand ist.
        if (!SimplifyRationalFunctionMethods.isRationalFunctionInFunctions(f, var, Variable.create(var), algebraicTerm)) {
            throw new NotRationalFunctionInVarAndAnotherAlgebraicFunctionException();
        }

        return algebraicTerm;

    }

    // Typ 3: f(x^(1/n)) = 0, f = rationale Funktion.
    /**
     * Hauptmethode zum Integrieren von algebraischen Gleichungen der Form
     * f(x^(1/n)) = 0, f = rationale Funktion. Ist f keine solche Gleichung, so
     * wird eine NotAlgebraicallyIntegrableException geworfen.
     *
     * @throws EvaluationException, NotAlgebraicallyIntegrableException
     */
    public static Expression integrateRationalFunctionInRationalPowerOfVar(Operator expr) throws EvaluationException, NotAlgebraicallyIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        BigInteger exponentDenominator;
        try {
            exponentDenominator = getExponentIfFunctionIsRationalFunctionInRationalPowerOfVar(f, var);
        } catch (NotRationalFunctionInRationalPowerOfVarException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
        Expression substitution = Variable.create(var).pow(BigInteger.ONE, exponentDenominator);
        Expression fSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(f, substitution, Variable.create(substVar));

        if (fSubstituted.contains(var) || !SimplifyRationalFunctionMethods.isRationalFunction(fSubstituted, substVar)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression derivativeOfInverseTransformation = new Constant(exponentDenominator).mult(Variable.create(substVar).pow(exponentDenominator.subtract(BigInteger.ONE)));

        Expression integrandSubstituted = fSubstituted.mult(derivativeOfInverseTransformation);
        Expression resultFunction = indefiniteIntegration(new Operator(TypeOperator.integral, new Object[]{integrandSubstituted, substVar}), true);

        try {
            // Vor der Rücksubstitution vereinfachen.
            resultFunction = resultFunction.simplify();
        } catch (EvaluationException e) {
            // Sollte bei einer gültigen Stammfunktion nicht passieren.
            throw new NotAlgebraicallyIntegrableException();
        }
        // Falls noch unbestimmte Integrale auftauchen, dann konnte nicht ordentlich integriert werden.
        if (resultFunction.containsIndefiniteIntegral()) {
            throw new NotAlgebraicallyIntegrableException();
        }
        // Rücksubstitution: y = g(x).
        return resultFunction.replaceVariable(substVar, Variable.create(var).pow(BigInteger.ONE, exponentDenominator));

    }

    private static BigInteger getExponentIfFunctionIsRationalFunctionInRationalPowerOfVar(Expression f, String var) throws NotRationalFunctionInRationalPowerOfVarException {

        ExpressionCollection setOfSubstitutions = new ExpressionCollection();
        addSuitableSubstitutionForIntegrationOfAlgebraicFunctions(f, var, setOfSubstitutions);

        BigInteger lcmOfExponentDenominator = null;
        for (Expression subst : setOfSubstitutions) {
            if (!subst.isPower() || !((BinaryOperation) subst).getLeft().equals(Variable.create(var)) || !((BinaryOperation) subst).getRight().isRationalConstant()) {
                throw new NotRationalFunctionInRationalPowerOfVarException();
            }
            if (lcmOfExponentDenominator == null) {
                lcmOfExponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) subst).getRight()).getRight()).getBigIntValue().abs();
            } else {
                lcmOfExponentDenominator = ArithmeticMethods.lcm(lcmOfExponentDenominator,
                        ((Constant) ((BinaryOperation) ((BinaryOperation) subst).getRight()).getRight()).getBigIntValue().abs());
            }
        }

        if (lcmOfExponentDenominator == null || lcmOfExponentDenominator.equals(BigInteger.ONE)) {
            // Dann ist die Funktion eine rationale Funktion (andere Methoden sind dann dafür zuständig). 
            throw new NotRationalFunctionInRationalPowerOfVarException();
        }

        // Schließlich: Prüfung, ob f eine rationale Funktion in x und g(x) = radicand ist.
        if (!SimplifyRationalFunctionMethods.isRationalFunctionInFunctions(f, var, Variable.create(var).pow(BigInteger.ONE, lcmOfExponentDenominator))) {
            throw new NotRationalFunctionInRationalPowerOfVarException();
        }

        return lcmOfExponentDenominator;

    }

}
