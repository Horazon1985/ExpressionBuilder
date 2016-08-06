package graphic;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import lang.translator.Translator;

public abstract class AbstractGraphicPanel3D extends AbstractGraphicPanel implements Runnable {

    // Parameter für 3D-Graphen
    // Boolsche Variable, die angibt, ob der Graph gerade rotiert oder nicht.
    protected boolean isRotating;
    
    //Radien für die Grundellipse
    protected double bigRadius, smallRadius;
    protected double height, heightProjection;
    /*
     Neigungswinkel des Graphen: angle = horizontaler Winkel (er wird
     inkrementiert, wenn der Graph im Uhrzeigersinn rotiert) verticalAngle =
     Winkel, unter dem man die dritte Achse sieht. 0 = man schaut seitlich auf
     den Graphen, 90 = man schaut von oben auf den Graphen.
     */
    protected double angle, verticalAngle;

    /*
     Die boolsche Variable isAngleMeant ist true <-> der aktuelle Winkel
     (welcher durch das Auslösen eines MouseEvent verändert wird) ist angle.
     Ansonsten verticalAngle.
     */
    protected boolean isAngleMeant = true;
    protected Point lastMousePosition;

    protected double zoomfactor;
    
    protected double maxX, maxY, maxZ;
    protected int expX, expY, expZ;
    
    public AbstractGraphicPanel3D() {
        this.isRotating = false;
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
    
    protected void computeExpXExpYExpZ() {

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
     * Berechnet aus Punktkoordinaten (x, y, z) Koordinaten (x', y') für die graphische Darstellung
     */ 
    protected int[] convertToPixel(double x, double y, double z) {

        double angleAbsc = getGraphicalAngle(this.bigRadius, this.smallRadius, this.angle);
        double angleOrd;
        if (this.angle < 90) {
            angleOrd = getGraphicalAngle(this.bigRadius, this.smallRadius, this.angle + 270);
        } else {
            angleOrd = getGraphicalAngle(this.bigRadius, this.smallRadius, this.angle - 90);
        }

        //pixels sind die Pixelkoordinaten für die Graphische Darstellung von (x, y, z)
        int[] pixel = new int[2];

        //Berechnung von pixels[0]
        double x_1, x_2;

        if (angleAbsc == 0) {
            x_1 = this.bigRadius * x / this.maxX;
            x_2 = 0;
        } else if (angleAbsc == 90) {
            x_1 = 0;
            x_2 = this.bigRadius * y / this.maxY;
        } else if (angleAbsc == 180) {
            x_1 = -this.bigRadius * x / this.maxX;
            x_2 = 0;
        } else if (angleAbsc == 270) {
            x_1 = 0;
            x_2 = -this.bigRadius * y / this.maxY;
        } else if (angleAbsc < 90) {
            x_1 = (x / this.maxX) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleAbsc * Math.PI / 180) / this.smallRadius, 2));
            x_2 = (y / this.maxY) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleOrd * Math.PI / 180) / this.smallRadius, 2));
        } else if (angleAbsc < 180) {
            x_1 = -(x / this.maxX) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleAbsc * Math.PI / 180) / this.smallRadius, 2));
            x_2 = (y / this.maxY) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleOrd * Math.PI / 180) / this.smallRadius, 2));
        } else if (angleAbsc < 270) {
            x_1 = -(x / this.maxX) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleAbsc * Math.PI / 180) / this.smallRadius, 2));
            x_2 = -(y / this.maxY) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleOrd * Math.PI / 180) / this.smallRadius, 2));
        } else {
            x_1 = (x / this.maxX) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleAbsc * Math.PI / 180) / this.smallRadius, 2));
            x_2 = -(y / this.maxY) * this.bigRadius / Math.sqrt(1 + Math.pow(this.bigRadius * Math.tan(angleOrd * Math.PI / 180) / this.smallRadius, 2));
        }

        pixel[0] = (int) (250 + x_1 + x_2);

        //Berechnung von pixels[1]
        double y_1, y_2, y_3;

        if (angleAbsc == 0) {
            y_1 = 0;
            y_2 = -this.smallRadius * y / this.maxY;
        } else if (angleAbsc == 90) {
            y_1 = this.smallRadius * x / this.maxX;
            y_2 = 0;
        } else if (angleAbsc == 180) {
            y_1 = 0;
            y_2 = this.smallRadius * y / this.maxY;
        } else if (angleAbsc == 270) {
            y_1 = -this.smallRadius * x / this.maxX;
            y_2 = 0;
        } else {
            y_1 = x_1 * Math.tan(angleAbsc * Math.PI / 180);
            y_2 = x_2 * Math.tan(angleOrd * Math.PI / 180);
        }

        //maximaler Funktionswert (also max_z) soll h Pixel betragen
        y_3 = -this.height * z / this.maxZ;
        pixel[1] = (int) (250 + y_1 + y_2 + y_3);

        return pixel;

    }
    
    /**
     * Berechnet aus dem Winkel "angle" den Winkel, welcher in der graphischen Darstellung auftaucht
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
    
    public static ArrayList<String> getInstructions() {
        ArrayList<String> instructions = new ArrayList<>();
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_HOLD_DOWN_LEFT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_HOLD_DOWN_RIGHT_MOUSE_BUTTON"));
        instructions.add(Translator.translateOutputMessage("GR_Graphic3D_MOVE_MOUSE_WHEEL"));
        return instructions;
    }
    
    @Override
    public void run() {
        while (this.isRotating) {

            this.angle = this.angle + 1;
            if (this.angle >= 360) {
                this.angle = this.angle - 360;
            }
            if (this.angle < 0) {
                this.angle = this.angle + 360;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            repaint();

        }
    }
    
    
}
