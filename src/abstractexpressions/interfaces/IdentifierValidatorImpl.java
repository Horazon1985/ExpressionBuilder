package abstractexpressions.interfaces;

import abstractexpressions.expression.classes.Expression;

public class IdentifierValidatorImpl implements IdentifierValidator {

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
        while (identifier.length() > 0 && identifier.substring(identifier.length() - 1).equals("'")) {
            identifier = identifier.substring(0, identifier.length() - 1);
        }
        return Expression.isValidVariable(identifier);
    }
    
}
