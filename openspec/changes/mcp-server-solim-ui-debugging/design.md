## Context

Solim is a declarative UI framework for Mindustry mods. It provides reactive components, signals, computed values, and structural reactivity. Currently, debugging UI state requires manual inspection via logging or breakpoints. There's no programmatic way for external tools (especially AI assistants) to query the live UI state.

The MCP (Model Context Protocol) is a standardized protocol for AI assistants to interact with external tools and data sources. An MCP server in Solim would allow AI debugging assistants to query real-time UI state through a well-defined protocol.

## Goals / Non-Goals

**Goals:**
- Implement an MCP server within Solim that exposes UI debugging capabilities
- Provide MCP tools for: component tree traversal, signal/computed value inspection, binding status, layout metrics
- Use WebSocket transport for real-time updates (subscription to UI changes)
- Integrate with Solim's existing reactive system without performance overhead when disabled
- Secure by default: disabled in production, opt-in for development
- Follow Solim's architectural patterns (declarative, reactive, automatic ownership)

**Non-Goals:**
- Full MCP protocol implementation (only subset needed for debugging)
- UI modification capabilities (read-only inspection)
- Production monitoring/telemetry (debug-only)
- Replacement for existing logging/debugging tools

## Decisions

### 1. Transport: WebSocket over HTTP
**Decision**: Use WebSocket for real-time subscriptions, HTTP for one-off queries.
**Rationale**: MCP supports both. WebSocket enables live updates when UI changes (signal updates, component mount/unmount). HTTP fallback for simple queries.
**Alternatives**: Server-Sent Events (SSE) - less bidirectional; pure HTTP polling - high latency.

### 2. Integration Point: Solim Core Debug Hooks
**Decision**: Add debug hooks to `Component`, `Signal`, `Computed`, `Binding` classes that the MCP server can register to.
**Rationale**: Minimal intrusion. Hooks are no-ops when debug server disabled. Follows observer pattern.
**Alternatives**: Reflection-based inspection - fragile, breaks with obfuscation; separate debug build - complex.

### 3. MCP Server Location: New `solim-mcp` module
**Decision**: Create separate module `solim-mcp` under `solim/` directory.
**Rationale**: Keeps debug code isolated. Optional dependency. Clear ownership. Follows feature-oriented architecture.
**Alternatives**: Embed in `solim-core` - bloats core; separate repo - overkill for debug tool.

### 4. Data Model: JSON-RPC 2.0 over MCP
**Decision**: Use standard MCP tool definitions with JSON schemas for UI data.
**Rationale**: Standard protocol. AI assistants understand MCP. Schema enables validation.
**Alternatives**: Custom protocol - no tooling support; GraphQL - overkill.

### 5. Security: Opt-in with Token Auth
**Decision**: Server disabled by default. Enabled via config + random token. Token passed in WebSocket handshake.
**Rationale**: Prevents accidental exposure. Token prevents unauthorized local connections.
**Alternatives**: IP allowlist - complex in dev; no auth - security risk.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Performance overhead when enabled | Hooks are no-ops when disabled; lazy initialization; sampling for high-frequency signals |
| Memory leaks from subscriptions | Automatic cleanup on component dispose; weak references for signal observers |
| Exposing internal implementation details | Curated API surface; transform internal models to stable debug DTOs |
| MCP protocol changes | Pin MCP SDK version; abstract transport layer |
| Complexity in reactive system integration | Start with read-only snapshot API; add subscriptions incrementally |

## Migration Plan

1. Create `solim-mcp` module with MCP server skeleton
2. Add debug hooks to Solim core (Component, Signal, Binding)
3. Implement MCP tools: `get_component_tree`, `get_signal_values`, `get_bindings`, `get_layout`
4. Add WebSocket transport with subscription support
5. Add configuration (enabled, port, token)
6. Integration tests with mock AI client
7. Documentation for AI assistant integration

## Open Questions

1. Should we expose `Effect` execution traces?
2. How to handle large component trees (virtualization/pagination)?
3. Should we support historical replay (time-travel debugging)?
4. Integration with Mindustry's existing dev tools?