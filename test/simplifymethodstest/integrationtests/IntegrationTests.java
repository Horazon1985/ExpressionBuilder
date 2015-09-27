
package simplifymethodstest.integrationtests;

import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.ExpressionException;
import expressionbuilder.Operator;
import integrationmethods.SimplifyIntegralMethods;
import junit.framework.Assert;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IntegrationTests {

    Expression f;

    
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
    public void computeIntegralOfPolynomialTest() {
        // integral von x^3/7+x^2-5 ist = x^4/28+x^2-5.
        try {
            f = Expression.build("int(x^3/7+x^2-5,x)", null);
            Object integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            Expression result = Expression.build("((x^(3+1)/(3+1))/7+x^(2+1)/(2+1))-5*x", null);
            Assert.assertTrue(integral instanceof Expression);
            Assert.assertTrue(((Expression) integral).equals(result));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void computeIntegralBySubstitutionTest() {
        // integral von x^2*exp(x^3) ist = exp(x^3)/3.
        try {
            f = Expression.build("int(x^2*exp(x^3),x)", null);
            Object integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            Expression result = Expression.build("exp(x^3)/3", null);
            Assert.assertTrue(integral instanceof Expression);
            Assert.assertTrue(((Expression) integral).equals(result));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void computeIntegralByPartialIntegrationTest() {
        // integral von ?? ist = ??.
        try {
            f = Expression.build("int(x^2*exp(x^3),x)", null);
            Object integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            Expression result = Expression.build("exp(x^3)/3", null);
            Assert.assertTrue(integral instanceof Expression);
            Assert.assertTrue(((Expression) integral).equals(result));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void integralOfGaussianFunctionNotExistsTest() {
        // integral von exp(x^2) ist nicht in kompakter Form berechenbar.
        try {
            f = Expression.build("int(exp(x^2),x)", null);
            Object integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            Assert.assertTrue(integral.equals(false));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    
}
