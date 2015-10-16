package computation;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeBinary;
import expressionbuilder.TypeFunction;
import expressionbuilder.Variable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import matrixexpressionbuilder.Matrix;
import matrixexpressionbuilder.MatrixExpression;
import translator.Translator;

public abstract class AnalysisMethods {

    /**
     * Gibt die Summe f(var = k_0) + f(var = k_0 + 1) + ... + f(var = k_1)
     * zurück.
     *
     * @throws EvaluationException
     */
    public static Expression sum(Expression f, String var, int k_0, int k_1) {

        // Trivialer Fall: k_1 < k_0
        if (k_1 < k_0) {
            return Expression.ZERO;
        }

        // Trivialer Fall: expr hängt von var nicht ab.
        if (!f.contains(var)) {
            return new Constant(k_1 - k_0 + 1).mult(f);
        }

        /*
         Zunächst soll anhand von etwa 5 Gliedern getestet werden, ob man
         Glieder zusammenfassen kann. Konkret: Man wendet simplify() auf die
         entweder auf die ersten 5 Glieder an, falls k_0 <= -6, oder auf die
         letzten 5 Glieder, falls k_1 >= 6. Falls das Ergebnis ungleich dem
         vorherigen ist -> bei jedem neu hinzugekommenen Glied erneut
         vereinfachen. Ansonsten: nicht vereinfachen. (Später in der
         Hauptprozedur simplify() wird die gesamte Summe erneut vereinfacht).
         GRUND: Falls man nichts zusammenfassen kann, so dauert die gliedweise
         Anwendung von simplify() SEHR LANGE.
         */
        Expression result;
        Expression currentSummand;

        if (k_1 - k_0 >= 5) {

            // Vorab-Test auf die Möglichkeit einer "Zwischendurch-Vereinfachung" (s. o.)
            result = f.replaceVariable(var, new Constant(k_1));
            for (int i = k_1 - 1; i > k_1 - 5; i--) {
                currentSummand = f.replaceVariable(var, new Constant(i));
                result = currentSummand.add(result);
            }

            try {
                if (!result.equivalent(result.simplify())) {

                    for (int i = k_1 - 5; i >= k_0; i--) {
                        currentSummand = f.replaceVariable(var, new Constant(i));
                        result = currentSummand.add(result).simplify();
                    }

                } else {

                    for (int i = k_1 - 5; i >= k_0; i--) {
                        currentSummand = f.replaceVariable(var, new Constant(i));
                        result = currentSummand.add(result);
                    }

                }

                return result;
            } catch (EvaluationException e) {
            }

        }

        if (k_1 >= k_0) {
            result = f.replaceVariable(var, new Constant(k_0));
            for (int i = k_0 + 1; i <= k_1; i++) {
                result = result.add(f.replaceVariable(var, new Constant(i)));
            }
            return result;
        }

        return Expression.ZERO;

    }

    /**
     * Gibt die Summe f(var = k_0) + f(var = k_0 + 1) + ... + f(var = k_1)
     * zurück.
     *
     * @throws EvaluationException
     */
    public static MatrixExpression sum(MatrixExpression f, String var, int k_0, int k_1) {

        // Trivialer Fall: k_1 < k_0
        if (k_1 < k_0) {
            return new Matrix(Expression.ZERO);
        }

        // Trivialer Fall: expr hängt von var nicht ab.
        if (!f.contains(var)) {
            return new Matrix(new Constant(k_1 - k_0 + 1)).mult(f);
        }

        /*
         Zunächst soll anhand von etwa 5 Gliedern getestet werden, ob man
         Glieder zusammenfassen kann. Konkret: Man wendet simplify() auf die
         entweder auf die ersten 5 Glieder an, falls k_0 <= -6, oder auf die
         letzten 5 Glieder, falls k_1 >= 6. Falls das Ergebnis ungleich dem
         vorherigen ist -> bei jedem neu hinzugekommenen Glied erneut
         vereinfachen. Ansonsten: nicht vereinfachen. (Später in der
         Hauptprozedur simplify() wird die gesamte Summe erneut vereinfacht).
         GRUND: Falls man nichts zusammenfassen kann, so dauert die gliedweise
         Anwendung von simplify() SEHR LANGE.
         */
        MatrixExpression result;
        MatrixExpression currentSummand;

        if ((k_0 <= -6 || k_1 >= 6) && (k_1 - k_0 >= 5)) {

            // Vorab-Test auf die Möglichkeit einer "Zwischendurch-Vereinfachung" (s. o.)
            if (k_0 <= -6) {
                result = f.replaceVariable(var, new Constant(k_0));
                for (int i = k_0 + 1; i < k_0 + 5; i++) {
                    currentSummand = f.replaceVariable(var, new Constant(i));
                    result = result.add(currentSummand);
                }
            } else {
                result = f.replaceVariable(var, new Constant(k_1));
                for (int i = k_1 - 1; i > k_1 - 5; i--) {
                    currentSummand = f.replaceVariable(var, new Constant(i));
                    result = currentSummand.add(result);
                }
            }

            try {
                if (!result.equivalent(result.simplify())) {

                    for (int i = k_0; i <= k_1; i++) {
                        currentSummand = f.replaceVariable(var, new Constant(i));
                        if (i == k_0) {
                            result = currentSummand;
                        } else {
                            result = result.add(currentSummand).simplify();
                        }
                    }

                } else {

                    for (int i = k_0; i <= k_1; i++) {
                        currentSummand = f.replaceVariable(var, new Constant(i));
                        if (i == k_0) {
                            result = currentSummand;
                        } else {
                            result = result.add(currentSummand);
                        }
                    }

                }

                return result;
            } catch (EvaluationException e) {
            }

        }

        if (k_1 >= k_0) {
            result = f.replaceVariable(var, new Constant(k_0));
            for (int i = k_0 + 1; i <= k_1; i++) {
                currentSummand = f.replaceVariable(var, new Constant(i));
                result = result.add(currentSummand);
            }
            return result;
        }

        return new Matrix(Expression.ZERO);

    }

    /**
     * Bildet das Produkt f(var = k_0) * f(var = k_0 + 1) * ... * f(var = k_1).
     *
     * @throws EvaluationException
     */
    public static Expression prod(Expression f, String var, int k_0, int k_1) {

        // Trivialer Fall: k_1 < k_0
        if (k_1 < k_0) {
            return Expression.ONE;
        }

        // Trivialer Fall: expr hängt von var nicht ab.
        if (!f.contains(var)) {
            return f.pow(k_1 - k_0 + 1);
        }

        /*
         Zunächst soll anhand von etwa 5 Gliedern getestet werden, ob man
         Glieder zusammenfassen kann. Konkret: Man wendet simplify() auf die
         entweder auf die ersten 5 Glieder an, falls k_0 <= -6, oder auf die
         letzten 5 Glieder, falls k_1 >= 6. Falls das Ergebnis ungleich dem
         vorherigen ist -> bei jedem neu hinzugekommenen Glied erneut
         vereinfachen. Ansonsten: nicht vereinfachen. (Später in der
         Hauptprozedur simplify() wird die gesamte Summe erneut vereinfacht).
         GRUND: Falls man nichts zusammenfassen kann, so dauert die gliedweise
         Anwendung von simplify() SEHR LANGE.
         */
        Expression result;
        Expression currentFactor;

        if ((k_0 <= -6 || k_1 >= 6) && (k_1 - k_0 >= 5)) {

            // Vorab-Test auf die Möglichkeit einer "Zwischendurch-Vereinfachung" (s. o.)
            if (k_0 <= -6) {
                result = f.replaceVariable(var, new Constant(k_0));
                for (int i = k_0 + 1; i < k_0 + 5; i++) {
                    currentFactor = f.replaceVariable(var, new Constant(i));
                    result = result.mult(currentFactor);
                }
            } else {
                result = f.replaceVariable(var, new Constant(k_1));
                for (int i = k_1 - 1; i > k_1 - 5; i--) {
                    currentFactor = f.replaceVariable(var, new Constant(i));
                    result = currentFactor.mult(result);
                }
            }

            try {
                if (!result.equivalent(result.simplify())) {

                    for (int i = k_0; i <= k_1; i++) {
                        currentFactor = f.replaceVariable(var, new Constant(i));
                        if (i == k_0) {
                            result = currentFactor;
                        } else {
                            result = result.mult(currentFactor).simplify();
                        }
                    }

                } else {

                    for (int i = k_0; i <= k_1; i++) {
                        currentFactor = f.replaceVariable(var, new Constant(i));
                        if (i == k_0) {
                            result = currentFactor;
                        } else {
                            result = result.mult(currentFactor);
                        }
                    }

                }

                return result;
            } catch (EvaluationException e) {
            }

        }

        if (k_1 >= k_0) {
            result = f.replaceVariable(var, new Constant(k_0));
            for (int i = k_0 + 1; i <= k_1; i++) {
                currentFactor = f.replaceVariable(var, new Constant(i));
                result = result.mult(currentFactor);
            }
            return result;
        }

        return Expression.ONE;

    }

    /**
     * Bildet das Produkt f(var = k_0) * f(var = k_0 + 1) * ... * f(var = k_1).
     *
     * @throws EvaluationException
     */
    public static MatrixExpression prod(MatrixExpression f, String var, int k_0, int k_1) {

        // Trivialer Fall: k_1 < k_0
        if (k_1 < k_0) {
            return new Matrix(Expression.ONE);
        }

        // Trivialer Fall: expr hängt von var nicht ab.
        if (!f.contains(var)) {
            return f.pow(k_1 - k_0 + 1);
        }

        /*
         Zunächst soll anhand von etwa 5 Gliedern getestet werden, ob man
         Glieder zusammenfassen kann. Konkret: Man wendet simplify() auf die
         entweder auf die ersten 5 Glieder an, falls k_0 <= -6, oder auf die
         letzten 5 Glieder, falls k_1 >= 6. Falls das Ergebnis ungleich dem
         vorherigen ist -> bei jedem neu hinzugekommenen Glied erneut
         vereinfachen. Ansonsten: nicht vereinfachen. (Später in der
         Hauptprozedur simplify() wird die gesamte Summe erneut vereinfacht).
         GRUND: Falls man nichts zusammenfassen kann, so dauert die gliedweise
         Anwendung von simplify() SEHR LANGE.
         */
        MatrixExpression result;
        MatrixExpression currentFactor;

        if ((k_0 <= -6 || k_1 >= 6) && (k_1 - k_0 >= 5)) {

            // Vorab-Test auf die Möglichkeit einer "Zwischendurch-Vereinfachung" (s. o.)
            if (k_0 <= -6) {
                result = f.replaceVariable(var, new Constant(k_0));
                for (int i = k_0 + 1; i < k_0 + 5; i++) {
                    currentFactor = f.replaceVariable(var, new Constant(i));
                    result = result.mult(currentFactor);
                }
            } else {
                result = f.replaceVariable(var, new Constant(k_1));
                for (int i = k_1 - 1; i > k_1 - 5; i--) {
                    currentFactor = f.replaceVariable(var, new Constant(i));
                    result = currentFactor.mult(result);
                }
            }

            try {
                if (!result.equivalent(result.simplify())) {

                    for (int i = k_0; i <= k_1; i++) {
                        currentFactor = f.replaceVariable(var, new Constant(i));
                        if (i == k_0) {
                            result = currentFactor;
                        } else {
                            result = result.mult(currentFactor).simplify();
                        }
                    }

                } else {

                    for (int i = k_0; i <= k_1; i++) {
                        currentFactor = f.replaceVariable(var, new Constant(i));
                        if (i == k_0) {
                            result = currentFactor;
                        } else {
                            result = result.mult(currentFactor);
                        }
                    }

                }

                return result;
            } catch (EvaluationException e) {
            }

        }

        if (k_1 >= k_0) {
            result = f.replaceVariable(var, new Constant(k_0));
            for (int i = k_0 + 1; i <= k_1; i++) {
                currentFactor = f.replaceVariable(var, new Constant(i));
                result = result.mult(currentFactor);
            }
            return result;
        }

        return new Matrix(Expression.ONE);

    }

    /**
     * Ermittelt das Taylorpolynom vom Grad degree von y (mit Entwicklungsstelle
     * x_0), welches die DGL y^(n) = f erfüllt. VORAUSSETZUNG: expr enthält
     * höchstens zwei Variablen: var und noch eine andere; alles andere wurden
     * beim Compilieren bereits aussortiert. Außerdem: k >= 0, ord >= 1.
     *
     * @throws EvaluationException
     */
    public static Expression getTaylorPolynomialFromDifferentialEquation(Expression f, String argumentVar, String functionVar,
            int ord, Expression x_0, Expression[] y_0, int degree) throws EvaluationException {

        if (degree < 0) {
            return Expression.ZERO;
        }

        BigDecimal[] factorial = new BigDecimal[degree + 1];
        factorial[0] = BigDecimal.ONE;
        for (int i = 1; i <= degree; i++) {
            factorial[i] = factorial[i - 1].multiply(new BigDecimal(i));
        }

        // Array für die Koeffizienten des Taylorpolynoms
        Expression[] coefficient = new Expression[degree + 1];
        Expression taylorPolynomial = Expression.ZERO;

        if (degree <= y_0.length - 1) {

            for (int i = 0; i <= degree; i++) {
                coefficient[i] = y_0[i];

                if (coefficient[i].containsIndefiniteIntegral()) {
                    /*
                     Falls eines der Koeffizienten unbestimmte Integrale
                     enthält, welche NICHT explizit berechnet werden konnten,
                     so kann das Taylorpolynom ebenfalls nicht explizit
                     angegeben werden (da das Taylorpolynom etwa
                     Integrationskonstanten enthalten kann).
                     */
                    throw new EvaluationException(Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED_1")
                            + functionVar + Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED_2")
                            + ord + Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED_3")
                            + f.writeExpression()
                            + Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED_4"));
                }

            }
            // Ergebnis bereits ausgeben.
            for (int i = 0; i <= degree; i++) {
                taylorPolynomial = taylorPolynomial.add(coefficient[i].mult(Variable.create(argumentVar).sub(x_0).pow(i)).div(factorial[i]));
            }
            return taylorPolynomial.simplify();

        } else {

            for (int i = 0; i <= ord - 1; i++) {
                coefficient[i] = y_0[i];
                if (coefficient[i].containsIndefiniteIntegral()) {
                    /*
                     Falls eines der Koeffizienten unbestimmte Integrale
                     enthält, welche NICHT explizit berechnet werden konnten,
                     so kann das Taylorpolynom ebenfalls nicht explizit
                     angegeben werden (da das Taylorpolynom etwa
                     Integrationskonstanten enthalten kann).
                     */
                    throw new EvaluationException(Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED_1")
                            + functionVar + Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED_2")
                            + ord + Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED_3")
                            + f.writeExpression()
                            + Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED_4"));
                }
            }

        }

        Expression[] df = new Expression[degree - ord + 1];
        df[0] = f.simplify(); //df[0] = y^(ord) -> df[i] = y^(ord+i) für alle i >= 0.

        /*
         Angenommen, functionVar == "y". Dann nimmt functionVarWithPrimes
         nacheinander die Werte "y", "y'", "y''", "y'''" etc. an.
         */
        String functionVarWithPrimes;
        for (int i = ord; i <= degree; i++) {
            functionVarWithPrimes = functionVar;
            coefficient[i] = df[i - ord].replaceVariable(argumentVar, x_0);
            if (coefficient[i].containsIndefiniteIntegral()) {
                /*
                 Falls eines der Koeffizienten unbestimmte Integrale enthält,
                 welche NICHT explizit berechnet werden konnten, so kann das
                 Taylorpolynom ebenfalls nicht explizit angegeben werden (da
                 das Taylorpolynom etwa Integrationskonstanten enthalten
                 kann).
                 */
                throw new EvaluationException(Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED_1")
                        + functionVar + Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED_2")
                        + ord + Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED_3")
                        + f.writeExpression()
                        + Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED_4"));
            }
            for (int j = 0; j < i; j++) {
                coefficient[i] = coefficient[i].replaceVariable(functionVarWithPrimes, coefficient[j]);
                functionVarWithPrimes = functionVarWithPrimes + "'";
            }
            if (i < degree) {
                df[i - ord + 1] = df[i - ord].diffDifferentialEquation(argumentVar).simplify();
            }
            coefficient[i] = coefficient[i].simplify();
        }

        // Taylorpolynom berechnen und ausgeben.
        for (int i = 0; i <= degree; i++) {
            taylorPolynomial = taylorPolynomial.add(coefficient[i].mult(Variable.create(argumentVar).sub(x_0).pow(i)).div(factorial[i]));
        }
        return taylorPolynomial.simplify();

    }

    /**
     * Ermittelt das Taylorpolynom vom Grad degree von f (mit Entwicklungsstelle
     * x_0). VORAUSSETZUNG: k >= 0, ord >= 1.
     *
     * @throws EvaluationException
     */
    public static Expression getTaylorPolynomial(Expression f, String var, Expression x_0, int degree) throws EvaluationException {

        if (degree < 0) {
            return Expression.ZERO;
        }

        //Entwicklungspunkt ist a und die Variable var, nach der entwickelt wird, hat den Wert a.
        f = f.simplify();
        Expression taylorPolynomial = Expression.ZERO;

        Expression[] coefficient = new Expression[degree + 1];
        BigDecimal[] factorial = new BigDecimal[degree + 1];
        factorial[0] = BigDecimal.ONE;
        coefficient[0] = f.replaceVariable(var, x_0).simplify();

        if (coefficient[0].containsIndefiniteIntegral()) {
            /*
             Falls eines der Koeffizienten unbestimmte Integrale enthält,
             welche NICHT explizit berechnet werden konnten, so kann das
             Taylorpolynom ebenfalls nicht explizit angegeben werden (da das
             Taylorpolynom etwa Integrationskonstanten enthalten kann).
             */
            throw new EvaluationException(Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_FUNCTION_CANNOT_BE_COMPUTED_1")
                    + f.writeExpression()
                    + Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_FUNCTION_CANNOT_BE_COMPUTED_2"));
        }

        for (int i = 1; i <= degree; i++) {
            f = f.diff(var);
            f = f.simplify();
            factorial[i] = factorial[i - 1].multiply(new BigDecimal(i));
            coefficient[i] = f.replaceVariable(var, x_0).simplify();

            if (coefficient[i].containsIndefiniteIntegral()) {
                /*
                 Falls eines der Koeffizienten unbestimmte Integrale enthält,
                 welche NICHT explizit berechnet werden konnten, so kann das
                 Taylorpolynom ebenfalls nicht explizit angegeben werden (da
                 das Taylorpolynom etwa Integrationskonstanten enthalten
                 kann).
                 */
                throw new EvaluationException(Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_FUNCTION_CANNOT_BE_COMPUTED_1")
                        + f.writeExpression()
                        + Translator.translateExceptionMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_FUNCTION_CANNOT_BE_COMPUTED_2"));
            }

        }

        for (int i = 0; i <= degree; i++) {
            taylorPolynomial = taylorPolynomial.add(coefficient[i].mult(Variable.create(var).sub(x_0).pow(i)).div(factorial[i]));
        }
        return taylorPolynomial.simplify();

    }

    /**
     * Ermittelt die Gleichung des Tangentialraumes an den Graphen der Funktion
     * Y = f. VORAUSSETZUNG: x_0.size = Anzahl der Variablen in expr. Dies ist
     * aber stets der Fall, da es andernfalls bereits im Vorfeld aussortiert
     * wurde.
     *
     * @throws EvaluationException
     */
    public static Expression getTangentSpace(Expression f, HashMap<String, Expression> x_0)
            throws EvaluationException {

        HashSet vars = new HashSet();
        f.addContainedVars(vars);

        Expression result = new Constant(BigDecimal.ZERO);
        String var;
        Expression factor;

        Iterator iter;

        // Alle Variablen auf die Werte setzen, die dem Punkt x_0 entsprechen.
        iter = vars.iterator();
        for (int i = 0; i < vars.size(); i++) {
            var = (String) iter.next();
            Variable.setPreciseExpression(var, x_0.get(var));
            Variable.setPrecise(var, true);
        }

        iter = vars.iterator();
        try {
            for (int i = 0; i < vars.size(); i++) {
                var = (String) iter.next();
                factor = f.diff(var);
                factor = factor.evaluate(vars).simplify();
                result = new BinaryOperation(result, new BinaryOperation(factor,
                        new BinaryOperation(Variable.create(var), x_0.get(var), TypeBinary.MINUS), TypeBinary.TIMES),
                        TypeBinary.PLUS);
            }

            f = f.evaluate(vars).simplify();
            result = new BinaryOperation(f, result, TypeBinary.PLUS).simplify();
            return result;
        } catch (EvaluationException e) {
            throw new EvaluationException(Translator.translateExceptionMessage("CC_AnalysisMethods_TANGENT_SPACE_CANNOT_BE_COMPUTED"));
        }

    }

    /**
     * Ermittelt die ersten n Dezimalstellen der Eulerschen Konstante e.
     *
     * @throws ExpressionException
     */
    public static BigDecimal getDigitsOfE(int n) throws ExpressionException {

        if (n > computationbounds.ComputationBounds.getBound("Bound_MAX_DIGITS_OF_E")) {
            throw new ExpressionException(Translator.translateExceptionMessage("CC_AnalysisMethods_ENTER_A_SMALLER_NUMBER_OF_DIGITS"));
        }

        BigDecimal e = BigDecimal.ONE;

        // bound = 10^(-n - 5). Zusätzliche 5 Stellen sind für Rundungsfehler gedacht.
        BigDecimal bound = BigDecimal.ONE.divide(BigDecimal.TEN.pow(n + 5));
        /*
         summandOfSeriesForE ist der aktuelle Summand in der e-Reihe e = 1/0!
         + 1/1! + 1/2! + 1/3! + ...
         */
        BigDecimal summandOfSeriesForE = BigDecimal.ONE;

        int i = 1;
        while (summandOfSeriesForE.compareTo(bound) >= 0) {
            summandOfSeriesForE = summandOfSeriesForE.divide(BigDecimal.valueOf(i), n + 5, BigDecimal.ROUND_HALF_UP);
            e = e.add(summandOfSeriesForE);
            i++;
        }

        //Auf n Stellen runden.
        e = e.setScale(n, BigDecimal.ROUND_HALF_UP);

        return e;

    }

    /**
     * Ermittelt die ersten n Dezimalstellen der Kreiszahl pi.
     *
     * @throws ExpressionException
     */
    public static BigDecimal getDigitsOfPi(int n) throws ExpressionException {

        if (n > computationbounds.ComputationBounds.getBound("Bound_MAX_DIGITS_OF_PI")) {
            throw new ExpressionException(Translator.translateExceptionMessage("CC_AnalysisMethods_ENTER_A_SMALLER_NUMBER_OF_DIGITS"));
        }

        BigDecimal pi = BigDecimal.ONE.divide(BigDecimal.valueOf(2));

        // bound = 10^(-n - 5). Zusätzliche 5 Stellen sind für Rundungsfehler gedacht.
        BigDecimal bound = BigDecimal.ONE.divide(BigDecimal.TEN.pow(n + 5));
        /*
         summandOfSeriesForPi ist der aktuelle Summand in der pi-Reihe pi/6 =
         arcsin(1/2). Die Summanden werden hier rekursiv berechnet.
         */
        BigDecimal summandOfSeriesForPi = BigDecimal.ONE.divide(BigDecimal.valueOf(2));

        int i = 0;
        while (summandOfSeriesForPi.compareTo(bound) >= 0) {
            summandOfSeriesForPi = summandOfSeriesForPi.multiply(BigDecimal.valueOf(2 * i + 1));
            summandOfSeriesForPi = summandOfSeriesForPi.divide(BigDecimal.valueOf(2 * i + 3), n + 5, BigDecimal.ROUND_HALF_UP);
            summandOfSeriesForPi = summandOfSeriesForPi.multiply(BigDecimal.valueOf(2 * i + 1));
            summandOfSeriesForPi = summandOfSeriesForPi.divide(BigDecimal.valueOf(8 * i + 8), n + 5, BigDecimal.ROUND_HALF_UP);
            pi = pi.add(summandOfSeriesForPi);
            i++;
        }

        //Auf n Stellen runden.
        pi = pi.multiply(BigDecimal.valueOf(6));
        pi = pi.setScale(n, BigDecimal.ROUND_HALF_UP);

        return pi;

    }

    /**
     * Approximation der Gammafunktion. Die Integration wird nur für 1 <= s < 2
     * verwendet (da dann der Fehler <= 1E-16 ist), ansonsten die
     * Funktionalgleichung.
     *
     * @throws EvaluationException
     */
    public static double Gamma(double s) throws EvaluationException {

        if (s >= 171) {
            /*
             Für s >= 171 ist Gamma(s) >= 1E309 -> NaN zurückgeben; dies führt
             später automatisch zur Erzeugung eines Fehlers im
             Apprximationsmodus.
             */
            return Double.NaN;
        }
        if (s < -10000) {
            // Technisch zu schwer zu approximieren.
            return Double.NaN;
        }

        if (s >= 1 && s < 2) {
            Expression expr = Variable.create("x").pow(new Constant(s - 1)).mult(new Function(
                    Expression.MINUS_ONE.mult(Variable.create("x")), TypeFunction.exp));
            return NumericalMethods.integrateBySimpson(expr, "x", 0, 40, 100000);
        } else {
            /*
             Gilt !(s >= 1 && s < 2), wird die Funktionalgleichung Gamma(s +1) 
             = s * Gamma(s) ausgenutzt, um das Argument wieder in den
             Bereich [1, 2) zu bekommen. Der Wert der Gammafunktion von diesem
             Argument wird dann wie oben berechnet.
             */
            if (s >= 2) {
                int k = (int) Math.floor(s) - 1;
                double factor = 1;
                for (int i = 1; i <= k; i++) {
                    factor = factor * (s - i);
                }
                return factor * Gamma(s - k);
            } else {
                int k;
                if (-s == Math.floor(-s)) {
                    k = (int) Math.floor(-s + 1);
                } else {
                    k = (int) Math.floor(-s + 1) + 1;
                }

                double factor = 1;
                for (int i = 0; i < k; i++) {
                    factor = factor * (s + i);
                }
                return Gamma(s + k) / factor;
            }
        }

    }

}
