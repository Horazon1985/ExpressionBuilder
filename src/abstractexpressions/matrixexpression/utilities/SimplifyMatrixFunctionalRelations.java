package abstractexpressions.matrixexpression.utilities;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.TypeFunction;
import flowcontroller.FlowController;
import java.awt.Dimension;
import java.math.BigInteger;
import java.util.ArrayList;
import abstractexpressions.matrixexpression.computation.EigenvaluesEigenvectorsAlgorithms;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.classes.MatrixBinaryOperation;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import abstractexpressions.matrixexpression.classes.MatrixFunction;
import abstractexpressions.matrixexpression.classes.MatrixPower;
import abstractexpressions.matrixexpression.classes.TypeMatrixBinary;
import abstractexpressions.matrixexpression.classes.TypeMatrixFunction;

public abstract class SimplifyMatrixFunctionalRelations {

    /**
     * Prüft, ob MatExpr ein (rationales) Vielfaches einer Matrixfunktion vom
     * Typ type ist. Falls ja, wird das Argument und der Koeffizient
     * zurückgegeben. Falls nein, so wird false zurückgegeben. Diese Methode
     * wird benötigt, um etwa zu prüfen, ob 7*cosh(A) + 7*sinh(A) zu 7*exp(A)
     * vereinfacht werden kann.<br>
     * Beispiel: (1) expr = Für 2*sin(x), type = TypeMatrixFunction.sin wird {x,
     * 2} zurückgegeben.<br>
     * (2) expr = Für sin(A), type = TypeMatrixFunction.cos wird false
     * zurückgegeben. (3) expr = exp(A^2), type = TypeMatrixFunction.exp wird
     * {A^2, 1} zurückgegeben.
     */
    private static Object[] isMultipleOfMatrixFunction(MatrixExpression MatExpr, TypeMatrixFunction type) {

        // matExpr ist von der Form f(A).
        if (MatExpr.isMatrixFunction(type)) {
            Object[] result = new Object[2];
            result[0] = ((MatrixFunction) MatExpr).getLeft();
            result[1] = Expression.ONE;
            return result;
        }

        // matExpr ist von der Form a*f(A).
        if (MatExpr.isProduct()
                && ((MatrixBinaryOperation) MatExpr).getLeft().convertOneTimesOneMatrixToExpression() instanceof Expression
                && ((MatrixBinaryOperation) MatExpr).getRight().isMatrixFunction(type)) {
            Object[] result = new Object[2];
            result[0] = ((MatrixFunction) ((MatrixBinaryOperation) MatExpr).getRight()).getLeft();
            result[1] = (Expression) ((MatrixBinaryOperation) MatExpr).getLeft().convertOneTimesOneMatrixToExpression();
            return result;
        }

        Object[] result = new Object[1];
        result[0] = false;
        return result;

    }

    /**
     * Prüft, ob expr ein rationales Vielfaches eines Quadrats einer
     * Matrixfunktion vom Typ type ist. Falls ja, wird das Argument und der
     * Koeffizient zurückgegeben. Falls nein, so wird false zurückgegeben. Diese
     * Methode wird benötigt, um etwa zu prüfen, ob 7*cos(A)^2 + 7*sin(A)^2 zu 7
     * vereinfacht werden kann.
     */
    private static Object[] isMultipleOfSquareOfMatrixFunction(MatrixExpression matExpr, TypeMatrixFunction type) {

        // expr ist von der Form f(A)^2.
        if (matExpr.isPower()
                && ((MatrixPower) matExpr).getRight().equals(Expression.TWO)
                && ((MatrixPower) matExpr).getLeft().isMatrixFunction(type)) {
            Object[] result = new Object[2];
            result[0] = ((MatrixFunction) ((MatrixPower) matExpr).getLeft()).getLeft();
            result[1] = Expression.ONE;
            return result;
        }

        // expr ist von der Form a*f(A)^2.
        if (matExpr.isProduct() && ((MatrixBinaryOperation) matExpr).getLeft().convertOneTimesOneMatrixToExpression() instanceof Expression
                && ((MatrixBinaryOperation) matExpr).getRight().isPower()
                && ((MatrixPower) ((MatrixBinaryOperation) matExpr).getRight()).getRight().equals(Expression.TWO)
                && ((MatrixPower) ((MatrixBinaryOperation) matExpr).getRight()).getLeft().isMatrixFunction(type)) {
            Object[] result = new Object[2];
            result[0] = ((MatrixFunction) ((MatrixPower) ((MatrixBinaryOperation) matExpr).getRight()).getLeft()).getLeft();
            result[1] = (Expression) ((MatrixBinaryOperation) matExpr).getLeft().convertOneTimesOneMatrixToExpression();
            return result;
        }

        Object[] result = new Object[1];
        result[0] = false;
        return result;

    }

    /**
     * Falls in expr der Ausdruck a*sin(A)^2 + a*cos(A)^2 auftaucht -> zu a*E
     * vereinfachen.<br>
     * Beispiel: A+3*sin(B)^2+C+z+3*cos(B)^2 wird vereinfacht zu A + 3*E + C.
     */
    public static void reduceSumOfSquaresOfSineAndCosine(MatrixExpressionCollection summands) throws EvaluationException {

        Object[] isFirstSummandSuitable, isSecondSummandSuitable;
        Dimension dim;

        // Fall: sin(A)^2 steht VOR cos(A)^2
        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfSquareOfMatrixFunction(summands.get(i), TypeMatrixFunction.sin);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfSquareOfMatrixFunction(summands.get(j), TypeMatrixFunction.cos);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (((MatrixExpression) isFirstSummandSuitable[0]).equivalent((MatrixExpression) isSecondSummandSuitable[0])
                        && ((Expression) isFirstSummandSuitable[1]).equivalent((Expression) isSecondSummandSuitable[1])) {
                    try {
                        dim = ((MatrixExpression) isFirstSummandSuitable[0]).getDimension();
                        summands.put(i, new Matrix((Expression) isFirstSummandSuitable[1]).mult(MatrixExpression.getId(dim.height)));
                        summands.remove(j);
                        break;
                    } catch (EvaluationException e) {
                    }
                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                FlowController.interruptComputationIfNeeded();

            }

        }

        // Fall: cos(A)^2 steht VOR sin(A)^2
        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfSquareOfMatrixFunction(summands.get(i), TypeMatrixFunction.cos);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfSquareOfMatrixFunction(summands.get(j), TypeMatrixFunction.sin);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (((Expression) isFirstSummandSuitable[0]).equivalent((Expression) isSecondSummandSuitable[0])
                        && ((Expression) isFirstSummandSuitable[1]).equivalent((Expression) isSecondSummandSuitable[1])) {
                    try {
                        dim = ((MatrixExpression) isFirstSummandSuitable[0]).getDimension();
                        summands.put(i, new Matrix((Expression) isFirstSummandSuitable[1]).mult(MatrixExpression.getId(dim.height)));
                        summands.remove(j);
                        break;
                    } catch (EvaluationException e) {
                    }
                    break;
                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                FlowController.interruptComputationIfNeeded();

            }

        }

    }

    /**
     * Falls in matExpr der Ausdruck cosh(A)^2 - sinh(A)^2 auftaucht -> zu E
     * vereinfachen. Falls in matExpr sinh(A)^2 - cosh(A)^2 auftaucht -> zu -E
     * vereinfachen.<br>
     * Beispiel: x+y+cosh(a*b)^2+z-sinh(a*b)^2 wird vereinfacht zu 1+x+y+z.
     */
    public static void reduceDifferenceOfSquaresOfHypSineAndHypCosine(MatrixExpressionCollection summandsLeft, MatrixExpressionCollection summandsRight) throws EvaluationException {

        Dimension dim;
        Object[] isFirstSummandSuitable, isSecondSummandSuitable;

        // Fall: sinh(x)^2 steht VOR cosh(x)^2
        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfSquareOfMatrixFunction(summandsLeft.get(i), TypeMatrixFunction.sinh);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfSquareOfMatrixFunction(summandsRight.get(j), TypeMatrixFunction.cosh);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (isFirstSummandSuitable.length == isSecondSummandSuitable.length
                        && ((MatrixExpression) isFirstSummandSuitable[0]).equivalent((MatrixExpression) isSecondSummandSuitable[0])) {

                    if (((Expression) isFirstSummandSuitable[1]).equivalent((Expression) isSecondSummandSuitable[1])) {
                        dim = summandsLeft.get(i).getDimension();
                        summandsRight.put(j, new Matrix((Expression) isFirstSummandSuitable[1]).mult(MatrixExpression.getId(dim.height)));
                        summandsLeft.remove(i);
                        break;
                    }

                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                FlowController.interruptComputationIfNeeded();

            }

        }

        // Fall: cosh(x)^2 steht VOR sinh(x)^2
        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfSquareOfMatrixFunction(summandsLeft.get(i), TypeMatrixFunction.cosh);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfSquareOfMatrixFunction(summandsRight.get(j), TypeMatrixFunction.sinh);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (isFirstSummandSuitable.length == isSecondSummandSuitable.length
                        && ((MatrixExpression) isFirstSummandSuitable[0]).equivalent((MatrixExpression) isSecondSummandSuitable[0])) {

                    if (((Expression) isFirstSummandSuitable[1]).equivalent((Expression) isSecondSummandSuitable[1])) {
                        dim = summandsLeft.get(i).getDimension();
                        summandsLeft.put(i, new Matrix((Expression) isFirstSummandSuitable[1]).mult(MatrixExpression.getId(dim.height)));
                        summandsRight.remove(j);
                        break;
                    }

                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                FlowController.interruptComputationIfNeeded();

            }

        }

    }

    /**
     * Fasst in einer Summe sinh(A) und cosh(A) zu exp(A) zusammen.
     */
    public static void reduceSinhPlusCoshToExp(MatrixExpressionCollection summands) throws EvaluationException {

        Object[] isFirstSummandSuitable, isSecondSummandSuitable;

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfMatrixFunction(summands.get(i), TypeMatrixFunction.sinh);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = 0; j < summands.getBound(); j++) {

                if (i == j || summands.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfMatrixFunction(summands.get(j), TypeMatrixFunction.cosh);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (isFirstSummandSuitable.length == isSecondSummandSuitable.length
                        && ((MatrixExpression) isFirstSummandSuitable[0]).equivalent((MatrixExpression) isSecondSummandSuitable[0])) {

                    if (((Expression) isFirstSummandSuitable[1]).equivalent((Expression) isSecondSummandSuitable[1])) {
                        summands.put(Math.min(i, j), new Matrix((Expression) isFirstSummandSuitable[1]).mult(((MatrixExpression) isFirstSummandSuitable[0]).exp()));
                        summands.remove(Math.max(i, j));
                        break;
                    }

                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                FlowController.interruptComputationIfNeeded();

            }

        }

    }

    /**
     * Fasst in einer Differenz Folgendes zusammen: cosh(A) - sinh(A) zu exp(-A)
     * und sinh(A) - cosh(A) zu -exp(-A) zusammen.
     */
    public static void reduceCoshMinusSinhToExp(MatrixExpressionCollection summandsLeft, MatrixExpressionCollection summandsRight) throws EvaluationException {

        Object[] isFirstSummandSuitable, isSecondSummandSuitable;

        // Fall: cosh(A) - sinh(A)
        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfMatrixFunction(summandsLeft.get(i), TypeMatrixFunction.cosh);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfMatrixFunction(summandsRight.get(j), TypeMatrixFunction.sinh);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (isFirstSummandSuitable.length == isSecondSummandSuitable.length
                        && ((MatrixExpression) isFirstSummandSuitable[0]).equivalent((MatrixExpression) isSecondSummandSuitable[0])) {

                    if (((Expression) isFirstSummandSuitable[1]).equivalent((Expression) isSecondSummandSuitable[1])) {
                        summandsLeft.put(i, new Matrix((Expression) isFirstSummandSuitable[1]).mult(MatrixExpression.MINUS_ONE.mult((MatrixExpression) isFirstSummandSuitable[0]).exp()));
                        summandsRight.remove(j);
                        break;
                    }

                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                FlowController.interruptComputationIfNeeded();

            }

        }

        // Fall: sinh(A) - cosh(A)
        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }
            isFirstSummandSuitable = isMultipleOfMatrixFunction(summandsLeft.get(i), TypeMatrixFunction.sinh);
            if (isFirstSummandSuitable.length == 1) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }
                isSecondSummandSuitable = isMultipleOfMatrixFunction(summandsRight.get(j), TypeMatrixFunction.cosh);
                if (isSecondSummandSuitable.length == 1) {
                    continue;
                }

                if (isFirstSummandSuitable.length == isSecondSummandSuitable.length
                        && ((MatrixExpression) isFirstSummandSuitable[0]).equivalent((MatrixExpression) isSecondSummandSuitable[0])) {

                    if (((Expression) isFirstSummandSuitable[1]).equivalent((Expression) isSecondSummandSuitable[1])) {
                        summandsRight.put(j, new Matrix((Expression) isFirstSummandSuitable[1]).mult(MatrixExpression.MINUS_ONE.mult((MatrixExpression) isFirstSummandSuitable[0]).exp()));
                        summandsLeft.remove(i);
                        break;
                    }

                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                FlowController.interruptComputationIfNeeded();

            }

        }

    }

    /**
     * Vereinfacht Doppelpotenzen, falls möglich. Ansonsten wird expr
     * zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyDoublePowers(MatrixExpression matExpr) throws EvaluationException {

        if (matExpr.isPower() && ((MatrixPower) matExpr).getLeft().isPower()) {
            // Dann einfach nur Exponenten ausmultiplizieren, falls möglich.
            return ((MatrixPower) ((MatrixPower) matExpr).getLeft()).getLeft().pow(((MatrixPower) ((MatrixPower) matExpr).getLeft()).getRight().mult(((MatrixPower) matExpr).getRight()).simplifyTrivial());
        }
        return matExpr;

    }

    /**
     * Falls matExpr die Determinante einer Dreiecksmatrix darstellt, so wird
     * diese explizit berechnet (als das Produkt der Diagonalelemente).
     * Ansonsten wird matExpr zurückgegeben.
     */
    public static MatrixExpression simplifyDetOfTriangularMatrix(MatrixExpression matExpr) {

        if (matExpr instanceof MatrixFunction && ((MatrixFunction) matExpr).getType().equals(TypeMatrixFunction.det)
                && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && ((Matrix) ((MatrixFunction) matExpr).getLeft()).isSquareMatrix()
                && (((Matrix) ((MatrixFunction) matExpr).getLeft()).isLowerTriangularMatrix()
                || ((Matrix) ((MatrixFunction) matExpr).getLeft()).isUpperTriangularMatrix())) {

            // Dann explizit ausrechnen.
            Expression det = Expression.ONE;
            for (int i = 0; i < ((Matrix) ((MatrixFunction) matExpr).getLeft()).getRowNumber(); i++) {
                det = det.mult(((Matrix) ((MatrixFunction) matExpr).getLeft()).getEntry(i, i));
            }

            return new Matrix(det);
        }

        return matExpr;

    }

    /**
     * Falls matExpr die Determinante einer Blockmatrix darstellt, so wird diese
     * explizit berechnet (als das Produkt der Determinanten der einzelnen
     * Blöcke). Ansonsten wird matExpr zurückgegeben.
     */
    public static MatrixExpression simplifyDetOfBlockMatrix(MatrixExpression matExpr) {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.det)
                && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && ((Matrix) ((MatrixFunction) matExpr).getLeft()).getLengthOfUpperSquareBlock() instanceof Integer) {
            // Dann muss die Matrix in der Determinante automatisch quadratisch sein.
            Matrix m = (Matrix) ((MatrixFunction) matExpr).getLeft();
            Dimension dim = m.getDimension();
            int lengthUpperBlock = (int) m.getLengthOfUpperSquareBlock();
            if (lengthUpperBlock == dim.height) {
                return matExpr;
            }

            Expression[][] entriesUpperBlock = new Expression[lengthUpperBlock][lengthUpperBlock];
            Expression[][] entriesLowerBlock = new Expression[dim.height - lengthUpperBlock][dim.width - lengthUpperBlock];

            // Oberen Block als Matrix bilden.
            for (int i = 0; i < lengthUpperBlock; i++) {
                for (int j = 0; j < lengthUpperBlock; j++) {
                    entriesUpperBlock[i][j] = m.getEntry(i, j);
                }
            }
            // Unteren Block als Matrix bilden.
            for (int i = lengthUpperBlock; i < dim.height; i++) {
                for (int j = lengthUpperBlock; j < dim.width; j++) {
                    entriesLowerBlock[i - lengthUpperBlock][j - lengthUpperBlock] = m.getEntry(i, j);
                }
            }

            return new Matrix(entriesUpperBlock).det().mult(new Matrix(entriesLowerBlock).det());

        }

        return matExpr;

    }

    /**
     * Vereinfacht det(A^n) = (det(A))^n.
     */
    public static MatrixExpression simplifyDetOfMatrixPower(MatrixExpression matExpr) {
        if (matExpr.isMatrixFunction(TypeMatrixFunction.det) && ((MatrixFunction) matExpr).getLeft().isPower()) {
            // Dann einfach den Exponenten aus der Determinante herausziehen.
            return ((MatrixPower) ((MatrixFunction) matExpr).getLeft()).getLeft().det().pow(((MatrixPower) ((MatrixFunction) matExpr).getLeft()).getRight());
        }
        return matExpr;
    }

    /**
     * Vereinfacht det(A_1 * ... * A_n) = det(A_1) * ... * det(A_n).
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyDetOfMatrixProducts(MatrixExpression matExpr) throws EvaluationException {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.det) && ((MatrixFunction) matExpr).getLeft().isProduct()) {

            // Es wird geprüft, ob das Argument ein gültiger Matrizenausdruck ist.
            ((MatrixFunction) matExpr).getLeft().getDimension();

            MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(((MatrixFunction) matExpr).getLeft());
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, new MatrixFunction(factors.get(i), TypeMatrixFunction.det));
            }
            return SimplifyMatrixUtilities.produceProduct(factors);

        }
        return matExpr;

    }

    /**
     * Vereinfacht tr(A_1 +- ... +- A_n) = tr(A_1) +- ... +- tr(A_n).
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyTrOfMatrixSums(MatrixExpression matExpr) throws EvaluationException {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.tr) && ((MatrixFunction) matExpr).getLeft().isSum()) {

            // Es wird geprüft, ob das Argument ein gültiger Matrizenausdruck ist.
            ((MatrixFunction) matExpr).getLeft().getDimension();

            if (((MatrixFunction) matExpr).getLeft().isSum()) {
                MatrixExpressionCollection summands = SimplifyMatrixUtilities.getSummands(((MatrixFunction) matExpr).getLeft());
                for (int i = 0; i < summands.getBound(); i++) {
                    summands.put(i, new MatrixFunction(summands.get(i), TypeMatrixFunction.tr));
                }
                return SimplifyMatrixUtilities.produceSum(summands);
            }
            if (((MatrixFunction) matExpr).getLeft().isDifference()) {
                return new MatrixBinaryOperation(((MatrixBinaryOperation) ((MatrixFunction) matExpr).getLeft()).getLeft().tr(),
                        ((MatrixBinaryOperation) ((MatrixFunction) matExpr).getLeft()).getRight().tr(), TypeMatrixBinary.MINUS);
            }

        }

        return matExpr;

    }

    /**
     * Vereinfacht tr(A^k * B * A^(-k)) = tr(B).
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyTrOfConjugatedMatrix(MatrixExpression matExpr) throws EvaluationException {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.tr) && ((MatrixFunction) matExpr).getLeft().isProduct()) {

            MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(((MatrixFunction) matExpr).getLeft());
            if (factors.getBound() == 3 && factors.get(0).isPower() && factors.get(2).isPower()
                    && ((MatrixPower) factors.get(0)).getLeft().equivalent(((MatrixPower) factors.get(2)).getLeft())) {

                Expression sumOfExponents = ((MatrixPower) factors.get(0)).getRight().add(((MatrixPower) factors.get(2)).getRight()).simplify();
                if (sumOfExponents.equals(Expression.ZERO)) {
                    return new MatrixFunction(factors.get(1), TypeMatrixFunction.tr);
                }

            }

        }

        return matExpr;

    }

    /**
     * Vereinfacht det(exp(A)) = exp(tr(A)).
     */
    public static MatrixExpression simplifyDetOfExpOfMatrix(MatrixExpression matExpr) {

        if (matExpr.isMatrixFunction(TypeMatrixFunction.det)
                && ((MatrixFunction) matExpr).getLeft().isMatrixFunction(TypeMatrixFunction.exp)) {
            // Achtung: Künstliches Casten zu MatrixExpression notwendig, da sonst die falsche Methode exp() angewendet wird. 
            return ((MatrixExpression) ((MatrixFunction) ((MatrixFunction) matExpr).getLeft()).getLeft().tr()).exp();
        }
        return matExpr;

    }

    /**
     * Falls matExpr eine Diagonalmatrix ist, so wird f(matExpr) explizit
     * berechnet. Ansonsten wird f(m) zurückgegeben.
     */
    public static MatrixExpression simplifyPowerSeriesFunctionOfDiagonalMatrix(MatrixExpression matExpr, TypeMatrixFunction type) {

        if (matExpr.isMatrixFunction(type)
                && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && ((Matrix) ((MatrixFunction) matExpr).getLeft()).isDiagonalMatrix()) {

            // Dann explizit ausrechnen.
            Expression[][] resultExtry = new Expression[((Matrix) ((MatrixFunction) matExpr).getLeft()).getRowNumber()][((Matrix) ((MatrixFunction) matExpr).getLeft()).getColumnNumber()];
            for (int i = 0; i < ((Matrix) ((MatrixFunction) matExpr).getLeft()).getRowNumber(); i++) {
                for (int j = 0; j < ((Matrix) ((MatrixFunction) matExpr).getLeft()).getColumnNumber(); j++) {
                    if (i != j) {
                        resultExtry[i][j] = Expression.ZERO;
                    } else {
                        resultExtry[i][j] = new Function(((Matrix) ((MatrixFunction) matExpr).getLeft()).getEntry(i, j), convertMatrixFunctionTypeToFunctionType(type));
                    }
                }
            }

            return new Matrix(resultExtry);
        }
        return matExpr;

    }

    /**
     * Konvertiert den Typ einer Matrixfunktion zu einem entsprechenden
     * Funktionstyp.
     */
    private static TypeFunction convertMatrixFunctionTypeToFunctionType(TypeMatrixFunction type) {
        switch (type) {
            case cos:
                return TypeFunction.cos;
            case cosh:
                return TypeFunction.cosh;
            case exp:
                return TypeFunction.exp;
            case ln:
                return TypeFunction.ln;
            case sin:
                return TypeFunction.sin;
            default:
                // Hier ist type == TypeMatrixFunction.sinh
                return TypeFunction.sinh;
        }
    }

    /**
     * Falls matExpr eine diagonalisierbare Matrix ist, so wird f(matExpr)
     * explizit berechnet. Ansonsten wird f(m) zurückgegeben.
     */
    public static MatrixExpression simplifyPowerSeriesFunctionOfDiagonalizableMatrix(MatrixExpression matExpr, TypeMatrixFunction type) {

        if (matExpr.isMatrixFunction(type)
                && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) ((MatrixFunction) matExpr).getLeft())) {

            Object eigenvectorMatrix = EigenvaluesEigenvectorsAlgorithms.getEigenvectorBasisMatrix((Matrix) ((MatrixFunction) matExpr).getLeft());
            if (eigenvectorMatrix instanceof Matrix) {
                try {
                    Matrix m = (Matrix) ((MatrixFunction) matExpr).getLeft();
                    MatrixExpression matrixInDiagonalForm = ((Matrix) eigenvectorMatrix).pow(-1).mult(m).mult((Matrix) eigenvectorMatrix).simplify();
                    if (matrixInDiagonalForm instanceof Matrix && ((Matrix) matrixInDiagonalForm).isDiagonalMatrix()) {
                        // Das Folgende kann dann direkt explizit berechnet werden.
                        return ((Matrix) eigenvectorMatrix).mult(new MatrixFunction(((Matrix) matrixInDiagonalForm), type)).mult(((Matrix) eigenvectorMatrix).pow(-1));
                    }
                } catch (EvaluationException e) {
                    return matExpr;
                }
            }

        }

        return matExpr;

    }

    /**
     * Falls matExpr eine nilpotente Matrix ist, so wird f(matExpr) explizit
     * berechnet. Ansonsten wird f(m) zurückgegeben.
     */
    public static MatrixExpression simplifyPowerSeriesFunctionOfNilpotentMatrix(MatrixExpression matExpr, TypeMatrixFunction type) {

        if (matExpr.isMatrixFunction(type)
                && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && ((Matrix) ((MatrixFunction) matExpr).getLeft()).isNilpotentMatrix()) {

            try {

                Dimension dim = ((Matrix) ((MatrixFunction) matExpr).getLeft()).getDimension();
                Matrix m = (Matrix) ((MatrixFunction) matExpr).getLeft();
                MatrixExpression powerOfM = m;
                int maxExponent = 1;
                while (!powerOfM.equals(MatrixExpression.getZeroMatrix(dim.height, dim.width)) && maxExponent < dim.height) {
                    powerOfM = powerOfM.mult(m).simplify();
                    maxExponent++;
                }

                ArrayList<Expression> taylorCoefficients = getTaylorCoefficientsOfFunction(type, maxExponent);
                if (taylorCoefficients.isEmpty()) {
                    // Dann war dieser Funktionstyp in der Methode nicht vorgesehen.
                    return matExpr;
                }
                MatrixExpression result = MatrixExpression.getZeroMatrix(dim.height, dim.width);
                // Ergebnispolynom bilden.
                for (int i = 0; i < taylorCoefficients.size(); i++) {
                    if (taylorCoefficients.get(i).equals(Expression.ZERO)) {
                        continue;
                    }
                    if (result.equals(MatrixExpression.getZeroMatrix(dim.height, dim.width))) {
                        result = new Matrix(taylorCoefficients.get(i)).mult(m.pow(i));
                    } else {
                        result = result.add(new Matrix(taylorCoefficients.get(i)).mult(m.pow(i)));
                    }
                }
                return result;

            } catch (EvaluationException e) {
            }

        }

        return matExpr;

    }

    private static ArrayList<Expression> getTaylorCoefficientsOfFunction(TypeMatrixFunction type, int n) throws EvaluationException {

        ArrayList<Expression> taylorCoefficients = new ArrayList<>();
        BigInteger denominator;

        if (type.equals(TypeMatrixFunction.cos)) {
            denominator = BigInteger.ONE;
            taylorCoefficients.add(Expression.ONE);
            for (int i = 1; i <= n; i++) {
                denominator = denominator.multiply(BigInteger.valueOf(i));
                if (i % 4 == 0) {
                    taylorCoefficients.add(Expression.ONE.div(denominator));
                } else if (i % 4 == 2) {
                    taylorCoefficients.add(Expression.MINUS_ONE.div(denominator));
                } else {
                    taylorCoefficients.add(Expression.ZERO);
                }
            }
        } else if (type.equals(TypeMatrixFunction.cosh)) {
            denominator = BigInteger.ONE;
            taylorCoefficients.add(Expression.ONE);
            for (int i = 1; i <= n; i++) {
                denominator = denominator.multiply(BigInteger.valueOf(i));
                if (i % 2 == 0) {
                    taylorCoefficients.add(Expression.ONE.div(denominator));
                } else {
                    taylorCoefficients.add(Expression.ZERO);
                }
            }
        } else if (type.equals(TypeMatrixFunction.exp)) {
            denominator = BigInteger.ONE;
            taylorCoefficients.add(Expression.ONE);
            for (int i = 1; i <= n; i++) {
                denominator = denominator.multiply(BigInteger.valueOf(i));
                taylorCoefficients.add(Expression.ONE.div(denominator));
            }
        } else if (type.equals(TypeMatrixFunction.ln)) {
            denominator = BigInteger.ONE;
            taylorCoefficients.add(Expression.ZERO);
            for (int i = 1; i <= n; i++) {
                if (i % 2 == 0) {
                    taylorCoefficients.add(Expression.MINUS_ONE.div(denominator));
                } else {
                    taylorCoefficients.add(Expression.ONE.div(denominator));
                }
                denominator = denominator.add(BigInteger.ONE);
            }
        } else if (type.equals(TypeMatrixFunction.sin)) {
            denominator = BigInteger.ONE;
            taylorCoefficients.add(Expression.ZERO);
            for (int i = 1; i <= n; i++) {
                denominator = denominator.multiply(BigInteger.valueOf(i));
                if (i % 4 == 1) {
                    taylorCoefficients.add(Expression.ONE.div(denominator));
                } else if (i % 4 == 3) {
                    taylorCoefficients.add(Expression.MINUS_ONE.div(denominator));
                } else {
                    taylorCoefficients.add(Expression.ZERO);
                }
            }
        } else if (type.equals(TypeMatrixFunction.sinh)) {
            denominator = BigInteger.ONE;
            taylorCoefficients.add(Expression.ZERO);
            for (int i = 1; i <= n; i++) {
                denominator = denominator.multiply(BigInteger.valueOf(i));
                if (i % 2 == 1) {
                    taylorCoefficients.add(Expression.ONE.div(denominator));
                } else {
                    taylorCoefficients.add(Expression.ZERO);
                }
            }
        }

        return taylorCoefficients;

    }

    /**
     * Vereinfacht f(A^k * B * A^(-k)) = A^k * f(B) * A^(-k).
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyPowerSeriesFunctionOfConjugatedMatrix(MatrixExpression matExpr, TypeMatrixFunction type) throws EvaluationException {

        if (matExpr.isMatrixFunction(type) && ((MatrixFunction) matExpr).getLeft().isProduct()) {

            MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(((MatrixFunction) matExpr).getLeft());
            if (factors.getBound() == 3 && factors.get(0).isPower() && factors.get(2).isPower()
                    && ((MatrixPower) factors.get(0)).getLeft().equivalent(((MatrixPower) factors.get(2)).getLeft())) {

                Expression sumOfExponents = ((MatrixPower) factors.get(0)).getRight().add(((MatrixPower) factors.get(2)).getRight()).simplify();
                if (sumOfExponents.equals(Expression.ZERO)) {
                    return factors.get(0).mult(new MatrixFunction(factors.get(1), type).mult(factors.get(2)));
                }

            }

        }

        return matExpr;

    }

    /**
     * Vereinfacht f(A), wenn A eine (2x2)-Dreiecksmatrix ist.
     *
     * @throws EvaluationException
     */
    public static MatrixExpression simplifyPowerSeriesFunctionOfTwoDimensionalTriangularMatrix(MatrixExpression matExpr, TypeMatrixFunction type) throws EvaluationException {

        if (matExpr.isMatrixFunction(type) && ((MatrixFunction) matExpr).getLeft().isMatrix()
                && ((Matrix) ((MatrixFunction) matExpr).getLeft()).isTriangularMatrix()) {

            Matrix m = (Matrix) ((MatrixFunction) matExpr).getLeft();
            Dimension dim = m.getDimension();
            if (dim.width != 2 || dim.height != 2) {
                return matExpr;
            }

            if (m.isUpperTriangularMatrix() && m.getEntry(0, 0).equivalent(m.getEntry(1, 1))) {
                // m ist von der Form [a,b;0,a]. Dann ist f(A) = [f(a),b*f'(a);0,f(a)]
                Expression a = m.getEntry(0, 0);
                Expression b = m.getEntry(0, 1);
                switch (type) {
                    case cos:
                        return new Matrix(new Expression[][]{{a.cos(), MINUS_ONE.mult(b.mult(a.sin()))}, {ZERO, a.cos()}});
                    case cosh:
                        return new Matrix(new Expression[][]{{a.cosh(), b.mult(a.sinh())}, {ZERO, a.cosh()}});
                    case exp:
                        return new Matrix(new Expression[][]{{a.exp(), b.mult(a.exp())}, {ZERO, a.exp()}});
                    case ln:
                        return new Matrix(new Expression[][]{{a.ln(), b.div(a)}, {ZERO, a.ln()}});
                    case sin:
                        return new Matrix(new Expression[][]{{a.sin(), b.mult(a.cos())}, {ZERO, a.sin()}});
                    case sinh:
                        return new Matrix(new Expression[][]{{a.sinh(), b.mult(a.cosh())}, {ZERO, a.sinh()}});
                }
            }
            if (m.isLowerTriangularMatrix() && m.getEntry(0, 0).equivalent(m.getEntry(1, 1))) {
                // m ist von der Form [a,0;b,a]. Dann ist f(A) = [f(a),0;b*f'(a),f(a)]
                Expression a = m.getEntry(0, 0);
                Expression b = m.getEntry(1, 0);
                switch (type) {
                    case cos:
                        return new Matrix(new Expression[][]{{a.cos(), ZERO}, {MINUS_ONE.mult(b.mult(a.sin())), a.cos()}});
                    case cosh:
                        return new Matrix(new Expression[][]{{a.cosh(), ZERO}, {b.mult(a.sinh()), a.cosh()}});
                    case exp:
                        return new Matrix(new Expression[][]{{a.exp(), ZERO}, {b.mult(a.exp()), a.exp()}});
                    case ln:
                        return new Matrix(new Expression[][]{{a.ln(), ZERO}, {b.div(a), a.ln()}});
                    case sin:
                        return new Matrix(new Expression[][]{{a.sin(), ZERO}, {b.mult(a.cos()), a.sin()}});
                    case sinh:
                        return new Matrix(new Expression[][]{{a.sinh(), ZERO}, {b.mult(a.cosh()), a.sinh()}});
                }
            }

        }

        return matExpr;

    }

}
