//intentionally un-packaged to facilitate testing

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Fotf {
	private enum Menu {MAIN, ADMIN, QUIT};
	private static final String QUIT_KEY = "q";
	private static final String ADMIN_KEY = "a";
	private static final String BACK_KEY = "b";
	private static final int BACK_KEY_VALUE = -1;
	private final Integer FIRST_ID = 1;
	private final String FIRST_USER_NAME = "Admin";
	private Menu currentMenu = Menu.MAIN;

	DB myDB = null;
	private Person currentUser = null;


	// constructor requires a DB connection
	public Fotf (DB myDB) {
		if (myDB == null) {
			throw new IllegalArgumentException("DB connection required");
		}
		this.myDB = myDB;
	}

	private void run() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException {

		// main menu
		currentMenu.equals(Menu.MAIN);

		while (currentMenu.equals(Menu.MAIN)){   // exit method will bail out

			System.out.println("\n\n\nHello, " + currentUser.getName());

			System.out.printf("\nMAIN MENU\n");
			System.out.printf("---------\n");

			// drop the view of overdue tasks, in case it was left over from a previous run
			myDB.execute("drop view recent_completes");
			
            // create a fresh view and show the main menu
	    	try {
		        myDB.execute("CREATE VIEW RECENT_COMPLETES AS select task_id, max(completed_date) AS LAST_DATE from completed_task group by task_id");
	
		        ResultSet rset = myDB.execute("SELECT T.NAME, T.ID , MAX(L.LAST_DATE) AS DONE, (MAX(L.LAST_DATE) + T.INTERVAL) AS DUE_DATE, SYSDATE - MAX(L.LAST_DATE) AS DAYS_OVERDUE " + 
		        				"FROM TASK T, RECENT_COMPLETES L WHERE T.PERSON_ID = " + currentUser.getId() + " AND L.TASK_ID = T.ID AND (SYSDATE - LAST_DATE >= T.INTERVAL) " + 
		        				"GROUP BY T.NAME, T.ID, INTERVAL ORDER BY (1 - DAYS_OVERDUE)");
	
				//Action on the result
		        System.out.println();
		        System.out.println("My Due Tasks:");
		        int i = 1;
		        // map for storing menu items to task id's so we can act on them
		        Map<String,Integer> dueTasks = new HashMap<String,Integer>();
		        System.out.println("\n   TASK NAME                     LAST DONE   DUE DATE    DAYS PAST DUE");
		        System.out.println("   ---------                     ---------   --------    -------------");
		        while (rset.next())
		        {
					String task_name = rset.getString("name");
					Integer tid = rset.getInt("id");
		        	dueTasks.put(String.valueOf(i), tid);
		        	Date due_date = rset.getDate("due_date");
		        	Date last_done = rset.getDate("done");
		        	Integer overdue = rset.getInt("days_overdue");
					System.out.println(i + ". " + ConsoleUtils.fixWidth(task_name, 28, Boolean.FALSE) + 
										"  " + last_done + "  " + due_date + "    " + overdue);
					i++;
		        }
	
		        System.out.println();
		        System.out.println("\nEnter the number of a task to complete ");
		        if (currentUser.getAdmin() == Boolean.TRUE) {
		        	dueTasks.put(ADMIN_KEY, 0);
		        	System.out.println("or '" + ADMIN_KEY + "' for Admin functions");
		        }
		        System.out.printf("or '" + QUIT_KEY + "' to quit >>");
		        dueTasks.put(QUIT_KEY, 0);
				Scanner in = new Scanner(System.in);
				String cmd = "";
				while (!dueTasks.containsKey(cmd)) {
					if (!cmd.equals("")) {
						System.out.println("Bad Input, try again");
					}
					cmd = in.nextLine();
				}
				//System.out.printf("                         ***DEBUG*** cmd=%s\n", cmd);
	
				if (cmd.equals(QUIT_KEY)) {
					currentMenu = Menu.QUIT;
				}
				else if (cmd.equals(ADMIN_KEY)) {
				       adminMenu();
				}
				else { // complete a numbered task
					Integer taskId = dueTasks.get(cmd);
					//System.out.println("                         ***DEBUG*** about to complete task with id=" + taskId);
					completeTask(taskId);
					System.out.println("\n\n ** COMPLETED! ** \n");
				}
	    	}
	    	catch (Exception e) {
	    		System.out.println("Major error, sorry");
	    	}
		}
	}

	//MAIN MENU METHODS
    private void completeTask(Integer choice) {
    	if (choice == null) {
    		throw new IllegalArgumentException("Error, id of task to complete is null");
    	}
        ResultSet taskInfo = myDB.execute("select * from task where id = " + choice);
        try {
			if (taskInfo.next()){
			    Integer taskid = taskInfo.getInt("id");
			    Integer payerid = null;
			    Integer payeeid = currentUser.getId();
			    Double amount = taskInfo.getDouble("reward");
			    String datePaid = null;
			    String completedDate = "SYSDATE";

			    myDB.execute("insert into completed_task values (" + taskid + 
			    												", " + payerid + 
			    												", " + payeeid + 
			    												", " + amount + 
			    												", " + datePaid + 
			    												", " + completedDate + ")");
			}
			else {
				System.out.println("ERROR! no task found when attempting to complete!");
			}
		} catch (SQLException e) {
			System.out.println("ERROR! unable to parse columns for tasks to complete!");
		}
    }
	// END OF MAIN MENU METHODS

	private void adminMenu() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		currentMenu = Menu.ADMIN;
		while (currentMenu.equals(Menu.ADMIN)){   // exit method will bail out
			System.out.println("\nADMIN MENU");
			System.out.println("---------");
			for (int i=0; i<adminFunctions[0].length; i++){
				System.out.printf("%d. %s\n", i+1, adminFunctions[0][i]);
			}
			System.out.println();
			System.out.printf("\nEnter the command\n or 'b' to go back >>");
			Scanner in = new Scanner(System.in);
			int cmd = 0;
			while ((cmd > adminFunctions[0].length) || (cmd < 1)){
				String cmdName = in.nextLine();
				//System.out.println("                         ***DEBUG*** got=[" + cmdName + "]");
				if (cmdName.equals(BACK_KEY)) {
					currentMenu = Menu.MAIN;
					break;
				}
				else {
					try{
						cmd = Integer.valueOf(cmdName);
					}
					catch (NumberFormatException ex){
						System.out.printf("bad input - please try again\n>");
						cmd = 0;  // reinitialize
					}
				}
			}
			//System.out.printf("                         ***DEBUG*** cmd=%d, name=%s\n", cmd, adminFunctions[1][cmd-1]);
			if (cmd != 0) {
				Fotf.class.getMethod(adminFunctions[1][cmd-1]).invoke(this);
			}
		}

	}
	// ADMIN MENU METHODS
	// each of these must take no params and return nothing
	// also must be public!
	public void listUsers() throws SQLException {

		// determine next id
        ResultSet rset = myDB.execute("select id,name,is_admin from person");

        //Action on the result
        System.out.println("\nID  NAME     IS ADMIN");
        System.out.println("--  ----     --------");
        while (rset.next())
        {
        	Integer id = rset.getInt("id");
        	String name = rset.getString("name");
            String admin = rset.getString("is_admin");
			System.out.println(id + "   " + ConsoleUtils.fixWidth(name, 11, Boolean.FALSE) + " " + admin);
        }
	}

	public void listTasks() throws SQLException {
        ResultSet rset = myDB.execute("select task.name as tname,interval,reward,person.name as pname,task_type.name as ttname from task,task_type, person where task_type_id = task_type.id and person_id = person.id");

		//Action on the result
        System.out.println("\nTASK NAME                  INTERVAL  REWARD    PERSON      TASK TYPE");
        System.out.println("---------                  --------  ------    ------      ---------");
        while (rset.next())
        {
        	String name = rset.getString("tname");
        	Integer interval = rset.getInt("interval");
        	Double reward = rset.getDouble("reward");
        	String person = rset.getString("pname");
        	String task_type = rset.getString("ttname");
        	System.out.println(ConsoleUtils.fixWidth(name, 30, Boolean.FALSE) + " " +
        						ConsoleUtils.fixWidth(String.valueOf(interval), 4, Boolean.FALSE) + " " +
        						ConsoleUtils.formatDollar(reward, 6) + "      " +
        						ConsoleUtils.fixWidth(person,10, Boolean.FALSE) + "  " +
        						task_type);
        }
	}
	public void addUser() throws SQLException {

		// ask user for identity, Deliverable, tag and NPT
		Scanner in = new Scanner(System.in);
		System.out.printf("\nEnter user name >>");
		String userName = in.nextLine();
		System.out.printf("\nAdmin (Y/N) >>");
		String adminFlag = in.nextLine();
		adminFlag.toUpperCase();
		if (!adminFlag.equals("Y")) {
			adminFlag = "N";
		}

		// determine next id
        ResultSet rset = myDB.execute("select max(id) from person");
        Integer maxId = null;
        if (rset.next())
        {
            maxId = rset.getInt("max(id)");
            // make sure we aren't maxed out on users
            if (maxId == Integer.MAX_VALUE) {
            	System.out.println("Error - no more users can be added");
            	return;
            }
        } else {
        	System.out.println("Error - no users found and there should be at least one");
        	System.exit(0);
        }

		// now add new user
        myDB.execute("insert into Person values(" + (++maxId) + ",'" + userName + "','" + adminFlag + "')");
	}

    public void processPay(){
            
    	// first show every owner with unpaid tasks
        ResultSet rset = myDB.execute("select id, name, count(*) from person, completed_task where id = payee_id and date_paid is null and amount > 0 group by id, name");
        // TODO need to not proceed if nothing in the result set
        System.out.println("\nID NAME      UNPAID TASKS");
        System.out.println("-- ----      ------------");
        try {
			while(rset.next()){
			    System.out.println(rset.getInt("id") + ". "+
			    		ConsoleUtils.fixWidth(rset.getString("name"), 10, Boolean.FALSE) + "     " +
			            rset.getInt("count(*)"));
			}
		} catch (SQLException e) {
			System.out.println("ERROR retreiving columns from tasks");
		}

        // TODO, number users independent of their ID's
		Integer input = promptForNumberOrBack("\n\nEnter ID of user to process\n or '" + BACK_KEY + "' to go back >>", 9999);
		if(input != BACK_KEY_VALUE){
			paymentScreen(input);
		}
    }

	//link from processPay; should update CompletedTask table via
    //view that is created
    private void paymentScreen(int choice) {
    	// destroy the temporary view, if it already exists
        myDB.execute("drop view toBePaid");

    	// now do the work
        Integer taskChoice, fakeChoice;
        myDB.execute("create view toBePaid as select payer_id,date_paid,completed_date as cdate,name,amount,task_id from completed_task join task on id = task_id where payee_id = " + choice + " and amount > 0 and date_paid is null");
        ResultSet rset = myDB.execute("select * from toBePaid");
        System.out.println("\n\nPayments Due:\n");
        int count =0;
        HashMap<Integer, Integer> quickLookUp = new HashMap<Integer, Integer>();
        //first integer is what appears to user, next one is its ID
        // TODO determine if there are no rows
        System.out.println("  COMPLETED DATE  AMOUNT  TASK NAME");
        System.out.println("  --------------  ------  ---------");
        try {
			while(rset.next()){
			    count++;

			    System.out.println(count + ". " + rset.getDate("cdate") + 
			            "     " + ConsoleUtils.formatDollar(rset.getDouble("amount"), 6) + 
			            "  " + rset.getString("name"));

			    int taskID = rset.getInt("task_id");
			    quickLookUp.put(count, taskID);
			}
		} catch (SQLException e) {
			System.out.println("ERROR printing tasks needing payment");
		}
        fakeChoice = promptForNumberOrBack("Enter Task Number to pay \nor '" + BACK_KEY + "' to go back >>", count);
        if(fakeChoice != BACK_KEY_VALUE){
            taskChoice = quickLookUp.get(fakeChoice);
            //taskChoice defined when user makes selection
            if(taskChoice != null){
                myDB.execute("update toBePaid set payer_id = "
                        + currentUser.getId() + ", date_paid = SYSDATE where task_id = "+
                        taskChoice);
            } 
        	else {
        		System.out.println("ERROR, paid task not found in map!");
        	}
        }
    }

	public void addTask() throws SQLException {
		// prompt the user for new task data
		System.out.println("\nADD TASK\n");
		
		// first verify we have some types
		// TODO, test with empty DB!!
        ResultSet rset = myDB.execute("select max(id) from task_type");
        int taskTypeId = 0;
        if (rset.next())
        {
            taskTypeId = rset.getInt("max(id)");
            // make sure we aren't maxed out on users
            if (taskTypeId == Integer.MAX_VALUE) {
            	System.out.println("Error - no more types can be added");
            	return;
            }
        } 
        Scanner in = new Scanner(System.in);
		if (taskTypeId == 0) {  // no task types, add one
			System.out.println("No Task Types found, please enter a new one");
			String newTaskTypeName = "";
			while (newTaskTypeName.equals("")) {
				System.out.printf("\nEnter new task type name >>");
				newTaskTypeName = in.nextLine();
			}
			taskTypeId = FIRST_ID;

			// now add the new tasktype
			myDB.execute("insert into task_type values (taskTypeId, " + newTaskTypeName + ", null)");
		}
		else {
			// we have some task types pick one
    		rset = myDB.execute("select id,name,description as tdesc from task_type");

	        System.out.println();
	        int i = 1;
	        // map for storing menu items to task id's so we can act on them
	        Map<Integer,Integer> taskTypes = new HashMap<Integer,Integer>();
	        System.out.println("\n   TASK_TYPE  DESCRIPTION");
			while (rset.next())
			{
				String type = rset.getString("name");
				Integer id = rset.getInt("id");
				taskTypes.put(i, id);
				String desc = rset.getString("tdesc");
				if (desc == null) desc = "none";
				System.out.println(i + ". " + ConsoleUtils.fixWidth(type, 30, Boolean.FALSE) + "  " + ConsoleUtils.fixWidth(desc, 30, Boolean.FALSE));
				i++;
			}

			Integer input = promptForNumberOrBack("Select the task type \n or '" + BACK_KEY + "' to go back >>", i);
			if(input != BACK_KEY_VALUE){
				taskTypeId = taskTypes.get(input);
				
				// now get the other stuff for the task itself
				String newTaskName = "";
				while (newTaskName.equals("")) {
					System.out.printf("\nEnter new task name >>");
					newTaskName = in.nextLine();
				}
				
				// get the next id
				// TODO, test with empty DB!!
		        rset = myDB.execute("select max(id) from task");
		        int taskId = 0;
		        if (rset.next())
		        {
		            taskId = rset.getInt("max(id)");
		            // make sure we aren't maxed out on users
		            if (taskId == Integer.MAX_VALUE) {
		            	System.out.println("Error - no more tasks can be added");
		            	return;
		            }
		        } 
		        taskId++;  // use the next higher id
				
				System.out.printf("\nEnter task description >>");
				String newTaskDescription = in.nextLine();
				System.out.printf("\nEnter the frequency in days >>");
				Integer interval = Integer.valueOf(in.nextLine());
				System.out.printf("\nEnter the reward in dollars >>");
				Double reward = Double.valueOf(in.nextLine());
				
				// now select the owner - first get all of them
				// TODO - this should be it's own function since this is the second instance
				List<Person> allUsers = new ArrayList<Person>();

				// get a list of all the users in the DB
		        rset = myDB.execute("select id,name,is_admin from  person");

				//Action on the result
		        while (rset.next())
		        {
		            Integer id = rset.getInt("id");
		        	String nname = rset.getString("name");
		            String admin = rset.getString("is_admin");
					allUsers.add(new Person(id, nname, admin.equalsIgnoreCase("Y")));
					//System.out.println("                         ***DEBUG*** adding " + nname);
		        }

				System.out.println("SELECT USER");
				System.out.printf("-----------\n");
				i = 1; // menu index
				Map<Integer,Integer> indexMap = new HashMap<Integer,Integer>(); // map for 1,2,3 to real DB ids
				for (Person test:allUsers) {
					System.out.printf("%d. %s\n", i, test.getName());
					indexMap.put(i,test.getId());
					i++;
				}
				System.out.println();
				Integer index = BACK_KEY_VALUE;
				// don't allow the back key here
				while (index == BACK_KEY_VALUE) {
					index = promptForNumberOrBack("Enter user number >>", i-1);
				}
				// got the 1,2,3 index, now find the real person
				int person_id = indexMap.get(index);

				// need a last completed date too so we can insert into completed_task
				System.out.printf("\nEnter the last completed time [1970-01-01] >>");
				GregorianCalendar lastCompleted = null;

				do {
					String strDate = in.nextLine();
					// allow CR as substitute for the beginning of time
					if (strDate.equals("")) {
						lastCompleted = new GregorianCalendar(1970,01,01);
					}
					else {
						try {
							// TODO date manipulations
							//lastCompleted = GregorianCalendar(strDate);
						} catch (Exception e) {
							lastCompleted = null;
							System.out.println("invalid format, try again");
						}
					}
				} while (lastCompleted == null);
				// oracle needs DD-MMM-YYYY
				String strLastCompleted = "01-JAN-1970";
			
				
				// whew, lastly add the task!
				myDB.execute("insert into task values (" + taskId + ", '" +
														newTaskName + "', " +
														interval + ", " +
														reward + ", '" +
														newTaskDescription + "', " +
														person_id + ", " +
														taskTypeId + ")");
				
				// we got this far, lastly register it as completed
				myDB.execute("insert into completed_task values(" + taskId + ", null," + 
																person_id + ", " +
																reward + ", null, '" + 
																strLastCompleted + "')");
				
				
				
			}
		}
	}

	public void editTask() {

		System.out.printf("\nSORRY, NOT IMPLEMENTED");
	}

	public static String[][] adminFunctions = {{"List All Users",
												"List All Tasks",
												"Add User",
                                                "Process Payments",
												"Add a Task",
												"Edit a Task - not implemented"},

												{"listUsers",
												 "listTasks",
												 "addUser",
                                                 "processPay",
												 "addTask",
												 "editTask"}};

	private int promptForNumberOrBack(String prompt, int max) {
		System.out.printf(prompt);
		Scanner in = new Scanner(System.in);
		int cmd = BACK_KEY_VALUE;
		while (cmd == BACK_KEY_VALUE) {
			String cmdName = in.nextLine();
			//System.out.println("                         ***DEBUG*** got=[" + cmdName + "]");
			if (cmdName.equals(BACK_KEY)) {
				break;
			}
			else {
				try{
					cmd = Integer.valueOf(cmdName);
				}
				catch (NumberFormatException ex){
					//ignore
				}
				if ((cmd < 1) || (cmd > max)) {
					System.out.printf("bad input - please try again\n>");
					cmd = BACK_KEY_VALUE;
				}
			}
		}
		return cmd;
	}

	private void initUser(final String inputName) throws SQLException  {
		String tempName = inputName;  // make a copy of the name provided
		List<Person> allUsers = new ArrayList<Person>();

		// get a list of all the users in the DB
        ResultSet rset = myDB.execute("select id,name,is_admin from  person");

		//Action on the result
        while (rset.next())
        {
            Integer id = rset.getInt("id");
        	String nname = rset.getString("name");
            String admin = rset.getString("is_admin");
			allUsers.add(new Person(id, nname, admin.equalsIgnoreCase("Y")));
			//System.out.println("                         ***DEBUG*** adding " + nname);
        }

		if (allUsers.isEmpty()) {
			// no users defined, just add the current username, (or 'Admin')
			// to the DB as admin, and use it.
			if (tempName == null) {
				tempName = FIRST_USER_NAME;
				System.out.println("no users found in the db, inserting first user with name=" + FIRST_USER_NAME);
			}
			else {
				System.out.println("no users found in the db, inserting first user with name=" + tempName);
			}
			myDB.execute("insert into person values(FIRST_USER_ID, '" + tempName + "', 'Y')");
			currentUser = new Person(FIRST_ID, tempName, Boolean.TRUE);
			return;
		}

		// if username provided see if it's in the list, otherwise discard
		if (tempName != null) {
			//System.out.println("                         ***DEBUG*** Attempt to find user=" + tempName + " ...");
			// have to iterate through the list
			for (Person test:allUsers) {
				if (test.getName().equalsIgnoreCase(tempName)) {
					currentUser = new Person(test.getId(), test.getName(), test.getAdmin());
					//System.out.println("                         ***DEBUG*** Found!");
				}
			}
			if (currentUser == null) {
				//System.out.println("                         ***DEBUG*** NOT Found");
			}
		}
		if (currentUser == null) {
			//prompt for name
			System.out.println("SELECT USER");
			System.out.printf("-----------\n");
			int i = 1; // menu index
			Map<Integer,String> indexMap = new HashMap<Integer,String>(); // map for 1,2,3 to real DB ids
			for (Person test:allUsers) {
				System.out.printf("%d. %s\n", i, test.getName());
				indexMap.put(i,test.getName());
				i++;
			}
			System.out.println();
			Integer index = BACK_KEY_VALUE;
			// don't allow the back key here
			while (index == BACK_KEY_VALUE) {
				index = promptForNumberOrBack("Enter user number >>", i-1);
			}
			// got the 1,2,3 index, now find the real person
			for (Person test:allUsers) {
				if (test.getName().equals(indexMap.get(index))) {
					currentUser = new Person(test.getId(), test.getName(), test.getAdmin());
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws SQLException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, MalformedURLException {
		if (args.length < 2) {
			System.out.println("Error, Syntax is 'java Fotf [DB USER] [DB PASSWORD]    optional [APPLICATION USER NAME]'");
			System.exit(0);
		}

        String user = args[0];
        String password = args[1];
		String taskOwner = null;
		if (args.length > 2) {
			taskOwner = args[2];
		}
        DB myDB = new OracleDb("csdb.csc.villanova.edu:1521:csdb", user, password);

		Fotf app = new Fotf(myDB);
		System.out.println("\n\n   ***Welcome to the Family Task Tracker, by Focus on the Family***\n");
		app.initUser(taskOwner);
		app.run();
	}
}
