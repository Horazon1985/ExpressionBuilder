package integrationmethods;

import computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import exceptions.NotPreciseIntegrableException;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.MINUS_ONE;
import static expressionbuilder.Expression.TWO;
import static expressionbuilder.Expression.ZERO;
import expressionbuilder.Function;
import expressionbuilder.Operator;
import expressionbuilder.TypeFunction;
import expressionbuilder.TypeOperator;
import expressionbuilder.TypeSimplify;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyBinaryOperationMethods;
import expressionsimplifymethods.SimplifyPolynomialMethods;
import expressionsimplifymethods.SimplifyUtilities;
import static integrationmethods.SimplifyIntegralMethods.indefiniteIntegration;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import solveequationmethods.PolynomialRootsMethods;
import solveequationmethods.SpecialEquationMethods;
import substitutionmethods.SubstitutionUtilities;

public abstract class SpecialIntegrationMethods {

    /**
     * Hauptmethode für Partialbruchzerlegung. Gibt im Erfolgsfall die
     * Stammfunktion zurück (falls der Integrand eine rationale Funktion ist und
     * die Partialbruchzerlegung erfolgreich war), ansonsten false.
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateRationalFunction(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        // Falls f keine rationale Function ist -> abbrechen.
        if (f.isNotQuotient()
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getRight(), var)) {
            throw new NotPreciseIntegrableException();
        }

        /*
         Im Folgenden sind nur rationale Funktionen zugelassen: der Nenner
         enthält keine Parameter, der Zähler darf welche enthalten.
         */
        HashSet varsInDenominator = new HashSet();
        ((BinaryOperation) f).getRight().addContainedVars(varsInDenominator);
        if (!varsInDenominator.contains(var) || varsInDenominator.size() > 1) {
            /*
             Dies trifft AUCH DANN zu, wenn der Nenner Parameter enthält, aber
             von var nicht abhängt. Dann ist der Nenner bzgl. var konstant und
             kann vor das Integral getragen werden, was NICHT hier, sondern in
             SimplifyIntegralMethods.takeConstantsOutOfIntegral() geschieht.
             Daher -> beenden.
             */
            throw new NotPreciseIntegrableException();
        }

        BigInteger degreeEnumerator = SimplifyPolynomialMethods.degreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
        BigInteger degreeDenominator = SimplifyPolynomialMethods.degreeOfPolynomial(((BinaryOperation) f).getRight(), var);

        // Nur bei Graden <= 100 fortfahren.
        if (degreeEnumerator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_MAXIMAL_INTEGRABLE_POWER)) > 0
                || degreeDenominator.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_MAXIMAL_INTEGRABLE_POWER)) > 0) {
            throw new NotPreciseIntegrableException();
        }

        ExpressionCollection coefficientsEnumerator = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);
        ExpressionCollection coefficientsDenominator = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getRight(), var);

        // Zunächst Polynomdivision durchführen und Zwischenergebnis ausgeben, wenn nötig.
        if (coefficientsEnumerator.getBound() >= coefficientsDenominator.getBound()) {

            ExpressionCollection[] resultOfPolynomialDivision = PolynomialRootsMethods.polynomialDivision(coefficientsEnumerator, coefficientsDenominator);
            Expression integralOfPolynomialPart = Expression.ZERO;
            for (int i = 0; i < resultOfPolynomialDivision[0].getBound(); i++) {
                if (integralOfPolynomialPart.equals(Expression.ZERO)) {
                    integralOfPolynomialPart = resultOfPolynomialDivision[0].get(i).mult(Variable.create(var).pow(i + 1)).div(i + 1);
                } else {
                    integralOfPolynomialPart = integralOfPolynomialPart.add(resultOfPolynomialDivision[0].get(i).mult(Variable.create(var).pow(i + 1)).div(i + 1));
                }
            }

            Object[] paramsIntegralOfFractionalPart = new Object[2];
            paramsIntegralOfFractionalPart[0] = PolynomialRootsMethods.getPolynomialFromCoefficients(resultOfPolynomialDivision[1], var).div(
                    ((BinaryOperation) f).getRight());
            paramsIntegralOfFractionalPart[1] = var;
            Operator integralOfFractionalPart = new Operator(TypeOperator.integral, paramsIntegralOfFractionalPart);

            return integralOfPolynomialPart.add(integralOfFractionalPart);

        }

        // Ab hier ist deg(Zähler) < deg(Nenner).
        // Nenner wird zerlegt, soweit es möglich ist.
        Expression denominatorDecomposed = PolynomialRootsMethods.decomposePolynomialInIrreducibleFactors(((BinaryOperation) f).getRight(), var);

        /*
         Zunächst: Partialbruchzerlegung wird angewendet, falls NUR
         Linearfaktoren in der Zerlegung des Nenners auftauchen.
         */
        ExpressionCollection factorsOfDecomposedDenominator = SimplifyUtilities.getFactors(denominatorDecomposed);
        if (factorsOfDecomposedDenominator.getBound() > 1) {

            for (int i = 0; i < factorsOfDecomposedDenominator.getBound(); i++) {
                if (factorsOfDecomposedDenominator.get(i).isPower()) {
                    if (SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) factorsOfDecomposedDenominator.get(i)).getLeft(), var)
                            && SimplifyPolynomialMethods.degreeOfPolynomial(((BinaryOperation) factorsOfDecomposedDenominator.get(i)).getLeft(), var).compareTo(BigInteger.ONE) > 0) {
                        throw new NotPreciseIntegrableException();
                    }
                } else if (SimplifyPolynomialMethods.isPolynomial(factorsOfDecomposedDenominator.get(i), var)
                        && SimplifyPolynomialMethods.degreeOfPolynomial(factorsOfDecomposedDenominator.get(i), var).compareTo(BigInteger.ONE) > 0) {
                    throw new NotPreciseIntegrableException();
                }
            }
            /*
             Ab hier ist der Nenner ein Produkt vom (Leitkoeffizienten und)
             paarweise verschiedenen Potenzen von Linearfaktoren.
             */
            // Koeffizienten der einzelnen Partialbrüche bestimmen.
            Object partialFractionDecomposition = getPartialFractionDecomposition(((BinaryOperation) f).getLeft(), denominatorDecomposed, var);

            if (!(partialFractionDecomposition instanceof Expression)) {
                throw new NotPreciseIntegrableException();
            }

            ExpressionCollection summandsOfIntegralFunction = SimplifyUtilities.getSummands((Expression) partialFractionDecomposition);
            /*
             WICHTIG: Auch wenn einige Koeffizienten in der
             Partialbruchzerlegung negativ sind, so sind es alles SUMMANDEN,
             denn das Ergebnis in getPartialFractionDecomposition() wird beim
             Zurückgeben NICHT vereinfacht.
             */
            Object[][] paramsOfIntegralOfSummand = new Object[summandsOfIntegralFunction.getBound()][2];

            for (int i = 0; i < summandsOfIntegralFunction.getBound(); i++) {
                paramsOfIntegralOfSummand[i][0] = summandsOfIntegralFunction.get(i);
                paramsOfIntegralOfSummand[i][1] = var;
                summandsOfIntegralFunction.put(i, new Operator(TypeOperator.integral, paramsOfIntegralOfSummand[i]));
            }

            return SimplifyUtilities.produceSum(summandsOfIntegralFunction);

        }

        /*
         Falls f = (a*x+b)/(c*x^2+d*x+e)^n mit n >= 2 ->
         integrateQuotientLinearQuadraticPolynomial() anwenden. Hier ist der
         Nenner bereits IRREDUZIBEL.
         */
        if (coefficientsEnumerator.getBound() <= 2 && denominatorDecomposed.isPower()
                && ((BinaryOperation) denominatorDecomposed).getRight().isIntegerConstant()
                && ((BinaryOperation) denominatorDecomposed).getRight().isNonNegative()) {

            ExpressionCollection coefficientsOfBaseOfDenominatorDecomposed = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) denominatorDecomposed).getLeft(), var);
            if (coefficientsOfBaseOfDenominatorDecomposed.getBound() == 3) {
                /*
                 Da der Grad des Nenners hier <= 100 ist, ist
                 denominatorDecomposed.size() <= 51.
                 */
                int n = ((Constant) ((BinaryOperation) denominatorDecomposed).getRight()).getValue().intValue();
                return integrateQuotientOfLinearAndPowerOfQuadraticPolynomial(coefficientsEnumerator, coefficientsOfBaseOfDenominatorDecomposed, n, var);
            }

        }

        /*
         Falls f = (a*x+b)/(c*x^2+d*x+e) ->
         integrateQuotientLinearQuadraticPolynomial() anwenden. Hier ist der
         Nenner bereits IRREDUZIBEL.
         */
        if (coefficientsEnumerator.getBound() <= 2 && coefficientsDenominator.getBound() == 3) {
            return integrateQuotientOfLinearAndQuadraticPolynomial(coefficientsEnumerator, coefficientsDenominator, var);
        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Liefert die Partialbruchzerlegung. VORAUSSETZUNG: Nenner ist bereits
     * soweit es geht in Potenzen irreduzibler Faktoren zerlegt. Er wird hier
     * NICHT WEITER zerlegt, sondern es wird mit der vorhandenen Zerlegung
     * gearbeitet.
     *
     * @throws EvaluationException
     */
    public static Object getPartialFractionDecomposition(Expression enumerator, Expression denominator, String var)
            throws EvaluationException {

        if (denominator.isNotProduct()) {
            return false;
        }

        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactors(denominator);

        // Es wird nur zerlegt, wenn alle Faktoren im Nenner Potenzen von linearen Polynom sind.
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i).isPower()) {
                if (!((BinaryOperation) factorsDenominator.get(i)).getRight().isIntegerConstant()
                        || !((BinaryOperation) factorsDenominator.get(i)).getRight().isNonNegative()) {
                    return false;
                }
            } else if (SimplifyPolynomialMethods.isPolynomial(factorsDenominator.get(i), var)
                    && SimplifyPolynomialMethods.degreeOfPolynomial(factorsDenominator.get(i), var).compareTo(BigInteger.ONE) > 0) {
                return false;
            }
        }

        /*
         Bestimmung der Koeffizienten der Partialbruchdarstellung. Im
         Folgenden sei x = var; Ist f = enumerator/denominator = g/prod((x -
         a_i)^n_i, i, 1, k), dann ist f = sum(C_i1/(x - a_i) + ... + C_in_i/(x
         - a_i)^n_i, i, 1, k). In den folgenden Kommentaren werden diese
         Notationen übernommen.
         */
        // decomposition wird die Partialbruchzerlegung sein.
        Expression decomposition = Expression.ZERO;
        // currentExponent ist der jeweilige Exponent des aktuellen Linearfaktors.
        int currentExponent;
        /*
         currentFactorInDenominator = (x - a_i)^n_i für das aktuelle i,
         currentDifference ist in jedem Schritt f - (C_i1/(x - a_i) + ... +
         C_in_i/(x - a_i)^n_i) für das aktuelle i.
         */
        Expression currentFactorInDenominator, currentDifference, zero;
        ExpressionCollection coefficientsOfEnumeratorOfDifference;
        /*
         coefficients_current_linear_factor ist die HashMap der
         Polynomkoeffizienten zu x - a_i, also: 0 -> -a_i, 1 -> 1.
         */
        ExpressionCollection coefficientsOfCurrentLinearFactor = new ExpressionCollection();

        for (int i = 0; i < factorsDenominator.getBound(); i++) {

            // Polstellen a_i wird ermittelt.
            if (factorsDenominator.get(i).isPower()) {
                zero = ((BinaryOperation) factorsDenominator.get(i)).getLeft().replaceVariable(var, Expression.ZERO).mult(-1).simplify();
            } else {
                zero = factorsDenominator.get(i).replaceVariable(var, Expression.ZERO).mult(-1).simplify();
            }
            coefficientsOfCurrentLinearFactor.clear();
            coefficientsOfCurrentLinearFactor.put(0, Expression.MINUS_ONE.mult(zero).simplify());
            coefficientsOfCurrentLinearFactor.put(1, Expression.ONE);

            // Für jeden Linearfaktor im Nenner wird der maximale Exponent ermittelt.
            if (factorsDenominator.get(i).isPower()) {
                currentExponent = ((Constant) ((BinaryOperation) factorsDenominator.get(i)).getRight()).getValue().intValue();
            } else {
                currentExponent = 1;
            }

            // Koeffizienten in der Partialbruchzerlegung
            Expression[] C_ij = new Expression[currentExponent];
            currentFactorInDenominator = factorsDenominator.get(i);

            for (int j = currentExponent; j >= 1; j--) {

                /*
                 Differenz bilden, also current_difference = f - C_i1/(x -
                 a_i) + C_i2/(x - a_i^2) - ... - C_i(j - 1)/(x - a_i)^(j - 1).
                 */
                currentDifference = enumerator.div(denominator);
                for (int k = currentExponent; k > j; k--) {
                    currentDifference = currentDifference.sub(C_ij[k - 1].div(Variable.create(var).sub(zero).pow(k).simplify()));
                }

                // Alles in currentDifference auf einen Nenner bringen.
                currentDifference = SimplifyBinaryOperationMethods.bringFractionToCommonDenominator((BinaryOperation) currentDifference);
                if (!(currentDifference instanceof BinaryOperation)) {
                    // Dürfte eigentlich nicht passieren, aber sicherheitshalber.
                    return false;
                }

                // Multiplikation mit (x - a_i)^j, 1 <= j <= n_i, und Kürzen der gemeinsamen Linearfaktoren.
                currentDifference = ((BinaryOperation) currentDifference).getLeft().mult(Variable.create(var).sub(zero).pow(j)).div(((BinaryOperation) currentDifference).getRight());

                // Polynomkoeffizienten im neuen Nenner berechnen.
                coefficientsOfEnumeratorOfDifference = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) currentDifference).getLeft(), var);
                for (int k = currentExponent; k > 0; k--) {
                    coefficientsOfEnumeratorOfDifference = PolynomialRootsMethods.polynomialDivision(coefficientsOfEnumeratorOfDifference,
                            coefficientsOfCurrentLinearFactor)[0];
                }

                // Entsprechende Potenz des Linearfaktors aus dem Nenner entfernen.
                factorsDenominator.remove(i);
                // Entsprechenden Koeffizienten C_ij in der Partialbruchzerlegung berechnen.
                C_ij[j - 1] = PolynomialRootsMethods.getPolynomialFromCoefficients(coefficientsOfEnumeratorOfDifference, var).div(SimplifyUtilities.produceProduct(factorsDenominator)).replaceVariable(var, zero).simplify();
                if (decomposition.equals(Expression.ZERO)) {
                    if (j > 1) {
                        decomposition = C_ij[j - 1].div(Variable.create(var).sub(zero).pow(j));
                    } else {
                        decomposition = C_ij[j - 1].div(Variable.create(var).sub(zero));
                    }
                } else {
                    if (j > 1) {
                        decomposition = decomposition.add(C_ij[j - 1].div(Variable.create(var).sub(zero).pow(j)));
                    } else {
                        decomposition = decomposition.add(C_ij[j - 1].div(Variable.create(var).sub(zero)));
                    }
                }

                // Entsprechende Potenz des Linearfaktors dem Nenner wieder hinzufügen, die oben entfernt wurde.
                factorsDenominator.put(i, currentFactorInDenominator);

            }

        }

        return decomposition;

    }

    /**
     * Integriert (a*x + b)/(c*x^2 + d*x + e)^n mit ganzem n >= 2 und
     * ireduziblem Nenner. VORAUSSETZUNG: coefficientsEnumerator.size() == 2,
     * coefficientsDenominator.size() == 3 und die Elemente in
     * coefficientsEnumerator enthalten var nicht und die Elemente von
     * coefficientsDenominator sind konstant. Andernfalls wird false
     * zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateQuotientOfLinearAndPowerOfQuadraticPolynomial(ExpressionCollection coefficientsEnumerator,
            ExpressionCollection coefficientsDenominator, int n, String var)
            throws EvaluationException, NotPreciseIntegrableException {

        // In den folgenden Kommentaren sei a = coefficientsEnumerator, b = coefficientsDenominator.
        Expression denominator = coefficientsDenominator.get(0);
        for (int i = 1; i < coefficientsDenominator.getBound(); i++) {
            denominator = denominator.add(coefficientsDenominator.get(i).mult(Variable.create(var)).pow(i));
        }

        // Falls in den Nennerkoeffizienten Parameter auftreten -> false zurückgeben.
        for (int i = 0; i < 3; i++) {
            if (!coefficientsDenominator.get(i).isConstant()) {
                throw new NotPreciseIntegrableException();
            }
        }

        // Falls bei a irgendwelche Koeffizienten fehlen -> mit Nullen auffüllen.
        if (coefficientsEnumerator.getBound() < 2) {
            for (int i = coefficientsEnumerator.getBound(); i < 2; i++) {
                coefficientsEnumerator.put(i, Expression.ZERO);
            }
        }

        /*
         Falls der Nenner reduzibel ist -> Falsche Methode (es muss auf
         Partialbruchzerlegung zurückgegriffen werden).
         */
        Expression diskriminant = coefficientsDenominator.get(1).pow(2).sub(new Constant(4).mult(coefficientsDenominator.get(0)).mult(coefficientsDenominator.get(2))).simplify();
        if (!diskriminant.isNonPositive() || diskriminant.equals(Expression.ZERO)) {
            throw new NotPreciseIntegrableException();
        }

        /*
         Im Folgenden sei: a = a.get(1), b = a.get(0), c = a.get(2), d =
         a.get(1), e = a.get(0), x = var.
         */
        // firstSummand = -a*(2*c*x + d)/((2n - 2)*c*(c*x^2 + d*x + e)^(n - 1))
        Expression firstSummand = Expression.MINUS_ONE.mult(coefficientsEnumerator.get(1)).mult(Expression.TWO.mult(coefficientsDenominator.get(2)).mult(Variable.create(var)).add(coefficientsDenominator.get(1))).div(new Constant(2 * n - 2).mult(coefficientsDenominator.get(2)).mult(denominator.pow(n - 1))).simplify();

        // factor = (2*b*c - a*d)/(2*c)
        Expression factor = Expression.TWO.mult(coefficientsEnumerator.get(0)).mult(coefficientsDenominator.get(2)).div(Expression.TWO.mult(coefficientsDenominator.get(2))).simplify();

        /*
         summand_for_recursive_integral = (2*c*x + d)/((n - 1)*(4*c*e -
         d^2)*(c*x^2 + d*x + e)^(n - 1))
         */
        Expression summandForRecursiveIntegral = Expression.TWO.mult(coefficientsDenominator.get(2)).mult(Variable.create(var)).add(coefficientsDenominator.get(1)).div(new Constant(n - 1).mult(new Constant(4).mult(coefficientsDenominator.get(2)).mult(coefficientsDenominator.get(0)).sub(coefficientsDenominator.get(1).pow(2))).mult(denominator.pow(n - 1))).simplify();

        Object[] params = new Object[2];
        params[0] = Expression.ONE.div(denominator.pow(n - 1));
        params[1] = var;
        Object integralOfLowerPower = indefiniteIntegration(new Operator(TypeOperator.integral, params), true);

        if (integralOfLowerPower instanceof Expression) {
            return firstSummand.add(factor.mult(summandForRecursiveIntegral).add(factor.mult((Expression) integralOfLowerPower)));
        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Integriert (a*x + b)/(c*x^2 + d*x + e) mit ireduziblem Nenner.
     * VORAUSSETZUNG: coefficientsEnumerator.size() == 2,
     * coefficientsDenominator.size() == 3 und die Elemente in
     * coefficientsEnumerator enthalten var nicht und die Elemente von
     * coefficientsDenominator sind konstant. Andernfalls wird false
     * zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateQuotientOfLinearAndQuadraticPolynomial(ExpressionCollection coefficientsEnumerator,
            ExpressionCollection coefficientsDenominator, String var) throws EvaluationException, NotPreciseIntegrableException {

        Expression denominator = coefficientsDenominator.get(0);
        for (int i = 1; i < coefficientsDenominator.getBound(); i++) {
            denominator = denominator.add(coefficientsDenominator.get(i).mult(Variable.create(var)).pow(i));
        }

        // In den folgenden Kommentaren sei a = coefficientsEnumerator, b = coefficientsDenominator.
        // Falls in den Nennerkoeffizienten Parameter auftreten -> false zurückgeben.
        for (int i = 0; i < 3; i++) {
            if (!coefficientsDenominator.get(i).isConstant()) {
                throw new NotPreciseIntegrableException();
            }
        }

        // Falls bei a irgendwelche Koeffizienten fehlen -> mit Nullen auffüllen.
        if (coefficientsEnumerator.getBound() < 2) {
            for (int i = coefficientsEnumerator.getBound(); i < 2; i++) {
                coefficientsEnumerator.put(i, Expression.ZERO);
            }
        }

        /*
         Falls der Nenner reduzibel ist -> Falsche Methode (es muss auf
         Partialbruchzerlegung zurückgegriffen werden).
         */
        Expression diskr = coefficientsDenominator.get(1).pow(2).sub(new Constant(4).mult(coefficientsDenominator.get(0)).mult(coefficientsDenominator.get(2))).simplify();
        if (!diskr.isNonPositive() || diskr.equals(Expression.ZERO)) {
            throw new NotPreciseIntegrableException();
        }

        /*
         Hilfsgrößen: p = a[1]/(2*b[2]), q = a[0]/b[2] - a[1]*b[1]/(2*b[2]^2),
         r = (-D/(4*b[0]^2))^(1/2). DANN: int(a[1]x + a[0])/(b[2]x^2 + b[1]x +
         b[0]) = p*ln(b[2]x^2 + b[1]x + b[0]) + (q/r)*arctan((2*b[2]*x +
         b[1])/(-D)^(1/2)) mit D = b[1]^2 - 4*b[2]*b[0].
         */
        Expression p = coefficientsEnumerator.get(1).div(Expression.TWO.mult(coefficientsDenominator.get(2))).simplify();
        Expression q = coefficientsEnumerator.get(0).div(coefficientsDenominator.get(2)).sub(coefficientsEnumerator.get(1).mult(coefficientsDenominator.get(1)).div(Expression.TWO.mult(coefficientsDenominator.get(2).pow(2)))).simplify();
        Expression r = new Constant(4).mult(coefficientsDenominator.get(2).pow(2)).div(Expression.MINUS_ONE.mult(diskr)).pow(1, 2).simplify();

        // Log-Summanden bilden.
        Expression logSummand = p.mult(denominator.ln());
        // Arctan-Summanden bilden.
        Expression arctanArgument = Expression.TWO.mult(coefficientsDenominator.get(2)).mult(Variable.create(var)).add(coefficientsDenominator.get(1)).div((Expression.MINUS_ONE.mult(diskr)).pow(1, 2)).simplify();
        Expression arctanSummand = q.mult(r).mult(arctanArgument.arctan()).simplify();

        return logSummand.add(arctanSummand);

    }

    // Weitere Typen.
    /**
     * Integration von sin(a*x+b)*sin(c*x+d). VORAUSSETZUNG: expr ist ein
     * unbestimmtes Integral. Falls der Integrand nicht vom angegebenen Typ ist,
     * so wird false zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Object integrateProductOfSinSin(Operator expr) throws EvaluationException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        if (factors.getBound() != 2 || !factors.get(0).isFunction() || !factors.get(1).isFunction()) {
            return false;
        }

        TypeFunction typeLeft = ((Function) factors.get(0)).getType();
        TypeFunction typeRight = ((Function) factors.get(1)).getType();

        if (typeLeft.equals(TypeFunction.sin) && typeRight.equals(TypeFunction.sin)) {

            Expression leftSinArgument = ((Function) factors.get(0)).getLeft();
            Expression rightSinArgument = ((Function) factors.get(1)).getLeft();
            if (!SimplifyPolynomialMethods.isPolynomial(leftSinArgument, var)
                    || !SimplifyPolynomialMethods.isPolynomial(rightSinArgument, var)) {
                return false;
            }
            ExpressionCollection coefficientsInLeftSin = PolynomialRootsMethods.getPolynomialCoefficients(leftSinArgument, var);
            ExpressionCollection coefficientsInRightSin = PolynomialRootsMethods.getPolynomialCoefficients(rightSinArgument, var);
            if (coefficientsInLeftSin.getBound() != 2 || coefficientsInRightSin.getBound() != 2) {
                return false;
            }

            /*
             Ab hier ist der Integrand von der Form f = sin(a*x+b)*sin(c*x+d).
             Sei I = int(sin(a*x+b)*sin(c*x+d), x).
             */
            Expression resultSummandLeft, resultSummandRight;
            Expression APlusB = coefficientsInLeftSin.get(1).add(coefficientsInRightSin.get(1)).simplify();
            Expression AMinusB = coefficientsInLeftSin.get(1).sub(coefficientsInRightSin.get(1)).simplify();
            if (AMinusB.equals(Expression.ZERO)) {
                // a = c
                resultSummandLeft = Variable.create(var).mult(coefficientsInLeftSin.get(0).sub(coefficientsInRightSin.get(0)).cos()).div(Expression.TWO);
                resultSummandRight = ((Function) factors.get(0)).getLeft().add(((Function) factors.get(1)).getLeft()).sin().div(new Constant(4).mult(coefficientsInLeftSin.get(1)));
                return resultSummandLeft.sub(resultSummandRight);
            } else if (APlusB.equals(Expression.ZERO)) {
                // a = -c
                resultSummandLeft = Variable.create(var).mult(coefficientsInLeftSin.get(0).add(coefficientsInRightSin.get(0)).cos()).div(Expression.TWO);
                resultSummandRight = ((Function) factors.get(0)).getLeft().sub(((Function) factors.get(1)).getLeft()).sin().div(new Constant(4).mult(coefficientsInLeftSin.get(1)));
                return resultSummandRight.sub(resultSummandLeft);
            } else {
                // a != c und a != -c. I = sin((a-c)*x+(b-d))/(2*(a-c)) - sin((a+c)*x+(b+d))/(2*(a+c)).
                resultSummandLeft = ((Function) factors.get(0)).getLeft().sub(((Function) factors.get(1)).getLeft()).sin().div(Expression.TWO.mult(AMinusB));
                resultSummandRight = ((Function) factors.get(0)).getLeft().add(((Function) factors.get(1)).getLeft()).sin().div(Expression.TWO.mult(APlusB));
                return resultSummandLeft.sub(resultSummandRight);
            }

        }

        return false;

    }

    /**
     * Integration von cos(a*x+b)*cos(c*x+d). VORAUSSETZUNG: expr ist ein
     * unbestimmtes Integral. Falls der Integrand nicht vom angegebenen Typ ist,
     * so wird false zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Object integrateProductOfCosCos(Operator expr) throws EvaluationException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        if (factors.getBound() != 2 || !factors.get(0).isFunction() || !factors.get(1).isFunction()) {
            return false;
        }

        TypeFunction typeLeft = ((Function) factors.get(0)).getType();
        TypeFunction typeRight = ((Function) factors.get(1)).getType();

        if (typeLeft.equals(TypeFunction.cos) && typeRight.equals(TypeFunction.cos)) {

            Expression leftCosArgument = ((Function) factors.get(0)).getLeft();
            Expression rightCosArgument = ((Function) factors.get(1)).getLeft();
            if (!SimplifyPolynomialMethods.isPolynomial(leftCosArgument, var)
                    || !SimplifyPolynomialMethods.isPolynomial(rightCosArgument, var)) {
                return false;
            }
            ExpressionCollection coefficientsInLeftCos = PolynomialRootsMethods.getPolynomialCoefficients(leftCosArgument, var);
            ExpressionCollection coefficientsInRightCos = PolynomialRootsMethods.getPolynomialCoefficients(rightCosArgument, var);
            if (coefficientsInLeftCos.getBound() != 2 || coefficientsInRightCos.getBound() != 2) {
                return false;
            }

            /*
             Ab hier ist der Integrand von der Form f = cos(a*x+b)*cos(c*x+d).
             Sei I = int(cos(a*x+b)*cos(c*x+d), x).
             */
            Expression resultSummandLeft, resultSummandRight;
            Expression APlusB = coefficientsInLeftCos.get(1).add(coefficientsInRightCos.get(1)).simplify();
            Expression AMinusB = coefficientsInLeftCos.get(1).sub(coefficientsInRightCos.get(1)).simplify();
            if (AMinusB.equals(Expression.ZERO)) {
                // a = c
                resultSummandLeft = Variable.create(var).mult(coefficientsInLeftCos.get(0).sub(coefficientsInRightCos.get(0)).cos()).div(Expression.TWO);
                resultSummandRight = ((Function) factors.get(0)).getLeft().add(((Function) factors.get(1)).getLeft()).sin().div(new Constant(4).mult(coefficientsInLeftCos.get(1)));
                return resultSummandLeft.add(resultSummandRight);
            } else if (APlusB.equals(Expression.ZERO)) {
                // a = -c
                resultSummandLeft = Variable.create(var).mult(coefficientsInLeftCos.get(0).add(coefficientsInRightCos.get(0)).cos()).div(Expression.TWO);
                resultSummandRight = ((Function) factors.get(0)).getLeft().sub(((Function) factors.get(1)).getLeft()).sin().div(new Constant(4).mult(coefficientsInLeftCos.get(1)));
                return resultSummandRight.add(resultSummandLeft);
            } else {
                // a != c und a != -c. I = sin((a-c)*x+(b-d))/(2*(a-c)) + sin((a+c)*x+(b+d))/(2*(a+c)).
                resultSummandLeft = ((Function) factors.get(0)).getLeft().sub(((Function) factors.get(1)).getLeft()).sin().div(Expression.TWO.mult(AMinusB));
                resultSummandRight = ((Function) factors.get(0)).getLeft().add(((Function) factors.get(1)).getLeft()).sin().div(Expression.TWO.mult(APlusB));
                return resultSummandLeft.sub(resultSummandRight);
            }

        }

        return false;

    }

    /**
     * Integration von sin(a*x+b)*cos(c*x+d). VORAUSSETZUNG: expr ist ein
     * unbestimmtes Integral. Falls der Integrand nicht vom angegebenen Typ ist,
     * so wird false zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Object integrateProductOfSinCos(Operator expr) throws EvaluationException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        if (factors.getBound() != 2 || !factors.get(0).isFunction() || !factors.get(1).isFunction()) {
            return false;
        }

        TypeFunction typeLeft = ((Function) factors.get(0)).getType();
        TypeFunction typeRight = ((Function) factors.get(1)).getType();

        if (typeLeft.equals(TypeFunction.cos) && typeRight.equals(TypeFunction.sin)) {
            // Plätze tauschen und weitermachen!
            Expression tmpFactor = factors.get(0).copy();
            factors.put(0, factors.get(1));
            factors.put(1, tmpFactor);
            TypeFunction tmpType = typeLeft;
            typeLeft = typeRight;
            typeRight = tmpType;
        }
        if (typeLeft.equals(TypeFunction.sin) && typeRight.equals(TypeFunction.cos)) {

            Expression sinArgument = ((Function) factors.get(0)).getLeft();
            Expression cosArgument = ((Function) factors.get(1)).getLeft();
            if (!SimplifyPolynomialMethods.isPolynomial(sinArgument, var)
                    || !SimplifyPolynomialMethods.isPolynomial(cosArgument, var)) {
                return false;
            }
            ExpressionCollection coefficientsInSin = PolynomialRootsMethods.getPolynomialCoefficients(sinArgument, var);
            ExpressionCollection coefficientsInCos = PolynomialRootsMethods.getPolynomialCoefficients(cosArgument, var);
            if (coefficientsInSin.getBound() != 2 || coefficientsInCos.getBound() != 2) {
                return false;
            }

            /*
             Ab hier ist der Integrand von der Form f = sin(a*x+b)*cos(c*x+d).
             Sei I = int(sin(a*x+b)*cos(c*x+d), x).
             */
            Expression resultSummandLeft, resultSummandRight;
            Expression APlusB = coefficientsInSin.get(1).add(coefficientsInCos.get(1)).simplify();
            Expression AMinusB = coefficientsInSin.get(1).sub(coefficientsInCos.get(1)).simplify();
            if (AMinusB.equals(Expression.ZERO)) {
                // a = c
                resultSummandLeft = Variable.create(var).mult(coefficientsInSin.get(0).sub(coefficientsInCos.get(0)).sin()).div(Expression.TWO);
                resultSummandRight = ((Function) factors.get(0)).getLeft().add(((Function) factors.get(1)).getLeft()).cos().div(new Constant(4).mult(coefficientsInSin.get(1)));
                return resultSummandLeft.sub(resultSummandRight);
            } else if (APlusB.equals(Expression.ZERO)) {
                // a = -c
                resultSummandLeft = Variable.create(var).mult(coefficientsInSin.get(0).add(coefficientsInCos.get(0)).sin()).div(Expression.TWO);
                resultSummandRight = ((Function) factors.get(0)).getLeft().sub(((Function) factors.get(1)).getLeft()).cos().div(new Constant(4).mult(coefficientsInSin.get(1)));
                return resultSummandLeft.sub(resultSummandRight);
            } else {
                // a != c und a != -c. I = -cos((a-c)*x+(b-d))/(2*(a-c)) - cos((a+c)*x+(b+d))/(2*(a+c)).
                resultSummandLeft = ((Function) factors.get(0)).getLeft().sub(((Function) factors.get(1)).getLeft()).cos().div(Expression.TWO.mult(AMinusB));
                resultSummandRight = ((Function) factors.get(0)).getLeft().add(((Function) factors.get(1)).getLeft()).cos().div(Expression.TWO.mult(APlusB));
                return Expression.MINUS_ONE.mult(resultSummandLeft).sub(resultSummandRight);
            }

        }

        return false;

    }

    /**
     * Hilfsmethode. Gibt zurück, ob f eine ganzzahlige, positive Potenz einer
     * Funktion vom Type type ist.
     */
    private static boolean isIntegerPowerOfFunction(Expression f, TypeFunction type) {
        return f.isPower() && ((BinaryOperation) f).getLeft().isFunction(type)
                && ((BinaryOperation) f).getRight().isIntegerConstant()
                && ((BinaryOperation) f).getRight().isPositive();
    }

    /**
     * Integration von exp(a*x+b)*cos(c*x+d)^n.<br>
     * VORAUSSETZUNG: expr ist ein unbestimmtes Integral. Falls der Integrand
     * nicht vom angegebenen Typ ist, so wird false zurückgegeben.
     *
     * @throws EvaluationException
     */
    public static Object integrateProductOfExpPowerOfCos(Operator expr) throws EvaluationException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        if (factors.getBound() != 2
                || !(factors.get(0).isFunction(TypeFunction.exp)
                && isIntegerPowerOfFunction(factors.get(1), TypeFunction.cos))
                && !(factors.get(1).isFunction(TypeFunction.exp)
                && isIntegerPowerOfFunction(factors.get(0), TypeFunction.cos))) {
            return false;
        }

        if (factors.get(0).isPower()) {
            // Dann steht cos(...)^n im ersten Faktor. -> Plätze tauschen und weitermachen!
            Expression tmpFactor = factors.get(0).copy();
            factors.put(0, factors.get(1));
            factors.put(1, tmpFactor);
        }

        Expression expArgument = ((Function) factors.get(0)).getLeft();
        Expression cosArgument = ((Function) ((BinaryOperation) factors.get(1)).getLeft()).getLeft();
        BigInteger exponent = ((Constant) ((BinaryOperation) factors.get(1)).getRight()).getValue().toBigInteger();
        if (!SimplifyPolynomialMethods.isPolynomial(expArgument, var)
                || !SimplifyPolynomialMethods.isPolynomial(cosArgument, var)) {
            return false;
        }
        ExpressionCollection coefficientsInExp = PolynomialRootsMethods.getPolynomialCoefficients(expArgument, var);
        ExpressionCollection coefficientsInCos = PolynomialRootsMethods.getPolynomialCoefficients(cosArgument, var);
        if (coefficientsInExp.getBound() != 2 || coefficientsInCos.getBound() != 2
                || exponent.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_MAXIMAL_INTEGRABLE_POWER)) > 0) {
            return false;
        }

        int n = exponent.intValue();

        /*
         Ab hier ist der Integrand von der Form f = exp(a*x+b)*cos(c*x+d)^n.
         Sei I = int(exp(a*x+b)*cos(c*x+d)^n, x).
         */
        Expression a = coefficientsInExp.get(1);
        Expression c = coefficientsInCos.get(1);
        Expression d = coefficientsInCos.get(0);

        // a und c dürfen nicht 0 sein!
        if (a.equals(ZERO) || c.equals(ZERO)) {
            return false;
        }

        Expression resultFunction = ZERO;
        Expression cosPart, sinPart;
        for (int i = 0; i <= n; i++) {
            cosPart = a.div(new Constant(BigInteger.valueOf(n - 2 * i).pow(2)).mult(c.pow(2)).add(a.pow(2))).mult(new Constant(n - 2 * i).mult(c.mult(Variable.create(var)).add(d)).cos());
            sinPart = (new Constant(n - 2 * i).mult(c)).div(new Constant(BigInteger.valueOf(n - 2 * i).pow(2)).mult(c.pow(2)).add(a.pow(2))).mult(new Constant(n - 2 * i).mult(c.mult(Variable.create(var)).add(d)).sin());
            resultFunction = resultFunction.add(new Constant(ArithmeticMethods.bin(n, i)).mult(cosPart.add(sinPart)));
        }
        return (factors.get(0)).mult(resultFunction).div(TWO.pow(n));

    }

    private static boolean isExponentialFunction(Expression f, String var) {
        return f.isFunction(TypeFunction.exp) && SimplifyPolynomialMethods.isPolynomial(((Function) f).getLeft(), var)
                && SimplifyPolynomialMethods.degreeOfPolynomial(((Function) f).getLeft(), var).compareTo(BigInteger.ONE) == 0;
    }

    private static boolean isTrigonometricalFunction(Expression f, String var) {
        return (f.isFunction(TypeFunction.cos) || f.isFunction(TypeFunction.sin)) && SimplifyPolynomialMethods.isPolynomial(((Function) f).getLeft(), var)
                && SimplifyPolynomialMethods.degreeOfPolynomial(((Function) f).getLeft(), var).compareTo(BigInteger.ONE) == 0;
    }

    private static boolean isSumOfProductsOfExponentialAndTrigonometricalFunctions(Expression f, String var) {

        ExpressionCollection summands = SimplifyUtilities.getSummandsLeftInExpression(f);
        summands.add(SimplifyUtilities.getSummandsRightInExpression(f));

        ExpressionCollection factorsEnumerator, factorsDenominator;
        int numberOfExpFactors, numberOfTrigonometricalFactors;
        for (int i = 0; i < summands.getBound(); i++) {
            if (summands.get(i) == null || !summands.get(i).contains(var)) {
                continue;
            }
            factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summands.get(i));
            factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(i));
            numberOfExpFactors = 0;
            numberOfTrigonometricalFactors = 0;
            for (int j = 0; j < factorsEnumerator.getBound(); j++) {
                if (factorsEnumerator.get(j).contains(var) && !isExponentialFunction(factorsEnumerator.get(j), var)
                        && !isTrigonometricalFunction(factorsEnumerator.get(j), var)) {
                    return false;
                }
                if (isExponentialFunction(factorsEnumerator.get(j), var)) {
                    numberOfExpFactors++;
                }
                if (isTrigonometricalFunction(factorsEnumerator.get(j), var)) {
                    numberOfTrigonometricalFactors++;
                }
            }
            for (int j = 0; j < factorsDenominator.getBound(); j++) {
                if (factorsDenominator.get(j).contains(var)) {
                    return false;
                }
            }
            if (numberOfExpFactors > 1 || numberOfTrigonometricalFactors > 1) {
                return false;
            }
        }

        return true;

    }

    private static Expression expandProductsOfExponentialAndTrigonometricalFunctions(Expression f, String var) throws EvaluationException {
        return f.simplify(var, TypeSimplify.order_difference_and_division, TypeSimplify.order_sums_and_products,
                TypeSimplify.simplify_trivial, TypeSimplify.simplify_powers, TypeSimplify.collect_products,
                TypeSimplify.reduce_quotients, TypeSimplify.reduce_leadings_coefficients, TypeSimplify.expand,
                TypeSimplify.simplify_expand_powers_and_products_of_trigonometrical_functions);
    }

    /**
     * Integriert Funktionen vom Typ Polynom in Exponentialfunktionen und
     * trigonometrischen Funktionen, falls der Polynomgrad unterhalb einer
     * geeigneten Schranke liegt.
     *
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integratePolynomialInExponentialAndTrigonometricalFunctions(Operator expr) throws NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (!BinaryOperation.isPolynomialInVariousExponentialAndTrigonometricalFunctions(f, var)) {
            throw new NotPreciseIntegrableException();
        }

        try {
            // Nun Potenzen von sin und cos durch Summen von sin und cos ersetzen.
            Expression fExpanded = expandProductsOfExponentialAndTrigonometricalFunctions(f, var);
            if (isSumOfProductsOfExponentialAndTrigonometricalFunctions(fExpanded, var)) {
                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(fExpanded);
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(fExpanded);
                Object integralOfSummand;
                for (int i = 0; i < summandsLeft.getBound(); i++) {
                    integralOfSummand = integrateSummandOfPolynomialInExponentialAndTrigonometricalFunctions(summandsLeft.get(i), var);
                    if (!(integralOfSummand instanceof Expression)) {
                        throw new NotPreciseIntegrableException();
                    }
                    summandsLeft.put(i, (Expression) integralOfSummand);
                }
                for (int i = 0; i < summandsRight.getBound(); i++) {
                    integralOfSummand = integrateSummandOfPolynomialInExponentialAndTrigonometricalFunctions(summandsRight.get(i), var);
                    if (!(integralOfSummand instanceof Expression)) {
                        throw new NotPreciseIntegrableException();
                    }
                    summandsRight.put(i, (Expression) integralOfSummand);
                }
                return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);
            }
        } catch (EvaluationException e) {
        }

        throw new NotPreciseIntegrableException();

    }

    private static Object integrateSummandOfPolynomialInExponentialAndTrigonometricalFunctions(Expression f, String var) throws EvaluationException {

        // Zuerst: Fall eines konstanten Summanden:
        if (!f.contains(var)) {
            return f.mult(Variable.create(var));
        }

        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(f);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
        ExpressionCollection constantfactorsEnumerator = new ExpressionCollection();
        ExpressionCollection constantfactorsDenominator = new ExpressionCollection();

        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (!factorsEnumerator.get(i).contains(var)) {
                constantfactorsEnumerator.add(factorsEnumerator.get(i));
                factorsEnumerator.remove(i);
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (!factorsDenominator.get(i).contains(var)) {
                constantfactorsDenominator.add(factorsDenominator.get(i));
                factorsDenominator.remove(i);
            }
        }

        if (!factorsDenominator.isEmpty() || factorsEnumerator.getSize() > 2) {
            return false;
        }

        // Jetzt wird die Stammfunktion, abhängig von der Gestalt des Summanden, explizit bestimmt.
        ExpressionCollection factors = new ExpressionCollection(factorsEnumerator);
        Expression constantEnumerator = SimplifyUtilities.produceProduct(constantfactorsEnumerator);
        Expression constantDenominator = SimplifyUtilities.produceProduct(constantfactorsDenominator);

        // Fall: Integration von exp(a*x+b).
        if (factors.getBound() == 1 && factors.get(0).isFunction(TypeFunction.exp)) {
            Object integral = integrateExpOfLinearFunction(((Function) factors.get(0)).getLeft(), var);
            if (integral instanceof Expression) {
                return constantEnumerator.mult((Expression) integral).div(constantDenominator);
            }
        }
        // Fall: Integration von cos(a*x+b).
        if (factors.getBound() == 1 && factors.get(0).isFunction(TypeFunction.cos)) {
            Object integral = integrateCosOfLinearFunction(((Function) factors.get(0)).getLeft(), var);
            if (integral instanceof Expression) {
                return constantEnumerator.mult((Expression) integral).div(constantDenominator);
            }
        }
        // Fall: Integration von sin(a*x+b).
        if (factors.getBound() == 1 && factors.get(0).isFunction(TypeFunction.sin)) {
            Object integral = integrateSinOfLinearFunction(((Function) factors.get(0)).getLeft(), var);
            if (integral instanceof Expression) {
                return constantEnumerator.mult((Expression) integral).div(constantDenominator);
            }
        }
        // Fall: Integration von exp(a*x+b)*cos(c*x+d).
        if (factors.getBound() == 2 && factors.get(0).isFunction(TypeFunction.exp) && factors.get(1).isFunction(TypeFunction.cos)) {
            Object integral = integrateProductOfExpCos(((Function) factors.get(0)).getLeft(), ((Function) factors.get(1)).getLeft(), var);
            if (integral instanceof Expression) {
                return constantEnumerator.mult((Expression) integral).div(constantDenominator);
            }
        }
        // Fall: Integration von cos(a*x+b)*exp(c*x+d).
        if (factors.getBound() == 2 && factors.get(0).isFunction(TypeFunction.cos) && factors.get(1).isFunction(TypeFunction.exp)) {
            Object integral = integrateProductOfExpCos(((Function) factors.get(1)).getLeft(), ((Function) factors.get(0)).getLeft(), var);
            if (integral instanceof Expression) {
                return constantEnumerator.mult((Expression) integral).div(constantDenominator);
            }
        }
        // Fall: Integration von exp(a*x+b)*sin(c*x+d).
        if (factors.getBound() == 2 && factors.get(0).isFunction(TypeFunction.exp) && factors.get(1).isFunction(TypeFunction.sin)) {
            Object integral = integrateProductOfExpSin(((Function) factors.get(0)).getLeft(), ((Function) factors.get(1)).getLeft(), var);
            if (integral instanceof Expression) {
                return constantEnumerator.mult((Expression) integral).div(constantDenominator);
            }
        }
        // Fall: Integration von sin(a*x+b)*exp(c*x+d).
        if (factors.getBound() == 2 && factors.get(0).isFunction(TypeFunction.sin) && factors.get(1).isFunction(TypeFunction.exp)) {
            Object integral = integrateProductOfExpSin(((Function) factors.get(1)).getLeft(), ((Function) factors.get(0)).getLeft(), var);
            if (integral instanceof Expression) {
                return constantEnumerator.mult((Expression) integral).div(constantDenominator);
            }
        }

        return false;

    }

    /**
     * Explizite Integration von exp(a*x+b).<br>
     *
     * @throws EvaluationException
     */
    private static Object integrateExpOfLinearFunction(Expression expArgument, String var) throws EvaluationException {
        if (!SimplifyPolynomialMethods.isPolynomial(expArgument, var)
                || SimplifyPolynomialMethods.degreeOfPolynomial(expArgument, var).compareTo(BigInteger.ONE) != 0) {
            return false;
        }
        ExpressionCollection coefficients = solveequationmethods.PolynomialRootsMethods.getPolynomialCoefficients(expArgument, var);
        return expArgument.exp().div(coefficients.get(1));
    }

    /**
     * Explizite Integration von cos(a*x+b).<br>
     *
     * @throws EvaluationException
     */
    private static Object integrateCosOfLinearFunction(Expression expArgument, String var) throws EvaluationException {
        if (!SimplifyPolynomialMethods.isPolynomial(expArgument, var)
                || SimplifyPolynomialMethods.degreeOfPolynomial(expArgument, var).compareTo(BigInteger.ONE) != 0) {
            return false;
        }
        ExpressionCollection coefficients = solveequationmethods.PolynomialRootsMethods.getPolynomialCoefficients(expArgument, var);
        return expArgument.sin().div(coefficients.get(1));
    }

    /**
     * Explizite Integration von sin(a*x+b).<br>
     *
     * @throws EvaluationException
     */
    private static Object integrateSinOfLinearFunction(Expression expArgument, String var) throws EvaluationException {
        if (!SimplifyPolynomialMethods.isPolynomial(expArgument, var)
                || SimplifyPolynomialMethods.degreeOfPolynomial(expArgument, var).compareTo(BigInteger.ONE) != 0) {
            return false;
        }
        ExpressionCollection coefficients = solveequationmethods.PolynomialRootsMethods.getPolynomialCoefficients(expArgument, var);
        return MINUS_ONE.mult(expArgument.cos()).div(coefficients.get(1));
    }

    /**
     * Integration von exp(a*x+b)*sin(c*x+d).
     *
     * @throws EvaluationException
     */
    public static Object integrateProductOfExpSin(Expression expArgument, Expression sinArgument, String var) throws EvaluationException {

        ExpressionCollection coefficientsInExp = PolynomialRootsMethods.getPolynomialCoefficients(expArgument, var);
        ExpressionCollection coefficientsInSin = PolynomialRootsMethods.getPolynomialCoefficients(sinArgument, var);
        if (coefficientsInExp.getBound() != 2 || coefficientsInSin.getBound() != 2) {
            return false;
        }

        /*
         Es wird int(exp(a*x+b)*sin(c*x+d), x) =
         exp(a*x+b)*(a*sin(c*x+d)-c*cos(c*x+d))/(a^2+c^2) zurückgegeben.
         */
        return expArgument.exp().mult(coefficientsInExp.get(1).mult(sinArgument.sin()).sub(coefficientsInSin.get(1).mult(sinArgument.cos()))).div(
                coefficientsInExp.get(1).pow(2).add(coefficientsInSin.get(1).pow(2)));

    }

    /**
     * Integration von exp(a*x+b)*cos(c*x+d).
     *
     * @throws EvaluationException
     */
    public static Object integrateProductOfExpCos(Expression expArgument, Expression cosArgument, String var) throws EvaluationException {

        ExpressionCollection coefficientsInExp = PolynomialRootsMethods.getPolynomialCoefficients(expArgument, var);
        ExpressionCollection coefficientsInCos = PolynomialRootsMethods.getPolynomialCoefficients(cosArgument, var);
        if (coefficientsInExp.getBound() != 2 || coefficientsInCos.getBound() != 2) {
            return false;
        }

        /*
         Es wird int(exp(a*x+b)*cos(c*x+d), x) =
         exp(a*x+b)*(a*cos(c*x+d)+c*sin(c*x+d))/(a^2+c^2) zurückgegeben.
         */
        return expArgument.exp().mult(coefficientsInExp.get(1).mult(cosArgument.cos()).add(coefficientsInCos.get(1).mult(cosArgument.sin()))).div(
                coefficientsInExp.get(1).pow(2).add(coefficientsInCos.get(1).pow(2)));

    }

    /**
     * Integration von (a*x^2 + b*x + c)^(1/2). VORAUSSETZUNG: expr ist ein
     * unbestimmtes Integral. Falls der Integrand nicht vom angegebenen Typ ist,
     * so wird false zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateSqrtOfQuadraticFunction(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotPower() || !((BinaryOperation) f).getRight().equals(Expression.ONE.div(Expression.TWO))
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) f).getLeft(), var)) {
            throw new NotPreciseIntegrableException();
        }

        ExpressionCollection coefficients = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) f).getLeft(), var);

        if (coefficients.getBound() != 3) {
            throw new NotPreciseIntegrableException();
        }

        // Falls in den Koeffizienten Parameter auftreten, die das Vorzeichen ändern können -> false zurückgeben.
        for (int i = 0; i < 2; i++) {
            if (!coefficients.get(i).isConstant() && !coefficients.get(i).isAlwaysNonNegative() && !(Expression.MINUS_ONE).mult(coefficients.get(i)).simplify().isAlwaysNonNegative()) {
                throw new NotPreciseIntegrableException();
            }
        }

        // Im Folgenden sei a = a.get(2), b = a.get(1), c = a.get(0), diskr = D = b^2 - 4*a*c.
        Expression diskriminant = coefficients.get(1).pow(2).sub(new Constant(4).mult(coefficients.get(2)).mult(coefficients.get(0))).simplify();

        if (diskriminant.equals(Expression.ZERO) && coefficients.get(2).isAlwaysNonNegative() && !coefficients.get(2).equals(Expression.ZERO)) {
            // Dann ist f = a^(1/2)*|x - x_1| mit x_1 = -b/(2*a).
            Expression zero = Expression.MINUS_ONE.mult(coefficients.get(1)).div(Expression.TWO.mult(coefficients.get(2))).simplify();
            // F = a^(1/2)*(x - x_1)*|x - x_1|/2.
            return coefficients.get(2).pow(1, 2).mult(Variable.create(var).sub(zero)).mult(Variable.create(var).sub(zero).abs()).div(2);
        }
        if (!diskriminant.equals(Expression.ZERO) && (diskriminant.isNonNegative() || diskriminant.isAlwaysNonNegative())) {
            // Hier ist D > 0.
            if ((coefficients.get(2).isNonNegative() || coefficients.get(2).isAlwaysNonNegative()) && !coefficients.get(2).equals(Expression.ZERO)) {

                // Fall a > 0, D > 0. Reduktion auf den Typ int((x^2 - 1)^(1/2), x).
                /*
                 F = (2*a*x + b)*(a*x^2 + b*x + c)^(1/2)/(4*a) -
                 D*arcosh((2*a*x + b)/D^(1/2))/(8*a^(3/2)).
                 */
                return Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).mult(
                        ((BinaryOperation) f).getLeft().pow(1, 2)).div(new Constant(4).mult(coefficients.get(2))).sub(
                                diskriminant.mult(Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).div(diskriminant.pow(1, 2)).arcosh()).div(
                                        new Constant(8).mult(coefficients.get(2).pow(3, 2))));

            }
            if ((coefficients.get(2).isNonPositive() || Expression.MINUS_ONE.mult(coefficients.get(2)).simplify().isAlwaysNonNegative()) && !coefficients.get(2).equals(Expression.ZERO)) {

                // Fall a < 0, D > 0. Reduktion auf den Typ int((1 - x^2)^(1/2), x).
                /*
                 F = (2*a*x + b)*(a*x^2 + b*x + c)^(1/2)/(4*a) +
                 D*arcsin((-2*a*x - b)/D^(1/2))/(8*(-a)^(3/2)).
                 */
                return Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).mult(
                        ((BinaryOperation) f).getLeft().pow(1, 2)).div(new Constant(4).mult(coefficients.get(2))).add(
                                diskriminant.mult((new Constant(-2)).mult(coefficients.get(2)).mult(Variable.create(var)).sub(coefficients.get(1)).div(diskriminant.pow(1, 2)).arcsin()).div(
                                        new Constant(8).mult((Expression.MINUS_ONE).mult(coefficients.get(2)).pow(3, 2))));

            }
        }
        if (!diskriminant.equals(Expression.ZERO) && (diskriminant.isNonPositive() || Expression.MINUS_ONE.mult(diskriminant).simplify().isAlwaysNonNegative())
                && (coefficients.get(2).isNonNegative() || coefficients.get(2).isAlwaysNonNegative()) && !coefficients.get(2).equals(Expression.ZERO)) {

            // Fall a > 0, D < 0. Reduktion auf den Typ int((x^2 + 1)^(1/2), x).
            /*
             F = (2*a*x + b)*(a*x^2 + b*x + c)^(1/2)/(4*a) - D*arsinh((2*a*x +
             b)/(-D)^(1/2))/(8*a^(3/2)).
             */
            return Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).mult(
                    ((BinaryOperation) f).getLeft().pow(1, 2)).div(new Constant(4).mult(coefficients.get(2))).sub(
                            diskriminant.mult(Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).div(Expression.MINUS_ONE.mult(diskriminant).pow(1, 2)).arsinh()).div(
                                    new Constant(8).mult(coefficients.get(2).pow(3, 2))));

        }

        // Übrige, nicht eindeutig entscheidbare Fälle.
        throw new NotPreciseIntegrableException();

    }

    /**
     * Integration von 1/(a*x^2 + b*x + c)^(1/2). VORAUSSETZUNG: expr ist ein
     * unbestimmtes Integral. Falls der Integrand nicht vom angegebenen Typ ist,
     * so wird false zurückgegeben.
     *
     * @throws EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateReciprocalOfSqrtOfQuadraticFunction(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        if (f.isNotQuotient() || !((BinaryOperation) f).getLeft().equals(Expression.ONE)
                || ((BinaryOperation) f).getRight().isNotPower()
                || !((BinaryOperation) ((BinaryOperation) f).getRight()).getRight().equals(Expression.ONE.div(Expression.TWO))
                || !SimplifyPolynomialMethods.isPolynomial(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var)) {
            throw new NotPreciseIntegrableException();
        }

        ExpressionCollection coefficients = PolynomialRootsMethods.getPolynomialCoefficients(((BinaryOperation) ((BinaryOperation) f).getRight()).getLeft(), var);

        if (coefficients.getBound() != 3) {
            throw new NotPreciseIntegrableException();
        }

        // Falls in den Koeffizienten Parameter auftreten, die das Vorzeichen ändern können -> false zurückgeben.
        for (int i = 0; i < 2; i++) {
            if (!coefficients.get(i).isConstant() && !coefficients.get(i).isAlwaysNonNegative() && !(Expression.MINUS_ONE).mult(coefficients.get(i)).simplify().isAlwaysNonNegative()) {
                throw new NotPreciseIntegrableException();
            }
        }

        // Im Folgenden sei a = a.get(2), b = a.get(1), c = a.get(0), diskr = D = b^2 - 4*a*c.
        Expression diskriminant = coefficients.get(1).pow(2).sub(new Constant(4).mult(coefficients.get(2)).mult(coefficients.get(0))).simplify();

        if (diskriminant.equals(Expression.ZERO) && coefficients.get(2).isAlwaysNonNegative() && !coefficients.get(2).equals(Expression.ZERO)) {
            // Dann ist f = 1/(a^(1/2)*|x - x_1|) mit x_1 = -b/(2*a).
            Expression zero = Expression.MINUS_ONE.mult(coefficients.get(1)).div(Expression.TWO.mult(coefficients.get(2))).simplify();
            // F = sgn(x - x_1)*ln(|x - x_1|)/a^(1/2).
            return Variable.create(var).sub(zero).sgn().mult(
                    Variable.create(var).sub(zero).abs().ln()).div(coefficients.get(2).pow(1, 2));
        }
        if (!diskriminant.equals(Expression.ZERO) && (diskriminant.isNonNegative() || diskriminant.isAlwaysNonNegative())) {
            // Hier ist D > 0.
            if ((coefficients.get(2).isNonNegative() || coefficients.get(2).isAlwaysNonNegative()) && !coefficients.get(2).equals(Expression.ZERO)) {

                // Fall a > 0, D > 0. Reduktion auf den Typ int(1/(x^2 - 1)^(1/2), x).
                // F = arcosh((2*a*x + b)/D^(1/2))/(a^(1/2)).
                return Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).div(diskriminant.pow(1, 2)).arcosh().div(coefficients.get(2).pow(1, 2));

            }
            if ((coefficients.get(2).isNonPositive() || Expression.MINUS_ONE.mult(coefficients.get(2)).simplify().isAlwaysNonNegative()) && !coefficients.get(2).equals(Expression.ZERO)) {

                // Fall a < 0, D > 0. Reduktion auf den Typ int(1/(1 - x^2)^(1/2), x).
                // F = arcsin((-2*a*x - b)/D^(1/2))/((-a)^(1/2)).
                return new Constant(-2).mult(coefficients.get(2)).mult(Variable.create(var)).sub(coefficients.get(1)).div(diskriminant.pow(1, 2)).arcsin().div(Expression.MINUS_ONE.mult(coefficients.get(2)).pow(1, 2));

            }
        }
        if (!diskriminant.equals(Expression.ZERO) && (diskriminant.isNonPositive() || Expression.MINUS_ONE.mult(diskriminant).simplify().isAlwaysNonNegative())
                && (coefficients.get(2).isNonNegative() || coefficients.get(2).isAlwaysNonNegative()) && !coefficients.get(2).equals(Expression.ZERO)) {

            // Fall a > 0, D < 0. Reduktion auf den Typ int(1/(x^2 + 1)^(1/2), x).
            // F = arsinh((2*a*x + b)/(-D)^(1/2))/(a^(1/2)).
            return Expression.TWO.mult(coefficients.get(2)).mult(Variable.create(var)).add(coefficients.get(1)).div(Expression.MINUS_ONE.mult(diskriminant).pow(1, 2)).arsinh().div(coefficients.get(2).pow(1, 2));

        }

        // Übrige, nicht eindeutig entscheidbare Fälle.
        throw new NotPreciseIntegrableException();

    }

    /**
     * Integriert Funktionen vom Typ R(exp(a*x)), R = rationale Funktion.
     *
     * @throws exceptions.EvaluationException
     * @throws exceptions.NotPreciseIntegrableException
     */
    public static Expression integrateRationalFunctionInExp(Operator expr) throws EvaluationException, NotPreciseIntegrableException {

        Expression f = (Expression) expr.getParams()[0];
        String var = (String) expr.getParams()[1];

        HashSet<Expression> argumentsInExp = new HashSet<>();

        // Konstante Summanden aus Argumenten in Exponentialfunktionen herausziehen.
        f = SpecialEquationMethods.separateConstantPartsInRationalExponentialEquations(f, var);

        if (!SpecialEquationMethods.isRationalFunktionInExp(f, var, argumentsInExp) || argumentsInExp.isEmpty()) {
            throw new NotPreciseIntegrableException();
        }

        BigInteger gcdOfEnumerators = BigInteger.ONE;
        BigInteger lcmOfDenominators = BigInteger.ONE;

        Iterator<Expression> iter = argumentsInExp.iterator();
        Expression firstArgument = iter.next();

        Expression derivativeOfFirstArgument;
        try {
            derivativeOfFirstArgument = firstArgument.diff(var).simplify();
            if (derivativeOfFirstArgument.contains(var)) {
                throw new NotPreciseIntegrableException();
            }
        } catch (EvaluationException e) {
            throw new NotPreciseIntegrableException();
        }

        Expression currentQuotient;

        while (iter.hasNext()) {
            currentQuotient = iter.next().div(firstArgument).simplify();
            // Die folgende Abfrage müsste wegen Vorbedingung immer true sein. Trotzdem sicherheitshalber!
            if (currentQuotient.isIntegerConstantOrRationalConstant()) {

                if (currentQuotient.isIntegerConstant()) {
                    gcdOfEnumerators = gcdOfEnumerators.gcd(((Constant) currentQuotient).getValue().toBigInteger());
                } else {
                    gcdOfEnumerators = gcdOfEnumerators.gcd(((Constant) ((BinaryOperation) currentQuotient).getLeft()).getValue().toBigInteger());
                    lcmOfDenominators = ArithmeticMethods.lcm(lcmOfDenominators,
                            ((Constant) ((BinaryOperation) currentQuotient).getRight()).getValue().toBigInteger());
                }

            }
        }

        // Das ist die eigentliche Substitution.
        Expression factorOfExpArgument = new Constant(gcdOfEnumerators).mult(derivativeOfFirstArgument).div(lcmOfDenominators).simplify();
        Expression substitution = new Constant(gcdOfEnumerators).mult(firstArgument).div(lcmOfDenominators).exp().simplify();

        Object fSubstituted = SubstitutionUtilities.substitute(f, var, substitution, true);
        if (fSubstituted instanceof Expression) {
            String substVar = SubstitutionUtilities.getSubstitutionVariable(f);
            Expression substitutedIntegrand = ((Expression) fSubstituted).div(factorOfExpArgument.mult(Variable.create(substVar)));
            Operator substitutedIntegral = new Operator(TypeOperator.integral, new Object[]{substitutedIntegrand, substVar});
            Expression resultFunction = indefiniteIntegration(substitutedIntegral, true);
            if (resultFunction instanceof Expression) {
                return ((Expression) resultFunction).replaceVariable(substVar, factorOfExpArgument.mult(Variable.create(var)).exp());
            }
        }

        throw new NotPreciseIntegrableException();

    }

    /**
     * Integriert Funktionen vom Typ R(sin(a*x), cos(a*x)), R = rationale
     * Funktion.
     */
    public static Object integrateRationalFunctionInTrigonometricFunctions(Operator expr) throws EvaluationException {

        // TO DO.
        return null;

    }

}
