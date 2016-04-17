package abstractexpressions.expression.utilities;

import computationbounds.ComputationBounds;
import exceptions.EvaluationException;
import abstractexpressions.expression.classes.BinaryOperation;
import abstractexpressions.expression.classes.Constant;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Function;
import abstractexpressions.expression.classes.TypeFunction;
import abstractexpressions.expression.classes.Variable;
import enums.TypeExpansion;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;

public abstract class SimplifyRationalFunctionMethods {

    /**
     * Hilfsmethode. Gibt zurück, ob alle paarweisen Verhältnisse von Ausdrücken
     * in terms rational sind.
     */
    private static boolean areQuotientsOfTermsRational(HashSet<Expression> terms) {
        for (Expression expr : terms){
            if (!expr.equals(Expression.ZERO)){
                return areQuotientsRational(expr, terms);
            }
        }
        // Dies kann nur eintreten, falls alle Elemente von terms gleich ZERO sind.
        return true;
    }

    /**
     * Entscheidet, ob f bzgl. der Variablen var eine rationale Funktion in
     * einer Exponentialfunktion ist. Beispielsweise wird bei f = exp(2*x) +
     * 5*exp(3*x) true zurückgegeben, jedoch false bei f = exp(x) +
     * exp(2^(1/2)*x).
     */
    public static boolean isRationalFunktionInExp(Expression f, String var, HashSet<Expression> argumentsInExp) {
        if (!f.contains(var)) {
            return true;
        }
        if (f instanceof Constant) {
            return true;
        }
        if (f instanceof Variable) {
            return !((Variable) f).getName().equals(var);
        }
        if (f instanceof BinaryOperation) {
            if (f.isPower()) {
                if (!((BinaryOperation) f).getLeft().contains(var)) {
                    Function fAsExp = ((BinaryOperation) f).getLeft().ln().mult(((BinaryOperation) f).getRight()).exp();
                    return isRationalFunktionInExp(fAsExp, var, argumentsInExp);
                }
                return isRationalFunktionInExp(((BinaryOperation) f).getLeft(), var, argumentsInExp) && ((BinaryOperation) f).getRight().isIntegerConstant();
            }
            return isRationalFunktionInExp(((BinaryOperation) f).getLeft(), var, argumentsInExp) && isRationalFunktionInExp(((BinaryOperation) f).getRight(), var, argumentsInExp) && areQuotientsOfTermsRational(argumentsInExp);
        }
        if (f instanceof Function) {
            if (!((Function) f).getType().equals(TypeFunction.exp)) {
                return false;
            }
            Expression argumentOfExp = ((Function) f).getLeft();
            if (areQuotientsRational(argumentOfExp, argumentsInExp)) {
                argumentsInExp.add(argumentOfExp);
                return true;
            }
            return false;
        }
        return !f.contains(var);
    }

    /**
     * Ersetzt in einer rationalen Funktion in trigonometrischen Funktionen
     * Ausdrücke cos(n*x) und sin(m*x) durch die Ausdrücke sin(x) und cos(x).
     * VORAUSSETZUNG: f ist eine rationale Funktion in trigonometrischen
     * Funktionen (in der Variablen var). Ferner muss m und n im Integerbereich
     * liegen.
     */
    public static Expression expandRationalFunctionInTrigonometricalFunctions(Expression f, String var) {
        if (f instanceof BinaryOperation) {
            return new BinaryOperation(expandRationalFunctionInTrigonometricalFunctions(((BinaryOperation) f).getLeft(), var), expandRationalFunctionInTrigonometricalFunctions(((BinaryOperation) f).getRight(), var), ((BinaryOperation) f).getType());
        }
        if (f.isFunction() && f.contains(var)) {
            Expression argument = ((Function) f).getLeft();
            if (f.isFunction(TypeFunction.sin) || f.isFunction(TypeFunction.cos)
                    && (argument.equals(Variable.create(var)) || argument.isProduct() && ((BinaryOperation) argument).getLeft().isIntegerConstant() && ((BinaryOperation) argument).getRight().equals(Variable.create(var)))) {
                /*
                 Es werden nur Ausdrücke der Form sin(n*x) oder cos(n*x) vereinfach, wenn
                 |n| <= maximaler Grad eines Polynoms für das Lösen von Polynomgleichungen
                 gilt. GRUND: sin(n*x) und cos(n*x) werden zu Polynomen vom Grad n in
                 sin(x) und cos(x).
                 */
                int n = 1;
                if (argument.isProduct()) {
                    if (((Constant) ((BinaryOperation) argument).getLeft()).getValue().toBigInteger().abs().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                        return f;
                    }
                    n = ((Constant) ((BinaryOperation) argument).getLeft()).getValue().toBigInteger().intValue();
                    if (Math.abs(n) > ComputationBounds.BOUND_COMMAND_MAX_DEGREE_OF_POLYNOMIAL_EQUATION) {
                        return f;
                    }
                }
                if (f.isFunction(TypeFunction.sin)) {
                    return SimplifyTrigonometricalRelations.getSinOfMultipleArgument(var, n);
                } else if (f.isFunction(TypeFunction.cos)) {
                    return SimplifyTrigonometricalRelations.getCosOfMultipleArgument(var, n);
                }
            }
        }
        return f;
    }

    /**
     * Ersetzt in einer rationalen Funktion in trigonometrischen Funktionen
     * Ausdrücke cos(n*x) und sin(m*x) durch die Ausdrücke sin(x) und cos(x).
     * VORAUSSETZUNG: f ist eine rationale Funktion in trigonometrischen
     * Funktionen (in der Variablen var). Ferner muss m und n im Integerbereich
     * liegen.
     */
    public static Expression expandRationalFunctionInTrigonometricalFunctions(Expression f, TypeExpansion type) {
        if (f instanceof BinaryOperation) {
            return new BinaryOperation(expandRationalFunctionInTrigonometricalFunctions(((BinaryOperation) f).getLeft(), type), 
                    expandRationalFunctionInTrigonometricalFunctions(((BinaryOperation) f).getRight(), type), ((BinaryOperation) f).getType());
        }
        if (f.isFunction()) {
            Expression argument = ((Function) f).getLeft();
            if (f.isFunction(TypeFunction.sin) || f.isFunction(TypeFunction.cos)
                    && (argument instanceof Variable || argument.isProduct() && ((BinaryOperation) argument).getLeft().isIntegerConstant() && ((BinaryOperation) argument).getRight() instanceof Variable)) {
                /*
                 Es werden nur Ausdrücke der Form sin(n*x) oder cos(n*x) vereinfach, wenn
                 |n| <= maximaler Grad eines Polynoms für das Lösen von Polynomgleichungen
                 gilt. GRUND: sin(n*x) und cos(n*x) werden zu Polynomen vom Grad n in
                 sin(x) und cos(x).
                 */
                int n = 1;
                Expression variable = argument;
                if (argument.isProduct()) {
                    if (((Constant) ((BinaryOperation) argument).getLeft()).getValue().toBigInteger().abs().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                        return f;
                    }

                    // Je nach Modus wird nur bis zu einer bestimmten Potenz entwickelt.
                    int bound = 0;
                    switch (type) {
                        case SHORT:
                            bound = bound = ComputationBounds.BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_SHORT_EXPANSION;
                            break;
                        case MODERATE:
                            bound = bound = ComputationBounds.BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_MODERATE_EXPANSION;
                            break;
                        case POWERFUL:
                            bound = bound = ComputationBounds.BOUND_ALGEBRA_MAX_NUMBER_OF_SUMMANDS_IN_POWERFUL_EXPANSION;
                            break;
                    }

                    n = ((Constant) ((BinaryOperation) argument).getLeft()).getValue().toBigInteger().intValue();
                    variable = ((BinaryOperation) argument).getRight();
                    if (Math.abs(n) > bound) {
                        return f;
                    }
                }
                if (f.isFunction(TypeFunction.sin)) {
                    return SimplifyTrigonometricalRelations.getSinOfMultipleArgument(variable, n);
                } else if (f.isFunction(TypeFunction.cos)) {
                    return SimplifyTrigonometricalRelations.getCosOfMultipleArgument(variable, n);
                }
            }
        }
        return f;
    }

    // Allgemeine Hilfsmethoden um festzustellen, ob ein HashSet von Expressions
    // nur Ausdrücke enthält, deren paarweise Quotienten rational sind.
    /**
     * Hilfsmethode. Gibt zurück, ob alle Verhältnisse von Ausdrücken in terms
     * und expr rational sind.
     */
    private static boolean areQuotientsRational(Expression expr, HashSet<Expression> terms) {
        try {
            Iterator iter = terms.iterator();
            while (iter.hasNext()) {
                if (!((Expression) iter.next()).div(expr).simplify().isIntegerConstantOrRationalConstant()) {
                    return false;
                }
            }
            return true;
        } catch (EvaluationException e) {
            return false;
        }
    }

    public static boolean doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable(Expression f, String var) {
        if (!f.contains(var) || f instanceof Variable) {
            return true;
        }
        if (f instanceof BinaryOperation) {
            return doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable(((BinaryOperation) f).getLeft(), var) && doArgumentsOfTrigonometricalFunctionsContainOnlyMultiplesOfVariable(((BinaryOperation) f).getRight(), var);
        }
        if (f.isFunction(TypeFunction.sin) || f.isFunction(TypeFunction.cos) || f.isFunction(TypeFunction.tan) || f.isFunction(TypeFunction.cot) || f.isFunction(TypeFunction.sec) || f.isFunction(TypeFunction.cosec)) {
            Expression argument = ((Function) f).getLeft();
            return !argument.contains(var) || argument.equals(Variable.create(var)) || argument.isProduct() && ((BinaryOperation) argument).getLeft().isIntegerConstant() && ((BinaryOperation) argument).getRight().equals(Variable.create(var));
        }
        return false;
    }

    /**
     * Hilfsmethode. Gibt zurück, ob f eine rationale Funktion in var ist.
     */
    public static boolean isRationalFunction(Expression f, String var) {
        if (!f.contains(var)) {
            return true;
        }
        if (f.equals(Variable.create(var))) {
            return true;
        }
        if (f instanceof BinaryOperation) {
            if (f.isNotPower()) {
                return isRationalFunction(f, var) && isRationalFunction(f, var);
            } else if (f.isPower() && ((BinaryOperation) f).getRight().isIntegerConstant()) {
                return isRationalFunction(((BinaryOperation) f).getLeft(), var);
            }
        }
        /*
         Falls f eine Instanz von Operator oder SelfDefinedFunction ist, in
         welchem var vorkommt, dann false zurückgeben.
         */
        return false;
    }

    /**
     * Hilfsmethode. Gibt zurück, ob g eine rationale Funktion in f ist.
     */
    public static boolean isRationalFunctionIn(Expression f, Expression g, String var) {
        if (!g.contains(var)) {
            return true;
        }
        if (g.equivalent(f)) {
            return true;
        }
        if (g instanceof BinaryOperation) {
            if (g.isNotPower()) {
                return isRationalFunctionIn(f, ((BinaryOperation) g).getLeft(), var) && isRationalFunctionIn(f, ((BinaryOperation) g).getRight(), var);
            } else if (g.isPower() && ((BinaryOperation) g).getRight().isIntegerConstant() && ((BinaryOperation) g).getRight().isNonNegative()) {
                return isRationalFunctionIn(f, ((BinaryOperation) g).getLeft(), var);
            }
        }
        /*
         Falls f eine Instanz von Operator oder SelfDefinedFunction ist, in
         welchem var vorkommt, dann false zurückgeben.
         */
        return false;
    }

    /**
     * Entscheidet, ob f bzgl. der Variablen var eine algebraische Funktion in
     * den trigonometrischen Funktionen sin und cos ist.<br>
     * BEISPIEL: bei f = sin(2*x) + 5*cos(3*x) true zurückgegeben, jedoch false
     * bei f = sin(x) + cos(2^(1/2)*x).
     */
    public static boolean isRationalFunktionInTrigonometricalFunctions(Expression f, String var, HashSet<Expression> argumentsInTrigonometricFunctions) {
        if (!f.contains(var)) {
            return true;
        }
        if (f instanceof Constant) {
            return true;
        }
        if (f instanceof Variable) {
            return !((Variable) f).getName().equals(var);
        }
        if (f instanceof BinaryOperation) {
            if (f.isPower()) {
                return isRationalFunktionInTrigonometricalFunctions(((BinaryOperation) f).getLeft(), var, argumentsInTrigonometricFunctions) && ((BinaryOperation) f).getRight().isIntegerConstant();
            }
            return isRationalFunktionInTrigonometricalFunctions(((BinaryOperation) f).getLeft(), var, argumentsInTrigonometricFunctions) && isRationalFunktionInTrigonometricalFunctions(((BinaryOperation) f).getRight(), var, argumentsInTrigonometricFunctions) && areQuotientsOfTermsRational(argumentsInTrigonometricFunctions);
        }
        if (f instanceof Function) {
            if (!((Function) f).getType().equals(TypeFunction.sin) && !((Function) f).getType().equals(TypeFunction.cos) && !((Function) f).getType().equals(TypeFunction.tan) && !((Function) f).getType().equals(TypeFunction.cot) && !((Function) f).getType().equals(TypeFunction.sec) && !((Function) f).getType().equals(TypeFunction.cosec)) {
                return false;
            }
            Expression argumentOfExp = ((Function) f).getLeft();
            if (areQuotientsRational(argumentOfExp, argumentsInTrigonometricFunctions)) {
                argumentsInTrigonometricFunctions.add(argumentOfExp);
                return true;
            }
            return false;
        }
        return !f.contains(var);
    }

}
