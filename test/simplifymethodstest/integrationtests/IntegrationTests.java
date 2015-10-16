package simplifymethodstest.integrationtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import exceptions.NotPreciseIntegrableException;
import expressionbuilder.Expression;
import expressionbuilder.Operator;
import expressionbuilder.Variable;
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
            Expression integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            Expression result = Expression.build("((x^(3+1)/(3+1))/7+x^(2+1)/(2+1))-5*x", null);
            Assert.assertTrue(((Expression) integral).equals(result));
        } catch (ExpressionException | EvaluationException | NotPreciseIntegrableException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void computeIntegralBySubstitutionTest() {
        // integral von x^2*exp(x^3) ist = exp(x^3)/3.
        try {
            f = Expression.build("int(x^2*exp(x^3),x)", null);
            Expression integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            Expression result = Expression.build("exp(x^3)/3", null);
            Assert.assertTrue(((Expression) integral).equals(result));
        } catch (ExpressionException | EvaluationException | NotPreciseIntegrableException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void computeIntegralByPartialIntegrationTest() {
        // integral von x^2*cos(x) ist = sin(x)*x^2-2*(-cos(x)*x-(-sin(x))).
        try {
            f = Expression.build("int(x^2*cos(x),x)", null);
            Expression integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            Expression result = Variable.create("x").sin().mult(Variable.create("x").pow(2)).sub(
                    Expression.TWO.mult(Expression.MINUS_ONE.mult(Variable.create("x").cos()).mult(Variable.create("x")).sub(
                                            Expression.MINUS_ONE.mult(Variable.create("x").sin()))));
            Assert.assertTrue(((Expression) integral).equals(result));
        } catch (ExpressionException | EvaluationException | NotPreciseIntegrableException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void computeIntegralsOfElementaryFunctionsTest() {
        // integral von ln(x) ist = x*ln(x)-x.
        // integral von cot(x) ist = ln(|sin(x)|).
        // integral von 5^x ist = 5^x/ln(5).
        try {
            f = Expression.build("int(ln(x),x)", null);
            Expression integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            Expression result = Expression.build("x*ln(x)-x", null);
            Assert.assertTrue(((Expression) integral).equals(result));
            f = Expression.build("int(cot(x),x)", null);
            integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            result = Expression.build("ln(|sin(x)|)", null);
            Assert.assertTrue(((Expression) integral).equals(result));
            f = Expression.build("int(5^x,x)", null);
            integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            result = Expression.build("exp(ln(5)*x)/ln(5)", null);
            Assert.assertTrue(((Expression) integral).equals(result));
            result = result.simplify();
            // Vereinfacht ist integral = 5^x/ln(5).
            Expression resultSimplified = Expression.build("5^x/ln(5)", null);
            Assert.assertTrue(result.equals(resultSimplified));
        } catch (ExpressionException | EvaluationException | NotPreciseIntegrableException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void computeIntegralOfPowerOfElementaryFunctionTest() {
        // integral von tan(x)^3 ist = tan(x)^2/2+(-(-ln(|cos(x)|))).
        try {
            f = Expression.build("int(tan(x)^3,x)", null);
            Expression integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            Expression result = Variable.create("x").tan().pow(2).div(2).sub(Expression.MINUS_ONE.mult(Variable.create("x").cos().abs().ln()));
            Assert.assertTrue(integral.equals(result));
        } catch (ExpressionException | EvaluationException | NotPreciseIntegrableException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void integralOfGaussianFunctionNotExistsTest() {
        // integral von exp(x^2) ist nicht in kompakter Form berechenbar.
        try {
            f = Expression.build("int(exp(x^2),x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Assert.assertTrue(integral.equals(f));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void integralOfSumTest() {
        // integral von x^2+exp(x^2) ist = x^3/3 + int(exp(x^2), x).
        try {
            f = Expression.build("int(x^2+exp(x^2),x)", null);
            Expression integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            Expression result = Expression.build("x^(2+1)/(2+1)+int(exp(x^2),x)", null);
            Assert.assertTrue(((Expression) integral).equals(result));
        } catch (ExpressionException | EvaluationException | NotPreciseIntegrableException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void integralOfConstantMultipleOfFunctionTest() {
        // integral von (5*exp(x^2))/11 ist = (5*int(exp(x^2), x))/11.
        try {
            f = Expression.build("int((5*exp(x^2))/11,x)", null);
            Expression integral = SimplifyIntegralMethods.indefiniteIntegration((Operator) f, true);
            Expression result = Expression.build("(5*int(exp(x^2), x))/11", null);
            Assert.assertTrue(((Expression) integral).equals(result));
        } catch (ExpressionException | EvaluationException | NotPreciseIntegrableException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    // Test zur Integration spezieller Funktionstypen.
    
    
    
    
}
