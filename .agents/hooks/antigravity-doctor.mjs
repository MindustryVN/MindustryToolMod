#!/usr/bin/env node

import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';

function parseArgs(argv) {
  const options = {root: process.cwd(), json: false, strict: false};
  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    if (arg === '--root') options.root = path.resolve(argv[++i]);
    else if (arg === '--json') options.json = true;
    else if (arg === '--strict') options.strict = true;
    else if (arg === '--help') options.help = true;
    else throw new Error(`Unknown argument: ${arg}`);
  }
  return options;
}

function readJson(file) {
  return JSON.parse(fs.readFileSync(file, 'utf8'));
}

function frontmatter(file) {
  const text = fs.readFileSync(file, 'utf8');
  if (!text.startsWith('---\n')) return null;
  const end = text.indexOf('\n---\n', 4);
  if (end < 0) return null;
  const data = {};
  for (const line of text.slice(4, end).split(/\r?\n/)) {
    const match = line.match(/^([A-Za-z0-9_-]+):\s*(.*)$/);
    if (!match) continue;
    data[match[1]] = match[2].trim().replace(/^['"]|['"]$/g, '');
  }
  return data;
}

function markdownFiles(dir) {
  if (!fs.existsSync(dir)) return [];
  return fs.readdirSync(dir)
    .filter(name => name.endsWith('.md'))
    .map(name => path.join(dir, name))
    .sort();
}

function skillFiles(dir) {
  if (!fs.existsSync(dir)) return [];
  return fs.readdirSync(dir, {withFileTypes: true})
    .filter(entry => entry.isDirectory())
    .map(entry => path.join(dir, entry.name, 'SKILL.md'))
    .filter(file => fs.existsSync(file))
    .sort();
}

function add(report, severity, phase, code, file, message) {
  report.findings.push({severity, phase, code, file, message});
}

function relative(root, file) {
  return path.relative(root, file).split(path.sep).join('/');
}

function checkDiscovery(root, report) {
  const agentsRoot = path.join(root, '.agents');
  const groups = [
    ['rules', markdownFiles(path.join(agentsRoot, 'rules')), ['trigger']],
    ['workflows', markdownFiles(path.join(agentsRoot, 'workflows')), ['description']],
    ['skills', skillFiles(path.join(agentsRoot, 'skills')), ['description']]
  ];

  for (const [kind, files, required] of groups) {
    report.counts[kind] = files.length;
    if (files.length === 0) {
      add(report, 'error', 'discovery', `${kind}.missing`, `.agents/${kind}`, `No Antigravity ${kind} were discovered.`);
      continue;
    }
    for (const file of files) {
      const meta = frontmatter(file);
      if (!meta) {
        add(report, 'error', 'discovery', `${kind}.frontmatter`, relative(root, file), 'Missing YAML frontmatter.');
        continue;
      }
      for (const field of required) {
        if (!meta[field]) add(report, 'error', 'discovery', `${kind}.required`, relative(root, file), `Missing frontmatter field: ${field}`);
      }
    }
  }
}

function walkStrings(value, callback) {
  if (typeof value === 'string') callback(value);
  else if (Array.isArray(value)) value.forEach(item => walkStrings(item, callback));
  else if (value && typeof value === 'object') Object.values(value).forEach(item => walkStrings(item, callback));
}

function checkMcp(root, report) {
  const file = path.join(root, '.agents', 'mcp_config.json');
  if (!fs.existsSync(file)) {
    add(report, 'error', 'mcp', 'mcp.missing', '.agents/mcp_config.json', 'Workspace MCP configuration is missing.');
    return;
  }
  let config;
  try {
    config = readJson(file);
  } catch (error) {
    add(report, 'error', 'mcp', 'mcp.invalid_json', '.agents/mcp_config.json', error.message);
    return;
  }
  if (!config.mcpServers || typeof config.mcpServers !== 'object' || Array.isArray(config.mcpServers)) {
    add(report, 'error', 'mcp', 'mcp.servers', '.agents/mcp_config.json', 'mcpServers must be an object.');
    return;
  }
  report.counts.mcpServers = Object.keys(config.mcpServers).length;
  for (const [name, server] of Object.entries(config.mcpServers)) {
    const valid = server && typeof server === 'object' && (
      typeof server.command === 'string' || typeof server.serverURL === 'string' || typeof server.url === 'string'
    );
    if (!valid) add(report, 'error', 'mcp', 'mcp.server_shape', `.agents/mcp_config.json#${name}`, 'Server needs command, serverURL, or url.');
    walkStrings(server, value => {
      if (/YOUR_[A-Z0-9_]+|CHANGE_ME|<[^>]+>/.test(value)) {
        add(report, 'warning', 'mcp', 'mcp.placeholder', `.agents/mcp_config.json#${name}`, 'Server contains an unresolved placeholder; configure it before enabling the server.');
      }
    });
  }
}

function localCommandPath(command) {
  const match = command.match(/(?:^|\s)(\.agents[/\\][^\s"']+)/);
  return match ? match[1] : null;
}

function checkHooks(root, report) {
  const file = path.join(root, '.agents', 'hooks.json');
  if (!fs.existsSync(file)) {
    add(report, 'error', 'hooks', 'hooks.missing', '.agents/hooks.json', 'Native Antigravity hooks configuration is missing.');
    return;
  }
  let config;
  try {
    config = readJson(file);
  } catch (error) {
    add(report, 'error', 'hooks', 'hooks.invalid_json', '.agents/hooks.json', error.message);
    return;
  }
  if (typeof config.enabled !== 'boolean') add(report, 'error', 'hooks', 'hooks.enabled', '.agents/hooks.json', 'enabled must be boolean.');
  const events = ['PreToolUse', 'PostToolUse', 'PreInvocation', 'PostInvocation', 'Stop'];
  let total = 0;
  for (const event of events) {
    if (config[event] === undefined) continue;
    if (!Array.isArray(config[event])) {
      add(report, 'error', 'hooks', 'hooks.event_shape', `.agents/hooks.json#${event}`, `${event} must be an array.`);
      continue;
    }
    for (const [index, hook] of config[event].entries()) {
      total += 1;
      const ref = `.agents/hooks.json#${event}[${index}]`;
      if (!hook || typeof hook !== 'object') {
        add(report, 'error', 'hooks', 'hooks.hook_shape', ref, 'Hook must be an object.');
        continue;
      }
      if (typeof hook.matcher !== 'string' || !hook.matcher.trim()) add(report, 'error', 'hooks', 'hooks.matcher', ref, 'matcher is required.');
      if (typeof hook.command !== 'string' || !hook.command.trim()) add(report, 'error', 'hooks', 'hooks.command', ref, 'command is required.');
      if (!Number.isInteger(hook.timeout) || hook.timeout < 1 || hook.timeout > 300) add(report, 'error', 'hooks', 'hooks.timeout', ref, 'timeout must be an integer from 1 to 300 seconds.');
      const localPath = typeof hook.command === 'string' ? localCommandPath(hook.command) : null;
      if (localPath && !fs.existsSync(path.join(root, localPath))) add(report, 'error', 'hooks', 'hooks.command_missing', ref, `Local hook target does not exist: ${localPath}`);
    }
  }
  report.counts.hooks = total;
  if (total === 0) add(report, 'warning', 'hooks', 'hooks.empty', '.agents/hooks.json', 'No native hooks are registered.');
}

function checkOrchestration(root, report, contract) {
  const cfg = contract?.phases?.orchestration ?? {};
  const groups = [
    ['workflow', cfg.workflows ?? [], name => path.join(root, '.agents', 'workflows', `${name}.md`)],
    ['agent', cfg.agents ?? [], name => path.join(root, '.agents', 'agent', `${name}.md`)],
    ['skill', cfg.skills ?? [], name => path.join(root, '.agents', 'skills', name, 'SKILL.md')]
  ];
  for (const [kind, names, resolve] of groups) {
    for (const name of names) {
      const file = resolve(name);
      if (!fs.existsSync(file)) add(report, 'error', 'orchestration', `orchestration.${kind}_missing`, relative(root, file), `Required Antigravity ${kind} is missing.`);
    }
  }
}

function checkPlugin(root, report) {
  const files = [
    '.agents/hooks/build-plugin.mjs',
    '.agents/hooks/plugin/GEMINI.md',
    '.agents/hooks/plugin/gemini-extension.template.json'
  ];
  for (const file of files) {
    if (!fs.existsSync(path.join(root, file))) add(report, 'error', 'plugin', 'plugin.file_missing', file, 'Plugin packaging input is missing.');
  }
}

function checkValidation(root, report) {
  const requiredFiles = [
    '.agents/hooks/tests/antigravity.test.mjs',
    'MIGRATION.md',
    'SECURITY.md'
  ];
  for (const file of requiredFiles) {
    if (!fs.existsSync(path.join(root, file))) add(report, 'error', 'validation', 'validation.file_missing', file, 'Production validation or operator documentation is missing.');
  }

  const versionFiles = [
    ['.agents/VERSION', value => value.trim()],
    ['package.json', value => JSON.parse(value).version],
    ['cli/package.json', value => JSON.parse(value).version],
    ['web/package.json', value => JSON.parse(value).version]
  ];
  const versions = [];
  for (const [file, parse] of versionFiles) {
    const target = path.join(root, file);
    if (!fs.existsSync(target)) {
      add(report, 'error', 'validation', 'validation.version_missing', file, 'Version source is missing.');
      continue;
    }
    try {
      versions.push([file, parse(fs.readFileSync(target, 'utf8'))]);
    } catch (error) {
      add(report, 'error', 'validation', 'validation.version_invalid', file, error.message);
    }
  }
  const unique = new Set(versions.map(([, value]) => value));
  if (unique.size > 1) add(report, 'error', 'validation', 'validation.version_mismatch', 'VERSION', `Release versions are not synchronized: ${versions.map(([file, value]) => `${file}=${value}`).join(', ')}`);
  report.counts.releaseVersions = Object.fromEntries(versions);
}

export function diagnose(root) {
  const report = {runtime: 'antigravity', root, passed: true, counts: {}, phases: {}, findings: []};
  const contractFile = path.join(root, '.agents', 'antigravity.json');
  let contract;
  try {
    contract = readJson(contractFile);
    if (contract.runtime !== 'antigravity') add(report, 'error', 'discovery', 'contract.runtime', '.agents/antigravity.json', 'runtime must be antigravity.');
  } catch (error) {
    add(report, 'error', 'discovery', 'contract.invalid', '.agents/antigravity.json', error.message);
  }

  checkDiscovery(root, report);
  checkMcp(root, report);
  checkHooks(root, report);
  checkOrchestration(root, report, contract);
  checkPlugin(root, report);
  checkValidation(root, report);

  for (const phase of ['discovery', 'mcp', 'hooks', 'orchestration', 'plugin', 'validation']) {
    report.phases[phase] = !report.findings.some(item => item.phase === phase && item.severity === 'error');
  }
  report.passed = !report.findings.some(item => item.severity === 'error');
  return report;
}

function printHuman(report) {
  console.log(`AG Kit Antigravity doctor: ${report.root}`);
  for (const [phase, passed] of Object.entries(report.phases)) console.log(`${passed ? '[PASS]' : '[FAIL]'} ${phase}`);
  for (const item of report.findings) console.log(`[${item.severity.toUpperCase()}] ${item.file} ${item.code} - ${item.message}`);
  console.log(`Counts: ${JSON.stringify(report.counts)}`);
  console.log(report.passed ? '[PASS] Antigravity contract is ready.' : '[FAIL] Antigravity contract has blocking findings.');
}

if (import.meta.url === `file://${process.argv[1]}`) {
  try {
    const options = parseArgs(process.argv.slice(2));
    if (options.help) {
      console.log('Usage: node .agents/hooks/antigravity-doctor.mjs [--root PATH] [--json] [--strict]');
      process.exit(0);
    }
    const report = diagnose(options.root);
    if (options.json) console.log(JSON.stringify(report, null, 2));
    else printHuman(report);
    const hasWarnings = report.findings.some(item => item.severity === 'warning');
    process.exitCode = report.passed && !(options.strict && hasWarnings) ? 0 : 1;
  } catch (error) {
    console.error(error.message);
    process.exitCode = 2;
  }
}
