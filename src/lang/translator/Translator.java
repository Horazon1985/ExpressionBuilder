package lang.translator;

import abstractexpressions.expression.classes.Expression;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Translator {

    /**
     * Gibt eine Meldung entsprechend der exceptionId und der eingestellten
     * Sprache zurück.
     */
    private static String translateMessage(String exceptionId) {

        // Die entsprechende XML-Datei öffnen.
        try {
            URL langFile;
            if (exceptionId.substring(0, 2).equals("CC")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangComputationalClasses.xml");
            } else if (exceptionId.substring(0, 2).equals("EB")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangExpressionBuilder.xml");
            } else if (exceptionId.substring(0, 3).equals("GUI")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangGUI.xml");
            } else if (exceptionId.substring(0, 2).equals("GR")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangGraphic.xml");
            } else if (exceptionId.substring(0, 3).equals("LEB")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangLogicalExpressionBuilder.xml");
            } else if (exceptionId.substring(0, 3).equals("MCC")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangMathCommandCompiler.xml");
            } else if (exceptionId.substring(0, 3).equals("MEB")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangMatrixExpressionBuilder.xml");
            } else if (exceptionId.substring(0, 2).equals("SM")) {
                langFile = ClassLoader.getSystemResource("lang/messages/LangSimplifyMethods.xml");
            } else {
                // Datei für unbekannten Fehler öffnen.
                langFile = ClassLoader.getSystemResource("lang/messages/LangUndefinedError.xml");
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(langFile.openStream());

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("object");

            for (int i = 0; i < nList.getLength(); i++) {

                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    if (eElement.getAttribute("id").equals(exceptionId)) {
                        switch (Expression.getLanguage()) {
                            case DE:
                                return eElement.getElementsByTagName("German").item(0).getTextContent();
                            case EN:
                                return eElement.getElementsByTagName("English").item(0).getTextContent();
                            case RU:
                                return eElement.getElementsByTagName("Russian").item(0).getTextContent();
                            case UA:
                                return eElement.getElementsByTagName("Ukrainian").item(0).getTextContent();
                            default:
                                break;
                        }
                    }

                }
            }
        } catch (Exception e) {
        }

        // Sollte nie eintreten.
        return "";

    }

    /**
     * Gibt eine Meldung entsprechend der exceptionId und der eingestellten
     * Sprache zurück, wobei Tokens der Form [0], [1], [2], ... nacheinander
     * durch die Parameter params ersetzt werden.
     */
    public static String translateOutputMessage(String messageId, Object... params) {
        String message = translateMessage(messageId);
        String token;
        for (int i = 0; i < params.length; i++) {
            token = "[" + i + "]";
            while (message.contains(token)) {
                message = message.substring(0, message.indexOf(token)) + params[i].toString() + message.substring(message.indexOf(token) + token.length());
            }
        }
        return message;
    }

}
