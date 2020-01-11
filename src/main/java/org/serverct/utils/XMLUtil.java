package org.serverct.utils;

import lombok.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XMLUtil {

    public static Document create() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Document load(@NonNull File file) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(file);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean save(Document xmldoc, File file) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer former = factory.newTransformer();
            former.setOutputProperty(OutputKeys.INDENT, "yes");
            former.transform(new DOMSource(xmldoc), new StreamResult(file));
            return true;
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasNode(@NonNull Node element, String nodeName) {
        return getNode(element, nodeName) != null;
    }

    public static Node getNode(@NonNull Node element, String nodeName) {
        NodeList nodes = element.getChildNodes();
        for(int index = 0;index < nodes.getLength();index++) {
            Node node = nodes.item(index);
            if(node.getNodeName().equalsIgnoreCase(nodeName)) {
                return node;
            }
        }
        return null;
    }

    public static List<Node> getChilds(@NonNull Node element) {
        List<Node> result = new ArrayList<>();
        NodeList nodes = element.getChildNodes();
        for(int index = 0;index < nodes.getLength();index++) {
            result.add(nodes.item(index));
        }
        return result;
    }

    public static Node setChild(@NonNull Node node, Element element) {
        node.appendChild(element);
        return node;
    }

    public static String getAttr(@NonNull Node element, String attrName) {
        return element.getAttributes().getNamedItem(attrName).getNodeValue();
    }

}
