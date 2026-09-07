---
name: design-rules
version: 1.0.0
priority: P0
trigger: glob
globs: "**/*.{tsx,jsx,vue,svelte,css,scss},**/components/**,**/app/**/page.tsx"
---

# Design Rules (TIER 2) - AG Kit

> Loaded when touching UI files. Design rules live in the specialist agents, NOT here.

## 🛑 GATE: DESIGN.md before any UI code (MANDATORY)

Before writing or editing UI (components, pages, styles — web or mobile), a **`DESIGN.md` must exist at the project root**.

1. **Check** for `DESIGN.md` at the project root.
2. **If missing:** infer the design direction from the brief, then **create `DESIGN.md` first** (tokens + rationale) following the `design-spec` skill. Do not write UI code until it exists.
3. **If present:** READ it and build strictly against its tokens. Descriptive names in prose map to token names.
4. **Keep it in sync** when the visual language changes — it is the single source of truth.

> Exception: none for new UI. A genuinely trivial tweak to existing UI (one button color, a spacing nudge) may proceed if a `DESIGN.md` already governs the project. Net-new UI always requires the gate.

| Need | Read |
| ---- | ---- |
| DESIGN.md format / tokens | `.agents/skills/design-spec/SKILL.md` |

---

| Task         | Read                            |
| ------------ | ------------------------------- |
| Web UI/UX    | `.agents/agent/frontend-specialist.md` |
| Mobile UI/UX | `.agents/agent/mobile-developer.md`    |

**These agents contain:**

- Purple Ban (no purple by default — brand/brief override allowed)
- Template Ban (no standard layouts)
- Anti-cliché rules
- Deep Design Thinking protocol

> 🔴 **For design work:** Open and READ the agent file. Rules are there.

---
