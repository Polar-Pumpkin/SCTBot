package org.serverct.utils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.serverct.config.MemberManager;
import org.serverct.data.Actionlog;
import org.serverct.data.ContactDetail;
import org.serverct.data.SCTMember;
import org.serverct.data.Work;
import org.serverct.enums.ActionlogType;
import org.serverct.enums.HighlightType;
import org.serverct.enums.TagType;
import org.serverct.enums.WorkType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class XMLUtil {

    public static Document parse(URL url) throws DocumentException {
        return new SAXReader().read(url);
    }

    public static SCTMember loadMember(File file) throws DocumentException {
        BasicUtil.quickDebug("尝试读取成员档案: " + file.getName());
        SAXReader reader = new SAXReader();
        Document memberDoc = reader.read(file);
        BasicUtil.quickDebug("我读出来了: " + memberDoc.toString());
        Element sctMember = memberDoc.getRootElement();
        BasicUtil.quickDebug("根节点是 " + sctMember.getName());

        String nickname = sctMember.element("Nickname").attributeValue("Value");

        Calendar birth = TimeUtil.getCalender(sctMember.element("Birth").attributeValue("Date"));

        Map<String, String> departmentsMap = new HashMap<>();
        Element departments = sctMember.element("Departments");
        for(Element department : departments.elements()) {
            departmentsMap.put(department.attributeValue("ID"), department.attributeValue("Position"));
        }

        int ppoint = Integer.parseInt(sctMember.element("PPoint").attributeValue("Amount"));

        Map<String, ContactDetail> contactsMap = new HashMap<>();
        Element contacts = sctMember.element("Contacts");
        for(Element contact : contacts.elements()) {
            if(contact.getName().equalsIgnoreCase("BBS")) {
                contactsMap.put("BBS", new ContactDetail("BBS", contact.attributeValue("Username"), contact.attributeValue("ID")));
            } else {
                contactsMap.put(contact.getName(), new ContactDetail(contact.getName(), "", contact.attributeValue("ID")));
            }
        }

        List<Actionlog> actionlogList = new ArrayList<>();
        Element actionlogs = sctMember.element("Actionlogs");
        List<Element> actionlogNodes = actionlogs.elements();
        if(!actionlogNodes.isEmpty()) {
            for(Element actionlog : actionlogs.elements()) {
                actionlogList.add(new Actionlog(ActionlogType.valueOf(actionlog.attributeValue("Type").toUpperCase()),
                        TimeUtil.getCalender(actionlog.attributeValue("Date")),
                        actionlog.attributeValue("Additional")));
            }
        }

        List<Work> workList = new ArrayList<>();
        Element works = sctMember.element("Works");
        List<Element> workNodes = works.elements();
        if(!workNodes.isEmpty()) {
            for(Element work : workNodes) {
                List<TagType> tagList = new ArrayList<>();
                Element tags = work.element("Tags");
                try {
                    for(Element tag : tags.elements()) {
                        tagList.add(TagType.valueOf(tag.attributeValue("Type").toUpperCase()));
                    }
                } catch(Throwable ignored) {}

                workList.add(new Work(WorkType.valueOf(work.attributeValue("Type").toUpperCase()),
                        work.attributeValue("Url"),
                        TimeUtil.getCalender(work.attributeValue("Date")),
                        tagList,
                        HighlightType.valueOf(work.attributeValue("Highlight").toUpperCase()),
                        work.attributeValue("Rating")));
            }
        }

        SCTMember result = new SCTMember(nickname, birth, departmentsMap, ppoint, contactsMap, actionlogList, workList);
        BasicUtil.quickDebug("最后读出来的是: " + result.toString());
        return result;
    }

    public static boolean saveMember(SCTMember member) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("SCTMember");

        root.addElement("Nickname").addAttribute("Value", member.getNickname());

        Calendar birth = member.getBirth();
        if(birth != null) {
            root.addElement("Birth")
                    .addAttribute("Date", TimeUtil.format(birth, "yyyy-MM-dd"));
        }

        Element departments = root.addElement("Departments");
        Map<String, String> departmentMap = member.getDepartment();
        for(String id : departmentMap.keySet()) {
            departments.addElement("Department")
                    .addAttribute("ID", id)
                    .addAttribute("Position", departmentMap.get(id));
        }

        root.addElement("PPoint").addAttribute("Amount", String.valueOf(member.getPpoint()));

        Element contacts = root.addElement("Contacts");
        Map<String, ContactDetail> contactMap = member.getContacts();
        for(String id : contactMap.keySet()) {
            ContactDetail detail = contactMap.get(id);
            if(detail != null) {
                Element targetContact = contacts.addElement(id)
                        .addAttribute("ID", detail.getIdNumber());
                if(!detail.getUsername().equalsIgnoreCase("")) {
                    targetContact.addAttribute("Username", detail.getUsername());
                }
            }
        }

        Element actionlogs = root.addElement("Actionlogs");
        List<Actionlog> actionlogList = member.getLogs();
        for(Actionlog log : actionlogList) {
            actionlogs.addElement("Actionlog")
                    .addAttribute("Type", log.getType().toString())
                    .addAttribute("Date", TimeUtil.format(log.getDate(), "yyyy-MM-dd"))
                    .addAttribute("Additional", log.getAdditional());
        }

        Element works = root.addElement("Works");
        List<Work> workList = member.getWorks();
        for(Work work : workList) {
            Element workNode = works.addElement("Work")
                    .addAttribute("Type", work.getType().toString())
                    .addAttribute("Url", work.getUrl())
                    .addAttribute("Date", TimeUtil.format(work.getDate(), "yyyy-MM-dd"))
                    .addAttribute("Highlight", work.getHighlight().toString())
                    .addAttribute("Rating", work.getRating());
            List<TagType> tagList = work.getTags();
            if(!tagList.isEmpty()) {
                Element tags = workNode.addElement("Tags");
                for(TagType tag : tagList) {
                    tags.addElement("Tag")
                            .addAttribute("Type", tag.toString());
                }
            }
        }

        try (FileWriter fileWriter = new FileWriter(new File(MemberManager.getInstance().getDataFolder().getAbsolutePath() + File.separator + member.getContacts().get("QQ").getIdNumber() + ".xml"))) {
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(fileWriter, format);
            writer.write(document);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

}
