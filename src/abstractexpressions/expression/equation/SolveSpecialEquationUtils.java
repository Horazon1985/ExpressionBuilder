package abstractexpressions.expression.equation;

import abstractexpressions.expression.computation.ArithmeticUtils;
import exceptions.EvaluationException;
import exceptions.NotSubstitutableException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.THREE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.TypeFunction;
import enums.TypeSimplify;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.basic.SimplifyExponentialRelationsUtils;
import abstractexpressions.expression.basic.SimplifyRationalFunctionUtils;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import abstractexpressions.expression.substitution.SubstitutionUtilities;
import abstractexpressions.expression.basic.SimplifyPolynomialUtils;
import abstractexpressions.expression.basic.SimplifyUtilities;
import exceptions.MathToolException;
import exceptions.NotAlgebraicallySolvableException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class SolveSpecialEquationUtils extends SolveGeneralEquationUtils {

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

    private static final HashSet<TypeSimplify> simplifyTypesRationalExponentialEquation = new HashSet<>();
    private static final HashSet<TypeSimplify> simplifyTypesRationalTrigonometricalEquation = new HashSet<>();
    private static final HashSet<TypeSimplify> simplifyTypesAlgebraicEquation = new HashSet<>();

    static {
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.order_difference_and_division);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.order_sums_and_products);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.simplify_basic);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.order_difference_and_division);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.simplify_collect_products);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.simplify_factorize);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.simplify_functional_relations);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.simplify_expand_logarithms);
        simplifyTypesRationalExponentialEquation.add(TypeSimplify.order_sums_and_products);

        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.order_difference_and_division);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.order_sums_and_products);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_basic);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_collect_products);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_expand_moderate);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_multiply_exponents);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypesRationalTrigonometricalEquation.add(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions);

        simplifyTypesAlgebraicEquation.add(TypeSimplify.order_difference_and_division);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.order_sums_and_products);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_basic);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_collect_products);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_multiply_exponents);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_factorize);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypesAlgebraicEquation.add(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions);
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

        if (!SimplifyRationalFunctionUtils.isRationalFunktionInExp(f, var, new HashSet())) {
            throw new NotAlgebraicallySolvableException();
        }

        ExpressionCollection zeros = new ExpressionCollection();
        HashSet<Expression> argumentsInExp = new HashSet();

        // Konstante Summanden aus der Exponentialfunktion rausziehen.
        f = SimplifyExponentialRelationsUtils.separateConstantPartsInRationalExponentialEquations(f, var);

        /*
         Falls f keine rationale Funktion in einer Exponentialfunktion ist (1.
         Abfrage), oder falls f konstant bzgl. var ist (2. Abfrage), dann
         werden keine Lösungen ermittelt (diese Methode ist dafür nicht
         zuständig).
         */
        if (!SimplifyRationalFunctionUtils.isRationalFunktionInExp(f, var, argumentsInExp) || argumentsInExp.isEmpty()) {
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
                    gcdOfNumerators = gcdOfNumerators.gcd(((Constant) currentQuotient).getBigIntValue());
                } else {
                    gcdOfNumerators = gcdOfNumerators.gcd(((Constant) ((BinaryOperation) currentQuotient).getLeft()).getBigIntValue());
                    lcmOfDenominators = ArithmeticUtils.lcm(lcmOfDenominators,
                            ((Constant) ((BinaryOperation) currentQuotient).getRight()).getBigIntValue());
                }

            }
        }

        // Das ist die eigentliche Substitution.
        Expression substitution = new Constant(gcdOfNumerators).mult(firstFactorOfArgument).div(lcmOfDenominators).exp().simplify();

        try {

            Expression fSubstituted = SubstitutionUtilities.substitute(f, var, substitution);
            String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
            ExpressionCollection zerosOfSubstitutedEquation = SolveGeneralEquationUtils.solveZeroEquation(fSubstituted, substVar);
            zeros = new ExpressionCollection();
            ExpressionCollection currentZeros;
            // Rücksubstitution.
            for (int i = 0; i < zerosOfSubstitutedEquation.getBound(); i++) {
                try {
                    currentZeros = SolveGeneralEquationUtils.solveGeneralEquation(substitution, zerosOfSubstitutedEquation.get(i), var);
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

        if (!SimplifyRationalFunctionUtils.isRationalFunktionInTrigonometricalFunctions(f, var, new HashSet())) {
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
        if (!SimplifyRationalFunctionUtils.isRationalFunktionInTrigonometricalFunctions(f, var, argumentsInTrigonometricFunctions) || argumentsInTrigonometricFunctions.isEmpty()) {
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
                    gcdOfNumerators = gcdOfNumerators.gcd(((Constant) currentQuotient).getBigIntValue());
                } else {
                    gcdOfNumerators = gcdOfNumerators.gcd(((Constant) ((BinaryOperation) currentQuotient).getLeft()).getBigIntValue());
                    lcmOfDenominators = ArithmeticUtils.lcm(lcmOfDenominators,
                            ((Constant) ((BinaryOperation) currentQuotient).getRight()).getBigIntValue());
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
            if (!SimplifyRationalFunctionUtils.doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable(fSubstituted, substVar)) {
                throw new NotAlgebraicallySolvableException();
            }

            /*
             Nun ist fSubstituted eine rationale Funtion in sin(n*X_1) und cos(m*X_1), X_1 = substVar.
             Jetzt muss fSubstituted als rationale Function in sin(X_1) und cos(X_1) dargestellt werden.
             WICHTIG: Beim Vereinfachen darf hier nicht simplifyFunctionalRelations() verwendet werden,
             da dann beispielsweise sin(x)*cos(x) wieder zu sin(2*x)/2 vereinfacht wird.
             */
            fSubstituted = SimplifyRationalFunctionUtils.expandRationalFunctionInTrigonometricalFunctions(fSubstituted, substVar).simplify(simplifyTypesRationalTrigonometricalEquation);

            /*
             Jetzt werden zwei Versuche unternommen: (1) Sinusausdrücke durch Cosinusausdrücke zu ersetzen.
             (2) Cosinusausdrücke durch Sinusausdrücke zu ersetzen. Danach wird jeweils geprüft, ob 
             der so entstandene Ausdruck ein Polynom in einem Cosinus- oder einem Sinusausdruck ist.
             */
            Expression fNew = substituteInTrigonometricalEquationSinByCos(fSubstituted).simplify(simplifyTypesRationalTrigonometricalEquation);
            String polynomVar = SubstitutionUtilities.getSubstitutionVariable(fNew);

            if (SimplifyRationalFunctionUtils.isRationalFunctionIn(Variable.create(substVar).cos(), fNew, substVar)) {

                Expression trigonometricalSubst = Variable.create(substVar).cos();
                Expression polynomial = SubstitutionUtilities.substitute(fNew, substVar, trigonometricalSubst);
                ExpressionCollection zerosOfSubstitutedEquation = SolveGeneralEquationUtils.solveZeroEquation(polynomial, polynomVar);
                zeros = new ExpressionCollection();

                // Rücksubstitution.
                for (int i = 0; i < zerosOfSubstitutedEquation.getBound(); i++) {
                    try {
                        zeros.addAll(SolveGeneralEquationUtils.solveGeneralEquation(substitution.cos(), zerosOfSubstitutedEquation.get(i), var));
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

            if (SimplifyRationalFunctionUtils.isRationalFunctionIn(Variable.create(substVar).sin(), fNew, substVar)) {

                Expression trigonometricalSubst = Variable.create(substVar).sin();
                Expression polynomial = SubstitutionUtilities.substitute(fNew, substVar, trigonometricalSubst);
                ExpressionCollection zerosOfSubstitutedEquation = SolveGeneralEquationUtils.solveZeroEquation(polynomial, polynomVar);
                zeros = new ExpressionCollection();
                // Rücksubstitution.
                for (int i = 0; i < zerosOfSubstitutedEquation.getBound(); i++) {
                    try {
                        zeros.addAll(SolveGeneralEquationUtils.solveGeneralEquation(substitution.sin(), zerosOfSubstitutedEquation.get(i), var));
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

            ExpressionCollection zerosOfSubstitutedEquation = SolveGeneralEquationUtils.solveZeroEquation(fNew, polynomVar);
            zeros = new ExpressionCollection();
            // Rücksubstitution.
            for (int i = 0; i < zerosOfSubstitutedEquation.getBound(); i++) {
                try {
                    zeros.addAll(SolveGeneralEquationUtils.solveGeneralEquation(substitution.div(2).tan(), zerosOfSubstitutedEquation.get(i), var));
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
                BigInteger n = ((Constant) ((BinaryOperation) f).getRight()).getBigIntValue();
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
                BigInteger n = ((Constant) ((BinaryOperation) f).getRight()).getBigIntValue();
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
     * @throws EvaluationException
     * @throws NotAlgebraicallySolvableException
     */
    public static ExpressionCollection solveRationalFunctionInVarAndSqrtOfQuadraticFunctionEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        Expression radicand;
        try {
            radicand = getRadicandIfFunctionIsRationalFunctionInVarAndSqrtOfQuadraticFunction(f, var);
        } catch (NotRationalFunctionInVarAndSqrtOfQuadraticFunctionException e) {
            throw new NotAlgebraicallySolvableException();
        }

        ExpressionCollection coefficients = SimplifyPolynomialUtils.getPolynomialCoefficients(radicand, var);
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
                if (!SimplifyPolynomialUtils.isPolynomial(radicand, var)
                        || SimplifyPolynomialUtils.getDegreeOfPolynomial(radicand, var).compareTo(BigInteger.valueOf(2)) != 0) {
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
        if (!SimplifyRationalFunctionUtils.isRationalFunctionInFunctions(f, var, Variable.create(var), radicand.pow(1, 2))) {
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
     * @throws EvaluationException
     * @throws NotAlgebraicallySolvableException
     */
    public static ExpressionCollection solveRationalFunctionInVarAndAnotherAlgebraicExpressionEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        Expression algebraicTerm, radicand, exponent;
        try {
            algebraicTerm = getAlgebraicTermIfFunctionIsRationalFunctionInVarAndAnotherAlgebraicFunctionOrThrowException(f, var);
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

        if (!SimplifyRationalFunctionUtils.isRationalFunction(zerosOfAlgebraicTermMinusSubstVar.get(0), substVar)) {
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
            if (zero == null) {
                continue;
            }
            try {
                zeros.add(zerosOfAlgebraicTermMinusSubstVar.get(0).replaceVariable(substVar, zero).simplify());
            } catch (EvaluationException e) {
                // Nichts tun, ignorieren!
            }
        }

        return zeros;

    }

    private static Expression getAlgebraicTermIfFunctionIsRationalFunctionInVarAndAnotherAlgebraicFunctionOrThrowException(Expression f, String var) throws NotRationalFunctionInVarAndAnotherAlgebraicFunctionException {

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
        if (!SimplifyRationalFunctionUtils.isRationalFunctionInFunctions(f, var, Variable.create(var), algebraicTerm)) {
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

    //////////////////////// Gleichungen mit Radikalen /////////////////////////
    /**
     * Hilfsklasse;
     */
    private static class RadicalSummandData {

        public Expression factor;
        public Expression radicand;
        public Expression rootDegree;

        public RadicalSummandData(Expression factor, Expression radicand, Expression rootDegree) {
            this.factor = factor;
            this.radicand = radicand;
            this.rootDegree = rootDegree;
        }

        public RadicalSummandData(Expression factor, Expression radicand, Integer rootDegree) {
            this.factor = factor;
            this.radicand = radicand;
            this.rootDegree = new Constant(rootDegree);
        }

    }

    /**
     * Gibt Lösungen von Gleichungen, welche Summen von Wurzeln rationaler
     * Funktionen sind, zurück, wenn möglich.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallySolvableException
     */
    public static ExpressionCollection solveSumOfRadicalsEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        // Fall: a(x)*f(x)^(1/n) - h(x) = 0.
        try {
            return solveSumOfOneArbitraryRootEquation(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: a(x)*f(x)^(1/2) + b(x)*g(x)^(1/2) - h(x) = 0.
        try {
            return solveSumOfTwoSquareRootsEquation(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: a(x)*f(x)^(1/2) + b(x)*g(x)^(1/3) - h(x) = 0.
        try {
            return solveSumOfSquareRootAndCubicRootEquation(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: f(x)^(1/3) + g(x)^(1/3) - h(x) = 0.
        try {
            return solveSumOfTwoCubicRootsEquation(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        throw new NotAlgebraicallySolvableException();

    }

    private static Expression[] getSeparationInRadicalsAndNonRadicals(Expression f, String var, List<Integer> roots) throws NotAlgebraicallySolvableException {

        Expression rootPartLeft = ZERO;
        Expression rootPartRight = ZERO;
        Expression nonRootPartLeft = ZERO;
        Expression nonRootPartRight = ZERO;

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(f);

        boolean isRadicalSummand;

        for (Expression summand : summandsLeft) {
            if (roots == null) {
                try {
                    getFactorAndRadicand(summand, var, null);
                    rootPartLeft = rootPartLeft.add(summand);
                } catch (NotAlgebraicallySolvableException e) {
                    nonRootPartLeft = nonRootPartLeft.add(summand);
                }
            } else if (roots.isEmpty()) {
                try {
                    getFactorAndRadicand(summand, var, null);
                    throw new NotAlgebraicallySolvableException();
                } catch (NotAlgebraicallySolvableException e) {
                    nonRootPartLeft = nonRootPartLeft.add(summand);
                }
            } else {
                isRadicalSummand = false;
                for (int n : roots) {
                    try {
                        getFactorAndRadicand(summand, var, n);
                        isRadicalSummand = true;
                        rootPartLeft = rootPartLeft.add(summand);
                        roots.remove(Integer.valueOf(n));
                        break;
                    } catch (NotAlgebraicallySolvableException e) {
                    }
                }
                if (!isRadicalSummand) {
                    nonRootPartLeft = nonRootPartLeft.add(summand);
                }

            }
        }
        for (Expression summand : summandsRight) {
            if (roots == null) {
                try {
                    getFactorAndRadicand(summand, var, null);
                    rootPartRight = rootPartRight.add(summand);
                } catch (NotAlgebraicallySolvableException e) {
                    nonRootPartRight = nonRootPartRight.add(summand);
                }
            } else if (roots.isEmpty()) {
                try {
                    getFactorAndRadicand(summand, var, null);
                    throw new NotAlgebraicallySolvableException();
                } catch (NotAlgebraicallySolvableException e) {
                    nonRootPartRight = nonRootPartRight.add(summand);
                }
            } else {
                isRadicalSummand = false;
                for (int n : roots) {
                    try {
                        getFactorAndRadicand(summand, var, n);
                        isRadicalSummand = true;
                        rootPartRight = rootPartRight.add(summand);
                        roots.remove(Integer.valueOf(n));
                        break;
                    } catch (NotAlgebraicallySolvableException e) {
                    }
                }
                if (!isRadicalSummand) {
                    nonRootPartRight = nonRootPartRight.add(summand);
                }
            }
        }

        try {
            return new Expression[]{rootPartLeft.sub(rootPartRight).simplify(), nonRootPartLeft.sub(nonRootPartRight).simplify()};
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

    }

    private static RadicalSummandData[] getFactorsAndRadicandsInCaseOfSumOfTwoSquareRoots(Expression f, String var) throws NotAlgebraicallySolvableException {
        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(f);
        if (summandsLeft.getBound() + summandsRight.getBound() != 2) {
            throw new NotAlgebraicallySolvableException();
        }
        if (f.isSum()) {
            return new RadicalSummandData[]{getFactorAndRadicand(summandsLeft.get(0), var, 2), getFactorAndRadicand(summandsLeft.get(1), var, 2)};
        }
        RadicalSummandData factorAndRadicandRight = getFactorAndRadicand(summandsRight.get(0), var, 2);
        try {
            factorAndRadicandRight.factor = MINUS_ONE.mult(factorAndRadicandRight.factor).simplify();
            return new RadicalSummandData[]{getFactorAndRadicand(summandsLeft.get(0), var, 2), factorAndRadicandRight};
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }
    }

    private static RadicalSummandData[] getFactorsAndRadicandsInCaseOfSumOfOneArbitraryRoot(Expression f, String var) throws NotAlgebraicallySolvableException {
        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(f);
        if (summandsLeft.getBound() + summandsRight.getBound() != 1) {
            throw new NotAlgebraicallySolvableException();
        }
        return new RadicalSummandData[]{getFactorAndRadicand(summandsLeft.get(0), var, null)};
    }

    private static RadicalSummandData[] getFactorsAndRadicandsInCaseOfSumOfSquareRootAndCubicRoot(Expression f, String var) throws NotAlgebraicallySolvableException {
        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(f);
        if (summandsLeft.getBound() + summandsRight.getBound() != 2) {
            throw new NotAlgebraicallySolvableException();
        }
        if (f.isSum()) {
            try {
                return new RadicalSummandData[]{getFactorAndRadicand(summandsLeft.get(0), var, 2), getFactorAndRadicand(summandsLeft.get(1), var, 3)};
            } catch (NotAlgebraicallySolvableException e) {
                return new RadicalSummandData[]{getFactorAndRadicand(summandsLeft.get(1), var, 2), getFactorAndRadicand(summandsLeft.get(0), var, 3)};
            }
        }
        try {
            RadicalSummandData factorAndRadicandRight = getFactorAndRadicand(summandsRight.get(0), var, 2);
            factorAndRadicandRight.factor = MINUS_ONE.mult(factorAndRadicandRight.factor).simplify();
            return new RadicalSummandData[]{factorAndRadicandRight, getFactorAndRadicand(summandsLeft.get(0), var, 3)};
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        } catch (NotAlgebraicallySolvableException e) {
            try {
                RadicalSummandData factorAndRadicandRight = getFactorAndRadicand(summandsRight.get(0), var, 3);
                factorAndRadicandRight.factor = MINUS_ONE.mult(factorAndRadicandRight.factor).simplify();
                return new RadicalSummandData[]{getFactorAndRadicand(summandsLeft.get(0), var, 2), factorAndRadicandRight};
            } catch (EvaluationException ex) {
                throw new NotAlgebraicallySolvableException();
            }
        }
    }

    private static RadicalSummandData[] getFactorsAndRadicandsInCaseOfSumOfTwoCubicRoots(Expression f, String var) throws NotAlgebraicallySolvableException {
        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(f);
        if (summandsLeft.getBound() + summandsRight.getBound() != 2) {
            throw new NotAlgebraicallySolvableException();
        }
        if (f.isSum()) {
            return new RadicalSummandData[]{getFactorAndRadicand(summandsLeft.get(0), var, 3), getFactorAndRadicand(summandsLeft.get(1), var, 3)};
        }
        RadicalSummandData factorAndRadicandRight = getFactorAndRadicand(summandsRight.get(0), var, 2);
        try {
            factorAndRadicandRight.factor = MINUS_ONE.mult(factorAndRadicandRight.factor).simplify();
            return new RadicalSummandData[]{getFactorAndRadicand(summandsLeft.get(0), var, 3), factorAndRadicandRight};
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }
    }

    private static RadicalSummandData getFactorAndRadicand(Expression f, String var, Integer n) throws NotAlgebraicallySolvableException {
        try {
            if (f.isNotProduct() && f.isNotQuotient()) {
                if (n == null) {
                    return new RadicalSummandData(ONE, getRadicandInCaseOfPureRoot(f, var, n).simplify(), getRootDegreeInCaseOfPureRoot(f, var));
                }
                return new RadicalSummandData(ONE, getRadicandInCaseOfPureRoot(f, var, n).simplify(), n);
            }
            if (f.isProduct()) {
                Expression[] separationIntoRationalAndNonRationalFactors = separateIntoRationalAndNonRationalFactors(f, var);
                Expression rationalFactor = separationIntoRationalAndNonRationalFactors[0];
                Expression nonRationalFactor = separationIntoRationalAndNonRationalFactors[1];
                if (n == null) {
                    return new RadicalSummandData(rationalFactor, getRadicandInCaseOfPureRoot(nonRationalFactor, var, n),
                            getRootDegreeInCaseOfPureRoot(nonRationalFactor, var));
                }
                return new RadicalSummandData(rationalFactor, getRadicandInCaseOfPureRoot(nonRationalFactor, var, n), n);
            }
            if (f.isQuotient() && ((BinaryOperation) f).getLeft().isProduct()) {
                Expression[] separationIntoRationalAndNonRationalFactors = separateIntoRationalAndNonRationalFactors(((BinaryOperation) f).getLeft(), var);
                Expression rationalFactor = separationIntoRationalAndNonRationalFactors[0];
                Expression nonRationalFactor = separationIntoRationalAndNonRationalFactors[1];
                if (n == null) {
                    return new RadicalSummandData(rationalFactor.div(((BinaryOperation) f).getRight()),
                            getRadicandInCaseOfPureRoot(nonRationalFactor, var, n),
                            getRootDegreeInCaseOfPureRoot(nonRationalFactor, var));
                }
                return new RadicalSummandData(rationalFactor.div(((BinaryOperation) f).getRight()),
                        getRadicandInCaseOfPureRoot(nonRationalFactor, var, n), n);
            }
        } catch (EvaluationException e) {
        }
        throw new NotAlgebraicallySolvableException();
    }

    private static Expression[] separateIntoRationalAndNonRationalFactors(Expression f, String var) {
        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        ExpressionCollection rationalFactors = new ExpressionCollection();
        ExpressionCollection nonRationalFactors = new ExpressionCollection();
        for (Expression factor : factors) {
            if (SimplifyRationalFunctionUtils.isRationalFunction(factor, var)) {
                rationalFactors.add(factor);
            } else {
                nonRationalFactors.add(factor);
            }
        }
        return new Expression[]{SimplifyUtilities.produceProduct(rationalFactors), SimplifyUtilities.produceProduct(nonRationalFactors)};
    }

    private static Expression getRadicandInCaseOfPureRoot(Expression f, String var, Integer n) throws NotAlgebraicallySolvableException {
        if (n == null) {
            if (f.isPower() && SimplifyRationalFunctionUtils.isRationalFunction(((BinaryOperation) f).getLeft(), var)
                    && ((BinaryOperation) f).getRight().isRationalConstant()) {
                return ((BinaryOperation) f).getLeft().pow(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft());
            }
            throw new NotAlgebraicallySolvableException();
        }
        if (f.isPower() && SimplifyRationalFunctionUtils.isRationalFunction(((BinaryOperation) f).getLeft(), var)
                && ((BinaryOperation) f).getRight().isRationalConstant()
                && ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().equals(new Constant(n))
                && ((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft().isPositiveIntegerConstant()) {
            return ((BinaryOperation) f).getLeft().pow(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft());
        }
        throw new NotAlgebraicallySolvableException();
    }

    private static Expression getRootDegreeInCaseOfPureRoot(Expression f, String var) throws NotAlgebraicallySolvableException {
        if (f.isPower() && SimplifyRationalFunctionUtils.isRationalFunction(((BinaryOperation) f).getLeft(), var)
                && ((BinaryOperation) f).getRight().isRationalConstant()) {
            return ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight();
        }
        throw new NotAlgebraicallySolvableException();
    }

    /**
     * Gibt Lösungen der Gleichung a*g^(1/n) + p = 0 mit rationalen Funktionen
     * a, g, p in var.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallySolvableException
     */
    private static ExpressionCollection solveSumOfOneArbitraryRootEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        Expression[] separation = getSeparationInRadicalsAndNonRadicals(f, var, null);

        RadicalSummandData[] factorsAndRadicands = getFactorsAndRadicandsInCaseOfSumOfOneArbitraryRoot(separation[0], var);
        Expression rightSide = MINUS_ONE.mult(separation[1]).simplify();

        /*
        Die Gleichung a*g(x)^(1/n) = h(x) ist äquivalent zur
        Gleichung h^n - a^n*g = 0.
         */
        Expression a = factorsAndRadicands[0].factor;
        Expression g = factorsAndRadicands[0].radicand;
        Expression n = factorsAndRadicands[0].rootDegree;
        Expression newEquation = rightSide.pow(n).sub(a.pow(n).mult(g));

        ExpressionCollection solutionsOfNewEquation = solveZeroEquation(newEquation, var);

        if (n.isOddIntegerConstant()) {
            return solutionsOfNewEquation;
        }

        ExpressionCollection solutions = new ExpressionCollection();
        Expression valueAtZero;

        // Korrekte Lösungen filtern.
        for (Expression solution : solutionsOfNewEquation) {

            valueAtZero = g.replaceVariable(var, solution).simplify();
            if (!valueAtZero.isAlwaysNonNegative()) {
                continue;
            }

            solutions.add(solution);

        }

        return solutions;

    }

    /**
     * Gibt Lösungen der Gleichung a*g^(1/2) + b*h^(1/2) + p = 0 mit rationalen
     * Funktionen a, b, g, h, p in var.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallySolvableException
     */
    private static ExpressionCollection solveSumOfTwoSquareRootsEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        List<Integer> roots = new ArrayList<>();
        roots.add(2);
        roots.add(2);
        Expression[] separation = getSeparationInRadicalsAndNonRadicals(f, var, roots);

        RadicalSummandData[] factorsAndRadicands = getFactorsAndRadicandsInCaseOfSumOfTwoSquareRoots(separation[0], var);
        Expression rightSide = MINUS_ONE.mult(separation[1]).simplify();

        /*
        Die Gleichung a*g(x)^(1/2) + b*h(x)^(1/2) = p(x) ist äquivalent zur
        Gleichung (p^2 - a^2*g - b^2*h)^2 - 4*a^2*b^2*g*h = 0, bis auf "falsche" Lösungen.
         */
        Expression a = factorsAndRadicands[0].factor;
        Expression b = factorsAndRadicands[1].factor;
        Expression g = factorsAndRadicands[0].radicand;
        Expression h = factorsAndRadicands[1].radicand;
        Expression newEquation = rightSide.pow(2).sub(a.pow(2).mult(g).add(b.pow(2).mult(h))).pow(2).sub(
                new Constant(4).mult(a.pow(2)).mult(b.pow(2)).mult(g).mult(h));

        ExpressionCollection solutionsOfNewEquation = solveZeroEquation(newEquation, var);

        ExpressionCollection solutions = new ExpressionCollection();
        Expression valueAtZero, intermediatePolynomial, productOfFactors;

        // Korrekte Lösungen filtern.
        for (Expression solution : solutionsOfNewEquation) {

            valueAtZero = g.replaceVariable(var, solution).simplify();
            if (!valueAtZero.isAlwaysNonNegative()) {
                continue;
            }
            valueAtZero = h.replaceVariable(var, solution).simplify();
            if (!valueAtZero.isAlwaysNonNegative()) {
                continue;
            }

            intermediatePolynomial = rightSide.pow(2).sub(a.pow(2).mult(g).add(b.pow(2).mult(h))).replaceVariable(var, solution).simplify();
            productOfFactors = a.mult(b).replaceVariable(var, solution).simplify();
            if (!(intermediatePolynomial.isAlwaysNonNegative() && productOfFactors.isAlwaysNonNegative())
                    && !(intermediatePolynomial.isAlwaysNonPositive() && productOfFactors.isAlwaysNonPositive())) {
                continue;
            }

            solutions.add(solution);

        }

        return solutions;

    }

    /**
     * Gibt Lösungen der Gleichung a*g^(1/2) + b*h^(1/3) + p = 0 mit rationalen
     * Funktionen a, b, g, h, p in var.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallySolvableException
     */
    private static ExpressionCollection solveSumOfSquareRootAndCubicRootEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        List<Integer> roots = new ArrayList<>();
        roots.add(2);
        roots.add(3);
        Expression[] separation = getSeparationInRadicalsAndNonRadicals(f, var, roots);

        RadicalSummandData[] factorsAndRadicands = getFactorsAndRadicandsInCaseOfSumOfSquareRootAndCubicRoot(separation[0], var);
        Expression rightSide = MINUS_ONE.mult(separation[1]).simplify();

        Expression a = factorsAndRadicands[0].factor;
        Expression b = factorsAndRadicands[1].factor;
        Expression g = factorsAndRadicands[0].radicand;
        Expression h = factorsAndRadicands[1].radicand;

        /*
        Die Gleichung a*g(x)^(1/2) + b*h(x)^(1/3) = p(x) ist äquivalent zur
        Gleichung (p^3 + 3*a^2*g*p - b^3*h)^2 - g*(3*a*p^2 + a^3*g)^2 = 0, bis auf "falsche" Lösungen.
         */
        Expression newEquation = rightSide.pow(3).add(THREE.mult(a.pow(2)).mult(g).mult(rightSide)).sub(b.pow(3).mult(h)).pow(2).sub(
                g.mult(THREE.mult(a).mult(rightSide.pow(2)).add(a.pow(3).mult(g)).pow(2)));

        ExpressionCollection solutionsOfNewEquation = solveZeroEquation(newEquation, var);

        ExpressionCollection solutions = new ExpressionCollection();
        Expression valueAtZero, intermediatePolynomialOne, intermediatePolynomialTwo;

        // Korrekte Lösungen filtern.
        for (Expression solution : solutionsOfNewEquation) {

            valueAtZero = g.replaceVariable(var, solution).simplify();
            if (!valueAtZero.isAlwaysNonNegative()) {
                continue;
            }

            intermediatePolynomialOne = rightSide.pow(3).add(THREE.mult(a.pow(2)).mult(g).mult(rightSide)).sub(b.pow(3).mult(h)).replaceVariable(var, solution).simplify();
            intermediatePolynomialTwo = THREE.mult(a).mult(rightSide.pow(2)).add(a.pow(3).mult(g)).replaceVariable(var, solution).simplify();
            if (!(intermediatePolynomialOne.isAlwaysNonNegative() && intermediatePolynomialTwo.isAlwaysNonNegative())
                    && !(intermediatePolynomialOne.isAlwaysNonPositive() && intermediatePolynomialTwo.isAlwaysNonPositive())) {
                continue;
            }

            solutions.add(solution);

        }

        return solutions;

    }

    /**
     * Gibt Lösungen der Gleichung a*g^(1/3) + b*h^(1/3) + p = 0 mit rationalen
     * Funktionen a, b, g, h, p in var.
     *
     * @throws EvaluationException
     * @throws NotAlgebraicallySolvableException
     */
    private static ExpressionCollection solveSumOfTwoCubicRootsEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        List<Integer> roots = new ArrayList<>();
        roots.add(3);
        roots.add(3);

        Expression[] separation = getSeparationInRadicalsAndNonRadicals(f, var, roots);

        RadicalSummandData[] factorsAndRadicands = getFactorsAndRadicandsInCaseOfSumOfTwoCubicRoots(separation[0], var);
        Expression rightSide = MINUS_ONE.mult(separation[1]).simplify();

        /*
        Die Gleichung a*g^(1/3) + b*h^(1/3) = p ist äquivalent zur
        Gleichung (p^3 - a^3*g - b^3*h)^3 - 27*a^3*b^3*g*h*p^3 = 0.
         */
        Expression a = factorsAndRadicands[0].factor;
        Expression b = factorsAndRadicands[1].factor;
        Expression g = factorsAndRadicands[0].radicand;
        Expression h = factorsAndRadicands[1].radicand;
        Expression newEquation = rightSide.pow(3).sub(a.pow(3).mult(g).add(b.pow(3).mult(h))).pow(3).sub(
                new Constant(27).mult(a.pow(3)).mult(b.pow(3)).mult(g).mult(h).mult(rightSide.pow(3)));

        return solveZeroEquation(newEquation, var);

    }

}
