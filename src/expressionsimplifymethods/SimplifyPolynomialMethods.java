package expressionsimplifymethods;

import computationbounds.ComputationBounds;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.Operator;
import expressionbuilder.TypeSimplify;
import expressionbuilder.Variable;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;

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
     * Sei x = var. f ist ein Term genau dann wenn f die Form x^n, n natürliche
     * Zahl, besitzt.
     */
    private static boolean isTerm(Expression f, String var) {
        return f.equals(Expression.ONE) || f.equals(Variable.create(var))
                || (f.isPower() && ((BinaryOperation) f).getLeft().equals(Variable.create(var))
                && ((BinaryOperation) f).getRight().isIntegerConstant()
                && ((BinaryOperation) f).getRight().isNonNegative());
    }

    /**
     * Sei x = var. f ist ein Monom genau dann wenn f die Form a*x^n, n
     * natürliche Zahl, besitzt.
     */
    private static boolean isMonom(Expression f, String var) {
        return isTerm(f, var) || !f.contains(var) || (f.isProduct()
                && !((BinaryOperation) f).getLeft().contains(var) && isTerm(((BinaryOperation) f).getRight(), var))
                || (f.isProduct()
                && !((BinaryOperation) f).getRight().contains(var) && isTerm(((BinaryOperation) f).getLeft(), var))
                || (f.isQuotient()
                && isTerm(((BinaryOperation) f).getLeft(), var) && !((BinaryOperation) f).getRight().contains(var));
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
                BigInteger exp = ((Constant) ((BinaryOperation) f).getRight()).getPreciseValue().toBigInteger();
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
                BigInteger exp = ((Constant) ((BinaryOperation) f).getRight()).getPreciseValue().toBigInteger();
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

    /**
     * Gibt zurück, ob f ein Polynom in mehreren Veränderlichen ist.
     */
    private static boolean isMultiPolynomial(Expression f) {

        if (!f.isConstant()) {
            return true;
        }
        if (f instanceof Variable) {
            return true;
        }
        if (f.isSum() || f.isDifference() || f.isProduct()) {
            return isMultiPolynomial(((BinaryOperation) f).getLeft()) && isMultiPolynomial(((BinaryOperation) f).getRight());
        }
        if (f.isQuotient() && !((BinaryOperation) f).getRight().isConstant()) {
            return isMultiPolynomial(((BinaryOperation) f).getLeft());
        }
        if (f.isPower()
                && ((BinaryOperation) f).getRight().isIntegerConstant()
                && ((BinaryOperation) f).getRight().isNonNegative()) {
            return isMultiPolynomial(((BinaryOperation) f).getLeft());
        }
        return false;

    }

    /**
     * Sei x = var. f ist ein Term genau dann wenn f die Form x^n, n natürliche
     * Zahl, besitzt.
     */
    private static boolean isMultiTerm(Expression f) {
        return f.equals(Expression.ONE) || f instanceof Variable
                || (f.isPower() && ((BinaryOperation) f).getLeft() instanceof Variable
                && ((BinaryOperation) f).getRight().isIntegerConstant()
                && ((BinaryOperation) f).getRight().isNonNegative())
                || (f.isProduct() && isMultiTerm(((BinaryOperation) f).getLeft())
                && isMultiTerm(((BinaryOperation) f).getRight()));
    }

    /**
     * Sei x = var. f ist ein Monom genau dann wenn f die Form a*x^n, n
     * natürliche Zahl, besitzt.
     */
    private static boolean isMultiMonom(Expression f) {
        return isMultiTerm(f) || (f.isProduct()
                && ((BinaryOperation) f).getLeft().isConstant() && isMultiTerm(((BinaryOperation) f).getRight()))
                || (f.isProduct()
                && ((BinaryOperation) f).getRight().isConstant() && isMultiTerm(((BinaryOperation) f).getLeft()))
                || (f.isQuotient()
                && isMultiTerm(((BinaryOperation) f).getLeft()) && ((BinaryOperation) f).getRight().isConstant());
    }

    /**
     * Liefert (eine OBERE SCHRANKE für) den Grad des Polynoms in mehreren
     * Veränderlichen, welches von f repräsentiert wird. Falls f kein Polynom
     * ist in var ist, so wird -1 (als BigInteger) zurückgegeben.
     */
    public static BigInteger degreeOfMultiPolynomial(Expression f) {

        if (f.isConstant()) {
            return BigInteger.ZERO;
        }
        if (f instanceof Variable) {
            return BigInteger.ONE;
        }
        if (f instanceof BinaryOperation) {
            if (f.isSum() || f.isDifference()) {
                return degreeOfMultiPolynomial(((BinaryOperation) f).getLeft()).max(degreeOfMultiPolynomial(((BinaryOperation) f).getRight()));
            }
            if (f.isProduct()) {
                return degreeOfMultiPolynomial(((BinaryOperation) f).getLeft()).add(degreeOfMultiPolynomial(((BinaryOperation) f).getRight()));
            }
            if (f.isQuotient() && ((BinaryOperation) f).getRight().isConstant()) {
                return degreeOfMultiPolynomial(((BinaryOperation) f).getLeft());
            }
            if (f.isPower()
                    && ((BinaryOperation) f).getRight().isIntegerConstant()
                    && ((BinaryOperation) f).getRight().isNonNegative()) {
                return degreeOfMultiPolynomial(((BinaryOperation) f).getLeft()).multiply(
                        ((Constant) ((BinaryOperation) f).getRight()).getPreciseValue().toBigInteger());
            }
            // Dann ist f kein Polynom.
            return BigInteger.valueOf(-1);
        }
        if (f instanceof Operator) {
            if (f.isConstant()) {
                return BigInteger.ZERO;
            }
        }
        // Dann ist f kein Polynom.
        return BigInteger.valueOf(-1);

    }

    /**
     * Gibt die "Länge" eines Polynoms f bzgl. var zurück (ein für ein Polynom
     * definiertes Maß, welches in etwa beschreiben soll, wie lang f ist, wenn
     * man es ausschreibt).
     */
    private static int length(Expression f, String var) {

        if (!isPolynomial(f, var)) {
            return -1;
        }
        if (isMonom(f, var)) {
            return 1;
        }
        if (f instanceof BinaryOperation && !f.isPower()) {
            return length(((BinaryOperation) f).getLeft(), var) + length(((BinaryOperation) f).getRight(), var);
        }
        if (f.isPower()) {
            return length(((BinaryOperation) f).getLeft(), var);
        }
        return -1;

    }

    /**
     * Gibt die "Länge" eines Polynoms f in mehreren Veränderlichen bzgl. var
     * zurück (ein für ein Polynom in mehreren Veränderlichen definiertes Maß,
     * welches in etwa beschreiben soll, wie lang f ist, wenn man es
     * ausschreibt).
     */
    private static int length(Expression f) {

        if (!isMultiPolynomial(f)) {
            return -1;
        }
        if (isMultiMonom(f)) {
            return 1;
        }
        if (f instanceof BinaryOperation && !f.isPower()) {
            return length(((BinaryOperation) f).getLeft()) + length(((BinaryOperation) f).getRight());
        }
        if (f.isPower()) {
            return length(((BinaryOperation) f).getLeft());
        }
        return -1;

    }

    public static Expression simplifyPolynomial(Expression f) {

        HashSet<String> vars = new HashSet<>();
        f.getContainedVars(vars);
        if (vars.size() != 1) {
            return f;
        }
        // Ab hier kommt in f genau eine Variable vor.
        Iterator<String> iter = vars.iterator();
        String var = iter.next();
        return simplifyPolynomial(f, var);

    }

    private static Expression simplifyPolynomial(Expression f, String var) {

        if (!isPolynomial(f, var)) {
            return f;
        }

        BigInteger deg = degreeOfPolynomial(f, var);

        if (deg.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0
                || deg.intValue() > ComputationBounds.BOUND_DEGREE_OF_POLYNOMIAL_FOR_SIMPLIFY) {
            return f;
        }

        try {
            Expression polynomial = f.expand();
            /*
             Nun muss man polynomial vereinfachen, aber als Vereinfachungstyp
             darf NICHT simplifyPolynomials() verwendet werden.
             */
            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.simplify_trivial);
            simplifyTypes.add(TypeSimplify.sort_difference_and_division);
            simplifyTypes.add(TypeSimplify.simplify_powers);
            simplifyTypes.add(TypeSimplify.collect_products);
            simplifyTypes.add(TypeSimplify.factorize_rationals_in_sums);
            simplifyTypes.add(TypeSimplify.factorize_rationals_in_differences);
            simplifyTypes.add(TypeSimplify.reduce_quotients);
            simplifyTypes.add(TypeSimplify.reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_functional_relations);
            simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
            simplifyTypes.add(TypeSimplify.order_sums_and_products);

            polynomial = polynomial.simplify(simplifyTypes);

            if (length(polynomial, var) <= length(f, var)) {
                return polynomial;
            }
            return f;
        } catch (EvaluationException e) {
            return f;
        }

    }

    public static Expression simplifyMultiPolynomial(Expression f) {

        if (!isMultiPolynomial(f)) {
            return f;
        }

        BigInteger deg = degreeOfMultiPolynomial(f);

        if (deg.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0
                || deg.intValue() > ComputationBounds.BOUND_DEGREE_OF_MULTIPOLYNOMIAL_FOR_SIMPLIFY) {
            return f;
        }

        try {
            Expression polynomial = f.expand();
            /*
             Nun muss man polynomial vereinfachen, aber als Vereinfachungstyp
             darf NICHT simplifyPolynomials() verwendet werden.
             */
            HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
            simplifyTypes.add(TypeSimplify.simplify_trivial);
            simplifyTypes.add(TypeSimplify.sort_difference_and_division);
            simplifyTypes.add(TypeSimplify.simplify_powers);
            simplifyTypes.add(TypeSimplify.collect_products);
            simplifyTypes.add(TypeSimplify.factorize_rationals_in_sums);
            simplifyTypes.add(TypeSimplify.factorize_rationals_in_differences);
            simplifyTypes.add(TypeSimplify.reduce_quotients);
            simplifyTypes.add(TypeSimplify.reduce_leadings_coefficients);
            simplifyTypes.add(TypeSimplify.simplify_functional_relations);
            simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
            simplifyTypes.add(TypeSimplify.order_sums_and_products);

            polynomial = polynomial.simplify(simplifyTypes);

            if (length(polynomial) <= length(f) - 2) {
                /*
                 Die Schranke "<= length(f) - 1" reicht nicht aus für eine sinnvole 
                 Vereinfachung, denn dan wird sogar a*(b + c) zu a*b + a*c vereinfacht.
                 Sinnvoller ist daher die Schranke length(f) - 2.
                 */
                return polynomial;
            }
            return f;
        } catch (EvaluationException e) {
            return f;
        }

    }

}
