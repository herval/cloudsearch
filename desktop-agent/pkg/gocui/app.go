package gocui

import (
	"github.com/herval/cloudsearch-desktop-agent/pkg"
	"github.com/jroimartin/gocui"
)

func StartSearchApp(e *cloudsearch.SearchEngine, registry *cloudsearch.Registry) error {
	c := SearchApp{
		e,
		registry,
	}
	return c.Start()
}

type SearchApp struct {
	engine *cloudsearch.SearchEngine
	registry *cloudsearch.Registry
}

func (*SearchApp) quit(g *gocui.Gui, v *gocui.View) error {
	return gocui.ErrQuit
}

func (app *SearchApp) Start() error {
	g, err := gocui.NewGui(gocui.OutputNormal)
	if err != nil {
		return err
	}
	defer g.Close()

	maxX, maxY := g.Size()

	resultsList := NewResultsList(0, 2, maxX-1, maxY-1)
	bar := NewSearchBar(0, 0, maxX, maxY, app.engine, resultsList, app.registry)

	g.SetManager(bar, resultsList, gocui.ManagerFunc(bar.Focus))
	_, _ = g.SetCurrentView("search_bar")

	// global key bindings

	// quit on ctrl+c
	if err := g.SetKeybinding("", gocui.KeyCtrlC, gocui.ModNone, app.quit); err != nil {
		return err
	}

	// TODO this doesnt trigger?
	//if err := g.SetKeybinding("", gocui.KeyEsc, gocui.ModNone, app.clearInput); err != nil {
	//	return err
	//}

	if err := g.MainLoop(); err != nil && err != gocui.ErrQuit {
		return err
	}
	return nil
}
