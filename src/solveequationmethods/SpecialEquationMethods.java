package solveequationmethods;

import computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import expressionbuilder.TypeSimplify;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyUtilities;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import substitutionmethods.SubstitutionUtilities;

public abstract class SpecialEquationMethods {

    // Allgemeine Hilfsmethoden um festzustellen, ob ein HashSet von Expressions
    // nur Ausdrücke enthält, deren paarweise Quotienten rational sind.
    /**
     * Hilfsmethode. Gibt zurück, ob alle Verhältnisse von Ausdrücken in terms
     * und expr rational sind.
     */
    private static boolean areQuotientsRational(Expression expr, HashSet<Expression> terms) {

        try {
            Iterator iter = terms.iterator();
            while (iter.hasNext()) {
                if (!((Expression) iter.next()).div(expr).simplify().isIntegerConstantOrRationalConstant()) {
                    return false;
                }
            }
            return true;
        } catch (EvaluationException e) {
            return false;
        }

    }

    /**
     * Hilfsmethode. Gibt zurück, ob alle paarweisen Verhältnisse von Ausdrücken
     * in terms rational sind.
     */
    private static boolean areQuotientsOfTermsRational(HashSet<Expression> terms) {

        Iterator iter = terms.iterator();
        Expression expr;
        while (iter.hasNext()) {
            expr = (Expression) iter.next();
            if (!expr.equals(Expression.ZERO)) {
                return areQuotientsRational(expr, terms);
            }
        }
        // Dies kann nur eintreten, falls alle Elemente von terms gleich ZERO sind.
        return true;

    }

    /**
     * Entscheidet, ob f bzgl. der Variablen var eine rationale Funktion in
     * einer Exponentialfunktion ist. Beispielsweise wird bei f = exp(2*x) +
     * 5*exp(3*x) true zurückgegeben, jedoch false bei f = exp(x) +
     * exp(2^(1/2)*x).
     */
    public static boolean isRationalFunktionInExp(Expression f, String var, HashSet<Expression> argumentsInExp) {

        if (!f.contains(var)) {
            return true;
        }
        if (f instanceof Constant) {
            return true;
        }
        if (f instanceof Variable) {
            return !((Variable) f).getName().equals(var);
        }
        if (f instanceof BinaryOperation) {

            if (f.isPower()) {

                if (!((BinaryOperation) f).getLeft().contains(var)) {
                    // a^f(x), a von x unabhängig, wird umgeformt zu exp(ln(a)*f(x)).
                    Function fAsExp = ((BinaryOperation) f).getLeft().ln().mult(((BinaryOperation) f).getRight()).exp();
                    return isRationalFunktionInExp(fAsExp, var, argumentsInExp);
                }
                /* 
                 Sonstiger Fall: f(x)^g(x) ist nur dann eine rationale Exponentialgleichung, wenn f(x)
                 eine ist und wenn g(x) eine konstante ganze Zahl ist.
                 */
                return isRationalFunktionInExp(((BinaryOperation) f).getLeft(), var, argumentsInExp)
                        && ((BinaryOperation) f).getRight().isIntegerConstant();

            }
            return isRationalFunktionInExp(((BinaryOperation) f).getLeft(), var, argumentsInExp)
                    && isRationalFunktionInExp(((BinaryOperation) f).getRight(), var, argumentsInExp)
                    && areQuotientsOfTermsRational(argumentsInExp);

        }
        if (f instanceof Function) {

            if (!((Function) f).getType().equals(TypeFunction.exp)) {
                return false;
            }

            Expression argumentOfExp = ((Function) f).getLeft();
            if (areQuotientsRational(argumentOfExp, argumentsInExp)) {
                argumentsInExp.add(argumentOfExp);
                return true;
            }
            return false;

        }

        /*
         Falls f eine Instanz von Operator oder SelfDefinedFunction ist, in
         welchem var vorkommt, dann false zurückgeben.
         */
        return !f.contains(var);

    }

    /**
     * Entscheidet, ob f bzgl. der Variablen var eine algebraische Funktion in
     * den trigonometrischen Funktionen sin und cos ist. Beispielsweise wird bei
     * f = sin(2*x) + 5*cos(3*x) true zurückgegeben, jedoch false bei f = sin(x)
     * + cos(2^(1/2)*x).
     */
    public static boolean isRationalFunktionInTrigonometricalFunctions(Expression f, String var, HashSet<Expression> argumentsInTrigonometricFunctions) {

        if (!f.contains(var)) {
            return true;
        }
        if (f instanceof Constant) {
            return true;
        }
        if (f instanceof Variable) {
            return !((Variable) f).getName().equals(var);
        }
        if (f instanceof BinaryOperation) {

            if (f.isPower()) {
                return isRationalFunktionInTrigonometricalFunctions(((BinaryOperation) f).getLeft(), var, argumentsInTrigonometricFunctions)
                        && ((BinaryOperation) f).getRight().isIntegerConstant();
            }
            return isRationalFunktionInTrigonometricalFunctions(((BinaryOperation) f).getLeft(), var, argumentsInTrigonometricFunctions)
                    && isRationalFunktionInTrigonometricalFunctions(((BinaryOperation) f).getRight(), var, argumentsInTrigonometricFunctions)
                    && areQuotientsOfTermsRational(argumentsInTrigonometricFunctions);

        }
        if (f instanceof Function) {

            if (!((Function) f).getType().equals(TypeFunction.sin)
                    && !((Function) f).getType().equals(TypeFunction.cos)
                    && !((Function) f).getType().equals(TypeFunction.tan)
                    && !((Function) f).getType().equals(TypeFunction.cot)
                    && !((Function) f).getType().equals(TypeFunction.sec)
                    && !((Function) f).getType().equals(TypeFunction.cosec)) {
                return false;
            }

            Expression argumentOfExp = ((Function) f).getLeft();
            if (areQuotientsRational(argumentOfExp, argumentsInTrigonometricFunctions)) {
                argumentsInTrigonometricFunctions.add(argumentOfExp);
                return true;
            }
            return false;
            
        }

        /*
         Falls f eine Instanz von Operator oder SelfDefinedFunction ist, in
         welchem var vorkommt, dann false zurückgeben.
         */
        return !f.contains(var);

    }

    private static boolean doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable(Expression f, String var) {

        if (!f.contains(var) || f instanceof Variable) {
            return true;
        }

        if (f instanceof BinaryOperation) {
            return doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable(((BinaryOperation) f).getLeft(), var)
                    && doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable(((BinaryOperation) f).getRight(), var);
        }

        if (f.isFunction(TypeFunction.sin) || f.isFunction(TypeFunction.cos)
                || f.isFunction(TypeFunction.tan) || f.isFunction(TypeFunction.cot)
                || f.isFunction(TypeFunction.sec) || f.isFunction(TypeFunction.cosec)) {
            Expression argument = ((Function) f).getLeft();
            /*
             Ohne Einschränkung kann man annehmen, dass wenn argument ein ganzes Vielfaches
             von var ist, dass dann das Vielfache links steht.
             */
            return !argument.contains(var) || argument.equals(Variable.create(var))
                    || argument.isProduct() && ((BinaryOperation) argument).getLeft().isIntegerConstant()
                    && ((BinaryOperation) argument).getRight().equals(Variable.create(var));
        }

        return false;

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
        f = separateConstantPartsInRationalExponentialEquations(f, var);

        /*
         Falls f keine rationale Funktion in einer Exponentialfunktion ist (1.
         Abfrage), oder falls f konstant bzgl. var ist (2. Abfrage), dann
         werden keine Lösungen ermittelt (diese Methode ist dafür nicht
         zuständig).
         */
        if (!isRationalFunktionInExp(f, var, argumentsInExp) || argumentsInExp.isEmpty()) {
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
            /*
             Für die Vereinfachung der Lösungen sollen HIER Logarithmen auseinandergezogen werden, 
             da es für diesen Zweck besser ist.
             */
            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.simplify_trivial);
            simplifyTypes.add(TypeSimplify.order_difference_and_division);
            simplifyTypes.add(TypeSimplify.simplify_powers);
            simplifyTypes.add(TypeSimplify.collect_products);
            simplifyTypes.add(TypeSimplify.factorize_in_sums);
            simplifyTypes.add(TypeSimplify.factorize_in_differences);
            simplifyTypes.add(TypeSimplify.reduce_quotients);
            simplifyTypes.add(TypeSimplify.reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_expand_and_collect_equivalents_if_shorter);
            simplifyTypes.add(TypeSimplify.simplify_functional_relations);
            simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
            simplifyTypes.add(TypeSimplify.order_sums_and_products);
            for (int i = 0; i < zerosOfSubstitutedEquation.getBound(); i++) {
                try {
                    currentZeros = SolveMethods.solveGeneralEquation(substitution, zerosOfSubstitutedEquation.get(i), var);
                    for (int j = 0; j < currentZeros.getBound(); j++) {
                        currentZeros.put(j, currentZeros.get(j).simplify(simplifyTypes));
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
     * Gibt einen Ausdruck zurück, indem in Exponentialfunktionen bzgl. var der
     * von var abhängige Teil von dem von var unabhängigen Teil getrennt wurde.
     * BEISPIEL: Für f = 3 + 2^(x+7) - x^2*exp(8 - sin(x)) wird 3 + 2^7*2^x -
     * x^2*exp(8)*exp(-sin(x)) zurückgegeben.
     */
    public static Expression separateConstantPartsInRationalExponentialEquations(Expression f, String var) {

        // Im Folgenden sei x = var;
        if (!f.contains(var) || f instanceof Constant || f instanceof Variable) {
            return f;
        }
        if (f instanceof BinaryOperation) {

            if (f.isPower() && !((BinaryOperation) f).getLeft().contains(var)) {

                // a^(c + f(x)), a und c von x unabhängig, wird umgeformt zu a^c*a^f(x).
                Expression base = ((BinaryOperation) f).getLeft();
                ExpressionCollection summandsLeftConstant = SimplifyUtilities.getConstantSummandsLeftInExpression(((BinaryOperation) f).getRight(), var);
                ExpressionCollection summandsLeftNonConstant = SimplifyUtilities.getNonConstantSummandsLeftInExpression(((BinaryOperation) f).getRight(), var);
                ExpressionCollection summandsRightConstant = SimplifyUtilities.getConstantSummandsRightInExpression(((BinaryOperation) f).getRight(), var);
                ExpressionCollection summandsRightNonConstant = SimplifyUtilities.getNonConstantSummandsRightInExpression(((BinaryOperation) f).getRight(), var);
                Expression exponentLeft = SimplifyUtilities.produceDifference(summandsLeftConstant, summandsRightConstant);
                if (exponentLeft.equals(Expression.ZERO)) {
                    return f;
                }
                return base.pow(SimplifyUtilities.produceDifference(summandsLeftConstant, summandsRightConstant)).mult(
                        base.pow(SimplifyUtilities.produceDifference(summandsLeftNonConstant, summandsRightNonConstant)));

            }
            return new BinaryOperation(
                    separateConstantPartsInRationalExponentialEquations(((BinaryOperation) f).getLeft(), var),
                    separateConstantPartsInRationalExponentialEquations(((BinaryOperation) f).getRight(), var),
                    ((BinaryOperation) f).getType());

        }
        if (f instanceof Function) {

            if (!((Function) f).getType().equals(TypeFunction.exp)) {
                return new Function(separateConstantPartsInRationalExponentialEquations(((Function) f).getLeft(), var),
                        ((Function) f).getType());
            }

            Expression argumentOfExp = ((Function) f).getLeft();
            // exp(c + f(x)), c von x unabhängig, wird umgeformt zu exp(c)*exp(f(x)).
            ExpressionCollection summandsLeftConstant = SimplifyUtilities.getConstantSummandsLeftInExpression(argumentOfExp, var);
            ExpressionCollection summandsLeftNonConstant = SimplifyUtilities.getNonConstantSummandsLeftInExpression(argumentOfExp, var);
            ExpressionCollection summandsRightConstant = SimplifyUtilities.getConstantSummandsRightInExpression(argumentOfExp, var);
            ExpressionCollection summandsRightNonConstant = SimplifyUtilities.getNonConstantSummandsRightInExpression(argumentOfExp, var);
            Expression exponentLeft = SimplifyUtilities.produceDifference(summandsLeftConstant, summandsRightConstant);
            if (exponentLeft.equals(Expression.ZERO)) {
                return f;
            }
            return SimplifyUtilities.produceDifference(summandsLeftConstant, summandsRightConstant).exp().mult(
                    SimplifyUtilities.produceDifference(summandsLeftNonConstant, summandsRightNonConstant).exp());

        }

        /*
         Falls f eine Instanz von Operator oder SelfDefinedFunction ist, in
         welchem var vorkommt, dann false zurückgeben.
         */
        return f;

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
        if (!isRationalFunktionInTrigonometricalFunctions(f, var, argumentsInTrigonometricFunctions) || argumentsInTrigonometricFunctions.isEmpty()) {
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

            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.order_difference_and_division);
            simplifyTypes.add(TypeSimplify.order_sums_and_products);
            simplifyTypes.add(TypeSimplify.simplify_trivial);
            simplifyTypes.add(TypeSimplify.collect_products);
            simplifyTypes.add(TypeSimplify.expand);
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

            fSubstituted = fSubstituted.simplify(simplifyTypes);
            /*
             Das Folgende ist eine Sicherheitsabfrage: Die substituierte Gleichung sollte vom 
             folgenden Typ sein: Alle Argumente, die in trigonometrischen Funktionen vorkommen,
             müssen von der Form n*x sein, wobei n eine ganze Zahl und x eine Variable ist.
             */
            if (!doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable(fSubstituted, substVar)) {
                return new ExpressionCollection();
            }

            /*
             Nun ist fSubstituted eine rationale Funtion in sin(n*X_1) und cos(m*X_1), X_1 = substVar.
             Jetzt muss fSubstituted als rationale Function in sin(X_1) und cos(X_1) dargestellt werden.
             WICHTIG: Beim Vereinfachen darf hier nicht simplifyFunctionalRelations() verwendet werden,
             da dann beispielsweise sin(x)*cos(x) wieder zu sin(2*x)/2 vereinfacht wird.
             */
            fSubstituted = expandRationalFunctionInTrigonometricalFunctions(fSubstituted, substVar).simplify(simplifyTypes);

            /*
             Jetzt werden zwei Versuche unternommen: (1) Sinusausdrücke durch Cosinusausdrücke zu ersetzen.
             (2) Cosinusausdrücke durch Sinusausdrücke zu ersetzen. Danach wird jeweils geprüft, ob 
             der so entstandene Ausdruck ein Polynom in einem Cosinus- oder einem Sinusausdruck ist.
             WICHTIG:
             */
            Expression fNew = substituteInTrigonometricalEquationSinByCos(fSubstituted).simplify(simplifyTypes);

            String polynomVar = SubstitutionUtilities.getSubstitutionVariable(fNew);
            if (isRationalFunctionIn(Variable.create(substVar).cos(), fNew, substVar)) {
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
                fNew = substituteInTrigonometricalEquationCosBySin(fSubstituted).simplify(simplifyTypes);
                if (isRationalFunctionIn(Variable.create(substVar).sin(), fNew, substVar)) {
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
     * Hier beginnen Methoden und Hilfsmethoden für die Lösung spezieller
     * trigonometrischer Gleichungen.
     */
    /**
     * Hilfsmethode. Gibt n! zurück, falls n >= 0 ist, sonst 0.
     */
    private static BigInteger fac(int n) {
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    /**
     * Hilfsmethode. Gibt n!/(k! * (n - k)!) zurück, falls 0 <= k <= n, sonst 0.
     */
    private static BigInteger binCoefficient(int n, int k) {
        if (k < 0 || k > n) {
            return BigInteger.ZERO;
        }
        return fac(n).divide(fac(k).multiply(fac(n - k)));
    }

    /**
     * Sei x = var. Diese Methode gibt sin(n*x) zurück.
     */
    private static Expression getSinOfMultipleArgument(String var, int n) {

        if (n == 0) {
            return Expression.ZERO;
        } else if (n < 0) {
            return Expression.MINUS_ONE.mult(getSinOfMultipleArgument(var, -n));
        }

        // Ab hier ist n > 0.
        Expression sinOfArgument = Variable.create(var).sin();
        Expression cosOfArgument = Variable.create(var).cos();
        Expression result = Expression.ZERO;

        for (int i = 1; i <= n; i++) {
            if (i % 2 == 0) {
                continue;
            }
            if (i == 1) {
                result = new Constant(n).mult(sinOfArgument.mult(cosOfArgument.pow(n - 1)));
            } else {
                if (i % 4 == 1) {
                    result = result.add(new Constant(binCoefficient(n, i)).mult(sinOfArgument.pow(i).mult(cosOfArgument.pow(n - i))));
                } else {
                    result = result.sub(new Constant(binCoefficient(n, i)).mult(sinOfArgument.pow(i).mult(cosOfArgument.pow(n - i))));
                }
            }
        }

        return result;

    }

    /**
     * Sei x = var. Diese Methode gibt cos(n*x) zurück.
     */
    private static Expression getCosOfMultipleArgument(String var, int n) {

        if (n == 0) {
            return Expression.ONE;
        } else if (n < 0) {
            return getCosOfMultipleArgument(var, -n);
        }

        // Ab hier ist n > 0.
        Expression sinOfArgument = Variable.create(var).sin();
        Expression cosOfArgument = Variable.create(var).cos();
        Expression result = Expression.ZERO;

        for (int i = 0; i <= n; i++) {
            if (i % 2 == 1) {
                continue;
            }
            if (i == 0) {
                result = cosOfArgument.pow(n);
            } else {
                if (i % 4 == 0) {
                    result = result.add(new Constant(binCoefficient(n, i)).mult(sinOfArgument.pow(i).mult(cosOfArgument.pow(n - i))));
                } else {
                    result = result.sub(new Constant(binCoefficient(n, i)).mult(sinOfArgument.pow(i).mult(cosOfArgument.pow(n - i))));
                }
            }
        }

        return result;

    }

    /**
     * Ersetzt in einem Ausdruck gerade Potenzen vom Sinus durch
     * Cosinus-Ausdrücke. BEISPIELS: sin(x)^4 wird durch (1 - cos(x)^2)^2
     * ersetzt.
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
     * Sinus-Ausdrücke. BEISPIELS: cos(x)^6 wird durch (1 - sin(x)^2)^3 ersetzt.
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

    /**
     * Hilfsmethode. Gibt zurück, ob g ein Polynomin f ist.
     */
    private static boolean isRationalFunctionIn(Expression f, Expression g, String var) {

        if (!g.contains(var)) {
            return true;
        }

        if (g.equivalent(f)) {
            return true;
        }

        if (g instanceof BinaryOperation) {
            if (g.isNotPower()) {
                return isRationalFunctionIn(f, ((BinaryOperation) g).getLeft(), var)
                        && isRationalFunctionIn(f, ((BinaryOperation) g).getRight(), var);
            } else if (g.isPower() && ((BinaryOperation) g).getRight().isIntegerConstant()
                    && ((BinaryOperation) g).getRight().isNonNegative()) {
                return isRationalFunctionIn(f, ((BinaryOperation) g).getLeft(), var);
            }
        }

        /*
         Falls f eine Instanz von Operator oder SelfDefinedFunction ist, in
         welchem var vorkommt, dann false zurückgeben.
         */
        return false;

    }

    /**
     * Ersetzt in einer rationalen Funktion in trigonometrischen Funktionen
     * Ausdrücke cos(n*x) und sin(m*x) durch die Ausdrücke sin(x) und cos(x).
     * VORAUSSETZUNG: f ist eine rationale Funktion in trigonometrischen
     * Funktionen (in der Variablen var). Ferner muss m und n im Integerbereich
     * liegen.
     */
    private static Expression expandRationalFunctionInTrigonometricalFunctions(Expression f, String var) {

        if (f instanceof BinaryOperation) {
            return new BinaryOperation(expandRationalFunctionInTrigonometricalFunctions(((BinaryOperation) f).getLeft(), var),
                    expandRationalFunctionInTrigonometricalFunctions(((BinaryOperation) f).getRight(), var),
                    ((BinaryOperation) f).getType());
        }

        if (f.isFunction() && f.contains(var)) {
            Expression argument = ((Function) f).getLeft();
            if (f.isFunction(TypeFunction.sin) || f.isFunction(TypeFunction.cos)
                    && (argument.equals(Variable.create(var)) || argument.isProduct()
                    && ((BinaryOperation) argument).getLeft().isIntegerConstant()
                    && ((BinaryOperation) argument).getRight().equals(Variable.create(var)))) {

                /*
                 Es werden nur Ausdrücke der Form sin(n*x) oder cos(n*x) vereinfach, wenn
                 |n| <= maximaler Grad eines Polynoms für das Lösen von Polynomgleichungen
                 gilt. GRUND: sin(n*x) und cos(n*x) werden zu Polynomen vom Grad n in
                 sin(x) und cos(x). 
                 */
                int n = 1;
                if (argument.isProduct()) {
                    if (((Constant) ((BinaryOperation) argument).getLeft()).getValue().toBigInteger().abs().compareTo(
                            BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                        return f;
                    }
                    n = ((Constant) ((BinaryOperation) argument).getLeft()).getValue().toBigInteger().intValue();
                    if (Math.abs(n) > ComputationBounds.BOUND_DEGREE_OF_POLYNOMIAL_FOR_SOLVING_EQUATION) {
                        return f;
                    }
                }

                if (f.isFunction(TypeFunction.sin)) {
                    return getSinOfMultipleArgument(var, n);
                } else if (f.isFunction(TypeFunction.cos)) {
                    return getCosOfMultipleArgument(var, n);
                }

            }
        }

        return f;

    }

}
