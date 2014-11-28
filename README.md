# Retrospector

An experimental star viewer that allows you to step through the galactic starfield in a hyper abstract view. Information about stars is aggregated and encoded into the placement and color of the grid. Moving in 3 dimensions and viewing in 2 dimensions allows you to maintain an objective macro view so you can take the all that pixel glory in!

Demo from Hack the Universe presentation: http://youtu.be/FcbuDtVyzdA?list=UUIuhq9LTleLC-GMdAOvvZcg

## Usage

```
lein deps
lein repl

# In the repl
>> (in-ns 'retrospector.server)
>> (restart-server!)

# In your browser go to 127.0.0.1:9000/app
>> (in-ns 'retrospector.app)
>> (restart-app!)

# Load a slice of the galaxy!
>> (.send goog.net.XhrIo "http://127.0.0.1:9000/api/v1/stars?field=y&offset=0&limit=1000" load-colors-callback)
```

## License

Copyright © 2014 Alex Kehayias

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
