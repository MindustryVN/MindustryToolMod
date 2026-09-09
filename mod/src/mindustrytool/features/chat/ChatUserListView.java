package mindustrytool.features.chat;

import static solim.ui.Ui.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustrytool.models.response.ChatUser;
import solim.core.BaseComponent;
import solim.signal.Readable;

public class ChatUserListView extends BaseComponent {

    private final ChatStore store;

    public ChatUserListView(ChatStore store) {
        this.store = store;
    }

    @Override
    protected Element build() {
        Readable<Boolean> hasUsers = store.activeUsers()
                .map(list -> list != null && !list.isEmpty());

        return column()
                .grow()
                .gap(unit(1))
                .children(() -> {
                    row()
                            .growX()
                            .padding(unit(1))
                            .children(() -> {
                                text(Core.bundle.get("feature.chat.ui.members", "Members"))
                                        .color(Pal.accent)
                                        .fontScale(1.1f)
                                        .left();
                            });

                    divider();

                    scroll()
                            .grow()
                            .children(() -> {
                                column()
                                        .growX()
                                        .gap(unit(1))
                                        .children(() -> {
                                            dynamic(hasUsers, available -> {
                                                if (Boolean.TRUE.equals(available)) {
                                                    return forEach(store.activeUsers(), ChatUser::getName,
                                                            UserItem::new);
                                                } else {
                                                    return column()
                                                            .padding(unit(2))
                                                            .children(() -> {
                                                                text(Core.bundle.get("feature.chat.ui.empty-members",
                                                                        "No members online."))
                                                                                .color(Color.gray)
                                                                                .fontScale(0.9f);
                                                            });
                                                }
                                            });
                                        });
                            });
                })
                .element();
    }

    private static class UserItem extends BaseComponent {
        private final ChatUser user;

        public UserItem(ChatUser user) {
            this.user = user;
        }

        @Override
        protected Element build() {
            String name = user.getName() != null ? user.getName() : "Anonymous";
            Color roleColor = Color.white;
            if (user.getHighestRole()
                    .isPresent()) {
                try {
                    String colorHex = user.getHighestRole()
                            .get()
                            .getColor();
                    if (colorHex != null && !colorHex.isEmpty()) {
                        roleColor = Color.valueOf(colorHex);
                    }
                } catch (Exception ignored) {
                }
            }

            final Color finalRoleColor = roleColor;
                    return card().growX()
                            .children(() -> {
                                row().growX()
                                        .padding(unit(1))
                                        .gap(unit(1))
                                        .children(() -> {
                                            networkImage(user.getImageUrl())
                                                    .placeholder(Icon.players)
                                                    .fallback(Icon.players)
                                                    .size(unit(6), unit(6));

                                            text(name).color(finalRoleColor)
                                                    .fontScale(0.9f)
                                                    .left();
                                        });
                            })
                            .element();
        }
    }
}
