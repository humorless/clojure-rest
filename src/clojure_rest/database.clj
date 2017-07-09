(ns clojure-rest.database
  (:require [clojure.java.jdbc :as j]
            [clojure.tools.logging :as log]))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})

(defn create_items_table []
  (try (j/db-do-commands db
                         (j/create-table-ddl :items
                                             [[:name :integer :primary :key]
                                              [:level :integer]
                                              [:strength :integer]
                                              [:dexterity :integer]
                                              [:intelligence :integer]
                                              [:vitality :integer]]))
       (catch Exception e (println e))))

(defn create_users_table []
  (try (j/db-do-commands db
                         (j/create-table-ddl :users
                                             [[:name :integer :primary :key]
                                              [:level :integer]
                                              [:job :integer]]))
       (catch Exception e (println e))))

(defn create_useritems_table []
  (try (j/db-do-commands db
                         (j/create-table-ddl :useritems
                                             [[:rel :integer :primary :key]
                                              [:uname :integer]
                                              [:iname :integer]]))
       (catch Exception e (println e))))

(defn dump []
  (j/query db ["select * from items"]))

(defn init []
  (create_items_table)
  (create_users_table)
  (create_useritems_table))
  ;(j/insert-multi! db :items items_data))

(defn where_builder
  "Remove the kv with nil values.
   Form the where part of SQL statement as \" i <= ? \".
   Convert the types of values part to integers. "
  [record]
  (let [coll (into {} (filter second record))]
    (cons (str " WHERE "
               (clojure.string/join
                " AND "
                (map #(str (name %) " <= ?") (keys coll))))
          (map read-string (vals coll)))))

(defn items_query_builder
  "Return the vector of sql statement. 
   The vector consists of prepared statement and values."
  [[x y] q]
  (let [select_part (format "SELECT name, (%s + %s) AS effi FROM items " x y)
        where (select-keys q [:level :strength :dexterity :intelligence :vitality])
        order_part " ORDER BY effi DESC "]
    (let [where_part (first (where_builder where))
          where_v    (rest (where_builder where))]
      (if-let [limit (:limit q)]
        `[~(str select_part where_part order_part " LIMIT ? ") ~@where_v ~(read-string limit)]
        `[~(str select_part where_part order_part) ~@where_v]))))

(def role {"barbarian" ["strength"     "vitality"]
           "mage"      ["intelligence" "vitality"]
           "hunter"    ["dexterity"    "vitality"]})

(def jobid {"barbarian" 0
            "mage"      1
            "hunter"    2})

(defn query_items [q]
  (if-let [order (role (clojure.string/lower-case (:job q)))]
    (let [query_result (items_query_builder order q)]
      (log/info query_result)
      (vec (j/query db query_result {:row-fn :name})))
    "input job error, job must be Barbarian, Mage, Hunter"))

(defn users_query_builder
  "doc"
  [[x y] job q]
  (let [front_part (format "SELECT users.name, users.level, users.job, SUM(%s), SUM(%s)
                         FROM users INNER JOIN useritems ON users.name = useritems.uname 
                                    INNER JOIN items     ON items.name = useritems.iname 
                         WHERE users.level <= ? AND users.job = ? GROUP BY users.name" x y)]
    (if-let [limit (:limit q)]
      [(str front_part " LIMIT ? ") (read-string (:level q)) job (read-string (:limit q))]
      [front_part (read-string (:level q)) job])))

(defn point
  "input -> {:name 1, :level 9, :job 2, :sum(dexterity) 7, :sum(vitality) 0}
   output -> {:name 1, :point (+ (* 9 6)  7 0) }"
  [row]
  (let [e (dissoc row :name :level :job)]
    (let [pv (+ (apply + (vals e)) (* 6 (:level row)))
          nv (:name row)]
      {:name nv :point pv})))

(defn query_users [q]
  (if-let [order (role (clojure.string/lower-case (:job q)))]
    (let [job (jobid (clojure.string/lower-case (:job q)))]
      (let [query_result (users_query_builder order job q)]
        (log/info query_result)
        (let [sort_result (sort-by :point > (j/query db query_result {:row-fn point}))]
          (log/info sort_result)
          (vec (map :name sort_result)))))
    "input job error, job must be Barbarian, Mage, Hunter"))
