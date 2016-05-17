package abstractexpressions.expression.integration;

import abstractexpressions.expression.computation.ArithmeticMethods;
import exceptions.EvaluationException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.TypeOperator;
import enums.TypeSimplify;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.substitution.SubstitutionUtilities;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyBinaryOperationMethods;
import abstractexpressions.expression.equation.SolveGeneralEquationMethods;
import abstractexpressions.expression.utilities.SimplifyPolynomialMethods;
import abstractexpressions.expression.utilities.SimplifyRationalFunctionMethods;
import abstractexpressions.expression.utilities.SimplifyUtilities;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import exceptions.NotAlgebraicallyIntegrableException;
import exceptions.NotAlgebraicallySolvableException;
import java.math.BigInteger;
import java.util.HashSet;
import notations.NotationLoader;

public abstract class RischAlgorithmMethods extends GeneralIntegralMethods {

    private static final HashSet<TypeSimplify> simplifyTypesForDifferentialFieldExtension = getSimplifyTypesForDifferentialFieldExtensions();
    private static final HashSet<TypeSimplify> simplifyTypesRischAlgorithmPolynomialPart = getSimplifyTypesRischAlgorithmPolynomialPart();

    private static HashSet<TypeSimplify> getSimplifyTypesForDifferentialFieldExtensions() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients);
        simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
        simplifyTypes.add(TypeSimplify.simplify_replace_exponential_functions_with_respect_to_variable_by_definitions);
        return simplifyTypes;
    }

    private static HashSet<TypeSimplify> getSimplifyTypesRischAlgorithmPolynomialPart() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_factorize);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
        return simplifyTypes;
    }

    /**
     * Gibt zurück, ob transzendente Erweiterungen eine Standardform besitzen.
     * Für exponentielle Erweiterungen t = exp(f(x)) muss gelten, dass f(x)
     * keine konstanten nichttrivialen Summanden besitzt, für logarithmische
     * Erweiterungen t = ln(f(x)) muss gelten, dass f(x) keine konstanten
     * nichttrivialen Faktoren besitzt.
     */
    private static boolean areFieldExtensionsInCorrectForm(ExpressionCollection fieldGenerators, String var) {
        for (Expression fieldExtension : fieldGenerators) {
            if (fieldExtension.isFunction(TypeFunction.exp)) {
                ExpressionCollection constantSummandsLeft = SimplifyUtilities.getConstantSummandsLeftInExpression(fieldExtension, var);
                ExpressionCollection constantSummandsRight = SimplifyUtilities.getConstantSummandsRightInExpression(fieldExtension, var);
                return constantSummandsLeft.isEmpty() && constantSummandsRight.isEmpty();
            }
            if (fieldExtension.isFunction(TypeFunction.ln)) {
                ExpressionCollection constantFactorsNumerator = SimplifyUtilities.getConstantFactorsOfNumeratorInExpression(fieldExtension, var);
                ExpressionCollection constantFactorsDenominator = SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(fieldExtension, var);
                return constantFactorsNumerator.isEmpty() && constantFactorsDenominator.isEmpty();
            }
            if (!fieldExtension.isFunction(TypeFunction.exp) && !fieldExtension.isFunction(TypeFunction.ln)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sei x = var. Diese Methode gibt zurück, ob f algebraisch über dem Körper
     * R(x, t_1, ..., t_n) ist, wobei t_1, ..., t_n die Elemente von
     * fieldGenerators sind.<br>
     * VORAUSSETZUNGEN: (1) f enthält keine algebraischen Ausdrücke (also keine
     * Ausdrücke der Form (...)^(p/q) mit rationalem und nicht-ganzem p/q)<br>
     * (2) f ist so weit, wie es geht vereinfacht (d.h. f enthält nicht
     * Ausdrücke wie exp(ln(...)) o. ä.).<br>
     * (3) fieldGenerators darf nur Ausdrücke der Form exp(...) oder ln(...)
     * enthalten, also keine Summen, Differenzen etc. Die Methode
     * areFieldExtensionsInCorrectForm(fieldGenerators) muss also true
     * zurückgeben.<br>
     * BEISPIEL: (1) f = exp(x+2), var = "x", fieldGenerators = {exp(x)}. Hier
     * wird true zurückgegeben.<br>
     * (2) f = ln(exp(x+2)+x^2)+x^3/7, var = "x", fieldGenerators = {exp(x)}.
     * Hier wird false zurückgegeben.<br>
     * (3) f = ln(x)+x!, var = "x", fieldGenerators = {ln(x), exp(x)}. Hier wird
     * false zurückgegeben (aufgrund des Summanden x!, welcher transzendent über
     * der angegebenen Körpererweiterung ist).<br>
     */
    public static boolean isFunctionRationalOverDifferentialField(Expression f, String var, ExpressionCollection fieldGenerators) {

        if (!areFieldExtensionsInCorrectForm(fieldGenerators, var)) {
            // Schlechter Fall, da fieldExtensions nicht in die korrekte Form besitzt.
            return false;
        }
        // Weitestgehend vereinfachen, wenn möglich.
        try {
            f = f.simplify(simplifyTypesForDifferentialFieldExtension, var);
        } catch (EvaluationException e) {
            return false;
        }
        return isRationalOverDifferentialField(f, var, fieldGenerators);

    }

    /**
     * Sei x = var. Diese Hilfsmethode gibt zurück, ob f algebraisch über dem
     * Körper R(x, t_1, ..., t_n) ist, wobei t_1, ..., t_n die Elemente von
     * fieldGenerators sind.<br>
     * VORAUSSETZUNGEN: (1) f enthält keine algebraischen Ausdrücke (also keine
     * Ausdrücke der Form (...)^(p/q) mit rationalem und nicht-ganzem p/q)<br>
     * (2) f ist so weit, wie es geht vereinfacht (d.h. f enthält nicht
     * Ausdrücke wie exp(ln(...)) o. ä.).<br>
     * (3) fieldGenerators darf nur Ausdrücke der Form exp(...) oder ln(...)
     * enthalten, also keine Summen, Differenzen etc. Die Methode
     * areFieldExtensionsInCorrectForm(fieldGenerators) muss also true
     * zurückgeben.<br>
     * BEISPIEL: (1) f = exp(x+2), var = "x", fieldGenerators = {exp(x)}. Hier
     * wird true zurückgegeben.<br>
     * (2) f = ln(exp(x+2)+x^2)+x^3/7, var = "x", fieldGenerators = {exp(x)}.
     * Hier wird false zurückgegeben.<br>
     * (3) f = ln(x)+x!, var = "x", fieldGenerators = {ln(x), exp(x)}. Hier wird
     * false zurückgegeben (aufgrund des Summanden x!, welcher transzendent über
     * der angegebenen Körpererweiterung ist).<br>
     */
    private static boolean isRationalOverDifferentialField(Expression f, String var, ExpressionCollection fieldGenerators) {

        if (fieldGenerators.containsExquivalent(f)) {
            return true;
        }

        if (!f.contains(var) || f.equals(Variable.create(var))) {
            return true;
        }
        if (f instanceof BinaryOperation) {
            if (f.isNotPower()) {
                return isRationalOverDifferentialField(((BinaryOperation) f).getLeft(), var, fieldGenerators)
                        && isRationalOverDifferentialField(((BinaryOperation) f).getRight(), var, fieldGenerators);
            }
            if (f.isIntegerPower()) {
                return isRationalOverDifferentialField(((BinaryOperation) f).getLeft(), var, fieldGenerators);
            }
        }
        if (f.isFunction()) {

            if (fieldGenerators.containsExquivalent(f)) {
                return true;
            }

            if (f.isFunction(TypeFunction.exp)) {
                ExpressionCollection nonConstantSummandsLeft = SimplifyUtilities.getNonConstantSummandsLeftInExpression(((Function) f).getLeft(), var);
                ExpressionCollection nonConstantSummandsRight = SimplifyUtilities.getNonConstantSummandsRightInExpression(((Function) f).getLeft(), var);
                Expression nonConstantSummand = SimplifyUtilities.produceDifference(nonConstantSummandsLeft, nonConstantSummandsRight);
                Expression currentQuotient;
                for (Expression fieldGenerator : fieldGenerators) {
                    if (!fieldGenerator.isFunction(TypeFunction.exp)) {
                        continue;
                    }
                    try {
                        currentQuotient = nonConstantSummand.div(((Function) fieldGenerator).getLeft()).simplify();
                        if (currentQuotient.isIntegerConstant()) {
                            return true;
                        }
                    } catch (EvaluationException e) {
                    }
                }
                return false;
            }

            if (f.isFunction(TypeFunction.ln)) {
                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(((Function) f).getLeft());
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(((Function) f).getLeft());
                ExpressionCollection summandsLeftForCompare, summandsRightForCompare;
                Expression currentQuotient;
                boolean unclearCaseFound = false;
                for (Expression fieldGenerator : fieldGenerators) {
                    if (!fieldGenerator.isFunction(TypeFunction.ln)) {
                        continue;
                    }
                    summandsLeftForCompare = SimplifyUtilities.getSummandsLeftInExpression(((Function) fieldGenerator).getLeft());
                    summandsRightForCompare = SimplifyUtilities.getSummandsRightInExpression(((Function) fieldGenerator).getLeft());
                    if ((summandsLeft.getBound() + summandsRight.getBound()) * (summandsLeftForCompare.getBound() + summandsRightForCompare.getBound()) > 1) {
                        unclearCaseFound = true;
                    }
                    try {
                        currentQuotient = ((Function) f).getLeft().div(((Function) fieldGenerator).getLeft()).simplify();
                        if (currentQuotient.isIntegerConstantOrRationalConstant()) {
                            return true;
                        }
                    } catch (EvaluationException e) {
                        return false;
                    }

                }
                if (unclearCaseFound) {
                    return false;
                }
            }

            return false;

        }

        return false;

    }

    public static ExpressionCollection getOrderedTranscendentalGeneratorsForDifferentialField(Expression f, String var) {
        ExpressionCollection fieldGenerators = new ExpressionCollection();
        try {
            f = f.simplify(simplifyTypesForDifferentialFieldExtension, var);
        } catch (EvaluationException e) {
            return fieldGenerators;
        }
        boolean newGeneratorFound;
        do {
            newGeneratorFound = addTranscendentalGeneratorForDifferentialField(f, var, fieldGenerators);
        } while (newGeneratorFound);
        return fieldGenerators;
    }

    private static boolean addTranscendentalGeneratorForDifferentialField(Expression f, String var, ExpressionCollection fieldGenerators) {

        if (isFunctionRationalOverDifferentialField(f, var, fieldGenerators)) {
            return false;
        }
        if (f instanceof BinaryOperation) {
            if (f.isNotPower()) {
                return addTranscendentalGeneratorForDifferentialField(((BinaryOperation) f).getLeft(), var, fieldGenerators)
                        || addTranscendentalGeneratorForDifferentialField(((BinaryOperation) f).getRight(), var, fieldGenerators);
            }
            if (f.isIntegerPower()) {
                return addTranscendentalGeneratorForDifferentialField(((BinaryOperation) f).getLeft(), var, fieldGenerators);
            }
        }
        if (f.isFunction()) {

            if (!isFunctionRationalOverDifferentialField(((Function) f).getLeft(), var, fieldGenerators)) {
                // Dann zuerst Erzeuger hinzufügen, die im Funktionsargument enthalten sind.
                return addTranscendentalGeneratorForDifferentialField(((Function) f).getLeft(), var, fieldGenerators);
            }

            if (f.isFunction(TypeFunction.exp)) {
                ExpressionCollection nonConstantSummandsLeft = SimplifyUtilities.getNonConstantSummandsLeftInExpression(((Function) f).getLeft(), var);
                ExpressionCollection nonConstantSummandsRight = SimplifyUtilities.getNonConstantSummandsRightInExpression(((Function) f).getLeft(), var);
                Expression nonConstantSummand = SimplifyUtilities.produceDifference(nonConstantSummandsLeft, nonConstantSummandsRight);
                Expression currentQuotient;
                for (int i = 0; i < fieldGenerators.getBound(); i++) {
                    if (fieldGenerators.get(i) == null || !fieldGenerators.get(i).isFunction(TypeFunction.exp)) {
                        continue;
                    }
                    try {
                        currentQuotient = nonConstantSummand.div(((Function) fieldGenerators.get(i)).getLeft()).simplify();
                        if (currentQuotient.isRationalConstant()) {
                            // Wenn das Verhältnis ganz ist, braucht man nichts aufzunehmen.
                            /* Wenn currentQuotient = p/q ist und fieldGenerators.get(i) = exp(f(x)),
                             so wird fieldGenerators.get(i) zu exp(f(x)/q).
                             */
                            BigInteger a = BigInteger.ONE;
                            BigInteger b = BigInteger.ONE;
                            ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(nonConstantSummand);
                            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(nonConstantSummand);

                            if (factorsNumerator.get(0).isIntegerConstant()) {
                                a = ((Constant) factorsNumerator.get(0)).getValue().toBigInteger().abs();
                                factorsNumerator.remove(0);
                            }
                            if (!factorsDenominator.isEmpty() && factorsDenominator.get(0).isIntegerConstant()) {
                                b = ((Constant) factorsDenominator.get(0)).getValue().toBigInteger().abs();
                                factorsDenominator.remove(0);
                            }

                            Expression quotient = new Constant(a).div(b).div(currentQuotient).simplify();

                            BigInteger c, d;
                            if (quotient.isIntegerConstant()) {
                                c = ((Constant) quotient).getValue().toBigInteger();
                                d = BigInteger.ONE;
                            } else {
                                c = ((Constant) ((BinaryOperation) quotient).getLeft()).getValue().toBigInteger();
                                d = ((Constant) ((BinaryOperation) quotient).getRight()).getValue().toBigInteger();
                            }

                            a = a.gcd(c);
                            b = ArithmeticMethods.lcm(b, d);
                            factorsNumerator.add(new Constant(a));
                            factorsDenominator.add(new Constant(b));
                            Expression expArgument = SimplifyUtilities.produceQuotient(factorsNumerator, factorsDenominator);

                            fieldGenerators.put(i, expArgument.exp().simplify());
                            return true;
                        }
                    } catch (EvaluationException e) {
                    }
                }
                fieldGenerators.add(f);
                return true;
            }

            if (f.isFunction(TypeFunction.ln)) {
                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(((Function) f).getLeft());
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(((Function) f).getLeft());
                ExpressionCollection summandsLeftForCompare, summandsRightForCompare;
                Expression currentQuotient;
                boolean unclearCaseFound = false;
                for (Expression fieldGenerator : fieldGenerators) {
                    if (!fieldGenerator.isFunction(TypeFunction.ln)) {
                        continue;
                    }
                    try {
                        currentQuotient = ((Function) f).getLeft().div(((Function) fieldGenerator).getLeft()).simplify();
                        if (currentQuotient.isIntegerConstantOrRationalConstant()) {
                            return false;
                        }
                    } catch (EvaluationException e) {
                    }
                    summandsLeftForCompare = SimplifyUtilities.getSummandsLeftInExpression(((Function) fieldGenerator).getLeft());
                    summandsRightForCompare = SimplifyUtilities.getSummandsRightInExpression(((Function) fieldGenerator).getLeft());
                    if ((summandsLeft.getBound() + summandsRight.getBound()) * (summandsLeftForCompare.getBound() + summandsRightForCompare.getBound()) > 1) {
                        unclearCaseFound = true;
                    }
                }
                if (unclearCaseFound) {
                    return false;
                }
                fieldGenerators.add(f);
                return true;
            }

            return false;

        }

        return false;

    }

    ////////////////////////////////////////////////// Der Risch-Algorithmus ///////////////////////////////////////////
    /**
     * Gibt zurück, ob f durch Adjunktion eines einzigen transzendenten Elements
     * aus dem Körper der rationalen Funktionen (über den reellen Zahlen)
     * gewonnen werden kann. Das transzendente Element muss auch noch die
     * korrekte Form besitzen.
     */
    private static boolean isExtensionOfDegreeOne(Expression f, String var) {

        ExpressionCollection transcendentalGenerators = getOrderedTranscendentalGeneratorsForDifferentialField(f, var);
        boolean hasOnlyOneTranscendentalElement = transcendentalGenerators.getBound() == 1
                && areFieldExtensionsInCorrectForm(transcendentalGenerators, var);
        if (!hasOnlyOneTranscendentalElement) {
            return false;
        }
        return isRationalOverDifferentialField(f, var, transcendentalGenerators);

    }

    /**
     * Hauptmethode für das Integrieren gemäß dem Risch-Algorithmus im Falle
     * einer Erweiterung durch transzendente Elemente.
     *
     * @throws NotAlgebraicallyIntegrableException
     * @throws EvaluationException
     */
    public static Expression integrateByRischAlgorithmForTranscendentalExtension(Operator expr) throws NotAlgebraicallyIntegrableException, EvaluationException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        ExpressionCollection transcendentalExtensions = getOrderedTranscendentalGeneratorsForDifferentialField(f, var);

        // Nur echte transzende Erweiterungen betrachten.
        if (transcendentalExtensions.isEmpty() || !isFunctionRationalOverDifferentialField(f, var, transcendentalExtensions)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Letztes transzendes Element wählen und damit den Risch-Algorithmus starten.
        Expression transcententalElement = transcendentalExtensions.get(transcendentalExtensions.getBound() - 1);
        String transcendentalVar = SubstitutionUtilities.getSubstitutionVariable(f);
        Expression fSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(f, transcententalElement, Variable.create(transcendentalVar)).simplify();

        // Sei t = transcendentalVar. Dann muss fSubstituted eine rationale Funktion in t sein.
        if (!SimplifyRationalFunctionMethods.isRationalFunction(fSubstituted, transcendentalVar)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        if (!(fSubstituted instanceof BinaryOperation)) {
            // Dann sind andere Integrationsmethoden dafür zuständig.
            throw new NotAlgebraicallyIntegrableException();
        }

        // Zunächst alles auf einen Bruch bringen.
        fSubstituted = SimplifyBinaryOperationMethods.bringFractionToCommonDenominator((BinaryOperation) fSubstituted);

        // Separat behandeln, falls fSubstituted kein Quotient ist.
        if (!fSubstituted.isQuotient()) {
            // TO DO.
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection coefficientsNumerator, coefficientsDenominator;
        try {
            coefficientsNumerator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) fSubstituted).getLeft(), transcendentalVar);
            coefficientsDenominator = SimplifyPolynomialMethods.getPolynomialCoefficients(((BinaryOperation) fSubstituted).getRight(), transcendentalVar);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }
        ExpressionCollection[] quotient = SimplifyPolynomialMethods.polynomialDivision(coefficientsNumerator, coefficientsDenominator);

        // Im Fall einer Exponentialerweiterung: t im Nenner faktorisieren und in den polynomiallen Teil übertragen.
        if (transcententalElement.isFunction(TypeFunction.exp)) {

            int ordOfTranscendentalElementInDenominator = 0;
            for (int i = 0; i < coefficientsDenominator.getBound(); i++) {
                if (!coefficientsDenominator.get(i).equals(ZERO)) {
                    break;
                } else {
                    ordOfTranscendentalElementInDenominator++;
                }
            }

            if (ordOfTranscendentalElementInDenominator > 0) {
                // TO DO. Speziellen Teil abspalten.
                throw new NotAlgebraicallyIntegrableException();
            }

        }

        /* 
         Im Fall einer Logarithmuserweiterung (oder Exponentialerweiterung ohne speziellen Teil): 
         Polynomialen und gebrochenen Teil separat integrieren (Nach Risch-Algorithmus erlaubt).
         */
        Expression integralOfPolynomialPart = integrateByRischAlgorithmPolynomialPart(quotient[0], new ExpressionCollection(), transcententalElement, var, transcendentalVar);
        Expression integralOfFractionalPart = integrateByRischAlgorithmFractionalPart(quotient[1], coefficientsDenominator, transcententalElement, var, transcendentalVar);
        return integralOfPolynomialPart.add(integralOfFractionalPart);

    }

    /**
     * Risch-Algorithmus für den polynomialen Anteil.
     *
     * @throws NotAlgebraicallyIntegrableException
     * @throws EvaluationException
     */
    private static Expression integrateByRischAlgorithmPolynomialPart(ExpressionCollection polynomialCoefficients, ExpressionCollection laurentCoefficients,
            Expression transcententalElement, String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException, EvaluationException {
        if (polynomialCoefficients.isEmpty() && laurentCoefficients.isEmpty()) {
            return ZERO;
        }
        if ((!polynomialCoefficients.isEmpty() || !laurentCoefficients.isEmpty()) && transcententalElement.isFunction(TypeFunction.exp)) {
            // Noch zu implementieren.
            throw new NotAlgebraicallyIntegrableException();
        }
        if (!polynomialCoefficients.isEmpty() && transcententalElement.isFunction(TypeFunction.ln)) {
            return integrateByRischAlgorithmPolynomialPartLogarithmicExtension(polynomialCoefficients, transcententalElement, var, transcendentalVar);
        }

        throw new NotAlgebraicallyIntegrableException();
    }

    public static Expression integrateByRischAlgorithmPolynomialPartLogarithmicExtension(ExpressionCollection polynomialCoefficients, Expression transcententalElement,
            String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException {

        Expression logArgument = ((Function) transcententalElement).getLeft();

        Expression[] coefficientsOfPolynomialInTranscendentalVar = new Expression[polynomialCoefficients.getBound() + 1];
        String[] freeConstantsVars = new String[polynomialCoefficients.getBound() + 1];

        for (int i = 0; i < freeConstantsVars.length; i++) {
            freeConstantsVars[i] = NotationLoader.FREE_INTEGRATION_CONSTANT_VAR + "_" + i;
        }

        Expression integral;
        Expression equoationForFreeConstant;
        ExpressionCollection valuesForFreeConstant;
        for (int i = polynomialCoefficients.getBound(); i >= 0; i--) {

            if (i == polynomialCoefficients.getBound()) {
                coefficientsOfPolynomialInTranscendentalVar[i] = Variable.create(freeConstantsVars[i]);
            } else {
                
                try {
                    integral = new Operator(TypeOperator.integral, new Object[]{
                        polynomialCoefficients.get(i).sub(new Constant(i + 1).mult(Variable.create(freeConstantsVars[i + 1])).mult(logArgument.diff(var)).div(logArgument)), var}).simplify(simplifyTypesRischAlgorithmPolynomialPart);

                    integral = SubstitutionUtilities.substituteExpressionByAnotherExpression(integral, transcententalElement, Variable.create(transcendentalVar));
                    equoationForFreeConstant = integral.diff(transcendentalVar).simplify();
                    
                    if (!SimplifyPolynomialMethods.isLinearPolynomial(equoationForFreeConstant, freeConstantsVars[i + 1])) {
                        throw new NotAlgebraicallyIntegrableException();
                    }
                    valuesForFreeConstant = SolveGeneralEquationMethods.solvePolynomialEquation(equoationForFreeConstant, freeConstantsVars[i + 1]);
                    if (valuesForFreeConstant.getBound() != 1) {
                        throw new NotAlgebraicallyIntegrableException();
                    }
                    coefficientsOfPolynomialInTranscendentalVar[i + 1] = coefficientsOfPolynomialInTranscendentalVar[i + 1].replaceVariable(freeConstantsVars[i + 1], valuesForFreeConstant.get(0));
                    coefficientsOfPolynomialInTranscendentalVar[i] = new Operator(TypeOperator.integral, new Object[]{
                        polynomialCoefficients.get(i).sub(new Constant(i + 1).mult(coefficientsOfPolynomialInTranscendentalVar[i + 1]).mult(logArgument.diff(var)).div(logArgument)), var}).simplify(simplifyTypesRischAlgorithmPolynomialPart);
                    if (coefficientsOfPolynomialInTranscendentalVar[i].containsIndefiniteIntegral()){
                        throw new NotAlgebraicallyIntegrableException();
                    }
                    if (i > 0){
                       coefficientsOfPolynomialInTranscendentalVar[i] = coefficientsOfPolynomialInTranscendentalVar[i].add(Variable.create(freeConstantsVars[i]));
                    }
                } catch (EvaluationException | NotAlgebraicallySolvableException e) {
                    throw new NotAlgebraicallyIntegrableException();
                }

            }

        }

        return SimplifyPolynomialMethods.getPolynomialFromCoefficients(new ExpressionCollection(coefficientsOfPolynomialInTranscendentalVar),
                transcendentalVar).replaceVariable(transcendentalVar, transcententalElement);

    }

    /**
     * Risch-Algorithmus für den gebrochenen Anteil.
     *
     * @throws NotAlgebraicallyIntegrableException
     * @throws EvaluationException
     */
    private static Expression integrateByRischAlgorithmFractionalPart(ExpressionCollection coefficientsNumerator, ExpressionCollection coefficientsDenominator,
            Expression transcententalElement, String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException, EvaluationException {

        Expression decompositionOfDenominator;
        try {
            decompositionOfDenominator = SimplifyPolynomialMethods.decomposeRationalPolynomialIntoSquarefreeFactors(coefficientsDenominator, transcendentalVar);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        } catch (SimplifyPolynomialMethods.PolynomialNotDecomposableException e) {
            decompositionOfDenominator = SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsDenominator, transcendentalVar);
        }

        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactors(decompositionOfDenominator);
        Expression leadingCoefficient = ONE;

        if (factorsDenominator.getBound() == 1) {
            leadingCoefficient = coefficientsDenominator.get(coefficientsDenominator.getBound() - 1);
            factorsDenominator.divideByExpression(leadingCoefficient);
            factorsDenominator = factorsDenominator.simplify();
        } else {
            for (int i = 0; i < factorsDenominator.getBound(); i++) {
                if (!factorsDenominator.get(i).contains(transcendentalVar)) {
                    leadingCoefficient = leadingCoefficient.mult(factorsDenominator.get(i));
                    factorsDenominator.put(i, null);
                }
            }
        }

        leadingCoefficient = leadingCoefficient.simplify();

        // Nenner normieren!
        decompositionOfDenominator = SimplifyUtilities.produceProduct(factorsDenominator);
        coefficientsNumerator.divideByExpression(leadingCoefficient);
        coefficientsNumerator = coefficientsNumerator.simplify();

        // Hermite-Reduktion und später expliziten Risch-Algorithmus anwenden, wenn der Nenner quadratfrei ist.
        return doHermiteReduction(coefficientsNumerator, decompositionOfDenominator, transcententalElement, var, transcendentalVar);

    }

    ////////////////////////////////////////////////// Die Hermite-Reduktion ///////////////////////////////////////////
    /**
     * Integration mittels Hermite-Reduktion.
     *
     * @throws NotAlgebraicallyIntegrableException
     */
    private static Expression doHermiteReduction(ExpressionCollection coefficientsNumerator, Expression denominator,
            Expression transcententalElement, String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException {

        // Zunächst: Prüfung, ob Zähler und Nenner teilerfremd sind. Wenn nicht: kürzen, neuen Nenner wieder zerlegen und fortfahren.
        ExpressionCollection coefficientsDenominator;
        try {
            coefficientsDenominator = SimplifyPolynomialMethods.getPolynomialCoefficients(denominator, transcendentalVar);
            ExpressionCollection gcdOfNumeratorAndDenominator = SimplifyPolynomialMethods.getGGTOfPolynomials(coefficientsNumerator, coefficientsDenominator);
            if (gcdOfNumeratorAndDenominator.getBound() > 1) {
                // Dann ist der ggT nicht trivial.
                ExpressionCollection[] quotient = SimplifyPolynomialMethods.polynomialDivision(coefficientsNumerator, gcdOfNumeratorAndDenominator);
                coefficientsNumerator = quotient[0];
                quotient = SimplifyPolynomialMethods.polynomialDivision(coefficientsDenominator, gcdOfNumeratorAndDenominator);
                coefficientsDenominator = quotient[0];
                try {
                    denominator = SimplifyPolynomialMethods.decomposeRationalPolynomialIntoSquarefreeFactors(coefficientsDenominator, transcendentalVar);
                } catch (SimplifyPolynomialMethods.PolynomialNotDecomposableException e) {
                    denominator = SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsDenominator, transcendentalVar);
                }
            }
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        int degNumerator = coefficientsNumerator.getBound() - 1;
        int degDenominator = SimplifyPolynomialMethods.getDegreeOfPolynomial(denominator, transcendentalVar).intValue();

        // Der Grad des Zählers sollte eigentlich bei jedem Reduktionsschritt kleiner als der Grad des Nenners sein (außer beide Polynome sind konstant). Daher diese Sicherheitsabfrage.
        if (degDenominator == 0 && degNumerator > 0 || degDenominator > 0 && degNumerator >= degDenominator) {
            throw new NotAlgebraicallyIntegrableException();
        }

        if (denominator.isNotPower() && denominator.isNotProduct()) {
            // Nenner ist quadratfrei -> explizit Stammfunktion bestimmen.
            try {
                return integrateByRischAlgorithmInSquareFreeCase(coefficientsNumerator, coefficientsDenominator, transcententalElement, var, transcendentalVar);
            } catch (EvaluationException e) {
                throw new NotAlgebraicallyIntegrableException();
            }
        }

        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactors(denominator);
        for (int i = factorsDenominator.getBound() - 1; i >= 0; i--) {
            if (factorsDenominator.get(i).isIntegerPower()) {

                BigInteger m = ((Constant) ((BinaryOperation) factorsDenominator.get(i)).getRight()).getValue().toBigInteger();
                Expression v = ((BinaryOperation) factorsDenominator.get(i)).getLeft();
                factorsDenominator.put(i, null);
                Expression u = SimplifyUtilities.produceProduct(factorsDenominator);
                Expression derivativeOfV;
                try {
                    derivativeOfV = v.replaceVariable(transcendentalVar, transcententalElement);
                    derivativeOfV = derivativeOfV.diff(var);
                    derivativeOfV = SubstitutionUtilities.substituteExpressionByAnotherExpression(derivativeOfV, transcententalElement, Variable.create(transcendentalVar)).simplify();

                    Expression gcd = SimplifyPolynomialMethods.getGGTOfPolynomials(u.mult(derivativeOfV), v, transcendentalVar);
                    if (!gcd.equals(ONE)) {
                        // Sollte eigentlich laut Theorem nie passieren!
                        throw new NotAlgebraicallyIntegrableException();
                    }

                    Expression[] euclideanCoefficients = SimplifyPolynomialMethods.getEuclideanRepresentationOfGCDOfTwoPolynomials(u.mult(derivativeOfV), v, transcendentalVar);
                    Expression a = SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsNumerator, transcendentalVar);
                    Expression b = euclideanCoefficients[0].mult(a.div(ONE.sub(m))).simplify();
                    Expression c = euclideanCoefficients[1].mult(a.div(ONE.sub(m))).simplify();

                    Expression derivativeOfB = b.replaceVariable(transcendentalVar, transcententalElement);
                    derivativeOfB = derivativeOfB.diff(var);
                    derivativeOfB = SubstitutionUtilities.substituteExpressionByAnotherExpression(derivativeOfB, transcententalElement, Variable.create(transcendentalVar)).simplify();

                    Expression newIntegrand = ONE.sub(m).mult(c).sub(u.mult(derivativeOfB));
                    ExpressionCollection coefficientsNewNumerator = SimplifyPolynomialMethods.getPolynomialCoefficients(newIntegrand, transcendentalVar);
                    return b.div(v.pow(m.subtract(BigInteger.ONE))).replaceVariable(transcendentalVar, transcententalElement).add(doHermiteReduction(coefficientsNewNumerator, u.mult(v.pow(m.subtract(BigInteger.ONE))),
                            transcententalElement, var, transcendentalVar));

                } catch (EvaluationException e) {
                    factorsDenominator.put(i, v.pow(m));
                }

            }
        }

        // Dann ist der Nenner quadratfrei (aber eventuell faktorisiert).
        try {
            return integrateByRischAlgorithmInSquareFreeCase(coefficientsNumerator, coefficientsDenominator, transcententalElement, var, transcendentalVar);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

    }

    /**
     * Risch-Algorithmus für den gebrochenen Anteil im Falle eines quadratfreien
     * Nenners.
     *
     * @throws NotAlgebraicallyIntegrableException
     * @throws EvaluationException
     */
    private static Expression integrateByRischAlgorithmInSquareFreeCase(ExpressionCollection coefficientsNumerator, ExpressionCollection coefficientsDenominator,
            Expression transcententalElement, String var, String transcendentalVar) throws NotAlgebraicallyIntegrableException, EvaluationException {

        // Leitkoeffizienten vom Nenner in den Zähler verschieben.
        if (!coefficientsDenominator.get(coefficientsDenominator.getBound() - 1).equals(ONE)) {
            coefficientsNumerator.divideByExpression(coefficientsDenominator.get(coefficientsDenominator.getBound() - 1));
            coefficientsNumerator = coefficientsNumerator.simplify();

            coefficientsDenominator.divideByExpression(coefficientsDenominator.get(coefficientsDenominator.getBound() - 1));
            coefficientsDenominator = coefficientsDenominator.simplify();
        }

        // Sonderfall: Nenner hat Grad = 0 (also von t nicht abhängig) -> Integration mittels Partialbruchzerlegung.
        if (coefficientsDenominator.getBound() == 1) {
            if (coefficientsNumerator.getBound() > 1) {
                // Sollte eigentlich nie eintreten, da nach Hermite-Reduktion der Grad des Zählers kleiner als der Grad des Nenners sein sollte.
                throw new NotAlgebraicallyIntegrableException();
            }
            Expression integrand = SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsNumerator, transcendentalVar).div(
                    SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsDenominator, transcendentalVar)).replaceVariable(transcendentalVar, transcententalElement);
            return GeneralIntegralMethods.integrateIndefinite(new Operator(TypeOperator.integral, new Object[]{integrand, var}));
        }

        // Sei t = transcendentalVar, a(t) = Zählöer, b(t) = Nenner.
        // Zunächst: b'(t) bestimmen (Ableitung nach x = var).
        Expression derivativeOfDenominator = SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsDenominator, transcendentalVar);
        derivativeOfDenominator = derivativeOfDenominator.replaceVariable(transcendentalVar, transcententalElement);
        derivativeOfDenominator = derivativeOfDenominator.diff(var);
        derivativeOfDenominator = SubstitutionUtilities.substituteExpressionByAnotherExpression(derivativeOfDenominator, transcententalElement, Variable.create(transcendentalVar));

        // Koeffizienten von b'(t) bestimmen.
        ExpressionCollection coefficientsDerivativeOfDenominator = SimplifyPolynomialMethods.getPolynomialCoefficients(derivativeOfDenominator, transcendentalVar);

        String resultantVar = SubstitutionUtilities.getSubstitutionVariable(SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsNumerator, transcendentalVar),
                SimplifyPolynomialMethods.getPolynomialFromCoefficients(coefficientsDenominator, transcendentalVar));

        // Resultante bilden.
        // Erstes Argument der Resultante: a(t) - z*b'(t), z = resultantVar;
        ExpressionCollection coefficientsOfFirstArgument = SimplifyPolynomialMethods.subtractPolynomials(coefficientsNumerator,
                SimplifyPolynomialMethods.multiplyPolynomials(new ExpressionCollection(Variable.create(resultantVar)), coefficientsDerivativeOfDenominator));
        // Zweites Argument der Resultante: b(t);
        MatrixExpression resultantAsMatrixExpression = SimplifyPolynomialMethods.getResultant(coefficientsOfFirstArgument, coefficientsDenominator);

        if (!(resultantAsMatrixExpression.convertOneTimesOneMatrixToExpression() instanceof Expression)) {
            // Resultante nicht explizit berechenbar.
            throw new NotAlgebraicallyIntegrableException();
        }

        Expression resultant = (Expression) resultantAsMatrixExpression.convertOneTimesOneMatrixToExpression();
        if (!SimplifyPolynomialMethods.isPolynomial(resultant, resultantVar)) {
            // Sollte eigentlich nie vorkommen.
            throw new NotAlgebraicallyIntegrableException();
        }

        // Prüfen, ob 1. die Nullstellen von resultant von konstant sind und 2. ob ihre Anzahl = deg(resultant) ist.
        resultant = SimplifyPolynomialMethods.decomposePolynomialInIrreducibleFactors(resultant, resultantVar);
        if (!isPolynomialDecomposedIntoPairwiseDifferentLinearFaktors(resultant, resultantVar)) {
            throw new NotAlgebraicallyIntegrableException();
        }

        // Prüfung auf die Anzahl der Nullstellen.
        ExpressionCollection zerosOfResultant;
        try {
            zerosOfResultant = SolveGeneralEquationMethods.solvePolynomialEquation(resultant, resultantVar);
            if (BigInteger.valueOf(zerosOfResultant.getBound()).compareTo(SimplifyPolynomialMethods.getDegreeOfPolynomial(resultant, resultantVar)) < 0) {
                throw new NotAlgebraicallyIntegrableException();
            }
        } catch (NotAlgebraicallySolvableException e) {
            throw new NotAlgebraicallyIntegrableException();
        }

        ExpressionCollection thetas = new ExpressionCollection();
        Expression gcd;
        if (transcententalElement.isFunction(TypeFunction.exp) || transcententalElement.isFunction(TypeFunction.ln)) {
            // theta_i(t) = gcd(a(t) - z_i*b'(t), b(t)), {z_0, ..., z_(k - 1)} sind die Nullstellen der Resultante.
            for (Expression zero : zerosOfResultant) {
                gcd = SimplifyPolynomialMethods.getPolynomialFromCoefficients(SimplifyPolynomialMethods.getGGTOfPolynomials(SimplifyPolynomialMethods.subtractPolynomials(coefficientsNumerator,
                        SimplifyPolynomialMethods.multiplyPolynomials(new ExpressionCollection(zero), coefficientsDerivativeOfDenominator)),
                        coefficientsDenominator), transcendentalVar).replaceVariable(transcendentalVar, transcententalElement);
                thetas.add(gcd);
            }
        }

        // Logarithmischer Fall.
        if (transcententalElement.isFunction(TypeFunction.ln)) {

            // Stammfunktion ausgeben.
            Expression integral = ZERO;
            for (int i = 0; i < zerosOfResultant.getBound(); i++) {
                integral = integral.add(thetas.get(i).ln());
            }
            return integral;

        }

        // Exponentieller Fall.
        if (transcententalElement.isFunction(TypeFunction.exp)) {

            // Stammfunktion ausgeben.
            Expression integral = ZERO;
            for (int i = 0; i < zerosOfResultant.getBound(); i++) {
                integral = integral.add(zerosOfResultant.get(i).mult(SimplifyPolynomialMethods.getDegreeOfPolynomial(thetas.get(i), transcendentalVar)));
            }
            integral = MINUS_ONE.mult(integral).mult(((Function) transcententalElement).getLeft());
            for (int i = 0; i < zerosOfResultant.getBound(); i++) {
                integral = integral.add(thetas.get(i).ln());
            }
            return integral;
        }

        throw new NotAlgebraicallyIntegrableException();

    }

    /**
     * Hilfsmethode. Gibt zurück, ob f ein Produkt aus entweder bzw. var
     * konstanten oder bzw. var linearen Polynomen ist.
     */
    private static boolean isPolynomialDecomposedIntoPairwiseDifferentLinearFaktors(Expression f, String var) {
        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        for (Expression factor : factors) {
            if (!factor.contains(var)) {
                continue;
            }
            if (!SimplifyPolynomialMethods.isLinearPolynomial(factor, var)) {
                return false;
            }
        }
        return true;
    }

}
