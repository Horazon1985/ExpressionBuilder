package graphic;

import abstractexpressions.matrixexpression.classes.Matrix;
import exceptions.EvaluationException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import lang.translator.Translator;

public abstract class AbstractGraphicPanel2D extends AbstractGraphicPanel {

    final static Color[] fixedColors = {Color.blue, Color.green, Color.orange, Color.red, Color.PINK};

    protected double axeCenterX, axeCenterY;
    protected double maxX, maxY;
    protected int expX, expY;

    protected boolean movable = false;

    protected double[][] specialPoints;

    protected Point lastMousePosition;
    protected double zoomfactor, zoomfactorX, zoomfactorY;

    public AbstractGraphicPanel2D() {
    }
    
    public AbstractGraphicPanel2D(final double maxZoomfactor, final double minZoomfactor) {
        
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    lastMousePosition = e.getPoint();
                    movable = true;
                } else {
                    lastMousePosition = e.getPoint();
                    movable = false;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (movable) {
                    axeCenterX += (lastMousePosition.x - e.getPoint().x) * maxX / 250;
                    axeCenterY += (-lastMousePosition.y + e.getPoint().y) * maxY / 250;
                    lastMousePosition = e.getPoint();
                    repaint();
                } else {
                    if (lastMousePosition.x - e.getPoint().x >= 0 && zoomfactorX < maxZoomfactor
                            || lastMousePosition.x - e.getPoint().x <= 0 && zoomfactorX > minZoomfactor) {
                        maxX = maxX * Math.pow(1.02, lastMousePosition.x - e.getPoint().x);
                        zoomfactorX = zoomfactorX * Math.pow(1.02, lastMousePosition.x - e.getPoint().x);
                    }
                    if (lastMousePosition.y - e.getPoint().y >= 0 && zoomfactorY < maxZoomfactor
                            || lastMousePosition.y - e.getPoint().y <= 0 && zoomfactorY > minZoomfactor) {
                        maxY = maxY * Math.pow(1.02, lastMousePosition.y - e.getPoint().y);
                        zoomfactorY = zoomfactorY * Math.pow(1.02, lastMousePosition.y - e.getPoint().y);
                    }
                    lastMousePosition = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Der Zoomfaktor darf höchstens maxZoomfactor sein (und mindestens minZoomfactor)
                if ((e.getWheelRotation() >= 0 && zoomfactor < maxZoomfactor)
                        || (e.getWheelRotation() <= 0 && zoomfactor > minZoomfactor)) {
                    maxX *= Math.pow(1.1, e.getWheelRotation());
                    maxY *= Math.pow(1.1, e.getWheelRotation());
                    zoomfactor *= Math.pow(1.1, e.getWheelRotation());
                    computeExpXExpY();
                    repaint();
                }

                repaint();
            }
        });
    }

    public void setSpecialPoints(double[][] specialPoints) {
        this.specialPoints = specialPoints;
    }

    /**
     * Setzt die speziellen Punkte mittels Auswertung der Matrizenausdrücke
     * specialPoints.<br>
     * VORAUSSETZUNG: specialPoints sind allesamt (2x1)-Matrizen.
     *
     * @throws EvaluationException
     */
    public void setSpecialPoints(Matrix[] specialPoints) throws EvaluationException {
        this.specialPoints = new double[specialPoints.length][2];
        for (int i = 0; i < specialPoints.length; i++) {
            this.specialPoints[i][0] = specialPoints[i].getEntry(0, 0).evaluate();
            this.specialPoints[i][1] = specialPoints[i].getEntry(1, 0).evaluate();
        }
    }

    public static ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateOutputMessage("GR_Graphic2D_HOLD_DOWN_LEFT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic2D_HOLD_DOWN_RIGHT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic2D_MOVE_MOUSE_WHEEL"));
        return instructions;
    }

    /**
     * VORAUSSETZUNG: der Graph ist bereits initialisiert (bzw. mit
     * Funktionswerten gefüllt). maxX, maxY sind bekannt/initialisiert.
     */
    protected void computeExpXExpY() {

        /*
         Markierungen an den Achsen anbringen; exp_x = max. Exponent einer
         10er-Potenz, die kleiner als der größte x-Wert ist exp_y = min.
         Exponent einer 10er-Potenz, die kleiner als der größte y-Wert ist
         */
        this.expX = 0;
        this.expY = 0;

        if (this.maxX >= 1) {
            while (this.maxX / Math.pow(10, this.expX) >= 1) {
                this.expX++;
            }
            this.expX--;
        } else {
            while (this.maxX / Math.pow(10, this.expX) < 1) {
                this.expX--;
            }
        }

        if (this.maxY >= 1) {
            while (this.maxY / Math.pow(10, this.expY) >= 1) {
                this.expY++;
            }
            this.expY--;
        } else {
            while (this.maxY / Math.pow(10, this.expY) < 1) {
                this.expY--;
            }
        }

    }
    
    /**
     * Berechnet aus Punktkoordinaten (x, y) Koordinaten (x', y') für die
     * graphische Darstellung Voraussetzung: maxX, maxY sind bekannt (und nicht
     * 0).
     */
    protected int[] convertToPixel(double x, double y) {
        int[] pixel = new int[2];
        pixel[0] = 250 + (int) Math.round(250 * (x - axeCenterX) / maxX);
        pixel[1] = 250 - (int) Math.round(250 * (y - axeCenterY) / maxY);
        return pixel;
    }

    /**
     * Berechnet den Achseneintrag m*10^(-k) ohne den Double-typischen
     * Nachkommastellenfehler.
     */
    protected BigDecimal roundAxisEntries(int m, int k) {
        if (k >= 0) {
            return new BigDecimal(m).multiply(BigDecimal.TEN.pow(k));
        }
        return new BigDecimal(m).divide(BigDecimal.TEN.pow(-k));
    }

    /**
     * Zeichnet rote Punkte an wichtigen Stellen (etwa Markierung von
     * Nullstellen etc.)
     */
    protected void drawSpecialPoints(Graphics g, double[][] specialPoints) {
        g.setColor(Color.red);
        if (specialPoints == null) {
            return;
        }
        int[][] specialPointsCoordinates = new int[specialPoints.length][2];
        for (int i = 0; i < specialPoints.length; i++) {
            specialPointsCoordinates[i] = convertToPixel(specialPoints[i][0], specialPoints[i][1]);
            g.fillOval(specialPointsCoordinates[i][0] - 3, specialPointsCoordinates[i][1] - 3, 7, 7);
        }
    }
    
}
