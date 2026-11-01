const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

console.log('=======================================================');
console.log('📦 ACTel Portable Distribution ZIP Packager');
console.log('=======================================================');

const rootDir = path.join(__dirname, '..');
const distDir = path.join(rootDir, 'dist');
const bundleDir = path.join(distDir, 'ACTel-v1.0.0-windows-x64');
const zipFile = path.join(distDir, 'ACTel-v1.0.0-windows-x64.zip');

// 1. Ensure launcher is built
console.log('1️⃣ Launcher derleniyor...');
execSync('node scripts/build-launcher.js', { cwd: rootDir, stdio: 'inherit' });

// 2. Verify APK exists
const apkPath = path.join(rootDir, 'public', 'download', 'actel.apk');
if (!fs.existsSync(apkPath)) {
  console.log('⚠️ public/download/actel.apk bulunamadı, build:apk-bundle çalıştırılıyor...');
  execSync('node scripts/build-apk.js', { cwd: rootDir, stdio: 'inherit' });
}

// 3. Prepare Staging Folder
console.log('2️⃣ Dağıtım klasörü hazırlanıyor...');
if (fs.existsSync(bundleDir)) {
  fs.rmSync(bundleDir, { recursive: true, force: true });
}
fs.mkdirSync(bundleDir, { recursive: true });

// Helper to copy directory recursively
function copyDirSync(src, dest, ignore = []) {
  fs.mkdirSync(dest, { recursive: true });
  const entries = fs.readdirSync(src, { withFileTypes: true });

  for (const entry of entries) {
    if (ignore.includes(entry.name)) continue;
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);

    if (entry.isDirectory()) {
      copyDirSync(srcPath, destPath, ignore);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

// Copy Files
console.log('   - ACTel.exe kopyalanıyor...');
fs.copyFileSync(path.join(distDir, 'ACTel.exe'), path.join(bundleDir, 'ACTel.exe'));

console.log('   - allow_firewall.bat kopyalanıyor...');
fs.copyFileSync(path.join(rootDir, 'allow_firewall.bat'), path.join(bundleDir, 'allow_firewall.bat'));

console.log('   - server.js ve package.json kopyalanıyor...');
fs.copyFileSync(path.join(rootDir, 'server.js'), path.join(bundleDir, 'server.js'));
fs.copyFileSync(path.join(rootDir, 'package.json'), path.join(bundleDir, 'package.json'));

console.log('   - lib/ kütüphaneleri kopyalanıyor...');
copyDirSync(path.join(rootDir, 'lib'), path.join(bundleDir, 'lib'));

console.log('   - public/ web kokpiti (APK dahil) kopyalanıyor...');
copyDirSync(path.join(rootDir, 'public'), path.join(bundleDir, 'public'));

console.log('   - ac_plugin/ kopyalanıyor...');
copyDirSync(path.join(rootDir, 'ac_plugin'), path.join(bundleDir, 'ac_plugin'));

console.log('   - node_modules/ (çalışma zamanı bağımlılıkları) kopyalanıyor...');
copyDirSync(path.join(rootDir, 'node_modules'), path.join(bundleDir, 'node_modules'), ['.bin', '.package-lock.json']);

// 4. Create ZIP Archive
console.log('3️⃣ ZIP arşivi sıkıştırılıyor...');
if (fs.existsSync(zipFile)) {
  fs.unlinkSync(zipFile);
}

try {
  // Use PowerShell Compress-Archive for native, rock-solid Windows zip compression
  const psCmd = `powershell -NoProfile -Command "Compress-Archive -Path '${bundleDir}\\*' -DestinationPath '${zipFile}' -CompressionLevel Optimal -Force"`;
  execSync(psCmd, { stdio: 'inherit' });

  const stats = fs.statSync(zipFile);
  const sizeMb = (stats.size / (1024 * 1024)).toFixed(2);

  console.log('=======================================================');
  console.log('🎉 TEBRİKLER! TAŞINABİLİR PAKET BAŞARIYLA OLUŞTURULDU!');
  console.log(`📁 Konum: ${zipFile}`);
  console.log(`📊 Boyut: ${sizeMb} MB`);
  console.log('=======================================================');
} catch (err) {
  console.error('❌ ZIP sıkıştırma hatası:', err.message);
  process.exit(1);
}

