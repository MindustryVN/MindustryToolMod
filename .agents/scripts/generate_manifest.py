#!/usr/bin/env python3
"""Generate or verify the AG Kit component registry and lock file."""
from __future__ import annotations

import argparse
import json
from pathlib import Path

from component_registry import build_lock, build_manifest, canonical_json


def check_file(path: Path, expected: str) -> bool:
    return path.is_file() and path.read_text("utf-8") == expected


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate AG Kit manifest.json and manifest.lock.json")
    parser.add_argument("path", nargs="?", default=None, help="Path to .agents")
    parser.add_argument("--check", action="store_true", help="Fail when generated files are stale")
    args = parser.parse_args()

    root = Path(args.path).resolve() if args.path else Path(__file__).resolve().parents[1]
    manifest = build_manifest(root)
    manifest_text = canonical_json(manifest)
    lock = build_lock(root, manifest)
    lock_text = canonical_json(lock)
    outputs = [(root / "manifest.json", manifest_text), (root / "manifest.lock.json", lock_text)]

    if args.check:
        stale = [path.name for path, expected in outputs if not check_file(path, expected)]
        if stale:
            print("Stale generated registry files: " + ", ".join(stale))
            return 1
        print("Component registry is synchronized.")
        return 0

    for path, content in outputs:
        path.write_text(content, "utf-8")
        print(f"Wrote {path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
