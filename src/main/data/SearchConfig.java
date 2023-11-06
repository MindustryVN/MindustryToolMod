package main.data;

import java.util.ArrayList;
import java.util.List;

import main.config.Config;

public class SearchConfig {
    private List<String> selectedTags = new ArrayList<>();
    private Sort sort = Config.sorts.get(0);
    private boolean changed = false;

    public void update() {
        changed = false;
    }

    public boolean isChanged() {
        return changed;
    }

    public String getSelectedTagsString() {
        return String.join(",", selectedTags);
    }

    public List<String> getSelectedTags() {
        return selectedTags;
    }

    public void setTag(String tag) {
        if (selectedTags.contains(tag)) {
            this.selectedTags.remove(tag);
        } else {
            this.selectedTags.add(tag);
        }
        changed = true;
    }

    public boolean containTag(String tag) {
        return selectedTags.contains(tag);
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
        changed = true;
    }
}
