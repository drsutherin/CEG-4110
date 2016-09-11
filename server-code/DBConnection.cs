using System;
using System.Text;
//using MySql.Data.MySqlClient;
using System.Data;
using System.Collections;

namespace CheckersServer {

    /// <summary>
    /// Static class to define a connection to a MySQL database. This class requires the
    /// MySqlClient dll in the same directory of the exe. This class assumes that the DB has 
    /// two tables, users(user_id, name, password) and profile(pk_profile, fk_user, desc, win, loss, img).
    /// Currently img is not used. 
    /// 
    /// Calls to DBConnection, especially to autenticate, should not be enabled until the last part of the 
    /// project. 
    /// 
    /// -derek
    /// 11.09.08
    /// </summary>
    public class DBConnection {
        public enum QueryType { Select, Insert };
        static string MyConString = "SERVER=137.99.11.115;" +
                       "DATABASE=checkers;" +
                       "UID=checkersServer;" +
                       "PASSWORD=checkers;";

        public static int addUser(string user, string password){           
            MySqlConnection connection = new MySqlConnection(MyConString);
            MySqlCommand cmd = connection.CreateCommand();
            //first see if the user exists.
            connection.Open();
            cmd.CommandText = "select user_id from users where name ='" + user + "'";
            int fk_user;
            try {
                MySqlDataReader r = cmd.ExecuteReader();
                r.Read();
                fk_user = r.GetInt32(0);
                if (fk_user > 0)
                    return -1;
            }
            catch (Exception) { }
            finally { connection.Close(); }
            //now get a new user_id
            connection.Open();
            cmd.CommandText = "select max(user_id) from users";
            int x;
            try {
                MySqlDataReader r = cmd.ExecuteReader();
                r.Read();
                x = r.GetInt32(0);
                x++;
            }
            catch (Exception) {
                x = 1;
            }
            connection.Close();
            connection.Open();
            cmd.CommandText = "insert into users values('"+x.ToString()+"','"+user+"','"+password+"')";
            int ret = cmd.ExecuteNonQuery();
            connection.Close();
            DBConnection.insertNewProfile(x);
            return 1;
        }

        public static int insertNewProfile(int fk_user){        
            MySqlConnection connection = new MySqlConnection(MyConString);
            MySqlCommand cmd = connection.CreateCommand();
            connection.Open();
            cmd.CommandText = "select max(pk_profile) from profile";
            int x;
            try {
                MySqlDataReader r = cmd.ExecuteReader();
                r.Read();
                x = r.GetInt32(0);
                x++;
            }
            catch (Exception) {
                x = 1;
            }
            connection.Close();
            connection.Open();
            cmd.CommandText = "insert into profile values ("+x.ToString()+",'"+fk_user.ToString()+"','0','0',' ','')";
            int ret = cmd.ExecuteNonQuery();
            connection.Close();
            return ret;
        }

        public static int updateWin(string user){
            MySqlConnection connection = new MySqlConnection(MyConString);
            MySqlCommand cmd = connection.CreateCommand();
            connection.Open();
            cmd.CommandText = "select user_id from users where name ='" + user + "'";
            int fk_user;
            try {
                MySqlDataReader r = cmd.ExecuteReader();
                r.Read();
                fk_user = r.GetInt32(0);
            } catch (Exception) {
                return -1;
            }
            connection.Close();
            connection.Open();
            cmd.CommandText = "select win from profile where fk_user ='" + fk_user.ToString() + "'";
            int win;
            try {
                MySqlDataReader r = cmd.ExecuteReader();
                r.Read();
                win = r.GetInt32(0);
            } catch (Exception) {
                return -1;
            }
            connection.Close();
            connection.Open();
            cmd.CommandText = "update profile set win = '" + ++win + "' where fk_user = '" + fk_user.ToString() + "' limit 1";
            int ret = cmd.ExecuteNonQuery();
            connection.Close();
            return 1;
        }

        public static int updateLoss(string user) {
            MySqlConnection connection = new MySqlConnection(MyConString);
            MySqlCommand cmd = connection.CreateCommand();
            connection.Open();
            cmd.CommandText = "select user_id from users where name ='" + user + "'";
            int fk_user;
            try {
                MySqlDataReader r = cmd.ExecuteReader();
                r.Read();
                fk_user = r.GetInt32(0);
            } catch (Exception) {
                return -1;
            }
            connection.Close();
            connection.Open();
            cmd.CommandText = "select loss from profile where fk_user ='" + fk_user.ToString() + "'";
            int loss;
            try {
                MySqlDataReader r = cmd.ExecuteReader();
                r.Read();
                loss = r.GetInt32(0);
            } catch (Exception) {
                return -1;
            }
            connection.Close();
            connection.Open();
            cmd.CommandText = "update profile set loss = '" + ++loss + "' where fk_user = '" + fk_user.ToString() + "' limit 1";
            int ret = cmd.ExecuteNonQuery();
            connection.Close();
            return 1;
        }

        public static int updateProfile(string user, string profile) {
            MySqlConnection connection = new MySqlConnection(MyConString);
            MySqlCommand cmd = connection.CreateCommand();
            connection.Open();
            cmd.CommandText = "select user_id from users where name ='"+user+"'";
            int fk_user;
            try {
                MySqlDataReader r = cmd.ExecuteReader();
                r.Read();
                fk_user = r.GetInt32(0);
            }
            catch (Exception) {
                return -1;
            }
            connection.Close();
            connection.Open();
            cmd.CommandText = "update profile set profile = '"+profile+"' where fk_user = '"+fk_user.ToString()+"' limit 1";
            int ret = cmd.ExecuteNonQuery();
            connection.Close();
            return 1;
        }

        /// <summary>
        /// Returns the profile of user.
        /// </summary>
        /// <param name="user">username to get the profile for</param>
        /// <returns>string array of the profile. [0] = name, [1] = wins, [2] = loss, [3] = profile.</returns>
        public static string[] getProfile(string user) {
            MySqlConnection connection = new MySqlConnection(MyConString);
            MySqlCommand cmd = connection.CreateCommand();
            connection.Open();
            cmd.CommandText = "select user_id from users where name ='" + user + "'";
            int fk_user;
            try {
                MySqlDataReader r = cmd.ExecuteReader();
                r.Read();
                fk_user = r.GetInt32(0);
            }
            catch (Exception) {
                return null;
            }
            connection.Close();
            connection.Open();
            cmd.CommandText = "select win, loss, profile from profile where fk_user ='" + fk_user + "'";
            string[] ret = new string[4];
            ret[0] = user;
            try {
                MySqlDataReader r = cmd.ExecuteReader();
                r.Read();
                ret[1] = r.GetString(0);
                ret[2] = r.GetString(1);
                ret[3] = r.GetString(2);
            }
            catch (Exception) {
                return null;
            }
            connection.Close();
            return ret;
        }

        public static bool login(string user, string pwd) {
            MySqlConnection connection = new MySqlConnection(MyConString);
            MySqlCommand cmd = connection.CreateCommand();
            connection.Open();
            cmd.CommandText = "select password from users where name ='" + user + "'";
            string password;
            try {
                MySqlDataReader r = cmd.ExecuteReader();
                r.Read();
                password = r.GetString(0);
            }
            catch (Exception) {
                return false;
            }
            connection.Close();
            return password.Equals(pwd);
        }
    }                
}
