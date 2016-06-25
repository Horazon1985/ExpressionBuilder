package expression.computationtests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.integration.GeneralIntegralMethods;
import abstractexpressions.expression.integration.RischAlgorithmMethods;
import enums.TypeSimplify;
import exceptions.NotAlgebraicallyIntegrableException;
import java.util.HashSet;
import junit.framework.Assert;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import utilities.TestUtilities;

public class IntegrationTests {

    private static final HashSet<TypeSimplify> simplifyTypes = new HashSet<>();
    private Expression f;

    @BeforeClass
    public static void setUpClass() throws Exception {
        simplifyTypes.add(TypeSimplify.order_difference_and_division);
        simplifyTypes.add(TypeSimplify.order_sums_and_products);
        simplifyTypes.add(TypeSimplify.simplify_basic);
        simplifyTypes.add(TypeSimplify.simplify_by_inserting_defined_vars);
        simplifyTypes.add(TypeSimplify.simplify_pull_apart_powers);
        simplifyTypes.add(TypeSimplify.simplify_collect_products);
        simplifyTypes.add(TypeSimplify.simplify_factorize_all_but_rationals);
        simplifyTypes.add(TypeSimplify.simplify_factorize);
        simplifyTypes.add(TypeSimplify.simplify_bring_expression_to_common_denominator);
        simplifyTypes.add(TypeSimplify.simplify_reduce_quotients);
        simplifyTypes.add(TypeSimplify.simplify_reduce_differences_and_quotients_advanced);
        simplifyTypes.add(TypeSimplify.simplify_functional_relations);
        simplifyTypes.add(TypeSimplify.simplify_expand_logarithms);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void defineExpressions() throws Exception {
    }

    //////////////////////// Allgemeine Integrationsmethoden //////////////////////////////
    @Test
    public void computeIntegralOfPolynomialTest() {
        // Integral von x^3/7+x^2-5 ist = x^4/28+x^3/3-5*x.
        try {
            f = Expression.build("int(x^3/7+x^2-5,x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("((x^(3+1)/(3+1))/7+x^(2+1)/(2+1))-5*x", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void computeIntegralBySubstitutionTest1() {
        // Integral von x^2*exp(x^3) ist = exp(x^3)/3.
        try {
            f = Expression.build("int(x^2*exp(x^3),x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("exp(x^3)/3", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void computeIntegralBySubstitutionTest2() {
        // Integral von (6*x^2+2*cos(x))*cos(x^3+sin(x)) ist = sin(x^3+sin(x)).
        try {
            f = Expression.build("int((6*x^2+2*cos(x))*cos(x^3+sin(x)),x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("2*sin(x^3+sin(x))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void computeIntegralByPartialIntegrationTest() {
        // Integral von x^2*cos(x) ist = sin(x)*x^2-2*(-cos(x)*x-(-sin(x))).
        try {
            f = Expression.build("int(x^2*cos(x),x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Variable.create("x").sin().mult(Variable.create("x").pow(2)).sub(
                    Expression.TWO.mult(Expression.MINUS_ONE.mult(Variable.create("x").cos()).mult(Variable.create("x")).sub(
                            Expression.MINUS_ONE.mult(Variable.create("x").sin()))));
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void computeIntegralsOfElementaryFunctionsTest() {
        // Integral von ln(x) ist = x*ln(x)-x.
        // Integral von cot(x) ist = ln(|sin(x)|).
        // Integral von 5^x ist = 5^x/ln(5).
        try {
            f = Expression.build("int(ln(x),x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("x*ln(x)-x", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
            f = Expression.build("int(cot(x),x)", null);
            integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            expectedResult = Expression.build("ln(|sin(x)|)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
            f = Expression.build("int(5^x,x)", null);
            integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            expectedResult = Expression.build("exp(ln(5)*x)/ln(5)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
            expectedResult = expectedResult.simplify();
            // Vereinfacht ist integral = 5^x/ln(5).
            Expression resultSimplified = Expression.build("5^x/ln(5)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(expectedResult.equivalent(resultSimplified));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void computeIntegralOfPowerOfElementaryFunctionTest() {
        // Integral von tan(x)^3 ist = tan(x)^2/2+ln(|cos(x)|).
        try {
            f = Expression.build("int(tan(x)^3,x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Variable.create("x").tan().pow(2).div(2).add(Variable.create("x").cos().abs().ln());
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integralOfGaussianFunctionNotExistsTest() {
        // Integral von exp(x^2) ist nicht in kompakter Form berechenbar.
        try {
            f = Expression.build("int(exp(x^2),x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            TestUtilities.printResult(f, integral);
            Assert.assertTrue(integral.equals(f));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integralOfSumTest() {
        // Integral von x^2+exp(x^2) ist = x^3/3 + int(exp(x^2), x).
        try {
            f = Expression.build("int(x^2+exp(x^2),x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("x^(2+1)/(2+1)+int(exp(x^2),x)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integralOfConstantMultipleOfFunctionTest() {
        // Integral von (5*exp(x^2))/11 ist = (5*int(exp(x^2), x))/11.
        try {
            f = Expression.build("int((5*exp(x^2))/11,x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("(5*int(exp(x^2), x))/11", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equals(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    //////////////////////// Integrationsmethoden für spezielle Funktionstypen //////////////////////////////
    // Integration mittels Partialbruchzerlegung
    @Test
    public void integralOfRationalFunctionTest() {
        // Integral von (2*x^2+14*x+8)/(x^3+7*x^2+7*x-15) = ln(|x-1|)+2*ln(|3+x|)-ln(|5+x|).
        try {
            f = Expression.build("int((2*x^2+14*x+8)/(x^3+7*x^2+7*x-15),x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("(ln(|x-1|)+2*ln(|3+x|))-ln(|5+x|)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integralOfRationalFunctionTest2() {
        // Integral von (3*x+4)/(2*x^2+5*x+3) = ln(|1+x|)+ln(|3+2*x|)/2.
        try {
            f = Expression.build("int((3*x+4)/(2*x^2+5*x+3),x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("ln(|1+x|)+ln(|3+2*x|)/2", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integralOfRationalFunctionInExpTest1() {
        // Integral von 1/(1+exp(x)) = (x+ln(1))-ln(1+exp(x)).
        try {
            f = Expression.build("int(1/(1+exp(x)),x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("(x+ln(1))-ln(1+exp(x))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integralOfRationalFunctionInExpTest2() {
        // Integral von 1/(3+exp(2*x/7)) = x/3-ln((3+exp((2*x)/7))^(7/6)).
        try {
            f = Expression.build("int(1/(3+exp(2*x/7)),x)", null);
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f);
            Expression expectedResult = Expression.build("(7*((2/7*x+ln(1))/3-(ln(3+exp(2/7*x)))/3))/2", null);
            Expression expectedResultSimplified = Expression.build("x/3-ln((3+exp((2*x)/7))^(7/6))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
            integral = integral.simplify();
            Assert.assertTrue(integral.equivalent(expectedResultSimplified));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integralOfRationalFunctionInSinCosTest1() {
        // Integral von 1/(2+cos(x)) = (2*arctan(tan(x/2)/3^(1/2)))/3^(1/2).
        try {
            f = Expression.build("int(1/(2+cos(x)),x)", null);
            // Faktoren sortieren, damit die Überprüfung einfacher wird.
            Expression integral = GeneralIntegralMethods.integrateIndefinite((Operator) f).orderDifferencesAndQuotients().orderSumsAndProducts();
            Expression expectedResult = Expression.build("(2*arctan(tan(x/2)/3^(1/2)))/3^(1/2)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integralOfRationalFunctionInSinCosTest2() {
        // Integral von 1/(sin(x)+cos(x)) = ln(|2^(1/2)+tan(x/2)-1|^(1/2^(1/2))/|tan(x/2)-(1+2^(1/2))|^(1/2^(1/2))).
        try {
            f = Expression.build("int(1/(sin(x)+cos(x)),x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = f.simplify();
            Expression expectedResult = Expression.build("ln(|(2^(1/2)+tan(x/2))-1|^(1/2^(1/2))/|tan(x/2)-(1+2^(1/2))|^(1/2^(1/2)))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integralOfProductOfLinearPolynomialAndSqrtOfQuadraticPolynomialTest1() {
        // Integral von (6*x+7)*(1+x^2)^(1/2) = 2*(1+x^2)^(3/2)+(7*x*(1+x^2)^(1/2))/2+(7*arsinh(x))/2.
        try {
            f = Expression.build("int((6*x+7)*(1+x^2)^(1/2),x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = f.simplify();
            Expression expectedResult = Expression.build("2*(1+x^2)^(3/2)+(7*x*(1+x^2)^(1/2))/2+(7*arsinh(x))/2", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integralOfProductOfLinearPolynomialAndSqrtOfQuadraticPolynomialTest2() {
        // Integral von x^3*(1+x^2)^(1/2) = (1+x^2)^(5/2)/5-(1+x^2)^(3/2)/3.
        try {
            f = Expression.build("int(x^3*(1+x^2)^(1/2),x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = f.simplify();
            Expression expectedResult = Expression.build("(1+x^2)^(5/2)/5-(1+x^2)^(3/2)/3", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integralOfQuotientOfPolynomialAndSqrtOfOddPowerOfQuadraticPolynomialTest1() {
        // Integral von x^3/(1+x^2)^(1/2) = (1+x^2)^(3/2)/3-(1+x^2)^(1/2).
        try {
            f = Expression.build("int(x^3/(1+x^2)^(1/2),x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = f.simplify();
            Expression expectedResult = Expression.build("((x^2-2)*(1+x^2)^(1/2))/3", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integralOfQuotientOfPolynomialAndSqrtOfOddPowerOfQuadraticPolynomialTest2() {
        // Integral von (2+x^3)/(1+x^2)^(3/2) = (1+x^2)^(1/2)+(1+2*x)/(1+x^2)^(1/2).
        try {
            f = Expression.build("int((2+x^3)/(1+x^2)^(3/2),x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = f.simplify();
            Expression expectedResult = Expression.build(" (2+x^2+2*x)/(1+x^2)^(1/2)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    //////////////////////// Integration mittels Risch-Algorithmus //////////////////////////////
    @Test
    public void integrateByRischAlgorithmRationalPartTest1() {
        // Integral von (ln(x)-x^2-1)/(x^4+2*x^2*ln(x)+ln(x)^2) = x/(x^2+ln(x)).
        try {
            f = Expression.build("int((ln(x)-x^2-1)/(x^4+2*x^2*ln(x)+ln(x)^2),x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = f.simplify();
            Expression expectedResult = Expression.build("x/(x^2+ln(x))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmRationalPartTest2() {
        // int((x*exp(x)+1)/(x*(1+exp(x)+ln(x))), x) = ln(1+exp(x)+ln(x)).
        try {
            f = Expression.build("int((x*exp(x)+1)/(x*(1+exp(x)+ln(x))),x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = f.simplify();
            Expression expectedResult = Expression.build("ln(1+exp(x)+ln(x))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmRationalPartTest3() {
        /* 
         int((1+(1-2*x^2)*exp(x^2))/(1+2*exp(x^2)+exp(2*x^2)),x) = x/(1+exp(x^2)).
         Ideales Ergebnis wäre x/(1+exp(x^2)).
         */
        try {
            f = Expression.build("int((1+(1-2*x^2)*exp(x^2))/(1+2*exp(x^2)+exp(2*x^2)),x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = f.simplify();
            Expression expectedResult = Expression.build("x/(1+exp(x^2))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmRationalPartTest4() {
        // int((-exp(x)-x+ln(x)*x+ln(x)*x*exp(x))/(x*(exp(x)+x)^2),x) = (-ln(x))/(x+exp(x)) gemäß dem Risch-Algorithmus.
        try {
            // Für diesen Test müssen Logarithmen auseinandergezogen werden.
            f = Expression.build("int((-exp(x)-x+ln(x)*x+ln(x)*x*exp(x))/(x*(exp(x)+x)^2),x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = RischAlgorithmMethods.integrateByRischAlgorithmForTranscendentalExtension((Operator) f).simplify(simplifyTypes);
            Expression expectedResult = Expression.build("(-ln(x))/(x+exp(x))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotAlgebraicallyIntegrableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmRationalPartTest5() {
        // int((1+exp(x)-4*x*exp(x))/(1+exp(x))^5,x) = x/(1+exp(x))^4 gemäß dem Risch-Algorithmus.
        try {
            // Für diesen Test müssen Logarithmen auseinandergezogen werden.
            f = Expression.build("int((1+exp(x)-4*x*exp(x))/(1+exp(x))^5,x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = RischAlgorithmMethods.integrateByRischAlgorithmForTranscendentalExtension((Operator) f).simplify(simplifyTypes);
            Expression expectedResult = Expression.build("x/(1+exp(x))^4", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotAlgebraicallyIntegrableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmRationalPartTest6() {
        /* 
        int((1-2*x*exp(x)-exp(2*x))/(1+exp(x))^3,x) = (1+(1-2*x)*exp(x)+(1+exp(x))*(1+2*x))/(2*(1+exp(x))^2) 
        gemäß dem Risch-Algorithmus. "Längenverkürzung" ergibt später die einfachere Stammfunktion
        (1+x+exp(x))/(1+exp(x))^2.
         */
        try {
            // Für diesen Test müssen Logarithmen auseinandergezogen werden.
            f = Expression.build("int((1-2*x*exp(x)-exp(2*x))/(1+exp(x))^3,x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = RischAlgorithmMethods.integrateByRischAlgorithmForTranscendentalExtension((Operator) f).simplify(simplifyTypes);
            Expression expectedResult = Expression.build("(1+(1-2*x)*exp(x)+(1+exp(x))*(1+2*x))/(2*(1+exp(x))^2)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotAlgebraicallyIntegrableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmRationalPartTest7() {
        /* 
        int((2+(2*x-1)*ln(x)-x)/(x^2+x*ln(x)+x^2*ln(x)+x*ln(x)^2),x) = 2*ln(x+ln(x))-3*ln(1+ln(x)) 
        gemäß dem Risch-Algorithmus. "Längenverkürzung" ergibt später die einfachere Stammfunktion
        (1+x+exp(x))/(1+exp(x))^2.
         */
        try {
            // Für diesen Test müssen Logarithmen auseinandergezogen werden.
            f = Expression.build("int((2+(2*x-1)*ln(x)-x)/(x^2+x*ln(x)+x^2*ln(x)+x*ln(x)^2),x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = RischAlgorithmMethods.integrateByRischAlgorithmForTranscendentalExtension((Operator) f).simplify(simplifyTypes);
            Expression expectedResult = Expression.build("2*ln(x+ln(x))-3*ln(1+ln(x))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotAlgebraicallyIntegrableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmPolynomialPartLogarithmicCaseTest1() {
        // int(ln(1+x^2)/x^2,x) = 2*arctan(x)-ln(1+x^2)/x gemäß dem Risch-Algorithmus.
        try {
            f = Expression.build("int(ln(1+x^2)/x^2,x)", null);
            // Ohne simplify() ist der Ausdruck zu lang.
            Expression integral = RischAlgorithmMethods.integrateByRischAlgorithmForTranscendentalExtension((Operator) f).simplify(simplifyTypes);
            Expression expectedResult = Expression.build("2*arctan(x)-ln(1+x^2)/x", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotAlgebraicallyIntegrableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmPolynomialPartExponentialCaseTest1() {
        /* 
        int((2*x^2+4*x+1)*exp(x^2),x) = (2+x)*exp(x^2) gemäß dem Risch-Algorithmus.
         */
        try {
            f = Expression.build("int((2*x^2+4*x+1)*exp(x^2),x)", null);
            // simplify(...) macht den Ausdruck ein wenig schöner.
            Expression integral = RischAlgorithmMethods.integrateByRischAlgorithmForTranscendentalExtension((Operator) f).simplify(simplifyTypes);
            Expression expectedResult = Expression.build("(2+x)*exp(x^2)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotAlgebraicallyIntegrableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmPolynomialPartExponentialCaseTest2() {
        /* 
        int((x^3+x^2+x-1)*exp(x)/x^2,x) = ((1+x^2)*exp(x))/x gemäß dem Risch-Algorithmus.
         */
        try {
            f = Expression.build("int((x^3+x^2+x-1)*exp(x)/x^2,x)", null);
            // simplify(...) macht den Ausdruck ein wenig schöner.
            Expression integral = RischAlgorithmMethods.integrateByRischAlgorithmForTranscendentalExtension((Operator) f).simplify(simplifyTypes);
            Expression expectedResult = Expression.build("((1+x^2)*exp(x))/x", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotAlgebraicallyIntegrableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmPolynomialPartExponentialCaseTest3() {
        /* 
        int((x^2+2*x)*exp(x)+(2*x^2+2*x+1)*exp(2*x)/(1+x)^2,x) = x^2*exp(x)+(x*exp(2*x))/(1+x) gemäß dem Risch-Algorithmus.
         */
        try {
            f = Expression.build("int((x^2+2*x)*exp(x)+(2*x^2+2*x+1)*exp(2*x)/(1+x)^2,x)", null);
            // simplify(...) macht den Ausdruck ein wenig schöner.
            Expression integral = RischAlgorithmMethods.integrateByRischAlgorithmForTranscendentalExtension((Operator) f).simplify(simplifyTypes);
            Expression expectedResult = Expression.build("x^2*exp(x)+(x*exp(2*x))/(1+x)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotAlgebraicallyIntegrableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmPolynomialPartExponentialCaseTest4() {
        /* 
        int((8*x^2+16*x+2)*exp(2*x^2)+(-2*x^2+2*x+1)*exp(-x^2),x) = (4+2*x)*exp(2*x^2)+(1+2*x-2*x^2)*exp(-x^2) gemäß dem Risch-Algorithmus.
         */
        try {
            f = Expression.build("int((8*x^2+16*x+2)*exp(2*x^2)+(-2*x^2+2*x+1)*exp(-x^2),x)", null);
            // simplify(...) macht den Ausdruck ein wenig schöner.
            Expression integral = RischAlgorithmMethods.integrateByRischAlgorithmForTranscendentalExtension((Operator) f).simplify(simplifyTypes);
            Expression expectedResult = Expression.build("2*(2+x)*exp(2*x^2)+(x-1)*exp(-x^2)", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotAlgebraicallyIntegrableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmPolynomialAndFractionalPartExponentialCaseTest1() {
        /* 
        int((1+(3*x^2+4)*exp(x^3)+(24*x^3+12*x^2+8)*exp(2*x^3)+(24*x^3+12*x^2+8)*exp(3*x^3))/(1+4*exp(x^3)+4*exp(2*x^3)),x)
        = (1+2*x)*exp(x^3) + x/(1+2*exp(x^3)) gemäß dem Risch-Algorithmus.
         */
        try {
            f = Expression.build("int((1+(3*x^2+4)*exp(x^3)+(24*x^3+12*x^2+8)*exp(2*x^3)+(24*x^3+12*x^2+8)*exp(3*x^3))/(1+4*exp(x^3)+4*exp(2*x^3)),x)", null);
            // simplify(...) macht den Ausdruck ein wenig schöner.
            Expression integral = RischAlgorithmMethods.integrateByRischAlgorithmForTranscendentalExtension((Operator) f).simplify(simplifyTypes);
            Expression expectedResult = Expression.build("((1+2*exp(x^3))*(1+2*x)*exp(x^3)+x)/(1+2*exp(x^3))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotAlgebraicallyIntegrableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void integrateByRischAlgorithmPolynomialAndFractionalPartExponentialCaseTest2() {
        /* 
        int((x-2*x^3+2*(1-x^2)*exp(x^2)+2*x*exp(2*x^2))/(x*exp(x^2)+exp(2*x^2)),x)
        = x*exp(-x^2)+ln(x+exp(x^2)) gemäß dem Risch-Algorithmus.
         */
        try {
            f = Expression.build("int((x-2*x^3+2*(1-x^2)*exp(x^2)+2*x*exp(2*x^2))/(x*exp(x^2)+exp(2*x^2)),x)", null);
            // simplify(...) macht den Ausdruck ein wenig schöner.
            Expression integral = RischAlgorithmMethods.integrateByRischAlgorithmForTranscendentalExtension((Operator) f).simplify(simplifyTypes);
            Expression expectedResult = Expression.build("x*exp(-x^2)+ln(x+exp(x^2))", null);
            TestUtilities.printResult(expectedResult, integral);
            Assert.assertTrue(integral.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotAlgebraicallyIntegrableException e) {
            fail(e.getMessage());
        }
    }

}
