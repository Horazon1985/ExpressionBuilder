package abstractexpressions.output;

import abstractexpressions.interfaces.AbstractExpression;

public class EditableAbstractExpression {

    private final AbstractExpression abstrExpr;
    private final boolean editable;
    
    public EditableAbstractExpression(AbstractExpression abstrExpr, boolean editable){
        this.abstrExpr = abstrExpr;
        this.editable = editable;
    }
    
    public AbstractExpression getAbstractExpression(){
        return this.abstrExpr;
    }

    public boolean isEditable(){
        return this.editable;
    }
    
}
