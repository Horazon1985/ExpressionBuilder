package graphic.common;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JTextArea;
import java.awt.event.MouseListener;
import java.util.List;

public class GraphicArea extends JTextArea {

    private final List<GraphicPanelFormula> formulas;
    private final List<Point> formulasCoordinates;
    private static int fontSize;
    private static final int PADDING = 10;

    private final MouseListener mathToolMouseListener;

    public int mathToolGraphicAreaX;
    public int mathToolGraphicAreaY;
    public int mathToolGraphicAreaWidth;
    public int mathToolGraphicAreaHeight;

    public GraphicArea(int x, int y, int width, int height, MouseListener listener) {
        this.formulas = new ArrayList<>();
        this.formulasCoordinates = new ArrayList<>();
        this.setLayout(null);
        this.setEditable(false);
        this.setBounds(x, y, width, height);
        this.mathToolMouseListener = listener;
    }

    public void initializeBounds(int x, int y, int width, int height) {
        this.mathToolGraphicAreaX = x;
        this.mathToolGraphicAreaY = y;
        this.mathToolGraphicAreaWidth = width;
        this.mathToolGraphicAreaHeight = height;
    }

    public static int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        GraphicArea.fontSize = fontSize;
    }

    public List<GraphicPanelFormula> getFormulas() {
        return this.formulas;
    }

    /**
     * Fügt Elemente von out nacheinander der gegebenen Instanz von GraphicArea
     * hinzu.
     */
    public void addComponent(List out) {

        GraphicPanelFormula formula = new GraphicPanelFormula();
        this.add(formula);

        // Sicherheitshalber!
        if (formula.getGraphics() == null) {
            this.remove(formula);
            return;
        }

        formula.setOutput(out);
        formula.initialize(getFontSize());
        this.formulas.add(formula);
        formula.drawFormula();
        setPosition(formula);

        // Beim Initialisieren wird der GraphicArea der MouseListener übergeben, den MathToolGUI implementiert.
        formula.addMouseListener(this.mathToolMouseListener);

    }

    /**
     * Fügt Elemente von out nacheinander der gegebenen Instanz von GraphicArea
     * hinzu.
     */
    public void addComponent(Object... out) {

        GraphicPanelFormula formula = new GraphicPanelFormula();
        this.add(formula);

        // Sicherheitshalber!
        if (formula.getGraphics() == null) {
            this.remove(formula);
            return;
        }

        formula.setOutput(out);
        formula.initialize(getFontSize());
        this.formulas.add(formula);
        formula.drawFormula();
        setPosition(formula);

        // Beim Initialisieren wird der GraphicArea der MouseListener übergeben, den MathToolGUI implementiert.
        formula.addMouseListener(this.mathToolMouseListener);

    }

    /**
     * Richtet, nachdem f zur gegebenen Instanz von GraphicArea hinzugefügt
     * wurde, f korrekt aus, so dass es (mit einem Padding) unterhalb aller
     * zuvor ausgerichteten Ausgaben positioniert wird.
     */
    public void setPosition(GraphicPanelFormula f) {

        if (this.formulasCoordinates.isEmpty()) {
            this.formulasCoordinates.add(new Point(PADDING, f.getHeight() + PADDING));
        } else {
            this.formulasCoordinates.add(new Point(PADDING, this.formulasCoordinates.get(this.formulasCoordinates.size() - 1).y + PADDING + f.getHeight()));
        }
        f.setBounds(this.formulasCoordinates.get(this.formulasCoordinates.size() - 1).x,
                this.formulasCoordinates.get(this.formulasCoordinates.size() - 1).y - f.getHeight(),
                f.getWidth(), f.getHeight());
        updateSize();

        // Beim Initialisieren wird der GraphicArea der MouseListener übergeben, den die MathTool-GUI implementiert.
        if (getMouseListeners().length > 0) {
            addMouseListener(getMouseListeners()[0]);
        }

    }

    /**
     * Berechnet die Maße der gegebenen Instanz von GraphicArea neu. Dies wird
     * benötigt, wenn eine Ausgabe breiter oder tiefer positioniert wird, als
     * die aktuellen Maße zulassen.
     */
    public void updateSize() {

        GraphicPanelFormula formula;
        for (Iterator<GraphicPanelFormula> iterator = formulas.iterator(); iterator.hasNext();) {
            formula = iterator.next();
            if (formula.getWidth() + 10 > this.getWidth()) {
                this.setPreferredSize(new Dimension(formula.getWidth() + 40, this.getHeight()));
                revalidate();
            }
            if (this.formulasCoordinates.get(formulas.indexOf(formula)).y > this.getHeight()) {
                this.setPreferredSize(new Dimension(this.getWidth(), this.formulasCoordinates.get(formulas.indexOf(formula)).y + 40));
                revalidate();
            }
        }

    }

    /**
     * Entfernt alle Ausgaben von der gegegebenen Instanz von GraphicArea.
     */
    public void clearArea() {
        this.formulas.clear();
        this.formulasCoordinates.clear();
        this.removeAll();
        // Standardgröße wiederherstellen.
        this.setBounds(this.mathToolGraphicAreaX, this.mathToolGraphicAreaY,
                this.mathToolGraphicAreaWidth, this.mathToolGraphicAreaHeight);
        validate();
        repaint();
    }

}
