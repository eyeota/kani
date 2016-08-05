(ns eyeota.kani.core.consistency
  (:import (com.datastax.driver.core ConsistencyLevel)))

(def consistency-level
  {:all          ConsistencyLevel/ALL
   :any          ConsistencyLevel/ANY
   :each-quorum  ConsistencyLevel/EACH_QUORUM
   :local-one    ConsistencyLevel/LOCAL_ONE
   :local-quorum ConsistencyLevel/LOCAL_QUORUM
   :local-serial ConsistencyLevel/LOCAL_SERIAL
   :one          ConsistencyLevel/ONE
   :quorum       ConsistencyLevel/QUORUM
   :serial       ConsistencyLevel/SERIAL
   :three        ConsistencyLevel/THREE
   :two          ConsistencyLevel/TWO})
