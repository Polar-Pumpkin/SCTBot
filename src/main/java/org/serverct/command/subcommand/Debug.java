package org.serverct.command.subcommand;

import org.serverct.SCTBot;
import org.serverct.command.Subcommand;
import org.serverct.config.ConfigManager;
import org.serverct.config.MemberManager;
import org.serverct.utils.BasicUtil;

public class Debug implements Subcommand {
    @Override
    public boolean execute(long qq, String[] args) {
        switch(args[1]) {
            case "c":
            case "command":
            case "解析":
                /*
                args[-] ~
                args[0] debug
                args[1] command
                args[2] commands...
                 */
                StringBuilder commandBuilder = new StringBuilder();
                for(int index = 2;index < args.length;index++) {
                    commandBuilder.append(args[index]).append(" ");
                }

                String command = commandBuilder.toString();
                StringBuilder result = new StringBuilder("命令解析 | Debug\n");
                result.append("目标指令: \n").append(command).append("\n");
                String[] targetArgs = BasicUtil.buildParameter(command.split(" "));
                result.append("参数长度: ").append(targetArgs.length).append("\n");
                result.append("命令: ").append(targetArgs[0]).append("\n");
                result.append("子命令: ").append(targetArgs[1]).append("\n");
                result.append("参数: ");
                for(int index = 3;index < targetArgs.length;index++) {
                    result.append(targetArgs[index]).append(" ");
                }

                SCTBot.CQ.sendGroupMsg(SCTBot.SCT, result.toString());
                break;
            case "o":
            case "output":
            case "输出":
                switch (args[2]) {
                    case "p":
                    case "path":
                    case "配置目录":
                        SCTBot.CQ.sendGroupMsg(SCTBot.SCT, MemberManager.getInstance().getDataFolder().getAbsolutePath());
                        break;
                    case "c":
                    case "config":
                    case "配置文件":
                        SCTBot.CQ.sendGroupMsg(SCTBot.SCT, ConfigManager.getInstance().getSettings().toString());
                        break;
                    default:
                        SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "未定义输出信息类型.");
                        break;
                }
                break;
            default:
                break;
        }
        return true;
    }
}
