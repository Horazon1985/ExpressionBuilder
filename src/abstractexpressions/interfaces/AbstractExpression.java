package abstractexpressions.interfaces;

import java.util.HashSet;

public interface AbstractExpression {

    public boolean contains(String var);
    
    public HashSet<String> getContainedVars();
    
    public void addContainedVars(HashSet<String> vars);
    
}
