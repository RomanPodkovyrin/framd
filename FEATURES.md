# Features

## EXIF & Metadata

- [ ] Detect missing or stripped EXIF data
- [ ] Detect missing capture time, with suggestions based on folder name or nearby files
- [ ] Flag mismatch between file name date and EXIF capture date
- [ ] Flag files placed in a folder that does not match their capture date
- [ ] Check for presence of GPS data

## File Quality

- [ ] Flag outdated or legacy photo formats
- [ ] Flag overly large images
- [ ] Detect duplicate images based on file hash

## Library Structure

- [ ] Enforce file naming convention
- [ ] Enforce folder naming convention
- [ ] Detect empty folders
- [ ] Detect non-photo or non-media files in library folders
- [ ] Detect orphaned sidecar files

## Reporting

- [ ] Statistics on file formats across the library
- [ ] Storage breakdown by folder
- [ ] Overall library structure analysis

---

## Stretch Goals

- [ ] Detect blank or blown-out images based on dominant colour
- [ ] Face detection for filtering
- [ ] Index remote sources (OneDrive, Google Drive, etc.)
- [ ] Trend tracking with PDF export
- [ ] Config file in the library root, similar to ESLint or Prettier
- [ ] Webhook or notification support when a scan finds violations
- [ ] Generate a reviewable plan file for bulk operations (rename, move, etc.) that the user can inspect and apply manually. Applying the plan may be a separate Go CLI tool that reads and executes the plan file, keeping the scope of this app focused on analysis only.
- [ ] On-demand thumbnail generation that fills in gradually, rather than generating everything at scan time