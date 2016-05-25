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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.fail;

public class GeneralSimplifyExpressionTests {

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
            Expression fSimplified = f.simplify(TypeSimplify.order_sums_and_products, TypeSimplify.simplify_basic);
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
                    TypeSimplify.order_difference_and_division, TypeSimplify.simplify_basic);
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

    @Test
    public void equivalentTest1() {
        // cos((x+y)-z) äquivalent zu cos(z-(x+y))
        try {
            Expression f = Expression.build("cos((x+y)-z)", null);
            Expression g = Expression.build("cos(z-(x+y))", null);
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void equivalentTest2() {
        // tan((x+y)-z) nicht äquivalent zu tan(z-(x+y))
        // exp((x+y)-z) nicht äquivalent zu exp(z-(x+y))
        try {
            Expression f = Expression.build("tan((x+y)-z)", null);
            Expression g = Expression.build("tan(z-(x+y))", null);
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("exp((x+y)-z)", null);
            g = Expression.build("exp(z-(x+y))", null);
            Assert.assertFalse(f.equivalent(g));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void equivalentTest3() {
        try {
            Expression f = Expression.build("((x+y)-z)^6", null);
            Expression g = Expression.build("(z-(x+y))^6", null);
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("((x+y)-z)^(4/7)", null);
            g = Expression.build("(z-(x+y))^(4/7)", null);
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("((x+y)-z)^(-3)", null);
            g = Expression.build("(z-(x+y))^(-3)", null);
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("((x+y)-z)^(1/4)", null);
            g = Expression.build("(z-(x+y))^(1/4)", null);
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("((x+y)-z)^(1/7)", null);
            g = Expression.build("(z-(x+y))^(1/4)", null);
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("((x+y)-z)^a", null);
            g = Expression.build("(z-(x+y))^a", null);
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("((x+y)-z)*b*sin(s-t)", null);
            g = Expression.build("b*sin(t-s)*(z-(x+y))", null);
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("((x+y)-z)*b*sin(s-t)*(p-q)", null);
            g = Expression.build("b*sin(t-s)*(q-p)*(z-(x+y))", null);
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("((x+y)-z)*sin(s-t)", null);
            g = Expression.build("b*sin(t-s)*(z-(x+y))", null);
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("((x+y)-z)/sin(s-t)", null);
            g = Expression.build("(z-(x+y))/sin(t-s)", null);
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void antiEequivalentTest() {
        try {
            Expression f = Expression.build("((x+y)-z)^5", null);
            Expression g = Expression.build("(z-(x+y))^5", null);
            Assert.assertTrue(f.antiEquivalent(g));
            f = Expression.build("((x+y)-z)^(4/7)", null);
            g = Expression.build("(z-(x+y))^(4/7)", null);
            Assert.assertFalse(f.antiEquivalent(g));
            f = Expression.build("sin(((x+y)-z)^(-3))", null);
            g = Expression.build("sin((z-(x+y))^(-3))", null);
            Assert.assertTrue(f.antiEquivalent(g));
            f = Expression.build("(x+y)-z", null);
            g = Expression.build("z-(x+y)", null);
            Assert.assertTrue(f.antiEquivalent(g));
            f = Expression.build("exp(((x+y)-z))", null);
            g = Expression.build("exp((z-(x+y)))", null);
            Assert.assertFalse(f.antiEquivalent(g));
            f = Expression.build("((x+y)-z)*a*(p-q)*b*sin(s-t)", null);
            g = Expression.build("b*sin(t-s)*(z-(x+y))*a*(q-p)", null);
            Assert.assertTrue(f.antiEquivalent(g));
            f = Expression.build("((x+y)-z)*a*(p-q)*sin(s-t)", null);
            g = Expression.build("b*sin(t-s)*(z-(x+y))*a*(q-p)", null);
            Assert.assertFalse(f.antiEquivalent(g));
            f = Expression.build("((x+y)-z)*a*(p-q)*b", null);
            g = Expression.build("b*(z-(x+y))*a*(q-p)", null);
            Assert.assertFalse(f.antiEquivalent(g));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reduceFractionTest1() {
        // ((a^2+b^2)/(b+a^2/b) = b
        Expression f = a.pow(2).add(b.pow(2)).div(b.add(a.pow(2).div(b)));
        Expression g = b;
        try {
            f = f.simplify();
            Assert.assertTrue(f.equivalent(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reduceFractionTest2() {
        // (4*a*b^2/9-2*a^2*b/9-2*b^3/9)/(4*a*b/9-2*a^2/9-2*b^2/9) = b
        try {
            Expression f = Expression.build("(4*a*b^2/9-2*a^2*b/9-2*b^3/9)/(4*a*b/9-2*a^2/9-2*b^2/9)", null);
            Expression g = b;
            f = f.simplify();
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reduceFractionTest3() {
        //(b^3+a)/(a*b^3+a^2) = 1/a
        Expression f = b.pow(3).add(a).div(a.mult(b.pow(3)).add(a.pow(2)));
        Expression g = ONE.div(a);
        try {
            f = f.simplify();
            Assert.assertTrue(f.equivalent(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reduceFractionTest4() {
        // (b+b^2*a)/(1+a*b) = b
        Expression f = b.add(b.pow(2).mult(a)).div(ONE.add(a.mult(b)));
        Expression g = b;
        try {
            f = f.simplify();
            Assert.assertTrue(f.equivalent(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reduceFractionTest5() {
        // a/((a+a^2)*(a*b+a^5)) = 1/((1+a)*(a*b+a^5))
        // a^2/((a+a^2)*(a*b+a^5)) = 1/((1+a)*(b+a^4)) 
        try {
            Expression f = Expression.build("a/((a+a^2)*(a*b+a^5))", null);
            Expression g = Expression.build("1/((1+a)*(a*b+a^5))", null);
            f = f.simplify();
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("a^2/((a+a^2)*(a*b+a^5))", null);
            g = Expression.build("1/((1+a)*(b+a^4))", null);
            f = f.simplify();
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bringFractionToCommonDenominatorTest1() {
        // (1 + a*(1/x+1/y))/b = (1+a*(y+x)/(x*y))/b
        try {
            Expression f = Expression.build("(1 + a*(1/x+1/y))/b", null);
            Expression g = Expression.build("(1+a*(y+x)/(x*y))/b", null);
            f = f.simplifyBringFractionsToCommonDenominator();
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bringFractionToCommonDenominatorTest2() {
        // (1 + a*(1/x+1/y)*(1+2/z))/sin(1/(p/q)) = (1+a*(y+x)/(x*y)*(z+2)/z)/sin(q/p)
        try {
            Expression f = Expression.build("(1 + a*(1/x+1/y)*(1+2/z))/sin(1/(p/q))", null);
            Expression g = Expression.build("(1+a*(y+x)/(x*y)*(z+2)/z)/sin(q/p)", null);
            f = f.simplifyBringFractionsToCommonDenominator();
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bringFractionToCommonDenominatorTest3() {
        // a+x*(1/x+2*x) wird nicht vereinfacht, da es keine Mehrfachbrüche gibt.
        try {
            Expression f = Expression.build("a+x*(1/x+2*x)", null);
            Expression g = Expression.build("a+x*(1/x+2*x)", null);
            f = f.simplifyBringFractionsToCommonDenominator();
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reduceLongFractionTest() {
        /* 
         Anspruchsvoll: ((2*x^2*s)/(x^2+x^4-2*x^3)+1/(x^2+x^4-2*x^3)+1/(x-x^2)-((x*s)/(x-x^2)+(2*x)/(x^2+x^4-2*x^3)+(s*x^3)/(x^2+x^4-2*x^3)))/((x^2*s)/(x^2+x^4-2*x^3)+x/(x^2+x^4-2*x^3)+x/(x-x^2)-((x*s)/(x-x^2)+(2*x^2)/(x^2+x^4-2*x^3)))
         = 1/x.
         */
        try {
            Expression f = Expression.build("((2*x^2*s)/(x^2+x^4-2*x^3)+1/(x^2+x^4-2*x^3)+1/(x-x^2)-((x*s)/(x-x^2)+(2*x)/(x^2+x^4-2*x^3)+(s*x^3)/(x^2+x^4-2*x^3)))/((x^2*s)/(x^2+x^4-2*x^3)+x/(x^2+x^4-2*x^3)+x/(x-x^2)-((x*s)/(x-x^2)+(2*x^2)/(x^2+x^4-2*x^3)))", null);
            Expression g = Expression.build("1/x", null);
            f = f.simplify();
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

}
