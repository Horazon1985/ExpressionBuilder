package utilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestBaseSOAP {

    public static boolean compareXMLElements(String xml, String elementName, Object valueToCompare) {

        String restXML;
        String element, elementWithinTag;
        try {
            for (int i = 0; i < xml.length(); i++) {
                if (!"<".equals(xml.substring(i, i + 1)) || i + 1 < xml.length() && "/".equals(xml.substring(i + 1, i + 2))) {
                    continue;
                }
                
                restXML = xml.substring(i, xml.length());
                int indexOfClosingBracket = restXML.indexOf(">");
                if (indexOfClosingBracket > 1 && !restXML.substring(indexOfClosingBracket - 1, indexOfClosingBracket).equals("/")) {
                    elementWithinTag = restXML.substring(0, indexOfClosingBracket + 1);
                    element = getElementName(elementWithinTag);
                    if (element == null || !element.equals(elementName)){
                        continue;
                    }
                    String closingTag = "</" + elementWithinTag.substring(1, elementWithinTag.length());
                    if (restXML.contains(closingTag)){
                        String elementContent = restXML.substring(elementWithinTag.length(), restXML.indexOf(closingTag));
                        return elementContent.equals(valueToCompare.toString());
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;

    }

    /**
     * Voraussetzung: xmlPart hat die folgende Form: entweder "<xy:abc/>" (dann
     * wird "abc" zurückgegeben) oder "<xy:abc>" (dann wird "abc"). In allen
     * anderen Fällen wird null zurückgegeben.
     */
    private static String getElementName(String xmlPart) {
        int indexOfClosingBracket = xmlPart.indexOf(">");
        if (indexOfClosingBracket != xmlPart.length() - 1 || !xmlPart.contains(":")) {
            return null;
        }
        String elementName = xmlPart.substring(xmlPart.indexOf(":") + 1, xmlPart.length() - 1);
        if (elementName.substring(elementName.length() - 1, elementName.length()).equals("/")) {
            return elementName.substring(0, elementName.length() - 1);
        }
        return elementName;
    }
    
    @Test
    public void testReadAndCompareXML(){
        String xml = "<xml><ns1:nachname>Kovalenko</ns1:nachname><status/><ns2:betrag>105</ns2:betrag></xml>";
        assertTrue(compareXMLElements(xml, "nachname", "Kovalenko"));
        assertFalse(compareXMLElements(xml, "nachname", "Schmidt"));
        assertTrue(compareXMLElements(xml, "betrag", "105"));
        assertTrue(compareXMLElements(xml, "betrag", 105));
        assertFalse(compareXMLElements(xml, "status", "TestStatus"));
        assertFalse(compareXMLElements(xml, "solcheinelementgibtesnicht", "inhalt"));
    }
    

}
