Cahaya Kurnia - Aplikasi E-commerce Toko Teknik
Proyek ini adalah aplikasi e-commerce sederhana yang dibangun menggunakan Spring Boot. Tujuan utama dari proyek ini adalah sebagai proyek waktu luang dan media pembelajaran pribadi untuk memperdalam pemahaman saya tentang ekosistem Spring Boot.

Aplikasi ini dibuat sebagai situs web untuk Toko Alat Teknik Cahaya Kurnia, namun perlu dicatat bahwa proyek ini masih dalam tahap pengembangan (on development). Sebagian besar fitur dasar seperti katalog produk dan manajemen data sudah ada, tetapi masih banyak fitur lain yang akan ditambahkan dan disempurnakan.

Semua data sensitif, seperti kredensial database dan API key, disimpan di environment variables. Hal ini memastikan tidak ada informasi rahasia yang secara tidak sengaja ter-commit ke repositori.

application.properties dan application-dev.properties hanya berisi konfigurasi umum dan tidak mengandung data sensitif.

File .env sudah dimasukkan ke dalam .gitignore sehingga tidak akan di-commit.

Disarankan untuk selalu menggunakan environment variables, terutama saat melakukan deployment di server production.
