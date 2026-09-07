#!/usr/bin/env python3
"""Run the fast AG Kit validation checklist."""
from __future__ import annotations

import argparse
from datetime import datetime, timezone
from pathlib import Path

from validation_runner import (
    CONSOLE,
    CheckSpec,
    execute_suite,
    locate_toolkit_root,
    print_summary,
    write_report,
)

CORE_CHECKS = (
    CheckSpec(
        "Security Scan",
        "skills/vulnerability-scanner/scripts/security_scan.py",
        "P0 Security",
        required=True,
        args=("--output", "summary", "--fail-on", "high"),
    ),
    CheckSpec("Lint Check", "skills/lint-and-validate/scripts/lint_runner.py", "P1 Code Quality", required=True),
    CheckSpec("Type Coverage", "skills/lint-and-validate/scripts/type_coverage.py", "P1 Code Quality"),
    CheckSpec("Schema Validation", "skills/database-design/scripts/schema_validator.py", "P2 Data Layer"),
    CheckSpec("Test Runner", "skills/testing-patterns/scripts/test_runner.py", "P3 Testing"),
    CheckSpec("UX Audit", "skills/frontend-design/scripts/ux_audit.py", "P4 UX & Accessibility"),
    CheckSpec("Accessibility Check", "skills/frontend-design/scripts/accessibility_checker.py", "P4 UX & Accessibility"),
    CheckSpec("SEO Check", "skills/seo-fundamentals/scripts/seo_checker.py", "P5 SEO"),
)

URL_CHECKS = (
    CheckSpec("Lighthouse Audit", "skills/performance-profiling/scripts/lighthouse_audit.py", "P6 Runtime Checks", target="url", required=True, timeout=180),
    CheckSpec("Playwright Smoke Test", "skills/webapp-testing/scripts/playwright_runner.py", "P6 Runtime Checks", target="url", timeout=120),
)


def main() -> int:
    parser = argparse.ArgumentParser(description="Run the fast AG Kit validation checklist")
    parser.add_argument("project", nargs="?", default=".", help="Project path to validate")
    parser.add_argument("--url", help="Running application URL for Lighthouse and Playwright")
    parser.add_argument("--skip-runtime", action="store_true", help="Skip URL-based runtime checks")
    parser.add_argument("--stop-on-fail", action="store_true", help="Stop after the first failed check")
    parser.add_argument("--report", type=Path, help="Write a machine-readable JSON report")
    args = parser.parse_args()

    project = Path(args.project).resolve()
    if not project.is_dir():
        parser.error(f"Project directory does not exist: {project}")

    try:
        toolkit_root = locate_toolkit_root(project, __file__)
    except FileNotFoundError as exc:
        parser.error(str(exc))

    started = datetime.now(timezone.utc)
    CONSOLE.header("AG KIT - FAST CHECKLIST")
    print(f"Project: {project}\nToolkit: {toolkit_root}\nURL: {args.url or 'not provided'}")

    specs = list(CORE_CHECKS)
    if not args.skip_runtime:
        specs.extend(URL_CHECKS)
    results = execute_suite(specs, toolkit_root, project, args.url, args.stop_on_fail)
    success = print_summary("CHECKLIST SUMMARY", results, started)
    if args.report:
        write_report(args.report.resolve(), project, toolkit_root, results, started)
        print(f"Report: {args.report.resolve()}")
    return 0 if success else 1


if __name__ == "__main__":
    raise SystemExit(main())
