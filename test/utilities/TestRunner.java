package utilities;

import abstractexpressions.expression.utilities.SimplifyExpLog;
import expression.computationtests.*;
import expression.generaltests.*;
import logicalexpression.computationtests.*;
import logicalexpression.generaltests.*;
import matrixexpression.computationtests.*;
import matrixexpression.generaltests.*;
import parsetests.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestRunner {

    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';
    private static final String CLASS_FILE_SUFFIX = ".class";
    private static ArrayList<Class> testClasses = new ArrayList<>();

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void initTestClasses() throws Exception {
        testClasses.add(GeneralEquationMethodsTest.class);
        testClasses.add(IntegrationTests.class);
        testClasses.add(PolynomialTests.class);
        testClasses.add(SimplifyExpLog.class);
        testClasses.add(SimplifyOperatorTests.class);
        testClasses.add(SpecialEquationMethodsTest.class);
        testClasses.add(ExpressionCollectionTest.class);
        testClasses.add(GeneralSimplifyExpressionTest.class);
        testClasses.add(SimplifyUtilitiesTest.class);
        testClasses.add(GeneralLogicalTests.class);
        testClasses.add(GeneralSimplifyLogicalExpressionTest.class);
        testClasses.add(LinearAlgebraTests.class);
        testClasses.add(MatrixFunctionTest.class);
        testClasses.add(GeneralMatrixTest.class);
        testClasses.add(ParseTests.class);
    }

    @Test
    public void runAllMathToolTests() {
        Constructor constructor;
        Object obj;
        int numberOfSuccessfulTests = 0, numberOfFailedTests = 0;
        for (Class cls : testClasses) {
            try {
                constructor = cls.getConstructor();
            } catch (NoSuchMethodException | SecurityException ex) {
                continue;
            }
            try {
                obj = constructor.newInstance(new Object[]{});
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                continue;
            }
            Method[] methods = cls.getDeclaredMethods();
            // @Before aufrufen.
            for (Method test : methods) {
                if (test.getAnnotation(Before.class) != null) {
                    try {
                        test.invoke(obj);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        break;
                    }
                }
            }
            for (Method test : methods) {
                if (test.getAnnotation(Test.class) != null) {
                    try {
                        test.invoke(obj);
                        numberOfSuccessfulTests++;
                        System.out.println("Execution of test " + test.getName() + ": successful.");
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        numberOfFailedTests++;
                        System.err.println("Execution of test " + test.getName() + ": failed.");
                    }
                }
            }
        }
        System.out.println("Number of successful tests: " + numberOfSuccessfulTests);
        System.err.println("Number of failed tests: " + numberOfFailedTests);
    }

}
