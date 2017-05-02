package abstractexpressions.interfaces;

public class IdentifierValidatorMatrixExpression implements IdentifierValidator {

    /**
     * Matrizenvariablen sollen im normalen MathTool-Betrieb in der aktuellen
     * Version gar nicht vorkommen.
     */
    @Override
    public boolean isValidIdentifier(String identifier) {
        return false;
    }

}
