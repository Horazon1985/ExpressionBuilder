package command;

public class Command {

    private TypeCommand type;
    private Object[] params;

    // Patterns für die einzelnen Befehle.
    public static final String PATTERN_APPROX = "approx()";
    public static final String PATTERN_CCNF = "ccnf(logexpr)";
    public static final String PATTERN_CDNF = "cdnf(logexpr)";
    public static final String PATTERN_CLEAR = "clear()";
    public static final String PATTERN_DEFFUNCS = "deffuncs()";
    public static final String PATTERN_DEFVARS = "defvars()";
    public static final String PATTERN_EIGENVALUES = "eigenvalues(matexpr)";
    public static final String PATTERN_EIGENVECTORS = "eigenvectors(matexpr)";
    public static final String PATTERN_EULER = "euler(integer(0,2147483647))";
    public static final String PATTERN_EXPAND = "expand(expr)";
    public static final String PATTERN_EXTREMA_ONE_VAR = "extrema(expr(0,1))";
    public static final String PATTERN_EXTREMA_WITH_PARAMETER = "extrema(expr,var)";
    public static final String PATTERN_EXTREMA_APPROX = "extrema(expr(0,1),expr(0,0),expr(0,0))";
    public static final String PATTERN_EXTREMA_APPROX_WITH_NUMBER_OF_INTERVALS = "extrema(expr(0,1),expr(0,0),expr(0,0),integer(0,2147483647))";
    public static final String PATTERN_KER = "ker(matexpr)";
    public static final String PATTERN_PI = "pi(integer(0,2147483647))";
    public static final String PATTERN_PLOTCURVE2D = "plotcurve2d(matexpr(0,1),expr(0,0),expr(0,0))";
    public static final String PATTERN_PLOTCURVE3D = "plotcurve3d(matexpr(0,1),expr(0,0),expr(0,0))";
    public static final String PATTERN_PLOTIMPLICIT = "plotimplicit(equation(0,2),expr(0,0),expr(0,0),expr(0,0),expr(0,0))";
    public static final String PATTERN_REGRESSIONLINE = "regressionline(matexpr,matexpr+)";
    public static final String PATTERN_SOLVE_ONE_VAR = "solve(equation(0,1))";
    public static final String PATTERN_SOLVE_WITH_PARAMETER = "solve(equation,var)";
    public static final String PATTERN_SOLVE_APPROX = "solve(equation(0,1),expr(0,0),expr(0,0))";
    public static final String PATTERN_SOLVE_APPROX_WITH_NUMBER_OF_INTERVALS = "solve(equation(0,1),expr(0,0),expr(0,0),integer(0,2147483647))";
    public static final String PATTERN_SOLVESYSTEM = "solvesystem(equation+,uniquevar+)";
    public static final String PATTERN_TABLE = "table(logexpr)";
    public static final String PATTERN_UNDEFVARS = "undefvars(var+)";
    public static final String PATTERN_UNDEFFUNCS = "undeffuncs(var+)";
    public static final String PATTERN_UNDEFALLVARS = "undefallvars()";
    public static final String PATTERN_UNDEFALLFUNCS = "undefallfuncs()";
    public static final String PATTERN_UNDEFALL = "undefall()";
    
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
