package abstractexpressions.interfaces;

import java.util.Map;

public interface IdentifierValidator {

    public void setKnownVariables(Map<String, Class<? extends AbstractExpression>> knownVariables);
    
    public void unsetKnownVariables();
    
    public boolean isValidIdentifier(String identifier);
    
    public boolean isValidKnownIdentifier(String identifier, Map<String, Class<? extends AbstractExpression>> knownVariables);
    
}
