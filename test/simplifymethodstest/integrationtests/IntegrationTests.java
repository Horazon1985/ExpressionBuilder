package simplifymethodstest.integrationtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
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
import utilities.TestUtilities;

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
        // Integral von x^3/7+x^2-5 ist = x^4/28+x^3/3-5*x.
        try {
            f = Expression.build("int(x^3/7+x^2-5,x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("((x^(3+1)/(3+1))/7+x^(2+1)/(2+1))-5*x", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void computeIntegralBySubstitutionTest() {
        // Integral von x^2*exp(x^3) ist = exp(x^3)/3.
        try {
            f = Expression.build("int(x^2*exp(x^3),x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("exp(x^3)/3", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void computeIntegralByPartialIntegrationTest() {
        // Integral von x^2*cos(x) ist = sin(x)*x^2-2*(-cos(x)*x-(-sin(x))).
        try {
            f = Expression.build("int(x^2*cos(x),x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Variable.create("x").sin().mult(Variable.create("x").pow(2)).sub(
                    Expression.TWO.mult(Expression.MINUS_ONE.mult(Variable.create("x").cos()).mult(Variable.create("x")).sub(
                                    Expression.MINUS_ONE.mult(Variable.create("x").sin()))));
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void computeIntegralsOfElementaryFunctionsTest() {
        // Integral von ln(x) ist = x*ln(x)-x.
        // Integral von cot(x) ist = ln(|sin(x)|).
        // Integral von 5^x ist = 5^x/ln(5).
        try {
            f = Expression.build("int(ln(x),x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("x*ln(x)-x", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
            f = Expression.build("int(cot(x),x)", null);
            integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            expectedResult = Expression.build("ln(|sin(x)|)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
            f = Expression.build("int(5^x,x)", null);
            integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            expectedResult = Expression.build("exp(ln(5)*x)/ln(5)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
            expectedResult = expectedResult.simplify();
            // Vereinfacht ist integral = 5^x/ln(5).
            Expression resultSimplified = Expression.build("5^x/ln(5)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(expectedResult.equals(resultSimplified));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void computeIntegralOfPowerOfElementaryFunctionTest() {
        // Integral von tan(x)^3 ist = tan(x)^2/2+ln(|cos(x)|).
        try {
            f = Expression.build("int(tan(x)^3,x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Variable.create("x").tan().pow(2).div(2).add(Variable.create("x").cos().abs().ln());
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void integralOfGaussianFunctionNotExistsTest() {
        // Integral von exp(x^2) ist nicht in kompakter Form berechenbar.
        try {
            f = Expression.build("int(exp(x^2),x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            TestUtilities.printResult(f, integral);
            Assert.assertTrue(integral.equals(f));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void integralOfSumTest() {
        // Integral von x^2+exp(x^2) ist = x^3/3 + int(exp(x^2), x).
        try {
            f = Expression.build("int(x^2+exp(x^2),x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("x^(2+1)/(2+1)+int(exp(x^2),x)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void integralOfConstantMultipleOfFunctionTest() {
        // Integral von (5*exp(x^2))/11 ist = (5*int(exp(x^2), x))/11.
        try {
            f = Expression.build("int((5*exp(x^2))/11,x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("(5*int(exp(x^2), x))/11", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    // Test zur Integration spezieller Funktionstypen.
    // Integration mittels Partialbruchzerlegung
    @Test
    public void integralOfRationalFunctionTest() {
        // Integral von (2*x^2+14*x+8)/(x^3+7*x^2+7*x-15) = ln(|x-1|)+2*ln(|3+x|)-ln(|5+x|).
        try {
            f = Expression.build("int((2*x^2+14*x+8)/(x^3+7*x^2+7*x-15),x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("(ln(|x-1|)+2*ln(|3+x|))-ln(|5+x|)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void integralOfRationalFunctionTest2() {
        // Integral von (3*x+4)/(2*x^2+5*x+3) = (2*ln(|1+x|)+2*(ln(|3+2*x|)/2))/2.
        try {
            f = Expression.build("int((3*x+4)/(2*x^2+5*x+3),x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("ln(|1+x|)+ln(|3+2*x|)/2", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }

    @Test
    public void integralOfRationalFunctionInExpTest1() {
        // Integral von 1/(1+exp(x)) = (x+ln(1))-ln(1+exp(x)).
        try {
            f = Expression.build("int(1/(1+exp(x)),x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("(x+ln(1))-ln(1+exp(x))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void integralOfRationalFunctionInExpTest2() {
        // Integral von 1/(3+exp(2*x/7)) = x/3-ln((3+exp((2*x)/7))^(7/6)).
        try {
            f = Expression.build("int(1/(3+exp(2*x/7)),x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("(7*((2/7*x+ln(1))/3-(ln(3+exp(2/7*x)))/3))/2", null);
            Expression expectedResultSimplified = Expression.build("x/3-ln((3+exp((2*x)/7))^(7/6))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
            integral = integral.simplify();
            Assert.assertTrue(integral.equals(expectedResultSimplified));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void integralOfRationalFunctionInSinCosTest1() {
        // Integral von 1/(2+cos(x)) = x/3-ln((3+exp(x))^(1/3)).
        try {
            f = Expression.build("int(1/(2+cos(x)),x)", null);
            Expression integral = SimplifyIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("2*arctan((2*arctan(x))/3^(1/2))/3^(1/2)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
    @Test
    public void integralOfRationalFunctionInSinCosTest2() {
        // Integral von 1/(sin(x)+cos(x)) = x/3-ln((3+exp(x))^(1/3)).
        try {
            f = Expression.build("int(1/(sin(x)+cos(x)),x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = f.simplify();
            Expression expectedResult = Expression.build("ln(|(2^(1/2)+2*arctan(x))-1|^(1/2^(1/2))/|2*arctan(x)-(1+2^(1/2))|^(1/2^(1/2)))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail("Build fehlgeschlagen.");
        }
    }
    
}
