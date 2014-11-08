(ns retrospector.stars)

;; Based on input data translate to colors
;; Max and min calculated using the data in stars.speck

(defn scale-to-255
  "Scale to a number between 1 and 255 based on the upwards and lower bounds"
  [n max-n min-n]
  (js/parseInt (/ (* (- n min-n) (- 255 1)) (+ (- max-n min-n) 1))))

(defn luminosity->blue
  "Sets the blue level"
  [lum]
  (scale-to-255 lum 106732.27344 0.00016))

(defn velocity->red
  [vel]
  (scale-to-255 vel 96.942 -1000.125))

(defn density->green
  [count]
  (scale-to-255 count 10000 0))
