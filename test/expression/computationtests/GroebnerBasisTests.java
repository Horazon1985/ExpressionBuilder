package expression.computationtests;

import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.THREE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisUtils;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisUtils.Monomial;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisUtils.MultiPolynomial;
import abstractexpressions.expression.basic.ExpressionCollection;
import abstractexpressions.expression.basic.SimplifyMultiPolynomialUtils;
import exceptions.EvaluationException;
import exceptions.ExpressionException;
import java.math.BigInteger;
import java.util.ArrayList;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GroebnerBasisTests {

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
    public void getMultiPolynomialFromExpressionTest1() {
        try {
            Expression f = Expression.build("7*x^2*y-z^5/a");
            ArrayList<String> vars = new ArrayList<>();
            vars.add("x");
            vars.add("y");
            vars.add("z");
            MultiPolynomial fAsMultiPolynomial = SimplifyMultiPolynomialUtils.getMultiPolynomialFromExpression(f, vars);
            assertTrue(fAsMultiPolynomial.equalsToMultiPolynomial(new MultiPolynomial(new Monomial(new Constant(7), 2, 1, 0), new Monomial(MINUS_ONE.div(Variable.create("a")), 0, 0, 5))));
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getMultiPolynomialFromExpressionTest2() {
        try {
            Expression f = Expression.build("0");
            ArrayList<String> vars = new ArrayList<>();
            vars.add("x");
            vars.add("y");
            vars.add("z");
            MultiPolynomial fAsMultiPolynomial = SimplifyMultiPolynomialUtils.getMultiPolynomialFromExpression(f, vars);
            assertTrue(fAsMultiPolynomial.isZero());
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getMultiPolynomialFromExpressionIfNotMultiPolynomialTest() {
        try {
            Expression f = Expression.build("x^2*y+sin(z)");
            ArrayList<String> vars = new ArrayList<>();
            vars.add("x");
            vars.add("y");
            vars.add("z");
            MultiPolynomial fAsMultiPolynomial = SimplifyMultiPolynomialUtils.getMultiPolynomialFromExpression(f, vars);
            assertTrue(fAsMultiPolynomial.isZero());
        } catch (ExpressionException | EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void multiPolynomialToPolynomialTest1() {
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 5), new Monomial(TWO.div(THREE), 3, 1), new Monomial(ONE, 2, 0));
        ExpressionCollection coefficients = f.toPolynomial("x");
        assertTrue(coefficients.getBound() == 4);
        assertTrue(coefficients.get(0) == ZERO);
        assertTrue(coefficients.get(1) == ZERO);
        assertTrue(coefficients.get(2).equals(TWO.mult(Variable.create("y").pow(5)).add(ONE)));
        assertTrue(coefficients.get(3).equals(TWO.div(THREE).mult(Variable.create("y"))));
    }

    @Test
    public void multiPolynomialToPolynomialTest2() {
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 5), new Monomial(TWO.div(THREE), 3, 1), new Monomial(ONE, 2, 0));
        ExpressionCollection coefficients = f.toPolynomial("z");
        assertTrue(coefficients.getBound() == 1);
        assertTrue(coefficients.get(0).equals(f.toExpression()));
    }

    @Test
    public void getLeadingMonomialWithRespectToLexTest1() {
        GroebnerBasisUtils.setTermOrdering(GroebnerBasisUtils.TermOrderings.LEX);
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 5), new Monomial(TWO.div(THREE), 3, 1), new Monomial(ONE, 1, 0));
        Monomial leadingMonomial = f.getLeadingMonomial();
        assertTrue(leadingMonomial.equalsToMonomial(new Monomial(TWO.div(THREE), 3, 1)));
    }

    @Test
    public void getLeadingMonomialWithRespectToLexTest2() {
        GroebnerBasisUtils.setTermOrdering(GroebnerBasisUtils.TermOrderings.REVLEX);
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 5), new Monomial(TWO.div(THREE), 3, 1), new Monomial(ONE, 1, 0));
        Monomial leadingMonomial = f.getLeadingMonomial();
        assertTrue(leadingMonomial.equalsToMonomial(new Monomial(ONE, 1, 0)));
    }

    @Test
    public void clearZeroMonomialsTest1() {
        GroebnerBasisUtils.setTermOrdering(GroebnerBasisUtils.TermOrderings.LEX);
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 5), new Monomial(ZERO, 3, 1), new Monomial(ONE, 1, 0));
        f.clearZeroMonomials();
        assertTrue(f.equalsToMultiPolynomial(new MultiPolynomial(new Monomial(TWO, 2, 5), new Monomial(ONE, 1, 0))));
    }

    @Test
    public void clearZeroMonomialsTest2() {
        GroebnerBasisUtils.setTermOrdering(GroebnerBasisUtils.TermOrderings.LEX);
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(ZERO, 2, 5), new Monomial(ZERO, 3, 1), new Monomial(ZERO, 1, 0));
        f.clearZeroMonomials();
        assertTrue(f.isZero());
    }

    @Test
    public void syzygyPolynomialTest() {
        GroebnerBasisUtils.setTermOrdering(GroebnerBasisUtils.TermOrderings.LEX);
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(THREE, 2, 1), new Monomial(ONE, 1, 1), new Monomial(MINUS_ONE.div(TWO), 1, 0));
        MultiPolynomial g = new MultiPolynomial(new Monomial(ONE.div(TWO), 1, 2), new Monomial(MINUS_ONE, 0, 1));
        try {
            MultiPolynomial syzygyPolynomial = GroebnerBasisUtils.getSyzygyPolynomial(f, g);
            MultiPolynomial expectedResult = new MultiPolynomial(new Monomial(ONE.div(THREE), 1, 2), new Monomial(new Constant(11).div(6), 1, 1));
            assertTrue(syzygyPolynomial.equalsToMultiPolynomial(expectedResult));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reducePolynomialTest1() {
        // f = 2x^3y^2 - 5x^2y + xy + x, g = 1/2*x^2y + y. Reduziertes Polynom f mittels g = xy + x - 4xy^2 + 10y
        GroebnerBasisUtils.setTermOrdering(GroebnerBasisUtils.TermOrderings.LEX);
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 3, 2), new Monomial(new Constant(-5), 2, 1),
                new Monomial(ONE, 1, 1), new Monomial(ONE, 1, 0));
        MultiPolynomial reductionPolynomial = new MultiPolynomial(new Monomial(ONE.div(TWO), 2, 1), new Monomial(ONE, 0, 1));
        try {
            MultiPolynomial reducedPolynomial = GroebnerBasisUtils.reduce(f, reductionPolynomial);
            MultiPolynomial expectedResult = new MultiPolynomial(new Monomial(ONE, 1, 1), new Monomial(ONE, 1, 0),
                    new Monomial(new Constant(-4), 1, 2), new Monomial(new Constant(10), 0, 1));
            assertTrue(reducedPolynomial.equalsToMultiPolynomial(expectedResult));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reducePolynomialTest2() {
        // f = 2x^2y^3+xy^2+6x^3y+3x^2-(y^3+2xy), g = 2x^2y + x - y. Reduziertes Polynom f mittels g = xy
        GroebnerBasisUtils.setTermOrdering(GroebnerBasisUtils.TermOrderings.LEX);
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 3), new Monomial(ONE, 1, 2),
                new Monomial(new Constant(6), 3, 1), new Monomial(THREE, 2, 0),
                new Monomial(MINUS_ONE, 0, 3), new Monomial(new Constant(-2), 1, 1));
        MultiPolynomial reductionPolynomial = new MultiPolynomial(new Monomial(TWO, 2, 1), new Monomial(ONE, 1, 0), new Monomial(MINUS_ONE, 0, 1));
        try {
            MultiPolynomial reducedPolynomial = GroebnerBasisUtils.reduce(f, reductionPolynomial);
            MultiPolynomial expectedResult = new MultiPolynomial(new Monomial(ONE, 1, 1));
            assertTrue(reducedPolynomial.equalsToMultiPolynomial(expectedResult));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reducePolynomialToZeroTest() {
        // f = 2x^2y^3+xy^2+6x^3y+3x^2-(y^3+3xy), g = 2x^2y + x - y. Reduziertes Polynom f mittels g = 0
        GroebnerBasisUtils.setTermOrdering(GroebnerBasisUtils.TermOrderings.LEX);
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 3), new Monomial(ONE, 1, 2),
                new Monomial(new Constant(6), 3, 1), new Monomial(THREE, 2, 0),
                new Monomial(MINUS_ONE, 0, 3), new Monomial(new Constant(-3), 1, 1));
        MultiPolynomial reductionPolynomial = new MultiPolynomial(new Monomial(TWO, 2, 1), new Monomial(ONE, 1, 0), new Monomial(MINUS_ONE, 0, 1));
        try {
            MultiPolynomial reducedPolynomial = GroebnerBasisUtils.reduce(f, reductionPolynomial);
            assertTrue(reducedPolynomial.isZero());
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reducePolynomialNotPossibleTest() {
        // f = 2x^2y^3+xy^2+6x^3y+3x^2-(y^3+3xy), g = 2x^4y + y. Reduziertes Polynom f mittels g = 0
        GroebnerBasisUtils.setTermOrdering(GroebnerBasisUtils.TermOrderings.LEX);
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 3), new Monomial(ONE, 1, 2),
                new Monomial(new Constant(6), 3, 1), new Monomial(new Constant(-3), 2, 0),
                new Monomial(MINUS_ONE, 0, 3), new Monomial(THREE, 1, 1));
        MultiPolynomial reductionPolynomial = new MultiPolynomial(new Monomial(ONE, 4, 1), new Monomial(ONE, 0, 1));
        try {
            MultiPolynomial reducedPolynomial = GroebnerBasisUtils.reduce(f, reductionPolynomial);
            MultiPolynomial expectedResult = f;
            assertTrue(reducedPolynomial.equalsToMultiPolynomial(expectedResult));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getGroebnerBasisTest1() {
        // f = 2xy + 3x + 1, g = x - y^2 + 2 bzgl. LEX. Gröbnerbasis = {y^3 + 3/2*y^2 - 2y - 5/2, x - y^2 + 2}
        GroebnerBasisUtils.setTermOrdering(GroebnerBasisUtils.TermOrderings.LEX);
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 1, 1), new Monomial(THREE, 1, 0),
                new Monomial(ONE, 0, 0));
        MultiPolynomial g = new MultiPolynomial(new Monomial(ONE, 1, 0), new Monomial(MINUS_ONE, 0, 2),
                new Monomial(TWO, 0, 0));
        try {
            ArrayList<MultiPolynomial> groebnerBasis = GroebnerBasisUtils.getNormalizedReducedGroebnerBasis(f, g);
            MultiPolynomial groebnerBasisElementOne = new MultiPolynomial(new Monomial(ONE, 1, 0), new Monomial(MINUS_ONE, 0, 2),
                    new Monomial(TWO, 0, 0));
            MultiPolynomial groebnerBasisElementTwo = new MultiPolynomial(new Monomial(ONE, 0, 3), new Monomial(THREE.div(TWO), 0, 2),
                    new Monomial(new Constant(-2), 0, 1), new Monomial(new Constant(-5).div(2), 0, 0));
            assertTrue(groebnerBasis.size() == 2);
            assertTrue(groebnerBasis.get(0).equivalentToMultiPolynomial(groebnerBasisElementOne));
            assertTrue(groebnerBasis.get(1).equivalentToMultiPolynomial(groebnerBasisElementTwo));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getGroebnerBasisTest2() {
        // f = x^2 + xy - 10, g = y^2 + 5xy - 39 bzgl. LEX. Gröbnerbasis = {y^4 + 133/4*y^2 - 1521/4, x - 4/195y^3 - 94/195y}
        GroebnerBasisUtils.setTermOrdering(GroebnerBasisUtils.TermOrderings.LEX);
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(ONE, 2, 0), new Monomial(ONE, 1, 1),
                new Monomial(new Constant(-10), 0, 0));
        MultiPolynomial g = new MultiPolynomial(new Monomial(ONE, 0, 2), new Monomial(new Constant(5), 1, 1),
                new Monomial(new Constant(-39), 0, 0));
        try {
            ArrayList<MultiPolynomial> groebnerBasis = GroebnerBasisUtils.getNormalizedReducedGroebnerBasis(f, g);
            MultiPolynomial groebnerBasisElementTwo = new MultiPolynomial(new Monomial(ONE, 0, 4), new Monomial(new Constant(133).div(4), 0, 2),
                    new Monomial(new Constant(-1521).div(4), 0, 0));
            MultiPolynomial groebnerBasisElementOne = new MultiPolynomial(new Monomial(ONE, 1, 0), new Monomial(new Constant(-4).div(195), 0, 3),
                    new Monomial(new Constant(-94).div(195), 0, 1));
            assertTrue(groebnerBasis.size() == 2);
            assertTrue(groebnerBasis.get(0).equivalentToMultiPolynomial(groebnerBasisElementOne));
            assertTrue(groebnerBasis.get(1).equivalentToMultiPolynomial(groebnerBasisElementTwo));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getGroebnerBasisTest3() {
        // f = x^3*y^2 + x*y - 78, g = -y + x^2*y^3 - 105 bzgl. LEX. Gröbnerbasis = {y^4 + 133/4*y^2 - 1521/4, x - 4/195y^3 - 94/195y}
        GroebnerBasisUtils.setTermOrdering(GroebnerBasisUtils.TermOrderings.LEX);
        GroebnerBasisUtils.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(ONE, 3, 2), new Monomial(ONE, 1, 1),
                new Monomial(new Constant(-78), 0, 0));
        MultiPolynomial g = new MultiPolynomial(new Monomial(MINUS_ONE, 0, 1), new Monomial(ONE, 2, 3),
                new Monomial(new Constant(-105), 0, 0));
        try {
            ArrayList<MultiPolynomial> groebnerBasis = GroebnerBasisUtils.getNormalizedReducedGroebnerBasis(f, g);
            MultiPolynomial groebnerBasisElementOne = new MultiPolynomial(new Monomial(new Constant(-67075577).div(90294750), 0, 1),
                    new Monomial(new Constant(32992312).div(BigInteger.valueOf(4740474375L)), 0, 2),
                    new Monomial(new Constant(-181621).div(BigInteger.valueOf(1354421250)), 0, 4),
                    new Monomial(new Constant(66448723).div(BigInteger.valueOf(9480948750L)), 0, 3),
                    new Monomial(ONE, 1, 0),
                    new Monomial(new Constant(-10711).div(BigInteger.valueOf(859950)), 0, 0));
            MultiPolynomial groebnerBasisElementTwo = new MultiPolynomial(new Monomial(new Constant(-4725).div(869), 0, 1),
                    new Monomial(new Constant(-165375).div(869), 0, 0),
                    new Monomial(ONE, 0, 5),
                    new Monomial(new Constant(-107).div(6083), 0, 4),
                    new Monomial(new Constant(-421).div(6083), 0, 3),
                    new Monomial(new Constant(-3195).div(869), 0, 2));
            assertTrue(groebnerBasis.size() == 2);
            assertTrue(groebnerBasis.get(0).equivalentToMultiPolynomial(groebnerBasisElementOne));
            assertTrue(groebnerBasis.get(1).equivalentToMultiPolynomial(groebnerBasisElementTwo));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

}
