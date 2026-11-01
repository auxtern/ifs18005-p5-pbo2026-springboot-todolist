# ifs18005-p5-springboot-todolist

<<<<<<< HEAD
Aplikasi web Todo List berbasis **Spring Boot 4.1.2**, **Maven**, **Vaadin 25.3.4**, dan **JDBC (PostgreSQL)**, dengan arsitektur **Clean Architecture** (domain, usecase, adapter, framework).

| | |
| --- | --- |
| Folder | `ifs18005-p5-springboot-todolist` |
| Artifact Maven | `ifs18005-springboot-todolist` |
| Package | `org.delcom.ifs18005springboottodolist` |
| Database | `pbo2026_todolist` |
=======
Aplikasi web Todo List berbasis **Spring Boot**, **Maven**, dan **Vaadin**, dengan arsitektur **Clean Architecture**.

Data disimpan **in-memory** (belum memakai database). Port dikonfigurasi lewat `.env` (`APP_PORT`).

> **Penamaan proyek mahasiswa:** `{username}-springboot-todolist`  
> **Package:** `org.delcom.{username}springboottodolist`  
> Contoh di repo ini memakai username `ifs18005`. Ganti sesuai username Anda.
>>>>>>> d696c30f62b39832e279c76f0bd9e553d4071cef

## Prasyarat

- JDK 25
<<<<<<< HEAD
- PostgreSQL — buat database terlebih dahulu:
=======
- Maven Wrapper (`mvnw` / `mvnw.cmd`)
>>>>>>> d696c30f62b39832e279c76f0bd9e553d4071cef

```sql
CREATE DATABASE pbo2026_todolist;
```

- Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Konfigurasi `.env`

Salin `.env.example` menjadi `.env` (file `.env` diabaikan git):

```env
APP_PORT=8080

DB_HOST=localhost
DB_PORT=5432
DB_NAME=pbo2026_todolist
DB_USERNAME=postgres
DB_PASSWORD=postgres

ROOT_USERNAME=admin
ROOT_PASSWORD=admin

UPLOAD_DIR=uploads
```

Akun admin di-seed otomatis saat start dari `ROOT_USERNAME` / `ROOT_PASSWORD`. Upload disimpan di `UPLOAD_DIR` dan dilayani di `/uploads/**`.

## Fitur

- Registrasi, login, logout (`AuthSession`: cookie + flag logout)
- Todo per pengguna: judul, deskripsi, cover image opsional, status selesai
- Klik cover → modal zoom (`ImagePreviewDialog`)
- Profil: nama, password, foto
- Role **ADMIN**: daftar pengguna + lihat todo user lain (read-only); admin punya todo sendiri
- Role **USER**: hanya todo sendiri
- UI: `MainLayout` (sidebar + menu avatar Profil/Keluar), kartu login/register, `styles.css` (`cursor: pointer`)

## Struktur singkat

```text
<<<<<<< HEAD
src/main/java/org/delcom/ifs18005springboottodolist/
├── domain/          # entity, exception, repository port, PasswordHasher
├── usecase/         # AuthUseCase, TodoUseCase, UserUseCase
├── adapter/         # Jdbc*/InMemory*, SchemaInitializer, FileStorage, presenter
├── framework/       # auth (AuthSession), view (Vaadin)
├── config/          # AppConfig (composition root)
├── DotEnv.java
└── App.java
```

Runtime memakai **JDBC + PostgreSQL**. Unit test repository memakai **InMemory**; tes JDBC memakai **H2**.

## Menjalankan

```powershell
.\mvnw.cmd spring-boot:run
```

Buka [http://localhost:8080](http://localhost:8080) (atau port `APP_PORT`).

Login admin: sesuai `ROOT_USERNAME` / `ROOT_PASSWORD` di `.env`.

### Build JAR (profil production Vaadin)

```powershell
.\mvnw.cmd clean package -Pproduction
java -jar target\ifs18005-springboot-todolist-0.0.1-SNAPSHOT.jar
=======
ifs18005-springboot-todolist/
├── .env / .env.example
├── pom.xml
├── src/main/java/org/delcom/ifs18005springboottodolist/
│   ├── App.java              # entry point (@SpringBootApplication)
│   ├── DotEnv.java           # muat .env ke system properties
│   ├── config/AppConfig.java # composition root Spring (@Bean)
│   ├── domain/               # entity, exception, repository port
│   ├── usecase/              # TodoUseCase
│   ├── adapter/              # InMemoryRepository, TodoRepository, TodoPresenter
│   └── framework/view/       # TodoView (Vaadin)
├── src/main/resources/
│   └── application.yaml
└── src/test/java/.../
    └── AppTest.java
>>>>>>> d696c30f62b39832e279c76f0bd9e553d4071cef
```

Tanpa `-Pproduction`, menjalankan JAR biasanya gagal (`vaadin-dev-server not found`).

<<<<<<< HEAD
## Uji & coverage

```powershell
.\mvnw.cmd clean verify
```

- Jacoco mengecualikan `framework/view` dan `framework/auth`
- Laporan: `target/site/jacoco/index.html`
=======
```env
APP_PORT=8080
```

`application.yaml` memakai:

```yaml
server:
  port: ${APP_PORT:8080}
```

Salin dari `.env.example` jika `.env` belum ada. File `.env` diabaikan git.

## Perintah Build & Menjalankan

### 1. Development (disarankan)

```powershell
.\mvnw.cmd spring-boot:run
```

Atau dari Cursor/VS Code: Run and Debug → **TodoList (DevTools)**.

Buka [http://localhost:8080](http://localhost:8080) (atau port di `APP_PORT`).

Spring Boot DevTools aktif: simpan file → compile → auto-restart + live-reload.

Kalau autobuild IDE tidak jalan:

```powershell
.\dev.ps1
```

### 2. Uji + coverage Jacoco 100%

```powershell
.\mvnw.cmd clean verify
```

Laporan: `target/site/jacoco/index.html`

### 3. Build & jalankan JAR (wajib `-Pproduction`)

Tanpa `-Pproduction`, `java -jar` gagal (`vaadin-dev-server not found`) karena `vaadin-dev` tidak ikut ke JAR.

```powershell
.\mvnw.cmd clean package -Pproduction
java -jar target\ifs18005-springboot-todolist-0.0.1-SNAPSHOT.jar
```

Jalankan dari root proyek agar file `.env` terbaca.

## Fitur UI

- Tambah, ubah, hapus todo
- Cari berdasarkan judul
- Urutkan lewat klik header kolom
- Ringkasan jumlah todo / selesai / belum selesai
- Tanggal dibuat, tanggal selesai, dan durasi
- Penyimpanan sementara di memori (reset saat aplikasi di-restart)
>>>>>>> d696c30f62b39832e279c76f0bd9e553d4071cef
