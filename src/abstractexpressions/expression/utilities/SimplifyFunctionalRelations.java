package abstractexpressions.expression.utilities;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.PI;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.TypeOperator;
import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class SimplifyFunctionalRelations {

    /**
     * Prüft, ob expr ein (rationales) Vielfaches einer Funktion vom Typ type
     * ist. Falls ja, wird das Argument und der Koeffizient (in Form von Zähler
     * und Nenner) zurückgegeben. Falls nein, so wird false zurückgegeben. Diese
     * Methode wird benötigt, um etwa zu prüfen, ob 7*cosh(x)/5 + 7*sinh(x)/5 zu
     * 7*exp(x)/5 vereinfacht werden kann.<br>
     * Beispiel: (1) expr = Für 2*sin(x), type = TypeFunction.sin wird {x, 2}
     * zurückgegeben.<br>
     * (2) expr = Für (-3)*exp(x)/5, type = TypeFunction.exp wird {x, -3, 5}
     * zurückgegeben.<br>
     * (3) expr = Für sin(x), type = TypeFunction.cos wird false
     * zurückgegeben.<br>
     * (4) expr = tan(u^2), type = TypeFunction.tan wird {u^2, 1} zurückgegeben.
     */
    private static Object[] isMultipleOfFunction(Expression expr, TypeFunction type) {

        // expr ist von der Form f(x).
        if (expr.isFunction(type)) {
            Object[] result = new Object[2];
            result[0] = ((Function) expr).getLeft();
            result[1] = BigDecimal.ONE;
            return result;
        }

        // expr ist von der Form a*f(x).
        if (expr.isProduct()
                && ((BinaryOperation) expr).getLeft() instanceof Constant
                && ((BinaryOperation) expr).getRight().isFunction(type)) {
            Object[] result = new Object[2];
            result[0] = ((Function) ((BinaryOperation) expr).getRight()).getLeft();
            result[1] = ((Constant) ((BinaryOperation) expr).getLeft()).getValue();
            return result;
        }

        // expr ist von der Form f(x)/a.
        if (expr.isQuotient()
                && ((BinaryOperation) expr).getRight() instanceof Constant
                && ((BinaryOperation) expr).getLeft().isFunction(type)) {
            Object[] result = new Object[3];
            result[0] = ((Function) ((BinaryOperation) expr).getLeft()).getLeft();
            result[1] = BigDecimal.ONE;
            result[2] = ((Constant) ((BinaryOperation) expr).getRight()).getValue();
            return result;
        }

        // expr ist von der Form a*f(x)/b.
        if (expr.isQuotient()
                && ((BinaryOperation) expr).getRight() instanceof Constant
                && ((BinaryOperation) expr).getLeft().isProduct()
                && ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getLeft() instanceof Constant
                && ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getRight().isFunction(type)) {
            Object[] result = new Object[3];
            result[0] = ((Function) ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getRight()).getLeft();
            result[1] = ((Constant) ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getLeft()).getValue();
            result[2] = ((Constant) ((BinaryOperation) expr).getRight()).getValue();
            return result;
        }

        Object[] result = new Object[1];
        result[0] = false;
        return result;

    }

    /**
     * Prüft, ob expr ein rationales Vielfaches eines Quadrats einer Funktion
     * vom Typ type ist. Falls ja, wird das Argument und der Koeffizient (in
     * Form von Zähler und Nenner) zurückgegeben. Falls nein, so wird false
     * zurückgegeben. Diese Methode wird benötigt, um etwa zu prüfen, ob
     * 7*cos(x)^2/5 + 7*sin(x)^2/5 zu 7/5 vereinfacht werden kann.
     */
    private static Object[] isMultipleOfSquareOfFunction(Expression expr, TypeFunction type) {

        if (!(expr instanceof BinaryOperation)) {
            Object[] result = new Object[1];
            result[0] = false;
            return result;
        }

        // expr ist von der Form f(x)^2.
        if (expr.isPower()
                && ((BinaryOperation) expr).getRight().equals(Expression.TWO)
                && ((BinaryOperation) expr).getLeft().isFunction(type)) {
            Object[] result = new Object[2];
            result[0] = ((Function) ((BinaryOperation) expr).getLeft()).getLeft();
            result[1] = BigDecimal.ONE;
            return result;
        }

        // expr ist von der Form a*f(x)^2.
        if (expr.isProduct() && ((BinaryOperation) expr).getLeft() instanceof Constant
                && ((BinaryOperation) expr).getRight().isPower()
                && ((BinaryOperation) ((BinaryOperation) expr).getRight()).getRight().equals(Expression.TWO)
                && ((BinaryOperation) ((BinaryOperation) expr).getRight()).getLeft().isFunction(type)) {
            Object[] result = new Object[2];
            result[0] = ((Function) ((BinaryOperation) ((BinaryOperation) expr).getRight()).getLeft()).getLeft();
            result[1] = ((Constant) ((BinaryOperation) expr).getLeft()).getValue();
            return result;
        }

        // expr ist von der Form f(x)^2/a.
        if (expr.isQuotient() && ((BinaryOperation) expr).getRight() instanceof Constant
                && ((BinaryOperation) expr).getLeft().isPower()
                && (((BinaryOperation) ((BinaryOperation) expr).getLeft()).getRight().equals(Expression.TWO))
                && ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getLeft().isFunction(type)) {
            Object[] result = new Object[3];
            result[0] = ((Function) ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getLeft()).getLeft();
            result[1] = BigDecimal.ONE;
            result[2] = ((Constant) ((BinaryOperation) expr).getRight()).getValue();
            return result;
        }

        // expr ist von der Form a*f(x)^2/b.
        if (expr.isQuotient() && ((BinaryOperation) expr).getRight() instanceof Constant
                && ((BinaryOperation) expr).getLeft().isProduct()
                && ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getLeft() instanceof Constant
                && ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getRight().isPower()
                && (((BinaryOperation) ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getRight()).getRight().equals(Expression.TWO))
                && ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getRight()).getLeft().isFunction(type)) {
            Object[] result = new Object[3];
            result[0] = ((Function) ((BinaryOperation) ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getRight()).getLeft()).getLeft();
            result[1] = ((Constant) ((BinaryOperation) ((BinaryOperation) expr).getLeft()).getLeft()).getValue();
            result[2] = ((Constant) ((BinaryOperation) expr).getRight()).getValue();
            return result;
        }

        Object[] result = new Object[1];
        result[0] = false;
        return result;

    }

    /**
     * Falls in expr der Ausdruck sin(x)^2 + cos(x)^2 auftaucht -> zu 1
     * vereinfachen.<br>
     * Beispiel: x+y+sin(a*b)^2+z+cos(a*b)^2 wird vereinfacht zu 1+x+y+z.
     */
    public static void reduceSumOfSquaresOfSineAndCosine(ExpressionCollection summands) {

        Object[] isFirstSummandSuitable, isSecondSummandSuitable;

        // Fall: sin(x)^2 steht VOR cos(x)^2
        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfSquareOfFunction(summands.get(i), TypeFunction.sin);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfSquareOfFunction(summands.get(j), TypeFunction.cos);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (isFirstSummandSuitable.length == isSecondSummandSuitable.length
                        && ((Expression) isFirstSummandSuitable[0]).equivalent((Expression) isSecondSummandSuitable[0])) {

                    if (isFirstSummandSuitable.length == 2) {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0) {
                            summands.put(i, new Constant((BigDecimal) isFirstSummandSuitable[1]));
                            summands.remove(j);
                            break;
                        }
                    } else {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0
                                && ((BigDecimal) isFirstSummandSuitable[2]).compareTo((BigDecimal) isSecondSummandSuitable[2]) == 0) {
                            summands.put(i, new Constant((BigDecimal) isFirstSummandSuitable[1]).div((BigDecimal) isFirstSummandSuitable[2]));
                            summands.remove(j);
                            break;
                        }
                    }

                }

            }

        }

        // Fall: cos(x)^2 steht VOR sin(x)^2
        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfSquareOfFunction(summands.get(i), TypeFunction.cos);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfSquareOfFunction(summands.get(j), TypeFunction.sin);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (isFirstSummandSuitable.length == isSecondSummandSuitable.length
                        && ((Expression) isFirstSummandSuitable[0]).equivalent((Expression) isSecondSummandSuitable[0])) {

                    if (isFirstSummandSuitable.length == 2) {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0) {
                            summands.remove(i);
                            summands.remove(j);
                            summands.put(i, new Constant((BigDecimal) isFirstSummandSuitable[1]));
                            break;
                        }
                    } else {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0
                                && ((BigDecimal) isFirstSummandSuitable[2]).compareTo((BigDecimal) isSecondSummandSuitable[2]) == 0) {
                            summands.remove(i);
                            summands.remove(j);
                            summands.put(i, new Constant((BigDecimal) isFirstSummandSuitable[1]).div((BigDecimal) isFirstSummandSuitable[2]));
                            break;
                        }
                    }

                }

            }

        }

    }

    /**
     * Falls in expr der Ausdruck cosh(x)^2 - sinh(x)^2 auftaucht -> zu 1
     * vereinfachen. Falls in expr sinh(x)^2 - cosh(x)^2 auftaucht -> zu -1
     * vereinfachen.<br> 
     * Beispiel: x+y+cosh(a*b)^2+z-sinh(a*b)^2 wird vereinfacht zu 1+x+y+z.
     */
    public static void reduceDifferenceOfSquaresOfHypSineAndHypCosine(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) throws EvaluationException {

        Object[] isFirstSummandSuitable, isSecondSummandSuitable;

        // Fall: sinh(x)^2 steht VOR cosh(x)^2
        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfSquareOfFunction(summandsLeft.get(i), TypeFunction.sinh);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfSquareOfFunction(summandsRight.get(j), TypeFunction.cosh);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (isFirstSummandSuitable.length == isSecondSummandSuitable.length
                        && ((Expression) isFirstSummandSuitable[0]).equivalent((Expression) isSecondSummandSuitable[0])) {

                    if (isFirstSummandSuitable.length == 2) {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0) {
                            summandsRight.put(j, new Constant((BigDecimal) isFirstSummandSuitable[1]));
                            summandsLeft.remove(i);
                            break;
                        }
                    } else {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0
                                && ((BigDecimal) isFirstSummandSuitable[2]).compareTo((BigDecimal) isSecondSummandSuitable[2]) == 0) {
                            summandsRight.put(j, new Constant((BigDecimal) isFirstSummandSuitable[1]).div((BigDecimal) isFirstSummandSuitable[2]));
                            summandsLeft.remove(i);
                            break;
                        }
                    }

                }

            }

        }

        // Fall: cosh(x)^2 steht VOR sinh(x)^2
        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfSquareOfFunction(summandsLeft.get(i), TypeFunction.cosh);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfSquareOfFunction(summandsRight.get(j), TypeFunction.sinh);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (isFirstSummandSuitable.length == isSecondSummandSuitable.length
                        && ((Expression) isFirstSummandSuitable[0]).equivalent((Expression) isSecondSummandSuitable[0])) {

                    if (isFirstSummandSuitable.length == 2) {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0) {
                            summandsLeft.put(i, new Constant((BigDecimal) isFirstSummandSuitable[1]));
                            summandsRight.remove(j);
                            break;
                        }
                    } else {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0
                                && ((BigDecimal) isFirstSummandSuitable[2]).compareTo((BigDecimal) isSecondSummandSuitable[2]) == 0) {
                            summandsLeft.put(i, new Constant((BigDecimal) isFirstSummandSuitable[1]).div((BigDecimal) isFirstSummandSuitable[2]));
                            summandsRight.remove(j);
                            break;
                        }
                    }

                }

            }

        }

    }

    /**
     * Fasst in einer Summe Relationen der Form 1 + F(x)^2 zu G(x)^2 für
     * bestimmte Funktionstypen zusammen. Genauer: kommen in einer Summe die
     * Summanden 1 und F(x)^2 vor mit F.getTypeFunction() == type, so wird dies
     * zu G(x)^2 zusammengefasst, wobei G.getTypeFunction() == result_type ist.
     * Beispiel: 1 + tan(x)^2 wird zu sec(x)^2 vereinfacht, ebenso wie 1 +
     * cot(x)^2 zu cosec(x)^2.
     */
    public static void reduceOnePlusFunctionSquareToFunctionSquare(ExpressionCollection summands, TypeFunction type, TypeFunction resultType) {

        /*
         Entscheidend ist hierbei, dass oBdA angenommen werden kann, dass der
         konstante Summand im 0-ten Summanden steht, denn dies geschieht
         automatisch in der Hauptprozedur simplify().
         */
        if (summands.get(0) == null || !summands.get(0).isIntegerConstantOrRationalConstant()) {
            return;
        }

        Expression constantSummand = summands.get(0);
        Object[] isSummandSuitable;

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }
            isSummandSuitable = isMultipleOfSquareOfFunction(summands.get(i), type);
            if (isSummandSuitable.length == 1) {
                continue;
            }

            if (isSummandSuitable.length == 2) {
                if (new Constant((BigDecimal) isSummandSuitable[1]).equals(constantSummand)) {
                    summands.remove(0);
                    summands.put(i, constantSummand.mult(new Function((Expression) isSummandSuitable[0], resultType).pow(2)));
                    return;
                }
            } else {
                if (new Constant((BigDecimal) isSummandSuitable[1]).div((BigDecimal) isSummandSuitable[2]).equals(constantSummand)) {
                    summands.remove(0);
                    summands.put(i, constantSummand.mult(new Function((Expression) isSummandSuitable[0], resultType).pow(2)));
                    return;
                }
            }

        }

    }

    /**
     * Fasst in einer Summe Relationen der Form 1 - F(x)^2 zu G(x)^2 für
     * bestimmte Funktionstypen zusammen. Genauer: kommen in einer Differenz die
     * Summanden 1 und F(x)^2 vor mit F.getTypeFunction() == type, so wird dies
     * zu G(x)^2 zusammengefasst, wobei G.getTypeFunction() == result_type ist.
     * Beispiel: 1 - tanh(x)^2 wird zu sech(x)^2 vereinfacht.
     */
    public static void reduceOneMinusFunctionSquareToFunctionSquare(ExpressionCollection summandsLeft, ExpressionCollection summandsRight, TypeFunction type, TypeFunction resultType) {

        if ((summandsLeft.get(0) == null || !summandsLeft.get(0).isIntegerConstantOrRationalConstant())
                && (summandsRight.get(0) == null || !summandsRight.get(0).isIntegerConstantOrRationalConstant())) {
            return;
        }

        Expression constantSummand;
        Object[] isSummandSuitable;

        /*
         Entscheidend ist hierbei, dass oBdA angenommen werden kann, dass der
         konstante Summand im 0-ten Summanden in summandsLeft oder
         summandsRight steht, denn dies geschieht automatisch in der
         Hauptprozedur simplify().
         */
        if (summandsLeft.get(0) != null && summandsLeft.get(0).isIntegerConstantOrRationalConstant()) {

            constantSummand = summandsLeft.get(0);

            for (int i = 0; i < summandsRight.getBound(); i++) {

                // Fall 1 - F(x)^2 = G(x)^2.
                if (summandsRight.get(i) == null) {
                    continue;
                }
                isSummandSuitable = isMultipleOfSquareOfFunction(summandsRight.get(i), type);
                if (isSummandSuitable.length == 1) {
                    continue;
                }

                if (isSummandSuitable.length == 2) {
                    if (new Constant((BigDecimal) isSummandSuitable[1]).equals(constantSummand)) {
                        summandsRight.remove(i);
                        summandsLeft.put(0, constantSummand.mult(new Function((Expression) isSummandSuitable[0], resultType).pow(2)));
                        return;
                    }
                } else {
                    if (new Constant((BigDecimal) isSummandSuitable[1]).div(new Constant((BigDecimal) isSummandSuitable[2])).equals(constantSummand)) {
                        summandsRight.remove(i);
                        summandsLeft.put(0, constantSummand.mult(new Function((Expression) isSummandSuitable[0], resultType).pow(2)));
                        return;
                    }
                }

            }

        } else {

            constantSummand = summandsRight.get(0);

            for (int i = 0; i < summandsLeft.getBound(); i++) {

                // Fall F(x)^2 - 1 = -G(x)^2.
                if (summandsLeft.get(i) == null) {
                    continue;
                }
                isSummandSuitable = isMultipleOfSquareOfFunction(summandsLeft.get(i), type);
                if (isSummandSuitable.length == 1) {
                    continue;
                }

                if (isSummandSuitable.length == 2) {
                    if (new Constant((BigDecimal) isSummandSuitable[1]).equals(constantSummand)) {
                        summandsLeft.remove(i);
                        summandsRight.put(0, constantSummand.mult(new Function((Expression) isSummandSuitable[0], resultType).pow(2)));
                        return;
                    }
                } else {
                    if (new Constant((BigDecimal) isSummandSuitable[1]).div((BigDecimal) isSummandSuitable[2]).equals(constantSummand)) {
                        summandsLeft.remove(i);
                        summandsRight.put(0, constantSummand.mult(new Function((Expression) isSummandSuitable[0], resultType).pow(2)));
                        return;
                    }
                }

            }

        }

    }

    /**
     * Fasst in einer Summe Relationen der Form F(x)^2 - 1 zu G(x)^2 für
     * bestimmte Funktionstypen zusammen. Genauer: kommen in einer Differenz die
     * Summanden F(x)^2 und 1 vor mit F.getTypeFunction() == type, so wird dies
     * zu G(x)^2 zusammengefasst, wobei G.getTypeFunction() == result_type ist.
     * Beispiel: coth(x)^2 - 1 wird zu cosech(x)^2 vereinfacht.
     */
    public static void reduceFunctionSquareMinusOneToFunctionSquare(ExpressionCollection summandsLeft, ExpressionCollection summandsRight, TypeFunction type, TypeFunction resultType) throws EvaluationException {

        if ((summandsLeft.get(0) == null || !summandsLeft.get(0).isIntegerConstantOrRationalConstant())
                && (summandsRight.get(0) == null || !summandsRight.get(0).isIntegerConstantOrRationalConstant())) {
            return;
        }

        Expression constantSummand;
        Object[] isSummandSuitable;

        /*
         Entscheidend ist hierbei, dass oBdA angenommen werden kann, dass der
         konstante Summand im 0-ten Summanden in summandsLeft oder
         summandsRight steht, denn dies geschieht automatisch in der
         Hauptprozedur simplify().
         */
        if (summandsLeft.get(0) != null && summandsLeft.get(0).isIntegerConstantOrRationalConstant()) {

            constantSummand = summandsLeft.get(0);

            for (int i = 0; i < summandsRight.getBound(); i++) {

                // Fall 1 - F(x)^2 = -G(x)^2.
                if (summandsRight.get(i) == null) {
                    continue;
                }
                isSummandSuitable = isMultipleOfSquareOfFunction(summandsRight.get(i), type);
                if (isSummandSuitable.length == 1) {
                    continue;
                }

                if (isSummandSuitable.length == 2) {
                    if (new Constant((BigDecimal) isSummandSuitable[1]).equals(constantSummand)) {
                        summandsLeft.remove(0);
                        summandsRight.put(i, constantSummand.mult(new Function((Expression) isSummandSuitable[0], resultType).pow(2)));
                        return;
                    }
                } else {
                    if (new Constant((BigDecimal) isSummandSuitable[1]).div(new Constant((BigDecimal) isSummandSuitable[2])).equals(constantSummand)) {
                        summandsLeft.remove(0);
                        summandsRight.put(i, constantSummand.mult(new Function((Expression) isSummandSuitable[0], resultType).pow(2)));
                        return;
                    }
                }

            }

        } else {

            constantSummand = summandsRight.get(0);

            for (int i = 0; i < summandsLeft.getBound(); i++) {

                // Fall F(x)^2 - 1 = G(x)^2.
                if (summandsLeft.get(i) == null) {
                    continue;
                }
                isSummandSuitable = isMultipleOfSquareOfFunction(summandsLeft.get(i), type);
                if (isSummandSuitable.length == 1) {
                    continue;
                }

                if (isSummandSuitable.length == 2) {
                    if (new Constant((BigDecimal) isSummandSuitable[1]).equals(constantSummand)) {
                        summandsLeft.put(0, constantSummand.mult(new Function((Expression) isSummandSuitable[0], resultType).pow(2)));
                        summandsRight.remove(i);
                        return;
                    }
                } else {
                    if (new Constant((BigDecimal) isSummandSuitable[1]).div(new Constant((BigDecimal) isSummandSuitable[2])).equals(constantSummand)) {
                        summandsLeft.put(0, constantSummand.mult(new Function((Expression) isSummandSuitable[0], resultType).pow(2)));
                        summandsRight.remove(i);
                        return;
                    }
                }

            }

        }

    }

    /**
     * Fasst in einer Summe additive Relationen zusammen. Genauer: kommen in
     * einer Summe Summanden F(x) und G(x) vor mit F.getTypeFunction() ==
     * firstType, G.getTypeFunction() == secondType, so wird dies zu H(x)
     * zusammengefasst, wobei H.getTypeFunction() == result_type ist. Beispiel:
     * sinh(x) + cosh(x) wird zu exp(x) vereinfacht.
     */
    public static void sumOfTwoFunctions(ExpressionCollection summands, TypeFunction firstType, TypeFunction secondType,
            TypeFunction resultType) {

        Object[] isFirstSummandSuitable, isSecondSummandSuitable;

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfFunction(summands.get(i), firstType);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = 0; j < summands.getBound(); j++) {

                if (i == j || summands.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfFunction(summands.get(j), secondType);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (isFirstSummandSuitable.length == isSecondSummandSuitable.length
                        && ((Expression) isFirstSummandSuitable[0]).equivalent((Expression) isSecondSummandSuitable[0])) {

                    if (isFirstSummandSuitable.length == 2) {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0) {
                            summands.put(Math.min(i, j), new Constant((BigDecimal) isFirstSummandSuitable[1]).mult(new Function((Expression) isFirstSummandSuitable[0], resultType)));
                            summands.remove(Math.max(i, j));
                            break;
                        }
                    } else {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0
                                && ((BigDecimal) isFirstSummandSuitable[2]).compareTo((BigDecimal) isSecondSummandSuitable[2]) == 0) {
                            summands.put(Math.min(i, j), new Constant((BigDecimal) isFirstSummandSuitable[1]).mult(new Function((Expression) isFirstSummandSuitable[0], resultType)).div((BigDecimal) isFirstSummandSuitable[2]));
                            summands.remove(Math.max(i, j));
                            break;
                        }
                    }

                }

            }

        }

    }

    /**
     * Fasst in einem Produkt Faktoren F(x) und G(x) zu F(2*x)/2 zusammen, wobei
     * F.getTypeFunction() == firstType, G.getTypeFunction() == secondType ist.
     * Beispiel: sin(x)*cos(x) wird zu sin(2*x)/2 vereinfacht.<br>
     * Angewendet wird dies für die Funktionagleichungen sin(x)*cos(x) =
     * sin(2*x)/2 und sinh(x)*cosh(x) = sinh(2*x)/2.
     */
    public static void productOfTwoFunctionsToFunctionOfDoubleArgument(ExpressionCollection factors,
            TypeFunction firstType, TypeFunction secondType) {

        Expression factor, factorToCompare;

        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }

            // Keine innere Schleife beginnen, wenn nicht nötig.
            factor = factors.get(i);
            if (factor.isPower()) {
                if (!((BinaryOperation) factor).getLeft().isFunction(firstType)
                        && !((BinaryOperation) factor).getLeft().isFunction(secondType)) {
                    continue;
                }
            } else {
                if (!factor.isFunction(firstType) && !factor.isFunction(secondType)) {
                    continue;
                }
            }

            factor = factors.get(i);
            for (int j = i + 1; j < factors.getBound(); j++) {

                if (factors.get(j) == null) {
                    continue;
                }

                factorToCompare = factors.get(j);

                Expression exponent;
                if (factor.isPower() && factorToCompare.isPower()
                        && ((BinaryOperation) factor).getRight().equivalent(((BinaryOperation) factorToCompare).getRight())) {
                    exponent = ((BinaryOperation) factor).getRight();
                    factor = ((BinaryOperation) factor).getLeft();
                    factorToCompare = ((BinaryOperation) factorToCompare).getLeft();
                } else {
                    exponent = Expression.ONE;
                }

                if (factor.isFunction(firstType) && factorToCompare.isFunction(secondType)
                        && ((Function) factor).getLeft().equivalent(((Function) factorToCompare).getLeft())) {

                    factors.remove(j);
                    if (exponent.equals(Expression.ONE)) {
                        factors.put(i, new Function(Expression.TWO.mult(((Function) factor).getLeft()), firstType).div(2));
                    } else {
                        factors.put(i, (new Function(Expression.TWO.mult(((Function) factor).getLeft()), firstType)).pow(exponent).div(2));
                    }

                }

            }

        }

        // Fall: G(x) steht VOR F(x)
        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }

            // Keine innere Schleife beginnen, wenn nicht nötig.
            factor = factors.get(i);
            if (factor.isPower()) {
                if (!((BinaryOperation) factor).getLeft().isFunction(firstType)
                        && !((BinaryOperation) factor).getLeft().isFunction(secondType)) {
                    continue;
                }
            } else {
                if (!factor.isFunction(firstType) && !factor.isFunction(secondType)) {
                    continue;
                }
            }

            factor = factors.get(i);
            for (int j = i + 1; j < factors.getBound(); j++) {

                if (factors.get(j) == null) {
                    continue;
                }

                factorToCompare = factors.get(j);

                Expression exponent;
                if (factor.isPower() && factorToCompare.isPower()
                        && ((BinaryOperation) factor).getRight().equivalent(((BinaryOperation) factorToCompare).getRight())) {
                    exponent = ((BinaryOperation) factor).getRight();
                    factor = ((BinaryOperation) factor).getLeft();
                    factorToCompare = ((BinaryOperation) factorToCompare).getLeft();
                } else {
                    exponent = Expression.ONE;
                }

                if (factor.isFunction(secondType) && factorToCompare.isFunction(firstType)
                        && ((Function) factor).getLeft().equivalent(((Function) factorToCompare).getLeft())) {

                    factors.remove(j);
                    if (exponent.equals(Expression.ONE)) {
                        factors.put(i, new Function(Expression.TWO.mult(((Function) factor).getLeft()), firstType).div(2));
                    } else {
                        factors.put(i, (new Function(Expression.TWO.mult(((Function) factor).getLeft()), firstType)).pow(exponent).div(2));
                    }

                }

            }

        }

    }

    /**
     * Fasst cosh(x) - sinh(x) zu exp(-x) zusammen.
     */
    public static void reduceCoshMinusSinhToExp(ExpressionCollection summandsLeft, ExpressionCollection summandsRight) {

        Object[] isFirstSummandSuitable, isSecondSummandSuitable;

        // Fall: cosh(x) - sinh(x)
        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfFunction(summandsLeft.get(i), TypeFunction.cosh);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfFunction(summandsRight.get(j), TypeFunction.sinh);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (isFirstSummandSuitable.length == isSecondSummandSuitable.length
                        && ((Expression) isFirstSummandSuitable[0]).equivalent((Expression) isSecondSummandSuitable[0])) {

                    if (isFirstSummandSuitable.length == 2) {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0) {
                            summandsLeft.put(i, new Constant((BigDecimal) isFirstSummandSuitable[1]).mult(Expression.MINUS_ONE.mult((Expression) isFirstSummandSuitable[0]).exp()));
                            summandsRight.remove(j);
                            break;
                        }
                    } else {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0
                                && ((BigDecimal) isFirstSummandSuitable[2]).compareTo((BigDecimal) isSecondSummandSuitable[2]) == 0) {
                            summandsLeft.put(i, new Constant((BigDecimal) isFirstSummandSuitable[1]).mult(Expression.MINUS_ONE.mult((Expression) isFirstSummandSuitable[0]).exp()).div((BigDecimal) isFirstSummandSuitable[2]));
                            summandsRight.remove(j);
                            break;
                        }
                    }

                }

            }

        }

        // Fall: sinh(x) - cosh(x)
        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfFunction(summandsLeft.get(i), TypeFunction.sinh);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfFunction(summandsRight.get(j), TypeFunction.cosh);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (isFirstSummandSuitable.length == isSecondSummandSuitable.length
                        && ((Expression) isFirstSummandSuitable[0]).equivalent((Expression) isSecondSummandSuitable[0])) {

                    if (isFirstSummandSuitable.length == 2) {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0) {
                            summandsRight.put(j, new Constant((BigDecimal) isFirstSummandSuitable[1]).mult(Expression.MINUS_ONE.mult((Expression) isFirstSummandSuitable[0]).exp()));
                            summandsLeft.remove(i);
                            break;
                        }
                    } else {
                        if (((BigDecimal) isFirstSummandSuitable[1]).compareTo((BigDecimal) isSecondSummandSuitable[1]) == 0
                                && ((BigDecimal) isFirstSummandSuitable[2]).compareTo((BigDecimal) isSecondSummandSuitable[2]) == 0) {
                            summandsRight.put(j, new Constant((BigDecimal) isFirstSummandSuitable[1]).mult(
                                    Expression.MINUS_ONE.mult((Expression) isFirstSummandSuitable[0]).exp()).div((BigDecimal) isFirstSummandSuitable[2]));
                            summandsLeft.remove(i);
                            break;
                        }
                    }

                }

            }

        }

    }

    /**
     * Fasst in einem Produkt multiplikative Relationen zusammen. Genauer:
     * kommen in einem Produkt Faktoren F(x) und G(x) vor mit
     * F.getTypeFunction() == type_1, G.getTypeFunction() == type_2, so wird
     * dies zu H(x) zusammengefasst, wobei H.getTypeFunction() == result_type
     * ist. Beispiel: cos(x)*tan(x) wird zu sin(x) vereinfacht. Diese Funktion
     * kann auch gleiche Potenzen von solchen Funktionen zusammenfassen.
     * Konkret: F(x)^n*G(x)^n = H(x)^n.
     */
    public static void productOfTwoFunctions(ExpressionCollection factors, TypeFunction firstType, TypeFunction secondType,
            TypeFunction resultType) {

        Expression factor, factorToCompare, exponent;

        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }

            // Keine innere Schleife beginnen, wenn nicht nötig.
            factor = factors.get(i);
            if (factor.isPower()) {
                if (!((BinaryOperation) factor).getLeft().isFunction(firstType)
                        && !((BinaryOperation) factor).getLeft().isFunction(secondType)) {
                    continue;
                }
            } else {
                if (!factor.isFunction(firstType) && !factor.isFunction(secondType)) {
                    continue;
                }
            }

            for (int j = i + 1; j < factors.getBound(); j++) {

                if (factors.get(j) == null) {
                    continue;
                }

                factorToCompare = factors.get(j);
                if (factor.isPower() && factorToCompare.isPower()
                        && ((BinaryOperation) factor).getRight().equivalent(((BinaryOperation) factorToCompare).getRight())) {
                    exponent = ((BinaryOperation) factor).getRight();
                    factor = ((BinaryOperation) factor).getLeft();
                    factorToCompare = ((BinaryOperation) factorToCompare).getLeft();
                } else {
                    exponent = Expression.ONE;
                }

                if ((factor.isFunction(firstType) && factorToCompare.isFunction(secondType)
                        || factor.isFunction(secondType) && factorToCompare.isFunction(firstType))
                        && ((Function) factor).getLeft().equivalent(((Function) factorToCompare).getLeft())) {
                    factors.remove(j);
                    if (exponent.equals(Expression.ONE)) {
                        factors.put(i, new Function(((Function) factor).getLeft(), resultType));
                    } else {
                        factors.put(i, (new Function(((Function) factor).getLeft(), resultType)).pow(exponent));
                    }
                    break;
                }

            }

        }

    }

    /**
     * Fasst in einem Produkt multiplikative Relationen zusammen. Genauer:
     * kommen in einem Produkt Faktoren F(x) und G(x) vor mit
     * F.getTypeFunction() == type_1, G.getTypeFunction() == type_2, so wird
     * dies zu 1 vereinfacht. Beispiel: tan(x)*cot(x) wird zu 1 vereinfacht.
     * Diese Funktion kann auch gleiche Potenzen von solchen Funktionen
     * zusammenfassen. Konkret: F(x)^n*G(x)^n = 1.
     */
    public static void productOfTwoFunctionsEqualsOne(ExpressionCollection factors, TypeFunction firstType, TypeFunction secondType) {

        Expression factor, factorToCompare;

        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }

            factor = factors.get(i);
            for (int j = i + 1; j < factors.getBound(); j++) {

                if (factors.get(j) == null) {
                    continue;
                }

                factorToCompare = factors.get(j);
                if (factor.isPower() && factorToCompare.isPower()
                        && ((BinaryOperation) factor).getRight().equivalent(((BinaryOperation) factorToCompare).getRight())) {
                    factor = ((BinaryOperation) factor).getLeft();
                    factorToCompare = ((BinaryOperation) factorToCompare).getLeft();
                }

                if (factor instanceof Function && factorToCompare instanceof Function
                        && (((Function) factor).getType().equals(firstType) && ((Function) factorToCompare).getType().equals(secondType)
                        || ((Function) factor).getType().equals(secondType) && ((Function) factorToCompare).getType().equals(firstType))
                        && ((Function) factor).getLeft().equivalent(((Function) factorToCompare).getLeft())) {

                    factors.remove(i);
                    factors.remove(j);
                    factors.put(i, Expression.ONE);
                    break;

                }

            }

        }

    }

    /**
     * Fasst in einem Quotient eine Funktion F(x), welche im Nenner auftaucht,
     * als Funktion G(x) im Zähler auf. Dies gilt auch für Potenzen. Beispiel:
     * 1/cos(x)^5 = sec(x)^5.
     */
    public static void reciprocalOfFunction(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator,
            TypeFunction type, TypeFunction resultType) {

        for (int i = 0; i < factorsDenominator.getBound(); i++) {

            if (factorsDenominator.get(i) == null) {
                continue;
            }

            if (factorsDenominator.get(i).isPower()
                    && ((BinaryOperation) factorsDenominator.get(i)).getLeft() instanceof Function
                    && ((Function) ((BinaryOperation) factorsDenominator.get(i)).getLeft()).getType().equals(type)) {

                // Fall: factorsDenominator.get(i) ist eine Potenz von F(x).
                factorsEnumerator.add(new Function(((Function) ((BinaryOperation) factorsDenominator.get(i)).getLeft()).getLeft(), resultType).pow(
                        ((BinaryOperation) factorsDenominator.get(i)).getRight()));
                factorsDenominator.remove(i);

            } else if (factorsDenominator.get(i) instanceof Function && ((Function) factorsDenominator.get(i)).getType().equals(type)) {

                // Fall: denoms.get(i) == F(x).
                factorsEnumerator.add(new Function(((Function) factorsDenominator.get(i)).getLeft(), resultType));
                factorsDenominator.remove(i);

            }

        }

    }

    /**
     * Fasst in einem Produkt multiplikative Relationen zusammen. Genauer:
     * kommen in einem Quotient im Zähler bzw. im Nenner Faktoren F(x) bzw. G(x)
     * vor mit F.getTypeFunction() == type_1, G.getTypeFunction() == type_2, so
     * wird dies zu H(x) zusammengefasst, wobei H.getTypeFunction() ==
     * result_type ist. Beispiel: sin(x)/cos(x) wird zu tan(x) vereinfacht.
     * Diese Funktion kann auch gleiche Potenzen von solchen Funktionen
     * zusammenfassen. Konkret: F(x)/n*G(x)^n = H(x)^n.
     */
    public static void quotientOfTwoFunctions(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator,
            TypeFunction firstType, TypeFunction secondType, TypeFunction resultType) {

        Expression factor, factorToCompare;

        for (int i = 0; i < factorsEnumerator.getBound(); i++) {

            if (factorsEnumerator.get(i) == null) {
                continue;
            }

            // Keine innere Schleife beginnen, wenn nicht nötig.
            factor = factorsEnumerator.get(i);
            if (factor.isPower()) {
                if (!(((BinaryOperation) factor).getLeft() instanceof Function && (((Function) ((BinaryOperation) factor).getLeft()).getType().equals(firstType)
                        || ((Function) ((BinaryOperation) factor).getLeft()).getType().equals(secondType)))) {
                    continue;
                }
            } else {
                if (!(factor instanceof Function && (((Function) factor).getType().equals(firstType) || ((Function) factor).getType().equals(secondType)))) {
                    continue;
                }
            }

            factor = factorsEnumerator.get(i);
            for (int j = 0; j < factorsDenominator.getBound(); j++) {

                if (factorsDenominator.get(j) == null) {
                    continue;
                }

                factorToCompare = factorsDenominator.get(j);

                Expression exponent;
                if (factor.isPower() && factorToCompare.isPower()
                        && ((BinaryOperation) factor).getRight().equivalent(((BinaryOperation) factorToCompare).getRight())) {
                    exponent = ((BinaryOperation) factor).getRight();
                    factor = ((BinaryOperation) factor).getLeft();
                    factorToCompare = ((BinaryOperation) factorToCompare).getLeft();
                } else {
                    exponent = Expression.ONE;
                }

                if (factor instanceof Function && factorToCompare instanceof Function
                        && ((Function) factor).getType().equals(firstType) && ((Function) factorToCompare).getType().equals(secondType)
                        && ((Function) factor).getLeft().equivalent(((Function) factorToCompare).getLeft())) {

                    factorsDenominator.remove(j);
                    if (exponent.equals(Expression.ONE)) {
                        factorsEnumerator.put(i, new Function(((Function) factor).getLeft(), resultType));
                    } else {
                        factorsEnumerator.put(i, (new Function(((Function) factor).getLeft(), resultType)).pow(exponent));
                    }

                }

            }

        }

    }

    /**
     * Für abs() und sgn() gedacht: Es macht aus f(x)*f(y)*.... dann
     * f(x*y*...)*... .
     */
    public static void pullTogetherProductsOfMultiplicativeFunctions(ExpressionCollection factors, TypeFunction type) {

        Expression factor;

        // Zunächst: ganzzahlige Potenzen hineinziehen!
        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }

            factor = factors.get(i);
            if (factor.isPower()
                    && ((BinaryOperation) factor).getLeft() instanceof Function
                    && ((Function) ((BinaryOperation) factor).getLeft()).getType().equals(type)
                    && ((BinaryOperation) factor).getRight().isIntegerConstant()) {
                factors.put(i, new Function(((Function) ((BinaryOperation) factor).getLeft()).getLeft().pow(((BinaryOperation) factor).getRight()), type));
            }

        }

        Expression resultExpressionInFunction = Expression.ONE;
        int numberOfFunctions = 0, indexOfFirstFunction = -1;
        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }

            factor = factors.get(i);
            if (factor instanceof Function && ((Function) factor).getType().equals(type)) {
                if (resultExpressionInFunction.equals(Expression.ONE)) {
                    resultExpressionInFunction = ((Function) factor).getLeft();
                    numberOfFunctions++;
                    indexOfFirstFunction = i;
                } else {
                    resultExpressionInFunction = resultExpressionInFunction.mult(((Function) factor).getLeft());
                    numberOfFunctions++;
                }
                factors.remove(i);
            }

        }

        if (numberOfFunctions == 0) {
            return;
        }
        factors.put(indexOfFirstFunction, new Function(resultExpressionInFunction, type));

    }

    /**
     * Für abs() und sgn() gedacht: Es macht aus (f(x)*f(y)*....)/(f(z)*...)
     * dann f((x*y*...)/(z*...))*... .
     */
    public static void pullTogetherQuotientsOfMultiplicativeFunctions(ExpressionCollection factorsEnumerator, ExpressionCollection factorsDenominator, TypeFunction type) {

        Expression factor;

        Expression resultEnumeratorInFunction = Expression.ONE;
        Expression resultDenominatorInFunction = Expression.ONE;
        int indexOfFirstFunctionInEnumerator = -1, indexOfFirstFunctionInDenominator = -1;

        for (int i = 0; i < factorsEnumerator.getBound(); i++) {

            if (factorsEnumerator.get(i) == null) {
                continue;
            }

            factor = factorsEnumerator.get(i);
            if (factor instanceof Function && ((Function) factor).getType().equals(type)) {
                if (resultEnumeratorInFunction.equals(Expression.ONE)) {
                    resultEnumeratorInFunction = ((Function) factor).getLeft();
                    indexOfFirstFunctionInEnumerator = i;
                } else {
                    resultEnumeratorInFunction = resultEnumeratorInFunction.mult(((Function) factor).getLeft());
                }
                factorsEnumerator.remove(i);
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {

            if (factorsDenominator.get(i) == null) {
                continue;
            }

            factor = factorsDenominator.get(i);
            if (factor instanceof Function && ((Function) factor).getType().equals(type)) {
                if (resultDenominatorInFunction.equals(Expression.ONE)) {
                    resultDenominatorInFunction = ((Function) factor).getLeft();
                    indexOfFirstFunctionInDenominator = i;
                } else {
                    resultDenominatorInFunction = resultDenominatorInFunction.mult(((Function) factor).getLeft());
                }
                factorsDenominator.remove(i);
            }
        }

        if (indexOfFirstFunctionInEnumerator >= 0 && indexOfFirstFunctionInDenominator < 0) {
            factorsEnumerator.put(indexOfFirstFunctionInEnumerator, new Function(resultEnumeratorInFunction, type));
        } else if (indexOfFirstFunctionInEnumerator < 0 && indexOfFirstFunctionInDenominator >= 0) {
            factorsDenominator.put(indexOfFirstFunctionInDenominator, new Function(resultDenominatorInFunction, type));
        } else if (indexOfFirstFunctionInEnumerator >= 0 && indexOfFirstFunctionInDenominator >= 0) {
            factorsEnumerator.put(indexOfFirstFunctionInEnumerator, new Function(resultEnumeratorInFunction.div(resultDenominatorInFunction), type));
        }

    }

    /**
     * Vereinfacht Folgendes: Beispielsweise abs(a*x^5*y^8*z^(1/6)) =
     * x^4*y^8*z^(1/6)*abs(a).
     */
    public static Expression takeEvenPowersOutOfProductsInAbs(Expression expr) {

        if (!(expr instanceof Function) || !((Function) expr).getType().equals(TypeFunction.abs)) {
            return expr;
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(((Function) expr).getLeft());
        ExpressionCollection resultFactorsOutsideOfAbs = new ExpressionCollection();

        /*
         Gerade Potenzen von Faktoren innerhalb von abs() herausziehen. Ebenso
         Potenzen mit rationalem Exponenten, deren Nenner gerade und deren Zähler 
         ungerade ist.
         */
        Expression factor;
        BigInteger exponent;
        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }

            factor = factors.get(i);
            if (factor.isPower() && ((BinaryOperation) factor).getRight().isIntegerConstantOrRationalConstant()) {

                if (((BinaryOperation) factor).getRight().isRationalConstant()) {
                    if (((BinaryOperation) ((BinaryOperation) factor).getRight()).getLeft().isOddIntegerConstant()
                            && ((BinaryOperation) ((BinaryOperation) factor).getRight()).getRight().isEvenIntegerConstant()) {
                        resultFactorsOutsideOfAbs.add(factor);
                        factors.remove(i);
                    } else if (((BinaryOperation) ((BinaryOperation) factor).getRight()).getLeft().isEvenIntegerConstant()
                            && ((BinaryOperation) ((BinaryOperation) factor).getRight()).getRight().isOddIntegerConstant()) {
                        resultFactorsOutsideOfAbs.add(factor);
                        factors.remove(i);
                    }
                } else if (((BinaryOperation) factor).getRight().isIntegerConstant()) {
                    exponent = ((Constant) ((BinaryOperation) factor).getRight()).getValue().toBigInteger();
                    if (exponent.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                        resultFactorsOutsideOfAbs.add(factor);
                        factors.remove(i);
                    } else {
                        resultFactorsOutsideOfAbs.add(((BinaryOperation) factor).getLeft().pow(exponent.subtract(BigInteger.ONE)));
                        factors.put(i, ((BinaryOperation) factor).getLeft());
                    }
                }

            }

        }

        // Ergebnis bilden.
        if (resultFactorsOutsideOfAbs.isEmpty()) {
            // Dann konnten keine ganzzahligen Potenzen herausgezogen werden. -> expr zurückgeben.
            return expr;
        }

        return new Function(SimplifyUtilities.produceProduct(factors), TypeFunction.abs).mult(SimplifyUtilities.produceProduct(resultFactorsOutsideOfAbs));

    }

    /**
     * Vereinfacht Folgendes: Beispielsweise abs((a*x^5)/(y^8*b)) =
     * (x^4/y^8)*abs((a*x)/b).
     */
    public static Expression takeEvenPowersOutOfQuotientsInAbs(Expression expr) {

        if (!(expr instanceof Function) || !((Function) expr).getType().equals(TypeFunction.abs)) {
            return expr;
        }

        if (((Function) expr).getLeft().isNotQuotient()) {
            return expr;
        }

        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(((Function) expr).getLeft());
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(((Function) expr).getLeft());
        ExpressionCollection resultFactorsInEnumeratorOutsideOfAbs = new ExpressionCollection();
        ExpressionCollection resultFactorsInDenominatorOutsideOfAbs = new ExpressionCollection();

        /*
         Gerade Potenzen von Faktoren innerhalb von abs() herausziehen. Ebenso
         Potenzen mit rationalem Exponenten, deren Nenner gerade ist.
         */
        Expression factor;
        BigInteger exponent;
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i) != null) {
                factor = factorsEnumerator.get(i);
                if (factor.isPower() && ((BinaryOperation) factor).getRight().isIntegerConstantOrRationalConstant()) {

                    if (((BinaryOperation) factor).getRight().isRationalConstant()) {
                        if (((BinaryOperation) ((BinaryOperation) factor).getRight()).getLeft().isOddIntegerConstant() && ((BinaryOperation) ((BinaryOperation) factor).getRight()).getRight().isEvenIntegerConstant()) {
                            resultFactorsInEnumeratorOutsideOfAbs.add(factor);
                            factorsEnumerator.remove(i);
                        } else if (((BinaryOperation) ((BinaryOperation) factor).getRight()).getLeft().isEvenIntegerConstant() && ((BinaryOperation) ((BinaryOperation) factor).getRight()).getRight().isOddIntegerConstant()) {
                            resultFactorsInEnumeratorOutsideOfAbs.add(factor);
                            factorsEnumerator.remove(i);
                        }
                    } else if (((BinaryOperation) factor).getRight().isIntegerConstant()) {
                        exponent = ((Constant) ((BinaryOperation) factor).getRight()).getValue().toBigInteger();
                        if (exponent.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                            resultFactorsInEnumeratorOutsideOfAbs.add(factor);
                            factorsEnumerator.remove(i);
                        } else {
                            resultFactorsInEnumeratorOutsideOfAbs.add(((BinaryOperation) factor).getLeft().pow(exponent.subtract(BigInteger.ONE)));
                            factorsEnumerator.put(i, ((BinaryOperation) factor).getLeft());
                        }
                    }

                }
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i) != null) {
                factor = factorsDenominator.get(i);
                if (factor.isPower() && ((BinaryOperation) factor).getRight().isIntegerConstantOrRationalConstant()) {

                    if (((BinaryOperation) factor).getRight().isRationalConstant()) {
                        if (((BinaryOperation) ((BinaryOperation) factor).getRight()).getLeft().isOddIntegerConstant() && ((BinaryOperation) ((BinaryOperation) factor).getRight()).getRight().isEvenIntegerConstant()) {
                            resultFactorsInDenominatorOutsideOfAbs.add(factor);
                            factorsDenominator.remove(i);
                        } else if (((BinaryOperation) ((BinaryOperation) factor).getRight()).getLeft().isEvenIntegerConstant() && ((BinaryOperation) ((BinaryOperation) factor).getRight()).getRight().isOddIntegerConstant()) {
                            resultFactorsInDenominatorOutsideOfAbs.add(factor);
                            factorsDenominator.remove(i);
                        }
                    } else if (((BinaryOperation) factor).getRight().isIntegerConstant()) {
                        exponent = ((Constant) ((BinaryOperation) factor).getRight()).getValue().toBigInteger();
                        if (exponent.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                            resultFactorsInDenominatorOutsideOfAbs.add(factor);
                            factorsDenominator.remove(i);
                        } else {
                            resultFactorsInDenominatorOutsideOfAbs.add(((BinaryOperation) factor).getLeft().pow(exponent.subtract(BigInteger.ONE)));
                            factorsDenominator.put(i, ((BinaryOperation) factor).getLeft());
                        }
                    }

                }
            }
        }

        // Ergebnis bilden.
        Expression resultEnumeratorInAbs = Expression.ONE, resultDenominatorInAbs = Expression.ONE;
        if (resultFactorsInEnumeratorOutsideOfAbs.isEmpty() && resultFactorsInDenominatorOutsideOfAbs.isEmpty()) {

            /*
             Dann konnten weder im Zähler noch im Nenner ganzzahlige Potenzen
             herausgezogen werden. -> expr zurückgeben.
             */
            return expr;

        } else if (resultFactorsInEnumeratorOutsideOfAbs.isEmpty()) {

            for (int i = 0; i < factorsDenominator.getBound(); i++) {
                if (factorsDenominator.get(i) != null) {
                    if (resultDenominatorInAbs.equals(Expression.ONE)) {
                        resultDenominatorInAbs = factorsDenominator.get(i);
                    } else {
                        resultDenominatorInAbs = resultDenominatorInAbs.mult(factorsDenominator.get(i));
                    }
                }
            }
            return (new Function(((BinaryOperation) ((Function) expr).getLeft()).getLeft().div(resultDenominatorInAbs), TypeFunction.abs)).div(SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfAbs));

        } else if (resultFactorsInDenominatorOutsideOfAbs.isEmpty()) {

            for (int i = 0; i < factorsEnumerator.getBound(); i++) {
                if (factorsEnumerator.get(i) != null) {
                    if (resultEnumeratorInAbs.equals(Expression.ONE)) {
                        resultEnumeratorInAbs = factorsEnumerator.get(i);
                    } else {
                        resultEnumeratorInAbs = resultEnumeratorInAbs.mult(factorsEnumerator.get(i));
                    }
                }
            }
            return (new Function(resultEnumeratorInAbs.div(((BinaryOperation) ((Function) expr).getLeft()).getRight()), TypeFunction.abs)).mult(SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfAbs));

        } else {

            for (int i = 0; i < factorsEnumerator.getBound(); i++) {
                if (factorsEnumerator.get(i) != null) {
                    if (resultEnumeratorInAbs.equals(Expression.ONE)) {
                        resultEnumeratorInAbs = factorsEnumerator.get(i);
                    } else {
                        resultEnumeratorInAbs = resultEnumeratorInAbs.mult(factorsEnumerator.get(i));
                    }
                }
            }
            for (int i = 0; i < factorsDenominator.getBound(); i++) {
                if (factorsDenominator.get(i) != null) {
                    if (resultDenominatorInAbs.equals(Expression.ONE)) {
                        resultDenominatorInAbs = factorsDenominator.get(i);
                    } else {
                        resultDenominatorInAbs = resultDenominatorInAbs.mult(factorsDenominator.get(i));
                    }
                }
            }
            return (new Function(resultEnumeratorInAbs.div(resultDenominatorInAbs), TypeFunction.abs).mult(SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfAbs))).div(SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfAbs));

        }

    }

    /**
     * Zieht positive Konstanten aus Produkten in abs() und zieht negative
     * Konstanten aus abs() mit einem Minuszeichen heraus.
     */
    public static Expression takeConstantsOutOfProductsInAbs(Expression expr) {

        if (!(expr instanceof Function) || !((Function) expr).getType().equals(TypeFunction.abs)
                || ((Function) expr).getLeft().isNotProduct()) {
            return expr;
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(((Function) expr).getLeft());
        ExpressionCollection resultFactorsOutOfAbs = new ExpressionCollection();

        // Nichtnegative Konstanten herausziehen.
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i).isConstant() && factors.get(i).isNonNegative()) {
                resultFactorsOutOfAbs.add(factors.get(i));
                factors.remove(i);
            } else if (factors.get(i).isConstant() && factors.get(i).isNonPositive()) {
                resultFactorsOutOfAbs.add(Expression.MINUS_ONE.mult(factors.get(i)));
                factors.remove(i);
            }
        }

        // Ergebnis bilden.
        if (resultFactorsOutOfAbs.isEmpty()) {
            /*
             Dann gab es keine Konstanten in abs(), bei denen das Vorzeichen
             eindeutig entschieden werden konnte.
             */
            return expr;
        }

        return SimplifyUtilities.produceProduct(resultFactorsOutOfAbs).mult(new Function(SimplifyUtilities.produceProduct(factors), TypeFunction.abs));

    }

    /**
     * Zieht positive Konstanten aus Quotienten in abs() und zieht negative
     * Konstanten aus abs() mit einem Minuszeichen heraus.
     */
    public static Expression takeConstantsOutOfQuotientsInAbs(Expression expr) {

        if (!(expr instanceof Function) || !((Function) expr).getType().equals(TypeFunction.abs)
                || ((Function) expr).getLeft().isNotQuotient()) {
            return expr;
        }

        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(((Function) expr).getLeft());
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(((Function) expr).getLeft());
        ExpressionCollection resultFactorsInEnumeratorOutsideOfAbs = new ExpressionCollection();
        ExpressionCollection resultFactorsInDenominatorOutsideOfAbs = new ExpressionCollection();

        // Nichtnegative Konstanten aus dem Zähler herausziehen.
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i).isConstant() && factorsEnumerator.get(i).isNonNegative()) {
                resultFactorsInEnumeratorOutsideOfAbs.add(factorsEnumerator.get(i));
                factorsEnumerator.remove(i);
            } else if (factorsEnumerator.get(i).isConstant() && factorsEnumerator.get(i).isNonPositive()) {
                resultFactorsInEnumeratorOutsideOfAbs.add(Expression.MINUS_ONE.mult(factorsEnumerator.get(i)));
                factorsEnumerator.remove(i);
            }
        }
        // Nichtnegative Konstanten aus dem Nenner herausziehen.
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i).isConstant() && factorsDenominator.get(i).isNonNegative()) {
                resultFactorsInDenominatorOutsideOfAbs.add(factorsDenominator.get(i));
                factorsDenominator.remove(i);
            } else if (factorsDenominator.get(i).isConstant() && factorsDenominator.get(i).isNonPositive()) {
                resultFactorsInDenominatorOutsideOfAbs.add(Expression.MINUS_ONE.mult(factorsDenominator.get(i)));
                factorsDenominator.remove(i);
            }
        }

        // Ergebnis bilden.
        if (resultFactorsInEnumeratorOutsideOfAbs.isEmpty() && resultFactorsInDenominatorOutsideOfAbs.isEmpty()) {
            /*
             Dann gab es keine Konstanten in abs((...)/(...)), bei denen das
             Vorzeichen eindeutig entschieden werden konnte.
             */
            return expr;
        }

        return SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfAbs).mult(new Function(
                SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator),
                TypeFunction.abs)).div(SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfAbs));

    }

    public static Expression reduceAbsOfQuotientIfNumeratorHasFixedSign(Expression expr) {

        if (!(expr instanceof Function) || !((Function) expr).getType().equals(TypeFunction.abs) || !((Function) expr).getLeft().isQuotient()){
            return expr;
        }
        
        Expression numerator = ((BinaryOperation) ((Function) expr).getLeft()).getLeft();
        Expression denominator = ((BinaryOperation) ((Function) expr).getLeft()).getRight();
        
        if (numerator.isAlwaysNonNegative()){
            return numerator.div(denominator.abs());
        }
        
        if (numerator.isAlwaysNonPositive()){
            return MINUS_ONE.mult(numerator).div(denominator.abs());
        }
        
        return expr;

    }

    public static Expression reduceSgnOfQuotientIfNumeratorHasFixedSign(Expression expr) {

        if (!(expr instanceof Function) || !((Function) expr).getType().equals(TypeFunction.sgn) || !((Function) expr).getLeft().isQuotient()){
            return expr;
        }
        
        Expression numerator = ((BinaryOperation) ((Function) expr).getLeft()).getLeft();
        Expression denominator = ((BinaryOperation) ((Function) expr).getLeft()).getRight();
        
        if (numerator.isAlwaysPositive()){
            return ONE.div(denominator.abs());
        }
        
        if (numerator.isAlwaysNegative()){
            return MINUS_ONE.div(denominator.abs());
        }
        
        return expr;

    }

    /**
     * Vereinfacht Folgendes: Falls in sgn() ein Produkt vorkommt, so wird
     * x^(2*k) durch x^2, x^(2*k + 1) durch x, x^((2*k + 1)/(2*n + 1)) durch x
     * und x^(2*k/(2*n + 1)) durch x^2 ersetzt.
     */
    public static Expression removeSpecialPowersInProductsInSgn(Expression expr) {

        if (!(expr instanceof Function) || !((Function) expr).getType().equals(TypeFunction.sgn)
                || ((Function) expr).getLeft().isNotProduct()) {
            return expr;
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(((Function) expr).getLeft());

        // Gerade Potenzen von Faktoren in sgn() auf Quadrate reduzieren.
        Expression factor;
        BigInteger exponentEnumerator, exponentDenominator;
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) != null) {
                factor = factors.get(i);
                if (factor.isPower() && ((BinaryOperation) factor).getRight().isIntegerConstantOrRationalConstant()) {

                    if (((BinaryOperation) factor).getRight().isIntegerConstant()) {
                        exponentEnumerator = ((Constant) ((BinaryOperation) factor).getRight()).getValue().toBigInteger();
                        if (exponentEnumerator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                            factors.put(i, ((BinaryOperation) factor).getLeft().pow(Expression.TWO));
                        } else {
                            factors.put(i, ((BinaryOperation) factor).getLeft());
                        }
                    } else {
                        exponentEnumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) factor).getRight()).getLeft()).getValue().toBigInteger();
                        exponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) factor).getRight()).getRight()).getValue().toBigInteger();
                        if (exponentEnumerator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 1 && exponentDenominator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 1) {
                            factors.put(i, ((BinaryOperation) factor).getLeft());
                        } else if (exponentEnumerator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0 && exponentDenominator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 1) {
                            factors.put(i, ((BinaryOperation) factor).getLeft().pow(Expression.TWO));
                        }

                    }

                }
            }
        }

        // Ergebnis bilden.
        return new Function(SimplifyUtilities.produceProduct(factors), TypeFunction.sgn);

    }

    /**
     * Vereinfacht Folgendes: Falls in sgn() ein Quotient vorkommt, so wird
     * x^(2*k) durch x^2, x^(2*k + 1) durch x, x^((2*k + 1)/(2*n + 1)) durch x
     * und x^(2*k/(2*n + 1)) durch x^2 ersetzt.
     */
    public static Expression removeSpecialPowersInQuotientsInSgn(Expression expr) {

        if (!(expr instanceof Function) || !((Function) expr).getType().equals(TypeFunction.sgn)
                || ((Function) expr).getLeft().isNotQuotient()) {
            return expr;
        }

        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(((Function) expr).getLeft());
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(((Function) expr).getLeft());

        // Gerade Potenzen von Faktoren in sgn() auf Quadrate reduzieren.
        Expression factor;
        BigInteger exponentEnumerator, exponentDenominator;
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i) != null) {
                factor = factorsEnumerator.get(i);
                if (factor.isPower() && ((BinaryOperation) factor).getRight().isIntegerConstantOrRationalConstant()) {

                    if (((BinaryOperation) factor).getRight().isIntegerConstant()) {
                        exponentEnumerator = ((Constant) ((BinaryOperation) factor).getRight()).getValue().toBigInteger();
                        if (exponentEnumerator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                            factorsEnumerator.put(i, ((BinaryOperation) factor).getLeft().pow(Expression.TWO));
                        } else {
                            factorsEnumerator.put(i, ((BinaryOperation) factor).getLeft());
                        }
                    } else {
                        exponentEnumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) factor).getRight()).getLeft()).getValue().toBigInteger();
                        exponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) factor).getRight()).getRight()).getValue().toBigInteger();
                        if (exponentEnumerator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 1 && exponentDenominator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 1) {
                            factorsEnumerator.put(i, ((BinaryOperation) factor).getLeft());
                        } else if (exponentEnumerator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0 && exponentDenominator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 1) {
                            factorsEnumerator.put(i, ((BinaryOperation) factor).getLeft().pow(Expression.TWO));
                        }
                    }

                }
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i) != null) {
                factor = factorsDenominator.get(i);
                if (factor.isPower() && ((BinaryOperation) factor).getRight().isIntegerConstantOrRationalConstant()) {

                    if (((BinaryOperation) factor).getRight().isIntegerConstant()) {
                        exponentDenominator = ((Constant) ((BinaryOperation) factor).getRight()).getValue().toBigInteger();
                        if (exponentDenominator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                            factorsDenominator.put(i, ((BinaryOperation) factor).getLeft().pow(Expression.TWO));
                        } else {
                            factorsDenominator.put(i, ((BinaryOperation) factor).getLeft());
                        }
                    } else {
                        exponentEnumerator = ((Constant) ((BinaryOperation) ((BinaryOperation) factor).getRight()).getLeft()).getValue().toBigInteger();
                        exponentDenominator = ((Constant) ((BinaryOperation) ((BinaryOperation) factor).getRight()).getRight()).getValue().toBigInteger();
                        if (exponentEnumerator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 1 && exponentDenominator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 1) {
                            factorsDenominator.put(i, ((BinaryOperation) factor).getLeft());
                        } else if (exponentEnumerator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0 && exponentDenominator.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 1) {
                            factorsDenominator.put(i, ((BinaryOperation) factor).getLeft().pow(Expression.TWO));
                        }
                    }

                }
            }
        }

        // Ergebnis bilden.
        return new Function((SimplifyUtilities.produceProduct(factorsEnumerator)).div(SimplifyUtilities.produceProduct(factorsDenominator)), TypeFunction.sgn);

    }

    /**
     * Entfernt positive Konstanten aus Produkten in sgn() und ersetzt negative
     * Konstanten in sgn() durch ein Minuszeichen davor.
     */
    public static Expression removeConstantsInProductsInSgn(Expression expr) {

        if (!(expr instanceof Function) || !((Function) expr).getType().equals(TypeFunction.sgn)
                || ((Function) expr).getLeft().isNotProduct()) {
            return expr;
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(((Function) expr).getLeft());

        /*
         Konstanten mit eindeutigem Vorzeichen entfernen und entsprechendes
         Vorzeichen vor der Signumfunktion vermerken.
         */
        int signOutsideOfSgn = 1;
        boolean constantInSgnFound = false;
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i).isConstant() && factors.get(i).isNonNegative()) {
                constantInSgnFound = true;
                factors.remove(i);
            } else if (factors.get(i).isConstant() && factors.get(i).isNonPositive()) {
                signOutsideOfSgn = -signOutsideOfSgn;
                constantInSgnFound = true;
                factors.remove(i);
            }
        }

        // Ergebnis bilden.
        if (!constantInSgnFound) {
            /*
             Dann gab es keine Konstanten in sgn(), bei denen das Vorzeichen
             eindeutig entschieden werden konnte.
             */
            return expr;
        }

        if (signOutsideOfSgn == -1) {
            return (Expression.MINUS_ONE).mult(new Function(SimplifyUtilities.produceProduct(factors), TypeFunction.sgn));
        }
        return new Function(SimplifyUtilities.produceProduct(factors), TypeFunction.sgn);

    }

    /**
     * Entfernt positive Konstanten aus Quotienten in sgn() und ersetzt negative
     * Konstanten in sgn() durch ein Minuszeichen davor.
     */
    public static Expression removeConstantsInQuotientsInSgn(Expression expr) {

        if (!(expr instanceof Function) || !((Function) expr).getType().equals(TypeFunction.sgn)
                || ((Function) expr).getLeft().isNotQuotient()) {
            return expr;
        }

        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(((Function) expr).getLeft());
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(((Function) expr).getLeft());

        /*
         Konstanten mit eindeutigem Vorzeichen entfernen und entsprechendes
         Vorzeichen vor der Signumfunktion vermerken.
         */
        int signOutsideOfSgn = 1;
        boolean constantInSgnFound = false;
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i).isConstant() && factorsEnumerator.get(i).isNonNegative()) {
                constantInSgnFound = true;
                factorsEnumerator.remove(i);
            } else if (factorsEnumerator.get(i).isConstant() && factorsEnumerator.get(i).isNonPositive()) {
                signOutsideOfSgn = -signOutsideOfSgn;
                constantInSgnFound = true;
                factorsEnumerator.remove(i);
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i).isConstant() && factorsDenominator.get(i).isNonNegative()) {
                constantInSgnFound = true;
                factorsDenominator.remove(i);
            } else if (factorsDenominator.get(i).isConstant() && factorsDenominator.get(i).isNonPositive()) {
                signOutsideOfSgn = -signOutsideOfSgn;
                constantInSgnFound = true;
                factorsDenominator.remove(i);
            }
        }

        // Ergebnis bilden.
        if (!constantInSgnFound) {
            /*
             Dann gab es keine Konstanten in sgn(), bei denen das Vorzeichen
             eindeutig entschieden werden konnte.
             */
            return expr;
        }

        if (signOutsideOfSgn == -1) {
            return (Expression.MINUS_ONE).mult(new Function(SimplifyUtilities.produceProduct(factorsEnumerator).div(SimplifyUtilities.produceProduct(factorsDenominator)), TypeFunction.sgn));
        }
        return new Function(SimplifyUtilities.produceProduct(factorsEnumerator).div(SimplifyUtilities.produceProduct(factorsDenominator)), TypeFunction.sgn);

    }

    /**
     * Macht beispielsweise aus a*b*sgn(a*c)*c*d dann abs(a*c)*b*d. Es wird
     * immer nur eine Relation reduziert. Beim erneuten Durchlauf von simplify()
     * dann eventuell weitere.
     */
    public static void reduceProductOfIdAndSgnToAbs(ExpressionCollection factors) {

        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }

            if (factors.get(i) instanceof Function && ((Function) factors.get(i)).getType().equals(TypeFunction.sgn)) {
                ExpressionCollection factorsInsideSgn = SimplifyUtilities.getFactors(((Function) factors.get(i)).getLeft());

                /*
                 Nun müssen die Faktoren neu indiziert werden, damit es bei
                 difference() keine NullPointerExceptions gibt (dort müssen
                 sie geordnet sein).
                 */
                ExpressionCollection factorsOrdered = new ExpressionCollection();
                for (int j = 0; j < factors.getBound(); j++) {
                    if (factors.get(j) != null) {
                        factorsOrdered.add(factors.get(j));
                    }
                }

                if (SimplifyUtilities.difference(factorsOrdered, factorsInsideSgn).getBound() == factorsOrdered.getBound() - factorsInsideSgn.getBound()) {

                    factors.put(i, new Function(((Function) factors.get(i)).getLeft(), TypeFunction.abs));
                    // Nun alle einfließenden Faktoren aus factors beseitigen.
                    int numberOfFactorsInsideSgn = factorsInsideSgn.getBound();
                    for (int p = 0; p < numberOfFactorsInsideSgn; p++) {
                        for (int q = 0; q < factors.getBound(); q++) {

                            if (factors.get(q) == null) {
                                continue;
                            }
                            if (factors.get(q).equivalent(factorsInsideSgn.get(p))) {
                                factorsInsideSgn.remove(p);
                                factors.remove(q);
                                break;
                            }

                        }
                    }

                }
            }
        }

    }

    /**
     * Sammelt Fakultäten in einem Produkt mittels Ergänzungssatz.
     *
     * @throws EvaluationException
     */
    public static void collectFactorialsInProductByReflectionFormula(ExpressionCollection factors) throws EvaluationException {

        Expression base;
        Expression exponent;
        Expression baseToCompare;
        Expression exponentToCompare;

        Expression argument, sumOfArguments;
        
        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).isPower()) {
                base = ((BinaryOperation) factors.get(i)).getLeft();
                exponent = ((BinaryOperation) factors.get(i)).getRight();
            } else {
                base = factors.get(i);
                exponent = Expression.ONE;
            }

            for (int j = i + 1; j < factors.getBound(); j++) {

                if (factors.get(j) == null) {
                    continue;
                }
                if (factors.get(j).isPower()) {
                    baseToCompare = ((BinaryOperation) factors.get(j)).getLeft();
                    exponentToCompare = ((BinaryOperation) factors.get(j)).getRight();
                } else {
                    baseToCompare = factors.get(j);
                    exponentToCompare = Expression.ONE;
                }

                // Nun: x!^n*(-1-x)!^n = (Gamma(1+x)*Gamma(-x))^n = (x/sin(pi*x))^n.
                if (base.isOperator(TypeOperator.fac) && baseToCompare.isOperator(TypeOperator.fac) && exponent.equivalent(exponentToCompare)){
                    sumOfArguments = ((Expression) ((Operator) base).getParams()[0]).add((Expression) ((Operator) baseToCompare).getParams()[0]).simplify();
                    if (sumOfArguments.equals(MINUS_ONE)){
                        argument = (Expression) ((Operator) base).getParams()[0];
                        factors.put(i, argument.div(PI.mult(argument).sin()).pow(exponent));
                        factors.remove(j);
                    }
                }

            }

        }

    }
    
}
