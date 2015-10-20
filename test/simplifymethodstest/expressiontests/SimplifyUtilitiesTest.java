package simplifymethodstest.expressiontests;

import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyUtilities;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimplifyUtilitiesTest {

    Expression f, g, h;
    Expression exprWithMultipleVariables;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void defineExpressions() throws Exception {
        // f = 1*x*sin(x)
        f = new Constant(1).mult(Variable.create("x")).mult(new Function(Variable.create("x"), TypeFunction.sin));
        // g = x*1*sin(x)
        g = Variable.create("x").mult(new Constant(1)).mult(new Function(Variable.create("x"), TypeFunction.sin));
        // h = 1*1*1
        h = new Constant(1).mult(new Constant(1)).mult(new Constant(1));
        Variable x = Variable.create("x");
        Variable y = Variable.create("y");
        // expr = (1*x*sin(x)*1*y^2)/(5*y^3)
        exprWithMultipleVariables = new Constant(1).mult(x).mult(new Function(x, TypeFunction.sin)).mult(1).mult(y.pow(2)).div(new Constant(5).mult(y.pow(3)));
    }

    @Test
    public void getFactorsOfFTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactors(f);
        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfGTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactors(g);
        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfGIfApproximatingTest() {
        g = g.turnToApproximate();
        ExpressionCollection factors = SimplifyUtilities.getFactors(g);
        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfHTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactors(h);
        assertTrue(factors.getBound() == 1);
    }

    @Test
    public void getFactorsOfEnumeratorOfFTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactorsOfEnumeratorInExpression(f);
        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfDenominatorOfFTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
        assertTrue(factors.isEmpty());
    }

    @Test
    public void getFactorsOfEnumeratorOfGTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactorsOfEnumeratorInExpression(g);
        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getFactorsOfDenominatorOfGTest() {
        ExpressionCollection factors = SimplifyUtilities.getFactorsOfDenominatorInExpression(g);
        assertTrue(factors.isEmpty());
    }

    @Test
    public void getNonConstantFactorsOfEnumeratorOfExprTest() {
        ExpressionCollection factors = SimplifyUtilities.getNonConstantFactorsOfEnumeratorInExpression(exprWithMultipleVariables, "x");
        assertTrue(factors.getBound() == 2);
    }

    @Test
    public void getNonConstantFactorsOfDenominatorOfExprWithRespectToXTest() {
        ExpressionCollection factors = SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(exprWithMultipleVariables, "x");
        assertTrue(factors.isEmpty());
    }

    @Test
    public void getNonConstantFactorsOfDenominatorOfExprWithRespectToYTest() {
        ExpressionCollection factors = SimplifyUtilities.getNonConstantFactorsOfDenominatorInExpression(exprWithMultipleVariables, "y");
        assertTrue(factors.getBound() == 1);
    }

}