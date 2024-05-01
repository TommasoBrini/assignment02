package ex2.gui;

import ex2.gui.area.PrintArea;
import ex2.listener.CommandListener;
import ex2.eventLoop.searcher.Searcher;
import ex2.gui.area.CommandArea;
import ex2.gui.area.HistoryArea;
import ex2.listener.ViewListener;
import ex2.utils.MessageDialogUtils;

import javax.swing.*;
import java.awt.*;

public class GUISearchWord extends JFrame implements ViewListener {
    private static final Dimension FRAME_SIZE = new Dimension(800, 500);

    private final CommandArea commandArea;
    private final PrintArea printArea;
    private final HistoryArea historyArea;


    public GUISearchWord() {
        super("Search Word");
        this.commandArea = new CommandArea();
        this.printArea = new PrintArea();
        this.historyArea = new HistoryArea();

        this.setSize(FRAME_SIZE);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.commandArea.addInputListener(this.printArea);
        this.commandArea.addInputListener(this.historyArea);

        this.setupGraphics();
        this.setResizable(true);
        this.setVisible(true);
    }

    private void setupGraphics() {
        this.setLayout(new BorderLayout());
        this.add(BorderLayout.NORTH, this.commandArea);
        this.add(BorderLayout.EAST, this.historyArea);
        this.add(BorderLayout.CENTER, this.printArea);
    }

    @Override
    public void addInputListener(final CommandListener commandListener) {
        this.commandArea.addInputListener(commandListener);
    }

    @Override
    public void onResponse(final Searcher filter) {
        SwingUtilities.invokeLater(() -> {
            final String url = "URL: %s\n".formatted(filter.url());
            final String info = "Depth[%d] -------- Word = %d\n".formatted(filter.currentDepth(), filter.countWord());
            this.printArea.append(url + info + "--------------------\n");
        });
    }

    @Override
    public void onError(final String message) {
        SwingUtilities.invokeLater(() -> MessageDialogUtils.createError(this, "ERROR: %s".formatted(message)));
    }

}
