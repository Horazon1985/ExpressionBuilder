package abstractexpressions.logicalexpression.classes;

public enum TypeLogicalBinary {
    
    AND("&"), OR("|"), IMPLICATION(">"), EQUIVALENCE("=");
    
    private final String value;
    
    private TypeLogicalBinary(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
}
