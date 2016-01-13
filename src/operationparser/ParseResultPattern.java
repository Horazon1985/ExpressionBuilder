package operationparser;

import java.util.ArrayList;

public class ParseResultPattern {

    private final String operationName;
    
    private final ArrayList<ParameterPattern> parameterPatterns;
    
    public ParseResultPattern(String operationName, ArrayList<ParameterPattern> paramPatterns){
        this.operationName = operationName;
        this.parameterPatterns = paramPatterns;
    }
    
    public String getOperationName(){
        return this.operationName;
    }
    
    public ParameterPattern getParameterPattern(int i){
        return this.parameterPatterns.get(i);
    }
    
    public int size(){
        return this.parameterPatterns.size();
    }
    
}
