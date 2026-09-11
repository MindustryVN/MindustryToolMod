package mindustrytool.services.auth;

import static solim.UI.*;

import arc.Core;
import mindustry.Vars;
import solim.UI;
import solim.overlay.SolimDialog;
import solim.signal.Signal;

public class AuthLoginDialog extends SolimDialog {

	private final Signal<String> loginUrlSignal = signal();

	public AuthLoginDialog(MindustryAuthProvider authService) {
		super(Core.bundle.get("auth.login.dialog-title"));
		name("loginDialog");
		closeOnBack();

		content(() -> {
			column().grow().padding(unit(4)).center().children(() -> {
				dynamic(loginUrlSignal, url -> {
					if (url == null || url.isEmpty()) {
						return text(Core.bundle.get("auth.login.loading"));
					} else {
						return UI.button(() -> {
							Core.app.setClipboardText(url);
							Vars.ui.showInfoFade(Core.bundle.get("auth.login.copied"));
						}).children(() -> {
							text(url).fontScale(0.7f).wrap();
						});
					}
				});
			});
		});

		actionButton(Core.bundle.get("auth.login.cancel"), () -> {
			authService.cancelLogin();
			hide();
		}).setWidth(230f);
	}

	public void showLoading() {
		loginUrlSignal.set(null);
	}

	public void showLoginUrl(String loginUrl) {
		loginUrlSignal.set(loginUrl);
	}
}
