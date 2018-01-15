package abstractexpressions.expression.basic;

import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisUtils;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisUtils.Monomial;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisUtils.MultiPolynomial;
import enums.TypeSimplify;
import exceptions.EvaluationException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lang.translator.Translator;

public abstract class SimplifyMultiPolynomialUtils {

    private static final Set<TypeSimplify> simplifyTypesExpandPolynomial = getsimplifyTypesExpandPolynomial();

    private static Set<TypeSimplify> getsimplifyTypesExpandPolynomial() {
        Set<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_expand_powerful);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        simplifyTypes.add(TypeSimplify.simplify_algebraic_expressions);
        return simplifyTypes;
    }

    /**
     * Gibt zurück, ob f ein Polynom in der Variablen var ist.<br>
     * VORAUSSETZUNG: f ist vereinfacht, d.h. Operatoren etc. kommen NICHT vor
     * (außer evtl. Gamma(x), was kein Polynom ist).
     */
    public static boolean isMultiPolynomial(Expression f, List<String> vars) {

        boolean exprContainsSomeVariableFromVars = false;
        for (String var : vars) {
            if (f.contains(var)) {
                exprContainsSomeVariableFromVars = true;
                break;
            }
        }
        if (!exprContainsSomeVariableFromVars) {
            return true;
        }

        if (f instanceof Variable && vars.contains(((Variable) f).getName())) {
            return true;
        }
        if (f.isSum() || f.isDifference() || f.isProduct()) {
            return isMultiPolynomial(((BinaryOperation) f).getLeft(), vars) && isMultiPolynomial(((BinaryOperation) f).getRight(), vars);
        }
        if (f.isQuotient()) {

            boolean denominatorContainsSomeVariableFromVars = false;
            for (String var : vars) {
                if (((BinaryOperation) f).getRight().contains(var)) {
                    denominatorContainsSomeVariableFromVars = true;
                    break;
                }
            }
            if (!denominatorContainsSomeVariableFromVars) {
                return isMultiPolynomial(((BinaryOperation) f).getLeft(), vars);
            } else {
                return false;
            }

        }
        if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant() && ((BinaryOperation) f).getRight().isNonNegative()) {
            return isMultiPolynomial(((BinaryOperation) f).getLeft(), vars);
        }
        return false;
    }

    /**
     * Gibt zurück, ob f ein Polynom in der Variablen var ist.<br>
     * VORAUSSETZUNG: f ist vereinfacht, d.h. Operatoren etc. kommen NICHT vor
     * (außer evtl. Gamma(x), was kein Polynom ist).
     */
    private static boolean isMultiPolynomialInExpandedForm(Expression f, List<String> vars) {

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(f);

        for (Expression summand : summandsLeft) {
            if (!isMonomial(summand, vars)) {
                return false;
            }
        }
        for (Expression summand : summandsRight) {
            if (!isMonomial(summand, vars)) {
                return false;
            }
        }

        return true;

    }

    private static boolean isMonomial(Expression f, List<String> vars) {

        if (f.isSum() || f.isDifference()) {
            return false;
        }

        ExpressionCollection factorsNumerator;
        if (f.isQuotient()) {
            factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(f);
        } else {
            factorsNumerator = SimplifyUtilities.getFactors(f);
        }

        for (Expression factor : factorsNumerator) {
            for (String var : vars) {
                if (factor.contains(var)) {
                    if (!factor.equals(Variable.create(var)) && !(factor.isPositiveIntegerPower() && ((BinaryOperation) factor).getLeft().equals(Variable.create(var)))) {
                        return false;
                    }
                }
            }
        }

        if (f.isQuotient()) {
            Expression denominator = ((BinaryOperation) f).getRight();
            for (String var : vars) {
                if (denominator.contains(var)) {
                    return false;
                }
            }
        }

        return true;

    }

    /**
     * Liefert (eine OBERE SCHRANKE für) den Grad des Multipolynoms bzgl. der
     * Variablen vars, welches von f repräsentiert wird. Falls f kein Polynom
     * ist in var ist, so wird -1 (als BigInteger) zurückgegeben.
     */
    public static BigInteger getDegreeOfMultiPolynomial(Expression f, List<String> vars) {

        boolean fContainsVars = false;

        for (String var : vars) {
            fContainsVars = fContainsVars || f.contains(var);
        }
        if (!fContainsVars) {
            return BigInteger.ZERO;
        }
        if (f instanceof Variable) {
            if (vars.contains(((Variable) f).getName())) {
                return BigInteger.ONE;
            }
            return BigInteger.ZERO;
        }
        if (f instanceof BinaryOperation) {
            BigInteger degLeft = getDegreeOfMultiPolynomial(((BinaryOperation) f).getLeft(), vars);
            BigInteger degRight = getDegreeOfMultiPolynomial(((BinaryOperation) f).getRight(), vars);
            if (f.isSum() || f.isDifference()) {
                if (degLeft.equals(BigInteger.valueOf(-1)) || degRight.equals(BigInteger.valueOf(-1))) {
                    return BigInteger.valueOf(-1);
                }
                return degLeft.max(degRight);
            }
            if (f.isProduct()) {
                if (degLeft.equals(BigInteger.valueOf(-1)) || degRight.equals(BigInteger.valueOf(-1))) {
                    return BigInteger.valueOf(-1);
                }
                return degLeft.add(degRight);
            }
            if (f.isQuotient()) {
                for (String var : vars) {
                    if (((BinaryOperation) f).getRight().contains(var)) {
                        // Dann ist f kein Multipolynom.
                        return BigInteger.valueOf(-1);
                    }
                }
                return degLeft;
            }
            if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant() && ((BinaryOperation) f).getRight().isNonNegative()) {
                BigInteger exponent = ((Constant) ((BinaryOperation) f).getRight()).getBigIntValue();
                return degLeft.multiply(exponent);
            }
        }
        if (f instanceof Operator) {
            if (f.isConstant()) {
                return BigInteger.ZERO;
            }
        }

        // Dann ist f kein Mulitpolynom.
        return BigInteger.valueOf(-1);

    }

    /**
     * Liefert (eine OBERE SCHRANKE für) den Grad des Multipolynoms bzgl. der
     * Variablen vars, welches von f repräsentiert wird. Falls f kein Polynom
     * ist in var ist, so wird -1 (als BigInteger) zurückgegeben.
     */
    public static BigInteger getDegreeOfMultiPolynomial(Expression f, String... vars) {
        List<String> varsAsSet = new ArrayList<>();
        varsAsSet.addAll(Arrays.asList(vars));
        return getDegreeOfMultiPolynomial(f, varsAsSet);
    }

    private static Monomial getMonomialFromExpression(Expression f, List<String> vars) throws EvaluationException {

        ExpressionCollection factorsNumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(f);

        // Koeffizienten berechnen.
        Expression coefficient = f;
        for (String var : vars) {
            coefficient = coefficient.replaceVariable(var, ONE);
        }
        coefficient = coefficient.simplify();

        int[] term = new int[vars.size()];

        for (Expression factor : factorsNumerator) {
            for (String var : vars) {
                if (factor.equals(Variable.create(var))) {
                    term[vars.indexOf(var)] = 1;
                } else if (factor.isPositiveIntegerPower() && ((BinaryOperation) factor).getLeft().equals(Variable.create(var))) {
                    /* 
                     Bei der Anwendung dieser Methode wird zuerst geprüft, ob es ein Monom ist.  
                     Dort wird sichergestellt, dass der Exponent sich im Integerbereich aufhält.
                     */
                    term[vars.indexOf(var)] = ((Constant) ((BinaryOperation) factor).getRight()).getValue().intValue();
                }
            }
        }

        return new Monomial(coefficient, term);

    }

    public static MultiPolynomial getMultiPolynomialFromExpression(Expression f, List<String> vars) throws EvaluationException {

        MultiPolynomial fAsMultiPolynomial = new MultiPolynomial();

        if (!isMultiPolynomial(f, vars)) {
            return fAsMultiPolynomial;
        }

        String[] monomialVars = new String[vars.size()];
        GroebnerBasisUtils.setMonomialVars(vars.toArray(monomialVars));

        BigInteger deg = getDegreeOfMultiPolynomial(f, vars);
        if (deg.compareTo(BigInteger.valueOf(computationbounds.ComputationBounds.BOUND_ALGEBRA_MAX_DEGREE_OF_POLYNOMIAL)) > 0) {
            throw new EvaluationException(Translator.translateOutputMessage("SEM_PolynomialRootMethods_TOO_HIGH_DEGREE"));
        }

        f = f.simplify(simplifyTypesExpandPolynomial);
        if (!isMultiPolynomialInExpandedForm(f, vars)) {
            return fAsMultiPolynomial;
        }

        /* 
         Jetzt ist f ausmultipliziert. Einige Monome können allerdings doppelt vorkommen!
         Jetzt muss man die Koeffizienten passend sammeln.
         */
        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(f);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(f);

        for (Expression summand : summandsLeft) {
            fAsMultiPolynomial.addMonomial(getMonomialFromExpression(summand, vars));
        }

        for (Expression summand : summandsRight) {
            fAsMultiPolynomial.addMonomial(getMonomialFromExpression(MINUS_ONE.mult(summand).simplify(), vars));
        }

        fAsMultiPolynomial.clearZeroMonomials();

        return fAsMultiPolynomial;

    }

    public static List<MultiPolynomial> getMultiPolynomialsFromExpressions(List<Expression> exprs, List<String> vars) throws EvaluationException {
        List<MultiPolynomial> polynomials = new ArrayList<>();
        for (Expression expr : exprs) {
            polynomials.add(getMultiPolynomialFromExpression(expr, vars));
        }
        return polynomials;
    }

    public static List<MultiPolynomial> getMultiPolynomialsFromExpressions(Expression[] exprs, List<String> vars) throws EvaluationException {
        List<MultiPolynomial> polynomials = new ArrayList<>();
        for (Expression expr : exprs) {
            polynomials.add(getMultiPolynomialFromExpression(expr, vars));
        }
        return polynomials;
    }

}
