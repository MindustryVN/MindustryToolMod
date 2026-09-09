package mindustrytool.features.translation.ui;

import static solim.ui.Ui.*;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustrytool.features.translation.TranslationFeature;
import mindustrytool.features.translation.TranslationProvider;
import mindustrytool.features.translation.providers.DeepLTranslationProvider;
import mindustrytool.features.translation.providers.DevXTranslationProvider;
import mindustrytool.features.translation.providers.GeminiTranslationProvider;
import mindustrytool.features.translation.providers.MindustryToolTranslationProvider;
import mindustrytool.services.auth.AuthLoginDialog;
import mindustrytool.services.auth.MindustryAuthProvider;
import solim.core.BaseComponent;
import solim.core.Component;
import solim.signal.Computed;
import solim.signal.Readable;
import solim.signal.Signal;

/**
 * Declarative Solim settings view for Chat Translation feature.
 * Built according to ui-ux-pro-max design intelligence standards.
 */
public class TranslationSettingsView extends BaseComponent {

	private final TranslationFeature feature;
	private final Signal<String> testInputSignal = Signal.of("Hello from Mindustry! How is the defense going?");
	private final Signal<String> testResultSignal = Signal.of(
			Core.bundle.get("feature.translation.test.result-placeholder", "Translation result will appear here..."));
	private final Signal<Boolean> isTestingSignal = Signal.of(false);
	private final Signal<Boolean> testSuccessSignal = Signal.of(true);

	public TranslationSettingsView(TranslationFeature feature) {
		this.feature = feature;
	}

	@Override
	protected Element build() {
		return column().grow().children(() -> {
			scroll().grow().children(() -> {
				column().growX().padding(unit(3)).gap(unit(3)).children(() -> {

					// ─── 1. Header & Master Control Card ────────────
					card(Styles.defaultb, () -> {
						column().growX().padding(unit(3)).gap(unit(2)).children(() -> {
							row().growX().children(() -> {
								column().left().growX().gap(unit(0.5f)).children(() -> {
									text("[#58a6ff]✦ [white]" + Core.bundle.get("feature.translation.master.title", "Chat Translation"))
											.style(Styles.outlineLabel).fontScale(1.1f).left();
									text(Core.bundle.get("feature.translation.master.desc", "Real-time neural translation for in-game multiplayer chat."))
											.color(Color.gray).fontScale(0.85f).left();
								});

								// Master Toggle Button
								Readable<Boolean> isEnabled = feature.enabled();
								button(() -> feature.setEnabled(!Boolean.TRUE.equals(feature.enabled().peek())))
										.style(Styles.defaultb)
										.height(unit(8))
										.children(() -> {
											Readable<String> toggleText = isEnabled.map(en ->
													Boolean.TRUE.equals(en)
															? "[#a9d88a]● " + Core.bundle.get("feature.translation.master.enabled", "Enabled")
															: "[gray]○ " + Core.bundle.get("feature.translation.master.disabled", "Disabled"));
											text(toggleText);
										});
							});

							// Status Pills Row
							row().growX().gap(unit(2)).children(() -> {
								// System detected language pill
								card(Styles.black3, () -> {
									row().padding(unit(1.5f)).gap(unit(1)).children(() -> {
										text(Core.bundle.get("feature.translation.pref.target-lang", "Target Language:"))
												.color(Color.lightGray).fontScale(0.85f);
										text(feature.getTargetLanguage()).color(Pal.accent).fontScale(0.85f);
									});
								});

								// Active Provider pill
								Readable<String> activeProviderName = feature.providerConfig.signal().map(id -> {
									for (TranslationProvider p : feature.getProviders()) {
										if (p.getId().equals(id)) return p.getName();
									}
									return id != null ? id : "";
								});
								card(Styles.black3, () -> {
									row().padding(unit(1.5f)).gap(unit(1)).children(() -> {
										text("Engine:").color(Color.lightGray).fontScale(0.85f);
										text(activeProviderName).color(Pal.heal).fontScale(0.85f);
									});
								});
							});
						});
					}).growX();

					divider();

					// ─── 2. Provider Selector Cards ─────────────────
					text(Core.bundle.get("feature.translation.settings.providers", "Translation Provider"))
							.color(Color.lightGray).left();

					grid(dvw(90f).map(w -> (w != null && w > 750f) ? 4 : 2)).growX().gap(unit(2)).children(() -> {
						for (TranslationProvider provider : feature.getProviders()) {
							Readable<Boolean> isSelected = feature.providerConfig.signal().map(id -> provider.getId().equals(id));
							Readable<Color> borderColor = isSelected.map(sel -> sel ? Pal.accent : Pal.darkerGray);

							String iconPrefix;
							String subtitle;
							if (GeminiTranslationProvider.ID.equals(provider.getId())) {
								iconPrefix = "[#4285f4]✦ ";
								subtitle = "Google AI Studio";
							} else if (DevXTranslationProvider.ID.equals(provider.getId())) {
								iconPrefix = "[#76b900]⚡ ";
								subtitle = "NVIDIA AI (Built-in)";
							} else if (DeepLTranslationProvider.ID.equals(provider.getId())) {
								iconPrefix = "[#00bcd4]🌐 ";
								subtitle = "DeepL Neural MT";
							} else {
								iconPrefix = "[#e67e22]☁ ";
								subtitle = "Official Cloud";
							}

							card(Styles.defaultb, () -> {
								column().growX().padding(unit(2)).gap(unit(1)).center().children(() -> {
									text(iconPrefix + provider.getName())
											.color(isSelected.map(sel -> sel ? Pal.accent : Color.white))
											.style(Styles.outlineLabel);

									text(subtitle)
											.color(Color.gray)
											.fontScale(0.78f);

									// Status badge
									Readable<String> statusText = isSelected.map(sel -> {
										if (sel) {
											return "[#58a6ff]● [stat]" + Core.bundle.get("feature.translation.status.active", "Active");
										} else if (provider.isConfigured()) {
											return "[#a9d88a]● [lightgray]" + Core.bundle.get("feature.translation.status.ready", "Ready");
										} else {
											return "[gray]○ " + Core.bundle.get("feature.translation.status.no-key", "No Key");
										}
									});
									text(statusText).fontScale(0.85f);
								});
							})
							.growX()
							.color(borderColor)
							.onClick(() -> feature.providerConfig.set(provider.getId()));
						}
					});

					// ─── 3. Dynamic Sub-panel for Selected Provider ─
					dynamic(feature.providerConfig.signal(), this::buildProviderPanel).growX();

					divider();

					// ─── 4. Bento Section: Incoming & Outgoing Chat ─
					grid(dvw(90f).map(w -> (w != null && w > 820f) ? 2 : 1)).growX().gap(unit(3)).children(() -> {
						// Card A: Incoming Chat
						card(Styles.defaultb, () -> {
							column().growX().padding(unit(3)).gap(unit(2)).left().children(() -> {
								text("[#58a6ff]📥 " + Core.bundle.get("feature.translation.incoming.title", "Incoming Chat"))
										.color(Pal.accent).left();

								checkbox(
										Core.bundle.get("feature.translation.pref.show-original", "Show original message"),
										feature.showOriginalConfig.signal()
								);

								// Live message preview card
								card(Styles.black3, () -> {
									column().growX().padding(unit(2)).gap(unit(1)).left().children(() -> {
										text(Core.bundle.get("feature.translation.incoming.preview-title", "Live Message Preview"))
												.color(Color.lightGray).fontScale(0.82f).left();
										text(Core.bundle.get("feature.translation.incoming.preview-desc", "Shows how other players' messages appear in your chat."))
												.color(Color.gray).fontScale(0.75f).left();

										Readable<String> previewText = feature.showOriginalConfig.signal().map(show ->
												Boolean.TRUE.equals(show)
														? "[#58a6ff]Alex: [white]Hello team! [gold](Chào cả đội!)[white]"
														: "[#58a6ff]Alex: [gold][Chào cả đội!][white]");
										text(previewText).style(Styles.outlineLabel).left();
									});
								}).growX();
							});
						}).growX();

						// Card B: Outgoing Chat
						card(Styles.defaultb, () -> {
							column().growX().padding(unit(3)).gap(unit(2)).left().children(() -> {
								text("[#ffd37f]📤 " + Core.bundle.get("feature.translation.outgoing.title", "Outgoing Chat"))
										.color(Pal.accent).left();

								checkbox(
										Core.bundle.get("feature.translation.outgoing.enable", "Translate outgoing chat messages automatically"),
										feature.outgoingEnabledConfig.signal()
								);

								text(Core.bundle.get("feature.translation.outgoing.target-lang", "Outgoing Target Language"))
										.color(Color.lightGray).left();

								// Quick language selector chips
								grid(dvw(90f).map(w -> (w != null && w > 900f) ? 6 : 3)).growX().gap(unit(1)).children(() -> {
									String[] popularLangs = {"English", "Vietnamese", "Chinese", "Russian", "Japanese", "Spanish"};
									for (String lang : popularLangs) {
										Readable<Boolean> isCurrent = feature.outgoingTargetLangConfig.signal().map(lang::equalsIgnoreCase);
										button(lang, () -> feature.outgoingTargetLangConfig.set(lang))
												.style(Styles.togglet)
												.checked(isCurrent)
												.height(unit(7))
												.growX();
									}
								});

								// Custom language textfield
								textField(feature.outgoingTargetLangConfig.signal())
										.placeholder(Core.bundle.get("feature.translation.outgoing.target-lang.hint", "e.g. English, Chinese..."))
										.growX();

								// Outgoing format
								text(Core.bundle.get("feature.translation.outgoing.format", "Outgoing Format"))
										.color(Color.lightGray).left();

								row().growX().gap(unit(2)).children(() -> {
									Readable<Boolean> isBoth = feature.outgoingFormatConfig.signal().map(f -> !"translated_only".equalsIgnoreCase(f));
									Readable<Boolean> isOnly = feature.outgoingFormatConfig.signal().map("translated_only"::equalsIgnoreCase);

									button(Core.bundle.get("feature.translation.outgoing.format.both", "Translated (Original)"),
											() -> feature.outgoingFormatConfig.set("both"))
											.style(Styles.togglet)
											.checked(isBoth)
											.height(unit(7))
											.growX();

									button(Core.bundle.get("feature.translation.outgoing.format.translated-only", "Translated Only"),
											() -> feature.outgoingFormatConfig.set("translated_only"))
											.style(Styles.togglet)
											.checked(isOnly)
											.height(unit(7))
											.growX();
								});

								// Live preview box
								card(Styles.black3, () -> {
									column().growX().padding(unit(2)).gap(unit(1)).left().children(() -> {
										text(Core.bundle.get("feature.translation.outgoing.preview-title", "Live Message Preview"))
												.color(Color.lightGray).fontScale(0.82f).left();
										text(Core.bundle.get("feature.translation.outgoing.preview-desc", "Shows how your messages will be sent to the server."))
												.color(Color.gray).fontScale(0.75f).left();

										Readable<String> previewText = feature.outgoingFormatConfig.signal().map(fmt ->
												"translated_only".equalsIgnoreCase(fmt)
														? "[#ffd37f]You: [white]Defense is ready!"
														: "[#ffd37f]You: [white]Defense is ready! [gray](Phòng thủ đã xong!)");
										text(previewText).style(Styles.outlineLabel).left();
									});
								}).growX();

								// Hint callout card
								card(Styles.black3, () -> {
									row().growX().padding(unit(1.5f)).gap(unit(1)).children(() -> {
										text("[accent]💡 [lightgray]" + Core.bundle.get("feature.translation.outgoing.hint", "Tip: Type // to send original without translating, or /tr <text> to translate on demand."))
												.fontScale(0.82f).wrap().growX();
									});
								}).growX();
							});
						}).growX();
					});

					divider();

					// ─── 5. Connection Tester Widget ────────────────
					card(Styles.defaultb, () -> {
						column().growX().padding(unit(3)).gap(unit(2)).children(() -> {
							buildTesterWidget();
						});
					}).growX();
				});
			});
		}).element();
	}

	private Component buildProviderPanel(String providerId) {
		if (GeminiTranslationProvider.ID.equals(providerId)) {
			return buildGeminiPanel();
		} else if (DevXTranslationProvider.ID.equals(providerId)) {
			return buildDevXPanel();
		} else if (DeepLTranslationProvider.ID.equals(providerId)) {
			return buildDeepLPanel();
		} else if (MindustryToolTranslationProvider.ID.equals(providerId)) {
			return buildMindustryToolPanel();
		}
		return column();
	}

	private Component buildDevXPanel() {
		return card(Styles.defaultb, () -> {
			column().growX().padding(unit(3)).gap(unit(2)).children(() -> {
				row().growX().children(() -> {
					text("[#76b900]⚡ [white]DevX (NVIDIA AI)").left().growX();
					text("[#76b900]● BUILT-IN").right().fontScale(0.85f);
				});

				// Built-in info card
				card(Styles.black3, () -> {
					row().growX().padding(unit(2)).gap(unit(1)).children(() -> {
						text("[#76b900]✦ [lightgray]" + Core.bundle.get("feature.translation.devx.desc", "Powered by NVIDIA NIM architecture. Pre-configured and ready to use without any setup."))
								.fontScale(0.85f).wrap().growX();
					});
				}).growX();

				// Timeout slider
				row().growX().gap(unit(2)).children(() -> {
					Computed<String> timeoutLabel = feature.devxTimeoutConfig.signal()
							.map(t -> Core.bundle.format("feature.translation.devx.timeout", t != null ? t : 10));
					text(timeoutLabel).left().growX();
					row().width(unit(35)).right().children(() -> {
						slider(feature.devxTimeoutConfig.signal(), 3, 30, 1);
					});
				});

				// History context slider
				row().growX().gap(unit(2)).children(() -> {
					Computed<String> historyLabel = feature.devxMaxHistoryConfig.signal()
							.map(h -> Core.bundle.format("feature.translation.devx.history", h != null ? h : 5));
					text(historyLabel).left().growX();
					row().width(unit(35)).right().children(() -> {
						slider(feature.devxMaxHistoryConfig.signal(), 0, 10, 1);
					});
				});
			});
		}).growX();
	}

	private Component buildGeminiPanel() {
		return card(Styles.defaultb, () -> {
			column().growX().padding(unit(3)).gap(unit(2)).children(() -> {
				row().growX().children(() -> {
					text("[#4285f4]✦ [white]Google Gemini").left().growX();
					button(Core.bundle.get("feature.translation.gemini.get-key", "Get Key"), () -> {
						Core.app.openURI("https://aistudio.google.com/app/apikey");
					}).style(Styles.flatt).height(unit(6));
				});

				// API key warning if missing
				Readable<Boolean> hasKey = feature.geminiApiKeyConfig.signal().map(k -> k != null && !k.trim().isEmpty());
				dynamic(hasKey, present -> {
					if (Boolean.FALSE.equals(present)) {
						return card(Styles.black3, () -> {
							row().growX().padding(unit(2)).gap(unit(1)).children(() -> {
								text("[#ffd37f]⚠ [lightgray]" + Core.bundle.get("feature.translation.gemini.no-key-warning", "API Key required. Click 'Get Key' to generate a free key from Google AI Studio."))
										.fontScale(0.85f).wrap().growX();
							});
						}).growX();
					}
					return row();
				}).growX();

				// API Key
				text(Core.bundle.get("feature.translation.gemini.api-key", "Gemini API Key")).left().color(Color.lightGray);
				textField(feature.geminiApiKeyConfig.signal())
						.placeholder(Core.bundle.get("feature.translation.gemini.api-key.hint", "AIzaSy..."))
						.growX();

				// Model selection
				text(Core.bundle.get("feature.translation.gemini.model", "Model")).left().color(Color.lightGray);
				row().growX().gap(unit(1)).children(() -> {
					for (String model : GeminiTranslationProvider.MODELS) {
						Readable<Boolean> isCurrent = feature.geminiModelConfig.signal().map(model::equals);
						button(model, () -> feature.geminiModelConfig.set(model))
								.style(Styles.togglet)
								.checked(isCurrent)
								.height(unit(7))
								.growX();
					}
				});

				// Timeout slider
				row().growX().gap(unit(2)).children(() -> {
					Computed<String> timeoutLabel = feature.geminiTimeoutConfig.signal()
							.map(t -> Core.bundle.format("feature.translation.gemini.timeout", t != null ? t : 10));
					text(timeoutLabel).left().growX();
					row().width(unit(35)).right().children(() -> {
						slider(feature.geminiTimeoutConfig.signal(), 3, 30, 1);
					});
				});

				// History context slider
				row().growX().gap(unit(2)).children(() -> {
					Computed<String> historyLabel = feature.geminiMaxHistoryConfig.signal()
							.map(h -> Core.bundle.format("feature.translation.gemini.history", h != null ? h : 5));
					text(historyLabel).left().growX();
					row().width(unit(35)).right().children(() -> {
						slider(feature.geminiMaxHistoryConfig.signal(), 0, 10, 1);
					});
				});
			});
		}).growX();
	}

	private Component buildDeepLPanel() {
		return card(Styles.defaultb, () -> {
			column().growX().padding(unit(3)).gap(unit(2)).children(() -> {
				row().growX().children(() -> {
					text("[#00bcd4]🌐 [white]DeepL Neural MT").left().growX();
					button(Core.bundle.get("feature.translation.deepl.portal", "DeepL API Portal"), () -> {
						Core.app.openURI("https://www.deepl.com/pro-api");
					}).style(Styles.flatt).height(unit(6));
				});

				// API key warning if missing
				Readable<Boolean> hasKey = feature.deeplApiKeyConfig.signal().map(k -> k != null && !k.trim().isEmpty());
				dynamic(hasKey, present -> {
					if (Boolean.FALSE.equals(present)) {
						return card(Styles.black3, () -> {
							row().growX().padding(unit(2)).gap(unit(1)).children(() -> {
								text("[#ffd37f]⚠ [lightgray]" + Core.bundle.get("feature.translation.deepl.no-key-warning", "DeepL API Key required. Free or Pro authentication key ending with :fx."))
										.fontScale(0.85f).wrap().growX();
							});
						}).growX();
					}
					return row();
				}).growX();

				// API Key
				text(Core.bundle.get("feature.translation.deepl.api-key", "DeepL API Key")).left().color(Color.lightGray);
				textField(feature.deeplApiKeyConfig.signal())
						.placeholder(Core.bundle.get("feature.translation.deepl.api-key.hint", "...:fx"))
						.growX();

				// Timeout slider
				row().growX().gap(unit(2)).children(() -> {
					Computed<String> timeoutLabel = feature.deeplTimeoutConfig.signal()
							.map(t -> Core.bundle.format("feature.translation.deepl.timeout", t != null ? t : 10));
					text(timeoutLabel).left().growX();
					row().width(unit(35)).right().children(() -> {
						slider(feature.deeplTimeoutConfig.signal(), 2, 20, 1);
					});
				});
			});
		}).growX();
	}

	private Component buildMindustryToolPanel() {
		return card(Styles.defaultb, () -> {
			column().growX().padding(unit(3)).gap(unit(2)).children(() -> {
				text("[#e67e22]☁ [white]MindustryTool Cloud").left().growX();

				boolean loggedIn = MindustryAuthProvider.getInstance().isLoggedIn();
				if (loggedIn) {
					String username = MindustryAuthProvider.getInstance().getAccessToken() != null ? "Player" : "User";
					card(Styles.black3, () -> {
						row().growX().padding(unit(2)).children(() -> {
							text(Core.bundle.format("feature.translation.mindustrytool.logged-in", username)).color(Pal.heal);
						});
					}).growX();
				} else {
					card(Styles.black3, () -> {
						row().growX().padding(unit(2)).gap(unit(2)).children(() -> {
							text(Core.bundle.get("feature.translation.mindustrytool.not-logged-in")).color(Pal.accent).growX();
							button(Core.bundle.get("feature.translation.mindustrytool.login-btn", "Log In"), () -> {
								new AuthLoginDialog(MindustryAuthProvider.getInstance()).show();
							}).style(Styles.defaultb).height(unit(7));
						});
					}).growX();
				}

				// Timeout slider
				row().growX().gap(unit(2)).children(() -> {
					Computed<String> timeoutLabel = feature.mindustryToolTimeoutConfig.signal()
							.map(t -> Core.bundle.format("feature.translation.mindustrytool.timeout", t != null ? t : 30));
					text(timeoutLabel).left().growX();
					row().width(unit(35)).right().children(() -> {
						slider(feature.mindustryToolTimeoutConfig.signal(), 5, 60, 5);
					});
				});
			});
		}).growX();
	}

	private void buildTesterWidget() {
		column().growX().gap(unit(2)).children(() -> {
			row().growX().children(() -> {
				text("[accent]⚡ " + Core.bundle.get("feature.translation.test.title", "Connection Test")).left().color(Pal.accent).growX();
				text(isTestingSignal.map(testing -> Boolean.TRUE.equals(testing) ? "[accent]" + Core.bundle.get("feature.translation.test.testing", "Translating...") : "")).right();
			});

			// Sample phrases chips row
			row().growX().gap(unit(1)).children(() -> {
				text(Core.bundle.get("feature.translation.test.samples", "Quick test:")).color(Color.gray).fontScale(0.85f);
				String[] samples = {"Hello team!", "Defend the core!", "Cần thêm titan!", "Хорошая игра!"};
				for (String sample : samples) {
					button(sample, () -> testInputSignal.set(sample))
							.style(Styles.flatt)
							.height(unit(6));
				}
			});

			row().growX().gap(unit(2)).children(() -> {
				textField(testInputSignal).growX();

				Readable<String> buttonLabel = isTestingSignal.map(testing ->
						testing ? Core.bundle.get("feature.translation.test.testing", "Translating...")
								: Core.bundle.get("feature.translation.test.button", "Test Translate"));

				button(() -> {
					if (Boolean.TRUE.equals(isTestingSignal.get())) {
						return;
					}
					isTestingSignal.set(true);
					testResultSignal.set(Core.bundle.get("feature.translation.test.testing", "Translating..."));
					testSuccessSignal.set(true);

					long startTime = System.currentTimeMillis();
					feature.testTranslate(testInputSignal.get())
							.thenAccept(result -> {
								long elapsed = System.currentTimeMillis() - startTime;
								Core.app.post(() -> {
									isTestingSignal.set(false);
									testSuccessSignal.set(true);
									testResultSignal.set("[#58a6ff][" + elapsed + "ms] [white]" + Core.bundle.format("feature.translation.test.success", result));
								});
							})
							.exceptionally(err -> {
								Throwable cause = err.getCause() != null ? err.getCause() : err;
								Core.app.post(() -> {
									isTestingSignal.set(false);
									testSuccessSignal.set(false);
									testResultSignal.set(Core.bundle.format("feature.translation.test.error", cause.getMessage()));
								});
								return null;
							});
				})
				.style(Styles.defaultb)
				.height(unit(9))
				.width(unit(36))
				.children(() -> text(buttonLabel));
			});

			// Result output box
			card(Styles.defaultb, () -> {
				column().growX().padding(unit(2)).children(() -> {
					text(testResultSignal)
							.color(testSuccessSignal.map(s -> s ? Pal.heal : Pal.remove))
							.wrap()
							.growX();
				});
			}).growX();
		});
	}
}
