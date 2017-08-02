(ns auricle.ui
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [cljs-time.format :as tformat]
            [cljs-time.coerce :as tcoerce]
            [auricle.events]
            [auricle.subs]))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def text-input (r/adapt-react-class (.-TextInput ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def list-view (r/adapt-react-class (.-ListView ReactNative)))
(def list-view-ds (.-DataSource (.-ListView ReactNative)))

(defn create-ds [change-fn] (list-view-ds. #js{:rowHasChanged change-fn}))

(def love-img (js/require "./images/love.png"))
(def smile-img (js/require "./images/smile.png"))
(def neutral-img (js/require "./images/neutral.png"))
(def sleep-img (js/require "./images/sleep.png"))

(def icon-for-type
  {:love love-img
   :smile smile-img
   :neutral neutral-img
   :sleep sleep-img})

(defn rating-clicked [speaker rating]
  (dispatch [:add-rating speaker rating]))

(defn emoticon [speaker type]
  [touchable-highlight {:on-press #(rating-clicked speaker type) :underlayColor "#fff" }
   [image {:source (type icon-for-type) :style {:width 80 :height 80 :margin-bottom 30}}]])

(defn speaker-rating [speaker next-time]
  [view {:style {:flex 1 :flex-direction "column" :align-items "stretch"}}
   [view {:style {:flex 1 :flex-direction "column" :align-items "stretch" :justify-content "center"}}
    [text {:style {:font-size 30 :font-weight "100" :margin-top -20 :margin-bottom 20 :text-align "center"}} speaker]
    [view {:style {:flex-direction "row" :justify-content "space-around"}}
     [emoticon speaker :love]
     [emoticon speaker :smile]
     [emoticon speaker :neutral]
     [emoticon speaker :sleep]]]

   (if next-time
   [view {:style {:flex 1 :justify-content "center" :position "absolute" :backgroundColor "#000" :left 0 :right 0 :top 0 :bottom 0 :opacity 0.4 :padding 30}}
    [text {:style {:font-size 150 :font-weight "100" :color "#fff" :text-align "center"}} next-time]
    [text {:style {:font-size 30 :font-weight "100" :color "#fff" :text-align "center"}} "Dziękujemy za oddanie głosu!\nPrzekaż urządzenie następnej osobie."]])])

(def iso-date-formatter (tformat/formatter "yyyy-MM-dd HH:mm:ss"))

(defn speaker-item [props]
  (fn [props]
    (let [row (js->clj props :keywordize-keys true)
          {:keys [name created love smile neutral sleep]} row
          date (tformat/unparse iso-date-formatter (tcoerce/from-long created))]
      [text (str name " " date "\n"
                 "[ love: " (or love 0)
                 " smile: " (or smile 0)
                 " neutral: " (or neutral 0)
                 " sleep: " (or sleep 0) "]")])))

(defn speaker-list [speakers]
  [list-view {:dataSource (.cloneWithRows (create-ds (fn [a b] (= a b))) (clj->js speakers))
              :render-row #(r/as-element [speaker-item %])
              :enableEmptySections true}])

(def RNFS (js/require "react-native-fs"))
(def what-platform (keyword (str (.-OS (.-Platform ReactNative)))))
(def the-relative-file-path "auricle-saved.txt")
(def the-file-path (str (case what-platform ;;maybe could have used .-DocumentDirectoryPath here
                           :android (.-ExternalDirectoryPath RNFS)
                           :ios (.-LibraryDirectoryPath RNFS)) "/" the-relative-file-path))
(defn rnfs-write-to-file [speakers] (.writeFile RNFS the-file-path (str speakers) "utf8"))
(def alert-class (.-Alert ReactNative))
(defn alert-buttons-constructor [input]
  (loop [input input]
    (if (or (string? input) (map? input)) (recur [input])
        (mapv #(loop [input %] (cond (map? input) input
                                     (string? input) {:text input})) input))))
(defn alert
  ([title message buttons]
   (alert title message buttons {} ))
  ([title message buttons options]
   (.alert alert-class (str title) (str message)
           (clj->js (alert-buttons-constructor buttons))
           (clj->js options)))
  )
(defn alert-about-write-failed [e] (alert "Writing to file failed"
                                          (str "There was an error writing to file: " e
                                               "  →the path: " the-file-path)
                                          "Too bad"))
(defn write-to-file-fn [what-then-fn]
  (fn write-to-file [speakers] (-> speakers
                                   rnfs-write-to-file
                                   (.then (what-then-fn))
                                   (.catch #(alert-about-write-failed %)))))
(def esteban-share (js/require "react-native-share"))
(defn share-filepath [] (.open esteban-share
                               (clj->js {:url (str "file://" the-file-path)
                                         ;;maybe setting :title or :subject (not :message) would prevent crashes
                                         })))
(def write-to-file-and-share (write-to-file-fn share-filepath))
(defn share-button [text-on on-press-func speakers]
  [touchable-highlight {:on-press #(on-press-func speakers)}
   [text {:style {:padding 10 :background-color "#999999" :margin-bottom 10}} text-on]])
(defn export-button [speakers] (share-button "Export" write-to-file-and-share speakers))

(defn new-speaker []
  (let [speakers (subscribe [:speakers])]
    (fn []
      [view {:style {:flex 1 :flex-direction "column" :justify-content "space-between" :align-items "stretch" :margin-left 20 :margin-right 20}}
       [text-input {:onChangeText #(dispatch [:speaker-input-changed %])
                    :onSubmitEditing #(dispatch [:speaker-input-accepted])
                    :placeholder "New speaker name"
                    :autoFocus true
                    :autoCorrect false
                    :style {:flex 1}}]
       [speaker-list @speakers]
       [view {:flex 1 :flex-direction "row" :justify-content "center" :align-items "center"}
        [export-button @speakers]]
       ])))

(defn pages []
  (let [current-speaker (subscribe [:current-speaker])
        next-time (subscribe [:next-time])]
    (if-not @current-speaker
      [new-speaker]
      [speaker-rating @current-speaker @next-time])))
