#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

ktlint --editorconfig=.editorconfig "**/*.kt" "**/*.kts" --reporter=plain --reporter=sarif,output=ktlint-report.sarif
