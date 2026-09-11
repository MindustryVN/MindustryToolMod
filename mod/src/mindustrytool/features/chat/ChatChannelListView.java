package mindustrytool.features.chat;

import static solim.UI.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.Button.ButtonStyle;
import arc.scene.ui.TextButton.TextButtonStyle;

import java.util.Objects;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustrytool.components.ColoredDrawable;
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
                                    channel -> new ChannelItem(channel, store))
                                            .growX();
                        } else {
                            return column().padding(unit(2)).children(() -> {
                                text(Core.bundle.get("feature.chat.ui.empty-channels", "No channels available."))
                                        .color(Color.gray)
                                        .fontScale(0.9f);
                            });
                        }
                    })
                            .growX();
                });
            });
        }).element();
    }

    private static class ChannelItem extends BaseComponent {
        private static final Color SELECTED_BG = new Color(0.45f, 0.35f, 0.9f, 0.4f);
        private static final TextButtonStyle selectedStyle = new TextButtonStyle() {
            {
                down = Styles.flatDown;
                up =new ColoredDrawable(SELECTED_BG, Tex.whiteui);
                over = Styles.flatOver;
                font = Fonts.def;
                fontColor = Color.white;
                disabledFontColor = Color.gray;
            }
        };

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

            Computed<ButtonStyle> style = isSelected.map(s -> s ? selectedStyle : Styles.cleart);

            SolimStack stack = new SolimStack()
                    .growX()
                    .height(unit(10))
                    .layer(() -> row().grow().left().children(() -> {
                        button(() -> store.setActiveChannelId(channel.getId()))
                                .style(style)
                                .margin(unit(1))
                                .left()
                                .grow()
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
