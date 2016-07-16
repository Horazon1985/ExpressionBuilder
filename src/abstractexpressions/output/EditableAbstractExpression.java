package abstractexpressions.output;

import abstractexpressions.interfaces.AbstractExpression;

public class EditableAbstractExpression {

    private final AbstractExpression abstrExpr;
    
    public EditableAbstractExpression(AbstractExpression abstrExpr){
        this.abstrExpr = abstrExpr;
    }
    
    public AbstractExpression getAbstractExpression(){
        return this.abstrExpr;
    }
    
}
