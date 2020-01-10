package org.serverct.utils;

import lombok.NonNull;
import org.serverct.config.MemberManager;
import org.serverct.data.Actionlog;
import org.serverct.data.ContactDetail;
import org.serverct.data.SCTMember;
import org.serverct.data.Work;
import org.serverct.enums.ActionlogType;
import org.serverct.enums.HighlightType;
import org.serverct.enums.TagType;
import org.serverct.enums.WorkType;
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
import java.util.*;

public class XMLUtil {

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

    public static SCTMember loadMember(File file) {
        Document memberDoc = load(file);
        Element sctMember = memberDoc.getDocumentElement();

        Node nickname = getNode(sctMember, "Nickname");
        String nicknameStr = getAttr(nickname, "Value");

        Node birth = getNode(sctMember, "Birth");
        Calendar birthCal = TimeUtil.getCalender(getAttr(birth, "Date"));


        Map<String, String> departmentsMap = new HashMap<>();
        Node departments = getNode(sctMember, "Departments");
        for(Node department : getChilds(departments)) {
            departmentsMap.put(getAttr(department, "ID"), getAttr(department, "Position"));
        }

        Node ppoint = getNode(sctMember, "PPoint");
        int ppointInt = Integer.parseInt(getAttr(ppoint, "Amount"));

        Map<String, ContactDetail> contactsMap = new HashMap<>();
        Node contacts = getNode(sctMember, "Contacts");
        for(Node contact : getChilds(contacts)) {
            String contactID = contact.getNodeName();
            if(contactID.equalsIgnoreCase("BBS")) {
                contactsMap.put(contactID, new ContactDetail(contactID, getAttr(contact, "Username"), getAttr(contact, "ID")));
            } else {
                contactsMap.put(contactID, new ContactDetail(contactID, "", getAttr(contact, "ID")));
            }
        }

        List<Actionlog> actionlogList = new ArrayList<>();
        Node actionlogs = getNode(sctMember, "Actionlogs");
        List<Node> actionlogNodes = getChilds(actionlogs);
        if(!actionlogNodes.isEmpty()) {
            for(Node actionlog : actionlogNodes) {
                actionlogList.add(
                        new Actionlog(
                                ActionlogType.valueOf(getAttr(actionlog, "Type").toUpperCase()),
                                TimeUtil.getCalender(getAttr(actionlog, "Date")),
                                getAttr(actionlog, "Additional")
                        )
                );
            }
        }

        List<Work> workList = new ArrayList<>();
        Node works = getNode(sctMember, "Works");
        List<Node> workNodes = getChilds(works);
        if(!workNodes.isEmpty()) {
            for(Node work : workNodes) {
                List<TagType> tagList = new ArrayList<>();
                Node tags = getNode(work, "Tags");
                try {
                    for(Node tag : getChilds(tags)) {
                        tagList.add(TagType.valueOf(getAttr(tag, "Type").toUpperCase()));
                    }
                } catch(Throwable ignored) {}

                workList.add(
                        new Work(
                                WorkType.valueOf(getAttr(work, "Type").toUpperCase()),
                                getAttr(work, "Url"),
                                TimeUtil.getCalender(getAttr(work, "Date")),
                                tagList,
                                HighlightType.valueOf(getAttr(work, "Highlight").toUpperCase()),
                                getAttr(work, "Rating")
                        )
                );
            }
        }

        return new SCTMember(nicknameStr, birthCal, departmentsMap, ppointInt, contactsMap, actionlogList, workList);
    }

    public static boolean saveMember(SCTMember member) {
        StringBuilder path = new StringBuilder(MemberManager.getInstance().getDataFolder().getAbsolutePath());
        path
                .append(File.separator)
                .append(member.getContacts().get("QQ").getIdNumber())
                .append("-")
                .append(member.getNickname())
                .append(".xml");

        File output = new File(path.toString());

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        if (db != null) {
            Document document = db.newDocument();
            Element root = document.createElement("SCTMember");


            Element nickname = document.createElement("Nickname");
            nickname.setAttribute("Value", member.getNickname());
            setChild(root, nickname);

            Calendar birth = member.getBirth();
            if (birth != null) {
                Element birthElem = document.createElement("Birth");
                birthElem.setAttribute("Date", TimeUtil.format(birth, "yyyy-MM-dd"));
                setChild(root, birthElem);
            }

            Element departments = document.createElement("Departments");
            Map<String, String> departmentMap = member.getDepartment();
            for (String id : departmentMap.keySet()) {
                Element department = document.createElement("Department");
                department.setAttribute("ID", id);
                department.setAttribute("Position", departmentMap.get(id));
                setChild(departments, department);
            }
            setChild(root, departments);

            Element ppoint = document.createElement("PPoint");
            ppoint.setAttribute("Amount", String.valueOf(member.getPpoint()));
            setChild(root, ppoint);

            Element contacts = document.createElement("Contacts");
            Map<String, ContactDetail> contactMap = member.getContacts();
            for (String id : contactMap.keySet()) {
                ContactDetail detail = contactMap.get(id);
                if (detail != null) {
                    Element targetContact = document.createElement(id);
                    targetContact.setAttribute("ID", detail.getIdNumber());
                    if (!detail.getUsername().equalsIgnoreCase("")) {
                        targetContact.setAttribute("Username", detail.getUsername());
                    }
                    setChild(contacts, targetContact);
                }
            }
            setChild(root, contacts);

            Element actionlogs = document.createElement("Actionlogs");
            List<Actionlog> actionlogList = member.getLogs();
            for (Actionlog log : actionlogList) {
                Element targetActionlog = document.createElement("Actionlog");
                targetActionlog.setAttribute("Type", log.getType().toString());
                targetActionlog.setAttribute("Date", TimeUtil.format(log.getDate(), "yyyy-MM-dd"));
                targetActionlog.setAttribute("Additional", log.getAdditional());
                setChild(actionlogs, targetActionlog);
            }
            setChild(root, actionlogs);

            Element works = document.createElement("Works");
            List<Work> workList = member.getWorks();
            for (Work work : workList) {
                Element targetWork = document.createElement("Work");
                targetWork.setAttribute("Type", work.getType().toString());
                targetWork.setAttribute("Url", work.getUrl());
                targetWork.setAttribute("Date", TimeUtil.format(work.getDate(), "yyyy-MM-dd"));
                targetWork.setAttribute("Highlight", work.getHighlight().toString());
                targetWork.setAttribute("Rating", work.getRating());

                List<TagType> tagList = work.getTags();
                if (!tagList.isEmpty()) {
                    Element tags = document.createElement("Tags");
                    for (TagType tag : tagList) {
                        Element targetTag = document.createElement("Tag");
                        targetTag.setAttribute("Type", tag.toString());
                        setChild(tags, targetTag);
                    }
                    setChild(targetWork, tags);
                }
                setChild(works, targetWork);
            }
            setChild(root, works);

            document.appendChild(root);
            return save(document, output);
        }
        return false;
    }

}
