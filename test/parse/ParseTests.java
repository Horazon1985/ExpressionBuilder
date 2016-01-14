package parse;

import exceptions.ExpressionException;
import expressionbuilder.Expression;
import expressionbuilder.Operator;
import expressionbuilder.TypeOperator;
import java.util.ArrayList;
import operationparser.OperationParser;
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
import utilities.TestUtilities;

public class ParseTests {

    private ParseResultPattern resultPattern;
    private ParameterPattern p;
    private ArrayList<String> restrictions;

    private Operator operator;
    private Operator expectedOperator;
    private String patternForOperator;

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

    // Teil 1: Tests für das Parsen eines Pattern.
    @Test
    public void parseTest1() {

        // Parsen von "op(expr, var, int, expr)".
        resultPattern = OperationParser.getResultPattern("op(expr, var, int, expr)");
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
        resultPattern = OperationParser.getResultPattern("op()");
        Assert.assertTrue(resultPattern.getOperationName().equals("op"));
        Assert.assertTrue(resultPattern.size() == 0);
    }

    @Test
    public void parseTest3() {

        // Parsen von "op(expr(none, 2), var(0), int(3,5), expr(3,none))".
        resultPattern = OperationParser.getResultPattern("op(expr(none, 2), var(0), int(3,5), expr(3,none))");
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
    public void parseTest4() {
        // Parsen von "op(expr(none, 2), var(!0))".
        resultPattern = OperationParser.getResultPattern("op(expr(none, 2), var+(!0))");
        Assert.assertTrue(resultPattern.getOperationName().equals("op"));

        Assert.assertTrue(resultPattern.size() == 2);

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
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.plus));
        Assert.assertTrue(restrictions.size() == 1);
        Assert.assertTrue(restrictions.get(0).equals("!0"));
    }

    @Test
    public void parseTest5() {
        // Parsen von "op(expr, expr, var(0,!1, 4), expr, expr, expr)".
        resultPattern = OperationParser.getResultPattern("op(expr, expr, var(0,!1, 4), expr, expr, expr)");
        Assert.assertTrue(resultPattern.getOperationName().equals("op"));

        Assert.assertTrue(resultPattern.size() == 6);

        for (int i = 0; i < 6 && i != 2; i++) {
            p = resultPattern.getParameterPattern(i);
            restrictions = p.getRestrictions();
            Assert.assertTrue(p.getParamType().equals(ParamType.expr));
            Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
            Assert.assertTrue(restrictions.isEmpty());
        }

        p = resultPattern.getParameterPattern(2);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.var));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
        Assert.assertTrue(restrictions.size() == 3);
        Assert.assertTrue(restrictions.get(0).equals("0"));
        Assert.assertTrue(restrictions.get(1).equals("!1"));
        Assert.assertTrue(restrictions.get(2).equals("4"));
    }

    @Test
    public void parseTest6() {
        // Parsen von "op(expr+(none, 2), var+(0,0,2), expr)".
        resultPattern = OperationParser.getResultPattern("op(expr+(none, 2), var+(0,0,2), expr)");
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
    public void parseTest7() {
        // Parsen von "(expr, int)".
        try {
            resultPattern = OperationParser.getResultPattern("(expr, int)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ParseException e) {
        }
    }

    @Test
    public void parseTest8() {
        // Parsen von "op(expr(none, 2), var(!2), int)".
        try {
            resultPattern = OperationParser.getResultPattern("op(expr(none, 2), var(!2), int)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ParseException e) {
        }
    }

    @Test
    public void parseTest9() {
        // Parsen von "op(expr(none, 2), var(1), int)".
        try {
            resultPattern = OperationParser.getResultPattern("op(expr(none, 2), var(1), int)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ParseException e) {
        }
    }

    @Test
    public void parseTest10() {
        // Parsen von "op(expr,)".
        try {
            resultPattern = OperationParser.getResultPattern("op(expr,)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ParseException e) {
        }
    }

    @Test
    public void parseTest11() {
        // Parsen von "op(expr,)".
        try {
            resultPattern = OperationParser.getResultPattern("op(expr,+)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ParseException e) {
        }
    }

    // Teil 2: Tests für das Parsen eines Operators.
    @Test
    public void parseOperatorSuccesfullyParsedTest1() {
        // Parsen von "diff(x^2+y,x,y,x)" gegen das Pattern "diff(expr,var+)".
        try {
            patternForOperator = "diff(expr,var+)";
            operator = OperationParser.parseDefaultOperator("diff(x^2+y,x,y,x)", null, patternForOperator);

            // Ausgabe der Ergebnisse.
            expectedOperator = new Operator(TypeOperator.diff, new Object[]{ Expression.build("x^2+y", null), "x", "y", "x" });
            TestUtilities.printResult(expectedOperator, operator);
            
            Assert.assertTrue(operator.getType().equals(TypeOperator.diff));
            Assert.assertTrue(operator.getParams().length == 4);
            Assert.assertTrue(((Expression) operator.getParams()[0]).equals(Expression.build("x^2+y", null)));
            Assert.assertTrue(((String) operator.getParams()[1]).equals("x"));
            Assert.assertTrue(((String) operator.getParams()[2]).equals("y"));
            Assert.assertTrue(((String) operator.getParams()[3]).equals("x"));

        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseOperatorSuccesfullyParsedTest2() {
        // Parsen von "diff(x^2+y,x,3)" gegen das Pattern "diff(expr,var,int)".
        try {
            patternForOperator = "diff(expr,var,int)";
            operator = OperationParser.parseDefaultOperator("diff(x^2+y,x,3)", null, patternForOperator);

            // Ausgabe der Ergebnisse.
            expectedOperator = new Operator(TypeOperator.diff, new Object[]{ Expression.build("x^2+y", null), "x", 3 });
            TestUtilities.printResult(expectedOperator, operator);
            
            Assert.assertTrue(operator.getType().equals(TypeOperator.diff));
            Assert.assertTrue(operator.getParams().length == 3);
            Assert.assertTrue(((Expression) operator.getParams()[0]).equals(Expression.build("x^2+y", null)));
            Assert.assertTrue(((String) operator.getParams()[1]).equals("x"));
            Assert.assertTrue(((Integer) operator.getParams()[2]).equals(3));

        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseOperatorSuccesfullyParsedTest3() {
        // Parsen von "var(x^2,3,t,sin(u),a+b)" gegen das Pattern "var(expr+)".
        try {
            patternForOperator = "var(expr+)";
            operator = OperationParser.parseDefaultOperator("var(x^2,3,t,sin(u),a+b)", null, patternForOperator);

            // Ausgabe der Ergebnisse.
            expectedOperator = new Operator(TypeOperator.var, new Object[]{ Expression.build("x^2", null), 
                Expression.build("3", null), Expression.build("t", null), Expression.build("sin(u)", null), 
                Expression.build("a+b", null) });
            TestUtilities.printResult(expectedOperator, operator);
            
            Assert.assertTrue(operator.getType().equals(TypeOperator.var));
            Assert.assertTrue(operator.getParams().length == 5);
            Assert.assertTrue(((Expression) operator.getParams()[0]).equals(Expression.build("x^2", null)));
            Assert.assertTrue(((Expression) operator.getParams()[1]).equals(Expression.build("3", null)));
            Assert.assertTrue(((Expression) operator.getParams()[2]).equals(Expression.build("t", null)));
            Assert.assertTrue(((Expression) operator.getParams()[3]).equals(Expression.build("sin(u)", null)));
            Assert.assertTrue(((Expression) operator.getParams()[4]).equals(Expression.build("a+b", null)));

        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

}
