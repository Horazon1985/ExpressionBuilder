package graphic;

import exceptions.EvaluationException;
import abstractexpressions.expression.classes.Expression;
import abstractexpressions.expression.classes.Variable;
import abstractexpressions.matrixexpression.classes.Matrix;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import lang.translator.Translator;

public class GraphicPanelVectorField3D extends JPanel implements Runnable, Exportable {

    //Parameter für 3D-Graphen
    //Boolsche Variable, die angibt, ob der Graph gerade rotiert oder nicht.
    private boolean isRotating;
    //Variablennamen der ersten und der zweiten Achse
    private String varAbsc, varOrd, varAppl;
    //Array, indem die Punkte am Graphen gespeichert sind
    private Matrix vectorFieldExpr;
    private final ArrayList<double[]> vectorField3D = new ArrayList<>();

    private final Color color = Color.blue;

    private double zoomfactor;
    private double maxX, maxY, maxZ;
    private int expX, expY, expZ;

    //Radien für die Grundellipse
    private double bigRadius, smallRadius;
    private double height, heightProjection;
    /*
     Neigungswinkel des Graphen: angle = horizontaler Winkel (er wird
     inkrementiert, wenn der Graph im Uhrzeigersinn rotiert) vertical_angle =
     Winkel, unter dem man die dritte Achse sieht. 0 = man schaut seitlich auf
     den Graphen, 90 = man schaut von oben auf den Graphen.
     */
    private double angle, verticalAngle;
    /*
     Die boolsche Variable is_angle_meant ist true <-> der aktuelle Winkel
     (welcher durch das Auslösen eines MouseEvent verändert wird) ist angle.
     Ansonsten vertical_angle.
     */
    private boolean isAngleMeant = true;
    private Point lastMousePosition;

    public GraphicPanelVectorField3D() {

        isRotating = false;

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    isAngleMeant = true;
                    lastMousePosition = e.getPoint();
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    isAngleMeant = false;
                    lastMousePosition = e.getPoint();
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
                if (isAngleMeant) {
                    angle += (lastMousePosition.x - e.getPoint().x) * 0.5;

                    if (angle >= 360) {
                        angle = angle - 360;
                    }
                    if (angle < 0) {
                        angle = angle + 360;
                    }

                    lastMousePosition = e.getPoint();
                    repaint();
                } else {
                    verticalAngle -= (lastMousePosition.y - e.getPoint().y) * 0.3;

                    if (verticalAngle >= 90) {
                        verticalAngle = 90;
                    }
                    if (verticalAngle < 1) {
                        verticalAngle = 1;
                    }

                    smallRadius = bigRadius * Math.sin(verticalAngle / 180 * Math.PI);
                    height = heightProjection * Math.cos(verticalAngle / 180 * Math.PI);

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
                // Der Zoomfaktor darf höchstens 10 sein (und mindestens 0.1)
                if (((e.getWheelRotation() >= 0) && (zoomfactor < 10))
                        || ((e.getWheelRotation() <= 0) && (zoomfactor > 0.1))) {
                    maxX *= Math.pow(1.1, e.getWheelRotation());
                    maxY *= Math.pow(1.1, e.getWheelRotation());
                    maxZ *= Math.pow(1.1, e.getWheelRotation());
                    zoomfactor *= Math.pow(1.1, e.getWheelRotation());
                    repaint();
                }
            }
        });

    }

    public Color getColor() {
        return this.color;
    }

    public Matrix getVectorFieldExpression() {
        return this.vectorFieldExpr;
    }

    public static ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_HOLD_DOWN_LEFT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_HOLD_DOWN_RIGHT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_MOVE_MOUSE_WHEEL"));
        return instructions;
    }

    public void setIsRotating(boolean isRotating) {
        this.isRotating = isRotating;
    }

    public void setVectorFieldExpression(Expression[] vectorFieldComponents) {
        Matrix vectorField = new Matrix(vectorFieldComponents);
        this.vectorFieldExpr = vectorField;
        this.vectorField3D.clear();
    }

    public void setParameters(String varAbsc, String varOrd, String varAppl, double bigRadius, double heightProjection, double angle, double verticalAngle) {
        this.varAbsc = varAbsc;
        this.varOrd = varOrd;
        this.varAppl = varAppl;
        this.heightProjection = heightProjection;
        this.bigRadius = bigRadius;
        this.smallRadius = bigRadius * Math.sin(verticalAngle / 180 * Math.PI);
        this.height = heightProjection * Math.cos(verticalAngle / 180 * Math.PI);
        this.angle = angle;
        this.verticalAngle = verticalAngle;
        this.zoomfactor = 1;
    }

    /**
     * Voraussetzung: vectorFieldExpr, varAbsc und varOrd sind bereits gesetzt.
     *
     * @throws EvaluationException
     */
    public void computeScreenSizes(Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd,
            Expression exprApplStart, Expression exprApplEnd) throws EvaluationException {

        double varAbscStart = exprAbscStart.evaluate();
        double varAbscEnd = exprAbscEnd.evaluate();
        double varOrdStart = exprOrdStart.evaluate();
        double varOrdEnd = exprOrdEnd.evaluate();
        double varApplStart = exprApplStart.evaluate();
        double varApplEnd = exprApplEnd.evaluate();

        this.maxX = Math.max(Math.abs(varAbscStart), Math.abs(varAbscEnd));
        this.maxY = Math.max(Math.abs(varOrdStart), Math.abs(varOrdEnd));
        this.maxZ = Math.max(Math.abs(varApplStart), Math.abs(varApplEnd));

    }

    /**
     * Voraussetzung: curve3D ist bereits initialisiert (bzw. mit
     * Funktionswerten gefüllt), max_x, max_y, max_z sind bekannt/initialisiert.
     */
    private void computeExpXExpYExpZ() {

        this.expX = 0;
        this.expY = 0;
        this.expZ = 0;

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

        if (this.maxZ >= 1) {
            while (this.maxZ / Math.pow(10, this.expZ) >= 1) {
                this.expZ++;
            }
            this.expZ--;
        } else {
            while (this.maxZ / Math.pow(10, this.expZ) < 1) {
                this.expZ--;
            }
        }

    }

    /**
     * Berechnet die Gitterpunkte für den 3D-Graphen aus dem Ausdruck expr.
     *
     * @throws EvaluationException
     */
    private void expressionToVectorField(Expression exprAbscStart, Expression exprAbscEnd, Expression exprOrdStart, Expression exprOrdEnd, Expression exprApplStart, Expression exprApplEnd) throws EvaluationException {

        double varAbscStart = exprAbscStart.evaluate();
        double varAbscEnd = exprAbscEnd.evaluate();
        double varOrdStart = exprOrdStart.evaluate();
        double varOrdEnd = exprOrdEnd.evaluate();
        double varApplStart = exprApplStart.evaluate();
        double varApplEnd = exprApplEnd.evaluate();

        this.vectorField3D.clear();
        double[] vectorFieldArrow;

        /*
         Falls this.expr.get(i) konstant ist -> den Funktionswert nur
         einmal berechnen!
         */
        if (this.vectorFieldExpr.isConstant()) {
            Variable.setValue(this.varAbsc, varAbscStart);
            Variable.setValue(this.varOrd, varOrdStart);
            Variable.setValue(this.varAppl, varApplStart);
            double constAbscValue, constOrdValue, constApplValue;
            try {
                constAbscValue = this.vectorFieldExpr.getEntry(0, 0).evaluate();
                constOrdValue = this.vectorFieldExpr.getEntry(1, 0).evaluate();
                constApplValue = this.vectorFieldExpr.getEntry(2, 0).evaluate();
            } catch (EvaluationException e) {
                constAbscValue = Double.NaN;
                constOrdValue = Double.NaN;
                constApplValue = Double.NaN;
            }
            for (int i = 0; i <= 20; i++) {
                for (int j = 0; j <= 20; j++) {
                    for (int k = 0; k <= 20; k++) {
                        vectorFieldArrow = new double[6];
                        vectorFieldArrow[0] = varAbscStart + (varAbscEnd - varAbscStart) * i / 20;
                        vectorFieldArrow[1] = varOrdStart + (varOrdEnd - varOrdStart) * j / 20;
                        vectorFieldArrow[2] = varApplStart + (varApplEnd - varApplStart) * k / 20;
                        vectorFieldArrow[3] = vectorFieldArrow[0] + constAbscValue;
                        vectorFieldArrow[4] = vectorFieldArrow[1] + constOrdValue;
                        vectorFieldArrow[4] = vectorFieldArrow[2] + constApplValue;
                    }
                }
            }
        } else {
            Variable.setValue(this.varAbsc, varAbscStart);
            Variable.setValue(this.varOrd, varOrdStart);
            Variable.setValue(this.varAppl, varApplStart);
            for (int i = 0; i <= 20; i++) {
                for (int j = 0; j <= 20; j++) {
                    for (int k = 0; k <= 20; k++) {
                        vectorFieldArrow = new double[6];
                        vectorFieldArrow[0] = varAbscStart + (varAbscEnd - varAbscStart) * i / 20;
                        vectorFieldArrow[1] = varOrdStart + (varOrdEnd - varOrdStart) * j / 20;
                        vectorFieldArrow[2] = varApplStart + (varApplEnd - varApplStart) * k / 20;
                        Variable.setValue(this.varAbsc, varAbscStart + (varAbscEnd - varAbscStart) * i / 20);
                        Variable.setValue(this.varOrd, varOrdStart + (varOrdEnd - varOrdStart) * j / 20);
                        Variable.setValue(this.varAppl, varApplStart + (varApplEnd - varApplStart) * k / 20);
                        try {
                            vectorFieldArrow[3] = vectorFieldArrow[0] + this.vectorFieldExpr.getEntry(0, 0).evaluate();
                            vectorFieldArrow[4] = vectorFieldArrow[1] + this.vectorFieldExpr.getEntry(1, 0).evaluate();
                            vectorFieldArrow[5] = vectorFieldArrow[2] + this.vectorFieldExpr.getEntry(2, 0).evaluate();
                        } catch (EvaluationException e) {
                            vectorFieldArrow[3] = Double.NaN;
                            vectorFieldArrow[4] = Double.NaN;
                            vectorFieldArrow[5] = Double.NaN;
                        }
                        this.vectorField3D.add(vectorFieldArrow);
                    }
                }
            }
        }

    }

    /**
     * Berechnet aus dem Winkel "angle" den Winkel, welcher in der graphischen
     * Darstellung auftaucht
     */
    private double getGraphicalAngle(double bigRadius, double smallRadius, double angle) {
        //Vorausgesetzt: 0 <= real_angle < 360
        if ((angle == 0) || (angle == 90) || (angle == 180) || (angle == 270)) {
            return angle;
        } else if (angle < 90) {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI;
        } else if (angle < 180) {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI + 180;
        } else if (angle < 270) {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI + 180;
        } else {
            return Math.atan(smallRadius * Math.tan(angle * Math.PI / 180) / bigRadius) * 180 / Math.PI + 360;
        }
    }

    /**
     * Berechnet aus Punktkoordinaten (x, y, z) Koordinaten (x', y') für die
     * graphische Darstellung
     */
    private int[] convertToPixel(double x, double y, double z, double bigRadius, double smallRadius, double height, double angle) {

        double angle_x = getGraphicalAngle(bigRadius, smallRadius, angle);
        double angle_y;
        if (angle < 90) {
            angle_y = getGraphicalAngle(bigRadius, smallRadius, angle + 270);
        } else {
            angle_y = getGraphicalAngle(bigRadius, smallRadius, angle - 90);
        }

        //pixels sind die Pixelkoordinaten für die Graphische Darstellung von (x, y, z)
        int[] pixel = new int[2];

        //Berechnung von pixels[0]
        double x_1, x_2;

        if (angle_x == 0) {
            x_1 = bigRadius * x / maxX;
            x_2 = 0;
        } else if (angle_x == 90) {
            x_1 = 0;
            x_2 = bigRadius * y / maxY;
        } else if (angle_x == 180) {
            x_1 = -bigRadius * x / maxX;
            x_2 = 0;
        } else if (angle_x == 270) {
            x_1 = 0;
            x_2 = -bigRadius * y / maxY;
        } else if (angle_x < 90) {
            x_1 = (x / maxX) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angle_x * Math.PI / 180) / smallRadius, 2));
            x_2 = (y / maxY) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angle_y * Math.PI / 180) / smallRadius, 2));
        } else if (angle_x < 180) {
            x_1 = -(x / maxX) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angle_x * Math.PI / 180) / smallRadius, 2));
            x_2 = (y / maxY) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angle_y * Math.PI / 180) / smallRadius, 2));
        } else if (angle_x < 270) {
            x_1 = -(x / maxX) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angle_x * Math.PI / 180) / smallRadius, 2));
            x_2 = -(y / maxY) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angle_y * Math.PI / 180) / smallRadius, 2));
        } else {
            x_1 = (x / maxX) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angle_x * Math.PI / 180) / smallRadius, 2));
            x_2 = -(y / maxY) * bigRadius / Math.sqrt(1 + Math.pow(bigRadius * Math.tan(angle_y * Math.PI / 180) / smallRadius, 2));
        }

        pixel[0] = (int) (250 + x_1 + x_2);

        //Berechnung von pixels[1]
        double y_1, y_2, y_3;

        if (angle_x == 0) {
            y_1 = 0;
            y_2 = -smallRadius * y / maxY;
        } else if (angle_x == 90) {
            y_1 = smallRadius * x / maxX;
            y_2 = 0;
        } else if (angle_x == 180) {
            y_1 = 0;
            y_2 = smallRadius * y / maxY;
        } else if (angle_x == 270) {
            y_1 = -smallRadius * x / maxX;
            y_2 = 0;
        } else {
            y_1 = x_1 * Math.tan(angle_x * Math.PI / 180);
            y_2 = x_2 * Math.tan(angle_y * Math.PI / 180);
        }

        //maximaler Funktionswert (also max_z) soll h Pixel betragen
        y_3 = -height * z / maxZ;
        pixel[1] = (int) (250 + y_1 + y_2 + y_3);

        return pixel;

    }

    /**
     * Berechnet die Pixelkoordinaten des (gröberen) Graphen. Voraussetzung:
     * max_x, max_y sind bekannt!
     */
    private int[][] convertVectorFieldToGraphicalVectorField(Graphics g) {

        int[][] resultGraphicalVectorField3D = new int[this.vectorField3D.size()][4];
        int[] vectorStart, vectorEnd;
        for (int i = 0; i < this.vectorField3D.size(); i++) {
            vectorStart = convertToPixel(this.vectorField3D.get(i)[0], this.vectorField3D.get(i)[1], this.vectorField3D.get(i)[2], bigRadius, smallRadius, height, angle);
            vectorEnd = convertToPixel(this.vectorField3D.get(i)[3], this.vectorField3D.get(i)[4], this.vectorField3D.get(i)[5], bigRadius, smallRadius, height, angle);
            resultGraphicalVectorField3D[i] = new int[]{vectorStart[0], vectorStart[1], vectorEnd[0], vectorEnd[1]};
        }
        return resultGraphicalVectorField3D;

    }

    //Berechnet den Achseneintrag m*10^(-k) ohne den Double-typischen Nachkommastellenfehler.
    private BigDecimal roundAxisEntries(int m, int k) {
        if (k >= 0) {
            return new BigDecimal(m).multiply(BigDecimal.TEN.pow(k));
        }
        return new BigDecimal(m).divide(BigDecimal.TEN.pow(-k));
    }

    /**
     * Die folgenden vier Prozeduren zeichnen Niveaulinien am Rand des Graphen.
     * Voraussetzung: max_x, max_y, max_z, R, r, h, angle sind
     * bekannt/initialisiert.
     */
    private void drawLevelsOnEast(Graphics g, double bigRadius, double smallRadius, double height, double angle) {

        if ((angle >= 0) && (angle <= 180)) {
            return;
        }

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.maxX;
        border[0][1] = this.maxY;
        border[0][2] = this.maxZ;
        border[1][0] = this.maxX;
        border[1][1] = -this.maxY;
        border[1][2] = this.maxZ;
        border[2][0] = this.maxX;
        border[2][1] = -this.maxY;
        border[2][2] = -this.maxZ;
        border[3][0] = this.maxX;
        border[3][1] = this.maxY;
        border[3][2] = -this.maxZ;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], bigRadius, smallRadius, height, angle);
        }

        //Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        //Achse beschriften
        if (angle >= 270) {
            g.drawString("y", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("y", borderPixels[0][0] - g.getFontMetrics().stringWidth("y") - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZ / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, this.expZ) <= this.maxZ) {
            lineLevel[0][0] = this.maxX;
            lineLevel[0][1] = this.maxY;
            lineLevel[0][2] = i * Math.pow(10, this.expZ);
            lineLevel[1][0] = this.maxX;
            lineLevel[1][1] = -this.maxY;
            lineLevel[1][2] = i * Math.pow(10, this.expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 270) && ((i + 1) * Math.pow(10, this.expZ) <= this.maxZ)) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        //Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (this.maxY / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, this.expY) <= this.maxY) {
            lineLevel[0][0] = this.maxX;
            lineLevel[0][1] = i * Math.pow(10, this.expY);
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = this.maxX;
            lineLevel[1][1] = i * Math.pow(10, this.expY);
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (angle >= 270) {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, this.expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, this.expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsOnWest(Graphics g, double bigRadius, double smallRadius, double height, double angle) {

        if ((angle >= 180) && (angle <= 360)) {
            return;
        }

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = -this.maxX;
        border[0][1] = -this.maxY;
        border[0][2] = this.maxZ;
        border[1][0] = -this.maxX;
        border[1][1] = this.maxY;
        border[1][2] = this.maxZ;
        border[2][0] = -this.maxX;
        border[2][1] = this.maxY;
        border[2][2] = -this.maxZ;
        border[3][0] = -this.maxX;
        border[3][1] = -this.maxY;
        border[3][2] = -this.maxZ;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], bigRadius, smallRadius, height, angle);
        }

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (angle >= 90) {
            g.drawString("y", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("y", borderPixels[0][0] - g.getFontMetrics().stringWidth("y") - 10, borderPixels[0][1] + 15);
        }

        //horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZ / Math.pow(10, expZ));
        int i = -bound;

        while (i * Math.pow(10, expZ) <= this.maxZ) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = i * Math.pow(10, expZ);
            lineLevel[1][0] = -this.maxX;
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = i * Math.pow(10, expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 90) && ((i + 1) * Math.pow(10, this.expZ) <= this.maxZ)) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der zweiten Achse zeichnen
        bound = (int) (this.maxY / Math.pow(10, expY));
        i = -bound;

        while (i * Math.pow(10, expY) <= this.maxY) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = i * Math.pow(10, expY);
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = -this.maxX;
            lineLevel[1][1] = i * Math.pow(10, expY);
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (angle >= 90) {
                g.drawString(String.valueOf(roundAxisEntries(i, expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsOnSouth(Graphics g, double bigRadius, double smallRadius, double height, double angle) {

        if ((angle <= 90) || (angle >= 270)) {
            return;
        }

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.maxX;
        border[0][1] = -this.maxY;
        border[0][2] = this.maxZ;
        border[1][0] = -this.maxX;
        border[1][1] = -this.maxY;
        border[1][2] = this.maxZ;
        border[2][0] = -this.maxX;
        border[2][1] = -this.maxY;
        border[2][2] = -this.maxZ;
        border[3][0] = this.maxX;
        border[3][1] = -this.maxY;
        border[3][2] = -this.maxZ;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], bigRadius, smallRadius, height, angle);
        }

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (angle >= 180) {
            g.drawString("x", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("x", borderPixels[0][0] - g.getFontMetrics().stringWidth("x") - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZ / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, expZ) <= this.maxZ) {
            lineLevel[0][0] = this.maxX;
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = i * Math.pow(10, expZ);
            lineLevel[1][0] = -this.maxX;
            lineLevel[1][1] = -this.maxY;
            lineLevel[1][2] = i * Math.pow(10, expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle > 180) && ((i + 1) * Math.pow(10, this.expZ) <= this.maxZ)) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;

        }

        // Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.maxX / Math.pow(10, expX));
        i = -bound;

        while (i * Math.pow(10, expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, expX);
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = i * Math.pow(10, expX);
            lineLevel[1][1] = -this.maxY;
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (angle >= 180) {
                g.drawString(String.valueOf(roundAxisEntries(i, expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsOnNorth(Graphics g, double bigRadius, double smallRadius, double height, double angle) {

        if ((angle >= 90) && (angle <= 270)) {
            return;
        }

        g.setColor(Color.GRAY);
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = -this.maxX;
        border[0][1] = this.maxY;
        border[0][2] = this.maxZ;
        border[1][0] = this.maxX;
        border[1][1] = this.maxY;
        border[1][2] = this.maxZ;
        border[2][0] = this.maxX;
        border[2][1] = this.maxY;
        border[2][2] = -this.maxZ;
        border[3][0] = -this.maxX;
        border[3][1] = this.maxY;
        border[3][2] = -this.maxZ;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], bigRadius, smallRadius, height, angle);
        }

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);
        // Achse beschriften
        if (angle <= 90) {
            g.drawString("x", borderPixels[1][0] + 10, borderPixels[1][1] + 15);
        } else {
            g.drawString("x", borderPixels[0][0] - g.getFontMetrics().stringWidth("x") - 10, borderPixels[0][1] + 15);
        }

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        int bound = (int) (this.maxZ / Math.pow(10, this.expZ));
        int i = -bound;

        while (i * Math.pow(10, expZ) <= this.maxZ) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = this.maxY;
            lineLevel[0][2] = i * Math.pow(10, expZ);
            lineLevel[1][0] = this.maxX;
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = i * Math.pow(10, expZ);

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if ((angle < 90) && ((i + 1) * Math.pow(10, this.expZ) <= this.maxZ)) {
                g.drawString(String.valueOf(roundAxisEntries(i, expZ)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            }

            i++;
        }

        // Niveaulinien bzgl. der ersten Achse zeichnen
        bound = (int) (this.maxX / Math.pow(10, expX));
        i = -bound;

        while (i * Math.pow(10, expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, expX);
            lineLevel[0][1] = this.maxY;
            lineLevel[0][2] = this.maxZ;
            lineLevel[1][0] = i * Math.pow(10, expX);
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);
            if (angle <= 90) {
                g.drawString(String.valueOf(roundAxisEntries(i, expY)), lineLevelPixels[0][0] + 5, lineLevelPixels[0][1]);
            } else {
                g.drawString(String.valueOf(roundAxisEntries(i, expY)), lineLevelPixels[0][0]
                        - g.getFontMetrics().stringWidth(String.valueOf(roundAxisEntries(i, expY))) - 5,
                        lineLevelPixels[0][1]);
            }

            i++;
        }

    }

    private void drawLevelsBottom(Graphics g, double bigRadius, double smallRadius, double height, double angle) {

        //Zunächst den Rahmen auf dem Boden zeichnen
        double[][] border = new double[4][3];
        int[][] borderPixels = new int[4][2];

        border[0][0] = this.maxX;
        border[0][1] = this.maxY;
        border[0][2] = -this.maxZ;
        border[1][0] = -this.maxX;
        border[1][1] = this.maxY;
        border[1][2] = -this.maxZ;
        border[2][0] = -this.maxX;
        border[2][1] = -this.maxY;
        border[2][2] = -this.maxZ;
        border[3][0] = this.maxX;
        border[3][1] = -this.maxY;
        border[3][2] = -this.maxZ;

        for (int i = 0; i < 4; i++) {
            borderPixels[i] = convertToPixel(border[i][0], border[i][1], border[i][2], bigRadius, smallRadius, height, angle);
        }

        // Rahmen zeichnen
        g.drawLine(borderPixels[0][0], borderPixels[0][1], borderPixels[1][0], borderPixels[1][1]);
        g.drawLine(borderPixels[1][0], borderPixels[1][1], borderPixels[2][0], borderPixels[2][1]);
        g.drawLine(borderPixels[2][0], borderPixels[2][1], borderPixels[3][0], borderPixels[3][1]);
        g.drawLine(borderPixels[3][0], borderPixels[3][1], borderPixels[0][0], borderPixels[0][1]);

        // Horizontale Niveaulinien zeichnen
        double[][] lineLevel = new double[2][3];
        int[][] lineLevelPixels = new int[2][2];

        // Horizontale x-Niveaulinien zeichnen
        int bound = (int) (this.maxX / Math.pow(10, this.expX));
        int i = -bound;

        while (i * Math.pow(10, expX) <= this.maxX) {
            lineLevel[0][0] = i * Math.pow(10, expX);
            lineLevel[0][1] = -this.maxY;
            lineLevel[0][2] = -this.maxZ;
            lineLevel[1][0] = i * Math.pow(10, expX);
            lineLevel[1][1] = this.maxY;
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

        // Horizontale y-Niveaulinien zeichnen
        bound = (int) (this.maxY / Math.pow(10, this.expY));
        i = -bound;

        while (i * Math.pow(10, expY) <= this.maxY) {
            lineLevel[0][0] = -this.maxX;
            lineLevel[0][1] = i * Math.pow(10, expY);
            lineLevel[0][2] = -this.maxZ;
            lineLevel[1][0] = this.maxX;
            lineLevel[1][1] = i * Math.pow(10, expY);
            lineLevel[1][2] = -this.maxZ;

            lineLevelPixels[0] = convertToPixel(lineLevel[0][0], lineLevel[0][1], lineLevel[0][2], bigRadius, smallRadius, height, angle);
            lineLevelPixels[1] = convertToPixel(lineLevel[1][0], lineLevel[1][1], lineLevel[1][2], bigRadius, smallRadius, height, angle);

            g.drawLine(lineLevelPixels[0][0], lineLevelPixels[0][1], lineLevelPixels[1][0], lineLevelPixels[1][1]);

            i++;
        }

    }

    /**
     * Hauptmethode zum Zeichnen von 3D-Vektorfeldern.
     */
    private void drawVectorField3D(Graphics g, double angle) {

        //Zunächst weißen Hintergrund zeichnen.
        g.setColor(Color.white);
        g.fillRect(0, 0, 500, 500);

        computeExpXExpYExpZ();

        drawLevelsOnEast(g, bigRadius, smallRadius, height, angle);
        drawLevelsOnSouth(g, bigRadius, smallRadius, height, angle);
        drawLevelsOnWest(g, bigRadius, smallRadius, height, angle);
        drawLevelsOnNorth(g, bigRadius, smallRadius, height, angle);
        drawLevelsBottom(g, bigRadius, smallRadius, height, angle);

        if (this.vectorField3D.isEmpty()) {
            return;
        }
        
        g.setColor(Color.blue);
        int[][] graphicalVectorField = convertVectorFieldToGraphicalVectorField(g);
        
        for (int[] vectorArrow : graphicalVectorField) {
            if (!Double.isNaN(vectorArrow[0]) && !Double.isInfinite(vectorArrow[0])
                    && !Double.isNaN(vectorArrow[1]) && !Double.isInfinite(vectorArrow[1])
                    && !Double.isNaN(vectorArrow[2]) && !Double.isInfinite(vectorArrow[2])
                    && !Double.isNaN(vectorArrow[3]) && !Double.isInfinite(vectorArrow[3])) {

                g.drawLine(vectorArrow[0], vectorArrow[1], vectorArrow[2], vectorArrow[3]);
                // Pfeilspitze zeichnen (TO DO).

            }
        }

        g.setColor(Color.black);

    }

    public void drawVectorField3D(Expression x_0, Expression x_1, Expression y_0, Expression y_1, 
            Expression z_0, Expression z_1, Expression[] vectorFieldComponents) throws EvaluationException {
        this.zoomfactor = 1;
        setVectorFieldExpression(vectorFieldComponents);
        computeScreenSizes(x_0, x_1, y_0, y_1, z_0, z_1);
        expressionToVectorField(x_0, x_1, y_0, y_1, z_0, z_1);
        drawVectorField3D();
    }

    private void drawVectorField3D() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawVectorField3D(g, angle);
    }

    @Override
    public void run() {
        while (isRotating) {

            angle = angle + 1;
            if (angle >= 360) {
                angle = angle - 360;
            }
            if (angle < 0) {
                angle = angle + 360;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            repaint();
        }
    }

    // Grafikexport.
    @Override
    public void export(String filePath) throws IOException {
        BufferedImage bi = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        paintComponent(g);
        ImageIO.write(bi, "PNG", new File(filePath));
    }

}
