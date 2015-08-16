# comportex-notebook

**Comportex Notebook is now part of [ComportexViz](https://github.com/nupic-community/comportexviz).**

This `comportex-notebook` repo is no longer relevant, though I'm still accepting stars.

# OLD STUFF

**I made some breaking changes to ComportexViz and this now broken.** I'm now building the next version. You can sync ComportexViz back a couple months if you want to use this now, but I recommend waiting.

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

Change ComportexViz to use a client-server model. The server tells the client, at a high level, what to render. It's like a webserver.

At the end of this work, ComportexViz will not access models directly, except possibly via pluggable servers.

At the end of this work, there will be three pluggable servers.

Terminology: "ComportexViz Runner" is the traditional ComportexViz UI. This is "ComportexViz Notebook". This will soon live inside of ComportexViz, and we'll just refer to "starting a runner" or "starting a notebook".

More work is needed to connect Comportex Notebook to this server (see sections below) so the first task will be to get 1 and 2 working with traditional ComportexViz simulations.

#### Server 1 - Browser-only

This is simply a ComportexViz runner behaving the same as today, but shuffled into this new architecture. Comportex will run in ClojureScript, as it does today.

#### Server 2 - Comportex in the Cloud

Both the notebook and the runner will use this.

ComportexViz is currently `.cljs`-only. This work will add `.clj` files. Consumers will call `(comportexviz.server.launchpad/start-runner model inputs)` to host a runner webpage.

The client and server will communicate via a WebSocket. The overall philosophy is that client is in charge of rendering content, and the server is in charge of specifying it.

When a browser navigates to the webpage, it signs up to receive steps of the simulation. As you click around the UI, it asks the server for what to render with the new selection. Also, the UI currently auto-increments the selection when new steps arrive, so many requests happen without user intervention.

Open question: Is there a good way to specify and show the "world"?

Comportex Notebook will not use a WebSocket, and instead inserts the token into the page alongside the canvas. It will add models to the lookup table inside the render method.

In Comportex Simulator, the server does not store a vector of models. It just stores the current model and a lookup table, and the browser stores a vector of tokens.

#### Server 3 - Static files

Exported notebooks will use this. It's just a layer around static files.

The browser code will make HTTP requests to download the needed files to satisfy UI requests.

The real challenge here is deciding how to exporting all possible server outputs into multiple files such that they can be selectively downloaded.

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
	- Version 1: On save/export, export all possible server responses into static files.

As mentioned above, the first task is to get Comportex Runner running on the server. In other words, I won't be thinking about Gorilla REPL for some time. There's a good chance I'll be smarter after building Client-server ComportexViz, and I wouldn't want to inconvenience @JonyEpsilon with the current, dumber me.

### Export

As stated above: the visualizations need to become more like Google Maps. Don't download every detail about the world. Only download what you need.

This is more difficult with static files. On export, we need to create:

- Blobs of data
- Some sort of lookup table into that data

We shouldn't create large files, but we also shouldn't create a million files.

This is an interesting problem that is independent of Comportex. How do you cleverly serialize a large map into static files? Should the caller and the serialization code "meet in the middle" somewhere, with the caller providing extra context? Or can you do a pretty good job without that? Since this code runs on export, it doesn't have to be fast -- the code could create a tree of blob sizes and decide how to partition the tree.

This will be the final work. Stabilize everything else first. And, to help stabilize everything else, start with a lame version that simply exports all canvases to PNG.
