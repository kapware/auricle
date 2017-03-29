(ns auricle.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [auricle.events]
            [auricle.subs]))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def text-input (r/adapt-react-class (.-TextInput ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))

(def love-img (js/require "./images/love.png"))
(def smile-img (js/require "./images/smile.png"))
(def neutral-img (js/require "./images/neutral.png"))
(def sleep-img (js/require "./images/sleep.png"))

(def icon-for-type
  {:love love-img
   :smile smile-img
   :neutral neutral-img
   :sleep sleep-img})

(defn alert [title]
      (.alert (.-Alert ReactNative) title))

(defn rating-clicked [speaker rating]
  (dispatch [:add-rating speaker rating]))

(defn emoticon [speaker type]
  [touchable-highlight {:on-press #(rating-clicked speaker type)}
   [image {:source (type icon-for-type) :style  {:width 80 :height 80 :margin-bottom 30}}]])

(defn speaker-rating [speaker]
      [view {:style {:flex-direction "column" :margin 40 :align-items "stretch"}}
       [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} speaker]
       [view {:style {:flex-direction "row" :justify-content "space-around"}}
        [emoticon speaker :love]
        [emoticon speaker :smile]
        [emoticon speaker :neutral]
        [emoticon speaker :sleep]]])

(defn new-speaker []
  [view {:style {:flex 0 :flex-direction "column" :margin 40 :align-items "center"}}
   [text "Enter speaker:"]
   [text-input {:onChangeText #(dispatch [:speaker-input-changed %]):auto-focus true :style {:width 100 :height 100 }}]
   [touchable-highlight {:on-press #(dispatch [:speaker-input-accepted])}
    [text "OK"]]])

(defn app-root []
  (let [speaker (subscribe [:speaker])]
    (if-not @speaker
      [new-speaker]
      [speaker-rating @speaker])))

(defn init []
      (dispatch-sync [:initialize-db])
      (.registerComponent app-registry "auricle" #(r/reactify-component app-root)))
