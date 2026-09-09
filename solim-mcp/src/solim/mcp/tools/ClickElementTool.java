package solim.mcp.tools;

import arc.Core;
import arc.scene.Element;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import solim.mcp.introspection.SnapshotRoot;
import solim.mcp.introspection.UiSnapshot;

/**
 * Dispatches a programmatic click event to a target UI element on the main UI thread.
 */
public final class ClickElementTool implements McpTool {
	private final SnapshotRoot snapshotRoot;

	public ClickElementTool(SnapshotRoot snapshotRoot) {
		this.snapshotRoot = snapshotRoot;
	}

	@Override
	public String name() {
		return "click_element";
	}

	@Override
	public String description() {
		return "Finds a UI element by name or class and triggers a click event on the main game thread.";
	}

	@Override
	public ObjectNode inputSchema() {
		ObjectNode schema = JsonNodeFactory.instance.objectNode();
		schema.put("type", "object");
		ObjectNode props = schema.putObject("properties");
		props.putObject("target")
			.put("type", "string")
			.put("description", "Element name or class name to click.");
		schema.putArray("required").add("target");
		return schema;
	}

	@Override
	public ObjectNode execute(ObjectNode args) throws MCPException {
		if (!args.hasNonNull("target")) {
			throw new MCPException("Missing required argument: 'target'");
		}
		String target = args.get("target").asText();

		Element root = snapshotRoot.get();
		Element element = UiSnapshot.lookup(root, target);
		if (element == null) {
			throw new MCPException("No element matches '" + target + "'");
		}

		if (Core.app != null) {
			CompletableFuture<Void> future = new CompletableFuture<>();
			Core.app.post(() -> {
				try {
					element.fireClick();
					future.complete(null);
				} catch (Throwable t) {
					future.completeExceptionally(t);
				}
			});
			try {
				future.get(5, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				throw new MCPException("Click timed out after 5 seconds");
			} catch (Exception e) {
				throw new MCPException("Click failed: " + e.getMessage());
			}
		} else {
			element.fireClick();
		}

		ObjectNode out = JsonNodeFactory.instance.objectNode();
		out.put("clicked", true);
		out.put("target", target);
		out.put("elementClass", element.getClass().getName());
		return out;
	}
}
