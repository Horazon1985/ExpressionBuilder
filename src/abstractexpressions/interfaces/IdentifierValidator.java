package abstractexpressions.interfaces;

import java.util.Map;

public interface IdentifierValidator {

    public void setKnownVariables(Map<String, Class<? extends AbstractExpression>> knownVariables);
    
    public void unsetKnownVariables();
    
    public boolean isValidIdentifier(String identifierName);
    
    public boolean isValidIdentifierOfRequiredType(String identifierName, Class requiredClass);
    
    public boolean isValidKnownIdentifier(String identifierName, Class requiredClass, Map<String, Class<? extends AbstractExpression>> knownVariables);
    
}
