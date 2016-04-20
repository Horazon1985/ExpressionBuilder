package expression.computationtests;

import abstractexpressions.expression.classes.Constant;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.THREE;
import static abstractexpressions.expression.classes.Expression.TWO;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisMethods;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisMethods.Monomial;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisMethods.MultiPolynomial;
import exceptions.EvaluationException;
import java.math.BigDecimal;
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
    public void getLeadingMonomialWithRespectToLexTest1() {
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 5), new Monomial(TWO.div(THREE), 3, 1), new Monomial(ONE, 1, 0));
        Monomial leadingMonomial = f.getLeadingMonomial();
        assertTrue(leadingMonomial.equalsToMonomial(new Monomial(TWO.div(THREE), 3, 1)));
    }

    @Test
    public void getLeadingMonomialWithRespectToLexTest2() {
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.REVLEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 5), new Monomial(TWO.div(THREE), 3, 1), new Monomial(ONE, 1, 0));
        Monomial leadingMonomial = f.getLeadingMonomial();
        assertTrue(leadingMonomial.equalsToMonomial(new Monomial(ONE, 1, 0)));
    }

    @Test
    public void clearZeroMonomialsTest1() {
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 5), new Monomial(ZERO, 3, 1), new Monomial(ONE, 1, 0));
        f.clearZeroMonomials();
        assertTrue(f.equalsToMultiPolynomial(new MultiPolynomial(new Monomial(TWO, 2, 5), new Monomial(ONE, 1, 0))));
    }

    @Test
    public void clearZeroMonomialsTest2() {
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(ZERO, 2, 5), new Monomial(ZERO, 3, 1), new Monomial(ZERO, 1, 0));
        f.clearZeroMonomials();
        assertTrue(f.isZero());
    }

    @Test
    public void syzygyPolynomialTest() {
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(THREE, 2, 1), new Monomial(ONE, 1, 1), new Monomial(MINUS_ONE.div(TWO), 1, 0));
        MultiPolynomial g = new MultiPolynomial(new Monomial(ONE.div(TWO), 1, 2), new Monomial(MINUS_ONE, 0, 1));
        try {
            MultiPolynomial syzygyPolynomial = GroebnerBasisMethods.getSyzygyPolynomial(f, g);
            MultiPolynomial expectedResult = new MultiPolynomial(new Monomial(ONE.div(THREE), 1, 2), new Monomial(new Constant(11).div(6), 1, 1));
            assertTrue(syzygyPolynomial.equalsToMultiPolynomial(expectedResult));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reducePolynomialTest1() {
        // f = 2x^3y^2 - 5x^2y + xy + x, g = 1/2*x^2y + y. Reduziertes Polynom f mittels g = xy + x - 4xy^2 + 10y
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 3, 2), new Monomial(new Constant(-5), 2, 1),
                new Monomial(ONE, 1, 1), new Monomial(ONE, 1, 0));
        MultiPolynomial reductionPolynomial = new MultiPolynomial(new Monomial(ONE.div(TWO), 2, 1), new Monomial(ONE, 0, 1));
        try {
            MultiPolynomial reducedPolynomial = GroebnerBasisMethods.reduce(f, reductionPolynomial);
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
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 3), new Monomial(ONE, 1, 2),
                new Monomial(new Constant(6), 3, 1), new Monomial(THREE, 2, 0),
                new Monomial(MINUS_ONE, 0, 3), new Monomial(new Constant(-2), 1, 1));
        MultiPolynomial reductionPolynomial = new MultiPolynomial(new Monomial(TWO, 2, 1), new Monomial(ONE, 1, 0), new Monomial(MINUS_ONE, 0, 1));
        try {
            MultiPolynomial reducedPolynomial = GroebnerBasisMethods.reduce(f, reductionPolynomial);
            MultiPolynomial expectedResult = new MultiPolynomial(new Monomial(ONE, 1, 1));
            assertTrue(reducedPolynomial.equalsToMultiPolynomial(expectedResult));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reducePolynomialToZeroTest() {
        // f = 2x^2y^3+xy^2+6x^3y+3x^2-(y^3+3xy), g = 2x^2y + x - y. Reduziertes Polynom f mittels g = 0
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 3), new Monomial(ONE, 1, 2),
                new Monomial(new Constant(6), 3, 1), new Monomial(THREE, 2, 0),
                new Monomial(MINUS_ONE, 0, 3), new Monomial(new Constant(-3), 1, 1));
        MultiPolynomial reductionPolynomial = new MultiPolynomial(new Monomial(TWO, 2, 1), new Monomial(ONE, 1, 0), new Monomial(MINUS_ONE, 0, 1));
        try {
            MultiPolynomial reducedPolynomial = GroebnerBasisMethods.reduce(f, reductionPolynomial);
            assertTrue(reducedPolynomial.isZero());
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void reducePolynomialNotPossibleTest() {
        // f = 2x^2y^3+xy^2+6x^3y+3x^2-(y^3+3xy), g = 2x^4y + y. Reduziertes Polynom f mittels g = 0
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 2, 3), new Monomial(ONE, 1, 2),
                new Monomial(new Constant(6), 3, 1), new Monomial(new Constant(-3), 2, 0),
                new Monomial(MINUS_ONE, 0, 3), new Monomial(THREE, 1, 1));
        MultiPolynomial reductionPolynomial = new MultiPolynomial(new Monomial(ONE, 4, 1), new Monomial(ONE, 0, 1));
        try {
            MultiPolynomial reducedPolynomial = GroebnerBasisMethods.reduce(f, reductionPolynomial);
            MultiPolynomial expectedResult = f;
            assertTrue(reducedPolynomial.equalsToMultiPolynomial(expectedResult));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getGroebnerBasisTest1() {
        // f = 2xy + 3x + 1, g = x - y^2 + 2 bzgl. LEX. Gröbnerbasis = {y^3 + 3/2*y^2 - 2y - 5/2, x - y^2 + 2}
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, 1, 1), new Monomial(THREE, 1, 0),
                new Monomial(ONE, 0, 0));
        MultiPolynomial g = new MultiPolynomial(new Monomial(ONE, 1, 0), new Monomial(MINUS_ONE, 0, 2),
                new Monomial(TWO, 0, 0));
        try {
            ArrayList<MultiPolynomial> groebnerBasis = GroebnerBasisMethods.getNormalizedReducedGroebnerBasis(f, g);
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

}
