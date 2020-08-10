package org.serverct.utils;

import lombok.NonNull;
import org.w3c.dom.*;
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
        String[] report = {
                "> 正在读取值 -> 节点属性...",
                "  > 参数检查(节点): " + element.toString(),
                "  > 参数检查(目标属性): " + attrName
        };
        BasicUtil.debug(report, true);
        return element.getAttributes().getNamedItem(attrName).getNodeValue();
    }

    public static void details(Document doc) {
        BasicUtil.debug("输出 XML 数据至控制台: " + doc.getLocalName(), true);
        BasicUtil.debug(doc.toString(), true);

        Element root = doc.getDocumentElement();
        BasicUtil.debug(root.toString(), true);
        String[] elemReport = {
                "Element >",
                "hasAttributes: " + root.hasAttributes(),
                "hasChildNodes: " + root.hasChildNodes(),
                "",
                "NodeName: " + root.getNodeName(),
                "NodeValue: " + root.getNodeValue(),
                "BaseURI: " + root.getBaseURI(),
                "LocalName: " + root.getLocalName(),
                "NamespaceURI: " + root.getNamespaceURI(),
                "Prefix: " + root.getPrefix(),
                "TextContent: " + root.getTextContent(),
                "--------------------------------------------------"
        };
        BasicUtil.debug(elemReport, true);
        attrDetails(root);
        childDetails(root);
    }

    private static void attrDetails(@NonNull Node target) {
        NamedNodeMap attrs = target.getAttributes();
        BasicUtil.debug(attrs.toString(), true);
        String[] attrReport = {
                "NamedNodeMap(Attributes) >",
                "Length: " + attrs.getLength(),
                "--------------------------------------------------"
        };
        BasicUtil.debug(attrReport, true);
        for(int index = 0;index < attrs.getLength();index++) {
            Node node = attrs.item(index);
            String[] nodeReport = {
                    "Node >",
                    "hasAttributes: " + node.hasAttributes(),
                    "hasChildNodes: " + node.hasChildNodes(),
                    "",
                    "NodeName: " + node.getNodeName(),
                    "NodeValue: " + node.getNodeValue(),
                    "BaseURI: " + node.getBaseURI(),
                    "LocalName: " + node.getLocalName(),
                    "NamespaceURI: " + node.getNamespaceURI(),
                    "Prefix: " + node.getPrefix(),
                    "TextContent: " + node.getTextContent(),
                    "--------------------------------------------------"
            };
            BasicUtil.debug(nodeReport, true);
        }
    }

    private static void childDetails(@NonNull Node target) {
        NodeList childs = target.getChildNodes();
        BasicUtil.debug(childs.toString(), true);
        String[] nodeListReport = {
                "NodeList(Childs) >",
                "Length: " + childs.getLength(),
                "--------------------------------------------------"
        };
        BasicUtil.debug(nodeListReport, true);
        for(int index = 0;index < childs.getLength();index++) {
            Node node = childs.item(index);
            String[] nodeReport = {
                    "Node >",
                    "hasAttributes: " + node.hasAttributes(),
                    "hasChildNodes: " + node.hasChildNodes(),
                    "",
                    "NodeName: " + node.getNodeName(),
                    "NodeValue: " + node.getNodeValue(),
                    "BaseURI: " + node.getBaseURI(),
                    "LocalName: " + node.getLocalName(),
                    "NamespaceURI: " + node.getNamespaceURI(),
                    "Prefix: " + node.getPrefix(),
                    "TextContent: " + node.getTextContent(),
                    "--------------------------------------------------"
            };
            BasicUtil.debug(nodeReport, true);
            attrDetails(node);
        }
    }

}
