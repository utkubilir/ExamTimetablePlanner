# Release Packaging Guide

Instructions for creating distributable packages of ExamTimetablePlanner.

---

## Prerequisites

- JDK 21+ with `jpackage` tool
- Maven 3.9+
- Platform-specific requirements:
  - **Windows:** WiX Toolset 3.x for MSI creation
  - **macOS:** Xcode Command Line Tools
  - **Linux:** `dpkg-deb` for .deb, `rpm-build` for .rpm

---

## Step 1: Build Shaded JAR

First, create a fat JAR with all dependencies:

```bash
cd ExamTimetablePlanner
mvn clean package -Pshaded-jar -DskipTests
```

This produces: `target/examplanner-1.0.1.jar`

---

## Step 2: Create Platform Packages

### Windows (MSI Installer)

```bash
jpackage \
  --input target \
  --name "ExamPlanner" \
  --main-jar examplanner-1.0.1.jar \
  --main-class com.examplanner.ui.Launcher \
  --type msi \
  --icon icon.ico \
  --win-menu \
  --win-shortcut \
  --app-version 1.0.1 \
  --vendor "SE-302 Team"
```

Output: `ExamPlanner-1.0.1.msi`

### macOS (DMG)

```bash
jpackage \
  --input target \
  --name "ExamPlanner" \
  --main-jar examplanner-1.0.1.jar \
  --main-class com.examplanner.ui.Launcher \
  --type dmg \
  --icon icon.icns \
  --mac-package-identifier com.examplanner \
  --app-version 1.0.1 \
  --vendor "SE-302 Team"
```

Output: `ExamPlanner-1.0.1.dmg`

> **Note:** You need to create `icon.icns` from `icon.png`:
> ```bash
> mkdir icon.iconset
> sips -z 512 512 icon.png --out icon.iconset/icon_512x512.png
> iconutil -c icns icon.iconset
> ```

### Linux (DEB)

```bash
jpackage \
  --input target \
  --name "examplanner" \
  --main-jar examplanner-1.0.1.jar \
  --main-class com.examplanner.ui.Launcher \
  --type deb \
  --linux-shortcut \
  --app-version 1.0.1 \
  --vendor "SE-302 Team"
```

Output: `examplanner_1.0.1-1_amd64.deb`

---

## Step 3: Test Packages

Before release, verify each package:

1. **Install** on a clean machine (or VM)
2. **Launch** the application
3. **Import** sample data from `sample_data/small/`
4. **Generate** a schedule
5. **Export** the result
6. **Uninstall** cleanly

---

## GitHub Releases

### Creating a Release

1. Go to [Releases](https://github.com/UtkuBilir/ExamTimetablePlanner/releases)
2. Click "Draft a new release"
3. Tag: `v1.0.1`
4. Title: `ExamPlanner v1.0.1`
5. Description: Include changelog
6. Attach binaries:
   - `ExamPlanner-1.0.1.msi` (Windows)
   - `ExamPlanner-1.0.1.dmg` (macOS)
   - `examplanner_1.0.1-1_amd64.deb` (Linux)
   - `examplanner-1.0.1.jar` (Cross-platform)

### Release Checklist

- [ ] Version number updated in `pom.xml`
- [ ] CHANGELOG updated
- [ ] All tests passing
- [ ] README reflects current features
- [ ] Shaded JAR builds successfully
- [ ] Platform packages created and tested
- [ ] GitHub Release created with binaries
- [ ] Documentation updated to reference new version

---

## Alternative: JAR Distribution

For users who prefer a cross-platform JAR:

```bash
# User runs:
java -jar examplanner-1.0.1.jar
```

Requires user to have JDK 21+ with JavaFX installed.

---

## Versioning

Follow [Semantic Versioning](https://semver.org/):

- **MAJOR:** Breaking changes
- **MINOR:** New features, backward compatible
- **PATCH:** Bug fixes only

Current: `1.0.1`
