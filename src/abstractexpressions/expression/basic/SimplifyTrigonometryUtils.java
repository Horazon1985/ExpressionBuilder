package abstractexpressions.expression.basic;

import exceptions.EvaluationException;
import exceptions.MathToolException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.Variable;
import computationbounds.ComputationBounds;
import java.math.BigInteger;
import lang.translator.Translator;
import notations.NotationLoader;

public abstract class SimplifyTrigonometryUtils {

    private static final String SU_SimplifyTrigonometry_FUNCTION_VALUE_NOT_DEFINED = "SU_SimplifyTrigonometry_FUNCTION_VALUE_NOT_DEFINED";
    
    /**
     * Private Fehlerklasse für den Fall, dass ein Ausdruck kein rationales
     * Vielfaches von Pi ist.
     */
    private static class NotRationalMultipleOfPiException extends MathToolException {

        private static final String NOT_RATIONAL_MULTIPLE_OF_PI_MESSAGE = "Expression is not a rational multiple of Pi.";

        public NotRationalMultipleOfPiException() {
            super(NOT_RATIONAL_MULTIPLE_OF_PI_MESSAGE);
        }

    }

    public static final Expression MINUS_ONE = new Constant(-1);
    public static final Expression ZERO = new Constant(0);
    public static final Expression ONE = new Constant(1);
    public static final Expression TWO = new Constant(2);
    public static final Expression THREE = new Constant(3);
    public final static Variable PI = Variable.create("pi");

    /**
     * Hilfsmethode. Gibt zurück, ob der Ausdruck expr eine Variable mit
     * ganzzahligen Werten (also eine Variable der Form K_1, K_2, ...) ist.
     */
    private static boolean isIntegerVariable(Expression expr) {
        if (!(expr instanceof Variable)) {
            return false;
        }
        String varName = ((Variable) expr).getName();
        return varName.startsWith(NotationLoader.FREE_INTEGER_PARAMETER_VAR + "_") && !varName.contains("'");
    }

    /**
     * Hilfsmethode. Gibt zurück, ob der Ausdruck expr stets ganzzahlig ist.
     * Dies soll genau dann der Fall sein, wenn expr ein Produkt aus ganzen
     * Zahlen und Variablen mit ganzzahligen Werten (also Variablen der Form
     * K_1, K_2, ...) ist.
     */
    private static boolean isExpressionIntegerValued(Expression expr) {
        ExpressionCollection factors = SimplifyUtilities.getFactors(expr);
        for (Expression factor : factors) {
            if (!factor.isIntegerConstant() && !isIntegerVariable(factor)) {
                return false;
            }
        }
        return true;
    }

    private static Expression[] getRationalFactorOfPi(Expression expr) throws NotRationalMultipleOfPiException {

        Expression[] factorOfPi = new Expression[2];

        ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(expr);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(expr);

        ExpressionCollection resultFactorsNumerator = new ExpressionCollection();
        ExpressionCollection resultFactorsDenominator = new ExpressionCollection();

        boolean numeratorContainsPi = false;

        for (Expression factor : factorsNumerator) {
            if (factor.equals(PI)) {
                if (!numeratorContainsPi) {
                    numeratorContainsPi = true;
                    continue;
                }
                throw new NotRationalMultipleOfPiException();
            }
            if (isExpressionIntegerValued(factor)) {
                resultFactorsNumerator.add(factor);
            } else {
                throw new NotRationalMultipleOfPiException();
            }
        }

        // Pi muss genau einmal vorkommen.
        if (!numeratorContainsPi) {
            throw new NotRationalMultipleOfPiException();
        }

        for (Expression factor : factorsDenominator) {
            if (isExpressionIntegerValued(factor)) {
                resultFactorsDenominator.add(factor);
            } else {
                throw new NotRationalMultipleOfPiException();
            }
        }

        try {
            // Ganyyahlige Factoren werden sofort verrechnet!
            factorOfPi[0] = SimplifyUtilities.produceProduct(resultFactorsNumerator).orderSumsAndProducts();
            factorOfPi[1] = SimplifyUtilities.produceProduct(resultFactorsDenominator).orderSumsAndProducts();
        } catch (EvaluationException e) {
            throw new NotRationalMultipleOfPiException();
        }
        return factorOfPi;

    }

    /**
     * Hilfsmethode. Gibt zurück, ob der Ausdruck expr stets ganzzahlig und
     * gerade ist. Dies soll genau dann der Fall sein, wenn expr ein Produkt aus
     * ganzen Zahlen und Variablen mit ganzzahligen Werten (also Variablen der
     * Form K_1, K_2, ...) ist und mindestens eine der Zahlen gerade ist.
     */
    private static boolean isExpressionEvenIntegerValued(Expression expr) {
        if (!isExpressionIntegerValued(expr)) {
            return false;
        }
        ExpressionCollection factors = SimplifyUtilities.getFactors(expr);
        for (Expression factor : factors) {
            if (factor.isEvenIntegerConstant()) {
                return true;
            }
        }
        return false;
    }

    public static Expression reduceSineCosineSecansCosecansIfArgumentContainsSummandOfMultipleOfPi(Function f) {

        if (!f.getType().equals(TypeFunction.sin) && !f.getType().equals(TypeFunction.cos)
                && !f.getType().equals(TypeFunction.cosec) && !f.getType().equals(TypeFunction.sec)) {
            return f;
        }

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(((Function) f).getLeft());
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(((Function) f).getLeft());

        Expression[] factorOfPi;
        BigInteger quotient;
        boolean sign = true;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            /*
             Zwei Kriterien:
             (1) Ganzzahlige Vielfache von pi werden (mit entsprechendem Vorzeichen) beseitigt.
             (2) Ganzzahlige Vielfache von pi werden (mit entsprechendem Vorzeichen) beseitigt, 
             wenn ganzzahlige Variablen (K_1, K_2, ...) auftauchen.
             */
            try {
                factorOfPi = getRationalFactorOfPi(summandsLeft.get(i));
                // Kriterium (1)
                if (factorOfPi[0].isIntegerConstant() && factorOfPi[1].isIntegerConstant()) {
                    quotient = ((Constant) factorOfPi[0]).getBigIntValue().divide(((Constant) factorOfPi[1]).getBigIntValue());
                    factorOfPi[0] = factorOfPi[0].sub(factorOfPi[1].mult(quotient));
                    summandsLeft.put(i, factorOfPi[0].mult(PI).div(factorOfPi[1]));
                    if (quotient.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ONE) == 0) {
                        sign = !sign;
                    }
                }
                // Kriterium (2)
                if (isExpressionEvenIntegerValued(factorOfPi[0]) && factorOfPi[1].equals(ONE)) {
                    summandsLeft.remove(i);
                }
            } catch (NotRationalMultipleOfPiException e) {
            }

        }

        for (int i = 0; i < summandsRight.getBound(); i++) {

            try {
                factorOfPi = getRationalFactorOfPi(summandsRight.get(i));
                // Kriterium (1)
                if (factorOfPi[0].isIntegerConstant() && factorOfPi[1].isIntegerConstant()) {
                    quotient = ((Constant) factorOfPi[0]).getBigIntValue().divide(((Constant) factorOfPi[1]).getBigIntValue());
                    factorOfPi[0] = factorOfPi[0].sub(factorOfPi[1].mult(quotient));
                    summandsRight.put(i, factorOfPi[0].mult(PI).div(factorOfPi[1]));
                    if (quotient.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ONE) == 0) {
                        sign = !sign;
                    }
                }
                // Kriterium (2)
                if (isExpressionEvenIntegerValued(factorOfPi[0]) && factorOfPi[1].equals(ONE)) {
                    summandsRight.remove(i);
                }
            } catch (NotRationalMultipleOfPiException e) {
            }

        }

        if (!sign) {
            return MINUS_ONE.mult(new Function(SimplifyUtilities.produceDifference(summandsLeft, summandsRight), f.getType()));
        }
        return new Function(SimplifyUtilities.produceDifference(summandsLeft, summandsRight), f.getType());

    }

    public static Expression interchangeSineWithCosineAndSecansWithCosecansIfArgumentContainsSummandOfMultipleOfPi(Function f) {

        if (!f.getType().equals(TypeFunction.sin) && !f.getType().equals(TypeFunction.cos)
                && !f.getType().equals(TypeFunction.cosec) && !f.getType().equals(TypeFunction.sec)) {
            return f;
        }

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(((Function) f).getLeft());
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(((Function) f).getLeft());

        Expression[] factorOfPi;
        BigInteger numerator;
        // Bedeutung: Funktionen sin/cos und sec/cosec werden vertauscht.
        boolean interchange = false;
        // Bedeutung: sign = false bedeutet ein negatives Vorzeichen vor der Funktion.
        boolean sign = true;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            try {
                factorOfPi = getRationalFactorOfPi(summandsLeft.get(i));
                if (factorOfPi[0].isIntegerConstant() && factorOfPi[1].equals(TWO)) {
                    numerator = ((Constant) factorOfPi[0]).getBigIntValue();

                    if (numerator.mod(BigInteger.valueOf(4)).equals(BigInteger.ONE)) {
                        if (f.getType().equals(TypeFunction.sin) || f.getType().equals(TypeFunction.cosec)) {
                            interchange = !interchange;
                        } else {
                            interchange = !interchange;
                            sign = !sign;
                        }
                    } else if (numerator.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
                        if (f.getType().equals(TypeFunction.sin) || f.getType().equals(TypeFunction.cosec)) {
                            interchange = !interchange;
                            sign = !sign;
                        } else {
                            interchange = !interchange;
                        }
                    }
                    summandsLeft.remove(i);

                }
            } catch (NotRationalMultipleOfPiException e) {
            }

        }

        for (int i = 0; i < summandsRight.getBound(); i++) {

            try {
                factorOfPi = getRationalFactorOfPi(summandsRight.get(i));
                if (factorOfPi[0].isIntegerConstant() && factorOfPi[1].equals(TWO)) {
                    numerator = ((Constant) factorOfPi[0]).getBigIntValue();

                    if (numerator.mod(BigInteger.valueOf(4)).equals(BigInteger.ONE)) {
                        if (f.getType().equals(TypeFunction.sin) || f.getType().equals(TypeFunction.cosec)) {
                            interchange = !interchange;
                            sign = !sign;
                        } else {
                            interchange = !interchange;
                        }
                    } else if (numerator.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
                        if (f.getType().equals(TypeFunction.sin) || f.getType().equals(TypeFunction.cosec)) {
                            interchange = !interchange;
                        } else {
                            interchange = !interchange;
                            sign = !sign;
                        }
                    }
                    summandsRight.remove(i);

                }
            } catch (NotRationalMultipleOfPiException e) {
            }

        }

        if (interchange) {
            Expression functionWithoutSign;
            if (f.getType().equals(TypeFunction.sin)) {
                functionWithoutSign = SimplifyUtilities.produceDifference(summandsLeft, summandsRight).cos();
            } else if (f.getType().equals(TypeFunction.cos)) {
                functionWithoutSign = SimplifyUtilities.produceDifference(summandsLeft, summandsRight).sin();
            } else if (f.getType().equals(TypeFunction.cosec)) {
                functionWithoutSign = SimplifyUtilities.produceDifference(summandsLeft, summandsRight).sec();
            } else {
                functionWithoutSign = SimplifyUtilities.produceDifference(summandsLeft, summandsRight).cosec();
            }
            if (!sign) {
                return MINUS_ONE.mult(functionWithoutSign);
            }
            return functionWithoutSign;
        }
        if (!sign) {
            return MINUS_ONE.mult(new Function(SimplifyUtilities.produceDifference(summandsLeft, summandsRight), f.getType()));
        }
        return new Function(SimplifyUtilities.produceDifference(summandsLeft, summandsRight), f.getType());

    }

    public static Expression reduceTangentCotangentIfArgumentContainsSummandOfMultipleOfPi(Function f) {

        if (!f.getType().equals(TypeFunction.tan) && !f.getType().equals(TypeFunction.cot)) {
            return f;
        }

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(((Function) f).getLeft());
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(((Function) f).getLeft());

        Expression[] factorOfPi;
        BigInteger quotient;
        /* 
         Gibt an, ob man beim Gesamtergebnis tan durch cot (und umgekehrt) ersetzen muss.
         Dies tritt beispielsweise dann ein, wenn im Argument ein Summand von der Form 
         (2n+1)*pi/2 vorkommt.
         */
        boolean interchangeFunctionTypes = false;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            /*
             Zwei Kriterien:
             (1) Ganzzahlige Vielfache von pi werden (mit entsprechendem Vorzeichen) beseitigt.
             (2) Ganzzahlige Vielfache von pi werden (mit entsprechendem Vorzeichen) beseitigt, 
             wenn ganzzahlige Variablen (K_1, K_2, ...) auftauchen.
             */
            try {
                factorOfPi = getRationalFactorOfPi(summandsLeft.get(i));
                // Kriterium (1)
                if (factorOfPi[0].isOddIntegerConstant() && factorOfPi[1].equals(TWO)) {
                    summandsLeft.remove(i);
                    interchangeFunctionTypes = !interchangeFunctionTypes;
                } else {
                    quotient = ((Constant) factorOfPi[0]).getBigIntValue().divide(((Constant) factorOfPi[1]).getBigIntValue());
                    factorOfPi[0] = factorOfPi[0].sub(factorOfPi[1].mult(quotient));
                    summandsLeft.put(i, factorOfPi[0].mult(PI).div(factorOfPi[1]));
                }
                // Kriterium (2)
                if (isExpressionIntegerValued(factorOfPi[0]) && factorOfPi[1].equals(ONE)) {
                    summandsLeft.remove(i);
                }
            } catch (NotRationalMultipleOfPiException e) {
            }

        }

        for (int i = 0; i < summandsRight.getBound(); i++) {

            try {
                factorOfPi = getRationalFactorOfPi(summandsRight.get(i));
                // Kriterium (1)
                if (factorOfPi[0].isOddIntegerConstant() && factorOfPi[1].equals(TWO)) {
                    summandsRight.remove(i);
                    interchangeFunctionTypes = !interchangeFunctionTypes;
                } else {
                    quotient = ((Constant) factorOfPi[0]).getBigIntValue().divide(((Constant) factorOfPi[1]).getBigIntValue());
                    factorOfPi[0] = factorOfPi[0].sub(factorOfPi[1].mult(quotient));
                    summandsRight.put(i, factorOfPi[0].mult(PI).div(factorOfPi[1]));
                }
                // Kriterium (2)
                if (isExpressionIntegerValued(factorOfPi[0]) && factorOfPi[1].equals(ONE)) {
                    summandsRight.remove(i);
                }
            } catch (NotRationalMultipleOfPiException e) {
            }

        }

        if (interchangeFunctionTypes) {
            if (f.getType().equals(TypeFunction.tan)) {
                return MINUS_ONE.mult(SimplifyUtilities.produceDifference(summandsLeft, summandsRight).cot());
            } else {
                return SimplifyUtilities.produceDifference(summandsLeft, summandsRight).tan();
            }
        }
        return new Function(SimplifyUtilities.produceDifference(summandsLeft, summandsRight), f.getType());

    }

    private static int getMaxPowerOfTwoInPrimeDecomposition(BigInteger a) {
        int maxExponentOfTwo = 0;
        while (a.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
            a = a.divide(BigInteger.valueOf(2));
            maxExponentOfTwo++;
        }
        return maxExponentOfTwo;
    }

    /**
     * Sei n = exponentOfTwo. Dies ist eine n-fache Iteration der
     * Halbwinkelformel für den Sinus: sin(x/2) = (1/2 - cos(x)/2)^(1/2).
     * Zurückgegeben wird der explizit ausgeschriebene Ausdruck für sin(x/2^n),
     * wo x = argument ist.<br>
     * VORAUSSETZUNG: 0 <= argument <= pi (Sonst gibt es Vorzeichenfehler).
     */
    private static Expression getSineByIteratingHalfAngleFormula(Expression argument, int exponentOfTwo) {
        if (exponentOfTwo <= 0) {
            return argument.cos();
        }
        if (exponentOfTwo > 1) {
            return ONE.div(TWO).sub(getCosineByIteratingHalfAngleFormula(argument, exponentOfTwo - 1).div(TWO)).pow(1, 2);
        }
        return ONE.div(TWO).sub(argument.cos().div(TWO)).pow(1, 2);
    }

    /**
     * Sei n = exponentOfTwo. Dies ist eine n-fache Iteration der
     * Halbwinkelformel für den Kosinus: cos(x/2) = (1/2 + cos(x)/2)^(1/2).
     * Zurückgegeben wird der explizit ausgeschriebene Ausdruck für cos(x/2^n),
     * wo x = argument ist.<br>
     * VORAUSSETZUNG: 0 <= argument <= pi (Sonst gibt es Vorzeichenfehler).
     */
    private static Expression getCosineByIteratingHalfAngleFormula(Expression argument, int exponentOfTwo) {
        if (exponentOfTwo <= 0) {
            return argument.cos();
        }
        if (exponentOfTwo > 1) {
            return ONE.div(TWO).add(getCosineByIteratingHalfAngleFormula(argument, exponentOfTwo - 1).div(TWO)).pow(1, 2);
        }
        return ONE.div(TWO).add(argument.cos().div(TWO)).pow(1, 2);
    }

    public static Expression reduceSine(Function f) {

        //sin(0) = 0
        if (f.getType().equals(TypeFunction.sin) && f.getLeft().equals(ZERO)) {
            return ZERO;
        }

        //sin(pi) = 0
        if (f.getType().equals(TypeFunction.sin) && f.getLeft().equals(PI)) {
            return ZERO;
        }

        //sin(k*pi) = 0
        if (f.getType().equals(TypeFunction.sin) && f.getLeft().isProduct()
                && ((BinaryOperation) f.getLeft()).getLeft().isIntegerConstant() && ((BinaryOperation) f.getLeft()).getRight().equals(PI)) {
            return ZERO;
        }

        //sin(m*pi/n) = (Sinustabelle)
        if (f.getType().equals(TypeFunction.sin) && f.getLeft().isQuotient()) {
            if (((BinaryOperation) f.getLeft()).getLeft().isProduct() && ((BinaryOperation) f.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getRight().equals(PI)) {

                    BigInteger m = ((Constant) ((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getLeft()).getBigIntValue();
                    BigInteger n = ((Constant) ((BinaryOperation) f.getLeft()).getRight()).getBigIntValue();

                    //sin(2pi/3) = 3^(1/2)/2
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(3)) == 0)) {
                        return THREE.pow(1, 2).div(TWO);
                    }
                    //sin(3pi/4) = 1/2^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(3)) == 0) && (n.compareTo(BigInteger.valueOf(4)) == 0)) {
                        return ONE.div(TWO.pow(1, 2));
                    }
                    //sin(5pi/6) = 1/2
                    if ((m.compareTo(BigInteger.valueOf(5)) == 0) && (n.compareTo(BigInteger.valueOf(6)) == 0)) {
                        return ONE.div(TWO);
                    }
                    //sin(2pi/5) = (10+2*5^(1/2))^(1/2)/4
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return new Constant(10).add(TWO.mult((new Constant(5)).pow(1, 2))).pow(1, 2).div(4);
                    }
                    //sin(3pi/5) = (10+2*5^(1/2))^(1/2)/4
                    if ((m.compareTo(BigInteger.valueOf(3)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return new Constant(10).add(TWO.mult((new Constant(5)).pow(1, 2))).pow(1, 2).div(4);
                    }
                    //sin(4pi/5) = (10-2*5^(1/2))^(1/2)/4
                    if ((m.compareTo(BigInteger.valueOf(4)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return new Constant(10).sub(TWO.mult((new Constant(5)).pow(1, 2))).pow(1, 2).div(4);
                    }
                    // Schließlich: (Sinnvolle) Iteration der Halbwinkelformel für den Sinus.
                    int exponentOfTwo = getMaxPowerOfTwoInPrimeDecomposition(n);
                    if (m.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) != 0 && exponentOfTwo > 0 && exponentOfTwo <= ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS) {

                        BigInteger quotientOfNByPowerOfTwo = n.divide(BigInteger.valueOf(2).pow(exponentOfTwo));
                        BigInteger denominator = quotientOfNByPowerOfTwo.divide(quotientOfNByPowerOfTwo.gcd(m));
                        BigInteger numerator = m.divide(quotientOfNByPowerOfTwo.gcd(m));

                        if (denominator.compareTo(BigInteger.valueOf(7)) < 0 || denominator.compareTo(BigInteger.valueOf(17)) == 0 && numerator.compareTo(BigInteger.ONE) == 0) {
                            return getSineByIteratingHalfAngleFormula(new Constant(numerator).mult(PI).div(denominator), exponentOfTwo);
                        }

                    }

                }
            }
        }

        //sin(pi/n) = (Sinustabelle)
        if (f.getType().equals(TypeFunction.sin) && f.getLeft().isQuotient()) {
            if (((BinaryOperation) f.getLeft()).getLeft().equals(PI) && ((BinaryOperation) f.getLeft()).getRight().isIntegerConstant()) {

                BigInteger n = ((Constant) ((BinaryOperation) f.getLeft()).getRight()).getBigIntValue();

                //sin(pi/6) = 1/2
                if (n.compareTo(BigInteger.valueOf(6)) == 0) {
                    return ONE.div(TWO);
                }
                //sin(pi/5) = (10-2*5^(1/2))^(1/2)/4
                if ((n.compareTo(BigInteger.valueOf(5)) == 0)) {
                    return new Constant(10).sub(TWO.mult((new Constant(5)).pow(1, 2))).pow(1, 2).div(4);
                }
                //sin(pi/4) = 1/2^(1/2)
                if (n.compareTo(BigInteger.valueOf(4)) == 0) {
                    return ONE.div(TWO.pow(1, 2));
                }
                //sin(pi/3) = 3^(1/2)/2
                if (n.compareTo(BigInteger.valueOf(3)) == 0) {
                    return THREE.pow(1, 2).div(TWO);
                }
                //sin(pi/2) = 1
                if (n.compareTo(BigInteger.valueOf(2)) == 0) {
                    return ONE;
                }
                // Schließlich: (Sinnvolle) Iteration der Halbwinkelformel für den Sinus.
                int exponentOfTwo = getMaxPowerOfTwoInPrimeDecomposition(n);
                if (exponentOfTwo > 0 && exponentOfTwo <= ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS) {

                    BigInteger quotientOfNByPowerOfTwo = n.divide(BigInteger.valueOf(2).pow(exponentOfTwo));

                    if (quotientOfNByPowerOfTwo.compareTo(BigInteger.valueOf(7)) < 0 || quotientOfNByPowerOfTwo.compareTo(BigInteger.valueOf(17)) == 0 && quotientOfNByPowerOfTwo.compareTo(BigInteger.ONE) == 0) {
                        return getSineByIteratingHalfAngleFormula(PI.div(quotientOfNByPowerOfTwo), exponentOfTwo);
                    }

                }

            }
        }

        return f;

    }

    public static Expression reduceCosine(Function f) {

        //cos(0) = 1
        if (f.getType().equals(TypeFunction.cos) && f.getLeft().equals(ZERO)) {
            return ONE;
        }

        //cos(pi) = -1
        if (f.getType().equals(TypeFunction.cos) && f.getLeft().equals(PI)) {
            return MINUS_ONE;
        }

        //cos(k*pi) = (-1)^k
        if (f.getType().equals(TypeFunction.cos) && f.getLeft().isProduct()
                && ((BinaryOperation) f.getLeft()).getLeft().isIntegerConstant() && ((BinaryOperation) f.getLeft()).getRight().equals(PI)) {
            BigInteger k = ((Constant) ((BinaryOperation) f.getLeft()).getLeft()).getBigIntValue();
            if (k.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                return ONE;
            }
            return MINUS_ONE;
        }

        //cos(m*pi/n) = (Cosinustabelle)
        if (f.getType().equals(TypeFunction.cos) && f.getLeft().isQuotient()) {
            if (((BinaryOperation) f.getLeft()).getLeft().isProduct() && ((BinaryOperation) f.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getRight().equals(PI)) {

                    BigInteger m = ((Constant) ((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getLeft()).getBigIntValue();
                    BigInteger n = ((Constant) ((BinaryOperation) f.getLeft()).getRight()).getBigIntValue();

                    //cos(2pi/3) = -1/2
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(3)) == 0)) {
                        return MINUS_ONE.div(2);
                    }
                    //cos(3pi/4) = -1/2^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(3)) == 0) && (n.compareTo(BigInteger.valueOf(4)) == 0)) {
                        return MINUS_ONE.div(TWO.pow(1, 2));
                    }
                    //cos(5pi/6) = -(3^(1/2))/2
                    if ((m.compareTo(BigInteger.valueOf(5)) == 0) && (n.compareTo(BigInteger.valueOf(6)) == 0)) {
                        return MINUS_ONE.mult(THREE.pow(1, 2)).div(2);
                    }
                    //cos(2pi/5) = 5^(1/2)/4-1/4
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return new Constant(5).pow(1, 2).div(4).sub(ONE.div(4));
                    }
                    //cos(3pi/5) = 1/4-5^(1/2)/4
                    if ((m.compareTo(BigInteger.valueOf(3)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return ONE.div(4).sub(new Constant(5).pow(1, 2).div(4));
                    }
                    //cos(4pi/5) = -1/4-5^(1/2)/4
                    if ((m.compareTo(BigInteger.valueOf(4)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return MINUS_ONE.div(4).sub((new Constant(5).pow(1, 2)).div(4));
                    }
                    //cos(2pi/17) = (-1+17^(1/2)+(2*(17-17^(1/2))^(1/2)+2(17+3*17^(1/2)-(2*(17-17^(1/2))^(1/2)-2(2*(17+17^(1/2))^(1/2))^(1/2))^(1/2))/16
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(17)) == 0)) {
                        Constant seventeen = new Constant(17);
                        Expression intermediateSqrtOfSum = (TWO.mult(seventeen.add(seventeen.pow(1, 2)))).pow(1, 2);
                        Expression intermediateSqrtOfDifference = (TWO.mult(seventeen.sub(seventeen.pow(1, 2)))).pow(1, 2);
                        return seventeen.pow(1, 2).add(intermediateSqrtOfDifference).add(
                                TWO.mult(seventeen.add(THREE.mult(seventeen.pow(1, 2))).sub(TWO.mult(intermediateSqrtOfSum).add(intermediateSqrtOfDifference)).pow(1, 2))).sub(1).div(16);
                    }
                    // Schließlich: (Sinnvolle) Iteration der Halbwinkelformel für den Kosinus.
                    int exponentOfTwo = getMaxPowerOfTwoInPrimeDecomposition(n);
                    if (m.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) != 0 && exponentOfTwo > 0 && exponentOfTwo <= ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS) {

                        BigInteger quotientOfNByPowerOfTwo = n.divide(BigInteger.valueOf(2).pow(exponentOfTwo));
                        BigInteger denominator = quotientOfNByPowerOfTwo.divide(quotientOfNByPowerOfTwo.gcd(m));
                        BigInteger numerator = m.divide(quotientOfNByPowerOfTwo.gcd(m));

                        if (denominator.compareTo(BigInteger.valueOf(7)) < 0 || denominator.compareTo(BigInteger.valueOf(17)) == 0 && numerator.compareTo(BigInteger.ONE) == 0) {
                            // Vorzeichen beachten!
                            if (m.compareTo(n) < 0) {
                                return getCosineByIteratingHalfAngleFormula(new Constant(numerator).mult(PI).div(denominator), exponentOfTwo);
                            } else {
                                return MINUS_ONE.mult(getCosineByIteratingHalfAngleFormula(new Constant(numerator).mult(PI).div(denominator), exponentOfTwo));
                            }
                        }

                    }

                }
            }
        }

        //cos(pi/n) = (Cosinustabelle)
        if (f.getType().equals(TypeFunction.cos) && f.getLeft().isQuotient()) {
            if (((BinaryOperation) f.getLeft()).getLeft().equals(PI) && ((BinaryOperation) f.getLeft()).getRight().isIntegerConstant()) {

                BigInteger n = ((Constant) ((BinaryOperation) f.getLeft()).getRight()).getBigIntValue();

                //cos(pi/6) = 3^(1/2)/2
                if (n.compareTo(BigInteger.valueOf(6)) == 0) {
                    return THREE.pow(1, 2).div(2);
                }
                //cos(pi/5) = 1/4+5^(1/2)/4
                if (n.compareTo(BigInteger.valueOf(5)) == 0) {
                    return ONE.div(4).add((new Constant(5).pow(1, 2)).div(4));
                }
                //cos(pi/4) = 1/2^(1/2)
                if (n.compareTo(BigInteger.valueOf(4)) == 0) {
                    return ONE.div(TWO.pow(1, 2));
                }
                //cos(pi/3) = 1/2
                if (n.compareTo(BigInteger.valueOf(3)) == 0) {
                    return ONE.div(2);
                }
                //cos(pi/2) = 0
                if (n.compareTo(BigInteger.valueOf(2)) == 0) {
                    return ZERO;
                }
                // Schließlich: (Sinnvolle) Iteration der Halbwinkelformel für den Kosinus.
                int exponentOfTwo = getMaxPowerOfTwoInPrimeDecomposition(n);
                if (exponentOfTwo > 0 && exponentOfTwo <= ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS) {

                    BigInteger quotientOfNByPowerOfTwo = n.divide(BigInteger.valueOf(2).pow(exponentOfTwo));

                    if (quotientOfNByPowerOfTwo.compareTo(BigInteger.valueOf(7)) < 0 || quotientOfNByPowerOfTwo.compareTo(BigInteger.valueOf(17)) == 0 && quotientOfNByPowerOfTwo.compareTo(BigInteger.ONE) == 0) {
                        return getCosineByIteratingHalfAngleFormula(PI.div(quotientOfNByPowerOfTwo), exponentOfTwo);
                    }

                }

            }
        }

        return f;

    }

    public static Expression reduceTangent(Function f) throws EvaluationException {

        //tan(0) = 0
        if (f.getType().equals(TypeFunction.tan) && f.getLeft().equals(ZERO)) {
            return ZERO;
        }

        //tan(pi) = 0
        if (f.getType().equals(TypeFunction.tan) && f.getLeft().equals(PI)) {
            return ZERO;
        }

        //tan(k*pi) = 0
        if (f.getType().equals(TypeFunction.tan) && f.getLeft().isProduct()
                && ((BinaryOperation) f.getLeft()).getLeft().isIntegerConstant() && ((BinaryOperation) f.getLeft()).getRight().equals(PI)) {
            return ZERO;
        }

        //tan(m*pi/n) = (Tangenstabelle)
        if (f.getType().equals(TypeFunction.tan) && f.getLeft().isQuotient()) {
            if (((BinaryOperation) f.getLeft()).getLeft().isProduct() && ((BinaryOperation) f.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getRight().equals(PI)) {

                    BigInteger m = ((Constant) ((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getLeft()).getBigIntValue();
                    BigInteger n = ((Constant) ((BinaryOperation) f.getLeft()).getRight()).getBigIntValue();

                    //tan(2pi/3) = -3^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(3)) == 0)) {
                        return MINUS_ONE.mult(THREE.pow(1, 2));
                    }
                    //tan(3pi/4) = -1
                    if ((m.compareTo(BigInteger.valueOf(3)) == 0) && (n.compareTo(BigInteger.valueOf(4)) == 0)) {
                        return MINUS_ONE;
                    }
                    //tan(5pi/6) = -1/3^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(5)) == 0) && (n.compareTo(BigInteger.valueOf(6)) == 0)) {
                        return MINUS_ONE.div(THREE.pow(1, 2));
                    }
                    //tan(2pi/5) = (5+2*5^(1/2))^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return (new Constant(5).add(TWO.mult(new Constant(5).pow(1, 2)))).pow(1, 2);
                    }
                    //tan(4pi/5) = -(5-2*5^(1/2))^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(4)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return MINUS_ONE.mult(new Constant(5).sub(TWO.mult(new Constant(5).pow(1, 2))).pow(1, 2));
                    }
                    // Schließlich: (Sinnvolle) Iteration der Halbwinkelformel für den Tangens: tan(x/2) = (1 - cos(x))/sin(x).
                    int exponentOfTwo = getMaxPowerOfTwoInPrimeDecomposition(n);
                    if (m.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) != 0 && exponentOfTwo > 0 && exponentOfTwo <= ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS) {

                        BigInteger quotientOfNByPowerOfTwo = n.divide(BigInteger.valueOf(2).pow(exponentOfTwo));

                        if (quotientOfNByPowerOfTwo.compareTo(BigInteger.valueOf(7)) < 0) {
                            Expression doubleAngle = new Constant(m).mult(PI).div(n.divide(BigInteger.valueOf(2)));
                            return ONE.sub(doubleAngle.cos()).div(doubleAngle.sin());
                        }

                    }

                }
            }
        }

        //tan(pi/n) = (Tangenstabelle)
        if (f.getType().equals(TypeFunction.tan) && f.getLeft().isQuotient()) {
            if (((BinaryOperation) f.getLeft()).getLeft().equals(PI) && ((BinaryOperation) f.getLeft()).getRight().isIntegerConstant()) {

                BigInteger n = ((Constant) ((BinaryOperation) f.getLeft()).getRight()).getBigIntValue();

                //tan(pi/6) = 1/3^(1/2)
                if (n.compareTo(BigInteger.valueOf(6)) == 0) {
                    return ONE.div(THREE.pow(1, 2));
                }
                //tan(pi/5) = (5-2*5^(1/2))^(1/2)
                if (n.compareTo(BigInteger.valueOf(5)) == 0) {
                    return new Constant(5).sub(TWO.mult(new Constant(5).pow(1, 2))).pow(1, 2);
                }
                //tan(pi/4) = 1
                if (n.compareTo(BigInteger.valueOf(4)) == 0) {
                    return ONE;
                }
                //tan(pi/3) = 3^(1/2)
                if (n.compareTo(BigInteger.valueOf(3)) == 0) {
                    return THREE.pow(1, 2);
                }
                //tan(pi/2) = FEHLER!
                if (n.compareTo(BigInteger.valueOf(2)) == 0) {
                    throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyTrigonometry_FUNCTION_VALUE_NOT_DEFINED, f));
                }
                // Schließlich: (Sinnvolle) Iteration der Halbwinkelformel für den Tangens: tan(x/2) = (1 - cos(x))/sin(x).
                int exponentOfTwo = getMaxPowerOfTwoInPrimeDecomposition(n);
                if (exponentOfTwo > 0 && exponentOfTwo <= ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS) {

                    BigInteger quotientOfNByPowerOfTwo = n.divide(BigInteger.valueOf(2).pow(exponentOfTwo));

                    if (quotientOfNByPowerOfTwo.compareTo(BigInteger.valueOf(7)) < 0) {
                        Expression doubleAngle = PI.div(n.divide(BigInteger.valueOf(2)));
                        return ONE.sub(doubleAngle.cos()).div(doubleAngle.sin());
                    }

                }

            }
        }

        return f;

    }

    public static Expression reduceCotangent(Function f) throws EvaluationException {

        //cot(0) = FEHLER!
        if (f.equals(ZERO.cot()) || f.equals(PI.cot())) {
            throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyTrigonometry_FUNCTION_VALUE_NOT_DEFINED, f));
        }

        //cot(k*pi) = FEHLER!
        if (f.getType().equals(TypeFunction.cot) && f.getLeft().isProduct()
                && ((BinaryOperation) f.getLeft()).getLeft().isIntegerConstant() && ((BinaryOperation) f.getLeft()).getRight().equals(PI)) {
            throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyTrigonometry_FUNCTION_VALUE_NOT_DEFINED, f));
        }

        //cot(m*pi/n) = (Kotangenstabelle)
        if (f.getType().equals(TypeFunction.cot) && f.getLeft().isQuotient()) {
            if (((BinaryOperation) f.getLeft()).getLeft().isProduct() && ((BinaryOperation) f.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getRight().equals(PI)) {

                    BigInteger m = ((Constant) ((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getLeft()).getBigIntValue();
                    BigInteger n = ((Constant) ((BinaryOperation) f.getLeft()).getRight()).getBigIntValue();

                    //cot(2pi/3) = -1/3^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(3)) == 0)) {
                        return MINUS_ONE.div(THREE.pow(1, 2));
                    }
                    //cot(3pi/4) = -1
                    if ((m.compareTo(BigInteger.valueOf(3)) == 0) && (n.compareTo(BigInteger.valueOf(4)) == 0)) {
                        return MINUS_ONE;
                    }
                    //cot(5pi/6) = -3^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(5)) == 0) && (n.compareTo(BigInteger.valueOf(6)) == 0)) {
                        return MINUS_ONE.mult(THREE.pow(1, 2));
                    }
                    //cot(2pi/5) = (1-2/5^(1/2))^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return ONE.sub(TWO.div(new Constant(5).pow(1, 2))).pow(1, 2);
                    }
                    //cot(4pi/5) = -(1+2/5^(1/2))^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(4)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return MINUS_ONE.mult(ONE.add(TWO.div(new Constant(5).pow(1, 2))).pow(1, 2));
                    }
                    // Schließlich: (Sinnvolle) Iteration der Halbwinkelformel für den Kotangens: cot(x/2) = sin(x)/(1 - cos(x)).
                    int exponentOfTwo = getMaxPowerOfTwoInPrimeDecomposition(n);
                    if (m.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) != 0 && exponentOfTwo > 0 && exponentOfTwo <= ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS) {

                        BigInteger quotientOfNByPowerOfTwo = n.divide(BigInteger.valueOf(2).pow(exponentOfTwo));

                        if (quotientOfNByPowerOfTwo.compareTo(BigInteger.valueOf(7)) < 0) {
                            Expression doubleAngle = new Constant(m).mult(PI).div(n.divide(BigInteger.valueOf(2)));
                            return doubleAngle.sin().div(ONE.sub(doubleAngle.cos()));
                        }

                    }

                }
            }
        }

        //cot(pi/n) = (Kotangenstabelle)
        if (f.getType().equals(TypeFunction.cot) && f.getLeft().isQuotient()) {
            if (((BinaryOperation) f.getLeft()).getLeft().equals(PI) && ((BinaryOperation) f.getLeft()).getRight().isIntegerConstant()) {

                BigInteger n = ((Constant) ((BinaryOperation) f.getLeft()).getRight()).getBigIntValue();

                //cot(pi/6) = 3^(1/2)
                if (n.compareTo(BigInteger.valueOf(6)) == 0) {
                    return THREE.pow(1, 2);
                }
                //cot(pi/5) = (1+2/5^(1/2))^(1/2)
                if (n.compareTo(BigInteger.valueOf(5)) == 0) {
                    return ONE.add(TWO.div(new Constant(5).pow(1, 2))).pow(1, 2);
                }
                //cot(pi/4) = 1
                if (n.compareTo(BigInteger.valueOf(4)) == 0) {
                    return ONE;
                }
                //cot(pi/3) = 1/3^(1/2)
                if (n.compareTo(BigInteger.valueOf(3)) == 0) {
                    return ONE.div(THREE.pow(1, 2));
                }
                //cot(pi/2) = 0!
                if (n.compareTo(BigInteger.valueOf(2)) == 0) {
                    return ZERO;
                }
                // Schließlich: (Sinnvolle) Iteration der Halbwinkelformel für den Kotangens: cot(x/2) = sin(x)/(1 - cos(x)).
                int exponentOfTwo = getMaxPowerOfTwoInPrimeDecomposition(n);
                if (exponentOfTwo > 0 && exponentOfTwo <= ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS) {

                    BigInteger quotientOfNByPowerOfTwo = n.divide(BigInteger.valueOf(2).pow(exponentOfTwo));

                    if (quotientOfNByPowerOfTwo.compareTo(BigInteger.valueOf(7)) < 0) {
                        Expression doubleAngle = PI.div(n.divide(BigInteger.valueOf(2)));
                        return doubleAngle.sin().div(ONE.sub(doubleAngle.cos()));
                    }

                }

            }
        }

        return f;

    }

    public static Expression reduceCosecans(Function f) throws EvaluationException {

        // cosec(0) = FEHLER!, cosec(pi) = FEHLER!
        if (f.equals(ZERO.cosec()) || f.equals(PI.cosec())) {
            throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyTrigonometry_FUNCTION_VALUE_NOT_DEFINED, f));
        }

        // cosec(k*pi) = FEHLER!
        if (f.getType().equals(TypeFunction.cosec) && f.getLeft().isProduct()
                && ((BinaryOperation) f.getLeft()).getLeft().isIntegerConstant() && ((BinaryOperation) f.getLeft()).getRight().equals(PI)) {
            throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyTrigonometry_FUNCTION_VALUE_NOT_DEFINED, f));
        }

        // cosec(m*pi/n) = (Kosecanstabelle)
        if (f.getType().equals(TypeFunction.cosec) && f.getLeft().isQuotient()) {
            if (((BinaryOperation) f.getLeft()).getLeft().isProduct() && ((BinaryOperation) f.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getRight().equals(PI)) {

                    BigInteger m = ((Constant) ((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getLeft()).getBigIntValue();
                    BigInteger n = ((Constant) ((BinaryOperation) f.getLeft()).getRight()).getBigIntValue();

                    //cosec(2pi/3) = 2/3^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(3)) == 0)) {
                        return TWO.div(THREE.pow(1, 2));
                    }
                    //cosec(3pi/4) = 2^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(3)) == 0) && (n.compareTo(BigInteger.valueOf(4)) == 0)) {
                        return TWO.pow(1, 2);
                    }
                    //cosec(5pi/6) = 2
                    if ((m.compareTo(BigInteger.valueOf(5)) == 0) && (n.compareTo(BigInteger.valueOf(6)) == 0)) {
                        return TWO;
                    }
                    //cosec(2pi/5) = (2-2/5^(1/2))^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return TWO.sub(TWO.div(new Constant(5).pow(1, 2))).pow(1, 2);
                    }
                    //cosec(4pi/5) = (2+2/5^(1/2))^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(4)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return TWO.add(TWO.div(new Constant(5).pow(1, 2))).pow(1, 2);
                    }
                    // Schließlich: (Sinnvolle) Iteration der Halbwinkelformel für den Kosecans.
                    int exponentOfTwo = getMaxPowerOfTwoInPrimeDecomposition(n);
                    if (m.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) != 0 && exponentOfTwo > 0 && exponentOfTwo <= ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS) {

                        BigInteger quotientOfNByPowerOfTwo = n.divide(BigInteger.valueOf(2).pow(exponentOfTwo));
                        BigInteger denominator = quotientOfNByPowerOfTwo.divide(quotientOfNByPowerOfTwo.gcd(m));
                        BigInteger numerator = m.divide(quotientOfNByPowerOfTwo.gcd(m));

                        if (denominator.compareTo(BigInteger.valueOf(7)) < 0 || denominator.compareTo(BigInteger.valueOf(17)) == 0 && numerator.compareTo(BigInteger.ONE) == 0) {
                            return ONE.div(getSineByIteratingHalfAngleFormula(new Constant(numerator).mult(PI).div(denominator), exponentOfTwo));
                        }

                    }

                }
            }
        }

        //cosec(pi/n) = (Kosecanstabelle)
        if (f.getType().equals(TypeFunction.cosec) && f.getLeft().isQuotient()) {
            if (((BinaryOperation) f.getLeft()).getLeft().equals(PI) && ((BinaryOperation) f.getLeft()).getRight().isIntegerConstant()) {

                BigInteger n = ((Constant) ((BinaryOperation) f.getLeft()).getRight()).getBigIntValue();

                //cosec(pi/6) = 2
                if (n.compareTo(BigInteger.valueOf(6)) == 0) {
                    return TWO;
                }
                //cosec(pi/5) = (2+2/5^(1/2))^(1/2)
                if (n.compareTo(BigInteger.valueOf(5)) == 0) {
                    return TWO.add(TWO.div(new Constant(5).pow(1, 2))).pow(1, 2);
                }
                //cosec(pi/4) = 2^(1/2)
                if (n.compareTo(BigInteger.valueOf(4)) == 0) {
                    return TWO.pow(1, 2);
                }
                //cosec(pi/3) = 2/3^(1/2)
                if (n.compareTo(BigInteger.valueOf(3)) == 0) {
                    return TWO.div(THREE.pow(1, 2));
                }
                //cosec(pi/2) = 1
                if (n.compareTo(BigInteger.valueOf(2)) == 0) {
                    return ONE;
                }
                // Schließlich: (Sinnvolle) Iteration der Halbwinkelformel für den Kosecans.
                int exponentOfTwo = getMaxPowerOfTwoInPrimeDecomposition(n);
                if (exponentOfTwo > 0 && exponentOfTwo <= ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS) {

                    BigInteger quotientOfNByPowerOfTwo = n.divide(BigInteger.valueOf(2).pow(exponentOfTwo));

                    if (quotientOfNByPowerOfTwo.compareTo(BigInteger.valueOf(7)) < 0 || quotientOfNByPowerOfTwo.compareTo(BigInteger.valueOf(17)) == 0 && quotientOfNByPowerOfTwo.compareTo(BigInteger.ONE) == 0) {
                        return ONE.div(getSineByIteratingHalfAngleFormula(PI.div(quotientOfNByPowerOfTwo), exponentOfTwo));
                    }

                }

            }
        }

        return f;

    }

    public static Expression reduceSecans(Function f) throws EvaluationException {

        //sec(0) = 1
        if (f.getType().equals(TypeFunction.sec) && f.getLeft().equals(ZERO)) {
            return ONE;
        }

        //sec(pi) = -1
        if (f.getType().equals(TypeFunction.sec) && f.getLeft().equals(PI)) {
            return MINUS_ONE;
        }

        //sec(k*pi) = (-1)^k
        if (f.getType().equals(TypeFunction.sec) && f.getLeft().isProduct()
                && ((BinaryOperation) f.getLeft()).getLeft().isIntegerConstant() && ((BinaryOperation) f.getLeft()).getRight().equals(PI)) {
            BigInteger k = ((Constant) ((BinaryOperation) f.getLeft()).getLeft()).getBigIntValue();
            if (k.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                return ONE;
            }
            return MINUS_ONE;
        }

        //sec(m*pi/n) = (Secanstabelle)
        if (f.getType().equals(TypeFunction.sec) && f.getLeft().isQuotient()) {
            if (((BinaryOperation) f.getLeft()).getLeft().isProduct() && ((BinaryOperation) f.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getRight().equals(PI)) {

                    BigInteger m = ((Constant) ((BinaryOperation) ((BinaryOperation) f.getLeft()).getLeft()).getLeft()).getBigIntValue();
                    BigInteger n = ((Constant) ((BinaryOperation) f.getLeft()).getRight()).getBigIntValue();

                    //sec(2pi/3) = -2
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(3)) == 0)) {
                        return new Constant(-2);
                    }
                    //sec(3pi/4) = -2^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(3)) == 0) && (n.compareTo(BigInteger.valueOf(4)) == 0)) {
                        return (MINUS_ONE).mult(TWO.pow(1, 2));
                    }
                    //sec(5pi/6) = -2/3^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(5)) == 0) && (n.compareTo(BigInteger.valueOf(6)) == 0)) {
                        return (new Constant(-2)).div(THREE.pow(1, 2));
                    }
                    //sec(2pi/5) = 4/(5^(1/2)-1) = 1 + 5^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(2)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return ONE.add(new Constant(5).pow(1, 2));
                    }
                    //sec(4pi/5) = -4/(1+5^(1/2)) = 1 - 5^(1/2)
                    if ((m.compareTo(BigInteger.valueOf(4)) == 0) && (n.compareTo(BigInteger.valueOf(5)) == 0)) {
                        return ONE.sub(new Constant(5).pow(1, 2));
                    }
                    // Schließlich: (Sinnvolle) Iteration der Halbwinkelformel für den Secans.
                    int exponentOfTwo = getMaxPowerOfTwoInPrimeDecomposition(n);
                    if (m.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) != 0 && exponentOfTwo > 0 && exponentOfTwo <= ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS) {

                        BigInteger quotientOfNByPowerOfTwo = n.divide(BigInteger.valueOf(2).pow(exponentOfTwo));
                        BigInteger denominator = quotientOfNByPowerOfTwo.divide(quotientOfNByPowerOfTwo.gcd(m));
                        BigInteger numerator = m.divide(quotientOfNByPowerOfTwo.gcd(m));

                        if (denominator.compareTo(BigInteger.valueOf(7)) < 0 || denominator.compareTo(BigInteger.valueOf(17)) == 0 && numerator.compareTo(BigInteger.ONE) == 0) {
                            // Vorzeichen beachten!
                            if (m.compareTo(n) < 0) {
                                return ONE.div(getCosineByIteratingHalfAngleFormula(new Constant(numerator).mult(PI).div(denominator), exponentOfTwo));
                            } else {
                                return MINUS_ONE.div(getCosineByIteratingHalfAngleFormula(new Constant(numerator).mult(PI).div(denominator), exponentOfTwo));
                            }
                        }

                    }

                }
            }
        }

        //sec(pi/n) = (Secanstabelle)
        if (f.getType().equals(TypeFunction.sec) && f.getLeft().isQuotient()) {
            if (((BinaryOperation) f.getLeft()).getLeft().equals(PI) && ((BinaryOperation) f.getLeft()).getRight().isIntegerConstant()) {

                BigInteger n = ((Constant) ((BinaryOperation) f.getLeft()).getRight()).getBigIntValue();

                //sec(pi/6) = 2/3^(1/2)
                if (n.compareTo(BigInteger.valueOf(6)) == 0) {
                    return TWO.div(THREE.pow(1, 2));
                }
                //sec(pi/5) = 5^(1/2) - 1
                if (n.compareTo(BigInteger.valueOf(5)) == 0) {
                    return new Constant(5).pow(1, 2).sub(ONE);
                }
                //sec(pi/4) = 2^(1/2)
                if (n.compareTo(BigInteger.valueOf(4)) == 0) {
                    return TWO.pow(1, 2);
                }
                //sec(pi/3) = 2
                if (n.compareTo(BigInteger.valueOf(3)) == 0) {
                    return TWO;
                }
                //sec(pi/2) = FEHLER!
                if (n.compareTo(BigInteger.valueOf(2)) == 0) {
                    throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyTrigonometry_FUNCTION_VALUE_NOT_DEFINED, f));
                }
                // Schließlich: (Sinnvolle) Iteration der Halbwinkelformel für den Secans.
                int exponentOfTwo = getMaxPowerOfTwoInPrimeDecomposition(n);
                if (exponentOfTwo > 0 && exponentOfTwo <= ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_TWO_FOR_COMPUTING_VALUES_OF_TRIGONOMETRICAL_FUNCTIONS) {

                    BigInteger quotientOfNByPowerOfTwo = n.divide(BigInteger.valueOf(2).pow(exponentOfTwo));

                    if (quotientOfNByPowerOfTwo.compareTo(BigInteger.valueOf(7)) < 0 || quotientOfNByPowerOfTwo.compareTo(BigInteger.valueOf(17)) == 0 && quotientOfNByPowerOfTwo.compareTo(BigInteger.ONE) == 0) {
                        return ONE.div(getCosineByIteratingHalfAngleFormula(PI.div(quotientOfNByPowerOfTwo), exponentOfTwo));
                    }

                }

            }
        }

        return f;

    }

    public static Expression reduceArcsine(Function f) {

        if (f.getType().equals(TypeFunction.arcsin) && f.getLeft().isConstant()) {

            // arcsin(0) = 0
            if (f.getLeft().equals(ZERO)) {
                return ZERO;
            }
            // arcsin(1) = pi/2
            if (f.getLeft().equals(ONE)) {
                return PI.div(2);
            }

            // Arcsin-Tabelle
            //arcsin(1/2) = pi/6
            if (f.getLeft().equivalent(ONE.div(TWO))) {
                return PI.div(6);
            }
            //arcsin(1/2^(1/2)) = pi/4
            if (f.getLeft().equivalent(ONE.div(TWO.pow(1, 2)))) {
                return PI.div(4);
            }
            //arcsin(3^(1/2)/2) = pi/3
            if (f.getLeft().equivalent(THREE.pow(1, 2).div(TWO))) {
                return PI.div(3);
            }

        }

        return f;

    }

    public static Expression reduceArccosine(Function f) {

        if (f.getType().equals(TypeFunction.arccos) && f.getLeft().isConstant()) {

            // arccos(0) = pi/2
            if (f.getLeft().equals(ZERO)) {
                return PI.div(2);
            }
            // arccos(1) = 0
            if (f.getLeft().equals(ONE)) {
                return ZERO;
            }
            // arccos(-1) = pi
            if (f.getLeft().equals(MINUS_ONE)) {
                return PI;
            }

            // Arccos-Tabelle
            //arccos(-1/2) = 2*pi/3
            if (f.getLeft().equivalent(MINUS_ONE.div(TWO))) {
                return TWO.mult(PI).div(3);
            }
            //arccos(-1/2^(1/2)) = 3*pi/4
            if (f.getLeft().equivalent(MINUS_ONE.div(TWO.pow(1, 2)))) {
                return THREE.mult(PI).div(4);
            }
            //arccos(-3^(1/2)/2) = 5*pi/6
            if (f.getLeft().equivalent((MINUS_ONE.mult(THREE.pow(1, 2))).div(TWO))) {
                return (new Constant(5)).mult(PI).div(6);
            }
            //arccos(1/2) = pi/3
            if (f.getLeft().equivalent(ONE.div(TWO))) {
                return PI.div(3);
            }
            //arccos(1/2^(1/2)) = pi/4
            if (f.getLeft().equivalent(ONE.div(TWO.pow(1, 2)))) {
                return PI.div(4);
            }
            //arccos(3^(1/2)/2) = pi/6
            if (f.getLeft().equivalent(THREE.pow(1, 2).div(2))) {
                return PI.div(6);
            }

        }

        return f;

    }

    public static Expression reduceArctangent(Function f) {

        if (f.getType().equals(TypeFunction.arctan) && f.getLeft().isConstant()) {

            // Arctan-Tabelle
            // arctan(0) = 0
            if (f.getLeft().equals(ZERO)) {
                return ZERO;
            }
            // arctan(1) = pi/4
            if (f.getLeft().equals(ONE)) {
                return PI.div(4);
            }
            //arctan(1/3^(1/2)) = pi/6
            if (f.getLeft().equivalent(ONE.div(THREE.pow(1, 2)))) {
                return PI.div(6);
            }
            //arctan(3^(1/2)) = pi/3
            if (f.getLeft().equivalent(THREE.pow(1, 2))) {
                return PI.div(3);
            }
            //arctan(2^(1/2)-1) = pi/8
            if (f.getLeft().equivalent(TWO.pow(1, 2).sub(1))) {
                return PI.div(8);
            }
            //arctan(1+2^(1/2)) = 3*pi/8
            if (f.getLeft().equivalent(ONE.add(TWO.pow(1, 2)))) {
                return THREE.mult(PI).div(8);
            }
            //arctan(1-2^(1/2)) = -pi/8
            if (f.getLeft().equivalent(ONE.sub(TWO.pow(1, 2)))) {
                return MINUS_ONE.mult(PI).div(8);
            }
            //arctan(-1-2^(1/2)) = -3*pi/8
            if (f.getLeft().equivalent(MINUS_ONE.sub(TWO.pow(1, 2)))) {
                return new Constant(-3).mult(PI).div(8);
            }

        }

        return f;

    }

    public static Expression reduceArccotangent(Function f) {

        if (f.getType().equals(TypeFunction.arccot) && f.getLeft().isConstant()) {

            // Arccot-Tabelle
            // arccot(0) = pi/2
            if (f.getLeft().equals(ZERO)) {
                return PI.div(2);
            }
            // arccot(1) = pi/4
            if (f.getLeft().equals(ONE)) {
                return PI.div(4);
            }
            // arccot(1/3^(1/2)) = pi/3
            if (f.getLeft().equivalent(ONE.div(THREE.pow(1, 2)))) {
                return PI.div(3);
            }
            // arccot(3^(1/2)) = pi/6
            if (f.getLeft().equivalent(THREE.pow(1, 2))) {
                return PI.div(6);
            }
            //arccot(2^(1/2)-1) = 3*pi/8
            if (f.getLeft().equivalent(TWO.pow(1, 2).sub(1))) {
                return THREE.mult(PI).div(8);
            }
            //arccot(1+2^(1/2)) = pi/8
            if (f.getLeft().equivalent(ONE.add(TWO.pow(1, 2)))) {
                return PI.div(8);
            }
            //arccot(1-2^(1/2)) = -3*pi/8
            if (f.getLeft().equivalent(ONE.sub(TWO.pow(1, 2)))) {
                return new Constant(-3).mult(PI).div(8);
            }
            //arccot(-1-2^(1/2)) = -pi/8
            if (f.getLeft().equivalent(MINUS_ONE.sub(TWO.pow(1, 2)))) {
                return MINUS_ONE.mult(PI).div(8);
            }

        }

        return f;

    }

    public static Expression reduceArccosecans(Function f) throws EvaluationException {

        if (f.getType().equals(TypeFunction.arccosec) && f.getLeft().isConstant()) {

            // arccosec(0) = 0
            if (f.getLeft().equals(ZERO)) {
                throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyTrigonometry_FUNCTION_VALUE_NOT_DEFINED, f));
            }
            // arccosec(1) = pi/2
            if (f.getLeft().equals(ONE)) {
                return PI.div(2);
            }

            // Arccosec-Tabelle
            //arccosec(2) = pi/6
            if (f.getLeft().equivalent(TWO)) {
                return PI.div(6);
            }
            //arccosec(2^(1/2)) = pi/4
            if (f.getLeft().equivalent(TWO.pow(1, 2))) {
                return PI.div(4);
            }
            //arccosec(2/3^(1/2)) = pi/3
            if (f.getLeft().equivalent(TWO.div(THREE.pow(1, 2)))) {
                return PI.div(3);
            }

        }

        return f;

    }

    public static Expression reduceArcsecans(Function f) throws EvaluationException {

        if (f.getType().equals(TypeFunction.arcsec) && f.getLeft().isConstant()) {

            // arcsec(0) = FEHLER!
            if (f.getLeft().equals(ZERO)) {
                throw new EvaluationException(Translator.translateOutputMessage(SU_SimplifyTrigonometry_FUNCTION_VALUE_NOT_DEFINED, f));
            }
            // arcsec(1) = 0
            if (f.getLeft().equals(ONE)) {
                return ZERO;
            }
            // arcsec(-1) = pi
            if (f.getLeft().equals(MINUS_ONE)) {
                return PI;
            }

            // Arcsec-Tabelle
            //arcsec(-2) = 2*pi/3
            if (f.getLeft().equivalent(new Constant(-2))) {
                return TWO.mult(PI).div(3);
            }
            //arcsec(-2^(1/2)) = 3*pi/4
            if (f.getLeft().equivalent(MINUS_ONE.mult(TWO.pow(1, 2)))) {
                return THREE.mult(PI).div(4);
            }
            //arcsec(-2/3^(1/2)) = 5*pi/6
            if (f.getLeft().equivalent(new Constant(-2).div(THREE.pow(1, 2)))) {
                return (new Constant(5)).mult(PI).div(6);
            }
            //arcsec(2) = pi/3
            if (f.getLeft().equivalent(TWO)) {
                return PI.div(3);
            }
            //arcsec(2^(1/2)) = pi/4
            if (f.getLeft().equivalent(TWO.pow(1, 2))) {
                return PI.div(4);
            }
            //arcsec(2/3^(1/2)) = pi/6
            if (f.getLeft().equivalent(TWO.div(THREE.pow(1, 2)))) {
                return PI.div(6);
            }

        }

        return f;

    }

}
