using System;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.IO;
using System.Windows.Forms;

namespace ACTel
{
    static class Program
    {
        private static Process nodeProcess;
        private static NotifyIcon trayIcon;
        private static string appDir;

        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            appDir = AppDomain.CurrentDomain.BaseDirectory;

            // 1. Clean up any stale processes on port 8080
            CleanStalePort8080();

            // 2. Start background Node.js server
            StartNodeServer();

            // 3. Create System Tray Icon with Custom Motorsport Emblem
            SetupSystemTray();

            // 4. Launch Modern Edge/Chrome App Mode Window
            LaunchDesktopApp("http://localhost:8080");

            // 5. Clean exit hooks
            Application.ApplicationExit += OnExit;
            AppDomain.CurrentDomain.ProcessExit += OnExit;

            Application.Run();
        }

        private static void CleanStalePort8080()
        {
            try
            {
                Process netstat = new Process();
                netstat.StartInfo.FileName = "netstat.exe";
                netstat.StartInfo.Arguments = "-ano";
                netstat.StartInfo.UseShellExecute = false;
                netstat.StartInfo.RedirectStandardOutput = true;
                netstat.StartInfo.CreateNoWindow = true;
                netstat.Start();
                string output = netstat.StandardOutput.ReadToEnd();
                netstat.WaitForExit();

                foreach (string line in output.Split('\n'))
                {
                    if (line.Contains("LISTENING"))
                    {
                        string[] parts = line.Trim().Split(new char[] { ' ' }, StringSplitOptions.RemoveEmptyEntries);
                        if (parts.Length >= 5 && parts[1].EndsWith(":8080"))
                        {
                            int pid;
                            if (int.TryParse(parts[parts.Length - 1], out pid) && pid > 0)
                            {
                                try
                                {
                                    Process p = Process.GetProcessById(pid);
                                    if (p != null && (p.ProcessName.ToLower().Contains("node") || p.ProcessName.ToLower().Contains("actel")))
                                    {
                                        p.Kill();
                                        p.WaitForExit(1000);
                                    }
                                }
                                catch { }
                            }
                        }
                    }
                }
            }
            catch { }
        }

        private static void StartNodeServer()
        {
            string nodeExe = "node.exe";
            string localNode = Path.Combine(appDir, "bin", "node.exe");
            if (File.Exists(localNode))
            {
                nodeExe = localNode;
            }

            string serverScript = Path.Combine(appDir, "server.js");

            ProcessStartInfo psi = new ProcessStartInfo();
            psi.FileName = nodeExe;
            psi.Arguments = "\"" + serverScript + "\"";
            psi.WorkingDirectory = appDir;
            psi.UseShellExecute = false;
            psi.CreateNoWindow = true;
            psi.WindowStyle = ProcessWindowStyle.Hidden;

            try
            {
                nodeProcess = Process.Start(psi);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Node.js sunucusu başlatılamadı:\n" + ex.Message, "ACTel Hatası", MessageBoxButtons.OK, MessageBoxIcon.Error);
                Environment.Exit(1);
            }
        }

        private static Icon CreateMotorsportIcon()
        {
            try
            {
                using (Bitmap bmp = new Bitmap(32, 32))
                using (Graphics g = Graphics.FromImage(bmp))
                {
                    g.SmoothingMode = SmoothingMode.AntiAlias;
                    g.Clear(Color.Transparent);

                    // Deep slate background circle
                    using (Brush bgBrush = new SolidBrush(Color.FromArgb(10, 14, 22)))
                    {
                        g.FillEllipse(bgBrush, 1, 1, 30, 30);
                    }

                    // Electric Cyan outer border
                    using (Pen cyanPen = new Pen(Color.FromArgb(0, 229, 255), 2f))
                    {
                        g.DrawEllipse(cyanPen, 2, 2, 28, 28);
                    }

                    // Neon Green shift rev arch
                    using (Pen greenPen = new Pen(Color.FromArgb(0, 230, 118), 2.5f))
                    {
                        g.DrawArc(greenPen, 5, 5, 22, 22, 200, 140);
                    }

                    // High-contrast racing red center hub
                    using (Brush redBrush = new SolidBrush(Color.FromArgb(255, 23, 68)))
                    {
                        g.FillEllipse(redBrush, 12, 12, 8, 8);
                    }

                    IntPtr hIcon = bmp.GetHicon();
                    return Icon.FromHandle(hIcon);
                }
            }
            catch
            {
                return SystemIcons.Application;
            }
        }

        private static void SetupSystemTray()
        {
            trayIcon = new NotifyIcon();
            trayIcon.Text = "ACTel Pro - Assetto Corsa Cockpit";
            trayIcon.Icon = CreateMotorsportIcon();
            trayIcon.Visible = true;

            ContextMenuStrip menu = new ContextMenuStrip();
            menu.Renderer = new ToolStripProfessionalRenderer();

            ToolStripMenuItem itemOpen = new ToolStripMenuItem("🏎️ Kokpit Dashboard'ı Aç", null, (s, e) => LaunchDesktopApp("http://localhost:8080"));
            itemOpen.Font = new Font(menu.Font, FontStyle.Bold);

            ToolStripMenuItem itemButtons = new ToolStripMenuItem("🎛️ Buton Kutusu (Button Box)", null, (s, e) => LaunchDesktopApp("http://localhost:8080/#buttonbox"));
            ToolStripMenuItem itemTelemetry = new ToolStripMenuItem("📈 Canlı Telemetri Grafikleri", null, (s, e) => LaunchDesktopApp("http://localhost:8080/#telemetry"));
            ToolStripMenuItem itemSetup = new ToolStripMenuItem("⚙️ Kurulum & Cihaz Eşleştirme", null, (s, e) => LaunchDesktopApp("http://localhost:8080/#settings"));
            
            ToolStripSeparator sep = new ToolStripSeparator();
            
            ToolStripMenuItem itemExit = new ToolStripMenuItem("❌ Çıkış", null, (s, e) => {
                trayIcon.Visible = false;
                Application.Exit();
            });

            menu.Items.Add(itemOpen);
            menu.Items.Add(itemButtons);
            menu.Items.Add(itemTelemetry);
            menu.Items.Add(itemSetup);
            menu.Items.Add(sep);
            menu.Items.Add(itemExit);

            trayIcon.ContextMenuStrip = menu;
            trayIcon.DoubleClick += (s, e) => LaunchDesktopApp("http://localhost:8080");
        }

        private static string FindBrowserPath()
        {
            string localAppData = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
            string programFiles = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
            string programFilesX86 = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86);

            string[] candidates = new string[]
            {
                Path.Combine(programFilesX86, @"Microsoft\Edge\Application\msedge.exe"),
                Path.Combine(programFiles, @"Microsoft\Edge\Application\msedge.exe"),
                Path.Combine(localAppData, @"Microsoft\Edge\Application\msedge.exe"),
                Path.Combine(programFiles, @"Google\Chrome\Application\chrome.exe"),
                Path.Combine(programFilesX86, @"Google\Chrome\Application\chrome.exe"),
                Path.Combine(localAppData, @"Google\Chrome\Application\chrome.exe")
            };

            foreach (string candidate in candidates)
            {
                if (!string.IsNullOrEmpty(candidate) && File.Exists(candidate))
                {
                    return candidate;
                }
            }

            return null;
        }

        private static void LaunchDesktopApp(string url)
        {
            try
            {
                string browser = FindBrowserPath();
                if (browser != null)
                {
                    // Clean frameless app window, optimal 1366x840 sim proportions
                    Process.Start(browser, "--app=\"" + url + "\" --window-size=1366,840");
                }
                else
                {
                    Process.Start(url);
                }
            }
            catch
            {
                try
                {
                    Process.Start(url);
                }
                catch { }
            }
        }

        private static void OnExit(object sender, EventArgs e)
        {
            if (trayIcon != null)
            {
                trayIcon.Visible = false;
                trayIcon.Dispose();
            }

            if (nodeProcess != null && !nodeProcess.HasExited)
            {
                try
                {
                    nodeProcess.Kill();
                }
                catch { }
            }
        }
    }
}
