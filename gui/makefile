

PATH  := node_modules/.bin:$(PATH)
SHELL := /bin/bash

MAIN_CSS := app/styles/app.css
SCSS_FILES = $(shell find app/scss -name '*.scss')

.PHONY: ALL watch clean

ALL: watch

$(MAIN_CSS): $(SCSS_FILES)
	mkdir -p $(dir $@)
	node-sass --source-map=true app/scss/app.scss $@

watch: $(MAIN_CSS)
	browser-sync start --files "app/styles/app.css" --server "app"

clean:
	rm -rf $(dir $(MAIN_CSS))

