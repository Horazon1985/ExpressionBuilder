package abstractexpressions.expression.computation;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import graphic.GraphicPanelImplicit2D.MarchingSquare;
import graphic.GraphicPanelImplicit3D.MarchingCube;
import java.util.ArrayList;
import lang.translator.Translator;

public abstract class NumericalUtils {

    /**
     * Gibt die Summe der Vektoren a und b zurück.
     *
     * @throws EvaluationException
     */
    private static double[] add(double[] a, double[] b) throws EvaluationException {

        if (a.length != b.length) {
            throw new EvaluationException(Translator.translateOutputMessage("CC_NumericalMethods_VECTORS_MUST_HAVE_SAME_DIMENSION"));
        }

        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }

        return result;

    }

    /**
     * Gibt das Produkt des Skalars a mit dem Vektor b zurück.
     */
    private static double[] mult(double a, double[] b) {

        double[] result = new double[b.length];
        for (int i = 0; i < b.length; i++) {
            result[i] = a * b[i];
        }

        return result;

    }

    /**
     * Ein Schritt des (klassischen) Runge-Kutta-Verfahrens.
     *
     * @throws EvaluationException
     */
    private static double[] rungeKuttaStep(Expression[] exprs, String argumentVar, String[] functionVar,
            int ord, double x, double y[], double h) throws EvaluationException {

        double[] k_1 = new double[ord];
        double[] k_2 = new double[ord];
        double[] k_3 = new double[ord];

        /**
         * Berechnung von k_1, k_2, k_3: k_1 = f(x, y) k_2 = f(x + h/2, y +
         * h*k_1/2) k_3 = f(x + h, y - h*k_1 + 2*h*k_2) -> y(x + h) = y(x) +
         * h*(k_1 + 4*k_2 + k_3)/6;
         */
        /**
         * Berechnung von k_1.
         */
        Variable.setValue(argumentVar, x);
        for (int i = 0; i < ord; i++) {
            Variable.setValue(functionVar[i], y[i]);
        }
        for (int i = 0; i < ord; i++) {
            k_1[i] = exprs[i].evaluate();
        }

        /**
         * Berechnung von k_2.
         */
        Variable.setValue(argumentVar, x + h / 2);
        double[] y_1 = add(y, mult(h / 2, k_1));
        for (int i = 0; i < ord; i++) {
            Variable.setValue(functionVar[i], y_1[i]);
        }
        for (int i = 0; i < ord; i++) {
            k_2[i] = exprs[i].evaluate();
        }

        /**
         * Berechnung von k_3.
         */
        Variable.setValue(argumentVar, x + h);
        double[] y_2 = add(y, add(mult(-h, k_1), mult(2 * h, k_2)));
        for (int i = 0; i < ord; i++) {
            Variable.setValue(functionVar[i], y_2[i]);
        }
        for (int i = 0; i < ord; i++) {
            k_3[i] = exprs[i].evaluate();
        }

        return add(y, mult(h / 6, add(k_1, add(mult(4, k_2), k_3))));

    }

    /**
     * Sei x = argumentVar und y = functionVar. Gibt die numerische Lösung der
     * Differentialgleichung y^{(ord - 1)} = f zurück. VORAUSSETZUNG: f enthält
     * höchstens die Variablen x, y, y', y'', ..., y^{(ord - 1)} und ord &#8805;
     * 1. Dies wird im Vorfeld beim Kompilieren in MathCommandCompiler
     * sichergestellt.
     */
    public static double[][] solveDifferentialEquationByRungeKutta(Expression f, String argumentVar, String functionVar,
            int ord, double x_0, double x_1, double[] y_0, int n) {

        /**
         * varPrimes sind die Ableitungsstriche (als String) von functionVar.
         * Diese werden später an functionVar angehängt, um die Ableitung von
         * functionVar als String darzustellen.
         */
        String[] varPrimes = new String[ord];
        varPrimes[0] = functionVar;

        for (int i = 1; i < ord; i++) {
            varPrimes[i] = varPrimes[i - 1] + "'";
        }

        /**
         * DGL zu einer vektoriellen DGL erster Ordnung machen. GENAUER:
         * y^{(ord)} =
         */
        Expression[] exprs = new Expression[ord];
        for (int i = 0; i < ord - 1; i++) {
            exprs[i] = Variable.create(varPrimes[i + 1]);
        }
        exprs[ord - 1] = f;

        double h = (x_1 - x_0) / n;
        double x_act = x_0;
        double[] y_act = y_0;

        double[][] solutionVector = new double[n + 1][ord + 1];

        solutionVector[0][0] = x_act;
        System.arraycopy(y_act, 0, solutionVector[0], 1, ord);

        //Iterativ Lösungen produzieren
        for (int i = 1; i <= n; i++) {

            x_act = x_0 + i * h;

            try {
                y_act = rungeKuttaStep(exprs, argumentVar, varPrimes, ord, x_act, y_act, h);
                solutionVector[i][0] = x_act;
                System.arraycopy(y_act, 0, solutionVector[i], 1, ord);
            } catch (EvaluationException e) {
                /**
                 * Numerische Lösung BIS zum undefinierten Wert ausgeben
                 * Solution_short ist das Lösungsarray, welches nur die Lösungen
                 * bis zur Problemstelle beinhaltet
                 */

                double[][] vector_valued_solution_short = new double[i - 1][ord + 1];
                for (int j = 0; j < i - 1; j++) {
                    vector_valued_solution_short[j][0] = solutionVector[j][0];
                    vector_valued_solution_short[j][1] = solutionVector[j][1];
                }
                solutionVector = vector_valued_solution_short;
                break;

            }

        }

        /**
         * solutionVector enthält die Werte von (x, y, y', y'', ...). benötigt
         * werden aber nur die Werte von x und y.
         */
        double[][] solutionFunction = new double[solutionVector.length][2];

        for (int i = 0; i < solutionVector.length; i++) {
            solutionFunction[i][0] = solutionVector[i][0];
            solutionFunction[i][1] = solutionVector[i][1];
        }

        return solutionFunction;
    }

    /**
     * Approximiert das Integral über f von x_0 bis x_1 (nach der Regel von
     * Simpson). Dabei wird das Intervall [x_0, x_1] in n Schritte unterteilt.
     * VORAUSSETZUNG: expr enthält höchstens die Variable var, x_1 &#8805; x_0,
     * n &#8805; 1.
     *
     * @throws EvaluationException
     */
    public static double integrateBySimpson(Expression f, String var, double x_0, double x_1, int n)
            throws EvaluationException {

        Variable.setValue(var, x_0);
        double h = (x_1 - x_0) / (2 * n);

        double weight;
        double integral = 0;

        /**
         * Regel: integral(f, a, b) ist in etwa gleich h/3 * (f(x_0) + 4*f(x_0 +
         * h) + 2*f(x + 2*h) + 4*f(x_0 + 3*h) + ... + f(x_1)).
         */
        for (int i = 0; i <= 2 * n; i++) {

            if ((i == 0) || (i == 2 * n)) {
                weight = 1;
            } else if ((i / 2) * 2 == i) {
                weight = 2;
            } else {
                weight = 4;
            }

            Variable.setValue(var, x_0 + i * h);
            integral = integral + weight * f.evaluate();
        }

        return integral * h / 3;
    }

    /**
     * Newton-Verfahren für die Gleichung x_0 mit vorgegebenen Startwert x_0 und
     * einer vorgegebenen Anzahl n von Schritten. Gibt nach n Iterationen des
     * Newton-Verfahrens die (approximierte) Nullstelle von f = 0 zurück.
     * VORAUSSETZUNG: f hängt nur von einer Variablen ab.
     *
     * @throws EvaluationException
     */
    public static double solveEquationByNewtonIteration(Expression f, String var, double x_0, int n) throws EvaluationException {

        Expression derivative = f.diff(var).simplify();
        Variable.setValue(var, x_0);
        double zeroOfEquation = x_0;

        /**
         * Regel: x_{i + 1} = x_i - f(x_i)/f'(x_i), i = 0, ..., n - 1.
         */
        for (int i = 0; i < n; i++) {
            Variable.setValue(var, zeroOfEquation);
            if (derivative.evaluate() == 0) {
                throw new EvaluationException(Translator.translateOutputMessage("CC_NumericalMethods_UNDEFINED_VALUE"));
            }
            zeroOfEquation = zeroOfEquation - f.evaluate() / derivative.evaluate();
            if (Double.isNaN(zeroOfEquation) || Double.isInfinite(zeroOfEquation)) {
                throw new EvaluationException(Translator.translateOutputMessage("CC_NumericalMethods_UNDEFINED_VALUE"));
            }
        }

        return zeroOfEquation;

    }

    /**
     * Hauptmethode zum (numerischen) Lösen der Gleichung f = 0 mittels
     * Newton-Verfahren. VORAUSSETZUNG: f hängt nur von der Variablen var ab.
     */
    public static ArrayList<Double> solveEquation(Expression f, String var, double x_1, double x_2, int n) {

        ArrayList<Double> zerosOfEquation = new ArrayList<>();

        double x;
        double valueAtCurrentArgument, valueAtNextArgument;

        for (int i = 0; i < n; i++) {
            try {
                x = x_1 + i * (x_2 - x_1) / n;
                Variable.setValue(var, x);
                valueAtCurrentArgument = f.evaluate();
                x = x_1 + (i + 1) * (x_2 - x_1) / n;
                Variable.setValue(var, x);
                valueAtNextArgument = f.evaluate();
                if (valueAtCurrentArgument == 0) {
                    zerosOfEquation.add(x_1 + i * (x_2 - x_1) / n);
                } else if (valueAtCurrentArgument * valueAtNextArgument < 0) {
                    /**
                     * Es liegt eine Nullstelle zwischen den beiden x-Werten
                     * vor. -> Newton-Iteration anwenden.
                     */
                    double x_0 = x_1 + (x_2 - x_1) * (((double) i) / n + 1 / (n * (1 + Math.abs(valueAtNextArgument) / Math.abs(valueAtCurrentArgument))));
                    double zero = solveEquationByNewtonIteration(f, var, x_0, 100);
                    if ((x_1 + i * (x_2 - x_1) / n < zero) && (x_1 + (i + 1) * (x_2 - x_1) / n > zero)) {
                        zerosOfEquation.add(zero);
                    }
                }
                if (i == n - 1 && valueAtNextArgument == 0) {
                    zerosOfEquation.add(x_2);
                }
            } catch (EvaluationException e) {
            }
        }

        return zerosOfEquation;

    }

    /**
     * Hauptmethode zum (numerischen) Lösen der (impliziten) Gleichung f(var1,
     * var2) = 0 im Bereich x_0 &#8804; varAbsc &#8804; x_1, y_0 &#8804; varOrd
     * &#8804; y_1.<br> 
     * VORAUSSETZUNG: f hängt nur von varAbsc und varOrd ab.
     */
    public static MarchingSquare[][] solveImplicitEquation2D(Expression f, String varAbsc, String varOrd,
            double xStart, double xEnd, double yStart, double yEnd) {

        int numberOfIntervals = 100;

        MarchingSquare[][] squares = new MarchingSquare[numberOfIntervals][numberOfIntervals];
        MarchingSquare square;

        Double[][] innerPoints = new Double[numberOfIntervals + 1][numberOfIntervals + 1];

        for (int i = 0; i <= numberOfIntervals; i++) {
            for (int j = 0; j <= numberOfIntervals; j++) {

                Variable.setValue(varAbsc, xStart + i * (xEnd - xStart) / numberOfIntervals);
                Variable.setValue(varOrd, yStart + j * (yEnd - yStart) / numberOfIntervals);
                try {
                    innerPoints[i][j] = f.evaluate();
                } catch (EvaluationException e) {
                    innerPoints[i][j] = Double.NaN;
                }

            }
        }

        for (int i = 0; i < numberOfIntervals; i++) {
            for (int j = 0; j < numberOfIntervals; j++) {

                square = new MarchingSquare();
                for (int p = 0; p < 2; p++) {
                    for (int q = 0; q < 2; q++) {                      
                        square.setVertexValue(p, q, innerPoints[p + i][q + j]);
                    }
                }
                squares[i][j] = square;

            }
        }

        return squares;

    }

    public static MarchingCube[][][] solveImplicitEquation3D(Expression f, String varAbsc, String varOrd, String varAppl,
            double xStart, double xEnd, double yStart, double yEnd, double zStart, double zEnd) {

        int numberOfIntervals = 50;

        MarchingCube[][][] cubes = new MarchingCube[numberOfIntervals][numberOfIntervals][numberOfIntervals];
        MarchingCube cube;

        boolean[][][] innerPoints = new boolean[numberOfIntervals + 1][numberOfIntervals + 1][numberOfIntervals + 1];

        for (int i = 0; i <= numberOfIntervals; i++) {
            for (int j = 0; j <= numberOfIntervals; j++) {
                for (int k = 0; k <= numberOfIntervals; k++) {

                    Variable.setValue(varAbsc, xStart + i * (xEnd - xStart) / numberOfIntervals);
                    Variable.setValue(varOrd, yStart + j * (yEnd - yStart) / numberOfIntervals);
                    Variable.setValue(varAppl, zStart + k * (zEnd - zStart) / numberOfIntervals);
                    try {
                        innerPoints[i][j][k] = f.evaluate() <= 0;
                    } catch (EvaluationException e) {
                    }

                }
            }
        }

        for (int i = 0; i < numberOfIntervals; i++) {
            for (int j = 0; j < numberOfIntervals; j++) {
                for (int k = 0; k < numberOfIntervals; k++) {

                    cube = new MarchingCube();
                    for (int p = 0; p < 2; p++) {
                        for (int q = 0; q < 2; q++) {
                            for (int r = 0; r < 2; r++) {
                                if (innerPoints[i + p][j + q][k + r]) {
                                    cube.addInnerVertex(new Boolean[]{p == 1, q == 1, r == 1});
                                }
                            }
                        }
                    }
                    cubes[i][j][k] = cube;

                }
            }
        }

        return cubes;

    }

}
