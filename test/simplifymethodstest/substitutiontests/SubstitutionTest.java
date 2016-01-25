package simplifymethodstest.substitutiontests;

import exceptions.EvaluationException;
import exceptions.ExpressionException;
import exceptions.NotSubstitutableException;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.ONE;
import abstractexpressions.expression.classes.Variable;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import substitutionmethods.SubstitutionUtilities;

public class SubstitutionTest {

    Expression f, subst, expectedResult, substVar;
    String var;
    
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
    public void substitutionTest1() {
        try {
            /* 
             f = 3*x^2+3*sin(x)+(x^2+sin(x))^5+7, var = "x", subst = x^2+sin(x).
            Dann ist f = 3*X_1+X_1^5+7, X_1 = subst.
             */
            f = Expression.build("3*x^2+3*sin(x)+(x^2+sin(x))^5+7", null);
            subst = Expression.build("x^2+sin(x)", null);
            var = "x";
            substVar = Variable.create(SubstitutionUtilities.getSubstitutionVariable(f));
            f = SubstitutionUtilities.substitute(f, var, subst);
            expectedResult = new Constant(7).add(new Constant(3).mult(substVar)).add(substVar.pow(5));
            assertTrue(f.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotSubstitutableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void substitutionTest2() {
        try {
            /* 
             f = (8+x/5+exp(4*x-(2*x^2+4)))-(x^2/10+1/5), var = "x", subst = 2*x-(x^2+2).
            Dann ist f = 8+X_1/10+exp(2*X_1), X_1 = subst.
             */
            f = Expression.build("(8+x/5+exp(4*x-(2*x^2+4)))-(x^2/10+1/5)", null);
            subst = Expression.build("2*x-(x^2+2)", null);
            var = "x";
            substVar = Variable.create(SubstitutionUtilities.getSubstitutionVariable(f));
            f = SubstitutionUtilities.substitute(f, var, subst).simplify();
            expectedResult = new Constant(8).add(substVar.div(10)).add(new Constant(2).mult(substVar).exp());
            assertTrue(f.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotSubstitutableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void substitutionTest3() {
        try {
            /* 
             f = x^10, var = "x", subst = 2*x^5.
            Dann ist f = X_1^2/4, X_1 = subst.
             */
            f = Expression.build("x^10", null);
            subst = Expression.build("2*x^5", null);
            var = "x";
            substVar = Variable.create(SubstitutionUtilities.getSubstitutionVariable(f));
            f = SubstitutionUtilities.substitute(f, var, subst);
            expectedResult = substVar.pow(2).div(new Constant(2).pow(2));
            assertTrue(f.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotSubstitutableException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void substitutionTest4() {
        try {
            /* 
             f = a*x^3*sin(x)^6+x*sin(x)^2, var = "x", subst = x*sin(x)^2.
            Dann ist f = a*X_1^3+X_1, X_1 = subst.
             */
            f = Expression.build("a*x^3*sin(x)^6+x*sin(x)^2", null);
            subst = Expression.build("x*sin(x)^2", null);
            var = "x";
            substVar = Variable.create(SubstitutionUtilities.getSubstitutionVariable(f));
            f = SubstitutionUtilities.substitute(f, var, subst);
            expectedResult = Variable.create("a").mult(substVar.pow(3)).add(substVar);
            assertTrue(f.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotSubstitutableException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void substitutionTest5() {
        try {
            /* 
             f = 2+sin(x^2+x+5)/7, var = "x", subst = cosec(5+x+x^2).
            Dann ist f = 2+1/(7*X_1), X_1 = subst.
             */
            f = Expression.build("2+sin(x^2+x+5)/7", null);
            subst = Expression.build("cosec(5+x+x^2)", null);
            var = "x";
            substVar = Variable.create(SubstitutionUtilities.getSubstitutionVariable(f));
            f = SubstitutionUtilities.substitute(f, var, subst).simplify();
            expectedResult = new Constant(2).add(ONE.div(new Constant(7).mult(substVar)));
            assertTrue(f.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotSubstitutableException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void substitutionTest6() {
        try {
            /* 
             f = exp(8*x+5), var = "x", subst = exp(4*x+1).
            Dann ist f = exp(3)*X_1^2, X_1 = subst.
             */
            f = Expression.build("exp(8*x+5)", null);
            subst = Expression.build("exp(4*x+1)", null);
            var = "x";
            substVar = Variable.create(SubstitutionUtilities.getSubstitutionVariable(f));
            f = SubstitutionUtilities.substitute(f, var, subst).simplify();
            expectedResult = new Constant(3).exp().mult(substVar.pow(2));
            assertTrue(f.equivalent(expectedResult));
        } catch (ExpressionException | EvaluationException | NotSubstitutableException e) {
            fail(e.getMessage());
        }
    }
    
}
