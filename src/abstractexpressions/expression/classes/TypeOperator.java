package abstractexpressions.expression.classes;

public enum TypeOperator {

    /*
     Der Name 'int' konnte für das Integral NICHT vergeben werden, da es
     bereits einen primitiven Datentyp kennzeichnet. In der Konsole aber muss
     'int' eingegeben werden.
     */
    diff("diff"), fac("fac"), fourier("fourier"), gcd("gcd"), 
    integral("int"), laplace("laplace"), lcm("lcm"), max("max"), min("min"), 
    mod("mod"), mu("mu"), modpow("modpow"), prod("prod"), sigma("sigma"), 
    sum("sum"), taylor("taylor"), var("var");

    private final String operatorName;
    
    private TypeOperator(String operatorName) {
        this.operatorName = operatorName;
    }
    
    public String getOperatorName() {
        return this.operatorName;
    }
    
}
