package simplifymethodstest;

import expressionbuilder.EvaluationException;
import expressionbuilder.Expression;
import static expressionbuilder.Expression.ONE;
import static expressionbuilder.Expression.TWO;
import java.util.HashSet;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeneralSimplifyTest {

    Expression a, b, c, x, y, z;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void defineExpressions() throws Exception {
        a = Expression.build("a", new HashSet<String>());
        b = Expression.build("b", new HashSet<String>());
        c = Expression.build("c", new HashSet<String>());
        x = Expression.build("x", new HashSet<String>());
        y = Expression.build("y", new HashSet<String>());
        z = Expression.build("z", new HashSet<String>());
    }

    @Test
    public void orderSumsAndProductsTest() {
        // Ordnen von Summen und Produkten.
        Expression f = x.mult((a.add(b).add(c).add(y))).mult(z);
        Expression g = x.mult((a.add(b.add(c.add(y)))).mult(z));
        try {
            f = f.orderSumsAndProducts();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }

    @Test
    public void orderDifferencesAndQuotientsTest() {
        // Ordnen von Differenzen und Quotienten. (x*y)*(exp(a+(b-c))/z) = (x*(y*exp((a+b)-c)))/z
        Expression f = x.mult(y).mult((a.add(b.sub(c)).exp()).div(z));
        Expression g = x.mult(y.mult(a.add(b).sub(c).exp())).div(z);
        try {
            f = f.orderDifferenceAndDivision();
            Assert.assertTrue(f.equivalent(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }
    
    @Test
    public void collectPowersInProductTest() {
        // Sammeln von Potenzen mit gleicher Basis.
        Expression f = a.mult(b.mult(a.pow(2))).mult(c);
        Expression g = a.pow(ONE.add(TWO)).mult(b.mult(c));
        try {
            f = f.collectProducts();
            Assert.assertTrue(f.equivalent(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }

    @Test
    public void collectPowersInQuotientTest() {
        // Sammeln von Potenzen mit gleicher Basis.
        Expression f = a.mult(b.mult(c.sin())).div(x.mult(b.pow(5)));
        Expression g = a.mult(b.pow(ONE.sub(5)).mult(c.sin())).div(x);
        try {
            f = f.reduceQuotients().orderDifferenceAndDivision();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }

    @Test
    public void factorizeSumsTest1() {
        // a*b+c*c = a*(b+c) 
        Expression f = a.mult(b).add(a.mult(c));
        Expression g = a.mult(b.add(c));
        try {
            f = f.factorizeInSums();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }
            
    @Test
    public void factorizeSumsTest2() {
        // b/a+c/a = (b+c)/a 
        Expression f = b.div(a).add(c.div(a));
        Expression g = b.add(c).div(a);
        try {
            f = f.factorizeInSums();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }
    
    @Test
    public void factorizeSumsTest3() {
        // (x*b)/a+(c*x)/a = (b+c)/a 
        Expression f = x.mult(b).div(a).add(c.mult(x).div(a));
        Expression g = x.mult(b.add(c)).div(a);
        try {
            f = f.factorizeInSums();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }
    
    @Test
    public void factorizeSumsSimplifyNotFactorizePowersTest() {
        // x*1+x*x = x+x^2 NOT x*(1+x)
        Expression f = x.mult(1).add(x.mult(x));
        Expression g = x.add(x.pow(2));
        try {
            f = f.simplify();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }   
    
    @Test
    public void factorizeSumsSimplifyFactorizePowersTest() {
        // x*1+x*x = x*(1+x)
        Expression f = x.mult(1).add(x.mult(x));
        Expression g = x.mult(ONE.add(x));
        try {
            f = f.factorizeInSums();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }
    
    @Test
    public void factorizeDifferencesTest1() {
        // a*b-c*c = a*(b-c) 
        Expression f = a.mult(b).sub(a.mult(c));
        Expression g = a.mult(b.sub(c));
        try {
            f = f.factorizeInDifferences();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }
            
    @Test
    public void factorizeDifferencesTest2() {
        // b/a-c/a = (b-c)/a 
        Expression f = b.div(a).sub(c.div(a));
        Expression g = b.sub(c).div(a);
        try {
            f = f.factorizeInDifferences();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }
    
    @Test
    public void factorizeDifferencesTest3() {
        // (x*b)/a-(c*x)/a = (b-c)/a 
        Expression f = x.mult(b).div(a).sub(c.mult(x).div(a));
        Expression g = x.mult(b.sub(c)).div(a);
        try {
            f = f.factorizeInDifferences();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }
    
    @Test
    public void factorizeDifferencesSimplifyNotFactorizePowersTest() {
        // x*1-x*x = x-x^2 NOT x*(1-x)
        Expression f = x.mult(1).sub(x.mult(x));
        Expression g = x.sub(x.pow(2));
        try {
            f = f.simplify();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }   
    
    @Test
    public void factorizeDifferencesSimplifyFactorizePowersTest() {
        // x*1-x*x = x*(1-x)
        Expression f = x.mult(1).sub(x.mult(x));
        Expression g = x.mult(ONE.sub(x));
        try {
            f = f.factorizeInDifferences();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e){
            fail("f konnte nicht vereinfacht werden.");
        }
    }
    
    
    
    
    
}
