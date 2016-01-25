package abstractexpressions.expression.integration;

import abstractexpressions.expression.computation.ArithmeticMethods;
import exceptions.EvaluationException;
import exceptions.MathToolException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.TypeFunction;
import enums.TypeSimplify;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyUtilities;
import java.math.BigInteger;
import java.util.HashSet;

public abstract class RischAlgorithmMethods {

    private static final HashSet<TypeSimplify> simplifyTypesForDifferentialFieldExtension = getSimplifyTypesForDifferentialFieldExtensions();

    private static HashSet<TypeSimplify> getSimplifyTypesForDifferentialFieldExtensions() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
        simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
        simplifyTypes.add(TypeSimplify.simplify_replace_exponential_functions_with_respect_to_variable_by_definitions);
        return simplifyTypes;
    }

    /**
     * Private Fehlerklasse für den Fall, dass im Risch-Algorithmus etwas nicht
     * entscheidbar ist.
     */
    private static class NotDecidableException extends MathToolException {

        private static final String NOT_DECIDABLE_MESSAGE = "Some aspects in the Risch algorithm are not decidable.";

        public NotDecidableException() {
            super(NOT_DECIDABLE_MESSAGE);
        }

        public NotDecidableException(String s) {
            super(s);
        }

    }

    private static boolean areFieldExtensionsInCorrectForm(ExpressionCollection fieldGenerators, String var) {
        for (Expression fieldExtension : fieldGenerators) {
            if (fieldExtension.isFunction(TypeFunction.exp)) {
                ExpressionCollection constantSummandsLeft = SimplifyUtilities.getConstantSummandsLeftInExpression(fieldExtension, var);
                ExpressionCollection constantSummandsRight = SimplifyUtilities.getConstantSummandsRightInExpression(fieldExtension, var);
                return constantSummandsLeft.isEmpty() && constantSummandsRight.isEmpty();
            }
            if (fieldExtension.isFunction(TypeFunction.ln)) {
                ExpressionCollection constantFactorsEnumerator = SimplifyUtilities.getConstantFactorsOfEnumeratorInExpression(fieldExtension, var);
                ExpressionCollection constantFactorsDenominator = SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(fieldExtension, var);
                return constantFactorsEnumerator.isEmpty() && constantFactorsDenominator.isEmpty();
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
     * (2) f = ln(x)+x!, var = "x", fieldGenerators = {ln(x), exp(x)}. Hier wird
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

        try {
            return isRationalOverDifferentialField(f, var, fieldGenerators);
        } catch (NotDecidableException e) {
            return false;
        }

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
     * (2) f = ln(x)+x!, var = "x", fieldGenerators = {ln(x), exp(x)}. Hier wird
     * false zurückgegeben (aufgrund des Summanden x!, welcher transzendent über
     * der angegebenen Körpererweiterung ist).<br>
     */
    private static boolean isRationalOverDifferentialField(Expression f, String var, ExpressionCollection fieldGenerators) throws NotDecidableException {

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
            if (f.isIntegerPowerOrRationalPower()) {
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
                        throw new NotDecidableException();
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
                        throw new NotDecidableException();
                    }

                }
                if (unclearCaseFound) {
                    // Schlecht entscheidbare Fälle!
                    throw new NotDecidableException();
                }
            }

            return false;

        }
        if (f.isOperator()) {
            throw new NotDecidableException();
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
                            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(nonConstantSummand);
                            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(nonConstantSummand);

                            if (factorsEnumerator.get(0).isIntegerConstant()) {
                                a = ((Constant) factorsEnumerator.get(0)).getValue().toBigInteger().abs();
                                factorsEnumerator.remove(0);
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
                            factorsEnumerator.add(new Constant(a));
                            factorsDenominator.add(new Constant(b));
                            Expression expArgument = SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);

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

    /*
     Ab hier folgt der eigentliche Risch-Algorithmus!
     */
    private static boolean isExtensionOfDegreeOne(){
    
        
        
        
    
        return true;
        
    }
    
    
    
    
}
