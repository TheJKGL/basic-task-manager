# Local run guide
1. Navigate to ./basis-task-manager directory and run `docker compose up`
2. Open pgAdmin in localhost:5050. 
   - Use these credentials: 
     - email: admin@admin.com, 
     - password: password
3. Create new db server with "host name/address" = "local_pgdb".
4. Add new table with name "task". 
5. Run TaskManagerApplication.

Now we can use our app with two databases PostgresSQL as primary and H2 as secondary.
We will connect to H2 when PostgresSQL connection failed.