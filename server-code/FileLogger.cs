using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

namespace CheckersServer {
    class FileLogger {
        private static string filePath = "C:\\Documents and Settings\\cse2102\\CheckersServerLog\\consoleLog.txt";
        private static string excepFilePath = "C:\\Documents and Settings\\cse2102\\CheckersServerLog\\exceptionLog.txt";
        
        public static void writeToLog(string s) {
            try {
                File.AppendAllText(filePath, s + "\r\n");
            }
            catch (Exception) { }
        }
        public static void writeToExceptionLog(string s)
        {
            try
            {
                File.AppendAllText(excepFilePath, "<" + DateTime.Now.ToShortDateString() + " " + DateTime.Now.ToShortTimeString() + "> " + s + "\r\n");
            }
            catch (Exception) { }
        }
    }
}
