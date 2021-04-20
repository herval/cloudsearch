.DEFAULT := all


all: install test build build_all

install:
	go get ./...

build_all:
	GOOS=linux GOARCH=arm go build -o cloudsearch.bin ./cmd
	GOOS=windows GOARCH=386 go build -o cloudsearch.exe ./cmd

build:
	go build -o cloudsearch ./cmd

test:
	go test -v ./...

integration:
	go test -test.v ./cmd/

server: build
	./cloudsearch server -index -debug -httpPort :65432

ui:
	qtmoc desktop . # generate the c++ bindings
	# qtrcc # build resource files?
	# qtdeploy build desktop
	# qtminimal desktop .
	CGO_LDFLAGS_ALLOW='-Wl,-rpath,@executable_path/Frameworks' go build -o ui && ./ui