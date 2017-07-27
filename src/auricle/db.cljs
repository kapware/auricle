(ns auricle.db
  (:require [cljs.spec.alpha :as s]))

;; spec of app-db
(s/def ::app-db
  (s/keys :req-un []))

;; initial state of app-db
(def app-db {:loading true})
