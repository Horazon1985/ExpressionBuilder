package expression.computationtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
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
import abstractexpressions.expression.equation.SolveMethods;

public class SpecialEquationMethodsTest {

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
            ExpressionCollection zeros = SolveMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 3);
            assertTrue(zeros.contains(new Constant(2).ln()));
            assertTrue(zeros.contains(new Constant(5).ln()));
            assertTrue(zeros.contains(new Constant(13).ln()));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        } 
    }
    
    @Test
    public void solveExponentialEquationTest2() {
        try {
            // Test: f = exp(x) + exp(2^(1/2)*x) = 10. Keine algebraischen Lösungen.
            Expression f = Expression.build("exp(x) + exp(2^(1/2)*x) - 10", null);
            ExpressionCollection zeros = SolveMethods.solveEquation(f, ZERO, "x");
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
            ExpressionCollection zeros = SolveMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.contains(Expression.MINUS_ONE));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveGeneralRationalExponentialEquationTest() {
        try {
            // Test: a^x+a^(2*x)-30 = 0. Lösung x = ln(5)/ln(a)
            Expression f = Expression.build("a^x+a^(2*x)-30", null);
            ExpressionCollection zeros = SolveMethods.solveEquation(f, ZERO, "x");
            assertTrue(zeros.getBound() == 1);
            assertTrue(zeros.contains(new Constant(5).ln().div(Variable.create("a").ln())));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }
    
}