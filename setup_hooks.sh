#!/bin/sh

# Copy the hooks to the Git hooks directory
cp githooks/* .git/hooks

# Set the pre-commit hook as executable based on the operating system
if [ "$(uname)" = "Darwin" ]; then
    # macOS
    chmod +x .git/hooks/pre-push
else
    # Windows
    chmod +x .git/hooks/pre-push
    chmod +x .git/hooks/pre-push.bat
fi
