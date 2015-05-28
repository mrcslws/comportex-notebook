# comportex-notebook

## Usage

Clone https://github.com/mrcslws/gorilla-repl.git
Use the "numenta2015" branch.
"lein install"

Install comportex.
Install comportexviz.

"lein cljsbuild once"
(or "lein cljsbuild auto")

"lein repl"

In the REPL:
(start-server)

Open the worksheet. Use URL "/worksheet.html?filename=worksheet.clj"

Start evaluating rows.

When you evaluate "model", magic will happen.

## License

Copyright © 2015 I HAVEN'T THOUGHT ABOUT THIS YET

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
