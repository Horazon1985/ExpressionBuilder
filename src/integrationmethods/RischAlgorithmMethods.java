package integrationmethods;

import exceptions.EvaluationException;
import exceptions.MathToolException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import expressionbuilder.TypeOperator;
import expressionbuilder.TypeSimplify;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyUtilities;
import java.util.HashSet;

public abstract class RischAlgorithmMethods {

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
    public static boolean isFunctionAlgebraicOverDifferentialField(Expression f, String var, ExpressionCollection fieldGenerators) {

        if (!areFieldExtensionsInCorrectForm(fieldGenerators, var)) {
            // Schlechter Fall, da fieldExtensions nicht in die korrekte Form besitzt.
            return false;
        }
        // Weitestgehend vereinfachen, wenn möglich.
        try {
            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.order_difference_and_division);
            simplifyTypes.add(TypeSimplify.order_sums_and_products);
            simplifyTypes.add(TypeSimplify.simplify_trivial);
            simplifyTypes.add(TypeSimplify.simplify_expand_rational_factors);
            simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals_in_sums);
            simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals_in_differences);
            simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
            simplifyTypes.add(TypeSimplify.simplify_collect_products);
            simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
            simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
            simplifyTypes.add(TypeSimplify.simplify_replace_exponential_functions_with_respect_to_variable_by_definitions);
            f = f.simplify(var, simplifyTypes);
        } catch (EvaluationException e) {
            return false;
        }

        try {
            return isAlgebraicOverDifferentialField(f, var, fieldGenerators);
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
    private static boolean isAlgebraicOverDifferentialField(Expression f, String var, ExpressionCollection fieldGenerators) throws NotDecidableException {

        if (fieldGenerators.containsExquivalent(f)) {
            return true;
        }

        if (!f.contains(var) || f.equals(Variable.create(var))) {
            return true;
        }
        if (f instanceof BinaryOperation) {
            if (f.isNotPower()) {
                return isAlgebraicOverDifferentialField(((BinaryOperation) f).getLeft(), var, fieldGenerators)
                        && isAlgebraicOverDifferentialField(((BinaryOperation) f).getRight(), var, fieldGenerators);
            }
            if (f.isRationalPower()) {
                return isAlgebraicOverDifferentialField(((BinaryOperation) f).getLeft(), var, fieldGenerators);
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
                        if (!currentQuotient.contains(var)){
                            return true;
                        }
                    } catch (EvaluationException e) {
                        throw new NotDecidableException();
                    }
                }
                return false;
            }
            if (f.isFunction(TypeFunction.ln)) {
                // TO DO!
            }
            return (f.isFunction(TypeFunction.id) || f.isFunction(TypeFunction.abs)
                    || f.isFunction(TypeFunction.sgn) || f.isFunction(TypeFunction.sqrt))
                    && isAlgebraicOverDifferentialField(((Function) f).getLeft(), var, fieldGenerators);
        }
        if (f.isOperator()) {
            if (f.isOperator(TypeOperator.fac) || f.isOperator(TypeOperator.gcd)
                    || f.isOperator(TypeOperator.lcm) || f.isOperator(TypeOperator.mod)) {
                return !f.contains(var);
            }
            throw new NotDecidableException();
        }

        return false;

    }

    public static ExpressionCollection getOrderedTranscendentalGeneratorsForDifferentialField(Expression f, String var) {

        ExpressionCollection fieldGenerators = new ExpressionCollection();

        return fieldGenerators;

    }

}
