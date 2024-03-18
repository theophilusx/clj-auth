(ns theophilusx.auth.log
  (:require [taoensso.timbre :as timbre]))

(defmacro debug [& args]
  (timbre/keep-callsite `(timbre/log! :debug :p ~args)))

(defmacro error [& args]
  (timbre/keep-callsite `(timbre/log! :error :p ~args)))

(defmacro fatal [& args]
  (timbre/keep-callsite `(timbre/log! :fatal :p ~args)))

(defmacro info [& args]
  (timbre/keep-callsite `(timbre/log! :info :p ~args)))

(defmacro trace [& args]
  (timbre/keep-callsite `(timbre/log! :trace :p ~args)))

(defmacro warn [& args]
  (timbre/keep-callsite `(timbre/log! :warn :p ~args)))

(defn set-min-level [lvl]
  (timbre/set-min-level! lvl))

(defn set-ns-min-level [ns lvl]
  (timbre/set-ns-min-level! ns lvl))

