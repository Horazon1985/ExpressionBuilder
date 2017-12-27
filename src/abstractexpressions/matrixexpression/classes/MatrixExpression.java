package abstractexpressions.matrixexpression.classes;

import enums.TypeSimplify;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.interfaces.AbstractExpression;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.interfaces.IdentifierValidator;
import abstractexpressions.interfaces.IdentifierValidatorExpression;
import abstractexpressions.interfaces.IdentifierValidatorMatrixExpression;
import java.awt.Dimension;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import lang.translator.Translator;
import process.Canceller;
import util.OperationDataTO;
import util.OperationParsingUtils;

public abstract class MatrixExpression implements AbstractExpression {

    private static final String MEB_MatrixExpression_EXPRESSION_EMPTY_OR_INCOMPLETE = "MEB_MatrixExpression_EXPRESSION_EMPTY_OR_INCOMPLETE";
    private static final String MEB_MatrixExpression_ROW_MISSING = "MEB_MatrixExpression_ROW_MISSING";
    private static final String MEB_MatrixExpression_NO_ROWS = "MEB_MatrixExpression_NO_ROWS";
    private static final String MEB_MatrixExpression_EMPTY_PARAMETER = "MEB_MatrixExpression_EMPTY_PARAMETER";
    private static final String MEB_MatrixExpression_WRONG_BRACKETS = "MEB_MatrixExpression_WRONG_BRACKETS";
    private static final String MEB_MatrixExpression_TWO_OPERATIONS = "MEB_MatrixExpression_TWO_OPERATIONS";
    private static final String MEB_MatrixExpression_LEFT_SIDE_OF_MATRIXBINARY_IS_EMPTY = "MEB_MatrixExpression_LEFT_SIDE_OF_MATRIXBINARY_IS_EMPTY";
    private static final String MEB_MatrixExpression_RIGHT_SIDE_OF_MATRIXBINARY_IS_EMPTY = "MEB_MatrixExpression_RIGHT_SIDE_OF_MATRIXBINARY_IS_EMPTY";
    private static final String MEB_MatrixExpression_EXPONENT_FORMULA_CANNOT_BE_INTERPRETED = "MEB_MatrixExpression_EXPONENT_FORMULA_CANNOT_BE_INTERPRETED";
    private static final String MEB_MatrixExpression_FORMULA_CANNOT_BE_INTERPRETED = "MEB_MatrixExpression_FORMULA_CANNOT_BE_INTERPRETED";
    private static final String MEB_MatrixExpression_NOT_A_MATRIX = "MEB_MatrixExpression_NOT_A_MATRIX";
    private static final String MEB_MatrixExpression_STACK_OVERFLOW = "MEB_MatrixExpression_STACK_OVERFLOW";

    public final static Matrix MINUS_ONE = new Matrix(Expression.MINUS_ONE);

    public final static IdentifierValidator VALIDATOR_EXPRESSION = new IdentifierValidatorExpression();
    public final static IdentifierValidator VALIDATOR_MATRIX_EXPRESSION = new IdentifierValidatorMatrixExpression();

    /**
     * Falls matrix eine Matrix darstellt, so werden die einzelnen Matrixzeilen
     * in einem Stringarray zurückgegeben. Andernfalls wird eine
     * ExpressionException geworfen.<br>
     * BEISPIEL: Bei der (2x3)-Matrix matrix = "[x+y,a,b;5,sin(y),f(u,v)]" wird
     * das Stringarray {"x+y,a,b","5,sin(y),f(u,v)"} zurückgegeben.
     *
     * @throws ExpressionException
     */
    private static String[] getRowsFromMatrix(String matrix) throws ExpressionException {

        int numberOfRows = 1;
        for (int i = 0; i < matrix.length(); i++) {
            if (matrix.substring(i, i + 1).equals(";")) {
                numberOfRows++;
            }
        }

        String[] rows = matrix.split(";");
        if (rows.length != numberOfRows) {
            throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_ROW_MISSING));
        }

        if (rows.length == 0) {
            throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_NO_ROWS));
        }
        return rows;

    }

    /**
     * Falls row eine Zeile in einer Matrix darstellt, so werden die einzelnen
     * Matrixeinträge in einem Stringarray zurückgegeben. Andernfalls wird eine
     * ExpressionException geworfen.<br>
     * BEISPIEL: Bei der 2. Zeile row = "5,sin(y),f(u,v)" aus dem obigen
     * Beispiel bei der Beschreibung von getRowsFromMatrix() wird das
     * Stringarray {"5", "sin(y)", "gcd(12,15)"} zurückgegeben.
     *
     * @throws ExpressionException
     */
    private static ArrayList<String> getEntriesFromRow(String row) throws ExpressionException {

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
                    throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_EMPTY_PARAMETER));
                }
                result.add(row.substring(startPositionOfCurrentParameter, i));
                startPositionOfCurrentParameter = i + 1;
            }
            if (i == row.length() - 1) {
                if (startPositionOfCurrentParameter == row.length()) {
                    throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_EMPTY_PARAMETER));
                }
                result.add(row.substring(startPositionOfCurrentParameter, row.length()));
            }

        }

        if (bracketsCount != 0) {
            throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_WRONG_BRACKETS));
        }

        return result;

    }

    /**
     * Erzeugt eine Matrix aus formula.
     *
     * @throws ExpressionException
     */
    public static Matrix getMatrix(String formula, Set<String> vars, IdentifierValidator validatorExpression) throws ExpressionException {

        String[] rows = getRowsFromMatrix(formula);
        ArrayList<String> currentRowList = getEntriesFromRow(rows[0]);
        String[] currentRow = currentRowList.toArray(new String[currentRowList.size()]);
        Expression[][] entry = new Expression[rows.length][currentRow.length];

        for (int i = 0; i < rows.length; i++) {

            if (rows[i].isEmpty()) {
                throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_NOT_A_MATRIX));
            }
            currentRow = getEntriesFromRow(rows[i]).toArray(new String[currentRowList.size()]);
            if (currentRow.length != entry[0].length) {
                throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_NOT_A_MATRIX));
            }
            for (int j = 0; j < currentRow.length; j++) {
                entry[i][j] = Expression.build(currentRow[j], vars, validatorExpression);
            }

        }

        return new Matrix(entry);

    }

    private static boolean isOperation(String formula) {
        return formula.equals("+") || formula.equals("-") || formula.equals("*") || formula.equals("/") || formula.equals("^");
    }

    /**
     * Hauptmethode zum Erstellen einer MatrixExpression aus einem String.
     *
     * @throws ExpressionException
     */
    public static MatrixExpression build(String formula) throws ExpressionException {
        return build(formula, null);
    }

    /**
     * Hauptmethode zum Erstellen einer MatrixExpression aus einem String.
     *
     * @throws ExpressionException
     */
    public static MatrixExpression build(String formula, IdentifierValidator validatorExpression, IdentifierValidator validatorMatrixExpression)
            throws ExpressionException {
        return build(formula, null, validatorExpression, validatorMatrixExpression);
    }

    /**
     * Hauptmethode zum Erstellen einer MatrixExpression aus einem String.
     *
     * @throws ExpressionException
     */
    public static MatrixExpression build(String formula, Set<String> vars) throws ExpressionException {
        return build(formula, vars, VALIDATOR_EXPRESSION, VALIDATOR_MATRIX_EXPRESSION);
    }

    /**
     * Hauptmethode zum Erstellen einer MatrixExpression aus einem String.
     *
     * @throws ExpressionException
     */
    public static MatrixExpression build(String formula, Set<String> vars,
            IdentifierValidator validatorExpression, IdentifierValidator validatorMatrixExpression) throws ExpressionException {

        formula = formula.replaceAll(" ", "").toLowerCase();

        // Prioritäten: + = 0, - = 1, * = 2, ^ = 3, 4 = Matrix, Matrixfunktion, MatrixOperator.
        int priority = 4;
        int breakpoint = -1;
        int bracketCounter = 0;
        int squareBracketCounter = 0;
        int formulaLength = formula.length();
        String currentChar;

        if (formula.isEmpty()) {
            throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_EXPRESSION_EMPTY_OR_INCOMPLETE));
        }

        // Prüfen, ob nicht zwei Operatoren nacheinander auftreten.
        for (int i = 0; i < formulaLength - 1; i++) {
            if (isOperation(formula.substring(i, i + 1)) && isOperation(formula.substring(i + 1, i + 2))) {
                throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_TWO_OPERATIONS));
            }
        }

        for (int i = 1; i <= formulaLength; i++) {
            currentChar = formula.substring(formulaLength - i, formulaLength - i + 1);

            // Öffnende und schließende Klammern zählen.
            if (currentChar.equals("(") && bracketCounter == 0) {
                throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_WRONG_BRACKETS));
            }
            if (currentChar.equals("[") && squareBracketCounter == 0) {
                throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_WRONG_BRACKETS));
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
            throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_WRONG_BRACKETS));
        }

        // Aufteilung, falls eine Elementaroperation (-, +, *, ^) vorliegt
        if (priority <= 3) {
            String formulaLeft = formula.substring(0, breakpoint);
            String formulaRight = formula.substring(breakpoint + 1, formulaLength);

            if (formulaLeft.isEmpty() && priority > 1) {
                throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_LEFT_SIDE_OF_MATRIXBINARY_IS_EMPTY));
            }
            if (formulaRight.isEmpty()) {
                throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_RIGHT_SIDE_OF_MATRIXBINARY_IS_EMPTY));
            }

            //Falls der Ausdruck die Form "+A..." besitzt -> daraus "A..." machen
            if (formulaLeft.isEmpty() && priority == 0) {
                return build(formulaRight, vars, validatorExpression, validatorMatrixExpression);
            }
            //Falls der Ausdruck die Form "-A..." besitzt -> daraus "(-1)*A..." machen
            if (formulaLeft.isEmpty() && priority == 1) {
                return MINUS_ONE.mult(build(formulaRight, vars, validatorExpression, validatorMatrixExpression));
            }
            
            MatrixExpression resultLeft = build(formulaLeft, vars, validatorExpression, validatorMatrixExpression);

            MatrixExpression resultRight, result;
            switch (priority) {
                case 0:
                    resultRight = build(formulaRight, vars, validatorExpression, validatorMatrixExpression);
                    result = new MatrixBinaryOperation(resultLeft, resultRight, TypeMatrixBinary.PLUS);
                    // Prüfung, ob die Dimension wohldefiniert ist.
                    result.getMatrixExpressionDimension();
                    return result;
                case 1:
                    resultRight = build(formulaRight, vars, validatorExpression, validatorMatrixExpression);
                    result = new MatrixBinaryOperation(resultLeft, resultRight, TypeMatrixBinary.MINUS);
                    // Prüfung, ob die Dimension wohldefiniert ist.
                    result.getMatrixExpressionDimension();
                    return result;
                case 2:
                    resultRight = build(formulaRight, vars, validatorExpression, validatorMatrixExpression);
                    result = new MatrixBinaryOperation(resultLeft, resultRight, TypeMatrixBinary.TIMES);
                    // Prüfung, ob die Dimension wohldefiniert ist.
                    result.getMatrixExpressionDimension();
                    return result;
                default:
                    Expression exponent;
                    try {
                        exponent = Expression.build(formulaRight, vars, validatorExpression);
                    } catch (ExpressionException e) {
                        throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_EXPONENT_FORMULA_CANNOT_BE_INTERPRETED, formulaRight));
                    }
                    result = new MatrixPower(resultLeft, exponent);
                    // Prüfung, ob die Dimension wohldefiniert ist.
                    result.getMatrixExpressionDimension();
                    return result;
            }
        }

        /*
         Falls kein binärer Operator und die Formel die Form (...) hat ->
         Klammern beseitigen
         */
        if (priority == 4 && formula.substring(0, 1).equals("(") && formula.substring(formulaLength - 1, formulaLength).equals(")")) {
            return build(formula.substring(1, formulaLength - 1), vars, validatorExpression, validatorMatrixExpression);
        }

        /*
         Falls der Ausdruck keine MatrixBinaryOperation darstellt, dann stellt er
         (höchstens) eine Matrix, eine Matrixvariable, eine Matrixfunktion oder einen Matrizenoperator dar.
         */
        // Falls der Ausdruck eine Matrix ist.
        if (priority == 4 && formula.substring(0, 1).equals("[") && formula.substring(formulaLength - 1, formulaLength).equals("]")) {
            return getMatrix(formula.substring(1, formulaLength - 1), vars, validatorExpression);
        }
        // Falls der Ausdruck eine Matrixvariable ist.
        if (priority == 4) {
            if (validatorMatrixExpression.isValidIdentifierOfRequiredType(formula, MatrixExpression.class)) {
                if (vars != null) {
                    vars.add(formula);
                }
                return MatrixVariable.create(formula);
            }
        }

        // Falls der Ausdruck eine Matrixfunktion ist.
        if (priority == 4) {
            int functionNameLength;
            for (TypeMatrixFunction type : TypeMatrixFunction.values()) {
                functionNameLength = type.toString().length();
                //Falls der Ausdruck die Form function(...) hat -> Funktion und Argument auslesen
                if (formula.length() >= functionNameLength + 2) {
                    if ((formula.substring(0, functionNameLength).equals(type.toString())) && (formula.substring(functionNameLength, functionNameLength + 1).equals("(")) && (formula.substring(formulaLength - 1, formulaLength).equals(")"))) {

                        String formulaArgument = formula.substring(functionNameLength + 1, formulaLength - 1);
                        return new MatrixFunction(build(formulaArgument, vars, validatorExpression, validatorMatrixExpression), type);

                    }
                }
            }
        }

        /*
         Versuch: versuchen, formula zu einer Instanz von Expression
         zu kompilieren. Falls dies klappt, soll diese zu einer (1x1)-Matrix
         konvertiert und zurückgegeben werden.
         */
        try {
            return new Matrix(Expression.build(formula, vars, validatorExpression));
        } catch (ExpressionException e) {
        }

        // Falls der Ausdruck ein MatrixOperator ist.
        if (priority == 4) {
            OperationDataTO opData = OperationParsingUtils.getOperationData(formula);
            String opName = opData.getOperationName();
            String[] params = opData.getOperationArguments();
            String operatorName;
            for (TypeMatrixOperator type : TypeMatrixOperator.values()) {
                operatorName = MatrixOperator.getNameFromType(type);
                if (opName.equals(operatorName)) {
                    return MatrixOperator.getMatrixOperator(operatorName, params, vars);
                }
            }
        }

        throw new ExpressionException(Translator.translateOutputMessage(MEB_MatrixExpression_FORMULA_CANNOT_BE_INTERPRETED, formula));

    }

    /////////////// Folgende Funktionen dienen der Kürze halber /////////////////////
    /**
     * Gibt die (m x n)-Nullmatrix (m Zeilen, n Spalten) zurück.
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
     * Gibt die (n x n)-Einheitsmatrix zurück.
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

    /**
     * Gibt den i-ten Einheitsvektor im R<sup>n</sup> zurück.
     */
    public static Matrix getUnitVector(int i, int n) {

        if (i < 0) {
            i = 0;
        } else if (i >= n) {
            i = n - 1;
        }

        Expression[] entry = new Expression[n];

        for (int j = 0; j < n; j++) {
            if (j == i) {
                entry[j] = ONE;
            } else {
                entry[j] = ZERO;
            }
        }

        return new Matrix(entry);

    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck eine Nullmatrix ist.
     */
    public boolean isZeroMatrix() {

        if (!(this instanceof Matrix)) {
            return false;
        }

        for (int i = 0; i < ((Matrix) this).getRowNumber(); i++) {
            for (int j = 0; j < ((Matrix) this).getColumnNumber(); j++) {
                if (!((Matrix) this).getEntry(i, j).equals(Expression.ZERO)) {
                    return false;
                }
            }
        }
        return true;

    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck eine Einheitsmatrix ist.
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
                } else if (!((Matrix) this).getEntry(i, j).equals(Expression.ZERO)) {
                    return false;
                }
            }
        }
        return true;

    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck ein Vielfaches einer
     * Einheitsmatrix ist.
     */
    public boolean isMultipleOfId() {

        if (!(this instanceof Matrix && ((Matrix) this).isDiagonalMatrix())) {
            return false;
        }

        for (int i = 0; i < ((Matrix) this).getRowNumber(); i++) {
            if (!((Matrix) this).getEntry(0, 0).equivalent(((Matrix) this).getEntry(i, i))) {
                return false;
            }
        }
        return true;

    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck eine Matrix ist.
     */
    public boolean isMatrix() {
        return this instanceof Matrix;
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck eine Matrix ist.
     */
    public boolean isMatrix(int m, int n) {
        return this instanceof Matrix && ((Matrix) this).getDimension().equals(new Dimension(n, m));
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck eine Summe ist.
     */
    public boolean isSum() {
        return this instanceof MatrixBinaryOperation && ((MatrixBinaryOperation) this).getType().equals(TypeMatrixBinary.PLUS);
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck eine Differenz ist.
     */
    public boolean isDifference() {
        return this instanceof MatrixBinaryOperation && ((MatrixBinaryOperation) this).getType().equals(TypeMatrixBinary.MINUS);
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck ein Produkt ist.
     */
    public boolean isProduct() {
        return this instanceof MatrixBinaryOperation && ((MatrixBinaryOperation) this).getType().equals(TypeMatrixBinary.TIMES);
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck eine Potenz ist.
     */
    public boolean isPower() {
        return this instanceof MatrixPower;
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck eine ganzzahlige Potenz
     * ist.
     */
    public boolean isIntegerPower() {
        return this instanceof MatrixPower && ((MatrixPower) this).getRight().isIntegerConstant();
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck eine ungerade Potenz ist.
     */
    public boolean isOddIntegerPower() {
        return this instanceof MatrixPower && ((MatrixPower) this).getRight().isOddIntegerConstant();
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck eine gerade Potenz ist.
     */
    public boolean isEvenIntegerPower() {
        return this instanceof MatrixPower && ((MatrixPower) this).getRight().isEvenIntegerConstant();
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck keine Matrix ist.
     */
    public boolean isNotMatrix() {
        return !(this instanceof Matrix);
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck keine Summe ist.
     */
    public boolean isNotSum() {
        return !(this instanceof MatrixBinaryOperation && ((MatrixBinaryOperation) this).getType().equals(TypeMatrixBinary.PLUS));
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck keine Differenz ist.
     */
    public boolean isNotDifference() {
        return !(this instanceof MatrixBinaryOperation && ((MatrixBinaryOperation) this).getType().equals(TypeMatrixBinary.MINUS));
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck kein Produkt ist.
     */
    public boolean isNotProduct() {
        return !(this instanceof MatrixBinaryOperation && ((MatrixBinaryOperation) this).getType().equals(TypeMatrixBinary.TIMES));
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck keine Potenz ist.
     */
    public boolean isNotPower() {
        return !(this instanceof MatrixPower);
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck eine Matrizenfunktion ist.
     */
    public boolean isMatrixFunction() {
        return this instanceof MatrixFunction;
    }

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck eine Matrizenfunktion vom
     * Typ type ist.
     */
    public boolean isMatrixFunction(TypeMatrixFunction type) {
        return this instanceof MatrixFunction && ((MatrixFunction) this).getType().equals(type);
    }

    /**
     * Addiert den gegebenen Matrizenausdruck zu matExpr.
     */
    public MatrixExpression add(MatrixExpression matExpr) {
        return new MatrixBinaryOperation(this, matExpr, TypeMatrixBinary.PLUS);
    }

    /**
     * Subztrahiert matExpr vom gegebenen Matrizenausdruck.
     */
    public MatrixExpression sub(MatrixExpression matExpr) {
        return new MatrixBinaryOperation(this, matExpr, TypeMatrixBinary.MINUS);
    }

    /**
     * Multipliziert den gegebenen Matrizenausdruck mit matExpr von rechts.
     */
    public MatrixExpression mult(MatrixExpression matExpr) {
        return new MatrixBinaryOperation(this, matExpr, TypeMatrixBinary.TIMES);
    }

    /**
     * Multipliziert den gegebenen Matrizenausdruck mit dem Skalar a von links.
     */
    public MatrixExpression mult(Expression a) {
        if (a.equals(ONE)) {
            return this;
        }
        return new MatrixBinaryOperation(new Matrix(a), this, TypeMatrixBinary.TIMES);
    }

    /**
     * Potenziert den gegebenen Matrizenausdruck in die Potenz a.
     */
    public MatrixExpression pow(Expression expr) {
        if (expr.equals(ONE)) {
            return this;
        }
        return new MatrixPower(this, expr);
    }

    /**
     * Potenziert den gegebenen Matrizenausdruck in die Potenz n.
     */
    public MatrixExpression pow(int n) {
        if (n == 1) {
            return this;
        }
        return new MatrixPower(this, new Constant(n));
    }

    /**
     * Potenziert den gegebenen Matrizenausdruck in die Potenz n.
     */
    public MatrixExpression pow(BigInteger n) {
        if (n.equals(BigInteger.ONE)) {
            return this;
        }
        return new MatrixPower(this, new Constant(n));
    }

    /**
     * Gibt die Determinante des Matrizenausdrucks zurück.
     */
    public MatrixFunction det() {
        return new MatrixFunction(this, TypeMatrixFunction.det);
    }

    /**
     * Gibt den Kosinus des Matrizenausdrucks zurück.
     */
    public MatrixFunction cos() {
        return new MatrixFunction(this, TypeMatrixFunction.cos);
    }

    /**
     * Gibt den hyperbolischen Kosinus des Matrizenausdrucks zurück.
     */
    public MatrixFunction cosh() {
        return new MatrixFunction(this, TypeMatrixFunction.cosh);
    }

    /**
     * Gibt die Exponentialfunktion des Matrizenausdrucks zurück.
     */
    public MatrixFunction exp() {
        return new MatrixFunction(this, TypeMatrixFunction.exp);
    }

    /**
     * Gibt den natürlichen Logarithmus des Matrizenausdrucks zurück.
     */
    public MatrixFunction ln() {
        return new MatrixFunction(this, TypeMatrixFunction.ln);
    }

    /**
     * Gibt den Sinus des Matrizenausdrucks zurück.
     */
    public MatrixFunction sin() {
        return new MatrixFunction(this, TypeMatrixFunction.sin);
    }

    /**
     * Gibt den hyperbolischen Sinus des Matrizenausdrucks zurück.
     */
    public MatrixFunction sinh() {
        return new MatrixFunction(this, TypeMatrixFunction.sinh);
    }

    /**
     * Gibt die Spur des Matrizenausdrucks zurück.
     */
    public MatrixFunction tr() {
        return new MatrixFunction(this, TypeMatrixFunction.tr);
    }

    /**
     * Gibt das Transponierte des Matrizenausdrucks zurück.
     */
    public MatrixFunction trans() {
        return new MatrixFunction(this, TypeMatrixFunction.trans);
    }

    /**
     * Falls der Ausdruck eine 1x1-Matrix ist, so wird daraus eine Erpression
     * erzeugt. Ansonsten wird der gegebene Matrizenausdruck wieder
     * zurückgegeben.
     */
    public Object convertOneTimesOneMatrixToExpression() {
        if (this instanceof Matrix && ((Matrix) this).getRowNumber() == 1 && ((Matrix) this).getColumnNumber() == 1) {
            return ((Matrix) this).getEntry(0, 0);
        }
        return this;
    }

    /**
     * Gibt die Maße der Ergebnismatrix an, oder wirft eine EvaluationException,
     * wenn die Ergebnismatrix nicht wohldefiniert ist.
     *
     * @throws EvaluationException
     */
    public abstract Dimension getDimension() throws EvaluationException;

    /**
     * Gibt die Maße der Ergebnismatrix an, oder wirft eine ExpressionException,
     * wenn die Ergebnismatrix nicht wohldefiniert ist.
     *
     * @throws ExpressionException
     */
    private Dimension getMatrixExpressionDimension() throws ExpressionException {
        try {
            return getDimension();
        } catch (EvaluationException e) {
            throw new ExpressionException(e.getMessage());
        }
    }

    /**
     * Führt, falls möglich, die auftretenden algebraischen Operationen +, - und
     * * aus und gibt das Ergebnis als MatrixExpression (und falls möglich, als
     * Matrix) zurück.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression simplifyComputeMatrixOperations() throws EvaluationException;

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck und matExpr gleich sind.
     */
    public abstract boolean equals(MatrixExpression matExpr);

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck und matExpr äquivalent
     * sind.
     */
    public abstract boolean equivalent(MatrixExpression matExpr);

    /**
     * Gibt den vereinfachten Matrizenausdruck zurück, wobei bei der
     * Vereinfachung Ketten von + und von * nach rechts geordnet werden.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression orderSumsAndProducts() throws EvaluationException;

    /**
     * Gibt den vereinfachten Matrizenausdruck zurück, wobei bei der
     * Vereinfachung verschachtelte Differenzen zu einer Differenz umgewandelt
     * werden.
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
    @Override
    public abstract boolean contains(String var);

    /**
     * Fügt alle Variablen, die in dem gegebenen Matrizenausdruck vorkommen, zum
     * HashSet vars hinzu.
     */
    @Override
    public abstract void addContainedVars(Set<String> vars);

    /**
     * Gibt ein Set mit allen Variablen, die in dem gegebenen
     * Matrizenausdruck vorkommen, zurück.
     */
    @Override
    public Set<String> getContainedVars() {
        HashSet<String> vars = new HashSet<>();
        addContainedVars(vars);
        return vars;
    }

    /**
     * Fügt alle Variablen, denen kein Wert zugewiesen wurde und die in dem
     * gegebenen Matrizenausdruck vorkommen, zum HashSet vars hinzu.
     */
    @Override
    public abstract void addContainedIndeterminates(Set<String> vars);

    /**
     * Gibt ein Set mit allen Variablen, denen kein Wert zugewiesen wurde
     * und die in dem gegebenen Matrizenausdruck vorkommen, zurück.
     */
    @Override
    public Set<String> getContainedIndeterminates() {
        HashSet<String> vars = new HashSet<>();
        addContainedIndeterminates(vars);
        return vars;
    }

    public Set<String> getContainedExpressionVars() {
        Set<String> allVars = getContainedVars();
        Set<String> exprVars = new HashSet<>();
        for (String var : allVars) {
            if (Variable.doesVariableAlreadyExist(var)) {
                exprVars.add(var);
            }
        }
        return exprVars;
    }

    public Set<String> getContainedExpressionIndeterminates() {
        Set<String> allVars = getContainedIndeterminates();
        Set<String> exprVars = new HashSet<>();
        for (String var : allVars) {
            if (Variable.doesVariableAlreadyExist(var)) {
                exprVars.add(var);
            }
        }
        return exprVars;
    }

    public Set<String> getContainedMatrixVars() {
        Set<String> allVars = getContainedVars();
        Set<String> matrixVars = new HashSet<>();
        for (String var : allVars) {
            if (MatrixVariable.doesMatrixVariableAlreadyExist(var)) {
                matrixVars.add(var);
            }
        }
        return matrixVars;
    }

    public Set<String> getContainedMatrixIndeterminates() {
        Set<String> allVars = getContainedIndeterminates();
        Set<String> exprVars = new HashSet<>();
        for (String var : allVars) {
            if (MatrixVariable.doesMatrixVariableAlreadyExist(var)) {
                exprVars.add(var);
            }
        }
        return exprVars;
    }

    /**
     * Setzt alle im gegebenen Matrizenausdruck vorkommenden Konstanten auf
     * 'approximativ' (precise = false).
     */
    public abstract MatrixExpression turnToApproximate();

    /**
     * Setzt alle im gegebenen Matrizenausdruck vorkommenden Konstanten auf
     * 'exakt' (precise = true).
     */
    public abstract MatrixExpression turnToPrecise();

    /**
     * Gibt zurück, ob der gegebene Matrizenausdruck nichtexakte Konstanten
     * enthält.
     */
    public abstract boolean containsApproximates();

    /**
     * Legt eine neue Kopie vom gegebenen Matrizenausdruck an.
     */
    public abstract MatrixExpression copy();

    /**
     * Liefert den Wert des gegebenen Ausdrucks unter Einsetzung aller
     * Variablenwerte.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression evaluate() throws EvaluationException;

    /**
     * Gibt die Ableitung des gegebenen Matrizenausdrucks nach der Variablen var
     * zurück.
     */
    public abstract MatrixExpression diff(String var) throws EvaluationException;

    /**
     * Gibt den Matrizenausdruck zurück, der durch das Ersetzen der Variablen
     * var durch den Ausdruck expr entsteht.
     */
    public abstract MatrixExpression replaceVariable(String var, Expression expr);

    /**
     * Gibt den Matrizenausdruck zurück, der durch das Ersetzen der
     * Matrizenvariablen var durch den Matrizenausdruck matExpr entsteht.
     */
    public abstract MatrixExpression replaceMatrixVariable(String var, MatrixExpression matExpr);

    /**
     * Führt triviale Vereinfachungen am gegebenen Matrizenausdruck durch und
     * gibt den vereinfachten Ausdruck zurück.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression simplifyBasic() throws EvaluationException;

    /**
     * Liefert einen Matrizenausdruck, bei dem für alle Variablen ihre
     * zugehörigen Werte eingesetzt werden, falls diesen Werte zugeordnet
     * wurden. Die restlichen Variablen werden als Unbestimmte gelassen.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression simplifyByInsertingDefinedVars() throws EvaluationException;

    /**
     * Liefert einen Ausdruck, bei dem die Matrizeneinträge aus dem gegebenen
     * Matrizenausdruck durch das Anwenden der Standardvereinfachung simplify()
     * entstehen.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression simplifyMatrixEntries() throws EvaluationException;

    /**
     * Liefert einen Ausdruck, bei dem die Matrizeneinträge aus dem gegebenen
     * Matrizenausdruck durch das Anwenden der mittels simplifyTypes definierten
     * Vereinfachung entstehen.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression simplifyMatrixEntries(Set<TypeSimplify> simplifyTypes) throws EvaluationException;

    /**
     * Gibt den vereinfachten Matrizenausdruck zurück, wobei bei der
     * Vereinfachung in Produkten gleiche aufeinanderfolgende Faktoren (oder
     * Potenzen mit gleicher Basis), zu einem einzigen Faktor (bzw. Potenz)
     * zusammengefasst werden.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression simplifyCollectProducts() throws EvaluationException;

    /**
     * Gibt den vereinfachten Matrizenausdruck zurück, wobei bei der
     * Vereinfachung in Summen oder Differenzen Skalarfaktoren faktorisiert
     * werden.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression simplifyFactorizeScalars() throws EvaluationException;

    /**
     * Gibt den vereinfachten Matrizenausdruck zurück, wobei bei der
     * Vereinfachung in Summen oder Differenzen faktorisiert wird.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression simplifyFactorize() throws EvaluationException;

    /**
     * Gibt den vereinfachten Matrizenausdruck zurück, wobei bei der
     * Vereinfachung eine Reihe vorgegebener Funktionalgleichungen verwendet
     * wird.
     *
     * @throws EvaluationException
     */
    public abstract MatrixExpression simplifyMatrixFunctionalRelations() throws EvaluationException;

    /**
     * Gibt den Matrizenausdruck zurück, welcher durch 'Standardvereinfachung'
     * des gegebenen Matrizenausdrucks entsteht.
     *
     * @throws EvaluationException
     */
    public MatrixExpression simplify() throws EvaluationException {

        try {
            MatrixExpression matExpr, matExprSimplified = this;

            do {
                matExpr = matExprSimplified.copy();
                matExprSimplified = matExprSimplified.orderDifferences();
                Canceller.interruptComputationIfNeeded();
                matExprSimplified = matExprSimplified.orderSumsAndProducts();
                Canceller.interruptComputationIfNeeded();
                matExprSimplified = matExprSimplified.simplifyBasic();
                Canceller.interruptComputationIfNeeded();
                matExprSimplified = matExprSimplified.simplifyByInsertingDefinedVars();
                Canceller.interruptComputationIfNeeded();
                matExprSimplified = matExprSimplified.simplifyMatrixEntries();
                Canceller.interruptComputationIfNeeded();
                matExprSimplified = matExprSimplified.simplifyCollectProducts();
                Canceller.interruptComputationIfNeeded();
                matExprSimplified = matExprSimplified.simplifyFactorizeScalars();
                Canceller.interruptComputationIfNeeded();
                matExprSimplified = matExprSimplified.simplifyFactorize();
                Canceller.interruptComputationIfNeeded();
                matExprSimplified = matExprSimplified.simplifyMatrixFunctionalRelations();
                Canceller.interruptComputationIfNeeded();
                matExprSimplified = matExprSimplified.simplifyComputeMatrixOperations();
                Canceller.interruptComputationIfNeeded();
            } while (!matExpr.equals(matExprSimplified));

            return matExprSimplified;

        } catch (java.lang.StackOverflowError e) {
            throw new EvaluationException(Translator.translateOutputMessage(MEB_MatrixExpression_STACK_OVERFLOW));
        }

    }

    /**
     * Gibt den Matrizenausdruck zurück, welcher durch die mittels simplifyTypes
     * definierten Vereinfachung des gegebenen Matrizenausdrucks entsteht.
     *
     * @throws EvaluationException
     */
    public MatrixExpression simplify(Set<TypeSimplify> simplifyTypes) throws EvaluationException {

        try {
            MatrixExpression matExpr, matExprSimplified = this;
            do {
                matExpr = matExprSimplified.copy();
                if (simplifyTypes.contains(TypeSimplify.order_difference)) {
                    matExprSimplified = matExprSimplified.orderDifferences();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.order_sums_and_products)) {
                    matExprSimplified = matExprSimplified.orderSumsAndProducts();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_basic)) {
                    matExprSimplified = matExprSimplified.simplifyBasic();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_by_inserting_defined_vars)) {
                    matExprSimplified = matExprSimplified.simplifyByInsertingDefinedVars();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_matrix_entries)) {
                    matExprSimplified = matExprSimplified.simplifyMatrixEntries(simplifyTypes);
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_collect_products)) {
                    matExprSimplified = matExprSimplified.simplifyCollectProducts();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_factorize_scalars)) {
                    matExprSimplified = matExprSimplified.simplifyFactorizeScalars();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_factorize)) {
                    matExprSimplified = matExprSimplified.simplifyFactorize();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_matrix_functional_relations)) {
                    matExprSimplified = matExprSimplified.simplifyMatrixFunctionalRelations();
                    Canceller.interruptComputationIfNeeded();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_compute_matrix_operations)) {
                    matExprSimplified = matExprSimplified.simplifyComputeMatrixOperations();
                    Canceller.interruptComputationIfNeeded();
                }
            } while (!matExpr.equals(matExprSimplified));

            return matExprSimplified;

        } catch (java.lang.StackOverflowError e) {
            throw new EvaluationException(Translator.translateOutputMessage(MEB_MatrixExpression_STACK_OVERFLOW));
        }

    }

}
