package abstractexpressions.logicalexpression.classes;

public enum TypeLogicalUnary {
    
    NEGATION("!");

    private final String value;
    
    private TypeLogicalUnary(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
}
