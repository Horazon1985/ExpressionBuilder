package expression.computationtests;

import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.diferentialequation.SolveGeneralDifferentialEquationMethods;
import abstractexpressions.expression.equation.SolveGeneralEquationMethods;
import abstractexpressions.expression.utilities.ExpressionCollection;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeneralDifferentialEquationTests {

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
    public void getOrderAndSubOrderOfDifferentialEquationTest1() {
        try {
            // DGL: f = x^2*y''''-sin(x)*y'''-x^5/7. Dann ist ord = 4, subOrd = 3. 
            Expression f = Expression.build("x^2*y''''-sin(x)*y'''-x^5/7", null);
            int ord = SolveGeneralDifferentialEquationMethods.getOrderOfDifferentialEquation(f, "y");
            int subOrd = SolveGeneralDifferentialEquationMethods.getSubOrderOfDifferentialEquation(f, "y");
            assertTrue(ord == 4);
            assertTrue(subOrd == 3);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getOrderAndSubOrderOfDifferentialEquationTest2() {
        try {
            // DGL: f = y_1'''''+x^2*y''''-sin(x)*y'''-x^5/7. Dann ist ord = 4, subOrd = 3. 
            Expression f = Expression.build("y_1'''''+x^2*y''''-sin(x)*y'''-x^5/7", null);
            int ord = SolveGeneralDifferentialEquationMethods.getOrderOfDifferentialEquation(f, "y");
            int subOrd = SolveGeneralDifferentialEquationMethods.getSubOrderOfDifferentialEquation(f, "y");
            assertTrue(ord == 4);
            assertTrue(subOrd == 3);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void solveDiffEqWithSeparableVariablesTest1() {
        
    }
    
    @Test
    public void solveDiffEqWithSeparableVariablesTest2() {
        
    }
    
    @Test
    public void solveDiffEqLinearAndHomogeneousWithConstantCoefficientsTest1() {
        
    }
    
    @Test
    public void solveDiffEqLinearAndHomogeneousWithConstantCoefficientsTest2() {
        
    }
    
    @Test
    public void solveDiffEqLinearAndHomogeneousWithConstantCoefficientsTest3() {
        
    }
    
    @Test
    public void solveDiffEqLinearWithConstantCoefficientsTest() {
        
    }
    
    
    
}
