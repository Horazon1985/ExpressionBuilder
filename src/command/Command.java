package command;

public class Command {

    private TypeCommand type;
    private Object[] params;

    public Command(){
    }
    
    public Command(TypeCommand type, Object[] params){
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
    
}
