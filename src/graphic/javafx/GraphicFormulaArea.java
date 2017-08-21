package graphic.javafx;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

public class GraphicFormulaArea extends TextArea {

    private final GridPane mainGrid;

    private final List<GraphicCanvasFormula> FORMULAS;
    private final List<Point> formulasCoordinates;
    private static int fontSize;

    private final EventHandler<MouseEvent> detailsEventHandler;

    private static final String BACKGROUND_COLOR_CSS = "-fx-background-color: #ffffff;";

//    public int mathToolGraphicAreaX;
//    public int mathToolGraphicAreaY;
//    public int mathToolGraphicAreaWidth;
//    public int mathToolGraphicAreaHeight;
    public GraphicFormulaArea(EventHandler<MouseEvent> handler) {
        this.FORMULAS = new ArrayList<>();
        this.formulasCoordinates = new ArrayList<>();
        this.setEditable(false);
//        setLayoutX(0);
//        setLayoutY(0);
//        setWidth(width);
//        setHeight(height);

        this.mainGrid = new GridPane();
        this.mainGrid.setAlignment(Pos.CENTER);
        this.mainGrid.setHgap(10);
        this.mainGrid.setVgap(10);
        this.mainGrid.setPadding(new Insets(10, 10, 10, 10));
        this.mainGrid.setStyle(BACKGROUND_COLOR_CSS);

        BorderPane root = new BorderPane();
        root.setCenter(mainGrid);

        this.detailsEventHandler = handler;
    }

//    public void initializeBounds(int x, int y, int width, int height) {
//        this.mathToolGraphicAreaX = x;
//        this.mathToolGraphicAreaY = y;
//        this.mathToolGraphicAreaWidth = width;
//        this.mathToolGraphicAreaHeight = height;
//    }
    public static int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        GraphicFormulaArea.fontSize = fontSize;
    }

    public List<GraphicCanvasFormula> getFormulas() {
        return this.FORMULAS;
    }

    /**
     * Fügt Elemente von out nacheinander der gegebenen Instanz von GraphicArea
     * hinzu.
     */
    public void addComponent(List out) {
        GraphicCanvasFormula formula = new GraphicCanvasFormula();
        this.mainGrid.add(formula, 0, FORMULAS.size());

        formula.setOutput(out);
        formula.initialize(getFontSize());
        this.FORMULAS.add(formula);
        formula.drawFormula();

        formula.setOnMouseClicked(this.detailsEventHandler);
    }

    /**
     * Fügt Elemente von out nacheinander der gegebenen Instanz von GraphicArea
     * hinzu.
     */
    public void addComponent(Object... out) {
        GraphicCanvasFormula formula = new GraphicCanvasFormula();
//        this.mainGrid.add(formula, 0, FORMULAS.size());
        this.mainGrid.add(new Button("Jo!"), 0, FORMULAS.size());

        formula.setOutput(out);
        formula.initialize(getFontSize());
        this.FORMULAS.add(formula);
        formula.drawFormula();

        formula.setOnMouseClicked(this.detailsEventHandler);
    }

//    /**
//     * Richtet, nachdem f zur gegebenen Instanz von GraphicArea hinzugefügt
//     * wurde, f korrekt aus, so dass es (mit einem Padding) unterhalb aller
//     * zuvor ausgerichteten Ausgaben positioniert wird.
//     */
//    public void setPosition(GraphicPanelFormula f) {
//
//        if (this.formulasCoordinates.isEmpty()) {
//            this.formulasCoordinates.add(new Point(10, f.getHeight()));
//        } else {
//            this.formulasCoordinates.add(new Point(10, this.formulasCoordinates.get(this.formulasCoordinates.size() - 1).y + 10 + f.getHeight()));
//        }
//        f.setBounds(this.formulasCoordinates.get(this.formulasCoordinates.size() - 1).x,
//                this.formulasCoordinates.get(this.formulasCoordinates.size() - 1).y - f.getHeight(),
//                f.getWidth(), f.getHeight());
//        updateSize();
//
//        // Beim Initialisieren wird der GraphicArea der MouseListener übergeben, den MathToolGUI implementiert.
//        if (getMouseListeners().length > 0) {
//            addMouseListener(getMouseListeners()[0]);
//        }
//
//    }
//    /**
//     * Berechnet die Maße der gegebenen Instanz von GraphicArea neu. Dies wird
//     * benötigt, wenn eine Ausgabe breiter oder tiefer positioniert wird, als
//     * die aktuellen Maße zulassen.
//     */
//    public void updateSize() {
//
//        GraphicPanelFormula formula;
//        for (Iterator<GraphicCanvasFormula> iterator = FORMULAS.iterator(); iterator.hasNext();) {
//            formula = iterator.next();
//            if (formula.getWidth() + 10 > this.getWidth()) {
//                this.setPreferredSize(new Dimension(formula.getWidth() + 40, this.getHeight()));
//                revalidate();
//            }
//            if (this.formulasCoordinates.get(FORMULAS.indexOf(formula)).y > this.getHeight()) {
//                this.setPreferredSize(new Dimension(this.getWidth(), this.formulasCoordinates.get(FORMULAS.indexOf(formula)).y + 40));
//                revalidate();
//            }
//        }
//
//    }
    /**
     * Entfernt alle Ausgaben von der gegegebenen Instanz von GraphicArea.
     */
    public void clearArea() {
        this.FORMULAS.clear();
        this.formulasCoordinates.clear();
        this.mainGrid.getChildren().clear();
//        // Standardgröße wiederherstellen.
//        this.setBounds(this.mathToolGraphicAreaX, this.mathToolGraphicAreaY,
//                this.mathToolGraphicAreaWidth, this.mathToolGraphicAreaHeight - 50);
    }

}
