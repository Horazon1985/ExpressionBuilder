package simplifymethodstest.matrixtests;

import expressionbuilder.EvaluationException;
import expressionbuilder.ExpressionException;
import matrixexpressionbuilder.Matrix;
import matrixexpressionbuilder.MatrixExpression;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MatrixFunctionTest {
 
    MatrixExpression a;

    
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
    public void computeExpTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[0,0,0;-1,0,0;2,5,0]", null);
            Assert.assertTrue(m instanceof Matrix);
            MatrixExpression expOfM = m.exp().simplify();
            MatrixExpression result = MatrixExpression.build("[1,0,0;-1,1,0;(-1)/2,5,1]", null);
            Assert.assertTrue(expOfM.equals(result));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    
    
    
    
    
    
    
}
