package logicalexpression.computationtests;

import abstractexpressions.logicalexpression.classes.LogicalExpression;
import static abstractexpressions.logicalexpression.classes.LogicalExpression.FALSE;
import static abstractexpressions.logicalexpression.classes.LogicalExpression.TRUE;
import basic.MathToolTestBase;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeneralLogicalTests extends MathToolTestBase {

    public GeneralLogicalTests() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void cancelFalseInSumsTest() {
        try {
            LogicalExpression logExpr = LogicalExpression.build("a|0|b|0|0|c", null);
            logExpr = logExpr.simplify();
            LogicalExpression expectedResult = LogicalExpression.build("a|b|c", null);
            assertTrue(logExpr.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void trueInSumsOccursTest() {
        try {
            LogicalExpression logExpr = LogicalExpression.build("a|0|b|1|0|c", null);
            logExpr = logExpr.simplify();
            LogicalExpression expectedResult = TRUE;
            assertTrue(logExpr.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void cancelTrueInProductsTest() {
        try {
            LogicalExpression logExpr = LogicalExpression.build("a&1&1&(c|d)&1", null);
            logExpr = logExpr.simplify();
            LogicalExpression expectedResult = LogicalExpression.build("a&(c|d)", null);
            assertTrue(logExpr.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void falseInProductsOccursTest() {
        try {
            LogicalExpression logExpr = LogicalExpression.build("a&0&c&(d|e)", null);
            logExpr = logExpr.simplify();
            LogicalExpression expectedResult = FALSE;
            assertTrue(logExpr.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void factorizeInSumsTest() {
        try {
            LogicalExpression logExpr = LogicalExpression.build("a&b|a&c|x|a&d", null);
            logExpr = logExpr.simplify();
            LogicalExpression expectedResult = LogicalExpression.build("a&(b|c|d)|x", null);
            assertTrue(logExpr.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeInProductsTest() {
        try {
            LogicalExpression logExpr = LogicalExpression.build("(a|b)&(a|c)&x&(a|d)", null);
            logExpr = logExpr.simplify();
            LogicalExpression expectedResult = LogicalExpression.build("(a|b&c&d)&x", null);
            assertTrue(logExpr.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
}
