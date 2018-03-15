package abstractexpressions.matrixexpression.classes;

public enum TypeMatrixOperator {
    
    /*
     Der Name 'int' konnte für das Integral NICHT vergeben werden, da es
     bereits einen primitiven Datentyp kennzeichnet. In der Konsole aber muss
     'int' eingegeben werden.
     */
    cov("cov"), cross("cross"), diff("diff"), div("div"), grad("grad"), 
    integral("int"), laplace("laplace"), prod("prod"), rot("rot"), sum("sum");
    
    private final String operatorName;
    
    private TypeMatrixOperator(String operatorName) {
        this.operatorName = operatorName;
    }
    
    public String getOperatorName() {
        return this.operatorName;
    }
    
}
