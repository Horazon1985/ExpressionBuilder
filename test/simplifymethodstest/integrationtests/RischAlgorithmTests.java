package simplifymethodstest.integrationtests;

import exceptions.ExpressionException;
import expressionbuilder.Expression;
import expressionsimplifymethods.ExpressionCollection;
import integrationmethods.RischAlgorithmMethods;
import junit.framework.Assert;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RischAlgorithmTests {

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
    public void isAlgebraicOverFieldExtensionTest1() {
        // f = exp(x+2), var = "x", fieldExtensions = {}.
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection();
            f = Expression.build("exp(x+2)", null);
            boolean result = RischAlgorithmMethods.isFunctionAlgebraicOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertFalse(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isAlgebraicOverFieldExtensionTest2() {
        // f = exp(x)+3*ln(x)-7/4, var = "x", fieldExtensions = {exp(x), ln(x)}.
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection(Expression.build("exp(x)", null), Expression.build("ln(x)", null));
            f = Expression.build("exp(x)+3*ln(x)-7/4", null);
            boolean result = RischAlgorithmMethods.isFunctionAlgebraicOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertTrue(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isAlgebraicOverFieldExtensionTest3() {
        // f = exp(x)+3*ln(x)-7/4, var = "x", fieldExtensions = {exp(x), ln(x)}.
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection(Expression.build("exp(x)", null), Expression.build("ln(x)", null));
            f = Expression.build("exp(x)+3*ln(7*x)-7/4", null);
            boolean result = RischAlgorithmMethods.isFunctionAlgebraicOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertTrue(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isAlgebraicOverFieldExtensionTest4() {
        /* 
         Für f = exp(5+3*x/8+a^2)-7/11, var = "x", fieldExtensions = {exp(x)} wird 
         true zurückgegeben, für f = exp(5+3^(1/5)*x+a^2)-7/11 dagegen false..
         */
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection(Expression.build("exp(x)", null), Expression.build("ln(x)", null));
            f = Expression.build("exp(5+3*x/8+a^2)-7/11", null);
            boolean result = RischAlgorithmMethods.isFunctionAlgebraicOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertTrue(result);
            f = Expression.build("exp(5+3^(1/5)*x+a^2)-7/11", null);
            result = RischAlgorithmMethods.isFunctionAlgebraicOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertFalse(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isAlgebraicOverFieldExtensionTest5() {
        /* 
         Für f = x^2/(1+x)+ln((3+2*x)^7)-x, var = "x", fieldExtensions = {exp(x), ln(2*x+3)} wird 
         true zurückgegeben, für f = x^3+7*ln(x)-5 dagegen false.
         */
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection(Expression.build("exp(x)", null), Expression.build("ln(2*x+3)", null));
            f = Expression.build("x^2/(1+x)+ln((3+2*x)^7)-x", null);
            boolean result = RischAlgorithmMethods.isFunctionAlgebraicOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertTrue(result);
            f = Expression.build("x^3+7*ln(x)-5", null);
            result = RischAlgorithmMethods.isFunctionAlgebraicOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertFalse(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isAlgebraicOverFieldExtensionTest6() {
        /* 
         Für f = x^2/(1+x)+x*ln(21+9*x)-x^5, var = "x", fieldExtensions = {exp(x), ln(14/5+(6*x)/5)} wird 
         true zurückgegeben.
         */
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection(Expression.build("exp(x)", null), Expression.build("ln(14/5+(6*x)/5)", null));
            f = Expression.build("x^2/(1+x)+x*ln(21/8+9*x/8)-x^5", null);
            boolean result = RischAlgorithmMethods.isFunctionAlgebraicOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertTrue(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getFieldGeneratorsTest() {
        /* 
         Für f = x^7*exp(x)+ln(3+x)+exp(5*x+exp(x)-1), var = "x". Dann wird 
         fieldExtensions = {exp(x), ln(3+x), exp(5*x+exp(x)-1)} zurückgegeben.
         */
        try {
            f = Expression.build("x^7*exp(x)+ln(3+x)+exp(5*x+exp(x)-1)", null);
            ExpressionCollection fieldGenerators = RischAlgorithmMethods.getOrderedTranscendentalGeneratorsForDifferentialField(f, "x");
            ExpressionCollection fieldGeneratorsForCompare = new ExpressionCollection(Expression.build("exp(5*x+exp(x)-1)", null), 
                    Expression.build("ln(3+x)", null), Expression.build("exp(x)", null));
            Assert.assertTrue(fieldGenerators.equals(fieldGeneratorsForCompare));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

}
