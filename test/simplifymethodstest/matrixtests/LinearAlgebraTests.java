package simplifymethodstest.matrixtests;

import expressionbuilder.ExpressionException;
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
            MatrixExpression m = MatrixExpression.build("[2,3;-1,6]", null);
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
            MatrixExpression m = MatrixExpression.build("[1,2,3;4,3,2;1,2,5]", null);
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) m));
        } catch (ExpressionException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void isNotDiagonalizableTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[1,2;0,1]", null);
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertFalse(EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) m));
        } catch (ExpressionException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void diagonalizeMatrixTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[3,-2;2,-3]", null);
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) m));
        } catch (ExpressionException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void isMatrixNilpotentTest1() {
        try {
            MatrixExpression m = MatrixExpression.build("[-18,-24;27/2,18]", null);
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(((Matrix) m).isNilpotentMatrix());
        } catch (ExpressionException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void isMatrixNilpotentTest2() {
        try {
            MatrixExpression m = MatrixExpression.build("[0,a,b;0,0,c;0,0,0]", null);
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(((Matrix) m).isNilpotentMatrix());
        } catch (ExpressionException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void isMatrixNotNilpotentTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[1,-2,5;6,8,11;2,4,3]", null);
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertFalse(((Matrix) m).isNilpotentMatrix());
        } catch (ExpressionException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
}
