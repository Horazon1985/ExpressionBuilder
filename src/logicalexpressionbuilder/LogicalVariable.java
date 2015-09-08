package logicalexpressionbuilder;

import java.util.HashMap;
import java.util.HashSet;

public class LogicalVariable extends LogicalExpression {

    private static HashMap<String, LogicalVariable> logicalVariables = new HashMap<>();
    private final String name;
    private boolean value;

    public LogicalVariable(String name) {
        this.name = name;
        this.value = false;
    }

    public LogicalVariable(String name, boolean value) {
        this.name = name;
        this.value = value;
    }

    public LogicalVariable(String name, int value) {
        this.name = name;
        this.value = value != 0;
    }

    public String getName() {
        return this.name;
    }

    public boolean getValue() {
        return this.value;
    }

    public static void setValue(String name, boolean value) {
        if (logicalVariables.containsKey(name)) {
            logicalVariables.get(name).value = value;
        } else {
            LogicalVariable.create(name, value);
        }
    }

    public static void setValue(String name, int value) {
        if (logicalVariables.containsKey(name)) {
            logicalVariables.get(name).value = value != 0;
        } else {
            LogicalVariable.create(name, value);
        }
    }
    
    public static LogicalVariable create(String name) {
        if (logicalVariables.containsKey(name)) {
            return logicalVariables.get(name);
        } else {
            LogicalVariable result = new LogicalVariable(name, 0);
            logicalVariables.put(name, result);
            return result;
        }
    }

    public static LogicalVariable create(String name, boolean value) {
        if (logicalVariables.containsKey(name)) {
            logicalVariables.get(name).value = value;
            return logicalVariables.get(name);
        } else {
            LogicalVariable result = new LogicalVariable(name, value);
            logicalVariables.put(name, result);
            return result;
        }
    }

    public static LogicalVariable create(String name, int value) {
        if (logicalVariables.containsKey(name)) {
            logicalVariables.get(name).value = value != 0;
            return logicalVariables.get(name);
        } else {
            LogicalVariable result = new LogicalVariable(name, value);
            logicalVariables.put(name, result);
            return result;
        }
    }

    @Override
    public LogicalExpression copy(){
        return LogicalVariable.create(this.name, this.value);
    }

    @Override
    public boolean evaluate(){
        return this.value;
    }
    
    @Override
    public void getContainedVars(HashSet vars){
        vars.add(this.name);
    }

    @Override
    public boolean contains(String var){
        return this.name.equals(var);
    }

    @Override
    public boolean isConstant(){
        return false;
    }
    
    @Override
    public boolean equals(LogicalExpression logExpr) {
        if (logExpr instanceof LogicalVariable) {
            return this.getName().equals(((LogicalVariable) logExpr).getName());
        }
        return false;
    }

    @Override
    public boolean equivalent(LogicalExpression logExpr) {
        if (logExpr instanceof LogicalVariable) {
            return this.getName().equals(((LogicalVariable) logExpr).getName());
        }
        return false;
    }
    
    @Override
    public String writeLogicalExpression() {
        return this.name;
    }

    @Override
    public LogicalExpression simplifyTrivial() {
        return this;
    }

    @Override
    public LogicalExpression factorizeInSums() {
        return this;
    }
    
    @Override
    public LogicalExpression factorizeInProducts() {
        return this;
    }
    
}
