const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

console.log('=======================================================');
console.log('🤖 ACTel Android APK Build & Bundle Automation');
console.log('=======================================================');

const androidDir = path.join(__dirname, '..', 'android');
const gradlewBat = path.join(androidDir, 'gradlew.bat');

if (!fs.existsSync(gradlewBat)) {
  console.error('❌ gradlew.bat bulunamadı:', gradlewBat);
  process.exit(1);
}

console.log('📦 Gradle assembleDebug derleniyor...');
try {
  execSync('gradlew.bat assembleDebug', {
    cwd: androidDir,
    stdio: 'inherit',
    shell: 'cmd.exe'
  });
} catch (err) {
  console.error('❌ Gradle derleme hatası:', err.message);
  process.exit(1);
}

const sourceApk = path.join(androidDir, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk');
if (!fs.existsSync(sourceApk)) {
  console.error('❌ Derlenen APK bulunamadı:', sourceApk);
  process.exit(1);
}

const publicDownloadDir = path.join(__dirname, '..', 'public', 'download');
fs.mkdirSync(publicDownloadDir, { recursive: true });

const targetApk = path.join(publicDownloadDir, 'actel.apk');
fs.copyFileSync(sourceApk, targetApk);

const stats = fs.statSync(targetApk);
const sizeMb = (stats.size / (1024 * 1024)).toFixed(2);

console.log('=======================================================');
console.log(`✅ APK Başarıyla Hazırlandı ve Kopyalandı!`);
console.log(`📁 Konum: ${targetApk}`);
console.log(`📊 Boyut: ${sizeMb} MB`);
console.log(`🌐 Web URL: http://localhost:8080/download/actel.apk`);
console.log('=======================================================');

