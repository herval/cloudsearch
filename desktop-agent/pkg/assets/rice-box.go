package assets

import (
	"time"

	"github.com/GeertJohan/go.rice/embedded"
)

func init() {

	// define files
	file2 := &embedded.EmbeddedFile{
		Filename:    "account_linked.html",
		FileModTime: time.Unix(1553983413, 0),

		Content: string("<html>\n<head>\n    <link rel=\"stylesheet\"\n          href=\"https://use.fontawesome.com/releases/v5.7.2/css/all.css\"\n          integrity=\"sha384-fnmOCqbTlWIlj8LyTjo7mOUStjsKC4pOpQbqyi7RrhN7udi9RwhKkMHpvLbHG9Sr\"\n          crossorigin=\"anonymous\">\n    <style>\n        .centralized {\n            text-align: center;\n            font-size: 5em;\n            padding: 10%;\n        }\n\n        small {\n            font-size: 0.5em;\n        }\n\n    </style>\n\n</head>\n\n<body>\n\n<div class=\"centralized\">\n    <p>\n    <i class=\"far fa-check-circle\"></i>\n    </p>\n    <p>\n        All done!\n    </p>\n    <p>\n        <small>You can close this browser tab now</small>\n    </p>\n</div>\n\n</body>\n\n</html>"),
	}

	// define dirs
	dir1 := &embedded.EmbeddedDir{
		Filename:   "",
		DirModTime: time.Unix(1553983413, 0),
		ChildFiles: []*embedded.EmbeddedFile{
			file2, // "account_linked.html"

		},
	}

	// link ChildDirs
	dir1.ChildDirs = []*embedded.EmbeddedDir{}

	// register embeddedBox
	embedded.RegisterEmbeddedBox(`../../static`, &embedded.EmbeddedBox{
		Name: `../../static`,
		Time: time.Unix(1553983413, 0),
		Dirs: map[string]*embedded.EmbeddedDir{
			"": dir1,
		},
		Files: map[string]*embedded.EmbeddedFile{
			"account_linked.html": file2,
		},
	})
}
