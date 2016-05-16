package abstractexpressions.matrixexpression.classes;

import enums.TypeSimplify;
import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.util.HashSet;

public class Matrix extends MatrixExpression {

    private final Expression[][] entry;

    /**
     * Konstruktor: erzeugt eine MatrixExpression aus dem Array entry.
     * VORAUSSETZUNG: die Spalten- und die Zeilenzahl ist mindestens 1
     */
    public Matrix(Expression[][] entry) {
        this.entry = new Expression[entry.length][entry[0].length];
        for (int i = 0; i < entry.length; i++) {
            System.arraycopy(entry[i], 0, this.entry[i], 0, entry[0].length);
        }
    }

    /**
     * Konstruktor: erzeugt eine MatrixExpression aus dem Array entry.
     * VORAUSSETZUNG: die Spalten- und die Zeilenzahl ist mindestens 1
     */
    public Matrix(int[][] entry) {
        this.entry = new Expression[entry.length][entry[0].length];
        for (int i = 0; i < entry.length; i++) {
            for (int j = 0; j < entry[0].length; j++) {
                this.entry[i][j] = new Constant(entry[i][j]);
            }
        }
    }

    /**
     * Konstruktor: erzeugt eine MatrixExpression aus dem Array entry.<br>
     * VORAUSSETZUNG: die Spalten- und die Zeilenzahl ist mindestens 1
     */
    public Matrix(double[][] entry) throws EvaluationException {
        this.entry = new Expression[entry.length][entry[0].length];
        for (int i = 0; i < entry.length; i++) {
            for (int j = 0; j < entry[0].length; j++) {
                this.entry[i][j] = new Constant(entry[i][j]);
            }
        }
    }

    /**
     * Konstruktor: erzeugt eine MatrixExpression aus dem Array entry, welcher
     * als Vektor interpretiert wird. Voraussetzung: die Spalten- und die
     * Zeilenzahl ist mindestens 1
     */
    public Matrix(Expression[] entry) {
        this.entry = new Expression[entry.length][1];
        for (int i = 0; i < entry.length; i++) {
            this.entry[i][0] = entry[i];
        }
    }

    /**
     * Konstruktor: erzeugt eine (1x1)-Matrix aus der Expression expr.
     */
    public Matrix(Expression entry) {
        this.entry = new Expression[1][1];
        this.entry[0][0] = entry;
    }

    /**
     * Konstruktor: erzeugt eine (1x1)-Matrix aus mit einem Eintrag = a.
     */
    public Matrix(BigDecimal a) {
        this.entry = new Expression[1][1];
        this.entry[0][0] = new Constant(a);
    }

    public int getRowNumber() {
        return this.entry.length;
    }

    public int getColumnNumber() {
        if (this.entry.length == 0) {
            return 0;
        }
        return this.entry[0].length;
    }

    /**
     * Gibt den (i, j)-ten Matrixeintrag zurück. Falls i oder j nicht im
     * richtigen Bereich liegen, wird null zurückgegeben.
     */
    public Expression getEntry(int i, int j) {
        if (this.entry.length > 0 && this.entry[0].length > 0
                && i >= 0 && i < this.entry.length && j >= 0 && j < this.entry[0].length) {
            return this.entry[i][j];
        }
        return null;
    }

    /**
     * Gibt die i-te Matrixzeile zurück. Falls i nicht im richtigen Bereich
     * liegt, wird null zurückgegeben.
     */
    public Expression[] getRow(int i) {
        if (this.entry.length > 0 && this.entry[0].length > 0
                && i >= 0 && i < this.entry.length) {
            Expression[] row = new Expression[this.entry[0].length];
            System.arraycopy(this.entry[i], 0, row, 0, this.entry[0].length);
            return row;
        }
        return null;
    }

    /**
     * Gibt die i-te Matrixspalte zurück. Falls i nicht im richtigen Bereich
     * liegt, wird null zurückgegeben.
     */
    public Expression[] getColumn(int i) {
        if (this.entry.length > 0 && this.entry[0].length > 0
                && i >= 0 && i < this.entry[0].length) {
            Expression[] column = new Expression[this.entry.length];
            for (int j = 0; j < this.entry.length; j++) {
                column[j] = this.entry[j][i];
            }
            return column;
        }
        return null;
    }

    /**
     * Gibt zurück, ob die i-te Zeile der vorliegenden Matrix eine Nullzeile
     * ist. Existiert zum gegebenen Index i keine Zeile, so wird true
     * zurückgegeben.
     */
    public boolean isRowZero(int i) {
        Expression[] row = this.getRow(i);
        if (row == null) {
            return true;
        }
        for (int j = 0; j < row.length; j++) {
            if (!row[j].equals(ZERO)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gibt zurück, ob die i-te Spalte der vorliegenden Matrix eine Nullspalte
     * ist. Existiert zum gegebenen Index i keine Spalte, so wird true
     * zurückgegeben.
     */
    public boolean isColumnZero(int i) {
        Expression[] column = this.getColumn(i);
        if (column == null) {
            return true;
        }
        for (int j = 0; j < column.length; j++) {
            if (!column[j].equals(ZERO)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Dimension getDimension() {
        return new Dimension(this.getColumnNumber(), this.getRowNumber());
    }

    @Override
    public MatrixExpression simplifyComputeMatrixOperations() {
        return this;
    }

    /**
     * Gibt zurück, ob eine Matrix quadratisch ist.
     */
    public boolean isSquareMatrix() {
        Dimension dim = this.getDimension();
        return dim.width == dim.height;
    }

    /**
     * Gibt zurück, ob eine Matrix Diagonalgestalt besitzt. Dies trifft genau
     * dann zu, wenn sie quadratisch ist und nur auf der Diagonalen
     * nicht-triviale Einträge vorhanden sind.
     */
    public boolean isDiagonalMatrix() {

        Dimension dim = this.getDimension();
        if (dim.width != dim.height) {
            return false;
        }
        for (int i = 0; i < dim.height; i++) {
            for (int j = 0; j < dim.width; j++) {
                if (i != j && !this.entry[i][j].equals(ZERO)) {
                    return false;
                }
            }
        }
        return true;

    }

    /**
     * Gibt zurück, ob eine Matrix A = (a_{i, j}) eine Dreiecksgestalt besitzt.
     */
    public boolean isTriangularMatrix() {
        return isLowerTriangularMatrix() || isUpperTriangularMatrix();
    }

    /**
     * Gibt zurück, ob eine Matrix A = (a_{i, j}) untere Dreiecksgestalt
     * besitzt. Dies trifft genau dann zu, wenn sie quadratisch ist und für i <
     * j gilt a_{i, j} = 0.
     */
    public boolean isLowerTriangularMatrix() {

        Dimension dim = this.getDimension();
        if (dim.width != dim.height) {
            return false;
        }
        /*
         Zwar ist dim.height == dim.width, aber trotzdem wird hier aus
         Schönheitsgründen zwischen den Bezeichnern unterschieden.
         */
        for (int i = 0; i < dim.height; i++) {
            for (int j = i + 1; j < dim.width; j++) {
                if (!this.entry[i][j].equals(ZERO)) {
                    return false;
                }
            }
        }
        return true;

    }

    /**
     * Gibt zurück, ob eine Matrix A = (a_{i, j}) obere Dreiecksgestalt besitzt.
     * Dies trifft genau dann zu, wenn sie quadratisch ist und für i > j gilt
     * a_{i, j} = 0.
     */
    public boolean isUpperTriangularMatrix() {

        Dimension dim = this.getDimension();
        if (dim.width != dim.height) {
            return false;
        }
        for (int i = 0; i < dim.height; i++) {
            for (int j = 0; j < i; j++) {
                if (!this.entry[i][j].equals(ZERO)) {
                    return false;
                }
            }
        }
        return true;

    }

    /**
     * Gibt zurück, ob eine Matrix A = (a_{i, j}) Dreiecksgestalt besitzt und
     * nilpotent ist (d.h. auf der Diagonalen stehen nur Nullen), ODER ob A
     * rational und nilpotent ist.
     */
    public boolean isNilpotentMatrix() {

        if (!this.isSquareMatrix()) {
            return false;
        }

        Dimension dim = this.getDimension();
        if (this.isUpperTriangularMatrix() || this.isLowerTriangularMatrix()) {
            // Ab hier ist this eine quadratische Matrix in Dreiecksgestalt.
            for (int i = 0; i < dim.height; i++) {
                if (!this.entry[i][i].equals(ZERO)) {
                    return false;
                }
            }
            return true;
        }

        /* 
         Nächster Test: Matrix ist rational und es gibt eine Potenz der Ordnung 
         <= dim, die eine Nullmatrix ergibt.
         */
        if (this.isRationalMatrix()) {
            int exponent = 1;
            MatrixExpression powerOfMatrix = this;
            // Prüfung, ob m^(dim.height) == 0 liefert mit m = this. 
            try {
                while (!powerOfMatrix.equals(MatrixExpression.getZeroMatrix(dim.height, dim.width)) && exponent <= dim.height) {
                    powerOfMatrix = powerOfMatrix.mult(this).simplify();
                    exponent++;
                }
                return powerOfMatrix.equals(MatrixExpression.getZeroMatrix(dim.height, dim.width));
            } catch (EvaluationException e) {
                return false;
            }
        }

        return false;

    }

    /**
     * Gibt zurück, ob eine Matrix A = (a_{i, j}) Dreiecksgestalt besitzt und
     * unipotent ist (d.h. auf der Diagonalen stehen nur Einsen), ODER ob A
     * rational und unipotent ist.
     */
    public boolean isUnipotentMatrix() {

        if (!this.isSquareMatrix()) {
            return false;
        }

        Dimension dim = this.getDimension();
        if (this.isUpperTriangularMatrix() || this.isLowerTriangularMatrix()) {
            // Ab hier ist this eine quadratische Matrix in Dreiecksgestalt.
            for (int i = 0; i < dim.height; i++) {
                if (!this.entry[i][i].equals(ONE)) {
                    return false;
                }
            }
            return true;
        }

        /* 
         Nächster Test: Matrix ist rational und es gibt eine Potenz der Ordnung 
         <= dim, die eine Nullmatrix ergibt.
         */
        if (this.isRationalMatrix()) {
            int exponent = 1;
            MatrixExpression powerOfMatrix = this;
            // m = Matrix - Id berechnen.
            Expression[][] entry = new Expression[dim.width][dim.height];
            for (int i = 0; i < dim.height; i++) {
                for (int j = 0; j < dim.width; j++) {
                    if (i == j){
                        try {
                            entry[i][j] = this.getEntry(i, j).sub(ONE).simplify();
                        } catch (EvaluationException ex) {
                            return false;
                        }
                    } else {
                        entry[i][j] = this.getEntry(i, j);
                    }
                }
            }
            Matrix mMinusId = new Matrix(entry);
            // Prüfung, ob m^(dim.height) == 0 liefert. 
            try {
                while (!powerOfMatrix.equals(MatrixExpression.getZeroMatrix(dim.height, dim.width)) && exponent <= dim.height) {
                    powerOfMatrix = powerOfMatrix.mult(mMinusId).simplify();
                    exponent++;
                }
                return powerOfMatrix.equals(MatrixExpression.getZeroMatrix(dim.height, dim.width));
            } catch (EvaluationException e) {
                return false;
            }
        }

        return false;

    }

    /**
     * Gibt die Länge des oberen linken quadratischen Blocks der Matrix A =
     * (a_{i, j}) zurück, falls A eine quadratische Matrix ist. Ansonsten wird
     * false zurückgegeben.
     */
    public Object getLengthOfUpperSquareBlock() {

        if (!this.isSquareMatrix()) {
            return false;
        }

        Dimension dim = this.getDimension();
        int l = 1;
        boolean isBlock;

        while (l < dim.height) {

            isBlock = true;
            // Prüfung, ob rechts vom oberen Block nur 0 stehen.
            for (int i = 0; i < l; i++) {
                for (int j = l; j < dim.width; j++) {
                    isBlock = isBlock && this.entry[i][j].equals(ZERO);
                    if (!isBlock) {
                        break;
                    }
                }
                if (!isBlock) {
                    break;
                }
            }

            if (isBlock) {
                return l;
            }

            isBlock = true;
            // Prüfung, ob unterhalb des oberen Block nur 0 stehen.
            for (int i = l; i < dim.height; i++) {
                for (int j = 0; j < l; j++) {
                    isBlock = isBlock && this.entry[i][j].equals(ZERO);
                    if (!isBlock) {
                        break;
                    }
                }
                if (!isBlock) {
                    break;
                }
            }

            if (isBlock) {
                return l;
            }
            l++;

        }

        return dim.height;

    }

    /**
     * Gibt zurück, ob eine Matrix nur konstante rationale Einträge besitzt.
     */
    public boolean isRationalMatrix() {

        Dimension dim = this.getDimension();
        for (int i = 0; i < dim.height; i++) {
            for (int j = 0; j < dim.width; j++) {
                if (!this.entry[i][j].isIntegerConstantOrRationalConstant()) {
                    return false;
                }
            }
        }
        return true;

    }

    /**
     * Gibt die Matrix zurück, welche sich durch das Streichen der i. Zeile und
     * der j. Spalte ergibt.
     *
     * @throws EvaluationException
     */
    public Matrix minor(int i, int j) throws EvaluationException {

        Dimension dim = this.getDimension();

        if (dim.height == 0 || dim.width == 0) {
            return new Matrix(Expression.ZERO);
        }

        Expression[][] minorEntry = new Expression[dim.height - 1][dim.width - 1];

        if (i < 0 || i >= dim.height || j < 0 || j >= dim.width) {
            return new Matrix(Expression.ZERO);
        }

        for (int p = 0; p < dim.height - 1; p++) {
            for (int q = 0; q < dim.width - 1; q++) {

                if (p < i && q < j) {
                    minorEntry[p][q] = entry[p][q];
                } else if (p >= i && q < j) {
                    minorEntry[p][q] = entry[p + 1][q];
                } else if (p < i && q >= j) {
                    minorEntry[p][q] = entry[p][q + 1];
                } else {
                    minorEntry[p][q] = entry[p + 1][q + 1];
                }

            }
        }

        return new Matrix(minorEntry);

    }

    /**
     * Hier folgt die Implementierung einiger Elementarumformungen für Zeilen
     * von Matrizen.
     */
    /**
     * Elementarumformung: Zeile i = c * Zeile i.
     */
    public Matrix multRow(int i, Expression c) {

        if (this.entry.length > 0 && this.entry[0].length > 0
                && i >= 0 && i < this.entry.length) {

            Expression[][] e = new Expression[this.entry.length][this.entry[0].length];
            for (int p = 0; p < this.entry.length; p++) {
                for (int q = 0; q < this.entry[0].length; q++) {
                    if (p == i) {
                        e[p][q] = c.mult(this.entry[p][q]);
                    } else {
                        e[p][q] = this.entry[p][q];
                    }
                }
            }
            return new Matrix(e);

        }
        return this;

    }

    /**
     * Elementarumformung: Zeile i und Zeile j vertauschen.
     */
    public Matrix interchangeRows(int i, int j) {

        if (i != j && this.entry.length > 0 && this.entry[0].length > 0
                && i >= 0 && j >= 0 && i < this.entry.length && j < this.entry.length) {

            int s = Math.min(i, j);
            int t = Math.max(i, j);

            Expression[][] e = new Expression[this.entry.length][this.entry[0].length];
            for (int p = 0; p < this.entry.length; p++) {
                for (int q = 0; q < this.entry[0].length; q++) {
                    if (p == s) {
                        e[s][q] = this.entry[t][q];
                        e[t][q] = this.entry[s][q];
                    } else if (p != t) {
                        e[p][q] = this.entry[p][q];
                    }
                }
            }
            return new Matrix(e);

        }
        return this;

    }

    /**
     * Elementarumformung: Zeile i = Zeile i + c * Zeile j.
     */
    public Matrix addMultipleOfRowToRow(int i, int j, Expression c) {

        if (this.entry.length > 0 && this.entry[0].length > 0
                && i >= 0 && i < this.entry.length && j >= 0 && j < this.entry[0].length) {

            Expression[][] e = new Expression[this.entry.length][this.entry[0].length];
            for (int p = 0; p < this.entry.length; p++) {
                for (int q = 0; q < this.entry[0].length; q++) {
                    if (p == i) {
                        e[i][q] = this.entry[i][q].add(c.mult(this.entry[j][q]));
                    } else {
                        e[p][q] = this.entry[p][q];
                    }
                }
            }
            return new Matrix(e);

        }
        return this;

    }

    @Override
    public boolean equals(MatrixExpression matExpr) {

        boolean result = matExpr.isMatrix() && ((Matrix) matExpr).getRowNumber() == this.getRowNumber()
                && ((Matrix) matExpr).getColumnNumber() == this.getColumnNumber();

        if (!result) {
            return false;
        }

        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                result = result && this.entry[i][j].equals(((Matrix) matExpr).getEntry(i, j));
                if (!result) {
                    return false;
                }
            }
        }

        return true;

    }

    @Override
    public boolean equivalent(MatrixExpression matExpr) {

        boolean result = matExpr.isMatrix() && ((Matrix) matExpr).getRowNumber() == this.getRowNumber()
                && ((Matrix) matExpr).getColumnNumber() == this.getColumnNumber();

        if (!result) {
            return false;
        }

        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                result = result && this.entry[i][j].equivalent(((Matrix) matExpr).getEntry(i, j));
                if (!result) {
                    return false;
                }
            }
        }

        return true;

    }

    @Override
    public MatrixExpression orderSumsAndProducts() throws EvaluationException {
        return this;
    }

    @Override
    public MatrixExpression orderDifferences() throws EvaluationException {
        return this;
    }

    @Override
    public boolean isConstant() {

        boolean result = true;

        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                result = result && this.entry[i][j].isConstant();
                if (!result) {
                    return false;
                }
            }
        }

        return true;

    }

    @Override
    public boolean contains(String var) {
        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                if (this.entry[i][j].contains(var)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void addContainedVars(HashSet<String> vars) {
        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                this.entry[i][j].addContainedVars(vars);
            }
        }
    }

    @Override
    public void addContainedIndeterminates(HashSet<String> vars) {
        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                this.entry[i][j].addContainedIndeterminates(vars);
            }
        }
    }

    @Override
    public MatrixExpression turnToApproximate() {
        Expression[][] entryApprox = new Expression[this.entry.length][this.entry[0].length];
        for (int i = 0; i < this.entry.length; i++) {
            for (int j = 0; j < this.entry[0].length; j++) {
                entryApprox[i][j] = this.entry[i][j].turnToApproximate();
            }
        }
        return new Matrix(entryApprox);
    }

    @Override
    public MatrixExpression turnToPrecise() {
        Expression[][] entryApprox = new Expression[this.entry.length][this.entry[0].length];
        for (int i = 0; i < this.entry.length; i++) {
            for (int j = 0; j < this.entry[0].length; j++) {
                entryApprox[i][j] = this.entry[i][j].turnToPrecise();
            }
        }
        return new Matrix(entryApprox);
    }

    @Override
    public boolean containsApproximates() {
        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                if (this.entry[i][j].containsApproximates()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public MatrixExpression copy() {

        Expression[][] copyOfEntries = new Expression[this.getRowNumber()][this.getColumnNumber()];
        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                copyOfEntries[i][j] = this.entry[i][j].copy();
            }
        }
        return new Matrix(copyOfEntries);

    }

    @Override
    public MatrixExpression evaluate() throws EvaluationException {

        Expression[][] evaluatedEntries = new Expression[this.getRowNumber()][this.getColumnNumber()];
        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                evaluatedEntries[i][j] = new Constant(this.getEntry(i, j).evaluate());
            }
        }
        return new Matrix(evaluatedEntries);

    }

    @Override
    public MatrixExpression diff(String var) throws EvaluationException {

        Dimension dim = this.getDimension();
        Expression[][] entryOfDerivative = new Expression[dim.height][dim.width];

        for (int i = 0; i < this.entry.length; i++) {
            for (int j = 0; j < this.entry[0].length; j++) {
                entryOfDerivative[i][j] = this.entry[i][j].diff(var);
            }
        }

        return new Matrix(entryOfDerivative);

    }

    @Override
    public MatrixExpression replaceVariable(String var, Expression expr) {

        if (this.entry.length == 0) {
            Expression[][] resultEntry = new Expression[0][0];
            return new Matrix(resultEntry);
        }

        Expression[][] resultEntry = new Expression[this.entry.length][this.entry[0].length];
        for (int i = 0; i < this.entry.length; i++) {
            for (int j = 0; j < this.entry[0].length; j++) {
                resultEntry[i][j] = this.entry[i][j].replaceVariable(var, expr);
            }
        }

        return new Matrix(resultEntry);

    }

    @Override
    public String writeMatrixExpression() {

        String entriesAsText = "";
        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {

                entriesAsText = entriesAsText + this.entry[i][j].writeExpression();

                if (j < this.getColumnNumber() - 1) {
                    entriesAsText = entriesAsText + ",";
                }

            }

            if (i < this.getRowNumber() - 1) {
                entriesAsText = entriesAsText + ";";
            }

        }

        return "[" + entriesAsText + "]";

    }

    @Override
    public MatrixExpression simplifyBasic() {
        return this;
    }

    @Override
    public MatrixExpression simplifyByInsertingDefinedVars() throws EvaluationException {

        Expression[][] evaluatedEntries = new Expression[this.getRowNumber()][this.getColumnNumber()];
        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                evaluatedEntries[i][j] = this.getEntry(i, j).simplifyByInsertingDefinedVars();
            }
        }
        return new Matrix(evaluatedEntries);

    }

    @Override
    public MatrixExpression simplifyMatrixEntries() throws EvaluationException {

        Expression[][] entrySimplified = new Expression[this.getRowNumber()][this.getColumnNumber()];
        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                entrySimplified[i][j] = this.entry[i][j].simplify();
            }
        }
        return new Matrix(entrySimplified);

    }

    @Override
    public MatrixExpression simplifyMatrixEntries(HashSet<TypeSimplify> simplifyTypes) throws EvaluationException {
        Expression[][] entrySimplified = new Expression[this.getRowNumber()][this.getColumnNumber()];
        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                entrySimplified[i][j] = this.entry[i][j].simplify(simplifyTypes);
            }
        }
        return new Matrix(entrySimplified);
    }

    @Override
    public MatrixExpression simplifyCollectProducts() {
        return this;
    }

    @Override
    public MatrixExpression simplifyFactorizeScalars() throws EvaluationException {
        return this;
    }

    @Override
    public MatrixExpression simplifyFactorize() throws EvaluationException {
        Expression[][] entrySimplified = new Expression[this.getRowNumber()][this.getColumnNumber()];
        for (int i = 0; i < this.getRowNumber(); i++) {
            for (int j = 0; j < this.getColumnNumber(); j++) {
                entrySimplified[i][j] = this.entry[i][j].simplifyFactorize();
            }
        }
        return new Matrix(entrySimplified);
    }

    @Override
    public MatrixExpression simplifyMatrixFunctionalRelations() {
        return this;
    }

}
