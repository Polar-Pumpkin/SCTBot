package org.serverct.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.serverct.utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public @Data @AllArgsConstructor class SCTMember {

    String nickname;
    Calendar birth;
    Map<String, String> department;
    int ppoint;
    Map<String, ContactDetail> contacts;
    List<Actionlog> logs;
    List<Work> works;

    public String info() {
        return "组员信息 | Member" + "\n" +
                "> 昵称: " + nickname + "\n" +
                "> BBSID: " + contacts.get("BBS").getUsername() + "(UID: " + contacts.get("BBS").getIdNumber() +  ")" + "\n" +
                "> 入组时间: " + TimeUtil.format(logs.get(0).getDate(), "yyyy-MM-dd") + "\n" +
                "> 所属部门: " + departmentString() + "\n" +
                "> P点数: " + ppoint
                ;
    }

    private String departmentString() {
        String format = "%department%部(%position%)";
        StringBuilder result = new StringBuilder();

        Iterator<String> keySet = department.keySet().iterator();
        while(keySet.hasNext()) {
            String id = keySet.next();
            result.append(format.replace("%department%", id).replace("%position%", department.get(id)));
            if(keySet.hasNext()) {
               result.append(", ");
            }
        }
        return result.toString();
    }
}
