#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

# Local ktlint formatting script
# Usage: ./Scripts/format.sh [--check]
# - format.sh          : Format all Kotlin files
# - format.sh --check  : Check formatting without modifying files

COMMAND="${1:-format}"

# Check if ktlint is installed
if ! command -v ktlint &> /dev/null; then
    echo "ktlint is not installed. Install it with:"
    echo "  brew install ktlint  (macOS)"
    echo "  choco install ktlint (Windows)"
    echo "  Or download from: https://github.com/pinterest/ktlint/releases"
    exit 1
fi

echo "Running ktlint ($COMMAND mode)..."

case "$COMMAND" in
    format)
        ktlint --editorconfig=.editorconfig --format "**/*.kt" "**/*.kts" --reporter=plain
        ;;
    --check)
        ktlint --editorconfig=.editorconfig "**/*.kt" "**/*.kts" --reporter=plain
        ;;
    *)
        echo "Unknown option: $COMMAND"
        echo "Usage: ./Scripts/format.sh [--check]"
        exit 2
        ;;
esac

