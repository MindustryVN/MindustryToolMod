package mindustrytool.features.chat;

import static solim.UI.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import java.util.Objects;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustrytool.models.response.ChannelDto;
import solim.core.BaseComponent;
import solim.layout.SolimStack;
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
        private static final Color SELECTED_BG = new Color(0.45f, 0.35f, 0.9f, 0.4f);

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
            Readable<Boolean> hasUnread = store.channelUnread(channel.getId())
                    .map(count -> count != null && count > 0);

            SolimStack stack = new SolimStack()
                    .layer(() -> image(Tex.whiteui).grow()
                            .color(isSelected.map(sel -> Boolean.TRUE.equals(sel) ? SELECTED_BG : Color.clear)))
                    .layer(() -> row().growX().padding(unit(1)).left().gap(unit(1)).children(() -> {
                        button(() -> store.setActiveChannelId(channel.getId()))
                                .style(Styles.cleart)
                                .left()
                                .growX()
                                .children(() -> {
                                    text("# " + channel.getName())
                                            .left()
                                            .color(isSelected.map(sel -> Boolean.TRUE.equals(sel) ? Pal.accent
                                                    : Color.white));
                                });
                        image(Tex.whiteui)
                                .size(unit(1.5f), unit(1.5f))
                                .color(Pal.heal)
                                .visible(hasUnread);
                    }));
            return stack.element();
        }
    }
}
