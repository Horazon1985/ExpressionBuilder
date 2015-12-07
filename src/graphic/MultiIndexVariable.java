package graphic;

import expressionbuilder.Variable;
import java.math.BigInteger;

/**
 * Klasse, die eine Variable mit einem Multiindex darstellt. Zulässige Namen
 * sind NUR Buchstaben, eventuell gefolgt von einem oder mehreren Apostrophs.
 */
public class MultiIndexVariable {

    private String name;
    private BigInteger[] indices;

    public MultiIndexVariable(String name, BigInteger[] indices) {
        this.name = name;
        this.indices = indices;
    }

    public MultiIndexVariable(Variable v) {
        if (v.getName().contains("_")) {
            // In diesem Fall besitzt v einen Index.
            String nameWithoutIndex = v.getName();
            nameWithoutIndex = nameWithoutIndex.replace("_", "").replaceAll("0", "").replaceAll("1", "").replaceAll("2", "").replaceAll("3", "").replaceAll("4", "").replaceAll("5", "").replaceAll("6", "").replaceAll("7", "").replaceAll("8", "").replaceAll("7", "");
            this.name = nameWithoutIndex;
            this.indices = new BigInteger[1];
            if (v.getName().contains("'")) {
                this.indices[0] = new BigInteger(v.getName().substring(v.getName().indexOf("_") + 1, v.getName().indexOf("'")));
            } else {
                this.indices[0] = new BigInteger(v.getName().substring(v.getName().indexOf("_") + 1));
            }
        } else {
            this.name = v.getName();
            this.indices = new BigInteger[0];
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigInteger[] getIndices() {
        return indices;
    }

    public void setIndices(BigInteger[] indices) {
        this.indices = indices;
    }
    
    @Override
    public String toString(){
        String result = this.name + "_{";
        for (int i = 0; i < this.indices.length; i++){
            result = result + this.indices[i].toString();
            if (i < this.indices.length - 1){
                result = result + ",";
            }
        }
        return result + "}";
    }

}
