package abstractexpressions.matrixexpression.classes;

import computationbounds.ComputationBounds;
import enums.TypeSimplify;
import exceptions.EvaluationException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.matrixexpression.computation.EigenvaluesEigenvectorsUtils;
import abstractexpressions.matrixexpression.basic.SimplifyMatrixBinaryOperationUtils;
import java.awt.Dimension;
import java.math.BigDecimal;
import abstractexpressions.matrixexpression.basic.SimplifyMatrixFunctionalRelationsUtils;
import java.util.Set;
import lang.translator.Translator;

public class MatrixPower extends MatrixExpression {

    private static final String MEB_MatrixPower_POWER_OF_MATRIX_NOT_DEFINED = "MEB_MatrixPower_POWER_OF_MATRIX_NOT_DEFINED";
    
    private final MatrixExpression left;
    private final Expression right;

    public MatrixPower(MatrixExpression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public MatrixExpression getLeft() {
        return this.left;
    }

    public Expression getRight() {
        return this.right;
    }

    @Override
    public Dimension getDimension() throws EvaluationException {
        Dimension dim = this.left.getDimension();
        if (dim.height != dim.width) {
            throw new EvaluationException(Translator.translateOutputMessage(MEB_MatrixPower_POWER_OF_MATRIX_NOT_DEFINED));
        }
        return dim;
    }

    /**
     * Gibt die Binärdarstellung von exponent zurück, falls exponent > 0 ist.
     * Wird für eine schnelle Implementierung für das Potenzieren von Matrizen
     * verwendet (in computeMatrixOperations()).
     */
    private boolean[] getBinaryRepresentation(int n) {

        if (n < 0) {
            return new boolean[0];
        }
        if (n == 0) {
            boolean[] result = new boolean[1];
            result[0] = false;
            return result;
        }

        int powerOfTwo = 1;
        int exponent = 0;
        while (n / powerOfTwo > 1) {
            powerOfTwo = 2 * powerOfTwo;
            exponent++;
        }
        boolean[] result = new boolean[exponent + 1];
        result[exponent] = true;
        n = n - powerOfTwo;

        while (n != 0) {
            powerOfTwo = 1;
            exponent = 0;
            while (n / powerOfTwo > 1) {
                powerOfTwo = 2 * powerOfTwo;
                exponent++;
            }
            result[exponent] = true;
            n = n - powerOfTwo;
        }

        return result;

    }

    @Override
    public MatrixExpression simplifyComputeMatrixOperations() throws EvaluationException {

        Dimension dim = this.getDimension();

        // simplify() wird deshalb benutzt, damit alle Ausdrücke in der Basis und im Exponenten möglich weit vereinfacht / verkürzt werden.
        Expression exponentSimplified = this.right.simplify();
        MatrixExpression leftComputed = this.left.simplifyComputeMatrixOperations().simplify();
        MatrixPower matExprSimplified = new MatrixPower(leftComputed, exponentSimplified);

        if (leftComputed.isNotMatrix()) {
            return new MatrixPower(this.left.simplifyComputeMatrixOperations(), exponentSimplified);
        }

        // Bei Diagonalmatrizen sind beliebige Potenzen berechenbar.
        if (((Matrix) leftComputed).isDiagonalMatrix()) {
            Expression[][] resultEntry = new Expression[dim.height][dim.width];
            for (int i = 0; i < dim.height; i++) {
                for (int j = 0; j < dim.width; j++) {
                    if (i != j) {
                        resultEntry[i][j] = ZERO;
                    } else {
                        resultEntry[i][j] = ((Matrix) leftComputed).getEntry(i, j).pow(exponentSimplified);
                    }
                }
            }
            return new Matrix(resultEntry);
        }

        // Potenzen rationaler Matrizen.
        if (exponentSimplified.isIntegerConstant() && ((Matrix) leftComputed).isRationalMatrix()
                && ((Constant) exponentSimplified).getValue().abs().compareTo(
                        BigDecimal.valueOf(ComputationBounds.BOUND_MATRIX_MAX_POWER_OF_RATIONAL_MATRIX)) <= 0) {

            int exponent = ((Constant) exponentSimplified).getValue().intValue();
            MatrixExpression result = MatrixExpression.getId(dim.width);

            if (exponent == 0) {
                return result;
            } else if (exponent < 0) {

                MatrixExpression inverseMatrix = invertMatrix((Matrix) leftComputed).simplify();
                if (inverseMatrix.isMatrix()) {
                    return inverseMatrix.pow(Expression.MINUS_ONE.mult(exponentSimplified));
                }
                return leftComputed.pow(exponentSimplified);

            } else {

                boolean[] binaryRepresentationOfExponent = getBinaryRepresentation(exponent);
                MatrixExpression[] squaresOfBaseMatrix = new MatrixExpression[binaryRepresentationOfExponent.length];
                // Noch ist result = Id(exponent).
                squaresOfBaseMatrix[0] = leftComputed;
                
                for (int i = 1; i < binaryRepresentationOfExponent.length; i++) {
                    squaresOfBaseMatrix[i] = squaresOfBaseMatrix[i - 1].mult(squaresOfBaseMatrix[i - 1]).simplifyComputeMatrixOperations().simplifyMatrixEntries();
                }
                
                // Nun kommt die eigentliche Berechnung der Potenz.
                for (int i = 0; i < binaryRepresentationOfExponent.length; i++) {
                    if (binaryRepresentationOfExponent[i]) {
                        result = result.mult(squaresOfBaseMatrix[i]).simplifyComputeMatrixOperations().simplifyMatrixEntries();
                    }
                }

                return result;

            }

        }

        // Potenzen beliebiger Matrizen.
        if (exponentSimplified.isIntegerConstant() && ((Constant) exponentSimplified).getValue().abs().compareTo(
                BigDecimal.valueOf(ComputationBounds.BOUND_MATRIX_MAX_POWER_OF_GENERAL_MATRIX)) <= 0) {

            int exponent = ((Constant) exponentSimplified).getValue().intValue();

            if (exponent < 0) {
                MatrixExpression inverseMatrix = invertMatrix((Matrix) leftComputed).simplify();
                if (inverseMatrix.isMatrix()) {
                    return inverseMatrix.pow(Expression.MINUS_ONE.mult(exponentSimplified));
                }
                return leftComputed.pow(exponentSimplified);
            }

            MatrixExpression result = MatrixExpression.getId(dim.width);
            for (int i = 0; i < exponent; i++) {
                result = result.mult(leftComputed).simplifyComputeMatrixOperations();
            }
            return result;

        }
        
        // Potenzen disgonalisierbarer Matrizen
        if (this.left.isMatrix() && EigenvaluesEigenvectorsUtils.isMatrixDiagonalizable((Matrix) this.left)) {
            return SimplifyMatrixBinaryOperationUtils.simplifyPowerOfDiagonalizableMatrix((Matrix) this.left, this.right);
        }
        
        return matExprSimplified;

    }

    /**
     * Hilfsmethode. Berechnet (mit Gewalt) das Inverse der Matrix matrix. Die
     * Berechnung erfolgt wie folgt: Bildung der adjugierten Matrix und Division
     * durch die Determinante von matrix.
     *
     * @throws EvaluationException
     */
    private static MatrixExpression invertMatrix(Matrix matrix) throws EvaluationException {

        /*
         Zunächst soll geprüft werden, ob die Determinante von matrix
         berechenbar ist. Wenn nicht -> EvaluationException ausgeben, dass die
         Inverse von matrix nicht existiert.
         */
        MatrixExpression det;
        try {
            det = new MatrixFunction(matrix, TypeMatrixFunction.det).simplify();
            if (det.isZeroMatrix()) {
                throw new EvaluationException(Translator.translateOutputMessage(MEB_MatrixPower_POWER_OF_MATRIX_NOT_DEFINED));
            }
        } catch (EvaluationException e) {
            throw new EvaluationException(Translator.translateOutputMessage(MEB_MatrixPower_POWER_OF_MATRIX_NOT_DEFINED));
        }

        // Nun wird die adjungierte Matrix berechnet.
        Expression[][] adjointMatrixEntry = new Expression[matrix.getRowNumber()][matrix.getColumnNumber()];
        MatrixExpression currentEntryOfAdjointMatrix;
        boolean currentSignIsPositiv;

        for (int i = 0; i < matrix.getRowNumber(); i++) {
            for (int j = 0; j < matrix.getColumnNumber(); j++) {

                currentSignIsPositiv = (i + j) % 2 == 0;
                currentEntryOfAdjointMatrix = new MatrixFunction(matrix.minor(i, j), TypeMatrixFunction.det).simplify();
                if (!(currentEntryOfAdjointMatrix.convertOneTimesOneMatrixToExpression() instanceof Expression)) {
                    /*
                     Dann konnte der aktuelle Eintrag der adjungierten Matrix
                     nicht explizit berechnet werden. In diesem Fall soll als
                     Ergebnis matrix^(-1) zurückgegeben werden.
                     */
                    return new MatrixPower(matrix, Expression.MINUS_ONE);
                }

                if (currentSignIsPositiv) {
                    adjointMatrixEntry[j][i] = (Expression) currentEntryOfAdjointMatrix.convertOneTimesOneMatrixToExpression();
                } else {
                    adjointMatrixEntry[j][i] = Expression.MINUS_ONE.mult((Expression) currentEntryOfAdjointMatrix.convertOneTimesOneMatrixToExpression());
                }

            }
        }

        Matrix adjointMatrix = new Matrix(adjointMatrixEntry);
        return new MatrixPower(det, Expression.MINUS_ONE).mult(adjointMatrix);

    }

    @Override
    public boolean equals(MatrixExpression matExpr) {
        return matExpr.isPower() && this.left.equals(((MatrixPower) matExpr).getLeft())
                && this.right.equals((Expression) ((MatrixPower) matExpr).getRight());
    }

    @Override
    public boolean equivalent(MatrixExpression matExpr) {
        return matExpr.isPower() && this.left.equivalent(((MatrixPower) matExpr).getLeft())
                && this.right.equivalent(((MatrixPower) matExpr).getRight());
    }

    @Override
    public MatrixExpression orderSumsAndProducts() throws EvaluationException {
        return new MatrixPower(this.left.orderSumsAndProducts(), this.right.orderSumsAndProducts());
    }

    @Override
    public MatrixExpression orderDifferences() throws EvaluationException {
        return new MatrixPower(this.left.orderDifferences(), this.right.orderDifferencesAndQuotients());
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
    public void addContainedVars(Set<String> vars) {
        this.left.addContainedVars(vars);
        this.right.addContainedVars(vars);
    }

    @Override
    public void addContainedIndeterminates(Set<String> vars) {
        this.left.addContainedIndeterminates(vars);
        this.right.addContainedIndeterminates(vars);
    }

    @Override
    public MatrixExpression turnToApproximate() {
        return new MatrixPower(this.left.turnToApproximate(), this.right.turnToApproximate());
    }

    @Override
    public MatrixExpression turnToPrecise() {
        return new MatrixPower(this.left.turnToPrecise(), this.right.turnToPrecise());
    }
    
    @Override
    public boolean containsApproximates(){
        return this.left.containsApproximates() ||this.right.containsApproximates();
    }
    
    @Override
    public MatrixExpression copy() {
        return new MatrixPower(this.left.copy(), this.right.copy());
    }

    @Override
    public MatrixExpression evaluate() throws EvaluationException {
        return new MatrixPower(this.left.evaluate(), new Constant(this.right.evaluate()));
    }

    @Override
    public MatrixExpression diff(String var) throws EvaluationException {
        Object[] params = new Object[2];
        params[0] = this;
        params[1] = var;
        return new MatrixOperator(TypeMatrixOperator.diff, params);
    }

    @Override
    public MatrixExpression replaceVariable(String var, Expression expr) {
        return new MatrixPower(this.left.replaceVariable(var, expr), this.right.replaceVariable(var, expr));
    }

    @Override
    public MatrixExpression replaceMatrixVariable(String var, MatrixExpression matExpr) {
        return new MatrixPower(this.left.replaceMatrixVariable(var, matExpr), this.right);
    }
    
    @Override
    public String toString() {

        String baseAsText, exponentAsText;

        if (this.left instanceof MatrixBinaryOperation) {
            baseAsText = "(" + this.left.toString() + ")";
        } else {
            baseAsText = this.left.toString();
        }

        if (this.right instanceof BinaryOperation
                || (this.right instanceof Constant && this.right.isNonPositive())) {
            exponentAsText = "(" + this.right.toString()+ ")";
        } else {
            exponentAsText = this.right.toString();
        }

        return baseAsText + "^" + exponentAsText;

    }

    @Override
    public MatrixExpression simplifyBasic() throws EvaluationException {
        return this.left.simplifyBasic().pow(this.right.simplifyBasic());
    }

    @Override
    public MatrixExpression simplifyByInsertingDefinedVars() throws EvaluationException {
        return new MatrixPower(this.left.simplifyByInsertingDefinedVars(), this.right.simplifyByInsertingDefinedVars());
    }
    
    @Override
    public MatrixExpression simplifyMatrixEntries() throws EvaluationException {
        return new MatrixPower(this.left.simplifyMatrixEntries(), this.right.simplify());
    }

    @Override
    public MatrixExpression simplifyMatrixEntries(Set<TypeSimplify> simplifyTypes) throws EvaluationException {
        return new MatrixPower(this.left.simplifyMatrixEntries(simplifyTypes), this.right.simplify(simplifyTypes));
    }

    @Override
    public MatrixExpression simplifyCollectProducts() throws EvaluationException {
        return new MatrixPower(this.left.simplifyCollectProducts(), this.right.simplifyCollectProducts());
    }
    
    @Override
    public MatrixExpression simplifyFactorizeScalars() throws EvaluationException {
        return new MatrixPower(this.left.simplifyFactorizeScalars(), this.right.simplifyFactorize());
    }
    
    @Override
    public MatrixExpression simplifyFactorize() throws EvaluationException {
        return new MatrixPower(this.left.simplifyFactorize(), this.right.simplifyFactorize());
    }
    
    @Override
    public MatrixExpression simplifyMatrixFunctionalRelations() throws EvaluationException {

        MatrixExpression matExpr = new MatrixPower(this.left.simplifyMatrixFunctionalRelations(), this.right);
        MatrixExpression matExprSimplified;

        matExprSimplified = SimplifyMatrixFunctionalRelationsUtils.simplifyDoublePowers(matExpr);
        if (!matExprSimplified.equals(matExpr)) {
            return matExprSimplified;
        }

        return matExpr;

    }

}
