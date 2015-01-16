(ns moviedb-scraper.init-test
  (:require [cheshire.core :as json]
            [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clojurewerkz.neocons.rest.relationships :as nrl]
            [clojurewerkz.neocons.rest.cypher :as cy]
            [clojure.test :refer :all]
            [moviedb-scraper.init :refer :all]))

(defn- destroy-graph []
  (cy/tquery conn "MATCH (n)<-[r]->(x) DELETE r, x")
  (cy/tquery conn "MATCH (n) DELETE n"))

(destroy-graph)

(defn clean-db [f]
  (f)
  (destroy-graph))

(use-fixtures :each clean-db)

(defn- slurp-json [path]
  (-> path slurp (json/parse-string true)))

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
      (is (thrown? Exception (create-node "Person" {}))))))

(deftest create-movie-test
  (let [data (slurp-json "test/data/movie/2.json")
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

(deftest create-person-test
  (let [data (slurp-json "test/data/person/4826.json")
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
      (is (= (nl/get-all-labels conn node) [:Person])))))

(deftest create-company-test
  (let [data (slurp-json "test/data/company/1.json")
        {props :data :as node} (create-company data)]
    (testing "set node properties"
      (is (= (:description props) nil))
      (is (= (:headquarters props) "San Francisco, California"))
      (is (= (:homepage props) "http://www.lucasfilm.com"))
      (is (= (:mdb_id props) 1))
      (is (= (:logo_path props) "/8rUnVMVZjlmQsJ45UGotD0Uznxj.png"))
      (is (= (:name props) "Lucasfilm")))
    (testing "set :Company label"
      (is (= (nl/get-all-labels conn node) [:Company])))))

(deftest create-keyword-test
  (let [data {:id 1721 :name "fight"}
        {props :data :as node} (create-keyword data)]
    (testing "set node properties"
      (is (= (:mdb_id props) 1721))
      (is (= (:name props) "fight")))
    (testing "set :Keyword label"
      (is (= (nl/get-all-labels conn node) [:Keyword])))))

(deftest create-language-test
  (let [data {:iso_639_1 "de" :name "Deutsch"}
        {props :data :as node} (create-language data)]
    (testing "set node properties"
      (is (= (:mdb_id props) "de"))
      (is (= (:name props) "Deutsch")))
    (testing "set :Language label"
      (is (= (nl/get-all-labels conn node) [:Language])))))

(deftest create-country-test
  (let [data {:iso_3166_1 "FI" :name "Finland"}
        {props :data :as node} (create-country data)]
    (testing "set node properties"
      (is (= (:mdb_id props) "FI"))
      (is (= (:name props) "Finland")))
    (testing "set :Country label"
      (is (= (nl/get-all-labels conn node) [:Country])))))

(deftest create-genre-test
  (let [data {:id "18" :name "Drama"}
        {props :data :as node} (create-genre data)]
    (testing "set node properties"
      (is (= (:mdb_id props) "18"))
      (is (= (:name props) "Drama")))
    (testing "set :Country label"
      (is (= (nl/get-all-labels conn node) [:Genre])))))

(defn- get-node-names [pos rels]
  (map (fn [rel]
         (->> (get rel pos)
              (nn/fetch-from conn)
              :data :name)) rels))

(deftest add-movie-test
  (let [data (slurp-json "test/data/movie/2.json")
        movie (add-movie data)]
    (testing "create companies with relations"
      (let [produces-rels (nrl/incoming-for conn movie :types [:PRODUCES])
            companies (get-node-names :start produces-rels)]
        (is (= (count produces-rels) 2))
        (is (= companies ["Villealfa Filmproduction Oy"
                          "Finnish Film Foundation"]))))
    (testing "create genres with relations"
      (let [has-genre-rels (nrl/outgoing-for conn movie :types [:HAS_GENRE])
            genres (get-node-names :end has-genre-rels)]
        (is (= (count has-genre-rels) 2))
        (is (= genres ["Drama"
                       "Foreign"]))))
    (testing "create languages with relations"
      (let [language-spoken-rels (nrl/outgoing-for conn movie :types [:LANGUAGE_SPOKEN])
            languages (get-node-names :end language-spoken-rels)]
        (is (= (count language-spoken-rels) 2))
        (is (= languages ["Deutsch"
                          "suomi"]))))
    (testing "create countries with relations"
      (let [produced-in-rels (nrl/outgoing-for conn movie :types [:PRODUCED_IN])
            countries (get-node-names :end produced-in-rels)]
        (is (= (count produced-in-rels) 1))
        (is (= countries ["Finland"]))))))

(deftest add-credits-test
  (let [credits-data (slurp-json "test/data/movie/2_credits.json")
        movie-data (slurp-json "test/data/movie/2_credits.json")
        movie (create-movie movie-data)]
    (add-credits movie credits-data)
    (testing "create actors with relations"
      (let [acts-in-rels (nrl/incoming-for conn movie :types [:ACTS_IN])
            actors (get-node-names :start acts-in-rels)]
        (is (= (count acts-in-rels) 4))
        (is (= (first actors) "Turo Pajala"))
        (is (= (-> acts-in-rels first :data) {:order 0
                                              :character "Taisto Olavi Kasurinen"}))
        (is (= (nth actors 2) "Matti Pellonpää"))
        (is (= (-> acts-in-rels (nth 2) :data) {:order 2
                                              :character "Mikkonen"}))))
    (testing "create crew members with relations"
      (let [directs-rels (nrl/incoming-for conn movie :types [:DIRECTS])
            writes-rels (nrl/incoming-for conn movie :types [:WRITES])
            does-editing-rels (nrl/incoming-for conn movie :types [:DOES_EDITING])
            does-camera-work-rels (nrl/incoming-for conn movie :types [:DOES_CAMERA_WORK])
            does-art-rels (nrl/incoming-for conn movie :types [:DOES_ART])
            does-costumes-rels (nrl/incoming-for conn movie :types [:DOES_COSTUMES])
            directors (get-node-names :start directs-rels)
            writers (get-node-names :start writes-rels)
            editors (get-node-names :start does-editing-rels)
            camera-operators (get-node-names :start does-camera-work-rels)
            art-designer (get-node-names :start does-art-rels)
            costumes-designer (get-node-names :start does-costumes-rels)]
        (is (= (count directs-rels) 1))
        (is (= directors ["Aki Kaurismäki"]))
        (is (= (-> directs-rels first :data) {:job "Director"}))
        (is (= (count writes-rels) 1))
        (is (= writers ["Aki Kaurismäki"]))
        (is (= (-> writes-rels first :data) {:job "Screenplay"}))
        (is (= (count does-editing-rels) 1))
        (is (= editors ["Raija Talvio"]))
        (is (= (-> does-editing-rels first :data) {:job "Editor"}))
        (is (= (count does-camera-work-rels) 1))
        (is (= camera-operators ["Timo Salminen"]))
        (is (= (-> does-camera-work-rels first :data) {:job "Director of Photography"}))
        (is (= (count does-art-rels) 1))
        (is (= art-designer ["Risto Karhula"]))
        (is (= (-> does-art-rels first :data) {:job "Production Design"}))
        (is (= (count does-costumes-rels) 1))
        (is (= costumes-designer ["Tuula Hilkamo"]))
        (is (= (-> does-costumes-rels first :data) {:job "Costume Design"}))))))
