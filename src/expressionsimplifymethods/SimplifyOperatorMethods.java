package expressionsimplifymethods;

import expressionbuilder.BinaryOperation;
import expressionbuilder.Expression;
import expressionbuilder.Operator;
import expressionbuilder.TypeOperator;

public class SimplifyOperatorMethods {

    /**
     * Falls im Summenoperator eine Summe oder eine Differenz auftaucht, so wird
     * dieser in eine entsprechende Summe oder Differenz von Summenoperatoren
     * aufgeteilt.
     */
    public static Expression splitSumOfSumsOrDifferences(BinaryOperation expr, String var, Expression lowerLimit, Expression upperLimit) {

        if (expr.isDifference()) {

            Object[] paramsLeft = new Object[4];
            paramsLeft[0] = expr.getLeft();
            paramsLeft[1] = var;
            paramsLeft[2] = lowerLimit;
            paramsLeft[3] = upperLimit;
            Object[] paramsRight = new Object[4];
            paramsRight[0] = expr.getRight();
            paramsRight[1] = var;
            paramsRight[2] = lowerLimit;
            paramsRight[3] = upperLimit;
            return new Operator(TypeOperator.sum, paramsLeft).sub(new Operator(TypeOperator.sum, paramsRight));

        } else {

            ExpressionCollection summands = SimplifyUtilities.getSummands(expr);
            Object[][] params = new Object[summands.getBound()][4];
            for (int i = 0; i < summands.getBound(); i++) {
                for (int j = 0; j < 4; j++) {
                    params[i][0] = summands.get(i);
                    params[i][1] = var;
                    params[i][2] = lowerLimit;
                    params[i][3] = upperLimit;
                }
                summands.put(i, new Operator(TypeOperator.sum, params[i]));
            }

            return SimplifyUtilities.produceSum(summands);

        }

    }

    /**
     * Falls im Summenoperator konstante Faktoren (im Zähler oder Nenner)
     * auftauchen, so werden diese herausgezogen. VORAUSSETZUNG: expr hat type
     * == TypeBinary.TIMES oder type == TypeBinary.DIV.
     */
    public static Expression takeConstantsOutOfSums(BinaryOperation expr, String var, Expression lowerLimit, Expression upperLimit) {

        ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(expr);
        ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(expr);
        ExpressionCollection resultFactorsInEnumeratorOutsideOfSum = new ExpressionCollection();
        ExpressionCollection resultFactorsInDenominatorOutsideOfSum = new ExpressionCollection();
        ExpressionCollection resultFactorsInEnumeratorInSum = new ExpressionCollection();
        ExpressionCollection resultFactorsInDenominatorInSum = new ExpressionCollection();

        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (!factorsEnumerator.get(i).contains(var)) {
                resultFactorsInEnumeratorOutsideOfSum.add(factorsEnumerator.get(i));
                factorsEnumerator.remove(i);
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (!factorsDenominator.get(i).contains(var)) {
                resultFactorsInDenominatorOutsideOfSum.add(factorsDenominator.get(i));
                factorsDenominator.remove(i);
            }
        }
        for (int i = 0; i < factorsEnumerator.getBound(); i++) {
            if (factorsEnumerator.get(i) != null) {
                resultFactorsInEnumeratorInSum.add(factorsEnumerator.get(i));
            }
        }
        for (int i = 0; i < factorsDenominator.getBound(); i++) {
            if (factorsDenominator.get(i) != null) {
                resultFactorsInDenominatorInSum.add(factorsDenominator.get(i));
            }
        }

        Expression resultArgumentInSum;
        if (resultFactorsInEnumeratorInSum.isEmpty() && resultFactorsInDenominatorInSum.isEmpty()) {
            resultArgumentInSum = Expression.ONE;
        } else if (!resultFactorsInEnumeratorInSum.isEmpty() && resultFactorsInDenominatorInSum.isEmpty()) {
            resultArgumentInSum = SimplifyUtilities.produceProduct(resultFactorsInEnumeratorInSum);
        } else if (resultFactorsInEnumeratorInSum.isEmpty() && !resultFactorsInDenominatorInSum.isEmpty()) {
            resultArgumentInSum = Expression.ONE.div(SimplifyUtilities.produceProduct(resultFactorsInDenominatorInSum));
        } else {
            resultArgumentInSum = SimplifyUtilities.produceQuotient(resultFactorsInEnumeratorInSum, resultFactorsInDenominatorInSum);
        }

        Object[] params = new Object[4];
        params[0] = resultArgumentInSum;
        params[1] = var;
        params[2] = lowerLimit;
        params[3] = upperLimit;

        if (resultFactorsInEnumeratorOutsideOfSum.isEmpty() && resultFactorsInDenominatorOutsideOfSum.isEmpty()) {
            return new Operator(TypeOperator.sum, params);
        } else if (!resultFactorsInEnumeratorOutsideOfSum.isEmpty() && resultFactorsInDenominatorOutsideOfSum.isEmpty()) {
            return SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfSum).mult(new Operator(TypeOperator.sum, params));
        } else if (resultFactorsInEnumeratorOutsideOfSum.isEmpty() && !resultFactorsInDenominatorOutsideOfSum.isEmpty()) {
            return new Operator(TypeOperator.sum, params).div(SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfSum));
        }
        return SimplifyUtilities.produceProduct(resultFactorsInEnumeratorOutsideOfSum).mult(new Operator(TypeOperator.sum, params)).div(SimplifyUtilities.produceProduct(resultFactorsInDenominatorOutsideOfSum));

    }

    /**
     * Falls im Summenoperator eine Summe oder eine Differenz auftaucht, so wird
     * dieser in eine entsprechende Summe oder Differenz von Summenoperatoren
     * aufgeteilt. VORAUSSETZUNG: expr hat type == TypeBinary.TIMES oder type ==
     * TypeBinary.DIV.
     */
    public static Expression splitProductsOfProductsOrQuotients(BinaryOperation expr, String var, Expression lowerLimit, Expression upperLimit) {

        if (expr.isQuotient()) {

            Object[] paramsLeft = new Object[4];
            paramsLeft[0] = expr.getLeft();
            paramsLeft[1] = var;
            paramsLeft[2] = lowerLimit;
            paramsLeft[3] = upperLimit;
            Object[] paramsRight = new Object[4];
            paramsRight[0] = expr.getRight();
            paramsRight[1] = var;
            paramsRight[2] = lowerLimit;
            paramsRight[3] = upperLimit;
            return new Operator(TypeOperator.prod, paramsLeft).div(new Operator(TypeOperator.prod, paramsRight));

        } else {

            ExpressionCollection factors = SimplifyUtilities.getFactors(expr);
            Object[][] params = new Object[factors.getBound()][4];
            for (int i = 0; i < factors.getBound(); i++) {
                for (int j = 0; j < 4; j++) {
                    params[i][0] = factors.get(i);
                    params[i][1] = var;
                    params[i][2] = lowerLimit;
                    params[i][3] = upperLimit;
                }
                factors.put(i, new Operator(TypeOperator.prod, params[i]));
            }
            return SimplifyUtilities.produceProduct(factors);

        }

    }

    /**
     * Falls im Produktoperator konstante Exponenten auftauchen, so werden diese
     * herausgezogen. VORAUSSETZUNG: Exponent hat type == TypeBinary.TIMES oder
     * type == TypeBinary.DIV.
     */
    public static Expression takeConstantExponentsOutOfProducts(BinaryOperation expr, String var, Expression lowerLimit, Expression upperLimit) {

        if (expr.isNotPower()) {
            // Dann nichts tun!
            Object[] params = new Object[4];
            params[0] = expr;
            params[1] = var;
            params[2] = lowerLimit;
            params[3] = upperLimit;
            return new Operator(TypeOperator.prod, params);
        }

        ExpressionCollection exponentFactorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(expr.getRight());
        ExpressionCollection exponentFactorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(expr.getRight());
        Expression exponentEnumeratorOutsideOfProduct = Expression.ONE;
        Expression exponentDenominatorOutsideOfProduct = Expression.ONE;
        ExpressionCollection resultFactorsInExponentEnumerator = new ExpressionCollection();
        ExpressionCollection resultFactorsInExponentDenominator = new ExpressionCollection();

        if (exponentFactorsEnumerator.get(0) != null && !exponentFactorsEnumerator.get(0).contains(var) && exponentFactorsEnumerator.get(0).isIntegerConstant()) {
            exponentEnumeratorOutsideOfProduct = exponentFactorsEnumerator.get(0);
            exponentFactorsEnumerator.remove(0);
        }

        if (exponentFactorsDenominator.get(0) != null && !exponentFactorsDenominator.get(0).contains(var) && exponentFactorsDenominator.get(0).isOddConstant()) {
            exponentDenominatorOutsideOfProduct = exponentFactorsDenominator.get(0);
            exponentFactorsDenominator.remove(0);
        }

        for (int i = 0; i < exponentFactorsEnumerator.getBound(); i++) {
            if (exponentFactorsEnumerator.get(i) != null) {
                resultFactorsInExponentEnumerator.add(exponentFactorsEnumerator.get(i));
            }
        }
        for (int i = 0; i < exponentFactorsDenominator.getBound(); i++) {
            if (exponentFactorsDenominator.get(i) != null) {
                resultFactorsInExponentDenominator.add(exponentFactorsDenominator.get(i));
            }
        }

        Expression resultExponentInProduct;
        if (resultFactorsInExponentEnumerator.isEmpty() && resultFactorsInExponentDenominator.isEmpty()) {
            resultExponentInProduct = Expression.ONE;
        } else if (!resultFactorsInExponentEnumerator.isEmpty() && resultFactorsInExponentDenominator.isEmpty()) {
            resultExponentInProduct = SimplifyUtilities.produceProduct(resultFactorsInExponentEnumerator);
        } else if (resultFactorsInExponentEnumerator.isEmpty() && !resultFactorsInExponentDenominator.isEmpty()) {
            resultExponentInProduct = Expression.ONE.div(SimplifyUtilities.produceProduct(resultFactorsInExponentDenominator));
        } else {
            resultExponentInProduct = SimplifyUtilities.produceQuotient(resultFactorsInExponentEnumerator, resultFactorsInExponentDenominator);
        }

        Object[] params = new Object[4];
        params[0] = expr.getLeft().pow(resultExponentInProduct);
        params[1] = var;
        params[2] = lowerLimit;
        params[3] = upperLimit;

        if (exponentEnumeratorOutsideOfProduct.equals(Expression.ONE) && exponentDenominatorOutsideOfProduct.equals(Expression.ONE)) {
            return new Operator(TypeOperator.prod, params);
        } else if (!exponentEnumeratorOutsideOfProduct.equals(Expression.ONE) && exponentDenominatorOutsideOfProduct.equals(Expression.ONE)) {
            return new Operator(TypeOperator.prod, params).pow(exponentEnumeratorOutsideOfProduct);
        } else if (exponentEnumeratorOutsideOfProduct.equals(Expression.ONE) && !exponentDenominatorOutsideOfProduct.equals(Expression.ONE)) {
            return new Operator(TypeOperator.prod, params).pow(Expression.ONE.div(exponentDenominatorOutsideOfProduct));
        }
        return new Operator(TypeOperator.prod, params).pow(exponentEnumeratorOutsideOfProduct.div(exponentDenominatorOutsideOfProduct));

    }

    public static Expression simplifyProductWithConstantBase(BinaryOperation expr, String var, Expression lowerLimit, Expression upperLimit ){
    
        if (expr.isNotPower()) {
            // Dann nichts tun!
            Object[] params = new Object[4];
            params[0] = expr;
            params[1] = var;
            params[2] = lowerLimit;
            params[3] = upperLimit;
            return new Operator(TypeOperator.prod, params);
        }
        
        
        
        
        
        return null;
    
    }
    
    
    
}
