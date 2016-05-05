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
import org.junit.Test;
import substitutiontests.SubstitutionTests;

public class TestRunner {

    private static final ArrayList<Class> testClasses = new ArrayList<>();

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void initTestClasses() throws Exception {
        testClasses.add(AlgebraicMethodsTests.class);
        testClasses.add(GroebnerBasisTests.class);
        testClasses.add(SolveSpecialDifferentialEquationTests.class);
        testClasses.add(SolveGeneralEquationMethodsTests.class);
        testClasses.add(IntegrationTests.class);
        testClasses.add(PolynomialTests.class);
        testClasses.add(SimplifyExpLogTests.class);
        testClasses.add(SimplifyOperatorTests.class);
        testClasses.add(SolveGeneralEquationSystemTests.class);
        testClasses.add(SolveGeneralDifferentialEquationTests.class);
        testClasses.add(SolveSpecialEquationMethodsTests.class);
        testClasses.add(ExpressionCollectionTests.class);
        testClasses.add(GeneralSimplifyExpressionTests.class);
        testClasses.add(SimplifyUtilitiesTests.class);
        testClasses.add(GeneralLogicalTests.class);
        testClasses.add(GeneralSimplifyLogicalExpressionTests.class);
        testClasses.add(LinearAlgebraTests.class);
        testClasses.add(MatrixFunctionTests.class);
        testClasses.add(GeneralMatrixTests.class);
        testClasses.add(ParseTests.class);
        testClasses.add(SubstitutionTests.class);
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
                System.err.println("Cound not find a constructor of the class " + cls.toString());
                continue;
            }
            try {
                obj = constructor.newInstance(new Object[]{});
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                System.err.println("Cound not create an object of the class " + cls.toString());
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
