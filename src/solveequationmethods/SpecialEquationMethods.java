package solveequationmethods;

import computation.ArithmeticMethods;
import exceptions.EvaluationException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import expressionbuilder.TypeSimplify;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyExponentialRelations;
import expressionsimplifymethods.SimplifyRationalFunctionMethods;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import substitutionmethods.SubstitutionUtilities;

public abstract class SpecialEquationMethods {

    private static final HashSet<TypeSimplify> simplifyTypesRationalExponentialEquation = getSimplifyTypesRationalExponentialEquation();
    private static final HashSet<TypeSimplify> simplifyTypesRationalTrigonometricalEquation = getSimplifyTypesRationalTrigonometricalEquation();

    private static HashSet<TypeSimplify> getSimplifyTypesRationalExponentialEquation() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_factorize_in_sums);
        simplifyTypes.add(TypeSimplify.simplify_factorize_in_differences);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
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
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_expand_moderate);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_multiply_exponents);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals_in_sums);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals_in_differences);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypes.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
        simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypes.add(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions);
        return simplifyTypes;
    }

    /**
     * Hauptmethode zum Lösen von Exponentialgleichungen f = 0. Ist f keine
     * Exponentialgleichung, so wird eine leere ExpressionCollection
     * zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveExponentialEquation(Expression f, String var) throws EvaluationException {

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

        BigInteger gcdOfEnumerators = BigInteger.ONE;
        BigInteger lcmOfDenominators = BigInteger.ONE;

        Iterator<Expression> iter = argumentsInExp.iterator();
        Expression firstFactorOfArgument = iter.next();
        Expression currentQuotient;

        while (iter.hasNext()) {
            currentQuotient = iter.next().div(firstFactorOfArgument).simplify();
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
        Expression substitution = new Constant(gcdOfEnumerators).mult(firstFactorOfArgument).div(lcmOfDenominators).exp().simplify();

        Object fSubstituted = SubstitutionUtilities.substitute(f, var, substitution, true);
        if (fSubstituted instanceof Expression) {

            String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
            ExpressionCollection zerosOfSubstitutedEquation = SolveMethods.solveZeroEquation((Expression) fSubstituted, substVar);
            zeros = new ExpressionCollection();
            ExpressionCollection currentZeros;
            // Rücksubstitution.
            for (int i = 0; i < zerosOfSubstitutedEquation.getBound(); i++) {
                try {
                    currentZeros = SolveMethods.solveGeneralEquation(substitution, zerosOfSubstitutedEquation.get(i), var);
                    for (int j = 0; j < currentZeros.getBound(); j++) {
                        /*
                         Für die Vereinfachung der Lösungen sollen HIER Logarithmen auseinandergezogen werden, 
                         da es für diesen Zweck besser ist.
                         */
                        currentZeros.put(j, currentZeros.get(j).simplify(simplifyTypesRationalExponentialEquation));
                    }
                    // Lösungen hinzufügen.
                    zeros.add(currentZeros);
                } catch (EvaluationException e) {
                    /*
                     Dann ist zerosOfSubstitutedEquation.get(i) eine ungültige
                     Lösung -> zerosOfSubstitutedEquation.get(i) nicht in die
                     Lösungen mitaufnehmen.
                     */
                }
            }

        }

        return zeros;

    }

    /**
     * Hauptmethode zum Lösen von trigonometrischen Gleichungen f = 0. Ist f
     * keine trigonometrische Gleichung, so wird eine leere ExpressionCollection
     * zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveTrigonometricalEquation(Expression f, String var) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();
        HashSet<Expression> argumentsInTrigonometricFunctions = new HashSet();
        /*
         Falls f keine rationale Funktion in einer Exponentialfunktion ist (1.
         Abfrage), oder falls f konstant bzgl. var ist (2. Abfrage), dann
         werden keine Lösungen ermittelt (diese Methode ist dafür nicht
         zuständig).
         */
        if (!SimplifyRationalFunctionMethods.isRationalFunktionInTrigonometricalFunctions(f, var, argumentsInTrigonometricFunctions) || argumentsInTrigonometricFunctions.isEmpty()) {
            return zeros;
        }

        BigInteger gcdOfEnumerators = BigInteger.ONE;
        BigInteger lcmOfDenominators = BigInteger.ONE;

        Iterator<Expression> iter = argumentsInTrigonometricFunctions.iterator();
        Expression firstFactorOfArgument = iter.next();
        Expression currentQuotient;

        while (iter.hasNext()) {
            currentQuotient = iter.next().div(firstFactorOfArgument).simplify();
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

        /* 
         Das ist die eigentliche Substitution. BEISPIEL: f = sin(4*a*x/7) + 4*cos(6*a*x/7).
         Dann substitution = 2*a*x/7. Die substitutierte Gleichung lautet demnach: 
         f = sin(2*X_1) + 4*cos(3*X_1). X_1 ist dabei der Ausdruck substitution.
         */
        Expression substitution = new Constant(gcdOfEnumerators).mult(firstFactorOfArgument).div(lcmOfDenominators).simplify();

        Object fSubstitutedAsObject = SubstitutionUtilities.substitute(f, var, substitution, true);
        if (fSubstitutedAsObject instanceof Expression) {

            Expression fSubstituted = (Expression) fSubstitutedAsObject;
            String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
            fSubstituted = fSubstituted.simplify(simplifyTypesRationalTrigonometricalEquation);
            /*
             Das Folgende ist eine Sicherheitsabfrage: Die substituierte Gleichung sollte vom 
             folgenden Typ sein: Alle Argumente, die in trigonometrischen Funktionen vorkommen,
             müssen von der Form n*x sein, wobei n eine ganze Zahl und x eine Variable ist.
             */
            if (!SimplifyRationalFunctionMethods.doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable(fSubstituted, substVar)) {
                return new ExpressionCollection();
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
             WICHTIG:
             */
            Expression fNew = substituteInTrigonometricalEquationSinByCos(fSubstituted).simplify(simplifyTypesRationalTrigonometricalEquation);

            String polynomVar = SubstitutionUtilities.getSubstitutionVariable(fNew);
            if (SimplifyRationalFunctionMethods.isRationalFunctionIn(Variable.create(substVar).cos(), fNew, substVar)) {
                Expression trigonometricalSubst = Variable.create(substVar).cos();
                Object polynomial = SubstitutionUtilities.substitute(fNew, substVar, trigonometricalSubst, true);

                // Sicherheitsabfrage. Sollte immer true sein.
                if (!(polynomial instanceof Expression)) {
                    return new ExpressionCollection();
                }

                ExpressionCollection zerosOfSubstitutedEquation = SolveMethods.solveZeroEquation((Expression) polynomial, polynomVar);
                zeros = new ExpressionCollection();

                // Rücksubstitution.
                for (int i = 0; i < zerosOfSubstitutedEquation.getBound(); i++) {
                    try {
                        zeros.add(SolveMethods.solveGeneralEquation(substitution.cos(), zerosOfSubstitutedEquation.get(i), var));
                    } catch (EvaluationException e) {
                        /*
                         Dann ist zerosOfSubstitutedEquation.get(i) eine ungültige
                         Lösung -> zerosOfSubstitutedEquation.get(i) nicht in die
                         Lösungen mitaufnehmen.
                         */
                    }
                }

            } else {
                fNew = substituteInTrigonometricalEquationCosBySin(fSubstituted).simplify(simplifyTypesRationalTrigonometricalEquation);
                if (SimplifyRationalFunctionMethods.isRationalFunctionIn(Variable.create(substVar).sin(), fNew, substVar)) {
                    Expression trigonometricalSubst = Variable.create(substVar).sin();
                    Object polynomial = SubstitutionUtilities.substitute(fNew, substVar, trigonometricalSubst, true);

                    // Sicherheitsabfrage. Sollte immer true sein.
                    if (!(polynomial instanceof Expression)) {
                        return new ExpressionCollection();
                    }

                    ExpressionCollection zerosOfSubstitutedEquation = SolveMethods.solveZeroEquation((Expression) polynomial, polynomVar);
                    zeros = new ExpressionCollection();
                    // Rücksubstitution.
                    for (int i = 0; i < zerosOfSubstitutedEquation.getBound(); i++) {
                        try {
                            zeros.add(SolveMethods.solveGeneralEquation(substitution.sin(), zerosOfSubstitutedEquation.get(i), var));
                        } catch (EvaluationException e) {
                            /*
                             Dann ist zerosOfSubstitutedEquation.get(i) eine ungültige
                             Lösung -> zerosOfSubstitutedEquation.get(i) nicht in die
                             Lösungen mitaufnehmen.
                             */
                        }
                    }

                }
            }

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
                    && ((BinaryOperation) f).getRight().isEvenConstant()) {
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
                    && ((BinaryOperation) f).getRight().isEvenConstant()) {
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

}
