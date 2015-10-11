package substitutionmethods;

import exceptions.EvaluationException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import expressionbuilder.TypeSimplify;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyAlgebraicExpressionMethods;
import expressionsimplifymethods.SimplifyUtilities;
import java.util.HashSet;

public abstract class SubstitutionUtilities {

    /**
     * In f sind Variablen enthalten, unter anderem möglicherweise auch
     * "Parametervariablen" X_1, X_2, .... Diese Funktion liefert dasjenige X_i
     * mit dem kleinsten Index i, welches in f noch nicht vorkommt.
     */
    public static String getSubstitutionVariable(Expression f) {
        String var = "X_";
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
    private static Object isPositiveIntegerPower(Expression f, Expression g) throws EvaluationException {

        if (f.equivalent(g)) {
            return Expression.ONE;
        }

        if (g.isPower() && f.equivalent(((BinaryOperation) g).getLeft())
                && ((BinaryOperation) g).getRight().isIntegerConstant()) {
            return ((BinaryOperation) g).getRight();
        }

        if (f.isPower() && g.equivalent(((BinaryOperation) f).getLeft())) {
            Expression exponent = Expression.ONE.div(((BinaryOperation) f).getRight()).simplify();
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

        return false;

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
    public static Object substitute(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {
        if (!f.contains(var)) {
            return f;
        }
        if (f.equivalent(substitution)) {
            return Variable.create(getSubstitutionVariable(f));
        }
        if (f.equals(Variable.create(var))) {
            Object variableSubstituted = substituteVariable(var, substitution, beginning);
            if (variableSubstituted instanceof Expression) {
                return variableSubstituted;
            }
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
            return substituteInPower(f, var, substitution, beginning);
        }
        if (f.isFunction()) {
            return substituteInFunction(f, var, substitution, beginning);
        }
        return false;
    }

    /**
     * Hier wird versucht, x = var durch substitution = x/a + b mit ganzem a zu
     * substituieren (also x = a*substitution - a*b).
     */
    private static Object substituteVariable(String var, Expression substitution, boolean beginning) throws EvaluationException {
        if (!beginning){
            return false;
        }
        Expression derivative = substitution.diff(var).simplify();
        if (derivative.equals(Expression.ZERO)) {
            return false;
        }
        Expression reciprocalOfDerivative = Expression.ONE.div(derivative).simplify();
        if (reciprocalOfDerivative.isIntegerConstant()) {
            Expression rest = substitution.replaceVariable(var, Expression.ZERO).simplify();
            String substVar = getSubstitutionVariable(Variable.create(var));
            return reciprocalOfDerivative.mult(Variable.create(substVar)).sub(reciprocalOfDerivative.mult(rest));
        }
        return false;
    }

    /**
     * Versucht, falls f eine Summe ist, f durch einen Ausdruck von substitution
     * zu ersetzen.
     *
     * @throws EvaluationException
     */
    private static Object substituteInSum(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {
        ExpressionCollection summandsF = SimplifyUtilities.getSummands(f);
        ExpressionCollection nonConstantSummandsSubstitution = SimplifyUtilities.getNonConstantSummands(substitution, var);
        if (nonConstantSummandsSubstitution.isEmpty()) {
            // Sollte nie passieren, aber trotzdem sicherheitshalber.
            return false;
        }
        Expression firstNonConstantSummandInSubstitution = nonConstantSummandsSubstitution.get(0);
        Expression k = Expression.ZERO;
        for (int i = 0; i < summandsF.getBound(); i++) {
            k = summandsF.get(i).div(firstNonConstantSummandInSubstitution).simplify();
            if (k.isIntegerConstantOrRationalConstant()) {
                break;
            }
        }
        if (!k.isIntegerConstantOrRationalConstant() || k.equals(Expression.ZERO)) {
            ExpressionCollection substitutedSummands = new ExpressionCollection();
            Object substitutedSummand;
            for (int i = 0; i < summandsF.getBound(); i++) {
                substitutedSummand = substitute(summandsF.get(i), var, substitution, false);
                if (substitutedSummand instanceof Boolean) {
                    return false;
                }
                substitutedSummands.put(i, (Expression) substitutedSummand);
            }
            return SimplifyUtilities.produceSum(substitutedSummands);
        }
        if (!k.equals(Expression.ONE)) {
            for (int i = 0; i < nonConstantSummandsSubstitution.getBound(); i++) {
                nonConstantSummandsSubstitution.put(i, k.mult(nonConstantSummandsSubstitution.get(i)).simplify());
            }
        }
        if (beginning) {

            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.order_difference_and_division);
            simplifyTypes.add(TypeSimplify.order_sums_and_products);
            simplifyTypes.add(TypeSimplify.simplify_trivial);
            simplifyTypes.add(TypeSimplify.simplify_powers);
            simplifyTypes.add(TypeSimplify.collect_products);
            simplifyTypes.add(TypeSimplify.factorize_in_sums);
            simplifyTypes.add(TypeSimplify.factorize_in_differences);
            simplifyTypes.add(TypeSimplify.reduce_quotients);
            simplifyTypes.add(TypeSimplify.reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);

            if (k.equals(Expression.ONE)) {
                Expression rest = f.sub(substitution).simplify(simplifyTypes);
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return Variable.create(getSubstitutionVariable(f)).add((Expression) restSubstituted);
                }
            } else {
                Expression rest = f.sub(k.mult(substitution)).simplify(simplifyTypes);
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return k.mult(Variable.create(getSubstitutionVariable(f))).add((Expression) restSubstituted);
                }
            }
            return false;
        }
        ExpressionCollection fMinusMultipleOfSubstitution = SimplifyUtilities.difference(summandsF, nonConstantSummandsSubstitution);
        if (fMinusMultipleOfSubstitution.getBound() != summandsF.getBound() - nonConstantSummandsSubstitution.getBound()) {
            return false;
        }
        Object restSubstituted = substitute(SimplifyUtilities.produceSum(fMinusMultipleOfSubstitution), var, substitution, false);
        if (restSubstituted instanceof Expression) {
            Expression constantSummandOfSubstitution = SimplifyUtilities.produceProduct(SimplifyUtilities.getConstantSummands(substitution, var));
            if (k.equals(Expression.ONE)) {
                return Variable.create(getSubstitutionVariable(f)).add((Expression) restSubstituted).sub(constantSummandOfSubstitution);
            }
            return k.mult(Variable.create(getSubstitutionVariable(f))).add((Expression) restSubstituted).sub(k.mult(constantSummandOfSubstitution));
        }
        return false;
    }

    /**
     * Versucht, falls f eine Differenz ist, f durch einen Ausdruck von
     * substitution zu ersetzen.
     *
     * @throws EvaluationException
     */
    private static Object substituteInDifference(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {
        if (f.isNotDifference()) {
            return false;
        }
        ExpressionCollection summandsLeftF = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRightF = SimplifyUtilities.getSummandsRightInExpression(f);
        ExpressionCollection nonConstantSummandsLeftSubstitution = SimplifyUtilities.getNonConstantSummandsLeftInExpression(substitution, var);
        ExpressionCollection nonConstantSummandsRightSubstitution = SimplifyUtilities.getNonConstantSummandsRightInExpression(substitution, var);
        Expression firstNonConstantSummandInSubstitution;
        if (nonConstantSummandsLeftSubstitution.isEmpty()) {
            if (nonConstantSummandsRightSubstitution.isEmpty()) {
                // Sollte nie passieren, aber trotzdem sicherheitshalber.
                return false;
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
        Expression k = Expression.ZERO;
        for (int i = 0; i < summandsLeftF.getBound(); i++) {
            k = summandsLeftF.get(i).div(firstNonConstantSummandInSubstitution).simplify();
            if (k.isIntegerConstantOrRationalConstant()) {
                break;
            }
        }
        if (!k.isIntegerConstantOrRationalConstant() || k.equals(Expression.ZERO)) {
            for (int i = 0; i < summandsRightF.getBound(); i++) {
                k = summandsRightF.get(i).div(firstNonConstantSummandInSubstitution).simplify();
                if (k.isIntegerConstantOrRationalConstant()) {
                    potentialMultipleFoundInSummandsLeft = false;
                    break;
                }
            }
        }
        if (!k.isIntegerConstantOrRationalConstant() || k.equals(Expression.ZERO)) {
            ExpressionCollection substitutedSummandsLeft = new ExpressionCollection();
            ExpressionCollection substitutedSummandsRight = new ExpressionCollection();
            Object substitutedSummand;
            for (int i = 0; i < summandsLeftF.getBound(); i++) {
                substitutedSummand = substitute(summandsLeftF.get(i), var, substitution, false);
                if (substitutedSummand instanceof Boolean) {
                    return false;
                }
                substitutedSummandsLeft.put(i, (Expression) substitutedSummand);
            }
            for (int i = 0; i < summandsRightF.getBound(); i++) {
                substitutedSummand = substitute(summandsRightF.get(i), var, substitution, false);
                if (substitutedSummand instanceof Boolean) {
                    return false;
                }
                substitutedSummandsRight.put(i, (Expression) substitutedSummand);
            }
            return SimplifyUtilities.produceDifference(substitutedSummandsLeft, substitutedSummandsRight);
        }
        if (!k.equals(Expression.ONE)) {
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
            simplifyTypes.add(TypeSimplify.simplify_powers);
            simplifyTypes.add(TypeSimplify.collect_products);
            simplifyTypes.add(TypeSimplify.factorize_in_sums);
            simplifyTypes.add(TypeSimplify.factorize_in_differences);
            simplifyTypes.add(TypeSimplify.reduce_quotients);
            simplifyTypes.add(TypeSimplify.reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);

            if (potentialMultipleFoundInSummandsLeft != firstNonConstantFactorInSubstitutionIsInLeft) {
                k = k.mult(-1).simplify(simplifyTypes);
            }
            if (k.equals(Expression.ONE)) {
                Expression rest = f.sub(substitution).simplify(simplifyTypes);
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return Variable.create(getSubstitutionVariable(f)).add((Expression) restSubstituted);
                }
            } else {
                Expression rest = f.sub(k.mult(substitution)).simplify(simplifyTypes);
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return k.mult(Variable.create(getSubstitutionVariable(f))).add((Expression) restSubstituted);
                }
            }
            return false;
        }
        ExpressionCollection summandsLeftFMinusMultipleOfSubstitutionLeft;
        ExpressionCollection summandsRightFMinusMultipleOfSubstitutionRight;
        if (potentialMultipleFoundInSummandsLeft == firstNonConstantFactorInSubstitutionIsInLeft) {
            summandsLeftFMinusMultipleOfSubstitutionLeft = SimplifyUtilities.difference(summandsLeftF, nonConstantSummandsLeftSubstitution);
            summandsRightFMinusMultipleOfSubstitutionRight = SimplifyUtilities.difference(summandsRightF, nonConstantSummandsRightSubstitution);
            if (summandsLeftFMinusMultipleOfSubstitutionLeft.getBound() != summandsLeftF.getBound() - nonConstantSummandsLeftSubstitution.getBound() || summandsRightFMinusMultipleOfSubstitutionRight.getBound() != summandsRightF.getBound() - nonConstantSummandsRightSubstitution.getBound()) {
                return false;
            }
        } else {
            summandsLeftFMinusMultipleOfSubstitutionLeft = SimplifyUtilities.difference(summandsLeftF, nonConstantSummandsRightSubstitution);
            summandsRightFMinusMultipleOfSubstitutionRight = SimplifyUtilities.difference(summandsRightF, nonConstantSummandsLeftSubstitution);
            if (summandsLeftFMinusMultipleOfSubstitutionLeft.getBound() != summandsLeftF.getBound() - nonConstantSummandsRightSubstitution.getBound() || summandsRightFMinusMultipleOfSubstitutionRight.getBound() != summandsRightF.getBound() - nonConstantSummandsLeftSubstitution.getBound()) {
                return false;
            }
        }
        Object restSubstituted = substitute(SimplifyUtilities.produceDifference(summandsLeftFMinusMultipleOfSubstitutionLeft, summandsRightFMinusMultipleOfSubstitutionRight), var, substitution, false);
        if (restSubstituted instanceof Expression) {
            if (potentialMultipleFoundInSummandsLeft != firstNonConstantFactorInSubstitutionIsInLeft) {
                k = k.mult(-1).simplify();
            }
            Expression constantSummandOfSubstitution = SimplifyUtilities.produceDifference(SimplifyUtilities.getConstantSummandsLeftInExpression(substitution, var), SimplifyUtilities.getConstantSummandsRightInExpression(substitution, var));
            if (k.equals(Expression.ONE)) {
                return Variable.create(getSubstitutionVariable(f)).add((Expression) restSubstituted).sub(constantSummandOfSubstitution);
            }
            return k.mult(Variable.create(getSubstitutionVariable(f))).add((Expression) restSubstituted).sub(k.mult(constantSummandOfSubstitution));
        }
        return false;
    }

    /**
     * Versucht, falls f ein Produkt ist, f durch einen Ausdruck von
     * substitution zu ersetzen.
     *
     * @throws EvaluationException
     */
    private static Object substituteInProduct(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {
        ExpressionCollection factorsF = SimplifyUtilities.getFactors(f);
        ExpressionCollection nonConstantFactorsSubstitution = SimplifyUtilities.getNonConstantFactors(substitution, var);
        if (nonConstantFactorsSubstitution.isEmpty()) {
            // Sollte nie passieren, aber trotzdem sicherheitshalber.
            return false;
        }
        Expression firstNonConstantFactorInSubstitution = nonConstantFactorsSubstitution.get(0);
        Expression exponentOfFirstNonConstantFactor = Expression.ONE;
        if (firstNonConstantFactorInSubstitution.isPower()) {
            exponentOfFirstNonConstantFactor = ((BinaryOperation) firstNonConstantFactorInSubstitution).getRight();
            firstNonConstantFactorInSubstitution = ((BinaryOperation) firstNonConstantFactorInSubstitution).getLeft();
        }
        Expression k = Expression.ZERO;
        for (int i = 0; i < factorsF.getBound(); i++) {
            if (factorsF.get(i).isPower()) {
                if (((BinaryOperation) factorsF.get(i)).getLeft().equivalent(firstNonConstantFactorInSubstitution)) {
                    k = ((BinaryOperation) factorsF.get(i)).getRight().div(exponentOfFirstNonConstantFactor).simplify();
                }
            } else if (factorsF.get(i).equivalent(firstNonConstantFactorInSubstitution)) {
                k = Expression.ONE.div(exponentOfFirstNonConstantFactor).simplify();
            }
            if (!k.equals(Expression.ZERO) && k.isIntegerConstantOrRationalConstant()) {
                break;
            }
        }
        if (!k.isIntegerConstantOrRationalConstant() || k.equals(Expression.ZERO)) {
            ExpressionCollection substitutedFactors = new ExpressionCollection();
            Object substitutedSummand;
            for (int i = 0; i < factorsF.getBound(); i++) {
                substitutedSummand = substitute(factorsF.get(i), var, substitution, false);
                if (substitutedSummand instanceof Boolean) {
                    return false;
                }
                substitutedFactors.put(i, (Expression) substitutedSummand);
            }
            return SimplifyUtilities.produceProduct(substitutedFactors);
        }
        if (!k.equals(Expression.ONE)) {
            for (int i = 0; i < nonConstantFactorsSubstitution.getBound(); i++) {
                nonConstantFactorsSubstitution.put(i, nonConstantFactorsSubstitution.get(i).pow(k).simplify());
            }
        }
        if (beginning) {

            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.order_difference_and_division);
            simplifyTypes.add(TypeSimplify.order_sums_and_products);
            simplifyTypes.add(TypeSimplify.simplify_trivial);
            simplifyTypes.add(TypeSimplify.simplify_powers);
            simplifyTypes.add(TypeSimplify.collect_products);
            simplifyTypes.add(TypeSimplify.factorize_in_sums);
            simplifyTypes.add(TypeSimplify.factorize_in_differences);
            simplifyTypes.add(TypeSimplify.reduce_quotients);
            simplifyTypes.add(TypeSimplify.reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);

            if (k.equals(Expression.ONE)) {
                Expression rest = f.div(substitution).simplify(simplifyTypes);
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return Variable.create(getSubstitutionVariable(f)).mult((Expression) restSubstituted);
                }
            } else {
                Expression rest = f.div(substitution.pow(k)).simplify(simplifyTypes);
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return Variable.create(getSubstitutionVariable(f)).pow(k).mult((Expression) restSubstituted);
                }
            }
            return false;
        }
        ExpressionCollection factorsOfFDividedByPowerOfSubstitution = SimplifyUtilities.difference(factorsF, nonConstantFactorsSubstitution);
        if (factorsOfFDividedByPowerOfSubstitution.getBound() != factorsF.getBound() - nonConstantFactorsSubstitution.getBound()) {
            return false;
        }
        Object restSubstituted = substitute(SimplifyUtilities.produceProduct(factorsOfFDividedByPowerOfSubstitution), var, substitution, false);
        if (restSubstituted instanceof Expression) {
            Expression constantFactorOfSubstitution = SimplifyUtilities.produceProduct(SimplifyUtilities.getConstantFactors(substitution, var));
            if (k.equals(Expression.ONE)) {
                return Variable.create(getSubstitutionVariable(f)).mult((Expression) restSubstituted).div(constantFactorOfSubstitution);
            }
            return Variable.create(getSubstitutionVariable(f)).pow(k).mult((Expression) restSubstituted).div(constantFactorOfSubstitution.pow(k));
        }
        return false;
    }

    /**
     * Versucht, falls f ein Quotient ist, f durch einen Ausdruck von
     * substitution zu ersetzen.
     */
    private static Object substituteInQuotient(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {
        if (f.isNotQuotient()) {
            return false;
        }
        ExpressionCollection factorsEnumeratorF = SimplifyUtilities.getFactorsOfEnumeratorInExpression(f);
        ExpressionCollection factorsDenominatorF = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
        ExpressionCollection nonConstantFactorsEnumeratorSubstitution = SimplifyUtilities.getNonConstantFactorsOfEnumeratorInExpression(substitution, var);
        ExpressionCollection nonConstantFactorsDenominatorSubstitution = SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(substitution, var);
        Expression firstNonConstantFactorInSubstitution;
        if (nonConstantFactorsEnumeratorSubstitution.isEmpty()) {
            if (nonConstantFactorsDenominatorSubstitution.isEmpty()) {
                // Sollte nie passieren, aber trotzdem sicherheitshalber.
                return false;
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
        Object k = Expression.ZERO;
        for (int i = 0; i < factorsEnumeratorF.getBound(); i++) {
            k = isPositiveIntegerPower(firstNonConstantFactorInSubstitution, factorsEnumeratorF.get(i));
            if (k instanceof Expression) {
                k = ((Expression) k).simplify();
                if (((Expression) k).isIntegerConstantOrRationalConstant()) {
                    break;
                }
            }
        }
        if (!(k instanceof Expression) || !((Expression) k).isIntegerConstantOrRationalConstant() || !SimplifyAlgebraicExpressionMethods.isAdmissibleExponent((Expression) k) || k.equals(Expression.ZERO)) {
            for (int i = 0; i < factorsDenominatorF.getBound(); i++) {
                k = isPositiveIntegerPower(firstNonConstantFactorInSubstitution, factorsDenominatorF.get(i));
                if (k instanceof Expression) {
                    k = ((Expression) k).simplify();
                    if (((Expression) k).isIntegerConstantOrRationalConstant()) {
                        potentialPowerFoundInFactorsDenominator = true;
                        break;
                    }
                }
            }
        }
        if (!(k instanceof Expression) || !((Expression) k).isIntegerConstantOrRationalConstant() || !SimplifyAlgebraicExpressionMethods.isAdmissibleExponent((Expression) k) || k.equals(Expression.ZERO)) {
            ExpressionCollection substitutedFactorsLeft = new ExpressionCollection();
            ExpressionCollection substitutedFactorsRight = new ExpressionCollection();
            Object substitutedFactor;
            for (int i = 0; i < factorsEnumeratorF.getBound(); i++) {
                substitutedFactor = substitute(factorsEnumeratorF.get(i), var, substitution, false);
                if (substitutedFactor instanceof Boolean) {
                    return false;
                }
                substitutedFactorsLeft.put(i, (Expression) substitutedFactor);
            }
            for (int i = 0; i < factorsDenominatorF.getBound(); i++) {
                substitutedFactor = substitute(factorsDenominatorF.get(i), var, substitution, false);
                if (substitutedFactor instanceof Boolean) {
                    return false;
                }
                substitutedFactorsRight.put(i, (Expression) substitutedFactor);
            }
            return SimplifyUtilities.produceQuotient(substitutedFactorsLeft, substitutedFactorsRight);
        }
        Expression exponent = (Expression) k;
        if (potentialPowerFoundInFactorsDenominator && firstNonConstantFactorInSubstitutionIsInEnumerator) {
            exponent = exponent.mult(-1).simplify();
        }
        if (!exponent.equals(Expression.ONE)) {
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
            simplifyTypes.add(TypeSimplify.simplify_powers);
            simplifyTypes.add(TypeSimplify.collect_products);
            simplifyTypes.add(TypeSimplify.factorize_in_sums);
            simplifyTypes.add(TypeSimplify.factorize_in_differences);
            simplifyTypes.add(TypeSimplify.reduce_quotients);
            simplifyTypes.add(TypeSimplify.reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);

            if (exponent.equals(Expression.ONE)) {
                Expression rest = f.div(substitution).simplify(simplifyTypes);
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return Variable.create(getSubstitutionVariable(f)).mult((Expression) restSubstituted);
                }
            } else {
                Expression rest = f.div(substitution.pow(exponent)).simplify(simplifyTypes);
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return Variable.create(getSubstitutionVariable(f)).pow(exponent).mult((Expression) restSubstituted);
                }
            }
            return false;
        }
        ExpressionCollection factorsEnumeratorFDividedByPowerOfSubstitutionEnumerator = SimplifyUtilities.difference(factorsEnumeratorF, nonConstantFactorsEnumeratorSubstitution);
        ExpressionCollection factorsDenominatorFDividedByPowerOfSubstitutionDenominator = SimplifyUtilities.difference(factorsDenominatorF, nonConstantFactorsDenominatorSubstitution);
        if (factorsEnumeratorFDividedByPowerOfSubstitutionEnumerator.getBound() != factorsEnumeratorF.getBound() - nonConstantFactorsEnumeratorSubstitution.getBound() || factorsDenominatorFDividedByPowerOfSubstitutionDenominator.getBound() != factorsDenominatorF.getBound() - nonConstantFactorsDenominatorSubstitution.getBound()) {
            return false;
        }
        Object restSubstituted = substitute(SimplifyUtilities.produceQuotient(factorsEnumeratorFDividedByPowerOfSubstitutionEnumerator, factorsDenominatorFDividedByPowerOfSubstitutionDenominator), var, substitution, false);
        if (restSubstituted instanceof Expression) {
            Expression constantFactorOfSubstitution = SimplifyUtilities.produceQuotient(SimplifyUtilities.getConstantFactorsOfEnumeratorInExpression(substitution, var), SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(substitution, var));
            if (exponent.equals(Expression.ONE)) {
                return Variable.create(getSubstitutionVariable(f)).mult((Expression) restSubstituted).div(constantFactorOfSubstitution);
            }
            return Variable.create(getSubstitutionVariable(f)).pow(exponent).mult((Expression) restSubstituted).div(constantFactorOfSubstitution.pow(exponent));
        }
        return false;
    }

    /**
     * Versucht, falls f eine Potenz ist, f durch einen Ausdruck von
     * substitution zu ersetzen.
     *
     * @throws EvaluationException
     */
    private static Object substituteInPower(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {
        if (f.isNotPower()) {
            return false;
        }
        Object fIsIntegerPowerOfSubstitution = isPositiveIntegerPower(substitution, f);
        if (fIsIntegerPowerOfSubstitution instanceof Expression) {
            return Variable.create(getSubstitutionVariable(f)).pow((Expression) fIsIntegerPowerOfSubstitution);
        }
        if (!((BinaryOperation) f).getRight().contains(var) && ((BinaryOperation) f).getLeft().contains(var) && !(((BinaryOperation) f).getLeft() instanceof Variable)) {
            Object baseSubstituted = substitute(((BinaryOperation) f).getLeft(), var, substitution, false);
            if (baseSubstituted instanceof Boolean) {
                return false;
            }
            return ((Expression) baseSubstituted).pow(((BinaryOperation) f).getRight());
        }
        if (!((BinaryOperation) f).getLeft().contains(var) && ((BinaryOperation) f).getRight().contains(var) && !(((BinaryOperation) f).getRight() instanceof Variable)) {
            Object exponentSubstituted = substitute(((BinaryOperation) f).getRight(), var, substitution, false);
            if (exponentSubstituted instanceof Boolean) {
                return false;
            }
            return ((BinaryOperation) f).getLeft().pow((Expression) exponentSubstituted);
        }
        if (f.isPower() && !((BinaryOperation) f).getLeft().contains(var) && ((BinaryOperation) f).getRight().contains(var) && substitution.isPower() && !((BinaryOperation) substitution).getLeft().contains(var) && ((BinaryOperation) substitution).getRight().contains(var)) {
            Expression c = ((BinaryOperation) f).getRight().diff(var).simplify();
            if (c.contains(var)) {
                return false;
            }
            Expression d = ((BinaryOperation) f).getRight().sub(c.mult(Variable.create(var))).simplify();
            if (d.contains(var)) {
                return false;
            }
            Expression p = ((BinaryOperation) f).getRight().diff(var).simplify();
            if (p.contains(var)) {
                return false;
            }
            Expression q = ((BinaryOperation) f).getRight().sub(p.mult(Variable.create(var))).simplify();
            if (q.contains(var)) {
                return false;
            }
            Expression a = ((BinaryOperation) f).getLeft();
            Expression b = ((BinaryOperation) substitution).getLeft();
            Expression factor = a.pow(d.sub(c.mult(q).div(p))).simplify();
            Expression exponent = a.ln().mult(c).div(b.ln().mult(p)).simplify();
            return factor.mult(Variable.create(getSubstitutionVariable(f)).pow(exponent));
        }
        return false;
    }

    /**
     * Versucht, falls f eine Funktion ist, diese durch einen Ausdruck von subst
     * zu ersetzen.
     *
     * @throws EvaluationException
     */
    private static Object substituteInFunction(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {
        if (!(f instanceof Function)) {
            return false;
        }
        if (f.isFunction(TypeFunction.exp) && substitution.isFunction(TypeFunction.exp)) {
            String substVar = getSubstitutionVariable(f);
            Object expArgumentSubstituted = substitute(((Function) f).getLeft(), var, ((Function) substitution).getLeft(), false);
            if (expArgumentSubstituted instanceof Expression) {
                Expression derivativeOfExpArgumentBySubstVar = ((Expression) expArgumentSubstituted).diff(substVar).simplify();
                if (derivativeOfExpArgumentBySubstVar.isIntegerConstant() && !derivativeOfExpArgumentBySubstVar.equals(Expression.ZERO)) {
                    Expression constantRest = ((Expression) expArgumentSubstituted).replaceVariable(substVar, Expression.ZERO).simplify();
                    if (constantRest.equals(Expression.ZERO)) {
                        return Variable.create(substVar).pow(derivativeOfExpArgumentBySubstVar);
                    }
                    return new Function(constantRest, TypeFunction.exp).mult(Variable.create(substVar).pow(derivativeOfExpArgumentBySubstVar));
                }
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
        Object fArgumentSubstituted = substitute(((Function) f).getLeft(), var, substitution, false);
        if (fArgumentSubstituted instanceof Boolean) {
            return false;
        }
        return new Function((Expression) fArgumentSubstituted, ((Function) f).getType());
    }

}
