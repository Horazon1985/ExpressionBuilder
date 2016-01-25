package expression.computationtests;

import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import enumerations.TypeSimplify;
import abstractexpressions.expression.classes.Variable;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimplifyOperatorTests {

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
    public void simplifyProductTest() {
        try {
            // Ausschreiben von Summen.
            Expression f = Expression.build("sum(x^k,k,1,5)", null);
            Expression varX = Variable.create("x");
            Expression g = varX.add(varX.pow(2).add(varX.pow(3).add(varX.pow(4).add(varX.pow(5)))));
            Expression fSimplified = f.simplify(TypeSimplify.order_sums_and_products, TypeSimplify.simplify_trivial);
            Assert.assertTrue(fSimplified.equals(g));
            fSimplified = f.simplify();
            Assert.assertTrue(fSimplified.equals(g));
            // Summen werden bei zu vielen Summanden nicht ausgeschrieben.
            f = new Operator(TypeOperator.sum, new Object[]{varX.pow(Variable.create("k")), "k",
                new Constant(1), new Constant(ComputationBounds.BOUND_OPERATOR_MAX_NUMBER_OF_MEMBERS_IN_SUM_OR_PRODUCT + 2)});
            fSimplified = f.simplify(TypeSimplify.order_sums_and_products, TypeSimplify.simplify_trivial);
            Assert.assertTrue(fSimplified.equals(f));
            fSimplified = f.simplify();
            Assert.assertTrue(fSimplified.equals(f));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void simplifySumTest() {
        try {
            // Ausschreiben von Produkten.
            Expression f = Expression.build("prod(sin(k),k,1,5)", null);
            Expression g = new Constant(1).sin().mult(new Constant(2).sin().mult(new Constant(3).sin().mult(
                    new Constant(4).sin().mult(new Constant(5).sin()))));
            Expression fSimplified = f.simplify(TypeSimplify.order_sums_and_products, TypeSimplify.simplify_trivial);
            Assert.assertTrue(fSimplified.equals(g));
            fSimplified = f.simplify();
            Assert.assertTrue(fSimplified.equals(g));
            // Produkte werden bei zu vielen faktoren nicht ausgeschrieben.
            f = new Operator(TypeOperator.prod, new Object[]{Variable.create("k").sin(), "k",
                new Constant(1), new Constant(ComputationBounds.BOUND_OPERATOR_MAX_NUMBER_OF_MEMBERS_IN_SUM_OR_PRODUCT + 2)});
            fSimplified = f.simplify(TypeSimplify.order_sums_and_products, TypeSimplify.simplify_trivial);
            Assert.assertTrue(fSimplified.equals(f));
            fSimplified = f.simplify();
            Assert.assertTrue(fSimplified.equals(f));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

}
