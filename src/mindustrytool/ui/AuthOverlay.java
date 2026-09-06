package mindustrytool.ui;

import arc.Core;
import arc.Events;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustrytool.events.LoginUriEvent;
import mindustrytool.events.SessionLoadEvent;
import mindustrytool.services.MindustryAuthProvider;

public class AuthOverlay {
    private static AuthOverlay instance;

    private Table authWindow;
    private AuthLoginDialog loginDialog;

    public static AuthOverlay getInstance() {
        if (instance == null) {
            instance = new AuthOverlay();
        }
        return instance;
    }

    private AuthOverlay() {
    }

    public void init() {
        initUi();
    }

    public void initUi() {
        if (Vars.ui == null) {
            Log.info("AuthOverlay init skipped: headless");
            return;
        }

        var wholeViewport = new Table();
        wholeViewport.name = "authWindow";
        wholeViewport.setFillParent(true);
        wholeViewport.top().right();

        authWindow = wholeViewport.table().get();

        authWindow.top().right();
        authWindow.touchable = Touchable.childrenOnly;

        if (Vars.ui.menuGroup == null) {
            Log.info("AuthOverlay init skipped: menuGroup null");
            return;
        }

        Core.app.post(() -> Vars.ui.menuGroup.addChild(wholeViewport));

        Table content = new Table();
        content.setBackground(Styles.black6);

        authWindow.add(content).top().right().margin(8f);
        authWindow.toFront();

        Events.on(SessionLoadEvent.class, e -> {
            var user = e.user;
            var error = e.error;
            var isLoading = e.isLoading;

            if (isLoading) {
                content.clear();
                content.add(Core.bundle.get("auth.session.loading")).wrapLabel(false).labelAlign(Align.left).padLeft(8);
            } else if (error != null) {
                content.clear();
                content.add(Core.bundle.get("auth.session.error")).labelAlign(Align.left).padLeft(8);
                content.add(error.getLocalizedMessage()).labelAlign(Align.left).padLeft(8).row();
                content.button(Core.bundle.get("auth.session.retry"), Icon.refresh, this::startLoginUI);

                Log.err("Failed to load session", error);
            } else if (user == null) {
                content.clear();
                content.button(Core.bundle.get("auth.login"), this::startLoginUI).wrapLabel(false);
            } else {
                content.clear();

                if (user.getImageUrl() != null) {
                    content.add(new NetworkImage(user.getImageUrl())).size(64);
                }

                if (!Vars.mobile) {
                    content.add(user.getName()).labelAlign(Align.left).padLeft(8);
                }

                content.touchable = Touchable.enabled;
                content.clicked(() -> {
                    Vars.ui.showConfirm(Core.bundle.get("auth.logout.confirm-title"),
                            Core.bundle.format("auth.logout.confirm-message", user.getName()),
                            MindustryAuthProvider.getInstance()::logout);
                });
            }

            content.pack();
        });

        Events.on(LoginUriEvent.class, e -> {
            if (loginDialog != null) {
                Core.app.post(() -> loginDialog.showLoginUrl(e.loginUrl));
            }
        });
    }

    private void startLoginUI() {
        MindustryAuthProvider auth = MindustryAuthProvider.getInstance();

        if (loginDialog == null) {
            loginDialog = new AuthLoginDialog(auth);
        }

        Core.app.post(() -> {
            loginDialog.showLoading();
            loginDialog.show();
        });

        auth.login()
                .thenRun(() -> Core.app.post(() -> {
                    if (loginDialog != null) loginDialog.hide();
                    Vars.ui.showInfo(Core.bundle.get("auth.login.success"));
                }))
                .exceptionally(e -> {
                    Core.app.post(() -> {
                        if (loginDialog != null) loginDialog.hide();
                        Vars.ui.showException(Core.bundle.get("auth.login.failed"), e);
                    });
                    return null;
                });

        // Update dialog with login URL via LoginUriEvent fired by MindustryAuthProvider.
    }

    public void showLoading() {
        if (loginDialog != null) {
            Core.app.post(() -> loginDialog.showLoading());
        }
    }

    public void showLoginUrl(String loginUrl) {
        if (loginDialog != null) {
            Core.app.post(() -> loginDialog.showLoginUrl(loginUrl));
        }
    }
}
