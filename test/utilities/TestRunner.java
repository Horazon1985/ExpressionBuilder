package utilities;

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
import java.util.ArrayList;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import substitutiontests.SubstitutionTests;

public class TestRunner {

    private static final ArrayList<Class> TEST_CLASSES = new ArrayList<>();

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void initTestClasses() throws Exception {
        TEST_CLASSES.add(AlgebraicMethodsTests.class);
        TEST_CLASSES.add(GroebnerBasisTests.class);
        TEST_CLASSES.add(SolveSpecialDifferentialEquationTests.class);
        TEST_CLASSES.add(SolveGeneralEquationMethodsTests.class);
        TEST_CLASSES.add(IntegrationTests.class);
        TEST_CLASSES.add(PolynomialTests.class);
        TEST_CLASSES.add(SimplifyExpLogTests.class);
        TEST_CLASSES.add(SimplifyOperatorTests.class);
        TEST_CLASSES.add(SolveGeneralEquationSystemTests.class);
        TEST_CLASSES.add(SolveGeneralDifferentialEquationTests.class);
        TEST_CLASSES.add(SolveSpecialEquationMethodsTests.class);
        TEST_CLASSES.add(ExpressionCollectionTests.class);
        TEST_CLASSES.add(GeneralSimplifyExpressionTests.class);
        TEST_CLASSES.add(SimplifyUtilitiesTests.class);
        TEST_CLASSES.add(GeneralLogicalTests.class);
        TEST_CLASSES.add(GeneralSimplifyLogicalExpressionTests.class);
        TEST_CLASSES.add(LinearAlgebraTests.class);
        TEST_CLASSES.add(MatrixFunctionTests.class);
        TEST_CLASSES.add(GeneralMatrixTests.class);
        TEST_CLASSES.add(ParseTests.class);
        TEST_CLASSES.add(SubstitutionTests.class);
    }

    @Test
    public void runAllMathToolTests() {
        Constructor constructor;
        Object obj;
        int numberOfSuccessfulTests = 0, numberOfFailedTests = 0;
        for (Class cls : TEST_CLASSES) {
            try {
                constructor = cls.getConstructor();
            } catch (NoSuchMethodException | SecurityException ex) {
                System.err.println("Cound not find a constructor of the class " + cls.toString());
                continue;
            }
            try {
                obj = constructor.newInstance(new Object[]{});
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                System.err.println("Cound not create an object of the class " + cls.toString());
                continue;
            }
            Method[] methods = cls.getDeclaredMethods();
            Method beforeMethod = null;
            // @Before-Methode finden.
            for (Method m : methods) {
                if (m.getAnnotation(Before.class) != null) {
                    beforeMethod = m;
                    break;
                }
            }

            // Zuerst muss die mit @BeforeClass annotierte Methode aufgerufen werden.
            for (Method method : methods) {
                if (method.getAnnotation(BeforeClass.class) != null) {
                    try {
                        method.invoke(obj);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    }
                }
            }

            for (Method test : methods) {
                if (test.getAnnotation(Test.class) != null && test.getAnnotation(Ignore.class) == null) {
                    try {
                        System.out.println("Execution of test " + test.getName() + " begins ...");
                        if (beforeMethod != null) {
                            // @Before-Methode aufrufen.
                            beforeMethod.invoke(obj);
                        }
                        test.invoke(obj);
                        numberOfSuccessfulTests++;
                        System.out.println("Execution of test " + test.getName() + ": successful.");
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        numberOfFailedTests++;
                        System.err.println("Execution of test " + test.getName() + ": failed.");
                    }
                }
            }

            // Zum Schluss muss die mit @AfterClass annotierte Methode aufgerufen werden.
            for (Method method : methods) {
                if (method.getAnnotation(AfterClass.class) != null) {
                    try {
                        method.invoke(obj);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    }
                }
            }

        }

        System.out.println("Number of successful tests: " + numberOfSuccessfulTests);
        System.err.println("Number of failed tests: " + numberOfFailedTests);
    }

}
