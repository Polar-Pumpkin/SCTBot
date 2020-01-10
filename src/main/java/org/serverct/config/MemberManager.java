package org.serverct.config;

import lombok.Getter;
import org.dom4j.DocumentException;
import org.serverct.SCTBot;
import org.serverct.data.Actionlog;
import org.serverct.data.ContactDetail;
import org.serverct.data.SCTMember;
import org.serverct.data.Work;
import org.serverct.enums.ActionlogType;
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

    @Getter private File dataFolder = new File("D:\\test\\Members");
    private Map<Long, SCTMember> loadMemberMap = new HashMap<>();

    public int load() {
        if(!dataFolder.exists()) {
            dataFolder.mkdirs();
            return 0;
        }

        File[] files = BasicUtil.getXML(dataFolder);

        if(files != null) {
            for(File file : files) {
                try {
                    SCTMember member = XMLUtil.loadMember(file);
                    loadMemberMap.put(Long.parseLong(member.getContacts().get("QQ").getIdNumber()), member);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
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

        for(int index = 5; index < args.length; index++) {
            String[] dataSet = args[index].split(":");
            switch(dataSet[0]) {
                case "Wechat":
                case "WeChat":
                case "wechat":
                    contactsMap.put("Wechat", new ContactDetail("Wechat", "", dataSet[1]));
                    break;
                case "bbs":
                case "BBS":
                    if(dataSet[1].contains(",")) {
                        String[] bbsData = dataSet[1].split(",");
                        contactsMap.put("BBS", new ContactDetail("BBS", bbsData[0], bbsData[1]));
                    } else {
                        contactsMap.put("BBS", new ContactDetail("BBS", dataSet[1], ""));
                    }
                    break;
                case "birth":
                case "Birth":
                    birth = TimeUtil.getCalender(dataSet[1]);
                    break;

            }
        }
        SCTMember newMember = new SCTMember(nickname, birth, departmentsMap, ppoint, contactsMap, actionlogList, workList);
        loadMemberMap.put(target, newMember);
        XMLUtil.saveMember(newMember);
        return newMember;
    }


}
