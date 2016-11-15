package expression.computationtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.THREE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyExponentialRelations;
import abstractexpressions.expression.utilities.SimplifyRationalFunctionMethods;
import java.util.HashSet;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import abstractexpressions.expression.equation.SolveGeneralEquationMethods;

public class SolveSpecialEquationMethodsTests {

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
    public void isRationalExponentialEquationTest() {
        try {
            // Test: e^(2*x)+e^(7*x+1)/(2-e^(4*x))^3 ist eine rationale Exponentialgleichung.
            Expression f = Expression.build("exp(2*x)+exp(7*x+1)/(2-exp(4*x))^3", null);
            f = SimplifyExponentialRelations.separateConstantPartsInRationalExponentialEquations(f, "x");
            HashSet<Expression> argumentsOfExp = new HashSet<>();
            assertTrue(SimplifyRationalFunctionMethods.isRationalFunktionInExp(f, "x", argumentsOfExp));
            System.out.println(f);
            System.out.println(argumentsOfExp);
            // Test: 5^x/(25^(x+3)-14)-12*625^x ist eine rationale Exponentialgleichung.
            Expression g = Expression.build("5^x/(25^(x+3)-14)-12*625^x", null);
            g = SimplifyExponentialRelations.separateConstantPartsInRationalExponentialEquations(g, "x");
            argumentsOfExp.clear();
            assertTrue(SimplifyRationalFunctionMethods.isRationalFunktionInExp(g, "x", argumentsOfExp));
            System.out.println(g);
            System.out.println(argumentsOfExp);
            // Test: 7^x+2*10^x ist keine rationale Exponentialgleichung.
            Expression h = Expression.build("7^x+2*10^x", null);
            h = SimplifyExponentialRelations.separateConstantPartsInRationalExponentialEquations(h, "x");
            argumentsOfExp.clear();
            Assert.assertFalse(SimplifyRationalFunctionMethods.isRationalFunktionInExp(h, "x", argumentsOfExp));
            System.out.println(h);
            System.out.println(argumentsOfExp);
        } catch (exceptions.ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isGeneralRationalExponentialEquationTest() {
        try {
            // Test: a^x+a^(2*x)-30 ist eine rationale Exponentialgleichung.
            Expression f = Expression.build("a^x+a^(2*x)-30", null);
            f = SimplifyExponentialRelations.separateConstantPartsInRationalExponentialEquations(f, "x");
            HashSet<Expression> factorsOfVar = new HashSet<>();
            assertTrue(SimplifyRationalFunctionMethods.isRationalFunktionInExp(f, "x", factorsOfVar));
            System.out.println(f);
            System.out.println(factorsOfVar);
        } catch (exceptions.ExpressionException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void solveExponentialEquationTest1() {
        try {
            // Test: f = e^(3*x) - 20*e^(2*x) + 101*e^(x) - 130 = 0. Lösungen sind ln(2), ln(5), ln(13).
            Expression f = Expression.build("exp(3*x)-20*exp(2*x)+101*exp(x)-130", null);
            ExpressionCollection zeros = SolveGeneralEquationMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 3);
            assertTrue(zeros.containsExpression(new Constant(2).ln()));
            assertTrue(zeros.containsExpression(new Constant(5).ln()));
            assertTrue(zeros.containsExpression(new Constant(13).ln()));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        } 
    }
    
    @Test
    public void solveExponentialEquationTest2() {
        try {
            // Test: f = exp(x) + exp(2^(1/2)*x) = 10. Keine algebraischen Lösungen.
            Expression f = Expression.build("exp(x) + exp(2^(1/2)*x) - 10", null);
            ExpressionCollection zeros = SolveGeneralEquationMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 0);
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void solveExponentialEquationTest3() {
        try {
            // Test: f = 5^x + 3*25^x - 8/25 = 0. Lösung ist x = -1.
            Expression f = Expression.build("5^x + 3*25^x - 8/25", null);
            ExpressionCollection zeros = SolveGeneralEquationMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.containsExpression(Expression.MINUS_ONE));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveGeneralRationalExponentialEquationTest() {
        try {
            // Test: a^x+a^(2*x)-30 = 0. Lösung x = ln(5)/ln(a)
            Expression f = Expression.build("a^x+a^(2*x)-30", null);
            ExpressionCollection zeros = SolveGeneralEquationMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.containsExpression(new Constant(5).ln().div(Variable.create("a").ln())));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void solveAlgebraicEquationTest1() {
        try {
            // Test: (7*x+1)^(1/3)+(x^2+5*x+21)^(1/3)-5 = 0. Lösung x = 1.
            Expression f = Expression.build("(7*x+1)^(1/3)+(x^2+5*x+21)^(1/3)-5", null);
            ExpressionCollection zeros = SolveGeneralEquationMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.containsExpression(ONE));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveAlgebraicEquationTest2() {
        try {
            // Test: x*(2*x+3)^(1/2)-(x+1)*(x^2-5)^(1/2)-1 = 0. Lösung x = 3.
            Expression f = Expression.build("x*(2*x+3)^(1/2)-(x+1)*(x^2-5)^(1/2)-1", null);
            ExpressionCollection zeros = SolveGeneralEquationMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.containsExpression(THREE));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void solveAlgebraicEquationTest3() {
        try {
            // Test: x^2+(x^3+1)*(3*x^5+x+30)^(1/7)-(x^4+6) = 0. Lösung x = 2.
            Expression f = Expression.build("x^2+(x^3+1)*(3*x^5+x+30)^(1/7)-(x^4+6)", null);
            ExpressionCollection zeros = SolveGeneralEquationMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.containsExpression(TWO));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

   @Test
    public void solveAlgebraicEquationTest4() {
        try {
            // Test: x*x^(1/2)+(x^2+1)^(3/2)-(8+x^3) = 0. Lösung x = 1.
            Expression f = Expression.build("x*x^(1/2)+(x^2+3)^(3/2)-(8+x^3)", null);
            ExpressionCollection zeros = SolveGeneralEquationMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.containsExpression(ONE));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

   @Test
    public void solveAlgebraicEquationTest5() {
        try {
            // Test: x*(2+x)^(1/2)-(x-1)^(1/3) - 3 = 0. Lösung x = 2.
            Expression f = Expression.build("x*(2+x)^(1/2)-(x-1)^(1/3) - 3", null);
            ExpressionCollection zeros = SolveGeneralEquationMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.containsExpression(TWO));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

   @Test
    public void solveAlgebraicEquationTest6() {
        try {
            // Test: (12*x+1/x)^(2/3)+(3/4+x^2)^(1/2)-5 = 0. Lösung x = 1/2.
            Expression f = Expression.build("(12*x+1/x)^(2/3)+(3/4+x^2)^(1/2)-5", null);
            ExpressionCollection zeros = SolveGeneralEquationMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.containsExpression(ONE.div(TWO)));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

   @Test
    public void solveAlgebraicEquationTest7() {
        try {
            // Test: (x+(x^3+1)^(1/3))^2-(1+x^2) = 0. Lösung x = 2.
            Expression f = Expression.build("(x+(x^3+1)^(1/3))^2-(1+x^2)", null);
            ExpressionCollection zeros = SolveGeneralEquationMethods.solveEquation(f, ZERO, "x");
//            assertTrue(zeros.getBound() == 1);
//            assertTrue(zeros.containsExpression(TWO));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }


    
}
