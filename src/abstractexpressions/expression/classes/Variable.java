package abstractexpressions.expression.classes;

import enums.TypeExpansion;
import enums.TypeFractionSimplification;
import exceptions.EvaluationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import lang.translator.Translator;

public class Variable extends Expression {

    protected static Map<String, Variable> variables = new HashMap<>();
    protected String name;
    private double value;
    private Expression preciseExpression;
    /*
    dependingVariable gibt den Namen der Variablen an, von dem die vorliegende 
    Variable abhängt. Ist dieser null, so hängt die vorliegende Variable von
    keiner weiteren variablen ab.
     */
    private String dependingOnVariable;
    /*
     Sinn vom Attribut "precise": Die Variablen gelten zunächst alle als
     Präzise, d. h. sie können exakte Werte annehmen wie 3/7 oder pi. Im
     Approximationsmodus wird zunächst im gesamten Ausdruck in allen
     vorkommenden Variablen der precise-Wert auf false gesetzt (und nach
     Beenden des Approximationsmodus wieder auf true). Dies bewirkt, dass der
     exakte Wert (welcher in preciseExpression gespeichert ist) sofort
     (approximativ per evaluateByInsertingDefinedVars()) ausgewertet und zurückgegeben wird.
     Benötigt wird dies eigentlich nur bei PI.
     */
    private boolean precise;

    protected Variable() {
    }

    private Variable(String name, double value) {
        this.name = name;
        this.value = value;
        this.preciseExpression = null;
        this.dependingOnVariable = null;
        this.precise = true;
    }

    private Variable(String name, Expression preciseExpression) {
        this.name = name;
        try {
            this.value = preciseExpression.evaluate();
        } catch (EvaluationException e) {
            this.value = Double.NaN;
        }
        this.preciseExpression = preciseExpression;
        this.dependingOnVariable = null;
        this.precise = true;
    }

    public String getName() {
        return this.name;
    }

    public double getValue() {
        return this.value;
    }

    public Expression getPreciseExpression() {
        return this.preciseExpression;
    }

    public String getDependingVariable() {
        return this.dependingOnVariable;
    }

    public boolean getPrecise() {
        return this.precise;
    }

    /**
     * Methode create: ohne Wertzuweisung (d.h. die Variable wird automatisch
     * auf 0 gesetzt)
     */
    public static Variable create(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (name.equals("pi")) {
            Variable result = new Variable("pi", Math.PI);
            variables.put(name, result);
            return result;
        } else {
            Variable result = new Variable(name, 0);
            variables.put(name, result);
            setDependenceIfDependenceAlreadyExists(name);
            return result;
        }
    }

    /**
     * Methode create: mit Wertzuweisung
     */
    public static Variable create(String name, double value) {
        if (variables.containsKey(name)) {
            variables.get(name).value = value;
            return variables.get(name);
        } else if (name.equals("pi")) {
            Variable result = new Variable("pi", Math.PI);
            variables.put(name, result);
            return result;
        } else {
            Variable result = new Variable(name, value);
            variables.put(name, result);
            setDependenceIfDependenceAlreadyExists(name);
            return result;
        }
    }

    /**
     * Methode create: mit Wertzuweisung als exakten Ausdruck
     */
    public static Variable create(String name, Expression preciseExpression) {
        if (variables.containsKey(name)) {
            variables.get(name).preciseExpression = preciseExpression;
            return variables.get(name);
        } else if (name.equals("pi")) {
            Variable result = new Variable("pi", Math.PI);
            variables.put(name, result);
            return result;
        } else {
            Variable result = new Variable(name, preciseExpression);
            variables.put(name, result);
            setDependenceIfDependenceAlreadyExists(name);
            return result;
        }
    }

    public static void setValue(String name, double value) {
        // PI darf nicht verändert werden.
        if (name.equals("pi")) {
            if (variables.containsKey("pi")) {
                variables.get("pi").value = Math.PI;
            } else {
                Variable.create("pi", Math.PI);
            }
        }
        if (variables.containsKey(name)) {
            variables.get(name).value = value;
        } else {
            Variable.create(name, value);
        }
    }

    public static void setPreciseExpression(String name, Expression preciseExpression) {
        // PI darf nicht verändert werden.
        if (name.equals("pi")) {
            if (variables.containsKey("pi")) {
                variables.get("pi").value = Math.PI;
            } else {
                Variable.create("pi", Math.PI);
            }
        } else {
            Variable.create(name, preciseExpression);
        }
    }

    public static void setDependingOnVariable(String name, String dependingVariable) {
        // PI darf nicht von irgendeiner Variablen abhängig sein.
        if (!name.equals("pi")) {
            Variable var = Variable.create(name);
            var.dependingOnVariable = dependingVariable;
            setDependingOnVariableForFurtherVariables(name, dependingVariable);
        }
    }

    private static void setDependingOnVariableForFurtherVariables(String name, String dependingVariable) {
        // Falls beispielsweise name == y und y von x abhängt, so sollen auch y', y'', ... von x abhängen.
        for (String var : variables.keySet()) {
            if (isVariableDerivativeOfGivenVariable(var, name)) {
                variables.get(var).dependingOnVariable = dependingVariable;
            }
        }
    }
    
    /**
     * Gibt zurück, ob var eine echte Ableitung von givenVar ist.<br>
     * BEISPIELE: (1) var = y''', givenVar = y'. Hier wird true zurückgegeben.<br>
     * (1) var = y'', givenVar = y''. Hier wird false zurückgegeben.<br>
     * (1) var = y, givenVar = x. Hier wird false zurückgegeben.<br>
     */
    private static boolean isVariableDerivativeOfGivenVariable(String var, String givenVar){
        while (var.startsWith(givenVar) && !var.equals(givenVar)){
            givenVar += "'";
        }
        return var.equals(givenVar);
    }
    
    private static void setDependenceIfDependenceAlreadyExists(String name){
        // Falls beispielsweise name == y'' und y' von x abhängt, so soll auch y'' von x abhängen.
        for (String var : variables.keySet()) {
            if (name.startsWith(var)) {
                variables.get(name).dependingOnVariable = variables.get(var).dependingOnVariable;
            }
        }
    }

    /**
     * Gibt eine Liste mit den Namen der Variablen zurück, die von der Variablen
     * mit dem Namen name abhängen.
     */
    public static HashSet<String> getVariablesDependingOnGivenVariable(String name) {
        HashSet<String> vars = new HashSet<>();
        for (String var : variables.keySet()) {
            if (name.equals(variables.get(var).dependingOnVariable)) {
                vars.add(var);
            }
        }
        return vars;
    }
    
    public static void setPrecise(String name, boolean precise) {
        if (variables.containsKey(name)) {
            variables.get(name).precise = precise;
        } else {
            Variable.create(name);
            variables.get(name).precise = precise;
        }
    }

    public static void setAllPrecise(boolean precise) {
        for (String var : variables.keySet()) {
            variables.get(var).precise = precise;
        }
    }

    /**
     * Liefert eine Liste mit allen Variablen, denen ein fester Wert zugeordnet
     * wurde.
     */
    public static HashSet<String> getVariablesWithPredefinedValues() {
        HashSet<String> vars = new HashSet<>();
        for (String var : variables.keySet()) {
            if (variables.get(var).preciseExpression != null) {
                vars.add(var);
            }
        }
        return vars;
    }

    public static boolean doesVariableAlreadyExist(String varName) {
        return variables.containsKey(varName);
    }
    
    @Override
    public Expression copy() {
        return Variable.create(this.name, this.value);
    }

    @Override
    public double evaluate() throws EvaluationException {

        /* 
         Falls der Variable ein konstanter Wert zugeordnet wurde, dann soll dieser  
         ausgewertet werden. Beispiel: x wurde als 4/3 definiert (durch das Command def()).
         Dann soll x beim Evaluieren 1.33333333333333 zurückgeben.
         */
        if (this.preciseExpression != null) {
            return this.preciseExpression.evaluate();
        }
        if (!Double.isNaN(this.value) && !Double.isInfinite(this.value)) {
            return this.value;
        }
        throw new EvaluationException(Translator.translateOutputMessage("EB_Variable_VARIABLE_HAS_UNDEFINED_VALUE", this.name));
    }

    @Override
    public Expression simplifyByInsertingDefinedVars() throws EvaluationException {
        if (this.preciseExpression != null) {
            return this.preciseExpression;
        }
        return this;
    }

    @Override
    public void addContainedVars(HashSet<String> vars) {
        if (!this.name.equals("pi")) {
            vars.add(this.name);
        }
    }

    @Override
    public void addContainedIndeterminates(HashSet<String> vars) {
        if (!this.name.equals("pi") && this.preciseExpression == null) {
            vars.add(this.name);
        }
    }

    @Override
    public boolean contains(String var) {
        return this.name.equals(var);
    }

    @Override
    public boolean containsApproximates() {
        return false;
    }

    @Override
    public boolean containsFunction() {
        return false;
    }

    @Override
    public boolean containsExponentialFunction() {
        return false;
    }

    @Override
    public boolean containsTrigonometricalFunction() {
        return false;
    }

    @Override
    public boolean containsIndefiniteIntegral() {
        return false;
    }

    @Override
    public boolean containsOperator() {
        return false;
    }

    @Override
    public boolean containsOperator(TypeOperator type) {
        return false;
    }

    @Override
    public boolean containsAlgebraicOperation(){
        return false;
    }
    
    @Override
    public Expression turnToApproximate() {
        Variable.setPrecise(this.name, false);
        return this;
    }

    @Override
    public Expression turnToPrecise() {
        Variable.setPrecise(this.name, true);
        return this;
    }

    @Override
    public Expression replaceVariable(String var, Expression expr) {
        if (this.name.equals(var)) {
            return expr;
        }
        return this;
    }

    @Override
    public Expression replaceSelfDefinedFunctionsByPredefinedFunctions() {
        return this;
    }

    @Override
    public Expression diff(String var) {

        // Partielles Differenzieren nach der Variablen var
        if (this.name.equals(var)) {
            if (this.precise) {
                return Expression.ONE;
            }
            return Expression.ONE.turnToApproximate();
        }
        if (this.dependingOnVariable != null && this.dependingOnVariable.equals(var)) {
            /* 
            Falls beispielsweise this.name == x'' und dependingVariable == u ist, 
            so soll x''' zurückgegeben werden.
             */
            return Variable.create(this.name + "'");
        }
        if (this.precise) {
            return Expression.ZERO;
        }
        return Expression.ZERO.turnToApproximate();

    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String expressionToLatex() {
        if (!this.name.equals("pi")) {
            return this.name;
        }
        return "\\" + "pi";
    }

    @Override
    public boolean isConstant() {
        return this.name.equals("pi") || this.preciseExpression != null;
    }

    @Override
    public boolean isNonNegative() {
        return this.name.equals("pi");
    }

    @Override
    public boolean isNonPositive() {
        return false;
    }

    @Override
    public boolean isAlwaysNonNegative() {
        return this.name.equals("pi");
    }

    @Override
    public boolean isAlwaysPositive() {
        return this.name.equals("pi");
    }

    @Override
    public boolean isAlwaysNonPositive() {
        return this.name.equals("pi");
    }

    @Override
    public boolean isAlwaysNegative() {
        return false;
    }

    @Override
    public boolean equals(Expression expr) {
        if (expr instanceof Variable) {
            return this.getName().equals(((Variable) expr).getName());
        }
        return false;
    }

    @Override
    public boolean equivalent(Expression expr) {
        if (expr instanceof Variable) {
            Variable v = (Variable) expr;
            return this.getName().equals(v.getName());
        }
        return false;
    }

    @Override
    public boolean antiEquivalent(Expression expr) {
        return false;
    }

    @Override
    public boolean hasPositiveSign() {
        return true;
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public int getMaximalNumberOfSummandsInExpansion(){
        return 1;
    }

    @Override
    public Expression simplifyBasic() throws EvaluationException {
        if (this.name.equals("pi") && !this.precise) {
            return new Constant(Math.PI);
        }
        return this;
    }

    @Override
    public Expression simplifyExpandRationalFactors() {
        return this;
    }

    @Override
    public Expression simplifyExpand(TypeExpansion type) {
        return this;
    }

    @Override
    public Expression simplifyBringExpressionToCommonDenominator(TypeFractionSimplification type){
        return this;
    }
    
    @Override
    public Expression simplifyReduceDifferencesAndQuotientsAdvanced() {
        return this;
    }

    @Override
    public Expression orderSumsAndProducts() {
        return this;
    }

    @Override
    public Expression orderDifferencesAndQuotients() {
        return this;
    }

    @Override
    public Expression simplifyCollectProducts() {
        return this;
    }

    @Override
    public Expression simplifyFactorize() {
        return this;
    }

    @Override
    public Expression simplifyFactorizeAllButRationalsInSums() {
        return this;
    }

    @Override
    public Expression simplifyFactorizeAllButRationalsInDifferences() {
        return this;
    }

    @Override
    public Expression simplifyFactorizeAllButRationals() {
        return this;
    }

    @Override
    public Expression simplifyReduceQuotients() {
        return this;
    }

    @Override
    public Expression simplifyPullApartPowers() {
        return this;
    }

    @Override
    public Expression simplifyMultiplyExponents() {
        return this;
    }

    @Override
    public Expression simplifyFunctionalRelations() {
        return this;
    }

    @Override
    public Expression simplifyExpandAndCollectEquivalentsIfShorter() {
        return this;
    }

    @Override
    public Expression simplifyCollectLogarithms() {
        return this;
    }

    @Override
    public Expression simplifyExpandLogarithms() {
        return this;
    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsByDefinitions() {
        return this;
    }

    @Override
    public Expression simplifyReplaceExponentialFunctionsWithRespectToVariableByDefinitions(String var) {
        return this;
    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsByDefinitions() {
        return this;
    }

    @Override
    public Expression simplifyReplaceTrigonometricalFunctionsWithRespectToVariableByDefinitions(String var) {
        return this;
    }

    @Override
    public Expression simplifyExpandProductsOfComplexExponentialFunctions(String var) {
        return this;
    }

    @Override
    public Expression simplifyAlgebraicExpressions() throws EvaluationException {
        return this;
    }

}
