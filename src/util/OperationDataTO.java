package util;

public class OperationDataTO {

    private final String operationName;
    private final String[] operationArguments;

    public OperationDataTO(String operationName, String[] operationArguments) {
        this.operationName = operationName;
        this.operationArguments = operationArguments;
    }

    public String getOperationName() {
        return operationName;
    }

    public String[] getOperationArguments() {
        return operationArguments;
    }
    
}
