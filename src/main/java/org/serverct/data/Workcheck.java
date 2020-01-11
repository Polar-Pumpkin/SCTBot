package org.serverct.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.serverct.config.DepartmentManager;
import org.serverct.enums.workcheck.*;
import org.serverct.utils.XMLUtil;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public @Data @AllArgsConstructor class Workcheck {

    int duration;
    List<Integer> startMonth;
    Map<Department, Integer> workAmount;
    Map<Department, Integer> ppointAmount;
    Map<Integer, Punishment> punishPunishment;
    Map<Integer, String> punishMessage;
    Map<RewardTarget, Map<Object, Integer>> rewardMap;

    public Workcheck(Node element) {
        duration = Integer.parseInt(XMLUtil.getAttr(XMLUtil.getNode(element, "Duration"), "Month"));

        startMonth = new ArrayList<>();
        Node startTime = XMLUtil.getNode(element, "Timepoints");
        for(Node timepoint : XMLUtil.getChilds(startTime)) {
            startMonth.add(Integer.parseInt(XMLUtil.getAttr(timepoint, "Month")));
        }

        workAmount = new HashMap<>();
        ppointAmount = new HashMap<>();
        Node requirements = XMLUtil.getNode(element, "Requirements");
        for(Node requirement : XMLUtil.getChilds(requirements)) {
            Department target = DepartmentManager.getInstance().get(XMLUtil.getAttr(requirement, "Department"));
            workAmount.put(target, Integer.parseInt(XMLUtil.getAttr(requirement, "Amount")));
            ppointAmount.put(target, Integer.parseInt(XMLUtil.getAttr(requirement, "PPoint")));
        }

        punishPunishment = new HashMap<>();
        punishMessage = new HashMap<>();
        Node punishments = XMLUtil.getNode(element, "Punishments");
        for(Node punishment : XMLUtil.getChilds(punishments)) {
            int times = Integer.parseInt(XMLUtil.getAttr(punishment, "Times"));
            punishPunishment.put(times, Punishment.valueOf(XMLUtil.getAttr(punishment, "Type").toUpperCase()));
            punishMessage.put(times, XMLUtil.getAttr(punishment, "Message"));
        }

        rewardMap = new HashMap<>();
        Node rewards = XMLUtil.getNode(element, "Rewards");
        for(Node reward : XMLUtil.getChilds(rewards)) {
            RewardTarget target = RewardTarget.valueOf(XMLUtil.getAttr(reward, "Target").toUpperCase());
            String type = XMLUtil.getAttr(reward, "Type");
            int ppoint = Integer.parseInt(XMLUtil.getAttr(reward, "PPoint"));

            Map<Object, Integer> targetMap = rewardMap.getOrDefault(target, new HashMap<>());
            switch (target) {
                case RATING:
                    Rating rating = Rating.valueOf(type);
                    targetMap.put(rating, ppoint);
                    break;
                case HIGHLIGHT:
                    Highlight highlight = Highlight.valueOf(type);
                    targetMap.put(highlight, ppoint);
                    break;
                case TAG:
                    Tag tag = Tag.valueOf(type);
                    targetMap.put(tag, ppoint);
                    break;
                default:
                    break;
            }
            rewardMap.put(target, targetMap);
        }
    }
}
