package abstractexpressions.interfaces;

import abstractexpressions.expression.classes.Expression;
import java.util.Map;

public class IdentifierValidatorExpression implements IdentifierValidator {

    @Override
    public void setKnownVariables(Map<String, Class<? extends AbstractExpression>> knownVariables) {
    }
    
    @Override
    public void unsetKnownVariables() {
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
    public boolean isValidIdentifier(String identifier) {
        while (identifier.length() > 0 && identifier.endsWith("'")) {
            identifier = identifier.substring(0, identifier.length() - 1);
        }
        return Expression.isValidVariable(identifier);
    }

    @Override
    public boolean isValidKnownIdentifier(String identifier, Map<String, Class<? extends AbstractExpression>> knownVariables) {
        return isValidIdentifier(identifier);
    }
   
}
