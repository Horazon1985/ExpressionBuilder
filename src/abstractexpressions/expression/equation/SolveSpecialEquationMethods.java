package abstractexpressions.expression.equation;

import abstractexpressions.expression.computation.ArithmeticMethods;
import exceptions.EvaluationException;
import exceptions.NotSubstitutableException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.TWO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.TypeFunction;
import enums.TypeSimplify;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyExponentialRelations;
import abstractexpressions.expression.utilities.SimplifyRationalFunctionMethods;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import abstractexpressions.expression.substitution.SubstitutionUtilities;
import abstractexpressions.expression.utilities.SimplifyPolynomialMethods;
import exceptions.MathToolException;
import exceptions.NotAlgebraicallySolvableException;

public abstract class SolveSpecialEquationMethods extends SolveGeneralEquationMethods {

    /**
     * Private Fehlerklasse für den Fall, dass ein Ausdruck keine rationale
     * Funktion in x und einer Quadratwurzel aus einer quadratischen Funktion in
     * x (x = var) ist.
     */
    private static class NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException extends MathToolException {

        private static final String NOT_RATIONAL_FUNCTION_IN_VAR_AND_SQRT_OF_QUADRATIC_FUNCTION = "Expression is not a rational function in variable and the square root of a quadratic function.";

        public NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException() {
            super(NOT_RATIONAL_FUNCTION_IN_VAR_AND_SQRT_OF_QUADRATIC_FUNCTION);
        }

    }

    /**
     * Private Fehlerklasse für den Fall, dass ein Ausdruck keine rationale
     * Funktion in x und einer weiteren algebraischen Funktion in x (x = var)
     * ist.
     */
    private static class NotRationalFunctionInVarAndAnotherAlgebraicFunctionException extends MathToolException {

        private static final String NOT_RATIONAL_FUNCTION_IN_VAR_AND_ANOTHER_ALGEBRAIC_FUNCTION = "Expression is not a rational function in variable and another algebraic function.";

        public NotRationalFunctionInVarAndAnotherAlgebraicFunctionException() {
            super(NOT_RATIONAL_FUNCTION_IN_VAR_AND_ANOTHER_ALGEBRAIC_FUNCTION);
        }

    }

    private static final HashSet<TypeSimplify> simplifyTypesRationalExponentialEquation = getSimplifyTypesRationalExponentialEquation();
    private static final HashSet<TypeSimplify> simplifyTypesRationalTrigonometricalEquation = getSimplifyTypesRationalTrigonometricalEquation();
    private static final HashSet<TypeSimplify> simplifyTypesAlgebraicEquation = getSimplifyTypesAlgebraicEquation();

    private static HashSet<TypeSimplify> getSimplifyTypesRationalExponentialEquation() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_factorize);
        simplifyTypes.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients);
        simplifyTypes.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        return simplifyTypes;
    }

    private static HashSet<TypeSimplify> getSimplifyTypesRationalTrigonometricalEquation() {
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
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypes.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypes.add(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions);
        return simplifyTypes;
    }

    private static HashSet<TypeSimplify> getSimplifyTypesAlgebraicEquation() {
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
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypes.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypes.add(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions);
        return simplifyTypes;
    }

    // Exponentialgleichungen.
    /**
     * Hauptmethode zum Lösen von Exponentialgleichungen f = 0. Ist f keine
     * solche Gleichung, so wird eine NotAlgebraicallySolvableException
     * geworfen.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveExponentialEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        if (!SimplifyRationalFunctionMethods.isRationalFunktionInExp(f, var, new HashSet())) {
            throw new NotAlgebraicallySolvableException();
        }

        ExpressionCollection zeros = new ExpressionCollection();
        HashSet<Expression> argumentsInExp = new HashSet();

        // Konstante Summanden aus der Exponentialfunktion rausziehen.
        f = SimplifyExponentialRelations.separateConstantPartsInRationalExponentialEquations(f, var);

        /*
         Falls f keine rationale Funktion in einer Exponentialfunktion ist (1.
         Abfrage), oder falls f konstant bzgl. var ist (2. Abfrage), dann
         werden keine Lösungen ermittelt (diese Methode ist dafür nicht
         zuständig).
         */
        if (!SimplifyRationalFunctionMethods.isRationalFunktionInExp(f, var, argumentsInExp) || argumentsInExp.isEmpty()) {
            return zeros;
        }

        BigInteger gcdOfNumerators = BigInteger.ONE;
        BigInteger lcmOfDenominators = BigInteger.ONE;

        Iterator<Expression> iter = argumentsInExp.iterator();
        Expression firstFactorOfArgument = iter.next();
        Expression currentQuotient;

        while (iter.hasNext()) {
            currentQuotient = iter.next().div(firstFactorOfArgument).simplify();
            // Die folgende Abfrage müsste wegen Vorbedingung immer true sein. Trotzdem sicherheitshalber!
            if (currentQuotient.isIntegerConstantOrRationalConstant()) {

                if (currentQuotient.isIntegerConstant()) {
                    gcdOfNumerators = gcdOfNumerators.gcd(((Constant) currentQuotient).getValue().toBigInteger());
                } else {
                    gcdOfNumerators = gcdOfNumerators.gcd(((Constant) ((BinaryOperation) currentQuotient).getLeft()).getValue().toBigInteger());
                    lcmOfDenominators = ArithmeticMethods.lcm(lcmOfDenominators,
                            ((Constant) ((BinaryOperation) currentQuotient).getRight()).getValue().toBigInteger());
                }

            }
        }

        // Das ist die eigentliche Substitution.
        Expression substitution = new Constant(gcdOfNumerators).mult(firstFactorOfArgument).div(lcmOfDenominators).exp().simplify();

        try {

            Expression fSubstituted = SubstitutionUtilities.substitute(f, var, substitution);
            String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
            ExpressionCollection zerosOfSubstitutedEquation = SolveGeneralEquationMethods.solveZeroEquation(fSubstituted, substVar);
            zeros = new ExpressionCollection();
            ExpressionCollection currentZeros;
            // Rücksubstitution.
            for (int i = 0; i < zerosOfSubstitutedEquation.getBound(); i++) {
                try {
                    currentZeros = SolveGeneralEquationMethods.solveGeneralEquation(substitution, zerosOfSubstitutedEquation.get(i), var);
                    for (int j = 0; j < currentZeros.getBound(); j++) {
                        /*
                         Für die Vereinfachung der Lösungen sollen HIER Logarithmen auseinandergezogen werden, 
                         da es für diesen Zweck besser ist.
                         */
                        currentZeros.put(j, currentZeros.get(j).simplify(simplifyTypesRationalExponentialEquation));
                    }
                    // Lösungen hinzufügen.
                    zeros.addAll(currentZeros);
                } catch (EvaluationException e) {
                    /*
                     Dann ist zerosOfSubstitutedEquation.get(i) eine ungültige
                     Lösung -> zerosOfSubstitutedEquation.get(i) nicht in die
                     Lösungen mitaufnehmen.
                     */
                }
            }

        } catch (NotSubstitutableException e) {
        }

        return zeros;

    }

    // Trigonometrische Gleichungen.
    /**
     * Hauptmethode zum Lösen von trigonometrischen Gleichungen f = 0. Ist f
     * keine solche Gleichung, so wird eine NotAlgebraicallySolvableException
     * geworfen.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveTrigonometricalEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        if (!SimplifyRationalFunctionMethods.isRationalFunktionInTrigonometricalFunctions(f, var, new HashSet())) {
            throw new NotAlgebraicallySolvableException();
        }

        ExpressionCollection zeros = new ExpressionCollection();
        HashSet<Expression> argumentsInTrigonometricFunctions = new HashSet();
        /*
         Falls f keine rationale Funktion in einer Exponentialfunktion ist (1.
         Abfrage), oder falls f konstant bzgl. var ist (2. Abfrage), dann
         werden keine Lösungen ermittelt (diese Methode ist dafür nicht
         zuständig).
         */
        if (!SimplifyRationalFunctionMethods.isRationalFunktionInTrigonometricalFunctions(f, var, argumentsInTrigonometricFunctions) || argumentsInTrigonometricFunctions.isEmpty()) {
            throw new NotAlgebraicallySolvableException();
        }

        BigInteger gcdOfNumerators = BigInteger.ONE;
        BigInteger lcmOfDenominators = BigInteger.ONE;

        Iterator<Expression> iter = argumentsInTrigonometricFunctions.iterator();
        Expression firstFactorOfArgument = iter.next();
        Expression currentQuotient;

        while (iter.hasNext()) {
            currentQuotient = iter.next().div(firstFactorOfArgument).simplify();
            // Die folgende Abfrage müsste wegen Vorbedingung immer true sein. Trotzdem sicherheitshalber!
            if (currentQuotient.isIntegerConstantOrRationalConstant()) {

                if (currentQuotient.isIntegerConstant()) {
                    gcdOfNumerators = gcdOfNumerators.gcd(((Constant) currentQuotient).getValue().toBigInteger());
                } else {
                    gcdOfNumerators = gcdOfNumerators.gcd(((Constant) ((BinaryOperation) currentQuotient).getLeft()).getValue().toBigInteger());
                    lcmOfDenominators = ArithmeticMethods.lcm(lcmOfDenominators,
                            ((Constant) ((BinaryOperation) currentQuotient).getRight()).getValue().toBigInteger());
                }

            }
        }

        /* 
         Das ist die eigentliche Substitution. BEISPIEL: f = sin(4*a*x/7) + 4*cos(6*a*x/7).
         Dann substitution = 2*a*x/7. Die substitutierte Gleichung lautet demnach: 
         f = sin(2*X_1) + 4*cos(3*X_1). X_1 ist dabei der Ausdruck substitution.
         */
        Expression substitution = new Constant(gcdOfNumerators).mult(firstFactorOfArgument).div(lcmOfDenominators).simplify();

        try {

            Expression fSubstituted = SubstitutionUtilities.substitute(f, var, substitution);
            String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
            fSubstituted = fSubstituted.simplify(simplifyTypesRationalTrigonometricalEquation);
            /*
             Das Folgende ist eine Sicherheitsabfrage: Die substituierte Gleichung sollte vom 
             folgenden Typ sein: Alle Argumente, die in trigonometrischen Funktionen vorkommen,
             müssen von der Form n*x sein, wobei n eine ganze Zahl und x eine Variable ist.
             */
            if (!SimplifyRationalFunctionMethods.doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable(fSubstituted, substVar)) {
                throw new NotAlgebraicallySolvableException();
            }

            /*
             Nun ist fSubstituted eine rationale Funtion in sin(n*X_1) und cos(m*X_1), X_1 = substVar.
             Jetzt muss fSubstituted als rationale Function in sin(X_1) und cos(X_1) dargestellt werden.
             WICHTIG: Beim Vereinfachen darf hier nicht simplifyFunctionalRelations() verwendet werden,
             da dann beispielsweise sin(x)*cos(x) wieder zu sin(2*x)/2 vereinfacht wird.
             */
            fSubstituted = SimplifyRationalFunctionMethods.expandRationalFunctionInTrigonometricalFunctions(fSubstituted, substVar).simplify(simplifyTypesRationalTrigonometricalEquation);

            /*
             Jetzt werden zwei Versuche unternommen: (1) Sinusausdrücke durch Cosinusausdrücke zu ersetzen.
             (2) Cosinusausdrücke durch Sinusausdrücke zu ersetzen. Danach wird jeweils geprüft, ob 
             der so entstandene Ausdruck ein Polynom in einem Cosinus- oder einem Sinusausdruck ist.
             */
            Expression fNew = substituteInTrigonometricalEquationSinByCos(fSubstituted).simplify(simplifyTypesRationalTrigonometricalEquation);
            String polynomVar = SubstitutionUtilities.getSubstitutionVariable(fNew);

            if (SimplifyRationalFunctionMethods.isRationalFunctionIn(Variable.create(substVar).cos(), fNew, substVar)) {

                Expression trigonometricalSubst = Variable.create(substVar).cos();
                Expression polynomial = SubstitutionUtilities.substitute(fNew, substVar, trigonometricalSubst);
                ExpressionCollection zerosOfSubstitutedEquation = SolveGeneralEquationMethods.solveZeroEquation(polynomial, polynomVar);
                zeros = new ExpressionCollection();

                // Rücksubstitution.
                for (int i = 0; i < zerosOfSubstitutedEquation.getBound(); i++) {
                    try {
                        zeros.addAll(SolveGeneralEquationMethods.solveGeneralEquation(substitution.cos(), zerosOfSubstitutedEquation.get(i), var));
                    } catch (EvaluationException e) {
                        /*
                         Dann ist zerosOfSubstitutedEquation.get(i) eine ungültige
                         Lösung -> zerosOfSubstitutedEquation.get(i) nicht in die
                         Lösungen mitaufnehmen.
                         */
                    }
                }

            }

            if (!zeros.isEmpty()) {
                return zeros;
            }

            fNew = substituteInTrigonometricalEquationCosBySin(fSubstituted).simplify(simplifyTypesRationalTrigonometricalEquation);

            if (SimplifyRationalFunctionMethods.isRationalFunctionIn(Variable.create(substVar).sin(), fNew, substVar)) {

                Expression trigonometricalSubst = Variable.create(substVar).sin();
                Expression polynomial = SubstitutionUtilities.substitute(fNew, substVar, trigonometricalSubst);
                ExpressionCollection zerosOfSubstitutedEquation = SolveGeneralEquationMethods.solveZeroEquation(polynomial, polynomVar);
                zeros = new ExpressionCollection();
                // Rücksubstitution.
                for (int i = 0; i < zerosOfSubstitutedEquation.getBound(); i++) {
                    try {
                        zeros.addAll(SolveGeneralEquationMethods.solveGeneralEquation(substitution.sin(), zerosOfSubstitutedEquation.get(i), var));
                    } catch (EvaluationException e) {
                        /*
                         Dann ist zerosOfSubstitutedEquation.get(i) eine ungültige
                         Lösung -> zerosOfSubstitutedEquation.get(i) nicht in die
                         Lösungen mitaufnehmen.
                         */
                    }
                }

            }

            if (!zeros.isEmpty()) {
                return zeros;
            }

            /* 
             3. Versuch: Wenn sich weder cos durch sin, noch umgekehrt substituieren lassen, 
             dann folgende Substitution vornehmen: t = tan(x/2) -> cos(x) = (1-t^2)/(1+t^2)
             und sin(x) = 2t/(1+t^2).
             */
            fNew = SubstitutionUtilities.substituteExpressionByAnotherExpression(fSubstituted, Variable.create(substVar).cos(),
                    ONE.sub(Variable.create(polynomVar).pow(2)).div(ONE.add(Variable.create(polynomVar).pow(2))));
            fNew = SubstitutionUtilities.substituteExpressionByAnotherExpression(fNew, Variable.create(substVar).sin(),
                    TWO.mult(Variable.create(polynomVar)).div(ONE.add(Variable.create(polynomVar).pow(2))));
            if (fNew.contains(substVar)) {
                throw new NotAlgebraicallySolvableException();
            }

            ExpressionCollection zerosOfSubstitutedEquation = SolveGeneralEquationMethods.solveZeroEquation(fNew, polynomVar);
            zeros = new ExpressionCollection();
            // Rücksubstitution.
            for (int i = 0; i < zerosOfSubstitutedEquation.getBound(); i++) {
                try {
                    zeros.addAll(SolveGeneralEquationMethods.solveGeneralEquation(substitution.div(2).tan(), zerosOfSubstitutedEquation.get(i), var));
                } catch (EvaluationException e) {
                    /*
                     Dann ist zerosOfSubstitutedEquation.get(i) eine ungültige
                     Lösung -> zerosOfSubstitutedEquation.get(i) nicht in die
                     Lösungen mitaufnehmen.
                     */
                }
            }

        } catch (NotSubstitutableException e) {
        }

        return zeros;

    }

    /**
     * Ersetzt in einem Ausdruck gerade Potenzen vom Sinus durch
     * Cosinus-Ausdrücke.<br>
     * BEISPIEL: sin(x)^4 wird durch (1 - cos(x)^2)^2 ersetzt.
     */
    private static Expression substituteInTrigonometricalEquationSinByCos(Expression f) {

        if (f instanceof Constant || f instanceof Variable) {
            return f;
        } else if (f instanceof BinaryOperation && f.isNotPower()) {
            return new BinaryOperation(substituteInTrigonometricalEquationSinByCos(((BinaryOperation) f).getLeft()),
                    substituteInTrigonometricalEquationSinByCos(((BinaryOperation) f).getRight()),
                    ((BinaryOperation) f).getType());
        } else if (f.isPower()) {
            if (((BinaryOperation) f).getLeft() instanceof Function
                    && ((Function) ((BinaryOperation) f).getLeft()).getType().equals(TypeFunction.sin)
                    && ((BinaryOperation) f).getRight().isEvenIntegerConstant()) {
                BigInteger n = ((Constant) ((BinaryOperation) f).getRight()).getValue().toBigInteger();
                return Expression.ONE.sub(((Function) ((BinaryOperation) f).getLeft()).getLeft().cos().pow(2)).pow(n.divide(BigInteger.valueOf(2)));
            }
            return substituteInTrigonometricalEquationSinByCos(((BinaryOperation) f).getLeft()).pow(
                    substituteInTrigonometricalEquationSinByCos(((BinaryOperation) f).getRight()));
        } else if (f instanceof Function) {
            return new Function(substituteInTrigonometricalEquationSinByCos(((Function) f).getLeft()), ((Function) f).getType());
        }

        /*
         Der Einfachheit halber sollen Operatoren und vom Benutzer definierte
         Funktionen, falls diese im Vorfeld nicht sinnvoll vereinfacht werden
         konnten, auch nicht transformiert werden.
         */
        return f;

    }

    /**
     * Ersetzt in einem Ausdruck gerade Potenzen vom Cosinus durch
     * Sinus-Ausdrücke.<br>
     * BEISPIEL: cos(x)^6 wird durch (1 - sin(x)^2)^3 ersetzt.
     */
    private static Expression substituteInTrigonometricalEquationCosBySin(Expression f) {

        if (f instanceof Constant || f instanceof Variable) {
            return f;
        } else if (f instanceof BinaryOperation && f.isNotPower()) {
            return new BinaryOperation(substituteInTrigonometricalEquationCosBySin(((BinaryOperation) f).getLeft()),
                    substituteInTrigonometricalEquationCosBySin(((BinaryOperation) f).getRight()),
                    ((BinaryOperation) f).getType());
        } else if (f.isPower()) {
            if (((BinaryOperation) f).getLeft().isFunction(TypeFunction.cos)
                    && ((BinaryOperation) f).getRight().isEvenIntegerConstant()) {
                BigInteger n = ((Constant) ((BinaryOperation) f).getRight()).getValue().toBigInteger();
                return Expression.ONE.sub(((Function) ((BinaryOperation) f).getLeft()).getLeft().sin().pow(2)).pow(n.divide(BigInteger.valueOf(2)));
            }
            return substituteInTrigonometricalEquationCosBySin(((BinaryOperation) f).getLeft()).pow(
                    substituteInTrigonometricalEquationCosBySin(((BinaryOperation) f).getRight()));
        } else if (f instanceof Function) {
            return new Function(substituteInTrigonometricalEquationCosBySin(((Function) f).getLeft()), ((Function) f).getType());
        }

        /*
         Der Einfachheit halber sollen Operatoren und vom Benutzer definierte
         Funktionen, falls diese im Vorfeld nicht sinnvoll vereinfacht werden
         konnten, auch nicht transformiert werden.
         */
        return f;

    }

    // Algebraische Gleichungen
    // Typ 1: f(x, (ax^2+bx+c)^(1/2)) = 0, f = rationale Funktion in zwei Veränderlichen. 
    /**
     * Hauptmethode zum Lösen von algebraischer Gleichungen der Form f(x,
     * (ax^2+bx+c)^(1/2)) = 0, f = rationale Funktion in zwei Veränderlichen.
     * Ist f keine solche Gleichung, so wird eine
     * NotAlgebraicallySolvableException geworfen.
     *
     * @throws EvaluationException, NotAlgebraicallySolvableException
     */
    public static ExpressionCollection solveRationalFunctionInVarAndSqrtOfQuadraticFunctionEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        Expression radicand;
        try {
            radicand = getRadicandIfFunctionIsRationalFunctionInVarAndSqrtOfQuadraticFunction(f, var);
        } catch (NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException e) {
            throw new NotAlgebraicallySolvableException();
        }

        ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(radicand, var);
        if (coefficients.getBound() != 3) {
            // Sollte eigentlich nicht eintreten.
            throw new NotAlgebraicallySolvableException();
        }

        Expression a = coefficients.get(2);
        Expression b = coefficients.get(1);
        Expression c = coefficients.get(0);
        Expression discriminant;
        try {
            discriminant = b.pow(2).sub(new Constant(4).mult(a).mult(c)).simplify();
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        ExpressionCollection zeros = new ExpressionCollection();

        Expression p, q;
        String substVarForEquation = SubstitutionUtilities.getSubstitutionVariable(f);
        if (discriminant.isAlwaysNegative()) {

            try {
                p = b.div(TWO.mult(a)).simplify();
                q = MINUS_ONE.mult(discriminant).pow(1, 2).div(TWO.mult(a)).simplify();
            } catch (EvaluationException e) {
                throw new NotAlgebraicallySolvableException();
            }

            // Nur der Fall a > 0 ist möglich (bei a < 0 wäre die Wurzel für kein x definiert, x = var).
            if (a.isAlwaysPositive()) {

                Expression fSubstituted = f;

                // ZUERST: (ax^2+bx+c)^(1/2) wird durch q*a^(1/2)*(exp(t)+exp(-t))/2 ersetzt.
                fSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(fSubstituted, radicand.pow(1, 2),
                        q.mult(a.pow(1, 2)).mult(Variable.create(substVarForEquation).exp().add(MINUS_ONE.mult(Variable.create(substVarForEquation)).exp())).div(2));
                // DANACH: x wird durch q*(exp(t)-exp(-t))/2 - p ersetzt, t = substVarForEquation.
                fSubstituted = fSubstituted.replaceVariable(var, q.mult(Variable.create(substVarForEquation).exp().sub(MINUS_ONE.mult(Variable.create(substVarForEquation)).exp())).div(2).sub(p));

                if (fSubstituted.contains(var)) {
                    // Sollte eigentlich nicht passieren.
                    throw new NotAlgebraicallySolvableException();
                }

                try {
                    // Vor dem Lösen vereinfachen.
                    fSubstituted = fSubstituted.simplify();
                } catch (EvaluationException e) {
                    // Sollte bei einer gültigen Gleichung nicht passieren.
                    throw new NotAlgebraicallySolvableException();
                }

                ExpressionCollection zerosOfSubstitutedEquation = solveZeroEquation(fSubstituted, substVarForEquation);

                // Rücksubstitution: u = (exp(t) - exp(-t))/2 und u = (x + p)/q, d.h. x = qu - p = q(exp(t) - exp(-t))/2 - p, t = substVarForEquation.
                for (Expression zero : zerosOfSubstitutedEquation) {
                    try {
                        zeros.add(q.mult(zero.exp().sub(MINUS_ONE.mult(zero).exp()).div(2)).sub(p).simplify());
                    } catch (EvaluationException e) {
                        // Nichts tun, ignorieren!
                    }
                }

                return zeros;

            }

        } else if (discriminant.isAlwaysPositive()) {

            if (a.isAlwaysPositive()) {

                try {
                    p = b.div(TWO.mult(a)).simplify();
                    q = discriminant.pow(1, 2).div(TWO.mult(a)).simplify();
                } catch (EvaluationException e) {
                    throw new NotAlgebraicallySolvableException();
                }
                Expression fSubstituted = f;

                // ZUERST: (ax^2+bx+c)^(1/2) wird durch q*a^(1/2)*(exp(t)-exp(-t))/2 ersetzt.
                fSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(fSubstituted, radicand.pow(1, 2),
                        q.mult(a.pow(1, 2)).mult(Variable.create(substVarForEquation).exp().sub(MINUS_ONE.mult(Variable.create(substVarForEquation)).exp())).div(2));
                // DANACH: x wird durch q*(exp(t)+exp(-t))/2 - p ersetzt, t = substVarForEquation.
                fSubstituted = fSubstituted.replaceVariable(var, q.mult(Variable.create(substVarForEquation).exp().add(MINUS_ONE.mult(Variable.create(substVarForEquation)).exp())).div(2).sub(p));

                if (fSubstituted.contains(var)) {
                    // Sollte eigentlich nicht passieren.
                    throw new NotAlgebraicallySolvableException();
                }

                try {
                    // Vor dem Lösen vereinfachen.
                    fSubstituted = fSubstituted.simplify();
                } catch (EvaluationException e) {
                    // Sollte bei einer gültigen Gleichung nicht passieren.
                    throw new NotAlgebraicallySolvableException();
                }

                ExpressionCollection zerosOfSubstitutedEquation = solveZeroEquation(fSubstituted, substVarForEquation);

                // Rücksubstitution: u = (exp(t) + exp(-t))/2 und u = (x + p)/q, d.h. x = qu - p = q(exp(t) + exp(-t))/2 - p, t = substVarForEquation.
                for (Expression zero : zerosOfSubstitutedEquation) {
                    try {
                        zeros.add(q.mult(zero.exp().add(MINUS_ONE.mult(zero).exp()).div(2)).sub(p).simplify());
                    } catch (EvaluationException e) {
                        // Nichts tun, ignorieren!
                    }
                }

                return zeros;

            } else if (a.isAlwaysNegative()) {

                try {
                    p = b.div(TWO.mult(a)).simplify();
                    q = MINUS_ONE.mult(discriminant.pow(1, 2)).div(TWO.mult(a)).simplify();
                } catch (EvaluationException e) {
                    throw new NotAlgebraicallySolvableException();
                }
                Expression fSubstituted = f;

                // ZUERST: (ax^2+bx+c)^(1/2) wird durch q*(-a)^(1/2)*cos(t) ersetzt.
                fSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(fSubstituted, radicand.pow(1, 2),
                        q.mult(MINUS_ONE.mult(a).pow(1, 2)).mult(Variable.create(substVarForEquation).cos()));
                // DANACH: x wird durch q*sin(t) - p ersetzt, t = substVarForEquation.
                fSubstituted = fSubstituted.replaceVariable(var, q.mult(Variable.create(substVarForEquation).sin().sub(p)));

                if (fSubstituted.contains(var)) {
                    // Sollte eigentlich nicht passieren.
                    throw new NotAlgebraicallySolvableException();
                }

                try {
                    // Vor dem Lösen vereinfachen.
                    fSubstituted = fSubstituted.simplify();
                } catch (EvaluationException e) {
                    // Sollte bei einer gültigen Gleichung nicht passieren.
                    throw new NotAlgebraicallySolvableException();
                }

                ExpressionCollection zerosOfSubstitutedEquation = solveZeroEquation(fSubstituted, substVarForEquation);

                // Rücksubstitution: u = cos(t) und u = (x + p)/q, d.h. x = qu - p = q*cos(t) - p, t = substVarForEquation.
                Expression zeroSimplified;
                for (Expression zero : zerosOfSubstitutedEquation) {
                    try {
                        /*
                         Folgende Lösungen werden ausgeschlossen: diejenigen, bei
                         denen q*(-a)^(1/2)*cos(t) < 0 (d.h. als cos(t) < 0),
                         denn die Quadratwurzel muss darf nicht negativ sein.
                         */
                        zeroSimplified = q.mult(zero.cos()).sub(p).simplify();
                        if (!zeroSimplified.isConstant() && !zeroSimplified.isAlwaysNegative()
                                || zeroSimplified.isConstant() && zeroSimplified.isAlwaysNonNegative()) {
                            zeros.add(zeroSimplified);
                        }
                    } catch (EvaluationException e) {
                        // Nichts tun, ignorieren!
                    }
                }

                return zeros;

            }

        }

        // Fall: discriminant = 0. Schwierig zu lösen!
        throw new NotAlgebraicallySolvableException();

    }

    private static Expression getRadicandIfFunctionIsRationalFunctionInVarAndSqrtOfQuadraticFunction(Expression f, String var) throws NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException {

        ExpressionCollection setOfSubstitutions = new ExpressionCollection();
        addSuitableSubstitutionForSolvingAlgebraicEquations(f, var, setOfSubstitutions);

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
     * Hauptmethode zum Lösen von algebraischen Gleichungen der Form f(x, g(x))
     * = 0, f = rationale Funktion in zwei Veränderlichen und g mit der
     * Eigenschaft, dass die Auflösung x = h(y) eine rationale Funktion in y
     * ist. Ist f keine solche Gleichung, so wird eine
     * NotAlgebraicallySolvableException geworfen.
     *
     * @throws EvaluationException, NotAlgebraicallySolvableException
     */
    public static ExpressionCollection solveRationalFunctionInVarAndAnotherAlgebraicExpressionEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        Expression algebraicTerm, radicand, exponent;
        try {
            algebraicTerm = getAlgebraicTermIfFunctionIsRationalFunctionInVarAndAnotherAlgebraicFunction(f, var);
            if (!algebraicTerm.isRationalPower()) {
                throw new NotAlgebraicallySolvableException();
            }
            radicand = ((BinaryOperation) algebraicTerm).getLeft();
            exponent = ((BinaryOperation) algebraicTerm).getRight();
        } catch (NotRationalFunctionInVarAndAnotherAlgebraicFunctionException e) {
            throw new NotAlgebraicallySolvableException();
        }

        String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
        ExpressionCollection zerosOfAlgebraicTermMinusSubstVar;

        try {
            zerosOfAlgebraicTermMinusSubstVar = solveGeneralEquation(radicand, Variable.create(substVar).pow(ONE.div(exponent)), var);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        // Lösung muss eindeutig sein.        
        if (zerosOfAlgebraicTermMinusSubstVar.getBound() != 1) {
            throw new NotAlgebraicallySolvableException();
        }

        if (!SimplifyRationalFunctionMethods.isRationalFunction(zerosOfAlgebraicTermMinusSubstVar.get(0), substVar)) {
            throw new NotAlgebraicallySolvableException();
        }

        Expression fSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(f, algebraicTerm, Variable.create(substVar));
        fSubstituted = fSubstituted.replaceVariable(var, zerosOfAlgebraicTermMinusSubstVar.get(0));

        ExpressionCollection zerosInSubstitutionVar;
        try {
            fSubstituted = fSubstituted.simplify(simplifyTypesAlgebraicEquation);
            zerosInSubstitutionVar = solveZeroEquation(fSubstituted, substVar);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        // Prüfung auf Gültigkeit!
        for (int i = 0; i < zerosInSubstitutionVar.getBound(); i++) {
            try {
                algebraicTerm.replaceVariable(var, zerosInSubstitutionVar.get(i)).simplify();
            } catch (EvaluationException e) {
                // Lösung rauswerfen.
                zerosInSubstitutionVar.put(i, null);
            }
        }

        ExpressionCollection zeros = new ExpressionCollection();
        // Rücksubstitution!
        for (Expression zero : zerosInSubstitutionVar) {
            try {
                zeros.add(zerosOfAlgebraicTermMinusSubstVar.get(0).replaceVariable(substVar, zero).simplify());
            } catch (EvaluationException e) {
                // Nichts tun, ignorieren!
            }
        }

        return zeros;

    }

    private static Expression getAlgebraicTermIfFunctionIsRationalFunctionInVarAndAnotherAlgebraicFunction(Expression f, String var) throws NotRationalFunctionInVarAndAnotherAlgebraicFunctionException {

        ExpressionCollection setOfSubstitutions = new ExpressionCollection();
        addSuitableSubstitutionForSolvingAlgebraicEquations(f, var, setOfSubstitutions);

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

    /**
     * Ermittelt potenzielle Substitutionen für eine algebraische Gleichung.
     */
    private static void addSuitableSubstitutionForSolvingAlgebraicEquations(Expression f, String var, ExpressionCollection setOfSubstitutions) {
        if (f.contains(var) && f instanceof BinaryOperation && f.isNotPower()) {
            addSuitableSubstitutionForSolvingAlgebraicEquations(((BinaryOperation) f).getLeft(), var, setOfSubstitutions);
            addSuitableSubstitutionForSolvingAlgebraicEquations(((BinaryOperation) f).getRight(), var, setOfSubstitutions);
        } else if (f.contains(var) && f.isPower() && ((BinaryOperation) f).getRight().isRationalConstant()) {
            setOfSubstitutions.add(f);
        }
    }

}
