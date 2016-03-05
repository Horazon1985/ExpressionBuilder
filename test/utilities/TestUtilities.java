package utilities;

public class TestUtilities {

    public static void printResult(Object expected, Object result) {
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();
        String callerClassName = elements[1].getClassName();
        String callerMethodName = elements[1].getMethodName();
        System.out.println("--------------Begin of test----------------");
        System.out.println("Test class: " + callerClassName);
        System.out.println("Test: " + callerMethodName);
        System.out.println("Expected result: " + expected.toString());
        System.out.println("Result: " + result.toString());
        System.out.println("--------------End of test------------------");
    }
    
}
