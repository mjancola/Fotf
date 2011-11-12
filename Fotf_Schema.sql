/*   CSC8490 
 *   Project Phase 05
 *   Group FotF
 *   28 SEPT 2011   
 *   Version 1
 */



DROP TABLE COMPLETED_TASK;
DROP TABLE TASK;
DROP TABLE PERSON;
DROP TABLE TASK_TYPE;

/* 
 * Task types are assigned to a task and must have at least the following:
 *   - id
 *   - unique name
 * 
 */
CREATE TABLE TASK_TYPE (
Id INTEGER NOT NULL,
Name VARCHAR(30) UNIQUE NOT NULL,
Description VARCHAR(60),
PRIMARY KEY(Id) 
);

/* 
 * Each person must have the following:
 *   - id
 *   - unique name
 *   - is_admin flag, Y or N only
 */
CREATE TABLE PERSON (
Id INTEGER NOT NULL,
Name VARCHAR(30) UNIQUE NOT NULL,
Is_Admin CHAR(1) NOT NULL CHECK (Is_Admin IN ('Y','N')),
PRIMARY KEY(Id)
);

/* 
 * Task_Type_Id  and Person_Id are Foreign keys
 * to ensure each TASK only references valid types and persons
 *
 * A Task is required to have the following when created:
 *   - id  
 *   - unique name
 *   - interval: frequency in days which task should be completed
 *   - reward: amount someone should be paid for completing this task one time (could be 0.0)
 *   - person_id: the person who is responsible for doing the task
 *   - task_type_id: type as defined in type table
 */
CREATE TABLE TASK (
Id INTEGER NOT NULL,
Name VARCHAR(30) UNIQUE NOT NULL,
Interval INTEGER NOT NULL,
Reward DECIMAL(5,2) NOT NULL,
Description VARCHAR(60),
Person_Id INTEGER NOT NULL,
Task_Type_Id INTEGER NOT NULL,
PRIMARY KEY(Id),
FOREIGN KEY(Task_Type_Id) REFERENCES TASK_TYPE(Id),
FOREIGN KEY(Person_Id) REFERENCES PERSON(Id)
);


/* 
 * Task_Id is a Foreign key to ensure only valid tasks may be COMPLETED_TASKS
 * Payer_Id and Payee_Id are foreign keys to ensure valid persons are referenced
 * 
 * When a task is completed, a completed_task entry is required with the following:
 *   - task_id
 *   - payee_id: the person who completed the task
 *   - amount: the task reward at the time the task is completed (could be 0.0)
 *   - completed_date
 */
CREATE TABLE COMPLETED_TASK (
Task_Id INTEGER NOT NULL,
Payer_Id INTEGER,
Payee_Id INTEGER NOT NULL,
Amount DECIMAL(5,2) NOT NULL,
Date_Paid DATE,
Completed_Date DATE NOT NULL,
PRIMARY KEY(Completed_Date,Task_Id),
FOREIGN KEY(Task_Id) REFERENCES TASK(Id),
FOREIGN KEY(Payer_Id) REFERENCES PERSON(Id),
FOREIGN KEY(Payee_Id) REFERENCES PERSON(Id)
);

GRANT ALL ON mjancola.COMPLETED_TASK TO CCHESTNU;
GRANT ALL ON mjancola.COMPLETED_TASK TO DSIVIERI;
GRANT ALL ON mjancola.COMPLETED_TASK TO GOELMAN;
GRANT ALL ON mjancola.COMPLETED_TASK TO RGAGRANI;
GRANT ALL ON mjancola.TASK TO CCHESTNU;
GRANT ALL ON mjancola.TASK TO DSIVIERI;
GRANT ALL ON mjancola.TASK TO GOELMAN;
GRANT ALL ON mjancola.TASK TO RGAGRANI;
GRANT ALL ON mjancola.PERSON TO CCHESTNU;
GRANT ALL ON mjancola.PERSON TO DSIVIERI;
GRANT ALL ON mjancola.PERSON TO GOELMAN;
GRANT ALL ON mjancola.PERSON TO RGAGRANI;
GRANT ALL ON mjancola.TASK_TYPE TO CCHESTNU;
GRANT ALL ON mjancola.TASK_TYPE TO DSIVIERI;
GRANT ALL ON mjancola.TASK_TYPE TO GOELMAN;
GRANT ALL ON mjancola.TASK_TYPE TO RGAGRANI;
