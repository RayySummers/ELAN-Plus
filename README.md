# ELAN-Plus

**Community-driven enhancements for ELAN: auto-save, performance optimizations, and modern development workflows.**

Making linguistic annotation more reliable and efficient.

---

## 🎯 Why ELAN-Plus?

This project started with a simple frustration: **losing hours of annotation work because ELAN doesn't auto-save.**

As a linguistics student, I rely on ELAN for phonetic annotation and multimodal corpus building. But the original ELAN hasn't changed much in years—no auto-save, occasional freezes, and a development workflow stuck in the past.

**ELAN-Plus is my attempt to fix that.**

---

## 🚀 What's Different?

| Feature | Original ELAN | ELAN-Plus |
|:---|:---|:---|
| Auto-save | ❌ Manual only | 🚧 **In Progress** |
| Performance | Occasional freezes | 🚧 **Optimizing** |
| Modern Dev Workflow | ❌ | ✅ Git, CI/CD, Tests |
| Community Driven | MPI Institute | **You & Me** |

---

## 🛠️ Tech Stack

- **Language:** Java 25
- **Build:** Maven 3.9+
- **UI:** Java Swing
- **Media:** VLCJ (VLC bindings)
- **License:** GPLv3 (same as original ELAN)

---

## 📦 Installation

### Prerequisites
- Java 25+
- Maven 3.9+

### Build from Source

```bash
# Clone the repo
git clone https://github.com/RayySummers/ELAN-Plus.git
cd ELAN-Plus

# Build
mvn clean package -DskipTests

# Run
java -cp target/classes:target/dependency/* mpi.eudico.client.annotator.ELAN
```

---

## 🗺️ Roadmap

### Phase 1: Foundation ✅
- [x] Fork and build ELAN 7.0 source
- [x] Set up Git repository
- [x] Push to GitHub

### Phase 2: Core Features 🚧
- [ ] Auto-save (every 5 minutes + on edit)
- [ ] Save preferences
- [ ] Performance profiling

### Phase 3: Enhancements 📋
- [ ] Incremental save
- [ ] Async save (non-blocking UI)
- [ ] Modern UI themes
- [ ] Plugin system

---

## 🤝 Contributing

This is a **vibe-coded** project—built with passion, not corporate resources.

**Ways to contribute:**
- 🐛 Report bugs
- 💡 Suggest features
- 🔧 Submit PRs
- 📖 Improve docs

**No contribution too small.** If auto-save has saved you from losing work once, it's worth it.

---

## 🙏 Acknowledgments

- **Original ELAN:** Max Planck Institute for Psycholinguistics
- **Source:** https://archive.mpi.nl/tla/elan
- **License:** GPLv3

This project stands on the shoulders of decades of linguistic research tools. We're just adding a safety net.

---

## 📄 License

GNU General Public License v3.0 - see [LICENSE](LICENSE)

ELAN is copyright © Max Planck Institute for Psycholinguistics.

---

*Built with 💛 by a linguistics student who got tired of losing work.*
