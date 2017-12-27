package abstractexpressions.interfaces;

import abstractexpressions.expression.classes.Expression;
import java.util.Map;

public class IdentifierValidatorExpression implements IdentifierValidator {

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
     * Prüft, ob es sich bei var um einen zulässigen Variablennamen oder um die
     * formale Ableitung einer zulässigen Variable handelt. True wird genau dann
     * zurückgegeben, wenn var ein Kleinbuchstabe ist, eventuell gefolgt von '_'
     * und einer natürlichen Zahl (als Index) und eventuell von einer Anzahl von
     * Apostrophs. Beispielsweise wird bei y, x_2, z_4''', t'' true
     * zurückgegeben, bei t'_3' dagegen wird false zurückgegeben.
     */
    @Override
    public boolean isValidIdentifier(String identifierName) {
        // Wenn bekannte Variables explizit gesetzt wurden, dann soll danach ausgewertet werden.
        if (this.knownVariables != null) {
            return this.knownVariables.containsKey(identifierName);
        }
        
        while (identifierName.length() > 0 && identifierName.endsWith("'")) {
            identifierName = identifierName.substring(0, identifierName.length() - 1);
        }
        return Expression.isValidVariable(identifierName);
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
