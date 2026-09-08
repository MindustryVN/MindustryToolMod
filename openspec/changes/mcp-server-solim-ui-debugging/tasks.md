# Tasks

## 1. Module Setup

- [x] 1.1 Register the `solim-mcp` module in `settings.gradle` with a project dependency on `:solim`, keeping `solim` sources untouched
- [x] 1.2 Create `solim-mcp/build.gradle` with a Java-WebSocket dependency and the shared subproject config
- [x] 1.3 Implement `McpConfig` (host, port, token) read from system properties, disabled by default

## 2. Reflection Infrastructure

- [x] 2.1 Implement `ReflectAccess`: cached, gracefully-degrading reflective field/method access
- [x] 2.2 Implement `ObjectGraphScanner`: cycle-safe reflective scan of an object graph for Signal/Computed/Effect/Subscription instances

## 3. Introspection Capabilities

- [x] 3.1 Implement `SignalIntrospector`: current values plus listener/observer/dependency counts mapped to stable DTOs
- [x] 3.2 Implement `ComponentTreeInspector`: snapshot the Arc Element tree (name, class, children)
- [x] 3.3 Implement `BindingInspector`: bindings/effects owned by scanned components with subscribed state
- [x] 3.4 Implement `LayoutInspector`: x/y/width/height and expansion flags for elements

## 4. MCP Tools and JSON-RPC

- [x] 4.1 Define MCP tool schemas for `get_component_tree`, `get_signal_values`, `get_bindings`, `get_layout`
- [x] 4.2 Implement JSON-RPC 2.0 dispatch binding tools to their request handlers with error responses

## 5. WebSocket Transport

- [x] 5.1 Implement the WebSocket server with token-auth handshake rejection and JSON-RPC handling
- [x] 5.2 Implement subscription change notifications via signal-value and tree-shape sampling diffs

## 6. Tests and Documentation

- [ ] 6.1 Unit tests for the reflection infrastructure and introspection tools
- [ ] 6.2 Integration test connecting a mock WebSocket client (valid + invalid token)
- [ ] 6.3 AI assistant integration documentation in `solim-mcp/README.md`