package org.serverct.config;

import lombok.Getter;
import org.serverct.SCTBot;
import org.serverct.data.Subscription;
import org.serverct.data.Workcheck;
import org.serverct.enums.SettingType;
import org.serverct.enums.SubscriptionTarget;
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
        Document doc = null;
        try {
            if (!settingFile.exists()) {
                String path = SCTBot.class.getClassLoader().getResource("Settings.xml").getFile().replace("file:/", "");
                SCTBot.CQ.logError("SCTBot", path);

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(new File(path));
                if (doc != null) {
                    XMLUtil.save(doc, settingFile);
                }
            } else {
                doc = XMLUtil.load(settingFile);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        if(doc != null) {
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
