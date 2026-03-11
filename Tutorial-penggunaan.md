# Panduan Penggunaan BizManager

Panduan ini dibuat untuk pengguna umum. Bahasa sengaja dibuat sederhana agar mudah diikuti.

BizManager dipakai untuk membantu usaha dalam:
- menyimpan data pelanggan
- menyimpan data barang
- membuat tagihan penjualan
- mencatat pembayaran
- melihat sisa tagihan pelanggan
- melihat ringkasan usaha

## 1. Sebelum Mulai

Pastikan:
- menggunakan Windows 10 atau Windows 11
- file pemasangan aplikasi sudah tersedia, misalnya `BizManager-1.0.0.exe`

Untuk memakai aplikasi ini:
- tidak perlu memasang aplikasi tambahan
- tidak perlu internet saat digunakan sehari-hari

## 2. Cara Memasang Aplikasi di Windows

1. Buka folder tempat file pemasangan disimpan.
2. Klik dua kali file `BizManager-<versi>.exe`.
3. Jika muncul peringatan dari Windows:
   - klik `More info`
   - lalu klik `Run anyway`
4. Ikuti langkah pemasangan sampai selesai.
5. Setelah selesai, buka aplikasi dari:
   - Start Menu
   - shortcut di desktop
   - atau folder `C:\Program Files\BizManager\`

## 3. Saat Aplikasi Dibuka Pertama Kali

Saat pertama kali dibuka, aplikasi akan langsung menyiapkan tempat penyimpanan data.

Semua data aplikasi disimpan di komputer Windows Anda. Biasanya lokasinya ada di:

```text
C:\Users\<NamaUser>\AppData\Local\BizManager\app.db
```

Anda tidak perlu membuka file tersebut secara manual. Cukup gunakan aplikasi seperti biasa.

Catatan:
- semua data tersimpan di komputer yang dipakai
- jika ganti komputer, data bisa dipindahkan lewat menu cadangan data
- jika aplikasi dipasang ulang, data lama biasanya masih tetap ada

## 4. Pengenalan Menu

Di sebelah kiri aplikasi ada beberapa menu utama.

### Dashboard

Menu ini menampilkan ringkasan usaha, seperti:
- total penjualan
- keuntungan
- pembayaran yang sudah masuk
- sisa tagihan pelanggan

Gunakan menu ini untuk melihat gambaran usaha secara cepat.

### Customers

Menu ini dipakai untuk menyimpan data pelanggan.

Yang bisa dilakukan:
- menambah pelanggan baru
- mengubah data pelanggan
- melihat riwayat tagihan pelanggan

Data yang biasanya diisi:
- kode pelanggan
- nama pelanggan
- nama perusahaan
- nomor telepon
- email
- alamat
- catatan

### Products

Menu ini dipakai untuk menyimpan data barang.

Yang bisa dilakukan:
- menambah barang
- mengubah data barang
- mengaktifkan atau menonaktifkan barang

Data yang biasanya diisi:
- kode barang
- nama barang
- kategori
- satuan
- harga modal
- harga jual

### Invoices

Menu ini dipakai untuk membuat tagihan penjualan.

Yang bisa dilakukan:
- memilih pelanggan
- menambahkan barang ke tagihan
- mengisi jumlah barang
- memberi potongan harga jika ada
- menambah catatan
- menyimpan tagihan

Di menu ini ada dua pilihan:
- `Simpan Draft`: dipakai jika tagihan belum final
- `Post Invoice`: dipakai jika tagihan sudah final dan siap ditagihkan

### Payments

Menu ini dipakai untuk mencatat pembayaran dari pelanggan.

Yang biasanya diisi:
- tagihan yang dibayar
- jumlah pembayaran
- cara pembayaran
- nomor referensi jika ada
- catatan

Setelah pembayaran disimpan, sisa tagihan pelanggan akan berkurang otomatis.

### Piutang

Menu ini dipakai untuk melihat tagihan yang belum lunas.

Gunanya:
- melihat pelanggan yang masih punya tagihan
- melihat sisa tagihan
- membantu penagihan

### Buku Besar Customer

Menu ini dipakai untuk melihat riwayat lengkap per pelanggan.

Di sini Anda bisa melihat:
- semua tagihan pelanggan
- barang apa saja yang ada di tiap tagihan
- riwayat pembayaran
- tagihan yang masih berjalan
- tagihan yang sudah lunas

Menu ini sangat berguna saat pelanggan menanyakan rincian tagihan mereka.

### Reports

Menu ini dipakai untuk melihat ringkasan dalam rentang tanggal tertentu.

Cara pakai:
1. isi tanggal awal
2. isi tanggal akhir
3. klik `Cari`

Menu ini membantu melihat:
- total penjualan
- total keuntungan
- total pembayaran masuk
- total tagihan yang muncul pada periode tersebut

### Settings

Menu ini dipakai untuk mengisi pengaturan dasar aplikasi.

Yang sebaiknya diisi:
- nama usaha
- alamat usaha
- nomor telepon
- email
- awalan nomor tagihan
- awalan nomor pembayaran
- mata uang
- folder penyimpanan cadangan data

Sebaiknya menu ini diisi lebih dulu sebelum aplikasi digunakan.

### Backup & Restore

Menu ini dipakai untuk menyimpan cadangan data dan mengembalikan data lama.

Mudahnya:
- `Backup` berarti membuat salinan data
- `Restore` berarti mengembalikan data dari salinan yang pernah dibuat

Gunakan menu ini dengan hati-hati, terutama saat mengembalikan data lama.

### Tentang Aplikasi

Menu ini berisi informasi singkat tentang aplikasi dan pembuatnya.

## 5. Urutan Penggunaan yang Disarankan

Supaya lebih mudah, gunakan aplikasi dengan urutan berikut.

### Langkah 1: Isi Pengaturan

Buka `Settings`, lalu isi data dasar usaha:
- nama usaha
- awalan nomor tagihan
- awalan nomor pembayaran
- mata uang
- folder cadangan data

Lalu klik `Simpan Pengaturan`.

### Langkah 2: Masukkan Data Pelanggan

Buka `Customers`, lalu tambahkan pelanggan.

Minimal isi:
- kode
- nama

Setelah selesai, klik `Simpan`.

### Langkah 3: Masukkan Data Barang

Buka `Products`, lalu tambahkan barang yang dijual.

Minimal isi:
- kode barang
- nama barang
- harga modal
- harga jual

Pastikan barang aktif agar bisa dipilih saat membuat tagihan.

### Langkah 4: Buat Tagihan

Buka `Invoices`, lalu:
1. pilih pelanggan
2. tambahkan barang
3. isi jumlah barang
4. isi potongan harga jika ada
5. isi catatan jika perlu
6. pilih `Simpan Draft` atau `Post Invoice`

Jika tagihan sudah benar dan siap dipakai, pilih `Post Invoice`.

### Langkah 5: Catat Pembayaran

Buka `Payments`, lalu:
1. pilih tagihan yang dibayar
2. isi jumlah pembayaran
3. pilih cara pembayaran
4. isi catatan jika perlu
5. klik `Simpan Pembayaran`

### Langkah 6: Pantau Sisa Tagihan

Buka `Piutang` untuk melihat:
- siapa yang masih punya tagihan
- berapa sisa tagihannya

### Langkah 7: Cek Rincian Pelanggan

Buka `Buku Besar Customer`, lalu:
1. cari nama pelanggan
2. pilih pelanggan
3. lihat semua tagihan
4. lihat riwayat pembayaran
5. lihat sisa tagihan yang masih ada

### Langkah 8: Lihat Ringkasan

Gunakan:
- `Dashboard` untuk melihat gambaran cepat
- `Reports` untuk melihat ringkasan berdasarkan tanggal

## 6. Cara Menyimpan Cadangan Data

Sebaiknya lakukan cadangan data secara rutin, misalnya:
- setiap akhir hari
- setiap akhir minggu
- sebelum ganti komputer
- sebelum mengembalikan data lama

Caranya:
1. buka `Backup & Restore`
2. isi folder tujuan penyimpanan
3. klik `Proses Backup Sekarang`
4. simpan file cadangan dengan baik

## 7. Cara Mengembalikan Data Lama

Jika ingin memakai data lama:

1. buka `Backup & Restore`
2. masukkan lokasi file cadangan
3. klik `Jalankan Restore Data`
4. tutup dan buka kembali aplikasi jika perlu

Contoh lokasi file cadangan:

```text
C:\Backup\BizManager\backup-2026-03-11.db
```

## 8. Jika Ada Kendala

### Aplikasi tidak mau terbuka

Coba langkah berikut:
- tutup aplikasi lalu buka lagi
- restart komputer
- pastikan pemasangan aplikasi sudah selesai

Jika masih tidak bisa, minta file pemasangan terbaru dari tim Anda.

### Data tidak muncul

Coba periksa:
- apakah sebelumnya Anda baru mengembalikan data lama
- apakah Anda sedang memakai komputer yang benar

### Ingin pindah ke komputer lain

Langkah yang aman:
1. buat cadangan data di komputer lama
2. salin file cadangan ke komputer baru
3. pasang BizManager di komputer baru
4. kembalikan data dari file cadangan tadi

## 9. Ringkasan Singkat

Urutan paling mudah:

1. pasang aplikasi
2. buka aplikasi
3. isi `Settings`
4. tambah `Customers`
5. tambah `Products`
6. buat `Invoices`
7. catat `Payments`
8. cek `Piutang`
9. cek `Buku Besar Customer`
10. lakukan `Backup` rutin

## 10. Kontak

Informasi pembuat aplikasi:
- Nama: Indirokan Fadhilah
- Email: indirokanfadhilah@gmail.com
- Website: indirokanfadhilah.vercel.app
