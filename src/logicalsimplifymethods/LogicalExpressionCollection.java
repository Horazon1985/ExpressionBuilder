package logicalsimplifymethods;

import java.util.HashMap;
import java.util.Iterator;
import logicalexpressionbuilder.LogicalExpression;

public class LogicalExpressionCollection implements Iterable<LogicalExpression> {

    private final HashMap<Integer, LogicalExpression> logTerms;
    private int bound;
    
    public LogicalExpressionCollection(){
        this.logTerms = new HashMap<>();
        this.bound = 0;
    }
    
    public int getBound(){
        return this.bound;
    }
    
    public boolean isEmpty(){
        return this.logTerms.isEmpty();
    }
    
    public LogicalExpression get(int i){
        return this.logTerms.get(i);
    }
    
    public void put(int i, LogicalExpression logExpr){
        if (i < 0){
            return;
        }
        this.logTerms.put(i, logExpr);
        if (i >= this.bound - 1){
            this.bound = i + 1;
        }
    }
    
    public void add(LogicalExpression expr){
        this.logTerms.put(this.bound, expr);
        this.bound++;
    }
    
    public void remove(int i){
        this.logTerms.remove(i);
        for (int j = this.bound - 1; j >= 0; j--){
            if (this.logTerms.get(j) != null){
                this.bound = j + 1;
                return;
            }
        }
        this.bound = 0;
    }
    
    public void clear(){
        this.logTerms.clear();
        this.bound = 0;
    }
    
    @Override
    public String toString(){
        if (this.bound == 0){
            return "[]";
        }
        String result = "[";
        for (int i = 0; i < this.bound; i++){
            if (this.logTerms.get(i) != null){
                result = result + this.logTerms.get(i).toString() + ", ";
            }
        }
        return result.substring(0, result.length() - 2) + "]";
    }
    
    /**
     * Kopiert terms.
     */
    public static LogicalExpressionCollection copy(LogicalExpressionCollection terms) {

        LogicalExpressionCollection result = new LogicalExpressionCollection();
        for (int i = 0; i < terms.getBound(); i++) {
            if (terms.get(i) != null) {
                result.put(i, terms.get(i).copy());
            }
        }
        result.bound = terms.bound;
        return result;

    }
    
    @Override
    public Iterator<LogicalExpression> iterator() {
        return new Iterator<LogicalExpression>() {
            
            private int currentIndex = -1;
            
            @Override
            public boolean hasNext() {
                return this.currentIndex < getBound() - 1;
            }

            @Override
            public LogicalExpression next() {
                for (int i = this.currentIndex + 1; i < getBound() - 1; i++){
                    if (get(i) != null){
                        this.currentIndex = i;
                        return get(i);
                    }
                }
                this.currentIndex = getBound() - 1;
                return get(getBound() - 1);
            }

        };
    }
    
}
