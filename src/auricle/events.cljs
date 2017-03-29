(ns auricle.events
  (:require
   [re-frame.core :refer [reg-event-db after]]
   [clojure.spec :as s]
   [auricle.db :as db :refer [app-db]]))

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
 :initialize-db
 validate-spec
 (fn [_ _]
   app-db))

(reg-event-db
 :add-rating
 validate-spec
 (fn [db [_ speaker rating]]
   (update-in db [speaker rating] inc)))


(reg-event-db
 :speaker-input-changed
 validate-spec
 (fn [db [_ new-name]]
   (assoc db :speaker-input new-name)))

(reg-event-db
 :speaker-input-accepted
 validate-spec
 (fn [db [_]]
   (assoc db :speaker (:speaker-input db))))
