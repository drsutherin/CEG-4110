using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.Net;


namespace CheckersServer {
    /// <summary>
    /// This class defines form logic. Server initialization
    /// also begins in the constructor.
    /// 
    /// derk 8.13.08
    /// </summary>
    public partial class ServerGUI : Form {

        GameServer serverCore;
        //a generic object that can be used as a locking mechanism. 
       // static readonly object theLock = new object();

        public ServerGUI() {
            InitializeComponent();
            string strHostName = System.Net.Dns.GetHostName();
            IPHostEntry ipEntry = System.Net.Dns.GetHostEntry(strHostName);
            IPAddress[] addr = ipEntry.AddressList;
            lblAddress.Text = "Server IP address is " + addr[2].ToString();
            serverCore = new GameServer(this);
            output("Server ready to accept connections!");
        }

        private void btnClear_Click(object sender, EventArgs e) {
            txtStatus.Text = "";
        }

        /// <summary>
        /// output text to the GUI window in a threadsafe fashion. 
        /// </summary>
        /// <param name="s">The message to output</param>
        public void output(string s) {
            if (txtStatus.InvokeRequired) {
                this.txtStatus.Invoke(new outputTextDelegate(this.output), s);
            } else {
                string status = "<" + DateTime.Now.ToShortDateString() + " " + DateTime.Now.ToShortTimeString() + "> " + s + "\r\n";
                txtStatus.Text += status;
                FileLogger.writeToLog(status);
            }
        }
        private delegate void outputTextDelegate(string s);

        private void ServerGUI_FormClosed(object sender, FormClosedEventArgs e) {
            serverCore.tearDownAll();
            Application.Exit();
        }

        private void button1_Click(object sender, EventArgs e) {
            // Shut down the current instance
            Application.Exit();
            // Restart the app
            System.Diagnostics.Process.Start(Application.ExecutablePath);
        }

        private void button2_Click(object sender, EventArgs e) {
            //serverCore.stopListening();
            Application.Exit();
        }

        private void button1_Click_1(object sender, EventArgs e) {
            if (textBox1.Text.Equals("localmode")) {
                Program.CUR_PHASE = Program.Phase.GAME;
                output("** localmode enabled **");
            }
            else if (!this.textBox1.Text.Equals("")) {
                serverCore.removeUser(this.textBox1.Text);
            }
        }

    }
}
