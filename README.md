# ifs18005-p5-springboot-todolist

Aplikasi web Todo List berbasis **Spring Boot 4.1.2**, **Maven**, **Vaadin 25.3.4**, dan **JDBC (PostgreSQL)**, dengan arsitektur **Clean Architecture** (domain, usecase, adapter, framework).

| | |
| --- | --- |
| Folder | `ifs18005-p5-springboot-todolist` |
| Artifact Maven | `ifs18005-springboot-todolist` |
| Package | `org.delcom.ifs18005springboottodolist` |
| Database | `pbo2026_todolist` |

## Prasyarat

- JDK 25
- PostgreSQL — buat database terlebih dahulu:

```sql
CREATE DATABASE pbo2026_todolist;
```

- Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Konfigurasi `.env`

Salin `.env.example` menjadi `.env` (file `.env` diabaikan git):

```env
APP_PORT=8080

DB_CONNECTION=pgsql
DB_HOST=localhost
DB_PORT=5432
DB_DATABASE=pbo2026_todolist
DB_USERNAME=postgres
DB_PASSWORD=postgres

ROOT_USERNAME=admin
ROOT_PASSWORD=admin

UPLOAD_DIR=uploads
```

Akun admin di-seed otomatis saat start dari `ROOT_USERNAME` / `ROOT_PASSWORD`. Upload disimpan di `UPLOAD_DIR` dan dilayani di `/uploads/**`.

`DotEnv` hanya mengisi system property jika variabel lingkungan / property dengan nama yang sama **belum** ada. Di server/cloud, nilai `DB_*` dari environment platform yang dipakai.

**Env DB yang didukung (selaras platform deploy):**

| Variabel | Keterangan |
| --- | --- |
| `DB_CONNECTION` | `pgsql` (informasional; driver tetap PostgreSQL) |
| `DB_HOST` | host Postgres/PgBouncer |
| `DB_PORT` | port (contoh cloud: `6432`) |
| `DB_DATABASE` | nama database (utama; di cloud: `db_<website_id>` dengan `-` diganti `_`) |
| `DB_USERNAME` | user DB |
| `DB_PASSWORD` | password DB |

`application.yaml` memakai `${DB_DATABASE}` tanpa default — wajib diisi lewat `.env` / environment.

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

Dari direktori proyek `ifs18005-p5-springboot-todolist`:

```powershell
cd ifs18005-p5-springboot-todolist  # jika berada di root workspace praktikum5
.\mvnw.cmd spring-boot:run
```

Buka [http://localhost:8080](http://localhost:8080) (atau port `APP_PORT`).

Login admin: sesuai `ROOT_USERNAME` / `ROOT_PASSWORD` di `.env`.

### Build JAR (profil production Vaadin)

Jalankan dari **root proyek** agar `.env` terbaca (jika tidak di-override environment):

```powershell
.\mvnw.cmd clean package -Pproduction
java -jar target\ifs18005-springboot-todolist-0.0.1-SNAPSHOT.jar
```

Tanpa `-Pproduction`, menjalankan JAR biasanya gagal (`vaadin-dev-server not found`).

## Uji & coverage

```powershell
.\mvnw.cmd clean verify
```

- Jacoco mengecualikan `framework/view` dan `framework/auth`
- Laporan: `target/site/jacoco/index.html`

## Troubleshooting JAR di server/cloud

Jika log berisi `Gagal menginisialisasi schema database` / `Connection ... refused`, **JAR sudah berhasil di-build**; yang gagal adalah koneksi database.

1. Pastikan PostgreSQL (atau PgBouncer) hidup dan menerima TCP dari host aplikasi.
2. Cocokkan `DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD` (environment server mengalahkan `.env`).
3. Pastikan database sudah ada / dibuat di platform.
4. Contoh error `Connection to delcom-deployment-pgbouncer:6432 refused` berarti host/port DB tidak reachable — perbaiki layanan DB/PgBouncer atau jaringan, bukan rebuild Vaadin.
