package abstractexpressions.matrixexpression.basic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.classes.MatrixBinaryOperation;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.MatrixPower;
import java.util.Map;
import lang.translator.Translator;

public abstract class SimplifyMatrixUtilities {

    /**
     * Liefert die Differenz termsLeft \ termsRight (mit Vielfachheiten
     * gezählt!).
     */
    public static MatrixExpressionCollection difference(MatrixExpressionCollection matTermsLeft, MatrixExpressionCollection matTermsRight) {

        MatrixExpressionCollection result = new MatrixExpressionCollection();
        /*
         matTermsLeft und matTermsRight werden in manchen Prozeduren noch
         nachträglich gebraucht und sollten nicht verändert werden ->
         matTermsLeft und matTermsRight kopieren.
         */
        MatrixExpressionCollection matTermsLeftCopy = matTermsLeft.copy();
        MatrixExpressionCollection matTermsRightCopy = matTermsRight.copy();

        MatrixExpression matTermLeft, matTermRight;
        boolean equivalentMatTermFound;

        for (int i = 0; i < matTermsLeft.getBound(); i++) {

            if (matTermsLeftCopy.get(i) == null) {
                continue;
            }

            matTermLeft = matTermsLeftCopy.get(i);
            equivalentMatTermFound = false;
            for (int j = 0; j < matTermsRight.getBound(); j++) {

                if (matTermsRightCopy.get(j) == null) {
                    continue;
                }
                matTermRight = matTermsRightCopy.get(j);
                if (matTermLeft.equivalent(matTermRight)) {
                    equivalentMatTermFound = true;
                    matTermsLeftCopy.remove(i);
                    matTermsRightCopy.remove(j);
                    break;
                }

            }

            if (!equivalentMatTermFound) {
                result.add(matTermLeft);
            }

        }

        return result;

    }

    /**
     * Liefert den Durchschnitt von termsLeft und termsRight (mit Vielfachheiten
     * gezählt!).
     */
    public static MatrixExpressionCollection intersection(MatrixExpressionCollection matTermsLeft, MatrixExpressionCollection matTermsRight) {

        MatrixExpressionCollection termsLeftCopy = matTermsLeft.copy();
        MatrixExpressionCollection termsRightCopy = matTermsRight.copy();
        MatrixExpressionCollection result = new MatrixExpressionCollection();

        for (int i = 0; i < matTermsLeft.getBound(); i++) {
            for (int j = 0; j < matTermsRight.getBound(); j++) {
                if (termsLeftCopy.get(i) != null && termsRightCopy.get(j) != null && termsLeftCopy.get(i).equivalent(termsRightCopy.get(j))) {
                    result.add(termsLeftCopy.get(i));
                    termsLeftCopy.remove(i);
                    termsRightCopy.remove(j);
                }
            }
        }

        return result;

    }
    
    /**
     * Vereinigt zwei MatrixExpressionCollection, welche via 0, 1, 2, ... indiziert
     * sind und MatrixExpressions enthalten. Elemente in termsRight, welche in
     * termsLeft bereits vorkommen, werden NICHT mitaufgenommen.
     */
    public static MatrixExpressionCollection union(MatrixExpressionCollection termsLeft, MatrixExpressionCollection termsRight) {

        /*
         termsLeft und termsRight werden in manchen Prozeduren noch
         nachträglich gebraucht und sollten nicht verändert werden ->
         termsLeft und termsRight kopieren.
         */
        MatrixExpressionCollection termsLeftCopy = termsLeft.copy();
        boolean termIsContainedInTermsLeft;

        for (int i = 0; i < termsRight.getBound(); i++) {
            termIsContainedInTermsLeft = false;
            for (int j = 0; j < termsLeft.getBound(); j++) {
                if (termsLeftCopy.get(j).equivalent(termsRight.get(i))) {
                    termIsContainedInTermsLeft = true;
                    break;
                }
            }
            if (!termIsContainedInTermsLeft) {
                termsLeftCopy.add(termsRight.get(i));
            }
        }

        return termsLeftCopy;

    }
    
    /**
     * Fügt der MatrixExpressionCollection summands alle Summanden von matExpr
     * hinzu, falls man matExpr als Summe auffasst. Die Keys sind 0, 1, 2, ...,
     * size - 1.
     */
    public static void addSummands(MatrixExpression matExpr, MatrixExpressionCollection summands) {
        if (matExpr.isSum()) {
            addSummands(((MatrixBinaryOperation) matExpr).getLeft(), summands);
            addSummands(((MatrixBinaryOperation) matExpr).getRight(), summands);
        } else {
            summands.add(matExpr);
        }
    }

    /**
     * Gibt eine MatrixExpressionCollection mit allen Summanden von expr hinzu,
     * falls man expr als Summe auffasst. Die Keys sind 0, 1, 2, ..., size - 1.
     */
    public static MatrixExpressionCollection getSummands(MatrixExpression matExpr) {
        MatrixExpressionCollection summands = new MatrixExpressionCollection();
        addSummands(matExpr, summands);
        return summands;
    }

    /**
     * Liefert Summanden im Minuenden eines Ausdrucks. VORAUSSETZUNG: Der
     * Ausdruck muss in folgender Form sein, falls in diesem Differenzen
     * auftauchen: (...) - (...). Verboten sind also Ausdrücke wie a-(b-c) etc.
     * Ist expr keine Differenz, so wird expr als Minuend angesehen.<br>
     * BEISPIEL: (a+b^2+exp(x)) - (u+v) liefert die Summanden {a, b^2, exp(x)}
     * (als ExpressionCollection, nummeriert via 0, 1, 2).
     */
    public static MatrixExpressionCollection getSummandsLeftInMatrixExpression(MatrixExpression expr) {
        if (expr.isDifference()) {
            return getSummands(((MatrixBinaryOperation) expr).getLeft());
        }
        return getSummands(expr);
    }

    /**
     * Liefert Summanden im Subtrahenden eines Ausdrucks. VORAUSSETZUNG: Der
     * Ausdruck muss in folgender Form sein, falls in diesem Differenzen
     * auftauchen: (...) - (...). Verboten sind also Ausdrücke wie a-(b-c) etc.
     * Ist expr keine Differenz, so wird expr als Minuend angesehen.<br>
     * WICHTIG: Das Ergebnis kann auch leer sein!
     */
    public static MatrixExpressionCollection getSummandsRightInMatrixExpression(MatrixExpression expr) {
        if (expr.isDifference()) {
            return getSummands(((MatrixBinaryOperation) expr).getRight());
        }
        return new MatrixExpressionCollection();
    }

    /**
     * Fügt der MatrixExpressionCollection factors alle Faktoren (in der
     * richtigen Reihenfolge!) von matExpr hinzu, falls man expr als Produkt
     * auffasst. Die Keys sind 0, 1, 2, ..., size - 1.
     */
    public static void addFactors(MatrixExpression matExpr, MatrixExpressionCollection factors) {
        if (matExpr.isProduct()) {
            addFactors(((MatrixBinaryOperation) matExpr).getLeft(), factors);
            addFactors(((MatrixBinaryOperation) matExpr).getRight(), factors);
        } else {
            factors.add(matExpr);
        }
    }

    /**
     * Gibt eine MatrixExpressionCollection mit allen Faktoren (in der richtigen
     * Reihenfolge!) von matExpr hinzu, falls man matEpr als Produkt auffasst.
     * Die Keys sind 0, 1, 2, ..., size - 1.
     */
    public static MatrixExpressionCollection getFactors(MatrixExpression matExpr) {
        MatrixExpressionCollection factors = new MatrixExpressionCollection();
        addFactors(matExpr, factors);
        return factors;
    }

    /**
     * Bildet die Summe aus allen Termen von summands
     */
    public static MatrixExpression produceSum(MatrixExpressionCollection summands) {

        if (summands.isEmpty()) {
            summands.put(0, new Matrix(Expression.ZERO));
        }

        MatrixExpression result = new Matrix(Expression.ZERO);
        for (int i = summands.getBound() - 1; i >= 0; i--) {
            if (summands.get(i) == null) {
                continue;
            }
            if (result.equals(new Matrix(Expression.ZERO))) {
                result = summands.get(i);
            } else {
                result = summands.get(i).add(result);
            }
        }

        return result;

    }

    /**
     * Bildet die Differenz aus den Summen von allen Summanden in summandsLeft
     * und summandsRight.
     */
    public static MatrixExpression produceDifference(MatrixExpressionCollection summandsLeft, MatrixExpressionCollection summandsRight) {

        if (summandsLeft.isEmpty() && summandsRight.isEmpty()) {
            return MatrixExpression.getZeroMatrix(1, 1);
        } else if (!summandsLeft.isEmpty() && summandsRight.isEmpty()) {
            return produceSum(summandsLeft);
        } else if (summandsLeft.isEmpty() && !summandsRight.isEmpty()) {
            return MatrixExpression.MINUS_ONE.mult(produceSum(summandsRight));
        }
        return produceSum(summandsLeft).sub(produceSum(summandsRight));

    }

    /**
     * Bildet das Produkt aus allen Termen von factors
     */
    public static MatrixExpression produceProduct(MatrixExpressionCollection factors) {

        if (factors.isEmpty()) {
            factors.put(0, new Matrix(Expression.ONE));
        }

        MatrixExpression result = new Matrix(Expression.ONE);
        for (int i = factors.getBound() - 1; i >= 0; i--) {
            if (factors.get(i) == null) {
                continue;
            }
            if (result.equals(new Matrix(Expression.ONE))) {
                result = factors.get(i);
            } else {
                result = factors.get(i).mult(result);
            }
        }

        return result;

    }

    /**
     * Fasst in Produkten benachbarte Matrizenpotenzen mit äquivalenter Basis zu
     * einer einzigen Potenz zusammen.
     *
     * @throws EvaluationException
     */
    public static void collectFactorsInMatrixProduct(Map<Integer, MatrixExpression> factors, int l) throws EvaluationException {

        MatrixExpression base;
        Expression exponent;
        MatrixExpression baseToCompare;
        Expression exponentToCompare;

        for (int i = 0; i < l - 1; i++) {

            if (factors.get(i) == null || factors.get(i + 1) == null) {
                continue;
            }

            if (factors.get(i).isPower()) {
                base = ((MatrixPower) factors.get(i)).getLeft();
                exponent = ((MatrixPower) factors.get(i)).getRight();
            } else {
                base = factors.get(i);
                exponent = Expression.ONE;
            }

            if (factors.get(i + 1).isPower()) {
                baseToCompare = ((MatrixPower) factors.get(i + 1)).getLeft();
                exponentToCompare = ((MatrixPower) factors.get(i + 1)).getRight();
            } else {
                baseToCompare = factors.get(i + 1);
                exponentToCompare = Expression.ONE;
            }

            if (base.equivalent(baseToCompare)) {
                factors.put(i, base.pow(exponent.add(exponentToCompare)));
                factors.remove(i + 1);
            }

            // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
            if (Thread.interrupted()) {
                throw new EvaluationException(Translator.translateOutputMessage("MSM_SimplifyMatrixMethods_COMPUTATION_ABORTED"));
            }

        }

    }

    /**
     * Fasst in Produkten benachbarte Matrizenpotenzen mit äquivalenter Basis zu
     * einer einzigen Potenz zusammen.
     *
     * @throws EvaluationException
     */
    public static void collectFactorsInMatrixProduct(MatrixExpressionCollection factors) throws EvaluationException {

        MatrixExpression base;
        Expression exponent;
        MatrixExpression baseToCompare;
        Expression exponentToCompare;

        for (int i = 0; i < factors.getBound(); i++) {

            if (factors.get(i) == null || factors.get(i + 1) == null) {
                continue;
            }

            if (factors.get(i).isPower()) {
                base = ((MatrixPower) factors.get(i)).getLeft();
                exponent = ((MatrixPower) factors.get(i)).getRight();
            } else {
                base = factors.get(i);
                exponent = Expression.ONE;
            }

            if (factors.get(i + 1).isPower()) {
                baseToCompare = ((MatrixPower) factors.get(i + 1)).getLeft();
                exponentToCompare = ((MatrixPower) factors.get(i + 1)).getRight();
            } else {
                baseToCompare = factors.get(i + 1);
                exponentToCompare = Expression.ONE;
            }

            if (base.equivalent(baseToCompare)) {
                factors.put(i, base.pow(exponent.add(exponentToCompare)));
                factors.remove(i + 1);
            }

            // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
            if (Thread.interrupted()) {
                throw new EvaluationException(Translator.translateOutputMessage("MSM_SimplifyMatrixMethods_COMPUTATION_ABORTED"));
            }

        }

    }

    /**
     * Hilfsprozeduren für das Sortieren von Summen/Differenzen und
     * Produkten/Quotienten
     */
    public static void orderDifference(MatrixExpression matExpr, MatrixExpressionCollection summandsLeft, MatrixExpressionCollection summandsRight) {

        if (matExpr.isNotSum() && matExpr.isNotDifference()) {
            summandsLeft.add(matExpr);
            return;
        }

        if (matExpr.isSum()) {
            orderDifference(((MatrixBinaryOperation) matExpr).getLeft(), summandsLeft, summandsRight);
            orderDifference(((MatrixBinaryOperation) matExpr).getRight(), summandsLeft, summandsRight);
        } else {
            orderDifference(((MatrixBinaryOperation) matExpr).getLeft(), summandsLeft, summandsRight);
            orderDifference(((MatrixBinaryOperation) matExpr).getRight(), summandsRight, summandsLeft);
        }

    }

}
