using System;
using System.Collections.Generic;
using System.Windows.Forms;

namespace CheckersServer {
    static class Program {

        /**
         * CUR_PHASE is the current project phase. 
         * This static constant is used throughout the server to check what
         * features should be enabled or disabled.
         */
        public enum Phase {
            GAME,       //game playing and observing is enabled.
            FULL        //adds additional server functions: profiles and authentication. For Spring 2009 
                        //FULL should -not- be used.
        };
        public static Phase CUR_PHASE = Phase.GAME;
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main() {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            FileLogger.writeToLog(@" 
                    ==== 
                    | Startup: "+"<" + DateTime.Now.ToShortDateString() + " " + DateTime.Now.ToShortTimeString() + @"> 
                    ==== ");
            Application.Run(new ServerGUI());
        }
    }
}
