package abstractexpressions.expression.equation;

import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import exceptions.NotSubstitutableException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.TypeFunction;
import enums.TypeSimplify;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyBinaryOperationMethods;
import abstractexpressions.expression.utilities.SimplifyPolynomialMethods;
import abstractexpressions.expression.utilities.SimplifyRationalFunctionMethods;
import abstractexpressions.expression.utilities.SimplifyUtilities;
import java.math.BigInteger;
import java.util.HashSet;
import notations.NotationLoader;
import abstractexpressions.expression.substitution.SubstitutionUtilities;
import exceptions.NotAlgebraicallySolvableException;

public abstract class SolveGeneralEquationMethods {

    /**
     * Anzahl der Versuche, wie oft eine Gleichung versucht werden soll, gelöst
     * zu werden. Bei jedem Versuch wird dieser Parameter um 1 dekrementiert.
     * Vor dem Lösen einer Gleichung muss dieser gesetzt werden.
     */
    private static int solveTries;

    /**
     * Konstanten, die aussagen, ob eine Gleichung keine Lösungen besitzt oder
     * ob die Gleichung definitiv alle reellen Zahlen als Lösungen besitzt.
     */
    public static final ExpressionCollection ALL_REALS = new ExpressionCollection();
    public static final ExpressionCollection NO_SOLUTIONS = new ExpressionCollection();

    private static final HashSet<TypeSimplify> simplifyTypesEquation = getSimplifyTypesEquation();

    private static HashSet<TypeSimplify> getSimplifyTypesEquation() {
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_factorize);
        simplifyTypes.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_multiply_exponents);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        return simplifyTypes;
    }

    /**
     * Hauptprozedur zum algebraischen Lösen von Gleichungen f(x) = g(x).
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveEquation(Expression f, Expression g, String var) throws EvaluationException {
        solveTries = 100;
        return solveGeneralEquation(f, g, var);
    }

    /**
     * Interne Hauptprozedur zum algebraischen Lösen von Gleichungen f(x) =
     * g(x).
     *
     * @throws EvaluationException
     */
    protected static ExpressionCollection solveGeneralEquation(Expression f, Expression g, String var) throws EvaluationException {

//        System.out.println(solveTries + ": Löse allg. Gl. " + f.writeExpression() + " = " + g.writeExpression());
        if (solveTries <= 0) {
            return new ExpressionCollection();
        }
        solveTries--;

        // Zunächst beide Seiten entsprechend vereinfachen.
        try {
            f = f.simplify(simplifyTypesEquation);
            g = g.simplify(simplifyTypesEquation);
        } catch (EvaluationException e) {
            return new ExpressionCollection();
        }

        if (f.equivalent(g)) {
            return ALL_REALS;
        }

        if (!f.contains(var) && !g.contains(var)) {

            if (!f.equivalent(g)) {
                // Konstante Gleichung f = g mit f != g besitzt keine Lösungen.
                return new ExpressionCollection();
            }
            // Benachrichtigung an den User, dass alle reellen Zahlen Lösungen der Gleichung darstellen.
            return ALL_REALS;
        }

        // Falls f konstant und g nicht konstant bzgl. var ist -> die nichtkonstante Seite nach links!
        if (!f.contains(var) && g.contains(var)) {
            return solveGeneralEquation(g, f, var);
        }

        // Gleiche Summanden auf beiden Seiten kürzen.
        Expression[] F;
        F = cancelEqualSummandsInEquation(f, g);
        f = F[0];
        g = F[1];

        // Äquivalenzumformungen vornehmen.
        ExpressionCollection zeros = elementaryEquivalentTransformation(f, g, var);
        if (!zeros.isEmpty() || zeros == NO_SOLUTIONS) {
            return zeros;
        }

        // Falls die Gleichung die Form var = a, a unabhängig von var, besitzt.
        try {
            return solveVariableEquation(f, g, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Falls die Gleichung eine Potenzgleichung darstellt.
        try {
            return solvePowerEquation(f, g, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Falls die Gleichung eine Funktionsgleichung darstellt.
        try {
            return solveFunctionEquation(f, g, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Falls f und g einen gemeinsamen Faktor h im Zähler besitzen.
        try {
            return solveEquationWithCommonFactors(f, g, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Letzter Versuch: f - g = 0 lösen.
        return solveZeroEquation(f.sub(g), var);

    }

    // Es folgt eine Reihe von Einzelfunktionen zur Vereinfachung von f(x) = g(x).
    /**
     * Kürzt gleiche Summanden auf beiden Seiten.
     */
    private static Expression[] cancelEqualSummandsInEquation(Expression f, Expression g) {

        ExpressionCollection summandsLeftF = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRightF = SimplifyUtilities.getSummandsRightInExpression(f);
        ExpressionCollection summandsLeftG = SimplifyUtilities.getSummandsLeftInExpression(g);
        ExpressionCollection summandsRightG = SimplifyUtilities.getSummandsRightInExpression(g);

        // Gleiche Summanden in Minuenden beseitigen.
        Expression summandF, summandG;
        for (int i = 0; i < summandsLeftF.getBound(); i++) {
            summandF = summandsLeftF.get(i);
            for (int j = 0; j < summandsLeftG.getBound(); j++) {
                if (summandsLeftG.get(j) != null) {
                    summandG = summandsLeftG.get(j);
                    if (summandF.equivalent(summandG)) {
                        summandsLeftF.remove(i);
                        summandsLeftG.remove(j);
                        break;
                    }
                }
            }
        }
        // Gleiche Summanden in Subtrahenden beseitigen.
        for (int i = 0; i < summandsRightF.getBound(); i++) {
            summandF = summandsRightF.get(i);
            for (int j = 0; j < summandsRightG.getBound(); j++) {
                if (summandsRightG.get(j) != null) {
                    summandG = summandsRightG.get(j);
                    if (summandF.equivalent(summandG)) {
                        summandsRightF.remove(i);
                        summandsRightG.remove(j);
                        break;
                    }
                }
            }
        }

        Expression[] result = new Expression[2];
        result[0] = SimplifyUtilities.produceDifference(summandsLeftF, summandsRightF);
        result[1] = SimplifyUtilities.produceDifference(summandsLeftG, summandsRightG);
        return result;

    }

    /**
     * Liefert die Lösung der Gleichung x = const.
     */
    private static ExpressionCollection solveVariableEquation(Expression f, Expression g, String var) throws NotAlgebraicallySolvableException {
        if (f instanceof Variable && ((Variable) f).getName().equals(var) && !g.contains(var)) {
            ExpressionCollection zero = new ExpressionCollection();
            zero.put(0, g);
        }
        throw new NotAlgebraicallySolvableException();
    }

    /**
     * Führt an f und g, wenn möglich, elementare Äquivalenzumformungen durch
     * und gibt dann die Lösungen der Gleichung f = g aus, falls welche gefunden
     * wurden.
     *
     * @throws EvaluationException
     */
    private static ExpressionCollection elementaryEquivalentTransformation(Expression f, Expression g, String var) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();

        if (f.contains(var) && !g.contains(var) && f instanceof BinaryOperation) {

            BinaryOperation fAsBinaryOperation = (BinaryOperation) f;
            if (fAsBinaryOperation.getLeft().contains(var) && !fAsBinaryOperation.getRight().contains(var)) {

                if (fAsBinaryOperation.isSum()) {
                    return solveGeneralEquation(fAsBinaryOperation.getLeft(), g.sub(fAsBinaryOperation.getRight()), var);
                } else if (fAsBinaryOperation.isDifference()) {
                    return solveGeneralEquation(fAsBinaryOperation.getLeft(), g.add(fAsBinaryOperation.getRight()), var);
                } else if (fAsBinaryOperation.isProduct() && !fAsBinaryOperation.getRight().equals(ZERO)) {
                    return solveGeneralEquation(fAsBinaryOperation.getLeft(), g.div(fAsBinaryOperation.getRight()), var);
                } else if (fAsBinaryOperation.isQuotient() && !fAsBinaryOperation.getRight().equals(ZERO)) {
                    return solveGeneralEquation(fAsBinaryOperation.getLeft(), g.mult(fAsBinaryOperation.getRight()), var);
                } else if (fAsBinaryOperation.isPower()) {

                    if (fAsBinaryOperation.getRight().isOddIntegerConstant()) {
                        return solveGeneralEquation(fAsBinaryOperation.getLeft(), g.pow(ONE, fAsBinaryOperation.getRight()), var);
                    }
                    if (fAsBinaryOperation.getRight().isEvenIntegerConstant()) {
                        if (!g.isConstant() || g.isNonNegative()) {
                            ExpressionCollection resultPositive = solveGeneralEquation(fAsBinaryOperation.getLeft(), g.pow(ONE, fAsBinaryOperation.getRight()), var);
                            ExpressionCollection resultNegative = solveGeneralEquation(fAsBinaryOperation.getLeft(), Expression.MINUS_ONE.mult(g.pow(ONE, fAsBinaryOperation.getRight())), var);
                            return SimplifyUtilities.union(resultPositive, resultNegative);
                        }
                        return new ExpressionCollection();
                    }
                    if (fAsBinaryOperation.getRight().isRationalConstant()) {
                        BigInteger rootDegree = ((Constant) ((BinaryOperation) fAsBinaryOperation.getRight()).getRight()).getValue().toBigInteger();
                        if (((BinaryOperation) fAsBinaryOperation.getRight()).getRight().isEvenIntegerConstant()) {
                            zeros = solveGeneralEquation(fAsBinaryOperation.pow(rootDegree), g.pow(rootDegree), var);
                            if (g.isConstant() && g.isNonPositive()) {
                                /*
                                 Falsche Lösungen aussortieren: wenn g <= 0
                                 ist und f von der Form f = h^(1/n) mit
                                 geradem n, dann kann die Gleichung f = g
                                 nicht bestehen (f ist nicht konstant, denn es
                                 enthält die Variable var).
                                 */
                                return new ExpressionCollection();
                            }
                            return zeros;
                        }
                        return solveGeneralEquation(fAsBinaryOperation.pow(rootDegree), g.pow(rootDegree), var);
                    }

                    return solveGeneralEquation(fAsBinaryOperation.getLeft(), g.pow(ONE, fAsBinaryOperation.getRight()), var);

                }
            } else if (!fAsBinaryOperation.getLeft().contains(var) && fAsBinaryOperation.getRight().contains(var)) {

                if (fAsBinaryOperation.isSum()) {
                    return solveGeneralEquation(fAsBinaryOperation.getRight(), g.sub(fAsBinaryOperation.getLeft()), var);
                } else if (fAsBinaryOperation.isDifference()) {
                    return solveGeneralEquation(Expression.MINUS_ONE.mult(fAsBinaryOperation.getRight()), g.sub(fAsBinaryOperation.getLeft()), var);
                } else if (fAsBinaryOperation.isProduct() && !fAsBinaryOperation.getLeft().equals(ZERO)) {
                    return solveGeneralEquation(fAsBinaryOperation.getRight(), g.div(fAsBinaryOperation.getLeft()), var);
                } else if (fAsBinaryOperation.isQuotient() && !fAsBinaryOperation.getLeft().equals(ZERO) && !g.equals(ZERO)) {
                    return solveGeneralEquation(fAsBinaryOperation.getRight(), fAsBinaryOperation.getLeft().div(g), var);
                } else if (fAsBinaryOperation.isPower()) {
                    return solveGeneralEquation(fAsBinaryOperation.getRight(), (new Function(g, TypeFunction.ln)).div(new Function(fAsBinaryOperation.getLeft(), TypeFunction.ln)), var);
                }

            }

        }

        // Falls nichts von all dem funktioniert hat, dann zumindest alle Nenner durch Multiplikation eliminieren.
        try {
            return solveFractionalEquation(f, g, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        return zeros;

    }

    private static boolean doesQuotientOccur(Expression f) {

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(f);

        boolean result = false;
        for (int i = 0; i < summandsLeft.getBound(); i++) {
            result = result || summandsLeft.get(i).isQuotient();
        }
        for (int i = 0; i < summandsRight.getBound(); i++) {
            result = result || summandsRight.get(i).isQuotient();
        }

        return result;

    }

    /**
     * Falls in f oder g Brüche auftreten, so werden f und g auf einen
     * gemeinsamen Nenner gebracht und die so entstandene neue Gleichung wird
     * gelöst. Andernfalls wird eine leere HashMap zurückgegeben.
     *
     * @throws EvaluationException
     */
    private static ExpressionCollection solveFractionalEquation(Expression f, Expression g, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        ExpressionCollection zeros;

        // 1. Alle Nenner in f eliminieren, falls f var enthält.
        if (doesQuotientOccur(f) && f.contains(var)) {

            // Zunächst alle Summanden in f auf den kleinsten gemeinsamen Nenner bringen.
            f = SimplifyBinaryOperationMethods.bringExpressionToCommonDenominator((BinaryOperation) f);
            /*
             Beide Seiten mit dem kleinsten gemeinsamen Nenner von f
             multiplizieren. WICHTIG: f ist nach der Anwendung von
             bringExpressionToCommonDenominator() automatisch ein Bruch, d. h.
             der Typecast zu BinaryOperation ist unkritisch.
             */
            Expression multipleOfF = ((BinaryOperation) f).getLeft();
            Expression multipleOfG = ((BinaryOperation) f).getRight().mult(g);

            zeros = solveGeneralEquation(multipleOfF, multipleOfG, var);
            ExpressionCollection validZeros = new ExpressionCollection();

            Expression valueOfDenominatorOfFAtZero;
            for (int i = 0; i < zeros.getBound(); i++) {
                valueOfDenominatorOfFAtZero = ((BinaryOperation) f).getRight().replaceVariable(var, zeros.get(i)).simplify();
                if (!valueOfDenominatorOfFAtZero.equals(ZERO)) {
                    validZeros.add(zeros.get(i));
                }
            }

            return validZeros;

        }

        // 2. Alle Nenner in g eliminieren, falls g var enthält.
        if (doesQuotientOccur(g) && g.contains(var)) {

            // Zunächst alle Summanden in g auf den kleinsten gemeinsamen Nenner bringen.
            g = SimplifyBinaryOperationMethods.bringExpressionToCommonDenominator((BinaryOperation) g);
            /*
             Beide Seiten mit dem kleinsten gemeinsamen Nenner von g
             multiplizieren. WICHTIG: g ist nach der Anwendung von
             bringExpressionToCommonDenominator() automatisch ein Bruch, d. h.
             der Typecast zu BinaryOperation ist unkritisch.
             */
            Expression multipleOfF = ((BinaryOperation) g).getRight().mult(f);
            Expression multipleOfG = ((BinaryOperation) g).getLeft();

            zeros = solveGeneralEquation(multipleOfF, multipleOfG, var);
            ExpressionCollection validZeros = new ExpressionCollection();

            Expression valueOfDenominatorOfGAtZero;
            for (int i = 0; i < zeros.getBound(); i++) {
                valueOfDenominatorOfGAtZero = ((BinaryOperation) g).getRight().replaceVariable(var, zeros.get(i)).simplify();
                if (!valueOfDenominatorOfGAtZero.equals(ZERO)) {
                    validZeros.add(zeros.get(i));
                }
            }

            return validZeros;

        }

        throw new NotAlgebraicallySolvableException();

    }

    /**
     * Prozedur zum Finden spezieller Lösungen von Gleichungen der Form
     * f(x)^p(x) = g(x)^q(x).
     *
     * @throws EvaluationException
     */
    private static ExpressionCollection solvePowerEquation(Expression f, Expression g, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        if (f.isPower() && g.isPower()) {

            /*
             Zunächst Speziallösung: p(x) = q(x) = 0 lösen und testen, ob
             f(x), g(x) dort nicht negativ und nicht 0 sind. ODER p(x) = q(x)
             = 1 lösen. ODER p(x) = q(x) und testen, ob dort f(x) = g(x) >= 0
             gilt. ODER: Falls f(x) = g(x), dann p(x) = q(x) lösen.
             */
            ExpressionCollection specialZeros;
            if (((BinaryOperation) f).getLeft().equivalent(((BinaryOperation) g).getLeft())) {
                specialZeros = solveGeneralEquation(((BinaryOperation) f).getRight(), ((BinaryOperation) g).getRight(), var);
            } else {
                specialZeros = SimplifyUtilities.intersection(solveGeneralEquation(((BinaryOperation) f).getRight(), ZERO, var),
                        solveGeneralEquation(((BinaryOperation) g).getRight(), ZERO, var));
            }
            ExpressionCollection zeros = new ExpressionCollection();
            Expression baseOfFAtSpecialZero;
            for (int i = 0; i < specialZeros.getBound(); i++) {
                baseOfFAtSpecialZero = ((BinaryOperation) f).getLeft().replaceVariable(var, specialZeros.get(i)).simplify();
                if (!baseOfFAtSpecialZero.isConstant() || baseOfFAtSpecialZero.isNonNegative()) {
                    zeros.add(specialZeros.get(i));
                }
            }

            specialZeros = SimplifyUtilities.intersection(solveGeneralEquation(((BinaryOperation) f).getLeft(), ONE, var),
                    solveGeneralEquation(((BinaryOperation) g).getLeft(), ONE, var));
            for (int i = 0; i < specialZeros.getBound(); i++) {
                zeros.add(specialZeros.get(i));
            }

            specialZeros = SimplifyUtilities.intersection(solveGeneralEquation(((BinaryOperation) f).getLeft(), ((BinaryOperation) g).getLeft(), var),
                    solveGeneralEquation(((BinaryOperation) f).getRight(), ((BinaryOperation) g).getRight(), var));
            Expression valueOfFAtSpecialZero, valueOfGAtSpecialZero;
            for (int i = 0; i < specialZeros.getBound(); i++) {
                valueOfFAtSpecialZero = ((BinaryOperation) f).getLeft().replaceVariable(var, specialZeros.get(i)).simplify();
                valueOfGAtSpecialZero = ((BinaryOperation) g).getLeft().replaceVariable(var, specialZeros.get(i)).simplify();
                if (valueOfFAtSpecialZero.equivalent(valueOfGAtSpecialZero)
                        && (!valueOfFAtSpecialZero.isConstant() || valueOfFAtSpecialZero.isNonNegative())) {
                    zeros.add(specialZeros.get(i));
                }
            }

            if (((BinaryOperation) f).getRight().isIntegerConstant() && ((BinaryOperation) g).getRight().isIntegerConstant()) {

                BigInteger m = ((Constant) ((BinaryOperation) f).getRight()).getValue().toBigInteger();
                BigInteger n = ((Constant) ((BinaryOperation) g).getRight()).getValue().toBigInteger();
                BigInteger commonRootDegree = m.gcd(n);
                if (commonRootDegree.compareTo(BigInteger.ONE) > 0 && commonRootDegree.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ONE) == 0) {
                    return solveGeneralEquation(((BinaryOperation) f).getLeft().pow(m.divide(commonRootDegree)),
                            ((BinaryOperation) g).getLeft().pow(n.divide(commonRootDegree)), var);
                }
                if (commonRootDegree.compareTo(BigInteger.ONE) > 0 && commonRootDegree.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                    ExpressionCollection zerosPositive = solveGeneralEquation(((BinaryOperation) f).getLeft().pow(m.divide(commonRootDegree)),
                            ((BinaryOperation) g).getLeft().pow(n.divide(commonRootDegree)), var);
                    ExpressionCollection zerosNegative = solveGeneralEquation(((BinaryOperation) f).getLeft().pow(m.divide(commonRootDegree)),
                            (Expression.MINUS_ONE).mult(((BinaryOperation) g).getLeft()).pow(n.divide(commonRootDegree)), var);
                    return SimplifyUtilities.union(zerosPositive, zerosNegative);
                }

            }

            if (((BinaryOperation) f).getRight().isIntegerConstantOrRationalConstant() && ((BinaryOperation) g).getRight().isIntegerConstantOrRationalConstant()) {
                BigInteger m, n;
                if (((BinaryOperation) f).getRight().isRationalConstant()) {
                    m = ((Constant) ((BinaryOperation) ((BinaryOperation) f).getRight()).getRight()).getValue().toBigInteger();
                } else {
                    m = BigInteger.ONE;
                }
                if (((BinaryOperation) g).getRight().isRationalConstant()) {
                    n = ((Constant) ((BinaryOperation) ((BinaryOperation) g).getRight()).getRight()).getValue().toBigInteger();
                } else {
                    n = BigInteger.ONE;
                }

                BigInteger commonPower = m.multiply(n).divide(m.gcd(n));
                if (commonPower.compareTo(BigInteger.ONE) > 0) {
                    return solveGeneralEquation(f.pow(commonPower), g.pow(commonPower), var);
                }
            }

            /*
             Wenn Exponenten äquivalent sind (aber evtl. nicht konstant) ->
             Basen müssen gleich sein. Danach: Prüfen, ob die Exponenten an
             den betreffenden Nullstellen positiv sind.
             */
            if (((BinaryOperation) f).getRight().equivalent(((BinaryOperation) g).getRight())) {

                ExpressionCollection possibleZeros = solveGeneralEquation(((BinaryOperation) f).getLeft(), ((BinaryOperation) g).getLeft(), var);

                // Falsche Lösungen aussortieren
                Expression exponentAtZero;
                boolean validZero;
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    baseOfFAtSpecialZero = ((BinaryOperation) f).getLeft().replaceVariable(var, possibleZeros.get(i)).simplify();
                    exponentAtZero = ((BinaryOperation) f).getRight().replaceVariable(var, possibleZeros.get(i)).simplify();
                    validZero = baseOfFAtSpecialZero.isNonNegative() || (baseOfFAtSpecialZero.equals(ZERO)
                            && exponentAtZero.isNonNegative());

                    if (exponentAtZero.isRationalConstant() && ((BinaryOperation) exponentAtZero).getLeft().isIntegerConstant()
                            && ((BinaryOperation) exponentAtZero).getRight().isIntegerConstant()) {
                        validZero = validZero || (!baseOfFAtSpecialZero.isNonNegative() && ((BinaryOperation) exponentAtZero).getRight().isOddIntegerConstant());
                    }

                    if (validZero) {
                        zeros.add(possibleZeros.get(i));
                    }
                }
            }

            return zeros;

        }

        throw new NotAlgebraicallySolvableException();

    }

    /**
     * Prozedur zum Lösen von Gleichungen der Form F(f(x)) = F(g(x)) oder
     * F(f(x)) = const, var == x.
     */
    private static ExpressionCollection solveFunctionEquation(Expression f, Expression g, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        if (!f.contains(var) || !(f instanceof Function)) {
            throw new NotAlgebraicallySolvableException();
        }

        Function functionF = (Function) f;
        TypeFunction type = functionF.getType();

        if (type.equals(TypeFunction.abs)) {
            return solveEquationAbs(((Function) f).getLeft(), g, var);
        } else if (type.equals(TypeFunction.sgn)) {
            return solveEquationSgn(((Function) f).getLeft(), g, var);
        } else if (type.equals(TypeFunction.exp)) {
            return solveEquationExp(((Function) f).getLeft(), g, var);
        } else if (type.equals(TypeFunction.lg)) {
            return solveEquationLg(((Function) f).getLeft(), g, var);
        } else if (type.equals(TypeFunction.ln)) {
            return solveEquationLn(((Function) f).getLeft(), g, var);
        } else if (type.equals(TypeFunction.sin)) {
            return solveEquationSin(((Function) f).getLeft(), g, var);
        } else if (type.equals(TypeFunction.cos)) {
            return solveEquationCos(((Function) f).getLeft(), g, var);
        } else if (type.equals(TypeFunction.tan)) {
            return solveEquationTan(((Function) f).getLeft(), g, var);
        } else if (type.equals(TypeFunction.cot)) {
            return solveEquationCot(((Function) f).getLeft(), g, var);
        } else if (type.equals(TypeFunction.sec)) {
            return solveEquationSec(((Function) f).getLeft(), g, var);
        } else if (type.equals(TypeFunction.cosec)) {
            return solveEquationCosec(((Function) f).getLeft(), g, var);
        } else if (type.equals(TypeFunction.cosh)) {
            return solveEquationCosh(((Function) f).getLeft(), g, var);
        } else if (type.equals(TypeFunction.sech)) {
            return solveEquationSec(((Function) f).getLeft(), g, var);
        }

        // Ansonsten ist f eine bijektive Funktion
        return solveEquationWithBijectiveFunction(((Function) f).getLeft(), type, g, var);

    }

    /**
     * Methode zum Lösen einer Betragsgleichung |argument| = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationWithBijectiveFunction(Expression argument, TypeFunction type,
            Expression g, String var) {

        TypeFunction inverseType;

        if (type.equals(TypeFunction.sinh)) {
            inverseType = TypeFunction.arsinh;
        } else if (type.equals(TypeFunction.tanh)) {
            inverseType = TypeFunction.artanh;
        } else if (type.equals(TypeFunction.coth)) {
            inverseType = TypeFunction.arcoth;
        } else if (type.equals(TypeFunction.cosech)) {
            inverseType = TypeFunction.arcosech;
        } else if (type.equals(TypeFunction.arcsin)) {
            inverseType = TypeFunction.sin;
        } else if (type.equals(TypeFunction.arccos)) {
            inverseType = TypeFunction.cos;
        } else if (type.equals(TypeFunction.arctan)) {
            inverseType = TypeFunction.tan;
        } else if (type.equals(TypeFunction.arccot)) {
            inverseType = TypeFunction.cot;
        } else if (type.equals(TypeFunction.arcsec)) {
            inverseType = TypeFunction.sec;
        } else if (type.equals(TypeFunction.arccosec)) {
            inverseType = TypeFunction.cosec;
        } else if (type.equals(TypeFunction.arsinh)) {
            inverseType = TypeFunction.sinh;
        } else if (type.equals(TypeFunction.arcosh)) {
            inverseType = TypeFunction.cosh;
        } else if (type.equals(TypeFunction.artanh)) {
            inverseType = TypeFunction.tanh;
        } else if (type.equals(TypeFunction.arcoth)) {
            inverseType = TypeFunction.coth;
        } else if (type.equals(TypeFunction.arsech)) {
            inverseType = TypeFunction.sech;
        } else {
            // Hier ist type == arccosech.
            inverseType = TypeFunction.cosech;
        }

        ExpressionCollection zeros = new ExpressionCollection();

        if (g instanceof Function && ((Function) g).getType().equals(type)) {
            try {
                zeros = solveGeneralEquation(argument, ((Function) g).getLeft(), var);
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        } else if (!g.contains(var)) {
            try {
                zeros = solveGeneralEquation(argument, new Function(g, inverseType), var);
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Betragsgleichung |argument| = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationAbs(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection zerosPositive, zerosNegative;

        if (g instanceof Function && ((Function) g).getType().equals(TypeFunction.abs)) {
            try {
                zerosPositive = solveGeneralEquation(argument, ((Function) g).getLeft(), var);
                zerosNegative = solveGeneralEquation(argument, Expression.MINUS_ONE.mult(((Function) g).getLeft()), var);
                zeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        } else if (!g.contains(var)) {
            if (g.isNonPositive() && !g.equals(ZERO)) {
                // Gleichung ist unlösbar. 
                return NO_SOLUTIONS;
            }
            try {
                zerosPositive = solveGeneralEquation(argument, g, var);
                zerosNegative = solveGeneralEquation(argument, Expression.MINUS_ONE.mult(g), var);
                if (!zerosPositive.isEmpty() || !zerosNegative.isEmpty()) {
                    zeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Signumgleichung sgn(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationSgn(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();

        if (g.isConstant() && !g.equals(ZERO)) {
            // Gleichung ist entweder unlösbar oder kann nicht explizit gelöst werden.
            return NO_SOLUTIONS;
        }
        // Man kann zumindest über den Spezialfall g = 0 etwas aussagen: sgn(f(x)) = 0 <=> f(x) = 0.
        if (g.equals(ZERO)) {
            try {
                zeros = solveGeneralEquation(argument, ZERO, var);
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Exponentialgleichung exp(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationExp(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();

        if (g instanceof Function && ((Function) g).getType().equals(TypeFunction.exp)) {
            try {
                zeros = solveGeneralEquation(argument, ((Function) g).getLeft(), var);
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        } else if (!g.contains(var)) {
            if (g.isNonPositive()) {
                // Gleichung ist unlösbar. 
                return NO_SOLUTIONS;
            }
            try {
                zeros = solveGeneralEquation(argument, new Function(g, TypeFunction.ln), var);
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Logarithmusgleichung lg(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationLg(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();

        if (g.isFunction(TypeFunction.lg)) {
            try {
                zeros = solveGeneralEquation(argument, ((Function) g).getLeft(), var);
                if (zeros == NO_SOLUTIONS) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        } else if (!g.contains(var)) {
            if (argument.isAlwaysNonPositive()) {
                // Gleichung ist unlösbar. 
                return NO_SOLUTIONS;
            }
            try {
                zeros = solveGeneralEquation(argument, new Constant(10).pow(g), var);
                if (zeros == NO_SOLUTIONS) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Logarithmusgleichung ln(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationLn(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();

        if (g.isFunction(TypeFunction.ln)) {
            try {
                zeros = solveGeneralEquation(argument, ((Function) g).getLeft(), var);
                if (zeros == NO_SOLUTIONS) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        } else if (!g.contains(var)) {
            if (argument.isAlwaysNonPositive()) {
                // Gleichung ist unlösbar. 
                return NO_SOLUTIONS;
            }
            try {
                zeros = solveGeneralEquation(argument, g.exp(), var);
                if (zeros == NO_SOLUTIONS) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }
        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Logarithmusgleichung cosh(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationCosh(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection possibleZeros;
        ExpressionCollection zerosPositive;
        ExpressionCollection zerosNegative;

        // Lösungsfamilien erzeugen!
        if (g instanceof Function && ((Function) g).getType().equals(TypeFunction.sin)) {

            try {
                zerosPositive = solveGeneralEquation(argument, ((Function) g).getLeft(), var);
                zerosNegative = solveGeneralEquation(argument, (Expression.MINUS_ONE).mult(((Function) g).getLeft()), var);
                possibleZeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (!g.contains(var)) {

            try {
                zerosPositive = solveGeneralEquation(argument, new Function(g, TypeFunction.arcosh), var);
                zerosNegative = solveGeneralEquation(argument, (Expression.MINUS_ONE).mult(new Function(g, TypeFunction.arcosh)), var);
                possibleZeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Logarithmusgleichung sech(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationSech(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection possibleZeros;
        ExpressionCollection zerosPositive;
        ExpressionCollection zerosNegative;

        // Lösungsfamilien erzeugen!
        if (g instanceof Function && ((Function) g).getType().equals(TypeFunction.sin)) {

            try {
                zerosPositive = solveGeneralEquation(argument, ((Function) g).getLeft(), var);
                zerosNegative = solveGeneralEquation(argument, (Expression.MINUS_ONE).mult(((Function) g).getLeft()), var);
                possibleZeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (!g.contains(var)) {

            try {
                zerosPositive = solveGeneralEquation(argument, new Function(g, TypeFunction.arsech), var);
                zerosNegative = solveGeneralEquation(argument, (Expression.MINUS_ONE).mult(new Function(g, TypeFunction.arsech)), var);
                possibleZeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Logarithmusgleichung sin(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationSin(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection possibleZeros;
        ExpressionCollection zerosPositive;
        ExpressionCollection zerosNegative = new ExpressionCollection();
        String K = getParameterVariable(g);

        // Lösungsfamilien erzeugen!
        if (g instanceof Function && ((Function) g).getType().equals(TypeFunction.sin)) {

            try {
                zerosPositive = solveGeneralEquation(argument, ((Function) g).getLeft().add((TWO).mult(Expression.PI.mult(Variable.create(K)))), var);
                zerosNegative = solveGeneralEquation(argument, ONE.add((TWO).mult(Variable.create(K))).mult(Expression.PI).sub(((Function) g).getLeft()), var);
                possibleZeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (!g.contains(var)) {

            Function gComposedWithInverse = new Function(g, TypeFunction.arcsin);
            try {
                if (g.equals(ONE) || g.equals(Expression.MINUS_ONE)) {
                    zerosPositive = solveGeneralEquation(argument, gComposedWithInverse.add((TWO).mult(Expression.PI.mult(Variable.create(K)))), var);
                } else if (g.equals(ZERO)) {
                    zerosPositive = solveGeneralEquation(argument, Expression.PI.mult(Variable.create(K)), var);
                } else {
                    zerosPositive = solveGeneralEquation(argument, gComposedWithInverse.add((TWO).mult(Expression.PI.mult(Variable.create(K)))), var);
                    zerosNegative = solveGeneralEquation(argument, ONE.add((TWO).mult(Variable.create(K))).mult(Expression.PI).sub(gComposedWithInverse), var);
                }
                possibleZeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Logarithmusgleichung cos(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationCos(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection possibleZeros;
        ExpressionCollection zerosPositive;
        ExpressionCollection zerosNegative = new ExpressionCollection();
        String K = getParameterVariable(g);

        // Lösungsfamilien erzeugen!
        if (g instanceof Function && ((Function) g).getType().equals(TypeFunction.sin)) {

            try {
                zerosPositive = solveGeneralEquation(argument, ((Function) g).getLeft().add((TWO).mult(Expression.PI.mult(Variable.create(K)))), var);
                zerosNegative = solveGeneralEquation(argument, TWO.mult(Expression.PI.mult(Variable.create(K))).sub(((Function) g).getLeft()), var);
                possibleZeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (!g.contains(var)) {

            Function gComposedWithInverse = new Function(g, TypeFunction.arccos);
            try {
                if (g.equals(ONE) || g.equals(Expression.MINUS_ONE)) {
                    zerosPositive = solveGeneralEquation(argument, gComposedWithInverse.add((TWO).mult(Expression.PI.mult(Variable.create(K)))), var);
                } else if (g.equals(ZERO)) {
                    zerosPositive = solveGeneralEquation(argument, gComposedWithInverse.add(Expression.PI.mult(Variable.create(K))), var);
                } else {
                    zerosPositive = solveGeneralEquation(argument, gComposedWithInverse.add((TWO).mult(Expression.PI.mult(Variable.create(K)))), var);
                    zerosNegative = solveGeneralEquation(argument, TWO.mult(Expression.PI.mult(Variable.create(K))).sub(gComposedWithInverse), var);
                }
                possibleZeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Logarithmusgleichung tan(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationTan(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection possibleZeros;
        String K = getParameterVariable(g);

        // Lösungsfamilien erzeugen!
        if (g instanceof Function && ((Function) g).getType().equals(TypeFunction.sin)) {

            try {
                possibleZeros = solveGeneralEquation(argument, ((Function) g).getLeft().add(Expression.PI.mult(Variable.create(K))), var);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (!g.contains(var)) {

            Function gComposedWithInverse = new Function(g, TypeFunction.arctan);
            try {
                possibleZeros = solveGeneralEquation(argument, gComposedWithInverse.add(Expression.PI.mult(Variable.create(K))), var);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Logarithmusgleichung cot(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationCot(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection possibleZeros;
        String K = getParameterVariable(g);

        // Lösungsfamilien erzeugen!
        if (g instanceof Function && ((Function) g).getType().equals(TypeFunction.sin)) {

            try {
                possibleZeros = solveGeneralEquation(argument, ((Function) g).getLeft().add(Expression.PI.mult(Variable.create(K))), var);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (!g.contains(var)) {

            Function gComposedWithInverse = new Function(g, TypeFunction.arccot);
            try {
                possibleZeros = solveGeneralEquation(argument, gComposedWithInverse.add(Expression.PI.mult(Variable.create(K))), var);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Logarithmusgleichung sec(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationSec(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection possibleZeros;
        ExpressionCollection zerosPositive;
        ExpressionCollection zerosNegative = new ExpressionCollection();
        String K = getParameterVariable(g);

        // Lösungsfamilien erzeugen!
        if (g instanceof Function && ((Function) g).getType().equals(TypeFunction.sin)) {

            try {
                zerosPositive = solveGeneralEquation(argument, ((Function) g).getLeft().add((TWO).mult(Expression.PI.mult(Variable.create(K)))), var);
                zerosNegative = solveGeneralEquation(argument, TWO.mult(Expression.PI.mult(Variable.create(K))).sub(((Function) g).getLeft()), var);
                possibleZeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (!g.contains(var)) {

            Function gComposedWithInverse = new Function(g, TypeFunction.arcsec);
            try {
                if (g.equals(ONE) || g.equals(Expression.MINUS_ONE)) {
                    zerosPositive = solveGeneralEquation(argument, gComposedWithInverse.add((TWO).mult(Expression.PI.mult(Variable.create(K)))), var);
                } else {
                    zerosPositive = solveGeneralEquation(argument, gComposedWithInverse.add((TWO).mult(Expression.PI.mult(Variable.create(K)))), var);
                    zerosNegative = solveGeneralEquation(argument, TWO.mult(Expression.PI.mult(Variable.create(K))).sub(gComposedWithInverse), var);
                }
                possibleZeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return zeros;

    }

    /**
     * Methode zum Lösen einer Logarithmusgleichung cosec(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationCosec(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection possibleZeros;
        ExpressionCollection zerosPositive;
        ExpressionCollection zerosNegative = new ExpressionCollection();
        String K = getParameterVariable(g);

        // Lösungsfamilien erzeugen!
        if (g instanceof Function && ((Function) g).getType().equals(TypeFunction.sin)) {

            try {
                zerosPositive = solveGeneralEquation(argument, ((Function) g).getLeft().add((TWO).mult(Expression.PI.mult(Variable.create(K)))), var);
                zerosNegative = solveGeneralEquation(argument, ONE.add((TWO).mult(Variable.create(K))).mult(Expression.PI).sub(((Function) g).getLeft()), var);
                possibleZeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        } else if (!g.contains(var)) {

            Function gComposedWithInverse = new Function(g, TypeFunction.arccosec);
            try {
                if (g.equals(ONE) || g.equals(Expression.MINUS_ONE)) {
                    zerosPositive = solveGeneralEquation(argument, gComposedWithInverse.add((TWO).mult(Expression.PI.mult(Variable.create(K)))), var);
                } else {
                    zerosPositive = solveGeneralEquation(argument, gComposedWithInverse.add((TWO).mult(Expression.PI.mult(Variable.create(K)))), var);
                    zerosNegative = solveGeneralEquation(argument, ONE.add((TWO).mult(Variable.create(K))).mult(Expression.PI).sub(gComposedWithInverse), var);
                }
                possibleZeros = SimplifyUtilities.union(zerosPositive, zerosNegative);
                // Ungültige Lösungen aussortieren. 
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    try {
                        possibleZeros.put(i, possibleZeros.get(i).simplify());
                        zeros.add(possibleZeros.get(i));
                    } catch (EvaluationException e) {
                    }
                }
                if (zeros.isEmpty()) {
                    return NO_SOLUTIONS;
                }
            } catch (EvaluationException e) {
                return NO_SOLUTIONS;
            }

        }

        return zeros;

    }

    /**
     * In f sind Variablen enthalten, unter anderem "Parametervariablen" K_1,
     * K_2, .... Diese Funktion liefert dasjenige K_i, welches in h noch nicht
     * vorkommt.
     */
    private static String getParameterVariable(Expression f) {
        String var = NotationLoader.FREE_INTEGER_PARAMETER_VAR + "_";
        int j = 1;
        while (f.contains(var + j)) {
            j++;
        }
        return var + j;
    }

    /**
     * Hilfsmethode für solveGeneralEquation(). Liefert alle gemeinsamen
     * Faktoren (mit Vielfachheiten) von f und g.
     */
    private static ExpressionCollection getCommonFactors(Expression f, Expression g) {

        ExpressionCollection factorsF, factorsG;

        if (f.isQuotient()) {
            factorsF = SimplifyUtilities.getFactorsOfNumeratorInExpression(((BinaryOperation) f).getLeft());
        } else {
            factorsF = SimplifyUtilities.getFactors(f);
        }

        if (g.isQuotient()) {
            factorsG = SimplifyUtilities.getFactorsOfNumeratorInExpression(((BinaryOperation) g).getLeft());
        } else {
            factorsG = SimplifyUtilities.getFactors(g);
        }

        ExpressionCollection factorsFCopy = ExpressionCollection.copy(factorsF);
        ExpressionCollection factorsGCopy = ExpressionCollection.copy(factorsG);

        try {

            /*
             Idee: Falls f und g Faktoren der Form h^m und h^n besitzen, wobei
             m und n rationale Zahlen sind, so wird h^(min(m, n)) zur Menge
             der gemeinsamen Faktoren hinzugefügt. Andernfalls werden Faktoren
             nur dann hinzugefügt, wenn sie äquivalent sind.
             */
            ExpressionCollection commonFactors = new ExpressionCollection();
            Expression base, baseToCompare, exponent, exponentToCompare, exponentDifference, exponentOfCommonFactor;

            for (int i = 0; i < factorsF.getBound(); i++) {
                if (factorsF.get(i) == null) {
                    continue;
                }

                if (factorsF.get(i).isPower() && ((BinaryOperation) factorsF.get(i)).getRight().isIntegerConstantOrRationalConstant()) {
                    base = ((BinaryOperation) factorsF.get(i)).getLeft();
                    exponent = ((BinaryOperation) factorsF.get(i)).getRight();
                } else {
                    base = factorsF.get(i);
                    exponent = ONE;
                }

                for (int j = 0; j < factorsG.getBound(); j++) {
                    if (factorsG.get(j) == null) {
                        continue;
                    }

                    if (factorsG.get(j).isPower() && ((BinaryOperation) factorsG.get(j)).getRight().isIntegerConstantOrRationalConstant()) {
                        baseToCompare = ((BinaryOperation) factorsG.get(j)).getLeft();
                        exponentToCompare = ((BinaryOperation) factorsG.get(j)).getRight();
                    } else {
                        baseToCompare = factorsG.get(j);
                        exponentToCompare = ONE;
                    }

                    if (base.equivalent(baseToCompare)) {
                        exponentDifference = exponent.sub(exponentToCompare).simplify();
                        if (exponentDifference.isNonNegative()) {
                            exponentOfCommonFactor = exponentToCompare;
                        } else {
                            exponentOfCommonFactor = exponent;
                        }
                        if (exponentOfCommonFactor.equals(ONE)) {
                            commonFactors.add(base);
                        } else {
                            commonFactors.add(base.pow(exponentOfCommonFactor));
                        }
                        factorsF.remove(i);
                        factorsG.remove(j);
                        break;
                    }

                }
            }

            return commonFactors;

        } catch (EvaluationException e) {
        }

        return SimplifyUtilities.intersection(factorsFCopy, factorsGCopy);

    }

    /**
     * Hilfsmethode für solveGeneralEquation(). Liefert Lösungen für die
     * Gleichung f = g, falls f und g gemeinsame nichtkonstante Faktoren
     * besitzen.
     */
    private static ExpressionCollection solveEquationWithCommonFactors(Expression f, Expression g, String var) throws NotAlgebraicallySolvableException {

        ExpressionCollection zerosOfCancelledFactors = new ExpressionCollection();

        if (g.contains(var)) {
            try {
                ExpressionCollection commonFactorsOfFAndG = getCommonFactors(f, g);
                if (!commonFactorsOfFAndG.isEmpty()) {
                    Expression fWithoutCommonFactors = f.div(SimplifyUtilities.produceProduct(commonFactorsOfFAndG)).simplify();
                    Expression gWithoutCommonFactors = g.div(SimplifyUtilities.produceProduct(commonFactorsOfFAndG)).simplify();
                    for (int i = 0; i < commonFactorsOfFAndG.getBound(); i++) {
                        zerosOfCancelledFactors = SimplifyUtilities.union(zerosOfCancelledFactors,
                                solveZeroEquation(commonFactorsOfFAndG.get(i), var));
                    }
                    return SimplifyUtilities.union(solveGeneralEquation(fWithoutCommonFactors, gWithoutCommonFactors, var),
                            zerosOfCancelledFactors);
                }
            } catch (EvaluationException e) {
            }
        }

        throw new NotAlgebraicallySolvableException();

    }

    /**
     * Hauptmethode zum algebraischen Lösen von Gleichungen f = 0.
     *
     * @throws EvaluationException
     */
    protected static ExpressionCollection solveZeroEquation(Expression f, String var) throws EvaluationException {

//        System.out.println(solveTries + ": Löse Nullgl. " + f.writeExpression() + " = 0");
        if (solveTries <= 0) {
            return new ExpressionCollection();
        }
        solveTries--;

        try {
            f = f.simplify(simplifyTypesEquation);
        } catch (EvaluationException e) {
            /*
             Wenn beim Vereinfachen etwas schief gelaufen ist, dann war das
             keine sinnvolle Gleichung und sie kann dementsprechend nicht
             gelöst werden.
             */
            return NO_SOLUTIONS;
        }

        // Fall: f ist ein Produkt.
        try {
            return solveZeroProduct(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: f ist ein Quotient.
        try {
            return solveZeroQuotient(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: f ist eine Potenz.
        try {
            return solveZeroPower(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: f ist eine Funktion.
        try {
            return solveZeroFunction(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: f ist nicht-negativ (unabhängig von var und anderen Variablen).
        try {
            /*
             Falls f = 0 und f stets nichtnegativ ist, dann wird die Lösung
             entweder in solveAlwaysNonNegativeExpressionEqualsZero()
             gefunden, oder gar nicht.
             */
            return solveAlwaysNonNegativeExpressionEqualsZeroEquation(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: f ist ein Polynom.
        try {
            return solvePolynomialEquation(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: f is ein Polynom in x^(1/m) mit geeignetem m, x = var.
        try {
            return PolynomialAlgebraMethods.solvePolynomialEquationWithFractionalExponents(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: f ist eine rationale Funktion in einer Exponentialfunktion.
        try {
            return SolveSpecialEquationMethods.solveExponentialEquation(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: f ist eine rationale Funktion in trigonometrischen Funktionen.
        try {
            return SolveSpecialEquationMethods.solveTrigonometricalEquation(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: f = f(x, (ax^2+bx+c)^(1/2)), f = rationale Funktion in zwei Veränderlichen.
        try {
            return SolveSpecialEquationMethods.solveRationalFunctionInVarAndSqrtOfQuadraticFunctionEquation(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Fall: f = f(x, g(x)), f = rationale Funktion in zwei Veränderlichen, g derart, für y = g(x) die Auflösung x = h(y) eindeutig und rational in x ist.
        try {
            return SolveSpecialEquationMethods.solveRationalFunctionInVarAndAnotherAlgebraicExpressionEquation(f, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        /*
         Fall: f besitzt Brüche. Dann alles mit dem Hauptnenner
         ausmultiplizieren und prüfen, ob es Lösungen gibt.
         */
        try {
            return solveFractionalEquation(f, ZERO, var);
        } catch (NotAlgebraicallySolvableException e) {
        }

        /*
         Nächster Versuch: werden die üblichen Standardsubstitutionen
         ausprobiert. Im Folgenden stellt die HashMap setOfSubstitutions eine
         Menge von potiellen (einfachen) Substitutionen zur Verfügung.
         */
        ExpressionCollection setOfSubstitutions = getSuitableSubstitutionForEquation(f, var);
        setOfSubstitutions.removeMultipleTerms();
        Expression fSubstituted;

        ExpressionCollection zeros = new ExpressionCollection();
        for (int i = 0; i < setOfSubstitutions.getBound(); i++) {

            try {
                fSubstituted = SubstitutionUtilities.substitute(f, var, setOfSubstitutions.get(i));
                ExpressionCollection zerosOfSubstitutedEquation = solveGeneralEquation(((Expression) fSubstituted).simplify(), ZERO,
                        SubstitutionUtilities.getSubstitutionVariable(f));
                for (int j = 0; j < zerosOfSubstitutedEquation.getBound(); j++) {
                    zeros = SimplifyUtilities.union(zeros, solveGeneralEquation(setOfSubstitutions.get(i), zerosOfSubstitutedEquation.get(j), var));
                }
            } catch (NotSubstitutableException e) {
            }

            /*
             Falls Lösungen gefunden wurde (oder definitiv keine Lösungen
             existieren), dann stoppen und Lösungen zurückgeben.
             */
            if (!zeros.isEmpty() || zeros == NO_SOLUTIONS) {
                return zeros;
            }

        }

        /*
         Nächster Versuch: In f werden Funktionen, abhängig vom Typ der
         Gleichung, durch ihre eigentliche Definition ersetzt, beispielsweise
         2^x durch exp(ln(2)*x), lg(x) durch ln(x)/ln(10) oder tan(x) durch
         sin(x)/cos(x) etc. Dann wird nochmals versucht, die Gleichung zu
         lösen.
         */
        Expression fByDefinition = f.simplifyReplaceExponentialFunctionsByDefinitions();
        if (SimplifyRationalFunctionMethods.isRationalFunktionInExp(fByDefinition, var, new HashSet<Expression>())) {
            if (!fByDefinition.equals(f)) {
                try {
                    return SolveSpecialEquationMethods.solveExponentialEquation(fByDefinition, var);
                } catch (NotAlgebraicallySolvableException e) {
                }
            }
        }
        fByDefinition = f.simplifyReplaceTrigonometricalFunctionsByDefinitions();
        if (SimplifyRationalFunctionMethods.isRationalFunktionInTrigonometricalFunctions(fByDefinition, var, new HashSet<Expression>())) {
            if (!fByDefinition.equals(f)) {
                try {
                    return SolveSpecialEquationMethods.solveTrigonometricalEquation(fByDefinition, var);
                } catch (NotAlgebraicallySolvableException e) {
                }
            }
        }

        return zeros;

    }

    /**
     * Ab hier kommen eine Reihe von Einzelfunktionen, die bestimmte Typen von
     * Gleichungen der Form f(x) = 0 lösen.
     */
    private static ExpressionCollection solveZeroProduct(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        /**
         * Bei Multiplikation: expr = f(x)*g(x) -> f(x) = 0, g(x) = 0 separat
         * lösen und die Lösungen dann vereinigen.
         */
        if (f.isProduct()) {
            ExpressionCollection zerosLeft = solveGeneralEquation(((BinaryOperation) f).getLeft(), ZERO, var);
            ExpressionCollection zerosRight = solveGeneralEquation(((BinaryOperation) f).getRight(), ZERO, var);
            return SimplifyUtilities.union(zerosLeft, zerosRight);
        }

        throw new NotAlgebraicallySolvableException();

    }

    private static ExpressionCollection solveZeroQuotient(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        /*
         Bei Division: expr = f(x)/g(x) -> f(x) = 0 lösen und dann prüfen, ob
         g(x) bei den Lösungen nicht verschwindet.
         */
        if (f.isQuotient()) {

            // Sonderfall: wenn der Zähler bzgl. var konstant und != 0 ist, so besitzt die Gleichung keine Lösungen.
            Expression numerator = ((BinaryOperation) f).getLeft();
            if (!numerator.contains(var) && !numerator.equals(ZERO)) {
                return NO_SOLUTIONS;
            }

            ExpressionCollection zerosLeft = solveGeneralEquation(((BinaryOperation) f).getLeft(), ZERO, var);
            Expression valueOfDenominatorAtZero;
            boolean validZero;
            /*
             Es müssen nun solche Nullstellen ausgeschlossen werden, welche
             zugleich Nullstellen des Nenners sind.
             */
            ExpressionCollection zeros = new ExpressionCollection();
            for (int i = 0; i < zerosLeft.getBound(); i++) {
                valueOfDenominatorAtZero = ((BinaryOperation) f).getRight().replaceVariable(var, zerosLeft.get(i)).simplify();
                validZero = !valueOfDenominatorAtZero.isConstant() || !valueOfDenominatorAtZero.equals(ZERO);
                if (validZero) {
                    zeros.add(zerosLeft.get(i));
                }
            }

            return zeros;

        }

        throw new NotAlgebraicallySolvableException();

    }

    private static ExpressionCollection solveZeroPower(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        if (f.isPower()) {
            /*
            Bei Potenzen: expr = f(x)^g(x) -> f(x) = 0 lösen und dann prüfen, ob
            g(x) bei den Lösungen nicht <= 0 wird, falls g(x) konstant ist. Falls
            g(x) an der betreffenden Nullstelle x noch von Parametern abhängt,
            dann soll dies eine gültige Nullstelle sein.
             */
            ExpressionCollection zerosLeft = solveGeneralEquation(((BinaryOperation) f).getLeft(), ZERO, var);
            Expression exponentAtZero;
            boolean validZero;

            ExpressionCollection zeros = new ExpressionCollection();
            for (int i = 0; i < zerosLeft.getBound(); i++) {
                exponentAtZero = ((BinaryOperation) f).getRight().replaceVariable(var, zerosLeft.get(i)).simplify();
                validZero = !exponentAtZero.isConstant() || (exponentAtZero.isNonNegative() && !exponentAtZero.equals(ZERO));
                if (validZero) {
                    zeros.add(zerosLeft.get(i));
                }
            }

        }

        throw new NotAlgebraicallySolvableException();

    }

    private static ExpressionCollection solveZeroFunction(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {
        return solveFunctionEquation(f, ZERO, var);
    }

    /**
     * Löst Gleichungen der Form f = 0, wobei f stets >= 0 ist.
     */
    private static ExpressionCollection solveAlwaysNonNegativeExpressionEqualsZeroEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        if (!f.isAlwaysNonNegative()) {
            throw new NotAlgebraicallySolvableException();
        }

        if (f.isAlwaysPositive()) {
            return NO_SOLUTIONS;
        }

        ExpressionCollection summands = SimplifyUtilities.getSummands(f);

        if (summands.getBound() <= 1) {
            // Hierfür sind dann andere Methoden verantwortlich.
            throw new NotAlgebraicallySolvableException();
        }

        /*
         Jeder Summand muss = 0 sein, da die Summanden ebenfalls alle stets
         nicht-negativ sind (unabhängig vom Wert von var).
         */
        ExpressionCollection zeros = solveGeneralEquation(summands.get(0), ZERO, var);
        for (int i = 1; i < summands.getBound(); i++) {
            zeros = SimplifyUtilities.intersection(zeros, solveGeneralEquation(summands.get(i), ZERO, var));
        }

        return zeros;

    }

    /**
     * Löst Gleichungen der Form f = 0, wobei f ein Polynom ist.
     */
    public static ExpressionCollection solvePolynomialEquation(Expression f, String var) throws EvaluationException, NotAlgebraicallySolvableException {

        if (!SimplifyPolynomialMethods.isPolynomial(f, var)) {
            throw new NotAlgebraicallySolvableException();
        }

        ExpressionCollection zeros = new ExpressionCollection();

        BigInteger degree = SimplifyPolynomialMethods.getDegreeOfPolynomial(f, var);
        BigInteger order = SimplifyPolynomialMethods.getOrderOfPolynomial(f, var);
        /*
         Falls k := Ord(f) >= 0 -> 0 ist eine k-fache Nullstelle von f.
         Dividiere diese heraus und fahre fort.
         */
        if (order.compareTo(BigInteger.ZERO) > 0) {
            f = divideExpressionByPowerOfVar(f, var, order);
            zeros.add(ZERO);
            return SimplifyUtilities.union(zeros, solvePolynomialEquation(f, var));
        }

        BigInteger gcdOfExponents = PolynomialAlgebraMethods.getGCDOfExponentsInPolynomial(f, var);
        if (gcdOfExponents.compareTo(BigInteger.ONE) > 0) {
            /* 
             Falls das Polynom f(x) als f(x) = g(x^m) mit einem ganzen m > 1 geschrieben werden kann,
             dann soll zunächst g = 0 glöst werden und dann daraus die Nullstellen von f ermittelt werden.
             */
            String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
            Expression fSubstituted = SubstitutionUtilities.substituteExpressionByAnotherExpression(f, Variable.create(var).pow(gcdOfExponents), Variable.create(substVar));
            ExpressionCollection zerosOfFSubstituted = solvePolynomialEquation(fSubstituted, substVar);
            if (gcdOfExponents.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                for (Expression zero : zerosOfFSubstituted) {
                    try {
                        zeros.add(zero.pow(BigInteger.ONE, gcdOfExponents).simplify());
                        zeros.add(MINUS_ONE.mult(zero.pow(BigInteger.ONE, gcdOfExponents)).simplify());
                    } catch (EvaluationException e) {
                    }
                }
            } else {
                for (Expression zero : zerosOfFSubstituted) {
                    zeros.add(zero.pow(BigInteger.ONE, gcdOfExponents).simplify());
                }
            }
            return zeros;
        }

        degree = degree.subtract(order);

        if (degree.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL)) > 0) {
            throw new NotAlgebraicallySolvableException();
        }

        ExpressionCollection coefficients = SimplifyPolynomialMethods.getPolynomialCoefficients(f, var);
        return PolynomialAlgebraMethods.solvePolynomialEquation(coefficients, var);

    }

    /**
     * Hilfsmethode für solvePolynomialEquation(). Gibt f/x^exponent zurück,
     * wobei x = var.
     */
    private static Expression divideExpressionByPowerOfVar(Expression f, String var, BigInteger exponent) throws EvaluationException {
        if (f.isSum() || f.isDifference()) {
            return new BinaryOperation(divideExpressionByPowerOfVar(((BinaryOperation) f).getLeft(), var, exponent),
                    divideExpressionByPowerOfVar(((BinaryOperation) f).getRight(), var, exponent), ((BinaryOperation) f).getType()).simplify();
        }
        return f.div(Variable.create(var).pow(exponent)).simplify();
    }

    private static ExpressionCollection getSuitableSubstitutionForEquation(Expression f, String var) {
        ExpressionCollection substitutions = new ExpressionCollection();
        addSuitableSubstitutionForEquation(f, var, substitutions, true);
        return substitutions;
    }

    /**
     * Ermittelt potenzielle Substitutionen für eine Gleichung. Der boolsche
     * Parameter beginning sagt aus, ob f den ganzen Ausdruck darstellt, oder
     * nur einen Teil eines größeren Ausdrucks bildet. Dies ist wichtig, damit
     * es keine Endlosschleifen gibt!
     */
    private static void addSuitableSubstitutionForEquation(Expression f, String var, ExpressionCollection setOfSubstitutions, boolean beginning) {

        /*
         Es wird Folgendes als potentielle Substitution angesehen: (1) Argumente
         innerhalb von Funktionsklammern (2) Basen von Potenzen mit konstantem
         Exponenten (3) Exponenten von Potenzen mit konstanter Basis. Ferner
         darf eine Substitution nicht konstant bzgl. var und keine Variable
         sein.
         */
        if (f.contains(var) && f instanceof BinaryOperation && f.isNotPower()) {
            addSuitableSubstitutionForEquation(((BinaryOperation) f).getLeft(), var, setOfSubstitutions, false);
            addSuitableSubstitutionForEquation(((BinaryOperation) f).getRight(), var, setOfSubstitutions, false);
        }
        if (f.isPower()) {

            if (!((BinaryOperation) f).getRight().contains(var)
                    && !((BinaryOperation) f).getRight().equals(ONE)
                    && ((BinaryOperation) f).getLeft().contains(var)
                    && !(((BinaryOperation) f).getLeft() instanceof Variable)) {
                setOfSubstitutions.add(((BinaryOperation) f).getLeft());
                addSuitableSubstitutionForEquation(((BinaryOperation) f).getLeft(), var, setOfSubstitutions, false);
            } else if (!((BinaryOperation) f).getLeft().contains(var)
                    && ((BinaryOperation) f).getRight().contains(var)
                    && !(((BinaryOperation) f).getRight() instanceof Variable)) {
                setOfSubstitutions.add(((BinaryOperation) f).getRight());
                addSuitableSubstitutionForEquation(((BinaryOperation) f).getRight(), var, setOfSubstitutions, false);
            }

        } else if (f.isFunction()) {

            /*
             Als potentielle Substitution kommt die Funktion selbst in Frage,
             wenn sie NICHT die Gesamte Funktion/Gleichung darstellt (wenn
             also !beginning gilt).
             */
            if (f.contains(var) && !beginning) {
                setOfSubstitutions.add(f);
            }
            // Weitere potentielle Substitutionen finden sich möglicherweise im Argument der Funktion.
            addSuitableSubstitutionForEquation(((Function) f).getLeft(), var, setOfSubstitutions, false);

        }

    }

}
