package expressionsimplifymethods;

import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import exceptions.EvaluationException;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.MINUS_ONE;
import static expressionbuilder.Expression.ONE;
import expressionbuilder.Function;
import expressionbuilder.TypeBinary;
import expressionbuilder.TypeFunction;
import translator.Translator;

public abstract class SimplifyFunctionMethods {

    public static Expression approxConstantExpression(Function expr) throws EvaluationException {
        if (expr.getLeft().isConstant() && expr.getLeft().containsApproximates()) {
            return new Constant(expr.evaluate());
        }
        return expr;
    }

    /**
     * Prüft, ob der Funktionswert definiert ist, falls das Argument in der
     * Funktion f konstant ist. Wenn ja, so wird f wieder zurückgegeben,
     * ansonsten wird eine EvaluationException geworfen.
     *
     * @throws EvaluationException
     */
    public static Expression checkIfFunctionValueDefined(Function f) throws EvaluationException {

        if (!f.getLeft().isConstant()) {
            return f;
        }

        Expression left = f.getLeft();
        TypeFunction type = f.getType();

        boolean valueComputable = false;
        try {
            double value = left.evaluate();
            valueComputable = true;

            if (type.equals(TypeFunction.lg) && value < 0) {
                throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyFunctionMethods_UNDEFINED_VALUE"));
            }
            if (type.equals(TypeFunction.ln) && value < 0) {
                throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyFunctionMethods_UNDEFINED_VALUE"));
            }
            if ((type.equals(TypeFunction.arcsin) || type.equals(TypeFunction.arccos)
                    || type.equals(TypeFunction.artanh)) && Math.abs(value) > 1) {
                throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyFunctionMethods_UNDEFINED_VALUE"));
            }
            if (type.equals(TypeFunction.arsech) && ((value > 1) || value < 0)) {
                throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyFunctionMethods_UNDEFINED_VALUE"));
            }
            if ((type.equals(TypeFunction.arcsec) || type.equals(TypeFunction.arccosec)
                    || type.equals(TypeFunction.arcoth)) && Math.abs(value) < 1) {
                throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyFunctionMethods_UNDEFINED_VALUE"));
            }
            if (type.equals(TypeFunction.arcosh) && value < 1) {
                throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyFunctionMethods_UNDEFINED_VALUE"));
            }
            return f;
        } catch (EvaluationException e) {
            if (valueComputable) {
                throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyFunctionMethods_UNDEFINED_VALUE"));
            }
        }

        // Weitere Fälle abgreifen, in denen Funktionswerte definitiv nicht definiert sind.
        if (type.equals(TypeFunction.lg) && left.isNonPositive()) {
            throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyFunctionMethods_UNDEFINED_VALUE"));
        }
        if (type.equals(TypeFunction.ln) && left.isNonPositive()) {
            throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyFunctionMethods_UNDEFINED_VALUE"));
        }
        if ((type.equals(TypeFunction.arcsin) || type.equals(TypeFunction.arccos)
                || type.equals(TypeFunction.artanh)) && (left.isNonNegative()
                || left.isNonPositive())) {
            // Dann muss |left| definitiv >= 10^309, also > 1 sein.
            throw new EvaluationException(Translator.translateExceptionMessage("SM_SimplifyFunctionMethods_UNDEFINED_VALUE"));
        }

        return f;

    }

    /**
     * Vereinfacht die Identität zu ihrem eigenen Argument.
     */
    public static Expression simplifyIdentity(Function f) {
        if (f.getType().equals(TypeFunction.id)) {
            return f.getLeft();
        }
        return f;
    }

    /**
     * Vereinfacht bestimmte Kompositionen zweier Funktionen.
     */
    public static Expression simplifyCompositionOfTwoFunctions(Function f, Expression argumentF) {

        if (argumentF instanceof Function) {

            Function argumentFunction = (Function) argumentF;

            //Doppelten Betrag zu einem Betrag machen
            if (f.getType().equals(TypeFunction.abs) && argumentFunction.getType().equals(TypeFunction.abs)) {
                return argumentFunction;
            }
            if (f.getType().equals(TypeFunction.sin) && argumentFunction.getType().equals(TypeFunction.arcsin)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.cos) && argumentFunction.getType().equals(TypeFunction.arccos)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.tan) && argumentFunction.getType().equals(TypeFunction.arctan)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.cot) && argumentFunction.getType().equals(TypeFunction.arccot)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.sec) && argumentFunction.getType().equals(TypeFunction.arcsec)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.cosec) && argumentFunction.getType().equals(TypeFunction.arccosec)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.sinh) && argumentFunction.getType().equals(TypeFunction.arsinh)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.cosh) && argumentFunction.getType().equals(TypeFunction.arcosh)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.tanh) && argumentFunction.getType().equals(TypeFunction.artanh)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.coth) && argumentFunction.getType().equals(TypeFunction.arcoth)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.sech) && argumentFunction.getType().equals(TypeFunction.arsech)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.cosech) && argumentFunction.getType().equals(TypeFunction.arcosech)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.arsinh) && argumentFunction.getType().equals(TypeFunction.sinh)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.artanh) && argumentFunction.getType().equals(TypeFunction.tanh)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.arcoth) && argumentFunction.getType().equals(TypeFunction.coth)) {
                return argumentFunction.getLeft();
            }
            if (f.getType().equals(TypeFunction.arcosech) && argumentFunction.getType().equals(TypeFunction.cosech)) {
                return argumentFunction.getLeft();
            }

            // Sonstige Identitäten
            //tan(arcsin(x)) = x/(1 - x^2)^(1/2)
            if (f.getType().equals(TypeFunction.tan) && argumentFunction.getType().equals(TypeFunction.arcsin)) {
                return argumentFunction.getLeft().div((Expression.ONE.sub(argumentFunction.getLeft().mult(argumentFunction.getLeft()))).pow(1, 2));
            }
            //tanh(arsinh(x)) = x/(1 + x^2)^(1/2)
            if (f.getType().equals(TypeFunction.tanh) && argumentFunction.getType().equals(TypeFunction.arsinh)) {
                return argumentFunction.getLeft().div((Expression.ONE.add(argumentFunction.getLeft().mult(argumentFunction.getLeft()))).pow(1, 2));
            }
            //tan(arccos(x)) = (1 - x^2)^(1/2)/x
            if (f.getType().equals(TypeFunction.tan) && argumentFunction.getType().equals(TypeFunction.arccos)) {
                return Expression.ONE.sub(argumentFunction.getLeft().mult(argumentFunction.getLeft())).pow(1, 2).div(argumentFunction.getLeft());
            }
            //tanh(arcosh(x)) = (x^2 - 1)^(1/2)/x
            if (f.getType().equals(TypeFunction.tanh) && argumentFunction.getType().equals(TypeFunction.arcosh)) {
                return ((argumentFunction.getLeft().mult(argumentFunction.getLeft()).sub(Expression.ONE))).pow(1, 2).div(argumentFunction.getLeft());
            }
            //tan(arccot(x)) = 1/x (Analoges ist für hyp. Funktionen falsch)
            if (f.getType().equals(TypeFunction.tan) && argumentFunction.getType().equals(TypeFunction.arccot)) {
                return Expression.ONE.div(argumentFunction.getLeft());
            }
            //cot(arcsin(x)) = (1 - x^2)^(1/2)/x
            if (f.getType().equals(TypeFunction.cot) && argumentFunction.getType().equals(TypeFunction.arcsin)) {
                return Expression.ONE.sub(argumentFunction.getLeft().mult(argumentFunction.getLeft())).pow(1, 2).div(argumentFunction.getLeft());
            }
            //coth(arsinh(x)) = (1 + x^2)^(1/2)/x
            if (f.getType().equals(TypeFunction.coth) && argumentFunction.getType().equals(TypeFunction.arsinh)) {
                return Expression.ONE.add(argumentFunction.getLeft().mult(argumentFunction.getLeft())).pow(1, 2).div(argumentFunction.getLeft());
            }
            //cot(arccos(x)) = x/(1 - x^2)^(1/2)
            if (f.getType().equals(TypeFunction.cot) && argumentFunction.getType().equals(TypeFunction.arccos)) {
                return argumentFunction.getLeft().div(Expression.ONE.sub(argumentFunction.getLeft().mult(argumentFunction.getLeft())).pow(1, 2));
            }
            //coth(arcosh(x)) = x/(x^2 - 1)^(1/2)
            if (f.getType().equals(TypeFunction.coth) && argumentFunction.getType().equals(TypeFunction.arcosh)) {
                return argumentFunction.getLeft().div(argumentFunction.getLeft().mult(argumentFunction.getLeft()).sub(
                        Expression.ONE).pow(1, 2));
            }
            //cot(arctan(x)) = 1/x (Analoges ist für hyp. Funktionen falsch)
            if (f.getType().equals(TypeFunction.cot) && argumentFunction.getType().equals(TypeFunction.arctan)) {
                return Expression.ONE.div(argumentFunction.getLeft());
            }
            //sin(arccos(x)) = (1 - x^2)^(1/2)
            if (f.getType().equals(TypeFunction.sin) && argumentFunction.getType().equals(TypeFunction.arccos)) {
                return Expression.ONE.sub(argumentFunction.getLeft().mult(argumentFunction.getLeft())).pow(1, 2);
            }
            //sinh(arcosh(x)) = (x^2 - 1)^(1/2)
            if (f.getType().equals(TypeFunction.sinh) && argumentFunction.getType().equals(TypeFunction.arcosh)) {
                return argumentFunction.getLeft().mult(argumentFunction.getLeft()).sub(Expression.ONE).pow(1, 2);
            }
            //sin(arctan(x)) = x/(1 + x^2)^(1/2)
            if (f.getType().equals(TypeFunction.sin) && argumentFunction.getType().equals(TypeFunction.arctan)) {
                return argumentFunction.getLeft().div(Expression.ONE.add(argumentFunction.getLeft().mult(argumentFunction.getLeft())).pow(1, 2));
            }
            //sinh(artanh(x)) = x/(1 - x^2)^(1/2)
            if (f.getType().equals(TypeFunction.sinh) && argumentFunction.getType().equals(TypeFunction.artanh)) {
                return argumentFunction.getLeft().div(Expression.ONE.sub(argumentFunction.getLeft().mult(argumentFunction.getLeft())).pow(1, 2));
            }
            //sin(arccot(x)) = 1/(x*(1 + 1/x^2)^(1/2))
            if (f.getType().equals(TypeFunction.sin) && argumentFunction.getType().equals(TypeFunction.arccot)) {
                return Expression.ONE.div(argumentFunction.getLeft().mult((Expression.ONE.add(Expression.ONE.div(argumentFunction.getLeft().mult(argumentFunction.getLeft())))).pow(1, 2)));
            }
            //sinh(arcoth(x)) = 1/(x*(1 - 1/x^2)^(1/2))
            if (f.getType().equals(TypeFunction.sinh) && argumentFunction.getType().equals(TypeFunction.arcoth)) {
                return Expression.ONE.div(argumentFunction.getLeft().mult((Expression.ONE.sub(Expression.ONE.div(argumentFunction.getLeft().mult(argumentFunction.getLeft())))).pow(1, 2)));
            }
            //cos(arcsin(x)) = (1 - x^2)^(1/2)
            if (f.getType().equals(TypeFunction.cos) && argumentFunction.getType().equals(TypeFunction.arcsin)) {
                return Expression.ONE.sub(argumentFunction.getLeft().mult(argumentFunction.getLeft())).pow(1, 2);
            }
            //cosh(arsinh(x)) = (1 + x^2)^(1/2)
            if (f.getType().equals(TypeFunction.cosh) && argumentFunction.getType().equals(TypeFunction.arsinh)) {
                return Expression.ONE.add(argumentFunction.getLeft().mult(argumentFunction.getLeft())).pow(1, 2);
            }
            //cos(arctan(x)) = 1/(1 + x^2)^(1/2)
            if (f.getType().equals(TypeFunction.cos) && argumentFunction.getType().equals(TypeFunction.arctan)) {
                return Expression.ONE.div(Expression.ONE.add(argumentFunction.getLeft().mult(argumentFunction.getLeft())).pow(1, 2));
            }
            //cosh(artanh(x)) = 1/(1 - x^2)^(1/2)
            if (f.getType().equals(TypeFunction.cosh) && argumentFunction.getType().equals(TypeFunction.artanh)) {
                return Expression.ONE.div(Expression.ONE.sub(argumentFunction.getLeft().mult(argumentFunction.getLeft())).pow(1, 2));
            }
            //cos(arccot(x)) = 1/(1 + 1/x^2)^(1/2)
            if (f.getType().equals(TypeFunction.cos) && argumentFunction.getType().equals(TypeFunction.arccot)) {
                return Expression.ONE.div((Expression.ONE.add(Expression.ONE.div(argumentFunction.getLeft().mult(argumentFunction.getLeft())))).pow(1, 2));
            }
            //cosh(arcoth(x)) = 1/(1 - 1/x^2)^(1/2)
            if (f.getType().equals(TypeFunction.cosh) && argumentFunction.getType().equals(TypeFunction.arcoth)) {
                return Expression.ONE.div((Expression.ONE.sub(Expression.ONE.div(argumentFunction.getLeft().mult(argumentFunction.getLeft())))).pow(1, 2));
            }
            //exp(arsinh(x)) = x + (x^2 - 1)^(1/2)
            if (f.getType().equals(TypeFunction.exp) && argumentFunction.getType().equals(TypeFunction.arsinh)) {
                return argumentFunction.getLeft().add(argumentFunction.getLeft().mult(argumentFunction.getLeft()).sub(Expression.ONE).pow(1, 2));
            }
            //exp(arcosh(x)) = x + (1 + x^2)^(1/2)
            if (f.getType().equals(TypeFunction.exp) && argumentFunction.getType().equals(TypeFunction.arcosh)) {
                return argumentFunction.getLeft().add(Expression.ONE.add(argumentFunction.getLeft().mult(argumentFunction.getLeft())).pow(1, 2));
            }
            //exp(artanh(x)) = ((1 + x)/(1 - x))^(1/2)
            if (f.getType().equals(TypeFunction.exp) && argumentFunction.getType().equals(TypeFunction.artanh)) {
                return Expression.ONE.add(argumentFunction.getLeft()).div(Expression.ONE.sub(argumentFunction.getLeft())).pow(1, 2);
            }
            //exp(arcosh(x)) = ((1 + x)/(x - 1))^(1/2)
            if (f.getType().equals(TypeFunction.exp) && argumentFunction.getType().equals(TypeFunction.arcoth)) {
                return Expression.ONE.add(argumentFunction.getLeft()).div(argumentFunction.getLeft().sub(Expression.ONE)).pow(1, 2);
            }

        }

        return f;

    }

    /**
     * Versucht den Betrag von Konstanten Ausdrücken zu bestimmen, falls
     * möglich.
     */
    public static Expression computeAbsIfExpressionIsConstant(Function f) {

        if (!f.getType().equals(TypeFunction.abs) || !f.isConstant()) {
            return f;
        }
        if (f.getLeft().isNonNegative()) {
            return f.getLeft();
        }
        if (f.getLeft().isNonPositive()) {
            return Expression.MINUS_ONE.mult(f.getLeft());
        }
        return f;

    }

    /**
     * Falls f der Betrag ist und das Argument von f stets nichtnegativ ist
     * (etwa x^2 oder exp(x) etc.), so wird der Betrag weggelassen.
     */
    public static Expression computeAbsIfExpressionIsAlwaysNonNegative(Function f) {
        if (f.getType().equals(TypeFunction.abs) && f.getLeft().isAlwaysNonNegative()) {
            return f.getLeft();
        }
        return f;
    }

    /**
     * Falls f der Betrag ist und das Argument von f stets nichtnegativ ist
     * (etwa x^2 oder exp(x) etc.), so wird der Betrag weggelassen.
     */
    public static Expression computeAbsIfExpressionIsAlwaysNonPositive(Function f) {
        if (f.getType().equals(TypeFunction.abs) && f.getLeft().isAlwaysNonPositive()) {
            return MINUS_ONE.mult(f.getLeft());
        }
        return f;
    }
    
    /**
     * Versucht das Signum von Konstanten Ausdrücken zu bestimmen, falls
     * möglich.
     */
    public static Expression computeSgnIfExpressionIsConstant(Function f) {

        if (!f.getType().equals(TypeFunction.sgn) || !f.isConstant()) {
            return f;
        }
        if (f.getLeft().equals(Expression.ZERO)) {
            return Expression.ZERO;
        }
        if (f.getLeft().isNonNegative()) {
            return Expression.ONE;
        }
        if (f.getLeft().isNonPositive()) {
            return Expression.MINUS_ONE;
        }
        return f;

    }

    /**
     * Falls f das Signum ist und das Argument von f stets positiv ist
     * (etwa 1+x^2 oder exp(x) etc.), so wird 1 zurückgegeben.
     */
    public static Expression computeSgnIfExpressionIsAlwaysPositive(Function f) {
        if (f.getType().equals(TypeFunction.sgn) && f.getLeft().isAlwaysPositive()) {
            return ONE;
        }
        return f;
    }

    /**
     * Falls f das Signum ist und das Argument von f stets negativ ist
     * (etwa -1-x^2 oder -exp(x) etc.), so wird -1 zurückgegeben.
     */
    public static Expression computeSgnIfExpressionIsAlwaysNegative(Function f) {
        if (f.getType().equals(TypeFunction.abs) && f.getLeft().isAlwaysNegative()) {
            return MINUS_ONE;
        }
        return f;
    }
    
    /**
     * Anwendung der Funktionalgleichung f(-x) = -f(x) bzw. f(-x) = f(x) für
     * bestimmte Funktionen f. Beispielsweise wird cos(-x) zu cos(x)
     * vereinfacht, oder sinh(-x) zu -sinh(x) etc.
     */
    public static Expression simplifySymetricAndAntisymmetricFunctions(Function f) {

        if (!f.getLeft().hasPositiveSign()) {
            // Alle Funktionen mit f(-x) = -f(x).
            if (f.getType().equals(TypeFunction.sin) || f.getType().equals(TypeFunction.sinh)
                    || f.getType().equals(TypeFunction.tan) || f.getType().equals(TypeFunction.tanh)
                    || f.getType().equals(TypeFunction.cot) || f.getType().equals(TypeFunction.coth)
                    || f.getType().equals(TypeFunction.cosec) || f.getType().equals(TypeFunction.cosech)
                    || f.getType().equals(TypeFunction.arcsin) || f.getType().equals(TypeFunction.arsinh)
                    || f.getType().equals(TypeFunction.arccosec) || f.getType().equals(TypeFunction.arcosech)
                    || f.getType().equals(TypeFunction.arctan) || f.getType().equals(TypeFunction.artanh)
                    || f.getType().equals(TypeFunction.arccot) || f.getType().equals(TypeFunction.arcoth)) {

                return Expression.MINUS_ONE.mult(new Function(Expression.MINUS_ONE.mult(f.getLeft()), f.getType()));

            }
            // Alle Funktionen mit f(-x) = f(x).
            if (f.getType().equals(TypeFunction.cos) || f.getType().equals(TypeFunction.cosh)
                    || f.getType().equals(TypeFunction.sec) || f.getType().equals(TypeFunction.sech)
                    || f.getType().equals(TypeFunction.abs)) {

                return new Function(Expression.MINUS_ONE.mult(f.getLeft()), f.getType());

            }
        }

        return f;

    }

    /**
     * Vereinfachung bestimmter Funktionswerte f(0). Beispielsweise wird sin(0)
     * = 0, cos(0) = 1 etc.
     */
    public static Expression simplifySpecialValuesOfFunctions(Function f) throws EvaluationException {

        if (f.getLeft().equals(Expression.ZERO)) {

            // Alle Fälle mit f(0) = 0.
            if (f.getType().equals(TypeFunction.sin) || f.getType().equals(TypeFunction.sinh)
                    || f.getType().equals(TypeFunction.tan) || f.getType().equals(TypeFunction.tanh)
                    || f.getType().equals(TypeFunction.arcsin) || f.getType().equals(TypeFunction.arsinh)) {
                return Expression.ZERO;
            }

            // Alle Fälle mit f(0) = 1.
            if (f.getType().equals(TypeFunction.cos) || f.getType().equals(TypeFunction.cosh)
                    || f.getType().equals(TypeFunction.sec) || f.getType().equals(TypeFunction.sech)
                    || f.getType().equals(TypeFunction.exp)) {
                return Expression.ONE;
            }
            // Alle Fälle, wo f(0) nicht definiert ist.
            if (f.getType().equals(TypeFunction.arsech)
                    || f.getType().equals(TypeFunction.arcosech)
                    || f.getType().equals(TypeFunction.cot)
                    || f.getType().equals(TypeFunction.coth)
                    || f.getType().equals(TypeFunction.cosec)
                    || f.getType().equals(TypeFunction.cosech)
                    || f.getType().equals(TypeFunction.lg)
                    || f.getType().equals(TypeFunction.ln)) {
                throw new EvaluationException(f.writeExpression()
                        + Translator.translateExceptionMessage("SM_SimplifyFunctionMethods_NOT_DEFINED"));
            }

        }

        // ln(1) = 0, lg(1) = 0 und arcosh(1) = 0
        if (f.getLeft().equals(Expression.ONE) && (f.getType().equals(TypeFunction.lg)
                || f.getType().equals(TypeFunction.ln) || f.getType().equals(TypeFunction.arcosh))) {
            return Expression.ZERO;
        }

        return f;

    }

    /**
     * sgn(x)^a für rationales a zu = sgn(x) bzw. = sgn(x)^2 vereinfachen.
     */ 
    public static Expression powerOfSgn(BinaryOperation expr) {

        if (expr.getType().equals(TypeBinary.POW) && expr.getLeft() instanceof Function && ((Function) expr.getLeft()).getType().equals(TypeFunction.sgn)) {
            Expression exponent = expr.getRight();
            if (exponent.isEvenIntegerConstant()) {
                return expr.getLeft().pow(2);
            }
            if (exponent.isOddIntegerConstant()) {
                return expr.getLeft();
            }
            if (exponent.isRationalConstant()) {
                if (((BinaryOperation) exponent).getLeft().isEvenIntegerConstant() && ((BinaryOperation) exponent).getRight().isOddIntegerConstant()) {
                    return expr.getLeft().pow(2);
                }
                if (((BinaryOperation) exponent).getLeft().isOddIntegerConstant() && ((BinaryOperation) exponent).getRight().isOddIntegerConstant()) {
                    return expr.getLeft();
                }
            }
        }

        return expr;

    }

}
