(ns converter.panels.model
  (:require [re-frame.core :as rf]
            [js-yaml :as yaml]
            [clojure.string :as str]
            [cljs.reader :as reader]
            [converter.location :as location]))

(def formats
  [:json :yaml :edn :js])

(rf/reg-event-fx
 ::update-source-value
 (fn [{db :db} [_ value]]
   {:db (assoc-in db [:source :value] value)}))


(rf/reg-sub
 ::source-value
 (fn [db _]
   (get-in db [:source :value] "")))

(rf/reg-sub
 ::source-format
 (fn [db _]
   (get-in db [:source :format])))

(rf/reg-sub
 ::output-format
 (fn [db _]
   (get-in db [:output :format])))

(defn sanitize-json [value]
  (or (not-empty value) "\"\""))


(defmulti parse (fn [format _] format))

(defmethod parse :json [_ value]
  (-> value
      sanitize-json
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defmethod parse :js [_ value]
  (-> (js/eval (str "new Object(" value ")"))
      (js->clj :keywordize-keys true)))


(defmethod parse :yaml [_ value]
  (-> (.load yaml value)
      (js->clj :keywordize-keys true)))

(defmethod parse :edn [_ value]
  (reader/read-string value))

(defmethod parse :default [_ value] value)


(defmulti generate (fn [format _] format))

(defmethod generate :json [_ value]
  (-> value
      clj->js
      (js/JSON.stringify nil 2)))

(defmethod generate :yaml [_ value]
  (->> value
       clj->js
       (.dump yaml)))

(defmethod generate :edn [_ value] value)
(defmethod generate :default [_ value] value)


(defmulti align (fn [format _] format))
(defmethod align :edn [_ value]
  (with-out-str (cljs.pprint/pprint value)))

(defmethod align :default [_ value]
  value)

(defn try-parse [format value]
  (try
    (parse format value)
    (catch js/Error e
      (prn e)
      "")))

(defn try-generate [format value]
  (try
    (generate format value)
    (catch js/Error e
      (prn e)
      "")))

(defn source->output [value from to]
  (->> value
          (try-parse from)
          (try-generate to)
          (align to)
          str/trim))


(rf/reg-sub
 ::transformed-value
 (fn [db _]
   (let [value (get-in db [:source :value] "")
         from (get-in db [:source :format] :json)
         to (get-in db [:output :format] :json)]
     (source->output value from to))))




(rf/reg-event-fx
 ::change-source-format
 (fn [{db :db} [_ format]]
   {:db (assoc-in db [:source :format] format)
    :dispatch [::location/redirect-merge {"from" (name format)}]}))

(rf/reg-event-fx
 ::change-output-format
 (fn [{db :db} [_ format]]
   {:db (assoc-in db [:output :format] format)
    :dispatch [::location/redirect-merge {"to" (name format)}]}))


(rf/reg-fx
 ::copy-fx
 (fn [value]
   (->
    (js/navigator.clipboard.writeText value)
    (.catch (fn [_] (js/Error. "Copy to clipboard failed"))))))

(rf/reg-event-fx
 ::copy
 (fn [{db :db} _]
   (let [value (get-in db [:source :value] "")
         from (get-in db [:source :format] :json)
         to (get-in db [:output :format] :json)]
     {::copy-fx (source->output value from to)
      :dispatch [::toggle [:copy?]]
      ::debounce [[::toggle [:copy?]] 800]})))

(rf/reg-event-db
 ::toggle
 (fn [db [_ path]]
   (update-in db path not)))

(rf/reg-sub
 ::copy?
 (fn [db _]
   (get-in db [:copy?])))

(rf/reg-fx
 ::debounce
 (fn [[payload timeout]]
   (js/setTimeout rf/dispatch timeout payload)))

