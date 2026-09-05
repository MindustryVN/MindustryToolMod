#!/usr/bin/env python3
"""Self-validate an AG Kit installation.

Checks machine-readable configuration, versioned frontmatter contracts,
cross references, generated registries, local Markdown links, memory schema,
Python syntax, and architecture inventory counts.
"""
from __future__ import annotations

import argparse
import ast
import json
import re
import sys
from dataclasses import dataclass
from pathlib import Path
from urllib.parse import unquote

try:
    import yaml  # type: ignore
except ImportError:  # pragma: no cover - optional enhancement
    yaml = None

from component_registry import (
    build_lock,
    build_manifest,
    canonical_json,
    is_semver,
    normalize_list,
    version_satisfies,
)
from dependency_graph import render as render_dependency_graph


@dataclass
class Finding:
    severity: str
    code: str
    file: str
    line: int
    message: str


REQUIRED_FIELDS = {
    "agent": {"name", "description", "tools", "model", "skills", "version"},
    "skill": {"name", "description", "when_to_use", "allowed-tools", "version"},
    "workflow": {
        "name",
        "description",
        "version",
        "requires_agents",
        "requires_skills",
        "artifact_outputs",
    },
    "rule": {"name", "trigger", "version", "priority"},
}


def add(findings: list[Finding], severity: str, code: str, path: Path, message: str, line: int = 1) -> None:
    findings.append(Finding(severity, code, path.as_posix(), line, message))


def extract_frontmatter(path: Path) -> tuple[str | None, int]:
    text = path.read_text("utf-8", errors="replace")
    if not text.startswith("---\n"):
        return None, 1
    end = text.find("\n---\n", 4)
    if end < 0:
        return None, 1
    return text[4:end], 1


def fallback_frontmatter(raw: str) -> dict[str, object]:
    data: dict[str, object] = {}
    for line in raw.splitlines():
        if not line or line[0].isspace() or line.lstrip().startswith("#"):
            continue
        match = re.match(r"^([A-Za-z0-9_-]+):\s*(.*)$", line)
        if not match:
            continue
        key, value = match.groups()
        data[key] = value.strip().strip('"\'')
    return data


def parse_frontmatter(path: Path, findings: list[Finding]) -> dict[str, object] | None:
    raw, _ = extract_frontmatter(path)
    if raw is None:
        add(findings, "error", "frontmatter.missing", path, "Missing or unterminated YAML frontmatter")
        return None
    if yaml is None:
        return fallback_frontmatter(raw)
    try:
        data = yaml.safe_load(raw)
    except Exception as exc:
        line = int(getattr(getattr(exc, "problem_mark", None), "line", 0)) + 2
        add(findings, "error", "frontmatter.invalid_yaml", path, str(exc), line)
        return None
    if not isinstance(data, dict):
        add(findings, "error", "frontmatter.not_mapping", path, "Frontmatter must be a YAML mapping")
        return None
    return data


def validate_json(root: Path, findings: list[Finding]) -> None:
    for path in root.rglob("*.json"):
        if "__pycache__" in path.parts:
            continue
        try:
            json.loads(path.read_text("utf-8"))
        except (OSError, json.JSONDecodeError) as exc:
            line = int(getattr(exc, "lineno", 1))
            add(findings, "error", "json.invalid", path.relative_to(root), str(exc), line)


def validate_kit_version(root: Path, findings: list[Finding]) -> None:
    version_path = root / "VERSION"
    if not version_path.is_file():
        add(findings, "error", "version.missing", Path("VERSION"), "VERSION is missing")
        return
    value = version_path.read_text("utf-8").strip()
    if not re.fullmatch(r"\d{4}\.\d{1,2}\.\d{1,2}", value):
        add(findings, "error", "version.invalid_calver", Path("VERSION"), f"Expected YYYY.M.D CalVer, got {value!r}")


def validate_frontmatter(
    root: Path, findings: list[Finding]
) -> tuple[
    dict[str, dict[str, object]],
    dict[str, dict[str, object]],
    dict[str, dict[str, object]],
    dict[str, dict[str, object]],
]:
    agents: dict[str, dict[str, object]] = {}
    skills: dict[str, dict[str, object]] = {}
    workflows: dict[str, dict[str, object]] = {}
    rules: dict[str, dict[str, object]] = {}
    groups = (
        ("agent", sorted((root / "agent").glob("*.md"))),
        ("skill", sorted((root / "skills").glob("*/SKILL.md"))),
        ("workflow", sorted((root / "workflows").glob("*.md"))),
        ("rule", sorted((root / "rules").glob("*.md"))),
    )
    registries = {"agent": agents, "skill": skills, "workflow": workflows, "rule": rules}
    for kind, paths in groups:
        names_seen: set[str] = set()
        for path in paths:
            rel = path.relative_to(root)
            data = parse_frontmatter(path, findings)
            if data is None:
                continue
            missing = REQUIRED_FIELDS[kind] - set(data)
            for field in sorted(missing):
                add(findings, "error", "frontmatter.required_field", rel, f"Missing required field: {field}")
            name = str(data.get("name", ""))
            expected = path.parent.name if kind == "skill" else path.stem
            if name != expected:
                add(findings, "error", "frontmatter.name_mismatch", rel, f"name={name!r}, expected {expected!r}")
            if name:
                if name in names_seen:
                    add(findings, "error", "frontmatter.duplicate_name", rel, f"Duplicate {kind} name: {name}")
                names_seen.add(name)
            version = data.get("version")
            if version is not None and not is_semver(str(version)):
                add(findings, "error", "frontmatter.invalid_semver", rel, f"Invalid SemVer: {version!r}")
            registries[kind][expected] = data
    return agents, skills, workflows, rules


def validate_references(
    root: Path,
    agents: dict[str, dict[str, object]],
    skills: dict[str, dict[str, object]],
    workflows: dict[str, dict[str, object]],
    findings: list[Finding],
) -> None:
    for agent_name, data in agents.items():
        path = Path("agent") / f"{agent_name}.md"
        for skill in normalize_list(data.get("skills")):
            if skill not in skills:
                add(findings, "error", "reference.unknown_skill", path, f"Agent references missing skill: {skill}")

    for workflow_name, data in workflows.items():
        path = Path("workflows") / f"{workflow_name}.md"
        for agent in normalize_list(data.get("requires_agents")):
            if agent not in agents:
                add(findings, "error", "reference.unknown_agent", path, f"Workflow references missing agent: {agent}")
        for skill in normalize_list(data.get("requires_skills")):
            if skill not in skills:
                add(findings, "error", "reference.unknown_skill", path, f"Workflow references missing skill: {skill}")
        if not normalize_list(data.get("artifact_outputs")):
            add(findings, "warning", "workflow.no_outputs", path, "Workflow declares no artifact outputs")

    script_pattern = re.compile(r'["\']((?:skills|scripts)/[^"\']+?\.py)["\']')
    for path in (root / "scripts").glob("*.py"):
        text = path.read_text("utf-8", errors="replace")
        for match in script_pattern.finditer(text):
            if any(char in match.group(1) for char in "*?["):
                continue
            target = root / match.group(1)
            if not target.is_file():
                line = text.count("\n", 0, match.start()) + 1
                add(findings, "error", "reference.missing_script", path.relative_to(root), f"Referenced script does not exist: {match.group(1)}", line)


def validate_manifest(root: Path, findings: list[Finding]) -> None:
    manifest_path = root / "manifest.json"
    lock_path = root / "manifest.lock.json"
    if not manifest_path.is_file():
        add(findings, "error", "manifest.missing", Path("manifest.json"), "Run scripts/generate_manifest.py")
        return
    if not lock_path.is_file():
        add(findings, "error", "manifest.lock_missing", Path("manifest.lock.json"), "Run scripts/generate_manifest.py")
        return
    try:
        actual_manifest = json.loads(manifest_path.read_text("utf-8"))
        actual_lock = json.loads(lock_path.read_text("utf-8"))
    except json.JSONDecodeError:
        return
    try:
        expected_manifest = build_manifest(root)
        expected_lock = build_lock(root, expected_manifest)
    except Exception as exc:
        add(findings, "error", "manifest.generation_failed", Path("manifest.json"), str(exc))
        return
    if actual_manifest != expected_manifest:
        add(findings, "error", "manifest.stale", Path("manifest.json"), "Registry differs from component frontmatter; regenerate it")
    if actual_lock != expected_lock:
        add(findings, "error", "manifest.lock_stale", Path("manifest.lock.json"), "Lock differs from current component files; regenerate it")

    skill_versions = {name: data["version"] for name, data in expected_manifest["skills"].items()}
    for agent_name, agent in expected_manifest["agents"].items():
        for skill_name, constraint in agent["requires"]["skills"].items():
            version = skill_versions.get(skill_name)
            if version and not version_satisfies(version, constraint):
                add(
                    findings,
                    "error",
                    "manifest.incompatible_dependency",
                    Path(agent["path"]),
                    f"{skill_name} {version} does not satisfy {constraint}",
                )


def validate_generated_docs(root: Path, findings: list[Finding]) -> None:
    path = root / "DEPENDENCY_GRAPH.md"
    if not path.is_file():
        add(findings, "error", "graph.missing", Path("DEPENDENCY_GRAPH.md"), "Run scripts/dependency_graph.py")
        return
    expected = render_dependency_graph(root)
    if path.read_text("utf-8") != expected:
        add(findings, "error", "graph.stale", Path("DEPENDENCY_GRAPH.md"), "Dependency graph is stale")


def validate_markdown_links(root: Path, findings: list[Finding]) -> None:
    pattern = re.compile(r"!?\[[^\]]*\]\(([^)]+)\)")
    for path in root.rglob("*.md"):
        text = path.read_text("utf-8", errors="replace")
        for match in pattern.finditer(text):
            raw = match.group(1).strip()
            if not raw:
                continue
            target_text = raw.split()[0].strip("<>")
            if target_text.startswith(("#", "http://", "https://", "mailto:", "tel:", "data:")):
                continue
            target_text = unquote(target_text.split("#", 1)[0])
            if not target_text:
                continue
            target = (path.parent / target_text).resolve()
            try:
                target.relative_to(root.resolve())
            except ValueError:
                continue
            if path.relative_to(root).as_posix() == "skills/documentation-templates/SKILL.md" and target_text.startswith("./docs/"):
                continue
            if not target.exists():
                line = text.count("\n", 0, match.start()) + 1
                add(findings, "error", "markdown.missing_link", path.relative_to(root), f"Missing local target: {target_text}", line)


def validate_memory(root: Path, findings: list[Finding]) -> None:
    memory_root = root / "memory"
    index = memory_root / "MEMORY.md"
    required_topics = {"project-conventions.md", "user-preferences.md", "tech-decisions.md", "feedback-history.md"}
    if not index.is_file():
        add(findings, "error", "memory.index_missing", Path("memory/MEMORY.md"), "Memory index is missing")
        return
    lines = index.read_text("utf-8").splitlines()
    if len(lines) > 200:
        add(findings, "error", "memory.index_too_large", Path("memory/MEMORY.md"), f"Memory index has {len(lines)} lines; maximum is 200")
    entry_pattern = re.compile(r"^- \[(user|feedback|project|reference)\] .+ → ([A-Za-z0-9._-]+\.md)$")
    for line_no, line in enumerate(lines, 1):
        if not line.startswith("- ["):
            continue
        match = entry_pattern.fullmatch(line)
        if not match:
            add(findings, "error", "memory.invalid_entry", Path("memory/MEMORY.md"), "Invalid memory index entry", line_no)
            continue
        target = memory_root / match.group(2)
        if not target.is_file():
            add(findings, "error", "memory.missing_topic", Path("memory/MEMORY.md"), f"Missing topic file: {match.group(2)}", line_no)
    for topic in sorted(required_topics):
        path = memory_root / topic
        if not path.is_file():
            add(findings, "error", "memory.required_topic", Path("memory") / topic, "Required memory topic is missing")
            continue
        data = parse_frontmatter(path, findings)
        if data is None:
            continue
        missing = {"type", "created", "updated"} - set(data)
        for field in sorted(missing):
            add(findings, "error", "memory.required_field", path.relative_to(root), f"Missing required field: {field}")
        if str(data.get("type", "")) not in {"user", "feedback", "project", "reference"}:
            add(findings, "error", "memory.invalid_type", path.relative_to(root), f"Invalid memory type: {data.get('type')!r}")


def validate_python(root: Path, findings: list[Finding]) -> None:
    for path in root.rglob("*.py"):
        if "__pycache__" in path.parts:
            continue
        try:
            ast.parse(path.read_text("utf-8"), filename=str(path))
        except (OSError, SyntaxError) as exc:
            add(findings, "error", "python.syntax", path.relative_to(root), str(exc), int(getattr(exc, "lineno", 1) or 1))


def validate_architecture_counts(root: Path, findings: list[Finding]) -> None:
    path = root / "ARCHITECTURE.md"
    if not path.is_file():
        add(findings, "error", "architecture.missing", Path("ARCHITECTURE.md"), "ARCHITECTURE.md is missing")
        return
    text = path.read_text("utf-8", errors="replace")
    actual = {
        "agents": len(list((root / "agent").glob("*.md"))),
        "skills": len(list((root / "skills").glob("*/SKILL.md"))),
        "workflows": len(list((root / "workflows").glob("*.md"))),
        "skill_scripts": len(list((root / "skills").glob("*/scripts/*.py"))),
    }
    patterns = {
        "agents": r"\*\*Total Agents\*\*\s*\|\s*(\d+)",
        "skills": r"\*\*Total Skills\*\*\s*\|\s*(\d+)",
        "workflows": r"\*\*Total Workflows\*\*\s*\|\s*(\d+)",
        "skill_scripts": r"\*\*Total Skill Scripts\*\*\s*\|\s*(\d+)",
    }
    for key, pattern in patterns.items():
        match = re.search(pattern, text)
        if not match:
            add(findings, "error", "architecture.count_missing", Path("ARCHITECTURE.md"), f"Missing inventory field for {key}")
        elif int(match.group(1)) != actual[key]:
            add(findings, "error", "architecture.count_mismatch", Path("ARCHITECTURE.md"), f"{key}: documented {match.group(1)}, actual {actual[key]}")


def validate(root: Path) -> list[Finding]:
    findings: list[Finding] = []
    validate_json(root, findings)
    validate_kit_version(root, findings)
    agents, skills, workflows, _rules = validate_frontmatter(root, findings)
    validate_references(root, agents, skills, workflows, findings)
    validate_manifest(root, findings)
    validate_generated_docs(root, findings)
    validate_markdown_links(root, findings)
    validate_memory(root, findings)
    validate_python(root, findings)
    validate_architecture_counts(root, findings)
    return findings


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate AG Kit structure and cross references")
    parser.add_argument("path", nargs="?", default=None, help="Path to .agents (defaults to this toolkit)")
    parser.add_argument("--json", action="store_true", dest="as_json")
    args = parser.parse_args()
    root = Path(args.path).resolve() if args.path else Path(__file__).resolve().parents[1]
    if not root.is_dir():
        parser.error(f"Toolkit directory does not exist: {root}")
    findings = validate(root)
    errors = [item for item in findings if item.severity == "error"]
    warnings = [item for item in findings if item.severity == "warning"]
    payload = {
        "toolkit": str(root),
        "passed": not errors,
        "summary": {"errors": len(errors), "warnings": len(warnings)},
        "findings": [item.__dict__ for item in findings],
    }
    if args.as_json:
        print(json.dumps(payload, indent=2, ensure_ascii=False))
    else:
        print(f"AG Kit self-validation: {root}")
        for item in findings:
            print(f"[{item.severity.upper()}] {item.file}:{item.line} {item.code} - {item.message}")
        print(f"Summary: {len(errors)} error(s), {len(warnings)} warning(s)")
        print("[PASS] Toolkit is structurally valid." if not errors else "[FAIL] Toolkit validation failed.")
    return 0 if not errors else 1


if __name__ == "__main__":
    raise SystemExit(main())
