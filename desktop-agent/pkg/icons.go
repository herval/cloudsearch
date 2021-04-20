package cloudsearch

// these match fontawesome names (https://fontawesome.com/icons)
func AccountIcon(data AccountType) string {
	switch data {
	case Google:
		return "fab fa-google"
	case Dropbox:
		return "fab fa-dropbox"
	case Github:
		return "fab fa-github"
	case Slack:
		return "fab fa-slack"
	case Trello:
		return "fab fa-trello"
	case Jira:
		return "fab fa-jira"
	case Confluence:
		return "fab fa-confluence"
	default:
		return "far fa-user-circle"
	}
}

// these match fontawesome names (https://fontawesome.com/icons)
func ContentIcon(result *Result, query Query) string {
	switch result.ContentType {
	case File:
		return "fas fa-file"
	case Email:
		if result.Unread {
			return "fas fa-envelope"
		} else {
			return "fas fa-envelope-open"
		}
	case Document:
		return "fas fa-file"
	case Image:
		return "fas fa-image"
	case Contact:
		return "fas fa-user"
	case Message:
		return "fas fa-comment"
	case Folder:
		return "fas fa-folder"
	case Event:
		return "fas fa-calendar-alt"
	case Task:
		return "fas fa-check-square"
	case Post:
		return "fas fa-file-alt"
	case Video:
		return "fas fa-file-video"
	case SearchQuery:
		return "fas fa-search"
	default:
		return "fas fa-file"
	}
}
