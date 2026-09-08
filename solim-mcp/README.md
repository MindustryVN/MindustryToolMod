# solim-mcp — Solim UI Debug Server (MCP)

A three-in-one debug transport for inspecting the live Mindustry **Solim** UI from an AI assistant or
any JSON-RPC 2.0 client. Inspects the running UI through **pure reflection**; it makes **no changes
to Solim source code**.

- WebSocket (primary): streaming JSON-RPC 2.0, MCP-compatible subset.
- HTTP (fallback): one-off queries via `POST /mcp`.
- Notifications: polled `solim/notify` pushes for subscribed clients.

## Why reflection?

The entire Solim codebase stays byte-identical. `solim-mcp` reads `Signal`/`Computed`/`Effect`
fields and counts, the Arc element tree, and layout metrics via cached reflective access that
degrades gracefully if a Solim field is renamed or removed.

## Enabling the server

Disabled by default. Enable with a system property, e.g. in a launcher or script:

```text
-solim.mcp.enabled=true
-solim.mcp.host=127.0.0.1
-solim.mcp.port=8754
-solim.mcp.httpPort=0
-solim.mcp.token=my-secret-token
-solim.mcp.pollMs=500
```

| Property               | Default     | Meaning                                       |
| ---------------------- | ----------- | --------------------------------------------- |
| `solim.mcp.enabled`    | `false`     | Set to `true` to bind sockets                |
| `solim.mcp.host`       | `127.0.0.1` | Bind host                                     |
| `solim.mcp.port`       | `8754`      | WebSocket bind port                           |
| `solim.mcp.httpPort`   | `0`         | HTTP fallback port; `0` disables              |
| `solim.mcp.token`      | *random*    | Required connection token when enabled        |
| `solim.mcp.pollMs`     | `500`       | Subscription sampling interval (ms)           |

When enabled without a token, a random token is generated and printed to the log. When disabled,
no socket is ever opened and reflection costs are zero.

## Connecting

WebSocket URL with token:

```text
ws://127.0.0.1:8754/?token=my-secret-token
```

Token can also be passed as the `X-Mcp-Token` request header. Unauthorized connections are closed
immediately (client sees close code 4003 or -1/1006 depending on timing) and never exchange data.

Client compatibility notes:

- Clients that support custom WebSocket URIs can connect to the `ws://` URL above directly and
  speak the JSON-RPC subset described below.
- Clients that support HTTP can point at the HTTP fallback (`http://127.0.0.1:PORT/mcp`) with
  `Authorization: Bearer <token>`; only one-off tool calls will work (no streamed notifications).
- The token is always printed in the server startup log so assistants can be configured in-game.

## Protocol

Implemented subset of MCP over JSON-RPC 2.0:

| Method                  | Behavior                                          |
| ----------------------- | ------------------------------------------------- |
| `initialize`            | Advertises `protocolVersion: "2024-11-05"`, tools |
| `notifications/initialized` | Ack, no response                              |
| `ping`                  | Empty result                                      |
| `tools/list`            | The four debug tools                              |
| `tools/call`            | Executes a tool; errors carry `code: -32000`      |
| `solim/subscribe`       | Registers the connection for change notifications |
| `solim/unsubscribe`     | Removes the connection                            |

Push notifications:

```text
{"jsonrpc":"2.0","method":"solim/notify","params":{"treeChanged":true,"signals":[...]}}
```

Notifications are sent whenever a subscribed client's last delivered snapshot differs on a poll
(signal values, tree shape, or discovered refs). No per-signal subscriptions are installed.

### Tools

#### `get_component_tree`

Arguments: `{"query": "optional name / class name"}` (empty = full tree).

```json
{"tree":{"name":"hud","type":"Table","className":"arc.scene.ui.layout.Table","layout":{"x":0,"y":0,"width":1920,"height":1080,"visible":true,"expanding":false},"children":[...]}}
```

#### `get_signal_values`

Arguments: `{"query": "optional substring match on name/field/location"}` (empty = all).

```json
[{"id":"hud/toolbar/enabled","kind":"SIGNAL","location":"hud/toolbar/enabled","element":"toolbar","field":"enabled","value":true,"valueClass":"java.lang.Boolean","listeners":2,"observers":0}]
```

Discoverable sources: reactive fields on elements, `Element.userObject` graphs, and whatever is
currently on the ambient `ComponentContext`/`ReactiveContext`/`ParentStack` stacks (in-flight
builds).

#### `get_bindings`

Arguments: `{"query": "optional substring match"}` (empty = all). Lists discovered `Effect`s as
bindings with dependency counts and disposed state.

#### `get_layout`

Arguments: `{"target": "name or class name of an element"}` (required).

```json
{"layout":{"x":0,"y":0,"width":1920,"height":1080,"visible":true,"expanding":false}}
```

## HTTP fallback

One-off POSTs (no streaming): `POST /mcp` with a JSON-RPC body. Requires
`Authorization: Bearer <token>` or `X-Mcp-Token`. `401` when missing/wrong.

```text
POST /mcp HTTP/1.1
Authorization: Bearer my-secret-token
Content-Type: application/json

{"jsonrpc":"2.0","id":1,"method":"tools/list"}
```

## Embedding

```java
McpConfig config = new McpConfig(true, "127.0.0.1", 8754, 0, "token", 500);
SolimMcpServer server = SolimMcpServer.start(config); // null when disabled
// ...
server.stop();
```

## Building and testing

```text
gradlew :solim-mcp:build
gradlew :solim-mcp:test
```

The integration test starts a real server (ephemeral ports) and drives it with a Java-WebSocket
client: initialize, tools/list, tools/call, subscribe, a live signal change, and an unauthorized
rejection, plus the HTTP fallback path.

## Zero-cost when disabled

When `solim.mcp.enabled=false` the module still exists on the classpath but opens no sockets,
registers no listeners, and installs no hooks into Solim or Arc.