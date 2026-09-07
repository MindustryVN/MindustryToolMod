---
name: core-protocol
version: 1.0.0
priority: P0
trigger: always_on
---

# Core Protocol - AG Kit

> The highest-priority workspace rules. How the AI loads agents/skills and what it must do before any implementation.

---

## CRITICAL: AGENT & SKILL PROTOCOL (START HERE)

> **MANDATORY:** You MUST read the appropriate agent file and its skills BEFORE performing any implementation. This is the highest priority rule.

### 1. Modular Skill Loading Protocol

Agent activated → Check frontmatter "skills:" → Read SKILL.md (INDEX) → Read specific sections.

- **Selective Reading:** DO NOT read ALL files in a skill folder. Read `SKILL.md` first, then only read sections matching the user's request.
- **Rule Priority:** P0 (Workspace Rules in `.agents/rules/`) > P1 (Agent `.md`) > P2 (SKILL.md). All rules are binding.

### 1.1 Skill Announcement (MANDATORY)

**Every time you load and apply a skill, announce it BEFORE using it** — so the user can verify which knowledge is active.

```markdown
📚 **Using skill: `@[skill-name]`...**
```

- List multiple skills together: `📚 Using skills: @frontend-design + @design-spec...`
- Announce on-demand skills too (e.g. a companion skill pulled from a hub, or `app-builder` for a new app), not just frontmatter ones.
- ❌ Applying a skill without announcing it = **USER CANNOT VERIFY THE SKILL WAS USED**.

### 2. Enforcement Protocol

1. **When agent is activated:**
    - ✅ Activate: Read Rules → Check Frontmatter → Load SKILL.md → Apply All.
2. **Forbidden:** Never skip reading agent rules or skill instructions. "Read → Understand → Apply" is mandatory.

---

## 📁 File Dependency Awareness

**Before modifying ANY file:**

1. If `CODEBASE.md` exists, check its File Dependencies section.
2. Otherwise, discover dependencies with targeted search/import analysis; do not block waiting for a missing file.
3. Identify dependent files and update all affected files together.

---

## 🗺️ System Map & Memory Read

> 🔴 **MANDATORY:** At session start, you MUST read `.agents/memory/MEMORY.md` to load persistent project conventions, user preferences, and decisions.

> 📚 **Catalog lookup (on-demand, NOT every session):** Need the full list of Agents / Skills / Scripts? The `quick-reference` rule has the essentials. For the complete catalog, read `.agents/ARCHITECTURE.md` only when you actually need it (e.g. orchestration, or discovering a skill you're unsure exists) — do NOT load it on every request.

**Path Awareness (Note: the project directory name is `.agents` plural):**

- Agents: `.agents/agent/` (Project)
- Skills: `.agents/skills/` (Project)
- Memory: `.agents/memory/` (Project)
- Runtime Scripts: `.agents/skills/<skill>/scripts/`

---

## 🧠 Read → Understand → Apply

```
❌ WRONG: Read agent file → Start coding
✅ CORRECT: Read → Understand WHY → Apply PRINCIPLES → Code
```

**Before coding, answer:**

1. What is the GOAL of this agent/skill?
2. What PRINCIPLES must I apply?
3. How does this DIFFER from generic output?

---
