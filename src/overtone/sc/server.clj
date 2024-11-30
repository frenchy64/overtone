(ns overtone.sc.server
  "An interface to the SuperCollider synthesis server.
  This is at heart an OSC client library for the SuperCollider scsynth
  DSP engine."
  {:author "Jeff Rose"}
  (:require
   [overtone.config.log :as log]
   [overtone.helpers.lib :as lib]
   [overtone.libs.deps :as deps]
   [overtone.libs.event :as event]
   [overtone.osc :as osc]
   [overtone.osc.dyn-vars :as osc-dyn-vars]
   [overtone.sc.dyn-vars :as dyn-vars]
   [overtone.sc.machinery.server.comms :as comms]
   [overtone.sc.machinery.server.connection :as connection])
  (:import
   (java.util.concurrent TimeoutException)))

(defn connection-info
  "Returns connection information regarding the currently connected
  server"
  []
  @connection/connection-info*)

(defn server-opts
  "Returns options for currently connected server (if available)"
  []
  (:opts @connection/connection-info*))

(defn server-connected?
  "Returns true if the server is currently connected"
  []
  (= :connected @connection/connection-status*))

(defn server-connecting?
  "Returns true if the server is connecting"
  []
  (= :connecting @connection/connection-status*))

(defn server-disconnected?
  "Returns true if the server is currently disconnected"
  []
  (= :disconnected @connection/connection-status*))

(defn external-server?
  "Returns true if the server is external"
  []
  (= :external (:connection-type (connection-info))))

(defmacro at
  "Schedule server communication - specify that communication messages
   execute on the server at a specific time in the future:

   ;; control synth foo to change :freq to 150
   ;; one second from now:
   (at (+ (now) 1000) (ctl foo :freq 150))

   Only affects code that communicates with the server using OSC
   messaging i.e. synth triggering and control. All code in the body of
   the at macro is executed immediately. Any OSC messages which are
   triggered as a result of executing the body are not immediately sent
   but are instead captured and then sent in a single OSC bundle with
   the specified timestamp once the body has completed. The server then
   stores these bundles and executes them at the specified time. This
   allows you to schedule the triggering and control of synths for
   specific times.

   The bundling is thread-local, so you don't have to worry about
   accidentally scheduling packets into a bundle started on another
   thread.

   Be careful not to confuse at with apply-at and apply-by which
   directly affect Clojure code.

   Warning, all liveness and 'node blocking when not ready' checks are
   disabled within the context of this macro. This means that it will
   fail silently if a server node you wish to control either has been
   since terminated or not had time to be initialised."
  [time-ms & body]
  `(dyn-vars/with-inactive-modification-error :silent
     (dyn-vars/without-node-blocking
      (let [time-ms# ~time-ms]
        (binding [osc-dyn-vars/*at-time* time-ms#]
          (osc/in-unested-osc-bundle @comms/server-osc-peer* time-ms# (do ~@body)))))))

(def min-at-offset-ms 20)

(defmacro at-offset
  "Schedule server communication - specify that communication messages
   execute on the server at a specific time in the future relative to
   the current time:

   ```
   ;; control synth foo to change :freq to 150 one second from now:
   (at-offset 1000 (ctl foo :freq 150))
   ```

   See `overtone.sc.server/at` for more details."
  [offset-ms & body]
  `(let [at-time# osc-dyn-vars/*at-time*
         offset-ms# ~offset-ms
         immediate?# (and (nil? at-time#)
                          (<= offset-ms# min-at-offset-ms))
         body# #(do ~@body)]
     (if immediate?#
       ;; Callers of `at-offset` do not know whether they are sendng
       ;; events relative to now or some future `*at-time*`.
       ;; In case it's now and the offset is small, <20ms, we don't have enough
       ;; time to schedule messages - they will be late on the server.
       ;; Instead, we send them immediately to achieve (nearly) the intended
       ;; timing with no late warning.
       (dyn-vars/with-inactive-modification-error :silent
         (dyn-vars/without-node-blocking
          (body#)))
       (let [now# (or at-time# (System/currentTimeMillis))
             t# (+ now# offset-ms#)]
         (at t# (body#))))))

(defmacro snd-immediately
  [& body]
  `(osc/without-osc-bundle ~@body))

(defn snd
  "Sends an OSC message to the server. If the message path is a known
  scsynth path, then the types of the arguments will be checked
  according to what scsynth is expecting. Automatically converts any
  args which are longs to ints.

  (snd \"/foo\" 1 2.0 \"eggs\")"
  [path & args]
  (when (server-disconnected?)
    (throw (Exception. "Unable to send messages to a disconnected server. Please boot or connect to a server.")))
  (apply comms/server-snd path args))

(defn recv
  "Register your intent to wait for a message associated with given
  path to be received from the server. Returns a promise that will
  contain the message once it has been received. Does not block
  current thread (this only happens once you try and look inside the
  promise and the reply has not yet been received).

  If an optional matcher-fn is specified, will only deliver the
  promise when the matcher-fn returns true. The matcher-fn should
  accept one arg which is the incoming event info."
  ([path] (recv path nil))
  ([path matcher-fn]
   (when-not (server-connected?)
     (throw (Exception. "Unable to receive messages from a disconnected server. Please boot or connect to a server.")))
   (comms/server-recv path matcher-fn)))

(defn connect-server
  "Connect to an externally running SC audio server listening to port
  on host.  Host defaults to localhost and port defaults to 57110."
  ([]
   (connect-server 57110))
  ([port]
   (connect-server "127.0.0.1" port))
  ([host port]
   (connection/connect host port)
   (deps/wait-until-deps-satisfied :server-ready)
   :happy-hacking))

(def ^:deprecated connect-external-server connect-server)

(defn boot-external-server
  "Boot an external server by starting up an external process and connecting to
  it. Requires SuperCollider to be installed in the standard location for your
  OS."
  ([]
   (boot-external-server (+ (rand-int 50000) 2000)))
  ([port]
   (boot-external-server port {}))
  ([port opts]
   (connection/boot :external port opts)
   :happy-hacking))

(defn boot-server
  "Boot the default server."
  []
  (connection/boot)
  :happy-hacking)

(defn kill-server
  "Shutdown the running server"
  []
  (connection/shutdown-server)
  :server-killed)

(defn- before-ns-unload []
  (kill-server))

(defn external-server-log
  "Print the external server log."
  []
  (doseq [msg @connection/external-server-log*]
    (print msg)))

(defn- parse-status
  "Returns a map representing the server status"
  [_ ugens synths groups loaded avg peak nominal actual]
  {:n-ugens ugens
   :n-synths synths
   :n-groups groups
   :n-loaded-synths loaded
   :avg-cpu avg
   :peak-cpu peak
   :nominal-sample-rate nominal
   :actual-sample-rate actual})

(defn server-status
  "Check the status of the audio server."
  []
  (if (server-connected?)
    (let [p (comms/server-recv "/status.reply")]
      (snd "/status")
      (try
        (apply parse-status (:args (lib/deref! p "attempting to get the server status. Perhaps the server is down?")))
        (catch TimeoutException t
          :timeout)))
    :disconnected))

(def status server-status)

(defn clear-msg-queue
  "Remove any scheduled OSC messages from the run queue."
  []
  (snd "/clearSched"))

(defn stop
  "Stop all running synths and metronomes. This does not remove any
  synths/insts you may have defined, rather it just stops any of them
  that are currently playing. Groups are left unaffected."
  []
  (event/event :reset))

(defn clear
  "Stop all running synths and metronomes. This does not remove any
  synths/insts you may have defined, rather it just stops any of them
  that are currently playing. Subgroups are cleared out and removed."
  []
  (event/event :reset)
  (event/event :clear))

(defn stop-all
  "Stop all running synths and metronomes including those in the safe
  pre and post groups. This does not remove any synths/insts you may
  have defined, rather it just stops any of them that are currently
  playing. Groups are left unaffected"
  []
  (event/event :reset)
  (event/event :reset-safe))

(defn clear-all
  "Stop all running synths and metronomes including those in the safe
  pre and post groups. This does not remove any synths/insts you may
  have defined, rather it just stops any of them that are currently
  playing. Subgroups are cleared out and removed."
  []
  (event/event :reset)
  (event/event :reset-safe)
  (event/event :clear)
  (event/event :clear-safe))

(defn sc-osc-debug-on
  "Log and print out all outgoing OSC messages"
  []
  (reset! comms/osc-debug* true ))

(defn sc-osc-debug-off
  "Turns off OSC debug messages (see sc-osc-debug-on)"
  []
  (reset! comms/osc-debug* false))

(defn sc-debug-on
  "Turn on output from both the Overtone and the audio server."
  []
  (log/set-level! :debug)
  (sc-osc-debug-on)
  (snd "/dumpOSC" 1))

(defn sc-debug-off
  "Turn off debug output from both the Overtone and the audio server."
  []
  (log/set-level! :error)
  (sc-osc-debug-off)
  (snd "/dumpOSC" 0))

(defn ensure-connected!
  "Throws an exception if the server isn't currently connected"
  []
  (when-not (server-connected?)
    (throw (Exception. "Server needs to be connected before you can perform this action."))))

(event/on-sync-event
 [:overtone :osc-msg-received]
 (fn [{{path :path args :args} :msg}]
   (let [poll-path "/overtone/internal/poll/"]
     (when (.startsWith ^java.lang.String path poll-path)
       (println "-->" (.substring ^java.lang.String path (count poll-path)) (nth args 2)))))
 ::handle-incoming-poll-messages)

(def connect-jack-ports #'connection/connect-jack-ports)
