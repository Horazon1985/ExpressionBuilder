package simplifymethodstest.matrixtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import matrixexpressionbuilder.MatrixExpression;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import utilities.TestUtilities;

public class GeneralMatrixTest {

    MatrixExpression matExpr;
    MatrixExpression expectedResult;
    
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
    public void invertMatrixTest() {
        try {
            matExpr = MatrixExpression.build("[2,3;-1,6]^(-1)", null);
            expectedResult = MatrixExpression.build("[2/5,-1/5;1/15,2/15]", null);
            matExpr = matExpr.simplify();
            TestUtilities.printResult(expectedResult, matExpr);
            Assert.assertTrue(matExpr.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void factorizeInSumsTest() {
        try {
            matExpr = MatrixExpression.build("[2,3;-1,6]*exp([a,b;c,d])+[6,3;2,-7]+[2,3;-1,6]*sin([a,b;c,d])^2", null);
            expectedResult = MatrixExpression.build("[2,3;-1,6]*(exp([a,b;c,d])+sin([a,b;c,d])^2)+[6,3;2,-7]", null);
            matExpr = matExpr.simplifyFactorizeInSums();
            TestUtilities.printResult(expectedResult, matExpr);
            Assert.assertTrue(matExpr.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void factorizeInSumsNotPossibleTest() {
        try {
            matExpr = MatrixExpression.build("[2,3;-1,6]*exp([a,b;c,d])+([6,3;2,-7]+[5,3;-1,6]*sin([a,b;c,d])^2)", null);
            expectedResult = MatrixExpression.build("[2,3;-1,6]*exp([a,b;c,d])+([6,3;2,-7]+[5,3;-1,6]*sin([a,b;c,d])^2)", null);
            matExpr = matExpr.simplifyFactorizeInSums();
            TestUtilities.printResult(expectedResult, matExpr);
            Assert.assertTrue(matExpr.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void factorizeInDifferencesTest() {
        try {
            matExpr = MatrixExpression.build("([2,3;-1,6]*exp([a,b;c,d])+[6,3;2,-7])-[2,3;-1,6]*sin([a,b;c,d])^2", null);
            expectedResult = MatrixExpression.build("[2,3;-1,6]*(exp([a,b;c,d])-sin([a,b;c,d])^2)+[6,3;2,-7]", null);
            matExpr = matExpr.simplifyFactorizeInDifferences();
            TestUtilities.printResult(expectedResult, matExpr);
            Assert.assertTrue(matExpr.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void factorizeInDifferencesNotPossibleTest() {
        try {
            matExpr = MatrixExpression.build("([2,3;-1,6]*exp([a,b;c,d])+[6,3;2,-7])-[5,3;-1,6]*sin([a,b;c,d])^2", null);
            expectedResult = MatrixExpression.build("([2,3;-1,6]*exp([a,b;c,d])+[6,3;2,-7])-[5,3;-1,6]*sin([a,b;c,d])^2", null);
            matExpr = matExpr.simplifyFactorizeInDifferences();
            TestUtilities.printResult(expectedResult, matExpr);
            Assert.assertTrue(matExpr.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    
    
    
    
    
    
    
    
}