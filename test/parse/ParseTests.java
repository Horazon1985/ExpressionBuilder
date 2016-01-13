package parse;

import java.util.ArrayList;
import operationparser.ParameterPattern;
import operationparser.ParameterPattern.Multiplicity;
import operationparser.ParameterPattern.ParamType;
import operationparser.ParseException;
import operationparser.ParseResultPattern;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParseTests {

    private ParseResultPattern resultPattern;
    private ParameterPattern p;
    private ArrayList<String> restrictions;

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

        Assert.assertTrue(resultPattern.size() == 4);

        p = resultPattern.getParameterPattern(0);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.expr));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
        Assert.assertTrue(restrictions.isEmpty());

        p = resultPattern.getParameterPattern(1);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.var));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
        Assert.assertTrue(restrictions.isEmpty());

        p = resultPattern.getParameterPattern(2);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.integer));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
        Assert.assertTrue(restrictions.isEmpty());

        p = resultPattern.getParameterPattern(3);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.expr));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
        Assert.assertTrue(restrictions.isEmpty());

    }

    @Test
    public void parseTest2() {
        // Parsen von "op()".
        resultPattern = operationparser.OperationParser.getResultPattern("op()");
        Assert.assertTrue(resultPattern.getOperationName().equals("op"));
        Assert.assertTrue(resultPattern.size() == 0);
    }

    @Test
    public void parseTest3() {
        // Parsen von "(expr, int)".
        try {
            resultPattern = operationparser.OperationParser.getResultPattern("(expr, int)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ParseException e) {
        }
    }

    @Test
    public void parseTest4() {

        // Parsen von "op(expr(none, 2), var(0), int(3,5), expr(3,none))".
        resultPattern = operationparser.OperationParser.getResultPattern("op(expr(none, 2), var(0), int(3,5), expr(3,none))");
        Assert.assertTrue(resultPattern.getOperationName().equals("op"));

        Assert.assertTrue(resultPattern.size() == 4);

        p = resultPattern.getParameterPattern(0);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.expr));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
        Assert.assertTrue(restrictions.size() == 2);
        Assert.assertTrue(restrictions.get(0).equals("none"));
        Assert.assertTrue(restrictions.get(1).equals("2"));

        p = resultPattern.getParameterPattern(1);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.var));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
        Assert.assertTrue(restrictions.size() == 1);
        Assert.assertTrue(restrictions.get(0).equals("0"));

        p = resultPattern.getParameterPattern(2);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.integer));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
        Assert.assertTrue(restrictions.size() == 2);
        Assert.assertTrue(restrictions.get(0).equals("3"));
        Assert.assertTrue(restrictions.get(1).equals("5"));

        p = resultPattern.getParameterPattern(3);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.expr));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
        Assert.assertTrue(restrictions.size() == 2);
        Assert.assertTrue(restrictions.get(0).equals("3"));
        Assert.assertTrue(restrictions.get(1).equals("none"));

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
        // Parsen von "op(expr+(none, 2), var+(0,0,2), expr)".
        resultPattern = operationparser.OperationParser.getResultPattern("op(expr+(none, 2), var+(0,0,2), expr)");
        Assert.assertTrue(resultPattern.getOperationName().equals("op"));

        Assert.assertTrue(resultPattern.size() == 3);

        p = resultPattern.getParameterPattern(0);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.expr));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.plus));
        Assert.assertTrue(restrictions.size() == 2);
        Assert.assertTrue(restrictions.get(0).equals("none"));
        Assert.assertTrue(restrictions.get(1).equals("2"));

        p = resultPattern.getParameterPattern(1);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.var));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.plus));
        Assert.assertTrue(restrictions.size() == 3);
        Assert.assertTrue(restrictions.get(0).equals("0"));
        Assert.assertTrue(restrictions.get(1).equals("0"));
        Assert.assertTrue(restrictions.get(2).equals("2"));

        p = resultPattern.getParameterPattern(2);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.expr));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
        Assert.assertTrue(restrictions.isEmpty());
    }

    @Test
    public void parseTest9() {
        // Parsen von "op(expr(none, 2), var(1), int)".
        try {
            resultPattern = operationparser.OperationParser.getResultPattern("op(expr(none, 2), var(1), int)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ParseException e) {
        }
    }

    @Test
    public void parseTest10() {
        // Parsen von "op(expr,)".
        try {
            resultPattern = operationparser.OperationParser.getResultPattern("op(expr,)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ParseException e) {
        }
    }
    
    @Test
    public void parseTest11() {
        // Parsen von "op(expr,)".
        try {
            resultPattern = operationparser.OperationParser.getResultPattern("op(expr,+)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ParseException e) {
        }
    }
    
}
