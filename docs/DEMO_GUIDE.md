# Demo Recording Guide

Instructions for creating a demonstration video/GIF of ExamTimetablePlanner.

---

## Overview

A 60-90 second demo showcasing the core workflow:
1. Data import
2. Schedule generation
3. Manual adjustments
4. Export

---

## Recording Setup

### Recommended Tools

| Platform | Tool | Notes |
|----------|------|-------|
| macOS | QuickTime / OBS | Built-in or free |
| Windows | OBS Studio | Free, open-source |
| Linux | OBS Studio | Free, open-source |

### Settings

- **Resolution:** 1920×1080 (Full HD)
- **Frame Rate:** 30 fps
- **Format:** MP4 or MOV
- **Window:** Application only (not full screen)

---

## Demo Script

### Timeline

| Time | Action | Key Points |
|------|--------|------------|
| 0:00-0:05 | App launches | Show splash screen |
| 0:05-0:10 | Dashboard visible | Highlight stats area |
| 0:10-0:25 | Navigate to Data Import | Import 4 CSV files from `sample_data/small/` |
| 0:25-0:35 | Show imported data | Scroll through courses, classrooms |
| 0:35-0:45 | Click "Generate Schedule" | Show loading animation |
| 0:45-0:55 | Timetable appears | Pan through the schedule |
| 0:55-1:05 | Manual adjustment | Drag an exam to new slot (show validation) |
| 1:05-1:15 | Export | Click export, show PDF or CSV |
| 1:15-1:20 | End | Return to dashboard |

### Narration Points (Optional)

1. "Import your data via CSV files"
2. "Generate a conflict-free schedule in seconds"
3. "Make manual adjustments with real-time validation"
4. "Export to PDF for distribution"

---

## Recording Checklist

Before recording:

- [ ] Clean database (`rm examplanner.db`)
- [ ] Hide personal files from desktop
- [ ] Close unnecessary applications
- [ ] Set display to 1920×1080 if possible
- [ ] Turn off notifications
- [ ] Prepare sample data in `sample_data/small/`

During recording:

- [ ] Move cursor slowly and deliberately
- [ ] Pause on key UI elements (1-2 seconds)
- [ ] Don't rush through actions
- [ ] Avoid erratic mouse movements

---

## Post-Processing

### Convert to GIF

Using ffmpeg:

```bash
# High quality GIF (larger file)
ffmpeg -i demo.mp4 -vf "fps=15,scale=1280:-1" -loop 0 demo.gif

# Optimized GIF (smaller file)
ffmpeg -i demo.mp4 -vf "fps=10,scale=960:-1,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse" demo.gif
```

### Compress Video

```bash
ffmpeg -i demo.mp4 -vcodec h264 -acodec aac -crf 23 demo_compressed.mp4
```

---

## File Locations

Place final assets in:

- `docs/assets/demo.gif` – For embedding in docs
- `docs/assets/demo.mp4` – Higher quality version

---

## Embedding in Documentation

### In index.html

```html
<div class="screenshot-container">
    <img src="assets/demo.gif" alt="ExamPlanner Demo" loading="lazy">
    <p class="figure-caption">
        Application demo showing import → generate → export workflow
        <em>[Actual Build Recording]</em>
    </p>
</div>
```

### In README.md

```markdown
## Demo

![ExamPlanner Demo](docs/assets/demo.gif)
```

---

## TODO

- [ ] Record demo video following this guide
- [ ] Convert to GIF
- [ ] Upload to `docs/assets/`
- [ ] Embed in documentation
- [ ] Update this file with actual screenshot
