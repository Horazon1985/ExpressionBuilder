package utilities;

import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.logicalexpression.basic.LogicalExpressionCollection;
import abstractexpressions.logicalexpression.classes.LogicalExpression;
import abstractexpressions.matrixexpression.basic.MatrixExpressionCollection;
import abstractexpressions.matrixexpression.classes.MatrixExpression;

public class TestUtilities {

    public static void printResult(Object expected, Object result) {
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();
        String callerClassName = elements[1].getClassName();
        String callerMethodName = elements[1].getMethodName();
        System.out.println("--------------Begin of test----------------");
        System.out.println("Test class: " + callerClassName);
        System.out.println("Test: " + callerMethodName);

        if (expected instanceof Expression && result instanceof Expression) {
            if (((Expression) expected).equivalent((Expression) result)) {
                System.out.println("\033[32m Expected result:" + expected.toString());
            } else {
                System.out.println("\033[31m Expected result: " + expected.toString());
            }
        } else if (expected instanceof LogicalExpression && result instanceof LogicalExpression) {
            if (((LogicalExpression) expected).equivalent((LogicalExpression) result)) {
                System.out.println("\033[32m Expected result:" + expected.toString());
            } else {
                System.out.println("\033[31m Expected result: " + expected.toString());
            }
        } else if (expected instanceof MatrixExpression && result instanceof MatrixExpression) {
            if (((MatrixExpression) expected).equivalent((MatrixExpression) result)) {
                System.out.println("\033[32m Expected result:" + expected.toString());
            } else {
                System.out.println("\033[31m Expected result: " + expected.toString());
            }
        } else if (expected instanceof ExpressionCollection && result instanceof ExpressionCollection) {
            if (((ExpressionCollection) expected).equals((ExpressionCollection) result)) {
                System.out.println("\033[32m Expected result:" + expected.toString());
            } else {
                System.out.println("\033[31m Expected result: " + expected.toString());
            }
        } else if (expected instanceof LogicalExpressionCollection && result instanceof LogicalExpressionCollection) {
            if (((LogicalExpressionCollection) expected).equals((LogicalExpressionCollection) result)) {
                System.out.println("\033[32m Expected result:" + expected.toString());
            } else {
                System.out.println("\033[31m Expected result: " + expected.toString());
            }
        } else if (expected instanceof MatrixExpressionCollection && result instanceof MatrixExpressionCollection) {
            if (((MatrixExpressionCollection) expected).equals((MatrixExpressionCollection) result)) {
                System.out.println("\033[32m Expected result:" + expected.toString());
            } else {
                System.out.println("\033[31m Expected result: " + expected.toString());
            }
        } else if (expected.equals(result)) {
            System.out.println("\033[32m Expected result:" + expected.toString());
        } else {
            System.out.println("\033[31m Expected result: " + expected.toString());
        }

        System.out.println("Result: " + result.toString());
        System.out.println("--------------End of test------------------");
    }

    public static void printResults(Object[] expectedResults, Object[] results) {
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();
        String callerClassName = elements[1].getClassName();
        String callerMethodName = elements[1].getMethodName();
        printResults(callerClassName, callerMethodName, expectedResults, results);
    }
    
    public static void printResults(String className, String testMethodName, Object[] expectedResults, Object[] results) {
        System.out.println("--------------Begin of test----------------");
        System.out.println("Test class: " + className);
        System.out.println("Test: " + testMethodName);
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
