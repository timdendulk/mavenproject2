package Testutil;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertTrue;

/**
 * Created by tjitte.bouma on 09-01-2017.
 */
public class XmlHelper {

    public static void printGeformatteXml(String xml) {
        String formattedXml = xmlToPrettyString(xml);
        System.out.println("\n" + formattedXml);
    }

    public static String haalWaardeVanUniekeTagInXmlBericht(String xmlInString, String tagNaam) {
        NodeList nodeList = haalNodeListVoorOnderdeelInXmlBericht(xmlInString, tagNaam);
        Node xmlVeld = nodeList.item(0); // Hoeft niet per se uniek te zijn, er wordt een lijst opgehaald en de eerste wordt bekeken.
        assertTrue(xmlVeld.getNodeName().equals(tagNaam));
        return xmlVeld.getTextContent();
    }

    private static NodeList haalNodeListVoorOnderdeelInXmlBericht(String outputString, String onderdeel) {
        Document document = parseXmlFile(outputString);
        return document.getElementsByTagName(onderdeel);
    }

    private static Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (Exception e) {
            throw new RuntimeException("Het parsen van het xml bestand is mislukt.", e);
        }
    }

    public static String xmlToPrettyString(String xml) {
        try {
            Document document = parseXmlFile(xml);
            // Remove whitespaces outside tags
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']", document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }
            // Setup pretty print options
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 2); // Indent number, aantal spaties gebruikt voor elke 'sprong'.
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            // Return pretty print xml string
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException("XML omzetten naar pretty print is mislukt.",e);
        }
    }
}
