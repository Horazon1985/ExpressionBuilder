package parse;

import operationparser.ParseResultPattern;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParseTests {

    private ParseResultPattern resultPattern;
    
    public ParseTests() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void definePattern() {
    }

    @Test
    public void parseTest1() {
        // Parsen von "op(expr, var, int, expr)".
        resultPattern = operationparser.OperationParser.getResultPattern("op(expr, var, int, expr)");
        Assert.assertTrue(resultPattern.getOperationName().equals("op"));
    }
    
    @Test
    public void parseTest2() {
        // Parsen von "op()".
    }
    
    @Test
    public void parseTest3() {
        // Parsen von "(expr, int)".
    }
    
    @Test
    public void parseTest4() {
        // Parsen von "op(expr(none, 2), var(0), int(3,5), expr(3,none))".
        resultPattern = operationparser.OperationParser.getResultPattern("op(expr(none, 2), var(0), int(3,5), expr(3,none))");
        Assert.assertTrue(resultPattern.getOperationName().equals("op"));
    }
    
    @Test
    public void parseTest5() {
        // Parsen von "op(expr(none, 2), var(!0))".
    }

    @Test
    public void parseTest6() {
        // Parsen von "op(expr(none, 2), var(!2), int)".
    }

    @Test
    public void parseTest7() {
        // Parsen von "op(expr, expr, var(0,!1, 4), expr, expr, expr)".
    }
    
    @Test
    public void parseTest8() {
        // Parsen von "op(expr(none, 2), var(3), expr)".
    }
    
}
