# Setting Up the GitHub Wiki

This guide explains how to publish the API documentation from `docs/wiki/` to the GitHub Wiki.

## Quick Setup (Recommended)

### Step 1: Enable GitHub Wiki

1. Go to your repository: https://github.com/tomassirio/tracker-backend
2. Click on **Settings** tab
3. Scroll down to **Features** section
4. Check the box for **Wikis** if not already enabled

### Step 2: Clone the Wiki Repository

```bash
# Clone the wiki repository (separate from main repo)
git clone https://github.com/tomassirio/tracker-backend.wiki.git

# Or if you prefer SSH
git clone git@github.com:tomassirio/tracker-backend.wiki.git
```

### Step 3: Copy Documentation Files

```bash
# From your main repository directory
cd /path/to/tracker-backend

# Copy all wiki files to the wiki repository
cp docs/wiki/*.md ../tracker-backend.wiki/

# Don't copy the README.md and SETUP-INSTRUCTIONS.md (they're for the main repo)
cd ../tracker-backend.wiki
rm README.md SETUP-INSTRUCTIONS.md 2>/dev/null || true
```

### Step 4: Commit and Push to Wiki

```bash
cd /path/to/tracker-backend.wiki

# Add all files
git add .

# Commit
git commit -m "Add comprehensive API documentation

- API Overview and architecture
- Authentication and security guides
- Complete API reference for all endpoints
- Getting started guide with examples
- Response formats and error handling"

# Push to wiki
git push origin master
```

### Step 5: Verify

Visit https://github.com/tomassirio/tracker-backend/wiki to see your documentation!

---

## Alternative: Manual Upload via GitHub UI

If you prefer to use the GitHub web interface:

1. Go to https://github.com/tomassirio/tracker-backend/wiki
2. Click **"New Page"** button
3. For each markdown file in `docs/wiki/`:
   - Title: Use the filename without `.md` (e.g., "Home", "API-Overview")
   - Content: Copy and paste the content from the file
   - Click **"Save Page"**

**Pages to create:**
- Home (from Home.md)
- API-Overview (from API-Overview.md)
- Authentication-API (from Authentication-API.md)
- User-API (from User-API.md)
- Trip-API (from Trip-API.md)
- Trip-Plan-API (from Trip-Plan-API.md)
- Trip-Update-API (from Trip-Update-API.md)
- Comment-API (from Comment-API.md)
- API-Response-Formats (from API-Response-Formats.md)
- Getting-Started-with-APIs (from Getting-Started-with-APIs.md)
- Security-and-Authorization (from Security-and-Authorization.md)

---

## Automated Setup Script

Save this as `setup-wiki.sh` and run it:

```bash
#!/bin/bash

# Configuration
REPO_DIR="$(pwd)"
WIKI_DIR="../tracker-backend.wiki"
WIKI_URL="https://github.com/tomassirio/tracker-backend.wiki.git"

echo "🚀 Setting up GitHub Wiki..."

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
    git clone "$WIKI_URL"
    cd "$REPO_DIR"
fi

# Copy wiki files
echo "📄 Copying wiki files..."
cp docs/wiki/*.md "$WIKI_DIR/"

# Remove files that shouldn't be in wiki
cd "$WIKI_DIR"
rm -f README.md SETUP-INSTRUCTIONS.md

# Commit and push
echo "📤 Committing and pushing to wiki..."
git add .
git commit -m "Update API documentation" || echo "No changes to commit"
git push origin master

echo "✅ Wiki setup complete!"
echo "Visit: https://github.com/tomassirio/tracker-backend/wiki"
```

Make it executable and run:
```bash
chmod +x setup-wiki.sh
./setup-wiki.sh
```

---

## Keeping Wiki Updated

When you update the API documentation:

1. **Edit files in `docs/wiki/`** in your main repository
2. **Commit to main repository**:
   ```bash
   git add docs/wiki/
   git commit -m "Update API documentation"
   git push
   ```
3. **Update wiki** (using script or manual copy):
   ```bash
   ./setup-wiki.sh
   ```

---

## Wiki Best Practices

### Page Naming
- Use hyphens for spaces: `API-Overview.md`
- Use title case: `Getting-Started-with-APIs.md`
- Be descriptive but concise

### Content Organization
- Keep the Home page as a navigation hub
- Group related pages together
- Use consistent formatting across pages

### Linking Between Pages
Use this format for internal links:
```markdown
[Link Text](Page-Name)
```

Examples:
```markdown
[Authentication API](Authentication-API)
[Getting Started](Getting-Started-with-APIs)
```

### Maintaining Freshness
- Review documentation with each API change
- Update examples when endpoints change
- Keep version numbers current
- Test all code examples

---

## Troubleshooting

### "Wiki repository not found"

**Solution**: Make sure Wiki is enabled in repository settings:
1. Go to Settings → Features → Enable Wikis
2. Create at least one page via the web UI
3. Then you can clone the wiki repository

### "Permission denied"

**Solution**: 
- Make sure you have write access to the repository
- Use SSH if HTTPS authentication fails: `git clone git@github.com:tomassirio/tracker-backend.wiki.git`

### "Wiki pages not linking correctly"

**Solution**:
- GitHub Wiki converts spaces to hyphens automatically
- File name `Getting Started.md` becomes link `[text](Getting-Started)`
- Use the exact page name (with hyphens) in links

### "Images not showing"

**Solution**:
- Upload images via GitHub Wiki UI
- Reference them in markdown: `![alt text](image-name.png)`
- Or use absolute URLs to images in the main repo

---

## Additional Resources

- **GitHub Wiki Documentation**: https://docs.github.com/en/communities/documenting-your-project-with-wikis
- **Markdown Guide**: https://www.markdownguide.org/
- **GitHub Flavored Markdown**: https://github.github.com/gfm/

---

## Questions?

If you encounter issues setting up the wiki:
1. Check this guide first
2. Search GitHub Wiki documentation
3. Open an issue: https://github.com/tomassirio/tracker-backend/issues

---

**Happy Documenting! 📚**
