package abstractexpressions.interfaces;

import java.util.Set;

public interface AbstractExpression {

    /**
     * Gibt zurück, ob der abstrakte Ausdruck die variable mit dem Namen var
     * enthält.
     */
    public boolean contains(String var);

    /**
     * Fügt dem HashSet vars alle Variablen hinzu, die in diesem abstrakten
     * Ausdruck vorkommen.
     */
    public void addContainedVars(Set<String> vars);

    /**
     * Gibt ein HashSet zurück, welches alle Variablennamen enthält, die in
     * diesem abstrakten Ausdruck vorkommen.
     */
    public Set<String> getContainedVars();

    /**
     * Fügt dem HashSet vars alle Variablen hinzu, die in diesem abstrakten
     * Ausdruck vorkommen.
     */
    public void addContainedIndeterminates(Set<String> vars);
    
    /**
     * Gibt ein HashSet zurück, welches alle Variablennamen enthält, die in
     * diesem abstrakten Ausdruck vorkommen und deren KEIN fester Wert
     * zugeordnet wurde.
     */
    public Set<String> getContainedIndeterminates();

}
