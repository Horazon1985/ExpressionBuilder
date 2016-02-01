package abstractexpressions.matrixexpression.classes;

import enums.TypeSimplify;
import exceptions.EvaluationException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.TypeBinary;
import java.awt.Dimension;
import java.util.HashSet;
import abstractexpressions.matrixexpression.utilities.MatrixExpressionCollection;
import abstractexpressions.matrixexpression.utilities.SimplifyMatrixBinaryOperationMethods;
import abstractexpressions.matrixexpression.utilities.SimplifyMatrixFunctionalRelations;
import abstractexpressions.matrixexpression.utilities.SimplifyMatrixUtilities;
import translator.Translator;

public class MatrixBinaryOperation extends MatrixExpression {

    private final MatrixExpression left, right;
    private final TypeMatrixBinary type;

    public MatrixBinaryOperation(MatrixExpression left, MatrixExpression right, TypeMatrixBinary type) {
        this.left = left;
        this.right = right;
        this.type = type;
    }

    public MatrixExpression getLeft() {
        return this.left;
    }

    public MatrixExpression getRight() {
        return this.right;
    }

    public TypeMatrixBinary getType() {
        return this.type;
    }

    @Override
    public Dimension getDimension() throws EvaluationException {

        Dimension dimLeft = this.left.getDimension();
        Dimension dimRight = this.right.getDimension();

        if (!this.isProduct()) {
            if (dimLeft.height != dimRight.height || dimLeft.width != dimRight.width) {
                throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixBinaryOperation_SUM_OR_DIFFERENCE_OF_MATRICES_NOT_DEFINED"));
            }
            return dimLeft;
        } else {
            /*
             Ausnahme: eine (1x1)-Matrix wird bei der Multiplikation (egal, ob von
             links oder von rechts) wie ein Skalar interpretiert und eben auch so
             behandelt.
             */
            if (dimLeft.height == 1 && dimLeft.width == 1) {
                return dimRight;
            }
            if (dimRight.height == 1 && dimRight.width == 1) {
                return dimLeft;
            }
            if (dimLeft.width != dimRight.height) {
                throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixBinaryOperation_PRODUCT_OF_MATRICES_NOT_DEFINED"));
            }
            return new Dimension(dimRight.width, dimLeft.height);
        }

    }

    @Override
    public MatrixExpression simplifyComputeMatrixOperations() throws EvaluationException {

        /*
         Dient unter anderem dazu, festzustellen, ob das Ergebnis
         wohldefiniert ist. Falls nicht, so wird bei der Berechnung von dim
         eine EvaluationException geworfen.
         */
        Dimension dim = this.getDimension();

        MatrixExpression leftComputed = this.left.simplifyComputeMatrixOperations();
        MatrixExpression rightComputed = this.right.simplifyComputeMatrixOperations();

        if (leftComputed.isNotMatrix() || rightComputed.isNotMatrix()) {
            return new MatrixBinaryOperation(leftComputed, rightComputed, this.type);
        }

        if (this.isSum()) {

            Expression[][] resultEntry = new Expression[((Matrix) leftComputed).getRowNumber()][((Matrix) leftComputed).getColumnNumber()];
            for (int i = 0; i < dim.height; i++) {
                for (int j = 0; j < dim.width; j++) {
                    resultEntry[i][j] = ((Matrix) leftComputed).getEntry(i, j).add(((Matrix) rightComputed).getEntry(i, j));
                }
            }
            return new Matrix(resultEntry);

        } else if (this.isDifference()) {

            Expression[][] resultEntry = new Expression[((Matrix) leftComputed).getRowNumber()][((Matrix) leftComputed).getColumnNumber()];
            for (int i = 0; i < dim.height; i++) {
                for (int j = 0; j < dim.width; j++) {
                    resultEntry[i][j] = ((Matrix) leftComputed).getEntry(i, j).sub(((Matrix) rightComputed).getEntry(i, j));
                }
            }
            return new Matrix(resultEntry);

        } else {

            Dimension dimLeft = leftComputed.getDimension();
            Dimension dimRight = rightComputed.getDimension();
            /*
             Ausnahme: eine (1x1)-Matrix wird bei der Multiplikation (egal, ob
             von links oder von rechts) wie ein Skalar interpretiert und eben
             auch so behandelt.
             */
            if (dimLeft.height == 1 && dimLeft.width == 1 && rightComputed.isMatrix()) {
                Expression[][] resultEntry = new Expression[dimRight.height][dimRight.width];
                for (int i = 0; i < dimRight.height; i++) {
                    for (int j = 0; j < dimRight.width; j++) {
                        resultEntry[i][j] = ((Matrix) leftComputed).getEntry(0, 0).mult(((Matrix) rightComputed).getEntry(i, j));
                    }
                }
                return new Matrix(resultEntry);
            }

            if (dimRight.height == 1 && dimRight.width == 1 && rightComputed.isMatrix()) {
                Expression[][] resultEntry = new Expression[dimLeft.height][dimLeft.width];
                for (int i = 0; i < dimLeft.height; i++) {
                    for (int j = 0; j < dimLeft.width; j++) {
                        resultEntry[i][j] = ((Matrix) rightComputed).getEntry(0, 0).mult(((Matrix) leftComputed).getEntry(i, j));
                    }
                }
                return new Matrix(resultEntry);
            }

            Expression[][] resultEntry = new Expression[((Matrix) leftComputed).getRowNumber()][((Matrix) rightComputed).getColumnNumber()];
            for (int i = 0; i < dim.height; i++) {
                for (int j = 0; j < dim.width; j++) {

                    resultEntry[i][j] = ZERO;
                    for (int k = 0; k < ((Matrix) leftComputed).getColumnNumber(); k++) {
                        if (resultEntry[i][j].equals(ZERO)) {
                            resultEntry[i][j] = ((Matrix) leftComputed).getEntry(i, k).mult(((Matrix) rightComputed).getEntry(k, j));
                        } else {
                            resultEntry[i][j] = resultEntry[i][j].add(((Matrix) leftComputed).getEntry(i, k).mult(((Matrix) rightComputed).getEntry(k, j)));
                        }
                    }

                }
            }
            return new Matrix(resultEntry);

        }

    }

    @Override
    public boolean equals(MatrixExpression matExpr) {

        return matExpr instanceof MatrixBinaryOperation
                && this.type.equals(((MatrixBinaryOperation) matExpr).getType())
                && this.left.equals(((MatrixBinaryOperation) matExpr).getLeft())
                && this.right.equals(((MatrixBinaryOperation) matExpr).getRight());

    }

    @Override
    public boolean equivalent(MatrixExpression matExpr) {

        if (matExpr instanceof MatrixBinaryOperation) {
            if (this.getType().equals(((MatrixBinaryOperation) matExpr).getType())) {
                if (this.isSum()) {

                    MatrixExpressionCollection summandsOfThis = SimplifyMatrixUtilities.getSummands(this);
                    MatrixExpressionCollection summandsOfExpr = SimplifyMatrixUtilities.getSummands(matExpr);
                    return summandsOfThis.getBound() == summandsOfExpr.getBound()
                            && SimplifyMatrixUtilities.difference(summandsOfThis, summandsOfExpr).isEmpty();

                }
                if (this.isProduct()){
                    MatrixExpressionCollection factorsOfThis = SimplifyMatrixUtilities.getFactors(this);
                    MatrixExpressionCollection factorsOfExpr = SimplifyMatrixUtilities.getFactors(matExpr);
                    if (factorsOfThis.getBound() != factorsOfExpr.getBound()){
                        return false;
                    }
                    for (int i = 0; i < factorsOfThis.getBound(); i++){
                        if (!factorsOfThis.get(i).equivalent(factorsOfExpr.get(i))){
                            return false;
                        }
                    }
                    return true;
                }
                return this.getLeft().equivalent(((MatrixBinaryOperation) matExpr).getLeft())
                        && this.getRight().equivalent(((MatrixBinaryOperation) matExpr).getRight());
            }
            return false;
        }
        return false;

    }

    @Override
    public MatrixExpression orderSumsAndProducts() throws EvaluationException {

        if (this.isNotSum() && this.isNotProduct()) {
            return new MatrixBinaryOperation(this.left.orderSumsAndProducts(), this.right.orderSumsAndProducts(), this.type);
        }

        // Fall type = +.
        if (this.isSum()) {

            Dimension dim;
            try {
                dim = this.getDimension();
            } catch (EvaluationException e) {
                return this;
            }
            MatrixExpression result = MatrixExpression.getZeroMatrix(dim.height, dim.width);
            MatrixExpressionCollection summands = SimplifyMatrixUtilities.getSummands(this);

            for (int i = summands.getBound() - 1; i >= 0; i--) {
                if (summands.get(i) == null) {
                    continue;
                }
                summands.put(i, summands.get(i).orderSumsAndProducts());
                if (result.isZeroMatrix()) {
                    result = summands.get(i).orderSumsAndProducts();
                } else {
                    result = summands.get(i).orderSumsAndProducts().add(result);
                }
            }

            return result;

        } else {

            // Fall type = *.
            MatrixExpression result = MatrixExpression.getId(1);
            MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(this);

            // 1x1-Matrizen als Faktoren nach vorne ziehen 
            factors = pullScalarFactorsToBeginning(factors);

            for (int i = factors.getBound() - 1; i >= 0; i--) {
                if (factors.get(i) == null) {
                    continue;
                }
                factors.put(i, factors.get(i).orderSumsAndProducts());
                if (result.equals(MatrixExpression.getId(1))) {
                    result = factors.get(i).orderSumsAndProducts();
                } else {
                    result = factors.get(i).orderSumsAndProducts().mult(result);
                }
            }

            return result;

        }

    }

    private MatrixExpressionCollection pullScalarFactorsToBeginning(MatrixExpressionCollection factors) {

        MatrixExpressionCollection factorsOrdered = new MatrixExpressionCollection();
        for (int i = 0; i < factors.getBound(); i++) {
            if (factors.get(i) == null) {
                continue;
            }
            if (factors.get(i).convertOneTimesOneMatrixToExpression() instanceof Expression) {
                factorsOrdered.add(factors.get(i));
                factors.remove(i);
            }
        }
        if (factorsOrdered.isEmpty()) {
            return factors;
        }
        factorsOrdered.add(factors);
        return factorsOrdered;

    }

    @Override
    public MatrixExpression orderDifferences() throws EvaluationException {

        MatrixExpression result;

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
        }

        MatrixExpressionCollection termsLeft = new MatrixExpressionCollection();
        MatrixExpressionCollection termsRight = new MatrixExpressionCollection();

        if (this.isSum() || this.isDifference()) {

            SimplifyMatrixUtilities.orderDifference(this, termsLeft, termsRight);
            for (int i = 0; i < termsLeft.getBound(); i++) {
                termsLeft.put(i, termsLeft.get(i).orderDifferences());
            }
            for (int i = 0; i < termsRight.getBound(); i++) {
                termsRight.put(i, termsRight.get(i).orderDifferences());
            }
            result = SimplifyMatrixUtilities.produceDifference(termsLeft, termsRight);

        } else {
            // Hier ist type == TypeMatrixBinary.TIMES.
            result = this.left.orderDifferences().mult(this.right.orderDifferences());
        }

        return result;

    }

    @Override
    public boolean isConstant() {
        return this.left.isConstant() && this.right.isConstant();
    }

    @Override
    public boolean contains(String var) {
        return this.left.contains(var) || this.right.contains(var);
    }

    @Override
    public void addContainedVars(HashSet<String> vars) {
        this.left.addContainedVars(vars);
        this.right.addContainedVars(vars);
    }

    @Override
    public void addContainedIndeterminates(HashSet<String> vars) {
        this.left.addContainedIndeterminates(vars);
        this.right.addContainedIndeterminates(vars);
    }

    @Override
    public MatrixExpression turnToApproximate() {
        return new MatrixBinaryOperation(this.left.turnToApproximate(), this.right.turnToApproximate(), this.type);
    }

    @Override
    public MatrixExpression turnToPrecise() {
        return new MatrixBinaryOperation(this.left.turnToPrecise(), this.right.turnToPrecise(), this.type);
    }

    @Override
    public boolean containsApproximates() {
        return this.left.containsApproximates() || this.right.containsApproximates();
    }

    @Override
    public MatrixExpression copy() {
        return new MatrixBinaryOperation(this.left.copy(), this.right.copy(), this.type);
    }

    @Override
    public MatrixExpression evaluate() throws EvaluationException {
        return new MatrixBinaryOperation(this.left.evaluate(), this.right.evaluate(), this.type);
    }

    @Override
    public MatrixExpression diff(String var) throws EvaluationException {

        if (this.isSum() || this.isDifference()) {
            return new MatrixBinaryOperation(this.left.diff(var), this.right.diff(var), this.type);
        }
        // Hier ist this.type == TypeMatrixBinary.TIMES
        return this.left.diff(var).mult(this.right).add(this.left.mult(this.right.diff(var)));

    }

    @Override
    public MatrixExpression replaceVariable(String var, Expression expr) {
        return new MatrixBinaryOperation(this.left.replaceVariable(var, expr), this.right.replaceVariable(var, expr), this.type);
    }

    @Override
    public String writeMatrixExpression() {

        String leftAsText, rightAsText;

        if (this.type.equals(TypeMatrixBinary.PLUS)) {
            return this.left.writeMatrixExpression() + "+" + this.right.writeMatrixExpression();
        } else if (this.type.equals(TypeMatrixBinary.MINUS)) {

            leftAsText = this.left.writeMatrixExpression();

            if (this.right.isSum() || this.right.isDifference()) {
                return leftAsText + "-(" + this.right.writeMatrixExpression() + ")";
            }
            return leftAsText + "-" + this.right.writeMatrixExpression();

        }

        // Hier handelt es sich um eine Matrizenmultiplikation.
        Expression matrixConverted;
        if (this.left.convertOneTimesOneMatrixToExpression() instanceof Expression) {
            /*
             Spezialfall: this.left ist eine (1x1)-Matrix. Dann wird sie als
             Expression ausgegeben. Zusätzlich wird [-1] als - ausgegeben,
             wenn beginning == true gilt.
             */
            matrixConverted = (Expression) this.left.convertOneTimesOneMatrixToExpression();
            if (matrixConverted.equals(Expression.MINUS_ONE)) {
                leftAsText = "-";
            } else if (matrixConverted.doesExpressionStartWithAMinusSign()
                    || (matrixConverted instanceof BinaryOperation
                    && (((BinaryOperation) matrixConverted).getType().equals(TypeBinary.PLUS)
                    || ((BinaryOperation) matrixConverted).getType().equals(TypeBinary.MINUS)))) {
                leftAsText = "(" + matrixConverted.writeExpression() + ")";
            } else {
                leftAsText = matrixConverted.writeExpression();
            }
        } else if (this.left.isSum() || this.left.isDifference()) {
            leftAsText = "(" + this.left.writeMatrixExpression() + ")";
        } else {
            leftAsText = this.left.writeMatrixExpression();
        }

        if (this.right.convertOneTimesOneMatrixToExpression() instanceof Expression) {
            // Spezialfall: this.right ist eine (1x1)-Matrix. Dann wird sie als Expression ausgegeben.
            matrixConverted = (Expression) this.right.convertOneTimesOneMatrixToExpression();
            if (matrixConverted.doesExpressionStartWithAMinusSign()
                    || (matrixConverted instanceof BinaryOperation
                    && (((BinaryOperation) matrixConverted).getType().equals(TypeBinary.PLUS)
                    || ((BinaryOperation) matrixConverted).getType().equals(TypeBinary.MINUS)))) {
                rightAsText = "(" + matrixConverted.writeExpression() + ")";
            } else {
                rightAsText = matrixConverted.writeExpression();
            }
        } else if (this.right.isSum() || this.right.isDifference()) {
            rightAsText = "(" + this.right.writeMatrixExpression() + ")";
        } else {
            rightAsText = this.right.writeMatrixExpression();
        }

        if (leftAsText.equals("-")) {
            return leftAsText + rightAsText;
        }
        return leftAsText + "*" + rightAsText;

    }

    @Override
    public MatrixExpression simplifyTrivial() throws EvaluationException {

        if (this.isSum()) {

            MatrixExpressionCollection summands = SimplifyMatrixUtilities.getSummands(this);
            // In jedem Summanden einzeln vereinfachen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyTrivial());
            }

            // Nullmatrizen in Summen beseitigen.
            SimplifyMatrixBinaryOperationMethods.removeZeroMatrixInSum(summands);

            // Sammelt Matrizen im ersten Summanden. Beispiel: [2]+exp([x])+[3]+[sin(1)] wird zu [5+sin(1)]+exp([x])
            SimplifyMatrixBinaryOperationMethods.collectMatricesInSum(summands);

            return SimplifyMatrixUtilities.produceSum(summands);

        } else if (this.isDifference()) {

            MatrixExpression matExpr = new MatrixBinaryOperation(this.left.simplifyMatrixFunctionalRelations(), this.right.simplifyMatrixFunctionalRelations(), this.type);

            MatrixExpression matExprSimplified = SimplifyMatrixBinaryOperationMethods.trivialOperationsInDifferenceWithZeroIdMatrices(matExpr);
            if (!matExprSimplified.equals(matExpr)) {
                return matExprSimplified;
            }
            
            MatrixExpressionCollection summandsLeft = SimplifyMatrixUtilities.getSummandsLeftInMatrixExpression(matExpr);
            MatrixExpressionCollection summandsRight = SimplifyMatrixUtilities.getSummandsRightInMatrixExpression(matExpr);

            // Sammelt Matrizen im ersten Summanden des Minuenden. Beispiel: ([7]+exp([x]))-([3]+[sin(1)]) wird zu [4-sin(1)]+exp([x])
            SimplifyMatrixBinaryOperationMethods.collectMatricesInDifference(summandsLeft, summandsRight);

            return SimplifyMatrixUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isProduct()) {

            MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(this);
            // In jedem Faktor einzeln vereinfachen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyTrivial());
            }

            MatrixExpression matExpr = SimplifyMatrixUtilities.produceProduct(factors);
            // Falls Vielfache von Id in Produkten auftauchen, dann als 1x1-Faktor nach vorne ziehen.
            matExpr = SimplifyMatrixBinaryOperationMethods.factorizeMultiplesOfId(matExpr);

            factors = SimplifyMatrixUtilities.getFactors(matExpr);

            // Falls Nullmatrizen in Produkten auftauchen, dann nur Nullmatrix zurückgeben.
            SimplifyMatrixBinaryOperationMethods.reduceZeroProductToZero(factors);
            // Identitätsmatrizen in Produkten beseitigen.
            SimplifyMatrixBinaryOperationMethods.removeIdInProduct(factors);
            /* 
             Sammelt aufeinanderfolgende Matrizen in einem Produkt. 
             Beispiel: [2]*exp([x])*[3]*[sin(1)] wird [2]*exp([x])*[3*sin(1)]
             */
            SimplifyMatrixBinaryOperationMethods.collectMatricesInProduct(factors);

            return SimplifyMatrixUtilities.produceProduct(factors);

        }

        return this;

    }

    @Override
    public MatrixExpression simplifyByInsertingDefinedVars() throws EvaluationException {
        return new MatrixBinaryOperation(this.left.simplifyByInsertingDefinedVars(), this.right.simplifyByInsertingDefinedVars(), this.type);
    }

    @Override
    public MatrixExpression simplifyMatrixEntries() throws EvaluationException {
        return new MatrixBinaryOperation(this.left.simplifyMatrixEntries(), this.right.simplifyMatrixEntries(), this.type);
    }

    @Override
    public MatrixExpression simplifyMatrixEntries(HashSet<TypeSimplify> simplify_types) throws EvaluationException {
        return new MatrixBinaryOperation(this.left.simplifyMatrixEntries(simplify_types), this.right.simplifyMatrixEntries(simplify_types), this.type);
    }

    @Override
    public MatrixExpression simplifyCollectProducts() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln Faktoren sammeln.
            MatrixExpressionCollection summands = SimplifyMatrixUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyCollectProducts());
            }
            return SimplifyMatrixUtilities.produceSum(summands);
        } else if (this.isDifference()) {
            // Im linken und rechten Teil einzeln Faktoren sammeln.
            return new MatrixBinaryOperation(this.left.simplifyCollectProducts(), this.right.simplifyCollectProducts(), this.type);
        }

        MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(this);
        // Zunächst in jedem Faktor einzeln Faktoren sammeln.
        for (int i = 0; i < factors.getBound(); i++) {
            factors.put(i, factors.get(i).simplifyCollectProducts());
        }

        SimplifyMatrixUtilities.collectFactorsInMatrixProduct(factors);
        return SimplifyMatrixUtilities.produceProduct(factors);

    }

    @Override
    public MatrixExpression simplifyFactorizeScalars() throws EvaluationException {

        if (this.isSum()) {

            // In jedem Summanden einzeln Faktoren sammeln.
            MatrixExpressionCollection summands = SimplifyMatrixUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyFactorizeScalars());
            }
            // Skalare Faktoren faktorisieren.
            SimplifyMatrixBinaryOperationMethods.factorizeScalarsInSum(summands);
            return SimplifyMatrixUtilities.produceSum(summands);

        } else if (this.isDifference()) {

            MatrixExpression matExpr = this.left.simplifyFactorizeScalars().sub(this.right.simplifyFactorizeScalars());
            MatrixExpressionCollection summandsLeft = SimplifyMatrixUtilities.getSummandsLeftInMatrixExpression(matExpr);
            MatrixExpressionCollection summandsRight = SimplifyMatrixUtilities.getSummandsRightInMatrixExpression(matExpr);
            // Skalare Faktoren faktorisieren.
            SimplifyMatrixBinaryOperationMethods.factorizeScalarsInDifference(summandsLeft, summandsRight);
            return SimplifyMatrixUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isProduct()) {

            // In jedem Summanden einzeln Faktoren sammeln.
            MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyFactorizeScalars());
            }
            return SimplifyMatrixUtilities.produceProduct(factors);

        }

        return new MatrixBinaryOperation(this.left.simplifyFactorizeScalars(), this.right.simplifyFactorizeScalars(), this.type);

    }

    @Override
    public MatrixExpression simplifyFactorize() throws EvaluationException {

        if (this.isSum()) {

            MatrixExpressionCollection summands = SimplifyMatrixUtilities.getSummands(this);
            // In jedem Summanden einzeln faktorisieren
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyFactorize());
            }
            // Faktorisierung in Summen.
            SimplifyMatrixBinaryOperationMethods.factorizeInSum(summands);
            return SimplifyMatrixUtilities.produceSum(summands);

        } else if (this.isDifference()) {

            MatrixExpression matExpr = this.left.simplifyFactorize().sub(this.right.simplifyFactorize());
            MatrixExpressionCollection summandsLeft = SimplifyMatrixUtilities.getSummandsLeftInMatrixExpression(matExpr);
            MatrixExpressionCollection summandsRight = SimplifyMatrixUtilities.getSummandsRightInMatrixExpression(matExpr);
            // Faktorisierung in Differenzen.
            SimplifyMatrixBinaryOperationMethods.factorizeInDifference(summandsLeft, summandsRight);
            return SimplifyMatrixUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isProduct()) {

            // In jedem Faktor einzeln faktorisieren.
            MatrixExpressionCollection factors = SimplifyMatrixUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyFactorize());
            }
            return SimplifyMatrixUtilities.produceProduct(factors);

        }

        return new MatrixBinaryOperation(this.left.simplifyFactorize(), this.right.simplifyFactorize(), this.type);

    }

    @Override
    public MatrixExpression simplifyMatrixFunctionalRelations() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixBinaryOperation_COMPUTATION_ABORTED"));
        }

        if (this.isSum()) {

            MatrixExpressionCollection summands = SimplifyMatrixUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionalgleichungen anwenden.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyMatrixFunctionalRelations());
            }

            // cos(A)^2 + sin(A)^2 = E
            SimplifyMatrixFunctionalRelations.reduceSumOfSquaresOfSineAndCosine(summands);
            // cosh(A) + sinh(A) = exp(A)
            SimplifyMatrixFunctionalRelations.reduceSinhPlusCoshToExp(summands);

            return SimplifyMatrixUtilities.produceSum(summands);

        }
        if (this.isDifference()) {

            // Im Minuenden und Subtrahenden einzeln Funktionalgleichungen anwenden.
            MatrixExpression simplifiedDifference = this.left.simplifyMatrixFunctionalRelations().sub(this.right.simplifyMatrixFunctionalRelations());
            if (!(simplifiedDifference instanceof MatrixBinaryOperation)) {
                return simplifiedDifference;
            }

            MatrixExpressionCollection summandsLeft = SimplifyMatrixUtilities.getSummandsLeftInMatrixExpression(simplifiedDifference);
            MatrixExpressionCollection summandsRight = SimplifyMatrixUtilities.getSummandsRightInMatrixExpression(simplifiedDifference);

            // cosh(A) - sinh(A) = exp(-A) bzw. sinh(A) - cosh(A) = -exp(-A)
            SimplifyMatrixFunctionalRelations.reduceCoshMinusSinhToExp(summandsLeft, summandsRight);
            // cosh(A)^2 - sinh(A)^2 = E bzw. sinh(A)^2 - cosh(A)^2 = -E
            SimplifyMatrixFunctionalRelations.reduceDifferenceOfSquaresOfHypSineAndHypCosine(summandsLeft, summandsRight);

            return SimplifyMatrixUtilities.produceDifference(summandsLeft, summandsRight);

        }

        return new MatrixBinaryOperation(this.left.simplifyMatrixFunctionalRelations(), this.right.simplifyMatrixFunctionalRelations(), this.type);
    }

}
