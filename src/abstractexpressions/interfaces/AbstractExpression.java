package abstractexpressions.interfaces;

import java.util.HashSet;

public interface AbstractExpression {

    /**
     * Gibt zurück, ob der abstrakte Ausdruck die variable mit dem Namen var
     * enthält.
     */
    public boolean contains(String var);

    /**
     * Fügt dem hashSet vars alle Variablen hinzu, die in diesem abstrakten
     * Ausdruck vorkommen.
     */
    public void addContainedVars(HashSet<String> vars);

    /**
     * Gibt ein HashSet zurück, welches alle Variablennamen enthält, die in
     * diesem abstrakten Ausdruck vorkommen.
     */
    public HashSet<String> getContainedVars();

    /**
     * Fügt dem hashSet vars alle Variablen hinzu, die in diesem abstrakten
     * Ausdruck vorkommen.
     */
    public void addContainedIndeterminates(HashSet<String> vars);
    
    /**
     * Gibt ein HashSet zurück, welches alle Variablennamen enthält, die in
     * diesem abstrakten Ausdruck vorkommen und deren KEIN fester Wert
     * zugeordnet wurde.
     */
    public HashSet<String> getContainedIndeterminates();

}
