package abstractexpressions.matrixexpression.classes;

import abstractexpressions.expression.classes.Expression;
import enums.TypeSimplify;
import exceptions.EvaluationException;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MatrixVariable extends MatrixExpression {

    protected static Map<String, MatrixVariable> matrixVariables = new HashMap<>();
    protected String name;
    private MatrixExpression value;
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

    public String getName() {
        return this.name;
    }

    public MatrixExpression getValue() {
        return this.value;
    }

    public boolean getPrecise() {
        return this.precise;
    }

    private MatrixVariable() {
    }

    private MatrixVariable(String name, MatrixExpression value) {
        this.name = name;
        this.value = value;
        this.precise = true;
    }

    /**
     * Methode create: ohne Wertzuweisung (d.h. die Variable wird automatisch
     * auf 0 gesetzt)
     */
    public static MatrixVariable create(String name) {
        if (matrixVariables.containsKey(name)) {
            return matrixVariables.get(name);
        } else {
            MatrixVariable result = new MatrixVariable(name, MatrixExpression.getZeroMatrix(1, 1));
            matrixVariables.put(name, result);
            return result;
        }
    }

    public static void setValue(String name, MatrixExpression value) {
        if (matrixVariables.containsKey(name)) {
            matrixVariables.get(name).value = value;
        } else {
            MatrixVariable.create(name);
        }
    }

    @Override
    public Dimension getDimension() throws EvaluationException {
        return this.value.getDimension();
    }

    @Override
    public MatrixExpression simplifyComputeMatrixOperations() throws EvaluationException {
        return this.value.simplifyComputeMatrixOperations();
    }

    @Override
    public boolean equals(MatrixExpression matExpr) {
        return this.value.equals(matExpr);
    }

    @Override
    public boolean equivalent(MatrixExpression matExpr) {
        return this.value.equivalent(matExpr);
    }

    @Override
    public MatrixExpression orderSumsAndProducts() throws EvaluationException {
        return this.value.orderSumsAndProducts();
    }

    @Override
    public MatrixExpression orderDifferences() throws EvaluationException {
        return this.value.orderDifferences();
    }

    @Override
    public boolean isConstant() {
        return this.value.isConstant();
    }

    @Override
    public boolean contains(String var) {
        return this.value.contains(var);
    }

    @Override
    public void addContainedVars(HashSet<String> vars) {
        vars.add(this.name);
    }

    @Override
    public void addContainedIndeterminates(HashSet<String> vars) {
        if (this.value != null) {
            vars.add(this.name);
        }
    }

    @Override
    public MatrixExpression turnToApproximate() {
        return this.value.turnToApproximate();
    }

    @Override
    public MatrixExpression turnToPrecise() {
        return this.value.turnToPrecise();
    }

    @Override
    public boolean containsApproximates() {
        return this.value.containsApproximates();
    }

    @Override
    public MatrixExpression copy() {
        return this.value.copy();
    }

    @Override
    public MatrixExpression evaluate() throws EvaluationException {
        return this.value.evaluate();
    }

    @Override
    public MatrixExpression diff(String var) throws EvaluationException {
        return this.value.diff(var);
    }

    @Override
    public MatrixExpression replaceVariable(String var, Expression expr) {
        return this.value.replaceVariable(var, expr);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public MatrixExpression simplifyBasic() throws EvaluationException {
        return this.value.simplifyBasic();
    }

    @Override
    public MatrixExpression simplifyByInsertingDefinedVars() throws EvaluationException {
        return this.value.simplifyByInsertingDefinedVars();
    }

    @Override
    public MatrixExpression simplifyMatrixEntries() throws EvaluationException {
        return this.value.simplifyMatrixEntries();
    }

    @Override
    public MatrixExpression simplifyMatrixEntries(HashSet<TypeSimplify> simplifyTypes) throws EvaluationException {
        return this.value.simplifyMatrixEntries();
    }

    @Override
    public MatrixExpression simplifyCollectProducts() throws EvaluationException {
        return this.value.simplifyCollectProducts();
    }

    @Override
    public MatrixExpression simplifyFactorizeScalars() throws EvaluationException {
        return this.value.simplifyFactorizeScalars();
    }

    @Override
    public MatrixExpression simplifyFactorize() throws EvaluationException {
        return this.value.simplifyFactorize();
    }

    @Override
    public MatrixExpression simplifyMatrixFunctionalRelations() throws EvaluationException {
        return this.value.simplifyMatrixFunctionalRelations();
    }

}
