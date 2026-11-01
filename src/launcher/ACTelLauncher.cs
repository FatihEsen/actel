using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Net.NetworkInformation;
using System.Windows.Forms;

namespace ACTel
{
    static class Program
    {
        private static Process nodeProcess;
        private static NotifyIcon trayIcon;
        private static string appDir;
        private static string edgePath = @"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe";

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

            // 3. Create System Tray Icon
            SetupSystemTray();

            // 4. Launch Edge App Mode
            LaunchEdgeApp("http://localhost:8080");

            // 5. Clean exit hooks
            Application.ApplicationExit += OnExit;
            AppDomain.CurrentDomain.ProcessExit += OnExit;

            Application.Run();
        }

        private static void CleanStalePort8080()
        {
            try
            {
                // Simple netstat query to find PID on port 8080
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

        private static void SetupSystemTray()
        {
            trayIcon = new NotifyIcon();
            trayIcon.Text = "ACTel - Assetto Corsa Telemetry & Dashboard";

            // Default system application icon
            trayIcon.Icon = SystemIcons.Application;
            trayIcon.Visible = true;

            ContextMenuStrip menu = new ContextMenuStrip();
            
            ToolStripMenuItem itemOpen = new ToolStripMenuItem("🏎️ Dashboard'ı Aç", null, (s, e) => LaunchEdgeApp("http://localhost:8080"));
            ToolStripMenuItem itemSetup = new ToolStripMenuItem("⚙️ Kurulum & Cihazlar", null, (s, e) => LaunchEdgeApp("http://localhost:8080/#settings"));
            ToolStripSeparator sep = new ToolStripSeparator();
            ToolStripMenuItem itemExit = new ToolStripMenuItem("❌ Çıkış", null, (s, e) => {
                trayIcon.Visible = false;
                Application.Exit();
            });

            menu.Items.Add(itemOpen);
            menu.Items.Add(itemSetup);
            menu.Items.Add(sep);
            menu.Items.Add(itemExit);

            trayIcon.ContextMenuStrip = menu;
            trayIcon.DoubleClick += (s, e) => LaunchEdgeApp("http://localhost:8080");
        }

        private static void LaunchEdgeApp(string url)
        {
            try
            {
                if (File.Exists(edgePath))
                {
                    Process.Start(edgePath, "--app=\"" + url + "\" --window-size=1280,820");
                }
                else
                {
                    // Fallback to default browser
                    Process.Start(url);
                }
            }
            catch
            {
                Process.Start(url);
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

