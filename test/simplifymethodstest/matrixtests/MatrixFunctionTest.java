package simplifymethodstest.matrixtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
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
            a = MatrixExpression.build("[0,0,0;-1,0,0;2,5,0]", null);
            Assert.assertTrue(a instanceof Matrix);
            MatrixExpression expOfM = a.exp().simplify();
            MatrixExpression result = MatrixExpression.build("[1,0,0;-1,1,0;(-1)/2,5,1]", null);
            Assert.assertTrue(expOfM.equals(result));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void computeLnOfDiagonalizableMatrixTest() {
        /* 
         Die Matrix [3,-1;3,7] hat die Eigenwerte 4, 6 und ist daher diagonalisierbar. 
         ln() kann daher leicht berechnet werden.
         */
        try {
            a = MatrixExpression.build("[3,-1;3,7]", null);
            Assert.assertTrue(a instanceof Matrix);
            MatrixExpression expOfM =a.ln().simplify();
            MatrixExpression result = MatrixExpression.build("[ln(8/6^(1/2)),ln(2/6^(1/2));ln((3*6^(1/2))/4),ln(3*6^(1/2))]", null);
            Assert.assertTrue(expOfM.equals(result));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

}
