package matrixexpression.computationtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.matrixexpression.computation.EigenvaluesEigenvectorsAlgorithms;
import abstractexpressions.matrixexpression.computation.GaussAlgorithm;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.classes.MatrixExpression;
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
    public void solveLGSWithUniqueSolutionTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[2,-1,0;3,2,6;0,-1,4]", null);
            MatrixExpression b = MatrixExpression.build("[0;49;26]", null);
            Expression[] expectedSolution = new Expression[]{new Constant(1), new Constant(2), new Constant(7)};
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(b instanceof Matrix);
            Expression[] solution = GaussAlgorithm.solveLinearSystemOfEquations((Matrix) m, (Matrix) b);
            Assert.assertTrue(solution.length == 3);
            Assert.assertTrue(solution[0].equals(expectedSolution[0]));
            Assert.assertTrue(solution[1].equals(expectedSolution[1]));
            Assert.assertTrue(solution[2].equals(expectedSolution[2]));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveLGSWithNonUniqueSolutionTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[0,1,3,-2,4;0,0,0,2,5]", null);
            MatrixExpression b = MatrixExpression.build("[11;17]", null);
            // Lösungen sind x_0 = T_2, x_1 = 28 - (9*T_0 + 3*T_1), x_2 = T_1, x_3 = 17/2 - 5*T_0/2, x_4 = T_0.
            Expression[] expectedSolution = new Expression[]{Variable.create("T_2"),
                new Constant(28).sub(new Constant(9).mult(Variable.create("T_0")).add(new Constant(3).mult(Variable.create("T_1")))),
                Variable.create("T_1"), new Constant(17).div(2).sub(new Constant(5).mult(Variable.create("T_0")).div(2)),
                Variable.create("T_0")};
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(b instanceof Matrix);
            Expression[] solution = GaussAlgorithm.solveLinearSystemOfEquations((Matrix) m, (Matrix) b);
            Assert.assertTrue(solution.length == 5);
            Assert.assertTrue(solution[0].equals(expectedSolution[0]));
            Assert.assertTrue(solution[1].equals(expectedSolution[1]));
            Assert.assertTrue(solution[2].equals(expectedSolution[2]));
            Assert.assertTrue(solution[3].equals(expectedSolution[3]));
            Assert.assertTrue(solution[4].equals(expectedSolution[4]));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveLGSWithNoSolutionTest() {
        try {
            MatrixExpression m = MatrixExpression.build("[2,3;6,10;8,5]", null);
            MatrixExpression b = MatrixExpression.build("[-1;-4;4]", null);
            Assert.assertTrue(m instanceof Matrix);
            Assert.assertTrue(b instanceof Matrix);
            Assert.assertTrue(GaussAlgorithm.solveLinearSystemOfEquations((Matrix) m, (Matrix) b) == GaussAlgorithm.NO_SOLUTIONS);
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

}
