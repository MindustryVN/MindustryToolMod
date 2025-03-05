package mindytool.data;

import mindytool.config.Config;
import mindytool.data.TagData.TagValue;

import arc.struct.Seq;
import lombok.Data;

public class SearchConfig {
    private Seq<SelectedTag> selectedTags = new Seq<>();
    private Sort sort = Config.sorts.get(0);
    private String modId;
    private boolean changed = false;

    public void update() {
        changed = false;
    }

    public boolean isChanged() {
        return changed;
    }

    public String getSelectedTagsString() {
        return String.join(",", selectedTags.map(s -> s.categoryName + "_" + s.name));
    }

    public Seq<SelectedTag> getSelectedTags() {
        return selectedTags;
    }

    public void setTag(TagData category, TagValue value) {
        SelectedTag tag = new SelectedTag();
        tag.name = value.name;
        tag.categoryName = category.name;
        tag.icon = value.icon;

        if (selectedTags.contains(tag)) {
            this.selectedTags.remove(tag);
        } else {
            this.selectedTags.add(tag);
        }
        changed = true;
    }

    public boolean containTag(TagData category, TagValue tag) {
        return selectedTags.contains(v -> v.name.equals(tag.name) && category.name.equals(v.categoryName));
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
        changed = true;
    }

    public String getModId() {
        return modId;
    }

    public void setModId(String modId) {
        this.modId = modId;
        changed = true;
    }

    @Data
    public static class SelectedTag {
        private String name;
        private String categoryName;
        private String icon;
    }
}
