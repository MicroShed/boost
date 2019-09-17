package io.openliberty.guides.inventory.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class InventoryList {

    private List<SystemData> systems;

    public InventoryList(List<SystemData> systems) {
        this.systems = systems;
    }

    public List<SystemData> getSystems() {
        return systems;
    }

    public int getTotal() {
        return systems.size();
    }
}
