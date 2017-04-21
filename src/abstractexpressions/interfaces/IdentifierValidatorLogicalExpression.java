package abstractexpressions.interfaces;

public class IdentifierValidatorLogicalExpression implements IdentifierValidator {

    /**
     * Prüft, ob es sich bei var um einen zulässigen Variablennamen oder um die
     * formale Ableitung einer zulässigen Variable handelt. True wird genau dann
     * zurückgegeben, wenn var ein Kleinbuchstabe ist, eventuell gefolgt von '_'
     * und einer natürlichen Zahl (als Index).
     */
    @Override
    public boolean isValidIdentifier(String identifier) {
        if (identifier.length() == 0) {
            return false;
        }
        //Falls der Ausdruck eine (einfache) Variable ist
        if ((identifier.length() == 1) && ((int) identifier.charAt(0) >= 97) && ((int) identifier.charAt(0) <= 122)) {
            return true;
        }
        //Falls der Ausdruck eine logische Variable mit Index ist (Form: Buchstabe_Index)
        if ((identifier.length() >= 3) && ((int) identifier.charAt(0) >= 97) && ((int) identifier.charAt(0) <= 122)
                && ((int) identifier.charAt(1) == 95)) {
            for (int i = 2; i < identifier.length(); i++) {
                if (((int) identifier.charAt(i) < 48) || ((int) identifier.charAt(i) > 57)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
}
