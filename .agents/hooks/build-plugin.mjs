#!/usr/bin/env node

import crypto from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';

function parseArgs(argv) {
  const options = {root: process.cwd(), output: null};
  for (let i = 0; i < argv.length; i += 1) {
    if (argv[i] === '--root') options.root = path.resolve(argv[++i]);
    else if (argv[i] === '--output') options.output = path.resolve(argv[++i]);
    else throw new Error(`Unknown argument: ${argv[i]}`);
  }
  options.output ??= path.join(options.root, 'dist', 'antigravity-plugin');
  return options;
}

function copyTree(source, destination) {
  if (!fs.existsSync(source)) return 0;
  fs.cpSync(source, destination, {recursive: true});
  let count = 0;
  const visit = dir => {
    for (const entry of fs.readdirSync(dir, {withFileTypes: true})) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) visit(full);
      else count += 1;
    }
  };
  visit(source);
  return count;
}

function frontmatterDescription(text) {
  if (!text.startsWith('---\n')) return '';
  const end = text.indexOf('\n---\n', 4);
  if (end < 0) return '';
  const line = text.slice(4, end).split(/\r?\n/).find(item => item.startsWith('description:'));
  return line ? line.slice('description:'.length).trim().replace(/^['"]|['"]$/g, '') : '';
}

function tomlString(value) {
  return JSON.stringify(value);
}

function tomlMultiline(value) {
  return `'''\n${value.replace(/'''/g, "'\\''")}\n'''`;
}

function convertWorkflows(root, output) {
  const source = path.join(root, '.agents', 'workflows');
  const destination = path.join(output, 'commands', 'ag-kit');
  fs.mkdirSync(destination, {recursive: true});
  let count = 0;
  for (const name of fs.readdirSync(source).filter(item => item.endsWith('.md')).sort()) {
    const text = fs.readFileSync(path.join(source, name), 'utf8');
    const commandName = name.replace(/\.md$/, '');
    const description = frontmatterDescription(text) || `Run AG Kit workflow ${commandName}`;
    const prompt = `${text}\n\nUser arguments: {{args}}`;
    fs.writeFileSync(
      path.join(destination, `${commandName}.toml`),
      `description = ${tomlString(description)}\nprompt = ${tomlMultiline(prompt)}\n`,
      'utf8'
    );
    count += 1;
  }
  return count;
}

function sha256(file) {
  return crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
}

function inventory(output) {
  const files = [];
  const visit = dir => {
    for (const entry of fs.readdirSync(dir, {withFileTypes: true}).sort((a, b) => a.name.localeCompare(b.name))) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) visit(full);
      else files.push({path: path.relative(output, full).split(path.sep).join('/'), sha256: sha256(full)});
    }
  };
  visit(output);
  return files;
}

export function buildPlugin(root, output) {
  fs.rmSync(output, {recursive: true, force: true});
  fs.mkdirSync(output, {recursive: true});

  const version = fs.readFileSync(path.join(root, '.agents', 'VERSION'), 'utf8').trim();
  const template = JSON.parse(fs.readFileSync(path.join(root, '.agents', 'hooks', 'plugin', 'gemini-extension.template.json'), 'utf8'));
  template.version = version;
  fs.writeFileSync(path.join(output, 'gemini-extension.json'), `${JSON.stringify(template, null, 2)}\n`, 'utf8');
  fs.copyFileSync(path.join(root, '.agents', 'hooks', 'plugin', 'GEMINI.md'), path.join(output, 'GEMINI.md'));

  const counts = {
    skills: copyTree(path.join(root, '.agents', 'skills'), path.join(output, 'skills')),
    agents: copyTree(path.join(root, '.agents', 'agent'), path.join(output, 'agents')),
    rules: copyTree(path.join(root, '.agents', 'rules'), path.join(output, 'rules')),
    workflows: convertWorkflows(root, output)
  };

  fs.mkdirSync(path.join(output, 'hooks'), {recursive: true});
  fs.copyFileSync(path.join(root, '.agents', 'hooks.json'), path.join(output, 'hooks', 'hooks.json'));
  fs.copyFileSync(path.join(root, '.agents', 'hooks', 'validate-tool-call.mjs'), path.join(output, 'hooks', 'validate-tool-call.mjs'));
  fs.copyFileSync(path.join(root, '.agents', 'mcp_config.json'), path.join(output, 'mcp_config.example.json'));

  const manifest = {
    name: 'ag-kit',
    version,
    runtime: 'antigravity',
    counts,
    files: inventory(output)
  };
  fs.writeFileSync(path.join(output, 'PLUGIN_CONTENTS.json'), `${JSON.stringify(manifest, null, 2)}\n`, 'utf8');
  return manifest;
}

if (import.meta.url === `file://${process.argv[1]}`) {
  try {
    const options = parseArgs(process.argv.slice(2));
    const manifest = buildPlugin(options.root, options.output);
    console.log(`Built Antigravity plugin: ${options.output}`);
    console.log(JSON.stringify(manifest.counts));
  } catch (error) {
    console.error(`Plugin build failed: ${error.message}`);
    process.exitCode = 1;
  }
}
