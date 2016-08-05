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

(ns eyeota.kani.core.encoding-test
  (:require [clojure.test :refer :all]
            [eyeota.kani.core.encoding :as encoding]))

(deftest test-str->base64
  (is (= "abc blah blah" (encoding/hex->str (encoding/str->hex "abc blah blah"))))
  (is (= "XyZ1235!!!!@$!@$#%" (encoding/hex->str (encoding/str->hex "XyZ1235!!!!@$!@$#%"))))
  (is (= "我很帅气" (encoding/hex->str (encoding/str->hex "我很帅气"))))
  (is (= "私はハンサムです" (encoding/hex->str (encoding/str->hex "私はハンサムです")))))

(deftest test-str->coll-of-str
  (is (= #{"1", "2", "3"} (set (encoding/str->coll-of-str "[1, 2, 3]"))))
  (is (= #{"abc", "def"} (set (encoding/str->coll-of-str "{abc, def}"))))
  (is (= #{"key=123", "key2=777"} (set (encoding/str->coll-of-str "{key=123, key2=777}")))))
