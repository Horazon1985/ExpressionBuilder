package graphic;

import command.Command;
import command.TypeCommand;
import expressionbuilder.BinaryOperation;
import expressionbuilder.Constant;
import expressionbuilder.Expression;
import expressionbuilder.Function;
import expressionbuilder.Operator;
import expressionbuilder.SelfDefinedFunction;
import expressionbuilder.TypeBinary;
import expressionbuilder.TypeFunction;
import expressionbuilder.TypeOperator;
import expressionbuilder.Variable;
import expressionsimplifymethods.ExpressionCollection;
import expressionsimplifymethods.SimplifyUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JPanel;
import logicalexpressionbuilder.LogicalBinaryOperation;
import logicalexpressionbuilder.LogicalConstant;
import logicalexpressionbuilder.LogicalExpression;
import logicalexpressionbuilder.LogicalUnaryOperation;
import logicalexpressionbuilder.LogicalVariable;
import matrixexpressionbuilder.Matrix;
import matrixexpressionbuilder.MatrixBinaryOperation;
import matrixexpressionbuilder.MatrixExpression;
import matrixexpressionbuilder.MatrixFunction;
import matrixexpressionbuilder.MatrixOperator;
import matrixexpressionbuilder.MatrixPower;
import matrixexpressionbuilder.TypeMatrixBinary;
import matrixexpressionbuilder.TypeMatrixOperator;

public class GraphicPanelFormula extends JPanel {

    private Expression expr;
    private LogicalExpression logExpr;
    private MatrixExpression matExpr;
    private Command c;
    private String t;
    private Object[] output;
    private TypeGraphicFormula type;
    private int width, height, fontSize;

    private Color backgroundColor = Color.white;
    private static final Color backgroundColorUnmarked = Color.white;
    private static final Color backgroundColorMarked = new Color(51, 204, 255);

    private static ArrayList<GraphicPanelFormula> formulas = new ArrayList<>();

    public GraphicPanelFormula() {
        formulas.add(this);
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (getBackgroundColor().equals(GraphicPanelFormula.backgroundColorUnmarked)) {
                    setMarked();
                } else {
                    setUnmarked();
                }
                setRestUnmarked();
            }

            @Override
            public void mousePressed(MouseEvent e) {

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

    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public boolean isMarked() {
        return this.backgroundColor.equals(backgroundColorMarked);
    }

    // MouseListener-Methoden
    private void setMarked() {
        this.backgroundColor = backgroundColorMarked;
        repaint();
    }

    private void setUnmarked() {
        this.backgroundColor = backgroundColorUnmarked;
        repaint();
    }

    private void setRestUnmarked() {
        GraphicPanelFormula formula;
        for (Iterator<GraphicPanelFormula> iterator = formulas.iterator(); iterator.hasNext();) {
            formula = iterator.next();
            if (this != formula) {
                formula.setUnmarked();
            }
        }
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    public void setTypeGraphicFormula(TypeGraphicFormula type) {
        this.type = type;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public void setLogExpr(LogicalExpression logExpr) {
        this.logExpr = logExpr;
    }

    public void setMatExpr(MatrixExpression matExpr) {
        this.matExpr = matExpr;
    }

    public void setCommand(Command c) {
        this.c = c;
    }

    public void setText(String t) {
        this.t = t;
    }

    public void setOutput(Object... out) {
        output = new Object[out.length];
        int i = 0;
        for (Object o : out) {
            output[i] = o;
            i++;
        }
    }

    public void setOutput(ArrayList out) {
        output = new Object[out.size()];
        int i = 0;
        for (Object o : out) {
            output[i] = o;
            i++;
        }
    }

    public void initialize(int fontSize) {

        this.fontSize = fontSize;
        Graphics g = this.getGraphics();
        setFont(g, fontSize);

        /*
         Pufferrahmen mit Breite fontsize/2 lassen! GRUND: Buchstaben wie
         beispielsweise g (die einen Haken nach unten besitzen) werden sonst
         nicht vollständig gezeichnet. Die Höhe 0 bei der Methode drawString
         beginnt nämlich beim Kopf von g! Daher bei getLengthOfFormula() und
         getHeightOfFormula() noch this.fontsize hinzuaddieren.
         */
        if (type.equals(TypeGraphicFormula.EXPRESSION)) {
            // Fall: expr zeichnen.
            this.width = getLengthOfExpression(g, this.expr, this.fontSize) + this.fontSize;
            this.height = getHeightOfExpression(g, this.expr, this.fontSize) + this.fontSize;
            this.setBounds(0, 0, this.width, this.height);
        } else if (type.equals(TypeGraphicFormula.LOGICAL_EXPRESSION)) {
            // Fall: logExpr zeichnen.
            this.width = getLengthOfLogicalExpression(g, this.logExpr, this.fontSize) + this.fontSize;
            this.height = getHeightOfLogicalExpression(g, this.logExpr, this.fontSize) + this.fontSize;
            this.setBounds(0, 0, this.width, this.height);
        } else if (type.equals(TypeGraphicFormula.MATRIX_EXPRESSION)) {
            // Fall: matExpr zeichnen.
            this.width = getLengthOfMatrixExpression(g, this.matExpr, this.fontSize) + this.fontSize;
            this.height = getHeightOfMatrixExpression(g, this.matExpr, this.fontSize) + this.fontSize;
            this.setBounds(0, 0, this.width, this.height);
        } else if (type.equals(TypeGraphicFormula.COMMAND)) {
            // Fall: c zeichnen.
            this.width = getLengthOfCommand(g, this.c, this.fontSize) + this.fontSize;
            this.height = getHeightOfCommand(g, this.c, this.fontSize) + this.fontSize;
            this.setBounds(0, 0, this.width, this.height);
        } else if (type.equals(TypeGraphicFormula.TEXT)) {
            // Fall: t zeichnen.
            this.width = g.getFontMetrics().stringWidth(t) + this.fontSize;
            this.height = 2 * this.fontSize;
            this.setBounds(0, 0, this.width, this.height);
        } else if (type.equals(TypeGraphicFormula.OUTPUT)) {
            // Fall: output zeichnen.
            this.width = getLengthOfOutput(g, fontSize, output) + this.fontSize;
            this.height = getHeightOfOutput(g, fontSize, output) + this.fontSize;
            this.setBounds(0, 0, this.width, this.height);
        }

    }

    public void setFont(Graphics g, int fontSize) {
        Font f = new Font("Times New Roman", Font.BOLD, fontSize);
        g.setFont(f);
    }

    private int getSizeForSup(int fontsize) {
        return (int) (0.8 * fontsize);
    }

    private int getSizeForSub(int fontsize) {
        return (int) (0.7 * fontsize);
    }

    public int getHeightOfCommand(Graphics g, Command c, int fontSize) {

        Object[] params = c.getParams();

        int heightBelowCommonCenter = (2 * fontSize) / 5;
        int heightBeyondCommonCenter = (3 * fontSize) / 5;

        if (c.getTypeCommand().equals(TypeCommand.tangent)) {

            // Höhen über und unter dem Zentrum von der Funktionsgleichung ermitteln.
            int heightParameter = getHeightOfExpression(g, (Expression) params[0], fontSize);
            int heightParameterCenter = getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize);

            heightBelowCommonCenter = heightParameterCenter;
            heightBeyondCommonCenter = heightParameter - heightParameterCenter;

            HashMap<String, Expression> varsWithValues = (HashMap<String, Expression>) params[1];
            String varCurrent;

            // Höhen über und unter dem Zentrum von allen Koordinatenausdrücken ermitteln.
            for (Iterator iter = varsWithValues.keySet().iterator(); iter.hasNext();) {
                varCurrent = (String) iter.next();

                heightParameter = getHeightOfExpression(g, varsWithValues.get(varCurrent), fontSize);
                heightParameterCenter = getHeightOfCenterOfExpression(g, varsWithValues.get(varCurrent), fontSize);
                heightBelowCommonCenter = Math.max(heightBelowCommonCenter, heightParameterCenter);
                heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter, heightParameter - heightParameterCenter);

            }

        } else {

            /*
             WICHTIG: In allen Fällen, außer im TANGENT-Fall, können params[i]
             nur Instanzen von Expression, LogicalExpression,
             MatrixExpression, String, Integer sein.
             */
            for (Object param : params) {
                if (param instanceof Expression) {
                    heightBelowCommonCenter = Math.max(heightBelowCommonCenter, getHeightOfCenterOfExpression(g, (Expression) param, fontSize));
                    heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter, getHeightOfExpression(g, (Expression) param, fontSize) - getHeightOfCenterOfExpression(g, (Expression) param, fontSize));
                } else if (param instanceof LogicalExpression) {
                    heightBelowCommonCenter = Math.max(heightBelowCommonCenter, getHeightOfCenterOfLogicalExpression(g, (LogicalExpression) param, fontSize));
                    heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter, getHeightOfLogicalExpression(g, (LogicalExpression) param, fontSize) - getHeightOfCenterOfLogicalExpression(g, (LogicalExpression) param, fontSize));
                } else if (param instanceof MatrixExpression) {
                    heightBelowCommonCenter = Math.max(heightBelowCommonCenter, getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) param, fontSize));
                    heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter, getHeightOfMatrixExpression(g, (MatrixExpression) param, fontSize) - getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) param, fontSize));
                }
                /*
                 Ansonsten ist params[i] eine Instanz von Integer oder von
                 String. Dann nicht tun, denn Strings und Integer haben die
                 kleinstmögliche Höhe und liefern daher keinen Beitrag zur
                 Höhe.
                 */
            }

        }

        return heightBelowCommonCenter + heightBeyondCommonCenter;

    }

    public int getHeightOfCenterOfCommand(Graphics g, Command c, int fontSize) {

        Object[] params = c.getParams();

        if (params.length == 0) {
            return (2 * fontSize) / 5;
        }

        int heightParameterCenter;
        /*
         Zuerst eine Ausnahme behandeln: wenn c.typeCommand ==
         TypeCommand.TANGENT ist. Dann ist params[0] eine Instanz von
         Expression und params[1] eine Instanz von
         HashMap<String, Expression>.
         */
        if (c.getTypeCommand().equals(TypeCommand.tangent)) {

            heightParameterCenter = getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize);

            HashMap<String, Expression> varsWithValues = (HashMap<String, Expression>) params[1];
            String varCurrent;

            // Höhenzentren der einzelnen Koordinatenausdrücke ermitteln.
            for (Iterator iter = varsWithValues.keySet().iterator(); iter.hasNext();) {
                varCurrent = (String) iter.next();
                heightParameterCenter = Math.max(heightParameterCenter, getHeightOfCenterOfExpression(g, varsWithValues.get(varCurrent), fontSize));
            }

        } else {

            /*
             WICHTIG: ist c.typeCommand != TypeCommand.TANGENT, dann kann
             params[i] nur eine Instanz von Expression, LogicalExpression,
             String, Integer sein.
             */
            heightParameterCenter = (2 * fontSize) / 5;

            for (Object param : params) {
                if (param instanceof Expression) {
                    heightParameterCenter = Math.max(heightParameterCenter, getHeightOfCenterOfExpression(g, (Expression) param, fontSize));
                } else if (param instanceof LogicalExpression) {
                    heightParameterCenter = Math.max(heightParameterCenter, getHeightOfCenterOfLogicalExpression(g, (LogicalExpression) param, fontSize));
                } else if (param instanceof MatrixExpression) {
                    heightParameterCenter = Math.max(heightParameterCenter, getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) param, fontSize));
                }
                /*
                 Ansonsten ist params[i] eine Instanz von Integer oder von
                 String. Dann nicht tun, denn Strings und Integer haben die
                 kleinstmögliche Höhe des Zentrums und liefern daher keinen
                 Beitrag zur Höhe des Zentrums.
                 */
            }

        }

        return heightParameterCenter;

    }

    public int getLengthOfCommand(Graphics g, Command c, int fontSize) {

        setFont(g, fontSize);

        Object[] params = c.getParams();

        // result ist bereits die Pixellänge des Befehlsnamens zusammen mit den beiden Klammern.
        int resultLength = g.getFontMetrics().stringWidth(c.getName())
                + 2 * getWidthOfBracket(fontSize) + getWidthOfSignEquals(g, fontSize);

        /*
         Ab hier wird zusätzlich die Gesamtlänge aller Argumente im
         Klammerinneren ermittelt und zu result hinzuaddiert.
         */
        if (c.getTypeCommand().equals(TypeCommand.def)) {

            if (params.length == 2) {

                /**
                 * Fall: Variablendeklaration.
                 */
                resultLength = resultLength + g.getFontMetrics().stringWidth((String) params[0])
                        + getWidthOfSignEquals(g, fontSize) + getLengthOfExpression(g, (Expression) params[1], fontSize);

            } else {

                // Fall: Funktionsdeklaration.
                Object[] originalParams = new Object[params.length];

                originalParams[0] = params[0];

                for (int i = 1; i < params.length - 1; i++) {
                    /*
                     Die Variablennamen in f_arguments haben alle "_ABSTRACT"
                     als Anhängsel. Dieses wird nun beseitigt, um die
                     Originalnamen wiederzubekommen. Die Variablen mit den
                     Originalnamen werden im Array result abgespechert.
                     */
                    originalParams[i] = ((String) params[i]).substring(0, ((String) params[i]).indexOf("_ABSTRACT"));
                }

                Expression f = (Expression) params[params.length - 1];

                for (int i = 1; i < params.length - 1; i++) {
                    f = f.replaceVariable(((String) params[i]), Variable.create((String) originalParams[i]));
                }

                originalParams[params.length - 1] = f;

                // Originalbezeichnungen wurden zurückgewonnen. Nun wird die Länge berechnet.
                resultLength = resultLength + g.getFontMetrics().stringWidth((String) originalParams[0])
                        + 2 * getWidthOfBracket(fontSize) + (params.length - 3) * g.getFontMetrics().stringWidth(", ");

                for (int i = 1; i < originalParams.length - 1; i++) {
                    resultLength = resultLength + g.getFontMetrics().stringWidth((String) originalParams[i]);
                }

                resultLength = resultLength + getWidthOfSignEquals(g, fontSize);
                resultLength = resultLength + getLengthOfExpression(g, (Expression) originalParams[originalParams.length - 1], fontSize);

                return resultLength;

            }

        } else if (c.getTypeCommand().equals(TypeCommand.latex)) {

            resultLength = resultLength + (params.length - 1) * getWidthOfSignEquals(g, fontSize);

            for (Object param : params) {
                resultLength = resultLength + getLengthOfExpression(g, (Expression) param, fontSize);
            }

        } else if (c.getTypeCommand().equals(TypeCommand.plotimplicit)) {

            resultLength = resultLength + 2 * getWidthOfBracket(fontSize)
                    + 4 * g.getFontMetrics().stringWidth(", ") + g.getFontMetrics().stringWidth("=");

            for (Object param : params) {
                resultLength = resultLength + getLengthOfExpression(g, (Expression) param, fontSize);
            }

        } else if (c.getTypeCommand().equals(TypeCommand.plotcurve)) {

            resultLength = resultLength + 2 * getWidthOfBracket(fontSize)
                    + (params.length - 1) * g.getFontMetrics().stringWidth(", ");

            for (Object param : params) {
                resultLength = resultLength + getLengthOfExpression(g, (Expression) param, fontSize);
            }

        } else if (c.getTypeCommand().equals(TypeCommand.solve)) {

            resultLength = resultLength + (params.length - 2) * g.getFontMetrics().stringWidth(", ");

            for (Object param : params) {
                if (param instanceof Expression) {
                    resultLength = resultLength + getLengthOfExpression(g, (Expression) param, fontSize);
                } else if (param instanceof LogicalExpression) {
                    resultLength = resultLength + getLengthOfLogicalExpression(g, (LogicalExpression) param, fontSize);
                } else if (param instanceof Integer) {
                    setFont(g, fontSize);
                    resultLength = resultLength + g.getFontMetrics().stringWidth(String.valueOf((Integer) param));
                } else {
                    // Hier ist params[i] eine Instanz von String;
                    setFont(g, fontSize);
                    resultLength = resultLength + g.getFontMetrics().stringWidth((String) param);
                }
            }

        } else if (c.getTypeCommand().equals(TypeCommand.tangent)) {

            // Die Gesamtlänge aller Kommata und aller "="-Zeichen hinzuaddieren.
            resultLength = resultLength + ((HashMap) params[1]).size() * (g.getFontMetrics().stringWidth(", ") + getWidthOfSignEquals(g, fontSize));
            // Länge der Funktionsvorschrift hinzuaddieren.
            resultLength = resultLength + getLengthOfExpression(g, (Expression) params[0], fontSize);

            HashMap<String, Expression> varsWithValues = (HashMap<String, Expression>) params[1];
            String varCurrent;

            for (Iterator iter = varsWithValues.keySet().iterator(); iter.hasNext();) {
                setFont(g, fontSize);
                varCurrent = (String) iter.next();
                // Pixellänge der jeweiligen Variable hinzuaddieren.
                resultLength = resultLength + g.getFontMetrics().stringWidth(varCurrent);
                // Pixellänge des jeweiligen Variablenwertes hinzuaddieren.
                resultLength = resultLength + getLengthOfExpression(g, (Expression) varsWithValues.get(varCurrent), fontSize);
            }

        } else {

            // Sonstiger Fall.
            resultLength = resultLength + Math.max(params.length - 1, 0) * g.getFontMetrics().stringWidth(", ");

            for (Object param : params) {
                if (param instanceof Expression) {
                    resultLength = resultLength + getLengthOfExpression(g, (Expression) param, fontSize);
                } else if (param instanceof LogicalExpression) {
                    resultLength = resultLength + getLengthOfLogicalExpression(g, (LogicalExpression) param, fontSize);
                } else if (param instanceof MatrixExpression) {
                    resultLength = resultLength + getLengthOfMatrixExpression(g, (MatrixExpression) param, fontSize);
                } else if (param instanceof Integer) {
                    setFont(g, fontSize);
                    resultLength = resultLength + g.getFontMetrics().stringWidth(String.valueOf((Integer) param));
                } else if (param instanceof String) {
                    setFont(g, fontSize);
                    resultLength = resultLength + g.getFontMetrics().stringWidth((String) param);
                } else {
                    // Sollte eigentlich nicht vorkommen, aber sicherheitshalber.
                    resultLength = 0;
                }
            }

        }

        return resultLength;

    }

    public int getHeightOfLogicalExpression(Graphics g, LogicalExpression logExpr, int fontSize) {
        return fontSize;
    }

    public int getHeightOfCenterOfLogicalExpression(Graphics g, LogicalExpression logExpr, int fontSize) {
        return (2 * fontSize) / 5;
    }

    public int getLengthOfLogicalExpression(Graphics g, LogicalExpression logExpr, int fontSize) {

        if (logExpr instanceof LogicalConstant) {
            return getLengthOfLogicalConstant(g, (LogicalConstant) logExpr, fontSize);
        } else if (logExpr instanceof LogicalVariable) {
            return getLengthOfLogicalVariable(g, (LogicalVariable) logExpr, fontSize);
        } else if (logExpr instanceof LogicalUnaryOperation) {
            return getLengthOfLogicalUnaryOperation(g, (LogicalUnaryOperation) logExpr, fontSize);
        } else {
            // Dann ist es eine logische Binäroperation.
            return getLengthOfLogicalBinaryOperation(g, (LogicalBinaryOperation) logExpr, fontSize);
        }

    }

    private int getLengthOfLogicalConstant(Graphics g, LogicalConstant logExpr, int fontSize) {
        setFont(g, fontSize);
        return g.getFontMetrics().stringWidth(logExpr.writeLogicalExpression());
    }

    private int getLengthOfLogicalVariable(Graphics g, LogicalVariable logExpr, int fontSize) {
        setFont(g, fontSize);
        return g.getFontMetrics().stringWidth(logExpr.writeLogicalExpression());
    }

    private int getLengthOfLogicalUnaryOperation(Graphics g, LogicalUnaryOperation logExpr, int fontSize) {
        setFont(g, fontSize);
        if (logExpr.getLeft() instanceof LogicalVariable) {
            return getWidthOfSignNegation(g, fontSize)
                    + getLengthOfLogicalExpression(g, logExpr.getLeft(), fontSize);
        }
        return getWidthOfSignNegation(g, fontSize)
                + getLengthOfLogicalExpression(g, logExpr.getLeft(), fontSize)
                + 2 * getWidthOfBracket(fontSize);
    }

    private int getLengthOfLogicalBinaryOperation(Graphics g, LogicalBinaryOperation logExpr, int fontSize) {

        setFont(g, fontSize);

        if (logExpr.isAnd()) {

            int l_left, l_right;
            if (logExpr.getLeft().isOr() || logExpr.getLeft().isImpl() || logExpr.getLeft().isEquiv()) {
                l_left = getLengthOfLogicalExpression(g, logExpr.getLeft(), fontSize)
                        + 2 * getWidthOfBracket(fontSize);
            } else {
                l_left = getLengthOfLogicalExpression(g, logExpr.getLeft(), fontSize);
            }
            if (logExpr.getRight().isOr() || logExpr.getRight().isImpl() || logExpr.getRight().isEquiv()) {
                l_right = getLengthOfLogicalExpression(g, logExpr.getRight(), fontSize)
                        + 2 * getWidthOfBracket(fontSize);
            } else {
                l_right = getLengthOfLogicalExpression(g, logExpr.getRight(), fontSize);
            }
            return l_left + getWidthOfSignOr(fontSize) + l_right;

        } else if (logExpr.isOr()) {

            int l_left, l_right;
            if (logExpr.getLeft().isImpl() || logExpr.getLeft().isEquiv()) {
                l_left = getLengthOfLogicalExpression(g, logExpr.getLeft(), fontSize)
                        + 2 * getWidthOfBracket(fontSize);
            } else {
                l_left = getLengthOfLogicalExpression(g, logExpr.getLeft(), fontSize);
            }
            if (logExpr.getRight().isImpl() || logExpr.getRight().isEquiv()) {
                l_right = getLengthOfLogicalExpression(g, logExpr.getRight(), fontSize)
                        + 2 * getWidthOfBracket(fontSize);
            } else {
                l_right = getLengthOfLogicalExpression(g, logExpr.getRight(), fontSize);
            }
            return l_left + getWidthOfSignOr(fontSize) + l_right;

        } else if (logExpr.isImpl()) {

            int l_left, l_right;
            if (logExpr.getLeft().isEquiv()) {
                l_left = getLengthOfLogicalExpression(g, logExpr.getLeft(), fontSize)
                        + 2 * getWidthOfBracket(fontSize);
            } else {
                l_left = getLengthOfLogicalExpression(g, logExpr.getLeft(), fontSize);
            }
            if (logExpr.getRight().isEquiv()) {
                l_right = getLengthOfLogicalExpression(g, logExpr.getRight(), fontSize)
                        + 2 * getWidthOfBracket(fontSize);
            } else {
                l_right = getLengthOfLogicalExpression(g, logExpr.getRight(), fontSize);
            }
            return l_left + getWidthOfSignImplication(fontSize) + l_right;

        }

        // Ansonsten ist logExpr eine Äquivalenz.
        return getLengthOfLogicalExpression(g, logExpr.getLeft(), fontSize)
                + getWidthOfSignEquivalence(fontSize)
                + getLengthOfLogicalExpression(g, logExpr.getRight(), fontSize);

    }

    public int getHeightOfMatrixExpression(Graphics g, MatrixExpression matExpr, int fontSize) {

        setFont(g, fontSize);

        if (matExpr instanceof Matrix) {

            Matrix matrix = (Matrix) matExpr;
            int result = (matrix.getRowNumber() - 1) * getWidthOfMatrixSpace(fontSize);
            for (int i = 0; i < matrix.getRowNumber(); i++) {
                result = result + getHeightOfMatrixRow(g, matrix.getRow(i), fontSize);
            }
            return result;

        } else if (matExpr instanceof MatrixBinaryOperation) {

            int heightCenterLeft = getHeightOfCenterOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getLeft(), fontSize);
            int heightCenterRight = getHeightOfCenterOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getRight(), fontSize);
            return Math.max(heightCenterLeft, heightCenterRight)
                    + Math.max(getHeightOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getLeft(), fontSize) - heightCenterLeft,
                            getHeightOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getRight(), fontSize) - heightCenterRight);

        } else if (matExpr instanceof MatrixPower) {

            return getHeightOfMatrixExpression(g, ((MatrixPower) matExpr).getLeft(), fontSize)
                    + getHeightOfExpression(g, ((MatrixPower) matExpr).getRight(), getSizeForSup(fontSize));

        } else if (matExpr instanceof MatrixFunction) {

            return getHeightOfMatrixExpression(g, ((MatrixFunction) matExpr).getLeft(), fontSize);

        } else {

            // Hier ist matExpr eine Instanz von MatrixOperator.
            Object[] params = ((MatrixOperator) matExpr).getParams();

            if (((MatrixOperator) matExpr).getType().equals(TypeMatrixOperator.diff)) {

                if (params.length == 2) {

                    return Math.max(3 * fontSize, getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize));

                } else if (params.length == 3 && params[2] instanceof Integer) {

                    return Math.max(3 * fontSize + 2 * getSizeForSup(fontSize), getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize));

                } else {

                    return Math.max(3 * fontSize + getSizeForSup(fontSize), getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize));

                }

            } else if (((MatrixOperator) matExpr).getType().equals(TypeMatrixOperator.integral)) {

                if (params.length == 2) {

                    // Unbestimmtes Integral.
                    return getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);

                } else {

                    // Bestimmtes Integral.
                    return getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize)
                            + getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize))
                            + getHeightOfExpression(g, (Expression) params[3], getSizeForSup(fontSize));

                }

            } else if (((MatrixOperator) matExpr).getType().equals(TypeMatrixOperator.laplace)) {

                return getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);

            } else if (((MatrixOperator) matExpr).getType().equals(TypeMatrixOperator.prod)
                    || ((MatrixOperator) matExpr).getType().equals(TypeMatrixOperator.sum)) {

                return getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize)
                        + getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize))
                        + getHeightOfExpression(g, (Expression) params[3], getSizeForSup(fontSize));

            } else {

                int heightBelowCommonCenter = (2 * fontSize) / 5;
                int heightBeyondCommonCenter = (3 * fontSize) / 5;
                Object[] left = ((MatrixOperator) matExpr).getParams();

                for (int i = 0; i < left.length; i++) {

                    if (left[i] instanceof Expression) {
                        heightBelowCommonCenter = Math.max(heightBelowCommonCenter, getHeightOfCenterOfExpression(g, (Expression) left[i], fontSize));
                        heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter,
                                getHeightOfExpression(g, (Expression) left[i], fontSize) - getHeightOfCenterOfExpression(g, (Expression) left[i], fontSize));
                    } else if (left[i] instanceof MatrixExpression) {
                        heightBelowCommonCenter = Math.max(heightBelowCommonCenter, getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) left[i], fontSize));
                        heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter,
                                getHeightOfMatrixExpression(g, (MatrixExpression) left[i], fontSize) - getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) left[i], fontSize));
                    }
                    /*
                     Ansonsten ist left[i] eine Instanz von String oder von
                     Integer. In beiden Fällen wird das gleiche berechnet (so,
                     als wäre es eine Variable; die Höhe des Zentrums beträgt
                     40% der Gesamthöhe des Ausdrucks). Dann ändern sich aber
                     heightBelowCommonCenter und heightBeyondCommonCenter
                     NICHT.
                     */

                }

                return heightBelowCommonCenter + heightBeyondCommonCenter;

            }

        }

    }

    public int getHeightOfMatrixRow(Graphics g, Expression[] row, int fontSize) {

        setFont(g, fontSize);

        int heightBelowCommonCenter = (2 * fontSize) / 5;
        int heightBeyondCommonCenter = (3 * fontSize) / 5;
        for (int i = 0; i < row.length; i++) {
            heightBelowCommonCenter = Math.max(heightBelowCommonCenter, getHeightOfCenterOfExpression(g, row[i], fontSize));
            heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter,
                    getHeightOfExpression(g, row[i], fontSize) - getHeightOfCenterOfExpression(g, row[i], fontSize));
        }
        return heightBelowCommonCenter + heightBeyondCommonCenter;

    }

    public int getHeightOfCenterOfMatrixExpression(Graphics g, MatrixExpression matExpr, int fontSize) {

        setFont(g, fontSize);

        if (matExpr instanceof Matrix) {

            Matrix matrix = (Matrix) matExpr;
            int rowNumber = matrix.getRowNumber();
            int heightCenter = 0;
            // Im Folgenden muss unterschieden werden, ob die Zeilenanzahl in m gerade oder ungerade ist.
            if (rowNumber % 2 == 0) {
                for (int i = rowNumber / 2; i < rowNumber; i++) {
                    heightCenter = heightCenter + getHeightOfMatrixRow(g, matrix.getRow(i), fontSize);
                }
                return heightCenter + (rowNumber / 2 - 1) * getWidthOfMatrixSpace(fontSize)
                        + getWidthOfMatrixSpace(fontSize) / 2;
            } else {
                for (int i = (rowNumber + 1) / 2; i < rowNumber; i++) {
                    heightCenter = heightCenter + getHeightOfMatrixRow(g, matrix.getRow(i), fontSize);
                }
                return heightCenter + getHeightOfCenterOfMatrixRow(g, matrix.getRow((rowNumber - 1) / 2), fontSize)
                        + ((rowNumber - 1) / 2) * getWidthOfMatrixSpace(fontSize);
            }

        } else if (matExpr instanceof MatrixBinaryOperation) {

            return Math.max(getHeightOfCenterOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getLeft(), fontSize),
                    getHeightOfCenterOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getRight(), fontSize));

        } else if (matExpr instanceof MatrixPower) {

            return getHeightOfCenterOfMatrixExpression(g, ((MatrixPower) matExpr).getLeft(), fontSize);

        } else if (matExpr instanceof MatrixFunction) {

            return getHeightOfCenterOfMatrixExpression(g, ((MatrixFunction) matExpr).getLeft(), fontSize);

        }

        // Hier ist matExpr eine Instanz von MatrixOperator.
        Object[] params = ((MatrixOperator) matExpr).getParams();

        if (((MatrixOperator) matExpr).getType().equals(TypeMatrixOperator.diff)) {

            if (params.length == 2) {
                return Math.max((3 * fontSize) / 2, getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) params[0], fontSize));
            } else if (params.length == 3 && params[2] instanceof Integer) {
                return Math.max((3 * fontSize) / 2 + getSizeForSup(fontSize), getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) params[0], fontSize));
            } else {
                return Math.max((3 * fontSize) / 2, getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) params[0], fontSize));
            }

        } else if (((MatrixOperator) matExpr).getType().equals(TypeMatrixOperator.integral)) {

            if (params.length == 2) {
                // Unbestimmtes Integral.
                return getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
            } else {
                // Bestimmtes Integral.
                return getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) params[0], fontSize)
                        + getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
            }

        } else if (((MatrixOperator) matExpr).getType().equals(TypeMatrixOperator.laplace)) {
            return getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
        } else if (((MatrixOperator) matExpr).getType().equals(TypeMatrixOperator.prod)
                || ((MatrixOperator) matExpr).getType().equals(TypeMatrixOperator.sum)) {
            return getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) params[0], fontSize)
                    + getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        } else {

            int result = 2 * fontSize / 5;
            for (Object param : ((MatrixOperator) matExpr).getParams()) {
                /*
                 Von allen möglichen Parametern in Operatoren können nur die
                 Parameter, welche Instanzen von Expression oder von
                 MatrixExpression sind, zur Höhe des Zentrums beitragen. Alle
                 anderen haben die Zentrumhöhe 2 * fontsize / 5.
                 */
                if (param instanceof Expression) {
                    result = Math.max(result, getHeightOfCenterOfExpression(g, (Expression) param, fontSize));
                } else if (param instanceof MatrixExpression) {
                    result = Math.max(result, getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) param, fontSize));
                }
            }
            return result;

        }

    }

    public int getHeightOfCenterOfMatrixRow(Graphics g, Expression[] row, int fontSize) {

        setFont(g, fontSize);

        int result = (2 * fontSize) / 5;
        for (int i = 0; i < row.length; i++) {
            result = Math.max(result, getHeightOfCenterOfExpression(g, row[i], fontSize));
        }
        return result;

    }

    public int getLengthOfMatrixColumn(Graphics g, Expression[] column, int fontSize) {

        setFont(g, fontSize);

        int result = 0;
        for (int i = 0; i < column.length; i++) {
            result = Math.max(result, getLengthOfExpression(g, column[i], fontSize));
        }
        return result;

    }

    public int getLengthOfMatrixExpression(Graphics g, MatrixExpression matExpr, int fontSize) {

        // Im Vorfeld prüfen, ob es sich um eine 1x1-Matrix handelt. Falls ja, dann wie eine Instanz von Expression behandeln.
        Object matExprConverted = matExpr.convertOneTimesOneMatrixToExpression();
        if (matExprConverted instanceof Expression) {
            Expression expr = (Expression) matExprConverted;
            if (expr.isSum() || expr.isDifference() || (expr instanceof Constant && expr.isNegative())) {
                return getLengthOfExpression(g, (Expression) matExprConverted, fontSize) + 2 * getWidthOfBracket(fontSize);
            }
            return getLengthOfExpression(g, (Expression) matExprConverted, fontSize);
        }

        setFont(g, fontSize);

        // Ab hier ist matExpr keine 1x1-Matrix mehr!
        if (matExpr instanceof Matrix) {

            Matrix matrix = (Matrix) matExpr;
            int lengthRow;
            int columnNumber = matrix.getColumnNumber();

            lengthRow = 2 * getWidthOfBracket(fontSize) + (columnNumber - 1) * getWidthOfMatrixSpace(fontSize);
            for (int i = 0; i < columnNumber; i++) {
                lengthRow = lengthRow + getLengthOfMatrixColumn(g, matrix.getColumn(i), fontSize);
            }

            return lengthRow;

        } else if (matExpr instanceof MatrixBinaryOperation) {

            if (matExpr.isSum()) {
                return getLengthOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getLeft(), fontSize)
                        + getWidthOfSignPlus(g, fontSize)
                        + getLengthOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getRight(), fontSize);
            } else if (matExpr.isDifference()) {
                if (((MatrixBinaryOperation) matExpr).getRight().isSum() || ((MatrixBinaryOperation) matExpr).getRight().isDifference()) {
                    // Hier noch Klammern um den Subtrahenden berücksichtigen.
                    return getLengthOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getLeft(), fontSize)
                            + getWidthOfSignMinus(g, fontSize)
                            + getLengthOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getRight(), fontSize)
                            + 2 * getWidthOfBracket(fontSize);
                } else {
                    return getLengthOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getLeft(), fontSize)
                            + getWidthOfSignMinus(g, fontSize)
                            + getLengthOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getRight(), fontSize);
                }
            } else {

                // Hier ist matExpr ein Matrizenprodukt.
                int resultLength = getLengthOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getLeft(), fontSize)
                        + getWidthOfSignMult(g, fontSize)
                        + getLengthOfMatrixExpression(g, ((MatrixBinaryOperation) matExpr).getRight(), fontSize);
                if (((MatrixBinaryOperation) matExpr).getLeft().isSum() || ((MatrixBinaryOperation) matExpr).getLeft().isDifference()) {
                    // Hier noch Klammern um den linken Faktor berücksichtigen.
                    resultLength = resultLength + 2 * getWidthOfBracket(fontSize);
                }
                if (((MatrixBinaryOperation) matExpr).getRight().isSum() || ((MatrixBinaryOperation) matExpr).getRight().isDifference()) {
                    // Hier noch Klammern um den rechten Faktor berücksichtigen.
                    resultLength = resultLength + 2 * getWidthOfBracket(fontSize);
                }
                return resultLength;

            }

        } else if (matExpr instanceof MatrixPower) {

            if (((MatrixPower) matExpr).getLeft() instanceof Matrix) {
                // Dann keine Klammern um die Basis herum berücksichtigen.
                return getLengthOfMatrixExpression(g, ((MatrixPower) matExpr).getLeft(), fontSize)
                        + getLengthOfExpression(g, ((MatrixPower) matExpr).getRight(), getSizeForSup(fontSize));
            } else {
                // Ansonsten Klammern um die Basis herum berücksichtigen.
                return getLengthOfMatrixExpression(g, ((MatrixPower) matExpr).getLeft(), fontSize)
                        + 2 * getWidthOfBracket(fontSize)
                        + getLengthOfExpression(g, ((MatrixPower) matExpr).getRight(), getSizeForSup(fontSize));
            }

        } else if (matExpr instanceof MatrixFunction) {

            setFont(g, fontSize);
            if (((MatrixFunction) matExpr).getLeft() instanceof Matrix
                    && !(((MatrixFunction) matExpr).getLeft().convertOneTimesOneMatrixToExpression() instanceof Expression)) {
                // In diesem Fall braucht man keine umschließende Klammern für matExpr.
                return g.getFontMetrics().stringWidth(((MatrixFunction) matExpr).getName())
                        + getLengthOfMatrixExpression(g, ((MatrixFunction) matExpr).getLeft(), fontSize);
            } else {
                return g.getFontMetrics().stringWidth(((MatrixFunction) matExpr).getName()) + 2 * getWidthOfBracket(fontSize)
                        + getLengthOfMatrixExpression(g, ((MatrixFunction) matExpr).getLeft(), fontSize);
            }

        } else {

            // Hier ist matExpr eine Instanz von MatrixOperator2.
            return getLengthOfMatrixOperator(g, (MatrixOperator) matExpr, fontSize);

        }

    }

    public int getLengthOfMatrixOperator(Graphics g, MatrixOperator matOperator, int fontSize) {

        setFont(g, fontSize);

        Object[] params = matOperator.getParams();

        if (matOperator.getType().equals(TypeMatrixOperator.diff)) {

            int result;

            if (params.length == 2) {

                result = getWidthOfSignPartial(g, fontSize) + g.getFontMetrics().stringWidth((String) params[1])
                        + 2 * getWidthOfBracket(fontSize) + getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);

            } else if (params.length == 3 && params[2] instanceof Integer) {

                int widthOfDifferentialOperator = getWidthOfSignPartial(g, fontSize) + g.getFontMetrics().stringWidth((String) params[1]);
                setFont(g, getSizeForSup(fontSize));
                widthOfDifferentialOperator = widthOfDifferentialOperator + g.getFontMetrics().stringWidth(String.valueOf((int) params[2]));
                result = widthOfDifferentialOperator + 2 * getWidthOfBracket(fontSize) + getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);

            } else {

                int widthOfDifferentialOperator = g.getFontMetrics().stringWidth((String) params[1]);
                for (int i = 2; i < params.length; i++) {
                    widthOfDifferentialOperator = widthOfDifferentialOperator + g.getFontMetrics().stringWidth((String) params[i]);
                }
                widthOfDifferentialOperator = widthOfDifferentialOperator + (params.length - 1) * getWidthOfSignPartial(g, fontSize);
                result = widthOfDifferentialOperator + 2 * getWidthOfBracket(fontSize) + getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);

            }

            /*
             Die Addition von fontSize / 2 ist dazu da, damit nach dem
             Differentialoperator etwas Abstand zum nächsten Zeichen ist
             (nämlich fontSize / 2 Pixel).
             */
            return result + fontSize / 2;

        } else if (matOperator.getType().equals(TypeMatrixOperator.integral)) {

            int heightOfIntegral = getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
            int result = getWidthOfSignIntegral(g, fontSize, heightOfIntegral);

            if (params.length == 4) {
                // Bestimmtes Integral.
                result = Math.max(result, getLengthOfExpression(g, (Expression) params[2], getSizeForSup(fontSize)));
                result = Math.max(result, getLengthOfExpression(g, (Expression) params[3], getSizeForSup(fontSize)));
            }

            result = result + getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
            setFont(g, fontSize);
            /*
             Die Addition von fontSize / 2 ist dazu da, damit nach dem dx (x =
             var) etwas Abstand zum nächsten Zeichen ist (nämlich fontSize / 2
             Pixel).
             */
            return result + g.getFontMetrics().stringWidth("d" + (String) params[1]) + fontSize / 2;

        } else if (matOperator.getType().equals(TypeMatrixOperator.laplace)) {

            return g.getFontMetrics().stringWidth("\u0394") + 2 * getWidthOfBracket(fontSize) + getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);

        } else if (matOperator.getType().equals(TypeMatrixOperator.prod)) {

            int heightFactor = getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
            int result = getWidthOfSignPi(g, fontSize, heightFactor);
            setFont(g, getSizeForSup(fontSize));
            int lengthIndexVar = g.getFontMetrics().stringWidth((String) params[1] + " = ");
            result = Math.max(result, lengthIndexVar + getLengthOfExpression(g, (Expression) params[2], getSizeForSup(fontSize)));
            result = Math.max(result, getLengthOfExpression(g, (Expression) params[3], getSizeForSup(fontSize)));
            if (((MatrixExpression) params[0]) instanceof MatrixBinaryOperation
                    && ((((MatrixBinaryOperation) params[0]).getType()).equals(TypeMatrixBinary.PLUS)
                    || (((MatrixBinaryOperation) params[0]).getType()).equals(TypeMatrixBinary.MINUS))) {
                return result + 2 * getWidthOfBracket(fontSize) + getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
            } else {
                return result + getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
            }

        } else if (matOperator.getType().equals(TypeMatrixOperator.sum)) {

            int heightSummand = getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
            int result = getWidthOfSignSigma(g, fontSize, heightSummand);
            setFont(g, getSizeForSup(fontSize));
            int lengthIndexVar = g.getFontMetrics().stringWidth((String) params[1] + " = ");
            result = Math.max(result, lengthIndexVar + getLengthOfExpression(g, (Expression) params[2], getSizeForSup(fontSize)));
            result = Math.max(result, getLengthOfExpression(g, (Expression) params[3], getSizeForSup(fontSize)));
            if (((MatrixExpression) params[0]) instanceof MatrixBinaryOperation
                    && ((((MatrixBinaryOperation) params[0]).getType()).equals(TypeMatrixBinary.PLUS)
                    || (((MatrixBinaryOperation) params[0]).getType()).equals(TypeMatrixBinary.MINUS))) {
                return result + 2 * getWidthOfBracket(fontSize) + getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
            } else {
                return result + getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
            }

        } else {

            // Sonstiger Standardfall
            String name = MatrixOperator.getNameFromType(matOperator.getType());
            int result = g.getFontMetrics().stringWidth(name) + 2 * getWidthOfBracket(fontSize);

            for (int i = 0; i < params.length; i++) {

                if (params[i] instanceof Expression) {
                    result = result + getLengthOfExpression(g, (Expression) matOperator.getParams()[i], fontSize);
                } else if (params[i] instanceof MatrixExpression) {
                    result = result + getLengthOfMatrixExpression(g, (MatrixExpression) matOperator.getParams()[i], fontSize);
                } else if (params[i] instanceof String) {
                    result = result + g.getFontMetrics().stringWidth((String) params[i]);
                } else if (params[i] instanceof Integer) {
                    result = result + g.getFontMetrics().stringWidth(String.valueOf((Integer) params[i]));
                }

                if (i < params.length - 1) {
                    result = result + g.getFontMetrics().stringWidth(", ");
                }

            }
            return result;

        }

    }

    public int getHeightOfExpression(Graphics g, Expression expr, int fontSize) {

        if (expr instanceof Constant) {
            return fontSize;
        } else if (expr instanceof Variable) {
            return fontSize;
        } else if (expr instanceof BinaryOperation) {

            if (((BinaryOperation) expr).getType().equals(TypeBinary.PLUS) || ((BinaryOperation) expr).getType().equals(TypeBinary.MINUS)
                    || ((BinaryOperation) expr).getType().equals(TypeBinary.TIMES)) {

                int h_center_l = getHeightOfCenterOfExpression(g, ((BinaryOperation) expr).getLeft(), fontSize);
                int h_center_r = getHeightOfCenterOfExpression(g, ((BinaryOperation) expr).getRight(), fontSize);
                return Math.max(h_center_l, h_center_r)
                        + Math.max(getHeightOfExpression(g, ((BinaryOperation) expr).getLeft(), fontSize) - h_center_l,
                                getHeightOfExpression(g, ((BinaryOperation) expr).getRight(), fontSize) - h_center_r);

            } else if (((BinaryOperation) expr).getType().equals(TypeBinary.DIV)) {

                /*
                 Der Anbstand zwischen Zähler und Nenner soll so groß sein,
                 wie die Schriftgröße in Pixel beträgt (also == fontSize).
                 */
                return getHeightOfExpression(g, ((BinaryOperation) expr).getLeft(), fontSize)
                        + getHeightOfExpression(g, ((BinaryOperation) expr).getRight(), fontSize) + fontSize;

            } else {

                // Der Exponent soll etwas höher stehen als die Basis.
                return getHeightOfExpression(g, ((BinaryOperation) expr).getLeft(), fontSize)
                        + getHeightOfExpression(g, ((BinaryOperation) expr).getRight(), getSizeForSup(fontSize));

            }

        } else if (expr instanceof Function) {

            return getHeightOfExpression(g, ((Function) expr).getLeft(), fontSize);

        } else if (expr instanceof Operator) {

            Object[] params = ((Operator) expr).getParams();

            if (((Operator) expr).getType().equals(TypeOperator.diff)) {

                int h = getHeightOfExpression(g, (Expression) params[0], fontSize);
                int h_center = getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize);

                if (params.length == 2) {

                    return Math.max((3 * fontSize) / 2, h_center) + Math.max((3 * fontSize) / 2, h - h_center);

                } else if (params.length == 3 && params[2] instanceof Integer) {

                    return Math.max((3 * fontSize) / 2 + getSizeForSup(fontSize), h_center) + Math.max((3 * fontSize) / 2 + getSizeForSup(fontSize), h - h_center);

                } else {

                    return Math.max((3 * fontSize) / 2, h_center) + Math.max((3 * fontSize) / 2 + getSizeForSup(fontSize), h - h_center);

                }

            } else if (((Operator) expr).getType().equals(TypeOperator.fac)) {

                return getHeightOfExpression(g, (Expression) params[0], fontSize);

            } else if (((Operator) expr).getType().equals(TypeOperator.integral)) {

                if (params.length == 2) {

                    // Unbestimmtes Integral.
                    return getHeightOfExpression(g, (Expression) params[0], fontSize);

                } else {

                    // Bestimmtes Integral.
                    return getHeightOfExpression(g, (Expression) params[0], fontSize)
                            + getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize))
                            + getHeightOfExpression(g, (Expression) params[3], getSizeForSup(fontSize));

                }

            } else if (((Operator) expr).getType().equals(TypeOperator.laplace)) {

                return getHeightOfExpression(g, (Expression) params[0], fontSize);

            } else if (((Operator) expr).getType().equals(TypeOperator.prod)
                    || ((Operator) expr).getType().equals(TypeOperator.sum)) {

                return getHeightOfExpression(g, (Expression) params[0], fontSize)
                        + getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize))
                        + getHeightOfExpression(g, (Expression) params[3], getSizeForSup(fontSize));

            } else {

                int heightBelowCommonCenter = (2 * fontSize) / 5;
                int heightBeyondCommonCenter = (3 * fontSize) / 5;
                Object[] left = ((Operator) expr).getParams();

                for (int i = 0; i < left.length; i++) {

                    if (left[i] instanceof Expression) {
                        heightBelowCommonCenter = Math.max(heightBelowCommonCenter, getHeightOfCenterOfExpression(g, (Expression) left[i], fontSize));
                        heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter,
                                getHeightOfExpression(g, (Expression) left[i], fontSize) - getHeightOfCenterOfExpression(g, (Expression) left[i], fontSize));
                    }
                    /*
                     Ansonsten ist left[i] eine Instanz von String oder von
                     Integer. In beiden Fällen wird das gleiche berechnet (so,
                     als wäre es eine Variable; die Höhe des Zentrums beträgt
                     40% der Gesamthöhe des Ausdrucks). Dann ändern sich aber
                     heightBelowCommonCenter und heightBeyondCommonCenter
                     NICHT.
                     */

                }

                return heightBelowCommonCenter + heightBeyondCommonCenter;

            }

        } else {

            // Hier ist expr eine Instanz von SelfDefinedFunction.
            SelfDefinedFunction expr_as_selfdefinedfunction = (SelfDefinedFunction) expr;
            Expression[] left = expr_as_selfdefinedfunction.getLeft();

            int heightBelowCommonCenter = getHeightOfCenterOfExpression(g, left[0], fontSize);
            int heightBeyondCommonCenter = getHeightOfExpression(g, left[0], fontSize) - getHeightOfCenterOfExpression(g, left[0], fontSize);
            for (int i = 1; i < left.length; i++) {
                heightBelowCommonCenter = Math.max(heightBelowCommonCenter, getHeightOfCenterOfExpression(g, left[i], fontSize));
                heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter,
                        getHeightOfExpression(g, left[i], fontSize) - getHeightOfCenterOfExpression(g, left[i], fontSize));
            }
            return heightBelowCommonCenter + heightBeyondCommonCenter;

        }

    }

    public int getHeightOfCenterOfExpression(Graphics g, Expression expr, int fontSize) {

        setFont(g, fontSize);

        if (expr instanceof Constant) {
            return (2 * fontSize) / 5;
        } else if (expr instanceof Variable) {
            return (2 * fontSize) / 5;
        } else if (expr instanceof BinaryOperation) {

            if (((BinaryOperation) expr).getType().equals(TypeBinary.PLUS) || ((BinaryOperation) expr).getType().equals(TypeBinary.MINUS)
                    || ((BinaryOperation) expr).getType().equals(TypeBinary.TIMES)) {

                return Math.max(getHeightOfCenterOfExpression(g, ((BinaryOperation) expr).getLeft(), fontSize),
                        getHeightOfCenterOfExpression(g, ((BinaryOperation) expr).getRight(), fontSize));

            } else if (((BinaryOperation) expr).getType().equals(TypeBinary.DIV)) {

                /*
                 Der Anbstand zwischen Zähler und Nenner soll so groß sein,
                 wie die Schriftgröße in Pixel beträgt (also == fontSize).
                 */
                return getHeightOfExpression(g, ((BinaryOperation) expr).getRight(), fontSize)
                        + fontSize / 2;

            } else {

                // Das Zentrum ist auf derselben Höhe wie das Zentrum von der Basis.
                return getHeightOfCenterOfExpression(g, ((BinaryOperation) expr).getLeft(), fontSize);

            }

        } else if (expr instanceof Function) {

            return getHeightOfCenterOfExpression(g, ((Function) expr).getLeft(), fontSize);

        } else if (expr instanceof Operator) {

            Object[] params = ((Operator) expr).getParams();

            if (((Operator) expr).getType().equals(TypeOperator.diff)) {

                if (params.length == 2) {
                    return Math.max((3 * fontSize) / 2, getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize));
                } else if (params.length == 3 && params[2] instanceof Integer) {
                    return Math.max((3 * fontSize) / 2 + getSizeForSup(fontSize), getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize));
                } else {
                    return Math.max((3 * fontSize) / 2, getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize));
                }

            } else if (((Operator) expr).getType().equals(TypeOperator.fac)) {

                return getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize);

            } else if (((Operator) expr).getType().equals(TypeOperator.integral)) {

                if (params.length == 2) {
                    // Unbestimmtes Integral.
                    return getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize);
                } else {
                    // Bestimmtes Integral.
                    return getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize)
                            + getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
                }

            } else if (((Operator) expr).getType().equals(TypeOperator.laplace)) {
                return getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize);
            } else if (((Operator) expr).getType().equals(TypeOperator.prod)
                    || ((Operator) expr).getType().equals(TypeOperator.sum)) {
                return getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize)
                        + getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
            } else {

                int result = 2 * fontSize / 5;
                for (Object param : ((Operator) expr).getParams()) {
                    /*
                     Von allen möglichen Parametern in Operatoren können nur
                     die Parameter, welche Instanzen von Expression sind, zur
                     Höhe des Zentrums beitragen. Alle anderen haben die
                     Zentrumhöhe 2 * fontsize / 5.
                     */
                    if (param instanceof Expression) {
                        result = Math.max(result, getHeightOfCenterOfExpression(g, (Expression) param, fontSize));
                    }
                }
                return result;

            }

        } else {

            /*
             In diesem Fall ist es eine vom Benutzer definierte Funktion. Die
             Höhe des Gesamtterms ist dann das Maximum aller Termhöhen in den
             einzelnen Parametern.
             */
            Expression[] left = ((SelfDefinedFunction) expr).getLeft();

            int heightCenter = getHeightOfCenterOfExpression(g, left[0], fontSize);
            for (int i = 1; i < left.length; i++) {
                heightCenter = Math.max(heightCenter, getHeightOfCenterOfExpression(g, left[i], fontSize));
            }
            return heightCenter;

        }

    }

    public int getLengthOfExpression(Graphics g, Expression expr, int fontSize) {

        setFont(g, fontSize);

        if (expr instanceof Constant) {
            return getLengthOfConstant(g, (Constant) expr, fontSize);
        } else if (expr instanceof Variable) {
            return getLengthOfVariable(g, (Variable) expr, fontSize);
        } else if (expr instanceof BinaryOperation) {
            return getLengthOfBinaryOperation(g, (BinaryOperation) expr, fontSize);
        } else if (expr instanceof Function) {
            return getLengthOfFunction(g, (Function) expr, fontSize);
        } else if (expr instanceof Operator) {
            return getLengthOfOperator(g, (Operator) expr, fontSize);
        } else {
            // Dann ist es eine vom Benutzer definierte Funktion
            return getLengthOfSelfDefinedFunction(g, (SelfDefinedFunction) expr, fontSize);
        }

    }

    private int getLengthOfConstant(Graphics g, Constant expr, int fontSize) {
        setFont(g, fontSize);
        String s = expr.writeExpression();
        if (expr.getValue().compareTo(BigDecimal.ZERO) < 0) {
            return getWidthOfSignMinus(g, fontSize) + g.getFontMetrics().stringWidth(s.substring(1, s.length()));
        }
        return g.getFontMetrics().stringWidth(s);
    }

    private int getLengthOfVariable(Graphics g, Variable expr, int fontSize) {
        setFont(g, fontSize);
        if (!expr.getName().contains("_")) {
            String s = expr.writeExpression();
            return g.getFontMetrics().stringWidth(s);
        }

        // Die Variable ist von der Form x_index mit eventuellen Apostrophs.
        int i = 2;
        while (i < expr.getName().length() && (int) expr.getName().charAt(i) >= 48 && (int) expr.getName().charAt(i) <= 57) {
            i++;
        }
        // Ersten Buchstaben auslesen.
        String varName = expr.getName().substring(0, 1);
        // Index auslesen.
        String index = expr.getName().substring(2, i);
        // Apostrophs auslesen.
        String apostrophs = expr.getName().substring(i, expr.getName().length());

        int lengthVar = g.getFontMetrics().stringWidth(varName);
        int lengthApostrophs = g.getFontMetrics().stringWidth(apostrophs);
        setFont(g, getSizeForSub(fontSize));
        int lengthIndex = g.getFontMetrics().stringWidth(index);

        // Es wird zwischen der Variablen und dem Index getSizeForSub(fontSize) / 3 Pixel Platz gelassen.
        return lengthVar + Math.max(getSizeForSub(fontSize) / 3 + lengthIndex, lengthApostrophs);
    }

    /**
     * Gibt die Länge von expr zurück. VORAUSSETZUNG: expr ist eine Instanz von
     * BinaryOperation.
     */
    private int getLengthOfBinaryOperation(Graphics g, BinaryOperation expr, int fontSize) {

        setFont(g, fontSize);

        ExpressionCollection factorsOfRight = SimplifyUtilities.getFactors(expr.getRight());

        // Im Folgenden bezeichnet l immer die Pixellänge der Formel.
        if (expr.isSum()) {

            if (!factorsOfRight.get(0).hasPositiveSign()) {
                // In diesem Falls soll die Formel folgendermaßen ausgeschrieben werden: a+(-b) statt a+(-1)*b.
                return getLengthOfExpression(g, expr.getLeft(), fontSize)
                        + getWidthOfSignPlus(g, fontSize) + 2 * getWidthOfBracket(fontSize)
                        + getLengthOfExpression(g, expr.getRight(), fontSize);
            } else {
                return getLengthOfExpression(g, expr.getLeft(), fontSize)
                        + getWidthOfSignPlus(g, fontSize)
                        + getLengthOfExpression(g, expr.getRight(), fontSize);
            }

        } else if (expr.isDifference()) {

            int lengthLeft = getLengthOfExpression(g, expr.getLeft(), fontSize);
            if (expr.getLeft().equals(Expression.ZERO)) {
                lengthLeft = 0;
            }

            if (expr.getRight().isSum() || expr.getRight().isDifference() || !factorsOfRight.get(0).hasPositiveSign()) {
                // l(left) + l(-) + l("(") + l(right) + l(")").
                return lengthLeft + getWidthOfSignMinus(g, fontSize) + 2 * getWidthOfBracket(fontSize)
                        + getLengthOfExpression(g, expr.getRight(), fontSize);
            } else {
                // l(left) + l(-) + l(right).
                return lengthLeft + getWidthOfSignMinus(g, fontSize) + getLengthOfExpression(g, expr.getRight(), fontSize);
            }

        } else if (expr.isProduct()) {

            int lengthLeft, lengthRight;

            if (expr.getLeft().isSum() || expr.getLeft().isDifference()) {
                // l_left = l("(") + l(left) + l(")").
                lengthLeft = getLengthOfExpression(g, expr.getLeft(), fontSize)
                        + 2 * getWidthOfBracket(fontSize);
            } else {
                /*
                 l_left = l(left). Falls left == -1 ist, so soll nur die Länge
                 des Minuszeichens berücksichtigt werden (da nicht -1, sondern
                 - ausgeschrieben wird).
                 */
                lengthLeft = getLengthOfExpression(g, expr.getLeft(), fontSize);
                if (expr.getLeft() instanceof Constant
                        && ((Constant) expr.getLeft()).getValue().compareTo(BigDecimal.valueOf(-1)) == 0) {
                    lengthLeft = getWidthOfSignMinus(g, fontSize);
                }
            }

            if (expr.getRight().doesExpressionStartWithAMinusSign()
                    || expr.getRight().isSum() || expr.getRight().isDifference()) {
                // l_right = l("(") + l(right) + l(")").
                lengthRight = getLengthOfExpression(g, expr.getRight(), fontSize)
                        + fontSize;
            } else {
                // l_right = l(right).
                lengthRight = getLengthOfExpression(g, expr.getRight(), fontSize);
            }

            if (expr.getLeft().equals(Expression.MINUS_ONE)) {
                /*
                 Falls left == -1. Dann soll die Länge des
                 Multiplikationszeichens NICHT berücksichtigt werden.
                 */
                return lengthLeft + lengthRight;
            } else {
                return lengthLeft + getWidthOfSignMult(g, fontSize) + lengthRight;
            }

        } else if (expr.isQuotient()) {

            if (expr.doesExpressionStartWithAMinusSign()) {
                /* 
                 Fall: im Zähler beginnt der Ausdruck mit einer negativen Konstante.
                 Ignoriert wird dagegen der Fall, dass der Nenner mit einer negativen Konstante
                 beginnt. Das Minuszeichen wird in diesem Fall im Nenner gelassen und wird
                 nicht herausgezogen.
                 */
                return getWidthOfSignMinus(g, fontSize) + Math.max(getLengthOfExpression(g, expr.getLeft().negate(), fontSize),
                        getLengthOfExpression(g, expr.getRight(), fontSize));

            }

            return Math.max(getLengthOfExpression(g, expr.getLeft(), fontSize),
                    getLengthOfExpression(g, expr.getRight(), fontSize));

        } else {

            int lengthLeft;

            if (expr.getLeft() instanceof BinaryOperation
                    || (expr.getLeft() instanceof Constant && expr.getLeft().isNegative())
                    || expr.getLeft().isOperator(TypeOperator.diff) || expr.getLeft().isOperator(TypeOperator.fac)
                    || expr.getLeft().isOperator(TypeOperator.integral) || expr.getLeft().isOperator(TypeOperator.laplace)
                    || expr.getLeft().isOperator(TypeOperator.prod) || expr.getLeft().isOperator(TypeOperator.sum)) {

                lengthLeft = getLengthOfExpression(g, expr.getLeft(), fontSize) + 2 * getWidthOfBracket(fontSize);

            } else {

                lengthLeft = getLengthOfExpression(g, expr.getLeft(), fontSize);

            }

            /*
             Die Schriftgröße der Potenz soll kleiner sein als die
             Schriftgröße der Basis. Wie genau die Skalierung ist, regelt
             getSizeForSupAndSub().
             */
            return lengthLeft + getLengthOfExpression(g, expr.getRight(), getSizeForSup(fontSize));

        }

    }

    private int getLengthOfFunction(Graphics g, Function expr, int fontSize) {

        setFont(g, fontSize);

        /*
         Der letzte Summand (2 * getWidthOfBracket(fontSize)) ist reserviert für die 
         beiden Klammerbreiten. Diese betragen jeweils fontsize.
         */
        if (!expr.getType().equals(TypeFunction.abs)) {
            return g.getFontMetrics().stringWidth(expr.getName())
                    + getLengthOfExpression(g, expr.getLeft(), fontSize)
                    + 2 * getWidthOfBracket(fontSize);
        } else {
            return getLengthOfExpression(g, expr.getLeft(), fontSize)
                    + 2 * getWidthOfAbsBracket(fontSize);
        }

    }

    private int getLengthOfOperator(Graphics g, Operator expr, int fontSize) {

        setFont(g, fontSize);

        Object[] params = expr.getParams();

        if (expr.getType().equals(TypeOperator.diff)) {

            int result;

            if (params.length == 2) {

                result = getWidthOfSignPartial(g, fontSize) + g.getFontMetrics().stringWidth((String) params[1])
                        + 2 * getWidthOfBracket(fontSize) + getLengthOfExpression(g, (Expression) params[0], fontSize);

            } else if (params.length == 3 && params[2] instanceof Integer) {

                int widthOfDifferentialOperator = getWidthOfSignPartial(g, fontSize) + g.getFontMetrics().stringWidth((String) params[1]);
                setFont(g, getSizeForSup(fontSize));
                widthOfDifferentialOperator = widthOfDifferentialOperator + g.getFontMetrics().stringWidth(String.valueOf((int) params[2]));
                result = widthOfDifferentialOperator + 2 * getWidthOfBracket(fontSize) + getLengthOfExpression(g, (Expression) params[0], fontSize);

            } else {

                int widthOfDifferentialOperator = g.getFontMetrics().stringWidth((String) params[1]);
                for (int i = 2; i < params.length; i++) {
                    widthOfDifferentialOperator = widthOfDifferentialOperator + g.getFontMetrics().stringWidth((String) params[i]);
                }
                widthOfDifferentialOperator = widthOfDifferentialOperator + (params.length - 1) * getWidthOfSignPartial(g, fontSize);
                result = widthOfDifferentialOperator + 2 * getWidthOfBracket(fontSize) + getLengthOfExpression(g, (Expression) params[0], fontSize);

            }

            /*
             Die Addition von fontSize / 2 ist dazu da, damit nach dem
             Differentialoperator etwas Abstand zum nächsten Zeichen ist
             (nämlich fontSize / 2 Pixel).
             */
            return result + fontSize / 2;

        } else if (expr.getType().equals(TypeOperator.fac)) {

            Expression argument = (Expression) params[0];

            if (argument instanceof BinaryOperation || (argument instanceof Constant
                    && ((Constant) argument).getValue().compareTo(BigDecimal.ZERO) < 0)) {

                // l = l("(") + l(interior) + l(")") + l("!")
                int lengthArgument = getLengthOfExpression(g, argument, fontSize);
                setFont(g, fontSize);
                return lengthArgument + 2 * getWidthOfBracket(fontSize) + getWidthOfSignFac(g, fontSize);

            } else {

                // l = l(Argument) + l("!")
                int lengthArgument = getLengthOfExpression(g, argument, fontSize);
                setFont(g, fontSize);
                return lengthArgument + getWidthOfSignFac(g, fontSize);

            }

        } else if (expr.getType().equals(TypeOperator.integral)) {

            int heightOfIntegral = getHeightOfExpression(g, (Expression) params[0], fontSize);
            int result = getWidthOfSignIntegral(g, fontSize, heightOfIntegral);

            if (params.length == 4) {
                // Bestimmtes Integral.
                result = Math.max(result, getLengthOfExpression(g, (Expression) params[2], getSizeForSup(fontSize)));
                result = Math.max(result, getLengthOfExpression(g, (Expression) params[3], getSizeForSup(fontSize)));
            }

            result = result + getLengthOfExpression(g, (Expression) params[0], fontSize);
            setFont(g, fontSize);
            /*
             Die Addition von fontSize / 2 ist dazu da, damit nach dem dx (x =
             var) etwas Abstand zum nächsten Zeichen ist (nämlich fontSize / 2
             Pixel).
             */
            return result + g.getFontMetrics().stringWidth("d" + (String) params[1]) + fontSize / 2;

        } else if (expr.getType().equals(TypeOperator.laplace)) {

            return g.getFontMetrics().stringWidth("\u0394") + 2 * getWidthOfBracket(fontSize) + getLengthOfExpression(g, (Expression) params[0], fontSize);

        } else if (expr.getType().equals(TypeOperator.prod)) {

            int heightFactor = getHeightOfExpression(g, (Expression) params[0], fontSize);
            int result = getWidthOfSignPi(g, fontSize, heightFactor);
            setFont(g, getSizeForSup(fontSize));
            int lengthIndexVar = g.getFontMetrics().stringWidth((String) params[1] + " = ");
            result = Math.max(result, lengthIndexVar + getLengthOfExpression(g, (Expression) params[2], getSizeForSup(fontSize)));
            result = Math.max(result, getLengthOfExpression(g, (Expression) params[3], getSizeForSup(fontSize)));
            if (((Expression) params[0]) instanceof BinaryOperation
                    && ((((BinaryOperation) params[0]).getType()).equals(TypeBinary.PLUS)
                    || (((BinaryOperation) params[0]).getType()).equals(TypeBinary.MINUS))) {
                return result + 2 * getWidthOfBracket(fontSize) + getLengthOfExpression(g, (Expression) params[0], fontSize);
            } else {
                return result + getLengthOfExpression(g, (Expression) params[0], fontSize);
            }

        } else if (expr.getType().equals(TypeOperator.sum)) {

            int heightSummand = getHeightOfExpression(g, (Expression) params[0], fontSize);
            int result = getWidthOfSignSigma(g, fontSize, heightSummand);
            setFont(g, getSizeForSup(fontSize));
            int lengthIndexVar = g.getFontMetrics().stringWidth((String) params[1] + " = ");
            result = Math.max(result, lengthIndexVar + getLengthOfExpression(g, (Expression) params[2], getSizeForSup(fontSize)));
            result = Math.max(result, getLengthOfExpression(g, (Expression) params[3], getSizeForSup(fontSize)));
            if (((Expression) params[0]) instanceof BinaryOperation
                    && ((((BinaryOperation) params[0]).getType()).equals(TypeBinary.PLUS)
                    || (((BinaryOperation) params[0]).getType()).equals(TypeBinary.MINUS))) {
                return result + 2 * getWidthOfBracket(fontSize) + getLengthOfExpression(g, (Expression) params[0], fontSize);
            } else {
                return result + getLengthOfExpression(g, (Expression) params[0], fontSize);
            }

        } else {

            String name = Operator.getNameFromType(expr.getType());
            if (expr.getType().equals(TypeOperator.taylor)) {
                /**
                 * Ausnahme: bei der graphischen Darstellung soll der
                 * Taylor-Operator nur als "T" ausgeschrieben werden (und in
                 * Klammern dahinter alle notwendigen Parameter).
                 */
                name = "T";
            }

            int result = g.getFontMetrics().stringWidth(name) + 2 * getWidthOfBracket(fontSize);

            for (int i = 0; i < params.length; i++) {

                if (params[i] instanceof Expression) {
                    result = result + getLengthOfExpression(g, (Expression) expr.getParams()[i], fontSize);
                } else if (params[i] instanceof String) {
                    result = result + g.getFontMetrics().stringWidth((String) params[i]);
                } else if (params[i] instanceof Integer) {
                    result = result + g.getFontMetrics().stringWidth(String.valueOf((Integer) params[i]));
                }

                if (i < params.length - 1) {
                    result = result + g.getFontMetrics().stringWidth(", ");
                }

            }
            return result;

        }

    }

    private int getLengthOfSelfDefinedFunction(Graphics g, SelfDefinedFunction expr, int fontSize) {

        setFont(g, fontSize);

        int result = g.getFontMetrics().stringWidth(expr.getName());
        result = result + 2 * getWidthOfBracket(fontSize)
                + (expr.getArguments().length - 1) * g.getFontMetrics().stringWidth(", ");

        for (Expression left : expr.getLeft()) {
            result = result + getLengthOfExpression(g, left, fontSize);
        }

        return result;

    }

    private int getHeightOfOutput(Graphics g, int fontSize, Object... output) {

        int heightBelowCommonCenter = (2 * fontSize) / 5;
        int heightBeyondCommonCenter = (3 * fontSize) / 5;
        int heightCenter;

        for (Object out : output) {

            /*
             Hier spielt es keine Rolle, ob out eine Instanz von TypeBracket
             sein kann. Dann hat man nur zusätzliche Klammern, welche einen
             Ausdruck umschließen, und diese tragen zur Höhe des Zentrums
             nicht bei.
             */
            if (out instanceof Expression) {
                heightCenter = getHeightOfCenterOfExpression(g, (Expression) out, fontSize);
                heightBelowCommonCenter = Math.max(heightBelowCommonCenter, heightCenter);
                heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter, getHeightOfExpression(g, (Expression) out, fontSize) - heightCenter);
            } else if (out instanceof LogicalExpression) {
                heightCenter = getHeightOfCenterOfLogicalExpression(g, (LogicalExpression) out, fontSize);
                heightBelowCommonCenter = Math.max(heightBelowCommonCenter, heightCenter);
                heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter, getHeightOfLogicalExpression(g, (LogicalExpression) out, fontSize) - heightCenter);
            } else if (out instanceof MatrixExpression) {
                heightCenter = getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) out, fontSize);
                heightBelowCommonCenter = Math.max(heightBelowCommonCenter, heightCenter);
                heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter, getHeightOfMatrixExpression(g, (MatrixExpression) out, fontSize) - heightCenter);
            } else if (out instanceof Command) {
                heightCenter = getHeightOfCenterOfCommand(g, (Command) out, fontSize);
                heightBelowCommonCenter = Math.max(heightBelowCommonCenter, heightCenter);
                heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter, getHeightOfCommand(g, (Command) out, fontSize) - heightCenter);
            } else if (out instanceof String) {
                heightBelowCommonCenter = Math.max(heightBelowCommonCenter, (2 * fontSize) / 5);
                heightBeyondCommonCenter = Math.max(heightBeyondCommonCenter, (3 * fontSize) / 5);
            }

        }

        return heightBelowCommonCenter + heightBeyondCommonCenter;

    }

    private int getHeightOfCenterOfOutput(Graphics g, int fontSize, Object... output) {

        int heightCenter = 2 * fontSize / 5;

        for (Object out : output) {

            /*
             Hier spielt es keine Rolle, ob out eine Instanz von TypeBracket
             sein kann. Dann hat man nur zusätzliche Klammern, welche einen
             Ausdruck umschließen, und diese tragen zur Höhe des Zentrums
             nicht bei.
             */
            if (out instanceof Expression) {
                heightCenter = Math.max(heightCenter, getHeightOfCenterOfExpression(g, (Expression) out, fontSize));
            } else if (out instanceof LogicalExpression) {
                heightCenter = Math.max(heightCenter, getHeightOfCenterOfLogicalExpression(g, (LogicalExpression) out, fontSize));
            } else if (out instanceof MatrixExpression) {
                heightCenter = Math.max(heightCenter, getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) out, fontSize));
            } else if (out instanceof Command) {
                heightCenter = Math.max(heightCenter, getHeightOfCenterOfCommand(g, (Command) out, fontSize));
            } else if (out instanceof String) {
                heightCenter = Math.max(heightCenter, (2 * fontSize) / 5);
            }

        }

        return heightCenter;

    }

    private int getLengthOfOutput(Graphics g, int fontSize, Object... output) {

        int length = 0;

        for (Object out : output) {

            if (out instanceof Expression) {
                length = length + getLengthOfExpression(g, (Expression) out, fontSize);
            } else if (out instanceof LogicalExpression) {
                length = length + getLengthOfLogicalExpression(g, (LogicalExpression) out, fontSize);
            } else if (out instanceof MatrixExpression) {
                length = length + getLengthOfMatrixExpression(g, (MatrixExpression) out, fontSize);
            } else if (out instanceof Command) {
                length = length + getLengthOfCommand(g, (Command) out, fontSize);
            } else if (out instanceof String) {
                setFont(g, fontSize);
                length = length + g.getFontMetrics().stringWidth((String) out);
            } else if (out instanceof TypeBracket) {
                length = length + 2 * getWidthOfBracket(fontSize);
            }

        }

        return length;

    }

    private int getWidthOfBracket(int fontSize) {
        return fontSize / 2;
    }

    private int getWidthOfAbsBracket(int fontSize) {
        return fontSize / 2;
    }

    private int getWidthOfMatrixSpace(int fontSize) {
        return fontSize;
    }

    private int getWidthOfSignPlus(Graphics g, int fontSize) {
        return fontSize;
    }

    private int getWidthOfSignMinus(Graphics g, int fontSize) {
        return fontSize;
    }

    private int getWidthOfSignMult(Graphics g, int fontSize) {
        return fontSize;
    }

    private int getWidthOfSignEquals(Graphics g, int fontSize) {
        return fontSize;
    }

    private int getWidthOfSignPartial(Graphics g, int fontSize) {
        setFont(g, fontSize);
        int result = g.getFontMetrics().stringWidth("\u2202");
        return result;
    }

    private int getWidthOfSignFac(Graphics g, int fontSize) {
        int old_fontSize = g.getFont().getSize();
        setFont(g, fontSize);
        int result = g.getFontMetrics().stringWidth("!");
        setFont(g, old_fontSize);
        return result;
    }

    private int getWidthOfSignIntegral(Graphics g, int fontSize, int height) {
        setFont(g, height);
        int result = g.getFontMetrics().stringWidth("\u222B");
        setFont(g, fontSize);
        return result;
    }

    private int getWidthOfSignDelta(Graphics g, int fontSize) {
        setFont(g, fontSize);
        return g.getFontMetrics().stringWidth("\u0394");
    }

    private int getWidthOfSignPi(Graphics g, int fontSize, int height) {
        setFont(g, height);
        int result = g.getFontMetrics().stringWidth("\u03A0");
        setFont(g, fontSize);
        return result;
    }

    private int getWidthOfSignSigma(Graphics g, int fontSize, int height) {
        setFont(g, height);
        int result = g.getFontMetrics().stringWidth("\u03A3");
        setFont(g, fontSize);
        return result;
    }

    /**
     * Gibt die Länge des UND-Symbols zurück.
     */
    private int getWidthOfSignAnd(int fontSize) {
        return fontSize;
    }

    /**
     * Gibt die Länge des des ODER-Symbols zurück.
     */
    private int getWidthOfSignOr(int fontSize) {
        return fontSize;
    }

    /**
     * Gibt die Länge des des NOT-Symbols zurück.
     */
    private int getWidthOfSignNegation(Graphics g, int fontSize) {
        setFont(g, fontSize);
        return g.getFontMetrics().stringWidth("\u00AC");
    }

    /**
     * Gibt die Länge des des Implikationssymbols zurück.
     */
    private int getWidthOfSignImplication(int fontSize) {
        return fontSize;
    }

    /**
     * Gibt die Länge des des Äquivalenzsymbols zurück.
     */
    private int getWidthOfSignEquivalence(int fontSize) {
        return fontSize;
    }

    private void drawOpeningBracket(Graphics g, int x_0, int y_0, int fontSize, int height) {

        Graphics2D g2 = (Graphics2D) g;
        /**
         * Strichdicke abhängig von fontSize berechnen.
         */
        int thick = Math.max((int) Math.round(((double) this.fontSize) / 10), 1);
        g2.setStroke(new BasicStroke(thick));

        g2.drawArc(x_0 + getWidthOfBracket(fontSize) / 4, y_0 - height + (3 * getWidthOfBracket(fontSize)) / 4,
                getWidthOfBracket(fontSize) / 2, getWidthOfBracket(fontSize) / 2,
                90, 90);
        g2.drawLine(x_0 + getWidthOfBracket(fontSize) / 4, y_0 - height + getWidthOfBracket(fontSize),
                x_0 + getWidthOfBracket(fontSize) / 4, y_0);
        g2.drawArc(x_0 + getWidthOfBracket(fontSize) / 4, y_0 - getWidthOfBracket(fontSize) / 4,
                getWidthOfBracket(fontSize) / 2, getWidthOfBracket(fontSize) / 2,
                180, 90);

    }

    private void drawClosingBracket(Graphics g, int x_0, int y_0, int fontSize, int height) {

        Graphics2D g2 = (Graphics2D) g;
        // Strichdicke abhängig von fontSize berechnen.
        int thick = Math.max((int) Math.round(((double) this.fontSize) / 10), 1);
        g2.setStroke(new BasicStroke(thick));

        g2.drawArc(x_0 + getWidthOfBracket(fontSize) / 4, y_0 - height + (3 * getWidthOfBracket(fontSize)) / 4,
                getWidthOfBracket(fontSize) / 2, getWidthOfBracket(fontSize) / 2,
                0, 90);
        g2.drawLine(x_0 + (3 * getWidthOfBracket(fontSize)) / 4, y_0 - height + getWidthOfBracket(fontSize),
                x_0 + (3 * getWidthOfBracket(fontSize)) / 4, y_0);
        g2.drawArc(x_0 + getWidthOfBracket(fontSize) / 4, y_0 - getWidthOfBracket(fontSize) / 4,
                getWidthOfBracket(fontSize) / 2, getWidthOfBracket(fontSize) / 2,
                270, 90);

    }

    private void drawAbsLine(Graphics g, int x_0, int y_0, int fontSize, int height) {
        Graphics2D g2 = (Graphics2D) g;
        // Strichdicke abhängig von fontSize berechnen.
        int thick = Math.max((int) Math.round(((double) this.fontSize) / 10), 1);
        g2.setStroke(new BasicStroke(thick));
        g2.drawLine(x_0 + getWidthOfAbsBracket(fontSize) / 2, y_0 - height, x_0 + getWidthOfAbsBracket(fontSize) / 2, y_0 + getWidthOfAbsBracket(fontSize) / 4);
    }

    private void drawSignPlus(Graphics g, int x_0, int y_0, int fontSize) {
        Graphics2D g2 = (Graphics2D) g;
        // Strichdicke abhängig von fontSize berechnen.
        int thick = Math.max((int) Math.round(((double) this.fontSize) / 10), 1);
        g2.setStroke(new BasicStroke(thick));
        //Waagerechter Strich
        g2.drawLine(x_0 + fontSize / 3, y_0 - fontSize / 2, x_0 + (2 * fontSize) / 3, y_0 - fontSize / 2);
        //Senkrechter Strich
        g2.drawLine(x_0 + fontSize / 2, y_0 - (2 * fontSize) / 3, x_0 + fontSize / 2, y_0 - fontSize / 3);
    }

    private void drawSignMinus(Graphics g, int x_0, int y_0, int fontSize) {
        Graphics2D g2 = (Graphics2D) g;
        // Strichdicke abhängig von fontSize berechnen.
        int thick = Math.max((int) Math.round(((double) this.fontSize) / 10), 1);
        g2.setStroke(new BasicStroke(thick));
        //Waagerechter Strich
        g2.drawLine(x_0 + fontSize / 3, y_0 - fontSize / 2, x_0 + (2 * fontSize) / 3, y_0 - fontSize / 2);
    }

    private void drawSignMult(Graphics g, int x_0, int y_0, int fontSize) {
        //Ausgefüllter Kreis
        g.fillOval(x_0 + (2 * fontSize) / 5, y_0 - (3 * fontSize) / 5, fontSize / 5, fontSize / 5);
    }

    private void drawSignEquals(Graphics g, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        g.drawString("=", x_0 + (getWidthOfSignEquals(g, fontSize) - g.getFontMetrics().stringWidth("=")) / 2, y_0);
    }

    private void drawFractionLine(Graphics g, int x_0, int y_0, int fontSize, int l) {
        Graphics2D g2 = (Graphics2D) g;
        // Strichdicke abhängig von fontSize berechnen.
        int thick = Math.max((int) Math.round(((double) fontSize) / 10), 1);
        g2.setStroke(new BasicStroke(thick));
        g2.drawLine(x_0, y_0, x_0 + l, y_0);
    }

    private void drawSignPartial(Graphics g, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        g.drawString("\u2202", x_0, y_0);
    }

    private void drawSignFac(Graphics g, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        g.drawString("!", x_0, y_0);
    }

    private void drawSignIntegral(Graphics g, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        /*
         Die Verschiebung um forntsize / 15 ist deshalb da, weil das
         Integralzeichen etwas nach unten aus seiner "Box" herausragt. Dieses
         Maß wurde durch PROBIEREN herausgefunden / optimiert.
         */
        g.drawString("\u222B", x_0, y_0 - fontSize / 15);
    }

    private void drawSignDelta(Graphics g, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        g.drawString("\u2206", x_0, y_0);
    }

    private void drawSignPi(Graphics g, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        g.drawString("\u03A0", x_0, y_0 - fontSize / 15);
    }

    private void drawSignSigma(Graphics g, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        /*
         Die Verschiebung um forntsize / 15 ist deshalb da, weil das
         Summenzeichen etwas nach unten aus seiner "Box" herausragt. Dieses
         Maß wurde durch PROBIEREN herausgefunden / optimiert.
         */
        g.drawString("\u03A3", x_0, y_0 - fontSize / 15);
    }

    /**
     * Zeichnen des UND-Symbols.
     */
    private void drawSignAnd(Graphics g, int x_0, int y_0, int fontSize) {

        Graphics2D g2 = (Graphics2D) g;
        // Strichdicke abhängig von fontSize berechnen.
        int thick = Math.max((int) Math.round(((double) this.fontSize) / 10), 1);
        g2.setStroke(new BasicStroke(thick));
        //Schräger Strich: Links unten nach rechts oben.
        g2.drawLine(x_0 + fontSize / 4, y_0 - fontSize / 10, x_0 + fontSize / 2, y_0 - fontSize / 2);
        //Schräger Strich: Links oben nach rechts unten.
        g2.drawLine(x_0 + fontSize / 2, y_0 - fontSize / 2, x_0 + (3 * fontSize) / 4, y_0 - fontSize / 10);

    }

    /**
     * Zeichnen des ODER-Symbols.
     */
    private void drawSignOr(Graphics g, int x_0, int y_0, int fontSize) {

        Graphics2D g2 = (Graphics2D) g;
        // Strichdicke abhängig von fontSize berechnen.
        int thick = Math.max((int) Math.round(((double) this.fontSize) / 10), 1);
        g2.setStroke(new BasicStroke(thick));
        //Schräger Strich: Links oben nach rechts unten.
        g2.drawLine(x_0 + fontSize / 4, y_0 - fontSize / 2, x_0 + fontSize / 2, y_0 - fontSize / 10);
        //Schräger Strich: Links unten nach rechts oben.
        g2.drawLine(x_0 + fontSize / 2, y_0 - fontSize / 10, x_0 + (3 * fontSize) / 4, y_0 - fontSize / 2);

    }

    /**
     * Zeichnen des NOT-Symbols.
     */
    private void drawSignNegation(Graphics g, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        g.drawString("\u00AC", x_0, y_0);
    }

    /**
     * Zeichnen des Implikationssymbols.
     */
    private void drawSignImplication(Graphics g, int x_0, int y_0, int fontSize) {
        Graphics2D g2 = (Graphics2D) g;
        // Strichdicke abhängig von fontSize berechnen.
        int thick = Math.max((int) Math.round(((double) this.fontSize) / 10), 1);
        g2.setStroke(new BasicStroke(thick));
        // Zwei waagerechter Strich
        g2.drawLine(x_0 + fontSize / 4, y_0 - fontSize / 4, x_0 + (5 * fontSize) / 8, y_0 - fontSize / 4);
        g2.drawLine(x_0 + fontSize / 4, y_0 - fontSize / 2, x_0 + (5 * fontSize) / 8, y_0 - fontSize / 2);
        // Zwei Pfeilspitzenstriche
        g2.drawLine(x_0 + (5 * fontSize) / 8, y_0 - (5 * fontSize) / 8, x_0 + (3 * fontSize) / 4, y_0 - (3 * fontSize) / 8);
        g2.drawLine(x_0 + (5 * fontSize) / 8, y_0 - fontSize / 8, x_0 + (3 * fontSize) / 4, y_0 - (3 * fontSize) / 8);
    }

    /**
     * Zeichnen des Äquivalenzsymbols.
     */
    private void drawSignEquivalence(Graphics g, int x_0, int y_0, int fontSize) {
        Graphics2D g2 = (Graphics2D) g;
        // Strichdicke abhängig von fontSize berechnen.
        int thick = Math.max((int) Math.round(((double) this.fontSize) / 10), 1);
        g2.setStroke(new BasicStroke(thick));
        // Zwei waagerechter Strich
        g2.drawLine(x_0 + (3 * fontSize) / 8, y_0 - fontSize / 4, x_0 + (5 * fontSize) / 8, y_0 - fontSize / 4);
        g2.drawLine(x_0 + (3 * fontSize) / 8, y_0 - fontSize / 2, x_0 + (5 * fontSize) / 8, y_0 - fontSize / 2);
        // Vier Pfeilspitzenstriche
        g2.drawLine(x_0 + (5 * fontSize) / 8, y_0 - (5 * fontSize) / 8, x_0 + (3 * fontSize) / 4, y_0 - (3 * fontSize) / 8);
        g2.drawLine(x_0 + (5 * fontSize) / 8, y_0 - fontSize / 8, x_0 + (3 * fontSize) / 4, y_0 - (3 * fontSize) / 8);
        g2.drawLine(x_0 + (3 * fontSize) / 8, y_0 - (5 * fontSize) / 8, x_0 + fontSize / 4, y_0 - (3 * fontSize) / 8);
        g2.drawLine(x_0 + (3 * fontSize) / 8, y_0 - fontSize / 8, x_0 + fontSize / 4, y_0 - (3 * fontSize) / 8);
    }

    private void drawConstant(Graphics g, Constant expr, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        if (expr.getValue().compareTo(BigDecimal.ZERO) < 0) {
            Constant exprNeg = new Constant(expr.getValue().negate());
            // Minuszeichen wird mit der internen Methode separat gezeichnet (der Einheitlichkeit halber).
            drawSignMinus(g, x_0, y_0 - (getHeightOfCenterOfExpression(g, exprNeg, fontSize) - fontSize / 2), fontSize);
            g.drawString(exprNeg.writeExpression(), x_0 + getWidthOfSignMinus(g, fontSize), y_0);
            return;
        }
        g.drawString(expr.writeExpression(), x_0, y_0);
    }

    private void drawVariable(Graphics g, Variable expr, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        if (expr.getName().equals("pi")) {
            // Unicode: pi = \u03C0
            g.drawString("\u03C0", x_0, y_0);
        } else {

            if (expr.getName().length() == 1 || !expr.getName().contains("_")) {
                g.drawString(expr.writeExpression(), x_0, y_0);
            } else {
                // Die Variable ist von der Form x_index mit eventuellen Apostrophs.
                int i = 2;
                while (i < expr.getName().length() && (int) expr.getName().charAt(i) >= 48 && (int) expr.getName().charAt(i) <= 57) {
                    i++;
                }
                // Ersten Buchstaben auslesen.
                String varName = expr.getName().substring(0, 1);
                // Index auslesen.
                String index = expr.getName().substring(2, i);
                // Apostrophs auslesen.
                String apostrophs = expr.getName().substring(i, expr.getName().length());

                int lengthVar = g.getFontMetrics().stringWidth(varName);

                // Ersten Buchstaben und die Apostrophs zeichnen.
                g.drawString(varName + apostrophs, x_0, y_0);
                setFont(g, getSizeForSub(fontSize));
                // Es wird zwischen der Variablen und dem Index getSizeForSub(fontSize) / 3 Pixel Platz gelassen.
                g.drawString(index, x_0 + lengthVar + getSizeForSub(fontSize) / 3, y_0 + Math.max(getSizeForSub(fontSize) / 2, 1));

            }

        }
    }

    private void drawBinaryOperationPlus(Graphics g, BinaryOperation expr, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightCenterLeft = getHeightOfCenterOfExpression(g, expr.getLeft(), fontSize);
        int heightCenterRight = getHeightOfCenterOfExpression(g, expr.getRight(), fontSize);

        drawExpression(g, expr.getLeft(), x_0,
                y_0 - Math.max(heightCenterRight - heightCenterLeft, 0),
                fontSize);
        drawSignPlus(g, x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize),
                y_0 - (Math.max(heightCenterLeft, heightCenterRight) - fontSize / 2), fontSize);

        ExpressionCollection factorsOfRight = SimplifyUtilities.getFactors(expr.getRight());

        if (!factorsOfRight.get(0).hasPositiveSign()) {

            int h_r = getHeightOfExpression(g, expr.getRight(), fontSize);
            drawOpeningBracket(g, x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize)
                    + fontSize, y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, h_r);
            drawExpression(g, expr.getRight(),
                    x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize)
                    + getWidthOfBracket(fontSize) + fontSize, y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                    fontSize);
            drawClosingBracket(g, x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize)
                    + getWidthOfBracket(fontSize) + fontSize
                    + getLengthOfExpression(g, expr.getRight(), fontSize),
                    y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, h_r);

        } else {

            drawExpression(g, expr.getRight(),
                    x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize)
                    + fontSize, y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                    fontSize);

        }

    }

    private void drawBinaryOperationMinus(Graphics g, BinaryOperation expr, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightCenterLeft = getHeightOfCenterOfExpression(g, expr.getLeft(), fontSize);
        int heightCenterRight = getHeightOfCenterOfExpression(g, expr.getRight(), fontSize);
        int heightRight = getHeightOfExpression(g, expr.getRight(), fontSize);
        int lengthLeft = getLengthOfExpression(g, expr.getLeft(), fontSize);

        if (expr.getLeft().equals(Expression.ZERO)) {
            lengthLeft = 0;
        } else {
            // left wird nur dann gezeichnet, wenn es nicht 0 ist.
            drawExpression(g, expr.getLeft(), x_0,
                    y_0 - Math.max(heightCenterRight - heightCenterLeft, 0),
                    fontSize);
        }

        drawSignMinus(g, x_0 + lengthLeft, y_0 - (Math.max(heightCenterLeft, heightCenterRight) - fontSize / 2), fontSize);

        ExpressionCollection factorsOfRight = SimplifyUtilities.getFactors(expr.getRight());

        if (expr.getRight().isSum() || expr.getRight().isDifference() || !factorsOfRight.get(0).hasPositiveSign()) {

            // Zeichnen von (right).
            drawOpeningBracket(g,
                    x_0 + lengthLeft + fontSize,
                    y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, heightRight);
            drawExpression(g, expr.getRight(),
                    x_0 + lengthLeft + (3 * fontSize) / 2, y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                    fontSize);
            drawClosingBracket(g, x_0 + lengthLeft
                    + getLengthOfExpression(g, expr.getRight(), fontSize)
                    + (3 * fontSize) / 2, y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, heightRight);

        } else {

            // Zeichnen von right.
            drawExpression(g, expr.getRight(),
                    x_0 + lengthLeft + fontSize, y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                    fontSize);

        }

    }

    private void drawBinaryOperationTimes(Graphics g, BinaryOperation expr, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightCenterLeft = getHeightOfCenterOfExpression(g, expr.getLeft(), fontSize);
        int heightCenterRight = getHeightOfCenterOfExpression(g, expr.getRight(), fontSize);
        int heightLeft = getHeightOfExpression(g, expr.getLeft(), fontSize);
        int heightRight = getHeightOfExpression(g, expr.getRight(), fontSize);

        if (expr.getLeft().isSum() || expr.getLeft().isDifference()) {

            // Fall: left benötigt eine Klammer.
            // Zeichnen von (left) * .
            drawOpeningBracket(g, x_0, y_0 - Math.max(heightCenterRight - heightCenterLeft, 0), fontSize, heightLeft);
            drawExpression(g, expr.getLeft(), x_0 + fontSize / 2, y_0 - Math.max(heightCenterRight - heightCenterLeft, 0), fontSize);
            drawClosingBracket(g, x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize) + fontSize / 2, y_0 - Math.max(heightCenterRight - heightCenterLeft, 0), fontSize, heightLeft);
            drawSignMult(g, x_0 + 2 * getWidthOfBracket(fontSize) + getLengthOfExpression(g, expr.getLeft(), fontSize),
                    y_0 - (Math.max(heightCenterLeft, heightCenterRight) - fontSize / 2), fontSize);

            if (expr.getRight().doesExpressionStartWithAMinusSign() || expr.getRight().isSum() || expr.getRight().isDifference()) {

                // Zeichnen von (right).
                drawOpeningBracket(g,
                        x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize) + 2 * getWidthOfBracket(fontSize) + getWidthOfSignMult(g, fontSize),
                        y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, heightRight);
                drawExpression(g, expr.getRight(),
                        x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize) + 3 * getWidthOfBracket(fontSize) + getWidthOfSignMult(g, fontSize),
                        y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                        fontSize);
                drawClosingBracket(g, x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize)
                        + getLengthOfExpression(g, expr.getRight(), fontSize)
                        + 3 * getWidthOfBracket(fontSize) + getWidthOfSignMult(g, fontSize), y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, heightRight);

            } else {

                // zeichnen von right.
                drawExpression(g, expr.getRight(),
                        x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize)
                        + 2 * fontSize, y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                        fontSize);

            }

        } else {

            // Fall: left benötigt keine Klammer.
            int lengthLeft = getLengthOfExpression(g, expr.getLeft(), fontSize);
            int lengthMultSign = getWidthOfSignMult(g, fontSize);

            if (expr.getLeft() instanceof Constant
                    && ((Constant) expr.getLeft()).getValue().compareTo(BigDecimal.valueOf(-1)) == 0) {

                // Falls left = -1: Statt (-1)*right nur -right zeichnen.
                lengthLeft = getWidthOfSignMinus(g, fontSize);
                lengthMultSign = 0;
                drawSignMinus(g, x_0, y_0 - (Math.max(heightCenterLeft, heightCenterRight) - fontSize / 2), fontSize);

            } else {

                // Zeichnen von left * .
                drawExpression(g, expr.getLeft(), x_0, y_0 - Math.max(heightCenterRight - heightCenterLeft, 0), fontSize);
                drawSignMult(g, x_0 + lengthLeft, y_0 - (Math.max(heightCenterLeft, heightCenterRight) - fontSize / 2), fontSize);

            }

            if (expr.getRight().doesExpressionStartWithAMinusSign()
                    || expr.getRight().isSum() || expr.getRight().isDifference()) {

                // Zeichnen von (right).
                drawOpeningBracket(g,
                        x_0 + lengthLeft + lengthMultSign,
                        y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, heightRight);
                drawExpression(g, expr.getRight(),
                        x_0 + lengthLeft + lengthMultSign
                        + getWidthOfBracket(fontSize), y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                        fontSize);
                drawClosingBracket(g, x_0 + lengthLeft + lengthMultSign
                        + getWidthOfBracket(fontSize)
                        + getLengthOfExpression(g, expr.getRight(), fontSize),
                        y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, heightRight);

            } else {

                // Zeichnen von right.
                drawExpression(g, expr.getRight(),
                        x_0 + lengthLeft + lengthMultSign,
                        y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                        fontSize);

            }

        }

    }

    private void drawBinaryOperationDiv(Graphics g, BinaryOperation expr, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        if (expr.doesExpressionStartWithAMinusSign()) {

            /* 
             Hier muss explizit der Konstruktor von BinaryOperation benutzt werden,
             damit beispielsweise a/1 explizit als a/1 ausgeschrieben wird und nicht als a!
             Letzteres wird nämlich bei a.div(1) passieren.
             */
            BinaryOperation exprNegated = new BinaryOperation(expr.getLeft().negate(), expr.getRight(), TypeBinary.DIV);

            // In diesem Fall: Minuszeichen aus dem Zähler vor den Bruchstrich schreiben.
            drawSignMinus(g, x_0,
                    y_0 - getHeightOfExpression(g, exprNegated.getRight(), fontSize), fontSize);
            drawExpression(g, exprNegated.getLeft(),
                    x_0 + getWidthOfSignMinus(g, fontSize) + (getLengthOfExpression(g, exprNegated, fontSize) - getLengthOfExpression(g, exprNegated.getLeft(), fontSize)) / 2,
                    y_0 - getHeightOfExpression(g, exprNegated.getRight(), fontSize) - fontSize,
                    fontSize);
            drawFractionLine(g, x_0 + getWidthOfSignMinus(g, fontSize),
                    y_0 - getHeightOfExpression(g, exprNegated.getRight(), fontSize) - fontSize / 2, fontSize,
                    Math.max(getLengthOfExpression(g, exprNegated.getLeft(), fontSize), getLengthOfExpression(g, exprNegated.getRight(), fontSize)));
            drawExpression(g, exprNegated.getRight(),
                    x_0 + getWidthOfSignMinus(g, fontSize) + (getLengthOfExpression(g, exprNegated, fontSize) - getLengthOfExpression(g, exprNegated.getRight(), fontSize)) / 2,
                    y_0, fontSize);

        } else {

            /* 
             Selbst wenn der Ausdruck im Nenner mit einem Minuszeichen beginnt,
             so wird dieser im Nenner gelassen. Nur derjenige im Zähler wird herausgezogen.
             */
            drawExpression(g, expr.getLeft(),
                    x_0 + (getLengthOfExpression(g, expr, fontSize) - getLengthOfExpression(g, expr.getLeft(), fontSize)) / 2,
                    y_0 - getHeightOfExpression(g, expr.getRight(), fontSize) - fontSize,
                    fontSize);
            drawFractionLine(g, x_0,
                    y_0 - getHeightOfExpression(g, expr.getRight(), fontSize) - fontSize / 2, fontSize,
                    getLengthOfExpression(g, expr, fontSize));
            drawExpression(g, expr.getRight(),
                    x_0 + (getLengthOfExpression(g, expr, fontSize) - getLengthOfExpression(g, expr.getRight(), fontSize)) / 2,
                    y_0, fontSize);

        }

    }

    private void drawBinaryOperationPow(Graphics g, BinaryOperation expr, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightLeft = getHeightOfExpression(g, expr.getLeft(), fontSize);

        if (expr.getLeft() instanceof BinaryOperation
                || (expr.getLeft() instanceof Constant
                && ((Constant) (expr.getLeft())).getValue().compareTo(BigDecimal.ZERO) < 0)
                || expr.getLeft().isOperator(TypeOperator.diff) || expr.getLeft().isOperator(TypeOperator.fac)
                || expr.getLeft().isOperator(TypeOperator.integral) || expr.getLeft().isOperator(TypeOperator.laplace)
                || expr.getLeft().isOperator(TypeOperator.prod) || expr.getLeft().isOperator(TypeOperator.sum)) {

            // Zeichnen von (left)^right
            drawOpeningBracket(g, x_0, y_0, fontSize, heightLeft);
            drawExpression(g, expr.getLeft(),
                    x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
            drawClosingBracket(g, x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize)
                    + getWidthOfBracket(fontSize), y_0, fontSize, heightLeft);
            drawExpression(g, expr.getRight(),
                    x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize)
                    + 2 * getWidthOfBracket(fontSize),
                    y_0 - heightLeft, getSizeForSup(fontSize));

        } else {

            // Zeichnen von left^right
            drawExpression(g, expr.getLeft(),
                    x_0, y_0, fontSize);
            drawExpression(g, expr.getRight(),
                    x_0 + getLengthOfExpression(g, expr.getLeft(), fontSize),
                    y_0 - heightLeft, getSizeForSup(fontSize));

        }

    }

    private void drawFunction(Graphics g, Function f, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightLeft = getHeightOfExpression(g, f.getLeft(), fontSize);
        int heightCenterLeft = getHeightOfCenterOfExpression(g, f.getLeft(), fontSize);

        if (!f.getType().equals(TypeFunction.abs)) {

            setFont(g, fontSize);
            g.drawString(f.getName(), x_0, y_0 - (heightCenterLeft - (2 * fontSize) / 5));
            drawOpeningBracket(g, x_0 + g.getFontMetrics().stringWidth(f.getName()), y_0, fontSize, heightLeft);
            drawExpression(g, f.getLeft(),
                    x_0 + g.getFontMetrics().stringWidth(f.getName()) + getWidthOfBracket(fontSize),
                    y_0, fontSize);
            setFont(g, fontSize);
            drawClosingBracket(g,
                    x_0 + g.getFontMetrics().stringWidth(f.getName()) + getWidthOfBracket(fontSize)
                    + getLengthOfExpression(g, f.getLeft(), fontSize),
                    y_0, fontSize, heightLeft);

        } else {

            // Beim Betrag sollen Betragsstriche gezeichnet werden, anstatt abs(...) auszuschreiben.
            drawAbsLine(g, x_0, y_0, fontSize, heightLeft);
            drawExpression(g, f.getLeft(), x_0 + getWidthOfAbsBracket(fontSize), y_0, fontSize);
            drawAbsLine(g,
                    x_0 + getWidthOfAbsBracket(fontSize) + getLengthOfExpression(g, f.getLeft(), fontSize),
                    y_0, fontSize, heightLeft);

        }

    }

    private void drawOperator(Graphics g, Operator operator, int x_0, int y_0, int fontSize) {

        /*
         Unicodes: SIGMA = \u03A3, PI = \u03A0, INT = \u222B, sqrt = \u221A,
         LAPLACE (DELTA) = \u0394, DEL (PARTIAL) = \u2202.
         */
        if (operator.getType().equals(TypeOperator.diff)) {
            drawOperatorDiff(g, operator, x_0, y_0, fontSize);
        } else if (operator.getType().equals(TypeOperator.fac)) {
            drawOperatorFac(g, operator, x_0, y_0, fontSize);
        } else if (operator.getType().equals(TypeOperator.integral)) {
            drawOperatorInt(g, operator, x_0, y_0, fontSize);
        } else if (operator.getType().equals(TypeOperator.laplace)) {
            drawOperatorLaplace(g, operator, x_0, y_0, fontSize);
        } else if (operator.getType().equals(TypeOperator.prod)) {
            drawOperatorProd(g, operator, x_0, y_0, fontSize);
        } else if (operator.getType().equals(TypeOperator.sum)) {
            drawOperatorSum(g, operator, x_0, y_0, fontSize);
        } else {
            drawOperatorDefault(g, operator, x_0, y_0, fontSize);
        }

    }

    private void drawOperatorDiff(Graphics g, Operator operator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = operator.getParams();
        int lengthEnumeratorDifferentialOperator, lengthDifferentialOperator;
        int heightCenterOperand = getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize);
        int heightOperand = getHeightOfExpression(g, (Expression) params[0], fontSize);
        int lengthOperand = getLengthOfExpression(g, (Expression) params[0], fontSize);

        if (params.length == 2) {

            lengthEnumeratorDifferentialOperator = getWidthOfSignPartial(g, fontSize);
            lengthDifferentialOperator = getWidthOfSignPartial(g, fontSize) + g.getFontMetrics().stringWidth((String) params[1]);

            // Im Folgenden sei x = var.
            // Zeichnen von dx (im Nenner des Operators) 
            drawSignPartial(g, x_0, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2), fontSize);
            g.drawString((String) params[1], x_0 + lengthEnumeratorDifferentialOperator, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2));

            // Zeichnen des Bruchstrichs
            drawFractionLine(g, x_0, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2) - (3 * fontSize) / 2, fontSize, lengthDifferentialOperator);

            // Zeichnen von d (im Zähler des Operators)
            drawSignPartial(g, x_0 + (lengthDifferentialOperator - lengthEnumeratorDifferentialOperator) / 2,
                    y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2) - 2 * fontSize, fontSize);

            /*
             Zeichnen des Operanden: Die Addition von fontSize / 2 ist dazu da, 
             damit nach dem Differentialoperator etwas Abstand zum nächsten 
             Zeichen ist (nämlich fontSize / 2 Pixel).
             */
            lengthDifferentialOperator = lengthDifferentialOperator + fontSize / 2;
            drawOpeningBracket(g, x_0 + lengthDifferentialOperator, y_0 - Math.max(0, (3 * fontSize) / 2 - heightCenterOperand), fontSize, heightOperand);
            drawExpression(g, (Expression) params[0], x_0 + lengthDifferentialOperator + getWidthOfBracket(fontSize),
                    y_0 - Math.max(0, (3 * fontSize) / 2 - heightCenterOperand), fontSize);
            drawClosingBracket(g, x_0 + lengthDifferentialOperator + getWidthOfBracket(fontSize) + lengthOperand,
                    y_0 - Math.max(0, (3 * fontSize) / 2 - heightCenterOperand), fontSize, heightOperand);

        } else if (params.length == 3 && params[2] instanceof Integer) {

            int lengthPartialSign = getWidthOfSignPartial(g, fontSize);
            lengthDifferentialOperator = lengthPartialSign + g.getFontMetrics().stringWidth((String) params[1]) + getLengthOfConstant(g, new Constant((int) params[2]), getSizeForSup(fontSize));
            lengthEnumeratorDifferentialOperator = lengthPartialSign + getLengthOfConstant(g, new Constant((int) params[2]), getSizeForSup(fontSize));

            // Im Folgenden sei x = var, k = Ordnung der Ableitung.
            // Zeichnen von dx^k (im Nenner des Operators) 
            drawSignPartial(g, x_0, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2 - getSizeForSup(fontSize)), fontSize);
            g.drawString((String) params[1], x_0 + lengthPartialSign, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2 - getSizeForSup(fontSize)));
            drawExpression(g, new Constant((int) params[2]), x_0 + lengthPartialSign + g.getFontMetrics().stringWidth((String) params[1]),
                    y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2 - getSizeForSup(fontSize)) - fontSize, getSizeForSup(fontSize));

            // Zeichnen des Bruchstrichs
            drawFractionLine(g, x_0, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2 - getSizeForSup(fontSize)) - (3 * fontSize) / 2 - getSizeForSup(fontSize),
                    fontSize, lengthDifferentialOperator);

            // Zeichnen von d^k (im Zähler des Operators)
            drawSignPartial(g, x_0 + (lengthDifferentialOperator - lengthEnumeratorDifferentialOperator) / 2,
                    y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2 - getSizeForSup(fontSize)) - 2 * fontSize - getSizeForSup(fontSize), fontSize);
            drawConstant(g, new Constant((int) params[2]), x_0 + (lengthDifferentialOperator - lengthEnumeratorDifferentialOperator) / 2 + lengthPartialSign,
                    y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2 - getSizeForSup(fontSize)) - 3 * fontSize - getSizeForSup(fontSize), getSizeForSup(fontSize));

            /*
             Zeichnen des Operanden Die Addition von fontSize / 2 ist dazu da,
             damit nach dem Differentialoperator etwas Abstand zum nächsten
             Zeichen ist (nämlich fontSize / 2 Pixel).
             */
            lengthDifferentialOperator = lengthDifferentialOperator + fontSize / 2;
            drawOpeningBracket(g, x_0 + lengthDifferentialOperator, y_0 - Math.max(0, (3 * fontSize) / 2 + getSizeForSup(fontSize) - heightCenterOperand), fontSize, heightOperand);
            drawExpression(g, (Expression) params[0], x_0 + lengthDifferentialOperator + getWidthOfBracket(fontSize),
                    y_0 - Math.max(0, (3 * fontSize) / 2 + getSizeForSup(fontSize) - heightCenterOperand), fontSize);
            drawClosingBracket(g, x_0 + lengthDifferentialOperator + getWidthOfBracket(fontSize) + lengthOperand,
                    y_0 - Math.max(0, (3 * fontSize) / 2 + getSizeForSup(fontSize) - heightCenterOperand), fontSize, heightOperand);

        } else {

            int lengthPartialSign = getWidthOfSignPartial(g, fontSize);
            lengthDifferentialOperator = g.getFontMetrics().stringWidth((String) params[1]);
            for (int i = 2; i < params.length; i++) {
                lengthDifferentialOperator = lengthDifferentialOperator + g.getFontMetrics().stringWidth((String) params[i]);
            }
            lengthDifferentialOperator = lengthDifferentialOperator + (params.length - 1) * getWidthOfSignPartial(g, fontSize);

            lengthEnumeratorDifferentialOperator = lengthPartialSign + getLengthOfConstant(g, new Constant(params.length - 1), getSizeForSup(fontSize));

            /*
             Im Folgenden sei x = var, k = Anzahl der Variablen, nach denen
             differenziart wird, also = params.length - 1.
             */
            // Zeichnen von dx_1 ... dx_k (im Nenner des Operators) 
            int l_current = 0;
            for (int i = 1; i < params.length; i++) {
                drawSignPartial(g, x_0 + l_current, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2), fontSize);
                g.drawString((String) params[i], x_0 + l_current + lengthPartialSign, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2));
                l_current = l_current + lengthPartialSign + g.getFontMetrics().stringWidth((String) params[i]);
            }

            // Zeichnen des Bruchstrichs
            drawFractionLine(g, x_0, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2) - (3 * fontSize) / 2, fontSize, lengthDifferentialOperator);

            // Zeichnen von d^k (im Zähler des Operators)
            drawSignPartial(g, x_0 + (lengthDifferentialOperator - lengthEnumeratorDifferentialOperator) / 2,
                    y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2) - 2 * fontSize, fontSize);
            drawConstant(g, new Constant(params.length - 1), x_0 + (lengthDifferentialOperator - lengthEnumeratorDifferentialOperator) / 2 + lengthPartialSign,
                    y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2) - 3 * fontSize, getSizeForSup(fontSize));

            /*
             Zeichnen des Operanden: Die Addition von fontSize / 2 ist dazu
             da, damit nach dem Differentialoperator etwas Abstand zum
             nächsten Zeichen ist (nämlich fontSize / 2 Pixel).
             */
            lengthDifferentialOperator = lengthDifferentialOperator + fontSize / 2;
            drawOpeningBracket(g, x_0 + lengthDifferentialOperator, y_0 - Math.max(0, (3 * fontSize) / 2 - heightCenterOperand), fontSize, heightOperand);
            drawExpression(g, (Expression) params[0], x_0 + lengthDifferentialOperator + getWidthOfBracket(fontSize),
                    y_0 - Math.max(0, (3 * fontSize) / 2 - heightCenterOperand), fontSize);
            drawClosingBracket(g, x_0 + lengthDifferentialOperator + getWidthOfBracket(fontSize) + lengthOperand,
                    y_0 - Math.max(0, (3 * fontSize) / 2 - heightCenterOperand), fontSize, heightOperand);

        }

    }

    private void drawOperatorFac(Graphics g, Operator operator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Expression argument = (Expression) operator.getParams()[0];

        if (argument instanceof BinaryOperation || (argument instanceof Constant
                && ((Constant) argument).getValue().compareTo(BigDecimal.ZERO) < 0)) {

            int heightArgument = getHeightOfExpression(g, argument, fontSize);
            drawOpeningBracket(g, x_0, y_0, fontSize, heightArgument);
            drawExpression(g, argument, x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
            drawClosingBracket(g, x_0 + getWidthOfBracket(fontSize) + getLengthOfExpression(g, argument, fontSize), y_0, fontSize, heightArgument);
            drawSignFac(g, x_0 + getLengthOfExpression(g, argument, fontSize) + 2 * getWidthOfBracket(fontSize),
                    y_0 - (getHeightOfCenterOfExpression(g, argument, fontSize) - (2 * fontSize) / 5),
                    fontSize);

        } else {

            drawExpression(g, argument, x_0, y_0, fontSize);
            drawSignFac(g, x_0 + getLengthOfExpression(g, argument, fontSize),
                    y_0 - (getHeightOfCenterOfExpression(g, argument, fontSize) - (2 * fontSize) / 5),
                    fontSize);

        }

    }

    private void drawOperatorInt(Graphics g, Operator operator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = ((Operator) operator).getParams();
        int heightIntegrand = getHeightOfExpression(g, (Expression) params[0], fontSize);
        int heightCenterIntegrand = getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize);
        int lengthIntegrand = getLengthOfExpression(g, (Expression) params[0], fontSize);

        if (params.length == 2) {

            //Integralzeichen
            drawSignIntegral(g, x_0, y_0, heightIntegrand);
            //Integrand
            drawExpression(g, (Expression) params[0], x_0 + getWidthOfSignIntegral(g, fontSize, heightIntegrand), y_0, fontSize);
            //dx
            setFont(g, fontSize);
            g.drawString(" d", x_0 + getWidthOfSignIntegral(g, fontSize, heightIntegrand) + lengthIntegrand,
                    y_0 - (heightCenterIntegrand - (2 * fontSize) / 5));
            setFont(g, fontSize);
            drawVariable(g, Variable.create((String) params[1]),
                    x_0 + getWidthOfSignIntegral(g, fontSize, heightIntegrand) + lengthIntegrand + g.getFontMetrics().stringWidth(" d"),
                    y_0 - (heightCenterIntegrand - (2 * fontSize) / 5), fontSize);

        } else {

            int heightLowerLimit = getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
            int lengthLowerLimit = getLengthOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
            int lengthUpperLimit = getLengthOfExpression(g, (Expression) params[3], getSizeForSup(fontSize));
            int lengthIntegralSign = getWidthOfSignIntegral(g, fontSize, heightIntegrand);
            int lengthOfIntegralSignWithLimits = Math.max(Math.max(lengthUpperLimit, lengthLowerLimit), lengthIntegralSign);

            //Untere Grenze
            drawExpression(g, (Expression) params[2], x_0 + (lengthOfIntegralSignWithLimits - lengthLowerLimit) / 2, y_0, getSizeForSup(fontSize));
            //Integralzeichen
            drawSignIntegral(g, x_0 + (lengthOfIntegralSignWithLimits - lengthIntegralSign) / 2, y_0 - heightLowerLimit, heightIntegrand);
            //Obere Grenze
            drawExpression(g, (Expression) params[3], x_0 + (lengthOfIntegralSignWithLimits - lengthUpperLimit) / 2, y_0 - heightLowerLimit - heightIntegrand, getSizeForSup(fontSize));
            //Integrand
            drawExpression(g, (Expression) params[0], x_0 + lengthOfIntegralSignWithLimits, y_0 - heightLowerLimit, fontSize);
            //dx
            setFont(g, fontSize);
            g.drawString(" d", x_0 + lengthOfIntegralSignWithLimits + lengthIntegrand,
                    y_0 - heightLowerLimit - (heightCenterIntegrand - (2 * fontSize) / 5));
            drawVariable(g, Variable.create((String) params[1]),
                    x_0 + lengthOfIntegralSignWithLimits + lengthIntegrand + g.getFontMetrics().stringWidth(" d"),
                    y_0 - heightLowerLimit - (heightCenterIntegrand - (2 * fontSize) / 5), fontSize);

        }

    }

    private void drawOperatorLaplace(Graphics g, Operator operator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightOperand = getHeightOfExpression(g, (Expression) operator.getParams()[0], fontSize);
        int heightCenterOperand = getHeightOfCenterOfExpression(g, (Expression) operator.getParams()[0], fontSize);
        int lengthOperand = getLengthOfExpression(g, (Expression) operator.getParams()[0], fontSize);
        //Delta
        drawSignDelta(g, x_0, y_0 - (heightCenterOperand - (2 * fontSize) / 5), fontSize);
        //(
        drawOpeningBracket(g, x_0 + getWidthOfSignDelta(g, fontSize), y_0, fontSize, heightOperand);
        //Argument
        drawExpression(g, (Expression) operator.getParams()[0], x_0 + getWidthOfSignDelta(g, fontSize) + getWidthOfBracket(fontSize), y_0, fontSize);
        //)
        drawClosingBracket(g, x_0 + getWidthOfSignDelta(g, fontSize) + getWidthOfBracket(fontSize) + lengthOperand, y_0, fontSize, heightOperand);

    }

    private void drawOperatorProd(Graphics g, Operator operator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = ((Operator) operator).getParams();
        int heightFactor = getHeightOfExpression(g, (Expression) params[0], fontSize);
        int lengthFactor = getLengthOfExpression(g, (Expression) params[0], fontSize);
        int heightLowerLimit = getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        int heightCenterLowerLimit = getHeightOfCenterOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        int heightCenterVarEquals = getHeightOfCenterOfExpression(g, Variable.create((String) params[1]), getSizeForSup(fontSize));
        setFont(g, getSizeForSup(fontSize));
        int lengthVarEquals = g.getFontMetrics().stringWidth((String) params[1] + " = ");
        int lengthLowerLimit = getLengthOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        int lengthUpperLimit = getLengthOfExpression(g, (Expression) params[3], getSizeForSup(fontSize));
        int lengthPiSign = getWidthOfSignPi(g, fontSize, heightFactor);
        int lengthOfPiSignWithLimits = Math.max(Math.max(lengthUpperLimit, lengthVarEquals + lengthLowerLimit), lengthPiSign);

        //Untere Grenze mitsamt dem String "var="
        //Zunächst Schriftgröße verkleinern, da Indizes kleinerev Schriftgröße besitzen.
        setFont(g, getSizeForSup(fontSize));
        g.drawString((String) params[1] + " = ", x_0 + (lengthOfPiSignWithLimits - lengthLowerLimit - lengthVarEquals) / 2, y_0 - (heightCenterLowerLimit - heightCenterVarEquals));
        drawExpression(g, (Expression) params[2], x_0 + (lengthOfPiSignWithLimits - lengthLowerLimit - lengthVarEquals) / 2 + lengthVarEquals, y_0, getSizeForSup(fontSize));
        //Produktzeichen
        drawSignPi(g, x_0 + (lengthOfPiSignWithLimits - lengthPiSign) / 2, y_0 - heightLowerLimit, heightFactor);
        //Obere Grenze
        drawExpression(g, (Expression) params[3], x_0 + (lengthOfPiSignWithLimits - lengthUpperLimit) / 2, y_0 - heightLowerLimit - heightFactor, getSizeForSup(fontSize));
        //Produktfunktion
        if (((Expression) params[0]) instanceof BinaryOperation
                && ((((BinaryOperation) params[0]).getType()).equals(TypeBinary.PLUS)
                || (((BinaryOperation) params[0]).getType()).equals(TypeBinary.MINUS))) {
            drawOpeningBracket(g, x_0 + lengthOfPiSignWithLimits, y_0 - heightLowerLimit, fontSize, heightFactor);
            drawExpression(g, (Expression) params[0], x_0 + lengthOfPiSignWithLimits + getWidthOfBracket(fontSize), y_0 - heightLowerLimit, fontSize);
            drawClosingBracket(g, x_0 + lengthOfPiSignWithLimits + getWidthOfBracket(fontSize) + lengthFactor,
                    y_0 - heightLowerLimit, fontSize, heightFactor);
        } else {
            drawExpression(g, (Expression) params[0], x_0 + lengthOfPiSignWithLimits, y_0 - heightLowerLimit, fontSize);
        }

    }

    private void drawOperatorSum(Graphics g, Operator operator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = ((Operator) operator).getParams();
        int heightSummand = getHeightOfExpression(g, (Expression) params[0], fontSize);
        int lengthSummand = getLengthOfExpression(g, (Expression) params[0], fontSize);
        int heightLowerLimit = getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        int heightCenterLowerLimit = getHeightOfCenterOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        int heightCenterVarEquals = getHeightOfCenterOfExpression(g, Variable.create((String) params[1]), getSizeForSup(fontSize));
        setFont(g, getSizeForSup(fontSize));
        int lengthVarEquals = g.getFontMetrics().stringWidth((String) params[1] + " = ");
        int lengthLowerLimit = getLengthOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        int lengthUpperLimit = getLengthOfExpression(g, (Expression) params[3], getSizeForSup(fontSize));
        int lengthSigmaSign = getWidthOfSignSigma(g, fontSize, heightSummand);
        int lengthOfSigmaSignWithLimits = Math.max(Math.max(lengthUpperLimit, lengthVarEquals + lengthLowerLimit), lengthSigmaSign);

        //Untere Grenze mitsamt dem String "var="
        //Zunächst Schriftgröße verkleinern, da Indizes kleinerev Schriftgröße besitzen.
        setFont(g, getSizeForSup(fontSize));
        g.drawString((String) params[1] + " = ", x_0 + (lengthOfSigmaSignWithLimits - lengthLowerLimit - lengthVarEquals) / 2, y_0 - (heightCenterLowerLimit - heightCenterVarEquals));
        drawExpression(g, (Expression) params[2], x_0 + (lengthOfSigmaSignWithLimits - lengthLowerLimit - lengthVarEquals) / 2 + lengthVarEquals, y_0, getSizeForSup(fontSize));
        //Produktzeichen
        drawSignSigma(g, x_0 + (lengthOfSigmaSignWithLimits - lengthSigmaSign) / 2, y_0 - heightLowerLimit, heightSummand);
        //Obere Grenze
        drawExpression(g, (Expression) params[3], x_0 + (lengthOfSigmaSignWithLimits - lengthUpperLimit) / 2, y_0 - heightLowerLimit - heightSummand, getSizeForSup(fontSize));
        //Produktfunktion
        if (((Expression) params[0]) instanceof BinaryOperation
                && ((((BinaryOperation) params[0]).getType()).equals(TypeBinary.PLUS)
                || (((BinaryOperation) params[0]).getType()).equals(TypeBinary.MINUS))) {
            drawOpeningBracket(g, x_0 + lengthOfSigmaSignWithLimits, y_0 - heightLowerLimit, fontSize, heightSummand);
            drawExpression(g, (Expression) params[0], x_0 + lengthOfSigmaSignWithLimits + getWidthOfBracket(fontSize), y_0 - heightLowerLimit, fontSize);
            drawClosingBracket(g, x_0 + lengthOfSigmaSignWithLimits + getWidthOfBracket(fontSize) + lengthSummand,
                    y_0 - heightLowerLimit, fontSize, heightSummand);
        } else {
            drawExpression(g, (Expression) params[0], x_0 + lengthOfSigmaSignWithLimits, y_0 - heightLowerLimit, fontSize);
        }

    }

    /**
     * Zeichnen aller anderen Operatoren, welche keine besonderen Symbole
     * gebrauchen (also div, gcd, lcm, mod, ...).
     */
    private void drawOperatorDefault(Graphics g, Operator operator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightOperator = getHeightOfExpression(g, operator, fontSize);
        int heightCenterOperator = getHeightOfCenterOfExpression(g, operator, fontSize);
        Object[] left = ((Operator) operator).getParams();
        String name = Operator.getNameFromType(((Operator) operator).getType());

        if (((Operator) operator).getType().equals(TypeOperator.taylor)) {
            name = "T";
        }

        g.drawString(name, x_0, y_0 - (heightCenterOperator - (2 * fontSize) / 5));
        drawOpeningBracket(g, x_0 + g.getFontMetrics().stringWidth(name), y_0, fontSize, heightOperator);

        int distanceFromOpeningBracket = 0;

        for (int i = 0; i < left.length; i++) {

            if (left[i] instanceof Expression) {
                drawExpression(g, (Expression) left[i],
                        x_0 + g.getFontMetrics().stringWidth(name)
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterOperator - getHeightOfCenterOfExpression(g, (Expression) left[i], fontSize)), fontSize);
                distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, (Expression) left[i], fontSize);
            } else if (left[i] instanceof String) {
                setFont(g, fontSize);
                g.drawString((String) left[i], x_0 + g.getFontMetrics().stringWidth(name)
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterOperator - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth((String) left[i]);
            } else if (left[i] instanceof Integer) {
                setFont(g, fontSize);
                g.drawString(String.valueOf((Integer) left[i]), x_0 + g.getFontMetrics().stringWidth(name)
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterOperator - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(String.valueOf((Integer) left[i]));
            }

            if (i < left.length - 1) {
                setFont(g, fontSize);
                g.drawString(", ", x_0 + g.getFontMetrics().stringWidth(name)
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterOperator - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");
            }

        }

        setFont(g, fontSize);
        drawClosingBracket(g,
                x_0 + g.getFontMetrics().stringWidth(name)
                + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0, fontSize, heightOperator);

    }

    private void drawSelfDefinedFunction(Graphics g, SelfDefinedFunction f, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightF = getHeightOfExpression(g, f, fontSize);
        int heightCenterF = getHeightOfCenterOfExpression(g, f, fontSize);
        Expression[] left = f.getLeft();

        g.drawString(((SelfDefinedFunction) f).getName(), x_0, y_0 - (heightCenterF - (2 * fontSize) / 5));
        drawOpeningBracket(g, x_0 + g.getFontMetrics().stringWidth(((SelfDefinedFunction) f).getName()), y_0, fontSize, heightF);

        int distanceFromOpeningBracket = 0;

        for (int i = 0; i < left.length; i++) {

            drawExpression(g, left[i],
                    x_0 + g.getFontMetrics().stringWidth(((SelfDefinedFunction) f).getName())
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterF - getHeightOfCenterOfExpression(g, left[i], fontSize)), fontSize);
            distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, left[i], fontSize);

            if (i < left.length - 1) {
                setFont(g, fontSize);
                g.drawString(", ", x_0 + g.getFontMetrics().stringWidth(((SelfDefinedFunction) f).getName())
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterF - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");
            }

        }

        setFont(g, fontSize);
        drawClosingBracket(g,
                x_0 + g.getFontMetrics().stringWidth(((SelfDefinedFunction) f).getName())
                + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0, fontSize, heightF);

    }

    /**
     * Hauptmethode zum Zeichnen einer Expression expr.
     */
    private void drawExpression(Graphics g, Expression expr, int x_0, int y_0, int fontSize) {

        if (expr instanceof Constant) {
            drawConstant(g, (Constant) expr, x_0, y_0, fontSize);
        } else if (expr instanceof Variable) {
            drawVariable(g, (Variable) expr, x_0, y_0, fontSize);
        } else if (expr instanceof BinaryOperation) {

            if (expr.isSum()) {
                drawBinaryOperationPlus(g, (BinaryOperation) expr, x_0, y_0, fontSize);
            } else if (expr.isDifference()) {
                drawBinaryOperationMinus(g, (BinaryOperation) expr, x_0, y_0, fontSize);
            } else if (expr.isProduct()) {
                drawBinaryOperationTimes(g, (BinaryOperation) expr, x_0, y_0, fontSize);
            } else if (expr.isQuotient()) {
                drawBinaryOperationDiv(g, (BinaryOperation) expr, x_0, y_0, fontSize);
            } else {
                drawBinaryOperationPow(g, (BinaryOperation) expr, x_0, y_0, fontSize);
            }

        } else if (expr instanceof Function) {
            drawFunction(g, (Function) expr, x_0, y_0, fontSize);
        } else if (expr instanceof Operator) {
            drawOperator(g, (Operator) expr, x_0, y_0, fontSize);
        } else {
            drawSelfDefinedFunction(g, (SelfDefinedFunction) expr, x_0, y_0, fontSize);
        }

    }

    /**
     * Hauptmethode zum Zeichnen einer Expression expr, welche von Klammern
     * umgeben ist.
     */
    private void drawExpressionSurroundedByBrackets(Graphics g, Expression expr, int x_0, int y_0, int fontSize) {

        int heightExpr = getHeightOfExpression(g, expr, fontSize);

        // Öffnende Klammer zeichnen.
        drawOpeningBracket(g, x_0, y_0, fontSize, heightExpr);

        if (expr instanceof Constant) {
            drawConstant(g, (Constant) expr, x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
        } else if (expr instanceof Variable) {
            drawVariable(g, (Variable) expr, x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
        } else if (expr instanceof BinaryOperation) {

            BinaryOperation expr_as_binaryoperation = (BinaryOperation) expr;
            if (expr_as_binaryoperation.getType().equals(TypeBinary.PLUS)) {
                drawBinaryOperationPlus(g, (BinaryOperation) expr, x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
            } else if (expr_as_binaryoperation.getType().equals(TypeBinary.MINUS)) {
                drawBinaryOperationMinus(g, (BinaryOperation) expr, x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
            } else if (expr_as_binaryoperation.getType().equals(TypeBinary.TIMES)) {
                drawBinaryOperationTimes(g, (BinaryOperation) expr, x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
            } else if (expr_as_binaryoperation.getType().equals(TypeBinary.DIV)) {
                drawBinaryOperationDiv(g, (BinaryOperation) expr, x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
            } else {
                drawBinaryOperationPow(g, (BinaryOperation) expr, x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
            }

        } else if (expr instanceof Function) {
            drawFunction(g, (Function) expr, x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
        } else if (expr instanceof Operator) {
            drawOperator(g, (Operator) expr, x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
        } else {
            drawSelfDefinedFunction(g, (SelfDefinedFunction) expr, x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
        }

        // Schließende Klammer zeichnen.
        drawClosingBracket(g, x_0 + getWidthOfBracket(fontSize) + getLengthOfExpression(g, expr, fontSize), y_0, fontSize, heightExpr);

    }

    /**
     * Hauptmethode zum Zeichnen einer LogicalExpression log_expr.
     */
    private void drawLogicalExpression(Graphics g, LogicalExpression logExpr, int x_0, int y_0, int fontSize) {

        if (logExpr instanceof LogicalConstant) {
            drawLogicalConstant(g, (LogicalConstant) logExpr, x_0, y_0, fontSize);
        } else if (logExpr instanceof LogicalVariable) {
            drawLogicalVariable(g, (LogicalVariable) logExpr, x_0, y_0, fontSize);
        } else if (logExpr instanceof LogicalUnaryOperation) {
            drawLogicalUnaryOperation(g, (LogicalUnaryOperation) logExpr, x_0, y_0, fontSize);
        } else if (logExpr instanceof LogicalBinaryOperation) {
            drawLogicalBinaryOperation(g, (LogicalBinaryOperation) logExpr, x_0, y_0, fontSize);
        }

    }

    /**
     * Methode zum Zeichnen einer logischen Konstante.
     */
    private void drawLogicalConstant(Graphics g, LogicalConstant logExpr, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        g.drawString(logExpr.writeLogicalExpression(), x_0, y_0);
    }

    /**
     * Methode zum Zeichnen einer logischen Variablen.
     */
    private void drawLogicalVariable(Graphics g, LogicalVariable logExpr, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        g.drawString(logExpr.writeLogicalExpression(), x_0, y_0);
    }

    /**
     * Methode zum Zeichnen einer logischen unären Operation.
     */
    private void drawLogicalUnaryOperation(Graphics g, LogicalUnaryOperation logExpr, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        drawSignNegation(g, x_0, y_0, fontSize);
        int l = getWidthOfSignNegation(g, fontSize);
        if (logExpr.getLeft() instanceof LogicalVariable) {
            /**
             * Zeichnen von !logExpr.
             */
            drawLogicalExpression(g, logExpr.getLeft(), x_0 + l, y_0, fontSize);
        } else {
            /**
             * Zeichnen von !(logExpr). Die Höhe eines logischen Ausdrucks ist
             * immer == fontSize.
             */
            drawOpeningBracket(g, x_0 + l, y_0, fontSize, fontSize);
            drawLogicalExpression(g, logExpr.getLeft(), x_0 + l + getWidthOfBracket(fontSize), y_0, fontSize);
            drawClosingBracket(g, x_0 + l + getWidthOfBracket(fontSize) + getLengthOfLogicalExpression(g, logExpr.getLeft(), fontSize),
                    y_0, fontSize, fontSize);
        }
    }

    /**
     * Methode zum Zeichnen einer logischen Binäroperation.
     */
    private void drawLogicalBinaryOperation(Graphics g, LogicalBinaryOperation logExpr, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int l_left = getLengthOfLogicalExpression(g, logExpr.getLeft(), fontSize);
        int l_right = getLengthOfLogicalExpression(g, logExpr.getRight(), fontSize);
        int distanceFromBeginning = 0;

        if (logExpr.isAnd()) {

            if (logExpr.getLeft().isOr() || logExpr.getLeft().isImpl() || logExpr.getLeft().isEquiv()) {
                drawOpeningBracket(g, x_0, y_0, fontSize, fontSize);
                distanceFromBeginning = getWidthOfBracket(fontSize);
                drawLogicalExpression(g, logExpr.getLeft(), x_0 + distanceFromBeginning, y_0, fontSize);
                distanceFromBeginning = distanceFromBeginning + l_left;
                drawClosingBracket(g, x_0 + distanceFromBeginning, y_0, fontSize, fontSize);
                distanceFromBeginning = distanceFromBeginning + getWidthOfBracket(fontSize);
            } else {
                drawLogicalExpression(g, logExpr.getLeft(), x_0 + distanceFromBeginning, y_0, fontSize);
                distanceFromBeginning = l_left;
            }

            drawSignAnd(g, x_0 + distanceFromBeginning, y_0, fontSize);
            distanceFromBeginning = distanceFromBeginning + getWidthOfSignAnd(fontSize);

            if (logExpr.getRight().isOr() || logExpr.getRight().isImpl() || logExpr.getRight().isEquiv()) {
                drawOpeningBracket(g, x_0 + distanceFromBeginning, y_0, fontSize, fontSize);
                distanceFromBeginning = distanceFromBeginning + getWidthOfBracket(fontSize);
                drawLogicalExpression(g, logExpr.getRight(), x_0 + distanceFromBeginning, y_0, fontSize);
                distanceFromBeginning = distanceFromBeginning + l_right;
                drawClosingBracket(g, x_0 + distanceFromBeginning, y_0, fontSize, fontSize);
            } else {
                drawLogicalExpression(g, logExpr.getRight(), x_0 + distanceFromBeginning, y_0, fontSize);
            }

        } else if (logExpr.isOr()) {

            if (logExpr.getLeft().isImpl() || logExpr.getLeft().isEquiv()) {
                drawOpeningBracket(g, x_0, y_0, fontSize, fontSize);
                distanceFromBeginning = getWidthOfBracket(fontSize);
                drawLogicalExpression(g, logExpr.getLeft(), x_0 + distanceFromBeginning, y_0, fontSize);
                distanceFromBeginning = distanceFromBeginning + l_left;
                drawClosingBracket(g, x_0 + distanceFromBeginning, y_0, fontSize, fontSize);
                distanceFromBeginning = distanceFromBeginning + getWidthOfBracket(fontSize);
            } else {
                drawLogicalExpression(g, logExpr.getLeft(), x_0 + distanceFromBeginning, y_0, fontSize);
                distanceFromBeginning = l_left;
            }

            drawSignOr(g, x_0 + distanceFromBeginning, y_0, fontSize);
            distanceFromBeginning = distanceFromBeginning + getWidthOfSignAnd(fontSize);

            if (logExpr.getRight().isImpl() || logExpr.getRight().isEquiv()) {
                drawOpeningBracket(g, x_0 + distanceFromBeginning, y_0, fontSize, fontSize);
                distanceFromBeginning = distanceFromBeginning + getWidthOfBracket(fontSize);
                drawLogicalExpression(g, logExpr.getRight(), x_0 + distanceFromBeginning, y_0, fontSize);
                distanceFromBeginning = distanceFromBeginning + l_right;
                drawClosingBracket(g, x_0 + distanceFromBeginning, y_0, fontSize, fontSize);
            } else {
                drawLogicalExpression(g, logExpr.getRight(), x_0 + distanceFromBeginning, y_0, fontSize);
            }

        } else if (logExpr.isImpl()) {

            if (logExpr.getLeft().isEquiv()) {
                drawOpeningBracket(g, x_0, y_0, fontSize, fontSize);
                distanceFromBeginning = getWidthOfBracket(fontSize);
                drawLogicalExpression(g, logExpr.getLeft(), x_0 + distanceFromBeginning, y_0, fontSize);
                distanceFromBeginning = distanceFromBeginning + l_left;
                drawClosingBracket(g, x_0 + distanceFromBeginning, y_0, fontSize, fontSize);
                distanceFromBeginning = distanceFromBeginning + getWidthOfBracket(fontSize);
            } else {
                drawLogicalExpression(g, logExpr.getLeft(), x_0 + distanceFromBeginning, y_0, fontSize);
                distanceFromBeginning = l_left;
            }

            drawSignImplication(g, x_0 + distanceFromBeginning, y_0, fontSize);
            distanceFromBeginning = distanceFromBeginning + getWidthOfSignAnd(fontSize);

            if (logExpr.getRight().isEquiv()) {
                drawOpeningBracket(g, x_0 + distanceFromBeginning, y_0, fontSize, fontSize);
                distanceFromBeginning = distanceFromBeginning + getWidthOfBracket(fontSize);
                drawLogicalExpression(g, logExpr.getRight(), x_0 + distanceFromBeginning, y_0, fontSize);
                distanceFromBeginning = distanceFromBeginning + l_right;
                drawClosingBracket(g, x_0 + distanceFromBeginning, y_0, fontSize, fontSize);
            } else {
                drawLogicalExpression(g, logExpr.getRight(), x_0 + distanceFromBeginning, y_0, fontSize);
            }

        } else if (logExpr.isEquiv()) {
            drawLogicalExpression(g, logExpr.getLeft(), x_0, y_0, fontSize);
            drawSignEquivalence(g, x_0 + getLengthOfLogicalExpression(g, logExpr.getLeft(), fontSize), y_0, fontSize);
            drawLogicalExpression(g, logExpr.getRight(), x_0 + l_left + getWidthOfSignEquivalence(fontSize), y_0, fontSize);
        }

    }

    /**
     * Methode zum Zeichnen einer Matrix m.
     */
    private void drawMatrix(Graphics g, Matrix matrix, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightMatrix = getHeightOfMatrixExpression(g, matrix, fontSize);
        int distanceFromOpeningBracket = 0;

        // Öffnende matrixklammer zeichnen.
        drawOpeningBracket(g, x_0, y_0, fontSize, heightMatrix);

        // Matrixeinträge zeichnen.
        int[] heightRow = new int[matrix.getRowNumber()];
        int[] heightCenterRow = new int[matrix.getRowNumber()];
        int[] lengthColumn = new int[matrix.getColumnNumber()];

        // Höhen einzelner Matrixzeilen bestimmen.
        for (int i = 0; i < heightRow.length; i++) {
            heightRow[i] = getHeightOfMatrixRow(g, matrix.getRow(i), fontSize);
        }

        // Höhen der Zentren einzelner Matrixzeilen bestimmen.
        for (int i = 0; i < heightRow.length; i++) {
            heightCenterRow[i] = getHeightOfCenterOfMatrixRow(g, matrix.getRow(i), fontSize);
        }

        // Breiten einzelner Matrixspalten bestimmen.
        for (int i = 0; i < lengthColumn.length; i++) {
            lengthColumn[i] = getLengthOfMatrixColumn(g, matrix.getColumn(i), fontSize);
        }

        /*
         Das Zeichen der Matrix geschieht von links nach rechts, von unten
         nach oben. Genauer: es werden Spalten nacheinander gezeichnet, von
         unten nach oben (dies ist leichter für die Berechnung der
         Anfangskoordinaten).
         */
        int currentHeight;
        for (int j = 0; j < lengthColumn.length; j++) {
            currentHeight = 0;
            for (int i = heightRow.length - 1; i >= 0; i--) {
                drawExpression(g, matrix.getEntry(i, j),
                        x_0 + getWidthOfBracket(fontSize) + distanceFromOpeningBracket
                        + (lengthColumn[j] - getLengthOfExpression(g, matrix.getEntry(i, j), fontSize)) / 2,
                        y_0 - currentHeight - (heightCenterRow[i] - getHeightOfCenterOfExpression(g, matrix.getEntry(i, j), fontSize)),
                        fontSize);
                currentHeight = currentHeight + heightRow[i] + getWidthOfMatrixSpace(fontSize);
            }
            distanceFromOpeningBracket = distanceFromOpeningBracket + lengthColumn[j];
            if (j < lengthColumn.length - 1) {
                distanceFromOpeningBracket = distanceFromOpeningBracket + getWidthOfMatrixSpace(fontSize);
            }
        }

        // Schließende matrixklammer zeichnen.
        drawClosingBracket(g, x_0 + getWidthOfBracket(fontSize) + distanceFromOpeningBracket, y_0, fontSize, heightMatrix);

    }

    /**
     * Methode zum Zeichnen einer Summe von MatrixExpressions.
     */
    private void drawMatrixBinaryOperationPlus(Graphics g, MatrixBinaryOperation matExpr, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightCenterLeft = getHeightOfCenterOfMatrixExpression(g, matExpr.getLeft(), fontSize);
        int heightCenterRight = getHeightOfCenterOfMatrixExpression(g, matExpr.getRight(), fontSize);

        drawMatrixExpression(g, matExpr.getLeft(), x_0,
                y_0 - Math.max(heightCenterRight - heightCenterLeft, 0),
                fontSize);
        drawSignPlus(g, x_0 + getLengthOfMatrixExpression(g, matExpr.getLeft(), fontSize),
                y_0 - (Math.max(heightCenterLeft, heightCenterRight) - fontSize / 2), fontSize);
        drawMatrixExpression(g, matExpr.getRight(),
                x_0 + getLengthOfMatrixExpression(g, matExpr.getLeft(), fontSize)
                + fontSize, y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                fontSize);

    }

    /**
     * Methode zum Zeichnen einer Differenz von MatrixExpressions.
     */
    private void drawMatrixBinaryOperationMinus(Graphics g, MatrixBinaryOperation matExpr, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightCenterLeft = getHeightOfCenterOfMatrixExpression(g, matExpr.getLeft(), fontSize);
        int heightCenterRight = getHeightOfCenterOfMatrixExpression(g, matExpr.getRight(), fontSize);
        int heightRight = getHeightOfMatrixExpression(g, matExpr.getRight(), fontSize);
        int lengthLeft = getLengthOfMatrixExpression(g, matExpr.getLeft(), fontSize);

        drawMatrixExpression(g, matExpr.getLeft(), x_0,
                y_0 - Math.max(heightCenterRight - heightCenterLeft, 0),
                fontSize);
        drawSignMinus(g, x_0 + lengthLeft, y_0 - (Math.max(heightCenterLeft, heightCenterRight) - fontSize / 2), fontSize);

        if (matExpr.getRight().isSum() || matExpr.getRight().isDifference()) {

            // Zeichnen von (right).
            drawOpeningBracket(g,
                    x_0 + lengthLeft + fontSize,
                    y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, heightRight);
            drawMatrixExpression(g, matExpr.getRight(),
                    x_0 + lengthLeft + (3 * fontSize) / 2, y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                    fontSize);
            drawClosingBracket(g, x_0 + lengthLeft
                    + getLengthOfMatrixExpression(g, matExpr.getRight(), fontSize)
                    + (3 * fontSize) / 2, y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, heightRight);

        } else {

            // zeichnen von right.
            drawMatrixExpression(g, matExpr.getRight(),
                    x_0 + lengthLeft + fontSize, y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                    fontSize);

        }

    }

    /**
     * Methode zum Zeichnen eines Produkts von MatrixExpressions.
     */
    private void drawMatrixBinaryOperationTimes(Graphics g, MatrixBinaryOperation matExpr, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightCenterLeft = getHeightOfCenterOfMatrixExpression(g, matExpr.getLeft(), fontSize);
        int heightCenterRight = getHeightOfCenterOfMatrixExpression(g, matExpr.getRight(), fontSize);
        int heightLeft = getHeightOfMatrixExpression(g, matExpr.getLeft(), fontSize);
        int heightRight = getHeightOfMatrixExpression(g, matExpr.getRight(), fontSize);

        if (matExpr.getLeft().isSum() || matExpr.getLeft().isDifference()) {

            // Fall: left benötigt eine Klammer.
            // Zeichnen von (left) * .
            drawOpeningBracket(g, x_0, y_0 - Math.max(heightCenterRight - heightCenterLeft, 0), fontSize, heightLeft);
            drawMatrixExpression(g, matExpr.getLeft(), x_0 + fontSize / 2, y_0 - Math.max(heightCenterRight - heightCenterLeft, 0), fontSize);
            drawClosingBracket(g, x_0 + getLengthOfMatrixExpression(g,
                    matExpr.getLeft(), fontSize) + fontSize / 2,
                    y_0 - Math.max(heightCenterRight - heightCenterLeft, 0), fontSize, heightLeft);
            drawSignMult(g, x_0 + 2 * getWidthOfBracket(fontSize) + getLengthOfMatrixExpression(g, matExpr.getLeft(), fontSize),
                    y_0 - (Math.max(heightCenterLeft, heightCenterRight) - fontSize / 2), fontSize);

            if (matExpr.getRight() instanceof MatrixBinaryOperation
                    && (((MatrixBinaryOperation) matExpr.getRight()).getType().equals(TypeMatrixBinary.PLUS)
                    || ((MatrixBinaryOperation) matExpr.getRight()).getType().equals(TypeMatrixBinary.MINUS))) {

                // Zeichnen von (right).
                drawOpeningBracket(g,
                        x_0 + getLengthOfMatrixExpression(g, matExpr.getLeft(), fontSize) + 2 * getWidthOfBracket(fontSize) + getWidthOfSignMult(g, fontSize),
                        y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, heightRight);
                drawMatrixExpression(g, matExpr.getRight(),
                        x_0 + getLengthOfMatrixExpression(g, matExpr.getLeft(), fontSize) + 3 * getWidthOfBracket(fontSize) + getWidthOfSignMult(g, fontSize),
                        y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                        fontSize);
                drawClosingBracket(g, x_0 + getLengthOfMatrixExpression(g, matExpr.getLeft(), fontSize)
                        + getLengthOfMatrixExpression(g, matExpr.getRight(), fontSize)
                        + 3 * getWidthOfBracket(fontSize) + getWidthOfSignMult(g, fontSize), y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, heightRight);

            } else {

                // Zeichnen von right.
                drawMatrixExpression(g, matExpr.getRight(),
                        x_0 + getLengthOfMatrixExpression(g, matExpr.getLeft(), fontSize)
                        + 2 * fontSize, y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                        fontSize);

            }

        } else {

            // Fall: left benötigt keine Klammer.
            int lengthLeft = getLengthOfMatrixExpression(g, matExpr.getLeft(), fontSize);
            int lengthMultSign = getWidthOfSignMult(g, fontSize);

            // Zeichnen von left * .
            drawMatrixExpression(g, matExpr.getLeft(), x_0, y_0 - Math.max(heightCenterRight - heightCenterLeft, 0), fontSize);
            drawSignMult(g, x_0 + lengthLeft, y_0 - (Math.max(heightCenterLeft, heightCenterRight) - fontSize / 2), fontSize);

            if (matExpr.getRight().isSum() || matExpr.getRight().isDifference()) {

                // Zeichnen von (right).
                drawOpeningBracket(g,
                        x_0 + lengthLeft + lengthMultSign,
                        y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, heightRight);
                drawMatrixExpression(g, matExpr.getRight(),
                        x_0 + lengthLeft + lengthMultSign
                        + getWidthOfBracket(fontSize), y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                        fontSize);
                drawClosingBracket(g, x_0 + lengthLeft + lengthMultSign
                        + getWidthOfBracket(fontSize)
                        + getLengthOfMatrixExpression(g, matExpr.getRight(), fontSize),
                        y_0 - Math.max(heightCenterLeft - heightCenterRight, 0), fontSize, heightRight);

            } else {

                // Zeichnen von right.
                drawMatrixExpression(g, matExpr.getRight(),
                        x_0 + lengthLeft + lengthMultSign,
                        y_0 - Math.max(heightCenterLeft - heightCenterRight, 0),
                        fontSize);

            }

        }

    }

    /**
     * Hauptmethode zum Zeichnen einer MatrixBinaryOperation matExpr.
     */
    private void drawMatrixBinaryOperation(Graphics g, MatrixBinaryOperation matExpr, int x_0, int y_0, int fontSize) {

        if (matExpr.getType().equals(TypeMatrixBinary.PLUS)) {
            drawMatrixBinaryOperationPlus(g, matExpr, x_0, y_0, fontSize);
        } else if (matExpr.getType().equals(TypeMatrixBinary.MINUS)) {
            drawMatrixBinaryOperationMinus(g, matExpr, x_0, y_0, fontSize);
        } else if (matExpr.getType().equals(TypeMatrixBinary.TIMES)) {
            drawMatrixBinaryOperationTimes(g, matExpr, x_0, y_0, fontSize);
        }

    }

    /**
     * Methode zum Zeichnen einer MatrixPower matExpr.
     */
    private void drawMatrixPower(Graphics g, MatrixPower matExpr, int x_0, int y_0, int fontSize) {

        int heightLeft = getHeightOfMatrixExpression(g, ((MatrixPower) matExpr).getLeft(), fontSize);
        int lengthLeft = getLengthOfMatrixExpression(g, ((MatrixPower) matExpr).getLeft(), fontSize);

        if (matExpr.getLeft() instanceof Matrix) {
            // Dann keine Klammern um die Basis herum zeichnen.
            drawMatrixExpression(g, ((MatrixPower) matExpr).getLeft(),
                    x_0, y_0, fontSize);
            drawExpression(g, ((MatrixPower) matExpr).getRight(),
                    x_0 + lengthLeft, y_0 - heightLeft, getSizeForSup(fontSize));
        } else {
            // Ansonsten Klammern um die Basis herum zeichnen.
            drawOpeningBracket(g, x_0, y_0, fontSize, heightLeft);
            drawMatrixExpression(g, ((MatrixPower) matExpr).getLeft(),
                    x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
            drawClosingBracket(g, x_0 + getWidthOfBracket(fontSize) + lengthLeft, y_0, fontSize, heightLeft);
            drawExpression(g, ((MatrixPower) matExpr).getRight(),
                    x_0 + 2 * getWidthOfBracket(fontSize) + lengthLeft, y_0 - heightLeft, getSizeForSup(fontSize));
        }

    }

    /**
     * Methode zum Zeichnen einer MatrixFunktion matExpr.
     */
    private void drawMatrixFunction(Graphics g, MatrixFunction matExpr, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightLeft = getHeightOfMatrixExpression(g, matExpr.getLeft(), fontSize);
        int heightCenterLeft = getHeightOfCenterOfMatrixExpression(g, matExpr.getLeft(), fontSize);
        int lengthName = g.getFontMetrics().stringWidth(matExpr.getName());

        setFont(g, fontSize);
        g.drawString(matExpr.getName(), x_0, y_0 - (heightCenterLeft - (2 * fontSize) / 5));

        if (matExpr.getLeft() instanceof Matrix && !(matExpr.getLeft().convertOneTimesOneMatrixToExpression() instanceof Expression)) {
            /*
             Man soll die Matrixklammern nur weglassen, wenn es eine Matrix, aber KEINE 
             1x1-Matrix ist (Letzteres wird nämlich als Expression dargestellt und dementsprechend
             werden Klammern benötigt!).
             */
            drawMatrixExpression(g, matExpr.getLeft(), x_0 + lengthName, y_0, fontSize);
        } else {
            drawOpeningBracket(g, x_0 + lengthName, y_0, fontSize, heightLeft);
            drawMatrixExpression(g, matExpr.getLeft(),
                    x_0 + lengthName + getWidthOfBracket(fontSize),
                    y_0, fontSize);
            drawClosingBracket(g,
                    x_0 + lengthName + getWidthOfBracket(fontSize)
                    + getLengthOfMatrixExpression(g, matExpr.getLeft(), fontSize),
                    y_0, fontSize, heightLeft);
        }

    }

    /**
     * Methode zum Zeichnen eines MatrixOperators matExpr.
     */
    private void drawMatrixOperator(Graphics g, MatrixOperator matOperator, int x_0, int y_0, int fontSize) {

        /*
         Unicodes: SIGMA = \u03A3, PI = \u03A0, INT = \u222B, sqrt = \u221A,
         LAPLACE (DELTA) = \u0394, DEL (PARTIAL) = \u2202.
         */
        if (matOperator.getType().equals(TypeMatrixOperator.diff)) {
            drawMatrixOperatorDiff(g, matOperator, x_0, y_0, fontSize);
        } else if (matOperator.getType().equals(TypeMatrixOperator.integral)) {
            drawMatrixOperatorInt(g, matOperator, x_0, y_0, fontSize);
        } else if (matOperator.getType().equals(TypeMatrixOperator.laplace)) {
            drawMatrixOperatorLaplace(g, matOperator, x_0, y_0, fontSize);
        } else if (matOperator.getType().equals(TypeMatrixOperator.prod)) {
            drawMatrixOperatorProd(g, matOperator, x_0, y_0, fontSize);
        } else if (matOperator.getType().equals(TypeMatrixOperator.sum)) {
            drawMatrixOperatorSum(g, matOperator, x_0, y_0, fontSize);
        } else {
            drawMatrixOperatorDefault(g, matOperator, x_0, y_0, fontSize);
        }

    }

    private void drawMatrixOperatorDiff(Graphics g, MatrixOperator matOperator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = matOperator.getParams();
        int lengthEnumeratorDifferentialOperator, lengthDifferentialOperator;
        int heightCenterOperand = getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
        int heightOperand = getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
        int lengthOperand = getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);

        if (params.length == 2) {

            lengthEnumeratorDifferentialOperator = getWidthOfSignPartial(g, fontSize);
            lengthDifferentialOperator = getWidthOfSignPartial(g, fontSize) + g.getFontMetrics().stringWidth((String) params[1]);

            // Im Folgenden sei x = var.
            //zeichnen von dx (im Nenner des Operators) 
            drawSignPartial(g, x_0, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2), fontSize);
            g.drawString((String) params[1], x_0 + lengthEnumeratorDifferentialOperator, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2));

            //Zeichnen des Bruchstrichs
            drawFractionLine(g, x_0, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2) - (3 * fontSize) / 2, fontSize, lengthDifferentialOperator);

            //Zeichnen von d (im Zähler des Operators)
            drawSignPartial(g, x_0 + (lengthDifferentialOperator - lengthEnumeratorDifferentialOperator) / 2,
                    y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2) - 2 * fontSize, fontSize);

            //Zeichnen des Operanden
            /*
             Die Addition von fontSize / 2 ist dazu da, damit nach dem
             Differentialoperator etwas Abstand zum nächsten Zeichen ist
             (nämlich fontSize / 2 Pixel).
             */
            lengthDifferentialOperator = lengthDifferentialOperator + fontSize / 2;
            drawOpeningBracket(g, x_0 + lengthDifferentialOperator, y_0 - Math.max(0, (3 * fontSize) / 2 - heightCenterOperand), fontSize, heightOperand);
            drawMatrixExpression(g, (MatrixExpression) params[0], x_0 + lengthDifferentialOperator + getWidthOfBracket(fontSize),
                    y_0 - Math.max(0, (3 * fontSize) / 2 - heightCenterOperand), fontSize);
            drawClosingBracket(g, x_0 + lengthDifferentialOperator + getWidthOfBracket(fontSize) + lengthOperand,
                    y_0 - Math.max(0, (3 * fontSize) / 2 - heightCenterOperand), fontSize, heightOperand);

        } else if (params.length == 3 && params[2] instanceof Integer) {

            int lengthPartialSign = getWidthOfSignPartial(g, fontSize);
            lengthDifferentialOperator = lengthPartialSign + g.getFontMetrics().stringWidth((String) params[1]) + getLengthOfConstant(g, new Constant((int) params[2]), getSizeForSup(fontSize));
            lengthEnumeratorDifferentialOperator = lengthPartialSign + getLengthOfConstant(g, new Constant((int) params[2]), getSizeForSup(fontSize));

            // Im Folgenden sei x = var, k = Ordnung der Ableitung.
            //zeichnen von dx^k (im Nenner des Operators) 
            drawSignPartial(g, x_0, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2 - getSizeForSup(fontSize)), fontSize);
            g.drawString((String) params[1], x_0 + lengthPartialSign, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2 - getSizeForSup(fontSize)));
            drawExpression(g, new Constant((int) params[2]), x_0 + lengthPartialSign + g.getFontMetrics().stringWidth((String) params[1]),
                    y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2 - getSizeForSup(fontSize)) - fontSize, getSizeForSup(fontSize));

            //Zeichnen des Bruchstrichs
            drawFractionLine(g, x_0, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2 - getSizeForSup(fontSize)) - (3 * fontSize) / 2 - getSizeForSup(fontSize),
                    fontSize, lengthDifferentialOperator);

            //Zeichnen von d^k (im Zähler des Operators)
            drawSignPartial(g, x_0 + (lengthDifferentialOperator - lengthEnumeratorDifferentialOperator) / 2,
                    y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2 - getSizeForSup(fontSize)) - 2 * fontSize - getSizeForSup(fontSize), fontSize);
            drawConstant(g, new Constant((int) params[2]), x_0 + (lengthDifferentialOperator - lengthEnumeratorDifferentialOperator) / 2 + lengthPartialSign,
                    y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2 - getSizeForSup(fontSize)) - 3 * fontSize - getSizeForSup(fontSize), getSizeForSup(fontSize));

            //Zeichnen des Operanden
            /*
             Die Addition von fontSize / 2 ist dazu da, damit nach dem
             Differentialoperator etwas Abstand zum nächsten Zeichen ist
             (nämlich fontSize / 2 Pixel).
             */
            lengthDifferentialOperator = lengthDifferentialOperator + fontSize / 2;
            drawOpeningBracket(g, x_0 + lengthDifferentialOperator, y_0 - Math.max(0, (3 * fontSize) / 2 + getSizeForSup(fontSize) - heightCenterOperand), fontSize, heightOperand);
            drawMatrixExpression(g, (MatrixExpression) params[0], x_0 + lengthDifferentialOperator + getWidthOfBracket(fontSize),
                    y_0 - Math.max(0, (3 * fontSize) / 2 + getSizeForSup(fontSize) - heightCenterOperand), fontSize);
            drawClosingBracket(g, x_0 + lengthDifferentialOperator + getWidthOfBracket(fontSize) + lengthOperand,
                    y_0 - Math.max(0, (3 * fontSize) / 2 + getSizeForSup(fontSize) - heightCenterOperand), fontSize, heightOperand);

        } else {

            int lengthPartialSign = getWidthOfSignPartial(g, fontSize);
            lengthDifferentialOperator = g.getFontMetrics().stringWidth((String) params[1]);
            for (int i = 2; i < params.length; i++) {
                lengthDifferentialOperator = lengthDifferentialOperator + g.getFontMetrics().stringWidth((String) params[i]);
            }
            lengthDifferentialOperator = lengthDifferentialOperator + (params.length - 1) * getWidthOfSignPartial(g, fontSize);

            lengthEnumeratorDifferentialOperator = lengthPartialSign + getLengthOfConstant(g, new Constant(params.length - 1), getSizeForSup(fontSize));

            /*
             Im Folgenden sei x = var, k = Anzahl der Variablen, nach denen
             differenziart wird, also = params.length - 1.
             */
            //zeichnen von dx_1 ... dx_k (im Nenner des Operators) 
            int l_current = 0;
            for (int i = 1; i < params.length; i++) {
                drawSignPartial(g, x_0 + l_current, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2), fontSize);
                g.drawString((String) params[i], x_0 + l_current + lengthPartialSign, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2));
                l_current = l_current + lengthPartialSign + g.getFontMetrics().stringWidth((String) params[i]);
            }

            //Zeichnen des Bruchstrichs
            drawFractionLine(g, x_0, y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2) - (3 * fontSize) / 2, fontSize, lengthDifferentialOperator);

            //Zeichnen von d^k (im Zähler des Operators)
            drawSignPartial(g, x_0 + (lengthDifferentialOperator - lengthEnumeratorDifferentialOperator) / 2,
                    y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2) - 2 * fontSize, fontSize);
            drawConstant(g, new Constant(params.length - 1), x_0 + (lengthDifferentialOperator - lengthEnumeratorDifferentialOperator) / 2 + lengthPartialSign,
                    y_0 - Math.max(0, heightCenterOperand - (3 * fontSize) / 2) - 3 * fontSize, getSizeForSup(fontSize));

            //Zeichnen des Operanden
            /*
             Die Addition von fontSize / 2 ist dazu da, damit nach dem
             Differentialoperator etwas Abstand zum nächsten Zeichen ist
             (nämlich fontSize / 2 Pixel).
             */
            lengthDifferentialOperator = lengthDifferentialOperator + fontSize / 2;
            drawOpeningBracket(g, x_0 + lengthDifferentialOperator, y_0 - Math.max(0, (3 * fontSize) / 2 - heightCenterOperand), fontSize, heightOperand);
            drawMatrixExpression(g, (MatrixExpression) params[0], x_0 + lengthDifferentialOperator + getWidthOfBracket(fontSize),
                    y_0 - Math.max(0, (3 * fontSize) / 2 - heightCenterOperand), fontSize);
            drawClosingBracket(g, x_0 + lengthDifferentialOperator + getWidthOfBracket(fontSize) + lengthOperand,
                    y_0 - Math.max(0, (3 * fontSize) / 2 - heightCenterOperand), fontSize, heightOperand);

        }

    }

    private void drawMatrixOperatorInt(Graphics g, MatrixOperator matOperator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = ((MatrixOperator) matOperator).getParams();
        int heightIntegrand = getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
        int heightCenterIntegrand = getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
        int lengthIntegrand = getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);

        if (params.length == 2) {

            //Integralzeichen
            drawSignIntegral(g, x_0, y_0, heightIntegrand);
            //Integrand
            drawMatrixExpression(g, (MatrixExpression) params[0], x_0 + getWidthOfSignIntegral(g, fontSize, heightIntegrand), y_0, fontSize);
            //dx
            setFont(g, fontSize);
            g.drawString(" d", x_0 + getWidthOfSignIntegral(g, fontSize, heightIntegrand) + lengthIntegrand,
                    y_0 - (heightCenterIntegrand - (2 * fontSize) / 5));
            setFont(g, fontSize);
            drawVariable(g, Variable.create((String) params[1]),
                    x_0 + getWidthOfSignIntegral(g, fontSize, heightIntegrand) + lengthIntegrand + g.getFontMetrics().stringWidth(" d"),
                    y_0 - (heightCenterIntegrand - (2 * fontSize) / 5), fontSize);

        } else {

            int heightLowerLimit = getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
            int lengthLowerLimit = getLengthOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
            int lengthUpperLimit = getLengthOfExpression(g, (Expression) params[3], getSizeForSup(fontSize));
            int lengthIntegralSign = getWidthOfSignIntegral(g, fontSize, heightIntegrand);
            int lengthOfIntegralSignWithLimits = Math.max(Math.max(lengthUpperLimit, lengthLowerLimit), lengthIntegralSign);

            //Untere Grenze
            drawExpression(g, (Expression) params[2], x_0 + (lengthOfIntegralSignWithLimits - lengthLowerLimit) / 2, y_0, getSizeForSup(fontSize));
            //Integralzeichen
            drawSignIntegral(g, x_0 + (lengthOfIntegralSignWithLimits - lengthIntegralSign) / 2, y_0 - heightLowerLimit, heightIntegrand);
            //Obere Grenze
            drawExpression(g, (Expression) params[3], x_0 + (lengthOfIntegralSignWithLimits - lengthUpperLimit) / 2, y_0 - heightLowerLimit - heightIntegrand, getSizeForSup(fontSize));
            //Integrand
            drawMatrixExpression(g, (MatrixExpression) params[0], x_0 + lengthOfIntegralSignWithLimits, y_0 - heightLowerLimit, fontSize);
            //dx
            setFont(g, fontSize);
            g.drawString(" d", x_0 + lengthOfIntegralSignWithLimits + lengthIntegrand,
                    y_0 - heightLowerLimit - (heightCenterIntegrand - (2 * fontSize) / 5));
            drawVariable(g, Variable.create((String) params[1]),
                    x_0 + lengthOfIntegralSignWithLimits + lengthIntegrand + g.getFontMetrics().stringWidth(" d"),
                    y_0 - heightLowerLimit - (heightCenterIntegrand - (2 * fontSize) / 5), fontSize);

        }

    }

    private void drawMatrixOperatorLaplace(Graphics g, MatrixOperator matOperator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightOperand = getHeightOfMatrixExpression(g, (MatrixExpression) matOperator.getParams()[0], fontSize);
        int heightCenterOperand = getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) matOperator.getParams()[0], fontSize);
        int lengthOperand = getLengthOfMatrixExpression(g, (MatrixExpression) matOperator.getParams()[0], fontSize);
        //Delta
        drawSignDelta(g, x_0, y_0 - (heightCenterOperand - (2 * fontSize) / 5), fontSize);
        //(
        drawOpeningBracket(g, x_0 + getWidthOfSignDelta(g, fontSize), y_0, fontSize, heightOperand);
        //Argument
        drawMatrixExpression(g, (MatrixExpression) matOperator.getParams()[0], x_0 + getWidthOfSignDelta(g, fontSize) + getWidthOfBracket(fontSize), y_0, fontSize);
        //)
        drawClosingBracket(g, x_0 + getWidthOfSignDelta(g, fontSize) + getWidthOfBracket(fontSize) + lengthOperand, y_0, fontSize, heightOperand);

    }

    private void drawMatrixOperatorProd(Graphics g, MatrixOperator matOperator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = ((MatrixOperator) matOperator).getParams();
        int heightFactor = getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
        int lengthFactor = getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
        int heightLowerLimit = getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        int heightCenterLowerLimit = getHeightOfCenterOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        int heightCenterVarEquals = getHeightOfCenterOfExpression(g, Variable.create((String) params[1]), getSizeForSup(fontSize));
        setFont(g, getSizeForSup(fontSize));
        int lengthVarEquals = g.getFontMetrics().stringWidth((String) params[1] + " = ");
        int lengthLowerLimit = getLengthOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        int lengthUpperLimit = getLengthOfExpression(g, (Expression) params[3], getSizeForSup(fontSize));
        int lengthPiSign = getWidthOfSignPi(g, fontSize, heightFactor);
        int lengthOfPiSignWithLimits = Math.max(Math.max(lengthUpperLimit, lengthVarEquals + lengthLowerLimit), lengthPiSign);

        //Untere Grenze mitsamt dem String "var="
        //Zunächst Schriftgröße verkleinern, da Indizes kleinerev Schriftgröße besitzen.
        setFont(g, getSizeForSup(fontSize));
        g.drawString((String) params[1] + " = ", x_0 + (lengthOfPiSignWithLimits - lengthLowerLimit - lengthVarEquals) / 2, y_0 - (heightCenterLowerLimit - heightCenterVarEquals));
        drawExpression(g, (Expression) params[2], x_0 + (lengthOfPiSignWithLimits - lengthLowerLimit - lengthVarEquals) / 2 + lengthVarEquals, y_0, getSizeForSup(fontSize));
        //Produktzeichen
        drawSignPi(g, x_0 + (lengthOfPiSignWithLimits - lengthPiSign) / 2, y_0 - heightLowerLimit, heightFactor);
        //Obere Grenze
        drawExpression(g, (Expression) params[3], x_0 + (lengthOfPiSignWithLimits - lengthUpperLimit) / 2, y_0 - heightLowerLimit - heightFactor, getSizeForSup(fontSize));
        //Produktfunktion
        if (((MatrixExpression) params[0]) instanceof MatrixBinaryOperation
                && ((((MatrixBinaryOperation) params[0]).getType()).equals(TypeMatrixBinary.PLUS)
                || (((MatrixBinaryOperation) params[0]).getType()).equals(TypeMatrixBinary.MINUS))) {
            drawOpeningBracket(g, x_0 + lengthOfPiSignWithLimits, y_0 - heightLowerLimit, fontSize, heightFactor);
            drawMatrixExpression(g, (MatrixExpression) params[0], x_0 + lengthOfPiSignWithLimits + getWidthOfBracket(fontSize), y_0 - heightLowerLimit, fontSize);
            drawClosingBracket(g, x_0 + lengthOfPiSignWithLimits + getWidthOfBracket(fontSize) + lengthFactor,
                    y_0 - heightLowerLimit, fontSize, heightFactor);
        } else {
            drawMatrixExpression(g, (MatrixExpression) params[0], x_0 + lengthOfPiSignWithLimits, y_0 - heightLowerLimit, fontSize);
        }

    }

    private void drawMatrixOperatorSum(Graphics g, MatrixOperator matOperator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = ((MatrixOperator) matOperator).getParams();
        int heightSummand = getHeightOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
        int lengthSummand = getLengthOfMatrixExpression(g, (MatrixExpression) params[0], fontSize);
        int heightLowerLimit = getHeightOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        int heightCenterLowerLimit = getHeightOfCenterOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        int heightCenterVarEquals = getHeightOfCenterOfExpression(g, Variable.create((String) params[1]), getSizeForSup(fontSize));
        setFont(g, getSizeForSup(fontSize));
        int lengthVarEquals = g.getFontMetrics().stringWidth((String) params[1] + " = ");
        int lengthLowerLimit = getLengthOfExpression(g, (Expression) params[2], getSizeForSup(fontSize));
        int lengthUpperLimit = getLengthOfExpression(g, (Expression) params[3], getSizeForSup(fontSize));
        int lengthSigmaSign = getWidthOfSignSigma(g, fontSize, heightSummand);
        int lengthOfSigmaSignWithLimits = Math.max(Math.max(lengthUpperLimit, lengthVarEquals + lengthLowerLimit), lengthSigmaSign);

        //Untere Grenze mitsamt dem String "var="
        //Zunächst Schriftgröße verkleinern, da Indizes kleinerev Schriftgröße besitzen.
        setFont(g, getSizeForSup(fontSize));
        g.drawString((String) params[1] + " = ", x_0 + (lengthOfSigmaSignWithLimits - lengthLowerLimit - lengthVarEquals) / 2, y_0 - (heightCenterLowerLimit - heightCenterVarEquals));
        drawExpression(g, (Expression) params[2], x_0 + (lengthOfSigmaSignWithLimits - lengthLowerLimit - lengthVarEquals) / 2 + lengthVarEquals, y_0, getSizeForSup(fontSize));
        //Produktzeichen
        drawSignSigma(g, x_0 + (lengthOfSigmaSignWithLimits - lengthSigmaSign) / 2, y_0 - heightLowerLimit, heightSummand);
        //Obere Grenze
        drawExpression(g, (Expression) params[3], x_0 + (lengthOfSigmaSignWithLimits - lengthUpperLimit) / 2, y_0 - heightLowerLimit - heightSummand, getSizeForSup(fontSize));
        //Produktfunktion
        if (((MatrixExpression) params[0]) instanceof MatrixBinaryOperation
                && ((((MatrixBinaryOperation) params[0]).getType()).equals(TypeMatrixBinary.PLUS)
                || (((MatrixBinaryOperation) params[0]).getType()).equals(TypeMatrixBinary.MINUS))) {
            drawOpeningBracket(g, x_0 + lengthOfSigmaSignWithLimits, y_0 - heightLowerLimit, fontSize, heightSummand);
            drawMatrixExpression(g, (MatrixExpression) params[0], x_0 + lengthOfSigmaSignWithLimits + getWidthOfBracket(fontSize), y_0 - heightLowerLimit, fontSize);
            drawClosingBracket(g, x_0 + lengthOfSigmaSignWithLimits + getWidthOfBracket(fontSize) + lengthSummand,
                    y_0 - heightLowerLimit, fontSize, heightSummand);
        } else {
            drawMatrixExpression(g, (MatrixExpression) params[0], x_0 + lengthOfSigmaSignWithLimits, y_0 - heightLowerLimit, fontSize);
        }

    }

    /**
     * Zeichnen aller anderen Operatoren, welche keine besonderen Symbole
     * gebrauchen (also div, gcd, lcm, mod, ...).
     */
    private void drawMatrixOperatorDefault(Graphics g, MatrixOperator matOperator, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        int heightOperator = getHeightOfMatrixExpression(g, matOperator, fontSize);
        int heightCenterOperator = getHeightOfCenterOfMatrixExpression(g, matOperator, fontSize);
        Object[] left = ((MatrixOperator) matOperator).getParams();
        String name = MatrixOperator.getNameFromType(((MatrixOperator) matOperator).getType());

        g.drawString(name, x_0, y_0 - (heightCenterOperator - (2 * fontSize) / 5));
        drawOpeningBracket(g, x_0 + g.getFontMetrics().stringWidth(name), y_0, fontSize, heightOperator);

        int distanceFromOpeningBracket = 0;

        for (int i = 0; i < left.length; i++) {

            if (left[i] instanceof Expression) {
                drawExpression(g, (Expression) left[i],
                        x_0 + g.getFontMetrics().stringWidth(name)
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterOperator - getHeightOfCenterOfExpression(g, (Expression) left[i], fontSize)), fontSize);
                distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, (Expression) left[i], fontSize);
            } else if (left[i] instanceof MatrixExpression) {
                drawMatrixExpression(g, (MatrixExpression) left[i],
                        x_0 + g.getFontMetrics().stringWidth(name)
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterOperator - getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) left[i], fontSize)), fontSize);
                distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfMatrixExpression(g, (MatrixExpression) left[i], fontSize);
            } else if (left[i] instanceof String) {
                setFont(g, fontSize);
                g.drawString((String) left[i], x_0 + g.getFontMetrics().stringWidth(name)
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterOperator - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth((String) left[i]);
            } else if (left[i] instanceof Integer) {
                setFont(g, fontSize);
                g.drawString(String.valueOf((Integer) left[i]), x_0 + g.getFontMetrics().stringWidth(name)
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterOperator - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(String.valueOf((Integer) left[i]));
            }

            if (i < left.length - 1) {
                setFont(g, fontSize);
                g.drawString(", ", x_0 + g.getFontMetrics().stringWidth(name)
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterOperator - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");
            }

        }

        setFont(g, fontSize);
        drawClosingBracket(g,
                x_0 + g.getFontMetrics().stringWidth(name)
                + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0, fontSize, heightOperator);

    }

    /**
     * Hauptmethode zum Zeichnen einer MatrixExpression matExpr.
     */
    private void drawMatrixExpression(Graphics g, MatrixExpression matExpr, int x_0, int y_0, int fontSize) {

        // Im Vorfeld prüfen, ob es sich um eine 1x1-Matrix handelt. Falls ja, dann wie eine Instanz von Expression behandeln.
        Object matExprConverted = matExpr.convertOneTimesOneMatrixToExpression();
        if (matExprConverted instanceof Expression) {
            Expression expr = (Expression) matExprConverted;
            if (expr.isSum() || expr.isDifference() || (expr instanceof Constant && expr.isNegative())) {
                int h = getHeightOfExpression(g, expr, fontSize);
                int l = getLengthOfExpression(g, expr, fontSize);
                setFont(g, fontSize);
                drawOpeningBracket(g, x_0, y_0, fontSize, h);
                drawExpression(g, (Expression) matExprConverted, x_0 + getWidthOfBracket(fontSize), y_0, fontSize);
                drawClosingBracket(g, x_0 + getWidthOfBracket(fontSize) + l, y_0, fontSize, h);
            } else {
                drawExpression(g, (Expression) matExprConverted, x_0, y_0, fontSize);
            }
            return;
        }

        setFont(g, fontSize);

        if (matExpr instanceof Matrix) {
            drawMatrix(g, (Matrix) matExpr, x_0, y_0, fontSize);
        } else if (matExpr instanceof MatrixBinaryOperation) {
            drawMatrixBinaryOperation(g, (MatrixBinaryOperation) matExpr, x_0, y_0, fontSize);
        } else if (matExpr instanceof MatrixPower) {
            drawMatrixPower(g, (MatrixPower) matExpr, x_0, y_0, fontSize);
        } else if (matExpr instanceof MatrixFunction) {
            drawMatrixFunction(g, (MatrixFunction) matExpr, x_0, y_0, fontSize);
        } else if (matExpr instanceof MatrixOperator) {
            drawMatrixOperator(g, (MatrixOperator) matExpr, x_0, y_0, fontSize);
        }

    }

    /**
     * Hauptmethode zum Zeichnen eines Befehls.
     */
    private void drawCommand(Graphics g, Command c, int x_0, int y_0, int fontSize) {

        if (c.getTypeCommand().equals(TypeCommand.def)) {
            drawCommandDef(g, c, x_0, y_0, fontSize);
        } else if (c.getTypeCommand().equals(TypeCommand.latex)) {
            drawCommandLatex(g, c, x_0, y_0, fontSize);
        } else if (c.getTypeCommand().equals(TypeCommand.plotimplicit)) {
            drawCommandPlotImplicit(g, c, x_0, y_0, fontSize);
        } else if (c.getTypeCommand().equals(TypeCommand.plotcurve)) {
            drawCommandPlotCurve(g, c, x_0, y_0, fontSize);
        } else if (c.getTypeCommand().equals(TypeCommand.solve)) {
            drawCommandSolve(g, c, x_0, y_0, fontSize);
        } else if (c.getTypeCommand().equals(TypeCommand.tangent)) {
            drawCommandTangent(g, c, x_0, y_0, fontSize);
        } else {
            drawCommandDefault(g, c, x_0, y_0, fontSize);
        }

    }

    private void drawCommandDef(Graphics g, Command c, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = c.getParams();

        int heightCommand = getHeightOfCommand(g, c, fontSize);
        int heightCenterCommand = getHeightOfCenterOfCommand(g, c, fontSize);
        setFont(g, fontSize);
        int lengthName = g.getFontMetrics().stringWidth(c.getName());

        g.drawString(c.getName(), x_0, y_0 - (heightCenterCommand - (2 * fontSize) / 5));
        drawOpeningBracket(g, x_0 + lengthName, y_0, fontSize, heightCommand);

        if (params.length == 2) {

            // Fall: Variablendeklaration.
            int lengthVar = g.getFontMetrics().stringWidth((String) params[0]);
            g.drawString((String) params[0],
                    x_0 + lengthName + getWidthOfBracket(fontSize),
                    y_0 - (heightCenterCommand - (2 * fontSize) / 5));
            drawSignEquals(g, x_0 + lengthName + getWidthOfBracket(fontSize) + lengthVar,
                    y_0 - (heightCenterCommand - (2 * fontSize) / 5),
                    fontSize);
            drawExpression(g, (Expression) params[1],
                    x_0 + lengthName + getWidthOfBracket(fontSize) + lengthVar + getWidthOfSignEquals(g, fontSize),
                    y_0, fontSize);
            drawClosingBracket(g,
                    x_0 + lengthName + getWidthOfBracket(fontSize) + lengthVar + getWidthOfSignEquals(g, fontSize)
                    + getLengthOfExpression(g, (Expression) params[1], fontSize),
                    y_0, fontSize, heightCommand);

        } else {

            // Fall: Funktionsdeklaration.
            String functionName = (String) params[0];

            // Ausgabe an den Benutzer.
            String[] functionArguments = new String[params.length - 2];
            Expression[] varsForOutput = new Expression[functionArguments.length];

            for (int i = 0; i < params.length - 2; i++) {
                functionArguments[i] = (String) params[i + 1];
                /*
                 Die Variablennamen in f_arguments haben alle "_ABSTRACT" als
                 Anhängsel. Dieses wird nun beseitigt, um die Originalnamen
                 wiederzubekommen. Die Variablen mit den Originalnamen werden
                 im Array vars_for_output abgespechert.
                 */
                varsForOutput[i] = Variable.create(functionArguments[i].substring(0, functionArguments[i].indexOf("_ABSTRACT")));
            }

            Expression function = (Expression) params[params.length - 1];

            for (int i = 0; i < params.length - 2; i++) {
                function = function.replaceVariable(functionArguments[i], varsForOutput[i]);
            }

            /*
             Die Originalnamen der Variablen und die
             Originalfunktionsvorschrift wurden rekonstruiert. Jetzt können
             sie gezeichnet werden.
             */
            int distanceFromOpeningBracket = 0;

            // Funktionsnamen zeichnen.
            g.drawString(functionName, x_0 + lengthName + getWidthOfBracket(fontSize),
                    y_0 - (heightCenterCommand - (2 * fontSize) / 5));
            distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(functionName);
            // Öffnende Klammer für die Funktion zeichnen.
            drawOpeningBracket(g, x_0 + lengthName + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - (2 * fontSize) / 5), fontSize, fontSize);
            distanceFromOpeningBracket = distanceFromOpeningBracket + getWidthOfBracket(fontSize);

            for (int i = 0; i < varsForOutput.length; i++) {

                g.drawString(((Variable) varsForOutput[i]).getName(),
                        x_0 + lengthName + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterCommand - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(((Variable) varsForOutput[i]).getName());

                // Einzelne Funktionsvariablen mit einem Komma abtrennen.
                if (i < varsForOutput.length - 1) {
                    g.drawString(", ",
                            x_0 + lengthName + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                            y_0 - (heightCenterCommand - (2 * fontSize) / 5));
                    distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");
                }

            }

            // Schließende Klammer für die Funktion zeichnen.
            drawClosingBracket(g,
                    x_0 + lengthName + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - (2 * fontSize) / 5), fontSize, fontSize);
            distanceFromOpeningBracket = distanceFromOpeningBracket + getWidthOfBracket(fontSize);
            // "=" zeichnen.
            drawSignEquals(g, x_0 + lengthName + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - (2 * fontSize) / 5), fontSize);
            distanceFromOpeningBracket = distanceFromOpeningBracket + getWidthOfSignEquals(g, fontSize);

            // Zeichnen der Funktionsvorschrift.
            drawExpression(g, function, x_0 + lengthName + getWidthOfBracket(fontSize) + distanceFromOpeningBracket, y_0, fontSize);
            distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, function, fontSize);

            // Schließende Klammer für den gesamten Befehl.
            drawClosingBracket(g,
                    x_0 + lengthName + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0, fontSize, heightCommand);

        }

    }

    private void drawCommandLatex(Graphics g, Command c, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = c.getParams();

        int heightCommand = getHeightOfCommand(g, c, fontSize);
        int heightCenterCommand = getHeightOfCenterOfCommand(g, c, fontSize);
        setFont(g, fontSize);
        int lengthName = g.getFontMetrics().stringWidth(c.getName());

        g.drawString(c.getName(), x_0, y_0 - (heightCenterCommand - (2 * fontSize) / 5));
        drawOpeningBracket(g, x_0 + lengthName, y_0, fontSize, heightCommand);

        int distanceFromOpeningBracket = 0;

        for (int i = 0; i < params.length; i++) {

            drawExpression(g, (Expression) params[i],
                    x_0 + lengthName
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - getHeightOfCenterOfExpression(g, (Expression) params[i], fontSize)), fontSize);
            distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, (Expression) params[i], fontSize);

            if (i < params.length - 1) {
                drawSignEquals(g, x_0 + lengthName
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterCommand - (2 * fontSize) / 5),
                        fontSize);
                distanceFromOpeningBracket = distanceFromOpeningBracket + getWidthOfSignEquals(g, fontSize);
            }

        }

        drawClosingBracket(g,
                x_0 + lengthName
                + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0, fontSize, heightCommand);

    }

    private void drawCommandPlotImplicit(Graphics g, Command c, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = c.getParams();

        int heightCommand = getHeightOfCommand(g, c, fontSize);
        int heightCenterCommand = getHeightOfCenterOfCommand(g, c, fontSize);
        setFont(g, fontSize);
        int lengthName = g.getFontMetrics().stringWidth(c.getName());

        g.drawString(c.getName(), x_0, y_0 - (heightCenterCommand - (2 * fontSize) / 5));
        drawOpeningBracket(g, x_0 + lengthName, y_0, fontSize, heightCommand);

        int distanceFromOpeningBracket = 0;

        for (int i = 0; i < params.length; i++) {

            drawExpression(g, (Expression) params[i],
                    x_0 + lengthName
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - getHeightOfCenterOfExpression(g, (Expression) params[i], fontSize)), fontSize);
            distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, (Expression) params[i], fontSize);

            if (i < params.length - 1) {
                setFont(g, fontSize);
                if (i == 0) {
                    // Gleichheitszeichen zeichnen.
                    drawSignEquals(g, x_0 + lengthName
                            + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                            y_0 - (heightCenterCommand - (2 * fontSize) / 5), fontSize);
                    distanceFromOpeningBracket = distanceFromOpeningBracket + getWidthOfSignEquals(g, fontSize);
                } else {
                    // Komma zeichnen.
                    g.drawString(", ", x_0 + lengthName
                            + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                            y_0 - (heightCenterCommand - (2 * fontSize) / 5));
                    distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");
                }
            }

        }

        drawClosingBracket(g,
                x_0 + lengthName
                + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0, fontSize, heightCommand);

    }

    private void drawCommandPlotCurve(Graphics g, Command c, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = c.getParams();

        int heightCommand = getHeightOfCommand(g, c, fontSize);
        int heightCenterCommand = getHeightOfCenterOfCommand(g, c, fontSize);
        setFont(g, fontSize);
        int lengthName = g.getFontMetrics().stringWidth(c.getName());

        g.drawString(c.getName(), x_0, y_0 - (heightCenterCommand - (2 * fontSize) / 5));
        drawOpeningBracket(g, x_0 + lengthName, y_0, fontSize, heightCommand);

        int distanceFromOpeningBracket = 0;

        for (int i = 0; i < params.length; i++) {

            // Öffnende Klammer für die Komponenten der Kurve.
            if (i == 0) {
                drawOpeningBracket(g, x_0 + lengthName + getWidthOfBracket(fontSize), y_0, fontSize, heightCommand);
                distanceFromOpeningBracket = distanceFromOpeningBracket + getWidthOfBracket(fontSize);
            }

            drawExpression(g, (Expression) params[i],
                    x_0 + lengthName
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - getHeightOfCenterOfExpression(g, (Expression) params[i], fontSize)), fontSize);
            distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, (Expression) params[i], fontSize);

            // Schließende Klammer für die Komponenten der Kurve.
            if (i == params.length - 3) {
                drawClosingBracket(g, x_0 + lengthName
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0, fontSize, heightCommand);
                distanceFromOpeningBracket = distanceFromOpeningBracket + getWidthOfBracket(fontSize);
            }

            if (i < params.length - 1) {
                setFont(g, fontSize);
                g.drawString(", ", x_0 + lengthName
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterCommand - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");
            }

        }

        drawClosingBracket(g,
                x_0 + lengthName
                + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0, fontSize, heightCommand);

    }

    private void drawCommandSolve(Graphics g, Command c, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = c.getParams();

        int heightCenterLeftSideOfEquation = getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize);
        int heightCenterRightSideOfEquation = getHeightOfCenterOfExpression(g, (Expression) params[1], fontSize);
        int heightCommand = getHeightOfCommand(g, c, fontSize);
        int heightCenterCommand = getHeightOfCenterOfCommand(g, c, fontSize);
        setFont(g, fontSize);
        int lengthName = g.getFontMetrics().stringWidth(c.getName());

        int distanceFromOpeningBracket = 0;
        setFont(g, fontSize);
        g.drawString(c.getName(), x_0, y_0 - (heightCenterCommand - (2 * fontSize) / 5));
        drawOpeningBracket(g, x_0 + lengthName, y_0, fontSize, heightCommand);

        //1. Ausdruck zeichnen.
        drawExpression(g, (Expression) params[0],
                x_0 + lengthName
                + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0 - (heightCenterCommand - heightCenterLeftSideOfEquation), fontSize);
        distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, (Expression) params[0], fontSize);
        //"=" zeichnen.
        setFont(g, fontSize);
        drawSignEquals(g, x_0 + lengthName
                + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0 - (heightCenterCommand - (2 * fontSize) / 5), fontSize);
        distanceFromOpeningBracket = distanceFromOpeningBracket + getWidthOfSignEquals(g, fontSize);
        //2. Ausdruck zeichnen.
        drawExpression(g, (Expression) params[1],
                x_0 + lengthName
                + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0 - (heightCenterCommand - heightCenterRightSideOfEquation), fontSize);
        distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, (Expression) params[1], fontSize);

        if (params.length == 3) {

            setFont(g, fontSize);
            g.drawString(", ", x_0 + lengthName
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - (2 * fontSize) / 5));
            distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");
            //3. Ausdruck zeichnen.
            g.drawString((String) params[2], x_0 + lengthName
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - (2 * fontSize) / 5));
            distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth((String) params[2]);

        } else if (params.length >= 4) {

            int heightCenterLeftLimit = getHeightOfCenterOfExpression(g, (Expression) params[2], fontSize);
            int heightCenterRightLimit = getHeightOfCenterOfExpression(g, (Expression) params[3], fontSize);

            //Komma zeichnen.
            setFont(g, fontSize);
            g.drawString(", ", x_0 + lengthName
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - (2 * fontSize) / 5));
            distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");
            //3. Ausdruck zeichnen.
            drawExpression(g, (Expression) params[2],
                    x_0 + lengthName
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - heightCenterLeftLimit), fontSize);
            distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, (Expression) params[2], fontSize);
            //Komma zeichnen.
            setFont(g, fontSize);
            g.drawString(", ", x_0 + lengthName
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - (2 * fontSize) / 5));
            distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");
            //4. Ausdruck zeichnen.
            drawExpression(g, (Expression) params[3],
                    x_0 + lengthName
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - heightCenterRightLimit), fontSize);
            distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, (Expression) params[3], fontSize);

            if (params.length == 5) {

                //Komma zeichnen.
                setFont(g, fontSize);
                g.drawString(", ", x_0 + lengthName
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterCommand - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");
                //5. Ausdruck zeichnen.
                g.drawString(String.valueOf((Integer) params[4]),
                        x_0 + lengthName
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterCommand - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(String.valueOf((Integer) params[4]));

            }

        }

        drawClosingBracket(g,
                x_0 + lengthName
                + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0, fontSize, heightCommand);

    }

    private void drawCommandTangent(Graphics g, Command c, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = c.getParams();

        int heightCommand = getHeightOfCommand(g, c, fontSize);
        int heightCenterCommand = getHeightOfCenterOfCommand(g, c, fontSize);
        setFont(g, fontSize);
        int lengthName = g.getFontMetrics().stringWidth(c.getName());

        g.drawString(c.getName(), x_0, y_0 - (heightCenterCommand - (2 * fontSize) / 5));
        drawOpeningBracket(g, x_0 + lengthName, y_0, fontSize, heightCommand);

        int distanceFromOpeningBracket = 0;
        int heightCenterOfCurrentParameter = getHeightOfCenterOfExpression(g, (Expression) params[0], fontSize);

        // Funktionsgleichung zeichnen.
        drawExpression(g, (Expression) params[0], x_0 + lengthName + getWidthOfBracket(fontSize),
                y_0 - (heightCenterCommand - heightCenterOfCurrentParameter), fontSize);
        distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, (Expression) params[0], fontSize);
        // Komma zeichnen.
        setFont(g, fontSize);
        g.drawString(", ", x_0 + lengthName + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0 - (heightCenterCommand - (2 * fontSize) / 5));
        distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");

        /*
         Variablennamen mit den dazugehörigen Werten auslesen (params[1] ist
         eine HashMap mit Keys = Variablennamen und Values = Variablenwerten).
         */
        HashMap<String, Expression> varsWithValues = (HashMap<String, Expression>) params[1];
        String varCurrent;
        Expression valueOfVarCurrent;

        for (Iterator iter = varsWithValues.keySet().iterator(); iter.hasNext();) {

            setFont(g, fontSize);
            varCurrent = (String) iter.next();
            valueOfVarCurrent = (Expression) varsWithValues.get(varCurrent);

            // Variablennamen zeichnen.
            g.drawString(varCurrent, x_0 + lengthName
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - (2 * fontSize) / 5));
            distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(varCurrent);
            // "="-Zeichen zeichnen.
            drawSignEquals(g, x_0 + lengthName
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - (2 * fontSize) / 5), fontSize);
            distanceFromOpeningBracket = distanceFromOpeningBracket + getWidthOfSignEquals(g, fontSize);
            // Variablenwert zeichnen.
            drawExpression(g, valueOfVarCurrent,
                    x_0 + lengthName
                    + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                    y_0 - (heightCenterCommand - getHeightOfCenterOfExpression(g, valueOfVarCurrent, fontSize)), fontSize);
            distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, valueOfVarCurrent, fontSize);

            if (iter.hasNext()) {
                setFont(g, fontSize);
                // Koma zeichnen.
                g.drawString(", ", x_0 + lengthName
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterCommand - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");
            }

        }

        drawClosingBracket(g,
                x_0 + lengthName
                + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0, fontSize, heightCommand);

    }

    private void drawCommandDefault(Graphics g, Command c, int x_0, int y_0, int fontSize) {

        setFont(g, fontSize);

        Object[] params = c.getParams();

        int heightCommand = getHeightOfCommand(g, c, fontSize);
        int heightCenterCommand = getHeightOfCenterOfCommand(g, c, fontSize);
        setFont(g, fontSize);
        int lengthName = g.getFontMetrics().stringWidth(c.getName());

        g.drawString(c.getName(), x_0, y_0 - (heightCenterCommand - (2 * fontSize) / 5));
        drawOpeningBracket(g, x_0 + lengthName, y_0, fontSize, heightCommand);

        int distanceFromOpeningBracket = 0;

        for (int i = 0; i < params.length; i++) {

            // Parameter zeichnen.
            if (params[i] instanceof Expression) {

                drawExpression(g, (Expression) params[i],
                        x_0 + lengthName
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterCommand - getHeightOfCenterOfExpression(g, (Expression) params[i], fontSize)), fontSize);
                distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfExpression(g, (Expression) params[i], fontSize);

            } else if (params[i] instanceof LogicalExpression) {

                drawLogicalExpression(g, (LogicalExpression) params[i],
                        x_0 + lengthName
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterCommand - getHeightOfCenterOfLogicalExpression(g, (LogicalExpression) params[i], fontSize)), fontSize);
                distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfLogicalExpression(g, (LogicalExpression) params[i], fontSize);

            } else if (params[i] instanceof MatrixExpression) {

                drawMatrixExpression(g, (MatrixExpression) params[i],
                        x_0 + lengthName
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterCommand - getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) params[i], fontSize)), fontSize);
                distanceFromOpeningBracket = distanceFromOpeningBracket + getLengthOfMatrixExpression(g, (MatrixExpression) params[i], fontSize);

            } else if (params[i] instanceof String) {

                setFont(g, fontSize);
                g.drawString((String) params[i], x_0 + lengthName
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterCommand - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth((String) params[i]);

            } else if (params[i] instanceof Integer) {

                setFont(g, fontSize);
                g.drawString(String.valueOf((Integer) params[i]), x_0 + lengthName
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterCommand - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(String.valueOf((Integer) params[i]));

            }

            if (i < params.length - 1) {
                setFont(g, fontSize);
                // Koma zeichnen.
                g.drawString(", ", x_0 + lengthName
                        + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                        y_0 - (heightCenterCommand - (2 * fontSize) / 5));
                distanceFromOpeningBracket = distanceFromOpeningBracket + g.getFontMetrics().stringWidth(", ");
            }

        }

        drawClosingBracket(g,
                x_0 + lengthName
                + getWidthOfBracket(fontSize) + distanceFromOpeningBracket,
                y_0, fontSize, heightCommand);

    }

    /**
     * Hauptmethode zum Zeichnen eines Textes.
     */
    private void drawText(Graphics g, String s, int x_0, int y_0, int fontSize) {
        setFont(g, fontSize);
        g.drawString(s, x_0, y_0);
    }

    /**
     * Hauptmethode zum Zeichnen einer gemischten Ausgabe. Die Objekte in output
     * sollen nur Instanzen von Expression, LogicalExpression, Command und
     * String sein.
     */
    private void drawOutput(Graphics g, int x_0, int y_0, int fontSize, Object... output) {

        int distanceFromBeginningOfOutput = 0;
        int heightCenterOutput = getHeightOfCenterOfOutput(g, fontSize, output);
        int heightCenterOfCurrentOutputParameter;
        /*
         Dieser Parameter besagt, dass die nächste Expression von Klammern
         umgeben ist. Nach einer Anwendung davon wird dieser Parameter
         umgehend wieder auf false gesetzt (s. u.).
         */
        boolean nextExpressionIsSurroundedByBrackets = false;

        for (Object out : output) {

            if (out instanceof Expression) {
                heightCenterOfCurrentOutputParameter = getHeightOfCenterOfExpression(g, (Expression) out, fontSize);
                if (nextExpressionIsSurroundedByBrackets) {
                    drawExpressionSurroundedByBrackets(g, (Expression) out, x_0 + distanceFromBeginningOfOutput, y_0 - (heightCenterOutput - heightCenterOfCurrentOutputParameter), fontSize);
                    distanceFromBeginningOfOutput = distanceFromBeginningOfOutput + 2 * getWidthOfBracket(fontSize) + getLengthOfExpression(g, (Expression) out, fontSize);
                    nextExpressionIsSurroundedByBrackets = false;
                } else {
                    drawExpression(g, (Expression) out, x_0 + distanceFromBeginningOfOutput, y_0 - (heightCenterOutput - heightCenterOfCurrentOutputParameter), fontSize);
                    distanceFromBeginningOfOutput = distanceFromBeginningOfOutput + getLengthOfExpression(g, (Expression) out, fontSize);
                }
            } else if (out instanceof LogicalExpression) {
                heightCenterOfCurrentOutputParameter = getHeightOfCenterOfLogicalExpression(g, (LogicalExpression) out, fontSize);
                drawLogicalExpression(g, (LogicalExpression) out, x_0 + distanceFromBeginningOfOutput, y_0 - (heightCenterOutput - heightCenterOfCurrentOutputParameter), fontSize);
                distanceFromBeginningOfOutput = distanceFromBeginningOfOutput + getLengthOfLogicalExpression(g, (LogicalExpression) out, fontSize);
            } else if (out instanceof MatrixExpression) {
                heightCenterOfCurrentOutputParameter = getHeightOfCenterOfMatrixExpression(g, (MatrixExpression) out, fontSize);
                drawMatrixExpression(g, (MatrixExpression) out, x_0 + distanceFromBeginningOfOutput, y_0 - (heightCenterOutput - heightCenterOfCurrentOutputParameter), fontSize);
                distanceFromBeginningOfOutput = distanceFromBeginningOfOutput + getLengthOfMatrixExpression(g, (MatrixExpression) out, fontSize);
            } else if (out instanceof Command) {
                heightCenterOfCurrentOutputParameter = getHeightOfCenterOfCommand(g, (Command) out, fontSize);
                drawCommand(g, (Command) out, x_0 + distanceFromBeginningOfOutput, y_0 - (heightCenterOutput - heightCenterOfCurrentOutputParameter), fontSize);
                distanceFromBeginningOfOutput = distanceFromBeginningOfOutput + getLengthOfCommand(g, (Command) out, fontSize);
            } else if (out instanceof String) {
                setFont(g, fontSize);
                g.drawString((String) out, x_0 + distanceFromBeginningOfOutput, y_0 - (heightCenterOutput - (2 * fontSize) / 5));
                distanceFromBeginningOfOutput = distanceFromBeginningOfOutput + g.getFontMetrics().stringWidth((String) out);
            } else if (out instanceof TypeBracket) {
                nextExpressionIsSurroundedByBrackets = true;
            }

        }

    }

    public void drawFormula() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        g.setColor(this.backgroundColor);
        g.fillRect(0, 0, this.width, this.height);
        g.setColor(Color.black);
        setFont(g, fontSize);
        // Pufferrahmen mit Breite fontsize/2 lassen!
        if (type.equals(TypeGraphicFormula.EXPRESSION)) {
            drawExpression(g, this.expr, this.fontSize / 2, this.height - this.fontSize / 2, this.fontSize);
        } else if (type.equals(TypeGraphicFormula.LOGICAL_EXPRESSION)) {
            drawLogicalExpression(g, this.logExpr, this.fontSize / 2, this.height - this.fontSize / 2, this.fontSize);
        } else if (type.equals(TypeGraphicFormula.MATRIX_EXPRESSION)) {
            drawMatrixExpression(g, this.matExpr, this.fontSize / 2, this.height - this.fontSize / 2, this.fontSize);
        } else if (type.equals(TypeGraphicFormula.COMMAND)) {
            drawCommand(g, c, this.fontSize / 2, this.height - this.fontSize / 2, this.fontSize);
        } else if (type.equals(TypeGraphicFormula.TEXT)) {
            drawText(g, this.t, this.fontSize / 2, this.height - this.fontSize / 2, fontSize);
        } else if (type.equals(TypeGraphicFormula.OUTPUT)) {
            drawOutput(g, this.fontSize / 2, this.height - this.fontSize / 2, fontSize, output);
        }

    }

}
