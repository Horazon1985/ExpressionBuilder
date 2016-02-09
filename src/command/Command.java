package command;

public class Command {

    private TypeCommand type;
    private Object[] params;

    // Patterns für die einzelnen Befehle.
    public static final String patternApprox = "approx()";
    public static final String patternCCNF = "ccnf(logexpr)";
    public static final String patternCDNF = "cdnf(logexpr)";
    public static final String patternClear = "clear()";
    public static final String patternDefFuncs = "deffuncs()";
    public static final String patternDefVars = "defvars()";
    public static final String patternEigenvalues = "eigenvalues(matexpr)";
    public static final String patternEigenvectors = "eigenvectors(matexpr)";
    public static final String patternEuler = "euler(integer(0,2147483647))";
    public static final String patternExpand = "expand(expr)";
    public static final String patternExtremaOneVar = "extrema(expr(0,1))";
    public static final String patternExtremaWithParameter = "extrema(expr,var)";
    public static final String patternExtremaApprox = "extrema(expr(0,1),expr(0,0),expr(0,0))";
    public static final String patternExtremaApproxWithNumberOfIntervals = "extrema(expr(0,1),expr(0,0),expr(0,0),integer(0,2147483647))";
    public static final String patternKer = "ker(matexpr)";
    public static final String patternPi = "pi(integer(0,2147483647))";
    public static final String patternPlotCurve2D = "plotcurve2d(matexpr(0,1),expr(0,0),expr(0,0))";
    public static final String patternPlotCurve3D = "plotcurve3d(matexpr(0,1),expr(0,0),expr(0,0))";
    public static final String patternPlotImplicit = "plotimplicit(equation(0,2),expr(0,0),expr(0,0),expr(0,0),expr(0,0))";
    public static final String patternRegressionLine = "regressionline(matexpr,matexpr+)";
    public static final String patternSolveOneVar = "solve(equation(0,1))";
    public static final String patternSolveWithParameter = "solve(equation,var)";
    public static final String patternSolveApprox = "solve(equation(0,1),expr(0,0),expr(0,0))";
    public static final String patternSolveApproxWithNumberOfIntervals = "solve(equation(0,1),expr(0,0),expr(0,0),integer(0,2147483647))";
    public static final String patternSolveSystem = "solvesystem(equation+,uniquevar+)";
    public static final String patternTable = "table(logexpr)";
    public static final String patternUndefVars = "undefvars(var+)";
    public static final String patternUndefFuncs = "undeffuncs(var+)";
    public static final String patternUndefAllVars = "undefallvars()";
    public static final String patternUndefAllFuncs = "undefallfuncs()";
    public static final String patternUndefAll = "undefall()";
    
    public Command(){
    }
    
    public Command(TypeCommand type, Object[] params){
        this.type = type;
        this.params = params;
    }
    
    public TypeCommand getTypeCommand() {
        return this.type;
    }

    public Object[] getParams() {
        return this.params;
    }

    /**
     * Für Ausgabe benötigt. Der Typ, als String ausgeschrieben, ist der
     * Befehlsname.
     */
    public String getName() {
        return type.toString();
    }
    
    public void setType(TypeCommand type) {
        this.type = type;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    /**
     * Gibt den entsprechenden Typ des Operators zurück, der zum String operator
     * (welcher in der Konsole eingegeben wird) gehört.
     */
    public static TypeCommand getTypeFromName(String command) {
        return TypeCommand.valueOf(command);
    }
    
}
