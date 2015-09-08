package expressionbuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import translator.Translator;

public abstract class Expression {

    //Sprache für Fehlermeldungen.
    private static TypeLanguage language;

    public final static Variable PI = Variable.create("pi");
    public final static Constant ZERO = new Constant(0);
    public final static Constant ONE = new Constant(1);
    public final static Constant TWO = new Constant(2);
    public final static Constant THREE = new Constant(3);
    public final static Constant FOUR = new Constant(4);
    public final static Constant MINUS_ONE = new Constant(-1);

    public static TypeLanguage getLanguage() {
        return language;
    }

    public static void setLanguage(TypeLanguage typeLanguage) {
        language = typeLanguage;
    }

    /**
     * Der Befehl für die jeweilige math. Operation UND die Parameter in der
     * Befehlsklammer werden ausgelesen und zurückgegeben. BEISPIEL: commandLine
     * = f(x, y, z). Zurückgegeben wird ein array der Länge zwei: im 0. Eintrag
     * steht der String "f", im 1. der String "x, y, z".
     *
     * @throws ExpressionException
     */
    public static String[] getOperatorAndArguments(String input) throws ExpressionException {

        //Leerzeichen beseitigen
        input = input.replaceAll(" ", "");

        String[] result = new String[2];
        int i = input.indexOf("(");
        if (i == -1) {
            // Um zu verhindern, dass es eine IndexOutOfBoundsException gibt.
            i = 0;
        }
        result[0] = input.substring(0, i);

        //Wenn der Befehl leer ist -> Fehler.
        if (result[0].length() == 0) {
            throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_EXPRESSION_EMPTY_OR_INCOMPLETE"));
        }

        //Wenn length(result[0]) > l - 2 -> Fehler (der Befehl besitzt NICHT die Form command(...)).
        if (result[0].length() > input.length() - 2) {
            throw new ExpressionException(input + Translator.translateExceptionMessage("EB_Expression_IS_NOT_VALID_COMMAND"));
        }

        //Wenn am Ende nicht ")" steht.
        if (!input.substring(input.length() - 1, input.length()).equals(")")) {
            throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_MISSING_CLOSING_BRACKET_1")
                    + input
                    + Translator.translateExceptionMessage("EB_Expression_MISSING_CLOSING_BRACKET_2"));
        }

        result[1] = input.substring(result[0].length() + 1, input.length() - 1);

        return result;

    }

    /**
     * Input: String commandline, in der NUR die Parameter (getrennt durch ein
     * Komma) stehen. Beispiel commandLine == "x,y,f(w,z),u,v" -> Paremeter sind
     * dann {x, y, f(w, z), u, v}. Nach einem eingelesenen Komma, welches NICHT
     * von runden Klammern umgeben ist, werden die Parameter getrennt.
     *
     * @throws ExpressionException
     */
    public static String[] getArguments(String input) throws ExpressionException {

        //Leerzeichen beseitigen
        input = input.replaceAll(" ", "");

        //Falls Parameterstring leer ist -> Fertig
        if (input.isEmpty()) {
            return new String[0];
        }

        ArrayList<String> resultParameters = new ArrayList<>();
        int startPositionOfCurrentParameter = 0;

        /*
         Differenz zwischen der Anzahl der öffnenden und der der schließenden
         Klammern (bracketCounter == 0 am Ende -> alles ok).
         */
        int bracketCounter = 0;
        int squareBracketCounter = 0;
        String currentChar;
        //Jetzt werden die einzelnen Parameter ausgelesen
        for (int i = 0; i < input.length(); i++) {

            currentChar = input.substring(i, i + 1);
            if (currentChar.equals("(")) {
                bracketCounter++;
            }
            if (currentChar.equals(")")) {
                bracketCounter--;
            }
            if (currentChar.equals("[")) {
                squareBracketCounter++;
            }
            if (currentChar.equals("]")) {
                squareBracketCounter--;
            }
            if (bracketCounter == 0 && squareBracketCounter == 0 && currentChar.equals(",")) {
                if (input.substring(startPositionOfCurrentParameter, i).isEmpty()) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_EMPTY_PARAMETER"));
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, i));
                startPositionOfCurrentParameter = i + 1;
            }
            if (i == input.length() - 1) {
                if (startPositionOfCurrentParameter == input.length()) {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_EMPTY_PARAMETER"));
                }
                resultParameters.add(input.substring(startPositionOfCurrentParameter, input.length()));
            }

        }

        if (bracketCounter != 0 || squareBracketCounter != 0) {
            throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_WRONG_BRACKETS"));
        }

        String[] resultParametersAsArray = new String[resultParameters.size()];
        for (int i = 0; i < resultParameters.size(); i++) {
            resultParametersAsArray[i] = resultParameters.get(i);
        }

        return resultParametersAsArray;

    }

    /**
     * Prüft, ob es sich bei var um einen zulässigen Variablennamen handelt.
     * True wird genau dann zurückgegeben, wenn var ein Kleinbuchstabe ist,
     * eventuell gefolgt von '_' und einer natürlichen Zahl (als Index).
     * Beispielsweise wird bei y, x_2, z_4, true zurückgegeben, bei t_3_5
     * dagegen wird false zurückgegeben.
     */
    public static boolean isValidVariable(String var) {

        if (var.length() == 0) {
            return false;
        }

        //Falls der Ausdruck eine (einfache) Variable ist
        if ((var.length() == 1) && ((int) var.charAt(0) >= 97) && ((int) var.charAt(0) <= 122)) {
            return true;
        }

        //Falls der Ausdruck eine Variable mit Index ist (Form: Buchstabe_Index)
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

    /**
     * Prüft, ob es sich bei var um einen zulässigen Variablennamen oder um die
     * formale Ableitung einer zulässigen Variable handelt. True wird genau dann
     * zurückgegeben, wenn var ein Kleinbuchstabe ist, eventuell gefolgt von '_'
     * und einer natürlichen Zahl (als Index) und eventuell von einer Anzahl von
     * Apostrophs. Beispielsweise wird bei y, x_2, z_4''', t'' true
     * zurückgegeben, bei t'_3' dagegen wird false zurückgegeben.
     */
    public static boolean isValidDerivateOfVariable(String var) {

        while ((var.length() > 0) && var.substring(var.length() - 1).equals("'")) {
            var = var.substring(0, var.length() - 1);
        }

        return isValidVariable(var);

    }

    public static boolean isPI(String formula) {
        return formula.equals("pi");
    }

    /**
     * Beseitigt alle Leerzeichen im String s und verwandelt alle Großbuchstaben
     * zu Kleinbuchstaben.
     */
    private static String convertToSmallLetters(String s) {

        //Leerzeichen beseitigen
        s = s.replaceAll(" ", "");

        //Falls Großbuchstaben auftreten -> zu Kleinbuchstaben machen
        for (int i = 0; i < s.length(); i++) {
            if (((int) s.charAt(i) >= 65) && ((int) s.charAt(i) <= 90)) {
                //Macht Großbuchstaben zu Kleinbuchstaben
                s = s.substring(0, i) + (char) ((int) s.charAt(i) + 32) + s.substring(i + 1, s.length());
            }
        }

        return s;

    }

    /**
     * Hauptmethode zum Erstellen einer Expression aus einem String.
     *
     * @throws ExpressionException
     */
    public static Expression build(String formula, HashSet<String> vars) throws ExpressionException {

        // Leerzeichen beseitigen und alles zu Kleinbuchstaben machen
        formula = convertToSmallLetters(formula);

        // Prioritäten: + = 0, - = 1, * = 2, / = 3, ^ = 4, Zahl, Var, Funktion, Operator = 5.
        int priority = 5;
        int breakpoint = -1;
        int bracketCounter = 0;
        int absBracketCounter = 0;
        int formulaLength = formula.length();
        String currentChar;

        if (formula.isEmpty()) {
            throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_EXPRESSION_EMPTY_OR_INCOMPLETE"));
        }

        for (int i = 1; i <= formulaLength; i++) {
            currentChar = formula.substring(formulaLength - i, formulaLength - i + 1);

            // Öffnende und schließende Klammern zählen.
            if (currentChar.equals("(") && bracketCounter == 0) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_WRONG_BRACKETS"));
            }

            if (currentChar.equals(")")) {
                bracketCounter++;
            }
            if (currentChar.equals("(")) {
                bracketCounter--;
            }

            // Öffnende und schließende Betragsklammern zählen.
            char charWithinAbsBrackets;
            if (currentChar.equals("|") && formulaLength - i - 1 >= 0) {

                int k = 1;
                //Aufeinanderfolgende Betragsstriche werden gezählt
                while (formulaLength - i - k >= 0 && (int) formula.charAt(formulaLength - i - k) == 124) {
                    k++;
                }

                if (formulaLength - i - k >= 0) {
                    charWithinAbsBrackets = formula.charAt(formulaLength - i - k);
                } else {
                    absBracketCounter = absBracketCounter - k;
                    if (absBracketCounter != 0) {
                        throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_WRONG_ABS_BRACKETS"));
                    }
                    break;
                }

                if (formulaLength - i - k == 0) {
                    absBracketCounter = absBracketCounter - k;
                    i = i + k - 1;
                } else if ((int) charWithinAbsBrackets >= 97 && (int) charWithinAbsBrackets <= 122
                        || (int) charWithinAbsBrackets >= 48 && (int) charWithinAbsBrackets <= 57
                        || (int) charWithinAbsBrackets == 41) {
                    /*
                     Dann steht links von einer |-Kette eine Zahl oder ein
                     Buchstabe oder ")" -> Es ist eine Ketten von schließenden
                     Betragsklammern.
                     */
                    absBracketCounter = absBracketCounter + k;
                    i = i + k - 1;
                } else {
                    // Andernfalls ist eine Ketten von öffnenden Betragsklammern.
                    absBracketCounter = absBracketCounter - k;
                    i = i + k - 1;
                }

            } else if (currentChar.equals("|") && i == formulaLength) {
                absBracketCounter--;
            }

            if (absBracketCounter < 0) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_WRONG_ABS_BRACKETS"));
            }

            if (bracketCounter != 0 || absBracketCounter != 0) {
                continue;
            }
            //Aufteilungspunkt finden; zunächst wird nach -, +, *, /, ^ gesucht 
            //breakpoint gibt den Index in formula an, wo die Formel aufgespalten werden soll.
            if (currentChar.equals("+") && priority > 0) {
                priority = 0;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("-") && priority > 1) {
                priority = 1;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("*") && priority > 2) {
                priority = 2;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("/") && priority > 3) {
                priority = 3;
                breakpoint = formulaLength - i;
            } else if (currentChar.equals("^") && priority > 4) {
                priority = 4;
                breakpoint = formulaLength - i;
            }
        }

        if (bracketCounter > 0) {
            throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_WRONG_BRACKETS"));
        }
        if (absBracketCounter > 0) {
            throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_WRONG_ABS_BRACKETS"));
        }

        // Aufteilung, falls eine Elementaroperation (-, +, /, *, ^) vorliegt
        if (priority <= 4) {
            String formulaLeft = formula.substring(0, breakpoint);
            String formulaRight = formula.substring(breakpoint + 1, formulaLength);

            if ((formulaLeft.isEmpty()) && priority > 1) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_LEFT_SIDE_OF_BINARY_IS_EMPTY"));
            }
            if (formulaRight.isEmpty()) {
                throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_RIGHT_SIDE_OF_BINARY_IS_EMPTY"));
            }

            //Falls der Ausdruck die Form "+abc..." besitzt -> daraus "abc..." machen
            if ((formulaLeft.isEmpty()) && (priority == 0)) {
                return build(formulaRight, vars);
            }
            //Falls der Ausdruck die Form "-abc..." besitzt -> daraus "(-1)*abc..." machen
            if ((formulaLeft.isEmpty()) && (priority == 1)) {
                Expression right = build(formulaRight, vars);
                if (right instanceof Constant) {
                    return new Constant(((Constant) right).getPreciseValue().negate());
                }
                return MINUS_ONE.mult(build(formulaRight, vars));
            }
            switch (priority) {
                case 0:
                    return new BinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeBinary.PLUS);
                case 1:
                    return new BinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeBinary.MINUS);
                case 2:
                    return new BinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeBinary.TIMES);
                case 3:
                    return new BinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeBinary.DIV);
                case 4:
                    return new BinaryOperation(build(formulaLeft, vars), build(formulaRight, vars), TypeBinary.POW);
                default:    //Passiert zwar nicht, aber trotzdem!
                    return null;
            }
        }

        // Falls kein binärer Operator und die Formel die Form (...) hat -> Klammern beseitigen.
        if ((priority == 5) && (formula.substring(0, 1).equals("(")) && (formula.substring(formulaLength - 1, formulaLength).equals(")"))) {
            return build(formula.substring(1, formulaLength - 1), vars);
        }

        //Falls der Ausdruck eine Zahl ist.
        if (priority == 5) {
            try {
                return new Constant(new BigDecimal(formula));
            } catch (NumberFormatException e) {
            }
        }

        //Falls der Ausdruck eine Variable ist.
        if (priority == 5) {
            if (isValidDerivateOfVariable(formula)) {
                vars.add(formula);
                return Variable.create(formula);
            }
            if (isPI(formula)) {
                return Variable.create(formula, Math.PI);
            }
        }

        //AUSNAHME: |...| = abs(...), falls es klappt!
        if (formula.substring(0, 1).equals("|") && formula.substring(formula.length() - 1, formula.length()).equals("|")) {
            Expression formulaInAbsBrackets = Expression.build(formula.substring(1, formula.length() - 1), vars);
            return new Function(formulaInAbsBrackets, TypeFunction.abs);
        }

        //Falls der Ausdruck eine Funktion ist.
        if (priority == 5) {
            int functionNameLength;
            for (TypeFunction type : TypeFunction.values()) {
                functionNameLength = type.toString().length();
                //Falls der Ausdruck die Form function(...) hat -> Funktion und Argument auslesen
                if (formula.length() >= functionNameLength + 2) {
                    if ((formula.substring(0, functionNameLength).equals(type.toString()))
                            && (formula.substring(functionNameLength, functionNameLength + 1).equals("("))
                            && (formula.substring(formulaLength - 1, formulaLength).equals(")"))) {

                        String functionArgument = formula.substring(functionNameLength + 1, formulaLength - 1);
                        if (type.equals(TypeFunction.sqrt)) {
                            // Die Wurzel wird intern sofort als (...)^(1/2) aufgefasst.
                            return build(functionArgument, vars).pow(ONE.div(TWO));
                        }
                        return new Function(build(functionArgument, vars), type);

                    }
                }
            }
        }

        //AUSNAHME: Operator Fakultät (== !).
        if (priority == 5) {
            if (formula.substring(formula.length() - 1, formula.length()).equals("!")) {
                Expression[] params = new Expression[1];
                params[0] = Expression.build(formula.substring(0, formula.length() - 1), vars);
                return new Operator(TypeOperator.fac, params);
            }
        }

        //Falls der Ausdruck ein Operator ist.
        if (priority == 5) {
            String[] operatorNameAndParams = getOperatorAndArguments(formula);
            String[] params = getArguments(operatorNameAndParams[1]);
            String operatorName;
            for (TypeOperator type : TypeOperator.values()) {
                if (type.equals(TypeOperator.integral)) {
                    operatorName = "int";
                } else {
                    operatorName = type.toString();
                }
                if (operatorNameAndParams[0].equals(operatorName)) {
                    return Operator.getOperator(operatorName, params, vars);
                }
            }
        }

        //Falls der Ausdruck eine vom Benutzer selbstdefinierte Funktion ist.
        if (priority == 5) {
            String function = getOperatorAndArguments(formula)[0];
            String[] functionArguments = getArguments(getOperatorAndArguments(formula)[1]);

            if (SelfDefinedFunction.innerExpressionsForSelfDefinedFunctions.containsKey(function)) {
                if (SelfDefinedFunction.varsForSelfDefinedFunctions.get(function).length == functionArguments.length) {
                    Expression[] exprs_in_arguments = new Expression[functionArguments.length];
                    for (int i = 0; i < functionArguments.length; i++) {
                        exprs_in_arguments[i] = Expression.build(functionArguments[i], vars);
                    }
                    return new SelfDefinedFunction(function, SelfDefinedFunction.varsForSelfDefinedFunctions.get(function),
                            SelfDefinedFunction.abstractExpressionsForSelfDefinedFunctions.get(function), exprs_in_arguments);
                } else {
                    throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_WRONG_NUMBER_OF_PARAMETERS_IN_SELF_DEFINED_FUNCTION_1")
                            + function
                            + Translator.translateExceptionMessage("EB_Expression_WRONG_NUMBER_OF_PARAMETERS_IN_SELF_DEFINED_FUNCTION_2")
                            + String.valueOf(SelfDefinedFunction.varsForSelfDefinedFunctions.get(function).length)
                            + Translator.translateExceptionMessage("EB_Expression_WRONG_NUMBER_OF_PARAMETERS_IN_SELF_DEFINED_FUNCTION_3"));
                }
            }

        }

        throw new ExpressionException(Translator.translateExceptionMessage("EB_Expression_FORMULA_CANNOT_BE_INTERPRETED") + formula);

    }

    /**
     * Legt eine neue Kopie von this an.
     */
    public abstract Expression copy();

    /**
     * Liefert den Wert des zugrundeliegenden Ausdrucks unter Einsetzung aller
     * Variablenwerte.
     *
     * @throws EvaluationException
     */
    public abstract double evaluate() throws EvaluationException;

    /**
     * Liefert einen Ausdruck, bei dem für alle Variablen, die in vars enthalten
     * sind, die zugehörigen präzisen Werte eingesetzt werden. Die restlichen
     * Variablen werden als Unbestimmte gelassen.
     *
     * @throws EvaluationException
     */
    public abstract Expression evaluate(HashSet<String> vars) throws EvaluationException;

    /**
     * Fügt alle Variablen, die in dem gegebenen Ausdruck vorkommen, zum HashSet
     * vars hinzu. Ziel: Start mit vars = {} liefert alle vorkommenden
     * Variablen.
     */
    public abstract void getContainedVars(HashSet<String> vars);

    /**
     * Gibt zurück, ob this die Variable var enthält.
     */
    public abstract boolean contains(String var);

    /**
     * Gibt zurück, ob this nichtexakte Konstanten enthält.
     */
    public abstract boolean containsApproximates();

    /**
     * Gibt zurück, ob this Funktionen enthält.
     */
    public abstract boolean containsFunction();

    /**
     * Gibt zurück, ob this UNBESTIMMTE Integrale enthält.
     */
    public abstract boolean containsIndefiniteIntegral();

    /**
     * Setzt alle im Ausdruck vorkommenden Konstanten auf 'approximativ'
     * (precise = false).
     */
    public abstract Expression turnToApproximate();

    /**
     * Setzt alle im Ausdruck vorkommenden Konstanten auf 'exakt' (precise =
     * true).
     */
    public abstract Expression turnToPrecise();

    /**
     * ersetzt in einer Funktion f(x_1, ..., x_n) die Variable var durch den
     * Ausdruck expr.
     */
    public abstract Expression replaceVariable(String var, Expression expr);

    /**
     * Schreibt eine vom Benutzer definierte Funktion in den üblichen
     * vordefinierten Termen aus. Beispiel: Der Benutzer definiert def(f(x) =
     * exp(x)+x^2). Dann liefert diese Methode für den Ausdruck expr = f(u) den
     * Ausdruck exp(u)+u^2 zurück. Technische Umsetzung: Alle Instanzen von
     * Klassen, außer SelfDefinedFunction, werden gleich gelassen. In Instanzen
     * von SelfDefinedFunction wird der abstrakte Ausdruck durch die konkreten
     * Einträge ersetzt.
     */
    public abstract Expression replaceSelfDefinedFunctionsByPredefinedFunctions();

    /**
     * Differenziert eine Funktion nach der Variablen var
     *
     * @throws EvaluationException
     */
    public abstract Expression diff(String var) throws EvaluationException;

    /**
     * Differenziert eine Differentialgleichung nach der Variablen var
     *
     * @throws EvaluationException
     */
    public abstract Expression diffDifferentialEquation(String var) throws EvaluationException;

    /**
     * Methode, die liefert, ob ein Ausdruck konstant ist
     */
    public abstract boolean isConstant();

    /**
     * Liefert true, falls der Ausdruck konstant ist und mit Sicherheit
     * mindestens 0 ist. Im ungewissen Fall wird false ausgegeben.
     */
    public abstract boolean isNonNegative();

    /**
     * Liefert true, falls der Ausdruck konstant ist und mit Sicherheit
     * höchstens 0 ist. Im ungewissen Fall wird false ausgegeben.
     */
    public boolean isNonPositive() {
        try {
            return (Expression.MINUS_ONE).mult(this).simplify().isNonNegative();
        } catch (EvaluationException e) {
            return false;
        }
    }

    /**
     * Liefert true, falls der Ausdruck konstant ist und mit Sicherheit > 0 ist.
     * Im ungewissen Fall wird false ausgegeben.
     */
    public boolean isPositive() {
        return this.isNonNegative() && !this.equals(ZERO);
    }

    /**
     * Liefert true, falls der Ausdruck konstant ist und mit Sicherheit < 0 ist.
     * Im ungewissen Fall wird false ausgegeben.
     */
    public boolean isNegative() {
        return this.isNonPositive() && !this.equals(ZERO);
    }

    /**
     * Liefert true, falls der Ausdruck definiv immer nichtnegativ ist (z.B.
     * x^2+y^4 etc.)
     */
    public abstract boolean isAlwaysNonNegative();

    /**
     * Liefert true, falls der Ausdruck definiv immer positiv ist (z.B.
     * 1+x^2+y^4 etc.)
     */
    public abstract boolean isAlwaysPositive();

    /**
     * Prüft, ob der Ausdruck ein eine rationale Konstante ist.
     */
    public boolean isRationalConstant() {
        return this.isQuotient()
                && ((BinaryOperation) this).getLeft().isIntegerConstant()
                && ((BinaryOperation) this).getRight().isIntegerConstant();
    }

    /**
     * Prüft, ob der Ausdruck eine ganzzahlige oder eine rationale Konstante
     * ist.
     */
    public boolean isIntegerConstantOrRationalConstant() {
        return this.isIntegerConstant() || this.isRationalConstant();
    }

    /**
     * Prüft, ob eine Konstante oder ein Bruch negativ ist. Liefert true, wenn
     * expr eine Konstante oder ein Bruch ist UND wenn expr negativ ist,
     * ansonsten false.
     */
    public boolean isIntegerConstantOrRationalConstantNegative() {
        if (this instanceof Constant) {
            return (((Constant) this).getPreciseValue().compareTo(BigDecimal.ZERO) < 0);
        }
        if (this.isRationalConstant()) {
            return (((Constant) ((BinaryOperation) this).getLeft()).getPreciseValue().multiply(
                    ((Constant) ((BinaryOperation) this).getRight()).getPreciseValue())).compareTo(BigDecimal.ZERO) < 0;
        }
        return false;
    }

    /**
     * Liefert true genau dann, wenn this eine Konstante mit ganzzahligem Wert
     * ist.
     */
    public boolean isIntegerConstant() {
        return this instanceof Constant && ((Constant) this).getPreciseValue().compareTo(((Constant) this).getPreciseValue().setScale(0, BigDecimal.ROUND_HALF_UP)) == 0;
    }

    /**
     * Liefert true genau dann, wenn this eine Konstante mit ganzzahligem
     * ungeraden Wert ist.
     */
    public boolean isOddConstant() {
        if (this.isIntegerConstant()) {
            BigInteger value = ((Constant) this).getPreciseValue().toBigInteger();
            if (value.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ONE) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Liefert true genau dann, wenn this eine Konstante mit ganzzahligem
     * geraden Wert ist.
     */
    public boolean isEvenConstant() {
        if (this.isIntegerConstant()) {
            BigInteger value = ((Constant) this).getPreciseValue().toBigInteger();
            if (value.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt den Ausdruck als String aus.
     */
    public abstract String writeExpression();

    @Override
    public String toString() {
        return this.writeExpression();
    }

    /**
     * Hilfsmethode für writeExpression und für das Zeichnen von Ausdrücken.
     * Gibt zurück, ob der Ausdruck mit einem negativen Vorzeichen anfängt.
     * Beispiel: bei this = (-2)*3 wird true zurückgegeben, bei x*(-7)*5 wird
     * false zurückgegeben.
     */
    public boolean doesExpressionStartsWithAMinusSign() {

        if (this instanceof Constant) {
            return ((Constant) this).getPreciseValue().compareTo(BigDecimal.ZERO) < 0;
        }
        if (this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.TIMES)) {
            return ((BinaryOperation) this).getLeft().doesExpressionStartsWithAMinusSign();
        }
        return false;

    }

    /**
     * Generierung eines Latex-Codes aus einem Ausdruck.
     */
    public abstract String expressionToLatex(boolean beginning);

    /**
     * Liefert, ob der gegebene Ausdruck eine identische Kopie von expr
     * darstellt.
     */
    public abstract boolean equals(Expression expr);

    /**
     * Liefert, ob der gegebene Ausdruck äquivalent zu dem von expr ist.
     */
    public abstract boolean equivalent(Expression expr);

    /**
     * Liefert true, wenn der Ausdruck this einen nicht-negativen Koeffizienten
     * besitzt, falls man this als Produkt auffasst. Beispiele: (1) Für expr =
     * 2*x*(-3)*y wird false zurückgegeben, da expr == (-6)*x*y einen negativen
     * Koeffizienten besitzt. (2) Für expr = x + 3*y wird true zurückgegeben, da
     * der Koeffizient 1 ist, wenn man expr als Produkt auffasst.
     */
    public abstract boolean hasPositiveSign();

    /**
     * Addiert zwei Ausdrücke.
     */
    public Expression add(Expression expr) {
        return new BinaryOperation(this, expr, TypeBinary.PLUS);
    }

    //Folgende Funktionen dienen der Kürze halber.
    public Expression add(BigDecimal a) {
        return this.add(new Constant(a));
    }

    public Expression add(BigInteger a) {
        return this.add(new Constant(a));
    }

    public Expression add(int a) {
        return this.add(new Constant(a));
    }

    /**
     * Subtrahiert zwei Ausdrücke.
     */
    public Expression sub(Expression expr) {
        return new BinaryOperation(this, expr, TypeBinary.MINUS);
    }

    //Folgende Funktionen dienen der Kürze halber.
    public Expression sub(BigDecimal a) {
        return this.sub(new Constant(a));
    }

    public Expression sub(BigInteger a) {
        return this.sub(new Constant(a));
    }

    public Expression sub(int a) {
        return this.sub(new Constant(a));
    }

    /**
     * Multipliziert zwei Ausdrücke.
     */
    public Expression mult(Expression expr) {
        return new BinaryOperation(this, expr, TypeBinary.TIMES);
    }

    // Folgende Funktionen dienen der Kürze halber.
    public Expression mult(BigDecimal n) {
        return this.mult(new Constant(n));
    }

    public Expression mult(BigInteger n) {
        return this.mult(new Constant(n));
    }

    public Expression mult(int n) {
        return this.mult(new Constant(n));
    }

    /**
     * Dividiert zwei Ausdrücke.
     */
    public Expression div(Expression expr) {
        return new BinaryOperation(this, expr, TypeBinary.DIV);
    }

    // Folgende Funktionen dienen der Kürze halber.
    public Expression div(BigDecimal n) {
        return this.div(new Constant(n));
    }

    public Expression div(BigInteger n) {
        return this.div(new Constant(n));
    }

    public Expression div(int n) {
        return this.div(new Constant(n));
    }

    /**
     * Potenziert zwei Ausdrücke.
     */
    public Expression pow(Expression expr) {
        return new BinaryOperation(this, expr, TypeBinary.POW);
    }

    // Folgende Funktionen dienen der Kürze halber.
    public Expression pow(BigDecimal n) {
        return this.pow(new Constant(n));
    }

    public Expression pow(BigInteger n) {
        return this.pow(new Constant(n));
    }

    public Expression pow(int n) {
        return this.pow(new Constant(n));
    }

    // Dasselbe wie pow(), nur für gebrochene Potenzen.
    public Expression pow(Expression m, Expression n) {
        return this.pow(m.div(n));
    }

    public Expression pow(BigDecimal m, BigDecimal n) {
        return this.pow((new Constant(m).div(new Constant(n))));
    }

    public Expression pow(BigInteger m, BigInteger n) {
        return this.pow((new Constant(m).div(new Constant(n))));
    }

    public Expression pow(int m, int n) {
        return this.pow((new Constant(m).div(new Constant(n))));
    }

    // Hilfsmethoden: Bilden die entsprechenden Funktionen eines Ausdrucks.
    public Function abs() {
        return new Function(this, TypeFunction.abs);
    }

    public Function arccos() {
        return new Function(this, TypeFunction.arccos);
    }

    public Function arccosec() {
        return new Function(this, TypeFunction.arccosec);
    }

    public Function arccot() {
        return new Function(this, TypeFunction.arccot);
    }

    public Function arcosech() {
        return new Function(this, TypeFunction.arcosech);
    }

    public Function arcosh() {
        return new Function(this, TypeFunction.arcosh);
    }

    public Function arcoth() {
        return new Function(this, TypeFunction.arcoth);
    }

    public Function arcsec() {
        return new Function(this, TypeFunction.arcsec);
    }

    public Function arcsin() {
        return new Function(this, TypeFunction.arcsin);
    }

    public Function arctan() {
        return new Function(this, TypeFunction.arctan);
    }

    public Function arsech() {
        return new Function(this, TypeFunction.arsech);
    }

    public Function arsinh() {
        return new Function(this, TypeFunction.arsinh);
    }

    public Function artanh() {
        return new Function(this, TypeFunction.artanh);
    }

    public Function cos() {
        return new Function(this, TypeFunction.cos);
    }

    public Function cosec() {
        return new Function(this, TypeFunction.cosec);
    }

    public Function cosech() {
        return new Function(this, TypeFunction.cosech);
    }

    public Function cosh() {
        return new Function(this, TypeFunction.cosh);
    }

    public Function cot() {
        return new Function(this, TypeFunction.cot);
    }

    public Function coth() {
        return new Function(this, TypeFunction.coth);
    }

    public Function exp() {
        return new Function(this, TypeFunction.exp);
    }

    public Function id() {
        return new Function(this, TypeFunction.id);
    }

    public Function lg() {
        return new Function(this, TypeFunction.lg);
    }

    public Function ln() {
        return new Function(this, TypeFunction.ln);
    }

    public Function sec() {
        return new Function(this, TypeFunction.sec);
    }

    public Function sech() {
        return new Function(this, TypeFunction.sech);
    }

    public Function sgn() {
        return new Function(this, TypeFunction.sgn);
    }

    public Function sin() {
        return new Function(this, TypeFunction.sin);
    }

    public Function sinh() {
        return new Function(this, TypeFunction.sinh);
    }

    public Function sqrt() {
        return new Function(this, TypeFunction.sqrt);
    }

    public Function tan() {
        return new Function(this, TypeFunction.tan);
    }

    public Function tanh() {
        return new Function(this, TypeFunction.tanh);
    }

    /*
     * Es folgen Methoden zur Ermittlung, ob der zugrundeliegende Ausdruck eine
     * Instanz einer speziellen Unterklasse von Expression mit speziellem Typ
     * ist.
     */
    public boolean isSum() {
        return this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.PLUS);
    }

    public boolean isDifference() {
        return this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.MINUS);
    }

    public boolean isProduct() {
        return this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.TIMES);
    }

    public boolean isQuotient() {
        return this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.DIV);
    }

    public boolean isPower() {
        return this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.POW);
    }

    public boolean isFunction() {
        return this instanceof Function;
    }

    public boolean isFunction(TypeFunction type) {
        return this instanceof Function && ((Function) this).getType().equals(type);
    }

    public boolean isNotSum() {
        return !(this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.PLUS));
    }

    public boolean isNotDifference() {
        return !(this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.MINUS));
    }

    public boolean isNotProduct() {
        return !(this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.TIMES));
    }

    public boolean isNotQuotient() {
        return !(this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.DIV));
    }

    public boolean isNotPower() {
        return !(this instanceof BinaryOperation && ((BinaryOperation) this).getType().equals(TypeBinary.POW));
    }

    /**
     * Einzelschritt: Triviale Vereinfachung.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyTrivial() throws EvaluationException;

    /**
     * Distributivgesetz für Konstanten: a*(b + c) = a*b + a*c für konstantes
     * rationales a.
     *
     * @throws EvaluationException
     */
    public abstract Expression expandRationalFactors() throws EvaluationException;

    /**
     * Anwendung des Distributivgesetz: a*(b + c) = a*b + a*c.
     *
     * @throws EvaluationException
     */
    public abstract Expression expand() throws EvaluationException;

    /**
     * Fasst Leitkoeffizienten in Brüchen/Differenzen zusammen.
     *
     * @throws EvaluationException
     */
    public abstract Expression reduceLeadingsCoefficients() throws EvaluationException;

    /**
     * Ordnet Ketten von + und von * nach rechts.
     *
     * @throws EvaluationException
     */
    public abstract Expression orderSumsAndProducts() throws EvaluationException;

    /**
     * Sortiert Summen und Differenzen zu einer grpßen Differenz (...)-(...).
     *
     * @throws EvaluationException
     */
    public abstract Expression orderDifferenceAndDivision() throws EvaluationException;

    /**
     * Sammelt in einem Produkt gleiche Ausdrücke zu einem einzigen Ausdruck.
     *
     * @throws EvaluationException
     */
    public abstract Expression collectProducts() throws EvaluationException;

    /**
     * Versucht in einer Summe möglichst viel zu faktorisieren.
     *
     * @throws EvaluationException
     */
    public abstract Expression factorizeInSums() throws EvaluationException;

    /**
     * Dasselbe wie factorizeInSums(), nur für Differenzen.
     *
     * @throws EvaluationException
     */
    public abstract Expression factorizeInDifferences() throws EvaluationException;

    /**
     * Versucht in einer Summe gleiche nichtkonstante Summanden mit
     * verschiedenen Koeffizienten zu sammeln.
     *
     * @throws EvaluationException
     */
    public abstract Expression factorizeRationalsInSums() throws EvaluationException;

    /**
     * Dasselbe wie factorizeRationalsInSums(), nur für Differenzen.
     *
     * @throws EvaluationException
     */
    public abstract Expression factorizeRationalsInDifferences() throws EvaluationException;

    /**
     * Kürzt Ausdrücke in einem Quotienten (...)-(...).
     *
     * @throws EvaluationException
     */
    public abstract Expression reduceQuotients() throws EvaluationException;

    /**
     * Vereinfacht Potenzen.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyPowers() throws EvaluationException;

    /**
     * Versucht STUR, Exponenten auszumultiplizieren, etwa (x^3)^5 = x^15, aber
     * auch (x^4)^(1/2) = x^2. Letzteres ist im Allgemeinen FALSCH, aber diese
     * Art von Umformung wird bei Substitution bei der Integration benötigt.
     *
     * @throws EvaluationException
     */
    public abstract Expression multiplyPowers() throws EvaluationException;

    /**
     * Beachtet eine Reihe vorgegebener Funktionalgleichungen.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyFunctionalRelations() throws EvaluationException;

    /**
     * Sammelt Logarithmen zu einem Logarithmus.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyCollectLogarithms() throws EvaluationException;

    /**
     * Schreibt Logarithmen von Produkten/Quotienten aus zu Summen/Differenzen
     * von Logarithmen.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyExpandLogarithms() throws EvaluationException;

    /**
     * Ersetzt Funktionen durch einfachere Funktionen (wichtig für Integrale!).
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyReplaceExponentialFunctionsByDefinitions() throws EvaluationException;

    /**
     * Ersetzt Funktionen durch einfachere Funktionen (wichtig für Integrale!).
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyReplaceTrigonometricalFunctionsByDefinitions() throws EvaluationException;

    /**
     * Führt eine Reihe von Vereinfachungen algebraischer Ausdrücke aus.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyAlgebraicExpressions() throws EvaluationException;

    /**
     * Führt eine Reihe von Vereinfachungen polynomieller Ausdrücke aus.
     *
     * @throws EvaluationException
     */
    public abstract Expression simplifyPolynomials() throws EvaluationException;

    /**
     * Standardvereinfachung allgemeiner Terme. Es wird solange iteriert, bis
     * sich nichts mehr ändert -> Der Ausdruck ist dann weitestgehend
     * vereinfacht.
     *
     * @throws EvaluationException
     */
    public Expression simplify() throws EvaluationException {

        try {
            Expression expr = this;

            Expression exprSimplified = expr.simplifyTrivial();
//            System.out.println(exprSimplified.writeExpression());
            exprSimplified = exprSimplified.orderDifferenceAndDivision();
            exprSimplified = exprSimplified.simplifyPowers();
            exprSimplified = exprSimplified.collectProducts();
            exprSimplified = exprSimplified.expandRationalFactors();
            exprSimplified = exprSimplified.factorizeRationalsInSums();
            exprSimplified = exprSimplified.factorizeRationalsInDifferences();
            exprSimplified = exprSimplified.factorizeInSums();
            exprSimplified = exprSimplified.factorizeInDifferences();
            exprSimplified = exprSimplified.reduceQuotients();
            exprSimplified = exprSimplified.reduceLeadingsCoefficients();
            exprSimplified = exprSimplified.simplifyAlgebraicExpressions();
            exprSimplified = exprSimplified.simplifyPolynomials();
            if (this.containsFunction()) {
                exprSimplified = exprSimplified.simplifyFunctionalRelations();
                exprSimplified = exprSimplified.simplifyCollectLogarithms();
            }
            exprSimplified = exprSimplified.orderSumsAndProducts();

            while (!expr.equals(exprSimplified)) {
                expr = exprSimplified.copy();

                exprSimplified = exprSimplified.simplifyTrivial();
                exprSimplified = exprSimplified.orderDifferenceAndDivision();
                exprSimplified = exprSimplified.simplifyPowers();
                exprSimplified = exprSimplified.collectProducts();
                exprSimplified = exprSimplified.expandRationalFactors();
                exprSimplified = exprSimplified.factorizeRationalsInSums();
                exprSimplified = exprSimplified.factorizeRationalsInDifferences();
                exprSimplified = exprSimplified.factorizeInSums();
                exprSimplified = exprSimplified.factorizeInDifferences();
                exprSimplified = exprSimplified.reduceQuotients();
                exprSimplified = exprSimplified.reduceLeadingsCoefficients();
                exprSimplified = exprSimplified.simplifyAlgebraicExpressions();
                exprSimplified = exprSimplified.simplifyPolynomials();
                if (this.containsFunction()) {
                    exprSimplified = exprSimplified.simplifyFunctionalRelations();
                    exprSimplified = exprSimplified.simplifyCollectLogarithms();
                }
                exprSimplified = exprSimplified.orderSumsAndProducts();
            }

            /*
             Nun kann nichts mehr gekürzt werden. Zum Schluss nur noch
             triviale Umformungen vornehmen und Terme endgültig ordnen.
             */
            expr = exprSimplified.copy();
            exprSimplified = exprSimplified.simplifyTrivial();
            exprSimplified = exprSimplified.orderSumsAndProducts();
            exprSimplified = exprSimplified.orderDifferenceAndDivision();

            while (!expr.equals(exprSimplified)) {
                expr = exprSimplified.copy();

                exprSimplified = exprSimplified.simplifyTrivial();
                exprSimplified = exprSimplified.orderSumsAndProducts();
                exprSimplified = exprSimplified.orderDifferenceAndDivision();
            }

            return exprSimplified;
        } catch (java.lang.StackOverflowError e) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_Expression_STACK_OVERFLOW"));
        }

    }

    /**
     * Spezielle Vereinfachung allgemeiner Terme. Es wird solange iteriert, bis
     * sich nichts mehr ändert -> Der Ausdruck ist dann weitestgehend
     * vereinfacht.
     *
     * @throws EvaluationException
     */
    public Expression simplify(HashSet<TypeSimplify> simplifyTypes) throws EvaluationException {

        try {
            Expression expr = this;
            Expression exprSimplified = expr;

            if (simplifyTypes.contains(TypeSimplify.simplify_trivial)) {
                exprSimplified = exprSimplified.simplifyTrivial();
            }
            if (simplifyTypes.contains(TypeSimplify.sort_difference_and_division)) {
                exprSimplified = exprSimplified.orderDifferenceAndDivision();
            }
            if (simplifyTypes.contains(TypeSimplify.expand)) {
                exprSimplified = exprSimplified.expand();
            }
            if (simplifyTypes.contains(TypeSimplify.expand_rational_factors)) {
                exprSimplified = exprSimplified.expandRationalFactors();
            }
            if (simplifyTypes.contains(TypeSimplify.simplify_powers)) {
                exprSimplified = exprSimplified.simplifyPowers();
            }
            if (simplifyTypes.contains(TypeSimplify.multiply_powers)) {
                exprSimplified = exprSimplified.multiplyPowers();
            }
            if (simplifyTypes.contains(TypeSimplify.collect_products)) {
                exprSimplified = exprSimplified.collectProducts();
            }
            if (simplifyTypes.contains(TypeSimplify.factorize_rationals_in_sums)) {
                exprSimplified = exprSimplified.factorizeRationalsInSums();
            }
            if (simplifyTypes.contains(TypeSimplify.factorize_in_sums)) {
                exprSimplified = exprSimplified.factorizeInSums();
            }
            if (simplifyTypes.contains(TypeSimplify.reduce_quotients)) {
                exprSimplified = exprSimplified.reduceQuotients();
            }
            if (simplifyTypes.contains(TypeSimplify.factorize_rationals_in_differences)) {
                exprSimplified = exprSimplified.factorizeRationalsInDifferences();
            }
            if (simplifyTypes.contains(TypeSimplify.factorize_in_differences)) {
                exprSimplified = exprSimplified.factorizeInDifferences();
            }
            if (simplifyTypes.contains(TypeSimplify.reduce_leadings_coefficients)) {
                exprSimplified = exprSimplified.reduceLeadingsCoefficients();
            }
            if (simplifyTypes.contains(TypeSimplify.simplify_algebraic_expressions)) {
                exprSimplified = exprSimplified.simplifyAlgebraicExpressions();
            }
            if (simplifyTypes.contains(TypeSimplify.simplify_polynomials)) {
                exprSimplified = exprSimplified.simplifyPolynomials();
            }
            if (this.containsFunction()) {
                if (simplifyTypes.contains(TypeSimplify.simplify_functional_relations)) {
                    exprSimplified = exprSimplified.simplifyFunctionalRelations();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_replace_exponential_functions_by_definitions)) {
                    exprSimplified = exprSimplified.simplifyReplaceExponentialFunctionsByDefinitions();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions)) {
                    exprSimplified = exprSimplified.simplifyReplaceTrigonometricalFunctionsByDefinitions();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_collect_logarithms)) {
                    exprSimplified = exprSimplified.simplifyCollectLogarithms();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_expand_logarithms)) {
                    exprSimplified = exprSimplified.simplifyExpandLogarithms();
                }
            }
            if (simplifyTypes.contains(TypeSimplify.order_sums_and_products)) {
                exprSimplified = exprSimplified.orderSumsAndProducts();
            }

            while (!expr.equals(exprSimplified)) {
                expr = exprSimplified.copy();

                if (simplifyTypes.contains(TypeSimplify.simplify_trivial)) {
                    exprSimplified = exprSimplified.simplifyTrivial();
                }
                if (simplifyTypes.contains(TypeSimplify.sort_difference_and_division)) {
                    exprSimplified = exprSimplified.orderDifferenceAndDivision();
                }
                if (simplifyTypes.contains(TypeSimplify.expand)) {
                    exprSimplified = exprSimplified.expand();
                }
                if (simplifyTypes.contains(TypeSimplify.expand_rational_factors)) {
                    exprSimplified = exprSimplified.expandRationalFactors();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_powers)) {
                    exprSimplified = exprSimplified.simplifyPowers();
                }
                if (simplifyTypes.contains(TypeSimplify.multiply_powers)) {
                    exprSimplified = exprSimplified.multiplyPowers();
                }
                if (simplifyTypes.contains(TypeSimplify.collect_products)) {
                    exprSimplified = exprSimplified.collectProducts();
                }
                if (simplifyTypes.contains(TypeSimplify.factorize_rationals_in_sums)) {
                    exprSimplified = exprSimplified.factorizeRationalsInSums();
                }
                if (simplifyTypes.contains(TypeSimplify.factorize_in_sums)) {
                    exprSimplified = exprSimplified.factorizeInSums();
                }
                if (simplifyTypes.contains(TypeSimplify.reduce_quotients)) {
                    exprSimplified = exprSimplified.reduceQuotients();
                }
                if (simplifyTypes.contains(TypeSimplify.factorize_rationals_in_differences)) {
                    exprSimplified = exprSimplified.factorizeRationalsInDifferences();
                }
                if (simplifyTypes.contains(TypeSimplify.factorize_in_differences)) {
                    exprSimplified = exprSimplified.factorizeInDifferences();
                }
                if (simplifyTypes.contains(TypeSimplify.reduce_leadings_coefficients)) {
                    exprSimplified = exprSimplified.reduceLeadingsCoefficients();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_algebraic_expressions)) {
                    exprSimplified = exprSimplified.simplifyAlgebraicExpressions();
                }
                if (simplifyTypes.contains(TypeSimplify.simplify_polynomials)) {
                    exprSimplified = exprSimplified.simplifyPolynomials();
                }
                if (this.containsFunction()) {
                    if (simplifyTypes.contains(TypeSimplify.simplify_functional_relations)) {
                        exprSimplified = exprSimplified.simplifyFunctionalRelations();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_replace_exponential_functions_by_definitions)) {
                        exprSimplified = exprSimplified.simplifyReplaceExponentialFunctionsByDefinitions();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_replace_trigonometrical_functions_by_definitions)) {
                        exprSimplified = exprSimplified.simplifyReplaceTrigonometricalFunctionsByDefinitions();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_collect_logarithms)) {
                        exprSimplified = exprSimplified.simplifyCollectLogarithms();
                    }
                    if (simplifyTypes.contains(TypeSimplify.simplify_expand_logarithms)) {
                        exprSimplified = exprSimplified.simplifyExpandLogarithms();
                    }
                }
                if (simplifyTypes.contains(TypeSimplify.order_sums_and_products)) {
                    exprSimplified = exprSimplified.orderSumsAndProducts();
                }
            }

            /*
             Nun kann nichts mehr gekürzt werden. Zum Schluss nur noch
             triviale Umformungen vornehmen und Terme endgültig ordnen.
             */
            expr = exprSimplified.copy();
            exprSimplified = exprSimplified.simplifyTrivial();
            exprSimplified = exprSimplified.orderSumsAndProducts();
            exprSimplified = exprSimplified.orderDifferenceAndDivision();

            while (!expr.equals(exprSimplified)) {
                expr = exprSimplified.copy();

                exprSimplified = exprSimplified.simplifyTrivial();
                exprSimplified = exprSimplified.orderSumsAndProducts();
                exprSimplified = exprSimplified.orderDifferenceAndDivision();
            }

            return exprSimplified;
        } catch (java.lang.StackOverflowError e) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_Expression_STACK_OVERFLOW"));
        }

    }

}
