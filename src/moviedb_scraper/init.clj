(ns moviedb-scraper.init
  (:require [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clj-time.format :as format]
            [clj-time.coerce :as coerce]))

(def conn (nr/connect "http://localhost:7474/db/data/"))

(defn- parse-date [s]
  (->> s
      (format/parse (format/formatters :year-month-day ))
      (coerce/to-long)))

(defn create-movie [data]
  (let [node (nn/create conn {:adult (:adult data)
                           :backdrop_path (:backdrop_path data)
                           :budget (:budget data)
                           :homepage (:homepage data)
                           :original_language (:original_language data)
                           :original_title (:original_title data)
                           :mdb_id (:id data)
                           :imdb_id (:imdb_id data)
                           :overview (:overview data)
                           :popularity (:popularity data)
                           :poster_path (:poster_path data)
                           :release_date (if-let [release-date (:release_date data)]
                                           (parse-date release-date))
                           :revenue (:revenue data)
                           :runtime (:runtime data)
                           :status (:status data)
                           :tagline (:tagline data)
                           :title (:title data)
                           :vote_average (:vote_average data)
                           :vote_count (:vote_count data)})]
      (nl/add conn node :Movie)
      node))
