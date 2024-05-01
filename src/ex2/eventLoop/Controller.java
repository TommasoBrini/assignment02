package ex2.eventLoop;

import ex2.eventLoop.loop.EventLoop;
import ex2.eventLoop.loop.EventLoopImpl;
import ex2.eventLoop.dataEvent.DataEventImpl;
import ex2.gui.GUISearchWord;
import ex2.gui.area.CommandArea;
import ex2.listener.CommandListener;

public class Controller implements CommandListener {
    final private GUISearchWord gui;
    final private EventLoop eventLoop;

    public Controller() {
        this.gui = new GUISearchWord();
        this.eventLoop = new EventLoopImpl();
        this.eventLoop.addViewListener(this.gui);
        this.gui.addInputListener(this);
    }

    @Override
    public void onSearch(final CommandArea commandArea, final String site, final String word, final int maxDepth) {
        this.eventLoop.searchUrl(new DataEventImpl(site, word, maxDepth, 0));
    }

    @Override
    public void onExit() {
        this.eventLoop.stop();
        this.gui.dispose();
    }
}
