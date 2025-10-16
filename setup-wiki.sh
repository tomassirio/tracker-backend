#!/bin/bash

# Script to set up and update GitHub Wiki with API documentation

set -e

# Configuration
REPO_DIR="$(pwd)"
WIKI_DIR="../tracker-backend.wiki"
WIKI_URL="https://github.com/tomassirio/tracker-backend.wiki.git"

echo "🚀 Setting up GitHub Wiki for tracker-backend..."
echo ""

# Check if we're in the right directory
if [ ! -d "docs/wiki" ]; then
    echo "❌ Error: docs/wiki directory not found"
    echo "Please run this script from the repository root"
    exit 1
fi

# Clone wiki if it doesn't exist
if [ ! -d "$WIKI_DIR" ]; then
    echo "📥 Cloning wiki repository..."
    cd "$(dirname $REPO_DIR)"
    git clone "$WIKI_URL" || {
        echo "❌ Failed to clone wiki repository"
        echo "Make sure:"
        echo "  1. Wiki is enabled in repository settings"
        echo "  2. You have write access to the repository"
        echo "  3. At least one wiki page exists (create via GitHub UI)"
        exit 1
    }
    cd "$REPO_DIR"
    echo "✅ Wiki repository cloned"
else
    echo "📦 Wiki repository already exists"
fi

# Copy wiki files
echo "📄 Copying wiki documentation files..."
cp docs/wiki/*.md "$WIKI_DIR/"
echo "✅ Files copied"

# Remove files that shouldn't be in wiki
cd "$WIKI_DIR"
echo "🧹 Removing non-wiki files..."
rm -f README.md SETUP-INSTRUCTIONS.md

# Show what's being committed
echo ""
echo "📋 Files to be committed:"
git status --short

# Commit and push
echo ""
echo "📤 Committing and pushing to wiki..."
git add .

if git diff-index --quiet HEAD --; then
    echo "ℹ️  No changes to commit - wiki is already up to date"
else
    git commit -m "Update API documentation

- Comprehensive API reference for all endpoints
- Getting started guide with examples
- Authentication and security documentation
- Response formats and error handling guides
- CQRS architecture overview"
    
    git push origin master
    echo "✅ Wiki updated successfully!"
fi

echo ""
echo "🎉 Setup complete!"
echo ""
echo "📚 View your wiki at:"
echo "   https://github.com/tomassirio/tracker-backend/wiki"
echo ""
