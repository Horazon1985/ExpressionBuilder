package simplifymethodstest;

import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.TypeFunction;
import expressionbuilder.Variable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import expressionsimplifymethods.ExpressionCollection;

public class ExpressionCollectionTest {
    
    Expression f, g, h;
    
    public ExpressionCollectionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void defineExpressions() {
        // f = 1*x*sin(x)
        f = new Constant(1).mult(Variable.create("x")).mult(new Function(Variable.create("x"), TypeFunction.sin));
        // g = x*1*sin(x)
        g = Variable.create("x").mult(new Constant(1)).mult(new Function(Variable.create("x"), TypeFunction.sin));
        // h = 1*1*1
        h = new Constant(1).mult(new Constant(1)).mult(new Constant(1));
    }
 
    @Test
    public void getMaxIndexTest() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.add(f);
        terms.add(g);
        terms.add(h);
        terms.add(f);
        assertTrue(terms.getBound() == 4);
    }
    
    @Test
    public void removeTest1() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.add(f);
        terms.add(g);
        terms.add(h);
        terms.add(f);
        terms.remove(3);
        assertTrue(terms.getBound() == 3);
    }
    
    @Test
    public void removeTest2() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.add(f);
        terms.add(g);
        terms.add(h);
        terms.add(f);
        terms.remove(1);
        assertTrue(terms.getBound() == 4);
    }

    @Test
    public void addTest1() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.add(Variable.create("a"));
        terms.add(Variable.create("b"));
        terms.add(Variable.create("c"));
        terms.add(Variable.create("d"));
        terms.add(Variable.create("e"));
        terms.remove(4);
        terms.remove(3);
        terms.remove(2);
        terms.add(Variable.create("f"));
        assertTrue(terms.getBound() == 3);
        assertTrue(terms.get(0) != null);
        assertTrue(terms.get(1) != null);
        assertTrue(terms.get(2) != null);
        assertFalse(terms.get(3) != null);
        assertFalse(terms.get(4) != null);
        assertFalse(terms.get(5) != null);
    }
    
    @Test
    public void addTest2() {
        ExpressionCollection terms = new ExpressionCollection();
        terms.add(Variable.create("a"));
        terms.add(Variable.create("b"));
        terms.add(Variable.create("c"));
        terms.add(Variable.create("d"));
        terms.add(Variable.create("e"));
        terms.remove(3);
        terms.add(Variable.create("f"));
        assertTrue(terms.getBound() == 6);
        assertTrue(terms.get(0) != null);
        assertTrue(terms.get(1) != null);
        assertTrue(terms.get(2) != null);
        assertFalse(terms.get(3) != null);
        assertTrue(terms.get(4) != null);
        assertTrue(terms.get(5) != null);
    }


}
