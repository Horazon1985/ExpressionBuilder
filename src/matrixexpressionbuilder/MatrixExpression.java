package matrixexpressionbuilder;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.TypeSimplify;
import java.awt.Dimension;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import translator.Translator;

public abstract class MatrixExpression {

    public final static Matrix MINUS_ONE = new Matrix(Expression.MINUS_ONE);

    /**
     * Falls matrix eine Matrix darstellt, so werden die einzelnen Matrixzeilen
     * in einem Stringarray zurückgegeben. Beispiel: Bei der (2x3)-Matrix matrix
     * = "[x+y,a,b;5,sin(y),f(u,v)]" wird das Stringarray
     * {"x+y","a","b","5","sin(y)","f(u,v)"} zurückgegeben.
     *
     * @throws ExpressionException
     */
    public static String[] getRowsFromMatrix(String matrix) throws ExpressionException {

        int numberOfRows = 1;
        for (int i = 0; i < matrix.length(); i++) {
            if (matrix.substring(i, i + 1).equals(";")) {
                numberOfRows++;
            }
        }

        String[] rows = matrix.split(";");
        if (rows.length != numberOfRows) {
            throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_ROW_MISSING"));
        }

        if (rows.length == 0) {
            throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_NO_ROWS"));
        }
        return rows;

    }

    /**
     * Falls row eine Zeile in einer Matrix darstellt, so werden die einzelnen
     * Matrixeinträge in einem Stringarray zurückgegeben. Beispiel: Bei der 2.
     * Zeile row = "5,sin(y),f(u,v)" aus dem obigen Beispiel bei der
     * Beschreibung von getRowsFromMatrix() wird das Stringarray {"5", "sin(y)",
     * "gcd(12,15)"} zurückgegeben.
     *
     * @throws ExpressionException
     */
    public static ArrayList<String> getEntriesFromRow(String row) throws ExpressionException {

        /*
         Differenz zwischen der Anzahl der öffnenden und der der schließenden
         Klammern (bracketCounter == 0 am Ende -> alles ok).
         */
        int bracketsCount = 0;
        String currentChar;

        //Leerzeichen beseitigen
        row = row.replaceAll(" ", "");

        //Falls Parameterstring leer ist -> fertig.
        if (row.equals("")) {
            return new ArrayList<>();
        }

        ArrayList<String> result = new ArrayList<>();
        int startPositionOfCurrentParameter = 0;

        //Jetzt werden die einzelnen Parameter ausgelesen
        for (int i = 0; i < row.length(); i++) {

            currentChar = row.substring(i, i + 1);
            if (currentChar.equals("(")) {
                bracketsCount++;
            }
            if (currentChar.equals(")")) {
                bracketsCount--;
            }
            if ((bracketsCount == 0) && (currentChar.equals(","))) {
                if (row.substring(startPositionOfCurrentParameter, i).equals("")) {
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_EMPTY_PARAMETER"));
                }
                result.add(row.substring(startPositionOfCurrentParameter, i));
                startPositionOfCurrentParameter = i + 1;
            }
            if (i == row.length() - 1) {
                if (startPositionOfCurrentParameter == row.length()) {
                    throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_EMPTY_PARAMETER"));
                }
                result.add(row.substring(startPositionOfCurrentParameter, row.length()));
            }

        }

        if (bracketsCount != 0) {
            throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_WRONG_BRACKETS"));
        }

        return result;

    }

    /**
     * Erzeugt eine MatrixExpression aus formula.
     *
     * @throws ExpressionException
     */
    public static Matrix getMatrix(String formula, HashSet<String> vars) throws ExpressionException {

        String[] rows = getRowsFromMatrix(formula);
        Object[] currentRow = getEntriesFromRow(rows[0]).toArray();
        Expression[][] entry = new Expression[rows.length][currentRow.length];

        for (int i = 0; i < rows.length; i++) {

            if (rows[i].isEmpty()) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_NOT_A_MATRIX"));
            }
            currentRow = getEntriesFromRow(rows[i]).toArray();
            if (currentRow.length != entry[0].length) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_NOT_A_MATRIX"));
            }
            for (int j = 0; j < currentRow.length; j++) {
                entry[i][j] = Expression.build((String) currentRow[j], vars);
            }

        }

        return new Matrix(entry);

    }

    /**
     * Hauptmethode zum Erstellen einer MatrixExpression aus einem String.
     *
     * @throws ExpressionException
     */
    public static MatrixExpression build(String formula, HashSet<String> vars) throws ExpressionException {

        formula = formula.replaceAll(" ", "").toLowerCase();

        /*
         Versuche zunächst, ob formula zu einer Instanz von Expression
         kompiliert werden kann. Falls ja, soll diese zu einer (1x1)-matrix
         konvertiert und zurückgegeben werden.
         */
        Expression expr;
        try {
            expr = Expression.build(formula, vars);
            return new Matrix(expr);
        } catch (ExpressionException e) {
            /*
             Dann müssen, falls formula eine korrekte MatrixExpression
             darstellt, echte Matrizen involviert sein. -> Weiter im Code.
             */
        }

        // Prioritäten: + = 0, - = 1, * = 2, ^ = 3, 4 = Matrix, Matrixfunktion, MatrixOperator.
        int priority = 4;
        int breakpoint = -1;
        int bracketCounter = 0;
        int squareBracketCounter = 0;
        int formulaLength = formula.length();
        String currentChar;

        if (formula.isEmpty()) {
            throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_EXPRESSION_EMPTY_OR_INCOMPLETE"));
        }

        for (int i = 1; i <= formulaLength; i++) {
            currentChar = formula.substring(formulaLength - i, formulaLength - i + 1);

            // Öffnende und schließende Klammern zählen.
            if (currentChar.equals("(") && bracketCounter == 0) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_WRONG_BRACKETS"));
            }
            if (currentChar.equals("[") && squareBracketCounter == 0) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_WRONG_BRACKETS"));
            }

            if (currentChar.equals(")")) {
                bracketCounter++;
            }
            if (currentChar.equals("(")) {
                bracketCounter--;
            }
            if (currentChar.equals("]")) {
                squareBracketCounter++;
            }
            if (currentChar.equals("[")) {
                squareBracketCounter--;
            }

            if (bracketCounter != 0 || squareBracketCounter != 0) {
                continue;
            }
            // Aufteilungspunkt finden; zunächst wird nach -, +, *, /, ^ gesucht 
            // breakpoint gibt den Index in formula an, wo die Formel aufgespalten werden soll.
            if (currentChar.equals("+") && priority > 0) {
                priority = 0;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("-") && priority > 1) {
                priority = 1;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("*") && priority > 2) {
                priority = 2;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("^") && priority > 3) {
                priority = 3;
                breakpoint = formulaLength - i;
            }
        }

        if (bracketCounter > 0) {
            throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_WRONG_BRACKETS"));
        }

        // Aufteilung, falls eine Elementaroperation (-, +, *, ^) vorliegt
        if (priority <= 3) {
            String formulaLeft = formula.substring(0, breakpoint);
            String formulaRight = formula.substring(breakpoint + 1, formulaLength);

            if (formulaLeft.isEmpty() && priority > 1) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_LEFT_SIDE_OF_MATRIXBINARY_IS_EMPTY"));
            }
            if (formulaRight.isEmpty()) {
                throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_RIGHT_SIDE_OF_MATRIXBINARY_IS_EMPTY"));
            }

            //Falls der Ausdruck die Form "+A..." besitzt -> daraus "A..." machen
            if (formulaLeft.isEmpty() && priority == 0) {
                return build(formulaRight, vars);
            }
            //Falls der Ausdruck die Form "-A..." besitzt -> daraus "(-1)*A..." machen
            if (formulaLeft.isEmpty() && priority == 1) {
                return MINUS_ONE.mult(build(formulaRight, vars));
            }
            switch (priority) {
                case 0:
                    return new MatrixBinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeMatrixBinary.PLUS);
                case 1:
                    return new MatrixBinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeMatrixBinary.MINUS);
                case 2:
                    return new MatrixBinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeMatrixBinary.TIMES);
                default:
                    try {
                        Expression exponent = Expression.build(formulaRight, vars);
                        return new MatrixPower(build(formulaLeft, vars), exponent);
                    } catch (ExpressionException e) {
                        throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_EXPONENT_FORMULA_CANNOT_BE_INTERPRETED"));
                    }
            }
        }

        /*
         Falls kein binärer Operator und die Formel die Form (...) hat ->
         Klammern beseitigen
         */
        if (priority == 4 && formula.substring(0, 1).equals("(") && formula.substring(formulaLength - 1, formulaLength).equals(")")) {
            return build(formula.substring(1, formulaLength - 1), vars);
        }

        /*
         Falls der Ausdruck keine Expression darstellt, dann stellt er
         (höchstens) eine Matrix oder eine Matrixfunktion dar.
         */
        //Falls der Ausdruck eine Matrix ist.
        if (priority == 4 && formula.substring(0, 1).equals("[") && formula.substring(formulaLength - 1, formulaLength).equals("]")) {
            return getMatrix(formula.substring(1, formulaLength - 1), vars);
        }
        //Falls der Ausdruck eine Matrixfunktion ist.
        if (priority == 4) {
            int functionNameLength;
            for (TypeMatrixFunction type : TypeMatrixFunction.values()) {
                functionNameLength = type.toString().length();
                //Falls der Ausdruck die Form function(...) hat -> Funktion und Argument auslesen
                if (formula.length() >= functionNameLength + 2) {
                    if ((formula.substring(0, functionNameLength).equals(type.toString())) && (formula.substring(functionNameLength, functionNameLength + 1).equals("(")) && (formula.substring(formulaLength - 1, formulaLength).equals(")"))) {

                        String formula_interior = formula.substring(functionNameLength + 1, formulaLength - 1);
                        return new MatrixFunction(build(formula_interior, vars), type);

                    }
                }
            }
        }

        //Falls der Ausdruck ein MatrixOperator ist.
        if (priority == 4) {
            String[] operatorNameAndParams = Expression.getOperatorAndArguments(formula);
            String[] params = Expression.getArguments(operatorNameAndParams[1]);
            String operatorName;
            for (TypeMatrixOperator type : TypeMatrixOperator.values()) {
                operatorName = MatrixOperator.getNameFromType(type);
                if (operatorNameAndParams[0].equals(operatorName)) {
                    return MatrixOperator.getOperator(operatorName, params, vars);
                }
            }
        }

        throw new ExpressionException(Translator.translateExceptionMessage("MEB_MatrixExpression_FORMULA_CANNOT_BE_INTERPRETED") + formula);

    }

    //Folgende Funktionen dienen der Kürze halber.
    /**
     * Generiert eine (m x n)-Nullmatrix (m Zeilen, n Spalten).
     */
    public static Matrix getZeroMatrix(int m, int n) {

        // Die Nullmatrix soll mindestens eine Zeile und eine Spalte haben.
        m = Math.max(m, 1);
        n = Math.max(n, 1);
        Expression[][] entry = new Expression[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                entry[i][j] = Expression.ZERO;
            }
        }
        return new Matrix(entry);

    }

    /**
     * Generiert eine (n x n)-Einheitsmatrix.
     */
    public static Matrix getId(int n) {

        // Die Einheitsmatrix soll mindestens eine Zeile und eine Spalte haben.
        n = Math.max(n, 1);

        Expression[][] entry = new Expression[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    entry[i][j] = Expression.ONE;
                } else {
                    entry[i][j] = Expression.ZERO;
                }
            }
        }
        return new Matrix(entry);

    }

    public boolean isZeroMatrix() {

        boolean result = this instanceof Matrix;

        if (!result) {
            return false;
        }

        for (int i = 0; i < ((Matrix) this).getRowNumber(); i++) {
            for (int j = 0; j < ((Matrix) this).getColumnNumber(); j++) {
                result = result && ((Matrix) this).getEntry(i, j).equals(Expression.ZERO);
            }
        }
        return result;

    }

    /**
     * Gibt zurück, ob der vorliegende Matrizenausdruck eine Einheitsmatrix ist.
     */
    public boolean isId() {

        if (!(this instanceof Matrix)) {
            return false;
        }

        if (((Matrix) this).getRowNumber() != ((Matrix) this).getColumnNumber()) {
            return false;
        }

        for (int i = 0; i < ((Matrix) this).getRowNumber(); i++) {
            for (int j = 0; j < ((Matrix) this).getColumnNumber(); j++) {
                if (i == j) {
                    if (!((Matrix) this).getEntry(i, j).equals(Expression.ONE)) {
                        return false;
                    }
                } else {
                    if (!((Matrix) this).getEntry(i, j).equals(Expression.ZERO)) {
                        return false;
                    }
                }
            }
        }
        return true;

    }

    /**
     * Es folgen Methoden zur Ermittlung, ob der zugrundeliegende Ausdruck eine
     * Instanz einer speziellen Unterklasse von Expression mit speziellem Typ
     * ist.
     */
    public boolean isMatrix() {
        return this instanceof Matrix;
    }

    public boolean isSum() {
        return this instanceof MatrixBinaryOperation && ((MatrixBinaryOperation) this).getType().equals(TypeMatrixBinary.PLUS);
    }

    public boolean isDifference() {
        return this instanceof MatrixBinaryOperation && ((MatrixBinaryOperation) this).getType().equals(TypeMatrixBinary.MINUS);
    }

    public boolean isProduct() {
        return this instanceof MatrixBinaryOperation && ((MatrixBinaryOperation) this).getType().equals(TypeMatrixBinary.TIMES);
    }

    public boolean isPower() {
        return this instanceof MatrixPower;
    }

    public boolean isNotMatrix() {
        return !(this instanceof Matrix);
    }

    public boolean isNotSum() {
        return !(this instanceof MatrixBinaryOperation && ((MatrixBinaryOperation) this).getType().equals(TypeMatrixBinary.PLUS));
    }

    public boolean isNotDifference() {
        return !(this instanceof MatrixBinaryOperation && ((MatrixBinaryOperation) this).getType().equals(TypeMatrixBinary.MINUS));
    }

    public boolean isNotProduct() {
        return !(this instanceof MatrixBinaryOperation && ((MatrixBinaryOperation) this).getType().equals(TypeMatrixBinary.TIMES));
    }

    public boolean isNotPower() {
        return !(this instanceof MatrixPower);
    }

    public boolean isMatrixFunction() {
        return this instanceof MatrixFunction;
    }

    public boolean isMatrixFunction(TypeMatrixFunction type) {
        return this instanceof MatrixFunction && ((MatrixFunction) this).getType().equals(type);
    }

    //Addiert zwei Matrizen.
    public MatrixExpression add(MatrixExpression a) {
        return new MatrixBinaryOperation(this, a, TypeMatrixBinary.PLUS);
    }

    //Subtrahiert zwei Matrizen.
    public MatrixExpression sub(MatrixExpression a) {
        return new MatrixBinaryOperation(this, a, TypeMatrixBinary.MINUS);
    }

    //Multipliziert zwei Matrizen.
    public MatrixExpression mult(MatrixExpression a) {
        return new MatrixBinaryOperation(this, a, TypeMatrixBinary.TIMES);
    }

    //Multipliziert eine Matrix mit einem Skalar a von links.
    public MatrixExpression mult(Expression a) {
        return new MatrixBinaryOperation(new Matrix(a), this, TypeMatrixBinary.TIMES);
    }

    //Potenziert eine Matrix.
    public MatrixExpression pow(Expression expr) {
        return new MatrixPower(this, expr);
    }

    //Potenziert eine Matrix.
    public MatrixExpression pow(int n) {
        return new MatrixPower(this, new Constant(n));
    }

    //Potenziert eine Matrix.
    public MatrixExpression pow(BigInteger n) {
        return new MatrixPower(this, new Constant(n));
    }

    public MatrixFunction det() {
        return new MatrixFunction(this, TypeMatrixFunction.det);
    }

    public MatrixFunction exp() {
        return new MatrixFunction(this, TypeMatrixFunction.exp);
    }

    public MatrixFunction ln() {
        return new MatrixFunction(this, TypeMatrixFunction.ln);
    }

    public MatrixFunction tr() {
        return new MatrixFunction(this, TypeMatrixFunction.tr);
    }

    public MatrixFunction trans() {
        return new MatrixFunction(this, TypeMatrixFunction.trans);
    }

    /**
     * Falls der Ausdruck eine 1x1-Matrix ist, so wird daraus eine Erpression
     * erzeugt. Ansonsten wird die MatrixExpression selbst zurückgegeben. Dies
     * ist wichtig für die Ausgabe am Ende einer Vereinfachung.
     */
    public Object convertOneTimesOneMatrixToExpression() {
        if (this instanceof Matrix && ((Matrix) this).getRowNumber() == 1 && ((Matrix) this).getColumnNumber() == 1) {
            return ((Matrix) this).getEntry(0, 0);
        }
        return this;
    }

    /**
     * Gibt die Maße der Ergebnismatrix an, oder wirft eine EvaluationException,
     * wenn die Ergebnismatrix nicht wohldefiniert ist. Wird benötigt, um zu
     * checken, ob gewisse Matrixoperationen wohldefiniert sind,
     *
     * @throws EvaluationException
     */
    public abstract Dimension getDimension() throws EvaluationException;

    /**
     * Führt, falls möglich, die auftretenden algebraischen Operationen +, - und
     * * aus und gibt das Ergebnis als MatrixExpression (und falls möglich, als
     * Matrix) zurück. Falls das Ergebnis nicht wohldefiniert ist, wird eine
     * EvaluationException geworfen.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression computeMatrixOperations() throws EvaluationException;

    /**
     * Gibt zurück, ob this und matExpr gleich sind.
     */
    public abstract boolean equals(MatrixExpression matExpr);

    /**
     * Gibt zurück, ob this und matExpr äquivalent sind.
     */
    public abstract boolean equivalent(MatrixExpression matExpr);

    /**
     * Ordnet Ketten von + und von * nach rechts.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression orderSumsAndProducts() throws EvaluationException;

    /**
     * Sortiert Summen und Differenzen zu einer grpßen Differenz (...)-(...).
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression orderDifferences() throws EvaluationException;

    /**
     * Gibt zurück, ob der Matrizenausdruck konstant ist.
     */
    public abstract boolean isConstant();

    /**
     * Gibt zurück, ob der Matrizenausdruck die Variable var enthält.
     */
    public abstract boolean contains(String var);

    /**
     * Fügt einem HashSet alle im Matrizenausdruck vorkommenden Variablen hinzu.
     */
    public abstract void getContainedVars(HashSet<String> vars);

    /**
     * Legt eine neue Kopie von this an.
     */
    public abstract MatrixExpression copy();

    /**
     * Gibt die Ableitung eines Matrizenausdrucks nach der Variablen var zurück.
     */
    public abstract MatrixExpression diff(String var) throws EvaluationException;

    /**
     * Gibt den Matrizenausdruck zurück, der durch das Ersetzen der Variablen
     * var durch den Ausdruck expr entsteht.
     */
    public abstract MatrixExpression replaceVariable(String var, Expression expr);

    /**
     * Schreibt Matrizenausdrücke aus.
     */
    public abstract String writeMatrixExpression();

    @Override
    public String toString() {
        return this.writeMatrixExpression();
    }

    /**
     * Triviale Vereinfachungen für Matrizenausdrücke.
     */
    public abstract MatrixExpression simplifyTrivial() throws EvaluationException;
    
    /**
     * Hier wird die Methode simplify() aus der Klasse
     * expressionbuilder.Expression auf jeden einzelnen Matrixeintrag
     * angewendet.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression simplifyMatrixEntries() throws EvaluationException;

    /**
     * Hier wird die Methode simplify(simplifyTypes) aus der Klasse
     * expressionbuilder.Expression auf jeden einzelnen Matrixeintrag
     * angewendet.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression simplifyMatrixEntries(HashSet<TypeSimplify> simplifyTypes) throws EvaluationException;

    /**
     * Sammelt in einem Produkt gleiche Ausdrücke zu einem einzigen Ausdruck.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression collectProducts() throws EvaluationException;

    /**
     * Hier wird die Methode simplify() aus der Klasse
     * expressionbuilder.Expression auf jeden einzelnen Matrixeintrag
     * angewendet.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression simplifyMatrixFunctionalRelations() throws EvaluationException;

    /**
     * Standardvereinfachung allgemeiner Matrizenausdrücke. Es wird solange
     * iteriert, bis sich nichts mehr ändert -> Der Ausdruck ist dann
     * weitestgehend vereinfacht.
     *
     * @throws EvaluationException
     */
    public MatrixExpression simplify() throws EvaluationException {

        try {
            MatrixExpression matExpr, matExprSimplified = this;

            do {
                matExpr = matExprSimplified.copy();
                matExprSimplified = matExprSimplified.orderDifferences();
                matExprSimplified = matExprSimplified.orderSumsAndProducts();
                matExprSimplified = matExprSimplified.simplifyTrivial();
                matExprSimplified = matExprSimplified.simplifyMatrixEntries();
                matExprSimplified = matExprSimplified.collectProducts();
                matExprSimplified = matExprSimplified.simplifyMatrixFunctionalRelations();
                matExprSimplified = matExprSimplified.computeMatrixOperations();
            } while (!matExpr.equals(matExprSimplified));

            return matExprSimplified;

        } catch (java.lang.StackOverflowError e) {
            throw new EvaluationException(Translator.translateExceptionMessage("MEB_Expression_STACK_OVERFLOW"));
        }

    }

}
