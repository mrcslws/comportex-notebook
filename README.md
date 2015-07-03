# comportex-notebook

## Usage

Clone https://github.com/mrcslws/gorilla-repl.git
Use the "numenta2015" branch.
`lein install`

Install comportex.
Install comportexviz.
(both via clone + `lein install`)

`lein cljsbuild once`
(or `lein cljsbuild auto`)

`lein repl`

In the REPL:
`(run)`

Open the worksheet. Use URL "/worksheet.html?filename=ws/hackathon-start.clj"

Start evaluating rows by pressing \[shift\] + \[enter\]

## Roadmap

Some more work is needed before this will be ready for prime time. The gaping flaw in the current code is that it dumps entire HTM models into the browser. The server-side serialization and the browser-side decoding often takes multiple seconds, and even longer for large models. And then the output files are megabytes in size -- way too large for a blog post.

The visualizations need to become more like Google Maps. Don't download every detail about the world. Only download what you need.

Here's the work that's needed.

### Client-server ComportexViz

Change ComportexViz to use a client-server model. The client will use a channel-based `get-in` API to retrieve model values.

At the end of this work, ComportexViz will not access models directly, except possibly via pluggable servers.

At the end of this work, there will be three pluggable servers.

Terminology: "Comportex Simulator" is the traditional ComportexViz UI. "ComportexViz" is the visualizations that are shared between Comportex Simulator and "Comportex Notebook". Marcus put 3 seconds of thought into this naming.

More work is needed to connect Comportex Notebook to this server (see sections below) so the first task will be to get 1 and 2 working with traditional ComportexViz simulations.

#### Server 1 - Browser-only

This is simply Comportex Simulator behaving the same as today, but shuffled into this new architecture. Comportex will run in ClojureScript, as it does today.

#### Server 2 - Comportex in the Cloud

Comportex Notebook will use this, as will Comportex Simulator.

ComportexViz is currently `.cljs`-only. This work will add `.clj` files. Consumers will call `(comportexviz/simulate model inputs)` to host a ComportexViz simulation webpage

The server stores a mutable lookup table of HTM models. It exposes an HTTP API for accessing / releasing models. For each model, the client will get two URLs, one for performing `get-in`s on the model and another for releasing the model.

When a browser navigates to the webpage, it signs up to receive tokens for the current and all future models, and the browser releases tokens as it finishes with models. Also, the server will release tokens when a web socket connection is broken.

Comportex Simulator will support a few other HTTP APIs for commands like Run / Pause. It will also use a WebSocket to notify clients of changes. The notifications will be:

   - a new model is available
   - the Run / Pause state has changed

Open question: Is there a good way to specify and show the "world"?

Comportex Notebook will not use a WebSocket, and instead inserts the token into the page alongside the canvas. It will add models to the lookup table inside the render method.

In Comportex Simulator, the server does not store a vector of models. It just stores the current model and a lookup table, and the browser stores a vector of tokens.

Here is some example incomplete untested code to help illustrate what this will look like.

~~~clojure
;; As new models are rendered, assoc them into this map with a unique key.
(def lookups
  (atom {:token1 {}
		 :token2 {}
		 :and-so :on}))

(defroutes http-routes
  (GET "/query" (-> (fn [req]
					  (let [{:keys [token ks]} (:params req)]
						(-> (get @lookups token)
							(get-in ks))))
					ring-params/assoc-query-params
					ring-transit/wrap-transit-response))
  (GET "/release" (-> (fn [req]
						(let [{:keys [token]} (:params req)]
						  (swap! lookups dissoc token)
						  (ring-response/response {:status "ok"})))
					  ring-params/assoc-query-params))

  ;; Comportex Simulator only
  (GET "/play" :TODO)
  (GET "/pause" :TODO))

;; Store sockets with their tokens so that we can release the tokens when a
;; connection is broken.
(def web-sockets
  (atom {:socket1 #{:token1 :token2}
		 :socket2 #{:token3 :token4}}))

(def example-url "http://localhost:24601")

(defn on-new-model [model]
  (let [token :todo
		message {:query-url (str example-url "/query?token=" token)
				 :release-url (str example-url "/release?token=" token)}]
	(swap! lookups assoc token model)

	(doseq [s (keys web-sockets)]
	  ;; TODO put the message on the web socket.
	  (swap! web-sockets update s conj token))))
~~~

#### Server 3 - Static files

Exported notebooks will use this. It's just a layer around static files.

The browser code will make HTTP requests to download the needed files to satisfy `get-in` requests.

The real challenge here is deciding how to export HTM models into multiple files such that they can be selectively downloaded.

### Gorilla REPL integration

I've forked Gorilla REPL and made some hacky changes to it. Via pull requests, custom `worksheet.html`s, and other options that I haven't thought of yet, I should try to get rid of this fork. Or formally fork it. Regardless, using Comportex Notebook shouldn't require you to clone and install my fork of gorilla-repl.

Extension points needed: (more research needed)

- Run a custom js file on page-load.
- Client-side / server-side callbacks when a rendered item is cleared.
  - Release the associated models on the server
  - Remove the React component from the DOM
	- Currently Comportex Notebook leaks every component. This doesn't affect the resulting HTML, it's the just JavaScript objects that are confused.
- Run custom Clojure on startup, or specify custom routes for the gorilla server.
  - Initiate server for "/query", "/release", etc.
  - Alternately, could be custom code on page load, and each page would create its own HTTP server with "/query", "/release", etc.
- Run custom Clojure on page exit
	- Release that browser's associated models.
- Plugins to the "save" process, or add a true "export" command.
	- Version 0.2: On save, convert all canvases to PNGs, perhaps Base64-encoded and inlined as a img data:image/png.
	- Version 1: On save/export, export the models into static files.

As mentioned above, the first task is to get Comportex Simulator running on the server. In other words, I won't be thinking about Gorilla REPL for some time. There's a good chance I'll be smarter after building Client-server ComportexViz, and I wouldn't want to inconvenience @JonyEpsilon with the current, dumber me.

### Export

As stated above: the visualizations need to become more like Google Maps. Don't download every detail about the world. Only download what you need.

This is more difficult with static files. On export, we need to create:

- Blobs of data
- Some sort of lookup table into that data

We shouldn't create large files, but we also shouldn't create a million files.

This is an interesting problem that is independent of Comportex. How do you cleverly serialize a large map into static files? Should the caller and the serialization code "meet in the middle" somewhere, with the caller providing extra context? Or can you do a pretty good job without that? Since this code runs on export, it doesn't have to be fast -- the code could create a tree of blob sizes and decide how to partition the tree.

This will be the final work. Stabilize everything else first. And, to help stabilize everything else, start with a lame version that simply exports all canvases to PNG.
