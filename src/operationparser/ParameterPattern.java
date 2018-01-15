package operationparser;

import java.util.ArrayList;
import java.util.List;

public class ParameterPattern {

    public static enum ParamType {

        equation, expr, integer, logexpr, matexpr, uniqueindet, indet, uniquevar, var, type;

        /** 
         * Liefert die Rolle des zugehörigen Parametertyps.
         */
        public ParamRole getRole() {
            if (this.equals(integer)){
                return ParamRole.INTEGER;
            }
            if (this.equals(type)) {
                return ParamRole.TYPE;
            }
            if (this.equals(uniqueindet) || this.equals(indet) || this.equals(uniquevar) || this.equals(var)) {
                return ParamRole.VARIABLE;
            }
            return ParamRole.EXPRESSION;
        }

    }

    public static enum ParamRole {

        EXPRESSION, INTEGER, TYPE, VARIABLE;
    }

    public static enum Multiplicity {

        one, plus;
    }

    public static final String none = "none";
    public static final String notin = "!";

    public static final String multPlus = "+";

    private final ParamType paramType;
    private final Multiplicity mulitplicity;
    private final List<String> restrictions = new ArrayList<>();

    public ParameterPattern(ParamType paramType, Multiplicity multiplicity, List<String> restrictions) {
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

    public List<String> getRestrictions() {
        return restrictions;
    }

}
