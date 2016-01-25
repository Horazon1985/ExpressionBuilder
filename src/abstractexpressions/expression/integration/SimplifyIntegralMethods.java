package abstractexpressions.expression.integration;

import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import exceptions.NotPreciseIntegrableException;
import exceptions.NotSubstitutableException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.TWO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.SelfDefinedFunction;
import abstractexpressions.expression.classes.TypeBinary;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.TypeOperator;
import enums.TypeSimplify;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyExpLog;
import abstractexpressions.expression.utilities.SimplifyPolynomialMethods;
import abstractexpressions.expression.utilities.SimplifyUtilities;
import java.math.BigInteger;
import java.util.HashSet;
import abstractexpressions.expression.substitution.SubstitutionUtilities;

public abstract class SimplifyIntegralMethods {

    private static final HashSet<TypeSimplify> simplifyTypesPrepareIntegrand = getSimplifyTypesPrepareIntegrand();
    private static final HashSet<TypeSimplify> simplifyTypesPrepareDominatorOfIntegrand = getSimplifyTypesPrepareDominatorOfIntegrand();
    private static final HashSet<TypeSimplify> simplifyTypesMultiplyOutIntegrand = getSimplifyTypesMultiplyOutIntegrand();

    private static HashSet<TypeSimplify> getSimplifyTypesPrepareIntegrand() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
        return simplifyTypes;
    }

    private static HashSet<TypeSimplify> getSimplifyTypesPrepareDominatorOfIntegrand() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_expand_moderate);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
        return simplifyTypes;
    }

    private static HashSet<TypeSimplify> getSimplifyTypesMultiplyOutIntegrand() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.simplify_expand_moderate);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        return simplifyTypes;
    }

    /**
     * Vereinfacht den Integranden (sinnvoll) für eine bequemere Integration.
     * Beispiel: f = (x^2+x)*exp(x)*(x-2) wäre zu kompliziert mittels partieller
     * Integration zu integrieren. Sinnvoller wäre es, die beiden Polynome
     * auszumultiplizieren und DANN zu integrieren, also den Integranden f =
     * (x^3 - x^2 - 2*x)*exp(x).<br>
     * WICHTIG: Falls der Integrand f ein Bruch ist, dann werden Zähler und
     * Nenner verschieden vereinfacht.
     *
     * @throws EvaluationException
     */
    public static Expression prepareIntegrand(Expression f, String var) throws EvaluationException {

        if (f.isQuotient()) {
            f = prepareIntegrand(((BinaryOperation) f).getLeft(), var).div(prepareDominatorOfIntegrand(((BinaryOperation) f).getRight(), var));
        }

        // Wenn in den Faktoren MEHR als ein Polynom auftaucht -> Polynome ausmultiplizieren.
        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        Expression polynomialFactor = Expression.ONE;
        /*
         Counter, welcher zählt, wie viele Polynome im Produkt auftreten.
         SINN: Falls mindestens ein Polynom auftreten -> vereinfachen (bzw.
         bei mindestens zwei Polynomen alle ausmultiplizieren und dann
         vereinfachen).
         */
        int numberOfPolynomials = 0;
        // Index des letzten Polynoms im Produkt.
        int indexOfLastPolynomial = -1;

        for (int i = 0; i < factors.getBound(); i++) {

            // Polynome zusammenfassen.
            if (SimplifyPolynomialMethods.isPolynomial(factors.get(i), var)) {
                if (numberOfPolynomials == 0) {
                    polynomialFactor = factors.get(i);
                } else {
                    polynomialFactor = polynomialFactor.mult(factors.get(i));
                }
                indexOfLastPolynomial = i;
                factors.remove(i);
                numberOfPolynomials++;
            }

        }

        if (numberOfPolynomials > 0) {
            /*
             Falls Polynome auftauchen, dann zusätzlich alle Polynome
             ausmultiplizieren und dann zu einem Polynom zusammenfassen.
             */
            simplifyTypesPrepareIntegrand.add(TypeSimplify.simplify_expand_moderate);
            polynomialFactor = polynomialFactor.simplify(simplifyTypesPrepareIntegrand);
            factors.put(indexOfLastPolynomial, polynomialFactor);
            simplifyTypesPrepareIntegrand.remove(TypeSimplify.simplify_expand_moderate);
        }

        return SimplifyUtilities.produceProduct(factors).simplify(simplifyTypesPrepareIntegrand);

    }

    /**
     * Hilfsmethode für prepareIntegrand(). Hier wird alles genauso vereinfacht,
     * wie in prepareIntegrand() für den Zähler, nur werden Polynome im Nenner
     * NICHT ausmultipliziert.
     *
     * @throws EvaluationException
     */
    public static Expression prepareDominatorOfIntegrand(Expression f, String var) throws EvaluationException {

        if (f.isQuotient()) {
            return prepareIntegrand(((BinaryOperation) f).getLeft(), var).div(prepareIntegrand(((BinaryOperation) f).getRight(), var));
        }

        /*
         Falls f ein Produkt ist und als Faktoren Polynome auftreten, so sollen
         diese ausmultipliziert werden. Dies ist dafür gedacht, damit z.B.
         (x^2+x+5)*((x-1)^2 - (x-2)^2) zu (x^2+x+5)*(2*x - 3) bzw.
         (x^2+x+5)*((x-1)^2 - (x-2)^2)^15 zu (x^2+x+5)*(2*x - 3)^15
         vereinfacht wird.
         */
        if (f.isProduct()) {
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            for (int i = 0; i < factors.getBound(); i++) {
                if (factors.get(i).isPower()) {
                    factors.put(i, ((BinaryOperation) factors.get(i)).getLeft().simplify(simplifyTypesPrepareDominatorOfIntegrand).pow(((BinaryOperation) factors.get(i)).getRight()));
                } else {
                    factors.put(i, factors.get(i).simplify(simplifyTypesPrepareDominatorOfIntegrand));
                }
            }
            f = SimplifyUtilities.produceProduct(factors);
        } else if (f.isPower()) {
            f = ((BinaryOperation) f).getLeft().simplify(simplifyTypesPrepareDominatorOfIntegrand).pow(((BinaryOperation) f).getRight());
        }

        // Zum Schluss: Nochmal vereinfachen, aber OHNE Ausmultiplizieren.
        simplifyTypesPrepareDominatorOfIntegrand.remove(TypeSimplify.simplify_expand_moderate);
        return f.simplify(simplifyTypesPrepareDominatorOfIntegrand);

    }

    /**
     * Hilfsmethode für die unbestimmte Integration. Multipliziert den
     * Integranden aus und ersetzt Funktionen durch "elementare" Funktionen
     * (beispielsweise tan(x) durch sin(x)/cos(x) etc.).
     *
     * @throws EvaluationException
     */
    public static Expression multiplyOutIntegrand(Expression f, String var) throws EvaluationException {

        ExpressionCollection factors = SimplifyUtilities.getFactors(f);

        /*
         Verschiedene Exponentialfunktionen nun zusammenfassen (etwa
         exp(a*x+b) * ... * exp(c*x+d) = exp((a+c)*x + (b+d))).
         */
        SimplifyExpLog.collectExponentialFunctionsInProduct(factors);

        // Nun den Rest integrationsspezifisch vereinfachen.
        if (f.containsTrigonometricalFunction()) {
            simplifyTypesMultiplyOutIntegrand.add(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions);
        }

        /*
         Wichtig zum Schluss: Exponentialfunktionen in var (d.h. Potenzfunktionen, 
         bei denen die Basis bzgl. var konstant ist) werden durch ihre ursprüngliche 
         Definition ersetzt. Dies ermöglicht es beispielsweise 2^x zu integrieren, 
         da dies zu exp(ln(2)*x) vereinfacht wird.
         */
        return SimplifyUtilities.produceProduct(factors).simplify(simplifyTypesMultiplyOutIntegrand).simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(var);

    }

    /**
     * Falls F(x) = integralFunction, x = var, eine korrekte Stammfunktion ist
     * (die sich gemäß Integrationsregeln ergibt!), dann wird F(upperLimit) -
     * F(lowerLimit) zurückgegeben. Summanden, die als nicht korrekt erkannt
     * werden (etwa welche, die sich nicht durch eine korrekte Integration
     * ergeben können), werden ignoriert (was bei einer korrekten Stammfunktion
     * nie passieren sollte)! Diese Methode wird in definiteIntegration
     * verwendet.
     *
     * @throws EvaluationException
     */
    public static Expression substituteBoundsInIntegral(Expression integralFunction, String var, Expression lowerLimit, Expression upperLimit) throws EvaluationException {

        if (integralFunction instanceof Constant) {
            return Expression.ZERO;
        }
        if (integralFunction instanceof Variable) {
            if (((Variable) integralFunction).getName().equals(var)) {
                return upperLimit.sub(lowerLimit);
            }
            return Expression.ZERO;
        }

        if (integralFunction instanceof BinaryOperation) {

            if (integralFunction.isSum()) {
                ExpressionCollection summands = SimplifyUtilities.getSummands(integralFunction);
                for (int i = 0; i < summands.getBound(); i++) {
                    summands.put(i, substituteBoundsInIntegral(summands.get(i), var, lowerLimit, upperLimit));
                }
                return SimplifyUtilities.produceSum(summands);
            }

            if (integralFunction.isDifference()) {
                return substituteBoundsInIntegral(((BinaryOperation) integralFunction).getLeft(), var, lowerLimit, upperLimit).sub(
                        substituteBoundsInIntegral(((BinaryOperation) integralFunction).getRight(), var, lowerLimit, upperLimit));
            }

            if (integralFunction.isProduct()) {

                ExpressionCollection factors = SimplifyUtilities.getFactors(integralFunction);
                boolean integralOccurs = false;
                boolean integralOccursTwice = false;
                boolean varOccurs = false;

                for (int i = 0; i < factors.getBound(); i++) {

                    if (factors.get(i).contains(var)) {
                        varOccurs = true;
                    }
                    if (factors.get(i) instanceof Operator && ((Operator) factors.get(i)).getType().equals(TypeOperator.integral)
                            && ((Operator) factors.get(i)).getParams().length == 2
                            && ((String) ((Operator) factors.get(i)).getParams()[1]).equals(var)) {

                        if (integralOccurs) {
                            integralOccursTwice = true;
                        }
                        Object[] paramsOfInnerDefiniteIntegral = new Object[4];
                        paramsOfInnerDefiniteIntegral[0] = ((Operator) factors.get(i)).getParams()[0];
                        paramsOfInnerDefiniteIntegral[1] = var;
                        paramsOfInnerDefiniteIntegral[2] = lowerLimit;
                        paramsOfInnerDefiniteIntegral[3] = upperLimit;
                        factors.put(i, new Operator(TypeOperator.integral, paramsOfInnerDefiniteIntegral));
                        integralOccurs = true;
                    }

                }

                if (varOccurs && !integralOccurs) {
                    return integralFunction.replaceVariable(var, upperLimit).sub(integralFunction.replaceVariable(var, lowerLimit));
                }
                if (!varOccurs && integralOccurs && !integralOccursTwice) {
                    return SimplifyUtilities.produceProduct(factors);
                }

                // Eigentlich ein Fehler, so etwas darf in einer korrekten Stammfunktion nicht vorkommen.
                return Expression.ZERO;

            }

            if (integralFunction.isQuotient()) {

                ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(integralFunction);
                ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(integralFunction);
                boolean integralOccursInEnumerator = false;
                boolean integralOccursTwiceOrInDenominator = false;
                boolean varOccurs = false;

                for (int i = 0; i < factorsEnumerator.getBound(); i++) {

                    if (factorsEnumerator.get(i).contains(var)) {
                        varOccurs = true;
                    }
                    if (factorsEnumerator.get(i) instanceof Operator && ((Operator) factorsEnumerator.get(i)).getType().equals(TypeOperator.integral)
                            && ((Operator) factorsEnumerator.get(i)).getParams().length == 2
                            && ((String) ((Operator) factorsEnumerator.get(i)).getParams()[1]).equals(var)) {

                        if (integralOccursInEnumerator) {
                            integralOccursTwiceOrInDenominator = true;
                        }
                        Object[] paramsOfInnerDefiniteIntegral = new Object[4];
                        paramsOfInnerDefiniteIntegral[0] = ((Operator) factorsEnumerator.get(i)).getParams()[0];
                        paramsOfInnerDefiniteIntegral[1] = var;
                        paramsOfInnerDefiniteIntegral[2] = lowerLimit;
                        paramsOfInnerDefiniteIntegral[3] = upperLimit;
                        factorsEnumerator.put(i, new Operator(TypeOperator.integral, paramsOfInnerDefiniteIntegral));
                        integralOccursInEnumerator = true;
                    }

                }
                for (int i = 0; i < factorsDenominator.getBound(); i++) {

                    if (factorsDenominator.get(i).contains(var)) {
                        varOccurs = true;
                    }
                    if (factorsDenominator.get(i) instanceof Operator && ((Operator) factorsDenominator.get(i)).getType().equals(TypeOperator.integral)
                            && ((Operator) factorsDenominator.get(i)).getParams().length == 2
                            && ((String) ((Operator) factorsDenominator.get(i)).getParams()[1]).equals(var)) {
                        integralOccursTwiceOrInDenominator = true;
                    }

                }

                if (varOccurs && !integralOccursInEnumerator) {
                    return integralFunction.replaceVariable(var, upperLimit).sub(integralFunction.replaceVariable(var, lowerLimit));
                }
                if (!varOccurs && integralOccursInEnumerator && !integralOccursTwiceOrInDenominator) {
                    return SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);
                }

                // Eigentlich ein Fehler, so etwas darf in einer korrekten Stammfunktion nicht vorkommen.
                return Expression.ZERO;

            }

            // Falls integralFunction eine Potenz ist, kann dort kein unbestimmtes Integral vorkommen.
            return integralFunction.replaceVariable(var, upperLimit).sub(integralFunction.replaceVariable(var, lowerLimit));

        }

        if (integralFunction instanceof Function) {
            // Falls integralFunction eine Funktion ist, kann dort kein unbestimmtes Integral vorkommen.
            return integralFunction.replaceVariable(var, upperLimit).sub(integralFunction.replaceVariable(var, lowerLimit));
        }

        if (integralFunction instanceof Operator) {
            if (((Operator) integralFunction).getType().equals(TypeOperator.integral)
                    && ((Operator) integralFunction).getParams().length == 2
                    && ((String) ((Operator) integralFunction).getParams()[1]).equals(var)) {

                Object[] paramsOfDefiniteIntegral = new Object[4];
                paramsOfDefiniteIntegral[0] = ((Operator) integralFunction).getParams()[0];
                paramsOfDefiniteIntegral[1] = var;
                paramsOfDefiniteIntegral[2] = lowerLimit;
                paramsOfDefiniteIntegral[3] = upperLimit;
                return new Operator(TypeOperator.integral, paramsOfDefiniteIntegral);

            }
            return integralFunction.replaceVariable(var, upperLimit).sub(integralFunction.replaceVariable(var, lowerLimit));
        }

        /*
         Selbstdefinierte Funktionen sollten nie auftreten, da sie im Vorfeld
         (durch simplify() etwa) durch vordefinierte Fuinktionen ersetzt
         werden. Falls dies doch auftreten SOLLTE, so wird das Ergebnis durch
         die folgenden beiden Zeilen berechnet.
         */
        Expression integralFunctionSimplified = ((SelfDefinedFunction) integralFunction).replaceAllVariables(((SelfDefinedFunction) integralFunction).getLeft());
        return integralFunctionSimplified.replaceVariable(var, upperLimit).sub(integralFunctionSimplified.replaceVariable(var, lowerLimit));

    }

    /**
     * Hauptmethode für bestimmte Integration.
     *
     * @throws EvaluationException
     */
    public static Expression integrateDefinite(Operator expr) throws EvaluationException {
        try {
            return definiteIntegration(expr);
        } catch (NotPreciseIntegrableException e) {
            return expr;
        }
    }

    /**
     * Interne Hauptmethode für bestimmte Integration. Ist expr ein bestimmtes
     * Integral expr == int(f, x, a, b), so wird das bestimmte Integral
     * zurückgegeben (soweit es geht, explizit). Ansonsten wird expr
     * zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression definiteIntegration(Operator expr)
            throws EvaluationException, NotPreciseIntegrableException {

        if (!expr.getType().equals(TypeOperator.integral) || expr.getParams().length != 4) {
            // Dann war der Operator expr kein bestimmtes Integral.
            return expr;
        }

        // Falls obere und untere Grenze übereinstimmen, dann soll 0 zurückgegeben werden.
        if (((Expression) expr.getParams()[2]).equivalent((Expression) expr.getParams()[3])) {
            return Expression.ZERO;
        }

        // Zunächst das zugehörige unbestimmte Integral bilden.
        Object[] paramsOfIndefiniteIntegral = new Object[2];
        paramsOfIndefiniteIntegral[0] = ((Expression) expr.getParams()[0]).simplify();
        paramsOfIndefiniteIntegral[1] = expr.getParams()[1];
        Operator indefiniteIntegral = new Operator(TypeOperator.integral, paramsOfIndefiniteIntegral);

        try {
            Expression resultOfIndefiniteIntegration = indefiniteIntegration(indefiniteIntegral, true);
            return substituteBoundsInIntegral((Expression) resultOfIndefiniteIntegration, (String) expr.getParams()[1],
                    (Expression) expr.getParams()[2], (Expression) expr.getParams()[3]);
        } catch (NotPreciseIntegrableException e) {
            return expr;
        }

    }

    /**
     * Hauptmethode für die unbestimmte Integration.
     *
     * @throws EvaluationException
     */
    public static Expression integrateIndefinite(Operator expr) throws EvaluationException {
        try {
            return indefiniteIntegration(expr, true);
        } catch (NotPreciseIntegrableException e) {
            return expr;
        }
    }

    /**
     * Interne Hauptmethode für die unbestimmte Integration. Hier wird entweder
     * ein Ausdruck (im Erfolgsfall oder falls expr kein unbestimmtes Integral
     * ist) oder false (falls der Ausdruck nicht exakt integriert werden kann)
     * zurückgegeben.
     *
     * @throws EvaluationException
     */
    protected static Expression indefiniteIntegration(Operator expr, boolean prepareIntegrand) throws EvaluationException, NotPreciseIntegrableException {

        if (!expr.getType().equals(TypeOperator.integral) || expr.getParams().length != 2) {
            // Dann war der Operator expr kein unbestimmtes Integral.
            return expr;
        }

        /*
         Integranden vereinfachen. Wichtig: hier muss nicht "allgemein"
         vereinfacht werden, sondern soweit, wie es für die Integration
         sinnvoll ist.
         */
        Object[] paramsOfSimplifiedIntegral = new Object[2];
        // Der Integrand wird sinnvoll/passend "vorbereitet" (vereinfacht).
        if (prepareIntegrand) {
            paramsOfSimplifiedIntegral[0] = prepareIntegrand((Expression) expr.getParams()[0], (String) expr.getParams()[1]);
            paramsOfSimplifiedIntegral[1] = expr.getParams()[1];
            expr = new Operator(TypeOperator.integral, paramsOfSimplifiedIntegral);
        }

        Expression result;

        // Summenregel
        try {
            return integrateSumsAndDifferences(expr);
        } catch (NotPreciseIntegrableException e) {
        }

        // Faktorregel
        try {
            return takeConstantFactorsOutOfIntegral(expr);
        } catch (NotPreciseIntegrableException e) {
        }

        // Ab hier folgt die Integration spezieller Typen.
        /*
         AHCTUNG: beim return muss vorher noch simplifyTrivial() angewendet
         werden. GRUND: dieses ruft simplifyTrivialInt() auf, falls in result
         noch irgendwo Integrale auftauchen (und diese weiter vereinfacht
         werden können).
         */
        // Integration von Monomen
        try {
            return integrateMonomial(expr).simplifyTrivial();
        } catch (NotPreciseIntegrableException e) {
        }

        // Integration von Elementarfunktionen
        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];
        if (f.isFunction() && ((Function) f).getLeft().equals(Variable.create(var))) {
            return integrateElementaryFunction(((Function) f).getType(), var);
        }

        // Integration von Potenzen von Elementarfunktionen
        try {
            return integratePowerOfElementaryFunction(expr).simplifyTrivial();
        } catch (NotPreciseIntegrableException e) {
        }

        // Integration logarithmischer Ableitungen.
        try {
            return integrateLogarithmicDerivative(expr).simplifyTrivial();
        } catch (NotPreciseIntegrableException e) {
        }

        // Integration elementarer Partialbrüche
        // Typ: (A*x + B) / (a*x^2 + b*x + c).
        try {
            return SpecialIntegrationMethods.integrateQuotientOfLinearPolynomialAndQuadraticPolynomial(expr).simplifyTrivial();
        } catch (NotPreciseIntegrableException e) {
        }

        // Typ: (A*x + B) / (a*x^2 + b*x + c)^k, k > 1.
        try {
            return SpecialIntegrationMethods.integrateQuotientOfLinearPolynomialAndPowerOfQuadraticPolynomial(expr).simplifyTrivial();
        } catch (NotPreciseIntegrableException e) {
        }

        // Partialbruchzerlegung
        try {
            return SpecialIntegrationMethods.integrateRationalFunction(expr).simplifyTrivial();
        } catch (NotPreciseIntegrableException e) {
        }

        // Integration von Polynomen in x, exp, sin und cos mit linearen Argumenten in var.
        try {
            return SpecialIntegrationMethods.integrateProductOfPolynomialAndPowerOfTrigonometricFunction(expr);
        } catch (NotPreciseIntegrableException e) {
        }

        try {
            return SpecialIntegrationMethods.integratePolynomialInComplexExponentialFunctions(expr);
        } catch (NotPreciseIntegrableException e) {
        }

        // Integration von P(x)*(a*x^2 + b*x + c)^((2*n + 1)/2), P = Polynom.
        try {
            return SpecialIntegrationMethods.integrateProductOfPolynomialAndOddPowerOfSqrtOfQuadraticPolynomial(expr);
        } catch (NotPreciseIntegrableException e) {
        }

        // Integration von P(x)/(a*x^2 + b*x + c)^((2*n + 1)/2).
        try {
            return SpecialIntegrationMethods.integrateQuotientOfPolynomialAndOddPowerOfSqrtOfQuadraticPolynomial(expr);
        } catch (NotPreciseIntegrableException e) {
        }

        // Integration von R(exp(a*x)), R(t) = rationale Funktion in t.
        try {
            return SpecialIntegrationMethods.integrateRationalFunctionInExp(expr).simplifyTrivial();
        } catch (NotPreciseIntegrableException e) {
        }

        // Integragtion von R(cos(a*x), sin(a*x)), R(t) = rationale Funktion in t.
        try {
            return SpecialIntegrationMethods.integrateRationalFunctionInTrigonometricFunctions(expr).simplifyTrivial();
        } catch (NotPreciseIntegrableException e) {
        }

        // ALLGEMEIN, falls bisher kein Ergebnis: Integration mittels Standardsubstitution.
        try {
            result = integrateByStandardSubstitution(expr);
            if (!result.containsIndefiniteIntegral()) {
                /*
                 Ergebnis nur DANN ausgeben, wenn darin keine weiteren Integrale
                 vorkommen. Die Methode simplifyMultiplyExponents() wird hier benötigt, damit
                 Exponenten STUR ausmultipliziert werden (ohne Beträge etc.),
                 falls diese in Substitutionen involviert sind.
                 */
                return result.simplifyMultiplyExponents().simplifyTrivial();
            }
        } catch (NotPreciseIntegrableException e) {
        }

        // GANZ ZUM SCHLUSS: Partielle Integration, falls erlaubt.
        if (allowPartialIntegration((Expression) expr.getParams()[0], (String) expr.getParams()[1])) {
            try {
                result = integrateByPartialIntegration(expr);
                if (!result.containsIndefiniteIntegral()) {
                    // Ergebnis nur DANN ausgeben, wenn darin keine weiteren Integrale vorkommen.
                    return (Expression) result;
                }
            } catch (NotPreciseIntegrableException e) {
            }
        }

        /*
         Falls nichts geholfen hat: Integranden ausmultiplizieren. Falls sich
         etwas geändert hat -> noch einmal versuchen zu integrieren.
         */
        Expression integrand = (Expression) expr.getParams()[0];
        Expression expandedIntegrand = multiplyOutIntegrand(integrand, var);
        if (!integrand.equals(expandedIntegrand)) {

            Object[] paramsOfExpandedIntegral = new Object[2];
            paramsOfExpandedIntegral[0] = expandedIntegrand;
            paramsOfExpandedIntegral[1] = expr.getParams()[1];
            return indefiniteIntegration(new Operator(TypeOperator.integral, paramsOfExpandedIntegral), false);

        }

        throw new NotPreciseIntegrableException();

    }

    /*
     Verschiedene Integrationensmethoden. Für die folgenden
     Integrationsmethoden gilt die generelle Voraussetzung: (1) expr ist ein
     Operator vom Typ TypeOperator.integral (dies wird auch nicht weiter
     abgefragt). (2) Der Integrand ist durch Anwendung von simplify()in der
     Hauptmethode indefiniteIntegration() bereits vereinfacht worden.
     */
    /**
     * Summenregel für Integrale. Gibt die Summe/Differenz der Integrale über
     * die Summanden des Integranden zurück.
     *
     * @throws EvaluationException
     */
    public static Expression integrateSumsAndDifferences(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(f);
            Object[][] paramsSummands;
            paramsSummands = new Object[summands.getBound()][2];
            for (int i = 0; i < summands.getBound(); i++) {
                paramsSummands[i][0] = summands.get(i);
                paramsSummands[i][1] = var;
            }
            ExpressionCollection resultSummands = new ExpressionCollection();
            for (int i = 0; i < summands.getBound(); i++) {
                resultSummands.add(new Operator(TypeOperator.integral, paramsSummands[i], expr.getPrecise()).simplifyTrivial());
            }
            return SimplifyUtilities.produceSum(resultSummands);

        }
        if (f.isDifference()) {

            Object[] paramsLeft = new Object[2];
            paramsLeft[0] = ((BinaryOperation) f).getLeft();
            paramsLeft[1] = var;
            Object[] paramsRight = new Object[2];
            paramsRight[0] = ((BinaryOperation) f).getRight();
            paramsRight[1] = var;
            return new Operator(TypeOperator.integral, paramsLeft, expr.getPrecise()).simplifyTrivial().sub(
                    new Operator(TypeOperator.integral, paramsRight, expr.getPrecise()).simplifyTrivial());

        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Faktorregel für Integrale. Gibt das Produkt aus der Konstanten, welche im
     * Integranden auftaucht, und dem Integral über den Restintegranden zurück.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression takeConstantFactorsOutOfIntegral(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            ExpressionCollection resultFactorsInIntegrand = new ExpressionCollection();
            ExpressionCollection resultFactorsOutsideOfIntegrand = new ExpressionCollection();

            for (int i = 0; i < factors.getBound(); i++) {
                if (factors.get(i).contains(var)) {
                    resultFactorsInIntegrand.add(factors.get(i));
                } else {
                    resultFactorsOutsideOfIntegrand.add(factors.get(i));
                }
            }

            if (!resultFactorsOutsideOfIntegrand.isEmpty()) {
                Object[] paramsResultIntegrand = new Object[2];
                paramsResultIntegrand[0] = SimplifyUtilities.produceProduct(resultFactorsInIntegrand);
                paramsResultIntegrand[1] = var;
                return SimplifyUtilities.produceProduct(resultFactorsOutsideOfIntegrand).mult(new Operator(TypeOperator.integral, paramsResultIntegrand, expr.getPrecise()).simplifyTrivial());
            }

        }
        if (f.isQuotient()) {

            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(f);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
            ExpressionCollection resultFactorsInIntegrandEnumerator = new ExpressionCollection();
            ExpressionCollection resultFactorsInIntegrandDenominator = new ExpressionCollection();
            ExpressionCollection resultFactorsInEnumeratorOutsideOfIntegrand = new ExpressionCollection();
            ExpressionCollection resultFactorsInDenominatorOutsideOfIntegrand = new ExpressionCollection();

            for (int i = 0; i < factorsEnumerator.getBound(); i++) {
                if (factorsEnumerator.get(i).contains(var)) {
                    resultFactorsInIntegrandEnumerator.add(factorsEnumerator.get(i));
                } else {
                    if (!factorsEnumerator.get(i).equals(Expression.ONE)) {
                        resultFactorsInEnumeratorOutsideOfIntegrand.add(factorsEnumerator.get(i));
                    }
                }
            }
            for (int i = 0; i < factorsDenominator.getBound(); i++) {
                if (factorsDenominator.get(i).contains(var)) {
                    resultFactorsInIntegrandDenominator.add(factorsDenominator.get(i));
                } else {
                    if (!factorsDenominator.get(i).equals(Expression.ONE)) {
                        resultFactorsInDenominatorOutsideOfIntegrand.add(factorsDenominator.get(i));
                    }
                }
            }

            Object[] paramsResultIntegrand = new Object[2];
            paramsResultIntegrand[0] = SimplifyUtilities.produceQuotient(resultFactorsInIntegrandEnumerator, resultFactorsInIntegrandDenominator);
            paramsResultIntegrand[1] = var;

            if (!resultFactorsInEnumeratorOutsideOfIntegrand.isEmpty() && resultFactorsInDenominatorOutsideOfIntegrand.isEmpty()) {
                return SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfIntegrand).mult(new Operator(TypeOperator.integral, paramsResultIntegrand, expr.getPrecise()).simplifyTrivial());
            } else if (resultFactorsInEnumeratorOutsideOfIntegrand.isEmpty() && !resultFactorsInDenominatorOutsideOfIntegrand.isEmpty()) {
                return new Operator(TypeOperator.integral, paramsResultIntegrand, expr.getPrecise()).simplifyTrivial().div(SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfIntegrand));
            } else if (!resultFactorsInEnumeratorOutsideOfIntegrand.isEmpty() && !resultFactorsInDenominatorOutsideOfIntegrand.isEmpty()) {
                return SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfIntegrand).mult(new Operator(TypeOperator.integral, paramsResultIntegrand, expr.getPrecise()).simplifyTrivial()).div(SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfIntegrand));
            }

        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Gibt die Stammfunktion zurück, falls der Integrand ein Monom ist.
     * Ansonsten wird false zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateMonomial(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        Expression derivative;
        try {
            derivative = f.diff(var).simplify();
        } catch (EvaluationException e) {
            throw new NotPreciseIntegrableException();
        }

        if (!f.contains(var)) {
            // Dann ist f konstant bezüglich var.
            return f.mult(Variable.create(var));
        }

        if (!derivative.contains(var) && !derivative.equals(Expression.ZERO)
                && f.isNotSum() && f.isNotDifference()) {
            // Dann ist f von der Form a*x, x = var, a unabhängig von x. Also F = a*x^2/2 zurückgeben.
            return derivative.mult(Variable.create(var).pow(2)).div(2);
        }

        if (f.isPower() && !((BinaryOperation) f).getRight().contains(var)) {
            Expression k = ((BinaryOperation) f).getRight();

            try {
                derivative = ((BinaryOperation) f).getLeft().diff(var).simplify();
            } catch (EvaluationException e) {
            }

            if (!derivative.contains(var)) {

                /*
                 Dann ist f von der Form (a*x+b)^k, x = var, k != -1 (da es
                 nach dem simplify() zum Kehrwert vereinfacht und dann weiter
                 unten behandelt wird).
                 */
                return ((BinaryOperation) f).getLeft().pow(k.add(1)).div((k.add(1)).mult(derivative));

            }
        }

        // Falls f = 1/(a*x+b).
        if (f.isQuotient() && ((BinaryOperation) f).getLeft().equals(Expression.ONE)) {

            try {
                derivative = ((BinaryOperation) f).getRight().diff(var).simplify();
            } catch (EvaluationException e) {
            }

            if (!derivative.contains(var)) {
                return new Function(new Function(((BinaryOperation) f).getRight(), TypeFunction.abs), TypeFunction.ln).div(derivative);
            }
        }

        // Falls f Kehrwert eines Monoms ist
        if (f.isQuotient() && ((BinaryOperation) f).getLeft().equals(Expression.ONE)
                && ((BinaryOperation) f).getRight().isPower()
                && !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().contains(var)) {
            Expression k = ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().simplify();

            try {
                derivative = ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft().diff(var).simplify();
            } catch (EvaluationException e) {
            }

            if (!derivative.contains(var)) {

                Expression baseOfMonomial = ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft();
                // Dann ist f von der Form 1/(a*x+b)^k, x = var, k != 1. Also F = (a*x+b)^(1-k)/((1-k)*a)
                return baseOfMonomial.pow(Expression.ONE.sub(k)).div(Expression.ONE.sub(k).mult(derivative));

            }
        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Tabelle mit Stammfunktionen von Elementarfunktionen. type gibt die
     * Funktion an und var die Variable im Argument. Ist beispielsweise type =
     * TypeFunction.cos und var = "x", so wird sin(x) zurückgegeben.
     */
    public static Expression integrateElementaryFunction(TypeFunction type, String var) {

        if (type.equals(TypeFunction.abs)) {
            //F = x*|x|/2
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.abs)).div(2);
        }
        if (type.equals(TypeFunction.arccos)) {
            //F = x*arccos(x) - (1 - x^2)^(1/2)
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.arccos)).sub(Expression.ONE.sub(Variable.create(var).pow(2)).pow(1, 2));
        }
        if (type.equals(TypeFunction.arccosec)) {
            //F = x*(arccosec(x) + arcosh(x)/|x|)
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.arccosec).add(
                    new Function(Variable.create(var), TypeFunction.arcosh).div(new Function(Variable.create(var), TypeFunction.abs))));
        }
        if (type.equals(TypeFunction.arccot)) {
            //F = x*arccot(x) + ln(1 + x^2)/2
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.arccot)).add(
                    new Function(Expression.ONE.add(Variable.create(var).pow(2)), TypeFunction.ln).div(2));
        }
        if (type.equals(TypeFunction.arcosech)) {
            //F = x*(arcosech(x) + arsinh(x)/|x|)
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.arcosech).add(
                    new Function(Variable.create(var), TypeFunction.arsinh).div(new Function(Variable.create(var), TypeFunction.abs))));
        }
        if (type.equals(TypeFunction.arcosh)) {
            //F = x*arcosh(x) - (x^2 - 1)^(1/2)
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.arccos)).sub(Variable.create(var).pow(2).sub(Expression.ONE).pow(1, 2));
        }
        if (type.equals(TypeFunction.arcoth)) {
            //F = x*arcoth(x) + ln(x^2 - 1)/2
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.arcoth)).add(
                    new Function(Variable.create(var).pow(2).sub(Expression.ONE), TypeFunction.ln).div(2));
        }
        if (type.equals(TypeFunction.arcsec)) {
            //F = x*(arcsec(x) - arcosh(x)/|x|)
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.arcsec).sub(
                    new Function(Variable.create(var), TypeFunction.arcosh).div(new Function(Variable.create(var), TypeFunction.abs))));
        }
        if (type.equals(TypeFunction.arcsin)) {
            //F = x*arcsin(x) + (1 - x^2)^(1/2)
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.arcsin)).add(
                    Expression.ONE.sub(Variable.create(var).pow(2)).pow(1, 2));
        }
        if (type.equals(TypeFunction.arctan)) {
            //F = x*arctan(x) - ln(1 + x^2)/2
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.arctan)).sub(
                    new Function(Expression.ONE.add(Variable.create(var).pow(2)), TypeFunction.ln).div(2));
        }
        if (type.equals(TypeFunction.arsech)) {
            //F = x*arsech(x) + 2*arcsin(((1 + x)/2)^(1/2))
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.arsech)).add(
                    Expression.TWO.mult(new Function(Expression.ONE.add(Variable.create(var)).div(2).pow(1, 2), TypeFunction.arcsin)));
        }
        if (type.equals(TypeFunction.arsinh)) {
            //F = x*arsinh(x) - (1 + x^2)^(1/2)
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.arsinh)).sub(
                    Expression.ONE.add(Variable.create(var).pow(2)).pow(1, 2));
        }
        if (type.equals(TypeFunction.artanh)) {
            //F = x*arctanh(x) + ln(1 - x^2)/2
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.arctan)).add(
                    new Function(Expression.ONE.sub(Variable.create(var).pow(2)), TypeFunction.ln).div(2));
        }
        if (type.equals(TypeFunction.cos)) {
            //F = sin(x)
            return new Function(Variable.create(var), TypeFunction.sin);
        }
        if (type.equals(TypeFunction.cosec)) {
            //F = ln(|tan(x/2)|)
            return new Function(new Function(new Function(Variable.create(var).div(2), TypeFunction.tan), TypeFunction.abs), TypeFunction.ln);
        }
        if (type.equals(TypeFunction.cosech)) {
            //F = ln(|tanh(x/2)|)
            return new Function(new Function(new Function(Variable.create(var).div(2), TypeFunction.tanh), TypeFunction.abs), TypeFunction.ln);
        }
        if (type.equals(TypeFunction.cosh)) {
            //F = sinh(x)
            return new Function(Variable.create(var), TypeFunction.sinh);
        }
        if (type.equals(TypeFunction.cot)) {
            //F = ln(|sin(x)|)
            return new Function(new Function(new Function(Variable.create(var), TypeFunction.sin), TypeFunction.abs), TypeFunction.ln);
        }
        if (type.equals(TypeFunction.coth)) {
            //F = ln(|sinh(x)|)
            return new Function(new Function(new Function(Variable.create(var), TypeFunction.sinh), TypeFunction.abs), TypeFunction.ln);
        }
        if (type.equals(TypeFunction.exp)) {
            //F = exp(x)
            return new Function(Variable.create(var), TypeFunction.exp);
        }
        if (type.equals(TypeFunction.id)) {
            //F = x^2/2
            return Variable.create(var).pow(2).div(2);
        }
        if (type.equals(TypeFunction.lg)) {
            //F = (x*ln(x) - x)/ln(10)
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.ln)).sub(Variable.create(var)).div(new Function(new Constant(10), TypeFunction.ln));
        }
        if (type.equals(TypeFunction.ln)) {
            //F = x*ln(x) - x
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.ln)).sub(Variable.create(var));
        }
        if (type.equals(TypeFunction.sec)) {
            //F = ln(|(1 + tan(x/2))/(1 - tan(x/2))|)
            return new Function(new Function(Expression.ONE.add(new Function(Variable.create(var).div(2), TypeFunction.tan)).div(
                    Expression.ONE.sub(new Function(Variable.create(var).div(2), TypeFunction.tan))), TypeFunction.abs), TypeFunction.ln);
        }
        if (type.equals(TypeFunction.sech)) {
            //F = 2*arctan(tanh(x/2))
            return Expression.TWO.mult(new Function(new Function(Variable.create(var).div(2), TypeFunction.tanh), TypeFunction.arctan));
        }
        if (type.equals(TypeFunction.sgn)) {
            //F = |x|
            return new Function(Variable.create(var), TypeFunction.abs);
        }
        if (type.equals(TypeFunction.sin)) {
            //F = -cos(x)
            return Expression.MINUS_ONE.mult(new Function(Variable.create(var), TypeFunction.cos));
        }
        if (type.equals(TypeFunction.sinh)) {
            //F = cosh(x)
            return new Function(Variable.create(var), TypeFunction.cosh);
        }
        if (type.equals(TypeFunction.sqrt)) {
            //F = 2*x^(3/2)/3
            return Expression.TWO.mult(Variable.create(var).pow(3, 2)).div(3);
        }
        if (type.equals(TypeFunction.tan)) {
            //F = -ln(|cos(x)|)
            return Expression.MINUS_ONE.mult(new Function(new Function(new Function(Variable.create(var), TypeFunction.cos), TypeFunction.abs), TypeFunction.ln));
        } else {
            // Dann ist type == TypeFunction.tanh. Also ist F = ln(cosh(x)).
            return new Function(new Function(Variable.create(var), TypeFunction.cosh), TypeFunction.ln);
        }

    }

    /**
     * Tabelle mit Stammfunktionen von Potenzen von Elementarfunktionen. expr
     * ist ein unbestimmtes Integral, welches eine positive ganzzahlige Potenz
     * einer Elementarfunktion als Integranden besitzt. maxIntegrablePower gibt
     * die maximale Potenz an, bei der noch explizit integriert werden soll. Ist
     * beispielsweise expr = int(sin(x)^3,x) und maxIntegrablePower = 100, so
     * wird (cos(x)*(-2-sin(x)^2))/3 zurückgegeben. Ist dagegen und
     * maxIntegrablePower = 2, so wird false zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePowerOfElementaryFunction(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotPower()
                || !((BinaryOperation) f).getRight().isIntegerConstant()
                || ((BinaryOperation) f).getRight().isIntegerConstantOrRationalConstantNegative()
                || !(((BinaryOperation) f).getLeft() instanceof Function)
                || !((Function) ((BinaryOperation) f).getLeft()).getLeft().equals(Variable.create(var))) {
            throw new NotPreciseIntegrableException();
        }

        BigInteger exponent = ((Constant) ((BinaryOperation) f).getRight()).getValue().toBigInteger();
        TypeFunction type = ((Function) ((BinaryOperation) f).getLeft()).getType();

        if (exponent.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_INTEGRABLE_POWER)) > 0) {
            throw new NotPreciseIntegrableException();
        }

        if (type.equals(TypeFunction.ln)) {
            return integratePowerOfLn(exponent.intValue(), var);
        }
        if (type.equals(TypeFunction.tan)) {
            return integratePowerOfTan(exponent.intValue(), var);
        }
        if (type.equals(TypeFunction.cot)) {
            return integratePowerOfCot(exponent.intValue(), var);
        }
        if (type.equals(TypeFunction.sec)) {
            return integratePowerOfSec(exponent.intValue(), var);
        }
        if (type.equals(TypeFunction.cosec)) {
            return integratePowerOfCosec(exponent.intValue(), var);
        }
        if (type.equals(TypeFunction.sinh)) {
            return integratePowerOfSinh(exponent.intValue(), var);
        }
        if (type.equals(TypeFunction.cosh)) {
            return integratePowerOfCosh(exponent.intValue(), var);
        }
        if (type.equals(TypeFunction.tanh)) {
            return integratePowerOfTanh(exponent.intValue(), var);
        }
        if (type.equals(TypeFunction.coth)) {
            return integratePowerOfCoth(exponent.intValue(), var);
        }
        if (type.equals(TypeFunction.sech)) {
            return integratePowerOfSech(exponent.intValue(), var);
        }
        if (type.equals(TypeFunction.cosech)) {
            return integratePowerOfCosech(exponent.intValue(), var);
        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Rekursionsformel: int(ln(x)^n, x) = x*ln(x)^n - n*int(ln(x)^(n - 1), x)
     *
     * @throws EvaluationException
     * @throws NotPreciseIntegrableException
     */
    public static Expression integratePowerOfLn(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.ln).sub(1));
        }
        Object[] params = new Object[2];
        params[0] = new Function(Variable.create(var), TypeFunction.ln).pow(n - 1);
        params[1] = var;
        Object integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
        return Variable.create(var).mult(new Function(Variable.create(var), TypeFunction.ln).pow(n)).sub(new Constant(n).mult((Expression) integralOfLowerPower));

    }

    /**
     * Rekursionsformel: int(sin(x)^n, x) = -(cos(x)*sin(x)^(n - 1))/n + (n -
     * 1)/n*int(sin(x)^(n - 2), x)
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePowerOfSin(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            return Expression.MINUS_ONE.mult(new Function(Variable.create(var), TypeFunction.cos));
        } else if (n == 2) {
            return Variable.create(var).div(2).sub((new Function(new Constant(2).mult(Variable.create(var)), TypeFunction.sin)).div(4));
        }
        Object[] params = new Object[2];
        params[0] = new Function(Variable.create(var), TypeFunction.sin).pow(n - 2);
        params[1] = var;
        Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
        return new Constant(n - 1).mult(integralOfLowerPower).div(n).sub(
                Variable.create(var).cos().mult(Variable.create(var).sin().pow(n - 1)).div(n));

    }

    /**
     * Rekursionsformel: int(cos(x)^n, x) = (sin(x)*cos(x)^(n - 1))/n + (n -
     * 1)/n*int(cos(x)^(n - 2), x)
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Object integratePowerOfCos(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            return new Function(Variable.create(var), TypeFunction.sin);
        } else if (n == 2) {
            return Variable.create(var).div(2).add((new Function(new Constant(2).mult(Variable.create(var)), TypeFunction.sin)).div(4));
        }
        Object[] params = new Object[2];
        params[0] = new Function(Variable.create(var), TypeFunction.cos).pow(n - 2);
        params[1] = var;
        Object integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
        if (integralOfLowerPower instanceof Expression) {
            return new Constant(n - 1).mult((Expression) integralOfLowerPower).div(n).add(
                    new Function(Variable.create(var), TypeFunction.sin).mult(new Function(Variable.create(var), TypeFunction.cos).pow(n - 1)).div(n));
        }
        throw new NotPreciseIntegrableException();

    }

    /**
     * Rekursionsformel: int(tan(x)^n, x) = tan(x)^(n - 1)/(n - 1) -
     * int(tan(x)^(n - 2), x)
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePowerOfTan(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            return Expression.MINUS_ONE.mult(Variable.create(var).cos().abs().ln());
        } else if (n == 2) {
            return new Function(Variable.create(var), TypeFunction.tan).sub(Variable.create(var));
        }
        Object[] params = new Object[2];
        params[0] = Variable.create(var).tan().pow(n - 2);
        params[1] = var;
        Object integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
        if (integralOfLowerPower instanceof Expression) {
            return new Function(Variable.create(var), TypeFunction.tan).pow(n - 1).div(n - 1).sub((Expression) integralOfLowerPower);
        }
        throw new NotPreciseIntegrableException();

    }

    /**
     * Rekursionsformel: int(cot(x)^n, x) = -cot(x)^(n - 1)/(n - 1) -
     * int(cot(x)^(n - 2), x)
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePowerOfCot(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            return new Function(new Function(new Function(Variable.create(var), TypeFunction.sin), TypeFunction.abs), TypeFunction.ln);
        } else if (n == 2) {
            return Expression.MINUS_ONE.mult(Variable.create(var)).sub(new Function(Variable.create(var), TypeFunction.cot));
        }
        Object[] params = new Object[2];
        params[0] = new Function(Variable.create(var), TypeFunction.cot).pow(n - 2);
        params[1] = var;
        Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
        return Expression.MINUS_ONE.mult(new Function(Variable.create(var), TypeFunction.cot).pow(n - 1)).div(n - 1).sub(integralOfLowerPower);

    }

    /**
     * Rekursionsformel: int(sec(x)^n, x) = sec(x)^(n - 1)*sin(x)/(n - 1) + (n -
     * 2)*int(sec(x)^(n - 2), x)/(n - 1)
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePowerOfSec(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            //F = ln(|(1 + tan(x/2))/(1 - tan(x/2))|)
            return new Function(new Function(Expression.ONE.add(new Function(Variable.create(var).div(2), TypeFunction.tan)).div(
                    Expression.ONE.sub(new Function(Variable.create(var).div(2), TypeFunction.tan))), TypeFunction.abs), TypeFunction.ln);
        } else if (n == 2) {
            //F = tan(x)
            return Variable.create(var).tan();
        }
        Object[] params = new Object[2];
        params[0] = Variable.create(var).sec().pow(n - 2);
        params[1] = var;
        Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
        return Variable.create(var).sec().pow(n - 1).mult(Variable.create(var).sin()).div(n - 1).add(new Constant(n - 2).mult(integralOfLowerPower).div(n - 1));

    }

    /**
     * Rekursionsformel: int(cosec(x)^n, x) = -cosec(x)^(n - 1)*cos(x)/(n - 1) +
     * (n - 2)*int(cosec(x)^(n - 2), x)/(n - 1)
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePowerOfCosec(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            //F = ln(|tan(x/2)|)
            return Variable.create(var).div(2).tan().abs().ln();
        } else if (n == 2) {
            //F = -cot(x)
            return Expression.MINUS_ONE.mult(Variable.create(var).cot());
        }
        Object[] params = new Object[2];
        params[0] = Variable.create(var).cosec().pow(n - 2);
        params[1] = var;
        Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
        return Expression.MINUS_ONE.mult(Variable.create(var).cosec().pow(n - 1).mult(Variable.create(var).cos())).div(n - 1).add(
                new Constant(n - 2).mult(integralOfLowerPower).div(n - 1));

    }

    /**
     * Rekursionsformel: int(sinh(x)^n, x) = (cosh(x)*sinh(x)^(n - 1))/n - (n -
     * 1)/n*int(sinh(x)^(n - 2), x)
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePowerOfSinh(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            return Variable.create(var).cosh();
        } else if (n == 2) {
            return new Constant(2).mult(Variable.create(var)).sinh().div(4).sub(Variable.create(var).div(2));
        }
        Object[] params = new Object[2];
        params[0] = Variable.create(var).sinh().pow(n - 2);
        params[1] = var;
        Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
        return Variable.create(var).cosh().mult(Variable.create(var).sinh().pow(n - 1)).div(n).sub(
                new Constant(n - 1).mult(integralOfLowerPower).div(n));

    }

    /**
     * Rekursionsformel: int(cosh(x)^n, x) = (sinh(x)*cosh(x)^(n - 1))/n + (n -
     * 1)/n*int(cosh(x)^(n - 2), x)
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePowerOfCosh(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            //F = sinh(x)
            return Variable.create(var).sinh();
        } else if (n == 2) {
            //F = x/2 + sinh(2*x)/4
            return Variable.create(var).div(2).add((TWO.mult(Variable.create(var)).sinh()).div(4));
        } else {
            Object[] params = new Object[2];
            params[0] = Variable.create(var).cosh().pow(n - 2);
            params[1] = var;
            Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
            return new Constant(n - 1).mult(integralOfLowerPower).div(n).add(
                    Variable.create(var).sinh().mult(Variable.create(var).cosh().pow(n - 1)).div(n));
        }

    }

    /**
     * Rekursionsformel: int(tanh(x)^n, x) = -tanh(x)^(n - 1)/(n - 1) +
     * int(tanh(x)^(n - 2), x)
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePowerOfTanh(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            return Variable.create(var).cosh().ln();
        } else if (n == 2) {
            return Variable.create(var).sub(Variable.create(var).tanh());
        }
        Object[] params = new Object[2];
        params[0] = Variable.create(var).tanh().pow(n - 2);
        params[1] = var;
        Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
        return integralOfLowerPower.sub(Variable.create(var).tanh().pow(n - 1).div(n - 1));

    }

    /**
     * Rekursionsformel: int(coth(x)^n, x) = -coth(x)^(n - 1)/(n - 1) +
     * int(coth(x)^(n - 2), x)
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePowerOfCoth(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            //F = ln(|sinh(x)|)
            return Variable.create(var).sinh().abs().ln();
        } else if (n == 2) {
            //F = x - coth(x)
            return Variable.create(var).sub(Variable.create(var).coth());
        }
        Object[] params = new Object[2];
        params[0] = Variable.create(var).coth().pow(n - 2);
        params[1] = var;
        Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
        return Expression.MINUS_ONE.mult(Variable.create(var).coth().pow(n - 1)).div(n - 1).add(integralOfLowerPower);

    }

    /**
     * Rekursionsformel: int(sech(x)^n, x) = sech(x)^(n - 1)*sinh(x)/(n - 1) +
     * (n - 2)*int(sech(x)^(n - 2), x)/(n - 1)
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePowerOfSech(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            //F = 2*arctan(tanh(x/2))
            return TWO.mult(Variable.create(var).div(2).tanh().arctan());
        } else if (n == 2) {
            //F = tanh(x)
            return Variable.create(var).tanh();
        } else {
            Object[] params = new Object[2];
            params[0] = Variable.create(var).sech().pow(n - 2);
            params[1] = var;
            Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
            return Variable.create(var).sech().pow(n - 1).mult(Variable.create(var).sinh()).div(n - 1).add(
                    new Constant(n - 2).mult(integralOfLowerPower).div(n - 1));
        }

    }

    /**
     * Rekursionsformel: int(cosech(x)^n, x) = -cosech(x)^(n - 1)*cosh(x)/(n -
     * 1) - (n - 2)*int(cosech(x)^(n - 2), x)/(n - 1)
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePowerOfCosech(int n, String var) throws EvaluationException, NotPreciseIntegrableException {

        if (n == 1) {
            //F = ln(|tanh(x/2)|)
            return Variable.create(var).div(2).tanh().abs().ln();
        } else if (n == 2) {
            //F = -coth(x)
            return Expression.MINUS_ONE.mult(Variable.create(var).coth());
        }
        Object[] params = new Object[2];
        params[0] = Variable.create(var).cosech().pow(n - 2);
        params[1] = var;
        Expression integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);
        return Expression.MINUS_ONE.mult(Variable.create(var).cosech().pow(n - 1).mult(Variable.create(var).cosh())).div(n - 1).sub(
                new Constant(n - 2).mult(integralOfLowerPower).div(n - 1));

    }

    /**
     * Gibt die Stammfunktion von a^x zurück, falls expr ein unbestimmtes
     * Integral ist und der Integrand die Form a^x besitzt. Ansonsten wird false
     * zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateGeneralExponentialFunction(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotPower() || ((BinaryOperation) f).getLeft().contains(var)) {
            throw new NotPreciseIntegrableException();
        }

        Expression derivativeOfExponent = ((BinaryOperation) f).getRight().diff(var).simplify();

        if (derivativeOfExponent.contains(var) || derivativeOfExponent.equals(Expression.ZERO)) {
            throw new NotPreciseIntegrableException();
        }

        // Ab hier ist der Exponent linear in var.
        return f.div(derivativeOfExponent.mult(new Function(((BinaryOperation) f).getLeft(), TypeFunction.ln)));

    }

    /**
     * Gibt die Stammfunktion von c * f'/f zurück, falls expr ein unbestimmtes
     * Integral ist und der Integrand die Form c * f'/f mit einer Konstante (von
     * var unabhängigen Funktion) besitzt. Ansonsten wird false
     * zurückgegeben.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateLogarithmicDerivative(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient() || !((BinaryOperation) f).getRight().contains(var)) {
            throw new NotPreciseIntegrableException();
        }

        Expression logArgument = ((BinaryOperation) f).getRight();
        Expression derivative = logArgument.diff(var).simplify();
        Expression quotient = ((BinaryOperation) f).getLeft().div(derivative).simplify();

        if (!quotient.contains(var)) {
            // Dann war f vom folgenden Typ: f = c*g/g'. Also F = c*ln(|g|) mit c = quotient.
            return quotient.mult(logArgument.abs().ln());
        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Hilfsmethode für integrateByStandardSubstitution(). Ermittelt für einen
     * Ausdruck factor alle möglichen potentiellen (sinnvollen) Substitutionen
     * und fügt diese in die HashMap setOfSubstitutions ein. Die Elemente in der
     * HashMap werden via 0, 1, 2, ..., size() - 1 durchnummeriert.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection getSuitableSubstitutionForIntegration(Expression factor, String var, boolean beginning)
            throws EvaluationException {

        ExpressionCollection setOfSubstitutions = new ExpressionCollection();

        if (factor.isPower()) {

            /*
             Es wird Folgendes als potentielle Substitution angesehen: (1)
             Nichttriviale Exponenten, wobei die Basis bzgl. var konstant ist
             (2) Nichttriviale Basen, wobei der Exponent bzgl. var konstant
             ist (3) Weitere Substitutionen, welche IN DER BASIS gefunden
             werden, falls diese NICHT die Variable var selbst ist.
             */
            if (((BinaryOperation) factor).getRight().contains(var) && !((BinaryOperation) factor).getLeft().contains(var)
                    && !(((BinaryOperation) factor).getRight() instanceof Variable)) {
                setOfSubstitutions.add(((BinaryOperation) factor).getRight());
            } else if (((BinaryOperation) factor).getLeft().contains(var) && !((BinaryOperation) factor).getRight().contains(var)
                    && !(((BinaryOperation) factor).getLeft() instanceof Variable)) {
                setOfSubstitutions.add(((BinaryOperation) factor).getLeft());
                ExpressionCollection substitutionsInsideOfBase = getSuitableSubstitutionForIntegration(((BinaryOperation) factor).getLeft(), var, false);
                for (int i = 0; i < substitutionsInsideOfBase.getBound(); i++) {
                    setOfSubstitutions.add(substitutionsInsideOfBase.get(i));
                }
            } else if (factor.contains(var) && !(((BinaryOperation) factor).getLeft().equals(Variable.create(var))
                    && (((BinaryOperation) factor).getRight().equals(Expression.ONE))
                    || (((BinaryOperation) factor).getRight().equals(Expression.ZERO)))
                    && !beginning) {
                /*
                 Falls factor eine Potenz ist, so soll die gesamte Potenz als
                 potentielle Substitution betrachtet werden (FALLS es bereits
                 ein Argument einer umschließenden Funktion ist), außer es
                 handelt sich um var^1 oder var^0 (dies sollte aber wegen
                 vorheriger Anwendung von simplifyTrivial() ausgeschlossen
                 sein; sicherheitshalber bleibt es drin, damit es keine
                 Endlosschleifen gibt).
                 */
                setOfSubstitutions.add(factor);
            }

        } else if (factor.isFunction()) {

            /*
             Als potentielle Substitution kommt die Funktion selbst in Frage,
             sowie das Argument dieser Funktion, falls es NICHT die Variable
             selbst ist.
             */
            if (((Function) factor).getLeft().contains(var)) {
                setOfSubstitutions.add(factor);
                /**
                 * Weitere potentielle Substitutionen finden sich möglicherweise
                 * im Argument der Funktion.
                 */
                ExpressionCollection substitutionsInFunctionArgument = getSuitableSubstitutionForIntegration(((Function) factor).getLeft(), var, false);

                if (substitutionsInFunctionArgument.isEmpty() && !(((Function) factor).getLeft() instanceof Variable)) {
                    /*
                     Falls Im Funktionsargument keine weiteren potenziellen
                     Substitutionen gefunden werden konnten, dann soll das
                     Argument selbst als potenzielle Substitution genommen
                     werden, falls es KEINE Variable ist (denn dann wäre es
                     eine triviale Substitution).
                     */
                    setOfSubstitutions.add(((Function) factor).getLeft());
                } else {
                    for (int i = 0; i < substitutionsInFunctionArgument.getBound(); i++) {
                        setOfSubstitutions.add(substitutionsInFunctionArgument.get(i));
                    }
                }

            }

        }

        return setOfSubstitutions;

    }

    /**
     * Methode, die versucht, mittels Substitution zu integrieren, wobei die
     * potentiellen Substitution für jeden Faktor Faktor im Zähler oder Nenner
     * des Integranden mittels findSuitableSubstitutionForIntegration() gefunden
     * werden. Gibt im Erfolgsfall die Stammfunktion des Integranden zurück,
     * ansonsten false.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateByStandardSubstitution(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];
        Expression factor, quotient, substitution, derivative;

        ExpressionCollection setOfSubstitutions = new ExpressionCollection();

        if (f.isNotQuotient()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(f);

            for (int i = 0; i < factors.getBound(); i++) {

                setOfSubstitutions.clear();
                setOfSubstitutions = getSuitableSubstitutionForIntegration(factors.get(i), var, true);
                factor = factors.get(i);
                factors.remove(i);

                // Jede potentielle Substitution ausprobieren.
                for (int j = 0; j < setOfSubstitutions.getBound(); j++) {

                    substitution = setOfSubstitutions.get(j);
                    derivative = substitution.diff(var).simplify();
                    quotient = SimplifyUtilities.produceProduct(factors).div(derivative).simplify();

                    /*
                     Sei g(x) der Integrand, x = var und subst = f(x). Es wird
                     versucht, im Faktor factor und im Rest des Integranden,
                     also in g(x)/(factor * f'(x)) zu substituieren (gemäß der
                     Substitutionsregel).
                     */
                    String substVar = SubstitutionUtilities.getSubstitutionVariable(f);

                    try {
                        Expression substitutedRestIntegrand = SubstitutionUtilities.substitute(quotient, var, substitution);
                        Expression substitutedFactor = SubstitutionUtilities.substitute(factor, var, substitution);
                        // Kommt man hier an, war die Substitution erfolgreich.
                        Expression fSubstituted = substitutedRestIntegrand.mult(substitutedFactor).simplify();
                        Object[] paramsSubstitutedIntegral;
                        paramsSubstitutedIntegral = new Object[2];
                        paramsSubstitutedIntegral[0] = fSubstituted;
                        paramsSubstitutedIntegral[1] = substVar;
                        Operator substitutedIntegral = new Operator(TypeOperator.integral, paramsSubstitutedIntegral, expr.getPrecise());

                        // Nun wird versucht, das substituierte Integral zu berechnen.
                        Expression F = indefiniteIntegration(substitutedIntegral, true);
                        /*
                         Kommt man hier an, war die Integration des substituierten
                         Integrals erfolgreich. Dann zurücksubstitutieren und
                         Stammfunktion ausgeben.
                         */
                        return F.replaceVariable(substVar, substitution);
                    } catch (NotSubstitutableException | NotPreciseIntegrableException e) {
                    }

                }

                /*
                 Falls keine Stammfunktion ermittelt werden konnte -> Faktor
                 factor wieder zu Faktoren von f hinzufügen und weiter
                 versuchen.
                 */
                factors.put(i, factor);

            }

        } else {

            /*
             Dasselbe wie oben, nur hier wird sowohl im Zähler als auch im
             Nenner von f nach potenziellen Substitutionen gesucht.
             */
            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(f);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);

            // Im Zähler suchen.
            for (int i = 0; i < factorsEnumerator.getBound(); i++) {

                setOfSubstitutions.clear();
                setOfSubstitutions = getSuitableSubstitutionForIntegration(factorsEnumerator.get(i), var, true);
                factor = factorsEnumerator.get(i);
                factorsEnumerator.remove(i);

                // Jede potentielle Substitution ausprobieren.
                for (int j = 0; j < setOfSubstitutions.getBound(); j++) {

                    substitution = setOfSubstitutions.get(j);
                    derivative = substitution.diff(var).simplify();
                    quotient = SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator).div(derivative).simplify();

                    /*
                     Sei g(x) der Integrand, x = var und subst = f(x). Es wird
                     versucht, im Faktor factor und im Rest des Integranden,
                     also in g(x)/(factor * f'(x)) zu substituieren (gemäß der
                     Substitutionsregel).
                     */
                    String substVar = SubstitutionUtilities.getSubstitutionVariable(f);

                    try {
                        Expression substitutedRestIntegrand = SubstitutionUtilities.substitute(quotient, var, substitution);
                        Expression substitutedFactor = SubstitutionUtilities.substitute(factor, var, substitution);

                        // Kommt man hier an, war die Substitution erfolgreich.
                        Expression fSubstituted = substitutedRestIntegrand.mult(substitutedFactor).simplify();

                        Object[] paramsSubstitutedIntegral;
                        paramsSubstitutedIntegral = new Object[2];
                        paramsSubstitutedIntegral[0] = fSubstituted;
                        paramsSubstitutedIntegral[1] = substVar;
                        Operator substitutedIntegral = new Operator(TypeOperator.integral, paramsSubstitutedIntegral, expr.getPrecise());

                        // Nun wird versucht, das substituierte Integral zu berechnen.
                        Expression F = indefiniteIntegration(substitutedIntegral, true);
                        /*
                         Kommt man hier an, war die Integration des substituierten
                         Integrals erfolgreich. Dann zurücksubstitutieren und
                         Stammfunktion ausgeben.
                         */
                        return F.replaceVariable(substVar, substitution);
                    } catch (NotSubstitutableException | NotPreciseIntegrableException e) {
                    }

                }

                /*
                 Falls keine Stammfunktion ermittelt werden konnte -> Faktor
                 factor wieder zu Faktoren von f hinzufügen und weiter
                 versuchen.
                 */
                factorsEnumerator.put(i, factor);

            }

            // Im Nenner suchen.
            for (int i = 0; i < factorsDenominator.getBound(); i++) {

                setOfSubstitutions.clear();
                setOfSubstitutions = getSuitableSubstitutionForIntegration(factorsDenominator.get(i), var, true);
                factor = factorsDenominator.get(i);
                factorsDenominator.remove(i);

                // Jede potentielle Substitution ausprobieren.
                for (int j = 0; j < setOfSubstitutions.getBound(); j++) {

                    substitution = setOfSubstitutions.get(j);
                    derivative = substitution.diff(var).simplify();
                    quotient = SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator).div(derivative).simplify();

                    /*
                     Sei g(x) der Integrand, x = var und subst = f(x). Es wird
                     versucht, im Faktor factor und im Rest des Integranden,
                     also in g(x)/(factor * f'(x)) zu substituieren (gemäß der
                     Substitutionsregel).
                     */
                    String substVar = SubstitutionUtilities.getSubstitutionVariable(f);

                    try {
                        Expression substitutedRestIntegrand = SubstitutionUtilities.substitute(quotient, var, substitution);
                        Expression substitutedFactor = SubstitutionUtilities.substitute(factor, var, substitution);

                        // Kommt man hier an, war die Substitution erfolgreich.
                        Expression fSubstituted = substitutedRestIntegrand.div(substitutedFactor).simplify();

                        Object[] paramsSubstitutedIntegral;
                        paramsSubstitutedIntegral = new Object[2];
                        paramsSubstitutedIntegral[0] = fSubstituted;
                        paramsSubstitutedIntegral[1] = substVar;
                        Operator substitutedIntegral = new Operator(TypeOperator.integral, paramsSubstitutedIntegral, expr.getPrecise());

                        // Nun wird versucht, das substituierte Integral zu berechnen.
                        Expression F = indefiniteIntegration(substitutedIntegral, true);
                        /*
                         Kommt man hier an, war die Integration des substituierten
                         Integrals erfolgreich. Dann zurücksubstitutieren und
                         Stammfunktion ausgeben.
                         */
                        return F.replaceVariable(substVar, substitution);
                    } catch (NotSubstitutableException | NotPreciseIntegrableException e) {
                    }

                }

                /*
                 Falls keine Stammfunktion ermittelt werden konnte -> Faktor
                 factor wieder zu Faktoren von f hinzufügen und weiter
                 versuchen.
                 */
                factorsDenominator.put(i, factor);

            }

        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Hilfsmethode für partielle Integration. Versucht, bei der partiellen
     * Integration int(u'*v) = u*v - int(u*v') eine clevere Wahl für v zu
     * treffen. Die HashMap factors enthält dabei alle Faktoren des Integranden
     * u'v. Falls eine Zahl zurückgegeben wird, so ist dies der index in
     * factors, der v entspricht. Das Produkt über alle übrigen Faktoren ist
     * dann eine gute Wahl für u'. Falls keine clevere Wahl für v gefunden
     * wurde, so wird false zurückgegeben.
     */
    public static Object cleverChoiceForPartialIntegration(ExpressionCollection factors, String var) {

        boolean factorsContainPolynomial = false;
        int indexOfPolynomial = -1;
        boolean factorsContainLogarithm = false;
        int indexOfLogarithm = -1;
        boolean factorsContainArctanOrArtanh = false;
        int indexOfArctanOrArtanh = -1;
        boolean factorsContainOnlyTrigonometricalFunctions = true;
        boolean factorsContainOnlyTrigonometricalAndExponentialFunctions = true;
        int indexOfExponentialFunction = -1;

        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }
            if (!(factors.get(i) instanceof Function) || !(((Function) factors.get(i)).getType().equals(TypeFunction.sin)
                    || ((Function) factors.get(i)).getType().equals(TypeFunction.cos) || ((Function) factors.get(i)).getType().equals(TypeFunction.exp))
                    || !factors.get(i).contains(var)) {
                factorsContainOnlyTrigonometricalAndExponentialFunctions = false;
            }
            if (factorsContainOnlyTrigonometricalAndExponentialFunctions
                    && ((Function) factors.get(i)).getType().equals(TypeFunction.exp)
                    && indexOfExponentialFunction == -1) {
                /*
                 Falls der Integrand nur Faktoren enthält, welche aus
                 Exponentialfunktionen und trig. Funktionen bestehen -> Index
                 der (letzten) Exponentialfunktion ausgeben (diese werden
                 ohnehin zu einer gesammelt).
                 */
                indexOfExponentialFunction = i;
            }
            if (!(factors.get(i) instanceof Function) || !(((Function) factors.get(i)).getType().equals(TypeFunction.sin)
                    || ((Function) factors.get(i)).getType().equals(TypeFunction.cos)) || !factors.get(i).contains(var)) {
                factorsContainOnlyTrigonometricalFunctions = false;
            }
            if (factors.get(i).contains(var) && SimplifyPolynomialMethods.isPolynomial(factors.get(i), var)) {
                factorsContainPolynomial = true;
                indexOfPolynomial = i;
            }
            if (factors.get(i) instanceof Function && (((Function) factors.get(i)).getType().equals(TypeFunction.lg)
                    || ((Function) factors.get(i)).getType().equals(TypeFunction.ln)) && factors.get(i).contains(var)) {
                factorsContainLogarithm = true;
                indexOfLogarithm = i;
            }
            if (factors.get(i) instanceof Function && (((Function) factors.get(i)).getType().equals(TypeFunction.arctan)
                    || ((Function) factors.get(i)).getType().equals(TypeFunction.artanh)) && factors.get(i).contains(var)) {
                factorsContainArctanOrArtanh = true;
                indexOfArctanOrArtanh = i;
            }

        }

        if (factorsContainPolynomial && !factorsContainLogarithm && !factorsContainArctanOrArtanh) {
            return indexOfPolynomial;
        }
        if (factorsContainLogarithm) {
            return indexOfLogarithm;
        }
        if (factorsContainArctanOrArtanh) {
            return indexOfArctanOrArtanh;
        }
        if (factorsContainOnlyTrigonometricalFunctions) {
            // Hier ist die Wahl für u' egal.
            return 0;
        }
        if (factorsContainOnlyTrigonometricalAndExponentialFunctions) {
            // Hier angelangt, MUSS factors mindestens eine Exponentialfunktion enthalten.
            return indexOfExponentialFunction;
        }

        return false;

    }

    /**
     * Entscheidet, ob eine partielle Integration von f erlaubt ist.
     *
     * @throws EvaluationException
     */
    public static boolean allowPartialIntegration(Expression f, String var) throws EvaluationException {

        ExpressionCollection factors = SimplifyUtilities.getFactors(f);

        boolean allowPartialIntegration = true;
        boolean containsExpSinCos = false;

        for (int i = 0; i < factors.getBound(); i++) {

            if (!allowPartialIntegration) {
                break;
            }

            if (SimplifyPolynomialMethods.isPolynomial(factors.get(i), var)
                    && SimplifyPolynomialMethods.degreeOfPolynomial(factors.get(i), var).compareTo(BigInteger.valueOf(100)) <= 0) {
                continue;
            }

            if (factors.get(i) instanceof Function) {
                if (((Function) factors.get(i)).getType().equals(TypeFunction.exp)
                        || ((Function) factors.get(i)).getType().equals(TypeFunction.sin)
                        || ((Function) factors.get(i)).getType().equals(TypeFunction.cos)) {
                    Expression interior_derivative = ((Function) factors.get(i)).getLeft().diff(var).simplify();
                    if (!interior_derivative.contains(var)) {
                        containsExpSinCos = true;
                        continue;
                    }
                }
                if (((Function) factors.get(i)).getType().equals(TypeFunction.ln) && !containsExpSinCos) {
                    if (SimplifyPolynomialMethods.isPolynomial(((Function) factors.get(i)).getLeft(), var)) {
                        continue;
                    }
                }
            } else if (factors.get(i).isPower() && !((BinaryOperation) factors.get(i)).getLeft().contains(var)) {
                // Es liegt eine Exponentialfunktion (zu einer eventuell anderen Basis als e) vor.
                Expression exponentDerivative = ((BinaryOperation) factors.get(i)).getRight().diff(var).simplify();
                if (!exponentDerivative.contains(var)) {
                    containsExpSinCos = true;
                    continue;
                }
            }

            allowPartialIntegration = false;

        }

        if (allowPartialIntegration) {
            return allowPartialIntegration;
        }

        if (factors.getBound() == 2 && factors.get(0) != null && factors.get(1) != null) {
            // Typ: ln*x^a oder lg(x)*x^a
            if (factors.get(0) instanceof Function && (((Function) factors.get(0)).getType().equals(TypeFunction.lg)
                    || ((Function) factors.get(0)).getType().equals(TypeFunction.ln))
                    && ((Function) factors.get(0)).getLeft().equals(Variable.create(var))
                    && factors.get(1).isPower()
                    && ((BinaryOperation) factors.get(1)).getLeft().equals(Variable.create(var))
                    && !((BinaryOperation) factors.get(1)).getRight().contains(var)) {
                allowPartialIntegration = true;
            } else if (factors.get(1) instanceof Function && (((Function) factors.get(1)).getType().equals(TypeFunction.lg)
                    || ((Function) factors.get(1)).getType().equals(TypeFunction.ln))
                    && ((Function) factors.get(1)).getLeft().equals(Variable.create(var))
                    && factors.get(0).isPower()
                    && ((BinaryOperation) factors.get(0)).getLeft().equals(Variable.create(var))
                    && !((BinaryOperation) factors.get(0)).getRight().contains(var)) {
                allowPartialIntegration = true;
            }
            // Typ Polynom*arctan(x)
            if (factors.get(0) instanceof Function && ((Function) factors.get(0)).getType().equals(TypeFunction.arctan)
                    && ((Function) factors.get(0)).getLeft().equals(Variable.create(var))
                    && SimplifyPolynomialMethods.isPolynomial(factors.get(1), var)) {
                allowPartialIntegration = true;
            } else if (factors.get(1) instanceof Function && ((Function) factors.get(1)).getType().equals(TypeFunction.arctan)
                    && ((Function) factors.get(1)).getLeft().equals(Variable.create(var))
                    && SimplifyPolynomialMethods.isPolynomial(factors.get(0), var)) {
                allowPartialIntegration = true;
            }
            // Typ Polynom*artanh(x)
            if (factors.get(0) instanceof Function && ((Function) factors.get(0)).getType().equals(TypeFunction.artanh)
                    && ((Function) factors.get(0)).getLeft().equals(Variable.create(var))
                    && SimplifyPolynomialMethods.isPolynomial(factors.get(1), var)) {
                allowPartialIntegration = true;
            } else if (factors.get(1) instanceof Function && ((Function) factors.get(1)).getType().equals(TypeFunction.artanh)
                    && ((Function) factors.get(1)).getLeft().equals(Variable.create(var))
                    && SimplifyPolynomialMethods.isPolynomial(factors.get(0), var)) {
                allowPartialIntegration = true;
            }

        }

        if (allowPartialIntegration) {
            return allowPartialIntegration;
        }

        if (f.isQuotient()) {
            // Typ: ln(x)/x^a
            if (((BinaryOperation) f).getLeft().equals(new Function(Variable.create(var), TypeFunction.ln))
                    && ((BinaryOperation) f).getRight() instanceof BinaryOperation
                    && ((BinaryOperation) ((BinaryOperation) f).getRight()).getType().equals(TypeBinary.POW)
                    && ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft().equals(Variable.create(var))
                    && !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().contains(var)
                    && !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().equals(Expression.ONE)) {
                allowPartialIntegration = true;
            }

        }

        return allowPartialIntegration;

    }

    /**
     * Versucht mittels partieller Integration int(u'*v) = u*v - int(u*v') zu
     * integrieren.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws exceptions.EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateByPartialIntegration(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotProduct() && f.isNotQuotient()) {
            throw new NotPreciseIntegrableException();
        }

        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(f);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
        Expression uPrime, v;
        Operator integralOfUPrime;
        Object u;

        // Zunächst wird versucht, eine geschickte Wahl für u' zu treffen.
        Object indexOfUPrime = cleverChoiceForPartialIntegration(factorsEnumerator, var);

        // Falls eine "clevere" Wahl für u' vorliegt.
        if (indexOfUPrime instanceof Integer) {

            v = factorsEnumerator.get((int) indexOfUPrime);
            factorsEnumerator.remove((int) indexOfUPrime);
            uPrime = SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);

            Object[] paramsIntegralOfUPrime = new Object[2];
            paramsIntegralOfUPrime[0] = uPrime;
            paramsIntegralOfUPrime[1] = var;
            integralOfUPrime = new Operator(TypeOperator.integral, paramsIntegralOfUPrime);
            u = indefiniteIntegration(integralOfUPrime, true);

            if (u instanceof Expression) {

                Operator integralOfUTimesVPrime;
                Object[] paramsIntegralOfUTimesVPrime = new Object[2];
                paramsIntegralOfUTimesVPrime[0] = ((Expression) u).mult(v.diff(var)).simplify();
                paramsIntegralOfUTimesVPrime[1] = var;
                integralOfUTimesVPrime = new Operator(TypeOperator.integral, paramsIntegralOfUTimesVPrime);

                Object restOfPartialIntegral = indefiniteIntegration(integralOfUTimesVPrime, true);

                if (restOfPartialIntegral instanceof Expression) {
                    return ((Expression) u).mult(v).sub((Expression) restOfPartialIntegral);
                } else {
                    factorsEnumerator.put((int) indexOfUPrime, v);
                }

            } else {
                factorsEnumerator.put((int) indexOfUPrime, v);
            }

        }

        /*
         Sonst, falls keine "clevere" Wahl für u' getroffen werden konnte,
         dann kann gar nicht partiell integriert werden.
         */
        throw new NotPreciseIntegrableException();

    }

}
