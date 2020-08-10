package org.serverct.config;

import lombok.Getter;
import org.serverct.SCTBot;
import org.serverct.data.ContactDetail;
import org.serverct.data.SCTMember;
import org.serverct.data.Work;
import org.serverct.enums.memberdata.Actionlog;
import org.serverct.enums.memberdata.MemberData;
import org.serverct.enums.workcheck.Highlight;
import org.serverct.enums.workcheck.Tag;
import org.serverct.enums.workcheck.WorkType;
import org.serverct.utils.BasicUtil;
import org.serverct.utils.TimeUtil;
import org.serverct.utils.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.util.*;

public class MemberManager {

    private static MemberManager instance;
    public static MemberManager getInstance() {
        if(instance == null) {
            instance = new MemberManager();
        }
        return instance;
    }

    @Getter private File dataFolder = new File(SCTBot.CQ.getAppDirectory() + File.separator + "Members");
    private Map<Long, SCTMember> loadMemberMap = new HashMap<>();

    public int load() {
        if(!dataFolder.exists()) {
            dataFolder.mkdirs();
            return 0;
        }

        File[] files = BasicUtil.getXML(dataFolder);

        if(files != null) {
            for(File file : files) {
                SCTMember member = loadMember(file);
                loadMemberMap.put(Long.parseLong(member.getContacts().get("QQ").getIdNumber()), member);
            }
        }
        return loadMemberMap.size();
    }

    public SCTMember get(long qq) {
        return loadMemberMap.getOrDefault(qq, null);
    }

    public String list() {
        if(loadMemberMap.size() > 0) {
            StringBuilder result = new StringBuilder("成员列表 | Member\n共加载 " + loadMemberMap.size() + " 名成员档案.\n");
            Iterator<Long> keySet = loadMemberMap.keySet().iterator();
            while (keySet.hasNext()) {
                long qq = keySet.next();
                result.append("> " + loadMemberMap.get(qq).getNickname() + "(" + qq + ")");
                if(keySet.hasNext()) {
                    result.append("\n");
                }
            }
            return result.toString();
        }
        return "无数据. :(";
    }

    public SCTMember modify(long target, String[] args) {
        if(!loadMemberMap.containsKey(target)) {
            return null;
        }

        SCTMember member = loadMemberMap.get(target);

        Map<MemberData, Object> result = new HashMap<>();
        for(int index = 4; index < args.length; index++) {
            result.putAll(parsingExpression(args[index]));
        }
        for(MemberData type : result.keySet()) {
            Object data = result.get(type);
            switch (type) {
                case NICKNAME:
                    member.setNickname((String) data);
                case DEPARTMENT:
                    if(data instanceof HashMap<?,?>) {
                        Map<String, String> departments = member.getDepartment();
                        departments.putAll((HashMap<String, String>) data);
                        member.setDepartment(departments);
                    }
                case BIRTH:
                    if(data instanceof Calendar) {
                        member.setBirth((Calendar) data);
                    }
                case PPOINT:
                    member.setPpoint(Integer.parseInt((String) data));
                case BBS:
                    if(data instanceof ContactDetail) {
                        Map<String, ContactDetail> contactsMap = member.getContacts();
                        contactsMap.put("BBS", (ContactDetail) data);
                        member.setContacts(contactsMap);
                    }
                case WECHAT:
                    if(data instanceof ContactDetail) {
                        Map<String, ContactDetail> contactsMap = member.getContacts();
                        contactsMap.put("Wechat", (ContactDetail) data);
                        member.setContacts(contactsMap);
                    }
                case NONE:
                default:
                    break;
            }
        }
        loadMemberMap.put(target, member);
        saveMember(member);
        return member;
    }

    /*
    args[-] ~
    args[0] members
    args[1] new
    args[2] target
    args[3] nickname
    args[4] id,position;id,position
    ~Optional
    birth:yyyy-MM-dd
    bbs:username,uid
    wechat:id
    */
    public SCTMember newMember(long target, String[] args) {
        if(loadMemberMap.containsKey(target)) {
            return null;
        }

        String nickname = args[3];

        Map<String, String> departmentsMap = new HashMap<>();
        String[] departments = args[4].split(";");
        for(String department : departments) {
            String[] depart = department.split(",");
            departmentsMap.put(depart[0], depart[1]);
        }

        Map<String, ContactDetail> contactsMap = new HashMap<>();
        contactsMap.put("QQ", new ContactDetail("QQ", "", String.valueOf(target)));

        List<org.serverct.data.Actionlog> actionlogList = new ArrayList<>();
        actionlogList.add(new org.serverct.data.Actionlog(Actionlog.LOG, TimeUtil.getCalender(TimeUtil.format(Calendar.getInstance(), "yyyy-MM-dd")), Actionlog.LOG.getName()));

        List<Work> workList = new ArrayList<>();

        Calendar birth = null;
        int ppoint = 0;

        Map<MemberData, Object> result = new HashMap<>();
        for(int index = 5; index < args.length; index++) {
            result.putAll(parsingExpression(args[index]));
        }
        for(MemberData type : result.keySet()) {
            Object data = result.get(type);
            switch (type) {
                case BIRTH:
                    if(data instanceof Calendar) {
                        birth = (Calendar) data;
                    }
                case BBS:
                    if(data instanceof ContactDetail) {
                        contactsMap.put("BBS", (ContactDetail) data);
                    }
                case WECHAT:
                    if(data instanceof ContactDetail) {
                        contactsMap.put("Wechat", (ContactDetail) data);
                    }
                case NONE:
                default:
                    break;
            }
        }

        SCTMember newMember = new SCTMember(nickname, birth, departmentsMap, ppoint, contactsMap, actionlogList, workList);
        loadMemberMap.put(target, newMember);
        saveMember(newMember);
        return newMember;
    }

    public Map<MemberData, Object> parsingExpression(String expression) {
        Map<MemberData, Object> result = new HashMap<>();
        String[] dataSet = expression.split(":");

        MemberData type = MemberData.NONE;
        try {
            type = MemberData.valueOf(dataSet[0].toUpperCase());
        } catch (Throwable ignored) {}

        switch(type) {
            case NICKNAME:
                result.put(MemberData.NICKNAME, dataSet[1]);
                break;
            case DEPARTMENT:
                String[] departs = dataSet[1].split(";");
                Map<String, String> departments = new HashMap<>();
                for(String depart : departs) {
                    String[] departData = depart.split(",");
                    departments.put(departData[0], departData[1]);
                }
                result.put(MemberData.DEPARTMENT, departments);
                break;
            case BIRTH:
                result.put(MemberData.BIRTH, TimeUtil.getCalender(dataSet[1]));
                break;
            case PPOINT:
                result.put(MemberData.PPOINT, dataSet[1]);
                break;
            case BBS:
                if(dataSet[1].contains(",")) {
                    String[] bbsData = dataSet[1].split(",");
                    result.put(MemberData.BBS, new ContactDetail("BBS", bbsData[0], bbsData[1]));
                } else {
                    result.put(MemberData.BBS, new ContactDetail("BBS", dataSet[1], "未登记"));
                }
                break;
            case WECHAT:
                result.put(MemberData.WECHAT, new ContactDetail("Wechat", "", dataSet[1]));
                break;
            case LOGS:
            case WORKS:
                SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "还没做好呢!");
                break;
            case NONE:
            default:
                break;
        }
        return result;
    }

    public static SCTMember loadMember(File file) {
        String[] report = {
                "    > 加载成员档案文件...",
                "      > 文件名: " + file.getName(),
                "      > 大小: " + file.getUsableSpace() + "/" + file.getTotalSpace(),
                "      > 绝对路径: " + file.getAbsolutePath(),
        };
        BasicUtil.debug(report, true);
        Document memberDoc = XMLUtil.load(file);
        XMLUtil.details(memberDoc);
        Element sctMember = memberDoc.getDocumentElement();

        Node nickname = XMLUtil.getNode(sctMember, "Nickname");
        String nicknameStr = XMLUtil.getAttr(nickname, "Value");

        Calendar birthCal = null;
        if(XMLUtil.hasNode(sctMember, "Birth")) {
            Node birth = XMLUtil.getNode(sctMember, "Birth");
            birthCal = TimeUtil.getCalender(XMLUtil.getAttr(birth, "Date"));
        }

        Map<String, String> departmentsMap = new HashMap<>();
        Node departments = XMLUtil.getNode(sctMember, "Departments");
        for(Node department : XMLUtil.getChilds(departments)) {
            departmentsMap.put(XMLUtil.getAttr(department, "ID"), XMLUtil.getAttr(department, "Position"));
        }

        Node ppoint = XMLUtil.getNode(sctMember, "PPoint");
        int ppointInt = Integer.parseInt(XMLUtil.getAttr(ppoint, "Amount"));

        Map<String, ContactDetail> contactsMap = new HashMap<>();
        Node contacts = XMLUtil.getNode(sctMember, "Contacts");
        for(Node contact : XMLUtil.getChilds(contacts)) {
            String contactID = contact.getNodeName();
            if(contactID.equalsIgnoreCase("BBS")) {
                contactsMap.put(contactID, new ContactDetail(contactID, XMLUtil.getAttr(contact, "Username"), XMLUtil.getAttr(contact, "ID")));
            } else {
                contactsMap.put(contactID, new ContactDetail(contactID, "", XMLUtil.getAttr(contact, "ID")));
            }
        }

        List<org.serverct.data.Actionlog> actionlogList = new ArrayList<>();
        Node actionlogs = XMLUtil.getNode(sctMember, "Actionlogs");
        List<Node> actionlogNodes = XMLUtil.getChilds(actionlogs);
        if(!actionlogNodes.isEmpty()) {
            for(Node actionlog : actionlogNodes) {
                actionlogList.add(
                        new org.serverct.data.Actionlog(
                                Actionlog.valueOf(XMLUtil.getAttr(actionlog, "Type").toUpperCase()),
                                TimeUtil.getCalender(XMLUtil.getAttr(actionlog, "Date")),
                                XMLUtil.getAttr(actionlog, "Additional")
                        )
                );
            }
        }

        List<Work> workList = new ArrayList<>();
        Node works = XMLUtil.getNode(sctMember, "Works");
        List<Node> workNodes = XMLUtil.getChilds(works);
        if(!workNodes.isEmpty()) {
            for(Node work : workNodes) {
                List<Tag> tagList = new ArrayList<>();
                Node tags = XMLUtil.getNode(work, "Tags");
                try {
                    for(Node tag : XMLUtil.getChilds(tags)) {
                        tagList.add(Tag.valueOf(XMLUtil.getAttr(tag, "Type").toUpperCase()));
                    }
                } catch(Throwable ignored) {}

                workList.add(
                        new Work(
                                WorkType.valueOf(XMLUtil.getAttr(work, "Type").toUpperCase()),
                                XMLUtil.getAttr(work, "Url"),
                                TimeUtil.getCalender(XMLUtil.getAttr(work, "Date")),
                                tagList,
                                Highlight.valueOf(XMLUtil.getAttr(work, "Highlight").toUpperCase()),
                                XMLUtil.getAttr(work, "Rating")
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

        Document document = XMLUtil.create();
        Element root = document.createElement("SCTMember");

        Element nickname = document.createElement("Nickname");
        nickname.setAttribute("Value", member.getNickname());
        XMLUtil.setChild(root, nickname);

        Calendar birth = member.getBirth();
        if (birth != null) {
            Element birthElem = document.createElement("Birth");
            birthElem.setAttribute("Date", TimeUtil.format(birth, "yyyy-MM-dd"));
            XMLUtil.setChild(root, birthElem);
        }

        Element departments = document.createElement("Departments");
        Map<String, String> departmentMap = member.getDepartment();
        for (String id : departmentMap.keySet()) {
            Element department = document.createElement("Department");
            department.setAttribute("ID", id);
            department.setAttribute("Position", departmentMap.get(id));
            XMLUtil.setChild(departments, department);
        }
        XMLUtil.setChild(root, departments);

        Element ppoint = document.createElement("PPoint");
        ppoint.setAttribute("Amount", String.valueOf(member.getPpoint()));
        XMLUtil.setChild(root, ppoint);

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
                XMLUtil.setChild(contacts, targetContact);
            }
        }
        XMLUtil.setChild(root, contacts);

        Element actionlogs = document.createElement("Actionlogs");
        List<org.serverct.data.Actionlog> actionlogList = member.getLogs();
        for (org.serverct.data.Actionlog log : actionlogList) {
            Element targetActionlog = document.createElement("Actionlog");
            targetActionlog.setAttribute("Type", log.getType().toString());
            targetActionlog.setAttribute("Date", TimeUtil.format(log.getDate(), "yyyy-MM-dd"));
            targetActionlog.setAttribute("Additional", log.getAdditional());
            XMLUtil.setChild(actionlogs, targetActionlog);
        }
        XMLUtil.setChild(root, actionlogs);

        Element works = document.createElement("Works");
        List<Work> workList = member.getWorks();
        for (Work work : workList) {
            Element targetWork = document.createElement("Work");
            targetWork.setAttribute("Type", work.getType().toString());
            targetWork.setAttribute("Url", work.getUrl());
            targetWork.setAttribute("Date", TimeUtil.format(work.getDate(), "yyyy-MM-dd"));
            targetWork.setAttribute("Highlight", work.getHighlight().toString());
            targetWork.setAttribute("Rating", work.getRating());

            List<Tag> tagList = work.getTags();
            if (!tagList.isEmpty()) {
                Element tags = document.createElement("Tags");
                for (Tag tag : tagList) {
                    Element targetTag = document.createElement("Tag");
                    targetTag.setAttribute("Type", tag.toString());
                    XMLUtil.setChild(tags, targetTag);
                }
                XMLUtil.setChild(targetWork, tags);
            }
            XMLUtil.setChild(works, targetWork);
        }
        XMLUtil.setChild(root, works);

        document.appendChild(root);
        return XMLUtil.save(document, output);
    }
}
