package expression.generaltests;

import enums.TypeExpansion;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.TWO;
import enums.TypeSimplify;
import java.util.HashSet;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeneralSimplifyExpressionTest {

    Expression a, b, c, d, x, y, z;

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
        d = Expression.build("d", new HashSet<String>());
        x = Expression.build("x", new HashSet<String>());
        y = Expression.build("y", new HashSet<String>());
        z = Expression.build("z", new HashSet<String>());
    }

    // Tests für triviale Vereinfachung.
    @Test
    public void computeFractionsTest1() {
        // Addition und Multiplikation von Brüchen: 2+3*5/2 = 19/2
        try {
            Expression f = Expression.build("2+3*5/2", null);
            Expression g = new Constant(19).div(2);
            Expression fSimplified = f.simplify(TypeSimplify.order_sums_and_products, TypeSimplify.simplify_trivial);
            Assert.assertTrue(fSimplified.equals(g));
            fSimplified = f.simplify();
            Assert.assertTrue(fSimplified.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void computeFractionsTest2() {
        // Subtraktion und Multiplikation von Brüchen: 2-7/2+3*5/7 = 9/14
        try {
            Expression f = Expression.build("2-7/2+3*5/7", null);
            Expression g = new Constant(9).div(14);
            Expression fSimplified = f.simplify(TypeSimplify.order_sums_and_products,
                    TypeSimplify.order_difference_and_division, TypeSimplify.simplify_trivial);
            Assert.assertTrue(fSimplified.equals(g));
            fSimplified = f.simplify();
            Assert.assertTrue(fSimplified.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void orderSumsAndProductsTest() {
        // Ordnen von Summen und Produkten.
        Expression f = x.mult((a.add(b).add(c).add(y))).mult(z);
        Expression g = x.mult((a.add(b.add(c.add(y)))).mult(z));
        try {
            f = f.orderSumsAndProducts();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void orderDifferencesAndQuotientsTest() {
        // Ordnen von Differenzen und Quotienten. (x*y)*(exp(a+(b-c))/z) = (x*(y*exp((a+b)-c)))/z
        Expression f = x.mult(y).mult((a.add(b.sub(c)).exp()).div(z));
        Expression g = x.mult(y.mult(a.add(b).sub(c).exp())).div(z);
        try {
            f = f.orderDifferencesAndQuotients();
            Assert.assertTrue(f.equivalent(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void collectPowersInProductTest() {
        // Sammeln von Potenzen mit gleicher Basis.
        Expression f = a.mult(b.mult(a.pow(2))).mult(c);
        Expression g = a.pow(ONE.add(TWO)).mult(b.mult(c));
        try {
            f = f.simplifyCollectProducts();
            Assert.assertTrue(f.equivalent(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void collectPowersInQuotientTest() {
        // Sammeln von Potenzen mit gleicher Basis.
        Expression f = a.mult(b.mult(c.sin())).div(x.mult(b.pow(5)));
        Expression g = a.mult(b.pow(ONE.sub(5)).mult(c.sin())).div(x);
        try {
            f = f.simplifyReduceQuotients().orderDifferencesAndQuotients();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeSumsTest1() {
        // a*b+c*c = a*(b+c) 
        Expression f = a.mult(b).add(a.mult(c));
        Expression g = a.mult(b.add(c));
        try {
            f = f.simplifyFactorize();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeSumsTest2() {
        // b/a+c/a = (b+c)/a 
        Expression f = b.div(a).add(c.div(a));
        Expression g = b.add(c).div(a);
        try {
            f = f.simplifyFactorize();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeSumsTest3() {
        // (x*b)/a+(c*x)/a = (b+c)/a 
        Expression f = x.mult(b).div(a).add(c.mult(x).div(a));
        Expression g = x.mult(b.add(c)).div(a);
        try {
            f = f.simplifyFactorize();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
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
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeSumsSimplifyFactorizePowersTest() {
        // x*1+x*x = x*(1+x)
        Expression f = x.mult(1).add(x.mult(x));
        Expression g = x.mult(ONE.add(x));
        try {
            f = f.simplifyFactorize();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeDifferencesTest1() {
        // a*b-c*c = a*(b-c) 
        Expression f = a.mult(b).sub(a.mult(c));
        Expression g = a.mult(b.sub(c));
        try {
            f = f.simplifyFactorize();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeDifferencesTest2() {
        // b/a-c/a = (b-c)/a 
        Expression f = b.div(a).sub(c.div(a));
        Expression g = b.sub(c).div(a);
        try {
            f = f.simplifyFactorize();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeDifferencesTest3() {
        // (x*b)/a-(c*x)/a = (b-c)/a 
        Expression f = x.mult(b).div(a).sub(c.mult(x).div(a));
        Expression g = x.mult(b.sub(c)).div(a);
        try {
            f = f.simplifyFactorize();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
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
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeDifferencesSimplifyFactorizePowersTest() {
        // x*1-x*x = x*(1-x)
        Expression f = x.mult(1).sub(x.mult(x));
        Expression g = x.mult(ONE.sub(x));
        try {
            f = f.simplifyFactorize();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInSumsTest1() {
        // 3*a*b+a*5*b+x = (3+5)*a*b+x
        try {
            Expression f = Expression.build("3*a*b+a*5*b+x", null);
            Expression g = Expression.build("(3+5)*(a*b)+x", null);
            f = f.simplifyFactorizeAllButRationals();
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInSumsTest2() {
        // (a*b)/3+(b*a)/7 = (1/3+1/7)*(a*b) 
        try {
            Expression f = Expression.build("(a*b)/3+(b*a)/7", null);
            Expression g = Expression.build("(1/3+1/7)*(a*b)", null);
            f = f.simplifyFactorizeAllButRationals();
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInSumsTest3() {
        // (11*a*b)/3+(b*a*15)/7 = (11/3+15/7)*(a*b)
        try {
            Expression f = Expression.build("(11*a*b)/3+(b*a*15)/7", null);
            Expression g = Expression.build("(11/3+15/7)*(a*b)", null);
            f = f.simplifyFactorizeAllButRationals();
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInDifferencesTest1() {
        // 3*a*b-a*5*b+x = (3-5)*a*b+x
        try {
            Expression f = Expression.build("(3*a*b-a*5*b)+x", null);
            Expression g = Expression.build("(3-5)*(a*b)+x", null);
            f = f.simplifyFactorizeAllButRationals();
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInDifferencesTest2() {
        // (a*b)/3-(b*a)/7 = (1/3-1/7)*(a*b) 
        try {
            Expression f = Expression.build("(a*b)/3-(b*a)/7", null);
            Expression g = Expression.build("(1/3-1/7)*(a*b)", null);
            f = f.simplifyFactorizeAllButRationals();
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInDifferencesTest3() {
        // (11*a*b)/3+x-(b*a*15)/7 = (11/3-15/7)*(a*b)+x
        try {
            Expression f = Expression.build("((11*a*b)/3+x)-(b*a*15)/7", null);
            Expression g = Expression.build("(11/3-15/7)*(a*b)+x", null);
            f = f.simplifyFactorizeAllButRationals();
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInDifferencesTest4() {
        // 5^(1/2)*a+5^(1/2)*b wird nicht faktorisiert.
        try {
            Expression f = Expression.build("5^(1/2)*a+5^(1/2)*b", null);
            Expression g = Expression.build("5^(1/2)*a+5^(1/2)*b", null);
            f = f.simplifyFactorizeAllButRationals();
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void expandTest() {
        // (a+b)*(c+d) = a*c + a*d + b*c + b*d
        Expression f = (a.add(b)).mult(c.add(d));
        Expression g = a.mult(c).add(a.mult(d).add(b.mult(c).add(b.mult(d))));
        try {
            // Durch orderSumsAndProducts() werden überflüssige Einsen beseitigt.
            f = f.simplifyExpand(TypeExpansion.POWERFUL).orderSumsAndProducts();
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void expandBinomialTest() {
        // (a+b)^3 = a^3 + 3*a^2*b + 3*a*b^2 + b^3
        Expression f = a.add(b).pow(3);
        Expression g = a.pow(3).add(Expression.THREE.mult(a.pow(2)).mult(b.pow(1))).add(
                Expression.THREE.mult(a.pow(1)).mult(b.pow(2))).add(b.pow(3));
        try {
            // Durch orderSumsAndProducts() werden überflüssige Einsen beseitigt.
            f = f.simplifyExpand(TypeExpansion.POWERFUL).orderSumsAndProducts();
            Assert.assertTrue(f.equivalent(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

}
