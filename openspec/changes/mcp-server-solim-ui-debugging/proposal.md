## Why

Debugging Solim UI components currently requires manual inspection or custom logging. There's no standardized way for AI assistants or external tools to query real-time UI state (component tree, signal values, bindings, layout info) for debugging. An MCP (Model Context Protocol) server would provide a structured API for AI tools to fetch live UI data, enabling intelligent debugging assistance.

## What Changes

- New MCP server implementation in Solim that exposes UI debugging endpoints
- New capability: `solim-mcp-debug-server` - MCP server for real-time UI state inspection
- WebSocket/HTTP endpoints for querying component tree, signal values, reactive bindings, layout metrics
- Integration with Solim's existing reactive system to expose live data

## Capabilities

### New Capabilities
- `solim-mcp-debug-server`: MCP server providing real-time UI debugging data (component hierarchy, signal states, binding status, layout info)

### Modified Capabilities
- `solim-reactivity`: May need to expose internal signal/binding inspection APIs
- `solim-component`: May need to expose component tree traversal for debugging

## Impact

- New Solim module for MCP server implementation
- New dependencies: MCP SDK (if not already available)
- Modifications to Solim core to expose debugging hooks
- Configuration for enabling/disabling debug server
- Security considerations for exposing internal state