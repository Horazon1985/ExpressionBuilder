package testrunner;

import abstractexpressions.expression.computation.ArithmeticUtils;
import basic.MathToolTestBase;
import exceptions.EvaluationException;
import expression.computationtests.*;
import expression.generaltests.*;
import logicalexpression.computationtests.*;
import logicalexpression.generaltests.*;
import matrixexpression.computationtests.*;
import matrixexpression.generaltests.*;
import parsetests.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
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
        TEST_CLASSES.add(ArithmeticTests.class);
        TEST_CLASSES.add(AlgebraicTests.class);
        TEST_CLASSES.add(GroebnerBasisTests.class);
        TEST_CLASSES.add(SolveSpecialDifferentialEquationTests.class);
        TEST_CLASSES.add(SolveGeneralEquationTests.class);
        TEST_CLASSES.add(IntegrationTests.class);
        TEST_CLASSES.add(PolynomialTests.class);
        TEST_CLASSES.add(SimplifyExpLogTests.class);
        TEST_CLASSES.add(SimplifyOperatorTests.class);
        TEST_CLASSES.add(SolveGeneralEquationSystemTests.class);
        TEST_CLASSES.add(SolveGeneralDifferentialEquationTests.class);
        TEST_CLASSES.add(SolveSpecialEquationTests.class);
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
        List<Long> executionTimes = new ArrayList<>();
        for (Class cls : TEST_CLASSES) {
            if (cls.getAnnotation(Ignore.class) != null) {
                System.out.println("Test class " + cls.toString() + " will be ignored.");
                continue;
            }
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
            Method afterMethod = null;
            // @After-Methode finden.
            for (Method m : methods) {
                if (m.getAnnotation(After.class) != null) {
                    afterMethod = m;
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
                    long beginTimeInMs = System.currentTimeMillis();
                    long endTimeInMs;
                    try {
                        System.out.println("Execution of test " + test.getName() + " begins.");
                        if (beforeMethod != null) {
                            // @Before-Methode aufrufen.
                            beforeMethod.invoke(obj);
                        }
                        test.invoke(obj);
                        numberOfSuccessfulTests++;
                        System.out.println("Execution of test " + test.getName() + ": successful.");
                        endTimeInMs = System.currentTimeMillis();
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        numberOfFailedTests++;
                        System.err.println("Execution of test " + test.getName() + ": failed.");
                        endTimeInMs = System.currentTimeMillis();
                    } finally {
                        if (afterMethod != null) {
                            // @After-Methode aufrufen.
                            try {
                                afterMethod.invoke(obj);
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                                System.err.println("Can't invoke @After method for test " + test.getName());

                            }
                        }
                        printResults(cls, test);
                        MathToolTestBase.resetResults();
                    }
                    executionTimes.add(endTimeInMs - beginTimeInMs);
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
        System.err.println("\033[31mNumber of failed tests: " + numberOfFailedTests);
        TestMeasure measure = computeTestMeasure(executionTimes);
        System.out.println("Minimal time of a test execution: " + measure.getMinTime() + " sec.");
        System.out.println("Maximal time of a test execution: " + measure.getMaxTime() + " sec.");
        System.out.println("Average time of a test execution: " + measure.getAvgTime() + " sec.");
        System.out.println("Mean square deviation a test execution: " + measure.getMeanSquareDeviation() + " sec.");
    }

    private void printResults(Class clazz, Method testMethod) {
        if (MathToolTestBase.class.isAssignableFrom(clazz)) {
            try {
                Field expectedResults = MathToolTestBase.class.getDeclaredField("expectedResults");
                Field results = MathToolTestBase.class.getDeclaredField("results");
                expectedResults.setAccessible(true);
                results.setAccessible(true);
                Object[] expectedResultArray = (Object[]) expectedResults.get(null);
                Object[] resultArray = (Object[]) results.get(null);
                if (exists(expectedResultArray) && exists(resultArray)) {
                    TestUtilities.printResults(clazz.getName(), testMethod.getName(), expectedResultArray, resultArray);
                }
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {

            }

        }
    }

    private boolean exists(Object[] objects) {
        return objects != null && objects.length > 0;
    }

    private static class TestMeasure {

        BigDecimal minTime;
        BigDecimal maxTime;
        BigDecimal avgTime;
        BigDecimal meanSquareDeviation;

        public TestMeasure(BigDecimal minTime, BigDecimal maxTime, BigDecimal avgTime, BigDecimal meanSquareDeviation) {
            this.minTime = minTime;
            this.maxTime = maxTime;
            this.avgTime = avgTime;
            this.meanSquareDeviation = meanSquareDeviation;
        }

        public BigDecimal getMinTime() {
            return minTime;
        }

        public BigDecimal getMaxTime() {
            return maxTime;
        }

        public BigDecimal getAvgTime() {
            return avgTime;
        }

        public BigDecimal getMeanSquareDeviation() {
            return meanSquareDeviation;
        }

    }

    private TestMeasure computeTestMeasure(List<Long> times) {
        long minTime = 0;
        long maxTime = 0;
        long avgTime;
        long sigma;
        BigInteger sum = BigInteger.ZERO;
        BigInteger sumOfSquares = BigInteger.ZERO;
        for (long time : times) {
            minTime = Math.min(minTime, time);
            maxTime = Math.max(maxTime, time);
            sum = sum.add(BigInteger.valueOf(time));
        }
        avgTime = sum.divide(BigInteger.valueOf(times.size())).longValue();
        for (long time : times) {
            sumOfSquares = sumOfSquares.add(BigInteger.valueOf(time - avgTime).pow(2));
        }
        sumOfSquares = sumOfSquares.divide(BigInteger.valueOf(times.size()));
        try {
            sigma = ArithmeticUtils.root(sumOfSquares, 2).longValue();
        } catch (EvaluationException e) {
            sigma = 0;
        }
        
        BigDecimal thousand = BigDecimal.valueOf(1000);
        return new TestMeasure(BigDecimal.valueOf(minTime).divide(thousand), BigDecimal.valueOf(maxTime).divide(thousand), 
                BigDecimal.valueOf(avgTime).divide(thousand), BigDecimal.valueOf(sigma).divide(thousand));
    }

}
