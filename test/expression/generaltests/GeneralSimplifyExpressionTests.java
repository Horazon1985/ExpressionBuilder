package expression.generaltests;

import enums.TypeExpansion;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.TWO;
import basic.MathToolTestBase;
import enums.TypeFractionSimplification;
import enums.TypeSimplify;
import java.util.HashSet;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.fail;

public class GeneralSimplifyExpressionTests extends MathToolTestBase {

    Expression a, b, c, d, x, y, z;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void defineExpressions() throws Exception {
        a = Expression.build("a");
        b = Expression.build("b");
        c = Expression.build("c");
        d = Expression.build("d");
        x = Expression.build("x");
        y = Expression.build("y");
        z = Expression.build("z");
    }

    // Tests für triviale Vereinfachung.
    @Test
    public void computeFractionsTest1() {
        // Addition und Multiplikation von Brüchen: 2+3*5/2 = 19/2
        try {
            Expression f = Expression.build("2+3*5/2");
            Expression g = new Constant(19).div(2);
            Expression fSimplified = f.simplify(TypeSimplify.order_sums_and_products, TypeSimplify.simplify_basic);
            Expression  fSimplifiedStandard = f.simplify();
            
            expectedResults = new Object[]{g, g};
            results = new Object[]{fSimplified, fSimplifiedStandard};
            
            Assert.assertTrue(fSimplified.equals(g));
            Assert.assertTrue(fSimplifiedStandard.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void computeFractionsTest2() {
        // Subtraktion und Multiplikation von Brüchen: 2-7/2+3*5/7 = 9/14
        try {
            Expression f = Expression.build("2-7/2+3*5/7");
            Expression g = new Constant(9).div(14);
            Expression fSimplified = f.simplify(TypeSimplify.order_sums_and_products,
                    TypeSimplify.order_difference_and_division, TypeSimplify.simplify_basic);
            Expression fSimplifiedStandard = f.simplify();
            
            expectedResults = new Object[]{g, g};
            results = new Object[]{fSimplified, fSimplifiedStandard};
            
            Assert.assertTrue(fSimplified.equals(g));
            Assert.assertTrue(fSimplifiedStandard.equals(g));
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
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
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
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
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
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
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
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
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
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
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
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
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
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
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
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
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
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
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
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
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
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
            Assert.assertTrue(f.equals(g));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInSumsTest1() {
        // 3*a*b+a*5*b+x = (3+5)*a*b+x
        try {
            Expression f = Expression.build("3*a*b+a*5*b+x");
            Expression g = Expression.build("(3+5)*(a*b)+x");
            f = f.simplifyFactorizeAllButRationals();
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInSumsTest2() {
        // (a*b)/3+(b*a)/7 = (1/3+1/7)*(a*b) 
        try {
            Expression f = Expression.build("(a*b)/3+(b*a)/7");
            Expression g = Expression.build("(1/3+1/7)*(a*b)");
            f = f.simplifyFactorizeAllButRationals();
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInSumsTest3() {
        // (11*a*b)/3+(b*a*15)/7 = (11/3+15/7)*(a*b)
        try {
            Expression f = Expression.build("(11*a*b)/3+(b*a*15)/7");
            Expression g = Expression.build("(11/3+15/7)*(a*b)");
            f = f.simplifyFactorizeAllButRationals();
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInDifferencesTest1() {
        // 3*a*b-a*5*b+x = (3-5)*a*b+x
        try {
            Expression f = Expression.build("(3*a*b-a*5*b)+x");
            Expression g = Expression.build("(3-5)*(a*b)+x");
            f = f.simplifyFactorizeAllButRationals();
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInDifferencesTest2() {
        // (a*b)/3-(b*a)/7 = (1/3-1/7)*(a*b) 
        try {
            Expression f = Expression.build("(a*b)/3-(b*a)/7");
            Expression g = Expression.build("(1/3-1/7)*(a*b)");
            f = f.simplifyFactorizeAllButRationals();
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInDifferencesTest3() {
        // (11*a*b)/3+x-(b*a*15)/7 = (11/3-15/7)*(a*b)+x
        try {
            Expression f = Expression.build("((11*a*b)/3+x)-(b*a*15)/7");
            Expression g = Expression.build("(11/3-15/7)*(a*b)+x");
            f = f.simplifyFactorizeAllButRationals();
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsInDifferencesTest4() {
        // 5^(1/2)*a+5^(1/2)*b wird nicht faktorisiert.
        try {
            Expression f = Expression.build("5^(1/2)*a+5^(1/2)*b");
            Expression g = Expression.build("5^(1/2)*a+5^(1/2)*b");
            f = f.simplifyFactorizeAllButRationals();
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
            Assert.assertTrue(f.equals(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void factorizeAllButRationalsWithAntiEquivalentExpressionsTest() {
        // 5/(x-y)+-2/(y-x)+z wird zu (5-+2)/(x-y)+z faktorisiert.
        // (5*a)/(x-y)-(2*a)/(y-x)+z wird zu ((5+2)*a)/(x-y)+z faktorisiert.
        try {
            Expression f = Expression.build("5/(x-y)+2/(y-x)+z");
            Expression g = Expression.build("(5-2)/(x-y)+z");
            f = f.simplifyFactorizeAllButRationals();
            Assert.assertTrue(f.equals(g));
            f = Expression.build("5/(x-y)-2/(y-x)+z");
            g = Expression.build("(5+2)/(x-y)+z");
            f = f.simplifyFactorizeAllButRationals();
            Assert.assertTrue(f.equals(g));
            f = Expression.build("(5*a)/(x-y)-(2*a)/(y-x)+z");
            g = Expression.build("((5+2)*a)/(x-y)+z");
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
            
            expectedResults = new Object[]{g};
            results = new Object[]{f};
            
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
            Expression f = Expression.build("cos((x+y)-z)");
            Expression g = Expression.build("cos(z-(x+y))");
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
            Expression f = Expression.build("tan((x+y)-z)");
            Expression g = Expression.build("tan(z-(x+y))");
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("exp((x+y)-z)");
            g = Expression.build("exp(z-(x+y))");
            Assert.assertFalse(f.equivalent(g));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void equivalentTest3() {
        try {
            Expression f = Expression.build("((x+y)-z)^6");
            Expression g = Expression.build("(z-(x+y))^6");
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("((x+y)-z)^(4/7)");
            g = Expression.build("(z-(x+y))^(4/7)");
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("((x+y)-z)^(-3)");
            g = Expression.build("(z-(x+y))^(-3)");
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("((x+y)-z)^(1/4)");
            g = Expression.build("(z-(x+y))^(1/4)");
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("((x+y)-z)^(1/7)");
            g = Expression.build("(z-(x+y))^(1/4)");
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("((x+y)-z)^a");
            g = Expression.build("(z-(x+y))^a");
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("((x+y)-z)*b*sin(s-t)");
            g = Expression.build("b*sin(t-s)*(z-(x+y))");
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("((x+y)-z)*b*sin(s-t)*(p-q)");
            g = Expression.build("b*sin(t-s)*(q-p)*(z-(x+y))");
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("((x+y)-z)*sin(s-t)");
            g = Expression.build("b*sin(t-s)*(z-(x+y))");
            Assert.assertFalse(f.equivalent(g));
            f = Expression.build("((x+y)-z)/sin(s-t)");
            g = Expression.build("(z-(x+y))/sin(t-s)");
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void antiEequivalentTest() {
        try {
            Expression f = Expression.build("((x+y)-z)^5");
            Expression g = Expression.build("(z-(x+y))^5");
            Assert.assertTrue(f.antiEquivalent(g));
            f = Expression.build("((x+y)-z)^(4/7)");
            g = Expression.build("(z-(x+y))^(4/7)");
            Assert.assertFalse(f.antiEquivalent(g));
            f = Expression.build("sin(((x+y)-z)^(-3))");
            g = Expression.build("sin((z-(x+y))^(-3))");
            Assert.assertTrue(f.antiEquivalent(g));
            f = Expression.build("(x+y)-z");
            g = Expression.build("z-(x+y)");
            Assert.assertTrue(f.antiEquivalent(g));
            f = Expression.build("exp(((x+y)-z))");
            g = Expression.build("exp((z-(x+y)))");
            Assert.assertFalse(f.antiEquivalent(g));
            f = Expression.build("((x+y)-z)*a*(p-q)*b*sin(s-t)");
            g = Expression.build("b*sin(t-s)*(z-(x+y))*a*(q-p)");
            Assert.assertTrue(f.antiEquivalent(g));
            f = Expression.build("((x+y)-z)*a*(p-q)*sin(s-t)");
            g = Expression.build("b*sin(t-s)*(z-(x+y))*a*(q-p)");
            Assert.assertFalse(f.antiEquivalent(g));
            f = Expression.build("((x+y)-z)*a*(p-q)*b");
            g = Expression.build("b*(z-(x+y))*a*(q-p)");
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
            Expression f = Expression.build("(4*a*b^2/9-2*a^2*b/9-2*b^3/9)/(4*a*b/9-2*a^2/9-2*b^2/9)");
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
            Expression f = Expression.build("a/((a+a^2)*(a*b+a^5))");
            Expression g = Expression.build("1/((1+a)*(a*b+a^5))");
            f = f.simplify();
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("a^2/((a+a^2)*(a*b+a^5))");
            g = Expression.build("1/((1+a)*(b+a^4))");
            f = f.simplify();
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bringFractionToCommonDenominatorTest1() {
        // (1 + a*(1/x+1/y))/b = (1+a*(y+x)/(x*y))/b in beiden Modi.
        try {
            Expression f = Expression.build("(1 + a*(1/x+1/y))/b");
            Expression g = Expression.build("(x*y+a*(y+x))/(x*y*b)");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.IF_MULTIPLE_FRACTION_OCCURS);
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("(1 + a*(1/x+1/y))/b");
            g = Expression.build("(x*y+a*(y+x))/(x*y*b)");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bringFractionToCommonDenominatorTest2() {
        // (1 + a*(1/x+1/y)*(1+2/z))/sin(1/(p/q)) = (1+a*(y+x)/(x*y)*(z+2)/z)/sin(q/p) in beiden Modi.
        try {
            Expression f = Expression.build("(1 + a*(1/x+1/y)*(1+2/z))/sin(1/(p/q))");
            Expression g = Expression.build("(x*y*z+a*(y+x)*(2+z))/(x*y*z*sin(q/p))");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.IF_MULTIPLE_FRACTION_OCCURS);
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("(1 + a*(1/x+1/y)*(1+2/z))/sin(1/(p/q))");
            g = Expression.build("(x*y*z+a*(y+x)*(2+z))/(x*y*z*sin(q/p))");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bringFractionToCommonDenominatorTest3() {
        // 1+1/(1+1/(1+1/x)) = (2*(2+2*x+x))/(2*(1+2*x)) in beiden Modi.
        try {
            Expression f = Expression.build("1+1/(1+1/(1+1/x))");
            Expression g = Expression.build("(2*(2+2*x+x))/(2*(1+2*x))");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.IF_MULTIPLE_FRACTION_OCCURS);
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("1+1/(1+1/(1+1/x))");
            g = Expression.build("(2*(2+2*x+x))/(2*(1+2*x))");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bringFractionToCommonDenominatorTest4() {
        // (a+b/(1+1/c))^n = (((1+c)*a+b*c)/(1+c))^n in beiden Modi.
        try {
            Expression f = Expression.build("(a+b/(1+1/c))^n");
            Expression g = Expression.build("(((1+c)*a+b*c)/(1+c))^n");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.IF_MULTIPLE_FRACTION_OCCURS);
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("(a+b/(1+1/c))^n");
            g = Expression.build("(((1+c)*a+b*c)/(1+c))^n");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bringFractionToCommonDenominatorTest5() {
        // (a*b^2/9-a^2*b/9)/(a*b/9-a^2/9) = (9*(a*b^2-a^2*b))/(9*(a*b-a^2)) in beiden Modi.
        try {
            Expression f = Expression.build("(a*b^2/9-a^2*b/9)/(a*b/9-a^2/9)");
            Expression g = Expression.build("(9*(a*b^2-a^2*b))/(9*(a*b-a^2))");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.IF_MULTIPLE_FRACTION_OCCURS);
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("(a*b^2/9-a^2*b/9)/(a*b/9-a^2/9)");
            g = Expression.build("(9*(a*b^2-a^2*b))/(9*(a*b-a^2))");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bringFractionToCommonDenominatorTest6() {
        // 1/(3+7*x+5*x^2+x^3)-1/(9+15*x+7*x^2+x^3) = 
        try {
            Expression f = Expression.build("1/(3+7*x+5*x^2+x^3)-1/(9+15*x+7*x^2+x^3)");
            Expression g = Expression.build("((x+3)-(x+1))/((x+1)^2*(x+3)^2)");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bringFractionToCommonDenominatorNotAlwaysNecessaryTest() {
        // a+x*(1/x+2*x) wird im Modus IF_MULTIPLE_FRACTION_OCCURS nicht vereinfacht, da es keine Mehrfachbrüche gibt, im ALWAYS dagagen zu (x*a+x+2*x^3)/x.
        try {
            Expression f = Expression.build("a+x*(1/x+2*x)");
            Expression g = Expression.build("a+x*(1/x+2*x)");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.IF_MULTIPLE_FRACTION_OCCURS);
            Assert.assertTrue(f.equivalent(g));
            f = Expression.build("a+x*(1/x+2*x)");
            g = Expression.build("(x*a+x+2*x^3)/x");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bringFractionToCommonDenominatorNotNecessaryTest() {
        // a+b-sin(x) wird in beiden Modi nicht weiter vereinfacht.
        try {
            Expression f = Expression.build("a+b-sin(x)");
            Expression g = Expression.build("a+b-sin(x)");
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.IF_MULTIPLE_FRACTION_OCCURS);
            Assert.assertTrue(f.equivalent(g));
            f = f.simplifyBringExpressionToCommonDenominator(TypeFractionSimplification.ALWAYS);
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
            Expression f = Expression.build("((2*x^2*s)/(x^2+x^4-2*x^3)+1/(x^2+x^4-2*x^3)+1/(x-x^2)-((x*s)/(x-x^2)+(2*x)/(x^2+x^4-2*x^3)+(s*x^3)/(x^2+x^4-2*x^3)))/((x^2*s)/(x^2+x^4-2*x^3)+x/(x^2+x^4-2*x^3)+x/(x-x^2)-((x*s)/(x-x^2)+(2*x^2)/(x^2+x^4-2*x^3)))");
            Expression g = Expression.build("1/x");
            f = f.simplify();
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reduceLengthOfExpressionTest1() {
        // (1+x)^2-x^2 wird zu 1+2*x vereinfacht.
        try {
            Expression f = Expression.build("(1+x)^2-x^2");
            Expression g = Expression.build("1+2*x");
            f = f.simplifyExpandAndCollectEquivalentsIfShorter();
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reduceLengthOfExpressionTest2() {
        // 1-1/(1+x) wird zu x/(1+x) vereinfacht.
        try {
            Expression f = Expression.build("1-1/(1+x)");
            Expression g = Expression.build("x/(1+x)");
            f = f.simplifyExpandAndCollectEquivalentsIfShorter();
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reduceLengthOfExpressionTest3() {
        // 1/(2+x)+x/(2+x)^2 wird zu (2+2*x)/(2+x)^2 vereinfacht.
        try {
            Expression f = Expression.build("1/(2+x)+x/(2+x)^2");
            Expression g = Expression.build("(2+2*x)/(2+x)^2");
            f = f.simplifyExpandAndCollectEquivalentsIfShorter();
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reduceLengthOfExpressionTest4() {
        // 1/x-1/(1+x) wird zu 1/(x+x^2) vereinfacht.
        try {
            Expression f = Expression.build("1/x-1/(1+x)");
            Expression g = Expression.build("1/(x+x^2)");
            f = f.simplifyExpandAndCollectEquivalentsIfShorter();
            Assert.assertTrue(f.equivalent(g));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void isNonPositiveTest1() {
        // 1/2+ln((1/2+(2/3)^(1/2))^(1/3)+(1/2-(2/3)^(1/2))^(1/3)) liefert false bei isNonPositive().
        try {
            Expression f = Expression.build("1/2+ln((1/2+(2/3)^(1/2))^(1/3)+(1/2-(2/3)^(1/2))^(1/3))");
            Assert.assertTrue(f.isNonPositive());
        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    
}
