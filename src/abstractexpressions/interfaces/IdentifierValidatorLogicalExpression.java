package abstractexpressions.interfaces;

import java.util.Map;

public class IdentifierValidatorLogicalExpression implements IdentifierValidator {

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
     * und einer natürlichen Zahl (als Index).
     */
    @Override
    public boolean isValidIdentifier(String identifierName) {
        if (identifierName.length() == 0) {
            return false;
        }
        //Falls der Ausdruck eine (einfache) Variable ist
        if ((identifierName.length() == 1) && ((int) identifierName.charAt(0) >= 97) && ((int) identifierName.charAt(0) <= 122)) {
            return true;
        }
        //Falls der Ausdruck eine logische Variable mit Index ist (Form: Buchstabe_Index)
        if ((identifierName.length() >= 3) && ((int) identifierName.charAt(0) >= 97) && ((int) identifierName.charAt(0) <= 122)
                && ((int) identifierName.charAt(1) == 95)) {
            for (int i = 2; i < identifierName.length(); i++) {
                if (((int) identifierName.charAt(i) < 48) || ((int) identifierName.charAt(i) > 57)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Prüft dasselbe wie isValidIdentifier(), da für die Klasse
     * LogicalExpression bislang keine bekannten Bezeichner vorgesehen sind.
     */
    @Override
    public boolean isValidIdentifierOfRequiredType(String identifierName, Class requiredClass) {
        return isValidIdentifier(identifierName);
    }

    @Override
    public boolean isValidKnownIdentifier(String identifier, Class requiredClass, Map<String, Class<? extends AbstractExpression>> knownVariables) {
        return isValidIdentifier(identifier) && knownVariables.get(identifier).equals(requiredClass);
    }

}
