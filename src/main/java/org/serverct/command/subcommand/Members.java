package org.serverct.command.subcommand;

import org.serverct.SCTBot;
import org.serverct.command.Subcommand;
import org.serverct.config.MemberManager;
import org.serverct.data.SCTMember;
import org.serverct.utils.BasicUtil;

public class Members implements Subcommand {
    public boolean execute(long qq, String[] args) {
        switch(args[1].split("\\[")[0]) {
            case "i":
            case "info":
            case "查询":
                long targetQQ = 0L;
                if(args.length == 3) {
                    targetQQ = BasicUtil.getQQ(args[2]);
                } else if(args.length == 2) {
                    try {
                        targetQQ = SCTBot.CC.getAt("[" + args[1].split("\\[")[1]);
                    } catch(Throwable ignored) {
                        SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "解析参数困难, 该打空格就乖乖打空格嘛.");
                    }
                } else {
                    return false;
                }

                SCTMember target = MemberManager.getInstance().get(targetQQ);
                if(target != null) {
                    SCTBot.CQ.sendGroupMsg(SCTBot.SCT, target.info());
                } else {
                    SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "查無此人嗷, 或者你参数写错了?");
                }
                break;
            case "n":
            case "new":
            case "新增":
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

                if(args.length >=5 && args.length <=8) {
                    long memberQQ = BasicUtil.getQQ(args[2]);
                    SCTMember result = MemberManager.getInstance().newMember(memberQQ, args);
                    if(result != null) {
                        SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "保存成功. ^^");
                    } else {
                        SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "不知道为啥保存不了, 可能是因为已经存在一个有效的档案了吧.");
                    }
                } else {
                    SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "我觉得你参数写错了.");
                }
                break;
            case "r":
            case "reload":
            case "重载":
                SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "共加载 " + MemberManager.getInstance().load() + " 名成员档案.");
                break;
            case "l":
            case "list":
            case "列表":
                SCTBot.CQ.sendGroupMsg(SCTBot.SCT, MemberManager.getInstance().list());
                break;
            case "m":
            case "modify":
            case "修改":
                /*
                args[-] ~
                args[0] members
                args[1] modify
                args[2] target
                ~Optional
                nickname:nickname
                department:id,position;...
                birth:yyyy-MM-dd
                ppoint:amount
                bbs:username,uid
                wechat:id
                actionlog:index,type,date,additional;...

                 */
                if(args.length >=5 && args.length <=10) {
                    // TODO 新指令
                    if(null != null) {
                        SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "保存成功. ^^");
                    } else {
                        SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "不知道为啥保存不了, 可能是因为已经存在一个有效的档案了吧.");
                    }
                } else {
                    SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "我觉得你参数写错了.");
                }
                break;
            default:
                break;
        }
        return true;
    }
}
