# Primitve LA App (Android)

An offline-first Android application for fast and reliable event attendance tracking using 1D barcode scanning. Designed for event organizers and managers who need a simple, secure, and internet-independent solution.

---

## Overview

This app allows organizers to record attendance by scanning ID card barcodes during events. Each event is treated as a read-only note, ensuring data integrity and preventing accidental edits. All scanned IDs are stored locally and can be exported as CSV and TXT files, making sharing and reporting effortless.

The application works fully offline, making it ideal for on-site usage in locations with limited or no internet connectivity.

---

## Target Users

- Event organizers  
- College and university administrators  
- Conference managers  
- Workshop and seminar coordinators  
- Organizations requiring offline attendance tracking  

---

## Key Features

- Fully offline operation  
- Notes-style event management  
- 1D barcode scanning  
- Multi-session attendance tracking (morning, afternoon, evening)
- Session-based data organization
- Student list management with CSV import/export
- Manual student addition and editing
- Visual attendance status indicators (present/absent)
- Individual attendance timestamp recording
- Attendance ratio tracking and export
- Scan confirm / retry workflow  
- Read-only event records  
- Consolidated and session-wise file exports
- Easy file sharing via Android share options  
- Fast and lightweight design  

---

## App Workflow

### 1. App Launch
- Displays a list of events in a notes-style interface  
- Each note represents a single event  
- Events are read-only and store creation date as metadata  

### 2. Create Event
- Tap the “+” button to create a new event  
- App automatically enters barcode scan mode for the selected session

### 3. Session Management
- Switch between sessions within the same event
- Each session maintains separate attendance records
- Visual indicators show current active session
- Session timestamps are automatically recorded

### 4. Student List Management
- Import student list via CSV file upload
- View complete student roster with attendance status
- Manual addition of new students to the list
- Edit existing student details (name, ID, etc.)
- Visual status indicators:
  - Present: Green background with white text
  - Absent: Red background with white text

### 5. Scan Barcode
- Uses an offline 1D barcode scanner  
- Supports structured IDs (e.g. `23BT001`)  

### 6. Scan Confirmation
- After each scan, the user can:
  - Retry – re-scan the barcode  
  - Next – confirm and save the ID  
- Scanner immediately prepares for the next scan  

### 7. Data Storage
- For each event:
  - One consolidated CSV file with all sessions and attendance ratios
  - Session-specific CSV files (morning.csv, afternoon.csv, evening.csv)
  - One consolidated TXT file with all sessions
  - Session-specific TXT files
- Each entry includes session identifier, individual attendance timestamp, and attendance ratio (e.g., 2/3 for 2 present out of 3 sessions)
- Student details with present/absent status and exact scan time for each session
- Session metadata stored separately  

### 8. View and Share
- View scanned IDs in read-only mode  
- Export or share CSV and TXT files via Android-supported apps  

---

## File Format Examples

### CSV
### TXT
