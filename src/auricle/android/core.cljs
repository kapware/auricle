(ns auricle.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch-sync]]
            [auricle.subs]
            [auricle.ui :as ui]))

(defn app-root []
  (let [loading (subscribe [:loading])]
    (if @loading
      [ui/text "Loading..."]
      [ui/pages])))

(defn init []
      (dispatch-sync [:initialize-db])
      (.registerComponent ui/app-registry "auricle" #(r/reactify-component app-root)))
