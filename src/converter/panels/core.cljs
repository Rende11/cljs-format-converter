(ns converter.panels.core
  (:require [re-frame.core :as rf]
            [converter.panels.model :as model]
            [clojure.string :as str]))
(def copy-icon
  [:svg
   {:stroke "currentColor",
    :viewBox "0 0 24 24",
    :fill "none",
    :class "h-6 w-6"}
   [:path
    {:d "M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z",
     :strokeWidth "{2}",
     :strokeLinejoin "round",
     :strokeLinecap "round"}]])

(defn left []
  (let [value (rf/subscribe [::model/source-value])
        err (rf/subscribe [::model/error])]
    [:div.wrapper.m-5.rounded.shadow.flex.flex-col {:class "w-1/2"}
     [:div.area-header.bg-blue-50.p-2.flex.items-baseline
      [:div.header-name.px-2.mx-1.font-bold "Source"]
      [:div.header-tabs.flex.ml-auto
       (let [current-format @(rf/subscribe [::model/source-format])]
         (doall
          (for [f model/formats]
            [:button.tab.py-1.px-2.mx-1.cursor-pointer.border.bg-white
             {:key f
              :class
              (str "transition hover:border-blue-300 hover:text-blue-400
                  active:text-blue-500 active:border-blue-400 active:shadow-md"
                   (when (= current-format f)
                     " active text-blue-400"))
              :on-click #(rf/dispatch [::model/change-source-format f])}
             (str/upper-case (name f))])))]]
     [:textarea.w-full.resize-none.outline-none.rounded-b.p-3.px-6.flex-grow
      {:on-change #(rf/dispatch [::model/update-source-value (-> % .-target .-value)])
       :value (.toString @value)}]
     (when-let [err-msg @err]
       [:div.err.px-2.py-1.transition-all err-msg])]))


(defn right []
  (let [value (rf/subscribe [::model/output-value])
        copy? (rf/subscribe [::model/copy?])]
    [:div.wrapper.m-5.rounded.shadow.flex.flex-col.relative {:class "w-1/2"}
     [:div.area-header.bg-blue-50.p-2.flex.items-baseline
      [:div.header-name.px-2.mx-1.font-bold "Output"]
      [:div.header-tabs.flex.ml-auto
       (let [output-format @(rf/subscribe [::model/output-format])]
         [:<>
          (doall
           (for [f (remove #{:js} model/formats)]
             [:button.tab.py-1.px-2.mx-1.cursor-pointer.border.bg-white
              {:key f
               :class
               (str "transition hover:border-blue-300 hover:text-blue-400
                  active:text-blue-500 active:border-blue-400 active:shadow-md"
                    (when (= output-format f)
                      " active text-blue-400"))
               :on-click #(rf/dispatch [::model/change-output-format f])}
              (str/upper-case (name f))]))
          [:button.tab.copy-btn.py-1.px-2.mx-1.cursor-pointer.border.bg-white.transition
           {:key "icon-id"
            :class (str "hover:border-blue-300 hover:text-blue-400
                    active:text-blue-500 active:border-blue-400 active:shadow-md"
                    (when @copy?
                      " pointer-events-none text-green-400 border-green-400"))
            :on-click (fn [e]
                        (rf/dispatch [::model/copy e]))}
           (if @copy?
             "Copied!"
             copy-icon)]])]]
     [:textarea.w-full.resize-none.outline-none.rounded-b.p-3.px-6.flex-grow
      {:value (.toString @value)
       :readOnly true}]]))

