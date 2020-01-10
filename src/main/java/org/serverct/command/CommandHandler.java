package org.serverct.command;

import org.serverct.SCTBot;
import org.serverct.command.subcommand.Debug;
import org.serverct.command.subcommand.Members;
import org.serverct.utils.BasicUtil;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {

    private static CommandHandler instance;
    public static CommandHandler getInstance() {
        if(instance == null) {
            instance = new CommandHandler();
        }
        return instance;
    }

    private Map<String, Subcommand> subcommandMap = new HashMap<>();

    public void load() {
        registerSubcommand("成员|members|member|m", new Members());
        registerSubcommand("测试|debug|d", new Debug());
    }

    public void registerSubcommand(String cmd, Subcommand executor) {
        for(String command : cmd.split("\\|")) {
            if(!subcommandMap.containsKey(command)) {
                subcommandMap.put(command, executor);
            } else {
                SCTBot.CQ.logWarning("[SCTBot]", "重复注册命令: " + command);
            }
        }
    }

    public boolean onCommand(long qq, String[] args) {
        if(subcommandMap.containsKey(args[0])) {
            return subcommandMap.get(args[0]).execute(qq, args);
        } else {
            return false;
        }
    }

    public void active(long number, String msg, boolean isGroup) {
        String[] srcArgs = msg.split(" ");
        String[] args = BasicUtil.buildParameter(srcArgs);
        boolean exist = SCTBot.CC.getAt(srcArgs[0]) == SCTBot.CQ.getLoginQQ() || srcArgs[0].equalsIgnoreCase("~");
        if(exist) {
            if(isGroup) {
                if(number == SCTBot.SCT) {
                    if(srcArgs.length == 1 || !CommandHandler.getInstance().onCommand(number, args)) {
                        SCTBot.CQ.sendGroupMsg(number, "嗷，未知指令？");
                    }
                }
            } else {
                if(srcArgs.length == 1 || !CommandHandler.getInstance().onCommand(number, args)) {
                    SCTBot.CQ.sendPrivateMsg(number, "嗷，未知指令？");
                }
            }
        }


    }

}
