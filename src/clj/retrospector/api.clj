(ns retrospector.api
  (:require [cheshire.core :as json]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as st]))

(def star-data-mapping
  ["map-x"
   "map-y"
   "x"
   "y"
   "z"
   "colorb_v"
   "lum"
   "absmag"
   "appmag"
   "texnum"
   "distly"
   "dcalc"
   "plx"
   "plxerr"
   "vx"
   "vy"
   "vz"
   "speed"
   "hipnum"])

(defn limit [n ceil]
  (if (> n ceil)
    ceil
    n))

(defn scale-to-range
  "Scale to a number between target-max and target-min based on the upwards 
   and lower bounds"
  [n max min target-max target-min]
  (limit (float (/ (* (- n min) (- target-max target-min))
                   (+ (- max min) target-min)))
         target-max))

(def file-location "./resources/public/stars")

(defn sort-stars
  "Sort stars by calling out to the shell. Dear god."
  [field limit & [offset]]
  (let [offset (if (or (nil? offset) (= 0 offset)) 1 offset)
        command (format "sed -n %s,%sp %s_%s.txt"
                        offset
                        (+ offset limit)
                        file-location field)]
    (map #(map read-string (take 17 (st/split (st/trim %) #"\s+")))
         (st/split (:out (sh "bash" "-c" command)) #"\n"))))

(defn aggregate-stars
  "Return the aggregation of each star in the coll

   Example star-coll:
   [norm-x norm-y [all-fields...]]
   "
  [star-coll]
  (let [total (count star-coll)
        agg (if (empty? star-coll)
              []
              (apply map + star-coll))]
    (assoc (into {} (map vector star-data-mapping agg)) :total total)))

(defn safe-divide [n1 n2]
  (try (/ n1 n2) (catch Exception e 0)))

(defn gen-cell-colors
  "Generate the cell color for a grid based on all aggregate star data
   for the cell
   Rules:
   - Luminsity -> blue
   - Velocity -> red
   - Concentration -> green

   Returns a grid where each value is an rgb hexicode value from 1 to 255
   for each r, g, b

   Example input:
   [
     [
       {
          field: 0.3,
       {
          field: 1.2
       },
       ...
     ]
     ...
   ]
   "
  [grid]
  ;; Scale based on the max and min of the data set for each
  ;; attribute used for determining color
  (for [row grid]
    (for [cell row]
      (let [star-count (:total cell 0)
            r (scale-to-range (safe-divide (get cell "speed" 0) star-count)
                              1000 -1000.125 255 0)
            g (scale-to-range star-count 10 0 255 0)
            b (scale-to-range (safe-divide (get cell "lum" 0) star-count)
                              50 0 255 0)]
        (assert (<= r 255))
        (assert (<= g 255))
        (assert (<= b 255))
        (if (> (:total cell) 0) 
          (str "#"
             (format "%02X" (int r))
             (format "%02X" (int g))
             (format "%02X" (int b)))
          "#000000")))))

(def dimension->indx
  {"x" 0
   "y" 1
   "z" 2})

(defn make-grid [w h dx dy]
  (for [x (range 0 w dx)]
    (for [y (range 0 h dy)]
      [x y])))

(defn within-grid?
  "Check if point falls within the bounds of the grid"
  [x1 y1 x2 y2 dx dy]
  (and (< x1 x2) (< x2 (+ x1 dx))
       (< y1 y2) (< y2 (+ y1 dy))))

(defn filter-within-cell [cell star-coords]
  (filter #(within-grid? (nth cell 0) (nth cell 1)
                         (nth % 0) (nth % 1)
                         ;; TODO set these as globals somewhere
                         (/ 1 (* 8 4)) (/ 1 (* 5 4)))
          star-coords))

(defn segment-stars
  "Separate stars into a coordinate based on the grid dimension specified
   for x and y"
  ;; WARNING you need to pass in which axis to use for x an y here
  [star-coll dimension-x dimension-y]
  (let [dimension-indices (map dimension->indx [dimension-x dimension-y])
        ;; Get all coords for x and y
        ;; Returns a seq of x, y coords
        star-coords (for [s star-coll] (map #(nth s %) dimension-indices))
        ;; Translate star-coords to scale of 0 to 1
        all-x (map #(nth % 0) star-coords)
        all-y (map #(nth % 1) star-coords)
        
        ;; TODO ughhhhhh this
        max-x (apply max all-x)
        min-x (apply min all-x)
        max-y (apply max all-y)
        min-y (apply min all-y)

        ;; And this
        normalized-x (map #(scale-to-range % max-x min-x 1 0) all-x)
        normalized-y (map #(scale-to-range % max-y min-y 1 0) all-y)
        ;; Contains the normalized values and a vector of the original
        ;; star values
        normalized-stars (map (comp flatten vector) normalized-x normalized-y star-coll)
        grid (make-grid 1 1 (/ 1 (* 8 4)) (/ 1 (* 5 4)))]
    ;; Iterate through each row in the grid an get all the stars that
    ;; are within the bounds of each cell
    (map (fn [row]
           (for [cell row]
             (filter-within-cell cell normalized-stars)))
         grid)))


(defn stars-handler [request]
  (let [params (:query-params request)
        stars (sort-stars (get params "field")
                          (read-string (get params "limit" "100"))
                          (read-string (get params "offset" "1")))
        grid-with-stars (segment-stars stars "x" "y")
        grid-aggregates (for [row grid-with-stars]
                          (for [cell row]
                            (aggregate-stars cell)))]
    (json/generate-string (gen-cell-colors grid-aggregates))))
