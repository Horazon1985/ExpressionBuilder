package operationparser;

import java.util.ArrayList;

public class ParameterPattern {

    public static enum ParamType {

        equation, expr, integer, logexpr, matexpr, uniquevar, var;

        /** 
         * Liefert die Rolle des zugehörigen Parametertyps.
         */
        public ParamRole getRole() {
            if (this.equals(integer)){
                return ParamRole.INTEGER;
            }
            if (this.equals(uniquevar) || this.equals(var)) {
                return ParamRole.VARIABLE;
            }
            return ParamRole.EXPRESSION;
        }

    }

    public static enum ParamRole {

        EXPRESSION, INTEGER, VARIABLE;
    }

    public static enum Multiplicity {

        one, plus;
    }

    public static final String none = "none";
    public static final String notin = "!";

    public static final String multPlus = "+";

    private final ParamType paramType;
    private final Multiplicity mulitplicity;
    private final ArrayList<String> restrictions = new ArrayList<>();

    public ParameterPattern(ParamType paramType, Multiplicity multiplicity, ArrayList<String> restrictions) {
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
