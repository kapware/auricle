(ns auricle.async-storage-fx
  (:require [re-frame.core :as r]
            [glittershark.core-async-storage :refer [get-item set-item]]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(r/reg-fx
 :async-storage-fx/set-item
 (fn [{:keys [key value on-success on-failure]}]
   (go
     (->> (<! (set-item key value))
          (last)
          (conj on-success)
          (r/dispatch)))))

(r/reg-fx
 :async-storage-fx/get-item
 (fn [{:keys [key on-success on-failure]}]
   (go
     (->> (<! (get-item key))
         (last)
         (conj on-success)
         (r/dispatch)))))
