package graphic;

import abstractexpressions.expression.classes.Variable;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Klasse, die eine Variable mit einem Multiindex darstellt. Zulässige Namen
 * sind NUR Buchstaben, eventuell gefolgt von einem oder mehreren Apostrophs.
 */
public class MultiIndexVariable {

    private String name;
    private ArrayList<BigInteger> indices;

    public MultiIndexVariable(String name, ArrayList<BigInteger> indices) {
        this.name = name;
        this.indices = indices;
    }

    public MultiIndexVariable(String var) {
        if (var.contains("_")) {
            // In diesem Fall besitzt v einen Index.
            String nameWithoutIndex = var;
            nameWithoutIndex = nameWithoutIndex.replace("_", "").replaceAll("0", "").replaceAll("1", "").replaceAll("2", "").replaceAll("3", "").replaceAll("4", "").replaceAll("5", "").replaceAll("6", "").replaceAll("7", "").replaceAll("8", "").replaceAll("7", "");
            this.name = nameWithoutIndex;
            this.indices = new ArrayList<>();
            if (var.contains("'")) {
                this.indices.add(new BigInteger(var.substring(var.indexOf("_") + 1, var.indexOf("'"))));
            } else {
                this.indices.add(new BigInteger(var.substring(var.indexOf("_") + 1)));
            }
        } else {
            this.name = var;
            this.indices = new ArrayList<>();
        }
    }
    
    public MultiIndexVariable(Variable v) {
        this(v.getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<BigInteger> getIndices() {
        return indices;
    }

    public void setIndices(ArrayList<BigInteger> indices) {
        this.indices = indices;
    }
    
    @Override
    public String toString(){
        String result = this.name + "_{";
        for (int i = 0; i < this.indices.size(); i++){
            result = result + this.indices.get(i).toString();
            if (i < this.indices.size() - 1){
                result = result + ",";
            }
        }
        return result + "}";
    }

}
