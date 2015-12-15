package substitutionmethods;

import exceptions.EvaluationException;
import exceptions.NotSubstitutableException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.ONE;
import static expressionbuilder.Expression.ZERO;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import expressionbuilder.TypeSimplify;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyAlgebraicExpressionMethods;
import expressionsimplifymethods.SimplifyUtilities;
import java.util.HashSet;
import notations.NotationLoader;

public abstract class SubstitutionUtilitiesNew {

    /**
     * In f sind Variablen enthalten, unter anderem möglicherweise auch
     * "Parametervariablen" X_1, X_2, .... Diese Funktion liefert dasjenige X_i
     * mit dem kleinsten Index i, welches in f noch nicht vorkommt.
     */
    public static String getSubstitutionVariable(Expression f) {
        String var = NotationLoader.SUBSTITUTION_VAR + "_";
        int j = 1;
        while (f.contains(var + String.valueOf(j))) {
            j++;
        }
        return var + j;
    }

    /**
     * Gibt zurück, ob g eine positive ganzzahlige Potenz von f ist.
     * VORAUSSETZUNG: f und g sind KEINE Produkte. Falls ja, so wird der
     * entsprechende Exponent n zurückgegeben, so dass f^n = g. Andernfalls wird
     * false zurückgegeben.
     *
     * @throws EvaluationException
     */
    private static Expression isPositiveIntegerPower(Expression f, Expression g) throws EvaluationException, NotSubstitutableException {

        if (f.equivalent(g)) {
            return ONE;
        }

        if (g.isPower() && f.equivalent(((BinaryOperation) g).getLeft())
                && ((BinaryOperation) g).getRight().isIntegerConstant()) {
            return ((BinaryOperation) g).getRight();
        }

        if (f.isPower() && g.equivalent(((BinaryOperation) f).getLeft())) {
            Expression exponent = ONE.div(((BinaryOperation) f).getRight()).simplify();
            if (exponent.isIntegerConstant()) {
                return exponent;
            }
        }

        if (f.isPower() && g.isPower() && ((BinaryOperation) f).getLeft().equivalent(((BinaryOperation) g).getLeft())) {
            Expression exponent = ((BinaryOperation) g).getRight().div(((BinaryOperation) f).getRight()).simplify();
            if (exponent.isIntegerConstant()) {
                return exponent;
            }
        }

        throw new NotSubstitutableException();

    }

    /**
     * Hauptmethode zum Substituieren. Es wird versucht, im Ausdruck f den
     * Ausdruck substitution. Im Erfolgsfall wird der substituierte Ausdruck
     * zurückgegeben, wobei die Variable, durch die substitution ersetzt wird,
     * durch X_i, 1 = 1, 2, 3, ... bezeichnet wird (und i der kleinste Index
     * ist, so dass X_i in f nicht vorkommt). Ansonsten wird false
     * zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Expression substitute(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException, NotSubstitutableException {
        if (!f.contains(var)) {
            return f;
        }
        if (f.equivalent(substitution)) {
            return Variable.create(getSubstitutionVariable(f));
        }
        if (f.equals(Variable.create(var))) {
            return substituteVariable(var, substitution, beginning);
        }
        if (f.isSum()) {
            return substituteInSum(f, var, substitution, beginning);
        }
        if (f.isDifference()) {
            return substituteInDifference(f, var, substitution, beginning);
        }
        if (f.isProduct()) {
            return substituteInProduct(f, var, substitution, beginning);
        }
        if (f.isQuotient()) {
            return substituteInQuotient(f, var, substitution, beginning);
        }
        if (f.isPower()) {
            return substituteInPower(f, var, substitution);
        }
        if (f.isFunction()) {
            return substituteInFunction(f, var, substitution);
        }
        throw new NotSubstitutableException();
    }

    /**
     * Hier wird versucht, x = var durch substitution = x/a + b mit ganzem a zu
     * substituieren (also x = a*substitution - a*b).
     */
    private static Expression substituteVariable(String var, Expression substitution, boolean beginning) throws EvaluationException, NotSubstitutableException {
        if (!beginning) {
            throw new NotSubstitutableException();
        }
        Expression derivative = substitution.diff(var).simplify();
        if (derivative.equals(ZERO)) {
            throw new NotSubstitutableException();
        }
        Expression reciprocalOfDerivative = Expression.ONE.div(derivative).simplify();
        if (reciprocalOfDerivative.isIntegerConstant()) {
            Expression rest = substitution.replaceVariable(var, Expression.ZERO).simplify();
            String substVar = getSubstitutionVariable(Variable.create(var));
            return reciprocalOfDerivative.mult(Variable.create(substVar)).sub(reciprocalOfDerivative.mult(rest));
        }
        throw new NotSubstitutableException();
    }

    /**
     * Versucht, falls f eine Summe ist, f durch einen Ausdruck von substitution
     * zu ersetzen.
     *
     * @throws EvaluationException
     */
    private static Expression substituteInSum(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException, NotSubstitutableException {

        ExpressionCollection summandsF = SimplifyUtilities.getSummands(f);
        ExpressionCollection nonConstantSummandsSubstitution = SimplifyUtilities.getNonConstantSummands(substitution, var);
        if (nonConstantSummandsSubstitution.isEmpty()) {
            // Sollte nie passieren, aber trotzdem sicherheitshalber.
            throw new NotSubstitutableException();
        }

        Expression firstNonConstantSummandInSubstitution = nonConstantSummandsSubstitution.get(0);
        Expression k = ZERO;
        for (int i = 0; i < summandsF.getBound(); i++) {
            k = summandsF.get(i).div(firstNonConstantSummandInSubstitution).simplify();
            if (k.isIntegerConstantOrRationalConstant()) {
                break;
            }
        }

        if (!k.isIntegerConstantOrRationalConstant() || k.equals(ZERO)) {
            ExpressionCollection substitutedSummands = new ExpressionCollection();
            Expression substitutedSummand;
            for (int i = 0; i < summandsF.getBound(); i++) {
                substitutedSummand = substitute(summandsF.get(i), var, substitution, false);
                substitutedSummands.put(i, substitutedSummand);
            }
            return SimplifyUtilities.produceSum(substitutedSummands);
        }

        if (!k.equals(ONE)) {
            for (int i = 0; i < nonConstantSummandsSubstitution.getBound(); i++) {
                nonConstantSummandsSubstitution.put(i, k.mult(nonConstantSummandsSubstitution.get(i)).simplify());
            }
        }

        if (beginning) {

            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.order_difference_and_division);
            simplifyTypes.add(TypeSimplify.order_sums_and_products);
            simplifyTypes.add(TypeSimplify.simplify_trivial);
            simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
            simplifyTypes.add(TypeSimplify.simplify_collect_products);
            simplifyTypes.add(TypeSimplify.simplify_factorize_in_sums);
            simplifyTypes.add(TypeSimplify.simplify_factorize_in_differences);
            simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
            simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);

            Expression rest = f.sub(k.mult(substitution)).simplify(simplifyTypes);
            Expression restSubstituted = substitute(rest, var, substitution, beginning);
            return k.mult(Variable.create(getSubstitutionVariable(f))).add(restSubstituted);

        }

        ExpressionCollection fMinusMultipleOfSubstitution = SimplifyUtilities.difference(summandsF, nonConstantSummandsSubstitution);
        if (fMinusMultipleOfSubstitution.getBound() != summandsF.getBound() - nonConstantSummandsSubstitution.getBound()) {
            throw new NotSubstitutableException();
        }

        Expression restSubstituted = substitute(SimplifyUtilities.produceSum(fMinusMultipleOfSubstitution), var, substitution, false);
        Expression constantSummandOfSubstitution = SimplifyUtilities.produceProduct(SimplifyUtilities.getConstantSummands(substitution, var));
        return k.mult(Variable.create(getSubstitutionVariable(f))).add(restSubstituted).sub(k.mult(constantSummandOfSubstitution));

    }

    /**
     * Versucht, falls f eine Differenz ist, f durch einen Ausdruck von
     * substitution zu ersetzen.
     *
     * @throws EvaluationException
     */
    private static Expression substituteInDifference(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException, NotSubstitutableException {

        if (f.isNotDifference()) {
            throw new NotSubstitutableException();
        }

        ExpressionCollection summandsLeftF = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRightF = SimplifyUtilities.getSummandsRightInExpression(f);
        ExpressionCollection nonConstantSummandsLeftSubstitution = SimplifyUtilities.getNonConstantSummandsLeftInExpression(substitution, var);
        ExpressionCollection nonConstantSummandsRightSubstitution = SimplifyUtilities.getNonConstantSummandsRightInExpression(substitution, var);
        Expression firstNonConstantSummandInSubstitution;
        if (nonConstantSummandsLeftSubstitution.isEmpty()) {
            if (nonConstantSummandsRightSubstitution.isEmpty()) {
                // Sollte nie passieren, aber trotzdem sicherheitshalber.
                throw new NotSubstitutableException();
            }
            firstNonConstantSummandInSubstitution = nonConstantSummandsRightSubstitution.get(0);
        } else {
            firstNonConstantSummandInSubstitution = nonConstantSummandsLeftSubstitution.get(0);
        }

        boolean firstNonConstantFactorInSubstitutionIsInLeft = false;
        for (int i = 0; i < nonConstantSummandsLeftSubstitution.getBound(); i++) {
            if (nonConstantSummandsLeftSubstitution.get(i).contains(var)) {
                firstNonConstantSummandInSubstitution = nonConstantSummandsLeftSubstitution.get(i);
                firstNonConstantFactorInSubstitutionIsInLeft = true;
                break;
            }
        }
        if (!firstNonConstantFactorInSubstitutionIsInLeft) {
            for (int i = 0; i < nonConstantSummandsRightSubstitution.getBound(); i++) {
                if (nonConstantSummandsRightSubstitution.get(i).contains(var)) {
                    firstNonConstantSummandInSubstitution = nonConstantSummandsRightSubstitution.get(i);
                    break;
                }
            }
        }

        /*
         Nun wird geprüft, ob in summandsLeftF ein Summand auftaucht, welcher
         ein rationales Vielfaches von firstNonConstantSummandInSubstitution
         ist. Falls so ein Summand existiert, ist dieser eindeutig.
         */
        boolean potentialMultipleFoundInSummandsLeft = true;
        Expression k = ZERO;
        for (int i = 0; i < summandsLeftF.getBound(); i++) {
            k = summandsLeftF.get(i).div(firstNonConstantSummandInSubstitution).simplify();
            if (k.isIntegerConstantOrRationalConstant()) {
                break;
            }
        }
        if (!k.isIntegerConstantOrRationalConstant() || k.equals(ZERO)) {
            for (int i = 0; i < summandsRightF.getBound(); i++) {
                k = summandsRightF.get(i).div(firstNonConstantSummandInSubstitution).simplify();
                if (k.isIntegerConstantOrRationalConstant()) {
                    potentialMultipleFoundInSummandsLeft = false;
                    break;
                }
            }
        }

        if (!k.isIntegerConstantOrRationalConstant() || k.equals(ZERO)) {
            ExpressionCollection substitutedSummandsLeft = new ExpressionCollection();
            ExpressionCollection substitutedSummandsRight = new ExpressionCollection();
            Expression substitutedSummand;
            for (int i = 0; i < summandsLeftF.getBound(); i++) {
                substitutedSummand = substitute(summandsLeftF.get(i), var, substitution, false);
                substitutedSummandsLeft.put(i, substitutedSummand);
            }
            for (int i = 0; i < summandsRightF.getBound(); i++) {
                substitutedSummand = substitute(summandsRightF.get(i), var, substitution, false);
                substitutedSummandsRight.put(i, substitutedSummand);
            }
            return SimplifyUtilities.produceDifference(substitutedSummandsLeft, substitutedSummandsRight);
        }

        if (!k.equals(ONE)) {
            for (int i = 0; i < nonConstantSummandsLeftSubstitution.getBound(); i++) {
                nonConstantSummandsLeftSubstitution.put(i, k.mult(nonConstantSummandsLeftSubstitution.get(i)).simplify());
            }
            for (int i = 0; i < nonConstantSummandsRightSubstitution.getBound(); i++) {
                nonConstantSummandsRightSubstitution.put(i, k.mult(nonConstantSummandsRightSubstitution.get(i)).simplify());
            }
        }

        if (beginning) {

            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.order_difference_and_division);
            simplifyTypes.add(TypeSimplify.order_sums_and_products);
            simplifyTypes.add(TypeSimplify.simplify_trivial);
            simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
            simplifyTypes.add(TypeSimplify.simplify_collect_products);
            simplifyTypes.add(TypeSimplify.simplify_factorize_in_sums);
            simplifyTypes.add(TypeSimplify.simplify_factorize_in_differences);
            simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
            simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);

            if (potentialMultipleFoundInSummandsLeft != firstNonConstantFactorInSubstitutionIsInLeft) {
                k = k.mult(-1).simplify(simplifyTypes);
            }
            Expression rest = f.sub(k.mult(substitution)).simplify(simplifyTypes);
            Expression restSubstituted = substitute(rest, var, substitution, beginning);
            return k.mult(Variable.create(getSubstitutionVariable(f))).add(restSubstituted);

        }

        ExpressionCollection summandsLeftFMinusMultipleOfSubstitutionLeft;
        ExpressionCollection summandsRightFMinusMultipleOfSubstitutionRight;
        if (potentialMultipleFoundInSummandsLeft == firstNonConstantFactorInSubstitutionIsInLeft) {
            summandsLeftFMinusMultipleOfSubstitutionLeft = SimplifyUtilities.difference(summandsLeftF, nonConstantSummandsLeftSubstitution);
            summandsRightFMinusMultipleOfSubstitutionRight = SimplifyUtilities.difference(summandsRightF, nonConstantSummandsRightSubstitution);
            if (summandsLeftFMinusMultipleOfSubstitutionLeft.getBound() != summandsLeftF.getBound() - nonConstantSummandsLeftSubstitution.getBound() || summandsRightFMinusMultipleOfSubstitutionRight.getBound() != summandsRightF.getBound() - nonConstantSummandsRightSubstitution.getBound()) {
                throw new NotSubstitutableException();
            }
        } else {
            summandsLeftFMinusMultipleOfSubstitutionLeft = SimplifyUtilities.difference(summandsLeftF, nonConstantSummandsRightSubstitution);
            summandsRightFMinusMultipleOfSubstitutionRight = SimplifyUtilities.difference(summandsRightF, nonConstantSummandsLeftSubstitution);
            if (summandsLeftFMinusMultipleOfSubstitutionLeft.getBound() != summandsLeftF.getBound() - nonConstantSummandsRightSubstitution.getBound() || summandsRightFMinusMultipleOfSubstitutionRight.getBound() != summandsRightF.getBound() - nonConstantSummandsLeftSubstitution.getBound()) {
                throw new NotSubstitutableException();
            }
        }

        Expression restSubstituted = substitute(SimplifyUtilities.produceDifference(summandsLeftFMinusMultipleOfSubstitutionLeft, summandsRightFMinusMultipleOfSubstitutionRight), var, substitution, false);
        if (potentialMultipleFoundInSummandsLeft != firstNonConstantFactorInSubstitutionIsInLeft) {
            k = k.mult(-1).simplify();
        }
        Expression constantSummandOfSubstitution = SimplifyUtilities.produceDifference(SimplifyUtilities.getConstantSummandsLeftInExpression(substitution, var), SimplifyUtilities.getConstantSummandsRightInExpression(substitution, var));
        return k.mult(Variable.create(getSubstitutionVariable(f))).add((Expression) restSubstituted).sub(k.mult(constantSummandOfSubstitution));

    }

    /**
     * Versucht, falls f ein Produkt ist, f durch einen Ausdruck von
     * substitution zu ersetzen.
     *
     * @throws EvaluationException
     */
    private static Expression substituteInProduct(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException, NotSubstitutableException {

        ExpressionCollection factorsF = SimplifyUtilities.getFactors(f);
        ExpressionCollection nonConstantFactorsSubstitution = SimplifyUtilities.getNonConstantFactors(substitution, var);
        if (nonConstantFactorsSubstitution.isEmpty()) {
            // Sollte nie passieren, aber trotzdem sicherheitshalber.
            throw new NotSubstitutableException();
        }

        Expression firstNonConstantFactorInSubstitution = nonConstantFactorsSubstitution.get(0);
        Expression exponentOfFirstNonConstantFactor = Expression.ONE;
        if (firstNonConstantFactorInSubstitution.isPower()) {
            exponentOfFirstNonConstantFactor = ((BinaryOperation) firstNonConstantFactorInSubstitution).getRight();
            firstNonConstantFactorInSubstitution = ((BinaryOperation) firstNonConstantFactorInSubstitution).getLeft();
        }

        Expression k = ZERO;
        for (int i = 0; i < factorsF.getBound(); i++) {
            if (factorsF.get(i).isPower()) {
                if (((BinaryOperation) factorsF.get(i)).getLeft().equivalent(firstNonConstantFactorInSubstitution)) {
                    k = ((BinaryOperation) factorsF.get(i)).getRight().div(exponentOfFirstNonConstantFactor).simplify();
                }
            } else if (factorsF.get(i).equivalent(firstNonConstantFactorInSubstitution)) {
                k = ONE.div(exponentOfFirstNonConstantFactor).simplify();
            }
            if (!k.equals(ZERO) && k.isIntegerConstantOrRationalConstant()) {
                break;
            }
        }

        if (!k.isIntegerConstantOrRationalConstant() || k.equals(ZERO)) {
            ExpressionCollection substitutedFactors = new ExpressionCollection();
            Expression substitutedSummand;
            for (int i = 0; i < factorsF.getBound(); i++) {
                substitutedSummand = substitute(factorsF.get(i), var, substitution, false);
                substitutedFactors.put(i, substitutedSummand);
            }
            return SimplifyUtilities.produceProduct(substitutedFactors);
        }

        if (!k.equals(ONE)) {
            for (int i = 0; i < nonConstantFactorsSubstitution.getBound(); i++) {
                nonConstantFactorsSubstitution.put(i, nonConstantFactorsSubstitution.get(i).pow(k).simplify());
            }
        }

        if (beginning) {

            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.order_difference_and_division);
            simplifyTypes.add(TypeSimplify.order_sums_and_products);
            simplifyTypes.add(TypeSimplify.simplify_trivial);
            simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
            simplifyTypes.add(TypeSimplify.simplify_collect_products);
            simplifyTypes.add(TypeSimplify.simplify_factorize_in_sums);
            simplifyTypes.add(TypeSimplify.simplify_factorize_in_differences);
            simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
            simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);

            Expression rest = f.div(substitution.pow(k)).simplify(simplifyTypes);
            Expression restSubstituted = substitute(rest, var, substitution, beginning);
            return Variable.create(getSubstitutionVariable(f)).pow(k).mult(restSubstituted);

        }

        ExpressionCollection factorsOfFDividedByPowerOfSubstitution = SimplifyUtilities.difference(factorsF, nonConstantFactorsSubstitution);
        if (factorsOfFDividedByPowerOfSubstitution.getBound() != factorsF.getBound() - nonConstantFactorsSubstitution.getBound()) {
            throw new NotSubstitutableException();
        }

        Expression restSubstituted = substitute(SimplifyUtilities.produceProduct(factorsOfFDividedByPowerOfSubstitution), var, substitution, false);
        Expression constantFactorOfSubstitution = SimplifyUtilities.produceProduct(SimplifyUtilities.getConstantFactors(substitution, var));
        return Variable.create(getSubstitutionVariable(f)).pow(k).mult(restSubstituted).div(constantFactorOfSubstitution.pow(k));

    }

    /**
     * Versucht, falls f ein Quotient ist, f durch einen Ausdruck von
     * substitution zu ersetzen.
     */
    private static Expression substituteInQuotient(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException, NotSubstitutableException {

        if (f.isNotQuotient()) {
            throw new NotSubstitutableException();
        }

        ExpressionCollection factorsEnumeratorF = SimplifyUtilities.getFactorsOfEnumeratorInExpression(f);
        ExpressionCollection factorsDenominatorF = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
        ExpressionCollection nonConstantFactorsEnumeratorSubstitution = SimplifyUtilities.getNonConstantFactorsOfEnumeratorInExpression(substitution, var);
        ExpressionCollection nonConstantFactorsDenominatorSubstitution = SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(substitution, var);
        Expression firstNonConstantFactorInSubstitution;
        if (nonConstantFactorsEnumeratorSubstitution.isEmpty()) {
            if (nonConstantFactorsDenominatorSubstitution.isEmpty()) {
                // Sollte nie passieren, aber trotzdem sicherheitshalber.
                throw new NotSubstitutableException();
            }
            firstNonConstantFactorInSubstitution = nonConstantFactorsDenominatorSubstitution.get(0);
        } else {
            firstNonConstantFactorInSubstitution = nonConstantFactorsEnumeratorSubstitution.get(0);
        }

        boolean firstNonConstantFactorInSubstitutionIsInEnumerator = false;
        for (int i = 0; i < nonConstantFactorsEnumeratorSubstitution.getBound(); i++) {
            if (nonConstantFactorsEnumeratorSubstitution.get(i).contains(var)) {
                firstNonConstantFactorInSubstitution = nonConstantFactorsEnumeratorSubstitution.get(i);
                firstNonConstantFactorInSubstitutionIsInEnumerator = true;
                break;
            }
        }
        if (!firstNonConstantFactorInSubstitutionIsInEnumerator) {
            for (int i = 0; i < nonConstantFactorsDenominatorSubstitution.getBound(); i++) {
                if (nonConstantFactorsDenominatorSubstitution.get(i).contains(var)) {
                    firstNonConstantFactorInSubstitution = nonConstantFactorsDenominatorSubstitution.get(i);
                    break;
                }
            }
        }

        /*
         Nun wird geprüft, ob in factorsEnumeratorF ein Faktor auftaucht,
         welcher eine Potenz von firstNonConstantFactorInSubstitutionIsInLeft
         ist. Falls so ein Faktor existiert, ist dieser eindeutig.
         */
        boolean potentialPowerFoundInFactorsDenominator = false;
        Expression k = ZERO;
        for (int i = 0; i < factorsEnumeratorF.getBound(); i++) {
            k = isPositiveIntegerPower(firstNonConstantFactorInSubstitution, factorsEnumeratorF.get(i)).simplify();
            if (k.isIntegerConstantOrRationalConstant()) {
                break;
            }
        }
        if (!k.isIntegerConstantOrRationalConstant() || !SimplifyAlgebraicExpressionMethods.isAdmissibleExponent(k) || k.equals(ZERO)) {
            for (int i = 0; i < factorsDenominatorF.getBound(); i++) {
                k = isPositiveIntegerPower(firstNonConstantFactorInSubstitution, factorsDenominatorF.get(i)).simplify();
                if (k.isIntegerConstantOrRationalConstant()) {
                    potentialPowerFoundInFactorsDenominator = true;
                    break;
                }
            }
        }
        if (!k.isIntegerConstantOrRationalConstant() || !SimplifyAlgebraicExpressionMethods.isAdmissibleExponent(k) || k.equals(ZERO)) {
            ExpressionCollection substitutedFactorsLeft = new ExpressionCollection();
            ExpressionCollection substitutedFactorsRight = new ExpressionCollection();
            Expression substitutedFactor;
            for (int i = 0; i < factorsEnumeratorF.getBound(); i++) {
                substitutedFactor = substitute(factorsEnumeratorF.get(i), var, substitution, false);
                substitutedFactorsLeft.put(i, substitutedFactor);
            }
            for (int i = 0; i < factorsDenominatorF.getBound(); i++) {
                substitutedFactor = substitute(factorsDenominatorF.get(i), var, substitution, false);
                substitutedFactorsRight.put(i, substitutedFactor);
            }
            return SimplifyUtilities.produceQuotient(substitutedFactorsLeft, substitutedFactorsRight);
        }

        Expression exponent = (Expression) k;
        if (potentialPowerFoundInFactorsDenominator && firstNonConstantFactorInSubstitutionIsInEnumerator) {
            exponent = exponent.mult(-1).simplify();
        }

        if (!exponent.equals(ONE)) {
            for (int i = 0; i < nonConstantFactorsEnumeratorSubstitution.getBound(); i++) {
                nonConstantFactorsEnumeratorSubstitution.put(i, nonConstantFactorsEnumeratorSubstitution.get(i).pow(exponent).simplify());
            }
            for (int i = 0; i < nonConstantFactorsDenominatorSubstitution.getBound(); i++) {
                nonConstantFactorsDenominatorSubstitution.put(i, nonConstantFactorsDenominatorSubstitution.get(i).pow(exponent).simplify());
            }
        }

        if (beginning) {

            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.order_difference_and_division);
            simplifyTypes.add(TypeSimplify.order_sums_and_products);
            simplifyTypes.add(TypeSimplify.simplify_trivial);
            simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
            simplifyTypes.add(TypeSimplify.simplify_collect_products);
            simplifyTypes.add(TypeSimplify.simplify_factorize_in_sums);
            simplifyTypes.add(TypeSimplify.simplify_factorize_in_differences);
            simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
            simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);

            Expression rest = f.div(substitution.pow(exponent)).simplify(simplifyTypes);
            Expression restSubstituted = substitute(rest, var, substitution, beginning);
            return Variable.create(getSubstitutionVariable(f)).pow(exponent).mult(restSubstituted);

        }

        ExpressionCollection factorsEnumeratorFDividedByPowerOfSubstitutionEnumerator = SimplifyUtilities.difference(factorsEnumeratorF, nonConstantFactorsEnumeratorSubstitution);
        ExpressionCollection factorsDenominatorFDividedByPowerOfSubstitutionDenominator = SimplifyUtilities.difference(factorsDenominatorF, nonConstantFactorsDenominatorSubstitution);
        if (factorsEnumeratorFDividedByPowerOfSubstitutionEnumerator.getBound() != factorsEnumeratorF.getBound() - nonConstantFactorsEnumeratorSubstitution.getBound() || factorsDenominatorFDividedByPowerOfSubstitutionDenominator.getBound() != factorsDenominatorF.getBound() - nonConstantFactorsDenominatorSubstitution.getBound()) {
            throw new NotSubstitutableException();
        }

        Expression restSubstituted = substitute(SimplifyUtilities.produceQuotient(factorsEnumeratorFDividedByPowerOfSubstitutionEnumerator, factorsDenominatorFDividedByPowerOfSubstitutionDenominator), var, substitution, false);
        Expression constantFactorOfSubstitution = SimplifyUtilities.produceQuotient(SimplifyUtilities.getConstantFactorsOfEnumeratorInExpression(substitution, var), SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(substitution, var));
        return Variable.create(getSubstitutionVariable(f)).pow(exponent).mult(restSubstituted).div(constantFactorOfSubstitution.pow(exponent));

    }

    /**
     * Versucht, falls f eine Potenz ist, f durch einen Ausdruck von
     * substitution zu ersetzen.
     *
     * @throws EvaluationException
     */
    private static Expression substituteInPower(Expression f, String var, Expression substitution) throws EvaluationException, NotSubstitutableException {

        if (f.isNotPower()) {
            throw new NotSubstitutableException();
        }

        try {
            Expression fIsIntegerPowerOfSubstitution = isPositiveIntegerPower(substitution, f);
            return Variable.create(getSubstitutionVariable(f)).pow(fIsIntegerPowerOfSubstitution);
        } catch (NotSubstitutableException e) {
        }

        if (!((BinaryOperation) f).getRight().contains(var) && ((BinaryOperation) f).getLeft().contains(var) && !(((BinaryOperation) f).getLeft() instanceof Variable)) {
            Expression baseSubstituted = substitute(((BinaryOperation) f).getLeft(), var, substitution, false);
            return baseSubstituted.pow(((BinaryOperation) f).getRight());
        }
        
        if (!((BinaryOperation) f).getLeft().contains(var) && ((BinaryOperation) f).getRight().contains(var) && !(((BinaryOperation) f).getRight() instanceof Variable)) {
            Expression exponentSubstituted = substitute(((BinaryOperation) f).getRight(), var, substitution, false);
            return ((BinaryOperation) f).getLeft().pow((Expression) exponentSubstituted);
        }
        
        if (f.isPower() && !((BinaryOperation) f).getLeft().contains(var) && ((BinaryOperation) f).getRight().contains(var) && substitution.isPower() && !((BinaryOperation) substitution).getLeft().contains(var) && ((BinaryOperation) substitution).getRight().contains(var)) {
            Expression c = ((BinaryOperation) f).getRight().diff(var).simplify();
            if (c.contains(var)) {
                throw new NotSubstitutableException();
            }
            Expression d = ((BinaryOperation) f).getRight().sub(c.mult(Variable.create(var))).simplify();
            if (d.contains(var)) {
                throw new NotSubstitutableException();
            }
            Expression p = ((BinaryOperation) f).getRight().diff(var).simplify();
            if (p.contains(var)) {
                throw new NotSubstitutableException();
            }
            Expression q = ((BinaryOperation) f).getRight().sub(p.mult(Variable.create(var))).simplify();
            if (q.contains(var)) {
                throw new NotSubstitutableException();
            }
            Expression a = ((BinaryOperation) f).getLeft();
            Expression b = ((BinaryOperation) substitution).getLeft();
            Expression factor = a.pow(d.sub(c.mult(q).div(p))).simplify();
            Expression exponent = a.ln().mult(c).div(b.ln().mult(p)).simplify();
            return factor.mult(Variable.create(getSubstitutionVariable(f)).pow(exponent));
        }
        
        throw new NotSubstitutableException();
        
    }

    /**
     * Versucht, falls f eine Funktion ist, diese durch einen Ausdruck von subst
     * zu ersetzen.
     *
     * @throws EvaluationException
     */
    private static Expression substituteInFunction(Expression f, String var, Expression substitution) throws EvaluationException, NotSubstitutableException {

        if (!(f instanceof Function)) {
            throw new NotSubstitutableException();
        }

        if (f.isFunction(TypeFunction.exp) && substitution.isFunction(TypeFunction.exp)) {

            String substVar = getSubstitutionVariable(f);
            Expression expArgumentSubstituted = substitute(((Function) f).getLeft(), var, ((Function) substitution).getLeft(), false);
            Expression derivativeOfExpArgumentBySubstVar = expArgumentSubstituted.diff(substVar).simplify();

            if (derivativeOfExpArgumentBySubstVar.isIntegerConstant() && !derivativeOfExpArgumentBySubstVar.equals(Expression.ZERO)) {
                Expression constantRest = expArgumentSubstituted.replaceVariable(substVar, ZERO).simplify();
                if (constantRest.equals(ZERO)) {
                    return Variable.create(substVar).pow(derivativeOfExpArgumentBySubstVar);
                }
                return constantRest.exp().mult(Variable.create(substVar).pow(derivativeOfExpArgumentBySubstVar));
            }

        }

        if (f.isFunction(TypeFunction.sin) && substitution.isFunction(TypeFunction.cosec) && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return Expression.ONE.div(Variable.create(getSubstitutionVariable(f)));
        }
        if (f.isFunction(TypeFunction.cosec) && substitution.isFunction(TypeFunction.sin) && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return Expression.ONE.div(Variable.create(getSubstitutionVariable(f)));
        }
        if (f.isFunction(TypeFunction.cos) && substitution.isFunction(TypeFunction.sec) && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return Expression.ONE.div(Variable.create(getSubstitutionVariable(f)));
        }
        if (f.isFunction(TypeFunction.sec) && substitution.isFunction(TypeFunction.cos) && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return Expression.ONE.div(Variable.create(getSubstitutionVariable(f)));
        }
        if (f.isFunction(TypeFunction.tan) && substitution.isFunction(TypeFunction.cot) && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return Expression.ONE.div(Variable.create(getSubstitutionVariable(f)));
        }
        if (f.isFunction(TypeFunction.cot) && substitution.isFunction(TypeFunction.tan) && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return Expression.ONE.div(Variable.create(getSubstitutionVariable(f)));
        }
        if (f.isFunction(TypeFunction.sinh) && substitution.isFunction(TypeFunction.cosech) && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return Expression.ONE.div(Variable.create(getSubstitutionVariable(f)));
        }
        if (f.isFunction(TypeFunction.cosech) && substitution.isFunction(TypeFunction.sinh) && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return Expression.ONE.div(Variable.create(getSubstitutionVariable(f)));
        }
        if (f.isFunction(TypeFunction.cosh) && substitution.isFunction(TypeFunction.sech) && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return Expression.ONE.div(Variable.create(getSubstitutionVariable(f)));
        }
        if (f.isFunction(TypeFunction.sech) && substitution.isFunction(TypeFunction.cosh) && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return Expression.ONE.div(Variable.create(getSubstitutionVariable(f)));
        }
        if (f.isFunction(TypeFunction.tanh) && substitution.isFunction(TypeFunction.coth) && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return Expression.ONE.div(Variable.create(getSubstitutionVariable(f)));
        }
        if (f.isFunction(TypeFunction.coth) && substitution.isFunction(TypeFunction.tanh) && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return Expression.ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        Expression fArgumentSubstituted = substitute(((Function) f).getLeft(), var, substitution, false);
        return new Function(fArgumentSubstituted, ((Function) f).getType());

    }

}
