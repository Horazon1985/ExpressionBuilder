package expressionsimplifymethods;

import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.Operator;
import expressionbuilder.Variable;
import java.math.BigInteger;

public class SimplifyPolynomialMethods {

    /**
     * Gibt zurück, ob expr ein Polynom in derivative Variablen var ist.
     * Voraussetzung: expr ist vereinfacht, d.h. Operatoren etc. kommen NICHT
     * vor (außer evtl. Gamma(x), was kein Polynom ist).
     */
    public static boolean isPolynomial(Expression expr, String var) {
        if (!expr.contains(var)) {
            return true;
        }
        if (expr instanceof Variable) {
            return true;
        }
        if (expr.isSum() || expr.isDifference() || expr.isProduct()) {
            return isPolynomial(((BinaryOperation) expr).getLeft(), var) && isPolynomial(((BinaryOperation) expr).getRight(), var);
        }
        if (expr.isQuotient() && !((BinaryOperation) expr).getRight().contains(var)) {
            return isPolynomial(((BinaryOperation) expr).getLeft(), var);
        }
        if (expr.isPower() && ((BinaryOperation) expr).getRight().isIntegerConstant() && ((BinaryOperation) expr).getRight().isNonNegative()) {
            return isPolynomial(((BinaryOperation) expr).getLeft(), var);
        }
        return false;
    }

    /**
     * Liefert (eine OBERE SCHRANKE für) den Grad des Polynoms, welches von f
     * repräsentiert wird. Falls f kein Polynom ist in var ist, so wird -1 (als
     * BigInteger) zurückgegeben.
     */
    public static BigInteger degreeOfPolynomial(Expression f, String var) {
        if (!f.contains(var)) {
            return BigInteger.ZERO;
        }
        if (f instanceof Variable) {
            if (((Variable) f).getName().equals(var)) {
                return BigInteger.ONE;
            }
            return BigInteger.ZERO;
        }
        if (f instanceof BinaryOperation) {
            if (f.isSum() || f.isDifference()) {
                return degreeOfPolynomial(((BinaryOperation) f).getLeft(), var).max(degreeOfPolynomial(((BinaryOperation) f).getRight(), var));
            }
            if (f.isProduct()) {
                return degreeOfPolynomial(((BinaryOperation) f).getLeft(), var).add(degreeOfPolynomial(((BinaryOperation) f).getRight(), var));
            }
            if (f.isQuotient() && !((BinaryOperation) f).getRight().contains(var)) {
                return degreeOfPolynomial(((BinaryOperation) f).getLeft(), var);
            }
            if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant() && ((BinaryOperation) f).getRight().isNonNegative()) {
                BigInteger exp = ((Constant) ((BinaryOperation) f).getRight()).getValue().toBigInteger();
                return degreeOfPolynomial(((BinaryOperation) f).getLeft(), var).multiply(exp);
            }
            // Dann ist f kein Polynom
            return BigInteger.valueOf(-1);
        }
        if (f instanceof Operator) {
            if (f.isConstant()) {
                return BigInteger.ZERO;
            }
            // Dann ist f kein Polynom
            return BigInteger.valueOf(-1);
        }
        // Sollte eigentlich nie eintreten, da Instanzen von SelfDefinedFunction 
        // stets in Instanzen anderer Klassen vereinfacht werden.
        return BigInteger.ZERO;
    }

    /**
     * Liefert (eine UNTERE SCHRANKE für) die Ordnung des Polynoms, welches von
     * f repräsentiert wird. Falls f kein Polynom ist in var ist, so wird -1
     * (als BigInteger) zurückgegeben.
     */
    public static BigInteger orderOfPolynomial(Expression f, String var) {
        if (!f.contains(var)) {
            return BigInteger.ZERO;
        }
        if (f instanceof Variable) {
            if (((Variable) f).getName().equals(var)) {
                return BigInteger.ONE;
            }
            return BigInteger.ZERO;
        }
        if (f instanceof BinaryOperation) {
            if (f.isSum() || f.isDifference()) {
                return orderOfPolynomial(((BinaryOperation) f).getLeft(), var).min(orderOfPolynomial(((BinaryOperation) f).getRight(), var));
            }
            if (f.isProduct()) {
                if (!((BinaryOperation) f).getLeft().contains(var) && !((BinaryOperation) f).getRight().contains(var)) {
                    return BigInteger.ZERO;
                }
                if (((BinaryOperation) f).getLeft().contains(var) && !((BinaryOperation) f).getRight().contains(var)) {
                    return orderOfPolynomial(((BinaryOperation) f).getLeft(), var);
                }
                if (!((BinaryOperation) f).getLeft().contains(var) && ((BinaryOperation) f).getRight().contains(var)) {
                    return orderOfPolynomial(((BinaryOperation) f).getRight(), var);
                }
                return orderOfPolynomial(((BinaryOperation) f).getLeft(), var).add(orderOfPolynomial(((BinaryOperation) f).getRight(), var));
            }
            if (f.isQuotient() && !((BinaryOperation) f).getRight().contains(var)) {
                return orderOfPolynomial(((BinaryOperation) f).getLeft(), var);
            }
            if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant() && ((BinaryOperation) f).getRight().isNonNegative()) {
                BigInteger exp = ((Constant) ((BinaryOperation) f).getRight()).getValue().toBigInteger();
                return orderOfPolynomial(((BinaryOperation) f).getLeft(), var).multiply(exp);
            }
            // Dann ist f kein Polynom
            return BigInteger.valueOf(-1);
        }
        if (f instanceof Operator) {
            if (f.isConstant()) {
                return BigInteger.ZERO;
            }
            // Dann ist f kein Polynom
            return BigInteger.valueOf(-1);
        }
        /*
         Sollte eigentlich nie eintreten, da Instanzen von SelfDefinedFunction 
         stets in Instanzen anderer Klassen vereinfacht werden.
         */
        return BigInteger.ZERO;
    }

}
