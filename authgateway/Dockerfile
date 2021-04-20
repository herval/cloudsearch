FROM golang:latest

RUN mkdir /app
WORKDIR /app

# cache deps
ENV GO111MODULE=on
COPY go.mod /app
COPY go.sum /app
RUN go mod download

# build the app
ADD . /app/
RUN go build -o main ./cmd/ 

CMD /app/main --config=./services.yaml --httpPort :$PORT
