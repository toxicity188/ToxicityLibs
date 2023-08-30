package kor.toxicity.toxicitylibs.util.data;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PlayerData {
    private final List<ItemData> storageItem;
    public PlayerData(List<String> item) {
        storageItem = item.stream().map(ItemData::deserialize).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<ItemData> getStorageItem() {
        return storageItem;
    }

    public String[] getSerializedStorageItem() {
        return storageItem.stream().map(ItemData::serialize).toArray(String[]::new);
    }
}
