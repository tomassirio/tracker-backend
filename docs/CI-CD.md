# CI/CD Workflows

This document describes the GitHub Actions workflows for building, testing, and deploying the Tracker Backend application.

## Workflows Overview

### 1. **CI Build** (`ci.yml`)
**Trigger**: Push to any branch except `main`

**Purpose**: Validates feature branches before merging

**Steps**:
- Runs full Maven build with tests (`mvn clean install`)
- Builds Docker images locally (no push to registry)
- Ensures Docker images can be built successfully

**Usage**: Automatically runs on every push to feature branches

---

### 2. **Merge to Main** (`merge.yml`)
**Trigger**: Push to `main` branch

**Purpose**: Creates releases and builds production-ready artifacts

**Steps**:
1. Removes `-SNAPSHOT` from version
2. Commits release version
3. Builds project with Maven
4. Creates GitHub Release with JAR artifacts
5. Bumps to next development version
6. Builds Docker images (locally by default)

**Docker Publishing**: Currently builds images locally. Uncomment the configuration to push to a registry:

```yaml
build-release-docker-images:
  needs: version-and-release
  uses: ./.github/workflows/docker-build.yml
  with:
    push-to-registry: true
    registry: docker.io
    image-tag: latest
  secrets:
    registry-username: ${{ secrets.DOCKER_USERNAME }}
    registry-password: ${{ secrets.DOCKER_PASSWORD }}
```

---

### 3. **Build Docker Images** (`docker-build.yml`)
**Trigger**: Reusable workflow called by other workflows

**Purpose**: Builds Docker images for both services using Jib

**Features**:
- ✅ **Matrix strategy**: Builds tracker-command and tracker-query in parallel
- ✅ **Fail-fast disabled**: If one module fails, the other continues
- ✅ **Flexible**: Can build locally or push to registry
- ✅ **Multi-architecture**: Configured for ARM64 (Apple Silicon)
- ✅ **Version extraction**: Automatically uses Maven project version

**Parameters**:
- `push-to-registry`: Whether to push images to a registry (default: false)
- `registry`: Docker registry URL (default: docker.io)
- `image-tag`: Additional tag for images (default: '')

**Secrets**:
- `registry-username`: Docker registry username
- `registry-password`: Docker registry password/token

---

### 4. **Publish Docker Images** (`docker-publish.yml`)
**Trigger**: Manual (workflow_dispatch)

**Purpose**: Manually publish Docker images to a registry

**Usage**:
1. Go to Actions tab in GitHub
2. Select "Publish Docker Images"
3. Click "Run workflow"
4. Choose registry (Docker Hub or GitHub Container Registry)
5. Specify additional tag (e.g., `latest`, `stable`, `v1.0.0`)

**Prerequisites**: Set up secrets in your repository:
- `DOCKER_USERNAME`: Your Docker Hub username
- `DOCKER_PASSWORD`: Your Docker Hub access token

---

## Setup Instructions

### 1. Configure Docker Registry Secrets

#### For Docker Hub:
1. Go to Repository Settings → Secrets and variables → Actions
2. Add secrets:
   - `DOCKER_USERNAME`: Your Docker Hub username
   - `DOCKER_PASSWORD`: Docker Hub access token (not password!)

**Create Docker Hub token**:
- Go to https://hub.docker.com/settings/security
- Click "New Access Token"
- Copy the token and save it as `DOCKER_PASSWORD` secret

#### For GitHub Container Registry (ghcr.io):
1. Use `GITHUB_TOKEN` (automatically available)
2. Or create a Personal Access Token with `write:packages` scope

### 2. Enable Docker Push on Releases

Edit `.github/workflows/merge.yml` and uncomment the Docker publishing section:

```yaml
build-release-docker-images:
  needs: version-and-release
  uses: ./.github/workflows/docker-build.yml
  with:
    push-to-registry: true        # Enable pushing
    registry: docker.io           # Or ghcr.io
    image-tag: latest
  secrets:
    registry-username: ${{ secrets.DOCKER_USERNAME }}
    registry-password: ${{ secrets.DOCKER_PASSWORD }}
```

### 3. Update Image Prefix for Your Registry

If pushing to Docker Hub, update `pom.xml`:

```xml
<docker.image.prefix>yourusername</docker.image.prefix>
```

Or for GitHub Container Registry:

```xml
<docker.image.prefix>ghcr.io/yourusername</docker.image.prefix>
```

---

## Workflow Architecture

### Reusable Workflow Pattern

The Docker build workflow is designed as a **reusable workflow** for maximum flexibility:

```
┌─────────────────┐
│   ci.yml        │
│ (Feature Branch)│
└────────┬────────┘
         │
         │ calls
         ▼
┌─────────────────────┐
│  docker-build.yml   │◄───────┐
│  (Reusable)         │        │
│                     │        │ calls
│ • Matrix Strategy   │        │
│ • Parallel Builds   │        │
│ • Push Optional     │        │
└─────────────────────┘        │
         ▲                     │
         │ calls               │
         │                     │
┌────────┴────────┐   ┌────────┴──────────┐
│   merge.yml     │   │ docker-publish.yml│
│  (Main Branch)  │   │    (Manual)       │
└─────────────────┘   └───────────────────┘
```

### Benefits:
- **DRY**: Docker build logic defined once
- **Consistent**: Same process across all workflows
- **Flexible**: Easy to add new triggers
- **Scalable**: Adding new modules is trivial (just update matrix)

---

## Adding New Modules

To add a new Docker module (e.g., `tracker-analytics`):

1. **Add to matrix** in `docker-build.yml`:
   ```yaml
   strategy:
     matrix:
       module: [tracker-command, tracker-query, tracker-analytics]
   ```

2. **Configure Jib** in the new module's `pom.xml` (follow existing pattern)

3. Done! All workflows automatically build the new module

---

## Image Naming Convention

Images are tagged with multiple versions:

- **Version tag**: `tracker-backend/tracker-command:0.1.1-SNAPSHOT`
- **Latest tag**: `tracker-backend/tracker-command:latest`
- **Custom tag**: `tracker-backend/tracker-command:stable` (if specified)

Example after release:
```
docker.io/yourusername/tracker-command:0.1.0
docker.io/yourusername/tracker-command:latest
docker.io/yourusername/tracker-query:0.1.0
docker.io/yourusername/tracker-query:latest
```

---

## Manual Publishing Workflow

### When to Use:
- Emergency hotfix needs to be deployed
- Testing deployment to different registries
- Creating specific version tags
- Publishing without creating a full release

### How to Use:
1. Navigate to **Actions** tab
2. Select **"Publish Docker Images"** workflow
3. Click **"Run workflow"** button
4. Configure:
   - **Branch**: Which branch to build from
   - **Registry**: docker.io or ghcr.io
   - **Image tag**: Additional tag (e.g., `hotfix-2024-10`, `beta`)
5. Click **"Run workflow"**

---

## Troubleshooting

### Docker build fails with "platform mismatch"
**Solution**: The workflow uses `ubuntu-latest` which is AMD64. Images are configured for ARM64 by default (Apple Silicon). Either:
- Remove platform specification from `pom.xml` for multi-arch
- Use Docker Buildx with multiple platforms
- Accept the warning (images still work with emulation)

### Images not pushing to registry
**Check**:
1. Secrets are configured correctly
2. `push-to-registry: true` is set
3. Docker Hub token has write permissions
4. Image name matches registry format

### Matrix build partially fails
**Behavior**: With `fail-fast: false`, if one module fails, others continue
**To fix**: Check the specific module's logs, fix the issue, re-run workflow

---

## Best Practices

### 1. **Feature Branches**
- Always build Docker images to catch integration issues early
- Images are built but not pushed (keeps registry clean)

### 2. **Main Branch**
- Docker images are built on every release
- Consider enabling push for automatic deployment
- Images are tagged with release version

### 3. **Secrets Management**
- Use Personal Access Tokens, not passwords
- Rotate tokens regularly
- Use minimal scope permissions

### 4. **Version Tags**
- Maven version automatically becomes Docker tag
- `latest` tag always points to most recent build
- Use semantic versioning (v1.0.0, v1.1.0, etc.)

---

## Future Enhancements

### Planned Improvements:
- [ ] Multi-architecture builds (AMD64 + ARM64)
- [ ] Vulnerability scanning with Trivy
- [ ] Image signing with Cosign
- [ ] Automated deployment to Kubernetes
- [ ] Build caching for faster builds
- [ ] Helm chart versioning and publishing

---

## Related Documentation

- [Docker Usage Guide](DOCKER.md)
- [Release Notes](release-notes.md)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Jib Maven Plugin](https://github.com/GoogleContainerTools/jib)

