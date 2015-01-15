(ns moviedb-scraper.init-test
  (:require [cheshire.core :as json]
            [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clojurewerkz.neocons.rest.constraints :as nc]
            [clojurewerkz.neocons.rest.index :as ni]
            [clojure.test :refer :all]
            [moviedb-scraper.init :refer :all]))

(deftest create-node-test
  (let [n1 (create-node "Person" {:mdb_id 666 :name "Peter"})
        n2 (create-node "Person" {:mdb_id 666 :name "Carl"})
        n3 (create-node "Person" {:mdb_id 661 :name "Georg"})]
    (testing "create node if there isn't another node with the same id"
      (is (= (-> n1 :data :name) "Peter")))
    (testing "if there is another node with the same id return existing node"
      (is (= (-> n2 :data :name) "Peter")))
    (testing "create another node if the id differs"
      (is (= (-> n3 :data :name) "Georg")))
    (testing "throw error if mdb_id is not defined"
      (is (thrown? Exception (create-node "Person" {}))))
    (nn/destroy conn n1)
    (nn/destroy conn n3)))

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
      (is (= (nl/get-all-labels conn node) [:Movie])))
    (nn/destroy conn node)))

(deftest create-person-test
  (let [data (-> "test/data/person/4826.json" slurp (json/parse-string true))
        {props :data :as node} (create-person data)]
    (testing "set node properties"
      (is (= (:adult props) false))
      (is (= (:alias props) nil))
      (is (= (:biography props) "From Wikipedia, the free encyclopedia\n\nMatti Pellonpää (28 March 1951 in Helsinki – 13 July 1995 in Vaasa) was an award-winning Finnish actor and a musician. He rose to international fame with his roles in both Aki Kaurismäki's and Mika Kaurismäki's films; particularly being a regular in Aki's films, appearing in 18 of them.\n\nHe started his career in 1962 as a radio actor at the Finnish state-owned broadcasting company YLE. He performed as an actor during the 70s in many amateur theatres, at the same time that he studied at the Finnish Theatre Academy, where he completed his studies in the year 1977.\n\nHe was nominated Best Actor by European Film Academy for his role as Rodolfo in La Vie de Boheme and won the Felix at the European Film Awards in 1992. He also starred in Jim Jarmusch's 1991 film Night on Earth.\n\nDescription above from the Wikipedia article Matti Pellonpää, licensed under CC-BY-SA, full list of contributors on Wikipedia." ))
      (is (= (:birthday props) -592185600000))
      (is (= (:deathday props) 805593600000))
      (is (= (:homepage props) ""))
      (is (= (:mdb_id props) 4826))
      (is (= (:name props) "Matti Pellonpää"))
      (is (= (:place_of_birth props) "Helsinki, Finland"))
      (is (= (:popularity props) 2.23756143185208e-32))
      (is (= (:profile_path props) nil)))
    (testing "set :Person label"
      (is (= (nl/get-all-labels conn node) [:Person])))
    (nn/destroy conn node)))

(deftest create-company-test
  (let [data (-> "test/data/company/1.json" slurp (json/parse-string true))
        {props :data :as node} (create-company data)]
    (testing "set node properties"
      (is (= (:description props) nil))
      (is (= (:headquarters props) "San Francisco, California"))
      (is (= (:homepage props) "http://www.lucasfilm.com"))
      (is (= (:mdb_id props) 1))
      (is (= (:logo_path props) "/8rUnVMVZjlmQsJ45UGotD0Uznxj.png"))
      (is (= (:name props) "Lucasfilm")))
    (testing "set :Company label"
      (is (= (nl/get-all-labels conn node) [:Company])))
    (nn/destroy conn node)))

(deftest create-keyword-test
  (let [data {:id 1721 :name "fight"}
        {props :data :as node} (create-keyword data)]
    (testing "set node properties"
      (is (= (:mdb_id props) 1721))
      (is (= (:name props) "fight")))
    (testing "set :Keyword label"
      (is (= (nl/get-all-labels conn node) [:Keyword])))
    (nn/destroy conn node)))
