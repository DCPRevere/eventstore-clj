(ns eventstore-clj.example
  (:require [eventstore-clj.message :as m]
            [eventstore-clj.utils :as u]
            [okku.core :as okku]))


;; Connect
(def objs (m/connect))

(def stream "example-stream")
(def group "example-group")



(.tell (::m/connection-actor objs)
       (m/->CreatePersistentSubscription
        {:eventstore-clj.message.stream/has-id stream
         :eventstore-clj.message.persistent/group group
         :eventstore-clj.message.persistent/settings
         {:eventstore-clj.message.persistent/min-check-point-count 1}})
       (akka.actor.ActorRef/noSender))



(defmulti handle class)

(defmethod handle eventstore.LiveProcessingStarted$ [m]
  (println "Starting..."))

(defmethod handle eventstore.Event [event]
  (println "Event: " event))

(defmethod handle :default [m]
  (println "Error, unhandled: " m))



(def event-handler
  (okku/spawn
   (okku/actor
    (onReceive
     [m] (handle m)))
   :in (::m/actor-system objs)))



(def per-sub-actor
  (okku/spawn
   (eventstore.PersistentSubscriptionActor/props
    (::m/connection-actor objs)
    event-handler
    (m/->EventStream stream)
    group
    (scala.Option/apply (m/->UserCredentials))
    (eventstore.Settings/Default)
    true)
   :in (::m/actor-system objs)))



(def write-result-handler
  (okku/spawn
   (okku/actor
    (onReceive
     [m] (print (str m "\n"))))
   :in (::m/actor-system objs)))



(.tell (::m/connection-actor objs)
       (m/->WriteEvents
        {::m/event-datas [{:eventstore-clj.message.event/type "ExampleType"
                          :eventstore-clj.message.event/id (java.util.UUID/randomUUID)
                          :eventstore-clj.message.event/data
                          {:eventstore-clj.message.event.data/type :eventstore-clj.message.event.data.type/json
                           :eventstore-clj.message.event.data/value "Event body"}}]
         ::m/version :eventstore-clj.message.version/any
         ::m/master? false
         :eventstore-clj.message.stream/has-id (m/stream-of stream)})
       write-result-handler)
