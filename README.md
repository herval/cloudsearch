# CloudSearch: a tiny realtime search tool for cloud accounts

`cloudsearch` allows you to index and search content on cloud services such as Google services 
(Gmail, Google Drive, etc) and Dropbox, directly from a command line.

Data gets indexed and stored *on your device only* - this doesn't utilize any intermediate service
for indexing, nor sends your documents or metadata *anywhere*. The only "intermediary" is an [auth gateway](http://github.com/herval/authgateway),
used for Oauth2 token exchanges, but you can always deploy your own auth, for extra independence. It doesn't log or store any tokens.

## Building et al

The `scripts` folder contains build scripts for building, running tests, etc

## Usage

### Configuring an account
> cloudsearch login <account type>

The available account types are `Dropbox` or `Google`.

In order for the OAuth2 loop to complete, `cloudsearch` will require your machine to accept inbound HTTP 
requests while adding an account. The default port is `65432`, but you can override it with the `--oauthPort` flag 

### Searching for content
> cloudsearch search foo

You can narrow down your results with cloudsearch's query macros:

* `before:2006-02-01` - only get documents created or modified _before_ the given date
* `after:2006-02-01` - only get documents created or modified _after_ the given date
* `mode:live` - only search for documents on the cloud services directly, skipping local cache
* `mode:cache` - only search for documents locally (pre-cached results)
* `type:<document type>` - include only results of a given type. Options include Application, Calendar, Contact, Document, Email, Event, File, Folder, Image, Message, Post, Task, Video
* `service:<Dropbox | Google>` - only get results from the given service

An advanced search would look like this:

> cloudsearch search foo before:2006-02-01 after: 2005-02-01 mode:cache type:Email type:Image service:Google
 
 In this example, your search results would only include emails and images, saved in cache, from Google accounts, created between two given dates.

### Interactive search
If you start `cloudsearch` with no parameters, you'll get into interactive mode. This will allow you to do search-as-you-type. You can navigate
on items using up/down arrows. Pressing enter will open the selected document on your default browser.

### Listing configured accounts
> cloudsearch accounts list

### Removing an account
> cloudsearch accounts remove <account id>


## TODO

- Something, _somewhere_ is importing "testing", which polutes the flags namespace. Ugh.
- Fix search w/ lowercase on titles (not working? eg email subject)
