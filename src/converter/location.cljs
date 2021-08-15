(ns converter.location
  (:require [clojure.string :as str]
            [re-frame.core :as rf]))

(defn parse-search-params [search]
  (some-> search 
      (str/replace "?" "")
      not-empty
      (str/split #"=|&")
      (->> (apply hash-map))))

(defn generate-search-params [params]
  (->> params
       (map #(str/join "=" %))
       (str/join "&")))

(rf/reg-cofx
 ::location
 (fn [cofx _]
   (assoc cofx :location (let [l (.-location js/document)]
                           {:href (.-href l)
                            :search (.-search l)
                            :search-parsed (parse-search-params (.-search l))
                            :host (.-host l)}))))


(rf/reg-fx
 ::push-params
 (fn [value]
   (.pushState js/history nil nil value)))


(rf/reg-event-fx
 ::redirect-merge
 [(rf/inject-cofx ::location)]
 (fn [{location :location} [_ params]]
   (let [merged-params (merge (:search-parsed location) params)
         search-string (str "?" (generate-search-params merged-params))]
     {::push-params search-string})))

(rf/reg-event-fx
 ::redirect
 (fn [_ [_ params]]
   (let [search-string (str "?" (generate-search-params params))]
     {::push-params search-string})))

(comment

  (rf/dispatch [::redirect {"from" "json" "to" "yaml"}])

  (parse-search-params "")

  (some-> ""
      (str/replace "?" "")
      not-empty
      (str/split #"=|&")
      (->> (apply hash-map)))

  (generate-search-params {"from" "a" "to" "z"})

  )

