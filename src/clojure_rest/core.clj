(ns clojure-rest.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clojure-rest.database :as db]))

(defn output
  "Do output formatting"
  [coll]
  (format "[%s]"
          (clojure.string/join ", " coll)))

(defroutes app-routes
  (GET "/" []
    (db/dump))

  (GET "/items" [level job limit strength dexterity intelligence vitality]
    (let [q {:level level
             :job job
             :limit limit
             :strength strength
             :dexterity dexterity
             :intelligence intelligence
             :vitality vitality}]
      (cond
        (nil? level) "input error, level is required."
        (nil? job) "input error, job is required."
        :else (output (db/query_items q)))))

  (GET "/users" [level job limit]
    (let [q {:level level :job job :limit limit}]
      (cond
        (nil? level) "input error, level is required."
        (nil? job) "input error, job is required."
        :else (output (db/query_users q)))))

  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
