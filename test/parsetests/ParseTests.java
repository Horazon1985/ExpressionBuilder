package parsetests;

import exceptions.ExpressionException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Operator;
import abstractexpressions.expression.classes.TypeOperator;
import command.Command;
import command.TypeCommand;
import java.util.ArrayList;
import operationparser.OperationParser;
import operationparser.ParameterPattern;
import operationparser.ParameterPattern.Multiplicity;
import operationparser.ParameterPattern.ParamType;
import exceptions.ParseException;
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
    public void parseTest1() throws ExpressionException {

        // Parsen von "op(expr, indet, integer, expr)".
        resultPattern = OperationParser.getResultPattern("op(expr, indet, integer, expr)");
        Assert.assertTrue(resultPattern.getOperationName().equals("op"));

        Assert.assertTrue(resultPattern.size() == 4);

        p = resultPattern.getParameterPattern(0);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.expr));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
        Assert.assertTrue(restrictions.isEmpty());

        p = resultPattern.getParameterPattern(1);
        restrictions = p.getRestrictions();
        Assert.assertTrue(p.getParamType().equals(ParamType.indet));
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
    public void parseTest2() throws ExpressionException {
        // Parsen von "op()".
        resultPattern = OperationParser.getResultPattern("op()");
        Assert.assertTrue(resultPattern.getOperationName().equals("op"));
        Assert.assertTrue(resultPattern.size() == 0);
    }

    @Test
    public void parseTest3() throws ExpressionException {

        // Parsen von "op(expr(none, 2), indet(0), integer(3,5), expr(3,none))".
        resultPattern = OperationParser.getResultPattern("op(expr(none, 2), indet(0), integer(3,5), expr(3,none))");
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
        Assert.assertTrue(p.getParamType().equals(ParamType.indet));
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
    public void parseTest4() throws ExpressionException {
        // Parsen von "op(expr(none, 2), indet(!0))".
        resultPattern = OperationParser.getResultPattern("op(expr(none, 2), indet+(!0))");
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
        Assert.assertTrue(p.getParamType().equals(ParamType.indet));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.plus));
        Assert.assertTrue(restrictions.size() == 1);
        Assert.assertTrue(restrictions.get(0).equals("!0"));
    }

    @Test
    public void parseTest5() throws ExpressionException {
        // Parsen von "op(expr, expr, indet(0,!1, 4), expr, expr, expr)".
        resultPattern = OperationParser.getResultPattern("op(expr, expr, indet(0,!1, 4), expr, expr, expr)");
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
        Assert.assertTrue(p.getParamType().equals(ParamType.indet));
        Assert.assertTrue(p.getMultiplicity().equals(Multiplicity.one));
        Assert.assertTrue(restrictions.size() == 3);
        Assert.assertTrue(restrictions.get(0).equals("0"));
        Assert.assertTrue(restrictions.get(1).equals("!1"));
        Assert.assertTrue(restrictions.get(2).equals("4"));
    }

    @Test
    public void parseTest6() throws ExpressionException {
        // Parsen von "op(expr+(none, 2), indet+(0,0,2), expr)".
        resultPattern = OperationParser.getResultPattern("op(expr+(none, 2), indet+(0,0,2), expr)");
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
        Assert.assertTrue(p.getParamType().equals(ParamType.indet));
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
    public void parseTest7() throws ExpressionException {
        // Parsen von "(expr, integer)".
        try {
            resultPattern = OperationParser.getResultPattern("(expr, integer)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ExpressionException | ParseException e) {
        }
    }

    @Test
    public void parseTest8() throws ExpressionException {
        // Parsen von "op(expr(none, 2), indet(!2), integer)".
        try {
            resultPattern = OperationParser.getResultPattern("op(expr(none, 2), indet(!2), integer)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ParseException e) {
        }
    }

    @Test
    public void parseTest9() throws ExpressionException {
        // Parsen von "op(expr(none, 2), indet(1), integer)".
        try {
            resultPattern = OperationParser.getResultPattern("op(expr(none, 2), indet(1), integer)");
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
        } catch (ParseException | ExpressionException e) {
        }
    }

    @Test
    public void parseTest11() throws ExpressionException {
        // Parsen von "op(expr,)".
        try {
            resultPattern = OperationParser.getResultPattern("op(expr,+)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ParseException e) {
        }
    }

    @Test
    public void parseTest12() throws ExpressionException {
        // Parsen von "op(expres(1,2),indet)".
        try {
            resultPattern = OperationParser.getResultPattern("op(expres(1,2),indet)");
            // Es darf nicht erfolgreich geparst werden!
            fail();
        } catch (ParseException e) {
        }
    }

    // Teil 2: Tests für das Parsen eines Operators.
    @Test
    public void parseOperatorSuccesfullyParsedTest1() {
        // Parsen von "diff(x^2+y,x,y,x)" gegen das Pattern "diff(expr,indet+)".
        try {
            patternForOperator = "diff(expr,indet+)";
            operator = OperationParser.parseDefaultOperator("diff", new String[]{"x^2+y", "x", "y", "x"}, null, patternForOperator);

            // Ausgabe der Ergebnisse.
            expectedOperator = new Operator(TypeOperator.diff, new Object[]{Expression.build("x^2+y", null), "x", "y", "x"});
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
        // Parsen von "diff(x^2+y,x,3)" gegen das Pattern "diff(expr,indet,integer)".
        try {
            patternForOperator = "diff(expr,indet,integer)";
            operator = OperationParser.parseDefaultOperator("diff", new String[]{"x^2+y", "x", "3"}, null, patternForOperator);

            // Ausgabe der Ergebnisse.
            expectedOperator = new Operator(TypeOperator.diff, new Object[]{Expression.build("x^2+y", null), "x", 3});
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
        // Parsen von "var(x^2,3,t,sin(u),a+b)" gegen das Pattern "indet(expr+)".
        try {
            patternForOperator = "var(expr+)";
            operator = OperationParser.parseDefaultOperator("var", new String[]{"x^2", "3", "t", "sin(u)", "a+b"}, null, patternForOperator);

            // Ausgabe der Ergebnisse.
            expectedOperator = new Operator(TypeOperator.var, new Object[]{Expression.build("x^2", null),
                Expression.build("3", null), Expression.build("t", null), Expression.build("sin(u)", null),
                Expression.build("a+b", null)});
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

    @Test
    public void parseIntegralOperatorSuccesfullyParsedTest() {
        // Parsen von "int(x^2,x,2,sin(1))" gegen das Pattern "int(expr,indet(!2,!3),expr,expr)".
        try {
            patternForOperator = "int(expr,indet(!2,!3),expr,expr)";
            operator = OperationParser.parseDefaultOperator("int", new String[]{"x^2", "x", "2", "sin(1)"}, null, patternForOperator);

            // Ausgabe der Ergebnisse.
            expectedOperator = new Operator(TypeOperator.integral, new Object[]{Expression.build("x^2", null),
                "x", Expression.build("2", null), Expression.build("sin(1)", null)});
            TestUtilities.printResult(expectedOperator, operator);

            Assert.assertTrue(operator.getType().equals(TypeOperator.integral));
            Assert.assertTrue(operator.getParams().length == 4);
            Assert.assertTrue(((Expression) operator.getParams()[0]).equals(Expression.build("x^2", null)));
            Assert.assertTrue(((String) operator.getParams()[1]).equals("x"));
            Assert.assertTrue(((Expression) operator.getParams()[2]).equals(Expression.build("2", null)));
            Assert.assertTrue(((Expression) operator.getParams()[3]).equals(Expression.build("sin(1)", null)));

        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseLaplaceOperatorSuccesfullyParsedTest() {
        // Parsen von "laplace(x^2+y,x,y,z)" gegen das Pattern "laplace(expr,uniqueindet+)".
        try {
            patternForOperator = "laplace(expr,uniqueindet+)";
            operator = OperationParser.parseDefaultOperator("laplace", new String[]{"x^2+y", "x", "y", "z"}, null, patternForOperator);

            // Ausgabe der Ergebnisse.
            expectedOperator = new Operator(TypeOperator.laplace, new Object[]{Expression.build("x^2+y", null),
                "x", "y", "z"});
            TestUtilities.printResult(expectedOperator, operator);

            Assert.assertTrue(operator.getType().equals(TypeOperator.laplace));
            Assert.assertTrue(operator.getParams().length == 4);
            Assert.assertTrue(((Expression) operator.getParams()[0]).equals(Expression.build("x^2+y", null)));
            Assert.assertTrue(((String) operator.getParams()[1]).equals("x"));
            Assert.assertTrue(((String) operator.getParams()[2]).equals("y"));
            Assert.assertTrue(((String) operator.getParams()[3]).equals("z"));

        } catch (ExpressionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseGroebnerBasisCommandSuccesfullyParsedTest() {
        // Parsen von "groebnerbasis(x^2+y,x,lex,x,y,z)" gegen das Pattern "groebnerbasis(expr+,type(lex,deglex,revlex,degrevlex),uniqueindet+)".
//        try {
//            String patternForCommand = "groebnerbasis(expr+,type(lex,deglex,revlex,degrevlex),uniqueindet+)";
//            Command command = OperationParser.parseDefaultCommand("groebnerbasis", new String[]{"x^2+y", "x", "lex", "y", "z"}, patternForCommand);
//
//            // Ausgabe der Ergebnisse.
//            Command expectedCommand = new Command(TypeOperator.groebnerbasis, new Object[]{Expression.build("x^2+y", null),
//                "lex", "x", "y", "z"});
//            TestUtilities.printResult(expectedCommand, command);
//
//            Assert.assertTrue(command.getTypeCommand().equals(TypeCommand.groebnerbasis));
//            Assert.assertTrue(command.getParams().length == 4);
//            Assert.assertTrue(((Expression) command.getParams()[0]).equals(Expression.build("x^2+y", null)));
//            Assert.assertTrue(((String) command.getParams()[1]).equals("lex"));
//            Assert.assertTrue(((String) command.getParams()[2]).equals("x"));
//            Assert.assertTrue(((String) command.getParams()[3]).equals("y"));
//            Assert.assertTrue(((String) command.getParams()[4]).equals("z"));
//
//        } catch (ExpressionException e) {
//            fail(e.getMessage());
//        }
    }

    @Test
    public void parseOperatorNotSuccesfullyParsedTest1() {
        // Parsen von "gcd(5,7,3)" gegen das Pattern "var(expr+)".
        try {
            patternForOperator = "var(expr+)";
            operator = OperationParser.parseDefaultOperator("gcd", new String[]{"5", "7", "3"}, null, patternForOperator);
            fail();
        } catch (ExpressionException e) {
            /* 
             Für den Erfolg des Tests muss eine ParseException geworfen werden, 
             da die Syntax des Operators an sich korrekt ist.
             */
            fail(e.getMessage());
        } catch (ParseException e) {
        }
    }

    @Test
    public void parseOperatorNotSuccesfullyParsedTest2() {
        // Parsen von "int(x^2,x,2,x)" gegen das Pattern "int(expr,indet(!2,!3),expr,expr)".
        try {
            patternForOperator = "int(expr,indet(!2,!3),expr,expr)";
            operator = OperationParser.parseDefaultOperator("int", new String[]{"x^2", "x", "2", "x"}, null, patternForOperator);
            fail();
        } catch (ParseException e) {
            /* 
             Für den Erfolg des Tests muss eine ExpressionException geworfen werden, 
             da die Syntax des Operators an sich korrekt ist.
             */
            fail(e.getMessage());
        } catch (ExpressionException e) {
        }
    }

    @Test
    public void parseOperatorNotSuccesfullyParsedTest3() {
        // Parsen von "div(x^2+y-z,x,y,x)" gegen das Pattern "div(expr,uniqueindet,uniqueindet,uniqueindet)".
        try {
            patternForOperator = "div(expr,uniqueindet,uniqueindet,uniqueindet)";
            operator = OperationParser.parseDefaultOperator("div", new String[]{"x^2+y-z", "x", "y", "x"}, null, patternForOperator);
            fail();
        } catch (ParseException e) {
            /* 
             Für den Erfolg des Tests muss eine ExpressionException geworfen werden, 
             da die Syntax des Operators an sich korrekt ist.
             */
            fail(e.getMessage());
        } catch (ExpressionException e) {
        }
    }

    @Test
    public void parseOperatorNotSuccesfullyParsedTest4() {
        try {
            Expression expr = Expression.build("diff(x)", null);
            fail();
        } catch (ParseException e) {
            /* 
             Für den Erfolg des Tests muss eine ExpressionException geworfen werden, 
             da die Syntax des Operators an sich korrekt ist.
             */
            fail(e.getMessage());
        } catch (ExpressionException e) {
        }
    }

}
