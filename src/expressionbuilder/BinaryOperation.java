package expressionbuilder;

import computation.ArithmeticMethods;
import computationbounds.ComputationBounds;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyAlgebraicExpressionMethods;
import expressionsimplifymethods.SimplifyBinaryOperationMethods;
import expressionsimplifymethods.SimplifyExpLog;
import expressionsimplifymethods.SimplifyFunctionMethods;
import expressionsimplifymethods.SimplifyFunctionalRelations;
import expressionsimplifymethods.SimplifyPolynomialMethods;
import expressionsimplifymethods.SimplifyUtilities;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import translator.Translator;

public class BinaryOperation extends Expression {

    private final Expression left, right;
    private final TypeBinary type;

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
                if (valueLeft < 0 && this.right.isRationalConstant() && ((BinaryOperation) this.right).getRight().isOddConstant()) {
                    double result;
                    if (((BinaryOperation) this.right).getLeft().isOddConstant()) {
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
    public Expression evaluate(HashSet<String> vars) throws EvaluationException {
        return new BinaryOperation(this.left.evaluate(vars), this.right.evaluate(vars), this.type);
    }

    @Override
    public void getContainedVars(HashSet<String> vars) {
        this.left.getContainedVars(vars);
        this.right.getContainedVars(vars);
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
    public boolean containsIndefiniteIntegral() {
        return this.left.containsIndefiniteIntegral() || this.right.containsIndefiniteIntegral();
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
            return this.left.writeExpression() + "+" + this.right.writeExpression();
        } else if (this.isDifference()) {

            leftAsText = this.left.writeExpression();

            //0 - a soll als -a ausgegeben werden.
            if (this.left.equals(Expression.ZERO)) {
                leftAsText = "";
            }

            if (this.right.isSum() || this.right.isDifference()) {
                return leftAsText + "-(" + this.right.writeExpression() + ")";
            }
            return leftAsText + "-" + this.right.writeExpression();

        } else if (this.isProduct()) {

            if (this.left.equals(Expression.MINUS_ONE)) {
                /*
                 (-1)*x soll als -x ausgegeben werden, falls links davor eine
                 Klammer steht oder die Formel dort anfängt.
                 */
                return "-" + this.right.writeExpression();
            } else if (this.left.isSum() || this.left.isDifference()) {
                leftAsText = "(" + this.left.writeExpression() + ")";
            } else {
                leftAsText = this.left.writeExpression();
            }

            if (this.right.doesExpressionStartsWithAMinusSign() || this.right.isSum() || this.right.isDifference()) {
                rightAsText = "(" + this.right.writeExpression() + ")";
            } else {
                rightAsText = this.right.writeExpression();
            }

            return leftAsText + "*" + rightAsText;

        } else if (this.isQuotient()) {

            if (this.left instanceof BinaryOperation
                    && !this.left.isQuotient() && !this.left.isPower()) {
                leftAsText = "(" + this.left.writeExpression() + ")";
            } else {
                leftAsText = this.left.writeExpression();
            }

            if (this.right.doesExpressionStartsWithAMinusSign()
                    || (this.right instanceof BinaryOperation && !this.right.isPower())) {
                rightAsText = "(" + this.right.writeExpression() + ")";
            } else {
                rightAsText = this.right.writeExpression();
            }

            return leftAsText + "/" + rightAsText;

        }

        /**
         * Hier handelt es sich um eine Potenz.
         */
        if (this.left instanceof BinaryOperation) {
            leftAsText = "(" + this.left.writeExpression() + ")";
        } else {
            leftAsText = this.left.writeExpression();
        }

        if (this.right instanceof BinaryOperation
                || (this.right instanceof Constant && this.right.isNonPositive() && !this.right.equals(Expression.ZERO))) {
            rightAsText = "(" + this.right.writeExpression() + ")";
        } else {
            rightAsText = this.right.writeExpression();
        }

        return leftAsText + "^" + rightAsText;

    }

    @Override
    public String expressionToLatex(boolean beginning) {

        String leftAsLatexCode, rightAsLatexCode;

        if (this.type.equals(TypeBinary.PLUS)) {
            return this.left.expressionToLatex(false) + "+" + this.right.expressionToLatex(false);
        } else if (this.type.equals(TypeBinary.MINUS)) {

            leftAsLatexCode = this.left.writeExpression();

            //0 - a soll als -a ausgegeben werden.
            if (this.left.equals(Expression.ZERO)) {
                leftAsLatexCode = "";
            }

            if (this.right.isSum() || this.right.isDifference()) {
                return leftAsLatexCode + "-\\left(" + this.right.expressionToLatex(true) + "\\right)";
            }
            return leftAsLatexCode + "-" + this.right.expressionToLatex(false);

        } else if (this.type.equals(TypeBinary.TIMES)) {

            if (this.left.isSum() || this.left.isDifference()) {
                leftAsLatexCode = "\\left(" + this.left.expressionToLatex(true) + "\\right)";
            } else {
                leftAsLatexCode = this.left.expressionToLatex(false);
            }

            if (this.right.isSum() || this.right.isDifference()) {
                rightAsLatexCode = "\\left(" + this.right.expressionToLatex(true) + "\\right)";
            } else {
                rightAsLatexCode = this.right.expressionToLatex(false);
            }

            return leftAsLatexCode + " \\cdot " + rightAsLatexCode;

        } else if (this.type.equals(TypeBinary.DIV)) {

            return "\\frac{" + this.left.expressionToLatex(true) + "}{" + this.right.expressionToLatex(true) + "}";

        } else {

            if (this.left instanceof BinaryOperation) {
                if (this.left.isDifference() && ((BinaryOperation) this.left).getLeft().equals(Expression.ZERO)) {
                    leftAsLatexCode = this.left.expressionToLatex(true);
                } else {
                    leftAsLatexCode = "\\left(" + this.left.expressionToLatex(true) + "\\right)";
                }
            } else {
                leftAsLatexCode = this.left.expressionToLatex(false);
            }

            if (this.left instanceof Variable) {

                if (this.right instanceof Variable && (this.right.writeExpression().length() == 1)) {
                    return leftAsLatexCode + "^" + this.right.expressionToLatex(true);
                }
                return leftAsLatexCode + "^{" + this.right.expressionToLatex(true) + "}";

            } else if (this.left instanceof Constant) {

                if (((Constant) this.left).getPreciseValue().compareTo(BigDecimal.ZERO) >= 0) {
                    if ((this.right instanceof Variable) && (this.right.writeExpression().length() == 1)) {
                        return leftAsLatexCode + "^" + this.right.expressionToLatex(true);
                    }
                    return leftAsLatexCode + "^{" + this.right.expressionToLatex(true) + "}";
                }

            } else {

                if ((this.right instanceof Variable) && (this.right.writeExpression().length() == 1)) {
                    return "{" + leftAsLatexCode + "}^" + this.right.expressionToLatex(true);
                }

            }

            return "{" + leftAsLatexCode + "}^{" + this.right.expressionToLatex(true) + "}";

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
            if (this.right.isEvenConstant()) {
                return true;
            }
            if (this.right.isRationalConstant() && ((BinaryOperation) this.right).getLeft().isEvenConstant()) {
                return true;
            }
            if (this.right.isRationalConstant() && ((BinaryOperation) this.right).getLeft().isOddConstant()
                    && ((BinaryOperation) this.right).getRight().isOddConstant()) {
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
            return this.left.isAlwaysNonNegative() || this.right.isEvenConstant();
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
                                summandsRightOfThisWithSign.add(MINUS_ONE.mult(summandsLeftOfThis.get(i)).simplify());
                            }
                        }
                        for (int i = 0; i < summandsRightOfThis.getBound(); i++) {
                            if (summandsRightOfThis.get(i).hasPositiveSign()) {
                                summandsRightOfThisWithSign.add(summandsRightOfThis.get(i));
                            } else {
                                summandsLeftOfThisWithSign.add(MINUS_ONE.mult(summandsRightOfThis.get(i)).simplify());
                            }
                        }
                        for (int i = 0; i < summandsLeftOfExpr.getBound(); i++) {
                            if (summandsLeftOfExpr.get(i).hasPositiveSign()) {
                                summandsLeftOfExprWithSign.add(summandsLeftOfExpr.get(i));
                            } else {
                                summandsRightOfExprWithSign.add(MINUS_ONE.mult(summandsLeftOfExpr.get(i)).simplify());
                            }
                        }
                        for (int i = 0; i < summandsRightOfExpr.getBound(); i++) {
                            if (summandsRightOfExpr.get(i).hasPositiveSign()) {
                                summandsRightOfExprWithSign.add(summandsRightOfExpr.get(i));
                            } else {
                                summandsLeftOfExprWithSign.add(MINUS_ONE.mult(summandsRightOfExpr.get(i)).simplify());
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
    public Expression simplifyTrivial() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
        }

        BinaryOperation expr = this;

        Expression exprSimplified;

        /*
         Hier wird das folgende kritische Problem aus dem Weg geschafft: Wird
         (-2)^(1/3) approximiert, so wird der Exponent zu 0.33333333333
         approximiert und dementsprechend kann das Ergebnis nicht berechnet
         werden. Daher, wenn expr eine ungerade Wurzeln darstellt: negatives
         Vorzeichen rausschaffen!
         */
        exprSimplified = SimplifyBinaryOperationMethods.computeOddRootOfNegativeConstantsInApprox(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Linken und rechten Teil bei Binäroperationen zunächst separat vereinfachen
        if (this instanceof BinaryOperation) {
            expr = new BinaryOperation(this.left.simplifyTrivial(), this.right.simplifyTrivial(), this.type);
        }

        // Division durch 0 im Approximationsmodus ausschließen, sonst dividieren.
        exprSimplified = SimplifyBinaryOperationMethods.computeReciprocalInApprox(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Im Approximationsmodus werden alle Konstanten (im Zähler, Nenner etc. approximiert).
        exprSimplified = SimplifyBinaryOperationMethods.approxConstantExpression(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Rationale Konstanten zu einem Bruch machen (etwa 0.74/0.2 = 37/10)
        exprSimplified = SimplifyBinaryOperationMethods.rationalConstantToQuotient(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Multipliziert zwei rationale Konstanten aus
        exprSimplified = SimplifyBinaryOperationMethods.multiplyRationalConstants(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Negative Zähler eliminieren.
        exprSimplified = SimplifyBinaryOperationMethods.eliminateNegativeDenominator(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        /*
         Im exakten Modus werden alle Konstanten soweit verarbeitet, solange
         man sich im rationalen Bereich bewegt.
         */
        if (expr.isConstant() && !expr.containsApproximates()) {

            // Brüche subtrahieren.
            exprSimplified = SimplifyBinaryOperationMethods.subtractRationalFractions(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Nun folgen Vereinfachungen von Potenzen und Wurzeln konstanter Ausdrücke, soweit möglich.
            exprSimplified = SimplifyBinaryOperationMethods.computePowersOfIntegers(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Prüfen, ob Wurzeln gerader Ordnung aus negativen Konstanten gezogen werden.
            exprSimplified = SimplifyBinaryOperationMethods.checkNegativityOfBaseInRootsOfEvenDegree(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Minus-Zeichen aus ungeraden Wurzeln herausziehen.
            exprSimplified = SimplifyBinaryOperationMethods.takeMinusSignOutOfRoots(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

            // Macht z.B. (5/7)^(4/3) = (5/7)*(5/7)^(1/3) = 5*(5/7)^(1/3)/7
            exprSimplified = SimplifyBinaryOperationMethods.separateIntegerPowersOfRationalConstants(expr);
            if (!exprSimplified.equals(expr)) {
                return exprSimplified;
            }

        }

        // Versuchen, Wurzeln (z.B. in Quotienten oder von ganzen Zahlen) zum Teil exakt anzugeben.
        exprSimplified = SimplifyBinaryOperationMethods.tryTakePartialRootsPrecisely(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Vereinfache Potenzen von Quotienten, falls im Zähler oder im Nenner ganze Zahlen auftauchen.
        exprSimplified = SimplifyBinaryOperationMethods.simplifyPowerOfQuotient(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Triviale Umformungen
        exprSimplified = SimplifyBinaryOperationMethods.trivialOperationsWithZeroOne(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Zunächst: (a/b)^(-k) = (b/a)^k
        exprSimplified = SimplifyBinaryOperationMethods.negativePowersOfQuotientsToReciprocal(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        /*
         Negative Potenzen in den Nenner: v1^(c1) = 1 / v1^(-c1), falls c1 <
         0. Hier kann aufgrund des obigen Schrittes expr.getLeft() KEIN
         Quotient mehr sein.
         */
        exprSimplified = SimplifyBinaryOperationMethods.negativePowersOfExpressionsToReciprocal(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Vereinfacht Folgendes (1/x)^y = 1/x^y.
        exprSimplified = SimplifyBinaryOperationMethods.simplifyPowersOfReciprocals(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        /*
         Bei Addition oder Subtraktion: Falls negative Koeffizienten
         auftauchen, dann Addition zu Subtraktion machen und umgekehrt.
         */
        exprSimplified = SimplifyBinaryOperationMethods.simplifySumsAndDifferencesWithNegativeCoefficient(expr);
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

        return expr;

    }

    @Override
    public Expression expandRationalFactors() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln ausmultiplizieren.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).expandRationalFactors());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isDifference() || this.isPower()) {
            return new BinaryOperation(this.left.expandRationalFactors(), this.right.expandRationalFactors(), this.type);
        }

        BinaryOperation expr;
        if (this.isProduct()) {
            // In jedem Faktor einzeln ausmultiplizieren.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).expandRationalFactors());
            }
            expr = (BinaryOperation) SimplifyUtilities.produceProduct(factors);
        } else {
            if (this.right instanceof Constant) {
                expr = (BinaryOperation) this.left.expandRationalFactors().div(this.right);
            } else {
                expr = (BinaryOperation) this.left.expandRationalFactors().div(this.right.expandRationalFactors());
            }
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
    public Expression expand() throws EvaluationException {

        BinaryOperation expr;
        if (this.isSum() || this.isDifference()) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);
            // In jedem Summanden einzeln ausmultiplizieren.
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, summandsLeft.get(i).expand());
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, summandsRight.get(i).expand());
            }
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (this.isQuotient() || this.isPower()) {

            expr = new BinaryOperation(this.left.expand(), this.right.expand(), this.type);
            if (!expr.equals(this)) {
                // In späteren Anwendungen von expand() kann noch weiter ausmultipliziert werden.
                return expr;
            }

        } else {

            // In jedem Faktor einzeln ausmultiplizieren.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).expand());
            }

            Expression productOfExpandedFactors = SimplifyUtilities.produceProduct(factors);

            if (!(productOfExpandedFactors instanceof BinaryOperation)) {
                /*
                 Dies kann z. B. passieren, wenn factors aus exp(x) und 1
                 besteht. SimplifyMethods.produceProduct(factors) liefert dann
                 exp(x), was KEINE Instanz von BinaryOperation ist.
                 */
                return productOfExpandedFactors;
            }

            // Dann ist productOfExpandedFactors eine Instanz von BinaryOperation.
            expr = (BinaryOperation) productOfExpandedFactors;

        }

        if (expr.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(expr);
            int smallestIndexOfFactorWhichIsEitherSumOrDifference = 0;
            while (smallestIndexOfFactorWhichIsEitherSumOrDifference < factors.getBound()
                    && factors.get(smallestIndexOfFactorWhichIsEitherSumOrDifference).isNotSum()
                    && factors.get(smallestIndexOfFactorWhichIsEitherSumOrDifference).isNotDifference()) {
                smallestIndexOfFactorWhichIsEitherSumOrDifference++;
            }

            // Dann wurde keine Summe oder Differenz im Produkt gefunden.
            if (smallestIndexOfFactorWhichIsEitherSumOrDifference == factors.getBound()) {
                return expr;
            }

            Expression currentFactor = factors.get(smallestIndexOfFactorWhichIsEitherSumOrDifference);
            factors.remove(smallestIndexOfFactorWhichIsEitherSumOrDifference);
            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(currentFactor);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(currentFactor);

            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, SimplifyUtilities.produceProduct(
                        ExpressionCollection.copy(factors, 0, smallestIndexOfFactorWhichIsEitherSumOrDifference)).mult(
                                summandsLeft.get(i)).mult(
                                SimplifyUtilities.produceProduct(ExpressionCollection.copy(factors, smallestIndexOfFactorWhichIsEitherSumOrDifference + 1, factors.getBound() + 1))));
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, SimplifyUtilities.produceProduct(
                        ExpressionCollection.copy(factors, 0, smallestIndexOfFactorWhichIsEitherSumOrDifference)).mult(
                                summandsRight.get(i)).mult(
                                SimplifyUtilities.produceProduct(ExpressionCollection.copy(factors, smallestIndexOfFactorWhichIsEitherSumOrDifference + 1, factors.getBound() + 1))));
            }
            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        } else if (expr.isQuotient()) {

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getLeft());
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getLeft());
            if (summandsLeft.getBound() + summandsRight.getBound() == 1) {
                return expr;
            }

            // Jeden Summanden einzeln durch den Nenner dividieren.
            for (int i = 0; i < summandsLeft.getBound(); i++) {
                summandsLeft.put(i, summandsLeft.get(i).div(expr.getRight()));
            }
            for (int i = 0; i < summandsRight.getBound(); i++) {
                summandsRight.put(i, summandsRight.get(i).div(expr.getRight()));
            }

            return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

        }

        if (expr.isPower() && expr.getRight().isIntegerConstant() && expr.getRight().isNonNegative()) {

            if (expr.getLeft().isSum()) {

                ExpressionCollection summands = SimplifyUtilities.getSummands(expr.getLeft());

                BigInteger exponent = ((Constant) expr.getRight()).getPreciseValue().toBigInteger();
                BigInteger numberOfSummandsInBase = BigInteger.valueOf(summands.getBound());

                if (numberOfSummandsInBase.subtract(BigInteger.ONE).add(exponent).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                    return expr;
                }

                // Anzahl der Summanden in (a_1 + ... + a_n)^k ist (n - 1 + k)!/[(n - 1)! * k!]
                BigInteger numberOfSummandsInResult = ArithmeticMethods.factorial(numberOfSummandsInBase.subtract(BigInteger.ONE).add(exponent).intValue()).divide(ArithmeticMethods.factorial(numberOfSummandsInBase.intValue() - 1).multiply(ArithmeticMethods.factorial(exponent.intValue())));

                /*
                 Falls die (geschätzte) Anzahl der resultierenden Summanden >=
                 einer bestimmten Schranke beträgt, dann nicht
                 ausmultiplizieren.
                 */
                if (numberOfSummandsInResult.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_NUMBER_OF_SUMMANDS_IN_EXPANSION)) > 0) {
                    return expr;
                }

                return SimplifyAlgebraicExpressionMethods.binomialExpansion((BinaryOperation) expr.getLeft(), exponent.intValue());

            } else if (expr.getLeft().isDifference()) {

                ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr.getLeft());
                ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr.getLeft());

                BigInteger exponent = ((Constant) expr.getRight()).getPreciseValue().toBigInteger();
                BigInteger numberOfSummandsInBase = BigInteger.valueOf(summandsLeft.getBound() + summandsRight.getBound());

                if (numberOfSummandsInBase.subtract(BigInteger.ONE).add(exponent).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                    return expr;
                }

                BigInteger numberOfSummandsInResult = ArithmeticMethods.factorial(numberOfSummandsInBase.subtract(BigInteger.ONE).add(exponent).intValue()).divide(
                        ArithmeticMethods.factorial(numberOfSummandsInBase.intValue() - 1).multiply(ArithmeticMethods.factorial(exponent.intValue())));

                /*
                 Falls die (geschätzte) Anzahl der resultierenden Summanden >=
                 einer bestimmten Schranke beträgt, dann nicht
                 ausmultiplizieren.
                 */
                if (numberOfSummandsInResult.compareTo(BigInteger.valueOf(ComputationBounds.BOUND_NUMBER_OF_SUMMANDS_IN_EXPANSION)) > 0) {
                    return expr;
                }

                return SimplifyAlgebraicExpressionMethods.binomialExpansion((BinaryOperation) expr.getLeft(), exponent.intValue());

            }

        }

        return expr;

    }

    @Override
    public Expression reduceLeadingsCoefficients() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
        }

        if (this.isSum()) {
            // In jedem Summanden einzeln kürzen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).reduceLeadingsCoefficients());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isProduct()) {
            // In jedem Faktor einzeln kürzen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).reduceLeadingsCoefficients());
            }
            return SimplifyUtilities.produceProduct(factors);
        } else if (this.isPower()) {
            return this.left.reduceLeadingsCoefficients().pow(this.right.reduceLeadingsCoefficients());
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
            termsLeft.put(i, termsLeft.get(i).reduceLeadingsCoefficients());
        }
        for (int i = 0; i < termsRight.getBound(); i++) {
            termsRight.put(i, termsRight.get(i).reduceLeadingsCoefficients());
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
             Prüft, ob sich ganze Ausdrücke zu einer Konstanten kürzen lassen,
             etwa (5*a + 7*b)/(15*a + 21*b) = 1/3, (x - 3*y)/(12*y - 4*x) =
             -1/4 etc.
             */
            SimplifyBinaryOperationMethods.reduceFactorsInEnumeratorAndFactorInDenominatorToConstant(termsLeft, termsRight);

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
                return SimplifyUtilities.produceQuotient(termsLeft, termsRight).orderDifferenceAndDivision().reduceQuotients();
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
            summands = SimplifyUtilities.collectConstantsAndConstantExpressionsInSum(summands);

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
            factors = SimplifyUtilities.collectConstantsAndConstantExpressionsInProduct(factors);

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
    public Expression orderDifferenceAndDivision() throws EvaluationException {

        Expression result;

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
        }

        ExpressionCollection termsLeft = new ExpressionCollection();
        ExpressionCollection termsRight = new ExpressionCollection();

        if (this.isSum() || this.isDifference()) {

            SimplifyUtilities.orderDifference(this, termsLeft, termsRight);
            for (int i = 0; i < termsLeft.getBound(); i++) {
                termsLeft.put(i, termsLeft.get(i).orderDifferenceAndDivision());
            }
            for (int i = 0; i < termsRight.getBound(); i++) {
                termsRight.put(i, termsRight.get(i).orderDifferenceAndDivision());
            }
            result = SimplifyUtilities.produceDifference(termsLeft, termsRight);

        } else if (this.isProduct() || this.isQuotient()) {

            SimplifyUtilities.orderQuotient(this, termsLeft, termsRight);
            for (int i = 0; i < termsLeft.getBound(); i++) {
                termsLeft.put(i, termsLeft.get(i).orderDifferenceAndDivision());
            }
            for (int i = 0; i < termsRight.getBound(); i++) {
                termsRight.put(i, termsRight.get(i).orderDifferenceAndDivision());
            }
            result = SimplifyUtilities.produceQuotient(termsLeft, termsRight);

        } else {
            // Hier ist expr.getType() == TypeBinary.POW.
            result = this.left.orderDifferenceAndDivision().pow(this.right.orderDifferenceAndDivision());
        }

        return result;

    }

    @Override
    public Expression collectProducts() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln Faktoren sammeln.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).collectProducts());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isDifference() || this.isQuotient() || this.isPower()) {
            // Im linken und rechten Teil einzeln Faktoren sammeln.
            return new BinaryOperation(this.left.collectProducts(), this.right.collectProducts(), this.type);
        }

        ExpressionCollection factors = SimplifyUtilities.getFactors(this);

        //Ab hier ist type == *.
        // Zunächst in jedem Faktor einzeln Faktoren sammeln.
        for (int i = 0; i < factors.getBound(); i++) {
            factors.put(i, factors.get(i).collectProducts());
        }

        SimplifyUtilities.collectFactorsInProduct(factors);
        return SimplifyUtilities.produceProduct(factors);

    }

    @Override
    public Expression factorizeInSums() throws EvaluationException {

        if (this.isProduct()) {
            // In jedem Faktor einzeln faktorisieren.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).factorizeInSums());
            }
            return SimplifyUtilities.produceProduct(factors);
        }
        if (this.isNotSum()) {
            return new BinaryOperation(this.left.factorizeInSums(), this.right.factorizeInSums(), this.type);
        }

        // Ab hier muss this als type + besitzen.
        ExpressionCollection summands = SimplifyUtilities.getSummands(this);
        // In jedem Summanden einzeln faktorisieren
        for (int i = 0; i < summands.getBound(); i++) {
            summands.put(i, summands.get(i).factorizeInSums());
        }

        ExpressionCollection commonEnumerators, commonDenominators;
        ExpressionCollection leftSummandRestEnumerators, leftSummandRestDenominators,
                rightSummandRestEnumerators, rightSummandRestDenominators;
        Expression factorizedSummand;
        ExpressionCollection leftRestFactors, rightRestFactors;

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }

                leftRestFactors = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summands.get(i));
                rightRestFactors = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summands.get(j));
                commonEnumerators = SimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                leftSummandRestEnumerators = SimplifyUtilities.difference(leftRestFactors, commonEnumerators);
                rightSummandRestEnumerators = SimplifyUtilities.difference(rightRestFactors, commonEnumerators);

                leftRestFactors = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(i));
                rightRestFactors = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(j));
                commonDenominators = SimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                leftSummandRestDenominators = SimplifyUtilities.difference(leftRestFactors, commonDenominators);
                rightSummandRestDenominators = SimplifyUtilities.difference(rightRestFactors, commonDenominators);

                // Im Folgenden werden gemeinsame Faktoren, welche rationale Zahlen sind, NICHT faktorisiert!
                if (!commonEnumerators.isEmpty() && commonDenominators.isEmpty()) {

                    if (commonEnumerators.getBound() == 1 && commonEnumerators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceProduct(commonEnumerators).mult(
                            SimplifyUtilities.produceQuotient(leftSummandRestEnumerators, leftSummandRestDenominators).add(
                                    SimplifyUtilities.produceQuotient(rightSummandRestEnumerators, rightSummandRestDenominators)));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (commonEnumerators.isEmpty() && !commonDenominators.isEmpty()) {

                    if (commonDenominators.getBound() == 1 && commonDenominators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceQuotient(leftSummandRestEnumerators, leftSummandRestDenominators).add(
                            SimplifyUtilities.produceQuotient(rightSummandRestEnumerators, rightSummandRestDenominators)).div(SimplifyUtilities.produceProduct(commonDenominators));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!commonEnumerators.isEmpty() && !commonDenominators.isEmpty()) {

                    if (commonEnumerators.getBound() == 1 && commonEnumerators.get(0) instanceof Constant
                            && commonDenominators.getBound() == 1 && commonDenominators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceProduct(commonEnumerators).mult(
                            SimplifyUtilities.produceQuotient(leftSummandRestEnumerators, leftSummandRestDenominators).add(
                                    SimplifyUtilities.produceQuotient(rightSummandRestEnumerators, rightSummandRestDenominators))).div(SimplifyUtilities.produceProduct(commonDenominators));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                if (Thread.interrupted()) {
                    throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
                }

            }

        }

        // Ergebnis bilden.
        return SimplifyUtilities.produceSum(summands);

    }

    @Override
    public Expression factorizeInDifferences() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln kürzen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).factorizeInDifferences());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isProduct()) {
            // In jedem Faktor einzeln kürzen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).factorizeInDifferences());
            }
            return SimplifyUtilities.produceProduct(factors);
        } else if (this.isQuotient() || this.isPower()) {
            return new BinaryOperation(this.left.factorizeInDifferences(), this.right.factorizeInDifferences(), this.type);
        }

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);
        // In jedem Summanden einzeln faktorisieren
        for (int i = 0; i < summandsLeft.getBound(); i++) {
            summandsLeft.put(i, summandsLeft.get(i).factorizeInDifferences());
        }
        for (int i = 0; i < summandsRight.getBound(); i++) {
            summandsRight.put(i, summandsRight.get(i).factorizeInDifferences());
        }

        ExpressionCollection commonEnumerators, commonDenominators;
        ExpressionCollection leftSummandRestEnumerators, leftSummandRestDenominators,
                rightSummandRestEnumerators, rightSummandRestDenominators;
        Expression factorizedSummand;
        ExpressionCollection leftRestFactors, rightRestFactors;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }

                leftRestFactors = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsLeft.get(i));
                rightRestFactors = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsRight.get(j));
                commonEnumerators = SimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                leftSummandRestEnumerators = SimplifyUtilities.difference(leftRestFactors, commonEnumerators);
                rightSummandRestEnumerators = SimplifyUtilities.difference(rightRestFactors, commonEnumerators);

                leftRestFactors = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(i));
                rightRestFactors = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(j));
                commonDenominators = SimplifyUtilities.intersection(leftRestFactors, rightRestFactors);

                leftSummandRestDenominators = SimplifyUtilities.difference(leftRestFactors, commonDenominators);
                rightSummandRestDenominators = SimplifyUtilities.difference(rightRestFactors, commonDenominators);

                // Im Folgenden werden gemeinsame Faktoren, welche rationale Zahlen sind, NICHT faktorisiert!
                if (!commonEnumerators.isEmpty() && commonDenominators.isEmpty()) {

                    if (commonEnumerators.getBound() == 1 && commonEnumerators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceProduct(commonEnumerators).mult(
                            SimplifyUtilities.produceQuotient(leftSummandRestEnumerators, leftSummandRestDenominators).sub(
                                    SimplifyUtilities.produceQuotient(rightSummandRestEnumerators, rightSummandRestDenominators)));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (commonEnumerators.isEmpty() && !commonDenominators.isEmpty()) {

                    if (commonDenominators.getBound() == 1 && commonDenominators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceQuotient(leftSummandRestEnumerators, leftSummandRestDenominators).sub(
                            SimplifyUtilities.produceQuotient(rightSummandRestEnumerators, rightSummandRestDenominators)).div(
                                    SimplifyUtilities.produceProduct(commonDenominators));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!commonEnumerators.isEmpty() && !commonDenominators.isEmpty()) {

                    if (commonEnumerators.getBound() == 1 && commonEnumerators.get(0) instanceof Constant
                            && commonDenominators.getBound() == 1 && commonDenominators.get(0) instanceof Constant) {
                        continue;
                    }

                    factorizedSummand = SimplifyUtilities.produceProduct(commonEnumerators).mult(
                            SimplifyUtilities.produceQuotient(leftSummandRestEnumerators, leftSummandRestDenominators).sub(
                                    SimplifyUtilities.produceQuotient(rightSummandRestEnumerators, rightSummandRestDenominators))).div(
                                    SimplifyUtilities.produceProduct(commonDenominators));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                if (Thread.interrupted()) {
                    throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
                }

            }

        }

        // Ergebnis bilden.
        return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

    }

    @Override
    public Expression factorizeRationalsInSums() throws EvaluationException {

        if (this.isProduct()) {
            // In jedem Faktor einzeln faktorisieren.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).factorizeRationalsInSums());
            }
            return SimplifyUtilities.produceProduct(factors);
        }
        if (this.isNotSum()) {
            return new BinaryOperation(this.left.factorizeRationalsInSums(), this.right.factorizeRationalsInSums(), this.type);
        }

        // Ab hier muss this als type + besitzen.
        ExpressionCollection summands = SimplifyUtilities.getSummands(this);
        // In jedem Summanden einzeln faktorisieren
        for (int i = 0; i < summands.getBound(); i++) {
            summands.put(i, summands.get(i).factorizeRationalsInSums());
        }

        ExpressionCollection constantEnumeratorsLeft;
        ExpressionCollection nonConstantEnumeratorsLeft = new ExpressionCollection();
        ExpressionCollection constantDenominatorsLeft;
        ExpressionCollection nonConstantDenominatorsLeft = new ExpressionCollection();
        ExpressionCollection constantEnumeratorsRight;
        ExpressionCollection nonConstantEnumeratorsRight = new ExpressionCollection();
        ExpressionCollection constantDenominatorsRight;
        ExpressionCollection nonConstantDenominatorsRight = new ExpressionCollection();

        Expression factorizedSummand;

        for (int i = 0; i < summands.getBound(); i++) {

            if (summands.get(i) == null) {
                continue;
            }

            for (int j = i + 1; j < summands.getBound(); j++) {

                if (summands.get(j) == null) {
                    continue;
                }

                constantEnumeratorsLeft = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summands.get(i));
                constantEnumeratorsRight = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summands.get(j));
                constantDenominatorsLeft = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(i));
                constantDenominatorsRight = SimplifyUtilities.getFactorsOfDenominatorInExpression(summands.get(j));
                nonConstantEnumeratorsLeft.clear();
                nonConstantEnumeratorsRight.clear();
                nonConstantDenominatorsLeft.clear();
                nonConstantDenominatorsRight.clear();

                for (int k = 0; k < constantEnumeratorsLeft.getBound(); k++) {
                    if (!constantEnumeratorsLeft.get(k).isConstant()) {
                        nonConstantEnumeratorsLeft.add(constantEnumeratorsLeft.get(k));
                        constantEnumeratorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < constantEnumeratorsRight.getBound(); k++) {
                    if (!constantEnumeratorsRight.get(k).isConstant()) {
                        nonConstantEnumeratorsRight.add(constantEnumeratorsRight.get(k));
                        constantEnumeratorsRight.remove(k);
                    }
                }
                for (int k = 0; k < constantDenominatorsLeft.getBound(); k++) {
                    if (!constantDenominatorsLeft.get(k).isConstant()) {
                        nonConstantDenominatorsLeft.add(constantDenominatorsLeft.get(k));
                        constantDenominatorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < constantDenominatorsRight.getBound(); k++) {
                    if (!constantDenominatorsRight.get(k).isConstant()) {
                        nonConstantDenominatorsRight.add(constantDenominatorsRight.get(k));
                        constantEnumeratorsLeft.remove(k);
                        constantDenominatorsRight.remove(k);
                    }
                }

                // Falls die nichtkonstanten Faktoren NICHT übereinstimmen, nächster Schleifendurchgang.
                if (nonConstantEnumeratorsLeft.getBound() != nonConstantEnumeratorsRight.getBound()
                        || nonConstantDenominatorsLeft.getBound() != nonConstantDenominatorsRight.getBound()
                        || !SimplifyUtilities.difference(nonConstantEnumeratorsLeft, nonConstantEnumeratorsRight).isEmpty()
                        || !SimplifyUtilities.difference(nonConstantDenominatorsLeft, nonConstantDenominatorsRight).isEmpty()) {
                    continue;
                }

                if (!nonConstantEnumeratorsLeft.isEmpty() && nonConstantDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceProduct(nonConstantEnumeratorsLeft).mult(SimplifyUtilities.produceQuotient(constantEnumeratorsLeft, constantDenominatorsLeft).add(SimplifyUtilities.produceQuotient(constantEnumeratorsRight, constantDenominatorsRight)));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (nonConstantEnumeratorsLeft.isEmpty() && !nonConstantDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(constantEnumeratorsLeft, constantDenominatorsLeft).add(SimplifyUtilities.produceQuotient(constantEnumeratorsRight, constantDenominatorsRight)).div(SimplifyUtilities.produceProduct(nonConstantDenominatorsLeft));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!nonConstantEnumeratorsLeft.isEmpty() && !nonConstantDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceProduct(nonConstantEnumeratorsLeft).mult(SimplifyUtilities.produceQuotient(constantEnumeratorsLeft, constantDenominatorsLeft).add(SimplifyUtilities.produceQuotient(constantEnumeratorsRight, constantDenominatorsRight))).div(SimplifyUtilities.produceProduct(nonConstantDenominatorsLeft));
                    summands.put(i, factorizedSummand);
                    summands.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                if (Thread.interrupted()) {
                    throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
                }

            }

        }

        // Ergebnis bilden.
        return SimplifyUtilities.produceSum(summands);

    }

    @Override
    public Expression factorizeRationalsInDifferences() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln kürzen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).factorizeRationalsInDifferences());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isProduct()) {
            // In jedem Faktor einzeln kürzen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).factorizeRationalsInDifferences());
            }
            return SimplifyUtilities.produceProduct(factors);
        } else if (this.isQuotient() || this.isPower()) {
            return new BinaryOperation(this.left.factorizeRationalsInDifferences(), this.right.factorizeRationalsInDifferences(), this.type);
        }

        ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(this);
        ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(this);
        // In jedem Summanden einzeln faktorisieren
        for (int i = 0; i < summandsLeft.getBound(); i++) {
            summandsLeft.put(i, summandsLeft.get(i).factorizeRationalsInDifferences());
        }
        for (int i = 0; i < summandsRight.getBound(); i++) {
            summandsRight.put(i, summandsRight.get(i).factorizeRationalsInDifferences());
        }

        ExpressionCollection constantEnumeratorsLeft;
        ExpressionCollection nonConstantEnumeratorsLeft = new ExpressionCollection();
        ExpressionCollection constantDenominatorsLeft;
        ExpressionCollection nonConstantDenominatorsLeft = new ExpressionCollection();
        ExpressionCollection constantEnumeratorsRight;
        ExpressionCollection nonConstantEnumeratorsRight = new ExpressionCollection();
        ExpressionCollection constantDenominatorsRight;
        ExpressionCollection nonConstantDenominatorsRight = new ExpressionCollection();

        Expression factorizedSummand;

        for (int i = 0; i < summandsLeft.getBound(); i++) {

            if (summandsLeft.get(i) == null) {
                continue;
            }

            for (int j = 0; j < summandsRight.getBound(); j++) {

                if (summandsRight.get(j) == null) {
                    continue;
                }

                constantEnumeratorsLeft = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsLeft.get(i));
                constantEnumeratorsRight = SimplifyUtilities.getFactorsOfEnumeratorInExpression(summandsRight.get(j));
                constantDenominatorsLeft = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsLeft.get(i));
                constantDenominatorsRight = SimplifyUtilities.getFactorsOfDenominatorInExpression(summandsRight.get(j));

                nonConstantEnumeratorsLeft.clear();
                nonConstantEnumeratorsRight.clear();
                nonConstantDenominatorsLeft.clear();
                nonConstantDenominatorsRight.clear();
                for (int k = 0; k < constantEnumeratorsLeft.getBound(); k++) {
                    if (!constantEnumeratorsLeft.get(k).isConstant()) {
                        nonConstantEnumeratorsLeft.add(constantEnumeratorsLeft.get(k));
                        constantEnumeratorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < constantEnumeratorsRight.getBound(); k++) {
                    if (!constantEnumeratorsRight.get(k).isConstant()) {
                        nonConstantEnumeratorsRight.add(constantEnumeratorsRight.get(k));
                        constantEnumeratorsRight.remove(k);
                    }
                }
                for (int k = 0; k < constantDenominatorsLeft.getBound(); k++) {
                    if (!constantDenominatorsLeft.get(k).isConstant()) {
                        nonConstantDenominatorsLeft.add(constantDenominatorsLeft.get(k));
                        constantDenominatorsLeft.remove(k);
                    }
                }
                for (int k = 0; k < constantDenominatorsRight.getBound(); k++) {
                    if (!constantDenominatorsRight.get(k).isConstant()) {
                        nonConstantDenominatorsRight.add(constantDenominatorsRight.get(k));
                        constantDenominatorsRight.remove(k);
                    }
                }

                // Falls die nichtkonstanten Faktoren NICHT übereinstimmen, nächster Schleifendurchgang.
                if (nonConstantEnumeratorsLeft.getBound() != nonConstantEnumeratorsRight.getBound()
                        || nonConstantDenominatorsLeft.getBound() != nonConstantDenominatorsRight.getBound()
                        || !SimplifyUtilities.difference(nonConstantEnumeratorsLeft, nonConstantEnumeratorsRight).isEmpty()
                        || !SimplifyUtilities.difference(nonConstantDenominatorsLeft, nonConstantDenominatorsRight).isEmpty()) {
                    continue;
                }

                if (!nonConstantEnumeratorsLeft.isEmpty() && nonConstantDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceProduct(nonConstantEnumeratorsLeft).mult(SimplifyUtilities.produceQuotient(constantEnumeratorsLeft, constantDenominatorsLeft).sub(SimplifyUtilities.produceQuotient(constantEnumeratorsRight, constantDenominatorsRight)));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;

                } else if (nonConstantEnumeratorsLeft.isEmpty() && !nonConstantDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceQuotient(constantEnumeratorsLeft, constantDenominatorsLeft).sub(SimplifyUtilities.produceQuotient(constantEnumeratorsRight, constantDenominatorsRight)).div(SimplifyUtilities.produceProduct(nonConstantDenominatorsLeft));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                } else if (!nonConstantEnumeratorsLeft.isEmpty() && !nonConstantDenominatorsLeft.isEmpty()) {

                    factorizedSummand = SimplifyUtilities.produceProduct(nonConstantEnumeratorsLeft).mult(SimplifyUtilities.produceQuotient(constantEnumeratorsLeft, constantDenominatorsLeft).sub(SimplifyUtilities.produceQuotient(constantEnumeratorsRight, constantDenominatorsRight))).div(SimplifyUtilities.produceProduct(nonConstantDenominatorsLeft));
                    summandsLeft.put(i, factorizedSummand);
                    summandsRight.remove(j);
                    // Zweite Schleife abbrechen und weiter fortfahren mit dem nächsten Summanden!
                    break;
                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                if (Thread.interrupted()) {
                    throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
                }

            }

        }

        // Ergebnis bilden.
        return SimplifyUtilities.produceDifference(summandsLeft, summandsRight);

    }

    @Override
    public Expression reduceQuotients() throws EvaluationException {

        if (this.isSum()) {
            // In jedem Summanden einzeln kürzen.
            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).reduceQuotients());
            }
            return SimplifyUtilities.produceSum(summands);
        } else if (this.isProduct()) {
            // In jedem Faktor einzeln kürzen.
            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).reduceQuotients());
            }
            return SimplifyUtilities.produceProduct(factors);
        } else if (this.isDifference() || this.isPower()) {
            return new BinaryOperation(this.left.reduceQuotients(), this.right.reduceQuotients(), this.type);
        }

        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(this);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(this);

        // In jedem Faktor einzeln kürzen
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            factorsEnumerator.put(i, factorsEnumerator.get(i).reduceQuotients());
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            factorsDenominator.put(i, factorsDenominator.get(i).reduceQuotients());
        }

        Expression base;
        Expression exponent;
        Expression compareBase;
        Expression compareExponent;

        for (int i = 0; i < factorsEnumerator.getBound(); i++) {

            if (factorsEnumerator.get(i) == null) {
                continue;
            }

            if (factorsEnumerator.get(i).isPower()) {
                base = ((BinaryOperation) factorsEnumerator.get(i)).getLeft();
                exponent = ((BinaryOperation) factorsEnumerator.get(i)).getRight();
            } else {
                base = factorsEnumerator.get(i);
                exponent = ONE;
            }

            for (int j = 0; j < factorsDenominator.getBound(); j++) {

                if (factorsDenominator.get(j) == null) {
                    continue;
                }

                if (factorsDenominator.get(j).isPower()) {
                    compareBase = ((BinaryOperation) factorsDenominator.get(j)).getLeft();
                    compareExponent = ((BinaryOperation) factorsDenominator.get(j)).getRight();
                } else {
                    compareBase = factorsDenominator.get(j);
                    compareExponent = ONE;
                }

                if (base.equivalent(compareBase)) {
                    exponent = exponent.sub(compareExponent);
                    factorsDenominator.remove(j);
                    break;
                }

                // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
                if (Thread.interrupted()) {
                    throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
                }

            }

            if (exponent.equals(ONE)) {
                factorsEnumerator.put(i, base);
            } else {
                factorsEnumerator.put(i, base.pow(exponent));
            }

        }

        // Ergebnis bilden.
        return SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator);

    }

    @Override
    public Expression simplifyPowers() throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Potenzen vereinfachen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyPowers());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        }

        if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Summanden einzeln Potenzen vereinfachen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyPowers());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        }

        if (this.isDifference() || this.isQuotient()) {
            return new BinaryOperation(this.left.simplifyPowers(), this.right.simplifyPowers(), this.type);
        }

        // Ab hier ist type == TypeBinary.POW
        Expression expr = this.left.simplifyPowers().pow(this.right.simplifyPowers());
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
    public Expression multiplyPowers() throws EvaluationException {

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Potenzen vereinfachen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).multiplyPowers());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Summanden einzeln Potenzen vereinfachen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).multiplyPowers());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        } else if (this.isDifference() || this.isQuotient()) {

            return new BinaryOperation(this.left.multiplyPowers(), this.right.multiplyPowers(), this.type);

        }

        // Hier ist this.type == TypeBinary.POW
        Expression leftSimplified = this.left.multiplyPowers();
        if (leftSimplified.isPower()) {
            return ((BinaryOperation) leftSimplified).getLeft().pow(((BinaryOperation) leftSimplified).getRight().mult(this.right));
        }
        return this;

    }

    @Override
    public Expression simplifyFunctionalRelations() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
        }

        BinaryOperation expr = this;
        Expression exprSimplified;

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionalgleichungen anwenden.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyFunctionalRelations());
            }

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
            expr = (BinaryOperation) this.left.simplifyFunctionalRelations().sub(this.right.simplifyFunctionalRelations());

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);

            SimplifyFunctionalRelations.reduceCoshMinusSinhToOne(summandsLeft, summandsRight);
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

        if (expr.isProduct()) {

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

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        }

        if (this.isQuotient()) {

            // Im Dividenden und Divisor einzeln Funktionalgleichungen anwenden.
            expr = (BinaryOperation) this.left.simplifyFunctionalRelations().div(this.right.simplifyFunctionalRelations());

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
            expr = (BinaryOperation) this.left.simplifyFunctionalRelations().pow(this.right.simplifyFunctionalRelations());

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
    public Expression simplifyPolynomials() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
        }

        Expression expr = this;
        Expression exprSimplified;

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Funktionalgleichungen anwenden.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyPolynomials());
            }

            expr = SimplifyUtilities.produceSum(summands);

            exprSimplified = SimplifyPolynomialMethods.simplifyPolynomial(expr);
            exprSimplified = SimplifyPolynomialMethods.simplifyMultiPolynomial(exprSimplified);

            // Ergebnis bilden.
            return exprSimplified;

        }

        if (this.isDifference()) {

            // Im Minuenden und Subtrahenden einzeln Funktionalgleichungen anwenden.
            expr = this.left.simplifyPolynomials().sub(this.right.simplifyPolynomials());

            exprSimplified = SimplifyPolynomialMethods.simplifyPolynomial(expr);
            exprSimplified = SimplifyPolynomialMethods.simplifyMultiPolynomial(exprSimplified);

            // Ergebnis bilden.
            return exprSimplified;

        }

        if (expr.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Funktionalgleichungen anwenden.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyPolynomials());
            }

            expr = SimplifyUtilities.produceProduct(factors);
            exprSimplified = SimplifyPolynomialMethods.simplifyPolynomial(expr);
            exprSimplified = SimplifyPolynomialMethods.simplifyMultiPolynomial(exprSimplified);

            // Ergebnis bilden.
            return exprSimplified;

        }

        if (this.isQuotient()) {

            // Im Dividenden und Divisor einzeln Funktionalgleichungen anwenden.
            expr = this.left.simplifyPolynomials().div(this.right.simplifyPolynomials());

            exprSimplified = SimplifyPolynomialMethods.simplifyPolynomial(expr);
            exprSimplified = SimplifyPolynomialMethods.simplifyMultiPolynomial(exprSimplified);

            // Ergebnis bilden.
            return exprSimplified;

        }

        if (this.isPower()) {

            // In Basis und Exponenten einzeln Funktionalgleichungen anwenden.
            expr = this.left.simplifyPolynomials().pow(this.right.simplifyPolynomials());

            exprSimplified = SimplifyPolynomialMethods.simplifyPolynomial(expr);
            exprSimplified = SimplifyPolynomialMethods.simplifyMultiPolynomial(exprSimplified);

            // Ergebnis bilden.
            return exprSimplified;

        }

        return expr;

    }

    @Override
    public Expression simplifyCollectLogarithms() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
        }

        ExpressionCollection summands = SimplifyUtilities.getSummands(this);
        // Faktoren vor Logarithmusfunktionen zur Basis 10 in die Logarithmen hineinziehen.
        SimplifyExpLog.pullFactorsIntoLogarithmicFunctions(summands, TypeFunction.lg);
        // Faktoren vor Logarithmusfunktionen zur Basis e in die Logarithmen hineinziehen.
        SimplifyExpLog.pullFactorsIntoLogarithmicFunctions(summands, TypeFunction.ln);
        Expression expr = SimplifyUtilities.produceSum(summands);

        if (expr.isSum()) {

            // In jedem Summanden einzeln Logarithmen sammeln.
            summands = SimplifyUtilities.getSummands(expr);
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyCollectLogarithms());
            }

            //Logarithmusfunktionen zur Basis 10 in einer Summe sammeln
            SimplifyExpLog.collectLogarithmicFunctionsInSum(summands, TypeFunction.lg);
            //Logarithmusfunktionen zur Basis e in einer Summe sammeln
            SimplifyExpLog.collectLogarithmicFunctionsInSum(summands, TypeFunction.ln);

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (expr.isDifference()) {

            // Im Minuenden und Subtrahenden einzeln Logarithmen sammeln.
            expr = ((BinaryOperation) expr).getLeft().simplifyCollectLogarithms().sub(((BinaryOperation) expr).getRight().simplifyCollectLogarithms());

            ExpressionCollection summandsLeft = SimplifyUtilities.getSummandsLeftInExpression(expr);
            ExpressionCollection summandsRight = SimplifyUtilities.getSummandsRightInExpression(expr);

            //Logarithmusfunktionen zur Basis 10 in einer Differenz sammeln
            SimplifyExpLog.collectLogarithmicFunctionsInDifference(summandsLeft, summandsRight, TypeFunction.lg);
            //Logarithmusfunktionen zur Basis e in einer Differenz sammeln
            SimplifyExpLog.collectLogarithmicFunctionsInDifference(summandsLeft, summandsRight, TypeFunction.ln);

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
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
        }

        if (this.isSum()) {

            ExpressionCollection summands = SimplifyUtilities.getSummands(this);
            // In jedem Summanden einzeln Logarithmen auseinanderziehen.
            for (int i = 0; i < summands.getBound(); i++) {
                summands.put(i, summands.get(i).simplifyExpandLogarithms());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceSum(summands);

        } else if (this.isProduct()) {

            ExpressionCollection factors = SimplifyUtilities.getFactors(this);
            // In jedem Faktor einzeln Logarithmen auseinanderziehen.
            for (int i = 0; i < factors.getBound(); i++) {
                factors.put(i, factors.get(i).simplifyExpandLogarithms());
            }

            // Ergebnis bilden.
            return SimplifyUtilities.produceProduct(factors);

        }

        return new BinaryOperation(this.left.simplifyExpandLogarithms(), this.right.simplifyExpandLogarithms(), this.type);

    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsByDefinitions() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
        }

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

        } else if (this.isPower()) {
            return new Function(new Function(this.left, TypeFunction.ln).mult(this.right), TypeFunction.exp);
        }

        return new BinaryOperation(this.left.simplifyReplaceExponentialFunctionsByDefinitions(),
                this.right.simplifyReplaceExponentialFunctionsByDefinitions(),
                this.type);

    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsByDefinitions() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
        }

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

        } else if (this.isPower()) {
            return new Function(new Function(this.left, TypeFunction.ln).mult(this.right), TypeFunction.exp);
        }

        return new BinaryOperation(this.left.simplifyReplaceTrigonometricalFunctionsByDefinitions(),
                this.right.simplifyReplaceTrigonometricalFunctionsByDefinitions(),
                this.type);

    }

    @Override
    public Expression simplifyAlgebraicExpressions() throws EvaluationException {

        // Zur Kontrolle, ob zwischendurch die Berechnung unterbrochen wurde.
        if (Thread.interrupted()) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_BinaryOperation_COMPUTATION_ABORTED"));
        }

        BinaryOperation expr = this;

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
        exprSimplified = SimplifyAlgebraicExpressionMethods.expandAlgebraicExpressionsByBinomial(expr, ComputationBounds.BOUND_POWER_OF_BINOMIAL);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Faktorisiert ganze Faktoren aus Radikalen mit ganzzahligem Radikanden
        exprSimplified = SimplifyAlgebraicExpressionMethods.factorizeIntegerFactorsFromIntegerRoots(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Sammelt mehrere Wurzeln zu einer zusammen.
        exprSimplified = SimplifyAlgebraicExpressionMethods.collectVariousRootsToOneCommonRoot(expr);
        if (!exprSimplified.equals(expr)) {
            return exprSimplified;
        }

        // Faktorisiert rationale Faktoren aus Radikalen mit rationalem Radikanden
        exprSimplified = SimplifyAlgebraicExpressionMethods.factorizeIntegerFactorsFromRationalRoots(expr);
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
