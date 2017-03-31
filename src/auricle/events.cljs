(ns auricle.events
  (:require
   [re-frame.core :as r :refer [reg-event-db after reg-event-fx]]
   [clojure.spec :as s]
   [auricle.db :as db :refer [app-db]]
   [cljs-time.core :as tcore]
   [cljs-time.format :as tformat]
   [cljs-time.coerce :as tcoerce]
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]
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
(defn now []
  (tcoerce/to-long (tcore/now)))

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
   (let [new-db (update-in db [:speakers speaker rating] conj (now))]
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
   (let [speaker (:speaker-input db)]
     (-> db
         (assoc :current-speaker speaker)
         (assoc-in [:speakers speaker] {:name speaker
                                        :created (now)})))))

(reg-event-fx
 :load-data
 validate-spec
 (fn
   [{:keys [db]} [_ key]]
   {:db (assoc db :loading true)
    :async-storage-fx/get-item {:key key
                                :on-success [:load-data-success key]
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

(reg-event-fx
 :save-api-key
 validate-spec
 (fn [{:keys [db]} [_]]
   (let [api-key (:api-key-input db)]
     {:db (assoc db :api-key api-key)
      :dispatch [:save-data :api-key api-key]})))

(reg-event-db
 :api-key-input-changed
 validate-spec
 (fn [db [_ new-name]]
   (assoc db :api-key-input new-name)))

(reg-event-fx
 :export-data
 validate-spec
 (fn
   [{:keys [db]} [_]]
   {:db (assoc db :loading true)
    :http-xhrio {:method :post
                 :uri "https://api.paste.ee/v1/pastes"
                 :timeout 10000
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :params {:key (:api-key db)
                          :description "Auricle export"
                          :sections [{:name "Speakers"
                                      :contents (str (:speakers db))}]}
                 :on-failure [:export-data-failure]
                 :on-success [:export-data-success]}}))


(reg-event-db
 :export-data-failure
 validate-spec
 (fn [db [_ reason]]
   (assoc db :export-fail reason
          :loading false)))


(reg-event-db
 :export-data-success
 validate-spec
 (fn [db [_ response]]
   (assoc db :export-success response
          :loading false)))


