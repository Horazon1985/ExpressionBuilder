package utilities;

public class TestUtilities {

    public static void printResult(Object expected, Object result) {
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();
        String callerClassName = elements[1].getClassName();
        String callerMethodName = elements[1].getMethodName();
        System.out.println("--------------Testbeginn----------------");
        System.out.println("Testklasse: " + callerClassName);
        System.out.println("Test: " + callerMethodName);
        System.out.println("Erwartetes Ergebnis: " + expected.toString());
        System.out.println("Ergebnis: " + result.toString());
        System.out.println("--------------Testende------------------");
    }
    
}
