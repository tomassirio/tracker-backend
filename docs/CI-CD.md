# CI/CD Workflows

This document describes the GitHub Actions workflows for building, testing, and deploying the Tracker Backend application to GitHub Container Registry (GHCR).

## Workflows Overview

### 1. **CI Build** (`ci.yml`)
**Trigger**: Push to any branch except `main`

**Purpose**: Validates feature branches and publishes test images to GHCR

**Steps**:
- Runs full Maven build with tests (`mvn clean install`)
- Builds Docker images with Jib
- Pushes images to GHCR with `ci-test` tag for testing

**Images Published**:
- `ghcr.io/tomassirio/tracker-command:0.1.1-SNAPSHOT`
- `ghcr.io/tomassirio/tracker-command:ci-test`
- `ghcr.io/tomassirio/tracker-query:0.1.1-SNAPSHOT`
- `ghcr.io/tomassirio/tracker-query:ci-test`

**Usage**: Automatically runs on every push to feature branches

---

### 2. **Merge to Main** (`merge.yml`)
**Trigger**: Push to `main` branch

**Purpose**: Creates releases and publishes production images to GHCR

**Steps**:
1. Removes `-SNAPSHOT` from version
2. Commits release version
3. Builds project with Maven
4. Creates GitHub Release with JAR artifacts
5. Bumps to next development version
6. Builds and pushes Docker images to GHCR with `latest` tag

**Images Published**:
- `ghcr.io/tomassirio/tracker-command:0.1.1`
- `ghcr.io/tomassirio/tracker-command:latest`
- `ghcr.io/tomassirio/tracker-query:0.1.1`
- `ghcr.io/tomassirio/tracker-query:latest`

---

### 3. **Build Docker Images** (`docker/docker-build.yml`)
**Trigger**: Reusable workflow called by other workflows

**Purpose**: Builds Docker images for both services using Jib and pushes to GHCR

**Location**: `.github/workflows/docker/docker-build.yml`

**Features**:
- ✅ **Matrix strategy**: Builds tracker-command and tracker-query in parallel
- ✅ **Fail-fast disabled**: If one module fails, the other continues
- ✅ **ARM64 support**: Configured for Apple Silicon
- ✅ **Version extraction**: Automatically uses Maven project version
- ✅ **GHCR integration**: Uses GitHub token for authentication

**Parameters**:
- `push-to-registry`: Whether to push images to GHCR (default: false)
- `image-tag`: Additional tag for images (default: '')

**Secrets** (automatically provided):
- `registry-username`: GitHub username (`github.actor`)
- `registry-password`: GitHub token (`GITHUB_TOKEN`)

---

### 4. **Publish Docker Images** (`docker/docker-publish.yml`)
**Trigger**: Manual (workflow_dispatch)

**Purpose**: Manually publish Docker images to GHCR with custom tags

**Location**: `.github/workflows/docker/docker-publish.yml`

**Usage**:
1. Go to Actions tab in GitHub
2. Select "Publish Docker Images to GHCR"
3. Click "Run workflow"
4. Specify additional tag (e.g., `stable`, `v1.0.0`, `hotfix-123`)
5. Click "Run workflow"

**No Setup Required**: Uses automatic GitHub authentication

---

## Setup Instructions

### GitHub Container Registry (GHCR)

**No manual setup required!** The workflows automatically use:
- **Username**: `github.actor` (your GitHub username)
- **Password**: `GITHUB_TOKEN` (automatically available in GitHub Actions)
- **Registry**: `ghcr.io`

The `packages: write` permission in each workflow provides access to push images.

### Image Configuration

Your `pom.xml` is already configured:
```xml
<docker.image.prefix>ghcr.io/tomassirio</docker.image.prefix>
```

This creates images like:
- `ghcr.io/tomassirio/tracker-command:version`
- `ghcr.io/tomassirio/tracker-query:version`

---

## Workflow Architecture

### Reusable Workflow Pattern

Docker workflows are organized in `.github/workflows/docker/` for better organization:

```
.github/workflows/
├── ci.yml                    → Feature branch builds
├── merge.yml                 → Release builds
└── docker/
    ├── docker-build.yml      → Reusable build logic
    └── docker-publish.yml    → Manual publishing
```

**Workflow Relationships**:
```
┌─────────────────┐
│   ci.yml        │
│ (Feature Branch)│
└────────┬────────┘
         │
         │ calls
         ▼
┌──────────────────────────┐
│  docker/docker-build.yml │◄───────┐
│     (Reusable)           │        │
│                          │        │ calls
│ • Matrix Strategy        │        │
│ • Parallel Builds        │        │
│ • GHCR Push              │        │
└──────────────────────────┘        │
         ▲                          │
         │ calls                    │
         │                          │
┌────────┴────────┐   ┌─────────────┴──────────┐
│   merge.yml     │   │ docker/docker-publish  │
│  (Main Branch)  │   │     (Manual)           │
└─────────────────┘   └────────────────────────┘
```

### Benefits:
- **Organized**: Docker workflows in dedicated subfolder
- **DRY**: Build logic defined once
- **Consistent**: Same process across all workflows
- **Scalable**: Easy to add new modules

---

## Adding New Modules

To add a new Docker module (e.g., `tracker-analytics`):

1. **Add to matrix** in `docker/docker-build.yml`:
   ```yaml
   strategy:
     matrix:
       module: [tracker-command, tracker-query, tracker-analytics]
   ```

2. **Configure Jib** in the new module's `pom.xml`:
   ```xml
   <properties>
       <start-class>com.tomassirio.wanderer.analytics.TrackerAnalyticsApplication</start-class>
       <docker.image.name>${docker.image.prefix}/tracker-analytics</docker.image.name>
       <docker.image.port>8083</docker.image.port>
   </properties>
   ```

3. Done! All workflows automatically build the new module

---

## Image Naming Convention

Images are tagged with multiple versions:

**Format**: `ghcr.io/tomassirio/{module}:{tag}`

**Tags**:
- **Version tag**: `0.1.1-SNAPSHOT`, `0.1.1`
- **Environment tag**: `ci-test` (feature branches), `latest` (releases)
- **Custom tag**: `stable`, `v1.0.0`, etc. (manual)

**Examples**:
```
ghcr.io/tomassirio/tracker-command:0.1.1-SNAPSHOT  (CI builds)
ghcr.io/tomassirio/tracker-command:ci-test         (CI builds)
ghcr.io/tomassirio/tracker-command:0.1.1           (Release)
ghcr.io/tomassirio/tracker-command:latest          (Release)
ghcr.io/tomassirio/tracker-query:0.1.1-SNAPSHOT
ghcr.io/tomassirio/tracker-query:ci-test
ghcr.io/tomassirio/tracker-query:0.1.1
ghcr.io/tomassirio/tracker-query:latest
```

---

## Manual Publishing Workflow

### When to Use:
- Emergency hotfix deployment
- Creating specific version tags
- Testing before release
- Publishing beta/alpha versions

### How to Use:
1. Navigate to **Actions** tab
2. Select **"Publish Docker Images to GHCR"** workflow
3. Click **"Run workflow"** button
4. Configure:
   - **Branch**: Which branch to build from
   - **Image tag**: Custom tag (e.g., `hotfix-2024-10`, `beta`, `v1.2.3`)
5. Click **"Run workflow"**

---

## Troubleshooting

### Docker build fails with "platform mismatch"
**Solution**: The workflow uses `ubuntu-latest` (AMD64). Images are configured for ARM64 (Apple Silicon).
- The warning is informational - images work with emulation
- For production, consider multi-arch builds

### Images not appearing in GHCR
**Check**:
1. Workflow completed successfully
2. `packages: write` permission exists
3. Check the "Packages" tab in your repository

### Cannot pull images
**Solution**: Make packages public (see "Making Packages Public" below)

### Matrix build partially fails
**Behavior**: With `fail-fast: false`, if one module fails, others continue
**To fix**: Check the specific module's logs, fix the issue, re-run workflow

---

## Verifying Published Images

### On GitHub Container Registry (GHCR)

After your workflow completes successfully, verify the images were published:

**1. GitHub Packages UI:**
- Go to: `https://github.com/tomassirio/tracker-backend`
- Click "Packages" tab (right sidebar)
- You should see:
  - `tracker-command`
  - `tracker-query`

**2. Direct Package Links:**
- `https://github.com/tomassirio/tracker-backend/pkgs/container/tracker-command`
- `https://github.com/tomassirio/tracker-backend/pkgs/container/tracker-query`

**3. Command Line Verification:**
```bash
# Pull CI test images
docker pull ghcr.io/tomassirio/tracker-command:ci-test
docker pull ghcr.io/tomassirio/tracker-query:ci-test

# Pull version-specific images
docker pull ghcr.io/tomassirio/tracker-command:0.1.1-SNAPSHOT
docker pull ghcr.io/tomassirio/tracker-query:0.1.1-SNAPSHOT

# Pull latest release images
docker pull ghcr.io/tomassirio/tracker-command:latest
docker pull ghcr.io/tomassirio/tracker-query:latest
```

**4. List All Tags:**
View all available tags in the GitHub Packages UI, or use GitHub CLI:
```bash
gh api /user/packages/container/tracker-command/versions
```

### Making Packages Public

By default, GHCR packages are **private**. To make them public for unauthenticated pulls:

1. Go to package page: `https://github.com/tomassirio/tracker-backend/pkgs/container/tracker-command`
2. Click **"Package settings"** (right sidebar)
3. Scroll to **"Danger Zone"**
4. Click **"Change visibility"** → Select **"Public"**
5. Confirm the change
6. Repeat for `tracker-query`

**After making public**, anyone can pull without authentication:
```bash
docker pull ghcr.io/tomassirio/tracker-command:latest
```

### Checking Workflow Results

**In GitHub Actions UI:**
1. Repository → **"Actions"** tab
2. Click on the latest workflow run
3. Click on **"build-images"** job
4. Expand **"Tag and Push to GHCR"** step
5. Verify output shows successful pushes:
   ```
   The push refers to repository [ghcr.io/tomassirio/tracker-command]
   abc123def: Pushed
   0.1.1-SNAPSHOT: digest: sha256:... size: 1234
   ci-test: digest: sha256:... size: 1234
   ```

---

## Best Practices

### 1. **Feature Branches**
- Images automatically pushed to GHCR with `ci-test` tag
- Allows testing Docker deployments before merging
- Old `ci-test` tags are overwritten on each push

### 2. **Main Branch**
- Releases automatically publish with version and `latest` tags
- Version tags are immutable (e.g., `0.1.1`)
- `latest` always points to most recent release

### 3. **Package Visibility**
- Keep packages private during development
- Make public when ready to share
- Can change visibility at any time

### 4. **Version Tags**
- Maven version automatically becomes Docker tag
- Use semantic versioning (0.1.0, 1.0.0, 2.0.0)
- Never reuse version tags

---

## Future Enhancements

### Planned Improvements:
- [ ] Multi-architecture builds (AMD64 + ARM64)
- [ ] Vulnerability scanning with Trivy
- [ ] Image signing with Cosign
- [ ] Automated deployment to Kubernetes
- [ ] Build caching for faster builds
- [ ] Helm chart versioning and publishing
- [ ] Retention policy for old `ci-test` images

---

## Related Documentation

- [Docker Usage Guide](DOCKER.md)
- [Release Notes](release-notes.md)
- [GitHub Packages Documentation](https://docs.github.com/en/packages)
- [Jib Maven Plugin](https://github.com/GoogleContainerTools/jib)
