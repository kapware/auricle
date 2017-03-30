(ns auricle.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :current-speaker
  (fn [db _]
    (:current-speaker db)))

(reg-sub
 :db
 (fn [db _]
   db))

(reg-sub
 :loading
 (fn [db _]
   (:loading db)))

(defn format-speaker [speaker]
  (-> speaker
      (update :love count)
      (update :smile count)
      (update :neutral count)
      (update :sleep count)))

(defn format-speakers [speakers]
  (map format-speaker speakers))

(reg-sub
 :speakers
 (fn [db _]
   (-> db
       :speakers
       vals
       (or {})
       (format-speakers))))
