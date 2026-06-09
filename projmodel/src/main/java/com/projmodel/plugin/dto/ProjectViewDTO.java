package com.projmodel.plugin.dto;

public class ProjectViewDTO {

    private final String _key;
    private final String _name;

    public ProjectViewDTO(String key, String name) {
        _key = key;
        _name = name;
    }

    public String getKey() {
        return _key;
    }

    public String getName() {
        return _name;
    }
}
