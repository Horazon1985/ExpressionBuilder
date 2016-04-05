package expression.computationtests;

import abstractexpressions.expression.classes.Expression;
import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import junit.framework.Assert;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import utilities.TestUtilities;

public class AlgebraicMethodsTests {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void defineExpressions() throws Exception {
    }

    @Test
    public void expandAlgebraicExpressionTest1() {
        try {
            // (3 + 5*7^(1/2))^5 = 506868+233900*7^(1/2). 
            Expression f = Expression.build("(3 + 5*7^(1/2))^5", null);
            Expression fExpanded = f.simplify();
            Expression expectedResult = Expression.build("506868+233900*7^(1/2)", null);
            TestUtilities.printResult(expectedResult, fExpanded);
            Assert.assertTrue(fExpanded.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void expandAlgebraicExpressionTest2() {
        try {
            // (2 - 3*5^(1/2))^3 = 278-171*5^(1/2). 
            Expression f = Expression.build("(2 - 3*5^(1/2))^3", null);
            Expression fExpanded = f.simplify();
            Expression expectedResult = Expression.build("278-171*5^(1/2)", null);
            TestUtilities.printResult(expectedResult, fExpanded);
            Assert.assertTrue(fExpanded.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void expandAlgebraicExpressionTest3() {
        try {
            // (2^(1/2)/3-4)^3 = (434*2^(1/2))/27-200/3. 
            Expression f = Expression.build("(2^(1/2)/3-4)^3", null);
            Expression fExpanded = f.simplify();
            Expression expectedResult = Expression.build("(434*2^(1/2))/27-200/3", null);
            TestUtilities.printResult(expectedResult, fExpanded);
            Assert.assertTrue(fExpanded.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void expandAlgebraicExpressionTest4() {
        try {
            // (6*2^(1/2)-4*3^(1/2))^7 = 73073664*2^(1/2)-59664384*3^(1/2). 
            Expression f = Expression.build("(6*2^(1/2)-4*3^(1/2))^7", null);
            Expression fExpanded = f.simplify();
            Expression expectedResult = Expression.build("73073664*2^(1/2)-59664384*3^(1/2)", null);
            TestUtilities.printResult(expectedResult, fExpanded);
            Assert.assertTrue(fExpanded.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void expandAlgebraicExpressionFailedTest() {
        try {
            // (6*2^(1/2)-4*3^(1/2))^101 wird nicht vereinfacht. 
            Expression f = Expression.build("6*2^(1/2)-4*3^(1/2)", null);
            f = f.pow(ComputationBounds.BOUND_ALGEBRA_MAX_POWER_OF_BINOMIAL + 1);
            Expression fExpanded = f.simplify();
            Expression expectedResult = f;
            TestUtilities.printResult(expectedResult, f);
            Assert.assertTrue(fExpanded.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
}
