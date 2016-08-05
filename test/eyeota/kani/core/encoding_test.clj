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
