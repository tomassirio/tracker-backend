# Wiki Documentation

This directory contains comprehensive API documentation for the Trip Tracker Backend. These markdown files are designed to be used with GitHub Wiki.

## Setting Up GitHub Wiki

To add these pages to your GitHub Wiki:

### Option 1: Clone Wiki Repository and Copy Files

1. Clone the wiki repository:
   ```bash
   git clone https://github.com/tomassirio/tracker-backend.wiki.git
   ```

2. Copy all markdown files from this directory:
   ```bash
   cp docs/wiki/*.md tracker-backend.wiki/
   ```

3. Commit and push to wiki:
   ```bash
   cd tracker-backend.wiki
   git add .
   git commit -m "Add comprehensive API documentation"
   git push origin master
   ```

### Option 2: Manual Creation via GitHub UI

1. Go to https://github.com/tomassirio/tracker-backend/wiki
2. Click "New Page" for each markdown file
3. Copy the content from each `.md` file in this directory
4. Use the filename (without `.md`) as the page title

### Option 3: Use GitHub Wiki as Submodule (Advanced)

```bash
# In your main repository
git submodule add https://github.com/tomassirio/tracker-backend.wiki.git docs/wiki-repo

# Copy documentation
cp docs/wiki/*.md docs/wiki-repo/

# Commit to wiki
cd docs/wiki-repo
git add .
git commit -m "Update API documentation"
git push
```

## Documentation Structure

The wiki consists of the following pages:

### Getting Started
- **Home.md** - Wiki homepage with navigation and overview
- **API-Overview.md** - Introduction to the API architecture
- **Getting-Started-with-APIs.md** - Quick start guide with examples

### Authentication & Security
- **Authentication-API.md** - User registration and login
- **Security-and-Authorization.md** - Security model and authorization rules

### API References
- **User-API.md** - User management endpoints
- **Trip-API.md** - Trip CRUD operations
- **Trip-Plan-API.md** - Trip planning and routes
- **Trip-Update-API.md** - Location tracking and updates
- **Comment-API.md** - Comments and reactions

### Reference Guides
- **API-Response-Formats.md** - Response structures and error handling

## Navigation Between Pages

GitHub Wiki automatically converts markdown links to wiki pages. Use this format:

```markdown
[Link Text](Page-Name)
```

For example:
```markdown
[Authentication API](Authentication-API)
[Trip API](Trip-API)
```

## Updating Documentation

When making changes to the API:

1. Update the corresponding markdown file in `docs/wiki/`
2. Commit changes to the main repository
3. Copy updated files to the wiki repository (using one of the methods above)
4. The wiki will be updated automatically

## Documentation Guidelines

When adding or modifying documentation:

- Use clear, concise language
- Include code examples for all endpoints
- Show both request and response formats
- Document error cases
- Keep examples up-to-date with the API

## Viewing Documentation Locally

You can view the wiki documentation locally using any markdown viewer or by running a local markdown server:

```bash
# Using Python markdown server
pip install grip
cd docs/wiki
grip Home.md
# Open http://localhost:6419 in browser
```

## Interactive API Documentation

In addition to this wiki, interactive API documentation is available via Swagger UI:

- **Auth Service**: http://localhost:8083/swagger-ui.html
- **Command Service**: http://localhost:8081/swagger-ui.html
- **Query Service**: http://localhost:8082/swagger-ui.html

The Swagger UI provides:
- Interactive API testing
- Detailed schema documentation
- Request/response examples
- Authentication support

## Contributing

To contribute to the documentation:

1. Edit the markdown files in `docs/wiki/`
2. Test your changes by viewing locally
3. Submit a pull request with your changes
4. Once merged, update the wiki repository

## Questions or Issues

If you find errors or have suggestions for improving the documentation:

- Open an issue: https://github.com/tomassirio/tracker-backend/issues
- Tag it with the `documentation` label
- Describe the problem or suggestion clearly

---

**Last Updated**: October 2025
