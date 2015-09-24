package simplifymethodstest;

import expressionbuilder.ExpressionException;
import java.util.HashSet;
import linearalgebraalgorithms.EigenvaluesEigenvectorsAlgorithms;
import matrixexpressionbuilder.Matrix;
import matrixexpressionbuilder.MatrixExpression;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LinearAlgebraTests {

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
    public void isDiagonalizableTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[2,3;-1,6]", new HashSet<String>());
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) m));
        } catch (ExpressionException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void isDiagonalizableWithoutComputingEigenvectorsTest() {
        try {
            // Diese Matrix besitzt drei verschiedene Eigenwerte, aber sehr komplizierte.
            MatrixExpression m = MatrixExpression.build("[1,2,3;4,3,2;1,2,5]", new HashSet<String>());
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) m));
        } catch (ExpressionException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void isNotDiagonalizableTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[1,2;0,1]", new HashSet<String>());
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertFalse(EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) m));
        } catch (ExpressionException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void diagonalizeMatrixTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[3,-2;2,-3]", new HashSet<String>());
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) m));
        } catch (ExpressionException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
}
