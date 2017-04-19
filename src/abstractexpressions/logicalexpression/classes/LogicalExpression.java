package abstractexpressions.logicalexpression.classes;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.interfaces.AbstractExpression;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import lang.translator.Translator;
import process.Canceller;

public abstract class LogicalExpression implements AbstractExpression {

    private static final String LEB_LogicalExpression_EXPRESSION_EMPTY_OR_INCOMPLETE = "LEB_LogicalExpression_EXPRESSION_EMPTY_OR_INCOMPLETE";    
    private static final String LEB_LogicalExpression_WRONG_BRACKETS = "LEB_LogicalExpression_WRONG_BRACKETS";    
    private static final String LEB_LogicalExpression_LEFT_SIDE_OF_LOGICAL_BINARY_IS_EMPTY = "LEB_LogicalExpression_LEFT_SIDE_OF_LOGICAL_BINARY_IS_EMPTY";    
    private static final String LEB_LogicalExpression_RIGHT_SIDE_OF_LOGICAL_BINARY_IS_EMPTY = "LEB_LogicalExpression_RIGHT_SIDE_OF_LOGICAL_BINARY_IS_EMPTY";    
    private static final String LEB_LogicalExpression_LOGICAL_EXPRESSION_CANNOT_BE_INTERPRETED = "LEB_LogicalExpression_LOGICAL_EXPRESSION_CANNOT_BE_INTERPRETED";    
    private static final String LEB_LogicalExpression_STACK_OVERFLOW = "LEB_LogicalExpression_STACK_OVERFLOW";    
    
    public final static LogicalConstant FALSE = new LogicalConstant(false);
    public final static LogicalConstant TRUE = new LogicalConstant(true);

    /**
     * Erstellt aus einem String einen logischen Ausdruck.
     *
     * @throws ExpressionException
     */
    public static LogicalExpression build(String formula) throws ExpressionException {
        return build(formula, null);
    }
    
    /**
     * Erstellt aus einem String einen logischen Ausdruck.
     *
     * @throws ExpressionException
     */
    public static LogicalExpression build(String formula, HashSet<String> vars) throws ExpressionException {

        formula = formula.replaceAll(" ", "").toLowerCase();

        /*
         Prioritäten: = = 0, > = 1, | = 2, & = 3, ! = 4 Boolsche Konstante,
         Boolsche Variable = 5.
         */
        int priority = 5;
        int breakpoint = -1;
        int bracketCounter = 0;
        int formulaLength = formula.length();
        String currentChar;

        if (formula.equals("")) {
            throw new ExpressionException(Translator.translateOutputMessage(LEB_LogicalExpression_EXPRESSION_EMPTY_OR_INCOMPLETE));
        }

        for (int i = 1; i <= formulaLength; i++) {
            currentChar = formula.substring(formulaLength - i, formulaLength - i + 1);

            // Öffnende und schließende Klammern zählen.
            if (currentChar.equals("(") && bracketCounter == 0) {
                throw new ExpressionException(Translator.translateOutputMessage(LEB_LogicalExpression_WRONG_BRACKETS));
            }

            if (currentChar.equals(")")) {
                bracketCounter++;
            }
            if (currentChar.equals("(")) {
                bracketCounter--;
            }

            if (bracketCounter != 0) {
                continue;
            }
            //Aufteilungspunkt finden; zunächst wird nach =, >, |, &, ! gesucht 
            //breakpoint gibt den Index in formula an, wo die Formel aufgespalten werden soll.
            if (currentChar.equals("=") && priority > 0) {
                priority = 0;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals(">") && priority > 1) {
                priority = 1;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("|") && priority > 2) {
                priority = 2;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("&") && priority > 3) {
                priority = 3;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("!") && priority >= 4) {
                /*
                 Falls Vorher bereits eine NEgation aufgetaucht ist, dann soll
                 man sich diejenige merken, die sich weiter links befindet.
                 Ansonsten gibt es Probleme bei "!!a".
                 */
                priority = 4;
                breakpoint = formulaLength - i;
            }
        }

        if (bracketCounter > 0) {
            throw new ExpressionException(Translator.translateOutputMessage(LEB_LogicalExpression_WRONG_BRACKETS));
        }

        // Aufteilung, falls eine Elementaroperation (=, >, |, &) vorliegt
        if (priority <= 3) {
            String formulaLeft = formula.substring(0, breakpoint);
            String formulaRight = formula.substring(breakpoint + 1, formulaLength);

            if ((formulaLeft.equals("")) && (priority != 1)) {
                throw new ExpressionException(Translator.translateOutputMessage(LEB_LogicalExpression_LEFT_SIDE_OF_LOGICAL_BINARY_IS_EMPTY));
            }
            if (formulaRight.equals("")) {
                throw new ExpressionException(Translator.translateOutputMessage(LEB_LogicalExpression_RIGHT_SIDE_OF_LOGICAL_BINARY_IS_EMPTY));
            }

            switch (priority) {
                case 0:
                    return new LogicalBinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeLogicalBinary.EQUIVALENCE);
                case 1:
                    return new LogicalBinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeLogicalBinary.IMPLICATION);
                case 2:
                    return new LogicalBinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeLogicalBinary.OR);
                case 3:
                    return new LogicalBinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeLogicalBinary.AND);
                default:    //Passiert zwar nicht, aber trotzdem!
                    return null;
            }
        }
        if (priority == 4 && breakpoint == 0) {
            /*
             Falls eine Negation vorliegt, dann muss breakpoint == 0 sein.
             Falls formula von der Form !xyz... ist, dann soll xyz... gelesen
             werden und dann die entsprechende unäre Operation zurückgegeben
             werden.
             */
            String formulaLeft = formula.substring(1, formulaLength);
            return new LogicalUnaryOperation(build(formulaLeft, vars), TypeLogicalUnary.NEGATION);
        }

        //Falls kein binärer Operator und die Formel die Form (...) hat -> Klammern beseitigen
        if ((priority == 5) && (formula.substring(0, 1).equals("(")) && (formula.substring(formulaLength - 1, formulaLength).equals(")"))) {
            return build(formula.substring(1, formulaLength - 1), vars);
        }

        //Falls der Ausdruck eine ganze Zahl ist (0 = false, 1 = true)
        if (priority == 5) {
            if (formula.equals("0")) {
                return new LogicalConstant(false);
            }
            if (formula.equals("1")) {
                return new LogicalConstant(true);
            }
        }

        //Falls der Ausdruck eine Variable ist
        if (priority == 5) {
            if (isValidVariable(formula)) {
                if (vars != null) {
                    vars.add(formula);
                }
                return LogicalVariable.create(formula);
            }
        }

        throw new ExpressionException(Translator.translateOutputMessage(LEB_LogicalExpression_LOGICAL_EXPRESSION_CANNOT_BE_INTERPRETED, formula));

    }

    /**
     * Prüft, ob es sich bei var um einen zulässigen Variablennamen einer
     * logischen Variable handelt.
     */
    public static boolean isValidVariable(String var) {

        if (var.length() == 0) {
            return false;
        }

        //Falls der Ausdruck eine (einfache) Variable ist
        if ((var.length() == 1) && ((int) var.charAt(0) >= 97) && ((int) var.charAt(0) <= 122)) {
            return true;
        }

        //Falls der Ausdruck eine logische Variable mit Index ist (Form: Buchstabe_Index)
        if ((var.length() >= 3) && ((int) var.charAt(0) >= 97) && ((int) var.charAt(0) <= 122)
                && ((int) var.charAt(1) == 95)) {

            for (int i = 2; i < var.length(); i++) {
                if (((int) var.charAt(i) < 48) || ((int) var.charAt(i) > 57)) {
                    return false;
                }
            }
            return true;

        }

        return false;

    }

    // Die folgenden Methoden dienen der Kürze halber.
    /**
     * Gibt die Negation des gegebenen logischen Ausdrucks zurück.
     */
    public LogicalExpression neg() {
        if (this.equals(TRUE)) {
            return FALSE;
        }
        if (this.equals(FALSE)) {
            return TRUE;
        }
        return new LogicalUnaryOperation(this, TypeLogicalUnary.NEGATION);
    }

    /**
     * Gibt die Konjunktion des gegebenen logischen Ausdrucks mit dem logischen
     * Ausdruck logExpr zurück.
     */
    public LogicalExpression and(LogicalExpression logExpr) {
        if (this.equals(FALSE) || logExpr.equals(FALSE)) {
            return FALSE;
        }
        if (this.equals(TRUE)) {
            return logExpr;
        }
        if (logExpr.equals(TRUE)) {
            return this;
        }
        return new LogicalBinaryOperation(this, logExpr, TypeLogicalBinary.AND);
    }

    /**
     * Gibt die Disjunktion des gegebenen logischen Ausdrucks mit dem logischen
     * Ausdruck logExpr zurück.
     */
    public LogicalExpression or(LogicalExpression logExpr) {
        if (this.equals(TRUE) || logExpr.equals(TRUE)) {
            return TRUE;
        }
        if (this.equals(FALSE)) {
            return logExpr;
        }
        if (logExpr.equals(FALSE)) {
            return this;
        }
        return new LogicalBinaryOperation(this, logExpr, TypeLogicalBinary.OR);
    }

    /**
     * Gibt die Implikation des gegebenen logischen Ausdrucks mit dem logischen
     * Ausdruck logExpr zurück.
     */
    public LogicalExpression impl(LogicalExpression logExpr) {
        if (this.equals(FALSE)) {
            return TRUE;
        }
        if (this.equals(TRUE)) {
            return logExpr;
        }
        return new LogicalBinaryOperation(this, logExpr, TypeLogicalBinary.IMPLICATION);
    }

    /**
     * Gibt die Äquivalenz des gegebenen logischen Ausdrucks mit dem logischen
     * Ausdruck logExpr zurück.
     */
    public LogicalExpression equiv(LogicalExpression logExpr) {
        if (this.equals(TRUE)) {
            return logExpr;
        }
        if (logExpr.equals(TRUE)) {
            return this;
        }
        if (this.equals(FALSE) && logExpr.equals(FALSE)) {
            return TRUE;
        }
        return new LogicalBinaryOperation(this, logExpr, TypeLogicalBinary.EQUIVALENCE);
    }

    /**
     * Gibt die Konjunktion des gegebenen logischen Ausdrucks mit dem logischen
     * Ausdruck (value != 0) zurück.
     */
    public LogicalExpression and(int value) {
        if (this.equals(FALSE) || value != 0) {
            return FALSE;
        }
        if (this.equals(TRUE)) {
            return new LogicalConstant(value);
        }
        if (value == 0) {
            return this;
        }
        return new LogicalBinaryOperation(this, new LogicalConstant(value), TypeLogicalBinary.AND);
    }

    /**
     * Gibt die Disjunktion des gegebenen logischen Ausdrucks mit dem logischen
     * Ausdruck (value != 0) zurück.
     */
    public LogicalExpression or(int value) {
        if (this.equals(TRUE) || value == 0) {
            return TRUE;
        }
        if (this.equals(FALSE)) {
            return new LogicalConstant(value);
        }
        if (value != 0) {
            return this;
        }
        return new LogicalBinaryOperation(this, new LogicalConstant(value), TypeLogicalBinary.OR);
    }

    /**
     * Gibt die Implikation des gegebenen logischen Ausdrucks mit dem logischen
     * Ausdruck (value != 0) zurück.
     */
    public LogicalExpression impl(int value) {
        if (this.equals(FALSE)) {
            return TRUE;
        }
        if (this.equals(TRUE)) {
            return new LogicalConstant(value);
        }
        return new LogicalBinaryOperation(this, new LogicalConstant(value), TypeLogicalBinary.IMPLICATION);
    }

    /**
     * Gibt die Äquivalenz des gegebenen logischen Ausdrucks mit dem logischen
     * Ausdruck (value != 0) zurück.
     */
    public LogicalExpression equiv(int value) {
        if (this.equals(TRUE)) {
            return new LogicalConstant(value);
        }
        if (value == 0) {
            return this;
        }
        if (this.equals(FALSE) && value != 0) {
            return TRUE;
        }
        return new LogicalBinaryOperation(this, new LogicalConstant(value), TypeLogicalBinary.EQUIVALENCE);
    }

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck eine Negation ist.
     */
    public boolean isNeg() {
        return this instanceof LogicalUnaryOperation && ((LogicalUnaryOperation) this).getType().equals(TypeLogicalUnary.NEGATION);
    }

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck eine Konjunktion ist.
     */
    public boolean isAnd() {
        return this instanceof LogicalBinaryOperation && ((LogicalBinaryOperation) this).getType().equals(TypeLogicalBinary.AND);
    }

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck eine Disjunktion ist.
     */
    public boolean isOr() {
        return this instanceof LogicalBinaryOperation && ((LogicalBinaryOperation) this).getType().equals(TypeLogicalBinary.OR);
    }

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck eine Implikation ist.
     */
    public boolean isImpl() {
        return this instanceof LogicalBinaryOperation && ((LogicalBinaryOperation) this).getType().equals(TypeLogicalBinary.IMPLICATION);
    }

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck eine Equivalenz ist.
     */
    public boolean isEquiv() {
        return this instanceof LogicalBinaryOperation && ((LogicalBinaryOperation) this).getType().equals(TypeLogicalBinary.EQUIVALENCE);
    }

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck keine Negation ist.
     */
    public boolean isNotNeg() {
        return !(this instanceof LogicalUnaryOperation && ((LogicalUnaryOperation) this).getType().equals(TypeLogicalUnary.NEGATION));
    }

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck keine Konjunktion ist.
     */
    public boolean isNotAnd() {
        return !(this instanceof LogicalBinaryOperation && ((LogicalBinaryOperation) this).getType().equals(TypeLogicalBinary.AND));
    }

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck keine Disjunktion ist.
     */
    public boolean isNotOr() {
        return !(this instanceof LogicalBinaryOperation && ((LogicalBinaryOperation) this).getType().equals(TypeLogicalBinary.OR));
    }

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck keine Implikation ist.
     */
    public boolean isNotImpl() {
        return !(this instanceof LogicalBinaryOperation && ((LogicalBinaryOperation) this).getType().equals(TypeLogicalBinary.IMPLICATION));
    }

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck keine Equivalenz ist.
     */
    public boolean isNotEquiv() {
        return !(this instanceof LogicalBinaryOperation && ((LogicalBinaryOperation) this).getType().equals(TypeLogicalBinary.EQUIVALENCE));
    }

    /**
     * Gibt eine Kopie des gegebenen logischen Ausdrucks zurück.
     */
    public abstract LogicalExpression copy();

    /**
     * Wertet den gegebenen logischen Ausdrucks aus.
     */
    public abstract boolean evaluate();

    /**
     * Fügt alle logischen Variablen, die in dem gegebenen Ausdruck vorkommen,
     * zum HashSet vars hinzu.
     */
    @Override
    public abstract void addContainedVars(HashSet vars);

    /**
     * Gibt ein HashSet mit allen logischen Variablen, die in dem gegebenen
     * Ausdruck vorkommen, zurück.
     */
    @Override
    public HashSet<String> getContainedVars() {
        HashSet<String> vars = new HashSet<>();
        addContainedVars(vars);
        return vars;
    }

    /**
     * Fügt alle logischen Variablen, die in dem gegebenen Ausdruck vorkommen
     * und denen kein fester Wert zugewiesen wurde, zum HashSet vars hinzu.
     */
    @Override
    public void addContainedIndeterminates(HashSet vars) {
        addContainedVars(vars);
    }

    /**
     * Gibt ein HashSet mit allen logischen Variablen, die in dem gegebenen
     * Ausdruck vorkommen und denen kein fester Wert zugewiesen wurde, zurück.
     */
    @Override
    public HashSet<String> getContainedIndeterminates() {
        return getContainedVars();
    }

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck die logische Variable var
     * enthält.
     */
    @Override
    public abstract boolean contains(String var);

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck logische keine logischen
     * Variablen enthält.
     */
    public abstract boolean isConstant();

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck logische gleich dem
     * logischen Ausdruck logExpr ist.
     */
    public abstract boolean equals(LogicalExpression logExpr);

    /**
     * Gibt zurück, ob der gegebene logische Ausdruck logische äquivalent zum
     * logischen Ausdruck logExpr ist.
     */
    public abstract boolean equivalent(LogicalExpression logExpr);

    /**
     * Ersetzt im gegebenen Ausdruck die Variable var durch den Ausdruck expr.
     */
    public abstract LogicalExpression replaceVariable(String var, LogicalExpression expr);
    
    /**
     * Gibt einen vereinfachten logischen Ausdruck des gegebenen logischen
     * Ausdrucks zurück, wobei bei der Vereinfachung einfache
     * Vereinfachungsoperationen eingesetzt werden.
     *
     * @throws EvaluationException
     */
    public abstract LogicalExpression simplifyBasic() throws EvaluationException;

    /**
     * Gibt einen vereinfachten logischen Ausdruck des gegebenen logischen
     * Ausdrucks zurück, wobei bei der Vereinfachung das Distributivgesetz
     * verwendet wird. Letzteres bedeutet, dass in einem logischen Ausdruck
     * bezüglich OR wie folgt faktorsiert wird, falls möglich: der logische
     * Ausdruck a &#38; b | a &#38; c wird zu a &#38; (b | c) vereinfacht.
     *
     * @throws EvaluationException
     */
    public abstract LogicalExpression factorizeInSums() throws EvaluationException;

    /**
     * Gibt einen vereinfachten logischen Ausdruck des gegebenen logischen
     * Ausdrucks zurück, wobei bei der Vereinfachung das Distributivgesetz
     * verwendet wird. Letzteres bedeutet, dass in einem logischen Ausdruck
     * bezüglich AND wie folgt faktorsiert wird, falls möglich: der logische
     * Ausdruck(a | b) &#38; (a | c) wird zu a | (b &#38; c) vereinfacht.
     *
     * @throws EvaluationException
     */
    public abstract LogicalExpression factorizeInProducts() throws EvaluationException;

    /**
     * Inkrementiert den Binärcounter counter um 1, wobei true mit 1 und false
     * mit 0 identifiziert wird. Beim Überlauf wird das Array {0, ..., 0}
     * zurückgegeben.
     */
    public static boolean[] binaryCounter(boolean[] counter) {

        boolean[] result = counter;
        for (int i = 0; i < counter.length; i++) {
            if (counter[i]) {
                counter[i] = false;
            } else {
                counter[i] = true;
                break;
            }
        }
        return result;

    }

    /**
     * Gibt die kanonische konjunktive Normalform des gegebenen logischen
     * Ausdrucks zurück.
     */
    public LogicalExpression toCCNF() {

        HashSet vars = new HashSet();
        this.addContainedVars(vars);
        HashMap<Integer, String> varsEnumerated = new HashMap<>();

        // Variablen vars in eine Reihenfolge bringen.
        Iterator iter = vars.iterator();
        for (int i = 0; i < vars.size(); i++) {
            varsEnumerated.put(varsEnumerated.size(), (String) iter.next());
        }

        /*
         Erstellung eines Binärcounters zum Durchlaufen aller möglichen
         Belegungen der Variablen in vars.
         */
        boolean[] varsValues = new boolean[vars.size()];
        boolean currentValueOfLogicalExpression;
        int tableLength = BigInteger.valueOf(2).pow(vars.size()).intValue();

        LogicalExpression result = FALSE;
        LogicalExpression currentNormalFormTerm = TRUE;

        for (int i = 0; i < tableLength; i++) {

            // Logische Variablen gemäß dem aktuellen Counterstand belegen.
            for (int j = 0; j < vars.size(); j++) {
                LogicalVariable.setValue(varsEnumerated.get(j), varsValues[j]);
            }

            currentValueOfLogicalExpression = this.evaluate();
            if (!currentValueOfLogicalExpression) {

                // Aktuellen KKNF-Faktor bilden.
                for (int j = 0; j < vars.size(); j++) {
                    if (j == 0) {
                        if (varsValues[0]) {
                            currentNormalFormTerm = LogicalVariable.create(varsEnumerated.get(0)).neg();
                        } else {
                            currentNormalFormTerm = LogicalVariable.create(varsEnumerated.get(0));
                        }
                    } else if (varsValues[j]) {
                        currentNormalFormTerm = currentNormalFormTerm.or(LogicalVariable.create(varsEnumerated.get(j)).neg());
                    } else {
                        currentNormalFormTerm = currentNormalFormTerm.or(LogicalVariable.create(varsEnumerated.get(j)));
                    }
                }

                if (result.equals(FALSE)) {
                    result = currentNormalFormTerm;
                } else {
                    result = currentNormalFormTerm.and(result);
                }
            }
            varsValues = binaryCounter(varsValues);

        }

        return result;

    }

    /**
     * Gibt die kanonische disjunktive Normalform des gegebenen logischen
     * Ausdrucks zurück.
     */
    public LogicalExpression toCDNF() {

        HashSet vars = new HashSet();
        this.addContainedVars(vars);
        HashMap<Integer, String> varsEnumerated = new HashMap<>();

        // Variables vars in eine Reihenfolge bringen.
        Iterator iter = vars.iterator();
        for (int i = 0; i < vars.size(); i++) {
            varsEnumerated.put(varsEnumerated.size(), (String) iter.next());
        }

        /*
         Erstellung eines Binärcounters zum Durchlaufen aller möglichen
         Belegungen der Variablen in vars.
         */
        boolean[] varsValues = new boolean[vars.size()];
        boolean currentValueOfLogicalExpression;
        int tableLength = BigInteger.valueOf(2).pow(vars.size()).intValue();

        LogicalExpression result = FALSE;
        LogicalExpression currentNormalFormTerm = TRUE;

        for (int i = 0; i < tableLength; i++) {

            // Logische Variablen gemäß dem aktuellen Counterstand belegen.
            for (int j = 0; j < vars.size(); j++) {
                LogicalVariable.setValue(varsEnumerated.get(j), varsValues[j]);
            }

            currentValueOfLogicalExpression = this.evaluate();
            if (currentValueOfLogicalExpression) {

                // Aktuellen KDNF-Summanden bilden.
                for (int j = 0; j < vars.size(); j++) {
                    if (j == 0) {
                        if (varsValues[0]) {
                            currentNormalFormTerm = LogicalVariable.create(varsEnumerated.get(0));
                        } else {
                            currentNormalFormTerm = LogicalVariable.create(varsEnumerated.get(0)).neg();
                        }
                    } else if (varsValues[j]) {
                        currentNormalFormTerm = currentNormalFormTerm.and(LogicalVariable.create(varsEnumerated.get(j)));
                    } else {
                        currentNormalFormTerm = currentNormalFormTerm.and(LogicalVariable.create(varsEnumerated.get(j)).neg());
                    }
                }

                if (result.equals(FALSE)) {
                    result = currentNormalFormTerm;
                } else {
                    result = currentNormalFormTerm.or(result);
                }
            }
            varsValues = binaryCounter(varsValues);

        }

        return result;

    }

    /**
     * Gibt die 'Standardvereinfachung' allgemeiner Ausdrücke zurück.
     */
    public LogicalExpression simplify() throws EvaluationException {

        try {
            LogicalExpression logExpr, logExprSimplified = this;
            do {
                logExpr = logExprSimplified.copy();
//                System.out.println(logExprSimplified.writeLogicalExpression());
                logExprSimplified = logExprSimplified.simplifyBasic();
                Canceller.interruptComputationIfNeeded();
                logExprSimplified = logExprSimplified.factorizeInProducts();
                Canceller.interruptComputationIfNeeded();
                logExprSimplified = logExprSimplified.factorizeInSums();
                Canceller.interruptComputationIfNeeded();
            } while (!logExpr.equals(logExprSimplified));
            return logExprSimplified;
        } catch (java.lang.StackOverflowError e) {
            throw new EvaluationException(Translator.translateOutputMessage(LEB_LogicalExpression_STACK_OVERFLOW));
        }

    }

}
