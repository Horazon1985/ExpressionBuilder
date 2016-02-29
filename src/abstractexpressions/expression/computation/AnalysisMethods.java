package abstractexpressions.expression.computation;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.PI;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.TypeOperator;
import abstractexpressions.expression.classes.Variable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import java.awt.Dimension;
import java.math.BigInteger;
import lang.translator.Translator;
import notations.NotationLoader;

public abstract class AnalysisMethods {

    /**
     * Gibt die Summe f(var = k_0) + f(var = k_0 + 1) + ... + f(var = k_1)
     * zurück.
     *
     * @throws EvaluationException
     */
    public static Expression sum(Expression f, String var, BigInteger k_0, BigInteger k_1) {

        int difference = k_1.subtract(k_0).intValue();

        // Trivialer Fall: k_1 < k_0
        if (difference < 0) {
            return Expression.ZERO;
        }

        // Trivialer Fall: expr hängt von var nicht ab.
        if (!f.contains(var)) {
            return new Constant(difference + 1).mult(f);
        }

        Expression result = ZERO;
        for (int i = difference; i >= 0; i--) {
            result = f.replaceVariable(var, new Constant(k_0.add(BigInteger.valueOf(i)))).add(result);
        }
        return result;

    }

    /**
     * Gibt die Summe f(var = k_0) + f(var = k_0 + 1) + ... + f(var = k_1)
     * zurück.
     *
     * @throws EvaluationException
     */
    public static MatrixExpression sum(MatrixExpression f, String var, BigInteger k_0, BigInteger k_1) {

        int difference = k_1.subtract(k_0).intValue();

        // Trivialer Fall: k_1 < k_0
        if (difference < 0) {
            try {
                Dimension dim = f.getDimension();
                return MatrixExpression.getZeroMatrix(dim.height, dim.width);
            } catch (EvaluationException e) {
                return new Matrix(Expression.ZERO);
            }
        }

        // Trivialer Fall: expr hängt von var nicht ab.
        if (!f.contains(var)) {
            return new Matrix(new Constant(difference + 1)).mult(f);
        }

        MatrixExpression result;
        result = f.replaceVariable(var, new Constant(k_1));
        for (int i = difference - 1; i >= 0; i--) {
            result = f.replaceVariable(var, new Constant(k_0.add(BigInteger.valueOf(i)))).add(result);
        }
        return result;

    }

    /**
     * Bildet das Produkt f(var = k_0) * f(var = k_0 + 1) * ... * f(var = k_1).
     *
     * @throws EvaluationException
     */
    public static Expression prod(Expression f, String var, BigInteger k_0, BigInteger k_1) {

        int difference = k_1.subtract(k_0).intValue();

        // Trivialer Fall: k_1 < k_0
        if (difference < 0) {
            return Expression.ONE;
        }

        // Trivialer Fall: expr hängt von var nicht ab.
        if (!f.contains(var)) {
            return f.pow(difference + 1);
        }

        Expression result = ONE;
        for (int i = difference; i >= 0; i--) {
            result = f.replaceVariable(var, new Constant(k_0.add(BigInteger.valueOf(i)))).mult(result);
        }
        return result;

    }

    /**
     * Bildet das Produkt f(var = k_0) * f(var = k_0 + 1) * ... * f(var = k_1).
     *
     * @throws EvaluationException
     */
    public static MatrixExpression prod(MatrixExpression f, String var, BigInteger k_0, BigInteger k_1) {

        int difference = k_1.subtract(k_0).intValue();

        // Trivialer Fall: k_1 < k_0
        if (difference < 0) {
            return new Matrix(Expression.ONE);
        }

        // Trivialer Fall: expr hängt von var nicht ab.
        if (!f.contains(var)) {
            return f.pow(difference + 1);
        }

        MatrixExpression result;
        result = f.replaceVariable(var, new Constant(k_1));
        for (int i = difference - 1; i >= 0; i--) {
            result = f.replaceVariable(var, new Constant(k_0.add(BigInteger.valueOf(i)))).mult(result);
        }
        return result;

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
                    throw new EvaluationException(Translator.translateOutputMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED", functionVar, ord, f));
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
                    throw new EvaluationException(Translator.translateOutputMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED", functionVar, ord, f));
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
                throw new EvaluationException(Translator.translateOutputMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_DEQ_CANNOT_BE_COMPUTED", functionVar, ord, f));
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
     * x_0).<br>
     * VORAUSSETZUNG: k >= 0, ord >= 1.
     *
     * @throws EvaluationException
     */
    public static Expression getFourierPolynomial(Expression f, String var, Expression startPoint, Expression endPoint, int degree) throws EvaluationException {

        if (degree < 0) {
            return Expression.ZERO;
        }

        // l ist die Periodenlänge
        Expression l = endPoint.sub(startPoint).simplify();

        Expression sumOfSines = ZERO, sumOfCosines;

        Object[][] paramsOfCosineSummands = new Object[degree + 1][4];
        Object[][] paramsOfSineSummands = new Object[degree + 1][4];

        paramsOfCosineSummands[0][0] = f;
        paramsOfCosineSummands[0][1] = var;
        paramsOfCosineSummands[0][2] = startPoint;
        paramsOfCosineSummands[0][3] = endPoint;
        sumOfCosines = new Operator(TypeOperator.integral, paramsOfCosineSummands[0]).div(l);

        // Es wird versucht, die Stammfunktion von f(x)*cos(2*pi*x/l) exakt zu berechnen.
        String parameterVar = NotationLoader.FREE_INTEGER_PARAMETER_VAR;
        Expression integralOfFunctionTimesCosine = new Operator(TypeOperator.integral,
                new Object[]{f.mult(TWO.mult(PI).mult(Variable.create(parameterVar)).mult(Variable.create(var)).div(l).cos()), var}).simplify();
        Expression integralOfFunctionTimesSine = new Operator(TypeOperator.integral,
                new Object[]{f.mult(TWO.mult(PI).mult(Variable.create(parameterVar)).mult(Variable.create(var)).div(l).sin()), var}).simplify();

        /* 
         Falls die Integrale, die in den Koeffizienten auftreten, nicht exakt
         berechnet werden können, so werden sie als Integrale eben stehengelassen.
         */
        if (integralOfFunctionTimesCosine.containsIndefiniteIntegral() || integralOfFunctionTimesSine.containsIndefiniteIntegral()) {
            for (int i = 1; i <= degree; i++) {
                paramsOfCosineSummands[i][0] = f.mult(TWO.mult(PI).mult(i).mult(Variable.create(var)).div(l).cos());
                paramsOfCosineSummands[i][1] = var;
                paramsOfCosineSummands[i][2] = startPoint;
                paramsOfCosineSummands[i][3] = endPoint;
                sumOfCosines = sumOfCosines.add(TWO.mult(new Operator(TypeOperator.integral, paramsOfCosineSummands[i]).mult(TWO.mult(PI).mult(i).mult(Variable.create(var)).div(l).cos())).div(l));
                paramsOfSineSummands[i][0] = f.mult(TWO.mult(PI).mult(i).mult(Variable.create(var)).div(l).sin());
                paramsOfSineSummands[i][1] = var;
                paramsOfSineSummands[i][2] = startPoint;
                paramsOfSineSummands[i][3] = endPoint;
                sumOfSines = sumOfSines.add(TWO.mult(new Operator(TypeOperator.integral, paramsOfSineSummands[i]).mult(TWO.mult(PI).mult(i).mult(Variable.create(var)).div(l).sin())).div(l));
            }
            return sumOfCosines.add(sumOfSines);
        }

        Expression cosineSummand, sineSummand;

        for (int i = 1; i <= degree; i++) {
            cosineSummand = integralOfFunctionTimesCosine.replaceVariable(parameterVar, new Constant(i)).replaceVariable(var, endPoint).sub(
                    integralOfFunctionTimesCosine.replaceVariable(parameterVar, new Constant(i)).replaceVariable(var, startPoint)).simplify();
            sineSummand = integralOfFunctionTimesSine.replaceVariable(parameterVar, new Constant(i)).replaceVariable(var, endPoint).sub(
                    integralOfFunctionTimesSine.replaceVariable(parameterVar, new Constant(i)).replaceVariable(var, startPoint)).simplify();
            sumOfCosines = sumOfCosines.add(TWO.mult(cosineSummand).mult(TWO.mult(PI).mult(i).mult(Variable.create(var)).div(l).cos()).div(l));
            sumOfSines = sumOfSines.add(TWO.mult(sineSummand).mult(TWO.mult(PI).mult(i).mult(Variable.create(var)).div(l).sin()).div(l));
        }

        return sumOfCosines.add(sumOfSines);

    }

    /**
     * Ermittelt das Taylorpolynom vom Grad degree von f (mit Entwicklungsstelle
     * x_0).<br>
     * VORAUSSETZUNG: k >= 0, ord >= 1.
     *
     * @throws EvaluationException
     */
    public static Expression getTaylorPolynomial(Expression f, String var, Expression x_0, int degree) throws EvaluationException {

        if (degree < 0) {
            return Expression.ZERO;
        }

        // Entwicklungspunkt ist a und die Variable var, nach der entwickelt wird, hat den Wert a.
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
            throw new EvaluationException(Translator.translateOutputMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_FUNCTION_CANNOT_BE_COMPUTED", f));
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
                throw new EvaluationException(Translator.translateMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_FUNCTION_CANNOT_BE_COMPUTED_1")
                        + f.writeExpression()
                        + Translator.translateMessage("CC_AnalysisMethods_TAYLOR_POLYNOMIAL_OF_FUNCTION_CANNOT_BE_COMPUTED_2"));
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
    public static Expression getTangentSpace(Expression f, HashMap<String, Expression> point)
            throws EvaluationException {

        HashSet<String> vars = f.getContainedIndeterminates();

        Expression result = ZERO;
        Expression factor;

        // Alle Variablen auf die Werte setzen, die dem Punkt x_0 entsprechen.
        for (String var : vars) {
            Variable.setPreciseExpression(var, point.get(var));
        }

        try {
            for (String var : vars) {
                factor = f.diff(var);
                factor = factor.simplifyByInsertingDefinedVars().simplify();
                result = result.add(factor.mult(Variable.create(var).sub(point.get(var))));
            }

            f = f.simplifyByInsertingDefinedVars().simplify();

            // Wichtig: allen Variablen, die in point vorkommen, wieder zu Unbestimmten machen.
            for (String var : vars) {
                Variable.setPreciseExpression(var, null);
            }

            return f.add(result).simplify();
        } catch (EvaluationException e) {
            throw new EvaluationException(Translator.translateMessage("CC_AnalysisMethods_TANGENT_SPACE_CANNOT_BE_COMPUTED"));
        } finally {
            /* 
             Egal, ob die Berechnung des Tangentialraumes erfolgreich war oder nicht,
             die Variablen, welchen ein fester Wert zugeordnet wurde, müssen wieder 
             zu Unbestimmten werden.
             */
            for (String var : vars) {
                Variable.setPreciseExpression(var, null);
            }
        }

    }

    /**
     * Ermittelt die ersten n Dezimalstellen der Eulerschen Konstante e.
     *
     * @throws ExpressionException
     */
    public static BigDecimal getDigitsOfE(int n) throws EvaluationException {

        if (n > computationbounds.ComputationBounds.getBound("BOUND_COMMAND_MAX_DIGITS_OF_E")) {
            throw new EvaluationException(Translator.translateMessage("CC_AnalysisMethods_ENTER_A_SMALLER_NUMBER_OF_DIGITS"));
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
    public static BigDecimal getDigitsOfPi(int n) throws EvaluationException {

        if (n > computationbounds.ComputationBounds.getBound("BOUND_COMMAND_MAX_DIGITS_OF_PI")) {
            throw new EvaluationException(Translator.translateMessage("CC_AnalysisMethods_ENTER_A_SMALLER_NUMBER_OF_DIGITS"));
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
        } else /*
             Gilt !(s >= 1 && s < 2), wird die Funktionalgleichung Gamma(s +1) 
             = s * Gamma(s) ausgenutzt, um das Argument wieder in den
             Bereich [1, 2) zu bekommen. Der Wert der Gammafunktion von diesem
             Argument wird dann wie oben berechnet.
         */ if (s >= 2) {
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
