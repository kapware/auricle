(ns auricle.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :speaker
  (fn [db _]
    (:speaker db)))

(reg-sub
 :db
 (fn [db _]
   db))

(reg-sub
 :loading
 (fn [db _]
   (:loading db)))
