# AG Kit Runtime Scripts

## Entry points

### `validate_kit.py`

Self-validates the `.agents/` package. It checks JSON, strict SemVer frontmatter, workflow/agent/skill references, manifest and lock synchronization, generated dependency docs, memory contracts, local Markdown links, Python syntax, referenced script paths, and documented inventory counts.

```bash
python .agents/scripts/validate_kit.py
python .agents/scripts/validate_kit.py --json
```


### `generate_manifest.py`

Builds deterministic `manifest.json` and `manifest.lock.json` files from component frontmatter. Use `--check` in CI to fail on registry drift.

```bash
python .agents/scripts/generate_manifest.py
python .agents/scripts/generate_manifest.py --check
```

### `dependency_graph.py`

Builds `DEPENDENCY_GRAPH.md` from workflow and agent dependencies. Use `--check` to verify that the generated graph is current.

```bash
python .agents/scripts/dependency_graph.py
python .agents/scripts/dependency_graph.py --check
```

### `checklist.py`

Runs the fast project validation path. URL checks are automatically skipped when `--url` is not supplied.

```bash
python .agents/scripts/checklist.py .
python .agents/scripts/checklist.py . --url http://localhost:3000
python .agents/scripts/checklist.py . --report .agents/reports/checklist.json
```

### `verify_all.py`

Runs the broad release suite. `--no-runtime` excludes Lighthouse and Playwright; `--no-e2e` excludes Playwright only.

```bash
python .agents/scripts/verify_all.py . --no-runtime
python .agents/scripts/verify_all.py . --url http://localhost:3000 --report verification.json
```

### `session_manager.py`

Prints project type, package metadata, feature hints, and file counts.

```bash
python .agents/scripts/session_manager.py status .
```

### `auto_preview.py`

Starts, stops, or inspects a detected local preview server.

```bash
python .agents/scripts/auto_preview.py start 3000
python .agents/scripts/auto_preview.py status
python .agents/scripts/auto_preview.py stop
```

## Runtime prerequisites

Most scripts use only the Python standard library. Optional checks require the corresponding project tools:

- Lighthouse: `npm install -g lighthouse`
- Playwright: `pip install playwright && playwright install chromium`
- Node lint/test commands: project package manager and configured scripts
- Python lint/test commands: project-configured tools such as Ruff, MyPy, or pytest

Missing URL-based tools produce a failed runtime check rather than a false pass. Missing non-applicable build outputs are reported as skipped/pass with an explanatory message.

## Exit-code contract

- `0`: check passed or was not applicable
- `1`: findings met the configured failure threshold
- `2`: invalid command usage or missing input path

The master runners use subprocess exit codes as the source of truth and can emit a full JSON report with commands, durations, stdout, stderr, and status.

## Regression tests

Toolkit scripts include regression tests for self-validation, component SemVer, manifest/lock integrity, dependency graph synchronization, memory contracts, security scanning, dependency and bundle analysis, and toolkit-root discovery.

```bash
python -m unittest discover -s .agents/scripts/tests -v
```
