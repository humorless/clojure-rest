# clojure-rest

 It is a clojure backend service demo with sqlite

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## Testing

   1. invoke the web server    
   
      method 1. in leiningen environment => ```lein ring server```
 
      method 2. using jar file to invoke => ```java -jar target/clojure-rest-0.1.0-1-standalone.jar``` 

   2. use python script to generating testing data into Sqlite database

      ./create.py useritems 5 7 10 

   3. testing command

      ```curl http://localhost:3000/items?level=10&job=Barbarian&strength=6&limit=3```
      ```curl http://localhost:3000/users?level=20&job=Barbarian&limit=17```

## checking the SQL part

  * connect sqlite database
 
    sqlite3 db/database.db

  * show tables

    sqlite> .table

  * verity query items
    
    sqlite> SELECT name, (strength + vitality) AS effi FROM items WHERE level <= 10 ORDER BY effi DESC LIMIT 7;

  * verify query users
    
    sqlite> SELECT users.name, users.level, users.job, SUM(items.strength), SUM(items.vitality)
              FROM users INNER JOIN useritems ON users.name = useritems.uname
                         INNER JOIN items     ON items.name = useritems.iname
              WHERE users.level <= 20 AND users.job = 0 GROUP BY users.name LIMIT 5;
