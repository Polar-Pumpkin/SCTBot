package org.serverct.config;

import lombok.NonNull;
import org.serverct.data.Department;
import org.serverct.utils.XMLUtil;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

public class DepartmentManager {

    private static DepartmentManager instance;
    public static DepartmentManager getInstance() {
        if(instance == null) {
            instance = new DepartmentManager();
        }
        return instance;
    }

    private Map<String, Department> loadDepartmentMap = new HashMap<>();

    public Map<String, Department> load(@NonNull Node settings) {
        for(Node depart : XMLUtil.getChilds(settings)) {
            String id = XMLUtil.getAttr(depart, "ID");
            String display = XMLUtil.getAttr(depart, "Display");
            loadDepartmentMap.put(id, new Department(id, display));
        }
        return loadDepartmentMap;
    }

    public Department get(String id) {
        return loadDepartmentMap.getOrDefault(id, null);
    }
}
