(ns auricle.events
  (:require
   [re-frame.core :as r :refer [reg-event-db after reg-event-fx]]
   [clojure.spec :as s]
   [auricle.db :as db :refer [app-db]]
   [auricle.async-storage-fx :as async-storage-fx]))


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

(reg-event-fx
 :initialize-db
 validate-spec
 (fn [{:keys [db]} [_]]
   {:db app-db
    :dispatch [:load-data :speakers]}))

(reg-event-fx
 :add-rating
 validate-spec
 (fn [{:keys [db]} [_ speaker rating]]
   (let [new-db (update-in db [:speakers speaker rating] inc)]
     {:db new-db
      :dispatch [:save-data :speakers (:speakers new-db)]})))

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

(reg-event-fx
 :load-data
 validate-spec
 (fn
   [{:keys [db]} [_ key]]
   {:db (assoc db :loading true)
    :async-storage-fx/get-item {:key key
                                :on-success [:load-data-success :speakers]
                                :on-failure [:load-data-failure]}}))

(reg-event-db
 :load-data-success
 validate-spec
 (fn [db [_  key result]]
   (assoc db key result
          :loading false)))

(reg-event-db
 :load-data-failure
 validate-spec
 (fn [db [_ reason]]
   (assoc db :fail reason)))

(reg-event-fx
 :save-data
 validate-spec
 (fn
   [{:keys [db]} [_ key value]]
   {:db db
    :async-storage-fx/set-item {:key key
                                :value value
                                :on-success [:save-data-success]
                                :on-failure [:save-data-failure]}}))

(reg-event-db
 :save-data-success
 validate-spec
 (fn [db [_ val]]
   (assoc db :loading false)))

(reg-event-db
 :save-data-failure
 validate-spec
 (fn [db [_ reason]]
   (assoc db :fail reason)))
