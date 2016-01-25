package graphic;

import command.Command;
import abstractexpressions.expression.classes.Expression;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JTextArea;
import abstractexpressions.logicalexpression.classes.LogicalExpression;
import abstractexpressions.matrixexpression.classes.MatrixExpression;

public class GraphicArea extends JTextArea {

    private final ArrayList<GraphicPanelFormula> formulas;
    private final ArrayList<Point> formulasCoordinates;
    private static int fontSize;

    public int mathToolGraphicAreaX;
    public int mathToolGraphicAreaY;
    public int mathToolGraphicAreaWidth;
    public int mathToolGraphicAreaHeight;

    public GraphicArea(int x, int y, int width, int height) {
        this.formulas = new ArrayList<>();
        this.formulasCoordinates = new ArrayList<>();
        this.setLayout(null);
        this.setEditable(false);
        this.setBounds(x, y, width, height);
    }

    public static int getFontSize() {
        return fontSize;
    }

    public void initializeBounds(int x, int y, int width, int height) {
        this.mathToolGraphicAreaX = x;
        this.mathToolGraphicAreaY = y;
        this.mathToolGraphicAreaWidth = width;
        this.mathToolGraphicAreaHeight = height;
    }

    public ArrayList<GraphicPanelFormula> getFormulas() {
        return this.formulas;
    }

    public void setFontSize(int fontSize) {
        GraphicArea.fontSize = fontSize;
    }

    public void addComponent(Expression f) {

        GraphicPanelFormula graphicPresentationOfFormula = new GraphicPanelFormula();
        this.add(graphicPresentationOfFormula);

        // Sicherheitshalber!
        if (graphicPresentationOfFormula.getGraphics() == null) {
            this.remove(graphicPresentationOfFormula);
            return;
        }

        graphicPresentationOfFormula.setExpr(f);
        graphicPresentationOfFormula.setTypeGraphicFormula(TypeGraphicFormula.EXPRESSION);
        graphicPresentationOfFormula.initialize(getFontSize());
        formulas.add(graphicPresentationOfFormula);
        graphicPresentationOfFormula.drawFormula();
        setPosition(graphicPresentationOfFormula);

    }

    public void addComponent(LogicalExpression f) {

        GraphicPanelFormula graphicPresentationOfFormula = new GraphicPanelFormula();
        this.add(graphicPresentationOfFormula);

        // Sicherheitshalber!
        if (graphicPresentationOfFormula.getGraphics() == null) {
            this.remove(graphicPresentationOfFormula);
            return;
        }

        graphicPresentationOfFormula.setLogExpr(f);
        graphicPresentationOfFormula.setTypeGraphicFormula(TypeGraphicFormula.LOGICAL_EXPRESSION);
        graphicPresentationOfFormula.initialize(getFontSize());
        formulas.add(graphicPresentationOfFormula);
        graphicPresentationOfFormula.drawFormula();
        setPosition(graphicPresentationOfFormula);

    }

    public void addComponent(MatrixExpression f) {

        GraphicPanelFormula graphicPresentationOfFormula = new GraphicPanelFormula();
        this.add(graphicPresentationOfFormula);

        // Sicherheitshalber!
        if (graphicPresentationOfFormula.getGraphics() == null) {
            this.remove(graphicPresentationOfFormula);
            return;
        }

        graphicPresentationOfFormula.setMatExpr(f);
        graphicPresentationOfFormula.setTypeGraphicFormula(TypeGraphicFormula.MATRIX_EXPRESSION);
        graphicPresentationOfFormula.initialize(getFontSize());
        formulas.add(graphicPresentationOfFormula);
        graphicPresentationOfFormula.drawFormula();
        setPosition(graphicPresentationOfFormula);

    }

    public void addComponent(Command c) {

        GraphicPanelFormula graphicPresentationOfFormula = new GraphicPanelFormula();
        this.add(graphicPresentationOfFormula);

        // Sicherheitshalber!
        if (graphicPresentationOfFormula.getGraphics() == null) {
            this.remove(graphicPresentationOfFormula);
            return;
        }

        graphicPresentationOfFormula.setCommand(c);
        graphicPresentationOfFormula.setTypeGraphicFormula(TypeGraphicFormula.COMMAND);
        graphicPresentationOfFormula.initialize(getFontSize());
        formulas.add(graphicPresentationOfFormula);
        graphicPresentationOfFormula.drawFormula();
        setPosition(graphicPresentationOfFormula);

    }

    public void addComponent(String t) {

        GraphicPanelFormula graphicPresentationOfFormula = new GraphicPanelFormula();
        this.add(graphicPresentationOfFormula);

        // Sicherheitshalber!
        if (graphicPresentationOfFormula.getGraphics() == null) {
            this.remove(graphicPresentationOfFormula);
            return;
        }

        graphicPresentationOfFormula.setText(t);
        graphicPresentationOfFormula.setTypeGraphicFormula(TypeGraphicFormula.TEXT);
        graphicPresentationOfFormula.initialize(getFontSize());
        formulas.add(graphicPresentationOfFormula);
        graphicPresentationOfFormula.drawFormula();
        setPosition(graphicPresentationOfFormula);

    }

    public void addComponent(ArrayList out) {

        GraphicPanelFormula graphicPresentationOfFormula = new GraphicPanelFormula();
        this.add(graphicPresentationOfFormula);

        // Sicherheitshalber!
        if (graphicPresentationOfFormula.getGraphics() == null) {
            this.remove(graphicPresentationOfFormula);
            return;
        }

        graphicPresentationOfFormula.setOutput(out);
        graphicPresentationOfFormula.setTypeGraphicFormula(TypeGraphicFormula.OUTPUT);
        graphicPresentationOfFormula.initialize(getFontSize());
        formulas.add(graphicPresentationOfFormula);
        graphicPresentationOfFormula.drawFormula();
        setPosition(graphicPresentationOfFormula);

    }

    public void addComponent(Object... out) {

        GraphicPanelFormula graphicPresentationOfFormula = new GraphicPanelFormula();
        this.add(graphicPresentationOfFormula);

        // Sicherheitshalber!
        if (graphicPresentationOfFormula.getGraphics() == null) {
            this.remove(graphicPresentationOfFormula);
            return;
        }

        graphicPresentationOfFormula.setOutput(out);
        graphicPresentationOfFormula.setTypeGraphicFormula(TypeGraphicFormula.OUTPUT);
        graphicPresentationOfFormula.initialize(getFontSize());
        formulas.add(graphicPresentationOfFormula);
        graphicPresentationOfFormula.drawFormula();
        setPosition(graphicPresentationOfFormula);

    }

    public void setPosition(GraphicPanelFormula f) {

        if (formulasCoordinates.isEmpty()) {
            formulasCoordinates.add(new Point(10, f.getHeight()));
        } else {
            formulasCoordinates.add(new Point(10, formulasCoordinates.get(formulasCoordinates.size() - 1).y + 10 + f.getHeight()));
        }
        f.setBounds(formulasCoordinates.get(formulasCoordinates.size() - 1).x,
                formulasCoordinates.get(formulasCoordinates.size() - 1).y - f.getHeight(),
                f.getWidth(), f.getHeight());
        updateSize();

    }

    public void updateSize() {

        GraphicPanelFormula formula;
        for (Iterator<GraphicPanelFormula> iterator = formulas.iterator(); iterator.hasNext();) {
            formula = iterator.next();
            if (formula.getWidth() + 10 > this.getWidth()) {
                this.setPreferredSize(new Dimension(formula.getWidth() + 40, this.getHeight()));
                revalidate();
            }
            if (formulasCoordinates.get(formulas.indexOf(formula)).y > this.getHeight()) {
                this.setPreferredSize(new Dimension(this.getWidth(), formulasCoordinates.get(formulas.indexOf(formula)).y + 40));
                revalidate();
            }
        }

    }

    public void clearArea() {
        formulas.clear();
        formulasCoordinates.clear();
        this.removeAll();
        // Standardgröße wiederherstellen.
        this.setBounds(this.mathToolGraphicAreaX, this.mathToolGraphicAreaY,
                this.mathToolGraphicAreaWidth, this.mathToolGraphicAreaHeight - 50);
        validate();
        repaint();
    }

}
