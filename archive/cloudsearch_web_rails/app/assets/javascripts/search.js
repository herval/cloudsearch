$(document).on('ready page:load', function() {

    var QueryHandler = function(query) {
        var socket;

        this.open = function(messageCallback) {
            socket = new WebSocket($("body").data("search-server") + "/search/interactive", []);

            socket.onopen = function() {
                socket.send(JSON.stringify({
                    command: "search",
                    token: $("body").data("token"),
                    query: query
                }));
            };

            socket.onmessage = function(response) {
                console.log(response);
                messageCallback(JSON.parse(response.data))
            };

            socket.onerror = function(err) {
                console.log(err);
                alert("Couldn't connect to search - please try again");
                socket.close();
            };

            return this;
        };

        this.stop = function() {
            socket.close();
        };

    };

    var Template = {

        templates: {
            "dropbox": Handlebars.compile($("#dropbox-result-template").html()),
            "gmail": Handlebars.compile($("#gmail-result-template").html()),
            "google_drive": Handlebars.compile($("#gdrive-result-template").html()),
            "google_contacts": Handlebars.compile($("#gcontacts-result-template").html())
        },

        apply: function(data) {
            return this.templates[data.source](data);
        },

        render: function(templateName, params) {
            params = params === undefined ? {} : params;
            var template = Handlebars.compile($("#" + templateName + "-template").html());
            return template(params);
        }

    };

    var currentQuery;

    function appendContent(content) {
        content.results.map(function(r) {
            $(".search-results").append(Template.apply(r));
        });
        //$('.search-results').quicksand(
        //    $('.result'),
        //    {
        //        attribute: "data-timestamp",
        //        adjustWidth: false,
        //    },
        //    function() {
        //        console.log("Done");
        //    }
        //);

        var sorted = $(".search-results .result").sort(function (a, b) {
            return $(b).data('timestamp') - $(a).data('timestamp');
        });

        $(".search-results").html(sorted);
    }

    function showSearching(content) {
        $(".search-status").html("");
        content.services.forEach(function(service) {
            $(".search-status").append(
                Template.render("searching-indicator", { service: service })
            )
        });
    }

    function showDone(content) {
        $(".search-status .searching." + content.service).removeClass("loading").addClass("done");
    }

    $(".search-box").submit(function(e) {
        e.preventDefault();
        if(currentQuery) {
            currentQuery.stop();
        }

        $(".search-status").html(Template.render("searching"));
        $(".search-results").html("");

        var query = $("#q").val();


        currentQuery = new QueryHandler(query).open(function(res) {
            console.log(res);
            switch(res.event) {
                case "search_started":
                    showSearching(res.content);
                    return;
                case "service_results":
                    appendContent(res.content);
                    return;
                case "search_ended":
                    showDone(res.content);
                    return;
            }
        });
    });

});