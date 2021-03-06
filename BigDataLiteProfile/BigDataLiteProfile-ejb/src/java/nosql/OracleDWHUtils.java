/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nosql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author oracle
 */
public class OracleDWHUtils {
    private static final String DEFAULT_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String DEFAULT_URL = "jdbc:oracle:thin:@localhost:1521:cdb";
    private static final String DEFAULT_USERNAME = "zac";
    private static final String DEFAULT_PASSWORD = "welcome1";
    
    private static String insertPlayer = "", deletePlayer = "";

   private static Connection connection ;
    public static void main(String[] args)
    {
        long begTime = System.currentTimeMillis();

        String driver = ((args.length > 0) ? args[0] : DEFAULT_DRIVER);
        String url = ((args.length > 1) ? args[1] : DEFAULT_URL);
        String username = ((args.length > 2) ? args[2] : DEFAULT_USERNAME);
        String password = ((args.length > 3) ? args[3] : DEFAULT_PASSWORD);

         connection = null;

        try
        {
            Class.forName(DEFAULT_DRIVER);
                  
            connection = DriverManager.getConnection(url, username, password);
            DatabaseMetaData meta = connection.getMetaData();
            System.out.println(meta.getDatabaseProductName());
            System.out.println(meta.getDatabaseProductVersion());

            String usrExtOracle = "CREATE TABLE Utilisateur (IDUSER number(8),NOM varchar2(40),PRENOM varchar2(100),"
                    + "EMAIL varchar2(50), profil_id number(8)) ORGANIZATION EXTERNAL (TYPE ORACLE_HIVE DEFAULT DIRECTORY "
                    + "ORACLE_BIGDATA_CONFIG ACCESS PARAMETERS (com.oracle.bigdata.tablename=default.Utilisateur )) "
                    + "REJECT LIMIT UNLIMITED";


            String tableName = "Utilisateur";
            Statement stmt = connection.createStatement();           
            stmt.executeQuery("drop table "+tableName);         
            stmt.executeQuery(usrExtOracle);
            connection.setAutoCommit(false);          
            connection.commit();
            

        }
        catch (Exception e)
        {
            //rollback(connection);
            System.out.println(e.getMessage());
        } 
        finally
        {
            close(connection);
            long endTime = System.currentTimeMillis();
            System.out.println("wall time: " + (endTime - begTime) + " ms");
        }
    }

    public static void insertPlay(String fname, String lname, String natio, int atp) {
        try {
                System.out.println("in insert prepareStatement...");
                PreparedStatement insPlay = connection.prepareStatement(insertPlayer);
                System.out.println("prepareStatement ok...");
                insPlay.setString(1, fname);
                insPlay.setString(2, lname);
                insPlay.setString(3, natio);
                insPlay.setInt(4, atp);
                System.out.println("executeUpdate...");
                insPlay.executeUpdate();
                System.out.println("executeUpdate ok...");
                
        } catch (SQLException e) {
            System.out.println("Problème lors de la requête insertPlay");
            System.out.println(e.getMessage());
        }
    }
    public static Connection createConnection(String driver, String url, String username, String password) throws ClassNotFoundException, SQLException
    {
        Class.forName(driver);

        if ((username == null) || (password == null) || (username.trim().length() == 0) || (password.trim().length() == 0))
        {
            return DriverManager.getConnection(url);
        }
        else
        {
            return DriverManager.getConnection(url, username, password);
        }
    }

    public static void close(Connection connection)
    {
        try
        {
            if (connection != null)
            {
                connection.close();
            }
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }


    public static void close(Statement st)
    {
        try
        {
            if (st != null)
            {
                st.close();
            }
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void close(ResultSet rs)
    {
        try
        {
            if (rs != null)
            {
                rs.close();
            }
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void rollback(Connection connection)
    {
        try
        {
            if (connection != null)
            {
                connection.rollback();
            }
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static List<Map<String, Object>> map(ResultSet rs) throws SQLException
    {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

        try
        {
            if (rs != null)
            {
                ResultSetMetaData meta = rs.getMetaData();
                int numColumns = meta.getColumnCount();
                while (rs.next())
                {
                    Map<String, Object> row = new HashMap<String, Object>();
                    for (int i = 1; i <= numColumns; ++i)
                    {
                        String name = meta.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(name, value);
                    }
                    results.add(row);
                }
            }
        }
        finally
        {
            close(rs);
        }

        return results;
    }

    public static List<Map<String, Object>> query(Connection connection, String sql, List<Object> parameters) throws SQLException
    {
        List<Map<String, Object>> results = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            ps = connection.prepareStatement(sql);

            int i = 0;
            for (Object parameter : parameters)
            {
                ps.setObject(++i, parameter);
            }

            rs = ps.executeQuery();
            results = map(rs);
        }
        finally
        {
            close(rs);
            close(ps);
        }

        return results;
    }

    public static int update(Connection connection, String sql, List<Object> parameters) throws SQLException
    {
        int numRowsUpdated = 0;

        PreparedStatement ps = null;

        try
        {
            ps = connection.prepareStatement(sql);

            int i = 0;
            for (Object parameter : parameters)
            {
                ps.setObject(++i, parameter);
            }

            numRowsUpdated = ps.executeUpdate();
        }
        finally
        {
            close(ps);
        }

        return numRowsUpdated;
    }
}
