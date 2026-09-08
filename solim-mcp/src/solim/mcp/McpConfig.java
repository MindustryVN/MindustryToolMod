package solim.mcp;

import java.util.UUID;

/**
 * Configuration for the Solim MCP debug server.
 *
 * <p>Read from system properties; disabled by default so no socket is ever opened unless explicitly
 * enabled:
 *
 * <ul>
 *   <li>{@code solim.mcp.enabled} - "true" enables the server (default "false")
 *   <li>{@code solim.mcp.host} - bind host (default "127.0.0.1")
 *   <li>{@code solim.mcp.port} - bind port (default 8754)
 *   <li>{@code solim.mcp.token} - connection token; when enabled and unset, a random token is
 *       generated
 * </ul>
 */
public final class McpConfig {
	public final boolean enabled;
	public final String host;
	public final int port;
	public final String token;

	private McpConfig(boolean enabled, String host, int port, String token) {
		this.enabled = enabled;
		this.host = host;
		this.port = port;
		this.token = token;
	}

	public static McpConfig fromProperties() {
		boolean enabled = Boolean.parseBoolean(System.getProperty("solim.mcp.enabled", "false"));
		String host = System.getProperty("solim.mcp.host", "127.0.0.1");
		int port = parsePort(System.getProperty("solim.mcp.port", "8754"));
		String token = System.getProperty("solim.mcp.token", "");
		if (enabled && token.isEmpty()) {
			token = UUID.randomUUID().toString();
		}
		return new McpConfig(enabled, host, port, token);
	}

	private static int parsePort(String raw) {
		try {
			int port = Integer.parseInt(raw);
			if (port < 1 || port > 65535) {
				throw new IllegalArgumentException("port out of range: " + port);
			}
			return port;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("invalid port: " + raw, e);
		}
	}

	/** Whether the server is enabled and should bind a socket. */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * The non-null token required for connections. Guaranteed non-empty when {@link #isEnabled()}
	 * is true.
	 */
	public String token() {
		return token;
	}
}