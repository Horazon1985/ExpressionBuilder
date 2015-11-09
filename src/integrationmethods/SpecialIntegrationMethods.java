package integrationmethods;

import computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import exceptions.NotPreciseIntegrableException;
import exceptions.NotSubstitutableException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.MINUS_ONE;
import static expressionbuilder.Expression.ONE;
import static expressionbuilder.Expression.TWO;
import static expressionbuilder.Expression.ZERO;
import expressionbuilder.Function;
import expressionbuilder.Operator;
import expressionbuilder.TypeFunction;
import expressionbuilder.TypeOperator;
import expressionbuilder.TypeSimplify;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyExponentialRelations;
import expressionsimplifymethods.SimplifyPolynomialMethods;
import expressionsimplifymethods.SimplifyRationalFunctionMethods;
import expressionsimplifymethods.SimplifyTrigonometricalRelations;
import expressionsimplifymethods.SimplifyUtilities;
import static integrationmethods.SimplifyIntegralMethods.indefiniteIntegration;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import solveequationmethods.PolynomialRootsMethods;
import substitutionmethods.SubstitutionUtilities;

public abstract class SpecialIntegrationMethods {

    private static final HashSet<TypeSimplify> simplifyTypesRationalTrigonometricalFunctions = getSimplifyTypesRationalTrigonometricalFunctions();

    private static HashSet<TypeSimplify> getSimplifyTypesRationalTrigonometricalFunctions() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.collect_products);
        simplifyTypes.add(TypeSimplify.expand_moderate);
        simplifyTypes.add(TypeSimplify.simplify_powers);
        simplifyTypes.add(TypeSimplify.multiply_powers);
        simplifyTypes.add(TypeSimplify.factorize_all_but_rationals_in_sums);
        simplifyTypes.add(TypeSimplify.factorize_all_but_rationals_in_differences);
        simplifyTypes.add(TypeSimplify.reduce_quotients);
        simplifyTypes.add(TypeSimplify.reduce_leadings_coefficients);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypes.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypes.add(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions);
        return simplifyTypes;
    }

    /**
     * Hauptmethode für Partialbruchzerlegung. Gibt im Erfolgsfall die
     * Stammfunktion zurück (falls der Integrand eine rationale Funktion ist und
     * die Partialbruchzerlegung erfolgreich war), ansonsten false.
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateRationalFunction(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        // Falls f keine rationale Function ist -> abbrechen.
        if (!PartialFractionDecompositionMethods.isRationalFunctionInCanonicalForm(f, var)) {
            throw new NotPreciseIntegrableException();
        }

        /*
         Im Folgenden sind nur rationale Funktionen zugelassen: der Nenner
         enthält keine Parameter, der Zähler darf welche enthalten.
         */
        HashSet varsInDenominator = new HashSet();
        ((BinaryOperation) f).getRight().addContainedVars(varsInDenominator);
        if (!varsInDenominator.contains(var) || varsInDenominator.size() > 1) {
            /*
             Dies trifft AUCH DANN zu, wenn der Nenner Parameter enthält, aber
             von var nicht abhängt. Dann ist der Nenner bzgl. var konstant und
             kann vor das Integral getragen werden, was NICHT hier, sondern in
             SimplifyIntegralMethods.takeConstantsOutOfIntegral() geschieht.
             Daher -> beenden.
             */
            throw new NotPreciseIntegrableException();
        }

        BigInteger degreeEnumerator = SimplifyPolynomialMethods.degreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
        BigInteger degreeDenominator = SimplifyPolynomialMethods.degreeOfPolynomial(((BinaryOperation) f).getRight(), var);

        // Nur bei Graden <= gewisse Schranke fortfahren.
        if (degreeEnumerator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0
                || degreeDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotPreciseIntegrableException();
        }

        Expression partialFractionDecompositionOfF = PartialFractionDecompositionMethods.getPartialFractionDecomposition(f, var);

        if (f.equals(partialFractionDecompositionOfF)) {
            // Dann konnte f nicht in Partialbrüche zerlegt werden.
            throw new NotPreciseIntegrableException();
        }

        ExpressionCollection summands = SimplifyUtilities.getSummands(partialFractionDecompositionOfF);

        for (int i = 0; i < summands.getBound(); i++) {
            summands.put(i, new Operator(TypeOperator.integral, new Object[]{summands.get(i), var}).simplifyTrivial());
        }

        return SimplifyUtilities.produceSum(summands);

    }

    /**
     * Integriert (a*x + b)/(c*x^2 + d*x + e)^n mit ganzem n >= 2 und
     * ireduziblem Nenner.<br>
     * VORAUSSETZUNG: coefficientsEnumerator.size() == 2,
     * coefficientsDenominator.size() == 3 und die Elemente in
     * coefficientsEnumerator enthalten var nicht und die Elemente von
     * coefficientsDenominator sind konstant. Andernfalls wird false
     * zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateQuotientOfLinearAndPowerOfQuadraticPolynomial(Operator expr)
            throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient() || ((BinaryOperation) f).getRight().isNotPower()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().isIntegerConstant()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().isPositive()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var)) {
            throw new NotPreciseIntegrableException();
        }

        ExpressionCollection coefficientsEnumerator = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
        ExpressionCollection coefficientsDenominator = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);
        BigInteger exponent = ((Constant) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getValue().toBigInteger();

        if (exponent.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotPreciseIntegrableException();
        }

        int n = ((Constant) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getValue().intValue();

        if (coefficientsEnumerator.getBound() > 2 || coefficientsDenominator.getBound() != 3) {
            throw new NotPreciseIntegrableException();
        }

        // In den folgenden Kommentaren sei a = coefficientsEnumerator, b = coefficientsDenominator.
        Expression denominator = coefficientsDenominator.get(0);
        for (int i = 1; i < coefficientsDenominator.getBound(); i++) {
            denominator = denominator.add(coefficientsDenominator.get(i).mult(Variable.create(var)).pow(i));
        }

        // Falls in den Nennerkoeffizienten Parameter auftreten -> Fehler werfen.
        for (int i = 0; i < 3; i++) {
            if (!coefficientsDenominator.get(i).isConstant()) {
                throw new NotPreciseIntegrableException();
            }
        }

        // Falls bei a irgendwelche Koeffizienten fehlen -> mit Nullen auffüllen.
        if (coefficientsEnumerator.getBound() < 2) {
            for (int i = coefficientsEnumerator.getBound(); i < 2; i++) {
                coefficientsEnumerator.put(i, Expression.ZERO);
            }
        }

        Expression a = coefficientsDenominator.get(2);
        Expression b = coefficientsDenominator.get(1);
        Expression c = coefficientsDenominator.get(0);
        Expression d = coefficientsEnumerator.get(1);
        Expression e = coefficientsEnumerator.get(0);

        /*
         Falls der Nenner reduzibel ist -> Falsche Methode (es muss auf
         Partialbruchzerlegung zurückgegriffen werden).
         */
        Expression discriminant = new Constant(4).mult(a).mult(c).sub(b.pow(2)).simplify();
        if (discriminant.isNonPositive()) {
            throw new NotPreciseIntegrableException();
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
     * Integriert (a*x + b)/(c*x^2 + d*x + e) mit ireduziblem Nenner.
     * VORAUSSETZUNG: coefficientsEnumerator.size() == 2,
     * coefficientsDenominator.size() == 3 und die Elemente in
     * coefficientsEnumerator enthalten var nicht und die Elemente von
     * coefficientsDenominator sind konstant. Andernfalls wird false
     * zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateQuotientOfLinearAndQuadraticPolynomial(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getRight(), var)) {
            throw new NotPreciseIntegrableException();
        }

        ExpressionCollection coefficientsEnumerator = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
        ExpressionCollection coefficientsDenominator = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getRight(), var);

        if (coefficientsEnumerator.getBound() > 2 || coefficientsDenominator.getBound() != 3) {
            throw new NotPreciseIntegrableException();
        }

        // In den folgenden Kommentaren sei a = coefficientsEnumerator, b = coefficientsDenominator.
        // Falls in den Nennerkoeffizienten Parameter auftreten -> Fehler werfen.
        for (int i = 0; i < 3; i++) {
            if (!coefficientsDenominator.get(i).isConstant()) {
                throw new NotPreciseIntegrableException();
            }
        }

        // Falls bei a irgendwelche Koeffizienten fehlen -> mit Nullen auffüllen.
        if (coefficientsEnumerator.getBound() < 2) {
            for (int i = coefficientsEnumerator.getBound(); i < 2; i++) {
                coefficientsEnumerator.put(i, Expression.ZERO);
            }
        }

        /*
         Falls der Nenner reduzibel ist -> Falsche Methode (es muss auf
         Partialbruchzerlegung zurückgegriffen werden).
         */
        Expression discriminant = coefficientsDenominator.get(1).pow(2).sub(new Constant(4).mult(coefficientsDenominator.get(0)).mult(coefficientsDenominator.get(2))).simplify();
        if (!discriminant.isNonPositive() || discriminant.equals(Expression.ZERO)) {
            throw new NotPreciseIntegrableException();
        }

        /*
         Hilfsgrößen: p = a[1]/(2*b[2]), q = a[0]/b[2] - a[1]*b[1]/(2*b[2]^2),
         r = (-D/(4*b[0]^2))^(1/2). DANN: int(a[1]x + a[0])/(b[2]x^2 + b[1]x +
         b[0]) = p*ln(b[2]x^2 + b[1]x + b[0]) + (q/r)*arctan((2*b[2]*x +
         b[1])/(-D)^(1/2)) mit D = b[1]^2 - 4*b[2]*b[0].
         */
        Expression p = coefficientsEnumerator.get(1).div(Expression.TWO.mult(coefficientsDenominator.get(2))).simplify();
        Expression q = coefficientsEnumerator.get(0).div(coefficientsDenominator.get(2)).sub(coefficientsEnumerator.get(1).mult(coefficientsDenominator.get(1)).div(Expression.TWO.mult(coefficientsDenominator.get(2).pow(2)))).simplify();
        Expression r = new Constant(4).mult(coefficientsDenominator.get(2).pow(2)).div(Expression.MINUS_ONE.mult(discriminant)).pow(1, 2).simplify();

        // Log-Summanden bilden.
        Expression logSummand = p.mult(((BinaryOperation) f).getRight().ln());
        // Arctan-Summanden bilden.
        Expression arctanArgument = Expression.TWO.mult(coefficientsDenominator.get(2)).mult(Variable.create(var)).add(coefficientsDenominator.get(1)).div((Expression.MINUS_ONE.mult(discriminant)).pow(1, 2)).simplify();
        Expression arctanSummand = q.mult(r).mult(arctanArgument.arctan()).simplify();

        return logSummand.add(arctanSummand);

    }

    // Weitere Typen.
    private static boolean isExponentialFunction(Expression f, String var) {
        return f.isFunction(TypeFunction.exp) && SimplifyPolynomialMethods.isPolynomial(((Function) f).getLeft(), var)
                && SimplifyPolynomialMethods.degreeOfPolynomial(((Function) f).getLeft(), var).compareTo(BigInteger.ONE) == 0;
    }

    private static boolean isTrigonometricalFunction(Expression f, String var) {
        return (f.isFunction(TypeFunction.cos) || f.isFunction(TypeFunction.sin)) && SimplifyPolynomialMethods.isPolynomial(((Function) f).getLeft(), var)
                && SimplifyPolynomialMethods.degreeOfPolynomial(((Function) f).getLeft(), var).compareTo(BigInteger.ONE) == 0;
    }

    private static boolean isSumOfProductsOfExponentialAndTrigonometricalFunctions(Expression f, String var) {

        ExpressionCollection summands = SimplifyUtilities.getSummandsLeftInExpression(f);
        summands.add(SimplifyUtilities.getSummandsRightInExpression(f));

        ExpressionCollection factorsEnumerator, factorsDenominator;
        int numberOfExpFactors, numberOfTrigonometricalFactors;
        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) == null || !summands.get(i).contains(var)) {
                continue;
            }
            factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summands.get(i));
            factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(i));
            numberOfExpFactors = 0;
            numberOfTrigonometricalFactors = 0;
            for (int j = 0; j < factorsEnumerator.getBound(); j++) {
                if (factorsEnumerator.get(j).contains(var) && !isExponentialFunction(factorsEnumerator.get(j), var)
                        && !isTrigonometricalFunction(factorsEnumerator.get(j), var)) {
                    return false;
                }
                if (isExponentialFunction(factorsEnumerator.get(j), var)) {
                    numberOfExpFactors++;
                }
                if (isTrigonometricalFunction(factorsEnumerator.get(j), var)) {
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
        return f.simplify(var, TypeSimplify.order_difference_and_division, TypeSimplify.order_sums_and_products,
                TypeSimplify.simplify_trivial, TypeSimplify.simplify_powers, TypeSimplify.collect_products,
                TypeSimplify.reduce_quotients, TypeSimplify.reduce_leadings_coefficients,
                TypeSimplify.simplify_expand_products_of_complex_exponential_functions,
                TypeSimplify.expand_moderate);
    }

    /**
     * Integriert Funktionen vom Typ Polynom * f(a*x+b)^n, f = sin oder f = cos,
     * wobei der Polynomgrad unterhalb einer geeigneten Schranke liegt.
     *
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateProductOfPolynomialAndPowerOfTrigonometricFunction(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotProduct()) {
            throw new NotPreciseIntegrableException();
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(f);

        if (factors.getBound() != 2) {
            throw new NotPreciseIntegrableException();
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

            BigInteger degPolynomial = SimplifyPolynomialMethods.degreeOfPolynomial(factors.get(0), var);
            BigInteger exponentOfTrigonometricFunction = ((Constant) ((BinaryOperation) factors.get(1)).getRight()).getValue().toBigInteger();
            BigInteger degArgument = SimplifyPolynomialMethods.degreeOfPolynomial(((Function) ((BinaryOperation) factors.get(1)).getLeft()).getLeft(), var);

            if (degPolynomial.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0
                    || exponentOfTrigonometricFunction.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0
                    || degArgument.compareTo(BigInteger.ONE) > 0) {
                throw new NotPreciseIntegrableException();
            }

            f = expandProductsOfComplexExponentialFunctions(f, var);
            Operator exprExpanded = new Operator(TypeOperator.integral, new Object[]{f, var});
            // Jetzt gelingt eine Integration mittels partieller Integration.
            return indefiniteIntegration(exprExpanded, true);

        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Integriert Funktionen vom Typ Polynom in Exponentialfunktionen und
     * trigonometrischen Funktionen, falls der Polynomgrad unterhalb einer
     * geeigneten Schranke liegt.
     *
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePolynomialInComplexExponentialFunctions(Operator expr) throws NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (!BinaryOperation.isPolynomialInVariousExponentialAndTrigonometricalFunctions(f, var)) {
            throw new NotPreciseIntegrableException();
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

        throw new NotPreciseIntegrableException();

    }

    private static Expression integrateSummandOfPolynomialInExponentialAndTrigonometricalFunctions(Expression f, String var) throws EvaluationException, NotPreciseIntegrableException {

        // Zuerst: Fall eines konstanten Summanden:
        if (!f.contains(var)) {
            return f.mult(Variable.create(var));
        }

        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(f);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
        ExpressionCollection constantfactorsEnumerator = new ExpressionCollection();
        ExpressionCollection constantfactorsDenominator = new ExpressionCollection();

        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (!factorsEnumerator.get(i).contains(var)) {
                constantfactorsEnumerator.add(factorsEnumerator.get(i));
                factorsEnumerator.remove(i);
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (!factorsDenominator.get(i).contains(var)) {
                constantfactorsDenominator.add(factorsDenominator.get(i));
                factorsDenominator.remove(i);
            }
        }

        if (!factorsDenominator.isEmpty() || factorsEnumerator.getSize() > 2) {
            throw new NotPreciseIntegrableException();
        }

        // Jetzt wird die Stammfunktion, abhängig von der Gestalt des Summanden, explizit bestimmt.
        ExpressionCollection factors = new ExpressionCollection(factorsEnumerator);
        Expression constantEnumerator = SimplifyUtilities.produceProduct(constantfactorsEnumerator);
        Expression constantDenominator = SimplifyUtilities.produceProduct(constantfactorsDenominator);

        // Fall: Integration von exp(a*x+b).
        if (factors.getBound() == 1 && factors.get(0).isFunction(TypeFunction.exp)) {
            Expression integral = integrateExpOfLinearFunction(((Function) factors.get(0)).getLeft(), var);
            return constantEnumerator.mult((Expression) integral).div(constantDenominator);
        }
        // Fall: Integration von cos(a*x+b).
        if (factors.getBound() == 1 && factors.get(0).isFunction(TypeFunction.cos)) {
            Expression integral = integrateCosOfLinearFunction(((Function) factors.get(0)).getLeft(), var);
            return constantEnumerator.mult((Expression) integral).div(constantDenominator);
        }
        // Fall: Integration von sin(a*x+b).
        if (factors.getBound() == 1 && factors.get(0).isFunction(TypeFunction.sin)) {
            Expression integral = integrateSinOfLinearFunction(((Function) factors.get(0)).getLeft(), var);
            return constantEnumerator.mult((Expression) integral).div(constantDenominator);
        }
        // Fall: Integration von exp(a*x+b)*cos(c*x+d).
        if (factors.getBound() == 2 && factors.get(0).isFunction(TypeFunction.exp) && factors.get(1).isFunction(TypeFunction.cos)) {
            Expression integral = integrateProductOfExpCos(((Function) factors.get(0)).getLeft(), ((Function) factors.get(1)).getLeft(), var);
            return constantEnumerator.mult((Expression) integral).div(constantDenominator);
        }
        // Fall: Integration von cos(a*x+b)*exp(c*x+d).
        if (factors.getBound() == 2 && factors.get(0).isFunction(TypeFunction.cos) && factors.get(1).isFunction(TypeFunction.exp)) {
            Expression integral = integrateProductOfExpCos(((Function) factors.get(1)).getLeft(), ((Function) factors.get(0)).getLeft(), var);
            return constantEnumerator.mult((Expression) integral).div(constantDenominator);
        }
        // Fall: Integration von exp(a*x+b)*sin(c*x+d).
        if (factors.getBound() == 2 && factors.get(0).isFunction(TypeFunction.exp) && factors.get(1).isFunction(TypeFunction.sin)) {
            Expression integral = integrateProductOfExpSin(((Function) factors.get(0)).getLeft(), ((Function) factors.get(1)).getLeft(), var);
            return constantEnumerator.mult((Expression) integral).div(constantDenominator);
        }
        // Fall: Integration von sin(a*x+b)*exp(c*x+d).
        if (factors.getBound() == 2 && factors.get(0).isFunction(TypeFunction.sin) && factors.get(1).isFunction(TypeFunction.exp)) {
            Expression integral = integrateProductOfExpSin(((Function) factors.get(1)).getLeft(), ((Function) factors.get(0)).getLeft(), var);
            return constantEnumerator.mult((Expression) integral).div(constantDenominator);
        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Explizite Integration von exp(a*x+b).<br>
     *
     * @throws EvaluationException
     */
    private static Expression integrateExpOfLinearFunction(Expression expArgument, String var) throws EvaluationException, NotPreciseIntegrableException {
        if (!SimplifyPolynomialMethods.isPolynomial(expArgument, var)
                || SimplifyPolynomialMethods.degreeOfPolynomial(expArgument, var).compareTo(BigInteger.ONE) != 0) {
            throw new NotPreciseIntegrableException();
        }
        ExpressionCollection coefficients = solveequationmethods.PolynomialRootsMethods.getPolynomialCoefficients(expArgument, var);
        return expArgument.exp().div(coefficients.get(1));
    }

    /**
     * Explizite Integration von cos(a*x+b).<br>
     *
     * @throws EvaluationException
     */
    private static Expression integrateCosOfLinearFunction(Expression expArgument, String var) throws EvaluationException, NotPreciseIntegrableException {
        if (!SimplifyPolynomialMethods.isPolynomial(expArgument, var)
                || SimplifyPolynomialMethods.degreeOfPolynomial(expArgument, var).compareTo(BigInteger.ONE) != 0) {
            throw new NotPreciseIntegrableException();
        }
        ExpressionCollection coefficients = solveequationmethods.PolynomialRootsMethods.getPolynomialCoefficients(expArgument, var);
        return expArgument.sin().div(coefficients.get(1));
    }

    /**
     * Explizite Integration von sin(a*x+b).<br>
     *
     * @throws EvaluationException
     */
    private static Expression integrateSinOfLinearFunction(Expression expArgument, String var) throws EvaluationException, NotPreciseIntegrableException {
        if (!SimplifyPolynomialMethods.isPolynomial(expArgument, var)
                || SimplifyPolynomialMethods.degreeOfPolynomial(expArgument, var).compareTo(BigInteger.ONE) != 0) {
            throw new NotPreciseIntegrableException();
        }
        ExpressionCollection coefficients = solveequationmethods.PolynomialRootsMethods.getPolynomialCoefficients(expArgument, var);
        return MINUS_ONE.mult(expArgument.cos()).div(coefficients.get(1));
    }

    /**
     * Integration von exp(a*x+b)*sin(c*x+d).
     *
     * @throws EvaluationException
     */
    public static Expression integrateProductOfExpSin(Expression expArgument, Expression sinArgument, String var) throws EvaluationException, NotPreciseIntegrableException {

        ExpressionCollection coefficientsInExp = PolynomialRootsMethods.getPolynomialCoefficients(expArgument, var);
        ExpressionCollection coefficientsInSin = PolynomialRootsMethods.getPolynomialCoefficients(sinArgument, var);
        if (coefficientsInExp.getBound() != 2 || coefficientsInSin.getBound() != 2) {
            throw new NotPreciseIntegrableException();
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
    public static Expression integrateProductOfExpCos(Expression expArgument, Expression cosArgument, String var) throws EvaluationException, NotPreciseIntegrableException {

        ExpressionCollection coefficientsInExp = PolynomialRootsMethods.getPolynomialCoefficients(expArgument, var);
        ExpressionCollection coefficientsInCos = PolynomialRootsMethods.getPolynomialCoefficients(cosArgument, var);
        if (coefficientsInExp.getBound() != 2 || coefficientsInCos.getBound() != 2) {
            throw new NotPreciseIntegrableException();
        }

        /*
         Es wird int(exp(a*x+b)*cos(c*x+d), x) =
         exp(a*x+b)*(a*cos(c*x+d)+c*sin(c*x+d))/(a^2+c^2) zurückgegeben.
         */
        return expArgument.exp().mult(coefficientsInExp.get(1).mult(cosArgument.cos()).add(coefficientsInCos.get(1).mult(cosArgument.sin()))).div(
                coefficientsInExp.get(1).pow(2).add(coefficientsInCos.get(1).pow(2)));

    }

    /**
     * Integration von (a*x^2 + b*x + c)^(1/2). VORAUSSETZUNG: expr ist ein
     * unbestimmtes Integral. Falls der Integrand nicht vom angegebenen Typ ist,
     * so wird false zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    private static Expression integrateSqrtOfQuadraticFunction(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotPower() || !((BinaryOperation) f).getRight().equals(Expression.ONE.div(Expression.TWO))
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)) {
            throw new NotPreciseIntegrableException();
        }

        ExpressionCollection coefficients = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);

        if (coefficients.getBound() != 3) {
            throw new NotPreciseIntegrableException();
        }

        // Falls in den Koeffizienten Parameter auftreten, die das Vorzeichen ändern können -> false zurückgeben.
        for (int i = 0; i < 2; i++) {
            if (!coefficients.get(i).isConstant() && !coefficients.get(i).isAlwaysNonNegative() && !(Expression.MINUS_ONE).mult(coefficients.get(i)).simplify().isAlwaysNonNegative()) {
                throw new NotPreciseIntegrableException();
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
        throw new NotPreciseIntegrableException();

    }

    /**
     * Integration von (a*x^2 + b*x + c)^((2*n+1)/2).<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral. Falls der Integrand
     * nicht vom angegebenen Typ ist, so wird false zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateOddPowerOfSqrtOfQuadraticFunction(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotPower()
                || !((BinaryOperation) f).getRight().isRationalConstant()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft().isOddConstant()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft().isPositive()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().equals(TWO)
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)) {
            throw new NotPreciseIntegrableException();
        }

        ExpressionCollection coefficients = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);

        if (coefficients.getBound() != 3) {
            throw new NotPreciseIntegrableException();
        }

        // Falls in den Koeffizienten Parameter auftreten, die das Vorzeichen ändern können -> false zurückgeben.
        for (int i = 0; i < 2; i++) {
            if (!coefficients.get(i).isConstant() && !coefficients.get(i).isAlwaysNonNegative() && !(Expression.MINUS_ONE).mult(coefficients.get(i)).simplify().isAlwaysNonNegative()) {
                throw new NotPreciseIntegrableException();
            }
        }

        // Im Folgenden sei a = a.get(2), b = a.get(1), c = a.get(0), discriminant = D = b^2 - 4*a*c.
        Expression a = coefficients.get(2);
        Expression b = coefficients.get(1);
        Expression c = coefficients.get(0);
        Expression discriminant = b.pow(2).sub(new Constant(4).mult(a).mult(c)).simplify();

        BigInteger exponentEnumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft()).getValue().toBigInteger();

        if (exponentEnumerator.compareTo(BigInteger.valueOf(2 * ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotPreciseIntegrableException();
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
     * Integration von 1/(a*x^2 + b*x + c)^(1/2).<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral. Falls der Integrand
     * nicht vom angegebenen Typ ist, so wird false zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    private static Expression integrateReciprocalOfSqrtOfQuadraticFunction(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient() || !((BinaryOperation) f).getLeft().equals(Expression.ONE)
                || ((BinaryOperation) f).getRight().isNotPower()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().equals(Expression.ONE.div(Expression.TWO))
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var)) {
            throw new NotPreciseIntegrableException();
        }

        ExpressionCollection coefficients = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);

        if (coefficients.getBound() != 3) {
            throw new NotPreciseIntegrableException();
        }

        // Falls in den Koeffizienten Parameter auftreten, die das Vorzeichen ändern können -> Fehler werfen.
        for (int i = 0; i < 2; i++) {
            if (!coefficients.get(i).isConstant() && !coefficients.get(i).isAlwaysNonNegative() && !(Expression.MINUS_ONE).mult(coefficients.get(i)).simplify().isAlwaysNonNegative()) {
                throw new NotPreciseIntegrableException();
            }
        }

        // Im Folgenden sei a = a.get(2), b = a.get(1), c = a.get(0), diskr = D = b^2 - 4*a*c.
        Expression discriminant = coefficients.get(1).pow(2).sub(new Constant(4).mult(coefficients.get(2)).mult(coefficients.get(0))).simplify();

        if (discriminant.equals(Expression.ZERO) && coefficients.get(2).isAlwaysNonNegative() && !coefficients.get(2).equals(Expression.ZERO)) {
            // Dann ist f = 1/(a^(1/2)*|x - x_1|) mit x_1 = -b/(2*a).
            Expression zero = Expression.MINUS_ONE.mult(coefficients.get(1)).div(Expression.TWO.mult(coefficients.get(2))).simplify();
            // F = sgn(x - x_1)*ln(|x - x_1|)/a^(1/2).
            return Variable.create(var).sub(zero).sgn().mult(
                    Variable.create(var).sub(zero).abs().ln()).div(coefficients.get(2).pow(1, 2));
        }
        if (!discriminant.equals(Expression.ZERO) && (discriminant.isNonNegative() || discriminant.isAlwaysNonNegative())) {
            // Hier ist D > 0.
            if ((coefficients.get(2).isNonNegative() || coefficients.get(2).isAlwaysNonNegative()) && !coefficients.get(2).equals(Expression.ZERO)) {

                // Fall a > 0, D > 0. Reduktion auf den Typ int(1/(x^2 - 1)^(1/2), x).
                // F = arcosh((2*a*x + b)/D^(1/2))/(a^(1/2)).
                return Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).div(discriminant.pow(1, 2)).arcosh().div(coefficients.get(2).pow(1, 2));

            }
            if ((coefficients.get(2).isNonPositive() || Expression.MINUS_ONE.mult(coefficients.get(2)).simplify().isAlwaysNonNegative()) && !coefficients.get(2).equals(Expression.ZERO)) {

                // Fall a < 0, D > 0. Reduktion auf den Typ int(1/(1 - x^2)^(1/2), x).
                // F = arcsin((-2*a*x - b)/D^(1/2))/((-a)^(1/2)).
                return new Constant(-2).mult(coefficients.get(2)).mult(Variable.create(var)).sub(coefficients.get(1)).div(discriminant.pow(1, 2)).arcsin().div(Expression.MINUS_ONE.mult(coefficients.get(2)).pow(1, 2));

            }
        }
        if (!discriminant.equals(Expression.ZERO) && (discriminant.isNonPositive() || Expression.MINUS_ONE.mult(discriminant).simplify().isAlwaysNonNegative())
                && (coefficients.get(2).isNonNegative() || coefficients.get(2).isAlwaysNonNegative()) && !coefficients.get(2).equals(Expression.ZERO)) {

            // Fall a > 0, D < 0. Reduktion auf den Typ int(1/(x^2 + 1)^(1/2), x).
            // F = arsinh((2*a*x + b)/(-D)^(1/2))/(a^(1/2)).
            return Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).div(Expression.MINUS_ONE.mult(discriminant).pow(1, 2)).arsinh().div(coefficients.get(2).pow(1, 2));

        }

        // Übrige, nicht eindeutig entscheidbare Fälle.
        throw new NotPreciseIntegrableException();

    }

    /**
     * Integration von 1/(a*x^2 + b*x + c)^((2*n + 1)/2).<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral. Falls der Integrand
     * nicht vom angegebenen Typ ist, so wird false zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    private static Expression integrateReciprocalOfOddPowerOfSqrtOfQuadraticFunction(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient() || !((BinaryOperation) f).getLeft().equals(Expression.ONE)
                || ((BinaryOperation) f).getRight().isNotPower()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().isRationalConstant()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var)) {
            throw new NotPreciseIntegrableException();
        }

        int n;

        if (!((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getRight().equals(TWO)
                || !((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getLeft().isOddConstant()
                || !((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getLeft().isPositive()) {
            throw new NotPreciseIntegrableException();
        }

        BigInteger exponentEnumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getLeft()).getValue().toBigInteger();

        if (exponentEnumerator.compareTo(BigInteger.valueOf(2 * ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotPreciseIntegrableException();
        }

        n = (exponentEnumerator.intValue() - 1) / 2;

        if (n == 0) {
            return integrateReciprocalOfSqrtOfQuadraticFunction(expr);
        }

        ExpressionCollection coefficients = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);

        if (coefficients.getBound() != 3) {
            throw new NotPreciseIntegrableException();
        }

        // Falls in den Koeffizienten Parameter auftreten, die das Vorzeichen ändern können -> fehler werfen.
        for (int i = 0; i < 2; i++) {
            if (!coefficients.get(i).isConstant()
                    && !coefficients.get(i).isAlwaysNonNegative()
                    && !coefficients.get(i).isAlwaysNonPositive()) {
                throw new NotPreciseIntegrableException();
            }
        }

        // Im Folgenden sei a = a.get(2), b = a.get(1), c = a.get(0), diskr = D = 4*a*c - b^2, x = var.
        Expression a = coefficients.get(2);
        Expression b = coefficients.get(1);
        Expression c = coefficients.get(0);
        Expression discriminant = new Constant(4).mult(a).mult(c).sub(b.pow(2)).simplify();
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
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral. Falls der Integrand
     * nicht vom angegebenen Typ ist, so wird false zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateQuotientOfLinearAndOddPowerOfSqrtOfQuadraticFunction(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)
                || ((BinaryOperation) f).getRight().isNotPower()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().isRationalConstant()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var)) {
            throw new NotPreciseIntegrableException();
        }

        ExpressionCollection coefficientsEnumerator = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
        ExpressionCollection coefficientsDenominator = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);

        if (coefficientsEnumerator.getBound() > 2 || coefficientsDenominator.getBound() != 3) {
            throw new NotPreciseIntegrableException();
        }

        // Fehlende Koeffizienten im Zählen mit Nullen auffüllen.
        for (int i = 0; i < 2; i++) {
            if (coefficientsEnumerator.get(i) == null) {
                coefficientsEnumerator.put(i, ZERO);
            }
        }

        int n;

        if (!((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getRight().equals(TWO)
                || !((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getLeft().isOddConstant()
                || !((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getLeft().isPositive()) {
            throw new NotPreciseIntegrableException();
        }

        BigInteger exponentEnumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getLeft()).getValue().toBigInteger();

        if (exponentEnumerator.compareTo(BigInteger.valueOf(2 * ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotPreciseIntegrableException();
        }

        n = (exponentEnumerator.intValue() - 1) / 2;

        if (coefficientsDenominator.getBound() != 3) {
            throw new NotPreciseIntegrableException();
        }

        // Falls in den Koeffizienten Parameter auftreten, die das Vorzeichen ändern können -> Fehler werfen.
        for (int i = 0; i < 2; i++) {
            if (!coefficientsDenominator.get(i).isConstant() && !coefficientsDenominator.get(i).isAlwaysNonNegative()
                    && !coefficientsDenominator.get(i).isAlwaysNonPositive()) {
                throw new NotPreciseIntegrableException();
            }
        }

        // In den folgenden Kommentaren sei a = coefficientsEnumerator, b = coefficientsDenominator.
        // Im Folgenden sei a = a.get(2), b = a.get(1), c = a.get(0), d = b.get(1), e = b.get(0), x = var.
        Expression a = coefficientsDenominator.get(2);
        Expression b = coefficientsDenominator.get(1);
        Expression d = coefficientsEnumerator.get(1);
        Expression e = coefficientsEnumerator.get(0);

        // radicand = a*x^2 + b*x + c
        Expression radicand = ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft();

        // firstSummand = d/((1-2*n)*a*R^((2*n-1)/2)
        Expression firstSummand = d.div(new Constant(1 - 2 * n).mult(radicand.pow(2 * n - 1, 2)));

        // factor = e - (d*b)/(2*a)
        Expression factor = e.sub(d.mult(b).div(TWO.mult(a)));

        // Integral = firstSummand + factor*int(1/(a*x^2+b*x+c)^((2*n + 1)/2),x).
        Object[] params = new Object[2];
        params[0] = ONE.div(((BinaryOperation) f).getRight());
        params[1] = var;
        Expression integralOfLowerPower = integrateReciprocalOfOddPowerOfSqrtOfQuadraticFunction(new Operator(TypeOperator.integral, params));

        return firstSummand.add(factor.mult(integralOfLowerPower));

    }

    /**
     * Integriert Funktionen vom Typ R(exp(a*x)), R = rationale Funktion.
     *
     * @throws exceptions.EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateRationalFunctionInExp(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        HashSet<Expression> argumentsInExp = new HashSet<>();

        // Konstante Summanden aus Argumenten in Exponentialfunktionen herausziehen.
        f = SimplifyExponentialRelations.separateConstantPartsInRationalExponentialEquations(f, var);

        if (!SimplifyRationalFunctionMethods.isRationalFunktionInExp(f, var, argumentsInExp) || argumentsInExp.isEmpty()) {
            throw new NotPreciseIntegrableException();
        }

        BigInteger gcdOfEnumerators = BigInteger.ONE;
        BigInteger lcmOfDenominators = BigInteger.ONE;

        Iterator<Expression> iter = argumentsInExp.iterator();
        Expression firstArgument = iter.next();

        Expression derivativeOfFirstArgument;
        try {
            derivativeOfFirstArgument = firstArgument.diff(var).simplify();
            if (derivativeOfFirstArgument.contains(var)) {
                throw new NotPreciseIntegrableException();
            }
        } catch (EvaluationException e) {
            throw new NotPreciseIntegrableException();
        }

        Expression currentQuotient;

        while (iter.hasNext()) {
            currentQuotient = iter.next().div(firstArgument).simplify();
            // Die folgende Abfrage müsste wegen Vorbedingung immer true sein. Trotzdem sicherheitshalber!
            if (currentQuotient.isIntegerConstantOrRationalConstant()) {

                if (currentQuotient.isIntegerConstant()) {
                    gcdOfEnumerators = gcdOfEnumerators.gcd(((Constant) currentQuotient).getValue().toBigInteger());
                } else {
                    gcdOfEnumerators = gcdOfEnumerators.gcd(((Constant) ((BinaryOperation) currentQuotient).getLeft()).getValue().toBigInteger());
                    lcmOfDenominators = ArithmeticMethods.lcm(lcmOfDenominators,
                            ((Constant) ((BinaryOperation) currentQuotient).getRight()).getValue().toBigInteger());
                }

            }
        }

        // Das ist die eigentliche Substitution.
        Expression factorOfExpArgument = new Constant(gcdOfEnumerators).mult(derivativeOfFirstArgument).div(lcmOfDenominators).simplify();
        Expression substitution = new Constant(gcdOfEnumerators).mult(firstArgument).div(lcmOfDenominators).exp().simplify();

        Object fSubstituted = SubstitutionUtilities.substitute(f, var, substitution, true);
        if (fSubstituted instanceof Expression) {

            String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
            /*
             Das Folgende ist eine Sicherheitsabfrage: Die substituierte Gleichung sollte vom 
             folgenden Typ sein: Alle Argumente, die in trigonometrischen Funktionen vorkommen,
             müssen von der Form n*x sein, wobei n eine ganze Zahl und x eine Variable ist.
             */
            if (!SimplifyRationalFunctionMethods.doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable((Expression) fSubstituted, substVar)) {
                throw new NotPreciseIntegrableException();
            }

            Expression substitutedIntegrand = ((Expression) fSubstituted).div(factorOfExpArgument.mult(Variable.create(substVar)));

            Operator substitutedIntegral = new Operator(TypeOperator.integral, new Object[]{substitutedIntegrand, substVar});
            Expression resultFunction = indefiniteIntegration(substitutedIntegral, true);
            return resultFunction.replaceVariable(substVar, factorOfExpArgument.mult(Variable.create(var)).exp());
        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Integriert Funktionen vom Typ R(sin(a*x), cos(a*x)), R = rationale
     * Funktion.
     *
     * @throws exceptions.EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateRationalFunctionInTrigonometricFunctions(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        HashSet<Expression> argumentsInTrigonometricalFunctions = new HashSet<>();

        // Konstante Summanden aus Argumenten in Exponentialfunktionen herausziehen.
        f = SimplifyTrigonometricalRelations.separateConstantPartsInRationalTrigonometricalEquations(f, var);

        if (!SimplifyRationalFunctionMethods.isRationalFunktionInTrigonometricalFunctions(f, var, argumentsInTrigonometricalFunctions) || argumentsInTrigonometricalFunctions.isEmpty()) {
            throw new NotPreciseIntegrableException();
        }

        BigInteger gcdOfEnumerators = BigInteger.ONE;
        BigInteger lcmOfDenominators = BigInteger.ONE;

        Iterator<Expression> iter = argumentsInTrigonometricalFunctions.iterator();
        Expression firstArgument = iter.next();

        Expression derivativeOfFirstArgument;
        try {
            derivativeOfFirstArgument = firstArgument.diff(var).simplify();
            if (derivativeOfFirstArgument.contains(var)) {
                throw new NotPreciseIntegrableException();
            }
        } catch (EvaluationException e) {
            throw new NotPreciseIntegrableException();
        }

        Expression currentQuotient;

        while (iter.hasNext()) {
            currentQuotient = iter.next().div(firstArgument).simplify();
            // Die folgende Abfrage müsste wegen Vorbedingung immer true sein. Trotzdem sicherheitshalber!
            if (currentQuotient.isIntegerConstantOrRationalConstant()) {

                if (currentQuotient.isIntegerConstant()) {
                    gcdOfEnumerators = gcdOfEnumerators.gcd(((Constant) currentQuotient).getValue().toBigInteger());
                } else {
                    gcdOfEnumerators = gcdOfEnumerators.gcd(((Constant) ((BinaryOperation) currentQuotient).getLeft()).getValue().toBigInteger());
                    lcmOfDenominators = ArithmeticMethods.lcm(lcmOfDenominators,
                            ((Constant) ((BinaryOperation) currentQuotient).getRight()).getValue().toBigInteger());
                }

            }
        }

        // Das ist die eigentliche Substitution.
        Expression factorOfTrigonometricalArgument = new Constant(gcdOfEnumerators).mult(derivativeOfFirstArgument).div(lcmOfDenominators).simplify();
        Expression substitution = new Constant(gcdOfEnumerators).mult(firstArgument).div(lcmOfDenominators).simplify();

        Object fSubstitutedAsObject = SubstitutionUtilities.substitute(f, var, substitution, true);
        if (fSubstitutedAsObject instanceof Expression) {
            Expression fSubstituted = (Expression) fSubstitutedAsObject;
            String substVar = SubstitutionUtilities.getSubstitutionVariable(f);

            /*
             Das Folgende ist eine Sicherheitsabfrage: Die substituierte Gleichung sollte vom 
             folgenden Typ sein: Alle Argumente, die in trigonometrischen Funktionen vorkommen,
             müssen von der Form n*x sein, wobei n eine ganze Zahl und x eine Variable ist.
             */
            if (!SimplifyRationalFunctionMethods.doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable(fSubstituted, substVar)) {
                throw new NotPreciseIntegrableException();
            }

            /*
             Nun ist fSubstituted eine rationale Funtion in sin(n*X_1) und cos(m*X_1), X_1 = substVar.
             Jetzt muss fSubstituted als rationale Function in sin(X_1) und cos(X_1) dargestellt werden.
             WICHTIG: Beim Vereinfachen darf hier nicht simplifyFunctionalRelations() verwendet werden,
             da dann beispielsweise sin(x)*cos(x) wieder zu sin(2*x)/2 vereinfacht wird.
             */
            fSubstituted = SimplifyRationalFunctionMethods.expandRationalFunctionInTrigonometricalFunctions(fSubstituted, substVar).simplify(simplifyTypesRationalTrigonometricalFunctions);
            /*
             Jetzt erfolgt die eigentliche Substitution: cos(x) = (1-t^2)/(1+t^2),
             sin(x) = 2t/(1+t^2), dx = 2/(1+t^2)*dt.
             */
            String substVarForIntegral = SubstitutionUtilities.getSubstitutionVariable(fSubstituted);
            Expression substForCos = ONE.sub(Variable.create(substVarForIntegral).pow(2)).div(ONE.add(Variable.create(substVarForIntegral).pow(2)));
            Expression substForSin = TWO.mult(Variable.create(substVarForIntegral)).div(ONE.add(Variable.create(substVarForIntegral).pow(2)));
            Expression substForDX = TWO.div(ONE.add(Variable.create(substVarForIntegral).pow(2)));

            Expression substitutedIntegrand = substituteExpressionByAnotherExpression(fSubstituted,
                    Variable.create(substVar).cos(), substForCos);
            substitutedIntegrand = substituteExpressionByAnotherExpression(substitutedIntegrand,
                    Variable.create(substVar).sin(), substForSin);

            if (!substitutedIntegrand.contains(substVarForIntegral)) {
                // Dann konnte weder sin, noch cos substitutiert werden. Dies sollte hier eigentlich nicht passieren.
                throw new NotPreciseIntegrableException();
            }

            substitutedIntegrand = substitutedIntegrand.mult(substForDX).div(factorOfTrigonometricalArgument);
            Operator substitutedIntegral = new Operator(TypeOperator.integral, new Object[]{substitutedIntegrand, substVarForIntegral});
            Expression resultFunction = indefiniteIntegration(substitutedIntegral, true);
            // Falls noch unbestimmte Integrale auftauchen, dann konnte nicht ordentlich integriert werden.
            if (resultFunction.containsIndefiniteIntegral()) {
                throw new NotPreciseIntegrableException();
            }
            return resultFunction.replaceVariable(substVarForIntegral,
                    TWO.mult(factorOfTrigonometricalArgument.mult(Variable.create(var)).arctan()));

        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Elementare Hilfsmethode für
     * integrateRationalFunctionInTrigonometricalFunctions(). Substituiert,
     * falls möglich, im Ausdruck f Ausdrücke, die äquivalent sind zu
     * exprToSubstitute, durch subst. Andernfalls wird eine
     * NotSubstitutableException geworfen.
     *
     * @throws NotSubstitutableException
     */
    private static Expression substituteExpressionByAnotherExpression(Expression f, Expression exprToSubstitute, Expression subst)
            throws EvaluationException {

        if (f.equivalent(exprToSubstitute)) {
            return subst;
        }
        if (f instanceof BinaryOperation) {
            return new BinaryOperation(substituteExpressionByAnotherExpression(((BinaryOperation) f).getLeft(), exprToSubstitute, subst),
                    substituteExpressionByAnotherExpression(((BinaryOperation) f).getRight(), exprToSubstitute, subst),
                    ((BinaryOperation) f).getType());
        }
        if (f.isFunction()) {
            return new Function(substituteExpressionByAnotherExpression(((Function) f).getLeft(), exprToSubstitute, subst), ((Function) f).getType());
        }

        return f;

    }

}
