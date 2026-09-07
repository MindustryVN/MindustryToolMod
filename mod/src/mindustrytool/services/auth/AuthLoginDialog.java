package mindustrytool.services.auth;

import arc.Core;
import mindustry.Vars;
import mindustry.ui.dialogs.BaseDialog;

public class AuthLoginDialog extends BaseDialog {
	public AuthLoginDialog(MindustryAuthProvider authService) {
		super(Core.bundle.get("auth.login.dialog-title"));
		name = "loginDialog";

		buttons.button(Core.bundle.get("auth.login.cancel"), () -> {
					authService.cancelLogin();
					hide();
				})
				.width(230);
	}

	public void showLoading() {
		cont.clear();
		cont.add(Core.bundle.get("auth.login.loading"));
	}

	public void showLoginUrl(String loginUrl) {
		cont.clear();
		cont.button(loginUrl, () -> {
					Core.app.setClipboardText(loginUrl);
					Vars.ui.showInfoFade(Core.bundle.get("auth.login.copied"));
				})
				.margin(40)
				.growX()
				.wrapLabel(true)
				.fontScale(0.5f);
	}
}
