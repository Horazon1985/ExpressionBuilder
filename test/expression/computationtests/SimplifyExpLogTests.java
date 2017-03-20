package expression.computationtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.basic.SimplifyExpLogUtils;
import abstractexpressions.expression.basic.SimplifyUtilities;
import basic.MathToolTestBase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.fail;
import utilities.TestUtilities;

public class SimplifyExpLogTests extends MathToolTestBase {

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
    public void collectExponentialFunctionsInProduct() {
        try{
            Expression f = Expression.build("a*exp(x)*b*exp(y^2)");
            Expression g = Expression.build("a*exp(x+y^2)*b");
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            SimplifyExpLogUtils.collectExponentialFunctionsInProduct(factors);
            Expression fNew = SimplifyUtilities.produceProduct(factors);
            TestUtilities.printResult(g, fNew);
            Assert.assertTrue(fNew.equivalent(g));
        } catch (ExpressionException e){
            fail(e.getMessage());
        }
    }
    
    @Test
    public void collectExponentialFunctionsInQuotient() {
        try{
            Expression f = Expression.build("(a*exp(x)*b*exp(y^2))/(c*exp(z)*d)");
            Expression g = Expression.build("(a*exp(x+y^2-z)*b)/(c*d)");
            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(f);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
            SimplifyExpLogUtils.collectExponentialFunctionsInQuotient(factorsEnumerator, factorsDenominator);
            Expression fNew = SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator).orderDifferencesAndQuotients();
            g = g.orderDifferencesAndQuotients();
            TestUtilities.printResult(g, fNew);
            Assert.assertTrue(fNew.equivalent(g));
        } catch (ExpressionException | EvaluationException e){
            fail(e.getMessage());
        }
    }
    
    
    
    
    
    
    
}
