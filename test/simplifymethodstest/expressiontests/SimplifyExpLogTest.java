package simplifymethodstest.expressiontests;

import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import expressionbuilder.ExpressionException;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyExpLog;
import expressionsimplifymethods.SimplifyUtilities;
import java.util.HashSet;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimplifyExpLogTest {

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
            Expression f = Expression.build("a*exp(x)*b*exp(y^2)", new HashSet<String>());
            Expression g = Expression.build("a*exp(x+y^2)*b", new HashSet<String>());
            ExpressionCollection factors = SimplifyUtilities.getFactors(f);
            SimplifyExpLog.collectExponentialFunctionsInProduct(factors);
            Expression fNew = SimplifyUtilities.produceProduct(factors);
            Assert.assertTrue(fNew.equivalent(g));
        } catch (ExpressionException e){
            fail("Ein Ausdruck konnte nicht kompiliert werden.");
        }
    }
    
    @Test
    public void collectExponentialFunctionsInQuotient() {
        try{
            Expression f = Expression.build("(a*exp(x)*b*exp(y^2))/(c*exp(z)*d)", new HashSet<String>());
            Expression g = Expression.build("(a*exp(x+y^2-z)*b)/(c*d)", new HashSet<String>());
            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfEnumeratorInExpression(f);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
            SimplifyExpLog.collectExponentialFunctionsInQuotient(factorsEnumerator, factorsDenominator);
            Expression fNew = SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator).orderDifferenceAndDivision();
            g = g.orderDifferenceAndDivision();
            Assert.assertTrue(fNew.equivalent(g));
        } catch (ExpressionException | EvaluationException e){
            fail("Ein Ausdruck konnte nicht kompiliert werden.");
        }
    }
    
    
    
    
    
    
    
}
