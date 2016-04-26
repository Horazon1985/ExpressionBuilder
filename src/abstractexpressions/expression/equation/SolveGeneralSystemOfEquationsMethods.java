package abstractexpressions.expression.equation;

import abstractexpressions.expression.classes.Expression;
import static abstractexpressions.expression.classes.Expression.MINUS_ONE;
import static abstractexpressions.expression.classes.Expression.ZERO;
import abstractexpressions.expression.classes.MultiIndexVariable;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisMethods;
import abstractexpressions.expression.commutativealgebra.GroebnerBasisMethods.MultiPolynomial;
import abstractexpressions.expression.utilities.ExpressionCollection;
import abstractexpressions.expression.utilities.SimplifyMultiPolynomialMethods;
import abstractexpressions.expression.utilities.SimplifyPolynomialMethods;
import abstractexpressions.matrixexpression.classes.Matrix;
import abstractexpressions.matrixexpression.computation.GaussAlgorithm;
import exceptions.EvaluationException;
import exceptions.NotAlgebraicallySolvableException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import lang.translator.Translator;
import notations.NotationLoader;

public class SolveGeneralSystemOfEquationsMethods {

    private static boolean containsSet(HashSet<String> varsToBeContained, HashSet<String> vars) {
        for (String var : varsToBeContained) {
            if (!vars.contains(var)) {
                return false;
            }
        }
        return true;
    }

    private static HashSet<String> getIndeterminatesInEquation(Expression equation, ArrayList<String> vars) {
        HashSet<String> allVarsInEquation = equation.getContainedIndeterminates();
        HashSet<String> varsInEquation = new HashSet<>();
        for (String var : allVarsInEquation) {
            if (vars.contains(var)) {
                varsInEquation.add(var);
            }
        }
        return varsInEquation;
    }

    private static HashSet<String> getIndeterminatesInEquation(MultiPolynomial equation, ArrayList<String> vars) {
        HashSet<String> allVarsInEquation = equation.toExpression().getContainedIndeterminates();
        HashSet<String> varsInEquation = new HashSet<>();
        for (String var : allVarsInEquation) {
            if (vars.contains(var)) {
                varsInEquation.add(var);
            }
        }
        return varsInEquation;
    }

    private static boolean isSystemPolynomial(Expression[] equations, ArrayList<String> vars) {
        for (Expression equation : equations) {
            if (!SimplifyMultiPolynomialMethods.isMultiPolynomial(equation, vars)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSystemLinear(Expression[] equations, ArrayList<String> vars) {
        if (!isSystemPolynomial(equations, vars)) {
            return false;
        }
        // Prüfen, ob alle Grade <= 1 sind.
        for (Expression equation : equations) {
            if (SimplifyMultiPolynomialMethods.getDegreeOfMultiPolynomial(equation, vars).compareTo(BigInteger.ONE) > 0) {
                return false;
            }
        }
        return true;
    }

    public static Expression[] solveLinearSystemOfEquations(Expression[] equations, ArrayList<String> vars) throws NotAlgebraicallySolvableException {

        // Prüfung, ob alle Gleichungen linear in den angegebenen Variablen sind.
        BigInteger degree;
        for (Expression equation : equations) {
            degree = SimplifyMultiPolynomialMethods.getDegreeOfMultiPolynomial(equation, vars);
            if (degree.compareTo(BigInteger.ONE) > 0) {
                throw new NotAlgebraicallySolvableException();
            }
        }

        Expression[][] matrixEntries = new Expression[equations.length][vars.size()];
        Expression[] vectorEntries = new Expression[equations.length];

        try {
            for (int i = 0; i < equations.length; i++) {
                for (int j = 0; j < vars.size(); j++) {
                    matrixEntries[i][j] = equations[i].diff(vars.get(j)).simplify();
                }
            }

            // Alle Variablen durch 0 ersetzen.
            for (int i = 0; i < equations.length; i++) {
                vectorEntries[i] = equations[i];
                for (String solutionVar : vars) {
                    vectorEntries[i] = vectorEntries[i].replaceVariable(solutionVar, ZERO);
                }
                vectorEntries[i] = MINUS_ONE.mult(vectorEntries[i]).simplify();
            }

            Matrix m = new Matrix(matrixEntries);
            Matrix b = new Matrix(vectorEntries);

            Expression[] solutions = GaussAlgorithm.solveLinearSystemOfEquations(m, b);
            return solutions;
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

    }

    private static boolean isSystemOfMultiPolynomialsInTriangularForm(ArrayList<MultiPolynomial> equations, ArrayList<String> vars) {
        ArrayList<Expression> equationsAsExpression = new ArrayList<>();
        for (MultiPolynomial equation : equations) {
            equationsAsExpression.add(equation.toExpression());
        }
        return isSystemInTriangularForm(equationsAsExpression, vars);
    }

    private static boolean isSystemInTriangularForm(ArrayList<Expression> equations, ArrayList<String> vars) {

        if (equations.size() != vars.size()) {
            return false;
        }

        // Zunächst: für jede Gleichung Variablen aus vars ermitteln, die darin auftauchen.
        ArrayList<HashSet<String>> varSets = new ArrayList<>();
        for (Expression f : equations) {
            varSets.add(getIndeterminatesInEquation(f, vars));
        }
        // Nun: prüfen, ob die Mengen in varSets eine strikt aufsteigende Kette bilden.
        boolean[] sizes = new boolean[vars.size()];
        for (HashSet<String> varSet : varSets) {
            sizes[varSet.size() - 1] = true;
        }

        /* 
         Prüfen, ob unter den Variablenmengen zu jedem i = 1, 2, ..., vars.size()
         eine Menge mit Mächtigkeit i auftaucht.
         */
        for (boolean size : sizes) {
            if (!size) {
                return false;
            }
        }

        // Auf strikte Aufsteigung prüfen.
        for (int i = 0; i < vars.size() - 1; i++) {
            for (HashSet<String> varSet : varSets) {
                if (varSet.size() != i + 1) {
                    continue;
                }
                for (HashSet<String> biggerVarSet : varSets) {
                    if (biggerVarSet.size() != i + 2) {
                        continue;
                    }
                    if (!containsSet(varSet, biggerVarSet)) {
                        return false;
                    }
                }
            }
        }

        return true;

    }

    public static ArrayList<Expression[]> solvePolynomialSystemOfEquations(Expression[] equations, ArrayList<String> vars) throws NotAlgebraicallySolvableException {

        // Prüfung, ob alle Gleichungen Polynome sind.
        for (Expression equation : equations) {
            if (!SimplifyMultiPolynomialMethods.isMultiPolynomial(equation, vars)) {
                throw new NotAlgebraicallySolvableException();
            }
        }

        ArrayList<MultiPolynomial> polynomials;
        try {
            polynomials = SimplifyMultiPolynomialMethods.getMultiPolynomialsFromExpressions(equations, vars);
            if (polynomials.size() < equations.length) {
                throw new NotAlgebraicallySolvableException();
            }
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        GroebnerBasisMethods.setTermOrdering(GroebnerBasisMethods.TermOrderings.LEX);
        ArrayList<MultiPolynomial> groebnerBasis;
        try {
            groebnerBasis = GroebnerBasisMethods.getNormalizedReducedGroebnerBasis(polynomials);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        if (!isSystemOfMultiPolynomialsInTriangularForm(groebnerBasis, vars)) {
            throw new NotAlgebraicallySolvableException();
        }

        // Das Gleichungssystem ist nun in Dreiecksform. Nun kann es rekursiv gelöst werden.
        ArrayList<String> varsCopy = new ArrayList<>(vars);
        ArrayList<HashMap<String, Expression>> solutions = solveTriangularPolynomialSystemOfEquations(groebnerBasis, varsCopy);

        // Die Lösungstupel in Arrays verwandeln und ausgeben.
        ArrayList<Expression[]> orderedSolutions = new ArrayList<>();
        Expression[] orderedSolution;
        for (HashMap<String, Expression> solution : solutions) {
            orderedSolution = new Expression[solution.size()];
            for (String var : solution.keySet()) {
                orderedSolution[vars.indexOf(var)] = solution.get(var);
            }
            orderedSolutions.add(orderedSolution);
        }

        return orderedSolutions;

    }

    private static ArrayList<HashMap<String, Expression>> solveTriangularPolynomialSystemOfEquations(ArrayList<MultiPolynomial> equations, ArrayList<String> vars) throws NotAlgebraicallySolvableException {

        // Dann ist es nur eine Gleichung in einer Variablen.
        if (vars.size() == 1) {

            String var = null;
            for (String v : vars) {
                var = v;
            }
            if (var == null) {
                throw new NotAlgebraicallySolvableException();
            }

            ExpressionCollection solutions;
            try {
                solutions = SolveGeneralEquationMethods.solveEquation(equations.get(0).toExpression(), ZERO, var);
            } catch (EvaluationException e) {
                throw new NotAlgebraicallySolvableException();
            }

            ArrayList<HashMap<String, Expression>> solutionsOfSystem = new ArrayList<>();
            HashMap<String, Expression> solutionOfSystem;
            for (Expression solution : solutions) {
                solutionOfSystem = new HashMap<>();
                solutionOfSystem.put(var, solution);
                solutionsOfSystem.add(solutionOfSystem);
            }

            return solutionsOfSystem;

        }

        String[] monomialVars = new String[vars.size()];
        GroebnerBasisMethods.setMonomialVars(vars.toArray(monomialVars));

        HashSet<String> varsInEquation;
        MultiPolynomial equation = null;
        String var = null;
        for (MultiPolynomial f : equations) {
            varsInEquation = getIndeterminatesInEquation(f, vars);
            if (varsInEquation.size() == 1) {
                equation = f;
                for (String v : varsInEquation) {
                    var = v;
                }
                equations.remove(equation);
                break;
            }
        }

        if (equation == null) {
            throw new NotAlgebraicallySolvableException();
        }

        ExpressionCollection coefficientsOfEquation = equation.toPolynomial(var);

        ExpressionCollection solutionsOfEquation;
        try {
            solutionsOfEquation = PolynomialRootsMethods.solvePolynomialEquation(coefficientsOfEquation, var);
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        vars.remove(var);
        ArrayList<MultiPolynomial> equationsWithVarReplacedBySolution = new ArrayList<>();
        ArrayList<HashMap<String, Expression>> solutions = new ArrayList<>();

        // Jede gefundene Lösung in die restlichen Gleichungen einsetzen und weiter rekursiv auflösen.
        try {
            for (Expression solutionOfEquation : solutionsOfEquation) {

                equationsWithVarReplacedBySolution.clear();

                for (MultiPolynomial f : equations) {
                    equationsWithVarReplacedBySolution.add(f.replaceVarByExpression(var, solutionOfEquation).simplify());
                }

                ArrayList<HashMap<String, Expression>> solutionsOfReducedPolynomialSystem = solveTriangularPolynomialSystemOfEquations(equationsWithVarReplacedBySolution, vars);

                HashMap<String, Expression> singleSolution;
                for (HashMap<String, Expression> solutionOfReducedPolynomialSystem : solutionsOfReducedPolynomialSystem) {
                    singleSolution = new HashMap<>();
                    for (String v : solutionOfReducedPolynomialSystem.keySet()) {
                        singleSolution.put(v, solutionOfReducedPolynomialSystem.get(v));
                    }
                    singleSolution.put(var, solutionOfEquation);
                    solutions.add(singleSolution);
                }

            }
        } catch (EvaluationException e) {
            throw new NotAlgebraicallySolvableException();
        }

        return solutions;

    }

    public static ArrayList<Expression[]> solveGeneralSystemOfEquations(Expression[] equations, ArrayList<String> vars) {

        // Typ: lineares Gleichungssystem.
        ArrayList<Expression[]> solutions = new ArrayList<>();
        try {
            solutions.add(solveLinearSystemOfEquations(equations, vars));
            return solutions;
        } catch (NotAlgebraicallySolvableException e) {
        }

        // Typ: lineares Gleichungssystem.
        try {
            solutions = solvePolynomialSystemOfEquations(equations, vars);
            return solutions;
        } catch (NotAlgebraicallySolvableException e) {
        }

//        try {
//            solutions = solveTriangularSystemOfEquations(equations, vars);
//            return solutions;
//        } catch (NotAlgebraicallySolvableException e) {
//        }

        return new ArrayList<>();

    }

}
