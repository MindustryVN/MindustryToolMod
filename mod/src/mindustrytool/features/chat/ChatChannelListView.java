package mindustrytool.features.chat;

import static solim.ui.Ui.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import java.util.Objects;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustrytool.models.response.ChannelDto;
import solim.core.BaseComponent;
import solim.signal.Computed;
import solim.signal.Readable;

public class ChatChannelListView extends BaseComponent {

    private final ChatStore store;

    public ChatChannelListView(ChatStore store) {
        this.store = store;
    }

    @Override
    protected Element build() {
        Readable<Boolean> hasChannels = store.channels().map(list -> list != null && !list.isEmpty());

        return column().grow().gap(unit(1)).children(() -> {
            row().growX().padding(unit(1)).children(() -> {
                text(Core.bundle.get("feature.chat.ui.channels", "Channels"))
                        .color(Pal.accent)
                        .fontScale(1.1f)
                        .left();
            });

            divider();

            scroll().grow().children(() -> {
                column().growX().gap(unit(1)).children(() -> {
                    dynamic(hasChannels, available -> {
                        if (Boolean.TRUE.equals(available)) {
                            return forEach(store.channels(), ChannelDto::getId,
                                    channel -> new ChannelItem(channel, store));
                        } else {
                            return column().padding(unit(2)).children(() -> {
                                text(Core.bundle.get("feature.chat.ui.empty-channels", "No channels available."))
                                        .color(Color.gray)
                                        .fontScale(0.9f);
                            });
                        }
                    });
                });
            });
        }).element();
    }

    private static class ChannelItem extends BaseComponent {
        private final ChannelDto channel;
        private final ChatStore store;

        public ChannelItem(ChannelDto channel, ChatStore store) {
            this.channel = channel;
            this.store = store;
        }

        @Override
        protected Element build() {
            Computed<Boolean> isSelected = new Computed<>(
                    () -> Objects.equals(store.activeChannelId().get(), channel.getId()));

            return card().growX().children(() -> {
                row().growX().padding(unit(1)).left().children(() -> {
                    button(() -> store.setActiveChannelId(channel.getId()))
                            .style(Styles.cleart)
                            .left()
                            .growX()
                            .children(() -> {
                                text("# " + channel.getName())
                                        .left()
                                        .color(isSelected.map(sel -> sel ? Pal.accent : Color.white));
                            });
                });
            }).element();
        }
    }
}
