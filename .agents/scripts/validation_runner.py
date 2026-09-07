#!/usr/bin/env python3
"""Shared process runner for AG Kit validation entry points."""
from __future__ import annotations

import json
import os
import subprocess
import sys
from dataclasses import asdict, dataclass, field
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable, Literal

Target = Literal["project", "url", "none"]


@dataclass(frozen=True)
class CheckSpec:
    name: str
    script: str
    category: str
    target: Target = "project"
    required: bool = False
    args: tuple[str, ...] = ()
    timeout: int = 300


@dataclass
class CheckResult:
    name: str
    category: str
    status: Literal["passed", "failed", "skipped", "error"]
    required: bool
    duration_seconds: float = 0.0
    command: list[str] = field(default_factory=list)
    stdout: str = ""
    stderr: str = ""
    reason: str = ""

    @property
    def passed(self) -> bool:
        return self.status in {"passed", "skipped"}


class Console:
    def __init__(self) -> None:
        enabled = sys.stdout.isatty() and "NO_COLOR" not in os.environ
        self.bold = "\033[1m" if enabled else ""
        self.cyan = "\033[96m" if enabled else ""
        self.green = "\033[92m" if enabled else ""
        self.yellow = "\033[93m" if enabled else ""
        self.red = "\033[91m" if enabled else ""
        self.end = "\033[0m" if enabled else ""

    def header(self, text: str) -> None:
        line = "=" * 70
        print(f"\n{self.bold}{self.cyan}{line}\n{text.center(70)}\n{line}{self.end}\n")

    def passed(self, text: str) -> None:
        print(f"{self.green}[PASS] {text}{self.end}")

    def skipped(self, text: str) -> None:
        print(f"{self.yellow}[SKIP] {text}{self.end}")

    def failed(self, text: str) -> None:
        print(f"{self.red}[FAIL] {text}{self.end}")

    def info(self, text: str) -> None:
        print(text)


CONSOLE = Console()


def locate_toolkit_root(project: Path, caller_file: str) -> Path:
    """Find the toolkit independently from the project being audited."""
    for candidate in (project / ".agents", project / ".agent"):
        if (candidate / "skills").is_dir() and (candidate / "scripts").is_dir():
            return candidate
    embedded = Path(caller_file).resolve().parents[1]
    if (embedded / "skills").is_dir() and (embedded / "scripts").is_dir():
        return embedded
    raise FileNotFoundError(
        "AG Kit root was not found. Expected <project>/.agents, <project>/.agent, "
        "or a runner located inside the toolkit."
    )


def _command_for(spec: CheckSpec, script: Path, project: Path, url: str | None) -> list[str] | None:
    command = [sys.executable, str(script)]
    if spec.target == "project":
        command.append(str(project))
    elif spec.target == "url":
        if not url:
            return None
        command.append(url)
    command.extend(spec.args)
    return command


def run_check(spec: CheckSpec, toolkit_root: Path, project: Path, url: str | None) -> CheckResult:
    script = toolkit_root / spec.script
    if not script.is_file():
        status = "failed" if spec.required else "skipped"
        reason = f"Script not found: {script}"
        result = CheckResult(spec.name, spec.category, status, spec.required, reason=reason)
        (CONSOLE.failed if status == "failed" else CONSOLE.skipped)(f"{spec.name}: {reason}")
        return result

    command = _command_for(spec, script, project, url)
    if command is None:
        reason = "URL not provided"
        CONSOLE.skipped(f"{spec.name}: {reason}")
        return CheckResult(spec.name, spec.category, "skipped", spec.required, reason=reason)

    started = datetime.now(timezone.utc)
    try:
        proc = subprocess.run(
            command,
            cwd=project,
            capture_output=True,
            text=True,
            timeout=spec.timeout,
            check=False,
        )
        duration = (datetime.now(timezone.utc) - started).total_seconds()
        status = "passed" if proc.returncode == 0 else "failed"
        result = CheckResult(
            spec.name, spec.category, status, spec.required, duration,
            command, proc.stdout, proc.stderr,
            reason="" if status == "passed" else f"Exit code {proc.returncode}",
        )
        if status == "passed":
            CONSOLE.passed(f"{spec.name} ({duration:.1f}s)")
        else:
            CONSOLE.failed(f"{spec.name} ({duration:.1f}s, exit {proc.returncode})")
            _print_failure_output(result)
        return result
    except subprocess.TimeoutExpired as exc:
        duration = (datetime.now(timezone.utc) - started).total_seconds()
        result = CheckResult(
            spec.name, spec.category, "error", spec.required, duration, command,
            _decode_timeout(exc.stdout), _decode_timeout(exc.stderr),
            f"Timed out after {spec.timeout}s",
        )
        CONSOLE.failed(f"{spec.name}: {result.reason}")
        return result
    except OSError as exc:
        duration = (datetime.now(timezone.utc) - started).total_seconds()
        result = CheckResult(spec.name, spec.category, "error", spec.required, duration, command, reason=str(exc))
        CONSOLE.failed(f"{spec.name}: {exc}")
        return result


def _decode_timeout(value: str | bytes | None) -> str:
    if value is None:
        return ""
    return value.decode(errors="replace") if isinstance(value, bytes) else value


def _print_failure_output(result: CheckResult, limit: int = 1600) -> None:
    combined = "\n".join(part.strip() for part in (result.stdout, result.stderr) if part.strip())
    if combined:
        print(combined[-limit:])


def execute_suite(
    specs: Iterable[CheckSpec],
    toolkit_root: Path,
    project: Path,
    url: str | None,
    stop_on_fail: bool = False,
) -> list[CheckResult]:
    results: list[CheckResult] = []
    current_category = ""
    for spec in specs:
        if spec.category != current_category:
            current_category = spec.category
            CONSOLE.header(current_category)
        result = run_check(spec, toolkit_root, project, url)
        results.append(result)
        if stop_on_fail and result.status in {"failed", "error"}:
            break
    return results


def suite_success(results: Iterable[CheckResult]) -> bool:
    return all(result.status not in {"failed", "error"} for result in results)


def print_summary(title: str, results: list[CheckResult], started: datetime) -> bool:
    CONSOLE.header(title)
    counts = {status: sum(r.status == status for r in results) for status in ("passed", "failed", "error", "skipped")}
    duration = (datetime.now(timezone.utc) - started).total_seconds()
    print(f"Duration: {duration:.1f}s")
    print(f"Checks: {len(results)} | Passed: {counts['passed']} | Failed: {counts['failed']} | Errors: {counts['error']} | Skipped: {counts['skipped']}")
    for result in results:
        marker = {"passed": "PASS", "failed": "FAIL", "error": "ERROR", "skipped": "SKIP"}[result.status]
        detail = f" - {result.reason}" if result.reason else ""
        print(f"[{marker}] {result.category} / {result.name}{detail}")
    ok = suite_success(results)
    (CONSOLE.passed if ok else CONSOLE.failed)("Validation completed successfully" if ok else "Validation found blocking failures")
    return ok


def write_report(path: Path, project: Path, toolkit_root: Path, results: list[CheckResult], started: datetime) -> None:
    payload = {
        "project": str(project),
        "toolkit_root": str(toolkit_root),
        "started_at": started.isoformat(),
        "finished_at": datetime.now(timezone.utc).isoformat(),
        "success": suite_success(results),
        "results": [asdict(result) for result in results],
    }
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
