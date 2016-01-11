package operationparser;

import java.util.ArrayList;

public class ParseResultPattern {

    private final String operationName;
    
    private final ArrayList<ParameterPattern> paramPatterns;
    
    public ParseResultPattern(String operationName, ArrayList<ParameterPattern> paramPatterns){
        this.operationName = operationName;
        this.paramPatterns = paramPatterns;
    }
    
    public String getOperationName(){
        return this.operationName;
    }
    
    public ParameterPattern getParameterPattern(int i){
        return this.paramPatterns.get(i);
    }
    
    public int size(){
        return this.paramPatterns.size();
    }
    
}
