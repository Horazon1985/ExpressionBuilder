package simplifymethodstest.matrixtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import linearalgebraalgorithms.EigenvaluesEigenvectorsAlgorithms;
import linearalgebraalgorithms.GaussAlgorithm;
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
            fail(e.getMessage());
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
            fail(e.getMessage());
        }
    }

    @Test
    public void isNotDiagonalizableTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[1,2;0,1]", null);
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertFalse(EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) m));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void diagonalizeMatrixTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[3,-2;2,-3]", null);
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(EigenvaluesEigenvectorsAlgorithms.isMatrixDiagonalizable((Matrix) m));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isMatrixNilpotentTest1() {
        try {
            MatrixExpression m = MatrixExpression.build("[-18,-24;27/2,18]", null);
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(((Matrix) m).isNilpotentMatrix());
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isMatrixNilpotentTest2() {
        try {
            MatrixExpression m = MatrixExpression.build("[0,a,b;0,0,c;0,0,0]", null);
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(((Matrix) m).isNilpotentMatrix());
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isMatrixNotNilpotentTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[1,-2,5;6,8,11;2,4,3]", null);
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertFalse(((Matrix) m).isNilpotentMatrix());
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    // Tests für das Lösen von linearen Gleichungssystemen
    @Test
    public void solveLGSTest1() {
        try {
            MatrixExpression m = MatrixExpression.build("[2,-1,0;3,2,6;0,-1,4]", null);
            MatrixExpression b = MatrixExpression.build("[0;49;26]", null);
            Expression[] expectedSolution = new Expression[]{new Constant(1), new Constant(2), new Constant(7)};
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(b instanceof Matrix);
            Expression[] solution = GaussAlgorithm.solveLinearSystemOfEquations((Matrix) m, (Matrix) b);
            Assert.assertFalse(expectedSolution.equals(solution));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

}
