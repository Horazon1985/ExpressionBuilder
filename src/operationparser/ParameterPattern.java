package operationparser;

import java.util.ArrayList;

public class ParameterPattern {

    public static enum ParamType{
        equation, expr, integer, logexpr, matexpr, var;
    }
    
    public static enum Multiplicity{
        one, plus;
    }
    
    public static final String equation = "expr=expr";
    public static final String expr = "expr";
    public static final String integer = "int";
    public static final String logexpr = "logexpr";
    public static final String matexpr = "matexpr";
    public static final String var = "var";

    public static final String none = "none";
    public static final String notin = "!";

    public static final String multPlus = "+";
    
    private final ParamType paramType;
    private final Multiplicity mulitplicity;
    private final ArrayList<String> restrictions = new ArrayList<>();
    
    public ParameterPattern(ParamType paramType, Multiplicity multiplicity, ArrayList<String> restrictions){
        this.paramType = paramType;
        this.mulitplicity = multiplicity;
        this.restrictions.addAll(restrictions);
    }
    
    public ParamType getParamType() {
        return paramType;
    }

    public Multiplicity getMultiplicity() {
        return mulitplicity;
    }

    public ArrayList<String> getRestrictions() {
        return restrictions;
    }
    
}
