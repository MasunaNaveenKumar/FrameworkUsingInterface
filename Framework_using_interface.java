// Framework program using an interface to switch between multiple database servers.

import java.sql.*;
import java.util.Scanner;

interface iServer
{
    public Connection establishConnection();
}

class MySQLServer implements iServer
{
    Connection connection = null;
    public Connection establishConnection()
    {
        System.out.println("connected to MySQL server.");
        try
        {
            connection = DriverManager.getConnection("jdbc:mysql://165.22.14.77/dbNaveen?autoReconnect=true&useSSL=false", "root", "pwd");
        }
        catch(SQLException ex)
        {
            System.out.println(ex);
        }
        return connection;
    }
}

class SQLiteServer implements iServer
{
    Connection connection = null;
    public Connection establishConnection()
    {
        System.out.println("connected to SQLite server.");
        try
        {
            String databaseName = "frameworkDatabase.db";
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseName);
        }
        catch(Exception e)
        {
            System.out.println("Exception: " + e);
        }
        return connection;
    }
}

class Framework
{
	static Connection connection;
	static Scanner scanner = new Scanner(System.in);
    static String tableName;

    public static void main(String args[])
    {
        String className;
        if(args.length == 1)
        {
            className = args[0];
        }
        else
        {
            System.out.print("To which database server do you want to connect: MySQLServer or SQLiteServer \nPlease enter one database server name: ");
            className = scanner.nextLine();
        }
        try
        {
            iServer oServer = (iServer)Class.forName(className).newInstance();
            connection = oServer.establishConnection();
            if (connection != null)
            {
                System.out.print("Enter table name: ");
                tableName = scanner.next();
                storeColumnNamesIntoArray();
                showMenu();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
            System.out.println("Exception occured while connecting to database.");
        }
    }

    static Statement statement;
    static ResultSet resultSet;
    static int columnCount;
    static String[] columnNames;

    public static void storeColumnNamesIntoArray()
    {
        try
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select * from " + tableName);

            ResultSetMetaData metadata = resultSet.getMetaData();
            columnCount = metadata.getColumnCount();
            columnNames = new String[columnCount];

            for (int counter = 0; counter < columnCount; counter ++)
            {
                columnNames[counter] = metadata.getColumnName(counter + 1);
            }
        }
        catch(SQLException ex)
        {
            System.out.println(ex);
        }
    }

    public static void drawLine(String sentence)
    {
        System.out.println(sentence);
        for(int counter = 0; counter < sentence.length(); counter++)
        {
            System.out.print('-');
        }
        System.out.println();
    }

    static int choice;

    public static void showMenu()
    {
        scanner = new Scanner(System.in);
        while(true)
        {
            String Heading = tableName + " table.";
            drawLine(Heading);
            System.out.print("1. Add a record.\n2. Show all records.\n3. Search a record.\n");
            System.out.print("4. Update a record.\n5. Delete a record.\n6. Exit.\nEnter your choice: ");
            try
            {
                choice = scanner.nextInt();
                scanner.nextLine();
            }
            catch(Exception e)
            {
                System.out.println("\nINVALID INPUT.\n");
                showMenu();
            }
            switch(choice)
            {
                case 1: create();
                        break;
                case 2: read();
                        break;
                case 3: search();
                        break;
                case 4: update();
                        break;
                case 5: delete();
                        break;
                case 6: terminate();
                        System.exit(0);
                default: System.out.println("\nINVALID CHOICE.\n");
            }
            System.out.println();
        }
    }

    public static void create()
    {
        String insertQuery;
        
        String values = "";
        for(int columnCounter = 0; columnCounter < columnCount - 1; columnCounter++)
        {
            System.out.print("Please enter " + columnNames[columnCounter] + ": ");
            values = values + "'" + scanner.nextLine() + "', ";
        }
        values = values + "'active'";

        insertQuery = "insert into "+ tableName +" values(" + values + ")";

        try
        {
            statement.executeUpdate(insertQuery);
            System.out.println("\nRECORD INSERTED");
        }
        catch(SQLException ex)
        {
            System.out.println(ex);
        }
    }

    static String columnNamesHeading = "";

    public static void read()
    {
        String readQuery;
    	try
        {
            readQuery = "select * from " + tableName + " where " + columnNames[columnNames.length - 1] + " = 'active'";
            resultSet = statement.executeQuery(readQuery);

            columnNamesHeading = "";

            getColumnNamesFromArray();

            getValuesFromTable();
        }
        catch(SQLException ex)
        {
            System.out.println(ex);
        }
    }

    public static void getColumnNamesFromArray()
    {
        if( columnNames[columnCount - 1].equals("Status") )
        {
            for(int columnNameCounter = 0; columnNameCounter < columnCount ; columnNameCounter++)
            {
                if(columnNameCounter != columnCount - 1)
                {
                    columnNamesHeading = columnNamesHeading + String.format("%-25s", columnNames[columnNameCounter]);   
                }
                else
                {
                    columnNamesHeading = columnNamesHeading + String.format(columnNames[columnNameCounter]);
                }
            }
            drawLine(columnNamesHeading);
        }
        else
        {
            System.out.print("\nThere is no Status column in "+ tableName + " table.");
            System.out.println(" Please add Status column and re-execute the program.");
        }
    }

    public static void getValuesFromTable()
    {
        try
        {
            while(resultSet.next())
            {
                for(int valueCounter = 1; valueCounter <= columnCount; valueCounter++)
                {
                    System.out.print(String.format("%-25s", resultSet.getString(valueCounter))); 
                }
                System.out.println();
            } 
        }
        catch(SQLException ex)
        {
            System.out.println(ex);
        }
    }

    static String primaryKeyValue;

    public static void search()
    {
        String searchQuery;
    	try
        {
            columnNamesHeading = "";

            System.out.print("Enter " + columnNames[0] + ": ");
            primaryKeyValue = scanner.nextLine();

            getColumnNamesFromArray();

            searchQuery = "select * from "+ tableName +" where " + columnNames[0] + " = '" + primaryKeyValue + "' and " + columnNames[columnNames.length - 1] + " = 'active'";
            resultSet = statement.executeQuery(searchQuery);

            getValuesFromTable();
        }
        catch(SQLException ex)
        {
            System.out.println(ex);
        }
    }

    public static void update()
    {
    	System.out.print("Enter " + columnNames[0] + " to update details: ");
        primaryKeyValue = scanner.nextLine();

        drawLine("Select a field to update:");
        for(int index = 1; index < columnCount - 1; index++)
        {
            System.out.println((index) + ". " + columnNames[index] );
        }
        System.out.print("Enter your choice: ");

        int choiceForUpdate = scanner.nextInt();
        scanner.nextLine();

        String updateQuery;
        try
        {
            if(choiceForUpdate > 0 && choiceForUpdate < columnCount - 1 )
            {
                System.out.print("Enter new " + columnNames[choiceForUpdate] + " to update: ");
                String updated_column_value = scanner.nextLine();
                updateQuery = "update " + tableName + " set " + columnNames[choiceForUpdate] + " = '"+ updated_column_value + "' where " + columnNames[0] + " = '" + primaryKeyValue + "' and " + columnNames[columnNames.length - 1] + " = 'active'";
                // System.out.println(updateQuery);

                int rowsChanged = statement.executeUpdate(updateQuery);
                if(rowsChanged > 0)
                {
                    System.out.println("\n" + columnNames[choiceForUpdate] + " of " + tableName + " updated.");  
                }
                else
                {
                    System.out.println("\nNO RECORDS FOUND.\n");
                }
            }
            else
            {
                System.out.println("\nINVALID CHOICE FOR UPDATE");
            }
        }
        catch(SQLException ex)
        {
            System.out.println(ex);
        }
    }

    public static void delete()
    {
        String deleteQuery;
    	try
        {
            System.out.print("Enter " + columnNames[0] + ": ");
            primaryKeyValue = scanner.nextLine();

            deleteQuery = "update "+ tableName +" set " + columnNames[columnNames.length - 1] + " = 'inactive' where " + columnNames[0] + " = '" + primaryKeyValue + "' and " + columnNames[columnNames.length - 1]+ " = 'active'";

            int rowsChanged = statement.executeUpdate(deleteQuery);
            if(rowsChanged > 0)
            {
                System.out.println( "\nRECORD DELETED.");   
            }
            else
            {
                System.out.println("\nNO RECORDS FOUND.\n");
            }
        }
        catch(SQLException ex)
        {
            System.out.println(ex);
        }
    }

    public static void terminate()
    {
    	try
        {
            connection.close();
        }
        catch(SQLException ex)
        {
            System.out.println(ex);
        }
        System.out.println("\nTHANK YOU.\n");
    }
}