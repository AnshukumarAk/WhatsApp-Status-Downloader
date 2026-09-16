# Status Saver — WhatsApp Status Downloader

View and **download WhatsApp & WhatsApp Business statuses** (photos and videos) straight to your gallery in one tap. Clean Material design, saves reliably via the MediaStore API on modern Android, no ads, no tracking.

---

## ✨ Features

- 🖼️ **View all statuses** your contacts have posted — images and videos in a clean 2-column grid
- ▶️ **Video badge** so you can tell photos and videos apart at a glance
- ⬇️ **One-tap download** — saved to your **Gallery** (`Pictures/StatusSaver` for images, `Movies/StatusSaver` for videos)
- ✅ **Works on modern Android** — uses the **MediaStore API** (Android 10+ scoped storage) so downloads actually land in the gallery, with clear "Saved ✓ / failed" feedback
- 📂 Reads both **WhatsApp** and **WhatsApp Business** status folders
- ⚡ **Smooth** — status scanning runs off the UI thread; images load with Glide
- 🎨 WhatsApp-green Material UI

---

## 📸 Screenshots

> _Add screenshots here_ (`screenshots/home.png`)

| Status grid | Downloaded |
|-------------|------------|
| _images + videos_ | _saved to gallery_ |

---

## 🛠️ Tech Stack

- **Language:** Java
- **UI:** RecyclerView (Grid), Material Components, CardView
- **Image loading:** [Glide](https://github.com/bumptech/glide)
- **Saving:** `MediaStore` (Android 10+) with legacy `File` + `MediaScanner` fallback
- **Min SDK:** 24 (Android 7.0) · **Target SDK:** 35 (Android 15)

---

## 🔐 Permissions

| Permission | Why it's needed |
|-----------|-----------------|
| `MANAGE_EXTERNAL_STORAGE` (Android 11+) | To read WhatsApp's `.Statuses` folder, which lives under `Android/media/com.whatsapp/…` |
| `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` (Android 13+) | Read status media |
| `READ_EXTERNAL_STORAGE` (Android ≤ 12) | Read status media on older devices |

Downloads are written with **MediaStore**, so no write permission is required on Android 10+. Everything runs on-device — no internet permission, no data collection.

---

## ⚙️ How It Works

1. WhatsApp stores statuses you've viewed in a hidden `.Statuses` folder.
2. The app scans that folder (and the WhatsApp Business one) for `.jpg` / `.png` / `.mp4` files.
3. Tap **download** → the file is copied into the public gallery via `MediaStore` and shows up in the **StatusSaver** album.

> ℹ️ You can only see/download statuses you have already **viewed inside WhatsApp** (that's when WhatsApp caches them on the device).

---

## 🚀 Build & Run

```bash
git clone https://github.com/<your-username>/WhatsAppStatusDownloader.git
```

1. Open in **Android Studio**, let Gradle sync.
2. Run ▶ on a device.
3. Grant the **All files access** permission when prompted (needed to read the WhatsApp status folder).

---

## 📄 Notes & Disclaimer

- This is an independent utility and is **not affiliated with WhatsApp or Meta**.
- Please respect content owners — only download and re-share statuses with permission.
- `MANAGE_EXTERNAL_STORAGE` is restricted on Google Play; a status-saver is an accepted use case but must be declared if you publish there.

---

## 📝 License

Released under the **MIT License** — see [LICENSE](LICENSE).
