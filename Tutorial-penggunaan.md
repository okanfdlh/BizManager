# Tutorial Penggunaan BizManager

Dokumen ini ditujukan untuk user Windows yang akan memasang dan memakai BizManager sehari-hari.

BizManager adalah aplikasi desktop offline untuk:
- mengelola customer
- mengelola produk
- membuat invoice
- mencatat pembayaran
- memantau piutang
- melihat buku besar customer
- melihat ringkasan dashboard dan laporan

## 1. Kebutuhan Sistem

Sebelum instalasi, pastikan:
- menggunakan Windows 10 atau Windows 11
- file installer `BizManager-<versi>.exe` sudah tersedia
- user memiliki izin untuk menjalankan installer

Untuk user biasa:
- tidak perlu install SQLite
- tidak perlu install JDK
- tidak perlu koneksi internet untuk memakai aplikasi setelah terpasang

## 2. Cara Install EXE di Windows

1. Buka folder tempat file installer disimpan.
2. Jalankan file `BizManager-<versi>.exe`.
3. Jika muncul peringatan Windows SmartScreen:
   - klik `More info`
   - klik `Run anyway`
4. Ikuti langkah instalasi sampai selesai.
5. Setelah selesai, jalankan aplikasi dari:
   - Start Menu, atau
   - shortcut desktop, atau
   - folder instalasi, biasanya `C:\Program Files\BizManager\`

## 3. Saat Pertama Kali Aplikasi Dibuka

Saat pertama kali dibuka, aplikasi akan otomatis membuat database SQLite lokal.

Lokasi database di Windows:

```text
C:\Users\<NamaUser>\AppData\Local\BizManager\app.db
```

Catatan:
- seluruh data aplikasi disimpan di file database tersebut
- jika aplikasi di-uninstall, file database biasanya tetap ada kecuali dihapus manual
- jika pindah device, file backup database dapat dipakai untuk restore

## 4. Gambaran Menu Utama

Menu yang tersedia di sidebar:

### Dashboard

Menampilkan ringkasan bisnis, seperti:
- omzet periode berjalan
- laba kotor dan laba bersih
- pembayaran masuk
- piutang aktif
- penjualan hutang vs non-hutang
- tren performa dan customer dengan piutang terbesar

### Customers

Dipakai untuk:
- menambah customer baru
- mengedit data customer
- melihat histori invoice customer

Field utama customer:
- kode
- nama
- perusahaan
- telepon
- email
- alamat
- catatan
- status aktif

### Products

Dipakai untuk:
- menambah produk
- mengedit produk
- mengaktifkan/nonaktifkan produk

Field utama produk:
- kode produk
- nama produk
- kategori
- satuan
- harga modal
- harga jual
- status aktif

### Invoices

Dipakai untuk membuat invoice customer.

Fitur utama:
- pilih customer
- tambah item produk
- isi qty
- isi diskon per item
- isi biaya tambahan internal
- isi catatan
- simpan draft
- post invoice

Catatan:
- invoice minimal memiliki 1 item
- hanya produk aktif yang sebaiknya dipakai
- invoice yang sudah `posted` dipakai untuk proses pembayaran
- jatuh tempo default mengikuti sistem yang sudah disiapkan aplikasi

### Payments

Dipakai untuk mencatat pembayaran dari customer.

Fitur utama:
- memilih invoice yang masih punya sisa tagihan
- mengisi nominal pembayaran
- mengisi metode pembayaran
- mengisi nomor referensi
- menyimpan pembayaran

Catatan:
- hanya invoice yang belum lunas yang tampil untuk dibayar
- nominal akan otomatis mudah diisi karena sisa tagihan ditampilkan

### Piutang

Dipakai untuk memantau invoice yang masih outstanding.

Biasanya dipakai untuk:
- melihat sisa tagihan customer
- memantau invoice yang belum lunas
- memantau invoice yang mendekati atau melewati jatuh tempo

### Buku Besar Customer

Dipakai untuk melihat histori lengkap per customer.

Alur menu ini:
- cari customer
- pilih customer dari dropdown
- lihat semua invoice customer
- lihat item produk per invoice
- lihat riwayat pembayaran per invoice
- lihat status invoice
- lihat total hutang per invoice
- lihat total hutang customer secara keseluruhan

Menu ini cocok dipakai saat:
- ada customer menanyakan rincian tagihan
- perlu audit pembayaran customer
- perlu mengecek invoice mana yang masih ongoing atau sudah lunas

### Reports

Dipakai untuk melihat laporan periode tertentu.

Cara pakai:
1. isi tanggal awal dengan format `YYYY-MM-DD`
2. isi tanggal akhir dengan format `YYYY-MM-DD`
3. klik `Cari`

Ringkasan laporan yang ditampilkan:
- total omzet
- total laba kotor
- total laba bersih
- total pembayaran masuk
- total piutang baru pada periode tersebut

### Settings

Dipakai untuk mengatur data dasar aplikasi.

Data yang bisa diisi:
- nama perusahaan
- alamat perusahaan
- telepon
- email
- prefix invoice
- prefix pembayaran
- mata uang
- folder backup default

Disarankan menu ini diisi lebih dulu sebelum mulai transaksi.

### Backup & Restore

Dipakai untuk mengamankan data aplikasi.

Fitur:
- backup database ke folder tujuan
- restore database dari file `.db`

Catatan:
- restore akan menimpa data aktif
- sistem menyiapkan auto-backup sebelum file database diganti

### Tentang Aplikasi

Berisi informasi singkat aplikasi dan author:
- Indirokan Fadhilah
- indirokanfadhilah@gmail.com
- indirokanfadhilah.vercel.app

## 5. Alur Penggunaan yang Disarankan

Urutan pemakaian yang paling aman:

### Langkah 1: Isi Pengaturan Aplikasi

Buka `Settings`, lalu isi:
- nama perusahaan
- prefix invoice
- prefix pembayaran
- mata uang
- folder backup

Setelah itu klik `Simpan Pengaturan`.

### Langkah 2: Tambahkan Customer

Buka `Customers`, lalu tambahkan data customer.

Minimal isi:
- kode
- nama

Simpan setelah selesai.

### Langkah 3: Tambahkan Produk

Buka `Products`, lalu tambahkan produk yang akan dijual.

Minimal isi:
- kode produk
- nama produk
- harga modal
- harga jual

Pastikan produk dalam status aktif agar muncul saat membuat invoice.

### Langkah 4: Buat Invoice

Buka `Invoices`, lalu:
1. pilih customer
2. tambah item invoice
3. pilih produk
4. isi qty
5. isi diskon jika ada
6. isi biaya tambahan jika ada
7. isi catatan jika perlu
8. pilih:
   - `Simpan Draft` jika belum final
   - `Post Invoice` jika sudah final

Gunakan `Post Invoice` jika invoice sudah siap ditagihkan.

### Langkah 5: Catat Pembayaran

Buka `Payments`, lalu:
1. pilih invoice yang akan dibayar
2. isi nominal pembayaran
3. isi metode pembayaran
4. isi nomor referensi bila ada
5. klik `Simpan Pembayaran`

Setelah pembayaran tersimpan, status tagihan akan ikut berubah.

### Langkah 6: Pantau Piutang

Buka `Piutang` untuk:
- melihat invoice yang belum lunas
- melihat sisa tagihan
- memprioritaskan penagihan

### Langkah 7: Cek Buku Besar Customer

Buka `Buku Besar Customer`, lalu:
1. cari customer
2. pilih customer
3. cek daftar invoice
4. cek produk pada tiap invoice
5. cek histori pembayaran
6. cek sisa hutang per invoice

Menu ini paling cocok untuk pengecekan detail customer.

### Langkah 8: Cek Dashboard dan Reports

Gunakan:
- `Dashboard` untuk ringkasan cepat
- `Reports` untuk ringkasan periode tertentu

## 6. Backup Data

Disarankan melakukan backup berkala, misalnya:
- setiap akhir hari
- setiap akhir minggu
- sebelum reinstall aplikasi
- sebelum restore database

Cara backup:
1. buka `Backup & Restore`
2. isi folder tujuan backup
3. klik `Proses Backup Sekarang`
4. simpan file backup dengan aman

## 7. Restore Data

Gunakan restore hanya jika perlu mengembalikan data lama.

Cara restore:
1. buka `Backup & Restore`
2. isi path lengkap file backup `.db`
3. klik `Jalankan Restore Data`
4. restart aplikasi bila diperlukan

Contoh path file backup:

```text
C:\Backup\BizManager\backup-2026-03-11.db
```

## 8. Troubleshooting Dasar

### Aplikasi tidak bisa dibuka

Periksa:
- instalasi sudah selesai sempurna
- jalankan ulang dari Start Menu
- jika perlu restart Windows

Jika build aplikasi berasal dari tim internal, minta file installer terbaru.

### Data tidak muncul

Periksa:
- apakah Anda sedang memakai database yang benar
- apakah sebelumnya pernah melakukan restore dari file lama

Lokasi database aktif:

```text
C:\Users\<NamaUser>\AppData\Local\BizManager\app.db
```

### Ingin pindah data ke komputer lain

Langkah aman:
1. lakukan backup dari komputer lama
2. salin file backup
3. install BizManager di komputer baru
4. restore file backup di komputer baru

### Apakah perlu install SQLite?

Tidak perlu. SQLite sudah dipakai internal oleh aplikasi.

### Apakah perlu install JDK?

Tidak perlu untuk user biasa yang hanya memakai aplikasi hasil installer.

## 9. Ringkasan Pemakaian Cepat

Urutan paling singkat:

1. install `BizManager.exe`
2. buka aplikasi
3. isi `Settings`
4. tambah `Customers`
5. tambah `Products`
6. buat `Invoices`
7. catat `Payments`
8. pantau `Piutang`
9. cek `Buku Besar Customer`
10. lakukan `Backup` rutin

## 10. Kontak

Informasi author aplikasi:
- Nama: Indirokan Fadhilah
- Contact: indirokanfadhilah@gmail.com
- Website: indirokanfadhilah.vercel.app
