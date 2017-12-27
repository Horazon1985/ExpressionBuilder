package abstractexpressions.interfaces;

import java.util.Map;

public class IdentifierValidatorMatrixExpression implements IdentifierValidator {

    Map<String, Class<? extends AbstractExpression>> knownVariables;

    @Override
    public void setKnownVariables(Map<String, Class<? extends AbstractExpression>> knownVariables) {
        this.knownVariables = knownVariables;
    }

    @Override
    public void unsetKnownVariables() {
        this.knownVariables = null;
    }
    
    /**
     * Matrizenvariablen sollen im normalen MathTool-Betrieb in der aktuellen
     * Version gar nicht vorkommen.
     */
    @Override
    public boolean isValidIdentifier(String identifierName) {
        // Wenn bekannte Variables explizit gesetzt wurden, dann soll danach ausgewertet werden.
        if (this.knownVariables != null) {
            return this.knownVariables.containsKey(identifierName);
        }
        return false;
    }

    /**
     * Prüft, ob der Name identifier ein gültiger (bereits bekannter) Bezeichner
     * ist vom geforderten Typ ist.
     */
    @Override
    public boolean isValidIdentifierOfRequiredType(String identifierName, Class requiredClass) {
        // Wenn bekannte Variables explizit gesetzt wurden, dann soll danach ausgewertet werden.
        if (this.knownVariables != null) {
            return this.knownVariables.containsKey(identifierName) && this.knownVariables.get(identifierName).equals(requiredClass);
        }
        return isValidIdentifier(identifierName);
    }
    
    @Override
    public boolean isValidKnownIdentifier(String identifier, Class requiredClass, Map<String, Class<? extends AbstractExpression>> knownVariables) {
        return isValidIdentifier(identifier) && knownVariables.get(identifier).equals(requiredClass);
    }

}
