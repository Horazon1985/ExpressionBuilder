package abstractexpressions.expression.utilities;

import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.Variable;
import java.math.BigInteger;

public abstract class SimplifyTrigonometricalRelations {

    /*
     Hier beginnen Methoden und Hilfsmethoden für die Lösung spezieller
     trigonometrischer Gleichungen oder für das Integrieren rationaler Funktionen
     Sinus und Kosinus.
     */
    /**
     * Hilfsmethode. Gibt n! zurück, falls n >= 0 ist, sonst 0.
     */
    private static BigInteger fac(int n) {
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    /**
     * Hilfsmethode. Gibt n!/(k! * (n - k)!) zurück, falls 0 <= k <= n, sonst 0.
     */
    private static BigInteger binCoefficient(int n, int k) {
        if (k < 0 || k > n) {
            return BigInteger.ZERO;
        }
        return fac(n).divide(fac(k).multiply(fac(n - k)));
    }

    /**
     * Sei x = var. Diese Methode gibt sin(n*x) zurück.
     */
    public static Expression getSinOfMultipleArgument(String var, int n) {

        if (n == 0) {
            return Expression.ZERO;
        } else if (n < 0) {
            return Expression.MINUS_ONE.mult(getSinOfMultipleArgument(var, -n));
        }

        // Ab hier ist n > 0.
        Expression sinOfArgument = Variable.create(var).sin();
        Expression cosOfArgument = Variable.create(var).cos();
        Expression result = Expression.ZERO;

        for (int i = 1; i <= n; i++) {
            if (i % 2 == 0) {
                continue;
            }
            if (i == 1) {
                result = new Constant(n).mult(sinOfArgument.mult(cosOfArgument.pow(n - 1)));
            } else if (i % 4 == 1) {
                result = result.add(new Constant(binCoefficient(n, i)).mult(sinOfArgument.pow(i).mult(cosOfArgument.pow(n - i))));
            } else {
                result = result.sub(new Constant(binCoefficient(n, i)).mult(sinOfArgument.pow(i).mult(cosOfArgument.pow(n - i))));
            }
        }

        return result;

    }

    /**
     * Sei x = var. Diese Methode gibt sin(n*x) zurück.
     */
    public static Expression getSinOfMultipleArgument(Expression argument, int n) {

        if (n == 0) {
            return Expression.ZERO;
        } else if (n < 0) {
            return Expression.MINUS_ONE.mult(getSinOfMultipleArgument(argument, -n));
        }

        // Ab hier ist n > 0.
        Expression sinOfArgument = argument.sin();
        Expression cosOfArgument = argument.cos();
        Expression result = Expression.ZERO;

        for (int i = 1; i <= n; i++) {
            if (i % 2 == 0) {
                continue;
            }
            if (i == 1) {
                result = new Constant(n).mult(sinOfArgument.mult(cosOfArgument.pow(n - 1)));
            } else if (i % 4 == 1) {
                result = result.add(new Constant(binCoefficient(n, i)).mult(sinOfArgument.pow(i).mult(cosOfArgument.pow(n - i))));
            } else {
                result = result.sub(new Constant(binCoefficient(n, i)).mult(sinOfArgument.pow(i).mult(cosOfArgument.pow(n - i))));
            }
        }

        return result;

    }

    /**
     * Sei x = var. Diese Methode gibt cos(n*x) zurück.
     */
    public static Expression getCosOfMultipleArgument(String var, int n) {

        if (n == 0) {
            return Expression.ONE;
        } else if (n < 0) {
            return getCosOfMultipleArgument(var, -n);
        }

        // Ab hier ist n > 0.
        Expression sinOfArgument = Variable.create(var).sin();
        Expression cosOfArgument = Variable.create(var).cos();
        Expression result = Expression.ZERO;

        for (int i = 0; i <= n; i++) {
            if (i % 2 == 1) {
                continue;
            }
            if (i == 0) {
                result = cosOfArgument.pow(n);
            } else if (i % 4 == 0) {
                result = result.add(new Constant(binCoefficient(n, i)).mult(sinOfArgument.pow(i).mult(cosOfArgument.pow(n - i))));
            } else {
                result = result.sub(new Constant(binCoefficient(n, i)).mult(sinOfArgument.pow(i).mult(cosOfArgument.pow(n - i))));
            }
        }

        return result;

    }

    /**
     * Sei x = var. Diese Methode gibt cos(n*x) zurück.
     */
    public static Expression getCosOfMultipleArgument(Expression argument, int n) {

        if (n == 0) {
            return Expression.ONE;
        } else if (n < 0) {
            return getCosOfMultipleArgument(argument, -n);
        }

        // Ab hier ist n > 0.
        Expression sinOfArgument = argument.sin();
        Expression cosOfArgument = argument.cos();
        Expression result = Expression.ZERO;

        for (int i = 0; i <= n; i++) {
            if (i % 2 == 1) {
                continue;
            }
            if (i == 0) {
                result = cosOfArgument.pow(n);
            } else if (i % 4 == 0) {
                result = result.add(new Constant(binCoefficient(n, i)).mult(sinOfArgument.pow(i).mult(cosOfArgument.pow(n - i))));
            } else {
                result = result.sub(new Constant(binCoefficient(n, i)).mult(sinOfArgument.pow(i).mult(cosOfArgument.pow(n - i))));
            }
        }

        return result;

    }

    /**
     * Additionstheorem für den Sinus: ist x = var, sinArgument = f(x) + c und
     * ist c von x unabhängig, so wird sin(f(x))*cos(x) + cos(f(x))*sin(c)
     * zurückgegeben.
     */
    public static Expression simplifyAdditionTheoremForSine(Expression sinArgument, String var) {

        ExpressionCollection nonConstantSummandsLeft = SimplifyUtilities.getNonConstantSummandsLeftInExpression(sinArgument, var);
        ExpressionCollection nonConstantSummandsRight = SimplifyUtilities.getNonConstantSummandsRightInExpression(sinArgument, var);
        ExpressionCollection constantSummandsLeft = SimplifyUtilities.getConstantSummandsLeftInExpression(sinArgument, var);
        ExpressionCollection constantSummandsRight = SimplifyUtilities.getConstantSummandsRightInExpression(sinArgument, var);

        Expression nonConstantSummand = SimplifyUtilities.produceDifference(nonConstantSummandsLeft, nonConstantSummandsRight);
        Expression constantSummand = SimplifyUtilities.produceDifference(constantSummandsLeft, constantSummandsRight);

        if (constantSummand.equals(Expression.ZERO)) {
            // Dann gibt es keinen konstanten Summanden im Argument.
            return nonConstantSummand.sin();
        }
        return nonConstantSummand.sin().mult(constantSummand.cos()).add(nonConstantSummand.cos().mult(constantSummand.sin()));

    }

    /**
     * Additionstheorem für den Sinus: sinArgument = a +- b, so wird
     * sin(a)*cos(b) +- cos(a)*sin(b) zurückgegeben.
     */
    public static Expression simplifyAdditionTheoremForSine(Expression sinArgument) {
        if (sinArgument.isSum() || sinArgument.isDifference()) {
            Expression leftSummand = ((BinaryOperation) sinArgument).getLeft();
            Expression rightSummand = ((BinaryOperation) sinArgument).getRight();
            if (sinArgument.isSum()) {
                return leftSummand.sin().mult(rightSummand.cos()).add(leftSummand.cos().mult(rightSummand.sin()));
            }
            return leftSummand.sin().mult(rightSummand.cos()).sub(leftSummand.cos().mult(rightSummand.sin()));
        }
        return sinArgument.sin();
    }

    /**
     * Additionstheorem für den Kosinus: ist x = var, cosArgument = f(x) + c und
     * ist c von x unabhängig, so wird cos(f(x))*cos(x) + sin(f(x))*sin(c)
     * zurückgegeben.
     */
    public static Expression simplifyAdditionTheoremForCosine(Expression cosArgument, String var) {

        ExpressionCollection nonConstantSummandsLeft = SimplifyUtilities.getNonConstantSummandsLeftInExpression(cosArgument, var);
        ExpressionCollection nonConstantSummandsRight = SimplifyUtilities.getNonConstantSummandsRightInExpression(cosArgument, var);
        ExpressionCollection constantSummandsLeft = SimplifyUtilities.getConstantSummandsLeftInExpression(cosArgument, var);
        ExpressionCollection constantSummandsRight = SimplifyUtilities.getConstantSummandsRightInExpression(cosArgument, var);

        Expression nonConstantSummand = SimplifyUtilities.produceDifference(nonConstantSummandsLeft, nonConstantSummandsRight);
        Expression constantSummand = SimplifyUtilities.produceDifference(constantSummandsLeft, constantSummandsRight);

        if (constantSummand.equals(Expression.ZERO)) {
            // Dann gibt es keinen konstanten Summanden im Argument.
            return nonConstantSummand.cos();
        }
        return nonConstantSummand.cos().mult(constantSummand.cos()).sub(nonConstantSummand.sin().mult(constantSummand.sin()));

    }

    /**
     * Additionstheorem für den Sinus: sinArgument = a +- b, so wird
     * cos(a)*cos(b) -+ sin(a)*sin(b) zurückgegeben.
     */
    public static Expression simplifyAdditionTheoremForCosine(Expression sinArgument) {
        if (sinArgument.isSum() || sinArgument.isDifference()) {
            Expression leftSummand = ((BinaryOperation) sinArgument).getLeft();
            Expression rightSummand = ((BinaryOperation) sinArgument).getRight();
            if (sinArgument.isSum()) {
                return leftSummand.cos().mult(rightSummand.cos()).sub(leftSummand.sin().mult(rightSummand.sin()));
            }
            return leftSummand.cos().mult(rightSummand.cos()).add(leftSummand.sin().mult(rightSummand.sin()));
        }
        return sinArgument.cos();
    }

    /**
     * Gibt einen Ausdruck zurück, indem in trigonometrischen Funktionen bzgl.
     * var der von var abhängige Teil von dem von var unabhängigen Teil getrennt
     * wurde.<br>
     * BEISPIEL: Für f = 5+sin(2*x+7) wird 5 + sin(2*x)*cos(7)+cos(2*x)*sin(7)
     * zurückgegeben.
     */
    public static Expression separateConstantPartsInRationalTrigonometricalEquations(Expression f, String var) {
        if (!f.contains(var) || f instanceof Constant || f instanceof Variable) {
            return f;
        }
        if (f instanceof BinaryOperation) {
            return new BinaryOperation(separateConstantPartsInRationalTrigonometricalEquations(((BinaryOperation) f).getLeft(), var),
                    separateConstantPartsInRationalTrigonometricalEquations(((BinaryOperation) f).getRight(), var), ((BinaryOperation) f).getType());
        }
        if (f instanceof Function && f.contains(var)) {
            if (!((Function) f).getType().equals(TypeFunction.cos) && !((Function) f).getType().equals(TypeFunction.sin)) {
                return new Function(separateConstantPartsInRationalTrigonometricalEquations(((Function) f).getLeft(), var), ((Function) f).getType());
            }
            Expression argumentOfTrigonometricalFunction = ((Function) f).getLeft();
            if (((Function) f).getType().equals(TypeFunction.cos)) {
                return simplifyAdditionTheoremForCosine(argumentOfTrigonometricalFunction, var);
            } else {
                return simplifyAdditionTheoremForSine(argumentOfTrigonometricalFunction, var);
            }
        }
        return f;
    }

    /**
     * Gibt einen Ausdruck zurück, indem in trigonometrischen Funktionen die
     * Additionstheoreme angewendet wurden.<br>
     * BEISPIEL: Für f = 5+sin(2*x+7) wird 5 + sin(2*x)*cos(7)+cos(2*x)*sin(7)
     * zurückgegeben.
     */
    public static Expression separateConstantPartsInRationalTrigonometricalEquations(Expression f) {
        if (f instanceof Constant || f instanceof Variable) {
            return f;
        }
        if (f instanceof BinaryOperation) {
            return new BinaryOperation(separateConstantPartsInRationalTrigonometricalEquations(((BinaryOperation) f).getLeft()),
                    separateConstantPartsInRationalTrigonometricalEquations(((BinaryOperation) f).getRight()), ((BinaryOperation) f).getType());
        }
        if (f instanceof Function) {
            if (!((Function) f).getType().equals(TypeFunction.cos) && !((Function) f).getType().equals(TypeFunction.sin)) {
                return new Function(separateConstantPartsInRationalTrigonometricalEquations(((Function) f).getLeft()), ((Function) f).getType());
            }
            Expression argumentOfTrigonometricalFunction = ((Function) f).getLeft();
            if (((Function) f).getType().equals(TypeFunction.cos)) {
                return simplifyAdditionTheoremForCosine(argumentOfTrigonometricalFunction);
            } else {
                return simplifyAdditionTheoremForSine(argumentOfTrigonometricalFunction);
            }
        }
        return f;
    }

}
