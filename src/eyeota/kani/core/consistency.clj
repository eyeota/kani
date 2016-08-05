;; Copyright (C) 2016 Eyeota

;; This file is part of kani.

;; kani is free software:  you can redistribute it and/or modify
;; it under the terms of the GNU Lesser General Public License as published by
;; the Free Software Foundation, version 3 of the License.

;; kani is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Lesser General Public License for more details.

;; You should have received a copy of the GNU Lesser General Public License
;; along with kani.  If not, see <http://www.gnu.org/licenses/>.

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
