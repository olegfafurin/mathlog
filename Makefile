.PHONY: all compile run pack clean out

KOTLINC=kotlinc
KOTLIN=kotlin
SOURCES = src/mathlog/*
MAINCLASS = mathlog.Second
JAR = out/main.jar
all: compile

run:
	${KOTLIN} -cp ${JAR}  ${MAINCLASS}

pack:
	zip hw0.zip -r Makefile src

clean:
	rm -rf out
	rm -rf tmp

compile: ${SOURCES} out
	${KOTLINC} ${SOURCES} -d ${JAR}

out:
	mkdir -p out
