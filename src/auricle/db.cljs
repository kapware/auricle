(ns auricle.db
  (:require [clojure.spec :as s]))

;; spec of app-db
(s/def ::app-db
  (s/keys :req-un []))

;; initial state of app-db
(def app-db {})
