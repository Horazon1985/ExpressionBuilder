package solveequationmethods;

import computationbounds.ComputationBounds;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.ONE;
import static expressionbuilder.Expression.TWO;
import static expressionbuilder.Expression.ZERO;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import expressionbuilder.TypeSimplify;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyAlgebraicExpressionMethods;
import expressionsimplifymethods.SimplifyBinaryOperationMethods;
import expressionsimplifymethods.SimplifyPolynomialMethods;
import expressionsimplifymethods.SimplifyUtilities;
import java.math.BigInteger;
import java.util.HashSet;

public class SolveMethods {

    /**
     * Anzahl der Versuche, wie oft eine Gleichung versucht werden soll, gelöst
     * zu werden. Bei jedem Versuch wird dieser Parameter um 1 dekrementiert.
     * Vor dem Lösen einer Gleichung muss dieser gesetzt werden.
     */
    private static int solveTries;

    /**
     * Konstanten, die aussagen, ob bei einer Gleichung keine Lösungen gefunden
     * werden konnten oder ob die Gleichung definitiv alle reellen Zahlen als
     * Lösungen besitzt.
     */
    public static final ExpressionCollection ALL_REALS = new ExpressionCollection();
    public static final ExpressionCollection NO_SOLUTIONS = new ExpressionCollection();

    public static void setSolveTries(int n) {
        solveTries = n;
    }

    /**
     * Hauptprozedur zum algebraischen Lösen von Gleichungen f(x) = g(x).
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveGeneralEquation(Expression f, Expression g, String var) throws EvaluationException {

        System.out.println(solveTries + ": Löse allg. Gl. " + f.writeExpression() + " = " + g.writeExpression());

        if (solveTries <= 0) {
            return new ExpressionCollection();
        }
        solveTries--;

        // Zunächst beide Seiten entsprechend vereinfachen.
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.sort_difference_and_division);
        simplifyTypes.add(TypeSimplify.expand_rational_factors);
        simplifyTypes.add(TypeSimplify.simplify_powers);
        simplifyTypes.add(TypeSimplify.collect_products);
        simplifyTypes.add(TypeSimplify.factorize_all_but_rationals_in_sums);
        simplifyTypes.add(TypeSimplify.factorize_all_but_rationals_in_differences);
        simplifyTypes.add(TypeSimplify.factorize_in_sums);
        simplifyTypes.add(TypeSimplify.factorize_in_differences);
        simplifyTypes.add(TypeSimplify.reduce_quotients);
        simplifyTypes.add(TypeSimplify.reduce_leadings_coefficients);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);

        try {
            f = f.simplify(simplifyTypes);
            g = g.simplify(simplifyTypes);
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
        ExpressionCollection possibleZeros = elementaryEquivalentTransformation(f, g, var);
        if (!possibleZeros.isEmpty() || possibleZeros == NO_SOLUTIONS) {
            return possibleZeros;
        }

        // Falls die Gleichung die Form var = a, a unabhängig von var, besitzt.
        possibleZeros = solveVariableEquation(f, g, var);
        if (!possibleZeros.isEmpty() || possibleZeros == NO_SOLUTIONS) {
            return possibleZeros;
        }

        // Falls die Gleichung eine Potenzgleichung darstellt.
        possibleZeros = solvePowerEquation(f, g, var);
        if (!possibleZeros.isEmpty() || possibleZeros == NO_SOLUTIONS) {
            return possibleZeros;
        }

        // Falls die Gleichung eine Funktionsgleichung darstellt.
        possibleZeros = solveFunctionEquation(f, g, var);
        if (!possibleZeros.isEmpty() || possibleZeros == NO_SOLUTIONS) {
            return possibleZeros;
        }

        // Falls f und g einen gemeinsamen Faktor h im Zähler besitzen.
        possibleZeros = solveEquationWithCommonFactors(f, g, var);
        if (!possibleZeros.isEmpty() || possibleZeros == NO_SOLUTIONS) {
            return possibleZeros;
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
    public static ExpressionCollection solveVariableEquation(Expression f, Expression g, String var) {

        ExpressionCollection zero = new ExpressionCollection();
        if (f instanceof Variable && ((Variable) f).getName().equals(var) && !g.contains(var)) {
            zero.put(0, g);
        }
        return zero;

    }

    /**
     * Führt an f und g, wenn möglich, elementare Äquivalenzumformungen durch
     * und gibt dann die Lösungen der Gleichung f = g aus, falls welche gefunden
     * wurden.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection elementaryEquivalentTransformation(Expression f, Expression g, String var) throws EvaluationException {

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

                    if (fAsBinaryOperation.getRight().isOddConstant()) {
                        return solveGeneralEquation(fAsBinaryOperation.getLeft(), g.pow(ONE, fAsBinaryOperation.getRight()), var);
                    }
                    if (fAsBinaryOperation.getRight().isEvenConstant()) {
                        if (!g.isConstant() || g.isNonNegative()) {
                            ExpressionCollection resultPositive = solveGeneralEquation(fAsBinaryOperation.getLeft(), g.pow(ONE, fAsBinaryOperation.getRight()), var);
                            ExpressionCollection resultNegative = solveGeneralEquation(fAsBinaryOperation.getLeft(), Expression.MINUS_ONE.mult(g.pow(ONE, fAsBinaryOperation.getRight())), var);
                            return SimplifyUtilities.union(resultPositive, resultNegative);
                        }
                        return new ExpressionCollection();
                    }
                    if (fAsBinaryOperation.getRight().isRationalConstant()) {
                        BigInteger rootDegree = ((Constant) ((BinaryOperation) fAsBinaryOperation.getRight()).getRight()).getValue().toBigInteger();
                        if (((BinaryOperation) fAsBinaryOperation.getRight()).getRight().isEvenConstant()) {
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
        if (doesQuotientOccur(f) || doesQuotientOccur(g)) {
            return solveFractionalEquation(f, g, var);
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
    private static ExpressionCollection solveFractionalEquation(Expression f, Expression g, String var) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();

        // 1. Alle Nenner in f eliminieren, falls f var enthält.
        if (doesQuotientOccur(f) && f.contains(var)) {

            // Zunächst alle Summanden in f auf den kleinsten gemeinsamen Nenner bringen.
            f = SimplifyBinaryOperationMethods.bringFractionToCommonDenominator((BinaryOperation) f);
            /*
             Beide Seiten mit dem kleinsten gemeinsamen Nenner von f
             multiplizieren. WICHTIG: f ist nach der Anwendung von
             bringFractionToCommonDenominator() automatisch ein Bruch, d. h.
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
            g = SimplifyBinaryOperationMethods.bringFractionToCommonDenominator((BinaryOperation) g);
            /*
             Beide Seiten mit dem kleinsten gemeinsamen Nenner von g
             multiplizieren. WICHTIG: g ist nach der Anwendung von
             bringFractionToCommonDenominator() automatisch ein Bruch, d. h.
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

        return zeros;

    }

    /**
     * Prozedur zum Finden spezieller Lösungen von Gleichungen der Form
     * f(x)^p(x) = g(x)^q(x).
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solvePowerEquation(Expression f, Expression g, String var) throws EvaluationException {

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
                        validZero = validZero || (!baseOfFAtSpecialZero.isNonNegative() && ((BinaryOperation) exponentAtZero).getRight().isOddConstant());
                    }

                    if (validZero) {
                        zeros.add(possibleZeros.get(i));
                    }
                }
            }

            return zeros;

        }

        return new ExpressionCollection();

    }

    /**
     * Prozedur zum Lösen von Gleichungen der Form F(f(x)) = F(g(x)) oder
     * F(f(x)) = const, var == x.
     */
    public static ExpressionCollection solveFunctionEquation(Expression f, Expression g, String var) throws EvaluationException {

        if (!f.contains(var) || !(f instanceof Function)) {
            return new ExpressionCollection();
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

        if (g instanceof Function && ((Function) g).getType().equals(TypeFunction.lg)) {
            try {
                ExpressionCollection possibleZeros = solveGeneralEquation(argument, ((Function) g).getLeft(), var);
                // Nichtpositive Lösungen aussortieren.
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    if (possibleZeros.get(i).isPositive()) {
                        zeros.add(possibleZeros.get(i));
                    }
                }
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
                ExpressionCollection possibleZeros = solveGeneralEquation(argument, new Constant(10).pow(g), var);
                // Nichtpositive Lösungen aussortieren.
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    if (possibleZeros.get(i).isPositive()) {
                        zeros.add(possibleZeros.get(i));
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
     * Methode zum Lösen einer Logarithmusgleichung ln(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationLn(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();

        if (g instanceof Function && ((Function) g).getType().equals(TypeFunction.ln)) {
            try {
                ExpressionCollection possibleZeros = solveGeneralEquation(argument, ((Function) g).getLeft(), var);
                // Nichtpositive Lösungen aussortieren.
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    if (possibleZeros.get(i).isPositive()) {
                        zeros.add(possibleZeros.get(i));
                    }
                }
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
                ExpressionCollection possibleZeros = solveGeneralEquation(argument, new Function(g, TypeFunction.exp), var);
                // Nichtpositive Lösungen aussortieren.
                for (int i = 0; i < possibleZeros.getBound(); i++) {
                    if (possibleZeros.get(i).isPositive()) {
                        zeros.add(possibleZeros.get(i));
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
     * Methode zum Lösen einer Logarithmusgleichung cosh(argument) = g nach der
     * Variablen var.
     */
    private static ExpressionCollection solveEquationCosh(Expression argument, Expression g, String var) {

        ExpressionCollection zeros = new ExpressionCollection();
        ExpressionCollection possibleZeros;
        ExpressionCollection zerosPositive;
        ExpressionCollection zerosNegative = new ExpressionCollection();

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
        String var = "K_";
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
            factorsF = SimplifyUtilities.getFactorsOfEnumeratorInExpression(((BinaryOperation) f).getLeft());
        } else {
            factorsF = SimplifyUtilities.getFactors(f);
        }

        if (g.isQuotient()) {
            factorsG = SimplifyUtilities.getFactorsOfEnumeratorInExpression(((BinaryOperation) g).getLeft());
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
    private static ExpressionCollection solveEquationWithCommonFactors(Expression f, Expression g, String var) {

        ExpressionCollection possibleZeros = new ExpressionCollection();
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
                    possibleZeros = SimplifyUtilities.union(solveGeneralEquation(fWithoutCommonFactors, gWithoutCommonFactors, var),
                            zerosOfCancelledFactors);
                }
            } catch (EvaluationException e) {
            }
        }

        return possibleZeros;

    }

    /**
     * Hauptmethode zum algebraischen Lösen von Gleichungen f = 0.
     *
     * @throws EvaluationException
     */
    public static ExpressionCollection solveZeroEquation(Expression f, String var) throws EvaluationException {

        System.out.println(solveTries + ": Löse Nullgl. " + f.writeExpression() + " = 0");

        if (solveTries <= 0) {
            return new ExpressionCollection();
        }
        solveTries--;

        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.sort_difference_and_division);
        simplifyTypes.add(TypeSimplify.expand_rational_factors);
        simplifyTypes.add(TypeSimplify.simplify_powers);
        simplifyTypes.add(TypeSimplify.collect_products);
        simplifyTypes.add(TypeSimplify.factorize_all_but_rationals_in_sums);
        simplifyTypes.add(TypeSimplify.factorize_all_but_rationals_in_differences);
        simplifyTypes.add(TypeSimplify.factorize_in_sums);
        simplifyTypes.add(TypeSimplify.factorize_in_differences);
        simplifyTypes.add(TypeSimplify.reduce_quotients);
        simplifyTypes.add(TypeSimplify.reduce_leadings_coefficients);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);

        try {
            f = f.simplify(simplifyTypes);
        } catch (EvaluationException e) {
            /*
             Wenn beim Vereinfachen etwas schief gelaufen ist, dann war das
             keine sinnvolle Gleichung und sie kann dementsprechend nicht
             gelöst werden.
             */
            return NO_SOLUTIONS;
        }

        // Fall: f ist ein Produkt.
        ExpressionCollection zeros = solveZeroProduct(f, var);
        if (!zeros.isEmpty() || zeros == NO_SOLUTIONS) {
            return zeros;
        }

        // Fall: f ist ein Quotient.
        zeros = solveZeroQuotient(f, var);
        if (!zeros.isEmpty() || zeros == NO_SOLUTIONS) {
            return zeros;
        }

        // Fall: f ist eine Potenz.
        zeros = solveZeroPower(f, var);
        if (!zeros.isEmpty() || zeros == NO_SOLUTIONS) {
            return zeros;
        }

        // Fall: f ist eine Funktion.
        zeros = solveZeroFunction(f, var);
        if (!zeros.isEmpty() || zeros == NO_SOLUTIONS) {
            return zeros;
        }

        // Fall: f ist nicht-negativ (unabhängig von var und anderen Variablen).
        if (f.isAlwaysNonNegative() || zeros == NO_SOLUTIONS) {
            /*
             Falls f = 0 und f stets nichtnegativ ist, dann wird die Lösung
             entweder in solveAlwaysNonNegativeExpressionEqualsZero()
             gefunden, oder gar nicht.
             */
            return solveAlwaysNonNegativeExpressionEqualsZero(f, var);
        }

        // Fall: f ist ein Polynom.
        if (SimplifyPolynomialMethods.isPolynomial(f, var)) {
            return solvePolynomialEquation(f, var);
        }

        // Fall: f is ein Polynom in var^(1/m) mit geeignetem m.
        if (PolynomialRootsMethods.isPolynomialAfterSubstitutionByRoots(f, var)) {
            return PolynomialRootsMethods.solvePolynomialEquationWithFractionalExponents(f, var);
        }

        // Fall: f ist eine rationale Funktion in einer Exponentialfunktion.
        if (SpecialEquationMethods.isRationalFunktionInExp(f, var, new HashSet())) {
            return SpecialEquationMethods.solveExponentialEquation(f, var);
        }

        // Fall: f ist eine rationale Funktion in trigonometrischen Funktionen.
        if (SpecialEquationMethods.isRationalFunktionInTrigonometricalFunctions(f, var, new HashSet())) {
            return SpecialEquationMethods.solveTrigonometricalEquation(f, var);
        }

        /*
         Fall: f besitzt Brüche. Dann alles mit dem Hauptnenner
         ausmultiplizieren und prüfen, ob es Lösungen gibt.
         */
        if (doesQuotientOccur(f)) {
            zeros = solveFractionalEquation(f, ZERO, var);
            if (!zeros.isEmpty() || zeros == NO_SOLUTIONS) {
                return zeros;
            }
        }

        /*
         Nächster Versuch: werden die üblichen Standardsubstitutionen
         ausprobiert. Im Folgenden stellt die HashMap setOfSubstitutions eine
         Menge von potiellen (einfachen) Substitutionen zur Verfügung.
         */
        ExpressionCollection setOfSubstitutions = new ExpressionCollection();
        getSuitableSubstitutionForEquation(f, var, setOfSubstitutions, true);
        Object fSubstituted;

        for (int i = 0; i < setOfSubstitutions.getBound(); i++) {

            fSubstituted = substitute(f, var, setOfSubstitutions.get(i), true);
            if (fSubstituted instanceof Expression) {

                ExpressionCollection zerosOfSubstitutedEquation = solveGeneralEquation(((Expression) fSubstituted).simplify(), ZERO,
                        getSubstitutionVariable(f));
                for (int j = 0; j < zerosOfSubstitutedEquation.getBound(); j++) {
                    zeros = SimplifyUtilities.union(zeros, solveGeneralEquation(setOfSubstitutions.get(i), zerosOfSubstitutedEquation.get(j), var));
                }

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
        HashSet<Expression> factorsOfVar = new HashSet<>();
        if (SpecialEquationMethods.isRationalFunktionInExp(fByDefinition, var, factorsOfVar)) {
            if (!fByDefinition.equals(f)) {
                return SpecialEquationMethods.solveExponentialEquation(fByDefinition, var);
            }
        }
        fByDefinition = f.simplifyReplaceTrigonometricalFunctionsByDefinitions();
        if (SpecialEquationMethods.isRationalFunktionInTrigonometricalFunctions(fByDefinition, var, factorsOfVar)) {
            if (!fByDefinition.equals(f)) {
                return solveZeroEquation(fByDefinition, var);
            }
        }

        return zeros;

    }

    /**
     * Ab hier kommen eine Reihe von Einzelfunktionen, die bestimmte Typen von
     * Gleichungen der Form f(x) = 0 lösen.
     */
    public static ExpressionCollection solveZeroProduct(Expression f, String var) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();
        /**
         * Bei Multiplikation: expr = f(x)*g(x) -> f(x) = 0, g(x) = 0 separat
         * lösen und die Lösungen dann vereinigen.
         */
        if (f.isProduct()) {
            ExpressionCollection zerosLeft = solveGeneralEquation(((BinaryOperation) f).getLeft(), ZERO, var);
            ExpressionCollection zerosRight = solveGeneralEquation(((BinaryOperation) f).getRight(), ZERO, var);
            zeros = SimplifyUtilities.union(zerosLeft, zerosRight);
        }
        return zeros;

    }

    public static ExpressionCollection solveZeroQuotient(Expression f, String var) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();
        /*
         Bei Division: expr = f(x)/g(x) -> f(x) = 0 lösen und dann prüfen, ob
         g(x) bei den Lösungen nicht verschwindet.
         */
        if (f.isQuotient()) {
            ExpressionCollection zerosLeft = solveGeneralEquation(((BinaryOperation) f).getLeft(), ZERO, var);
            Expression valueOfDenominatorAtZero;
            boolean validZero;
            /*
             Es müssen nun solche Nullstellen ausgeschlossen werden, welche
             zugleich Nullstellen des Nenners sind.
             */
            for (int i = 0; i < zerosLeft.getBound(); i++) {
                valueOfDenominatorAtZero = ((BinaryOperation) f).getRight().replaceVariable(var, zerosLeft.get(i)).simplify();
                validZero = !valueOfDenominatorAtZero.isConstant() || !valueOfDenominatorAtZero.equals(ZERO);
                if (validZero) {
                    zeros.add(zerosLeft.get(i));
                }
            }
        }
        return zeros;

    }

    public static ExpressionCollection solveZeroPower(Expression f, String var) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();
        /*
         Bei Potenzen: expr = f(x)^g(x) -> f(x) = 0 lösen und dann prüfen, ob
         g(x) bei den Lösungen nicht <= 0 wird, falls g(x) konstant ist. Falls
         g(x) an der betreffenden Nullstelle x noch von Parametern abhängt,
         dann soll dies eine gültige Nullstelle sein.
         */
        if (f.isPower()) {
            ExpressionCollection zerosLeft = solveGeneralEquation(((BinaryOperation) f).getLeft(), ZERO, var);
            Expression exponentAtZero;
            boolean validZero;

            for (int i = 0; i < zerosLeft.getBound(); i++) {
                exponentAtZero = ((BinaryOperation) f).getRight().replaceVariable(var, zerosLeft.get(i)).simplify();
                validZero = !exponentAtZero.isConstant() || (exponentAtZero.isNonNegative() && !exponentAtZero.equals(ZERO));
                if (validZero) {
                    zeros.add(zerosLeft.get(i));
                }
            }

        }
        return zeros;

    }

    public static ExpressionCollection solveZeroFunction(Expression f, String var) throws EvaluationException {

        return solveFunctionEquation(f, ZERO, var);

    }

    /**
     * Löst Gleichungen der Form f = 0, wobei f stets >= 0 ist.
     */
    public static ExpressionCollection solveAlwaysNonNegativeExpressionEqualsZero(Expression f, String var) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();

        if (!f.isAlwaysNonNegative()) {
            return zeros;
        }

        if (f.isAlwaysPositive()) {
            return NO_SOLUTIONS;
        }

        ExpressionCollection summands = SimplifyUtilities.getSummands(f);

        if (summands.getBound() <= 1) {
            return zeros;
        }

        /*
         Jeder Summand muss = 0 sein, da die Summanden ebenfalls alle stets
         nicht-negativ sind (unabhängig vom Wert von var).
         */
        zeros = solveGeneralEquation(summands.get(0), ZERO, var);
        for (int i = 1; i < summands.getBound(); i++) {
            zeros = SimplifyUtilities.intersection(zeros, solveGeneralEquation(summands.get(i), ZERO, var));
        }

        return zeros;

    }

    /**
     * Löst Gleichungen der Form f = 0, wobei f stets >= 0 ist.
     */
    public static ExpressionCollection solvePolynomialEquation(Expression f, String var) throws EvaluationException {

        ExpressionCollection zeros = new ExpressionCollection();

        if (!SimplifyPolynomialMethods.isPolynomial(f, var)) {
            return zeros;
        }

        BigInteger degree = SimplifyPolynomialMethods.degreeOfPolynomial(f, var);
        BigInteger order = SimplifyPolynomialMethods.orderOfPolynomial(f, var);
        /*
         Falls k := Ord(f) >= 0 -> 0 ist eine k-fache Nullstelle von f.
         Dividiere diese heraus und fahre fort.
         */
        if (order.compareTo(BigInteger.ZERO) > 0) {
            f = PolynomialRootsMethods.divideExpressionByPowerOfVar(f, var, order);
            zeros.put(0, ZERO);
        }

        degree = degree.subtract(order);

        /*
         TO DO: Lösungsmethoden für Polynome hohen Grades mit wenigen
         nichttrivialen Koeffizienten.
         */
        if (degree.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_DEGREE_OF_POLYNOMIAL_FOR_SOLVING_EQUATION)) > 0) {
            return zeros;
        }

        ExpressionCollection coefficients = PolynomialRootsMethods.getPolynomialCoefficients(f, var);
        zeros = SimplifyUtilities.union(zeros, PolynomialRootsMethods.solvePolynomialEquation(coefficients, var));
        return zeros;

    }

    /**
     * Ermittelt potenzielle Substitutionen für eine Gleichung. Der boolsche
     * Parameter beginning sagt aus, ob f den ganzen Ausdruck darstellt, oder
     * nur einen Teil eines größeren Ausdrucks bildet. Dies ist wichtig, damit
     * es keine Endlosschleifen gibt!
     */
    public static void getSuitableSubstitutionForEquation(Expression f, String var, ExpressionCollection setOfSubstitutions, boolean beginning) {

        /*
         Es wird Folgendes als potentielle Substitution angesehen: (1)
         innerhalb von Funktionsklammern (2) Basen von Potenzen mit konstantem
         Exponenten (3) Exponenten von Potenzen mit konstanter Basis. Ferner
         darf eine Substitution nicht konstant bzgl. var und keine Variable
         sein.
         */
        if (f.contains(var) && f instanceof BinaryOperation && f.isNotPower()) {
            getSuitableSubstitutionForEquation(((BinaryOperation) f).getLeft(), var, setOfSubstitutions, false);
            getSuitableSubstitutionForEquation(((BinaryOperation) f).getRight(), var, setOfSubstitutions, false);
        }
        if (f.isPower()) {

            if (!((BinaryOperation) f).getRight().contains(var)
                    && !((BinaryOperation) f).getRight().equals(ONE)
                    && ((BinaryOperation) f).getLeft().contains(var)
                    && !(((BinaryOperation) f).getLeft() instanceof Variable)) {
                setOfSubstitutions.add(((BinaryOperation) f).getLeft());
                getSuitableSubstitutionForEquation(((BinaryOperation) f).getLeft(), var, setOfSubstitutions, false);
            } else if (!((BinaryOperation) f).getLeft().contains(var)
                    && ((BinaryOperation) f).getRight().contains(var)
                    && !(((BinaryOperation) f).getRight() instanceof Variable)) {
                setOfSubstitutions.add(((BinaryOperation) f).getRight());
                getSuitableSubstitutionForEquation(((BinaryOperation) f).getRight(), var, setOfSubstitutions, false);
            }

        } else if (f instanceof Function) {

            /*
             Als potentielle Substitution kommt die Funktion selbst in Frage,
             wenn sie NICHT die Gesamte Funktion/Gleichung darstellt (wenn
             also !beginning gilt).
             */
            if (f.contains(var) && !beginning) {
                setOfSubstitutions.add(f);
            }
            // Weitere potentielle Substitutionen finden sich möglicherweise im Argument der Funktion.
            getSuitableSubstitutionForEquation(((Function) f).getLeft(), var, setOfSubstitutions, false);

        }

    }

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
            /*
             WICHTIG: Falls f = x und substitution = x^k ist, wird NICHT
             substitutiert. GRUND: Dies kann Endlosschleifen verursachen!
             */
            Object variableSubstituted = substituteVariable(var, substitution);
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
    public static Object substituteVariable(String var, Expression substitution) throws EvaluationException {

        Expression derivative = substitution.diff(var).simplify();
        if (derivative.equals(ZERO)) {
            return false;
        }

        // Berechnung von a = reciprocalOfDerivative.
        Expression reciprocalOfDerivative = ONE.div(derivative).simplify();
        if (reciprocalOfDerivative.isIntegerConstant()) {
            // Berechnung von b = rest.
            Expression rest = substitution.replaceVariable(var, ZERO).simplify();
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
    public static Object substituteInSum(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {

        ExpressionCollection summandsF = SimplifyUtilities.getSummands(f);
        ExpressionCollection nonConstantSummandsSubstitution = SimplifyUtilities.getNonConstantSummands(substitution, var);

        if (nonConstantSummandsSubstitution.isEmpty()) {
            // Sollte nie passieren, aber trotzdem sicherheitshalber.
            return false;
        }
        Expression firstNonConstantSummandInSubstitution = nonConstantSummandsSubstitution.get(0);

        /*
         Nun wird geprüft, ob in summandsF ein Summand auftaucht, welcher ein
         rationales Vielfaches (k-faches) von
         firstNonConstantSummandInSubstitution ist. Falls so ein Summand
         existiert, ist dieser eindeutig.
         */
        Expression k = ZERO;
        for (int i = 0; i < summandsF.getBound(); i++) {
            k = summandsF.get(i).div(firstNonConstantSummandInSubstitution).simplify();
            if (k.isIntegerConstantOrRationalConstant()) {
                break;
            }
        }

        /*
         Falls k keine rationale Zahl ist (oder == 0, was eigentlich nicht
         passieren sollte), dann wurde KEIN passender Summand gefunden. ->
         Versuche, in allen Summanden von f einzeln zu substitutieren.
         */
        if (!k.isIntegerConstantOrRationalConstant() || k.equals(ZERO)) {
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

        /*
         Nun werden alle Summanden von subst mit k multipliziert, und es wird
         getestet, ob alle diese Summanden nun in summandsF enthalten sind.
         */
        if (!k.equals(ONE)) {
            for (int i = 0; i < nonConstantSummandsSubstitution.getBound(); i++) {
                nonConstantSummandsSubstitution.put(i, k.mult(nonConstantSummandsSubstitution.get(i)).simplify());
            }
        }

        if (beginning) {
            if (k.equals(ONE)) {
                Expression rest = f.sub(substitution).simplify();
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return Variable.create(getSubstitutionVariable(f)).add((Expression) restSubstituted);
                }
            } else {
                Expression rest = f.sub(k.mult(substitution)).simplify();
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

        /*
         Der boolsche Parameter in substitute wird auf false gesetzt, was
         bedeutet, dass die Differenz von f und einem Vielfachen von subst
         NICHT mehr die Ausgangsgleichung repräsentiert.
         */
        Object restSubstituted = substitute(SimplifyUtilities.produceSum(fMinusMultipleOfSubstitution), var, substitution, false);
        if (restSubstituted instanceof Expression) {
            Expression constantSummandOfSubstitution = SimplifyUtilities.produceProduct(SimplifyUtilities.getConstantSummands(substitution, var));
            if (k.equals(ONE)) {
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
    public static Object substituteInDifference(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {

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

        /*
         Falls im Minuenden kein nichtkonstanter Summand zu finden war, dann
         im Substrahenden suchen. Dieser MUSS dort auftauchen!
         */
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

        /*
         Nun wird geprüft, ob in summandsRightF ein Summand auftaucht, welcher
         ein rationales Vielfaches von firstNonConstantSummandInSubstitution
         ist. Falls so ein Summand existiert, ist dieser eindeutig.
         */
        if (!k.isIntegerConstantOrRationalConstant() || k.equals(ZERO)) {
            for (int i = 0; i < summandsRightF.getBound(); i++) {
                k = summandsRightF.get(i).div(firstNonConstantSummandInSubstitution).simplify();
                if (k.isIntegerConstantOrRationalConstant()) {
                    potentialMultipleFoundInSummandsLeft = false;
                    break;
                }
            }
        }

        /*
         Falls k keine rationale Zahl ist (oder == 0, was eigentlich nicht
         passieren sollte), dann wurde KEIN passender Summand gefunden. ->
         Versuche, in allen Summanden von f einzeln zu substitutieren.
         */
        if (!k.isIntegerConstantOrRationalConstant() || k.equals(ZERO)) {

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

        /*
         Nun werden alle Summanden von substitution mit k multipliziert, und
         es wird getestet, ob alle nichtkonstanten Summanden nun in
         summandsLeftF und summandsRightF enthalten sind.
         */
        if (!k.equals(ONE)) {
            for (int i = 0; i < nonConstantSummandsLeftSubstitution.getBound(); i++) {
                nonConstantSummandsLeftSubstitution.put(i, k.mult(nonConstantSummandsLeftSubstitution.get(i)).simplify());
            }
            for (int i = 0; i < nonConstantSummandsRightSubstitution.getBound(); i++) {
                nonConstantSummandsRightSubstitution.put(i, k.mult(nonConstantSummandsRightSubstitution.get(i)).simplify());
            }
        }

        if (beginning) {
            if (potentialMultipleFoundInSummandsLeft != firstNonConstantFactorInSubstitutionIsInLeft) {
                k = k.mult(-1).simplify();
            }
            if (k.equals(ONE)) {
                Expression rest = f.sub(substitution).simplify();
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return Variable.create(getSubstitutionVariable(f)).add((Expression) restSubstituted);
                }
            } else {
                Expression rest = f.sub(k.mult(substitution)).simplify();
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
            if (summandsLeftFMinusMultipleOfSubstitutionLeft.getBound() != summandsLeftF.getBound() - nonConstantSummandsLeftSubstitution.getBound()
                    || summandsRightFMinusMultipleOfSubstitutionRight.getBound() != summandsRightF.getBound() - nonConstantSummandsRightSubstitution.getBound()) {
                return false;
            }
        } else {
            summandsLeftFMinusMultipleOfSubstitutionLeft = SimplifyUtilities.difference(summandsLeftF, nonConstantSummandsRightSubstitution);
            summandsRightFMinusMultipleOfSubstitutionRight = SimplifyUtilities.difference(summandsRightF, nonConstantSummandsLeftSubstitution);
            if (summandsLeftFMinusMultipleOfSubstitutionLeft.getBound() != summandsLeftF.getBound() - nonConstantSummandsRightSubstitution.getBound()
                    || summandsRightFMinusMultipleOfSubstitutionRight.getBound() != summandsRightF.getBound() - nonConstantSummandsLeftSubstitution.getBound()) {
                return false;
            }
        }

        /*
         Der boolsche Parameter in substitute wird in der folgenden Zeile auf
         false gesetzt, was bedeutet, dass die Differenz von f und einem
         Vielfachen von subst NICHT mehr die Ausgangsgleichung repräsentiert,
         sondern nur einen teil davon.
         */
        Object restSubstituted = substitute(SimplifyUtilities.produceDifference(summandsLeftFMinusMultipleOfSubstitutionLeft, summandsRightFMinusMultipleOfSubstitutionRight), var, substitution, false);
        if (restSubstituted instanceof Expression) {
            if (potentialMultipleFoundInSummandsLeft != firstNonConstantFactorInSubstitutionIsInLeft) {
                k = k.mult(-1).simplify();
            }
            Expression constantSummandOfSubstitution = SimplifyUtilities.produceDifference(SimplifyUtilities.getConstantSummandsLeftInExpression(substitution, var),
                    SimplifyUtilities.getConstantSummandsRightInExpression(substitution, var));
            if (k.equals(ONE)) {
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
    public static Object substituteInProduct(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {

        ExpressionCollection factorsF = SimplifyUtilities.getFactors(f);
        ExpressionCollection nonConstantFactorsSubstitution = SimplifyUtilities.getNonConstantFactors(substitution, var);

        if (nonConstantFactorsSubstitution.isEmpty()) {
            // Sollte nie passieren, aber trotzdem sicherheitshalber.
            return false;
        }
        Expression firstNonConstantFactorInSubstitution = nonConstantFactorsSubstitution.get(0);

        Expression exponentOfFirstNonConstantFactor = ONE;
        if (firstNonConstantFactorInSubstitution.isPower()) {
            /*
             Falls der erste Faktor (welcher zum Vergleich dient) etwa x^3 und
             nicht x ist, wird x als firstNonConstantFactorInSubstitution
             gewählt und im Hinterkopf der Exponent
             exponentOfFirstNonConstantFactor = 3 behalten.
             */
            exponentOfFirstNonConstantFactor = ((BinaryOperation) firstNonConstantFactorInSubstitution).getRight();
            firstNonConstantFactorInSubstitution = ((BinaryOperation) firstNonConstantFactorInSubstitution).getLeft();
        }

        /*
         Nun wird geprüft, ob in factorsF ein Faktor auftaucht, welcher eine
         rationale Potenz von firstNonConstantFactorInSubstitution ist. Falls
         so ein Faktor existiert, ist dieser eindeutig.
         */
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

        /*
         Falls k keine rationale Zahl ist, dann wurde KEIN passender Faktor
         gefunden. -> Versuche, in allen Faktoren von f einzeln zu
         substitutieren.
         */
        if (!k.isIntegerConstantOrRationalConstant() || k.equals(ZERO)) {
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

        /*
         Nun werden alle Faktoren von subst mit k potenziert, und es wird
         getestet, ob alle diese faktoren nun in factors_f enthalten sind.
         */
        if (!k.equals(ONE)) {
            for (int i = 0; i < nonConstantFactorsSubstitution.getBound(); i++) {
                nonConstantFactorsSubstitution.put(i, nonConstantFactorsSubstitution.get(i).pow(k).simplify());
            }
        }

        if (beginning) {
            if (k.equals(ONE)) {
                Expression rest = f.div(substitution).simplify();
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return Variable.create(getSubstitutionVariable(f)).mult((Expression) restSubstituted);
                }
            } else {
                Expression rest = f.div(substitution.pow(k)).simplify();
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

        /*
         Der boolsche Parameter in substitute wird auf false gesetzt, was
         bedeutet, dass der Quotient von f und einer Potenz von subst NICHT
         mehr die Ausgangsgleichung repräsentiert.
         */
        Object restSubstituted = substitute(SimplifyUtilities.produceProduct(factorsOfFDividedByPowerOfSubstitution), var, substitution, false);
        if (restSubstituted instanceof Expression) {
            Expression constantFactorOfSubstitution = SimplifyUtilities.produceProduct(SimplifyUtilities.getConstantFactors(substitution, var));
            if (k.equals(ONE)) {
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
    public static Object substituteInQuotient(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {

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

        /*
         Falls im Zähler kein nichtkonstanter Faktor zu finden war, dann im
         Substrahenden suchen. Dieser MUSS dort auftauchen!
         */
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
        Object k = ZERO;
        for (int i = 0; i < factorsEnumeratorF.getBound(); i++) {
            k = isPositiveIntegerPower(firstNonConstantFactorInSubstitution, factorsEnumeratorF.get(i));
            if (k instanceof Expression) {
                k = ((Expression) k).simplify();
                if (((Expression) k).isIntegerConstantOrRationalConstant()) {
                    break;
                }
            }
        }

        /*
         Nun wird geprüft, ob in factorsRightF ein Faktor auftaucht, welcher
         eine Potenz von firstNonConstantFactorInSubstitutionIsInLeft ist.
         Falls so ein Faktor existiert, ist dieser eindeutig.
         */
        if (!(k instanceof Expression) || !((Expression) k).isIntegerConstantOrRationalConstant()
                || !SimplifyAlgebraicExpressionMethods.isAdmissibleExponent((Expression) k) || k.equals(ZERO)) {
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

        /*
         Falls k keine rationale Zahl ist (oder == 0, was eigentlich nicht
         passieren sollte), dann wurde KEIN passender Faktor gefunden. ->
         Versuche, in allen Faktoren von f einzeln zu substitutieren.
         */
        if (!(k instanceof Expression) || !((Expression) k).isIntegerConstantOrRationalConstant()
                || !SimplifyAlgebraicExpressionMethods.isAdmissibleExponent((Expression) k) || k.equals(ZERO)) {

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

        /*
         Nun werden alle Faktoren von substitution in die k-te Potenz erhoben,
         und es wird getestet, ob alle nichtkonstanten Faktoren nun in
         factorsEnumeratorF und factorsDenominatorF enthalten sind.
         */
        if (!exponent.equals(ONE)) {
            for (int i = 0; i < nonConstantFactorsEnumeratorSubstitution.getBound(); i++) {
                nonConstantFactorsEnumeratorSubstitution.put(i, nonConstantFactorsEnumeratorSubstitution.get(i).pow(exponent).simplify());
            }
            for (int i = 0; i < nonConstantFactorsDenominatorSubstitution.getBound(); i++) {
                nonConstantFactorsDenominatorSubstitution.put(i, nonConstantFactorsDenominatorSubstitution.get(i).pow(exponent).simplify());
            }
        }

        if (beginning) {
            if (exponent.equals(ONE)) {
                Expression rest = f.div(substitution).simplify();
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return Variable.create(getSubstitutionVariable(f)).mult((Expression) restSubstituted);
                }
            } else {
                Expression rest = f.div(substitution.pow(exponent)).simplify();
                Object restSubstituted = substitute(rest, var, substitution, beginning);
                if (restSubstituted instanceof Expression) {
                    return Variable.create(getSubstitutionVariable(f)).pow(exponent).mult((Expression) restSubstituted);
                }
            }
            return false;
        }

        ExpressionCollection factorsEnumeratorFDividedByPowerOfSubstitutionEnumerator = SimplifyUtilities.difference(factorsEnumeratorF, nonConstantFactorsEnumeratorSubstitution);
        ExpressionCollection factorsDenominatorFDividedByPowerOfSubstitutionDenominator = SimplifyUtilities.difference(factorsDenominatorF, nonConstantFactorsDenominatorSubstitution);

        if (factorsEnumeratorFDividedByPowerOfSubstitutionEnumerator.getBound() != factorsEnumeratorF.getBound() - nonConstantFactorsEnumeratorSubstitution.getBound()
                || factorsDenominatorFDividedByPowerOfSubstitutionDenominator.getBound() != factorsDenominatorF.getBound() - nonConstantFactorsDenominatorSubstitution.getBound()) {
            return false;
        }

        /*
         Der boolsche Parameter beginning (letzter Parameter) in substitute
         wird auf false gesetzt, was bedeutet, dass der Quotient von f und
         einer Potenz von subst NICHT mehr die Ausgangsgleichung
         repräsentiert.
         */
        Object restSubstituted = substitute(SimplifyUtilities.produceQuotient(factorsEnumeratorFDividedByPowerOfSubstitutionEnumerator,
                factorsDenominatorFDividedByPowerOfSubstitutionDenominator), var, substitution, false);
        if (restSubstituted instanceof Expression) {
            Expression constantFactorOfSubstitution = SimplifyUtilities.produceQuotient(SimplifyUtilities.getConstantFactorsOfEnumeratorInExpression(substitution, var),
                    SimplifyUtilities.getConstantFactorsOfDenominatorInExpression(substitution, var));
            if (exponent.equals(ONE)) {
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
    public static Object substituteInPower(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {

        if (f.isNotPower()) {
            return false;
        }

        Object fIsIntegerPowerOfSubstitution = isPositiveIntegerPower(substitution, f);
        if (fIsIntegerPowerOfSubstitution instanceof Expression) {
            return Variable.create(getSubstitutionVariable(f)).pow((Expression) fIsIntegerPowerOfSubstitution);
        }
        if (!((BinaryOperation) f).getRight().contains(var) && ((BinaryOperation) f).getLeft().contains(var)
                && !(((BinaryOperation) f).getLeft() instanceof Variable)) {

            Object baseSubstituted = substitute(((BinaryOperation) f).getLeft(), var, substitution, false);
            if (baseSubstituted instanceof Boolean) {
                return false;
            }
            return ((Expression) baseSubstituted).pow(((BinaryOperation) f).getRight());

        }
        if (!((BinaryOperation) f).getLeft().contains(var) && ((BinaryOperation) f).getRight().contains(var)
                && !(((BinaryOperation) f).getRight() instanceof Variable)) {

            Object exponentSubstituted = substitute(((BinaryOperation) f).getRight(), var, substitution, false);
            if (exponentSubstituted instanceof Boolean) {
                return false;
            }
            return ((BinaryOperation) f).getLeft().pow((Expression) exponentSubstituted);

        }

        /*
         Spezialfall: f = a^(c*x + d) und subst = b^(p*x + q) 
         Dann ist f = C * subst^D mit geeignetem C.
         */
        if (f.isPower() && !((BinaryOperation) f).getLeft().contains(var)
                && ((BinaryOperation) f).getRight().contains(var)
                && substitution.isPower() && !((BinaryOperation) substitution).getLeft().contains(var)
                && ((BinaryOperation) substitution).getRight().contains(var)) {

            // Berechnung von c, falls möglich.
            Expression c = ((BinaryOperation) f).getRight().diff(var).simplify();
            if (c.contains(var)){
                return false;
            }
            // Berechnung von d, falls möglich.
            Expression d = ((BinaryOperation) f).getRight().sub(c.mult(Variable.create(var))).simplify();
            if (d.contains(var)){
                return false;
            }
            
            // Berechnung von p, falls möglich.
            Expression p = ((BinaryOperation) f).getRight().diff(var).simplify();
            if (p.contains(var)){
                return false;
            }
            // Berechnung von q, falls möglich.
            Expression q = ((BinaryOperation) f).getRight().sub(p.mult(Variable.create(var))).simplify();
            if (q.contains(var)){
                return false;
            }

            Expression a = ((BinaryOperation) f).getLeft();
            Expression b = ((BinaryOperation) substitution).getLeft();
            
            // Berechnung von C und D.
            // C = a^(d - c*q/p), D = (ln(a)*c)/(ln(b)*d) 
            Expression factor = a.pow(d.sub(c.mult(q).div(p))).simplify();
            Expression exponent = a.ln().mult(c).div(b.ln().mult(p)).simplify();

            return factor.mult(substitution.pow(exponent));
            
        }

        return false;

    }

    /**
     * Versucht, falls f eine Funktion ist, diese durch einen Ausdruck von subst
     * zu ersetzen.
     *
     * @throws EvaluationException
     */
    public static Object substituteInFunction(Expression f, String var, Expression substitution, boolean beginning) throws EvaluationException {

        if (!(f instanceof Function)) {
            return false;
        }

        /*
         Sonderfall f = exp(a*h(x)+b) und substitution = exp(c*h(x)+d) mit x =
         var und n = a/c ganzzahlig. DANN: f = exp(b - n*d)*substitution^n,
         d.h. f ist ein Monom in subst.
         */
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

        /*
         Im Folgenden Sonderfälle: substitution ist eine Funktion, welche der
         Kehrwert von f ist (mit äquivalenten Argumenten).
         */
        if (f.isFunction(TypeFunction.sin) && substitution.isFunction(TypeFunction.cosec)
                && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        if (f.isFunction(TypeFunction.cosec) && substitution.isFunction(TypeFunction.sin)
                && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        if (f.isFunction(TypeFunction.cos) && substitution.isFunction(TypeFunction.sec)
                && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        if (f.isFunction(TypeFunction.sec) && substitution.isFunction(TypeFunction.cos)
                && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        if (f.isFunction(TypeFunction.tan) && substitution.isFunction(TypeFunction.cot)
                && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        if (f.isFunction(TypeFunction.cot) && substitution.isFunction(TypeFunction.tan)
                && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        if (f.isFunction(TypeFunction.sinh) && substitution.isFunction(TypeFunction.cosech)
                && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        if (f.isFunction(TypeFunction.cosech) && substitution.isFunction(TypeFunction.sinh)
                && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        if (f.isFunction(TypeFunction.cosh) && substitution.isFunction(TypeFunction.sech)
                && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        if (f.isFunction(TypeFunction.sech) && substitution.isFunction(TypeFunction.cosh)
                && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        if (f.isFunction(TypeFunction.tanh) && substitution.isFunction(TypeFunction.coth)
                && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        if (f.isFunction(TypeFunction.coth) && substitution.isFunction(TypeFunction.tanh)
                && ((Function) f).getLeft().equivalent(((Function) substitution).getLeft())) {
            return ONE.div(Variable.create(getSubstitutionVariable(f)));
        }

        // Alle anderen Fälle.
        Object fArgumentSubstituted = substitute(((Function) f).getLeft(), var, substitution, false);
        if (fArgumentSubstituted instanceof Boolean) {
            return false;
        }
        return new Function((Expression) fArgumentSubstituted, ((Function) f).getType());

    }

    /**
     * Gibt zurück, ob g eine positive ganzzahlige Potenz von f ist.
     * VORAUSSETZUNG: f und g sind KEINE Produkte. Falls ja, so wird der
     * entsprechende Exponent n zurückgegeben, so dass f^n = g. Andernfalls wird
     * false zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Object isPositiveIntegerPower(Expression f, Expression g) throws EvaluationException {

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

}
