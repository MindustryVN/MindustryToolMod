#!/usr/bin/env python3
"""Run the complete AG Kit verification suite."""
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

VERIFICATION_SUITE = (
    CheckSpec(
        "Security Scan",
        "skills/vulnerability-scanner/scripts/security_scan.py",
        "P0 Security",
        required=True,
        args=("--output", "summary", "--fail-on", "high"),
    ),
    CheckSpec("Dependency Analysis", "skills/vulnerability-scanner/scripts/dependency_analyzer.py", "P0 Security"),
    CheckSpec("Lint Check", "skills/lint-and-validate/scripts/lint_runner.py", "P1 Code Quality", required=True),
    CheckSpec("Type Coverage", "skills/lint-and-validate/scripts/type_coverage.py", "P1 Code Quality"),
    CheckSpec("Schema Validation", "skills/database-design/scripts/schema_validator.py", "P2 Data Layer"),
    CheckSpec("Test Suite", "skills/testing-patterns/scripts/test_runner.py", "P3 Testing"),
    CheckSpec("UX Audit", "skills/frontend-design/scripts/ux_audit.py", "P4 UX & Accessibility"),
    CheckSpec("Accessibility Check", "skills/frontend-design/scripts/accessibility_checker.py", "P4 UX & Accessibility"),
    CheckSpec("SEO Check", "skills/seo-fundamentals/scripts/seo_checker.py", "P5 SEO & Content"),
    CheckSpec("GEO Check", "skills/geo-fundamentals/scripts/geo_checker.py", "P5 SEO & Content"),
    CheckSpec("Bundle Analysis", "skills/performance-profiling/scripts/bundle_analyzer.py", "P6 Build Performance"),
    CheckSpec("React Performance", "skills/nextjs-react-expert/scripts/react_performance_checker.py", "P6 Build Performance"),
    CheckSpec("Mobile Audit", "skills/mobile-design/scripts/mobile_audit.py", "P7 Platform Quality"),
    CheckSpec("i18n Check", "skills/i18n-localization/scripts/i18n_checker.py", "P7 Platform Quality"),
    CheckSpec("API Validation", "skills/api-patterns/scripts/api_validator.py", "P7 Platform Quality"),
    CheckSpec("Lighthouse Audit", "skills/performance-profiling/scripts/lighthouse_audit.py", "P8 Runtime", target="url", required=True, timeout=180),
    CheckSpec("Playwright E2E", "skills/webapp-testing/scripts/playwright_runner.py", "P8 Runtime", target="url", timeout=120),
)


def main() -> int:
    parser = argparse.ArgumentParser(description="Run the complete AG Kit verification suite")
    parser.add_argument("project", nargs="?", default=".", help="Project path to validate")
    parser.add_argument("--url", help="Running application URL for Lighthouse and Playwright")
    parser.add_argument("--no-e2e", action="store_true", help="Skip Playwright")
    parser.add_argument("--no-runtime", action="store_true", help="Skip all URL-based runtime checks")
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

    specs = []
    for spec in VERIFICATION_SUITE:
        if args.no_runtime and spec.target == "url":
            continue
        if args.no_e2e and spec.name == "Playwright E2E":
            continue
        specs.append(spec)

    started = datetime.now(timezone.utc)
    CONSOLE.header("AG KIT - FULL VERIFICATION")
    print(f"Project: {project}\nToolkit: {toolkit_root}\nURL: {args.url or 'not provided'}")

    results = execute_suite(specs, toolkit_root, project, args.url, args.stop_on_fail)
    success = print_summary("FULL VERIFICATION REPORT", results, started)
    if args.report:
        write_report(args.report.resolve(), project, toolkit_root, results, started)
        print(f"Report: {args.report.resolve()}")
    return 0 if success else 1


if __name__ == "__main__":
    raise SystemExit(main())
