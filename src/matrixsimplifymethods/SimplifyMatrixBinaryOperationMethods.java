package matrixsimplifymethods;

import exceptions.EvaluationException;
import expressionbuilder.Expression;
import java.awt.Dimension;
import matrixexpressionbuilder.Matrix;
import matrixexpressionbuilder.MatrixBinaryOperation;
import matrixexpressionbuilder.MatrixExpression;
import translator.Translator;

public abstract class SimplifyMatrixBinaryOperationMethods {

    public static void removeZeroMatrixInSum(MatrixExpressionCollection summands) throws EvaluationException {
        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) == null) {
                continue;
            }
            if (summands.get(i).isZeroMatrix() && !summands.isEmpty()) {
                summands.remove(i);
            }
            if (Thread.interrupted()) {
                throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixBinaryOperation_COMPUTATION_ABORTED"));
            }
        }
    }

    public static MatrixExpression trivialOperationsInDifferenceWithZeroIdMatrices(MatrixExpression matExpr) {

        if (matExpr.isDifference()) {
            if (((MatrixBinaryOperation) matExpr).getRight().isZeroMatrix()) {
                return ((MatrixBinaryOperation) matExpr).getLeft();
            }
            if (((MatrixBinaryOperation) matExpr).getLeft().isZeroMatrix()) {
                return new Matrix(Expression.MINUS_ONE).mult(((MatrixBinaryOperation) matExpr).getRight());
            }
        }

        return matExpr;

    }

    public static MatrixExpression factorizeMultiplesOfId(MatrixExpression matExpr) {

        if (matExpr.isNotProduct()) {
            return matExpr;
        }

        MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(matExpr);
        MatrixExpressionCollection resultFactors = new MatrixExpressionCollection();

        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).isMultipleOfId()) {
                resultFactors.add(new Matrix(((Matrix) factors.get(i)).getEntry(0, 0)));
                factors.remove(i);
            }
        }

        if (resultFactors.isEmpty()) {
            return matExpr;
        }

        for (int i = 0; i < factors.getBound(); i++) {
            resultFactors.add(factors.get(i));
        }
        return SimplifyMatrixUtilities.produceProduct(resultFactors);

    }

    public static void removeIdInProduct(MatrixExpressionCollection factors) throws EvaluationException {
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).isId() && !factors.isEmpty()) {
                factors.remove(i);
            }
            if (Thread.interrupted()) {
                throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixBinaryOperation_COMPUTATION_ABORTED"));
            }
        }
    }

    public static void reduceZeroProductToZero(MatrixExpressionCollection factors) throws EvaluationException {
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).isZeroMatrix()) {
                Dimension dim = SimplifyMatrixUtilities.produceProduct(factors).getDimension();
                factors.clear();
                factors.add(MatrixExpression.getZeroMatrix(dim.height, dim.width));
            }
            if (Thread.interrupted()) {
                throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixBinaryOperation_COMPUTATION_ABORTED"));
            }
        }
    }

    public static void factorizeScalarsInSum(MatrixExpressionCollection summands) {

        MatrixExpressionCollection factorsOfLeftSummand, factorsOfRightSummand, commonScalarFactors;
        MatrixExpression commonFactor, restSummandLeft, restSummandRight;

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }

            factorsOfLeftSummand = SimplifyMatrixUtilities.getFactors(summands.get(i));

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }

                factorsOfRightSummand = SimplifyMatrixUtilities.getFactors(summands.get(j));

                commonScalarFactors = SimplifyMatrixUtilities.intersection(factorsOfLeftSummand, factorsOfRightSummand);

                // Nun müssen unter den gemeinsamen Faktoren diejenigen ausgewählt werden, welche 1x1-Matrizen darstellen.
                for (int k = 0; k < commonScalarFactors.getBound(); k++) {
                    if (!(commonScalarFactors.get(k).convertOneTimesOneMatrixToExpression() instanceof Expression)) {
                        commonScalarFactors.remove(k);
                    }
                }

                // Summanden faktorisieren, wenn gemeinsame Skalarfaktoren vorhanden sind.
                if (!commonScalarFactors.isEmpty()) {
                    commonFactor = SimplifyMatrixUtilities.produceProduct(commonScalarFactors);
                    restSummandLeft = SimplifyMatrixUtilities.produceProduct(SimplifyMatrixUtilities.difference(factorsOfLeftSummand, commonScalarFactors));
                    restSummandRight = SimplifyMatrixUtilities.produceProduct(SimplifyMatrixUtilities.difference(factorsOfRightSummand, commonScalarFactors));
                    summands.put(i, commonFactor.mult(restSummandLeft.add(restSummandRight)));
                    summands.remove(j);
                    break;
                }

            }

        }

    }

    public static void factorizeScalarsInDifference(MatrixExpressionCollection summandsLeft, MatrixExpressionCollection summandsRight) {

        MatrixExpressionCollection factorsOfLeftSummand, factorsOfRightSummand, commonScalarFactors;
        MatrixExpression commonFactor, restSummandLeft, restSummandRight;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }

            factorsOfLeftSummand = SimplifyMatrixUtilities.getFactors(summandsLeft.get(i));

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }

                factorsOfRightSummand = SimplifyMatrixUtilities.getFactors(summandsRight.get(j));

                commonScalarFactors = SimplifyMatrixUtilities.intersection(factorsOfLeftSummand, factorsOfRightSummand);

                // Nun müssen unter den gemeinsamen Faktoren diejenigen ausgewählt werden, welche 1x1-Matrizen darstellen.
                for (int k = 0; k < commonScalarFactors.getBound(); k++) {
                    if (!(commonScalarFactors.get(k).convertOneTimesOneMatrixToExpression() instanceof Expression)) {
                        commonScalarFactors.remove(k);
                    }
                }

                // Summanden faktorisieren, wenn gemeinsame Skalarfaktoren vorhanden sind.
                if (!commonScalarFactors.isEmpty()) {
                    commonFactor = SimplifyMatrixUtilities.produceProduct(commonScalarFactors);
                    restSummandLeft = SimplifyMatrixUtilities.produceProduct(SimplifyMatrixUtilities.difference(factorsOfLeftSummand, commonScalarFactors));
                    restSummandRight = SimplifyMatrixUtilities.produceProduct(SimplifyMatrixUtilities.difference(factorsOfRightSummand, commonScalarFactors));
                    summandsLeft.put(i, commonFactor.mult(restSummandLeft.sub(restSummandRight)));
                    summandsRight.remove(j);
                    break;
                }

            }

        }

    }

    public static void factorizeInSum(MatrixExpressionCollection summands) {

        MatrixExpressionCollection factorsOfLeftSummand, factorsOfRightSummand;
        MatrixExpression commonFactor, factorizedSummand;

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }

            factorsOfLeftSummand = SimplifyMatrixUtilities.getFactors(summands.get(i));

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }

                factorsOfRightSummand = SimplifyMatrixUtilities.getFactors(summands.get(j));

                factorizedSummand = null;
                // Jetzt wird der erste nichtskalare Faktor im linken und im rechten Summanden gesucht.
                for (int p = 0; p < factorsOfLeftSummand.getBound(); p++) {
                    if (factorsOfLeftSummand.get(p).convertOneTimesOneMatrixToExpression() instanceof MatrixExpression) {
                        for (int q = 0; q < factorsOfRightSummand.getBound(); q++) {
                            if (factorsOfRightSummand.get(q).convertOneTimesOneMatrixToExpression() instanceof MatrixExpression) {
                                if (factorsOfLeftSummand.get(p).equivalent(factorsOfRightSummand.get(q))) {
                                    commonFactor = factorsOfLeftSummand.get(p);
                                    factorsOfLeftSummand.remove(p);
                                    factorsOfRightSummand.remove(q);
                                    factorizedSummand = commonFactor.mult(SimplifyMatrixUtilities.produceProduct(factorsOfLeftSummand).add(
                                            SimplifyMatrixUtilities.produceProduct(factorsOfRightSummand)));
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
                // Faktorisierten Summanden ablegen, falls solch einer existiert.
                if (factorizedSummand != null){
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    break;
                }

            }

        }

    }
    
    public static void factorizeInDifference(MatrixExpressionCollection summandsLeft, MatrixExpressionCollection summandsRight) {

        MatrixExpressionCollection factorsOfLeftSummand, factorsOfRightSummand;
        MatrixExpression commonFactor, factorizedSummand;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }

            factorsOfLeftSummand = SimplifyMatrixUtilities.getFactors(summandsLeft.get(i));

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }

                factorsOfRightSummand = SimplifyMatrixUtilities.getFactors(summandsRight.get(j));

                factorizedSummand = null;
                // Jetzt wird der erste nichtskalare Faktor im linken und im rechten Summanden gesucht.
                for (int p = 0; p < factorsOfLeftSummand.getBound(); p++) {
                    if (factorsOfLeftSummand.get(p).convertOneTimesOneMatrixToExpression() instanceof MatrixExpression) {
                        for (int q = 0; q < factorsOfRightSummand.getBound(); q++) {
                            if (factorsOfRightSummand.get(q).convertOneTimesOneMatrixToExpression() instanceof MatrixExpression) {
                                if (factorsOfLeftSummand.get(p).equivalent(factorsOfRightSummand.get(q))) {
                                    commonFactor = factorsOfLeftSummand.get(p);
                                    factorsOfLeftSummand.remove(p);
                                    factorsOfRightSummand.remove(q);
                                    factorizedSummand = commonFactor.mult(SimplifyMatrixUtilities.produceProduct(factorsOfLeftSummand).sub(
                                            SimplifyMatrixUtilities.produceProduct(factorsOfRightSummand)));
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
                // Faktorisierten Summanden ablegen, falls solch einer existiert.
                if (factorizedSummand != null){
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    break;
                }

            }

        }

    }

}
