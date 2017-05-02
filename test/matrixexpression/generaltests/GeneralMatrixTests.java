package matrixexpression.generaltests;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.matrixexpression.classes.Matrix;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
import basic.MathToolTestBase;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeneralMatrixTests extends MathToolTestBase {

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
            matExpr = MatrixExpression.build("[2,3;-1,6]^(-1)");
            expectedResult = MatrixExpression.build("[2/5,-1/5;1/15,2/15]");
            matExpr = matExpr.simplify();
            
            results = new Object[]{matExpr};
            expectedResults = new Object[]{expectedResult};
            
            Assert.assertTrue(matExpr.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void matrixExpressionBuildTest1() {
        try {
            matExpr = MatrixExpression.build("a*[1;3]");
            expectedResult = MatrixExpression.build("[a]").mult(MatrixExpression.build("[1;3]"));
            
            results = new Object[]{matExpr};
            expectedResults = new Object[]{expectedResult};
            
            Assert.assertTrue(matExpr.equals(expectedResult));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void matrixExpressionBuildTest2() {
        try {
            matExpr = MatrixExpression.build("exp([1,2;3,x])");
            expectedResult = MatrixExpression.build("[1,2;3,x]").exp();
            
            results = new Object[]{matExpr};
            expectedResults = new Object[]{expectedResult};
            
            Assert.assertTrue(matExpr.equals(expectedResult));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void factorizeInSumsTest() {
        try {
            matExpr = MatrixExpression.build("[2,3;-1,6]*exp([a,b;c,d])+[6,3;2,-7]+[2,3;-1,6]*sin([a,b;c,d])^2");
            expectedResult = MatrixExpression.build("[2,3;-1,6]*(exp([a,b;c,d])+sin([a,b;c,d])^2)+[6,3;2,-7]");
            matExpr = matExpr.simplifyFactorize();
            
            results = new Object[]{matExpr};
            expectedResults = new Object[]{expectedResult};
            
            Assert.assertTrue(matExpr.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void factorizeInSumsNotPossibleTest() {
        try {
            matExpr = MatrixExpression.build("[2,3;-1,6]*exp([a,b;c,d])+([6,3;2,-7]+[5,3;-1,6]*sin([a,b;c,d])^2)");
            expectedResult = MatrixExpression.build("[2,3;-1,6]*exp([a,b;c,d])+([6,3;2,-7]+[5,3;-1,6]*sin([a,b;c,d])^2)");
            matExpr = matExpr.simplifyFactorize();
            
            results = new Object[]{matExpr};
            expectedResults = new Object[]{expectedResult};
            
            Assert.assertTrue(matExpr.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void factorizeInDifferencesTest() {
        try {
            matExpr = MatrixExpression.build("([2,3;-1,6]*exp([a,b;c,d])+[6,3;2,-7])-[2,3;-1,6]*sin([a,b;c,d])^2");
            expectedResult = MatrixExpression.build("[2,3;-1,6]*(exp([a,b;c,d])-sin([a,b;c,d])^2)+[6,3;2,-7]");
            matExpr = matExpr.simplifyFactorize();
            
            results = new Object[]{matExpr};
            expectedResults = new Object[]{expectedResult};
            
            Assert.assertTrue(matExpr.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void factorizeInDifferencesNotPossibleTest() {
        try {
            matExpr = MatrixExpression.build("([2,3;-1,6]*exp([a,b;c,d])+[6,3;2,-7])-[5,3;-1,6]*sin([a,b;c,d])^2");
            expectedResult = MatrixExpression.build("([2,3;-1,6]*exp([a,b;c,d])+[6,3;2,-7])-[5,3;-1,6]*sin([a,b;c,d])^2");
            matExpr = matExpr.simplifyFactorize();
            
            results = new Object[]{matExpr};
            expectedResults = new Object[]{expectedResult};
            
            Assert.assertTrue(matExpr.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void collectSummandsTest() {
        try {
            matExpr = MatrixExpression.build("(exp([a,b;c,d])+[1,2;3,4])+(sin([a,b;c,d])+[5,6;7,8/11])");
            expectedResult = MatrixExpression.build("[6,8;10,52/11]+exp([a,b;c,d])+sin([a,b;c,d])");
            matExpr = matExpr.simplify();
            
            results = new Object[]{matExpr};
            expectedResults = new Object[]{expectedResult};
            
            Assert.assertTrue(matExpr.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void collectFactorsTest() {
        try {
            matExpr = MatrixExpression.build("(exp([a,b;c,d])*[1,2;3,4])*([5,6;7,8/11]*sin([a,b;c,d]))*[3,-4;1,-9]");
            expectedResult = MatrixExpression.build("exp([a,b;c,d])*[19,82/11;43,230/11]*sin([a,b;c,d])*[3,-4;1,-9]");
            matExpr = matExpr.simplify();
            
            results = new Object[]{matExpr};
            expectedResults = new Object[]{expectedResult};
            
            Assert.assertTrue(matExpr.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    
    
    
    
    
    
    
    
}
