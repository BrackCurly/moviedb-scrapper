(ns moviedb-scraper.init
  (:require [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clojurewerkz.neocons.rest.relationships :as nrl]
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

(defn create-node [label {id :mdb_id :as props}]
  (if (nil? id) (throw (Exception. "Can't create node without :mdb_id")))
  (let [node (nn/create-unique-in-index conn
                                        label
                                        "mdb-id" id
                                        (clean-map props))]
    (nl/add conn node label)
    node))

(defn create-movie [data]
  (create-node "Movie" {:adult (:adult data)
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
                        :vote_count (:vote_count data)}))

(defn create-person [data]
  (create-node "Person" {:adult (:adult data)
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
                         :profile_path (:profile_path data)}))

(defn create-company [data]
  (create-node "Company" {:description (:description data)
                          :headquarters (:headquarters data)
                          :homepage (:homepage data)
                          :mdb_id (:id data)
                          :logo_path (:logo_path data)
                          :name (:name data)}))

(defn create-language [data]
  (create-node "Language" {:name (:name data)
                           :mdb_id (:iso_639_1 data)}))

(defn create-country [data]
  (create-node "Country" {:name (:name data)
                          :mdb_id (:iso_3166_1 data)}))

(defn create-keyword [data]
  (create-node "Keyword" {:name (:name data)
                          :mdb_id (:id data)}))

(defn create-genre [data]
  (create-node "Genre" {:name (:name data)
                        :mdb_id (:id data)}))

(defn- create-rels
  ([node nodes rel] (create-rels node nodes rel false))
  ([node nodes rel inverse]
     (dorun (for [node-i nodes]
              (if inverse
                (nrl/create conn node-i node rel)
                (nrl/create conn node node-i rel))))))

(defn add-movie [{companies :production_companies
                  countries :production_countries
                  languages :spoken_languages
                  genres :genres
                  :as data}]
  (let [movie-node (create-movie data)
        company-nodes (doall (map create-company companies))
        country-nodes (doall (map create-country countries))
        language-nodes (doall (map create-language languages))
        genre-nodes (doall (map create-genre genres))]
        (create-rels movie-node company-nodes :PRODUCES true)
        (create-rels movie-node country-nodes :PRODUCED_IN)
        (create-rels movie-node language-nodes :LANGUAGE_SPOKEN)
        (create-rels movie-node genre-nodes :HAS_GENRE)
        movie-node))
