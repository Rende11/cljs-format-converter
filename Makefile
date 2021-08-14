.PHONY: test dev

install:
	npm install
dev:
	npx shadow-cljs watch app

release:
	npx shadow-cljs release app

clear:
	rm -rf public/js/cljs-runtime

test-compile:
	shadow-cljs compile test

test-run:
	node out/node-tests.js

test:
	make test-compile
	make test-run

tw-build:
	npx tailwind build public/css/tw.css -o public/css/styles.css
