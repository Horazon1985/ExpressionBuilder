package matrixexpressionbuilder;

import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.TypeSimplify;
import java.awt.Dimension;
import java.util.HashSet;
import linearalgebraalgorithms.EigenvaluesEigenvectorsAlgorithms;
import matrixsimplifymethods.SimplifyMatrixFunctionalRelations;
import translator.Translator;

public class MatrixFunction extends MatrixExpression {

    private final MatrixExpression left;
    private final TypeMatrixFunction type;

    public MatrixFunction(MatrixExpression left, TypeMatrixFunction type) {
        this.left = left;
        this.type = type;
    }

    public TypeMatrixFunction getType() {
        return this.type;
    }

    public MatrixExpression getLeft() {
        return this.left;
    }

    public String getName() {
        return this.type.toString();
    }

    @Override
    public Dimension getDimension() throws EvaluationException {
        if (this.type.equals(TypeMatrixFunction.trans)) {
            return new Dimension(this.left.getDimension().height, this.left.getDimension().width);
        }
        if (this.type.equals(TypeMatrixFunction.det) || this.type.equals(TypeMatrixFunction.tr)) {
            return new Dimension(1, 1);
        }
        /*
         Alle anderen Funktionen, die durch eine Potenzreiche definiert sind. 
         Voraussetzung: F(this) oder ln(this) sind wohldefiniert, d.h.
         this stellt eine quadratische Matrix dar.
         */
        return new Dimension(this.left.getDimension().width, this.left.getDimension().height);
    }

    @Override
    public MatrixExpression computeMatrixOperations() throws EvaluationException {

        MatrixExpression leftComputed = this.left.computeMatrixOperations();
        MatrixFunction matExpr = new MatrixFunction(leftComputed, this.type);

        if (matExpr.type.equals(TypeMatrixFunction.det)) {
            return matExpr.computeDet();
        } else if (matExpr.type.equals(TypeMatrixFunction.cos)) {
            return matExpr.computeCos();
        } else if (matExpr.type.equals(TypeMatrixFunction.cosh)) {
            return matExpr.computeCosh();
        } else if (matExpr.type.equals(TypeMatrixFunction.exp)) {
            return matExpr.computeExp();
        } else if (matExpr.type.equals(TypeMatrixFunction.ln)) {
            return matExpr.computeLn();
        } else if (matExpr.type.equals(TypeMatrixFunction.sin)) {
            return matExpr.computeSin();
        } else if (matExpr.type.equals(TypeMatrixFunction.sinh)) {
            return matExpr.computeSinh();
        } else if (matExpr.type.equals(TypeMatrixFunction.tr)) {
            return matExpr.computeTr();
        } else if (matExpr.type.equals(TypeMatrixFunction.trans)) {
            return matExpr.computeTrans();
        }
        // Defaultfall: 
        return matExpr;

    }

    @Override
    public boolean equals(MatrixExpression matExpr) {
        return matExpr instanceof MatrixFunction
                && this.getType().equals(((MatrixFunction) matExpr).getType())
                && this.getLeft().equals(((MatrixFunction) matExpr).getLeft());
    }

    @Override
    public boolean equivalent(MatrixExpression matExpr) {
        return matExpr instanceof MatrixFunction
                && this.getType().equals(((MatrixFunction) matExpr).getType())
                && this.getLeft().equivalent(((MatrixFunction) matExpr).getLeft());
    }

    @Override
    public MatrixExpression orderSumsAndProducts() throws EvaluationException {
        return new MatrixFunction(this.left.orderSumsAndProducts(), this.type);
    }

    @Override
    public MatrixExpression orderDifferences() throws EvaluationException {
        return new MatrixFunction(this.left.orderDifferences(), this.type);
    }

    @Override
    public boolean isConstant() {
        return this.left.isConstant();
    }

    @Override
    public boolean contains(String var) {
        return this.left.contains(var);
    }

    @Override
    public void addContainedVars(HashSet<String> vars) {
        this.left.addContainedVars(vars);
    }

    @Override
    public MatrixExpression copy() {
        return new MatrixFunction(this.left, this.type);
    }

    @Override
    public MatrixExpression diff(String var) throws EvaluationException {

        if (this.type.equals(TypeMatrixFunction.tr) || this.type.equals(TypeMatrixFunction.trans)) {
            return new MatrixFunction(this.left.diff(var), this.type);
        }
        // Hier ist this.type == TypeMatrixFunction.det
        Object[] params = new Object[2];
        params[0] = this;
        params[1] = var;
        return new MatrixOperator(TypeMatrixOperator.diff, params);

    }

    @Override
    public MatrixExpression replaceVariable(String var, Expression expr) {
        return new MatrixFunction(this.left.replaceVariable(var, expr), this.type);
    }

    @Override
    public String writeMatrixExpression() {
        return this.type.toString() + "(" + this.left.writeMatrixExpression() + ")";
    }

    /**
     * Berechnet die Determinante einer MatrixExpression, falls möglich. Das
     * Ergebnis wird als eine (1x1)-matrix interpretiert.
     *
     * @throws EvaluationException
     */
    public MatrixExpression computeDet() throws EvaluationException {

        Dimension dim = this.left.getDimension();
        if (dim.height != dim.width) {
            throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixFunction_DET_NOT_DEFINED"));
        }

        if (dim.height == 0) {
            // Die Determinante einer (0x0)-Matrix soll 1 sein.
            return new Matrix(Expression.ONE);
        }

        if (dim.height == 1) {
            return this.left;
        }

        // Fall: Determinante einer Dreiecksmatrix.
        if (this.left.isMatrix() && (((Matrix) this.left).isLowerTriangularMatrix() || ((Matrix) this.left).isUpperTriangularMatrix())) {
            return SimplifyMatrixFunctionalRelations.simplifyDetOfTriangularMatrix(this);
        }

        // Fall: Determinante einer Blockmatrix.
        if (this.left.isMatrix() && ((Matrix) this.left).getLengthOfUpperSquareBlock() instanceof Integer
                && (int) ((Matrix) this.left).getLengthOfUpperSquareBlock() < ((Matrix) this.left).getRowNumber()) {
            return SimplifyMatrixFunctionalRelations.simplifyDetOfBlockMatrix(this);
        }

        // Fall: Determinante einer Matrix mit rationalen Koeffizienten.
        if (this.left.isMatrix() && ((Matrix) this.left).isRationalMatrix()) {
            return computeDetByRowEchelonForm();
        }

        // Fall: Determinante einer allgemeinen quadratischen Matrix.
        if (dim.height <= ComputationBounds.BOUND_MATRIX_MAX_DIM_FOR_COMPUTE_DET_EXPLICITELY) {

            int indexOfRowWithMaxZeros = 0, indexOfColumnWithMaxZeros = 0;
            int numberOfZerosInRow = 0, numberOfZerosInColumn = 0;

            /*
             zunächst wird ermittelt, ob Zeilen oder Spalten mit Nullen
             vorhanden sind. Wenn ja, wird nach derjenigen Zeile/Spalte
             entwickelt, welche am meisten Nullen aufweist. Ansonsten wird die
             Laplace-Entwicklung nach der ersten Zeile ausgeführt.
             */
            int currentNumberOfZeros;
            for (int i = 0; i < dim.height; i++) {
                currentNumberOfZeros = this.numberOfZerosInRow(i);
                if (currentNumberOfZeros > numberOfZerosInRow) {
                    indexOfRowWithMaxZeros = i;
                    numberOfZerosInRow = currentNumberOfZeros;
                }
            }
            for (int j = 0; j < dim.width; j++) {
                currentNumberOfZeros = this.numberOfZerosInColumn(j);
                if (currentNumberOfZeros > numberOfZerosInColumn) {
                    indexOfColumnWithMaxZeros = j;
                    numberOfZerosInColumn = currentNumberOfZeros;
                }
            }

            if (numberOfZerosInRow >= numberOfZerosInColumn) {
                /*
                 Falls in einer Zeile mit maximal vielen Nullen genauso viele
                 Nullen vorhanden sind wie in einer Spalte mit maximal vielen
                 Nullen, so wird hier automatisch die Laplace-Entwicklung nach
                 der entsprechenden Zeile ausgeführt.
                 */
                return computeDetByLaplaceRow(indexOfRowWithMaxZeros);
            } else {
                return computeDetByLaplaceColumn(indexOfColumnWithMaxZeros);
            }

        }

        return this;

    }

    /**
     * Berechnet den Kosinus einer MatrixExpression, falls möglich.
     *
     * @throws EvaluationException
     */
    public MatrixExpression computeCos() throws EvaluationException {

        Dimension dim = this.left.getDimension();
        if (dim.height != dim.width) {
            throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixFunction_COS_NOT_DEFINED"));
        }

        if (dim.height == 0) {
            // Die Exponentialfunktion einer (0x0)-Matrix soll 1 sein.
            return new Matrix(Expression.ONE);
        }

        if (dim.height == 1 && this.left.isMatrix()) {
            return new Matrix(((Matrix) this.left).getEntry(0, 0).cos());
        }

        // Fall: Cos einer Diagonalmatrix.
        if (this.left.isMatrix() && ((Matrix) this.left).isDiagonalMatrix()) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfDiagonalMatrix(this, TypeMatrixFunction.cos);
        }

        // Fall: Cos einer diagonalisierbaren Matrix.
        if (this.left.isMatrix() && EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) this.left)) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfDiagonalizableMatrix(this, TypeMatrixFunction.cos);
        }

        // Fall: Cos einer nilpotenten Matrix.
        if (this.left.isMatrix() && ((Matrix) this.left).isNilpotentMatrix()) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfNilpotentMatrix(this, TypeMatrixFunction.cos);
        }

        return this;

    }

    /**
     * Berechnet den hyperbolischen Kosinus einer MatrixExpression, falls
     * möglich.
     *
     * @throws EvaluationException
     */
    public MatrixExpression computeCosh() throws EvaluationException {

        Dimension dim = this.left.getDimension();
        if (dim.height != dim.width) {
            throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixFunction_COSH_NOT_DEFINED"));
        }

        if (dim.height == 0) {
            // Die Exponentialfunktion einer (0x0)-Matrix soll 1 sein.
            return new Matrix(Expression.ONE);
        }

        if (dim.height == 1 && this.left.isMatrix()) {
            return new Matrix(((Matrix) this.left).getEntry(0, 0).cosh());
        }

        // Fall: Cosh einer Diagonalmatrix.
        if (this.left.isMatrix() && ((Matrix) this.left).isDiagonalMatrix()) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfDiagonalMatrix(this, TypeMatrixFunction.cosh);
        }

        // Fall: Cosh einer diagonalisierbaren Matrix.
        if (this.left.isMatrix() && EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) this.left)) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfDiagonalizableMatrix(this, TypeMatrixFunction.cosh);
        }

        // Fall: Cosh einer nilpotenten Matrix.
        if (this.left.isMatrix() && ((Matrix) this.left).isNilpotentMatrix()) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfNilpotentMatrix(this, TypeMatrixFunction.cosh);
        }

        return this;

    }

    /**
     * Berechnet die Exponentialfunktion einer MatrixExpression, falls möglich.
     *
     * @throws EvaluationException
     */
    public MatrixExpression computeExp() throws EvaluationException {

        Dimension dim = this.left.getDimension();
        if (dim.height != dim.width) {
            throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixFunction_EXP_NOT_DEFINED"));
        }

        if (dim.height == 0) {
            // Die Exponentialfunktion einer (0x0)-Matrix soll 1 sein.
            return new Matrix(Expression.ONE);
        }

        if (dim.height == 1 && this.left.isMatrix()) {
            return new Matrix(((Matrix) this.left).getEntry(0, 0).exp());
        }

        // Fall: Exponentialfunktion einer Diagonalmatrix.
        if (this.left.isMatrix() && ((Matrix) this.left).isDiagonalMatrix()) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfDiagonalMatrix(this, TypeMatrixFunction.exp);
        }

        // Fall: Exponentialfunktion einer diagonalisierbaren Matrix.
        if (this.left.isMatrix() && EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) this.left)) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfDiagonalizableMatrix(this, TypeMatrixFunction.exp);
        }

        // Fall: Exponentialfunktion einer nilpotenten Matrix.
        if (this.left.isMatrix() && ((Matrix) this.left).isNilpotentMatrix()) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfNilpotentMatrix(this, TypeMatrixFunction.exp);
        }

        return this;

    }

    /**
     * Berechnet den Logarithmus einer MatrixExpression, falls möglich.
     *
     * @throws EvaluationException
     */
    public MatrixExpression computeLn() throws EvaluationException {

        Dimension dim = this.left.getDimension();
        if (dim.height != dim.width) {
            throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixFunction_LN_NOT_DEFINED"));
        }

        if (dim.height == 0) {
            // Der Logarithmus einer (0x0)-Matrix soll 0 sein.
            return MatrixExpression.getZeroMatrix(1, 1);
        }

        if (dim.height == 1 && this.left.isMatrix()) {
            return new Matrix(((Matrix) this.left).getEntry(0, 0).ln());
        }

        // Fall: Logarithmus einer Diagonalmatrix.
        if (this.left.isMatrix() && ((Matrix) this.left).isDiagonalMatrix()) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfDiagonalMatrix(this, TypeMatrixFunction.ln);
        }

        // Fall: Logarithmus einer diagonalisierbaren Matrix.
        if (this.left.isMatrix() && EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) this.left)) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfDiagonalizableMatrix(this, TypeMatrixFunction.ln);
        }

        // Fall: Logarithmus einer nilpotenten Matrix.
        if (this.left.isMatrix() && ((Matrix) this.left).isNilpotentMatrix()) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfNilpotentMatrix(this, TypeMatrixFunction.ln);
        }

        return this;

    }

    /**
     * Berechnet den Sinus einer MatrixExpression, falls möglich.
     *
     * @throws EvaluationException
     */
    public MatrixExpression computeSin() throws EvaluationException {

        Dimension dim = this.left.getDimension();
        if (dim.height != dim.width) {
            throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixFunction_SIN_NOT_DEFINED"));
        }

        if (dim.height == 0) {
            // Die Exponentialfunktion einer (0x0)-Matrix soll 1 sein.
            return new Matrix(Expression.ONE);
        }

        if (dim.height == 1 && this.left.isMatrix()) {
            return new Matrix(((Matrix) this.left).getEntry(0, 0).sin());
        }

        // Fall: Sin einer Diagonalmatrix.
        if (this.left.isMatrix() && ((Matrix) this.left).isDiagonalMatrix()) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfDiagonalMatrix(this, TypeMatrixFunction.sin);
        }

        // Fall: Sin einer diagonalisierbaren Matrix.
        if (this.left.isMatrix() && EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) this.left)) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfDiagonalizableMatrix(this, TypeMatrixFunction.sin);
        }

        // Fall: Sin einer nilpotenten Matrix.
        if (this.left.isMatrix() && ((Matrix) this.left).isNilpotentMatrix()) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfNilpotentMatrix(this, TypeMatrixFunction.sin);
        }

        return this;

    }

    /**
     * Berechnet den hyperbolischen Sinus einer MatrixExpression, falls möglich.
     *
     * @throws EvaluationException
     */
    public MatrixExpression computeSinh() throws EvaluationException {

        Dimension dim = this.left.getDimension();
        if (dim.height != dim.width) {
            throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixFunction_SINH_NOT_DEFINED"));
        }

        if (dim.height == 0) {
            // Die Exponentialfunktion einer (0x0)-Matrix soll 1 sein.
            return new Matrix(Expression.ONE);
        }

        if (dim.height == 1 && this.left.isMatrix()) {
            return new Matrix(((Matrix) this.left).getEntry(0, 0).cosh());
        }

        // Fall: Sinh einer Diagonalmatrix.
        if (this.left.isMatrix() && ((Matrix) this.left).isDiagonalMatrix()) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfDiagonalMatrix(this, TypeMatrixFunction.sinh);
        }

        // Fall: Sinh einer diagonalisierbaren Matrix.
        if (this.left.isMatrix() && EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) this.left)) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfDiagonalizableMatrix(this, TypeMatrixFunction.sinh);
        }

        // Fall: Sinh einer nilpotenten Matrix.
        if (this.left.isMatrix() && ((Matrix) this.left).isNilpotentMatrix()) {
            return SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfNilpotentMatrix(this, TypeMatrixFunction.sinh);
        }

        return this;

    }

    /**
     * Berechnet die Transponierte einer MatrixExpression, falls möglich.
     *
     * @throws EvaluationException
     */
    public MatrixExpression computeTrans() throws EvaluationException {

        Dimension dim = this.left.getDimension();
        if (dim.height == 0 || dim.width == 0) {
            throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixExpression_NO_ROWS"));
        }

        if (this.left.isMatrix()) {
            int p = ((Matrix) this.left).getColumnNumber(), q = ((Matrix) this.left).getRowNumber();
            Expression[][] entryOfTransposedMatrix = new Expression[p][q];
            for (int i = 0; i < p; i++) {
                for (int j = 0; j < q; j++) {
                    entryOfTransposedMatrix[i][j] = ((Matrix) this.left).getEntry(j, i);
                }
            }
            return new Matrix(entryOfTransposedMatrix);
        }

        /*
         Falls this.left keine Matrix darstellt, dann zunächst alles so
         belassen, wie es ist. Später wird das Transponieren der einzelnen
         Komponenten von this.left in simplify vorgenommen.
         */
        return this;

    }

    /**
     * Berechnet die Spur einer MatrixExpression, falls möglich. Das Ergebnis
     * wird als eine (1x1)-matrix interpretiert.
     *
     * @throws EvaluationException
     */
    public MatrixExpression computeTr() throws EvaluationException {

        Dimension dim = this.left.getDimension();
        if (dim.height != dim.width) {
            throw new EvaluationException(Translator.translateExceptionMessage("MEB_MatrixFunction_TR_NOT_DEFINED"));
        }

        if (this.left.isNotMatrix()) {
            return this;
        }

        Expression trace = Expression.ZERO;
        for (int i = 0; i < dim.width; i++) {
            if (trace.equals(Expression.ZERO)) {
                trace = ((Matrix) this.left).getEntry(i, i);
            } else {
                trace = trace.add(((Matrix) this.left).getEntry(i, i));
            }
        }
        return new Matrix(trace);

    }

    /**
     * Falls das Argument eine Matrix ist, wird die Anzahl der Nullen in der
     * i-ten Zeile zurückgegeben. Ansonsten wird 0 zurückgegeben.
     */
    private int numberOfZerosInRow(int i) {

        if (this.left.isNotMatrix()) {
            return 0;
        }

        Matrix matrix = (Matrix) this.left;

        if (i < 0 || i > matrix.getRowNumber()) {
            return 0;
        }

        int result = 0;
        for (int j = 0; j < matrix.getColumnNumber(); j++) {
            if (matrix.getEntry(i, j).equals(Expression.ZERO)) {
                result++;
            }
        }
        return result;

    }

    /**
     * Falls das Argument eine Matrix ist, wird die Anzahl der Nullen in der
     * j-ten Spalte zurückgegeben. Ansonsten wird 0 zurückgegeben.
     */
    private int numberOfZerosInColumn(int j) {

        if (this.left.isNotMatrix()) {
            return 0;
        }

        Matrix matrix = (Matrix) this.left;

        if (j < 0 || j > matrix.getColumnNumber()) {
            return 0;
        }

        int result = 0;
        for (int i = 0; i < matrix.getColumnNumber(); i++) {
            if (matrix.getEntry(i, j).equals(Expression.ZERO)) {
                result++;
            }
        }
        return result;

    }

    /**
     * Laplaceentwicklung der Determinante nach der i-ten Zeile.
     *
     * @throws EvaluationException
     */
    private MatrixExpression computeDetByLaplaceRow(int i) throws EvaluationException {

        Dimension dim = this.left.getDimension();

        if (this.left.isNotMatrix() || !this.type.equals(TypeMatrixFunction.det)) {
            return this;
        }

        Matrix matrix = (Matrix) this.left;

        if (dim.height == 0 || dim.width == 0) {
            return new Matrix(Expression.ONE);
        }

        boolean currentSignIsPositiv;
        MatrixExpression result = new Matrix(Expression.ZERO);

        for (int j = 0; j < dim.width; j++) {

            if (matrix.getEntry(i, j).equals(Expression.ZERO)) {
                continue;
            }

            currentSignIsPositiv = (i + j) % 2 == 0;
            if (j == 0) {
                if (currentSignIsPositiv) {
                    result = new Matrix(matrix.getEntry(i, 0)).mult(new MatrixFunction(matrix.minor(i, 0), TypeMatrixFunction.det));
                } else {
                    result = new Matrix(Expression.MINUS_ONE.mult(matrix.getEntry(i, 0))).mult(new MatrixFunction(matrix.minor(i, 0), TypeMatrixFunction.det));
                }
            } else {
                if (currentSignIsPositiv) {
                    result = result.add(new Matrix(matrix.getEntry(i, j)).mult(new MatrixFunction(matrix.minor(i, j), TypeMatrixFunction.det)));
                } else {
                    result = result.sub(new Matrix(matrix.getEntry(i, j)).mult(new MatrixFunction(matrix.minor(i, j), TypeMatrixFunction.det)));
                }
            }

        }

        return result;

    }

    /**
     * Laplaceentwicklung der Determinante nach der j-ten Spalte.
     *
     * @throws EvaluationException
     */
    private MatrixExpression computeDetByLaplaceColumn(int j) throws EvaluationException {

        Dimension dim = this.left.getDimension();

        if (this.left.isNotMatrix() || !this.type.equals(TypeMatrixFunction.det)) {
            return this;
        }

        Matrix matrix = (Matrix) this.left;

        if (dim.height == 0 || dim.width == 0) {
            return new Matrix(Expression.ONE);
        }

        boolean currentSignIsPositiv;
        MatrixExpression result = new Matrix(Expression.ZERO);

        for (int i = 0; i < dim.height; i++) {

            if (matrix.getEntry(i, j).equals(Expression.ZERO)) {
                continue;
            }

            currentSignIsPositiv = (i + j) % 2 == 0;
            if (i == 0) {
                if (currentSignIsPositiv) {
                    result = new Matrix(matrix.getEntry(0, j)).mult(new MatrixFunction(matrix.minor(0, j), TypeMatrixFunction.det));
                } else {
                    result = new Matrix(Expression.MINUS_ONE.mult(matrix.getEntry(0, j))).mult(new MatrixFunction(matrix.minor(0, j), TypeMatrixFunction.det));
                }
            } else {
                if (currentSignIsPositiv) {
                    result = result.add(new Matrix(matrix.getEntry(i, j)).mult(new MatrixFunction(matrix.minor(i, j), TypeMatrixFunction.det)));
                } else {
                    result = result.sub(new Matrix(matrix.getEntry(i, j)).mult(new MatrixFunction(matrix.minor(i, j), TypeMatrixFunction.det)));
                }
            }

        }

        return result;

    }

    /**
     * Bringt die Matrix durch elementare Zeilenoperationen auf
     * Zeilenstufenform, falls diese nur rationale Einträge besitzt. Ansonsten
     * wird die Matrix selbst zurückgegeben.
     *
     * @throws EvaluationException
     */
    public MatrixExpression computeDetByRowEchelonForm() throws EvaluationException {

        Dimension dim = this.left.getDimension();

        if (this.left.isNotMatrix() || !((Matrix) this.left).isRationalMatrix()) {
            return this;
        }

        Matrix matrix = (Matrix) this.left;

        if (dim.height == 0 || dim.width == 0) {
            return new Matrix(Expression.ONE);
        }

        // Jetzt wird die Determinante rekursiv berechnet, indem matrix auf Zeilenstufenform gebracht wird.
        int indexOfFirstRowWithNonZeroFirstEntry = 0;
        Expression currentEntry;

        for (int i = 0; i < matrix.getRowNumber(); i++) {
            // Zur Vereinfachung genügt simplifyTrivial(), da die Einträge nur rationale Zahlen sind.
            currentEntry = matrix.getEntry(i, 0).simplifyTrivial();
            if (currentEntry.equals(Expression.ZERO)) {
                indexOfFirstRowWithNonZeroFirstEntry++;
            } else {
                break;
            }
        }

        if (indexOfFirstRowWithNonZeroFirstEntry == matrix.getRowNumber()) {
            // Dann ist die gesamte erste Spalte die 0-Spalte, also ist die Determinante ebenfalls gleich 0.
            return new Matrix(Expression.ZERO);
        }
        // Hier angelangt ist die erste Spalte nicht identisch 0.
        Matrix matrixRowsInterchanged = matrix.interchangeRows(0, indexOfFirstRowWithNonZeroFirstEntry);
        Expression[][] minorEntry = new Expression[matrix.getRowNumber() - 1][matrix.getColumnNumber() - 1];

        Expression pivotElement = matrixRowsInterchanged.getEntry(0, 0);
        for (int i = 1; i < matrix.getRowNumber(); i++) {
            matrixRowsInterchanged = matrixRowsInterchanged.addMultipleOfRowToRow(i, 0,
                    Expression.MINUS_ONE.mult(matrixRowsInterchanged.getEntry(i, 0)).div(pivotElement));
        }

        for (int i = 1; i < matrix.getRowNumber(); i++) {
            for (int j = 1; j < matrix.getColumnNumber(); j++) {
                minorEntry[i - 1][j - 1] = matrixRowsInterchanged.getEntry(i, j);
            }
        }
        Matrix minorMatrix = new Matrix(minorEntry);
        MatrixExpression determinantWithoutSign = new Matrix(pivotElement).mult(new MatrixFunction(minorMatrix, TypeMatrixFunction.det).computeDet());

        if (indexOfFirstRowWithNonZeroFirstEntry > 0) {
            return new Matrix(Expression.MINUS_ONE).mult(determinantWithoutSign);
        }
        return determinantWithoutSign;

    }

    @Override
    public MatrixExpression simplifyTrivial() throws EvaluationException {
        return new MatrixFunction(this.left.simplifyTrivial(), this.type);
    }

    @Override
    public MatrixExpression simplifyMatrixEntries() throws EvaluationException {
        return new MatrixFunction(this.left.simplifyMatrixEntries(), this.type);
    }

    @Override
    public MatrixExpression simplifyMatrixEntries(HashSet<TypeSimplify> simplifyTypes) throws EvaluationException {
        return new MatrixFunction(this.left.simplifyMatrixEntries(simplifyTypes), this.type);
    }

    @Override
    public MatrixExpression simplifyCollectProducts() throws EvaluationException {
        return new MatrixFunction(this.left.simplifyCollectProducts(), this.type);
    }

    @Override
    public MatrixExpression simplifyFactorizeScalarsInSums() throws EvaluationException {
        return new MatrixFunction(this.left.simplifyFactorizeScalarsInSums(), this.type);
    }
    
    @Override
    public MatrixExpression simplifyFactorizeScalarsInDifferences() throws EvaluationException {
        return new MatrixFunction(this.left.simplifyFactorizeScalarsInDifferences(), this.type);
    }
    
    @Override
    public MatrixExpression simplifyMatrixFunctionalRelations() throws EvaluationException {

        MatrixExpression matExpr = new MatrixFunction(this.left.simplifyMatrixFunctionalRelations(), this.type);
        MatrixExpression matExprSimplified;

        matExprSimplified = SimplifyMatrixFunctionalRelations.simplifyDetOfMatrixProducts(matExpr);
        if (!matExprSimplified.equals(matExpr)) {
            return matExprSimplified;
        }

        matExprSimplified = SimplifyMatrixFunctionalRelations.simplifyDetOfMatrixPower(matExpr);
        if (!matExprSimplified.equals(matExpr)) {
            return matExprSimplified;
        }

        matExprSimplified = SimplifyMatrixFunctionalRelations.simplifyTrOfMatrixSums(matExpr);
        if (!matExprSimplified.equals(matExpr)) {
            return matExprSimplified;
        }

        matExprSimplified = SimplifyMatrixFunctionalRelations.simplifyTrOfConjugatedMatrix(matExpr);
        if (!matExprSimplified.equals(matExpr)) {
            return matExprSimplified;
        }

        matExprSimplified = SimplifyMatrixFunctionalRelations.simplifyDetOfExpOfMatrix(matExpr);
        if (!matExprSimplified.equals(matExpr)) {
            return matExprSimplified;
        }

        matExprSimplified = SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfConjugatedMatrix(matExpr, TypeMatrixFunction.cos);
        if (!matExprSimplified.equals(matExpr)) {
            return matExprSimplified;
        }

        matExprSimplified = SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfConjugatedMatrix(matExpr, TypeMatrixFunction.cosh);
        if (!matExprSimplified.equals(matExpr)) {
            return matExprSimplified;
        }

        matExprSimplified = SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfConjugatedMatrix(matExpr, TypeMatrixFunction.exp);
        if (!matExprSimplified.equals(matExpr)) {
            return matExprSimplified;
        }

        matExprSimplified = SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfConjugatedMatrix(matExpr, TypeMatrixFunction.ln);
        if (!matExprSimplified.equals(matExpr)) {
            return matExprSimplified;
        }

        matExprSimplified = SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfConjugatedMatrix(matExpr, TypeMatrixFunction.sin);
        if (!matExprSimplified.equals(matExpr)) {
            return matExprSimplified;
        }

        matExprSimplified = SimplifyMatrixFunctionalRelations.simplifyPowerSeriesFunctionOfConjugatedMatrix(matExpr, TypeMatrixFunction.sinh);
        if (!matExprSimplified.equals(matExpr)) {
            return matExprSimplified;
        }

        return matExpr;

    }

}
