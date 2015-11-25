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
        // f = exp(5+3*x/8+a^2)-7/11, var = "x", fieldExtensions = {exp(x)}.
        try {
            ExpressionCollection fieldGenerators = new ExpressionCollection(Expression.build("exp(x)", null), Expression.build("ln(x)", null));
            f = Expression.build("exp(5+3*x/8+a^2)-7/11", null);
            boolean result = RischAlgorithmMethods.isFunctionAlgebraicOverDifferentialField(f, "x", fieldGenerators);
            Assert.assertTrue(result);
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }
    
}
