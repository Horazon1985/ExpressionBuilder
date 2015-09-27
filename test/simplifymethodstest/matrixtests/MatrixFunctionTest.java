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
    public void computeExpOfNilpotentMatrixTest() {
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

    @Test
    public void computeLnOfDiagonalizableMatrixTest() {
        /* 
         Die Matrix [3,-1;3,7] hat die Eigenwerte 4, 6 und ist daher diagonalisierbar. ln() kann daher leicht berechnet werden.
         */
        try {
            MatrixExpression m = MatrixExpression.build("[3,-1;3,7]", null);
            Assert.assertTrue(m instanceof Matrix);
            MatrixExpression expOfM = m.ln().simplify();
            MatrixExpression result = MatrixExpression.build("[ln(8/6^(1/2)),ln(2/6^(1/2));ln((3*6^(1/2))/4),ln(3*6^(1/2))]", null);
            Assert.assertTrue(expOfM.equals(result));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

}
