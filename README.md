# Cahaya Kurnia - Spring Boot Application

## 🔐 Setup Environment Variables (WAJIB)

Aplikasi ini menggunakan environment variables untuk keamanan. **Tidak ada lagi data sensitif di file properties!**

### 1. Buat file `.env` di root project

Copy dari `env.example` dan isi dengan nilai yang sesuai:

```bash
# Database Configuration (PostgreSQL JDBC)
DB_URL=jdbc:postgresql://your-database-host:5432/your-database-name
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

# Supabase Configuration (API)
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_BUCKET_NAME=your_bucket_name
SUPABASE_API_KEY=your_supabase_api_key
```

### 2. Cara Set Environment Variables

#### Windows (PowerShell):
```powershell
$env:DB_URL="jdbc:postgresql://your-database-host:5432/your-database-name"
$env:DB_USERNAME="your_database_username"
$env:DB_PASSWORD="your_database_password"
$env:SUPABASE_URL="https://your-project-id.supabase.co"
$env:SUPABASE_BUCKET_NAME="your_bucket_name"
$env:SUPABASE_API_KEY="your_supabase_api_key"
```

#### Windows (Command Prompt):
```cmd
set DB_URL=jdbc:postgresql://your-database-host:5432/your-database-name
set DB_USERNAME=your_database_username
set DB_PASSWORD=your_database_password
set SUPABASE_URL=https://your-project-id.supabase.co
set SUPABASE_BUCKET_NAME=your_bucket_name
set SUPABASE_API_KEY=your_supabase_api_key
```

#### Linux/Mac:
```bash
export DB_URL="jdbc:postgresql://your-database-host:5432/your-database-name"
export DB_USERNAME="your_database_username"
export DB_PASSWORD="your_database_password"
export SUPABASE_URL="https://your-project-id.supabase.co"
export SUPABASE_BUCKET_NAME="your_bucket_name"
export SUPABASE_API_KEY="your_supabase_api_key"
```

### 3. Run Application

```bash
./gradlew bootRun
```

## 🛡️ Keamanan

✅ **File sensitif sudah diamankan:**
- `application-dev.properties` - Hanya berisi environment variables, tidak ada data sensitif
- `application.properties` - Konfigurasi umum tanpa data sensitif
- `.env` dan `env.example` - Ditambahkan ke `.gitignore`
- Tidak ada lagi API key atau password yang terekspos di file properties

✅ **Yang perlu dilakukan:**
- Set environment variables sebelum run aplikasi
- Jangan commit file `.env` (sudah di-ignore)
- Gunakan environment variables untuk production deployment

## 🚨 Troubleshooting

Jika aplikasi error "Could not resolve placeholder":
1. **Pastikan environment variables sudah diset** dengan benar
2. **Cek koneksi internet**
3. **Pastikan API key Supabase masih valid**

## 📁 Struktur File Konfigurasi

```
src/main/resources/
├── application.properties          # Konfigurasi umum (aman)
└── application-dev.properties     # Hanya env vars (aman)

root/
├── .env                           # File sensitif (tidak masuk git)
├── env.example                    # Template (aman)
└── .gitignore                     # Mengabaikan file sensitif
```
