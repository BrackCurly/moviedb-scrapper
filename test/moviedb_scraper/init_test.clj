(ns moviedb-scraper.init-test
  (:require [cheshire.core :as json]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clojure.test :refer :all]
            [moviedb-scraper.init :refer :all]))

(deftest create-movie-test
  (let [data (-> "test/data/movie/2.json" slurp (json/parse-string true))
        {props :data :as node} (create-movie data)]
    (testing "set node properties"
      (is (= (:adult props) false))
      (is (= (:backdrop_path props) "/8qk8f84gfkfoD3tfBVdG8b4ZOvU.jpg"))
      (is (= (:homepage props) ""))
      (is (= (:original_title props) "Ariel"))
      (is (= (:original_language props) "en"))
      (is (= (:mdb_id props) 2))
      (is (= (:imdb_id props) "tt0094675"))
      (is (= (:overview props) "Taisto Kasurinen is a Finnish coal miner whose father has just committed suicide and who is framed for a crime he did not commit. In jail, he starts to dream about leaving the country and starting a new life. He escapes from prison but things don't go as planned..."))
      (is (= (:popularity props) 0.284497318346696))
      (is (= (:poster_path props) "/w0NzAc4Lv6euPtPAmsdEf0ZCF8C.jpg"))
      (is (= (:release_date props) 593395200000))
      (is (= (:revenue props) 0))
      (is (= (:runtime props) 69))
      (is (= (:status props) "Released"))
      (is (= (:tagline props) ""))
      (is (= (:title props) "Ariel"))
      (is (= (:vote_average props) 6.5))
      (is (= (:vote_count props) 5)))
    (testing "set :Movie label"
      (is (= (nl/get-all-labels conn node) [:Movie])))))
