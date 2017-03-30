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

(reg-sub
 :speakers
 (fn [db _]
   (-> db
       :speakers
       vals
       (or {}))))
