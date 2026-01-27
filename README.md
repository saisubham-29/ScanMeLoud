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
- Scan confirm / retry workflow  
- Read-only event records  
- One CSV and one TXT file per event  
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
- App automatically enters barcode scan mode  

### 3. Scan Barcode
- Uses an offline 1D barcode scanner  
- Supports structured IDs (e.g. `23BT001`)  

### 4. Scan Confirmation
- After each scan, the user can:
  - Retry – re-scan the barcode  
  - Next – confirm and save the ID  
- Scanner immediately prepares for the next scan  

### 5. Data Storage
- For each event:
  - One CSV file  
  - One TXT file  
- Only scanned ID values are stored  
- No per-entry timestamps (event date stored as metadata)  

### 6. View and Share
- View scanned IDs in read-only mode  
- Export or share CSV and TXT files via Android-supported apps  

---

## File Format Examples

### CSV
### TXT
