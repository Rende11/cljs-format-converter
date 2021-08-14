(ns converter.app
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [converter.location :as location]
            [converter.panels.model :as model]
            [converter.panels.core :refer [left right]]))

(defn app
  []
  [:div#app.bg-gray-100.min-h-screen.flex.flex-auto.flex-row.justify-between.text-gray-600
   [left]
   [right]])


(defn ^:dev/after-load render []
  (rdom/render [app] (.getElementById js/document "root")))


(rf/reg-event-fx
 ::initialize
 [(rf/inject-cofx ::location/location)]
 (fn [cofx _]
   (let [search (location/parse-search-params (get-in cofx [:location :search]))
         from (or (keyword (get search "from")) :json)
         to (or (keyword (get search "to")) :yaml)]
     (cond-> 
         {:db {:source {:format from 
                        :value (if (= :js from)
                                 "{ message: \"Hello!\",
  data: { one: 1,
              pi: 3.14 },
  items: [ \"Let's start!\", \"Yeah!\" ] }"
                                 (->> (model/generate from  {:message "Hello!"
                                                             :data {:one 1
                                                                    :pi 3.14}
                                                             :items ["Let's start!" "Yeah!"]})
                                      (model/align from)
                                      (.toString)))}
               :output {:format to}}}
       (not search) (assoc :dispatch [::location/redirect {"from" "json" "to" "yaml"}])))))

(defn ^:export init
  []
  (rf/dispatch-sync [::initialize])
  (render))

