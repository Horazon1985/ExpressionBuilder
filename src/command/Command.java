package command;

import abstractexpressions.expression.classes.Expression;

public class Command {

    private TypeCommand type;
    private Object[] params;

    public Command(TypeCommand type, Object[] params) {
        this.type = type;
        this.params = params;
    }

    public TypeCommand getTypeCommand() {
        return this.type;
    }

    public Object[] getParams() {
        return this.params;
    }

    /**
     * Für Ausgabe benötigt. Der Typ, als String ausgeschrieben, ist der
     * Befehlsname.
     */
    public String getName() {
        return type.toString();
    }

    public void setType(TypeCommand type) {
        this.type = type;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    /**
     * Gibt den entsprechenden Typ des Operators zurück, der zum String operator
     * (welcher in der Konsole eingegeben wird) gehört.
     */
    public static TypeCommand getTypeFromName(String command) {
        return TypeCommand.valueOf(command);
    }

    @Override
    public String toString() {
        String result = this.type.name() + "(";
        for (Object param : this.params) {
            if (param instanceof Expression[]) {
                // Dann ist der Parameter eine Gleichung.
                result = result + ((Expression[]) param)[0].toString() + "=" + ((Expression[]) param)[1].toString() + ",";
            } else {
                // Alle anderen Fälle.
                result = result + param.toString() + ",";
            }

        }
        return result.substring(0, result.length() - 1) + ")";
    }

}
