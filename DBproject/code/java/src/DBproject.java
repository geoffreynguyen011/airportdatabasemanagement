/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Plane");
				System.out.println("2. Add Pilot");
				System.out.println("3. Add Flight");
				System.out.println("4. Add Technician");
				System.out.println("5. Book Flight");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order");
				System.out.println("8. List total number of repairs per year in ascending order");
				System.out.println("9. Find total number of passengers with a given status");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPlane(esql); break;
					case 2: AddPilot(esql); break;
					case 3: AddFlight(esql); break;
					case 4: AddTechnician(esql); break;
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddPlane(DBproject esql) {//1
		try{
         		String query = "INSERT INTO Plane(make, model, age, seats) VALUES(";
         		System.out.print("\tEnter make: ");
         		String input = in.readLine();
         		query += "'" + input + "',";

			System.out.print("\tEnter model: ");
         		input = in.readLine();
         		query += "'" + input + "',";

			System.out.print("\tEnter age of the plane: ");
         		input = in.readLine();
         		query += input + ',';

			System.out.print("\tEnter number of seats: ");
         		input = in.readLine();
         		query += input + ");";

         		esql.executeUpdate(query);
      		}catch(Exception e){
         		System.err.println (e.getMessage());
      		}	
	}

	public static void AddPilot(DBproject esql) {//2
		try{
         		String query = "INSERT INTO Pilot(fullname, nationality) VALUES(";
         		System.out.print("\tEnter their full name: ");
         		String input = in.readLine();
         		query += "'" + input + "',";

			System.out.print("\tEnter their nationality: ");
         		input = in.readLine();
         		query += "'" + input + "');";
			
			//System.out.print(query + '\n');

         		esql.executeUpdate(query);
      		}catch(Exception e){
         		System.err.println (e.getMessage());
      		}
	}

	public static void AddFlight(DBproject esql) {//3
		// Given a pilot, plane and flight, adds a flight in the DB
		try{
			String query = "INSERT INTO Flight (cost, num_sold, num_stops, actual_departure_date, actual_arrival_date, arrival_airport, departure_airport) VALUES (";
			System.out.print("\tEnter flight cost: ");
			String input = in.readLine();
			query += input + ",";
		
			System.out.print("\tEnter number of flight tickets sold: ");
			input = in.readLine();
			query += input + ",";
		

			System.out.print("\tEnter number of flight stops:");
			input = in.readLine();
			query += input + ",";


			System.out.print("\tEnter departure date: ");
			input = in.readLine();
			query += "'" + input + "',";


			System.out.print("\tEnter arrival date: ");
			input = in.readLine();
			query += "'" + input + "',";


			System.out.print("\tEnter the airport it arrives at: ");
			input = in.readLine();
			query += "'" + input + "',";

			System.out.print("\tEnter the airport it departs from: ");
			input = in.readLine();
			query += "'" + input + "');";
			
			esql.executeUpdate(query);

			query = "INSERT INTO FlightInfo (flight_id, pilot_id, plane_id) VALUES (";
			query += esql.getCurrSeqVal("flight_num") + ",";
	
			System.out.print("\tEnter pilot ID:");
			input = in.readLine();
			query += input + ",";


			System.out.print("\tEnter plane ID:");
			input = in.readLine();
			query += input + ");";

			esql.executeUpdate(query);

			query = "INSERT INTO Schedule (flightNum, departure_time, arrival_time) VALUES (";
			query += esql.getCurrSeqVal("flight_num") + ",";

			System.out.print("\tEnter departure time: ");
			input = in.readLine();
			query += "'" + input + "',";

			System.out.print("\tEnter arrival time: ");
			input = in.readLine();
			query += "'" + input + "');";
			
			esql.executeUpdate(query);
				
	      }catch(Exception e){
		 System.err.println (e.getMessage());
	      }
	}

	public static void AddTechnician(DBproject esql) {//4
		try{
         		String query = "INSERT INTO Technician(full_name) VALUES(";
         		System.out.print("\tEnter their full name: ");
         		String input = in.readLine();
         		query += "'" + input + "');";
			
			//System.out.print(query + '\n');

         		esql.executeUpdate(query);
      		}catch(Exception e){
         		System.err.println (e.getMessage());
      		}
	}

	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		try{
			System.out.print("\tEnter customer id: ");
         		String cid = in.readLine();         		
			String query = "SELECT (P.seats - F.num_sold) FROM FlightInfo FI, Flight F, Plane P WHERE (F.fnum = ";
         		System.out.print("\tEnter flight number: ");
         		String flight = in.readLine();
         		query += flight +  " AND F.fnum = FI.flight_id AND P.id = FI.plane_id);";
			
			List<List<String>> result = esql.executeQueryAndReturnResult(query);
			List<String> hold = result.get(0);
			int num_seats = Integer.parseInt(hold.get(0));
			
			if(num_seats > 0){
				query = "INSERT INTO Reservation(cid,fid,status) VALUES(" + cid + "," + flight + ", 'C'); UPDATE Flight SET num_sold = num_sold + 1 WHERE fnum = " + flight + ";";    			
			}
			else{
				query = "INSERT INTO Reservation(cid,fid,status) VALUES(" + cid + "," + flight + ", 'W');";
			}
			//System.out.print(num_seats + '\n');

         		int rowCount = esql.executeQuery(query);
         		System.out.println ("total row(s): " + rowCount + "\n");
      		}catch(Exception e){
         		System.err.println (e.getMessage());
      		}
	}

	public static void ListNumberOfAvailableSeats(DBproject esql) {//6
		// For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
		try{
         		String query = "SELECT (P.seats - F.num_sold) as Seats_Available FROM FlightInfo FI, Flight F, Plane P WHERE (F.fnum = ";
         		System.out.print("\tEnter their flight number: ");
         		String input = in.readLine();
         		query += input + " AND F.fnum = Fi.flight_id AND P.id = FI.plane_id);";

			System.out.print("\tEnter departure date (YYYY-MM-DD): ");
         		input = in.readLine();
			
			//System.out.print(query + '\n');

         		int rowCount = esql.executeQueryAndPrintResult(query);
         		System.out.println ("total row(s): " + rowCount + "\n");
      		}catch(Exception e){
         		System.err.println (e.getMessage());
      		}
	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
		// Count number of repairs per planes and list them in descending order
		try{
         		String query = "SELECT P.id, COUNT(R.rid) FROM Plane P, Repairs R WHERE P.id = R.plane_id GROUP BY P.id ORDER BY COUNT DESC;";
			
			//System.out.print(query + '\n');

         		int rowCount = esql.executeQueryAndPrintResult(query);
         		System.out.println ("total row(s): " + rowCount + "\n");
      		}catch(Exception e){
         		System.err.println (e.getMessage());
      		}
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		// Count repairs per year and list them in ascending order
		try{
         		String query = "SELECT DISTINCT Year, SUM(count) AS num_repairs FROM (SELECT EXTRACT(YEAR FROM R.repair_date) AS Year, COUNT(R.rid) FROM Repairs R GROUP BY R.repair_date) AS counts GROUP BY Year ORDER BY num_repairs ASC;";
			
			//System.out.print(query + '\n');

         		int rowCount = esql.executeQueryAndPrintResult(query);
         		System.out.println ("total row(s): " + rowCount + "\n");
      		}catch(Exception e){
         		System.err.println (e.getMessage());
      		}
	}
	
	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
		try{
         		String query = "SELECT COUNT(R.rnum) AS reservations FROM Reservation R WHERE (R.fid = ";
         		System.out.print("\tEnter flight id: ");
         		String input = in.readLine();
         		query += input;
			
			query += " AND R.status = ";

			System.out.print("\tEnter status ('W', 'C', or 'R'): ");
         		input = in.readLine();
         		query += "'" + input + "');";
			
			//System.out.print(query + '\n');

         		int rowCount = esql.executeQueryAndPrintResult(query);
         		System.out.println ("total row(s): " + rowCount + "\n");
      		}catch(Exception e){
         		System.err.println (e.getMessage());
      		}
	}
}
