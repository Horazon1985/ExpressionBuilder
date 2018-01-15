package testrunner;

import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.logicalexpression.basic.LogicalExpressionCollection;
import abstractexpressions.logicalexpression.classes.LogicalExpression;
import abstractexpressions.matrixexpression.basic.MatrixExpressionCollection;
import abstractexpressions.matrixexpression.classes.MatrixExpression;

public class TestUtilities {

    public static void printResults(String className, String testMethodName, Object[] expectedResults, Object[] results) {
        System.out.println("--------------Begin of test----------------");
        System.out.println("Test class: " + className);
        System.out.println("Test: " + testMethodName);
        if (expectedResults == null && results == null) {
            return;
        } else if (expectedResults != null && results == null) {
            System.err.println("ExpectedResults is not null, but results is null!");
            return;
        } else if (expectedResults == null && results != null) {
            System.err.println("ExpectedResults is null, but results is not null!");
            return;
        }
        if (expectedResults.length != results.length) {
            System.err.println("Number of expected results does not coincide with the number of results!");
        } else {
            for (int i = 0; i < expectedResults.length; i++) {

                if (expectedResults[i] instanceof Expression && results[i] instanceof Expression) {
                    if (((Expression) expectedResults[i]).equivalent((Expression) results[i])) {
                        System.out.println("\033[32mExpected result:" + expectedResults[i]);
                    } else {
                        System.out.println("\033[31mExpected result: " + expectedResults[i]);
                    }
                } else if (expectedResults[i] instanceof LogicalExpression && results[i] instanceof LogicalExpression) {
                    if (((LogicalExpression) expectedResults[i]).equivalent((LogicalExpression) results[i])) {
                        System.out.println("\033[32mExpected result:" + expectedResults[i]);
                    } else {
                        System.out.println("\033[31mExpected result: " + expectedResults[i]);
                    }
                } else if (expectedResults[i] instanceof MatrixExpression && results[i] instanceof MatrixExpression) {
                    if (((MatrixExpression) expectedResults[i]).equivalent((MatrixExpression) results[i])) {
                        System.out.println("\033[32mExpected result:" + expectedResults[i]);
                    } else {
                        System.out.println("\033[31mExpected result: " + expectedResults[i]);
                    }
                } else if (expectedResults[i] instanceof ExpressionCollection && results[i] instanceof ExpressionCollection) {
                    if (((ExpressionCollection) expectedResults[i]).equals((ExpressionCollection) results[i])) {
                        System.out.println("\033[32mExpected result:" + expectedResults[i]);
                    } else {
                        System.out.println("\033[31mExpected result: " + expectedResults[i]);
                    }
                } else if (expectedResults[i] instanceof LogicalExpressionCollection && results[i] instanceof LogicalExpressionCollection) {
                    if (((LogicalExpressionCollection) expectedResults[i]).equals((LogicalExpressionCollection) results[i])) {
                        System.out.println("\033[32mExpected result:" + expectedResults[i]);
                    } else {
                        System.out.println("\033[31mExpected result: " + expectedResults[i]);
                    }
                } else if (expectedResults[i] instanceof MatrixExpressionCollection && results[i] instanceof MatrixExpressionCollection) {
                    if (((MatrixExpressionCollection) expectedResults[i]).equals((MatrixExpressionCollection) results[i])) {
                        System.out.println("\033[32mExpected result:" + expectedResults[i]);
                    } else {
                        System.out.println("\033[31mExpected result: " + expectedResults[i]);
                    }
                } else if (expectedResults[i].equals(results[i])) {
                    System.out.println("\033[32mExpected result:" + expectedResults[i]);
                } else {
                    System.out.println("\033[31mExpected result: " + expectedResults[i]);
                }

                System.out.println("Result: " + results[i].toString());
            }
        }
        System.out.println("--------------End of test------------------");
    }

}
