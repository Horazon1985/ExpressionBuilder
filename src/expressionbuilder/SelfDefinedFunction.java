package expressionbuilder;

import java.util.HashMap;
import java.util.HashSet;
import translator.Translator;

public class SelfDefinedFunction extends Expression {

    // Tabelle für Funktionsname <-> abstrakte Funktionsterme für Funktionen
    public static HashMap<String, Expression> abstractExpressionsForSelfDefinedFunctions = new HashMap<>();
    // Tabelle für Funktionsname <-> Funktionsterme für abstrakte Variablen
    public static HashMap<String, Expression[]> innerExpressionsForSelfDefinedFunctions = new HashMap<>();
    // Tabelle für Funktionsname <-> Funktionsargumente
    public static HashMap<String, String[]> varsForSelfDefinedFunctions = new HashMap<>();
    /*
     Beispiel: Die Funktionen f(x, y) = x + y und g(x, y, z) = x^2*y-z liefern
     folgende Tabellen: abstractExpressionsForSelfDefinedFunctions {f ->
     x_ABSTRACT + y_ABSTRACT, g -> x_ABSTRACT^2-y_ABSTRACT+z_ABSTRACT}
     varsForSelfDefinedFunctions {f -> {"x_ABSTRACT", "y_ABSTRACT"}, g ->
     {"x_ABSTRACT", "y_ABSTRACT", "z_ABSTRACT"}}
     innerExpressionsForSelfDefinedFunctions {f -> was auch immer, g -> was
     auch immer}.
     */

    /*
     name = Funktionsname arguments = abstrakte Funktionsargumente; das sind
     Variablen von der Form var_ABSTRACT. Diese können beim parsen mittels
     Expression.build NICHT eingelesen werden, denn das sind keine legalen
     Ausdrücke. Intern können diese jedoch verarbeitet werden, falls sie
     bereits erschaffen wurden. Sie werden bei der Ausführung des Befehls
     def(Funktionsdefinition) automatisch in dieses Format konvertiert.
     expressionAbstract = abstrakter Funktionsausdruck, der die Funktion
     definiert. left = Funktionsausdrucke, die für die Variablen der Form
     var_ABSTRACT eingesetzt werden. Beispiel: f(x_ABSTRACT, y_ABSTRACT) =
     3*x_ABSTRACT+y_ABSTRACT^2, arguments = {"x_ABSTRACT", "y_ABSTRACT"}, left
     = {x^2, sin(u*v)}. Dann: name = "f" und die Funktion hat dann den
     Funktionsterm f = 3*x^2 + sin(u*v)^2. Zu den Hashtables werden dann die
     folgenden Einträge hinzugefügt:
     abstractExpressionsForSelfDefinedFunctions: f ->
     3*x_ABSTRACT+y_ABSTRACT^2 innerExpressionsForSelfDefinedFunctions: f ->
     {x^2, sin(u*v)} (als Expressions) varsForSelfDefinedFunctions: f ->
     {"x_ABSTRACT", "y_ABSTRACT"} BEMERKUNG: arguments und left müssen
     natürlich dieselbe Länge haben.
     */
    private String name;
    private String[] arguments;
    private Expression abstractExpression;
    private Expression[] left;

    public SelfDefinedFunction(String name, String[] arguments, Expression abstractExpression,
            Expression[] left) {
        this.name = name;
        this.arguments = arguments;
        this.abstractExpression = abstractExpression;
        this.left = left;
    }

    public String getName() {
        return this.name;
    }

    public String[] getArguments() {
        return this.arguments;
    }

    public Expression getAbstractExpression() {
        return this.abstractExpression;
    }

    public Expression[] getLeft() {
        return this.left;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public void setAbstractExpression(Expression abstractExpression) {
        this.abstractExpression = abstractExpression;
    }

    public void setLeft(Expression[] left) {
        this.left = left;
    }

    @Override
    public Expression copy() {
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression, this.left);
    }

    @Override
    public double evaluate() throws EvaluationException {
        return this.replaceSelfDefinedFunctionsByPredefinedFunctions().evaluate();
    }

    @Override
    public Expression evaluate(HashSet<String> vars) throws EvaluationException {
        return this.replaceSelfDefinedFunctionsByPredefinedFunctions().evaluate(vars);
    }

    @Override
    public void getContainedVars(HashSet<String> vars) {
        for (int i = 0; i < this.left.length; i++) {
            this.left[i].getContainedVars(vars);
        }
    }

    @Override
    public boolean contains(String var) {
        boolean result = false;
        for (int i = 0; i < this.left.length; i++) {
            result = result || ((Expression) this.left[i]).contains(var);
        }
        return result;
    }

    @Override
    public boolean containsApproximates() {
        boolean result = false;
        for (int i = 0; i < this.left.length; i++) {
            result = result || this.left[i].containsApproximates();
        }
        return result || this.abstractExpression.containsApproximates();
    }

    @Override
    public boolean containsFunction() {
        boolean result = false;
        for (int i = 0; i < this.left.length; i++) {
            result = result || this.left[i].containsFunction();
        }
        return result || this.abstractExpression.containsFunction();
    }

    @Override
    public boolean containsExponentialFunction() {
        boolean result = false;
        for (int i = 0; i < this.left.length; i++) {
            result = result || this.left[i].containsExponentialFunction();
        }
        return result || this.abstractExpression.containsExponentialFunction();
    }

    @Override
    public boolean containsTrigonometricalFunction() {
        boolean result = false;
        for (int i = 0; i < this.left.length; i++) {
            result = result || this.left[i].containsTrigonometricalFunction();
        }
        return result || this.abstractExpression.containsTrigonometricalFunction();
    }
    
    @Override
    public boolean containsIndefiniteIntegral() {
        boolean result = false;
        for (int i = 0; i < this.left.length; i++) {
            result = result || this.left[i].containsIndefiniteIntegral();
        }
        return result || this.abstractExpression.containsIndefiniteIntegral();
    }

    @Override
    public Expression turnToApproximate() {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = this.left[i].turnToApproximate();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.turnToApproximate(), resultLeft);
    }

    @Override
    public Expression turnToPrecise() {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = this.left[i].turnToPrecise();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.turnToPrecise(), resultLeft);
    }

    /**
     * Ersetzt in einer Funktion f(x_1, ..., x_n) die Variable var durch den
     * Ausdruck expr.
     */
    @Override
    public Expression replaceVariable(String var, Expression expr) {
        return this.abstractExpression.replaceVariable(var, expr);
    }

    @Override
    public Expression replaceSelfDefinedFunctionsByPredefinedFunctions() {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = this.left[i].replaceSelfDefinedFunctionsByPredefinedFunctions();
        }
        Expression result = this.abstractExpression;
        for (int i = 0; i < this.left.length; i++) {
            result = result.replaceVariable(this.arguments[i], resultLeft[i]);
        }
        return result;
    }

    /**
     * Setzt in eine Funktion f(x_1, ..., x_n) n Funktionen g_1, ..., g_n ein
     * und gibt die so erhaltene Komposition zurück. Ergebnis ist also f(g_1,
     * ..., g_n).
     *
     * @throws EvaluationException
     */
    public Expression replaceAllVariables(Expression[] exprs) throws EvaluationException {
        if (this.arguments.length != exprs.length) {
            throw new EvaluationException(Translator.translateExceptionMessage("EB_SelfDefinedFunction_INCORRECT_NUMBER_OF_INSERTED_FUNCTIONS"));
        }
        Expression result = this.abstractExpression;
        for (int i = 0; i < exprs.length; i++) {
            result = result.replaceVariable(this.arguments[i], exprs[i]);
        }
        return result;
    }

    @Override
    public Expression diff(String var) throws EvaluationException {

        if (!this.contains(var)) {
            return Expression.ZERO;
        }
        // Die Funktion muss zunächst vereinfacht werden, denn in den Parametern können weitere Ausdrücke stehen.
        return this.simplifyTrivial().diff(var);

    }

    @Override
    public Expression diffDifferentialEquation(String var) throws EvaluationException {

        if (!this.contains(var)) {
            return Expression.ZERO;
        }
        // Die Funktion muss zunächst vereinfacht werden, denn in den Parametern können weitere Ausdrücke stehen.
        return this.simplifyTrivial().diffDifferentialEquation(var);

    }

    @Override
    public String writeExpression() {
        String result = this.name + "(";
        for (int i = 0; i < this.left.length - 1; i++) {
            result = result + this.left[i].writeExpression() + ",";
        }
        result = result + this.left[this.left.length - 1].writeExpression() + ")";
        return result;
    }

    @Override
    public String expressionToLatex() {

        String result = this.name + "\\left(";
        for (int i = 0; i < this.left.length - 1; i++) {
            result = result + this.left[i].expressionToLatex() + ",";
        }
        result = result + this.left[this.left.length - 1].expressionToLatex() + "\\right)";
        return result;

    }

    @Override
    public boolean isConstant() {
        
        boolean allLeftsAreConstant = true;
        for (int i = 0; i < this.left.length; i++){
            allLeftsAreConstant = allLeftsAreConstant && this.left[i].isConstant();
        }
        return this.abstractExpression.isConstant() || allLeftsAreConstant;
        
    }

    @Override
    public boolean isNonNegative() {

        if (!this.isConstant()) {
            return false;
        }

        // Falls die Funktion konstant ist -> versuchen auszuwerten.
        try {
            return this.evaluate() >= 0;
        } catch (EvaluationException e) {
        }

        return false;

    }

    @Override
    public boolean isAlwaysNonNegative() {
        return this.abstractExpression.isAlwaysNonNegative();
    }
    
    @Override
    public boolean isAlwaysPositive() {
        return this.abstractExpression.isAlwaysPositive();
    }

    @Override
    public boolean equals(Expression expr) {

        if (expr instanceof SelfDefinedFunction) {
            SelfDefinedFunction f = (SelfDefinedFunction) expr;
            boolean result = (this.name.equals(f.name)) && (this.arguments.length == f.arguments.length)
                    && (this.left.length == f.left.length) && (this.abstractExpression.equals(f.abstractExpression));
            if (!result) {
                return false;
            }
            for (int i = 0; i < this.arguments.length; i++) {
                result = result & (this.arguments[i].equals(f.arguments[i]));
            }
            for (int i = 0; i < this.left.length; i++) {
                result = result & (this.left[i].equals(f.left[i]));
            }
            return result;
        }
        return false;

    }

    @Override
    public boolean equivalent(Expression expr) {

        if (expr instanceof SelfDefinedFunction) {
            SelfDefinedFunction f = (SelfDefinedFunction) expr;
            boolean result = (this.name.equals(f.name)) & (this.arguments.length == f.arguments.length)
                    & (this.left.length == f.left.length) & (this.abstractExpression.equivalent(f.abstractExpression));
            if (!result) {
                return false;
            }
            for (int i = 0; i < this.arguments.length; i++) {
                result = result & (this.arguments[i].equals(f.arguments[i]));
            }
            for (int i = 0; i < this.left.length; i++) {
                result = result & (this.left[i].equivalent(f.left[i]));
            }
            return result;
        }
        return false;

    }

    @Override
    public boolean hasPositiveSign() {
        return this.abstractExpression.hasPositiveSign();
    }

    @Override
    public Expression simplifyTrivial() throws EvaluationException {
        return this.replaceAllVariables(left);
    }

    @Override
    public Expression expandRationalFactors() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).expandRationalFactors();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.expandRationalFactors(), resultLeft);
    }

    @Override
    public Expression expand() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).expand();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.expand(), resultLeft);
    }

    @Override
    public Expression reduceLeadingsCoefficients() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).reduceLeadingsCoefficients();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.reduceLeadingsCoefficients(), resultLeft);
    }

    @Override
    public Expression orderSumsAndProducts() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).orderSumsAndProducts();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.orderSumsAndProducts(), resultLeft);
    }

    @Override
    public Expression orderDifferenceAndDivision() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).orderDifferenceAndDivision();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.orderDifferenceAndDivision(), resultLeft);
    }

    @Override
    public Expression collectProducts() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).collectProducts();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.collectProducts(), resultLeft);
    }

    @Override
    public Expression factorizeInSums() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).factorizeInSums();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.factorizeInSums(), resultLeft);
    }

    @Override
    public Expression factorizeInDifferences() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).factorizeInDifferences();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.factorizeInDifferences(), resultLeft);
    }

    @Override
    public Expression factorizeRationalsInSums() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).factorizeRationalsInSums();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.factorizeRationalsInSums(), resultLeft);
    }

    @Override
    public Expression factorizeRationalsInDifferences() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).factorizeRationalsInDifferences();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.factorizeRationalsInDifferences(), resultLeft);
    }

    @Override
    public Expression reduceQuotients() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).reduceQuotients();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.reduceQuotients(), resultLeft);
    }

    @Override
    public Expression simplifyFunctionalRelations() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).simplifyFunctionalRelations();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.simplifyFunctionalRelations(), resultLeft);
    }

    @Override
    public Expression simplifyPolynomials() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).simplifyPolynomials();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.simplifyPolynomials(), resultLeft);
    }

    @Override
    public Expression simplifyCollectLogarithms() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).simplifyCollectLogarithms();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.simplifyCollectLogarithms(), resultLeft);
    }

    @Override
    public Expression simplifyExpandLogarithms() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).simplifyExpandLogarithms();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.simplifyExpandLogarithms(), resultLeft);
    }

    @Override
    public Expression simplifyPowers() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).simplifyPowers();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.simplifyPowers(), resultLeft);
    }

    @Override
    public Expression multiplyPowers() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).multiplyPowers();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.multiplyPowers(), resultLeft);
    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsByDefinitions() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).simplifyReplaceExponentialFunctionsByDefinitions();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.simplifyReplaceExponentialFunctionsByDefinitions(), resultLeft);
    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsByDefinitions() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).simplifyReplaceTrigonometricalFunctionsByDefinitions();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.simplifyReplaceTrigonometricalFunctionsByDefinitions(), resultLeft);
    }

    @Override
    public Expression simplifyAlgebraicExpressions() throws EvaluationException {
        Expression[] resultLeft = new Expression[this.left.length];
        for (int i = 0; i < this.left.length; i++) {
            resultLeft[i] = ((Expression) this.left[i]).simplifyAlgebraicExpressions();
        }
        return new SelfDefinedFunction(this.name, this.arguments, this.abstractExpression.simplifyAlgebraicExpressions(), resultLeft);
    }

}
