package org.serverct.config;

import lombok.Getter;
import org.serverct.SCTBot;
import org.serverct.data.Subscription;
import org.serverct.data.Workcheck;
import org.serverct.enums.SettingType;
import org.serverct.enums.SubscriptionTarget;
import org.serverct.utils.BasicUtil;
import org.serverct.utils.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private static ConfigManager instance;
    public static ConfigManager getInstance() {
        if(instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    @Getter private Map<SettingType, Object> settings = new HashMap<>();
    private File settingFile = new File(SCTBot.CQ.getAppDirectory() + File.separator + "Settings.xml");

    public void load() {
        BasicUtil.debug("> 正在读取文件 -> 机器人配置...", true);
        Document doc = null;
        try {
            if (!settingFile.exists()) {
                BasicUtil.debug("  > 机器人配置文件不存在或无效, 尝试生成默认配置文件.", true);
                String path = SCTBot.class.getClassLoader().getResource("Settings.xml").getFile().replace("file:/", "");
                BasicUtil.debug("    > 默认配置文件目录: " + path, true);

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                File defaultSettings = new File(path);
                String[] report = {
                        "    > 获取默认配置文件的本地化文件对象...",
                        "      > 文件名: " + defaultSettings.getName(),
                        "      > 大小: " + defaultSettings.getUsableSpace() + "/" + defaultSettings.getTotalSpace(),
                        "      > 绝对路径: " + defaultSettings.getAbsolutePath(),
                };
                BasicUtil.debug(report, true);
                doc = db.parse(defaultSettings);
                BasicUtil.debug("  > 读取默认配置文件(XML)...", true);
                if (doc != null) {
                    XMLUtil.save(doc, settingFile);
                    BasicUtil.debug("  > 读取数据不为 Null, 生成至插件数据文件目录: " + settingFile.getAbsolutePath(), true);
                }
            } else {
                BasicUtil.debug("  > 机器人配置文件有效, 尝试读取...", true);
                doc = XMLUtil.load(settingFile);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        if(doc != null) {
            BasicUtil.debug("  > 配置文件数据读取成功, 正在加载并构造对象...", true);
            BasicUtil.debug(doc.toString(), true);

            Element root = doc.getDocumentElement();

            List<Subscription> subscriptionList = new ArrayList<>();
            Node subscriptions = XMLUtil.getNode(root, "Subscriptions");
            for(Node subscription : XMLUtil.getChilds(subscriptions)) {
                subscriptionList.add(
                        new Subscription(
                                SubscriptionTarget.valueOf(XMLUtil.getAttr(subscription, "Target").toUpperCase()),
                                Integer.parseInt(XMLUtil.getAttr(XMLUtil.getNode(subscription, "Interval"), "Hour")),
                                XMLUtil.getNode(subscription, "Broadcast").getTextContent()
                        )
                );
            }
            settings.put(SettingType.SUBSCRIPTIONS, subscriptionList);
            settings.put(SettingType.DEPARTMENTS, DepartmentManager.getInstance().load(XMLUtil.getNode(root, "Departments")));
            settings.put(SettingType.GROUPCARD, XMLUtil.getNode(root, "GroupCard").getTextContent());
            settings.put(SettingType.WORKCHECK, new Workcheck(XMLUtil.getNode(root, "Workcheck")));
            List<Long> adminList = new ArrayList<>();
            Node admins = XMLUtil.getNode(root, "Admins");
            for(Node admin : XMLUtil.getChilds(admins)) {
                adminList.add(Long.parseLong(XMLUtil.getAttr(admin, "QQ")));
            }
            settings.put(SettingType.ADMINS, adminList);
        } else {
            SCTBot.CQ.sendGroupMsg(SCTBot.SCT, "草，我找不到我的配置文件了!");
        }
    }

    public Object get(SettingType type) {
        return settings.getOrDefault(type, null);
    }
}
