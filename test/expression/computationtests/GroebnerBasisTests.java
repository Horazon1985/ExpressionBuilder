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
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, new int[]{2, 5}), new Monomial(TWO.div(THREE), new int[]{3, 1}), new Monomial(ONE, new int[]{1, 0}));
        Monomial leadingMonomial = f.getLeadingMonomial();
        assertTrue(leadingMonomial.equalsToMonomial(new Monomial(TWO.div(THREE), new int[]{3, 1})));
    }

    @Test
    public void getLeadingMonomialWithRespectToLexTest2() {
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.REVLEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, new int[]{2, 5}), new Monomial(TWO.div(THREE), new int[]{3, 1}), new Monomial(ONE, new int[]{1, 0}));
        Monomial leadingMonomial = f.getLeadingMonomial();
        assertTrue(leadingMonomial.equalsToMonomial(new Monomial(ONE, new int[]{1, 0})));
    }

    @Test
    public void clearZeroMonomialsTest1() {
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(TWO, new int[]{2, 5}), new Monomial(ZERO, new int[]{3, 1}), new Monomial(ONE, new int[]{1, 0}));
        f.clearZeroMonomials();
        assertTrue(f.equalsToMultiPolynomial(new MultiPolynomial(new Monomial(TWO, new int[]{2, 5}), new Monomial(ONE, new int[]{1, 0}))));
    }

    @Test
    public void clearZeroMonomialsTest2() {
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(ZERO, new int[]{2, 5}), new Monomial(ZERO, new int[]{3, 1}), new Monomial(ZERO, new int[]{1, 0}));
        f.clearZeroMonomials();
        assertTrue(f.equalsToMultiPolynomial(new MultiPolynomial()));
    }

    @Test
    public void syzygyPolynomialTest() {
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        MultiPolynomial f = new MultiPolynomial(new Monomial(THREE, new int[]{2, 1}), new Monomial(ONE, new int[]{1, 1}), new Monomial(MINUS_ONE.div(TWO), new int[]{1, 0}));
        MultiPolynomial g = new MultiPolynomial(new Monomial(ONE.div(TWO), new int[]{1, 2}), new Monomial(MINUS_ONE, new int[]{0, 1}));
        try {
            MultiPolynomial syzygyPolynomial = GroebnerBasisMethods.getSyzygyPolynomial(f, g);
            MultiPolynomial expectedResult = new MultiPolynomial(new Monomial(ONE.div(THREE), new int[]{1, 2}), new Monomial(new Constant(11).div(6), new int[]{1, 1}));
            assertTrue(syzygyPolynomial.equalsToMultiPolynomial(expectedResult));
        } catch (EvaluationException e) {
            fail(e.getMessage());
        }
    }

}
