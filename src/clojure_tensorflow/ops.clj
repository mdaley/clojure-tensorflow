(ns clojure-tensorflow.ops
  (:require
   [clojure-tensorflow.build :as build :refer [op-builder]]
   [clojure-tensorflow.graph :as graph]
   [clojure-tensorflow.utils :as utils]

   [clojure-tensorflow.ops :as tf]))

(defn global-variables-initializer []
  @graph/global-variables)

;; value ops

(defn constant [val]
  (let [tensor (utils/clj->tensor val)]
    (op-builder
     {:operation "Const"
      :attrs {:dtype (.dataType tensor)
              :value tensor
              }})))

(defn assign [var val]
  (op-builder
   {:operation "Assign"
    :inputs [var (if (utils/tf-obj? val) val (constant val))]
    }))

(defn variable
  ([val] (variable val {}))
  ([val bits]
   (let [tensor (utils/clj->tensor val)
         var (op-builder
          (merge
           {:operation "Variable"
            :attrs {:shape (utils/tensor->shape tensor)
                    :dtype (.dataType tensor)}
            } bits))]
     (swap! graph/global-variables conj (assign var val))
     var)))

(defn placeholder [node-name datatype]
  (op-builder
   {:operation "Placeholder"
    :node-name (name node-name)
    :attrs {:dtype datatype}
    }))

;; math ops

(defn mult [a b]
  (op-builder
   {:operation "Mul"
    :inputs [a b]}))

(defn div [a b]
  (op-builder
   {:operation "Div"
    :inputs [a b]}))

(defn add [a b]
  (op-builder
   {:operation "Add"
    :inputs [a b]}))

(defn sub [a b]
  (op-builder
   {:operation "Sub"
    :inputs [a b]}))

(defn sum
  ([t] (sum t (constant 0) false))
  ([t dims] (sum t (constant 0) false))
  ([t dims keep-dims]
   (op-builder
    {:operation "Sum"
     :attrs {:keep_dims keep-dims}
     :inputs [t dims]})))

(defn tanh [a]
  (op-builder
   {:operation "Tanh"
    :inputs [a]}))

(defn sigmoid [a]
  (op-builder
   {:operation "Sigmoid"
    :inputs [a]}))

(defn relu [a]
  (op-builder
   {:operation "Relu"
    :inputs [a]}))

(defn softmax [a]
  (op-builder
   {:operation "Softmax"
    :inputs [a]}))

(defn maximum [a b]
  (op-builder
   {:operation "Maximum"
    :inputs [a b]}))

(defn reduce-max [a]
  (op-builder
   {:operation "Max"
    :inputs [a (constant -1)]}))

(defn minimum [a b]
  (op-builder
   {:operation "Minimum"
    :inputs [a b]}))

(defn gather [params indices]
  (op-builder
   {:operation "Gather"
    :attrs {:validate_indices true}
    :inputs [params indices]}))

(defn slice [input begin size]
  (op-builder
   {:operation "Slice"
    :inputs [input begin size]}))

(defn pad [input paddings]
  (op-builder
   {:operation "Pad"
    :inputs [input paddings]}))

(defn reshape [input shape]
  (op-builder
   {:operation "Reshape"
    :inputs [input shape]}))

(defn concat [tensors axis]
  (op-builder
   {:operation "Concat"
    :inputs [tensors axis]}))

(defn neg [a]
  (op-builder
   {:operation "Neg"
    :inputs [a]}))

(defn pow [a b]
  (op-builder
   {:operation "Pow"
    :inputs [a b]}))

(def square #(pow % (constant 2.)))

(defn log [a]
  (op-builder
   {:operation "Log"
    :inputs [a]}))

(defn size [a]
  (op-builder
   {:operation "Size"
    :inputs [a]}))

(defn abs [a]
  (op-builder
   {:operation "Abs"
    :inputs [a]}))

(defn mean [a]
  (op-builder
   {:operation "Mean"
    :inputs [a (constant 0)]}))

(defn size [a]
  (op-builder
   {:operation "Size"
    :inputs [a]}))

(defn transpose [a]
  (op-builder
   {:operation "Transpose"
    :inputs [a (constant [1 0])]}))

(defn matmul [a b]
  (op-builder
   {:operation "MatMul"
    :inputs [a b]}))

(defn dot-a [a b]
  (op-builder
   {:operation "MatMul"
    :inputs [a b]
    :attrs {:transpose_a true}
    }))

(defn dot-b [a b]
  (op-builder
   {:operation "MatMul"
    :inputs [a b]
    :attrs {:transpose_b true}
    }))

(defn identity [a]
  (op-builder
   {:operation "Identity"
    :inputs [a]}))

(defn unstack
  ([value num axis]
   (op-builder
    {:operation "Unpack"
     :inputs [value]
     :attrs {:num num
             :axis axis}}))
  ([value num] (unstack value num 0)))

(defn stack
  ([value axis]
   (op-builder
    {:operation "Pack"
     :inputs [value]
     :attrs {:axis axis}}
    ))
  ([value] (stack value 0)))

(def float32 org.tensorflow.DataType/FLOAT)
(def int32 org.tensorflow.DataType/INT32)
(def int64 org.tensorflow.DataType/INT64)
(def float64 org.tensorflow.DataType/DOUBLE)


(defn cast [a dtype]
  (op-builder
   {:operation "Cast"
    :inputs [a]
    :attrs {:DstT dtype}
    }))

(def to-float #(cast % float32))
(def to-int32 #(cast % int32))


(defn one-hot
  ([indices depth on-value off-value]
   (op-builder
    {:operation "OneHot"
     :inputs [(to-int32 indices) (to-int32 depth) on-value off-value]}))
  ([indices depth] (one-hot indices depth (constant 1) (constant 0)))
  ([indices] (one-hot indices (add (constant 1) (to-int32 (reduce-max indices))) (constant 1) (constant 0)))
  )

(defn random-normal
  "Generate a tensor of random values with a normal distribution"
  ([shape stddev]
   (let [source (java.util.Random. (rand))]
     ((reduce #(partial repeatedly %2 %1)
              #(.nextGaussian source)
              (reverse shape)))))
  ([shape] (random-normal shape 0.35)))
