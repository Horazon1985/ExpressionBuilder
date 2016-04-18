package expression.computationtests;

import static abstractexpressions.expression.classes.Expression.ONE;
import static abstractexpressions.expression.classes.Expression.THREE;
import static abstractexpressions.expression.classes.Expression.TWO;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisMethods;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisMethods.Monomial;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
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
        GroebnerBasisMethods.MultiPolynomial f = new GroebnerBasisMethods.MultiPolynomial(new Monomial(TWO, new int[]{2, 5}), new Monomial(TWO.div(THREE), new int[]{3, 1}), new Monomial(ONE, new int[]{1, 0}));
        Monomial leadingMonomial = f.getLeadingMonomial();
        assertTrue(leadingMonomial.equalsToMonomial(new Monomial(TWO.div(THREE), new int[]{3, 1})));
    }

    @Test
    public void getLeadingMonomialWithRespectToLexTest2() {
        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.REVLEX);
        GroebnerBasisMethods.setMonomialVars(new String[]{"x", "y"});
        GroebnerBasisMethods.MultiPolynomial f = new GroebnerBasisMethods.MultiPolynomial(new Monomial(TWO, new int[]{2, 5}), new Monomial(TWO.div(THREE), new int[]{3, 1}), new Monomial(ONE, new int[]{1, 0}));
        Monomial leadingMonomial = f.getLeadingMonomial();
        assertTrue(leadingMonomial.equalsToMonomial(new Monomial(ONE, new int[]{1, 0})));
    }

    
}
