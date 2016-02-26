package expression.computationtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyExpLog;
import abstractexpressions.expression.utilities.SimplifyUtilities;
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
            fail(e.getMessage());
        }
    }
    
    @Test
    public void collectExponentialFunctionsInQuotient() {
        try{
            Expression f = Expression.build("(a*exp(x)*b*exp(y^2))/(c*exp(z)*d)", new HashSet<String>());
            Expression g = Expression.build("(a*exp(x+y^2-z)*b)/(c*d)", new HashSet<String>());
            ExpressionCollection factorsEnumerator = SimplifyUtilities.getFactorsOfNumeratorInExpression(f);
            ExpressionCollection factorsDenominator = SimplifyUtilities.getFactorsOfDenominatorInExpression(f);
            SimplifyExpLog.collectExponentialFunctionsInQuotient(factorsEnumerator, factorsDenominator);
            Expression fNew = SimplifyUtilities.produceQuotient(factorsEnumerator, factorsDenominator).orderDifferencesAndQuotients();
            g = g.orderDifferencesAndQuotients();
            Assert.assertTrue(fNew.equivalent(g));
        } catch (ExpressionException | EvaluationException e){
            fail(e.getMessage());
        }
    }
    
    
    
    
    
    
    
}
