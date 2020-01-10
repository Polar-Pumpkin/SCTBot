package org.serverct.config;

import lombok.Getter;
import org.serverct.SCTBot;
import org.serverct.data.Actionlog;
import org.serverct.data.ContactDetail;
import org.serverct.data.SCTMember;
import org.serverct.data.Work;
import org.serverct.enums.ActionlogType;
import org.serverct.enums.MemberDataType;
import org.serverct.utils.BasicUtil;
import org.serverct.utils.TimeUtil;
import org.serverct.utils.XMLUtil;

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
                SCTMember member = XMLUtil.loadMember(file);
                loadMemberMap.put(Long.parseLong(member.getContacts().get("QQ").getIdNumber()), member);
            }
        }
        return loadMemberMap.size();
    }

    public SCTMember get(long qq) {
        return loadMemberMap.get(qq);
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

        Map<MemberDataType, Object> result = new HashMap<>();
        for(int index = 4; index < args.length; index++) {
            result.putAll(parsingExpression(args[index]));
        }
        for(MemberDataType type : result.keySet()) {
            Object data = result.get(type);
            switch (type) {
                case NICKNAME:
                    if(data instanceof String) {
                        member.setNickname((String) data);
                    }
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
                    if(data instanceof String) {
                        member.setPpoint(Integer.parseInt((String) data));
                    }
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
        XMLUtil.saveMember(member);
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

        List<Actionlog> actionlogList = new ArrayList<>();
        actionlogList.add(new Actionlog(ActionlogType.LOG, TimeUtil.getCalender(TimeUtil.format(Calendar.getInstance(), "yyyy-MM-dd")), ActionlogType.LOG.getName()));

        List<Work> workList = new ArrayList<>();

        Calendar birth = null;
        int ppoint = 0;

        Map<MemberDataType, Object> result = new HashMap<>();
        for(int index = 5; index < args.length; index++) {
            result.putAll(parsingExpression(args[index]));
        }
        for(MemberDataType type : result.keySet()) {
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
        XMLUtil.saveMember(newMember);
        return newMember;
    }

    public Map<MemberDataType, Object> parsingExpression(String expression) {
        Map<MemberDataType, Object> result = new HashMap<>();
        String[] dataSet = expression.split(":");

        MemberDataType type = MemberDataType.NONE;
        try {
            type = MemberDataType.valueOf(dataSet[0].toUpperCase());
        } catch (Throwable ignored) {}

        switch(type) {
            case NICKNAME:
                result.put(MemberDataType.NICKNAME, dataSet[1]);
                break;
            case DEPARTMENT:
                String[] departs = dataSet[1].split(";");
                Map<String, String> departments = new HashMap<>();
                for(String depart : departs) {
                    String[] departData = depart.split(",");
                    departments.put(departData[0], departData[1]);
                }
                result.put(MemberDataType.DEPARTMENT, departments);
                break;
            case BIRTH:
                result.put(MemberDataType.BIRTH, TimeUtil.getCalender(dataSet[1]));
                break;
            case PPOINT:
                result.put(MemberDataType.PPOINT, dataSet[1]);
                break;
            case BBS:
                if(dataSet[1].contains(",")) {
                    String[] bbsData = dataSet[1].split(",");
                    result.put(MemberDataType.BBS, new ContactDetail("BBS", bbsData[0], bbsData[1]));
                } else {
                    result.put(MemberDataType.BBS, new ContactDetail("BBS", dataSet[1], "未登记"));
                }
                break;
            case WECHAT:
                result.put(MemberDataType.WECHAT, new ContactDetail("Wechat", "", dataSet[1]));
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

}
