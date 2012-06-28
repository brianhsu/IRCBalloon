#!/bin/bash
xgettext --from-code=utf-8 -L java -ktrc -ktr -kmarktr -ktrn:1,2 \
         -o po/IRCBalloon.pot src/main/scala/*.scala

msgfmt --java2 -d target/scala-2.9.1/classes/ -r app.i18n.Messages po/*.po
