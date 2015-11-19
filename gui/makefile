

PATH  := node_modules/.bin:$(PATH)
SHELL := /bin/bash

MAIN_CSS := app/styles/app.css
SCSS_FILES = $(shell find app/scss -name '*.scss')

DEPS = $(SCSS_FILES)

.PHONY: ALL watch clean

ALL: $(MAIN_CSS)

$(MAIN_CSS): $(SCSS_FILES)
	mkdir -p $(dir $@)
	node-sass --source-map=true app/scss/app.scss $@

watch: $(ALL)
	browser-sync start --browser "Google Chrome" --files "app/styles/app.css, app/*.html" --server "app" &
	watch make app

clean:
	rm -rf $(dir $(MAIN_CSS))

