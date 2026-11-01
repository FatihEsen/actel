const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

console.log('=======================================================');
console.log('🖥️  ACTel Windows Desktop Launcher Compiler');
console.log('=======================================================');

const cscExe = 'C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe';

if (!fs.existsSync(cscExe)) {
  console.error('❌ csc.exe bulunamadı:', cscExe);
  process.exit(1);
}

const sourceFile = path.join(__dirname, '..', 'src', 'launcher', 'ACTelLauncher.cs');
const distDir = path.join(__dirname, '..', 'dist');
fs.mkdirSync(distDir, { recursive: true });

const targetExe = path.join(distDir, 'ACTel.exe');

const cmd = `"${cscExe}" /target:winexe /out:"${targetExe}" /reference:System.dll /reference:System.Windows.Forms.dll /reference:System.Drawing.dll "${sourceFile}"`;

console.log('🔨 C# kaynak kodu derleniyor...');
try {
  execSync(cmd, { stdio: 'inherit' });
  const stats = fs.statSync(targetExe);
  const sizeKb = (stats.size / 1024).toFixed(1);
  console.log('=======================================================');
  console.log(`✅ ACTel.exe Başarıyla Derlendi!`);
  console.log(`📁 Konum: ${targetExe}`);
  console.log(`📊 Boyut: ${sizeKb} KB (Ultra Hafif)`);
  console.log('=======================================================');
} catch (err) {
  console.error('❌ Launcher derleme hatası:', err.message);
  process.exit(1);
}

