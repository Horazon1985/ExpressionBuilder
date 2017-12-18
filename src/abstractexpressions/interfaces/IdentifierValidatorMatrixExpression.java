package abstractexpressions.interfaces;

import java.util.Map;

public class IdentifierValidatorMatrixExpression implements IdentifierValidator {

    @Override
    public void setKnownVariables(Map<String, Class<? extends AbstractExpression>> knownVariables) {
    }
    
    @Override
    public void unsetKnownVariables() {
    }
    
    /**
     * Matrizenvariablen sollen im normalen MathTool-Betrieb in der aktuellen
     * Version gar nicht vorkommen.
     */
    @Override
    public boolean isValidIdentifier(String identifier) {
        return false;
    }

    @Override
    public boolean isValidKnownIdentifier(String identifier, Map<String, Class<? extends AbstractExpression>> knownVariables) {
        return isValidIdentifier(identifier);
    }

}
