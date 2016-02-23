package abstractexpressions.expression.classes;

import abstractexpressions.expression.computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import enums.TypeExpansion;
import enums.TypeSimplify;
import exceptions.EvaluationException;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyAlgebraicExpressionMethods;
import abstractexpressions.expression.utilities.SimplifyBinaryOperationMethods;
import abstractexpressions.expression.utilities.SimplifyExpLog;
import abstractexpressions.expression.utilities.SimplifyFunctionMethods;
import abstractexpressions.expression.utilities.SimplifyFunctionalRelations;
import abstractexpressions.expression.utilities.SimplifyPolynomialMethods;
import abstractexpressions.expression.utilities.SimplifyUtilities;
import flowcontroller.FlowController;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import lang.translator.Translator;

public class BinaryOperation extends Expression {

    private final Expression left, right;
    private final TypeBinary type;

    private static final HashSet<TypeSimplify> simplifyTypesExpandAndCollectIfShorter = getSimplifyTypesExpandAndCollectIfShorter();

    private static HashSet<TypeSimplify> getSimplifyTypesExpandAndCollectIfShorter() {
        /*
         Als Vereinfachungstyp darf NICHT 
         simplify_expand_and_collect_equivalents_if_shorter verwendet werden.
         */
        HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
        simplifyTypes.add(TypeSimplify.simplify_trivial);
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_expand_rational_factors);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_leadings_coefficients);
        simplifyTypes.add(TypeSimplify.simplify_collect_logarithms);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        return simplifyTypes;
    }
    
    public BinaryOperation(Expression left, Expression right, TypeBinary type) {
        this.left = left;
        this.right = right;
        this.type = type;
    }

    public TypeBinary getType() {
        return this.type;
    }

    public Expression getLeft() {
        return this.left;
    }

    public Expression getRight() {
        return this.right;
    }

    @Override
    public Expression copy() {
        return new BinaryOperation(this.left, this.right, this.type);
    }

    @Override
    public double evaluate() throws EvaluationException {

        double valueLeft = this.left.evaluate();
        double valueRight = this.right.evaluate();

        if (Double.isNaN(valueLeft) || Double.isInfinite(valueLeft) || Double.isNaN(valueRight) || Double.isInfinite(valueRight)) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
        }

        switch (type) {
            case PLUS:
                if (Double.isNaN(valueLeft + valueRight) || Double.isInfinite(valueLeft + valueRight)) {
                    throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
                }
                return valueLeft + valueRight;
            case MINUS:
                if (Double.isNaN(valueLeft - valueRight) || Double.isInfinite(valueLeft - valueRight)) {
                    throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
                }
                return valueLeft - valueRight;
            case TIMES:
                if (Double.isNaN(valueLeft * valueRight) || Double.isInfinite(valueLeft * valueRight)) {
                    throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
                }
                return valueLeft * valueRight;
            case DIV:
                if ((!Double.isNaN(valueLeft / valueRight)) && (!Double.isInfinite(valueLeft / valueRight))) {
                    if (Double.isNaN(valueLeft / valueRight) || Double.isInfinite(valueLeft / valueRight)) {
                        throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
                    }
                    return valueLeft / valueRight;
                } else {
                    throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_DIVISION_BY_ZERO"));
                }
            case POW:
                // Abfangen von Wurzeln ungerader Ordnung aus negativen Zahlen.
                if (valueLeft < 0 && this.right.isRationalConstant() && ((BinaryOperation) this.right).getRight().isOddIntegerConstant()) {
                    double result;
                    if (((BinaryOperation) this.right).getLeft().isOddIntegerConstant()) {
                        result = -Math.pow(-valueLeft, valueRight);
                    } else {
                        result = Math.pow(-valueLeft, valueRight);
                    }
                    if (!Double.isNaN(result) && !Double.isInfinite(result)) {
                        return result;
                    } else {
                        throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
                    }
                }
                // Dann ganz normal weiter.
                if (!Double.isNaN(Math.pow(valueLeft, valueRight)) && !Double.isInfinite(Math.pow(valueLeft, valueRight))) {
                    return Math.pow(valueLeft, valueRight);
                } else {
                    throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_UNDEFINED_VALUE"));
                }
            default:
                return 0;
        }

    }

    @Override
    public void addContainedVars(HashSet<String> vars) {
        this.left.addContainedVars(vars);
        this.right.addContainedVars(vars);
    }

    @Override
    public void addContainedIndeterminates(HashSet<String> vars) {
        this.left.addContainedIndeterminates(vars);
        this.right.addContainedIndeterminates(vars);
    }
    
    @Override
    public boolean contains(String var) {
        return this.left.contains(var) || this.right.contains(var);
    }

    @Override
    public boolean containsApproximates() {
        return (this.left.containsApproximates() || this.right.containsApproximates());
    }

    @Override
    public boolean containsFunction() {
        if (this.type.equals(TypeBinary.POW) && (!this.right.isConstant())) {
            // Im diesem Fall handelt es sich (eventuell) um Exponentialfunktionen.
            return true;
        }
        return this.left.containsFunction() || this.right.containsFunction();
    }

    @Override
    public boolean containsExponentialFunction() {
        if (this.type.equals(TypeBinary.POW) && (!this.right.isConstant())) {
            // Im diesem Fall handelt es sich (eventuell) um Exponentialfunktionen.
            return true;
        }
        return this.left.containsExponentialFunction() || this.right.containsExponentialFunction();
    }

    @Override
    public boolean containsTrigonometricalFunction() {
        return this.left.containsTrigonometricalFunction() || this.right.containsTrigonometricalFunction();
    }

    @Override
    public boolean containsIndefiniteIntegral() {
        return this.left.containsIndefiniteIntegral() || this.right.containsIndefiniteIntegral();
    }

    @Override
    public boolean containsOperator() {
        return this.left.containsOperator() || this.right.containsOperator();
    }

    @Override
    public boolean containsOperator(TypeOperator type) {
        return this.left.containsOperator(type) || this.right.containsOperator(type);
    }

    @Override
    public Expression turnToApproximate() {
        return new BinaryOperation(this.left.turnToApproximate(), this.right.turnToApproximate(), this.type);
    }

    @Override
    public Expression turnToPrecise() {
        return new BinaryOperation(this.left.turnToPrecise(), this.right.turnToPrecise(), this.type);
    }

    @Override
    public Expression replaceVariable(String var, Expression expr) {
        return new BinaryOperation(this.left.replaceVariable(var, expr), this.right.replaceVariable(var, expr), this.type);
    }

    @Override
    public Expression replaceSelfDefinedFunctionsByPredefinedFunctions() {
        return new BinaryOperation(this.left.replaceSelfDefinedFunctionsByPredefinedFunctions(),
                this.right.replaceSelfDefinedFunctionsByPredefinedFunctions(), this.type);
    }

    @Override
    public Expression diff(String var) throws EvaluationException {

        if (!this.contains(var)) {
            return Expression.ZERO;
        }

        if (this.isSum()) {
            return this.left.diff(var).add(this.right.diff(var));
        } else if (this.isDifference()) {
            return this.left.diff(var).sub(this.right.diff(var));
        } else if (this.isProduct()) {
            return this.left.diff(var).mult(this.right).add(this.left.mult(this.right.diff(var)));
        } else if (this.isQuotient()) {
            Expression enumerator = this.left.diff(var).mult(this.right).sub(this.left.mult(this.right.diff(var)));
            Expression denominator = this.right.pow(2);
            return enumerator.div(denominator);
        } else {
            if (!this.right.contains(var)) {
                //Regel: (f^n)' = n*f^(n - 1)*f'
                return this.right.mult(this.left.pow(this.right.sub(1))).mult(this.left.diff(var));
            } else if (!this.left.contains(var)) {
                //Regel: (a^g)' = ln(a)*a^g*g')
                //Fehlerbehandlung: a muss > 0 sein!
                if (this.left.isConstant() && this.left.isNonPositive()) {
                    throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_FUNCTION_NOT_DIFFERENTIABLE"));
                }
                return new Function(this.left, TypeFunction.ln).mult(this).mult(this.right.diff(var));
            } else {
                //Regel: (f^g)' = f^g*(gf'/f + ln(f)*g')
                Expression rightBracket = this.left.diff(var).mult(this.right).div(this.left).add(new Function(this.left, TypeFunction.ln).mult(this.right.diff(var)));
                return this.mult(rightBracket);
            }
        }

    }

    @Override
    public Expression diffDifferentialEquation(String var) throws EvaluationException {

        if (this.isConstant()) {
            return Expression.ZERO;
        }

        if (this.isSum()) {
            return this.left.diffDifferentialEquation(var).add(this.right.diffDifferentialEquation(var));
        } else if (this.isDifference()) {
            return this.left.diffDifferentialEquation(var).sub(this.right.diffDifferentialEquation(var));
        } else if (this.isProduct()) {
            return this.left.diffDifferentialEquation(var).mult(this.right).add(this.left.mult(this.right.diffDifferentialEquation(var)));
        } else if (this.isQuotient()) {
            Expression enumerator = this.left.diffDifferentialEquation(var).mult(this.right).sub(this.left.mult(this.right.diffDifferentialEquation(var)));
            Expression denominator = this.right.pow(2);
            return enumerator.div(denominator);
        } else {
            if (!this.right.contains(var)) {
                //Regel: (f^n)' = n*f^(n - 1)*f'
                return this.right.mult(this.left.pow(this.right.sub(1))).mult(this.left.diffDifferentialEquation(var));
            } else if (!this.left.contains(var)) {
                //Regel: (a^g)' = ln(a)*a^g*g')
                //Fehlerbehandlung: a muss > 0 sein!
                if (this.left.isConstant() && this.left.isNonPositive()) {
                    throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_FUNCTION_NOT_DIFFERENTIABLE"));
                }
                return new Function(this.left, TypeFunction.ln).mult(this).mult(this.right.diffDifferentialEquation(var));
            } else {
                //Regel: (f^g)' = f^g*(gf'/f + ln(f)*g')
                Expression rightBracket = this.left.diffDifferentialEquation(var).mult(this.right).div(this.left).add(
                        new Function(this.left, TypeFunction.ln).mult(this.right.diffDifferentialEquation(var)));
                return this.mult(rightBracket);
            }
        }

    }

    @Override
    public String writeExpression() {

        String leftAsText, rightAsText;

        if (this.isSum()) {
            if (this.right.doesExpressionStartWithAMinusSign()) {
                return this.left.writeExpression() + "+(" + this.right.writeExpression() + ")";
            } else {
                return this.left.writeExpression() + "+" + this.right.writeExpression();
            }
        } else if (this.isDifference()) {

            leftAsText = this.left.writeExpression();

            //0 - a soll als -a ausgegeben werden.
            if (this.left.equals(Expression.ZERO)) {
                leftAsText = "";
            }

            if (this.right.doesExpressionStartWithAMinusSign() || this.right.isSum() || this.right.isDifference()) {
                return leftAsText + "-(" + this.right.writeExpression() + ")";
            }
            return leftAsText + "-" + this.right.writeExpression();

        } else if (this.isProduct()) {

            if (this.left.isSum() || this.left.isDifference()) {

                leftAsText = "(" + this.left.writeExpression() + ")";
                if (this.right.doesExpressionStartWithAMinusSign() || this.right.isSum() || this.right.isDifference()) {
                    rightAsText = "(" + this.right.writeExpression() + ")";
                } else {
                    rightAsText = this.right.writeExpression();
                }

            } else {

                leftAsText = this.left.writeExpression();
                if (this.left instanceof Constant
                        && ((Constant) this.left).getValue().compareTo(BigDecimal.valueOf(-1)) == 0) {
                    // Ausnahmefall: Der Ausdruck fängt mit einem - an.
                    if (this.right.doesExpressionStartWithAMinusSign() || this.right.isSum() || this.right.isDifference()) {
                        return "-(" + this.right.writeExpression() + ")";
                    }
                    return "-" + this.right.writeExpression();
                } else {
                    if (this.right.doesExpressionStartWithAMinusSign() || this.right.isSum() || this.right.isDifference()) {
                        rightAsText = "(" + this.right.writeExpression() + ")";
                    } else {
                        rightAsText = this.right.writeExpression();
                    }
                }

            }

            return leftAsText + "*" + rightAsText;

        } else if (this.isQuotient()) {

            if (this.left.isSum() || this.left.isDifference() || this.left.isProduct()) {
                leftAsText = "(" + this.left.writeExpression() + ")";
            } else {
                leftAsText = this.left.writeExpression();
            }

            if (this.right.doesExpressionStartWithAMinusSign()
                    || (this.right instanceof BinaryOperation && !this.right.isPower())) {
                rightAsText = "(" + this.right.writeExpression() + ")";
            } else {
                rightAsText = this.right.writeExpression();
            }

            return leftAsText + "/" + rightAsText;

        }

        // Hier handelt es sich um eine Potenz.
        if (this.left instanceof BinaryOperation
                || (this.left instanceof Constant && ((Constant) this.left).getValue().compareTo(BigDecimal.ZERO) < 0)) {
            leftAsText = "(" + this.left.writeExpression() + ")";
        } else {
            leftAsText = this.left.writeExpression();
        }

        if (this.right instanceof BinaryOperation
                || (this.right instanceof Constant && ((Constant) this.right).getValue().compareTo(BigDecimal.ZERO) < 0)) {
            rightAsText = "(" + this.right.writeExpression() + ")";
        } else {
            rightAsText = this.right.writeExpression();
        }

        return leftAsText + "^" + rightAsText;

    }

    @Override
    public String expressionToLatex() {

        String leftAsLatexCode, rightAsLatexCode;

        if (this.isSum()) {
            return this.left.expressionToLatex() + "+" + this.right.expressionToLatex();
        } else if (this.isDifference()) {

            leftAsLatexCode = this.left.writeExpression();

            //0 - a soll als -a ausgegeben werden.
            if (this.left.equals(Expression.ZERO)) {
                leftAsLatexCode = "";
            }

            if (this.right.isSum() || this.right.isDifference()) {
                return leftAsLatexCode + "-\\left(" + this.right.expressionToLatex() + "\\right)";
            }
            return leftAsLatexCode + "-" + this.right.expressionToLatex();

        } else if (this.isProduct()) {

            //(-1)*a soll als -a ausgegeben werden.
            if (this.left.equals(Expression.MINUS_ONE)) {
                if (this.right.isSum() || this.right.isDifference()) {
                    // Hier noch zusätzliche Klammern um den rechten Faktor.
                    return "-(" + this.right.expressionToLatex() + ")";
                }
                return "-" + this.right.expressionToLatex();
            }

            if (this.left.isSum() || this.left.isDifference()) {
                leftAsLatexCode = "\\left(" + this.left.expressionToLatex() + "\\right)";
            } else {
                leftAsLatexCode = this.left.expressionToLatex();
            }

            if (this.right.isSum() || this.right.isDifference()) {
                rightAsLatexCode = "\\left(" + this.right.expressionToLatex() + "\\right)";
            } else {
                rightAsLatexCode = this.right.expressionToLatex();
            }

            return leftAsLatexCode + " \\cdot " + rightAsLatexCode;

        } else if (this.isQuotient()) {

            return "\\frac{" + this.left.expressionToLatex() + "}{" + this.right.expressionToLatex() + "}";

        } else {

            if (this.left instanceof BinaryOperation) {
                if (this.left.isDifference() && ((BinaryOperation) this.left).getLeft().equals(Expression.ZERO)) {
                    leftAsLatexCode = this.left.expressionToLatex();
                } else {
                    leftAsLatexCode = "\\left(" + this.left.expressionToLatex() + "\\right)";
                }
            } else {
                leftAsLatexCode = this.left.expressionToLatex();
            }

            if (this.left instanceof Variable) {

                if (this.right instanceof Variable && (this.right.writeExpression().length() == 1)) {
                    return leftAsLatexCode + "^" + this.right.expressionToLatex();
                }
                return leftAsLatexCode + "^{" + this.right.expressionToLatex() + "}";

            } else if (this.left instanceof Constant) {

                if (this.left.isNonNegative()) {
                    if ((this.right instanceof Variable) && (this.right.writeExpression().length() == 1)) {
                        return leftAsLatexCode + "^" + this.right.expressionToLatex();
                    }
                    return leftAsLatexCode + "^{" + this.right.expressionToLatex() + "}";
                }

            } else {

                if ((this.right instanceof Variable) && (this.right.writeExpression().length() == 1)) {
                    return "{" + leftAsLatexCode + "}^" + this.right.expressionToLatex();
                }

            }

            return "{" + leftAsLatexCode + "}^{" + this.right.expressionToLatex() + "}";

        }

    }

    @Override
    public boolean isConstant() {
        return this.left.isConstant() && this.right.isConstant();
    }

    @Override
    public boolean isNonNegative() {

        if (!this.isConstant()) {
            return false;
        }

        try {
            return this.evaluate() >= 0;
        } catch (EvaluationException e) {
        }

        if (this.type.equals(TypeBinary.PLUS)) {
            return this.left.isNonNegative() && this.right.isNonNegative();
        } else if (this.type.equals(TypeBinary.MINUS)) {
            return this.left.isNonNegative() && this.right.isNonPositive();
        } else if (this.type.equals(TypeBinary.TIMES) || this.type.equals(TypeBinary.DIV)) {
            return (this.left.isNonNegative() && this.right.isNonNegative());
        } else {

            // Hier ist type == TypeBinary.POW
            if (this.left.isNonNegative()) {
                return true;
            }
            if (this.right.isEvenIntegerConstant()) {
                return true;
            }
            if (this.right.isRationalConstant() && ((BinaryOperation) this.right).getLeft().isEvenIntegerConstant()) {
                return true;
            }
            if (this.right.isRationalConstant() && ((BinaryOperation) this.right).getLeft().isOddIntegerConstant()
                    && ((BinaryOperation) this.right).getRight().isOddIntegerConstant()) {
                return this.left.isNonNegative();
            }
            return false;

        }

    }

    @Override
    public boolean isAlwaysNonNegative() {

        if (this.isNonNegative()) {
            return true;
        }
        if (this.isSum() || this.isProduct() || this.isQuotient()) {
            return this.left.isAlwaysNonNegative() && this.right.isAlwaysNonNegative();
        }
        if (this.isPower()) {
            return this.left.isAlwaysNonNegative() || this.right.isEvenIntegerConstant();
        }
        return false;

    }

    @Override
    public boolean isAlwaysPositive() {

        if (this.isNonNegative() && !this.equals(ZERO)) {
            return true;
        }
        if (this.isSum()) {
            return (this.left.isAlwaysPositive() && this.right.isAlwaysNonNegative())
                    || (this.left.isAlwaysNonNegative() && this.right.isAlwaysPositive());
        }
        if (this.isProduct()) {
            return this.left.isAlwaysPositive() && this.right.isAlwaysPositive();
        }
        if (this.isQuotient()) {
            return this.left.isAlwaysPositive() && this.right.isAlwaysPositive();
        }
        if (this.isPower()) {
            return this.left.isAlwaysPositive();
        }
        return false;

    }

    @Override
    public boolean equals(Expression expr) {
        return expr instanceof BinaryOperation
                && this.getType().equals(((BinaryOperation) expr).getType())
                && this.getLeft().equals(((BinaryOperation) expr).getLeft())
                && this.getRight().equals(((BinaryOperation) expr).getRight());
    }

    @Override
    public boolean equivalent(Expression expr) {

        if (expr instanceof BinaryOperation) {
            if (this.getType().equals(((BinaryOperation) expr).getType())) {
                if (this.isSum()) {

                    ExpressionCollection summandsOfThis = SimplifyUtilities.getSummands(this);
                    ExpressionCollection summandsOfExpr = SimplifyUtilities.getSummands(expr);
                    return summandsOfThis.getBound() == summandsOfExpr.getBound()
                            && SimplifyUtilities.difference(summandsOfThis, summandsOfExpr).isEmpty();

                }
                if (this.isDifference()) {

                    ExpressionCollection summandsLeftOfThis = SimplifyUtilities.getSummandsLeftInExpression(this);
                    ExpressionCollection summandsRightOfThis = SimplifyUtilities.getSummandsRightInExpression(this);
                    ExpressionCollection summandsLeftOfExpr = SimplifyUtilities.getSummandsLeftInExpression(expr);
                    ExpressionCollection summandsRightOfExpr = SimplifyUtilities.getSummandsRightInExpression(expr);

                    ExpressionCollection summandsLeftOfThisWithSign = new ExpressionCollection();
                    ExpressionCollection summandsRightOfThisWithSign = new ExpressionCollection();
                    ExpressionCollection summandsLeftOfExprWithSign = new ExpressionCollection();
                    ExpressionCollection summandsRightOfExprWithSign = new ExpressionCollection();

                    try {
                        for (int i = 0; i < summandsLeftOfThis.getBound(); i++) {
                            if (summandsLeftOfThis.get(i).hasPositiveSign()) {
                                summandsLeftOfThisWithSign.add(summandsLeftOfThis.get(i));
                            } else {
                                summandsRightOfThisWithSign.add(MINUS_ONE.mult(summandsLeftOfThis.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsRightOfThis.getBound(); i++) {
                            if (summandsRightOfThis.get(i).hasPositiveSign()) {
                                summandsRightOfThisWithSign.add(summandsRightOfThis.get(i));
                            } else {
                                summandsLeftOfThisWithSign.add(MINUS_ONE.mult(summandsRightOfThis.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsLeftOfExpr.getBound(); i++) {
                            if (summandsLeftOfExpr.get(i).hasPositiveSign()) {
                                summandsLeftOfExprWithSign.add(summandsLeftOfExpr.get(i));
                            } else {
                                summandsRightOfExprWithSign.add(MINUS_ONE.mult(summandsLeftOfExpr.get(i)).orderSumsAndProducts());
                            }
                        }
                        for (int i = 0; i < summandsRightOfExpr.getBound(); i++) {
                            if (summandsRightOfExpr.get(i).hasPositiveSign()) {
                                summandsRightOfExprWithSign.add(summandsRightOfExpr.get(i));
                            } else {
                                summandsLeftOfExprWithSign.add(MINUS_ONE.mult(summandsRightOfExpr.get(i)).orderSumsAndProducts());
                            }
                        }
                        return summandsLeftOfThisWithSign.getBound() == summandsLeftOfExprWithSign.getBound()
                                && SimplifyUtilities.difference(summandsLeftOfThisWithSign, summandsLeftOfExprWithSign).isEmpty()
                                && summandsRightOfThisWithSign.getBound() == summandsRightOfExprWithSign.getBound()
                                && SimplifyUtilities.difference(summandsRightOfThisWithSign, summandsRightOfExprWithSign).isEmpty();
                    } catch (EvaluationException e) {
                    }

                }
                if (this.isProduct()) {

                    ExpressionCollection factorsOfThis = SimplifyUtilities.getFactors(this);
                    ExpressionCollection factorsOfExpr = SimplifyUtilities.getFactors(expr);
                    return factorsOfThis.getBound() == factorsOfExpr.getBound()
                            && SimplifyUtilities.difference(factorsOfThis, factorsOfExpr).isEmpty();

                }
                return this.getLeft().equivalent(((BinaryOperation) expr).getLeft())
                        && this.getRight().equivalent(((BinaryOperation) expr).getRight());
            }
            return false;
        }
        return false;

    }

    @Override
    public boolean hasPositiveSign() {
        if (this.type != TypeBinary.TIMES && this.type != TypeBinary.DIV) {
            return true;
        }
        return (this.left.hasPositiveSign() && this.right.hasPositiveSign()) || (!this.left.hasPositiveSign() && !this.right.hasPositiveSign());
    }

    @Override
    public int length() {
        if (this.isProduct()) {
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            int length = 0;
            for (int i = 0; i < factors.getBound(); i++) {
                if (!(factors.get(i) instanceof Constant)) {
                    length += factors.get(i).length();
                }
            }
            /* 
             Konstante Koeffizienten sollen nicht in die Länge miteinfließen, außer, 
             der Ausdruck ist an sich konstant.
             */
            return Math.max(length, 1);
        }
        if (this.isPower()) {
            if (this.left instanceof Constant) {
                return ((BinaryOperation) this).getRight().length();
            }
            if (this.right instanceof Constant) {
                return ((BinaryOperation) this).getLeft().length();
            }
        }
        if (this.isQuotient()) {
            // Hier wird eher die Länge des Bruches gewertet, nicht die des ausgeschriebenen Strings.
            return Math.max(this.left.length(), this.right.length());
        }
        return this.left.length() + this.right.length();
    }

    @Override
    public Expression simplifyTrivial() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        // Allgemeine Vereinfachungen, falls der zugrundeliegende Ausdruck konstant ist.
        Expression exprLeftAndRightSimplified;
        BinaryOperation expr;

        if (this.isSum()) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, summandsLeft.get(i).simplifyTrivial());
            }

            ExpressionCollection summandsRight = new ExpressionCollection();

            // Nullen in Summen beseitigen.
            SimplifyBinaryOperationMethods.removeZerosInSums(summandsLeft);

            // Summanden mit negativen Koeffizienten in den Subtrahenden bringen.
            SimplifyBinaryOperationMethods.simplifySumsAndDifferencesWithNegativeCoefficient(summandsLeft, summandsRight);

            // Schließlich: Falls der Ausdruck konstant ist und approximiert wird, direkt auswerten.
            if (this.isConstant() && this.containsApproximates()) {
                SimplifyBinaryOperationMethods.computeSumIfApprox(summandsLeft, summandsRight);
            }

            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isDifference()) {

            exprLeftAndRightSimplified = this.left.simplifyTrivial().sub(this.right.simplifyTrivial());
            if (!(exprLeftAndRightSimplified instanceof BinaryOperation)) {
                return exprLeftAndRightSimplified;
            }
            expr = (BinaryOperation) exprLeftAndRightSimplified;

            // Triviale Umformungen
            Expression exprSimplified = SimplifyBinaryOperationMethods.trivialOperationsInDifferenceWithZeroOne(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(exprSimplified);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(exprSimplified);

            // Brüche subtrahieren
            SimplifyBinaryOperationMethods.subtractFractions(summandsLeft, summandsRight);

            // Summanden mit negativen Koeffizienten in den in jeweils anderen Teil herübertragen.
            SimplifyBinaryOperationMethods.simplifySumsAndDifferencesWithNegativeCoefficient(summandsLeft, summandsRight);

            // Schließlich: Falls der Ausdruck konstant ist und approximiert wird, direkt auswerten.
            if (this.isConstant() && this.containsApproximates()) {
                return SimplifyBinaryOperationMethods.computeDifferenceIfApprox(SimplifyUtilities.produceDifference(summandsLeft, summandsRight));
            }

            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyTrivial());
            }

            Expression exprSimplified = SimplifyUtilities.produceProduct(factors);
            if (!exprSimplified.isProduct()) {
                return exprSimplified;
            }

            // Falls Nullen in Produkten auftauchen: 0 zurückgeben.
            SimplifyBinaryOperationMethods.reduceProductWithZeroToZero(factors);

            // Einsen in Produkten beseitigen.
            SimplifyBinaryOperationMethods.removeOnesInProducts(factors);

            /* 
             Falls in den Faktoren Summen / Differenzen auftauchen, in denen die 
             Summanden alle negatives Vorzeichen besitzen: -1 ausklammern!
             */
            SimplifyBinaryOperationMethods.pullMinusSignFromProductOrQuotientsWithCompleteNegativeSums(factors, new ExpressionCollection());

            // Schließlich: Falls der Ausdruck konstant ist und approximiert wird, direkt auswerten.
            if (this.isConstant() && this.containsApproximates()) {
                SimplifyBinaryOperationMethods.computeProductIfApprox(factors);
            }

            return SimplifyUtilities.produceProduct(factors);

        } else if (this.isQuotient()) {

            exprLeftAndRightSimplified = this.left.simplifyTrivial().div(this.right.simplifyTrivial());
            if (!(exprLeftAndRightSimplified instanceof BinaryOperation)) {
                return exprLeftAndRightSimplified;
            }
            expr = (BinaryOperation) exprLeftAndRightSimplified;
            Expression exprSimplified;

            // Triviale Umformungen
            exprSimplified = SimplifyBinaryOperationMethods.trivialOperationsInQuotientWithZeroOne(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Division durch 0 im Approximationsmodus ausschließen, sonst dividieren.
            exprSimplified = SimplifyBinaryOperationMethods.computeReciprocalInApprox(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Rationale Konstanten zu einem Bruch machen (etwa 0.74/0.2 = 37/10)
            exprSimplified = SimplifyBinaryOperationMethods.rationalConstantToQuotient(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Negative Zähler eliminieren.
            exprSimplified = SimplifyBinaryOperationMethods.eliminateNegativeDenominator(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Falls der Ausdruck konstant ist und approximiert wird, direkt auswerten.
            if (expr.isConstant() && expr.containsApproximates()) {
                return SimplifyBinaryOperationMethods.computeQuotientIfApprox(expr);
            }

            /* 
             Schließlich: Falls in den Faktoren im Zähler oder im Nenner Summen / Differenzen 
             auftauchen, in denen die Summanden alle negatives Vorzeichen besitzen: 
             -1 ausklammern!
             */
            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(expr);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(expr);
            SimplifyBinaryOperationMethods.pullMinusSignFromProductOrQuotientsWithCompleteNegativeSums(factorsEnumerator, factorsDenominator);
            return SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);

        }

        // Ab hier ist this eine Potenz.
        /*
         Hier wird das folgende kritische Problem aus dem Weg geschafft: Wird
         (-2)^(1/3) approximiert, so wird der Exponent zu 0.33333333333
         approximiert und dementsprechend kann das Ergebnis nicht berechnet
         werden. Daher, wenn expr eine ungerade Wurzeln darstellt: negatives
         Vorzeichen rausschaffen!
         */
        Expression exprSimplified = SimplifyBinaryOperationMethods.computeOddRootOfNegativeConstantsInApprox(this);
        if (!exprSimplified.equals(this)) {
            return exprSimplified;
        }

        exprLeftAndRightSimplified = this.left.simplifyTrivial().pow(this.right.simplifyTrivial());
        if (!(exprLeftAndRightSimplified instanceof BinaryOperation)) {
            return exprLeftAndRightSimplified;
        }
        expr = (BinaryOperation) exprLeftAndRightSimplified;

        // Nun folgen Vereinfachungen von Potenzen und Wurzeln konstanter Ausdrücke, soweit möglich.
        // Berechnung ganzzahliger Potenzen ganzer Zahlen.
        exprSimplified = SimplifyBinaryOperationMethods.computePowersOfIntegers(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Berechnung ganzzahliger Potenzen von Brüchen.
        exprSimplified = SimplifyBinaryOperationMethods.computePowersOfFractions(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Triviale Umformungen
        exprSimplified = SimplifyBinaryOperationMethods.trivialOperationsInPowerWithZeroOne(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Macht z.B. (5/7)^(4/3) = (5/7)*(5/7)^(1/3) = 5*(5/7)^(1/3)/7
        exprSimplified = SimplifyBinaryOperationMethods.separateIntegerPowersOfRationalConstants(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Minus-Zeichen aus ungeraden Wurzeln herausziehen.
        exprSimplified = SimplifyBinaryOperationMethods.takeMinusSignOutOfOddRoots(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Prüfen, ob Wurzeln gerader Ordnung aus negativen Konstanten gezogen werden.
        exprSimplified = SimplifyBinaryOperationMethods.checkNegativityOfBaseInRootsOfEvenDegree(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Versuchen, Wurzeln (z.B. in Quotienten oder von ganzen Zahlen) zum Teil exakt anzugeben.
        exprSimplified = SimplifyBinaryOperationMethods.tryTakePartialRootsPrecisely(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Versuchen, ganzzahlige Anteile von Exponenten abzuspalten.
        exprSimplified = SimplifyBinaryOperationMethods.separateIntegerPowersOfRationalConstants(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Vereinfache Potenzen von Quotienten, falls im Zähler oder im Nenner ganze Zahlen auftauchen.
        exprSimplified = SimplifyBinaryOperationMethods.simplifyPowerOfQuotient(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Vereinfacht: (a/b)^(-k) = (b/a)^k
        exprSimplified = SimplifyBinaryOperationMethods.negativePowersOfQuotientsToReciprocal(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Vereinfacht Folgendes (1/x)^y = 1/x^y.
        exprSimplified = SimplifyBinaryOperationMethods.simplifyPowersOfReciprocals(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Negative Potenzen in den Nenner: x^y = 1/x^(-y), falls y < 0. 
        exprSimplified = SimplifyBinaryOperationMethods.negativePowersOfExpressionsToReciprocal(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // (a^x)^y = a^(x*y)
        exprSimplified = SimplifyBinaryOperationMethods.simplifyDoublePowers(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // exp(x)^y = exp(x*y)
        exprSimplified = SimplifyBinaryOperationMethods.simplifyPowersOfExpFunction(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Vereinfacht Potenzen von Beträgen.
        exprSimplified = SimplifyBinaryOperationMethods.simplifyPowersOfAbs(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Schließlich: Falls der Ausdruck konstant ist und approximiert wird, direkt auswerten.
        if (expr.isConstant() && expr.containsApproximates()) {
            return SimplifyBinaryOperationMethods.computePowerIfApprox(expr);
        }

        return expr;

    }

    @Override
    public Expression simplifyByInsertingDefinedVars() throws EvaluationException {
        return new BinaryOperation(this.left.simplifyByInsertingDefinedVars(), this.right.simplifyByInsertingDefinedVars(), this.type);
    }

    @Override
    public Expression simplifyExpandRationalFactors() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln ausmultiplizieren.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyExpandRationalFactors());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isDifference() || this.isPower()) {
            return new BinaryOperation(this.left.simplifyExpandRationalFactors(), this.right.simplifyExpandRationalFactors(), this.type);
        }

        BinaryOperation expr;
        if (this.isProduct()) {
            // In jedem Faktor einzeln ausmultiplizieren.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyExpandRationalFactors());
            }
            Expression productOfSimplifiedFactors = SimplifyUtilities.produceProduct(factors);
            if (!(productOfSimplifiedFactors instanceof BinaryOperation)) {
                return productOfSimplifiedFactors;
            }
            expr = (BinaryOperation) productOfSimplifiedFactors;
        } else {
            Expression simplifiedQuotient;
            simplifiedQuotient = this.left.simplifyExpandRationalFactors().div(this.right.simplifyExpandRationalFactors());
            if (!(simplifiedQuotient instanceof BinaryOperation)) {
                return simplifiedQuotient;
            }
            expr = (BinaryOperation) this.left.simplifyExpandRationalFactors().div(this.right.simplifyExpandRationalFactors());
        }

        if (expr.isProduct() && expr.getLeft() instanceof Constant) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getRight());
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getRight());

            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, expr.getLeft().mult(summandsLeft.get(i)));
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, expr.getLeft().mult(summandsRight.get(i)));
            }
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (expr.isQuotient() && expr.getRight() instanceof Constant) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getLeft());
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getLeft());

            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, summandsLeft.get(i).div(expr.getRight()));
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, summandsRight.get(i).div(expr.getRight()));
            }
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        }

        return expr;

    }

    @Override
    public Expression simplifyExpand(TypeExpansion type) throws EvaluationException {

        Expression expr = this, exprExpanded = SimplifyBinaryOperationMethods.simplifySingleExpand(this, type);

        // Es wird solange ausmultipliziert, bis keine weitere Ausmultiplikation mehr möglich ist.
        while (!expr.equals(exprExpanded)) {
            // Prüfung, ob zwischendurch abgebrochen wurde.
            FlowController.interruptComputationIfNeeded();
            expr = exprExpanded.copy();
            exprExpanded = SimplifyBinaryOperationMethods.simplifySingleExpand(expr, type);
        }

        return expr;

    }

    @Override
    public Expression simplifyReduceLeadingsCoefficients() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        if (this.isSum()) {
            // In jedem Summanden einzeln kürzen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyReduceLeadingsCoefficients());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isProduct()) {
            // In jedem Faktor einzeln kürzen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyReduceLeadingsCoefficients());
            }

            /*
             Prüft, ob man beispielsweise flgendermaßen kürzen kann: 10*x*(1/6 + y/14)
             = 5*x*(1/3 + y/7).
             */
            SimplifyBinaryOperationMethods.pullGCDOfCoefficientsInProducts(factors);

            return SimplifyUtilities.produceProduct(factors);
        } else if (this.isPower()) {
            return this.left.simplifyReduceLeadingsCoefficients().pow(this.right.simplifyReduceLeadingsCoefficients());
        }

        // Nun kann es dich nur noch um type == TypeBinary.MINUS oder um type == TypeBinary.DIV handeln.
        ExpressionCollection termsLeft;
        ExpressionCollection termsRight;
        if (this.isDifference()) {
            termsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
            termsRight = SimplifyUtilities.getSummandsRightInExpression(this);
        } else {
            termsLeft = SimplifyUtilities.getFactorsOfEnumeratorInExpression(this);
            termsRight = SimplifyUtilities.getFactorsOfDenominatorInExpression(this);
        }

        // Zunächst in allen Summanden/Faktoren einzeln kürzen.
        for (int i = 0; i < termsLeft.getBound(); i++) {
            termsLeft.put(i, termsLeft.get(i).simplifyReduceLeadingsCoefficients());
        }
        for (int i = 0; i < termsRight.getBound(); i++) {
            termsRight.put(i, termsRight.get(i).simplifyReduceLeadingsCoefficients());
        }

        // Nun das eigentliche Kürzen!
        if (this.isDifference()) {

            SimplifyBinaryOperationMethods.reduceLeadingCoefficientsInDifferenceInApprox(termsLeft, termsRight);
            SimplifyBinaryOperationMethods.reduceLeadingCoefficientsInDifference(termsLeft, termsRight);

            // Ergebnis bilden.
            return SimplifyUtilities.produceDifference(termsLeft, termsRight);

        } else {

            // Vereinfachungen, bei den im Quotienten die FAKTOREN im Zähler und Nenner eine Rolle spielen.
            SimplifyBinaryOperationMethods.reduceLeadingCoefficientsInQuotientInApprox(termsLeft, termsRight);
            SimplifyBinaryOperationMethods.reduceLeadingCoefficientsInQuotient(termsLeft, termsRight);

            /*
             Prüft, ob man beispielsweise flgendermaßen kürzen kann: x*(10*a +
             25*b)*y/(35*c - 20*d) = x*(2*a + 5*b)*y/(7*c - 4*d).
             */
            SimplifyBinaryOperationMethods.reduceGCDInQuotient(termsLeft, termsRight);

            /*
             Prüft im Zähler und im Nenner, ob man beispielsweise flgendermaßen 
             kürzen kann: 10*x*(1/6 + y/14) = 5*x*(1/3 + y/7).
             */
            SimplifyBinaryOperationMethods.pullGCDOfCoefficientsInProducts(termsLeft);
            SimplifyBinaryOperationMethods.pullGCDOfCoefficientsInProducts(termsRight);

            /*
             Prüft, ob sich ganze Ausdrücke zu einer Konstanten kürzen lassen,
             etwa (5*a + 7*b)/(15*a + 21*b) = 1/3, (x - 3*y)/(12*y - 4*x) =
             -1/4 etc.
             */
            SimplifyBinaryOperationMethods.reduceFactorsInEnumeratorAndFactorInDenominatorToConstant(termsLeft, termsRight);

            /*
             Prüft, ob für RATIONALE Polynome (von nicht allzu hohem Grad)
             */
            SimplifyBinaryOperationMethods.reducePolynomialFactorsInNumeratorAndDenominatorByGCD(termsLeft, termsRight);

            /*
             Hier wird Folgendes vereinfacht: Falls der zugrundeliegende
             Ausdruck ein Bruch ist, etwa (A_1 * ... * A_m)/(B_1 * ... * B_n)
             und mindestens eines der A_i oder der B_j eine Summe oder
             Differenz ist, in der Brüche auftauchen, dann sollen diese auf
             einen gemeinsamen Nenner gebracht werden.
             */
            boolean sumsOrDifferencesWithFractionsOccur = false;

            for (int i = 0; i < termsLeft.getBound(); i++) {
                if (termsLeft.get(i) == null) {
                    continue;
                }
                if (termsLeft.get(i).isSum() || termsLeft.get(i).isDifference()) {

                    Expression factorSimplified = SimplifyBinaryOperationMethods.bringFractionToCommonDenominator((BinaryOperation) termsLeft.get(i));
                    if (!factorSimplified.equals(termsLeft.get(i))) {
                        termsLeft.put(i, factorSimplified);
                        sumsOrDifferencesWithFractionsOccur = true;
                    }

                } else if (termsLeft.get(i).isPower()
                        && SimplifyAlgebraicExpressionMethods.isAdmissibleExponent(((BinaryOperation) termsLeft.get(i)).getRight())
                        && (((BinaryOperation) termsLeft.get(i)).getLeft().isSum()
                        || ((BinaryOperation) termsLeft.get(i)).getLeft().isDifference())) {

                    Expression factorSimplified = SimplifyBinaryOperationMethods.bringFractionToCommonDenominator((BinaryOperation) ((BinaryOperation) termsLeft.get(i)).getLeft());
                    if (!factorSimplified.equals(termsLeft.get(i))) {
                        termsLeft.put(i, factorSimplified.pow(((BinaryOperation) termsLeft.get(i)).getRight()));
                        sumsOrDifferencesWithFractionsOccur = true;
                    }

                }
            }
            for (int i = 0; i < termsRight.getBound(); i++) {
                if (termsRight.get(i) == null) {
                    continue;
                }
                if (termsRight.get(i).isSum() || termsRight.get(i).isDifference()) {

                    Expression factorSimplified = SimplifyBinaryOperationMethods.bringFractionToCommonDenominator((BinaryOperation) termsRight.get(i));
                    if (!factorSimplified.equals(termsRight.get(i))) {
                        termsRight.put(i, factorSimplified);
                        sumsOrDifferencesWithFractionsOccur = true;
                    }

                } else if (termsRight.get(i).isPower()
                        && SimplifyAlgebraicExpressionMethods.isAdmissibleExponent(((BinaryOperation) termsRight.get(i)).getRight())
                        && (((BinaryOperation) termsRight.get(i)).getLeft().isSum()
                        || ((BinaryOperation) termsRight.get(i)).getLeft().isDifference())) {

                    Expression factorSimplified = SimplifyBinaryOperationMethods.bringFractionToCommonDenominator((BinaryOperation) ((BinaryOperation) termsRight.get(i)).getLeft());
                    if (!factorSimplified.equals(termsRight.get(i))) {
                        termsRight.put(i, factorSimplified.pow(((BinaryOperation) termsRight.get(i)).getRight()));
                        sumsOrDifferencesWithFractionsOccur = true;
                    }

                }
            }

            // Ergebnis bilden.
            if (sumsOrDifferencesWithFractionsOccur) {
                return SimplifyUtilities.produceQuotient(termsLeft, termsRight).orderDifferencesAndQuotients().simplifyReduceQuotients();
            }

            return SimplifyUtilities.produceQuotient(termsLeft, termsRight);

        }

    }

    @Override
    public Expression orderSumsAndProducts() throws EvaluationException {

        if (this.isNotSum() && this.isNotProduct()) {
            return new BinaryOperation(this.left.orderSumsAndProducts(), this.right.orderSumsAndProducts(), this.type);
        }

        // Fall type = +.
        if (this.isSum()) {

            Expression result = ZERO;
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // Sammelt Konstanten im ersten Summanden. Beispiel: 2+x+3+y+sin(1) wird zu 5+sin(1)+x+y
            summands = SimplifyBinaryOperationMethods.collectConstantsAndConstantExpressionsInSum(summands);

            for (int i = summands.getBound() - 1; i >= 0; i--) {
                if (summands.get(i) == null) {
                    continue;
                }
                summands.put(i, summands.get(i).orderSumsAndProducts());
                if (result.equals(ZERO)) {
                    result = summands.get(i).orderSumsAndProducts();
                } else {
                    result = summands.get(i).orderSumsAndProducts().add(result);
                }
            }

            return result;

        } else {

            // Fall type = *.
            Expression result = ONE;
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // Sammelt Konstanten im ersten Summanden. Beispiel: 2*x*3*y*sin(1) wird zu 6*sin(1)*x*y
            factors = SimplifyBinaryOperationMethods.collectConstantsAndConstantExpressionsInProduct(factors);

            for (int i = factors.getBound() - 1; i >= 0; i--) {
                if (factors.get(i) == null) {
                    continue;
                }
                factors.put(i, factors.get(i).orderSumsAndProducts());
                if (result.equals(ONE)) {
                    result = factors.get(i).orderSumsAndProducts();
                } else {
                    result = factors.get(i).orderSumsAndProducts().mult(result);
                }
            }

            return result;

        }

    }

    @Override
    public Expression orderDifferencesAndQuotients() throws EvaluationException {

        Expression result;

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        ExpressionCollection termsLeft = new ExpressionCollection();
        ExpressionCollection termsRight = new ExpressionCollection();

        if (this.isSum() || this.isDifference()) {

            SimplifyBinaryOperationMethods.orderDifference(this, termsLeft, termsRight);
            for (int i = 0; i < termsLeft.getBound(); i++) {
                termsLeft.put(i, termsLeft.get(i).orderDifferencesAndQuotients());
            }
            for (int i = 0; i < termsRight.getBound(); i++) {
                termsRight.put(i, termsRight.get(i).orderDifferencesAndQuotients());
            }
            result = SimplifyUtilities.produceDifference(termsLeft, termsRight);

        } else if (this.isProduct() || this.isQuotient()) {

            SimplifyBinaryOperationMethods.orderQuotient(this, termsLeft, termsRight);
            for (int i = 0; i < termsLeft.getBound(); i++) {
                termsLeft.put(i, termsLeft.get(i).orderDifferencesAndQuotients());
            }
            for (int i = 0; i < termsRight.getBound(); i++) {
                termsRight.put(i, termsRight.get(i).orderDifferencesAndQuotients());
            }
            result = SimplifyUtilities.produceQuotient(termsLeft, termsRight);

        } else {
            // Hier ist expr.getType() == TypeBinary.POW.
            result = this.left.orderDifferencesAndQuotients().pow(this.right.orderDifferencesAndQuotients());
        }

        return result;

    }

    @Override
    public Expression simplifyCollectProducts() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln Faktoren sammeln.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyCollectProducts());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isNotProduct()) {
            // Im linken und rechten Teil einzeln Faktoren sammeln.
            return new BinaryOperation(this.left.simplifyCollectProducts(), this.right.simplifyCollectProducts(), this.type);
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(this);

        //Ab hier ist type == *.
        // Zunächst in jedem Faktor einzeln Faktoren sammeln.
        for (int i = 0; i < factors.getBound(); i++) {
            factors.put(i, factors.get(i).simplifyCollectProducts());
        }

        SimplifyBinaryOperationMethods.collectFactorsInProduct(factors);
        return SimplifyUtilities.produceProduct(factors);

    }

    @Override
    public Expression simplifyFactorize() throws EvaluationException {

        if (this.isSum()) {

            // In jedem Summanden einzeln kürzen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyFactorize());
            }
            // Eigentliche Faktorisierung.
            SimplifyBinaryOperationMethods.simplifyFactorizeInSums(summands);
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isDifference()) {

            Expression expr = this.left.simplifyFactorize().sub(this.right.simplifyFactorize());
            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);
            // Eigentliche Faktorisierung.
            SimplifyBinaryOperationMethods.simplifyFactorizeInDifferences(summandsLeft, summandsRight);
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isProduct()) {

            // In jedem Faktor einzeln kürzen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyFactorize());
            }
            return SimplifyUtilities.produceProduct(factors);

        }

        // Hier ist type == DIV oder type == POW.
        return new BinaryOperation(this.left.simplifyFactorize(), this.right.simplifyFactorize(), this.type);

    }

    @Override
    public Expression simplifyFactorizeAllButRationalsInSums() throws EvaluationException {

        if (this.isProduct()) {
            // In jedem Faktor einzeln faktorisieren.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyFactorizeAllButRationalsInSums());
            }
            return SimplifyUtilities.produceProduct(factors);
        }
        if (this.isNotSum()) {
            return new BinaryOperation(this.left.simplifyFactorizeAllButRationalsInSums(), this.right.simplifyFactorizeAllButRationalsInSums(), this.type);
        }

        // Ab hier muss this als type + besitzen.
        ExpressionCollection summands = SimplifyUtilities.getSummands(this);
        // In jedem Summanden einzeln faktorisieren
        for (int i = 0; i < summands.getBound(); i++) {
            summands.put(i, summands.get(i).simplifyFactorizeAllButRationalsInSums());
        }

        // Eigentliche Faktorisierung.
        SimplifyBinaryOperationMethods.simplifyFactorizeAllButRationalsInSums(summands);
        // Ergebnis bilden.
        return SimplifyUtilities.produceSum(summands);

    }

    @Override
    public Expression simplifyFactorizeAllButRationalsInDifferences() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln kürzen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyFactorizeAllButRationalsInDifferences());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isProduct()) {
            // In jedem Faktor einzeln kürzen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyFactorizeAllButRationalsInDifferences());
            }
            return SimplifyUtilities.produceProduct(factors);
        } else if (this.isQuotient() || this.isPower()) {
            return new BinaryOperation(this.left.simplifyFactorizeAllButRationalsInDifferences(), this.right.simplifyFactorizeAllButRationalsInDifferences(), this.type);
        }

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);
        // In jedem Summanden einzeln faktorisieren
        for (int i = 0; i < summandsLeft.getBound(); i++) {
            summandsLeft.put(i, summandsLeft.get(i).simplifyFactorizeAllButRationalsInDifferences());
        }
        for (int i = 0; i < summandsRight.getBound(); i++) {
            summandsRight.put(i, summandsRight.get(i).simplifyFactorizeAllButRationalsInDifferences());
        }

        // Eigentliche Faktorisierung.
        SimplifyBinaryOperationMethods.simplifyFactorizeAllButRationalsInDifferences(summandsLeft, summandsRight);
        // Ergebnis bilden.
        return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

    }

    @Override
    public Expression simplifyFactorizeAllButRationals() throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln faktorisieren
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyFactorizeAllButRationals());
            }
            // Eigentliche Faktorisierung.
            SimplifyBinaryOperationMethods.simplifyFactorizeAllButRationalsInSums(summands);
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isDifference()) {

            Expression expr = this.left.simplifyFactorizeAllButRationals().sub(this.right.simplifyFactorizeAllButRationals());
            
            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);
            // Eigentliche Faktorisierung.
            SimplifyBinaryOperationMethods.simplifyFactorizeAllButRationalsInDifferences(summandsLeft, summandsRight);
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);
            
        } else if (this.isProduct()) {
            // In jedem Faktor einzeln faktorisieren.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyFactorizeAllButRationals());
            }
            return SimplifyUtilities.produceProduct(factors);
        }
        
        // Hier ist type == DIV oder type == POW.
        return new BinaryOperation(this.left.simplifyFactorizeAllButRationals(), this.right.simplifyFactorizeAllButRationals(), this.type);
        
    }

    @Override
    public Expression simplifyReduceQuotients() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln kürzen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyReduceQuotients());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isProduct()) {
            // In jedem Faktor einzeln kürzen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyReduceQuotients());
            }
            return SimplifyUtilities.produceProduct(factors);
        } else if (this.isDifference() || this.isPower()) {
            return new BinaryOperation(this.left.simplifyReduceQuotients(), this.right.simplifyReduceQuotients(), this.type);
        }

        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(this);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(this);

        // In jedem Faktor einzeln kürzen
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            factorsEnumerator.put(i, factorsEnumerator.get(i).simplifyReduceQuotients());
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            factorsDenominator.put(i, factorsDenominator.get(i).simplifyReduceQuotients());
        }

        // Eigentliches Kürzen.
        SimplifyBinaryOperationMethods.simplifyReduceFactorsInQuotients(factorsEnumerator, factorsDenominator);
        // Ergebnis bilden.
        return SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);

    }

    @Override
    public Expression simplifyPullApartPowers() throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Potenzen vereinfachen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyPullApartPowers());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        }

        if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Summanden einzeln Potenzen vereinfachen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyPullApartPowers());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        }

        if (this.isDifference() || this.isQuotient()) {
            return new BinaryOperation(this.left.simplifyPullApartPowers(), this.right.simplifyPullApartPowers(), this.type);
        }

        // Ab hier ist type == TypeBinary.POW
        Expression expr = this.left.simplifyPullApartPowers().pow(this.right.simplifyPullApartPowers());
        Expression exprSimplified;

        exprSimplified = SimplifyExpLog.splitPowersInProduct(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        exprSimplified = SimplifyExpLog.splitPowersInQuotient(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        return expr;

    }

    @Override
    public Expression simplifyMultiplyExponents() throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Potenzen vereinfachen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyMultiplyExponents());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Summanden einzeln Potenzen vereinfachen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyMultiplyExponents());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        } else if (this.isDifference() || this.isQuotient()) {

            return new BinaryOperation(this.left.simplifyMultiplyExponents(), this.right.simplifyMultiplyExponents(), this.type);

        }

        // Hier ist this.type == TypeBinary.POW
        Expression leftSimplified = this.left.simplifyMultiplyExponents();
        if (leftSimplified.isPower()) {
            return ((BinaryOperation) leftSimplified).getLeft().pow(((BinaryOperation) leftSimplified).getRight().mult(this.right));
        }
        return this;

    }

    @Override
    public Expression simplifyFunctionalRelations() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        BinaryOperation expr = this;
        Expression exprSimplified;

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionalgleichungen anwenden.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyFunctionalRelations());
            }

            // sinh(x) + cosh(x) = exp(x)
            SimplifyFunctionalRelations.sumOfTwoFunctions(summands, TypeFunction.sinh, TypeFunction.cosh, TypeFunction.exp);
            //cos(x)^2 + sin(x)^2 = 1
            SimplifyFunctionalRelations.reduceSumOfSquaresOfSineAndCosine(summands);
            //1 + tan(x)^2 = sec(x)^2
            SimplifyFunctionalRelations.reduceOnePlusFunctionSquareToFunctionSquare(summands, TypeFunction.tan, TypeFunction.sec);
            //1 + cot(x)^2 = cosec(x)^2
            SimplifyFunctionalRelations.reduceOnePlusFunctionSquareToFunctionSquare(summands, TypeFunction.cot, TypeFunction.cosec);
            //1 + sinh(x)^2 = cosh(x)^2
            SimplifyFunctionalRelations.reduceOnePlusFunctionSquareToFunctionSquare(summands, TypeFunction.sinh, TypeFunction.cosh);
            //1 + cosech(x)^2 = coth(x)^2
            SimplifyFunctionalRelations.reduceOnePlusFunctionSquareToFunctionSquare(summands, TypeFunction.cosech, TypeFunction.coth);

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        }

        if (this.isDifference()) {

            // Im Minuenden und Subtrahenden einzeln Funktionalgleichungen anwenden.
            Expression simplifiedDifference = this.left.simplifyFunctionalRelations().sub(this.right.simplifyFunctionalRelations());
            if (!(simplifiedDifference instanceof BinaryOperation)) {
                return simplifiedDifference;
            }
            expr = (BinaryOperation) simplifiedDifference;

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);

            //cosh(x) - sinh(x) = exp(-x) bzw. sinh(x) - cosh(x) = -exp(-x)
            SimplifyFunctionalRelations.reduceCoshMinusSinhToExp(summandsLeft, summandsRight);
            //cosh(x)^2 - sinh(x)^2 = 1 bzw. sinh(x)^2 - cosh(x)^2 = -1
            SimplifyFunctionalRelations.reduceDifferenceOfSquaresOfHypSineAndHypCosine(summandsLeft, summandsRight);
            //1 - tanh(x)^2 = sech(x)^2 bzw. tanh(x)^2 - 1 = -sech(x)^2
            SimplifyFunctionalRelations.reduceOneMinusFunctionSquareToFunctionSquare(summandsLeft, summandsRight, TypeFunction.tanh, TypeFunction.sech);
            //1 - sech(x)^2 = tanh(x)^2 bzw. sech(x)^2 - 1 = -tanh(x)^2
            SimplifyFunctionalRelations.reduceOneMinusFunctionSquareToFunctionSquare(summandsLeft, summandsRight, TypeFunction.sech, TypeFunction.tanh);
            //1 - sin(x)^2 = cos(x)^2 bzw. sin(x)^2 - 1 = -cos(x)^2
            SimplifyFunctionalRelations.reduceOneMinusFunctionSquareToFunctionSquare(summandsLeft, summandsRight, TypeFunction.sin, TypeFunction.cos);
            //1 - cos(x)^2 = sin(x)^2 bzw. cos(x)^2 - 1 = -sin(x)^2
            SimplifyFunctionalRelations.reduceOneMinusFunctionSquareToFunctionSquare(summandsLeft, summandsRight, TypeFunction.cos, TypeFunction.sin);
            //cosh(x)^2 - 1 = sinh(x)^2 bzw. 1 - cosh(x)^2 = -sinh(x)^2
            SimplifyFunctionalRelations.reduceFunctionSquareMinusOneToFunctionSquare(summandsLeft, summandsRight, TypeFunction.cosh, TypeFunction.sinh);
            //coth(x)^2 - 1 = cosech(x)^2 bzw. 1 - coth(x)^2 = -cosech(x)^2
            SimplifyFunctionalRelations.reduceFunctionSquareMinusOneToFunctionSquare(summandsLeft, summandsRight, TypeFunction.coth, TypeFunction.cosech);
            //sec(x)^2 - 1 = tan(x)^2 bzw. 1 - sec(x)^2 = -tan(x)^2
            SimplifyFunctionalRelations.reduceFunctionSquareMinusOneToFunctionSquare(summandsLeft, summandsRight, TypeFunction.sec, TypeFunction.tan);
            //cosec(x)^2 - 1 = cot(x)^2 bzw. 1 - cosec(x)^2 = -cot(x)^2
            SimplifyFunctionalRelations.reduceFunctionSquareMinusOneToFunctionSquare(summandsLeft, summandsRight, TypeFunction.cosec, TypeFunction.cot);

            // Ergebnis bilden.
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        }

        if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionalgleichungen anwenden.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyFunctionalRelations());
            }

            //Potenzen von rationalen Zahlen sammeln
            SimplifyExpLog.collectPowersOfRationalsWithSameExponentInProduct(factors);
            //Exponentialfunktionen sammeln
            SimplifyExpLog.collectExponentialFunctionsInProduct(factors);
            //Produkte von Beträgen zu einem einzigen Betrag machen
            SimplifyFunctionalRelations.pullTogetherProductsOfMultiplicativeFunctions(factors, TypeFunction.abs);
            //Produkte von Signum zu einem einzigen Signum machen
            SimplifyFunctionalRelations.pullTogetherProductsOfMultiplicativeFunctions(factors, TypeFunction.sgn);
            //x*sgn(x) = abs(x)
            SimplifyFunctionalRelations.reduceProductOfIdAndSgnToAbs(factors);
            //abs(x)^k*sgn(x)^k = x^k (genauer = id(x)^k)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.abs, TypeFunction.sgn, TypeFunction.id);
            //cos(x)*tan(x) = sin(x)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.cos, TypeFunction.tan, TypeFunction.sin);
            //sin(x)*sec(x) = tan(x)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.sin, TypeFunction.sec, TypeFunction.tan);
            //sin(x)*cot(x) = cos(x)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.sin, TypeFunction.cot, TypeFunction.cos);
            //cos(x)*cosec(x) = cot(x)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.cos, TypeFunction.cosec, TypeFunction.cot);
            //sec(x)*cot(x) = cosec(x)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.sec, TypeFunction.cot, TypeFunction.cosec);
            //cosec(x)*tan(x) = sec(x)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.cosec, TypeFunction.tan, TypeFunction.sec);
            //cosh(x)*tanh(x) = sinh(x)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.cosh, TypeFunction.tanh, TypeFunction.sinh);
            //sinh(x)*sech(x) = tanh(x)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.sinh, TypeFunction.sech, TypeFunction.tanh);
            //sinh(x)*coth(x) = cosh(x)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.sinh, TypeFunction.coth, TypeFunction.cosh);
            //cosh(x)*cosech(x) = coth(x)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.cosh, TypeFunction.cosech, TypeFunction.coth);
            //sech(x)*coth(x) = cosech(x)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.sech, TypeFunction.coth, TypeFunction.cosech);
            //cosec(x)*tan(x) = sec(x)
            SimplifyFunctionalRelations.productOfTwoFunctions(factors, TypeFunction.cosech, TypeFunction.tanh, TypeFunction.sech);
            //sin(x)*cosec(x) = 1
            SimplifyFunctionalRelations.productOfTwoFunctionsEqualsOne(factors, TypeFunction.sin, TypeFunction.cosec);
            //cos(x)*sec(x) = 1
            SimplifyFunctionalRelations.productOfTwoFunctionsEqualsOne(factors, TypeFunction.cos, TypeFunction.sec);
            //tan(x)*cot(x) = 1
            SimplifyFunctionalRelations.productOfTwoFunctionsEqualsOne(factors, TypeFunction.tan, TypeFunction.cot);
            //sinh(x)*cosech(x) = 1
            SimplifyFunctionalRelations.productOfTwoFunctionsEqualsOne(factors, TypeFunction.sinh, TypeFunction.cosech);
            //cosh(x)*sech(x) = 1
            SimplifyFunctionalRelations.productOfTwoFunctionsEqualsOne(factors, TypeFunction.cosh, TypeFunction.sech);
            //tanh(x)*coth(x) = 1
            SimplifyFunctionalRelations.productOfTwoFunctionsEqualsOne(factors, TypeFunction.tanh, TypeFunction.coth);
            //sin(x)*cos(x) = sin(2*x)/2
            SimplifyFunctionalRelations.productOfTwoFunctionsToFunctionOfDoubleArgument(factors, TypeFunction.sin, TypeFunction.cos);
            //sinh(x)*cosh(x) = sinh(2*x)/2
            SimplifyFunctionalRelations.productOfTwoFunctionsToFunctionOfDoubleArgument(factors, TypeFunction.sinh, TypeFunction.cosh);
            // x!*(-1-x)! = x/sin(pi*x) (Ergänzungssatz)
            SimplifyFunctionalRelations.collectFactorialsInProductByReflectionFormula(factors);

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        }

        if (this.isQuotient()) {

            // Im Dividenden und Divisor einzeln Funktionalgleichungen anwenden.
            Expression simplifiedQuotient = this.left.simplifyFunctionalRelations().div(this.right.simplifyFunctionalRelations());
            if (!(simplifiedQuotient instanceof BinaryOperation)) {
                return simplifiedQuotient;
            }
            expr = (BinaryOperation) simplifiedQuotient;

            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(expr);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(expr);

            //Potenzen von rationalen Zahlen sammeln
            SimplifyExpLog.collectPowersOfRationalsWithSameExponentInQuotient(factorsEnumerator, factorsDenominator);
            //Exponentialfunktionen sammeln
            SimplifyExpLog.collectExponentialFunctionsInQuotient(factorsEnumerator, factorsDenominator);
            //Bringt allgemeine nichtkonstante Exponentialfunktionen aus dem Nenner in den Zähler
            SimplifyExpLog.bringNonConstantExponentialFunctionsToEnumerator(factorsEnumerator, factorsDenominator);
            //Logarithmen zur Basis 10 zu rationalen Zahlen kürzen
            SimplifyExpLog.simplifyQuotientsOfLogarithms(factorsEnumerator, factorsDenominator, TypeFunction.lg);
            //Logarithmen zur Basis e zu rationalen Zahlen kürzen
            SimplifyExpLog.simplifyQuotientsOfLogarithms(factorsEnumerator, factorsDenominator, TypeFunction.ln);
            //Quotienten von Beträgen zu einem einzigen Betrag machen
            SimplifyFunctionalRelations.pullTogetherQuotientsOfMultiplicativeFunctions(factorsEnumerator, factorsDenominator, TypeFunction.abs);
            //Quotienten von Signum zu einem einzigen Signum machen
            SimplifyFunctionalRelations.pullTogetherQuotientsOfMultiplicativeFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sgn);
            //sin(x)/cos(x) = tan(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sin, TypeFunction.cos, TypeFunction.tan);
            //cos(x)/sin(x) = cot(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cos, TypeFunction.sin, TypeFunction.cot);
            //tan(x)/sin(x) = sec(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.tan, TypeFunction.sin, TypeFunction.sec);
            //cot(x)/cos(x) = cosec(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cot, TypeFunction.cos, TypeFunction.cosec);
            //sin(x)/tan(x) = cos(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sin, TypeFunction.tan, TypeFunction.cos);
            //cos(x)/cot(x) = sin(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cos, TypeFunction.cot, TypeFunction.sin);
            //sec(x)/cosec(x) = tan(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sec, TypeFunction.cosec, TypeFunction.tan);
            //cosec(x)/sec(x) = cot(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cosec, TypeFunction.sec, TypeFunction.cot);
            //sinh(x)/cosh(x) = tanh(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sinh, TypeFunction.cosh, TypeFunction.tanh);
            //cosh(x)/sinh(x) = coth(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cosh, TypeFunction.sinh, TypeFunction.coth);
            //tanh(x)/sinh(x) = sech(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.tanh, TypeFunction.sinh, TypeFunction.sech);
            //coth(x)/cosh(x) = cosech(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.coth, TypeFunction.cosh, TypeFunction.cosech);
            //sinh(x)/tanh(x) = cosh(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sinh, TypeFunction.tanh, TypeFunction.cosh);
            //cosh(x)/coth(x) = sinh(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cosh, TypeFunction.coth, TypeFunction.sinh);
            //sech(x)/cosech(x) = tanh(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.sech, TypeFunction.cosech, TypeFunction.tanh);
            //cosech(x)/sech(x) = coth(x)
            SimplifyFunctionalRelations.quotientOfTwoFunctions(factorsEnumerator, factorsDenominator, TypeFunction.cosech, TypeFunction.sech, TypeFunction.coth);
            //1/sin(x) = cosec(x)
            SimplifyFunctionalRelations.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.sin, TypeFunction.cosec);
            //1/cos(x) = sec(x)
            SimplifyFunctionalRelations.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.cos, TypeFunction.sec);
            //1/tan(x) = cot(x)
            SimplifyFunctionalRelations.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.tan, TypeFunction.cot);
            //1/cot(x) = tan(x)
            SimplifyFunctionalRelations.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.cot, TypeFunction.tan);
            //1/sec(x) = cos(x)
            SimplifyFunctionalRelations.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.sec, TypeFunction.cos);
            //1/cosec(x) = sin(x)
            SimplifyFunctionalRelations.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.cosec, TypeFunction.sin);
            //1/sinh(x) = cosech(x)
            SimplifyFunctionalRelations.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.sinh, TypeFunction.cosech);
            //1/cosh(x) = sech(x)
            SimplifyFunctionalRelations.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.cosh, TypeFunction.sech);
            //1/tanh(x) = coth(x)
            SimplifyFunctionalRelations.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.tanh, TypeFunction.coth);
            //1/coth(x) = tanh(x)
            SimplifyFunctionalRelations.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.coth, TypeFunction.tanh);
            //1/sech(x) = cosh(x)
            SimplifyFunctionalRelations.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.sech, TypeFunction.cosh);
            //1/cosech(x) = sinh(x)
            SimplifyFunctionalRelations.reciprocalOfFunction(factorsEnumerator, factorsDenominator, TypeFunction.cosech, TypeFunction.sinh);

            // Ergebnis bilden.
            return SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);

        }

        if (this.isPower()) {

            // In Basis und Exponenten einzeln Funktionalgleichungen anwenden.
            Expression simplifiedPower = this.left.simplifyFunctionalRelations().pow(this.right.simplifyFunctionalRelations());
            if (!(simplifiedPower instanceof BinaryOperation)) {
                return simplifiedPower;
            }
            expr = (BinaryOperation) simplifiedPower;

            exprSimplified = SimplifyBinaryOperationMethods.reducePowerOfTenAndSumsOfLog10(expr);
            if (!exprSimplified.equals(this)) {
                return exprSimplified;
            }

            exprSimplified = SimplifyBinaryOperationMethods.reducePowerOfTenAndDifferencesOfLog10(expr);
            if (!exprSimplified.equals(this)) {
                return exprSimplified;
            }

            exprSimplified = SimplifyFunctionMethods.powerOfSgn(expr);
            if (!exprSimplified.equals(this)) {
                return exprSimplified;
            }

        }

        return expr;

    }

    @Override
    public Expression simplifyExpandAndCollectEquivalentsIfShorter() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        Expression expr = this;
        Expression exprSimplified;

        try {
            // "Kurzes / Schnelles" Ausmultiplizieren soll stattfinden.
            if (this.isQuotient()) {
                /* 
                 Bei einem Quotienten soll man im Zähler und im Nenner separat beurteilen, 
                 ob sich diese Vereinfachungsart lohnt. 
                 */
                exprSimplified = ((BinaryOperation) expr).getLeft().simplifyExpandAndCollectEquivalentsIfShorter().div(
                        ((BinaryOperation) expr).getRight().simplifyExpandAndCollectEquivalentsIfShorter());
                exprSimplified = exprSimplified.simplify(simplifyTypesExpandAndCollectIfShorter);
            } else {
                exprSimplified = expr.simplifyExpand(TypeExpansion.SHORT);
                exprSimplified = exprSimplified.simplify(simplifyTypesExpandAndCollectIfShorter);
            }
            if (exprSimplified.length() < expr.length()) {
                return exprSimplified;
            }
        } catch (EvaluationException e) {
            return expr;
        }

        return this;

    }

    @Override
    public Expression simplifyCollectLogarithms() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);
        // Faktoren vor Logarithmusfunktionen zur Basis 10 in die Logarithmen hineinziehen.
        SimplifyExpLog.pullFactorsIntoLogarithms(summandsLeft, TypeFunction.lg);
        SimplifyExpLog.pullFactorsIntoLogarithms(summandsRight, TypeFunction.lg);
        // Faktoren vor Logarithmusfunktionen zur Basis e in die Logarithmen hineinziehen.
        SimplifyExpLog.pullFactorsIntoLogarithms(summandsLeft, TypeFunction.ln);
        SimplifyExpLog.pullFactorsIntoLogarithms(summandsRight, TypeFunction.ln);
        Expression expr = SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        if (expr.isSum()) {

            // In jedem Summanden einzeln Logarithmen sammeln.
            ExpressionCollection summands = SimplifyUtilities.getSummands(expr);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyCollectLogarithms());
            }

            //Logarithmusfunktionen zur Basis 10 in einer Summe sammeln
            SimplifyExpLog.collectLogarithmsInSum(summands, TypeFunction.lg);
            //Logarithmusfunktionen zur Basis e in einer Summe sammeln
            SimplifyExpLog.collectLogarithmsInSum(summands, TypeFunction.ln);

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (expr.isDifference()) {

            // Im Minuenden und Subtrahenden einzeln Logarithmen sammeln.
            expr = ((BinaryOperation) expr).getLeft().simplifyCollectLogarithms().sub(((BinaryOperation) expr).getRight().simplifyCollectLogarithms());

            summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
            summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);

            //Logarithmusfunktionen zur Basis 10 in einer Differenz sammeln
            SimplifyExpLog.collectLogarithmsInDifference(summandsLeft, summandsRight, TypeFunction.lg);
            //Logarithmusfunktionen zur Basis e in einer Differenz sammeln
            SimplifyExpLog.collectLogarithmsInDifference(summandsLeft, summandsRight, TypeFunction.ln);

            // Ergebnis bilden.
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (expr.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(expr);
            // In jedem Faktor einzeln Logarithmen sammeln.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyCollectLogarithms());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        } else if (expr instanceof BinaryOperation) {
            return new BinaryOperation(((BinaryOperation) expr).getLeft().simplifyCollectLogarithms(),
                    ((BinaryOperation) expr).getRight().simplifyCollectLogarithms(),
                    ((BinaryOperation) expr).getType());
        }

        return expr;

    }

    @Override
    public Expression simplifyExpandLogarithms() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        if (this.isSum() || this.isDifference()) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);
            // In jedem Summanden einzeln Logarithmen auseinanderziehen.
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, summandsLeft.get(i).simplifyExpandLogarithms());
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, summandsRight.get(i).simplifyExpandLogarithms());
            }
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isProduct() || this.isQuotient()) {

            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(this);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(this);
            // In jedem Faktor einzeln Logarithmen auseinanderziehen.
            for (int i = 0; i < factorsEnumerator.getBound(); i++) {
                factorsEnumerator.put(i, factorsEnumerator.get(i).simplifyExpandLogarithms());
            }
            for (int i = 0; i < factorsDenominator.getBound(); i++) {
                factorsDenominator.put(i, factorsDenominator.get(i).simplifyExpandLogarithms());
            }
            return SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);

        }

        // Dann ist this eine Potenz.
        return this.left.simplifyExpandLogarithms().pow(this.right.simplifyExpandLogarithms());

    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsByDefinitions() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyReplaceExponentialFunctionsByDefinitions());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyReplaceExponentialFunctionsByDefinitions());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        } else if (this.isPower() && this.left.isConstant()) {
            // Nur dann ersetzen, wenn die Basis konstant ist.
            return this.left.ln().mult(this.right).exp();
        }

        return new BinaryOperation(this.left.simplifyReplaceExponentialFunctionsByDefinitions(),
                this.right.simplifyReplaceExponentialFunctionsByDefinitions(),
                this.type);

    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(String var) throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(var));
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(var));
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        } else if (this.isPower() && !this.left.contains(var)) {
            // Nur dann ersetzen, wenn die Basis bzgl. var konstant ist.
            return this.left.ln().mult(this.right).exp();
        }

        return new BinaryOperation(this.left.simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(var),
                this.right.simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(var),
                this.type);

    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsByDefinitions() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyReplaceTrigonometricalFunctionsByDefinitions());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyReplaceTrigonometricalFunctionsByDefinitions());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        }

        return new BinaryOperation(this.left.simplifyReplaceTrigonometricalFunctionsByDefinitions(),
                this.right.simplifyReplaceTrigonometricalFunctionsByDefinitions(),
                this.type);

    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(String var) throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(var));
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(var));
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        }

        return new BinaryOperation(this.left.simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(var),
                this.right.simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(var),
                this.type);

    }

    /* 
     Es folgen Methoden für die Integration von Funktionen vom Typ exp(ax+b) * h_1(c_1x+d_1)^n_1 * ... h_m(c_mx+d_m)^n_m
     mit h_i = sin oder = cos.
     */
    public static boolean isPolynomialInVariousExponentialAndTrigonometricalFunctions(Expression f, String var) {
        if (!f.contains(var)) {
            return true;
        }
        if (f.isSum() || f.isDifference() || f.isProduct()) {
            return isPolynomialInVariousExponentialAndTrigonometricalFunctions(((BinaryOperation) f).getLeft(), var)
                    && isPolynomialInVariousExponentialAndTrigonometricalFunctions(((BinaryOperation) f).getRight(), var);
        }
        if (f.isQuotient()) {
            return isPolynomialInVariousExponentialAndTrigonometricalFunctions(((BinaryOperation) f).getLeft(), var)
                    && !((BinaryOperation) f).getRight().contains(var);
        }
        if (f.isPower()) {
            return isPolynomialInVariousExponentialAndTrigonometricalFunctions(((BinaryOperation) f).getLeft(), var)
                    && ((BinaryOperation) f).getRight().isIntegerConstant()
                    && ((BinaryOperation) f).getRight().isPositive();
        }
        if (f.isFunction()) {
            return (f.isFunction(TypeFunction.exp) || f.isFunction(TypeFunction.cos)
                    || f.isFunction(TypeFunction.sin))
                    && SimplifyPolynomialMethods.isPolynomial(((Function) f).getLeft(), var)
                    && SimplifyPolynomialMethods.degreeOfPolynomial(((Function) f).getLeft(), var).compareTo(BigInteger.ONE) <= 0;
        }
        if (f instanceof Operator) {
            return !f.contains(var);
        }
        return false;
    }

    private static BigInteger getUpperBoundForSummands(Expression f, String var) {

        if (f.isSum()) {
            BigInteger numberOfSummands = BigInteger.ZERO;
            ExpressionCollection summands = SimplifyUtilities.getSummands(f);
            for (int i = 0; i < summands.getBound(); i++) {
                if (summands.get(i) == null) {
                    continue;
                }
                numberOfSummands = numberOfSummands.add(getUpperBoundForSummands(summands.get(i), var));
            }
            return numberOfSummands;
        } else if (f.isDifference()) {
            return getUpperBoundForSummands(((BinaryOperation) f).getLeft(), var).add(getUpperBoundForSummands(((BinaryOperation) f).getRight(), var));
        } else if (f.isProduct()) {
            BigInteger numberOfSummands = BigInteger.ONE;
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            for (int i = 0; i < factors.getBound(); i++) {
                if (factors.get(i) == null) {
                    continue;
                }
                numberOfSummands = numberOfSummands.multiply(getUpperBoundForSummands(factors.get(i), var));
            }
            return numberOfSummands;
        } else if (f.isQuotient() && !((BinaryOperation) f).getRight().contains(var)) {
            return getUpperBoundForSummands(((BinaryOperation) f).getLeft(), var);
        } else if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant()
                && ((BinaryOperation) f).getRight().isPositive()
                && ((Constant) ((BinaryOperation) f).getRight()).getValue().compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0) {

            Expression base = ((BinaryOperation) f).getLeft();
            int exponent = ((Constant) ((BinaryOperation) f).getRight()).getValue().intValue();

            /*
             Fall: Basis ist sin oder cos mit linearem Argument. Auf Linearität 
             des Arguments wird nicht geprüft, da dies vor dem Aufruf sichergestellt
             werden muss.
             */
            if (base.isFunction(TypeFunction.sin) || f.isFunction(TypeFunction.cos)) {
                return BigInteger.valueOf(exponent / 2 + 1);
            }

            // Fall: Basis ist keine trigonometrische Funktion.
            BigInteger numberOfSummandsInBaseAsBigInt = getUpperBoundForSummands(base, var);
            if (numberOfSummandsInBaseAsBigInt.compareTo(BigInteger.ZERO) > 0
                    && numberOfSummandsInBaseAsBigInt.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
                int numberOfSummandsInBase = numberOfSummandsInBaseAsBigInt.intValue();
                // Anzahl der Summanden in (a_1 + ... + a_n)^k ist (n - 1 + k)!/[(n - 1)! * k!]
                return ArithmeticMethods.factorial(numberOfSummandsInBase - 1 + exponent).divide(
                        ArithmeticMethods.factorial(numberOfSummandsInBase - 1).multiply(ArithmeticMethods.factorial(exponent)));
            }
        } else if (f.isFunction(TypeFunction.exp) || f.isFunction(TypeFunction.cos) || f.isFunction(TypeFunction.sin)) {
            return BigInteger.ONE;
        } else if (f instanceof Operator) {
            if (!f.contains(var)) {
                return BigInteger.ONE;
            }
        }

        // Dann ist das kein Polynom in komplexen Exponentialfunktionen.
        return BigInteger.valueOf(-1);

    }

    private static Expression expandPowerOfCos(Expression argument, int n) {
        // Achtung: Im Folgenden findet keine Validierung für n statt. Dies muss im Vorfeld stattfinden.
        Expression result = ZERO;

        if (n % 2 == 0) {
            for (int i = 0; i < n / 2; i++) {
                result = result.add(new Constant(ArithmeticMethods.bin(n, i)).mult(new Constant(n - 2 * i).mult(argument).cos()).div(
                        new Constant(BigInteger.valueOf(2).pow(n - 1))));
            }
            result = result.add(new Constant(ArithmeticMethods.bin(n, n / 2)).div(new Constant(BigInteger.valueOf(2).pow(n))));
        } else {
            for (int i = 0; i <= (n - 1) / 2; i++) {
                result = result.add(new Constant(ArithmeticMethods.bin(n, i)).mult(new Constant(n - 2 * i).mult(argument).cos()).div(
                        new Constant(BigInteger.valueOf(2).pow(n - 1))));
            }
        }

        return result;
    }

    /* 
     Es folgen Methoden für die Integration von Funktionen vom Typ exp(ax+b) * h_1(c_1x+d_1)^n_1 * ... h_m(c_mx+d_m)^n_m
     mit h_i = sin oder = cos.
     */
    private static Expression expandPowerOfSin(Expression argument, int n) {

        // Achtung: Im Folgenden findet keine Validierung für n statt. Dies muss im Vorfeld stattfinden.
        Expression result = ZERO;

        int m;
        if (n % 2 == 0) {
            m = n / 2;
            for (int i = 0; i < m; i++) {
                result = result.add(new Constant(BigInteger.valueOf(-1).pow(m + i).multiply(ArithmeticMethods.bin(n, i))).mult(new Constant(n - 2 * i).mult(argument).cos()).div(
                        new Constant(BigInteger.valueOf(2).pow(n - 1))));
            }
            result = result.add(new Constant(ArithmeticMethods.bin(n, m)).div(new Constant(BigInteger.valueOf(2).pow(n))));
        } else {
            // n = 2 * m + 1 ist ungerade.
            m = (n - 1) / 2;
            for (int i = 0; i <= m; i++) {
                result = result.add(new Constant(BigInteger.valueOf(-1).pow(m + i).multiply(ArithmeticMethods.bin(n, i))).mult(new Constant(n - 2 * i).mult(argument).sin()).div(
                        new Constant(BigInteger.valueOf(2).pow(n - 1))));
            }
        }

        return result;

    }

    private static Expression rewriteProductOfSinSin(Expression argumentLeft, Expression argumentRight) {
        return argumentLeft.sub(argumentRight).cos().sub(argumentLeft.add(argumentRight).cos()).div(2);
    }

    private static Expression rewriteProductOfCosCos(Expression argumentLeft, Expression argumentRight) {
        return argumentLeft.sub(argumentRight).cos().add(argumentLeft.add(argumentRight).cos()).div(2);
    }

    private static Expression rewriteProductOfSinCos(Expression argumentSin, Expression argumentCos) {
        return rewriteProductOfCosSin(argumentCos, argumentSin);
    }

    private static Expression rewriteProductOfCosSin(Expression argumentCos, Expression argumentSin) {
        return argumentCos.add(argumentSin).sin().sub(argumentCos.sub(argumentSin).sin()).div(2);
    }

    @Override
    public Expression simplifyExpandProductsOfComplexExponentialFunctions(String var) throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyExpandProductsOfComplexExponentialFunctions(var));
            }
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionen durch ihre Definitionen ersetzen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyExpandProductsOfComplexExponentialFunctions(var));
            }

            Expression expr = SimplifyUtilities.produceProduct(factors);
            BigInteger numberOfSummands = getUpperBoundForSummands(expr, var);

            // Im Folgenden Fall nicht weiter ausmultiplizieren.
            if (numberOfSummands.compareTo(BigInteger.ZERO) < 0
                    || numberOfSummands.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_NUMBER_OF_INTEGRABLE_SUMMANDS)) > 0) {
                return expr;
            }

            Expression factorLeft, factorRight;
            for (int i = 0; i < factors.getBound() - 1; i++) {

                if (factors.get(i) == null || !factors.get(i).contains(var)) {
                    continue;
                }
                factorLeft = factors.get(i);

                for (int j = i + 1; j < factors.getBound(); j++) {
                    if (factors.get(j) == null || !factors.get(j).contains(var)) {
                        continue;
                    }
                    factorRight = factors.get(j);

                    // Nun multiplikative Relationen anwenden.
                    if (factorLeft.isFunction(TypeFunction.cos)) {
                        if (factorRight.isFunction(TypeFunction.cos)) {
                            factors.put(i, rewriteProductOfCosCos(((Function) factorLeft).getLeft(), ((Function) factorRight).getLeft()));
                            factors.remove(j);
                        } else if (factorRight.isFunction(TypeFunction.sin)) {
                            factors.put(i, rewriteProductOfCosSin(((Function) factorLeft).getLeft(), ((Function) factorRight).getLeft()));
                            factors.remove(j);
                        }
                    } else if (factorLeft.isFunction(TypeFunction.sin)) {
                        if (factorRight.isFunction(TypeFunction.cos)) {
                            factors.put(i, rewriteProductOfSinCos(((Function) factorLeft).getLeft(), ((Function) factorRight).getLeft()));
                            factors.remove(j);
                        } else if (factorRight.isFunction(TypeFunction.sin)) {
                            factors.put(i, rewriteProductOfSinSin(((Function) factorLeft).getLeft(), ((Function) factorRight).getLeft()));
                            factors.remove(j);
                        }
                    }

                }
            }

            return SimplifyUtilities.produceProduct(factors);

        } else if (this.isPower()) {

            Expression expr = this.left.simplifyExpandProductsOfComplexExponentialFunctions(var).pow(this.right.simplifyExpandProductsOfComplexExponentialFunctions(var));

            if (expr.isPower() && ((BinaryOperation) expr).right.isIntegerConstant()
                    && ((BinaryOperation) expr).right.isPositive()
                    && ((Constant) ((BinaryOperation) expr).right).getValue().compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0
                    && isPolynomialInVariousExponentialAndTrigonometricalFunctions(((BinaryOperation) expr).left, var)) {

                BigInteger numberOfSummands = getUpperBoundForSummands(this, var);
                if (numberOfSummands.compareTo(BigInteger.ZERO) > 0
                        && numberOfSummands.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_OPERATOR_MAX_NUMBER_OF_INTEGRABLE_SUMMANDS)) <= 0) {

                    Expression base = ((BinaryOperation) expr).left;
                    int exponent = ((Constant) ((BinaryOperation) expr).right).getValue().intValue();

                    /* 
                     Falls base eine echte Summe / Differenz ist, dann expand() anwenden.
                     Falls base eine Sinus oder Kosinusfunktion ist, und das Argument var
                     enthält, dann wird gemäß bestimmter Relationen entwickelt.
                     */
                    if (base.isFunction(TypeFunction.cos) && ((Function) base).getLeft().contains(var)) {
                        return expandPowerOfCos(((Function) base).getLeft(), exponent);
                    }
                    if (base.isFunction(TypeFunction.sin) && ((Function) base).getLeft().contains(var)) {
                        return expandPowerOfSin(((Function) base).getLeft(), exponent);
                    }
                    // "Langes" Ausmultiplizieren soll stattfinden (Boolscher Parameter = true).
                    return this.simplifyExpand(TypeExpansion.POWERFUL);

                }
            }

            return expr;

        }

        return new BinaryOperation(this.left.simplifyExpandProductsOfComplexExponentialFunctions(var),
                this.right.simplifyExpandProductsOfComplexExponentialFunctions(var),
                this.type);

    }

    @Override
    public Expression simplifyAlgebraicExpressions() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        FlowController.interruptComputationIfNeeded();

        BinaryOperation expr;

        if (this.isSum()) {

            // In jedem Summanden einzeln algebraische Umformungen vornehmen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyAlgebraicExpressions());
            }
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            // In jedem Faktor einzeln algebraische Umformungen vornehmen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyAlgebraicExpressions());
            }

            Expression productOfAlgebraicallySimplifiedFactors = SimplifyUtilities.produceProduct(factors);

            if (!(productOfAlgebraicallySimplifiedFactors instanceof BinaryOperation)) {
                /*
                 Dies kann z. B. passieren, wenn factors aus 1 und exp(x)
                 besteht. SimplifyMethods.produceProduct(factors) liefert dann
                 exp(x), was KEINE Instanz von BinaryOperation ist.
                 */
                return productOfAlgebraicallySimplifiedFactors;
            }

            // Im Folgenden ist expr ein Produkt aus mindestens zwei Faktoren.
            expr = (BinaryOperation) productOfAlgebraicallySimplifiedFactors;
            if (!expr.equals(this)) {
                return expr;
            }

        } else {

            expr = new BinaryOperation(this.left.simplifyAlgebraicExpressions(), this.right.simplifyAlgebraicExpressions(), this.type);
            if (!expr.equals(this)) {
                return expr;
            }

        }

        Expression exprSimplified;

        /*
         Falls möglich, gewisse Binome vereinfachen (etwa (2^(1/2)+1)^2 =
         3+2*2^(1/2)). Maximal erlaubte Potenz ist <= einer bestimmten
         Schranke.
         */
        exprSimplified = SimplifyAlgebraicExpressionMethods.expandAlgebraicExpressionsByBinomial(expr, ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_BINOMIAL);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Faktorisiert ganze Faktoren aus Radikalen mit ganzzahligem Radikanden
        exprSimplified = SimplifyAlgebraicExpressionMethods.factorizeIntegerFactorsFromIntegerRoots(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Faktorisiert rationale Faktoren aus Radikalen mit rationalem Radikanden
        exprSimplified = SimplifyAlgebraicExpressionMethods.factorizeRationalFactorsFromRationalRoots(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }
        
        // Sammelt mehrere Wurzeln zu einer zusammen.
        exprSimplified = SimplifyAlgebraicExpressionMethods.collectVariousRootsToOneCommonRoot(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Wendet sinnvoll Rationalisierung des Nenners an.
        exprSimplified = SimplifyAlgebraicExpressionMethods.makeFactorsInDenominatorRational(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        /*
         Wendet sinnvoll die 3. binomische Formel an, etwa falls als Faktoren
         der Form x + 3*y^(5/2) und x - 3*y^(5/2) auftauchen (-> neuer Faktor
         ist x^2 - 9*y^5).
         */
        exprSimplified = SimplifyAlgebraicExpressionMethods.collectFactorsByThirdBinomialFormula(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        return expr;

    }

}
