package expressionsimplifymethods;

import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import expressionbuilder.Variable;
import java.math.BigDecimal;
import java.math.BigInteger;
import translator.Translator;

public class SimplifyTrigonometry {

    public static final Expression MINUS_ONE = new Constant(-1);
    public static final Expression ZERO = new Constant(0);
    public static final Expression ONE = new Constant(1);
    public static final Expression TWO = new Constant(2);
    public static final Expression THREE = new Constant(3);
    public final static Variable PI = Variable.create("pi");

    public static Expression reduceSine(Function expr) {

        //sin(0) = 0
        if (expr.getType().equals(TypeFunction.sin) && expr.getLeft().equals(ZERO)) {
            return ZERO;
        }

        //sin(pi) = 0
        if (expr.getType().equals(TypeFunction.sin) && expr.getLeft().equals(PI)) {
            return ZERO;
        }

        //sin(k*pi) = 0
        if (expr.getType().equals(TypeFunction.sin) && expr.getLeft().isProduct()
                && ((BinaryOperation) expr.getLeft()).getLeft().isIntegerConstant() && ((BinaryOperation) expr.getLeft()).getRight().equals(PI)) {
            return ZERO;
        }

        //sin(m*pi/n) = sin((m - 2k*n)*pi/n) mit k = rounddown(m/(2n))
        if (expr.getType().equals(TypeFunction.sin) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().isProduct() && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getRight().equals(PI)) {

                    // m, n sind bereits ganzzahlig!
                    BigDecimal m = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft()).getValue();
                    BigDecimal n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue();
                    BigInteger multiple = m.divide(n, 0, BigDecimal.ROUND_DOWN).toBigInteger();

                    if (m.multiply(n).compareTo(BigDecimal.ZERO) < 0) {
                        multiple = multiple.subtract(BigInteger.ONE);
                    }
                    if (multiple.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                        BigDecimal k = m.divide(n.multiply(BigDecimal.valueOf(2)), 0, BigDecimal.ROUND_DOWN);
                        if (k.compareTo(BigDecimal.ZERO) != 0) {
                            return new Constant(m.subtract(k.multiply(BigDecimal.valueOf(2).multiply(n)))).mult(
                                    PI).div(n).sin();
                        }
                    } else {
                        return MINUS_ONE.mult(new Constant(m.subtract(new BigDecimal(multiple).multiply(n))).mult(
                                PI).div(n).sin());
                    }

                }
            }
        }

        //sin(m*pi/n) = (Sinustabelle)
        if (expr.getType().equals(TypeFunction.sin) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().isProduct() && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getRight().equals(PI)) {

                    BigInteger m = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft()).getValue().toBigInteger();
                    BigInteger n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

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

                }
            }
        }

        //sin(pi/n) = (Sinustabelle)
        if (expr.getType().equals(TypeFunction.sin) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().equals(PI) && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {

                BigInteger n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

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

            }
        }

        return expr;

    }

    public static Expression reduceCosine(Function expr) {

        //cos(0) = 1
        if (expr.getType().equals(TypeFunction.cos) && expr.getLeft().equals(ZERO)) {
            return ONE;
        }

        //cos(pi) = -1
        if (expr.getType().equals(TypeFunction.cos) && expr.getLeft().equals(PI)) {
            return MINUS_ONE;
        }

        //cos(k*pi) = (-1)^k
        if (expr.getType().equals(TypeFunction.cos) && expr.getLeft().isProduct()
                && ((BinaryOperation) expr.getLeft()).getLeft().isIntegerConstant() && ((BinaryOperation) expr.getLeft()).getRight().equals(PI)) {
            BigInteger k = ((Constant) ((BinaryOperation) expr.getLeft()).getLeft()).getValue().toBigInteger();
            if (k.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                return ONE;
            }
            return MINUS_ONE;
        }

        //cos(m*pi/n) = cos((m - 2k*n)*pi/n) mit k = rounddown(m/(2n))
        if (expr.getType().equals(TypeFunction.cos) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().isProduct() && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getRight().equals(PI)) {

                    // m, n sind bereits ganzzahlig!
                    BigDecimal m = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft()).getValue();
                    BigDecimal n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue();
                    BigInteger multiple = m.divide(n, 0, BigDecimal.ROUND_DOWN).toBigInteger();
                    if (m.multiply(n).compareTo(BigDecimal.ZERO) < 0) {
                        multiple = multiple.subtract(BigInteger.ONE);
                    }

                    if (multiple.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                        BigDecimal k = m.divide(n.multiply(BigDecimal.valueOf(2)), 0, BigDecimal.ROUND_DOWN);
                        if (k.compareTo(BigDecimal.ZERO) != 0) {
                            return new Constant(m.subtract(k.multiply(BigDecimal.valueOf(2).multiply(n)))).mult(
                                    PI).div(n).cos();
                        }
                    } else {
                        return MINUS_ONE.mult(new Constant(m.subtract(new BigDecimal(multiple).multiply(n))).mult(
                                PI).div(n).cos());
                    }

                }
            }
        }

        //cos(m*pi/n) = (Cosinustabelle)
        if (expr.getType().equals(TypeFunction.cos) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().isProduct() && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getRight().equals(PI)) {

                    BigInteger m = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft()).getValue().toBigInteger();
                    BigInteger n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

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

                }
            }
        }

        //cos(pi/n) = (Cosinustabelle)
        if (expr.getType().equals(TypeFunction.cos) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().equals(PI) && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {

                BigInteger n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

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

            }
        }

        return expr;

    }

    public static Expression reduceTangent(Function expr) throws EvaluationException {

        //tan(0) = 0
        if (expr.getType().equals(TypeFunction.tan) && expr.getLeft().equals(ZERO)) {
            return ZERO;
        }

        //tan(pi) = 0
        if (expr.getType().equals(TypeFunction.tan) && expr.getLeft().equals(PI)) {
            return ZERO;
        }

        //tan(k*pi) = 0
        if (expr.getType().equals(TypeFunction.tan) && expr.getLeft().isProduct()
                && ((BinaryOperation) expr.getLeft()).getLeft().isIntegerConstant() && ((BinaryOperation) expr.getLeft()).getRight().equals(PI)) {
            return ZERO;
        }

        //tan(m*pi/n) = tan((m - k*n)*pi/n) mit k = rounddown(m/n)
        if (expr.getType().equals(TypeFunction.tan) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().isProduct() && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getRight().equals(PI)) {

                    // m, n sind bereits ganzzahlig!
                    BigDecimal m = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft()).getValue();
                    BigDecimal n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue();
                    BigDecimal k = m.divide(n, 0, BigDecimal.ROUND_DOWN);
                    if (k.compareTo(BigDecimal.ZERO) != 0) {
                        return new Constant(m.subtract(k.multiply(n))).mult(PI).div(n).tan();
                    }

                }
            }
        }

        //tan(m*pi/n) = (Tangenstabelle)
        if (expr.getType().equals(TypeFunction.tan) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().isProduct() && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getRight().equals(PI)) {

                    BigInteger m = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft()).getValue().toBigInteger();
                    BigInteger n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

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

                }
            }
        }

        //tan(pi/n) = (Tangenstabelle)
        if (expr.getType().equals(TypeFunction.tan) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().equals(PI) && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {

                BigInteger n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

                //tan(pi/6) = 1/3^(1/2)
                if (n.compareTo(BigInteger.valueOf(6)) == 0) {
                    return ONE.div(THREE.pow(1, 2));
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
                    throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyTrigonometry_TAN_PI_DIVIDED_BY_TWO_NOT_DEFINED"));
                }

            }
        }

        return expr;

    }

    public static Expression reduceCotangent(Function expr) throws EvaluationException {

        //cot(0) = FEHLER!
        if (expr.getType().equals(TypeFunction.cot) && expr.getLeft().equals(ZERO)) {
            throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyTrigonometry_COT_ZERO_NOT_DEFINED"));
        }

        //cot(pi) = FEHLER!
        if (expr.getType().equals(TypeFunction.cot) && expr.getLeft().equals(PI)) {
            throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyTrigonometry_COT_PI_NOT_DEFINED"));
        }

        //cot(k*pi) = FEHLER!
        if (expr.getType().equals(TypeFunction.cot) && expr.getLeft().isProduct()
                && ((BinaryOperation) expr.getLeft()).getLeft().isIntegerConstant() && ((BinaryOperation) expr.getLeft()).getRight().equals(PI)) {
            throw new EvaluationException(expr.writeExpression()
                    + Translator.translateExceptionMessage("SM_SimplifyTrigonometry_COT_MULTIPLE_OF_PI_NOT_DEFINED"));
        }

        //cot(m*pi/n) = cot((m - k*n)*pi/n) mit k = rounddown(m/n)
        if (expr.getType().equals(TypeFunction.cot) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().isProduct() && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getRight().equals(PI)) {

                    // m, n sind bereits ganzzahlig!
                    BigDecimal m = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft()).getValue();
                    BigDecimal n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue();
                    BigDecimal k = m.divide(n, 0, BigDecimal.ROUND_DOWN);
                    if (k.compareTo(BigDecimal.ZERO) != 0) {
                        return new Constant(m.subtract(k.multiply(n))).mult(PI).div(n).cot();
                    }

                }
            }
        }

        //cot(m*pi/n) = (Kotangenstabelle)
        if (expr.getType().equals(TypeFunction.cot) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().isProduct() && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getRight().equals(PI)) {

                    BigInteger m = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft()).getValue().toBigInteger();
                    BigInteger n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

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

                }
            }
        }

        //cot(pi/n) = (Kotangenstabelle)
        if (expr.getType().equals(TypeFunction.cot) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().equals(PI) && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {

                BigInteger n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

                //cot(pi/6) = 3^(1/2)
                if (n.compareTo(BigInteger.valueOf(6)) == 0) {
                    return THREE.pow(1, 2);
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

            }
        }

        return expr;

    }

    public static Expression reduceCosecans(Function expr) throws EvaluationException {

        //cosec(0) = FEHLER!
        if (expr.getType().equals(TypeFunction.cosec) && expr.getLeft().equals(ZERO)) {
            throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyTrigonometry_COSEC_ZERO_NOT_DEFINED"));
        }

        //cosec(pi) = FEHLER!
        if (expr.getType().equals(TypeFunction.cosec) && expr.getLeft().equals(PI)) {
            throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyTrigonometry_COSEC_PI_NOT_DEFINED"));
        }

        //cosec(k*pi) = FEHLER!
        if (expr.getType().equals(TypeFunction.cosec) && expr.getLeft().isProduct()
                && ((BinaryOperation) expr.getLeft()).getLeft().isIntegerConstant() && ((BinaryOperation) expr.getLeft()).getRight().equals(PI)) {
            throw new EvaluationException(expr.writeExpression()
                    + Translator.translateExceptionMessage("SM_SimplifyTrigonometry_COSEC_MULITPLE_OF_PI_NOT_DEFINED"));
        }

        //cosec(m*pi/n) = cosec((m - 2k*n)*pi/n) mit k = rounddown(m/(2n))
        if (expr.getType().equals(TypeFunction.cosec) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().isProduct() && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getRight().equals(PI)) {

                    // m, n sind bereits ganzzahlig!
                    BigDecimal m = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft()).getValue();
                    BigDecimal n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue();
                    BigInteger multiple = m.divide(n, 0, BigDecimal.ROUND_DOWN).toBigInteger();
                    if (m.multiply(n).compareTo(BigDecimal.ZERO) < 0) {
                        multiple = multiple.subtract(BigInteger.ONE);
                    }

                    if (multiple.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                        BigDecimal k = m.divide(n.multiply(BigDecimal.valueOf(2)), 0, BigDecimal.ROUND_DOWN);
                        if (k.compareTo(BigDecimal.ZERO) != 0) {
                            return new Constant(m.subtract(k.multiply(BigDecimal.valueOf(2).multiply(n)))).mult(PI).div(n).cosec();
                        }
                    } else {
                        return MINUS_ONE.mult(new Constant(m.subtract(new BigDecimal(multiple).multiply(n))).mult(PI).div(n).cosec());
                    }

                }
            }
        }

        //cosec(m*pi/n) = (Kosecanstabelle)
        if (expr.getType().equals(TypeFunction.cosec) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().isProduct() && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getRight().equals(PI)) {

                    BigInteger m = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft()).getValue().toBigInteger();
                    BigInteger n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

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

                }
            }
        }

        //cosec(pi/n) = (Kosecanstabelle)
        if (expr.getType().equals(TypeFunction.cos) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().equals(PI) && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {

                BigInteger n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

                //cosec(pi/6) = 2
                if (n.compareTo(BigInteger.valueOf(6)) == 0) {
                    return TWO;
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

            }
        }

        return expr;

    }

    public static Expression reduceSecans(Function expr) throws EvaluationException {

        //sec(0) = 1
        if (expr.getType().equals(TypeFunction.sec) && expr.getLeft().equals(ZERO)) {
            return ONE;
        }

        //sec(pi) = -1
        if (expr.getType().equals(TypeFunction.sec) && expr.getLeft().equals(PI)) {
            return MINUS_ONE;
        }

        //sec(k*pi) = (-1)^k
        if (expr.getType().equals(TypeFunction.sec) && expr.getLeft().isProduct()
                && ((BinaryOperation) expr.getLeft()).getLeft().isIntegerConstant() && ((BinaryOperation) expr.getLeft()).getRight().equals(PI)) {
            BigInteger k = ((Constant) ((BinaryOperation) expr.getLeft()).getLeft()).getValue().toBigInteger();
            if (k.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                return ONE;
            }
            return MINUS_ONE;
        }

        //sec(m*pi/n) = sec((m - 2k*n)*pi/n) mit k = rounddown(m/(2n))
        if (expr.getType().equals(TypeFunction.sec) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().isProduct() && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getRight().equals(PI)) {

                    // m, n sind bereits ganzzahlig!
                    BigDecimal m = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft()).getValue();
                    BigDecimal n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue();
                    BigInteger multiple = m.divide(n, 0, BigDecimal.ROUND_DOWN).toBigInteger();
                    if (m.multiply(n).compareTo(BigDecimal.ZERO) < 0) {
                        multiple = multiple.subtract(BigInteger.ONE);
                    }

                    if (multiple.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0) {
                        BigDecimal k = m.divide(n.multiply(BigDecimal.valueOf(2)), 0, BigDecimal.ROUND_DOWN);
                        if (k.compareTo(BigDecimal.ZERO) != 0) {
                            return new Constant(m.subtract(k.multiply(BigDecimal.valueOf(2).multiply(n)))).mult(
                                    PI).div(n).sec();
                        }
                    } else {
                        return MINUS_ONE.mult(new Constant(m.subtract(new BigDecimal(multiple).multiply(n))).mult(
                                PI).div(n).sec());
                    }

                }
            }
        }

        //sec(m*pi/n) = (Secanstabelle)
        if (expr.getType().equals(TypeFunction.sec) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().isProduct() && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {
                if (((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft().isIntegerConstant()
                        && ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getRight().equals(PI)) {

                    BigInteger m = ((Constant) ((BinaryOperation) ((BinaryOperation) expr.getLeft()).getLeft()).getLeft()).getValue().toBigInteger();
                    BigInteger n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

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

                }
            }
        }

        //sec(pi/n) = (Secanstabelle)
        if (expr.getType().equals(TypeFunction.sec) && expr.getLeft().isQuotient()) {
            if (((BinaryOperation) expr.getLeft()).getLeft().equals(PI) && ((BinaryOperation) expr.getLeft()).getRight().isIntegerConstant()) {

                BigInteger n = ((Constant) ((BinaryOperation) expr.getLeft()).getRight()).getValue().toBigInteger();

                //sec(pi/6) = 2/3^(1/2)
                if (n.compareTo(BigInteger.valueOf(6)) == 0) {
                    return TWO.div(THREE.pow(1, 2));
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
                    throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyTrigonometry_SEC_PI_DIVIDED_BY_TWO_NOT_DEFINED"));
                }

            }
        }

        return expr;

    }

    public static Expression reduceArcsine(Function expr) {

        if (expr.getType().equals(TypeFunction.arcsin) && expr.getLeft().isConstant()) {

            // arcsin(0) = 0
            if (expr.getLeft().equals(ZERO)) {
                return ZERO;
            }
            // arcsin(1) = pi/2
            if (expr.getLeft().equals(ONE)) {
                return PI.div(2);
            }

            // Arcsin-Tabelle
            //arcsin(1/2) = pi/6
            if (expr.getLeft().equivalent(ONE.div(TWO))) {
                return PI.div(6);
            }
            //arcsin(1/2^(1/2)) = pi/4
            if (expr.getLeft().equivalent(ONE.div(TWO.pow(1, 2)))) {
                return PI.div(4);
            }
            //arcsin(3^(1/2)/2) = pi/3
            if (expr.getLeft().equivalent(THREE.pow(1, 2).div(TWO))) {
                return PI.div(3);
            }

        }

        return expr;

    }

    public static Expression reduceArccosine(Function expr) {

        if (expr.getType().equals(TypeFunction.arccos) && expr.getLeft().isConstant()) {

            // arccos(0) = pi/2
            if (expr.getLeft().equals(ZERO)) {
                return PI.div(2);
            }
            // arccos(1) = 0
            if (expr.getLeft().equals(ONE)) {
                return ZERO;
            }
            // arccos(-1) = pi
            if (expr.getLeft().equals(MINUS_ONE)) {
                return PI;
            }

            // Arccos-Tabelle
            //arccos(-1/2) = 2*pi/3
            if (expr.getLeft().equivalent(MINUS_ONE.div(TWO))) {
                return TWO.mult(PI).div(3);
            }
            //arccos(-1/2^(1/2)) = 3*pi/4
            if (expr.getLeft().equivalent(MINUS_ONE.div(TWO.pow(1, 2)))) {
                return THREE.mult(PI).div(4);
            }
            //arccos(-3^(1/2)/2) = 5*pi/6
            if (expr.getLeft().equivalent((MINUS_ONE.mult(THREE.pow(1, 2))).div(TWO))) {
                return (new Constant(5)).mult(PI).div(6);
            }
            //arccos(1/2) = pi/3
            if (expr.getLeft().equivalent(ONE.div(TWO))) {
                return PI.div(3);
            }
            //arccos(1/2^(1/2)) = pi/4
            if (expr.getLeft().equivalent(ONE.div(TWO.pow(1, 2)))) {
                return PI.div(4);
            }
            //arccos(3^(1/2)/2) = pi/6
            if (expr.getLeft().equivalent(THREE.pow(1, 2).div(2))) {
                return PI.div(6);
            }

        }

        return expr;

    }

    public static Expression reduceArctangent(Function expr) {

        if (expr.getType().equals(TypeFunction.arctan) && expr.getLeft().isConstant()) {

            // arctan(0) = 0
            if (expr.getLeft().equals(ZERO)) {
                return ZERO;
            }
            // arctan(1) = pi/4
            if (expr.getLeft().equals(ONE)) {
                return PI.div(4);
            }

            // Arctan-Tabelle
            //arctan(1/3^(1/2)) = pi/6
            if (expr.getLeft().equivalent(ONE.div(THREE.pow(1, 2)))) {
                return PI.div(6);
            }
            //arctan(3^(1/2)) = pi/3
            if (expr.getLeft().equivalent(THREE.pow(1, 2))) {
                return PI.div(3);
            }

        }

        return expr;

    }

    public static Expression reduceArccotangent(Function expr) {

        if (expr.getType().equals(TypeFunction.arccot) && expr.getLeft().isConstant()) {

            // arccot(0) = pi/2
            if (expr.getLeft().equals(ZERO)) {
                return PI.div(2);
            }
            // arccot(1) = pi/4
            if (expr.getLeft().equals(ONE)) {
                return PI.div(4);
            }

            // Arccot-Tabelle
            // arccot(1/3^(1/2)) = pi/3
            if (expr.getLeft().equivalent(ONE.div(THREE.pow(1, 2)))) {
                return PI.div(3);
            }
            // arccot(3^(1/2)) = pi/6
            if (expr.getLeft().equivalent(THREE.pow(1, 2))) {
                return PI.div(6);
            }

        }

        return expr;

    }

    public static Expression reduceArccosecans(Function expr) throws EvaluationException {

        if (expr.getType().equals(TypeFunction.arccosec) && expr.getLeft().isConstant()) {

            // arccosec(0) = 0
            if (expr.getLeft().equals(ZERO)) {
                throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyTrigonometry_ARCCOSEC_ZERO_NOT_DEFINED"));
            }
            // arccosec(1) = pi/2
            if (expr.getLeft().equals(ONE)) {
                return PI.div(2);
            }

            // Arccosec-Tabelle
            //arccosec(2) = pi/6
            if (expr.getLeft().equivalent(TWO)) {
                return PI.div(6);
            }
            //arccosec(2^(1/2)) = pi/4
            if (expr.getLeft().equivalent(TWO.pow(1, 2))) {
                return PI.div(4);
            }
            //arccosec(2/3^(1/2)) = pi/3
            if (expr.getLeft().equivalent(TWO.div(THREE.pow(1, 2)))) {
                return PI.div(3);
            }

        }

        return expr;

    }

    public static Expression reduceArcsecans(Function expr) throws EvaluationException {

        if (expr.getType().equals(TypeFunction.arcsec) && expr.getLeft().isConstant()) {

            // arcsec(0) = FEHLER!
            if (expr.getLeft().equals(ZERO)) {
                throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyTrigonometry_ARCSEC_ZERO_NOT_DEFINED"));
            }
            // arcsec(1) = 0
            if (expr.getLeft().equals(ONE)) {
                return ZERO;
            }
            // arcsec(-1) = pi
            if (expr.getLeft().equals(MINUS_ONE)) {
                return PI;
            }

            // Arcsec-Tabelle
            //arcsec(-2) = 2*pi/3
            if (expr.getLeft().equivalent(new Constant(-2))) {
                return TWO.mult(PI).div(3);
            }
            //arcsec(-2^(1/2)) = 3*pi/4
            if (expr.getLeft().equivalent(MINUS_ONE.mult(TWO.pow(1, 2)))) {
                return THREE.mult(PI).div(4);
            }
            //arcsec(-2/3^(1/2)) = 5*pi/6
            if (expr.getLeft().equivalent(new Constant(-2).div(THREE.pow(1, 2)))) {
                return (new Constant(5)).mult(PI).div(6);
            }
            //arcsec(2) = pi/3
            if (expr.getLeft().equivalent(TWO)) {
                return PI.div(3);
            }
            //arcsec(2^(1/2)) = pi/4
            if (expr.getLeft().equivalent(TWO.pow(1, 2))) {
                return PI.div(4);
            }
            //arcsec(2/3^(1/2)) = pi/6
            if (expr.getLeft().equivalent(TWO.div(THREE.pow(1, 2)))) {
                return PI.div(6);
            }

        }

        return expr;

    }

}
