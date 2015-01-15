(ns moviedb-scraper.init
  (:require [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clojurewerkz.neocons.rest.constraints :as nc]
            [clj-time.format :as format]
            [clj-time.coerce :as coerce]))

(def conn (nr/connect "http://localhost:7474/db/data/"))

(defn- parse-date [s]
  (->> s
      (format/parse (format/formatters :year-month-day ))
      (coerce/to-long)))

(defn- date-field [s]
  (if (and (not= s "") (not (nil? s)))
    (parse-date s)))

(defn- collection-field [xs]
  (if (empty? xs)
    nil
    xs))

(defn- clean-map [props]
  (->> props
   (remove (fn [[_ v]] (nil? v)))
   (into {})))

(defn constrain? [label property]
  (let [constraints (nc/get-all conn label)]
    (some #(= property
              (-> % :property_keys first keyword)) constraints)))

(defn create-constraint [label property]
  (if-not (constrain? label property)
    (nc/create-unique conn label property)))

(defn create-constraints []
  (create-constraint :Movie :mdb_id)
  (create-constraint :Person :mdb_id)
  (create-constraint :Company :mdb_id)
  (create-constraint :Keyword :mdb_id))

(defn create-movie [data]
  (let [props (clean-map {:adult (:adult data)
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
                          :release_date (-> data :release_date date-field)
                          :revenue (:revenue data)
                          :runtime (:runtime data)
                          :status (:status data)
                          :tagline (:tagline data)
                          :title (:title data)
                          :vote_average (:vote_average data)
                          :vote_count (:vote_count data)})
        node (nn/create conn props)]
      (nl/add conn node :Movie)
      node))

(defn create-person [data]
  (let [props (clean-map {:adult (:adult data)
                          :alias (-> data :also_known_as collection-field)
                          :biography (:biography data)
                          :birthday (-> data :birthday date-field)
                          :deathday (-> data :deathday date-field)
                          :homepage (:homepage data)
                          :mdb_id (:id data)
                          :imdb_id (:imdb_id data)
                          :name (:name data)
                          :place_of_birth (:place_of_birth data)
                          :popularity (:popularity data)
                          :profile_path (:profile_path data)})
        node (nn/create conn props)]
    (nl/add conn node :Person)
    node))

(defn create-company [data]
  (let [props (clean-map {:description (:description data)
                          :headquarters (:headquarters data)
                          :homepage (:homepage data)
                          :mdb_id (:id data)
                          :logo_path (:logo_path data)
                          :name (:name data)})
        node (nn/create conn props)]
    (nl/add conn node :Company)
    node))

(defn create-keyword [data]
  (let [props (clean-map {:name (:name data)
                          :mdb_id (:id data)})
        node (nn/create conn props)]
    (nl/add conn node :Keyword)
    node))
